package com.itek.retail.reader.zebra;

import android.os.Handler;

import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.model.Inventory;
import com.itek.retail.reader.MainReaderRepository;

/**
 * The Zebra repository.
 */
public class ZebraRepository extends MainReaderRepository{
  
  /**
   * Instantiates a new Zebra repository.
   *
   * @param context the context
   */
  public ZebraRepository(CommonActivity context){
    super(context);
    rfidHandler = new ZebraRFIDHandler();
    barcodeHandler = new ZebraBarcodeHandlerOptimized(context, ZebraRepository.this, AppCommonMethods.SessionType.OTHER, false);
  }
  
  @Override
  public Object getReaderUHFInstance(AppCommonMethods.SessionType sessionType){
    rfidHandler.setProgressMessage(false);
    rfidHandler.onCreate(context, ZebraRepository.this, (status, message, reader) -> {
      if(status){
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
    barcodeHandler = new ZebraBarcodeHandlerOptimized(context, ZebraRepository.this, sessionType, true);
    return barcodeHandler;
  }
  
  @Override
  public void configureReader(AppCommonMethods.SessionType sessionType){
    //super.configureReader(sessionType);
    rfidHandler.configureReader(sessionType);
    rfidHandler.setProgressMessage(false);
  }
  
  @Override
  public void softScan(String type){
    //super.softScan(type);
    if(barcodeHandler != null){
      //updateTriggerMode(true);
      //new Handler().postDelayed(new Runnable(){
      //  @Override
      //  public void run(){
          barcodeHandler.startScan(type);
       // }
      //}, 5);
    }
  }
  
  /**
   * Update trigger mode.
   *
   * @param isBarcodeMode the is barcode mode
   */
  public void updateTriggerMode(boolean isBarcodeMode){
    if(rfidHandler != null && barcodeHandler != null)
      ((ZebraRFIDHandler) rfidHandler).updateTriggerMode(isBarcodeMode);
  }
  
  @Override
  public void readTagCurrentPassword(Inventory pickedTag){
    ((ZebraRFIDHandler) rfidHandler).readTagCurrentPassword(pickedTag, true);
  }
  
}
