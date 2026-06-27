package com.itek.retail.reader.honeywell;

import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.reader.MainReaderRepository;

public class HoneywellRepository extends MainReaderRepository{
  
  //private HoneywellRFIDHandler rfidHandler;
  //private HoneywellBarcodeHandler barcodeHandler;
  
  /**
   * Instantiates a new Main reader repository.
   *
   * @param context the context
   */
  public HoneywellRepository(CommonActivity context){
    super(context);
    rfidHandler = new HoneywellRFIDHandler();
    barcodeHandler = new HoneywellBarcodeHandler(context, HoneywellRepository.this, AppCommonMethods.SessionType.OTHER, true);
  }
  
  @Override
  public Object getReaderInstance(){
    setProgressMessage(false);
    return rfidHandler.getReaderInstance(context, HoneywellRepository.this, (status, message, reader) -> {
      if(status){
        setProgressMessage(false);
        return reader;
      }
      else{
        return null;
      }
    });
  }
  
  @Override
  public Object getReaderUHFInstance(AppCommonMethods.SessionType sessionType){
    setProgressMessage(false);
    if(rfidHandler == null) rfidHandler = new HoneywellRFIDHandler();
    rfidHandler.onCreate(context, HoneywellRepository.this, (status, message, reader) -> {
      if(status){
        setProgressMessage(false);
        return reader;
      }
      else{
        return null;
      }
    }, sessionType);
    return rfidHandler;
  }
  
  @Override
  public Object getBarcodeReaderInstance(AppCommonMethods.SessionType sessionType){
    if(barcodeHandler == null)
      barcodeHandler = new HoneywellBarcodeHandler(context, HoneywellRepository.this, sessionType, true);
    else barcodeHandler.init();
    return barcodeHandler;
  }
  
  @Override
  public void softScan(String type){
    if(barcodeHandler != null){
      updateTriggerMode(true);
      barcodeHandler.startScan(type);
    }
  }
  
  /**
   * Update trigger mode.
   *
   * @param isBarcodeMode the is barcode mode
   */
  public void updateTriggerMode(boolean isBarcodeMode){
    if(rfidHandler != null && barcodeHandler != null)
      ((HoneywellRFIDHandler) rfidHandler).updateTriggerMode(isBarcodeMode);
  }
  
  /*@Override
  public void performEncoding(Inventory pickedTag, final String currentTagPassword){
    rfidHandler.performEncoding(pickedTag, currentTagPassword);
  }
  
  @Override
  public void performEncoding(Inventory pickedTag){
    performEncoding(pickedTag, "");
  }
  
  @Override
  public void performDecoding(Inventory pickedTag){
    rfidHandler.performDecoding(pickedTag);
  }
  
  @Override
  public void performDecoding(List<Inventory> listPickedTags){
    rfidHandler.performDecoding(listPickedTags);
  }
  
  @Override
  public void performDecoding(AppCommonMethods.SessionType sessionType){
    rfidHandler.performDecoding(sessionType);
  }
  
  @Override
  public void performDecoding(List<Inventory> listPickedTags, AppCommonMethods.SessionType sessionType){
    rfidHandler.performDecoding(listPickedTags, sessionType);
  }*/
  
  /**
   * Set trigger power.
   *
   * @param value the value
   */
  /*@Override
  public void setTriggerValue(boolean value){
    rfidHandler.isTriggerPressed.postValue(value);
  }*/
}
