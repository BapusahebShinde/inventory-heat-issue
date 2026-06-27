package com.itek.retail.reader.chainway;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUseBluetoothScanConnect;
import static com.itek.retail.common.AppCommonMethods.isUseDeviceIDForIMEI;
import static com.itek.retail.common.AppCommonMethods.isUseShortRangeSearchForAll;
import static com.itek.retail.common.AppCommonMethods.isUseShortRangeSearchForOnlyGID;
import static com.itek.retail.common.AppCommonMethods.showShortToast;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
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
import com.itek.retail.ui.home.MainActivity;
import com.rscja.deviceapi.RFIDWithUHFBLE;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.BarcodeResult;
import com.rscja.deviceapi.entity.Gen2Entity;
import com.rscja.deviceapi.entity.InventoryParameter;
import com.rscja.deviceapi.entity.RadarLocationEntity;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.interfaces.ConnectionStatus;
import com.rscja.deviceapi.interfaces.ConnectionStatusCallback;
import com.rscja.deviceapi.interfaces.IUHF;
import com.rscja.deviceapi.interfaces.IUHFInventoryCallback;
import com.rscja.deviceapi.interfaces.IUHFLocationCallback;
import com.rscja.deviceapi.interfaces.IUHFRadarLocationCallback;
import com.rscja.deviceapi.interfaces.KeyEventCallback;
import com.rscja.deviceapi.interfaces.ScanBTCallback;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Chainway rfid handler.
 */
public class ChainwayRFIDHandler extends RFIDHandler{
  
  private static final String TAG_LOG = "CHAINWAY";
  private static final String FILE_TAG_LOG = "DeviceAPI_APP";
  private static final long TAG_SAMPLE_LOG_INTERVAL_MS = 5000L;
  private static ActivityResultLauncher<Intent> bluetoothResultLauncher;
  final int encodeRetryLimit = 2;
  final int decodeRetryLimit = 2;
  private final int MIN_POWER_TO_SET = 5;
  private final int MAX_POWER_TO_SET = 30;
  private final int SOUND_THRESHOLD = 8;
  public MutableLiveData<Boolean> isBarcodeOn = new MutableLiveData<>(false);
  public MutableLiveData<String> barcodeData = new MutableLiveData<>("");
  Set<UHFTAGInfo> pickUHFTags = new HashSet<>(0);
  String readerName = "(?i)(Chainway).*";
  private Boolean loopFlag = false;
  private RFIDWithUHFUART mReader = null;
  private RFIDWithUHFBLE reader = null;
  private boolean isConnected = false;
  private Handler handler;
  private BluetoothAdapter mBluetoothAdapter = null;
  private Boolean isBluetoothReader = false;
  private long lastTagSampleLogMs = 0L;
  //private Timer pickTimer = null;
  private Boolean isInit = false;
  
  /**
   * Calculate rssi percent float.
   *
   * @param var0 the var 0
   * @param var1 the var 1
   * @return the float
   */
  public static float calculateRssiPercent(int var0, float var1){
    return (new BigDecimal((double) var1)).setScale(var0, 4).floatValue();
  }
  
