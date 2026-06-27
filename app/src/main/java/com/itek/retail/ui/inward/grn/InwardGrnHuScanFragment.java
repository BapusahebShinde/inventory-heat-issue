package com.itek.retail.ui.inward.grn;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.BarcodeScanFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.databinding.FragmentInwardGrnHuScanBinding;
import com.itek.retail.model.TripInventory;
import com.itek.retail.model.TripStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Inward grn hu scan fragment.
 */
public class InwardGrnHuScanFragment extends BarcodeScanFragment {
  
  public TripStatus tripStatus;
  String tripNumber = "";
  String huNumber = "";
  private InwardGrnHuScanViewModel mViewModel;
  private FragmentInwardGrnHuScanBinding binding;
  private TripInventoryDao tripInventoryDao;

  /**
   * Instantiates a new Inward grn hu scan fragment.
   */
  public InwardGrnHuScanFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    tripInventoryDao = AppDatabase.getTripInventoryDao(context);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(InwardGrnHuScanViewModel.class);
    binding = FragmentInwardGrnHuScanBinding.inflate(inflater, container, false);
    
    if(getArguments() != null){
      Object obj = extractSerializable(getArguments(), TripStatus.class);
      tripStatus = obj != null && obj instanceof TripStatus ? (TripStatus) obj : null;
      showLog("tripStatus", String.valueOf(tripStatus));
      tripNumber = tripStatus != null ? tripStatus.getTripNumber() : extractString(getArguments(), AppConstants.TRIP_NUMBER, "");
      binding.edtHuNumber.setText(AppCommonMethods.isSetInwOnline ? extractString(getArguments(), AppConstants.HU_NUMBER, "") : "");
      if(binding.edtHuNumber.getText().length() > 0) binding.btnHUGo.performClick();
    }
    
