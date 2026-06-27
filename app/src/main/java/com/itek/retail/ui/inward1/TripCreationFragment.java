package com.itek.retail.ui.inward1;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isAllowLowerCaseTripNumber;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.SiteCodeDao;
import com.itek.retail.database.SiteTypeDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.FragmentTripCreationBinding;
import com.itek.retail.model.SiteType;
import com.itek.retail.model.TripStatus;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * The Trip Creation Fragment.
 */
public class TripCreationFragment extends CommonFragment{
  
  private FragmentTripCreationBinding binding;
  private TripStatusDao tripStatusDao;
  private SiteTypeDao siteTypeDao;
  private SiteCodeDao siteCodeDao;
  private String typeIO = "";
  private String tripNum = "";
  private String labelTrip = "";
  private boolean isVerified=false;
  
  /**
   * Instantiates a new Inward grn hu scan fragment.
   */
  public TripCreationFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    tripStatusDao = AppDatabase.getTripStatusDao(context);
    siteTypeDao = AppDatabase.getSiteTypeDao(context);
    siteCodeDao = AppDatabase.getSiteCodeDao(context);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    binding = FragmentTripCreationBinding.inflate(inflater, container, false);
    if(getArguments() != null){
      typeIO = extractString(getArguments(), ParamConstants.TYPE, extractString(getArguments(), ParamConstants.OPERATION_TYPE, AppConstants.INWARD));
      tripNum = extractString(getArguments(), ParamConstants.TRIP_NUMBER, "");
      isVerified = extractBoolean(getArguments(), ParamConstants.IS_VERIFED_TRIP_NUM, false);
    }
    
    initUI();
    
    if(binding.spinTripSrcLocType.isEnabled() && binding.ivSrcCode.isEnabled()){
      binding.spinTripSrcLocType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
          SiteType srcSiteType = (SiteType) binding.spinTripSrcLocType.getSelectedObject();
          binding.ivSrcCode.setAdapter(siteCodeDao.getSiteCodeNameById(srcSiteType.getSiteTypeId()));
        }
        
