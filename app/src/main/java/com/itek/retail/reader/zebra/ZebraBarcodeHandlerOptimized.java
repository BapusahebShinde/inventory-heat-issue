package com.itek.retail.reader.zebra;

import static android.provider.ContactsContract.Intents.Insert.ACTION;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.getScanRegex;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.reader.BarcodeHandler;
import com.itek.retail.reader.MainReaderRepository;
import com.itek.retail.ui.encoding.EncodingStartFragment;
import com.itek.retail.ui.home.MainActivity;

import java.util.Set;

/**
 * The Zebra barcode handler optimized.
 */
public class ZebraBarcodeHandlerOptimized extends BarcodeHandler{
  
  private static final String EXTRA_PROFILE_NAME = "Profile1";
  // DataWedge Extras
  //private static final String EXTRA_GET_VERSION_INFO = "com.symbol.datawedge.api.GET_VERSION_INFO";
  private static final String EXTRA_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";
  private static final String EXTRA_KEY_APPLICATION_NAME = "com.symbol.datawedge.api.APPLICATION_NAME";
  private static final String EXTRA_KEY_NOTIFICATION_TYPE = "com.symbol.datawedge.api.NOTIFICATION_TYPE";
  private static final String EXTRA_SOFT_SCAN_TRIGGER = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER";
  private static final String EXTRA_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION";
  private static final String EXTRA_REGISTER_NOTIFICATION = "com.symbol.datawedge.api.REGISTER_FOR_NOTIFICATION";
  private static final String EXTRA_UNREGISTER_NOTIFICATION = "com.symbol.datawedge.api.UNREGISTER_FOR_NOTIFICATION";
  private static final String EXTRA_KEY_INPUT = "com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN";
  private static final String EXTRA_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";
  private static final String EXTRA_RESULT_NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
  private static final String EXTRA_KEY_VALUE_SCANNER_STATUS = "SCANNER_STATUS";
  private static final String EXTRA_KEY_VALUE_PROFILE_SWITCH = "PROFILE_SWITCH";
  private static final String EXTRA_KEY_VALUE_CONFIGURATION_UPDATE = "CONFIGURATION_UPDATE";
  private static final String EXTRA_KEY_VALUE_NOTIFICATION_STATUS = "STATUS";
  private static final String EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME = "PROFILE_NAME";
  private static final String EXTRA_SEND_RESULT = "SEND_RESULT";
  private static final String EXTRA_PLUGIN_RESUME = "RESUME_PLUGIN";
  private static final String EXTRA_PLUGIN_SUSPEND = "SUSPEND_PLUGIN";
  private static final String EXTRA_EMPTY = "";
  //private static final String EXTRA_RESULT_GET_VERSION_INFO = "com.symbol.datawedge.api.RESULT_GET_VERSION_INFO";
  private static final String EXTRA_RESULT = "RESULT";
  private static final String EXTRA_RESULT_INFO = "RESULT_INFO";
  private static final String EXTRA_COMMAND = "COMMAND";
  // DataWedge Actions
  private static final String ACTION_DATAWEDGE = "com.symbol.datawedge.api.ACTION";
  private static final String ACTION_RESULT_NOTIFICATION = "com.symbol.datawedge.api.NOTIFICATION_ACTION";
  private static final String ACTION_RESULT = "com.symbol.datawedge.api.RESULT_ACTION";
  
  private boolean isDataScanned = false;
  private boolean isScanning = false;
  private boolean isBarcodeDataFound = false;
  private boolean isSuspended = false;
  
  private boolean bRequestSendResult = false;
  private boolean okToSuspend = false;
  private boolean isReceiverRegistered = false;
  