    showLog("tripNumber", tripNumber);
    binding.txtTripNumber.setText(HtmlCompat.fromHtml(isNonEmpty(tripNumber) ? String.format(getString(R.string.txt_trip), tripNumber) : "", HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtTripNumber.setVisibility(isNonEmpty(tripNumber) ? View.VISIBLE : View.GONE);
    
    binding.btnHUGo.setOnClickListener(v -> {
      if(isNonEmpty(AppCommonMethods.getLeftZeroReplacedString(context,binding.edtHuNumber.getText().toString()))){
        binding.edtHuNumber.setIsViewControlEnabled(false, true);
        final String huNumber = get20DigitHUNumber(binding.edtHuNumber.getText().toString().trim());
        if(AppCommonMethods.isSetInwOnline){
          try{
            JSONObject requestParams = new JSONObject();
            requestParams.put(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
            callWebService(URLConstants.GET_INWARD_HU_DATA, requestParams, getString(R.string.progress_msg_getting_data));
          }
          catch(Exception e){ e.printStackTrace(); }
        }
        else{
          if(!tripInventoryDao.isHuPresentInTrip(tripNumber, huNumber)){
            context.showCustomErrDialog(String.format(getResources().getString(R.string.err_inward_invalid_hu_scanned), huNumber));
            clearBarcode();
          }
          else{
            this.huNumber = huNumber;
            final int totalArticleQty = tripInventoryDao.getCurrentHuArticleQty(this.huNumber, tripNumber);
            final int totalEanQty = tripInventoryDao.getCurrentHuEanQty(this.huNumber, tripNumber);
            final int totalQty = tripInventoryDao.getCurrentHuQty(this.huNumber, tripNumber);
            hideKeyboard();
            if(totalArticleQty <= 0){
              context.showCustomErrDialog(String.format(context.getString(R.string.err_msg_error_no_article), this.huNumber));// no article
              return;
            }
            else if(totalEanQty <= 0){
              context.showCustomErrDialog(String.format(context.getString(R.string.err_msg_error_no_ean), this.huNumber));// no ean
              return;
            }
            else if(totalQty <= 0){
              context.showCustomErrDialog(String.format(context.getString(R.string.err_msg_error_no_qty), this.huNumber));// no ean
              return;
            }
            else if(tripInventoryDao.isHappyStatus(huNumber, tripNumber)){
              context.showCustomAlertDialog(getString(R.string.err_title_inward_data), getString(R.string.err_msg_inward_data), null, false, true, getString(R.string.btn_ok), null, "", null);
              return;
            }
            else if(!tripInventoryDao.getHuStatus(huNumber, tripNumber).equalsIgnoreCase("P") && !tripInventoryDao.getIsDuplicate(huNumber, tripNumber)){
              context.showCustomAlertDialog(null, context.getString(R.string.err_msg_inward_delete_data), context.getString(R.string.btn_yes), (dialogInterface, i) -> {
                deleteHURecords();
                Bundle arg = chkNull(getArguments(), new Bundle());
                arg.putString(AppConstants.TRIP_NUMBER, this.tripNumber);
                arg.putString(AppConstants.HU_NUMBER, this.huNumber);
                popBackStack();
                handleFragmentRedirection(new InwardGrnStartFragment(), arg);
              }, context.getString(R.string.btn_no), null);
              return;
            }
            else if(tripInventoryDao.getHuStatus(huNumber, tripNumber).equalsIgnoreCase("P")){
              deleteHURecords();
            }
            Bundle arg = chkNull(getArguments(), new Bundle());
            arg.putString(AppConstants.TRIP_NUMBER, tripNumber);
            arg.putString(AppConstants.HU_NUMBER, this.huNumber);
            popBackStack();
            handleFragmentRedirection(new InwardGrnStartFragment(), arg);
          }
        }
      }
      else binding.edtHuNumber.validate();
    });

    setInputView(binding.edtHuNumber,binding.btnHUGo);
    
    return binding.getRoot();
  }
  
  /**
   * Clear barcode.
   */
  public void clearBarcode(){
    binding.edtHuNumber.setText("");
    //binding.edtHuNumber.setIsViewControlEnabled(true);
  }
  
  /**
   * Delete hu records.
   */
  public void deleteHURecords(){
    tripInventoryDao.deleteHU(huNumber, tripNumber);
    tripInventoryDao.updateHUattemptcount(huNumber, tripNumber);
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_INWARD_HU_DATA:
          showLog("inward", "info:" + jsonResponse);
          if(isSuccess){
            final TripStatus tripStatus = new Gson().fromJson(jsonResponse.toString(), TripStatus.class);
            final TripInventory tripInventory1 = new Gson().fromJson(jsonResponse.toString(), TripInventory.class);
            huNumber = tripInventory1 != null ? tripInventory1.huNo : extractString(jsonResponse, ParamConstants.K_TRIP_HU_NUMBER, extractString(jsonRequest, ParamConstants.K_TRIP_HU_NUMBER, ""));
            tripNumber = tripStatus != null ? tripStatus.getTripNumber() : extractString(jsonResponse, ParamConstants.K_TRIP_NUMBER, "");
            
            if(tripStatus != null){
              tripStatus.setType(AppConstants.INWARD);
              JSONArray jsonArticles = AppCommonMethods.extractJSONArray(jsonResponse, ParamConstants.K_ARTICLE_DATA);
              if(isNonEmpty(jsonArticles)){
                tripInventoryDao.deleteAllTripInventory();
                List<TripInventory> list = new ArrayList<>(0);
                for(int i = 0; i < jsonArticles.length(); i++){
                  TripInventory tripInventory = new Gson().fromJson(jsonArticles.getJSONObject(i).toString(), TripInventory.class);
                  tripInventory.tripNo = tripInventory1.tripNo;
                  tripInventory.deliveryNo = tripInventory1.deliveryNo;
                  tripInventory.huNo = tripInventory1.huNo;
                  tripInventory.isHuVerified = tripInventory1.isHuVerified;
                  tripInventory.fromVendor = tripInventory1.fromVendor;
                  tripInventory.apparelBarcodeBased = tripInventory1.apparelBarcodeBased;
                  tripInventory.status = tripInventory1.status;
                  list.add(tripInventory);
                }
                tripInventoryDao.insertAll(list);
              }
            }
            args = args == null ? new Bundle() : args;
            args.putSerializable(tripStatus.getClass().getSimpleName(), (Serializable) tripStatus);
            args.putString(ParamConstants.K_TRIP_NUMBER, tripNumber);
            args.putString(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
            handleFragmentRedirection(new InwardGrnStartFragment(), args);
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
}