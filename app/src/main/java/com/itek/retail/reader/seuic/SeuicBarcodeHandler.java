package com.itek.retail.reader.seuic;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.reader.BarcodeHandler;
import com.itek.retail.reader.MainReaderRepository;
import com.seuic.scanner.DecodeInfo;
import com.seuic.scanner.DecodeInfoCallBack;
import com.seuic.scanner.Scanner;
import com.seuic.scanner.ScannerFactory;
import com.seuic.scanner.ScannerKey;

/**
 * The Seuic barcode handler.
 */
public class SeuicBarcodeHandler extends BarcodeHandler implements DecodeInfoCallBack{
    private Scanner scanner;
  private boolean mScanRunning = false;
  Runnable runnable = new Runnable(){
    @Override
    public void run(){
      if(ScannerKey.open() > -1){
        final int ret = ScannerKey.getKeyEvent();
        if(ret > -1 && mScanRunning){
          switch(ret){
            case ScannerKey.KEY_DOWN:
            case ScannerKey.KEY_UP:
              if(scanner != null && mScanRunning){
                mScanRunning = false;
                isBarcodeOn.postValue(true);
                setProgressMessage("Scanning " + scanType + "...", true);
                scanTimer.start();
                scanner.startScan();
              }
              break;
            default:
              break;
          }
        }
      }
    }
  };
  private boolean isDataScanned = false;
  
  public SeuicBarcodeHandler(CommonActivity context, MainReaderRepository mainReaderRepository, AppCommonMethods.SessionType sessionType, boolean isInit){
    super(context, mainReaderRepository, sessionType, isInit);
  }
  
  @Override
  protected void onTimerFinish(){
    stopScan();
    if(!isDataScanned)
      context.showCustomErrDialog(String.format(context.getString(R.string.err_scan_fail), getTypeCharCode(), scanType));
  }
  
  @Override
  public void init(){
    try{
      if(scanner == null) scanner = ScannerFactory.getScanner(context);
      scanner.open();
      scanner.enable();
      scanner.setDecodeInfoCallBack(this);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void startScan(String scanType){
    this.scanType = chkNull(scanType, "Barcode");;
    //Log.e("scanner", "" + (scanner != null));
    mScanRunning = true;
    new Thread(runnable).start();
  }
  
  @Override
  public void stopScan(){
    mScanRunning = false;
    scanTimer.cancel();
    isBarcodeOn.postValue(false);
    setProgressMessage(false);
    try{
      scanner.stopScan();
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void onResume(){
    init();
  }
  
  @Override
  public void onPause(){
    stopScan();
    if(scanner != null){
      scanner.disable();
      scanner.close();
    }
  }
  
  @Override
  public void onDestroy(){
    stopScan();
    if(scanner != null){
      scanner.disable();
      scanner.close();
    }
  }
  
  @Override
  public void onDecodeComplete(DecodeInfo decodeInfo){
    final String scannedBarcodeData = decodeInfo.barcode;
    //Log.e("scannedBarcodeData", "_"+scannedBarcodeData);
    if(isNonEmpty(scannedBarcodeData)){
      isDataScanned = true;
      stopScan();
      if(!chkNull(scannedBarcodeData, "").matches(AppCommonMethods.getScanRegex(scanType))){
        context.showCustomErrDialog(String.format(context.getString(R.string.err_invalid_scan), getTypeCharCode(), scanType, scannedBarcodeData));
      }
      else
        barcodeData.postValue(!scanType.matches("(?i)(^.*(Barcode|TID|RFID QR).*$)")?chkNull(scannedBarcodeData, "").trim():AppCommonMethods.getLeftZeroReplacedString(context,chkNull(scannedBarcodeData, "")) + (scanType.replace("Barcode", "").trim().length() > 0 && chkNull(scannedBarcodeData, "").trim().length() > 0 ? ";;" + scanType : ""));
      stopScan();
    }
  }
}
