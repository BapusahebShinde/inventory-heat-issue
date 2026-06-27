package com.itek.retail.ui.inward1;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isAllowLowerCaseTripNumber;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.itek.retail.R;
import com.itek.retail.adapter.TripListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.BarcodeScanFragment;
import com.itek.retail.common.InsertDBHUDetails;
import com.itek.retail.common.InsertDBHUs;
import com.itek.retail.common.InsertDBTrips;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.HUDetailsDao;
import com.itek.retail.database.HUStatusDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.FragmentTripListBinding;
import com.itek.retail.model.HUStatus;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.customviews.InputView;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.home.MainViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The Trip List fragment.
 */
public class TripListFragment extends BarcodeScanFragment {
  
  private String typeIO = AppConstants.INWARD;
  private String labelTrip = "Trip";
  private String labelHU = "Carton";
  private FragmentTripListBinding binding;
  private InOutWardPhase1ViewModel inOutWardPhase1ViewModel;
  private TripStatusDao tripStatusDao;
  private HUStatusDao huStatusDao;
  private HUDetailsDao huDetailsDao;
  private ArrayList<TripStatus> dataList = new ArrayList<>(0);
  private String sortByValues = "";
  private MainViewModel mainViewModel;
  private boolean is1stTime=false;
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    tripStatusDao = AppDatabase.getDbInstance(this.context).TripStatusDao();
    huStatusDao = AppDatabase.getDbInstance(this.context).HUStatusDao();
    huDetailsDao = AppDatabase.getDbInstance(this.context).HUDetailsDao();
    is1stTime = savedInstanceState==null;
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    binding = FragmentTripListBinding.inflate(inflater, container, false);
    
    typeIO = extractString(getArguments(), ParamConstants.OPERATION_TYPE, extractString(getArguments(), ParamConstants.TYPE, AppConstants.INWARD));
    showLog("noOfTrips", "" + tripStatusDao.getTripsAllCount(typeIO));
    
