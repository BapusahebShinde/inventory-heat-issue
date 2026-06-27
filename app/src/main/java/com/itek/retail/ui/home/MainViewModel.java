package com.itek.retail.ui.home;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.reader.ProgressDialogModel;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * The Main view model.
 */
public class MainViewModel extends ViewModel{
  
  public MutableLiveData<Object> readerInstance = new MutableLiveData<>();
  public MutableLiveData<Object> sensorInstance = new MutableLiveData<>();
  public MutableLiveData<Object> readerUHFInstance = new MutableLiveData<>();
  private MainRepository mainRepository;
  
  private CommonActivity context;
  
  /**
   * Instantiates a new Main view model.
   *
   * @param context the context
   */
  
  public MainViewModel(CommonActivity context){
    super();
    // instantiating object of model class
    this.context = context;
    mainRepository = new MainRepository(this.context);
    getReaderInstance();
    
  }
  
  /**
   * Get reader object mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Object> getReaderObject(){
    return readerInstance;
  }
  
  /**
   * Get reader uhf object mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Object> getReaderUHFObject(){
    return readerUHFInstance;
  }
  
  /**
   * Get is device configured mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> getIsDeviceConfigured(){ return mainRepository.isDeviceConfigured(); }
  
  /**
   * Get reader power mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Integer> getReaderPower(){ return mainRepository.readerPower(); }
  
  /**
   * Set reader power.
   *
   * @param power the power
   */
  public void setReaderPower(int power){ mainRepository.setReaderPower(power); }
  
  /**
   * Get read tag password mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<String> readTagPassword(){ return mainRepository.readTagPassword(); }
  
  /**
   * Set trigger power.
   *
   * @param value the value
   */
  public void setTriggerValue(boolean value){ mainRepository.setTriggerValue(value); }
  
  /**
   * Set fifo date.
   * @param fifoDate the fifo date
   */
  public void setFifoDate(final String fifoDate){
    mainRepository.setFifoDate(fifoDate);
  }
  
  /**
   * Get is session on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> getIsSessionOn(){ return mainRepository.isSessionOn(); }
  
  /**
   * Get is inventory on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> getIsInventoryOn(){ return mainRepository.isInventoryOn(); }
  
  /**
   * Get is search on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> getIsSearchOn(){ return mainRepository.isSearchOn(); }
  
  public MutableLiveData<String> getSensorData(){
    return mainRepository.getSensorData();
  }
  
  /**
   * Get search percentage mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Integer> getSearchPercentage(){ return mainRepository.searchPercentage(); }
  
  /**
   * Get search rssi mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<String> getSearchRssi(){ return mainRepository.searchRssi(); }
  
  /**
   * Get is pick on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> getIsPickOn(){ return mainRepository.isPickOn(); }
  
  /**
   * Get is encode on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> getIsEncodeOn(){ return mainRepository.isEncodeOn(); }
  
  /**
   * Get is encode on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> getIsDecodeOn(){ return mainRepository.isDecodeOn(); }
  
  /**
   * Get is encode done mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> getIsEncodeDone(){ return mainRepository.isEncodeDone(); }
  
  /**
   * Get is decode done mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> getIsDecodeDone(){ return mainRepository.isDecodeDone(); }
  
  /**
   * Get pick data mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Inventory> getPickData(){ return mainRepository.pickData(); }
  
  /**
   * Get pick data mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<List<Inventory>> getPickedListData(){ return mainRepository.pickedListData(); }
  
  /**
   * Get porgress status mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<ProgressDialogModel> getPorgressStatus(){ return mainRepository.getProgressSTatus(); }
  
  /**
   * Get all inventory data live data.
   *
   * @return the live data
   */
  public LiveData<List<Inventory>> getAllInventoryData(){ return mainRepository.getAllInventoryData(); }
  
  /**
   * Get all inventory data size live data.
   *
   * @param sessionId the session id
   * @return the live data
   */
  public LiveData<Integer> getAllInventoryDataSize(String sessionId){ return mainRepository.getAllInventoryDataSize(sessionId); }
  
  /**
   * Get all inventory data size live data.
   *
   * @param sessionId the session id
   * @return the live data
   */
  public LiveData<Integer> getAllInventoryDataSize(String sessionId, boolean isTripInventory){ return mainRepository.getAllInventoryDataSize(sessionId, isTripInventory); }
  
