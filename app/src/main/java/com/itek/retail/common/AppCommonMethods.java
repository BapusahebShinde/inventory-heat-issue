package com.itek.retail.common;

import static android.content.Context.LOCATION_SERVICE;
import static com.itek.retail.apis.ParamConstants.DEVICE_TYPE_VAL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.itek.retail.BuildConfig;
import com.itek.retail.R;
import com.itek.retail.apis.APIsInterface;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.receiver.AppBroadcastReceiver;
import com.itek.retail.sgtin.EPCEncoderDecoder;
import com.itek.retail.ui.decoding.DecodingStartFragment;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.outward.tote.OutwardToteDCFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * The App common methods.
 * contains constant values/flags and static methods
 * which are used throughout the app
 */
public class AppCommonMethods{

  public static int fileSize = 30;
  public static Long maxFileSize = fileSize * 1000 * 1000l;
  public static int fileCount = 5;
  
  //constant values/flags used throughout the app
  public static final boolean isDemoApp = false;
  public static final boolean isDebugApp = BuildConfig.DEBUG && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug");
  public static final boolean isStaticDebugApp = isDebugApp && false;
  public static final boolean isUseInAppStorage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
  public static final boolean isUseDeviceIDForIMEI = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
  public static final boolean isUseBluetoothScanConnect = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
  public static final boolean isUseNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
  public static final int REQUEST_PERMISSION_LOCATION = 1;
  public static final int REQUEST_PERMISSION_CAMERA = 2;
  public static final int REQUEST_PERMISSION_STORAGE = 4;
  public static final int REQUEST_PERMISSION_IMEI = 8;
  public static final int REQUEST_PERMISSION_BLUETOOTH = 16;
  public static final int REQUEST_PERMISSION_NOTIFICATION = 32;
  public static final String chainwayTriggerKeys = "66, 139, 280, 293";
  public static final List<Integer> chainwayKeyCodes = Arrays.asList(new Integer[]{139, 280, 293});
  public static final List<Integer> seuicKeyCodes = Arrays.asList(new Integer[]{});
  public static final String LOG_ID_DATE_TIME_FORMAT = "ddMMyyyyHHmmss";
  public static final String SERVER_DATE_FORMAT = "yyyy-MM-dd";
  public static final String SERVER_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String SERVER_DATE_TIME_FORMAT_MILI = "yyyy-MM-dd HH:mm:ss.SSS";
  public static final String SERVER_DATE_TIME_FORMAT_PATTERN = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}";
  public static final String DATE_FORMAT = "dd-MMM-yyyy";
  public static final String DATE_FORMAT_YY = "yyyy-MM-dd";
  public static final String DATE_TIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";
  public static final String DATE_TIME_FORMAT_MILI = "dd-MMM-yyyy HH:mm:ss.SSS";
  public static final String DATE_TIME_FORMAT_PATTERN = "[0-9]{2}-[A-Za-z]{3}-[0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}";
  public static final String SERVER_URL_APPEND_API = "/api/";
  public static final String SERVER_URL_APPEND_IMG = "/productimage/";
  public static final String SERVER_URL_APPEND_DASHBOARD = "/dashBoard/index/";
  
  //flags for DC app configurations
  public static final boolean isSafariApp = false;
  public static final boolean isDCApp = true;
  public static final boolean isDCAppTataBuild = isDCApp && true;
  
  //static internal flags (default values)
  public static final boolean isSetUserMgmt = !isDCApp && !isSafariApp;
  public static final boolean isAllowShareLog = isDebugApp;
  //login
  //if true, shows profile alert after successful login (with proceed button)
  public static final boolean isShowUserProfileAlertAfterLogin = isSetUserMgmt;
  //if true, login api (i.e. getStoreDetails) is called even for pin based login
  public static final boolean isCallLoginAPIForPinBasedLogin = !isSetUserMgmt && !isDCApp && !isSafariApp;
  //if true, login api (i.e. getStoreDetails) will be called on the arrow button
  public static final boolean isCallLoginAPI = isSetUserMgmt || isCallLoginAPIForPinBasedLogin;
  //if true, logout api will be called for pin based login (otherwise direct logout without api)
  public static final boolean isCallLogoutAPIForPinBasedLogin = !isSetUserMgmt && !isDCApp && !isSafariApp;
  //if true, logout api will be called (otherwise direct logout without api)
  public static final boolean isCallLogoutAPI = isSetUserMgmt || isCallLogoutAPIForPinBasedLogin;
  //if true, app will check for next play store update
  public static final boolean isCheckPlayStoreUpdates = false;
  //if true, app will auto-check for version update
  public static final boolean isCheckVersionUpdates = false;
  //if true, favourites menu screen/tab will be displayed (if the menu list have applicable menus)
  public static final boolean isShowFavouriteMenuScreen = !isDCApp && !isSafariApp;
  
  public static final boolean isAllowNonStdEan = true;
  public static final boolean isSetInwOnline = false;
  public static final boolean isOnlyAllowHappyFlowOutward = true;
  public static final boolean isSinglePick = true;
  public static final boolean isUploadSlider = true;
  public static final boolean isShowErrorForOfflineProcess = false;
  public static final boolean isUpdateUploadStatusBasedOnTID = true;
  public static final boolean isUse24LengthTIDForUpload = true;
  public static final boolean isAllowDecodeOnPick = false;
  public static final boolean isAutoBackOnEanZoneDecoded = true;
  public static final boolean isUseNewUIForLBS = true;
  public static final boolean isAllowAdvanceFilterForBrand = true;
  public static final boolean isAllowAdvanceFilterForMultiBrands = false;
  public static final boolean isCheckEncPasswordFirst = true;
  public static final boolean isCheckEncCurrentPasswordFirst = false;
  public static final boolean isCheckEncPasswordBeforeAPI = false;
  public static final boolean isCheckEncPasswordBasedOnEPC = false;
  public static final boolean isUseDirectionalSearch = true;
  public static final boolean isShowReaderCommandFailToast = true;
  public static final boolean isShowEPCSearchInStockCorrection = true;
  public static final boolean isAutoRefreshDashboards = false;
  
  //session
  public static final boolean isCheckActiveUsersBeforeLoadingSession = !isDCApp && !isSafariApp;
  public static final boolean isUseAPICallForSessionInventory = !isDCApp && !isSafariApp;
  public static final boolean isAllowInventoryUpload = !isDCApp;
  public static final boolean isAllowSessionSaveForInventory = !isDCApp && !isSafariApp;
  public static final boolean isUseAPICallForSessionEncode = !isDCApp;
  public static final boolean isAllowSessionSaveForEncode = true;
  public static final boolean isAllowSessionSaveForThanEncode = false;
  public static final boolean isUseAPICallForSessionDecode = !isDCApp;
  public static final boolean isAllowSessionSaveForDecode = true;
  
  //static internal flags/values (Zone)
  //if true, GET_ZONES API will be called in case of No Zones Data Available In DB
  public static final boolean isCheckExistingZoneDataForInv = true;
  //if true, custom error will be shown for GET_ZONES API in case of No Zones Data Available In DB + Empty Response
  public static final boolean isShowErrorForNoZonesInAPIResponse = true;
  
  //static internal flags/values (Inventory)
  //if true, only epcs will be sent as tag data (i.e. No ean,No rssi, etc..) in the request while uploading inventory
  public static final boolean isAllowShortJsonRequestForInventoryUpload = true;
  //if true, inventory will be continuously auto-uploaded from background for defined time duration
  public static final boolean isOptimizedInventory = false;
  //if true, Inventory of all zones can be taken for take stock (i.e. Inventory Start Menu)
  public static final boolean isAllowAllZoneInventoryForTakeStock = false;
  
  
  //static internal flags (default values) (Unencoded/Alien Search)
  public static final boolean isDismissSearchDialogWhenMarkedFound = false;
  public static final boolean isUseShortRangeSearchForAll = true;
  public static final boolean isUseShortRangeSearchForOnlyGID = !isUseShortRangeSearchForAll && true;
  
  //static internal flags (default values) (product search)
  public static final boolean isShowOnlyErrorForProdSearchAPIError = false;
  public static final boolean isShowOnlyProdDetailsForProdSearchAPIError = isDCApp;
  public static final boolean isShowCheckAvailabilityBtnForProductDetails = !isDCApp;
  
  //static internal flags (default values) (encode)
  public static final boolean isOfflineEncode = false;
  public static final boolean isOfflineReEncode = false;
  public static final boolean isCheckDefaultPasswordFirst = true;
  
  //flags for Verify Encode feature
  public static final boolean isVerifyEncode = true;
  public static final boolean isVerifyEncodeSearch = false;
  public static final String sessionForceEndPass = isDebugApp || true ? "44dl9n" : "";
  public static final boolean isVerifyEncodeRedirection = true;
  public static final boolean isUploadEncodeAfterVerify = false;//isVerifyEncode;
  public static final boolean isUseSchedulerForWritenTagUpload = true;
  public static final boolean isAllowBackgroundWritenTagUploadWhileProcessing = false;
  public static final boolean isAllowBothImmediateUploadAndUploadSchedulerForWrittenTags = true;
  
  //flags for Off range feature
  public static final boolean isAllowDiscardOperationForPickedData = false;
  public static final boolean isSavePickedDataForNonCompletedCarton = false;
  public static final boolean isSaveCompletedCartonAfterUpload = true;
  public static final int saveLimitForCompletedCartons = isDebugApp ? 3 : 50;
  
  //flags for Decode feature
  public static final boolean isShowDecodeTypeSelectionDialog = true;
  public static final boolean isShowStaticDecodeTypesForSelectionIfApiFails = true;
  public static final boolean isAllowNonEncodedTagPickForDecode = false;
  public static final boolean isCheckProductDetailsBeforeDecoding = true;
  
  //flags for barcode with leading zero
  public static final boolean isIsAllowLeadingZeroForNonStdBarcode = true;
  public static final boolean isIsAllowLeadingZeroForStdBarcode = false;
  
  //flags for than
  public static final boolean isSetExtraPickTimeForThanEncoding = true;
  public static final boolean isGetOriginalLengthInEncFromField = true;
  public static final boolean isClearOriginalLengthInEncForEachTag = true;
  public static final boolean isGetOriginalLengthInEncBeforeAPI = true;
  public static final boolean isAllowZeroLengthForThanClosure = false;
  public static final String defLengthUnitThan = "cm";
  
  //flags for Scan Count
  //if true, Show Product Details instead of Ean wise qty
  public static final boolean isShowProductDetailsForScanCount = false;
  //if true, Show Ean wise Qty List
  public static final boolean isShowEanQtyListWithHeaderForScanCount = true;
  //if true, Don't save Unencoded Tags while scanning (i.e. inventory operation)
  public static final boolean isHideUnencodedTags = true;
  //if true, Don't save tags from given epc list while scanning (i.e. inventory operation)
  public static final boolean isAllowEpcSkipFromGivenList = true;
  //flags for Empty Tote Inward/Outward
  public static final boolean isShowErrorForSameToteTag = false;
  public static final boolean isShowErrorForOtherThanToteEanTag = !isDCApp;
  public static final boolean isPlayBeepForSameToteTagIfNotLastInserted = isDCApp;
  public static final boolean isPlayErrorBeepForOtherThanToteEanTag = isDCApp;
  
  //flags for New Inward/Outward (Phase1)
  public static final boolean isShowDashboard = false;
  //Use/Not Use Trip As HU (excluding extra screen & api call)
  public static final boolean isUseTripAsHU = false;
  //Allow/Not Allow Manual Trip Creation
  public static final boolean isAllowManualTripCreation = true;
  //Show/Hide Location & Type for the Trip
  public static final boolean isShowTripLocAndType = true;
  //Show Both Total & Completed Count Or Just Show Completed Count as 'completed/total' for the Trip
  public static final boolean isShowTotalAndCompletedCount = false;
  //Allow/Don't Allow to Scan Trip Number
  public static final boolean isScanTripNumber = true;
  //Verify/Don't Verify Scanned/Entered Non-Existing Trip No before Redirecting to Manual Trip Creation Page
  public static final boolean isVerifyManualTripNoBeforeCreating = true;
  //Allow/Don't Allow to Trip Number as lower case (i.e. Change to Uppercase on button click)
  public static final boolean isAllowLowerCaseTripNumber = false;
  //Allow/Don't Allow Manual HU Creation (for Non-Manual Trip)
  public static final boolean isAllowManualHUCreation = false;
  //Auto-Redirect to HU Entry Page for Previously Processing Trip
  public static final boolean isLockAndRedirectToProcessingTrip = false;
  //Auto-Redirect to HU Entry Page for Previously Processing Manual Trip
  public static final boolean isLockAndRedirectToProcessingManualTrip = true;
  //Auto-Redirect to HU Process Page for Previously Processing HU
  public static final boolean isLockAndRedirectToInProcessHU = false;
  //Auto-Call to 'acceptHU' API if Happy Flow.
  public static final boolean isAutoAcceptHUForHappyFlow = false;
  //Auto-Call to 'acceptHU' API for Manual HU for Qty Matched
  public static final boolean isAutoAcceptManualHUForHappyFlow = false;
  //Auto-Call to 'completeTripStatus' API If all HUs are completed
  public static final boolean isAutoCompleteTripIfAllHUAccepted = false;
  //Auto-Call to 'accept/reject' API for proceed HU based on success/error conditions
  public static final boolean isAutoProcessHU = false;
  //Auto-Call to 'accept/reject' API for proceed HU based on success/error conditions
  public static final boolean isAutoProcessManualHU = false;
  //'rejectHU' API should be called or not (i.e. api based reject OR manual device level reject)
  public static final boolean isAPIBasedRejectHU = false;
  //if true, show the 'rejected successfully' dialog for Non-API Based Reject HU
  public static final boolean isShowErrorDialogForNonAPIBasedRejectHU = !isAPIBasedRejectHU && true;
  //Allow/Don't Allow Mix Tag Type (i.e. Both Hard/Soft Tags) -- Old flag
  public static final boolean isAllowMixTagType = true;
  //Allow/Don't Allow Barcode Scanning  -- Old flag
  public static final boolean isAllowBarcodeScanning = false;
  //Lock/Don't Lock trip to Device (API Based Trip Lock -> i.e. Other Devices can't see/process same Trip when locked)
  public static final boolean isTripDeviceLock = false;
  //Lock/Don't Lock HU to Device (API Based HU Lock -> i.e. Other Devices can't see/process same HU when locked)
  public static final boolean isHUDeviceLock = false;
  //Save Offline Data for Completed Trips (unused)
  public static final boolean isSaveCompletedTripDataOffline = false;
  //If true, Don't Show HU Details for Trip Directly as List, Show them as Dialog on Demand.
  public static final boolean isOnDemandTripHuList = true;
  //If true, Show HU Details on Dialog on 'info' icon click
  public static final boolean isShowOnDemandTripHuDetails = true;
  //If true, Don't Show Exp Qty Column in Scan Summary For Manual HU
  public static final boolean isHideEanExpQtyColumnForManualHU = true;
  //If true, Show Red Background on Error
  public static final boolean isShowRedBgWhileScanningHU = true;
  //Allow/Don't Allow to Scan Serial Number
  public static final boolean isInwardWithSerialNumber = true;
  public static final boolean isSerialNumberMandatory = true;


  //flags for On Demand Replenishment
  //Show Both Total & Picked Qty Or Just Show Picked Qty as 'picked/total' for the Article
  public static final boolean isShowTotalAndPickedQtyForArticle = true;
  //Handle Notification Data via Intent (i.e. upon Clicking Notification & opening activity)
  public static final boolean isHandleNotificationDataFromIntent = false;
  
  //flags for Excel Based List Search
  //Call/Don't call API to update an Ean/Product as found.
  public static final boolean isCallAPIBasedMarkFound = false;
  //Confirm/Don't confirm with User for New Available list for Processing.
  public static final boolean isConfirmUserActionIfNewListAvailable = false;
  //Notify/Don't notify User for New Available list for Processing.
  public static final boolean isNotifyUserActionIfNewListAvailable = false;
  //Restrict/Don't Restrict User for Searching is All Qty Found for the ean.
  public static final boolean isRestrictUserForAllFound = true;
  
  //flags for File Based Search
  //Allow/Don't Allow Inventory Operation on the List Screen
  public static final boolean isListInventoryAllowed = true;
  
  //fixed static values
  public static final int markFoundPercentLBS = 50;
  public static final int markFoundPercentNewLBS = 0;
  public static final int markFoundPercentUnencodedSearch = 80;
  public static final int markFoundPercentAlienSearch = 80;
  public static final int markFoundPercentAssortmentSearch = 80;
  public static final int invLiveDataLimit = isDebugApp ? 20 : 20000;
  public static final int invLimit = 75000;//500000;
  public static final int searchResetCounterOnZero = isDebugApp ? 3 : 5;
  public static boolean isUseGID = false;
  public static boolean isIdentifyAndAllowOnlyITEKTagsByEPC = false;
  public static boolean isIdentifyAndAllowOnlyITEKTagsByTID = false;
  //if true, Unencoded/Alien count separation will be shown in inventory
  public static boolean isShowUnencodedAndAlienCountsInInventory = false;
  
  //flags for Reference Barcode
  public static boolean isUseReferenceBarcode = false;
  public static final boolean isSyncProductReferences = isUseReferenceBarcode && false;
  
  //static variables/values used throughout the app
  public static boolean allowBtnClick = true;
  public static MainActivity mainActivity;
  public static ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 100);
  public static int retryCount = 0;
  public static String batteryPercent = "";
  private static AlertDialog progressDialog;
  
  public static String getScanRegex(String type){
    return type.matches("(?i)(Serial)") ? getSerialRegex() : type.matches("(?i)(TID|RFID QR)") ? getTidRegex() : type.equalsIgnoreCase("HU") ? SharedPrefManager.getBoolean(ParamConstants.IS_USE_REFERENCE_BARCODE, isUseReferenceBarcode) ? AppConstants.REGEX_ANY_BARCODE_BIG : AppConstants.REGEX_ANY_BARCODE : getEanRegex();
  }
  
  public static String getEanRegex(){
    return getEanRegex(false);
  }
  
  public static String getEanRegex(boolean isRFID){
    return getEanRegex(false, isRFID);
  }
  
  public static String getEanRegex(boolean isHUField, boolean isRFID){
    return isHUField || isAllowNonStdEan ? SharedPrefManager.getBoolean(ParamConstants.IS_USE_REFERENCE_BARCODE, isUseReferenceBarcode) ? AppConstants.REGEX_ANY_BARCODE_BIG : AppConstants.REGEX_ANY_BARCODE : isRFID || !SharedPrefManager.isStdEanType() ? AppConstants.REGEX_HEX_BARCODE : AppConstants.REGEX_NUM_BARCODE;
  }
  
  public static String getTidRegex(){
    return AppConstants.REGEX_TID;
  }
  
  public static String getSerialRegex(){
    return AppConstants.REGEX_SERIAL;
  }
  
  /**
   * Is null or empty boolean.
   *
   * @param jsonArray the json array
   * @return the boolean
   */
  public static boolean isNullOrEmpty(JSONArray jsonArray){ return !isNonEmpty(jsonArray); }
  
  /**
   * Is null or empty boolean.
   *
   * @param jsonArray the json array
   * @return the boolean
   */
  public static boolean isNullOrEmpty(JsonArray jsonArray){ return !isNonEmpty(jsonArray); }
  
  /**
   * Is null or empty boolean.
   *
   * @param jsonObj the json obj
   * @return the boolean
   */
  public static boolean isNullOrEmpty(JSONObject jsonObj){ return !isNonEmpty(jsonObj); }
  
  /**
   * Is null or empty boolean.
   *
   * @param str the str
   * @return the boolean
   */
  public static boolean isNullOrEmpty(String str){ return !isNonEmpty(str); }
  
  /**
   * Is null or empty boolean.
   *
   * @param set the set
   * @return the boolean
   */
  public static boolean isNullOrEmpty(Set<?> set){ return !isNonEmpty(set); }
  
  /**
   * Is null or empty boolean.
   *
   * @param list the list
   * @return the boolean
   */
  public static boolean isNullOrEmpty(List<?> list){ return !isNonEmpty(list); }
  
  /**
   * Is null or empty boolean.
   *
   * @param arrayList the array list
   * @return the boolean
   */
  public static boolean isNullOrEmpty(ArrayList<?> arrayList){ return !isNonEmpty(arrayList); }
  
  /**
   * Is null or empty boolean.
   *
   * @param jsonObj the json obj
   * @return the boolean
   */
  public static boolean isNullOrEmpty(JsonObject jsonObj){ return !isNonEmpty(jsonObj); }
  
  /**
   * Is non empty boolean.
   *
   * @param bundle the Bundle
   * @return the boolean
   */
  public static boolean isNonEmpty(Bundle bundle){ return bundle != null && bundle.size() > 0; }
  
  /**
   * Is non empty boolean.
   *
   * @param jsonArray the json array
   * @return the boolean
   */
  public static boolean isNonEmpty(JSONArray jsonArray){ return jsonArray != null && jsonArray.length() > 0; }
  
  /**
   * Is non empty boolean.
   *
   * @param jsonArray the json array
   * @return the boolean
   */
  public static boolean isNonEmpty(JsonArray jsonArray){ return jsonArray != null && !jsonArray.isEmpty() && jsonArray.size() > 0; }
  
  /**
   * Is non empty boolean.
   *
   * @param jsonObj the json obj
   * @return the boolean
   */
  public static boolean isNonEmpty(JSONObject jsonObj){ return jsonObj != null && jsonObj.length() > 0; }
  
  /**
   * Is non empty boolean.
   *
   * @param str the str
   * @return the boolean
   */
  public static boolean isNonEmpty(String str){ return str != null && str.trim().length() > 0 && !str.trim().equalsIgnoreCase("null"); }
  
  public static boolean isNonEmpty(byte[] str){ return str != null && str.length > 0; }
  
  public static boolean isNonEmpty(String[] strArray){ return strArray != null && strArray.length > 0; }
  
  /**
   * Is non empty boolean.
   *
   * @param set the set
   * @return the boolean
   */
  public static boolean isNonEmpty(Set<?> set){ return set != null && !set.isEmpty() && set.size() > 0; }
  
  /**
   * Is non empty boolean.
   *
   * @param set the set
   * @return the boolean
   */
  public static boolean isNonEmpty(Map<?, ?> set){ return set != null && !set.isEmpty() && set.size() > 0; }
  
  /**
   * Is non empty boolean.
   *
   * @param list the list
   * @return the boolean
   */
  public static boolean isNonEmpty(List<?> list){ return list != null && !list.isEmpty() && list.size() > 0; }
  
  /**
   * Is non empty boolean.
   *
   * @param arrayList the array list
   * @return the boolean
   */
  public static boolean isNonEmpty(ArrayList<?> arrayList){ return arrayList != null && !arrayList.isEmpty() && arrayList.size() > 0; }
  
  /**
   * Is non empty boolean.
   *
   * @param jsonObj the json obj
   * @return the boolean
   */
  public static boolean isNonEmpty(JsonObject jsonObj){ return jsonObj != null && jsonObj.size() > 0; }
  
  /**
   * Error beep.
   */
  public static void errorBeep(){
    toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK);
  }
  
  /**
   * Success beep.
   */
  public static void successBeep(){ toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK); }
  
  /**
   * Beep.
   */
  public static void beep(){ toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP); }
  
  /**
   * Beep notification.
   */
  public static void beepNotification(){ toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER); }
  
  /**
   * Stop beep.
   */
  public static void stopBeep(){ toneGenerator.stopTone(); }
  
  public static void searchBeep(final Activity commonActivity, final int searchPercentValue){
    commonActivity.runOnUiThread(new Runnable(){
      @Override
      public void run(){
        try{
          if(searchPercentValue >= AppConstants.SEARCH_PERCENT_VALUE_90)
            playSound(commonActivity, R.raw.successbeep);
          else if(searchPercentValue >= AppConstants.SEARCH_PERCENT_VALUE_66)
            playSound(commonActivity, R.raw.blep_300ms);
          else if(searchPercentValue >= AppConstants.SEARCH_PERCENT_VALUE_33)
            playSound(commonActivity, R.raw.blep_100ms);
          else if(searchPercentValue > AppConstants.SEARCH_PERCENT_VALUE_0)
            playSound(commonActivity, R.raw.mute);
        }
        catch(Exception e){
          e.printStackTrace();
        }
      }
    });
  }
  
  /**
   * Play Sound.
   */
  public static void playSound(final Context context, final int resId){
    try{
      if(resId != 0){
        final MediaPlayer sound1 = MediaPlayer.create(context, resId);
        if(sound1.isPlaying() == true){
          sound1.pause();
        }
        else{
          sound1.start();
        }
        sound1.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
          public void onCompletion(MediaPlayer mp){
            mp.reset();
          }
        });
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Is non zero id boolean.
   *
   * @param strResId the str res id
   * @return the boolean
   */
  public static boolean isNonZeroId(@StringRes int strResId){
    return Math.abs(chkNull(strResId, 0)) != 0;
  }
  
  /**
   * Chk null Bundle.
   *
   * @param bundle the Bundle
   * @param def    the Bundle
   * @return the Bundle
   */
  public static Bundle chkNull(Bundle bundle, Bundle def){ return isNonEmpty(bundle) ? bundle : def; }
  
  /**
   * Chk null integer.
   *
   * @param i   the
   * @param def the def
   * @return the integer
   */
  public static Integer chkNull(Integer i, Integer def){ return i != null ? i : def; }
  
  /**
   * Chk null long.
   *
   * @param l   the l
   * @param def the def
   * @return the long
   */
  public static Long chkNull(Long l, Long def){ return l != null ? l : def; }
  
  /**
   * Chk null float.
   *
   * @param f   the f
   * @param def the def
   * @return the float
   */
  public static Float chkNull(Float f, Float def){ return f != null ? f : def; }
  
  /**
   * Hide keyboard.
   *
   * @param activity the activity
   */
  public static void hideKeyboard(final Activity activity){
    if(activity != null){
      InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
      // check if no view has focus:
      View currentFocusedView = activity.getCurrentFocus();
      if(currentFocusedView != null)
        inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
  }
  
  /**
   * Chk not null true boolean.
   *
   * @param bool the bool
   * @return the boolean
   */
  public static boolean chkNotNullTrue(Boolean bool){ return bool != null && bool; }
  
  /**
   * Chk not null false boolean.
   *
   * @param bool the bool
   * @return the boolean
   */
  public static boolean chkNotNullFalse(Boolean bool){ return bool != null && !bool; }
  
  /**
   * Chk null boolean.
   *
   * @param bool the bool
   * @return the boolean
   */
  public static Boolean chkNull(Boolean bool){ return chkNull(bool, false); }
  
  /**
   * Chk null boolean.
   *
   * @param bool the bool
   * @param def  the def
   * @return the boolean
   */
  public static Boolean chkNull(Boolean bool, Boolean def){ return bool != null ? bool : def; }
  
  /**
   * Chk val string.
   *
   * @param obj the obj
   * @return the string
   */
  public static String chkVal(Object obj){ return obj != null ? obj.toString() : "null"; }
  
  /**
   * Chk null double.
   *
   * @param d   the d
   * @param def the def
   * @return the double
   */
  public static Double chkNull(Double d, Double def){ return d != null ? d : def; }
  
  /**
   * Chk null json array.
   *
   * @param jArray the j array
   * @param def    the def
   * @return the json array
   */
  public static JsonArray chkNull(JsonArray jArray, JsonArray def){ return jArray != null ? jArray : def; }
  
  /**
   * Chk null json object.
   *
   * @param jObj the j obj
   * @param def  the def
   * @return the json object
   */
  public static JsonObject chkNull(JsonObject jObj, JsonObject def){ return jObj != null ? jObj : def; }
  
  /**
   * Chk null json array.
   *
   * @param jArray the j array
   * @param def    the def
   * @return the json array
   */
  public static JSONArray chkNull(JSONArray jArray, JSONArray def){ return jArray != null ? jArray : def; }
  
  /**
   * Chk null string.
   *
   * @param str the str
   * @param def the def
   * @return the string
   */
  public static String chkNull(String str, String def){
    return isNonEmpty(str) ? str.trim() : def;
  }
  
  /**
   * Chk null list strings.
   *
   * @param list the list of strings
   * @param def  the def
   * @return the list of strings
   */
  public static List<String> chkNull(List<String> list, List<String> def){
    return isNonEmpty(list) ? list : def;
  }
  
  /**
   * Chk null class.
   *
   * @param cls the cls
   * @param def the def
   * @return the class
   */
  public static Class chkNull(Class cls, Class def){ return cls != null ? cls : def; }
  
  /**
   * Chk null object.
   *
   * @param obj the obj
   * @param def the def
   * @return the object
   */
  public static Object chkNull(Object obj, Object def){ return obj != null ? obj : def; }
  
  /**
   * Chk null view.
   *
   * @param view the view
   * @param def  the def
   * @return the object
   */
  public static View chkNull(View view, View def){ return view != null ? view : def; }
  
  public static int parseInt(String str){
    return parseInt(str, "0");
  }
  
  public static int parseInt(String str, String def){
    return Integer.parseInt(chkNull(str.replaceAll("-", ""), def));
  }
  
  public static int greater(int i1, int i2){
    return i1 >= i2 ? i1 : i2;
  }
  
  public static long greater(long l1, long l2){
    return l1 >= l2 ? l1 : l2;
  }
  
  public static float greater(float f1, float f2){
    return f1 >= f2 ? f1 : f2;
  }
  
  public static double greater(double d1, double d2){
    return d1 >= d2 ? d1 : d2;
  }
  
  /**
   * Chk zero string.
   *
   * @param str the str
   * @param def the def
   * @return the string
   */
  public static String chkZeroStart(String str, String def){
    try{
      return isNonEmpty(str) && !str.startsWith("0") ? str : def;
    }
    catch(NumberFormatException e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Chk zero string.
   *
   * @param str the str
   * @param def the def
   * @return the string
   */
  public static String chkZero(String str, String def){
    try{
      return isNonEmpty(str) && Double.parseDouble(str) > 0 ? str : def;
    }
    catch(NumberFormatException e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Chk zero integer.
   *
   * @param integer the integer
   * @param def     the def
   * @return the integer
   */
  public static Integer chkZero(Integer integer, Integer def){
    try{
      return chkNull(integer, 0) > 0 ? integer : def;
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Chk zero string.
   *
   * @param integer the integer
   * @param def     the def
   * @return the string
   */
  public static String chkZero(Integer integer, String def){
    try{
      return chkNull(integer, 0) > 0 ? String.valueOf(integer) : def;
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Chk zero long.
   *
   * @param aLong the a long
   * @param def   the def
   * @return the long
   */
  public static Long chkZero(Long aLong, Long def){
    try{
      return chkNull(aLong, 0l) > 0l ? aLong : def;
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Chk zero string.
   *
   * @param aLong the a long
   * @param def   the def
   * @return the string
   */
  public static String chkZero(Long aLong, String def){
    try{
      return chkNull(aLong, 0l) > 0l ? String.valueOf(aLong) : def;
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Chk null json object.
   *
   * @param jObj the j obj
   * @param def  the def
   * @return the json object
   */
  public static JSONObject chkNull(JSONObject jObj, JSONObject def){ return jObj != null ? jObj : def; }
  
  /**
   * Show log.
   *
   * @param tag the tag
   * @param msg the msg
   */
  public static void showLog(final String tag, final String msg){
    showLog(tag, msg, false);
  }
  
  /**
   * Show log.
   *
   * @param tag the tag
   * @param msg the msg
   */
  public static void showLog(String tag, String msg, final boolean isViewInRelease){
    if((isDebugApp || (SharedPrefManager.getIsAllowFileLogs() && isViewInRelease)) && isNonEmpty(tag) && isNonEmpty(msg)){
      if(isDebugApp) Log.e(tag, msg);
      else Log.e(LogFileUtilityHHD.APP_TAG, tag + "_" + msg);
    }
  }
  
  /**
   * Show short toast.
   *
   * @param context the context
   * @param res     the res
   */
  public static void showShortToast(Context context, int res){
    showToast(context, context.getString(res), false);
  }
  
  /**
   * Show long toast.
   *
   * @param context the context
   * @param res     the res
   */
  public static void showLongToast(Context context, int res){
    showToast(context, context.getString(res), true);
  }
  
  /**
   * Show short toast.
   *
   * @param context the context
   * @param msg     the msg
   */
  public static void showShortToast(Context context, String msg){
    showToast(context, msg, false);
  }
  
  /**
   * Show long toast.
   *
   * @param context the context
   * @param msg     the msg
   */
  public static void showLongToast(Context context, String msg){
    showToast(context, msg, true);
  }
  
  /**
   * Show toast.
   *
   * @param context     the context
   * @param msg         the msg
   * @param isLongToast the is long toast
   */
  static void showToast(Context context, String msg, boolean isLongToast){
    if(context != null && isNonEmpty(msg)){
      new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, msg.contains("<") && msg.contains(">") ? HtmlCompat.fromHtml(msg, HtmlCompat.FROM_HTML_MODE_LEGACY) : msg, isLongToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show());
    }
  }
  
  /**
   * Is valid url boolean.
   *
   * @param url the url
   * @return the boolean
   */
  public static boolean isValidUrl(String url){
    showLog("isValidUrl", url);
    return isNonEmpty(url) && url.matches("(?i)^(http|https)://.*$");
  }
  
  /**
   * To under score case string.
   *
   * @param str the str
   * @return the string
   */
  public static String toUnderScoreCase(String str){
    if(isNullOrEmpty(str)) return str;
    if(!str.replaceAll("([A-Z])", " $1").trim().matches(".*\\s+.*")) return str.toLowerCase();
    return str.replaceAll("([A-Z])", " $1").trim().replaceAll(" ", "_").toLowerCase();
  }
  
  /**
   * To camel case string.
   *
   * @param str the str
   * @return the string
   */
  public static String toCamelCase(String str){
    if(isNullOrEmpty(str)) return str;
    if(!str.replaceAll("_", " ").matches(".*\\s+.*")) return str.toLowerCase();
    StringBuilder result = new StringBuilder("");
    boolean is1st = true;
    for(String word : str.replaceAll("_", " ").split(" ")){
      result.append(is1st ? word.toLowerCase() : word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
      if(is1st) is1st = false;
      
    }
    return result.toString().trim();
  }
  
  /**
   * To pascal case string.
   *
   * @param str the str
   * @return the string
   */
  public static String toPascalCase(String str){
    if(isNullOrEmpty(str)) return str;
    if(!str.replaceAll("_", " ").matches(".*\\s+.*"))
      return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    StringBuilder result = new StringBuilder("");
    for(String word : str.replaceAll("_", " ").split(" ")){
      result.append(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
    }
    return result.toString().trim();
  }
  
  /**
   * To title case string.
   *
   * @param str the str
   * @return the string
   */
  public static String toTitleCase(String str){
    if(isNullOrEmpty(str)) return str;
    StringBuilder result = new StringBuilder("");
    if(str.matches("(?=.*[A-Z])(?!.*_).+")) str = str.replaceAll("([A-Z])", "_$1").trim();
    for(String word : str.replaceAll("_", " ").split(" ")){
      if (word.isEmpty()) continue;
      result.append(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
      result.append(" ");
    }
    return result.toString().replaceAll("([A-Z]) ", "$1").trim();
  }
  
  /**
   * To sentence case string.
   *
   * @param str the str
   * @return the string
   */
  public static String toSentenceCase(String str){
    if(isNullOrEmpty(str)) return str;
    StringBuilder result = new StringBuilder("");
    return result.toString();
  }
  
  /**
   * Dp 2 px int.
   *
   * @param dp the dp
   * @return the int
   */
  public static int dp2px(int dp){
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
  }
  
  /**
   * Sp 2 px int.
   *
   * @param sp the sp
   * @return the int
   */
  public static int sp2px(int sp){
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics());
  }
  
  /**
   * Get location.
   *
   * @param context the context
   */
  public static void getLocation(Context context){
    if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
      return;
    try{
      if(!checkLocationOn(context)) return;
    }
    catch(Exception e){ e.printStackTrace(); }
    try{
      LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
      if(locationManager != null){
        for(String provider : locationManager.getAllProviders()){
          if(locationManager.isProviderEnabled(provider)){
            locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener(){
              @Override
              public void onStatusChanged(String provider, int status, Bundle extras){
                //do nothing
              }
              
              @Override
              public void onLocationChanged(@NonNull Location location){
                if(location != null){
                  SharedPrefManager.setLatLng(location.getLatitude() + "," + location.getLongitude());
                  locationManager.removeUpdates(this);
                }
              }
              
              @Override
              public void onProviderEnabled(@NonNull String provider){
                showLog("onProviderEnabled", provider);
              }
              
              @Override
              public void onProviderDisabled(@NonNull String provider){
                showLog("onProviderDisabled", provider);
              }
            }, Looper.getMainLooper());
          }
        }
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  /**
   * Check location on boolean.
   *
   * @param context the context
   */
  public static boolean checkLocationOn(final Context context){
    try{
      boolean isLocationOn = context != null && Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF;
      if(!isLocationOn){
        if(context != null && context instanceof CommonActivity)
          ((CommonActivity) context).showCustomAlertDialog("", R.string.err_location_disabled, R.string.btn_ok, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              if(context instanceof CommonActivity && ((CommonActivity) context).locationResultLauncher != null){
                try{
                  final boolean isLocationOn = context != null && Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF;
                  if(!isLocationOn)
                    ((CommonActivity) context).locationResultLauncher.launch(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
                catch(Settings.SettingNotFoundException e){
                  e.printStackTrace();
                }
              }
              else checkLocationOn(context);
            }
          });
        return false;
      }
    }
    catch(Settings.SettingNotFoundException e){ e.printStackTrace(); }
    return true;
  }
  
  public static boolean isRooted(Context context){
    boolean isEmulator = isEmulator(context);
    if(!isEmulator && (chkNull(Build.TAGS, "").contains("test-keys") || checkRootMethod2()))
      return true;
    else if(checkRootMethod3()) return true;
    return false;
  }
  
  private static boolean checkRootMethod2(){
    String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
    for(String path : paths){
      if(new File(path).exists()) return true;
    }
    return false;
  }
  
  private static boolean checkRootMethod3(){
    Process process = null;
    try{
      process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
      BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
      if(in.readLine() != null) return true;
      return false;
    }
    catch(Throwable t){
      return false;
    }
    finally{
      if(process != null) process.destroy();
    }
  }
  
  public static boolean isEmulator(Context context){
    String androidId = Settings.Secure.getString(context.getContentResolver(), "android_id");
    return "sdk".equals(Build.PRODUCT) || "google_sdk".equals(Build.PRODUCT) || androidId == null;
  }
  
  public static boolean isStaticDebug(){
    //Commented Code (to be used for Demo Only)
    return isDemoApp ? ((mainActivity != null && mainActivity.isTablet) || isDebugApp) && isStaticDebugApp : isDebugApp && isStaticDebugApp;
  }
  
  public static boolean isStaticDebug(CommonActivity commonActivity){//temp code
    //Commented Code (to be used for Demo Only)
    return isDemoApp ? ((commonActivity != null && commonActivity.isTablet) || isDebugApp) && isStaticDebugApp : isStaticDebug();
  }
  
  /**
   * Create service t.
   *
   * @param <T>          the type parameter
   * @param serviceClass the service class
   * @param isTokenCall  the is token call
   * @param baseUrl      the base url
   * @return the t
   */
  public static <T> T createService(Class<T> serviceClass, final Boolean isTokenCall, final String baseUrl){
    OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(URLConstants.TIME_OUT, TimeUnit.SECONDS).readTimeout(URLConstants.TIME_OUT, TimeUnit.SECONDS).writeTimeout(URLConstants.TIME_OUT, TimeUnit.SECONDS).addInterceptor(new Interceptor(){
      @Override
      public okhttp3.Response intercept(Chain chain) throws IOException{
        Request request = isTokenCall == null ? chain.request().newBuilder().build() : chain.request().newBuilder().addHeader(ParamConstants.HEADER_ACCEPT, ParamConstants.HEADER_ACCEPT_VAL).addHeader(ParamConstants.HEADER_CONTENT_TYPE, ParamConstants.HEADER_CONTENT_TYPE_VAL).addHeader(ParamConstants.HEADER_AUTHORIZATION, (isTokenCall ? ParamConstants.BASIC_ACCESS_TOKEN_VAL : SharedPrefManager.getAccessToken())).build();
        return chain.proceed(request);
      }
    }).build();
    try{
      Retrofit retrofit = isTokenCall == null ? new Retrofit.Builder().baseUrl(baseUrl).client(httpClient).build() : new Retrofit.Builder().baseUrl(baseUrl).client(httpClient).addConverterFactory(GsonConverterFactory.create()).build();
      return retrofit.create(serviceClass);
    }
    catch(Exception e){ e.printStackTrace(); }
    return null;
  }
  
  public static <T> T createServiceFileDownload(Class<T> serviceClass, final Context context, final String baseUrl, final boolean isShowProgress){
    OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(URLConstants.TIME_OUT, TimeUnit.SECONDS).readTimeout(URLConstants.TIME_OUT, TimeUnit.SECONDS).writeTimeout(URLConstants.TIME_OUT, TimeUnit.SECONDS).addInterceptor(new DownloadProgressInterceptor(new DownloadProgressListener(){
      @Override
      public void update(long bytesRead, long contentLength, boolean done){
        if(isShowProgress){
          if(!done && contentLength > 0)
            showProgressDialog(context, "Downloading (" + String.format("%d%%", (100 * bytesRead) / contentLength) + ")...\nPlease Wait...");
          else if(done) hideProgressDialog(context);
        }
      }
    })).build();
    try{
      Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(httpClient).build();
      return retrofit.create(serviceClass);
    }
    catch(Exception e){ e.printStackTrace(); }
    return null;
  }
  
  /**
   * Get sample json json object.
   *
   * @param context  the context
   * @param fileName the file name
   * @return the json object
   */
  public static JSONObject getSampleJSON(Context context, String fileName){
    if(isDemoApp && fileName.equalsIgnoreCase("getStoreDetails")) fileName += "_demo";
    try(InputStream stream = context.getAssets().open(fileName + ".json")){
      int size = stream.available();
      byte[] buffer = new byte[size];
      stream.read(buffer);
      stream.close();
      return new JSONObject(new String(buffer));
    }
    catch(Exception e){ e.printStackTrace(); }
    return null;
  }
  
  /**
   * Is airplane mode on boolean.
   *
   * @param context the context
   * @return the boolean
   */
  private static boolean isAirplaneModeOn(Context context){
    return Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) > 0;
  }
  
  /**
   * Is internet connected boolean.
   *
   * @param context         the context
   * @param isShowErrDialog the is show err dialog
   * @param isShowErrToast  the is show err toast
   * @return the boolean
   */
  @SuppressLint("MissingPermission")
  public static boolean isInternetConnected(Context context, boolean isShowErrDialog, boolean isShowErrToast){
    boolean isNetConnected = false;
    if(context != null && (!(context instanceof Activity) || !((Activity) context).isFinishing())){
      ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      final boolean isAirPlaneMode = isAirplaneModeOn(context);
      if(connectivityManager != null && !isAirPlaneMode){
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null) isNetConnected = activeNetworkInfo.isConnected();
        else{
          NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
          if(networkInfos != null) for(NetworkInfo networkInfo : networkInfos)
            if(networkInfo.isConnected()){
              isNetConnected = true;
              break;
            }
        }
      }
      if(!isNetConnected && isShowErrDialog && context instanceof CommonActivity)
        ((CommonActivity) context).showCustomErrDialog(context.getString(isAirPlaneMode ? R.string.err_airplane_mode : R.string.err_internet_no_connect));
      if(!isNetConnected && isShowErrToast)
        showShortToast(context, context.getString(isAirPlaneMode ? R.string.err_airplane_mode : R.string.err_internet_no_connect));
    }
    if(!isNetConnected) allowBtnClick = true;
    return isNetConnected;
  }
  
  /**
   * Get ip address string.
   *
   * @param context the context
   * @return the string
   */
  public static String getIPAddress(final Context context){
    if(isInternetConnected(context, false, false)){
      try{
        for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ){
          NetworkInterface intf = en.nextElement();
          for(Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ){
            InetAddress inetAddress = enumIpAddr.nextElement();
            if(!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address){
              showLog("local ip", inetAddress.getHostAddress());
              return inetAddress.getHostAddress();
            }
          }
        }
      }
      catch(SocketException ex){ ex.printStackTrace(); }
    }
    return null;
  }
  
  /**
   * Get mac address string.
   *
   * @param context the context
   * @return the string
   */
  private static String getMacAddress(final Context context){
    String macAddress = "";
    try{
      macAddress = SharedPrefManager.getMACAddress();
      if(macAddress.length() > 0) return macAddress;
      List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
      for(NetworkInterface nif : all){
        if(!nif.getName().equalsIgnoreCase("wlan0")) continue;
        
        byte[] macBytes = nif.getHardwareAddress();
        if(macBytes == null){
          return "";
        }
        
        StringBuilder res1 = new StringBuilder();
        for(byte b : macBytes){
          res1.append(String.format("%02X:", b));
        }
        if(res1.length() > 0){ res1.deleteCharAt(res1.length() - 1); }
        SharedPrefManager.setMACAddress(res1.toString());
        return res1.toString();
      }
    }
    catch(Exception ex){ ex.printStackTrace(); }
    
    return "";
  }
  
  public static File getBaseDirectory(final Context context, final String subDir){
    final File dir = isUseInAppStorage ? subDir.equalsIgnoreCase(Environment.DIRECTORY_DOWNLOADS) ? context.getExternalFilesDir("") : context.getExternalCacheDir() : Environment.getExternalStorageDirectory();
    dir.mkdirs();
    if(chkNull(subDir, "").length() > 0){
      final File subDirs = new File(dir, subDir);
      subDirs.mkdirs();
      return subDirs;
    }
    else return dir;
  }
  
  public static void callDownloadFile(final Context context, final String url, final String fileName){ callDownloadFile(context, url, fileName, ""); }
  
  public static void callDownloadFile(final Context context, final String url, final String fileName, final String responseMsg){ callDownloadFile(context, null, null, url, fileName, responseMsg); }
  
  public static void callDownloadFile(final Context context, final CommonFragment fragment, final String url, final String fileName){ callDownloadFile(context, fragment, url, fileName, ""); }
  
  public static void callDownloadFile(final Context context, final CommonFragment fragment, final String url, final String fileName, final String responseMsg){ callDownloadFile(context, fragment, null, url, fileName, responseMsg); }
  
  public static void callDownloadFile(final Context context, final AppBroadcastReceiver receiver, final String url, final String fileName){ callDownloadFile(context, receiver, url, fileName, ""); }
  
  public static void callDownloadFile(final Context context, final AppBroadcastReceiver receiver, final String url, final String fileName, final String responseMsg){ callDownloadFile(context, null, receiver, url, fileName, responseMsg); }
  
  public static void callDownloadFile(final Context context, final CommonFragment fragment, final AppBroadcastReceiver receiver, final String url, final String fileName){ callDownloadFile(context, null, receiver, url, fileName, ""); }
  
  public static void callDownloadFile(final Context context, final CommonFragment fragment, final AppBroadcastReceiver receiver, final String url, final String fileName, final String responseMsg){
    final String fullFileURL = url + fileName;
    showLog("fullFileUrl", fullFileURL);
    final boolean isShowProgress = context != null && context instanceof CommonActivity && receiver == null;
    APIsInterface apiInterface = isShowProgress ? createServiceFileDownload(APIsInterface.class, context, url, isShowProgress) : createService(APIsInterface.class, null, url);
    showLog("apiInterface", "" + (apiInterface != null));
    if(apiInterface == null) return;
    if(isShowProgress) allowBtnClick = false;
    apiInterface.downloadFileByUrl(fileName).enqueue(new Callback<ResponseBody>(){
      @Override
      public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response){
        try{
          if(isShowProgress){
            allowBtnClick = true;
            hideProgressDialog(context);
          }
          showLog("codeFileDownload", "" + response.code());
          showLog("isSuccessFileDownload", "" + response.isSuccessful());
          if(response.isSuccessful()){
            if(isShowProgress) showProgressDialog(context, R.string.progress_msg_saving_file);
            new CommonTasks.WriteFileTask(context, fragment, receiver, url, fileName, isShowProgress, responseMsg).execute(response.body());
          }
          else{
            if(isShowProgress) allowBtnClick = true;
            if(response != null && response.body() != null){
            }
            else if(response != null && response.errorBody() != null){
            }
            else if(response != null && response.message() != null){
            }
            else{
            
            }
          }
        }
        catch(Exception e){
          e.printStackTrace();
          if(isShowProgress) allowBtnClick = true;
        }
      }
      
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t){
        t.printStackTrace();
        if(isShowProgress){
          allowBtnClick = true;
          hideProgressDialog(context);
        }
      }
    });
  }
  
  /**
   * Call web service.
   *
   * @param context     the context
   * @param receiver    the receiver
   * @param url         the url
   * @param jsonRequest the json request
   * @param args        the args
   * @param isRetry     the is retry
   */
  public static void callWebService(final Context context, final AppBroadcastReceiver receiver, final String url, JSONObject jsonRequest, final Bundle args, final boolean isRetry){
    callWebService(context, null, receiver, url, jsonRequest, args, isRetry, null, true, false);
  }
  
  /**
   * Call web service.
   *
   * @param context     the context
   * @param fragment    the fragment
   * @param url         the url
   * @param jsonRequest the json request
   * @param args        the args
   * @param isRetry     the is retry
   * @param progressMsg the progress msg
   */
  public static void callWebService(final Context context, final CommonFragment fragment, final String url, JSONObject jsonRequest, final Bundle args, final boolean isRetry, final String progressMsg){
    callWebService(context, fragment, null, url, jsonRequest, args, isRetry, progressMsg, false, false);
  }
  
  /**
   * Call web service.
   *
   * @param context     the context
   * @param fragment    the fragment
   * @param url         the url
   * @param jsonRequest the json request
   * @param args        the args
   * @param isRetry     the is retry
   * @param progressMsg the progress msg
   */
  public static void callWebService(final Context context, final CommonFragment fragment, final String url, JSONObject jsonRequest, final Bundle args, final boolean isRetry, final String progressMsg, final boolean isOfflineProcess){
    callWebService(context, fragment, null, url, jsonRequest, args, isRetry, progressMsg, isOfflineProcess, false);
  }
  
  /**
   * Call web service.
   *
   * @param context     the context
   * @param fragment    the fragment
   * @param url         the url
   * @param jsonRequest the json request
   * @param args        the args
   * @param isRetry     the is retry
   * @param progressMsg the progress msg
   */
  public static void callWebService(final Context context, final CommonFragment fragment, final String url, JSONObject jsonRequest, final Bundle args, final boolean isRetry, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    callWebService(context, fragment, null, url, jsonRequest, args, isRetry, progressMsg, isOfflineProcess, isDBProcess);
  }
  
  /**
   * Call web service.
   *
   * @param context     the context
   * @param fragment    the fragment
   * @param receiver    the receiver
   * @param url         the url
   * @param jsonRequest the json request
   * @param args        the args
   * @param isRetry     the is retry
   * @param progressMsg the progress msg
   */
  public static void callWebService(final Context context, final CommonFragment fragment, final AppBroadcastReceiver receiver, final String url, JSONObject jsonRequest, final Bundle args, final boolean isRetry, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    if(BuildConfig.IS_DIAGNOSTIC_BUILD){
      handleDiagnosticWebService(context, fragment, receiver, url, jsonRequest, args, progressMsg);
      return;
    }
    getLocation(context);
    if((isInternetConnected(context, false, true) || isStaticDebug()) && (context != null && (receiver != null || allowBtnClick))){
      if(context != null && context instanceof CommonActivity && receiver == null){
        allowBtnClick = false;
        showLog("allowBtnClick", "" + allowBtnClick);
      }
      AppCommonMethods.logInFile(context, url + "_REQUEST_" + jsonRequest.toString() + "_CALL");
      
      try{
        if(jsonRequest == null) jsonRequest = new JSONObject();
        jsonRequest.put(ParamConstants.DEVICE_ID, SharedPrefManager.getIMEI().toUpperCase());
        jsonRequest.put(ParamConstants.DEVICE_MAC_ID, SharedPrefManager.getIMEI().toUpperCase());
        jsonRequest.put(ParamConstants.DEVICE_TYPE, DEVICE_TYPE_VAL);
        jsonRequest.put(ParamConstants.APP_VERSION, BuildConfig.VERSION_NAME);
        jsonRequest.put(ParamConstants.APPLICATION_VERSION, BuildConfig.VERSION_NAME);
        jsonRequest.put(ParamConstants.MAC_ID, SharedPrefManager.getMACAddress());
        jsonRequest.put(ParamConstants.IP_ADDRESS, getIPAddress(context));
        jsonRequest.put(ParamConstants.LAT_LNG, SharedPrefManager.getLatLng());
        jsonRequest.put(ParamConstants.DEVICE_DATE_TIME, new SimpleDateFormat(SERVER_DATE_TIME_FORMAT).format(Calendar.getInstance().getTime()));
        jsonRequest.put(ParamConstants.TRANSACTION_DATE, new SimpleDateFormat(SERVER_DATE_TIME_FORMAT).format(Calendar.getInstance().getTime()));
        if(SharedPrefManager.getIsLoggedIn()){
          jsonRequest.put(ParamConstants.CLIENT_ID, SharedPrefManager.getClientID());
          jsonRequest.put(ParamConstants.USER_ID, SharedPrefManager.getUserID());
          jsonRequest.put(ParamConstants.STORE_ID, SharedPrefManager.getStoreID());
          jsonRequest.put(ParamConstants.STORE_CODE, SharedPrefManager.getStoreCode());
          jsonRequest.put(ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_ID, SharedPrefManager.getLong(ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_ID, 0l));
          jsonRequest.put(ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_NAME, SharedPrefManager.getString(ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_NAME, ""));
          jsonRequest.put(ParamConstants.IS_RFID_STORE, SharedPrefManager.getBoolean(ParamConstants.IS_RFID_STORE, false));
          jsonRequest.put(ParamConstants.TIME_ZONE_OFFSET_HOURS, SharedPrefManager.getDouble(ParamConstants.TIME_ZONE_OFFSET_HOURS, 0.00));
        }
      }
      catch(Exception e){ e.printStackTrace(); }
      retryCount = 0;
      callWebService(context, fragment, receiver, url, jsonRequest, args, isRetry, SharedPrefManager.getAccessTokenTime() < URLConstants.TIME_OUT, progressMsg, isOfflineProcess, isDBProcess);
    }
    else if(isOfflineProcess && (context != null && (receiver != null || allowBtnClick))){
      if(receiver != null) receiver.handleResponse(url, jsonRequest, null, -1, false, null);
      else if(fragment != null) fragment.handleResponse(url, jsonRequest, null, -1, false, null);
      else if(context != null && context instanceof CommonActivity)
        ((CommonActivity) context).handleResponse(url, jsonRequest, null, -1, false, null);
    }
    else if(context != null && context instanceof CommonActivity && receiver == null)
      allowBtnClick = true;
  }

  private static void handleDiagnosticWebService(final Context context, final CommonFragment fragment, final AppBroadcastReceiver receiver, final String url, JSONObject jsonRequest, final Bundle args, final String progressMsg){
    final CommonActivity activity = context instanceof CommonActivity ? (CommonActivity) context : null;
    try{
      if(jsonRequest == null) jsonRequest = new JSONObject();
      final boolean isUrlConfig = url != null && url.startsWith("http") && url.endsWith(SERVER_URL_APPEND_API);
      if(isUrlConfig){
        SharedPrefManager.setServerUrl(url);
        SharedPrefManager.setIsServerURLConfigured(true);
      }
      JSONObject result = isDiagnosticBackendMutation(url) || isUrlConfig ? null : getSampleJSON(context, url);
      boolean isSuccess = result != null || isUrlConfig;
      if(result == null){
        result = new JSONObject();
        result.put(ParamConstants.MESSAGE, isUrlConfig ? "Diagnostic build: server URL marked local-only." : "Diagnostic build: backend disabled; no remote call was made.");
      }
      if(receiver != null) receiver.handleResponse(url, jsonRequest, result, 200, isSuccess, args);
      else if(fragment != null) fragment.handleResponse(url, jsonRequest, result, 200, isSuccess, args);
      else if(activity != null) activity.handleResponse(url, jsonRequest, result, 200, isSuccess, args);
    }
    catch(Exception e){
      e.printStackTrace();
    }
    finally{
      allowBtnClick = true;
      if(activity != null && !activity.isFinishing() && progressMsg != null) activity.hideProgressDialog();
    }
  }

  private static boolean isDiagnosticBackendMutation(final String url){
    final String value = chkNull(url, "").toLowerCase();
    return value.contains("upload") ||
        value.contains("logout") ||
        value.contains("reset") ||
        value.contains("update") ||
        value.contains("set") ||
        value.contains("release") ||
        value.contains("complete") ||
        value.contains("accept") ||
        value.contains("reject") ||
        value.contains("verify");
  }
  
  /**
   * Call web service.
   *
   * @param context     the context
   * @param fragment    the fragment
   * @param receiver    the receiver
   * @param url         the url
   * @param jsonRequest the json request
   * @param args        the args
   * @param isRetry     the is retry
   * @param isTokenCall the is token call
   * @param progressMsg the progress msg
   */
  public static void callWebService(final Context context, final CommonFragment fragment, final AppBroadcastReceiver receiver, final String url, final JSONObject jsonRequest, final Bundle args, final boolean isRetry, final boolean isTokenCall, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    if(BuildConfig.IS_DIAGNOSTIC_BUILD){
      handleDiagnosticWebService(context, fragment, receiver, url, jsonRequest, args, progressMsg);
      return;
    }
    final CommonActivity activity = context != null && context instanceof CommonActivity ? (CommonActivity) context : null;
    final boolean isUrlConfig = isTokenCall && url.startsWith("http") && url.endsWith(SERVER_URL_APPEND_API);
    final String baseURL = isUrlConfig ? url : SharedPrefManager.getServerUrl();
    final boolean isStaticDebug = (isDebugApp || (activity != null && activity.isTablet) || (mainActivity != null && mainActivity.isTablet)) && isStaticDebugApp;
    APIsInterface apiCall = !isUrlConfig && isStaticDebug ? null : createService(APIsInterface.class, isTokenCall, baseURL);
    if(isStaticDebug && isUrlConfig){
      SharedPrefManager.setServerUrl(url);
      SharedPrefManager.setIsServerURLConfigured(true);
      if(activity != null && !activity.isFinishing() && progressMsg != null){
        hideProgressDialog(activity);
        activity.showCustomAlertDialog("", context.getString(R.string.done_server_url), null, true, true, context.getString(R.string.btn_ok), null);
      }
      return;
    }
    if(!isUrlConfig && isStaticDebug){
      showLog(url + "request", jsonRequest.toString(), !isTokenCall);
      activity.insertAuditTrailsLog("API_START", url, jsonRequest.toString());
      boolean isSuccess = false;
      int responseCode = 0;
      JSONObject result = null;
      String errMsg = "";
      boolean isForceLogout = false;
      try{
        result = getSampleJSON(context, url);
        responseCode = 200;
        isSuccess = true;
        isForceLogout = extractBoolean(result, ParamConstants.IS_FORCE_LOGOUT, false);
        AppCommonMethods.logInFile(context, url + "_RESULT_" + result != null ? result.toString() : "null" + "(" + isSuccess + ")");
        showLog(url + "_result", result != null ? result.toString() : "", !isTokenCall);
        
        if(receiver != null)
          receiver.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
        else if(fragment != null)
          fragment.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
        else if(activity != null)
          activity.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
      }
      catch(Exception e){
        e.printStackTrace();
        errMsg = context.getString(R.string.err_server_no_parse_json);
        showLog(url + "_json exception err", errMsg, !isTokenCall);
      }
      if(!isShowErrorForOfflineProcess && isOfflineProcess && !isForceLogout && !isRetry && responseCode != 200 && responseCode != 400 && responseCode != 406 && responseCode != 411){
        errMsg = "";
        if(receiver != null)
          receiver.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
        else if(fragment != null)
          fragment.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
        else if(activity != null)
          activity.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
      }
      else if(activity != null && !activity.isFinishing() && progressMsg != null && isNonEmpty(errMsg))
        activity.showCustomErrRetryDialog(fragment, url, jsonRequest, result, responseCode, args, !isForceLogout && isRetry, progressMsg, responseCode == 404 ? context.getString(isUrlConfig ? R.string.err_server_no_connect_config : R.string.err_server_no_connect_failed) : errMsg, isForceLogout);
      if(activity != null && !activity.isFinishing() && progressMsg != null){
        allowBtnClick = true;
        activity.hideProgressDialog();
        activity.insertAuditTrailsLog("API_STOP", url, result != null ? result.toString() : chkNull(errMsg, ""));
        showLog("allowBtnClick", "" + allowBtnClick);
      }
    }
    if(apiCall == null) return;
    final JSONObject jsonReq = isTokenCall ? null : jsonRequest;
    Call<ResponseBody> call = invokeMethod(isStaticDebug, apiCall, isTokenCall ? URLConstants.GET_ACCESS_TOKEN : isStaticDebug ? URLConstants.GET_STORE_DETAILS : url, jsonReq);
    if(call == null) return;
    if(url.toLowerCase().contains("enc") && url.toLowerCase().contains("than"))
      logInFile(context, SessionType.ENCODING_THAN.name(), "_callAPI (" + url + "->" + jsonReq + ")");
    else if(url.toLowerCase().contains("enc"))
      logInFile(context, SessionType.ENCODING.name(), "_callAPI (" + url + "->" + jsonReq + ")");
    showLog("URL", call.request().url().toString().replaceFirst("/getStoreDetails", isStaticDebug ? "/" + url : "/getStoreDetails"));
    showLog("Token ", isTokenCall ? ParamConstants.BASIC_ACCESS_TOKEN_VAL : SharedPrefManager.getAccessToken());
    if(!isTokenCall && jsonRequest != null){
      if(isStaticDebug && isNullOrEmpty(extractString(jsonRequest, ParamConstants.PIN))){
        try{
          jsonRequest.put(ParamConstants.PIN, "1234");
        }
        catch(Exception e){/*Dont' handle*/}
      }
      showLog("Request", jsonRequest.toString(), !isTokenCall);
      activity.insertAuditTrailsLog("API_START", url, jsonRequest.toString());
    }
    if(activity != null && !activity.isFinishing() && progressMsg != null)
      showProgressDialog(context, chkNull(isTokenCall ? context.getString(R.string.progress_msg_connect_server) : progressMsg, ""));
    call.enqueue(new Callback<ResponseBody>(){
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
        JSONObject result = null;
        String errMsg = "";
        boolean isForceLogout = false;
        final int responseCode = response != null ? response.code() : -1;
        final boolean isSuccess = response != null && response.isSuccessful();
        if(jsonReq != null && !isTokenCall)// && (url.toUpperCase().contains("ENC")||url.toUpperCase().contains("INV")||url.toUpperCase().contains("STOCK")))
          AppCommonMethods.logInFile(context, url + "_REQUEST_" + jsonReq.toString() + "(" + isSuccess + ")");
        try{
          if((!isDBProcess || !isSuccess) && activity != null && !activity.isFinishing() && progressMsg != null)
            hideProgressDialog(activity);
          showLog("isSuccess", "" + isSuccess, !isTokenCall);
          showLog("code", "" + responseCode, !isTokenCall);
          //code to recall Token-Web-Service if Expired
          if(response != null && responseCode == 403 && retryCount < 3){
            retryCount++;
            callWebService(context, fragment, receiver, url, jsonRequest, args, isRetry, true, progressMsg, isOfflineProcess, isDBProcess);
            return;
          }
          if(response != null && response.isSuccessful()){
            String responseBody = getStringFromInputStream(response.body().byteStream());
            showLog("response", responseBody);
            responseBody = responseBody.replaceAll("\\[\\s*\\{", "[{").replaceAll("\\}\s*\\]", "}]").trim();
            showLog("response1", responseBody);
            //In case of JSONArray as Response
            if(responseBody.startsWith("[") || responseBody.endsWith("]")){
              if(responseBody.matches("(?i)(" + ParamConstants.SUCCESS + ")") || responseBody.matches("(?i)(" + ParamConstants.ERROR + ")")){
                responseBody = responseBody.substring(responseBody.indexOf("{"), responseBody.lastIndexOf("}") + 1);
                showLog("response2", responseBody);
              }
              try{
                JSONArray jsonArray = new JSONArray(responseBody);
                if(isNonEmpty(jsonArray)){
                    JSONObject obj = new JSONObject();
                    obj.put(ParamConstants.DATA, jsonArray);
                    responseBody = obj.toString();
                    showLog("response3", responseBody);
                  }
                }
              catch(Exception e){
                e.printStackTrace();
              }
            }
            //In case of Empty String Response
            if(!responseBody.startsWith("{") && !responseBody.endsWith("}")){
              if(responseBody.contains("{") && responseBody.contains("}"))
                responseBody = responseBody.substring(responseBody.indexOf("{"), responseBody.lastIndexOf("}") + 1);
              else if(responseBody.contains(":")) responseBody = "{" + responseBody + "}";
              else if(!responseBody.contains("{") || !responseBody.contains("}")){
                if(responseBody.contains("\"")) responseBody = "{\"message\":" + responseBody + "}";
                else responseBody = "{\"message\":\"" + responseBody + "\"}";
              }
              showLog("response3", responseBody);
            }
            //temp code conditions (for static json responses) ---
            if(!isTokenCall && isStaticDebug) result = getSampleJSON(context, url);
            // ----
            if(result == null) result = response != null ? new JSONObject(responseBody) : null;
            if(isNonEmpty(result)) result = extractJSONObject(result, ParamConstants.DATA, result);
            AppCommonMethods.logInFile(context, url + "_RESULT_" + result != null ? result.toString() : "null" + "(" + isSuccess + ")");
            showLog(url + "_result", result != null ? result.toString() : "", !isTokenCall);
            showLog("isTokenCall", "" + isTokenCall);
            
            final String err = extractString(result, ParamConstants.ERROR, extractString(result, ParamConstants.ERR_MSG, ""));
            errMsg = isNullOrEmpty(chkNull(err, "").replaceFirst("(?i)(true|false)", "").trim()) ? "" : err;
            if(activity != null && !activity.isFinishing() && progressMsg != null){
              allowBtnClick = true;
              showLog("allowBtnClick", "" + allowBtnClick);
              isForceLogout = extractBoolean(result, ParamConstants.IS_FORCE_LOGOUT, false);
            }
            if(!isTokenCall && isForceLogout && SharedPrefManager.getIsLoggedIn() && activity != null && !activity.isFinishing() && activity instanceof MainActivity){
              final String msg = extractString(result, ParamConstants.MESSAGE, extractString(result, ParamConstants.MSG, ""));
              if(isNonEmpty(msg)){
                hideProgressDialog(activity);
                activity.showCustomSuccessDialog(msg, new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialog, int which){
                    if(activity instanceof MainActivity)
                      ((MainActivity) activity).clearSavedDataOnLogout();
                    else activity.popBackStack();
                  }
                });
                return;
              }
            }
            if(url.toLowerCase().contains("enc") && url.toLowerCase().contains("than"))
              logInFile(context, SessionType.ENCODING_THAN.name(), "_APIResponse (" + url + "->" + result + ")");
            else if(url.toLowerCase().contains("enc"))
              logInFile(context, SessionType.ENCODING.name(), "_APIResponse (" + url + "->" + result + ")");
            if(isTokenCall){
              if(isUrlConfig){
                SharedPrefManager.setServerUrl(url);
                SharedPrefManager.setIsServerURLConfigured(true);
                if(isDebugApp) processConfiguredUrl(url);
                if(activity != null && !activity.isFinishing() && progressMsg != null)
                  activity.showCustomAlertDialog("", context.getString(R.string.done_server_url), null, true, true, context.getString(R.string.btn_ok), null);
              }
              final String token = extractString(result, ParamConstants.TOKEN_TYPE) + " " + extractString(result, ParamConstants.ACCESS_TOKEN);
              SharedPrefManager.setAccessToken(token);
              long expiry = extractLong(result, ParamConstants.EXPIRES_IN);
              SharedPrefManager.setAccessTokenTime(expiry);
              if(!url.equalsIgnoreCase(URLConstants.GET_ACCESS_TOKEN) && (!url.startsWith("http") || !url.endsWith(SERVER_URL_APPEND_API)))
                callWebService(context, fragment, receiver, url, jsonRequest, args, isRetry, false, progressMsg, isOfflineProcess, isDBProcess);
              return;
            }
            else if(receiver != null)
              receiver.handleResponse(url, jsonRequest, result, responseCode, isSuccess && isNullOrEmpty(errMsg), args);
            else if(fragment != null && isNullOrEmpty(errMsg))
              fragment.handleResponse(url, jsonRequest, result, responseCode, isSuccess && isNullOrEmpty(errMsg), args);
            else if(activity != null && isNullOrEmpty(errMsg))
              activity.handleResponse(url, jsonRequest, result, responseCode, isSuccess && isNullOrEmpty(errMsg), args);
          }
          else if(response != null && response.body() != null){
            if(activity != null && !activity.isFinishing() && progressMsg != null){
              allowBtnClick = true;
              showLog("fail body allowBtnClick", "" + allowBtnClick);
            }
            if(!isTokenCall && isStaticDebug){
              result = getSampleJSON(context, url);
              showLog(url + "_result", result != null ? result.toString() : "", !isTokenCall);
              if(receiver != null)
                receiver.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
              else if(fragment != null)
                fragment.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
              else if(activity != null)
                activity.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
              return;
            }
            else{
              String responseBody = getStringFromInputStream(response.body().byteStream());
              showLog("fail body response", responseBody);
              responseBody = responseBody.replaceAll("\\[\\s*\\{", "[{").replaceAll("\\}\s*\\]", "}]");
              showLog("fail body response1", responseBody);
              if(responseBody.startsWith("[{") && responseBody.endsWith("}]")){
                responseBody = responseBody.substring(responseBody.indexOf("{"), responseBody.lastIndexOf("}") + 1);
                showLog("fail body response2", responseBody);
              }
              result = response != null ? new JSONObject(responseBody) : null;
              showLog(url + "_result", result != null ? result.toString() : "", !isTokenCall);
              AppCommonMethods.logInFile(context, url + "_RESULT_" + result != null ? result.toString() : "null" + "(" + isSuccess + ")");
              isForceLogout = extractBoolean(result, ParamConstants.IS_FORCE_LOGOUT);
              errMsg = extractString(result, isTokenCall ? ParamConstants.TOKEN_ERR_MESSAGE : ParamConstants.MESSAGE, context.getString(R.string.err_server_no_connect)).replaceAll(baseURL, "").replaceAll(url, "").replaceAll("\"\"", "").replaceAll("''", "").trim();
            }
          }
          else if(response != null && response.errorBody() != null){
            if(activity != null && !activity.isFinishing() && progressMsg != null){
              allowBtnClick = true;
              showLog("err body allowBtnClick", "" + allowBtnClick);
            }
            if(!isTokenCall && isStaticDebug){
              result = getSampleJSON(context, url);
              showLog(url + "_result", result != null ? result.toString() : "");
              if(receiver != null)
                receiver.handleResponse(url, jsonRequest, result, responseCode, true, args);
              else if(fragment != null)
                fragment.handleResponse(url, jsonRequest, result, responseCode, true, args);
              else if(activity != null)
                activity.handleResponse(url, jsonRequest, result, responseCode, true, args);
            }
            else{
              String responseErrBody = getStringFromInputStream(response.errorBody().byteStream());
              showLog("err body response", responseErrBody);
              responseErrBody = responseErrBody.replaceAll("\\[\\s*\\{", "[{").replaceAll("\\}\s*\\]", "}]");
              showLog("err body response1", responseErrBody);
              if(responseErrBody.startsWith("[{") && responseErrBody.endsWith("}]")){
                responseErrBody = responseErrBody.substring(responseErrBody.indexOf("{"), responseErrBody.lastIndexOf("}") + 1);
                showLog("err body response2", responseErrBody);
              }
              result = response != null ? new JSONObject(responseErrBody) : null;
              showLog(url + "_result", result != null ? result.toString() : "");
              if(url.toLowerCase().contains("enc") && url.toLowerCase().contains("than"))
                logInFile(context, SessionType.ENCODING_THAN.name(), "_APIResponse (" + url + "->" + result + ")");
              else if(url.toLowerCase().contains("enc"))
                logInFile(context, SessionType.ENCODING.name(), "_APIResponse (" + url + "->" + result + ")");
              AppCommonMethods.logInFile(context, url + "_RESULT_" + result != null ? result.toString() : "null" + "(" + isSuccess + ")");
              isForceLogout = extractBoolean(result, ParamConstants.IS_FORCE_LOGOUT, false);
              if(!isShowErrorForOfflineProcess && isOfflineProcess && !isForceLogout && !isRetry && responseCode != 200 && responseCode != 400 && responseCode != 406 && responseCode != 411){
                errMsg = "";
                if(receiver != null)
                  receiver.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
                else if(fragment != null)
                  fragment.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
                else if(activity != null)
                  activity.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
                return;
              }
              else
                errMsg = responseCode == 404 ? context.getString(isUrlConfig ? R.string.err_server_no_connect_config : R.string.err_server_no_connect_failed) : extractString(result, isTokenCall ? ParamConstants.TOKEN_ERR_MESSAGE : ParamConstants.MESSAGE, context.getString(R.string.err_server_no_connect)).replaceAll(baseURL, "").replaceAll(url, "").replaceAll("\"\"", "").replaceAll("''", "").trim();
            }
          }
          else if(response != null && response.message() != null){
            if(!isShowErrorForOfflineProcess && isOfflineProcess && !isForceLogout && !isRetry && responseCode != 200 && responseCode != 400 && responseCode != 406 && responseCode != 411){
              errMsg = "";
              if(receiver != null)
                receiver.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
              else if(fragment != null)
                fragment.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
              else if(activity != null)
                activity.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
              return;
            }
            else
              errMsg = chkNull(response.message(), "").replaceAll(baseURL, "").replaceAll(url, "").replaceAll("\"\"", "").replaceAll("''", "").trim();
            showLog(url + "_err msg response", errMsg, !isTokenCall);
          }
          else{
            if(!isShowErrorForOfflineProcess && isOfflineProcess && !isForceLogout && !isRetry && responseCode != 200 && responseCode != 400 && responseCode != 406 && responseCode != 411){
              errMsg = "";
              if(receiver != null)
                receiver.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
              else if(fragment != null)
                fragment.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
              else if(activity != null)
                activity.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
              return;
            }
            else errMsg = context.getString(R.string.err_server_no_connect).trim();
          }
        }
        catch(JSONException e){
          e.printStackTrace();
          errMsg = context.getString(R.string.err_server_no_parse_json);
          showLog(url + "_json exception err", errMsg, !isTokenCall);
          if(isDBProcess && isSuccess && activity != null && !activity.isFinishing() && progressMsg != null)
            hideProgressDialog(activity);
        }
        if(!isShowErrorForOfflineProcess && isOfflineProcess && !isForceLogout && !isRetry && responseCode != 200 && responseCode != 400 && responseCode != 406 && responseCode != 411){
          errMsg = "";
          if(receiver != null)
            receiver.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
          else if(fragment != null)
            fragment.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
          else if(activity != null)
            activity.handleResponse(url, jsonRequest, result, responseCode, isSuccess, args);
        }
        else if(activity != null && !activity.isFinishing() && progressMsg != null && isNonEmpty(errMsg)){
          if(fragment != null && fragment instanceof DecodingStartFragment && (url.equalsIgnoreCase(fragment.getProductInfoUrl()) || url.equalsIgnoreCase(URLConstants.VALIDATE_PRODUCT_AGE_FOR_DECODE)))
            fragment.handleResponse(url, jsonRequest, result, responseCode, false, args);
          else{
            if(fragment != null && url.equalsIgnoreCase(fragment.getEPCForEncodeUrl()))
              fragment.handleResponse(url, jsonRequest, result, responseCode, false, args);
            activity.showCustomErrRetryDialog(fragment, url, jsonRequest, result, responseCode, args, !isForceLogout && isRetry, progressMsg, responseCode == 404 ? context.getString(isUrlConfig ? R.string.err_server_no_connect_config : R.string.err_server_no_connect_failed) : errMsg, isForceLogout);
          }
        }
        if(activity != null && !activity.isFinishing() && progressMsg != null){
          activity.insertAuditTrailsLog("API_STOP", url, result != null ? result.toString() : chkNull(errMsg, ""));
          allowBtnClick = true;
          showLog("allowBtnClick", "" + allowBtnClick);
        }
      }
      
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t){
        showLog("t", t.getClass().getSimpleName(), !isTokenCall);
        if(receiver != null && (url.equalsIgnoreCase(URLConstants.UPLOAD_ENCODING) || url.equalsIgnoreCase(URLConstants.UPLOAD_ENCODE) || url.equalsIgnoreCase(URLConstants.UPLOAD_ENCODING_THAN) || url.equalsIgnoreCase(URLConstants.UPLOAD_DECODING))){
          receiver.isUploadWrittenTagsRunning = false;
          //showLog("isUploadWrittenTagsRunning3", "" + receiver.isUploadWrittenTagsRunning);
        }
        t.printStackTrace();
        if(t != null && jsonReq != null && !isTokenCall)// && (url.toUpperCase().contains("ENC")||url.toUpperCase().contains("INV")||url.toUpperCase().contains("STOCK")))
          AppCommonMethods.logInFile(context, url + "_REQUEST_" + jsonReq.toString() + "\n_RESULT (" + t.getMessage() + ")");
        if(activity != null && !activity.isFinishing() && progressMsg != null)
          hideProgressDialog(activity);
        
        final int errMsgResId = t == null ? R.string.err_server_no_connect : t instanceof SocketTimeoutException ? R.string.err_server_no_connect_timeout : t instanceof ConnectException ? isUrlConfig ? R.string.err_server_no_connect_config : R.string.err_server_no_connect_failed : t instanceof ProtocolException ? R.string.err_no_data : 0;
        final String errMsg = chkNull(errMsgResId != 0 ? context.getString(errMsgResId) : "", chkNull(t.getMessage(), "")).replaceAll(baseURL, "").replaceAll(url, "").replaceAll("\"\"", "").replaceAll("''", "").trim();
        showLog("msg failure", errMsg, !isTokenCall);
        if(receiver != null && (url.equalsIgnoreCase(URLConstants.UPLOAD_ENCODING) || url.equalsIgnoreCase(URLConstants.UPLOAD_ENCODE) || url.equalsIgnoreCase(URLConstants.UPLOAD_ENCODING_THAN) || url.equalsIgnoreCase(URLConstants.UPLOAD_DECODING)))
          receiver.handleResponse(url, jsonRequest, null, -1, false, args);
        if(activity!=null && fragment!=null && fragment instanceof OutwardToteDCFragment && url.equalsIgnoreCase(URLConstants.GET_OUTWARD_TOTE_EANS)){ //&& apply condition for 404 error
          fragment.handleResponse(url, jsonRequest, null,404,false,args);
          }
        if(!isShowErrorForOfflineProcess && isOfflineProcess && !isRetry && isNullOrEmpty(errMsg)){
          if(receiver != null) receiver.handleResponse(url, jsonRequest, null, -1, false, args);
          else if(fragment != null)
            fragment.handleResponse(url, jsonRequest, null, -1, false, args);
          else if(activity != null)
            activity.handleResponse(url, jsonRequest, null, -1, false, args);
          return;
        }
        if(activity != null && !activity.isFinishing() && progressMsg != null && isNonEmpty(errMsg))
          activity.showCustomErrRetryDialog(fragment, url, jsonRequest, null, -1, args, isRetry, progressMsg, errMsg, false);
        if(activity != null && !activity.isFinishing() && progressMsg != null){
          activity.insertAuditTrailsLog("API_STOP", url, errMsg);
          allowBtnClick = true;
          showLog("allowBtnClick", "" + allowBtnClick);
        }
      }
    });
  }
  
  /**
   * Invoke method call.
   *
   * @param apiCall     the api call
   * @param url         the url
   * @param jsonRequest the json request
   * @return the call
   */
  public static Call<ResponseBody> invokeMethod(final boolean isStaticDebug, final APIsInterface apiCall, final String url, final JSONObject jsonRequest){
    if(url == null) return null;
    //final boolean isStaticDebug = isStaticDebug();
    switch(url){
      case URLConstants.GET_ACCESS_TOKEN:
        HashMap<String, String> jsonToken = new HashMap<>(0);
        jsonToken.put(ParamConstants.TOKEN_GRANT_TYPE, ParamConstants.TOKEN_GRANT_TYPE_VAL);
        jsonToken.put(ParamConstants.TOKEN_USER_NAME, !isSetUserMgmt || isStaticDebug ? ParamConstants.TOKEN_USER_NAME_VAL : chkNull(SharedPrefManager.getUserID(), ParamConstants.TOKEN_USER_NAME_VAL));
        jsonToken.put(ParamConstants.TOKEN_PASSWORD, !isSetUserMgmt || isStaticDebug ? ParamConstants.TOKEN_PASSWORD_VAL : chkNull(SharedPrefManager.getPassword(), ParamConstants.TOKEN_PASSWORD_VAL));
        jsonToken.put(ParamConstants.TOKEN_DEVICE_ID, SharedPrefManager.getIMEI());
        showLog("jsonToken", jsonToken.toString());
        return apiCall.getAccessToken(jsonToken);
      case URLConstants.GET_STORE_DETAILS:
        return apiCall.getStoreDetails(getGSON().fromJson(jsonRequest.toString(), JsonObject.class));
      case URLConstants.GET_INVENTORY_DASHBOARD:
        return apiCall.getInventoryDashboard(getGSON().fromJson(jsonRequest.toString(), JsonObject.class));
      case URLConstants.GET_ACTIVE_USERS:
        return apiCall.getActiveUserCount(getGSON().fromJson(jsonRequest.toString(), JsonObject.class));
      case URLConstants.GET_STOCK_CORRECTION_DASHBOARD:
        return apiCall.getStockCorrectionDashboard(getGSON().fromJson(jsonRequest.toString(), JsonObject.class));
      case URLConstants.GET_OUTWARD_TOTE_EANS:
        return apiCall.getToteMaster();
      default:
        break;
    }
    try{
      Method method = apiCall.getClass().getMethod(url, JsonObject.class);
      showLog("method", "" + (method != null ? method.getName() : "null"));
      if(method != null)
        return (Call<ResponseBody>) method.invoke(apiCall, getGSON().fromJson(jsonRequest.toString(), JsonObject.class));
    }
    catch(SecurityException e){ e.printStackTrace(); }
    catch(NoSuchMethodException e){
      e.printStackTrace();
    }
    catch(InvocationTargetException e){ e.printStackTrace(); }
    catch(IllegalAccessException e){
      e.printStackTrace();
    }
    allowBtnClick = true;
    return null;
  }
  
  private static void processConfiguredUrl(final String url){
    final boolean isUseRefId = url.matches("(?i)(^.*(eyewa|chl).*$)");
    showLog("isUseRefId", "" + isUseRefId);
    AppCommonMethods.isUseReferenceBarcode = isUseRefId;
    final boolean isUseGID = isUseRefId || url.matches("(?i)(^.*(tata|zudio|trent|westside|zara|titan|taneira|cliq|fasttrack|rover|chroma|zoya|tanishq|caratlane|neu|bigbasket|starbazaar).*$)");
    AppCommonMethods.isUseGID = isUseGID;
    SharedPrefManager.setString(ParamConstants.ENCODE_ALGORITHM_STD, (SharedPrefManager.getBoolean(ParamConstants.IS_GID, isUseGID) ? EPCEncoderDecoder.EncodeAlgorithmStd.TATA_GID.toString() : EPCEncoderDecoder.EncodeAlgorithmStd.SGTIN.toString()));
    SharedPrefManager.setString(ParamConstants.ENCODE_ALGORITHM_NON_STD, (SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_ALPHANUMERIC_NON_STD_EAN, true) ? EPCEncoderDecoder.EncodeAlgorithmNonStd.BC_ALPHA_NUM : EPCEncoderDecoder.EncodeAlgorithmNonStd.BC_NUM).toString());
    
    final boolean isShowNonEncodedAlienCountsInInventory = !isUseGID && url.matches("(?i)(^.*(giva|njl).*$)");
    AppCommonMethods.isShowUnencodedAndAlienCountsInInventory = isShowNonEncodedAlienCountsInInventory;
  }
  
  public static JSONObject getJsonFromBundle(final Bundle bundle){
    JSONObject jsonObject = new JSONObject();
    Set<String> keys = bundle.keySet();
    for(String key : keys){
      try{
        // json.put(key, bundle.get(key)); see edit below
        jsonObject.put(key, bundle.get(key));
      }
      catch(JSONException e){
        //Handle exception here
        e.printStackTrace();
      }
    }
    return isNonEmpty(jsonObject) && jsonObject.length() > 2 ? jsonObject : null;
  }
  
  public static HashMap<String, String> getObjFieldsMap(final Object obj){
    HashMap<String, String> map = new HashMap<>();
    if(obj != null) for(Field field : obj.getClass().getDeclaredFields()){
      field.setAccessible(true);
      try{
        final Object fieldVal = field.get(obj);
        if(fieldVal != null) map.put(field.getName().trim(), fieldVal.toString());
      }
      catch(IllegalAccessException e){
        e.printStackTrace();
      }
    }
    return map;
  }
  
  public static JSONArray getObjFieldsJson(final Object obj){
    JSONArray jsonArray = new JSONArray();
    if(obj != null) for(Field field : obj.getClass().getDeclaredFields()){
      field.setAccessible(true);
      try{
        final Object fieldVal = field.get(obj);
        if(fieldVal != null){
          jsonArray.put(getLabelValueJson(field.getName().trim(), fieldVal.toString()));
        }
      }
      catch(Exception e){ e.printStackTrace(); }
    }
    return jsonArray;
  }
  
  public static JSONObject getLabelValueJson(String label, String value){
    JSONObject jsonObject = null;
    if(chkNull(value, "").length() > 0){
      try{
        jsonObject = new JSONObject();
        jsonObject.put(ParamConstants.LABEL, label);
        jsonObject.put(ParamConstants.VALUE, value);
      }
      catch(Exception e){ }
    }
    return jsonObject;
  }
  
  /**
   * To map hash map.
   *
   * @param object the object
   * @return the hash map
   * @throws JSONException the json exception
   */
  public static HashMap<String, Object> toMap(JSONObject object) throws JSONException{
    HashMap<String, Object> map = new HashMap<String, Object>();
    
    Iterator<String> keysItr = object.keys();
    while(keysItr.hasNext()){
      String key = keysItr.next();
      Object value = object.get(key);
      
      if(value instanceof JSONArray){
        value = toList((JSONArray) value);
      }
      else if(value instanceof JSONObject){
        value = toMap((JSONObject) value);
      }
      map.put(key, value);
    }
    return map;
  }
  
  /**
   * To list list.
   *
   * @param array the array
   * @return the list
   * @throws JSONException the json exception
   */
  public static List<Object> toList(JSONArray array) throws JSONException{
    List<Object> list = new ArrayList<>(0);
    for(int i = 0; i < array.length(); i++){
      Object value = array.get(i);
      if(value instanceof JSONArray){
        value = toList((JSONArray) value);
      }
      
      else if(value instanceof JSONObject){
        value = toMap((JSONObject) value);
      }
      list.add(value);
    }
    return list;
  }
  
  /**
   * Get string from input stream string.
   *
   * @param inputStream the input stream
   * @return the string
   */
  public static String getStringFromInputStream(InputStream inputStream){
    BufferedReader reader = null;
    StringBuilder sb = new StringBuilder();
    try{
      reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      try{
        while((line = reader.readLine()) != null){
          sb.append(line);
        }
      }
      catch(IOException e){ e.printStackTrace(); }
    }
    catch(Exception e){ e.printStackTrace(); }
    return sb.toString();
  }
  
  /**
   * Is app in background boolean.
   *
   * @param context the context
   * @return the boolean
   */
  public static boolean isAppInBackground(final Context context){
    ActivityManager.RunningAppProcessInfo myProcess = new ActivityManager.RunningAppProcessInfo();
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
      ActivityManager.getMyMemoryState(myProcess);
    return myProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
  }
  
  /**
   * Gets gson.
   *
   * @return the gson
   * @throws JsonParseException the json parse exception
   */
  public static Gson getGSON() throws JsonParseException{
    return new GsonBuilder().registerTypeAdapter(String.class, new StringTrimJsonDeserializer()).create();
  }
  
  /**
   * Check key string.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @return the string
   */
  private static String checkKey(JSONObject jsonObject, String key){
    String k = null;
    if(jsonObject != null){
      if(jsonObject.has(key)) return key;
      k = toPascalCase(key);
      if(jsonObject.has(k)) return k;
      k = toCamelCase(key);
      if(jsonObject.has(k)) return k;
      k = toUnderScoreCase(key);
      if(jsonObject.has(k)) return k;
    }
    return null;
  }
  
  /**
   * Extract json array json array.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @return the json array
   */
  public static JSONArray extractJSONArray(JSONObject jsonObject, String key){ return extractJSONArray(jsonObject, key, null); }
  
  //Extract from JSONObject
  
  /**
   * Extract json array json array.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the json array
   */
  public static JSONArray extractJSONArray(JSONObject jsonObject, String key, JSONArray def){
    try{
      key = checkKey(jsonObject, key);
      if(jsonObject != null && isNonEmpty(key) && jsonObject.has(key) && jsonObject.get(key) != null && jsonObject.get(key) instanceof JSONArray)
        return chkNull(jsonObject.getJSONArray(key), def);
    }
    catch(JSONException e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract json object json object.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @return the json object
   */
  public static JSONObject extractJSONObject(JSONObject jsonObject, String key){ return extractJSONObject(jsonObject, key, null); }
  
  /**
   * Extract json object json object.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the json object
   */
  public static JSONObject extractJSONObject(JSONObject jsonObject, String key, JSONObject def){
    try{
      key = checkKey(jsonObject, key);
      if(jsonObject != null && isNonEmpty(key) && jsonObject.has(key) && jsonObject.get(key) != null && jsonObject.get(key) instanceof JSONObject)
        return chkNull(jsonObject.getJSONObject(key), def);
      else if(jsonObject != null && isNonEmpty(key) && jsonObject.has(key) && jsonObject.get(key) != null && jsonObject.get(key) instanceof String){
        try{
          return chkNull(new JSONObject(jsonObject.getString(key).replaceAll("\\\"","\"")), def);
        }catch (Exception e){e.printStackTrace();}
      }

    }
    catch(JSONException e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract string string.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @return the string
   */
  public static String extractString(JSONObject jsonObject, String key){ return extractString(jsonObject, key, null); }
  
  /**
   * Extract string string.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the string
   */
  public static String extractString(JSONObject jsonObject, String key, String def){
    try{
      key = checkKey(jsonObject, key);
      if(jsonObject != null && isNonEmpty(key) && jsonObject.has(key) && jsonObject.get(key) != null){
        if(jsonObject.get(key) instanceof String) return chkNull(jsonObject.getString(key), def);
        else return chkNull(jsonObject.get(key).toString(), def);
      }
    }
    catch(JSONException e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract int integer.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @return the integer
   */
  public static Integer extractInt(JSONObject jsonObject, String key){ return extractInt(jsonObject, key, null); }
  
  /**
   * Extract int integer.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the integer
   */
  public static Integer extractInt(JSONObject jsonObject, String key, Integer def){
    try{
      key = checkKey(jsonObject, key);
      if(jsonObject != null && isNonEmpty(key) && jsonObject.has(key) && jsonObject.get(key) != null){
        if(jsonObject.get(key) instanceof Integer) return chkNull(jsonObject.getInt(key), def);
        else if(jsonObject.get(key) instanceof Long)
          return chkNull((int) jsonObject.getLong(key), def);
        else if(jsonObject.get(key) instanceof Double)
          return chkNull((int) jsonObject.getDouble(key), def);
        else return chkNull(Integer.parseInt(jsonObject.get(key).toString()), def);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract long long.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @return the long
   */
  public static Long extractLong(JSONObject jsonObject, String key){ return extractLong(jsonObject, key, null); }
  
  /**
   * Extract long long.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the long
   */
  public static Long extractLong(JSONObject jsonObject, String key, Long def){
    try{
      key = checkKey(jsonObject, key);
      if(jsonObject != null && isNonEmpty(key) && jsonObject.has(key) && jsonObject.get(key) != null){
        if(jsonObject.get(key) instanceof Long) return chkNull(jsonObject.getLong(key), def);
        else if(jsonObject.get(key) instanceof Integer)
          return chkNull((long) jsonObject.getInt(key), def);
        else if(jsonObject.get(key) instanceof Double)
          return chkNull((long) jsonObject.getDouble(key), def);
        else return chkNull(Long.parseLong(jsonObject.get(key).toString()), def);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract float float.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @return the float
   */
  public static Float extractFloat(JSONObject jsonObject, String key){ return extractFloat(jsonObject, key, null); }
  
  /**
   * Extract float float.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the float
   */
  public static Float extractFloat(JSONObject jsonObject, String key, Float def){
    try{
      key = checkKey(jsonObject, key);
      if(jsonObject != null && isNonEmpty(key) && jsonObject.has(key) && jsonObject.get(key) != null){
        if(jsonObject.get(key) instanceof Float) return chkNull((float) jsonObject.get(key), def);
        else if(jsonObject.get(key) instanceof Double)
          return chkNull((float) jsonObject.getDouble(key), def);
        else if(jsonObject.get(key) instanceof Long)
          return chkNull((float) jsonObject.getLong(key), def);
        else if(jsonObject.get(key) instanceof Integer)
          return chkNull((float) jsonObject.getInt(key), def);
        else return chkNull(Float.parseFloat(jsonObject.get(key).toString()), def);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract double double.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @return the double
   */
  public static Double extractDouble(JSONObject jsonObject, String key){ return extractDouble(jsonObject, key, null); }
  
  /**
   * Extract double double.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the double
   */
  public static Double extractDouble(JSONObject jsonObject, String key, Double def){
    try{
      key = checkKey(jsonObject, key);
      if(jsonObject != null && isNonEmpty(key) && jsonObject.has(key) && jsonObject.get(key) != null){
        if(jsonObject.get(key) instanceof Double) return chkNull(jsonObject.getDouble(key), def);
        else if(jsonObject.get(key) instanceof Long)
          return chkNull((double) jsonObject.getLong(key), def);
        else if(jsonObject.get(key) instanceof Integer)
          return chkNull((double) jsonObject.getInt(key), def);
        else return chkNull(Double.parseDouble(jsonObject.get(key).toString()), def);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract boolean boolean.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @return the boolean
   */
  public static Boolean extractBoolean(JSONObject jsonObject, String key){ return extractBoolean(jsonObject, key, null); }
  
  /**
   * Extract boolean boolean.
   *
   * @param jsonObject the json object
   * @param key        the key
   * @param def        the def
   * @return the boolean
   */
  public static Boolean extractBoolean(JSONObject jsonObject, String key, Boolean def){
    try{
      key = checkKey(jsonObject, key);
      if(jsonObject != null && isNonEmpty(key) && jsonObject.has(key) && jsonObject.get(key) != null){
        if(jsonObject.get(key) instanceof Boolean) return chkNull(jsonObject.getBoolean(key), def);
        else return chkNull(Boolean.parseBoolean(jsonObject.get(key).toString()), def);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  //Extract from Intent/Bundle
  
  /**
   * Extract string string.
   *
   * @param args the args
   * @param key  the key
   * @return the string
   */
  public static String extractString(Intent args, String key){ return extractString(args, key, null); }
  
  /**
   * Extract string string.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the string
   */
  public static String extractString(Intent args, String key, String def){ return args != null && args.getExtras() != null ? extractString(args.getExtras(), key, def) : def; }
  
  /**
   * Extract string string.
   *
   * @param args the args
   * @param key  the key
   * @return the string
   */
  public static String extractString(Bundle args, String key){ return extractString(args, key, null); }
  
  /**
   * Extract string string.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the string
   */
  public static String extractString(Bundle args, String key, String def){
    try{
      if(args != null && args.containsKey(key) && args.get(key) != null){
        if(args.get(key) instanceof String) return chkNull(args.getString(key), def);
        else return chkNull(args.get(key).toString(), def);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract int integer.
   *
   * @param args the args
   * @param key  the key
   * @return the integer
   */
  public static Integer extractInt(Intent args, String key){ return extractInt(args, key, null); }
  
  /**
   * Extract int integer.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the integer
   */
  public static Integer extractInt(Intent args, String key, Integer def){ return args != null && args.getExtras() != null ? extractInt(args.getExtras(), key, def) : def; }
  
  /**
   * Extract int integer.
   *
   * @param args the args
   * @param key  the key
   * @return the integer
   */
  public static Integer extractInt(Bundle args, String key){ return extractInt(args, key, null); }
  
  /**
   * Extract int integer.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the integer
   */
  public static Integer extractInt(Bundle args, String key, Integer def){
    try{
      if(args != null && args.containsKey(key) && args.get(key) != null){
        if(args.get(key) instanceof Integer) return chkNull(args.getInt(key), def);
        else if(args.get(key) instanceof Long) return chkNull((int) args.getLong(key), def);
        else if(args.get(key) instanceof Double) return chkNull((int) args.getDouble(key), def);
        else return chkNull(Integer.parseInt(args.get(key).toString()), def);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract long long.
   *
   * @param args the args
   * @param key  the key
   * @return the long
   */
  public static Long extractLong(Intent args, String key){ return extractLong(args, key, null); }
  
  /**
   * Extract long long.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the long
   */
  public static Long extractLong(Intent args, String key, Long def){ return args != null && args.getExtras() != null ? extractLong(args.getExtras(), key, def) : def; }
  
  /**
   * Extract long long.
   *
   * @param args the args
   * @param key  the key
   * @return the long
   */
  public static Long extractLong(Bundle args, String key){ return extractLong(args, key, null); }
  
  /**
   * Extract long long.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the long
   */
  public static Long extractLong(Bundle args, String key, Long def){
    try{
      if(args != null && args.containsKey(key) && args.get(key) != null){
        if(args.get(key) instanceof Long) return chkNull(args.getLong(key), def);
        else if(args.get(key) instanceof Integer) return chkNull((long) args.getInt(key), def);
        else if(args.get(key) instanceof Double) return chkNull((long) args.getDouble(key), def);
        else return chkNull(Long.parseLong(args.get(key).toString()), def);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract float float.
   *
   * @param args the args
   * @param key  the key
   * @return the float
   */
  public static Float extractFloat(Intent args, String key){ return extractFloat(args, key, null); }
  
  /**
   * Extract float float.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the float
   */
  public static Float extractFloat(Intent args, String key, Float def){ return args != null && args.getExtras() != null ? extractFloat(args.getExtras(), key, def) : def; }
  
  /**
   * Extract float float.
   *
   * @param args the args
   * @param key  the key
   * @return the float
   */
  public static Float extractFloat(Bundle args, String key){ return extractFloat(args, key, null); }
  
  /**
   * Extract float float.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the float
   */
  public static Float extractFloat(Bundle args, String key, Float def){
    try{
      if(args != null && args.containsKey(key) && args.get(key) != null){
        if(args.get(key) instanceof Float) return chkNull((float) args.get(key), def);
        else if(args.get(key) instanceof Double) return chkNull((float) args.getDouble(key), def);
        else if(args.get(key) instanceof Long) return chkNull((float) args.getLong(key), def);
        else if(args.get(key) instanceof Integer) return chkNull((float) args.getInt(key), def);
        else return chkNull(Float.parseFloat(args.get(key).toString()), def);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extracts double from Intent.
   *
   * @param args the args
   * @param key  the key
   * @return the extracted double value
   */
  public static Double extractDouble(Intent args, String key){ return extractDouble(args, key, null); }
  
  /**
   * Extract double double.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the double
   */
  public static Double extractDouble(Intent args, String key, Double def){ return args != null && args.getExtras() != null ? extractDouble(args.getExtras(), key, def) : def; }
  
  /**
   * Extract double double.
   *
   * @param args the args
   * @param key  the key
   * @return the double
   */
  public static Double extractDouble(Bundle args, String key){ return extractDouble(args, key, null); }
  
  /**
   * Extract double double.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the double
   */
  public static Double extractDouble(Bundle args, String key, Double def){
    try{
      if(args != null && args.containsKey(key) && args.get(key) != null){
        if(args.get(key) instanceof Double) return chkNull(args.getDouble(key), def);
        else if(args.get(key) instanceof Long) return chkNull((double) args.getLong(key), def);
        else if(args.get(key) instanceof Integer) return chkNull((double) args.getInt(key), def);
        else return chkNull(Double.parseDouble(args.get(key).toString()), def);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract boolean boolean.
   *
   * @param args the args
   * @param key  the key
   * @return the boolean
   */
  public static Boolean extractBoolean(Intent args, String key){ return extractBoolean(args, key, null); }
  
  /**
   * Extract boolean boolean.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the boolean
   */
  public static Boolean extractBoolean(Intent args, String key, Boolean def){ return args != null && args.getExtras() != null ? extractBoolean(args.getExtras(), key, def) : def; }
  
  /**
   * Extract boolean boolean.
   *
   * @param args the args
   * @param key  the key
   * @return the boolean
   */
  public static Boolean extractBoolean(Bundle args, String key){ return extractBoolean(args, key, null); }
  
  /**
   * Extract boolean boolean.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the boolean
   */
  public static Boolean extractBoolean(Bundle args, String key, Boolean def){
    try{
      if(args != null && args.containsKey(key) && args.get(key) != null){
        if(args.get(key) instanceof Boolean) return chkNull(args.getBoolean(key), def);
        else return chkNull(Boolean.parseBoolean(args.get(key).toString()), def);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Extract serializable serializable.
   *
   * @param args the args
   * @param cls  the cls
   * @return the serializable
   */
  public static Serializable extractSerializable(Intent args, Class cls){ return extractSerializable(args, cls, null); }
  
  /**
   * Extract serializable serializable.
   *
   * @param args the args
   * @param cls  the cls
   * @param def  the def
   * @return the serializable
   */
  public static Serializable extractSerializable(Intent args, Class cls, Serializable def){ return args != null && args.getExtras() != null ? extractSerializable(args.getExtras(), cls, def) : def; }
  
  /**
   * Extract serializable serializable.
   *
   * @param args the args
   * @param cls  the cls
   * @return the serializable
   */
  public static Serializable extractSerializable(Bundle args, Class cls){ return extractSerializable(args, cls, null); }
  
  /**
   * Extract serializable serializable.
   *
   * @param args the args
   * @param cls  the cls
   * @param def  the def
   * @return the serializable
   */
  public static Serializable extractSerializable(Bundle args, Class cls, Serializable def){
    final String key = cls.getSimpleName();
    return args != null && args.containsKey(key) && args.get(key) != null && args.get(key) instanceof Serializable && args.get(key).getClass() == cls ? args.getSerializable(key) : def;
  }
  
  /**
   * Extract serializable serializable.
   *
   * @param args the args
   * @param cls  the class
   * @param key  the key
   * @param def  the def
   * @return the serializable
   */
  public static Serializable extractSerializable(Bundle args, Class cls, String key, Serializable def){
    return args != null && args.containsKey(key) && args.get(key) != null && args.get(key) instanceof Serializable && args.get(key).getClass() == cls ? args.getSerializable(key) : def;
  }
  
  public static ArrayList<String> extractStringArrayList(Intent args, String key){ return extractStringArrayList(args, key, null); }
  
  /**
   * Extract string string.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the string
   */
  public static ArrayList<String> extractStringArrayList(Intent args, String key, ArrayList<String> def){ return args != null && args.getExtras() != null ? extractStringArrayList(args.getExtras(), key, def) : def; }
  
  /**
   * Extract string string.
   *
   * @param args the args
   * @param key  the key
   * @return the string
   */
  public static ArrayList<String> extractStringArrayList(Bundle args, String key){ return extractStringArrayList(args, key, null); }
  
  /**
   * Extract string string.
   *
   * @param args the args
   * @param key  the key
   * @param def  the def
   * @return the string
   */
  public static ArrayList<String> extractStringArrayList(Bundle args, String key, ArrayList<String> def){
    try{
      if(args != null && args.containsKey(key) && args.get(key) != null){
        if(args.get(key) instanceof ArrayList<?>)
          return args.getStringArrayList(key).size() > 0 ? args.getStringArrayList(key) : def;
        else return def;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return def;
  }
  
  /**
   * Log in file.
   *
   * @param text the text
   */
  public static void logInFile(final Context context, final String text){
    Log.e(LogFileUtilityHHD.APP_TAG, text);
  }
  
  /**
   * Log in file.
   *
   * @param txt the text
   */
  
  public static void logInFile(final Context context, final String sessionType, final String txt){ logInFile(context, sessionType, "", txt); }
  
  public static void logInFile(final Context context, final String sessionType, String className, final String txt){
    if(!isDebugApp && !SharedPrefManager.getIsAllowFileLogs()){
      //showLog("Log", txt);
      return;
    }
    if(context != null && isNonEmpty(txt)){
      new AsyncTask<Void, Void, Void>(){
        @Override
        protected Void doInBackground(Void... voids){
          final String text = /*txt + " : " + */new SimpleDateFormat(DATE_TIME_FORMAT_MILI).format(new Date(System.currentTimeMillis())) + " : " + txt;
          final File logFile = new File(isUseInAppStorage ? context.getExternalCacheDir() : Environment.getExternalStorageDirectory(), context.getString(R.string.app_name).replaceAll("(-|\\s)+", "").trim() + "_" + new SimpleDateFormat(DATE_FORMAT_YY).format(new Date(System.currentTimeMillis())) + (isNonEmpty(sessionType) ? "_" + chkNull(sessionType, "") : "") + "_log.txt");
          //showLog("logFile", logFile.exists() + "" + logFile.getAbsolutePath());
          try(BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true))){
            //BufferedWriter for performance, true to set append to file flag
            //buf.append("-----------------------------------\n");
            buf.append(text);
            buf.newLine();
          }
          catch(IOException e){ e.printStackTrace(); }
          return null;
        }
      }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
  }

  public static void writeReaderLog(final Context context, final String folderName, final String fileName, final String tag, final String txt){
    if (context == null || !(context instanceof CommonActivity)) return;
    final CommonActivity activity = (CommonActivity) context;
    if (activity == null || activity.isFinishing()) return;

    if(context != null && isNonEmpty(txt)){
      new AsyncTask<Void, Void, Void>(){
        @Override
        protected Void doInBackground(Void... voids){
          final File baseDir = activity.getBaseDirectory(isNonEmpty(folderName) ? folderName + "_Logs" : "Logs");
          // Safely handle missing directory
          if (baseDir == null) return null;
          if (!baseDir.exists()) {
            baseDir.mkdirs();
          }

          String filePath = "";

          File[] existingFiles = baseDir.listFiles();
          int size = (existingFiles != null) ? existingFiles.length : 0;

          if (size == 0) {
            File f = new File(baseDir, fileName + "-" + fileCount + ".txt");
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            filePath = f.getAbsolutePath();
          }
          else if (size > 0 && size < fileCount) {
            File file = new File(baseDir, fileName + "-" + (fileCount - (size - 1)) + ".txt");
            if (file.exists() && file.length() >= maxFileSize) {
              File f = new File(baseDir, fileName + "-" + (fileCount - size) + ".txt");
              if (!f.exists()) {
                  try {
                      f.createNewFile();
                  } catch (IOException e) {
                      throw new RuntimeException(e);
                  }
              }
              filePath = f.getAbsolutePath();
            } else {
              filePath = file.getAbsolutePath();
            }
          }
          else if (size >= fileCount) {
            final File file1 = new File(baseDir, fileName + "-1" + ".txt");
            if (file1.exists() && file1.length() >= maxFileSize) {
              // Safely find and delete the oldest file (fileCount)
              File[] targetToDelete = baseDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                  return name.contains("-" + fileCount + ".");
                }
              });
              if (targetToDelete != null && targetToDelete.length > 0) {
                targetToDelete[0].delete();
              }

              // Re-fetch remaining files to sort and shift safely
              File[] files = baseDir.listFiles();
              if (files != null) {
                Arrays.sort(files, new Comparator<File>() {
                  public int compare(File f1, File f2) {
                    return f2.getName().compareTo(f1.getName());
                  }
                });

                for (File file : files) {
                  try {
                    String name = file.getName();
                    int count = Integer.parseInt(name.substring(name.lastIndexOf('-') + 1, name.lastIndexOf('.')));
                    file.renameTo(new File(baseDir, fileName + "-" + (count + 1) + ".txt"));
                  } catch (Exception e) {
                    /*e.printStackTrace();*/
                  }
                }
              }
              filePath = file1.getAbsolutePath();
            } else {
              filePath = file1.getAbsolutePath();
            }
          }

          final String text = /*txt + " : " + */new SimpleDateFormat(DATE_TIME_FORMAT_MILI).format(new Date(System.currentTimeMillis())) + " : " + txt;
          final File logFile = new File(filePath) ;
          //showLog("logFile", logFile.exists() + "" + logFile.getAbsolutePath());
          try(BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true))){
            //BufferedWriter for performance, true to set append to file flag
            //buf.append("-----------------------------------\n");
            buf.append(text);
            buf.newLine();
          }
          catch(IOException e){ /*e.printStackTrace();*/ }
          return null;
        }
      }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
  }
  
  /**
   * Show progress dialog.
   * f
   *
   * @param context the context
   */
  public static void showProgressDialog(Context context){ showProgressDialog(context, ""); }
  
  /**
   * Show progress dialog.
   *
   * @param context      the context
   * @param messageResId the message res id
   */
  public static void showProgressDialog(Context context, @StringRes int messageResId){
    if(isNonZeroId(messageResId)) showProgressDialog(context, context.getString(messageResId));
  }
  
  /**
   * Show progress dialog.
   *
   * @param context          the context
   * @param progress_message the progress message
   */
  public static void showProgressDialog(Context context, String progress_message){
    showLog("showProgressDialog", (context != null) + "  " + progress_message);
    if(context != null && context instanceof Activity && !((Activity) context).isFinishing()){
      ((Activity) context).runOnUiThread(new Runnable(){
        @Override
        public void run(){
          if(progressDialog != null){
            if(progressDialog.isShowing()){//progressDialog.dismiss();
              //progressDialog.setMessage(chkNull(progress_message, "Please wait..."));
            }
            else{
              progressDialog.dismiss();
              progressDialog = null;
              showProgressDialog(context, progress_message);
            }
          }
          else{
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.ProgressDialog);
            final int wid = context.getResources().getDimensionPixelSize(R.dimen.dp_40);
            ImageView logo = new ImageView(context);
            logo.setLayoutParams(new ViewGroup.LayoutParams(wid, wid));
            Glide.with(context).load(R.drawable.loader).into(logo);
            //logo.setImageResource(R.drawable.circular_progress_bar);
            builder.setView(logo);
            progressDialog = builder.create();
            //progressDialog = new ProgressDialog(context);
            //progressDialog.setContentView();
            //progressDialog.setMessage(chkNull(progress_message, "Please wait..."));
            //progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
          }
        }
      });
    }
  }
  
  /**
   * Hide progress dialog.
   */
  public static void hideProgressDialog(Context context){
    showLog("hideProgressDialog", (context != null) + "  ");
    if(context != null && context instanceof Activity && !((Activity) context).isFinishing() && progressDialog != null){
      ((Activity) context).runOnUiThread(new Runnable(){
        @Override
        public void run(){
          if(progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
          progressDialog = null;
        }
      });
    }
  }
  
  //Enums
  
  //  /**
  //   * Gets left zero replaced string.
  //   *
  //   * @param barcode the barcode
  //   * @return the left zero replaced string
  //   */
  //  public static String getLeftZeroReplacedString(String barcode){
  //    return chkNull(barcode, "").replaceFirst("^0+(?!$)", "");
  //  }
  
  public static String formatDoubleStr2Decimals(String str){
    if(isNullOrEmpty(str)) return str;
    str = str.trim();
    if(str.matches("([0-9]+\\.[0-9]{2})")) return str;
    if(str.matches("([0-9]+\\.[0-9]{1})")) return str + "0";
    if(str.matches("([0-9]+\\.)")) return str + "00";
    if(str.matches("([0-9]+)")) return str + ".00";
    if(str.matches("(\\.[0-9]{2})")) return "0" + str;
    if(str.matches("(\\.[0-9]{1})")) return "0" + str + "0";
    return str;
  }
  
  /**
   * Gets left zero replaced string.
   *
   * @param barcode the barcode
   * @return the left zero replaced string
   */
  public static String getLeftZeroReplacedString(String barcode){
    return getLeftZeroReplacedString(null, barcode);
  }
  
  public static String getLeftZeroReplacedString(Context context, String barcode){
    return getLeftZeroReplacedString(context != null && context instanceof CommonActivity ? (CommonActivity) context : null, barcode);
  }
  
  public static String getLeftZeroReplacedString(CommonActivity context, String barcode){
    final boolean isStdBarcode = context == null || context.isFinishing() ? EPCEncoderDecoder.isStandardValidBarcode(barcode) : context.epcEncoderDecoder.isValidStdBarcode(barcode);
    if((isStdBarcode && SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_LEADING_ZERO_FOR_STD_BARCODE, isIsAllowLeadingZeroForStdBarcode)) || (!isStdBarcode && SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_LEADING_ZERO_FOR_NON_STD_BARCODE, isIsAllowLeadingZeroForNonStdBarcode)))
      return chkNull(barcode, "");
    return chkNull(barcode, "").replaceFirst("^0+(?!$)", "");
  }
  
  //Enums
  
  public static <T extends Parcelable> T deepClone(T objectToClone){
    Parcel parcel = null;
    try{
      parcel = Parcel.obtain();
      parcel.writeParcelable(objectToClone, 0);
      parcel.setDataPosition(0);
      return parcel.readParcelable(objectToClone.getClass().getClassLoader());
    }
    finally{
      if(parcel != null){
        parcel.recycle();
      }
    }
  }
  
  /**
   * The enum Device type.
   */
  public enum DeviceType{
    OTHER(0), ZEBRA(1), CHAINWAY(2), SEUIC(3), ALPS(4), HONEYWELL(5), CIPHERLAB(6);
    
    private final int value;
    
    /**
     * Instantiates a new Device type.
     *
     * @param newValue the new value
     */
    DeviceType(final int newValue){ value = newValue; }
    
    /**
     * Get device type.
     *
     * @param value the value
     * @return the device type
     */
    public static DeviceType get(int value){
      return DeviceType.values()[value];
    }
    
    /**
     * Get value int.
     *
     * @return the int
     */
    public int getValue(){ return value; }
  }
  
  /**
   * The enum Device type.
   */
  public enum SensorType{
    OTHER(0), ZEBRA(1), CHAINWAY(2), SEUIC(3), ALPS(4);
    
    private final int value;
    
    /**
     * Instantiates a new Device type.
     *
     * @param newValue the new value
     */
    SensorType(final int newValue){ value = newValue; }
    
    /**
     * Get device type.
     *
     * @param value the value
     * @return the device type
     */
    public static SensorType get(int value){
      return SensorType.values()[value];
    }
    
    /**
     * Get value int.
     *
     * @return the int
     */
    public int getValue(){ return value; }
  }
  
  /**
   * The enum Session type.
   */
  public enum SessionType{
    OTHER(0), ENCODING(1), INVENTORY(2), STOCK_CORRECTION(3), SEARCH(4), OMNICHANNEL(5), INWARD(6), MOVEMENT(7), REPLENISHMENT(8), OUTWARD(9), SCAN(10), VERIFY_ENCODING(11), BRAND_INVENTORY(12), ADD_INVENTORY(13), SEARCH_LIST(14), SEARCH_UNENCODED(15), OUTWARD_PICK(16), SEARCH_AGEING(17), DECODING(18), SEARCH_ASSORTMENT(19), SEARCH_ALIEN(20), SEARCH_FIFO(21), SEARCH_FILE(22), OUTWARD_TOTE(23), OFF_RANGE(24), ENCODING_THAN(25), INWARD_TOTE(26), SER_EXCEL(27), FILTER_INVENTORY(28),;
    
    private final int value;
    
    /**
     * Instantiates a new Session type.
     *
     * @param newValue the new value
     */
    SessionType(final int newValue){ value = newValue; }
    
    /**
     * Get session type.
     *
     * @param value the value
     * @return the session type
     */
    public static SessionType get(int value){ return SessionType.values()[value]; }
    
    /**
     * Get max value int.
     *
     * @return the int
     */
    public static int getMaxValue(){ return SessionType.values().length - 1; }
    
    /**
     * Get value int.
     *
     * @return the int
     */
    public int getValue(){ return value; }
  }
  
  /**
   * The enum Session action.
   */
  public enum SessionAction{
    OTHER(0), INVENTORY(1), ENCODE(2), SEARCH(3), PICK(4), DECODE(5);
    
    private final int value;
    
    /**
     * Instantiates a new Session action.
     *
     * @param newValue the new value
     */
    SessionAction(final int newValue){ value = newValue; }
    
    /**
     * Get session action.
     *
     * @param value the value
     * @return the session action
     */
    public static SessionAction get(int value){
      
      return SessionAction.values()[value];
    }
    
    /**
     * Get value int.
     *
     * @return the int
     */
    public int getValue(){ return value; }
  }
  
  /**
   * The enum Tag type.
   */
  public enum TagType{//1-Standard 2-Non Standard 3-unencoded
    Unknown(0), Standard(1), NonStandard(2), NonEncoded(3);
    
    private final int value;
    
    /**
     * Instantiates a new Tag type.
     *
     * @param newValue the new value
     */
    TagType(final int newValue){ value = newValue; }
    
    /**
     * Get tag type.
     *
     * @param value the value
     * @return the tag type
     */
    public static TagType get(int value){
      showLog("getTagType value", "" + value);
      showLog("getTagType return", "" + TagType.values()[value].toString());
      return TagType.values()[value];
    }
  }
  
  /**
   * The enum Replenishment type.
   */
  public enum ReplenishmentType{
    BOTH(0), STATIC(1), DYNAMIC(2);
    private final int value;
    
    /**
     * Instantiates a new Replenishment type.
     *
     * @param newValue the new value
     */
    ReplenishmentType(final int newValue){ value = newValue; }
    
    /**
     * Get replenishment type.
     *
     * @param value the value
     * @return the replenishment type
     */
    public static ReplenishmentType get(int value){
      showLog("getReplenishmentType value", "" + value);
      showLog("getReplenishmentType return", "" + ReplenishmentType.values()[value].toString());
      return ReplenishmentType.values()[value];
    }
  }
  
  /**
   * The enum Omnichannel type.
   */
  public enum OmnichannelType{
    BOTH(0), ORDER(1), EAN(2);
    private final int value;
    
    /**
     * Instantiates a new Omnichannel type.
     *
     * @param newValue the new value
     */
    OmnichannelType(final int newValue){ value = newValue; }
    
    /**
     * Get omnichannel type.
     *
     * @param value the value
     * @return the omnichannel type
     */
    public static OmnichannelType get(int value){
      showLog("getOmnichannelType value", "" + value);
      showLog("getOmnichannelType return", "" + OmnichannelType.values()[value].toString());
      return OmnichannelType.values()[value];
    }
  }
  
  /**
   * The enum Unencoded Search type.
   */
  public enum UnencodedSearchType{
    BOTH(0), ONLINE(1), OFFLINE(2);
    private final int value;
    
    /**
     * Instantiates a new unencoded search type.
     *
     * @param newValue the new value
     */
    UnencodedSearchType(final int newValue){ value = newValue; }
    
    /**
     * Get unencoded search type.
     *
     * @param value the value
     * @return the unencoded search type
     */
    public static UnencodedSearchType get(int value){
      showLog("getUnencodedSearchType value", "" + value);
      showLog("getUnencodedSearchType return", "" + UnencodedSearchType.values()[value].toString());
      return UnencodedSearchType.values()[value];
    }
  }
  
  /**
   * The enum Product type.
   */
  public enum ProductType{
    OTHER(0), CLOTH(1), BOOK(2);
    private final int value;
    
    /**
     * Instantiates a new Product type.
     *
     * @param newValue the new value
     */
    ProductType(final int newValue){ value = newValue; }
    
    /**
     * Get product type.
     *
     * @param value the value
     * @return the product type
     */
    public static ProductType get(int value){
      showLog("getProductType value", "" + value);
      showLog("getProductType return", "" + ProductType.values()[value].toString());
      return ProductType.values()[value];
    }
  }
  
  public enum EncodeVerifyStatus{
    UNUSED(0), EPC_WRONG(1), PENDING(2), RE_ENCODED(3), VERIFIED_DECODED(4), VERIFIED_SUCCESS(5);
    private final int value;
    
    /**
     * Instantiates a new Product type.
     *
     * @param newValue the new value
     */
    EncodeVerifyStatus(final int newValue){ value = newValue; }
    
    /**
     * Get product type.
     *
     * @param value the value
     * @return the product type
     */
    public static EncodeVerifyStatus get(int value){
      showLog("getEncodeVerifyStatus value", "" + value);
      showLog("getEncodeVerifyStatus return", "" + EncodeVerifyStatus.values()[value].toString());
      return EncodeVerifyStatus.values()[value];
    }
  }
  
  /**
   * The String trim json deserializer.
   */
  static class StringTrimJsonDeserializer implements JsonDeserializer<String>{
    
    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
      final String value = json.getAsString();
      return value == null ? null : value.trim();
    }
  }
  
}
 
