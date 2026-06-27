package com.itek.retail.reader.honeywell;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.showLog;
import static com.itek.retail.common.AppCommonMethods.showShortToast;

import android.os.Handler;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.InvalidScannerNameException;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;
import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.reader.BarcodeHandler;
import com.itek.retail.reader.MainReaderRepository;

import java.util.HashMap;
import java.util.Map;

public class HoneywellBarcodeHandler extends BarcodeHandler implements BarcodeReader.BarcodeListener, BarcodeReader.TriggerListener{
  //public MutableLiveData<Boolean> isBarcodeOn = new MutableLiveData<>(false);
  //public MutableLiveData<String> barcodeData = new MutableLiveData<>("");
  //protected AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.OTHER;
  //private final String TAG = HoneywellBarcodeHandler.class.getSimpleName();
  //private CommonActivity context;
  //private MainReaderRepository mainReaderRepository;
  //private String scanType = "";
  
  private BarcodeReader barcodeReader;
  private AidcManager manager;
  private BarcodeReader.BarcodeListener barcodeListener;
  private BarcodeReader.TriggerListener triggerListener;
  
  public HoneywellBarcodeHandler(CommonActivity context, MainReaderRepository mainReaderRepository, AppCommonMethods.SessionType sessionType, boolean isInit){
    super(context, mainReaderRepository, sessionType, isInit);
    /*this.context = context;
    this.mainReaderRepository = mainReaderRepository;
    this.sessionType = sessionType;
    try{
      //if(isInit) init();
    }catch(Exception e){
      e.printStackTrace();
      showLog(e.getClass().getSimpleName(), "" + e.getMessage());
    }*/
  }
  
  @Override
  protected void onTimerFinish(){
    stopScan();
  }
  
  @Override
  public void startScan(String type){
    showLog(TAG, "Barcode Scan Started");
    if(barcodeReader != null){
      scanType = chkNull(type, "Barcode");
      showLog(TAG, "" + (barcodeReader != null));
      try{
        //barcodeReader.light(true);
        //barcodeReader.decode(true);
        barcodeReader.softwareTrigger(true);
        isBarcodeOn.postValue(true);
        //barcodeReader.claim();
      }
      catch(ScannerUnavailableException e){
        e.printStackTrace();
        showShortToast(context, "Scanner unavailable");
      }
      catch(ScannerNotClaimedException e){
        throw new RuntimeException(e);
      }
    }
  }
  
  @Override
  public void stopScan(){
    scanTimer.cancel();
    isBarcodeOn.postValue(false);
    setProgressMessage(false);
    try{
      barcodeReader.softwareTrigger(false);
    }
    catch(ScannerNotClaimedException e){
      throw new RuntimeException(e);
    }
    catch(ScannerUnavailableException e){
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public void onResume(){
    showLog(TAG, "--------------------BARCODE SCAN RESUME--------------------");
    new Handler().post(new Runnable(){
      @Override
      public void run(){
        if(barcodeReader != null){
          showLog(TAG, "Barcode Init In Process");
          
          // register bar code event listener
          barcodeReader.addBarcodeListener(HoneywellBarcodeHandler.this);
          
          // set the trigger mode to client control
          try{
            barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            
          }
          catch(UnsupportedPropertyException e){
            showShortToast(context, "Fail to apply properties: " + e.getMessage());
          }
          // register trigger state change listener
          barcodeReader.addTriggerListener(HoneywellBarcodeHandler.this);
          
          Map<String, Object> properties = new HashMap<String, Object>();
          // Set Symbologies On/Off
          properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
          properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
          properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
          properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
          properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
          properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
          properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
          properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
          properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
          properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
          properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
          properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
          // Set Max Code 39 barcode length
          properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
          // Turn on center decoding
          properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
          // Enable bad read response
          properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
          // Sets time period for decoder timeout in any mode
          properties.put(BarcodeReader.PROPERTY_DECODER_TIMEOUT, 400);
          properties.put(BarcodeReader.PROPERTY_TRIGGER_AUTO_MODE_TIMEOUT, 3);
          // Apply the settings
          barcodeReader.setProperties(properties);
          
          if(barcodeReader != null){
            try{
              barcodeReader.claim();
            }
            catch(ScannerUnavailableException e){
              e.printStackTrace();
              showShortToast(context, "Scanner Unavailable: " + e.getMessage());
            }
          }
        }
      }
    });
  }
  
  @Override
  public void onPause(){
    if(barcodeReader != null){
      // release the scanner claim so we don't get any scanner
      // notifications while pau
      stopScan();
      barcodeReader.release();
    }
  }
  
  @Override
  public void init(){
    AidcManager.create(context, new AidcManager.CreatedCallback(){
      
      @Override
      public void onCreated(AidcManager aidcManager){
        manager = aidcManager;
        try{
          barcodeReader = manager.createBarcodeReader();
          onResume();
        }
        catch(InvalidScannerNameException e){
          showShortToast(context, "Invalid Scanner Name Exception: " + e.getMessage());
        }
        catch(Exception e){
          showShortToast(context, "Exception: " + e.getMessage());
        }
      }
    });
  }
  
  public void onDestroy(){
    if(barcodeReader != null){
      // close BarcodeReader to clean up resources.
      barcodeReader.close();
      barcodeReader = null;
    }
    if(manager != null){
      // close AidcManager to disconnect from the scanner service.
      // once closed, the object can no longer be used.
      manager.close();
    }
    if(barcodeReader != null){
      // unregister barcode event listener
      barcodeReader.removeBarcodeListener(this);
      // unregister trigger state change listener
      barcodeReader.removeTriggerListener(this);
    }
  }
  
  @Override
  public void onBarcodeEvent(BarcodeReadEvent event){
    // update UI to reflect the data
    showLog(TAG, " BARCODE EVENT FIRED : True");
    stopScan();
    
    final String scannedBarcodeData = event.getBarcodeData();
    showLog(TAG, "Barcode Value: " + scannedBarcodeData);
    scanTimer.cancel();
    isBarcodeOn.postValue(false);
    setProgressMessage(false);
    if(!chkNull(scannedBarcodeData, "").matches(AppCommonMethods.getScanRegex(scanType))){
      HoneywellBarcodeHandler.this.context.showCustomErrDialog(String.format(context.getString(R.string.err_invalid_scan), getTypeCharCode(), scanType, scannedBarcodeData));
      //if possible, pass session Type & check Std/NonStd in case of Encoding
    }
    else{ //barcodeData.postValue(scannedBarcodeData);
      barcodeData.postValue(!scanType.matches("(?i)(^.*(Barcode|TID|RFID QR).*$)")?chkNull(scannedBarcodeData, "").trim():AppCommonMethods.getLeftZeroReplacedString(context,chkNull(scannedBarcodeData, "")) + (scanType.replace("Barcode", "").trim().length() > 0 && chkNull(scannedBarcodeData, "").trim().length() > 0 ? ";;" + scanType : ""));
      ((HoneywellRepository) mainReaderRepository).updateTriggerMode(false);
    }
  }
  
  @Override
  public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent){
    stopScan();
    showLog(TAG, "Failure : " + barcodeFailureEvent.toString());
  }
  
  @Override
  public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent){
    showLog(TAG, "Trigger Event :" + triggerStateChangeEvent.toString());
  }
}