  public int getInventoryTotalCount(String sessionId){
    return mainRepository.getInventoryTotalCount(sessionId);
  }
  
  public int getInventoryScoreCount(String sessionId){
    return mainRepository.getInventoryScoreCount(sessionId);
  }
  
  public int getUnencodedTagCount(String sessionId){
    return mainRepository.getUnencodedTagCount(sessionId);
  }
  
  public int getVerifiedTagCount(String sessionId){
    return mainRepository.getVerifiedTagCount(sessionId);
  }
  
  public int getAlignTagCount(String sessionId){
    return mainRepository.getAlignTagCount(sessionId);
  }
  
  /**
   * Get reader instance.
   */
  public void getReaderInstance(){
    Object obj = mainRepository.getReaderInstance();
    if(obj != null){
      showLog("Instanse ", "SET");
      readerInstance.setValue(obj);
    }
    else{
      showLog("Instanse ", "NOT SET");
    }
    
  }
  
  /**
   * Get reader instance.
   */
  public void getSensorAndStart(){
    mainRepository.getSensorAndStart();
  }
  
  /**
   * Get reader uhf instance.
   *
   * @param sessionType the session type
   */
  public void getReaderUHFInstance(AppCommonMethods.SessionType sessionType){
    Object obj = mainRepository.getReaderUHFInstance(sessionType);
    if(obj != null){
      showLog("Instanse ", "SET");
      readerUHFInstance.setValue(obj);
    }
    else{
      showLog("Instanse ", "NOT SET");
    }
  }
  
  /**
   * Get barcode reader instance.
   *
   * @param sessionType the session type
   */
  public void getBarcodeReaderInstance(AppCommonMethods.SessionType sessionType){
    mainRepository.getBarcodeReaderInstance(sessionType);
  }
  
  /**
   * Generate offline session id string.
   *
   * @param type the type
   * @param cc   the cc
   * @return the string
   */
  public String generateOfflineSessionId(AppCommonMethods.SessionType type, Calendar cc){ return mainRepository.generateOfflineSessionId(type, cc); }
  
  /**
   * Start session.
   *
   * @param rfidSession      the rfid session
   * @param isStartInventory the is start inventory
   */
  public void startSession(RFIDSession rfidSession, boolean isStartInventory){ mainRepository.startSession(rfidSession, null, isStartInventory); }
  
  public void startSession(RFIDSession rfidSession, Set<String> eans, boolean isStartInventory){ mainRepository.startSession(rfidSession, eans, isStartInventory); }
  
  /**
   * Stop session.
   *
   * @param rfidSession   the rfid session
   * @param isDiscardData the is discard data
   */
  public void stopSession(RFIDSession rfidSession, boolean isDiscardData){ mainRepository.stopSession(rfidSession, rfidSession != null ? rfidSession.sessionType : 0, isDiscardData); }
  
  /**
   * Stop session.
   *
   * @param rfidSession   the rfid session
   * @param isDiscardData the is discard data
   */
  public void stopSession(RFIDSession rfidSession, int sessionType, boolean isDiscardData){ mainRepository.stopSession(rfidSession, sessionType, isDiscardData); }
  
  /**
   * Delete session.
   *
   * @param rfidSession the rfid session
   */
  public void deleteSession(RFIDSession rfidSession, int sessionType){ mainRepository.deleteSession(rfidSession, sessionType); }
  
  /**
   * Delete session.
   *
   * @param rfidSession the rfid session
   */
  public void deleteSession(RFIDSession rfidSession){ mainRepository.deleteSession(rfidSession, rfidSession != null ? rfidSession.sessionType : 0); }
  
  /**
   * Delete session.
   *
   * @param sessionType the session type
   */
  public void deleteSession(AppCommonMethods.SessionType sessionType){ mainRepository.deleteSessionByType(sessionType); }
  
  /**
   * Start inventory.
   */
  public void startInventory(){ mainRepository.startInventory(); }
  
  /**
   * Start inventory.
   */
  public void startInventory(boolean isHideUnencodedTags){ mainRepository.startInventory(isHideUnencodedTags); }
  
  /**
   * Start inventory.
   */
  public void startInventory(final List<String> listIgnoreEpcs){ mainRepository.startInventory(listIgnoreEpcs); }
  
