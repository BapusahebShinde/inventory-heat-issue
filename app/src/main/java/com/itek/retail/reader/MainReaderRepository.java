package com.itek.retail.reader;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.os.Bundle;

import androidx.lifecycle.MutableLiveData;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.RFIDSession;

import java.util.List;
import java.util.Set;

/**
 * The Main reader repository.
 */
public class MainReaderRepository{
  
  public MutableLiveData<ProgressDialogModel> progressData = new MutableLiveData<>();
  protected CommonActivity context;
  protected RFIDHandler rfidHandler;
  protected BarcodeHandler barcodeHandler;
  
  public MainReaderRepository(CommonActivity context){
    this.context = context;
  }
  
  /**
   * Instantiates a new Main reader repository.
   *
   * @param context the context
   */
  public MainReaderRepository(CommonActivity context, final RFIDHandler rfidHandler, final BarcodeHandler barcodeHandler){
    this.context = context;
    this.rfidHandler = rfidHandler;
    this.barcodeHandler = barcodeHandler;
  }
  
  public Object getReaderInstance(){
    setProgressMessage(false);
    return rfidHandler == null ? null : rfidHandler.getReaderInstance(context, this, (status, message, reader) -> {
      if(status){
        setProgressMessage(false);
        return reader;
      }
      else{
        return null;
      }
    });
  }
  
  /**
   * Set progress message.
   *
   * @param isShowDialog the is show dialog
   */
  public void setProgressMessage(boolean isShowDialog){ setProgressMessage("", isShowDialog); }
  
  /**
   * Set progress message.
   *
   * @param message      the message
   * @param isShowDialog the is show dialog
   */
  public void setProgressMessage(String message, boolean isShowDialog){
    showLog("setProgressMessage", chkNull(message, "") + "_" + isShowDialog);
    
    if(isShowDialog)
      context.showProgressDialog(chkNull(message, context.getString(R.string.msg_init_reader_connection)));
    else context.hideProgressDialog();
  }
  
  /**
   * Get porgress status mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<ProgressDialogModel> getPorgressStatus(){ return progressData; }
  
  /**
   * Start session.
   *
   * @param rfidSession      the rfid session
   * @param isStartInventory the is start inventory
   */
  public void startSession(RFIDSession rfidSession, boolean isStartInventory){
    if(rfidHandler != null) rfidHandler.startSession(rfidSession, isStartInventory);
  }
  
  public void startSession(RFIDSession rfidSession, Set<String> eans, boolean isStartInventory){
    if(rfidHandler != null) rfidHandler.startSession(rfidSession, eans, isStartInventory);
  }
  
  /**
   * Stop session.
   */
  public void stopSession(){ if(rfidHandler != null) rfidHandler.stopSession(); }
  
  /**
   * Start inventory.
   */
  public void startInventory(){ if(rfidHandler != null) rfidHandler.performInventory(); }
  
  /**
   * Start inventory.
   */
  public void startInventory(final boolean isHideUnencodedTags){
    if(rfidHandler != null) rfidHandler.performInventory(isHideUnencodedTags);
  }
  
  /**
   * Start inventory.
   */
  public void startInventory(final List<String> listIgnoreEpcs){ if(rfidHandler != null) rfidHandler.performInventory(listIgnoreEpcs); }
  
  /**
   * Start inventory.
   */
  public void startInventory(final boolean isHideUnencodedTags, final List<String> listIgnoreEpcs){ if(rfidHandler != null) rfidHandler.performInventory(isHideUnencodedTags,listIgnoreEpcs); }
  
  /**
   * Perform barcode based search.
   *
   * @param barcode the barcode
   */
  public void performBarcodeBasedSearch(String barcode){
    if(rfidHandler != null) rfidHandler.performBarcodeBasedSearch(barcode);
  }
  
  /**
   * Perform barcode based search.
   *
   * @param barcode the barcode
   */
  public void performBarcodeBasedSearch(String barcode, final boolean isLockSearchEPC){
    if(rfidHandler != null) rfidHandler.performBarcodeBasedSearch(barcode, isLockSearchEPC);
  }
  