  /**
   * Get value int.
   *
   * @param value the var 1
   * @return the int
   */
  @Override
  protected int getPercentage(int value){
    value = Math.abs(value) - 6;
    int a = 0;
    switch(value){
      case 15:
        a = 100;
      case 16:
        a = 100;
      case 17:
        a = 100;
      case 18:
        a = 100;
      case 19:
        a = 100;
      case 20:
        a = 100;
      case 21:
        a = 100;
      case 22:
        a = 100;
      case 23:
        a = 100;
      case 24:
        a = 100;
      case 25:
        a = 100;
      case 26:
        a = 100;
      case 27:
        a = 100;
      case 28:
        a = 100;
      case 29:
        a = 100;
      case 30:
        a = 100;
      case 31:
        a = 100;
      case 32:
        a = 100;
      case 33:
        a = 100;
      case 34:
        a = 100;
        break;
      case 35:
        a = 99;
      case 36:
        a = 99;
      case 37:
        a = 99;
      case 38:
        a = 99;
      case 39:
        a = 99;
        break;
      case 40:
        a = 98;
        break;
      case 41:
        a = 97;
        break;
      case 42:
        a = 96;
        break;
      case 43:
        a = 94;
        break;
      case 44:
        a = 92;
        break;
      case 45:
        a = 90;
        break;
      case 46:
        a = 89;
        break;
      case 47:
        a = 87;
        break;
      case 48:
        a = 85;
        break;
      case 49:
        a = 84;
        break;
      case 50:
        a = 82;
        break;
      case 51:
        a = 79;
        break;
      case 52:
        a = 75;
        break;
      case 53:
        a = 72;
        break;
      case 54:
        a = 70;
        break;
      case 55:
        a = 67;
        break;
      case 56:
        a = 65;
        break;
      case 57:
        a = 62;
        break;
      case 58:
        a = 60;
        break;
      case 59:
        a = 57;
        break;
      case 60:
        a = 54;
        break;
      case 61:
        a = 51;
        break;
      case 62:
        a = 48;
        break;
      case 63:
        a = 43;
        break;
      case 64:
        a = 40;
        break;
      case 65:
        a = 36;
        break;
      case 66:
        a = 33;
        break;
      case 67:
        a = 31;
        break;
      case 68:
        a = 29;
        break;
      case 69:
        a = 27;
        break;
      case 70:
        a = 25;
        break;
      case 71:
        a = 23;
        break;
      case 72:
        a = 21;
        break;
      case 73:
        a = 19;
        break;
      case 74:
        a = 17;
        break;
      case 75:
        a = 15;
        break;
      case 76:
        a = 13;
        break;
      case 77:
        a = 11;
        break;
      case 78:
        a = 10;
        break;
      case 79:
        a = 8;
        break;
      case 80:
        a = 7;
        break;
      case 81:
        a = 6;
        break;
      case 82:
        a = 5;
        break;
      case 83:
        a = 4;
        break;
      case 84:
        a = 3;
        break;
      case 85:
        a = 2;
        break;
      case 86:
        a = 1;
        break;
    }
    return a;
  }
  
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
      this.mainReaderRepository = mainReaderRepository;
      this.rfidInterface = rfidInitInterface;
      if(!Build.MANUFACTURER.equalsIgnoreCase(AppCommonMethods.DeviceType.CHAINWAY.name()) || SharedPrefManager.getIsDeviceBluetoothDependent()){
        SharedPrefManager.setIsDeviceBluetoothDependent(true);
        isBluetoothReader = true;
        registerBluetoothEvent();
        assignBluetoothReader();
        return reader;
      }
      mReader = RFIDWithUHFUART.getInstance();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"mReader.getInstance()->"+(mReader!=null));
    }
    catch(Exception ex){
      ex.printStackTrace();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,ex.getMessage());
    }
    return mReader;
  }
  
  private void assignBluetoothReader(){
    if(reader == null) reader = RFIDWithUHFBLE.getInstance();
    if(reader != null && checkBluetoothConnection()){
      if(isNonEmpty(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS))){
        if(reader.getConnectStatus() == ConnectionStatus.DISCONNECTED)
          reader.connect(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS));
      }
      else{
        if(isUseBluetoothScanConnect && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
          return;
        Set<BluetoothDevice> listPairedDevices = mBluetoothAdapter.getBondedDevices();
        if(isNonEmpty(listPairedDevices)){
          BluetoothDevice mDevice = null;
          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            Optional<BluetoothDevice> device = listPairedDevices.stream().filter(it -> it.getName().matches(readerName)).findFirst();
            mDevice = (device != null) ? device.get() : null;
          }
          else{
            for(BluetoothDevice bt : listPairedDevices)
              if(bt.getName().matches(readerName)){
                mDevice = bt;
                break;
              }
          }
          if(mDevice != null){
            SharedPrefManager.setString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS, mDevice.getAddress());
            if(reader.getConnectStatus() == ConnectionStatus.DISCONNECTED)
              reader.connect(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS));
          }
          else{
            context.showCustomErrDialog(R.string.err_printer_not_paired);
          }
        }
        else reader.startScanBTDevices(new ScanBTCallback(){
          @Override
          public void getDevices(BluetoothDevice bluetoothDevice, int i, byte[] bytes){
            if(bluetoothDevice != null && bluetoothDevice.getName().matches(readerName)){
              //boolean isPairedDevice = bluetoothDevice.createBond();
              SharedPrefManager.setString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS, bluetoothDevice.getAddress());
              if(reader.getConnectStatus() == ConnectionStatus.DISCONNECTED)
                reader.connect(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS));
            }
          }
        });
      }
    }
  }
  
  private boolean checkBluetoothConnection(){
    if(mBluetoothAdapter == null) mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if(mBluetoothAdapter == null){
      showShortToast(context, String.format(context.getString(R.string.err_no_bluetooth), getTypeCharCode()));
      return false;
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
      return false;
    }
    return true;
  }
  
  private void connect(){
    if(reader != null && reader.getConnectStatus() == ConnectionStatus.DISCONNECTED){
      assignBluetoothReader();
    }
  }
  
  private void disconnect(){
    if(reader != null) reader.disconnect();
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
    if(isBluetoothReader){
      if(!checkBluetoothConnection()) return;
      else if(reader == null){
        assignBluetoothReader();
      }
      else if(reader.getConnectStatus() != ConnectionStatus.CONNECTED) InitSDK(true);
      else if(sessionType.getValue() > 0 && !chkNotNullTrue(isDeviceConfigured.getValue()))
        configureReader(sessionType);
      return;
    }
    if(chkNull(reader, mReader) == null){
      isReaderSet.postValue(null);
      InitSDK(true);
    }
    else if(getConnectStatus() == ConnectionStatus.DISCONNECTED){
      InitSDK(true);
    }
    else if(sessionType.getValue() > 0 && !chkNotNullTrue(isDeviceConfigured.getValue()))
      configureReader(sessionType);
  }
  
  @Override
  public void checkAndSetReader(){
    if(isBluetoothReader && !checkBluetoothConnection()) return;
    if(chkNull(reader, mReader) == null){
      isReaderSet.postValue(null);
      InitSDK(true);
    }
    else if(getConnectStatus() == ConnectionStatus.DISCONNECTED){
      InitSDK(true);//new InitTask(true).execute();
    }
    //else if(sessionType.getValue()>0 && !chkNotNullTrue(isDeviceConfigured.getValue())) configureReader(sessionType);
  }
  
  @Override
  public void InitSDK(){
    super.InitSDK();
    InitSDK(false);
  }
  
  /**
   * Init sdk.
   *
   * @param isConfigureDevice the is configure device
   */
  public void InitSDK(boolean isConfigureDevice){
    saveSerialNo();
    setProgressMessage(true);
    
    try{
      if(isBluetoothReader && reader == null) assignBluetoothReader();
      else if(!isBluetoothReader && mReader == null) mReader = RFIDWithUHFUART.getInstance();
    }
    catch(Exception ex){
      ex.printStackTrace();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,ex.getMessage());
    }
    
    if(chkNull(reader, mReader) != null){
      //Previous code
      //setProgressMessage(false);
      //new InitTask(isConfigureDevice).execute();
      init();
    }
    else{
      setProgressMessage(false);
      rfidInterface.RFIDInitializationStatus(false, "", null);
    }
  }
  
  private void init(){
    if(isBluetoothReader && !checkBluetoothConnection()) return;
    try{
      setProgressMessage(true);
      if(isInit || initReader()){
        if(reader != null && reader.getConnectStatus() == ConnectionStatus.DISCONNECTED){
          if(isNonEmpty(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS)))
            reader.connect(SharedPrefManager.getString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS));
          else assignBluetoothReader();
        }
        showLog(TAG_LOG, "INIT SUCCESS");
        setProgressMessage(false);
        final String sdkVersion = isBluetoothReader ? reader.getVersion() : mReader.getVersion();
        if(isNonEmpty(sdkVersion) && !SharedPrefManager.getReaderSDKVersion().equalsIgnoreCase(sdkVersion))
          SharedPrefManager.setReaderSDKVersion(sdkVersion);
        configureReader(sessionType);
      }
      else{
        setProgressMessage(false);
        showLog(TAG_LOG, "INIT FAIL");
        int maxPower = sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN ? MIN_POWER_TO_SET * 2 : sessionType == AppCommonMethods.SessionType.MOVEMENT || sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD ? MAX_POWER_TO_SET / 2 : MAX_POWER_TO_SET;
        int power = chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower);
        showLog("maxPower", "" + maxPower);
        showLog("power", "" + power);
        setReaderPower(power);
        rfidInterface.RFIDInitializationStatus(false, "RFID initialization Failed", null);
      }
      //setup handler
      if(handler == null) handler = new Handler();
      
      //setup connection callback
      setConnectionStatusCallback((connectionStatus, o) -> {
        showLog("connectionStatus", connectionStatus == null ? "Null" : connectionStatus.name());
        isConnected = connectionStatus != null && connectionStatus == ConnectionStatus.CONNECTED;
        showLog("isConnected", "" + isConnected);
        isReaderSet.postValue(connectionStatus == null ? null : isConnected);
      });
      
      //setup inventory callback
      setInventoryCallback(new IUHFInventoryCallback(){
        @Override
        public void callback(UHFTAGInfo uhftagInfo){
          processUHFData(uhftagInfo);
        }
      });
      
      setKeyEventCallback();
    }catch(Exception e){e.printStackTrace();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
    }
  }
  
  @Override
  protected void saveSerialNo(){
    if(isUseDeviceIDForIMEI){
      if(isNonEmpty(SharedPrefManager.getString(ParamConstants.DEVICE_SERIAL, ""))) return;
      String serial = Settings.Global.getString(context.getContentResolver(), "Serial");
      //Commented for now
      //String imei = Settings.Global.getString(context.getContentResolver(), "Imei1");
      //String imei2 = Settings.Global.getString(context.getContentResolver(), "Imei2");
      if(isNonEmpty(serial)) SharedPrefManager.setString(ParamConstants.DEVICE_SERIAL, serial);
      /*showLog("dd_serial", chkNull(serial, "empty"));
      showLog("dd_imei", chkNull(imei, "empty"));
      showLog("dd_imei2", chkNull(imei2, "empty"));*/
    }
  }
  
  private void setSession(String session, String invType){
    final List<String> sessions = Arrays.asList(context.getResources().getStringArray(R.array.sessions));
    final List<String> inventoryTypes = Arrays.asList(context.getResources().getStringArray(R.array.inventoryTypes));
    int seesionid = sessions.indexOf(session);
    int inventoried = inventoryTypes.indexOf(invType);
    if(seesionid < 0 || inventoried < 0){
      return;
    }
    
    //old sdk code
    /*char[] p = mReader.getGen2();
    if(p != null && p.length >= 14){
      int g = p[12];
      int linkFrequency = p[13];
      if(inventoried == 2){
        linkFrequency = 1;
        inventoried = g;
      }
      else{
        linkFrequency = 0;
      }
      if(mReader.setGen2(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9], p[10], seesionid, inventoried, linkFrequency)){
        showLog(TAG + " SET_SESSION_" + session + "_INV_TYPE_" + invType, "SET");
      }
      else{
        showLog(TAG + " SET_SESSION_" + session + "_INV_TYPE_" + invType, "FAIL");
      }
    }*/
    
    //new SDK code
    Gen2Entity gen2Entity = getGen2();
    if(gen2Entity != null){
      gen2Entity.setQueryTarget(inventoried);
      gen2Entity.setQuerySession(seesionid);
      if(setGen2(gen2Entity)){
        showLog(TAG + " SET_SESSION_" + session + "_INV_TYPE_" + invType, "SET");
      }
      else{
        showLog(TAG + " SET_SESSION_" + session + "_INV_TYPE_" + invType, "FAIL");
      }
    }
    else{
      showLog(TAG + " SET_SESSION_" + session + "_INV_TYPE_" + invType, "FAIL");
    }
  }
  
  @Override
  public boolean performInventory(final boolean isHideUnencodedTags, final List<String> listIgnoreEPCs){
    if(super.performInventory(isHideUnencodedTags, listIgnoreEPCs)){
      int maxPower = sessionType == AppCommonMethods.SessionType.INWARD_TOTE ? (int) (MIN_POWER_TO_SET * inwToteMinPowerMultiplier) : sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE ? (int) (MIN_POWER_TO_SET * owtToteMinPowerMultiplier) : MAX_POWER_TO_SET;
      int power = chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower);
      if(power > maxPower){
        SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), maxPower);
        power = maxPower;
      }
      showLog("maxPower", "" + maxPower);
      showLog("power", "" + power);
      setPower(power);
      boolean isModeSet = readTid ? setEPCAndTIDMode() : setEPCMode();
      showLog("isModeSet", "" + isModeSet);
      setTagFocus(false);
      setFastID(false);
      setSession("S0", "A");
      clearFilters();
      sessionAction = AppCommonMethods.SessionAction.INVENTORY;
      readTag();
      return true;
    }
    return false;
  }
  
  /*@Override
  public void resetReaderPower(){
    super.resetReaderPower();
    int maxPower = sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING ? MIN_POWER_TO_SET * 2 :
    sessionType == AppCommonMethods.SessionType.MOVEMENT || sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD ? MAX_POWER_TO_SET / 2 : MAX_POWER_TO_SET;
    SharedPrefManager.setReaderPower(maxPower);
    SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), maxPower);
  }*/
  
  /**
   * On destroy.
   */
  public void onDestroy(){
    if(isInit)
   /*   new Handler(Looper.getMainLooper()).post(new Runnable(){
      @Override
      public void run()*/{
        boolean result= false;
        try{
          if(beepTimer != null) beepTimer.cancel();
          if(reader != null){
            isInit = false;
            result=reader.free();
            showLog("reader", "free!");
            AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"free -> "+result);
          }
          if(mReader != null){
            isInit = false;
            result=mReader.free();
            showLog("mReader", "free!");
            AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"free -> "+result);
          }
        }catch(Exception e){e.printStackTrace();
          AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
        }
      }
    //});
    super.onDestroy();
  }
  
  /**
   * Perform search.
   */
  @Override
  protected synchronized void performSearch(){
    sessionAction = AppCommonMethods.SessionAction.SEARCH;
    showLog("CMD", "" + isCommandForSearch);
    // check reader connection
    if(!isReaderConnected()){
      showLog("Reader", "NOT CONNECTED");
      return;
    }
    clearFilters();
    setSession("S0", Build.MODEL.equalsIgnoreCase("C5P") || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ? "A" : "B");
    setTagFocus(false);
    setFastID(false);
    showLog("isCommandForTIDSearch", "" + isCommandForTIDSearch);
    if(setPower(MAX_POWER_TO_SET)){
      if(isCommandForTIDSearch ? setEPCAndTIDMode() : setEPCMode()){
        new Thread(){
          @Override
          public void run(){
            try{
              String finalsgtin = SEARCH_EPC;
              String header = finalsgtin.length() > 2 ? finalsgtin.substring(0, 2) : "";
              final boolean isNonStdEnc = finalsgtin.length() >= 32 && header.matches("(?i)(BC|0C|00)");
              
              if(isCommandForTIDSearch) addTidBasedFilters(SEARCH_TID);
              else if(isCommandForEPCSearch) addEpcBasedFilters(finalsgtin, isNonStdEnc);
              else
                addFilters(isNonStdEnc && finalsgtin.length() > 9 ? finalsgtin.substring(9) : finalsgtin, isNonStdEnc);
              //}
            }
            catch(Exception e){
              e.printStackTrace();
              AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
              stopInventory();
              if(AppCommonMethods.isShowReaderCommandFailToast)
                showShortToast(context, R.string.err_reader_fail);
            }
          }
        }.start();
      }
    }
    /*else{
      onResume(sessionType);
    }*/
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
    showLog("tag_isBC", tag + "_" + isBc);
    int filterBank = RFIDWithUHFUART.Bank_EPC;
    //68 =>substring(9) 80=>substring(12)
    int offSet = isBc ? 68/*80*/ : 32;
    int len = tag.length() * 4;
    //select tags that match the criteria
    try{
      //if(setFilter(filterBank, offSet, len, tag)){ //Previous Code
      if(setFilter(filterBank, offSet, len, tag.length() % 2 == 0 ? tag : tag + "0")){ //Code given by Chainway Person for applying Filter for Odd Length
        readTag();
      }
    }
    catch(Exception e){
      e.printStackTrace();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
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
    int filterBank = RFIDWithUHFUART.Bank_EPC;
    int offSet = 32;
    int len = tag.length() * 4;
    //select tags that match the criteria
    try{
      if(setFilter(filterBank, offSet, len, tag)){
        readTag();
      }
    }
    catch(Exception e){ e.printStackTrace();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
    }
  }
  
  @Override
  protected void addTidBasedFilters(String tid){
    // Add state aware pre-filter
    showLog("addTidBasedFilters", tid);
    int filterBank = RFIDWithUHFUART.Bank_TID;
    int offSet = 0;
    int len = tid.length() * 4;
    //select tags that match the criteria
    try{
      if(setFilter(filterBank, offSet, len, tid)){
        readTag();
      }
    }
    catch(Exception e){ e.printStackTrace();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
    }
  }
  
  private int getValueOld(float var1){
    float var2 = calculateRssiPercent(2, (float) ((double) (var1 + 100.0F - 20.0F) * 2.25D));
    if(var2 > 100.0F){
      var2 = 100.0F;
    }
    else if(var2 < 1.0F){
      var2 = 1.0F;
    }
    
    return (int) var2;
  }
  
  private void startLocation(final String epcData){
    mReader.startLocation(context, epcData, IUHF.Bank_EPC, 32, new IUHFLocationCallback(){
      @Override
      public void getLocationValue(int i, boolean b){
        if(b){
          showLog("loc_percentValue", b + "" + i);
        }
        else showLog("loc_percentValue", b + "" + i);
      }
    });
  }
  
  private void startRadar(final String epcData, final int filterBank, final int offset){
    //startLocation(epcData);
    if(mReader != null && isConnected() && !isSearchOn.getValue()){
      try{
        IUHFRadarLocationCallback radarLocationCallback = new IUHFRadarLocationCallback(){
          @Override
          public void getLocationValue(List<RadarLocationEntity> list){
            showLog("rle_list", "" + (list==null?"null":list.size()));
            if(isNonEmpty(list)){
              RadarLocationEntity rle = list.get(0);
              if(rle != null){
                showLog("rle_angle1", "" + rle.getAngle());
                showLog("rle_value", "" + rle.getValue());
              /*showLog("rle_tag", "" + rle.getTag());
              showLog("rle_uhfBank", "" + rle.getUhfBank());*/
              }
            }
          }
          
          @Override
          public void getAngleValue(int i){
            showLog("rle_angleValue", "" + i);
          }
        };
        showLog("radarLocationCallback",""+(radarLocationCallback!=null));
        if(radarLocationCallback != null){
          boolean result = mReader.startRadarLocation(context, isDebugApp ? "30361F5FF4145D174876E809" : epcData, filterBank, isDebugApp ? 32 : offset, radarLocationCallback);
          isSearchOn.postValue(result);
          showLog("result", "" + result);
        }
      }
      catch(Throwable e){
        e.printStackTrace();
        AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
      }
    }
  }
  
  //@Override
  //public boolean performPick(final String findBarcode){ return performPick(findBarcode,false);}
  @Override
  public boolean performPick(final String findBarcode, final boolean isDecodeOnPick, final Integer pickPower, final boolean isPostPicked, final List<String> listEpcs){
    final boolean isPerformPick = super.performPick(findBarcode, isDecodeOnPick, pickPower, isPostPicked, listEpcs);
    if(isPerformPick){
      pickTags.clear();
      pickUHFTags.clear();
      isPickOn.postValue(false);
      isActionPick = false;
      startPick(findBarcode, isDecodeOnPick, isPostPicked);
    }
    return isPerformPick;
  }
  
  //@Override
  //protected void startPick(final String findBarcode){ startPick(findBarcode,false);}
  @Override
  protected void startPick(final String findBarcode, final boolean isDecodeOnPick, final boolean isPostPicked){
    try{
      sessionAction = AppCommonMethods.SessionAction.PICK;
      int maxPower = chkZero(pickPower, sessionType == AppCommonMethods.SessionType.MOVEMENT ? MAX_POWER_TO_SET / 2 : MIN_POWER_TO_SET * 2);
      int power = /*isActionTidPick? MAX_POWER_TO_SET: */sessionType == AppCommonMethods.SessionType.MOVEMENT ? chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower) : maxPower;
      if(Build.MODEL.equalsIgnoreCase("C71")) { maxPower +=5; power +=5; }
      if(power > maxPower){
        power = maxPower;
      }
      showLog("pickPower", "" + (pickPower != null ? "" + pickPower : "null"));
      showLog("maxPower", "" + maxPower);
      showLog("power", "" + power);
      setSession("S0", "A");
      setTagFocus(false);
      setFastID(false);
      clearFilters();
      if(/*getPower() == power || */setPower(power)){
        if(setEPCAndTIDMode()){
          //if(isActionTidPick) addTIDBasedfilters(SCANNED_TID);
          isActionPick = true;
          isPickOn.postValue(true);
          SEARCH_BARCODE = findBarcode;
          readTag();
          if(loopFlag && isSinglePick){
            //setProgressMessage(context.getString(R.string.msg_pick), true);
            //pickCountDownTimer = new CountDownTimer(sessionType.equals(AppCommonMethods.SessionType.ENCODING) || sessionType.equals(AppCommonMethods.SessionType.ENCODING_THAN) || sessionType.equals(AppCommonMethods.SessionType.DECODING) ? pickCountDownTime / 4 : pickCountDownTime, pickCountDownTime){
            //Set More Pick Time for Than Encoding (Configurable)
            pickTimer = new Timer();
            pickTimer.schedule(new TimerTask(){
              @Override
              public void run(){
                showLog("onFinish", "onFinish");
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
                  handler.post(new Runnable(){
                    @Override
                    public void run(){
                      final UHFTAGInfo tagData = new ArrayList<UHFTAGInfo>(pickUHFTags).get(0);
                      final String epcdt = chkNull(tagData.getEPC(), "");
                      final String tid = chkNull(tagData.getTid(), "");
                      setProgressMessage(false);
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
                      else if(sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING_THAN && sessionType != AppCommonMethods.SessionType.DECODING && (inventoryDao.isEPCPresent(sessionId, epcdt) || (isNonEmpty(pickedEpcs) && pickedEpcs.contains(epcdt)))){
                        String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING);
                        if(isNonEmpty(barcode) && !barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && sessionType != AppCommonMethods.SessionType.SCAN && sessionType != AppCommonMethods.SessionType.VERIFY_ENCODING)
                          context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag_for_barcode), getTypeCharCode(), barcode));
                        else
                          context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
                      }
                      else{
                        boolean isMatchingBarcode = false;
                        //TODO
                        //final String epc = ((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && chkNull(epcdt, "").length() > 2) ? chkNull(epcdt, "").startsWith("0C") ? epcdt.replaceFirst("0C", "BC") : chkNull(epcdt, "").startsWith("05") ? epcdt.replaceFirst("05", "35") : chkNull(epcdt, "").startsWith("00") ? (epcdt.length() >= 32) ? epcdt.replaceFirst("00", "BC") : (epcdt.length() >= 24) ? epcdt.replaceFirst("00", "35") : epcdt : epcdt : epcdt;
                        //final String epc = ((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && chkNull(epcdt, "").length() > 2) ? chkNull(epcdt, "").startsWith("0C") ? epcdt.replaceFirst("0C", "BC") : chkNull(epcdt, "").startsWith("00") ? (epcdt.length() >= 32) ? epcdt.replaceFirst("00", "BC") : (epcdt.length() >= 24) ? epcdt.replaceFirst("00", "30") : epcdt : epcdt : epcdt;
                        //final String header = epc.length() > 2 ? epc.substring(0, 2) : "";
                        String matchingBarcode = "";
                        //showLog("pick_finish_epc_header", header);
                        
                        //check by using getBarcode method instead of switch case
                        String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING);
                        String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context, findBarcode);
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
                        showLog("isMatchingBarcode", "" + isMatchingBarcode);
                        
                        if((isMatchingBarcode /*&& isNonEmpty(matchingBarcode)*/) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING){
                          readTid = true;
                          readRssi = true;
                          readEAN = true;
                          readPC = true;//sessionType == AppCommonMethods.SessionType.ENCODING;// || sessionType == AppCommonMethods.SessionType.DECODING;
                          if(isPostPicked || sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING){
                            final Inventory pickedTag = getDataFromTagInfo(tagData);
                            if((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && isMatchingBarcode && isNonEmpty(matchingBarcode))
                              pickedTag.ean = AppCommonMethods.getLeftZeroReplacedString(context, matchingBarcode);
                            if(isNullOrEmpty(matchingBarcode) && sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING_THAN && (sessionType != AppCommonMethods.SessionType.DECODING || !SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_NON_ENCODED_TAG_PICK_FOR_DECODE, AppCommonMethods.isAllowNonEncodedTagPickForDecode)))
                              context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                            else if((sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING) && (pickedTag == null || isNullOrEmpty(pickedTag.epc) || isNullOrEmpty(pickedTag.tid)))
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
                    }});
                }
              }
            },sessionType == AppCommonMethods.SessionType.ENCODING || (sessionType.equals(AppCommonMethods.SessionType.ENCODING_THAN) && !SharedPrefManager.getBoolean(ParamConstants.IS_SET_EXTRA_PICK_TIME_FOR_THAN_ENC, AppCommonMethods.isSetExtraPickTimeForThanEncoding)) || sessionType == AppCommonMethods.SessionType.DECODING ? pickCountDownTime / 4 : pickCountDownTime);
            /*pickCountDownTimer = new CountDownTimer(sessionType.equals(AppCommonMethods.SessionType.ENCODING) || (sessionType.equals(AppCommonMethods.SessionType.ENCODING_THAN) && !SharedPrefManager.getBoolean(ParamConstants.IS_SET_EXTRA_PICK_TIME_FOR_THAN_ENC, AppCommonMethods.isSetExtraPickTimeForThanEncoding)) || sessionType.equals(AppCommonMethods.SessionType.DECODING) ? pickCountDownTime / 4 : pickCountDownTime, pickCountDownTime){
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
                else if(pickTags.size() > 1){
                  setProgressMessage(false);
                  context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
                }
                else if(pickTags.size() == 1){
                  handler.post(new Runnable(){
                  @Override
                  public void run(){
                    final UHFTAGInfo tagData = new ArrayList<UHFTAGInfo>(pickUHFTags).get(0);
                    final String epcdt = chkNull(tagData.getEPC(), "");
                    final String tid = chkNull(tagData.getTid(), "");
                    setProgressMessage(false);
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
                    else if(sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING_THAN && sessionType != AppCommonMethods.SessionType.DECODING && (inventoryDao.isEPCPresent(sessionId, epcdt) || (isNonEmpty(pickedEpcs) && pickedEpcs.contains(epcdt)))){
                      String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING);
                      if(isNonEmpty(barcode) && !barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && sessionType != AppCommonMethods.SessionType.SCAN && sessionType != AppCommonMethods.SessionType.VERIFY_ENCODING)
                        context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag_for_barcode), getTypeCharCode(), barcode));
                      else
                        context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
                    }
                    else{
                      boolean isMatchingBarcode = false;
                      //TODO
                      //final String epc = ((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && chkNull(epcdt, "").length() > 2) ? chkNull(epcdt, "").startsWith("0C") ? epcdt.replaceFirst("0C", "BC") : chkNull(epcdt, "").startsWith("05") ? epcdt.replaceFirst("05", "35") : chkNull(epcdt, "").startsWith("00") ? (epcdt.length() >= 32) ? epcdt.replaceFirst("00", "BC") : (epcdt.length() >= 24) ? epcdt.replaceFirst("00", "35") : epcdt : epcdt : epcdt;
                      //final String epc = ((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && chkNull(epcdt, "").length() > 2) ? chkNull(epcdt, "").startsWith("0C") ? epcdt.replaceFirst("0C", "BC") : chkNull(epcdt, "").startsWith("00") ? (epcdt.length() >= 32) ? epcdt.replaceFirst("00", "BC") : (epcdt.length() >= 24) ? epcdt.replaceFirst("00", "30") : epcdt : epcdt : epcdt;
                      //final String header = epc.length() > 2 ? epc.substring(0, 2) : "";
                      String matchingBarcode = "";
                      //showLog("pick_finish_epc_header", header);
                      
                      //check by using getBarcode method instead of switch case
                      String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING);
                      String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context, findBarcode);
                      showLog("pick_barcode", barcode);
                      showLog("pick_compare_barcode", compbarcode);
                      final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                      if(!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase())) && (isNullOrEmpty(eans) || eans.contains(barcode)) && (sessionType != AppCommonMethods.SessionType.SEARCH_FIFO || fifoDao.isEPCPresent(epcdt, ean, fifoDate*//*,zone,zoneId*//*))){
                        isMatchingBarcode = true;
                        matchingBarcode = AppCommonMethods.getLeftZeroReplacedString(context, barcode);
                      }
                      else if(sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING_THAN && sessionType != AppCommonMethods.SessionType.DECODING){
                        if(isNullOrEmpty(barcode) || barcode.equalsIgnoreCase(AppConstants.UNKNOWN) || barcode.equalsIgnoreCase(AppConstants.NON_ENCODED))
                          context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                        else if(sessionType == AppCommonMethods.SessionType.OFF_RANGE && isNullOrEmpty(findBarcode) && isNonEmpty(eans) && isNonEmpty(barcode) && !eans.contains(barcode))
                          context.showCustomErrDialog(String.format(String.format(context.getString(R.string.err_pick_not_present_tag)*//*,getTypeCharCode()*//*, barcode)));
                        else if(isNonEmpty(barcode))
                          context.showCustomAlertDialog(String.format(context.getString(R.string.err_pick_wrong_tag_header), getTypeCharCode()), String.format(context.getString(R.string.err_pick_wrong_tag)*//*,getTypeCharCode()*//*, barcode), context.getString(R.string.btn_ok), null);
                      }
                      showLog("isMatchingBarcode", "" + isMatchingBarcode);
                      
                      if((isMatchingBarcode *//*&& isNonEmpty(matchingBarcode)*//*) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING){
                        readTid = true;
                        readRssi = true;
                        readEAN = true;
                        readPC = true;//sessionType == AppCommonMethods.SessionType.ENCODING;// || sessionType == AppCommonMethods.SessionType.DECODING;
                        if(isPostPicked || sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING){
                          final Inventory pickedTag = getDataFromTagInfo(tagData);
                          if((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && isMatchingBarcode && isNonEmpty(matchingBarcode))
                            pickedTag.ean = AppCommonMethods.getLeftZeroReplacedString(context, matchingBarcode);
                          if(isNullOrEmpty(matchingBarcode) && sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING_THAN && (sessionType != AppCommonMethods.SessionType.DECODING || !SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_NON_ENCODED_TAG_PICK_FOR_DECODE, AppCommonMethods.isAllowNonEncodedTagPickForDecode)))
                            context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                          else if((sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING) && (pickedTag == null || isNullOrEmpty(pickedTag.epc) || isNullOrEmpty(pickedTag.tid)))
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
                  }});
                }
              }
            };
            pickCountDownTimer.start();*/
          }
        }
      }
    }
    catch(Exception e){
      e.printStackTrace();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast)
        showShortToast(context, R.string.err_reader_fail);
    }
  }
  
  private void setTagFocus(Boolean isSetTagFocus) {
    showLog("SetTagFocus:",""+isSetTagFocus);
    boolean result = false;
    if (reader != null) result = reader.setTagFocus(isSetTagFocus);
    else if (mReader != null) result = mReader.setTagFocus(isSetTagFocus);
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"setTagFocus("+isSetTagFocus+") -> "+result);
  }
  
  private void setFastID(Boolean isSetFastID) {
    showLog("setFastID:",""+isSetFastID);
    boolean result = false;
    if (reader != null) result= reader.setFastID(isSetFastID);
    else if (mReader != null) result = mReader.setFastID(isSetFastID);
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"setFastID("+isSetFastID+") -> "+result);
  }
  
  private void setInventoryCallback(IUHFInventoryCallback inventoryCallback){
    if(reader != null) reader.setInventoryCallback(inventoryCallback);
    if(mReader != null) mReader.setInventoryCallback(inventoryCallback);
    //AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"setInventoryCallback -> ");
  }
  
  private void setKeyEventCallback(){
    setKeyEventCallback(new KeyEventCallback(){
      @Override
      public void onKeyDown(int keyCode){
        showLog("onKeyDown", "" + keyCode);
        if(keyCode == 1){
          setTriggerPressed();//.postValue(true);
        }
      }
      
      @Override
      public void onKeyUp(int keyCode){
        showLog("onKeyUp", "" + keyCode);
      }
    });
  }
  
  private void setKeyEventCallback(KeyEventCallback keyEventCallback){
    if(reader != null) reader.setKeyEventCallback(keyEventCallback);
    //if(mReader != null) mReader.setKeyEventCallback(keyEventCallback);
    //AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"setKeyEventCallback -> ");
  }
  
  private void setConnectionStatusCallback(ConnectionStatusCallback<Object> connectionStatusCallback){
    if(reader != null) reader.setConnectionStatusCallback(connectionStatusCallback);
    if(mReader != null) mReader.setConnectionStatusCallback(connectionStatusCallback);
    //AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"setConnectionStatusCallback -> ");
  }
  
  private boolean initReader(){
    showLog(TAG_LOG, "initReader called");
    isInit = reader != null ? reader.init(context) : mReader != null ? mReader.init(context) : false;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"init -> "+isInit);
    return isInit;
  }
  
  private Gen2Entity getGen2(){
    Gen2Entity result = reader != null ? reader.getGen2() : mReader != null ? mReader.getGen2() : null;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"getGen2() -> "+(result!=null));
    return result;
  }
  
  private boolean setGen2(Gen2Entity gen2Entity){
    boolean result = reader != null ? reader.setGen2(gen2Entity) : mReader != null ? mReader.setGen2(gen2Entity) : false;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"setGen2(...) -> "+result);
    return result;
  }
  
  private int getPower(){
    int result =  reader != null ? reader.getPower() : mReader != null ? mReader.getPower() : -1;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"getPower -> "+result);
    return result;
  }
  
  private boolean setPower(int power){
    boolean result = reader != null ? reader.setPower(power) : mReader != null ? mReader.setPower(power) : false;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"setPower("+power+") -> "+result);
    return result;
  }
  
  private boolean setEPCAndTIDMode(){
    boolean result =  reader != null ? reader.setEPCAndTIDMode() : mReader != null ? mReader.setEPCAndTIDMode() : false;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"setEPCAndTIDMode -> "+result);
    return result;
  }
  
  private boolean setEPCMode(){
    boolean result =  reader != null ? reader.setEPCMode() : mReader != null ? mReader.setEPCMode() : false;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"setEPCAndTIDMode -> "+result);
    return  result;
  }
  
  private boolean setFilter(int filterBank, int offSet, int len, String filterData){
    boolean result = reader != null ? reader.setFilter(filterBank, offSet, len, filterData) : mReader != null ? mReader.setFilter(filterBank, offSet, len, filterData) : false;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"setFilter -> "+result);
    return result;
  }
  
  private ConnectionStatus getConnectStatus(){
    ConnectionStatus result = reader != null ? reader.getConnectStatus() : mReader != null ? mReader.getConnectStatus() : ConnectionStatus.DISCONNECTED;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"getConnectStatus -> "+result.name());
    return result;
  }
  
  private boolean writeEpc(String password, String tid, String sgtin){
    switch(sgtin.length()){
      case 28:
      case 36:
      case 24:
      case 32:
        int dataLen = sgtin.length() / 4;
        int offSet = (dataLen % 2 == 0 ? 2 : 1);
        return writeEpc(password, tid, offSet, dataLen, sgtin);
    }
    return false;
  }
  
  private boolean writeEpc(String password, String tid, int offSet, int dataLen, String sgtin){
    return writeData(password, tid, RFIDWithUHFUART.Bank_EPC, offSet, dataLen, sgtin);
  }
  
  private boolean writeReserved(String password, String tid, String newPassword){
    return writeReserved(password, tid, 2, 2, newPassword);
  }
  
  private boolean writeReserved(String password, String tid, int offSet, int dataLen, String newPassword){
    return writeData(password, tid, RFIDWithUHFUART.Bank_RESERVED, offSet, dataLen, newPassword);
  }
  
  private String readReserved(String password, String tid){
    return readData(password, tid, RFIDWithUHFUART.Bank_RESERVED, 2, 2);
  }
  
  private String readData(String password, String tid, int readFilterBank, int readOffSet, int readDataLen){
    return readData(password, RFIDWithUHFUART.Bank_TID, 0, 96, tid, readFilterBank, readOffSet, readDataLen);
  }
  
  private String readData(String password, int filterBank, int filterOffset, int filterDataLen, String filterData, int readFilterBank, int readOffSet, int readDataLen){
    String result = reader != null ? reader.readData(password, filterBank, filterOffset, filterDataLen, filterData, readFilterBank, readOffSet, readDataLen) : mReader != null ? mReader.readData(password, filterBank, filterOffset, filterDataLen, filterData, readFilterBank, readOffSet, readDataLen) : null;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"readData(...) -> "+isNonEmpty(result));
    return result;
  }
  
  private boolean writeData(String password, String tid, int writeFilterBank, int writeOffSet, int writeDataLen, String writeData){
    return writeData(password, RFIDWithUHFUART.Bank_TID, 0, 96, tid, writeFilterBank, writeOffSet, writeDataLen, writeData);
  }
  
  private boolean writeData(String password, int filterBank, int filterOffset, int filterDataLen, String filterData, int writeFilterBank, int writeOffSet, int writeDataLen, String writeData){
    boolean result =  reader != null ? reader.writeData(password, filterBank, filterOffset, filterDataLen, filterData, writeFilterBank, writeOffSet, writeDataLen, writeData) : mReader != null ? mReader.writeData(password, filterBank, filterOffset, filterDataLen, filterData, writeFilterBank, writeOffSet, writeDataLen, writeData) : false;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"writeData(...) -> "+result);
    return result;
  }
  
  private boolean lockReservedMemory(String password, String tid){
    return lockMem(password, tid);
  }
  
  private boolean lockMem(String password, String tid){
    return lockMem(password, RFIDWithUHFUART.Bank_TID, 0, 96, tid);
  }
  
  private boolean lockMem(String password, int filterBank, int filterOffset, int filterLength, String filterData){
    boolean result =  reader != null ? reader.lockMem(password, RFIDWithUHFUART.Bank_TID, filterOffset, filterLength, filterData, "0280A0") : mReader != null ? mReader.lockMem(password, RFIDWithUHFUART.Bank_TID, 0, 96, filterData, "0280A0") : false;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"lockMem(...) -> "+result);
    return result;
  }
  
  private boolean startInventoryTag(){
    InventoryParameter inventoryParameter = null;
    if(isDebugApp && isCommandForSearch && !isCommandForTIDSearch){
      inventoryParameter = new InventoryParameter();
      inventoryParameter.setResultData(new InventoryParameter.ResultData().setNeedPhase(true));
    }
    boolean result = reader != null ? inventoryParameter!=null?reader.startInventoryTag(inventoryParameter):reader.startInventoryTag() : mReader != null ? inventoryParameter!=null?mReader.startInventoryTag(inventoryParameter):mReader.startInventoryTag() : false;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"startInventoryTag(...) -> "+result);
    return result;
  }
  
  private boolean stopInventoryTag(){
    //temp code
   /* if(isDebugApp && mReader!=null) mReader.stopRadarLocation();
    if(isDebugApp && mReader!=null) mReader.stopLocation();*/
    boolean result =  reader != null ? reader.stopInventory() : mReader != null ? mReader.stopInventory() : false;
    AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"stopInventory(...) -> "+result);
    return result;
  }
  
  @Override
  public boolean performTidPick(final String findBarcode, final List<String> tids){
    final boolean isPerformTidPick = super.performTidPick(findBarcode, tids);
    if(isPerformTidPick){
      pickTags.clear();
      pickUHFTags.clear();
      isPickOn.postValue(false);
      isActionPick = false;
      isActionTidPick = false;
      SCANNED_TIDS.clear();
      startTidPick(findBarcode, tids);
    }
    return isPerformTidPick;
  }
  
  @Override
  protected void startTidPick(final String findBarcode, final List<String> tids){
    try{
      sessionAction = AppCommonMethods.SessionAction.PICK;
      isActionTidPick = isNonEmpty(tids);//chkNull(tid,"").trim().length()>0;
      AppCommonMethods.showLog("isActionTidPick", "" + isActionTidPick);
      int maxPower = sessionType == AppCommonMethods.SessionType.MOVEMENT ? MAX_POWER_TO_SET / 2 : MIN_POWER_TO_SET * 2;
      int power = isActionTidPick ? MAX_POWER_TO_SET : sessionType == AppCommonMethods.SessionType.MOVEMENT ? chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower) : maxPower;
      showLog("power", "" + power);
      setSession("S0", "A");
      setTagFocus(false);
      setFastID(false);
      clearFilters();
      if(/*getPower() == power || */setPower(power)){
        if(setEPCAndTIDMode()){
          //if(isActionTidPick && tids.size() == 1 && addTIDBasedfilters(tids.get(0)))
          //showLog("TID_FILTER_SET", SCANNED_TIDS.get(0));
          isActionPick = true;
          isPickOn.postValue(true);
          //SEARCH_BARCODE = findBarcode;
          SCANNED_TIDS.clear();
          SCANNED_TIDS.addAll(tids);
          readTag();
          if(loopFlag && tids.size() >= 1){
            //setProgressMessage(context.getString(R.string.msg_pick), true);
            pickTimer = new Timer();
            pickTimer.schedule(new TimerTask(){
              @Override
              public void run(){
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
                  for(UHFTAGInfo tagData : pickUHFTags){
                    final String epc = chkNull(tagData.getEPC(), "");
                    final String tid = chkNull(tagData.getTid(), "");
                    showLog("picked_tag", epc + "_" + tid);
                    Inventory pickedTag = null;
                    try{
                      pickedTag = getDataFromTagInfo(tagData);
                    }
                    catch(Exception e){ e.printStackTrace();
                      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
                    }
                    if(pickedTag != null && isNonEmpty(epc) && isNonEmpty(tid) && tids.contains(tid.length() > 24 ? tid.substring(0, 24) : tid)){
                      pickedList.add(pickedTag);
                      //pickData.postValue(pickedTag);
                      //storeInventoryData(tagData);
                    }
                  }
                  if(pickedList.size() > 1) pickedListData.postValue(pickedList);
                  else if(pickedList.size() == 1) pickData.postValue(pickedList.get(0));
                }
              }
            },sessionType == AppCommonMethods.SessionType.ENCODING || (sessionType.equals(AppCommonMethods.SessionType.ENCODING_THAN) && !SharedPrefManager.getBoolean(ParamConstants.IS_SET_EXTRA_PICK_TIME_FOR_THAN_ENC, AppCommonMethods.isSetExtraPickTimeForThanEncoding)) || sessionType == AppCommonMethods.SessionType.DECODING ? pickCountDownTime / 4 : pickCountDownTime);
            /*pickCountDownTimer = new CountDownTimer(tids.size() * 1000, 1000){
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
                  for(UHFTAGInfo tagData : pickUHFTags){
                    final String epc = chkNull(tagData.getEPC(), "");
                    final String tid = chkNull(tagData.getTid(), "");
                    showLog("picked_tag", epc + "_" + tid);
                    Inventory pickedTag = null;
                    try{
                      pickedTag = getDataFromTagInfo(tagData);
                    }
                    catch(Exception e){ e.printStackTrace(); }
                    if(pickedTag != null && isNonEmpty(epc) && isNonEmpty(tid) && tids.contains(tid.length() > 24 ? tid.substring(0, 24) : tid)){
                      pickedList.add(pickedTag);
                      //pickData.postValue(pickedTag);
                      //storeInventoryData(tagData);
                    }
                  }
                  if(pickedList.size() > 1) pickedListData.postValue(pickedList);
                  else if(pickedList.size() == 1) pickData.postValue(pickedList.get(0));
                }
              }
            };
            pickCountDownTimer.start();*/
          }
          else{
            stopInventory();
            if(AppCommonMethods.isShowReaderCommandFailToast)
              showShortToast(context, R.string.err_reader_fail);
          }
        }
      }
    }
    catch(Exception e){
      e.printStackTrace();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast)
        showShortToast(context, R.string.err_reader_fail);
    }
  }
  
  @Override
  public void stopInventory(){
    super.stopInventory();
    if(loopFlag){
      loopFlag = false;
      if(stopInventoryTag()){
        /*
         * Success*/
      }
    }
   /* handler.post(new Runnable(){
      @Override
      public void run(){
        if(pickTimer != null){
          pickTimer.cancel();
          pickTimer = null;
        }
    }});*/
  }
  
  /**
   * Read tag.
   */
  private void readTag(){
      if(!loopFlag){
        if(startInventoryTag()){
          loopFlag = true;
          if(isCommandForSearch) isSearchOn.postValue(true);
          else if(!isActionPick) isInventoryOn.postValue(true);
          startTimer();
          //new TagThread().start();
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
  }
  
  @Override
  public void configureReader(){ configureReader(null); }
  
  /**
   * Configure reader.
   *
   * @param sessionType the session type
   */
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
      int maxPower = type == AppCommonMethods.SessionType.SCAN || type == AppCommonMethods.SessionType.VERIFY_ENCODING || type == AppCommonMethods.SessionType.ENCODING || type == AppCommonMethods.SessionType.ENCODING_THAN ? MIN_POWER_TO_SET * 2 : type == AppCommonMethods.SessionType.MOVEMENT || type == AppCommonMethods.SessionType.INWARD || type == AppCommonMethods.SessionType.OUTWARD ? MAX_POWER_TO_SET / 2 : sessionType == AppCommonMethods.SessionType.INWARD_TOTE ? (int) (MIN_POWER_TO_SET * inwToteMinPowerMultiplier) : type == AppCommonMethods.SessionType.OUTWARD_TOTE ? (int) (MIN_POWER_TO_SET * owtToteMinPowerMultiplier) : type == AppCommonMethods.SessionType.OFF_RANGE ? MAX_POWER_TO_SET - (MIN_POWER_TO_SET * 2) : type == AppCommonMethods.SessionType.DECODING ? (int) (MIN_POWER_TO_SET * decodePickMinPowerMultiplier) : type == AppCommonMethods.SessionType.SER_EXCEL ? MIN_POWER_TO_SET : MAX_POWER_TO_SET;
      showLog("maxpower", type.name() + ":" + maxPower);
      int power = sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD ? (int) (MIN_POWER_TO_SET * inwDefPowerMultiplier) : sessionType == AppCommonMethods.SessionType.INWARD_TOTE ? 8 : sessionType == AppCommonMethods.SessionType.DECODING ? (int) (MIN_POWER_TO_SET * decodePickMinPowerMultiplier) : chkZero(SharedPrefManager.getInt(type.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower);
      SharedPrefManager.setInt(type.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), power);
      showLog("config power", type.name() + ":" + power);
      setReaderPower(power);
      setEPCMode();
      setSession("S0", "A");
      setTagFocus(false);
      setFastID(false);
      clearFilters();
      showLog(TAG_LOG, "CONFIG SUCCESS");
      isDeviceConfigured.postValue(true);
      setProgressMessage(false);
      rfidInterface.RFIDInitializationStatus(true, "", mReader);
    }
    else setProgressMessage(false);
  }
  
  @Override
  public boolean isReaderConnected(){
    if (!isInit) return false;
    if(chkNull(reader, mReader) != null) showLog("status", "" + getConnectStatus());
    return chkNull(reader, mReader) != null && (reader == null || isConnected())/*&& getConnectStatus()==ConnectionStatus.CONNECTED*/;
    //code changes for 'Tag Not Found' - 26-07-2023
    //return chkNull(reader,mReader) != null && (getConnectStatus()==ConnectionStatus.CONNECTING || getConnectStatus()==ConnectionStatus.CONNECTED);
  }
  
  private boolean isConnected(){
    return chkNull(reader, mReader) != null && getConnectStatus() == ConnectionStatus.CONNECTED;
  }
  
  @Override
  public boolean isReaderPresent(boolean isReaderInstanceSet){
    return chkNull(reader, mReader) != null || isReaderInstanceSet;
  }
  
  /**
   * On resume.
   *
   * @param sessionType the session type
   */
  @Override
  public void onResume(AppCommonMethods.SessionType sessionType){
    super.onResume(sessionType);
    if(!isInit) init();
    else configureReader(sessionType);
    //InitSDK(true);
    //init();
    /*connect();
    if(isBluetoothReader ? reader == null : mReader == null){
      InitSDK(true);//new InitTask(true).execute();
    }
    else if(getConnectStatus() == ConnectionStatus.DISCONNECTED){
      InitSDK(true);//new InitTask(true).execute();
    }
    else if(sessionType.getValue() > 0) configureReader(sessionType);*/
  }
  
  /**
   * On pause.
   */
  @Override
  public void onPause(){
    super.onPause();
    //disconnect();
    if(isInit)
    /* new Handler(Looper.getMainLooper()).post(new Runnable(){
      @Override
      public void run()*/{
        boolean result = false;
        try{
          if(beepTimer != null) beepTimer.cancel();
          if(reader != null){
            isInit = false;
            result = reader.free();
            showLog("reader", "free!");
            AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"free -> "+result);
          }
          if(mReader != null){
            isInit = false;
            result = mReader.free();
            showLog("mReader", "free!");
            AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,"free -> "+result);
          }
        }catch(Exception e){e.printStackTrace();
          AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
        }
     }
      // });
  }
  
  @Override
  public synchronized void setReaderPower(final int power){
    final int oldPower = getPower();
    setProgressMessage("Setting Reader Power...", true);
    if(isReaderConnected() && power >= MIN_POWER_TO_SET && power <= MAX_POWER_TO_SET){
      new Handler(Looper.getMainLooper()).post(() -> handleReaderPowerCommand(oldPower, power));
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
   * Handle reader power command.
   *
   * @param oldPower the old power
   * @param newPower the new power
   */
  private void handleReaderPowerCommand(int oldPower, int newPower){
    try{
      showLog("old & new power", "Old:" + oldPower + " New:" + newPower);
      if(setPower(newPower)){
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
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
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
  private void storeInventoryData(final UHFTAGInfo uhftagInfo){
    try{
      if(isNonEmpty(sessionId) && sessionType.getValue() > 0){
        final String epcdt = chkNull(uhftagInfo.getEPC(), "");
        if(!acceptInventoryRead(epcdt, chkNull(uhftagInfo.getTid(), ""))) return;
        /*if(isCommandForSearch || sessionAction == AppCommonMethods.SessionAction.SEARCH){
          String epc = uhftagInfo.getEPC();
          showLog("epc", "" + epc);
          final int rssi = (int) Math.round(Double.parseDouble(uhftagInfo.getRssi()));
          showLog("rssi", "" + uhftagInfo.getRssi());
          int actualPercentage = getPercentage(rssi);
          percent = actualPercentage;
          showLog("actualPer", "" + actualPercentage);
          if(!isCommandForEPCSearch && !isCommandForTIDSearch){
            showLog("SEARCH_LOCKED_EPC", isLockSearchEPC + "_" + SEARCH_LOCKED_EPC);
            if(isLockSearchEPC && isNullOrEmpty(SEARCH_LOCKED_EPC)) SEARCH_LOCKED_EPC = epc;
            if(!isLockSearchEPC || isNullOrEmpty(SEARCH_LOCKED_EPC) || epc.equalsIgnoreCase(SEARCH_LOCKED_EPC)){
              if(epc.length() >= 24){
                //check by using getBarcode method instead of switch case
                final String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epc);
                final String compbarcode = AppCommonMethods.getLeftZeroReplacedString(SEARCH_BARCODE);
                showLog("search_barcode", barcode);
                showLog("search_compare_barcode", compbarcode);
                if(!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase()))){
                  showLog("RFIDHANDLER_barcode", "" + barcode);
                  searchPercent.postValue(actualPercentage);
                  searchRssi.postValue(String.valueOf(uhftagInfo.getRssi()));
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
            if(epc.equalsIgnoreCase(SEARCH_EPC)){
              if(sessionType == AppCommonMethods.SessionType.ENCODING || rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue()){
                if(!inventoryDao.isVerifiedByEpc(sessionId, epcdt)){
                  Integer result = inventoryDao.updateEncVerifiedByEpc(sessionId, epcdt);
                  showLog("updateEncVerifiedByEpc", "" + result);
                  if(result > 0) uploadInventoryDao.updateEncVerifiedByEpc(sessionId, epcdt);
                }
              }
              searchPercent.postValue(actualPercentage);
              searchRssi.postValue(uhftagInfo.getRssi());
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
            final String tid = readTid ? uhftagInfo.getTid() : "";
            showLog("tid_search", "" + (tid + "_" + SEARCH_TID));
            if(tid.equalsIgnoreCase(SEARCH_TID)){
              searchPercent.postValue(actualPercentage);
              searchRssi.postValue(String.valueOf(uhftagInfo.getRssi()));
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
        /*else if(isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK){
          showLog("epc_pick", "" + epcdt);
          String tid = uhftagInfo.getTid();
          if(tid.length() > 24) tid = tid.substring(0, 24);
          if(isActionTidPick) showLog("tid_pick", "" + tid);
          if(sessionType == AppCommonMethods.SessionType.ENCODING || epcdt.length() >= 24){
            if(isSinglePick && ((!isActionTidPick && !pickTags.contains(epcdt)) || (isActionTidPick && !pickTags.contains(tid) && SCANNED_TIDS.contains(tid) *//*uhftagInfo.getTid().matches("(?i)(^" + SCANNED_TID + ".*$)")*//*))){
              pickTags.add(isActionTidPick ? uhftagInfo.getTid() : uhftagInfo.getEPC());
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
          if(isSinglePick) return;
        }*/
        //else{
        Inventory inventory = new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
        inventory.epc = uhftagInfo.getEPC().toUpperCase();
        inventory.tid = readTid || (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST || sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE || sessionType == AppCommonMethods.SessionType.INWARD_TOTE ? /*AppCommonMethods.isUse24LengthTIDForUpload && chkNull(uhftagInfo.getTid(),"").length()>24?uhftagInfo.getTid().substring(0,24):*/uhftagInfo.getTid() : "";
        inventory.rssi = readRssi ? uhftagInfo.getRssi() : "";
        inventory.pcdata = readPC ? uhftagInfo.getPc() : "";
        inventory.zone = zone;
        inventory.zoneId = zoneId;
        try{
          inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
        }
        catch(Exception e){ e.printStackTrace();
          AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
        }
        inventory.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
        if(/*!AppCommonMethods.isSetInwOnline &&*/ (sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD)){
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
            enqueueTripInventoryWrite(tripInventory);
        }
        else{
          if(isNullOrEmpty(sessionId) || (isNullOrEmpty(inventory.tid) && (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST || sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE || sessionType == AppCommonMethods.SessionType.INWARD_TOTE)))
            throw new NullPointerException();
          enqueueInventoryWrite(inventory);
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
          if(productDao != null && sessionType == AppCommonMethods.SessionType.OFF_RANGE)
            productDao.updateFoundEPCOffRange(inventory.epc, inventory.ean, zone);
          if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO && fifoDate != null && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK))
            fifoDao.updateFound(inventory.ean, inventory.epc, fifoDate);
          if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY)// || sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY)
            AppDatabase.getBrandEansDao(context).updateScanQty("," + inventory.ean + ",");
        }
        //}
      }
    }
    catch(SQLiteConstraintException e){
      e.printStackTrace();
    }
    catch(Exception e){ e.printStackTrace();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
    }
  }
  
  @Override
  protected Inventory getDataFromTagInfo(Object object){
    return (object != null && object instanceof UHFTAGInfo) ? getDataFromTagInfo((UHFTAGInfo) object) : new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
  }
  
  /**
   * Get data from tag info inventory.
   *
   * @param uhftagInfo the uhftag info
   * @return the inventory
   */
  private Inventory getDataFromTagInfo(UHFTAGInfo uhftagInfo){
    Inventory inventory = new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
    inventory.epc = uhftagInfo.getEPC().toUpperCase();
    inventory.tid = readTid ? /*AppCommonMethods.isUse24LengthTIDForUpload && chkNull(uhftagInfo.getTid(),"").length()>24?uhftagInfo.getTid().substring(0,24):*/uhftagInfo.getTid() : "";
    inventory.rssi = readRssi ? uhftagInfo.getRssi() : "";
    inventory.pcdata = readPC ? uhftagInfo.getPc() : "";
    inventory.zone = zone;
    inventory.zoneId = zoneId;
    try{
      inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
    }
    catch(Exception e){ e.printStackTrace();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
    }
    return inventory;
  }
  
  /**
   * Perform encoding.
   *
   * @param pickedTags  the picked tags
   * @param sessionType the session type
   */
  @Override
  public synchronized void performEncoding(final List<Inventory> pickedTags, final AppCommonMethods.SessionType sessionType){
    showLog("pickedTags", "" + pickedTags.size());
    showLog("sessionType", "" + sessionType.name());
    if(isNonEmpty(pickedTags) && sessionType.getValue() > 0){
      ChainwayRFIDHandler.this.sessionType = sessionType;
      sessionAction = AppCommonMethods.SessionAction.ENCODE;
      multiWriteSuccessCount = 0;
      multiWriteListSize = pickedTags.size();
      multiWriteCount = multiWriteListSize;
      isMultiWriteDone = false;
      if(setPower(MAX_POWER_TO_SET / 2)){
        clearFilters();
        isEncodeOn.postValue(true);
        //setProgressMessage(context.getString(R.string.msg_pick), true);
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
  
  /**
   * Perform encoding.
   *
   * @param pickedTag the picked tag
   */
  @Override
  public synchronized void performEncoding(final Inventory pickedTag, final String currentTagPassword){
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
    //TODO check this condition only for Monza R6P tags (tid => E2801170)
    if(isMultiEncode || setPower(MAX_POWER_TO_SET / 2)){//(pickTag.tid.toUpperCase().startsWith("E2801170")?2:1))){
      if(!isMultiEncode){
        clearFilters();
        isEncodeOn.postValue(true);
        isMultiWriteDone = false;
        //setProgressMessage(context.getString(R.string.msg_pick), true);
      }
      handler.post(new Runnable(){
        @Override
        public void run(){
          startEncoding(pickTag, currentTagPassword, 0);
        }
      });
    }
  }
  
  @Override
  public void readTagCurrentPassword(Inventory pickedTag){
  
  }
  
  /*@Override
  public void configureSessionAction(final AppCommonMethods.SessionAction sessionAction){
    switch(sessionAction){
      case INVENTORY:
        break;
      case SEARCH:
        readRssi = true;
        break;
      case PICK:
        readEAN = true;
        readTid = true;
        break;
      case ENCODE:
        readEAN = true;
        readTid = true;
        readPC = true;
        break;
      case DECODE:
        readEAN = true;
        readTid = true;
        readPC = true;
        break;
      default:
        break;
    }
  }*/
  
  private void clearFilters(){
    //Old Code (Commented)
    /*if(setFilter(1, 32, 0, "00"))
      showLog("clearFilters","cleared");
    else
      showLog("clearFilters","failed");*/
    
    //New Code
    try{
      if(chkNull(reader, mReader) != null && setFilter(RFIDWithUHFUART.Bank_EPC, 0, 0, "") && setFilter(RFIDWithUHFUART.Bank_TID, 0, 0, ""))
        showLog("clearFilters", "cleared");
      else showLog("clearFilters", "failed");
    }
    catch(Exception e){
      e.printStackTrace();
      AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
    }
  }
  
  //private void startEncoding(final Inventory pickedTag){
  private synchronized void startEncoding(final Inventory pickedTag, final String currentTagPassword, final int encodeRetryCount){
    showLog("startEncoding", currentTagPassword + "_" + encodeRetryCount);
    final String barcode = pickedTag.ean;
    final String epc = pickedTag.epc;
    final String tid = pickedTag.tid;
    final String sgtin = pickedTag.newEpc.trim();
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
        dataLen = sgtin.length() / 4;
        offSet = (dataLen % 2 == 0 ? 2 : 1);
    }
    showLog("dataLen", "" + dataLen);
    showLog("offSet", "" + offSet);
    showLog("currentAccessPassword", "" + SharedPrefManager.getCurrentAccessPassword());
    if(offSet > 0 && dataLen > 0){
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
          if(isNonEmpty(pass) && writeData(pass, RFIDWithUHFUART.Bank_TID, 0, 96, tid, RFIDWithUHFUART.Bank_EPC, offSet, dataLen, sgtin)){
            showLog("WriteSuccess", "true");
            //insert Data in table.
            isWriteSuccess = true;
            break;
          }
        }
        if(isWriteSuccess){
          try{
            pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
            pickedTag.isUploaded = false;
            pickedTag.writeFailReason = null;
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
            else{
              if(inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
                inventoryDao.updateInventoryData(pickedTag);
              else inventoryDao.insertInventoryData(pickedTag);
            }
            try{
              uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
            }
            catch(Exception e){ e.printStackTrace();
              AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
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
          catch(SQLiteConstraintException e){ e.printStackTrace(); }
          catch(Exception e){
            e.printStackTrace();
            AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
          }
        }
        else if(!isWriteSuccess){
          showLog("LOCKMEMORY1", "FAIL");
          if(inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
            inventoryDao.updateInventoryData(pickedTag);
          else inventoryDao.insertInventoryData(pickedTag);
          
          if(encodeRetryCount <= encodeRetryLimit)
            startEncoding(pickedTag, currentTagPassword, encodeRetryCount + 1);
          else updateTagWriteCount(context.getString(R.string.err_encoding_auth_fail));
        }
      }
      else if(writeData(SharedPrefManager.getCurrentAccessPassword(), RFIDWithUHFUART.Bank_TID, 0, 96, tid, RFIDWithUHFUART.Bank_EPC, offSet, dataLen, sgtin)){
        showLog("WriteSuccess", "true");
        //insert Data in table.
        try{
          pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
          pickedTag.isUploaded = false;
          pickedTag.writeFailReason = null;
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
              AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
            }
          }
          else{
            if(inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
              inventoryDao.updateInventoryData(pickedTag);
            else inventoryDao.insertInventoryData(pickedTag);
          }
          try{
            uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
          }
          catch(Exception e){ e.printStackTrace();
            AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
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
        catch(SQLiteConstraintException e){ e.printStackTrace(); }
        catch(Exception e){
          e.printStackTrace();
          AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
        }
      }
      else if(isNonEmpty(currentTagPassword)){
        if(encodeRetryCount <= encodeRetryLimit){
          startEncoding(pickedTag, currentTagPassword, encodeRetryCount + 1);
        }
        else{
          //((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
          //isEncodeOn.postValue(false);
          //setProgressMessage(false);
          updateTagWriteCount(context.getString(R.string.err_encoding_auth_fail));
          showLog("RETRY_LIMIT_REACHED", "FAIL");
        }
      }
      else{
        List<String> passwords = SharedPrefManager.getOldAccessPasswords();
        if(passwords.contains(defaultTagZeroPassword)) passwords.remove(defaultTagZeroPassword);
        passwords.add(0, defaultTagZeroPassword);
        boolean isPasswordFound = false;
        //for(String oldPassword : passwords){
        for(int i = 0; i < passwords.size(); i++){
          final String oldPassword = passwords.get(i);
          if(isNonEmpty(oldPassword)){
            showLog("oldPassword", oldPassword);
            //final String reservedMemoryPassword = reader != null ? reader.readData(oldPassword, RFIDWithUHFUART.Bank_TID, 0, 96, tid, RFIDWithUHFUART.Bank_RESERVED, 2, 2) : mReader.readData(oldPassword, RFIDWithUHFUART.Bank_TID, 0, 96, tid, RFIDWithUHFUART.Bank_RESERVED, 2, 2);
            //final String reservedMemoryPassword = readData(oldPassword, RFIDWithUHFUART.Bank_TID, 0, 96, tid, RFIDWithUHFUART.Bank_RESERVED, 2, 2);
            //final String reservedMemoryPassword = readData(oldPassword, tid, RFIDWithUHFUART.Bank_RESERVED, 2, 2);
            final String reservedMemoryPassword = readReserved(oldPassword, tid);
            if(reservedMemoryPassword != null){
              showLog("reservedMemoryPassword", reservedMemoryPassword);
              if(reservedMemoryPassword.equalsIgnoreCase(oldPassword)){
                isPasswordFound = true;
                if(reservedMemoryPassword.equalsIgnoreCase(SharedPrefManager.getCurrentAccessPassword())){
                  /*if(encodeRetryCount<=encodeRetryLimit) {
                    encodeRetryCount++;*/
                  startEncoding(pickedTag, reservedMemoryPassword, encodeRetryCount);
                  //}
                  //else{
                  /*((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
                  isEncodeOn.postValue(false);
                  setProgressMessage(false);
                  showLog("RETRY_LIMIT_REACHED", "FAIL");
                  }*/
                }
                else writePasswordAndLock(pickedTag, reservedMemoryPassword, encodeRetryCount);
                break;
              }
            }
          }
        }
        if(!isPasswordFound){
          pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
          pickedTag.isUploaded = false;
          pickedTag.writeFailReason = "Auth Fail";
          pickedTag.retryWriteCount = pickedTag.retryWriteCount + 1;
          showLog("LOCKMEMORY1", "FAIL");
          if(inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
            inventoryDao.updateInventoryData(pickedTag);
          else inventoryDao.insertInventoryData(pickedTag);
          if(encodeRetryCount <= encodeRetryLimit)
            startEncoding(pickedTag, currentTagPassword, encodeRetryCount + 1);
          else updateTagWriteCount(context.getString(R.string.err_encoding_auth_fail));
        }
      }
    }
    else{
      //setProgressMessage(false);
      //((MainActivity) context).showCustomErrDialog(R.string.err_encoding_write_fail);
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
    final String tid = pickedTag.tid;
    //if(writeData(currentTagPassword, RFIDWithUHFUART.Bank_TID, 0, 96, tid, RFIDWithUHFUART.Bank_RESERVED, 2, 2, SharedPrefManager.getCurrentAccessPassword())){
    //if(writeData(currentTagPassword, tid, RFIDWithUHFUART.Bank_RESERVED, 2, 2, SharedPrefManager.getCurrentAccessPassword())){
    //if(writeReserved(currentTagPassword, tid,2, 2, SharedPrefManager.getCurrentAccessPassword())){
    if(writeReserved(currentTagPassword, tid, SharedPrefManager.getCurrentAccessPassword())){
      
      //if(SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase(defaultTagZeroPassword) || reader != null ? reader.lockMem(SharedPrefManager.getCurrentAccessPassword(), RFIDWithUHFUART.Bank_TID, 0, 96, tid, "0280A0") : mReader.lockMem(SharedPrefManager.getCurrentAccessPassword(), RFIDWithUHFUART.Bank_TID, 0, 96, tid, "0280A0")){
      //if(SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase(defaultTagZeroPassword) || lockMem(SharedPrefManager.getCurrentAccessPassword(), RFIDWithUHFUART.Bank_TID, 0, 96, tid)){
      //if(SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase(defaultTagZeroPassword) || lockMem(SharedPrefManager.getCurrentAccessPassword(), tid)){
      if(SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase(defaultTagZeroPassword) || lockReservedMemory(SharedPrefManager.getCurrentAccessPassword(), tid)){
        startEncoding(pickedTag, currentTagPassword, encodeRetryCount);
      }
      else{
        //fail
        //((MainActivity) context).showCustomErrDialog(R.string.err_encoding_fail);
        //isEncodeOn.postValue(false);
        //setProgressMessage(false);
        updateTagWriteCount(context.getString(R.string.err_encoding_fail));
        showLog("LOCKMEMORY", "FAIL");
      }
    }
    else{
      //fail
      //((MainActivity) context).showCustomErrDialog(R.string.err_encoding_write_fail);
      //isEncodeOn.postValue(false);
      //setProgressMessage(false);
      updateTagWriteCount(context.getString(R.string.err_encoding_write_fail));
      showLog("WRITECURRENTPASSWORD", "FAIL");
    }
  }
  
  /**
   * Perform decoding.
   *
   * @param sessionType the session type
   */
  @Override
  public synchronized void performDecoding(final AppCommonMethods.SessionType sessionType){ performDecoding(inventoryDao.getAllNonDecodedInventoryData(sessionType.getValue()), sessionType); }
  
  /**
   * Perform decoding.
   *
   * @param pickedTags the picked tags
   */
  @Override
  public synchronized void performDecoding(final List<Inventory> pickedTags){ performDecoding(pickedTags, AppCommonMethods.SessionType.OMNICHANNEL); }
  
  /**
   * Perform decoding.
   *
   * @param pickedTags  the picked tags
   * @param sessionType the session type
   */
  @Override
  public synchronized void performDecoding(final List<Inventory> pickedTags, final AppCommonMethods.SessionType sessionType){
    showLog("pickedTags", "" + pickedTags.size());
    showLog("sessionType", "" + sessionType.name());
    if(isNonEmpty(pickedTags) && sessionType.getValue() > 0){
      ChainwayRFIDHandler.this.sessionType = sessionType;
      sessionAction = AppCommonMethods.SessionAction.DECODE;
      multiWriteSuccessCount = 0;
      multiWriteListSize = pickedTags.size();
      multiWriteCount = multiWriteListSize;
      isMultiWriteDone = false;
      if(setPower(MAX_POWER_TO_SET / 2)){
        clearFilters();
        isDecodeOn.postValue(true);
        //setProgressMessage(context.getString(R.string.msg_pick), true);
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
  
  /**
   * Perform decoding.
   *
   * @param pickedTag the picked tag
   */
  @Override
  public void performDecoding(final Inventory pickedTag){
    if(pickedTag == null) return;
    sessionAction = AppCommonMethods.SessionAction.DECODE;
    boolean allowWrite = false;
    final boolean isMultiDecode = multiWriteListSize > 0;
    if(isMultiDecode || setPower(MAX_POWER_TO_SET / 2)){
      if(!isMultiDecode){
        clearFilters();
        isDecodeOn.postValue(true);
        isMultiWriteDone = false;
        //setProgressMessage(context.getString(R.string.msg_pick), true);
      }
      final List<String> listPasswords = SharedPrefManager.getOldAccessPasswords();
      if(listPasswords.contains(defaultTagZeroPassword))
        listPasswords.remove(defaultTagZeroPassword);
      listPasswords.add(0, defaultTagZeroPassword);
      if(listPasswords.contains(SharedPrefManager.getCurrentAccessPassword()))
        listPasswords.remove(SharedPrefManager.getCurrentAccessPassword());
      listPasswords.add(0, SharedPrefManager.getCurrentAccessPassword());
      if(SharedPrefManager.getNonPasswordTids().contains(pickedTag.tid.toUpperCase().substring(0, 8))){
        if(listPasswords.contains(defaultTagZeroPassword))
          listPasswords.remove(defaultTagZeroPassword);
        listPasswords.add(0, defaultTagZeroPassword);
      }
      handler.post(new Runnable(){
        @Override
        public void run(){
          startDecoding(pickedTag, listPasswords, 0);
        }
      });
    }
  }
  
  public void startDecoding(final Inventory pickedTag, final List<String> listPasswords, final int decodeRetryCount){
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
        dataLen = sgtin.length() / 4;
        offSet = (dataLen % 2 == 0 ? 2 : 1);
        break;
      default:
        break;
    }
    showLog("dataLen", "" + dataLen);
    showLog("offSet", "" + offSet);
    showLog("sgtin", "" + sgtin);
    showLog("tid", "" + tid);
    //showLog("currentAccessPassword", "" + SharedPrefManager.getCurrentAccessPassword());
    if(offSet > 0 && dataLen > 0){
      boolean isWriteSuccess = false;
      for(String pass : listPasswords){
        if(isNonEmpty(pass)){
          showLog("oldPassword", pass, false);
          if(isNonEmpty(pass) && writeData(pass, RFIDWithUHFUART.Bank_TID, 0, 96, tid, RFIDWithUHFUART.Bank_EPC, offSet, dataLen, sgtin)){
          //if(writeData(oldPassword, tid, RFIDWithUHFUART.Bank_EPC, offSet, dataLen, sgtin)){
          //if(isNonEmpty(pass) && writeEpc(pass, tid, offSet, dataLen, sgtin)){
            //if(writeEpc(oldPassword, tid, sgtin)){
            isWriteSuccess = true;
            showLog("WriteSuccess", "true");
            break;
          }
        }
      }
      if(isWriteSuccess){
        //update Data in table.
        pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
        pickedTag.writeFailReason = null;
        pickedTag.isUploaded = false;
        pickedTag.newEpc = /*sgtin.trim().matches("^(3000|3400|4000|4400).*$") ? sgtin.replaceFirst("(3000|3400|4000|4400)", "").trim() :*/ sgtin.trim();
        if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST ? inventoryDao.isEPCPresent(sessionId, pickedTag.epc) : inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
          //if(inventoryDao.isTagPresent(sessionId,pickedTag.epc,pickedTag.newEpc,pickedTag.tid) || (inventoryDao.isEPCPresent(sessionId, pickedTag.epc) && ()))
          inventoryDao.updateInventoryData(pickedTag);
        else inventoryDao.insertInventoryData(pickedTag);
        try{
          uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
        }
        catch(Exception e){ e.printStackTrace();
          AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
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
        catch(Exception e){ e.printStackTrace();
          AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
        }
        updateTagWriteCount(true);
      }
      else if(!isWriteSuccess){
        pickedTag.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
        pickedTag.isUploaded = false;
        pickedTag.writeFailReason = "Auth Fail";
        pickedTag.retryWriteCount = pickedTag.retryWriteCount + 1;
        if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST ? inventoryDao.isEPCPresent(sessionId, pickedTag.epc) : inventoryDao.isTagPresent(sessionId, pickedTag.epc, pickedTag.newEpc, pickedTag.tid))
          inventoryDao.updateInventoryData(pickedTag);
        else inventoryDao.insertInventoryData(pickedTag);
        if(decodeRetryCount <= decodeRetryLimit){
          startDecoding(pickedTag, listPasswords, decodeRetryCount + 1);
        }
        else{
          updateTagWriteCount(context.getString(R.string.err_decoding_auth_fail));
          showLog("LOCKMEMORY1", "FAIL");
        }
      }
    }
    else{
      updateTagWriteCount(context.getString(R.string.err_decoding_write_fail));
      showLog("TagWrite", "FAIL");
    }
  }
  
  private boolean shouldLogTagSample(){
    final long now = System.currentTimeMillis();
    if(now - lastTagSampleLogMs < TAG_SAMPLE_LOG_INTERVAL_MS) return false;
    lastTagSampleLogMs = now;
    return true;
  }
  
  private void processUHFData(final UHFTAGInfo uhftagInfo){
    final long callbackStartNs = System.nanoTime();
    try{
    if(loopFlag){
      try{
        if(uhftagInfo == null){
          AppCommonMethods.showLog("tag", "null");
          return;
        }
        if(shouldLogTagSample()){
          if(isActionTidPick || readTid)
            AppCommonMethods.showLog("tid_sample", chkNull(uhftagInfo.getTid(), "null"));
          if(isNonEmpty(uhftagInfo.getEPC()))
            showLog("tag_sample", sessionType.name() + "_TAG_EPC_" + chkNull(uhftagInfo.getEPC(), ""));
        }
      }
      catch(Throwable e){
        showLog("_err", e.getMessage());
        e.printStackTrace();
        AppCommonMethods.writeReaderLog(context,"CHAINWAY","CHAINWAY_RFID",FILE_TAG_LOG,e.getMessage());
      }
      if(uhftagInfo != null && isNonEmpty(uhftagInfo.getEPC())){
        final String epc = uhftagInfo != null ? chkNull(uhftagInfo.getEPC(), "") : "";
        final String tid = readTid ? chkNull(uhftagInfo.getTid(), "") : "";
        if(sessionAction == AppCommonMethods.SessionAction.INVENTORY) recordInventoryRawCallbackForDiagnostics();
        //updateFoundWrittenTag(epc,tid);
        //showLog("1epc_tid",epc+"_"+tid);
        if(!isValidItekTag(epc,tid)) return;
        //showLog("1epc_tid_valid",epc+"_"+tid);
        if(isCommandForSearch || sessionAction == AppCommonMethods.SessionAction.SEARCH){
          final int rssi = (int) Math.round(Double.parseDouble(uhftagInfo.getRssi()));
          final int phase = uhftagInfo.getPhase();
          showLog("tag_phase",""+phase);
          handleTagInfoForSearch(epc, String.valueOf(rssi), tid);
          /*if(validateTagInfoForSearch(epc, String.valueOf(rssi))){
            showLog(sessionType.name() + sessionAction.name() + " SEARCH_EPC == epc", SEARCH_EPC + "==" + epc);
            showLog(sessionType.name() + sessionAction.name() + " rssi", "" + rssi);
            int actualPercentage = isUseShortRangeSearchForAll || (isUseShortRangeSearchForOnlyGID && context.epcEncoderDecoder.isGID() && SEARCH_EPC.startsWith("35")) ? getPercentageForGID(rssi) : getPercentage(rssi);
            percent = actualPercentage;
            showLog(sessionType.name() + sessionAction.name() + " actualPer", "" + actualPercentage);
            if(!isCommandForEPCSearch && !isCommandForTIDSearch){
              showLog("SEARCH_LOCKED_EPC", isLockSearchEPC + "_" + SEARCH_LOCKED_EPC);
              if(isLockSearchEPC && isNullOrEmpty(SEARCH_LOCKED_EPC)) SEARCH_LOCKED_EPC = epc;
              if(!isLockSearchEPC || isNullOrEmpty(SEARCH_LOCKED_EPC) || epc.equalsIgnoreCase(SEARCH_LOCKED_EPC)){
                if(epc.length() >= 24){
                  //check by using getBarcode method instead of switch case
                  final String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epc);
                  final String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context, SEARCH_BARCODE);
                  showLog("search_barcode", barcode);
                  showLog("search_compare_barcode", compbarcode);
                  if(!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase()))){
                    showLog("RFIDHANDLER_barcode", "" + barcode);
                    searchPercent.postValue(actualPercentage);
                    searchRssi.postValue(String.valueOf(uhftagInfo.getRssi()));
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
              if(epc.equalsIgnoreCase(SEARCH_EPC)){
                *//*if(sessionType == AppCommonMethods.SessionType.ENCODING || rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue()){
                  updateEncVerifyByEpc(epcdt);
                }*//*
                searchPercent.postValue(actualPercentage);
                searchRssi.postValue(uhftagInfo.getRssi());
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
              showLog("tid_search", "" + (tid + "_" + SEARCH_TID));
              if(tid.equalsIgnoreCase(SEARCH_TID)){
                if((sessionType == AppCommonMethods.SessionType.ENCODING || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue())) || (sessionType == AppCommonMethods.SessionType.ENCODING_THAN || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING_THAN.getValue()))){
                  updateEncVerifyByEpcTid(epc, tid);
                }
                searchPercent.postValue(actualPercentage);
                searchRssi.postValue(String.valueOf(uhftagInfo.getRssi()));
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
          showLog("epc_tid", "" + epc + "_" + tid);
          String tid1 = tid;
          if(tid1.length() > 24) tid1 = tid1.substring(0, 24);
          if(isActionTidPick) showLog("tid_pick", "" + tid1);
          if(sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || epc.length() >= 24){
            if(isSinglePick && ((!isActionTidPick && !pickTags.contains(epc)) || (isActionTidPick && !pickTags.contains(tid) && SCANNED_TIDS.contains(tid) /*uhftagInfo.getTid().matches("(?i)(^" + SCANNED_TID + ".*$)")*/))){
              pickTags.add(isActionTidPick ? tid1 : epc);
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
        else if(sessionAction == AppCommonMethods.SessionAction.INVENTORY){
          if((sessionType == AppCommonMethods.SessionType.ENCODING || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue())) || (sessionType == AppCommonMethods.SessionType.ENCODING_THAN || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING_THAN.getValue()))){
            showLog("Inv_ENC", sessionId + "_" + sessionType + "_" + sessionAction);
            updateFoundWrittenTag(epc, tid);
            if(inventoryDao.getNonVerifiedCount(sessionId) <= 0){
              showLog("Inv_ENC", "all verified");
              stopInventory();
              context.showCustomSuccessDialog("Verified!");
            }
            else{
              showLog("Inv_ENC_epc_tid", "" + epc + "_" + tid);
              updateEncVerifyByEpcTid(epc, tid);
            }
          }
          else if(sessionType == AppCommonMethods.SessionType.SEARCH_FILE || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.SEARCH_FILE.getValue())){
            showLog("Inv_SER_FILE_epc_tid", "" + epc + "_" + tid);
            final boolean isEPCPresent = inventoryDao.isEPCPresent(sessionId, epc);
            final boolean isTidPresent = inventoryDao.isTidPresent(sessionId, tid);
            showLog("Inv_SER_FILE_epc_tid", "" + epc + "_" + tid + "_" + isEPCPresent + "_" + isTidPresent);
            if(isEPCPresent || isTidPresent){
              final int status = AppCommonMethods.EncodeVerifyStatus.RE_ENCODED.ordinal();
              if(isTidPresent){
                inventoryDao.updateEncVerifyStatusByTid(sessionId, tid, status);
              }
              else if(isEPCPresent){
                inventoryDao.updateEncVerifyStatusByEpc(sessionId, epc, status);
              }
            }
          }
          else if(sessionType == AppCommonMethods.SessionType.OFF_RANGE || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.OFF_RANGE.getValue())){
            final String ean = context.epcEncoderDecoder.getBarcodeFromEPC(epc);
            final int rssi = readRssi ? (int) Math.round(Double.parseDouble(uhftagInfo.getRssi())) : 0;
            int actualPercentage = isUseShortRangeSearchForAll || (isUseShortRangeSearchForOnlyGID && context.epcEncoderDecoder.isGID() && epc.startsWith("35")) ? getPercentageForGID(rssi) : getPercentage(rssi);
            if(isNonEmpty(ean) && isNonEmpty(eans) && eans.contains(ean)){
              showLog("off_matched", "true");
              final ProductDao productDao = AppDatabase.getProductDao(context);
              productDao.updateRssiPercentage(sessionType.getValue(), ean, actualPercentage);
            }
          }
          else if(sessionType == AppCommonMethods.SessionType.SER_EXCEL || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.SER_EXCEL.getValue())){
            final String ean = context.epcEncoderDecoder.getBarcodeFromEPC(epc);
            final int rssi = readRssi ? (int) Math.round(Double.parseDouble(uhftagInfo.getRssi())) : 0;
            int actualPercentage = isUseShortRangeSearchForAll || (isUseShortRangeSearchForOnlyGID && context.epcEncoderDecoder.isGID() && epc.startsWith("35")) ? getPercentageForGID(rssi) : getPercentage(rssi);
            if(isNonEmpty(ean) && isNonEmpty(eans) && eans.contains(ean)){
              showLog("off_matched", "true");
              final ProductDao productDao = AppDatabase.getProductDao(context);
              productDao.updateRssiPercentage(sessionType.getValue(), ean, actualPercentage);
            }
          }
          else if(!rejectKnownInventoryDuplicate(epc, tid) && validateTagInfoForInventory(epc)) storeInventoryData(uhftagInfo);
        }
      }
    }
    }
    finally{
      if(sessionAction == AppCommonMethods.SessionAction.INVENTORY) recordInventoryCallbackDuration(System.nanoTime() - callbackStartNs);
    }
  }
  
  //Barcode Scan Integration for R6 Device
  public void startBarcodeScan(final String type){
    final String scanType = chkNull(type, "Barcode");
    showLog("startBarcodeScan", scanType);
    isBarcodeOn.postValue(true);
    //setProgressMessage("Scanning " + scanType + "...", true);
    scanTimer.start();
    BarcodeResult result = reader.startScanBarcode();
    if(result != null){
      String scannedBarcodeData = new String(result.getBarcodeBytesData());
      showLog("scannedBarcodeData", scannedBarcodeData);
      if(isNonEmpty(scannedBarcodeData)){
        stopBarcodeScan();
        if(!chkNull(scannedBarcodeData, "").matches(AppCommonMethods.getScanRegex(scanType))){
          context.showCustomErrDialog(String.format(context.getString(R.string.err_invalid_scan), getTypeCharCode(), scanType, scannedBarcodeData));
          //if possible, pass session Type & check Std/NonStd in case of Encoding
        }
        else
          barcodeData.postValue(!scanType.matches("(?i)(^.*(Barcode|TID|RFID QR).*$)") ? chkNull(scannedBarcodeData, "").trim() : AppCommonMethods.getLeftZeroReplacedString(context, chkNull(scannedBarcodeData, "")) + (scanType.replace("Barcode", "").trim().length() > 0 && chkNull(scannedBarcodeData, "").trim().length() > 0 ? ";;" + scanType : ""));
      }
    }
  }
  
  private void stopBarcodeScan(){
    boolean stopScan = reader.stopScanBarcode();
    showLog("stopBarcodeScan", "" + stopScan);
    scanTimer.cancel();
    isBarcodeOn.postValue(false);
    setProgressMessage(false);
  }
  
  private void onTimerFinish(){
    if(isBarcodeOn.getValue()){
      stopBarcodeScan();
    }
  }
  
  @Override
  public boolean isProcessOn(){
    return super.isProcessOn() || chkNotNullTrue(isBarcodeOn.getValue());
  }
  
  protected final CountDownTimer scanTimer = new CountDownTimer(3000, 3000){
    @Override
    public void onTick(long l){ }
    
    @Override
    public void onFinish(){
      onTimerFinish();
    }
  };
}
