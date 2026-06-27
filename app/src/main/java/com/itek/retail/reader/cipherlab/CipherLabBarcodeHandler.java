package com.itek.retail.reader.cipherlab;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.showLog;
import static com.itek.retail.common.AppCommonMethods.showShortToast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;

import com.cipherlab.barcode.GeneralString;
import com.cipherlab.barcode.ReaderManager;
import com.cipherlab.barcode.decoder.ClResult;
import com.cipherlab.barcode.decoder.Enable_State;
import com.cipherlab.barcode.decoder.KeyboardEmulationType;
import com.cipherlab.barcode.decoder.OutputEnterChar;
import com.cipherlab.barcode.decoder.OutputEnterWay;
import com.cipherlab.barcode.decoderparams.ReaderOutputConfiguration;
import com.cipherlab.barcodebase.ReaderCallback;
import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.reader.BarcodeHandler;
import com.itek.retail.reader.MainReaderRepository;

public class CipherLabBarcodeHandler extends BarcodeHandler implements ReaderCallback{
  
  boolean isReceiverRegistered = false;
  ReaderManager mReaderManager;
  /// create a BroadcastReceiver for receiving intents from barcode reader service
  private final BroadcastReceiver myDataReceiver = new BroadcastReceiver(){
    @Override
    public void onReceive(Context context, Intent intent){
      if(intent.getAction().equals(GeneralString.Intent_READERSERVICE_CONNECTED)){
        showLog(intent.getAction(), "READERSERVICE_CONNECTED");
        
        // Make sure this app bind to barcode reader service , then user can use APIs to get/set settings from barcode reader service
        ReaderOutputConfiguration settings = new ReaderOutputConfiguration();
        mReaderManager.Get_ReaderOutputConfiguration(settings);
        settings.enableKeyboardEmulation = KeyboardEmulationType.CopyPaste;
        settings.autoEnterWay = OutputEnterWay.Disable;
        settings.autoEnterChar = OutputEnterChar.None;
        settings.showCodeLen = Enable_State.FALSE;
        settings.showCodeType = Enable_State.FALSE;
        settings.szPrefixCode = "";
        settings.szSuffixCode = "";
        settings.useDelim = ':';
        settings.clearPreviousData = Enable_State.TRUE;
        mReaderManager.Set_ReaderOutputConfiguration(settings);
        mReaderManager.SetReaderCallback(CipherLabBarcodeHandler.this);
        
      }
    }
  };
  private boolean mIsRunning = false;
  
  /**
   * Instantiates a new Chainway barcode handler.
   *
   * @param context              the context
   * @param mainReaderRepository the main reader repository
   * @param sessionType          the session type
   * @param isInit               the is init
   */
  public CipherLabBarcodeHandler(CommonActivity context, MainReaderRepository mainReaderRepository, AppCommonMethods.SessionType sessionType, boolean isInit){
    super(context, mainReaderRepository, sessionType, isInit);
    registerReceiver();
  }
  
  @Override
  protected void onTimerFinish(){
    stopScan();
  }
  
  @Override
  public void init(){
    if(mReaderManager == null) mReaderManager = ReaderManager.InitInstance(context);
    if(!isReceiverRegistered) registerReceiver();
  }
  
  private void registerReceiver(){
    IntentFilter filter = new IntentFilter();
    filter.addAction(GeneralString.Intent_READERSERVICE_CONNECTED);
    context.registerReceiver(myDataReceiver, filter);
    isReceiverRegistered =true;
  }
  
  @Override
  public void startScan(String type){
    if(mReaderManager != null && mReaderManager.SetActive(true) == ClResult.S_OK){
      scanType = chkNull(type, "Barcode");
      mReaderManager.SoftScanTrigger();
      isBarcodeOn.postValue(true);
      scanTimer.start();
    }
  }
  
  @Override
  public void stopScan(){
    if(mReaderManager != null){
      scanTimer.cancel();
      isBarcodeOn.postValue(false);
      setProgressMessage(false);
      if(mReaderManager.SetActive(false) == ClResult.S_OK) ;
    }
  }
  
  @Override
  public void onResume(){
  
  }
  
  @Override
  public void onPause(){
    stopScan();
  }
  
  @Override
  public void onDestroy(){
    if(myDataReceiver != null){
      context.unregisterReceiver(myDataReceiver);
      isReceiverRegistered =false;
    }
    if(mReaderManager != null){
      if(mReaderManager.GetActive()) stopScan();
      mReaderManager.Release();
    }
  }
  
  @Override
  public void onDecodeComplete(final String scannedBarcodeData) throws RemoteException{
    showLog("Barcode Data", scannedBarcodeData);
    showShortToast(context, "Decode Data: " + scannedBarcodeData);
    stopScan();
    if(!chkNull(scannedBarcodeData, "").matches(AppCommonMethods.getScanRegex(scanType))){
      context.showCustomErrDialog(String.format(context.getString(R.string.err_invalid_scan), getTypeCharCode(), scanType, scannedBarcodeData));
      //if possible, pass session Type & check Std/NonStd in case of Encoding
    }
    else
      barcodeData.postValue(!scanType.matches("(?i)(^.*(Barcode|TID|RFID QR).*$)")?chkNull(scannedBarcodeData, "").trim():AppCommonMethods.getLeftZeroReplacedString(context,chkNull(scannedBarcodeData, "")) + (scanType.replace("Barcode", "").trim().length() > 0 && chkNull(scannedBarcodeData, "").trim().length() > 0 ? ";;" + scanType : ""));
  }
  
  @Override
  public IBinder asBinder(){
    return null;
  }
}
