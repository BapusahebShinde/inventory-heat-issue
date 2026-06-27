package com.itek.retail.ui.outward;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isSinglePick;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.ZoneDao;
import com.itek.retail.databinding.FragmentOutwardPickStartBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.TripStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * The Outward start fragment.
 */
public class OutwardPickStartFragment extends RFIDSessionFragment{
  
  ZoneDao zoneDao;
  InventoryDao inventoryDao;
  ProductDao productDao;
  
  private OutwardPickStartViewModel mViewModel;
  private FragmentOutwardPickStartBinding binding;
  private ProductModel model;
  private TripStatus tripStatus;
  private String tripNum = "";
  private String huNum = "";
  private String tripType = "";
  private String actionType = "";
  private String mappedEan;
  private boolean isActionPick = false;
  
  /**
   * Instantiates a new Outward start fragment.
   */
  public OutwardPickStartFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    zoneDao = AppDatabase.getZoneDao(context);
    productDao = AppDatabase.getProductDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
    if(isAllowDirectionalSearch) mainViewModel.getSensorAndStart();
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OutwardPickStartViewModel.class);
    binding = FragmentOutwardPickStartBinding.inflate(inflater, container, false);
    mappedEan = !SharedPrefManager.getIsEANMapped() ? "" : extractString(getArguments(), ParamConstants.MAPPED_EAN, "");
    Object obj = extractSerializable(getArguments(), TripStatus.class);
    tripStatus = obj != null && obj instanceof TripStatus ? (TripStatus) obj : null;
    tripNum = tripStatus != null ? tripStatus.getTripNumber() : extractString(getArguments(), AppConstants.TRIP_NUMBER);
    tripType = tripStatus != null ? tripStatus.getTripType() : extractString(getArguments(), AppConstants.TRIP_TYPE);
    actionType = tripStatus != null ? tripStatus.getType() : extractString(getArguments(), AppConstants.ACTION_TYPE);
    huNum = extractString(getArguments(), AppConstants.HU_NUMBER);
    Object obj1 = extractSerializable(getArguments(), ProductModel.class);
    model = obj1 != null && obj1 instanceof ProductModel ? (ProductModel) obj1 : null;
    
    binding.txtTripNumberScan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_list), tripNum), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtHUNumberScan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_hu), huNum), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    binding.txtOutwardScoreCount.setText("" + 0);
    binding.txtOutwardStartTotal.setText("" + model.getEanQty());
    
    binding.pdvOutwardStart.setProductModel(model);
    
    isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
    binding.clOutwardPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
    binding.clOutwardSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
    
    binding.rbPick.setSelected(true);
    binding.rbSearch.setSelected(true);
    binding.rgPickSearchType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        if(isProcessOn()) mainViewModel.stopInventory();
        final int size = getSize();
        isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
        binding.clOutwardPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.clOutwardSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
        binding.txtPicked.setVisibility(!isActionPick && size > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
        binding.btnUpload.setVisibility(/*isActionPick &&*/ size > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
        binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider /*&& isActionPick*/ && size > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
      }
    });
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        if(binding.pdvOutwardStart!=null)binding.pdvOutwardStart.dismissAlerts();
        if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if(!isActionPick && chkNotNullTrue(mainViewModel.getIsPickOn().getValue())) return;
        if(isActionPick && chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())) return;
        if(!isActionPick && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())){
          context.dismissCustomAlertDialog();
          if(savedInstanceState == null && searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject();
              jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
              jsonExtras.put(ParamConstants.EAN_QTY, model.getEanQty());
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, AppConstants.OUTWARD);
              jsonExtras.put(ParamConstants.K_TRIP_TYPE, tripType);
              jsonExtras.put(ParamConstants.K_TRIP_NUMBER, tripNum);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), model.getEanQty(), getSessionType().name(), tripType, tripNum, jsonExtras);
          }
          if(checkReaderConnected()){
            if(searchLog != null && isNullOrEmpty(searchLog.getSearchDurationTime()))
              searchStartTime = System.currentTimeMillis();
            mainViewModel.performBarcodeBasedSearch(chkNull(mappedEan, model.getSearchEan()), binding.clOutwardSearch.isSingleTagSearch());
          }
        }
        else if(isActionPick && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          context.dismissCustomAlertDialog();
          if(savedInstanceState == null && searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject();
              jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
              jsonExtras.put(ParamConstants.EAN_QTY, model.getEanQty());
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, AppConstants.OUTWARD);
              jsonExtras.put(ParamConstants.K_TRIP_TYPE, tripType);
              jsonExtras.put(ParamConstants.K_TRIP_NUMBER, tripNum);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), model.getEanQty(), getSessionType().name(), tripType, tripNum, jsonExtras);
          }
          int size = getSize();
          if(size >= chkNull(model.eanQty, 0)){
            context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_qty_matched), getTypeCharCode()));
            return;
          }
          if(checkReaderConnected()){
            if(size > 0 && !isSinglePick){
              context.showCustomAlertDialog(getString(R.string.title_confirm_action), String.format(getString(R.string.msg_clear_and_start), "" + size), getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  inventoryDao.deleteInventory(sessionObject.sessionId);
                  mainViewModel.performPick(chkNull(mappedEan, model.getEan()));
                }
              }, getString(R.string.btn_no), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  mainViewModel.performPick(chkNull(mappedEan, model.getEan()));
                }
              });
            }
            else{
              mainViewModel.performPick(chkNull(mappedEan, model.getEan()));
            }
          }
        }
        else if(chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()) || chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          mainViewModel.stopInventory();
        }
      }
    });
    
    binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(view.getVisibility() != View.VISIBLE) return;
        if(binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
          return;
        context.showCustomConfirmDialog(R.string.title_stock_correction_upload, R.string.btn_upload, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            apiCall(AppConstants.SESSION_ACTION_UPLOAD);
          }
        });
      }
    });
    
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          binding.btnSwipeUpload.reset();
          context.showCustomConfirmDialog(R.string.title_stock_correction_upload, R.string.btn_upload, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              apiCall(AppConstants.SESSION_ACTION_UPLOAD);
            }
          });
        }
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);
    
    setDefaultSearchViews();
    setSessionAction(AppConstants.SESSION_ACTION_START);
    return binding.getRoot();
  }
  
  @Override
  public int getSize(){
    int size = super.getSize();
    return sessionObject != null && model != null && inventoryDao != null ? inventoryDao.getEANQty(sessionObject.sessionId, chkNull(mappedEan, model.getEan())) : size;
  }
  
  public void apiCall(String action){
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    if(isInternetConnected(context, false, isUpload)){
      try{
        if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        allowBtnClick = false;
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.K_TRIP_NUMBER, tripNum);
        requestParams.put(ParamConstants.K_TRIP_TYPE, tripType);
        requestParams.put(ParamConstants.K_TRIP_HU_NUMBER, huNum);
        requestParams.put(ParamConstants.TYPE, chkNull(actionType, AppConstants.OUTWARD));
        requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        requestParams.put(ParamConstants.CATEGORY, model.getCategory());
        requestParams.put(ParamConstants.BRAND, model.getBrand());
        requestParams.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
        requestParams.put(ParamConstants.EAN_QTY, model.getEanQty());
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        if(sessionObject != null)
          requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
        if(isUpload){
          if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          new Handler().post(new Runnable(){
            @Override
            public void run(){
              try{
                JSONArray js = new JSONArray();
                if(model != null){
                  for(Inventory inventory : inventoryDao.getAllInventoryData(sessionObject.sessionId)){
                    if(inventory != null){
                      JSONObject dataobject = inventory.toJson(context);
                      if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                        js.put(dataobject);
                    }
                  }
                }
                requestParams.put(ParamConstants.ITEMS, js);
                allowBtnClick = true;
                callWebService(URLConstants.UPLOAD_OUTWARD_PICK, requestParams, isUpload, getString(R.string.progress_msg_uploading_data), !isUpload);
              }
              catch(Exception e){
                e.printStackTrace();
                hideProgressDialog();
                allowBtnClick = true;
              }
            }
          });
        }
        else{
          allowBtnClick = true;
          callWebService(isUpload ? URLConstants.UPLOAD_OUTWARD_PICK : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload);
        }
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
        allowBtnClick = true;
      }
    }
  }
  
  /**
   * Set default search views.
   */
  @Override
  protected void setDefaultSearchViews(){
    super.setDefaultSearchViews();
    binding.clOutwardSearch.resetToDefault();
  }
  
  @Override
  public void setSessionObject(RFIDSession sessionObject){
    super.setSessionObject(sessionObject);
    if(sessionObject != null && sessionObject instanceof RFIDSession)
      this.sessionObject = (RFIDSession) sessionObject;
    showLog("outwardSession", "" + (this.sessionObject != null));
  }
  
  @Override
  protected void onReaderConfigured(){
    super.onReaderConfigured();
  }
  
  @Override
  protected void isSessionOnChanged(final Boolean isInventorySessionOn){
    super.isSessionOnChanged(isInventorySessionOn);
    if(isInventorySessionOn == null) return;
    boolean isInvSessionOn = chkNotNullTrue(isInventorySessionOn);
  }
  
  @Override
  protected void isInventoryOnChanged(Boolean isInventoryOn){
    super.isInventoryOnChanged(isInventoryOn);
    if(isInventoryOn == null) return;
    else{/*code here*/}
  }
  
  @Override
  protected void isPickOnChanged(Boolean isPickOn){
    super.isPickOnChanged(isPickOn);
    
    if(isPickOn == null) return;
    else{
      final int size = getSize();
      binding.pdvOutwardStart.setEnabled(!isPickOn && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()));
      binding.rgPickSearchType.setEnabled(!isPickOn && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()));binding.rgPickSearchType.setVisibility(!isPickOn && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())?View.VISIBLE:View.GONE);
      binding.rbPick.setEnabled(!isPickOn && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()));
      binding.rbSearch.setEnabled(!isPickOn && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()));
      binding.txtPicked.setVisibility(!isActionPick && size > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
      binding.btnUpload.setVisibility(/*isActionPick &&*/ size > 0 && !isPickOn && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) ? View.VISIBLE : View.GONE);
      binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && /*isActionPick &&*/ size > 0 && !isPickOn && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) ? View.VISIBLE : View.GONE);
      if(!isPickOn) stopTimer();
      binding.llBtnStart.toggle(isPickOn);
    }
  }
  
  @Override
  protected void isSearchOnChanged(Boolean isSearchOn){
    super.isSearchOnChanged(isSearchOn);
    
    if(isSearchOn == null) return;
    else{
      final int size = getSize();
      binding.clOutwardSearch.setEnableCheck(!isSearchOn);
      binding.pdvOutwardStart.setEnabled(!isSearchOn && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()));
      binding.rgPickSearchType.setEnabled(!isSearchOn && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()));binding.rgPickSearchType.setVisibility(!isSearchOn && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())?View.VISIBLE:View.GONE);
      binding.rbPick.setEnabled(!isSearchOn && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()));
      binding.rbSearch.setEnabled(!isSearchOn && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()));
      binding.txtPicked.setVisibility(!isActionPick && size > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
      binding.btnUpload.setVisibility(/*isActionPick &&*/ size > 0 && !isSearchOn && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
      binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider /*&& isActionPick*/ && size > 0 && !isSearchOn && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
      if(!isSearchOn) stopTimer();
      else startTimer(binding.clOutwardSearch, binding.imgSearchDir);
      binding.llBtnStart.toggle(isSearchOn);
    }
  }
  
  @Override
  protected void onTriggerPressed(){
    showLog(this.getClass().getSimpleName(), "onTriggerPressed");
    binding.llBtnStart.performClick();
  }
  
  @Override
  protected void onDataSizeChanged(Integer size1){
    final int size = getSize();
    binding.txtOutwardScoreCount.setText("" + chkNull(size, 0));
    binding.txtOutwardStartTotal.setText("" + model.getEanQty());
    long total = Long.parseLong(chkNull(binding.txtOutwardStartTotal.getText().toString().replace("-", ""), "0"));
    final boolean isInvCount = chkNull(size, 0) > 0;
    binding.progressOutwardStart.setVisibility(isInvCount && total > 0 ? View.VISIBLE : View.GONE);
    binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_picked_qty_total), String.valueOf(chkNull(size, 0)), model.getEanQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtPicked.setVisibility(!isActionPick && size > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
    binding.btnUpload.setVisibility(/*isActionPick &&*/ size > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
    binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider /*&& isActionPick*/ && size > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
    if(size > model.getEanQty()){
      context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_extra_tag), getTypeCharCode(), (chkNull(size, 0) + "/" + model.getEanQtyStr())));
      mainViewModel.stopInventory();
      AppDatabase.getInventoryDao(context).deleteInventory(sessionObject.sessionId);
    }
    //insertSearchLog(model.getEan(),model.getEanQty(), getSessionType().name(), tripType, tripNum);
    double per = total > 0 ? (chkNull(size, 0) * 100) / total : 0;
    int percentage = (int) per;
    binding.progressOutwardStart.setProgress(percentage);
  }
  
  @Override
  public void onPause(){
    super.onPause();
  }
  
  @Override
  public void onResume(){
    super.onResume();
  }
  
  @Override
  public void onDestroyView(){
    sessionObject = null;
    super.onDestroyView();
  }
  
  /**
   * Set session action.
   *
   * @param action the action
   */
  public void setSessionAction(String action){
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.eans = model.getEan();
      sessionObject.brands = model.getBrand();
      sessionObject.category = model.getCategory();
      sessionObject.zone = model.getZone();
      sessionObject.total = String.valueOf(model.getEanQty());
      sessionObject.sessionType = AppCommonMethods.SessionType.OUTWARD_PICK.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.SEARCH.getValue();
      sessionObject.userId = SharedPrefManager.getUserID();
      Calendar cc = Calendar.getInstance();
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, 24);
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc);
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, false);
      apiCall(action);
    }
    else if(sessionObject != null && !action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      //TODO
    }
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.SET_SESSION:
          break;
        case URLConstants.UPLOAD_OUTWARD_PICK:
          if(isSuccess){
            //delete Uploaded Records
            mainViewModel.stopSession(sessionObject, false);
            inventoryDao.updateUploaded(sessionObject.sessionId);
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), true);
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}