  private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver(){
    @Override
    public void onReceive(Context context, Intent intent){
      String action = intent.getAction();
      
      showLog("onReceive_Action", action);
      if(action.equals("com.zebra.datacapture1.ACTION")){
        //  Received a barcode scan
        showLog("isScanning & isDataScanned1",isScanning+" & "+isDataScanned+" & "+isBarcodeDataFound);
        if(isScanning && !isDataScanned && !isBarcodeDataFound){
          isDataScanned = true;
          isScanning = false;
          isBarcodeDataFound = true;
          showLog("isScanning & isDataScanned2",isScanning+" & "+isDataScanned+" & "+isBarcodeDataFound);
          showLog("ENC_Barcode","displayScanResult");
          try{ displayScanResult(intent, "via Broadcast"); }
          catch(Exception e){
            e.printStackTrace();
          }
        }
      }
      else if(action.equals(ACTION_RESULT)){
        // Register to receive the result code
        if((intent.hasExtra(EXTRA_RESULT)) && (intent.hasExtra(EXTRA_COMMAND))){
          String command = intent.getStringExtra(EXTRA_COMMAND);
          String result = intent.getStringExtra(EXTRA_RESULT);
          String info = "";
          //showLog("onReceive_command", command);
          //showLog("onReceive_result", result);
          if(intent.hasExtra(EXTRA_RESULT_INFO)){
            Bundle result_info = intent.getBundleExtra(EXTRA_RESULT_INFO);
            Set<String> keys = result_info.keySet();
            for(String key : keys){
              Object object = result_info.get(key);
              if(object instanceof String){
                info += key + ": " + object + "\n";
              }
              else if(object instanceof String[]){
                String[] codes = (String[]) object;
                for(String code : codes){
                  info += key + ": " + code + "\n";
                }
              }
            }
            //showLog("onReceive_info", info);
            /*showLog(this.getClass().getSimpleName(), "Command: "+command+"\n" +
              "Result: " +result+"\n" +
              "Result Info: " + info + "\n");*/
            
          }
        }
      }
      // Register for scanner change notification
      else if(action.equals(ACTION_RESULT_NOTIFICATION)){
        if(intent.hasExtra(EXTRA_RESULT_NOTIFICATION)){
          Bundle extras = intent.getBundleExtra(EXTRA_RESULT_NOTIFICATION);
          String notificationType = extras.getString(EXTRA_RESULT_NOTIFICATION_TYPE);
          if(notificationType != null){
            //showLog("onReceive_notificationType", notificationType);
            switch(notificationType){
              case EXTRA_KEY_VALUE_SCANNER_STATUS:
                // Change in scanner status occurred
                final String status = extras.getString(EXTRA_KEY_VALUE_NOTIFICATION_STATUS);
                showLog("onReceive_Scanner status", status + "_" + isDataScanned + "_" + isScanning);
                okToSuspend = status.equalsIgnoreCase("WAITING") || status.equalsIgnoreCase("SCANNING");
                if(!status.equalsIgnoreCase("Scanning")){
                  //showLog("isDataScanned_isScanning", "" + isDataScanned + "_" + isScanning);
                  /*if(!isDataScanned && isScanning){//chkNotNullTrue(isBarcodeOn.getValue())){
                    if(!status.equalsIgnoreCase("IDLE"))
                      ((MainActivity) ZebraBarcodeHandlerOptimized.this.context).showCustomErrDialog(String.format(context.getString(R.string.err_scan_fail), getTypeCharCode(),scanType));
                    isBarcodeOn.postValue(false);
                    AppCommonMethods.logInFile(context, sessionType.name() + "_BARCODE_SCAN_STOP");
                    ((ZebraRepository) mainReaderRepository).updateTriggerMode(false);
                    //}
                    isScanning = false;
                    isBarcodeOn.postValue(false);
                    setProgressMessage(false);
                  }
                  if(isScanning && !status.matches("(?i)(SCANNING|IDLE)")){
                    isScanning = false;
                    isBarcodeOn.postValue(false);
                    setProgressMessage(false);
                  }
                  if(isDataScanned && status.equalsIgnoreCase("DISABLED")) isDataScanned = false;
                  isScanning = false;
                  isBarcodeOn.postValue(false);*/
                }
                else{
                  //setProgressMessage("Scanning Barcode...", true);
                  //isBarcodeOn.postValue(true);
                  //isBarcodeOn.setValue(true);
                  isScanning = true;
                }
                break;
              
              case EXTRA_KEY_VALUE_PROFILE_SWITCH:
                
                // Received change in profile
                // For future enhancement
                break;
              
              case EXTRA_KEY_VALUE_CONFIGURATION_UPDATE:
                
                // Configuration change occurred
                // For future enhancement
                break;
              default:
                break;
            }
          }
        }
      }
    }
  };
  
