package com.itek.retail.ui.decoding;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT_PATTERN;
import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.errorBeep;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.getSampleJSON;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUse24LengthTIDForUpload;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.EncodingHistoryAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.databinding.FragmentDecodingStartBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.LabelValues;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.customviews.InputView;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.fifo.SearchFIFOFragment;
import com.itek.retail.ui.search.fifo.SearchFIFOStartFragment;
import com.itek.retail.ui.search.productsearch.ProductDetailsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Decoding start fragment.
 */
public class DecodingStartFragment extends RFIDSessionFragment{
  
  int activeUsers = 0, sessionValidTill = 48, target = -1;
  InventoryDao inventoryDao;
  UploadInventoryDao uploadInventoryDao;
  Boolean is1stSessionStart = false;
  private DecodingViewModel mViewModel;
  private FragmentDecodingStartBinding binding;
  private List<EanQty> listDecodedEans = new ArrayList<>(0);
  private String decodeType;
  private Inventory pickedData = null;
  private Timer uploadTimer;
  
  /**
   * Instantiates a new Decoding start fragment.
   */
  public DecodingStartFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    mainViewModel = ((MainActivity) context).getRfidViewModel();
    inventoryDao = AppDatabase.getInventoryDao(context);
    uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(DecodingViewModel.class);
    binding = FragmentDecodingStartBinding.inflate(inflater, container, false);
    