    binding.btnGo.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && allowBtnClick && binding.ivTripNo.validate()){
          final String tripNum = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_LOWER_CASE_TRIP_NUMBER,isAllowLowerCaseTripNumber)?binding.ivTripNo.getText().toString().trim():binding.ivTripNo.getText().toString().toUpperCase().trim();
          final TripStatus tripStatus = tripStatusDao.getTripData(tripNum, typeIO);
          if(tripStatus != null) processSelectedTrip(tripStatus);
          else{
            if(SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_MANUAL_TRIP_NO_ENTRY, SharedPrefManager.getBoolean(ParamConstants.IS_RFID_STORE, AppCommonMethods.isAllowManualTripCreation))) {
              if (SharedPrefManager.getBoolean(ParamConstants.IS_VERIFY_TRIP_NUM, AppCommonMethods.isVerifyManualTripNoBeforeCreating))
                callCheckManualTripNumberAPI(tripNum);
              else confirmManualTripCreationDialog(tripNum,false);
              /*context.showCustomMsgDialog(String.format(context.getResources().getString(R.string.err__not_exist_manual_input), labelTrip + ":" + tripNum), null, false, false, false, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Bundle arg = chkNull(getArguments(), new Bundle());
                    arg.putString(ParamConstants.TRIP_NUMBER, tripNum);
                    arg.putBoolean(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, true);
                    arg.putBoolean(ParamConstants.IS_VERIFED_TRIP_NUM, false);
                    context.loadFragment(new TripCreationFragment(), arg);
                    clearField();
                }
              }, getString(R.string.btn_cancel));*/
            }
            else
              context.showCustomErrDialog(String.format(context.getResources().getString(R.string.err__not_exist), labelTrip + ":" + tripNum));
          }
        }
      }
    });
    
    setHeader();
    
    binding.list.setAdapter(new TripListAdapter(context, this, dataList));
    binding.list.setLayoutManager(new LinearLayoutManager(context));
    
    binding.ivTripNo.setTextChangeEvent(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        updateLists();
      }
    });
    
    binding.ivTripNo.setIsShowRfidLogo(mainViewModel != null);
    setInputView(binding.ivTripNo,binding.btnGo);

    binding.swipeLayout.setColorSchemeColors(context.getColorPrimaryDarkFromTheme());
    binding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
      @Override
      public void onRefresh(){
        binding.swipeLayout.setRefreshing(false);
        callAPI();
      }
    });
    
    binding.header.imgConfigSync.setVisibility(View.VISIBLE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_sync);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        callAPI();
      }
    });
    
    initUI();
    
    return binding.getRoot();
  }


  void confirmManualTripCreationDialog(final String tripNum,final boolean isVerifiedTripNum){
    context.showCustomMsgDialog(String.format(context.getResources().getString(R.string.err__not_exist_manual_input), labelTrip + ":" + tripNum), null, false, false, false, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Bundle arg = chkNull(getArguments(), new Bundle());
        arg.putString(ParamConstants.TRIP_NUMBER, tripNum);
        arg.putBoolean(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, true);
        arg.putBoolean(ParamConstants.IS_VERIFED_TRIP_NUM, isVerifiedTripNum);
        context.loadFragment(new TripCreationFragment(), arg);
        clearField();
      }
    }, getString(R.string.btn_cancel));
  }
  
  public void processSelectedTrip(final TripStatus tripStatus){
    if(tripStatus==null || isNullOrEmpty(tripStatus.getTripNumber())) return;
    if(!tripStatus.isManualTrip() && tripStatus.getNumberOfHu() <= 0)
      context.showCustomErrDialog(String.format(context.getResources().getString(R.string.err_no_associated__found_for__), labelHU, labelTrip + ":" + tripStatus.getTripNumber()));
    else if(tripStatus.getStatus().equalsIgnoreCase(AppConstants.TRIP_STATUS_COMPLETED))
      context.showCustomErrDialog(String.format(context.getResources().getString(R.string.err__already_completed), labelTrip + ":" + tripStatus.getTripNumber()));
    else if(!tripStatus.isManualTrip() && AppCommonMethods.isNullOrEmpty(tripStatus.getRefTripNumber()))// && itemModel.getRefTripNumber().equalsIgnoreCase(chkNull(itemModel.getTripNumber(), itemModel.getRefTripNumber())))
      showTripNumChangeConfirmAlert(tripStatus);//showTripNumInputAlert(tripStatus);
    else{
      Bundle arg = chkNull(getArguments(), new Bundle());
      arg.putSerializable(tripStatus.getClass().getSimpleName(), (Serializable) tripStatus);
      callTripHuDetailsAPI(tripStatus);
    }
  }
  
  protected boolean isProcessOn(){
    return mainViewModel != null && chkNotNullTrue(mainViewModel.getIsProcessOn().getValue());
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      if(!is1stTime) callAPI();
      updateLists();
    }
  }
  
  private void clearField(){
    new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
      @Override
      public void run(){
        if(binding != null && binding.ivTripNo != null) binding.ivTripNo.setText("");
      }
    }, 30);
  }
  
  public void initUI(){
    labelTrip = SharedPrefManager.getString(ParamConstants.LABEL_TRIP, getString(R.string.lbl_trip));
    labelHU = SharedPrefManager.getString(ParamConstants.LABEL_HU, getString(R.string.lbl_hu));
    binding.ivTripNo.setLabel(String.format(getString(R.string.lbl__no), labelTrip));
    binding.ivTripNo.setHint(String.format(getString(R.string.hint__no), labelTrip));
    
    binding.tripListHeader.textTripNumber.setText(String.format(getString(R.string.lbl__no), labelTrip));
    binding.tripListHeader.textHuCount.setText(String.format(getString(R.string.lbl_total__s), labelHU));
    binding.tripListHeader.txtCompletedHuCount.setText(String.format(getString(R.string.lbl_completed__s), labelHU));
    binding.textNoData.setText(String.format(getString(R.string.err_no__found), labelTrip));
    
    binding.tripListHeader.textHuCount.setVisibility(AppCommonMethods.isShowTotalAndCompletedCount ? View.VISIBLE : View.GONE);
    binding.tripListHeader.imgAction.setVisibility(View.INVISIBLE);
    
    sortByValues = "";
    resetHeader(0);
    updateLists();
  }
  
  /**
   * Set header.
   */
  public void setHeader(){
    final LinearLayoutCompat llHeader = binding.tripListHeader.llHeader;
    final int childCount = llHeader.getChildCount();
    if(childCount > 0) for(int i = 0; i < childCount; i++){
      final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
      if(sortView != null){
        sortView.setOnClickListener(new View.OnClickListener(){
          @Override
          public void onClick(View view){
            if(llHeader.getVisibility() != View.VISIBLE) return;
            final SortHeaderView sortView = view != null && view instanceof SortHeaderView ? (SortHeaderView) view : null;
            if(sortView != null){
              resetHeader(sortView.getId());
              setSortBy(sortView.getSortColumn(), sortView.getSortOrder());
            }
          }
        });
      }
    }
  }
  
  /**
   * Reset header.
   *
   * @param viewId the view id
   */
  public void resetHeader(@IdRes final int viewId){
    final LinearLayoutCompat llHeader = binding.tripListHeader.llHeader;
    final int childCount = llHeader.getChildCount();
    if(childCount > 0) for(int i = 0; i < childCount; i++){
      final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
      if(sortView != null){
        if(viewId != 0 && sortView.getId() == viewId) sortView.updateDescOrder();
        else sortView.reset();
      }
    }
  }
  
  /**
   * Set sort by.
   *
   * @param column  the column
   * @param orderBy the order by
   */
  private void setSortBy(String column, String orderBy){
    sortByValues = isNonEmpty(column) && isNonEmpty(orderBy) ? column + " " + orderBy : "";
    updateLists();
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
      callWebService(URLConstants.GET_TRIPS_DATA, jsonRequest, getString(R.string.progress_msg_getting_data), false, true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Call verify api.
   */
  private void callVerifyAPI(final String tripNum){ callVerifyAPI(tripNum, null); }
  
  private void callVerifyAPI(final String tripNum, final TripStatus tripStatus){
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
      jsonRequest.put(ParamConstants.TRIP_NUMBER, tripStatus != null ? tripStatus.getTripNumber() : tripNum);
      if(tripStatus != null)
        jsonRequest.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripNum);//tripStatus != null ? tripStatus.getRefTripNumber() : "");
      jsonRequest.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus == null);
      jsonRequest.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus != null ? tripStatus.excelTripType : "");
      Bundle arg = chkNull(getArguments(), new Bundle());
      arg.putString(ParamConstants.TRIP_NUMBER, tripNum);
      arg.putBoolean(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus == null);
      if(tripStatus != null)
        arg.putSerializable(tripStatus.getClass().getSimpleName(), (Serializable) tripStatus);
      callWebService(tripStatus == null ? URLConstants.VERIFY_MANUAL_TRIP_NUM : URLConstants.UPDATE_REFERENCE_TRIP_NUM, jsonRequest, arg, getString(R.string.progress_msg_verifying_data));
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  private void callCheckManualTripNumberAPI(final String tripNum){
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
      jsonRequest.put(ParamConstants.TRIP_NUMBER, tripNum);
      jsonRequest.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, true);
      jsonRequest.put(ParamConstants.EXCEL_TRIP_TYPE, "");
     
      Bundle arg = chkNull(getArguments(), new Bundle());
      arg.putString(ParamConstants.TRIP_NUMBER, tripNum);
      arg.putBoolean(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, true);
      arg.putBoolean(ParamConstants.IS_VERIFED_TRIP_NUM, true);
      callWebService(URLConstants.CHECK_TRIP_NUM, jsonRequest, arg, getString(R.string.progress_msg_check_data));
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  public void callTripHuDetailsAPI(final TripStatus tripStatus){
    if(tripStatus == null) return;
    try{
      Bundle arg = chkNull(getArguments(), new Bundle());
      arg.putString(ParamConstants.TYPE, typeIO);
      arg.putSerializable(tripStatus.getClass().getSimpleName(), (Serializable) tripStatus);
      if(tripStatus.isManualTrip() || SharedPrefManager.getBoolean(ParamConstants.IS_ON_DEMAND_TRIP_HU_LIST, AppCommonMethods.isOnDemandTripHuList)){
        if(SharedPrefManager.getBoolean(ParamConstants.IS_USE_TRIP_AS_HU,AppCommonMethods.isUseTripAsHU)){
          arg.putString(ParamConstants.K_TRIP_HU_NUMBER, tripStatus.getTripNumber());
          if(tripStatus.isManualTrip()){
            HUStatus huStatus = huStatusDao.getHUData(typeIO,chkNull(tripStatus.getRefTripNumber(),tripStatus.getTripNumber()),tripStatus.getTripNumber());
            if(huStatus!=null){
              if(tripStatus.getNumberOfHu()==0){
                tripStatusDao.updateTripHUCount(chkNull(tripStatus.getRefTripNumber(),tripStatus.getTripNumber()),typeIO);
                tripStatus.numberOfHu+=1;
              }
              context.loadFragment(new HuProcessStartFragment(),arg);
              return;
            }
            else context.loadFragment(new HuCreationFragment(), arg);
          }
          /*if(tripStatus.isManualTrip() && tripStatus.getNumberOfHu()==0){
            huStatusDao.deleteAllTripHuData(typeIO,chkNull(tripStatus.getRefTripNumber(),tripStatus.getTripNumber()),tripStatus.getTripNumber());
            huDetailsDao.deleteAllNonCompletedTripHus(typeIO,chkNull(tripStatus.getRefTripNumber(),tripStatus.getTripNumber()));
          }*/
          //if(tripStatus.isManualTrip() && tripStatus.getNumberOfHu()==0) context.loadFragment(new HuCreationFragment(), arg);
          else {
            callHUDetails(tripStatus.tripNumber,tripStatus,arg,false);
            return;
          }
        }
        else context.loadFragment(new TripHUListFragment(), arg);
        clearField();
      }
      else{
        if(SharedPrefManager.getBoolean(ParamConstants.IS_USE_TRIP_AS_HU,AppCommonMethods.isUseTripAsHU)){
          arg.putString(ParamConstants.K_TRIP_HU_NUMBER, tripStatus.getTripNumber());
          callHUDetails(tripStatus.tripNumber,tripStatus,arg,false);
          return;
        }
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
        jsonRequest.put(ParamConstants.TRIP_NUMBER, tripStatus.getTripNumber());
        //jsonRequest.put(ParamConstants.REF_TRIP_NUMBER, tripStatus.getRefTripNumber());
        jsonRequest.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus.getRefTripNumber());
        jsonRequest.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus.isManualTrip());
        jsonRequest.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus.excelTripType);
        callWebService(URLConstants.GET_HU_DATA, jsonRequest, arg, getString(R.string.progress_msg_getting_data));
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  private void callHUDetails(final String huNumber, final TripStatus tripStatus, final Bundle args, final boolean isShowDialog) {
    if (tripStatus == null) return;
    if (tripStatus != null && tripStatus.isManualTrip()) return;
    if (isNullOrEmpty(huNumber)) return;
    try {
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
      jsonRequest.put(ParamConstants.TRIP_NUMBER, tripStatus.getTripNumber());
      jsonRequest.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus.getRefTripNumber());
      jsonRequest.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus != null && tripStatus.isManualTrip());
      jsonRequest.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus != null ? tripStatus.excelTripType : "");
      jsonRequest.put(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
      
      Bundle arg = chkNull(args, chkNull(getArguments(), new Bundle()));
      arg.putBoolean(ParamConstants.IS_SHOW_HU_INFO_DIALOG, isShowDialog);
      arg.putString(ParamConstants.TRIP_NUMBER, tripStatus.getTripNumber());
      arg.putString(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus.getRefTripNumber());
      if (tripStatus != null) arg.putSerializable(tripStatus.getClass().getSimpleName(), tripStatus);
      if (isNonEmpty(huNumber)) arg.putString(ParamConstants.K_TRIP_HU_NUMBER, huNumber);
      callWebService(URLConstants.GET_HU_DETAILS, jsonRequest, arg, getString(R.string.progress_msg_getting_data));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    final String searchName = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_LOWER_CASE_TRIP_NUMBER,isAllowLowerCaseTripNumber)?binding.ivTripNo.getText().toString().trim():binding.ivTripNo.getText().toUpperCase().toString().trim();
    dataList.clear();
    showLog("sortByValues", isNonEmpty(sortByValues) ? sortByValues : "--");
    dataList.addAll(tripStatusDao.getTripList(typeIO, searchName, sortByValues));
    final boolean hasData = dataList.size() > 0;
    if(binding != null){
      binding.tripListHeader.llHeader.setVisibility(hasData ? View.VISIBLE : View.GONE);
      binding.list.setVisibility(hasData ? View.VISIBLE : View.GONE);
      binding.textNoData.setVisibility(hasData ? View.GONE : View.VISIBLE);
    }
    if(binding != null && binding.list != null && binding.list.getAdapter() != null)
      ((RecyclerView.Adapter) binding.list.getAdapter()).notifyDataSetChanged();
  }
  
  public void showTripNumChangeConfirmAlert(final TripStatus tripStatus){
    context.showCustomAlertDialog(String.format(context.getString(R.string.title_change__no), labelTrip),String.format(getString(R.string.msg_change_autogenerated__),labelTrip,tripStatus.getTripNumber()),getString(R.string.btn_yes),new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        showTripNumInputAlert(tripStatus);
      }
    },getString(R.string.btn_no),new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        callVerifyAPI(tripStatus.getTripNumber(), tripStatus);
      }
    });
  }
  
  public void showTripNumInputAlert(final TripStatus tripStatus){
    InputView inputView = new InputView(context);
    inputView.setHint(String.format(getString(R.string.hint__no), labelTrip));
    inputView.setLabel(String.format(getString(R.string.lbl__no), labelTrip));
    inputView.setMinLen(1);
    inputView.setMaxLen(20);
    inputView.setValidationRegex("[0-9A-Za-z]{1,}"); // Trip No & Ref Trip No Can be same
    //inputView.setValidationRegex("(?!" + tripStatus.getTripNumber() + ")[0-9A-Za-z]{1,}"); // Trip No & Ref Trip No Can not be same
    //inputView.setValidationRegex("^(?!"+tripStatus.getRefTripNumber()+")[0-9A-Za-z]{1,}$");
    inputView.setInputRegex("[0-9A-Za-z]{0,}");
    //inputView.setText(tripStatus.getTripNumber()); //Set current trip number as text by default for Setting new Reference Trip Number
    inputView.setButtonClick(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(inputView.validate()){
          final String tripNum = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_LOWER_CASE_TRIP_NUMBER,isAllowLowerCaseTripNumber)?binding.ivTripNo.getText().toUpperCase().toString().trim():inputView.getText().toString().toUpperCase().trim();
          /*if(tripStatus.getTripNumber().equalsIgnoreCase(tripNum) || tripStatus.getRefTripNumber().equalsIgnoreCase(tripNum)){
            //inputView.updateError(String.format(context.getString(R.string.field_err_invalid), inputView.getErrLbl()));
            inputView.updateError(String.format(context.getString(R.string.field_err_invalid__can_not_be_same_as__), inputView.getErrLbl(), tripStatus.getRefTripNumber()));
          }
          else*/
          callVerifyAPI(tripNum, tripStatus);
        }
      }
    });
    context.showCustomAlertDialog(String.format(context.getString(R.string.title_set__no), labelTrip), "", inputView, context.getString(R.string.btn_save), context.getString(R.string.btn_cancel));
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_INWARD_TRIP_DATA:
        case URLConstants.GET_TRIPS_DATA:
          if(isSuccess && jsonResponse != null){
            final JSONObject config = extractJSONObject(jsonResponse, ParamConstants.CONFIG, jsonResponse);
            //context.saveIOConfig(config);
            final JSONArray tripDataArray = extractJSONArray(jsonResponse, ParamConstants.TRIPS_DATA, extractJSONArray(jsonResponse, ParamConstants.DATA));
            showLog("noOfTrips1", "" + tripDataArray.length());
            if(isNonEmpty(tripDataArray))
              new InsertDBTrips(context, this, url, typeIO, jsonResponse, args).execute(tripDataArray);
            else{
              tripStatusDao.deleteAllTripStatus(typeIO);
              AppDatabase.getTripInventoryDao(context).deleteAllTripInventory();
              updateLists();
              hideProgressDialog();
            }
          }
          break;
        case URLConstants.CHECK_TRIP_NUM:
          if(isSuccess){
            final String tripNum = extractString(args, ParamConstants.TRIP_NUMBER, "");
            confirmManualTripCreationDialog(tripNum,true);
            //context.loadFragment(new TripCreationFragment(), args);
            //clearField();
          }
          break;
        case URLConstants.VERIFY_MANUAL_TRIP_NUM:
        case URLConstants.UPDATE_REFERENCE_TRIP_NUM:
          if(isSuccess){
            final String tripNum = extractString(jsonRequest, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(args, ParamConstants.REFERENCE_TRIP_NUMBER, ""));
            if(isNonEmpty(tripNum)){
              final boolean isManualTripEntry = extractBoolean(jsonRequest, ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, extractBoolean(args, ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, false));
              final TripStatus tripStatus = isManualTripEntry ? null : (TripStatus) extractSerializable(args, TripStatus.class);
              final Bundle arg = args;
              context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.msg__verified), labelTrip)), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                  if(isManualTripEntry){
                    arg.putBoolean(ParamConstants.IS_VERIFED_TRIP_NUM, true);
                    context.loadFragment(new TripCreationFragment(), arg);
                    clearField();
                  }
                  //else if(tripStatus != null && isNonEmpty(tripStatus.getRefTripNumber()) && chkNull(tripStatus.getTripNumber(), tripStatus.getRefTripNumber()).equalsIgnoreCase(tripStatus.getRefTripNumber())){
                  else if(tripStatus != null && isNullOrEmpty(tripStatus.getRefTripNumber())){
                    //tripStatus.setTripNumber(tripNum);
                    tripStatus.setRefTripNumber(tripNum);
                    tripStatusDao.updateTripStatusData(tripStatus);
                    updateLists();
                    //callTripHuDetailsAPI(tripStatus);
                    //context.loadFragment(new TripHUListFragment(), args);
                  }
                  else if(tripStatus != null && isNonEmpty(tripStatus.getTripNumber()))
                    callTripHuDetailsAPI(tripStatus);//context.loadFragment(new TripHUListFragment(), args);
                }
              });
            }
          }
          break;
        case URLConstants.GET_HU_DATA:
          if(isSuccess && jsonResponse != null){
            final JSONObject config = extractJSONObject(jsonResponse, ParamConstants.CONFIG, jsonResponse);
            final String tripNum = extractString(config, ParamConstants.TRIP_NUMBER, extractString(jsonResponse, ParamConstants.TRIP_NUMBER, extractString(jsonRequest, ParamConstants.TRIP_NUMBER, extractString(args, ParamConstants.TRIP_NUMBER, ""))));
            final String refTripNum = extractString(config, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(jsonResponse, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(jsonRequest, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(args, ParamConstants.REFERENCE_TRIP_NUMBER, ""))));
            context.saveIOConfig(config, tripNum);
            final JSONArray huDataArray = extractJSONArray(jsonResponse, ParamConstants.HU_DATA, extractJSONArray(jsonResponse, ParamConstants.HUS, extractJSONArray(jsonResponse, ParamConstants.DATA)));
            if(isNonEmpty(huDataArray))
              new InsertDBHUs(context, this, url, typeIO, chkNull(refTripNum, tripNum), jsonResponse, args).execute(huDataArray);
            else hideProgressDialog();
            clearField();
          }
          break;
        case URLConstants.GET_HU_DETAILS:
          if (isSuccess && jsonResponse != null) {
            final TripStatus tripStatus = (TripStatus) extractSerializable(args, TripStatus.class);
            final String displayTripNo = chkNull(tripStatus.getRefTripNumber(), tripStatus.getTripNumber());
            final JSONObject config = extractJSONObject(jsonResponse, ParamConstants.CONFIG, jsonResponse);
            final String tripNum = extractString(config, ParamConstants.TRIP_NUMBER, extractString(jsonResponse, ParamConstants.TRIP_NUMBER, extractString(jsonRequest, ParamConstants.TRIP_NUMBER, extractString(args, ParamConstants.TRIP_NUMBER, ""))));
            final String tripRefNum = extractString(config, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(jsonResponse, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(jsonRequest, ParamConstants.REFERENCE_TRIP_NUMBER, extractString(args, ParamConstants.REFERENCE_TRIP_NUMBER, ""))));
            final String useTripNum = !tripRefNum.equalsIgnoreCase(displayTripNo) && tripNum.equalsIgnoreCase(tripNum) ? displayTripNo : chkNull(tripRefNum, tripNum);
            final String huNum = extractString(config, ParamConstants.K_TRIP_HU_NUMBER, extractString(jsonResponse, ParamConstants.K_TRIP_HU_NUMBER, extractString(jsonRequest, ParamConstants.K_TRIP_HU_NUMBER, extractString(args, ParamConstants.K_TRIP_HU_NUMBER, ""))));
            final String status = extractString(config, ParamConstants.K_TRIP_HU_STATUS, extractString(jsonResponse, ParamConstants.STATUS, AppConstants.HU_STATUS_PENDING));
            final boolean isShowDialog = extractBoolean(args, ParamConstants.IS_SHOW_HU_INFO_DIALOG, false);
            if (!isShowDialog && (status.equalsIgnoreCase(AppConstants.STATUS_COMPLETE) || status.equalsIgnoreCase(AppConstants.STATUS_COMPLETED) || status.equalsIgnoreCase(AppConstants.HU_STATUS_COMPLETE))) {
              context.showCustomErrDialog(String.format(context.getResources().getString(R.string.err__already_completed), labelHU + ":" + huNum));
              return;
            }
            context.saveIOConfig(config, useTripNum, huNum);
            final Boolean isOnDemandTripHuList = !tripStatus.isManualTrip() && SharedPrefManager.getBoolean(ParamConstants.IS_ON_DEMAND_TRIP_HU_LIST, AppCommonMethods.isOnDemandTripHuList);
            final JSONArray huDetailsArray = extractJSONArray(jsonResponse, ParamConstants.HU_DATA, extractJSONArray(jsonResponse, ParamConstants.K_TRIP_HU_DETAILS, extractJSONArray(jsonResponse, ParamConstants.DATA)));
            if (isNonEmpty(huDetailsArray)) {
              if (isOnDemandTripHuList) {
                HUStatus huStat = huStatusDao.getHUData(typeIO, displayTripNo, huNum);
                if (huStat == null) {
                  HUStatus huStatus = new HUStatus();
                  huStatus.setType(typeIO);
                  huStatus.setTripNumber(useTripNum);
                  huStatus.setHuNumber(huNum);
                  huStatus.setStatus(status);
                  huStatus.setExpQty(0);
                  huStatus.setScanQty(0);
                  huStatus.setReason("");
                  huStatus.setUploaded(false);
                  huStatus.setManualHU(false);
                  huStatusDao.insertHUStatusData(huStatus);
                  huStat = huStatus;
                }
                else if (huStat != null && !huStat.status.equalsIgnoreCase(status)) {
                  huStat.status = status;
                  huStatusDao.updateHUStatusData(huStat);
                }
                if (huStat != null) {
                  if (args == null) args = chkNull(getArguments(), new Bundle());
                  if (args.containsKey(huStat.getClass().getSimpleName()))
                    args.putSerializable(huStat.getClass().getSimpleName(), huStat);
                }
              }
              new InsertDBHUDetails(context, this, url, typeIO, useTripNum, huNum, jsonResponse, args).execute(huDetailsArray);
            } else hideProgressDialog();
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}