        @Override
        public void onNothingSelected(AdapterView<?> parent){
        
        }
      });
    }
    if(binding.spinTripDestLocType.isEnabled() && binding.ivDestCode.isEnabled()){
      binding.spinTripDestLocType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
          SiteType destSiteType = (SiteType) binding.spinTripDestLocType.getSelectedObject();
          binding.ivDestCode.setAdapter(siteCodeDao.getSiteCodeNameById(destSiteType.getSiteTypeId()));
        }
        
        @Override
        public void onNothingSelected(AdapterView<?> parent){
        
        }
      });
    }
    
    binding.btnGo.setOnClickListener(v -> {
      if(v != null && v.getVisibility() == View.VISIBLE && allowBtnClick && binding.ivTripNo.validate() && binding.spinTripSrcLocType.validate() && binding.ivSrcCode.validate() && binding.spinTripDestLocType.validate() && binding.ivDestCode.validate()){
        final boolean isInward = typeIO.equalsIgnoreCase(AppConstants.INWARD);
        final String tripNum = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_LOWER_CASE_TRIP_NUMBER,isAllowLowerCaseTripNumber)?binding.ivTripNo.getText().toString().trim():binding.ivTripNo.getText().toString().toUpperCase().trim();
        final SiteType selSiteType = (SiteType) (isInward ? binding.spinTripSrcLocType : binding.spinTripDestLocType).getSelectedObject();
        final String locType = (isInward ? binding.spinTripSrcLocType : binding.spinTripDestLocType).getSelectedItem();
        final String locCode = (isInward ? binding.ivSrcCode : binding.ivDestCode).getText().toString().toUpperCase().trim();
        if(tripStatusDao.isTripPresent(typeIO, tripNum)){
          //context.showCustomErrDialog(String.format(getResources().getString(R.string.err_duplicate__), labelHU+":"+huNumber, labelTrip+":"+tripNumber));
          context.showCustomErrDialog(String.format(getResources().getString(R.string.err__already_exist__), labelTrip + ":" + tripNum));
          clearBarcode();
        }
        else{
          TripStatus tripStatus = new TripStatus();
          tripStatus.setType(typeIO);
          tripStatus.setTripNumber(tripNum);
          tripStatus.setRefTripNumber(tripNum);
          tripStatus.setNumberOfHu(0);
          tripStatus.setCompletedHu(0);
          if(typeIO.equalsIgnoreCase(AppConstants.INWARD)){
            tripStatus.setDestLocCode(SharedPrefManager.getStoreCode());
            tripStatus.setDestLocType(SharedPrefManager.getStoreType());
            tripStatus.setSrcLocType(locType);
            tripStatus.setSrcLocCode(locCode);
          }
          else if(typeIO.equalsIgnoreCase(AppConstants.OUTWARD)){
            tripStatus.setSrcLocCode(SharedPrefManager.getStoreCode());
            tripStatus.setSrcLocType(SharedPrefManager.getStoreType());
            tripStatus.setDestLocType(locType);
            tripStatus.setDestLocCode(locCode);
          }
          tripStatus.setStatus(AppConstants.HU_STATUS_PENDING);
          tripStatus.setUploaded(false);
          tripStatus.setManualTrip(true);
          if(isNullOrEmpty(tripStatus.getTripType())){
            tripStatus.tripType = "";
            String srcLocType = chkNull(tripStatus.srcLocType, typeIO.equalsIgnoreCase(AppConstants.OUTWARD) ? SharedPrefManager.getStoreType() : "");
            String destLocType = chkNull(tripStatus.destLocType, typeIO.equalsIgnoreCase(AppConstants.INWARD) ? SharedPrefManager.getStoreType() : "");
            tripStatus.setTripType((srcLocType + " To " + destLocType).trim());
          }
          
          //tripStatusDao.insertTripStatusData(tripStatus);
          
          callVerifyAPI(tripStatus, selSiteType);
          /*Bundle args = chkNull(getArguments(), new Bundle());
          args.putSerializable(tripStatus.getClass().getSimpleName(), tripStatus);
          popBackStack();*/
        }
      }
    });
    
    /*if(isNonEmpty(tripNum)){
      binding.ivTripNo.setText(tripNum);
      binding.ivTripNo.setEnabled(false);
    }*/
    
    return binding.getRoot();
  }
  
  private void callVerifyAPI(final TripStatus tripStatus, final SiteType selSiteType){
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.OPERATION_TYPE, typeIO);
      jsonRequest.put(ParamConstants.TRIP_NUMBER, tripNum);
      if(isNonEmpty(tripStatus.getRefTripNumber()))
        jsonRequest.put(ParamConstants.REFERENCE_TRIP_NUMBER, tripStatus.getRefTripNumber());
      jsonRequest.put(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus.isManualTrip());
      jsonRequest.put(ParamConstants.EXCEL_TRIP_TYPE, tripStatus.excelTripType);
      jsonRequest.put(ParamConstants.SOURCE_CODE, tripStatus.getSrcLocCode());
      jsonRequest.put(ParamConstants.DESTINATION_CODE, tripStatus.getDestLocCode());
      jsonRequest.put(ParamConstants.SUPPLY_CHAIN_TYPE_ID, selSiteType.getSiteTypeId());
      jsonRequest.put(ParamConstants.SUPPLY_CHAIN_TYPE_NAME, selSiteType.getSiteTypeName());
      
      Bundle arg = chkNull(getArguments(), new Bundle());
      arg.putString(ParamConstants.TRIP_NUMBER, tripNum);
      arg.putString(ParamConstants.REFERENCE_TRIP_NUMBER, chkNull(tripStatus.getRefTripNumber(),tripNum));
      arg.putBoolean(ParamConstants.IS_MANUAL_TRIP_NO_ENTRY, tripStatus.isManualTrip());
      arg.putString(ParamConstants.EXCEL_TRIP_TYPE, tripStatus.excelTripType);
      if(tripStatus != null)
        arg.putSerializable(tripStatus.getClass().getSimpleName(), (Serializable) tripStatus);
      callWebService(URLConstants.VERIFY_MANUAL_TRIP_NUM, jsonRequest, arg, getString(R.string.progress_msg_verifying_data));
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Clear barcode.
   */
  public void clearBarcode(){
    binding.ivTripNo.setText("");
  }
  
  private void initUI(){
    final boolean isInward = typeIO.equalsIgnoreCase(AppConstants.INWARD);
    labelTrip = SharedPrefManager.getString(ParamConstants.LABEL_TRIP, getString(R.string.lbl_trip));
    
    binding.ivTripNo.setLabel(String.format(getString(R.string.lbl__no), labelTrip));
    binding.ivTripNo.setHint(String.format(getString(R.string.hint__no), labelTrip));
    binding.ivTripNo.setText(tripNum);
    binding.ivTripNo.setEnabled(!isVerified);
    
    //ArrayList<String> listLocTypes = SharedPrefManager.getStringArrayList(ParamConstants.SITE_TYPES, new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.location_types))));
    //SharedPrefManager.getLong(ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_ID) //findById
    SiteType defSiteType = siteTypeDao.getSiteTypeById(SharedPrefManager.getLong(ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_ID));
    List<SiteType> listSiteTypes = siteTypeDao.getAll();
    binding.spinTripSrcLocType.setLabel(getString(R.string.lbl_src_type));
    binding.spinTripSrcLocType.setAdapter(listSiteTypes);
    if(!isInward)
      binding.spinTripSrcLocType.setSelection(defSiteType);//SharedPrefManager.getLong(ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_ID),SharedPrefManager.getString(ParamConstants.SUPPLY_CHAIN_TYPE_NAME));
    binding.spinTripSrcLocType.setEnabled(isInward);
    
    binding.ivSrcCode.setLabel(getString(R.string.lbl_src_loc));
    binding.ivSrcCode.setHint(getString(R.string.hint_src_loc));
    binding.ivSrcCode.setText(isInward ? "" : SharedPrefManager.getStoreCode());
    binding.ivSrcCode.setEnabled(isInward);
    
    binding.spinTripDestLocType.setLabel(getString(R.string.lbl_dest_type));
    binding.spinTripDestLocType.setAdapter(listSiteTypes);
    if(isInward) binding.spinTripDestLocType.setSelection(defSiteType);
    binding.spinTripDestLocType.setEnabled(!isInward);
    
    binding.ivDestCode.setLabel(getString(R.string.lbl_dest_loc));
    binding.ivDestCode.setHint(getString(R.string.hint_dest_loc));
    binding.ivDestCode.setText(!isInward ? "" : SharedPrefManager.getStoreCode());
    binding.ivDestCode.setEnabled(!isInward);
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.VERIFY_MANUAL_TRIP_NUM:
          if(isSuccess){
            Serializable obj1 = args.getSerializable(TripStatus.class.getSimpleName());
            TripStatus tripStatus = (obj1 instanceof TripStatus) ? (TripStatus) args.getSerializable(TripStatus.class.getSimpleName()) : null;
            if(tripStatus != null) tripStatusDao.insertTripStatusData(tripStatus);
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.msg__verified), labelTrip)), new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialog, int which){
                if(SharedPrefManager.getBoolean(ParamConstants.IS_USE_TRIP_AS_HU, AppCommonMethods.isUseTripAsHU)){
                  args.putString(ParamConstants.K_TRIP_HU_NUMBER, tripStatus.getTripNumber());
                  args.putString(AppConstants.HU_NUMBER, tripStatus.getTripNumber());
                  context.loadFragment(new HuCreationFragment(),args);
                }
                else context.loadFragment(new TripHUListFragment(),args);
                /*new Handler().post(new Runnable(){
                  @Override
                  public void run(){*/
                    removeFromBackStack(TripCreationFragment.this);
                  /*}
                });*/
              }
            });
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}