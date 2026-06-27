package com.itek.retail.reader.cipherlab;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.playSound;
import static com.itek.retail.common.AppCommonMethods.showShortToast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.cipherlab.rfid.ClResult;
import com.cipherlab.rfid.DeviceEvent;
import com.cipherlab.rfid.DeviceInfo;
import com.cipherlab.rfid.DeviceResponse;
import com.cipherlab.rfid.DeviceVoltageInfo;
import com.cipherlab.rfid.Gen2Settings;
import com.cipherlab.rfid.GeneralString;
import com.cipherlab.rfid.InventoryStatusSettings;
import com.cipherlab.rfid.InventoryType;
import com.cipherlab.rfid.LockTarget;
import com.cipherlab.rfid.PowerMode;
import com.cipherlab.rfid.RFIDMemoryBank;
import com.cipherlab.rfid.RFIDMode;
import com.cipherlab.rfid.RfidEpcFilter;
import com.cipherlab.rfid.SLFlagSettings;
import com.cipherlab.rfid.ScanMode;
import com.cipherlab.rfid.SessionSettings;
import com.cipherlab.rfid.SwitchMode;
import com.cipherlab.rfid.WorkMode;
import com.cipherlab.rfidapi.RfidManager;
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
import com.rscja.deviceapi.RFIDWithUHFUART;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CipherLabRFIDHandler extends RFIDHandler{
  
  private final int MIN_POWER_TO_SET = 5;
  private final int MAX_POWER_TO_SET = 30;
  private final int SOUND_THRESHOLD = 8;
  boolean isReceiverRegistered = false;
  Set<Bundle> pickUHFTags = new HashSet<>(0);
  private RfidManager mReader;
  private Boolean loopFlag = false;
  private Inventory encPickedTag = null;
  private List<String> listTagPasswords = new ArrayList<>();
  private int listTagPasswordIndex = -1;
  private boolean isWritePass = false;
  private final BroadcastReceiver myDataReceiver = new BroadcastReceiver(){
    @Override
    public void onReceive(Context context1, Intent intent){
      if(intent.getAction().equals(GeneralString.Intent_RFIDSERVICE_CONNECTED)){
        isReaderSet.postValue(isReaderConnected());
        String PackageName = intent.getStringExtra("PackageName");
        // / make sure this AP does already connect with RFID service (after call RfidManager.InitInstance(this)
        String serviceVersion = mReader.GetServiceVersion();
        String apiVersion = mReader.GetAPIVersion();
        DeviceInfo deviceInfo = mReader.GetDeviceInfo();
        String region = deviceInfo.Region;
        String kernelVersion = deviceInfo.KernelVersion;
        String rfidModuleVersion = deviceInfo.RFIDModuleVersion;
        String userVersion = deviceInfo.UserVersion;
        
        if(isNonEmpty(serviceVersion) && !SharedPrefManager.getReaderSDKVersion().equalsIgnoreCase(serviceVersion))
          SharedPrefManager.setReaderSDKVersion(serviceVersion);
        
      }
      else if(intent.getAction().equals(GeneralString.Intent_RFIDSERVICE_TAG_DATA)){
        // Fetch data from the intent
        int type = intent.getIntExtra(GeneralString.EXTRA_DATA_TYPE, -1);
        showLog("type", "" + type);
        // type 0 = Normal Scan
        // type 1 = Inventory Epc
        // type 2 = Inventory Epc+Tid
        // type 3 = Read Tag
        // type 5 = Write Tag
        // type 6 = Lock Tag
        // type 7 = Kill Tag
        // type 8 = Authenticate Tag
        // type 9 = Untraceable Tag
        int response = intent.getIntExtra(GeneralString.EXTRA_RESPONSE, -1);
        showLog("response11", "" + response);
        // response 0 = RESPONSE_OPERATION_SUCCESS
        // response 1 = RESPONSE_OPERATION_FINISH
        // response 2 = RESPONSE_OPERATION_TIMEOUT_FAIL
        // response 6 = RESPONSE_OPERATION_PASSWORD_FAIL
        // response 7 = RESPONSE_OPERATION_FAIL
        // response 251 = DEVICE_BUSY
        if(type == 5){//Write Operation
          //Handle Response for Write Operation if Possible
          //handleOperationResponse(response);
          if(isWritePass){
            showLog("WritePass11", response + "_" + (response <= 1));
            if(sessionType == AppCommonMethods.SessionType.ENCODING && encPickedTag != null && response <= 1 && !SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase(defaultTagZeroPassword))
              lockPassWord(encPickedTag);
          }
          if(sessionType == AppCommonMethods.SessionType.ENCODING && encPickedTag != null && !isWritePass && isNonEmpty(listTagPasswords) && listTagPasswordIndex >= 0)
            startEncodePass(encPickedTag, response <= 1, response <= 1 ? listTagPasswordIndex : ++listTagPasswordIndex);
          return;
        }
        
        AppCommonMethods.showLog("WriteTagResponse", "fs");
        final String data1 = chkNull(intent.getStringExtra(GeneralString.EXTRA_DATA1), "");
        final String data2 = chkNull(intent.getStringExtra(GeneralString.EXTRA_DATA2), "");
        final Integer data1Len = intent.getIntExtra(GeneralString.EXTRA_DATA1_LENGTH, 0);
        final Integer data2Len = intent.getIntExtra(GeneralString.EXTRA_DATA2_LENGTH, 0);
        if(data2Len > 0 || data1Len > 0){
          showLog("ReadData1", chkNull(data1, ""));
          showLog("ReadData1_length", "" + data1Len);
          showLog("ReadData2", chkNull(data2, ""));
          showLog("ReadData2_length", "" + data2Len);
        }
        final double data_rssi = intent.getDoubleExtra(GeneralString.EXTRA_DATA_RSSI, 0);
        final String PC = chkNull(intent.getStringExtra(GeneralString.EXTRA_PC), "");
        final String EPC = chkNull(intent.getStringExtra(GeneralString.EXTRA_EPC), "");
        final String TID = chkNull(intent.getStringExtra(GeneralString.EXTRA_TID), "");
        final String ReadData = intent.getStringExtra(GeneralString.EXTRA_ReadData);
        int EPC_length = intent.getIntExtra(GeneralString.EXTRA_EPC_LENGTH, 0);
        int TID_length = intent.getIntExtra(GeneralString.EXTRA_TID_LENGTH, 0);
        int ReadData_length = intent.getIntExtra(GeneralString.EXTRA_ReadData_LENGTH, 0);
        if(ReadData_length > 0){
          showLog("ReadData", chkNull(ReadData, ""));
          showLog("ReadData_length", "" + ReadData_length);
          showLog("bundle", intent != null && intent.getExtras() != null && !intent.getExtras().isEmpty() ? intent.getExtras().toString() : "");
          return;
        }
        if(type >= 3) return;
        showLog("epc", "" + EPC);
        showLog("data_rssi", "" + data_rssi);
        if(isNullOrEmpty(EPC) && EPC_length < 24) return;
        final String epcdt = EPC;
        showLog("epcdt", "" + epcdt);
        showLog("data_rssi", "" + data_rssi);
        if(isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK){
          showLog("epc_pick", "" + EPC);
          String tid = TID;
          if(tid.length() > 24) tid = tid.substring(0, 24);
          if(isActionTidPick) showLog("tid_pick", "" + tid);
          if(sessionType == AppCommonMethods.SessionType.ENCODING || EPC.length() >= 24){
            if(isSinglePick && ((!isActionTidPick && !pickTags.contains(EPC)) || (isActionTidPick && !pickTags.contains(tid) && SCANNED_TIDS.contains(tid) /*uhftagInfo.getTid().matches("(?i)(^" + SCANNED_TID + ".*$)")*/))){
              pickTags.add(isActionTidPick ? TID : EPC);
              pickUHFTags.add(intent.getExtras());
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
          return;
        }
        else if(isCommandForSearch || sessionAction == AppCommonMethods.SessionAction.SEARCH){
          String epc = EPC;
          showLog("epc", "" + epc);
          final int rssi = (int) Math.round(data_rssi);
          showLog("rssi", "" + data_rssi);
          int actualPercentage = getPercentage(rssi);
          percent = actualPercentage;
          showLog("actualPer", "" + actualPercentage);
          if(!isCommandForEPCSearch){
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
                  searchRssi.postValue(String.valueOf(data_rssi));
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
            if(epc.equalsIgnoreCase(SEARCH_EPC)){
              searchPercent.postValue(actualPercentage);
              searchRssi.postValue("" + data_rssi);
              if(actualPercentage > 90){
                counter_for_threshold_percentage_to_sound_beep++;
                if(counter_for_threshold_percentage_to_sound_beep >= SOUND_THRESHOLD){
                  counter_for_threshold_percentage_to_sound_beep = 0;
                  playSound(context, R.raw.successbeep);
                }
              }
            }
          }
          return;
        }
        else if(!isActionPick && !isCommandForSearch && sessionAction == AppCommonMethods.SessionAction.INVENTORY){
          if(sessionType == AppCommonMethods.SessionType.ENCODING || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue())){
            showLog("Inv_ENC", sessionId + "_" + sessionType + "_" + sessionAction);
            updateFoundWrittenTag(epcdt, readTid ? TID : "");
            if(inventoryDao.getNonVerifiedCount(sessionId) <= 0){
              showLog("Inv_ENC", "all verified");
              stopInventory();
              context.showCustomSuccessDialog("Verified!");
            }
            else{
              updateEncVerifyByEpcTid(epcdt, readTid ? TID : "");
            }
          }
          else if(sessionType == AppCommonMethods.SessionType.SEARCH_FILE || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.SEARCH_FILE.getValue())){
            final String tid = readTid ? TID : "";
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
                inventoryDao.updateStatusByEpc(sessionId, epcdt, status);
              }
            }
          }
          else if(sessionType == AppCommonMethods.SessionType.OFF_RANGE || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.OFF_RANGE.getValue())){
            final String ean = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt);
            final int rssi = readRssi ? (int) data_rssi : 0;
            int actualPercentage = getPercentage(rssi);
            if(isNonEmpty(ean) && isNonEmpty(eans) && eans.contains(ean)){
              showLog("off_matched", "true");
              final ProductDao productDao = AppDatabase.getProductDao(context);
              productDao.updateRssiPercentage(sessionType.getValue(), ean, actualPercentage);
            }
          }
          else{
            final BrandEanDao brandEansDao = AppDatabase.getBrandEansDao(context);
            final ProductDao productDao = AppDatabase.getProductDao(context);
            final FIFODao fifoDao = AppDatabase.getFIFODao(context);
            if(sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION && !productDao.isEPCPresent(epcdt)){
              return;
            }
            if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY && (/*(brandEansDao.hasData() && !brandEansDao.isEANPresent("," + context.epcEncoderDecoder.getBarcodeFromEPC(epcdt) + ",")) ||*/ (isNonEmpty(eans) && !eans.contains(context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase())))){
              return;
            }
            if(sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY && (/*(brandEansDao.hasData() && !brandEansDao.isEANPresent("," + context.epcEncoderDecoder.getBarcodeFromEPC(epcdt) + ",")) ||*/ (isNonEmpty(eans) && !eans.contains(context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase())))){
              return;
            }
            if(sessionType == AppCommonMethods.SessionType.OFF_RANGE && (/*(brandEansDao.hasData() && !brandEansDao.isEANPresent("," + context.epcEncoderDecoder.getBarcodeFromEPC(epcdt) + ",")) ||*/ (isNonEmpty(eans) && !eans.contains(context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase())))){
              return;
            }
            if(sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE){
              //              if(inventoryDao.isEPCPresent(sessionId,epcdt)){
              //                stopInventory();
              //                context.showCustomErrDialog(R.string.err_msg_already_added);
              //                return;
              //              }
              if((isNonEmpty(eans) && !eans.contains(context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase()))){
                stopInventory();
                context.showCustomErrDialog(String.format(context.getString(R.string.err_msg_pick_wrong_tote), context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase()));
                return;
              }
            }
            if(sessionType == AppCommonMethods.SessionType.INWARD_TOTE){
              //TODO
              if((isNonEmpty(eans) && !eans.contains(context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase()))){
                //TODO
                stopInventory();
                context.showCustomErrDialog(String.format(context.getString(R.string.err_msg_pick_wrong_tote), context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase()));
                return;
              }
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
            if(sessionAction.getValue() > 0 && sessionAction != AppCommonMethods.SessionAction.SEARCH && (!isSinglePick || sessionAction != AppCommonMethods.SessionAction.PICK))
              storeInventoryData(intent.getExtras());
          }
        }
      }
      else if(intent.getAction().equals(GeneralString.Intent_RFIDSERVICE_EVENT)){
        int event = intent.getIntExtra(GeneralString.EXTRA_EVENT_MASK, -1);
        Log.d(TAG, "[Intent_RFIDSERVICE_EVENT] DeviceEvent=" + event);
        if(event == DeviceEvent.LowBattery.getValue()){
          Log.i(GeneralString.TAG, "LowBattery ");
        }
        else if(event == DeviceEvent.PowerSavingMode.getValue()){
          Log.i(GeneralString.TAG, "PowerSavingMode ");
        }
        else if(event == DeviceEvent.OverTemperature.getValue()){
          Log.i(GeneralString.TAG, "OverTemperature ");
          
        }
        else if(event == DeviceEvent.ScannerFailure.getValue()){
          Log.i(GeneralString.TAG, "ScannerFailure ");
        }
      }
      else if(intent.getAction().equals(GeneralString.Intent_FWUpdate_Percent)){
        //Battery
        DeviceVoltageInfo deviceVoltageInfo = new DeviceVoltageInfo();
        mReader.GetBatteryLifePercent(deviceVoltageInfo);
      }
      else if(intent.getAction().equals(GeneralString.Intent_FWUpdate_Finish)){
        Log.d(TAG, "Intent_FWUpdate_Finish");
        showShortToast(context, "Intent_FWUpdate_Finish");
      }
      else if(intent.getAction().equals(GeneralString.Intent_GUN_Attached)){
        Log.d(TAG, "Intent_GUN_Attached");
        showShortToast(context, "Intent_GUN_Attached");
      }
      else if(intent.getAction().equals(GeneralString.Intent_GUN_Unattached)){
        Log.d(TAG, "Intent_GUN_Unattached");
        showShortToast(context, "Intent_GUN_Unattached");
      }
      else if(intent.getAction().equals(GeneralString.Intent_GUN_Power)){
        Log.d(TAG, "Intent_GUN_Power");
        boolean AC = intent.getBooleanExtra(GeneralString.Data_GUN_ACPower, false);
        boolean Connect = intent.getBooleanExtra(GeneralString.Data_GUN_Connect, false);
      }
    }
  };
  
  void handleOperationResponse(int response){
    switch(response){
      case 0: /*Operation Success*/
        break;
      case 1: /*Operation Finish*/
        break;
      case 2: /*Operation TIMEOUT Fail*/
        break;
      case 6: /*Access Password Fail*/
        break;
      case 7: /*Operation Fail*/
        break;
      case 251: /*Device Busy*/
        break;
      default:
        break;
    }
  }
  
  @Override
  public void onCreate(CommonActivity activity, MainReaderRepository mainReaderRepository, RFIDInitInterface rfidInterface, AppCommonMethods.SessionType sessionType){
    super.onCreate(activity, mainReaderRepository, rfidInterface, sessionType);
    registerReceiver();
  }
  
  private void registerReceiver(){
    IntentFilter filter = new IntentFilter();
    filter.addAction(GeneralString.Intent_RFIDSERVICE_CONNECTED);
    filter.addAction(GeneralString.Intent_RFIDSERVICE_TAG_DATA);
    filter.addAction(GeneralString.Intent_RFIDSERVICE_EVENT);
    filter.addAction(GeneralString.Intent_FWUpdate_ErrorMessage);
    filter.addAction(GeneralString.Intent_FWUpdate_Percent);
    filter.addAction(GeneralString.Intent_FWUpdate_Finish);
    filter.addAction(GeneralString.Intent_GUN_Attached);
    filter.addAction(GeneralString.Intent_GUN_Unattached);
    filter.addAction(GeneralString.Intent_GUN_Power);
    context.registerReceiver(myDataReceiver, filter);
    isReceiverRegistered = true;
  }
  
  @Override
  protected void saveSerialNo(){
    if(isNonEmpty(SharedPrefManager.getString(ParamConstants.DEVICE_SERIAL,""))) return;
    if(mReader != null && mReader.GetDeviceInfo() != null){
      String deviceSerialNo = mReader.GetDeviceInfo().SerialNumber;
      if(isNonEmpty(deviceSerialNo))
        SharedPrefManager.setString(ParamConstants.DEVICE_SERIAL, deviceSerialNo);
    }
  }
  
  @Override
  public void InitSDK(){
    super.InitSDK();
    InitSDK(false);
  }
  
  public void InitSDK(boolean isConfigureDevice){
    setProgressMessage(true);
    if(mReader == null){
      try{
        mReader = RfidManager.InitInstance(context);
      }
      catch(Exception ex){
        ex.printStackTrace();
      }
    }
    if(mReader != null){
      if(!mReader.GetConnectionStatus()){
        saveSerialNo();
        //Configure
        mReader.SetPowerMode(PowerMode.Normal);
        //mReader.SetScanMode(ScanMode.Alternate);
        mReader.SetTxPower(MAX_POWER_TO_SET);//Power
        mReader.SetRFIDMode(RFIDMode.Inventory_EPC_TID);
        mReader.SetWorkMode(WorkMode.MultiTagMode);
        mReader.SetSwitchMode(SwitchMode.UHFRFIDReader);
        mReader.EnableDeviceTrigger(true);
        setProgressMessage(false);
      }
      else configureReader();
    }
    else{
      setProgressMessage(false);
      rfidInterface.RFIDInitializationStatus(false, "", null);
    }
  }
  
  @Override
  public void checkAndConnectReader(){
    if(mReader == null) isReaderSet.postValue(null);
    if(!isReaderConnected()) InitSDK(true);
    else if(sessionType.getValue() > 0 && !chkNotNullTrue(isDeviceConfigured.getValue()))
      configureReader(sessionType);
  }
  
  @Override
  public void checkAndSetReader(){
    if(mReader == null){
      isReaderSet.postValue(null);
      InitSDK(true);
    }
    else if(mReader.GetConnectionStatus()){
      InitSDK(true);
    }
  }
  
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
  
  @Override
  protected void performSearch(){
    sessionAction = AppCommonMethods.SessionAction.SEARCH;
    showLog("CMD", "" + isCommandForSearch);
    // check reader connection
    if(!isReaderConnected()){
      showLog("Reader", "NOT CONNECTED");
      return;
    }
    clearFilters();
    setSession(sessionAction);
    showLog("isCommandForTIDSearch", "" + isCommandForTIDSearch);
    if(mReader.SetTxPower(MAX_POWER_TO_SET) == ClResult.S_OK.ordinal()){
      //if(isCommandForTIDSearch ? mReader.setEPCAndTIDMode() : mReader.setEPCMode()){
      
      try{
        String finalsgtin = SEARCH_EPC;
        String header = finalsgtin.length() > 2 ? finalsgtin.substring(0, 2) : "";
        final boolean isNonStdEnc = finalsgtin.length() >= 32 && header.matches("(?i)(BC|0C|00)");
        
        mReader.SetScanMode(ScanMode.Continuous);
        if(isCommandForTIDSearch) addTidBasedFilters(SEARCH_TID);
        else if(isCommandForEPCSearch) addEpcBasedFilters(finalsgtin, isNonStdEnc);
        else
          addFilters(isNonStdEnc && finalsgtin.length() > 12 ? finalsgtin.substring(12) : finalsgtin, isNonStdEnc);
        //}
      }
      catch(Exception e){
        e.printStackTrace();
        stopInventory();
        if(AppCommonMethods.isShowReaderCommandFailToast)
          showShortToast(context, R.string.err_reader_fail);
      }
      //}
    }
  }
  
  @Override
  protected void addEpcBasedFilters(String epc, boolean isNonStdEnc){
    // Add state aware pre-filter
    int filterBank = RFIDWithUHFUART.Bank_EPC;
    int offSet = 32;
    int len = epc.length() * 2;
    final byte[] epcDataArray = getHexByteArray(epc);
    showLog("epcDataArray1", print(epcDataArray));
    
    //select tags that match the criteria
    byte lsb = (byte) (epcDataArray[epcDataArray.length - 1] & 1);// & 0xFF);
    showLog("lsb", "" + lsb);
    try{
      RfidEpcFilter epcFilter = new RfidEpcFilter();
      epcFilter.Startbit_LSB = lsb;
      epcFilter.PatternLength_LSB = (byte) epc.length();//(byte) epcDataArray[epcDataArray.length-1];
      epcFilter.Startbit_MSB = (byte) 0;//(byte) epcDataArray[epcDataArray.length-1]
      epcFilter.PatternLength_MSB = (byte) 0;
      epcFilter.EPCPattern1 = epc;
      epcFilter.EPCPattern2 = null;
      epcFilter.Scheme = epcDataArray[0];
      epcFilter.Enable = 1;
      
      if(mReader.SetIncludedEPCFilter(epcFilter) == ClResult.S_OK.ordinal()){
        showLog("addEpcBasedFilter_" + epc, "success");
        readTag();
        //        if(mReader.RFIDReadTagMassive(null, RFIDMemoryBank.EPC, 4, epc.length() / 2) != ClResult.S_OK.ordinal())
        //          showLog("addEpcBasedFilter_1" + epc, mReader.GetLastError());
      }
      else showLog("addEpcBasedFilter_" + epc, mReader.GetLastError());
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  protected void addFilters(String epc, boolean isNonStdEnc){
    // Add state aware pre-filter
    showLog("tag_isBC", epc + "_" + isNonStdEnc);
    int filterBank = RFIDWithUHFUART.Bank_EPC;
    //68 =>substring(9) 80=>substring(12)
    int offSet = isNonStdEnc ? 80 : 32;
    int len = epc.length() * 2;
    final byte[] epcDataArray = getHexByteArray(epc);
    showLog("epcDataArray", print(epcDataArray));
    //select tags that match the criteria
    try{
      byte lsb = (byte) (epcDataArray[epcDataArray.length - 1] & 1);// & 0xFF);
      //byte msb = (byte) ((epcDataArray[0] & 0xFF00) >> 8);
      //result[5] = (byte) (value & 0xFF);           // Least significant "byte"
      //result[6] = (byte) ((value & 0xFF00) >> 8);  // Most significant "byte"
      
      showLog("lsb", "" + lsb);
      RfidEpcFilter epcFilter = new RfidEpcFilter();
      epcFilter.Startbit_LSB = lsb;// (byte)(0x00);//(epcDataArray[epcDataArray.length-1] & 0xFF);
      epcFilter.PatternLength_LSB = (byte) epc.length();
      epcFilter.Startbit_MSB = (byte) 0;
      epcFilter.PatternLength_MSB = (byte) 0;
      epcFilter.EPCPattern1 = epc;
      epcFilter.EPCPattern2 = null;
      epcFilter.Enable = 1;
      epcFilter.Scheme = epcDataArray[0];
      
      byte[] EPCByteArray = new byte[]{(byte) 0x33, (byte) 0x30, (byte) 0xaf, (byte) 0xec, (byte) 0x2b, (byte) 0x01, (byte) 0x15, (byte) 0xc0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01};
      RfidEpcFilter f = new RfidEpcFilter();
      f.Enable = 1;
      f.Startbit_LSB = ((byte) (0x00));
      f.Startbit_MSB = ((byte) (0));
      //f.EPCPattern1 = "1e2400";
      f.EPCPattern1 = "1234";
      f.EPCPattern2 = null;
      f.PatternLength_LSB = ((byte) (0x60));
      f.PatternLength_MSB = ((byte) (0));
      f.Scheme = (byte) 0x33;
      if(mReader.SetIncludedEPCFilter(epcFilter) == ClResult.S_OK.ordinal()){
        readTag();
        showLog("addFilter_" + epc, "success");
        
        //        if(mReader.RFIDReadTagMassive(null,RFIDMemoryBank.EPC,offSet/8,len)!=ClResult.S_OK.ordinal())
        //          showLog("addFilter_1"+epc,mReader.GetLastError());
      }
      else showLog("addFilter_" + epc, mReader.GetLastError());
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  protected void addTidBasedFilters(String tid){
  
  }
  
  @Override
  public boolean performPick(final String findBarcode, final boolean isDecodeOnPick, final Integer pickPower, final boolean isPostPicked, List<String> pickedEpcs){
    final boolean isPerformPick = super.performPick(findBarcode, isDecodeOnPick, pickPower, isPostPicked, pickedEpcs);
    if(isPerformPick){
      pickTags.clear();
      pickUHFTags.clear();
      isPickOn.postValue(false);
      isActionPick = false;
      startPick(findBarcode, isDecodeOnPick, isPostPicked);
    }
    return isPerformPick;
  }
  
  @Override
  public boolean performInventory(final boolean isHideUnencodedTags, final List<String> listIgnoreEPCs){
    if(super.performInventory(isHideUnencodedTags, listIgnoreEPCs)){
      sessionAction = AppCommonMethods.SessionAction.INVENTORY;
      int maxPower = sessionType == AppCommonMethods.SessionType.INWARD_TOTE ? (int) (MIN_POWER_TO_SET * inwToteMinPowerMultiplier) : sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE ? (int) (MIN_POWER_TO_SET * owtToteMinPowerMultiplier) : MAX_POWER_TO_SET;
      int power = chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower / 10) * 10;
      showLog("power", "" + power);
      mReader.SetTxPower(power);
      clearFilters();
      setSession(sessionAction);
      mReader.SetScanMode(ScanMode.Continuous);
      readTag();
      return true;
    }
    return false;
  }
  
  @Override
  protected void startPick(final String barcode, boolean isDecodeOnPick, boolean isPostPicked){
    try{
      sessionAction = AppCommonMethods.SessionAction.PICK;
      int maxPower = chkZero(pickPower, sessionType == AppCommonMethods.SessionType.MOVEMENT ? MAX_POWER_TO_SET / 2 : MIN_POWER_TO_SET * 2);
      int power = sessionType == AppCommonMethods.SessionType.MOVEMENT ? chkZero(SharedPrefManager.getInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower) : maxPower;
      if(power > maxPower){
        power = maxPower;
      }
      showLog("power", "" + power);
      clearFilters();
      setSession(sessionAction);
      if(setPower(power)){
        SEARCH_BARCODE = barcode;
        isActionPick = true;
        isPickOn.postValue(true);
        readTag();
        if(loopFlag && isSinglePick){
          setProgressMessage(context.getString(R.string.msg_pick), true);
          pickCountDownTimer = new CountDownTimer(pickCountDownTime, 1000){
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
                final Bundle tagData = new ArrayList<Bundle>(pickUHFTags).get(0);
                final String epcdt = chkNull(tagData.getString(GeneralString.EXTRA_EPC), "");
                final String tid = chkNull(tagData.getString(GeneralString.EXTRA_TID), "");
                showLog("pick_epc_tid", epcdt + "_" + tid);
                setProgressMessage(false);
                updateFoundWrittenTag(epcdt, tid);
                if(sessionType == AppCommonMethods.SessionType.OFF_RANGE && uploadInventoryDao.isEPCPresent(sessionType.getValue(), epcdt)){
                  AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_STOP (Tag Already Picked)");
                  final UploadInventory ui = uploadInventoryDao.getBysessionTypeAndEpc(sessionType.getValue(), epcdt);
                  if(ui != null && isNonEmpty(ui.remark) && isNonEmpty(ui.fifoDate))
                    context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag_in_carton), getTypeCharCode(), ui.remark));
                  else
                    context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
                  setProgressMessage(false);
                }
                else if(sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.DECODING && (inventoryDao.isEPCPresent(sessionId, epcdt) || (isNonEmpty(pickedEpcs) && pickedEpcs.contains(epcdt)))){
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
                  String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING);
                  String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context, barcode);
                  showLog("pick_barcode", barcode);
                  showLog("pick_compare_barcode", compbarcode);
                  final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                  if(!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase())) && (sessionType != AppCommonMethods.SessionType.SEARCH_FIFO || fifoDao.isEPCPresent(epcdt, ean, fifoDate/*,zone,zoneId*/))){
                    isMatchingBarcode = true;
                    matchingBarcode = AppCommonMethods.getLeftZeroReplacedString(context, barcode);
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
                    readPC = true;//sessionType == AppCommonMethods.SessionType.ENCODING;// || sessionType == AppCommonMethods.SessionType.DECODING;
                    if(isPostPicked || sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING){
                      final Inventory pickedTag = getDataFromTagInfo(tagData);
                      if((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && isMatchingBarcode && isNonEmpty(matchingBarcode))
                        pickedTag.ean = AppCommonMethods.getLeftZeroReplacedString(context, matchingBarcode);
                      if(isNullOrEmpty(matchingBarcode) && sessionType != AppCommonMethods.SessionType.ENCODING && (sessionType != AppCommonMethods.SessionType.DECODING || !SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_NON_ENCODED_TAG_PICK_FOR_DECODE, AppCommonMethods.isAllowNonEncodedTagPickForDecode)))
                        context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                      else if((sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING) && (pickedTag == null || isNullOrEmpty(pickedTag.epc) || isNullOrEmpty(pickedTag.tid)))
                        context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
                        //                      else if(isDebugApp) readTagCurrentPassword(pickedTag); //temp code
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
          pickCountDownTimer.start();
        }
      }
    }
    catch(Exception e){
      e.printStackTrace();
      stopInventory();
      if(AppCommonMethods.isShowReaderCommandFailToast)
        showShortToast(context, R.string.err_reader_fail);
    }
  }
  
  private void clearFilters(){
    //Old Code (Commented)
    /*if(mReader.setFilter(1, 32, 0, "00"))
      showLog("clearFilters","cleared");
    else
      showLog("clearFilters","failed");*/
    
    //New Code
    try{
      if(isReaderConnected() && mReader.SetIncludedEPCFilter(new RfidEpcFilter()) == ClResult.S_OK.ordinal() && mReader.SetExcludedEPCFilter(new RfidEpcFilter()) == ClResult.S_OK.ordinal() && mReader.ClearFilterDuplicate() == ClResult.S_OK.ordinal())
        showLog("clearFilters", "cleared");
      else
        showLog("clearFilters", "failed_" + mReader == null || mReader.GetConnectionStatus() ? "reader Not connected" : mReader.GetLastError());
    }
    catch(Exception e){ }
  }
  
  @Override
  protected Inventory getDataFromTagInfo(Object object){
    return object != null && object instanceof Bundle ? getDataFromTagInfo((Bundle) object) : new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
  }
  
  /**
   * Get data from tag info inventory.
   *
   * @param tagData the tag data
   * @return the inventory
   */
  
  private Inventory getDataFromTagInfo(Bundle tagData){
    Inventory inventory = new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue());
    inventory.epc = tagData.getString(GeneralString.EXTRA_EPC).toUpperCase();
    inventory.tid = readTid ? tagData.getString(GeneralString.EXTRA_TID) : "";
    inventory.rssi = readRssi ? String.valueOf(tagData.getDouble(GeneralString.EXTRA_DATA_RSSI, 0)) : "";
    inventory.pcdata = readPC ? tagData.getString(GeneralString.EXTRA_PC) : "";
    inventory.zone = zone;
    inventory.zoneId = zoneId;
    try{
      inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
    }
    catch(Exception e){ e.printStackTrace(); }
    return inventory;
  }
  
  @Override
  protected void startTidPick(String barcode, List<String> tids){
  
  }
  
  @Override
  public void configureReader(AppCommonMethods.SessionType sessionType){
    mReader.SetPowerMode(PowerMode.Normal);
    //mReader.SetScanMode(ScanMode.Continuous);
    mReader.SetRFIDMode(RFIDMode.Inventory);
    mReader.SetWorkMode(WorkMode.MultiTagMode);
    mReader.SetSwitchMode(sessionType == AppCommonMethods.SessionType.ENCODING ? SwitchMode.UHFRFIDBarcodeReader : SwitchMode.UHFRFIDReader);
    mReader.EnableDeviceTrigger(true);
    //mReader.SoftScanTrigger(true);
    //mReader.SetRFLink(RFLink.DSB_ASK_FM0_40KHz);
    //mReader.SetContinuousInventoryTime();
    //mReader.SetRecognizedEPCEncoding();
    
    //    RfidOutputConfiguration rfidOutputConfiguration = new RfidOutputConfiguration();
    //    mReader.GetDataOutputSettings(rfidOutputConfiguration);
    //    rfidOutputConfiguration.CopyPasteOutput=true;
    //    mReader.SetDataOutputSettings(rfidOutputConfiguration);
    
    this.sessionType = this.sessionType.getValue() == 0 && sessionType != null && sessionType.getValue() > 0 ? sessionType : this.sessionType;
    AppCommonMethods.SessionType type = this.sessionType.getValue() > 0 || sessionType == null || sessionType.getValue() == 0 ? this.sessionType : sessionType != null ? sessionType : this.sessionType;
    if(!isReaderConnected()){
      showLog(TAG, "CONFIG FAIL");
      isDeviceConfigured.postValue(false);
      rfidInterface.RFIDInitializationStatus(false, "RFID initialization Failed", null);
    }
    else if(type != null && type.getValue() > 0){
      setProgressMessage("Please wait...\nConfiguring Reader...", true);
      showLog(TAG, "CONFIG SUCCESS");
      int maxPower = type == AppCommonMethods.SessionType.SCAN || type == AppCommonMethods.SessionType.VERIFY_ENCODING || type == AppCommonMethods.SessionType.ENCODING ? MIN_POWER_TO_SET * 2 : type == AppCommonMethods.SessionType.MOVEMENT || type == AppCommonMethods.SessionType.INWARD || type == AppCommonMethods.SessionType.OUTWARD ? MAX_POWER_TO_SET / 2 : MAX_POWER_TO_SET;
      showLog("maxpower", type.name() + ":" + maxPower);
      int power = chkZero(SharedPrefManager.getInt(type.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name()), maxPower);
      SharedPrefManager.setInt(type.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), power);
      showLog("config power", type.name() + ":" + power);
      setReaderPower(power);
      mReader.SetRFIDMode(RFIDMode.Inventory);
      clearFilters();
      setSession(sessionAction);
      isDeviceConfigured.postValue(true);
      setProgressMessage(false);
      rfidInterface.RFIDInitializationStatus(true, "", mReader);
    }
    else setProgressMessage(false);
    
  }
  
  private void setSession(AppCommonMethods.SessionAction sessionAction){
    if(isReaderConnected()){
      final boolean isSearch = isCommandForSearch || sessionAction == AppCommonMethods.SessionAction.SEARCH;
      Gen2Settings gen2Settings = new Gen2Settings();
      mReader.GetGen2(gen2Settings);
      gen2Settings.SL_Flag = SLFlagSettings.All;
      gen2Settings.Session = SessionSettings.S0;
      gen2Settings.InventoryStatus_Action = isSearch ? InventoryStatusSettings.STATE_B : InventoryStatusSettings.STATE_A;
      mReader.SetGen2(gen2Settings);
      mReader.SetRFIDMode(getRFIDMode(sessionAction));
      //      mReader.ClearFilterDuplicate();
      //      if(sessionAction!= AppCommonMethods.SessionAction.SEARCH){
      //        int result = mReader.SetFilterDuplicate(2);
      //        showLog("result",""+result);
      //      }
    }
  }
  
  private RFIDMode getRFIDMode(AppCommonMethods.SessionAction sessionAction){
    switch(sessionAction){
      case ENCODE, DECODE:
        return RFIDMode.WriteTag;
      case PICK:
        return RFIDMode.Inventory_EPC_TID;
      case SEARCH, INVENTORY:
        return readTid ? RFIDMode.Inventory_EPC_TID : RFIDMode.Inventory;
      default:
        return RFIDMode.Err;
    }
  }
  
  @Override
  public boolean isReaderConnected(){
    return mReader != null && mReader.GetConnectionStatus();
  }
  
  private boolean setPower(int power){
    if(!isReaderConnected()) return false;
    return mReader.SetTxPower(power) == ClResult.S_OK.ordinal();
  }
  
  @Override
  public void setReaderPower(int power){
    final int oldPower = mReader.GetTxPower();
    setProgressMessage("Setting Reader Power...", true);
    if(isReaderConnected() && power >= MIN_POWER_TO_SET && power <= MAX_POWER_TO_SET && setPower(power)){
      showLog("power", "set");
      setProgressMessage(false);
      readerPower.postValue(power);
      if(sessionType != null && sessionType.getValue() > 0 /*&& sessionType == AppCommonMethods.SessionType.MOVEMENT*/){
        showLog(sessionType.name() + "SetPower", "" + sessionType.name());
        SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), power);
      }
      SharedPrefManager.setReaderPower(power);
    }
    else{
      setProgressMessage(false);
      readerPower.postValue(oldPower);
      SharedPrefManager.setReaderPower(oldPower);
      if(sessionType != null)
        SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), power);
    }
    
  }
  
  @Override
  public Object getReaderInstance(CommonActivity context, MainReaderRepository mainReaderRepository, RFIDInitInterface rfidInitInterface){
    try{
      this.context = context;
      this.mainReaderRepository = mainReaderRepository;
      this.rfidInterface = rfidInitInterface;
      mReader = RfidManager.InitInstance(context);
    }
    catch(Exception ex){
      ex.printStackTrace();
    }
    return mReader;
  }
  
  @Override
  public void performEncoding(List<Inventory> listPickedTags, AppCommonMethods.SessionType sessionType){
    if(isNonEmpty(listPickedTags) && sessionType.getValue() > 0){
      this.sessionType = sessionType;
      sessionAction = AppCommonMethods.SessionAction.ENCODE;
      multiWriteSuccessCount = 0;
      multiWriteListSize = listPickedTags.size();
      multiWriteCount = multiWriteListSize;
      isMultiWriteDone = false;
      if(mReader.SetTxPower(MAX_POWER_TO_SET) == ClResult.S_OK.ordinal()){
        clearFilters();
        setSession(sessionAction);
        isEncodeOn.postValue(true);
        //setProgressMessage(context.getString(R.string.msg_pick), true);
        for(Inventory inventory : listPickedTags){
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
    if(isMultiEncode || (mReader.SetTxPower(MAX_POWER_TO_SET) == ClResult.S_OK.ordinal() && mReader.SetRFIDMode(RFIDMode.WriteTag) == ClResult.S_OK.ordinal())){
      if(!isMultiEncode){
        clearFilters();
        setSession(sessionAction);
        isEncodeOn.postValue(true);
        isMultiWriteDone = false;
        //setProgressMessage(context.getString(R.string.msg_pick), true);
      }
      encPickedTag = pickedTag;
      //startEncoding(pickTag, currentTagPassword, 0);
      final List<String> listPasswords = SharedPrefManager.getOldAccessPasswords();
      if(!listPasswords.contains(defaultTagZeroPassword))
        listPasswords.add(0, defaultTagZeroPassword);
      if(listPasswords.contains(SharedPrefManager.getCurrentAccessPassword()))
        listPasswords.remove(SharedPrefManager.getCurrentAccessPassword());
      listPasswords.add(0, SharedPrefManager.getCurrentAccessPassword());
      if(SharedPrefManager.getNonPasswordTids().contains(pickTag.tid.toUpperCase().substring(0, 8))){
        if(listPasswords.contains(defaultTagZeroPassword))
          listPasswords.remove(defaultTagZeroPassword);
        listPasswords.add(0, defaultTagZeroPassword);
      }
      this.listTagPasswords.clear();
      this.listTagPasswords.addAll(listPasswords);
      this.listTagPasswordIndex = 0;
      //Use 1 by 1 Password instead of Loop for Encoding (Since there is issue in DeviceResponse for Write Command)
      startEncodePass(pickedTag, false, listTagPasswordIndex);
    }
  }
  
  private void startEncoding(final Inventory pickedTag, final String currentTagPassword, final int encodeRetryCount){
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
        dataLen = sgtin.length() / 2;
        offSet = (dataLen % 4 == 0 ? 4 : 2);
    }
    showLog("dataLen", "" + dataLen);
    showLog("offSet", "" + offSet);
    showLog("currentAccessPassword", "" + SharedPrefManager.getCurrentAccessPassword());
    if(offSet > 0 && dataLen > 0){
      final List<String> listPasswords = SharedPrefManager.getOldAccessPasswords();
      if(!listPasswords.contains(defaultTagZeroPassword))
        listPasswords.add(0, defaultTagZeroPassword);
      if(listPasswords.contains(SharedPrefManager.getCurrentAccessPassword()))
        listPasswords.remove(SharedPrefManager.getCurrentAccessPassword());
      listPasswords.add(0, SharedPrefManager.getCurrentAccessPassword());
      if(SharedPrefManager.getNonPasswordTids().contains(tid.toUpperCase().substring(0, 8))){
        if(listPasswords.contains(defaultTagZeroPassword))
          listPasswords.remove(defaultTagZeroPassword);
        listPasswords.add(0, defaultTagZeroPassword);
      }
      
      final byte[] epcDataBytes = getHexByteArray(epc);
      final byte[] tidDataBytes = getHexByteArray(tid);
      final byte[] sgtinDataBytes = getHexByteArray(sgtin);
      
      boolean isWriteSuccess = false;
      String tagPass = "";
      for(String pass : listPasswords){
        showLog("pass", pass);
        if(isNonEmpty(pass)){
          //DeviceResponse deviceResponse = mReader.RFIDDirectWriteTagByEPC(getHexByteArray(pass), epcDataBytes, RFIDMemoryBank.EPC, offSet, 1, sgtinDataBytes);
          DeviceResponse deviceResponse = mReader.RFIDDirectWriteTagByTID(getHexByteArray(pass), tidDataBytes, RFIDMemoryBank.EPC, offSet, 1, sgtinDataBytes);
          showLog("response_enc_tag_write", deviceResponse.name() + "_" + deviceResponse.getValue());
          /*Note: Current SDK Version gives success device response even if wrong password is used or command is actually failed
            this may be resolved in their latest sdk version need to check availability for the same for integration*/
          if(deviceResponse == DeviceResponse.OperationSuccess){
            showLog("WriteSuccess", "true");
            tagPass = pass;
            isWriteSuccess = true;
            break;
          }
          else showLog("error_enc_tag_write", mReader.GetLastError());
        }
      }
      if(isWriteSuccess){
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
            int updatedRows = inventoryDao.updateInventoryData(pickedTag);
            //showLog("updatedRows",""+updatedRows);
          }
          else inventoryDao.insertInventoryData(pickedTag);
          try{
            uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
          }
          catch(Exception e){ e.printStackTrace(); }
          AppCommonMethods.successBeep();
          if(!tagPass.equalsIgnoreCase(SharedPrefManager.getCurrentAccessPassword()))
            writePasswordAndLock(pickedTag, tagPass, encodeRetryCount);
          updateTagWriteCount(true);
        }
        catch(SQLiteConstraintException e){ e.printStackTrace(); }
        catch(Exception e){
          e.printStackTrace();
        }
      }
      if(!isWriteSuccess){
        //((MainActivity) context).showCustomErrDialog(R.string.err_encoding_auth_fail);
        //isEncodeOn.postValue(false);
        //setProgressMessage(false);
        updateTagWriteCount(context.getString(R.string.err_encoding_auth_fail));
        showLog("LOCKMEMORY1", "FAIL");
      }
    }
    else{
      //setProgressMessage(false);
      //((MainActivity) context).showCustomErrDialog(R.string.err_encoding_write_fail);
      updateTagWriteCount(context.getString(R.string.err_encoding_write_fail));
    }
  }
  
  private void startEncodePass(final Inventory pickedTag, final boolean isWriteSuccess, int index){
    final String barcode = pickedTag.ean;
    final String epc = pickedTag.epc;
    final String tid = pickedTag.tid;
    final String sgtin = pickedTag.newEpc.trim();
    final String pc = pickedTag.pcdata;
    int offSet = 0;
    int dataLen = 0;
    if(index >= listTagPasswords.size()){
      updateTagWriteCount(context.getString(R.string.err_encoding_auth_fail));
      showLog("LOCKMEMORY1", "FAIL");
      return;
    }
    String pass = listTagPasswords.get(index);
    showLog("pass_index_listSize_isWriteSuccess", pass + "_(" + index + "/" + listTagPasswords.size() + ")_" + isWriteSuccess);
    if(isWriteSuccess && chkNull(pass, "").length() > 0){
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
          int updatedRows = inventoryDao.updateInventoryData(pickedTag);
          //showLog("updatedRows",""+updatedRows);
        }
        else inventoryDao.insertInventoryData(pickedTag);
        try{
          uploadInventoryDao.insertUploadInventoryData(new UploadInventory(pickedTag, extras));
        }
        catch(Exception e){ e.printStackTrace(); }
        AppCommonMethods.successBeep();
        if(!pass.equalsIgnoreCase(SharedPrefManager.getCurrentAccessPassword()))
          writePasswordAndLock(pickedTag, pass, index);
        updateTagWriteCount(true);
      }
      catch(SQLiteConstraintException e){ e.printStackTrace(); }
      catch(Exception e){
        e.printStackTrace();
      }
      return;
    }
    
    switch(sgtin.length()){
      case 28:
      case 36:
      case 24:
      case 32:
        dataLen = sgtin.length() / 2;
        offSet = (dataLen % 4 == 0 ? 4 : 2);
    }
    final byte[] epcDataBytes = getHexByteArray(pickedTag.epc);
    final byte[] tidDataBytes = getHexByteArray(pickedTag.tid);
    final byte[] sgtinDataBytes = getHexByteArray(sgtin);
    
    if(offSet > 0 && dataLen > 0){
      //DeviceResponse deviceResponse = mReader.RFIDDirectWriteTagByEPC(getHexByteArray(pass), epcDataBytes, RFIDMemoryBank.EPC, offSet, 1, sgtinDataBytes);
      DeviceResponse deviceResponse = mReader.RFIDDirectWriteTagByTID(getHexByteArray(pass), tidDataBytes, RFIDMemoryBank.EPC, offSet, 1, sgtinDataBytes);
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
  private void writePasswordAndLock(final Inventory pickedTag, final String currentTagPassword, final int encodeRetryCount){
    isWritePass = true;
    showLog("Write_Lock_CurrentTagPassword_retryCount", currentTagPassword + "_" + encodeRetryCount);
    final String tid = pickedTag.tid;
    final String currentAccessPass = SharedPrefManager.getCurrentAccessPassword();
    final byte[] tidDataBytes = getHexByteArray(tid);
    final byte[] epcDataBytes = getHexByteArray(pickedTag.newEpc);
    final byte[] tagPassDataBytes = getHexByteArray(currentTagPassword);
    final byte[] accessPassDataBytes = getHexByteArray(currentAccessPass);
    
    //mReader.RFIDDirectAuthenticateTag()?
    //DeviceResponse deviceResponse = mReader.RFIDDirectWriteTagByTID(tagPassDataBytes, tidDataBytes, RFIDMemoryBank.Reserved, 4, 1, accessPassDataBytes);
    DeviceResponse deviceResponse = mReader.RFIDDirectWriteTagByEPC(tagPassDataBytes, epcDataBytes, RFIDMemoryBank.Reserved, 4, 1, accessPassDataBytes);
    showLog("deviceResponse_enc_pass_write11", "" + deviceResponse.name());
    if(deviceResponse == DeviceResponse.OperationSuccess){
      showLog("WRITEPASS11", "SUCCESS");
      if(false && !SharedPrefManager.getCurrentAccessPassword().equalsIgnoreCase(defaultTagZeroPassword)){
        DeviceResponse deviceResponse1 = mReader.RFIDDirectLockTag(accessPassDataBytes, epcDataBytes, LockTarget.AccessPassword);
        showLog("deviceResponse_enc_pass_lock11", "" + deviceResponse1.name());
        if(deviceResponse1 == DeviceResponse.OperationSuccess) showLog("LOCKMEMORY11", "Success");
        else showLog("error_enc_pass_lock", mReader.GetLastError());
      }
    }
    else showLog("error_enc_pass_write", mReader.GetLastError());
  }
  
  private void lockPassWord(final Inventory pickedTag){
    final String tid = pickedTag.tid;
    final String currentAccessPass = SharedPrefManager.getCurrentAccessPassword();
    final byte[] tidDataBytes = getHexByteArray(tid);
    final byte[] epcDataBytes = getHexByteArray(pickedTag.newEpc);
    final byte[] accessPassDataBytes = getHexByteArray(currentAccessPass);
    
    showLog("lockPassWord", currentAccessPass);
    
    DeviceResponse deviceResponse1 = mReader.RFIDDirectLockTag(accessPassDataBytes, epcDataBytes, LockTarget.AccessPassword);
    showLog("deviceResponse_enc_pass_lock11", "" + deviceResponse1.name());
    if(deviceResponse1 == DeviceResponse.OperationSuccess) showLog("LOCKMEMORY11", "Success");
    else showLog("error_enc_pass_lock", mReader.GetLastError());
    
  }
  
  @Override
  public void readTagCurrentPassword(Inventory pickedTag){
    final List<String> listPasswords = SharedPrefManager.getOldAccessPasswords();
    if(!listPasswords.contains(defaultTagZeroPassword))
      listPasswords.add(0, defaultTagZeroPassword);
    if(listPasswords.contains(SharedPrefManager.getCurrentAccessPassword()))
      listPasswords.remove(SharedPrefManager.getCurrentAccessPassword());
    listPasswords.add(0, SharedPrefManager.getCurrentAccessPassword());
    if(SharedPrefManager.getNonPasswordTids().contains(pickedTag.tid.toUpperCase().substring(0, 8))){
      if(listPasswords.contains(defaultTagZeroPassword))
        listPasswords.remove(defaultTagZeroPassword);
      listPasswords.add(0, defaultTagZeroPassword);
    }
    
    boolean isReadSuccess = false;
    String tagPass = "";
    
    showLog("tid", pickedTag.tid);
    final byte[] tidDataBytes = getHexByteArray(pickedTag.tid);
    showLog("tidDataBytes", print(tidDataBytes));
    showLog("epc", pickedTag.epc);
    final byte[] epcDataBytes = getHexByteArray(pickedTag.epc);
    showLog("epcDataBytes", print(epcDataBytes));
    
    //byte[] password0 = new byte[] { (byte)0x00, (byte)0x00 ,(byte)0x00, (byte)0x00};
    //showLog("password0",bytesToHex(password0));
    //    byte[] tidBytes = new byte[] { (byte)0xe2, (byte)0x80 ,(byte)0x68, (byte)0x94, (byte)0x20, (byte)0x00 ,(byte)0x40, (byte)0x26, (byte)0x4a, (byte)0x94, (byte)0xc8 ,(byte)0xb0}; //e2806894200040264a94c8b0
    //    showLog("tidBytes",bytesToHex(tidBytes));
    //byte[] epcBytes = new byte[] { (byte)0x30, (byte)0x36 ,(byte)0x11, (byte)0xde, (byte)0xb4, (byte)0x15 ,(byte)0x8b, (byte)0xd2, (byte)0xa0, (byte)0x5f, (byte)0x20 ,(byte)0xce}; //303611deb4158bd2a05f20ce
    //showLog("epcBytes",bytesToHex(epcBytes));
    mReader.SetRFIDMode(RFIDMode.ReadTag);
    for(String pass : listPasswords){
      showLog("pass", pass);
      if(isNonEmpty(pass)){
        final byte[] passDataBytes = getHexByteArray(pass);
        //showLog("PassData",bytesToHex(passDataBytes));
        if(mReader.RFIDDirectReadTagByEPC(passDataBytes, epcDataBytes, RFIDMemoryBank.Reserved, 4, 4, 1) == ClResult.S_OK.ordinal()){
          //if(mReader.RFIDDirectReadTagByEPC(passDataBytes, epcDataBytes, RFIDMemoryBank.TID, 0, 12,1)==ClResult.S_OK.ordinal()){
          //if(mReader.RFIDDirectReadTagByTID(passDataBytes, tidDataBytes, RFIDMemoryBank.EPC, 4, 18,1)==ClResult.S_OK.ordinal()){
          showLog("ReadSuccess", "true");
          tagPass = pass;
          isReadSuccess = true;
          //break;
        }
        else showLog("error", mReader.GetLastError());
      }
    }
    if(isReadSuccess) showLog("tagPass", tagPass);
    else showLog("tagPass", mReader.GetLastError());
  }
  
  private String print(byte[] bytes){
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for(byte b : bytes){
      sb.append(String.format("0x%02X ", b));
    }
    sb.deleteCharAt(sb.lastIndexOf(" "));
    sb.append("]");
    return sb.toString();
  }
  
  @Override
  public void performDecoding(Inventory pickedTag){
  
  }
  
  @Override
  public void performDecoding(List<Inventory> listPickedTags, AppCommonMethods.SessionType sessionType){
  
  }
  
  private void storeInventoryData(final Bundle tagData){
    try{
      if(isNonEmpty(sessionId) && sessionType.getValue() > 0){
        if(isCommandForSearch || sessionAction == AppCommonMethods.SessionAction.SEARCH){
          Inventory inventory = getDataFromTagInfo(tagData);
          String epc = inventory.epc;//uhftagInfo.getEPC();
          showLog("epc", "" + epc);
          final int rssi = (int) Math.round(Double.parseDouble(inventory.rssi));
          showLog("rssi", "" + inventory.rssi);
          int actualPercentage = getPercentage(rssi);
          percent = actualPercentage;
          showLog("actualPer", "" + actualPercentage);
          if(!isCommandForEPCSearch){
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
                  searchRssi.postValue(String.valueOf(inventory.rssi));
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
            if(epc.equalsIgnoreCase(SEARCH_EPC)){
              searchPercent.postValue(actualPercentage);
              searchRssi.postValue(inventory.rssi);
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
          Inventory inventory = getDataFromTagInfo(tagData);
          inventory.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
          if(/*!AppCommonMethods.isSetInwOnline &&*/ (sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD)){
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
            if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY)//sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY)
              AppDatabase.getBrandEansDao(context).updateScanQty("," + inventory.ean + ",");
          }
        }
      }
    }
    catch(SQLiteConstraintException e){
      e.printStackTrace();
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void stopInventory(){
    super.stopInventory();
    if(loopFlag){
      loopFlag = false;
      mReader.SoftScanTrigger(false);
      mReader.SetScanMode(ScanMode.Err);
      mReader.RFIDDirectCancelInventoryRound();
    }
  }
  
  /**
   * Read tag.
   */
  private void readTag(){
    if(!loopFlag){
      if((isActionPick ? mReader.RFIDDirectStartInventoryRound(InventoryType.EPC_AND_TID, isActionPick ? 10 : 254) : mReader.SoftScanTrigger(true)) == ClResult.S_OK.ordinal()){
        //if(mReader.RFIDDirectStartInventoryRound(InventoryType.EPC, 10000) == ClResult.S_OK.ordinal()){
        loopFlag = true;
        if(isCommandForSearch) isSearchOn.postValue(true);
        else if(!isActionPick) isInventoryOn.postValue(true);
        startTimer();
      }
      else{
        stopInventory();
      }
    }
    else{
      loopFlag = false;
      stopInventory();
    }
  }
  
  @Override
  public void onDestroy(){
    super.onDestroy();
    if(myDataReceiver != null){
      context.unregisterReceiver(myDataReceiver);
      isReceiverRegistered = false;
    }
    if(mReader != null) mReader.Release();
  }
}
