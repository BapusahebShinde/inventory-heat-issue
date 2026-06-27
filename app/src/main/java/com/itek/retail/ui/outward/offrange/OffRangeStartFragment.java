package com.itek.retail.ui.outward.offrange;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isAllowDiscardOperationForPickedData;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isSinglePick;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.itek.retail.database.OutwardBatchDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.RFIDSessionDao;
import com.itek.retail.databinding.FragmentOffRangeStartBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.OutwardTypes;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.outward.OutwardPickStartViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * The Outward start fragment.
 */
public class OffRangeStartFragment extends RFIDSessionFragment{
  
  RFIDSessionDao rfidSessionDao;
  OutwardBatchDao outwardBatchDao;
  InventoryDao inventoryDao;
  ProductDao productDao;
  int activeUsers = 0, sessionValidTill = 48;
  private OutwardPickStartViewModel mViewModel;
  private FragmentOffRangeStartBinding binding;
  private ProductModel model;
  private boolean isActionPick = false;
  private String type;
  private String destCode;
  private String cartonNo;
  private JSONObject extras;
  private OutwardTypes outType;
  private String batchId;
  private String listRefBatchId;
  private boolean isOffRange = false;
  private boolean isEmptyToteOutward = false;
  //private Set<String> eans = new HashSet<>(0);
  private String sessionId;
  
