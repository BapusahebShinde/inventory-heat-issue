package com.itek.retail.ui.landing;

import static com.itek.retail.apis.ParamConstants.OLD_ACCESS_PASSWORDS;
import static com.itek.retail.common.AppCommonMethods.SERVER_URL_APPEND_API;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractDouble;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractLong;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isCallLoginAPI;
import static com.itek.retail.common.AppCommonMethods.isDCApp;
import static com.itek.retail.common.AppCommonMethods.isHandleNotificationDataFromIntent;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isSetInwOnline;
import static com.itek.retail.common.AppCommonMethods.isSetUserMgmt;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;
import static com.itek.retail.common.AppCommonMethods.successBeep;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.textfield.TextInputLayout;
import com.itek.retail.BuildConfig;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.LogFileUtilityHHD;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.ActivityLandingBinding;
import com.itek.retail.databinding.DialogProfileBinding;
import com.itek.retail.model.Brand;
import com.itek.retail.model.Category;
import com.itek.retail.model.DecodeType;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.Zone;
import com.itek.retail.sgtin.EPCEncoderDecoder;
import com.itek.retail.sgtin.SGTIN128Helper;
import com.itek.retail.ui.customviews.InputView;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.navmenu.AppInfoFragment;
import com.itek.retail.ui.resetpassword.ResetPasswordFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class LandingActivity extends CommonActivity implements PopupMenu.OnMenuItemClickListener{
  
  private final boolean isMenusLocked = false;
  PinViewModel pinViewModel;
  AppDatabase db;
  private ActivityLandingBinding binding;
  final TextWatcher clear = new TextWatcher(){
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){
      /*Empty Method (Default Overridden)*/
    }
    
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
      /*Empty Method (Default Overridden)*/
    }
    
    @Override
    public void afterTextChanged(Editable editable){
      clearError();
    }
  };
  
  @Override
  protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    if(BuildConfig.IS_DIAGNOSTIC_BUILD){
      SharedPrefManager.setServerUrl("https://diag.local/api/");
      SharedPrefManager.setIsServerURLConfigured(true);
      SharedPrefManager.setAccessToken("");
      SharedPrefManager.setAccessTokenTime(0L);
    }
    //Auto-Login
    if(SharedPrefManager.getIsLoggedIn()){
      Intent i = new Intent(LandingActivity.this, MainActivity.class);
      i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(i);
      finish();
    }
    binding = DataBindingUtil.setContentView(this, R.layout.activity_landing);
    pinViewModel = new PinViewModel(this);
    binding.setPinViewModel(pinViewModel);
    binding.executePendingBindings();
    db = AppDatabase.getDbInstance(this);
    
    binding.tilUserName.setVisibility(isSetUserMgmt ? View.VISIBLE : View.GONE);
    binding.tilPassword.setHint(isSetUserMgmt ? R.string.hint_password : R.string.hint_pin);
    if(!isSetUserMgmt){
      binding.edtPassword.setInputType(InputType.TYPE_CLASS_NUMBER);
      binding.edtPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
    }
    
    binding.edtUserName.addTextChangedListener(clear);
    binding.edtPassword.addTextChangedListener(clear);
    
    binding.edtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener(){
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
        if(actionId == EditorInfo.IME_ACTION_DONE){
          hideKeyboard();
          binding.btnProceed.performClick();
          return true;
        }
        return false;
      }
    });
    
    binding.btnProceed.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(isValid()){
          hideKeyboard();
          if(!SharedPrefManager.getIsServerURLConfigured()){
            
            binding.txtConfigUrl.performClick();
          }
          else{
            try{
              JSONObject requestParams = new JSONObject();
              if(!isCallLoginAPI){
                handleResponse(URLConstants.GET_STORE_DETAILS, requestParams, AppCommonMethods.getSampleJSON(LandingActivity.this, AppCommonMethods.isSafariApp ? "getStoreDetailsSafari" : AppCommonMethods.isDCAppTataBuild ? "getStoreDetailsDCTata" : "getStoreDetailsDC"), 200, true, null);
                return;
              }
              else if(!isSetUserMgmt) requestParams.put(ParamConstants.PIN, pinViewModel.getPin());
              else if(isSetUserMgmt){
                SharedPrefManager.setAccessTokenTime(1L);
                SharedPrefManager.setUserID(chkNull(binding.edtUserName.getText().toString(), "").trim());
                SharedPrefManager.setPassword(chkNull(binding.edtPassword.getText().toString(), "").trim());
                if(AppCommonMethods.isStaticDebug(LandingActivity.this))
                  requestParams.put(ParamConstants.PIN, "`1234");
                requestParams.put(ParamConstants.USER_NAME, chkNull(binding.edtUserName.getText().toString(), "").trim());
                requestParams.put(ParamConstants.PASSWORD, chkNull(binding.edtPassword.getText().toString(), "").trim());
              }
              requestParams.put(ParamConstants.FIREBASE_TOKEN, SharedPrefManager.getFirebaseToken());
              /*if(isDebugApp) handleResponse(URLConstants.GET_STORE_DETAILS, requestParams, AppCommonMethods.getSampleJSON(LandingActivity.this, "getStoreDetailsThan"), 200, true, null);
              else */
              callWebService(URLConstants.GET_STORE_DETAILS, requestParams, getString(isSetUserMgmt ? R.string.progress_msg_validate_user : R.string.progress_msg_validate_pin), false, true);
            }
            catch(JSONException e){
              e.printStackTrace();
            }
          }
        }
        //else
        
      }
    });
    
    binding.popup.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        PopupMenu popup = new PopupMenu(LandingActivity.this, view);
        popup.setOnMenuItemClickListener(LandingActivity.this);
        popup.inflate(R.menu.landing);
        popup.getMenu().findItem(R.id.imgLandingHeaderCheckUpadte).setVisible(SharedPrefManager.getIsServerURLConfigured() && !AppCommonMethods.isCheckPlayStoreUpdates && !AppCommonMethods.isCheckVersionUpdates);
        popup.show();
      }
    });
    
    SpannableString ss = new SpannableString(HtmlCompat.fromHtml(getString(isSetUserMgmt ? R.string.forgot_password : R.string.forgot_pin), HtmlCompat.FROM_HTML_MODE_LEGACY));
    int index = ss.toString().indexOf("?") + 2;
    ss.setSpan(new ClickableSpan(){
      @Override
      public void onClick(@NonNull View widget){
        if(isSetUserMgmt){
          if(isStaticDebug()){//resetPassword
            clearError();
            if(binding.tilUserName.getVisibility() == View.VISIBLE && isNullOrEmpty(binding.edtUserName.getText().toString().trim())){
              binding.tilUserName.setErrorEnabled(true);
              binding.tilUserName.setError(String.format(getString(R.string.field_err_empty), chkNull(binding.tilUserName.getPlaceholderText(), binding.tilUserName.getHint())));
            }
            else{
              showCustomAlertDialog(getString(R.string.lbl_reset_password), getString(R.string.msg_reset_password), getString(R.string.btn_reset), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                  try{
                    JSONObject requestParams = new JSONObject();
                    requestParams.put(ParamConstants.USER_NAME, chkNull(binding.edtUserName.getText().toString(), "").trim());
                    callWebService(URLConstants.RESET_PASSWORD, requestParams, getString(R.string.progress_msg_validate_data));
                  }
                  catch(Exception e){
                    e.printStackTrace();
                  }
                }
              }, getString(R.string.btn_cancel), null);
            }
          }
          else loadFragment(new ResetPasswordFragment());
        }
      }
    }, index, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    binding.txtForgotPin.setText(ss);
    binding.txtForgotPin.setMovementMethod(LinkMovementMethod.getInstance());
    //temp code
    binding.txtForgotPin.setVisibility(isStaticDebug() ? View.VISIBLE : View.GONE);
    binding.txtConfigUrl.setVisibility(/*!isSetUserMgmt ? View.VISIBLE :*/ View.GONE);
    
    binding.txtConfigUrl.setText(HtmlCompat.fromHtml(getString(R.string.config_server), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    binding.txtConfigUrl.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        InputView inputView = new InputView(LandingActivity.this);
        if(isDebugApp && !SharedPrefManager.getIsServerURLConfigured()){
          //SharedPrefManager.setServerUrl("https://retailtestdev.itekrfid.com/Stores/"); //Dev
          //SharedPrefManager.setServerUrl(isDCApp?"https://retailtest.itekrfid.com/zudio/dc":"https://retailtest.itekrfid.com/zudio/stores"); //Tata
          //SharedPrefManager.setServerUrl("https://vpay.itekrfid.com/"); //Samsonite
          //SharedPrefManager.setServerUrl("https://luluindiaqa.itekrfid.com");//LULU //"https://rftaarqa.itekrfid.com");//http://192.168.0.32:9030");
          //SharedPrefManager.setServerUrl("https://givaqa.garudavigil.com");//GIVA
          //SharedPrefManager.setServerUrl("https://eyewa.garudavigil.com");//EYEWA
          //SharedPrefManager.setServerUrl("https://njlqa.garudavigil.com/"); //ABFRL
          //SharedPrefManager.setServerUrl("https://ppagqa.garudavigil.com/"); //Pittappillil
          SharedPrefManager.setServerUrl("https://ppagdev.garudavigil.com"); //Pittappillil Dev
          // SharedPrefManager.setServerUrl("https://chlqa.garudavigil.com/"); //Chaudhary Lifestyle
        }
        inputView.setHint(R.string.hint_config_url);
        inputView.setLabel(R.string.lbl_server_url);
        inputView.setMinLen(8);
        inputView.setText(SharedPrefManager.getServerUrl().replaceFirst(SERVER_URL_APPEND_API, ""));
        inputView.setValidationRegex(AppConstants.REGEX_URL);
        
        inputView.setButtonClick(new View.OnClickListener(){
          @Override
          public void onClick(View view){
            String baseURL = inputView.getText().trim();
            baseURL = baseURL.endsWith("/") ? baseURL.substring(0, baseURL.length() - 1).trim() : baseURL;
            SharedPrefManager.setAccessTokenTime(1L);
            SharedPrefManager.setUserID("");
            SharedPrefManager.setPassword("");
            callWebService((baseURL.replaceAll(SERVER_URL_APPEND_API, "") + SERVER_URL_APPEND_API).trim(), getString(R.string.progress_msg_config_url));
          }
        });
        showCustomAlertDialog(getString(R.string.title_config_server), "", inputView, getString(R.string.btn_validate), getString(R.string.btn_cancel));
      }
    });
    
    if(isDebugApp && isSetUserMgmt){
      new Handler().postDelayed(new Runnable(){
        @Override
        public void run(){
          //TATA (Dev)
          //binding.edtUserName.setText("HM_Z215");
          //binding.edtPassword.setText("HM_Z215");
          //TATA (Zudio)
          //binding.edtUserName.setText("HM.Z294");//HM_Z215");//"HM_Z294");//"HM_Z215");
          //binding.edtPassword.setText("HM.Z294");//HM_Z215");//HM_Z294");//HM_Z215");
          //Samsonite
          //binding.edtUserName.setText("HM.TDS8");
          //binding.edtPassword.setText("HM.TDS8");
          //GIVA & LULU & ABFRL & Pittappillil & Chaudhary
          binding.edtUserName.setText("HM.ITTS");
          binding.edtPassword.setText("HM_1");//HM.ITTS");//""HM_2");
          //EYEWA
          //binding.edtUserName.setText("HM.DUBS");
          //binding.edtPassword.setText("HM_2");
        }
      }, 50);
    }
  }
  
  // field validation methods ---
  public void clearError(){
    binding.tilUserName.setError(null);
    binding.tilUserName.setErrorEnabled(false);
    binding.tilPassword.setError(null);
    binding.tilPassword.setErrorEnabled(false);
  }
  
  public boolean isValid(){
    clearError();
    if(!isSetUserMgmt) return validatePin();
    else return validate(binding.tilUserName) && validate(binding.tilPassword);
  }
  
  public PinViewModel getPinViewModel(){
    return pinViewModel;
  }
  
  private boolean validatePin(){
    if(pinViewModel.isValidPin()) return true;
    else if(binding.tilPassword != null){
      final CharSequence cs = binding.tilPassword.getPlaceholderText() != null ? binding.tilPassword.getPlaceholderText() : binding.tilPassword.getHint() != null ? binding.tilPassword.getHint() : "";
      final String errLbl = cs.toString().replaceAll("(?i)Please ", "").replaceAll("(?i)Enter ", "").trim().toLowerCase();
      updateError(binding.tilPassword, String.format(getString(R.string.field_err_pin_invalid), errLbl));
      return false;
    }
    return false;
  }
  
  private boolean validatePin(final TextInputLayout tilInput){
    if(tilInput == null || tilInput.getEditText() == null || chkNull(tilInput.getPlaceholderText(), tilInput.getHint()) == null)
      return false;
    clearError();
    if(tilInput.getVisibility() != View.VISIBLE) return true;
    final CharSequence cs = tilInput.getPlaceholderText() != null ? tilInput.getPlaceholderText() : tilInput.getHint() != null ? tilInput.getHint() : "";
    final EditText etInput = tilInput.getEditText();
    final String errLbl = cs.toString().replaceAll("(?i)Please ", "").replaceAll("(?i)Enter ", "").trim().toLowerCase();
    final String txt = chkNull(etInput.getText().toString(), "").trim();
    if(txt.length() == 0 || !pinViewModel.isValidPin(txt))
      updateError(tilInput, String.format(getString(R.string.field_err_pin_invalid), errLbl));
    return tilInput.getError() == null;
  }
  
  private boolean validate(final TextInputLayout tilInput){
    return validate(tilInput, "");
  }
  
  private boolean validate(final TextInputLayout tilInput, final String validationRegex){
    return validate(tilInput, validationRegex, 1);
  }
  
  private boolean validate(final TextInputLayout tilInput, final String validationRegex, final int minLen){
    if(tilInput == null || tilInput.getEditText() == null || chkNull(tilInput.getPlaceholderText(), tilInput.getHint()) == null)
      return false;
    clearError();
    if(tilInput.getVisibility() != View.VISIBLE) return true;
    final CharSequence cs = tilInput.getPlaceholderText() != null ? tilInput.getPlaceholderText() : tilInput.getHint() != null ? tilInput.getHint() : "";
    final EditText etInput = tilInput.getEditText();
    final String errLbl = cs.toString().replaceAll("(?i)Please ", "").replaceAll("(?i)Enter ", "").trim().toLowerCase();
    final String txt = chkNull(etInput.getText().toString(), "").trim();
    if(txt.length() == 0)
      updateError(tilInput, String.format(getString(R.string.field_err_empty), errLbl));
    else if(txt.length() < minLen || (isNonEmpty(validationRegex) && !txt.matches(validationRegex)))
      updateError(tilInput, String.format(getString(R.string.field_err_invalid), errLbl));
    return tilInput.getError() == null;
  }
  
  /**
   * Update error.
   *
   * @param error the error
   */
  private void updateError(final TextInputLayout tilInput, String error){
    if(isNonEmpty(error)){
      tilInput.setErrorEnabled(true);
      tilInput.setError(error);
    }
    else clearError();
  }
  
  // --- field validation methods
  
  @Override
  public void onBackPressed(){
    super.onBackPressed();
  }
  
  private void redirectToMain(){
    if(!db.RFIDSessionDao().hasExistingActiveSession(SharedPrefManager.getUserID())){
      SharedPrefManager.setDecodeAlertMsg("");
      db.InventoryDao().deleteAll();
      db.RFIDSessionDao().deleteAll();
    }
    SharedPrefManager.setIsLoggedIn(true);
    Intent i = new Intent(LandingActivity.this, MainActivity.class);
    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(i);
    finish();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponseBody, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponseBody, responseCode, isSuccess, args);
    try{
        switch(url){
        case URLConstants.GET_STORE_DETAILS:
          if(isSuccess){
            showLog("isReSuccess", "" + isSuccess);
            hideProgressDialog();
            SharedPrefManager.setIsShowCrashLog(extractBoolean(jsonResponseBody, ParamConstants.SHOW_CRASH_LOGS, true));
            SharedPrefManager.setIsAllowFileLogs(extractBoolean(jsonResponseBody, ParamConstants.SHOW_FILE_LOGS, true));
            SharedPrefManager.setMaxLogFiles(extractInt(jsonResponseBody, ParamConstants.MAX_LOG_FILES, LogFileUtilityHHD.fileCount));
            SharedPrefManager.setLogFileMaxSize(extractInt(jsonResponseBody, ParamConstants.MAX_LOG_FILE_SIZE, LogFileUtilityHHD.fileSize));
            LogFileUtilityHHD.setMaxFileCount();
            SharedPrefManager.setIsAllowDBBackup(extractBoolean(jsonResponseBody, ParamConstants.ALLOW_DB_BACKUP, true));
            
            try{
              SharedPrefManager.setReaderSDKVersion("");
              SharedPrefManager.setDeviceType(0);
              final String deviceTypeName = extractString(jsonResponseBody, ParamConstants.DEVICE_TYPE, AppCommonMethods.DeviceType.OTHER.name()).toUpperCase();
              AppCommonMethods.DeviceType deviceType = AppCommonMethods.DeviceType.valueOf(deviceTypeName);
              if(deviceType.getValue() > 0 && !Build.BRAND.toUpperCase().contains(deviceTypeName) && !Build.MANUFACTURER.toUpperCase().contains(deviceTypeName)){
                SharedPrefManager.setDeviceType(deviceType);
                for(AppCommonMethods.DeviceType dt : AppCommonMethods.DeviceType.values())
                  if(Build.BRAND.toUpperCase().contains(dt.name().toUpperCase()) || Build.MANUFACTURER.toUpperCase().contains(dt.name().toUpperCase())){
                    SharedPrefManager.setDeviceType(dt);
                    break;
                  }
              }
              else if(deviceType.getValue() > 0) SharedPrefManager.setDeviceType(deviceType);
              //if(isDebugApp) SharedPrefManager.setDeviceType(deviceType);
            }
            catch(Exception e){
              e.printStackTrace();
            }
            
            //Changes for Configurable Minimum Battery Percentage to Allow Reader Operations
            SharedPrefManager.setInt(ParamConstants.minBatteryPercentForReaderOperations, extractInt(jsonResponseBody, ParamConstants.minBatteryPercentForReaderOperations, 20));
            //SharedPrefManager.setInt(ParamConstants.maxBatteryPercentForReaderDisconnect,extractInt(jsonResponseBody,ParamConstants.maxBatteryPercentForReaderDisconnect,80));
            
            SharedPrefManager.setDeviceModel(extractString(jsonResponseBody, ParamConstants.DEVICE_MODEL, ""));
            SharedPrefManager.setTriggerKeyCodes(extractString(jsonResponseBody, ParamConstants.DEVICE_KEY_CODES, SharedPrefManager.getDeviceType() == AppCommonMethods.DeviceType.ZEBRA ? "" : SharedPrefManager.getDeviceType() == AppCommonMethods.DeviceType.CHAINWAY ? "139, 280, 293" : ""));
            SharedPrefManager.setIsDeviceBluetoothDependent(extractBoolean(jsonResponseBody, ParamConstants.IS_DEVICE_BLUETOOTH_DEPENDENT, (SharedPrefManager.getDeviceType() == AppCommonMethods.DeviceType.HONEYWELL) || (SharedPrefManager.getDeviceType() == AppCommonMethods.DeviceType.CHAINWAY && !Build.MANUFACTURER.equalsIgnoreCase(AppCommonMethods.DeviceType.CHAINWAY.name())) || (SharedPrefManager.getDeviceType() == AppCommonMethods.DeviceType.ZEBRA && Build.VERSION.SDK_INT < Build.VERSION_CODES.R)));
            SharedPrefManager.setReplenishmentType(extractString(jsonResponseBody, ParamConstants.REPLENISHMENT_TYPE, AppConstants.REPLENISH_TYPE_BOTH));
            SharedPrefManager.setOmnichannelType(extractString(jsonResponseBody, ParamConstants.OMNICHANNEL_TYPE, AppConstants.OMNI_TYPE_BOTH));
            SharedPrefManager.setUnencodedSearchType(extractString(jsonResponseBody, ParamConstants.UNENCODED_SEARCH_TYPE, AppConstants.UNENCODED_SEARCH_TYPE_OFFLINE));
            SharedPrefManager.setAlienSearchType(extractString(jsonResponseBody, ParamConstants.ALIEN_SEARCH_TYPE, AppConstants.ALIEN_SEARCH_TYPE_OFFLINE));
            SharedPrefManager.setEanType(extractString(jsonResponseBody, ParamConstants.EAN_TYPE, AppConstants.EAN_TYPE_BOTH));
            
            //Flags for Allowing Barcodes with Leading Zero
            SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_LEADING_ZERO_FOR_NON_STD_BARCODE, extractBoolean(jsonResponseBody, ParamConstants.IS_ALLOW_LEADING_ZERO_FOR_NON_STD_BARCODE, AppCommonMethods.isIsAllowLeadingZeroForNonStdBarcode));
            SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_LEADING_ZERO_FOR_STD_BARCODE, extractBoolean(jsonResponseBody, ParamConstants.IS_ALLOW_LEADING_ZERO_FOR_STD_BARCODE, AppCommonMethods.isIsAllowLeadingZeroForStdBarcode));
            
            SharedPrefManager.setClientID(extractString(jsonResponseBody, ParamConstants.CLIENT_ID, "0"));
            SharedPrefManager.setClientName(extractString(jsonResponseBody, ParamConstants.CLIENT_NAME, ""));
            SharedPrefManager.setStoreType(extractString(jsonResponseBody, ParamConstants.STORE_TYPE, extractString(jsonResponseBody, ParamConstants.SUPPLY_CHAIN_TYPE, extractString(jsonResponseBody, ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_NAME, extractString(jsonResponseBody, ParamConstants.SUPPLY_CHAIN_TYPE_NAME, extractString(jsonResponseBody, ParamConstants.TYPE, isDCApp ? "DC" : "Store"))))));
            SharedPrefManager.setStoreID(extractString(jsonResponseBody, ParamConstants.STORE_ID));
            SharedPrefManager.setStoreCode(extractString(jsonResponseBody, ParamConstants.STORE_CODE, ""));
            SharedPrefManager.setStoreName(extractString(jsonResponseBody, ParamConstants.STORE_NAME, ""));
            
            SharedPrefManager.setLong(ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_ID, extractLong(jsonResponseBody, ParamConstants.SUPPLY_CHAIN_TYPE_ID, extractLong(jsonResponseBody, ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_ID)));
            SharedPrefManager.setString(ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_NAME, extractString(jsonResponseBody, ParamConstants.SUPPLY_CHAIN_TYPE_NAME, extractString(jsonResponseBody, ParamConstants.SUPPLY_CHAIN_TYPE_MASTER_NAME)));
            SharedPrefManager.setBoolean(ParamConstants.IS_RFID_STORE, extractBoolean(jsonResponseBody, ParamConstants.IS_RFID_STORE, false));
            
            SharedPrefManager.setDouble(ParamConstants.TIME_ZONE_OFFSET_HOURS, extractDouble(jsonResponseBody, ParamConstants.TIME_ZONE_OFFSET_HOURS, 0.00));
            
            if(!isSetUserMgmt)
              SharedPrefManager.setUserID(extractString(jsonResponseBody, ParamConstants.USER_ID, "0"));
            SharedPrefManager.setUserName(extractString(jsonResponseBody, ParamConstants.USER_NAME, isStaticDebug(LandingActivity.this) ? extractString(jsonRequest, ParamConstants.USER_NAME, "") : ""));
            SharedPrefManager.setUserProfileUrl(extractString(jsonResponseBody, ParamConstants.USER_PROFILE_IMG, ""));
            SharedPrefManager.setDashboardUrl(extractString(jsonResponseBody, ParamConstants.DASHBOARD_URL, ""));
            SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_FAVOURITE_MENU_SCREEN, extractBoolean(jsonResponseBody, ParamConstants.IS_SHOW_FAVOURITE_MENU_SCREEN, AppCommonMethods.isShowFavouriteMenuScreen));
            
            //Save Site/Location Types
            final JSONArray jsonArraySiteTypes = extractJSONArray(jsonResponseBody, ParamConstants.LIST_SITE_TYPE, extractJSONArray(jsonResponseBody, ParamConstants.SITE_TYPES));
            if(isNonEmpty(jsonArraySiteTypes)){
              ArrayList<String> siteTypes = new ArrayList<>();
              for(int i = 0; i < jsonArraySiteTypes.length(); i++){
                final Object obj = jsonArraySiteTypes.get(i);
                if(obj != null){
                  if(obj instanceof String) siteTypes.add(obj.toString());
                  else siteTypes.add(obj.toString());
                }
              }
              SharedPrefManager.setStringArrayList(ParamConstants.SITE_TYPES, siteTypes);
            }
            
            //Save Zones
            SharedPrefManager.setString(ParamConstants.LABEL_ZONES, extractString(jsonResponseBody, ParamConstants.LABEL_ZONES));
            SharedPrefManager.setInt(ParamConstants.MAX_SELECTION_ZONES, extractInt(jsonResponseBody, ParamConstants.MAX_SELECTION_ZONES, 0));
            final JSONArray jsonArrayZones = extractJSONArray(jsonResponseBody, ParamConstants.ZONES);
            if(jsonArrayZones != null && jsonArrayZones.length() > 0){
              new Handler().post(new Runnable(){
                @Override
                public void run(){
                  db.ZoneDao().deleteAll();
                  for(int i = 0; i < jsonArrayZones.length(); i++){
                    try{
                      Zone zone = getGSON().fromJson(jsonArrayZones.getJSONObject(i).toString(), Zone.class);
                      if(zone != null) db.ZoneDao().insert(zone);
                    }
                    catch(Exception e){
                      e.printStackTrace();
                    }
                  }
                }
              });
            }
            SharedPrefManager.setBoolean(ParamConstants.IS_ACTUAL_TAG_DISPLAY_MAPPING, extractBoolean(jsonResponseBody, ParamConstants.IS_ACTUAL_TAG_DISPLAY_MAPPING, false));
            
            //Save Categories
            SharedPrefManager.setString(ParamConstants.LABEL_CATEGORIES, extractString(jsonResponseBody, ParamConstants.LABEL_CATEGORIES));
            SharedPrefManager.setInt(ParamConstants.MAX_SELECTION_CATEGORIES, extractInt(jsonResponseBody, ParamConstants.MAX_SELECTION_CATEGORIES, 0));
            final JSONArray jsonArrayCategories = extractJSONArray(jsonResponseBody, ParamConstants.CATEGORIES);
            if(jsonArrayCategories != null && jsonArrayCategories.length() > 0){
              new Handler().post(new Runnable(){
                @Override
                public void run(){
                  db.CategoryDao().deleteAll();
                  for(int i = 0; i < jsonArrayCategories.length(); i++){
                    try{
                      if(jsonArrayCategories.get(i) != null){
                        Category category = null;
                        if(jsonArrayCategories.get(i) instanceof JSONObject)
                          category = getGSON().fromJson(jsonArrayCategories.getJSONObject(i).toString(), Category.class);
                        else if(jsonArrayCategories.get(i) instanceof String){
                          final String categoryName = jsonArrayCategories.get(i).toString().trim();
                          if(isNonEmpty(categoryName))
                            category = new Category("" + (i + 1), categoryName.trim());
                        }
                        if(category != null) db.CategoryDao().insert(category);
                      }
                    }
                    catch(Exception e){
                      e.printStackTrace();
                    }
                  }
                }
              });
            }
            
            //Save Brands
            SharedPrefManager.setString(ParamConstants.LABEL_BRANDS, extractString(jsonResponseBody, ParamConstants.LABEL_BRANDS));
            SharedPrefManager.setInt(ParamConstants.MAX_SELECTION_BRANDS, extractInt(jsonResponseBody, ParamConstants.MAX_SELECTION_BRANDS, 5));
            SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_ADVANCE_FILTERS_FOR_BRAND_INVENTORY, extractBoolean(jsonResponseBody, ParamConstants.IS_ALLOW_ADVANCE_FILTERS_FOR_BRAND_INVENTORY, AppCommonMethods.isAllowAdvanceFilterForBrand));
            SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_ADVANCE_FILTERS_FOR_MULTI_BRANDS, extractBoolean(jsonResponseBody, ParamConstants.IS_ALLOW_ADVANCE_FILTERS_FOR_MULTI_BRANDS, AppCommonMethods.isAllowAdvanceFilterForMultiBrands));
            final JSONArray jsonArrayBrands = extractJSONArray(jsonResponseBody, ParamConstants.BRANDS);
            if(jsonArrayBrands != null && jsonArrayBrands.length() > 0){
              new Handler().post(new Runnable(){
                @Override
                public void run(){
                  db.BrandDao().deleteAll();
                  for(int i = 0; i < jsonArrayBrands.length(); i++){
                    try{
                      if(jsonArrayBrands.get(i) != null){
                        Brand brand = null;
                        if(jsonArrayBrands.get(i) instanceof JSONObject)
                          brand = getGSON().fromJson(jsonArrayBrands.getJSONObject(i).toString(), Brand.class);
                        else if(jsonArrayBrands.get(i) instanceof String){
                          final String brandName = jsonArrayBrands.get(i).toString().trim();
                          if(isNonEmpty(brandName))
                            brand = new Brand("" + (i + 1), brandName.trim());
                        }
                        if(brand != null) db.BrandDao().insert(brand);
                      }
                    }
                    catch(Exception e){ e.printStackTrace(); }
                  }
                }
              });
            }
            
            //Save Dynamic Labels
            JSONObject prodLabels = extractJSONObject(jsonResponseBody, ParamConstants.PRODUCT_LABELS, jsonResponseBody);
            //if(isNonEmpty(prodLabels)){
            ProductModel productLabels = getGSON().fromJson(prodLabels.toString(), ProductModel.class);
            showLog("lblProdModel", "" + productLabels != null ? productLabels.toString() : "null");
            ProductModel.productModelLabels = productLabels;
            SharedPrefManager.setString(ParamConstants.LABEL_BRANDS, productLabels != null && isNonEmpty(productLabels.brand) ? productLabels.brand : extractString(prodLabels, ParamConstants.LABEL_BRANDS, getString(R.string.lbl_brand)));
            SharedPrefManager.setString(ParamConstants.LABEL_CATEGORIES, productLabels != null && isNonEmpty(productLabels.category) ? productLabels.category : extractString(prodLabels, ParamConstants.LABEL_CATEGORIES, getString(R.string.lbl_category)));
            SharedPrefManager.setString(ParamConstants.LABEL_ZONES, productLabels != null && isNonEmpty(productLabels.zone) ? productLabels.zone : extractString(prodLabels, ParamConstants.LABEL_ZONES, getString(R.string.lbl_location)));
            SharedPrefManager.setString(ParamConstants.LABEL_EANS, productLabels != null && isNonEmpty(productLabels.ean) ? productLabels.ean : extractString(prodLabels, ParamConstants.LABEL_EANS, getString(R.string.lbl_ean)));
            SharedPrefManager.setString(ParamConstants.LABEL_PRODUCT_ID, productLabels != null && isNonEmpty(productLabels.productId) ? productLabels.productId : extractString(prodLabels, ParamConstants.LABEL_PRODUCT_ID, getString(R.string.lbl_product_id)));
            SharedPrefManager.setString(ParamConstants.LABEL_ARTICLES, productLabels != null && isNonEmpty(productLabels.articleNo) ? productLabels.articleNo : extractString(prodLabels, ParamConstants.LABEL_ARTICLES, getString(R.string.lbl_article)));
            SharedPrefManager.setString(ParamConstants.LABEL_EPC, productLabels != null && isNonEmpty(productLabels.epc) ? productLabels.epc : extractString(prodLabels, ParamConstants.LABEL_EPC, getString(R.string.lbl_epc)));
            SharedPrefManager.setString(ParamConstants.LABEL_TID,/*productLabels!=null && isNonEmpty(productLabels.tid)?productLabels.tid:*/ extractString(prodLabels, ParamConstants.LABEL_TID, getString(R.string.lbl_tid)));
            SharedPrefManager.setString(ParamConstants.LABEL_COLORS, productLabels != null && isNonEmpty(productLabels.color) ? productLabels.color : extractString(prodLabels, ParamConstants.LABEL_COLORS, getString(R.string.lbl_color)));
            SharedPrefManager.setString(ParamConstants.LABEL_SIZES, productLabels != null && isNonEmpty(productLabels.size) ? productLabels.size : extractString(prodLabels, ParamConstants.LABEL_SIZES, getString(R.string.lbl_size)));
            SharedPrefManager.setString(ParamConstants.LABEL_NAME, productLabels != null && isNonEmpty(productLabels.name) ? productLabels.name : extractString(prodLabels, ParamConstants.LABEL_NAME, getString(R.string.lbl_name)));
            SharedPrefManager.setString(ParamConstants.LABEL_DESCRIPTION, productLabels != null && isNonEmpty(productLabels.description) ? productLabels.description : extractString(prodLabels, ParamConstants.LABEL_DESCRIPTION, getString(R.string.lbl_description)));
            SharedPrefManager.setString(ParamConstants.LABEL_MATKL, productLabels != null && isNonEmpty(productLabels.matkl) ? productLabels.matkl : extractString(prodLabels, ParamConstants.LABEL_MATKL, getString(R.string.lbl_matkl)));
            SharedPrefManager.setString(ParamConstants.LABEL_ORDER, productLabels != null && isNonEmpty(productLabels.orderNo) ? productLabels.orderNo : extractString(prodLabels, ParamConstants.LABEL_ORDER, getString(R.string.lbl_order)));
            //}
            //}
            
            //Save Outward Tote Types
            SharedPrefManager.setString(ParamConstants.LABEL_OUTWARD_TOTE_TYPES, extractString(jsonResponseBody, ParamConstants.LABEL_OUTWARD_TOTE_TYPES, extractString(jsonResponseBody, ParamConstants.LABEL_TYPES)));
            
            //Save Menus & Sub-Menus
            db.MenuDao().deleteAll();
            clearTopics();
            int navMenus = 0, actionMenus = 0;
            final JSONArray jsonArrayMenus = extractJSONArray(jsonResponseBody, ParamConstants.MENUS);
            if(jsonArrayMenus != null && jsonArrayMenus.length() > 0){
              for(int i = 0; i < jsonArrayMenus.length(); i++){
                try{
                  String menuStr = jsonArrayMenus.getJSONObject(i).toString();
                  MenuModel menuModel = getGSON().fromJson(menuStr, MenuModel.class);
                  if(menuModel != null){
                    final boolean checkScrFavCond = menuModel.getParentId() > 0 && menuModel.getMenuCode().matches("(?i)^.*_(start|config)$");
                    if(menuModel.isActionMenu == null){
                      menuModel.setIsActionMenu(!chkNotNullTrue(menuModel.getIsNavMenu()) && !chkNotNullTrue(menuModel.getIsFavMenu()) && !chkNotNullTrue(menuModel.getIsHomeMenu()) && menuModel.getMenuCode().toUpperCase().matches("^(ACT_|TOP_).*$"));
                      actionMenus += (chkNotNullTrue(menuModel.getIsActionMenu()) ? 1 : 0);
                    }
                    if(menuModel.isNavMenu == null){
                      menuModel.setIsNavMenu(!chkNotNullTrue(menuModel.getIsActionMenu()) && !chkNotNullTrue(menuModel.getIsFavMenu()) && !chkNotNullTrue(menuModel.getIsHomeMenu()) && menuModel.getMenuCode().toUpperCase().startsWith("NAV_"));
                      navMenus += (chkNotNullTrue(menuModel.getIsNavMenu()) ? 1 : 0);
                    }
                    if(menuModel.isFavMenu == null)
                      menuModel.setIsFavMenu(!chkNotNullTrue(menuModel.getIsActionMenu()) && !chkNotNullTrue(menuModel.getIsNavMenu()) && !chkNotNullTrue(menuModel.getIsHomeMenu()) && chkNull(menuModel.getParentId(), 0) > 0 && !menuModel.getMenuCode().toUpperCase().matches("(^.*_START$)") && (isSetInwOnline || !menuModel.getMenuCode().toUpperCase().matches("(^INW_.*$)")));
                    if(menuModel.isHomeMenu == null)
                      menuModel.setIsHomeMenu(!chkNotNullTrue(menuModel.getIsActionMenu()) && !chkNotNullTrue(menuModel.getIsNavMenu()) && !chkNotNullTrue(menuModel.getIsFavMenu()) && chkNull(menuModel.getParentId(), 0) <= 0);
                    final MenuModel parentMenu = menuModel.getParentId() > 0 ? db.MenuDao().getParent(menuModel.getParentId()) : null;
                    if(menuModel.isEnabled && menuModel.getParentId() > 0 && parentMenu != null && !parentMenu.isEnabled)
                      menuModel.setIsEnabled(false);
                    //temp condition for by default allowing subscribe to Notification for On Demand & Dynamic Replenishment
                    if(!menuModel.isSubscribeForNotification && (menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_REPLENISH_DEMAND) || menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_REPLENISH_EOSS) || (SharedPrefManager.getReplenishmentType().matches("(?i)("+AppConstants.REPLENISH_TYPE_DYNAMIC+"|"+AppConstants.REPLENISH_TYPE_BOTH+")") && menuModel.getMenuCode().matches("(?i)("+AppConstants.MENU_CODE_REPLENISH+"|"+AppConstants.MENU_CODE_MOV_REPLENISH+")"))))
                      menuModel.isSubscribeForNotification = true;
                    if(checkScrFavCond){
                      if(menuModel.isFavMenu && isNullOrEmpty(menuModel.favMenuName) && parentMenu != null && !menuModel.getMenuName().contains(parentMenu.getMenuName()) && !parentMenu.getMenuName().contains(menuModel.getMenuName())){
                        menuModel.setFavMenuName(parentMenu.getMenuName() + " " + menuModel.getMenuName());
                      }
                      if(!menuModel.isHomeMenu && !menuModel.isActionMenu && !menuModel.isNavMenu && menuModel.getParentId() > 0 && (isNullOrEmpty(menuModel.screenMenuCode) || isNullOrEmpty(menuModel.screenMenuName))){
                        if(parentMenu != null){
                          if(isNullOrEmpty(menuModel.screenMenuCode))
                            menuModel.setScreenMenuCode(parentMenu.getScreenMenuCode());
                          if(isNullOrEmpty(menuModel.screenImageUrl))
                            menuModel.setScreenImageUrl(parentMenu.getScreenImageUrl());
                          if(isNullOrEmpty(menuModel.screenMenuName))
                            menuModel.setScreenMenuName(parentMenu.getScreenMenuName());
                        }
                      }
                    }
                    if(isNullOrEmpty(menuModel.getImageUrl()) && (menuModel.isHomeMenu | menuModel.parentId <= 0) && menuModel.menuCode.trim().matches("(?i)(^.*(_START|_CONFIG|_ORD_STATS|_ORDER_STATS|_ACHIEVE|_ACHIEVEMENT|_ACHIEVEMENTS|_AWARD)$)")){
                      menuModel.setImageUrl(menuModel.menuCode.replaceAll("(?i)(_START|_CONFIG|_ORD_STATS|_ORDER_STATS|_ACHIEVE|_ACHIEVEMENT|_ACHIEVEMENTS|_AWARD)", ""));
                    }
                    if(isNullOrEmpty(menuModel.screenMenuName) && menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_REPLENISH_DEMAND) && menuModel.getMenuName().split(" ").length > 2){
                      menuModel.setScreenMenuName(menuModel.getMenuName().split(" ")[2]);
                    }
                    if(isNullOrEmpty(menuModel.screenMenuName) && menuModel.getMenuCode().equalsIgnoreCase(AppConstants.MENU_CODE_REPLENISH_EOSS) && menuModel.getMenuName().split(" ").length > 3){
                      menuModel.setScreenMenuName(menuModel.getMenuName().split(" ")[3]);
                    }
                    //subscribe to Notification if allowed
                    if(menuModel.isSubscribeForNotification)
                      subscribeToFirebaseTopic(SharedPrefManager.getStoreID() + "_" + SharedPrefManager.getStoreCode() + "_" + menuModel.getMenuCode());
                    db.MenuDao().insert(menuModel);
                  }
                }
                catch(Exception e){
                  e.printStackTrace();
                }
              }
            }
            
            //Dynamic Action Menus (Now Commented)
            final JSONArray jsonArrayActionMenus = extractJSONArray(jsonResponseBody, ParamConstants.ACTION_MENUS, actionMenus > 0 ? null : new JSONArray(getString(R.string.def_action_menus)));
            if(jsonArrayActionMenus != null && jsonArrayActionMenus.length() > 0){
              for(int i = 0; i < jsonArrayActionMenus.length(); i++){
                try{
                  MenuModel menuModel = getGSON().fromJson(jsonArrayActionMenus.getJSONObject(i).toString(), MenuModel.class);
                  if(menuModel != null){
                    menuModel.isHomeMenu = false;
                    menuModel.isFavMenu = false;
                    menuModel.isNavMenu = false;
                    if(menuModel.isActionMenu == null) menuModel.setIsActionMenu(true);
                    db.MenuDao().insert(menuModel);
                  }
                }
                catch(Exception e){
                  e.printStackTrace();
                }
              }
            }
            
            //Dynamic Nav Menus (Now Commented)
            final JSONArray jsonArrayNavMenus = extractJSONArray(jsonResponseBody, ParamConstants.NAV_MENUS, navMenus > 0 ? null : new JSONArray(getString(R.string.def_nav_menus)));
            if(jsonArrayNavMenus != null && jsonArrayNavMenus.length() > 0){
              for(int i = 0; i < jsonArrayNavMenus.length(); i++){
                try{
                  MenuModel menuModel = getGSON().fromJson(jsonArrayNavMenus.getJSONObject(i).toString(), MenuModel.class);
                  if(menuModel != null){
                    menuModel.isHomeMenu = false;
                    menuModel.isFavMenu = false;
                    menuModel.isActionMenu = false;
                    if(menuModel.isNavMenu == null) menuModel.setIsNavMenu(true);
                    db.MenuDao().insert(menuModel);
                  }
                }
                catch(Exception e){
                  e.printStackTrace();
                }
              }
            }
            
            //Tag Passwords & Config Flags for Encode
            //saveTagWritePasswords(jsonResponseBody);
            SharedPrefManager.setBoolean(ParamConstants.IS_ENC_VERIFY, extractBoolean(jsonResponseBody, ParamConstants.IS_ENC_VERIFY, AppCommonMethods.isVerifyEncode));
            AppCommonMethods.showLog("IS_ENC_VERIFY", "" + SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY));
            SharedPrefManager.setBoolean(ParamConstants.IS_ENC_UPLOAD_AFTER_VERIFY, extractBoolean(jsonResponseBody, ParamConstants.IS_ENC_UPLOAD_AFTER_VERIFY, AppCommonMethods.isUploadEncodeAfterVerify));
            AppCommonMethods.showLog("IS_ENC_UPLOAD_AFTER_VERIFY", "" + SharedPrefManager.getBoolean(ParamConstants.IS_ENC_UPLOAD_AFTER_VERIFY));
            SharedPrefManager.setBoolean(ParamConstants.IS_ENC_UPLOAD_BY_SCHEDULER, extractBoolean(jsonResponseBody, ParamConstants.IS_ENC_UPLOAD_BY_SCHEDULER, AppCommonMethods.isUseSchedulerForWritenTagUpload));
            SharedPrefManager.setBoolean(ParamConstants.IS_ENC_UPLOAD_BY_WHILE_PROCESSING, extractBoolean(jsonResponseBody, ParamConstants.IS_ENC_UPLOAD_BY_WHILE_PROCESSING, AppCommonMethods.isAllowBackgroundWritenTagUploadWhileProcessing));
            SharedPrefManager.setBoolean(ParamConstants.IS_ENC_UPLOAD_BY_BOTH_IMMEDIATE_SCHEDULER, extractBoolean(jsonResponseBody, ParamConstants.IS_ENC_UPLOAD_BY_BOTH_IMMEDIATE_SCHEDULER, AppCommonMethods.isAllowBothImmediateUploadAndUploadSchedulerForWrittenTags));
            SharedPrefManager.setString(ParamConstants.SESSION_FORCE_END_PASSWORD, extractString(jsonResponseBody, ParamConstants.SESSION_FORCE_END_PASSWORD, AppCommonMethods.sessionForceEndPass));
            AppCommonMethods.showLog("SESSION_FORCE_END_PASSWORD", SharedPrefManager.getString(ParamConstants.SESSION_FORCE_END_PASSWORD));
            
            SharedPrefManager.setBoolean(ParamConstants.IS_OFFLINE_ENCODE, extractBoolean(jsonResponseBody, ParamConstants.IS_OFFLINE_ENCODE, AppCommonMethods.isOfflineEncode));
            AppCommonMethods.showLog("IS_OFFLINE_ENCODE", "" + SharedPrefManager.getBoolean(ParamConstants.IS_OFFLINE_ENCODE));
            SharedPrefManager.setBoolean(ParamConstants.IS_OFFLINE_REENCODE, extractBoolean(jsonResponseBody, ParamConstants.IS_OFFLINE_REENCODE, AppCommonMethods.isOfflineReEncode));
            AppCommonMethods.showLog("IS_OFFLINE_REENCODE", "" + SharedPrefManager.getBoolean(ParamConstants.IS_OFFLINE_REENCODE));
            SharedPrefManager.setBoolean(ParamConstants.IS_CHECK_DEFAULT_PASSWORD_FIRST, extractBoolean(jsonResponseBody, ParamConstants.IS_CHECK_DEFAULT_PASSWORD_FIRST, AppCommonMethods.isCheckDefaultPasswordFirst));
            AppCommonMethods.showLog("IS_CHECK_DEFAULT_PASSWORD_FIRST", "" + SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_DEFAULT_PASSWORD_FIRST));
            
            final JSONObject jsonAccessPassword = extractJSONObject(jsonResponseBody, ParamConstants.ACCESS_PASSWORD);
            SharedPrefManager.setCurrentAccessPassword(extractString(jsonAccessPassword, ParamConstants.CURRENT_ACCESS_PASSWORD, extractString(jsonResponseBody, ParamConstants.CURRENT_ACCESS_PASSWORD, isDebugApp ? "12345678" : "00000000")));
            JSONArray oldPasswords = extractJSONArray(jsonAccessPassword, OLD_ACCESS_PASSWORDS);
            ArrayList<String> listOldPasswords = new ArrayList<>(0);
            if(oldPasswords != null && oldPasswords.length() > 0){
              for(int i = 0; i < oldPasswords.length(); i++){
                try{
                  final JSONObject oldPass = oldPasswords.getJSONObject(i);
                  final String oldPassword = oldPass != null ? extractString(oldPass, ParamConstants.OLD_PASSWORD, "") : "";
                  if(isNonEmpty(oldPassword)) listOldPasswords.add(oldPassword);
                }
                catch(Exception e){
                  e.printStackTrace();
                }
              }
            }
            else listOldPasswords.add(AppConstants.defaultTagZeroPassword);
            //Code to Add Half Pass combinations with Current Access Password
            final String currentAccessPassword = SharedPrefManager.getCurrentAccessPassword();
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
            SharedPrefManager.setBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, extractBoolean(jsonAccessPassword, ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, extractBoolean(jsonResponseBody, ParamConstants.IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC, AppCommonMethods.isCheckEncPasswordBasedOnEPC)));
            SharedPrefManager.setBoolean(ParamConstants.IS_CHECK_PASSWORD_BEFORE_API, extractBoolean(jsonAccessPassword, ParamConstants.IS_CHECK_PASSWORD_BEFORE_API, extractBoolean(jsonResponseBody, ParamConstants.IS_CHECK_PASSWORD_BEFORE_API, AppCommonMethods.isCheckEncPasswordBeforeAPI)));
            SharedPrefManager.setBoolean(ParamConstants.IS_CHECK_PASSWORD_FIRST, extractBoolean(jsonAccessPassword, ParamConstants.IS_CHECK_PASSWORD_FIRST, extractBoolean(jsonResponseBody, ParamConstants.IS_CHECK_PASSWORD_FIRST, AppCommonMethods.isCheckEncPasswordFirst)));
            
            //Tids for Non Password Tags (i.e. Tags having no password/reserved memory)
            final JSONArray jsonArrayNonPasswordTIDs = extractJSONArray(jsonResponseBody, ParamConstants.NON_PASSWORD_TIDS);
            if(jsonArrayNonPasswordTIDs != null && jsonArrayNonPasswordTIDs.length() > 0){
              ArrayList<String> listNonPassTids = new ArrayList<>(0);
              for(int i = 0; i < jsonArrayNonPasswordTIDs.length(); i++){
                try{
                  Object obj = jsonArrayNonPasswordTIDs.get(i);
                  if(obj != null){
                    if(obj instanceof String) listNonPassTids.add(obj.toString());
                    //else if(obj instanceof JSONObject) listNonPassTids.add();
                  }
                }
                catch(Exception e){
                  e.printStackTrace();
                }
              }
              SharedPrefManager.setNonPasswordTids(listNonPassTids);
            }
            
            //Tids for Non 128 bit Tags (i.e. Tags having no password/reserved memory)
            final JSONArray jsonArrayNon128BitTIDs = extractJSONArray(jsonResponseBody, ParamConstants.NON_128_BIT_TIDS);
            if(jsonArrayNon128BitTIDs != null && jsonArrayNon128BitTIDs.length() > 0){
              ArrayList<String> listNon128BitTids = new ArrayList<>(0);
              for(int i = 0; i < jsonArrayNon128BitTIDs.length(); i++){
                try{
                  Object obj = jsonArrayNon128BitTIDs.get(i);
                  if(obj != null){
                    if(obj instanceof String) listNon128BitTids.add(obj.toString());
                    //else if(obj instanceof JSONObject) listNonPassTids.add();
                  }
                }
                catch(Exception e){
                  e.printStackTrace();
                }
              }
              SharedPrefManager.setNon128BitTids(listNon128BitTids);
            }
            
            //Other Config Flags
            SharedPrefManager.setLong(ParamConstants.OPTIMIZED_INVENTORY_UPLOAD_TIME_INTERVAL, extractLong(jsonResponseBody, ParamConstants.OPTIMIZED_INVENTORY_UPLOAD_TIME_INTERVAL, AppCommonMethods.isOptimizedInventory ? 10L : 0L));
            SharedPrefManager.setLong(ParamConstants.OPTIMIZED_INVENTORY_UPLOAD_TAG_COUNT, extractLong(jsonResponseBody, ParamConstants.OPTIMIZED_INVENTORY_UPLOAD_TAG_COUNT, 0L));
            SharedPrefManager.setBoolean(ParamConstants.IS_OPTIMIZED_INVENTORY, extractBoolean(jsonResponseBody, ParamConstants.IS_OPTIMIZED_INVENTORY, SharedPrefManager.getLong(ParamConstants.OPTIMIZED_INVENTORY_UPLOAD_TIME_INTERVAL, SharedPrefManager.getLong(ParamConstants.OPTIMIZED_INVENTORY_UPLOAD_TAG_COUNT)) > 0));
            SharedPrefManager.setBoolean(ParamConstants.IS_DASHBOARD_AUTO_REFRESH, extractBoolean(jsonResponseBody, ParamConstants.IS_DASHBOARD_AUTO_REFRESH, AppCommonMethods.isAutoRefreshDashboards));
            SharedPrefManager.setBoolean(ParamConstants.IS_EPC_SEARCH_IN_STOCK_CORRECTION, extractBoolean(jsonResponseBody, ParamConstants.IS_EPC_SEARCH_IN_STOCK_CORRECTION, AppCommonMethods.isShowEPCSearchInStockCorrection));
            SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_SHORT_JSON_REQUEST_FOR_INVENTORY_UPLOAD, extractBoolean(jsonResponseBody, ParamConstants.IS_ALLOW_SHORT_JSON_REQUEST_FOR_INVENTORY_UPLOAD, AppCommonMethods.isAllowShortJsonRequestForInventoryUpload));
            SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_SHORT_JSON_REQUEST_FOR_STOCK_CORRECTION_UPLOAD, extractBoolean(jsonResponseBody, ParamConstants.IS_ALLOW_SHORT_JSON_REQUEST_FOR_STOCK_CORRECTION_UPLOAD, SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_SHORT_JSON_REQUEST_FOR_INVENTORY_UPLOAD)));
            SharedPrefManager.setIsAllowNonStdEans(extractBoolean(jsonResponseBody, ParamConstants.IS_ALLOW_NON_STD_EAN, !SharedPrefManager.isStdEanType() && AppCommonMethods.isAllowNonStdEan));
            SharedPrefManager.setIsAllowAlphaNumericNonStdEans(extractBoolean(jsonResponseBody, ParamConstants.IS_ALLOW_ALPHANUMERIC_NON_STD_EAN, SharedPrefManager.getIsAllowNonStdEans() && SGTIN128Helper.isAllowAlphanumericNonStdEan));
            SharedPrefManager.setIsEanMapped(extractBoolean(jsonResponseBody, ParamConstants.IS_EAN_MAPPING, false));
            SharedPrefManager.setIs11DigitStdEAN(extractBoolean(jsonResponseBody, ParamConstants.IS_11_DIGIT_STD_EAN, true));
            SharedPrefManager.setBoolean(ParamConstants.IS_USE_DIRECTIONAL_SEARCH, extractBoolean(jsonResponseBody, ParamConstants.IS_USE_DIRECTIONAL_SEARCH, AppCommonMethods.isUseDirectionalSearch));
            SharedPrefManager.setInt(ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED, extractInt(jsonResponseBody, ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED, AppCommonMethods.markFoundPercentLBS));
            SharedPrefManager.setInt(ParamConstants.MARK_FOUND_PERCENT_SER_ASSORTMENT, extractInt(jsonResponseBody, ParamConstants.MARK_FOUND_PERCENT_SER_ASSORTMENT, AppCommonMethods.markFoundPercentAssortmentSearch));
            SharedPrefManager.setInt(ParamConstants.MARK_FOUND_PERCENT_SER_UNENCODED, extractInt(jsonResponseBody, ParamConstants.MARK_FOUND_PERCENT_SER_UNENCODED, AppCommonMethods.markFoundPercentUnencodedSearch));
            SharedPrefManager.setInt(ParamConstants.SEARCH_RESET_COUNTER, extractInt(jsonResponseBody, ParamConstants.SEARCH_RESET_COUNTER, AppCommonMethods.searchResetCounterOnZero));
            SharedPrefManager.setBoolean(ParamConstants.IS_SERIAL_NUMBER_MANDATORY, extractBoolean(jsonResponseBody, ParamConstants.IS_SERIAL_NUMBER_MANDATORY, AppCommonMethods.isSerialNumberMandatory));


            //INV Count Config
            SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_ALL_ZONE_INVENTORY_FOR_TAKE_STOCK, extractBoolean(jsonResponseBody, ParamConstants.IS_ALLOW_ALL_ZONE_INVENTORY_FOR_TAKE_STOCK, AppCommonMethods.isAllowAllZoneInventoryForTakeStock));
            //final boolean hasAlienSearchMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_SER_ALIEN);
            //final boolean hasUnencodedSearchMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_SER_UNENCODED);
            SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV, extractBoolean(jsonResponseBody, ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV, AppCommonMethods.isShowUnencodedAndAlienCountsInInventory));
            SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_BRAND_INV, extractBoolean(jsonResponseBody, ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_BRAND_INV, SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV)));
            SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_ALIEN_COUNT_IN_INV, SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV) && extractBoolean(jsonResponseBody, ParamConstants.IS_SHOW_ALIEN_COUNT_IN_INV, SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV)));
            SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_UNENCODED_COUNT_IN_INV, SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV) && extractBoolean(jsonResponseBody, ParamConstants.IS_SHOW_UNENCODED_COUNT_IN_INV, SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV)));
            
            //Reference Barcode
            SharedPrefManager.setBoolean(ParamConstants.IS_USE_REFERENCE_BARCODE, extractBoolean(jsonResponseBody, ParamConstants.IS_USE_REFERENCE_BARCODE, extractBoolean(jsonResponseBody, ParamConstants.IS_USE_REFERENCE_EAN, extractBoolean(jsonResponseBody, ParamConstants.IS_USE_REFERENCE_ID, AppCommonMethods.isUseReferenceBarcode))));
            //is Use GID
            SharedPrefManager.setBoolean(ParamConstants.IS_GID, extractBoolean(jsonResponseBody, ParamConstants.IS_GID, SharedPrefManager.getBoolean(ParamConstants.IS_USE_REFERENCE_BARCODE, false) || AppCommonMethods.isUseGID));
            
            SharedPrefManager.setBoolean(ParamConstants.IS_IDENTIFY_ITEK_TAG_BY_EPC, extractBoolean(jsonResponseBody, ParamConstants.IS_IDENTIFY_ITEK_TAG_BY_EPC, AppCommonMethods.isIdentifyAndAllowOnlyITEKTagsByEPC));
            SharedPrefManager.setBoolean(ParamConstants.IS_IDENTIFY_ITEK_TAG_BY_TID, extractBoolean(jsonResponseBody, ParamConstants.IS_IDENTIFY_ITEK_TAG_BY_TID, AppCommonMethods.isIdentifyAndAllowOnlyITEKTagsByTID));
            
            //Enc Config
            final boolean hasENCStartMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_ENC_START);
            final boolean hasSSWMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_ENC_SSW);
            SharedPrefManager.setBoolean(ParamConstants.IS_BULK_ENCODE, hasSSWMenu && extractBoolean(jsonResponseBody, ParamConstants.IS_BULK_ENCODE, isDebugApp && hasSSWMenu));
            
            SharedPrefManager.setBoolean(ParamConstants.IS_CONFIG_ENC_SSW, hasSSWMenu && extractBoolean(jsonResponseBody, ParamConstants.IS_CONFIG_ENC_SSW, isDebugApp && hasSSWMenu));
            SharedPrefManager.setBoolean(ParamConstants.IS_CONFIG_ENC_START, hasENCStartMenu && extractBoolean(jsonResponseBody, ParamConstants.IS_CONFIG_ENC_START, isDebugApp && hasENCStartMenu));
            
            //Enc/Dec Algo Config
            SharedPrefManager.setString(ParamConstants.ENCODE_ALGORITHM_STD, extractString(jsonResponseBody, ParamConstants.ENCODE_ALGORITHM_STD, SharedPrefManager.getBoolean(ParamConstants.IS_GID) ? EPCEncoderDecoder.EncodeAlgorithmStd.TATA_GID.toString() : EPCEncoderDecoder.EncodeAlgorithmStd.SGTIN.toString()));
            SharedPrefManager.setString(ParamConstants.ENCODE_ALGORITHM_NON_STD, extractString(jsonResponseBody, ParamConstants.ENCODE_ALGORITHM_NON_STD, (SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_ALPHANUMERIC_NON_STD_EAN) ? EPCEncoderDecoder.EncodeAlgorithmNonStd.ITEK_NONSTD : EPCEncoderDecoder.EncodeAlgorithmNonStd.ITEK_NONSTD).toString()));
            
            //FIFO Config
            final boolean hasFIFOMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_SER_FIFO);
            SharedPrefManager.setBoolean(ParamConstants.IS_TID_BASED_COUNT_FOR_ENCODE, extractBoolean(jsonResponseBody, ParamConstants.IS_TID_BASED_COUNT_FOR_ENCODE, false));
            //SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING, hasFIFOMenu && extractBoolean(jsonResponseBody, ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING, hasFIFOMenu));
            SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_FIFO_CHART, hasFIFOMenu && extractBoolean(jsonResponseBody, ParamConstants.IS_SHOW_FIFO_CHART, hasFIFOMenu));
            
            //FIFO Validation In Decode
            SharedPrefManager.setString(ParamConstants.FIFO_TYPE, extractString(jsonResponseBody, ParamConstants.FIFO_TYPE, "")); //Encode/Inward or empty
            SharedPrefManager.setString(ParamConstants.FIFO_VALIDATION_MODE, extractString(jsonResponseBody, ParamConstants.FIFO_VALIDATION_MODE, isDebugApp ? "Flexible" : "")); //Strict/Flexible or empty
            SharedPrefManager.setBoolean(ParamConstants.IS_FIFO_VALIDATION, extractBoolean(jsonResponseBody, ParamConstants.IS_FIFO_VALIDATION, isNonEmpty(SharedPrefManager.getString(ParamConstants.FIFO_VALIDATION_MODE, ""))));
            SharedPrefManager.setBoolean(ParamConstants.IS_FIFO_VALIDATION_DECODE, isDebugApp?SharedPrefManager.getBoolean(ParamConstants.IS_FIFO_VALIDATION, false):extractBoolean(jsonResponseBody, ParamConstants.IS_FIFO_VALIDATION_DECODE, SharedPrefManager.getBoolean(ParamConstants.IS_FIFO_VALIDATION, false)));
            SharedPrefManager.setBoolean(ParamConstants.IS_FIFO_VALIDATION_OUTWARD, extractBoolean(jsonResponseBody, ParamConstants.IS_FIFO_VALIDATION_OUTWARD, SharedPrefManager.getBoolean(ParamConstants.IS_FIFO_VALIDATION, false)));
            JSONArray fifoReasons = extractJSONArray(jsonResponseBody, ParamConstants.FIFO_OVERRIDE_REASONS, null);
            SharedPrefManager.clearArrayList(ParamConstants.FIFO_OVERRIDE_REASONS);
            final ArrayList<String> fifoReasonsList = new ArrayList<>(0);
            if(isNonEmpty(fifoReasons)){
              for(int i = 0; i < fifoReasons.length(); i++)
                if(fifoReasons.get(i) != null && isNonEmpty(fifoReasons.get(i).toString()))
                  fifoReasonsList.add(fifoReasons.get(i).toString());
            }
            if(isNonEmpty(fifoReasonsList))
              SharedPrefManager.setStringArrayList(ParamConstants.FIFO_OVERRIDE_REASONS, fifoReasonsList);
            
            SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING, extractBoolean(jsonResponseBody, ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING, hasFIFOMenu && isNullOrEmpty(SharedPrefManager.getString(ParamConstants.FIFO_TYPE,""))));
            showLog(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING,""+SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING));
            clearFIFOValidationCriteria();
            final JSONArray jsonArrayCriteria = extractJSONArray(jsonResponseBody, ParamConstants.FIFO_VALIDATION_CRITERIA, null);
            final JSONObject jsonObjectCriteria = isNonEmpty(jsonArrayCriteria) ? null : extractJSONObject(jsonResponseBody, ParamConstants.FIFO_VALIDATION_CRITERIA, new JSONObject("{\"brand\":[\"CROMPTON\"],\"category\":[\"MOBILE PHONE\"]}"));
            final String strCriteria = isNonEmpty(jsonArrayCriteria) || isNonEmpty(jsonObjectCriteria) ? null : extractString(jsonResponseBody, ParamConstants.FIFO_VALIDATION_CRITERIA, isDebugApp ? "Mobile,Phone,Mobile Phone" : "");
            if(isNonEmpty(jsonArrayCriteria)) parseFIFOCriteriaForDecode(jsonArrayCriteria);
            if(isNonEmpty(jsonObjectCriteria)) parseFIFOCriteriaForDecode(jsonObjectCriteria);
            if(isNonEmpty(strCriteria)) parseFIFOCriteriaForDecode(strCriteria);
            
            //temp code
            showLog("isFIFOValidationInDecode", "" + SharedPrefManager.getBoolean(ParamConstants.IS_FIFO_VALIDATION, false));
            showLog("FIFOValidationMode", "" + SharedPrefManager.getString(ParamConstants.FIFO_VALIDATION_MODE, "empty"));
         //   showLog("FIFOValidationCriteriaInDecode", "" + chkNull(SharedPrefManager.getStringArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "KEYS").toString(), "empty"));
            
            //File Based Search (menu add)
            //            final boolean hasFileSearchMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_SER_FILE);
            //            if(!hasFileSearchMenu){
            //              final MenuModel menuSearch = db.MenuDao().getMenuByCode(AppConstants.MENU_CODE_SER);
            //              MenuModel menuModelFileSer = new MenuModel(5011, AppConstants.MENU_CODE_SER_FILE,"File Based Search");
            //              menuModelFileSer.setParentId(menuSearch.getMenuId());
            //              menuModelFileSer.setSequence(1000);
            //              menuModelFileSer.setIsEnabled(true);
            //              db.MenuDao().insert(menuModelFileSer);
            //            }
            
            // Off Range Based Search (menu add)
            //            final boolean hasOffRangeMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_OFF_RANGE) || db.MenuDao().hasMenu(AppConstants.MENU_CODE_OTW_OFF_RANGE);
            //            if(isDebugApp && !hasOffRangeMenu){
            //              MenuModel menuModelOffRange = new MenuModel(6001, AppConstants.MENU_CODE_OFF_RANGE,"Off Range");
            //              menuModelOffRange.setParentId(0);
            //              menuModelOffRange.setSequence(1001);
            //              menuModelOffRange.setIsEnabled(true);
            //              menuModelOffRange.setIsHomeMenu(true);
            //              db.MenuDao().insert(menuModelOffRange);
            //            }
            
            // Debug (menu add)
            /*final boolean hasDecodeMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_DEC);
            if(isDebugApp && !hasDecodeMenu){
              final MenuModel menuModelDebug = new MenuModel(9001, AppConstants.MENU_CODE_DEC, "Decoding");
              menuModelDebug.setParentId(0);
              menuModelDebug.setSequence(2001);
              menuModelDebug.setIsEnabled(true);
              menuModelDebug.setIsHomeMenu(true);
              db.MenuDao().insert(menuModelDebug);
            }*/
            
            /*final boolean hasScanCountMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_SCN_CNT);
            if(isDebugApp && !hasScanCountMenu){
              final MenuModel menuModelDebug = new MenuModel(10005, AppConstants.MENU_CODE_SCN_CNT, "Scan Count");
              menuModelDebug.setParentId(0);
              menuModelDebug.setSequence(3001);
              menuModelDebug.setIsEnabled(true);
              menuModelDebug.setIsHomeMenu(true);
              db.MenuDao().insert(menuModelDebug);
            }*/
            
            /*final boolean hasReplenishDMDMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_REPLENISH_DEMAND);
            if(isDebugApp && !hasReplenishDMDMenu){
              final MenuModel menuModelDebug = new MenuModel(10006, AppConstants.MENU_CODE_REPLENISH_DEMAND, "On Demand Replenishment");
              menuModelDebug.setScreenMenuName("Replenishment");
              menuModelDebug.setParentId(0);
              menuModelDebug.setSequence(3002);
              menuModelDebug.setIsEnabled(true);
              menuModelDebug.setIsHomeMenu(true);
              db.MenuDao().insert(menuModelDebug);
            }*/

            final boolean hasReplenishDMDMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_REPLENISH_EOSS);
            if(isDebugApp && !isDCApp && !hasReplenishDMDMenu){
              final MenuModel menuModelDebug = new MenuModel(10016, AppConstants.MENU_CODE_REPLENISH_EOSS, "EOSS Pick List Replenishment");
              menuModelDebug.setScreenMenuName("Replenishment");
              menuModelDebug.setParentId(0);
              menuModelDebug.setSequence(3014);
              menuModelDebug.setIsEnabled(true);
              menuModelDebug.setIsHomeMenu(true);
              db.MenuDao().insert(menuModelDebug);
            }
            
            /*final boolean hasExcelBasedSearchListMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_SER_EXCEL);
            if(isDebugApp && !hasExcelBasedSearchListMenu){
              final MenuModel menuModelSerExcel = new MenuModel(10007, AppConstants.MENU_CODE_SER_EXCEL, "Excel Based Search");
              final MenuModel menuModelSer = db.MenuDao().getMenuByCode(AppConstants.MENU_CODE_SER);
              final boolean hasSearchMenu = menuModelSer!=null;
              menuModelSerExcel.setParentId(hasSearchMenu?menuModelSer.getMenuId():0);
              menuModelSerExcel.setSequence(3003);
              menuModelSerExcel.setIsEnabled(true);
              menuModelSerExcel.setIsHomeMenu(!hasSearchMenu);
              menuModelSerExcel.setIsFavMenu(hasSearchMenu);
              db.MenuDao().insert(menuModelSerExcel);
            }*/
            
            /*final boolean hasMoveToDisplayMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_MOV_DISPLAY);
            if(isDebugApp && !hasMoveToDisplayMenu){
              final MenuModel menuModelMovDisplay = new MenuModel(10007, AppConstants.MENU_CODE_MOV_DISPLAY, "Move to Display");
              final MenuModel menuModelMov = null;//db.MenuDao().getMenuByCode(AppConstants.MENU_CODE_MOV);
              final boolean hasMovementMenu = menuModelMov!=null;
              menuModelMovDisplay.setParentId(hasMovementMenu?menuModelMov.getMenuId():0);
              menuModelMovDisplay.setSequence(3003);
              menuModelMovDisplay.setIsEnabled(true);
              menuModelMovDisplay.setIsHomeMenu(!hasMovementMenu);
              menuModelMovDisplay.setIsFavMenu(hasMovementMenu);
              db.MenuDao().insert(menuModelMovDisplay);
            }*/
            
            //Temp code for Manually adding Inv Filter Menu
            /*final boolean hasInvFilterMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_INV_FILTER);
            if(isDebugApp && !hasInvFilterMenu){
              final MenuModel menuModelInvFilter = new MenuModel(10007, AppConstants.MENU_CODE_INV_FILTER, "Inventory Filter");
              final MenuModel menuModelInv = db.MenuDao().getMenuByCode(AppConstants.MENU_CODE_INV);
              final boolean hasInvMenu = menuModelInv != null;
              menuModelInvFilter.setParentId(hasInvMenu ? menuModelInv.getMenuId() : 0);
              menuModelInvFilter.setSequence(3003);
              menuModelInvFilter.setIsEnabled(true);
              menuModelInvFilter.setIsHomeMenu(!hasInvMenu);
              menuModelInvFilter.setIsFavMenu(hasInvMenu);
              db.MenuDao().insert(menuModelInvFilter);
            }*/
            
            //Temp code for Manually adding Save Serial Menu
            /*final boolean hasSaveSerialMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_SERIAL_SAVE);
            if(isDebugApp && !hasSaveSerialMenu){
              final MenuModel menuModelSaveSerial = new MenuModel(10077, AppConstants.MENU_CODE_SERIAL_SAVE, "Set Serial");
              menuModelSaveSerial.setParentId(0);
              menuModelSaveSerial.setSequence(3003);
              menuModelSaveSerial.setIsEnabled(true);
              menuModelSaveSerial.setIsHomeMenu(true);
              menuModelSaveSerial.setIsFavMenu(false);
              db.MenuDao().insert(menuModelSaveSerial);
            }*/
            
            
            //THAN Config
            //final boolean hasThanMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_THAN) || db.MenuDao().hasMenu(AppConstants.MENU_CODE_THAN_ENC) ||db.MenuDao().hasMenu(AppConstants.MENU_CODE_THAN_CUTTING) || db.MenuDao().hasMenu(AppConstants.MENU_CODE_THAN_CLOSURE);
            final boolean hasThanEncMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_THAN_ENC);
            final boolean hasThanCuttingMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_THAN_CUTTING);
            final boolean hasThanClosureMenu = db.MenuDao().hasMenu(AppConstants.MENU_CODE_THAN_CLOSURE);
            SharedPrefManager.setBoolean(ParamConstants.IS_SET_EXTRA_PICK_TIME_FOR_THAN_ENC, hasThanEncMenu && extractBoolean(jsonResponseBody, ParamConstants.IS_SET_EXTRA_PICK_TIME_FOR_THAN_ENC, hasThanEncMenu && AppCommonMethods.isSetExtraPickTimeForThanEncoding));
            SharedPrefManager.setBoolean(ParamConstants.IS_GET_ORIGINAL_LENGTH_IN_THAN_ENC_FROM_FIELD, hasThanEncMenu && extractBoolean(jsonResponseBody, ParamConstants.IS_GET_ORIGINAL_LENGTH_IN_THAN_ENC_FROM_FIELD, hasThanEncMenu && AppCommonMethods.isGetOriginalLengthInEncFromField));
            SharedPrefManager.setBoolean(ParamConstants.IS_GET_ORIGINAL_LENGTH_IN_THAN_ENC_BEFORE_API, hasThanEncMenu && extractBoolean(jsonResponseBody, ParamConstants.IS_GET_ORIGINAL_LENGTH_IN_THAN_ENC_BEFORE_API, hasThanEncMenu && AppCommonMethods.isGetOriginalLengthInEncBeforeAPI));
            SharedPrefManager.setBoolean(ParamConstants.IS_CLEAR_ORIGINAL_LENGTH_IN_THAN_ENC_FOR_EACH_TAG, hasThanEncMenu && extractBoolean(jsonResponseBody, ParamConstants.IS_CLEAR_ORIGINAL_LENGTH_IN_THAN_ENC_FOR_EACH_TAG, hasThanEncMenu && AppCommonMethods.isClearOriginalLengthInEncForEachTag));
            SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_ZERO_LENGTH_FOR_THAN_CLOSURE, hasThanClosureMenu && extractBoolean(jsonResponseBody, ParamConstants.IS_ALLOW_ZERO_LENGTH_FOR_THAN_CLOSURE, hasThanClosureMenu && AppCommonMethods.isAllowZeroLengthForThanClosure));
            SharedPrefManager.setString(ParamConstants.LENGTH_UNIT, extractString(jsonResponseBody, ParamConstants.LENGTH_UNIT, extractString(jsonResponseBody, ParamConstants.UNIT, AppCommonMethods.defLengthUnitThan)));
            try{
              SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_NON_ENCODED_TAG_PICK_FOR_DECODE, SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_NON_ENCODED_TAG_PICK_FOR_DECODE, AppCommonMethods.isAllowNonEncodedTagPickForDecode));
              SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_DECODE_TYPE_SELECTION_DIALOG, SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_DECODE_TYPE_SELECTION_DIALOG, AppCommonMethods.isShowDecodeTypeSelectionDialog));
              SharedPrefManager.setBoolean(ParamConstants.IS_CHECK_PRODUCT_DETAILS_BEFORE_DECODING, SharedPrefManager.getBoolean(ParamConstants.IS_CHECK_PRODUCT_DETAILS_BEFORE_DECODING, AppCommonMethods.isCheckProductDetailsBeforeDecoding));
              SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_STATIC_DECODE_TYPES_FOR_SELECTION_IF_API_FAILS, SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_STATIC_DECODE_TYPES_FOR_SELECTION_IF_API_FAILS, AppCommonMethods.isShowStaticDecodeTypesForSelectionIfApiFails));
              SharedPrefManager.clearArrayList(ParamConstants.DECODE_TYPES);
              SharedPrefManager.setBoolean(ParamConstants.DECODE_TYPES + "_" + ParamConstants.IS_SAVED_FROM_LOGIN, false);
              final JSONArray decodeTypes = extractJSONArray(jsonResponseBody, ParamConstants.DECODE_TYPES);// isDebugApp ? new JSONArray("[{\"name\": \"Shrinkage\",\"type\": \"D\"},{\"name\": \"Omnichannel Fulfillment\",\"type\": \"O\"},{\"name\": \"GRDC/IST\",\"type\": \"G\"},{\"name\": \"Other\",\"type\": \"T\"}]") : null);
              if(isNonEmpty(decodeTypes)){
                ArrayList<DecodeType> listDecodingTypes = new ArrayList<>(0);
                for(int i = 0; i < decodeTypes.length(); i++){
                  DecodeType decodeType = getGSON().fromJson(decodeTypes.getJSONObject(i).toString(), DecodeType.class);
                  if(decodeType != null && isNonEmpty(decodeType.type))
                    listDecodingTypes.add(decodeType);
                }
                if(isNonEmpty(listDecodingTypes))
                  SharedPrefManager.setArrayList(ParamConstants.DECODE_TYPES, listDecodingTypes);
                SharedPrefManager.setBoolean(ParamConstants.DECODE_TYPES + "_" + ParamConstants.IS_SAVED_FROM_LOGIN, true);
              }
            }
            catch(Exception e){
              e.printStackTrace();
            }
            
            //theme
            JSONObject theme = extractJSONObject(jsonResponseBody, ParamConstants.THEME, jsonResponseBody);
            SharedPrefManager.setString(ParamConstants.COLOR_PRIMARY_DARK, extractString(theme, ParamConstants.COLOR_PRIMARY_DARK, extractString(theme, ParamConstants.THEME_COLOR_PRIMARY_DARK, extractString(theme, ParamConstants.THEME_COLOR, extractString(theme, ParamConstants.COLOR_THEME, extractString(theme, ParamConstants.COLOR, extractString(theme, ParamConstants.THEME)))))));
            SharedPrefManager.setString(ParamConstants.COLOR_PRIMARY, extractString(theme, ParamConstants.COLOR_PRIMARY, extractString(theme, ParamConstants.THEME_COLOR_PRIMARY)));//,"#FEFEFE")));
            SharedPrefManager.setString(ParamConstants.COLOR_ACCENT, extractString(theme, ParamConstants.COLOR_ACCENT, extractString(theme, ParamConstants.THEME_COLOR_ACCENT, extractString(theme, ParamConstants.THEME_COLOR, extractString(theme, ParamConstants.COLOR_THEME, extractString(theme, ParamConstants.COLOR, extractString(theme, ParamConstants.THEME)))))));
            
            setNewThemeColor();
            
            SharedPrefManager.setBoolean(ParamConstants.HAS_BLUETOOTH_CONNECTION_REQUIREMENT, (hasThanCuttingMenu || hasThanClosureMenu) && extractBoolean(jsonResponseBody, ParamConstants.HAS_BLUETOOTH_CONNECTION_REQUIREMENT, hasThanCuttingMenu || hasThanClosureMenu));
            SharedPrefManager.setBoolean(ParamConstants.HAS_BLUETOOTH_PRINTING, (hasThanCuttingMenu || hasThanClosureMenu) && extractBoolean(jsonResponseBody, ParamConstants.HAS_BLUETOOTH_PRINTING, hasThanCuttingMenu || hasThanClosureMenu));
            
            //Flag to handle notification data via intent or not
            SharedPrefManager.setBoolean(ParamConstants.IS_HANDLE_NOTIFICATION_DATA_VIA_INTENT, extractBoolean(jsonResponseBody, ParamConstants.IS_HANDLE_NOTIFICATION_DATA_VIA_INTENT, isHandleNotificationDataFromIntent));
            
            //Flog to call/don't API for marking tag as found in Excel Base Search
            SharedPrefManager.setInt(ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED_NEW, extractInt(jsonResponseBody, ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED_NEW, AppCommonMethods.markFoundPercentNewLBS));
            SharedPrefManager.setBoolean(ParamConstants.IS_API_BASED_MARK_FOUND_LBS, extractBoolean(jsonResponseBody, ParamConstants.IS_API_BASED_MARK_FOUND_LBS, AppCommonMethods.isCallAPIBasedMarkFound));
            
            //Flag to show/don't show check availability button in product details
            SharedPrefManager.setBoolean(ParamConstants.IS_SHOW_CHECK_AVAILABILITY_BTN, extractBoolean(jsonResponseBody, ParamConstants.IS_SHOW_CHECK_AVAILABILITY_BTN, AppCommonMethods.isShowCheckAvailabilityBtnForProductDetails));
            
            //Flag to show/don't show Share Log Info option
            SharedPrefManager.setBoolean(ParamConstants.IS_ALLOW_SHARE_LOG, extractBoolean(jsonResponseBody, ParamConstants.IS_ALLOW_SHARE_LOG, AppCommonMethods.isAllowShareLog));
            
            final boolean isShowUserProfileAlertAfterLogin = SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_USER_PROFILE_ALERT_AFTER_LOGIN, AppCommonMethods.isShowUserProfileAlertAfterLogin);
            hideProgressDialog();
            successBeep();
            if(isShowUserProfileAlertAfterLogin) showUserProfileAlertDialog();
            else redirectToMain();
          }
          break;
        case URLConstants.RESET_PASSWORD:
          if(isSuccess){
            final String msg = extractString(jsonResponseBody, ParamConstants.MESSAGE);
            if(isNonEmpty(msg)) showCustomSuccessDialog(msg, false, false);
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  private void parseFIFOCriteriaForDecode(final JSONArray jsonArray){ parseFIFOCriteriaForDecode(jsonArray, ""); }
  
  private void parseFIFOCriteriaForDecode(final JSONArray jsonArray, String key){
    if(isNullOrEmpty(jsonArray)) return;
    try{
      ArrayList<String> listCriteria = new ArrayList<>(0);
      for(int i = 0; i < jsonArray.length(); i++){
        Object obj = jsonArray.get(i);
        if(obj == null) continue;
        if(obj instanceof String){
          listCriteria.add(obj.toString().toUpperCase());
        }
      }
      if(isNonEmpty(listCriteria)){
        //TODO add List OF Keys Separately
        ArrayList<String> listFIFOValidationCriteriaKeys = SharedPrefManager.getStringArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "KEYS", new ArrayList<String>(0));
        final String key1 = chkNull(key, "").toUpperCase();
        listFIFOValidationCriteriaKeys.add(key1);
        SharedPrefManager.setStringArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "KEYS", listFIFOValidationCriteriaKeys);
        SharedPrefManager.setStringArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "_" + key1, listCriteria);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  private void clearFIFOValidationCriteria(){
    ArrayList<String> listFIFOValidationCriteriaKeys = SharedPrefManager.getStringArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "KEYS", new ArrayList<String>(0));
    if(isNonEmpty(listFIFOValidationCriteriaKeys)){
      for(String key1 : listFIFOValidationCriteriaKeys)
        SharedPrefManager.clearArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "_" + key1);
      SharedPrefManager.clearArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "KEYS");
    }
  }
  
  private void parseFIFOCriteriaForDecode(final String string){ parseFIFOCriteriaForDecode(string, ""); }
  
  private void parseFIFOCriteriaForDecode(final String string, String key){
    if(isNullOrEmpty(string)) return;
    try{
      ArrayList<String> listCriteria = new ArrayList<>(0);
      for(String str : string.split(",")){
        if(isNullOrEmpty(str)) continue;
        listCriteria.add(str.toUpperCase());
      }
      if(isNonEmpty(listCriteria)){
        //TODO add List OF Keys Separately
        ArrayList<String> listFIFOValidationCriteriaKeys = SharedPrefManager.getStringArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "KEYS", new ArrayList<String>(0));
        final String key1 = chkNull(key, "").toUpperCase();
        listFIFOValidationCriteriaKeys.add(key1);
        SharedPrefManager.setStringArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "KEYS", listFIFOValidationCriteriaKeys);
        SharedPrefManager.setStringArrayList(ParamConstants.FIFO_VALIDATION_CRITERIA + "_" + key1, listCriteria);
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  private void parseFIFOCriteriaForDecode(final JSONObject jsonObject){
    if(isNullOrEmpty(jsonObject)) return;
    try{
      if(isNonEmpty(jsonObject)){
        for(Iterator<String> it = jsonObject.keys(); it.hasNext(); ){
          final String key = it.next();
          if(jsonObject.get(key) != null){//TODO handle properly for multiple attributes (hashMap maybe?)
            if(jsonObject.get(key) instanceof JSONArray)
              parseFIFOCriteriaForDecode(jsonObject.getJSONArray(key), key);
            else if(jsonObject.get(key) instanceof String)
              parseFIFOCriteriaForDecode(jsonObject.getString(key), key);
          }
        }
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  private void setNewThemeColor(){
    String stringColor = SharedPrefManager.getString(ParamConstants.COLOR_PRIMARY_DARK, "");
    if(isNonEmpty(stringColor)){
      AppCommonMethods.showLog("stringColor", stringColor);
      
      int color = Color.parseColor(stringColor);
      int rgb = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3;
      
      int red = Math.round(Color.red(color) / 15.0f) * 15;
      int green = Math.round(Color.green(color) / 15.0f) * 15;
      int blue = Math.round(Color.blue(color) / 15.0f) * 15;
      
      String strColor = Integer.toHexString(Color.rgb(red, green, blue)).substring(2);
      SharedPrefManager.setString(ParamConstants.THEME_COLOR, strColor);
    }
  }
  
  private void showUserProfileAlertDialog(){
    final DialogProfileBinding binding = DialogProfileBinding.inflate(LayoutInflater.from(this));
    loadImage(binding.imgProfileUser, SharedPrefManager.getUserProfileUrl(), R.drawable.ic_temp_person);
    binding.txtStoreId.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_store_id), chkNull(SharedPrefManager.getStoreID(), " -")), HtmlCompat.FROM_HTML_MODE_LEGACY));
    binding.txtStoreName.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_store_name), chkNull(SharedPrefManager.getStoreName(), " -")), HtmlCompat.FROM_HTML_MODE_LEGACY));
    showCustomAlertDialog(String.format(getString(R.string.txt_hello_user), chkNull(SharedPrefManager.getUserName(), "")), "", binding.getRoot(), getResources().getString(R.string.btn_proceed), new DialogInterface.OnClickListener(){
      public void onClick(DialogInterface dialog, int which){
        redirectToMain();
      }
    });
  }
  
  @Override
  public boolean onMenuItemClick(MenuItem menuItem){
    showLog("isMenusLocked", "" + isMenusLocked);
    if(isMenusLocked) return false;
    
    Bundle args = new Bundle();
    args.putInt(AppConstants.MENU_ICON_ID, menuItem.getItemId());
    switch(menuItem.getItemId()){
      case R.id.imgLandingHeaderConfic:
        binding.txtConfigUrl.performClick();
        return true;
      
      case R.id.imgLandingHeaderCheckUpadte:
        checkVersionUpdates();
        return true;
      
      case R.id.imgLandingHeaderInfo:
        args.putString(AppConstants.TITLE, getString(R.string.app_name));
        args.putString(AppConstants.TITLE_LOGO_URL, "");
        args.putInt(AppConstants.TITLE_LOGO_RES_ID, R.drawable.ic_app_logo);
        loadFragment(new AppInfoFragment(), args);
        
        return true;
      default:
        return super.onOptionsItemSelected(menuItem);
    }
  }
  
}