  /**
   * Perform epc based search.
   *
   * @param epc the epc
   */
  public void performEPCBasedSearch(String epc){
    if(rfidHandler != null) rfidHandler.performEPCBasedSearch(epc);
  }
  
  /**
   * Perform tid based search.
   *
   * @param tid the tid
   */
  public void performTIDBasedSearch(String tid){
    if(rfidHandler != null) rfidHandler.performTIDBasedSearch(tid);
  }
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(String barcode, boolean isDecodeOnPick){
    if(rfidHandler != null) rfidHandler.performPick(barcode, isDecodeOnPick);
  }
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(String barcode, boolean isDecodeOnPick,final Integer pickPower){
    if(rfidHandler != null) rfidHandler.performPick(barcode, isDecodeOnPick,pickPower);
  }
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(String barcode, final List<String> listEpcs){
    if(rfidHandler != null) rfidHandler.performPick(barcode, listEpcs);
  }
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(String barcode, boolean isPostPicked, final List<String> listEpcs){
    if(rfidHandler != null) rfidHandler.performPick(barcode, isPostPicked,listEpcs);
  }
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(String barcode, final Integer pickPower, final List<String> listEpcs){
    if(rfidHandler != null) rfidHandler.performPick(barcode, pickPower,listEpcs);
  }
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(String barcode, boolean isDecodeOnPick,final Integer pickPower, final List<String> listEpcs){
    if(rfidHandler != null) rfidHandler.performPick(barcode, isDecodeOnPick,pickPower,listEpcs);
  }
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(String barcode, final Integer pickPower,final boolean isPostPicked, final List<String> listEpcs){
    if(rfidHandler != null) rfidHandler.performPick(barcode,pickPower,isPostPicked,listEpcs);
  }
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(String barcode, boolean isDecodeOnPick,final Integer pickPower,final boolean isPostPicked, final List<String> listEpcs){
    if(rfidHandler != null) rfidHandler.performPick(barcode, isDecodeOnPick,pickPower,isPostPicked,listEpcs);
  }
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   * @param tids    the tids
   */
  public void performTidPick(String barcode, List<String> tids){
    if(rfidHandler != null) rfidHandler.performTidPick(barcode, tids);
  }
  
  /**
   * Stop inventory.
   */
  public void stopInventory(){
    if(rfidHandler != null) rfidHandler.stopInventory();
  }
  
  /**
   * On pause.
   */
  public void onPause(){
    if(rfidHandler != null) rfidHandler.onPause();
    if(barcodeHandler != null) barcodeHandler.onPause();
  }
  
  /**
   * Check and set reader.
   */
  public void checkAndSetReader(){
    if(rfidHandler != null) rfidHandler.checkAndSetReader();
  }
  
  /**
   * Check and connect reader.
   */
  public void checkAndConnectReader(){
    if(rfidHandler != null) rfidHandler.checkAndConnectReader();
  }
  
  /**
   * On resume.
   *
   * @param sessionType the session type
   */
  public void onResume(AppCommonMethods.SessionType sessionType){
    if(rfidHandler != null) rfidHandler.onResume(sessionType);
    if(barcodeHandler != null) barcodeHandler.onResume();
  }
  
  /**
   * On destroy.
   */
  public void onDestroy(){
    if(rfidHandler != null) rfidHandler.onDestroy();
    if(barcodeHandler != null) barcodeHandler.onDestroy();
  }
  
  /**
   * Is process on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isProcessOn(){
    return new MutableLiveData<>(rfidHandler != null || barcodeHandler != null ? ((rfidHandler != null && rfidHandler.isProcessOn()) || (barcodeHandler != null && chkNotNullTrue(barcodeHandler.isBarcodeOn.getValue()))) : null);
  }
  
  /**
   * Is reader present boolean.
   *
   * @return the boolean
   */
  public boolean isReaderPresent(boolean isReaderInstanceSet){ return rfidHandler != null && rfidHandler.isReaderPresent(isReaderInstanceSet); }
  