  /**
   * Instantiates a new Outward start fragment.
   */
  public OffRangeStartFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
    outwardBatchDao = AppDatabase.getOutwardBatchDao(context);
    rfidSessionDao = AppDatabase.getRIFDSessionDao(context);
    /*List<String> listEans = productDao.getDistinctEans(sessionObject != null ? sessionObject.sessionType : getSessionType().getValue());
    if(isNonEmpty(listEans)) eans.addAll(listEans);*/
    if(isAllowDirectionalSearch) mainViewModel.getSensorAndStart();
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OutwardPickStartViewModel.class);
    binding = FragmentOffRangeStartBinding.inflate(inflater, container, false);
    
    if(isNonEmpty(getArguments())){
      isEmptyToteOutward = getArguments().containsKey(ParamConstants.IS_EMPTY_TOTE_OUTWARD) && getArguments().getBoolean(ParamConstants.IS_EMPTY_TOTE_OUTWARD, false);
      isOffRange = getArguments().getBoolean(ParamConstants.IS_OFF_RANGE, true);
      outType = (OutwardTypes) extractSerializable(getArguments(), OutwardTypes.class);
      cartonNo = extractString(getArguments(), ParamConstants.CARTON_NO, extractString(getArguments(), ParamConstants.CARTON_NUM, extractString(getArguments(), ParamConstants.CARTON_NUMBER)));
      destCode = extractString(getArguments(), ParamConstants.DESTINATION_SITE_CODE, extractString(getArguments(), ParamConstants.DESTINATION_SITE, extractString(getArguments(), ParamConstants.DESTINATION_CODE)));
      batchId = extractString(getArguments(), ParamConstants.BATCH_ID, extractString(getArguments(), ParamConstants.BATCH));
      listRefBatchId = extractString(getArguments(), ParamConstants.LIST_REF_BATCH_ID, extractString(getArguments(), ParamConstants.LIST_BATCH_ID, extractString(getArguments(), ParamConstants.LIST_ID)));
      sessionId = extractString(getArguments(), ParamConstants.SESSION_ID, "");
      type = outType != null ? outType.getName() : extractString(getArguments(), ParamConstants.OUTWARD_TOTE_TYPE, extractString(getArguments(), ParamConstants.TYPE));
      try{
        extras = new JSONObject();
        extras.put(ParamConstants.DESTINATION_SITE_CODE, destCode);
        extras.put(ParamConstants.DESTINATION_CODE, destCode);
        extras.put(ParamConstants.CARTON_NUMBER, cartonNo);
        extras.put(ParamConstants.OUTWARD_TOTE_TYPE, type);
        extras.put(ParamConstants.IS_OFF_RANGE, isOffRange);
        extras.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
        extras.put(ParamConstants.BATCH_ID, batchId);
        extras.put(ParamConstants.LIST_REF_BATCH_ID, listRefBatchId);
        if(outType != null)
          extras.put(ParamConstants.STORE_OUTWARD_TYPE_MASTER_ID, outType.getTypeId());
        //extras.put(ParamConstants.OUTWARD_TOTE_EANS,type);
      }
      catch(JSONException e){
        throw new RuntimeException(e);
      }
      if(isNonEmpty(sessionId)){
        sessionObject = rfidSessionDao.getSession(sessionId, getSessionType().getValue());
        setSessionObject(sessionObject);
        rfidSessionDao.updateActiveSession(true, getSessionType().getValue());
      }
    }
    
    activeUsers = extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2);
    sessionValidTill = extractInt(getArguments(), AppConstants.SESSION_VALID_TILL, 48);
    
    Object obj1 = extractSerializable(getArguments(), ProductModel.class);
    model = obj1 != null && obj1 instanceof ProductModel ? (ProductModel) obj1 : null;
    
    //binding.txtTripNumberScan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_list), tripNum), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtHUNumberScan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_carton), cartonNo), HtmlCompat.FROM_HTML_MODE_LEGACY));
    ((View) binding.txtHUNumberScan.getParent()).setVisibility(View.GONE);
    
    // binding.txtOutwardScoreCount.setText("" + 0);
    binding.ctwInventoryStart.setScore(0);
    //  binding.txtOutwardStartTotal.setText("" +chkZero(""+model.getEanQty(),AppConstants.DEFAULT_NO_VALUE));
    binding.ctwInventoryStart.setTotal("" + chkZero("" + model.getEanQty(), AppConstants.DEFAULT_NO_VALUE));
    
    final boolean hasZone = !(model.getDestZone().equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE) || model.getZone().equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE));
    binding.pdvOutwardStart.setProductModel(model, hasZone);
    
    isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
    // binding.clOutwardPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
    binding.ctwInventoryStart.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
    binding.clOutwardSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
    
    binding.rbPick.setSelected(true);
    binding.rbSearch.setSelected(true);
    binding.rgPickSearchType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        if(isProcessOn()) mainViewModel.stopInventory();
        final boolean isProcessOn = isProcessOn();
        final int size = getSize();
        isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
        // binding.clOutwardPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.ctwInventoryStart.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.clOutwardSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
        binding.txtPicked.setVisibility(!isActionPick && size > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
        binding.btnUpload.setVisibility(isAllowDiscardOperationForPickedData && size > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
        //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && isAllowDiscardOperationForPickedData && size > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
      }
    });
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        if(binding.pdvOutwardStart != null) binding.pdvOutwardStart.dismissAlerts();
        if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if(!isActionPick && chkNotNullTrue(mainViewModel.getIsPickOn().getValue())) return;
        if(isActionPick && chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())) return;
        if(!isActionPick && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())){
          context.dismissCustomAlertDialog();
          if(savedInstanceState == null && searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject(extras.toString());
              jsonExtras.put(ParamConstants.EAN, model.getEan());
              jsonExtras.put(ParamConstants.EAN_QTY, model.getEanQty());
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, type);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", model.getEan(), model.getEanQty(), getSessionType().name(), type, batchId, jsonExtras);
          }
          if(checkReaderConnected()){
            if(searchLog != null && isNullOrEmpty(searchLog.getSearchDurationTime()))
              searchStartTime = System.currentTimeMillis();
            mainViewModel.performBarcodeBasedSearch(model.getSearchEan(), binding.clOutwardSearch.isSingleTagSearch());
          }
        }
        else if(isActionPick && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          context.dismissCustomAlertDialog();
          if(savedInstanceState == null && searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject(extras.toString());
              jsonExtras.put(ParamConstants.EAN, model.getEan());
              jsonExtras.put(ParamConstants.EAN_QTY, model.getEanQty());
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, type);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", model.getEan(), model.getEanQty(), getSessionType().name(), type, batchId, jsonExtras);
          }
          int size = getSize();
          //          if(size >= chkNull(model.eanQty, 0)){
          //            context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_qty_matched), getTypeCharCode()));
          //            return;
          //          }
          if(checkReaderConnected()){
            if(size > 0 && !isSinglePick){
              context.showCustomAlertDialog(getString(R.string.title_confirm_action), String.format(getString(R.string.msg_clear_and_start), "" + size), getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  inventoryDao.deleteInventory(sessionObject.sessionId);
                  mainViewModel.performPick(model.getEan());
                }
              }, getString(R.string.btn_no), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  mainViewModel.performPick(model.getEan());
                }
              });
            }
            else{
              mainViewModel.performPick(model.getEan());
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
        context.showCustomConfirmDialog(R.string.title_stock_correction_upload, R.string.btn_upload, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            apiCall(AppConstants.SESSION_ACTION_UPLOAD);
          }
        });
      }
    });
    
    /*binding.btnUpload.setOnClickListener(new View.OnClickListener(){
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
    });*/
    
    //setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);
    
    setDefaultSearchViews();
    
    setSessionAction(AppConstants.SESSION_ACTION_START, sessionObject != null ? sessionObject.sessionId : chkNull(sessionId, ""));
    
    int eanCount = inventoryDao.getEANQty(sessionId, model.getEan());
    int sessionCount = inventoryDao.getTotalCount(sessionId);
    showLog("eanCount11", model.getEan() + "=" + eanCount);
    showLog("sessionCount11", sessionId + "=" + sessionCount);
    
    return binding.getRoot();
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    updateView();
  }
  
  @Override
  public int getSize(){
    int size = super.getSize();
    return sessionObject != null && model != null && inventoryDao != null ? inventoryDao.getEANQty(sessionObject.sessionId, model.getEan()) : size;
  }
  
  public void apiCall(String action){
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    if(isUpload && isInternetConnected(context, false, isUpload)){
      try{
        if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        allowBtnClick = false;
        JSONObject requestParams = extras != null ? extras : new JSONObject();
        requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        requestParams.put(ParamConstants.IS_OFF_RANGE, isOffRange);
        requestParams.put(ParamConstants.TYPE, type);
        requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD);
        requestParams.put(ParamConstants.STATUS, AppConstants.TRIP_STATUS_PENDING);
        if(sessionObject != null)
          requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
        if(isUpload){
          showProgressDialog(getString(R.string.progress_msg_check_upload_data));
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
            callWebService(URLConstants.UPLOAD_OUTWARD_CARTON_DATA, requestParams, isUpload, getString(R.string.progress_msg_uploading_data), !isUpload);
          }
          catch(Exception e){
            e.printStackTrace();
            hideProgressDialog();
            allowBtnClick = true;
          }
        }
        else{
          allowBtnClick = true;
          callWebService(isUpload ? URLConstants.UPLOAD_OUTWARD_CARTON_DATA : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload);
        }
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
        allowBtnClick = true;
      }
    }
    else
      setSessionAction(action, sessionObject != null ? sessionObject.sessionId : chkNull(sessionId, ""));
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
      updateView();
      if(!isPickOn) stopTimer();
      binding.llBtnStart.toggle(isPickOn);
    }
  }
  
  void updateView(){
    final boolean isProcessOn = isProcessOn();
    final int size = getSize();//productDao.getEANFoundCount(model.getEan());
    binding.pdvOutwardStart.setEnabled(!isProcessOn);
    binding.rgPickSearchType.setEnabled(!isProcessOn);
    binding.rgPickSearchType.setVisibility(!isProcessOn ? View.VISIBLE : View.GONE);
    binding.rbPick.setEnabled(!isProcessOn);
    binding.rbSearch.setEnabled(!isProcessOn);
    binding.txtPicked.setVisibility(!isActionPick && size > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
    
    binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_picked_qty_total), String.valueOf(chkNull(size, 0)), model.getEanQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    // binding.txtOutwardScoreCount.setText("" + chkNull(size, 0));
    binding.ctwInventoryStart.setScore(chkNull(size, 0));
    // binding.txtOutwardStartTotal.setText("" + chkZero(""+model.getEanQty(),AppConstants.DEFAULT_NO_VALUE));
    binding.ctwInventoryStart.setTotal(chkZero("" + model.getEanQty(), AppConstants.DEFAULT_NO_VALUE));
    
    binding.btnUpload.setVisibility(isAllowDiscardOperationForPickedData && !isProcessOn && size > 0 ? View.VISIBLE : View.GONE);
    //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && isAllowDiscardOperationForPickedData && !isProcessOn && size > 0 ? View.VISIBLE : View.GONE);
  }
  
  @Override
  protected void isSearchOnChanged(Boolean isSearchOn){
    super.isSearchOnChanged(isSearchOn);
    if(isSearchOn == null) return;
    else{
      updateView();
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
    super.onDataSizeChanged(size1);
    showLog("onDataSizeChanged", size1 + "_" + getSize());
    final int size = getSize();//productDao.getEANFoundCount(model.getEan());
    showLog("Size", size1 + "_" + size);
    // binding.txtOutwardScoreCount.setText("" + chkNull(size, 0));
    binding.ctwInventoryStart.setScore(chkNull(size, 0));
    // binding.txtOutwardStartTotal.setText("" + chkZero(""+model.getEanQty(),AppConstants.DEFAULT_NO_VALUE));
    binding.ctwInventoryStart.setTotal(chkZero("" + model.getEanQty(), AppConstants.DEFAULT_NO_VALUE));
    binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_picked_qty_total), String.valueOf(chkNull(size, 0)), model.getEanQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtPicked.setVisibility(!isActionPick && size > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
    binding.btnUpload.setVisibility(isAllowDiscardOperationForPickedData && size > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
    //insertSearchLog(model.getEan(),model.getEanQty(), getSessionType().name(), tripType, tripNum);
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
    super.onDestroyView();
  }
  
  /**
   * Set session action.
   *
   * @param action the action
   */
  public void setSessionAction(String action, String sessionId){
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.eans = model.getEan();
      sessionObject.brands = model.getBrand();
      sessionObject.category = model.getCategory();
      sessionObject.zone = model.getZone();
      sessionObject.total = String.valueOf(model.getEanQty());
      sessionObject.extras = isNonEmpty(extras) ? extras.toString() : "";
      sessionObject.total = "";//totInvCount;
      sessionObject.sessionType = AppCommonMethods.SessionType.OFF_RANGE.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.SEARCH.getValue();
      sessionObject.userId = SharedPrefManager.getUserID();
      Calendar cc = Calendar.getInstance();
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, 24);
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = chkNull(sessionId, mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc));
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, false);
    }
    else if(sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      mainViewModel.startSession(sessionObject, true);
    }
    else if(sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_RESUME))
      mainViewModel.startSession(sessionObject, true);
    else if(sessionObject != null && !action.matches("(?i)(" + AppConstants.SESSION_ACTION_START + "|" + AppConstants.SESSION_ACTION_RESUME + ")")){
      mainViewModel.stopSession(sessionObject, false);
      if(!action.equalsIgnoreCase(AppConstants.SESSION_ACTION_SAVE)) context.popBackStack();
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