  /**
   * Instantiates a new Zebra barcode handler optimized.
   *
   * @param context              the context
   * @param mainReaderRepository the main reader repository
   * @param sessionType          the session type
   * @param isInit               the is init
   */
  public ZebraBarcodeHandlerOptimized(CommonActivity context, MainReaderRepository mainReaderRepository, AppCommonMethods.SessionType sessionType, boolean isInit){
    super(context, mainReaderRepository, sessionType, isInit);
    //setProgressMessage("Initializing Barcode...", true);
    Bundle b = new Bundle();
    b.putString(EXTRA_KEY_APPLICATION_NAME, context.getPackageName());
    b.putString(EXTRA_KEY_NOTIFICATION_TYPE, "SCANNER_STATUS");     // register for changes in scanner status
    sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_REGISTER_NOTIFICATION, b);
    if(!isReceiverRegistered) registerReceivers();
    //sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_GET_VERSION_INFO, EXTRA_EMPTY);
    if(!SharedPrefManager.getIsProfileSet()) //new Handler().postDelayed(() -> {
      CreateProfile();
   // }, 0);//200);
    else setProgressMessage(false);
  }
  
  @Override
  protected void onTimerFinish(){
    if(chkNotNullTrue(isBarcodeOn.getValue()) && !isDataScanned){
      //if(okToSuspend)
        //sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_KEY_INPUT, EXTRA_PLUGIN_SUSPEND);
      isScanning = false;
      stopScan();
      //isBarcodeOn.postValue(false);
      //isBarcodeOn.setValue(false);
     // ((ZebraRepository) mainReaderRepository).updateTriggerMode(false);
    }
  }
  
  @Override
  public void init(){
    //setProgressMessage("Initializing Barcode...", true);
    Bundle b = new Bundle();
    b.putString(EXTRA_KEY_APPLICATION_NAME, context.getPackageName());
    b.putString(EXTRA_KEY_NOTIFICATION_TYPE, "SCANNER_STATUS");     // register for changes in scanner status
    sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_REGISTER_NOTIFICATION, b);
    if(!isReceiverRegistered) registerReceivers();
    //sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_GET_VERSION_INFO, EXTRA_EMPTY);
    if(!SharedPrefManager.getIsProfileSet()) //new Handler().postDelayed(() -> {
      CreateProfile();
    //}, 0);//200);
    else setProgressMessage(false);
  }

  /*public void setProgressMessage(boolean isShowDialog){
    setProgressMessage("", isShowDialog);
  }*/

  /*protected void setProgressMessage(String message, boolean isShowDialog){
    if(mainReaderRepository != null) mainReaderRepository.setProgressMessage(message, isShowDialog);
  }*/
  
  private void CreateProfile(){
    
    try{
      String profileName = EXTRA_PROFILE_NAME;
      
      sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_CREATE_PROFILE, profileName);
      
      // Configure created profile to apply to this app
      Bundle profileConfig = new Bundle();
      profileConfig.putString("PROFILE_NAME", EXTRA_PROFILE_NAME);
      profileConfig.putString("PROFILE_ENABLED", "true");
      profileConfig.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");  // Create profile if it does not exist
      
      // Configure barcode input plugin
      Bundle barcodeConfig = new Bundle();
      barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
      barcodeConfig.putString("RESET_CONFIG", "true"); //  This is the default
      
      Bundle barcodeProps = new Bundle();
      /*barcodeProps.putString("scanner_selection", "auto");
      barcodeProps.putString("scanner_input_enabled", "true");
      barcodeProps.putString("decoder_code128", "true");
      barcodeProps.putString("decoder_code39", "true");
      barcodeProps.putString("decoder_ean13", "true");
      barcodeProps.putString("decoder_upca", "true");*/
      
      barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
      
      profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);
      
      // Associate profile with this app
      Bundle appConfig = new Bundle();
      appConfig.putString("PACKAGE_NAME", context.getPackageName());
      appConfig.putStringArray("ACTIVITY_LIST", new String[]{"*"});
      profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
      profileConfig.remove("PLUGIN_CONFIG");
      
      // Apply configs
      // Use SET_CONFIG: http://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
      sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);
      
      // Configure intent output for captured data to be sent to this app
      Bundle intentConfig = new Bundle();
      intentConfig.putString("PLUGIN_NAME", "INTENT");
      intentConfig.putString("RESET_CONFIG", "true");
      Bundle intentProps = new Bundle();
      intentProps.putString("intent_output_enabled", "true");
      intentProps.putString("intent_action", "com.zebra.datacapture1.ACTION");
      intentProps.putString("intent_delivery", "2");
      intentConfig.putBundle("PARAM_LIST", intentProps);
      profileConfig.putBundle("PLUGIN_CONFIG", intentConfig);
      sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);
      
      // Place "barcodeConfig" bundle within main "profileConfig" bundle
      profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);
      
      // Create APP_LIST bundle to associate app with profile
      profileConfig.putParcelableArray("APP_LIST", new Bundle[]{appConfig});
      sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);
      
      //registerReceivers();
      
