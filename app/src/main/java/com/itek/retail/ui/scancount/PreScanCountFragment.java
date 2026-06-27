package com.itek.retail.ui.scancount;

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
import com.itek.retail.ui.inward.grn.InwardGrnHuScanViewModel;
import com.itek.retail.ui.inward.grn.InwardGrnStartFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Inward grn hu scan fragment.
 */
public class PreScanCountFragment extends BarcodeScanFragment {
  
  public TripStatus tripStatus;
  String tripNumber = "";
  String huNumber = "";
  private InwardGrnHuScanViewModel mViewModel;
  private FragmentInwardGrnHuScanBinding binding;
  private TripInventoryDao tripInventoryDao;

  /**
   * Instantiates a new Inward grn hu scan fragment.
   */
  public PreScanCountFragment(){/*Default/Empty Constructor*/}
  
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
      if(isNonEmpty(AppCommonMethods.getLeftZeroReplacedString(binding.edtHuNumber.getText().toString()))){
        binding.edtHuNumber.setIsViewControlEnabled(false, true);
        String huNumber = binding.edtHuNumber.getText().toString().trim();
    //    huNumber = get30DigitHUNumber(huNumber);
        //TODO callapi
        if(false && AppCommonMethods.isInternetConnected(context, false, false)){
          try{
            JSONObject requestParams = new JSONObject();
            requestParams.put(ParamConstants.TYPE, AppConstants.INWARD);
            requestParams.put(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
            requestParams.put(ParamConstants.K_TRIP_NUMBER, tripNumber);
            callWebService(URLConstants.VERIFY_HU_DATA, requestParams, getArguments(), getString(R.string.progress_msg_validate_hu), true);
          }
          catch(Exception e){ e.printStackTrace(); }
        }
        else if(binding.edtHuNumber.validate()){
          Bundle bundle =PreScanCountFragment.this.getArguments();
          bundle.putString(AppConstants.HU_NUMBER,huNumber);
          context.loadFragment(new ScanCountFragment(),bundle);
          binding.edtHuNumber.setText("");
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