  /**
   * Is reader connected boolean.
   *
   * @return the boolean
   */
  public boolean isReaderConnected(){ return rfidHandler != null && rfidHandler.isReaderConnected(); }
  
  /**
   * Is trigger pressed mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isTriggerPressed(){
    return rfidHandler != null ? rfidHandler.isTriggerPressed : new MutableLiveData<Boolean>(null);
  }
  
  /**
   * Is barcode on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isBarcodeOn(){
    return barcodeHandler != null ? barcodeHandler.isBarcodeOn : new MutableLiveData<Boolean>(null);
  }
  
  /**
   * Barcode data mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<String> barcodeData(){
    return barcodeHandler != null ? barcodeHandler.barcodeData : new MutableLiveData<String>(null);
  }
  
  /**
   * Get reader uhf instance object.
   *
   * @param sessionType the session type
   * @return the object
   */
  public Object getReaderUHFInstance(AppCommonMethods.SessionType sessionType){
    return null;
  }
  
  /**
   * Get barcode reader instance object.
   *
   * @param sessionType the session type
   * @return the object
   */
  public Object getBarcodeReaderInstance(AppCommonMethods.SessionType sessionType){ return null; }
  
  /**
   * Configure reader.
   *
   * @param sessionType the session type
   */
  public void configureReader(AppCommonMethods.SessionType sessionType){
    if(rfidHandler != null && rfidHandler.isReaderConnected())
      rfidHandler.configureReader(sessionType);
  }
  
  /**
   * Set reader power.
   *
   * @param power the power
   */
  public void setReaderPower(int power){
    if(rfidHandler != null) rfidHandler.setReaderPower(power);
  }
  
  /**
   * Is reader set mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isReaderSet(){
    return rfidHandler != null ? rfidHandler.isReaderSet : new MutableLiveData<Boolean>(null);
  }
  
  /**
   * Is device configured mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isDeviceConfigured(){
    return rfidHandler != null ? rfidHandler.isDeviceConfigured : new MutableLiveData<Boolean>(null);
  }
  
  /**
   * Reader power mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Integer> readerPower(){
    return rfidHandler != null ? rfidHandler.readerPower : new MutableLiveData<Integer>(null);
  }
  
  /**
   * Read tag password mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<String> readTagPassword(){ return rfidHandler != null ? rfidHandler.readTagPassword : new MutableLiveData<String>(null); }
  
  /**
   * Is inventory on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isInventoryOn(){ return rfidHandler != null ? rfidHandler.isInventoryOn : new MutableLiveData<Boolean>(null); }
  
  /**
   * Is search on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isSearchOn(){ return rfidHandler != null ? rfidHandler.isSearchOn : new MutableLiveData<Boolean>(null); }
  
  /**
   * Search percentage mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Integer> searchPercentage(){ return rfidHandler != null ? rfidHandler.searchPercent : new MutableLiveData<Integer>(null); }
  
  /**
   * Search rssi mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<String> searchRssi(){ return rfidHandler != null ? rfidHandler.searchRssi : new MutableLiveData<String>(null); }
  
  /**
   * Is pick on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isPickOn(){ return rfidHandler != null ? rfidHandler.isPickOn : new MutableLiveData<Boolean>(null); }
  
  /**
   * Is encode on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isEncodeOn(){ return rfidHandler != null ? rfidHandler.isEncodeOn : new MutableLiveData<Boolean>(null); }
  
  /**
   * Is decode on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isDecodeOn(){ return rfidHandler != null ? rfidHandler.isDecodeOn : new MutableLiveData<Boolean>(null); }
  
  /**
   * Is encode done mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isEncodeDone(){ return rfidHandler != null ? rfidHandler.isEncodeDone : new MutableLiveData<Boolean>(null); }
  
  /**
   * Is decode done mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isDecodeDone(){ return rfidHandler != null ? rfidHandler.isDecodeDone : new MutableLiveData<Boolean>(null); }
  
  /**
   * Pick data mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Inventory> pickData(){ return rfidHandler != null ? rfidHandler.pickData : new MutableLiveData<Inventory>(null); }
  
  /**
   * Pick data mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<List<Inventory>> pickedListData(){ return rfidHandler != null ? rfidHandler.pickedListData : new MutableLiveData<List<Inventory>>(null); }
  
  /**
   * Is session on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isSessionOn(){ return rfidHandler != null ? rfidHandler.isSessionOn : new MutableLiveData<Boolean>(null); }
  
  /**
   * Soft scan.
   */
  public void softScan(String scanType){
    if(barcodeHandler != null) barcodeHandler.startScan(scanType);
  }
  