//      new Handler().postDelayed(new Runnable(){
//        @Override
//        public void run(){
          updateProfile();
//        }
//      }, 0);//100);
      
    }
    catch(Exception e){
      setProgressMessage(false);
      e.printStackTrace();
    }
    
  }
  
  private void updateProfile(){
    try{
      // Main bundle properties
      Bundle profileConfig = new Bundle();
      profileConfig.putString("PROFILE_NAME", EXTRA_PROFILE_NAME);
      profileConfig.putString("PROFILE_ENABLED", "true");
      profileConfig.putString("CONFIG_MODE", "UPDATE");  // Update specified settings in profile
      
      // PLUGIN_CONFIG bundle properties
      Bundle barcodeConfig = new Bundle();
      barcodeConfig.putString("PLUGIN_NAME", "BARCODE");
      barcodeConfig.putString("RESET_CONFIG", "true");
      
      // PARAM_LIST bundle properties
      Bundle barcodeProps = new Bundle();
      barcodeProps.putString("scanner_selection", "auto");
      barcodeProps.putString("scanner_input_enabled", "true");
      barcodeProps.putString("decoder_code128", "true");
      barcodeProps.putString("decoder_code39", "true");
      barcodeProps.putString("decoder_ean13", "true");
      barcodeProps.putString("decoder_upca", "true");
      
      // Bundle "barcodeProps" within bundle "barcodeConfig"
      barcodeConfig.putBundle("PARAM_LIST", barcodeProps);
      // Place "barcodeConfig" bundle within main "profileConfig" bundle
      profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig);
      sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SET_CONFIG, profileConfig);
      SharedPrefManager.setIsProfileSet(true);
    }
    catch(Exception e){ e.printStackTrace(); }
    setProgressMessage(false);
  }
  
  /**
   * Start scanning.
   */
  
  // Use SOFT_SCAN_TRIGGER: http://techdocs.zebra.com/datawedge/latest/guide/api/softscantrigger/
  @Override
  public void startScan(String type){
    if(!isScanning && !chkNotNullTrue(isBarcodeOn.getValue())){
      isBarcodeDataFound = false;
      isDataScanned = false;
      isScanning = false;
      scanType = chkNull(type, "Barcode");
      //barcodeData.setValue("");
      showLog("onReceive_Scanner Start Scan", "" + isDataScanned + "_" + isScanning);
      try{
        AppCommonMethods.logInFile(context, sessionType.name(),"-----------------------------------\n"  + "_BARCODE_SCAN_START");
        showLog(sessionType.name(),"BARCODE_SCAN_START");
        //sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_KEY_INPUT, EXTRA_PLUGIN_RESUME);
        sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_SOFT_SCAN_TRIGGER, "TOGGLE_SCANNING");
        scanTimer.start();
        //setProgressMessage("Scanning Barcode...", true);
        isBarcodeOn.setValue(true);
      }
      catch(Exception e){ e.printStackTrace(); }
    }
  }
  
  @Override
  public void stopScan(){
    if(scanTimer != null) scanTimer.cancel();
    isBarcodeOn.postValue(false);
    //setProgressMessage(false);
    //((ZebraRepository) mainReaderRepository).updateTriggerMode(false);
  }
  
  /**
   * Register receivers.
   */
  
  private void registerReceivers(){
    try{
      showLog("registerReceivers", "registerReceivers()");
      IntentFilter filter = new IntentFilter();
      filter.addAction(ACTION_RESULT_NOTIFICATION);   // for notification result
      filter.addAction(ACTION_RESULT);                // for error code result
      //filter.addCategory(Intent.CATEGORY_DEFAULT);    // needed to get version info
      // register to received broadcasts via DataWedge scanning
      filter.addAction("com.zebra.datacapture1.ACTION");
      filter.addAction("com.zebra.datacapture1.service.ACTION");
      context.registerReceiver(myBroadcastReceiver, filter);
      isReceiverRegistered = true;
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Un register scanner status.
   */
  
  private void unRegisterScannerStatus(){
    try{
      if(isReceiverRegistered){
        context.unregisterReceiver(myBroadcastReceiver);
        isReceiverRegistered = false;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    try{
      //showLog(this.getClass().getSimpleName(), "unRegisterScannerStatus()");
      Bundle b = new Bundle();
      b.putString(EXTRA_KEY_APPLICATION_NAME, context.getPackageName());
      b.putString(EXTRA_KEY_NOTIFICATION_TYPE, EXTRA_KEY_VALUE_SCANNER_STATUS);
      Intent i = new Intent();
      i.setAction(ACTION);
      i.putExtra(EXTRA_UNREGISTER_NOTIFICATION, b);
      context.sendBroadcast(i);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Display scan result.
   *
   * @param initiatingIntent the initiating intent
   * @param howDataReceived  the how data received
   */
  private void displayScanResult(Intent initiatingIntent, String howDataReceived){
    try{
      // store decoded data
      String decodedData = initiatingIntent.getExtras().getString("com.symbol.datawedge.data_string", "");
      // store decoder type
      //String decodedLabelType = initiatingIntent.getExtras().getString("com.symbol.datawedge.label_type", "");
      //showLog("decodedData", decodedData);
      //showLog("decodedLabelType", decodedLabelType);
      if(isNonEmpty(decodedData)){
        showLog("ENCODING_BARCODE_SCAN_RESULT",decodedData);
        //isDataScanned = true;
        /*if(scanTimer != null) scanTimer.cancel();
        isScanning = false;
        isBarcodeOn.setValue(false);*/
        updateData(decodedData.trim());
        //setProgressMessage(false);
        //if(okToSuspend)
          //sendDataWedgeIntentWithExtra(ACTION_DATAWEDGE, EXTRA_KEY_INPUT, EXTRA_PLUGIN_SUSPEND);
        stopScan();
        //if(scanTimer != null) scanTimer.cancel();
        //isScanning = false;
        //isBarcodeOn.postValue(false);
        //isBarcodeOn.setValue(false);
        /*setProgressMessage(false);
        ((ZebraRepository) mainReaderRepository).updateTriggerMode(false);*/
        //updateData(decodedData);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Send data wedge intent with extra.
   *
   * @param action   the action
   * @param extraKey the extra key
   * @param extras   the extras
   */
  private void sendDataWedgeIntentWithExtra(String action, String extraKey, Bundle extras){
    Intent dwIntent = new Intent();
    dwIntent.setAction(action);
    dwIntent.putExtra(extraKey, extras);
    if(bRequestSendResult) dwIntent.putExtra(EXTRA_SEND_RESULT, "true");
    context.sendBroadcast(dwIntent);
  }
  
  /**
   * Send data wedge intent with extra.
   *
   * @param action     the action
   * @param extraKey   the extra key
   * @param extraValue the extra value
   */
  private void sendDataWedgeIntentWithExtra(String action, String extraKey, String extraValue){
    Intent dwIntent = new Intent();
    dwIntent.setAction(action);
    dwIntent.putExtra(extraKey, extraValue);
    if(bRequestSendResult) dwIntent.putExtra(EXTRA_SEND_RESULT, "true");
    context.sendBroadcast(dwIntent);
  }
  
  /**
   * Update data.
   *
   * @param result the resultf
   */
  private void updateData(final String result){
    //showLog("_barcodeData_Result", result != null ? !result.isEmpty() ? result : "empty" : "result is null");
    //isDataScanned = isNonEmpty(result);
    if(isNonEmpty(result)){
      if(!chkNull(result, "").matches(getScanRegex(scanType))){//AppCommonMethods.getEanRegex())){
        context.showCustomErrDialog(String.format(context.getString(R.string.err_invalid_scan), getTypeCharCode(), scanType, result));
        //if possible, pass session Type & check Std/NonStd in case of Encoding
      }
      else{
        barcodeData.setValue(result);
        if(context!=null && !context.isFinishing() && context instanceof MainActivity){
          CommonFragment fragment = ((MainActivity) context).getTopFragment();
          if(fragment!=null && fragment instanceof EncodingStartFragment)
            ((EncodingStartFragment)fragment).setBarcode(result + (scanType.replace("Barcode", "").trim().length() > 0 && chkNull(result, "").trim().length() > 0 ? ";;" + scanType : ""));
          else
            barcodeData.setValue(result + (scanType.replace("Barcode", "").trim().length() > 0 && chkNull(result, "").trim().length() > 0 ? ";;" + scanType : ""));
        }
//        else
          barcodeData.setValue(!scanType.matches("(?i)(^.*(Barcode|TID|RFID QR).*$)")?chkNull(result, "").trim():AppCommonMethods.getLeftZeroReplacedString(context,chkNull(result, "")) + (scanType.replace("Barcode", "").trim().length() > 0 && chkNull(result, "").trim().length() > 0 ? ";;" + scanType : ""));
        //barcodeData.postValue(result + (scanType.replace("Barcode", "").trim().length() > 0 && chkNull(result, "").trim().length() > 0 ? ";;" + scanType : ""));
        //barcodeData.postValue(result);
      }
      AppCommonMethods.logInFile(context, sessionType.name(),"_BARCODE_SCAN_RESULT (" + result + ")");
      showLog(sessionType.name(),"_BARCODE_SCAN_RESULT (" + result + ")");
    }
    /*if(scanTimer != null) scanTimer.cancel();
    isScanning = false;
    //isBarcodeOn.postValue(false);
    isBarcodeOn.setValue(false);
    setProgressMessage(false);
    ((ZebraRepository) mainReaderRepository).updateTriggerMode(false);*/
    //setProgressMessage(false);
    showLog("onReceive_Scanner Update Data", chkNull(result, "none") + "_" + isDataScanned + "_" + isScanning);
  }
  
  /**
   * On resume.
   */
  @Override
  public void onResume(){
    //showLog("ZBHO", "onResume");
    try{
      registerReceivers();
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * On pause.
   */
  @Override
  public void onPause(){
    //showLog("ZBHO", "onPause");
    //isBarcodeOn.postValue(false);
    //isBarcodeOn.setValue(false);
    stopScan();
    unRegisterScannerStatus();
  }
  
  /**
   * On destroy.
   */
  @Override
  public void onDestroy(){
    //showLog("ZBHO", "onDestroy");
    stopScan();
    unRegisterScannerStatus();
  }
}
