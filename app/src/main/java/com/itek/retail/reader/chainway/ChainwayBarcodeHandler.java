package com.itek.retail.reader.chainway;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isDCApp;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.reader.BarcodeHandler;
import com.itek.retail.reader.MainReaderRepository;
import com.itek.retail.ui.home.MainActivity;
import com.rscja.barcode.BarcodeUtility;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.interfaces.ConnectionStatus;

import java.util.Optional;
import java.util.Set;

/**
 * The Chainway barcode handler.
 */
public class ChainwayBarcodeHandler extends BarcodeHandler{
  private static final String FILE_TAG_LOG = "DeviceAPI_APP";
  
  private BarcodeUtility barcodeUtility = BarcodeUtility.getInstance();
  private BarcodeDataReceiver barcodeDataReceiver = null;
  
 /* private static ActivityResultLauncher<Intent> bluetoothResultLauncher;
  private RFIDWithUHFBLE reader = null;
  private boolean isConnected = false;
  private Handler handler;
  private BluetoothAdapter mBluetoothAdapter = null;
  private Boolean isBluetoothReader = false;*/
  
  /**
   * Instantiates a new Chainway barcode handler.
   *
   * @param context              the context
   * @param mainReaderRepository the main reader repository
   * @param sessionType          the session type
   * @param isInit               the is init
   */
  public ChainwayBarcodeHandler(CommonActivity context, MainReaderRepository mainReaderRepository, AppCommonMethods.SessionType sessionType, boolean isInit){
    super(context, mainReaderRepository, sessionType, isInit);
    /*if(!Build.MANUFACTURER.equalsIgnoreCase(AppCommonMethods.DeviceType.CHAINWAY.name()) || SharedPrefManager.getIsDeviceBluetoothDependent()){
      SharedPrefManager.setIsDeviceBluetoothDependent(true);
      isBluetoothReader = true;
      registerBluetoothEvent();
      assignBluetoothReader();
    }*/
    /*this.context = context;
    this.mainReaderRepository = mainReaderRepository;
    this.sessionType = sessionType;
    try{
      if(isInit) init();
    }catch(Exception e){
      e.printStackTrace();
      showLog(e.getClass().getSimpleName(), "" + e.getMessage());
    }*/
  }
  
  @Override
  protected void onTimerFinish(){
    if(isBarcodeOn.getValue()){
      if(barcodeDataReceiver != null){
        Intent barcodeData = new Intent();
        barcodeData.putExtra("data", "");
        barcodeData.putExtra("SCAN_STATE", "failuer");
        barcodeDataReceiver.onReceive(context, barcodeData);
      }
      /*else*/
      stopScan();
    }
  }
  
  /**
   * Init.
   */
  @Override
  public void init(){
    new InitTask().execute();
  }
  