  /**
   * Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  public void performEncoding(Inventory pickedTag){
    if(rfidHandler != null) rfidHandler.performEncoding(pickedTag);
  }
  
  /**
   * Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  public void performEncoding(Inventory pickedTag, String currentTagPassword){
    if(rfidHandler != null) rfidHandler.performEncoding(pickedTag, currentTagPassword);
  }
  
  public void performEncoding(Inventory pickedTag, final Bundle extras){
    if(rfidHandler != null) rfidHandler.performEncoding(pickedTag,extras);
  }
  
  public void performEncoding(Inventory pickedTag, String currentTagPassword, final Bundle extras){
    if(rfidHandler != null) rfidHandler.performEncoding(pickedTag, currentTagPassword,extras);
  }
  
  /**
   * Read Tag Current Password.
   *
   * @param pickedTag the picked tag
   */
  public void readTagCurrentPassword(Inventory pickedTag){
    if(rfidHandler != null) rfidHandler.readTagCurrentPassword(pickedTag);
  }
  
  /**
   * Perform decoding.
   *
   * @param listPickedTags the list picked tags
   */
  public void performEncoding(List<Inventory> listPickedTags){
    if(rfidHandler != null) rfidHandler.performEncoding(listPickedTags);
  }
  
  /**
   * Perform decoding.
   *
   * @param listPickedTags the list picked tags
   * @param sessionType    the session type
   */
  public void performEncoding(List<Inventory> listPickedTags, AppCommonMethods.SessionType sessionType){
    if(rfidHandler != null) rfidHandler.performEncoding(listPickedTags, sessionType);
  }
  
  /**
   * Perform decoding.
   *
   * @param pickedTag the picked tag
   */
  public void performDecoding(Inventory pickedTag){
    if(rfidHandler != null) rfidHandler.performDecoding(pickedTag);
  }
  
  /**
   * Perform decoding.
   *
   * @param pickedTag the picked tag
   */
  public void performDecoding(Inventory pickedTag,Bundle extras){
    if(rfidHandler != null) rfidHandler.performDecoding(pickedTag,extras);
  }
  
  /**
   * Perform decoding.
   *
   * @param listPickedTags the list picked tags
   */
  public void performDecoding(List<Inventory> listPickedTags){
    if(rfidHandler != null) rfidHandler.performDecoding(listPickedTags);
  }
  
  /**
   * Perform decoding.
   *
   * @param listPickedTags the list picked tags
   * @param sessionType    the session type
   */
  public void performDecoding(List<Inventory> listPickedTags, AppCommonMethods.SessionType sessionType){
    if(rfidHandler != null) rfidHandler.performDecoding(listPickedTags, sessionType);
  }
  
  /**
   * Perform decoding.
   *
   * @param sessionType the session type
   */
  public void performDecoding(AppCommonMethods.SessionType sessionType){
    if(rfidHandler != null) rfidHandler.performDecoding(sessionType);
  }
  
  public void setTriggerValue(boolean value){
    if(rfidHandler != null) rfidHandler.setTriggerPressed();//.postValue(value);
  }
  
  public void setFifoDate(final String fifoDate){
    if(rfidHandler != null) rfidHandler.setFifoDate(fifoDate);
  }
}
