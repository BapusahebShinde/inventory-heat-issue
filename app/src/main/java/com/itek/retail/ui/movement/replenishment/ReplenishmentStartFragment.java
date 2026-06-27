package com.itek.retail.ui.movement.replenishment;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
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
import com.itek.retail.databinding.FragmentReplenishmentStartBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.Zone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * The Replenishment start fragment.
 */
public class ReplenishmentStartFragment extends RFIDSessionFragment{
  
  ZoneDao zoneDao;
  ProductDao productDao;
  InventoryDao inventoryDao;
  private ReplenishmentStartViewModel mViewModel;
  private FragmentReplenishmentStartBinding binding;
  private String mappedEan;
  private ProductModel model;
  private Zone srcZone, destZone;
  private String replenishmentType = "";
  private boolean isActionPick = false;
  /**
   * Start timer.
   */
  
  /**
   * Instantiates a new Replenishment start fragment.
   */
  public ReplenishmentStartFragment(){/*Default/Empty Constructor*/}
  
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
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ReplenishmentStartViewModel.class);
    binding = FragmentReplenishmentStartBinding.inflate(inflater, container, false);
    mappedEan = !SharedPrefManager.getIsEANMapped() ? "" : extractString(getArguments(), ParamConstants.MAPPED_EAN, "");
    final Object obj = extractSerializable(getArguments(), ProductModel.class);
    model = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
    final String srcZoneName = extractString(getArguments(), AppConstants.SRC_ZONE, AppConstants.ALL);
    final String destZoneName = chkNull(model.destZone,extractString(getArguments(), AppConstants.DEST_ZONE, AppConstants.ALL));
    List<Zone> srcZones =zoneDao.getZoneByName(srcZoneName);
    if(isNullOrEmpty(srcZones) && isNonEmpty(model.getZone())) srcZones = zoneDao.getZoneByName(model.getZone());
    List<Zone> destZones =zoneDao.getZoneByName(destZoneName);
    if(isNullOrEmpty(destZones) && isNonEmpty(model.getDestZone())) destZones = zoneDao.getZoneByName(model.getZone());
    srcZone = isNonEmpty(srcZones)?srcZones.get(0):null;
    destZone = isNonEmpty(destZones)?destZones.get(0):null;
    
    replenishmentType = extractString(getArguments(), AppConstants.REPLENISHMENT_TYPE, AppConstants.REPLENISH_TYPE_DYNAMIC);
   // binding.txtReplenishmentScoreCount.setText("" + 0);
    binding.ctwInventoryStart.setScore(0);
   // binding.txtReplenishmentStartTotal.setText("" + chkZero(model.getQty(),model.getEanQty()));
    binding.ctwInventoryStart.setTotal("" + chkZero(model.getQty(),model.getEanQty()));

    binding.pdvReplenishStart.setProductModel(model, chkNull(replenishmentType, ""));
    
    isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
   // binding.clReplenishmentPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
    binding.ctwInventoryStart.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
    binding.clReplenishmentSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
    
    binding.rbPick.setSelected(true);
    binding.rbSearch.setSelected(true);
    binding.rgPickSearchType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        if(isProcessOn()) mainViewModel.stopInventory();
        isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
       // binding.clReplenishmentPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.ctwInventoryStart.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.clReplenishmentSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
        binding.txtPicked.setVisibility(!isActionPick && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
        binding.btnUpload.setVisibility(/*isActionPick &&*/ getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
        //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider /*&& isActionPick*/ && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
      }
    });
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        if(binding.pdvReplenishStart!=null)binding.pdvReplenishStart.dismissAlerts();
        if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if(!isActionPick && chkNotNullTrue(mainViewModel.getIsPickOn().getValue())) return;
        if(isActionPick && chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())) return;
        if(!isActionPick && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())){
          context.dismissCustomAlertDialog();
          if(savedInstanceState == null && searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject();
              jsonExtras.put(ParamConstants.ZONE, model.getZone());
              jsonExtras.put(ParamConstants.ZONE_ID, model.getZoneId(context));
              jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
              jsonExtras.put(ParamConstants.EAN_QTY, chkZero(model.getQty(),model.getEanQty()));
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, AppConstants.REPLENISHMENT_STOCK);
              jsonExtras.put(ParamConstants.REPLENISHMENT_TYPE, replenishmentType);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), /*isStaticReplenishment ? 1 :*/ chkZero(model.getQty(),model.getEanQty()), getSessionType().name(), replenishmentType, jsonExtras);
          }
          if(checkReaderConnected()){
            if(searchLog != null && isNullOrEmpty(searchLog.getSearchDurationTime()))
              searchStartTime = System.currentTimeMillis();
            mainViewModel.performBarcodeBasedSearch(chkNull(mappedEan, model.getSearchEan()), binding.clReplenishmentSearch.isSingleTagSearch());
          }
        }
        else if(isActionPick && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          context.dismissCustomAlertDialog();
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
              if(savedInstanceState == null && searchLog == null){
                JSONObject jsonExtras = null;
                try{
                  jsonExtras = new JSONObject();
                  jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
                  jsonExtras.put(ParamConstants.EAN_QTY,chkZero(model.getQty(),model.getEanQty()));
                  jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
                  jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
                  jsonExtras.put(ParamConstants.TYPE, AppConstants.REPLENISHMENT_STOCK);
                  jsonExtras.put(ParamConstants.REPLENISHMENT_TYPE, replenishmentType);
                }
                catch(Exception e){ e.printStackTrace(); }
                searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), /*isStaticReplenishment ? 1 :*/ chkZero(model.getQty(),model.getEanQty()), getSessionType().name(), replenishmentType, jsonExtras);
              }
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
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);*/
    
    setDefaultSearchViews();
    setSessionAction(AppConstants.SESSION_ACTION_START);
    return binding.getRoot();
  }
  
  public void apiCall(String action){
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    if(isInternetConnected(context, false, isUpload)){
      try{
        if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        allowBtnClick = false;
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.REPLENISHMENT_TYPE, !chkNull(SharedPrefManager.getReplenishmentType(), AppConstants.REPLENISH_TYPE_BOTH).equalsIgnoreCase(AppConstants.REPLENISH_TYPE_BOTH) ? SharedPrefManager.getReplenishmentType() : chkNull(replenishmentType, AppConstants.REPLENISH_TYPE_DYNAMIC));
        requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        requestParams.put(ParamConstants.TYPE, AppConstants.REPLENISHMENT_STOCK);
        if(srcZone!=null)
         requestParams.put(ParamConstants.SOURCE_ZONE,srcZone.toJson());
        if(destZone!=null)
          requestParams.put(ParamConstants.DESTINATION_ZONE, destZone.toJson());
        requestParams.put(ParamConstants.CATEGORY, model.getCategory());
        requestParams.put(ParamConstants.BRAND, model.getBrand());
        
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        if(sessionObject != null)
          requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
        if(isUpload){
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
                callWebService(URLConstants.UPLOAD_MOVEMENT, requestParams, isUpload, getString(R.string.progress_msg_uploading_data), !isUpload);
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
          callWebService(isUpload ? URLConstants.UPLOAD_MOVEMENT : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload);
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
    //binding.clReplenishmentSearch.resetToDefault();
  }
  
  @Override
  public void setSessionObject(RFIDSession sessionObject){
    super.setSessionObject(sessionObject);
    if(sessionObject != null && sessionObject instanceof RFIDSession)
      this.sessionObject = (RFIDSession) sessionObject;
    showLog("movementSession", "" + (this.sessionObject != null));
  }
  
  @Override
  protected void onReaderConfigured(){
    super.onReaderConfigured();
  }
  
  @Override
  protected void isSessionOnChanged(final Boolean isSessionOn){
    super.isSessionOnChanged(isSessionOn);
    if(isSessionOn == null) return;
    
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
      binding.pdvReplenishStart.setEnabled(!isPickOn && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()));
      binding.rgPickSearchType.setEnabled(!isPickOn && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()));
      binding.rgPickSearchType.setVisibility(!isPickOn && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())?View.VISIBLE:View.GONE);
      binding.rbPick.setEnabled(!isPickOn && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()));
      binding.rbSearch.setEnabled(!isPickOn && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()));
      binding.txtPicked.setVisibility(!isActionPick && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
      binding.btnUpload.setVisibility(/*isActionPick &&*/ getSize() > 0 && !isPickOn && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) ? View.VISIBLE : View.GONE);
      //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider /*&& isActionPick*/ && getSize() > 0 && !isPickOn && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) ? View.VISIBLE : View.GONE);
      if(!isPickOn) stopTimer();
      binding.llBtnStart.toggle(isPickOn);
    }
  }
  
  @Override
  protected void isSearchOnChanged(Boolean isSearchOn){
    super.isSearchOnChanged(isSearchOn);
    if(isSearchOn == null) return;
    else{
      binding.clReplenishmentSearch.setEnableCheck(!isSearchOn);
      binding.pdvReplenishStart.setEnabled(!isSearchOn && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()));
      binding.rgPickSearchType.setEnabled(!isSearchOn && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()));
      binding.rgPickSearchType.setVisibility(!isSearchOn && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())?View.VISIBLE:View.GONE);
      binding.rbPick.setEnabled(!isSearchOn && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()));
      binding.rbSearch.setEnabled(!isSearchOn && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue()));
      binding.txtPicked.setVisibility(!isActionPick && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
      binding.btnUpload.setVisibility(/*isActionPick &&*/ getSize() > 0 && !isSearchOn && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
      //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider /*&& isActionPick*/ && getSize() > 0 && !isSearchOn && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
      if(!isSearchOn) stopTimer();
      else startTimer(binding.clReplenishmentSearch, binding.imgSearchDir);
      binding.llBtnStart.toggle(isSearchOn);
    }
  }
  
  @Override
  protected void onTriggerPressed(){
    showLog(this.getClass().getSimpleName(), "onTriggerPressed");
    binding.llBtnStart.performClick();
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
   // binding.txtReplenishmentScoreCount.setText("" + chkNull(size, 0));
    binding.ctwInventoryStart.setScore(chkNull(size,0));
  //  binding.txtReplenishmentStartTotal.setText("" + chkZero(model.getQty(),model.getEanQty()));
    binding.ctwInventoryStart.setTotal("" + chkZero(model.getQty(),model.getEanQty()));
  //  long total = Long.parseLong(chkNull(binding.txtReplenishmentStartTotal.getText().toString().replace("-", ""), "0"));
    long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
    binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_picked_qty_total), String.valueOf(chkNull(size, 0)), chkZero(model.getQtyStr(),model.getEanQtyStr())), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtPicked.setVisibility(!isActionPick && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
    binding.btnUpload.setVisibility(/*isActionPick &&*/ size > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
    if(size > chkZero(model.getQty(),model.getEanQty())){
      context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_extra_tag), getTypeCharCode(), (chkNull(size, 0) + "/" + chkZero(model.getQtyStr(),model.getEanQtyStr()))));
      mainViewModel.stopInventory();
      AppDatabase.getInventoryDao(context).deleteInventory(sessionObject.sessionId);
    }
  }
  
  /**
   * Set session action.
   *
   * @param action the action
   */
  public void setSessionAction(String action){
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.zone = model.getZone();
      sessionObject.zoneId = model.getZoneId();
      sessionObject.destZone = destZone!=null?destZone.getZoneName():AppConstants.ALL;
      sessionObject.destZoneId = destZone!=null?destZone.getZoneId():"0";
      sessionObject.eans = chkNull(mappedEan, model.getEan());
      sessionObject.brands = model.getBrand();
      sessionObject.category = model.getCategory();
      sessionObject.total = String.valueOf(chkZero(model.getQty(),model.getEanQty()));
      sessionObject.sessionType = AppCommonMethods.SessionType.REPLENISHMENT.getValue();
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
        case URLConstants.UPLOAD_MOVEMENT:
          context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), true);
          //delete Uploaded Records
          productDao.deleteAllExcept();
          mainViewModel.stopSession(sessionObject, true);
          inventoryDao.deleteInventory(sessionObject.sessionId);
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}