    activeUsers = extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2);
    sessionValidTill = extractInt(getArguments(), AppConstants.SESSION_VALID_TILL, 48);
    target = extractInt(getArguments(), AppConstants.TARGET, -1);
    
    decodeType = extractString(getArguments(), ParamConstants.DECODE_TYPE, AppConstants.DECODE);
    
    setActiveUsers(activeUsers);
    hideKeyboard();
    
    binding.seekEncodingStartTarget.setThumb(getResources().getDrawable(R.drawable.ic_target));
    
    binding.seekEncodingStartTarget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
        //code here
        //int encoded = AppCommonMethods.parseInt(binding.txtEncodingStartScore.getText().toString());
        // int encoded = AppCommonMethods.parseInt(binding.ctwInventoryStart.getScore().toString());
        int total = progress * 10;
        //binding.txtEncodingStartScoreTotal.setText("" + total);
        if(binding.ctwInventoryStart != null) binding.ctwInventoryStart.setTotal(total);
        //showLog("encoded", "" + encoded);
        showLog("target", "" + total);
       /* binding.progressEncodingStart.setProgress(encoded * 100 / (total > 0 ? total : 1));
        binding.progressEncodingStart.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
        binding.divEncodingStartScore.setVisibility(*//**//*isOneToOneRelation &&*//**//* total > 0 ? View.VISIBLE : View.GONE);*/
        binding.lblDecodingTotal.setText(total > 0 ? R.string.lbl_decode_target : R.string.lbl_total);
      }
      
      @Override
      public void onStartTrackingTouch(SeekBar seekBar){
        //unused method (Default Overridden)
      }
      
      @Override
      public void onStopTrackingTouch(SeekBar seekBar){
        //unused method (Default Overridden)
      }
    });
    
    //Power bar
    binding.llSeekbarPower.setupProgress(mainViewModel);
    
    binding.llBtnStart.setOnClickListener(v -> {
      context.dismissCustomAlertDialog();
      showLog("llBtnStart_isProcessOn", "" + isProcessOn());
      final Boolean isSessionOn = mainViewModel.getIsSessionOn().getValue();
      if(pickedData != null) pickedData = null;
      if(!chkNotNullTrue(isSessionOn) && sessionObject == null)
        apiCall(AppConstants.SESSION_ACTION_START);
      else if(chkNotNullTrue(isSessionOn) && sessionObject != null && !isProcessOn() && checkReaderConnected()){
        mainViewModel.performPick("", binding.llSeekbarPower.getProgress());
      }
    });
    
    binding.listDecodingStartHistory.setAdapter(new EncodingHistoryAdapter((MainActivity) context, DecodingStartFragment.this, listDecodedEans));
    binding.listDecodingStartHistory.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
    
    if(/*isOneToOneRelation &&*/ !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && sessionObject == null && chkNull(target, -1) >= 0){
      setTarget(target);
      is1stSessionStart = null;
      binding.llBtnStart.performClick();
    }
    else if(/*isOneToOneRelation &&*/ !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && sessionObject == null && chkNull(target, -1) < 0){
      binding.seekEncodingStartTarget.setProgress(AppCommonMethods.parseInt(binding.ctwInventoryStart.getTotal()) / 10);
    }
    else if(/*isOneToOneRelation &&*/ !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && sessionObject != null && chkNull(target, -1) < 0){
      final String totEncCount = sessionObject != null ? sessionObject.total : chkZero(binding.ctwInventoryStart.getTotal(), "0");
      showLog("totEncCount", "" + totEncCount);
      //binding.txtEncodingStartScoreTotal.setText(totEncCount);
      binding.ctwInventoryStart.setTotal(totEncCount);
      int total = -1;
      try{
        total = Integer.parseInt(totEncCount);
      }
      catch(Exception e){ e.printStackTrace(); }
      if(sessionObject != null && total >= 0)
        binding.seekEncodingStartTarget.setProgress(total / 10);
      else setTarget(target);
    }
    
    //if(true) mainViewModel.setReaderPower(7);//Set Power
    binding.header.imgConfigSync.setVisibility(View.VISIBLE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_config);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(chkNotNullTrue(mainViewModel.getIsProcessOn().getValue())) return;
        //binding.llEncodingConfig.setVisibility(binding.llEncodingConfig.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        binding.llSeekbarPower.setVisibility(binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
      }
    });
    
    callScheduler();
    
    return binding.getRoot();
  }
  
  @Override
  protected void onReaderPowerChanged(Integer power){
    binding.llSeekbarPower.updateReaderPower(mainViewModel, power);
  }
  
  private void callScheduler(){
    if(!SharedPrefManager.getBoolean(ParamConstants.IS_ENC_UPLOAD_BY_SCHEDULER, AppCommonMethods.isUseSchedulerForWritenTagUpload))
      return;
    if(uploadTimer == null){
      uploadTimer = new Timer();
      uploadTimer.schedule(new TimerTask(){
        @Override
        public void run(){
          //showLog("scheduler", "uploadTimer");
          if(isInternetConnected(context, false, false) && (SharedPrefManager.getBoolean(ParamConstants.IS_ENC_UPLOAD_BY_WHILE_PROCESSING, AppCommonMethods.isAllowBackgroundWritenTagUploadWhileProcessing) || !isProcessOn()))
            context.uploadWrittenInventoryTags(true);
        }
      }, 1000, 15000);
    }
  }
  
  /**
   * Set target.
   *
   * @param target the target
   */
  public void setTarget(int target){
    //if(!isOneToOneRelation) return;
    try{
      target = sessionObject != null ? AppCommonMethods.parseInt(sessionObject.total) : chkNull(DecodingStartFragment.this.target, -1) >= 0 ? DecodingStartFragment.this.target : target;
      if(sessionObject == null && chkNull(target, -1) >= 0){
        //binding.txtEncodingStartScoreTotal.setText("" + target);
        binding.ctwInventoryStart.setTotal(target);
        binding.seekEncodingStartTarget.setProgress(target / 10);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Set active users.
   */
  public void setActiveUsers(final int activeUsers){
    int oldActiveUsers = -2;
    try{
      oldActiveUsers = AppCommonMethods.parseInt(binding.header.btnActiveDevices.getText().toString(), "-2");
    }
    catch(NumberFormatException e){ e.printStackTrace(); }
    final int activeCount = chkNull(activeUsers, -1) >= 0 ? activeUsers : oldActiveUsers;
    binding.header.flActiveDevices.setVisibility(activeUsers >= -1 ? View.VISIBLE : View.GONE);
    binding.header.btnActiveDevices.setSelected(true);
    binding.header.btnActiveDevices.setText(activeCount >= 0 ? "" + activeCount : "");
  }
  
  @Override
  protected void setObservers(){
    super.setObservers();
  }
  
  void setUploadCountObserver(){ setUploadCountObserver(false); }
  
  void setUploadCountObserver(final boolean isRemove){
    if(inventoryDao != null && sessionObject != null){
      inventoryDao.getUploadedCount(sessionObject.sessionId).removeObservers(getViewLifecycleOwner());
      if(!isRemove)
        inventoryDao.getUploadedCount(sessionObject.sessionId).observe(getViewLifecycleOwner(), new Observer<MultiQtyModel>(){
          @Override
          public void onChanged(MultiQtyModel multiQtyModel){
            if(multiQtyModel != null && multiQtyModel.found != null && multiQtyModel.total != null){
              if(multiQtyModel.total > 0 && binding.txtDecodeUploadedCount.getVisibility() != View.VISIBLE)
                binding.txtDecodeUploadedCount.setVisibility(View.VISIBLE);
              binding.txtDecodeUploadedCount.setText(String.format(getString(R.string.txt_enc_count_uploaded), "" + multiQtyModel.found, "" + multiQtyModel.total));
            }
          }
        });
    }
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    setUploadCountObserver();
  }
  
  @Override
  protected void onReaderConfigured(){
    super.onReaderConfigured();
  }
  
  @Override
  public void apiCall(String action){
    apiCall(action, null);
  }
  
  @Override
  public void apiCall(String action, Bundle args){
    showLog("API", action);
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    if(isUpload && isInternetConnected(context, false, isUpload)){
      new Handler().post(new Runnable(){
        @Override
        public void run(){
          try{
            //if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
            JSONObject requestParams = new JSONObject();
            requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
            requestParams.put(ParamConstants.TYPE, decodeType);//AppConstants.DECODE);
            requestParams.put(ParamConstants.TARGET, sessionObject != null ? sessionObject.total : chkZero(binding.ctwInventoryStart.getTotal(), "-"));
            requestParams.put(ParamConstants.ACTION, action);
            requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
            if(sessionObject != null){
              requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
              if(isUpload){
                JSONArray js = new JSONArray();
                for(Inventory inventory : inventoryDao.getAllInventoryData(sessionObject.sessionId)){
                  if(inventory != null){
                    JSONObject dataobject = inventory.toJson(context);
                    if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                      js.put(dataobject);
                  }
                }
                requestParams.put(ParamConstants.ITEMS, js);
                //add Other Parameters for bulk uploading
              }
            }
            callWebService(isUpload ? URLConstants.UPLOAD_DECODING : URLConstants.SET_SESSION, requestParams, args, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload, false);
            //callWebService(isUpload ? URLConstants.UPLOAD_ENCODE : URLConstants.SET_SESSION, requestParams, args, isUpload, isUpload ? null : "", !isUpload, false);
          }
          catch(JSONException e){
            e.printStackTrace();
            hideProgressDialog();
          }
        }
      });
    }
    else setSessionAction(action, null, null, activeUsers, target, args);
  }
  
  @Override
  protected void isSessionOnChanged(final Boolean isSessionOn){
    if(isSessionOn == null) return;
    if(chkNotNullTrue(isSessionOn) && is1stSessionStart){
      is1stSessionStart = false;
      setUploadCountObserver();
      binding.llBtnStart.performClick();
    }
    binding.seekEncodingStartTarget.setEnabled(!isSessionOn);
    updateView();
    
    /*final boolean isProcessOn = isProcessOn();
    binding.llBtnStart.setEnabled(!isProcessOn);
    showLog("isSessionOn_isProcessOn", isSessionOn + "_" + isProcessOn);
    binding.seekEncodingStartTarget.setEnabled(!isSessionOn);
    binding.scanEncodingStartBarcode.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(isSessionOn && !isProcessOn);
    binding.scanEncodingStartTid.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(isSessionOn && !isProcessOn);
    binding.divFifo.setEnabled(binding.divFifo.getVisibility() == View.VISIBLE && !isProcessOn);*/
  }
  
  /**
   * Is session on boolean.
   *
   * @return the boolean
   */
  public boolean isSessionOn(){
    return chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
  }
  
  @Override
  protected void isPickOnChanged(Boolean isPickOn){
    super.isPickOnChanged(isPickOn);
    if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
    if(isPickOn == null) return;
    else{
      /*final boolean isProcessOn = isProcessOn();
      binding.llBtnStart.setEnabled(!isProcessOn);
      binding.scanEncodingStartBarcode.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(null,chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
      binding.scanEncodingStartTid.setIsProcessOn(isProcessOn);//.setIsViewControlEnabled(null,chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && !isProcessOn);
      binding.divFifo.setEnabled(binding.divFifo.getVisibility() == View.VISIBLE && !isProcessOn);*/
      updateView();
    }
  }
  
  @Override
  protected void onPickDataChanged(Inventory pickData){
    super.onPickDataChanged(pickData);
    if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
    if(pickData != null && pickData.epc.length() > 2){
      pickData.newEpc = "0" + pickData.epc.substring(1);
      if(pickData.newEpc.trim().equalsIgnoreCase(pickData.epc.trim()))
        context.showCustomErrDialog(R.string.err_decoding_already_decoded);
        //call api to check if correct tag is being sold, if category is matched
        //callValidateSerialForDecodeAPI(pickData);
      else if(SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PRODUCT_DETAILS_BEFORE_DECODING, AppCommonMethods.isCheckProductDetailsBeforeDecoding))
        callProductDetailsAPI(pickData);
      else startDecoding(pickData);
    }
  }
  
  void callValidateProductAgeForDecodeAPI(final Inventory tagInfo){
    if(!isTopInStack()) return;
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.TYPE, decodeType);
      if(sessionObject != null) jsonRequest.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
      jsonRequest.put(ParamConstants.SESSION_TYPE, getSessionType().name());
      jsonRequest.put(ParamConstants.EAN, tagInfo != null ? chkNull(tagInfo.ean, "") : "");
      jsonRequest.put(ParamConstants.EPC, tagInfo != null ? chkNull(tagInfo.epc, "") : "");
      jsonRequest.put(ParamConstants.TID, tagInfo != null ? chkNull(tagInfo.tid, "") : "");
      callWebService(URLConstants.VALIDATE_PRODUCT_AGE_FOR_DECODE, jsonRequest, getString(R.string.progress_msg_getting_data));
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  void callProductDetailsAPI(final Inventory tagInfo){
    if(!isTopInStack()) return;
    pickedData = tagInfo;
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.EAN, tagInfo != null ? chkNull(tagInfo.ean, "") : "");
      jsonRequest.put(ParamConstants.EPC, tagInfo != null ? chkNull(tagInfo.epc, "") : "");
      jsonRequest.put(ParamConstants.TID, tagInfo != null ? chkNull(tagInfo.tid, "") : "");
      final String productInfoUrl = getProductInfoUrl();
      if(productInfoUrl.equalsIgnoreCase(URLConstants.GET_PRODUCT_INFO_BY_SKU)){
        JSONArray js = new JSONArray();
        js.put(jsonRequest.get(ParamConstants.EAN));
        jsonRequest.put(ParamConstants.ITEMS, js);
      }
      callWebService(productInfoUrl, jsonRequest, getString(R.string.progress_msg_getting_data));
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Start encoding.
   *
   * @param pickedTag the picked tag
   */
  private void startDecoding(final Inventory pickedTag){
    final String sgtin = pickedTag.newEpc;
    boolean access_to_write = context.epcEncoderDecoder.isValidHeader(sgtin);
    if(access_to_write){
      /*if(isNonEmpty(userRemark)){
        Bundle extras = new Bundle();
        extras.putString(ParamConstants.REMARK,userRemark);
        mainViewModel.performDecoding(pickedTag,extras);
      }
      else*/
      mainViewModel.performDecoding(pickedTag);
    }
    else{
      hideProgressDialog();
      context.showCustomErrDialog(R.string.err_decoding_write_fail);
      //CANNOT WRITE
    }
  }
  
  @Override
  protected void isDecodeOnChanged(Boolean isDecodeOn){
    super.isDecodeOnChanged(isDecodeOn);
    if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) return;
    if(isDecodeOn == null) return;
    else{
      showLog("isDecodeOn", "" + isDecodeOn);
      updateView();
      if(chkNotNullFalse(isDecodeOn)){
        if(sessionObject != null && chkNotNullTrue(mainViewModel.getIsDecodeDone().getValue())){//Case Tag Write Success
          showShortToast(R.string.success_decoding);
          // long total = Long.parseLong(chkNull(binding.txtEncodingStartScoreTotal.getText().toString().replace("-", ""), "0"));
          long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
          if(total > 0 && getSize() == total)
            context.showCustomSuccessDialog(R.string.success_dec_target_achieved);
          //apiCall(AppConstants.SESSION_ACTION_UPLOAD);
          mainViewModel.getIsDecodeDone().postValue(false);
          if(pickedData != null) pickedData = null;
          if(isInternetConnected(context, false, false) && (SharedPrefManager.getBoolean(ParamConstants.IS_ENC_UPLOAD_BY_WHILE_PROCESSING, AppCommonMethods.isAllowBackgroundWritenTagUploadWhileProcessing) || !isProcessOn()) && (SharedPrefManager.getBoolean(ParamConstants.IS_ENC_UPLOAD_BY_BOTH_IMMEDIATE_SCHEDULER, AppCommonMethods.isAllowBothImmediateUploadAndUploadSchedulerForWrittenTags) || uploadTimer == null))
            context.uploadWrittenInventoryTags(true);
        }
      }
    }
  }
  
  private void updateView(){
    final boolean isProcessOn = isProcessOn();
    binding.llBtnStart.setEnabled(!isProcessOn);
    binding.llSeekbarPower.setEnabled(!isProcessOn);
    binding.llSeekbarPower.setVisibility(!isProcessOn && binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
  }
  
  @Override
  protected void onTriggerPressed(){
    showLog(this.getClass().getSimpleName(), "onTriggerPressed");
    binding.llBtnStart.performClick();
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    if(chkNull(size, 0) > 0 && sessionObject != null && !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))
      mainViewModel.startSession(sessionObject, false);
    listDecodedEans.clear();
    if(sessionObject != null){
      listDecodedEans.addAll(inventoryDao.getEncodedEans(sessionObject.sessionId));
    }
    if(binding != null){
      if(binding.listDecodingStartHistory != null && binding.listDecodingStartHistory.getAdapter() != null && binding.listDecodingStartHistory.getAdapter() instanceof RecyclerView.Adapter)
        ((RecyclerView.Adapter) binding.listDecodingStartHistory.getAdapter()).notifyDataSetChanged();
      //binding.txtEncodingStartScore.setText("" + chkNull(size, 0));
      if(binding.ctwInventoryStart != null) binding.ctwInventoryStart.setScore(chkNull(size, 0));
      long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
      /*final boolean isEncCount = chkNull(size, 0) > 0;
      binding.progressEncodingStart.setVisibility(isEncCount && total > 0 ? View.VISIBLE : View.GONE);
      double per = total > 0 ? (chkNull(size, 0) * 100) / total : 0;
      int percentage = (int) per;
      binding.progressEncodingStart.setProgress(percentage);*/
      // binding.txtEncodingStartScore.setTextColor(ContextCompat.getColor(context, total > 0 && size >= total ? R.color.txtGreen : R.color.txt_number));
      binding.ctwInventoryStart.setTextColorScore(total > 0 && size >= total ? R.color.txtGreen : R.color.txt_number);
    }
  }
  
  @Override
  public void onPause(){
    super.onPause();
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      if(sessionObject != null) apiCall(AppConstants.SESSION_ACTION_RESUME);
    }
  }
  
  @Override
  public void onDestroyView(){
    if(uploadTimer != null){
      uploadTimer.cancel();
      uploadTimer = null;
    }
    setUploadCountObserver(true);
    sessionObject = null;
    super.onDestroyView();
  }
  
  /**
   * Set session action.
   *
   * @param action          the action
   * @param sessionId       the session id
   * @param sessionTime     the session time
   * @param activeUserCount the active user count
   * @param target          the target
   * @param args            the args
   */
  void setSessionAction(String action, String sessionId, String sessionTime, Integer activeUserCount, int target, Bundle args){
    setActiveUsers(activeUserCount != null ? activeUserCount.intValue() : -2);
    final String totEncCount = sessionObject != null ? sessionObject.total : chkZero(binding.ctwInventoryStart.getTotal(), "0");
    showLog("totEncCount", "" + totEncCount);
    //binding.txtEncodingStartScoreTotal.setText(totEncCount);
    if(binding.ctwInventoryStart != null) binding.ctwInventoryStart.setTotal(totEncCount);
    int total = -1;
    try{
      total = Integer.parseInt(totEncCount);
    }
    catch(Exception e){ e.printStackTrace(); }
    if(sessionObject != null && total >= 0) binding.seekEncodingStartTarget.setProgress(total / 10);
    else setTarget(target);
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.total = totEncCount;
      sessionObject.sessionType = AppCommonMethods.SessionType.DECODING.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.DECODE.getValue();
      try{
        JSONObject jsonExtras = new JSONObject();
        jsonExtras.put(ParamConstants.TYPE, decodeType);//AppConstants.DECODE);
        jsonExtras.put(ParamConstants.TARGET, sessionObject != null ? sessionObject.total : chkZero(binding.ctwInventoryStart.getTotal(), "-"));
        sessionObject.extras = jsonExtras.toString();
      }
      catch(Exception e){ e.printStackTrace(); }
      sessionObject.userId = SharedPrefManager.getUserID();
      Calendar cc = Calendar.getInstance();
      if(chkNull(sessionTime, "").length() > 0 && sessionTime.matches(DATE_TIME_FORMAT_PATTERN)){
        try{
          cc.setTime(new SimpleDateFormat(DATE_TIME_FORMAT).parse(sessionTime));
        }
        catch(Exception e){ e.printStackTrace(); }
      }
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, chkZero(sessionValidTill, 48));
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = chkNull(sessionId, mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc));
      setSessionObject(sessionObject);
      is1stSessionStart = is1stSessionStart != null;
      mainViewModel.startSession(sessionObject, false);
    }
    else if(sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_RESUME))
      mainViewModel.startSession(sessionObject, false);
    else if(sessionObject != null && !action.matches("(?i)(" + AppConstants.SESSION_ACTION_START + "|" + AppConstants.SESSION_ACTION_RESUME + ")")){
      mainViewModel.stopSession(sessionObject, action.matches("(?i)(" + AppConstants.SESSION_ACTION_DISCARD + ")"));
      if(args != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_PAUSE)){
        showLog("allowBtnClick111", "" + allowBtnClick);
        final Serializable obj = extractSerializable(args, MenuModel.class);
        final MenuModel menuModel = obj != null && obj instanceof MenuModel ? (MenuModel) obj : null;
        final String ean = extractString(args, AppConstants.EAN, "");
        if(menuModel != null)
          handleFragmentRedirection(menuModel == null ? new ProductDetailsFragment() : null, menuModel, args);
        else if(isNonEmpty(ean)){
          try{
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put(ParamConstants.EAN, ean);
            jsonRequest.put(ParamConstants.EPC, "");
            jsonRequest.put(ParamConstants.TID, "");
            Bundle args1 = new Bundle();
            final MenuModel menuSearchDetails = AppDatabase.getMenuDao(context).getMenuByCode(AppConstants.MENU_CODE_SER_PROD);
            args1.putString(AppConstants.EAN, ean);
            args1.putString(AppConstants.TITLE, "Product Details");
            args1.putString(AppConstants.TITLE_LOGO_URL, menuSearchDetails != null ? menuSearchDetails.getScreenImageUrl() : "");
            args1.putInt(AppConstants.TITLE_LOGO_RES_ID, menuSearchDetails != null ? menuSearchDetails.getScreenIconId(context) : R.drawable.ic_ser_prod);
            final String productInfoUrl = getProductInfoUrl();
            if(productInfoUrl.equalsIgnoreCase(URLConstants.GET_PRODUCT_INFO_BY_SKU)){
              JSONArray js = new JSONArray();
              js.put(jsonRequest.get(ParamConstants.EAN));
              jsonRequest.put(ParamConstants.ITEMS, js);
            }
            callWebService(productInfoUrl, jsonRequest, args1, getString(R.string.progress_msg_getting_data), true);
          }
          catch(Exception e){ e.printStackTrace(); }
        }
      }
      else if(action.equalsIgnoreCase(AppConstants.SESSION_ACTION_SAVE)){
        context.showCustomAlertDialog("", getString(R.string.success_session_save), true, true, getString(R.string.btn_ok), (dialogInterface, i) -> popBackStack());
      }
      else context.popBackStack();
    }
  }
  
  void checkFIFOValidationBeforeStartDecoding(final ProductModel productModel){
    if(SharedPrefManager.getBoolean(ParamConstants.IS_FIFO_VALIDATION_DECODE, false) && isNonEmpty(SharedPrefManager.getString(ParamConstants.FIFO_VALIDATION_MODE, ""))){//TODO confirm mode boolean or string
      final ArrayList<String> listFIFOValidationCriteriaKeys = SharedPrefManager.getStringArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "KEYS", new ArrayList<String>(0));
      if(isNullOrEmpty(listFIFOValidationCriteriaKeys) || validateCriteria(listFIFOValidationCriteriaKeys, productModel)){
        callValidateProductAgeForDecodeAPI(pickedData);
        return;
      }
    }
    startDecoding(pickedData);
  }
  
  private boolean validateCriteria(ArrayList<String> listFIFOValidationCriteriaKeys, ProductModel productModel){
    if(isNullOrEmpty(listFIFOValidationCriteriaKeys) || productModel == null || isNullOrEmpty(productModel.getDisplayData(context)))
      return false;
    final JSONArray productDataJsonArray = productModel.getDisplayData(context);
    for(String key1 : listFIFOValidationCriteriaKeys){
      final ArrayList<String> listFIFOValidationCriteriaValues = SharedPrefManager.getStringArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "_" + key1);
      if(isNonEmpty(listFIFOValidationCriteriaValues)){
        for(int i = 0; i < productDataJsonArray.length(); i++){
          try{
            JSONObject jsonObject = productDataJsonArray.getJSONObject(i);
            if(isNullOrEmpty(jsonObject)) continue;
            String label = "";
            String value = "";
            if(!jsonObject.has(ParamConstants.LABEL) || !jsonObject.has(ParamConstants.VALUE)){
              label = jsonObject.keys().toString();
              value = jsonObject.getString(label);
            }
            else{
              final LabelValues lblValues = AppCommonMethods.getGSON().fromJson(jsonObject.toString(), LabelValues.class);
              if(lblValues != null && isNonEmpty(lblValues.getLabel()) && isNonEmpty(lblValues.getValue())){
                label = lblValues.getLabel();
                value = lblValues.getValue();
              }
            }
            if(isNonEmpty(label) && isNonEmpty(value) && chkNull(key1, productModel.getCategoryLbl(context)).equalsIgnoreCase(label)){
              if(listFIFOValidationCriteriaValues.contains(value.toUpperCase())) return true;
              else break;
            }
          }
          catch(JSONException e){
            e.printStackTrace();
          }
        }
      }
    }
    return false;
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.SET_SESSION:
          final String action = extractString(jsonRequest, ParamConstants.ACTION, sessionObject == null ? AppConstants.SESSION_ACTION_START : AppConstants.SESSION_ACTION_STOP);
          String sessionId = null, sessionTime = null;
          showLog("allowBtnClick111", "" + allowBtnClick);
          if(isSuccess && jsonResponse != null){
            sessionId = extractString(jsonResponse, ParamConstants.SESSION_ID);
            sessionTime = extractString(jsonResponse, ParamConstants.SESSION_TIME);
            sessionValidTill = extractInt(jsonResponse, ParamConstants.SESSION_VALID_TILL, 48);
            activeUsers = extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, activeUsers);
            target = extractInt(jsonResponse, ParamConstants.TARGET, target);
          }
          setSessionAction(action, sessionId, sessionTime, activeUsers, target, args);
          break;
        case URLConstants.GET_PRODUCT_INFO:
        case URLConstants.GET_PRODUCT_INFO_BY_SKU:
          ProductModel productModel = getProductModelFromResponse(jsonRequest, jsonResponse);
          if(productModel != null){
            final String err = extractString(jsonResponse, ParamConstants.ERROR, extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.MESSAGE, "")));
            final String errMsg = isNullOrEmpty(chkNull(err, "").replaceFirst("(?i)(true|false)", "").trim()) ? "" : err;
            if(isSuccess && isNullOrEmpty(errMsg))
              checkFIFOValidationBeforeStartDecoding(productModel);//startDecoding(pickedData);
            else{
              errorBeep();
              context.showCustomConfirmDialog((chkNull(errMsg, "") + "\n" + getString(R.string.msg_confirm_decode)).trim(), getString(R.string.decode), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                  startDecoding(pickedData);
                }
              });
            }
          }
          break;
        case URLConstants.VALIDATE_PRODUCT_AGE_FOR_DECODE:
          if(isSuccess) startDecoding(pickedData);
          else{
            final String err = extractString(jsonResponse, ParamConstants.ERROR, extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.MESSAGE, "")));
            final String errMsg = isNullOrEmpty(chkNull(err, "").replaceFirst("(?i)(true|false)", "").trim()) ? "" : err;
            if(isNullOrEmpty(SharedPrefManager.getString(ParamConstants.FIFO_VALIDATION_MODE, ""))) startDecoding(pickedData);
            else if(SharedPrefManager.getString(ParamConstants.FIFO_VALIDATION_MODE, "").matches("(?i)(Strict)")){
              context.showCustomAlertDialog("", chkNull(errMsg, getString(R.string.err_fifo_decode)),getString(R.string.btn_ok),null,getString(R.string.menu_fifo), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                  Bundle args1=chkNull(args,new Bundle());
                  args1.putString(ParamConstants.EAN,pickedData.ean);
                  mainViewModel.stopSession(sessionObject,false);
                  context.loadFragment(new SearchFIFOFragment(),args1);
                }
              }); //please give a default message
              return;
            }
            else if(SharedPrefManager.getString(ParamConstants.FIFO_VALIDATION_MODE, "").matches("(?i)(Flexible)")){
              InputView inputView = new InputView(context);
              //inputView.setPadding(0,context.getResources().getDimensionPixelSize(R.dimen.dp_5),0,0);
              inputView.setMandatory(true);
              inputView.setLabel(R.string.lbl_remark);
              inputView.setHint(R.string.hint_remark);
              inputView.setMinLen(1);
              inputView.setMaxLen(50);
              final ArrayList<String> listReasons =  SharedPrefManager.getStringArrayList(ParamConstants.FIFO_OVERRIDE_REASONS);
              if(isNonEmpty(listReasons)) inputView.setAdapter(listReasons,true);
              inputView.setValidationRegex(AppConstants.REGEX_ANY_BARCODE_BIG);
              inputView.setButtonClick(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                  pickedData.remark = inputView.getText().trim();
                  //check how remark is saved & uploaded
                  startDecoding(pickedData);
                }
              });
              errorBeep();
              context.showCustomAlertDialog("", (chkNull(errMsg, getString(R.string.err_fifo_decode)) + "\n" + getString(R.string.msg_mandatory_remark)).trim(), inputView, getString(R.string.btn_submit),null, getString(R.string.btn_cancel),null, getString(R.string.menu_fifo), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                  Bundle args1=chkNull(args,new Bundle());
                  args1.putString(ParamConstants.EAN,pickedData.ean);
                  mainViewModel.stopSession(sessionObject,false);
                  context.loadFragment(new SearchFIFOFragment(),args1);
                }
              });
            }
          }
          break;
        case URLConstants.UPLOAD_ENCODE:
        case URLConstants.UPLOAD_DECODING:
          final JSONArray js = AppCommonMethods.isUpdateUploadStatusBasedOnTID ? extractJSONArray(jsonRequest, ParamConstants.ITEMS) : null;
          final Set<String> tids = new HashSet<String>(0);
          if(isNonEmpty(js)){
            for(int i = 0; i < js.length(); i++){
              final String tid = extractString(js.getJSONObject(i), ParamConstants.TID, "").trim();
              if(isNonEmpty(tid))
                tids.add(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid);
            }
          }
          if(isSuccess){
            //            final long total = Long.parseLong(chkNull(binding.txtEncodingStartScoreTotal.getText().toString().replace("-", ""), "0"));
            //            String dialogMsg = extractString(jsonResponse, ParamConstants.MESSAGE, getString(total > 0 && getSize() == total ? R.string.success_enc_target_achieved : R.string.success_encoding));
            //            if(total > 0 && getSize() == total && !dialogMsg.toLowerCase().contains("target") && !dialogMsg.toLowerCase().contains("achieve"))
            //              dialogMsg = dialogMsg + "\n" + getString(R.string.success_enc_target_achieved);
            //context.showCustomSuccessDialog(dialogMsg);
            if(isNonEmpty(tids)){
              inventoryDao.updateUploaded(sessionObject.sessionId, tids);
              uploadInventoryDao.updateUploaded(sessionObject.sessionId, tids);
            }
            else{
              inventoryDao.updateUploaded(sessionObject.sessionId);
              uploadInventoryDao.updateUploaded(sessionObject.sessionId);
            }
            uploadInventoryDao.deleteUploaded();
          }
          else{
            if(isNonEmpty(tids)){
              inventoryDao.updateUploadRetryCount(sessionObject.sessionId, tids);
              uploadInventoryDao.updateUploadRetryCount(sessionObject.sessionId, tids);
            }
            else{
              inventoryDao.updateUploadRetryCount(sessionObject.sessionId);
              uploadInventoryDao.updateUploadRetryCount(sessionObject.sessionId);
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