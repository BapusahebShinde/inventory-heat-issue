package com.itek.retail.ui.home;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.RFIDSessionDao;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.reader.MainReaderRepository;
import com.itek.retail.reader.ProgressDialogModel;
import com.itek.retail.reader.chainway.ChainwayRepository;
import com.itek.retail.reader.cipherlab.CipherLabRepository;
import com.itek.retail.reader.honeywell.HoneywellRepository;
import com.itek.retail.reader.seuic.SeuicRepository;
import com.itek.retail.reader.zebra.ZebraRepository;
import com.itek.retail.sensors.MainSensorRepository;
import com.itek.retail.sensors.providers.CommonSensorRepository;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * The Main repository.
 */
public class MainRepository{
  
  MainReaderRepository readerRepository;
  MainSensorRepository sensorRepository;
  private CommonActivity context;
  private InventoryDao inventoryDao;
  private TripInventoryDao tripInventoryDao;
  private RFIDSessionDao rfidSessionDao;
  private String sessionId = null;
  private AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.OTHER;
  private AppCommonMethods.SessionAction sessionAction = AppCommonMethods.SessionAction.OTHER;
  private RFIDSession rfidSession;
  
  /**
   * Instantiates a new Main repository.
   *
   * @param context the context
   */
  public MainRepository(CommonActivity context){
    this.context = context;
    AppDatabase db = AppDatabase.getDbInstance(context);
    rfidSessionDao = db.RFIDSessionDao();
    inventoryDao = db.InventoryDao();
    tripInventoryDao = db.TripInventoryDao();
    
    switch(SharedPrefManager.getDeviceType()){
      case ZEBRA:
        readerRepository = new ZebraRepository(this.context);
        break;
      case CHAINWAY:
        readerRepository = new ChainwayRepository(this.context);
        break;
      case SEUIC:
        readerRepository = new SeuicRepository(this.context);
        break;
      case ALPS:
        //readerRepository = new SpeedDataRepository(this.context);
        break;
      case HONEYWELL:
        readerRepository = new HoneywellRepository(this.context);
        break;
      case CIPHERLAB:
        readerRepository = new CipherLabRepository(this.context);
        break;
      case OTHER:
        readerRepository = new MainReaderRepository(this.context);
        break;
      default:
        readerRepository = new MainReaderRepository(this.context);
    }
    
    if(SharedPrefManager.getIsSensorAvailable()){
      //Sets & Uses Common Sensor Repository which uses Common Sensor Provider
      // to handle multiple Sensors in order to
      // Resolve issue 'Sensor Available but not Providing Event Data
      sensorRepository = new CommonSensorRepository(this.context);
    }
  }
  
  /**
   * Get Sensor instance object.
   *
   * @return the object
   */
  public void getSensorAndStart(){
    if(sensorRepository != null){
      sensorRepository.getSensorAndStart();
    }
    
  }
  
  public void stopSensor(){
    if(sensorRepository != null){
      sensorRepository.stopSensor();
    }
  }
  
  public MutableLiveData<String> getSensorData(){
    if(sensorRepository != null){
      return sensorRepository.getSensorData();
    }
    else{
      return new MutableLiveData<String>("0$0$0$0");
    }
  }
  
  /**
   * Set trigger power.
   *
   * @param value the value
   */
  public void setTriggerValue(boolean value){readerRepository.setTriggerValue(value);}
  
  /**
   * Set fifo date.
   *
   * @param fifoDate the fifo date
   */
  public void setFifoDate(final String fifoDate){
    readerRepository.setFifoDate(fifoDate);
  }
  
  /**
   * Get all inventory data live data.
   *
   * @return the live data
   */
  public LiveData<List<Inventory>> getAllInventoryData(){
    return inventoryDao.getAllInventoryData();
  }
  
  /**
   * Get all inventory data size live data.
   *
   * @param sessionId the session id
   * @return the live data
   */
  public LiveData<Integer> getAllInventoryDataSize(String sessionId){
    return getAllInventoryDataSize(sessionId, false);
  }
  
  /**
   * Get all inventory data size live data.
   *
   * @param sessionId the session id
   * @return the live data
   */
  public LiveData<Integer> getAllInventoryDataSize(String sessionId, final boolean isTripInventory){
    if(isNonEmpty(sessionId)) this.sessionId = sessionId;
    showLog("InvDataSize sessionId", this.sessionId != null ? this.sessionId : "null");
    if(isTripInventory && isNonEmpty(SharedPrefManager.getTripNo()) && isNonEmpty(SharedPrefManager.getHuNo()))
      return tripInventoryDao.getTripInventoryDataSize(SharedPrefManager.getTripNo(), SharedPrefManager.getHuNo());
    else return inventoryDao.getAllInventoryDataSize(this.sessionId);
    
  }
  
