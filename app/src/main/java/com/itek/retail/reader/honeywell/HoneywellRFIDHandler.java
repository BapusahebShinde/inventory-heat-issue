package com.itek.retail.reader.honeywell;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.playSound;
import static com.itek.retail.common.AppCommonMethods.showShortToast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteConstraintException;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import com.cipherlab.rfid.GeneralString;
import com.honeywell.rfidservice.EventListener;
import com.honeywell.rfidservice.RfidManager;
import com.honeywell.rfidservice.TriggerMode;
import com.honeywell.rfidservice.rfid.AntennaPower;
import com.honeywell.rfidservice.rfid.BankInfo;
import com.honeywell.rfidservice.rfid.Gen2;
import com.honeywell.rfidservice.rfid.OnTagReadListener;
import com.honeywell.rfidservice.rfid.Region;
import com.honeywell.rfidservice.rfid.RegionV2;
import com.honeywell.rfidservice.rfid.RfidReader;
import com.honeywell.rfidservice.rfid.RfidReaderException;
import com.honeywell.rfidservice.rfid.TagAdditionData;
import com.honeywell.rfidservice.rfid.TagReadData;
import com.honeywell.rfidservice.rfid.TagReadOption;
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
import com.itek.retail.ui.home.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HoneywellRFIDHandler extends RFIDHandler implements OnTagReadListener, EventListener{
  
  private static final String TAG_LOG = "Honeywell";
  private static final String DEVICE_STATUS_CONNECTED = "connected";
  private static final String DEVICE_STATUS_DISCONNECTED = "disconnected";
  private static final String DEVICE_BATTERY_LOW = "low";
  private static ActivityResultLauncher<Intent> bluetoothResultLauncher;
  final int encodeRetryLimit = 2;
  private final int MIN_POWER_TO_SET = 500;
  private final int MAX_POWER_TO_SET = 3000;
  private final int SOUND_THRESHOLD = 8;
  private final List<BluetoothDevice> mDevices = new ArrayList<>();
  private final String readerName = "(?i)(I[A-Z][0-9]{2,4})";
  Set<TagReadData> pickUHFTags = new HashSet<>(0);
  private BluetoothAdapter mBluetoothAdapter = null;
  private boolean restrictTriggerPress = false;
  private Boolean loopFlag = false;
  private RfidReader mReader;
  private RfidManager rfidMgr;
  
  /**
   * Get reader instance object.
   *
   * @param context              the context
   * @param mainReaderRepository the main reader repository
   * @param rfidInitInterface    the rfid init interface
   * @return the object
   */
  @Override
  public Object getReaderInstance(CommonActivity context, MainReaderRepository mainReaderRepository, RFIDInitInterface rfidInitInterface){
    try{
      this.context = context;
      rfidMgr = RfidManager.getInstance(context);
      rfidMgr.addEventListener(this);
      this.mainReaderRepository = mainReaderRepository;
      this.rfidInterface = rfidInitInterface;
      registerBluetoothEvent();
      
    }
    catch(Exception ex){
      ex.printStackTrace();
    }
    return null;//mReader;
  }
  
  @Override
  public void InitSDK(){
    super.InitSDK();
    InitSDK(true, true);
  }
  
  public void InitSDK(boolean isConnect, boolean isConfigureDevice){
    showLog("INIT", "ggg");
    super.InitSDK();
    if(rfidMgr == null) rfidMgr = RfidManager.getInstance(context);
    rfidMgr.addEventListener(this);
    if(isConnect && !rfidMgr.isConnected()){
      connect();
    }
    else if(isReaderConnected() && mReader == null){
      rfidMgr.createReader();
      //new CreateInstanceTask(isConfigureDevice).execute();
    }
    else if(isConfigureDevice){
      configureReader();
    }
  }
  
  /**
   * Register bluetooth event.
   */
  private void registerBluetoothEvent(){
    if(SharedPrefManager.getIsDeviceBluetoothDependent() && ((MainActivity) context) != null && !((MainActivity) context).isFinishing()){
      bluetoothResultLauncher = ((MainActivity) context).registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
        @Override
        public void onActivityResult(ActivityResult result){
          checkAndConnectReader();
        }
      });
      showLog("bluetoothResultLauncher", "" + (bluetoothResultLauncher != null));
    }
  }
  
  @Override
  public void checkAndConnectReader(){
    if(SharedPrefManager.getIsDeviceBluetoothDependent()){
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if(mBluetoothAdapter == null){
        showShortToast(context, String.format(context.getString(R.string.err_no_bluetooth), getTypeCharCode()));
        return;
      }
      else if(!mBluetoothAdapter.isEnabled() /*&& bluetoothResultLauncher != null*/){
        showShortToast(context, String.format(context.getString(R.string.err_bluetooth_disabled), getTypeCharCode()));
        ((MainActivity) context).showCustomAlertDialog("", String.format(context.getString(R.string.err_bluetooth_disabled), getTypeCharCode()), null, false, false, context.getString(bluetoothResultLauncher != null ? R.string.btn_enable : R.string.btn_ok), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            if(bluetoothResultLauncher != null)
              bluetoothResultLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
          }
        }, bluetoothResultLauncher != null ? context.getString(R.string.btn_cancel) : "", null);
        return;
      }
    }
    if(rfidMgr == null) InitSDK(true, true);
    else if(rfidMgr != null && !rfidMgr.isConnected()){ connect(); }
    else if(rfidMgr != null && rfidMgr.isConnected() && mReader == null){ rfidMgr.createReader(); }
    else if(rfidMgr != null && rfidMgr.isConnected() && mReader != null)
      configureReader(sessionType);
    //rfidMgr.enableBluetoothFota();
  }
  
  @Override
  public void checkAndSetReader(){
    if(SharedPrefManager.getIsDeviceBluetoothDependent()){
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if(mBluetoothAdapter == null){
        
        ((MainActivity) context).showCustomErrDialog(String.format(context.getString(R.string.err_no_bluetooth), getTypeCharCode()));
        return;
      }
      else if(!mBluetoothAdapter.isEnabled() /*&& bluetoothResultLauncher != null*/){
        
        ((MainActivity) context).showCustomAlertDialog("", String.format(context.getString(R.string.err_bluetooth_disabled), getTypeCharCode()), null, false, false, context.getString(bluetoothResultLauncher != null ? R.string.btn_enable : R.string.btn_ok), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            if(bluetoothResultLauncher != null)
              bluetoothResultLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
          }
        }, bluetoothResultLauncher != null ? context.getString(R.string.btn_cancel) : "", null);
        
        return;
      }
    }
    if(rfidMgr == null || mReader == null) InitSDK(true, false);
    else if(rfidMgr != null && !rfidMgr.isConnected() && mReader == null) connect();
    //new ConnectionTask(false).execute();
  }
  
  /**
   * Connect and set region.
   *
   * @param region the region
   */
  public void ConnectAndSetRegion(final RegionV2 region){
    showLog(TAG, "ConnectAndSetRegion");
    this.isCommandForSearch = false;
    this.isCommandForEPCSearch = false;
    this.isLockSearchEPC = false;
    this.SEARCH_EPC = "";
    this.SEARCH_BARCODE = "";
    this.SEARCH_LOCKED_EPC = "";
    setProgressMessage(true);
    new Thread(() -> {
      showLog(TAG, "region not set");
      Region selectedRegionInfo = null;
      try{
        mReader.setRegion(region);
        selectedRegionInfo = mReader.getRegion();
        if(selectedRegionInfo != null){
          rfidInterface.RFIDInitializationStatus(true, "", mReader);
          setProgressMessage(false);
        }
        else{
          ((MainActivity) context).showCustomErrDialog(String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
          rfidInterface.RFIDInitializationStatus(false, "", mReader);
          setProgressMessage(false);
        }
      }
      catch(Exception e){
        e.printStackTrace();
        showLog("SetRegion" + "ex", e.getMessage());
        ((MainActivity) context).showCustomErrDialog(String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        rfidInterface.RFIDInitializationStatus(false, "", null);
        setProgressMessage(false);
        return;
      }
    }).start();
  }
  
  @Override
  public boolean isReaderConnected(){
    final boolean isBluetoothOn = !SharedPrefManager.getIsDeviceBluetoothDependent() || isBluetoothConnected();
    if(isBluetoothOn && rfidMgr != null && rfidMgr.isConnected()){
      if(mReader == null){
        rfidMgr.createReader();
        return false;
      }
      else return true;
    }
    else{
      showLog(TAG, "reader is not connected");
      return false;
    }
  }
  
  private boolean isBluetoothConnected(){
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
  }
  
  @Override
  public boolean isReaderPresent(boolean isReaderInstanceSet){
    final boolean isBluetoothOn = !SharedPrefManager.getIsDeviceBluetoothDependent() || isBluetoothConnected();
    if(isBluetoothOn && (rfidMgr != null || isReaderInstanceSet)){
      return true;
    }
    else{
      showLog(TAG, "reader is not connected");
      return false;
    }
  }
  
  @Override
  public void performEncoding(final Inventory pickedTag, final String currentTagPassword){
    final Inventory pickTag = context.epcEncoderDecoder.setPCDataBeforeEncoding(pickedTag);
    if(pickTag == null) return;
    else if(chkNull(pickTag.tid, "").length() >= 8){
      if(SharedPrefManager.getNon128BitTids().contains(pickTag.tid.toUpperCase().substring(0, 8)) && pickTag.newEpc.length() > 28){
        context.showCustomErrDialog(context.getString(R.string.err_encoding_non_std_not_allowed_for_non_128_bit_tids));
        return;
      }
    }
    setPower(MAX_POWER_TO_SET);
    sessionAction = AppCommonMethods.SessionAction.ENCODE;
    isEncodeOn.postValue(true);
    setProgressMessage(context.getString(R.string.msg_pick), true);
    startEncoding(pickTag, "", 0);
  }
  
  @Override
  public void performEncoding(Inventory pickedTag){
    performEncoding(pickedTag, "");
  }
  
  @Override
  public void performEncoding(List<Inventory> listPickedTags){
  
  }
  
  @Override
  public void performEncoding(List<Inventory> listPickedTags, AppCommonMethods.SessionType sessionType){
  
  }
  
  @Override
  public void readTagCurrentPassword(Inventory pickedTag){
  
  }
  
  @Override
  //public boolean performPick(final String findBarcode){ return performPick(findBarcode,false);}
  public boolean performPick(final String findBarcode, final boolean isDecodeOnPick,final Integer pickPower, final boolean isPostPicked,final List<String> pickedEpcs){
    final boolean isPerformPick = super.performPick(findBarcode, isDecodeOnPick,pickPower,isPostPicked,pickedEpcs);
    if(isPerformPick){
      pickTags.clear();
      pickUHFTags.clear();
      isPickOn.postValue(false);
      isActionPick = false;
      sessionAction = AppCommonMethods.SessionAction.INVENTORY;
      startPick(findBarcode, isDecodeOnPick,isPostPicked);
    }
    return isPerformPick;
  }
  
  @Override
  public void setReaderPower(int power){
    if(isReaderConnected() && (power * 100) >= MIN_POWER_TO_SET && (power * 100) <= MAX_POWER_TO_SET){
      setProgressMessage(true);
      new Handler().post(new Runnable(){
        @Override
        public void run(){
          int oldPower = 0;
          try{
            oldPower = rfidMgr.getAntennaPower()[0].getReadPower() / 100;
            AntennaPower[] ap = new AntennaPower[1];
            ap[0] = new AntennaPower(1, power * 100, MAX_POWER_TO_SET);
            rfidMgr.setAntennaPower(ap);
            if(sessionType != null && sessionType.getValue() > 0){
              SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), power);
              showLog("power", "" + power);
            }
            setProgressMessage(false);
            SharedPrefManager.setReaderPower(power);
            readerPower.postValue(power);
          }
          catch(Exception e){
            e.printStackTrace();
            setProgressMessage(false);
            readerPower.postValue(oldPower);
            SharedPrefManager.setReaderPower(oldPower);
            if(sessionType != null)
              SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), oldPower);
          }
        }
      });
    }
    else setProgressMessage(false);
  }
  
  @Override
  public boolean performTidPick(final String findBarcode, final List<String> tids){
    final boolean isPerformTidPick = super.performTidPick(findBarcode, tids);
    if(isPerformTidPick){
      pickTags.clear();
      pickUHFTags.clear();
      isPickOn.postValue(false);
      isActionPick = false;
      sessionAction = AppCommonMethods.SessionAction.INVENTORY;
      isActionTidPick = false;
      SCANNED_TIDS.clear();
      startTidPick(findBarcode, tids);
    }
    return isPerformTidPick;
  }
  
  @Override
  protected synchronized void performSearch(){
    showLog("CMD", "" + isCommandForSearch);
    // check reader connection
    if(!isReaderConnected()){// || reader.Config==null || reader.Config.Antennas==null){
      showLog("Reader", "NOT CONNECTED");
      return;
    }
    else{
      try{
        //TODO set beeper
        clearFilters();
        setPower(MAX_POWER_TO_SET);
        mReader.setSession(Gen2.Session.Session0);
        
        String finalsgtin = SEARCH_EPC;
        showLog("finalsgtin", chkNull(finalsgtin, "null"));
        String header = chkNull(finalsgtin, "").length() > 2 ? finalsgtin.substring(0, 2) : "";
        final boolean isNonStdEnc = finalsgtin.length() >= 32 && header.matches("(?i)(BC|0C|00)");
        sessionAction = AppCommonMethods.SessionAction.SEARCH;
        if(isCommandForEPCSearch) addEpcBasedFilters(finalsgtin, isNonStdEnc);
        else
          addFilters(isNonStdEnc && chkNull(finalsgtin, "").length() > 12 ? finalsgtin.substring(12) : finalsgtin, isNonStdEnc);
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  
  @Override
  protected void addEpcBasedFilters(String tag, boolean isBc){
    BankInfo filter = new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
    filter.setStartAddr(2);
    filter.setBankValue(tag);
    //filter.setBlockcnt(tag.length());
    if((sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO) && chkNull(tag, "").length() > 4){
      filter.setStartAddr(3);
      filter.setBankValue(tag.substring(4));
    }
    sessionAction = AppCommonMethods.SessionAction.SEARCH;
    readTags(filter);
  }
  
  @Override
  protected void addTidBasedFilters(String tid){
    BankInfo filter = new BankInfo(BankInfo.from(TagAdditionData.TID_BANK));
    filter.setStartAddr(0);
    filter.setBankValue(tid);
    sessionAction = AppCommonMethods.SessionAction.SEARCH;
    readTags(filter);
  }
  
  @Override
  protected void addFilters(String tag, boolean isBc){
    AppCommonMethods.showLog("addFilters", tag + "_" + isBc);
    final BankInfo filter = new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
    if(isBc){
      filter.setStartAddr(5);//80
      tag = tag.length() > 12 ? tag.substring(0, 12) : tag;
      filter.setBankValue(tag);
    }
    else{
      AppCommonMethods.showLog("addFiltersNEW", tag + "_" + isBc);
      filter.setStartAddr(2);//32
      filter.setBankValue(tag);
    }
    sessionAction = AppCommonMethods.SessionAction.SEARCH;
    readTags(filter);
  }
  
  @Override
  protected void startPick(final String findBarcode, final boolean isDecodeOnPick, final boolean isPostPicked){
    try{
      clearFilters();
      int maxPower = chkZero(pickPower,sessionType == AppCommonMethods.SessionType.ENCODING ? MIN_POWER_TO_SET * 2 : sessionType == AppCommonMethods.SessionType.MOVEMENT ? MAX_POWER_TO_SET / 2 : MIN_POWER_TO_SET * 2);
      //+ MIN_POWER_TO_SET / 2;
      int power = /*sessionType == AppCommonMethods.SessionType.ENCODING||*/sessionType == AppCommonMethods.SessionType.MOVEMENT ? chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower / 100) * 100 : maxPower;
      if(power > maxPower){
        power = maxPower;
      }
      showLog("pick_power", "" + (power));
      setPower(power);
      mReader.setSession(Gen2.Session.Session0);
      
      //configAction(power, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
      
      isActionPick = true;
      sessionAction = AppCommonMethods.SessionAction.PICK;
      pickTags.clear();
      pickUHFTags.clear();
      readTags();
      
      AppCommonMethods.logInFile(context, sessionType.name() + "_PICK_START" + (isNonEmpty(findBarcode) ? "(" + findBarcode + ")" : ""));
      //Read command with readAccessParams and accessFilter as null to read all the tags
      showLog("SINGLEPICK", "" + isSinglePick);
      if(isSinglePick){
        showLog("SINGLEPICK", "" + isSinglePick);
        setProgressMessage(context.getString(R.string.msg_pick), true);
        pickCountDownTimer = new CountDownTimer(sessionType == AppCommonMethods.SessionType.ENCODING ? 1000 : pickCountDownTime, 200){
          @Override
          public void onTick(long l){
            showLog("pick " + "onTick", "" + l);
          }
          
          @Override
          public void onFinish(){
            showLog("INTIMER", "FINISH");
            showLog("onFinish", "onFinish");
            stopInventory();
            if(isNullOrEmpty(pickTags)){
              AppCommonMethods.logInFile(context, sessionType.name() + "_PICK_STOP (Tag Not Found)");
              context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
              setProgressMessage(false);
            }
            else if(pickTags.size() > 1){
              setProgressMessage(false);
              AppCommonMethods.logInFile(context, sessionType.name() + "_PICK_RESULT (Multiple Tags Found)");
              context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
            }
            else if(pickTags.size() == 1){
              AppCommonMethods.showLog("pickTags.size()", "" + pickTags.size());
              setProgressMessage(false);
              final TagReadData tagData = new ArrayList<TagReadData>(pickUHFTags).get(0);
              final String epcdt = chkNull(tagData.getEpcHexStr(), "");
              final String tid = bytesToHex(tagData.getAdditionData());
              showLog("pick_epc_tid", epcdt + "_" + tid);
              //AppCommonMethods.showLog("epcdt_picked", "" + epcdt);
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
              else if((sessionType != AppCommonMethods.SessionType.SCAN && sessionType != AppCommonMethods.SessionType.VERIFY_ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.DECODING) && (inventoryDao.isEPCPresent(sessionId, epcdt)||(isNonEmpty(pickedEpcs) && pickedEpcs.contains(epcdt)))){
                AppCommonMethods.logInFile(context, sessionType.name() + "_PICK_STOP (Tag Already Picked)");
                context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
                setProgressMessage(false);
              }
              else{
                boolean isMatchingBarcode = false;
                //final String epc = ((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING) && chkNull(epcdt, "").length() > 2) ? chkNull(epcdt, "").startsWith("0C") ? epcdt.replaceFirst("0C", "BC") : chkNull(epcdt, "").startsWith("00") ? (epcdt.length() >= 32) ? epcdt.replaceFirst("00", "BC") : (epcdt.length() >= 24) ? epcdt.replaceFirst("00", "30") : epcdt : epcdt : epcdt;
                //TODO
                //final String epc = ((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && chkNull(epcdt, "").length() > 2)?chkNull(epcdt, "").startsWith("0C")?epcdt.replaceFirst("0C", "BC"):chkNull(epcdt, "").startsWith("05")?epcdt.replaceFirst("05", "35"):chkNull(epcdt, "").startsWith("00")? (epcdt.length() >= 32) ? epcdt.replaceFirst("00", "BC") : (epcdt.length() >= 24) ? epcdt.replaceFirst("00", "35") : epcdt : epcdt :epcdt;
                //final String header = epc.length() > 2 ? epc.substring(0, 2) : "";
                String matchingBarcode = "";
                //showLog("pick_finish_epc_header", header);
                //check by using getBarcode method instead of switch case
                final String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING);
                final String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context,findBarcode);
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
                setProgressMessage(false);
                if((isMatchingBarcode && isNonEmpty(matchingBarcode)) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING){
                  AppCommonMethods.logInFile(context, sessionType.name() + "_PICK_STOP (" + matchingBarcode + "_" + tagData.getEpcHexStr() + ")");
                  showLog("PICK", "1:SESSION : " + sessionType.name());
                  readTid = true;
                  readRssi = true;
                  readEAN = true;
                  readPC = sessionType == AppCommonMethods.SessionType.ENCODING;// || sessionType == AppCommonMethods.SessionType.DECODING;
                  if(isPostPicked || sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING){
                    final Inventory pickedTagData = getDataFromTagInfo(tagData);
                    if((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && isMatchingBarcode && isNonEmpty(matchingBarcode))
                      pickedTagData.ean = AppCommonMethods.getLeftZeroReplacedString(context,matchingBarcode);
                    if(isNullOrEmpty(matchingBarcode) && sessionType != AppCommonMethods.SessionType.ENCODING && (sessionType != AppCommonMethods.SessionType.DECODING || !SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_NON_ENCODED_TAG_PICK_FOR_DECODE,AppCommonMethods.isAllowNonEncodedTagPickForDecode)))
                      context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                    else if((sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING) && (pickedTagData == null || isNullOrEmpty(pickedTagData.epc) || isNullOrEmpty(pickedTagData.tid)))
                      context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
                    else pickData.postValue(pickedTagData);
                  }
                  else{
                    sessionAction = AppCommonMethods.SessionAction.INVENTORY;
                    storeInventoryData(tagData);
                    if(isDecodeOnPick){
                      final Inventory pickedTagData = getDataFromTagInfo(tagData);
                      pickData.postValue(pickedTagData);
                    }
                  }
                }
              }
            }
          }
        };
        pickCountDownTimer.start();
        showLog("INTIMER", "OUT");
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  private void readTags(){ readTags(null); }
  
  private void readTags(final BankInfo filter){
    if(!loopFlag){
      try{
        mReader.setOnTagReadListener(this);
        if(sessionAction == AppCommonMethods.SessionAction.PICK || isActionPick)
          isPickOn.postValue(true);
        else if(isCommandForSearch || sessionAction == AppCommonMethods.SessionAction.SEARCH)
          isSearchOn.postValue(true);
        else isInventoryOn.postValue(true);
        loopFlag = true;
        final boolean isInventory = sessionAction == AppCommonMethods.SessionAction.INVENTORY;
        if(sessionAction == AppCommonMethods.SessionAction.INVENTORY || sessionAction == AppCommonMethods.SessionAction.SEARCH)
          startTimer();
        TagReadOption readOptions = new TagReadOption();
        readOptions.setReadCount(true);
        readOptions.setAntennaId(true);
        if(!isInventory){
          readOptions.setData(true);
          readOptions.setFrequency(true);
          readOptions.setRssi(true);
          //readOptions.setStopPercent(1);
          readOptions.setTimestamp(true);
        }
        //readOptions.setProtocol(true);
        mReader.enableFastId(isInventory);
        if(filter != null)
          mReader.read(filter, TagAdditionData.get(TagAdditionData.TID_BANK.getName()), readOptions);
        else mReader.read(TagAdditionData.get(TagAdditionData.TID_BANK.getName()), readOptions);
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Start pick.
   *
   * @param findBarcode the find barcode
   */
  @Override
  protected void startTidPick(final String findBarcode, final List<String> tids){
    try{
      if(!isReaderConnected()) return;
      clearFilters();
      isActionTidPick = isNonEmpty(tids);//chkNull(tid,"").length()>0;
      SCANNED_TIDS.clear();
      SCANNED_TIDS.addAll(tids);
      int maxPower = sessionType == AppCommonMethods.SessionType.ENCODING ? MIN_POWER_TO_SET * 2 : sessionType == AppCommonMethods.SessionType.MOVEMENT ? MAX_POWER_TO_SET / 2 : MIN_POWER_TO_SET * 2;
      //+ MIN_POWER_TO_SET / 2;
      int power = isActionTidPick ? MAX_POWER_TO_SET :/*sessionType == AppCommonMethods.SessionType.ENCODING||*/sessionType == AppCommonMethods.SessionType.MOVEMENT ? chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower / 100) * 100 : maxPower;
      setPower(power);
      mReader.setSession(Gen2.Session.Session0);
      //configAction(power, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
      
      //if(isActionTidPick && tids.size()==1) addTidBasedfilters(tids.get(0));
      
      isActionPick = true;
      sessionAction = AppCommonMethods.SessionAction.PICK;
      pickTags.clear();
      pickUHFTags.clear();
      AppCommonMethods.logInFile(context, sessionType.name() + "_PICK_START" + (isNonEmpty(findBarcode) ? "(" + findBarcode + ")" : ""));
      
      //Read command with readAccessParams and accessFilter as null to read all the tags
      readTags();
      showLog("SINGLEPICK", "" + isSinglePick);
      if(tids.size() >= 1 && isSinglePick){
        setProgressMessage(context.getString(R.string.msg_pick), true);
        pickCountDownTimer = new CountDownTimer(tids.size() * 1000, 1000){
          @Override
          public void onTick(long l){
            showLog("onTick", "" + l);
          }
          
          @Override
          public void onFinish(){
            showLog("onFinishTidPick", "onFinish" + "_" + pickTags.size() + "_" + pickUHFTags.size() + "_" + tids.size());
            setProgressMessage(false);
            stopInventory();
            if(isNullOrEmpty(pickTags)){
              context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
              setProgressMessage(false);
            }
            else if(pickTags.size() < tids.size()){
              setProgressMessage(false);
              context.showCustomErrDialog(String.format(context.getString(R.string.err_picked_qty_total), getTypeCharCode(), "" + pickTags.size(), "" + tids.size()));
            }
            else if(pickTags.size() > tids.size()){
              setProgressMessage(false);
              context.showCustomErrDialog(String.format(context.getString(R.string.err_picked_qty_total), getTypeCharCode(), "" + pickTags.size(), "" + tids.size()));
            }
            else if(pickTags.size() == tids.size()){
              setProgressMessage(false);
              readTid = true;
              readRssi = true;
              readEAN = true;
              readPC = sessionType == AppCommonMethods.SessionType.ENCODING;
              List<Inventory> pickedList = new ArrayList<>(0);
              for(TagReadData tagData : pickUHFTags){
                final String epc = chkNull(tagData.getEpcHexStr(), "");
                final String tid = bytesToHex(tagData.getAdditionData());
                showLog("picked_tag", epc + "_" + tid);
                Inventory pickedTag = null;
                try{
                  pickedTag = getDataFromTagInfo(tagData);
                }
                catch(Exception e){
                  e.printStackTrace();
                }
                if(pickedTag != null && isNonEmpty(epc) && isNonEmpty(tid) && tids.contains(/*tid.length()>24?tid.substring(0,24):*/tid)){
                  pickedList.add(pickedTag);
                  //pickData.postValue(pickedTag);
                  //storeInventoryData(tagData);
                }
              }
              showLog("pickedList", "" + pickedList.size());
              if(pickedList.size() >= 1) pickedListData.postValue(pickedList);
              else if(isNullOrEmpty(pickedList)){
                setProgressMessage(false);
                context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
              }
              //else if(pickedList.size() == 1) pickData.postValue(pickedList.get(0));
            }
          }
        };
        pickCountDownTimer.start();
      }
    }
    catch(Exception e){
      e.printStackTrace();
      showLog("EXCPICK3", "" + e.getMessage());
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast)
        AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
    }
  }
  
  private synchronized void startEncoding(final Inventory pickedTag){
    startEncoding(pickedTag, "", 0);
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
        dataLen = sgtin.length() / 4;
        offSet = (dataLen % 2 == 0 ? 2 : 1);
    }
    showLog("dataLen", "" + dataLen);
    showLog("offSet", "" + offSet);
    showLog("currentAccessPassword", "" + SharedPrefManager.getCurrentAccessPassword());
    
    if(offSet > 0 && dataLen > 0){
      final BankInfo filter = new BankInfo(BankInfo.from(TagAdditionData.TID_BANK));
      // TID
      filter.setStartAddr(0);
      filter.setBankValue(tid);
      
      final BankInfo target = new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
      // EPC
      target.setStartAddr(offSet);
      //target.setPassword(SharedPrefManager.getCurrentAccessPassword());
      target.setBankValue(sgtin);
      
      if(SharedPrefManager.getNonPasswordTids().contains(tid.toUpperCase().substring(0, 8))){
        new Handler().post(new Runnable(){
          @Override
          public void run(){
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
              //filter.setBlockcnt(tid.length() / 4);
              // One block has 2 bytes
              //BankInfo target=new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
              // EPC
              //target.setStartAddr(offSet);
              target.setPassword(pass);
              //target.setBankValue(sgtin);
              try{
                mReader.writeTagData(filter, target);
                isWriteSuccess = true;
                saveOrUpdateEncodedData(pickedTag, barcode, epc, tid, pc, sgtin);
                break;
              }
              catch(RfidReaderException e){
                e.printStackTrace();
              }
            }
            if(!isWriteSuccess){
              /*((MainActivity) context).showCustomErrDialog(R.string.err_encoding_auth_fail);
              isEncodeOn.postValue(false);
              setProgressMessage(false);*/
              updateTagWriteCount(context.getString(R.string.err_encoding_auth_fail));
              showLog("LOCKMEMORY1", "FAIL");
            }
          }
        });
      }
      else{
        /*BankInfo target=new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
        target.setStartAddr(offSet);*/
        target.setPassword(SharedPrefManager.getCurrentAccessPassword());
        //target.setBankValue(sgtin);
        final int finalOffSet = offSet;
        new Handler().post(new Runnable(){
          @Override
          public void run(){
            try{
              mReader.writeTagData(filter, target);
              showLog("SUCCESS", "BY CURRENT PASSWORD");
              saveOrUpdateEncodedData(pickedTag, barcode, epc, tid, pc, sgtin);
            }
            catch(RfidReaderException e){
              e.printStackTrace();
              //TODO do for other passwords
              List<String> passwords = SharedPrefManager.getOldAccessPasswords();
              if(passwords.contains(defaultTagZeroPassword))
                passwords.remove(defaultTagZeroPassword);
              passwords.add(0, defaultTagZeroPassword);
              
              AppCommonMethods.showLog("passwords", passwords.size() + "_" + passwords.toString());
              boolean isWriteSuccess = false;
              /*BankInfo target1=new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
              // EPC
              target1.setStartAddr(finalOffSet);
              //target1.setPassword(oldPassword);
              target1.setBankValue(sgtin);*/
              for(String oldPassword : passwords){
                //BankInfo target1=new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
                // EPC
                //target1.setStartAddr(offSet);
                //target.setPassword(oldPassword);
                //target1.setBankValue(sgtin);
                try{
                  //target1.setPassword(oldPassword);
                  target.setPassword(oldPassword);
                  AppCommonMethods.showLog("oldPassword", oldPassword);
                  mReader.writeTagData(filter, target);
                  showLog("SUCCESS", "BY PASSWORD: " + oldPassword);
                  isWriteSuccess = true;
                  changeAndLockPassword(pickedTag, barcode, epc, tid, pc, sgtin, oldPassword);
                  //changeAndLockPassword(oldPassword);
                  break;
                }
                catch(RfidReaderException ex){
                  e.printStackTrace();
                }
              }
              if(!isWriteSuccess){
                /*((MainActivity) context).showCustomErrDialog(R.string.err_encoding_auth_fail);
                isEncodeOn.postValue(false);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_auth_fail));
                showLog("LOCKMEMORY1", "FAIL");
              }
            }
          }
        });
      }
    }
    else{
      updateTagWriteCount(context.getString(R.string.err_encoding_write_fail));
    }
  }
  
  private void changeAndLockPassword(Inventory pickedTag, String barcode, String epc, String tid, String pc, String sgtin, String oldPassword){
    final BankInfo filter = new BankInfo(BankInfo.from(TagAdditionData.TID_BANK));
    // TID
    filter.setStartAddr(0);
    filter.setBankValue(tid);
    
    final BankInfo target = new BankInfo(BankInfo.from(TagAdditionData.RESERVED_BANK));
    // EPC
    target.setStartAddr(2);
    target.setPassword(oldPassword);
    target.setBankValue(SharedPrefManager.getCurrentAccessPassword());
    
    new Handler().post(new Runnable(){
      @Override
      public void run(){
        try{
          mReader.writeTagData(filter, target);
          showLog("SUCCESS", String.format("CHANGED PASS From: %s To: %s", oldPassword, SharedPrefManager.getCurrentAccessPassword()));
          if(!SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase(defaultTagZeroPassword) && oldPassword.equalsIgnoreCase(defaultTagZeroPassword)){
            Gen2.LockBank lockBank = Gen2.LockBank.ACCESS_PASSWORD;//Gen2.LockBank.get(Gen2.LockBank.ACCESS_PASSWORD.getName());//"Access Password");
            Gen2.LockType lockType = Gen2.LockType.TEMPORARY_LOCK;//Gen2.LockType.get(Gen2.LockType.TEMPORARY_LOCK.getName());//"Temporary lock");
            
            filter.setPassword(SharedPrefManager.getCurrentAccessPassword());
            
            try{
              mReader.lockTag(filter, lockBank, lockType);
              showLog("SUCCESS", "LOCK BY  :" + SharedPrefManager.getCurrentAccessPassword());
            }
            catch(RfidReaderException ex){
              showLog("LOCK FAIL", "LOCK PASS FAIL");
            }
          }
        }
        catch(RfidReaderException ex){
          showLog("WRITE PASS FAIL", "LOCK OR WRITE PASS FAIL");
        }
        finally{
          saveOrUpdateEncodedData(pickedTag, barcode, epc, tid, pc, sgtin);
        }
      }
    });
  }
  
  private void saveOrUpdateEncodedData(Inventory pickedTag, String barcode, String epc, String tid, String pc, String sgtin){
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
      catch(Exception e){
        e.printStackTrace();
      }
      //isEncodeDone.postValue(true);
      AppCommonMethods.successBeep();
      updateTagWriteCount(true);
      /*new Handler().postDelayed(new Runnable(){
        @Override
        public void run(){
          isEncodeOn.postValue(false);
          setProgressMessage(false);
        }
      }, 50);*/
    }
    catch(SQLiteConstraintException e){
      e.printStackTrace();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  private void startDecoding(final Inventory pickedTag, final String currentPassword){
    if(pickedTag == null) return;
    int offSet = 0;
    //String currentpassword = currentPassword;//chkNull(TAG_CURRENT_PASSWORD,SharedPrefManager.getCurrentAccessPassword());
    final String barcode = pickedTag.ean;
    final String epc = pickedTag.epc;
    final String pc = pickedTag.pcdata;
    final String sgtin = (isNonEmpty(pickedTag.newEpc) && pickedTag.newEpc.startsWith("0") ? pickedTag.newEpc : !epc.startsWith("0") ? "0" + epc.substring(1) : epc).toUpperCase().trim();
    final String tid = pickedTag.tid;
    showLog("CURRENTPASSWORD", currentPassword);
    showLog("BARCODE", barcode);
    showLog("TID", tid);
    showLog("TAGID", epc);
    showLog("PCData", pc);
    showLog("WRITETAGID", sgtin);
    showLog("WRITETAGIDLENGTH", "" + sgtin.length());
    showLog("OFFSET", "" + offSet);
    showLog("TAGIDLENGTH", "" + epc.length());
    //logInFile(context,"ENCODECOMMAND:\nCURRENTPASSWORD:" + currentpassword + "\nTAGID:" + epc + "\nSGTINTOWRITE:" + sgtin + "\nSGTINLENGTH:" + sgtin.length());
    BankInfo filter = new BankInfo(BankInfo.from(TagAdditionData.TID_BANK));
    filter.setStartAddr(0);
    filter.setBankValue(tid);
    
    boolean allowtowrite = false;
    //final boolean isMultiWrite = multiWriteListSize > 0;
    try{
      //logInFile(context,"FINALDATATOWRITEINTAG : " + sgtin);
      switch(sgtin.length()){
        case 28:
        case 36:
        case 24:
        case 32:
          allowtowrite = true;
          int dataLen = sgtin.length() / 4;
          offSet = (dataLen % 2 == 0 ? 2 : 1);
          break;
        default:
          allowtowrite = false;
          break;
      }
      showLog("OFFSET_1", "" + offSet);
    }
    catch(Exception e){
      showLog("EXC", "" + e.getMessage());
      //logInFile(context,"EXCEPTIONLENG:" + "CRASH : " + e.getMessage());
    }
    
    if(allowtowrite){
      //final int finalOffSet = offSet;
      final BankInfo target = new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
      // EPC
      target.setStartAddr(offSet);
      //target.setPassword(SharedPrefManager.getCurrentAccessPassword());
      target.setBankValue(sgtin);
      /*try{
       reader.Config.setAccessOperationWaitTimeout(WRITE_OPERATION_TIMEOUT);
      }catch(Exception e){e.printStackTrace();}*/
      if(SharedPrefManager.getNonPasswordTids().contains(chkNull(pickedTag.tid, "").toUpperCase().substring(0, 8))){
        new Handler().post(new Runnable(){
          @Override
          public void run(){
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
              //filter.setBlockcnt(tid.length() / 4);
              // One block has 2 bytes
              //BankInfo target=new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
              // EPC
              //target.setStartAddr(offSet);
              target.setPassword(pass);
              //target.setBankValue(sgtin);
              try{
                mReader.writeTagData(filter, target);
                isWriteSuccess = true;
                pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                pickedTag.isUploaded = false;
                pickedTag.newEpc = (/*sgtin.toUpperCase().trim().matches("^(3000|3400|4000|4400).*$") ? sgtin.replaceFirst("(3000|3400|4000|4400)", "") : */sgtin).toUpperCase().trim();
                //inventoryDao.updateInventoryData(pickedTag);s
                try{
                  if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
                    inventoryDao.updateInventoryData(pickedTag);
                  else inventoryDao.insertInventoryData(pickedTag);
                  uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
                }
                catch(Exception ee){ ee.printStackTrace(); }
                AppCommonMethods.successBeep();
                try{
                  final ProductDao productDao = AppDatabase.getProductDao(context);
                  final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                  if(productDao != null && sessionType == AppCommonMethods.SessionType.OMNICHANNEL)
                    productDao.updateDecodedEPC(pickedTag.epc, pickedTag.ean, pickedTag.zone);
                  else if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
                    fifoDao.updateDecoded(pickedTag.ean, pickedTag.epc, fifoDate);
                }
                catch(Exception ex){ ex.printStackTrace(); }
                updateTagWriteCount(true);
                break;
              }
              catch(RfidReaderException e){
                e.printStackTrace();
              }
            }
            if(!isWriteSuccess){
              /*if(isMultiWrite) multiWriteCount--;
              if(multiWriteCount == 0 && !isMultiWriteDone){
                isMultiWriteDone = true;
                showLog("Remaining:", multiWriteSuccessCount + " of " + multiWriteListSize);
                updateMultiDecodeCount(isMultiWrite, isMultiWrite && multiWriteSuccessCount > 0);
                showLog("WRITE_PASSWORDS", "FAIL");
              }*/
              updateTagWriteCount(context.getString(R.string.err_decoding_auth_fail));
              showLog("WRITE_PASSWORDS", "FAIL");
            }
          }
        });
      }
      else{
          /*BankInfo target=new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
        target.setStartAddr(offSet);*/
        target.setPassword(SharedPrefManager.getCurrentAccessPassword());
        //target.setBankValue(sgtin);
        final int finalOffSet = offSet;
        new Handler().post(new Runnable(){
          @Override
          public void run(){
            try{
              mReader.writeTagData(filter, target);
              showLog("SUCCESS", "BY CURRENT PASSWORD");
              //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
              pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
              pickedTag.isUploaded = false;
              pickedTag.newEpc = (/*sgtin.toUpperCase().trim().matches("^(3000|3400|4000|4400).*$") ? sgtin.replaceFirst("(3000|3400|4000|4400)", "") : */sgtin).toUpperCase().trim();
              //inventoryDao.updateInventoryData(pickedTag);s
              try{
                if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
                  inventoryDao.updateInventoryData(pickedTag);
                else inventoryDao.insertInventoryData(pickedTag);
                uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
              }
              catch(Exception e){ e.printStackTrace(); }
              AppCommonMethods.successBeep();
              try{
                final ProductDao productDao = AppDatabase.getProductDao(context);
                final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                if(productDao != null && sessionType == AppCommonMethods.SessionType.OMNICHANNEL)
                  productDao.updateDecodedEPC(pickedTag.epc, pickedTag.ean, pickedTag.zone);
                else if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
                  fifoDao.updateDecoded(pickedTag.ean, pickedTag.epc, fifoDate);
              }
              catch(Exception e){ e.printStackTrace(); }
              /*if(isMultiWrite){
                multiWriteSuccessCount++;
                multiWriteCount--;
              }
              if(multiWriteCount == 0 && !isMultiWriteDone){
                updateMultiDecodeCount(isMultiWrite, true);
              }*/
              updateTagWriteCount(true);
            }
            catch(RfidReaderException e){
              e.printStackTrace();
              //TODO do for other passwords
              List<String> passwords = SharedPrefManager.getOldAccessPasswords();
              if(passwords.contains(defaultTagZeroPassword))
                passwords.remove(defaultTagZeroPassword);
              passwords.add(0, defaultTagZeroPassword);
              
              AppCommonMethods.showLog("passwords", passwords.size() + "_" + passwords.toString());
              boolean isWriteSuccess = false;
              /*BankInfo target1=new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
              // EPC
              target1.setStartAddr(finalOffSet);
              //target1.setPassword(oldPassword);
              target1.setBankValue(sgtin);*/
              for(String oldPassword : passwords){
                //BankInfo target1=new BankInfo(BankInfo.from(TagAdditionData.EPC_BANK));
                // EPC
                //target1.setStartAddr(offSet);
                //target.setPassword(oldPassword);
                //target1.setBankValue(sgtin);
                try{
                  //target1.setPassword(oldPassword);
                  target.setPassword(oldPassword);
                  AppCommonMethods.showLog("oldPassword", oldPassword);
                  mReader.writeTagData(filter, target);
                  showLog("SUCCESS", "BY PASSWORD: " + oldPassword);
                  isWriteSuccess = true;
                  pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                  pickedTag.isUploaded = false;
                  pickedTag.newEpc = (/*sgtin.toUpperCase().trim().matches("^(3000|3400|4000|4400).*$") ? sgtin.replaceFirst("(3000|3400|4000|4400)", "") : */sgtin).toUpperCase().trim();
                  //inventoryDao.updateInventoryData(pickedTag);s
                  try{
                    if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
                      inventoryDao.updateInventoryData(pickedTag);
                    else inventoryDao.insertInventoryData(pickedTag);
                    uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
                  }
                  catch(Exception ee){ ee.printStackTrace(); }
                  AppCommonMethods.successBeep();
                  try{
                    final ProductDao productDao = AppDatabase.getProductDao(context);
                    final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                    if(productDao != null && sessionType == AppCommonMethods.SessionType.OMNICHANNEL)
                      productDao.updateDecodedEPC(pickedTag.epc, pickedTag.ean, pickedTag.zone);
                    else if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
                      fifoDao.updateDecoded(pickedTag.ean, pickedTag.epc, fifoDate);
                  }
                  catch(Exception ex){ ex.printStackTrace(); }
                  /*if(isMultiWrite){
                    multiWriteSuccessCount++;
                    multiWriteCount--;
                  }
                  if(multiWriteCount == 0 && !isMultiWriteDone){
                    updateMultiDecodeCount(isMultiWrite, true);
                  }*/
                  updateTagWriteCount(true);
                  break;
                }
                catch(RfidReaderException ex){
                  e.printStackTrace();
                }
              }
              if(!isWriteSuccess){
                /*if(isMultiWrite) multiWriteCount--;
                if(multiWriteCount == 0 && !isMultiWriteDone){
                  isMultiWriteDone = true;
                  showLog("Remaining:", multiWriteSuccessCount + " of " + multiWriteListSize);
                  updateMultiDecodeCount(isMultiWrite, isMultiWrite && multiWriteSuccessCount > 0);
                }*/
                updateTagWriteCount(context.getString(R.string.err_decoding_auth_fail));
                showLog("WRITE_PASSWORDS", "FAIL");
              }
            }
          }
        });
      }
    }
    else{
      // fail
      /*if(isMultiWrite) multiWriteCount--;
      if(multiWriteCount == 0 && !isMultiWriteDone){
        updateMultiDecodeCount(isMultiWrite, isMultiWrite && multiWriteSuccessCount > 0);
        showLog("LOCKMEMORY1", "FAIL");
      }*/
      updateTagWriteCount(context.getString(R.string.err_decoding_write_fail));
    }
  }
  
  /*private void updateMultiDecodeCount(final boolean isMultiDecode, final boolean isDecoded){
    isMultiWriteDone = true;
    showLog("updateMultiDecodeCount", "updateMultiDecodeCount");
    if(sessionAction == AppCommonMethods.SessionAction.DECODE && multiWriteCount == 0){
      showLog("updateMultiDecodeCount1", "updateMultiDecodeCount1");
      isDecodeDone.postValue(isDecoded);
      setProgressMessage(false);
      new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
        @Override
        public void run(){
          setProgressMessage(false);
          if(isDecoded && sessionType != AppCommonMethods.SessionType.DECODING)
            ((MainActivity) context).showCustomSuccessDialog(context.getString(R.string.success_decoding) + (isMultiDecode ? "\n" + "( " + multiWriteSuccessCount + " / " + multiWriteListSize + " )" : ""));
          else if(!isDecoded)
            ((MainActivity) context).showCustomErrDialog(R.string.err_decoding_fail);
          isDecodeOn.postValue(false);
        }
      }, isMultiDecode ? 50 : 0);
    }
  }*/
  
  private void setPower(final int readPower){
    setPower(readPower, MAX_POWER_TO_SET);
  }
  
  private void setPower(final int readPower, final int writePower){
    if(isReaderConnected()){
      try{
        AntennaPower[] ap = new AntennaPower[1];
        ap[0] = new AntennaPower(1, readPower, writePower);
        rfidMgr.setAntennaPower(ap);
      }
      catch(RfidReaderException e){
        e.printStackTrace();
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  
  private void clearFilters(){
    try{
      mReader.getTarget();
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Perform decoding.
   *
   * @param sessionType the session type
   */
  @Override
  public void performDecoding(final AppCommonMethods.SessionType sessionType){
    performDecoding(inventoryDao.getAllNonDecodedInventoryData(sessionType.getValue()), sessionType);
  }
  
  /**
   * Perform decoding.
   *
   * @param pickedTags the picked tags
   */
  @Override
  public void performDecoding(final List<Inventory> pickedTags){
    performDecoding(pickedTags, AppCommonMethods.SessionType.OMNICHANNEL);
  }
  
  @Override
  public void performDecoding(final List<Inventory> pickedTags, final AppCommonMethods.SessionType sessionType){
    showLog("pickedTags", "" + pickedTags.size());
    showLog("sessionType", "" + sessionType.name());
    if(isNonEmpty(pickedTags) && sessionType.getValue() > 0){
      if(mReader != null){
        this.sessionType = sessionType;
        sessionAction = AppCommonMethods.SessionAction.DECODE;
        multiWriteSuccessCount = 0;
        multiWriteListSize = pickedTags.size();
        multiWriteCount = multiWriteListSize;
        isMultiWriteDone = false;
        try{
          clearFilters();
          mReader.setSession(Gen2.Session.Session0);
          setPower(MAX_POWER_TO_SET);
          //configAction(MAX_POWER_TO_SET - 30, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
          //reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
          
        }
        catch(RfidReaderException e){
          e.printStackTrace();
        }
        catch(Exception e){
          e.printStackTrace();
        }
        isDecodeOn.postValue(true);
        setProgressMessage(context.getString(R.string.msg_pick), true);
        for(Inventory inventory : pickedTags){
          if(inventory != null && !inventory.isDecoded() && isNonEmpty(inventory.epc) && !inventory.epc.startsWith("0")){
            startDecoding(inventory, SharedPrefManager.getCurrentAccessPassword());
          }
          else multiWriteCount--;
        }
        if(multiWriteCount == 0 && !isMultiWriteDone){
          isDecodeOn.postValue(false);
          setProgressMessage(false);
        }
      }
      else{
        isDecodeOn.postValue(false);
        ((MainActivity) context).showCustomErrDialog(R.string.err_decoding_fail);
        setProgressMessage(false);
        //fail
      }
    }
  }
  
  @Override
  public void performDecoding(final Inventory pickedTag){
    if(pickedTag == null) return;
    if(mReader != null){
      sessionAction = AppCommonMethods.SessionAction.DECODE;
      isDecodeOn.postValue(true);
      isMultiWriteDone = false;
      setProgressMessage(context.getString(R.string.msg_pick), true);
      try{
        clearFilters();
        mReader.setSession(Gen2.Session.Session0);
        setPower(MAX_POWER_TO_SET);
      }
      catch(RfidReaderException e){
        e.printStackTrace();
      }
      catch(Exception e){
        e.printStackTrace();
      }
      startDecoding(pickedTag, SharedPrefManager.getCurrentAccessPassword());
    }
    else{
      isDecodeOn.postValue(false);
      ((MainActivity) context).showCustomErrDialog(R.string.err_decoding_fail);
      setProgressMessage(false);
      //fail
    }
  }
  
  @Override
  public boolean performInventory(final boolean isHideUnencodedTags,final List<String> listIgnoreEPCs){
    if(super.performInventory(isHideUnencodedTags,listIgnoreEPCs)){
      sessionAction = AppCommonMethods.SessionAction.INVENTORY;
      readTags();
      return true;
    }
    return false;
  }
  
  @Override
  public void onDestroy(){
    new Handler().post(new Runnable(){
      @Override
      public void run(){
        dispose();
      }
    });
    super.onDestroy();
  }
  
  private void dispose(){
    try{
      if(mReader != null){
        mReader.release();
      }
      if(rfidMgr != null){
        rfidMgr.removeEventListener(this);
        rfidMgr.disconnect();
        rfidMgr = null;
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  @Override
  public void stopInventory(){
    super.stopInventory();
    try{
      loopFlag = false;
      if(mReader != null){
        mReader.stopRead();
        mReader.removeOnTagReadListener(this);
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  /**
   * On resume.
   *
   * @param sessionType the session type
   */
  @Override
  public void onResume(AppCommonMethods.SessionType sessionType){
    super.onResume(sessionType);
    if(rfidMgr != null) rfidMgr.addEventListener(this);
  }
  
  /**
   * On pause.
   */
  @Override
  public void onPause(){
    super.onPause();
    //stopInventory();
    if(rfidMgr != null) rfidMgr.removeEventListener(this);
  }
  
  @Override
  public void onDeviceConnected(Object o){
    //rfidMgr.connect();
    rfidMgr.createReader();
  }
  
  @Override
  public void onDeviceDisconnected(Object o){
    disconnect();
  }
  
  @Override
  public void onDeviceReconnectFailure(String s){
    EventListener.super.onDeviceReconnectFailure(s);
  }
  
  @Override
  public void onDeviceReconnectSuccess(){
    EventListener.super.onDeviceReconnectSuccess();
  }
  
  @Override
  public void onReaderCreated(boolean b, RfidReader rfidReader){
    mReader = rfidReader;
    showLog(TAG + "onReaderCreated", "" + (mReader != null));
    if(b) configureReader();
    else scan();
  }
  
  @Override
  public void onRfidTriggered(boolean trigger){
    if(isReaderConnected() && mReader.available()) setTriggerPressed();
    /*if(isReaderConnected() && !restrictTriggerPress){
      isTriggerPressed.postValue(trigger && mReader.available());
      checkTimer();
    }*/
  }
  
  /**
   * Update trigger mode.
   *
   * @param isBarcodeMode the is barcode mode
   */
  public void updateTriggerMode(boolean isBarcodeMode){
    if(rfidMgr != null && isReaderConnected() && sessionType.getValue() > 0){
      try{
        rfidMgr.setTriggerMode(isBarcodeMode ? TriggerMode.BARCODE_SCAN : TriggerMode.RFID);
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  
  @Override
  public void onTriggerModeSwitched(TriggerMode triggerMode){
    showLog(TAG + "_TriggerMode", triggerMode.name());
  }
  
  @Override
  public void onBleRssiRead(int i){
    EventListener.super.onBleRssiRead(i);
  }
  
  @Override
  public void onBleRssiRead(BluetoothDevice bluetoothDevice, int i){
    EventListener.super.onBleRssiRead(bluetoothDevice, i);
  }
  
  @Override
  public void onResetResult(boolean b, String s){
    EventListener.super.onResetResult(b, s);
  }
  
  @Override
  public void onTagRead(TagReadData[] tagReadData){
    showLog("onTagRead", "onTagRead");
    if(loopFlag && tagReadData != null && tagReadData.length > 0){
      for(TagReadData tagData : tagReadData){
        if(loopFlag){
          showLog("LOOPFLAG", "TRUE");
          if(tagData != null && isNonEmpty(tagData.getEpcHexStr()) /*&& chkNull(tagData.getEpcHexStr(), "").length() >= 24*/){
            storeInventoryData(tagData);
          }
        }
        else break;
      }
    }
  }
  
  /**
   * Store inventory data.
   *
   * @param tagData the tag data
   */
  private void storeInventoryData(final TagReadData tagData){
    //if(!loopFlag) return;
    final String epcdt = tagData.getEpcHexStr();
    if(sessionType == AppCommonMethods.SessionType.SEARCH_FIFO && sessionAction == AppCommonMethods.SessionAction.SEARCH && !isCommandForEPCSearch && !AppDatabase.getFIFODao(context).isEPCPresent(epcdt, ean, fifoDate/*,zone,zoneId*/))
      return;
    if(isNonEmpty(epcdt)){
      showLog(sessionAction.name() + "_epcdt", epcdt);
      AppCommonMethods.logInFile(context, sessionType.name() + "_TAG_EPC_" + chkNull(epcdt, "") + (sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION ? "_Status_" + AppDatabase.getProductDao(context).isEPCPresent(chkNull(epcdt, "")) : ""));
    }
    if((isNonEmpty(sessionId) || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.SCAN) && sessionType.getValue() > 0){
      showLog(TAG + "_" + sessionAction.name() + "_" + "finalsgtinepc", epcdt);
      try{
        if(isCommandForSearch || sessionAction == AppCommonMethods.SessionAction.SEARCH){
          final int rssi = tagData.getRssi();
          showLog("RFIDHANDLER_rssi", "" + rssi);
          int actualPercentage = getPercentage(rssi);
          percent = actualPercentage;
          showLog("RFIDHANDLER_actualPercentage", "" + actualPercentage);
          if(!isCommandForEPCSearch){
            if(isLockSearchEPC && isNullOrEmpty(SEARCH_LOCKED_EPC)){
              SEARCH_LOCKED_EPC = epcdt;
            }
            if(!isLockSearchEPC || isNullOrEmpty(SEARCH_LOCKED_EPC) || epcdt.equalsIgnoreCase(SEARCH_LOCKED_EPC)){
              if(epcdt.length() >= 24){
                //check by using getBarcode method instead of switch case
                final String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt);
                final String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context,SEARCH_BARCODE);
                showLog("search_barcode", barcode);
                showLog("search_compare_barcode", compbarcode);
                if(!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase()))){
                  showLog("RFIDHANDLER_barcode", "" + barcode);
                  searchPercent.postValue(actualPercentage);
                  searchRssi.postValue(String.valueOf(tagData.getRssi()));
                  if(actualPercentage > 90){
                    counter_for_threshold_percentage_to_sound_beep++;
                    if(counter_for_threshold_percentage_to_sound_beep >= SOUND_THRESHOLD){
                      counter_for_threshold_percentage_to_sound_beep = 0;
                      playSound(context, R.raw.successbeep);
                    }
                  }
                }
              }
            }
          }
          else{
            if(epcdt.equalsIgnoreCase(SEARCH_EPC) || ((sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO) && epcdt.length() > 1 && epcdt.equalsIgnoreCase("0" + SEARCH_EPC.substring(1)))){
              searchPercent.postValue(actualPercentage);
              searchRssi.postValue(String.valueOf(tagData.getRssi()));
              if(actualPercentage > 90){
                counter_for_threshold_percentage_to_sound_beep++;
                if(counter_for_threshold_percentage_to_sound_beep >= SOUND_THRESHOLD){
                  counter_for_threshold_percentage_to_sound_beep = 0;
                  playSound(context, R.raw.successbeep);
                }
              }
            }
          }
        }
        else{
          final BrandEanDao brandEansDao = AppDatabase.getBrandEansDao(context);
          final ProductDao productDao = AppDatabase.getProductDao(context);
          final FIFODao fifoDao = AppDatabase.getFIFODao(context);
          if(isSinglePick && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK)){
            showLog("epc_pick", "" + epcdt);
            final String tid = (readTid || (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO) ? bytesToHex(tagData.getAdditionData()) : "";
            final String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt);
            final String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context,SEARCH_BARCODE);
            showLog("pick_barcode", barcode);
            showLog("pick_compare_barcode", compbarcode);
            //showLog("isActionTidPick", isActionTidPick+"_"+SCANNED_TIDS.toString());
            if(sessionType == AppCommonMethods.SessionType.ENCODING || epcdt.length() >= 24){
              //final String tid = readTid? "" + bytesToHex(tagData.getAdditionData()) : "";
              //if(tid.length()>24) tid=tid.substring(0,24);
              showLog("isActionTidPick_scannedTids", isActionPick + "_" + SCANNED_TIDS.size());
              showLog("isActionTidPick_tid", isActionPick + "_" + tid);
              if(isSinglePick && ((!isActionTidPick && !pickTags.contains(epcdt)) || (isActionTidPick && !pickTags.contains(tid) && SCANNED_TIDS.contains(tid)/*tid.matches("(?i)(^" + SCANNED_TID + ".*$)")*/))){
                pickTags.add(isActionTidPick ? tid : epcdt);
                showLog("pickTags.add", isActionTidPick ? tid : epcdt);
                pickUHFTags.add(tagData);
                if(isActionTidPick && pickTags.size() == SCANNED_TIDS.size()){//1){
                  showLog("pickTags", "" + pickTags.size());
                  loopFlag = false;
                  pickCountDownTimer.cancel();
                  pickCountDownTimer.onFinish();
                }
                else if(pickTags.size() > (isActionTidPick ? SCANNED_TIDS.size() : 1)){
                  showLog("pickTags", "" + pickTags.size());
                  setProgressMessage(false);
                  stopInventory();
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
            if(isSinglePick) return;
          }
          else{
            if(sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION && !productDao.isEPCPresent(epcdt)){
              return;
            }
            if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY && ((brandEansDao.hasData() && !brandEansDao.isEANPresent("," + context.epcEncoderDecoder.getBarcodeFromEPC(epcdt) + ",")) || (isNonEmpty(eans) && !eans.contains(context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase())))){
              return;
            }
            if(sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY && ((brandEansDao.hasData() && !brandEansDao.isEANPresent("," + context.epcEncoderDecoder.getBarcodeFromEPC(epcdt) + ",")) || (isNonEmpty(eans) && !eans.contains(context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase())))){
              return;
            }
            //Old Code (Commented)
            //if(sessionType == AppCommonMethods.SessionType.SEARCH_UNENCODED && !chkNull(context.epcEncoderDecoder.getBarcodeFromEPC(epcdt), AppConstants.NON_ENCODED).matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.UNKNOWN + ")")){
            //return;
            //}
            
            //Checks only Non-Alien Decoded Tags
            if(sessionType == AppCommonMethods.SessionType.SEARCH_UNENCODED && !context.epcEncoderDecoder.setDataFromEPC(new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue(), epcdt)).tagStatus.equalsIgnoreCase(AppConstants.NON_ENCODED))//!chkNull(ApplicationCommonMethods.getBarcodeFromEPC(uhftagInfo.getEPC()), AppConstants.NON_ENCODED).matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.UNKNOWN + ")"))
              return;
            //Checks only Alien Tags
            if(sessionType == AppCommonMethods.SessionType.SEARCH_ALIEN && !context.epcEncoderDecoder.setDataFromEPC(new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue(), epcdt)).tagStatus.equalsIgnoreCase(AppConstants.ALIEN))
              return;
            if(inventoryDao.isEPCPresent(sessionId, epcdt)) return;
            if(sessionAction.getValue() > 0 && sessionAction != AppCommonMethods.SessionAction.SEARCH && (!isSinglePick || sessionAction != AppCommonMethods.SessionAction.PICK)){
              try{
                showLog("SAVE", "YES");
                Inventory inventory = new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
                inventory.epc = epcdt.toUpperCase();
                inventory.tid = (readTid || (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO) ? bytesToHex(tagData.getAdditionData()) : "";//change to appropriate method
                inventory.rssi = readRssi ? "" + tagData.getRssi() : "";
                inventory.pcdata = readPC ? "" + bytesToHex(tagData.getPc()) : "";
                inventory.zone = zone;
                inventory.zoneId = zoneId;
                try{
                  inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
                }
                catch(Exception e){
                  e.printStackTrace();
                }
                inventory.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                if((/*!AppCommonMethods.isSetInwOnline &&*/ sessionType == AppCommonMethods.SessionType.INWARD) || sessionType == AppCommonMethods.SessionType.OUTWARD){
                  //Set 3rd Table for this
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
                  if(isNullOrEmpty(sessionId) || (isNullOrEmpty(inventory.tid) && (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)))
                    throw new NullPointerException();
                  inventoryDao.insertInventoryData(inventory);
                }
                if(productDao != null && sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION){
                  if(zone.equalsIgnoreCase(AppConstants.ALL)) productDao.updateFound(inventory.epc);
                  else productDao.updateFound(inventory.epc, zone);
                }
                if(productDao != null && sessionType == AppCommonMethods.SessionType.OMNICHANNEL)
                  productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
                if(productDao != null && sessionType == AppCommonMethods.SessionType.OUTWARD_PICK)
                  productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
                if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY)// || sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY)
                  brandEansDao.updateScanQty("," + inventory.ean + ",");
                if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO && fifoDate != null && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK))
                  fifoDao.updateFound(inventory.ean, inventory.epc, fifoDate);
              }
              catch(SQLiteConstraintException sql){//Don't handle
              }
              catch(Exception ex){
                ex.printStackTrace();
                showLog("SQLEXC", "" + ex.getMessage());
              }
            }
          }
        }
      }
      catch(Exception ex){
        ex.printStackTrace();
        showLog("EXCC", "" + ex.getMessage());
      }
    }
  }
  
  @Override
  protected Inventory getDataFromTagInfo(Object object){
    return object!=null && object instanceof TagReadData ? getDataFromTagInfo((TagReadData) object) : new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
  }
  
  /**
   * Get data from tag info inventory.
   *
   * @param tagData the tag data
   * @return the inventory
   */
  private Inventory getDataFromTagInfo(TagReadData tagData){
    Inventory inventory = new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
    inventory.epc = chkNull(tagData.getEpcHexStr(),"").toUpperCase();
    inventory.tid = (readTid || (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO) && isNonEmpty(tagData.getAdditionData()) ? bytesToHex(tagData.getAdditionData()) : "";//change to appropriate method
    inventory.rssi = readRssi ? "" + tagData.getRssi() : "";
    inventory.pcdata = readPC && isNonEmpty(tagData.getPc()) ? "" + bytesToHex(tagData.getPc()) : "";
    inventory.zone = zone;
    inventory.zoneId = zoneId;
    try{
      inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return inventory;
  }
  
  @SuppressLint("NewApi")
  private void scan(){
    if(!context.isUseBluetoothScanConnect || ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED){
      if(mBluetoothAdapter == null) mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      try{
        mDevices.clear();
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        bluetoothScanTimer.start();
      /*mRunnable=new Runnable(){
        @Override
        public void run(){
          stopScan();
        }
      };
      new Handler().postDelayed(mRunnable, 5 * 1000);*/
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  
  private boolean checkConnect(final String address){
    if(rfidMgr != null && !rfidMgr.isConnected() && rfidMgr.connect(address)){
      rfidMgr.createReader();
      SharedPrefManager.setString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS, address);
      return true;
    }
    else return false;
  }
  
  @SuppressLint("NewApi")
  private void stopScan(){
    if(!context.isUseBluetoothScanConnect || ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED){
      if(mBluetoothAdapter == null) mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      try{
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  
  private void connect(){
    if(isReaderConnected()) return;
    if(rfidMgr == null) return;
    rfidMgr.setDevicePower(true);
    String savedAddress = SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS, /*AppCommonMethods.isDebugApp ? "0C:23:69:19:5E:8F" :*/ "");
    
    if(rfidMgr.isSerialDevice() && rfidMgr.connect(null)) rfidMgr.createReader();
    else if(isBluetoothConnected()){
      if(isNonEmpty(savedAddress) && rfidMgr.connect(savedAddress))
        rfidMgr.createReader();//return DEVICE_STATUS_CONNECTED;
      else scan();
    }
    
    /*if(rfidMgr.isSerialDevice() || mSelectedIdx==-1 || mSelectedIdx>=mDevices.size()){
            return rfidMgr.connect("0C:23:69:19:5E:8F") ? DEVICE_STATUS_CONNECTED : DEVICE_STATUS_DISCONNECTED;
        } else if (mSelectedIdx >= 0 && mSelectedIdx >= mDevices.size()) {
            return rfidMgr.connect(mDevices.get(mSelectedIdx).dev.getAddress()) ? DEVICE_STATUS_CONNECTED : DEVICE_STATUS_DISCONNECTED;
    }
    else return DEVICE_STATUS_DISCONNECTED;*/
  }
  
  private void disconnect(){
    if(rfidMgr != null){
      rfidMgr.disconnect();
      rfidMgr.setDevicePower(false);
    }
  }
  
  @Override
  public void configureReader(){
    configureReader(null);
  }
  
  /**
   * Configure reader.
   *
   * @param sessionType the session type
   */
  @Override
  public void configureReader(AppCommonMethods.SessionType sessionType){
    this.sessionType = this.sessionType.getValue() == 0 && sessionType != null && sessionType.getValue() > 0 ? sessionType : this.sessionType;
    AppCommonMethods.SessionType type = this.sessionType.getValue() > 0 || sessionType == null || sessionType.getValue() == 0 ? this.sessionType : sessionType != null ? sessionType : this.sessionType;
    if(type.getValue() > 0){
      showLog(TAG, "configureReader");
      if(isReaderConnected()){
        showLog("Config Device", "true");
        setProgressMessage(context.getString(R.string.msg_config_reader), true);
        
        try{
          
          // mReader.setReadMode(mReader.getReadMode());
          
          if(type == AppCommonMethods.SessionType.INVENTORY || type == AppCommonMethods.SessionType.ADD_INVENTORY || type == AppCommonMethods.SessionType.BRAND_INVENTORY ||type == AppCommonMethods.SessionType.FILTER_INVENTORY || type == AppCommonMethods.SessionType.STOCK_CORRECTION){
            //reader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP);
            rfidMgr.setBeeper(false, 0x02, 0x05);
          }
          else{
            //reader.Config.setBeeperVolume(BEEPER_VOLUME.LOW_BEEP);
            rfidMgr.setBeeper(false, 0x02, 0x05);
          }
          
          final String region = mReader.getRegionV2().regionName;//mReader.getRegion().getName();
          showLog("Region", region);
          if(!region.toUpperCase().matches("^IND[A-Z]*$"))
            ConnectAndSetRegion(RegionV2.India);//"INDIA");
          
          //reader.Actions.PreFilters.deleteAll();
          int maxPower = type == AppCommonMethods.SessionType.SCAN || type == AppCommonMethods.SessionType.VERIFY_ENCODING || type == AppCommonMethods.SessionType.ENCODING ? MIN_POWER_TO_SET * 2 : type == AppCommonMethods.SessionType.MOVEMENT || type == AppCommonMethods.SessionType.INWARD || type == AppCommonMethods.SessionType.OUTWARD ? MAX_POWER_TO_SET / 2 : MAX_POWER_TO_SET;
          int power = chkZero(SharedPrefManager.getInt(type.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower / 100) * 100;
          showLog("maxPower", "" + maxPower);
          showLog("power", "" + power);
          // mReader.setAntennaPower(power);
          AntennaPower[] ap = new AntennaPower[1];
          ap[0] = new AntennaPower(1, power, MAX_POWER_TO_SET);
          rfidMgr.setAntennaPower(ap);
          mReader.setSession(Gen2.Session.Session0);
          //mReader.setReadMode()
          //configAction(power, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
          readerPower.postValue(power / 100);
          SharedPrefManager.setReaderPower(power / 100);
          SharedPrefManager.setInt(type.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), power / 100);
          
          rfidInterface.RFIDInitializationStatus(true, "", mReader);
          isDeviceConfigured.postValue(true);
          showLog("isDeviceConfigured", "" + true);
          setProgressMessage(false);
          //checkReaderCapabilities();
        }
        catch(Exception e){
          e.printStackTrace();
          setProgressMessage(false);
          rfidInterface.RFIDInitializationStatus(false, "", null);
          isDeviceConfigured.postValue(false);
        }
      }
      else{
        isDeviceConfigured.postValue(false);
        setProgressMessage(false);
      }
    }
    else setProgressMessage(false);
  }
  
  /**
   * Check timer.
   */
  /*private void checkTimer(){
    restrictTriggerPress = true;
    final Handler handler = new Handler(Looper.getMainLooper());
    handler.postDelayed(() -> {
      //Do something after 100ms
      restrictTriggerPress = false;
    }, 500);
  }*/
  
  @Override
  protected void saveSerialNo(){
    if(isNonEmpty(SharedPrefManager.getString(ParamConstants.DEVICE_SERIAL,""))) return;
    //TODO implement
  }
  
  private CountDownTimer bluetoothScanTimer = new CountDownTimer(5000, 1000){
    @Override
    public void onTick(long millisUntilFinished){ }
    
    @Override
    public void onFinish(){
      stopScan();
    }
  };
  
  @SuppressLint("NewApi")
  private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord){
      if(!context.isUseBluetoothScanConnect || ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED){
        if(device.getName() != null && isNonEmpty(device.getName())){
          showLog("device.getName()", device.getName());
          if(device.getName().matches(readerName)){
            if(!mDevices.contains(device)){
              mDevices.add(device);
              if(checkConnect(device.getAddress())){
                if(bluetoothScanTimer != null) bluetoothScanTimer.cancel();
                stopScan();
              }
            }
          }
        }
      }
    }
  };
}

