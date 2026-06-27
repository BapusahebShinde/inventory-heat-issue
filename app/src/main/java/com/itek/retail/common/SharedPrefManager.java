package com.itek.retail.common;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.getGSON;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelFormatException;

import com.itek.retail.apis.ParamConstants;
import com.itek.retail.model.MenuModel;
import com.itek.retail.reader.RFIDHandler;
import com.itek.retail.sgtin.SGTIN128Helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Shared pref manager.
 * used for all Shared Preferences Operations throughout the app
 * contains Shared Pref keys and getter/setter methods
 */
public class SharedPrefManager{
  
  public static final String SHARED_PREF_NAME = "Retail_Store";
  private static final String LIST_OBJ_SEPARATOR = "~;%;";
  private static final String LIST_CLASS_SEPARATOR = ";~;";
  private static SharedPreferences pref;
  
  /**
   * Init.
   *
   * @param context the context
   */
  public static void init(Context context){
    if(pref == null && context != null)
      pref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
  }
  
  /**
   * Get is chk rationale boolean.
   *
   * @return the boolean
   */
  public static Boolean getIsChkRationale(){ return getBoolean(SharedPrefKeys.IS_CHK_RATIONALE); }
  
  /**
   * Set is chk rationale.
   *
   * @param isChkRationale the is chk rationale
   */
  public static void setIsChkRationale(Boolean isChkRationale){
    if(pref != null && isChkRationale != null)
      setBoolean(SharedPrefKeys.IS_CHK_RATIONALE, isChkRationale);
  }
  
  /**
   * Get is profile set boolean.
   *
   * @return the boolean
   */
  public static Boolean getIsProfileSet(){ return getBoolean(SharedPrefKeys.IS_PROFILE_SET); }
  
  /**
   * Set is profile set.
   *
   * @param isProfileSet the is profile set
   */
  public static void setIsProfileSet(Boolean isProfileSet){
    if(pref != null && isProfileSet != null)
      setBoolean(SharedPrefKeys.IS_PROFILE_SET, isProfileSet);
  }
  
  /**
   * Get imei string.
   *
   * @return the string
   */
  
  public static String getIMEI(){ return getString(SharedPrefKeys.IMEI).toString(); }
  
  /**
   * Set imei.
   *
   * @param IMEI the imei
   */
  public static void setIMEI(String IMEI){
    if(pref != null && isNonEmpty(IMEI)) setString(SharedPrefKeys.IMEI, IMEI);
  }
  
  /**
   * Get firebase token string.
   *
   * @return the string
   */
  public static String getFirebaseToken(){ return getString(SharedPrefKeys.FIREBASE_TOKEN); }
  
  /**
   * Set firebase token.
   *
   * @param firebaseToken the firebase token
   */
  public static void setFirebaseToken(String firebaseToken){
    if(pref != null && isNonEmpty(firebaseToken))
      setString(SharedPrefKeys.FIREBASE_TOKEN, firebaseToken);
  }
  
  /**
   * Get install date string.
   *
   * @return the string
   */
  public static String getInstallDate(){ return getString(SharedPrefKeys.INSTALL_DATE); }
  
  /**
   * Set install date.
   *
   * @param installDate the install date
   */
  public static void setInstallDate(String installDate){
    if(pref != null && installDate != null) setString(SharedPrefKeys.INSTALL_DATE, installDate);
  }
  
  /**
   * Get app version string.
   *
   * @return the string
   */
  public static String getAppVersion(){ return getString(SharedPrefKeys.APP_VERSION); }
  
  /**
   * Set app version.
   *
   * @param appVersion the app version
   */
  public static void setAppVersion(String appVersion){
    if(pref != null && appVersion != null) setString(SharedPrefKeys.APP_VERSION, appVersion);
  }
  
  /**
   * Get reader sdk version string.
   *
   * @return the string
   */
  public static String getReaderSDKVersion(){ return getString(SharedPrefKeys.READER_VERSION); }
  
  /**
   * Set reader sdk version.
   *
   * @param readerSDKVersion the reader sdk version
   */
  public static void setReaderSDKVersion(String readerSDKVersion){
    if(pref != null && readerSDKVersion != null)
      setString(SharedPrefKeys.READER_VERSION, readerSDKVersion);
  }
  
  /**
   * Get mac address string.
   *
   * @return the string
   */
  public static String getMACAddress(){ return getString(SharedPrefKeys.MAC_ADDRESS); }
  
  /**
   * Set mac address.
   *
   * @param macAddress the mac address
   */
  public static void setMACAddress(String macAddress){
    if(pref != null && isNonEmpty(macAddress)) setString(SharedPrefKeys.MAC_ADDRESS, macAddress);
  }
  
  /**
   * Get printer address string.
   *
   * @return the string
   */
  public static String getPrinterAddress(){ return getString(SharedPrefKeys.PRINTER_ADDRESS); }
  
  /**
   * Set printer address.
   *
   * @param printerAddress the printer address
   */
  public static void setPrinterAddress(String printerAddress){
    if(pref != null && isNonEmpty(printerAddress)) setString(SharedPrefKeys.PRINTER_ADDRESS, printerAddress);
  }
  
  /**
   * Get lat lng string.
   *
   * @return the string
   */
  public static String getLatLng(){ return getString(SharedPrefKeys.LAT_LNG); }
  
  /**
   * Set lat lng.
   *
   * @param latLng the lat lng
   */
  public static void setLatLng(String latLng){
    if(pref != null && isNonEmpty(latLng)) setString(SharedPrefKeys.LAT_LNG, latLng);
  }
  
  /**
   * Set lat lng.
   *
   * @param lat the lat
   * @param lng the lng
   */
  public static void setLatLng(double lat, double lng){
    if(pref != null && lat > 0 && lng > 0) setString(SharedPrefKeys.LAT_LNG, lat + "," + lng);
  }
  
  /**
   * Get device type app common methods . device type.
   *
   * @return the app common methods . device type
   */
  public static AppCommonMethods.DeviceType getDeviceType(){ return AppCommonMethods.DeviceType.get(getDeviceTypeValue()); }
  
  /**
   * Set device type.
   *
   * @param deviceType the device type
   */
  public static void setDeviceType(AppCommonMethods.DeviceType deviceType){ setDeviceType(deviceType.getValue()); }
  
