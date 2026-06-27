package com.itek.retail.ui.outward.huverification;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;
import com.itek.retail.R;
import com.itek.retail.adapter.pager.TripTypesPagerAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.FragmentOutwardHuDataBinding;
import com.itek.retail.model.TripInventory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Outward grn trips data fragment.
 */
public class OutwardHuDataFragment extends CommonFragment{
  
  private TripStatusDao tripStatusDao;
  private TripInventoryDao tripInventoryDao;
  private FragmentOutwardHuDataBinding binding;
  private OutwardHuDataViewModel mViewModel;
  
  /**
   * Instantiates a new Outward trips data fragment.
   */
  public OutwardHuDataFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    tripStatusDao = AppDatabase.getDbInstance(context).TripStatusDao();
    tripInventoryDao = AppDatabase.getDbInstance(context).TripInventoryDao();
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    binding = FragmentOutwardHuDataBinding.inflate(inflater, container, false);
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OutwardHuDataViewModel.class);
    
    final List<String> listTripTypeTabs = tripStatusDao != null ? tripStatusDao.getTripTypes(AppConstants.OUTWARD) : new ArrayList<>(0);
    
    binding.viewPager.setAdapter(new TripTypesPagerAdapter(this, listTripTypeTabs));
    new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> tab.setText(AppCommonMethods.toTitleCase(listTripTypeTabs.get(position)).replaceAll(" To ", " to "))).attach();
    
    binding.btnUpload.setOnClickListener(view -> {
      if(view.getVisibility() != View.VISIBLE) return;
      if(binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
        return;
      showTripCompleteConfirmDialog(tripStatusDao.getCompletedTrips(AppConstants.OUTWARD));
    });
    
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          binding.btnSwipeUpload.reset();
          showTripCompleteConfirmDialog(tripStatusDao.getCompletedTrips(AppConstants.OUTWARD));
        }
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);
    
    return binding.getRoot();
  }
  
  /**
   * Show trip complete confirm dialog.
   *
   * @param listTripNos the list trip nos
   */
  public void showTripCompleteConfirmDialog(List<String> listTripNos){
    if(isNonEmpty(listTripNos)){
      String message = "";
      if(listTripNos.size() == 1){
        int totalProcessedHu = tripInventoryDao.getProcessedHuCount(listTripNos.get(0));
        if(totalProcessedHu <= 0) return;
        List<String> countsTrip = tripInventoryDao.getAllTripCountDetails(listTripNos.get(0));
        message = AppConstants.K_TRIP_INFO;
        int accept = 0;
        int reject = 0;
        int pending = 0;
        
        for(String tripdata : countsTrip){
          if(tripdata.contains(",") && tripdata.split(",").length > 2){
            accept += AppCommonMethods.parseInt(tripdata.split(",")[0].trim());
            reject += AppCommonMethods.parseInt(tripdata.split(",")[1].trim());
            pending += AppCommonMethods.parseInt(tripdata.split(",")[2].trim());
          }
        }
        message += "\n" + AppConstants.K_TRIP_ACCEPT_COUNT + accept + " \n" + AppConstants.K_TRIP_REJECT_COUNT + reject + " \n" + AppConstants.K_TRIP_PENDING_COUNT + pending;
      }
      else{
        message = AppConstants.K_TRIP_COMPLETED + (listTripNos.size());
      }
      context.showCustomAlertDialog(AppConstants.K_UPLOAD_TRIP, message + "\n" + getString(R.string.msg_upload_trip_alert), R.string.btn_upload, (dialog, which) -> uploadTrip(listTripNos), R.string.btn_cancel, null);
    }
  }
  
  /**
   * Upload trip.
   *
   * @param listTripNos the list trip nos
   */
  public void uploadTrip(final List<String> listTripNos){
    new Handler().post(new Runnable(){
      @Override
      public void run(){
        try{
          showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          JSONObject requestParams = new JSONObject();
          requestParams.put(ParamConstants.SESSION_TYPE, AppCommonMethods.SessionType.OUTWARD.name());
          requestParams.put(ParamConstants.TYPE, AppConstants.OUTWARD);
          requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD);
          if(isNonEmpty(listTripNos)){
            Bundle args = new Bundle();
            args.putStringArrayList(AppConstants.TRIP_NUMBERS, new ArrayList<String>(listTripNos));
            JSONArray tripJsonArray = new JSONArray();
            for(String tripNo : listTripNos){
              JSONObject jobj = new JSONObject();
              jobj.put(ParamConstants.K_TRIP_NUMBER, tripNo);
              jobj.put(ParamConstants.K_TRIP_TYPE, tripStatusDao.getTripType(tripNo, AppConstants.OUTWARD));
              List<TripInventory> list = tripInventoryDao.getAllTripInventory(tripNo);
              JSONArray jhuArray = new JSONArray();
              String hu = "";
              int hurecivedqty = 0;
              JSONObject huobj = null;
              JSONArray jArrayRFIDItems = null;
              JSONArray jArrayBarcodeItems = null;
              if(isNonEmpty(list)){
                for(TripInventory tripInventory : list){
                  if(!hu.equalsIgnoreCase(tripInventory.huNo)){
                    if(huobj != null){
                      huobj.put(ParamConstants.ITEMS, jArrayRFIDItems);
                      huobj.put(ParamConstants.K_TRIP_ITEM_BARCODE, jArrayBarcodeItems);
                      jhuArray.put(huobj);
                      hurecivedqty = 0;
                    }
                    
                    hu = tripInventory.huNo;
                    hurecivedqty = hurecivedqty + tripInventory.eanQty;
                    
                    huobj = new JSONObject();
                    jArrayRFIDItems = new JSONArray();
                    jArrayBarcodeItems = new JSONArray();
                    huobj.put(ParamConstants.K_TRIP_DELIVERY_NUMBER, tripInventory.deliveryNo);
                    huobj.put(ParamConstants.K_TRIP_HU_NUMBER, hu);
                    huobj.put(ParamConstants.K_TRIP_RECEIVED_QTY, hurecivedqty);
                    huobj.put(ParamConstants.K_TRIP_HU_STATUS, tripInventory.status.replaceFirst("A", "C"));
                    huobj.put(ParamConstants.K_TRIP_HU_REASON, tripInventory.reason);
                  }
                  else if(tripInventory.epc.isEmpty()){
                    hurecivedqty = hurecivedqty + tripInventory.eanQty;
                  }
                  
                  if(tripInventory.epc.length() > 0){
                    String userAction = tripInventory.userAction;
                    
                    JSONObject item = tripInventory.toJson(context);
                    JSONObject barcodeItem = tripInventory.toBarcodeJson();
                    
                    if(userAction.equalsIgnoreCase("RFID")){
                      jArrayRFIDItems.put(item);
                    }
                    else{
                      jArrayBarcodeItems.put(barcodeItem);
                    }
                  }
                }
              }
              if(huobj != null){
                huobj.put(ParamConstants.ITEMS, jArrayRFIDItems);
                huobj.put(ParamConstants.K_TRIP_ITEM_BARCODE, jArrayBarcodeItems);
                
                jhuArray.put(huobj);
              }
              jobj.put(ParamConstants.K_TRIP_HU_DETAILS, jhuArray);
              tripJsonArray.put(jobj);
            }
            requestParams.put(ParamConstants.K_TRIPS_DATA, tripJsonArray);
            
            callWebService(URLConstants.UPLOAD_OUTWARD, requestParams, args, getString(R.string.progress_msg_uploading_data));
          }
        }
        catch(JSONException e){
          e.printStackTrace();
          hideProgressDialog();
        }
      }
    });
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    
  }
  
  @Override
  public void onResume(){
    super.onResume();
    updateLists();
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    if(binding != null && binding.btnUpload != null && binding.btnSwipeUpload != null){
      binding.btnUpload.setVisibility(tripStatusDao.getCompletedTripsCount(AppConstants.OUTWARD) > 0 ? View.VISIBLE : View.GONE);
      binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && tripStatusDao.getCompletedTripsCount(AppConstants.OUTWARD) > 0 ? View.VISIBLE : View.GONE);
    }
  }
  
  @Override
  public void onBackPressed(){
    if(!tripStatusDao.isAllTripsPending(AppConstants.OUTWARD)){
      String counts = tripStatusDao.getTripsStatusCounts(AppConstants.OUTWARD);
      if(counts != null && counts.length() > 0 && counts.contains(",") && counts.split(",").length > 2){
        context.showCustomAlertDialog(getString(R.string.title_trips_cancel_process), String.format(getString(R.string.msg_trips_cancel_process), counts.split(",")[0], counts.split(",")[1], counts.split(",")[2]), R.string.btn_yes, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            context.popBackStack();
          }
        }, R.string.btn_no, null);
      }
      else super.onBackPressed();
    }
    else super.onBackPressed();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.UPLOAD_OUTWARD:
          if(isSuccess){
            String msg = extractString(jsonResponse, ParamConstants.MESSAGE, "");
            final JSONArray completedTrips = extractJSONArray(jsonResponse, ParamConstants.SUCCEED_TRIPS);
            final JSONArray failedTrips = extractJSONArray(jsonResponse, ParamConstants.FAILED_TRIPS);
            final Set<String> completedTripNos = new HashSet<String>(0);
            if(completedTrips != null && completedTrips.length() > 0){
              msg += "\n" + "Successfully Uploaded Trip(s):" + completedTrips.length();
              for(int i = 0; i < completedTrips.length(); i++){
                JSONObject compTrip = completedTrips.getJSONObject(i);
                if(compTrip != null){
                  final String tripNo = extractString(compTrip, ParamConstants.K_TRIP_NUMBER);
                  final String reason = extractString(compTrip, ParamConstants.REASON);
                  if(isNonEmpty(tripNo)) completedTripNos.add(tripNo);
                  if(isNonEmpty(tripNo) && isNonEmpty(reason)) msg += "\n" + tripNo + ": " + reason;
                }
              }
              if(isNonEmpty(completedTripNos)){
                tripStatusDao.deleteUploadAllTripStatus(completedTripNos, AppConstants.OUTWARD);
                tripInventoryDao.deleteAllUploadedTripInventoryData(completedTripNos);
              }
            }
            if(failedTrips != null && failedTrips.length() > 0){
              msg += "\nFailed to Upload Trip(s):" + failedTrips.length();
              for(int i = 0; i < failedTrips.length(); i++){
                JSONObject failTrip = failedTrips.getJSONObject(i);
                if(failTrip != null){
                  final String tripNo = extractString(failTrip, ParamConstants.K_TRIP_NUMBER);
                  final String reason = extractString(failTrip, ParamConstants.REASON);
                  if(isNonEmpty(tripNo) && isNonEmpty(reason)) msg += "\n" + tripNo + ": " + reason;
                }
              }
            }
            if(isNonEmpty(completedTripNos))
              context.showCustomSuccessDialog(msg.trim(), tripStatusDao.getTripsCount(AppConstants.OUTWARD) == 0);
            else context.showCustomErrDialog(msg.trim());
          }
          updateLists();
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}