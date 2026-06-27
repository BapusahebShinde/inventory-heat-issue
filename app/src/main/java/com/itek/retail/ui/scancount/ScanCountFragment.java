package com.itek.retail.ui.scancount;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT_PATTERN;
import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractLong;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isHideUnencodedTags;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isShowEanQtyListWithHeaderForScanCount;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itek.retail.R;
import com.itek.retail.adapter.ProductListAdapter;
import com.itek.retail.adapter.ScanCountAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.BrandWiseZoneInventoryDao;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.ZoneDao;
import com.itek.retail.databinding.FragmentScanCountBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inventory.InventoryStartViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Inventory start fragment.
 */
public class ScanCountFragment extends RFIDSessionFragment{
  
  //temp flags
  boolean isAPICallForSessionResume = false;
  ZoneDao zoneDao;
  InventoryDao inventoryDao;
  ProductDao productDao;
  BrandWiseZoneInventoryDao brandWiseZoneInventoryDao;
  int activeUsers = 0, sessionValidTill = 48;
  private String huNumber;
  private List<EanQty> listEncodedEans = new ArrayList<>(0);
  private List<ProductModel> listProductEans = new ArrayList<>(0);
  private FragmentScanCountBinding binding;
  private InventoryStartViewModel mViewModel;
  private List<String> listIgnoreEpcs = new ArrayList<>(0);
  
  /**
   * Instantiates a new Inventory start fragment.
   */
  public ScanCountFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onPause(){
    super.onPause();
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    zoneDao = AppDatabase.getZoneDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
    productDao = AppDatabase.getProductDao(context);
    brandWiseZoneInventoryDao = AppDatabase.getBrandWiseZoneInventoryDao(context);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(InventoryStartViewModel.class);
    binding = FragmentScanCountBinding.inflate(inflater, container, false);
    
    huNumber = extractString(getArguments(), AppConstants.HU_NUMBER, "");
    //if(isNullOrEmpty(huNumber)) popBackStack();
    activeUsers = extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2);
    sessionValidTill = extractInt(getArguments(), AppConstants.SESSION_VALID_TILL, 48);
    
