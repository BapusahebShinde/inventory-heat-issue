package com.itek.retail.reader.zebra;

import static com.itek.retail.common.AppCommonMethods.beep;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.errorBeep;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;
import static com.itek.retail.common.AppCommonMethods.isUseShortRangeSearchForAll;
import static com.itek.retail.common.AppCommonMethods.isUseShortRangeSearchForOnlyGID;
import static com.itek.retail.common.AppCommonMethods.showShortToast;
import static com.zebra.rfid.api3.RFIDResults.RFID_API_SUCCESS;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.BrandEanDao;
import com.itek.retail.database.FIFODao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.TripInventory;
import com.itek.retail.model.TripStatus;
import com.itek.retail.model.UploadInventory;
import com.itek.retail.reader.MainReaderRepository;
import com.itek.retail.reader.RFIDHandler;
import com.itek.retail.reader.RFIDInitInterface;
import com.itek.retail.ui.encoding.EncodingStartFragment;
import com.itek.retail.ui.home.MainActivity;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.BATCH_MODE;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.DYNAMIC_POWER_OPTIMIZATION;
import com.zebra.rfid.api3.ENUM_NEW_KEYLAYOUT_TYPE;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.Events;
import com.zebra.rfid.api3.FILTER_ACTION;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.LOCK_DATA_FIELD;
import com.zebra.rfid.api3.LOCK_PRIVILEGE;
import com.zebra.rfid.api3.MEMORY_BANK;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.PreFilters;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RegionInfo;
import com.zebra.rfid.api3.RegulatoryConfig;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATE_AWARE_ACTION;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.StopTrigger;
import com.zebra.rfid.api3.TAG_FIELD;
import com.zebra.rfid.api3.TARGET;
import com.zebra.rfid.api3.TagAccess;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TagStorageSettings;
import com.zebra.rfid.api3.TriggerInfo;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * The Zebra rfid handler.
 */
public class ZebraRFIDHandler extends RFIDHandler implements IDcsSdkApiDelegate, Readers.RFIDReaderEventHandler{
  
  private static final String DEVICE_STATUS_CONNECTED = "connected";
  private static final String DEVICE_STATUS_DISCONNECTED = "disconnected";
  private static final String DEVICE_BATTERY_LOW = "low";
  // RFID Reader
  private static Readers readers;
  private static ReaderDevice readerDevice;
  private static ArrayList<ReaderDevice> availableRFIDReaderList;
  private static RFIDReader reader;
  private static ActivityResultLauncher<Intent> bluetoothResultLauncher;
  private final int MIN_POWER_TO_SET = 50;
  private final int MAX_POWER_TO_SET = 300;
  private final int SOUND_THRESHOLD = 8;
  private final int WRITE_OPERATION_TIMEOUT = 500;//2000;
  private final int READ_OPERATION_TIMEOUT = 1000;
  String readername = "(?i)(MC|RFD)";
  Set<TagData> pickUHFTags = new HashSet<>(0);
  boolean isRePick = false;
  boolean isRetryEncode = false;
  boolean isReEncodeForOldEPC = false;
  String encodeOldEpc = "";
  // general
  private Boolean loopFlag = false;
  private boolean isTidBasedPick = false;
  private CountDownTimer pickTidBasedCountDownTimer = null;
  private EventHandler eventHandler;
  private boolean beepON = false;
  private boolean restrictTriggerPress = false;
  private Inventory encPickedTag = null;
  private TagData encPickedData = null;
  //private Timer pickTimer = null;
  //private List<String> passwords = new ArrayList<>(0);
  
