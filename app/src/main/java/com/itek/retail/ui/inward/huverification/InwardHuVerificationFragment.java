package com.itek.retail.ui.inward.huverification;

import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.InwardHuVerificationAdapter;
import com.itek.retail.adapter.ProdDisplayDataListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.BarcodeScanFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.FragmentInwardHuVerificationBinding;
import com.itek.retail.model.InwardHuVerificationModel;
import com.itek.retail.model.LabelValues;
import com.itek.retail.ui.customviews.MaxHeightRecyclerView;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Inward hu verification fragment.
 */
public class InwardHuVerificationFragment extends BarcodeScanFragment {

  List<InwardHuVerificationModel> dataList = new ArrayList<>(0);
  private InwardHuVerificationViewModel mViewModel;
  private FragmentInwardHuVerificationBinding binding;

  /**
   * Instantiates a new Inward hu verification fragment.
   */
  public InwardHuVerificationFragment(){
    // Required empty public constructor
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(InwardHuVerificationViewModel.class);
    binding = FragmentInwardHuVerificationBinding.inflate(inflater, container, false);
    
    final String tripNumber = extractString(getArguments(), AppConstants.TRIP_NUMBER, "");
    binding.txtTripNumber.setText(HtmlCompat.fromHtml(isNonEmpty(tripNumber) ? String.format(getString(R.string.txt_trip), tripNumber) : "", HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtTripNumber.setVisibility(isNonEmpty(tripNumber) ? View.VISIBLE : View.GONE);
    //check if this HU is in list or not
    binding.btnHuVerification.setOnClickListener(v -> {
      if(isNonEmpty(AppCommonMethods.getLeftZeroReplacedString(context,binding.edtSearchHu.getText().toString()))){
        binding.edtSearchHu.setIsViewControlEnabled(false, true);
        String huNumber = binding.edtSearchHu.getText().toString().trim();
        huNumber = get20DigitHUNumber(huNumber);
        if(true && AppCommonMethods.isInternetConnected(context, false, false)){
          try{
            JSONObject requestParams = new JSONObject();
            requestParams.put(ParamConstants.TYPE, AppConstants.INWARD);
            requestParams.put(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
            requestParams.put(ParamConstants.K_TRIP_NUMBER, tripNumber);
            callWebService(URLConstants.VERIFY_HU_DATA, requestParams, getArguments(), getString(R.string.progress_msg_validate_hu), true);
          }
          catch(Exception e){ e.printStackTrace(); }
        }
        else checkIsHuNumberExist(huNumber, tripNumber);
      }
      else binding.edtSearchHu.validate();
    });
    
    dataList.clear();
    dataList.addAll(AppDatabase.getTripInventoryDao(context).getHuVerified(tripNumber));
    InwardHuVerificationAdapter inwardHuVerificationAdapter = new InwardHuVerificationAdapter(dataList, getContext());
    binding.rvHuVerification.setAdapter(inwardHuVerificationAdapter);
    binding.rvHuVerification.setLayoutManager(new LinearLayoutManager(context));

    setInputView(binding.edtSearchHu,binding.btnHuVerification);
    
    return binding.getRoot();
  }
  
  /**
   * Check is hu number exist.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   */
  public void checkIsHuNumberExist(String huNumber, final String tripNumber){ checkIsHuNumberExist(huNumber, tripNumber, false); }
  
  /**
   * Check is hu number exist.
   *
   * @param huNumber     the hu number
   * @param tripNumber   the trip number
   * @param isHuVerified the is hu verified
   */
  public void checkIsHuNumberExist(String huNumber, final String tripNumber, boolean isHuVerified){
    AppDatabase db = AppDatabase.getDbInstance(context);
    TripStatusDao tripStatusDao = db.TripStatusDao();
    TripInventoryDao tripInventoryDao = db.TripInventoryDao();
    
    final String tripNum = isNonEmpty(tripNumber) ? tripNumber : tripInventoryDao.geTripNoOfHu(huNumber);
    Boolean isVerified = tripNum == null ? null : tripInventoryDao.isHuVerified(tripNum, huNumber);
    if(!isHuVerified && isVerified == null){
      context.showCustomErrDialog(String.format(getString(R.string.err_inward_invalid_hu_scanned), huNumber));
      clearBarcode();
      hideKeyboard();
    }
    else if(chkNotNullTrue(isVerified)){
      context.showCustomSuccessDialog(String.format(getActivity().getResources().getString(R.string.store_inward_hu_already_verified), huNumber), false, false);
      binding.edtSearchHu.setText("");
      hideKeyboard();
    }
    else if(chkNotNullFalse(isVerified)){
      tripInventoryDao.updateHuVerified(tripNum, huNumber);
      tripStatusDao.updateHUVerifyCount(tripNum, AppConstants.INWARD);
      context.showCustomSuccessDialog(String.format(getActivity().getResources().getString(R.string.store_inward_hu_verified), huNumber), false, false);
      binding.edtSearchHu.setText("");
      hideKeyboard();
      dataList.clear();
      dataList.addAll(tripInventoryDao.getHuVerified(tripNumber));
      if(binding.rvHuVerification != null && binding.rvHuVerification.getAdapter() != null)
        ((RecyclerView.Adapter) binding.rvHuVerification.getAdapter()).notifyDataSetChanged();
    }
  }
  
  /**
   * Clear barcode.
   */
  public void clearBarcode(){
    binding.edtSearchHu.setText("");
    //binding.edtSearchHu.setIsViewControlEnabled(true);
  }
  
  /**
   * Get default json array.
   *
   * @param jsonResponse the json response
   * @param huNum        the hu num
   * @return the json array
   */
  JSONArray getDefault(final JSONObject jsonResponse, final String huNum){
    JSONArray jsonDataArray = new JSONArray();
    if(jsonResponse != null){
      final String tripNo = extractString(jsonResponse, ParamConstants.K_TRIP_NUMBER);
      final String deliveryNo = extractString(jsonResponse, ParamConstants.K_TRIP_DELIVERY_NUMBER);
      final String huNo = extractString(jsonResponse, ParamConstants.K_TRIP_HU_NUMBER);
      final String senderName = extractString(jsonResponse, ParamConstants.SENDER_NAME);
      final String senderCode = extractString(jsonResponse, ParamConstants.SENDER_CODE);
      final String receiverName = extractString(jsonResponse, ParamConstants.RECEIVER_NAME);
      final String receiverCode = extractString(jsonResponse, ParamConstants.RECEIVER_CODE);
      if(isNonEmpty(tripNo)){
        try{
          JSONObject jobj = new JSONObject();
          jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_trip_no));
          jobj.put(ParamConstants.VALUE, tripNo);
          jsonDataArray.put(jobj);
        }
        catch(Exception e){ e.printStackTrace(); }
      }
      if(isNonEmpty(deliveryNo)){
        try{
          JSONObject jobj = new JSONObject();
          jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_delivery_no));
          jobj.put(ParamConstants.VALUE, deliveryNo);
          jsonDataArray.put(jobj);
        }
        catch(Exception e){ e.printStackTrace(); }
      }
      if(isNonEmpty(chkNull(huNo, huNum))){
        try{
          JSONObject jobj = new JSONObject();
          jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_hu_no));
          jobj.put(ParamConstants.VALUE, chkNull(huNo, huNum));
          jsonDataArray.put(jobj);
        }
        catch(Exception e){ e.printStackTrace(); }
      }
      if(isNonEmpty(senderName)){
        try{
          JSONObject jobj = new JSONObject();
          jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_sender_name));
          jobj.put(ParamConstants.VALUE, senderName);
          jsonDataArray.put(jobj);
        }
        catch(Exception e){ e.printStackTrace(); }
      }
      if(isNonEmpty(senderCode)){
        try{
          JSONObject jobj = new JSONObject();
          jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_sender_code));
          jobj.put(ParamConstants.VALUE, senderCode);
          jsonDataArray.put(jobj);
        }
        catch(Exception e){ e.printStackTrace(); }
      }
      if(isNonEmpty(receiverName)){
        try{
          JSONObject jobj = new JSONObject();
          jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_receiver_name));
          jobj.put(ParamConstants.VALUE, receiverName);
          jsonDataArray.put(jobj);
        }
        catch(Exception e){ e.printStackTrace(); }
      }
      if(isNonEmpty(receiverCode)){
        try{
          JSONObject jobj = new JSONObject();
          jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_receiver_code));
          jobj.put(ParamConstants.VALUE, receiverCode);
          jsonDataArray.put(jobj);
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    }
    return jsonDataArray;
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.VERIFY_HU_DATA:
          final String tripNum = extractString(jsonRequest, ParamConstants.K_TRIP_NUMBER, "");
          final String huNum = extractString(jsonRequest, ParamConstants.K_TRIP_HU_NUMBER, "");
          final String receiverCode = extractString(jsonResponse, ParamConstants.RECEIVER_CODE);
          final boolean isStoreMatching = isNullOrEmpty(receiverCode) || isNullOrEmpty(SharedPrefManager.getStoreCode()) || receiverCode.equalsIgnoreCase(SharedPrefManager.getStoreCode());
          final JSONArray displayData = extractJSONArray(jsonResponse, ParamConstants.DI, getDefault(jsonResponse, responseCode == 200 || responseCode == 400 || responseCode == 411 ? huNum : ""));
          if(displayData != null && displayData.length() > 0){
            List<LabelValues> listData = new ArrayList<>(0);
            for(int i = 0; i < displayData.length(); i++){
              try{
                final LabelValues lblValues = AppCommonMethods.getGSON().fromJson(displayData.getJSONObject(i).toString(), LabelValues.class);
                if(lblValues != null) listData.add(lblValues);
              }
              catch(Exception e){ e.printStackTrace(); }
            }
            MaxHeightRecyclerView listDisplayData = null;
            if(isNonEmpty(listData)){
              //Set Data In List Ad
              int margin = getResources().getDimensionPixelSize(R.dimen.dp_15);
              LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
              llParams.setMargins(margin, margin, margin, 0);
              listDisplayData = new MaxHeightRecyclerView(context);
              listDisplayData.setLayoutParams(llParams);
              listDisplayData.setPadding(margin, margin, margin, 0);
              listDisplayData.setAdapter(new ProdDisplayDataListAdapter((MainActivity) context, listData));
              listDisplayData.setLayoutManager(new LinearLayoutManager(context));
            }
            context.showCustomAlertDialog("", extractString(jsonResponse, ParamConstants.MESSAGE, !isStoreMatching ? String.format(getString(R.string.err_inw_wrong_store), huNum, SharedPrefManager.getStoreCode()) : isSuccess ? String.format(getString(R.string.store_inward_hu_verified), huNum) : ""), listDisplayData, isSuccess && isStoreMatching, false, getString(R.string.btn_ok), new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialogInterface, int i){
                if(isStoreMatching)
                  checkIsHuNumberExist(huNum, tripNum, isSuccess && isStoreMatching);
              }
            });
          }
          else checkIsHuNumberExist(huNum, tripNum);
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}