    binding.header.imgConfigSync.setVisibility(View.VISIBLE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_config);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue())) return;
        binding.llSeekbarPower.setVisibility(binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
      }
    });
    
    setActiveUsers(activeUsers);
    binding.txtHu.setVisibility(isNonEmpty(huNumber) ? View.VISIBLE : View.GONE);
    if(isNonEmpty(huNumber))
      binding.txtHu.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_hu), huNumber), HtmlCompat.FROM_HTML_MODE_COMPACT));
    binding.rvLabels.getRoot().setVisibility(isShowEanQtyListWithHeaderForScanCount ? View.VISIBLE : View.GONE);
    binding.rvScn.setAdapter(isShowEanQtyListWithHeaderForScanCount ? new ScanCountAdapter((MainActivity) context, ScanCountFragment.this, listEncodedEans) : new ProductListAdapter((MainActivity) context, ScanCountFragment.this, listProductEans));
    binding.rvScn.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
    
    //binding.txtInventoryStartTotal.setText(getInvCount());
    
    binding.llSeekbarPower.setupProgress(mainViewModel);
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        context.dismissCustomAlertDialog();
        final Boolean isInventorySessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
        if(!isInventorySessionOn){
          if(sessionObject != null) mainViewModel.startSession(sessionObject, true);
          else if(sessionObject == null){
            //Call API & Start Inventory Session
            apiCall(AppConstants.SESSION_ACTION_START);
          }
        }
        else if(isInventorySessionOn){
          if(getSize() >= AppCommonMethods.invLimit){
            if(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
              mainViewModel.stopInventory();
            context.showCustomErrDialog(R.string.err_inventory_max_limit);
          }
          else toggleInventory();
        }
      }
    });
    
    binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.showCustomConfirmDialog(getString(R.string.msg_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            apiCall(AppConstants.SESSION_ACTION_UPLOAD);
          }
        });
      }
    });
    
    /*binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
          return;
        context.showCustomConfirmDialog(getString(R.string.msg_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            apiCall(AppConstants.SESSION_ACTION_UPLOAD);
          }
        });
      }
    });
    
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          binding.btnSwipeUpload.reset();
          context.showCustomConfirmDialog(getString(R.string.msg_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              apiCall(AppConstants.SESSION_ACTION_UPLOAD);
            }
          });
        }
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);*/
    
    apiCall(AppConstants.SESSION_ACTION_START);
    
    return binding.getRoot();
  }


  @Override
  protected void onReaderConfigured(){
    super.onReaderConfigured();
    if(mainViewModel != null && binding != null && binding.llSeekbarPower != null)
      binding.llSeekbarPower.updateReaderPower(mainViewModel, 5);
  }
  
  @Override
  protected void isSessionOnChanged(final Boolean isInventorySessionOn){
    super.isSessionOnChanged(isInventorySessionOn);
    if(isInventorySessionOn == null) return;
    boolean isInvSessionOn = chkNotNullTrue(isInventorySessionOn);
    
    binding.spinInventoryStartLocation.setEnabled(!isInvSessionOn);
    binding.llBtnStart.toggle(isInventorySessionOn);
    final boolean isInvCount = getSize() > 0;//AppCommonMethods.parseInt(chkNull(binding.txtInventoryStartScoreCount.getText().toString(), "0")) > 0;
    final boolean isShowLastInventoryDate = chkNull(SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.INVENTORY_TAKEN_DATE_TIME, "T").replaceFirst("T", " ").trim(), "").length() >= 5;
    binding.imgInventoryStartInfo.setVisibility(isShowLastInventoryDate && !chkNotNullTrue(isInventorySessionOn) /*&& isInvCount*/ ? View.VISIBLE : View.GONE);
    binding.btnUpload.setVisibility(chkNotNullFalse(isInventorySessionOn) && isInvCount ? View.VISIBLE : View.GONE);
    //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && chkNotNullFalse(isInventorySessionOn) && isInvCount ? View.VISIBLE : View.GONE);
  }
  
  @Override
  protected void isInventoryOnChanged(Boolean isSessionOn){
    super.isInventoryOnChanged(isSessionOn);
    final Boolean isInventoryOn = mainViewModel.getIsInventoryOn().getValue();
    if(isInventoryOn == null) return;
    else{
      binding.llSeekbarPower.setEnabled(!isInventoryOn);
      binding.llSeekbarPower.setVisibility(!isInventoryOn && binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
      binding.llBtnStart.toggle(isInventoryOn);
      final boolean isInvCount = getSize() > 0;//AppCommonMethods.parseInt(chkNull(binding.txtInventoryStartScoreCount.getText().toString(), "0")) > 0;
      final boolean isShowLastInventoryDate = chkNull(SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.INVENTORY_TAKEN_DATE_TIME, "T").replaceFirst("T", " ").trim(), "").length() >= 5;
      binding.imgInventoryStartInfo.setVisibility(isShowLastInventoryDate && !chkNotNullTrue(isInventoryOn) /*&& isInvCount*/ ? View.VISIBLE : View.GONE);
      binding.btnUpload.setVisibility(chkNotNullFalse(isInventoryOn) && isInvCount ? View.VISIBLE : View.GONE);
      //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && chkNotNullFalse(isInventoryOn) && isInvCount ? View.VISIBLE : View.GONE);
    }
  }
  
  @Override
  protected void toggleInventory(){
    if(!isHideUnencodedTags && isNullOrEmpty(listIgnoreEpcs)) super.toggleInventory();
    else{
      AppCommonMethods.showLog("toggleInventory", "" + getSessionType());
      if(sessionObject != null && chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
        mainViewModel.stopInventory();
      else if(checkReaderConnected() && (sessionObject != null || chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
        mainViewModel.startInventory(isHideUnencodedTags, listIgnoreEpcs);
    }
  }
  
  @Override
  protected void onTriggerPressed(){
    binding.llBtnStart.performClick();
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    if(chkNull(size, 0) > 0 && sessionObject != null && !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))// && chkNotNullTrue(mainViewModel.getIsDeviceConfigured().getValue()) && chkNotNullFalse(mainViewModel.getIsInventorySessionOn().getValue()))
      mainViewModel.startSession(sessionObject, false);
    
    final int totalTags = getInventoryTotalSize();
    final int alienTags = getAlignTagCount();
    final int unencodedTagCount = getUnencodedTagCount();
    final int invScore = totalTags - (alienTags + unencodedTagCount);
    updateLists();
    //binding.txtInventoryStartScoreCount.setText("" + chkNull(size, 0));
   // binding.txtInventoryStartScoreCount.setText("" + chkNull(invScore < 0 ? 0 : invScore, 0));
    binding.ctwInventoryStart.setTotal(chkNull(invScore < 0 ? 0 : invScore, 0));
  //  binding.txtInventoryStartAlien.setText("" + chkNull(alienTags, 0));
    binding.ctwAlien.setScore(chkNull(alienTags, 0));
   // binding.txtInventoryStartUnencoded.setText("" + chkNull(unencodedTagCount, 0));
    binding.ctwUnencoded.setScore(chkNull(unencodedTagCount, 0));

   // long total = Long.parseLong(chkNull(binding.txtInventoryStartTotal.getText().toString().replace("-", ""), "0"));
    long total = Long.parseLong(chkNull(binding.ctwInventoryStart.getTotal().replace("-", ""), "0"));
    final boolean isInvCount = chkNull(size, 0) > 0;
    final boolean isShowLastInventoryDate = chkNull(SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.INVENTORY_TAKEN_DATE_TIME, "T").replaceFirst("T", " ").trim(), "").length() >= 5;
    binding.imgInventoryStartInfo.setVisibility(isShowLastInventoryDate && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) /*&& isInvCount*/ ? View.VISIBLE : View.GONE);
    binding.btnUpload.setVisibility(chkNotNullFalse(mainViewModel.getIsInventoryOn().getValue()) && isInvCount ? View.VISIBLE : View.GONE);
    //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && chkNotNullFalse(mainViewModel.getIsInventoryOn().getValue()) && isInvCount ? View.VISIBLE : View.GONE);
    double per = total > 0 ? (chkNull(invScore, 0) * 100) / total : 0;
    //double per = total > 0 ? (chkNull(size, 0) * 100) / total : 0;
    int percentage = (int) per;
   // binding.progressInventoryStart.setProgress(percentage);
    if(chkNull(size, 0) >= AppCommonMethods.invLimit){
      if(sessionObject != null && chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))
        mainViewModel.stopInventory();
      if(chkNull(size, 0) == AppCommonMethods.invLimit)
        context.showCustomErrDialog(R.string.err_inventory_max_limit);
    }
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    listEncodedEans.clear();
    if(sessionObject != null) listEncodedEans.addAll(inventoryDao.getEans(sessionObject.sessionId));
    listProductEans.clear();
    //TODO after product data received
    // listProductEans.addAll( productDao.getProducts(listProductEans.stream().map(e->e.getEan()).collect(Collectors.toList())));
    listProductEans.addAll(listEncodedEans.stream().map(e -> {
      ProductModel pm = (ProductModel) chkNull(productDao.getProduct(e.getEan()), new ProductModel());
      
      pm.setEan(e.getEan());
      pm.setQty(e.getEanQty());
      pm.setEanQty(e.getEanQty());
      
      return pm;
    }).collect(Collectors.toList()));
    if(binding != null || binding.rvScn != null || binding.rvScn.getAdapter() != null)
      binding.rvScn.getAdapter().notifyDataSetChanged();
  }
  
  @Override
  protected void onReaderPowerChanged(Integer power){
    binding.llSeekbarPower.updateReaderPower(mainViewModel, power);
  }
  
  @Override
  public void apiCall(String action){
    if(!action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD)){
      String sessionId = null, sessionTime = null;
      sessionId = null;
      sessionTime = "-1";
      sessionValidTill = -1;
      activeUsers = -1;
      setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
      
      //setSessionAction(action, sessionId, sessionTime, activeUsers, target, args);
      return;
      
    }
    final Boolean isInventorySessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
    if(isInventorySessionOn != null){
      final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
      if(isUpload && isInternetConnected(context, false, isUpload)){
        allowBtnClick = false;
        try{
          if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          JSONObject requestParams = new JSONObject();
          requestParams.put("dccode", "02");
          if(isNonEmpty(huNumber)) requestParams.put("hunumber", huNumber);
          requestParams.put(ParamConstants.DEVICE_ID.toLowerCase(), SharedPrefManager.getIMEI());
          requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
          requestParams.put(ParamConstants.TYPE, AppConstants.TAKE_STOCK);
          final String selZone = sessionObject != null ? chkNull(sessionObject.zone, AppConstants.ALL) : chkNull(binding.spinInventoryStartLocation.getSelectedItem().toString(), AppConstants.ALL);
          if(selZone.equalsIgnoreCase(AppConstants.ALL)){
            requestParams.put(ParamConstants.ZONE_ID, 0);
            requestParams.put(ParamConstants.ZONE, selZone);
          }
          else{
            List<Zone> listZones = AppDatabase.getZoneDao(context).getZoneByName(sessionObject != null ? chkNull(sessionObject.zone, AppConstants.ALL) : chkNull(binding.spinInventoryStartLocation.getSelectedItem().toString(), AppConstants.ALL));
            if(isNonEmpty(listZones) && listZones.size() == 1){
              Zone zone = listZones.get(0);
              requestParams.put(ParamConstants.ZONE_ID, zone != null ? zone.zoneId : 0);
              requestParams.put(ParamConstants.ZONE, zone != null ? zone.zoneName : sessionObject != null ? chkNull(sessionObject.zone, AppConstants.ALL) : chkNull(binding.spinInventoryStartLocation.getSelectedItem().toString(), AppConstants.ALL));
              requestParams.put(ParamConstants.ZONE_TYPE, zone != null ? zone.zoneType : null);
              requestParams.put(ParamConstants.IS_DEFAULT_ZONE, zone != null ? zone.isDefault : false);
            }
            else{
              JSONArray zones = new JSONArray();
              for(Zone zone : listZones){
                if(zone != null){
                  JSONObject jsonZone = zone.toJson();
                  if(jsonZone != null){
                    zones.put(jsonZone);
                  }
                }
              }
              if(zones != null && zones.length() > 0)
                requestParams.put(ParamConstants.ZONES, zones);
            }
          }
          requestParams.put(ParamConstants.ACTION, action);
          requestParams.put(ParamConstants.STATUS, action.replaceFirst("(?i)" + AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst("(?i)" + AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst("(?i)" + AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
          if(sessionObject != null && sessionObject.sessionId != null){
            requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
            if(isUpload){
              new Handler().post(new Runnable(){
                @Override
                public void run(){
                  try{
                    JSONArray js = new JSONArray();
                    List<Inventory> dataList = AppDatabase.getInventoryDao(context).getAllInventoryData(sessionObject.sessionId);
                    if(isNonEmpty(dataList)) for(Inventory inventory : dataList){
                      JSONObject dataobject = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_SHORT_JSON_REQUEST_FOR_INVENTORY_UPLOAD) ? inventory.toOnlyEpcJson() : inventory.toJson(context);
                      if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                        js.put(dataobject);
                    }
                    requestParams.put(ParamConstants.ITEMS, js);
                    allowBtnClick = true;
                    callWebService(URLConstants.SCAN_COUNT, requestParams, getString(R.string.progress_msg_uploading_data), !isUpload);
                  }
                  catch(Exception e){
                    e.printStackTrace();
                    hideProgressDialog();
                    allowBtnClick = true;
                  }
                }
              });
            }
          }
          if(!isUpload){
            allowBtnClick = true;
            //callWebService(isUpload ? URLConstants.UPLOAD_INVENTORY : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : action.matches("(?i)(" + AppConstants.SESSION_ACTION_START + "|" + AppConstants.SESSION_ACTION_RESUME + ")") ? action + "ing Session...\nPlease wait..." : action + "ing Data...\nPlease wait...", !isUpload);
          }
        }
        catch(JSONException e){
          e.printStackTrace();
          if(!isUpload)
            setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
          else hideProgressDialog();
          allowBtnClick = true;
        }
      }
      else if(!isUpload)
        setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
    }
  }
  
  /**
   * Get inv count string.
   *
   * @return the string
   */
  public String getInvCount(){
    if(sessionObject != null) return sessionObject.total;
    final String loc = binding != null && binding.spinInventoryStartLocation != null ? chkNull(binding.spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL) : AppConstants.ALL;
    final Set<String> locations = binding != null && binding.spinInventoryStartLocation != null ? binding.spinInventoryStartLocation.getSelectedVals() : new HashSet<String>(0);
    if(brandWiseZoneInventoryDao == null || !brandWiseZoneInventoryDao.hasData()) return "-";
    else return chkZero(brandWiseZoneInventoryDao.getInvCount(loc, locations), "-");
  }
  
  /**
   * Set active users.
   */
  public void setActiveUsers(final int activeUsers){
    int oldActiveUsers = -2;
    try{
      oldActiveUsers = AppCommonMethods.parseInt(chkNull(binding.header.btnActiveDevices.getText().toString(), "-2"));
    }
    catch(NumberFormatException e){ e.printStackTrace(); }
    final int activeCount = chkNull(activeUsers, -2) >= 0 ? activeUsers : oldActiveUsers;
    binding.header.flActiveDevices.setVisibility(activeUsers >= -1 ? View.VISIBLE : View.GONE);
    binding.header.btnActiveDevices.setSelected(true);
    binding.header.btnActiveDevices.setText(activeCount >= 0 ? "" + activeCount : "");
  }
  
  /**
   * Set session action.
   *
   * @param action    the action
   * @param sessionId the session id
   */
  void setSessionAction(String action, String sessionId){
    setSessionAction(action, sessionId, null, null, null);
  }
  
  /**
   * Set session action.
   *
   * @param action          the action
   * @param sessionId       the session id
   * @param sessionTime     the session time
   * @param inventoryCount  the inventory count
   * @param activeUserCount the active user count
   */
  void setSessionAction(String action, String sessionId, String sessionTime, Long inventoryCount, Integer activeUserCount){
    setActiveUsers(activeUserCount != null ? activeUserCount.intValue() : -2);
    String totInvCount = sessionObject != null ? sessionObject.total : chkNull(inventoryCount, -1l) >= 0 ? chkZero(inventoryCount, "-") : getInvCount();
    showLog("totInvCount", "" + totInvCount);
    //binding.txtInventoryStartTotal.setText(totInvCount);
    // setTotalInvCount(totInvCount);
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      // sessionObject.zone = binding.spinInventoryStartLocation.getSelectedItem().toString();
      // final Object selZoneObj = binding.spinInventoryStartLocation.getSelectedObject();
      //  sessionObject.zoneId = selZoneObj != null && selZoneObj instanceof Zone ? ((Zone) selZoneObj).getZoneId() : "0";
      //  sessionObject.total = totInvCount;
      sessionObject.sessionType = AppCommonMethods.SessionType.SCAN.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.INVENTORY.getValue();
      sessionObject.userId = SharedPrefManager.getUserID();
      Calendar cc = Calendar.getInstance();
      if(chkNull(sessionTime, "").length() > 0 && sessionTime.matches(DATE_TIME_FORMAT_PATTERN)){
        try{
          cc.setTime(new SimpleDateFormat(DATE_TIME_FORMAT).parse(sessionTime));
        }
        catch(Exception e){ e.printStackTrace(); }
      }
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, chkZero(sessionValidTill, 48));
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = chkNull(sessionId, mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc));
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, false);
    }
    else if(sessionObject != null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_RESUME))
      mainViewModel.startSession(sessionObject, true);
    else if(sessionObject != null && !action.matches("(?i)(" + AppConstants.SESSION_ACTION_START + "|" + AppConstants.SESSION_ACTION_RESUME + ")")){
      mainViewModel.stopSession(sessionObject, action.matches("(?i)(" + AppConstants.SESSION_ACTION_UPLOAD + "|" + AppConstants.SESSION_ACTION_DISCARD + ")"));
      if(action.equalsIgnoreCase(AppConstants.SESSION_ACTION_SAVE)){
        context.showCustomAlertDialog("", getString(R.string.success_session_save), true, true, getString(R.string.btn_ok), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            context.clearBackStack();
          }
        });
      }
      else context.popBackStack();
    }
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.SET_SESSION:
          final String action = extractString(jsonRequest, ParamConstants.ACTION, sessionObject == null ? AppConstants.SESSION_ACTION_START : AppConstants.SESSION_ACTION_STOP);
          String sessionId = null, sessionTime = null;
          Long inventoryCount = null;
          if(isSuccess && jsonResponse != null){
            sessionId = extractString(jsonResponse, ParamConstants.SESSION_ID);
            sessionTime = extractString(jsonResponse, ParamConstants.SESSION_TIME);
            sessionValidTill = extractInt(jsonResponse, ParamConstants.SESSION_VALID_TILL, 48);
            final String loc = binding != null && binding.spinInventoryStartLocation != null ? chkNull(binding.spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL) : AppConstants.ALL;
            final Set<String> locations = binding != null && binding.spinInventoryStartLocation != null ? binding.spinInventoryStartLocation.getSelectedVals() : new HashSet<String>(0);
            inventoryCount = extractLong(jsonResponse, ParamConstants.INVENTORY_COUNT, AppDatabase.getBrandWiseZoneInventoryDao(context).getInvCount(loc, locations));
            activeUsers = extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, activeUsers);
          }
          setSessionAction(action, sessionId, sessionTime, inventoryCount, activeUsers);
          break;
        case URLConstants.SCAN_COUNT:
          if(isSuccess && jsonResponse != null){
            Log.e("RES", jsonResponse.toString());
            extractBoolean(jsonResponse, "Success");
            final String error = extractString(jsonResponse, "Error", "false").replaceFirst("(?i)(False)", "");
            if(isNonEmpty(error)){
              context.showCustomErrDialog(error);
              return;
            }
            mainViewModel.stopSession(sessionObject, true);
            final JSONObject data = extractJSONObject(jsonResponse, ParamConstants.DATA, jsonResponse);
            if(isNonEmpty(data))
              context.showCustomSuccessDialog(extractString(data, "BatchId", data.toString()), true);
          }
          break;
        case URLConstants.UPLOAD_INVENTORY:
          if(isSuccess && jsonResponse != null && sessionObject != null){
            removeObservers();
            mainViewModel.stopSession(sessionObject, true);
           // binding.txtInventoryStartScoreCount.setText("" + 0);
            binding.ctwInventoryStart.setScore(0);
          //  binding.txtInventoryStartAlien.setText("" + 0);
            binding.ctwAlien.setScore(0);
          //  binding.txtInventoryStartUnencoded.setText("" + 0);
            binding.ctwUnencoded.setScore(0);
            binding.btnUpload.setVisibility(View.GONE);
            //binding.btnSwipeUpload.setVisibility(View.GONE);
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), true);
          }
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}