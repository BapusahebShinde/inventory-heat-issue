package com.itek.retail.ui.search.ageing;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUse24LengthTIDForUpload;
import static com.itek.retail.common.AppCommonMethods.successBeep;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.ProductListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.databinding.FragmentOmnichannelListDetailsBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.MainViewModel;
import com.itek.retail.ui.search.omnichannel.OmniChannelListDetailsViewModel;
import com.itek.retail.ui.search.omnichannel.OmniChannelStartFragment;

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

public class AgeingSearchListFragment extends CommonFragment{
  
  final AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.SEARCH_AGEING;
  MainViewModel mainViewModel;
  ProductDao productDao;
  InventoryDao inventoryDao;
  UploadInventoryDao uploadInventoryDao;
  ProductModel selProd;
  boolean isEANSearch = false;
  boolean isAllowDecode = false;
  boolean isStatusVerified = false;
  String omniType = AppConstants.OMNI_TYPE_ORDER;
  String omniUploadType = AppConstants.OMNI_UPLOAD_TYPE_COMPLETE;
  private OmniChannelListDetailsViewModel mViewModel;
  private FragmentOmnichannelListDetailsBinding binding;
  private List<ProductModel> listProducts = new ArrayList<>(0);
  
  /**
   * Instantiates a new Omni channel list details fragment.
   */
  public AgeingSearchListFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    mainViewModel = ((MainActivity) context).getRfidViewModel();
    productDao = AppDatabase.getProductDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
    uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OmniChannelListDetailsViewModel.class);
    binding = FragmentOmnichannelListDetailsBinding.inflate(inflater, container, false);
    
    final Serializable obj = extractSerializable(getArguments(), ProductModel.class);
    selProd = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
    if(selProd == null) popBackStack();
    
    omniType = extractString(getArguments(), AppConstants.OMNICHANNEL_TYPE, AppConstants.OMNI_TYPE_ORDER);
    omniUploadType = extractString(getArguments(), AppConstants.OMNICHANNEL_UPLOAD_TYPE, AppConstants.OMNI_UPLOAD_TYPE_COMPLETE);
    isEANSearch = extractBoolean(getArguments(), AppConstants.IS_EAN_SEARCH, omniType.equalsIgnoreCase(AppConstants.OMNI_TYPE_EAN));
    isAllowDecode = extractBoolean(getArguments(), AppConstants.IS_ALLOW_DECODE, false);
    final boolean hasPickData = extractBoolean(getArguments(), AppConstants.HAS_PICK_DATA, false);
    showLog("hasPickData", "" + hasPickData);
    if(isAllowDecode && isStatusVerified()) mainViewModel.getReaderUHFInstance(sessionType);
    binding.listOmnichannelSearchDetails.setAdapter(new ProductListAdapter((MainActivity) context, this, listProducts));
    binding.listOmnichannelSearchDetails.setLayoutManager(/*isLandscape?new GridLayoutManager(context,2) :*/new LinearLayoutManager(context));
    binding.txtOmnichannelOrdernoEan.setVisibility(!isEANSearch ? View.VISIBLE : View.GONE);
    
    binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.showCustomConfirmDialog(R.string.title_stock_correction_upload, R.string.btn_upload, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            callUpload();
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
            callUpload();
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
              callUpload();
            }
          });
        }
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);*/
    
    updateLists();
    
    if(hasPickData){
      RFIDSession sessionObject = mainViewModel.updateActiveSessionFlag(sessionType, true);
      if(sessionObject == null || isNullOrEmpty(sessionObject.sessionId) || sessionObject.sessionType <= 0 || sessionObject.sessionType != sessionType.getValue()){
        sessionObject = new RFIDSession();
        sessionObject.sessionType = sessionType.getValue();
        sessionObject.sessionAction = AppCommonMethods.SessionAction.SEARCH.getValue();
        try{
          JSONObject jsonExtras = new JSONObject();
          jsonExtras.put(ParamConstants.TYPE, AppConstants.OMNICHANNEL);
          jsonExtras.put(ParamConstants.OMNICHANNEL_TYPE, getOmnichannelType());
          jsonExtras.put(ParamConstants.ORDER_NO, selProd.getOrderNo());
          if(isEANSearch) jsonExtras.put(ParamConstants.EAN, selProd.getEan());
          sessionObject.extras = jsonExtras.toString();
        }
        catch(Exception e){ e.printStackTrace(); }
        Calendar cc = Calendar.getInstance();
        sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
        cc.add(Calendar.HOUR_OF_DAY, 24);
        sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
        sessionObject.sessionId = mainViewModel.generateOfflineSessionId(sessionType, cc);
        sessionObject.isRunning = true;
        sessionObject.isUploading = false;
        AppDatabase.getRIFDSessionDao(context).insert(sessionObject);
      }
      AppCommonMethods.showLog("sessionObject", sessionObject.sessionId + "_" + sessionObject.sessionType);
      insertExistingData(sessionObject);
    }
    
    return binding.getRoot();
  }
  
  public void callUpload(){
    if(isAllowDecode){
      mainViewModel.getReaderUHFInstance(sessionType);
      final int nonDecodedVerifiedCount = inventoryDao.getNonDecodedVerifiedCount(sessionType.getValue());
      final int nonUploadedDecodeCount = inventoryDao.getNonUploadedDecodeCount(sessionType.getValue());
      if(isStatusVerified() && nonDecodedVerifiedCount > 0 && nonUploadedDecodeCount <= 0){
        performDecoding();
      }
      else apiCall(AppConstants.SESSION_ACTION_UPLOAD);
    }
    else apiCall(AppConstants.SESSION_ACTION_UPLOAD);
  }
  
  public boolean hasNonDecodedVerifiedCount(){
    int nonDecodedVerifiedCount = 0;
    try{
      if(isStatusVerified() && isNonEmpty(listProducts)){
        for(ProductModel model : listProducts){
          if(model != null && isNonEmpty(model.getPickedEPCs())){
            final JSONArray jsonTags = new JSONArray(model.getPickedEPCs());
            if(jsonTags != null && jsonTags.length() > 0){
              for(int i = 0; i < jsonTags.length(); i++){
                final JSONObject jsonTag = jsonTags.getJSONObject(i);
                final boolean isDecoded = extractBoolean(jsonTag, ParamConstants.IS_DECODED, false);
                if(!isDecoded) nonDecodedVerifiedCount++;
                else return false;
              }
            }
          }
        }
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return nonDecodedVerifiedCount > 0;
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    if(isAllowDecode){
      inventoryDao.getNonVerifiedPickCount(sessionType.getValue()).removeObservers(getViewLifecycleOwner());
      inventoryDao.getNonVerifiedPickCount(sessionType.getValue()).observe(getViewLifecycleOwner(), new Observer<Integer>(){
        @Override
        public void onChanged(Integer nonVerifiedPickCount){
          isStatusVerified = nonVerifiedPickCount <= 0;
        }
      });
      mainViewModel.getIsDecodeOn().removeObservers(getViewLifecycleOwner());
      mainViewModel.getIsDecodeOn().observe(getViewLifecycleOwner(), isDecodeOn -> {
        if(!isTopInStack()) return;
        showLog("RFIDFrag_isDecodeOn", AppCommonMethods.chkVal(isDecodeOn));
        if(isDecodeOn == null) return;
        else{
          updateLists();
          showLog("RFIDFrag_isDecodeDone", AppCommonMethods.chkVal(chkNotNullTrue(mainViewModel.getIsDecodeDone().getValue())));
          if(chkNotNullTrue(mainViewModel.getIsDecodeDone().getValue())){//Case Tag Write Success
            mainViewModel.getIsDecodeDone().postValue(false);
            final boolean isAllDecoded = inventoryDao.getNonDecodedCount(sessionType.getValue()) == 0;
            showLog("isAllDecoded", "" + isAllDecoded);
            final int decoded = inventoryDao.getNonUploadedDecodeCount(sessionType.getValue());
            showLog("decoded", "" + decoded);
            if(isAllDecoded || decoded > 0){
              apiCall(AppConstants.SESSION_ACTION_UPLOAD);
            }
          }
        }
      });
    }
  }
  
  /**
   * Is allow decode boolean.
   *
   * @return the boolean
   */
  public boolean isAllowDecode(){ return isAllowDecode; }
  
  /**
   * Is status verified boolean.
   *
   * @return the boolean
   */
  public boolean isStatusVerified(){
    return isStatusVerified;
  }
  
  /**
   * Is ean search boolean.
   *
   * @return the boolean
   */
  public boolean isEANSearch(){ return isEANSearch; }
  
  /**
   * Get omnichannel type string.
   *
   * @return the string
   */
  public String getOmnichannelType(){
    return !chkNull(SharedPrefManager.getOmnichannelType(), AppConstants.OMNI_TYPE_BOTH).equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH) ? SharedPrefManager.getOmnichannelType() : omniType.toLowerCase().trim();
  }
  
  /**
   * Get omnichannel upload type string.
   *
   * @return the string
   */
  public String getOmnichannelUploadType(){ return omniUploadType; }
  
  /**
   * Api call.
   *
   * @param action the action
   */
  public void apiCall(String action){
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    final boolean isRelease = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_RELEASE);
    final boolean isVerifyForDecode = isUpload && isAllowDecode && !isStatusVerified();
    action = isVerifyForDecode ? AppConstants.SESSION_ACTION_VERIFY_STATUS : action;
    if(isInternetConnected(context, false, isUpload || isRelease)){
      try{
        if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.OMNICHANNEL_TYPE, getOmnichannelType());
        requestParams.put(ParamConstants.ORDER_NO, chkNull(selProd.getOrderNo(), ""));
        if(isEANSearch)
          requestParams.put(ParamConstants.EAN, isEANSearch ? chkNull(selProd.getEan(), "") : "");
        if(isVerifyForDecode) requestParams.put(ParamConstants.IS_VERIFY_DECODE, isVerifyForDecode);
        requestParams.put(ParamConstants.SESSION_TYPE, sessionType.name());
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        if(isUpload){
          showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          new Handler().post(new Runnable(){
            @Override
            public void run(){
              try{
                JSONArray js = new JSONArray();
                for(Inventory inventory : inventoryDao.getAllInventoryData(sessionType.getValue())){
                  if(inventory != null && (!inventory.isUploaded /*|| inventory.isDecoded()*/)){
                    JSONObject dataobject = inventory.toJson(context);
                    if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                      js.put(dataobject);
                  }
                }
                requestParams.put(ParamConstants.ITEMS, js);
                callWebService(isVerifyForDecode ? URLConstants.VERIFY_OMNICHANNEL_FOR_DECODE : URLConstants.UPLOAD_OMNICHANNEL, requestParams, /*args,*/false, getString(isVerifyForDecode ? R.string.progress_msg_verifying_data : R.string.progress_msg_uploading_data), !isUpload);
              }
              catch(Exception e){
                e.printStackTrace();
                hideProgressDialog();
              }
            }
          });
          //add Other Parameters for bulk uploading
        }
        else
          callWebService(isUpload ? URLConstants.UPLOAD_OMNICHANNEL : isRelease ? URLConstants.RELEASE_OMNICHANNEL : URLConstants.SET_SESSION, requestParams, false, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload);
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
      }
    }
  }
  
  @Override
  public void onBackPressed(){
    final MultiQtyModel multiQtyModel = isEANSearch ? productDao.getTotalOmniEanCounts(selProd.getEan()) : productDao.getTotalOmniOrderCounts(selProd.getOrderNo());
    final int decoded = multiQtyModel != null ? chkNull(multiQtyModel.decoded, 0) : 0;
    final int found = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
    final int total = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
    final int required = multiQtyModel != null ? chkNull(multiQtyModel.required, 0) : 0;
    context.showCustomAlertDialog("_", found > 0 /*&& total > 0*/ && required > 0 ? String.format(getString(R.string.msg_omni_back_3), found + "/" + required/*total*/, omniType.toLowerCase()) : String.format(getString(R.string.msg_omni_back_1), omniType.toLowerCase()), R.string.btn_no, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        inventoryDao.deleteInventory(sessionType.getValue());
        popBackStack();
      }
    }, R.string.btn_yes, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        apiCall(AppConstants.SESSION_ACTION_RELEASE);
      }
    });
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    final MultiQtyModel multiQtyModel = isEANSearch ? productDao.getTotalOmniEanCounts(selProd.getEan()) : productDao.getTotalOmniOrderCounts(selProd.getOrderNo());
    final int found = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
    final int decoded = multiQtyModel != null ? chkNull(multiQtyModel.decoded, 0) : 0;
    final int total = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
    final int required = multiQtyModel != null ? chkNull(multiQtyModel.required, 0) : 0;
    ((MainActivity) context).lockDrawer(found > 0 /*&& total > 0*/ && required > 0);
    
    binding.txtOmnichannelOrdernoEan.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_order_no_qty), selProd.getOrderNo(), "" + found, "" + required), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    if(binding != null && binding.btnUpload != null/* && binding.btnSwipeUpload != null*/){
      final boolean isShowUpload = required > 0 && /*total > 0 &&*/ (omniUploadType.equalsIgnoreCase(AppConstants.OMNI_UPLOAD_TYPE_NIL) || (omniUploadType.equalsIgnoreCase(AppConstants.OMNI_UPLOAD_TYPE_PARTIAL) && found > 0) || (omniUploadType.equalsIgnoreCase(AppConstants.OMNI_UPLOAD_TYPE_COMPLETE) && found /*== total && total*/ >= required));
      binding.btnUpload.setVisibility(isShowUpload ? View.VISIBLE : View.GONE);
      //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && isShowUpload ? View.VISIBLE : View.GONE);
    }
    listProducts.clear();
    List<ProductModel> listOrderProducts = isEANSearch ? productDao.getOmniEANProducts(selProd.getEan()) : productDao.getOmniOrderProducts(selProd.getOrderNo());
    if(isNonEmpty(listOrderProducts)) listProducts.addAll(listOrderProducts);
    if(binding != null && binding.listOmnichannelSearchDetails != null && binding.listOmnichannelSearchDetails.getAdapter() != null)
      ((RecyclerView.Adapter) binding.listOmnichannelSearchDetails.getAdapter()).notifyDataSetChanged();
  }
  
  @Override
  public void onDestroyView(){
    super.onDestroyView();
    if(isAllowDecode){
      mainViewModel.getIsDecodeOn().removeObservers(getViewLifecycleOwner());
      mainViewModel.getIsDecodeDone().removeObservers(getViewLifecycleOwner());
      inventoryDao.getNonVerifiedPickCount(sessionType.getValue()).removeObservers(getViewLifecycleOwner());
    }
  }
  
  @Override
  public void onDetach(){
    ((MainActivity) context).lockDrawer(false);
    super.onDetach();
  }
  
  /**
   * Check reader connected boolean.
   *
   * @return the boolean
   */
  protected boolean checkReaderConnected(){
    if(!isAllowDecode) return false;
    if(mainViewModel.isReaderConnected()) return true;
    else{
      context.showCustomAlertDialog("", String.format(getString(R.string.err_reader_connection), getTypeCharCode()), getString(R.string.btn_ok), (dialogInterface, i) -> {
        if(((MainActivity) context).isReaderConnected())
          mainViewModel.performDecoding(inventoryDao.getAllNonDecodedInventoryData(sessionType.getValue()), sessionType);
        else mainViewModel.checkAndConnectReader();
      });
      return false;
    }
  }
  
  private void performDecoding(){
    if(isAllowDecode && isStatusVerified()){
      final String decodeAlertMsg = chkNull(SharedPrefManager.getDecodeAlertMsg(), getString(R.string.msg_omni_decode)).trim();
      context.showCustomAlertDialog(isNonEmpty(decodeAlertMsg) && decodeAlertMsg.contains(";") ? decodeAlertMsg.split(";")[0] : "_", isNonEmpty(decodeAlertMsg) && decodeAlertMsg.contains(";") ? decodeAlertMsg.split(";")[1] : decodeAlertMsg, R.string.btn_ok, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialogInterface, int i){
          if(checkReaderConnected())
            mainViewModel.performDecoding(inventoryDao.getAllNonDecodedInventoryData(sessionType.getValue()), sessionType);
        }
      });
    }
  }
  
  private void insertExistingData(final RFIDSession sessionObject){
    if(isNonEmpty(listProducts) && sessionObject != null){
      for(ProductModel model : listProducts){
        if(model != null && isNonEmpty(model.getPickedEPCs()) && sessionObject != null){
          new Handler().post(new Runnable(){
            @Override
            public void run(){
              try{
                final JSONArray jsonTags = new JSONArray(model.getPickedEPCs());
                if(jsonTags != null && jsonTags.length() > 0){
                  showLog("jsonTags", "" + jsonTags.length());
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
                        inventory.newEpc = !epc.startsWith("0") ? "0" + epc.substring(1) : epc;
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
    }
  }
  
  private boolean isAllDecoded(){
    final MultiQtyModel multiQtyModel = isEANSearch ? productDao.getTotalOmniEanCounts(selProd.getEan()) : productDao.getTotalOmniOrderCounts(selProd.getOrderNo());
    final int decoded = multiQtyModel != null ? chkNull(multiQtyModel.decoded, 0) : 0;
    final int found = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
    final int total = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
    final int required = multiQtyModel != null ? chkNull(multiQtyModel.required, 0) : 0;
    return inventoryDao.getInventorySize(sessionType.getValue()) >= required && inventoryDao.getNonDecodedCount(sessionType.getValue()) == 0;
  }
  
  @Override
  public void onPause(){
    super.onPause();
    if(isAllowDecode && isStatusVerified()) mainViewModel.onPause();
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      if(isAllowDecode && isStatusVerified()) new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
        @Override
        public void run(){
          if(isAllowDecode && isStatusVerified()) mainViewModel.getReaderUHFInstance(sessionType);
          else mainViewModel.onResume(sessionType);
        }
      }, 500);
      updateLists();
    }
  }
  
  @Override
  public void onDestroy(){
    super.onDestroy();
    //if(isAllowDecode && isStatusVerified()) mainViewModel.onDestroy();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.RELEASE_OMNICHANNEL:
          if(isSuccess){
            if(isEANSearch) productDao.deleteEan(selProd.getEan());
            else productDao.deleteOrder(selProd.getOrderNo());
            inventoryDao.deleteInventory(sessionType.getValue());
            popBackStack();
          }
          break;
        case URLConstants.VERIFY_OMNICHANNEL_FOR_DECODE:
          if(isSuccess){
            SharedPrefManager.setDecodeAlertMsg(extractString(jsonResponse, ParamConstants.MESSAGE, SharedPrefManager.getDecodeAlertMsg()));
            successBeep();
            productDao.updateVerifyStatus();
            inventoryDao.updateUploaded(sessionType.getValue());
            isStatusVerified = true;
            performDecoding();
          }
          updateLists();
          break;
        case URLConstants.UPLOAD_OMNICHANNEL:
          if(isSuccess){
            final boolean isVerifyForDecode = extractBoolean(jsonRequest, ParamConstants.IS_VERIFY_DECODE, false);
            showLog("isVerifyForDecode_Upload", "" + isVerifyForDecode);
            if(isVerifyForDecode){
              SharedPrefManager.setDecodeAlertMsg(extractString(jsonResponse, ParamConstants.MESSAGE, SharedPrefManager.getDecodeAlertMsg()));
              successBeep();
              productDao.updateVerifyStatus();
              inventoryDao.updateUploaded(sessionType.getValue());
              isStatusVerified = true;
              performDecoding();
            }
            else{
              final boolean isAllDecoded = !isAllowDecode || isAllDecoded();
              context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), isAllDecoded);
              //delete Uploaded Records
              new Handler().post(new Runnable(){
                @Override
                public void run(){
                  if(isAllDecoded) inventoryDao.deleteInventory(sessionType.getValue());
                  if(isAllowDecode){
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
                      if(!isAllDecoded) inventoryDao.updateUploaded(sessionType.getValue(), tids);
                      uploadInventoryDao.updateUploaded(sessionType.getValue(), tids);
                    }
                    uploadInventoryDao.deleteUploaded();
                  }
                  updateLists();
                }
              });
            }
          }
          updateLists();
          break;
        case URLConstants.GET_MAPPED_EAN:
          if(isSuccess){
            final String ean = extractString(jsonResponse, ParamConstants.EAN, extractString(jsonRequest, ParamConstants.EAN));
            final String mappedEan = extractString(jsonResponse, ParamConstants.MAPPED_EAN, "");
            if(args == null) args = getArguments();
            if(args == null) args = new Bundle();
            //use this Mapped EAN for Search/Encode
            args.putString(ParamConstants.MAPPED_EAN, mappedEan);
            if(context != null && !context.isFinishing() && context instanceof MainActivity)
              ((MainActivity) context).checkReaderConnection(new OmniChannelStartFragment(), args);
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
}