  /**
   
   * Start inventory.
   */
  public void startInventory(boolean isHideUnencodedTags,final List<String> listIgnoreEpcs){ mainRepository.startInventory(isHideUnencodedTags,listIgnoreEpcs); }
  
  /**
   * Perform barcode based search.
   *
   * @param barcode the barcode
   */
  public void performBarcodeBasedSearch(String barcode){ mainRepository.performBarcodeBasedSearch(barcode); }
  
  /**
   * Perform barcode based search.
   *
   * @param barcode the barcode
   */
  public void performBarcodeBasedSearch(String barcode, boolean isLockSearchEPC){ mainRepository.performBarcodeBasedSearch(barcode, isLockSearchEPC); }
  
  /**
   * Perform epc based search.
   *
   * @param epc the epc
   */
  public void performEPCBasedSearch(String epc){ mainRepository.performEPCBasedSearch(epc); }
  
  /**
   * Perform tid based search.
   *
   * @param tid the tid
   */
  public void performTIDBasedSearch(String tid){ mainRepository.performTIDBasedSearch(tid); }
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(String barcode){ performPick(barcode, false); }
  
  
  public void performPick(String barcode, final Integer pickPower){ performPick(barcode, false, pickPower); }
  public void performPick(String barcode, final boolean isDecodeOnPick){ performPick(barcode, isDecodeOnPick, 0); }
  public void performPick(String barcode, final boolean isDecodeOnPick, final Integer pickPower){ mainRepository.performPick(barcode, isDecodeOnPick, pickPower); }
  public void performPick(String barcode, final List<String> listEpcs){ mainRepository.performPick(barcode, listEpcs); }
  public void performPick(String barcode, final boolean isPostPicked, final List<String> listEpcs){ mainRepository.performPick(barcode, isPostPicked, listEpcs); }
  public void performPick(String barcode, final Integer pickPower,final List<String> listEpcs){ mainRepository.performPick(barcode, pickPower,listEpcs); }
  public void performPick(String barcode, final boolean isDecodeOnPick, final Integer pickPower,final List<String> listEpcs){ mainRepository.performPick(barcode, isDecodeOnPick, pickPower,listEpcs); }
  public void performPick(String barcode, final Integer pickPower,final boolean isPostPicked, final List<String> listEpcs){ mainRepository.performPick(barcode, pickPower, isPostPicked,listEpcs); }
  public void performPick(String barcode, final boolean isDecodeOnPick, final Integer pickPower,final boolean isPostPicked, final List<String> listEpcs){ mainRepository.performPick(barcode, isDecodeOnPick, pickPower,isPostPicked,listEpcs); }
  
  public void performTidPick(String barcode, List<String> tids){ mainRepository.performTidPick(barcode, tids); }
  
  /**
   * Stop inventory.
   */
  public void stopInventory(){ mainRepository.stopInventory(); }
  
  /**
   * On pause.
   */
  public void onPause(){ mainRepository.onPause(); }
  
  /**
   * Check and set reader.
   */
  public void checkAndSetReader(){ mainRepository.checkAndSetReader(); }
  
  /**
   * Check and connect reader.
   */
  public void checkAndConnectReader(){ mainRepository.checkAndConnectReader(); }
  
  /**
   * Configure device.
   *
   * @param sessionType the session type
   */
  public void configureDevice(AppCommonMethods.SessionType sessionType){ mainRepository.configureDevice(sessionType); }
  
  /**
   * On resume.
   *
   * @param sessionType the session type
   */
  public void onResume(AppCommonMethods.SessionType sessionType){ mainRepository.onResume(sessionType); }
  
  /**
   * On destroy.
   */
  public void onDestroy(){ mainRepository.onDestroy(); }
  
  /**
   * Soft scan.
   */
  public void softScan(){
    showLog("softScan", "softScan");
    mainRepository.softScan();
  }
  
  /**
   * Soft scan.
   */
  public void softScan(final String scanType){
    showLog("softScan", "softScan");
    mainRepository.softScan(scanType);
  }
  