  /**
   * Set battery configuration.
   */
  public static void setBatteryConfiguration(){
    if(reader != null){
      try{
        reader.Config.getDeviceStatus(true, false, false);
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  
  @Override
  public void onCreate(CommonActivity activity, MainReaderRepository mainReaderRepository, RFIDInitInterface rfidInterface, AppCommonMethods.SessionType sessionType){
    super.onCreate(activity, mainReaderRepository, rfidInterface, sessionType);
  }
  
  @Override
  public boolean isReaderConnected(){
    boolean isBluetoothOn = true;
    if(SharedPrefManager.getIsDeviceBluetoothDependent()){
      final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      isBluetoothOn = mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }
    if(isBluetoothOn && reader != null && reader.isConnected()){
      return true;
    }
    else{
      showLog(TAG, "reader is not connected");
      return false;
    }
  }
  
  @Override
  public boolean isReaderPresent(boolean isReaderInstanceSet){
    boolean isBluetoothOn = true;
    if(SharedPrefManager.getIsDeviceBluetoothDependent()){
      final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      isBluetoothOn = mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }
    if(isBluetoothOn && (reader != null || isReaderInstanceSet)){
      return true;
    }
    else{
      showLog(TAG, "reader is not connected");
      return false;
    }
  }
  
  /**
   * On resume string.
   *
   * @param sessionType the session type
   * @return the string
   */
  public void onResume(AppCommonMethods.SessionType sessionType){
    showLog("ZebraRFIDHandler", "onResume");
    super.onResume(sessionType);
    String result = connect();
    showLog("result", "" + result);
    if(reader != null && result.equalsIgnoreCase(DEVICE_STATUS_CONNECTED)){
      showLog("ZebraRFIDHandler", "onResumeConfig");
      configureReader(sessionType);
    }
    else if(result.contains("Failed to find or connect reader")){
      setProgressMessage(false);
      rfidInterface.RFIDInitializationStatus(false, "Failed to find or connect reader", null);
    }
    else if(result.contains("Connection failed RFID_COMM_OPEN_ERROR")){
      setProgressMessage(false);
      rfidInterface.RFIDInitializationStatus(false, "Connection failed RFID_COMM_OPEN_ERROR", null);
    }
    else if(result.contains("Connection failed null RFID_COMM_OPEN_ERROR")){
      setProgressMessage(false);
      rfidInterface.RFIDInitializationStatus(false, "Connection failed null RFID_COMM_OPEN_ERROR", null);
    }
    else{
      setProgressMessage(false);
      if(result.contains("RFID_READER_REGION_NOT_CONFIGURED")){
        ConnectAndSetRegion("India");
        rfidInterface.RFIDInitializationStatus(true, "RFID_READER_REGION_NOT_CONFIGURED", reader);
      }
      else{
        checkAndConnectReader();
      }
    }
  }
  
  /**
   * On pause.
   */
  public void onPause(){
    //enableReaderBeep();
    super.onPause();
    isTidBasedPick = false;
    if(pickCountDownTimer != null) pickCountDownTimer.cancel();
    encPickedTag = null;
    disconnect();
  }
  
  /**
   * On destroy.
   */
  public void onDestroy(){
    new Handler().post(new Runnable(){
      @Override
      public void run(){
        dispose();
      }
    });
    super.onDestroy();
  }
  
  /**
   * Enable reader beep.
   */
  public void enableReaderBeep(){
    if(!isReaderConnected()) return;
    try{
      if(reader != null && reader.isConnected() && reader.Config != null)
        reader.Config.setBeeperVolume(BEEPER_VOLUME.LOW_BEEP);
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  /**
   * Disable reader beep.
   */
  public void disableReaderBeep(){
    if(!isReaderConnected()) return;
    try{
      if(reader != null && reader.isConnected() && reader.Config != null)
        reader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP);
      
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  public void InitSDK(){
    InitSDK(true, true);
  }
  
  /**
   * Init sdk.
   *
   * @param isConnect         the is connect
   * @param isConfigureDevice the is configure device
   */
  public void InitSDK(boolean isConnect, boolean isConfigureDevice){
    showLog("INIT", "ggg");
    super.InitSDK();
    if(readers == null || reader == null){
      new CreateInstanceTask(isConfigureDevice).execute();
    }
    else if(isConnect && !isReaderConnected()){
      new ConnectionTask(isConfigureDevice).execute();
    }
    else if(isConfigureDevice){
      configureReader();
    }
  }
  
  /**
   * Connect and set region.
   *
   * @param region the region
   */
  public void ConnectAndSetRegion(String region){
    showLog(TAG, "ConnectAndSetRegion");
    this.isCommandForSearch = false;
    this.isCommandForTIDSearch = false;
    this.isCommandForEPCSearch = false;
    this.isLockSearchEPC = false;
    this.SEARCH_EPC = "";
    this.SEARCH_BARCODE = "";
    this.SEARCH_LOCKED_EPC = "";
    setProgressMessage(true);
    new Thread(() -> {
      showLog(TAG, "region not set");
      RegionInfo selectedRegionInfo = null;
      try{
        int a = reader.ReaderCapabilities.SupportedRegions.length();
        for(int reagions = 0; reagions < a; reagions++){
          selectedRegionInfo = reader.Config.getRegionInfo(reader.ReaderCapabilities.SupportedRegions.getRegionInfo(reagions));
          String channelname = selectedRegionInfo.getName();
          if(channelname.equalsIgnoreCase(region)){
            break;
          }
        }
        if(selectedRegionInfo != null){
          RegulatoryConfig regulatoryConfig;
          regulatoryConfig = reader.Config.getRegulatoryConfig();
          regulatoryConfig.setRegion(selectedRegionInfo.getRegionCode());
          regulatoryConfig.setIsHoppingOn(selectedRegionInfo.isHoppingConfigurable());
          regulatoryConfig.setEnabledChannels(selectedRegionInfo.getSupportedChannels());
          reader.Config.setRegulatoryConfig(regulatoryConfig);
          rfidInterface.RFIDInitializationStatus(true, "", reader);
          setProgressMessage(false);
          
        }
        else{
          
          context.showCustomErrDialog(String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
          rfidInterface.RFIDInitializationStatus(false, "", reader);
          setProgressMessage(false);
        }
      }
      catch(InvalidUsageException invalidUsageException){
        invalidUsageException.printStackTrace();
        showLog("EXC", invalidUsageException.getVendorMessage());
        
        context.showCustomErrDialog(String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        rfidInterface.RFIDInitializationStatus(false, "", null);
        setProgressMessage(false);
        return;
      }
      catch(OperationFailureException operationFailureException){
        operationFailureException.printStackTrace();
        showLog("EXC2", operationFailureException.getVendorMessage());
        
        context.showCustomErrDialog(String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        rfidInterface.RFIDInitializationStatus(false, "", null);
        setProgressMessage(false);
        return;
      }
      catch(Exception e){
        e.printStackTrace();
        showLog("EXC3", e.getMessage());
        
        context.showCustomErrDialog(String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        rfidInterface.RFIDInitializationStatus(false, "", null);
        setProgressMessage(false);
        return;
      }
    }).start();
  }
  
  /**
   * Get reader instance object.
   *
   * @param context              the context
   * @param mainReaderRepository the main reader repository
   * @param rfidInterface        the rfid interface
   * @return the object
   */
  public Object getReaderInstance(CommonActivity context, MainReaderRepository mainReaderRepository, RFIDInitInterface rfidInterface){
    this.context = context;
    this.mainReaderRepository = mainReaderRepository;
    this.rfidInterface = rfidInterface;
    /*if(SharedPrefManager.getIsDeviceBluetoothDependent())*/
    registerBluetoothEvent();
    try{
      try{
        readers = new Readers(context, ENUM_TRANSPORT.SERVICE_SERIAL);
        availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
        //Latest SDK (2.0.2.86)
        if(isNullOrEmpty(availableRFIDReaderList)){
          readers.Dispose();
          readers = null;
          readers = new Readers(context, ENUM_TRANSPORT.SERVICE_USB);
          availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
        }
        if(isNullOrEmpty(availableRFIDReaderList)){
          readers.Dispose();
          readers = null;
          readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
          availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
        }
      }
      catch(InvalidUsageException e){
        e.printStackTrace();
        readers.Dispose();
        readers = null;
        readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
      }
      
      if(readers != null){
        Readers.attach(this);
        if(readers.GetAvailableRFIDReaderList() != null){
          availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
          showLog("SIZE", availableRFIDReaderList.size() + "");
          if(availableRFIDReaderList.size() != 0){
            // if single reader is available then connect it
            if(availableRFIDReaderList.size() == 1){
              readerDevice = availableRFIDReaderList.get(0);
              reader = readerDevice.getRFIDReader();
              if(reader != null) saveSerialNo();
            }
            else{
              // search reader specified by name
              for(ReaderDevice device : availableRFIDReaderList){
                showLog(TAG, "NAME" + device.getName());
                if(device.getName().matches(readername)){
                  reader = device.getRFIDReader();
                  if(reader != null){
                    readerDevice = device;
                    saveSerialNo();
                  }
                }
              }
            }
          }
        }
      }
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      showLog("AVAILISTEXC", e.getLocalizedMessage());
      readers.Dispose();
      readers = null;
      readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
    }
    if(reader != null && reader.getTransport().equals("SERVICE_USB"))
      new ConnectionTask(false).execute();
    return reader;
  }
  
  @Override
  protected void saveSerialNo(){
    if(isNonEmpty(SharedPrefManager.getString(ParamConstants.DEVICE_SERIAL, ""))) return;
    final String readerName = readerDevice.getName();
    String deviceSerialNo = "";
    if(readerName.startsWith("MC33")){
      if(reader != null && reader.ReaderCapabilities != null)
        deviceSerialNo = reader.ReaderCapabilities.getSerialNumber();
      else{
        String[] splitStr = readerName.split("R");
        if(isNonEmpty(splitStr)) deviceSerialNo = splitStr[1];
      }
    }
    else if(readerName.startsWith("RFD40")){
      deviceSerialNo = readerDevice.getSerialNumber();
      if(readerName.startsWith("RFD40+") || readerName.startsWith("RFD40P")){
        String serialno[] = readerName.split("_");
        if(isNonEmpty(serialno)) deviceSerialNo = serialno[serialno.length - 1];
      }
    }
    else if(readerName.startsWith("RFD90+")){
      String serialno[] = readerName.split("_");
      if(isNonEmpty(serialno)) deviceSerialNo = serialno[serialno.length - 1];
    }
    else if(readerName.startsWith("RFD90")){
      String serialno[] = readerDevice.getSerialNumber().split("S/N:");
      if(isNonEmpty(serialno)) deviceSerialNo = serialno[1];
    }
    else if(readerName.startsWith("RFD8500")){
      String[] splitStr = readerName.split("RFD8500");
      if(isNonEmpty(splitStr)) deviceSerialNo = splitStr[1];
    }
    if(isNonEmpty(deviceSerialNo)){
      SharedPrefManager.setString(ParamConstants.DEVICE_SERIAL, deviceSerialNo);
    }
  }
  
  /**
   * Get available reader.
   */
  private synchronized void GetAvailableReader(){
    try{
      if(readers != null){
        Readers.attach(this);
        if(readers.GetAvailableRFIDReaderList() != null){
          availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
          showLog("SIZE", availableRFIDReaderList.size() + "");
          if(availableRFIDReaderList.size() != 0){
            // if single reader is available then connect it
            if(availableRFIDReaderList.size() == 1){
              reader = availableRFIDReaderList.get(0).getRFIDReader();
            }
            else{
              // search reader specified by name
              for(ReaderDevice device : availableRFIDReaderList){
                showLog(TAG, "NAME" + device.getName());
                if(device.getName().matches(readername)){
                  reader = device.getRFIDReader();
                }
              }
            }
          }
        }
      }
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      showLog("AVAILISTEXC", e.getLocalizedMessage());
      readers.Dispose();
      readers = null;
      if(readers == null){
        readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
      }
    }
  }
  
  // handler for receiving reader appearance events
  @Override
  public void RFIDReaderAppeared(ReaderDevice readerDevice){
    showLog(TAG, "RFIDReaderAppeared " + (readerDevice != null ? readerDevice.getName() : ""));
  }
  
  @Override
  public void RFIDReaderDisappeared(ReaderDevice readerDevice){
    showLog(TAG, "RFIDReaderDisappeared " + (readerDevice != null ? readerDevice.getName() : ""));
    if(readerDevice != null && reader != null && readerDevice.getName().equals(reader.getHostName()))
      disconnect();
  }
  
  /**
   * Register bluetooth event.
   */
  private void registerBluetoothEvent(){
    if(SharedPrefManager.getIsDeviceBluetoothDependent() && context != null && !context.isFinishing()){
      bluetoothResultLauncher = context.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
        @Override
        public void onActivityResult(ActivityResult result){
          checkAndConnectReader();
        }
      });
      showLog("bluetoothResultLauncher", "" + (bluetoothResultLauncher != null));
    }
  }
  
  /**
   * Check and set reader.
   */
  public void checkAndSetReader(){
    if(SharedPrefManager.getIsDeviceBluetoothDependent()){
      final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if(mBluetoothAdapter == null){
        
        context.showCustomErrDialog(String.format(context.getString(R.string.err_no_bluetooth), getTypeCharCode()));
        return;
      }
      else if(!mBluetoothAdapter.isEnabled() /*&& bluetoothResultLauncher != null*/){
        
        context.showCustomAlertDialog("", String.format(context.getString(R.string.err_bluetooth_disabled), getTypeCharCode()), null, false, false, context.getString(bluetoothResultLauncher != null ? R.string.btn_enable : R.string.btn_ok), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            if(bluetoothResultLauncher != null)
              bluetoothResultLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
          }
        }, bluetoothResultLauncher != null ? context.getString(R.string.btn_cancel) : "", null);
        
        return;
      }
    }
    if(reader == null) InitSDK(true, false);
    else if(reader != null && !reader.isConnected()) new ConnectionTask(false).execute();
  }
  
  public void checkAndConnectReader(){
    if(SharedPrefManager.getIsDeviceBluetoothDependent()){
      final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if(mBluetoothAdapter == null){
        showShortToast(context, String.format(context.getString(R.string.err_no_bluetooth), getTypeCharCode()));
        return;
      }
      else if(!mBluetoothAdapter.isEnabled() /*&& bluetoothResultLauncher != null*/){
        showShortToast(context, String.format(context.getString(R.string.err_bluetooth_disabled), getTypeCharCode()));
        context.showCustomAlertDialog("", String.format(context.getString(R.string.err_bluetooth_disabled), getTypeCharCode()), null, false, false, context.getString(bluetoothResultLauncher != null ? R.string.btn_enable : R.string.btn_ok), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            if(bluetoothResultLauncher != null)
              bluetoothResultLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
          }
        }, bluetoothResultLauncher != null ? context.getString(R.string.btn_cancel) : "", null);
        
        return;
      }
    }
    if(reader == null) InitSDK(true, true);
    else if(reader != null && !reader.isConnected()){
      String result = connect();
      if(reader != null && result.equalsIgnoreCase(DEVICE_STATUS_CONNECTED)){
        configureReader();
      }
      else if(result.contains("Failed to find or connect reader")){
        
        setProgressMessage(false);
        rfidInterface.RFIDInitializationStatus(false, "Failed to find or connect reader", null);
      }
      else if(result.contains("Connection failed RFID_COMM_OPEN_ERROR")){
        
        setProgressMessage(false);
        rfidInterface.RFIDInitializationStatus(false, "Connection failed RFID_COMM_OPEN_ERROR", null);
      }
      else if(result.contains("Connection failed null RFID_COMM_OPEN_ERROR")){
        
        setProgressMessage(false);
        rfidInterface.RFIDInitializationStatus(false, "Connection failed null RFID_COMM_OPEN_ERROR", null);
      }
      else{
        setProgressMessage(false);
        if(result.contains("RFID_READER_REGION_NOT_CONFIGURED")){
          setProgressMessage(false);
          ConnectAndSetRegion("India");
          rfidInterface.RFIDInitializationStatus(true, "RFID_READER_REGION_NOT_CONFIGURED", reader);
        }
        else{
          
          setProgressMessage(false);
          rfidInterface.RFIDInitializationStatus(false, "", null);
        }
      }
      
    }
    else if(reader != null && reader.isConnected()) configureReader(sessionType);
  }
  
  /**
   * Connect string.
   *
   * @return the string
   */
  private synchronized String connect(){
    if(reader != null){
      showLog(TAG, "connect " + reader.getHostName());
      try{
        if(!reader.isConnected()){
          // Establish connection to the RFID Reader
          reader.connect();
          showLog("CONFIG", "1");
          return DEVICE_STATUS_CONNECTED;
        }
        else{
          return DEVICE_STATUS_CONNECTED;
        }
      }
      catch(InvalidUsageException e){
        e.printStackTrace();
      }
      catch(OperationFailureException e){
        e.printStackTrace();
        String des = e.getResults().toString();
        if(e.getResults() == RFIDResults.RFID_READER_REGION_NOT_CONFIGURED){
          return "RFID_READER_REGION_NOT_CONFIGURED";
        }
        else{
          return reader.isConnected() ? DEVICE_STATUS_CONNECTED : "Connection failed " + e.getVendorMessage() + " " + des;
        }
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
    return "";
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
  public void configureReader(AppCommonMethods.SessionType sessionType){
    this.sessionType = this.sessionType.getValue() == 0 && sessionType != null && sessionType.getValue() > 0 ? sessionType : this.sessionType;
    AppCommonMethods.SessionType type = this.sessionType.getValue() > 0 || sessionType == null || sessionType.getValue() == 0 ? this.sessionType : sessionType != null ? sessionType : this.sessionType;
    if(type.getValue() > 0){
      showLog("ZebraRFIDHandler", "configureReader");

     /* new Handler(Looper.getMainLooper()).post(new Runnable(){
        @Override
        public void run(){*/
      if(isReaderConnected()){
        showLog("Config Device", "true");
        setProgressMessage(context.getString(R.string.msg_config_reader), true);
        TriggerInfo triggerInfo = new TriggerInfo();
        triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
        triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
        try{
          // receive events from reader
          if(eventHandler == null) eventHandler = new EventHandler();
          reader.Events.addEventsListener(eventHandler);
          // HH event
          reader.Events.setHandheldEvent(true);
          // tag event with tag data
          reader.Events.setBatteryEvent(true);
          reader.Events.setTagReadEvent(true);
          reader.Events.setAttachTagDataWithReadEvent(false);
          reader.Events.setReaderDisconnectEvent(true);
          //reader.Events.setAntennaEvent(true);
          //reader.Events.setBatchModeEvent(true);
          reader.Events.setBufferFullEvent(true);
          reader.Events.setBufferFullWarningEvent(true);
          //reader.Events.setGPIEvent(true);
          //reader.Events.setInventoryStartEvent(true);
          //reader.Events.setInventoryStopEvent(true);
          //reader.Events.setOperationEndSummaryEvent(true);
          reader.Events.setPowerEvent(true);
          // set trigger mode as rfid so scanner beam will not come
          reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, true);
          reader.Config.setKeylayoutType(ENUM_NEW_KEYLAYOUT_TYPE.RFID, ENUM_NEW_KEYLAYOUT_TYPE.RFID);
          // set start and stop triggers
          reader.Config.setStartTrigger(triggerInfo.StartTrigger);
          reader.Config.setStopTrigger(triggerInfo.StopTrigger);
          
          if(type == AppCommonMethods.SessionType.INVENTORY || type == AppCommonMethods.SessionType.ADD_INVENTORY || type == AppCommonMethods.SessionType.BRAND_INVENTORY || type == AppCommonMethods.SessionType.FILTER_INVENTORY || type == AppCommonMethods.SessionType.STOCK_CORRECTION){
            reader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP);
          }
          else{
            reader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP);
            //reader.Config.setBeeperVolume(BEEPER_VOLUME.LOW_BEEP);
          }
          
          final String region = reader.Config.getRegulatoryConfig().getRegion();
          showLog("Region", region);
          showLog("TimeOUT", "" + reader.getTimeout());
          if(!region.toUpperCase().matches("^IND[A-Z]*$")) ConnectAndSetRegion("INDIA");
          
          reader.Actions.PreFilters.deleteAll();
          clearFilters();
          int maxPower = type == AppCommonMethods.SessionType.SCAN || type == AppCommonMethods.SessionType.VERIFY_ENCODING || type == AppCommonMethods.SessionType.ENCODING || type == AppCommonMethods.SessionType.ENCODING_THAN ? MIN_POWER_TO_SET * 2 : type == AppCommonMethods.SessionType.MOVEMENT || type == AppCommonMethods.SessionType.INWARD || type == AppCommonMethods.SessionType.OUTWARD ? MAX_POWER_TO_SET / 2 : sessionType == AppCommonMethods.SessionType.INWARD_TOTE ? (int) (MIN_POWER_TO_SET * inwToteMinPowerMultiplier) : type == AppCommonMethods.SessionType.OUTWARD_TOTE ? (int) (MIN_POWER_TO_SET * owtToteMinPowerMultiplier) : type == AppCommonMethods.SessionType.OFF_RANGE ? MAX_POWER_TO_SET - (MIN_POWER_TO_SET * 2) : type == AppCommonMethods.SessionType.DECODING ? (int) (MIN_POWER_TO_SET * decodePickMinPowerMultiplier) : type == AppCommonMethods.SessionType.SER_EXCEL ? MIN_POWER_TO_SET : MAX_POWER_TO_SET;
          int power = type == AppCommonMethods.SessionType.INWARD || type == AppCommonMethods.SessionType.OUTWARD ? (int) (MIN_POWER_TO_SET * inwDefPowerMultiplier) : type == AppCommonMethods.SessionType.INWARD_TOTE ? MIN_POWER_TO_SET : type == AppCommonMethods.SessionType.DECODING ? (int) (MIN_POWER_TO_SET * decodePickMinPowerMultiplier) : chkZero(SharedPrefManager.getInt(type.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), maxPower / 10), maxPower / 10) * 10;
          showLog("maxPower", "" + maxPower);
          showLog("power", "" + power);
          configAction(power, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
          readerPower.postValue(power / 10);
          SharedPrefManager.setReaderPower(power / 10);
          SharedPrefManager.setInt(type.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), power / 10);
          
          reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
          if(!reader.getTransport().equals("SERVICE_USB"))
            reader.Config.setBatchMode(BATCH_MODE.DISABLE);
          showLog("Batch_Mode", reader.Config.getBatchModeConfig().toString());
          setBatteryConfiguration();
          
          rfidInterface.RFIDInitializationStatus(true, "", reader);
          isDeviceConfigured.postValue(true);
          showLog("isDeviceConfigured", "" + true);
          setProgressMessage(false);
          //checkReaderCapabilities();
        }
        catch(InvalidUsageException e){
          e.printStackTrace();
          showLog("EXC1Config", e.getVendorMessage());
          setProgressMessage(false);
          rfidInterface.RFIDInitializationStatus(false, "", null);
          isDeviceConfigured.postValue(false);
        }
        catch(OperationFailureException e){
          e.printStackTrace();
          //Response timeout
          showLog("EXCConfig", e.getVendorMessage());
          if(e.getVendorMessage().equalsIgnoreCase("Charging in Progress-Command Not allowed")){
            AppCommonMethods.showShortToast(context, e.getVendorMessage());
          }
          if(e.getVendorMessage().equalsIgnoreCase("Response timeout")){
            disconnect();
            AppCommonMethods.showShortToast(context, String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
            rfidInterface.RFIDInitializationStatus(false, "Reader Disconnected", null);
          }
          if(e.getVendorMessage().contains("Region Not Set")){
            ConnectAndSetRegion("India");
          }
          //if(!e.getVendorMessage().equalsIgnoreCase("Operation In Progress-Command Not Allowed"))
          
          setProgressMessage(false);
          rfidInterface.RFIDInitializationStatus(false, "", null);
          isDeviceConfigured.postValue(false);
        }
        catch(Exception e){
          showLog("EXCConfig", e.getMessage());
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
      //      /  }
      // });
    }
    else setProgressMessage(false);
  }
  
  private void checkReaderCapabilities(){
    showLog("Reader ID", reader.ReaderCapabilities.ReaderID.getID());
    showLog("ModelName", reader.ReaderCapabilities.getModelName());
    showLog("Communication Standard", reader.ReaderCapabilities.getCommunicationStandard().toString());
    showLog("Country Code", "" + reader.ReaderCapabilities.getCountryCode());
    showLog("RSSI Filter", "" + reader.ReaderCapabilities.isRSSIFilterSupported());
    showLog("Tag Event Reporting", "" + reader.ReaderCapabilities.isTagEventReportingSupported());
    showLog("Tag Locating Reporting", "" + reader.ReaderCapabilities.isTagLocationingSupported());
    showLog("NXP Command Support", "" + reader.ReaderCapabilities.isNXPCommandSupported());
    showLog("BlockEraseSupport", "" + reader.ReaderCapabilities.isBlockEraseSupported());
    showLog("BlockWriteSupport", "" + reader.ReaderCapabilities.isBlockWriteSupported());
    showLog("BlockPermalockSupport", "" + reader.ReaderCapabilities.isBlockPermalockSupported());
    showLog("RecommisionSupport", "" + reader.ReaderCapabilities.isRecommisionSupported());
    showLog("WriteWMISupport", "" + reader.ReaderCapabilities.isWriteUMISupported());
    showLog("RadioPowerControlSupport: ", "" + reader.ReaderCapabilities.isRadioPowerControlSupported());
    showLog("HoppingEnabled", "" + reader.ReaderCapabilities.isHoppingEnabled());
    showLog("StateAwareSingulationCapable", "" + reader.ReaderCapabilities.isTagInventoryStateAwareSingulationSupported());
    showLog("UTCClockCapable", "" + reader.ReaderCapabilities.isUTCClockSupported());
    showLog("NumOperationsInAccessSequence", "" + reader.ReaderCapabilities.getMaxNumOperationsInAccessSequence());
    showLog("NumPreFilters", "" + reader.ReaderCapabilities.getMaxNumPreFilters());
    showLog("NumAntennaSupported", "" + reader.ReaderCapabilities.getNumAntennaSupported());
  }
  
  /**
   * Update trigger mode.
   *
   * @param isBarcodeMode the is barcode mode
   */
  public void updateTriggerMode(boolean isBarcodeMode){
    if(reader != null && reader.isConnected() && sessionType.getValue() > 0){
      showLog("updateTriggerMode", "" + isBarcodeMode);
      try{
        reader.Config.setTriggerMode(isBarcodeMode ? ENUM_TRIGGER_MODE.BARCODE_MODE : ENUM_TRIGGER_MODE.RFID_MODE, true);
        //reader.Config.setKeylayoutType(isBarcodeMode ? ENUM_NEW_KEYLAYOUT_TYPE.TERMINAL_SCAN : ENUM_NEW_KEYLAYOUT_TYPE.RFID, isBarcodeMode ? ENUM_NEW_KEYLAYOUT_TYPE.TERMINAL_SCAN : ENUM_NEW_KEYLAYOUT_TYPE.RFID);
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Set beeper and stop inventory.
   */
  public void setBeeperAndStopInventory(){
    
    this.isCommandForSearch = false;
    this.isCommandForEPCSearch = false;
    this.isCommandForTIDSearch = false;
    this.isLockSearchEPC = false;
    this.SEARCH_EPC = "";
    this.SEARCH_BARCODE = "";
    this.SEARCH_LOCKED_EPC = "";
    // check reader connection
    if(!isReaderConnected()) return;
    try{
      reader.Config.setBeeperVolume(BEEPER_VOLUME.LOW_BEEP);
      
      reader.Actions.Inventory.stop();
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      showLog("STOPEXC1", e.getVendorMessage());
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      showLog("STOPEXC2", e.getVendorMessage());
      showLog("STOPEXC2", e.getMessage());
      showLog("STOPEXC2", e.getLocalizedMessage());
    }
    catch(Exception e){
      e.printStackTrace();
      showLog("STOPEXC3", e.getMessage());
    }
    
  }
  
  /**
   * Disconnect.
   */
  public synchronized void disconnect(){
    showLog(TAG, "disconnect " + reader);
    try{
      if(reader != null){
        stopInventory();
        if(reader.Events != null && eventHandler != null)
          reader.Events.removeEventsListener(eventHandler);
        reader.disconnect();
      }
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      showLog("MSG", e.getVendorMessage());
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      showLog("MSG1", e.getVendorMessage());
    }
    catch(Exception e){
      e.printStackTrace();
      showLog("MSG2", e.getMessage());
    }
  }
  
  /**
   * Dispose.
   */
  private synchronized void dispose(){
    try{
      if(readers != null){
        reader = null;
        readers.Dispose();
        readers = null;
        
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  /*@Override
  public synchronized boolean performEPCBasedSearch(String sgtin){
    final boolean isPerformEPCBasedSearch = super.performEPCBasedSearch(sgtin);
    if(isPerformEPCBasedSearch) performSearch();
    return isPerformEPCBasedSearch;
  }

  @Override
  public synchronized boolean performBarcodeBasedSearch(String barcode){
    return performBarcodeBasedSearch(barcode, false);
  }

  @Override
  public synchronized boolean performBarcodeBasedSearch(String barcode, final boolean isLockSearchEPC){
    final boolean isPerformBarcodeBasedSearch = super.performBarcodeBasedSearch(barcode, isLockSearchEPC);
    if(isPerformBarcodeBasedSearch) performSearch();
    return isPerformBarcodeBasedSearch;
  }*/
  
  /**
   * Perform search.
   */
  @Override
  protected synchronized void performSearch(){
    
    showLog("CMD", "" + isCommandForSearch);
    // check reader connection
    if(!isReaderConnected()){// || reader.Config==null || reader.Config.Antennas==null){
      showLog("Reader", "NOT CONNECTED");
      return;
    }
    /*new Thread(){
      @Override
      public void run(){*/
    try{
      sessionAction = AppCommonMethods.SessionAction.SEARCH;
      setSingulationControlPrefilterReset();
      reader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP);
      reader.Actions.PreFilters.deleteAll();
      clearFilters();
      configAction(MAX_POWER_TO_SET, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_B);
      //reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
      String finalsgtin = SEARCH_EPC;
      showLog("finalsgtin", chkNull(finalsgtin, "null"));
      String header = chkNull(finalsgtin, "").length() > 2 ? finalsgtin.substring(0, 2) : "";
      final boolean isNonStdEnc = finalsgtin.length() >= 32 && header.matches("(?i)(BC|0C|00)");
      
      if(isCommandForTIDSearch) addTidBasedFilters(SEARCH_TID); //TODO Same in Chaniway
      else if(isCommandForEPCSearch) addEpcBasedFilters(finalsgtin, isNonStdEnc);
      else addFilters(isNonStdEnc && chkNull(finalsgtin, "").length() > 9 ? finalsgtin.substring(9) : finalsgtin, isNonStdEnc);
      
      if(isCommandForTIDSearch && readTid){
        TagAccess tagAccess = new TagAccess();
        TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
        //Set the param values
        readAccessParams.setCount(0);
        readAccessParams.setOffset(0);
        readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
        reader.Actions.TagAccess.readEvent(readAccessParams, null, null);
      }
      else reader.Actions.Inventory.perform();
      isSearchOn.postValue(true);
      loopFlag = true;
      startTimer();
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      showLog("EXC1SEARCH", e.getVendorMessage());
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast)
        AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      //Response timeout
      showLog("EXCSEARCH", e.getVendorMessage());
      if(e.getVendorMessage().equalsIgnoreCase("Charging in Progress-Command Not allowed")){
        AppCommonMethods.showShortToast(context, e.getVendorMessage());
      }
      if(e.getVendorMessage().equalsIgnoreCase("Response timeout")){
        disconnect();
        AppCommonMethods.showShortToast(context, String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        rfidInterface.RFIDInitializationStatus(false, "Reader Disconnected", null);
      }
      if(e.getVendorMessage().contains("Region Not Set")){
        AppCommonMethods.showShortToast(context, e.getVendorMessage());
        ConnectAndSetRegion("India");
      }
      if(!e.getVendorMessage().equalsIgnoreCase("Operation In Progress-Command Not Allowed")){
        stopInventory();
        if(AppCommonMethods.isShowReaderCommandFailToast && !e.getVendorMessage().matches("(?i)(Charging in Progress-Command Not allowed|Region Not Set|Response timeout)"))
          AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
      }
    }
    catch(Exception e){
      e.printStackTrace();
      showLog("EXC1SEARCH", e.getMessage());
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast)
        AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
    }
    //}
    //}.start();
  }
  
  /**
   * add Filters.
   *
   * @param tag  the tag
   * @param isBc the is bc
   */
  @Override
  protected void addFilters(String tag, boolean isBc){
    // Add state aware pre-filter
    PreFilters filters = new PreFilters();
    PreFilters.PreFilter filter = filters.new PreFilter();
    filter.setAntennaID((short) 1);// Set this filter for Antenna ID 1
    filter.setTagPattern(tag);// Tags which starts with passed pattern
    filter.setTagPatternBitCount(tag.length() * 4); // set tag pattern length
    if(isBc){
      //68 =>substring(9) 80=>substring(12)
      filter.setBitOffset(68); // skip PC bits (always it should be in bit length)
    }
    else{
      filter.setBitOffset(32); // skip PC bits (always it should be in bit length)
    }
    
    filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
    filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE); // use state aware singulation
    filter.StateAwareAction.setTarget(TARGET.TARGET_INVENTORIED_STATE_S0); // inventoried flag of session S1 of matching tags to B
    filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A);
    // not to select tags that match the criteria
    try{
      if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.PreFilters != null)
        reader.Actions.PreFilters.add(filter);
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
    }
    catch(OperationFailureException e){
      e.printStackTrace();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  /**
   * Add epc basedfilters.
   *
   * @param tag  the tag
   * @param isBc the is bc
   */
  @Override
  protected void addEpcBasedFilters(String tag, boolean isBc){
    // Add state aware pre-filter
    PreFilters filters = new PreFilters();
    PreFilters.PreFilter filter = filters.new PreFilter();
    filter.setAntennaID((short) 1);// Set this filter for Antenna ID 1
    filter.setTagPattern(tag);// Tags which starts with passed pattern
    if((sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST) && chkNull(tag, "").length() > 1)
      filter.setTagPattern(tag.substring(1));
    if(isBc){
      filter.setTagPatternBitCount(tag.length() * 4);
      filter.setBitOffset(32); // skip PC bits (always it should be in bit length)
      if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST){
        filter.setTagPatternBitCount(tag.substring(1).length() * 4);
        filter.setBitOffset(32 + 4);
      }
    }
    else{
      filter.setTagPatternBitCount(tag.length() * 4);
      filter.setBitOffset(32); // skip PC bits (always it should be in bit length)
      if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST){
        filter.setTagPatternBitCount(tag.substring(1).length() * 4);
        filter.setBitOffset(32 + 4);
      }
    }
    
    filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
    filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE); // use state aware singulation
    filter.StateAwareAction.setTarget(TARGET.TARGET_INVENTORIED_STATE_S0); // inventoried flag of session S1 of matching tags to B
    filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A);
    // not to select tags that match the criteria
    try{
      if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.PreFilters != null)
        reader.Actions.PreFilters.add(filter);
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
    }
    catch(OperationFailureException e){
      e.printStackTrace();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  @Override
  protected void addTidBasedFilters(String tid){
    // Add state aware pre-filter
    PreFilters filters = new PreFilters();
    PreFilters.PreFilter filter = filters.new PreFilter();
    filter.setAntennaID((short) 1);// Set this filter for Antenna ID 1
    filter.setTagPattern(tid);// Tags which starts with passed pattern
    filter.setTagPatternBitCount(tid.length() * 4);
    filter.setBitOffset(0); // skip PC bits (always it should be in bit length)
    
    filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
    filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE); // use state aware singulation
    filter.StateAwareAction.setTarget(TARGET.TARGET_INVENTORIED_STATE_S0); // inventoried flag of session S1 of matching tags to B
    filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A);
    //filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A);
    // not to select tags that match the criteria
    try{
      if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.PreFilters != null)
        reader.Actions.PreFilters.add(filter);
      showLog("TID_FILTER_SET", tid);
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
    }
    catch(OperationFailureException e){
      e.printStackTrace();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  /**
   * Set singulation for filter.
   *
   * @param nonmatching the nonmatching
   * @param restore     the restore
   */
  
  private void setSingulationForFilter(boolean nonmatching, boolean restore){
    try{
      Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
      if(nonmatching){
        s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
      }
      else{
        s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_B);
      }
      s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
      if(reader != null && reader.isConnected() && reader.Config != null && reader.Config.Antennas != null)
        reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
    }
    catch(OperationFailureException e){
      e.printStackTrace();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  @Override
  public boolean performPick(final String findBarcode, final boolean isDecodeOnPick, final Integer pickPower, final boolean isPostPicked, final List<String> listEpcs){
    final boolean isPerformPick = super.performPick(findBarcode, isDecodeOnPick, pickPower, isPostPicked, listEpcs);
    if(isPerformPick){
      SEARCH_BARCODE = findBarcode;
      pickTags.clear();
      pickUHFTags.clear();
      sessionAction = AppCommonMethods.SessionAction.INVENTORY;
      isActionPick = false;
      isPickOn.postValue(false);
      loopFlag = false;
      //isRePick = isSinglePick && sessionType == AppCommonMethods.SessionType.ENCODING;
      showLog("isRePick", "" + isRePick);
      encPickedData = null;
      clearFilters();
      startPick(findBarcode, isDecodeOnPick, isPostPicked);
    }
    return isPerformPick;
  }
  
  /**
   * Start pick.
   *
   * @param findBarcode the find barcode
   */
  public void startPick(final String findBarcode, final boolean isDecodeOnPick, final boolean isPostPicked){
    AppCommonMethods.logInFile(context, sessionType.name(), "startPick (" + findBarcode + ")");
    showLog(sessionType.name(), "startPick (" + findBarcode + ")");
    try{
      if(reader == null || !reader.isConnected() || reader.Actions == null || reader.Actions.TagAccess == null)
        return;
      if(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING){
        reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
        if(!reader.getTransport().equals("SERVICE_USB"))
          reader.Config.setBatchMode(BATCH_MODE.DISABLE);
        showLog("Batch_Mode", reader.Config.getBatchModeConfig().toString());
      }
      setSingulationControlPrefilterReset();
      reader.Actions.PreFilters.deleteAll();
      clearFilters();
      int maxPower = chkZero(pickPower * 10, sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN ? (MIN_POWER_TO_SET + 30) : sessionType == AppCommonMethods.SessionType.MOVEMENT ? MAX_POWER_TO_SET / 2 : MIN_POWER_TO_SET * 2); //+ MIN_POWER_TO_SET / 2;
      int power = /*sessionType == AppCommonMethods.SessionType.ENCODING||*/sessionType == AppCommonMethods.SessionType.MOVEMENT ? chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), maxPower / 10), maxPower / 10) * 10 : maxPower;
      if(power > maxPower){
        power = maxPower;
      }
      configAction(power, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
      
      TagAccess tagAccess = new TagAccess();
      TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
      //Set the param values
      readAccessParams.setCount(0);
      readAccessParams.setOffset(0);
      readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
      isActionPick = true;
      sessionAction = AppCommonMethods.SessionAction.PICK;
      isPickOn.postValue(true);
      loopFlag = true;
      pickTags.clear();
      pickUHFTags.clear();
      
      AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_START" + (isNonEmpty(findBarcode) ? "(" + findBarcode + ")" : ""));
      showLog(sessionType.name(), "startPick (" + findBarcode + ")");
      //Read command with readAccessParams and accessFilter as null to read all the tags
      reader.Actions.TagAccess.readEvent(readAccessParams, null, null);
      //MultiLocateParams mlps = new MultiLocateParams();
      //reader.Actions.TagLocationing.PerformMultiLocate(,,null);
      //reader.Actions.TagLocationing.Perform("00361F5FF4145D174876E809",null,null);
      if(isSinglePick){
        showLog("SINGLEPICK", "" + isSinglePick);
        //setProgressMessage(context.getString(R.string.msg_pick), true);
        pickTimer = new Timer();
        pickTimer.schedule(new TimerTask(){
          @Override
          public void run(){
            showLog("INTIMER", "FINISH");
            showLog("SINGLEPICK_onFinish", "onFinish");
            showLog("SINGLEPICK_onFinish_Size", "_" + pickTags.size());
            stopInventory();
            showLog(sessionType.name(), "stopPick (" + findBarcode + ")");
            if(isNullOrEmpty(pickTags)){
              showLog("isRePick", "" + isRePick);
              if(isRePick){
                isRePick = false;
                AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_STOP (Tag Not Found & Re-Pick)");
                startPick(findBarcode, isDecodeOnPick, isPostPicked);
              }
              else{
                AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_STOP (Tag Not Found)");
                isRePick = false;
                context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
              }
              setProgressMessage(false);
            }
            else if(pickTags.size() > 1){
              isRePick = false;
              setProgressMessage(false);
              AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_RESULT (Multiple Tags Found)");
              context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
            }
            else if(pickTags.size() == 1){
              isRePick = false;
              final TagData tagData = new ArrayList<TagData>(pickUHFTags).get(0);
              encPickedData = sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN ? tagData : null;
              final String epcdt = chkNull(tagData.getTagID(), "");
              final String tid = tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? /*AppCommonMethods.isUse24LengthTIDForUpload && chkNull(tagData.getMemoryBankData(),"").length()>24? tagData.getMemoryBankData().substring(0,24) :*/ tagData.getMemoryBankData() : "";
              updateFoundWrittenTag(epcdt, tid);
              if((sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD) && AppDatabase.getTripInventoryDao(context).isEpcPresent(SharedPrefManager.getTripNo(), SharedPrefManager.getHuNo(), epcdt)){
                context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
              }
              else if(sessionType == AppCommonMethods.SessionType.OFF_RANGE && uploadInventoryDao.isEPCPresent(sessionType.getValue(), epcdt)){
                AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_STOP (Tag Already Picked)");
                final UploadInventory ui = uploadInventoryDao.getBysessionTypeAndEpc(sessionType.getValue(), epcdt);
                if(ui != null && isNonEmpty(ui.remark) && isNonEmpty(ui.fifoDate))
                  context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag_in_carton), getTypeCharCode(), ui.remark));
                else
                  context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
                setProgressMessage(false);
              }
              else if((sessionType != AppCommonMethods.SessionType.SCAN && sessionType != AppCommonMethods.SessionType.VERIFY_ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING_THAN && sessionType != AppCommonMethods.SessionType.DECODING) && (inventoryDao.isEPCPresent(sessionId, epcdt) || (isNonEmpty(pickedEpcs) && pickedEpcs.contains(epcdt)))){
                AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_STOP (Tag Already Picked)");
                String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING);
                if(isNonEmpty(barcode) && !barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && sessionType != AppCommonMethods.SessionType.SCAN && sessionType != AppCommonMethods.SessionType.VERIFY_ENCODING)
                  context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag_for_barcode), getTypeCharCode(), barcode));
                else
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
                final String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING);
                final String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context, findBarcode);
                showLog("pick_barcode", barcode);
                showLog("pick_compare_barcode", compbarcode);
                final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                
                if(!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase())) && (isNullOrEmpty(eans) || eans.contains(barcode)) && (sessionType != AppCommonMethods.SessionType.SEARCH_FIFO || fifoDao.isEPCPresent(epcdt, ean, fifoDate/*,zone,zoneId*/))){
                  isMatchingBarcode = true;
                  matchingBarcode = AppCommonMethods.getLeftZeroReplacedString(context, barcode);
                }
                else if(sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING_THAN && sessionType != AppCommonMethods.SessionType.DECODING){
                  if(isNullOrEmpty(barcode) || barcode.equalsIgnoreCase(AppConstants.UNKNOWN) || barcode.equalsIgnoreCase(AppConstants.NON_ENCODED))
                    context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                  else if(sessionType == AppCommonMethods.SessionType.OFF_RANGE && isNullOrEmpty(findBarcode) && isNonEmpty(eans) && isNonEmpty(barcode) && !eans.contains(barcode))
                    context.showCustomErrDialog(String.format(String.format(context.getString(R.string.err_pick_not_present_tag)/*,getTypeCharCode()*/, barcode)));
                  else if(isNonEmpty(barcode))
                    context.showCustomAlertDialog(String.format(context.getString(R.string.err_pick_wrong_tag_header), getTypeCharCode()), String.format(context.getString(R.string.err_pick_wrong_tag)/*,getTypeCharCode()*/, barcode), context.getString(R.string.btn_ok), null);
                }
                setProgressMessage(false);
                if((isMatchingBarcode /*&& isNonEmpty(matchingBarcode)*/) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING){
                  AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_STOP (" + matchingBarcode + "_" + tagData.getTagID() + ")");
                  showLog("PICK", "1:SESSION : " + sessionType.name());
                  readTid = true;
                  readRssi = true;
                  readEAN = true;
                  readPC = true;//sessionType == AppCommonMethods.SessionType.ENCODING;// || sessionType == AppCommonMethods.SessionType.DECODING;
                  if(isPostPicked || sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING){
                    final Inventory pickedTagData = getDataFromTagInfo(tagData);
                    if((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && isMatchingBarcode && isNonEmpty(matchingBarcode))
                      pickedTagData.ean = AppCommonMethods.getLeftZeroReplacedString(context, matchingBarcode);
                    if(isNullOrEmpty(matchingBarcode) && sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING_THAN && (sessionType != AppCommonMethods.SessionType.DECODING || !SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_NON_ENCODED_TAG_PICK_FOR_DECODE, AppCommonMethods.isAllowNonEncodedTagPickForDecode)))
                      context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                    else if((sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING) && (pickedTagData == null || isNullOrEmpty(pickedTagData.epc) || isNullOrEmpty(pickedTagData.tid)))
                      context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
                      //else pickData.postValue(pickedTagData);
                    else{
                      if(context != null && !context.isFinishing() && context instanceof MainActivity){
                        CommonFragment fragment = ((MainActivity) context).getTopFragment();
                        if(fragment != null && fragment instanceof EncodingStartFragment)
                          ((EncodingStartFragment) fragment).onPickDataChanged(pickedTagData);
                        else pickData.postValue(pickedTagData);
                      }
                      else pickData.postValue(pickedTagData);
                    }
                  }
                  else{
                    sessionAction = AppCommonMethods.SessionAction.INVENTORY;
                    saveInventoryData(tagData);
                    if(isDecodeOnPick){
                      final Inventory pickedTag = getDataFromTagInfo(tagData);
                      pickData.postValue(pickedTag);
                    }
                    //storeInventoryData(tagData);
                  }
                }
              }
            }
          }
          //}, sessionType == AppCommonMethods.SessionType.ENCODING ||sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING ? pickCountDownTime / 4 : pickCountDownTime);
          //Since More Pick Time is required for Than Encoding
        }, sessionType == AppCommonMethods.SessionType.ENCODING || (sessionType.equals(AppCommonMethods.SessionType.ENCODING_THAN) && !SharedPrefManager.getBoolean(ParamConstants.IS_SET_EXTRA_PICK_TIME_FOR_THAN_ENC, AppCommonMethods.isSetExtraPickTimeForThanEncoding)) || sessionType == AppCommonMethods.SessionType.DECODING ? pickCountDownTime / 4 : pickCountDownTime);
        //        pickCountDownTimer = new CountDownTimer(sessionType == AppCommonMethods.SessionType.ENCODING ? pickCountDownTime/4 : pickCountDownTime, 200){
        //          @Override
        //          public void onTick(long l){
        //            //showLog("pick " + "onTick", "" + l);
        //          }
        //
        //          @Override
        //          public void onFinish(){
        //            showLog("ENCODING_PICK_STOP1","Timer STOP");
        //            showLog("INTIMER", "FINISH");
        //            showLog("onFinish", "onFinish");
        //            stopInventory();
        //            showLog(sessionType.name(),"stopPick (" + findBarcode + ")");
        //            if(isNullOrEmpty(pickTags)){
        //              showLog("isRePick", "" + isRePick);
        //              if(isRePick){
        //                isRePick = false;
        //                AppCommonMethods.logInFile(context, sessionType.name() , "_PICK_STOP (Tag Not Found & Re-Pick)");
        //                startPick(findBarcode, isDecodeOnPick);
        //              }
        //              else{
        //                AppCommonMethods.logInFile(context, sessionType.name() , "_PICK_STOP (Tag Not Found)");
        //                isRePick = false;
        //                context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
        //              }
        //              setProgressMessage(false);
        //            }
        //            else if(pickTags.size() > 1){
        //              isRePick = false;
        //              setProgressMessage(false);
        //              AppCommonMethods.logInFile(context, sessionType.name() , "_PICK_RESULT (Multiple Tags Found)");
        //              context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
        //            }
        //            else if(pickTags.size() == 1){
        //              isRePick = false;
        //              final TagData tagData = new ArrayList<TagData>(pickUHFTags).get(0);
        //              encPickedData = sessionType == AppCommonMethods.SessionType.ENCODING ? tagData : null;
        //              final String epcdt = chkNull(tagData.getTagID(), "");
        //              if((sessionType != AppCommonMethods.SessionType.SCAN && sessionType != AppCommonMethods.SessionType.VERIFY_ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.DECODING) && inventoryDao.isEPCPresent(ean, sessionId, epcdt)){
        //                AppCommonMethods.logInFile(context, sessionType.name() , "_PICK_STOP (Tag Already Picked)");
        //                context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
        //                setProgressMessage(false);
        //              }
        //              else{
        //                boolean isMatchingBarcode = false;
        //                //final String epc = ((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING) && chkNull(epcdt, "").length() > 2) ? chkNull(epcdt, "").startsWith("0C") ? epcdt.replaceFirst("0C", "BC") : chkNull(epcdt, "").startsWith("00") ? (epcdt.length() >= 32) ? epcdt.replaceFirst("00", "BC") : (epcdt.length() >= 24) ? epcdt.replaceFirst("00", "30") : epcdt : epcdt : epcdt;
        //                //TODO
        //                //final String epc = ((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && chkNull(epcdt, "").length() > 2)?chkNull(epcdt, "").startsWith("0C")?epcdt.replaceFirst("0C", "BC"):chkNull(epcdt, "").startsWith("05")?epcdt.replaceFirst("05", "35"):chkNull(epcdt, "").startsWith("00")? (epcdt.length() >= 32) ? epcdt.replaceFirst("00", "BC") : (epcdt.length() >= 24) ? epcdt.replaceFirst("00", "35") : epcdt : epcdt :epcdt;
        //                //final String header = epc.length() > 2 ? epc.substring(0, 2) : "";
        //                String matchingBarcode = "";
        //                //showLog("pick_finish_epc_header", header);
        //                //check by using getBarcode method instead of switch case
        //                final String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING);
        //                final String compbarcode = AppCommonMethods.getLeftZeroReplacedString(findBarcode);
        //                showLog("pick_barcode", barcode);
        //                showLog("pick_compare_barcode", compbarcode);
        //                final FIFODao fifoDao = AppDatabase.getFIFODao(context);
        //
        //                if(!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase())) && (sessionType != AppCommonMethods.SessionType.SEARCH_FIFO || fifoDao.isEPCPresent(epcdt, ean, fifoDate/*,zone,zoneId*/))){
        //                  isMatchingBarcode = true;
        //                  matchingBarcode = AppCommonMethods.getLeftZeroReplacedString(barcode);
        //                }
        //                else if(sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.DECODING){
        //                  if(isNullOrEmpty(barcode) || barcode.equalsIgnoreCase(AppConstants.UNKNOWN) || barcode.equalsIgnoreCase(AppConstants.NON_ENCODED))
        //                    context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
        //                  else if(isNonEmpty(barcode))
        //                    context.showCustomAlertDialog(String.format(context.getString(R.string.err_pick_wrong_tag_header), getTypeCharCode()), String.format(context.getString(R.string.err_pick_wrong_tag)/*,getTypeCharCode()*/, barcode), context.getString(R.string.btn_ok), null);
        //                }
        //                setProgressMessage(false);
        //                if((isMatchingBarcode && isNonEmpty(matchingBarcode)) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING){
        //                  AppCommonMethods.logInFile(context, sessionType.name() , "_PICK_STOP (" + matchingBarcode + "_" + tagData.getTagID() + ")");
        //                  showLog("PICK", "1:SESSION : " + sessionType.name());
        //                  readTid = true;
        //                  readRssi = true;
        //                  readEAN = true;
        //                  readPC = true;//sessionType == AppCommonMethods.SessionType.ENCODING;// || sessionType == AppCommonMethods.SessionType.DECODING;
        //                  if(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING){
        //                    final Inventory pickedTagData = getDataFromTagInfo(tagData);
        //                    if((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && isMatchingBarcode && isNonEmpty(matchingBarcode))
        //                      pickedTagData.ean = AppCommonMethods.getLeftZeroReplacedString(matchingBarcode);
        //                    if(isNullOrEmpty(matchingBarcode) && sessionType != AppCommonMethods.SessionType.ENCODING)
        //                      context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
        //                    else if((sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING) && (pickedTagData == null || isNullOrEmpty(pickedTagData.epc) || isNullOrEmpty(pickedTagData.tid)))
        //                      context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
        //                    else pickData.postValue(pickedTagData);
        //                  }
        //                  else{
        //                    sessionAction = AppCommonMethods.SessionAction.INVENTORY;
        //                    storeInventoryData(tagData);
        //                  }
        //                }
        //              }
        //            }
        //          }
        //        };
        //        pickCountDownTimer.start();
        showLog("INTIMER", "OUT");
      }
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      showLog("EXCPICK1", e.getVendorMessage());
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast && context != null && !context.isFinishing())
        context.showShortToast(R.string.err_reader_fail);
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      //Response timeout
      showLog("EXCPICK2", e.getVendorMessage());
      if(e.getVendorMessage().equalsIgnoreCase("Charging in Progress-Command Not allowed")){
        AppCommonMethods.showShortToast(context, e.getVendorMessage());
      }
      if(e.getVendorMessage().equalsIgnoreCase("Response timeout")){
        disconnect();
        AppCommonMethods.showShortToast(context, String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        rfidInterface.RFIDInitializationStatus(false, "Reader Disconnected", null);
      }
      if(e.getVendorMessage().contains("Region Not Set")){
        AppCommonMethods.showShortToast(context, e.getVendorMessage());
        ConnectAndSetRegion("India");
      }
      if(!e.getVendorMessage().equalsIgnoreCase("Operation In Progress-Command Not Allowed")){
        stopInventory();
        if(AppCommonMethods.isShowReaderCommandFailToast && !e.getVendorMessage().matches("(?i)(Charging in Progress-Command Not allowed|Region Not Set|Response timeout)"))
          AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
      }
    }
    catch(Exception e){
      e.printStackTrace();
      showLog("EXCPICK3", e.getMessage());
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast)
        AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
    }
  }
  
  @Override
  public boolean performTidPick(final String findBarcode, final List<String> tids){
    final boolean isPerformTidPick = super.performTidPick(findBarcode, tids);
    if(isPerformTidPick){
      pickTags.clear();
      pickUHFTags.clear();
      sessionAction = AppCommonMethods.SessionAction.INVENTORY;
      isActionPick = false;
      isPickOn.postValue(false);
      loopFlag = false;
      isRePick = isSinglePick && (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN);
      showLog("isRePick", "" + isRePick);
      encPickedData = null;
      SEARCH_BARCODE = findBarcode;
      //SCANNED_TID = tid;
      SCANNED_TIDS.clear();
      SCANNED_TIDS.addAll(tids);
      isActionTidPick = SCANNED_TIDS.size() > 0;//chkNull(tid,"").length()>0;
      startTidPick(findBarcode, tids);
    }
    return isPerformTidPick;
  }
  
  /**
   * Start pick.
   *
   * @param findBarcode the find barcode
   */
  @Override
  protected void startTidPick(final String findBarcode, final List<String> tids){
    try{
      if(reader == null || !reader.isConnected() || reader.Actions == null || reader.Actions.TagAccess == null)
        return;
      if(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING){
        reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
        if(!reader.getTransport().equals("SERVICE_USB"))
          reader.Config.setBatchMode(BATCH_MODE.DISABLE);
        showLog("Batch_Mode", reader.Config.getBatchModeConfig().toString());
      }
      reader.Actions.PreFilters.deleteAll();
      clearFilters();
      isActionTidPick = isNonEmpty(tids);//chkNull(tid,"").length()>0;
      SCANNED_TIDS.clear();
      SCANNED_TIDS.addAll(tids);
      int maxPower = (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN) ? MIN_POWER_TO_SET * 2 : sessionType == AppCommonMethods.SessionType.MOVEMENT ? MAX_POWER_TO_SET / 2 : MIN_POWER_TO_SET * 2; //+ MIN_POWER_TO_SET / 2;
      int power = isActionTidPick ? MAX_POWER_TO_SET :/*sessionType == AppCommonMethods.SessionType.ENCODING||*/sessionType == AppCommonMethods.SessionType.MOVEMENT ? chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), maxPower / 10), maxPower / 10) * 10 : maxPower;
      configAction(power, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
      
      //if(isActionTidPick && tids.size()==1) addTidBasedfilters(tids.get(0));
      
      TagAccess tagAccess = new TagAccess();
      TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
      //Set the param values
      readAccessParams.setCount(0);
      readAccessParams.setOffset(0);
      readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
      
      isActionPick = true;
      sessionAction = AppCommonMethods.SessionAction.PICK;
      isPickOn.postValue(true);
      loopFlag = true;
      pickTags.clear();
      pickUHFTags.clear();
      
      AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_START" + (isNonEmpty(findBarcode) ? "(" + findBarcode + ")" : ""));
      //Read command with readAccessParams and accessFilter as null to read all the tags
      reader.Actions.TagAccess.readEvent(readAccessParams, null, null);
      showLog("SINGLEPICK", "" + isSinglePick);
      if(tids.size() >= 1 && isSinglePick){
        //setProgressMessage(context.getString(R.string.msg_pick), true);
        pickCountDownTimer = new CountDownTimer(tids.size() * 1000L, pickCountDownTime){
          @Override
          public void onTick(long l){
            showLog("onTick", "" + l);
          }
          
          @Override
          public void onFinish(){
            showLog("onFinish", "onFinish");
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
              readPC = true;//sessionType == AppCommonMethods.SessionType.ENCODING;
              List<Inventory> pickedList = new ArrayList<>(0);
              for(TagData tagData : pickUHFTags){
                final String epc = chkNull(tagData.getTagID(), "");
                final String tid = tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? tagData.getMemoryBankData() : "";
                showLog("picked_tag", epc + "_" + tid);
                Inventory pickedTag = null;
                try{
                  pickedTag = getDataFromTagInfo(tagData);
                }
                catch(Exception e){
                  e.printStackTrace();
                }
                if(pickedTag != null && isNonEmpty(epc) && isNonEmpty(tid) && tids.contains(tid.length() > 24 ? tid.substring(0, 24) : tid)){
                  pickedList.add(pickedTag);
                  //pickData.postValue(pickedTag);
                  //storeInventoryData(tagData);
                }
              }
              showLog("pickedList", "" + pickedList.size());
              if(pickedList.size() >= 1) pickedListData.postValue(pickedList);
                //else if(pickedList.size() == 1) pickData.postValue(pickedList.get(0));
              else{
                //TODO show Error
              }
            }
          }
        };
        pickCountDownTimer.start();
      }
      
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      showLog("EXCPICK1", e.getVendorMessage());
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast && context != null && !context.isFinishing())
        context.showShortToast(R.string.err_reader_fail);
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      //Response timeout
      showLog("EXCPICK2", e.getVendorMessage());
      if(e.getVendorMessage().equalsIgnoreCase("Charging in Progress-Command Not allowed")){
        AppCommonMethods.showShortToast(context, e.getVendorMessage());
      }
      if(e.getVendorMessage().equalsIgnoreCase("Response timeout")){
        disconnect();
        AppCommonMethods.showShortToast(context, String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        rfidInterface.RFIDInitializationStatus(false, "Reader Disconnected", null);
      }
      if(e.getVendorMessage().contains("Region Not Set")){
        AppCommonMethods.showShortToast(context, e.getVendorMessage());
        ConnectAndSetRegion("India");
      }
      if(!e.getVendorMessage().equalsIgnoreCase("Operation In Progress-Command Not Allowed")){
        stopInventory();
        if(AppCommonMethods.isShowReaderCommandFailToast && !e.getVendorMessage().matches("(?i)(Charging in Progress-Command Not allowed|Region Not Set|Response timeout)"))
          AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
      }
    }
    catch(Exception e){
      e.printStackTrace();
      showLog("EXCPICK3", e.getMessage());
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast)
        AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
    }
  }
  
  /**
   * Perform locate inventory.
   *
   * @param locateTag the locate tag
   */
  public synchronized void performLocateInventory(String locateTag){
    // check reader connection
    this.isCommandForSearch = false;
    this.isCommandForEPCSearch = false;
    this.isCommandForTIDSearch = false;
    this.isLockSearchEPC = false;
    this.SEARCH_EPC = "";
    this.SEARCH_BARCODE = "";
    this.SEARCH_LOCKED_EPC = "";
    showLog("LOCATE", locateTag);
    if(!isReaderConnected()) return;
    try{
      //30361FAE6C47F64000989680
      //30361FAE6C47F60000989680
      reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
      reader.Actions.PreFilters.deleteAll();
      clearFilters();
      reader.Actions.TagLocationing.Perform(locateTag, null, null);
      
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      showLog("EXCCLOCATE1", e.getMessage());
      showLog("EXCCLOCATE11", e.getVendorMessage());
      //logInFile(context,"Locate EXC : " + e.getVendorMessage());
      stopLocateInventory();
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      //logInFile(context,"Locate EXC2 : " + e.getVendorMessage());
      showLog("EXCCLOCATE2", e.getMessage());
      showLog("EXCCLOCATE22", e.getVendorMessage());
      stopLocateInventory();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  public synchronized void stopInventory(){
    super.stopInventory();
    isTidBasedPick = false;
    if(pickTimer != null){
      pickTimer.cancel();
      pickTimer = null;
    }
    if(pickCountDownTimer != null) pickCountDownTimer.cancel();
    encPickedTag = null;
    loopFlag = false;
    // check reader connection
    if(!isReaderConnected()) return;
    try{
      clearFilters();
      if(reader != null && reader.Actions != null && reader.Actions.Inventory != null)
        reader.Actions.Inventory.stop();
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      showLog("STOPEXC1", e.getVendorMessage());
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      showLog("STOPEXC2", e.getVendorMessage());
      showLog("STOPEXC2", e.getMessage());
      showLog("STOPEXC2", e.getLocalizedMessage());
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  /**
   * Stop locate inventory.
   */
  public synchronized void stopLocateInventory(){
    this.isCommandForSearch = false;
    this.isCommandForEPCSearch = false;
    this.isCommandForTIDSearch = false;
    this.isLockSearchEPC = false;
    this.SEARCH_EPC = "";
    this.SEARCH_BARCODE = "";
    this.SEARCH_LOCKED_EPC = "";
    // check reader connection
    if(!isReaderConnected()) return;
    try{
      
      reader.Actions.Inventory.stop();
      
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      showLog("STOPEXCLCCCC1", e.getVendorMessage());
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      showLog("STOPEXCLCCC2", e.getVendorMessage());
    }
    catch(Exception e){
      e.printStackTrace();
      showLog("STOPEXCLCCC3", e.getMessage());
    }
  }
  
  /**
   * Config action.
   *
   * @param power           the power
   * @param session         the session
   * @param inventory_state the inventory state
   * @throws OperationFailureException the operation failure exception
   * @throws InvalidUsageException     the invalid usage exception
   */
  private void configAction(int power, SESSION session, INVENTORY_STATE inventory_state) throws OperationFailureException, InvalidUsageException{
    if(reader == null || reader.Config == null || reader.Config.Antennas == null) return;
    try{
      showLog(TAG + "_" + sessionType.name() + "_configAction_power", "" + power);
      Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
      config.setTransmitPowerIndex(power);
      config.setrfModeTableIndex(0);
      config.setTari(0);
      reader.Config.Antennas.setAntennaRfConfig(1, config);
    }
    catch(Exception e){
      e.printStackTrace();
    }
    AppCommonMethods.logInFile(context, sessionType.name(), "_CONFIG_SET_POWER (" + power + ")");
    try{
      if(isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING){
        //TODO latest change
        TagStorageSettings tagst = new TagStorageSettings();
        if(isDebugApp){
          tagst.setTagFields(TAG_FIELD.PHASE_INFO);
          tagst.setTagFields(TAG_FIELD.ANTENNA_ID);
          tagst.setTagFields(TAG_FIELD.TAG_SEEN_COUNT);
          tagst.setTagFields(TAG_FIELD.CHANNEL_INDEX);
          tagst.setTagFields(TAG_FIELD.XPC);
          tagst.setTagFields(TAG_FIELD.FIRST_SEEN_TIME_STAMP);
          tagst.setTagFields(TAG_FIELD.LAST_SEEN_TIME_STAMP);
          tagst.setTagFields(TAG_FIELD.CRC);
        }
        tagst.setTagFields(TAG_FIELD.PC);
        tagst.setTagFields(TAG_FIELD.PEAK_RSSI);
        reader.Config.setTagStorageSettings(tagst);
      }
      else{
        //TODO latest change
        TagStorageSettings tagst = new TagStorageSettings();
        if(isDebugApp){
          tagst.setTagFields(TAG_FIELD.PHASE_INFO);
          tagst.setTagFields(TAG_FIELD.ANTENNA_ID);
          tagst.setTagFields(TAG_FIELD.TAG_SEEN_COUNT);
          tagst.setTagFields(TAG_FIELD.CHANNEL_INDEX);
          tagst.setTagFields(TAG_FIELD.XPC);
          tagst.setTagFields(TAG_FIELD.FIRST_SEEN_TIME_STAMP);
          tagst.setTagFields(TAG_FIELD.LAST_SEEN_TIME_STAMP);
          tagst.setTagFields(TAG_FIELD.CRC);
          tagst.setTagFields(TAG_FIELD.PC);
        }
        //temp comment
        if(isStaticDebug()) tagst.setTagFields(TAG_FIELD.ALL_TAG_FIELDS);
        else tagst.setTagFields(TAG_FIELD.PEAK_RSSI);
        reader.Config.setTagStorageSettings(tagst);
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
    try{
      Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
      s1_singulationControl.setSession(session);
      s1_singulationControl.Action.setInventoryState(inventory_state);
      s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
      s1_singulationControl.setTagPopulation((short) 200);
      reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
      reader.Config.setUniqueTagReport(false);
      if(sessionAction != AppCommonMethods.SessionAction.SEARCH)
        reader.Config.setUniqueTagReport(true);
      /*if(sessionType==AppCommonMethods.SessionType.OFF_RANGE && sessionAction== AppCommonMethods.SessionAction.INVENTORY)
        reader.Config.setUniqueTagReport(false);*/
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  private void setPower(final int power){
    if(reader != null && reader.isConnected()){
      int oldPower;
      try{
        Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
        oldPower = config.getTransmitPowerIndex() / 10;
        config.setTransmitPowerIndex(power * 10);
        config.setrfModeTableIndex(0);
        
        config.setTari(0);
        reader.Config.Antennas.setAntennaRfConfig(1, config);
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
  }
  
  public synchronized void setReaderPower(final int power){
    if(reader != null && reader.isConnected()){
      setProgressMessage(true);
      AsyncTask.execute(new Runnable(){
        @Override
        public void run(){
          //          TriggerInfo triggerInfo = new TriggerInfo();
          //          triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
          //          triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
          int oldPower = 0;
          try{
            
            if(reader == null || reader.Config == null || reader.Config.Antennas == null){
              setProgressMessage(false);
              return;
            }
            // set trigger mode as rfid so scanner beam will not come
            //reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, true);
            //reader.Config.setKeylayoutType(ENUM_NEW_KEYLAYOUT_TYPE.RFID, ENUM_NEW_KEYLAYOUT_TYPE.RFID);
            // set start and stop triggers
            //            reader.Config.setStartTrigger(triggerInfo.StartTrigger);
            //            reader.Config.setStopTrigger(triggerInfo.StopTrigger);
            Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
            oldPower = config.getTransmitPowerIndex() / 10;
            config.setTransmitPowerIndex(power * 10);
            config.setrfModeTableIndex(0);
            
            config.setTari(0);
            reader.Config.Antennas.setAntennaRfConfig(1, config);
            if(sessionType != null && sessionType.getValue() > 0){
              showLog(sessionType.name() + "_SetPower", "" + power);
              SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), power);
            }
            setProgressMessage(false);
            SharedPrefManager.setReaderPower(power);
            readerPower.postValue(power);
            showLog("power", "" + power);
          }
          catch(InvalidUsageException | OperationFailureException e){
            e.printStackTrace();
            showLog("CONFIGEXC0", e.getMessage());
            setProgressMessage(false);
            readerPower.postValue(oldPower);
            SharedPrefManager.setReaderPower(oldPower);
            if(sessionType != null)
              SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), oldPower);
          }
        }
      });
    }
    else{
      setProgressMessage(false);
    }
  }
  
  public synchronized boolean performInventory(final boolean isHideUnencodedTags, final List<String> listIgnoreEPCs){
    if(super.performInventory(isHideUnencodedTags, listIgnoreEPCs)){
      try{
        setSingulationControlPrefilterReset();
        int maxPower = sessionType == AppCommonMethods.SessionType.INWARD_TOTE ? (int) (MIN_POWER_TO_SET * inwToteMinPowerMultiplier) : sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE ? (int) (MIN_POWER_TO_SET * owtToteMinPowerMultiplier) : MAX_POWER_TO_SET;
        int power = chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), maxPower / 10), maxPower / 10) * 10;
        if(power > maxPower){
          SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), maxPower / 10);
          power = maxPower;
        }
        showLog("maxPower", "" + maxPower);
        showLog("power", "" + power);
        reader.Actions.PreFilters.deleteAll();
        clearFilters();
        configAction(power, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
        reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.ENABLE);
        //reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
        sessionAction = AppCommonMethods.SessionAction.INVENTORY;
        if(readTid){ // tid based Inventory (Epc + Tid mode)
          TagAccess tagAccess = new TagAccess();
          TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
          //Set the param values
          readAccessParams.setCount(0);
          readAccessParams.setOffset(0);
          readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
          reader.Actions.TagAccess.readEvent(readAccessParams, null, null);
        }
        else reader.Actions.Inventory.perform();
        isInventoryOn.postValue(true);
        loopFlag = true;
        startTimer();
        showLog("PerformInventory", "" + true);
        return true;
      }
      catch(InvalidUsageException e){
        e.printStackTrace();
        showLog("EXC1INVENTORY", e.getVendorMessage());
        stopInventory();
        if(AppCommonMethods.isShowReaderCommandFailToast)
          AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
      }
      catch(OperationFailureException e){
        e.printStackTrace();
        //Response timeout
        showLog("EXCINVENTORY", e.getVendorMessage());
        if(e.getVendorMessage().equalsIgnoreCase("Charging in Progress-Command Not allowed")){
          AppCommonMethods.showShortToast(context, e.getVendorMessage());
        }
        if(e.getVendorMessage().equalsIgnoreCase("Response timeout")){
          disconnect();
          rfidInterface.RFIDInitializationStatus(false, "Reader Disconnected", null);
          AppCommonMethods.showShortToast(context, String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        }
        if(e.getVendorMessage().contains("Region Not Set")){
          AppCommonMethods.showShortToast(context, e.getVendorMessage());
          ConnectAndSetRegion("India");
        }
        if(!e.getVendorMessage().equalsIgnoreCase("Operation In Progress-Command Not Allowed")){
          stopInventory();
          if(AppCommonMethods.isShowReaderCommandFailToast && !e.getVendorMessage().matches("(?i)(Charging in Progress-Command Not allowed|Region Not Set|Response timeout)"))
            AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
        }
      }
      catch(Exception e){
        if(AppCommonMethods.isShowReaderCommandFailToast)
          AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
        e.printStackTrace();
      }
    }
    return false;
  }
  
  /**
   * Check timer.
   */
 /* private void checkTimer(){
    restrictTriggerPress = true;
    final Handler handler = new Handler(Looper.getMainLooper());
    handler.postDelayed(() -> {
      //Do something after 100ms
      restrictTriggerPress = false;
    }, 500);
  }*/
  
  /**
   * Store inventory data.
   *
   * @param tagData the tag data
   */
  private void storeInventoryData(final TagData tagData){
    final String epcdt = tagData.getTagID();
    if(sessionType == AppCommonMethods.SessionType.SEARCH_FIFO && sessionAction == AppCommonMethods.SessionAction.SEARCH && !isCommandForEPCSearch && !AppDatabase.getFIFODao(context).isEPCPresent(epcdt, ean, fifoDate/*,zone,zoneId*/))
      return;
    if(/*sessionType== AppCommonMethods.SessionType.STOCK_CORRECTION && */isNonEmpty(epcdt))
      AppCommonMethods.logInFile(context, sessionType.name(), "_TAG_EPC_" + chkNull(epcdt, "") + (sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION ? "_Status_" + AppDatabase.getProductDao(context).isEPCPresent(chkNull(epcdt, "")) : ""));
    if((isNonEmpty(sessionId) || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.SCAN) && sessionType.getValue() > 0){
      showLog(TAG + "_" + sessionAction.name() + "_" + "finalsgtinepc", epcdt);
      try{
        showLog("sessionAction", sessionAction.name());
        showLog("isActionPick", "" + isActionPick);
        showLog("isSinglePick", "" + isSinglePick);
        if(isCommandForSearch || sessionAction == AppCommonMethods.SessionAction.SEARCH){
          //final int rssi = tagData.getPeakRSSI();
          handleTagInfoForSearch(epcdt, String.valueOf(tagData.getPeakRSSI()), readTid && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? tagData.getMemoryBankData() : "");
          /*if(validateTagInfoForSearch(epcdt, String.valueOf(rssi))){
          showLog("RFIDHANDLER_rssi", "" + rssi);
          int actualPercentage = isUseShortRangeSearchForAll || (isUseShortRangeSearchForOnlyGID && context.epcEncoderDecoder.isGID() && SEARCH_EPC.startsWith("35")) ? getPercentageForGID(rssi) : getPercentage(rssi);
          percent = actualPercentage;
          showLog("RFIDHANDLER_actualPercentage", "" + actualPercentage);
          if(!isCommandForEPCSearch && !isCommandForTIDSearch){
            if(isLockSearchEPC && isNullOrEmpty(SEARCH_LOCKED_EPC)){
              SEARCH_LOCKED_EPC = epcdt;
            }
            if(!isLockSearchEPC || isNullOrEmpty(SEARCH_LOCKED_EPC) || epcdt.equalsIgnoreCase(SEARCH_LOCKED_EPC)){
              if(epcdt.length() >= 24){
                //check by using getBarcode method instead of switch case
                final String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt);
                final String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context, SEARCH_BARCODE);
                showLog("search_barcode", barcode);
                showLog("search_compare_barcode", compbarcode);
                if(!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase()))){
                  showLog("RFIDHANDLER_barcode", "" + barcode);
                  searchPercent.postValue(actualPercentage);
                  searchRssi.postValue(String.valueOf(tagData.getPeakRSSI()));
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
          else if(isCommandForEPCSearch){
            showLog("epc_search", "" + (epcdt + "==" + SEARCH_EPC + "?" + "_rssi:" + rssi + "_percent:" + percent));
            if(epcdt.equalsIgnoreCase(SEARCH_EPC) || ((sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST) && epcdt.length() > 1 && epcdt.equalsIgnoreCase("0" + SEARCH_EPC.substring(1)))){
              *//*if(sessionType== AppCommonMethods.SessionType.ENCODING || rfidSession.sessionType==AppCommonMethods.SessionType.ENCODING.getValue()){
                updateEncVerifyByEpc(epcdt);
              }*//*
              searchPercent.postValue(actualPercentage);
              searchRssi.postValue(String.valueOf(tagData.getPeakRSSI()));
              if(actualPercentage > 90){
                counter_for_threshold_percentage_to_sound_beep++;
                if(counter_for_threshold_percentage_to_sound_beep >= SOUND_THRESHOLD){
                  counter_for_threshold_percentage_to_sound_beep = 0;
                  playSound(context, R.raw.successbeep);
                }
              }
            }
          }
          else if(isCommandForTIDSearch){
            final String tid = readTid && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? "" + tagData.getMemoryBankData() : "";
            showLog("tid_search", "" + (tid + "==" + SEARCH_TID + "?" + "_rssi:" + rssi + "_percent:" + percent));
            if(tid.equalsIgnoreCase(SEARCH_TID)){
              if((sessionType == AppCommonMethods.SessionType.ENCODING || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue())) || (sessionType == AppCommonMethods.SessionType.ENCODING_THAN || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING_THAN.getValue()))){
                updateEncVerifyByEpcTid(epcdt, tid);
              }
              searchPercent.postValue(actualPercentage);
              searchRssi.postValue(String.valueOf(tagData.getPeakRSSI()));
              if(actualPercentage > 90){
                counter_for_threshold_percentage_to_sound_beep++;
                if(counter_for_threshold_percentage_to_sound_beep >= SOUND_THRESHOLD){
                  counter_for_threshold_percentage_to_sound_beep = 0;
                  playSound(context, R.raw.successbeep);
                }
              }
            }
          }
          }*/
        }
        else if(isSinglePick && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK)){
          //final String tid = readTid && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? "" + tagData.getMemoryBankData() : "";
          if(isTidBasedPick){
            final String tid = readTid && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? tagData.getMemoryBankData() : "";
            showLog("PICK_TID", tid);
            showLog("PICK_TID_COMPARE", encPickedTag.tid);
            showLog("PICK_TID_SIZE", "" + pickTags.size());
            if(encPickedTag != null && isNonEmpty(encPickedTag.tid) && isNonEmpty(tid) && chkNull(encPickedTag.tid, "").equalsIgnoreCase(chkNull(tid, "")) && !pickTags.contains(tid)){//compareWithOldTID
              pickTags.add(tid);
              pickUHFTags.add(tagData);
              pickTidBasedCountDownTimer.cancel();
              pickTidBasedCountDownTimer.onFinish();
            }
            return;
          }
          else{
            showLog("epc_pick", epcdt);
            final String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt);
            final String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context, SEARCH_BARCODE);
            showLog("pick_barcode", barcode);
            showLog("pick_compare_barcode", compbarcode);
            showLog("isActionTidPick", isActionTidPick + "_" + SCANNED_TIDS.toString());
            if(sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || epcdt.length() >= 24){
              String tid = tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? tagData.getMemoryBankData() : "";
              if(tid.length() > 24) tid = tid.substring(0, 24);
              showLog("isActionTidPick_scannedTids", isActionPick + "_" + SCANNED_TIDS.size());
              showLog("isActionTidPick_tid", isActionPick + "_" + tid);
              if(isSinglePick && ((!isActionTidPick && !pickTags.contains(epcdt)) || (isActionTidPick && !pickTags.contains(tid) && SCANNED_TIDS.contains(tid)/*tid.matches("(?i)(^" + SCANNED_TID + ".*$)")*/))){
                pickTags.add(isActionTidPick ? tid : epcdt);
                pickUHFTags.add(tagData);
                if(isActionTidPick && pickTags.size() == SCANNED_TIDS.size()){//1){
                  showLog("pickTags", "" + pickTags.size());
                  pickCountDownTimer.cancel();
                  pickCountDownTimer.onFinish();
                }
                else if(pickTags.size() > (isActionTidPick ? SCANNED_TIDS.size() : 1)){
                  showLog("SINGLEPICK_pickTags", "" + pickTags.size());
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
          if(isSinglePick){
          }
        }
        else{
          if((sessionType == AppCommonMethods.SessionType.ENCODING || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue())) || (sessionType == AppCommonMethods.SessionType.ENCODING_THAN || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING_THAN.getValue()))){
            showLog("Inv_ENC", sessionId + "_" + sessionType + "_" + sessionAction);
            updateFoundWrittenTag(epcdt, readTid && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? tagData.getMemoryBankData() : "");
            if(inventoryDao.getNonVerifiedCount(sessionId) <= 0){
              showLog("Inv_ENC", "all verified");
              stopInventory();
              context.showCustomSuccessDialog("Verified!");
            }
            else{
              updateEncVerifyByEpcTid(tagData.getTagID(), readTid && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? tagData.getMemoryBankData() : "");
            }
          }
          else if(sessionType == AppCommonMethods.SessionType.SEARCH_FILE || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.SEARCH_FILE.getValue())){
            final String tid = readTid && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? tagData.getMemoryBankData() : "";
            showLog("Inv_SER_FILE_epc_tid", epcdt + "_" + tid);
            final boolean isEPCPresent = inventoryDao.isEPCPresent(sessionId, epcdt);
            final boolean isTidPresent = inventoryDao.isTidPresent(sessionId, tid);
            showLog("Inv_SER_FILE_epc_tid", epcdt + "_" + tid + "_" + isEPCPresent + "_" + isTidPresent);
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
            final int rssi = readRssi ? tagData.getPeakRSSI() : 0;
            int actualPercentage = isUseShortRangeSearchForAll || (isUseShortRangeSearchForOnlyGID && epcdt.startsWith("35")) ? getPercentageForGID(rssi) : getPercentage(rssi);
            if(isNonEmpty(ean) && isNonEmpty(eans) && eans.contains(ean)){
              showLog("off_matched", "true");
              final ProductDao productDao = AppDatabase.getProductDao(context);
              productDao.updateRssiPercentage(sessionType.getValue(), ean, actualPercentage);
            }
          }
          else if(sessionType == AppCommonMethods.SessionType.SER_EXCEL || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.SER_EXCEL.getValue())){
            final String ean = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt);
            final int rssi = readRssi ? tagData.getPeakRSSI() : 0;
            int actualPercentage = isUseShortRangeSearchForAll || (isUseShortRangeSearchForOnlyGID && epcdt.startsWith("35")) ? getPercentageForGID(rssi) : getPercentage(rssi);
            if(isNonEmpty(ean) && isNonEmpty(eans) && eans.contains(ean)){
              showLog("off_matched", "true");
              final ProductDao productDao = AppDatabase.getProductDao(context);
              productDao.updateRssiPercentage(sessionType.getValue(), ean, actualPercentage);
            }
          }
          else{
            //            final String ean = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase().trim();
            //            final BrandEanDao brandEansDao = AppDatabase.getBrandEansDao(context);
            //            final ProductDao productDao = AppDatabase.getProductDao(context);
            //            final FIFODao fifoDao = AppDatabase.getFIFODao(context);
            //            //final String ean = context.epcEncoderDecoder.getBarcodeFromEPC(epcfdt).toUpperCase().trim();
            //            if(sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION && !productDao.isEPCPresent(epcdt)){
            //              return;
            //            }
            //            if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY && (/*(brandEansDao.hasData() && !brandEansDao.isEANPresent("," + context.epcEncoderDecoder.getBarcodeFromEPC(epcdt) + ",")) ||*/ (isNonEmpty(eans) && !eans.contains(ean)))){
            //              return;
            //            }
            //            if(sessionType == AppCommonMethods.SessionType.OFF_RANGE && (/*(brandEansDao.hasData() && !brandEansDao.isEANPresent("," + context.epcEncoderDecoder.getBarcodeFromEPC(epcdt) + ",")) ||*/ (isNonEmpty(eans) && !eans.contains(ean)))){
            //              return;
            //            }
            //            if((sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE || sessionType == AppCommonMethods.SessionType.INWARD_TOTE) && isErrorOrBeepForTote() && isNonEmpty(eans)){
            //              if((AppCommonMethods.isShowErrorForOtherThanToteEanTag || AppCommonMethods.isPlayErrorBeepForOtherThanToteEanTag) && isNonEmpty(eans) && !eans.contains(ean)){
            //                if(AppCommonMethods.isShowErrorForOtherThanToteEanTag){
            //                  stopInventory();
            //                  context.showCustomErrDialog(String.format(context.getString(R.string.err_msg_pick_wrong_tote), ean));
            //                  return;
            //                }
            //                else if(AppCommonMethods.isPlayErrorBeepForOtherThanToteEanTag) errorBeep();
            //              }
            //              else if(AppCommonMethods.isShowErrorForSameToteTag && inventoryDao.isEPCPresent(sessionId, epcdt)){
            //                stopInventory();
            //                context.showCustomErrDialog(R.string.err_msg_already_added);
            //                return;
            //              }
            //              else if(AppCommonMethods.isPlayBeepForSameToteTagIfNotLastInserted && !chkNull(inventoryDao.getLastInsertedEpc(sessionId),"").equalsIgnoreCase(epcdt)) beep();
            //            }
            //            //Checks only Non-Alien Decoded Tags
            //            if(sessionType == AppCommonMethods.SessionType.SEARCH_UNENCODED && !context.epcEncoderDecoder.setDataFromEPC(new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue(), epcdt)).tagStatus.equalsIgnoreCase(AppConstants.NON_ENCODED))//!chkNull(ApplicationCommonMethods.getBarcodeFromEPC(uhftagInfo.getEPC()), AppConstants.NON_ENCODED).matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.UNKNOWN + ")"))
            //              return;
            //            //Checks only Alien Tags
            //            if(sessionType == AppCommonMethods.SessionType.SEARCH_ALIEN && !context.epcEncoderDecoder.setDataFromEPC(new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue(), epcdt)).tagStatus.equalsIgnoreCase(AppConstants.ALIEN))
            //              return;
            //            //Checks only Already Present Tag
            //            if(inventoryDao.isEPCPresent(sessionId, epcdt)) return;
            //            //Checks only Selected Ean Tags
            //            if(isNonEmpty(eans) && !eans.contains(ean)) return;
            //            //Check if Unencoded Tags should be scanned
            //            if(isHideUnencodedTags && ean.equalsIgnoreCase(AppConstants.NON_ENCODED)) return;
            //            //Check if Epcs Should be ignored
            //            if(isNonEmpty(listIgnoreEpcs) && listIgnoreEpcs.contains(epcdt)) return;
            
            if(validateTagInfoForInventory(epcdt) && sessionAction.getValue() > 0 && sessionAction != AppCommonMethods.SessionAction.SEARCH && (!isSinglePick || sessionAction != AppCommonMethods.SessionAction.PICK))
              saveInventoryData(tagData);
            //            else if(false && sessionAction.getValue() > 0 && sessionAction != AppCommonMethods.SessionAction.SEARCH && (!isSinglePick || sessionAction != AppCommonMethods.SessionAction.PICK)){
            //              try{
            //                showLog("SAVE", "YES");
            //                Inventory inventory = new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
            //                inventory.epc = epcdt;
            //                inventory.tid = (readTid || (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST || sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE || sessionType == AppCommonMethods.SessionType.INWARD_TOTE) && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? /*AppCommonMethods.isUse24LengthTIDForUpload && chkNull(tagData.getMemoryBankData(),"").length()>24? tagData.getMemoryBankData().substring(0,24) :*/ tagData.getMemoryBankData() : "";//change to appropriate method
            //                inventory.rssi = readRssi ? "" + tagData.getPeakRSSI() : "";
            //                inventory.pcdata = readPC ? "" + Integer.toHexString(tagData.getPC()) : "";
            //                inventory.zone = zone;
            //                inventory.zoneId = zoneId;
            //                try{
            //                  inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
            //                }
            //                catch(Exception e){ e.printStackTrace(); }
            //                inventory.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
            //                if((!AppCommonMethods.isSetInwOnline && sessionType == AppCommonMethods.SessionType.INWARD) || sessionType == AppCommonMethods.SessionType.OUTWARD){
            //                  //Set 3rd Table for this
            //                  final String tripNo = SharedPrefManager.getTripNo();
            //                  final String huNo = SharedPrefManager.getHuNo();
            //                  final String ean1 = chkNull(inventory.ean, "").replaceFirst(AppConstants.UNKNOWN, AppConstants.NON_ENCODED);
            //                  final TripInventoryDao tripInventoryDao = AppDatabase.getTripInventoryDao(context);
            //                  final String articleCode = !chkNull(ean1, AppConstants.NON_ENCODED).equalsIgnoreCase(AppConstants.NON_ENCODED) ? chkNull(tripInventoryDao.getArticleCode(ean1, huNo, tripNo), AppConstants.EXTRA_EAN) : AppConstants.NON_ENCODED;
            //                  final Integer originalEanQty = !chkNull(articleCode, AppConstants.NON_ENCODED).matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.EXTRA_EAN + ")") ? chkNull(tripInventoryDao.getOriginalArticleQty(tripNo, huNo, articleCode),0) : 0;
            //
            //                  TripInventory tripInventory = new TripInventory(tripNo, SharedPrefManager.getDeliveryNo(), huNo);
            //                  tripInventory.userAction = "RFID";
            //                  tripInventory.ean = ean1;
            //                  tripInventory.eanQty = originalEanQty;
            //                  tripInventory.tid = inventory.tid;
            //                  tripInventory.epc = inventory.epc;
            //                  tripInventory.rssi = inventory.rssi;
            //                  tripInventory.isOriginal = originalEanQty > 0;
            //                  tripInventory.articleCode = articleCode;
            //                  tripInventory.isHardTag = inventory.isHardTag;
            //                  if(!tripInventoryDao.isEpcPresent(tripNo, huNo, inventory.epc))
            //                    tripInventoryDao.insertTripInventoryData(tripInventory);
            //                }
            //                else{
            //                  if(isNullOrEmpty(sessionId) || (isNullOrEmpty(inventory.tid) && (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST || sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE || sessionType == AppCommonMethods.SessionType.INWARD_TOTE)))
            //                    throw new NullPointerException();
            //                  inventoryDao.insertInventoryData(inventory);
            //                }
            //                if(productDao != null && sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION){
            //                  if(zone.equalsIgnoreCase(AppConstants.ALL)) productDao.updateFound(inventory.epc);
            //                  else productDao.updateFound(inventory.epc, zone);
            //                }
            //                if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
            //                  productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
            //                if(productDao != null && sessionType == AppCommonMethods.SessionType.OUTWARD_PICK)
            //                  productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
            //                if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY)
            //                  brandEansDao.updateScanQty("," + inventory.ean + ",");
            //                if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO && fifoDate != null && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK))
            //                  fifoDao.updateFound(inventory.ean, inventory.epc, fifoDate);
            //              }
            //              catch(SQLiteConstraintException sql){//Don't handle
            //              }
            //              catch(Exception ex){
            //                ex.printStackTrace();
            //                showLog("SQLEXC", "" + ex.getMessage());
            //              }
            //            }
          }
        }
      }
      catch(Exception ex){
        ex.printStackTrace();
        showLog("EXCC", ex.getMessage());
      }
    }
  }
  
  private void saveInventoryData(final TagData tagData){
    final BrandEanDao brandEansDao = AppDatabase.getBrandEansDao(context);
    final ProductDao productDao = AppDatabase.getProductDao(context);
    final FIFODao fifoDao = AppDatabase.getFIFODao(context);
    final String epcdt = tagData.getTagID();
    if(inventoryDao.isEPCPresent(sessionId, epcdt)) return;
    if(sessionAction.getValue() > 0 && sessionAction != AppCommonMethods.SessionAction.SEARCH && (!isSinglePick || sessionAction != AppCommonMethods.SessionAction.PICK)){
      try{
        showLog("SAVE", "YES");
        Inventory inventory = new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
        inventory.epc = epcdt.toUpperCase();
        inventory.tid = (readTid || (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST || sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE || sessionType == AppCommonMethods.SessionType.INWARD_TOTE) && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? /*AppCommonMethods.isUse24LengthTIDForUpload && chkNull(tagData.getMemoryBankData(),"").length()>24? tagData.getMemoryBankData().substring(0,24) :*/ tagData.getMemoryBankData() : "";//change to appropriate method
        inventory.rssi = readRssi ? "" + tagData.getPeakRSSI() : "";
        inventory.pcdata = readPC ? Integer.toHexString(tagData.getPC()) : "";
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
          final TripInventoryDao tripInventoryDao = AppDatabase.getTripInventoryDao(context);
          final String tripNo = SharedPrefManager.getTripNo();
          final String huNo = SharedPrefManager.getHuNo();
          final TripStatus tripStatus = AppDatabase.getTripStatusDao(context).getTripData(tripNo, sessionType == AppCommonMethods.SessionType.OUTWARD ? AppConstants.OUTWARD : AppConstants.INWARD);
          final boolean hasArticleData = (tripStatus == null || tripStatus.isArticleBasedTrip()) && tripInventoryDao.hasArticleData(tripNo, huNo);
          //Apply condition based on rfid list
          ArrayList<String> epcs = SharedPrefManager.getStringArrayList(tripNo + huNo + ParamConstants.RFIDS, new ArrayList<>(0));
          final boolean isEpcBasedChecking = isNonEmpty(epcs);
          
          final String ean = /*isEpcBased && !epcs.contains(inventory.epc)?AppConstants.UNKNOWN:*/chkNull(inventory.ean, "").replaceFirst(AppConstants.UNKNOWN, AppConstants.NON_ENCODED);
          final String articleCode = !chkNull(ean, AppConstants.NON_ENCODED).equalsIgnoreCase(AppConstants.NON_ENCODED) ? ean.equalsIgnoreCase(AppConstants.UNKNOWN) ? AppConstants.UNKNOWN : chkNull(tripInventoryDao.getArticleCode(ean, huNo, tripNo), AppConstants.EXTRA_EAN) : AppConstants.NON_ENCODED;
          final Integer originalEanQty = chkNull(ean, AppConstants.NON_ENCODED).equalsIgnoreCase(AppConstants.NON_ENCODED) ? 0 : isEpcBasedChecking && !epcs.contains(inventory.epc) ? 0 : hasArticleData && !chkNull(articleCode, AppConstants.NON_ENCODED).matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.EXTRA_EAN + ")") ? chkNull(tripInventoryDao.getOriginalArticleQty(tripNo, huNo, articleCode), 0) : !hasArticleData ? chkNull(tripInventoryDao.getOriginalEanQty(tripNo, huNo, ean), 0) : 0;
          
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
          if(isNullOrEmpty(sessionId) || (isNullOrEmpty(inventory.tid) && (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST || sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE || sessionType == AppCommonMethods.SessionType.INWARD_TOTE)))
            throw new NullPointerException();
          inventoryDao.insertInventoryData(inventory);
        }
        if(productDao != null && sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION){
          if(zone.equalsIgnoreCase(AppConstants.ALL)) productDao.updateFound(inventory.epc);
          else productDao.updateFound(inventory.epc, zone);
        }
        if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
          productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
        if(productDao != null && sessionType == AppCommonMethods.SessionType.OUTWARD_PICK)
          productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
        if(productDao != null && sessionType == AppCommonMethods.SessionType.OFF_RANGE)
          productDao.updateFoundEPCOffRange(inventory.epc, inventory.ean, zone);
        if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY)//sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY)
          brandEansDao.updateScanQty("," + inventory.ean + ",");
        if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO && fifoDate != null && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK))
          fifoDao.updateFound(inventory.ean, inventory.epc, fifoDate);
      }
      catch(SQLiteConstraintException sql){//Don't handle
      }
      catch(Exception ex){
        ex.printStackTrace();
        showLog("SQLEXC", ex.getMessage());
      }
    }
  }
  
  protected Inventory getDataFromTagInfo(Object object){
    return object != null && object instanceof TagData ? getDataFromTagInfo((TagData) object) : new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
  }
  
  /**
   * Get data from tag info inventory.
   *
   * @param tagData the tag data
   * @return the inventory
   */
  
  private Inventory getDataFromTagInfo(TagData tagData){
    Inventory inventory = new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
    inventory.epc = tagData.getTagID().toUpperCase();
    inventory.tid = (readTid || (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST || sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE || sessionType == AppCommonMethods.SessionType.INWARD_TOTE) && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? /*AppCommonMethods.isUse24LengthTIDForUpload && chkNull(tagData.getMemoryBankData(),"").length()>24? tagData.getMemoryBankData().substring(0,24) :*/ tagData.getMemoryBankData() : "";//change to appropriate method
    inventory.rssi = readRssi ? "" + tagData.getPeakRSSI() : "";
    inventory.pcdata = readPC ? Integer.toHexString(tagData.getPC()) : "";
    inventory.zone = zone;
    inventory.zoneId = zoneId;
    try{
      inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
    }
    catch(Exception e){
      e.printStackTrace();
    }
    /*inventory.tagtype = readType ? ApplicationCommonMethods.getTagType(inventory.epc) : 0;
    inventory.isHardTag = null;
    if(inventory.ean==null) inventory.ean="";
    if(readEAN){
      try{
        inventory.ean = getBarcodeFromEPC(chkNull(inventory.epc, ""));
      }catch(Exception e){inventory.ean = AppConstants.UNKNOWN;}
      try{
        inventory.isHardTag = SGTIN96.isTagTypeHard(inventory.epc);
      }catch(Exception e) {inventory.ean = AppConstants.ALIEN_PREFIX+inventory.ean; }
    }*/
    return inventory;
  }
  
  /**
   * Perform decoding.
   *
   * @param pickedTags  the picked tags
   * @param sessionType the session type
   */
  @Override
  public void performDecoding(final List<Inventory> pickedTags, final AppCommonMethods.SessionType sessionType){
    showLog("pickedTags", "" + pickedTags.size());
    showLog("sessionType", sessionType.name());
    if(isNonEmpty(pickedTags) && sessionType.getValue() > 0){
      if(reader != null){
        ZebraRFIDHandler.this.sessionType = sessionType;
        sessionAction = AppCommonMethods.SessionAction.DECODE;
        multiWriteSuccessCount = 0;
        multiWriteListSize = pickedTags.size();
        multiWriteCount = multiWriteListSize;
        isMultiWriteDone = false;
        try{
          reader.Actions.PreFilters.deleteAll();
          clearFilters();
          configAction(MAX_POWER_TO_SET - 30, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
          //reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
          reader.Config.setAccessOperationWaitTimeout(WRITE_OPERATION_TIMEOUT);
        }
        catch(InvalidUsageException e){
          e.printStackTrace();
        }
        catch(OperationFailureException e){
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
          else updateTagWriteCount(false);
        }
        /*if(multiWriteCount == 0 && !isMultiWriteDone){
          isDecodeOn.postValue(false);
          setProgressMessage(false);
        }*/
      }
      else{
        isDecodeOn.postValue(false);
        context.showCustomErrDialog(R.string.err_decoding_fail);
        setProgressMessage(false);
        //fail
      }
    }
  }
  
  /**
   * Perform decoding.
   *
   * @param pickedTag the picked tag
   */
  public void performDecodingOld(final Inventory pickedTag){
    if(pickedTag == null) return;
    if(reader != null){
      sessionAction = AppCommonMethods.SessionAction.DECODE;
      isDecodeOn.postValue(true);
      isMultiWriteDone = false;
      isRetryEncode = true;
      isReEncodeForOldEPC = true;
      //encodeOldEpc = multiDecodeCount<=0?pickedTag.epc:null;
      setProgressMessage(context.getString(R.string.msg_pick), true);
      try{
        reader.Actions.PreFilters.deleteAll();
        clearFilters();
        configAction(MAX_POWER_TO_SET - 30, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
        //reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
        reader.Config.setAccessOperationWaitTimeout(WRITE_OPERATION_TIMEOUT);
      }
      catch(InvalidUsageException e){
        e.printStackTrace();
      }
      catch(OperationFailureException e){
        e.printStackTrace();
      }
      catch(Exception e){
        e.printStackTrace();
      }
      startDecoding(pickedTag, SharedPrefManager.getCurrentAccessPassword());
    }
    else{
      isDecodeOn.postValue(false);
      context.showCustomErrDialog(R.string.err_decoding_fail);
      setProgressMessage(false);
      //fail
    }
  }
  
  @Override
  public void performEncoding(final Inventory pickedTag, String password){
    if(SharedPrefManager.getIsDeviceBluetoothDependent()) performEncodingOld(pickedTag, password);
    else performEncoding(pickedTag, password, true);
  }
  
  public void performEncoding(final Inventory pickedTag, String password, boolean isRetryEncode){
    showLog(sessionType.name(), "performEncodingNew_START");
    AppCommonMethods.logInFile(context, sessionType.name(), "_PERFORM_ENCODING_START_");
    if(reader != null){
      sessionAction = AppCommonMethods.SessionAction.ENCODE;
      isEncodeOn.postValue(true);
      this.isRetryEncode = isRetryEncode;
      isReEncodeForOldEPC = true;
      encodeOldEpc = pickedTag.epc;
      isMultiWriteDone = false;
      //setProgressMessage(context.getString(R.string.msg_pick), true);
      
      setSingulationControl();
      setStopTriggerRead();
      boolean isInNonPasswordTIDs = false;
      final boolean isSameLenEpc = pickedTag.epc.length() == pickedTag.newEpc.length();
      final Inventory pickTag = isSameLenEpc ? pickedTag : context.epcEncoderDecoder.setPCDataBeforeEncoding(pickedTag);
      if(pickTag == null){
        isEncodeOn.postValue(false);
        context.showCustomErrDialog("Unable to set PC data! Invalid header.");
        setProgressMessage(false);
        return;
      }
      else if(chkNull(pickTag.tid, "").length() > 8){
        if(SharedPrefManager.getNon128BitTids().contains(pickTag.tid.toUpperCase().substring(0, 8)) && pickTag.newEpc.length() > 28){
          context.showCustomErrDialog(context.getString(R.string.err_encoding_non_std_not_allowed_for_non_128_bit_tids));
          return;
        }
        isInNonPasswordTIDs = SharedPrefManager.getNonPasswordTids().contains(chkNull(pickTag.tid, "").toUpperCase().substring(0, 8));
        final List<String> listPasswords = SharedPrefManager.getOldAccessPasswords();
        if(!listPasswords.contains(defaultTagZeroPassword))
          listPasswords.add(0, defaultTagZeroPassword);
        listPasswords.remove(SharedPrefManager.getCurrentAccessPassword());
        listPasswords.add(0, SharedPrefManager.getCurrentAccessPassword());
        if(isInNonPasswordTIDs || SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_DEFAULT_PASSWORD_FIRST)){
          listPasswords.remove(defaultTagZeroPassword);
          listPasswords.add(0, defaultTagZeroPassword);
        }
        
        setPower((MAX_POWER_TO_SET / 10) - 5);
        RFIDResults rfidResults = setPrefilterTid(pickTag.tid, true);
        showLog("setPrefilterTid_Result", "" + (rfidResults.ordinal == RFID_API_SUCCESS.ordinal));
        //RFIDResults rfidResults = setPrefilter(pickTag.epc, true);
        //showLog("setPrefilterTid_Result", "" + (rfidResults.ordinal == RFID_API_SUCCESS.ordinal));
        //addEpcBasedFilters(pickTag.epc,pickTag.epc.length()>24);
        boolean isWriteSuccess = false;
        String result = "";
        List<String> results = new ArrayList<>(0);
        String tagPassword = "";
        boolean isReWrite = false;
        for(String pass : listPasswords){
          result = startEncodingNew(pickTag, isSameLenEpc, pass);
          if(result.startsWith("ReEncode")){
            isReWrite = true;
            tagPassword = pass;
            if(isNonEmpty(result.replaceFirst("ReEncode", "")))
              pickTag.epc = result.replaceFirst("ReEncode", "").toUpperCase();
            showLog("ReEncode", result + "EPC:" + pickTag.epc + "_" + "TID:" + pickTag.tid + "_" + "NewEPC:" + pickTag.newEpc);
            break;
          }
          else{
            results.add(result);
            AppCommonMethods.logInFile(context, sessionType.name(), "_START_ENCODING_RESULT_" + "->(" + result + ")");
            showLog(sessionType.name(), "_START_ENCODING_RESULT_" + pass + "->(" + result + ")");
            if(result.equalsIgnoreCase("Success")){
              tagPassword = pass;
              isWriteSuccess = true;
              break;
            }
            else{
              AppCommonMethods.showLog("Write EPC Operation:", result);
              //updateTagWriteCount(false,result);
            }
          }
        }
        clearFilters();
        if(isWriteSuccess){
          showLog("ENCODING_ENCODE_EPC_WRITE", "DONE");
          AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_STOP (" + tagPassword + ")");
          if(!isInNonPasswordTIDs && !tagPassword.equalsIgnoreCase(SharedPrefManager.getCurrentAccessPassword()))
            writeCurrentPasswordInTagNew(pickTag, tagPassword);
          updateDBAfterEncode(pickTag);
          //          successBeep();
          //          updateTagWriteCount(true);
        }
        else if(isReWrite && isRetryEncode){
          showLog("ENCODING_ENCODE_EPC_WRITE", "RE-WRITE");
          performEncoding(pickTag, tagPassword, false);
        }
        else{
          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && results.stream().distinct().count() == 1)
            updateTagWriteCount(results.get(0));
          else if(results.contains(context.getString(R.string.err_encoding_fail_result_unknown)))
            updateTagWriteCount(context.getString(R.string.err_encoding_fail_result_unknown));
          else updateTagWriteCount(results.get(0));
        }
      }
    }
    else{
      isEncodeOn.postValue(false);
      context.showCustomErrDialog(R.string.err_reader_connection);
      setProgressMessage(false);
    }
    //showLog("performEncodingNew", "STOP");
    showLog(sessionType.name(), "performEncodingNew_STOP");
  }
  
  @Override
  public void performDecoding(final Inventory pickedTag){
    performDecoding(pickedTag, true);
  }
  
  public void performDecoding(final Inventory pickedTag, boolean isRetryDecode){
    showLog(sessionType.name(), "performDecodingNew_START");
    AppCommonMethods.logInFile(context, sessionType.name(), "_PERFORM_DECODING_START_");
    if(reader != null){
      sessionAction = AppCommonMethods.SessionAction.DECODE;
      isDecodeOn.postValue(true);
      this.isRetryEncode = isRetryDecode;
      isReEncodeForOldEPC = true;
      encodeOldEpc = pickedTag.epc;
      isMultiWriteDone = false;
      //setProgressMessage(context.getString(R.string.msg_pick), true);
      
      setSingulationControl();
      setStopTriggerRead();
      boolean isInNonPasswordTIDs = false;
      final boolean isSameLenEpc = pickedTag.epc.length() == pickedTag.newEpc.length();
      final Inventory pickTag = isSameLenEpc ? pickedTag : context.epcEncoderDecoder.setPCDataBeforeEncoding(pickedTag);
      if(pickTag == null){
        isDecodeOn.postValue(false);
        context.showCustomErrDialog("Unable to set PC data! Invalid header.");
        setProgressMessage(false);
        return;
      }
      else if(chkNull(pickTag.tid, "").length() > 8){
        if(SharedPrefManager.getNon128BitTids().contains(pickTag.tid.toUpperCase().substring(0, 8)) && pickTag.newEpc.length() > 28){
          context.showCustomErrDialog(context.getString(R.string.err_encoding_non_std_not_allowed_for_non_128_bit_tids));
          return;
        }
        isInNonPasswordTIDs = SharedPrefManager.getNonPasswordTids().contains(chkNull(pickTag.tid, "").toUpperCase().substring(0, 8));
        final List<String> listPasswords = SharedPrefManager.getOldAccessPasswords();
        if(!listPasswords.contains(defaultTagZeroPassword))
          listPasswords.add(0, defaultTagZeroPassword);
        listPasswords.remove(SharedPrefManager.getCurrentAccessPassword());
        listPasswords.add(0, SharedPrefManager.getCurrentAccessPassword());
        if(isInNonPasswordTIDs || SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_DEFAULT_PASSWORD_FIRST)){
          listPasswords.remove(defaultTagZeroPassword);
          listPasswords.add(0, defaultTagZeroPassword);
        }
        
        setPower((MAX_POWER_TO_SET / 10) - 5);
        RFIDResults rfidResults = setPrefilterTid(pickTag.tid, true);
        showLog("setPrefilterTid_Result", "" + (rfidResults.ordinal == RFID_API_SUCCESS.ordinal));
        //RFIDResults rfidResults = setPrefilter(pickTag.epc, true);
        //showLog("setPrefilterTid_Result", "" + (rfidResults.ordinal == RFID_API_SUCCESS.ordinal));
        //addEpcBasedFilters(pickTag.epc,pickTag.epc.length()>24);
        boolean isWriteSuccess = false;
        String result = "";
        List<String> results = new ArrayList<>(0);
        String tagPassword = "";
        boolean isReWrite = false;
        for(String pass : listPasswords){
          result = startEncodingNew(pickTag, isSameLenEpc, pass);
          if(result.startsWith("ReDecode")){
            isReWrite = true;
            tagPassword = pass;
            if(isNonEmpty(result.replaceFirst("ReDecode", "")))
              pickTag.epc = result.replaceFirst("ReDecode", "").toUpperCase();
            showLog("ReDecode", result + "EPC:" + pickTag.epc + "_" + "TID:" + pickTag.tid + "_" + "NewEPC:" + pickTag.newEpc);
            break;
          }
          else{
            results.add(result);
            AppCommonMethods.logInFile(context, sessionType.name(), "_START_DECODING_RESULT_" + "->(" + result + ")");
            showLog(sessionType.name(), "_START_DECODING_RESULT_" + pass + "->(" + result + ")");
            if(result.equalsIgnoreCase("Success")){
              tagPassword = pass;
              isWriteSuccess = true;
              break;
            }
            else{
              AppCommonMethods.showLog("Write EPC Operation:", result);
              //updateTagWriteCount(false,result);
            }
          }
        }
        clearFilters();
        if(isWriteSuccess){
          showLog("DECODING_DECODE_EPC_WRITE", "DONE");
          AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_STOP (" + tagPassword + ")");
          updateDBAfterEncode(pickTag);
          //          successBeep();
          //          updateTagWriteCount(true);
        }
        else if(isReWrite && isRetryEncode){
          showLog("DECODING_DECODE_EPC_WRITE", "RE-WRITE");
          performDecoding(pickTag, false);
        }
        else{
          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && results.stream().distinct().count() == 1)
            updateTagWriteCount(results.get(0));
          else if(results.contains(context.getString(R.string.err_decoding_fail_result_unknown)))
            updateTagWriteCount(context.getString(R.string.err_decoding_fail_result_unknown));
          else updateTagWriteCount(results.get(0));
        }
      }
    }
    else{
      isDecodeOn.postValue(false);
      context.showCustomErrDialog(R.string.err_reader_connection);
      setProgressMessage(false);
    }
    //showLog("performEncodingNew", "STOP");
    showLog(sessionType.name(), "performDecodingNew_STOP");
  }
  
  public void updateDBAfterEncode(final Inventory pickedTag){
    AppCommonMethods.showLog("updateDBAfterEncode", "START");
    boolean isSuccess = false;
    try{
      final String pc = pickedTag.pcdata;
      final String tid = pickedTag.tid;
      //if(isNonEmpty(encodeOldEpc) && !pickedTag.epc.equalsIgnoreCase(encodeOldEpc))
      //pickedTag.epc = encodeOldEpc;
      pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
      pickedTag.writeFailReason = null;
      pickedTag.isUploaded = false;
      if(sessionAction == AppCommonMethods.SessionAction.ENCODE && isNonEmpty(chkZero(pc, "")) && pickedTag.newEpc.toUpperCase().trim().matches("^(" + pc + ").*$"))
        pickedTag.newEpc = pickedTag.newEpc.replaceFirst(pc, "").toUpperCase().trim();
      if(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING) && isNonEmpty(fifoDate))
        pickedTag.fifoDate = fifoDate;
      //      if(SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY))
      //        pickedTag.encVerifyStatus=AppCommonMethods.EncodeVerifyStatus.PENDING.ordinal();
      try{
        if(SharedPrefManager.getBoolean(ParamConstants.IS_TID_BASED_COUNT_FOR_ENCODE) && inventoryDao.isTidPresent(sessionId, pickedTag.tid)){
          pickedTag.ino = chkNull(pickedTag.ino, chkNull(inventoryDao.getRowIdFromTid(sessionId, tid), 0));
          inventoryDao.updateInventoryData(pickedTag);
        }
        else{
          if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST ? inventoryDao.isEPCPresent(sessionId, pickedTag.epc) : inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
            //if(inventoryDao.isTagPresent(sessionId,pickedTag.epc,pickedTag.newEpc,pickedTag.tid) || (inventoryDao.isEPCPresent(sessionId, pickedTag.epc) && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST)))
            inventoryDao.updateInventoryData(pickedTag);
          else inventoryDao.insertInventoryData(pickedTag);
        }
        uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
        isSuccess = true;
      }
      catch(Exception e){
        e.printStackTrace();
      }
      if(sessionAction == AppCommonMethods.SessionAction.DECODE){
        try{
          final ProductDao productDao = AppDatabase.getProductDao(context);
          final FIFODao fifoDao = AppDatabase.getFIFODao(context);
          if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
            productDao.updateDecodedEPC(pickedTag.epc, pickedTag.ean, pickedTag.zone);
          else if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
            fifoDao.updateDecoded(pickedTag.ean, pickedTag.epc, fifoDate);
        }
        catch(Exception e){
          e.printStackTrace();
        }
      }
      //isEncodeDone.postValue(true);
      clearFilters();
      //AppCommonMethods.successBeep();
      updateTagWriteCount(true);
                  /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                      isEncodeOn.postValue(false);
                      setProgressMessage(false);
                    }
                  }, 50);*/
    }
    catch(SQLiteConstraintException sql){/*Don't handle*/
    }
    catch(Exception e){
      e.printStackTrace();
    }
    AppCommonMethods.showLog("updateDBAfterEncode", "STOP_" + isSuccess);
  }
  
  public String startEncodingNew(final Inventory pickTag, final boolean isSameLenEpc, final String currentPassword){
    final String logTag = sessionAction == AppCommonMethods.SessionAction.DECODE ? "Decod" : "Encod";
    AppCommonMethods.logInFile(context, sessionType.name(), "_START_" + logTag.toUpperCase() + "ING_START_(" + currentPassword + ")");
    showLog(sessionType.name(), "_START_" + logTag.toUpperCase() + "ING_START_(" + currentPassword + ")");
    if(reader != null){
      final String epcdt = pickTag.epc;
      final int currentWordLen = epcdt.length() / 4;//originalTagID.length() / 4;
      final String writeData = isSameLenEpc ? pickTag.newEpc : pickTag.newEpc.substring(4);
      final String writeDataPC = !isSameLenEpc ? pickTag.newEpc.substring(0, 4) : "";
      final int iTotalWriteLengthWORDPC = (writeDataPC.length() / 4);
      final int iTotalWriteLengthWORDEPC = (writeData.length() / 4);
      TagData tagData = new TagData();
      TagAccess tagAccess = new TagAccess();
      /////////////////////////////////////////////
      // Step 5: Write 8 tags
      /////////////////////////////////////////////
      String result;
      try{
        tagData = new TagData();
        if(isNonEmpty(writeDataPC)){ //Write PC Data
          showLog("writingData PC", writeDataPC + "_" + iTotalWriteLengthWORDPC);
          TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
          writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
          writeAccessParams.setOffset(1);
          writeAccessParams.setWriteData(writeDataPC);
          writeAccessParams.setWriteDataLength(iTotalWriteLengthWORDPC);
          writeAccessParams.setAccessPassword(Long.decode("0X" + currentPassword));
          showLog("writeDataPC", epcdt + " -> " + writeDataPC + "_" + iTotalWriteLengthWORDPC);
          reader.Actions.TagAccess.write(null, writeAccessParams, null, tagData, true, WRITE_OPERATION_TIMEOUT);
        }
        TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
        showLog("writingData Epc", epcdt + " -> " + writeData + "_" + iTotalWriteLengthWORDEPC + " with pass:" + currentPassword);
        writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
        writeAccessParams.setOffset(2);
        writeAccessParams.setWriteData(writeData);
        writeAccessParams.setWriteDataLength(iTotalWriteLengthWORDEPC);
        writeAccessParams.setAccessPassword(Long.decode("0X" + currentPassword));
        
        reader.Actions.TagAccess.writeblock(null, writeAccessParams, null, tagData, true, WRITE_OPERATION_TIMEOUT);
        result = "Success";
        //updateTagWriteCount(true);
        //Log.e("ECRT", "### DONE Read= " + tag.getTagID());
        Log.d("ECRT", "\r\n");
      }
      catch(InvalidUsageException e){
        result = e.getLocalizedMessage();
        showLog("Write Failed:", result);
        AppCommonMethods.logInFile(context, sessionType.name(), "_STOP_ENCODING->failed->" + result);
        //result = "Failed";
        // tiempo = (System.currentTimeMillis() - tiempo);
        // iErrorExcetion++;
        //e.printStackTrace();
      }
      catch(OperationFailureException e1){
        result = e1.getVendorMessage();
        showLog("Write Failed:", result);
        AppCommonMethods.logInFile(context, sessionType.name(), "_STOP_ENCODING->failed->" + result);
        //tiempo = (System.currentTimeMillis() - tiempo);
        //result = "Failed";
        //iErrorExcetion++;
        //final String sQFE = e1.getVendorMessage();
        //showLog("ECRT", "QFE=" + sQFE + ",RSSI=" + tag.getPeakRSSI() + ",ID=" + tag.getTagID());
        //listener.updateUI(tag.getTagID() + ",OFE=" + e1.getVendorMessage() + ",ms=" + tiempo);
      }
      catch(Exception e){
        result = e.getMessage();
        showLog("Write Failed:", result);
        AppCommonMethods.logInFile(context, sessionType.name(), "_STOP_ENCODING->failed->" + result);
      }
      AppCommonMethods.showLog("result1", result);
      /////////////////////////////////////////////
      // Step 6: Write Results
      /////////////////////////////////////////////
      if(tagData != null && tagData.getOpStatus() != null){
        //int iWrittenWord = tagData.getNumberOfWords();
        showLog("Originaln EPC", result + " TagId " + epcdt);
        showLog("TAG WRITE RESULT", result + " Returned TagId " + tagData.getTagID());
        showLog("TAG WRITE RESULT", result + " OpStatus: " + tagData.getOpStatus());
        showLog("TAG WRITE RESULT", result + " numofwordswritten " + tagData.getNumberOfWords());
        
        showLog("TAG WRITE RESULT", "\r\n");
        
        final int iWritten = tagData.getNumberOfWords();
        
        if(iWritten == iTotalWriteLengthWORDEPC){
          return result;
          // iWriteOK++;
          // Log.d("ECRT", "Passed=" + iWriteOK);
          // listener.updateUI(tag.getTagID() + " " + tagData.getOpStatus().toString() + " " + tiempo + ", Written=" + iWritten);
        }
        else{
          // if(iWritten > 0) iWritePartial++;
          final String sError = tagData.getOpStatus().toString();
          AppCommonMethods.logInFile(context, sessionType.name(), "_STOP_ENCODING_failed->" + sError);
          
          showLog("sError", "NEW_EPC=" + pickTag.newEpc + ", WRITTEN=" + tagData.getTagID() + ", Error=" + sError + " ,Written=" + iWritten);
          
          if(sError.contains("ACCESS_TAG_PASSWORD_ERROR"))
            return context.getString(R.string.err_encoding_auth_fail);
          if(sError.contains("ACCESS_NO_RESPONSE_FROM_TAG"))
            return isRetryEncode ? "Re" + logTag + "e" + tagData.getTagID() : context.getString(R.string.err_encoding_tag_response_fail);
          if(sError.contains("ACCESS_INSUFFICIENT_POWER"))
            return isRetryEncode ? "Re" + logTag + "e" + tagData.getTagID() : context.getString(R.string.err_encoding_access_power_insufficient);
          if(sError.contains("ACCESS_TAG_MEMORY_OVERRUN_ERROR"))
            return context.getString(R.string.err_encoding_overrun_fail);
          if(sError.contains("ACCESS_TAG_CRC_ERROR"))
            return isRetryEncode ? "Re" + logTag + "e" + tagData.getTagID() : context.getString(R.string.err_encoding_crc_fail);
          if(sError.contains("ACCESS_TAG_MEMORY_LOCKED_ERROR"))
            return isRetryEncode ? "Re" + logTag + "e" + tagData.getTagID() : context.getString(R.string.err_encoding_fail_locked_memory);
          showLog("isRe" + logTag + "e", "" + isRetryEncode);
          if(iWritten > 1 && iWritten < iTotalWriteLengthWORDEPC && isRetryEncode){
            return "Re" + logTag + "e" + tagData.getTagID();
            /*pickTag.epc=tagData.getTagID();
            performEncoding(pickTag,currentPassword);
            isReEncode = false;*/
          }
          else
            //          if(sError.contains("ACCESS_NO_RESPONSE_FROM_TAG")){
            //            // iErrorNoResponse++;
            //            //Log.e("ECRT", "No_Res:" + "Written=" + tagData.getNumberOfWords() + ",RSSI=" + tag.getPeakRSSI() + ",ID=" + tag.getTagID());
            //          }
            //          else if(sError.contains("ACCESS_INSUFFICIENT_POWER")){
            //            // iErrorPower++;
            //          }
            //          else if(sError.contains("ACCESS_TAG_CRC_ERROR")){
            //            //iErrorCRC++;
            //          }
            //else iErrorOther++;
            return sError;
          //listener.updateUI(tag.getTagID() + ",err=" + sError + ",Written=" + iWritten);
        }
      }
      else if(tagData != null && isNonEmpty(tagData.getTagID()) && tagData.getTagID().matches(pickTag.newEpc)){
        return result;
      }
      else if(tagData != null && tagData.getNumberOfWords() == iTotalWriteLengthWORDEPC){
        return result;
      }
      AppCommonMethods.logInFile(context, sessionType.name(), "_STOP_" + logTag.toUpperCase() + "ING->failed");
      /*if(result.equalsIgnoreCase("Success")){
        return context.getString(R.string.err_encoding_default);
      }*/
      //err_encoding_default
      return result.equalsIgnoreCase("Success") && isRetryEncode ? "Re" + logTag + "e" : result.equalsIgnoreCase("Success") ? context.getString(sessionAction == AppCommonMethods.SessionAction.DECODE ? R.string.err_decoding_default : R.string.err_encoding_default) : result;
      
      // return context.getString(R.string.err_encoding_fail_result_unknown);
      //Log.e("ECRT", "NULL NULL, Failed, RETRY: Passed=" + iWriteOK);
      //iErrorTagDataNull++;
      //Log.e("ECRT", "APP Disable RETRY 3, Null=" + iErrorTagDataNull);
      //listener.updateUI(tag.getTagID() + ", err=NULL ERROR");
      //return "Tag Data Null";
      
    }
    else{
      return context.getString(R.string.err_reader_connection);
    }
  }
  
  public void writeCurrentPasswordInTagNew(Inventory pickedTag, final String tagCurrentPassword){
    AppCommonMethods.showLog("Write Password Operation", "START");
    showLog("ENCODING_ENCODE_PASSWORD_WRITE", "START" + " (" + tagCurrentPassword + "->" + SharedPrefManager.getCurrentAccessPassword() + ")");
    final String epc = pickedTag.newEpc;
    RFIDResults rfidResults = setPrefilter(epc, true);
    showLog("setPrefilter_Result1", "" + (rfidResults.ordinal == RFID_API_SUCCESS.ordinal));
    //addEpcBasedFilters(epc,epc.length()>24);
    
    final String actualCurrentPassword = SharedPrefManager.getCurrentAccessPassword();
    showLog("CMDTAGCURRENT", tagCurrentPassword);
    showLog("CMDACTUALCURRENT", actualCurrentPassword);
    showLog("CMDEPC", epc);
    if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
      TagAccess tagAccess = new TagAccess();
      final TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
      //final TagAccess.WriteSpecificFieldAccessParams writeSpecificFieldAccessParams = tagAccess.new WriteSpecificFieldAccessParams();
      try{
        writeAccessParams.setAccessPassword(Long.decode("0X" + tagCurrentPassword.trim()));
        //writeSpecificFieldAccessParams.setAccessPassword(Long.decode("0X" + tagCurrentPassword.trim()));
      }
      catch(NumberFormatException nfe){
        nfe.printStackTrace();
      }
      
      writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_RESERVED);
      writeAccessParams.setOffset(2);//AppCommonMethods.parseInt("2"));
      writeAccessParams.setWriteData(actualCurrentPassword.trim());
      //writeSpecificFieldAccessParams.setWriteData(actualCurrentPassword.trim());
      
      writeAccessParams.setWriteDataLength(2);
      writeAccessParams.setWriteRetries(3);
      //writeSpecificFieldAccessParams.setWriteDataLength(2);
      AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_START (" + tagCurrentPassword + "->" + actualCurrentPassword + ")");
      TagData tagData = new TagData();
      try{
        //AntennaInfo antennaInfo = new AntennaInfo();
        //antennaInfo.setAntennaID(reader.Config.Antennas.getAvailableAntennas());
        //reader.Actions.TagAccess.writeAccessPasswordWait(epc, writeSpecificFieldAccessParams, antennaInfo);
        if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
          //Current Logic
          //reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, tagData, epc.length() <= 24, false);//encPickedData, false, true);//, epc.length()<=24, false);
          reader.Actions.TagAccess.writeblock(null, writeAccessParams, null, tagData, true, WRITE_OPERATION_TIMEOUT * 2);
          //reader.Actions.TagAccess.writeAccessPasswordWait(epc,writeSpecificFieldAccessParams,null);
          //AppCommonMethods.logInFile(context,sessionType.name(),"WRPASSWORDCMD:SUCCESS:");
          //showLog("SUCCESS", "WRITE PASS IN TAG");
        }
      }
      catch(InvalidUsageException e){
        e.printStackTrace();
        //logInFile(context,"WRPASSWORDCMD:ERR:" + e.getVendorMessage());
        AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (" + e.getVendorMessage() + ")");
        showLog("WRITEPASSWORDERROR1", e.getVendorMessage());
      }
      catch(OperationFailureException e){
        final String msg = e.getVendorMessage();
        //logInFile(context,"WRPASSWORDCMD:ERR:" + e.getVendorMessage());
        showLog("WRITEPASSWORDERROR2", msg);
        AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (" + msg + ")");
        //LOCK_ACQUIRE_FAILURE in C1G2AccessOperation
        //access tag crc error
        //access no response from tag
        e.printStackTrace();
      }
      catch(Exception e){
        AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (" + tagCurrentPassword + ")_" + e.getMessage() + ")");
        e.printStackTrace();
      }
      if(tagData != null && tagData.getOpStatus() != null){
        showLog("TAG PASS WRITE RESULT", " OpStatus: " + tagData.getOpStatus());
        showLog("TAG PASS WRITE RESULT", " numofwordswritten " + tagData.getNumberOfWords());
        final int iWritten = tagData.getNumberOfWords();
        if(iWritten == 2){
          AppCommonMethods.logInFile(context, sessionType.name(), "WRPASSWORDCMD:SUCCESS:");
          showLog("SUCCESS", "WRITE PASS IN TAG");
          showLog("ENCODING_ENCODE_PASSWORD_WRITE", "SUCCESS");
          if(!actualCurrentPassword.equalsIgnoreCase(defaultTagZeroPassword) && tagCurrentPassword.equalsIgnoreCase(defaultTagZeroPassword))
            lockRfidTagNew(pickedTag, actualCurrentPassword);
        }
        else{
          // if(iWritten > 0) iWritePartial++;
          final String sError = tagData.getOpStatus().toString();
          showLog("ECRT", "ID=" + tagData.getTagID() + ", Error=" + sError + " ,Written=" + iWritten);
          showLog("ENCODING_ENCODE_PASSWORD_WRITE", "Failed->" + sError);
          AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (" + tagCurrentPassword + "->" + actualCurrentPassword + ")_" + "failed! " + sError + ")");
          //          if(sError.contains("ACCESS_NO_RESPONSE_FROM_TAG")){
          //            // iErrorNoResponse++;
          //            //Log.e("ECRT", "No_Res:" + "Written=" + tagData.getNumberOfWords() + ",RSSI=" + tag.getPeakRSSI() + ",ID=" + tag.getTagID());
          //          }
          //          else if(sError.contains("ACCESS_INSUFFICIENT_POWER")){
          //            // iErrorPower++;
          //          }
          //          else if(sError.contains("ACCESS_TAG_CRC_ERROR")){
          //            //iErrorCRC++;
          //          }
          //else iErrorOther++;
          //return sError;
          //listener.updateUI(tag.getTagID() + ",err=" + sError + ",Written=" + iWritten);
        }
      }
      else{
        showLog("ENCODING_ENCODE_PASSWORD_WRITE", "Failed");
        showLog("TAG PASS WRITE RESULT", "failed!");
        AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (" + tagCurrentPassword + "->" + actualCurrentPassword + ")_" + "failed!" + ")");
      }
    }
    else{
      showLog("ENCODING_ENCODE_PASSWORD_WRITE", "Failed->Reader Disconnected");
      AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (reader is null)");
      /*isEncodeOn.postValue(false);
      ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_auth_fail);
      setProgressMessage(false);*/
      //updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_auth_fail));
    }
  }
  
  public void lockRfidTagNew(final Inventory pickedTag, final String actualCurrentPassword){
    //Set the param values
    AppCommonMethods.showLog("Lock Password Operation", "START");
    showLog("ENCODING_ENCODE_PASSWORD_LOCK", "START");
    final String epc = pickedTag.newEpc;
    LOCK_DATA_FIELD lockDataField = LOCK_DATA_FIELD.LOCK_EPC_MEMORY;
    LOCK_PRIVILEGE lockPrivilege = LOCK_PRIVILEGE.LOCK_PRIVILEGE_READ_WRITE;
    LOCK_DATA_FIELD lockDataFieldReserved = LOCK_DATA_FIELD.LOCK_ACCESS_PASSWORD;
    TagAccess tagAccess = new TagAccess();
    showLog("lockId", lockPrivilege.toString());
    final TagAccess.LockAccessParams lockAccessParams = tagAccess.new LockAccessParams();
    if(lockDataField != null) lockAccessParams.setLockPrivilege(lockDataField, lockPrivilege);
    if(lockDataFieldReserved != null)
      lockAccessParams.setLockPrivilege(lockDataFieldReserved, lockPrivilege);
    try{
      lockAccessParams.setAccessPassword(Long.decode("0X" + actualCurrentPassword.trim()));
    }
    catch(NumberFormatException nfe){
      nfe.printStackTrace();
      
    }
    AppCommonMethods.logInFile(context, sessionType.name(), "_LOCK_TAG_START (" + actualCurrentPassword + ")");
    if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
      try{
        reader.Actions.TagAccess.lockWait(epc, lockAccessParams, null, true);
        AppCommonMethods.showLog("Lock Password Operation", "SUCCESS");
        showLog("ENCODING_ENCODE_PASSWORD_LOCK", "Success");
      }
      catch(InvalidUsageException e){
        AppCommonMethods.logInFile(context, sessionType.name(), "_LOCK_TAG_STOP (" + e.getVendorMessage() + ")");
        showLog("LCPASSWORDCMD:ERR:", e.getVendorMessage());
        showLog("ENCODING_ENCODE_PASSWORD_LOCK", "Failed->" + e.getVendorMessage());
        //logInFile(context,"LCPASSWORDCMD:ERR:" + e.getVendorMessage());
        e.printStackTrace();
      }
      catch(OperationFailureException e){
        AppCommonMethods.logInFile(context, sessionType.name(), "_LOCK_TAG_STOP (" + e.getVendorMessage() + ")");
        showLog("LCPASSWORDCMD:ERR:", e.getVendorMessage());
        showLog("ENCODING_ENCODE_PASSWORD_LOCK", "Failed->" + e.getVendorMessage());
        //logInFile(context,"LCPASSWORDCMD:ERR:" + e.getVendorMessage());
        e.printStackTrace();
      }
      catch(Exception e){
        AppCommonMethods.logInFile(context, sessionType.name(), "_LOCK_TAG_STOP (" + e.getMessage() + ")");
        showLog("LCPASSWORDCMD:ERR:", e.getMessage());
        showLog("ENCODING_ENCODE_PASSWORD_LOCK", "Failed->" + e.getMessage());
        e.printStackTrace();
      }
    }
    else{
      AppCommonMethods.logInFile(context, sessionType.name(), "_LOCK_TAG_STOP (reader is null)");
      showLog("ENCODING_ENCODE_PASSWORD_LOCK", "Failed->Reader Disconnected");
      /*isEncodeOn.postValue(false);
      ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_auth_fail);
      setProgressMessage(false);*/
      //updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_auth_fail));
    }
    AppCommonMethods.showLog("Lock Password Operation", "END");
    
  }
  
  /**
   * Start decoding.
   *
   * @param pickedTag the picked tag
   */
  public synchronized void startDecoding(final Inventory pickedTag, final String currentPassword){
    if(pickedTag == null) return;
    String offsetText = "1";
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
    showLog("OFFSET", offsetText);
    showLog("TAGIDLENGTH", "" + epc.length());
    //logInFile(context,"ENCODECOMMAND:\nCURRENTPASSWORD:" + currentpassword + "\nTAGID:" + epc + "\nSGTINTOWRITE:" + sgtin + "\nSGTINLENGTH:" + sgtin.length());
    TagAccess tagAccess = new TagAccess();
    final TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
    boolean allowtowrite = false;
    final boolean isMultiWrite = multiWriteListSize > 0;
    try{
      writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
      writeAccessParams.setOffset(AppCommonMethods.parseInt(offsetText));
      writeAccessParams.setWriteData(sgtin);
      //logInFile(context,"FINALDATATOWRITEINTAG : " + sgtin);
      switch(sgtin.length()){
        case 28:
        case 36:
        case 24:
        case 32:
          allowtowrite = true;
          int dataLen = sgtin.length() / 4;
          writeAccessParams.setWriteDataLength(dataLen);
          writeAccessParams.setOffset(dataLen % 2 == 0 ? 2 : 1);
          offsetText = "" + (dataLen % 2 == 0 ? 2 : 1);
          break;
        default:
          allowtowrite = false;
          break;
      }
      showLog("OFFSET_1", offsetText);
    }
    catch(Exception e){
      showLog("EXC", e.getMessage());
      //logInFile(context,"EXCEPTIONLENG:" + "CRASH : " + e.getMessage());
    }
    if(allowtowrite && reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
      /*try{
       reader.Config.setAccessOperationWaitTimeout(WRITE_OPERATION_TIMEOUT);
      }catch(Exception e){e.printStackTrace();}*/
      if(SharedPrefManager.getNonPasswordTids().contains(chkNull(pickedTag.tid, "").toUpperCase().substring(0, 8))){
        //TODO LOOP ENCODING
        final List<String> listPasswords = SharedPrefManager.getOldAccessPasswords();
        listPasswords.remove(SharedPrefManager.getCurrentAccessPassword());
        listPasswords.add(0, SharedPrefManager.getCurrentAccessPassword());
        listPasswords.remove(defaultTagZeroPassword);
        listPasswords.add(0, defaultTagZeroPassword);
        
        new AsyncTask<Void, Void, String>(){
          private String bResult = "false";
          
          //private Boolean isWriteSuccess = false;
          @Override
          protected String doInBackground(Void... voids){
            try{
              Boolean isWriteSuccess = false;
              for(String pass : listPasswords){
                showLog("pass", pass);
                try{
                  try{
                    writeAccessParams.setAccessPassword(Long.decode("0X" + pass.trim()));
                  }
                  catch(NumberFormatException nfe){
                    nfe.printStackTrace();
                  }
                  showLog("FINALWRITTENDATA", bytesToHex(writeAccessParams.getWriteData()));
                  //logInFile(context,"FINALWRITTENDATA : " + StringConstants.bytesToHex(writeAccessParams.getWriteData()));
                  TagData tagData = null;
                  if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
                    //Previous Logic before 'Impinj Monza M730' Chip (Now Commented)
                    //if( tagData!=null) reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, tagData, true, true);
                    //Current Logic
                    reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, tagData, epc.length() <= 24, false);
                    //logInFile(context,"ENCODECOMMAND:SUCCESS");
                    showLog("SUCCESS", "WRITE NEW EPC FOR DECODE");
                    bResult = "true";
                    isWriteSuccess = true;
                    //return bResult;
                    break;
                  }
                }
                catch(InvalidUsageException e){
                  showLog("ENCODEEXC", e.getVendorMessage());
                  //logInFile(context,"ENCODECOMMAND:ERROR1:" + e.getVendorMessage());
                  e.printStackTrace();
                }
                catch(OperationFailureException e){
                  //access tag memory locked error
                  showLog("ENCODEEXC2", e.getVendorMessage());
                  //bResult = handleCatch(e, true);
                  //logInFile(context,"ENCODECOMMAND:ERROR2:" + e.getVendorMessage());
                  final String msg = e.getVendorMessage();
                  //LOCK_ACQUIRE_FAILURE in C1G2AccessOperation
                  //access tag crc error
                  //access no response from tag
                  if(msg.equalsIgnoreCase("access insufficient power")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    bResult = "INSUFFICIENT POWER";
                  }
                  if(msg.equalsIgnoreCase("access tag crc error") || msg.equalsIgnoreCase("data(x)- Size Less than Allowed")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    
                    bResult = "CRC ERROR";
                    isWriteSuccess = null;
                    break;
                  }
                  if(msg.equalsIgnoreCase("LOCK_ACQUIRE_FAILURE in C1G2AccessOperation")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    
                    bResult = "LOCKERROR";
                  }
                  if(msg.equalsIgnoreCase("access tag memory locked error")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    
                    bResult = "MEMORYLOCK";
                  }
                  if(msg.equalsIgnoreCase("access no response from tag")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    
                    bResult = "TAGNORESPONSE";
                    isWriteSuccess = null;
                    break;
                  }
                  if(msg.equalsIgnoreCase("access tag memory overrun error")){// || msg.equalsIgnoreCase("data(x)- Size Less than Allowed")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    bResult = "OVERRUN";
                    isWriteSuccess = null;
                    break;
                  }
                  if(msg.equalsIgnoreCase("TIME OUT") || msg.equalsIgnoreCase("Response timeout")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());//RFID_ACCESS_TAG_WRITE_FAILED
                    bResult = "TIMEOUT";
                  }
                  e.printStackTrace();
                }
                catch(Exception e){
                  bResult = "TIMEOUT";
                  //logInFile(context,"ENCODECOMMAND:CRASH: " + e.getMessage());
                  e.printStackTrace();
                }
              }
              if(isWriteSuccess != null && !isWriteSuccess) bResult = "false";
            }
            catch(Exception ex){
              showLog("EXC", ex.getMessage());
              //logInFile(context,"CRASHEXCEPTION : " + "\nLOCAL" + ex.getLocalizedMessage() + "\nMSG" + ex.getMessage() + "\nCAUSE" + ex.getCause().toString() + "\nCAUSE" + ex.getStackTrace()[0]);
            }
            return bResult;
          }

          /*private String handleCatch(final OperationFailureException e, final boolean isRecheckOnTimeout){
            if(e instanceof OperationFailureException){
              final String msg = e.getVendorMessage();
              if(msg.equalsIgnoreCase("access insufficient power")){
                showLog("EXCEPTIONINTIMEOUT", "" + e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", "" + e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", "" + e.getStatusDescription());
                bResult="INSUFFICIENT POWER";
              }
              else if(msg.equalsIgnoreCase("access tag crc error") || msg.equalsIgnoreCase("data(x)- Size Less than Allowed")){
                showLog("EXCEPTIONINTIMEOUT", "" + e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", "" + e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", "" + e.getStatusDescription());

                bResult = "CRC ERROR";
                isWriteSuccess=null;
                break;
              }
              else if(msg.equalsIgnoreCase("LOCK_ACQUIRE_FAILURE in C1G2AccessOperation")){
                showLog("EXCEPTIONINTIMEOUT", "" + e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", "" + e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", "" + e.getStatusDescription());

                bResult = "LOCKERROR";
              }
              else if(msg.equalsIgnoreCase("access tag memory locked error")){
                showLog("EXCEPTIONINTIMEOUT", "" + e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", "" + e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", "" + e.getStatusDescription());

                bResult = "MEMORYLOCK";
              }
              else if(msg.equalsIgnoreCase("access no response from tag")){
                showLog("EXCEPTIONINTIMEOUT", "" + e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", "" + e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", "" + e.getStatusDescription());
                bResult = "TAGNORESPONSE";
                isWriteSuccess=null;
                break;
              }
              else if(msg.equalsIgnoreCase("access tag memory overrun error") ){
                showLog("EXCEPTIONINTIMEOUT", "" + e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", "" + e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", "" + e.getStatusDescription());
                bResult = "OVERRUN";
                isWriteSuccess=null;
                break;
              }
              else if(msg.equalsIgnoreCase("TIME OUT") || msg.equalsIgnoreCase("Response timeout")){
                showLog("EXCEPTIONINTIMEOUT", "" + e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", "" + e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", "" + e.getStatusDescription());//RFID_ACCESS_TAG_WRITE_FAILED
                showLog("isRecheckOnTimeout", "" + isRecheckOnTimeout);
                if(isRecheckOnTimeout && reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
                  try{
                    showLog("Recheck_SGTN", sgtin);
                    TagData tagData = null;
                    reader.Actions.TagAccess.writeWait(sgtin, writeAccessParams, null, tagData, true, true);//, tagValue.length()<=24, false);
                    //logInFile(context,"ENCODECOMMAND:SUCCESS");
                    showLog("SUCCESS", "WRITE NEW EPC FOR DECODE");
                    bResult = "true";
                  }catch(InvalidUsageException ex){

                    showLog("ENCODEEXC", "" + e.getVendorMessage());
                    //logInFile(context,"ENCODECOMMAND:ERROR1:" + e.getVendorMessage());
                    ex.printStackTrace();
                  }catch(OperationFailureException ex){
                    bResult = handleCatch(ex, false);
                  }catch(Exception ex){ex.printStackTrace();}
                }
                else bResult = "TIMEOUT";
              }
              else{
                bResult = "false";
              }
            }
            return bResult;
          }*/
          
          @Override
          protected void onPostExecute(String result){
            if(result != null){
              if(result.equalsIgnoreCase("false")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                updateTagWriteFailReason(pickedTag, "Tag Write Fail");
                updateTagWriteCount(context.getString(R.string.err_decoding_fail));
              }
              else if(result.equalsIgnoreCase("true")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                pickedTag.writeFailReason = null;
                pickedTag.isUploaded = false;
                pickedTag.newEpc = (/*sgtin.toUpperCase().trim().matches("^(3000|3400|4000|4400).*$") ? sgtin.replaceFirst("(3000|3400|4000|4400)", "") : */sgtin).toUpperCase().trim();
                //inventoryDao.updateInventoryData(pickedTag);
                try{
                  if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST ? inventoryDao.isEPCPresent(sessionId, pickedTag.epc) : inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
                    //if(inventoryDao.isTagPresent(sessionId,pickedTag.epc,pickedTag.newEpc,pickedTag.tid) || (inventoryDao.isEPCPresent(sessionId, pickedTag.epc) && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST)))
                    inventoryDao.updateInventoryData(pickedTag);
                  else{
                    inventoryDao.insertInventoryData(pickedTag);
                  }
                  uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
                }
                catch(Exception e){
                  e.printStackTrace();
                }
                AppCommonMethods.successBeep();
                try{
                  final ProductDao productDao = AppDatabase.getProductDao(context);
                  final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                  if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
                    productDao.updateDecodedEPC(pickedTag.epc, pickedTag.ean, pickedTag.zone);
                  else if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
                    fifoDao.updateDecoded(pickedTag.ean, pickedTag.epc, fifoDate);
                }
                catch(Exception e){
                  e.printStackTrace();
                }
                updateTagWriteCount(true);
              }
              else if(result.equalsIgnoreCase("MEMORYLOCK")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  //decoding failed
                  updateTagWriteFailReason(pickedTag, "Memory Lock");
                  updateTagWriteCount(context.getString(R.string.err_decoding_fail_locked_memory));
                }
                else{
                  //check if one of old password and current password are not identical.
                  //read password by other using other passwords and replace current password in tag and then lock. after lock success then write into tag with current password.
                  //read tag passwords
                  readTagCurrentPassword(pickedTag);
                  
                }
              }
              else if(result.equalsIgnoreCase("TIMEOUT")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  //decoding failed
                  updateTagWriteFailReason(pickedTag, result);
                  updateTagWriteCount(context.getString(R.string.err_decoding_fail));
                }
                else{
                  //check if one of old password and current password are not identical.
                  //read password by other using other passwords and replace current password in tag and then lock. after lock success then write into tag with current password.
                  //read tag passwords
                  readTagCurrentPassword(pickedTag);
                }
              }
              else if(result.equalsIgnoreCase("TAGNORESPONSE")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  performTidBasedPick(pickedTag, currentPassword);
                }
                else{
                  updateTagWriteFailReason(pickedTag, result);
                  updateTagWriteCount(context.getString(R.string.err_decoding_tag_response_fail));
                }
              }
              else if(result.equalsIgnoreCase("LOCKERROR")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                updateTagWriteFailReason(pickedTag, "Lock Error");
                updateTagWriteCount(context.getString(R.string.err_decoding_fail_lock_grant));
              }
              else if(result.equalsIgnoreCase("CRC ERROR")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  performTidBasedPick(pickedTag, currentPassword);
                }
                else{
                  updateTagWriteFailReason(pickedTag, result);
                  updateTagWriteCount(context.getString(R.string.err_decoding_crc_fail));
                }
              }
              else if(result.equalsIgnoreCase("OVERRUN")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  performTidBasedPick(pickedTag, currentPassword);
                }
                else{
                  updateTagWriteFailReason(pickedTag, result);
                  updateTagWriteCount(context.getString(R.string.err_decoding_crc_fail));
                }
              }
              else if(result.equalsIgnoreCase("INSUFFICIENT POWER")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                updateTagWriteFailReason(pickedTag, result);
                updateTagWriteCount(context.getString(R.string.err_decoding_access_power_insufficient));
              }
            }
            else{
              //logInFile(context,"ENCODECOMMAND:RESULTFAILURE:NULL");
              updateTagWriteFailReason(pickedTag, "Auth Failed");
              updateTagWriteCount(context.getString(R.string.err_decoding_fail));
            }
          }
          
        }.execute();
      }
      else{
        new AsyncTask<Void, Void, String>(){
          private InvalidUsageException invalidUsageException;
          private OperationFailureException operationFailureException;
          private Exception exception;
          private String bResult = "false";
          
          @Override
          protected String doInBackground(Void... voids){
            try{
              try{
                try{
                  writeAccessParams.setAccessPassword(Long.decode("0X" + currentPassword.trim()));
                }
                catch(NumberFormatException nfe){
                  nfe.printStackTrace();
                }
                showLog("FINALWRITTENDATA", bytesToHex(writeAccessParams.getWriteData()));
                //logInFile(context,"FINALWRITTENDATA : " + StringConstants.bytesToHex(writeAccessParams.getWriteData()));
                TagData tagData = null;
                if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
                  //Previous Logic before 'Impinj Monza M730' Chip (Now Commented)
                  //if( tagData!=null) reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, tagData, true, true);
                  //Current Logic
                  reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, tagData, epc.length() <= 24, false);
                  //logInFile(context,"ENCODECOMMAND:SUCCESS");
                  showLog("SUCCESS", "WRITE NEW EPC FOR DECODE");
                  bResult = "true";
                }
                
              }
              catch(InvalidUsageException e){
                
                showLog("ENCODEEXC", e.getVendorMessage());
                //logInFile(context,"ENCODECOMMAND:ERROR1:" + e.getVendorMessage());
                invalidUsageException = e;
                e.printStackTrace();
              }
              catch(OperationFailureException e){
                //access tag memory locked error
                showLog("ENCODEEXC2", e.getVendorMessage());
                bResult = handleCatch(e, true);
                //logInFile(context,"ENCODECOMMAND:ERROR2:" + e.getVendorMessage());
                e.printStackTrace();
              }
              catch(Exception e){
                
                exception = e;
                bResult = "TIMEOUT";
                //logInFile(context,"ENCODECOMMAND:CRASH: " + e.getMessage());
                e.printStackTrace();
              }
            }
            catch(Exception ex){
              showLog("EXC", ex.getMessage());
              //logInFile(context,"CRASHEXCEPTION : " + "\nLOCAL" + ex.getLocalizedMessage() + "\nMSG" + ex.getMessage() + "\nCAUSE" + ex.getCause().toString() + "\nCAUSE" + ex.getStackTrace()[0]);
            }
            return bResult;
            
          }
          
          private String handleCatch(final OperationFailureException e, final boolean isRecheckOnTimeout){
            if(e instanceof OperationFailureException){
              operationFailureException = e;
              final String msg = e.getVendorMessage();
              if(msg.equalsIgnoreCase("access insufficient power")){
                showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                bResult = "INSUFFICIENT POWER";
              }
              else if(msg.equalsIgnoreCase("access tag crc error") || msg.equalsIgnoreCase("data(x)- Size Less than Allowed")){
                showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                
                bResult = "CRC ERROR";
              }
              else if(msg.equalsIgnoreCase("LOCK_ACQUIRE_FAILURE in C1G2AccessOperation")){
                showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                
                bResult = "LOCKERROR";
              }
              else if(msg.equalsIgnoreCase("access tag memory locked error")){
                showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                
                bResult = "MEMORYLOCK";
              }
              else if(msg.equalsIgnoreCase("access no response from tag")){
                showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                bResult = "TAGNORESPONSE";
              }
              else if(msg.equalsIgnoreCase("TIME OUT") || msg.equalsIgnoreCase("Response timeout")){
                showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());//RFID_ACCESS_TAG_WRITE_FAILED
                showLog("isRecheckOnTimeout", "" + isRecheckOnTimeout);
                if(isRecheckOnTimeout && reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
                  try{
                    showLog("Recheck_SGTN", sgtin);
                    TagData tagData = null;
                    reader.Actions.TagAccess.writeWait(sgtin, writeAccessParams, null, tagData, true, true);//, tagValue.length()<=24, false);
                    //logInFile(context,"ENCODECOMMAND:SUCCESS");
                    showLog("SUCCESS", "WRITE NEW EPC FOR DECODE");
                    bResult = "true";
                  }
                  catch(InvalidUsageException ex){
                    
                    showLog("ENCODEEXC", e.getVendorMessage());
                    //logInFile(context,"ENCODECOMMAND:ERROR1:" + e.getVendorMessage());
                    invalidUsageException = ex;
                    ex.printStackTrace();
                  }
                  catch(OperationFailureException ex){
                    bResult = handleCatch(ex, false);
                  }
                  catch(Exception ex){
                    ex.printStackTrace();
                  }
                }
                else bResult = "TIMEOUT";
              }
              else{
                bResult = "false";
              }
            }
            return bResult;
          }
          
          @Override
          protected void onPostExecute(String result){
            if(result != null){
              if(result.equalsIgnoreCase("false")){
                if(invalidUsageException != null){
                  // rfidListeners.onFailure(invalidUsageException);
                }
                else if(operationFailureException != null){
                  // rfidListeners.onFailure(operationFailureException);
                }
                else{
                  // rfidListeners.onFailure(""+exception.getMessage());
                }
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                updateTagWriteFailReason(pickedTag, "Tag Write Fail");
                updateTagWriteCount(context.getString(R.string.err_decoding_fail));
              }
              else if(result.equalsIgnoreCase("true")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                pickedTag.isUploaded = false;
                pickedTag.writeFailReason = null;
                pickedTag.newEpc = (/*sgtin.toUpperCase().trim().matches("^(3000|3400|4000|4400).*$") ? sgtin.replaceFirst("(3000|3400|4000|4400)", "") : */sgtin).toUpperCase().trim();
                //inventoryDao.updateInventoryData(pickedTag);s
                try{
                  if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST ? inventoryDao.isEPCPresent(sessionId, pickedTag.epc) : inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
                    //if(inventoryDao.isTagPresent(sessionId,pickedTag.epc,pickedTag.newEpc,pickedTag.tid) || (inventoryDao.isEPCPresent(sessionId, pickedTag.epc) && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST)))
                    inventoryDao.updateInventoryData(pickedTag);
                  else inventoryDao.insertInventoryData(pickedTag);
                  uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
                }
                catch(Exception e){
                  e.printStackTrace();
                }
                AppCommonMethods.successBeep();
                try{
                  final ProductDao productDao = AppDatabase.getProductDao(context);
                  final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                  if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
                    productDao.updateDecodedEPC(pickedTag.epc, pickedTag.ean, pickedTag.zone);
                  else if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
                    fifoDao.updateDecoded(pickedTag.ean, pickedTag.epc, fifoDate);
                }
                catch(Exception e){
                  e.printStackTrace();
                }
                updateTagWriteCount(true);
              }
              else if(result.equalsIgnoreCase("MEMORYLOCK")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  //decoding failed
                  updateTagWriteFailReason(pickedTag, "Memory Lock");
                  updateTagWriteCount(context.getString(R.string.err_decoding_fail_locked_memory));
                }
                else{
                  //check if one of old password and current password are not identical.
                  //read password by other using other passwords and replace current password in tag and then lock. after lock success then write into tag with current password.
                  //read tag passwords
                  readTagCurrentPassword(pickedTag);
                  
                }
              }
              else if(result.equalsIgnoreCase("TIMEOUT")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  //decoding failed
                  updateTagWriteFailReason(pickedTag, result);
                  updateTagWriteCount(context.getString(R.string.err_decoding_fail));
                }
                else{
                  //check if one of old password and current password are not identical.
                  //read password by other using other passwords and replace current password in tag and then lock. after lock success then write into tag with current password.
                  //read tag passwords
                  readTagCurrentPassword(pickedTag);
                }
              }
              else if(result.equalsIgnoreCase("TAGNORESPONSE")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  performTidBasedPick(pickedTag, currentPassword);
                }
                else{
                  updateTagWriteFailReason(pickedTag, result);
                  updateTagWriteCount(context.getString(R.string.err_decoding_tag_response_fail));
                }
              }
              else if(result.equalsIgnoreCase("LOCKERROR")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                updateTagWriteFailReason(pickedTag, "Lock Error");
                updateTagWriteCount(context.getString(R.string.err_decoding_fail_lock_grant));
              }
              else if(result.equalsIgnoreCase("CRC ERROR")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  performTidBasedPick(pickedTag, currentPassword);
                }
                else{
                  updateTagWriteFailReason(pickedTag, result);
                  updateTagWriteCount(context.getString(R.string.err_decoding_crc_fail));
                }
              }
              else if(result.equalsIgnoreCase("OVERRUN")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  performTidBasedPick(pickedTag, currentPassword);
                }
                else{
                  updateTagWriteFailReason(pickedTag, result);
                  updateTagWriteCount(context.getString(R.string.err_decoding_crc_fail));
                }
              }
              else if(result.equalsIgnoreCase("INSUFFICIENT POWER")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                updateTagWriteFailReason(pickedTag, result);
                updateTagWriteCount(context.getString(R.string.err_decoding_access_power_insufficient));
              }
            }
            else{
              //logInFile(context,"ENCODECOMMAND:RESULTFAILURE:NULL");
              updateTagWriteFailReason(pickedTag, "Auth Fail");
              updateTagWriteCount(context.getString(R.string.err_decoding_auth_fail));
            }
          }
        }.execute();
      }
    }
    else{
      // fail
      updateTagWriteFailReason(pickedTag, "Invalid New EPC Length : " + sgtin.length());
      updateTagWriteCount(context.getString(R.string.err_decoding_write_fail));
      showLog("LOCKMEMORY1", "FAIL");
    }
  }
  
  @Override
  public synchronized void performEncoding(final List<Inventory> pickedTags, final AppCommonMethods.SessionType sessionType){
    showLog("pickedTags", "" + pickedTags.size());
    showLog("sessionType", sessionType.name());
    if(isNonEmpty(pickedTags) && sessionType.getValue() > 0){
      if(reader != null){
        ZebraRFIDHandler.this.sessionType = sessionType;
        sessionAction = AppCommonMethods.SessionAction.ENCODE;
        multiWriteSuccessCount = 0;
        multiWriteListSize = pickedTags.size();
        multiWriteCount = multiWriteListSize;
        isMultiWriteDone = false;
        try{
          reader.Actions.PreFilters.deleteAll();
          clearFilters();
          configAction(MAX_POWER_TO_SET, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
          //reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
          reader.Config.setAccessOperationWaitTimeout(WRITE_OPERATION_TIMEOUT);
        }
        catch(InvalidUsageException e){
          e.printStackTrace();
        }
        catch(OperationFailureException e){
          e.printStackTrace();
        }
        catch(Exception e){
          e.printStackTrace();
        }
        isEncodeOn.postValue(true);
        //setProgressMessage(context.getString(R.string.msg_pick), true);
        for(Inventory inventory : pickedTags){
          if(inventory != null && isNonEmpty(inventory.epc)){
            performEncoding(inventory, SharedPrefManager.getCurrentAccessPassword());
          }
          else updateTagWriteCount(false);
        }
        /*if(multiWriteCount == 0 && !isMultiWriteDone){
          isEncodeOn.postValue(false);
          setProgressMessage(false);
        }*/
      }
      else{
        isEncodeOn.postValue(false);
        context.showCustomErrDialog(R.string.err_encoding_fail);
        setProgressMessage(false);
        //fail
      }
    }
  }
  
  public boolean configureSessionAction(AppCommonMethods.SessionAction sessionAction){
    switch(sessionAction){
      case INVENTORY:
        break;
      case SEARCH:
        break;
      case PICK:
        break;
      case ENCODE:
        break;
      case DECODE:
        break;
      default:
        break;
    }
    return false;
  }

  /*public boolean setupEncoding(final Inventory pickedTag, final String currentTagPassword){
    boolean isInNonPasswordTIDs = false;
    final Inventory pickTag = context.epcEncoderDecoder.setPCDataBeforeEncoding(pickedTag);
    if(pickTag == null) return false;
    else if(chkNull(pickTag.tid, "").length() > 8){
      if(SharedPrefManager.getNon128BitTids().contains(pickTag.tid.toUpperCase().substring(0, 8)) && pickTag.newEpc.length() > 28){
        context.showCustomErrDialog(context.getString(R.string.err_encoding_non_std_not_allowed_for_non_128_bit_tids));
        return false;
      }
      isInNonPasswordTIDs = SharedPrefManager.getNonPasswordTids().contains(chkNull(pickTag.tid, "").toUpperCase().substring(0, 8));
    }
    if(reader != null){
      sessionAction = AppCommonMethods.SessionAction.ENCODE;
      isEncodeOn.postValue(true);
      isReEncode = true;
      isReEncodeForOldEPC = true;
      encodeOldEpc = pickTag.epc;
      setProgressMessage(context.getString(R.string.msg_pick), true);
      try{
        reader.Actions.PreFilters.deleteAll();
        clearFilters();
        configAction(MAX_POWER_TO_SET - 30, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
        //reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
        reader.Config.setAccessOperationWaitTimeout(WRITE_OPERATION_TIMEOUT);
      }
      catch(InvalidUsageException e){
        e.printStackTrace();
        return false;
      }
      catch(OperationFailureException e){
        e.printStackTrace();
        return false;
      }
      catch(Exception e){
        e.printStackTrace();
        return false;
      }

      AppCommonMethods.logInFile(context, sessionType.name() , "_PERFORM (" + pickTag.ean + "_" + pickTag.epc + "_" + pickTag.tid + "_" + pickTag.newEpc + "_" + currentTagPassword + "_" + SharedPrefManager.getCurrentAccessPassword() + ")");
      if(!isInNonPasswordTIDs && isNullOrEmpty(currentTagPassword) && (SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_FIRST, AppCommonMethods.isCheckEncPasswordFirst) || (SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, AppCommonMethods.isCheckEncPasswordBasedOnEPC) && (!pickTag.epc.matches("(?i)^(00|30|0[A-C]|7[A-B]|BC).*$") || pickTag.ean.matches("(?i)(" + AppConstants.UNKNOWN + "|" + AppConstants.NON_ENCODED + ")")))))
        readTagCurrentPassword(pickTag);
      else if(!isInNonPasswordTIDs && isNonEmpty(currentTagPassword) && !currentTagPassword.equalsIgnoreCase(SharedPrefManager.getCurrentAccessPassword()))
        writeCurrentPasswordInTag(pickTag, currentTagPassword);
      else startEncoding(pickTag, chkNull(currentTagPassword, ""));
    }
    else{
      isEncodeOn.postValue(false);
      ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
      setProgressMessage(false);
      return false;
      //fail
    }
  }*/
  
  private void updateTagWriteFailReason(final Inventory pickedTag, final String failReason){
    //if(sessionType== AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST){
    pickedTag.isUploaded = false;
    pickedTag.writeFailReason = failReason;//"Memory Lock";
    pickedTag.retryWriteCount = pickedTag.retryWriteCount + 1;
    pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
    if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST ? inventoryDao.isEPCPresent(sessionId, pickedTag.epc) : inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
      //if(inventoryDao.isEPCPresent(sessionId,pickedTag.epc))
      inventoryDao.updateInventoryData(pickedTag);
    else inventoryDao.insertInventoryData(pickedTag);
  }
  
  private void clearFilters(){
    if(reader == null || reader.Actions == null || reader.Actions.PreFilters == null || reader.Actions.PreFilters.length() <= 0)
      return;
    try{
      showLog("clearFilters0", "" + reader.Actions.PreFilters.length());
      reader.Actions.PreFilters.deleteAll();
      reader.Actions.purgeTags();
      showLog("clearFilters", "1");
      showLog("clearFilters1", "" + reader.Actions.PreFilters.length());
    }
    catch(Exception e){
      e.printStackTrace();
      showLog("clearFilters2", e.getMessage());
    }
  }
  
  /**
   * Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  //@Override
  public synchronized void performEncodingOld(final Inventory pickedTag, final String currentTagPassword){
    boolean isInNonPasswordTIDs = false;
    final Inventory pickTag = context.epcEncoderDecoder.setPCDataBeforeEncoding(pickedTag);
    if(pickTag == null) return;
    else if(chkNull(pickTag.tid, "").length() > 8){
      if(SharedPrefManager.getNon128BitTids().contains(pickTag.tid.toUpperCase().substring(0, 8)) && pickTag.newEpc.length() > 28){
        context.showCustomErrDialog(context.getString(R.string.err_encoding_non_std_not_allowed_for_non_128_bit_tids));
        return;
      }
      isInNonPasswordTIDs = SharedPrefManager.getNonPasswordTids().contains(chkNull(pickTag.tid, "").toUpperCase().substring(0, 8));
    }
    if(reader != null){
      /*sessionAction = AppCommonMethods.SessionAction.ENCODE;
      isEncodeOn.postValue(true);
      isReEncode = true;
      isReEncodeForOldEPC = true;
      encodeOldEpc = pickTag.epc;*/
      if(multiWriteListSize <= 0){
        sessionAction = AppCommonMethods.SessionAction.ENCODE;
        isEncodeOn.postValue(true);
        isRetryEncode = true;
        isReEncodeForOldEPC = true;
        encodeOldEpc = pickTag.epc;
        isMultiWriteDone = false;
        //setProgressMessage(context.getString(R.string.msg_pick), true);
        try{
          //reader.Actions.PreFilters.deleteAll();
          clearFilters();
          configAction(MAX_POWER_TO_SET /*- 30*/, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
          //reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
          reader.Config.setAccessOperationWaitTimeout(WRITE_OPERATION_TIMEOUT);
        }
        catch(InvalidUsageException e){
          e.printStackTrace();
        }
        catch(OperationFailureException e){
          e.printStackTrace();
        }
        catch(Exception e){
          e.printStackTrace();
        }
      }
      AppCommonMethods.logInFile(context, sessionType.name(), "_PERFORM (" + pickTag.ean + "_" + pickTag.epc + "_" + pickTag.tid + "_" + pickTag.newEpc + "_" + currentTagPassword + "_" + SharedPrefManager.getCurrentAccessPassword() + ")");
      if(!isInNonPasswordTIDs && isNullOrEmpty(currentTagPassword) && (SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_FIRST, AppCommonMethods.isCheckEncPasswordFirst) || (SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, AppCommonMethods.isCheckEncPasswordBasedOnEPC) && (!pickTag.epc.matches("(?i)^(00|30|0[A-C]|7[A-B]|BC).*$") || pickTag.ean.matches("(?i)(" + AppConstants.UNKNOWN + "|" + AppConstants.NON_ENCODED + ")")))))
        readTagCurrentPassword(pickTag);
      else if(!isInNonPasswordTIDs && isNonEmpty(currentTagPassword) && !currentTagPassword.equalsIgnoreCase(SharedPrefManager.getCurrentAccessPassword()))
        writeCurrentPasswordInTag(pickTag, currentTagPassword);
      else startEncoding(pickTag, chkNull(currentTagPassword, ""));
    }
    else{
      isEncodeOn.postValue(false);
      context.showCustomErrDialog(R.string.err_encoding_fail);
      setProgressMessage(false);
      //fail
    }
  }
  
  /**
   * Start encoding.
   *
   * @param pickedTag the picked tag
   */
  public synchronized void startEncoding(final Inventory pickedTag, final String currentTagPassword){
    //reflink:
    //https://techdocs.zebra.com/dcs/rfid/android/2-15/guide/tagwrite/
    //https://techdocs.zebra.com/dcs/rfid/android/2-15/apis/reference/com/zebra/rfid/api3/TagAccess.html
    if(pickedTag == null) return;
    String offsetText = "1";
    final String currentAccessPassword = SharedPrefManager.getCurrentAccessPassword();
    final String barcode = pickedTag.ean;
    final String epc = pickedTag.epc;
    final String pc = pickedTag.pcdata;
    final String sgtin = pickedTag.newEpc;//.toUpperCase().trim();
    final String tid = pickedTag.tid;
    showLog("CURRENT ACCESS PASSWORD", currentAccessPassword);
    showLog("CURRENT TAG PASSWORD", currentTagPassword);
    showLog("BARCODE", barcode);
    showLog("TAGID", epc);
    showLog("TID", tid);
    showLog("PCData", pc);
    showLog("WRITETAGID", sgtin);
    showLog("WRITETAGIDLENGTH", "" + sgtin.length());
    showLog("OFFSET", offsetText);
    showLog("TAGIDLENGTH", "" + epc.length());
    //logInFile(context,"ENCODECOMMAND:\nCURRENTPASSWORD:" + currentpassword + "\nTAGID:" + epc + "\nSGTINTOWRITE:" + sgtin + "\nSGTINLENGTH:" + sgtin.length());
    
    TagAccess tagAccess = new TagAccess();
    final TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
    //final TagAccess.WriteSpecificFieldAccessParams writeSpecificFieldAccessParams = tagAccess.new WriteSpecificFieldAccessParams();
    //final AccessFilter accessFilter = new AccessFilter();
    
    // Tag Pattern A
    /*int len = pickedTag.epc.getBytes().length;
    showLog("tidBytes",""+len);
    accessFilter.TagPatternA.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
    accessFilter.TagPatternA.setBitOffset(0);
    accessFilter.TagPatternA.setTagPatternBitCount(len * 8);
    accessFilter.TagPatternA.setTagMask(pickedTag.epc);
    accessFilter.TagPatternA.setTagMaskBitCount(len * 8);
    accessFilter.setAccessFilterMatchPattern(FILTER_MATCH_PATTERN.A);*/

    /*len = pickedTag.epc.getBytes().length;
    accessFilter.TagPatternB.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
    accessFilter.TagPatternB.setBitOffset(0);
    accessFilter.TagPatternB.setTagPatternBitCount(len * 8);
    accessFilter.TagPatternB.setTagMask(pickedTag.epc);
    accessFilter.TagPatternB.setTagMaskBitCount(len * 8);
    accessFilter.setAccessFilterMatchPattern(FILTER_MATCH_PATTERN.NOTA_AND_B);*/
    
    boolean allowtowrite = false;
    try{
      writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
      writeAccessParams.setOffset(AppCommonMethods.parseInt(offsetText));
      writeAccessParams.setWriteData(sgtin);
      writeAccessParams.setWriteRetries(3);
      //logInFile(context,"FINALDATATOWRITEINTAG : " + sgtin);
      switch(sgtin.length()){
        case 28:
        case 36:
        case 24:
        case 32:
          allowtowrite = true;
          int dataLen = sgtin.length() / 4;
          writeAccessParams.setWriteDataLength(dataLen);
          writeAccessParams.setOffset(dataLen % 2 == 0 ? 2 : 1);
          offsetText = "" + (dataLen % 2 == 0 ? 2 : 1);
          showLog("DATALEN_1", "" + dataLen);
          showLog("OFFSET_1", offsetText);
          break;
        default:
          allowtowrite = false;
          break;
      }
      
    }
    catch(Exception e){
      showLog("EXC", e.getMessage());
      //logInFile(context,"EXCEPTIONLENG:" + "CRASH : " + e.getMessage());
    }
    if(allowtowrite && reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
      AppCommonMethods.logInFile(context, sessionType.name(), "_START (" + pickedTag.ean + "_" + pickedTag.epc + "_" + pickedTag.tid + "_" + pickedTag.newEpc + "_" + currentTagPassword + "_" + currentAccessPassword + ")");
      /*try{
       reader.Config.setAccessOperationWaitTimeout(WRITE_OPERATION_TIMEOUT);
      }catch(Exception e){e.printStackTrace();}*/
      if(SharedPrefManager.getNonPasswordTids().contains(tid.toUpperCase().substring(0, 8))){
        //TODO LOOP ENCODING
        final List<String> listPasswords = SharedPrefManager.getOldAccessPasswords();
        listPasswords.remove(SharedPrefManager.getCurrentAccessPassword());
        listPasswords.add(0, SharedPrefManager.getCurrentAccessPassword());
        listPasswords.remove(defaultTagZeroPassword);
        listPasswords.add(0, defaultTagZeroPassword);
        
        new AsyncTask<Void, Void, String>(){
          private String bResult = "false";
          
          @Override
          protected String doInBackground(Void... voids){
            try{
              Boolean isWriteSuccess = false;
              for(String pass : listPasswords){
                showLog("pass", pass);
                try{
                  try{
                    writeAccessParams.setAccessPassword(Long.decode("0X" + pass.trim()));
                  }
                  catch(NumberFormatException nfe){
                    nfe.printStackTrace();
                  }
                  //showLog("FINALWRITTENDATA", "" + StringConstants.bytesToHex(writeAccessParams.getWriteData()));
                  //AppCommonMethods.logInFile(context,"FINALWRITTENDATA : " + StringConstants.bytesToHex(writeAccessParams.getWriteData()));
                  showLog("encPickedData", "" + (encPickedData != null));
                  showLog("epc", epc);
                  showLog("encPickedData.getTagID", encPickedData != null ? encPickedData.getTagID() : "");
                  TagData tagData = null;//encPickedData;
                  if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
                    //reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, encPickedData);//, false, true);//, true, true);
                    //Previous Logic before 'Impinj Monza M730' Chip (Now Commented)
                    //reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, tagData, true, true);
                    //Current Logic
                    reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, tagData, epc.length() <= 24, false);
                    //logInFile(context,"ENCODECOMMAND:SUCCESS");
                    showLog("SUCCESS", "WRITE NEW EPC IN TAG");
                    bResult = "true";
                    isWriteSuccess = true;
                    //return bResult;
                    break;
                  }
                }
                catch(InvalidUsageException e){
                  
                  showLog("ENCODEEXC", e.getVendorMessage());
                  e.printStackTrace();
                }
                catch(OperationFailureException e){
                  //access tag memory locked error
                  showLog("ENCODEEXC2", e.getVendorMessage());
                  //logInFile(context,"ENCODECOMMAND:ERROR2:" + e.getVendorMessage());
                  final String msg = e.getVendorMessage();
                  //LOCK_ACQUIRE_FAILURE in C1G2AccessOperation
                  //access tag crc error
                  //access no response from tag
                  if(msg.equalsIgnoreCase("access insufficient power")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    bResult = "INSUFFICIENT POWER";
                  }
                  if(msg.equalsIgnoreCase("access tag crc error") || msg.equalsIgnoreCase("data(x)- Size Less than Allowed")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    
                    bResult = "CRC ERROR";
                    isWriteSuccess = null;
                    break;
                  }
                  if(msg.equalsIgnoreCase("LOCK_ACQUIRE_FAILURE in C1G2AccessOperation")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    
                    bResult = "LOCKERROR";
                  }
                  if(msg.equalsIgnoreCase("access tag memory locked error")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    
                    bResult = "MEMORYLOCK";
                  }
                  if(msg.equalsIgnoreCase("access no response from tag")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    
                    bResult = "TAGNORESPONSE";
                    isWriteSuccess = null;
                    break;
                  }
                  if(msg.equalsIgnoreCase("access tag memory overrun error")){// || msg.equalsIgnoreCase("data(x)- Size Less than Allowed")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                    bResult = "OVERRUN";
                    isWriteSuccess = null;
                    break;
                  }
                  if(msg.equalsIgnoreCase("TIME OUT") || msg.equalsIgnoreCase("Response timeout")){
                    showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                    showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                    showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                    showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());//RFID_ACCESS_TAG_WRITE_FAILED
                    bResult = "TIMEOUT";
                  }
                  e.printStackTrace();
                }
                catch(Exception e){
                  bResult = "TIMEOUT";
                  e.printStackTrace();
                }
              }
              if(isWriteSuccess != null && !isWriteSuccess) bResult = "false";
            }
            catch(Exception ex){
              showLog("EXC", ex.getMessage());
              ex.printStackTrace();
            }
            return bResult;
          }
          
          @Override
          protected void onPostExecute(String result){
            if(result != null){
              AppCommonMethods.logInFile(context, sessionType.name(), "_STOP (" + result + ")");
              if(result.equalsIgnoreCase("false")){
                /*isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_fail));
                
              }
              else if(result.equalsIgnoreCase("true")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                try{
                  if(isNonEmpty(encodeOldEpc) && !pickedTag.epc.equalsIgnoreCase(encodeOldEpc))
                    pickedTag.epc = encodeOldEpc;
                  pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                  pickedTag.isUploaded = false;
                  pickedTag.writeFailReason = null;
                  if(pickedTag.newEpc.toUpperCase().trim().matches("^(" + pc + ").*$"))
                    pickedTag.newEpc = pickedTag.newEpc.replaceFirst(pc, "").toUpperCase().trim();
                  if(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING) && isNonEmpty(fifoDate))
                    pickedTag.fifoDate = fifoDate;
                  try{
                    if(SharedPrefManager.getBoolean(ParamConstants.IS_TID_BASED_COUNT_FOR_ENCODE) && inventoryDao.isTidPresent(sessionId, pickedTag.tid)){
                      pickedTag.ino = chkNull(pickedTag.ino, chkNull(inventoryDao.getRowIdFromTid(sessionId, tid), 0));
                      inventoryDao.updateInventoryData(pickedTag);
                    }
                    else{
                      if(inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
                        inventoryDao.updateInventoryData(pickedTag);
                      else inventoryDao.insertInventoryData(pickedTag);
                    }
                    uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
                  }
                  catch(Exception e){
                    e.printStackTrace();
                  }
                  //isEncodeDone.postValue(true);
                  AppCommonMethods.successBeep();
                  updateTagWriteCount(true);
                  /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                      isEncodeOn.postValue(false);
                      setProgressMessage(false);
                    }
                  }, 50);*/
                }
                catch(SQLiteConstraintException sql){/*Don't handle*/
                }
                catch(Exception e){
                  e.printStackTrace();
                }
              }
              else if(result.equalsIgnoreCase("MEMORYLOCK")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                
                if(currentAccessPassword.equalsIgnoreCase(defaultTagZeroPassword)){
                  //encoding failed
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail_locked_memory);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_fail_locked_memory));
                }
                else{
                  //check if one of old password and current password are not identical.
                  //read password by other using other passwords and replace current password in tag and then lock. after lock success then write into tag with current password.
                  //read tag passwords
                  if(false) readTagCurrentPassword(pickedTag);
                  else{
                    /*isEncodeOn.postValue(false);
                    ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail_locked_memory);
                    setProgressMessage(false);*/
                    updateTagWriteCount(context.getString(R.string.err_encoding_fail_locked_memory));
                  }
                  
                }
              }
              else if(result.equalsIgnoreCase("TIMEOUT")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(currentAccessPassword.equalsIgnoreCase(defaultTagZeroPassword)){
                  //encoding failed
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_fail));
                }
                else{
                  //check if one of old password and current password are not identical.
                  //read password by other using other passwords and replace current password in tag and then lock. after lock success then write into tag with current password.
                  //read tag passwords
                  if(false) readTagCurrentPassword(pickedTag);
                  else{
                   /* isEncodeOn.postValue(false);
                    ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
                    setProgressMessage(false);*/
                    updateTagWriteCount(context.getString(R.string.err_encoding_fail));
                  }
                }
              }
              /*else if(result.equalsIgnoreCase("OVERRUN")){

                isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_overrun_fail);
                setProgressMessage(false);

              }*/
              else if(result.equalsIgnoreCase("TAGNORESPONSE")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){//isNullOrEmpty(currentTagPassword)){//false){//true){
                  performTidBasedPick(pickedTag, currentTagPassword);
                }
                else{
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_tag_response_fail);//err_encoding_tag_response_fail);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_tag_response_fail));
                }
              }
              else if(result.equalsIgnoreCase("LOCKERROR")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                /*isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail_lock_grant);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_fail_lock_grant));
              }
              else if(result.equalsIgnoreCase("CRC ERROR")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  performTidBasedPick(pickedTag, currentTagPassword);
                }
                else{
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_crc_fail);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_crc_fail));
                }
              }
              else if(result.equalsIgnoreCase("OVERRUN")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  performTidBasedPick(pickedTag, currentTagPassword);
                }
                else{
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_crc_fail);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_crc_fail));
                }
              }
              else if(result.equalsIgnoreCase("INSUFFICIENT POWER")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                /*isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_access_power_insufficient);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_access_power_insufficient));
              }
            }
            else{
              //logInFile(context,"ENCODECOMMAND:RESULTFAILURE:NULL");
              /*AppCommonMethods.logInFile(context, sessionType.name() , "_STOP (result is null)");
              isEncodeOn.postValue(false);
              ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
              setProgressMessage(false);*/
              updateTagWriteCount(context.getString(R.string.err_encoding_fail));
            }
          }
        }.execute();
      }
      else{
        new AsyncTask<Void, Void, String>(){
          private InvalidUsageException invalidUsageException;
          private OperationFailureException operationFailureException;
          private Exception exception;
          private String bResult = "false";
          
          @Override
          protected String doInBackground(Void... voids){
            try{
              try{
                try{
                  writeAccessParams.setAccessPassword(Long.decode("0X" + currentAccessPassword.trim()));
                }
                catch(NumberFormatException nfe){
                  nfe.printStackTrace();
                }
                //showLog("FINALWRITTENDATA", "" + StringConstants.bytesToHex(writeAccessParams.getWriteData()));
                //AppCommonMethods.logInFile(context,"FINALWRITTENDATA : " + StringConstants.bytesToHex(writeAccessParams.getWriteData()));
                showLog("encPickedData", "" + (encPickedData != null));
                showLog("epc", epc);
                showLog("encPickedData.getTagID", encPickedData != null ? encPickedData.getTagID() : "");
                TagData tagData = null;
                //TagData tagData = encPickedData;
                if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
                  //reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, encPickedData);//, false, true);//, true, true);
                  //Previous Logic before 'Impinj Monza M730' Chip (Now Commented)
                  //reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, tagData, true, true);
                  //Current Logic
                  reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, tagData, epc.length() <= 24, false);
                  //logInFile(context,"ENCODECOMMAND:SUCCESS");
                  showLog("SUCCESS", "WRITE NEW EPC IN TAG");
                  bResult = "true";
                }
                
              }
              catch(InvalidUsageException e){
                
                showLog("ENCODEEXC", e.getVendorMessage());
                //logInFile(context,"ENCODECOMMAND:ERROR1:" + e.getVendorMessage());
                invalidUsageException = e;
                e.printStackTrace();
              }
              catch(OperationFailureException e){
                //access tag memory locked error
                operationFailureException = e;
                showLog("ENCODEEXC2", e.getVendorMessage());
                //logInFile(context,"ENCODECOMMAND:ERROR2:" + e.getVendorMessage());
                final String msg = e.getVendorMessage();
                //LOCK_ACQUIRE_FAILURE in C1G2AccessOperation
                //access tag crc error
                //access no response from tag
                if(msg.equalsIgnoreCase("access insufficient power")){
                  showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                  showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                  showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                  showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                  bResult = "INSUFFICIENT POWER";
                }
                else if(msg.equalsIgnoreCase("access tag crc error") || msg.equalsIgnoreCase("data(x)- Size Less than Allowed")){
                  showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                  showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                  showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                  showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                  
                  bResult = "CRC ERROR";
                }
                if(msg.equalsIgnoreCase("LOCK_ACQUIRE_FAILURE in C1G2AccessOperation")){
                  showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                  showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                  showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                  showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                  
                  bResult = "LOCKERROR";
                }
                if(msg.equalsIgnoreCase("access tag memory locked error")){
                  showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                  showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                  showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                  showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                  
                  bResult = "MEMORYLOCK";
                }
                if(msg.equalsIgnoreCase("access no response from tag")){
                  showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                  showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                  showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                  showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                  
                  bResult = "TAGNORESPONSE";
                }
                if(msg.equalsIgnoreCase("access tag memory overrun error")){// || msg.equalsIgnoreCase("data(x)- Size Less than Allowed")){
                  showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                  showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                  showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                  showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
                  bResult = "OVERRUN";
                }
                if(msg.equalsIgnoreCase("TIME OUT") || msg.equalsIgnoreCase("Response timeout")){
                  showLog("EXCEPTIONINTIMEOUT", e.getMessage());
                  showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
                  showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
                  showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());//RFID_ACCESS_TAG_WRITE_FAILED
                  bResult = "TIMEOUT";
                }
              /*else{
                operationFailureException = e;
              }*/
                
                e.printStackTrace();
              }
              catch(Exception e){
                
                exception = e;
                bResult = "TIMEOUT";
                //logInFile(context,"ENCODECOMMAND:CRASH: " + e.getMessage());
                e.printStackTrace();
              }
            }
            catch(Exception ex){
              showLog("EXC", ex.getMessage());
              //logInFile(context,"CRASHEXCEPTION : " + "\nLOCAL" + ex.getLocalizedMessage() + "\nMSG" + ex.getMessage() + "\nCAUSE" + ex.getCause().toString() + "\nCAUSE" + ex.getStackTrace()[0]);
              ex.printStackTrace();
            }
            return bResult;
            
          }
          
          @Override
          protected void onPostExecute(String result){
            if(result != null){
              AppCommonMethods.logInFile(context, sessionType.name(), "_STOP (" + result + ")");
              if(result.equalsIgnoreCase("false")){
                if(invalidUsageException != null){
                  // rfidListeners.onFailure(invalidUsageException);
                }
                else if(operationFailureException != null){
                  // rfidListeners.onFailure(operationFailureException);
                }
                else{
                  // rfidListeners.onFailure(""+exception.getMessage());
                }
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                /*isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_fail));
              }
              else if(result.equalsIgnoreCase("true")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                try{
                  if(isNonEmpty(encodeOldEpc) && !pickedTag.epc.equalsIgnoreCase(encodeOldEpc))
                    pickedTag.epc = encodeOldEpc;
                  pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                  pickedTag.writeFailReason = null;
                  pickedTag.isUploaded = false;
                  try{
                    if(pickedTag.newEpc.toUpperCase().trim().matches("^(" + pc + ").*$"))
                      pickedTag.newEpc = pickedTag.newEpc.replaceFirst(pc, "").toUpperCase().trim();
                    if(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING) && isNonEmpty(fifoDate))
                      pickedTag.fifoDate = fifoDate;
                    if(SharedPrefManager.getBoolean(ParamConstants.IS_TID_BASED_COUNT_FOR_ENCODE) && inventoryDao.isTidPresent(sessionId, pickedTag.tid)){
                      pickedTag.ino = chkNull(pickedTag.ino, chkNull(inventoryDao.getRowIdFromTid(sessionId, tid), 0));
                      inventoryDao.updateInventoryData(pickedTag);
                    }
                    else{
                      if(inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
                        inventoryDao.updateInventoryData(pickedTag);
                      else inventoryDao.insertInventoryData(pickedTag);
                    }
                    uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
                  }
                  catch(Exception e){
                    e.printStackTrace();
                  }
                  //isEncodeDone.postValue(true);
                  AppCommonMethods.successBeep();
                  updateTagWriteCount(true);
                  /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                    @Override
                    public void run(){
                      isEncodeOn.postValue(false);
                      setProgressMessage(false);
                    }
                  }, 50);*/
                }
                catch(SQLiteConstraintException sql){/*Don't handle*/
                }
                catch(Exception e){
                  e.printStackTrace();
                }
              }
              else if(result.equalsIgnoreCase("MEMORYLOCK")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                
                if(currentAccessPassword.equalsIgnoreCase(defaultTagZeroPassword)){
                  //encoding failed
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail_locked_memory);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_fail_locked_memory));
                }
                else{
                  //check if one of old password and current password are not identical.
                  //read password by other using other passwords and replace current password in tag and then lock. after lock success then write into tag with current password.
                  //read tag passwords
                  if(isNullOrEmpty(currentTagPassword)) readTagCurrentPassword(pickedTag);
                  else{
                    /*isEncodeOn.postValue(false);
                    ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail_locked_memory);
                    setProgressMessage(false);*/
                    updateTagWriteCount(context.getString(R.string.err_encoding_fail_locked_memory));
                  }
                  
                }
              }
              else if(result.equalsIgnoreCase("TIMEOUT")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(currentAccessPassword.equalsIgnoreCase(defaultTagZeroPassword)){
                  //encoding failed
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_fail));
                }
                else{
                  //check if one of old password and current password are not identical.
                  //read password by other using other passwords and replace current password in tag and then lock. after lock success then write into tag with current password.
                  //read tag passwords
                  if(isNullOrEmpty(currentTagPassword)) readTagCurrentPassword(pickedTag);
                  else{
                    /*isEncodeOn.postValue(false);
                    ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
                    setProgressMessage(false);*/
                    updateTagWriteCount(context.getString(R.string.err_encoding_fail));
                  }
                }
              }
              /*else if(result.equalsIgnoreCase("OVERRUN")){

                isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_overrun_fail);
                setProgressMessage(false);

              }*/
              else if(result.equalsIgnoreCase("TAGNORESPONSE")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){//isNullOrEmpty(currentTagPassword)){//false){//true){
                  performTidBasedPick(pickedTag, currentTagPassword);
                }
                else{
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_tag_response_fail);//err_encoding_tag_response_fail);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_tag_response_fail));
                }
              }
              else if(result.equalsIgnoreCase("LOCKERROR")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                /*isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail_lock_grant);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_fail_lock_grant));
              }
              else if(result.equalsIgnoreCase("CRC ERROR")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  performTidBasedPick(pickedTag, currentTagPassword);
                }
                else{
                 /* isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_crc_fail);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_crc_fail));
                }
              }
              else if(result.equalsIgnoreCase("OVERRUN")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  performTidBasedPick(pickedTag, currentTagPassword);
                }
                else{
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_crc_fail);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_crc_fail));
                }
              }
              else if(result.equalsIgnoreCase("INSUFFICIENT POWER")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                /*isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_access_power_insufficient);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_access_power_insufficient));
              }
            }
            else{
              //logInFile(context,"ENCODECOMMAND:RESULTFAILURE:NULL");
              AppCommonMethods.logInFile(context, sessionType.name(), "_STOP (result is null)");
              /*isEncodeOn.postValue(false);
              ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
              setProgressMessage(false);*/
              updateTagWriteCount(context.getString(R.string.err_encoding_fail));
            }
          }
        }.execute();
      }
    }
    else{
      AppCommonMethods.logInFile(context, sessionType.name(), "_STOP (allowtowrite is false)");
      /*isEncodeOn.postValue(false);
      ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
      setProgressMessage(false);*/
      // fail
      updateTagWriteCount(context.getString(R.string.err_encoding_write_fail));
      showLog("LOCKMEMORY1", "FAIL");
    }
  }
  
  public void performTidBasedPick(final Inventory pickData, final String currentTagPassword){
    if(reader == null || !reader.isConnected() || reader.Actions == null || reader.Actions.TagAccess == null)
      return;
    try{
      reader.Actions.PreFilters.deleteAll();
      clearFilters();
      //configAction(MAX_POWER_TO_SET, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
      //reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
      TagAccess tagAccess = new TagAccess();
      TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
      //Set the param values
      readAccessParams.setCount(0);
      readAccessParams.setOffset(0);
      readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
      isActionPick = true;
      isTidBasedPick = true;
      loopFlag = true;
      pickTags.clear();
      pickUHFTags.clear();
      encPickedTag = pickData;
      readTid = true;
      final String barcode = pickData.ean;
      final String epc = pickData.epc;
      final String pc = pickData.pcdata;
      final String sgtin = (isNonEmpty(pickData.newEpc) && pickData.newEpc.startsWith("0") ? pickData.newEpc : !epc.startsWith("0") ? "0" + epc.substring(1) : epc).toUpperCase().trim();
      final String tid = pickData.tid;
      showLog("TID_PICK_BARCODE", barcode);
      showLog("TID_PICK_TID", tid);
      showLog("TID_PICK_EPC", epc);
      showLog("TID_PICK_PCData", pc);
      showLog("TID_PICK_WRITE_EPC", sgtin);
      showLog("TID_PICK_WRITE_EPC_LEN", "" + sgtin.length());
      //showLog("OFFSET", offsetText);
      showLog("TID_PICK_EPC_LEN", "" + epc.length());
      
      AppCommonMethods.logInFile(context, sessionType.name(), "_TID_PICK_START");
      //Read command with readAccessParams and accessFilter as null to read all the tags
      reader.Actions.TagAccess.readEvent(readAccessParams, null, null);
      showLog("SINGLEPICK", "" + isSinglePick);
      if(isSinglePick){
        //setProgressMessage(context.getString(R.string.msg_pick), true);
        pickTidBasedCountDownTimer = new CountDownTimer(7000, 1000){
          @Override
          public void onTick(long l){
            showLog("pick " + "onTick", "" + l);
          }
          
          @Override
          public void onFinish(){
            //tidBasedPickFinish(pickData,currentTagPassword,sgtin,epc);
            showLog("INTIMER", "FINISH");
            showLog("onFinish", "onFinish");
            stopInventory();
            encPickedTag = null;
            if(isNullOrEmpty(pickUHFTags)){
              //encoding failed
              AppCommonMethods.logInFile(context, sessionType.name(), "_TID_PICK_STOP (Tag Not Found)");
              context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
              setProgressMessage(false);
            }
            else if(pickTags.size() > 1){
              setProgressMessage(false);
              AppCommonMethods.logInFile(context, sessionType.name(), "_TID_PICK_STOP (Multiple Tags Found)");
              context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
            }
            else if(pickTags.size() == 1){
              final TagData tagData = new ArrayList<TagData>(pickUHFTags).get(0);
              encPickedData = sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN ? tagData : null;
              final String epcdt = tagData.getTagID();
              if((sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN) && inventoryDao.isEPCPresent(sessionId, epcdt)){
                AppCommonMethods.logInFile(context, sessionType.name(), "_TID_PICK_STOP (Tag Already Picked)");
                context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
                setProgressMessage(false);
              }
              else{
                AppCommonMethods.logInFile(context, sessionType.name(), "_TID_PICK_RESULT (" + tagData.getTagID() + "_" + pickData.ean + "_" + pickData.epc + "_" + pickData.tid + "_" + pickData.newEpc + "_" + currentTagPassword + "_" + SharedPrefManager.getCurrentAccessPassword() + ")");
                showLog("PICK", "1:SESSION : " + sessionType.name());
                readTid = true;
                readRssi = true;
                readEAN = true;
                readPC = true;
                final Inventory pickedTagData = getDataFromTagInfo(tagData);
                if(isNonEmpty(pickData.ean) && !pickedTagData.ean.equalsIgnoreCase(pickData.ean))
                  pickedTagData.ean = pickData.ean;
                if(pickedTagData == null || isNullOrEmpty(pickedTagData.epc) || isNullOrEmpty(pickedTagData.tid)){
                  if(sessionAction == AppCommonMethods.SessionAction.DECODE){
                    updateTagWriteCount(String.format(context.getString(R.string.err_pick_fail_read_tag), getTypeCharCode()));
                  }
                  else if(sessionAction == AppCommonMethods.SessionAction.ENCODE){
                    /*isEncodeOn.postValue(false);
                    AppCommonMethods.logInFile(context, sessionType.name() , "_TID_PICK_STOP (Tag Already Picked)");
                    context.showCustomErrDialog(R.string.err_pick_fail_read_tag);
                    setProgressMessage(false);*/
                    updateTagWriteCount(String.format(context.getString(R.string.err_pick_fail_read_tag), getTypeCharCode()));
                  }
                }
                else{
                  
                  //showLog("EAN", pickedTagData.ean + " " + pickData.ean);
                  showLog("EPC", pickedTagData.epc + " " + pickData.newEpc + " " + pickData.epc);
                  if(isNullOrEmpty(pickedTagData.epc) || (sessionAction == AppCommonMethods.SessionAction.ENCODE && isNullOrEmpty(pickData.newEpc))){
                    if(sessionAction == AppCommonMethods.SessionAction.DECODE){
                      updateTagWriteCount(String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()));
                    }
                    else if(sessionAction == AppCommonMethods.SessionAction.ENCODE){
                      AppCommonMethods.logInFile(context, sessionType.name(), "_TID_PICK_STOP (Tag Not Found)");
                      updateTagWriteCount(String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()));
                    }
                  }
                  else if((sessionAction == AppCommonMethods.SessionAction.DECODE && pickedTagData.epc.startsWith("0") && pickedTagData.epc.equalsIgnoreCase(sgtin)) || pickedTagData.epc.equalsIgnoreCase(pickData.newEpc)){
                    try{
                      if(sessionAction == AppCommonMethods.SessionAction.DECODE && pickedTagData.epc.startsWith("0") && pickedTagData.epc.equalsIgnoreCase(sgtin)){
                        pickData.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                        pickData.isUploaded = false;
                        pickData.writeFailReason = null;
                        pickData.newEpc = (/*sgtin.toUpperCase().trim().matches("^(3000|3400|4000|4400).*$") ? sgtin.replaceFirst("(3000|3400|4000|4400)", "") : */sgtin).toUpperCase().trim();
                        if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST ? inventoryDao.isEPCPresent(sessionId, pickData.epc) : inventoryDao.isTagPresent(sessionId, pickData.epc, pickData.newEpc, pickData.tid))
                          //if(inventoryDao.isTagPresent(sessionId, pickData.epc,pickData.newEpc,pickData.tid) || (inventoryDao.isEPCPresent(sessionId,pickData.epc) && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST)))
                          inventoryDao.updateInventoryData(pickData);
                        else inventoryDao.insertInventoryData(pickData);
                        try{
                          uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickData, extras));
                        }
                        catch(Exception e){
                          e.printStackTrace();
                        }
                        AppCommonMethods.successBeep();
                        try{
                          final ProductDao productDao = AppDatabase.getProductDao(context);
                          final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                          if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
                            productDao.updateDecodedEPC(pickData.epc, pickData.ean, pickData.zone);
                          else if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
                            fifoDao.updateDecoded(pickData.ean, pickData.epc, fifoDate);
                        }
                        catch(Exception e){
                          e.printStackTrace();
                        }
                        updateTagWriteCount(true);
                      }
                      else if(sessionAction == AppCommonMethods.SessionAction.ENCODE){//Encoding Success Call Upload
                        if(isNonEmpty(encodeOldEpc) && !pickData.epc.equalsIgnoreCase(encodeOldEpc))
                          pickData.epc = encodeOldEpc;
                        pickData.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                        pickData.isUploaded = false;
                        pickData.writeFailReason = null;
                        try{
                          if(pickData.newEpc.toUpperCase().trim().matches("^(" + pc + ").*$"))
                            pickData.newEpc = pickData.newEpc.replaceFirst(pc, "").toUpperCase().trim();
                          if(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING) && isNonEmpty(fifoDate))
                            pickData.fifoDate = fifoDate;
                          if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST ? inventoryDao.isEPCPresent(sessionId, pickData.epc) : inventoryDao.isTagPresent(sessionId, pickData.epc, pickData.newEpc, pickData.tid))
                            //if(inventoryDao.isTagPresent(sessionId, pickData.epc,pickData.newEpc,pickData.tid) || (inventoryDao.isEPCPresent(sessionId,pickData.epc) && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST)))
                            inventoryDao.updateInventoryData(pickData);
                          else inventoryDao.insertInventoryData(pickData);
                          uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickData, extras));
                        }
                        catch(Exception e){
                          e.printStackTrace();
                        }
                        //isEncodeDone.postValue(true);
                        AppCommonMethods.logInFile(context, sessionType.name(), "_TID_PICK_STOP (Encode Success)");
                        AppCommonMethods.successBeep();
                        updateTagWriteCount(true);
                        /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                          @Override
                          public void run(){
                            isEncodeOn.postValue(false);
                            setProgressMessage(false);
                          }
                        }, 50);*/
                      }
                    }
                    catch(SQLiteConstraintException sql){/*Don't handle*/
                    }
                    catch(Exception e){
                      e.printStackTrace();
                    }
                  }
                  else if((sessionAction == AppCommonMethods.SessionAction.DECODE && (!pickedTagData.epc.startsWith("0") || pickedTagData.epc.equalsIgnoreCase(epc))) || pickedTagData.epc.equalsIgnoreCase(encodeOldEpc)){
                    if(sessionAction == AppCommonMethods.SessionAction.DECODE){
                      updateTagWriteCount(context.getString(R.string.err_decoding_fail));
                    }
                    else if(sessionAction == AppCommonMethods.SessionAction.ENCODE){
                      if(isReEncodeForOldEPC){
                        pickedTagData.ean = pickData.ean;
                        pickedTagData.newEpc = pickData.newEpc;
                        AppCommonMethods.logInFile(context, sessionType.name(), "_TID_PICK_STOP (Old EPC Found->Re-Encode)");
                        isReEncodeForOldEPC = false;
                        startEncoding(pickedTagData, currentTagPassword);
                      }
                      else{
                        //Encoding Failed
                        /*isEncodeOn.postValue(false);
                        AppCommonMethods.logInFile(context, sessionType.name() , "_TID_PICK_STOP (Encode Failed->Old EPC Found)");
                        ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
                        setProgressMessage(false);*/
                        updateTagWriteCount(context.getString(R.string.err_encoding_fail));
                      }
                    }
                  }
                  else{
                    //Re-Write with new EPC)
                    if(isRetryEncode && sessionAction == AppCommonMethods.SessionAction.ENCODE){
                      pickedTagData.ean = pickData.ean;
                      pickedTagData.newEpc = pickData.newEpc;
                      AppCommonMethods.logInFile(context, sessionType.name(), "_TID_PICK_STOP (Re-Encode)");
                      startEncoding(pickedTagData, currentTagPassword);
                    }
                    else{
                      if(sessionAction == AppCommonMethods.SessionAction.DECODE){
                        updateTagWriteCount(context.getString(R.string.err_decoding_fail));
                      }
                      else if(sessionAction == AppCommonMethods.SessionAction.ENCODE){
                        AppCommonMethods.logInFile(context, sessionType.name(), "_TID_PICK_STOP (Encode Failed)");
                        /*isEncodeOn.postValue(false);
                        ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
                        setProgressMessage(false);*/
                        updateTagWriteCount(context.getString(R.string.err_encoding_fail));
                      }
                    }
                  }
                }
              }
            }
          }
        };
        pickTidBasedCountDownTimer.start();
        showLog("INTIMER", "OUT");
      }
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      showLog("EXCPICK1", e.getVendorMessage());
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast)
        AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      showLog("EXCPICK2", e.getVendorMessage());
      if(e.getVendorMessage().equalsIgnoreCase("Charging in Progress-Command Not allowed")){
        AppCommonMethods.showShortToast(context, e.getVendorMessage());
      }
      if(e.getVendorMessage().equalsIgnoreCase("Response timeout")){
        disconnect();
        AppCommonMethods.showShortToast(context, String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        rfidInterface.RFIDInitializationStatus(false, "Reader Disconnected", null);
      }
      if(e.getVendorMessage().contains("Region Not Set")){
        AppCommonMethods.showShortToast(context, e.getVendorMessage());
        ConnectAndSetRegion("India");
      }
      if(!e.getVendorMessage().equalsIgnoreCase("Operation In Progress-Command Not Allowed")){
        stopInventory();
        if(AppCommonMethods.isShowReaderCommandFailToast && !e.getVendorMessage().matches("(?i)(Charging in Progress-Command Not allowed|Region Not Set|Response timeout)"))
          AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
      }
    }
    catch(Exception e){
      showLog("EXCPICK3", e.getMessage());
      e.printStackTrace();
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast)
        AppCommonMethods.showShortToast(context, R.string.err_reader_fail);
    }
  }
  
  public void readTagCurrentPassword(Inventory pickedTag){
    readTagCurrentPassword(pickedTag, false, "");
  }
  
  public void readTagCurrentPassword(Inventory pickedTag, final boolean isOnlyRead){
    readTagCurrentPassword(pickedTag, isOnlyRead, "");
  }
  
  public void readTagCurrentPassword(Inventory pickedTag, final String tagCurrentPassword){
    readTagCurrentPassword(pickedTag, false, tagCurrentPassword);
  }
  
  /**
   * Read tag current password.
   *
   * @param pickedTag the picked tag
   */
  public void readTagCurrentPassword(Inventory pickedTag, final boolean isOnlyRead, final String tagCurrentPassword){
    //TAG_CURRENT_PASSWORD = "";
    if(isOnlyRead){
      if(reader != null){
        sessionAction = AppCommonMethods.SessionAction.ENCODE;
        isEncodeOn.postValue(true);
        //isReEncode = true;
        //isReEncodeForOldEPC = true;
        encodeOldEpc = pickedTag.epc;
        //setProgressMessage(context.getString(R.string.msg_pick), true);
        try{
          reader.Actions.PreFilters.deleteAll();
          clearFilters();
          configAction(MAX_POWER_TO_SET, SESSION.SESSION_S0, INVENTORY_STATE.INVENTORY_STATE_A);
          //reader.Config.setDPOState(DYNAMIC_POWER_OPTIMIZATION.DISABLE);
        }
        catch(InvalidUsageException e){
          e.printStackTrace();
          return;
        }
        catch(OperationFailureException e){
          e.printStackTrace();
          return;
        }
        catch(Exception e){
          e.printStackTrace();
          return;
        }
      }
      else{
        isEncodeOn.postValue(false);
        context.showCustomErrDialog(R.string.err_encoding_fail);
        setProgressMessage(false);
        return;
        //fail
      }
    }
    TagAccess tagAccess = new TagAccess();
    final String epc = pickedTag.epc;
    List<String> passwords = SharedPrefManager.getOldAccessPasswords();
    final String currentAccessPassword = SharedPrefManager.getCurrentAccessPassword();
    final boolean isCheckCurrentPasswordFirst = SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_CURRENT_PASSWORD_FIRST, AppCommonMethods.isCheckEncCurrentPasswordFirst);
    final boolean isCompareCurrentPassword = isOnlyRead || SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PASSWORD_FIRST, AppCommonMethods.isCheckEncPasswordFirst);
    
    if(!isCheckCurrentPasswordFirst && (!passwords.contains(currentAccessPassword) || isCompareCurrentPassword)){
      passwords.remove(currentAccessPassword);
      passwords.add(0, currentAccessPassword);
    }
    passwords.remove(defaultTagZeroPassword);
    passwords.add(0, defaultTagZeroPassword);
    if(isCheckCurrentPasswordFirst && (!passwords.contains(currentAccessPassword) || isCompareCurrentPassword)){
      passwords.remove(currentAccessPassword);
      passwords.add(0, currentAccessPassword);
    }
    
    final TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
    showLog("IN READ", "4");
    readAccessParams.setCount(AppCommonMethods.parseInt("2"));
    readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_RESERVED);
    readAccessParams.setOffset(AppCommonMethods.parseInt("2"));
    AppCommonMethods.logInFile(context, sessionType.name(), "_READ_TAG_PASSWORD_START");
    new AsyncTask<Void, Void, String>(){
      private InvalidUsageException invalidUsageException;
      private OperationFailureException operationFailureException;
      //private Boolean bResult = false;
      //private String TAG_CURRENT_PASSWORD = "";
      
      @Override
      protected String doInBackground(Void... voids){
        if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
          /*try{
            reader.Config.setAccessOperationWaitTimeout(READ_OPERATION_TIMEOUT);
          }catch(Exception e){e.printStackTrace();}*/
          String bResult = null;
          for(String oldPassword : passwords){
            try{
              showLog("PASSWORD", oldPassword);
              try{
                readAccessParams.setAccessPassword(Long.decode("0X" + oldPassword));
              }
              catch(NumberFormatException nfe){
                showLog("IN READ", "3 - " + nfe.getMessage());
                nfe.printStackTrace();
              }
              catch(Exception e){
                e.printStackTrace();
              }
              //String tagValue = ApplicationCommonMethods.convertAsciiToHex(epc);
              final TagData tagData = reader.Actions.TagAccess.readWait(epc, readAccessParams, null);//,tagValue.length()<=24);//true
              //showLog("ReadPassword tagData", "" + (tagData != null ? chkNull(tagData.getMemoryBankData(), "empty") : "null"));
              //handle
              //TAG_CURRENT_PASSWORD = "";
              if(tagData != null){
                final String TAG_CURRENT_PASSWORD = chkNull(tagData.getMemoryBankData(), "");
                if(isNonEmpty(TAG_CURRENT_PASSWORD)){
                  showLog("PASSWORDCMD:SUCCESS:", TAG_CURRENT_PASSWORD);
                  //logInFile(context,"PASSWORDCMD:SUCCESS:" + tagData.getMemoryBankData());
                  bResult = TAG_CURRENT_PASSWORD;
                  break;
                  //return TAG_CURRENT_PASSWORD;
                }
              }
              //Thread.sleep(10);
              
            }
            catch(InvalidUsageException e){
              invalidUsageException = e;
              e.printStackTrace();
              //logInFile(context,"PASSWORDCMD:ERR:" + e.getVendorMessage());
              showLog("ReadPASSWORDERROR1", e.getVendorMessage());
            }
            catch(OperationFailureException e){
              operationFailureException = e;
              //logInFile(context,"PASSWORDCMD:ERR:" + e.getVendorMessage());
              showLog("ReadPASSWORDERROR2", e.getVendorMessage());
              e.printStackTrace();
            }
            catch(Exception e){
              showLog("ReadPASSWORDERROR3", e.getMessage());
              e.printStackTrace();
            }
            
          }
          return bResult;
        /*if(bResult){
          return bResult;
        }*/
        }
        return null;
      }
      
      @Override
      protected void onPostExecute(String currentTagPassword){
        showLog(sessionAction.name() + "result", currentTagPassword != null ? currentTagPassword : "null");
        if(isNonEmpty(currentTagPassword)){
          AppCommonMethods.logInFile(context, sessionType.name(), "_READ_TAG_PASSWORD_STOP (" + currentTagPassword + ")");
          if(isOnlyRead){
            readTagPassword.postValue(currentTagPassword);
          }
          else{
            if(sessionAction == AppCommonMethods.SessionAction.DECODE){
              //try writing with the retrieved password
              startDecoding(pickedTag, currentTagPassword);
            }//overrite tags current password with current password
            else if(/*currentAccessPassword.equalsIgnoreCase(defaultTagZeroPassword) ||*/ (currentTagPassword.equalsIgnoreCase(currentAccessPassword))){
              startEncoding(pickedTag, currentTagPassword);
            }
            else{
              writeCurrentPasswordInTag(pickedTag, currentTagPassword);
            }
          }
        }
        //}
        else{
          if(sessionAction == AppCommonMethods.SessionAction.DECODE){
            updateTagWriteCount(context.getString(R.string.err_decoding_auth_fail));
          }
          else{
            AppCommonMethods.logInFile(context, sessionType.name(), "_READ_TAG_PASSWORD_STOP (result is null)");
            /*isEncodeOn.postValue(false);
            ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_auth_fail);
            setProgressMessage(false);*/
            updateTagWriteCount(context.getString(R.string.err_encoding_auth_fail));
          }
        }
      }
    }.execute();
  }
  
  /**
   * Write current password in tag.
   *
   * @param pickedTag the picked tag
   */
  public void writeCurrentPasswordInTag(Inventory pickedTag, final String tagCurrentPassword){
    final String epc = pickedTag.epc;
    //String tagcurrentpassword = tagCurrentPassword;//TAG_CURRENT_PASSWORD;
    final String actualCurrentPassword = SharedPrefManager.getCurrentAccessPassword();
    showLog("CMDTAGCURRENT", tagCurrentPassword);
    showLog("CMDACTUALCURRENT", actualCurrentPassword);
    showLog("CMDEPC", epc);
    if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
      
      TagAccess tagAccess = new TagAccess();
      final TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
      
      try{
        writeAccessParams.setAccessPassword(Long.decode("0X" + tagCurrentPassword.trim()));
        
      }
      catch(NumberFormatException nfe){
        nfe.printStackTrace();
        
      }
      
      writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_RESERVED);
      writeAccessParams.setOffset(2);//AppCommonMethods.parseInt("2"));
      writeAccessParams.setWriteData(actualCurrentPassword.trim());
      //writeSpecificFieldAccessParams.setWriteData(actualcurrentpassword.trim());
      
      writeAccessParams.setWriteDataLength(2);
      writeAccessParams.setWriteRetries(3);
      //writeSpecificFieldAccessParams.setWriteDataLength(2);
      AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_START (" + tagCurrentPassword + ")");
      new AsyncTask<Void, Void, String>(){
        private InvalidUsageException invalidUsageException;
        private OperationFailureException operationFailureException;
        private Exception exception;
        private String bResult = "false";
        
        @Override
        protected String doInBackground(Void... voids){
          try{
            //AntennaInfo antennaInfo = new AntennaInfo();
            //antennaInfo.setAntennaID(reader.Config.Antennas.getAvailableAntennas());
            //reader.Actions.TagAccess.writeAccessPasswordWait(epc, writeSpecificFieldAccessParams, antennaInfo);
            TagData tagData = null;
            if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
              //Current Logic
              reader.Actions.TagAccess.writeWait(epc, writeAccessParams, null, tagData, epc.length() <= 24, false);//encPickedData, false, true);//, epc.length()<=24, false);
              //logInFile(context,"WRPASSWORDCMD:SUCCESS:");
              showLog("SUCCESS", "WRITE PASS IN TAG");
              bResult = "true";
            }
          }
          catch(InvalidUsageException e){
            invalidUsageException = e;
            e.printStackTrace();
            //logInFile(context,"WRPASSWORDCMD:ERR:" + e.getVendorMessage());
            AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (" + e.getVendorMessage() + ")");
            showLog("WRITEPASSWORDERROR1", e.getVendorMessage());
          }
          catch(OperationFailureException e){
            operationFailureException = e;
            final String msg = e.getVendorMessage();
            //logInFile(context,"WRPASSWORDCMD:ERR:" + e.getVendorMessage());
            showLog("WRITEPASSWORDERROR2", msg);
            AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (" + msg + ")");
            //LOCK_ACQUIRE_FAILURE in C1G2AccessOperation
            //access tag crc error
            //access no response from tag
            if(msg.equalsIgnoreCase("access insufficient power")){
              showLog("EXCEPTIONINTIMEOUT", e.getMessage());
              showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
              showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
              showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
              bResult = "INSUFFICIENT POWER";
            }
            else if(msg.equalsIgnoreCase("access tag crc error") || msg.equalsIgnoreCase("data(x)- Size Less than Allowed")){
              showLog("EXCEPTIONINTIMEOUT", e.getMessage());
              showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
              showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
              showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
              
              bResult = "CRC ERROR";
            }
            if(msg.equalsIgnoreCase("LOCK_ACQUIRE_FAILURE in C1G2AccessOperation")){
              showLog("EXCEPTIONINTIMEOUT", e.getMessage());
              showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
              showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
              showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
              
              bResult = "LOCKERROR";
            }
            if(msg.equalsIgnoreCase("access tag memory locked error")){
              showLog("EXCEPTIONINTIMEOUT", e.getMessage());
              showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
              showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
              showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
              
              bResult = "MEMORYLOCK";
            }
            if(msg.equalsIgnoreCase("access no response from tag")){
              showLog("EXCEPTIONINTIMEOUT", e.getMessage());
              showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
              showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
              showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
              
              bResult = "TAGNORESPONSE";
            }
            if(msg.equalsIgnoreCase("access tag memory overrun error")){ //|| msg.equalsIgnoreCase("data(x)- Size Less than Allowed")
              showLog("EXCEPTIONINTIMEOUT", e.getMessage());
              showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
              showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
              showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());
              bResult = "OVERRUN";
            }
            if(msg.equalsIgnoreCase("TIME OUT") || msg.equalsIgnoreCase("Response timeout")){
              showLog("EXCEPTIONINTIMEOUT", e.getMessage());
              showLog("EXCEPTIONINTIMEOUTLOCAL", e.getLocalizedMessage());
              showLog("EXCEPTIONINTIMEOUTLRF", "" + e.getResults().getValue());
              showLog("EXCEPTIONINTIMEOUTLOSTD", e.getStatusDescription());//RFID_ACCESS_TAG_WRITE_FAILED
              bResult = "TIMEOUT";
            }
            e.printStackTrace();
          }
          catch(Exception e){
            exception = e;
            bResult = "TIMEOUT";
            AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (" + tagCurrentPassword + ")_" + e.getMessage() + ")");
            e.printStackTrace();
          }
          return bResult;
        }
        
        @Override
        protected void onPostExecute(String result){
          if(result != null){
            AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (" + actualCurrentPassword + ")_" + result.replaceFirst("true", "Success") + ")");
            if(!result.equalsIgnoreCase("true")){
              if(result.equalsIgnoreCase("false")){
                if(invalidUsageException != null){
                  // rfidListeners.onFailure(invalidUsageException);
                }
                else if(operationFailureException != null){
                  // rfidListeners.onFailure(operationFailureException);
                }
                else{
                  // rfidListeners.onFailure(""+exception.getMessage());
                }
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                /*isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_auth_fail);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_auth_fail));
              }
              else if(result.equalsIgnoreCase("MEMORYLOCK")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                /*isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_fail_locked_memory);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_fail_locked_memory));
              }
              else if(result.equalsIgnoreCase("TIMEOUT")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                //if(SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase(defaultTagZeroPassword)){
                //encoding failed
                /*isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_auth_fail);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_auth_fail));
                /*}
                else{
                  //check if one of old password and current password are not identical.
                  //read password by other using other passwords and replace current password in tag and then lock. after lock success then write into tag with current password.
                  //read tag passwords
                  if(isNullOrEmpty(tagCurrentPassword)) readTagCurrentPassword(pickedTag);
                  else{
                    isEncodeOn.postValue(false);
                    ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
                    setProgressMessage(false);
                  }
                }*/
              }
              /*else if(result.equalsIgnoreCase("OVERRUN")){
                isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_overrun_fail);
                setProgressMessage(false);
              }*/
              else if(result.equalsIgnoreCase("TAGNORESPONSE")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){//isNullOrEmpty(currentTagPassword)){//false){//true){
                  readTagCurrentPassword(pickedTag);
                }
                else{
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_tag_response_fail);//err_encoding_tag_response_fail);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_tag_response_fail));
                }
              }
              else if(result.equalsIgnoreCase("LOCKERROR")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                /*isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_fail_lock_grant);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_fail_lock_grant));
              }
              else if(result.equalsIgnoreCase("CRC ERROR")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  readTagCurrentPassword(pickedTag, tagCurrentPassword);
                }
                else{
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_crc_fail);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_crc_fail));
                }
              }
              else if(result.equalsIgnoreCase("OVERRUN")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                if(true){
                  readTagCurrentPassword(pickedTag, tagCurrentPassword);
                }
                else{
                  /*isEncodeOn.postValue(false);
                  ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_crc_fail);
                  setProgressMessage(false);*/
                  updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_crc_fail));
                }
              }
              else if(result.equalsIgnoreCase("INSUFFICIENT POWER")){
                //logInFile(context,"ENCODECOMMAND:RESULT:" + result);
                /*isEncodeOn.postValue(false);
                ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_access_power_insufficient);
                setProgressMessage(false);*/
                updateTagWriteCount(context.getString(R.string.err_encoding_access_power_insufficient));
              }
            }
            else if(!actualCurrentPassword.equalsIgnoreCase(defaultTagZeroPassword) && tagCurrentPassword.equalsIgnoreCase(defaultTagZeroPassword)){//temp condition
              //Lock RFID
              lockRfidTag(pickedTag, actualCurrentPassword);
            }
            else startEncoding(pickedTag, actualCurrentPassword);
            
          }
          else{
            AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (result is null)");
            /*isEncodeOn.postValue(false);
            ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_auth_fail);
            setProgressMessage(false);*/
            updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_auth_fail));
          }
        }
      }.execute();
    }
    else{
      AppCommonMethods.logInFile(context, sessionType.name(), "_WRITE_TAG_PASSWORD_STOP (reader is null)");
      /*isEncodeOn.postValue(false);
      ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_pass_write_auth_fail);
      setProgressMessage(false);*/
      updateTagWriteCount(context.getString(R.string.err_encoding_pass_write_auth_fail));
    }
  }
  
  /**
   * Lock rfid tag.
   *
   * @param pickedTag the picked tag
   */
  public void lockRfidTag(final Inventory pickedTag, final String currentTagPassword){
    //Set the param values
    final String epc = pickedTag.epc;
    LOCK_DATA_FIELD lockDataField = LOCK_DATA_FIELD.LOCK_EPC_MEMORY;
    LOCK_PRIVILEGE lockPrivilege = LOCK_PRIVILEGE.LOCK_PRIVILEGE_READ_WRITE;
    LOCK_DATA_FIELD lockDataFieldReserved = LOCK_DATA_FIELD.LOCK_ACCESS_PASSWORD;
    TagAccess tagAccess = new TagAccess();
    showLog("lockId", lockPrivilege.toString());
    final TagAccess.LockAccessParams lockAccessParams = tagAccess.new LockAccessParams();
    if(lockDataField != null) lockAccessParams.setLockPrivilege(lockDataField, lockPrivilege);
    
    if(lockDataFieldReserved != null){
      lockAccessParams.setLockPrivilege(lockDataFieldReserved, lockPrivilege);
    }
    try{
      lockAccessParams.setAccessPassword(Long.decode("0X" + SharedPrefManager.getCurrentAccessPassword()));
    }
    catch(NumberFormatException nfe){
      nfe.printStackTrace();
      
    }
    AppCommonMethods.logInFile(context, sessionType.name(), "_LOCK_TAG_START (" + currentTagPassword + ")");
    if(reader == null || !reader.isConnected() || reader.Actions == null || reader.Actions.TagAccess == null)
      return;
    new AsyncTask<Void, Void, String>(){
      private InvalidUsageException invalidUsageException;
      private OperationFailureException operationFailureException;
      private String bResult = "false";
      
      @Override
      protected String doInBackground(Void... voids){
        try{
          if(reader != null && reader.isConnected() && reader.Actions != null && reader.Actions.TagAccess != null){
            reader.Actions.TagAccess.lockWait(epc, lockAccessParams, null, true);
            //logInFile(context,"LCPASSWORDCMD:SUCCESS:");
            bResult = "true";
          }
        }
        catch(InvalidUsageException e){
          invalidUsageException = e;
          bResult = e.getVendorMessage();
          showLog("LCPASSWORDCMD:ERR:", e.getVendorMessage());
          //logInFile(context,"LCPASSWORDCMD:ERR:" + e.getVendorMessage());
          e.printStackTrace();
        }
        catch(OperationFailureException e){
          bResult = e.getVendorMessage();
          showLog("LCPASSWORDCMD:ERR:", e.getVendorMessage());
          //logInFile(context,"LCPASSWORDCMD:ERR:" + e.getVendorMessage());
          operationFailureException = e;
          e.printStackTrace();
        }
        catch(Exception e){
          bResult = e.getMessage();
          showLog("LCPASSWORDCMD:ERR:", e.getMessage());
          e.printStackTrace();
        }
        return bResult;
      }
      
      @Override
      protected void onPostExecute(String result){
        AppCommonMethods.logInFile(context, sessionType.name(), "_LOCK_TAG_STOP (" + currentTagPassword + ")_" + result.replaceFirst("true", "Success") + ")");
        if(!result.equalsIgnoreCase("true")){
          if(invalidUsageException != null){
            /*Empty method*/
          }
          else if(operationFailureException != null){
            /*Empty method*/
          }
          /*isEncodeOn.postValue(false);
          ((MainActivity) context).showCustomErrDialog(R.string.err_encoding_lock_tag_fail);
          setProgressMessage(false);*/
          updateTagWriteCount(context.getString(R.string.err_encoding_lock_tag_fail));
        }
        else{
          //write again
          startEncoding(pickedTag, currentTagPassword);
        }
      }
    }.execute();
  }
  
  /*public void checkReaderBatteryLevel(){
    if(batteryData != null){
      int level = batteryData.getLevel();
      checkReaderBatteryLevel(level);
    }
  }*/
  public void checkReaderBatteryLevel(final int level){
    if(level <= SharedPrefManager.getInt(ParamConstants.minBatteryPercentForReaderOperations, 20)){
      context.showCustomErrDialog(String.format(context.getString(R.string.err_reader_battery_low), level + "%"));
      stopInventory();
    }
  }
  
  @Override
  public void dcssdkEventScannerAppeared(DCSScannerInfo dcsScannerInfo){
  
  }
  
  @Override
  public void dcssdkEventScannerDisappeared(int i){
  
  }
  
  @Override
  public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo dcsScannerInfo){
  
  }
  
  @Override
  public void dcssdkEventCommunicationSessionTerminated(int i){
  
  }
  
  @Override
  public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType, int fromScannerID){
    String s = new String(barcodeData);
    //context.barcodeData(s);
    Log.d(TAG, "barcaode =" + s);
  }
  
  @Override
  public void dcssdkEventImage(byte[] bytes, int i){
  
  }
  
  @Override
  public void dcssdkEventVideo(byte[] bytes, int i){
  
  }
  
  @Override
  public void dcssdkEventBinaryData(byte[] bytes, int i){
  
  }
  
  @Override
  public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent firmwareUpdateEvent){
  
  }
  
  @Override
  public void dcssdkEventAuxScannerAppeared(DCSScannerInfo dcsScannerInfo, DCSScannerInfo dcsScannerInfo1){
  
  }
  
  public String Test2(){
    // check reader connection
    if(!isReaderConnected()) return "Not connected";
    // Set the singulation control to S2 which will read each tag once only
    try{
      Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
      s1_singulationControl.setSession(SESSION.SESSION_S2);
      s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
      s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
      reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      return e.getResults().toString() + " " + e.getVendorMessage();
    }
    return "Session set to S2";
  }
  
  public String Defaults(){
    // check reader connection
    if(!isReaderConnected()) return "Not connected";
    try{
      // Power to 270
      Antennas.AntennaRfConfig config = null;
      config = reader.Config.Antennas.getAntennaRfConfig(1);
      config.setTransmitPowerIndex(MAX_POWER_TO_SET);
      config.setrfModeTableIndex(0);
      config.setTari(0);
      reader.Config.Antennas.setAntennaRfConfig(1, config);
      // singulation to S0
      Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
      s1_singulationControl.setSession(SESSION.SESSION_S0);
      s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
      s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
      reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      return e.getResults().toString() + " " + e.getVendorMessage();
    }
    return "Default settings applied";
  }
  
  private RFIDResults setPrefilter(String tagId, boolean storeRestore){
    showLog("setPrefilter_START", tagId);
    PreFilters.PreFilter[] filterList = new PreFilters.PreFilter[1];
    PreFilters filters = new PreFilters();
    PreFilters.PreFilter filter = filters.new PreFilter();
    filter.setAntennaID((short) 1);// Set this filter for Antenna ID 1
    
    if(tagId.length() < 16){
      return RFID_API_SUCCESS;
    }
    String subTag = tagId.substring(16); // 8 words, cut 4 word = 8 bytes = 16 char
    filter.setTagPattern(subTag);
    filter.setTagPatternBitCount(subTag.length() * 4); // 4 words len =  8 byte = 16 char, subTag leb = 16 char = 16*4 = 64;
    filter.setBitOffset((2 + 4) * 2 * 8); // 2 word + 4 word data = *2 byte = *8 bit
    filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
    
    //INV B and Action=B
    filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE); // use state aware singulation
    filter.StateAwareAction.setTarget(TARGET.TARGET_INVENTORIED_STATE_S0); // inventoried flag of session S1 of matching tags to B
    //filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B);
    filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A);
    
    filterList[0] = filter;
    // not to select tags that match the criteria
    try{
      reader.Actions.PreFilters.add(filterList, null);
      //reader.Actions.PreFilters.add(filter);
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      return RFIDResults.RFID_COMM_SEND_ERROR;
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      return RFIDResults.RFID_COMM_SEND_ERROR;
    }
    return RFID_API_SUCCESS;
  }
  
  private RFIDResults setPrefilterTid(String tagId, boolean storeRestore){
    showLog("setPrefilter_START", tagId);
    PreFilters.PreFilter[] filterList = new PreFilters.PreFilter[1];
    PreFilters filters = new PreFilters();
    PreFilters.PreFilter filter = filters.new PreFilter();
    filter.setAntennaID((short) 1);// Set this filter for Antenna ID 1
    
    //    if(tagId.length() < 16){
    //      return RFID_API_SUCCESS;
    //    }
    //String subTag = tagId.substring(16); // 8 words, cut 4 word = 8 bytes = 16 char
    filter.setTagPattern(tagId);
    filter.setTagPatternBitCount(tagId.length() * 4); // 4 words len =  8 byte = 16 char, subTag leb = 16 char = 16*4 = 64;
    filter.setBitOffset(0); // 2 word + 4 word data = *2 byte = *8 bit
    filter.setMemoryBank(MEMORY_BANK.MEMORY_BANK_TID);
    
    //INV B and Action=B
    filter.setFilterAction(FILTER_ACTION.FILTER_ACTION_STATE_AWARE); // use state aware singulation
    filter.StateAwareAction.setTarget(TARGET.TARGET_INVENTORIED_STATE_S0); // inventoried flag of session S1 of matching tags to B
    //filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B);
    filter.StateAwareAction.setStateAwareAction(STATE_AWARE_ACTION.STATE_AWARE_ACTION_INV_B_NOT_INV_A);
    
    filterList[0] = filter;
    // not to select tags that match the criteria
    try{
      reader.Actions.PreFilters.add(filterList, null);
      //reader.Actions.PreFilters.add(filter);// null);
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
      return RFIDResults.RFID_COMM_SEND_ERROR;
    }
    catch(OperationFailureException e){
      e.printStackTrace();
      return RFIDResults.RFID_COMM_SEND_ERROR;
    }
    return RFID_API_SUCCESS;
  }
  
  private void setStopTrigger(){
    TriggerInfo triggerInfo = new TriggerInfo();
    triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
    triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_ACCESS_N_ATTEMPTS_WITH_TIMEOUT);
    StopTrigger stopTrigger = triggerInfo.StopTrigger;
    stopTrigger.AccessCount.setN((short) 1);
    stopTrigger.AccessCount.setTimeout(500);
    try{
      reader.Config.setStopTrigger(stopTrigger);
    }
    catch(InvalidUsageException e){
      throw new RuntimeException(e);
    }
    catch(OperationFailureException e){
      throw new RuntimeException(e);
    }
    
  }
  
  private void setStopTriggerRead(){
    TriggerInfo triggerInfo = new TriggerInfo();
    triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
    triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_TAG_OBSERVATION_WITH_TIMEOUT);
    StopTrigger stopTrigger = triggerInfo.StopTrigger;
    stopTrigger.TagObservation.setTimeout(100);
    stopTrigger.TagObservation.setN((short) 1);
    
    try{
      reader.Config.setStopTrigger(stopTrigger);
    }
    catch(InvalidUsageException e){
      throw new RuntimeException(e);
    }
    catch(OperationFailureException e){
      throw new RuntimeException(e);
    }
    
  }
  
  //  public void writeTags(final Inventory pickedTag, final String sPassword) throws InterruptedException{
  //    Long tiempo = Long.valueOf(0);
  //    if(pickedTag!=null){
  //      Toast.makeText(context, "Tag List Empty", Toast.LENGTH_SHORT).show();
  //      return;
  //    }
  //    try{
  //      reader.Actions.Inventory.stop();
  //    }
  //    catch(InvalidUsageException e){
  //      throw new RuntimeException(e);
  //    }
  //    catch(OperationFailureException e){
  //      throw new RuntimeException(e);
  //    }
  //    /////////////////////////////////////////////
  //    // Step 1: set Inv B, S0
  //    /////////////////////////////////////////////
  //    count = 1;
  //    setSingulationControl();
  //    //setSingulationControlPrefilterReset();
  //
  //    //setStopTrigger();
  //    setStopTriggerRead();
  //    //setLinkProfile();
  //    // Write user memory bank data
  //    Collection<List<TagData>> data = partitionTagListBasedOnSize(myTagsList, tagDB.size());
  //    List<TagData> batchedTagData = data.iterator().next();
  //
  //    iWriteOK = 0;
  //    iWritePartial = 0;
  //    iErrorExcetion = 0;
  //    iErrorPower = 0;
  //    iErrorNoResponse = 0;
  //    iErrorTagDataNull = 0;
  //    iErrorAPITimeout = 0;
  //    iTotalWriteCommandSent = 0;
  //    iErrorCRC = 0;
  //    iErrorOther = 0;
  //    //setPower(30);
  //    writeEpcLength = 6;//writeEpcLength>=8?6:8;
  //    for(int i = 0; i < batchedTagData.size(); i++){
  //      // for (TagData tag : batchedTagData)
  //      {
  //        //Toggle bit 95
  //        TagData tag = batchedTagData.get(i);
  //        final String originalTagID = tag.getTagID();
  //        String toWriteTagID = originalTagID;
  //
  //        setPrefilter(originalTagID, true);
  //
  //        TagData tagData = new TagData();
  //        TagAccess tagAccess = new TagAccess();
  //        TagAccess.WriteAccessParams writeAccessParams = tagAccess.new WriteAccessParams();
  //        final int currentWordLen = writeEpcLength;//originalTagID.length() / 4;
  //        Log.e("originalTagID", originalTagID + "_" + currentWordLen);
  //        final String writeData = generateRandom(currentWordLen);
  //        final String writeDataPC = writeData.substring(0, 4);
  //        final int iTotalWriteLengthWORD = (writeDataPC.length() / 4);
  //        //final int offset = iTotalWriteLengthWORD % 2 == 0 ? 2 : 1;
  //        Log.e("writeDataPC", writeDataPC + "_" + iTotalWriteLengthWORD);
  //
  //        writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
  //        writeAccessParams.setOffset(1);
  //        writeAccessParams.setWriteData(writeDataPC);
  //        writeAccessParams.setWriteDataLength(iTotalWriteLengthWORD);
  //        writeAccessParams.setAccessPassword(Long.decode("0X" + sPassword));
  //
  //                /*TagAccess.ReadAccessParams readAccessParams = tagAccess.new ReadAccessParams();
  //                //readAccessParams.setAccessPassword(Long.decode("0X" + sPassword));
  //                readAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
  //                readAccessParams.setOffset(2);
  //                readAccessParams.setCount(4);*/
  //
  //        String result;
  //        int iTotalWriteLengthWORD1 = 0;
  //        try{
  //          Log.d("BATCH TAG WRITE", originalTagID + "_" + currentWordLen + "-" + writeData + "_" + iTotalWriteLengthWORD1 + "-" + 1);
  //          /////////////////////////////////////////////
  //          // Step 5: Write 8 tags
  //          /////////////////////////////////////////////
  //          tagData = new TagData();
  //          tiempo = System.currentTimeMillis();
  //          reader.Actions.TagAccess.write(null, writeAccessParams, null, tagData, true, WRITE_OPERATION_TIMEOUT);
  //          final int currentWordLen1 = originalTagID.length() / 4;
  //
  //          final String writeData1 = writeData.substring(4);//generateRandom1(currentWordLen, originalTagID);
  //          iTotalWriteLengthWORD1 = (writeData1.length() / 4);
  //          Log.e("writeDataRest", writeData1 + "_" + iTotalWriteLengthWORD1);
  //          writeAccessParams.setMemoryBank(MEMORY_BANK.MEMORY_BANK_EPC);
  //          writeAccessParams.setOffset(2);
  //          writeAccessParams.setWriteData(writeData1);
  //          writeAccessParams.setWriteDataLength(iTotalWriteLengthWORD1);
  //          reader.Actions.TagAccess.writeblock(null, writeAccessParams, null, tagData, true, WRITE_OPERATION_TIMEOUT);
  //
  //          //Log.e("ECRT", "### START Read= " + tag.getTagID());
  //
  //          //reader.Actions.Inventory.perform();
  //          //reader.Actions.TagAccess.readEvent(readAccessParams, null, null);
  //
  //          iTotalWriteCommandSent++;
  //          tiempo = (System.currentTimeMillis() - tiempo);
  //          result = "Success";
  //
  //          Log.e("ECRT", "### DONE Read= " + tag.getTagID());
  //          Log.d("ECRT", "\r\n");
  //
  //        }
  //        catch(InvalidUsageException e){
  //          result = "Failed";
  //          tiempo = (System.currentTimeMillis() - tiempo);
  //          iErrorExcetion++;
  //          //e.printStackTrace();
  //        }
  //        catch(OperationFailureException e1){
  //          tiempo = (System.currentTimeMillis() - tiempo);
  //          result = "Failed";
  //          iErrorExcetion++;
  //          final String sQFE = e1.getVendorMessage();
  //
  //          Log.e("ECRT", "QFE=" + sQFE + ",RSSI=" + tag.getPeakRSSI() + ",ID=" + tag.getTagID());
  //          listener.updateUI(tag.getTagID() + ",OFE=" + e1.getVendorMessage() + ",ms=" + tiempo);
  //        }
  //        //final int randomepclength=iTotalWriteLengthWORD1;
  //        /////////////////////////////////////////////
  //        // Step 6: Write Results
  //        /////////////////////////////////////////////
  //        if(tagData.getOpStatus() != null){
  //          //int iWrittenWord = tagData.getNumberOfWords();
  //          Log.e("BATCH TAG Original Tag ID", result + " TagId " + originalTagID);
  //          Log.e("BATCH TAG WRITE RESULT", result + " Returned TagId " + tagData.getTagID());
  //          Log.e("BATCH TAG WRITE RESULT", result + " OpStatus: " + tagData.getOpStatus());
  //          Log.e("BATCH TAG WRITE RESULT", result + " numofwordswritten " + tagData.getNumberOfWords());
  //
  //          Log.e("BATCH TAG WRITE RESULT", "\r\n");
  //
  //          final int iWritten = tagData.getNumberOfWords();
  //
  //          if(iWritten == iTotalWriteLengthWORD1){
  //            iWriteOK++;
  //            Log.d("ECRT", "Passed=" + iWriteOK);
  //            listener.updateUI(tag.getTagID() + " " + tagData.getOpStatus().toString() + " " + tiempo + ", Written=" + iWritten);
  //          }
  //          else{
  //            if(iWritten > 0) iWritePartial++;
  //            final String sError = tagData.getOpStatus().toString();
  //            Log.e("ECRT", "ID=" + tagData.getTagID() + ", Error=" + sError + " ,Written=" + iWritten);
  //
  //            if(sError.contains("ACCESS_NO_RESPONSE_FROM_TAG")){
  //              iErrorNoResponse++;
  //              Log.e("ECRT", "No_Res:" + "Written=" + tagData.getNumberOfWords() + ",RSSI=" + tag.getPeakRSSI() + ",ID=" + tag.getTagID());
  //            }
  //            else if(sError.contains("ACCESS_INSUFFICIENT_POWER")){
  //              iErrorPower++;
  //            }
  //            else if(sError.contains("ACCESS_TAG_CRC_ERROR")){
  //              iErrorCRC++;
  //            }
  //            else iErrorOther++;
  //
  //            listener.updateUI(tag.getTagID() + ",err=" + sError + ",Written=" + iWritten);
  //          }
  //        }
  //        else{
  //          Log.e("ECRT", "NULL NULL, Failed, RETRY: Passed=" + iWriteOK);
  //          iErrorTagDataNull++;
  //          Log.e("ECRT", "APP Disable RETRY 3, Null=" + iErrorTagDataNull);
  //          //listener.updateUI(tag.getTagID() + ", err=NULL ERROR");
  //        }
  //      }
  //    }
  //  }
  
  private void setSingulationControlPrefilterReset(){
    try{
      Log.d("setSingulationControl", "SingulationControl...");
      
      Antennas.SingulationControl singulationControl;
      singulationControl = new Antennas.SingulationControl();
      
      singulationControl.setSession(SESSION.SESSION_S1); //to do
      singulationControl.setTagPopulation((short) 200);
      singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
      singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
      //mRfidReader.Config.Antennas.setSingulationControl(1, singulationControl);
      reader.Config.Antennas.setSingulationControl(1, singulationControl);
      reader.Actions.PreFilters.deleteAll();
      clearFilters();
      TriggerInfo triggerInfo = new TriggerInfo();
      triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
      triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
      try{
        reader.Config.setStopTrigger(triggerInfo.StopTrigger);
      }
      catch(InvalidUsageException e){
        throw new RuntimeException(e);
      }
      catch(OperationFailureException e){
        throw new RuntimeException(e);
      }
      
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
    }
    catch(OperationFailureException e){
      e.printStackTrace();
    }
  }
  
  private void setSingulationControl(){
    try{
      Log.d("setSingulationControl", "SingulationControl S0 INV_B");
      
      Antennas.SingulationControl singulationControl;
      singulationControl = new Antennas.SingulationControl();
      
      //Prefilter Action B and S0
      singulationControl.setSession(SESSION.SESSION_S0);
      singulationControl.setTagPopulation((short) 64);
      singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
      singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_B);
      
      //mRfidReader.Config.Antennas.setSingulationControl(1, singulationControl);
      reader.Config.Antennas.setSingulationControl(1, singulationControl);
    }
    catch(InvalidUsageException e){
      e.printStackTrace();
    }
    catch(OperationFailureException e){
      e.printStackTrace();
    }
  }
  
  /**
   * The Create instance task.
   */
  
  private class CreateInstanceTask extends AsyncTask<Void, Void, Void>{
    
    boolean isConfigureDevice = false;
    
    /**
     * Instantiates a new Create instance task.
     *
     * @param isConfigureDevice the is configure device
     */
    CreateInstanceTask(final boolean isConfigureDevice){
      this.isConfigureDevice = isConfigureDevice;
    }
    
    @Override
    protected void onPreExecute(){
      super.onPreExecute();
      setProgressMessage(true);
    }
    
    @Override
    protected Void doInBackground(Void... voids){
      showLog(TAG, "CreateInstanceTask");
      // Based on support available on host device choose the reader type
      try{
        
        readers = new Readers(context, ENUM_TRANSPORT.SERVICE_SERIAL);
        availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
        //Latest SDK (2.0.2.86)
        if(isNullOrEmpty(availableRFIDReaderList)){
          readers.Dispose();
          readers = null;
          readers = new Readers(context, ENUM_TRANSPORT.SERVICE_USB);
          availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
        }
        if(isNullOrEmpty(availableRFIDReaderList)){
          readers.Dispose();
          readers = null;
          readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
          availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
        }
      }
      catch(InvalidUsageException e){
        e.printStackTrace();
        readers.Dispose();
        readers = null;
        try{
          readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
          availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
        }
        catch(InvalidUsageException ex){
          ex.printStackTrace();
        }
      }
      if(readers != null && isNonEmpty(availableRFIDReaderList)){
        showLog("SIZE", availableRFIDReaderList.size() + "");
        // if single reader is available then connect it
        if(availableRFIDReaderList.size() == 1){
          reader = availableRFIDReaderList.get(0).getRFIDReader();
        }
        else{
          for(ReaderDevice device : availableRFIDReaderList){
            showLog(TAG, "NAME" + device.getName());
            if(device.getName().matches(readername)){
              reader = device.getRFIDReader();
              break;
            }
          }
        }
      }
      return null;
    }
    
    @Override
    protected void onPostExecute(Void aVoid){
      super.onPostExecute(aVoid);
      new ConnectionTask(isConfigureDevice).execute();
    }
  }
  
  /**
   * The Connection task.
   */
  private class ConnectionTask extends AsyncTask<Void, Void, String>{
    
    boolean isConfigureDevice = false;
    
    /**
     * Instantiates a new Connection task.
     *
     * @param isConfigureDevice the is configure device
     */
    ConnectionTask(final boolean isConfigureDevice){
      this.isConfigureDevice = isConfigureDevice;
    }
    
    @Override
    protected void onPreExecute(){
      super.onPreExecute();
      setProgressMessage(true);
      
    }
    
    @Override
    protected String doInBackground(Void... voids){
      showLog(TAG, "ConnectionTask");
      if(reader == null) GetAvailableReader();
      if(reader != null){
        String result = connect();
        return result;
      }
      return "Failed to find or connect reader";
    }
    
    @Override
    protected void onPostExecute(String result){
      super.onPostExecute(result);
      //Reader Result
      showLog(TAG, "Connection Result : " + result);
      
      isReaderSet.postValue((reader != null && reader.isConnected()) || result.equalsIgnoreCase(DEVICE_STATUS_CONNECTED));
      if((reader != null && reader.isConnected()) || result.equalsIgnoreCase(DEVICE_STATUS_CONNECTED)){
        try{
          final String sdkVersion = reader != null ? reader.versionInfo().getVersion() : "";
          if(isNonEmpty(sdkVersion) && !SharedPrefManager.getReaderSDKVersion().equalsIgnoreCase(sdkVersion))
            SharedPrefManager.setReaderSDKVersion(sdkVersion);
        }
        catch(Exception e){
          e.printStackTrace();
        }
        if(reader != null)
          SharedPrefManager.setIsDeviceBluetoothDependent(reader.getTransport().equals("BLUETOOTH"));
        if(reader != null && reader.Config != null && reader.getTransport().equals("SERVICE_USB")){
          try{
            showLog("setTriggerMode", sessionType.name());
            reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, true);
            reader.Config.setKeylayoutType(ENUM_NEW_KEYLAYOUT_TYPE.RFID, ENUM_NEW_KEYLAYOUT_TYPE.RFID);
          }
          catch(Exception e){
            e.printStackTrace();
          }
        }
        if(isConfigureDevice) ZebraRFIDHandler.this.configureReader(sessionType);
        else setProgressMessage(false);
      }
      else if(result.contains("Failed to find or connect reader")){
        context.showCustomErrDialog(String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        setProgressMessage(false);
        rfidInterface.RFIDInitializationStatus(false, "Failed to find or connect reader", null);
      }
      else if(result.contains("Connection failed  RFID_COMM_OPEN_ERROR")){
        
        context.showCustomErrDialog(String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        setProgressMessage(false);
        rfidInterface.RFIDInitializationStatus(false, "Connection failed  RFID_COMM_OPEN_ERROR", null);
      }
      else if(result.contains("Connection failed null RFID_COMM_OPEN_ERROR")){
        
        context.showCustomErrDialog(String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
        setProgressMessage(false);
        rfidInterface.RFIDInitializationStatus(false, "Connection failed null RFID_COMM_OPEN_ERROR", null);
      }
      else{
        setProgressMessage(false);
        if(result.contains("RFID_READER_REGION_NOT_CONFIGURED")){
          setProgressMessage(false);
          ConnectAndSetRegion("India");
          rfidInterface.RFIDInitializationStatus(true, "RFID_READER_REGION_NOT_CONFIGURED", reader);
        }
        else{
          context.showCustomErrDialog(String.format(context.getString(R.string.err_reader_connection), getTypeCharCode()));
          setProgressMessage(false);
          rfidInterface.RFIDInitializationStatus(false, "", null);
        }
      }
    }
  }
  
  /**
   * The Event handler.
   */
  
  // Implement the RfidEventsLister class to receive event notifications
  public class EventHandler implements RfidEventsListener{
    
    // Read Event Notification
    @Override
    public void eventReadNotify(RfidReadEvents e){
      try{
        if(!isCommandForSearch){
          Thread.sleep(5);
        }
      }
      catch(InterruptedException interruptedException){
        interruptedException.printStackTrace();
      }
      // Recommended to use new method getReadTagsEx for better performance in case of large tag population
      TagData[] myTags = reader.Actions.getReadTags(100);
      final boolean isInvSession = sessionType != null && (sessionType == AppCommonMethods.SessionType.INVENTORY || sessionType == AppCommonMethods.SessionType.ADD_INVENTORY || sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY || sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY);
      if(loopFlag && myTags != null && myTags.length > 0 && sessionAction.getValue() > 0 && sessionAction == AppCommonMethods.SessionAction.INVENTORY && isInvSession){
        insertBulkInventoryData(myTags);
      }
      //Commented code
      //      if(loopFlag && myTags != null && myTags.length > 0){
      //        if(isSinglePick && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK)){
      //          if(myTags.length>1 || pickTags.size()>1) {
      //            stopInventory();
      //            setProgressMessage(false);
      //            context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
      //          }
      //          else {
      //            final TagData tagData = myTags[0];
      //            final String epcdt=tagData.getTagID();
      //            final String tid = (readTid || (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST) && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? /*AppCommonMethods.isUse24LengthTIDForUpload && chkNull(tagData.getMemoryBankData(),"").length()>24? tagData.getMemoryBankData().substring(0,24) :*/ tagData.getMemoryBankData() : "";
      //            if((!isActionTidPick && !pickTags.contains(epcdt)) || (isActionTidPick && !pickTags.contains(tid) && SCANNED_TIDS.contains(tid))){
      //              pickTags.add(isActionTidPick ? tid : epcdt);
      //              pickUHFTags.add(tagData);
      //              if(isActionTidPick && pickTags.size() == SCANNED_TIDS.size()){//1){
      //                showLog("pickTags", "" + pickTags.size());
      //                pickCountDownTimer.cancel();
      //                pickCountDownTimer.onFinish();
      //              }
      //              else if(pickTags.size() > (isActionTidPick ? SCANNED_TIDS.size() : 1)){
      //                showLog("SINGLEPICK_pickTags", "" + pickTags.size());
      //                stopInventory();
      //                setProgressMessage(false);
      //                context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
      //              }
      //            }
      //          }
      //        }
      //        else if(isCommandForSearch || sessionAction == AppCommonMethods.SessionAction.SEARCH){
      //          //TODO find avg rssi & percentage
      //        }
      //        else{
      //          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
      //            inventoryDao.insertAll(handleTagFiltering(myTags).stream().map(tag->{
      //              return getDataFromTagInfo(tag);
      //            }).collect(Collectors.toList()));
      //          }
      //        }
      //      }
      //old code
      else if(loopFlag && myTags != null) //TODO process all records at once; for(Search/Pick/Inventory)
        for(TagData tagData : myTags){
          if(loopFlag){
            showLog("LOOPFLAG", "TRUE");
            if(tagData != null && isNonEmpty(tagData.getTagID()) /*&& chkNull(tagData.getTagID(), "").length() >= 24*/){
              printTagDataLogs(tagData);
              storeInventoryData(tagData);
            }
          }
          else break;
        }
    }
    
    private void printTagDataLogs(final TagData tagData){
      try{
        showLog("inv_epcdt", tagData.getTagID());
        showLog("inv_epcdtTID", tagData.getMemoryBankData());
        showLog("inv_PC", "" + chkNull(tagData.getPC(), 0));
        showLog("inv_Phase", "" + chkNull(tagData.getPhase(), 0));
        showLog("inv_Antenna", "" + chkNull(tagData.getAntennaID(), 0));
        showLog("inv_ChannelIndex", "" + chkNull(tagData.getChannelIndex(), 0));
        showLog("inv_SeenCount", "" + chkNull(tagData.getTagSeenCount(), 0));
        showLog("inv_CRC", "" + chkNull(tagData.getCRC(), 0));
        showLog("inv_XPC_W1", "" + chkNull(tagData.getXPC_W1(), 0));
        showLog("inv_XPC_W2", "" + chkNull(tagData.getXPC_W2(), 0));
        showLog("inv_OpCode", "" + tagData.getOpCode() != null ? chkNull(tagData.getOpCode().toString(), "empty") : "nullObj");
        showLog("inv_OpStatus", "" + tagData.getOpStatus() != null ? chkNull(tagData.getOpStatus().toString(), "empty") : "nullObj");
        showLog("inv_G2v2_OpCode", "" + tagData.getG2v2OpCode() != null ? chkNull(tagData.getG2v2OpCode().toString(), "empty") : "nullObj");
        showLog("inv_G2v2_OpStatus", "" + tagData.getG2v2OpStatus() != null ? chkNull(tagData.getG2v2OpStatus().toString(), "empty") : "nullObj");
        showLog("inv_G2v2_Response", "" + chkNull(tagData.getG2v2Response(), "empty"));
        showLog("inv_SIZE", "" + chkNull(tagData.getTagIDAllocatedSize(), 0));
      }
      catch(Exception e){ e.printStackTrace(); }
    }
    
    private void insertBulkInventoryData(final TagData[] myTags){
      List<TagData> listTags = handleTagFiltering(myTags);
      if(isNonEmpty(listTags)){
        List<Inventory> listInvTags = new ArrayList<>(0);
        //List<Inventory> listInvTags1 = new ArrayList<>(0);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
          listInvTags = listTags.stream().map(tag -> {
            if(isDebugApp) printTagDataLogs(tag);
            final Inventory inventory = getDataFromTagInfo(tag);
            return inventory;
          }).collect(Collectors.toList());
        }
        else{
          for(TagData tag : listTags){
            if(isDebugApp) printTagDataLogs(tag);
            final Inventory inventory = getDataFromTagInfo(tag);
            if(inventory != null){
              listInvTags.add(inventory);
            }
          }
        }
        if(isNonEmpty(listInvTags)){
          //temp code
          //          for(int i = 0; i < 500; i++)
          //            listInvTags1.addAll(listInvTags);
          //          inventoryDao.insertAll(listInvTags1);
          inventoryDao.insertAll(listInvTags);
          updateAfterInventoryTables(listInvTags);
        }
      }
    }
    
    //TODO make this Async Task
    private void updateAfterInventoryTables(final List<Inventory> inventory){
      new MyInvTask(context, sessionType, zone, fifoDate).executeOnExecutor(MyInvTask.THREAD_POOL_EXECUTOR, inventory.toArray(new Inventory[inventory.size()]));
      //      final ProductDao productDao = AppDatabase.getProductDao(context);
      //      final FIFODao fifoDao = AppDatabase.getFIFODao(context);
      //      if(productDao != null && sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION){
      //        if(zone.equalsIgnoreCase(AppConstants.ALL)) productDao.updateFound(inventory.epc);
      //        else productDao.updateFound(inventory.epc, zone);
      //      }
      //      if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
      //        productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
      //      if(productDao != null && sessionType == AppCommonMethods.SessionType.OUTWARD_PICK)
      //        productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
      //      if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO && fifoDate != null && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK))
      //        fifoDao.updateFound(inventory.ean, inventory.epc, fifoDate);
      //      if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY)
      //        AppDatabase.getBrandEansDao(context).updateScanQty("," + inventory.ean + ",");
    }
    
    private List<TagData> handleTagFiltering(TagData[] myTags){
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        List<TagData> listTagData = Arrays.stream(myTags).filter(tag -> tag != null && isNonEmpty(tag.getTagID()) && filterEPCAndTID(tag.getTagID(), tag.getMemoryBankData())).collect(Collectors.toList());
        return listTagData;
      }
      else{
        List<TagData> listTagData = new ArrayList<>(0);
        for(TagData tag : myTags){
          if(tag != null && isNonEmpty(tag.getTagID()) && filterEPCAndTID(tag.getTagID(), tag.getMemoryBankData()))
            listTagData.add(tag);
        }
        return listTagData;
      }
    }
    
    private boolean filterEPCAndTID(final String epcdt, final String tid){
      if(isNullOrEmpty(epcdt) || epcdt.length() < 24) return false;
      if(readTid && isNullOrEmpty(tid)) return false;
      if(isCommandForSearch || sessionAction == AppCommonMethods.SessionAction.SEARCH){
        if(!isCommandForEPCSearch && !isCommandForTIDSearch){
          if(!isLockSearchEPC || isNullOrEmpty(SEARCH_LOCKED_EPC) || epcdt.equalsIgnoreCase(SEARCH_LOCKED_EPC)){
            //check by using getBarcode method instead of switch case
            final String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt);
            final String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context, SEARCH_BARCODE);
            showLog("search_barcode", barcode);
            showLog("search_compare_barcode", compbarcode);
            return (!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase())));
          }
          else return false;
        }
        else if(isCommandForEPCSearch)
          return (epcdt.equalsIgnoreCase(SEARCH_EPC) || ((sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST) && epcdt.equalsIgnoreCase("0" + SEARCH_EPC.substring(1))));
        else if(isCommandForTIDSearch)
          return isNonEmpty(tid) && (tid.equalsIgnoreCase(SEARCH_TID) || tid.toUpperCase().startsWith(SEARCH_TID.toUpperCase()));
        else return false;
      }
      else if(isSinglePick && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK)){
        //final String tid = readTid && tagData.getMemoryBank() == MEMORY_BANK.MEMORY_BANK_TID ? "" + tagData.getMemoryBankData() : "";
        return isTidBasedPick && encPickedTag != null && isNonEmpty(encPickedTag.tid) && isNonEmpty(tid) && chkNull(encPickedTag.tid, "").equalsIgnoreCase(chkNull(tid, "")) && !pickTags.contains(tid);
      }
      else{
        final BrandEanDao brandEansDao = AppDatabase.getBrandEansDao(context);
        final ProductDao productDao = AppDatabase.getProductDao(context);
        final FIFODao fifoDao = AppDatabase.getFIFODao(context);
        final String ean = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase().trim();
        if(sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION && !productDao.isEPCPresent(epcdt))
          return false;
        if(sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY && (/*(brandEansDao.hasData() && !brandEansDao.isEANPresent("," + context.epcEncoderDecoder.getBarcodeFromEPC(epcdt) + ",")) ||*/ (isNonEmpty(eans) && !eans.contains(context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase()))))
          return false;
        if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY && (/*(brandEansDao.hasData() && !brandEansDao.isEANPresent("," + context.epcEncoderDecoder.getBarcodeFromEPC(epcdt) + ",")) ||*/ (isNonEmpty(eans) && !eans.contains(context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase()))))
          return false;
        if(sessionType == AppCommonMethods.SessionType.OFF_RANGE && (/*(brandEansDao.hasData() && !brandEansDao.isEANPresent("," + context.epcEncoderDecoder.getBarcodeFromEPC(epcdt) + ",")) ||*/ (isNonEmpty(eans) && !eans.contains(context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase()))))
          return false;
        if((sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE || sessionType == AppCommonMethods.SessionType.INWARD_TOTE) && isErrorOrBeepForTote() && isNonEmpty(eans)){
          if((AppCommonMethods.isShowErrorForOtherThanToteEanTag || AppCommonMethods.isPlayErrorBeepForOtherThanToteEanTag) && isNonEmpty(eans) && !eans.contains(ean)){
            if(AppCommonMethods.isShowErrorForOtherThanToteEanTag){
              stopInventory();
              context.showCustomErrDialog(String.format(context.getString(R.string.err_msg_pick_wrong_tote), ean));
              return false;
            }
            else if(AppCommonMethods.isPlayErrorBeepForOtherThanToteEanTag) errorBeep();
          }
          else if(AppCommonMethods.isShowErrorForSameToteTag && inventoryDao.isEPCPresent(sessionId, epcdt)){
            stopInventory();
            context.showCustomErrDialog(R.string.err_msg_already_added);
            return false;
          }
          else if(AppCommonMethods.isPlayBeepForSameToteTagIfNotLastInserted && !chkNull(inventoryDao.getLastInsertedEpc(sessionId), "").equalsIgnoreCase(epcdt))
            beep();
        }
        //Checks only Selected Ean Tags
        if(isNonEmpty(eans) && !eans.contains(ean)) return false;
        //check if epc already scanned
        if(inventoryDao.isEPCPresent(sessionId, epcdt)) return false;
        //Checks only Non-Alien Decoded Tags
        if(sessionType == AppCommonMethods.SessionType.SEARCH_UNENCODED && !context.epcEncoderDecoder.setDataFromEPC(new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue(), epcdt)).tagStatus.equalsIgnoreCase(AppConstants.NON_ENCODED))//!chkNull(ApplicationCommonMethods.getBarcodeFromEPC(uhftagInfo.getEPC()), AppConstants.NON_ENCODED).matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.UNKNOWN + ")"))
          return false;
        //Checks only Alien Tags
        if(sessionType == AppCommonMethods.SessionType.SEARCH_ALIEN && !context.epcEncoderDecoder.setDataFromEPC(new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue(), epcdt)).tagStatus.equalsIgnoreCase(AppConstants.ALIEN))
          return false;
        //Checks if EPC present in fifoDao
        if(sessionType == AppCommonMethods.SessionType.SEARCH_FIFO && sessionAction == AppCommonMethods.SessionAction.SEARCH && !isCommandForEPCSearch && !fifoDao.isEPCPresent(epcdt, ean, fifoDate/*,zone,zoneId*/))
          return false;
        return sessionType == AppCommonMethods.SessionType.ENCODING || (rfidSession != null && rfidSession.sessionType != AppCommonMethods.SessionType.ENCODING.getValue()) || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || (rfidSession != null && rfidSession.sessionType != AppCommonMethods.SessionType.ENCODING_THAN.getValue()) || sessionType == AppCommonMethods.SessionType.SEARCH_FILE || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.SEARCH_FILE.getValue()) && (inventoryDao.isEPCPresent(sessionId, epcdt) || inventoryDao.isTidPresent(sessionId, tid));
      }
    }
    
    // Status Event Notification
    @Override
    public void eventStatusNotify(RfidStatusEvents rfidStatusEvents){
      showLog("isReader", "" + (reader != null));
      showLog("isReaderConnected", "" + (reader != null && reader.isConnected()));
      showLog("rfidStatusEvents", "" + rfidStatusEvents.StatusEventData.getStatusEventType());
      if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT){
        showLog("HANDHELD_TRIGGER_EVENT", rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent().toString());
        showLog("HANDHELD_TRIGGER_EVENT_TRIGGER_TYPE", rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldTriggerType().toString());
        if(rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED){
          new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids){
               setTriggerPressed();
             /* if(!restrictTriggerPress){
                showLog("restrictTriggerPress", "" + restrictTriggerPress);
                isTriggerPressed.postValue(true);
                checkTimer();
              }*/
              
              return null;
            }
          }.execute();
        }
        /*if(rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED){
          new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids){
              
              if(!restrictTriggerPress){
                isTriggerPressed.postValue(false);
                checkTimer();
              }
              
              return null;
            }
          }.execute();
        }*/
        if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.INVENTORY_START_EVENT){
          // Access operation started
          new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids){
              
              return null;
            }
          }.execute();
          
        }
        else if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT){
          // Access operation stopped - Can be used to signal waiting thread
          new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids){
              
              return null;
            }
          }.execute();
        }
      }
      if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.BATTERY_EVENT){
        showLog("BATTERY_EVENT_LEVEL", "" + rfidStatusEvents.StatusEventData.BatteryData.getLevel());
        showLog("BATTERY_EVENT_CHARGING", "" + rfidStatusEvents.StatusEventData.BatteryData.getCharging());
        showLog("BATTERY_EVENT_CAUSE", rfidStatusEvents.StatusEventData.BatteryData.getCause());
        
        new AsyncTask<Void, Void, Void>(){
          @Override
          protected Void doInBackground(Void... voids){
            final Events.BatteryData batteryData = rfidStatusEvents.StatusEventData.BatteryData;
            if(batteryData != null){
              showLog("BATTERY cause", chkNull(batteryData.getCause(), ""));
              boolean isChargerConnected = batteryData != null && batteryData.getCharging() && chkNull(batteryData.getCause(), "").contains(DEVICE_STATUS_CONNECTED);
              final int level = batteryData.getLevel();
              SharedPrefManager.setBatteryReaderPercent(level);
              showLog("rfidStatusBattery", "" + level);
              if(isChargerConnected){
                context.showCustomErrDialog(batteryData.getCause());
                stopInventory();
              }
              else if(!batteryData.getCharging() && chkNull(batteryData.getCause(), "").contains(DEVICE_BATTERY_LOW))
                checkReaderBatteryLevel(level);
            }
            return null;
          }
        }.execute();
      }
      if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.DISCONNECTION_EVENT){
        disconnect();
        showLog("DISCONNECTION_EVENT_INFO", rfidStatusEvents.StatusEventData.DisconnectionEventData.m_DisconnectionEvent.eventInfo.toString());
        showLog("DISCONNECTION_EVENT_READERNAME", rfidStatusEvents.StatusEventData.DisconnectionEventData.m_DisconnectionEvent.getreadername());
      }
      if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.TEMPERATURE_ALARM_EVENT){
        //TODO show Error Dialog (on Handler/Thread)
        new Handler(Looper.getMainLooper()).post(new Runnable(){
          @Override
          public void run(){
            final Events.TemperatureAlarmData temperatureAlarmData = rfidStatusEvents.StatusEventData.TemperatureAlarmData;
            if(temperatureAlarmData != null){
              //stopInventory();
              context.showCustomErrDialog(temperatureAlarmData.getCause());
              showLog("TemperatureAlarmEvent_Cause", temperatureAlarmData.getCause());
              showLog("TemperatureAlarmEvent_AlarmLevel", temperatureAlarmData.getAlarmLevel().toString());
              showLog("TemperatureAlarmEvent_CurrentTemperature", "" + temperatureAlarmData.getCurrentTemperature());
              showLog("TemperatureAlarmEvent_TemperatureSource", temperatureAlarmData.getTemperatureSource().toString());
              showLog("TemperatureAlarmEvent_PATTemperature", "" + temperatureAlarmData.getPATemp());
              showLog("TemperatureAlarmEvent_AmbientTemperature", "" + temperatureAlarmData.getAmbientTemp());
            }
          }
        });
      }
      if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.READER_EXCEPTION_EVENT){
        showLog("ReaderExceptionEvent", rfidStatusEvents.StatusEventData.ReaderExceptionEventData.getReaderExceptionEventInfo());
      }
      if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.POWER_EVENT){
        showLog("PowerEvent_Cause", rfidStatusEvents.StatusEventData.PowerData.getCause());
        showLog("PowerEvent_Power", "" + rfidStatusEvents.StatusEventData.PowerData.getPower());
        showLog("PowerEvent_Current", "" + rfidStatusEvents.StatusEventData.PowerData.getCurrent());
        showLog("PowerEvent_Voltage", "" + rfidStatusEvents.StatusEventData.PowerData.getVoltage());
        //stopInventory();
      }
      if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.WPA_EVENT){
        //Don't Handle
      }
      if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.BUFFER_FULL_WARNING_EVENT){
        //TODO show Warning Dialog
        new Handler(Looper.getMainLooper()).post(new Runnable(){
          @Override
          public void run(){
            final Events.BufferFullWarningEventData bufferFullWarningEventData = rfidStatusEvents.StatusEventData.BufferFullWarningEventData;
            if(bufferFullWarningEventData != null){
              showLog("BufferFullWarningEvent_Cause", bufferFullWarningEventData.toString());
              //context.showCustomErrDialog(bufferFullWarningEventData.toString());
            }
          }
        });
      }
      if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.BUFFER_FULL_EVENT){
        //TODO show Error Dialog/Stop Inventory
        //stopInventory();
      }
      if(rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.OPERATION_END_SUMMARY_EVENT){
        //Don't Handle
      }
    }
    
    private static class MyInvTask extends AsyncTask<Inventory, Void, String>{
      
      ProductDao productDao;
      FIFODao fifoDao;
      BrandEanDao brandEanDao;
      CommonActivity context;
      AppCommonMethods.SessionType sessionType;
      String zone;
      String fifoDate;
      
      MyInvTask(CommonActivity context, AppCommonMethods.SessionType sessionType, String zone, String fifoDate){
        this.context = context;
        this.sessionType = sessionType;
        this.zone = zone;
        this.fifoDate = fifoDate;
        if(context != null && !context.isFinishing()){
          productDao = AppDatabase.getProductDao(context);
          fifoDao = AppDatabase.getFIFODao(context);
          brandEanDao = AppDatabase.getBrandEansDao(context);
        }
      }
      
      @Override
      protected String doInBackground(Inventory... inventories){
        if(inventories != null){
          for(Inventory inventory : inventories){
            if(productDao != null && sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION){
              if(zone.equalsIgnoreCase(AppConstants.ALL)) productDao.updateFound(inventory.epc);
              else productDao.updateFound(inventory.epc, zone);
            }
            if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
              productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
            if(productDao != null && sessionType == AppCommonMethods.SessionType.OUTWARD_PICK)
              productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
            //            if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO && fifoDate != null && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK))
            //              fifoDao.updateFound(inventory.ean, inventory.epc, fifoDate);
            if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY)//sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY)
              brandEanDao.updateScanQty("," + inventory.ean + ",");
          }
        }
        return "";
      }
    }
  }
}
