package com.itek.retail.reader.seuic;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUseShortRangeSearchForAll;
import static com.itek.retail.common.AppCommonMethods.isUseShortRangeSearchForOnlyGID;
import static com.itek.retail.common.AppCommonMethods.playSound;
import static com.itek.retail.common.AppCommonMethods.showShortToast;

import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.BrandEanDao;
import com.itek.retail.database.FIFODao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.TripInventory;
import com.itek.retail.model.UploadInventory;
import com.itek.retail.reader.MainReaderRepository;
import com.itek.retail.reader.RFIDHandler;
import com.itek.retail.reader.RFIDInitInterface;
import com.seuic.uhf.EPC;
import com.seuic.uhf.IReadTagsListener;
import com.seuic.uhf.UHFService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Seuic rfid handler.
 */
public class SeuicRFIDHandler extends RFIDHandler{
  
  private static final String TAG_LOG = "Seuic";
  final int encodeRetryLimit = 2;
  private final int MIN_POWER_TO_SET = 5;
  private final int MAX_POWER_TO_SET = 30;
  private final int SOUND_THRESHOLD = 8;
  public UHFService mReader;
  Set<EPC> pickUHFTags = new HashSet<>(0);
  private Boolean loopFlag = false;
  private final boolean isRegisterTagListener = Build.VERSION.SDK_INT > Build.VERSION_CODES.P;
  /**
   * String to hexadecimal array
   */
  public byte[] getHexByteArray(String hexString){
    byte[] buffer = new byte[hexString.length() / 2];
    if(hexString == null || hexString.equals("")){
      return null;
    }
    hexString = hexString.toUpperCase();
    int length = hexString.length() / 2;
    char[] hexChars = hexString.toCharArray();
    for(int i = 0; i < length; i++){
      int pos = i * 2;
      buffer[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
    }
    return buffer;
  }
  
  private byte charToByte(char c){
    return (byte) "0123456789ABCDEF".indexOf(c);
  }
  
  /**
   * String to hexadecimal array (Specified length)
   */
  public int getHexByteArray(String hexString, byte[] buffer, int nSize){
    
    hexString.replace(" ", "");
    if(nSize > hexString.length() / 2){
      nSize = hexString.length() / 2;
      if(hexString.length() == 1){
        nSize = 1;
        String str = "0";
        hexString = str + hexString;
      }
    }
    char[] hexChars = hexString.toCharArray();
    for(int i = 0; i < nSize; i++){
      int pos = i * 2;
      buffer[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
    }
    return nSize;
  }
  
  /**
   * byte array to hexadecimal string
   */
  public String getHexString(byte[] b, int length){
    return getHexString(b, length, " ").replaceAll("\\s+", "");
  }
  
  /**
   * Convert the specified splitter
   */
  public String getHexString(byte[] b, int length, String split){
    StringBuilder hex = new StringBuilder("");
    String temp = null;
    for(int i = 0; i < length; i++){
      temp = Integer.toHexString(b[i] & 0xFF);
      if(temp.length() == 1){
        temp = '0' + temp;
      }
      hex.append(temp + split);
    }
    return hex.toString().trim().toUpperCase();
  }
  
  @Override
  public void checkAndConnectReader(){
    if(mReader == null){
      isReaderSet.postValue(null);
      InitSDK(true);
    }
    else if(!mReader.isopen()){
      InitSDK(true);
    }
    else if(sessionType.getValue() > 0 && !chkNotNullTrue(isDeviceConfigured.getValue()))
      configureReader(sessionType);
  }
  
  @Override
  public void checkAndSetReader(){
    if(mReader == null){
      isReaderSet.postValue(null);
      InitSDK(true);
    }
    else if(!mReader.isopen()){
      InitSDK(true);
    }
    //else if(sessionType.getValue()>0 && !chkNotNullTrue(isDeviceConfigured.getValue())) configureReader(sessionType);
  }
  
  @Override
  public void InitSDK(){
    super.InitSDK();
    InitSDK(true);
  }
  
  @Override
  public void onCreate(CommonActivity activity, MainReaderRepository mainReaderRepository, RFIDInitInterface rfidInterface, AppCommonMethods.SessionType sessionType){
    super.onCreate(activity, mainReaderRepository, rfidInterface, sessionType);
  }
  
  @Override
  protected void performSearch(){
    sessionAction = AppCommonMethods.SessionAction.SEARCH;
    showLog("CMD", "" + isCommandForSearch);
    // check reader connection
    if(!isReaderConnected()){
      showLog("Reader", "NOT CONNECTED");
      return;
    }
    //setSession("S1", "A");
    setSession("S0", "A");
    if(mReader.setPower(MAX_POWER_TO_SET)){
      new Thread(){
        @Override
        public void run(){
          try{
            String finalsgtin = SEARCH_EPC;
            String header = finalsgtin.length() > 2 ? finalsgtin.substring(0, 2) : "";
            final boolean isNonStdEnc = finalsgtin.length() >= 32 && header.matches("(?i)(BC|0C|00)");
            if(isCommandForTIDSearch) addTidBasedFilters(SEARCH_TID);
            else if(isCommandForEPCSearch) addEpcBasedFilters(finalsgtin, isNonStdEnc);
            else addFilters(isNonStdEnc && finalsgtin.length() > 12 ? finalsgtin.substring(12) : finalsgtin, isNonStdEnc);
          }
          catch(Exception e){
            e.printStackTrace();
            stopInventory();
            if(AppCommonMethods.isShowReaderCommandFailToast)
              showShortToast(context, R.string.err_reader_fail);
          }
        }
      }.start();
    }
  }
  
  /**
   * add Filters.
   *
   * @param tag  the tag
   * @param isBc the is bc
   */
  @Override
  protected void addFilters(String tag, boolean isBc){
    //select tags that match the criteria
    try{
      //68 =>substring(9) 80=>substring(12)
      if(setFilter(1, isBc ? 6 : 0, tag)){
        readTag();
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Add epc basedfilters.
   *
   * @param tag  the tag
   * @param isBc the is bc
   */
  @Override
  protected void addEpcBasedFilters(String tag, boolean isBc){
    try{
      if(setFilter(1, 0, tag)){
        readTag();
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  protected void addTidBasedFilters(String tid){
    // Add state aware pre-filter
    try{
      if(setFilter(2, 0, tid)){
        readTag();
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  protected void startTidPick(String barcode, List<String> tids){
  
  }
  
  private boolean setFilter(final int filterBank, final int offSet, final String data){
    try{
      byte[] val = new byte[255];
      val[0] = (byte) filterBank;
      val[1] = (byte) offSet;
      val[2] = (byte) (chkNull(data, "").length() / 2);
      val[3] = (byte) 0;//mIsInvert;
      byte[] hexByteData = getHexByteArray(data);
      if(val[2] != hexByteData.length){ return false; }
      System.arraycopy(hexByteData, 0, val, 4, val[2]);
      return mReader.setParamBytes(UHFService.PARAMETER_TAG_FILTER, val);
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return false;
  }
  
  @Override
  public void configureReader(AppCommonMethods.SessionType sessionType){
    this.sessionType = this.sessionType.getValue() == 0 && sessionType != null && sessionType.getValue() > 0 ? sessionType : this.sessionType;
    AppCommonMethods.SessionType type = this.sessionType.getValue() > 0 || sessionType == null || sessionType.getValue() == 0 ? this.sessionType : sessionType != null ? sessionType : this.sessionType;
    if(!isReaderConnected()){
      showLog(TAG_LOG, "CONFIG FAIL");
      isDeviceConfigured.postValue(false);
      rfidInterface.RFIDInitializationStatus(false, "RFID initialization Failed", null);
    }
    else if(type != null && type.getValue() > 0){
      setProgressMessage("Please wait...\nConfiguring Reader...", true);
      showLog(TAG_LOG, "CONFIG SUCCESS");
      int maxPower = type == AppCommonMethods.SessionType.SCAN || type == AppCommonMethods.SessionType.VERIFY_ENCODING || type == AppCommonMethods.SessionType.ENCODING ? MIN_POWER_TO_SET * 2 : type == AppCommonMethods.SessionType.MOVEMENT || type == AppCommonMethods.SessionType.INWARD || type == AppCommonMethods.SessionType.OUTWARD ? MAX_POWER_TO_SET / 2 : MAX_POWER_TO_SET;
      showLog("maxpower", type.name() + ":" + maxPower);
      int power = chkZero(SharedPrefManager.getInt(type.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower);
      SharedPrefManager.setInt(type.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), power);
      showLog("config power", type.name() + ":" + power);
      setReaderPower(power);
      setSession("S0", "A");
      //clearFilters();
      isDeviceConfigured.postValue(true);
      setProgressMessage(false);
      rfidInterface.RFIDInitializationStatus(true, "", mReader);
    }
    else setProgressMessage(false);
  }
  
  private void setSession(String session, String invType){
    if(mReader != null){
      mReader.setParameters(UHFService.PARAMETER_INVENTORY_SESSION, UHFService.Session.valueOf(session).ordinal());
      mReader.setParameters(UHFService.PARAMETER_INVENTORY_SESSION_TARGET, UHFService.Target.valueOf(invType).ordinal());
    }
  }
  
  @Override
  public synchronized void setReaderPower(int power){
    final int oldPower = mReader.getPower();
    setProgressMessage("Setting Reader Power...", true);
    if(isReaderConnected() && power >= MIN_POWER_TO_SET && power <= MAX_POWER_TO_SET){
      new Handler().post(() -> handleReaderPowerCommand(oldPower, power));
    }
    else{
      setProgressMessage(false);
      readerPower.postValue(oldPower);
      SharedPrefManager.setReaderPower(oldPower);
      if(sessionType != null)
        SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), power);
    }
  }
  
  /**
   * Init sdk.
   *
   * @param isConfigureDevice the is configure device
   */
  public void InitSDK(boolean isConfigureDevice){
    setProgressMessage(true);
    
    if(mReader == null){
      try{
        mReader = UHFService.getInstance();
      }
      catch(Exception ex){
        ex.printStackTrace();
      }
    }
    if(mReader != null && !mReader.isopen()){
      setProgressMessage(false);
      new InitTask(isConfigureDevice).execute();
    }
    else if(mReader != null && mReader.isopen()) configureReader(sessionType);
    else{
      setProgressMessage(false);
      rfidInterface.RFIDInitializationStatus(false, "", null);
    }
  }
  
  @Override
  public void configureReader(){ configureReader(null); }
  
  @Override
  public boolean isReaderConnected(){
    return mReader != null && mReader.isopen();
  }
  
  private boolean isConnected(){
    return mReader != null && mReader.isopen();
  }
  
  @Override
  public boolean isReaderPresent(boolean isReaderInstanceSet){
    return mReader != null || isReaderInstanceSet;
  }
  
  /**
   * On resume.
   *
   * @param sessionType the session type
   */
  @Override
  public void onResume(AppCommonMethods.SessionType sessionType){
    super.onResume(sessionType);
    if(mReader == null){
      InitSDK(true);
    }
    else if(!mReader.isopen()){
      InitSDK(true);
    }
    else if(sessionType.getValue() > 0) configureReader(sessionType);
  }
  
  /**
   * On pause.
   */
  @Override
  public void onPause(){
    super.onPause();
    if(mReader != null && mReader.isopen()){
      if(isRegisterTagListener) mReader.unregisterReadTags(iReadTagsListener);
      mReader.close();
    }
  }
  
  /**
   * Handle reader power command.
   *
   * @param oldPower the old power
   * @param newPower the new power
   */
  private void handleReaderPowerCommand(int oldPower, int newPower){
    try{
      showLog("old & new power", "Old:" + oldPower + " New:" + newPower);
      if(mReader.setPower(newPower)){
        showLog("power", "set");
        setProgressMessage(false);
        readerPower.postValue(newPower);
        if(sessionType != null && sessionType.getValue() > 0 /*&& sessionType == AppCommonMethods.SessionType.MOVEMENT*/){
          showLog(sessionType.name() + "SetPower", "" + sessionType.name());
          SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), newPower);
        }
        SharedPrefManager.setReaderPower(newPower);
      }
      else{
        setProgressMessage(false);
        readerPower.postValue(oldPower);
        SharedPrefManager.setReaderPower(oldPower);
        if(sessionType != null)
          SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), newPower);
      }
      
    }
    catch(Exception e){
      e.printStackTrace();
      setProgressMessage(false);
      readerPower.postValue(oldPower);
      SharedPrefManager.setReaderPower(oldPower);
      if(sessionType != null)
        SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), newPower);
    }
  }
  
  /**
   * Store inventory data.
   *
   * @param uhftagInfo the uhftag info
   */
  private void storeInventoryData(final EPC uhftagInfo){
    try{
      if(isNonEmpty(sessionId) && sessionType.getValue() > 0){
          Inventory inventory = new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
          String epc = uhftagInfo.getId();
          inventory.pcdata = "";
          if(readPC){
            int index = 4;
            inventory.pcdata = epc.substring(0, index);
            epc = epc.substring(index);
          }
          inventory.tid = "";
          if(readTid){
            inventory.tid = uhftagInfo.getEmbeded();
          }
          inventory.epc = epc.toUpperCase();
          inventory.rssi = readRssi ? "" + uhftagInfo.rssi : "";
          inventory.zone = zone;
          inventory.zoneId = zoneId;
          try{
            inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
          }
          catch(Exception e){ e.printStackTrace(); }
          inventory.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
          if(/*!AppCommonMethods.isSetInwOnline &&*/ (sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD)){
            final String tripNo = SharedPrefManager.getTripNo();
            final String huNo = SharedPrefManager.getHuNo();
            final String ean = chkNull(inventory.ean, "").replaceFirst(AppConstants.UNKNOWN, AppConstants.NON_ENCODED);
            final TripInventoryDao tripInventoryDao = AppDatabase.getTripInventoryDao(context);
            final String articleCode = !chkNull(ean, AppConstants.NON_ENCODED).equalsIgnoreCase(AppConstants.NON_ENCODED) ? chkNull(tripInventoryDao.getArticleCode(ean, huNo, tripNo), AppConstants.EXTRA_EAN) : AppConstants.NON_ENCODED;
            final Integer originalEanQty = !chkNull(articleCode, AppConstants.NON_ENCODED).matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.EXTRA_EAN + ")") ? tripInventoryDao.getOriginalArticleQty(tripNo, huNo, articleCode) : 0;
            
            TripInventory tripInventory = new TripInventory(tripNo, SharedPrefManager.getDeliveryNo(), huNo);
            tripInventory.userAction = "RFID";
            tripInventory.ean = ean;
            tripInventory.eanQty = originalEanQty;
            tripInventory.tid = inventory.tid;
            tripInventory.epc = inventory.epc;
            tripInventory.rssi = inventory.rssi;
            tripInventory.isOriginal = originalEanQty > 0;
            tripInventory.articleCode = articleCode;
            tripInventory.isHardTag = inventory.isHardTag;
            if(!tripInventoryDao.isEpcPresent(tripNo, huNo, inventory.epc))
              tripInventoryDao.insertTripInventoryData(tripInventory);
          }
          else{
            if(isNullOrEmpty(sessionId) || (isNullOrEmpty(inventory.tid) && (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST)))
              throw new NullPointerException();
            showLog("insert", "" + inventory.ean);
            inventoryDao.insertInventoryData(inventory);
            final ProductDao productDao = AppDatabase.getProductDao(context);
            final FIFODao fifoDao = AppDatabase.getFIFODao(context);
            if(productDao != null && sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION){
              if(zone.equalsIgnoreCase(AppConstants.ALL)) productDao.updateFound(inventory.epc);
              else productDao.updateFound(inventory.epc, zone);
            }
            if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
              productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
            if(productDao != null && sessionType == AppCommonMethods.SessionType.OUTWARD_PICK)
              productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
            if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO && fifoDate != null && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK))
              fifoDao.updateFound(inventory.ean, inventory.epc, fifoDate);
            if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY)
              AppDatabase.getBrandEansDao(context).updateScanQty("," + inventory.ean + ",");
          }
      }
    }
    catch(SQLiteConstraintException e){
      e.printStackTrace();
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  protected Inventory getDataFromTagInfo(Object object){
    return object!=null && object instanceof EPC ? getDataFromTagInfo((EPC) object) : new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
  }
  
  /**
   * Get data from tag info inventory.
   *
   * @param uhftagInfo the uhftag info
   * @return the inventory
   */
  private Inventory getDataFromTagInfo(EPC uhftagInfo){
    Inventory inventory = new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
    String epc = uhftagInfo.getId();
    inventory.pcdata = "";
    if(readPC){
      int index = 4;
      inventory.pcdata = epc.substring(0, index);
      epc = epc.substring(index);
    }
    inventory.tid = "";
    if(readTid){
      inventory.tid = uhftagInfo.getEmbeded();
    }
    inventory.epc = epc.toUpperCase();
    inventory.rssi = readRssi ? "" + uhftagInfo.rssi : "";
    
    inventory.zone = zone;
    inventory.zoneId = zoneId;
    try{
      inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
    }
    catch(Exception e){ e.printStackTrace(); }
    return inventory;
  }
  
  @Override
  public Object getReaderInstance(CommonActivity context, MainReaderRepository mainReaderRepository, RFIDInitInterface rfidInitInterface){
    try{
      this.context = context;
      this.mainReaderRepository = mainReaderRepository;
      this.rfidInterface = rfidInitInterface;
      mReader = UHFService.getInstance();
    }
    catch(Exception ex){
      ex.printStackTrace();
    }
    return mReader;
  }
  
  @Override
  public boolean performPick(final String findBarcode, final boolean isDecodeOnPick, final Integer pickPower,final boolean isPostPicked,final List<String> pickedEpcs){
    final boolean isPerformPick = super.performPick(findBarcode, isDecodeOnPick,pickPower,isPostPicked,pickedEpcs);
    if(isPerformPick){
      pickTags.clear();
      pickUHFTags.clear();
      isPickOn.postValue(false);
      isActionPick = false;
      startPick(findBarcode, isDecodeOnPick,isPostPicked);
    }
    return isPerformPick;
  }
  
  @Override
  protected void startPick(final String findBarcode, final boolean isDecodeOnPick, final boolean isPostPicked){
    try{
      sessionAction = AppCommonMethods.SessionAction.PICK;
      int maxPower = chkZero(pickPower,sessionType == AppCommonMethods.SessionType.MOVEMENT ? MAX_POWER_TO_SET / 2 : MIN_POWER_TO_SET * 2);
      int power = /*isActionTidPick? MAX_POWER_TO_SET: */sessionType == AppCommonMethods.SessionType.MOVEMENT ? chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower) : maxPower;
      if(power > maxPower){
        power = maxPower;
      }
      showLog("power", "" + power);
      setSession("S0", "A");
      clearFilters();
      if(/*mReader.getPower() == power || */mReader.setPower(power)){
        readPC = true;
        readTid = true;
        isActionPick = true;
        isPickOn.postValue(true);
        SEARCH_BARCODE = findBarcode;
        readTag();
        if(loopFlag && isSinglePick){
          setProgressMessage(context.getString(R.string.msg_pick), true);
          pickTimer = new Timer();
          pickTimer.schedule(new TimerTask(){
            @Override
            public void run(){
              showLog("onFinish", "onFinish");
              showLog("pickTags", "" + pickTags.size());
              stopInventory();
              if(isNullOrEmpty(pickTags)){
                context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
                setProgressMessage(false);
              }
              else if(pickTags.size() > 1){
                setProgressMessage(false);
                context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
              }
              else if(pickTags.size() == 1){
                final EPC tagData = new ArrayList<EPC>(pickUHFTags).get(0);
                String epc = chkNull(tagData.getId(), "");
                String pc = "";
                String tid = "";
                readPC = true;
                readTid = true;
                if(readPC){
                  int index = 4;
                  pc = epc.substring(0, index);
                  epc = epc.substring(index);
                }
                if(readTid){
                  tid = tagData.getEmbeded();
                }
                final String epcdt = epc;
                AppCommonMethods.showLog("pc_epc_tid", pc + "_" + epcdt + "_" + tid);
                setProgressMessage(false);
                updateFoundWrittenTag(epcdt,tid);
                if(sessionType == AppCommonMethods.SessionType.OFF_RANGE && uploadInventoryDao.isEPCPresent(sessionType.getValue(), epcdt)){
                  AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_STOP (Tag Already Picked)");
                  final UploadInventory ui = uploadInventoryDao.getBysessionTypeAndEpc(sessionType.getValue(), epcdt);
                  if(ui != null && isNonEmpty(ui.remark) && isNonEmpty(ui.fifoDate))
                    context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag_in_carton), getTypeCharCode(), ui.remark));
                  else
                    context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
                  setProgressMessage(false);
                }
                else if(sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.DECODING && (inventoryDao.isEPCPresent(sessionId, epcdt)||(isNonEmpty(pickedEpcs) && pickedEpcs.contains(epcdt)))){
                  context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
                }
                else{
                  boolean isMatchingBarcode = false;
                  String matchingBarcode = "";
                  
                  //check by using getBarcode method instead of switch case
                  String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING);
                  String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context,findBarcode);
                  showLog("pick_barcode", barcode);
                  showLog("pick_compare_barcode", compbarcode);
                  final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                  if(!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase())) && (sessionType != AppCommonMethods.SessionType.SEARCH_FIFO || fifoDao.isEPCPresent(epcdt, ean, fifoDate/*,zone,zoneId*/))){
                    isMatchingBarcode = true;
                    matchingBarcode = AppCommonMethods.getLeftZeroReplacedString(context,barcode);
                  }
                  else if(sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.DECODING){
                    if(isNullOrEmpty(barcode) || barcode.equalsIgnoreCase(AppConstants.UNKNOWN) || barcode.equalsIgnoreCase(AppConstants.NON_ENCODED))
                      context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                    else if(isNonEmpty(barcode))
                      context.showCustomAlertDialog(String.format(context.getString(R.string.err_pick_wrong_tag_header), getTypeCharCode()), String.format(context.getString(R.string.err_pick_wrong_tag)/*,getTypeCharCode()*/, barcode), context.getString(R.string.btn_ok), null);
                  }
                  showLog("isMatchingBarcode", "" + isMatchingBarcode);
                  
                  if((isMatchingBarcode /*&& isNonEmpty(matchingBarcode)*/) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING){
                    readTid = true;
                    readRssi = true;
                    readEAN = true;
                    readPC = true;
                    clearFilters();
                    if(isPostPicked || sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING){
                      final Inventory pickedTag = getDataFromTagInfo(tagData);
                      if((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && isMatchingBarcode && isNonEmpty(matchingBarcode))
                        pickedTag.ean = AppCommonMethods.getLeftZeroReplacedString(context,matchingBarcode);
                      if(isNullOrEmpty(matchingBarcode) && sessionType != AppCommonMethods.SessionType.ENCODING && (sessionType != AppCommonMethods.SessionType.DECODING || !SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_NON_ENCODED_TAG_PICK_FOR_DECODE,AppCommonMethods.isAllowNonEncodedTagPickForDecode)))
                        context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                      else if((sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING) && (pickedTag == null || isNullOrEmpty(pickedTag.epc) || isNullOrEmpty(pickedTag.tid)))
                        context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
                      else pickData.postValue(pickedTag);
                    }
                    else{
                      storeInventoryData(tagData);
                      if(isDecodeOnPick){
                        final Inventory pickedTag = getDataFromTagInfo(tagData);
                        pickData.postValue(pickedTag);
                      }
                    }
                  }
                }
              }
            }
          },sessionType == AppCommonMethods.SessionType.ENCODING || (sessionType.equals(AppCommonMethods.SessionType.ENCODING_THAN) && !SharedPrefManager.getBoolean(ParamConstants.IS_SET_EXTRA_PICK_TIME_FOR_THAN_ENC, AppCommonMethods.isSetExtraPickTimeForThanEncoding)) || sessionType == AppCommonMethods.SessionType.DECODING ? pickCountDownTime / 4 : pickCountDownTime);
          /*pickCountDownTimer = new CountDownTimer(pickCountDownTime, 1000){
            @Override
            public void onTick(long l){
              showLog("onTick", "" + l);
            }
            
            @Override
            public void onFinish(){
              showLog("onFinish", "onFinish");
              showLog("pickTags", "" + pickTags.size());
              stopInventory();
              if(isNullOrEmpty(pickTags)){
                context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
                setProgressMessage(false);
              }
              else if(pickTags.size() > 1){
                setProgressMessage(false);
                context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
              }
              else if(pickTags.size() == 1){
                final EPC tagData = new ArrayList<EPC>(pickUHFTags).get(0);
                String epc = chkNull(tagData.getId(), "");
                String pc = "";
                String tid = "";
                readPC = true;
                readTid = true;
                if(readPC){
                  int index = 4;
                  pc = epc.substring(0, index);
                  epc = epc.substring(index);
                }
                if(readTid){
                  tid = tagData.getEmbeded();
                }
                final String epcdt = epc;
                AppCommonMethods.showLog("pc_epc_tid", pc + "_" + epcdt + "_" + tid);
                setProgressMessage(false);
                updateFoundWrittenTag(epcdt,tid);
                if(sessionType == AppCommonMethods.SessionType.OFF_RANGE && uploadInventoryDao.isEPCPresent(sessionType.getValue(), epcdt)){
                  AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_STOP (Tag Already Picked)");
                  final UploadInventory ui = uploadInventoryDao.getBysessionTypeAndEpc(sessionType.getValue(), epcdt);
                  if(ui != null && isNonEmpty(ui.remark) && isNonEmpty(ui.fifoDate))
                    context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag_in_carton), getTypeCharCode(), ui.remark));
                  else
                    context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
                  setProgressMessage(false);
                }
                else if(sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.DECODING && (inventoryDao.isEPCPresent(sessionId, epcdt)||(isNonEmpty(pickedEpcs) && pickedEpcs.contains(epcdt)))){
                  context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
                }
                else{
                  boolean isMatchingBarcode = false;
                  String matchingBarcode = "";
                  
                  //check by using getBarcode method instead of switch case
                  String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING);
                  String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context,findBarcode);
                  showLog("pick_barcode", barcode);
                  showLog("pick_compare_barcode", compbarcode);
                  final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                  if(!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase())) && (sessionType != AppCommonMethods.SessionType.SEARCH_FIFO || fifoDao.isEPCPresent(epcdt, ean, fifoDate*//*,zone,zoneId*//*))){
                    isMatchingBarcode = true;
                    matchingBarcode = AppCommonMethods.getLeftZeroReplacedString(context,barcode);
                  }
                  else if(sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.DECODING){
                    if(isNullOrEmpty(barcode) || barcode.equalsIgnoreCase(AppConstants.UNKNOWN) || barcode.equalsIgnoreCase(AppConstants.NON_ENCODED))
                      context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                    else if(isNonEmpty(barcode))
                      context.showCustomAlertDialog(String.format(context.getString(R.string.err_pick_wrong_tag_header), getTypeCharCode()), String.format(context.getString(R.string.err_pick_wrong_tag)*//*,getTypeCharCode()*//*, barcode), context.getString(R.string.btn_ok), null);
                  }
                  showLog("isMatchingBarcode", "" + isMatchingBarcode);
                  
                  if((isMatchingBarcode *//*&& isNonEmpty(matchingBarcode)*//*) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING){
                    readTid = true;
                    readRssi = true;
                    readEAN = true;
                    readPC = true;
                    clearFilters();
                    if(isPostPicked || sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING){
                      final Inventory pickedTag = getDataFromTagInfo(tagData);
                      if((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && isMatchingBarcode && isNonEmpty(matchingBarcode))
                        pickedTag.ean = AppCommonMethods.getLeftZeroReplacedString(context,matchingBarcode);
                      if(isNullOrEmpty(matchingBarcode) && sessionType != AppCommonMethods.SessionType.ENCODING && (sessionType != AppCommonMethods.SessionType.DECODING || !SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_NON_ENCODED_TAG_PICK_FOR_DECODE,AppCommonMethods.isAllowNonEncodedTagPickForDecode)))
                        context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                      else if((sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING) && (pickedTag == null || isNullOrEmpty(pickedTag.epc) || isNullOrEmpty(pickedTag.tid)))
                        context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
                      else pickData.postValue(pickedTag);
                    }
                    else{
                      storeInventoryData(tagData);
                      if(isDecodeOnPick){
                        final Inventory pickedTag = getDataFromTagInfo(tagData);
                        pickData.postValue(pickedTag);
                      }
                    }
                  }
                }
              }
            }
          };
          pickCountDownTimer.start();*/
        }
      }
      else{
        AppCommonMethods.showLog("setPower", "fail");
      }
    }
    catch(Exception e){
      e.printStackTrace();
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast)
        showShortToast(context, R.string.err_reader_fail);
    }
  }
  
  @Override
  public void performEncoding(Inventory pickedTag, String currentTagPassword){
    final Inventory pickTag = context.epcEncoderDecoder.setPCDataBeforeEncoding(pickedTag);
    if(pickTag == null) return;
    else if(chkNull(pickTag.tid, "").length() >= 8){
      if(SharedPrefManager.getNon128BitTids().contains(pickTag.tid.toUpperCase().substring(0, 8)) && pickTag.newEpc.length() > 28){
        context.showCustomErrDialog(context.getString(R.string.err_encoding_non_std_not_allowed_for_non_128_bit_tids));
        return;
      }
    }
    final boolean isMultiEncode = multiWriteListSize > 0;
    sessionAction = AppCommonMethods.SessionAction.ENCODE;
    if(isMultiEncode || mReader.setPower(MAX_POWER_TO_SET)){
      if(!isMultiEncode){
        clearFilters();
        isEncodeOn.postValue(true);
        isMultiWriteDone = false;
        setProgressMessage(context.getString(R.string.msg_pick), true);
      }
      startEncoding(pickTag, currentTagPassword, 0);
    }
  }
  
  @Override
  public void performEncoding(List<Inventory> pickedTags, AppCommonMethods.SessionType sessionType){
    showLog("pickedTags", "" + pickedTags.size());
    showLog("sessionType", "" + sessionType.name());
    if(isNonEmpty(pickedTags) && sessionType.getValue() > 0){
      this.sessionType = sessionType;
      sessionAction = AppCommonMethods.SessionAction.ENCODE;
      multiWriteSuccessCount = 0;
      multiWriteListSize = pickedTags.size();
      multiWriteCount = multiWriteListSize;
      isMultiWriteDone = false;
      if(mReader.setPower(MAX_POWER_TO_SET)){
        clearFilters();
        isEncodeOn.postValue(true);
        setProgressMessage(context.getString(R.string.msg_pick), true);
        for(Inventory inventory : pickedTags){
          if(inventory != null && isNonEmpty(inventory.epc) && isNonEmpty(inventory.newEpc) && context.epcEncoderDecoder.isValidHeader(inventory.newEpc)){
            performEncoding(inventory);
          }
          else multiWriteCount--;
        }
        if(multiWriteCount == 0){
          //err_encoding_write_fail
          isEncodeOn.postValue(false);
          setProgressMessage(false);
        }
      }
    }
  }
  
  private synchronized void startEncoding(final Inventory pickedTag, final String currentTagPassword, final int encodeRetryCount){
    showLog("startEncoding", currentTagPassword + "_" + encodeRetryCount);
    final String barcode = pickedTag.ean;
    final String epc = pickedTag.epc;
    final String tid = pickedTag.tid;
    final String sgtin = pickedTag.newEpc.trim();
    final String pc = pickedTag.pcdata;
    int offSet = 0;
    int dataLen = 0;
    showLog("ean", ean);
    showLog("epc", epc);
    showLog("sgtin", sgtin);
    showLog("tid", tid);
    showLog("pc", pc);
    switch(sgtin.length()){
      case 28:
      case 36:
      case 24:
      case 32:
        dataLen = sgtin.length() / 2;
        offSet = ((sgtin.length() / 4) % 2 == 0 ? 2 : 1) * 2;
    }
    showLog("dataLen", "" + dataLen);
    showLog("offSet", "" + offSet);
    showLog("currentAccessPassword", "" + SharedPrefManager.getCurrentAccessPassword());
    if(offSet > 0 && dataLen > 0){
      byte[] epcDataBytes = getHexByteArray(epc);
      byte[] sgtinDataBytes = getHexByteArray(sgtin);
      
      if(SharedPrefManager.getNonPasswordTids().contains(tid.toUpperCase().substring(0, 8))){
        final List<String> listPasswords = SharedPrefManager.getOldAccessPasswords();
        
        if(listPasswords.contains(SharedPrefManager.getCurrentAccessPassword()))
          listPasswords.remove(SharedPrefManager.getCurrentAccessPassword());
        listPasswords.add(0, SharedPrefManager.getCurrentAccessPassword());
        if(listPasswords.contains(defaultTagZeroPassword))
          listPasswords.remove(defaultTagZeroPassword);
        listPasswords.add(0, defaultTagZeroPassword);
        
        boolean isWriteSuccess = false;
        for(String pass : listPasswords){
          showLog("pass", pass);
          if(isNonEmpty(pass) && mReader.writeTagData(epcDataBytes, getHexByteArray(pass), 1, offSet, dataLen, sgtinDataBytes)){
            showLog("WriteSuccess", "true");
            //insert Data in table.
            isWriteSuccess = true;
            try{
              pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
              if(pickedTag.newEpc.trim().matches("^(" + pc + ").*$"))
                pickedTag.newEpc = pickedTag.newEpc.replaceFirst(pc, "").trim();
              if(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING) && isNonEmpty(fifoDate))
                pickedTag.fifoDate = fifoDate;
              if(SharedPrefManager.getBoolean(ParamConstants.IS_TID_BASED_COUNT_FOR_ENCODE) && inventoryDao.isTidPresent(sessionId, tid)){
                pickedTag.ino = chkNull(pickedTag.ino, chkNull(inventoryDao.getRowIdFromTid(sessionId, tid), 0));
                pickedTag.isFound = true;
                pickedTag.isUploaded = false;
                int updatedRows = inventoryDao.updateInventoryData(pickedTag);
                //showLog("updatedRows",""+updatedRows);
              }
              else inventoryDao.insertInventoryData(pickedTag);
              try{
                uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
              }
              catch(Exception e){ e.printStackTrace(); }
              AppCommonMethods.successBeep();
              updateTagWriteCount(true);
            }
            catch(SQLiteConstraintException e){ e.printStackTrace(); }
            catch(Exception e){
              e.printStackTrace();
            }
            break;
          }
        }
        if(!isWriteSuccess){
          updateTagWriteCount(context.getString(R.string.err_encoding_auth_fail));
          showLog("LOCKMEMORY1", "FAIL");
        }
      }
      else if(mReader.writeTagData(epcDataBytes, getHexByteArray(SharedPrefManager.getCurrentAccessPassword()), 1, offSet, dataLen, sgtinDataBytes)){
        showLog("WriteSuccess", "true");
        //insert Data in table.
        try{
          pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
          if(pickedTag.newEpc.trim().matches("^(" + pc + ").*$"))
            pickedTag.newEpc = pickedTag.newEpc.replaceFirst(pc, "").trim();
          if(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING) && isNonEmpty(fifoDate))
            pickedTag.fifoDate = fifoDate;
          if(SharedPrefManager.getBoolean(ParamConstants.IS_TID_BASED_COUNT_FOR_ENCODE) && inventoryDao.isTidPresent(sessionId, tid)){
            pickedTag.ino = chkNull(pickedTag.ino, chkNull(inventoryDao.getRowIdFromTid(sessionId, tid), 0));
            pickedTag.isFound = true;
            pickedTag.isUploaded = false;
            try{
              int updatedRows = inventoryDao.updateInventoryData(pickedTag);
              //showLog("updatedRows",updatedRows);
            }
            catch(Exception e){
              e.printStackTrace();
            }
          }
          else inventoryDao.insertInventoryData(pickedTag);
          try{
            uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
          }
          catch(Exception e){ e.printStackTrace(); }
          AppCommonMethods.successBeep();
          updateTagWriteCount(true);
        }
        catch(SQLiteConstraintException e){ e.printStackTrace(); }
        catch(Exception e){
          e.printStackTrace();
        }
      }
      else if(isNonEmpty(currentTagPassword)){
        if(encodeRetryCount <= encodeRetryLimit){
          startEncoding(pickedTag, currentTagPassword, encodeRetryCount + 1);
        }
        else{
          updateTagWriteCount(context.getString(R.string.err_encoding_auth_fail));
          showLog("RETRY_LIMIT_REACHED", "FAIL");
        }
      }
      else{
        List<String> passwords = SharedPrefManager.getOldAccessPasswords();
        if(passwords.contains(defaultTagZeroPassword)) passwords.remove(defaultTagZeroPassword);
        passwords.add(0, defaultTagZeroPassword);
        boolean isPasswordFound = false;
        for(String oldPassword : passwords){
          if(isNonEmpty(oldPassword)){
            showLog("oldPassword", oldPassword);
            byte[] buffer = new byte[256];
            String reservedMemoryPassword = "";
            if(mReader.readTagData(epcDataBytes, getHexByteArray(oldPassword), 0, 4, 4, buffer)){
              reservedMemoryPassword = getHexString(buffer, 4);
              if(reservedMemoryPassword != null){
                showLog("reservedMemoryPassword", reservedMemoryPassword);
                if(reservedMemoryPassword.equalsIgnoreCase(oldPassword)){
                  isPasswordFound = true;
                  if(reservedMemoryPassword.equalsIgnoreCase(SharedPrefManager.getCurrentAccessPassword())){
                    startEncoding(pickedTag, reservedMemoryPassword, encodeRetryCount);
                  }
                  else writePasswordAndLock(pickedTag, reservedMemoryPassword, encodeRetryCount);
                  break;
                }
              }
            }
          }
        }
        if(!isPasswordFound){
          showLog("LOCKMEMORY1", "FAIL");
          updateTagWriteCount(context.getString(R.string.err_encoding_auth_fail));
        }
      }
    }
    else{
      updateTagWriteCount(context.getString(R.string.err_encoding_write_fail));
    }
  }
  
  /**
   * Write password and lock.
   *
   * @param pickedTag          the picked tag
   * @param currentTagPassword the old password
   */
  private synchronized void writePasswordAndLock(final Inventory pickedTag, final String currentTagPassword, final int encodeRetryCount){
    showLog("Write_Lock_CurrentTagPassword_retryCount", currentTagPassword + "_" + encodeRetryCount);
    byte[] epcDataBytes = getHexByteArray(pickedTag.epc);
    byte[] accessPassDataBytes = getHexByteArray(SharedPrefManager.getCurrentAccessPassword());
    
    if(mReader.writeTagData(epcDataBytes, getHexByteArray(currentTagPassword), 0, 4, 4, accessPassDataBytes)){
      if(SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase(defaultTagZeroPassword) || mReader.lockTag(epcDataBytes, accessPassDataBytes, 1)){
        startEncoding(pickedTag, currentTagPassword, encodeRetryCount);
      }
      else{
        //TODO Update Fail Reason for Encoding also
        updateTagWriteCount(context.getString(R.string.err_encoding_fail));
        showLog("LOCKMEMORY", "FAIL");
      }
    }
    else{
      updateTagWriteCount(context.getString(R.string.err_encoding_write_fail));
      showLog("WRITECURRENTPASSWORD", "FAIL");
    }
  }
  
  @Override
  public void readTagCurrentPassword(Inventory pickedTag){
  
  }
  
  @Override
  public void performDecoding(Inventory pickedTag){
    if(pickedTag == null) return;
    sessionAction = AppCommonMethods.SessionAction.DECODE;
    boolean allowWrite = false;
    final boolean isMultiDecode = multiWriteListSize > 0;
    if(isMultiDecode || mReader.setPower(MAX_POWER_TO_SET)){
      if(!isMultiDecode){
        clearFilters();
        isDecodeOn.postValue(true);
        isMultiWriteDone = false;
        setProgressMessage(context.getString(R.string.msg_pick), true);
      }
      final String barcode = pickedTag.ean;
      final String epc = pickedTag.epc;
      final String tid = pickedTag.tid;
      final String sgtin = (isNonEmpty(pickedTag.newEpc) && pickedTag.newEpc.startsWith("0") ? pickedTag.newEpc : !epc.startsWith("0") ? "0" + epc.substring(1) : epc).trim();
      final String pc = pickedTag.pcdata;
      int offSet = 0;
      int dataLen = 0;
      showLog("ean", barcode);
      showLog("epc", epc);
      showLog("sgtin", sgtin);
      showLog("tid", tid);
      showLog("pc", pc);
      switch(sgtin.length()){
        case 28:
        case 36:
        case 24:
        case 32:
          dataLen = sgtin.length() / 2;
          offSet = ((sgtin.length() / 4) % 2 == 0 ? 2 : 1) * 2;
          allowWrite = offSet > 0 && dataLen > 0;
          break;
        default:
          allowWrite = false;
      }
      showLog("dataLen", "" + dataLen);
      showLog("offSet", "" + offSet);
      showLog("sgtin", "" + sgtin);
      showLog("tid", "" + tid);
      showLog("currentAccessPassword", "" + SharedPrefManager.getCurrentAccessPassword());
      if(allowWrite && offSet > 0 && dataLen > 0){
        byte[] epcDataBytes = getHexByteArray(epc);
        byte[] sgtinDataBytes = getHexByteArray(sgtin);
        
        if(SharedPrefManager.getNonPasswordTids().contains(tid.toUpperCase().substring(0, 8))){
          final List<String> listPasswords = SharedPrefManager.getOldAccessPasswords();
          
          if(listPasswords.contains(SharedPrefManager.getCurrentAccessPassword()))
            listPasswords.remove(SharedPrefManager.getCurrentAccessPassword());
          listPasswords.add(0, SharedPrefManager.getCurrentAccessPassword());
          if(listPasswords.contains(defaultTagZeroPassword))
            listPasswords.remove(defaultTagZeroPassword);
          listPasswords.add(0, defaultTagZeroPassword);
          
          boolean isWriteSuccess = false;
          for(String pass : listPasswords){
            showLog("pass", pass);
            if(isNonEmpty(pass) && mReader.writeTagData(epcDataBytes, getHexByteArray(pass), 1, offSet, dataLen, sgtinDataBytes)){
              showLog("WriteSuccess", "true");
              isWriteSuccess = true;
              pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
              pickedTag.isUploaded = false;
              pickedTag.newEpc = sgtin.trim();
              if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST)
                inventoryDao.updateInventoryData(pickedTag);
              else inventoryDao.insertInventoryData(pickedTag);
              try{
                uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
              }
              catch(Exception e){ e.printStackTrace(); }
              AppCommonMethods.successBeep();
              final ProductDao productDao = AppDatabase.getProductDao(context);
              final FIFODao fifoDao = AppDatabase.getFIFODao(context);
              if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
                productDao.updateDecodedEPC(pickedTag.epc, pickedTag.ean, pickedTag.zone);
              else if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
                fifoDao.updateDecoded(pickedTag.ean, pickedTag.epc, fifoDate);
              updateTagWriteCount(true);
              break;
            }
          }
          if(!isWriteSuccess){
            updateTagWriteCount(context.getString(R.string.err_decoding_fail));
            showLog("LOCKMEMORY1", "FAIL");
          }
        }
        else if(mReader.writeTagData(epcDataBytes, getHexByteArray(SharedPrefManager.getCurrentAccessPassword()), 1, offSet, dataLen, sgtinDataBytes)){
          showLog("WriteSuccess", "true");
          //update Data in table.
          pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
          pickedTag.isUploaded = false;
          pickedTag.newEpc = sgtin.trim();
          if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST)
            inventoryDao.updateInventoryData(pickedTag);
          else inventoryDao.insertInventoryData(pickedTag);
          
          try{
            uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
          }
          catch(Exception e){ e.printStackTrace(); }
          AppCommonMethods.successBeep();
          final ProductDao productDao = AppDatabase.getProductDao(context);
          final FIFODao fifoDao = AppDatabase.getFIFODao(context);
          if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
            productDao.updateDecodedEPC(pickedTag.epc, pickedTag.ean, pickedTag.zone);
          else if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
            fifoDao.updateDecoded(pickedTag.ean, pickedTag.epc, fifoDate);
          updateTagWriteCount(true);
        }
        else{
          showLog("CurrentPasswordWrite", "failed");
          List<String> passwords = SharedPrefManager.getOldAccessPasswords();
          if(passwords.contains(defaultTagZeroPassword)) passwords.remove(defaultTagZeroPassword);
          passwords.add(0, defaultTagZeroPassword);
          boolean isPasswordFound = false;
          for(String oldPassword : passwords){
            if(isNonEmpty(oldPassword)){
              showLog("oldPassword", oldPassword);
              if(mReader.writeTagData(epcDataBytes, getHexByteArray(oldPassword), 1, offSet, dataLen, sgtinDataBytes)){
                isPasswordFound = true;
                showLog("WriteSuccess", "true");
                //update Data in table.
                pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                pickedTag.isUploaded = false;
                pickedTag.newEpc = sgtin.trim();
                if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST)
                  inventoryDao.updateInventoryData(pickedTag);
                else inventoryDao.insertInventoryData(pickedTag);
                try{
                  uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
                }
                catch(Exception e){ e.printStackTrace(); }
                AppCommonMethods.successBeep();
                try{
                  final ProductDao productDao = AppDatabase.getProductDao(context);
                  final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                  if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
                    productDao.updateDecodedEPC(pickedTag.epc, pickedTag.ean, pickedTag.zone);
                  else if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
                    fifoDao.updateDecoded(pickedTag.ean, pickedTag.epc, fifoDate);
                }
                catch(Exception e){ e.printStackTrace(); }
                updateTagWriteCount(true);
              }
            }
          }
          if(!isPasswordFound){
            if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST){
              pickedTag.writeFailReason = "Auth Fail";
              pickedTag.retryWriteCount = pickedTag.retryWriteCount + 1;
              inventoryDao.updateInventoryData(pickedTag);
            }
            updateTagWriteCount(context.getString(R.string.err_decoding_auth_fail));
            showLog("LOCKMEMORY1", "FAIL");
          }
        }
      }
      else{
        if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST){
          pickedTag.writeFailReason = "Invalid New EPC Length : " + sgtin.length();
          pickedTag.retryWriteCount = pickedTag.retryWriteCount + 1;
          inventoryDao.updateInventoryData(pickedTag);
        }
        updateTagWriteCount(context.getString(R.string.err_decoding_write_fail));
        showLog("LOCKMEMORY1", "FAIL");
      }
    }
  }
  
  @Override
  public void performDecoding(List<Inventory> pickedTags, AppCommonMethods.SessionType sessionType){
    showLog("pickedTags", "" + pickedTags.size());
    showLog("sessionType", "" + sessionType.name());
    if(isNonEmpty(pickedTags) && sessionType.getValue() > 0){
      this.sessionType = sessionType;
      sessionAction = AppCommonMethods.SessionAction.DECODE;
      multiWriteSuccessCount = 0;
      multiWriteListSize = pickedTags.size();
      multiWriteCount = multiWriteListSize;
      isMultiWriteDone = false;
      if(mReader.setPower(MAX_POWER_TO_SET)){
        clearFilters();
        isDecodeOn.postValue(true);
        setProgressMessage(context.getString(R.string.msg_pick), true);
        for(Inventory inventory : pickedTags){
          if(inventory != null && !inventory.isDecoded() && isNonEmpty(inventory.epc) && !inventory.epc.startsWith("0")){
            performDecoding(inventory);
          }
          else multiWriteCount--;
        }
        if(multiWriteCount == 0){
          isDecodeOn.postValue(false);
          setProgressMessage(false);
        }
      }
    }
  }
  
  @Override
  public synchronized void performDecoding(final AppCommonMethods.SessionType sessionType){ performDecoding(inventoryDao.getAllNonDecodedInventoryData(sessionType.getValue()), sessionType); }
  
  /**
   * On destroy.
   */
  public void onDestroy(){
    new Handler().post(new Runnable(){
      @Override
      public void run(){
        if(mReader != null){
          if(isRegisterTagListener) mReader.unregisterReadTags(iReadTagsListener);
          mReader.close();
          showLog("mReader", "free!");
        }
        if(beepTimer != null) beepTimer.cancel();
      }
    });
    super.onDestroy();
  }
  
  @Override
  public boolean performInventory(final boolean isHideUnencodedTags,final List<String> listIgnoreEPCs){
    if(super.performInventory(isHideUnencodedTags,listIgnoreEPCs)){
      setSession("S1", "A");
      clearFilters();
      sessionAction = AppCommonMethods.SessionAction.INVENTORY;
      readTag();
      return true;
    }
    return false;
  }
  
  @Override
  public void stopInventory(){
    loopFlag = false;
    super.stopInventory();
    if (pickCountDownTimer != null) pickCountDownTimer.cancel();
    if (pickTimer != null) {
      pickTimer.cancel();
      pickTimer = null;
    }
    if(mReader.inventoryStop()){
      if(isRegisterTagListener) mReader.unregisterReadTags(iReadTagsListener);
    }
  }
  
  /**
   * Read tag.
   */
  private void readTag(){
    try{
      if(!loopFlag){
        byte[] embd = null;
        if(readTid){
          embd = new byte[255];
          embd[0] = (byte) 2;//Bank
          embd[1] = (byte) 0;//Offset;
          embd[2] = (byte) 12;//Length;
          System.arraycopy(getHexByteArray(defaultTagZeroPassword), 0, embd, 3, 4);
        }
        mReader.setParamBytes(UHFService.PARAMETER_TAG_EMBEDEDDATA, readTid ? embd : null);
        mReader.setParameters(UHFService.PARAMETER_HIDE_PC, readPC ? 0 : 1);
        if(isRegisterTagListener) mReader.registerReadTags(iReadTagsListener);
        if(mReader.inventoryStart()){
          loopFlag = true;
          if(isCommandForSearch) isSearchOn.postValue(true);
          else if(!isActionPick) isInventoryOn.postValue(true);
          startTimer();
          if(!isRegisterTagListener) new TagThread().start();
        }
        else{
          loopFlag = false;
          stopInventory();
        }
      }
      else{
        loopFlag = false;
        stopInventory();
      }
    }catch(Exception e){e.printStackTrace();}
  }
  
  private void clearFilters(){
    mReader.setParameters(UHFService.PARAMETER_CLEAR_EPCLIST_WHEN_START_INVENTORY, 1);
    mReader.setParameters(UHFService.PARAMETER_CLEAR_EPCLIST, 1);
    mReader.setParameters(UHFService.PARAMETER_EXTENSIONS_TAGFOCUS, 0);
    mReader.setParameters(UHFService.PARAMETER_EXTENSIONS_FASTID, 0);
    mReader.setParameters(UHFService.PARAMETER_HIDE_PC, 1);
  }
  
  /**
   * The Init task.
   */
  public class InitTask extends AsyncTask<String, Integer, Boolean>{
    
    boolean isConfigureDevice;
    
    /**
     * Instantiates a new Init task.
     *
     * @param isConfigureDevice the is configure device
     */
    InitTask(boolean isConfigureDevice){ this.isConfigureDevice = isConfigureDevice; }
    
    @Override
    protected Boolean doInBackground(String... params){
      return mReader.open();
    }
    
    @Override
    protected void onPostExecute(Boolean result){
      super.onPostExecute(result);
      setProgressMessage(false);
      if(!result){
        showLog(TAG_LOG, "INIT FAIL");
        int maxPower = sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING ? MIN_POWER_TO_SET * 2 : sessionType == AppCommonMethods.SessionType.MOVEMENT || sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD ? MAX_POWER_TO_SET / 2 : MAX_POWER_TO_SET;
        int power = chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower);
        showLog("maxPower", "" + maxPower);
        showLog("power", "" + power);
        setReaderPower(power);
        rfidInterface.RFIDInitializationStatus(false, "RFID initialization Failed", null);
      }
      else{
        showLog(TAG_LOG, "INIT SUCCESS");
        if(mReader != null){
          if(isRegisterTagListener) mReader.registerReadTags(iReadTagsListener);
          final String sdkVersion= mReader.getFirmwareVersion();
          if(isNonEmpty(sdkVersion) && !SharedPrefManager.getReaderSDKVersion().equalsIgnoreCase(sdkVersion))
            SharedPrefManager.setReaderSDKVersion(sdkVersion);
          configureReader(sessionType);
        }
      }
    }
    
    @Override
    protected void onPreExecute(){
      super.onPreExecute();
      setProgressMessage(true);
    }
  }
  
  @Override
  protected void saveSerialNo(){
    if(isNonEmpty(SharedPrefManager.getString(ParamConstants.DEVICE_SERIAL,""))) return;
    //TODO implement
  }
  
  /**
   * The Tag thread.
   */
  class TagThread extends Thread{
    
    @Override
    public void run(){
      //EPC uhftagInfo = null;
      while(loopFlag){
        final List<EPC> list = mReader.getTagIDs();
        for(EPC uhftagInfo : list){
          if(!loopFlag) break;
          else processTagData(uhftagInfo);
        }
      }
    }
  }
  
  private void processTagData(EPC uhftagInfo){
    String epcdt = "";
    String tid = "";
    String pc = "";
    try{
      //uhftagInfo = mReader.getTagIDs();
      if(uhftagInfo == null) AppCommonMethods.showLog("tag", "null");
      epcdt = uhftagInfo != null ? chkNull(uhftagInfo.getId(), "") : "";
      if(readPC){
        int index = 4;
        pc = epcdt.substring(0, index);
        epcdt = epcdt.substring(index);
        showLog("PC", pc);
      }
      if(readTid){
        tid = uhftagInfo.getEmbeded();//readTid(epcdt);
      }
      if(isActionTidPick)
        AppCommonMethods.showLog("tid", uhftagInfo != null ? chkNull(tid, "null") : "tag null");
      if(isNonEmpty(epcdt))
        AppCommonMethods.logInFile(context, sessionType.name() + "_TAG_EPC_" + chkNull(epcdt, "") + (sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION ? "_Status_" + AppDatabase.getProductDao(context).isEPCPresent(chkNull(epcdt, "")) : ""));
    }
    catch(Throwable e){
      showLog("_err", e.getMessage());
      e.printStackTrace();
    }
    if(loopFlag && uhftagInfo != null && isNonEmpty(epcdt) && (!isActionTidPick || isNonEmpty(tid))){
      if(loopFlag && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK)){
        showLog("epc_pick", "" + epcdt);
        if(tid.length() > 24) tid = tid.substring(0, 24);
        if(isActionTidPick) showLog("tid_pick", "" + tid);
        if(sessionType == AppCommonMethods.SessionType.ENCODING || epcdt.length() >= 24){
          if(isSinglePick && ((!isActionTidPick && !pickTags.contains(epcdt)) || (isActionTidPick && !pickTags.contains(tid) && SCANNED_TIDS.contains(tid) /*&& tid.matches("(?i)(^" + SCANNED_TID + ".*$)")*/))){
            pickTags.add(isActionTidPick ? tid : epcdt);
            pickUHFTags.add(uhftagInfo);
            if(isActionTidPick && pickTags.size() == SCANNED_TIDS.size() && pickCountDownTimer != null){
              pickCountDownTimer.cancel();
              pickCountDownTimer.onFinish();
            }
            else if(pickTags.size() > (isActionTidPick ? SCANNED_TIDS.size() : 1)){
              stopInventory();
              setProgressMessage(false);
              context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
            }
          }
        }
        else{
          stopInventory();
          context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
          setProgressMessage(false);
          return;
        }
      }
      else if(loopFlag && (isCommandForSearch || sessionAction == AppCommonMethods.SessionAction.SEARCH)){
          String epc = uhftagInfo.getId();
          if(readPC){
            int index = 4;
            epc = epc.substring(index);
          }
          showLog("epc", "" + epc);
          final int rssi = (int) Math.round(Double.parseDouble("" + uhftagInfo.rssi));
          showLog("rssi", "" + uhftagInfo.rssi);
          int actualPercentage = getPercentage(rssi);
          percent = actualPercentage;
          showLog("actualPer", "" + actualPercentage);
          handleTagInfoForSearch(epc,""+rssi,tid);
        }
      else if(loopFlag && sessionAction == AppCommonMethods.SessionAction.INVENTORY){
        if((sessionType == AppCommonMethods.SessionType.ENCODING || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue())) || (sessionType == AppCommonMethods.SessionType.ENCODING_THAN || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING_THAN.getValue()))){
          showLog("Inv_ENC", sessionId + "_" + sessionType + "_" + sessionAction);
          updateFoundWrittenTag(epcdt, tid);
          if(inventoryDao.getNonVerifiedCount(sessionId) <= 0){
            showLog("Inv_ENC", "all verified");
            stopInventory();
            context.showCustomSuccessDialog("Verified!");
          }
          else{
            showLog("Inv_ENC_epc_tid", "" + epcdt + "_" + tid);
            updateEncVerifyByEpcTid(epcdt, tid);
          }
        }
        else if(sessionType == AppCommonMethods.SessionType.SEARCH_FILE || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.SEARCH_FILE.getValue())){
          showLog("Inv_SER_FILE_epc_tid", "" + epcdt + "_" + tid);
          final boolean isEPCPresent = inventoryDao.isEPCPresent(sessionId, epcdt);
          final boolean isTidPresent = inventoryDao.isTidPresent(sessionId, tid);
          showLog("Inv_SER_FILE_epc_tid", "" + epcdt + "_" + tid + "_" + isEPCPresent + "_" + isTidPresent);
          if(isEPCPresent || isTidPresent){
            final int status = AppCommonMethods.EncodeVerifyStatus.RE_ENCODED.ordinal();
            if(isTidPresent){
              inventoryDao.updateEncVerifyStatusByTid(sessionId, tid, status);
            }
            else if(isEPCPresent){
              inventoryDao.updateEncVerifyStatusByEpc(sessionId, epcdt, status);
            }
          }
        }
        else if(sessionType == AppCommonMethods.SessionType.OFF_RANGE || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.OFF_RANGE.getValue())){
          final String ean = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt);
          final int rssi = readRssi ? Math.round(uhftagInfo.rssi) : 0;
          int actualPercentage = isUseShortRangeSearchForAll || (isUseShortRangeSearchForOnlyGID && context.epcEncoderDecoder.isGID() && epcdt.startsWith("35")) ? getPercentageForGID(rssi) : getPercentage(rssi);
          if(isNonEmpty(ean) && isNonEmpty(eans) && eans.contains(ean)){
            showLog("off_matched", "true");
            final ProductDao productDao = AppDatabase.getProductDao(context);
            productDao.updateRssiPercentage(sessionType.getValue(), ean, actualPercentage);
          }
        }
        else if(sessionType == AppCommonMethods.SessionType.SER_EXCEL || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.SER_EXCEL.getValue())){
          final String ean = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt);
          final int rssi = readRssi ? Math.round(uhftagInfo.rssi) : 0;
          int actualPercentage = isUseShortRangeSearchForAll || (isUseShortRangeSearchForOnlyGID && context.epcEncoderDecoder.isGID() && epcdt.startsWith("35")) ? getPercentageForGID(rssi) : getPercentage(rssi);
          if(isNonEmpty(ean) && isNonEmpty(eans) && eans.contains(ean)){
            showLog("off_matched", "true");
            final ProductDao productDao = AppDatabase.getProductDao(context);
            productDao.updateRssiPercentage(sessionType.getValue(), ean, actualPercentage);
          }
        }
        else if(validateTagInfoForInventory(epcdt)) storeInventoryData(uhftagInfo);
      }
    }
  }
  
  private final IReadTagsListener iReadTagsListener = new IReadTagsListener(){
    @Override
    public void tagsRead(List<EPC> list){
      for(EPC uhftagInfo : list){
        if(!loopFlag) break;
        else processTagData(uhftagInfo);
      }
    }
  };
}

