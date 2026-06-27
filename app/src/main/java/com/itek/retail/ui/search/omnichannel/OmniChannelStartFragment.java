package com.itek.retail.ui.search.omnichannel;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
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
import com.itek.retail.adapter.OmniPickedListAdapter;
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
import com.itek.retail.databinding.DialogOmniEpcSearchBinding;
import com.itek.retail.databinding.FragmentOmnichannelStartBinding;
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

/**
 * The Omni channel start fragment.
 */
public class OmniChannelStartFragment extends RFIDSessionFragment{
  
  ProductDao productDao;
  InventoryDao inventoryDao;
  UploadInventoryDao uploadInventoryDao;
  private OmniChannelStartViewModel mViewModel;
  private FragmentOmnichannelStartBinding binding;
  private DialogOmniEpcSearchBinding dialogOmniEpcSearchBinding;
  private AlertDialog searchEPCDialog = null;
  private ProductModel model;
  private String mappedEan;
  private boolean isActionPick = false;
  private boolean isActionDecode = false;
  private String omniType = AppConstants.OMNI_TYPE_ORDER;
  private String orderNo = "";
  private boolean isEANSearch = false;
  private boolean isAllowDecode = false;
  private boolean isAllowDecodeOnPick = false;
  private boolean isAllowDecodeWithoutVerify = false;
  private boolean isStatusVerified = false;
  private List<Inventory> listOmniPicked = new ArrayList<>(0);
  