  public int getInventoryTotalCount(String sessionId){
    return inventoryDao.getInventorySize(sessionId);
  }
  
  public int getInventoryScoreCount(String sessionId){
    return inventoryDao.getInventoryScoreCount(sessionId);
  }
  
  public int getUnencodedTagCount(String sessionId){
    return inventoryDao.getUnencodedTagCount(sessionId);
  }
  
  public int getVerifiedTagCount(String sessionId){
    return inventoryDao.getVerifiedCount(sessionId);
  }
  
  public int getAlignTagCount(String sessionId){
    return inventoryDao.getAlignTagCount(sessionId);
  }
  
  /**
   * Get progress s tatus mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<ProgressDialogModel> getProgressSTatus(){
    return readerRepository.getPorgressStatus();
  }
  
  /**
   * Delete session.
   *
   * @param session the session
   */
  public void deleteSession(RFIDSession session, int ssType){
    final String ssId = session != null ? chkNull(session.sessionId, this.sessionId) : this.sessionId;
    final int sessionType = ssType > 0 ? ssType : session != null ? chkNull(session.sessionType, this.sessionType.getValue()) : this.sessionType.getValue();
    showLog("deleteData_SessionId", ssId);
    if(sessionType > 0)
      SharedPrefManager.setInt(AppCommonMethods.SessionType.get(sessionType).name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), 0);
    if(ssId != null){
      inventoryDao.deleteInventory(ssId);
      rfidSessionDao.deleteAll(ssId);
      if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY.getValue()||sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY.getValue())
        AppDatabase.getDbInstance(context).BrandEansDao().deleteAll();
    }
  }
  
  /**
   * Delete session by type.
   *
   * @param sessionType the session type
   */
  public void deleteSessionByType(AppCommonMethods.SessionType sessionType){
    if(sessionType != null && sessionType.getValue() > 0){
      inventoryDao.deleteInventory(sessionType.getValue());
      rfidSessionDao.deleteAll(sessionType.getValue());
      if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY || sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY)
        AppDatabase.getDbInstance(context).BrandEansDao().deleteAll();
    }
  }
  
  /**
   * Get reader instance object.
   *
   * @return the object
   */
  public Object getReaderInstance(){
    if(readerRepository.getReaderInstance() != null){
      showLog("INST", "NOT NULL");
      return readerRepository.getReaderInstance();
    }
    else{
      
      return null;
    }
  }
  
  /**
   * Get reader uhf instance object.
   *
   * @param sessionType the session type
   * @return the object
   */
  public Object getReaderUHFInstance(AppCommonMethods.SessionType sessionType){return readerRepository.getReaderUHFInstance(sessionType);}
  
  /**
   * Get barcode reader instance object.
   *
   * @param sessionType the session type
   * @return the object
   */
  public Object getBarcodeReaderInstance(AppCommonMethods.SessionType sessionType){return readerRepository.getBarcodeReaderInstance(sessionType);}
  
  /**
   * Check and set reader.
   */
  public void checkAndSetReader(){readerRepository.checkAndSetReader();}
  
  /**
   * Check and connect reader.
   */
  public void checkAndConnectReader(){readerRepository.checkAndConnectReader();}
  
  /**
   * Configure device.
   *
   * @param sessionType the session type
   */
  public void configureDevice(AppCommonMethods.SessionType sessionType){
    readerRepository.configureReader(sessionType != null ? sessionType : MainRepository.this.sessionType);
  }
  
  /**
   * Update active session flag rfid session.
   *
   * @param sessionType the session type
   * @param isActive    the is active
   * @return the rfid session
   */
  public RFIDSession updateActiveSessionFlag(AppCommonMethods.SessionType sessionType, boolean isActive){
    if(rfidSessionDao != null && sessionType.getValue() > 0){
      try{
        rfidSessionDao.updateActiveSession(isActive, sessionType.getValue());
        if(isActive) return getCurrentSession(sessionType);
      }
      catch(Exception e){e.printStackTrace();}
    }
    return null;
  }
  
  /**
   * Get current session rfid session.
   *
   * @param sessionType the session type
   * @return the rfid session
   */
  public RFIDSession getCurrentSession(AppCommonMethods.SessionType sessionType){
    rfidSession = rfidSessionDao.getCurrentSession(sessionType.getValue());
    sessionId = rfidSession != null ? rfidSession.sessionId : null;
    this.sessionType = AppCommonMethods.SessionType.get(rfidSession != null ? rfidSession.sessionType : 0);
    sessionAction = AppCommonMethods.SessionAction.get(rfidSession != null ? rfidSession.sessionAction : 0);
    if(sessionId != null && this.sessionType.getValue() > 0){
      return rfidSessionDao.getSession(sessionId, this.sessionType.getValue());
    }
    return null;
  }
  
  /**
   * Generate offline session id string.
   *
   * @param type the type
   * @param cc   the cc
   * @return the string
   */
  public String generateOfflineSessionId(AppCommonMethods.SessionType type, Calendar cc){
    if(type.getValue() <= 0) return "";
    cc = cc == null ? Calendar.getInstance() : cc;
    String sessionId = type.name().toUpperCase().substring(0, 3).replaceFirst("BRA", "BRI").replaceFirst("STO", "STC").replaceFirst("OUT", "OTW");
    sessionId += SharedPrefManager.getIMEI();
    sessionId += cc.get(Calendar.YEAR);
    sessionId += Integer.toString(cc.get(Calendar.MONTH) + 1, 32).toUpperCase();
    sessionId += Integer.toString(cc.get(Calendar.DAY_OF_MONTH), 32).toUpperCase();
    sessionId += Integer.toString(cc.get(Calendar.HOUR_OF_DAY), 32).toUpperCase();
    sessionId += String.format("%01d", cc.get(Calendar.MINUTE));
    sessionId += String.format("%01d", cc.get(Calendar.SECOND));
    showLog("sessionId", sessionId);
    return sessionId;
  }
  
  /**
   * Start session.
   *
   * @param session          the session
   * @param isStartInventory the is start inventory
   */
  public void startSession(RFIDSession session, Set<String> eans, boolean isStartInventory){
    if(session == null) return;
    //check & insert session data
    if(session != null && session instanceof RFIDSession){
      sessionId = session.sessionId;
      sessionType = AppCommonMethods.SessionType.get(session.sessionType);
      sessionAction = AppCommonMethods.SessionAction.get(session.sessionAction);
      rfidSession = rfidSessionDao.getSession(sessionId, sessionType.getValue());
      if(rfidSession == null){
        rfidSession = new RFIDSession(sessionId, sessionType.getValue(), sessionAction.getValue());
        rfidSession.sessionStartTime = session.sessionStartTime;
        rfidSession.sessionStopTime = session.sessionStopTime;
        rfidSession.sessionValidTill = session.sessionValidTill;
      }
      rfidSession.zone = session.zone;
      rfidSession.zoneId = session.zoneId;
      rfidSession.destZone = session.destZone;
      rfidSession.destZoneId = session.destZoneId;
      rfidSession.brands = session.brands;
      rfidSession.category = session.category;
      rfidSession.eans = session.eans;
      rfidSession.brandEan = session.brandEan;
      rfidSession.extras = session.extras;
      rfidSession.total = session.total;
      rfidSession.isRunning = true;
      rfidSession.isUploading = false;
      rfidSessionDao.insert(rfidSession);
    }
    readerRepository.startSession(rfidSession, eans, isStartInventory);
  }
  
  /**
   * Stop session.
   *
   * @param session       the session
   * @param sessionType   the session type
   * @param isDiscardData the is discard data
   */
  public void stopSession(RFIDSession session, int sessionType, boolean isDiscardData){
    showLog("stopSession->isDiscardData",""+isDiscardData);
    if(session == null && this.rfidSession == null) session = this.rfidSession;
    if(session != null && session instanceof RFIDSession){
      RFIDSession rfidSession = session != null ? (RFIDSession) session : rfidSessionDao.getSession(this.sessionId, this.sessionType.getValue());
      if(rfidSession != null){
        rfidSession.sessionStopTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(Calendar.getInstance().getTime());//getCurrentTime
        rfidSession.isRunning = false;
        rfidSession.isUploading = !isDiscardData;
        rfidSessionDao.update(rfidSession);
        readerRepository.stopSession();
      }
    }
    if(isDiscardData){
      deleteSession(session, sessionType);
    }
  }
  
  /**
   * Start inventory.
   */
  public void startInventory(){readerRepository.startInventory();}
  
  
  /**
   * Start inventory.
   */
  public void startInventory(final boolean isHideUnencodedTags){readerRepository.startInventory(isHideUnencodedTags);}
  
  /**
   * Start inventory.
   */
  public void startInventory(List<String> listIgnoreEpcs){readerRepository.startInventory(listIgnoreEpcs);}
  
  /**
   * Start inventory.
   */
  public void startInventory(final boolean isHideUnencodedTags,List<String> listIgnoreEpcs){readerRepository.startInventory(isHideUnencodedTags);}
  
  /**
   * Perform barcode based search.
   *
   * @param barcode the barcode
   */
  public void performBarcodeBasedSearch(String barcode){readerRepository.performBarcodeBasedSearch(barcode);}
  
  /**
   * Perform barcode based search.
   *
   * @param barcode the barcode
   */
  public void performBarcodeBasedSearch(String barcode, boolean isLockSearchEPC){readerRepository.performBarcodeBasedSearch(barcode, isLockSearchEPC);}
  
  /**
   * Perform epc based search.
   *
   * @param epc the epc
   */
  public void performEPCBasedSearch(String epc){readerRepository.performEPCBasedSearch(epc);}
  
  /**
   * Perform tid based search.
   *
   * @param tid the tid
   */
  public void performTIDBasedSearch(String tid){readerRepository.performTIDBasedSearch(tid);}
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(final String barcode, final boolean isDecodeOnPick){readerRepository.performPick(barcode, isDecodeOnPick);}
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(final String barcode, final boolean isDecodeOnPick, final Integer pickPower){readerRepository.performPick(barcode, isDecodeOnPick,pickPower);}
  
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(final String barcode, final List<String> listEpcs){readerRepository.performPick(barcode,listEpcs);}
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(final String barcode, final boolean isPostPicked, final List<String> listEpcs){readerRepository.performPick(barcode, isPostPicked,listEpcs);}
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(final String barcode, final Integer pickPower,final List<String> listEpcs){readerRepository.performPick(barcode, pickPower,listEpcs);}
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(final String barcode, final boolean isDecodeOnPick, final Integer pickPower,final List<String> listEpcs){readerRepository.performPick(barcode, isDecodeOnPick,pickPower,listEpcs);}
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(final String barcode, final Integer pickPower,final boolean isPostPicked, final List<String> listEpcs){readerRepository.performPick(barcode,pickPower,isPostPicked,listEpcs);}
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performPick(final String barcode, final boolean isDecodeOnPick, final Integer pickPower,final boolean isPostPicked,final List<String> listEpcs){readerRepository.performPick(barcode, isDecodeOnPick,pickPower,isPostPicked,listEpcs);}
  
  /**
   * Perform pick.
   *
   * @param barcode the barcode
   */
  public void performTidPick(final String barcode, final List<String> tids){readerRepository.performTidPick(barcode, tids);}
  
  /**
   * Stop inventory.
   */
  public void stopInventory(){readerRepository.stopInventory();}
  
  /**
   * Set reader power.
   *
   * @param power the power
   */
  public void setReaderPower(int power){readerRepository.setReaderPower(power);}
  
  /**
   * On pause.
   */
  public void onPause(){
    readerRepository.onPause();
    //check if session type is search
    stopSensor();
  }
  
  /**
   * On resume.
   *
   * @param sessionType the session type
   */
  public void onResume(AppCommonMethods.SessionType sessionType){
    readerRepository.onResume(sessionType);
    //check if session type is search
    if(sensorRepository != null) sensorRepository.onResume();
    
  }
  
  /**
   * On destroy.
   */
  public void onDestroy(){
    readerRepository.onDestroy();
  }
  
  /**
   * Soft scan.
   */
  public void softScan(){readerRepository.softScan("");}
  
  /**
   * Soft scan.
   */
  public void softScan(final String scanType){readerRepository.softScan(scanType);}
  
  /**
   * Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  public void performEncoding(Inventory pickedTag){readerRepository.performEncoding(pickedTag);}
  
  /**
   * Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  public void performEncoding(Inventory pickedTag, final String currentTagPassword){readerRepository.performEncoding(pickedTag, currentTagPassword);}
  
  /**
   * Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  public void performEncoding(Inventory pickedTag, final Bundle extras){readerRepository.performEncoding(pickedTag,extras);}
  
  /**
   * Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  public void performEncoding(Inventory pickedTag, final String currentTagPassword, final Bundle extras){readerRepository.performEncoding(pickedTag, currentTagPassword,extras);}
  
  /**
   * Read Tag Current Password.
   *
   * @param pickedTag the picked tag
   */
  public void readTagCurrentPassword(Inventory pickedTag){readerRepository.readTagCurrentPassword(pickedTag);}
  
  /**
   * Perform encoding.
   *
   * @param listPickedTags the list picked tags
   */
  public void performEncoding(List<Inventory> listPickedTags){
    readerRepository.performEncoding(listPickedTags);
  }
  
  /**
   * Perform encoding.
   *
   * @param listPickedTags the list picked tags
   * @param sessionType    the session type
   */
  public void performEncoding(List<Inventory> listPickedTags, AppCommonMethods.SessionType sessionType){
    readerRepository.performEncoding(listPickedTags, sessionType);
  }
  
  /**
   * Perform decoding.
   *
   * @param pickedTag the picked tag
   */
  public void performDecoding(Inventory pickedTag){readerRepository.performDecoding(pickedTag);}
  
  /**
   * Perform decoding.
   *
   * @param pickedTag the picked tag
   */
  public void performDecoding(Inventory pickedTag,Bundle extras){readerRepository.performDecoding(pickedTag,extras);}
  
  /**
   * Perform decoding.
   *
   * @param listPickedTags the list picked tags
   */
  public void performDecoding(List<Inventory> listPickedTags){readerRepository.performDecoding(listPickedTags);}
  
  /**
   * Perform decoding.
   *
   * @param listPickedTags the list picked tags
   * @param sessionType    the session type
   */
  public void performDecoding(List<Inventory> listPickedTags, AppCommonMethods.SessionType sessionType){readerRepository.performDecoding(listPickedTags, sessionType);}
  
  /**
   * Perform decoding.
   *
   * @param sessionType the session type
   */
  public void performDecoding(AppCommonMethods.SessionType sessionType){readerRepository.performDecoding(sessionType);}
  
  /**
   * Is reader set mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isReaderSet(){return readerRepository.isReaderSet();}
  
  /**
   * Is device configured mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isDeviceConfigured(){return readerRepository.isDeviceConfigured();}
  
  /**
   * Reader power mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Integer> readerPower(){return readerRepository.readerPower();}
  
  /**
   * Read Tag Password mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<String> readTagPassword(){return readerRepository.readTagPassword();}
  
  /**
   * Is inventory on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isInventoryOn(){return readerRepository.isInventoryOn();}
  
  /**
   * Is search on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isSearchOn(){return readerRepository.isSearchOn();}
  
  /**
   * Search percentage mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Integer> searchPercentage(){return readerRepository.searchPercentage();}
  
  /**
   * Search rssi mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<String> searchRssi(){return readerRepository.searchRssi();}
  
  /**
   * Is pick on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isPickOn(){return readerRepository.isPickOn();}
  
  /**
   * Is encode on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isEncodeOn(){return readerRepository.isEncodeOn();}
  
  /**
   * Is encode on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isDecodeOn(){return readerRepository.isDecodeOn();}
  
  /**
   * Is encode done mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isEncodeDone(){return readerRepository.isEncodeDone();}
  
  /**
   * Is decode done mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isDecodeDone(){return readerRepository.isDecodeDone();}
  
  /**
   * Pick data mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Inventory> pickData(){return readerRepository.pickData();}
  
  /**
   * Pick data mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<List<Inventory>> pickedListData(){return readerRepository.pickedListData();}
  
  /**
   * Is session on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isSessionOn(){return readerRepository.isSessionOn();}
  
  /**
   * Is reader connected boolean.
   *
   * @return the boolean
   */
  public boolean isReaderConnected(){return readerRepository.isReaderConnected();}
  
  /**
   * Is reader present boolean.
   *
   * @return the boolean
   */
  public boolean isReaderPresent(boolean isReaderInstanceSet){return readerRepository.isReaderPresent(isReaderInstanceSet);}
  
  /**
   * Is process on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isProcessOn(){return readerRepository.isProcessOn();}
  
  /**
   * Is trigger pressed mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isTriggerPressed(){return readerRepository.isTriggerPressed();}
  
  /**
   * Is barcode on mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<Boolean> isBarcodeOn(){return readerRepository.isBarcodeOn();}
  
  /**
   * Barcode data mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<String> barcodeData(){return readerRepository.barcodeData();}
  
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
  public void callWebService(final CommonActivity commonActivity, final CommonFragment commonFragment, final String url, final JSONObject jsonRequest, final Bundle args, final boolean isRetry, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    AppCommonMethods.callWebService(commonActivity, commonFragment, url, jsonRequest, args, isRetry, progressMsg, isOfflineProcess, isDBProcess);
  }
  
}