  /**
   * Set device type.
   *
   * @param deviceType the device type
   */
  public static void setDeviceType(Integer deviceType){
    if(pref != null && deviceType >= 0) setInt(SharedPrefKeys.DEVICE_TYPE, deviceType);
  }
  
  /**
   * Get device type app common methods . device type.
   *
   * @return the app common methods . device type
   */
  public static AppCommonMethods.SensorType getSensorType(){ return AppCommonMethods.SensorType.get(getSensorTypeValue()); }
  
  /**
   * Get device type value integer.
   *
   * @return the integer
   */
  public static Integer getDeviceTypeValue(){ return getInt(SharedPrefKeys.DEVICE_TYPE); }
  
  /**
   * Get device type value integer.
   *
   * @return the integer
   */
  public static Integer getSensorTypeValue(){ return getInt(SharedPrefKeys.SENSOR_TYPE); }
  
  public static void setSensorTypeValue(int sensorType){
    if(pref != null && sensorType >= 0) setInt(SharedPrefKeys.SENSOR_TYPE, sensorType);
  }
  
  /**
   * Get device model string.
   *
   * @return the string
   */
  public static String getDeviceModel(){ return getString(SharedPrefKeys.DEVICE_MODEL); }
  
  /**
   * Set device model.
   *
   * @param deviceModel the device model
   */
  public static void setDeviceModel(String deviceModel){
    if(pref != null && deviceModel != null) setString(SharedPrefKeys.DEVICE_MODEL, deviceModel);
  }
  
  /**
   * Get ean type string.
   *
   * @return the string
   */
  public static String getEanType(){ return getString(SharedPrefKeys.EAN_TYPE, AppConstants.EAN_TYPE_STD); }
  
  /**
   * Set ean type.
   *
   * @param eanType the ean type
   */
  public static void setEanType(String eanType){
    if(pref != null && eanType != null) setString(SharedPrefKeys.EAN_TYPE, eanType.toLowerCase());
  }
  
  public static boolean isStdEanType(){
    return getEanType().equalsIgnoreCase(AppConstants.EAN_TYPE_STD);
  }
  
  /**
   * Get is allow non std eans type Boolean.
   *
   * @return the Boolean
   */
  public static boolean getIsAllowNonStdEans(){ return getBoolean(ParamConstants.IS_ALLOW_NON_STD_EAN, AppCommonMethods.isAllowNonStdEan); }
  
  /**
   * Set is allow non std eans.
   *
   * @param isAllowNonStdEans the allow non std eans.
   */
  public static void setIsAllowNonStdEans(Boolean isAllowNonStdEans){
    if(pref != null && isAllowNonStdEans != null)
      setBoolean(ParamConstants.IS_ALLOW_NON_STD_EAN, isAllowNonStdEans);
  }
  
  /**
   * Get is allow alpha numeric non std eans type Boolean.
   *
   * @return the Boolean
   */
  public static boolean getIsAllowAlphaNumericNonStdEans(){ return getBoolean(ParamConstants.IS_ALLOW_ALPHANUMERIC_NON_STD_EAN, SGTIN128Helper.isAllowAlphanumericNonStdEan); }
  
  /**
   * Set is allow alpha numeric non std eans.
   *
   * @param isAllowAlphaNumericNonStdEans the is allow alpha numeric non std eans.
   */
  public static void setIsAllowAlphaNumericNonStdEans(Boolean isAllowAlphaNumericNonStdEans){
    if(pref != null && isAllowAlphaNumericNonStdEans != null)
      setBoolean(ParamConstants.IS_ALLOW_ALPHANUMERIC_NON_STD_EAN, isAllowAlphaNumericNonStdEans);
  }
  
  /**
   * Get encode type string.
   *
   * @return the string
   */
  public static String getEncodeType(){ return getString(SharedPrefKeys.ENCODING_TYPE, !getString(SharedPrefKeys.ENCODING_BARCODE_RFID_TYPE, AppConstants.ENCODE_TYPE_BARCODE_RFID).equalsIgnoreCase(AppConstants.ENCODE_TYPE_BARCODE_RFID) ? getString(SharedPrefKeys.ENCODING_BARCODE_RFID_TYPE, AppConstants.ENCODE_TYPE_BARCODE_RFID) : getString(SharedPrefKeys.ENCODING_RELATION_TYPE, AppConstants.ENCODE_TYPE_ONE)); }
  
  /**
   * Set encode type.
   *
   * @param encodeType the encode type
   */
  public static void setEncodeType(String encodeType){
    if(pref != null && encodeType != null)
      setString(SharedPrefKeys.ENCODING_TYPE, encodeType.replaceAll(" to ", "-"));
  }
  
  /**
   * Get encode relation type string.
   *
   * @return the string
   */
  public static String getEncodeRelationType(){ return getString(SharedPrefKeys.ENCODING_RELATION_TYPE, AppConstants.ENCODE_TYPE_ONE); }
  
  /**
   * Set encode relation type.
   *
   * @param encodeRelationType the encode relation type
   */
  public static void setEncodeRelationType(String encodeRelationType){
    if(pref != null && encodeRelationType != null)
      setString(SharedPrefKeys.ENCODING_RELATION_TYPE, encodeRelationType);
  }
  
  /**
   * Get encode barcode rfid type string.
   *
   * @return the string
   */
  public static String getEncodeBarcodeRFIDType(){ return getString(SharedPrefKeys.ENCODING_BARCODE_RFID_TYPE, AppConstants.ENCODE_TYPE_BARCODE_RFID); }
  
  /**
   * Set encode barcode rfid type.
   *
   * @param encodeBarcodeRFIDType the encode barcode rfid type
   */
  public static void setEncodeBarcodeRFIDType(String encodeBarcodeRFIDType){
    if(pref != null && encodeBarcodeRFIDType != null)
      setString(SharedPrefKeys.ENCODING_BARCODE_RFID_TYPE, encodeBarcodeRFIDType);
  }
  
  /**
   * Get replenishment type string.
   *
   * @return the string
   */
  public static String getReplenishmentType(){ return getString(SharedPrefKeys.REPLENISHMENT_TYPE); }
  
  /**
   * Set replenishment type.
   *
   * @param replenishmentType the replenishment type
   */
  public static void setReplenishmentType(String replenishmentType){
    if(pref != null && replenishmentType != null)
      setString(SharedPrefKeys.REPLENISHMENT_TYPE, replenishmentType);
  }
  
