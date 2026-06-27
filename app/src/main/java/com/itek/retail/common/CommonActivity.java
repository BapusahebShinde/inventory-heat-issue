package com.itek.retail.common;

import static com.itek.retail.apis.ParamConstants.OLD_ACCESS_PASSWORDS;
import static com.itek.retail.apis.ParamConstants.OLD_ACCESS_PWDS;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.REQUEST_PERMISSION_BLUETOOTH;
import static com.itek.retail.common.AppCommonMethods.REQUEST_PERMISSION_CAMERA;
import static com.itek.retail.common.AppCommonMethods.REQUEST_PERMISSION_IMEI;
import static com.itek.retail.common.AppCommonMethods.REQUEST_PERMISSION_LOCATION;
import static com.itek.retail.common.AppCommonMethods.REQUEST_PERMISSION_NOTIFICATION;
import static com.itek.retail.common.AppCommonMethods.REQUEST_PERMISSION_STORAGE;
import static com.itek.retail.common.AppCommonMethods.SERVER_DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.errorBeep;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.getSampleJSON;
import static com.itek.retail.common.AppCommonMethods.isDCApp;
import static com.itek.retail.common.AppCommonMethods.isHandleNotificationDataFromIntent;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNonZeroId;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isSetUserMgmt;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;
import static com.itek.retail.common.AppCommonMethods.isValidUrl;
import static com.itek.retail.common.AppCommonMethods.successBeep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tscdll.TSCActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.itek.retail.BuildConfig;
import com.itek.retail.R;
import com.itek.retail.adapter.ImagesListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.RFIDSessionDao;
import com.itek.retail.model.AuditTrailsLog;
import com.itek.retail.model.LabelValues;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.Notification;
import com.itek.retail.model.PrintData;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.SearchLog;
import com.itek.retail.receiver.AppBroadcastReceiver;
import com.itek.retail.sgtin.EPCEncoderDecoder;
import com.itek.retail.ui.actionmenu.ActionMenuCompareFragment;
import com.itek.retail.ui.actionmenu.ActionMenuMsgFragment;
import com.itek.retail.ui.actionmenu.ActionMenuNotifyFragment;
import com.itek.retail.ui.actionmenu.ActionMenuSearchFragment;
import com.itek.retail.ui.customviews.InputView;
import com.itek.retail.ui.customviews.photoview.PhotoView;
import com.itek.retail.ui.encoding.EncodingScanScanWriteFragment;
import com.itek.retail.ui.encoding.EncodingStartFragment;
import com.itek.retail.ui.home.HomeFragment;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.landing.LandingActivity;
import com.itek.retail.ui.search.productsearch.ProductSearchDetailsFragment;
import com.itek.retail.ui.than.ThanCuttingFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Common activity is a parent activity
 * which is extended by all activities throughout the app
 * contains common constants/methods and common code implementation
 */
public class CommonActivity extends AppCompatActivity{
  
  private final static int APP_UPDATE_REQUEST_CODE = 2000;
  public static ActivityResultLauncher<Intent> locationResultLauncher;
  private static ActivityResultLauncher<Intent> appPermissionsResultLauncher;
  private static ActivityResultLauncher<Intent> bluetoothResultLauncher;
  public final boolean isDebugApp = AppCommonMethods.isDebugApp;
  public final boolean isUseInAppStorage = AppCommonMethods.isUseInAppStorage;
  public final boolean isUseDeviceIDForIMEI = AppCommonMethods.isUseDeviceIDForIMEI;
  public final boolean isUseBluetoothScanConnect = AppCommonMethods.isUseBluetoothScanConnect;
  public final boolean isUseNotificationPermission = AppCommonMethods.isUseNotificationPermission;
  public AlertDialog customAlertDialog;
  public String currentFragmentClassName = "";
  public boolean isTablet = false;
  public boolean isLandscape = false;
  public EPCEncoderDecoder epcEncoderDecoder;
  public PrintData savedPrintValues = null;
  public boolean isPrinting = false;
  protected Menu menu;
  private AppBroadcastReceiver appBroadcastReceiver = null;
  private int appUpdateType = -1;
  private int colorPrimaryDarkFromTheme = 0;
  private int colorPrimaryFromTheme = 0;
  private int colorAccentFromTheme = 0;
  private TSCActivity tscDll = new TSCActivity();//BT Printer
  private ExecutorService executor = Executors.newSingleThreadExecutor();
  private ArrayList<String> listSubscribedTopicsFireBase = new ArrayList<>(0);
  
  /**
   * Dp 2 px int.
   *
   * @param dp the dp
   * @return the int
   */
  public int dp2px(int dp){
    return AppCommonMethods.dp2px(dp);
  }
  
  /**
   * Sp 2 px int.
   *
   * @param sp the sp
   * @return the int
   */
  public int sp2px(int sp){
    return AppCommonMethods.sp2px(sp);
  }
  
  /**
   * Detach all action menu fragments.
   */
  public void detachAllActionMenuFragments(){
    List<MenuModel> listActionMenus = AppDatabase.getMenuDao(CommonActivity.this).getActionMenus();
    if(isNonEmpty(listActionMenus)){
      for(MenuModel menuModel : listActionMenus)
        getSupportFragmentManager().popBackStack(menuModel.getRedirectFragment().getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
    else{
      getSupportFragmentManager().popBackStack(ActionMenuSearchFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
      getSupportFragmentManager().popBackStack(ActionMenuNotifyFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
      getSupportFragmentManager().popBackStack(ActionMenuMsgFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
      getSupportFragmentManager().popBackStack(ActionMenuCompareFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
  }
  
  /**
   * Select menu item.
   *
   * @param id         the id
   * @param isSelected the is selected
   */
  public void selectMenuItem(int id, boolean isSelected){
    if(menu != null && id > 0 && menu.findItem(id) != null){
      final MenuItem selMenuItem = menu.findItem(id);
      if(selMenuItem.isEnabled()){
        if(selMenuItem.getActionView() != null){
          final ImageView imgMenuLogo = selMenuItem.getActionView().findViewById(R.id.imgMenuLogo);
          if(imgMenuLogo != null)// && !isValidUrl(selMenuItem.getImageUrl()))
            imgMenuLogo.setColorFilter(isSelected ? colorPrimaryDarkFromTheme : getResources().getColor(R.color.transparent), PorterDuff.Mode.SRC_ATOP);
        }
        else{
          switch(id){
            case R.id.act_ser:
              selMenuItem.setIcon(isSelected ? R.drawable.ic_act_ser_sel : R.drawable.ic_act_ser);
              break;
            case R.id.act_msg:
              selMenuItem.setIcon(isSelected ? R.drawable.ic_act_msg_sel : R.drawable.ic_act_msg);
              break;
            case R.id.act_notify:
              selMenuItem.setIcon(AppDatabase.getNotificationDao(this).isReadPending(isSetUserMgmt ? SharedPrefManager.getUserID() : "") ? isSelected ? R.drawable.ic_act_notify_dot_sel : R.drawable.ic_act_notify_dot : isSelected ? R.drawable.ic_act_notify_sel : R.drawable.ic_act_notify);
              break;
            default:
              break;
          }
        }
      }
    }
  }
  
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    isTablet = getResources().getBoolean(R.bool.isTablet);
    //setRequestedOrientation(isTablet ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    
    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
      AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    else{
      appPermissionsResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
        @Override
        public void onActivityResult(ActivityResult result){
          checkPermissions(CommonActivity.this instanceof LandingActivity ? 0 : REQUEST_PERMISSION_LOCATION);
        }
      });
      locationResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
        @Override
        public void onActivityResult(ActivityResult result){
          AppCommonMethods.checkLocationOn(CommonActivity.this);
        }
      });
    }
    
    SharedPrefManager.init(this);
    String stringColor = SharedPrefManager.getString(ParamConstants.THEME_COLOR, "");
    if(isNonEmpty(stringColor)){
      AppCommonMethods.showLog("stringColor", stringColor);
      int color = Color.parseColor(stringColor.contains("#") ? stringColor : "#" + stringColor);
      int rgb = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3;
      AppCommonMethods.showLog("color", rgb + "_" + (rgb <= 210));
      if(rgb > 210) ;//do something different to calculate related style
      else
        getTheme().applyStyle(getResources().getIdentifier("T_" + stringColor.replaceFirst("\\#", "").toLowerCase().trim(), "style", getPackageName()), true);
    }
    
    epcEncoderDecoder = (this instanceof MainActivity) ? EPCEncoderDecoder.getInstance() : null;
    isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    //Set/Save Install/Update Date & Version on Install/Update
    if(!SharedPrefManager.getAppVersion().equalsIgnoreCase(BuildConfig.VERSION_NAME)){
      if(!BuildConfig.IS_DIAGNOSTIC_BUILD) generateFireBaseMessagingId();
      if(isNonEmpty(SharedPrefManager.getUpdateAPKPath())){
        try{
          File apkFile = new File(SharedPrefManager.getUpdateAPKPath());
          if(apkFile.exists()) apkFile.delete();
        }
        catch(Exception e){ e.printStackTrace(); }
        SharedPrefManager.setUpdateAPKPath("");
      }
      SharedPrefManager.setAppVersion(BuildConfig.VERSION_NAME);
      SharedPrefManager.setInstallDate(new SimpleDateFormat(DATE_TIME_FORMAT).format(Calendar.getInstance().getTime()));
    }
    else if(!BuildConfig.IS_DIAGNOSTIC_BUILD && isNullOrEmpty(SharedPrefManager.getFirebaseToken())) generateFireBaseMessagingId();
    
    checkPermissions(this instanceof LandingActivity ? 0 : REQUEST_PERMISSION_LOCATION);
    if(isUseDeviceIDForIMEI || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
      getUniqueIMEI();
      getMacAddress();
    }
    AppCommonMethods.getLocation(this);
    
    getSupportFragmentManager().addOnBackStackChangedListener(() -> {
      final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
      if(fragment != null){
        currentFragmentClassName = fragment.getClass().getSimpleName();
        showLog("current fragment:", currentFragmentClassName);
        if(this instanceof MainActivity){
          ((MainActivity) this).hideNavItems(fragment.getClass().equals(HomeFragment.class));
        }
      }
      else currentFragmentClassName = "";
    });
    
    appBroadcastReceiver = new AppBroadcastReceiver();
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
    intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
    intentFilter.addAction(Intent.ACTION_TIME_TICK);
    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
    intentFilter.addAction(LocationManager.MODE_CHANGED_ACTION);
    registerReceiver(appBroadcastReceiver, intentFilter, "", new Handler());
    if(this instanceof MainActivity || isDebugApp) registerBluetoothEvent();
    
    //code to handle notification data (upon intent click) (not needed now since handled from web)
    if(SharedPrefManager.getBoolean(ParamConstants.IS_HANDLE_NOTIFICATION_DATA_VIA_INTENT, isHandleNotificationDataFromIntent) && getIntent().getExtras() != null){
      try{
        final Bundle extras = getIntent().getExtras();
        AppCommonMethods.showLog("onMessageReceived_getIntent", extras.toString());
        final JSONObject notiData = AppCommonMethods.getJsonFromBundle(extras);
        showLog("onMessageReceived_notiData", notiData.toString());
        Notification notification = getGSON().fromJson(notiData.toString(), Notification.class);
        //temp/static bug fix(AB#4276)
        if(notification.getTypeId().equalsIgnoreCase(AppConstants.MENU_CODE_MOV_REPLENISH))
          notification.setTypeId(AppConstants.MENU_CODE_REPLENISH);
        notification.setReceivedOn(new SimpleDateFormat(DATE_TIME_FORMAT).format(new Date()));
        notification.setUserId(SharedPrefManager.getUserID());
        Calendar cc = Calendar.getInstance();
        cc.add(Calendar.HOUR_OF_DAY, extractInt(notiData, ParamConstants.VALID_TILL, 48));
        notification.setValidTill(new SimpleDateFormat(SERVER_DATE_TIME_FORMAT).format(cc.getTime()));
        notification.setRead(false);
        showLog("notification time", notification.date);
        AppDatabase.getNotificationDao(this).insert(notification);
        if(SharedPrefManager.getIsLoggedIn() && this instanceof MainActivity)
          ((MainActivity) this).blink();
      }
      catch(Exception e){ e.printStackTrace(); }
    }
    
    //code to set notification topics
    ArrayList<String> topics = SharedPrefManager.getStringArrayList(AppConstants.FIREBASE_TOPICS);
    if(isNonEmpty(topics)){
      listSubscribedTopicsFireBase.clear();
      listSubscribedTopicsFireBase.addAll(topics);
    }
    
    final String name = this.getClass().getSimpleName().trim();
    //LogFileUtility.writeLog(this);
    LogFileUtilityHHD.writeLog(this);
    showLog(name, "\n_________________________" + name + "__________________________________________\n");
  }
  
  @Override
  protected void onNewIntent(Intent intent){
    super.onNewIntent(intent);
  }
  
  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState){
    super.onPostCreate(savedInstanceState);
    TypedValue tv = new TypedValue();
    colorPrimaryDarkFromTheme = getTheme().resolveAttribute(R.attr.colorPrimaryDark, tv, true) ? tv.data : ContextCompat.getColor(this, R.color.colorPrimaryDarkDef);
    showLog("colorPrimaryDarkFromTheme", "" + colorPrimaryDarkFromTheme + "_" + Integer.toHexString(colorPrimaryDarkFromTheme));
    TypedValue tv1 = new TypedValue();
    colorPrimaryFromTheme = getTheme().resolveAttribute(R.attr.colorPrimary, tv1, true) ? tv1.data : ContextCompat.getColor(this, R.color.colorPrimaryDef);
    showLog("colorPrimaryFromTheme", "" + colorPrimaryFromTheme + "_" + Integer.toHexString(colorPrimaryFromTheme));
    TypedValue tv2 = new TypedValue();
    colorAccentFromTheme = getTheme().resolveAttribute(R.attr.colorAccent, tv2, true) ? tv2.data : ContextCompat.getColor(this, R.color.colorAccentDef);
    showLog("colorAccentFromTheme", "" + colorAccentFromTheme + "_" + Integer.toHexString(colorAccentFromTheme));
    
    final TextClock textClock = findViewById(R.id.textClock);
    if(textClock != null)
      textClock.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_time_small, 0, 0, 0);
  }
  
  public int getColorPrimaryDarkFromTheme(){
    return colorPrimaryDarkFromTheme;
  }
  
  public int getColorPrimaryFromTheme(){
    return colorPrimaryFromTheme;
  }
  
  public int getColorAccentFromTheme(){
    return colorAccentFromTheme;
  }
  