  /**
   * Instantiates a new Omni channel start fragment.
   */
  public OmniChannelStartFragment(){/*Default/Empty Constructor*/}
  
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
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OmniChannelStartViewModel.class);
    binding = FragmentOmnichannelStartBinding.inflate(inflater, container, false);
    mappedEan = !SharedPrefManager.getIsEANMapped() ? "" : extractString(getArguments(), ParamConstants.MAPPED_EAN, "");
    final Serializable obj = extractSerializable(getArguments(), ProductModel.class);
    model = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
    
    if(model == null) popBackStack();
    
    binding.listOmniDecode.setAdapter(new OmniPickedListAdapter(context, this, listOmniPicked));
    binding.listOmniDecode.setLayoutManager(new LinearLayoutManager(context));
    
    orderNo = chkNull(model.orderNo, "");
    omniType = extractString(getArguments(), AppConstants.OMNICHANNEL_TYPE, AppConstants.OMNI_TYPE_ORDER);
    isEANSearch = extractBoolean(getArguments(), AppConstants.IS_EAN_SEARCH, omniType.equalsIgnoreCase(AppConstants.OMNI_TYPE_EAN));
    isAllowDecode = extractBoolean(getArguments(), AppConstants.IS_ALLOW_DECODE, false);
    isAllowDecodeOnPick = isAllowDecode && extractBoolean(getArguments(), AppConstants.IS_ALLOW_DECODE_ON_PICK, false);
    isAllowDecodeWithoutVerify = isAllowDecode && extractBoolean(getArguments(), AppConstants.IS_ALLOW_DECODE_WITHOUT_VERIFY, false);
    
   // binding.txtOmniChannelScoreCount.setText("" + 0);
    binding.ctwInventoryStart.setScore(0);
  //  binding.txtOmniChannelStartTotal.setText("" + model.getEanQty());
    binding.ctwInventoryStart.setTotal(model.getEanQty());
    
    binding.pdvOmniStart.setProductModel(model);
    
    isActionPick = binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbPick.getId();
    isActionDecode = isAllowDecode && binding.rgPickSearchType.getCheckedRadioButtonId() == binding.rbDecode.getId();
  //  binding.clOmniChannelPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
    binding.ctwInventoryStart.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
    binding.clOmniChannelSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
    binding.llCircle.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
    binding.llBtnStart.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
    binding.listOmniDecode.setVisibility(isActionDecode ? View.VISIBLE : View.GONE);
    
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
        //binding.clOmniChannelPick.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.ctwInventoryStart.setVisibility(isActionPick ? View.VISIBLE : View.GONE);
        binding.clOmniChannelSearch.setVisibility(!isActionPick ? View.VISIBLE : View.GONE);
        binding.llPickedDecoded.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) && chkNotNullFalse(mainViewModel.getIsDecodeOn().getValue()) ? View.VISIBLE : View.GONE);
        binding.txtPicked.setVisibility((isAllowDecode || !isActionPick) && getSize() > 0 && chkNotNullFalse(mainViewModel.getIsSearchOn().getValue()) && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) && chkNotNullFalse(mainViewModel.getIsDecodeOn().getValue()) ? View.VISIBLE : View.GONE);
        binding.llCircle.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
        binding.llBtnStart.setVisibility(!isActionDecode ? View.VISIBLE : View.GONE);
        binding.listOmniDecode.setVisibility(isActionDecode ? View.VISIBLE : View.GONE);
      }
    });
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        if(binding.pdvOmniStart!=null)binding.pdvOmniStart.dismissAlerts();
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
              jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
              jsonExtras.put(ParamConstants.EAN_QTY, model.getEanQty());
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, true);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, AppConstants.OMNICHANNEL);
              jsonExtras.put(ParamConstants.OMNICHANNEL_TYPE, omniType);
              jsonExtras.put(ParamConstants.ORDER_NO, orderNo);
              jsonExtras.put(ParamConstants.IS_ALLOW_DECODE, isAllowDecode);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), model.getEanQty(), getSessionType().name(), omniType, model.getOrderNo(), jsonExtras);
          }
          if(checkReaderConnected()){
            if(searchLog != null && isNullOrEmpty(searchLog.getSearchDurationTime()))
              searchStartTime = System.currentTimeMillis();
            mainViewModel.performBarcodeBasedSearch(chkNull(mappedEan, model.getSearchEan()), binding.clOmniChannelSearch.isSingleTagSearch());
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
              jsonExtras.put(ParamConstants.TYPE, AppConstants.OMNICHANNEL);
              jsonExtras.put(ParamConstants.OMNICHANNEL_TYPE, omniType);
              jsonExtras.put(ParamConstants.ORDER_NO, orderNo);
              jsonExtras.put(ParamConstants.IS_ALLOW_DECODE, isAllowDecode);
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(mappedEan, model.getEan()), model.getEanQty(), getSessionType().name(), omniType, model.getOrderNo(), jsonExtras);
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
                  inventoryDao.deleteInventory(sessionObject.sessionId, chkNull(mappedEan, model.getEan()), model.getZone());
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
    
    setDefaultSearchViews();
    
    setSessionAction(AppConstants.SESSION_ACTION_START);
    return binding.getRoot();
  }
  
  @Override
  public int getSize(){
    int size = super.getSize();
    return sessionObject != null && model != null && inventoryDao != null ? inventoryDao.getEANQty(sessionObject.sessionId, chkNull(mappedEan, model.getEan()), model.getZone()) : size;
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
    if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null)
      dialogOmniEpcSearchBinding.clOmniChannelEpcSearch.resetToDefault();
    else binding.clOmniChannelSearch.resetToDefault();
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
  protected void onPickDataChanged(Inventory pickData){
    super.onPickDataChanged(pickData);
    if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
    if(isAllowDecodeOnPick && pickData != null){
      showLog("EAN", pickData.ean);
      showLog("EPC", pickData.epc);
      showLog("TID", pickData.tid);
      showLog("PC", pickData.pcdata);
      try{
        startDecode(pickData);
      }
      catch(Exception e){ e.printStackTrace(); }
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
        startTimer(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null ? dialogOmniEpcSearchBinding.clOmniChannelEpcSearch : binding.clOmniChannelSearch, dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.imgSearchDir != null ? dialogOmniEpcSearchBinding.imgSearchDir : binding.imgSearchDir);
      if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null)
        dialogOmniEpcSearchBinding.clOmniChannelEpcSearch.setEnableCheck(!isSearchOn);
      if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null && dialogOmniEpcSearchBinding.btnDecode != null){
        dialogOmniEpcSearchBinding.llBtnStart.toggle(isSearchOn);
        final Object tag = dialogOmniEpcSearchBinding.btnDecode.getTag();
        final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
        dialogOmniEpcSearchBinding.btnDecode.setVisibility(inventory != null && (isAllowDecodeWithoutVerify || inventory.isUploaded) && !inventory.isDecoded() && !isSearchOn ? View.VISIBLE : View.GONE);
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
          if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.btnDecode != null){
            final Object tag = dialogOmniEpcSearchBinding.btnDecode.getTag();
            final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
            inventory.isUploaded = false;
            if(isNullOrEmpty(inventory.newEpc)) inventory.newEpc = "0" + inventory.epc.substring(1);
            dialogOmniEpcSearchBinding.btnDecode.setTag(inventory);
            dialogOmniEpcSearchBinding.btnDecode.setVisibility(inventory != null && (isAllowDecodeWithoutVerify || inventory.isUploaded) && !inventory.isDecoded() && !isProcessOn() ? View.VISIBLE : View.GONE);
            apiCall(AppConstants.SESSION_ACTION_UPLOAD);
          }
          int decodeQty = inventoryDao.getEANDecodeQty(sessionObject.sessionId, chkNull(mappedEan, model.getEan()), model.getZone());
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
    if(isAllowDecode && binding != null && binding.listOmniDecode != null && binding.listOmniDecode.getAdapter() != null && binding.listOmniDecode.getAdapter() instanceof RecyclerView.Adapter){
      listOmniPicked.clear();
      listOmniPicked.addAll(inventoryDao.getEANInventory(sessionObject.sessionId, chkNull(mappedEan, model.getEan()), model.zone));
      ((RecyclerView.Adapter) binding.listOmniDecode.getAdapter()).notifyDataSetChanged();
    }
  }
  
  private void updateViews(){
    final boolean isProcessOn = chkNotNullTrue(mainViewModel.getIsDecodeOn().getValue()) || chkNotNullTrue(mainViewModel.getIsPickOn().getValue()) || chkNotNullTrue(mainViewModel.getIsSearchOn().getValue());
    if(dialogOmniEpcSearchBinding != null){
      dialogOmniEpcSearchBinding.btnDecode.setEnabled(!isProcessOn);
      dialogOmniEpcSearchBinding.llBtnStart.setEnabled(!chkNotNullTrue(mainViewModel.getIsDecodeOn().getValue()));
    }
    binding.pdvOmniStart.setEnabled(!isProcessOn);
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
  
  public void removeNonDecodedTag(Inventory inventory){
    if(isAllowDecode && !isProcessOn() && inventory != null && !inventory.isUploaded && !inventory.isDecoded()){
      context.showCustomAlertDialog("", String.format(getString(R.string.msg_list_delete_tag), inventory.ean), R.string.btn_delete_all, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
          //UnPick/Remove Tag
          if(isAllowDecode && !isProcessOn() && inventory != null && !inventory.isUploaded && !inventory.isDecoded()){
            inventoryDao.deleteInventoryData(inventory);
            productDao.updateDeletedEPC(inventory.epc, model.ean, model.zone);
          }
        }
      }, R.string.btn_cancel, null);
      
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
      DialogOmniEpcSearchBinding binding = DialogOmniEpcSearchBinding.inflate(LayoutInflater.from(context), null, false);
      LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(wid, wid);
      binding.clOmniEPCSearch.setLayoutParams(llParams);
      binding.btnDecode.setTag(inventory);
      binding.btnDecode.setVisibility((isAllowDecodeWithoutVerify || inventory.isUploaded) && !inventory.isDecoded() ? View.VISIBLE : View.GONE);
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
          dialogOmniEpcSearchBinding = binding;
          searchEPCDialog = alertDialog;
        }
      });
      alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
          context.handleTriggerKeyEvent(keyCode, event);
          return false;
        }
      });
      alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
        @Override
        public void onDismiss(DialogInterface dialogInterface){
          if(chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()))
            mainViewModel.stopInventory();
          dialogOmniEpcSearchBinding = null;
          searchEPCDialog = null;
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
    if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null)
      dialogOmniEpcSearchBinding.llBtnStart.performClick();
    else binding.llBtnStart.performClick();
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    size = chkNull(size, 0) > 0 ? inventoryDao.getEANQty(sessionObject.sessionId, chkNull(mappedEan, model.getEan()), model.getZone()) : size;
   // final boolean isJustPicked = size > 0 && model.getFoundQty() < size && !chkZero(binding.txtOmniChannelScoreCount.getText().toString(), "-").equalsIgnoreCase("" + chkNull(size, 0));
    final boolean isJustPicked = size > 0 && model.getFoundQty() < size && !chkZero(binding.ctwInventoryStart.getTotal(), "-").equalsIgnoreCase("" + chkNull(size, 0));
   // binding.txtOmniChannelScoreCount.setText("" + chkNull(size, 0));
    binding.ctwInventoryStart.setScore(chkNull(size,0));
   // binding.txtOmniChannelStartTotal.setText("" + model.getEanQty());
    binding.ctwInventoryStart.setTotal(model.getEanQty());
   // long total = Long.parseLong(chkNull(binding.txtOmniChannelStartTotal.getText().toString().replace("-", ""), "0"));
    long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
    final boolean isInvCount = chkNull(size, 0) > 0;
    final int decodeQty = inventoryDao.getEANDecodeQty(sessionObject.sessionId, chkNull(mappedEan, model.getEan()), model.getZone());
    binding.txtPicked.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_picked_qty_total), "" + chkNull(size, 0), "" + model.getEanQty()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtDecoded.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_decoded_qty_total), String.valueOf(chkNull(decodeQty, 0)), model.getEanQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.divPick.setVisibility(!isAllowDecode || getSize() <= 0 ? View.VISIBLE : View.GONE);
    binding.rbDecode.setVisibility(isAllowDecode && getSize() > 0 ? View.VISIBLE : View.GONE);
    if(size > model.getEanQty()){
      context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_extra_tag), getTypeCharCode(), (chkNull(size, 0) + "/" + model.getEanQtyStr())));
      mainViewModel.stopInventory();
      inventoryDao.deleteInventory(sessionObject.sessionId, chkNull(mappedEan, model.getEan()), model.getZone());
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
    return !chkNull(SharedPrefManager.getOmnichannelType(), AppConstants.OMNI_TYPE_BOTH).equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH) ? SharedPrefManager.getOmnichannelType() : omniType.toLowerCase().trim();
  }
  
  /**
   * Set session action.
   *
   * @param action the action
   */
  public void setSessionAction(String action){
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.eans = chkNull(mappedEan, model.getEan());
      sessionObject.zone = model.getZone();
      sessionObject.zoneId = model.getZoneId();
      sessionObject.brands = model.getBrand();
      sessionObject.category = model.getCategory();
      sessionObject.total = model.getEanQtyStr();
      sessionObject.sessionType = AppCommonMethods.SessionType.OMNICHANNEL.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.SEARCH.getValue();
      try{
        JSONObject jsonExtras = new JSONObject();
        jsonExtras.put(ParamConstants.TYPE, AppConstants.OMNICHANNEL);
        jsonExtras.put(ParamConstants.OMNICHANNEL_TYPE, getOmnichannelType());
        jsonExtras.put(ParamConstants.ORDER_NO, chkNull(orderNo, chkNull(model.orderNo, "")));
        if(isEANSearch) jsonExtras.put(ParamConstants.EAN, chkNull(mappedEan, model.getEan()));
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
      sessionObject.eans = chkNull(mappedEan, model.getEan());
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
                if(!inventoryDao.isEPCPresent(chkNull(mappedEan, model.getEan()), sessionObject.sessionId, epc)){
                  inventory.ean = chkNull(mappedEan, model.getEan());
                  inventory.zone = model.getZone();
                  inventory.tid = extractString(jsonTag, ParamConstants.TID, "");
                  inventory.epc = epc;
                  inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
                  if(isDecoded)
                    inventory.newEpc = epc.length() > 1 && !epc.startsWith("0") ? "0" + epc.substring(1) : epc;
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
                if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.btnDecode != null){
                  dialogOmniEpcSearchBinding.btnDecode.setTag(inventory);
                  dialogOmniEpcSearchBinding.btnDecode.setVisibility((isAllowDecodeWithoutVerify || inventory.isUploaded) && !inventory.isDecoded() ? View.VISIBLE : View.GONE);
                  dialogOmniEpcSearchBinding.btnDecode.performClick();
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
                  if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.btnDecode != null){
                    dialogOmniEpcSearchBinding.btnDecode.setTag(inventory);
                    dialogOmniEpcSearchBinding.btnDecode.setVisibility((isAllowDecodeWithoutVerify || inventory.isUploaded) && !inventory.isDecoded() ? View.VISIBLE : View.GONE);
                    dialogOmniEpcSearchBinding.btnDecode.performClick();
                  }
                  else startDecode(inventory);
                }
              }
            }
            else{
              final int sessionType = sessionObject != null ? sessionObject.sessionType : 0;
              if(sessionType > 0){
                final boolean isAllDecoded = isAllDecoded();
                final int decodeQty = inventoryDao.getEANDecodeQty(sessionObject.sessionId, chkNull(mappedEan, model.getEan()), model.getZone());
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