  /**
   * Get omnichannel type string.
   *
   * @return the string
   */
  public static String getOmnichannelType(){ return getString(SharedPrefKeys.OMNICHANNEL_TYPE); }
  
  /**
   * Set omnichannel type.
   *
   * @param omnichannelType the omnichannel type
   */
  public static void setOmnichannelType(String omnichannelType){
    if(pref != null && omnichannelType != null)
      setString(SharedPrefKeys.OMNICHANNEL_TYPE, omnichannelType);
  }
  
  /**
   * Get unencoded search type string.
   *
   * @return the string
   */
  public static String getUnencodedSearchType(){ return getString(SharedPrefKeys.UNENCODED_SEARCH_TYPE); }
  
  /**
   * Set unencoded search type.
   *
   * @param unencodedSearchType the unencoded search type
   */
  public static void setUnencodedSearchType(String unencodedSearchType){
    if(pref != null && unencodedSearchType != null)
      setString(SharedPrefKeys.UNENCODED_SEARCH_TYPE, unencodedSearchType);
  }
  
  /**
   * Get unencoded search type string.
   *
   * @return the string
   */
  public static String getAlienSearchType(){ return getString(SharedPrefKeys.ALIEN_SEARCH_TYPE); }
  
  /**
   * Set alien search type.
   *
   * @param alienSearchType the alien search type
   */
  public static void setAlienSearchType(String alienSearchType){
    if(pref != null && alienSearchType != null)
      setString(SharedPrefKeys.ALIEN_SEARCH_TYPE, alienSearchType);
  }
  
  /**
   * Get trigger key codes string.
   *
   * @return the string
   */
  public static String getTriggerKeyCodes(){ return ("," + getString(SharedPrefKeys.DEVICE_TRIGGER_KEY_CODES) + ",").replaceAll("\\s", "").replaceAll(",,", "").trim(); }
  
  /**
   * Set trigger key codes.
   *
   * @param triggerKeyCodes the trigger key codes
   */
  public static void setTriggerKeyCodes(String triggerKeyCodes){
    if(pref != null && triggerKeyCodes != null)
      setString(SharedPrefKeys.DEVICE_TRIGGER_KEY_CODES, triggerKeyCodes);
  }
  
  /**
   * Get is device bluetooth dependent boolean.
   *
   * @return the boolean
   */
  public static Boolean getIsDeviceBluetoothDependent(){ return getBoolean(SharedPrefKeys.DEVICE_BLUETOOTH_DEPENDENT); }
  
  /**
   * Set is device bluetooth dependent.
   *
   * @param isDeviceBluetoothDependent the is device bluetooth dependent
   */
  public static void setIsDeviceBluetoothDependent(Boolean isDeviceBluetoothDependent){
    if(pref != null && isDeviceBluetoothDependent != null)
      setBoolean(SharedPrefKeys.DEVICE_BLUETOOTH_DEPENDENT, isDeviceBluetoothDependent);
  }
  
  /**
   * Get is ean mapped boolean.
   *
   * @return the boolean
   */
  public static Boolean getIsEANMapped(){ return getBoolean(ParamConstants.IS_EAN_MAPPING); }
  
  /**
   * Set is ean mapped.
   *
   * @param isEanMapped the is ean mapped
   */
  public static void setIsEanMapped(Boolean isEanMapped){
    if(pref != null && isEanMapped != null) setBoolean(ParamConstants.IS_EAN_MAPPING, isEanMapped);
  }
  
  /**
   * Get is 11 digit std ean boolean.
   *
   * @return the boolean
   */
  public static Boolean getIs11DigitStdEAN(){ return getBoolean(ParamConstants.IS_11_DIGIT_STD_EAN); }
  
  /**
   * Set is 11 digit std ean
   *
   * @param is11DigitStdEAN the is 11 digit std ean
   */
  public static void setIs11DigitStdEAN(Boolean is11DigitStdEAN){
    if(pref != null && is11DigitStdEAN != null)
      setBoolean(ParamConstants.IS_11_DIGIT_STD_EAN, is11DigitStdEAN);
  }
  
  /**
   * Get server url string.
   *
   * @return the string
   */
  public static String getServerUrl(){ return getString(SharedPrefKeys.SERVER_URL); }
  
  /**
   * Set server url.
   *
   * @param serverUrl the server url
   */
  public static void setServerUrl(String serverUrl){
    if(pref != null && serverUrl != null) setString(SharedPrefKeys.SERVER_URL, serverUrl);
  }
  
  /**
   * Get server url string.
   *
   * @return the string
   */
  public static String getUpdateAPKPath(){ return getString(SharedPrefKeys.UPDATE_APK_PATH); }
  
  /**
   * Set server url.
   *
   * @param serverUrl the server url
   */
  public static void setUpdateAPKPath(String serverUrl){
    if(pref != null && serverUrl != null) setString(SharedPrefKeys.UPDATE_APK_PATH, serverUrl);
  }
  
  /**
   * Get access token string.
   *
   * @return the string
   */
  public static String getAccessToken(){ return getString(SharedPrefKeys.ACCESS_TOKEN); }
  
  /**
   * Set access token.
   *
   * @param accessToken the access token
   */
  public static void setAccessToken(String accessToken){
    if(pref != null && accessToken != null) setString(SharedPrefKeys.ACCESS_TOKEN, accessToken);
  }
  
  /**
   * Get is server url configured boolean.
   *
   * @return the boolean
   */
  public static Boolean getIsServerURLConfigured(){ return getBoolean(SharedPrefKeys.IS_SERVER_URL_CONFIGURED); }
  
  /**
   * Set is server url configured.
   *
   * @param isServerURLConfigured the is server url configured
   */
  public static void setIsServerURLConfigured(Boolean isServerURLConfigured){
    if(pref != null && isServerURLConfigured != null)
      setBoolean(SharedPrefKeys.IS_SERVER_URL_CONFIGURED, isServerURLConfigured);
  }
  