  /**
   * Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  public void performEncoding(final Inventory pickedTag){ mainRepository.performEncoding(pickedTag); }
  
  /**
   * Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  public void performEncoding(final Inventory pickedTag, final String currentTagPassword){ mainRepository.performEncoding(pickedTag, currentTagPassword); } /**
   
   /* Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  public void performEncoding(final Inventory pickedTag, final Bundle extras){ mainRepository.performEncoding(pickedTag,extras); }
  
  /* Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  public void performEncoding(final Inventory pickedTag, final String currentTagPassword,final Bundle extras){ mainRepository.performEncoding(pickedTag, currentTagPassword,extras); }
  
  /**
   * Read Tag Current Password
   *
   * @param pickedTag the picked tag
   */
  public void readTagCurrentPassword(Inventory pickedTag){ mainRepository.readTagCurrentPassword(pickedTag); }
  
  /**
   * Perform encoding.
   *
   * @param listPickedTags the list picked tags
   */
  public void performEncoding(List<Inventory> listPickedTags){
    mainRepository.performEncoding(listPickedTags);
  }
  
  /**
   * Perform encoding.
   *
   * @param listPickedTags the list picked tags
   * @param sessionType    the session type
   */
  public void performEncoding(List<Inventory> listPickedTags, AppCommonMethods.SessionType sessionType){
    mainRepository.performEncoding(listPickedTags, sessionType);
  }
  
  /**
   * Perform decoding.
   *
   * @param pickedTag the picked tag
   */
  public void performDecoding(Inventory pickedTag){ mainRepository.performDecoding(pickedTag); }
  
  /**
   * Perform decoding.
   *
   * @param pickedTag the picked tag
   */
  public void performDecoding(Inventory pickedTag,Bundle extras){ mainRepository.performDecoding(pickedTag,extras); }
  
  /**
   * Perform decoding.
   *
   * @param listPickedTags the list picked tags
   */
  public void performDecoding(List<Inventory> listPickedTags){ mainRepository.performDecoding(listPickedTags); }
  
  /**
   * Perform decoding.
   *
   * @param listPickedTags the list picked tags
   * @param sessionType    the session type
   */
  public void performDecoding(List<Inventory> listPickedTags, AppCommonMethods.SessionType sessionType){ mainRepository.performDecoding(listPickedTags, sessionType); }
  
  /**
   * Get is process on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> getIsProcessOn(){ return mainRepository.isProcessOn(); }
  
  /**
   * Is reader present boolean.
   *
   * @return the boolean
   */
  public boolean isReaderPresent(boolean isReaderInstanceSet){ return mainRepository.isReaderPresent(isReaderInstanceSet); }
  
  /**
   * Is reader connected boolean.
   *
   * @return the boolean
   */
  public boolean isReaderConnected(){ return mainRepository.isReaderConnected(); }
  
  /**
   * Is reader set mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isReaderSet(){ return mainRepository.isReaderSet(); }
  
  /**
   * Is trigger pressed mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isTriggerPressed(){ return mainRepository.isTriggerPressed(); }
  
  /**
   * Get is barcode on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> getIsBarcodeOn(){ return mainRepository.isBarcodeOn(); }
  
  /**
   * Get barcode data mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<String> getBarcodeData(){ return mainRepository.barcodeData(); }
  
  /**
   * Update active session flag rfid session.
   *
   * @param sessionType the session type
   * @param isActive    the is active
   * @return the rfid session
   */
  public RFIDSession updateActiveSessionFlag(AppCommonMethods.SessionType sessionType, boolean isActive){
    return mainRepository.updateActiveSessionFlag(sessionType, isActive);
  }
  
  public RFIDSession getCurrentSession(AppCommonMethods.SessionType sessionType){
    return mainRepository.getCurrentSession(sessionType);
  }
  
  /**
   * Call web service.
   *
   * @param commonActivity   the common activity
   * @param commonFragment   the common fragment
   * @param url              the url
   * @param jsonRequest      the json request
   * @param args             the args
   * @param isRetry          the is retry
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(final CommonActivity commonActivity, final CommonFragment commonFragment, final String url, final JSONObject jsonRequest, final Bundle args, final boolean isRetry, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){ mainRepository.callWebService(commonActivity, commonFragment, url, jsonRequest, args, isRetry, progressMsg, isOfflineProcess, isDBProcess); }
  
  public void showLog(final String tag, final String msg){
    AppCommonMethods.showLog(tag, msg, true);
  }
}