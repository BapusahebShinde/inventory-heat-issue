package com.itek.retail.reader.seuic;

import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.reader.MainReaderRepository;

/**
 * The Seuic repository.
 */

public class SeuicRepository extends MainReaderRepository{
  
  /**
   * Instantiates a new Seuic repository.
   *
   * @param context the context
   */
  
  public SeuicRepository(CommonActivity context){
    super(context);
    rfidHandler = new SeuicRFIDHandler();
  }
  
  @Override
  public Object getReaderUHFInstance(AppCommonMethods.SessionType sessionType){
    setProgressMessage(false);
    if(rfidHandler == null) rfidHandler = new SeuicRFIDHandler();
    rfidHandler.onCreate(context, SeuicRepository.this, (status, message, reader) -> {
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
  
  public void setTriggerValue(final boolean value){
    if(rfidHandler != null && rfidHandler.isReaderConnected()) rfidHandler.setTriggerPressed();
    /*if(rfidHandler != null && rfidHandler.isReaderConnected()){
      new android.os.Handler().postDelayed(new Runnable(){
        @Override
        public void run(){
          rfidHandler.isTriggerPressed.postValue(value);
        }
      }, 500);
    }*/
  }
  
  /**
   * Get barcode reader instance object.
   *
   * @return the object
   */
  
  public Object getBarcodeReaderInstance(AppCommonMethods.SessionType sessionType){
    if(barcodeHandler == null)
      barcodeHandler = new SeuicBarcodeHandler(context, SeuicRepository.this, sessionType, true);
    else barcodeHandler.init();
    return barcodeHandler;
  }
  
}
