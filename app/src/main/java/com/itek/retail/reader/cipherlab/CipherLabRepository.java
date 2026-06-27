package com.itek.retail.reader.cipherlab;

import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.reader.MainReaderRepository;
import com.itek.retail.reader.zebra.ZebraBarcodeHandlerOptimized;
import com.itek.retail.reader.zebra.ZebraRepository;

public class CipherLabRepository extends MainReaderRepository
{
  /**
   * Instantiates a new CipherLab repository.
   *
   * @param context the context
   */
  
  public CipherLabRepository(CommonActivity context){
    super(context);
    rfidHandler = new CipherLabRFIDHandler();
    //barcodeHandler = new CipherLabBarcodeHandler(context, CipherLabRepository.this, AppCommonMethods.SessionType.OTHER, false);
  }
  
  @Override
  public Object getReaderUHFInstance(AppCommonMethods.SessionType sessionType){
    setProgressMessage(false);
    if(rfidHandler == null) rfidHandler = new CipherLabRFIDHandler();
    rfidHandler.onCreate(context, CipherLabRepository.this, (status, message, reader) -> {
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
    if(rfidHandler != null && rfidHandler.isReaderConnected()){
      new android.os.Handler().postDelayed(new Runnable(){
        @Override
        public void run(){
          rfidHandler.setTriggerPressed();//.postValue(value);
        }
      }, 500);
    }
  }
  
  /**
   * Get barcode reader instance object.
   *
   * @return the object
   */
  
  public Object getBarcodeReaderInstance(AppCommonMethods.SessionType sessionType){
    if(barcodeHandler == null)
      barcodeHandler = new CipherLabBarcodeHandler(context, CipherLabRepository.this, sessionType, true);
    else barcodeHandler.init();
    return barcodeHandler;
  }
}