  /**
   * Generate fire base messaging id.
   */
  private void generateFireBaseMessagingId(){
    if(BuildConfig.IS_DIAGNOSTIC_BUILD) return;
    
    FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
      if(!task.isSuccessful()){
        showLog("Fetching FCM registration token failed", task.getException().getLocalizedMessage());
        return;
      }
      
      // Get new FCM registration token
      String token = task.getResult();
      if(isNonEmpty(token)){
        SharedPrefManager.setFirebaseToken(token);
        showLog("token", "" + token);
      }
    });
  }
  
  /**
   * Subscribe to fire base topic
   */
  protected void subscribeToFirebaseTopic(final String topic){
    if(BuildConfig.IS_DIAGNOSTIC_BUILD) return;
    if(listSubscribedTopicsFireBase.contains(topic)) return;
    FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(task -> {
      String msg = "Subscribed to " + topic;
      if(!task.isSuccessful()) msg = "Subscribe failed for " + topic;
      else{
        listSubscribedTopicsFireBase.add(topic);
        SharedPrefManager.setStringArrayList(AppConstants.FIREBASE_TOPICS, listSubscribedTopicsFireBase);
      }
      showLog("subscribeToFirebaseTopic", msg);
    });
  }
  
  /**
   * unsubscribe to fire base topic
   */
  protected void unsubscribeFromFirebaseTopics(){
    if(isNullOrEmpty(listSubscribedTopicsFireBase)) return;
    List<String> listTopics = new ArrayList(0);
    listTopics.addAll(listSubscribedTopicsFireBase);
    for(String topic : listTopics) unsubscribeFromFirebaseTopic(topic);
    clearTopics();
  }
  
  protected void clearTopics(){
    SharedPrefManager.clearArrayList(AppConstants.FIREBASE_TOPICS);
    listSubscribedTopicsFireBase.clear();
  }
  
  /**
   * unsubscribe to fire base topic
   */
  protected void unsubscribeFromFirebaseTopic(final String topic){
    if(BuildConfig.IS_DIAGNOSTIC_BUILD) return;
    if(!listSubscribedTopicsFireBase.contains(topic)) return;
    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
      String msg = "unsubscribed from " + topic;
      if(!task.isSuccessful()) msg = "unsubscribe failed " + topic;
      else{
        listSubscribedTopicsFireBase.remove(topic);
        SharedPrefManager.setStringArrayList(AppConstants.FIREBASE_TOPICS, listSubscribedTopicsFireBase);
      }
      showLog("unsubscribeFromFirebaseTopic", msg);
    });
  }
  
  /**
   * Register and launch intent.
   *
   * @param intent         the intent
   * @param onResultMethod the on result method
   */
  public void registerAndLaunchIntent(final Intent intent, final View.OnClickListener onResultMethod){
    new Handler(Looper.getMainLooper()).post(() -> {
      if(CommonActivity.this != null && !CommonActivity.this.isFinishing() && intent != null)
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
          if(onResultMethod != null) onResultMethod.onClick(null);
        }).launch(intent);
    });
  }
  
  public void saveIOConfig(final JSONObject jsonResponse){ saveIOConfig(jsonResponse, "", ""); }
  
  public void saveIOConfig(final JSONObject jsonResponse, final String tripNum){ saveIOConfig(jsonResponse, tripNum, ""); }
  
  public void saveIOConfig(final JSONObject jsonResponse, final String tripNum, final String huNum){
    if(jsonResponse == null || jsonResponse.length() < 1) return;
    final JSONObject config = extractJSONObject(jsonResponse, ParamConstants.CONFIG, jsonResponse);
    final JSONObject labelConfig = extractJSONObject(config, ParamConstants.CONFIG_LABEL, extractJSONObject(config, ParamConstants.CONFIG_LABELS, config));
    final JSONObject labelSetting = extractJSONObject(config, ParamConstants.CONFIG_SETTING, extractJSONObject(config, ParamConstants.CONFIG_SETTING, config));
    if(config == null || config.length() < 1) return;
    SharedPrefManager.setString(huNum + tripNum + ParamConstants.LABEL_TRIP, extractString(labelConfig, ParamConstants.LABEL_TRIP, extractString(config, ParamConstants.LABEL_TRIP, SharedPrefManager.getString(huNum + tripNum + ParamConstants.LABEL_TRIP, SharedPrefManager.getString(tripNum + ParamConstants.LABEL_TRIP, SharedPrefManager.getString(ParamConstants.LABEL_TRIP, getString(R.string.lbl_trip)))))));
    SharedPrefManager.setString(huNum + tripNum + ParamConstants.LABEL_HU, extractString(labelConfig, ParamConstants.LABEL_HU, extractString(config, ParamConstants.LABEL_HU, SharedPrefManager.getString(huNum + tripNum + ParamConstants.LABEL_HU, SharedPrefManager.getString(tripNum + ParamConstants.LABEL_HU, SharedPrefManager.getString(ParamConstants.LABEL_HU, getString(R.string.lbl_hu)))))));
    SharedPrefManager.setString(huNum + tripNum + ParamConstants.LABEL_ARTICLE, extractString(labelConfig, ParamConstants.LABEL_ARTICLE, extractString(config, ParamConstants.LABEL_ARTICLE, SharedPrefManager.getString(huNum + tripNum + ParamConstants.LABEL_ARTICLE, SharedPrefManager.getString(tripNum + ParamConstants.LABEL_ARTICLE, SharedPrefManager.getString(ParamConstants.LABEL_ARTICLE, getString(R.string.lbl_article_no)))))));
    SharedPrefManager.setString(huNum + tripNum + ParamConstants.LABEL_SKUID, extractString(labelConfig, ParamConstants.LABEL_SKUID, extractString(config, ParamConstants.LABEL_SKUID, SharedPrefManager.getString(huNum + tripNum + ParamConstants.LABEL_SKUID, SharedPrefManager.getString(tripNum + ParamConstants.LABEL_SKUID, SharedPrefManager.getString(ParamConstants.LABEL_SKUID, getString(R.string.lbl_ean)))))));
    
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_USE_TRIP_AS_HU, extractBoolean(labelSetting, ParamConstants.IS_USE_TRIP_AS_HU, extractBoolean(config, ParamConstants.IS_USE_TRIP_AS_HU, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.IS_USE_TRIP_AS_HU, SharedPrefManager.getBoolean(tripNum + ParamConstants.IS_USE_TRIP_AS_HU, SharedPrefManager.getBoolean(ParamConstants.IS_USE_TRIP_AS_HU,AppCommonMethods.isUseTripAsHU))))));
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_ALLOW_MANUAL_TRIP_NO_ENTRY, extractBoolean(labelSetting, ParamConstants.IS_ALLOW_MANUAL_TRIP_NO_ENTRY, extractBoolean(config, ParamConstants.IS_ALLOW_MANUAL_TRIP_NO_ENTRY, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.IS_ALLOW_MANUAL_TRIP_NO_ENTRY, SharedPrefManager.getBoolean(tripNum + ParamConstants.IS_ALLOW_MANUAL_TRIP_NO_ENTRY, SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_MANUAL_TRIP_NO_ENTRY, SharedPrefManager.getBoolean(ParamConstants.IS_RFID_STORE, AppCommonMethods.isAllowManualTripCreation)))))));
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_ALLOW_MANUAL_HU_ENTRY, extractBoolean(labelSetting, ParamConstants.IS_ALLOW_MANUAL_HU_ENTRY, extractBoolean(config, ParamConstants.IS_ALLOW_MANUAL_HU_ENTRY, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.IS_ALLOW_MANUAL_HU_ENTRY, SharedPrefManager.getBoolean(tripNum + ParamConstants.IS_ALLOW_MANUAL_HU_ENTRY, SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_MANUAL_HU_ENTRY, AppCommonMethods.isAllowManualHUCreation))))));
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_TRIP_DEVICE_LOCK, extractBoolean(labelSetting, ParamConstants.IS_TRIP_DEVICE_LOCK, extractBoolean(config, ParamConstants.IS_TRIP_DEVICE_LOCK, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.IS_TRIP_DEVICE_LOCK, SharedPrefManager.getBoolean(tripNum + ParamConstants.IS_TRIP_DEVICE_LOCK, SharedPrefManager.getBoolean(ParamConstants.IS_TRIP_DEVICE_LOCK, AppCommonMethods.isTripDeviceLock))))));
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_HU_DEVICE_LOCK, extractBoolean(labelSetting, ParamConstants.IS_HU_DEVICE_LOCK, extractBoolean(config, ParamConstants.IS_HU_DEVICE_LOCK, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.IS_HU_DEVICE_LOCK, SharedPrefManager.getBoolean(tripNum + ParamConstants.IS_HU_DEVICE_LOCK, SharedPrefManager.getBoolean(ParamConstants.IS_HU_DEVICE_LOCK, AppCommonMethods.isHUDeviceLock))))));
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_API_CALL_FOR_REJECT_HU, extractBoolean(labelSetting, ParamConstants.IS_API_CALL_FOR_REJECT_HU, extractBoolean(config, ParamConstants.IS_API_CALL_FOR_REJECT_HU, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.IS_API_CALL_FOR_REJECT_HU, SharedPrefManager.getBoolean(tripNum + ParamConstants.IS_API_CALL_FOR_REJECT_HU, SharedPrefManager.getBoolean(ParamConstants.IS_API_CALL_FOR_REJECT_HU, AppCommonMethods.isAPIBasedRejectHU))))));
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_ON_DEMAND_TRIP_HU_LIST, extractBoolean(labelSetting, ParamConstants.IS_ON_DEMAND_TRIP_HU_LIST, extractBoolean(config, ParamConstants.IS_ON_DEMAND_TRIP_HU_LIST, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.IS_ON_DEMAND_TRIP_HU_LIST, SharedPrefManager.getBoolean(tripNum + ParamConstants.IS_ON_DEMAND_TRIP_HU_LIST, SharedPrefManager.getBoolean(ParamConstants.IS_ON_DEMAND_TRIP_HU_LIST, AppCommonMethods.isOnDemandTripHuList))))));
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_ALLOW_LOWER_CASE_TRIP_NUMBER, extractBoolean(labelSetting, ParamConstants.IS_ALLOW_LOWER_CASE_TRIP_NUMBER, extractBoolean(config, ParamConstants.IS_ALLOW_LOWER_CASE_TRIP_NUMBER, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.IS_ALLOW_LOWER_CASE_TRIP_NUMBER, SharedPrefManager.getBoolean(tripNum + ParamConstants.IS_ALLOW_LOWER_CASE_TRIP_NUMBER, SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_LOWER_CASE_TRIP_NUMBER, AppCommonMethods.isAllowLowerCaseTripNumber))))));
    
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_ENCODE_TAGS, extractBoolean(labelSetting, ParamConstants.IS_ENCODE_TAGS, extractBoolean(config, ParamConstants.IS_ENCODE_TAGS, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.IS_ENCODE_TAGS, SharedPrefManager.getBoolean(tripNum + ParamConstants.IS_ENCODE_TAGS, SharedPrefManager.getBoolean(ParamConstants.IS_ENCODE_TAGS, false))))));
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.RESTRICT_UNVERIFIED_HU, extractBoolean(labelSetting, ParamConstants.RESTRICT_UNVERIFIED_HU, extractBoolean(config, ParamConstants.RESTRICT_UNVERIFIED_HU, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.RESTRICT_UNVERIFIED_HU, SharedPrefManager.getBoolean(tripNum + ParamConstants.RESTRICT_UNVERIFIED_HU, SharedPrefManager.getBoolean(ParamConstants.RESTRICT_UNVERIFIED_HU, false))))));
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.ALLOW_BARCODE_SCANNING, extractBoolean(labelSetting, ParamConstants.ALLOW_BARCODE_SCANNING, extractBoolean(config, ParamConstants.ALLOW_BARCODE_SCANNING, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.ALLOW_BARCODE_SCANNING, SharedPrefManager.getBoolean(tripNum + ParamConstants.ALLOW_BARCODE_SCANNING, SharedPrefManager.getBoolean(ParamConstants.ALLOW_BARCODE_SCANNING, AppCommonMethods.isAllowBarcodeScanning))))));
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.ALLOW_MIX_TAG_TYPE, extractBoolean(labelSetting, ParamConstants.ALLOW_MIX_TAG_TYPE, extractBoolean(config, ParamConstants.ALLOW_MIX_TAG_TYPE, SharedPrefManager.getBoolean(huNum + tripNum + ParamConstants.ALLOW_MIX_TAG_TYPE, SharedPrefManager.getBoolean(tripNum + ParamConstants.ALLOW_MIX_TAG_TYPE, SharedPrefManager.getBoolean(ParamConstants.ALLOW_MIX_TAG_TYPE, AppCommonMethods.isAllowMixTagType))))));
    SharedPrefManager.setInt(huNum + tripNum + ParamConstants.LOWER_TOLERANCE, extractInt(labelSetting, ParamConstants.LOWER_TOLERANCE, extractInt(config, ParamConstants.LOWER_TOLERANCE, SharedPrefManager.getInt(huNum + tripNum + ParamConstants.LOWER_TOLERANCE, SharedPrefManager.getInt(tripNum + ParamConstants.LOWER_TOLERANCE, SharedPrefManager.getInt(ParamConstants.LOWER_TOLERANCE, 0))))));
    SharedPrefManager.setInt(huNum + tripNum + ParamConstants.UPPER_TOLERANCE, extractInt(labelSetting, ParamConstants.UPPER_TOLERANCE, extractInt(config, ParamConstants.UPPER_TOLERANCE, SharedPrefManager.getInt(huNum + tripNum + ParamConstants.UPPER_TOLERANCE, SharedPrefManager.getInt(tripNum + ParamConstants.UPPER_TOLERANCE, SharedPrefManager.getInt(ParamConstants.UPPER_TOLERANCE, 0))))));
  }
  
  public void clearIOConfig(final String tripNum){ clearIOConfig(tripNum, ""); }
  
  public void clearIOConfig(final String tripNum, final String huNum){
    if(isNullOrEmpty(tripNum)) return;
    SharedPrefManager.setString(huNum + tripNum + ParamConstants.LABEL_TRIP, "");
    SharedPrefManager.setString(huNum + tripNum + ParamConstants.LABEL_HU, "");
    SharedPrefManager.setString(huNum + tripNum + ParamConstants.LABEL_ARTICLE, "");
    SharedPrefManager.setString(huNum + tripNum + ParamConstants.LABEL_SKUID, "");
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_USE_TRIP_AS_HU, false);
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_ALLOW_MANUAL_TRIP_NO_ENTRY, false);
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_ALLOW_MANUAL_HU_ENTRY, false);
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_TRIP_DEVICE_LOCK, false);
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_HU_DEVICE_LOCK, false);
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_API_CALL_FOR_REJECT_HU, false);
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_ON_DEMAND_TRIP_HU_LIST, false);
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_ALLOW_LOWER_CASE_TRIP_NUMBER, false);
    
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.IS_ENCODE_TAGS, false);
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.RESTRICT_UNVERIFIED_HU, false);
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.ALLOW_BARCODE_SCANNING, false);
    SharedPrefManager.setBoolean(huNum + tripNum + ParamConstants.ALLOW_MIX_TAG_TYPE, AppCommonMethods.isAllowMixTagType);
    SharedPrefManager.setInt(huNum + tripNum + ParamConstants.LOWER_TOLERANCE, 0);
    SharedPrefManager.setInt(huNum + tripNum + ParamConstants.UPPER_TOLERANCE, 0);
  }
  
  public void uploadSearchLogs(){
    if(BuildConfig.IS_DIAGNOSTIC_BUILD) return;
    if(appBroadcastReceiver != null) appBroadcastReceiver.uploadSearchLogs(this);
  }
  
  public void uploadWrittenInventoryTags(){
    if(BuildConfig.IS_DIAGNOSTIC_BUILD) return;
    if(appBroadcastReceiver != null) appBroadcastReceiver.uploadWrittenInventoryTags(this);
  }
  
  public void uploadWrittenInventoryTags(final boolean isForceUpload){
    if(BuildConfig.IS_DIAGNOSTIC_BUILD) return;
    if(appBroadcastReceiver != null)
      appBroadcastReceiver.uploadWrittenInventoryTags(this, isForceUpload);
  }
  
  public void uploadSavedInventoryTags(){
    if(BuildConfig.IS_DIAGNOSTIC_BUILD) return;
    if(appBroadcastReceiver != null) appBroadcastReceiver.uploadSavedInventoryTags(this);
  }
  
  public void uploadSavedCartons(){
    if(BuildConfig.IS_DIAGNOSTIC_BUILD) return;
    if(appBroadcastReceiver != null) appBroadcastReceiver.uploadSavedCartons(this);
  }
  
  public void uploadAuditTrailsLogs(){
    if(BuildConfig.IS_DIAGNOSTIC_BUILD) return;
    if(appBroadcastReceiver != null) appBroadcastReceiver.uploadAuditTrailsLogs(this);
  }
  
  /**
   * Is app is in debug mode boolean.
   *
   * @return the boolean
   */
  public boolean isAppIsInDebugMode(){
    return ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
  }
  
  public void printProductData(final JSONArray jsonArray){
    try{
      if(isStaticDebug() && this instanceof MainActivity && isNonEmpty(jsonArray)){
        CommonFragment frag = ((MainActivity) this).getTopFragment();
        if(frag instanceof ProductSearchDetailsFragment)
          ((ProductSearchDetailsFragment) frag).updateSearch(jsonArray);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  public JSONObject getLabelValueJson(String label, String value){
    JSONObject jsonObject = null;
    if(chkNull(value, "").length() > 0){
      try{
        jsonObject = new JSONObject();
        jsonObject.put(ParamConstants.LABEL, label);
        jsonObject.put(ParamConstants.VALUE, value);
      }
      catch(Exception e){ e.printStackTrace(); }
    }
    return jsonObject;
  }
  
  /**
   * Show log.
   *
   * @param tag the tag
   * @param msg the msg
   */
  public void showLog(String tag, String msg){ AppCommonMethods.showLog(tag, msg); }
  
  /**
   * Show short toast.
   *
   * @param res the res
   */
  public void showShortToast(int res){ AppCommonMethods.showShortToast(this, res); }
  
  /**
   * Show long toast.
   *
   * @param res the res
   */
  public void showLongToast(int res){ AppCommonMethods.showLongToast(this, res); }
  
  /**
   * Show alert.
   *
   * @param imgarray the imgarray
   */
  public void showAlert(String[] imgarray){
    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
    
    final View root = getLayoutInflater().inflate(R.layout.view_image, null);
    final ViewPager2 pagerProdImages = root.findViewById(R.id.viewpagerProductImages);
    final TextView txtCount = root.findViewById(R.id.txtProductImagesCount);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    pagerProdImages.getLayoutParams().height = displayMetrics.heightPixels * 2 / 3;
    pagerProdImages.setAdapter(new ImagesListAdapter(this, imgarray));
    txtCount.setVisibility(imgarray.length > 1 ? View.VISIBLE : View.GONE);
    pagerProdImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
      }
      
      @Override
      public void onPageSelected(int position){
        super.onPageSelected(position);
        txtCount.setText((position + 1) + "/" + imgarray.length);
      }
    });
    
    builder.setPositiveButton(getString(R.string.btn_ok), null);
    builder.setView(root);
    customAlertDialog = builder.create();
    customAlertDialog.setCancelable(false);
    customAlertDialog.show();
  }
  
  /**
   * Show short toast.
   *
   * @param msg the msg
   */
  public void showShortToast(String msg){ AppCommonMethods.showShortToast(this, msg); }
  
  /**
   * Show long toast.
   *
   * @param msg the msg
   */
  public void showLongToast(String msg){ AppCommonMethods.showLongToast(this, msg); }
  
  /**
   * Get base directory file.
   *
   * @param subDir the sub dir
   * @return the file
   */
  public File getBaseDirectory(String subDir){
    final File dir = isUseInAppStorage ? subDir.equalsIgnoreCase(Environment.DIRECTORY_DOWNLOADS) ? getExternalFilesDir("") : getExternalCacheDir() : Environment.getExternalStorageDirectory();
    dir.mkdirs();
    if(chkNull(subDir, "").length() > 0){
      final File subDirs = new File(dir, subDir);
      subDirs.mkdirs();
      return subDirs;
    }
    else return dir;
  }
  
  /**
   * Get uri from file uri.
   *
   * @param file the file
   * @return the uri
   */
  public Uri getUriFromFile(File file){
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? FileProvider.getUriForFile(this, getPackageName(), file) : Uri.fromFile(file);
  }
  
  /**
   * Get unique imei string.
   *
   * @return the string
   */
  @SuppressLint("MissingPermission")
  private String getUniqueIMEI(){
    String imei = "";
    try{
      imei = SharedPrefManager.getIMEI();
      if(imei.length() > 0) return imei;
      if(isUseDeviceIDForIMEI){
        imei = chkNull(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID), "A");
        SharedPrefManager.setIMEI(imei);
        return imei;
      }
      else if(checkPermissions(REQUEST_PERMISSION_IMEI)){
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager == null || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
          return "";
        }
        imei = chkNull(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? telephonyManager.getImei() : telephonyManager.getDeviceId(), chkNull(android.os.Build.SERIAL, ""));
        SharedPrefManager.setIMEI(imei);
        
        return imei;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
    return "";
  }
  
  /**
   * Get mac address string.
   *
   * @return the string
   */
  private String getMacAddress(){
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
  
  /**
   * Check camera permissions boolean.
   *
   * @return the boolean
   */
  public boolean checkCameraPermissions(){
    return checkPermissions(isUseInAppStorage ? REQUEST_PERMISSION_CAMERA : REQUEST_PERMISSION_CAMERA + REQUEST_PERMISSION_STORAGE);
  }
  
  /**
   * Check location permissions boolean.
   *
   * @return the boolean
   */
  public boolean checkLocationPermissions(){
    return checkPermissions(REQUEST_PERMISSION_LOCATION);
  }
  
  /**
   * Check storage permissions boolean.
   *
   * @return the boolean
   */
  public boolean checkStoragePermissions(){
    return isUseInAppStorage || checkPermissions(REQUEST_PERMISSION_STORAGE);
  }
  
  /**
   * Check storage permissions boolean.
   *
   * @return the boolean
   */
  public boolean checkBluetoothPermissions(){
    return isUseBluetoothScanConnect || checkPermissions(REQUEST_PERMISSION_BLUETOOTH);
  }
  
  /**
   * Check permissions boolean.
   *
   * @param requestPermissionCode the request permission code
   * @return the boolean
   */
  public boolean checkPermissions(final int requestPermissionCode){
    final boolean isCheckBluetoothPermission = requestPermissionCode == 0 || (isUseBluetoothScanConnect && SharedPrefManager.getIsDeviceBluetoothDependent() && (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)) || requestPermissionCode >= REQUEST_PERMISSION_BLUETOOTH;
    final boolean isCheckIMEIPermission = requestPermissionCode == 0 || requestPermissionCode == REQUEST_PERMISSION_IMEI;
    final boolean isCheckStoragePermission = requestPermissionCode == 0 || requestPermissionCode == REQUEST_PERMISSION_STORAGE || requestPermissionCode == (REQUEST_PERMISSION_STORAGE + REQUEST_PERMISSION_CAMERA);
    final boolean isCheckCameraPermission = requestPermissionCode == 0 || requestPermissionCode >= REQUEST_PERMISSION_CAMERA || requestPermissionCode == (REQUEST_PERMISSION_STORAGE + REQUEST_PERMISSION_CAMERA);
    final boolean isCheckLocationPermission = requestPermissionCode == 0 || requestPermissionCode == REQUEST_PERMISSION_LOCATION;
    final boolean isCheckNotificationPermission = requestPermissionCode == 0 || requestPermissionCode == REQUEST_PERMISSION_NOTIFICATION;
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
      boolean isShowRationale = true;
      List<String> listRequestPermissions = new ArrayList<>(0);
      int requestCode = 0;
      if(isShowRationale && isCheckLocationPermission && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
        isShowRationale = !SharedPrefManager.getIsChkRationale() || (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION));
        showLog("isShowRationale", "" + isShowRationale);
        listRequestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        listRequestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        requestCode += REQUEST_PERMISSION_LOCATION;
      }
      if(isShowRationale && isCheckCameraPermission && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
        isShowRationale = !SharedPrefManager.getIsChkRationale() || shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);
        listRequestPermissions.add(Manifest.permission.CAMERA);
        requestCode += REQUEST_PERMISSION_CAMERA;
      }
      if(isShowRationale && isCheckStoragePermission && ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))){
        isShowRationale = !SharedPrefManager.getIsChkRationale() || ((Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2 || shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) || (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)));
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
          listRequestPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
          listRequestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requestCode += REQUEST_PERMISSION_STORAGE;
      }
      if(isShowRationale && isCheckIMEIPermission && !isUseDeviceIDForIMEI && SharedPrefManager.getIMEI().length() <= 0 && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
        isShowRationale = !SharedPrefManager.getIsChkRationale() || shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE);
        listRequestPermissions.add(Manifest.permission.READ_PHONE_STATE);
        requestCode += REQUEST_PERMISSION_IMEI;
      }
      if(isShowRationale && isCheckBluetoothPermission && isUseBluetoothScanConnect && (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)){
        isShowRationale = !SharedPrefManager.getIsChkRationale() || (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT) || shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN));
        listRequestPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        listRequestPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
        requestCode += REQUEST_PERMISSION_BLUETOOTH;
      }
      if(isShowRationale && isCheckNotificationPermission && isUseNotificationPermission && ActivityCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED){
        isShowRationale = !SharedPrefManager.getIsChkRationale() || shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
        listRequestPermissions.add(Manifest.permission.POST_NOTIFICATIONS);
        requestCode += REQUEST_PERMISSION_NOTIFICATION;
      }
      if(listRequestPermissions.size() > 0){
        if(!isShowRationale){ //Show Dialog if a permission is Always/Permanently Denied (i.e. Deny with Don't Ask)
          showCustomAlertDialog("", R.string.err_grant_app_permissions, R.string.btn_ok, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              if(appPermissionsResultLauncher != null)
                appPermissionsResultLauncher.launch(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null)));
            }
          });
        }
        else
          requestPermissions(listRequestPermissions.toArray(new String[listRequestPermissions.size()]), requestCode);
        return false;
      }
      else return true;
    }
    else return true;
  }
  
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    SharedPrefManager.setIsChkRationale(true);
    boolean isGranted = true;
    for(String permission : permissions)
      if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
        isGranted = false;
        break;
      }
    if(!isGranted) checkPermissions(requestCode);
    else if(isGranted){
      if(requestCode >= 8 && requestCode < 16){
        getUniqueIMEI();
        getMacAddress();
      }
    }
  }
  
  /**
   * Hide keyboard.
   */
  public void hideKeyboard(){ AppCommonMethods.hideKeyboard(this); }
  
  protected void deleteEmptySessions(AppDatabase db){
    if(db == null) db = AppDatabase.getDbInstance(this);
    final InventoryDao inventoryDao = db.InventoryDao();
    final RFIDSessionDao rfidSessionDao = db.RFIDSessionDao();
    if(inventoryDao != null && rfidSessionDao != null)
      for(AppCommonMethods.SessionType sessionType : AppCommonMethods.SessionType.values()){
        final RFIDSession rfidSession = rfidSessionDao.getCurrentSession(sessionType.getValue());
        if(rfidSession != null && (inventoryDao.getNonUploadedCount(rfidSession.sessionId) <= 0 && inventoryDao.getInventorySize(rfidSession.sessionId) <= 0)){
          rfidSessionDao.deleteAll(sessionType.getValue());
          if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY || sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY)
            db.BrandEansDao().deleteAll();
        }
      }
  }
  
  public String getProductInfoUrl(){
    return isDCApp ? URLConstants.GET_PRODUCT_INFO_BY_SKU : URLConstants.GET_PRODUCT_INFO;
  }
  
  public String getEPCForEncodeUrl(){
    return isDCApp ? URLConstants.GET_EPC_FOR_ENCODE : URLConstants.GET_EPC_FOR_ENCODING;
  }
  
  public String getUploadEncodeUrl(){
    return isDCApp ? URLConstants.UPLOAD_ENCODING : URLConstants.UPLOAD_ENCODE;
  }
  
  /**
   * Load image.
   *
   * @param imgView the img view
   * @param url     the url
   */
  public void loadImage(final ImageView imgView, String url){ loadImage(imgView, url, 0, true); }
  
  /**
   * Load image.
   *
   * @param imgView the img view
   * @param url     the url
   * @param imgId   the img id
   */
  public void loadImage(final ImageView imgView, String url, @DrawableRes int imgId){ loadImage(imgView, url, imgId, false); }
  
  /**
   * Load image.
   *
   * @param imgView     the img view
   * @param url         the url
   * @param imgId       the img id
   * @param isShowAlert the is show alert
   */
  public void loadImage(final ImageView imgView, String url, @DrawableRes int imgId, boolean isShowAlert){
    try{
      final String imgUrl = isNonEmpty(url) ? (isValidUrl(url) ? url : SharedPrefManager.getServerUrl().replaceFirst(AppCommonMethods.SERVER_URL_APPEND_API, url.startsWith("/") ? "" : AppCommonMethods.SERVER_URL_APPEND_IMG) + url.replaceAll("(\"|\\[|\\]|,null|null,)", "")).trim().split(",")[0] : "";
      final String fileName = isNonEmpty(url) ? url.replaceAll("(\"|\\[|\\]|,null|null,)", "").trim().split(",")[0] : "";
      showLog("fileName", "" + fileName);
      final File image = isNonEmpty(url) ? new File(SharedPrefManager.getServerUrl(), fileName) : null;
      final int menuIconId = getIconIdFromMenuCode(url);
      AppCommonMethods.showLog("url", url);
      if(menuIconId > 0) AppCommonMethods.showLog("menuIconId_" + url, "" + menuIconId);
      if(isShowAlert){
        imgView.setOnClickListener(null);
      }
      if(isValidUrl(imgUrl)){
        final ProgressBar progress = new ProgressBar(this);
        progress.setIndeterminate(true);
        final Drawable progressDrawable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? progress.getCurrentDrawable() : progress.getIndeterminateDrawable();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
          progressDrawable.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(colorPrimaryDarkFromTheme, BlendModeCompat.SRC_IN));
        else progressDrawable.setColorFilter(colorPrimaryDarkFromTheme, PorterDuff.Mode.SRC_IN);
        
        if(imgView instanceof PhotoView)
          Glide.with(this).load(imgUrl).error(menuIconId != 0 ? menuIconId : imgId != 0 ? imgId : R.drawable.ic_no_img).placeholder(progressDrawable).dontTransform().diskCacheStrategy(DiskCacheStrategy.NONE).into(imgView);
        else
          Glide.with(this).load(imgUrl).error(menuIconId != 0 ? menuIconId : imgId != 0 ? imgId : R.drawable.ic_no_img).placeholder(progressDrawable).fitCenter().diskCacheStrategy(DiskCacheStrategy.NONE).into(imgView);
        if(isShowAlert && !(imgView instanceof PhotoView) && isValidUrl(imgUrl)){
          imgView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
              if(isShowAlert && isValidUrl(imgUrl))
                showAlert(url.replaceAll("(\"|\\[|\\]|,null|null,)", "").trim().split(","));
            }
          });
        }
      }
      else if(image != null && image.exists() && image.length() > 0){
        showLog("filePath", "" + image.getAbsolutePath());
        final ProgressBar progress = new ProgressBar(this);
        progress.setIndeterminate(true);
        final Drawable progressDrawable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? progress.getCurrentDrawable() : progress.getIndeterminateDrawable();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
          progressDrawable.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(colorPrimaryDarkFromTheme, BlendModeCompat.SRC_IN));
        else progressDrawable.setColorFilter(colorPrimaryDarkFromTheme, PorterDuff.Mode.SRC_IN);
        
        if(imgView instanceof PhotoView)
          Glide.with(this).load(image).error(menuIconId != 0 ? menuIconId : imgId != 0 ? imgId : R.drawable.ic_no_img).placeholder(progressDrawable).dontTransform().diskCacheStrategy(DiskCacheStrategy.NONE).into(imgView);
        else
          Glide.with(this).load(image).error(menuIconId != 0 ? menuIconId : imgId != 0 ? imgId : R.drawable.ic_no_img).placeholder(progressDrawable).fitCenter().diskCacheStrategy(DiskCacheStrategy.NONE).into(imgView);
        if(isShowAlert && !(imgView instanceof PhotoView)){
          imgView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
              if(isShowAlert)
                showAlert(url.replaceAll("(\"|\\[|\\]|,null|null,)", "").trim().split(","));
            }
          });
        }
      }
      else if(menuIconId != 0) imgView.setImageResource(menuIconId);
      else if(imgId != 0) imgView.setImageResource(imgId);
      else imgView.setImageResource(R.drawable.ic_no_img);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  private int getIconIdFromMenuCode(final String menuCode){
    if(isNullOrEmpty(menuCode)) return 0;
    final String menuIcon = menuCode.toLowerCase().replaceAll(" ", "_");
    String menuIconName = "ic_" + (menuIcon.endsWith("_start") ? "start" : menuIcon.endsWith("_config") ? "config" : menuIcon.endsWith("_ord_stats") || menuIcon.endsWith("_order_stats") || menuIcon.endsWith("_achieve") || menuIcon.endsWith("_achievement") || menuIcon.endsWith("_achievements") || menuIcon.endsWith("_award") ? "achieve" : menuIcon);
    return getResources().getIdentifier(menuIconName, AppConstants.RES_DRAWABLE, getPackageName());
  }
  
  /**
   * Double pop back stack.
   */
  public void doublePopBackStack(){
    if(getSupportFragmentManager() != null && getSupportFragmentManager().getBackStackEntryCount() > 1){
      getSupportFragmentManager().popBackStackImmediate();
      popBackStack();
    }
  }
  
  /**
   * Pop back stack.
   */
  public void popBackStack(){
    if(getSupportFragmentManager() != null && getSupportFragmentManager().getBackStackEntryCount() > 0){
      getSupportFragmentManager().popBackStackImmediate();
      if(getSupportFragmentManager().getBackStackEntryCount() > 0 && this instanceof MainActivity){
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if(fragment != null && fragment instanceof CommonFragment) fragment.onResume();
      }
      else if(getSupportFragmentManager().getBackStackEntryCount() == 0) this.onResume();
    }
  }
  
  /**
   * Pop back stack.
   */
  public void popBackStack(String tag){
    if(getSupportFragmentManager() != null && getSupportFragmentManager().getBackStackEntryCount() > 0){
      getSupportFragmentManager().popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
      if(getSupportFragmentManager().getBackStackEntryCount() > 0 && this instanceof MainActivity){
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if(fragment != null && fragment instanceof CommonFragment) fragment.onResume();
      }
      else if(getSupportFragmentManager().getBackStackEntryCount() == 0) this.onResume();
    }
  }
  
  /**
   * Remove from back stack.
   */
  public void removeFromBackStack(Class frag){
    if(frag != null) removeFromBackStack(frag.getSimpleName());
  }
  
  /**
   * Remove from back stack.
   */
  public void removeFromBackStack(String tag){
    if(getSupportFragmentManager() != null && getSupportFragmentManager().getBackStackEntryCount() > 0 && isNonEmpty(tag))
      removeFromBackStack(getSupportFragmentManager().findFragmentByTag(tag));
  }
  
  /**
   * Remove from back stack.
   */
  public void removeFromBackStack(Fragment fragment){
    if(fragment != null && getSupportFragmentManager() != null && getSupportFragmentManager().getBackStackEntryCount() > 0)
      getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    else if(this instanceof MainActivity && fragment != null && fragment instanceof CommonFragment)
      fragment.onResume();
    else if(getSupportFragmentManager().getBackStackEntryCount() == 0) this.onResume();
  }
  
  /**
   * Clear back stack.
   */
  public void clearBackStack(){
    if(getSupportFragmentManager() != null && getSupportFragmentManager().getBackStackEntryCount() > 0){
      int count = getSupportFragmentManager().getBackStackEntryCount();
      showLog("backStackCount1", "" + count);
      for(int i = 0; i < count; i++)
        getSupportFragmentManager().popBackStackImmediate();
      showLog("backStackCount2", "" + getSupportFragmentManager().getBackStackEntryCount());
      if(getSupportFragmentManager().getBackStackEntryCount() == 0) this.onResume();
    }
  }
  
  /**
   * Set alert dialog custom title.
   *
   * @param alertDialog the alert dialog
   * @param resId       the res id
   */
  public void setAlertDialogCustomTitle(final AlertDialog alertDialog, final int resId){
    setAlertDialogCustomTitle(alertDialog, chkNull(getString(resId), ""));
  }
  
  /**
   * Set alert dialog custom title.
   *
   * @param alertDialog    the alert dialog
   * @param resId          the res id
   * @param titleLogoResId the title logo res id
   */
  public void setAlertDialogCustomTitle(final AlertDialog alertDialog, final int resId, final int titleLogoResId){
    setAlertDialogCustomTitle(alertDialog, chkNull(getString(resId), ""), titleLogoResId);
  }
  
  /**
   * Set alert dialog custom title.
   *
   * @param alertDialog the alert dialog
   * @param title       the title
   */
  public void setAlertDialogCustomTitle(final AlertDialog alertDialog, final String title){
    setAlertDialogCustomTitle(alertDialog, title, 0);
  }
  
  /**
   * Set alert dialog custom title.
   *
   * @param alertDialog    the alert dialog
   * @param title          the title
   * @param titleLogoResId the title logo res id
   */
  public void setAlertDialogCustomTitle(final AlertDialog alertDialog, final String title, final int titleLogoResId){
    setAlertDialogCustomTitle(alertDialog, title, titleLogoResId, null);
  }
  
  /**
   * Set alert dialog custom title.
   *
   * @param alertDialog the alert dialog
   * @param title       the title
   * @param isSuccess   the is success
   */
  public void setAlertDialogCustomTitle(final AlertDialog alertDialog, final String title, final Boolean isSuccess){
    setAlertDialogCustomTitle(alertDialog, title, 0, isSuccess);
  }
  
  /**
   * Set alert dialog custom title.
   *
   * @param alertDialog    the alert dialog
   * @param title          the title
   * @param titleLogoResId the title logo res id
   * @param isSuccess      the is success
   */
  public void setAlertDialogCustomTitle(final AlertDialog alertDialog, final String title, final int titleLogoResId, final Boolean isSuccess){
    final View dialogTitleLayout = getLayoutInflater().inflate(R.layout.dialog_cust_title, null);
    dialogTitleLayout.findViewById(R.id.btn_dialog_title_close).setOnClickListener(v -> alertDialog.dismiss());
    final ImageView imgLogo = dialogTitleLayout.findViewById(R.id.img_dialog_title_logo);
    final TextView txtTitle = dialogTitleLayout.findViewById(R.id.txt_dialog_title);
    txtTitle.setText(chkNull(title, "").replaceAll("_", ""));
    alertDialog.setCustomTitle(dialogTitleLayout);
    imgLogo.setVisibility(titleLogoResId > 0 || isSuccess != null ? View.VISIBLE : View.GONE);
    if(titleLogoResId > 0) imgLogo.setImageResource(titleLogoResId);
    else if(isSuccess != null){
      imgLogo.setImageResource(isSuccess ? R.drawable.ic_success : R.drawable.ic_error);
      if(imgLogo.getParent() != null && imgLogo.getParent() instanceof LinearLayoutCompat){
        ((LinearLayoutCompat.LayoutParams) imgLogo.getLayoutParams()).setMargins(0, 0, 0, 0);
        ((LinearLayoutCompat) imgLogo.getParent()).setOrientation(isSuccess != null ? LinearLayoutCompat.VERTICAL : LinearLayoutCompat.HORIZONTAL);
      }
    }
    else if(isNullOrEmpty(txtTitle.getText().toString())){
      if(txtTitle.getParent() != null && txtTitle.getParent() instanceof LinearLayoutCompat)
        ((LinearLayoutCompat) txtTitle.getParent()).setVisibility(View.GONE);
    }
    //check URL implementation also
    
  }
  
  /**
   * Show custom success dialog.
   *
   * @param successMsgStrRes the success msg str res
   */
  public void showCustomSuccessDialog(@StringRes int successMsgStrRes){
    showCustomSuccessDialog(successMsgStrRes, false);
  }
  
  /**
   * Show custom success dialog.
   *
   * @param successMsg the success msg
   */
  public void showCustomSuccessDialog(String successMsg){
    showCustomSuccessDialog(successMsg, false);
    if(successMsg.startsWith("IDE")){
      if(this instanceof MainActivity && currentFragmentClassName.equalsIgnoreCase(EncodingStartFragment.class.getSimpleName()))
        ((EncodingStartFragment) getSupportFragmentManager().findFragmentByTag(EncodingStartFragment.class.getSimpleName())).clearBarcode();
      else if(this instanceof MainActivity && currentFragmentClassName.equalsIgnoreCase(EncodingScanScanWriteFragment.class.getSimpleName()))
        ((EncodingScanScanWriteFragment) getSupportFragmentManager().findFragmentByTag(EncodingScanScanWriteFragment.class.getSimpleName())).clearBarcode();
    }
  }
  
  /**
   * Show custom success dialog.
   *
   * @param successMsgStrRes the success msg str res
   * @param isFinishOnClick  the is finish on click
   */
  public void showCustomSuccessDialog(@StringRes int successMsgStrRes, boolean isFinishOnClick){
    showCustomMsgDialog(successMsgStrRes, true, isFinishOnClick);
  }
  
  /**
   * Show custom success dialog.
   *
   * @param successMsg      the success msg
   * @param isFinishOnClick the is finish on click
   */
  public void showCustomSuccessDialog(String successMsg, boolean isFinishOnClick){
    showCustomMsgDialog(successMsg, true, isFinishOnClick);
  }
  
  /**
   * Show custom success dialog.
   *
   * @param successMsg the success msg
   * @param posClick   the pos click
   */
  public void showCustomSuccessDialog(String successMsg, DialogInterface.OnClickListener posClick){
    showCustomMsgDialog(successMsg, true, posClick);
  }
  
  /**
   * Show custom success dialog.
   *
   * @param successMsg      the success msg
   * @param isFinishOnClick the is finish on click
   * @param isAutoDismiss   the is auto dismiss
   */
  public void showCustomSuccessDialog(String successMsg, boolean isFinishOnClick, boolean isAutoDismiss){
    showCustomMsgDialog(successMsg, null, true, isFinishOnClick, isAutoDismiss, null);
  }
  
  /**
   * Show custom success dialog.
   *
   * @param successMsg    the success msg
   * @param isAutoDismiss the is finish on click
   * @param posClick      the pos click
   */
  public void showCustomSuccessDialog(String successMsg, boolean isAutoDismiss, DialogInterface.OnClickListener posClick){
    showCustomMsgDialog(successMsg, null, true, false, isAutoDismiss, posClick);
  }
  
  /**
   * Show custom err dialog.
   *
   * @param errMsgStrRes the err msg str res
   */
  public void showCustomErrDialog(@StringRes int errMsgStrRes){
    if(isNonZeroId(errMsgStrRes)) showCustomErrDialog(getString(errMsgStrRes));
  }
  
  /**
   * Show custom err dialog.
   *
   * @param errMsg the err msg
   */
  public void showCustomErrDialog(String errMsg){
    showCustomMsgDialog(errMsg, false, false);
    if(errMsg.startsWith("EDE")){
      if(this instanceof MainActivity && currentFragmentClassName.equalsIgnoreCase(EncodingStartFragment.class.getSimpleName()))
        ((EncodingStartFragment) getSupportFragmentManager().findFragmentByTag(EncodingStartFragment.class.getSimpleName())).clearBarcode();
      else if(this instanceof MainActivity && currentFragmentClassName.equalsIgnoreCase(EncodingScanScanWriteFragment.class.getSimpleName()))
        ((EncodingScanScanWriteFragment) getSupportFragmentManager().findFragmentByTag(EncodingScanScanWriteFragment.class.getSimpleName())).clearBarcode();
    }
  }
  
  /**
   * Show custom err dialog.
   *
   * @param errMsg          the err msg
   * @param isFinishOnClick the is finish on click
   */
  public void showCustomErrDialog(String errMsg, boolean isFinishOnClick){
    showCustomMsgDialog(errMsg, false, isFinishOnClick);
  }
  
  /**
   * Show custom msg dialog.
   *
   * @param msgStrRes       the msg str res
   * @param isSuccess       the is success
   * @param isFinishOnClick the is finish on click
   */
  public void showCustomMsgDialog(@StringRes int msgStrRes, boolean isSuccess, boolean isFinishOnClick){
    if(isNonZeroId(msgStrRes))
      showCustomMsgDialog(getString(msgStrRes), isSuccess, isFinishOnClick);
  }
  
  /**
   * Show custom msg dialog.
   *
   * @param msg             the msg
   * @param isSuccess       the is success
   * @param isFinishOnClick the is finish on click
   */
  public void showCustomMsgDialog(String msg, boolean isSuccess, boolean isFinishOnClick){ showCustomMsgDialog(msg, isSuccess, isFinishOnClick, null); }
  
  /**
   * Show custom msg dialog.
   *
   * @param msg       the msg
   * @param isSuccess the is success
   * @param posKey    the pos key
   */
  public void showCustomMsgDialog(String msg, boolean isSuccess, DialogInterface.OnClickListener posKey){ showCustomMsgDialog(msg, isSuccess, false, posKey); }
  
  /**
   * Show custom msg dialog.
   *
   * @param msg             the msg
   * @param isSuccess       the is success
   * @param isFinishOnClick the is finish on click
   * @param posKey          the pos key
   */
  public void showCustomMsgDialog(String msg, boolean isSuccess, boolean isFinishOnClick, DialogInterface.OnClickListener posKey){ showCustomMsgDialog(msg, null, isSuccess, isFinishOnClick, posKey); }
  
  /**
   * Show custom msg dialog.
   *
   * @param msg             the msg
   * @param view            the view
   * @param isSuccess       the is success
   * @param isFinishOnClick the is finish on click
   * @param posKey          the pos key
   */
  public void showCustomMsgDialog(String msg, View view, boolean isSuccess, boolean isFinishOnClick, DialogInterface.OnClickListener posKey){
    showCustomMsgDialog(msg, view, isSuccess, isFinishOnClick, isSuccess, posKey);
  }
  
  /**
   * Show custom msg dialog.
   *
   * @param msg             the msg
   * @param view            the view
   * @param isSuccess       the is success
   * @param isFinishOnClick the is finish on click
   * @param isAutoDismiss   the is finish on click
   * @param posKey          the pos key
   */
  public void showCustomMsgDialog(String msg, View view, boolean isSuccess, boolean isFinishOnClick, boolean isAutoDismiss, DialogInterface.OnClickListener posKey){
    showCustomMsgDialog(msg, view, isSuccess, isFinishOnClick, isAutoDismiss, posKey, null);
  }
  
  public void showCustomMsgDialog(String msg, View view, boolean isSuccess, boolean isFinishOnClick, boolean isAutoDismiss, DialogInterface.OnClickListener posKey, String negKey){
    try{
      runOnUiThread(() -> showCustomAlertDialog("", "<b><font color=" + "#" + Integer.toHexString(getResources().getColor(isSuccess ? R.color.green : R.color.err_red)) + ">" + msg + "</font></b>", view, isSuccess, isAutoDismiss, getString(R.string.btn_ok), posKey != null ? posKey : isFinishOnClick ? (DialogInterface.OnClickListener) (dialogInterface, i) -> {
        if(isFinishOnClick) popBackStack();
      } : null, negKey, null));
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Show custom err retry dialog.
   *
   * @param fragment      the fragment
   * @param url           the url
   * @param jsonRequest   the json request
   * @param jsonResponse  the json response
   * @param responseCode  the response code
   * @param args          the args
   * @param isRetry       the is retry
   * @param progressMsg   the progress msg
   * @param errMsg        the err msg
   * @param isForceLogout the is force logout
   */
  public void showCustomErrRetryDialog(final CommonFragment fragment, final String url, final JSONObject jsonRequest, final JSONObject jsonResponse, final Integer responseCode, final Bundle args, final boolean isRetry, final String progressMsg, final String errMsg, final boolean isForceLogout){
    try{
      runOnUiThread(() -> {
        final boolean isUserConfirmation = responseCode == 406;
        final ImageView imgLogo = new ImageView(CommonActivity.this);
        imgLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgLogo.setImageResource(R.drawable.ic_error);
        showCustomAlertDialog("", "<b><font color=" + "#" + Integer.toHexString(getResources().getColor(R.color.err_red)) + ">" + errMsg + (isUserConfirmation || isRetry ? "\n" + getString(isUserConfirmation ? R.string.msg_confirm_continue : R.string.msg_retry) : "") + "</font></b>", null, false, false/*!isRetry*/, getString(isUserConfirmation ? R.string.btn_yes : isRetry ? R.string.btn_retry : R.string.btn_ok), (dialogInterface, i) -> {
          if(isRetry || isUserConfirmation){
            if(isUserConfirmation){
              try{
                if(jsonRequest != null) jsonRequest.put(ParamConstants.IS_CONFIREMED_BY_USER, true);
              }
              catch(JSONException e){
                throw new RuntimeException(e);
              }
            }
            if(fragment != null)
              fragment.callWebService(url, jsonRequest, args, isRetry, progressMsg);
            else callWebService(url, jsonRequest, args, isRetry, progressMsg);
          }
          else{
            if(isForceLogout && SharedPrefManager.getIsLoggedIn() && CommonActivity.this instanceof MainActivity){
              ((MainActivity) CommonActivity.this).clearSavedDataOnLogout();
            }
            else if(fragment != null)
              fragment.handleResponse(url, jsonRequest, jsonResponse, responseCode, false, args);
            else handleResponse(url, jsonRequest, jsonResponse, responseCode, false, args);
          }
        }, isUserConfirmation || isRetry ? getString(isUserConfirmation ? R.string.btn_no : R.string.btn_cancel) : "", (dialogInterface, i) -> {
          if(fragment != null)
            fragment.handleResponse(url, jsonRequest, jsonResponse, responseCode, false, args);
          else handleResponse(url, jsonRequest, jsonResponse, responseCode, false, args);
        });
      });
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Show custom confirm dialog.
   *
   * @param msgStrRes    the msg str res
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   */
  public void showCustomConfirmDialog(@StringRes int msgStrRes, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes))
      showCustomConfirmDialog(getString(msgStrRes), getString(posKeyStrRes), posClick);
  }
  
  /**
   * Show custom confirm dialog.
   *
   * @param msg          the msg
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   */
  public void showCustomConfirmDialog(String msg, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick){
    if(isNonZeroId(posKeyStrRes)) showCustomConfirmDialog(msg, getString(posKeyStrRes), posClick);
  }
  
  /**
   * Show custom confirm dialog.
   *
   * @param msg      the msg
   * @param posKey   the pos key
   * @param posClick the pos click
   */
  public void showCustomConfirmDialog(String msg, String posKey, DialogInterface.OnClickListener posClick){
    showCustomAlertDialog("", msg, posKey, posClick, getString(R.string.btn_cancel), null);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msgStrRes    the msg str res
   * @param posKeyStrRes the pos key str res
   */
  public void showCustomAlertDialog(String title, @StringRes int msgStrRes, @StringRes int posKeyStrRes){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes))
      showCustomAlertDialog(title, getString(msgStrRes), getString(posKeyStrRes));
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param posKeyStrRes the pos key str res
   */
  public void showCustomAlertDialog(String title, String msg, @StringRes int posKeyStrRes){
    if(isNonZeroId(posKeyStrRes)) showCustomAlertDialog(title, msg, getString(posKeyStrRes));
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title  the title
   * @param msg    the msg
   * @param posKey the pos key
   */
  public void showCustomAlertDialog(String title, String msg, String posKey){
    showCustomAlertDialog(title, msg, posKey, null);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title     the title
   * @param msg       the msg
   * @param isSuccess the is success
   * @param posKey    the pos key
   */
  public void showCustomAlertDialog(String title, String msg, Boolean isSuccess, String posKey){
    showCustomAlertDialog(title, msg, isSuccess, false, posKey);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   */
  public void showCustomAlertDialog(String title, String msg, boolean isAutoDismiss, String posKey){
    showCustomAlertDialog(title, msg, null, isAutoDismiss, posKey);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param isSuccess     the is success
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   */
  public void showCustomAlertDialog(String title, String msg, Boolean isSuccess, boolean isAutoDismiss, String posKey){
    showCustomAlertDialog(title, msg, isSuccess, isAutoDismiss, posKey, null);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msgStrRes    the msg str res
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   */
  public void showCustomAlertDialog(String title, @StringRes int msgStrRes, final View view, @StringRes int posKeyStrRes){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes))
      showCustomAlertDialog(title, getString(msgStrRes), view, getString(posKeyStrRes));
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   */
  public void showCustomAlertDialog(String title, String msg, final View view, @StringRes int posKeyStrRes){
    if(isNonZeroId(posKeyStrRes)) showCustomAlertDialog(title, msg, view, getString(posKeyStrRes));
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title  the title
   * @param msg    the msg
   * @param view   the view
   * @param posKey the pos key
   */
  public void showCustomAlertDialog(String title, String msg, final View view, String posKey){
    showCustomAlertDialog(title, msg, view, posKey, null, null, null);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title    the title
   * @param msg      the msg
   * @param posKey   the pos key
   * @param posClick the pos click
   */
  public void showCustomAlertDialog(String title, String msg, String posKey, DialogInterface.OnClickListener posClick){
    showCustomAlertDialog(title, msg, posKey, posClick, null, null);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title     the title
   * @param msg       the msg
   * @param isSuccess the is success
   * @param posKey    the pos key
   * @param posClick  the pos click
   */
  public void showCustomAlertDialog(String title, String msg, Boolean isSuccess, String posKey, DialogInterface.OnClickListener posClick){
    showCustomAlertDialog(title, msg, isSuccess, false, posKey, posClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   * @param posClick      the pos click
   */
  public void showCustomAlertDialog(String title, String msg, boolean isAutoDismiss, String posKey, DialogInterface.OnClickListener posClick){
    showCustomAlertDialog(title, msg, isAutoDismiss, posKey, posClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param isSuccess     the is success
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   * @param posClick      the pos click
   */
  public void showCustomAlertDialog(String title, String msg, Boolean isSuccess, boolean isAutoDismiss, String posKey, DialogInterface.OnClickListener posClick){
    showCustomAlertDialog(title, msg, null, isSuccess, isAutoDismiss, posKey, posClick);
    if(!isSuccess && msg.startsWith("EDE") && this instanceof MainActivity){
      if(currentFragmentClassName.equalsIgnoreCase(EncodingStartFragment.class.getSimpleName()))
        ((EncodingStartFragment) getSupportFragmentManager().findFragmentByTag(EncodingStartFragment.class.getSimpleName())).clearBarcode();
      else if(currentFragmentClassName.equalsIgnoreCase(EncodingScanScanWriteFragment.class.getSimpleName()))
        ((EncodingScanScanWriteFragment) getSupportFragmentManager().findFragmentByTag(EncodingScanScanWriteFragment.class.getSimpleName())).clearBarcode();
    }
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msgStrRes    the msg str res
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   */
  public void showCustomAlertDialog(String title, @StringRes int msgStrRes, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes))
      showCustomAlertDialog(title, getString(msgStrRes), getString(posKeyStrRes), posClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   */
  public void showCustomAlertDialog(String title, String msg, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick){
    if(isNonZeroId(posKeyStrRes))
      showCustomAlertDialog(title, msg, getString(posKeyStrRes), posClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msgStrRes    the msg str res
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   */
  public void showCustomAlertDialog(String title, @StringRes int msgStrRes, final View view, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes))
      showCustomAlertDialog(title, getString(msgStrRes), view, getString(posKeyStrRes), posClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   */
  public void showCustomAlertDialog(String title, String msg, final View view, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick){
    if(isNonZeroId(posKeyStrRes))
      showCustomAlertDialog(title, msg, view, getString(posKeyStrRes), posClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title    the title
   * @param msg      the msg
   * @param view     the view
   * @param posKey   the pos key
   * @param posClick the pos click
   */
  public void showCustomAlertDialog(String title, String msg, View view, String posKey, DialogInterface.OnClickListener posClick){
    showCustomAlertDialog(title, msg, view, null, false, posKey, posClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title     the title
   * @param msg       the msg
   * @param view      the view
   * @param isSuccess the is success
   * @param posKey    the pos key
   * @param posClick  the pos click
   */
  public void showCustomAlertDialog(String title, String msg, View view, Boolean isSuccess, String posKey, DialogInterface.OnClickListener posClick){
    showCustomAlertDialog(title, msg, view, isSuccess, false, posKey, posClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param view          the view
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   * @param posClick      the pos click
   */
  public void showCustomAlertDialog(String title, String msg, View view, boolean isAutoDismiss, String posKey, DialogInterface.OnClickListener posClick){
    showCustomAlertDialog(title, msg, view, null, isAutoDismiss, posKey, posClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param view          the view
   * @param isSuccess     the is success
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   * @param posClick      the pos click
   */
  public void showCustomAlertDialog(String title, String msg, View view, Boolean isSuccess, boolean isAutoDismiss, String posKey, DialogInterface.OnClickListener posClick){
    showCustomAlertDialog(title, msg, view, isSuccess, isAutoDismiss, posKey, posClick, null, null);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msgStrRes    the msg str res
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   * @param negClick     the neg click
   */
  public void showCustomAlertDialog(String title, @StringRes int msgStrRes, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes, DialogInterface.OnClickListener negClick){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes))
      showCustomAlertDialog(title, getString(msgStrRes), getString(posKeyStrRes), posClick, getString(negKeyStrRes), negClick);
  }
  
  /**
   * Show custom alert selection dialog.
   *
   * @param title        the title
   * @param msgStrRes    the msg str res
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   * @param negClick     the neg click
   */
  public void showCustomAlertSelectionDialog(String title, @StringRes int msgStrRes, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes, DialogInterface.OnClickListener negClick){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes))
      showCustomAlertSelectionDialog(title, getString(msgStrRes), getString(posKeyStrRes), posClick, getString(negKeyStrRes), negClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msgStrRes    the msg str res
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   * @param negClick     the neg click
   */
  public void showCustomAlertDialog(String title, @StringRes int msgStrRes, final View view, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes, DialogInterface.OnClickListener negClick){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes))
      showCustomAlertDialog(title, getString(msgStrRes), view, getString(posKeyStrRes), posClick, getString(negKeyStrRes), negClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   * @param negClick     the neg click
   */
  public void showCustomAlertDialog(String title, String msg, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes, DialogInterface.OnClickListener negClick){
    if(isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes))
      showCustomAlertDialog(title, msg, getString(posKeyStrRes), posClick, getString(negKeyStrRes), negClick);
  }
  
  /**
   * Show custom alert selection dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   * @param negClick     the neg click
   */
  public void showCustomAlertSelectionDialog(String title, String msg, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes, DialogInterface.OnClickListener negClick){
    if(isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes))
      showCustomAlertSelectionDialog(title, msg, getString(posKeyStrRes), posClick, getString(negKeyStrRes), negClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title    the title
   * @param msg      the msg
   * @param posKey   the pos key
   * @param posClick the pos click
   * @param negKey   the neg key
   * @param negClick the neg click
   */
  public void showCustomAlertDialog(String title, String msg, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick){
    showCustomAlertDialog(title, msg, false, posKey, posClick, negKey, negClick, null, null);
  }
  
  /**
   * Show custom alert selection dialog.
   *
   * @param title    the title
   * @param msg      the msg
   * @param posKey   the pos key
   * @param posClick the pos click
   * @param negKey   the neg key
   * @param negClick the neg click
   */
  public void showCustomAlertSelectionDialog(String title, String msg, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick){
    showCustomAlertSelectionDialog(title, msg, posKey, posClick, negKey, negClick, null, null);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   * @param negClick     the neg click
   */
  public void showCustomAlertDialog(String title, String msg, final View view, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes, DialogInterface.OnClickListener negClick){
    if(isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes))
      showCustomAlertDialog(title, msg, view, getString(posKeyStrRes), posClick, getString(negKeyStrRes), negClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msgStrRes    the msg str res
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   * @param negKeyStrRes the neg key str res
   */
  public void showCustomAlertDialog(String title, @StringRes int msgStrRes, final View view, @StringRes int posKeyStrRes, @StringRes int negKeyStrRes){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes))
      showCustomAlertDialog(title, getString(msgStrRes), view, getString(posKeyStrRes), getString(negKeyStrRes));
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   * @param negKeyStrRes the neg key str res
   */
  public void showCustomAlertDialog(String title, String msg, final View view, @StringRes int posKeyStrRes, @StringRes int negKeyStrRes){
    if(isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes))
      showCustomAlertDialog(title, msg, view, getString(posKeyStrRes), getString(negKeyStrRes));
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title  the title
   * @param msg    the msg
   * @param view   the view
   * @param posKey the pos key
   * @param negKey the neg key
   */
  public void showCustomAlertDialog(String title, String msg, final View view, String posKey, String negKey){
    showCustomAlertDialog(title, msg, view, posKey, null, negKey);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msgStrRes    the msg str res
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   */
  public void showCustomAlertDialog(String title, @StringRes int msgStrRes, final View view, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes))
      showCustomAlertDialog(title, getString(msgStrRes), view, getString(posKeyStrRes), posClick, getString(negKeyStrRes));
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   */
  public void showCustomAlertDialog(String title, String msg, final View view, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes){
    if(isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes))
      showCustomAlertDialog(title, msg, view, getString(posKeyStrRes), posClick, getString(negKeyStrRes));
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title    the title
   * @param msg      the msg
   * @param view     the view
   * @param posKey   the pos key
   * @param posClick the pos click
   * @param negKey   the neg key
   */
  public void showCustomAlertDialog(String title, String msg, final View view, String posKey, DialogInterface.OnClickListener posClick, String negKey){
    showCustomAlertDialog(title, msg, view, posKey, posClick, negKey, null);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title    the title
   * @param msg      the msg
   * @param view     the view
   * @param posKey   the pos key
   * @param posClick the pos click
   * @param negKey   the neg key
   * @param negClick the neg click
   */
  public void showCustomAlertDialog(String title, String msg, final View view, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick){
    showCustomAlertDialog(title, msg, view, null, false, posKey, posClick, negKey, negClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title     the title
   * @param msg       the msg
   * @param view      the view
   * @param isSuccess the is success
   * @param posKey    the pos key
   * @param posClick  the pos click
   * @param negKey    the neg key
   * @param negClick  the neg click
   */
  public void showCustomAlertDialog(String title, String msg, final View view, Boolean isSuccess, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick){
    showCustomAlertDialog(title, msg, view, isSuccess, false, posKey, posClick, negKey, negClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param view          the view
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   * @param posClick      the pos click
   * @param negKey        the neg key
   * @param negClick      the neg click
   */
  public void showCustomAlertDialog(String title, String msg, final View view, boolean isAutoDismiss, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick){
    showCustomAlertDialog(title, msg, view, null, isAutoDismiss, posKey, posClick, negKey, negClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param view          the view
   * @param isSuccess     the is success
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   * @param posClick      the pos click
   * @param negKey        the neg key
   * @param negClick      the neg click
   */
  public void showCustomAlertDialog(String title, String msg, final View view, Boolean isSuccess, boolean isAutoDismiss, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick){
    showCustomAlertDialog(title, msg, view, isSuccess, isAutoDismiss, posKey, posClick, negKey, negClick, null, null);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the titleF
   * @param msgStrRes    the msg str res
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   * @param negClick     the neg click
   * @param neuKeyStrRes the neu key str res
   * @param neuClick     the neu click
   */
  public void showCustomAlertDialog(String title, @StringRes int msgStrRes, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes, DialogInterface.OnClickListener negClick, @StringRes int neuKeyStrRes, DialogInterface.OnClickListener neuClick){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes) && isNonZeroId(neuKeyStrRes))
      showCustomAlertDialog(title, getString(msgStrRes), getString(posKeyStrRes), posClick, getString(negKeyStrRes), negClick, getString(neuKeyStrRes), neuClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   * @param negClick     the neg click
   * @param neuKeyStrRes the neu key str res
   * @param neuClick     the neu click
   */
  public void showCustomAlertDialog(String title, String msg, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes, DialogInterface.OnClickListener negClick, @StringRes int neuKeyStrRes, DialogInterface.OnClickListener neuClick){
    if(isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes) && isNonZeroId(neuKeyStrRes))
      showCustomAlertDialog(title, msg, getString(posKeyStrRes), posClick, getString(negKeyStrRes), negClick, getString(neuKeyStrRes), neuClick);
  }
  
  /**
   * Show custom alert selection dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   * @param negClick     the neg click
   * @param neuKeyStrRes the neu key str res
   * @param neuClick     the neu click
   */
  public void showCustomAlertSelectionDialog(String title, String msg, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes, DialogInterface.OnClickListener negClick, @StringRes int neuKeyStrRes, DialogInterface.OnClickListener neuClick){
    if(isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes) && isNonZeroId(neuKeyStrRes))
      showCustomAlertSelectionDialog(title, msg, getString(posKeyStrRes), posClick, getString(negKeyStrRes), negClick, getString(neuKeyStrRes), neuClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title    the title
   * @param msg      the msg
   * @param posKey   the pos key
   * @param posClick the pos click
   * @param negKey   the neg key
   * @param negClick the neg click
   * @param neuKey   the neu key
   * @param neuClick the neu click
   */
  public void showCustomAlertDialog(String title, String msg, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick, String neuKey, DialogInterface.OnClickListener neuClick){
    showCustomAlertDialog(title, msg, false, posKey, posClick, negKey, negClick, neuKey, neuClick);
  }
  
  /**
   * Show custom alert selection dialog.
   *
   * @param title    the title
   * @param msg      the msg
   * @param posKey   the pos key
   * @param posClick the pos click
   * @param negKey   the neg key
   * @param negClick the neg click
   * @param neuKey   the neu key
   * @param neuClick the neu click
   */
  public void showCustomAlertSelectionDialog(String title, String msg, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick, String neuKey, DialogInterface.OnClickListener neuClick){
    showCustomAlertDialog(title, msg, null, null, false, posKey, posClick, negKey, negClick, neuKey, neuClick, true);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title     the title
   * @param msg       the msg
   * @param isSuccess the is success
   * @param posKey    the pos key
   * @param posClick  the pos click
   * @param negKey    the neg key
   * @param negClick  the neg click
   * @param neuKey    the neu key
   * @param neuClick  the neu click
   */
  public void showCustomAlertDialog(String title, String msg, Boolean isSuccess, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick, String neuKey, DialogInterface.OnClickListener neuClick){
    showCustomAlertDialog(title, msg, isSuccess, false, posKey, posClick, negKey, negClick, neuKey, neuClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   * @param posClick      the pos click
   * @param negKey        the neg key
   * @param negClick      the neg click
   * @param neuKey        the neu key
   * @param neuClick      the neu click
   */
  public void showCustomAlertDialog(String title, String msg, boolean isAutoDismiss, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick, String neuKey, DialogInterface.OnClickListener neuClick){
    showCustomAlertDialog(title, msg, (Boolean) null, isAutoDismiss, posKey, posClick, negKey, negClick, neuKey, neuClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param isSuccess     the is success
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   * @param posClick      the pos click
   * @param negKey        the neg key
   * @param negClick      the neg click
   * @param neuKey        the neu key
   * @param neuClick      the neu click
   */
  public void showCustomAlertDialog(String title, String msg, Boolean isSuccess, boolean isAutoDismiss, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick, String neuKey, DialogInterface.OnClickListener neuClick){
    showCustomAlertDialog(title, msg, null, isSuccess, isAutoDismiss, posKey, posClick, negKey, negClick, neuKey, neuClick, false);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msgStrRes    the msg str res
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   * @param negClick     the neg click
   * @param neuKeyStrRes the neu key str res
   * @param neuClick     the neu click
   */
  public void showCustomAlertDialog(String title, @StringRes int msgStrRes, final View view, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes, DialogInterface.OnClickListener negClick, @StringRes int neuKeyStrRes, DialogInterface.OnClickListener neuClick){
    if(isNonZeroId(msgStrRes) && isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes) && isNonZeroId(neuKeyStrRes))
      showCustomAlertDialog(title, getString(msgStrRes), view, getString(posKeyStrRes), posClick, getString(negKeyStrRes), negClick, getString(neuKeyStrRes), neuClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title        the title
   * @param msg          the msg
   * @param view         the view
   * @param posKeyStrRes the pos key str res
   * @param posClick     the pos click
   * @param negKeyStrRes the neg key str res
   * @param negClick     the neg click
   * @param neuKeyStrRes the neu key str res
   * @param neuClick     the neu click
   */
  public void showCustomAlertDialog(String title, String msg, final View view, @StringRes int posKeyStrRes, DialogInterface.OnClickListener posClick, @StringRes int negKeyStrRes, DialogInterface.OnClickListener negClick, @StringRes int neuKeyStrRes, DialogInterface.OnClickListener neuClick){
    if(isNonZeroId(posKeyStrRes) && isNonZeroId(negKeyStrRes) && isNonZeroId(neuKeyStrRes))
      showCustomAlertDialog(title, msg, view, getString(posKeyStrRes), posClick, getString(negKeyStrRes), negClick, getString(neuKeyStrRes), neuClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title    the title
   * @param msg      the msg
   * @param view     the view
   * @param posKey   the pos key
   * @param posClick the pos click
   * @param negKey   the neg key
   * @param negClick the neg click
   * @param neuKey   the neu key
   * @param neuClick the neu click
   */
  public void showCustomAlertDialog(String title, String msg, final View view, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick, String neuKey, DialogInterface.OnClickListener neuClick){
    showCustomAlertDialog(title, msg, view, null, posKey, posClick, negKey, negClick, neuKey, neuClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title     the title
   * @param msg       the msg
   * @param view      the view
   * @param isSuccess the is success
   * @param posKey    the pos key
   * @param posClick  the pos click
   * @param negKey    the neg key
   * @param negClick  the neg click
   * @param neuKey    the neu key
   * @param neuClick  the neu click
   */
  public void showCustomAlertDialog(String title, String msg, final View view, Boolean isSuccess, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick, String neuKey, DialogInterface.OnClickListener neuClick){
    showCustomAlertDialog(title, msg, view, isSuccess, false, posKey, posClick, negKey, negClick, neuKey, neuClick);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title     the title
   * @param msg       the msg
   * @param view      the view
   * @param isSuccess the is success
   * @param posKey    the pos key
   * @param posClick  the pos click
   * @param negKey    the neg key
   * @param negClick  the neg click
   * @param neuKey    the neu key
   * @param neuClick  the neu click
   */
  public void showCustomAlertDialog(String title, String msg, final View view, Boolean isSuccess, boolean isAutoDismiss, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick, String neuKey, DialogInterface.OnClickListener neuClick){
    showCustomAlertDialog(title, msg, view, isSuccess, isAutoDismiss, posKey, posClick, negKey, negClick, neuKey, neuClick, false);
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param view          the view
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   * @param posClick      the pos click
   * @param negKey        the neg key
   * @param negClick      the neg click
   * @param neuKey        the neu key
   * @param neuClick      the neu click
   */
  public void showCustomAlertDialog(String title, String msg, final View view, boolean isAutoDismiss, String posKey, DialogInterface.OnClickListener posClick, String negKey, DialogInterface.OnClickListener negClick, String neuKey, DialogInterface.OnClickListener neuClick){
    showCustomAlertDialog(title, msg, view, null, isAutoDismiss, posKey, posClick, negKey, negClick, neuKey, neuClick, false);
  }
  
  /**
   * Dismiss custom alert dialog.
   */
  public void dismissCustomAlertDialog(){
    if(customAlertDialog != null && customAlertDialog.isShowing()) customAlertDialog.dismiss();
  }
  
  /**
   * Show custom alert dialog.
   *
   * @param title         the title
   * @param msg           the msg
   * @param view          the view
   * @param isSuccess     the is success
   * @param isAutoDismiss the is auto dismiss
   * @param posKey        the pos key
   * @param posClick      the pos click
   * @param negKey        the neg key
   * @param negClick      the neg click
   * @param neuKey        the neu key
   * @param neuClick      the neu click
   */
  public void showCustomAlertDialog(final String title, final String msg, final View view, final Boolean isSuccess, final boolean isAutoDismiss, final String posKey, final DialogInterface.OnClickListener posClick, final String negKey, final DialogInterface.OnClickListener negClick, final String neuKey, final DialogInterface.OnClickListener neuClick, final boolean isSelectionDialog){
    runOnUiThread(() -> {
      final ImageView imgLogo = isSuccess != null && isNullOrEmpty(title) ? new ImageView(CommonActivity.this) : null;
      if(imgLogo != null){
        imgLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgLogo.setImageResource(isSuccess ? R.drawable.ic_success : R.drawable.ic_error);
      }
      AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, isSelectionDialog ? R.style.AlertDialogSelection : R.style.AlertDialog));
      builder.setCancelable(false);
      if(customAlertDialog != null && customAlertDialog.isShowing()) customAlertDialog.dismiss();
      
      if(isNonEmpty(msg))
        builder.setMessage(HtmlCompat.fromHtml(msg.replaceAll("\n", "<br/>"), HtmlCompat.FROM_HTML_MODE_LEGACY));
      if(view != null /*&& (isNonEmpty(title) || !(view instanceof ImageView))*/){
        if(isNonEmpty(msg) && view instanceof InputView)
          view.setPadding(0, getResources().getDimensionPixelSize(R.dimen.dp_5), 0, 0);
        builder.setView(view);
      }
      if(isNonEmpty(posKey)) builder.setPositiveButton(posKey, posClick);
      if(isNonEmpty(negKey)) builder.setNegativeButton(negKey, negClick);
      if(isNonEmpty(neuKey)) builder.setNeutralButton(neuKey, neuClick);
      customAlertDialog = builder.create();
      customAlertDialog.setCancelable(false);
      if(isNonEmpty(title)) setAlertDialogCustomTitle(customAlertDialog, title, isSuccess);
      else if(isNullOrEmpty(title) && imgLogo != null) customAlertDialog.setCustomTitle(imgLogo);
      final CountDownTimer autoDismissTimer = new CountDownTimer(2500, 2500){
        @Override
        public void onTick(long l){
          //do nothing
        }
        
        @Override
        public void onFinish(){
          if(customAlertDialog != null && posKey != null && posClick != null){
            posClick.onClick(customAlertDialog, DialogInterface.BUTTON_POSITIVE);
          }
          if(customAlertDialog != null) customAlertDialog.dismiss();
        }
      };
      customAlertDialog.setOnShowListener(dialog -> {
        if(chkNotNullTrue(isSuccess)) successBeep();
        else if(chkNotNullFalse(isSuccess)) errorBeep();
        if(isNonEmpty(msg)){
          final TextView txtMsg = (TextView) customAlertDialog.findViewById(android.R.id.message);
          if(txtMsg != null){
            setTextAppearance(txtMsg, R.style.TextStyleRegular);
            
          }
        }
        if(view != null && view instanceof InputView){
          FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
          int mar = getResources().getDimensionPixelSize(R.dimen.dp_15);
          lp.setMargins(mar, 0, mar, mar);
          view.setLayoutParams(lp);
        }
        else if(imgLogo != null){
          DisplayMetrics displayMetrics = new DisplayMetrics();
          getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
          final int wid = (isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) / 7;
          LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(wid, wid);
          int mar = getResources().getDimensionPixelSize(R.dimen.dp_5);
          lp.setMargins(mar, mar, mar, mar);
          lp.gravity = Gravity.CENTER;
          imgLogo.setLayoutParams(lp);
        }
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(dp2px(85), dp2px(35));
        int margin = getResources().getDimensionPixelSize(R.dimen.dp_5);
        llParams.setMargins(margin, 0, margin, 0);
        llParams.gravity = Gravity.CENTER_HORIZONTAL;
        if(posKey != null){
          final Button pos = customAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
          ((LinearLayout) pos.getParent()).setGravity(Gravity.CENTER);
          pos.setLayoutParams(llParams);
          if(view instanceof InputView && posClick == null){
            final InputView edtTxt = (InputView) view;
            pos.setOnClickListener(view1 -> {
              edtTxt.clearError();
              if(edtTxt.validate()){
                dialog.dismiss();
                edtTxt.performBtnClick();
              }
            });
          }
        }
        if(negKey != null){
          final Button neg = customAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
          ((LinearLayout) neg.getParent()).setGravity(Gravity.CENTER);
          neg.setLayoutParams(llParams);
          neg.setTextColor(isSelectionDialog ? getResources().getColor(R.color.txt_button) : colorPrimaryDarkFromTheme);
        }
        if(neuKey != null){
          final Button neu = customAlertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
          neu.setLayoutParams(llParams);
          neu.setTextColor(isSelectionDialog ? getResources().getColor(R.color.txt_button) : colorPrimaryDarkFromTheme);
        }
        if(isAutoDismiss && isNonEmpty(posKey) && isNullOrEmpty(negKey) && isNullOrEmpty(neuKey) && posKey.equalsIgnoreCase(getString(R.string.btn_ok))){
          if(autoDismissTimer != null) autoDismissTimer.start();
        }
      });
      customAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
        @Override
        public void onDismiss(DialogInterface dialogInterface){
          if(autoDismissTimer != null) autoDismissTimer.cancel();
        }
      });
      customAlertDialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
          handleTriggerKeyEvent(keyCode, event);
          return false;
        }
      });
      if(view == null){// && !msg.toLowerCase().replaceAll(" ", "").contains("logout")){
        Window window = customAlertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
      }
      customAlertDialog.show();
    });
  }
  
  /**
   * Get color code string.
   *
   * @param colorId the color id
   * @return the string
   */
  public String getColorCode(@ColorRes int colorId){
    if(colorId != 0){
      String hexString = Integer.toHexString(getResources().getColor(colorId)).toUpperCase();
      return "#" + (hexString.length() == 8 && hexString.startsWith("FF") ? hexString.replaceFirst("FF", "") : hexString);
    }
    return "";
  }
  
  /**
   * Set tooltip text.
   *
   * @param view             the view
   * @param toolTipTextResId the tool tip text res id
   */
  public void setTooltipText(final View view, int toolTipTextResId){
    final String toolTipText = toolTipTextResId > 0 ? getString(toolTipTextResId) : null;
    setTooltipText(view, toolTipText);
  }
  
  /**
   * Set tooltip text.
   *
   * @param view        the view
   * @param toolTipText the tool tip text
   */
  public void setTooltipText(final View view, final CharSequence toolTipText){
    if(view != null && toolTipText != null){
      view.setOnClickListener(view1 -> {
        TooltipCompat.setTooltipText(view1, toolTipText);
        view1.performLongClick();
        new Handler(Looper.getMainLooper()).postDelayed(() -> TooltipCompat.setTooltipText(view1, null), 2000);
      });
    }
  }
  
  /**
   * Set text appearance.
   *
   * @param textView the text view
   * @param styleId  the style id
   */
  public void setTextAppearance(TextView textView, int styleId){
    if(textView != null && styleId > 0){
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) textView.setTextAppearance(styleId);
      else textView.setTextAppearance(this, styleId);
    }
  }
  
  protected void checkVersionUpdates(){
    try{
      JSONObject request = new JSONObject();
      request.put(ParamConstants.VERSION, BuildConfig.VERSION_NAME);
      if(isDebugApp)
        handleResponse(URLConstants.CHECK_FOR_HARDWARE_UPDATE, request, getSampleJSON(this, URLConstants.CHECK_FOR_HARDWARE_UPDATE), 200, true, null);
      else
        callWebService(URLConstants.CHECK_FOR_HARDWARE_UPDATE/*URLConstants.CHECK_FOR_UPDATE*/, request, getString(R.string.progress_msg_getting_data));
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Insert search log.
   *
   * @param sessionId the session id
   * @param ean       the ean
   * @param eanQty    the ean qty
   * @param type      the type
   */
  public SearchLog insertSearchLog(String sessionId, String ean, Integer eanQty, String type){
    return insertSearchLog(new SearchLog(sessionId, ean, eanQty, type));
  }
  
  /**
   * Insert search log.
   *
   * @param sessionId the session id
   * @param ean       the ean
   * @param eanQty    the ean qty
   * @param type      the type
   * @param subType   the sub type
   */
  public SearchLog insertSearchLog(String sessionId, String ean, Integer eanQty, String type, String subType, JSONObject params){
    return insertSearchLog(new SearchLog(sessionId, ean, eanQty, type, subType, params != null ? params.toString() : ""));
  }
  
  /**
   * Insert search log.
   *
   * @param sessionId the session id
   * @param ean       the ean
   * @param eanQty    the ean qty
   * @param type      the type
   * @param subType   the sub type
   * @param typeId    the type id
   */
  public SearchLog insertSearchLog(String sessionId, String ean, Integer eanQty, String type, String subType, String typeId, JSONObject params){
    return insertSearchLog(new SearchLog(sessionId, ean, eanQty, type, subType, typeId, params != null ? params.toString() : ""));
  }
  
  /**
   * Insert search log.
   *
   * @param searchLog the search log
   */
  public SearchLog insertSearchLog(SearchLog searchLog){
    try{
      searchLog.transactionId = new SimpleDateFormat(AppCommonMethods.LOG_ID_DATE_TIME_FORMAT).format(new Date());
      searchLog.transactionDateTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date());
      searchLog.sessionUserId = SharedPrefManager.getUserID();
      AppDatabase.getSearchLogDao(this).insert(searchLog);
    }
    catch(Exception e){ e.printStackTrace(); }
    return searchLog;
  }
  
  /**
   * Insert search log.
   *
   * @param auditTrailsLog the auditTrails log
   */
  private void insertAuditTrailsLog(final AuditTrailsLog auditTrailsLog){
    if(AppCommonMethods.isDebugApp){
      new AsyncTask<Void, Void, Void>(){
        @Override
        protected Void doInBackground(Void... voids){
          try{
            final String lastAction = AppDatabase.getAuditTrailsDao(CommonActivity.this).getLastAction();
            //if(!auditTrailsLog.action.contains("_OFF") || (lastAction.contains("_ON") && lastAction.substring(0,lastAction.lastIndexOf("_")).equalsIgnoreCase(auditTrailsLog.action.substring(0,auditTrailsLog.action.lastIndexOf("_"))))){
            auditTrailsLog.transactionDateTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT_MILI).format(new Date());
            AppDatabase.getAuditTrailsDao(CommonActivity.this).insert(auditTrailsLog);
            //}
          }
          catch(Exception e){ e.printStackTrace(); }
          return null;
        }
      }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
  }
  
  /**
   * Insert search log.
   *
   * @param action the action
   * @param type   the type
   */
  public void insertAuditTrailsLog(String action, String type){
    insertAuditTrailsLog(new AuditTrailsLog(action, type));
  }
  
  /**
   * Insert search log.
   *
   * @param action the action
   * @param type   the type
   */
  public void insertAuditTrailsLog(String action, String type, String result){
    insertAuditTrailsLog(new AuditTrailsLog(action, type, result));
  }
  
  /**
   * Load fragment.
   *
   * @param fragment the fragment
   * @param args     the args
   */
  public void loadFragment(Fragment fragment, Bundle args){
    if(fragment != null && args != null) fragment.setArguments(args);
    loadFragment(fragment);
  }
  
  /**
   * Load fragment.
   *
   * @param fragment the fragment
   */
  public void loadFragment(Fragment fragment){
    if(fragment != null){
      showLog("loading", fragment.getClass().getSimpleName());
      if(fragment instanceof DialogFragment){
        ((DialogFragment) fragment).show(getSupportFragmentManager(), fragment.getClass().getSimpleName());
      }
      else{
        getSupportFragmentManager().popBackStack(fragment.getClass().getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if(fragment instanceof HomeFragment)
          getSupportFragmentManager().beginTransaction().add(R.id.nav_host_fragment_content_main, fragment, fragment.getClass().getSimpleName()).commit();
        else
          getSupportFragmentManager().beginTransaction().add(R.id.nav_host_fragment_content_main, fragment, fragment.getClass().getSimpleName()).addToBackStack(fragment.getClass().getSimpleName()).commit();
      }
    }
  }
  
  /**
   * Show progress dialog.
   *
   * @param messageResId the message res id
   */
  public void showProgressDialog(@StringRes int messageResId){
    AppCommonMethods.showProgressDialog(this, messageResId);
  }
  
  /**
   * Show progress dialog.
   *
   * @param message the message
   */
  public void showProgressDialog(String message){
    AppCommonMethods.showProgressDialog(CommonActivity.this, message);
  }
  
  /**
   * Hide progress dialog.
   */
  public void hideProgressDialog(){
    AppCommonMethods.hideProgressDialog(CommonActivity.this);
  }
  
  /**
   * Call web service.
   *
   * @param url the url
   */
  public void callWebService(String url){
    callWebService(url, "");
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param progressMsg the progress msg
   */
  public void callWebService(String url, final String progressMsg){
    callWebService(url, new JSONObject(), null, progressMsg);
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param args        the args
   * @param progressMsg the progress msg
   */
  public void callWebService(String url, final Bundle args, final String progressMsg){
    callWebService(url, new JSONObject(), args, false, progressMsg);
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param jsonRequest the json request
   */
  public void callWebService(String url, JSONObject jsonRequest){
    callWebService(url, jsonRequest, "");
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param jsonRequest the json request
   * @param progressMsg the progress msg
   */
  public void callWebService(String url, JSONObject jsonRequest, final String progressMsg){
    callWebService(url, jsonRequest, null, progressMsg);
  }
  
  /**
   * Call web service.
   *
   * @param url              the url
   * @param jsonRequest      the json request
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(String url, JSONObject jsonRequest, final String progressMsg, final boolean isOfflineProcess){
    callWebService(url, jsonRequest, null, progressMsg, isOfflineProcess);
  }
  
  /**
   * Call web service.
   *
   * @param url              the url
   * @param jsonRequest      the json request
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(String url, JSONObject jsonRequest, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    callWebService(url, jsonRequest, null, progressMsg, isOfflineProcess, isDBProcess);
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param jsonRequest the json request
   * @param args        the args
   * @param progressMsg the progress msg
   */
  public void callWebService(String url, JSONObject jsonRequest, final Bundle args, final String progressMsg){
    callWebService(url, jsonRequest, args, false, progressMsg);
  }
  
  /**
   * Call web service.
   *
   * @param url              the url
   * @param jsonRequest      the json request
   * @param args             the args
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(String url, JSONObject jsonRequest, final Bundle args, final String progressMsg, final boolean isOfflineProcess){
    callWebService(url, jsonRequest, args, false, progressMsg, isOfflineProcess, false);
  }
  
  /**
   * Call web service.
   *
   * @param url              the url
   * @param jsonRequest      the json request
   * @param args             the args
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(String url, JSONObject jsonRequest, final Bundle args, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    callWebService(url, jsonRequest, args, false, progressMsg, isOfflineProcess, isDBProcess);
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param jsonRequest the json request
   * @param args        the args
   * @param isRetry     the is retry
   * @param progressMsg the progress msg
   */
  public void callWebService(final String url, final JSONObject jsonRequest, final Bundle args, final boolean isRetry, final String progressMsg){
    callWebService(url, jsonRequest, args, isRetry, progressMsg, false, false);
  }
  
  /**
   * Call web service.
   *
   * @param url         the url
   * @param jsonRequest the json request
   * @param args        the args
   * @param isRetry     the is retry
   * @param progressMsg the progress msg
   */
  public void callWebService(final String url, final JSONObject jsonRequest, final Bundle args, final boolean isRetry, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    if(this instanceof LandingActivity)
      ((LandingActivity) this).getPinViewModel().callWebService(this, null, url, jsonRequest, args, isRetry, progressMsg, isOfflineProcess, isDBProcess);
    else if(this instanceof MainActivity)
      ((MainActivity) this).getRfidViewModel().callWebService(this, null, url, jsonRequest, args, isRetry, progressMsg, isOfflineProcess, isDBProcess);
  }
  
  /**
   * Get gson gson.
   *
   * @return the gson
   */
  public Gson getGSON(){ return AppCommonMethods.getGSON(); }
  
  public void copyDataBase(){
    if(SharedPrefManager.getIsAllowDBBackup()){
      new AsyncTask<Void, Void, Void>(){
        @Override
        protected Void doInBackground(Void... voids){
          try{
            final String DB_FILEPATH = "/data/data/" + getPackageName();//+"/databases/database.db";
            final File inputDir = new File(DB_FILEPATH, "databases");
            //final File inputFile = new File(inputDir, AppDatabase.DB_NAME);
            for(File inputFile : inputDir.listFiles(new FilenameFilter(){
              @Override
              public boolean accept(File dir, String name){
                return name.matches("(?i)^" + AppDatabase.DB_NAME + ".*$");
              }
            }))
              if(inputFile.exists() && inputFile.isFile() && inputFile.length() > 0){
                final File outputDir = new File(getBaseDirectory("DB"), inputDir.getName());
                outputDir.mkdirs();
                final File output = new File(outputDir, inputFile.getName());
                if(output.exists()) output.delete();
                copyFile(inputFile, outputDir);
              }
          }
          catch(Exception e){
            e.printStackTrace();
          }
          return null;
        }
      }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
  }
  
  private void copyFile(File inputFile, File outputDir){
    byte[] buffer = new byte[1024];
    OutputStream myOutput = null;
    int length;
    // Open your local db as the input stream
    InputStream myInput = null;
    try{
      myInput = new FileInputStream(inputFile);
      // outputfile
      final File output = new File(outputDir, inputFile.getName());
      if(output.exists()) output.delete();
      myOutput = new FileOutputStream(output);
      // transfer bytes from the inputfile to the
      AppCommonMethods.showLog("Copy DB", inputFile.getAbsolutePath() + "(" + inputFile.length() + ") to " + outputDir.getAbsolutePath());
      while((length = myInput.read(buffer)) > 0){
        myOutput.write(buffer, 0, length);
      }
      myOutput.close();
      myOutput.flush();
      myInput.close();
      AppCommonMethods.showLog("Copy DB", output.getAbsolutePath() + " (" + output.length() + ")");
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  public void saveTagWritePasswords(final JSONObject jsonResponse){
    if(jsonResponse == null) return;
    try{
      final JSONObject jsonAccessPassword = extractJSONObject(jsonResponse, ParamConstants.ACCESS_PASSWORD, extractJSONObject(jsonResponse, ParamConstants.ACCESS_PASS, jsonResponse));
      if(isNullOrEmpty(jsonAccessPassword)){
        callWebService(URLConstants.GET_ACCESS_PWD, new JSONObject(), null);
        return;
      }
      SharedPrefManager.setBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, extractBoolean(jsonAccessPassword, ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, extractBoolean(jsonResponse, ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, AppCommonMethods.isCheckEncPasswordBasedOnEPC)));
      SharedPrefManager.setBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_API, extractBoolean(jsonAccessPassword, ParamConstants.IS_CHECK_PASSWORD_BEFORE_API, extractBoolean(jsonResponse, ParamConstants.IS_CHECK_PASSWORD_BEFORE_API, AppCommonMethods.isCheckEncPasswordBeforeAPI)));
      SharedPrefManager.setBoolean(ParamConstants.IS_CHECK_PASSWORD_FIRST, extractBoolean(jsonAccessPassword, ParamConstants.IS_CHECK_PASSWORD_FIRST, extractBoolean(jsonResponse, ParamConstants.IS_CHECK_PASSWORD_FIRST, AppCommonMethods.isCheckEncPasswordFirst)));
      SharedPrefManager.setCurrentAccessPassword(extractString(jsonAccessPassword, ParamConstants.CURRENT_ACCESS_PASSWORD, extractString(jsonResponse, ParamConstants.CURRENT_ACCESS_PASSWORD, extractString(jsonAccessPassword, ParamConstants.CURRENT_ACCESS_PWD, extractString(jsonResponse, ParamConstants.CURRENT_ACCESS_PWD, isDebugApp ? "12345678" : "00000000")))));
      JSONArray oldPasswords = extractJSONArray(jsonAccessPassword, OLD_ACCESS_PASSWORDS, extractJSONArray(jsonAccessPassword, OLD_ACCESS_PWDS, extractJSONArray(jsonResponse, OLD_ACCESS_PASSWORDS, extractJSONArray(jsonResponse, OLD_ACCESS_PWDS))));
      ArrayList<String> listOldPasswords = new ArrayList<>(0);
      if(isNonEmpty(oldPasswords)){
        for(int i = 0; i < oldPasswords.length(); i++){
          try{
            final JSONObject oldPass = oldPasswords.get(i) != null && oldPasswords.get(i) instanceof JSONObject ? oldPasswords.getJSONObject(i) : null;
            final String oldPassword = oldPass != null ? extractString(oldPass, ParamConstants.OLD_PASSWORD, "") : oldPasswords.get(i) != null && oldPasswords.get(i) instanceof String ? oldPasswords.getString(i) : "";
            if(isNonEmpty(oldPassword)) listOldPasswords.add(oldPassword);
          }
          catch(Exception e){ e.printStackTrace(); }
        }
      }
      listOldPasswords.add(0, AppConstants.defaultTagZeroPassword);
      
      //Code to Add Half Pass combinations with Current Access Password
      final String currentAccessPassword = SharedPrefManager.getCurrentAccessPassword();
      AppCommonMethods.showLog("currentAccessPassword", currentAccessPassword);
      int size = listOldPasswords.size();
      for(int i = 0; i < size; i++){
        final String oldPass = listOldPasswords.get(i);
        if(isNonEmpty(oldPass) && oldPass.length() >= 8 && !oldPass.equalsIgnoreCase(currentAccessPassword)){
          final String halfOldPass = currentAccessPassword.substring(0, 4) + oldPass.substring(4);
          final String halfOldPass2 = oldPass.substring(0, 4) + currentAccessPassword.substring(4);
          final String halfZeroPass = AppConstants.defaultTagZeroPassword.substring(0, 4) + oldPass.substring(4);
          final String halfZeroPass2 = oldPass.substring(0, 4) + AppConstants.defaultTagZeroPassword.substring(4);
          if(!listOldPasswords.contains(halfOldPass)) listOldPasswords.add(halfOldPass);
          if(!listOldPasswords.contains(halfOldPass2)) listOldPasswords.add(halfOldPass2);
          if(!listOldPasswords.contains(halfZeroPass)) listOldPasswords.add(halfZeroPass);
          if(!listOldPasswords.contains(halfZeroPass2)) listOldPasswords.add(halfZeroPass2);
        }
      }
      SharedPrefManager.setOldAccessPasswords(listOldPasswords);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  protected void onDestroy(){
    if(appBroadcastReceiver != null) unregisterReceiver(appBroadcastReceiver);
    super.onDestroy();
  }
  
  @Override
  protected void onResume(){
    super.onResume();
    if(AppCommonMethods.isCheckPlayStoreUpdates && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && ((this instanceof LandingActivity && !SharedPrefManager.getIsLoggedIn()) || this instanceof MainActivity))
      checkAppUpdate();
    else if(AppCommonMethods.isCheckVersionUpdates && isNonEmpty(SharedPrefManager.getUpdateAPKPath()) && SharedPrefManager.getIsServerURLConfigured() && getSupportFragmentManager().getBackStackEntryCount() <= 0 && ((this instanceof LandingActivity && !SharedPrefManager.getIsLoggedIn()) || this instanceof MainActivity)){
      showCustomAlertDialog("", SharedPrefManager.getString(ParamConstants.APPLICATION_VERSION + "_" + ParamConstants.MESSAGE, getString(R.string.app_update_available_title)) + "\n" + getString(R.string.app_update_msg), getString(R.string.btn_update), new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which){
          installNewVersion(SharedPrefManager.getUpdateAPKPath());
        }
      });
    }
  }
  
  public String getImagePath(String barcode){
    final String br = AppCommonMethods.getLeftZeroReplacedString(this, barcode);
    String imagePath = "";
    File fl = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "images");
    //Log.e("IMAGEPATH", fl.getAbsolutePath());
    if(fl.exists() && fl.isDirectory()){
      String[] arr = fl.list(new FilenameFilter(){
        @Override
        public boolean accept(File file, String s){
          return s.contains(br);
        }
      });
      if(arr != null && arr.length > 0) imagePath = String.join(",", arr);
    }
    return imagePath;
  }
  
  //code for install version Updates received via API call
  public void installNewVersion(String location){
    AppCommonMethods.showLog("APKFileLocation", location);
    Intent installAPKIntent = new Intent(isUseInAppStorage ? Intent.ACTION_INSTALL_PACKAGE : Intent.ACTION_VIEW);
    installAPKIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    Uri fileUri = getUriFromFile(new File(location));
    if(installAPKIntent.getAction() == Intent.ACTION_VIEW)
      installAPKIntent.setDataAndType(fileUri, "application/vnd.android.package-archive");
    else installAPKIntent.setData(fileUri);
    startActivity(installAPKIntent);
  }
  
  //code for Integrating In-APP Updates for Play Store  --
  public void checkAppUpdate(){
    AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
    
    //Returns an intent object that you use to check for an update.
    Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
    
    //setup & register Install State Listener
    final InstallStateUpdatedListener listener = state -> {
      showLog("appUpdateInstallState_installStatus", "" + state.installStatus());
      showLog("appUpdateInstallState_installErrorCode", "" + state.installErrorCode());
      if(state.installStatus() == InstallStatus.INSTALLED){ }
      else if(state.installStatus() == InstallStatus.INSTALLING){ }
      else if(state.installStatus() == InstallStatus.DOWNLOADING){ }
      else if(state.installStatus() == InstallStatus.DOWNLOADED){
        // After the update is downloaded, show a notification
        // and request user confirmation to restart the app.
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.text1), "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.show();
      }
      else if(state.installStatus() == InstallStatus.CANCELED){ }
      else if(state.installStatus() == InstallStatus.FAILED){ }
      else if(state.installStatus() == InstallStatus.PENDING){ }
      else if(state.installStatus() == InstallStatus.UNKNOWN){ }
    };
    //Checks that the platform will allow the specified type of update.
    appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
      showLog("appUpdateInfo", appUpdateInfo.toString());
      if(appUpdateType >= 0 && appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
        // If an in-app update is already running, resume the update.
        startAppUpdate(appUpdateManager, appUpdateInfo, appUpdateType);
      }
      else if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
        // Request the update.
        if(appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
          appUpdateManager.registerListener(listener);
          startAppUpdate(appUpdateManager, appUpdateInfo, AppUpdateType.IMMEDIATE);
        }
        else if(appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)){
          appUpdateManager.registerListener(listener);
          startAppUpdate(appUpdateManager, appUpdateInfo, AppUpdateType.FLEXIBLE);
        }
      }
    });
    appUpdateInfoTask.addOnCompleteListener(appUpdateInfo -> {
      appUpdateType = -1;
      appUpdateManager.unregisterListener(listener);
    });
    appUpdateInfoTask.addOnFailureListener(appUpdateInfo -> {
      appUpdateType = -1;
      appUpdateManager.unregisterListener(listener);
    });
  }
  
  private void startAppUpdate(final AppUpdateManager appUpdateManager, final AppUpdateInfo appUpdateInfo, final int appUpdateType){
    try{
      this.appUpdateType = appUpdateType;
      appUpdateManager.startUpdateFlowForResult(
        // Pass the intent that is returned by 'getAppUpdateInfo()'.
        appUpdateInfo,
        // The current activity making the update request.
        this,
        // pass 'AppUpdateType' to newBuilder() for immediate/flexible updates.
        AppUpdateOptions.newBuilder(appUpdateType).setAllowAssetPackDeletion(false).build(),
        // Include a request code to later monitor this update request.
        APP_UPDATE_REQUEST_CODE);
    }
    catch(IntentSender.SendIntentException e){
      e.printStackTrace();
    }
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
    super.onActivityResult(requestCode, resultCode, data);
    if(requestCode == APP_UPDATE_REQUEST_CODE){
      if(resultCode == RESULT_OK){
        //App Update Successful
        showCustomErrDialog("Update Done!!");
      }
      else if(resultCode == RESULT_CANCELED){
        //App Update Cancelled
        //show Error
        showCustomErrDialog("Update Cancelled!!");
      }
      else if(resultCode == com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED){
        //App Update Failed
        //show Error
        showCustomErrDialog("Update Failed!!");
      }
    }
  }
  //--code for Integrating In-APP Updates for Play Store
  
  protected String getInvType(final int sessionType){
    if(sessionType == AppCommonMethods.SessionType.INVENTORY.getValue())
      return AppConstants.TAKE_STOCK;
    if(sessionType == AppCommonMethods.SessionType.ADD_INVENTORY.getValue())
      return AppConstants.ADD_STOCK;
    if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY.getValue() || sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY.getValue())
      return AppConstants.BRAND_STOCK;
    return "";
  }
  
  protected String getTypeCharCode(int sessionType){ return getTypeCharCode(AppCommonMethods.SessionType.get(sessionType)); }
  
  protected String getTypeCharCode(AppCommonMethods.SessionType sessionType){
    if(sessionType != null && sessionType.getValue() > 0)
      return sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION ? "C" : sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD ? sessionType.name().substring(1, 2).toUpperCase() : sessionType.name().substring(0, 1);
    return "";
  }
  
  /**
   * Handle response.
   *
   * @param url          the url
   * @param jsonRequest  the json request
   * @param jsonResponse the json response
   * @param responseCode the response code
   * @param isSuccess    the is success
   * @param args         the args
   */
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    //To be overridden in child/sub class
    
    try{
      switch(url){
        case URLConstants.CHECK_FOR_UPDATE:
        case URLConstants.CHECK_FOR_HARDWARE_UPDATE:
          if(isSuccess){
            final String errorMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            AppCommonMethods.showLog("errMsg" + url, errorMsg);
            if(isNonEmpty(errorMsg) && !errorMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
              showCustomErrDialog(errorMsg);
              return;
            }
            JSONObject jsonResponseData = extractJSONObject(jsonResponse, ParamConstants.DATA, jsonResponse);
            final String apkVersion = extractString(jsonResponseData, ParamConstants.APK_VER, "");
            final String downloadUrl1 = extractString(jsonResponseData, ParamConstants.FTP_PATH, extractString(jsonResponseData, ParamConstants.BUILD_URL, false && isDebugApp ? "http://43.204.72.42/" : "")).trim();
            final String apkName = downloadUrl1.matches("(?i)(^(http|https)://.*\\.apk$)") ? downloadUrl1.substring(downloadUrl1.lastIndexOf("/") + 1) : extractString(jsonResponseData, ParamConstants.APK_NAME, extractString(jsonResponseData, ParamConstants.BUILD_NAME, false && isDebugApp ? "Retail_240202_30.apk" : "")).trim();
            final String downloadUrl = downloadUrl1.contains(apkName) ? downloadUrl1.replace(apkName, "") : downloadUrl1;
            showLog("apk_fullFileUrl", downloadUrl + apkName);
            showLog("apkVer_appVer_compare", apkVersion + "==" + BuildConfig.VERSION_NAME + "? =>" + (apkVersion.replaceAll("[A-Za-z]", "").trim().compareTo(BuildConfig.VERSION_NAME)));
            final boolean isVersionMisMatch = isNullOrEmpty(apkVersion) || apkVersion.replaceAll("[A-Za-z]", "").trim().compareTo(BuildConfig.VERSION_NAME) > 0;
            if(!isVersionMisMatch){
              showCustomAlertDialog("", extractString(jsonResponseData, ParamConstants.MESSAGE, getString(R.string.app_update_no)), R.string.btn_ok);
              return;
            }
            if(!isValidUrl(downloadUrl) || !chkNull(apkName, "").trim().toLowerCase().endsWith(".apk")){
              showCustomAlertDialog("", extractString(jsonResponseData, ParamConstants.MESSAGE, getString(R.string.app_update_no)), R.string.btn_ok);
              //showCustomErrDialog(isNullOrEmpty(downloadUrl) ? R.string.app_update_err_url : R.string.app_update_err_file);
              return;
            }
            showCustomAlertDialog(extractString(jsonResponseData, ParamConstants.MESSAGE, getString(R.string.app_update_available_title)), getString(R.string.app_update_available_msg), null, getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialogInterface, int i){
                AppCommonMethods.callDownloadFile(CommonActivity.this, downloadUrl, apkName);
              }
            }, getString(R.string.btn_no));
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  public void handleTriggerKeyEvent(int keyCode, KeyEvent event){ }
  
  public void postFileWrite(final String filePath){
  
  }
  
  public void generateBarcode(final String inputValue, final ImageView img){
    img.setImageBitmap(generateBarcode(inputValue, img.getWidth(), img.getHeight()));
  }
  
  public Bitmap generateBarcode(final String inputValue, final int width, final int height){
    MultiFormatWriter mwriter = null;
    Bitmap bitmap = null;
    try{
      mwriter = new MultiFormatWriter();
      // Generating a barcode matrix
      BitMatrix matrix = mwriter.encode(inputValue, BarcodeFormat.CODE_128, width, height);
      
      // Creating a bitmap to represent the barcode
      bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
      
      // Iterating through the matrix and set pixels in the bitmap
      for(int i = 0; i < width; i++){
        for(int j = 0; j < height; j++){
          bitmap.setPixel(i, j, (matrix.get(i, j)) ? Color.BLACK : Color.WHITE);
        }
      }
    }
    catch(Exception e){
      Toast.makeText(this, "Exception $e", Toast.LENGTH_SHORT).show();
    }
    return bitmap;
  }
  
  /**
   * Register bluetooth event.
   */
  private void registerBluetoothEvent(){
    if(this instanceof MainActivity && SharedPrefManager.getBoolean(ParamConstants.HAS_BLUETOOTH_CONNECTION_REQUIREMENT) && !isFinishing()){
      bluetoothResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>(){
        @Override
        public void onActivityResult(ActivityResult result){
          if(result.getResultCode() == RESULT_OK){
            if(SharedPrefManager.getBoolean(ParamConstants.HAS_BLUETOOTH_PRINTING)) print();
            //showShortToast("Bluetooth On!");
          }
        }
      });
      showLog("bluetoothResultLauncher", "" + (bluetoothResultLauncher != null));
    }
  }
  
  public boolean checkPrinter(){
    return tscDll != null && tscDll.IsConnected;
  }
  
  public boolean print(){
    if(savedPrintValues == null) return false;
    boolean isPrintSuccess = print(savedPrintValues.getTitle(), savedPrintValues.getBarcode(), savedPrintValues.getListData(), savedPrintValues.getLength(), savedPrintValues.getUnit());
    if(isPrintSuccess && currentFragmentClassName.equalsIgnoreCase(ThanCuttingFragment.class.getSimpleName()))
      ((ThanCuttingFragment) getSupportFragmentManager().findFragmentByTag(ThanCuttingFragment.class.getSimpleName())).callUpdateAPI(savedPrintValues.getLength());
    return isPrintSuccess;
  }
  
  public boolean print(final String title, final String barcode, final List<LabelValues> listData, final String length, final String unit){
    if(isNullOrEmpty(title) && isNullOrEmpty(barcode) && isNullOrEmpty(listData)) return false;
    if(isPrinting){
      showLongToast(R.string.progress_msg_printing);
      return false;
    }
    isPrinting = true;
    savedPrintValues = null;
    //showProgressDialog(R.string.progress_msg_printing);
    boolean isPrintSuccess = false;
    if(tscDll == null) tscDll = new TSCActivity();
    try{
      if(getConnectedPrinter(title, barcode, listData, length, unit) && isNonEmpty(SharedPrefManager.getPrinterAddress())){
        String result = tscDll.openport(SharedPrefManager.getPrinterAddress());
        AppCommonMethods.showLog("result_openPort", "" + result);
        if(tscDll == null || !tscDll.IsConnected || !result.equalsIgnoreCase("1")){
          //tscDll.closeport(10);
          hideProgressDialog();
          showCustomErrDialog(R.string.err_printer_cmd_fail);//Printer Paired but Not Connected
          isPrinting = false;
          return false;
        }
        final int printWidth = 500;
        final int printHeight = printWidth / 5;
        final int printMargin = printWidth / 10;
        final int x = isNullOrEmpty(barcode) ? -10 : 5; // height adjustment //TODO calculate height based on Content
        
        //setup page width & height
        result = tscDll.setup(printWidth, printHeight / 2 + x, 4, 12, 0, 0, 0);
        AppCommonMethods.showLog("result_setup", "" + result);
        if(tscDll == null || !tscDll.IsConnected || !result.equalsIgnoreCase("1")){
          tscDll.closeport(10);
          hideProgressDialog();
          showCustomErrDialog(R.string.err_printer_cmd_fail);//Printer Paired but Not Connected
          isPrinting = false;
          return false;
        }
        
        tscDll.clearbuffer();
        
        String printResult = "";
        //writeData
        int y = printMargin;
        if(isNonEmpty(title)){
          printResult += tscDll.printerfont(printWidth / 2 - (printMargin * 2), printHeight / 2, "4", 0, 1, 1, title);
          y += printHeight / 2;
        }
        if(isNonEmpty(barcode)){
          printResult += tscDll.barcode(printWidth / 2 - (printMargin * 3), printHeight, "128", printHeight, 0, 0, 3, 3, barcode);
          y += printHeight + printMargin / 2;
        }
        if(isNonEmpty(listData)) for(LabelValues lv : listData){
          printResult += tscDll.printerfont(printMargin, y, "2", 0, 1, 1, lv.getLabel() + ": " + lv.getValue());
          //tscDll.printerfont(printWidth-printMargin, y, "3", 0, 1, 1, lv.getValue());
          //tscDll.printerfont(printWidth/2, y, "2", 0, 1, 1, lv.getValue());
          y += printHeight / 2;
        }
        //for moving page forward
        printResult += tscDll.printlabel(1, 1);
        AppCommonMethods.showLog("result_printLabel", "" + printResult);
        isPrintSuccess = chkNull(printResult, "").matches("^1+$");
        AppCommonMethods.showLog("isPrintSuccess", "" + isPrintSuccess);
        hideProgressDialog();
        tscDll.closeport(5000);
        isPrinting = false;
      }
      else{
        isPrinting = false;
        hideProgressDialog();
      }
    }
    catch(Exception e){
      hideProgressDialog();
      e.printStackTrace();
    }
    hideProgressDialog();
    return isPrintSuccess;
  }
  
  public boolean getConnectedPrinter(final String title, final String barcode, final List<LabelValues> listData, final String length, final String unit){
    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    boolean isBluetoothOn = mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    if(!isBluetoothOn){
      //showCustomErrDialog(String.format(getString(R.string.err_bluetooth_disabled), getTypeCharCode(AppCommonMethods.SessionType.SCAN)));
      showCustomAlertDialog("", String.format(getString(R.string.err_bluetooth_disabled), getTypeCharCode(AppCommonMethods.SessionType.SCAN)), null, false, false, getString(bluetoothResultLauncher != null ? R.string.btn_enable : R.string.btn_ok), new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialogInterface, int i){
          if(bluetoothResultLauncher != null){
            savedPrintValues = new PrintData(title, barcode, listData, length, unit);
            bluetoothResultLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
          }
        }
      }, bluetoothResultLauncher != null ? getString(R.string.btn_cancel) : "", null);
      return false;
    }
    else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED){
      Set<BluetoothDevice> listPairedDevices = mBluetoothAdapter.getBondedDevices();
      if(isNullOrEmpty(listPairedDevices)){//Printer Not Paired
        //showCustomErrDialog(R.string.err_printer_not_paired);
        showCustomAlertDialog("", getString(R.string.err_printer_not_paired), null, false, false, getString(bluetoothResultLauncher != null ? R.string.btn_settings_go : R.string.btn_ok), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            if(bluetoothResultLauncher != null){
              savedPrintValues = new PrintData(title, barcode, listData, length, unit);
              bluetoothResultLauncher.launch(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
            }
          }
        }, bluetoothResultLauncher != null ? getString(R.string.btn_cancel) : "", null);
        return false;
      }
      else{
        ArrayList<BluetoothDevice> listBTPairedDevices = new ArrayList<>(0);
        boolean isSavedPrinterFound = false;
        for(BluetoothDevice bd : listPairedDevices){
          AppCommonMethods.showLog("bd_name", bd.getName());
          if(isNonEmpty(SharedPrefManager.getPrinterAddress()) && SharedPrefManager.getPrinterAddress().equalsIgnoreCase(bd.getAddress())){
            isSavedPrinterFound = true;
            listBTPairedDevices.add(bd);
            break;
          }
          else if(bd.getName().matches("[0-9A-Z]{4,5}\\-[0-9A-Z]{3,4}"))
            listBTPairedDevices.add(bd);
        }
        if(isSavedPrinterFound) return true;
        else if(isNullOrEmpty(listBTPairedDevices)){//Printer Not Paired
          //showCustomErrDialog(R.string.err_printer_not_paired);
          showCustomAlertDialog("", getString(R.string.err_printer_not_paired), null, false, false, getString(bluetoothResultLauncher != null ? R.string.btn_settings_go : R.string.btn_ok), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              if(bluetoothResultLauncher != null){
                savedPrintValues = new PrintData(title, barcode, listData, length, unit);
                bluetoothResultLauncher.launch(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
              }
            }
          }, bluetoothResultLauncher != null ? getString(R.string.btn_cancel) : "", null);
          return false;
        }
        else if(listBTPairedDevices.size() == 1){
          BluetoothDevice bd = listBTPairedDevices.get(0);
          AppCommonMethods.showLog("bd_add", bd.getAddress());
          SharedPrefManager.setPrinterAddress(bd.getAddress());
          //print(isThanClosure,title,barcode,listData);
          return true;
        }
        else if(listPairedDevices.size() > 1){
          //showCustomErrDialog(R.string.err_printer_multiple_paired);
          showCustomAlertDialog("", getString(R.string.err_printer_multiple_paired), null, false, false, getString(bluetoothResultLauncher != null ? R.string.btn_settings_go : R.string.btn_ok), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              if(bluetoothResultLauncher != null){
                savedPrintValues = new PrintData(title, barcode, listData, length, unit);
                bluetoothResultLauncher.launch(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
              }
            }
          }, bluetoothResultLauncher != null ? getString(R.string.btn_cancel) : "", null);
          //TODO select printer radio list dialog
          return false;
        }
      }
    }
    else{
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) checkBluetoothPermissions();
      return false;
    }
    return false;
  }
  
  public void loadSessionFragment(String type, int activeUsers, int sessionValidTill, int target, Bundle args){ }
  
  public void handlePostRedirection(final String url, final Fragment fragment, final Bundle arguments){ }
}