  /**
   * Get access token time long.
   *
   * @return the long
   */
  public static Long getAccessTokenTime(){
    try{
      Calendar cc = Calendar.getInstance();
      String date = getString(SharedPrefKeys.TOKEN_TIME, "");
      if(date.length() <= 0) return 0l;
      Date tokenValidDate = new SimpleDateFormat(DATE_TIME_FORMAT).parse(date);
      if(!cc.getTime().before(tokenValidDate)) return 0l;
      else return (tokenValidDate.getTime() - cc.getTimeInMillis()) / 1000;
    }
    catch(ParcelFormatException | ParseException e){
      e.printStackTrace();
      return 0l;
    }
  }
  
  /**
   * Set access token time.
   *
   * @param accessTokenTime the access token time
   */
  public static void setAccessTokenTime(Long accessTokenTime){
    if(pref != null && accessTokenTime != null && accessTokenTime > 0){
      Calendar cc = Calendar.getInstance();
      cc.add(Calendar.SECOND, accessTokenTime.intValue());
      setString(SharedPrefKeys.TOKEN_TIME, new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime()));
    }
  }
  
  /**
   * Get battery Reader Percent.
   *
   * @return the integer
   */
  public static Integer getBatteryReaderPercent(){ return getInt(SharedPrefKeys.BATTERY_READER_PERCENT, 0); }
  
  /**
   * Set battery Reader Percent
   *
   * @param batteryRederPercent the reader power
   */
  public static void setBatteryReaderPercent(Integer batteryRederPercent){
    if(pref != null && batteryRederPercent != null){
      setInt(SharedPrefKeys.BATTERY_READER_PERCENT, batteryRederPercent);
    }
  }
  
  /**
   * Get battery Charged Percent integer.
   *
   * @return the integer
   */
  public static Integer getBatteryChargedPercent(){ return getInt(SharedPrefKeys.BATTERY_CHARGED_PERCENT, 20); }
  
  /**
   * Set battery Charged Percent .
   *
   * @param batteryChargedPercent the reader power
   */
  public static void setBatteryChargedPercent(Integer batteryChargedPercent){
    if(pref != null && batteryChargedPercent != null){
      setInt(SharedPrefKeys.BATTERY_CHARGED_PERCENT, batteryChargedPercent);
    }
  }
  
  /**
   * Get reader power integer.
   *
   * @return the integer
   */
  public static Integer getReaderPower(){ return getInt(SharedPrefKeys.READER_POWER, 30); }
  
  /**
   * Set reader power.
   *
   * @param readerPower the reader power
   */
  public static void setReaderPower(Integer readerPower){
    if(pref != null && readerPower != null){
      setInt(SharedPrefKeys.READER_POWER, readerPower);
    }
  }
  
  /**
   * Get current access password string.
   *
   * @return the string
   */
  public static String getCurrentAccessPassword(){ return getString(SharedPrefKeys.CURRENT_ACCESS_PASSWORD, "00000000"); }
  
  /**
   * Set current access password.
   *
   * @param currentPassword the current password
   */
  public static void setCurrentAccessPassword(String currentPassword){
    if(pref != null && currentPassword != null)
      setString(SharedPrefKeys.CURRENT_ACCESS_PASSWORD, currentPassword);
  }
  
  /**
   * Get old access passwords array list.
   *
   * @return the array list
   */
  public static ArrayList<String> getOldAccessPasswords(){
    ArrayList<String> defOldPassword = new ArrayList<String>(0);
    defOldPassword.add("00000000");
    List<?> list = getArrayList(SharedPrefKeys.OLD_ACCESS_PASSWORDS);
    return (isNonEmpty(list) && list.get(0) instanceof String) ? (ArrayList<String>) list : defOldPassword;
    //return new ArrayList<String>(getStringSet(SharedPrefKeys.OLD_ACCESS_PASSWORDS, defOldPassword));
  }
  
  /**
   * Set old access passwords.
   *
   * @param oldAccessPasswords the old access passwords
   */
  public static void setOldAccessPasswords(ArrayList<String> oldAccessPasswords){
    setArrayList(SharedPrefKeys.OLD_ACCESS_PASSWORDS, isNonEmpty(oldAccessPasswords) ? oldAccessPasswords : new ArrayList<String>(Arrays.asList(new String[]{"00000000"})));
    //setStringSet(SharedPrefKeys.OLD_ACCESS_PASSWORDS, new HashSet<String>(oldAccessPasswords != null ? oldAccessPasswords : new ArrayList<String>(Arrays.asList(new String[]{"00000000"}))));
  }
  
  /**
   * Get non password tids.
   *
   * @return the array list
   */
  public static ArrayList<String> getNonPasswordTids(){
    Set<String> defNonPasswordTids = new HashSet<String>(0);
    defNonPasswordTids.addAll(RFIDHandler.NON_PASSWORD_TIDS);
    return new ArrayList<String>(getStringSet(SharedPrefKeys.NON_PASSWORD_TIDS, defNonPasswordTids));
  }
  
  /**
   * Set non password tids.
   *
   * @param nonPasswordTids the non password tids
   */
  public static void setNonPasswordTids(ArrayList<String> nonPasswordTids){
    setStringSet(SharedPrefKeys.NON_PASSWORD_TIDS, new HashSet<String>(nonPasswordTids != null ? nonPasswordTids : new ArrayList<String>(0)));
  }
  
  /**
   * Get non 128 bit tids.
   *
   * @return the array list
   */
  public static ArrayList<String> getNon128BitTids(){
    Set<String> defNon128BitTids = new HashSet<String>(0);
    defNon128BitTids.addAll(RFIDHandler.NON_128_BIT_TIDS);
    return new ArrayList<String>(getStringSet(SharedPrefKeys.NON_128_BIT_TIDS, defNon128BitTids));
  }
  
  /**
   * Set non 128 bit tids.
   *
   * @param non128BitTids the non 128 bit tids
   */
  public static void setNon128BitTids(ArrayList<String> non128BitTids){
    setStringSet(SharedPrefKeys.NON_128_BIT_TIDS, new HashSet<String>(non128BitTids != null ? non128BitTids : new ArrayList<String>(0)));
  }
  
  /**
   * Get old access passwords array list.
   *
   * @return the array list
   */
  public static ArrayList<String> getStringArrayList(final String key){
    return getStringArrayList(key,null);
  }
  
  /**
   * Get old access passwords array list.
   *
   * @return the array list
   */
  public static ArrayList<String> getStringArrayList(final String key,final ArrayList<String> def){
    ArrayList<String> defOldPassword = new ArrayList<String>(0);
    List<?> list = getArrayList(key);
    return (isNonEmpty(list) && list.get(0) instanceof String) ? (ArrayList<String>) list : def;
  }
  
  /**
   * Set array list string
   *
   * @param listStr the list string
   */
  public static void setStringArrayList(String key,ArrayList<String> listStr){
    setArrayList(key, isNonEmpty(listStr) ? listStr : new ArrayList<String>());
    //setStringSet(SharedPrefKeys.OLD_ACCESS_PASSWORDS, new HashSet<String>(oldAccessPasswords != null ? oldAccessPasswords : new ArrayList<String>(Arrays.asList(new String[]{"00000000"}))));
  }
  
  /**
   * Get is logged in boolean.
   *
   * @return the boolean
   */
  public static Boolean getIsLoggedIn(){ return getBoolean(SharedPrefKeys.IS_LOGGED_IN); }
  
  /**
   * Set is logged in.
   *
   * @param isLoggedIn the is logged in
   */
  public static void setIsLoggedIn(Boolean isLoggedIn){
    if(pref != null && isLoggedIn != null) setBoolean(SharedPrefKeys.IS_LOGGED_IN, isLoggedIn);
  }
  
  /**
   * Get saved fav menu codes array list.
   *
   * @return the array list
   */
  public static ArrayList<String> getSavedFavMenuCodes(){ return new ArrayList<String>(getStringSet(SharedPrefKeys.SAVED_FAV_MENU_CODES, new HashSet<String>(0))); }
  
  /**
   * Set saved fav menu codes.
   *
   * @param listSavedFavMenuIds the list saved fav menu ids
   */
  public static void setSavedFavMenuCodes(ArrayList<String> listSavedFavMenuIds){ setStringSet(SharedPrefKeys.SAVED_FAV_MENU_CODES, new HashSet<String>(listSavedFavMenuIds != null ? listSavedFavMenuIds : new ArrayList<String>(0))); }
  
  /**
   * Get client id string.
   *
   * @return the string
   */
  public static String getClientID(){ return getString(SharedPrefKeys.CLIENT_ID); }
  
  /**
   * Set client id.
   *
   * @param clientId the client id
   */
  public static void setClientID(String clientId){
    if(pref != null && clientId != null) setString(SharedPrefKeys.CLIENT_ID, clientId);
  }
  
  /**
   * Get client name string.
   *
   * @return the string
   */
  public static String getClientName(){ return getString(SharedPrefKeys.CLIENT_NAME); }
  
  /**
   * Set client name.
   *
   * @param clientName the client name
   */
  public static void setClientName(String clientName){
    if(pref != null && clientName != null) setString(SharedPrefKeys.CLIENT_NAME, clientName);
  }
  
  /**
   * Get store type string.
   *
   * @return the string
   */
  public static String getStoreType(){ return getString(SharedPrefKeys.STORE_TYPE); }
  
  /**
   * Set store type.
   *
   * @param storeType the store type
   */
  public static void setStoreType(String storeType){
    if(pref != null && storeType != null) setString(SharedPrefKeys.STORE_TYPE, storeType);
  }
  
  /**
   * Get store id string.
   *
   * @return the string
   */
  public static String getStoreID(){ return getString(SharedPrefKeys.STORE_ID); }
  
  /**
   * Set store id.
   *
   * @param storeId the store id
   */
  public static void setStoreID(String storeId){
    if(pref != null && storeId != null) setString(SharedPrefKeys.STORE_ID, storeId);
  }
  
  /**
   * Get store code string.
   *
   * @return the string
   */
  public static String getStoreCode(){ return getString(SharedPrefKeys.STORE_CODE); }
  
  /**
   * Set store code.
   *
   * @param storeCode the store code
   */
  public static void setStoreCode(String storeCode){
    if(pref != null && storeCode != null) setString(SharedPrefKeys.STORE_CODE, storeCode);
  }
  
  /**
   * Get store name string.
   *
   * @return the string
   */
  public static String getStoreName(){ return getString(SharedPrefKeys.STORE_NAME); }
  
  /**
   * Set store name.
   *
   * @param storeName the store name
   */
  public static void setStoreName(String storeName){
    if(pref != null && storeName != null) setString(SharedPrefKeys.STORE_NAME, storeName);
  }
  
  /**
   * Get user id string.
   *
   * @return the string
   */
  public static String getUserID(){ return getString(SharedPrefKeys.USER_ID); }
  
  /**
   * Set user id.
   *
   * @param userId the user id
   */
  public static void setUserID(String userId){
    if(pref != null && userId != null) setString(SharedPrefKeys.USER_ID, userId);
  }
  
  /**
   * Get hour integer.
   *
   * @return the integer
   */
  public static Integer getHour(){ return getInt(SharedPrefKeys.HOUR, -1); }
  
  /**
   * Set hour.
   *
   * @param hour the hour
   */
  public static void setHour(Integer hour){
    if(pref != null && hour != null) setInt(SharedPrefKeys.HOUR, hour);
  }
  
  /**
   * Get min integer.
   *
   * @return the integer
   */
  public static Integer getMin(){ return getInt(SharedPrefKeys.MIN, -1); }
  
  /**
   * Set min.
   *
   * @param min the min
   */
  public static void setMin(Integer min){
    if(pref != null && min != null) setInt(SharedPrefKeys.MIN, min);
  }
  
  /**
   * Get time in milies long.
   *
   * @return the long
   */
  public static Long getTimeInMillis(){ return getLong(SharedPrefKeys.TIME, -1l); }
  
  /**
   * Set time in millis.
   *
   * @param timeInMillis the time in millis
   */
  public static void setTimeInMillis(Long timeInMillis){
    if(pref != null && timeInMillis != null) setLong(SharedPrefKeys.TIME, timeInMillis);
  }
  
  /**
   * Get user name string.
   *
   * @return the string
   */
  public static String getUserName(){ return getString(SharedPrefKeys.USER_NAME); }
  
  /**
   * Set user name.
   *
   * @param userName the user name
   */
  public static void setUserName(String userName){
    if(pref != null && userName != null) setString(SharedPrefKeys.USER_NAME, userName);
  }
  
  /**
   * Get password string.
   *
   * @return the string
   */
  public static String getPassword(){ return getString(SharedPrefKeys.PASSWORD); }
  
  /**
   * Set password.
   *
   * @param password the password
   */
  public static void setPassword(String password){
    if(pref != null && password != null) setString(SharedPrefKeys.PASSWORD, password);
  }
  
  /**
   * Get user profile url string.
   *
   * @return the string
   */
  public static String getUserProfileUrl(){ return getString(SharedPrefKeys.USER_PROFILE_URL); }
  
  /**
   * Set user profile url.
   *
   * @param userProfileUrl the user profile url
   */
  public static void setUserProfileUrl(String userProfileUrl){
    if(pref != null && userProfileUrl != null)
      setString(SharedPrefKeys.USER_PROFILE_URL, userProfileUrl);
  }
  
  /**
   * Get dashboard url string.
   *
   * @return the string
   */
  public static String getDashboardUrl(){ return getString(SharedPrefKeys.DASHBOARD_URL); }
  
  /**
   * Set dashboard url.
   *
   * @param dashboardUrl the user profile url
   */
  public static void setDashboardUrl(String dashboardUrl){
    if(pref != null && dashboardUrl != null) setString(SharedPrefKeys.DASHBOARD_URL, dashboardUrl);
  }
  
  /**
   * Get trip no string.
   *
   * @return the string
   */
  public static String getTripNo(){ return getString(SharedPrefKeys.TripNo); }
  
  /**
   * Set trip no.
   *
   * @param tripNo the trip no
   */
  public static void setTripNo(String tripNo){
    if(pref != null && tripNo != null) setString(SharedPrefKeys.TripNo, tripNo);
  }
  
  /**
   * Get delivery no string.
   *
   * @return the string
   */
  public static String getDeliveryNo(){ return getString(SharedPrefKeys.DeliveryNo); }
  
  /**
   * Set delivery no.
   *
   * @param deliveryNo the delivery no
   */
  public static void setDeliveryNo(String deliveryNo){
    if(pref != null && deliveryNo != null) setString(SharedPrefKeys.DeliveryNo, deliveryNo);
  }
  
  /**
   * Get hu no string.
   *
   * @return the string
   */
  public static String getHuNo(){ return getString(SharedPrefKeys.HuNo); }
  
  /**
   * Set hu no.
   *
   * @param huNo the hu no
   */
  public static void setHuNo(String huNo){
    if(pref != null && huNo != null) setString(SharedPrefKeys.HuNo, huNo);
  }
  
  /**
   * Get decode alert msg string.
   *
   * @return the string
   */
  public static String getDecodeAlertMsg(){ return getString(SharedPrefKeys.DECODE_ALERT_MSG); }
  
  /**
   * Set hu no.
   *
   * @param decodeAlertMsg the decode alert msg
   */
  public static void setDecodeAlertMsg(String decodeAlertMsg){
    if(pref != null && decodeAlertMsg != null)
      setString(SharedPrefKeys.DECODE_ALERT_MSG, decodeAlertMsg);
  }
  
  /**
   * Get is show crash log boolean.
   *
   * @return the boolean
   */
  public static Boolean getIsShowCrashLog(){ return getBoolean(SharedPrefKeys.IS_SHOW_CRASH_LOG); }
  
  /**
   * Set is show crash log.
   *
   * @param isShowCrashLog the is show crash log
   */
  public static void setIsShowCrashLog(Boolean isShowCrashLog){
    if(pref != null && isShowCrashLog != null)
      setBoolean(SharedPrefKeys.IS_SHOW_CRASH_LOG, isShowCrashLog);
  }
  
  /**
   * Get is allow file logs.
   *
   * @return the boolean
   */
  public static Boolean getIsAllowFileLogs(){ return getBoolean(ParamConstants.SHOW_FILE_LOGS); }
  
  /**
   * Set is allow file logs.
   *
   * @param isAllowFileLogs the is show crash log
   */
  public static void setIsAllowFileLogs(Boolean isAllowFileLogs){
    if(pref != null && isAllowFileLogs != null)
      setBoolean(ParamConstants.SHOW_FILE_LOGS, isAllowFileLogs);
  }
  
  /**
   * Get is allow file logs.
   *
   * @return the boolean
   */
  public static Integer getMaxLogFiles(){ return getInt(ParamConstants.MAX_LOG_FILES, 5); }
  
  /**
   * Set is max log files.
   */
  public static void setMaxLogFiles(Integer maxLogFiles){
    if(pref != null && maxLogFiles != null) setInt(ParamConstants.MAX_LOG_FILES, maxLogFiles);
  }
  
  /**
   * Get is allow file logs.
   *
   * @return the boolean
   */
  public static Integer getLogFileMaxSize(){ return getInt(ParamConstants.MAX_LOG_FILE_SIZE, 30); }
  
  /**
   * Set is max log files.
   */
  public static void setLogFileMaxSize(Integer maxLogFiles){
    if(pref != null && maxLogFiles != null) setInt(ParamConstants.MAX_LOG_FILE_SIZE, maxLogFiles);
  }
  
  /**
   * Get is allow db backup
   *
   * @return the boolean
   */
  public static Boolean getIsAllowDBBackup(){ return getBoolean(ParamConstants.ALLOW_DB_BACKUP); }
  
  /**
   * Set is allow db backup.
   *
   * @param isAllowDBBackup the is show crash log
   */
  public static void setIsAllowDBBackup(Boolean isAllowDBBackup){
    if(pref != null && isAllowDBBackup != null)
      setBoolean(ParamConstants.ALLOW_DB_BACKUP, isAllowDBBackup);
  }
  
  /**
   * Get is sensor available boolean.
   *
   * @return the boolean
   */
  public static Boolean getIsSensorAvailable(){ return getBoolean(SharedPrefKeys.IS_SENSOR_AVAILABLE); }
  
  /**
   * Set is sensor available.
   *
   * @param isSensorAvailable the is show crash log
   */
  public static void setIsSensorAvailable(Boolean isSensorAvailable){
    if(pref != null && isSensorAvailable != null)
      setBoolean(SharedPrefKeys.IS_SENSOR_AVAILABLE, isSensorAvailable);
  }
  
  /**
   * Get saved fav menus array list.
   *
   * @return the array list
   */
  public static ArrayList<MenuModel> getSavedFavMenus(){ return (ArrayList<MenuModel>) getArrayList(SharedPrefKeys.SELECTED_FAV_MENUS); }
  
  /**
   * Set saved fav menus.
   *
   * @param listFavMenus the list fav menus
   */
  public static void setSavedFavMenus(ArrayList<MenuModel> listFavMenus){ setArrayList(SharedPrefKeys.SELECTED_FAV_MENUS, listFavMenus); }
  
  //Generic Methods (Type Based)
  
  /**
   * Get string string.
   *
   * @param key the key
   * @return the string
   */
  public static String getString(SharedPrefKeys key){ return getString(key, ""); }
  
  /**
   * Get string string.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the string
   */
  public static String getString(SharedPrefKeys key, String defValue){ return key != null ? getString(key.toString(), defValue) : defValue; }
  
  /**
   * Get string string.
   *
   * @param key the key
   * @return the string
   */
  public static String getString(String key){ return getString(key, ""); }
  
  /**
   * Get string string.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the string
   */
  public static String getString(String key, String defValue){ return pref != null && isNonEmpty(key) ? pref.getString(key, defValue) : defValue; }
  
  /**
   * Set string.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setString(SharedPrefKeys key, String value){
    if(key != null) setString(key.toString(), value);
  }
  
  /**
   * Set string.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setString(String key, String value){
    if(pref != null && isNonEmpty(key) && value != null) pref.edit().putString(key, value).commit();
  }
  
  /**
   * Get string set set.
   *
   * @param key the key
   * @return the set
   */
  public static Set<String> getStringSet(SharedPrefKeys key){ return getStringSet(key, null); }
  
  /**
   * Get string set set.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the set
   */
  public static Set<String> getStringSet(SharedPrefKeys key, Set<String> defValue){ return getStringSet(key.toString(), defValue); }
  
  /**
   * Get string set set.
   *
   * @param key the key
   * @return the set
   */
  public static Set<String> getStringSet(String key){ return getStringSet(key, null); }
  
  /**
   * Get string set set.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the set
   */
  public static Set<String> getStringSet(String key, Set<String> defValue){ return pref != null ? pref.getStringSet(key, defValue) : defValue; }
  
  /**
   * Set string set.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setStringSet(SharedPrefKeys key, Set<String> value){ setStringSet(key.toString(), value); }
  
  /**
   * Set string set.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setStringSet(String key, Set<String> value){
    if(pref != null) pref.edit().putStringSet(key, value).commit();
  }
  
  /**
   * Get boolean boolean.
   *
   * @param key the key
   * @return the boolean
   */
  public static Boolean getBoolean(SharedPrefKeys key){ return getBoolean(key, false); }
  
  /**
   * Get boolean boolean.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the boolean
   */
  public static Boolean getBoolean(SharedPrefKeys key, Boolean defValue){ return getBoolean(key.toString(), defValue); }
  
  /**
   * Get boolean boolean.
   *
   * @param key the key
   * @return the boolean
   */
  public static Boolean getBoolean(String key){ return getBoolean(key, false); }
  
  /**
   * Get boolean boolean.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the boolean
   */
  public static Boolean getBoolean(String key, Boolean defValue){ return pref != null ? pref.getBoolean(key, defValue) : defValue; }
  
  /**
   * Set boolean.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setBoolean(SharedPrefKeys key, Boolean value){ setBoolean(key.toString(), value); }
  
  /**
   * Set boolean.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setBoolean(String key, Boolean value){
    if(pref != null && value != null) pref.edit().putBoolean(key, value).commit();
  }
  
  /**
   * Get int integer.
   *
   * @param key the key
   * @return the integer
   */
  public static Integer getInt(SharedPrefKeys key){ return getInt(key, 0); }
  
  /**
   * Get int integer.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the integer
   */
  public static Integer getInt(SharedPrefKeys key, Integer defValue){ return getInt(key.toString(), defValue); }
  
  /**
   * Get int integer.
   *
   * @param key the key
   * @return the integer
   */
  public static Integer getInt(String key){ return getInt(key, 0); }
  
  /**
   * Get int integer.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the integer
   */
  public static Integer getInt(String key, Integer defValue){ return pref != null ? pref.getInt(key, defValue) : defValue; }
  
  /**
   * Set int.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setInt(SharedPrefKeys key, Integer value){ setInt(key.toString(), value); }
  
  /**
   * Set int.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setInt(String key, Integer value){
    if(pref != null && value != null) pref.edit().putInt(key, value).commit();
  }
  
  /**
   * Get long long.
   *
   * @param key the key
   * @return the long
   */
  public static Long getLong(SharedPrefKeys key){ return getLong(key, 0l); }
  
  /**
   * Get long long.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the long
   */
  public static Long getLong(SharedPrefKeys key, Long defValue){ return getLong(key.toString(), defValue); }
  
  /**
   * Get long long.
   *
   * @param key the key
   * @return the long
   */
  public static Long getLong(String key){ return getLong(key, 0l); }
  
  /**
   * Get long long.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the long
   */
  public static Long getLong(String key, Long defValue){ return pref != null ? pref.getLong(key, defValue) : defValue; }
  
  /**
   * Set long.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setLong(SharedPrefKeys key, Long value){ setLong(key.toString(), value); }
  
  /**
   * Set long.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setLong(String key, Long value){
    if(pref != null && value != null) pref.edit().putLong(key, value).commit();
  }
  
  /**
   * Get double double.
   *
   * @param key the key
   * @return the double
   */
  public static Double getDouble(SharedPrefKeys key){ return getDouble(key, 0.00); }
  
  /**
   * Get double double.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the double
   */
  public static Double getDouble(SharedPrefKeys key, Double defValue){ return getDouble(key.toString(), defValue); }
  
  /**
   * Get double double.
   *
   * @param key the key
   * @return the double
   */
  public static Double getDouble(String key){ return getDouble(key, 0.00); }
  
  /**
   * Get double double.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the double
   */
  public static Double getDouble(String key, Double defValue){ return pref != null ? pref.getFloat(key, defValue.floatValue()) : defValue; }
  
  /**
   * Set double.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setDouble(SharedPrefKeys key, Double value){ setDouble(key.toString(), value); }
  
  /**
   * Set double.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setDouble(String key, Double value){
    if(pref != null && value != null) pref.edit().putFloat(key, value.floatValue()).commit();
  }
  
  /**
   * Get array list array list.
   *
   * @param key the key
   * @return the array list
   */
  public static ArrayList<?> getArrayList(SharedPrefKeys key){ return getArrayList(key, null); }
  
  /**
   * Get array list array list.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the array list
   */
  public static ArrayList<?> getArrayList(SharedPrefKeys key, ArrayList<?> defValue){ return getArrayList(key.toString(), defValue); }
  
  /**
   * Get array list array list.
   *
   * @param key the key
   * @return the array list
   */
  public static ArrayList<?> getArrayList(String key){ return getArrayList(key, null); }
  
  /**
   * Get array list array list.
   *
   * @param key      the key
   * @param defValue the def value
   * @return the array list
   */
  public static ArrayList<?> getArrayList(String key, ArrayList<?> defValue){ return pref != null ? strToList(pref.getString(key, "")) : defValue; }
  
  /**
   * Set array list.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setArrayList(SharedPrefKeys key, ArrayList<?> value){
    setArrayList(key.toString(), value);
  }
  
  /**
   * Set array list.
   *
   * @param key   the key
   * @param value the value
   */
  public static void setArrayList(String key, ArrayList<?> value){
    if(pref != null && isNonEmpty(value)) pref.edit().putString(key, listToStr(value)).commit();
  }
  
  /**
   * Clear array list.
   *
   * @param key   the key
   */
  public static void clearArrayList(SharedPrefKeys key){
    clearArrayList(key.toString());
  }
  
  /**
   * Clear array list.
   *
   * @param key   the key
   */
  public static void clearArrayList(String key){
    if(pref != null) pref.edit().putString(key, null).commit();
  }
  
  /**
   * Str to list array list.
   *
   * @param <T> the type parameter
   * @param str the str
   * @return the array list
   */
  public static <T extends Object> ArrayList<T> strToList(String str){
    ArrayList<T> listResults = new ArrayList<T>(0);
    if(isNonEmpty(str) && str.contains(LIST_CLASS_SEPARATOR)){
      Class<T> itemClass = null;
      try{
        itemClass = (Class<T>) Class.forName(str.split(LIST_CLASS_SEPARATOR)[0]);
      }
      catch(ClassNotFoundException e){ e.printStackTrace(); }
      str = str.split(LIST_CLASS_SEPARATOR)[1];
      if(itemClass != null){
        for(String objStr : str.split(LIST_OBJ_SEPARATOR)){
          listResults.add((T) getGSON().fromJson(objStr, itemClass));
        }
      }
    }
    return listResults;
  }
  
  /**
   * List to str string.
   *
   * @param list the list
   * @return the string
   */
  public static String listToStr(ArrayList<?> list){
    String str = "";
    if(isNonEmpty(list)){
      int i = 0, last = list.size() - 1;
      str = list.get(0).getClass().getName().toString() + LIST_CLASS_SEPARATOR;
      for(Object obj : list)
        str += getGSON().toJson(obj) + (i++ < last ? LIST_OBJ_SEPARATOR : "");
    }
    return str.endsWith(LIST_OBJ_SEPARATOR) ? str.substring(0, str.lastIndexOf(LIST_OBJ_SEPARATOR)) : str;
  }
  
  /**
   * List to str string.
   *
   * @param list the list
   * @return the string
   */
  public static String listToStr(List<?> list){
    String str = "";
    if(isNonEmpty(list)){
      int i = 0, last = list.size() - 1;
      str = list.get(0).getClass().getName().toString() + LIST_CLASS_SEPARATOR;
      for(Object obj : list)
        str += getGSON().toJson(obj) + (i++ < last ? LIST_OBJ_SEPARATOR : "");
    }
    return str.endsWith(LIST_OBJ_SEPARATOR) ? str.substring(0, str.lastIndexOf(LIST_OBJ_SEPARATOR)) : str;
  }
  
  /**
   * The enum Shared pref keys.
   */
  public enum SharedPrefKeys{
    //App Info
    IMEI, FIREBASE_TOKEN, MAC_ADDRESS, DEVICE_TYPE, SENSOR_TYPE, IS_SENSOR_AVAILABLE, DEVICE_MODEL, DEVICE_TRIGGER_KEY_CODES, DEVICE_BLUETOOTH_DEPENDENT, INSTALL_DATE, APP_VERSION, READER_VERSION, //App Specifications & Reader details
    LAT_LNG, IS_SHOW_CRASH_LOG, READER_POWER, CURRENT_ACCESS_PASSWORD, OLD_ACCESS_PASSWORDS, IS_LOGGED_IN, CLIENT_ID, CLIENT_NAME, USER_ID, PASSWORD, USER_NAME, DASHBOARD_URL, USER_PROFILE_URL, STORE_TYPE, STORE_ID, STORE_CODE, STORE_NAME, //Store/User details
    SERVER_URL, IS_SERVER_URL_CONFIGURED, ACCESS_TOKEN, TOKEN_TIME, //URL/Web-Service Related
    ENCODING_PURPOSE, EAN_TYPE, ENCODING_TYPE, ENCODING_RELATION_TYPE, ENCODING_BARCODE_RFID_TYPE, OMNICHANNEL_TYPE, UNENCODED_SEARCH_TYPE, ALIEN_SEARCH_TYPE, REPLENISHMENT_TYPE, //Types & Encoding Filters
    SELECTED_FAV_MENUS, SAVED_FAV_MENU_CODES, //Dashboard Favourites menu
    CURRENT_INVENTORY_SESSION_ID, INVENTORY_TAKEN_DATE_TIME, //last inventory taken DateTime
    NON_PASSWORD_TIDS, NON_128_BIT_TIDS, //Tags
    TripNo, DeliveryNo, HuNo, //Inward
    BEEPER_VOLUME, //beeper volume for SDKs
    HOUR, MIN, TIME,//time tick event
    IS_CHK_RATIONALE, //checking Permission Rationale
    IS_PROFILE_SET, //checking if profile is set for OPTIMIZED ZEBRA BARCODE HANDLER (SDK_INT>=30)
    UPDATE_APK_PATH, //setting if new apk version is available
    DECODE_ALERT_MSG, BATTERY_CHARGED_PERCENT, BATTERY_READER_PERCENT,
    PRINTER_ADDRESS
  }
}