  /**
   * Start scan.
   */
  @Override
  public void startScan(String type){
    try {
      if (barcodeUtility != null) {
        scanType = chkNull(type, "Barcode");
        showLog(TAG, "Scan" + scanType);
        barcodeUtility.startScan(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION);
        isBarcodeOn.postValue(true);
        AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcode_startScan->true");
        //setProgressMessage("Scanning " + scanType + "...", true);
        scanTimer.start();
      } else {
        isBarcodeOn.postValue(false);
        setProgressMessage(false);
      }
    }catch (Exception e) {e.printStackTrace();
      AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, e.getMessage());
    }
  }
  
  /**
   * Stop scan.
   */
  
  @Override
  public void stopScan(){
    try {
      if (barcodeUtility != null) {
        scanTimer.cancel();
        showLog(TAG, "stopScan");
        setProgressMessage(false);
        isBarcodeOn.postValue(false);
        barcodeUtility.stopScan(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION);
        AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcode_stopScan->true");
      } else {
        scanTimer.cancel();
        isBarcodeOn.postValue(false);
        setProgressMessage(false);
      }
    }catch (Exception e) {e.printStackTrace();
      AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, e.getMessage());
    }
  }
  
  @Override
  public void onResume(){
    try {
      if (barcodeUtility != null) {
        if (!isDCApp && !isInitiated) {
          barcodeUtility.open(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION);
          isInitiated = true;
          AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcode_open->true");
        }
        if (barcodeDataReceiver == null) {
          barcodeDataReceiver = new BarcodeDataReceiver();
          IntentFilter intentFilter = new IntentFilter();
          intentFilter.addAction("com.scanner.broadcast");
          context.registerReceiver(barcodeDataReceiver, intentFilter);
          AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcode_registerReceiver->true");
        }
      }
    }catch (Exception e){e.printStackTrace();
      AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, e.getMessage());
    }
  }
  
  @Override
  public void onPause(){
    try {
      stopScan();
      if (barcodeUtility != null) {
        if (!isDCApp && isInitiated) {
          barcodeUtility.close(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION);//关闭2D
          isInitiated = false;
          AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcode_close->true");
        }
        if (barcodeDataReceiver != null) {
          context.unregisterReceiver(barcodeDataReceiver);
          barcodeDataReceiver = null;
          AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcode_unregisterReceiver->true");
        }
      }
      //if(barcodeUtility != null) barcodeUtility.close(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION);
    } catch (Exception e) {e.printStackTrace();
      AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, e.getMessage());
    }
  }
  
  /**
   * On destroy.
   */
  public void onDestroy(){
    try {
    if(barcodeUtility != null){
      setProgressMessage(false);
      isBarcodeOn.postValue(false);
      if(!isDCApp && isInitiated) {
        barcodeUtility.close(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION);//关闭2D
        isInitiated=false;
        AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcode_close->true");
      }
      resetBarcodeSettings();
      if(barcodeDataReceiver != null){
        context.unregisterReceiver(barcodeDataReceiver);
        barcodeDataReceiver = null;
        AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcode_unregisterReceiver->true");
      }
    }
    } catch (Exception e) {e.printStackTrace();
      AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, e.getMessage());
    }
  }
  
  private void resetBarcodeSettings(){
    if(barcodeUtility != null && context!=null){
      barcodeUtility.setReleaseScan(context,true);
      barcodeUtility.setOutputMode(context,0);
      barcodeUtility.setScanFailureBroadcast(context,false);
      barcodeUtility.enableEnter(context,true);
      //0 => default, 1=> ASCII, 2=> GB2312, 3=> UTF8, 4=> Unicode, 5=> GBK, 6=> GB18030, 7=> SHIFT_JIS, 8=> AutoDetect
      barcodeUtility.setBarcodeEncodingFormat(context,0);
      //barcodeUtility.close(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION);
    }
  }
  
  /**
   * Set progress message.
   *
   * @param isShowDialog the is show dialog
   */
  /*public void setProgressMessage(boolean isShowDialog){
    setProgressMessage("", isShowDialog);
  }*/
  
  /**
   * Set progress message.
   *
   * @param message      the message
   * @param isShowDialog the is show dialog
   */
  /*protected void setProgressMessage(String message, boolean isShowDialog){
    if(mainReaderRepository != null) mainReaderRepository.setProgressMessage(message, isShowDialog);
  }*/
  
  /**
   * The Init task.
   */
  public class InitTask extends AsyncTask<String, Integer, Boolean>{
    
    @Override
    protected Boolean doInBackground(String... params){
      // TODO Auto-generated method stub
      boolean reuslt = false;
      if(barcodeUtility != null){
        //0 => Scan Content on cursor, 1=> Clipboard, 2=> BroadcastReceiver, 3=> Keyboard input, 4=> Overlay cursor position
        barcodeUtility.setOutputMode(context, 2);//设置广播接收数据
        barcodeUtility.setScanResultBroadcast(context, "com.scanner.broadcast", "data");//设置接收数据的广播
        if(!isDCApp) {
          barcodeUtility.open(context, BarcodeUtility.ModuleType.AUTOMATIC_ADAPTATION);//打开2D
          isInitiated = true;
          AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcode_open->"+isInitiated);
        }
        barcodeUtility.setReleaseScan(context, false);//设置松开扫描按键，不停止扫描
        //barcodeUtility.setScanOutTime(context,3000);
        barcodeUtility.setScanFailureBroadcast(context, true);//扫描失败也发送广播
        barcodeUtility.enableContinuousScan(context, false);//关闭键盘助手连续扫描
        barcodeUtility.enablePlayFailureSound(context, false);//关闭键盘助手 扫描失败的声音
        //关闭键盘助手 扫描成功的声音
        barcodeUtility.enableEnter(context, false);//关闭回车//false
        //0 => default, 1=> ASCII, 2=> GB2312, 3=> UTF8, 4=> Unicode, 5=> GBK, 6=> GB18030, 7=> SHIFT_JIS, 8=> AutoDetect
        barcodeUtility.setBarcodeEncodingFormat(context, 0);//1
        if(barcodeDataReceiver == null){
          barcodeDataReceiver = new BarcodeDataReceiver();
          IntentFilter intentFilter = new IntentFilter();
          intentFilter.addAction("com.scanner.broadcast");
          context.registerReceiver(barcodeDataReceiver, intentFilter);
          AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcode_registerReceiver->true");
          reuslt = true;
        }
      }
      return reuslt;
    }
    
    @Override
    protected void onPostExecute(Boolean result){
      super.onPostExecute(result);
      AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcode_init->"+result);
      if(result){
        showLog("INIT", "SUCCESS");
      }
      else{
        showLog("INIT", "FAIL");
      }
      
    }
    
    @Override
    protected void onPreExecute(){
      // TODO Auto-generated method stub
      super.onPreExecute();
    }
  }
  
  /**
   * The Barcode data receiver.
   */
  protected class BarcodeDataReceiver extends BroadcastReceiver{
    
    @Override
    public void onReceive(Context context1, Intent intent){
      final String scannedBarcodeData = intent.getStringExtra("data");
      final String status = intent.getStringExtra("SCAN_STATE");
      showLog("status", chkNull(status, "-"));
      showLog("_barcodeData_Result", chkNull(scannedBarcodeData, "fail"));
      AppCommonMethods.writeReaderLog(context, "CHAINWAY", "CHAINWAY_BARCODE", FILE_TAG_LOG, "barcodeData_scan_result->"+(!chkNull(status,"-").equalsIgnoreCase("failuer") && !chkNull(scannedBarcodeData, "fail").equalsIgnoreCase("fail")));
      /*if(status != null && (status.equals("cancel"))){
        barcodeData.postValue("");
      }
      else */
      if(status != null && (status.equals("failuer"))){
        scanTimer.cancel();
        if(isBarcodeOn.getValue()){
          //((MainActivity) ChainwayBarcodeHandler.this.context).showCustomErrDialog(String.format(context.getString(R.string.err_scan_fail), getTypeCharCode(), scanType));
          setProgressMessage(false);
          isBarcodeOn.postValue(false);
        }
      }
      else{
        scanTimer.cancel();
        if(isBarcodeOn.getValue()){
          isBarcodeOn.postValue(false);
          setProgressMessage(false);
          showLog("scanRegex",AppCommonMethods.getScanRegex(scanType));
          showLog("scannedBarcodeData",chkNull(scannedBarcodeData, ""));
          if(!chkNull(scannedBarcodeData, "").matches(AppCommonMethods.getScanRegex(scanType))){
            context.showCustomErrDialog(String.format(context.getString(R.string.err_invalid_scan), getTypeCharCode(), scanType, scannedBarcodeData));
            //if possible, pass session Type & check Std/NonStd in case of Encoding
          }
          else
            barcodeData.postValue(!scanType.matches("(?i)(^.*(Barcode|TID|RFID QR).*$)")?chkNull(scannedBarcodeData, "").trim():AppCommonMethods.getLeftZeroReplacedString(context,chkNull(scannedBarcodeData, "")) + (scanType.replace("Barcode", "").trim().length() > 0 && chkNull(scannedBarcodeData, "").trim().length() > 0 ? ";;" + scanType : ""));
        }
      }
    }
  }
  
  /*private void assignBluetoothReader(){
    if(reader==null) reader = RFIDWithUHFBLE.getInstance();
    if(reader != null){
      if(isNonEmpty(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS)))
        reader.connect(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS));
        //reader = RFIDWithUHFBLE.getInstance();
        //reader = new BleDevice(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS), context);
      else{
        if(mBluetoothAdapter == null) mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> listPairedDevices = mBluetoothAdapter.getBondedDevices();
        if(isNonEmpty(listPairedDevices)){
          Optional<BluetoothDevice> device = listPairedDevices.stream().filter(it -> it.getName().matches("(?i)chainway.*")).findFirst();
          BluetoothDevice mDevice = (device != null && !device.isEmpty()) ? device.get() : null;
          *//*reader = new BleDevice(mDevice.getAddress(), context);
          if(reader != null)*//*
          SharedPrefManager.setString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS, mDevice.getAddress());
          reader.connect(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS));
        }
      }
    }
  }
  
  private void connect(){
    if(reader!=null && reader.getConnectStatus()== ConnectionStatus.DISCONNECTED){
      assignBluetoothReader();
    }
  }
  
  private void disconnect(){
    if(reader!=null) reader.disconnect();
  } *//**
   * Register bluetooth event.
   *//*
  private void registerBluetoothEvent(){
    if(SharedPrefManager.getIsDeviceBluetoothDependent() && ((MainActivity) context) != null && !((MainActivity) context).isFinishing()){
      bluetoothResultLauncher = ((MainActivity) context).registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
        @Override
        public void onActivityResult(ActivityResult result){
          checkAndConnectReader();
        }
      });
      showLog("bluetoothResultLauncher", "" + (bluetoothResultLauncher != null));
    }
  }*/
  
  
  
}

