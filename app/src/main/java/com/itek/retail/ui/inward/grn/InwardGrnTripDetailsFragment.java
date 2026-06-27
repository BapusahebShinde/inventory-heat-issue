package com.itek.retail.ui.inward.grn;

import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.StoreInwardTripDetailsAdapter;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.FragmentInwardGrnTripDetailsBinding;
import com.itek.retail.model.TripInventory;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.home.MainActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Inward grn trip details fragment.
 */
public class InwardGrnTripDetailsFragment extends CommonFragment{
  
  public TripStatus tripStatus;
  public String tripNo;
  List<TripInventory> dataList = new ArrayList<>(0);
  private int noOfHu;
  private TripStatusDao tripStatusDao;
  private TripInventoryDao tripInventoryDao;
  private FragmentInwardGrnTripDetailsBinding binding;
  
  /**
   * Instantiates a new Inward grn trip details fragment.
   */
  public InwardGrnTripDetailsFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    tripStatusDao = AppDatabase.getDbInstance(context).TripStatusDao();
    tripInventoryDao = AppDatabase.getDbInstance(context).TripInventoryDao();
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    
    binding = FragmentInwardGrnTripDetailsBinding.inflate(inflater, container, false);
    
    if(getArguments() != null){
      Bundle activityBundle = getArguments();
      final Object obj = extractSerializable(activityBundle, TripStatus.class);
      tripStatus = obj != null && obj instanceof TripStatus ? (TripStatus) obj : null;
      tripNo = tripStatus != null ? tripStatus.getTripNumber() : extractString(activityBundle, AppConstants.TRIP_NUMBER, "");
      noOfHu = tripStatus != null ? tripStatus.getNumberOfHu() : extractInt(activityBundle, AppConstants.HU_NUMBERS, 0);
      final int totalProcessedHu = tripInventoryDao.getProcessedHuCount(tripNo);
      binding.txtTripCount.setText(tripNo);
      binding.textTotalHu.setText(String.valueOf(noOfHu));
      binding.textCompletedHu.setText(String.valueOf(totalProcessedHu) + "/" + noOfHu);
      binding.txtTripUpload.setVisibility(View.VISIBLE);
      binding.txtTripUpload.setTextColor(totalProcessedHu > 0 ? context.getColorPrimaryDarkFromTheme() : getResources().getColor(R.color.txt_sub_sub_header));
    }
    
    binding.txtTripUpload.setOnClickListener(v -> {
      int totalProcessedHu = tripInventoryDao.getProcessedHuCount(tripNo);
      if(totalProcessedHu <= 0) return;
      List<String> countsTrip = tripInventoryDao.getAllTripCountDetails(tripNo);
      String massage = AppConstants.K_TRIP_INFO;
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
      massage += "\n" + AppConstants.K_TRIP_ACCEPT_COUNT + accept + " \n" + AppConstants.K_TRIP_REJECT_COUNT + reject + " \n" + AppConstants.K_TRIP_PENDING_COUNT + pending;
      
      if(accept > 0){
        showTripCompleteConfirmDialog(massage);
      }
      else{
        showTripAllRejectedDialog(massage);
      }
    });
    
    binding.imgStart.setOnClickListener(v -> {
      showLog("GO", "HU SCAN");
      Bundle arg = getArguments();
      if(arg == null){
        arg = new Bundle();
      }
      arg.putString(AppConstants.TRIP_NUMBER, tripNo);
      arg.putSerializable(tripStatus.getClass().getSimpleName(), (Serializable) tripStatus);
      
      context.loadFragment(new InwardGrnHuScanFragment(), arg);
    });
    
    List<TripInventory> listTripData = tripInventoryDao.getOriginalHuDetails(tripNo);
    if(isNonEmpty(listTripData)) dataList.addAll(listTripData);
    
    binding.listTripDetails.setAdapter(new StoreInwardTripDetailsAdapter((MainActivity) context, InwardGrnTripDetailsFragment.this, dataList));
    binding.listTripDetails.setLayoutManager(new LinearLayoutManager(context));
    
    updateLists();
    
    return binding.getRoot();
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    int totalProcessedHu = tripInventoryDao.getProcessedHuCount(tripNo);
    binding.textCompletedHu.setText(String.valueOf(totalProcessedHu) + "/" + noOfHu);
    binding.txtTripUpload.setTextColor(totalProcessedHu > 0 ? context.getColorPrimaryDarkFromTheme() : getResources().getColor(R.color.txt_sub_sub_header));
    dataList.clear();
    List<TripInventory> listTripData = tripInventoryDao.getOriginalHuDetails(tripNo);
    if(isNonEmpty(listTripData)) dataList.addAll(listTripData);
    if(binding != null && binding.listTripDetails != null && binding.listTripDetails.getAdapter() != null)
      ((RecyclerView.Adapter) binding.listTripDetails.getAdapter()).notifyDataSetChanged();
  }
  
  @Override
  public void onResume(){
    super.onResume();
    updateLists();
  }
  
  @Override
  public void onBackPressed(){
    if(tripInventoryDao.getProcessedHuCount(tripNo) > 0){
      int totalProcessedHu = tripInventoryDao.getProcessedHuCount(tripNo);
      if(totalProcessedHu <= 0) return;
      List<String> countsTrip = tripInventoryDao.getAllTripCountDetails(tripNo);
      String massage = AppConstants.K_TRIP_INFO;
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
      massage += "\n" + AppConstants.K_TRIP_ACCEPT_COUNT + accept + " \n" + AppConstants.K_TRIP_REJECT_COUNT + reject + " \n" + AppConstants.K_TRIP_PENDING_COUNT + pending;
      showTripInprogressConfirmDialog(massage);
    }
    else super.onBackPressed();
  }
  
  /**
   * Show trip complete confirm dialog.
   *
   * @param msg the msg
   */
  public void showTripCompleteConfirmDialog(final String msg){
    context.showCustomAlertDialog("", msg + "\n" + getString(R.string.msg_complete_trip_alert), R.string.btn_yes, (dialog, which) -> {
      tripStatusDao.updateCompleteTripStatus(tripNo, AppConstants.INWARD);
      popBackStack();
    }, R.string.btn_no, (dialog, which) -> dialog.dismiss());
  }
  
  /**
   * Show trip all rejected dialog.
   *
   * @param msg the msg
   */
  public void showTripAllRejectedDialog(final String msg){
    context.showCustomErrDialog(msg + "\n" + getString(R.string.msg_all_reject_trip_alert));
  }
  
  /**
   * Show trip inprogress confirm dialog.
   *
   * @param msg the msg
   */
  public void showTripInprogressConfirmDialog(final String msg){
    context.showCustomAlertDialog("", msg + "\n" + getString(R.string.msg_inprogess_trip_alert), R.string.btn_yes, (dialog, which) -> {
      tripStatusDao.updateInProgressTripStatus(tripNo, AppConstants.INWARD);
      popBackStack();
    }, R.string.btn_no, (dialog, which) -> dialog.dismiss());
  }
}