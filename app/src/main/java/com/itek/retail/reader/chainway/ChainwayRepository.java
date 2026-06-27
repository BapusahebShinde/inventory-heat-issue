package com.itek.retail.reader.chainway;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.lifecycle.MutableLiveData;

import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.reader.MainReaderRepository;

/**
 * The Chainway repository.
 */
public class ChainwayRepository extends MainReaderRepository{
  
  private HomeKeyEventBroadCastReceiver homeKeyEventBroadCastReceiver = null;
  
  private boolean isBluetoothReader=false;
  
  /**
   * Instantiates a new Chainway repository.
   *
   * @param context the context
   */
  public ChainwayRepository(CommonActivity context){
    super(context);
    rfidHandler = new ChainwayRFIDHandler();
    if(!Build.MANUFACTURER.equalsIgnoreCase(AppCommonMethods.DeviceType.CHAINWAY.name()) || SharedPrefManager.getIsDeviceBluetoothDependent()){
      SharedPrefManager.setIsDeviceBluetoothDependent(true);
      isBluetoothReader = true;
    }
    else barcodeHandler = new ChainwayBarcodeHandler(context, ChainwayRepository.this, AppCommonMethods.SessionType.OTHER, true);
    setHomeKeyEventBroadCastReceiver();
  }
  
  private void setHomeKeyEventBroadCastReceiver(){
    if(isBluetoothReader) return;
    if(homeKeyEventBroadCastReceiver == null){
      homeKeyEventBroadCastReceiver = new HomeKeyEventBroadCastReceiver();
      IntentFilter intentFilter1 = new IntentFilter();
      intentFilter1.addAction("com.rscja.android.KEY_DOWN");
      context.registerReceiver(homeKeyEventBroadCastReceiver, intentFilter1);
    }
  }
  
  @Override
  public Object getReaderUHFInstance(AppCommonMethods.SessionType sessionType){
    setProgressMessage(false);
    setHomeKeyEventBroadCastReceiver();
    if(rfidHandler == null) rfidHandler = new ChainwayRFIDHandler();
    rfidHandler.onCreate(context, ChainwayRepository.this, (status, message, reader) -> {
      if(status){
        setProgressMessage(false);
        return reader;
      }
      else{
        return null;
      }
    }, sessionType);
    return null;
  }
  
  @Override
  public Object getBarcodeReaderInstance(AppCommonMethods.SessionType sessionType){
    if(isBluetoothReader) return null;
    if(barcodeHandler == null)
      barcodeHandler = new ChainwayBarcodeHandler(context, ChainwayRepository.this, sessionType, true);
    else barcodeHandler.init();
    setHomeKeyEventBroadCastReceiver();
    return barcodeHandler;
  }
  
  /*@Override
  public void onPause(){
    rfidHandler.onPause();
    barcodeHandler.stopScan();
  }*/
  
  @Override
  public void onResume(AppCommonMethods.SessionType sessionType){
    super.onResume(sessionType);
    setHomeKeyEventBroadCastReceiver();
  }
  
  @Override
  public void onDestroy(){
    super.onDestroy();
    if(homeKeyEventBroadCastReceiver != null){
      context.unregisterReceiver(homeKeyEventBroadCastReceiver);
      homeKeyEventBroadCastReceiver = null;
    }
  }
  
  /**
   * The Home key event broad cast receiver.
   */
  class HomeKeyEventBroadCastReceiver extends BroadcastReceiver{
    
    static final String SYSTEM_REASON = "reason";
    static final String SYSTEM_HOME_KEY = "homekey";//home key
    static final String SYSTEM_RECENT_APPS = "recentapps";//long home key
    
    @Override
    public void onReceive(Context context, Intent intent){
      String action = intent.getAction();
      if(action.equals("com.rscja.android.KEY_DOWN")){
        showLog("TRIGGER", "PRESSED");
        int keyCode = intent.getIntExtra("keycode", 0);
        showLog("TRIGGER keyCode", "" + keyCode);
        //getStringExtra
        boolean long1 = intent.getBooleanExtra("Pressed", false);
        // home key处理点
        if(SharedPrefManager.getTriggerKeyCodes().length() > 0 && SharedPrefManager.getTriggerKeyCodes().contains("," + keyCode + ",")){
          showLog("TRIGGER1", "PRESSED1");
          rfidHandler.setTriggerPressed();/*.postValue(true);*/
        }
      }
    }
  }
  
  @Override
  public MutableLiveData<Boolean> isBarcodeOn(){
    showLog("isBarcodeOn_isBluetoothReader",""+isBluetoothReader);
    return !isBluetoothReader? super.isBarcodeOn() : rfidHandler!=null && rfidHandler instanceof ChainwayRFIDHandler?((ChainwayRFIDHandler) rfidHandler).isBarcodeOn:new MutableLiveData<Boolean>(null);
  }
  
  @Override
  public MutableLiveData<String> barcodeData(){
    showLog("barcodeData_isBluetoothReader",""+isBluetoothReader);
    return !isBluetoothReader? super.barcodeData() : rfidHandler!=null && rfidHandler instanceof ChainwayRFIDHandler?((ChainwayRFIDHandler) rfidHandler).barcodeData : new MutableLiveData<String>(null);
  }
  
  @Override
  public void softScan(String scanType){
    showLog("softScan_isBluetoothReader",""+isBluetoothReader);
    if(!isBluetoothReader) super.softScan(scanType);
    else if(rfidHandler!=null && rfidHandler instanceof ChainwayRFIDHandler) ((ChainwayRFIDHandler)rfidHandler).startBarcodeScan(scanType);
  }
}
