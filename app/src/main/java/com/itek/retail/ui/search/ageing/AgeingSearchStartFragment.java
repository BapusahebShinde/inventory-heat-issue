package com.itek.retail.ui.search.ageing;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isAllowDecodeOnPick;
import static com.itek.retail.common.AppCommonMethods.isAutoBackOnEanZoneDecoded;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isSinglePick;
import static com.itek.retail.common.AppCommonMethods.isUse24LengthTIDForUpload;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.AgeingPickedListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.databinding.DialogAgeingEpcSearchBinding;
import com.itek.retail.databinding.FragmentAgeingSearchStartBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AgeingSearchStartFragment extends RFIDSessionFragment{
  
  ProductDao productDao;
  InventoryDao inventoryDao;
  UploadInventoryDao uploadInventoryDao;
  private AgeingSearchStartViewModel mViewModel;
  private FragmentAgeingSearchStartBinding binding;
  private DialogAgeingEpcSearchBinding dialogageingEpcSearchBinding;
  private AlertDialog searchEPCDialog = null;
  private ProductModel model;
  private boolean isActionPick = false;
  private boolean isActionDecode = false;
  private String ageingType = AppConstants.AGEING_TYPE_ORDER;
  private String orderNo = "";
  private boolean isEANSearch = false;
  private boolean isAllowDecode = false;
  private boolean isStatusVerified = false;
  private List<Inventory> listAgeingPicked = new ArrayList<>(0);
  
  public static AgeingSearchStartFragment newInstance(){
    return new AgeingSearchStartFragment();
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
    uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
    if(isAllowDirectionalSearch) mainViewModel.getSensorAndStart();
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(AgeingSearchStartViewModel.class);
    binding = FragmentAgeingSearchStartBinding.inflate(inflater, container, false);
    final Serializable obj = extractSerializable(getArguments(), ProductModel.class);
    model = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
    
    if(model == null) popBackStack();
    
    binding.listAgeingDecode.setAdapter(new AgeingPickedListAdapter((MainActivity) context, this, listAgeingPicked));
    binding.listAgeingDecode.setLayoutManager(new LinearLayoutManager(context));
    
    // orderNo = chkNull(model.orderNo, "");
    ageingType = extractString(getArguments(), AppConstants.OMNICHANNEL_TYPE, AppConstants.AGEING_TYPE_ORDER);
    isEANSearch = extractBoolean(getArguments(), AppConstants.IS_EAN_SEARCH, ageingType.equalsIgnoreCase(AppConstants.AGEING_TYPE_EAN));
    isAllowDecode = extractBoolean(getArguments(), AppConstants.IS_ALLOW_DECODE, false);
    
   // binding.txtAgeingScoreCount.setText("" + 0);
    binding.ctwInventoryStart.setScore(0);
    
    binding.pdvAgeingStart.setProductModel(model);
    
    isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
    isActionDecode = isAllowDecode && binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbDecode.getId();
    //binding.clAgeingPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
    binding.ctwInventoryStart.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
    binding.clAgeingSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
    binding.llCircle.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
    binding.llBtnStart.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
    binding.listAgeingDecode.setVisibility(isActionDecode ? View.VISIBLE : View.GONE);
    
    binding.rbPick.setSelected(true);
    binding.rbSearch.setSelected(true);
    binding.rbDecode.setSelected(true);
    
    binding.divPick.setVisibility(!isAllowDecode || getSize() <= 0 ? View.VISIBLE : View.GONE);
    binding.rbDecode.setVisibility(isAllowDecode && getSize() > 0 ? View.VISIBLE : View.GONE);
    binding.rgPickSearchType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        if(isProcessOn()) mainViewModel.stopInventory();
        isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
        isActionDecode = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbDecode.getId();
       // binding.clAgeingPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.ctwInventoryStart.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.clAgeingSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
        binding.llPickedDecoded.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) && chkNotNullFalse(mainViewModel.getIsDecodeOn().getValue()) ? View.VISIBLE : View.GONE);
        binding.txtPicked.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) && chkNotNullFalse(mainViewModel.getIsDecodeOn().getValue()) ? View.VISIBLE : View.GONE);
        binding.llCircle.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
        binding.llBtnStart.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
        binding.listAgeingDecode.setVisibility(isActionDecode ? View.VISIBLE : View.GONE);
      }
    });
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        if(binding.pdvAgeingStart!=null) binding.pdvAgeingStart.dismissAlerts();
        System.out.println("Session :" + mainViewModel.getIsSessionOn().getValue());
        if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
        if(binding.llBtnStart.getVisibility() != View.VISIBLE) return;
        if(!isActionPick && chkNotNullTrue(mainViewModel.getIsPickOn().getValue())) return;
        if(isActionPick && chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())) return;
        if(!isActionPick && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())){
          context.dismissCustomAlertDialog();
          if(savedInstanceState == null && searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject();
              jsonExtras.put(ParamConstants.EAN, model.getEan());
              jsonExtras.put(ParamConstants.EAN_QTY, model.getEanQty());
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, AppConstants.OMNICHANNEL);
              jsonExtras.put(ParamConstants.OMNICHANNEL_TYPE, ageingType);
              jsonExtras.put(ParamConstants.ORDER_NO, orderNo);
              jsonExtras.put(ParamConstants.IS_ALLOW_DECODE, isAllowDecode);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(null, model.getEan()), model.getEanQty(), getSessionType().name(), ageingType, model.getOrderNo(), jsonExtras);
          }
          if(checkReaderConnected()){
            if(searchLog != null && isNullOrEmpty(searchLog.getSearchDurationTime()))
              searchStartTime = System.currentTimeMillis();
            mainViewModel.performBarcodeBasedSearch(model.getSearchEan(), binding.clAgeingSearch.isSingleTagSearch());
          }
        }
        else if(isActionPick && !chkNotNullTrue(mainViewModel.getIsPickOn().getValue())){
          context.dismissCustomAlertDialog();
          if(savedInstanceState == null && searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject();
              jsonExtras.put(ParamConstants.EAN, model.getEan());
              jsonExtras.put(ParamConstants.EAN_QTY, model.getEanQty());
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, AppConstants.OMNICHANNEL);
              jsonExtras.put(ParamConstants.OMNICHANNEL_TYPE, ageingType);
              jsonExtras.put(ParamConstants.ORDER_NO, orderNo);
              jsonExtras.put(ParamConstants.IS_ALLOW_DECODE, isAllowDecode);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", model.getEan(), model.getEanQty(), getSessionType().name(), ageingType, model.getOrderNo(), jsonExtras);
          }
          int size = getSize();
          if(size >= chkNull(model.eanQty, 0) || productDao.isQtyFound(model.ean)){
            context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_qty_matched), getTypeCharCode()));
            return;
          }
          if(checkReaderConnected()){
            if(size > 0 && !isSinglePick){
              context.showCustomAlertDialog(getString(R.string.title_confirm_action), String.format(getString(R.string.msg_clear_and_start), "" + size), getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                  inventoryDao.deleteInventory(sessionObject.sessionId, model.getEan(), model.getZone());
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
    
    setDefaultSearchViews();
    
    setSessionAction(AppConstants.SESSION_ACTION_START);
    return binding.getRoot();
  }
  
  @Override
  public int getSize(){
    int size = super.getSize();
    return sessionObject != null && model != null && inventoryDao != null ? inventoryDao.getEANQty(sessionObject.sessionId, model.getEan(), model.getZone()) : size;
  }
  
  public void apiCall(String action){
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    final boolean isVerifyForDecode = isAllowDecode && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_VERIFY_STATUS);
    if(isInternetConnected(context, false, isUpload || isVerifyForDecode)){
      try{
        if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.OMNICHANNEL_TYPE, getOmnichannelType());
        requestParams.put(ParamConstants.ORDER_NO, chkNull(orderNo, chkNull(model.orderNo, "")));
        requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        if(isEANSearch) requestParams.put(ParamConstants.EAN, model.getEan());
        //requestParams.put(ParamConstants.EAN_QTY, model.getEanQty());
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        if(sessionObject != null)
          requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
        if(isUpload){
          showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          new Handler().post(new Runnable(){
            @Override
            public void run(){
              try{
                JSONArray js = new JSONArray();
                if(model != null){
                  for(Inventory inventory : inventoryDao.getAllInventoryData(sessionObject.sessionId)){
                    if(inventory != null && ((isVerifyForDecode && !inventory.isUploaded && !inventory.isDecoded()) || (isUpload && !inventory.isUploaded && inventory.isDecoded()))){
                      JSONObject dataobject = inventory.toJson(context);
                      if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                        js.put(dataobject);
                    }
                  }
                }
                requestParams.put(ParamConstants.ITEMS, js);
                if(js != null && js.length() > 0)
                  callWebService(isVerifyForDecode ? URLConstants.VERIFY_OMNICHANNEL_FOR_DECODE : URLConstants.UPLOAD_OMNICHANNEL, requestParams, /*args,*/false, getString(isVerifyForDecode ? R.string.progress_msg_verifying_data : R.string.progress_msg_uploading_data), !isUpload);
              }
              catch(Exception e){
                e.printStackTrace();
                hideProgressDialog();
              }
            }
          });
        }
        else
          callWebService(isUpload ? URLConstants.UPLOAD_OMNICHANNEL : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload);
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
      }
    }
  }
  
  /**
   * Set default search views.
   */
  @Override
  protected void setDefaultSearchViews(){
    super.setDefaultSearchViews();
    if(dialogageingEpcSearchBinding != null && dialogageingEpcSearchBinding.clageingEpcSearch != null)
      dialogageingEpcSearchBinding.clageingEpcSearch.resetToDefault();
    else binding.clAgeingSearch.resetToDefault();
  }
  
  @Override
  public void setSessionObject(RFIDSession sessionObject){
    super.setSessionObject(sessionObject);
    if(sessionObject != null && sessionObject instanceof RFIDSession)
      this.sessionObject = (RFIDSession) sessionObject;
    showLog("searchSession", "" + (this.sessionObject != null));
  }
  
  @Override
  protected void onReaderConfigured(){
    super.onReaderConfigured();
  }
  
  @Override
  protected void isSessionOnChanged(final Boolean isSearchSessionOn){
    super.isSessionOnChanged(isSearchSessionOn);
    if(isSearchSessionOn == null) return;
    boolean isSerSessionOn = chkNotNullTrue(isSearchSessionOn);
  }
  
  @Override
  protected void isPickOnChanged(Boolean isPickOn){
    super.isPickOnChanged(isPickOn);
    if(isPickOn == null) return;
    else{
      updateViews();
      if(!isPickOn) stopTimer();
      binding.llBtnStart.toggle(isPickOn);
    }
  }
  
  @Override
  protected void isSearchOnChanged(Boolean isSearchOn){
    super.isSearchOnChanged(isSearchOn);
    if(isSearchOn == null) return;
    else{
      updateViews();
      if(!isSearchOn) stopTimer();
      else
        startTimer(dialogageingEpcSearchBinding != null && dialogageingEpcSearchBinding.clageingEpcSearch != null ? dialogageingEpcSearchBinding.clageingEpcSearch : binding.clAgeingSearch, dialogageingEpcSearchBinding != null && dialogageingEpcSearchBinding.imgSearchDir != null ? dialogageingEpcSearchBinding.imgSearchDir : binding.imgSearchDir);
      if(dialogageingEpcSearchBinding != null && dialogageingEpcSearchBinding.clageingEpcSearch != null)
        dialogageingEpcSearchBinding.clageingEpcSearch.setEnableCheck(!isSearchOn);
      if(dialogageingEpcSearchBinding != null && dialogageingEpcSearchBinding.llBtnStart != null && dialogageingEpcSearchBinding.btnDecode != null){
        dialogageingEpcSearchBinding.llBtnStart.toggle(isSearchOn);
        final Object tag = dialogageingEpcSearchBinding.btnDecode.getTag();
        final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
        dialogageingEpcSearchBinding.btnDecode.setVisibility(inventory != null && (isAllowDecodeOnPick || inventory.isUploaded) && !inventory.isDecoded() && !isSearchOn ? View.VISIBLE : View.GONE);
      }
      else{
        binding.llBtnStart.toggle(isSearchOn);
      }
    }
  }
  
  @Override
  protected void isDecodeOnChanged(Boolean isDecodeOn){
    super.isDecodeOnChanged(isDecodeOn);
    if(isDecodeOn == null) return;
    else{
      if(chkNotNullFalse(isDecodeOn)){
        if(sessionObject != null && chkNotNullTrue(mainViewModel.getIsDecodeDone().getValue())){//Case Tag Write Success
          AppCommonMethods.showLog("isDecodeDone", "" + true);
          if(dialogageingEpcSearchBinding != null && dialogageingEpcSearchBinding.btnDecode != null){
            final Object tag = dialogageingEpcSearchBinding.btnDecode.getTag();
            final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
            inventory.isUploaded = false;
            if(isNullOrEmpty(inventory.newEpc)) inventory.newEpc = "0" + inventory.epc.substring(1);
            dialogageingEpcSearchBinding.btnDecode.setTag(inventory);
            dialogageingEpcSearchBinding.btnDecode.setVisibility(inventory != null && (isAllowDecodeOnPick || inventory.isUploaded) && !inventory.isDecoded() && !isProcessOn() ? View.VISIBLE : View.GONE);
            apiCall(AppConstants.SESSION_ACTION_UPLOAD);
          }
          int decodeQty = inventoryDao.getEANDecodeQty(sessionObject.sessionId, model.getEan(), model.getZone());
          binding.txtDecoded.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_decoded_qty_total), "" + chkNull(decodeQty, 0), "" + model.getEanQty()), HtmlCompat.FROM_HTML_MODE_LEGACY));
          mainViewModel.getIsDecodeDone().postValue(false);
          updateLists();
        }
      }
      updateViews();
    }
  }
  
  @Override
  public void updateLists(){
    if(isAllowDecode && binding != null && binding.listAgeingDecode != null && binding.listAgeingDecode.getAdapter() != null && binding.listAgeingDecode.getAdapter() instanceof RecyclerView.Adapter){
      listAgeingPicked.clear();
      listAgeingPicked.addAll(inventoryDao.getEANInventory(sessionObject.sessionId, model.getEan(), model.zone));
      ((RecyclerView.Adapter) binding.listAgeingDecode.getAdapter()).notifyDataSetChanged();
    }
  }
  
  private void updateViews(){
    final boolean isProcessOn = chkNotNullTrue(mainViewModel.getIsDecodeOn().getValue()) || chkNotNullTrue(mainViewModel.getIsPickOn().getValue()) || chkNotNullTrue(mainViewModel.getIsSearchOn().getValue());
    if(dialogageingEpcSearchBinding != null){
      dialogageingEpcSearchBinding.btnDecode.setEnabled(!isProcessOn);
      dialogageingEpcSearchBinding.llBtnStart.setEnabled(!chkNotNullTrue(mainViewModel.getIsDecodeOn().getValue()));
    }
    binding.pdvAgeingStart.setEnabled(!isProcessOn);
    binding.rgPickSearchType.setEnabled(!isProcessOn);
    binding.rgPickSearchType.setVisibility(!isProcessOn?View.VISIBLE:View.GONE);
    binding.rbPick.setEnabled(!isProcessOn);
    binding.rbSearch.setEnabled(!isProcessOn);
    binding.rbDecode.setEnabled(isAllowDecode && !isProcessOn);
    binding.llPickedDecoded.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
    binding.txtPicked.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
    binding.txtDecoded.setVisibility(isAllowDecode && getSize() > 0 && !isProcessOn ? View.VISIBLE : View.GONE);
  }
  
  public void startDecode(Inventory inventory){
    if(isAllowDecode && !isProcessOn() && inventory != null && !inventory.isDecoded() && isNonEmpty(inventory.epc) && inventory.epc.length() > 1 && !inventory.epc.startsWith("0") && (isNullOrEmpty(inventory.newEpc) || !inventory.newEpc.startsWith("0"))){
      if(inventory.isUploaded){
        mainViewModel.performDecoding(inventory);
      }
      else{
        try{
          final String action = inventory.isDecoded() ? AppConstants.SESSION_ACTION_UPLOAD : AppConstants.SESSION_ACTION_VERIFY_STATUS;
          final boolean isVerifyForDecode = !inventory.isDecoded();
          Bundle args = new Bundle();
          args.putSerializable(inventory.getClass().getSimpleName(), inventory.isUploaded);
          JSONObject requestParams = new JSONObject();
          requestParams.put(ParamConstants.OMNICHANNEL_TYPE, getOmnichannelType());
          requestParams.put(ParamConstants.ORDER_NO, chkNull(model.getOrderNo(), ""));
          if(isEANSearch)
            requestParams.put(ParamConstants.EAN, isEANSearch ? chkNull(model.getEan(), "") : "");
          if(isVerifyForDecode)
            requestParams.put(ParamConstants.IS_VERIFY_DECODE, isVerifyForDecode);
          requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
          requestParams.put(ParamConstants.ACTION, action);
          requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
          JSONArray js = new JSONArray();
          JSONObject dataObject = inventory.toJson(context);
          if(dataObject != null && chkNull(dataObject.toString(), "").length() > 2)
            js.put(dataObject);
          requestParams.put(ParamConstants.ITEMS, js);
          callWebService(URLConstants.VERIFY_OMNICHANNEL_FOR_DECODE, requestParams, args, getString(R.string.progress_msg_verifying_data));
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    }
  }
  
  @Override
  public void startEPCSearch(Inventory inventory){
    if(isAllowDecode && !isProcessOn() && inventory != null){
      final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
      setAlertDialogCustomTitle(alertDialog, R.string.search);
      DisplayMetrics displayMetrics = new DisplayMetrics();
      context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
      final int wid = (context.isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) / 2;
      DialogAgeingEpcSearchBinding binding = DialogAgeingEpcSearchBinding.inflate(LayoutInflater.from(context), null, false);
      LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(wid, wid);
      binding.clEpcSearch.setLayoutParams(llParams);
      binding.btnDecode.setTag(inventory);
      binding.btnDecode.setVisibility((isAllowDecodeOnPick || inventory.isUploaded) && !inventory.isDecoded() ? View.VISIBLE : View.GONE);
      binding.btnDecode.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          if(!isProcessOn() && inventory != null && !inventory.isDecoded()) startDecode(inventory);
        }
      });
      binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          context.dismissCustomAlertDialog();
          if(!isProcessOn() && checkReaderConnected()){
            mainViewModel.performEPCBasedSearch(chkNull(inventory.newEpc, inventory.epc));
            searchStartTime = System.currentTimeMillis();
          }
          else if(chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()))
            mainViewModel.stopInventory();
        }
      });
      alertDialog.setView(binding.getRoot());
      alertDialog.setOnShowListener(new DialogInterface.OnShowListener(){
        @Override
        public void onShow(DialogInterface dialogInterface){
          dialogageingEpcSearchBinding = binding;
          searchEPCDialog = alertDialog;
        }
      });
      alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
        @Override
        public void onDismiss(DialogInterface dialogInterface){
          if(chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()))
            mainViewModel.stopInventory();
          dialogageingEpcSearchBinding = null;
          searchEPCDialog = null;
        }
      });
      alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
          context.handleTriggerKeyEvent(keyCode, event);
          return false;
        }
      });
      alertDialog.show();
    }
    else if(isAllowDecode && isProcessOn() && inventory != null)
      showShortToast(R.string.not_allowed);
    //showShortToast(String.format(getString(R.string.err_op_not_allowed),getTypeCharCode(),getSessionType().name()));
  }
  
  @Override
  protected void onTriggerPressed(){
    showLog(this.getClass().getSimpleName(), "onTriggerPressed");
    if(dialogageingEpcSearchBinding != null && dialogageingEpcSearchBinding.llBtnStart != null)
      dialogageingEpcSearchBinding.llBtnStart.performClick();
    else binding.llBtnStart.performClick();
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    size = chkNull(size, 0) > 0 ? inventoryDao.getEANQty(sessionObject.sessionId, model.getEan(), model.getZone()) : size;
    final boolean isJustPicked = size > 0 && model.getFoundQty() < size && !chkZero(/*binding.txtAgeingScoreCount.getText().toString()*/binding.ctwInventoryStart.getScore(), "-").equalsIgnoreCase("" + chkNull(size, 0));
   // binding.txtAgeingScoreCount.setText("" + chkNull(size, 0));
    binding.ctwInventoryStart.setScore(chkNull(size,0));
   // binding.txtAgeingTotal.setText("" + model.getEanQty());
    binding.ctwInventoryStart.setTotal(model.getEanQty());
    long total = Long.parseLong(chkNull(/*binding.txtAgeingTotal.getText().toString()*/binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
    final boolean isInvCount = chkNull(size, 0) > 0;
    final int decodeQty = inventoryDao.getEANDecodeQty(sessionObject.sessionId, model.getEan(), model.getZone());
    binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_picked_qty_total), "" + chkNull(size, 0), "" + model.getEanQty()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtDecoded.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_decoded_qty_total), String.valueOf(chkNull(decodeQty, 0)), model.getEanQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.divPick.setVisibility(!isAllowDecode || getSize() <= 0 ? View.VISIBLE : View.GONE);
    binding.rbDecode.setVisibility(isAllowDecode && getSize() > 0 ? View.VISIBLE : View.GONE);
    binding.progressAgeingStart.setVisibility(isInvCount && total > 0 ? View.VISIBLE : View.GONE);
    if(size > model.getEanQty()){
      context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_extra_tag), getTypeCharCode(), (chkNull(size, 0) + "/" + model.getEanQtyStr())));
      mainViewModel.stopInventory();
      inventoryDao.deleteInventory(sessionObject.sessionId, model.getEan(), model.getZone());
    }
    else if(isJustPicked && (size == model.getEanQty() || productDao.isQtyFound(model.ean))){
      if(productDao.isQtyFound(model.ean)){
        final int orderQty = model.getQty() <= 0 ? productDao.getOrderQty(model.ean) : 0;
        productDao.deleteExtraZones(model.ean);
        if(orderQty > 0 && productDao.getOrderQty(model.ean) <= 0)
          productDao.updateOrderQty(orderQty, model.ean);
      }
      AppCommonMethods.successBeep();
      new Handler().postDelayed(new Runnable(){
        @Override
        public void run(){
          onBackPressed();
        }
      }, 50);
    }
    double per = total > 0 ? (chkNull(size, 0) * 100) / total : 0;
    int percentage = (int) per;
    binding.progressAgeingStart.setProgress(percentage);
    updateViews();
    updateLists();
  }
  
  @Override
  public void onPause(){
    super.onPause();
  }
  
  @Override
  public void onResume(){
    super.onResume();
  }
  
  private boolean isAllDecoded(){
    final MultiQtyModel multiQtyModel = isEANSearch ? productDao.getTotalOmniEanCounts(model.getEan()) : productDao.getTotalOmniOrderCounts(model.getOrderNo());
    final int decoded = multiQtyModel != null ? chkNull(multiQtyModel.decoded, 0) : 0;
    final int found = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
    final int total = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
    final int required = multiQtyModel != null ? chkNull(multiQtyModel.required, 0) : 0;
    final boolean isAllDecoded = sessionObject != null && inventoryDao.getInventorySize(sessionObject.sessionType) >= required && inventoryDao.getNonDecodedCount(sessionObject.sessionType) == 0;
    return isAllDecoded;
  }
  
  @Override
  public void onDestroyView(){
    sessionObject = null;
    super.onDestroyView();
  }
  
  public String getOmnichannelType(){
    return !chkNull(SharedPrefManager.getOmnichannelType(), AppConstants.AGEING_TYPE_BOTH).equalsIgnoreCase(AppConstants.AGEING_TYPE_BOTH) ? SharedPrefManager.getOmnichannelType() : ageingType.toLowerCase().trim();
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
      sessionObject.zone = model.getZone();
      sessionObject.zoneId = model.getZoneId();
      sessionObject.brands = model.getBrand();
      sessionObject.category = model.getCategory();
      sessionObject.total = model.getEanQtyStr();
      sessionObject.sessionType = AppCommonMethods.SessionType.SEARCH_AGEING.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.SEARCH.getValue();
      try{
        JSONObject jsonExtras = new JSONObject();
        
        if(isEANSearch) jsonExtras.put(ParamConstants.EAN, model.getEan());
        sessionObject.extras = jsonExtras.toString();
      }
      catch(Exception e){ e.printStackTrace(); }
      Calendar cc = Calendar.getInstance();
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, 24);
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc);
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, false);
      insertExistingData(sessionObject);
    }
    else if(sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      sessionObject.eans = model.getEan();
      sessionObject.zone = model.getZone();
      sessionObject.zoneId = model.getZoneId();
      sessionObject.brands = model.getBrand();
      sessionObject.category = model.getCategory();
      sessionObject.total = model.getEanQtyStr();
      sessionObject.sessionType = AppCommonMethods.SessionType.OMNICHANNEL.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.SEARCH.getValue();
      try{
        JSONObject jsonExtras = new JSONObject();
        jsonExtras.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
        jsonExtras.put(ParamConstants.SESSION_TYPE, AppCommonMethods.SessionType.OMNICHANNEL.name());
        jsonExtras.put(ParamConstants.TYPE, AppConstants.OMNICHANNEL);
        jsonExtras.put(ParamConstants.OMNICHANNEL_TYPE, getOmnichannelType());
        jsonExtras.put(ParamConstants.ORDER_NO, chkNull(orderNo, chkNull(model.orderNo, "")));
        sessionObject.extras = jsonExtras.toString();
      }
      catch(Exception e){ e.printStackTrace(); }
      mainViewModel.startSession(sessionObject, false);
      insertExistingData(sessionObject);
    }
    else if(sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_RESUME)){
      mainViewModel.startSession(sessionObject, false);
    }
  }
  
  private void insertExistingData(final RFIDSession sessionObject){
    if(model != null && isNonEmpty(model.getPickedEPCs()) && sessionObject != null){
      new Handler().post(new Runnable(){
        @Override
        public void run(){
          try{
            final JSONArray jsonTags = new JSONArray(model.getPickedEPCs());
            if(jsonTags != null && jsonTags.length() > 0){
              for(int i = 0; i < jsonTags.length(); i++){
                final JSONObject jsonTag = jsonTags.getJSONObject(i);
                Inventory inventory = new Inventory(sessionObject.sessionId, sessionObject.sessionType, AppCommonMethods.SessionAction.PICK.getValue());
                final boolean isDecoded = extractBoolean(jsonTag, ParamConstants.IS_DECODED, false);
                final String epc = extractString(jsonTag, ParamConstants.EPC, "").toUpperCase();
                if(!inventoryDao.isEPCPresent(model.getEan(), sessionObject.sessionId, epc)){
                  inventory.ean = model.getEan();
                  inventory.zone = model.getZone();
                  inventory.tid = extractString(jsonTag, ParamConstants.TID, "");
                  inventory.epc = epc;
                  inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
                  if(isDecoded)
                    inventory.newEpc = epc.length() > 1 && !epc.startsWith("0") ? "0" + epc.substring(1) : epc;
                  /*inventory.isHardTag = true;
                  inventory.tagtype = context.epcEncoderDecoder.getTagType(epc);
                  try{
                    inventory.isHardTag = SGTIN96.isTagTypeHard(epc);
                  }catch(Exception e){e.printStackTrace();}*/
                  inventory.isUploaded = true;
                  inventory.retryUploadCount = 0;
                  inventory.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                  try{
                    inventoryDao.insertInventoryData(inventory);
                  }
                  catch(Exception e){ e.printStackTrace(); }
                }
              }
            }
          }
          catch(Exception e){ }
        }
      });
    }
  }
  
  @Override
  public void onBackPressed(){
    super.onBackPressed();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.SET_SESSION:
          break;
        case URLConstants.VERIFY_OMNICHANNEL_FOR_DECODE:
          if(isSuccess){
            Object obj = extractSerializable(args, Inventory.class);
            if(obj != null && obj instanceof Inventory){
              final Inventory inventory = (Inventory) obj;
              if(inventory != null && !inventory.isDecoded()){
                inventory.isUploaded = true;
                inventoryDao.updateInventoryData(inventory);
                if(dialogageingEpcSearchBinding != null && dialogageingEpcSearchBinding.btnDecode != null){
                  dialogageingEpcSearchBinding.btnDecode.setTag(inventory);
                  dialogageingEpcSearchBinding.btnDecode.setVisibility((isAllowDecodeOnPick || inventory.isUploaded) && !inventory.isDecoded() ? View.VISIBLE : View.GONE);
                  dialogageingEpcSearchBinding.btnDecode.performClick();
                }
                else startDecode(inventory);
              }
            }
          }
        case URLConstants.UPLOAD_OMNICHANNEL:
          if(isSuccess){
            final boolean isVerifyForDecode = extractBoolean(jsonRequest, ParamConstants.IS_VERIFY_DECODE, false);
            if(isVerifyForDecode){
              Object obj = extractSerializable(args, Inventory.class);
              if(obj != null && obj instanceof Inventory){
                final Inventory inventory = (Inventory) obj;
                if(inventory != null && !inventory.isDecoded()){
                  inventory.isUploaded = true;
                  inventoryDao.updateInventoryData(inventory);
                  if(dialogageingEpcSearchBinding != null && dialogageingEpcSearchBinding.btnDecode != null){
                    dialogageingEpcSearchBinding.btnDecode.setTag(inventory);
                    dialogageingEpcSearchBinding.btnDecode.setVisibility((isAllowDecodeOnPick || inventory.isUploaded) && !inventory.isDecoded() ? View.VISIBLE : View.GONE);
                    dialogageingEpcSearchBinding.btnDecode.performClick();
                  }
                  else startDecode(inventory);
                }
              }
            }
            else{
              final int sessionType = sessionObject != null ? sessionObject.sessionType : 0;
              if(sessionType > 0){
                final boolean isAllDecoded = isAllDecoded();
                final int decodeQty = inventoryDao.getEANDecodeQty(sessionObject.sessionId, model.getEan(), model.getZone());
                final boolean isEanZoneDecoded = isAutoBackOnEanZoneDecoded && decodeQty >= model.getEanQty();
                showLog("isAllDecoded", "" + isAllDecoded);
                if((isAllDecoded || isEanZoneDecoded) && searchEPCDialog != null && searchEPCDialog.isShowing())
                  searchEPCDialog.dismiss();
                context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_decode), getTypeCharCode())).replaceFirst("(?i)Upload", "Decode"), isAllDecoded || isEanZoneDecoded ? new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i){
                    if(isAllDecoded) context.doublePopBackStack();
                    else if(isEanZoneDecoded) context.popBackStack();
                  }
                } : null);
                //update Uploaded Records
                new Handler().post(new Runnable(){
                  @Override
                  public void run(){
                    if(isAllDecoded) inventoryDao.deleteInventory(sessionType);
                    JSONArray js = extractJSONArray(jsonRequest, ParamConstants.ITEMS);
                    Set<String> tids = new HashSet<String>(0);
                    if(isNonEmpty(js)){
                      for(int i = 0; i < js.length(); i++){
                        try{
                          final String tid = extractString(js.getJSONObject(i), ParamConstants.TID, "").trim();
                          if(isNonEmpty(tid))
                            tids.add(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid);
                        }
                        catch(Exception e){ e.printStackTrace(); }
                      }
                    }
                    if(isNonEmpty(tids)){
                      if(!isAllDecoded) inventoryDao.updateUploaded(sessionType, tids);
                      uploadInventoryDao.updateUploaded(sessionType, tids);
                    }
                    uploadInventoryDao.deleteUploaded();
                    updateLists();
                  }
                });
              }
            }
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
}