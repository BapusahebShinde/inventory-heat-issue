package com.itek.retail.ui.inventory;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractLong;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUseAPICallForSessionInventory;
import static com.itek.retail.common.AppCommonMethods.toUnderScoreCase;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itek.retail.R;
import com.itek.retail.adapter.InvBrandwiseQtyAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.InsertDBEANs;
import com.itek.retail.common.InsertDBProductInvFilter;
import com.itek.retail.common.InventoryScanFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.BrandEanDao;
import com.itek.retail.database.ProductInvFilterDao;
import com.itek.retail.databinding.FragmentInventoryFilterBinding;
import com.itek.retail.model.BrandEans;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.customviews.HeaderSpinner;
import com.itek.retail.ui.customviews.MaxHeightRecyclerView;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The Inv brand fragment.
 */
public class InventoryFilterFragment extends InventoryScanFragment{
  
  BrandEanDao brandEanDao;
  ProductInvFilterDao productInvFilterDao;
  private FragmentInventoryFilterBinding binding;
  private InventoryBrandViewModel mViewModel;
  private HashMap<String, String> filters = new HashMap<>(0);
  private HashMap<String, Set<String>> selTempFilters = new HashMap<>(0);
  private AlertDialog advFilterDialog = null;
  private String selFilterZone = "";
  private JSONObject filtersVals = null;
  private boolean isAllNullFilters = true;
  private boolean isFilterApplied = false;
  //private Long inventoryCount = -1l;
  
  /**
   * Instantiates a new Inv brand fragment.
   */
  public InventoryFilterFragment(){
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
    brandEanDao = AppDatabase.getBrandEansDao(context);
    productInvFilterDao = AppDatabase.getProductInvFilterDao(context);
    brandWiseZoneInventoryDao = AppDatabase.getBrandWiseZoneInventoryDao(context);
  }
  
  private void setTotalInvCount(final String count){
    binding.ctwInventoryStart.setTotal(chkNull(count, "0"));
    /*binding.txtInventoryStartTotal.setText(chkNull(count,"-"));
    final long total = Long.parseLong(chkNull(chkNull(count,"-").replace("-", ""), "0"));
    binding.progressInventoryStart.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
    binding.txtInventoryStartTotal.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
    binding.divInvStartScore.setVisibility(total > 0 ? View.VISIBLE : View.GONE);*/
    
    //code to update constraints
    /*final ConstraintLayout root = (ConstraintLayout) binding.clInventoryStart;
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(root);
    constraintSet.clear(binding.txtInventoryStartScoreCount.getId(), ConstraintSet.TOP);
    constraintSet.clear(binding.txtInventoryStartScoreCount.getId(), ConstraintSet.BOTTOM);
    if(total <= 0)
      constraintSet.connect(binding.txtInventoryStartScoreCount.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
    constraintSet.connect(binding.txtInventoryStartScoreCount.getId(), ConstraintSet.BOTTOM, total > 0 ? binding.divInvStartScore.getId() : ConstraintSet.PARENT_ID, total > 0 ? ConstraintSet.TOP : ConstraintSet.BOTTOM);
    constraintSet.applyTo(root);*/
  }
  
  private boolean validateFilters(){
    if(isFilterApplied && binding.chkFilterApplied.isChecked()) return true;
    context.showCustomErrDialog(R.string.err_filter_apply);
    return false;
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(InventoryBrandViewModel.class);
    
    binding = FragmentInventoryFilterBinding.inflate(inflater, container, false);
    
    /*activeUsers = extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2);
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
    
    setActiveUsers(activeUsers);*/
    
    //API Call for Getting Active Users (When Restarting Inventory)
   /* if(isAPICallForSessionResume && sessionObject != null)
      apiCall(AppConstants.SESSION_ACTION_RESUME);*/
    
    /*List<String> listLocations = new ArrayList<String>(zoneDao.getAllNonDisplayZoneNames());
    if(SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_ALL_ZONE_INVENTORY_FOR_TAKE_STOCK, AppCommonMethods.isAllowAllZoneInventoryForTakeStock))
      listLocations.add(0, AppConstants.ALL);
    else if(isNullOrEmpty(listLocations))//Show error for 'No zones available'
      context.showCustomErrDialog(String.format(getString(R.string.err_no__available), binding.spinInventoryStartLocation.getLabel()), true);
    
    binding.spinInventoryStartLocation.setAdapter(listLocations, 1);*/
    
    setViews(binding.header, binding.spinInventoryStartLocation, binding.llSeekbarPower, binding.llBtnStart, binding.ctwInventoryStart, binding.ctwAlien, binding.ctwUnencoded, binding.llInventoryStartSessionLbls, binding.btnUpload);
    
    /*binding.spinInventoryStartLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())){
          if(isNonEmpty(binding.spinInventoryStartLocation.getSelectedItem()) && !binding.spinInventoryStartLocation.getSelectedItem().equalsIgnoreCase(selFilterZone)){
            selFilterZone = binding.spinInventoryStartLocation.getSelectedItem();
            filtersVals = null;
            inventoryCount = -1l;
            filters.clear();
            brandEanDao.deleteAll();
            binding.chkFilterApplied.setChecked(false);
            isFilterApplied = false;
            binding.chkApplyFilter.performClick();
          }
          //binding.txtInventoryStartTotal.setText(getInvCount());
          setTotalInvCount(getInvCount());
        }
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){
        *//*Empty Method (Default Overridden)*//*
      }
    });*/
    
    /*final String loc = sessionObject != null ? sessionObject.zone : extractString(getArguments(), AppConstants.LOCATION, "");
    showLog("loc", "" + loc);
    if(isNonEmpty(loc)) binding.spinInventoryStartLocation.setSelection(loc);
    binding.spinInventoryStartLocation.setEnabled(sessionObject == null);*/
    
    isFilterApplied = sessionObject != null && isNonEmpty(sessionObject.extras);
    binding.chkFilterApplied.setChecked(isFilterApplied);
    binding.chkApplyFilter.setVisibility(View.VISIBLE);
    binding.chkApplyFilter.setEnabled(sessionObject == null);
    binding.chkFilterApplied.setEnabled(sessionObject == null);
    
    //binding.txtInventoryStartTotal.setText(getInvCount());
    //setTotalInvCount(getInvCount());
    
    binding.chkApplyFilter.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        if(sessionObject == null && binding.spinInventoryStartLocation.validate()){
          if(isNonEmpty(selFilterZone) && binding.spinInventoryStartLocation.getSelectedItem().matches(selFilterZone) && filtersVals != null){//TODO check if FilterDataTable is Empty
            showFilterDialog(filtersVals);
          }
          else{
            try{
              filtersVals = null;
              inventoryCount = -1l;
              filters.clear();
              JSONObject requestParams = new JSONObject();
              requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              requestParams.put(ParamConstants.TYPE, AppConstants.BRAND_STOCK);
              final Set<String> selZones = sessionObject != null ? new HashSet<String>(Arrays.asList(sessionObject.zone.split(","))) : binding.spinInventoryStartLocation.getSelectedVals();
              final String selZone = sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL);
              if(selZone.equalsIgnoreCase(AppConstants.ALL)){
                requestParams.put(ParamConstants.ZONE_ID, 0);
                requestParams.put(ParamConstants.ZONE, selZone);
              }
              else{
                List<Zone> listZones = selZone.contains(",") ? AppDatabase.getZoneDao(context).getZoneByName(selZones) : AppDatabase.getZoneDao(context).getZoneByName(selZone);
                if(isNonEmpty(listZones) && listZones.size() == 1){
                  final Zone zone = listZones.get(0);
                  requestParams.put(ParamConstants.ZONE_ID, zone != null ? zone.zoneId : 0);
                  if(isDebugApp && false){
                    requestParams.put(ParamConstants.ZONE, zone != null ? zone.zoneName : sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem().toString(), AppConstants.ALL));
                    requestParams.put(ParamConstants.ZONE_NAME, zone != null ? zone.zoneName : sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem().toString(), AppConstants.ALL));
                    requestParams.put(ParamConstants.ZONE_TYPE, zone != null ? zone.zoneType : null);
                    requestParams.put(ParamConstants.IS_DEFAULT_ZONE, zone != null ? zone.isDefault : false);
                  }
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
                //make a common method like CallInvFilterAPI for this
                //call same API after Selecting a Filter Value for Refreshing other filter values
                requestParams.put(ParamConstants.FILTERS, null);
              }
              showLog(URLConstants.GET_INVENTORY_FILTERS, "" + requestParams);
              callWebService(URLConstants.GET_INVENTORY_FILTERS, requestParams, getString(R.string.progress_msg_getting_data), false, true);
            }
            catch(Exception e){ e.printStackTrace(); }
          }
        }
      }
    });
    
    /*binding.llSeekbarPower.setupProgress(mainViewModel);
    
    String lastInventoryDate = SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.INVENTORY_TAKEN_DATE_TIME, "T").replaceFirst("T", " ").trim();
    if(isNonEmpty(lastInventoryDate) && lastInventoryDate.trim().contains(" ") && lastInventoryDate.length() >= 5){
      context.setTooltipText(binding.imgInventoryStartInfo, HtmlCompat.fromHtml(isNonEmpty(lastInventoryDate) && lastInventoryDate.trim().contains(" ") && lastInventoryDate.trim().split(" ").length > 1 && lastInventoryDate.length() >= 5 ? String.format(getString(R.string.txt_inventory_date_time), lastInventoryDate.split(" ")[0], lastInventoryDate.split(" ")[1]) : "", HtmlCompat.FROM_HTML_MODE_LEGACY));
    }*/
    
    //Redirection to Unencoded Search
   /* binding.clUnencodedTags.setVisibility(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_BRAND_INV) && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV) && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_COUNT_IN_INV) ? View.VISIBLE : View.GONE);
    binding.clUnencodedTags.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && sessionObject != null && sessionObject.sessionId != null && getSize() > 0 && !chkNotNullTrue(mainViewModel.getIsProcessOn().getValue()) && Integer.parseInt(chkZero(binding.txtInventoryStartUnencoded.getText().toString(), "0")) > 0){
          Bundle args = new Bundle();
          args.putString(AppConstants.UNENCODED_SEARCH_TYPE, AppConstants.UNENCODED_SEARCH_TYPE_ONLINE);
          args.putString(ParamConstants.ZONE, sessionObject.zone);
          args.putString(ParamConstants.SESSION_ID, sessionObject.sessionId);
          args.putInt(ParamConstants.SESSION_TYPE, sessionObject.sessionType);
          context.loadFragment(new SearchUnencodedFragment(), args);
        }
      }
    });
    
    //Redirection to Alien Search (with Title changed)
    binding.clAlienTags.setVisibility(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_BRAND_INV) && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV) && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_ALIEN_COUNT_IN_INV) ? View.VISIBLE : View.GONE);
    binding.clAlienTags.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && sessionObject != null && sessionObject.sessionId != null && getSize() > 0 && !chkNotNullTrue(mainViewModel.getIsProcessOn().getValue()) && Integer.parseInt(chkZero(binding.txtInventoryStartAlien.getText().toString(), "0")) > 0){
          Bundle args = new Bundle();
          final MenuModel menuAlienSearch = AppDatabase.getMenuDao(context).getMenuByCode(AppConstants.MENU_CODE_SER_ALIEN);
          args.putString(AppConstants.TITLE, "Alien Search");
          args.putString(AppConstants.TITLE_LOGO_URL, menuAlienSearch != null ? menuAlienSearch.getScreenImageUrl() : "");
          args.putInt(AppConstants.TITLE_LOGO_RES_ID, menuAlienSearch != null ? menuAlienSearch.getScreenIconId(context) : R.drawable.ic_ser_unencoded);
          args.putString(AppConstants.ALIEN_SEARCH_TYPE, AppConstants.ALIEN_SEARCH_TYPE_ONLINE);
          args.putString(ParamConstants.ZONE, sessionObject.zone);
          args.putString(ParamConstants.SESSION_ID, sessionObject.sessionId);
          args.putInt(ParamConstants.SESSION_TYPE, sessionObject.sessionType);
          context.loadFragment(new SearchAlienFragment(), args);
        }
      }
    });*/
    
    /*binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(advFilterDialog != null && advFilterDialog.isShowing()) return;
        context.dismissCustomAlertDialog();
        final Boolean isInventorySessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
        if(!isInventorySessionOn){
          if(sessionObject != null) mainViewModel.startSession(sessionObject, eans, true);
          else if(sessionObject == null){
            //Call API & Start Inventory Session
            if(binding.spinInventoryStartLocation.validate() && validateFilters()) //TODO validation for filter applied
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
    });*/
    
    /*binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
          return;
        showConfirmUploadDialog();//showBrandInventoryUploadSummary();
      }
    });
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          binding.btnSwipeUpload.reset();
          showConfirmUploadDialog();//showBrandInventoryUploadSummary();
        }
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);*/
    
    //commented by Bhupen Morgaonkar on 26-09-2023 to avoid long time wait to user to start inventory
    //if(savedInstanceState == null && sessionObject == null && !brandWiseZoneInventoryDao.hasData())
    //callWebService(URLConstants.GET_INVENTORY_DASHBOARD, new JSONObject(), getString(R.string.progress_msg_getting_data), true, false);
    
    return binding.getRoot();
  }
  
  @Override
  protected void onSpinInventoryLocationItemSelected(){
    if(isNonEmpty(binding.spinInventoryStartLocation.getSelectedItem()) && !binding.spinInventoryStartLocation.getSelectedItem().equalsIgnoreCase(selFilterZone)){
      selFilterZone = binding.spinInventoryStartLocation.getSelectedItem();
      filtersVals = null;
      inventoryCount = -1l;
      filters.clear();
      brandEanDao.deleteAll();
      binding.chkFilterApplied.setChecked(false);
      isFilterApplied = false;
      binding.chkApplyFilter.performClick();
    }
    super.onSpinInventoryLocationItemSelected();
  }
  
  @Override
  protected void onBtnStartClick(){
    if(advFilterDialog != null && advFilterDialog.isShowing()) return;
    super.onBtnStartClick();
  }
  
  @Override
  protected void onBtnUploadSwiped(){
    super.onBtnUploadSwiped();
  }
  
  /**
   * Show brand inventory upload summary.
   */
  private void showBrandInventoryUploadSummary(){
    List<BrandEans> listBrandEans = brandEanDao.getBrandwiseCount();
    MaxHeightRecyclerView listDisplayData = isNonEmpty(listBrandEans) ? new MaxHeightRecyclerView(context) : null;
    if(listDisplayData != null){
      int margin = getResources().getDimensionPixelSize(R.dimen.dp_15);
      LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      llParams.setMargins(margin, 0, margin, 0);
      listDisplayData.setLayoutParams(llParams);
      listDisplayData.setPadding(margin, 0, margin, 0);
      listDisplayData.setAdapter(new InvBrandwiseQtyAdapter((MainActivity) context, this, listBrandEans));
      listDisplayData.setLayoutManager(new LinearLayoutManager(context));
    }
    context.showCustomAlertDialog(getString(R.string.title_summary), "", listDisplayData, R.string.btn_upload, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        showConfirmUploadDialog();
      }
    }, R.string.btn_save, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        context.showCustomConfirmDialog(getString(R.string.title_back_session_save), R.string.btn_save, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            apiCall(AppConstants.SESSION_ACTION_SAVE);
          }
        });
      }
    });
  }
  
  private void showConfirmUploadDialog(){
    context.showCustomConfirmDialog(getString(R.string.title_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        apiCall(AppConstants.SESSION_ACTION_UPLOAD);
      }
    });
  }
  
  /*@Override
  protected void onReaderConfigured(){
    super.onReaderConfigured();
  }*/
  
  @Override
  protected void isSessionOnChanged(final Boolean isInventorySessionOn){
    super.isSessionOnChanged(isInventorySessionOn);
    if(isInventorySessionOn == null) return;
    boolean isInvSessionOn = chkNotNullTrue(isInventorySessionOn);
    
    //binding.spinInventoryStartLocation.setEnabled(!isInvSessionOn);
    binding.chkApplyFilter.setEnabled(!isInvSessionOn);
    binding.chkFilterApplied.setEnabled(!isInvSessionOn);
    //binding.llBtnStart.toggle(isInvSessionOn);
    
   /* final boolean isInvCount = getSize() > 0;//AppCommonMethods.parseInt(chkNull(binding.txtInventoryStartScoreCount.getText().toString(), "0")) > 0;
    final boolean isShowLastInventoryDate = chkNull(SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.INVENTORY_TAKEN_DATE_TIME, "T").replaceFirst("T", " ").trim(), "").length() >= 5;
    binding.imgInventoryStartInfo.setVisibility(isShowLastInventoryDate && !chkNotNullTrue(isInventorySessionOn) ? View.VISIBLE : View.GONE);
    binding.btnUpload.setVisibility(isAllowInventoryUpload && chkNotNullFalse(isInventorySessionOn) && isInvCount && sessionObject != null ? View.VISIBLE : View.GONE);
    binding.btnSwipeUpload.setVisibility(isAllowInventoryUpload && AppCommonMethods.isUploadSlider && chkNotNullFalse(isInventorySessionOn) && isInvCount && sessionObject != null ? View.VISIBLE : View.GONE);*/
  }
  
  /*@Override
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
      binding.imgInventoryStartInfo.setVisibility(isShowLastInventoryDate && !chkNotNullTrue(isInventoryOn) ? View.VISIBLE : View.GONE);
      binding.btnUpload.setVisibility(isAllowInventoryUpload && chkNotNullFalse(isInventoryOn) && isInvCount && sessionObject != null ? View.VISIBLE : View.GONE);
      binding.btnSwipeUpload.setVisibility(isAllowInventoryUpload && AppCommonMethods.isUploadSlider && chkNotNullFalse(isInventoryOn) && isInvCount && sessionObject != null ? View.VISIBLE : View.GONE);
    }
    
  }*/
  
  /*@Override
  protected void onTriggerPressed(){
    super.onTriggerPressed();
    //binding.llBtnStart.performClick();
  }*/
  
  @Override
  protected void onDataSizeChanged(Integer size){
    super.onDataSizeChanged(size);
  }
  
  @Override
  protected boolean validateBeforeInvSessionStart(){
    return super.validateBeforeInvSessionStart() && validateFilters();
  }
  
  /*@Override
  protected void onReaderPowerChanged(Integer power){
    super.onReaderPowerChanged(power);
    binding.llSeekbarPower.updateReaderPower(mainViewModel, power);
  }*/
  
  @Override
  public void apiCall(String action){
    showLog(InventoryFilterFragment.class.getSimpleName(), "apiCall=" + action);
    /*final Boolean isInventorySessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
    if(isInventorySessionOn != null){*/
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    if(isUseAPICallForSessionInventory && isInternetConnected(context, false, isUpload || (sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)))){
      try{
        if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        //Send Empty Array in JSON Request if value is 'All'
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        requestParams.put(ParamConstants.TYPE, AppConstants.BRAND_STOCK);
        final Set<String> selZones = sessionObject != null ? new HashSet<String>(Arrays.asList(sessionObject.zone.split(","))) : binding.spinInventoryStartLocation.getSelectedVals();
        final String selZone = sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem().toString(), AppConstants.ALL);
        if(selZone.equalsIgnoreCase(AppConstants.ALL)){
          requestParams.put(ParamConstants.ZONE_ID, 0);
          requestParams.put(ParamConstants.ZONE, selZone);
        }
        else{
          List<Zone> listZones = selZone.contains(",") ? AppDatabase.getZoneDao(context).getZoneByName(selZones) : AppDatabase.getZoneDao(context).getZoneByName(selZone);
          if(isNonEmpty(listZones) && listZones.size() == 1){
            final Zone zone = listZones.get(0);
            requestParams.put(ParamConstants.ZONE_ID, zone != null ? zone.zoneId : 0);
            requestParams.put(ParamConstants.ZONE, zone != null ? zone.zoneName : sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem().toString(), AppConstants.ALL));
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
            if(zones != null && zones.length() > 0) requestParams.put(ParamConstants.ZONES, zones);
          }
        }
        if((sessionObject != null && isNonEmpty(sessionObject.extras)) || (isNonEmpty(filters) && !isAllNullFilters)){
          try{
            JSONObject jsonFilters = new JSONObject((sessionObject != null && isNonEmpty(sessionObject.extras) ? sessionObject.extras : isNonEmpty(filters) ? filters.toString() : "").replaceAll("\\{", "{\"").replaceAll("\\]\\}", "\"}").replaceAll("=\\[", "\":\"").replaceAll("\\], ", "\", \"").replaceAll("\\[", "\"").replaceAll("\"" + AppConstants.ALL + "\"", "null").trim());
            if(isNonEmpty(jsonFilters)) requestParams.put(ParamConstants.FILTERS, jsonFilters);
          }
          catch(Exception e){
            //Don't handle
          }
        }
        //else requestParams.put(ParamConstants.FILTERS, new JSONObject());//JSONObject.NULL);
        requestParams.put(ParamConstants.HAS_EANS, productInvFilterDao.hasData() || brandEanDao.hasData());
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
                  callWebService(URLConstants.UPLOAD_INVENTORY, requestParams, getString(R.string.progress_msg_uploading_data), !isUpload && !(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)));
                }
                catch(Exception e){
                  e.printStackTrace();
                  hideProgressDialog();
                }
              }
            });
          }
        }
        if(!isUpload)
          callWebService(isUpload ? URLConstants.UPLOAD_INVENTORY : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : action.matches("(?i)(" + AppConstants.SESSION_ACTION_START + "|" + AppConstants.SESSION_ACTION_RESUME + ")") ? action + "ing Session...\nPlease wait..." : action + "ing Data...\nPlease wait...", !isUpload && !(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)), !isUpload && sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START));
      }
      catch(JSONException e){
        e.printStackTrace();
        if(!isUpload)
          setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
        else hideProgressDialog();
      }
    }
    else if(!(isUpload || (sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START))))
      setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
    //}
  }
  
  @Override
  public String getInvCount(){
    if(sessionObject != null) return sessionObject.total;
    if(chkNull(inventoryCount, -1l) >= 0 || isFilterApplied)
      return inventoryCount == 0 ? "-" : String.valueOf(inventoryCount);
    return super.getInvCount();
  }
  
  @Override
  protected String getExtrasForSession(){
    return isNonEmpty(filters) ? filters.toString() : "";
  }
  
  void showFilterDialog(final JSONObject jsonResponse){
    selTempFilters.clear();
    final boolean isMultiSelect = extractBoolean(jsonResponse, ParamConstants.IS_MULTI_SELECT,false);
    final boolean isCascade = extractBoolean(jsonResponse, ParamConstants.IS_CASCADE,false);
    final boolean isCascadeWithDBData = isCascade && productInvFilterDao.hasData(getSessionType().getValue());
    final JSONObject jsonFilters = extractJSONObject(jsonResponse, ParamConstants.FILTERS, jsonResponse);
    AppCommonMethods.showLog("keys_jsonFilters", "" + jsonFilters.length());
    final Iterator<String> keys = jsonFilters.keys();
    if(advFilterDialog != null && advFilterDialog.isShowing()) advFilterDialog.dismiss();
    final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialog).create();
    alertDialog.setCancelable(false);
    setAlertDialogCustomTitle(alertDialog, R.string.title_set_filter);
    final int margin = getResources().getDimensionPixelSize(R.dimen.dp_15);
    final LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
    llParams.setMargins(margin, 0, margin, 0);
    final LinearLayout llMain = new LinearLayout(context);
    llMain.setOrientation(LinearLayout.VERTICAL);
    llMain.setLayoutParams(llParams);
    llMain.setPadding(margin, 0, margin, 0);
    int counter = 0;
    final int rowLimit = ((jsonFilters.length() - 1) > 9 ? 3 : 2) + (isLandscape ? 1 : 0);
    showLog("keys_rowLimit", "" + rowLimit);
    LinearLayout llRow = null;
    while(keys.hasNext()){
      final String key = keys.next();
      try{
        final JSONObject jsonObject = extractJSONObject(jsonFilters, key);
        final JSONArray jsonArray = extractJSONArray(jsonObject, ParamConstants.ITEMS, extractJSONArray(jsonFilters, key));
        final String displayKey = extractString(jsonObject, ParamConstants.LABEL, key);
        final List<String> listVals = new ArrayList<String>(0);
        boolean isLastRowSet = false;
        if(isNonEmpty(jsonArray)){
          for(int i = 0; i < jsonArray.length(); i++){
            final Object obj = jsonArray.get(i);
            final String val = obj != null ? obj.toString().trim() : "";
            if(isNonEmpty(val)) listVals.add(val);
          }
          if(isNonEmpty(listVals)){
            if(!listVals.contains(AppConstants.ALL)) listVals.add(0, AppConstants.ALL);
            final HeaderSpinner headerSpinner = new HeaderSpinner(context);
            final LinearLayout.LayoutParams headSpinParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            headSpinParams.gravity = Gravity.CENTER;
            headerSpinner.setId(111 + (++counter));
            headerSpinner.setLayoutParams(headSpinParams);
            headerSpinner.setBackgroundResource(R.drawable.border_light);
            headerSpinner.setLabel(displayKey);//toTitleCase(key));
            headerSpinner.setTag(key);//toTitleCase(key));
            headerSpinner.setAdapter(listVals, isMultiSelect);
            if(isCascade){
              headerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                  if(isCascadeWithDBData){
                    if(isNullOrEmpty(headerSpinner.getSelectedVals()) || headerSpinner.getSelectedVals().contains(AppConstants.ALL))
                      selTempFilters.remove(key);
                    else selTempFilters.put(key, headerSpinner.getSelectedVals());
                    resetCascadeFilters(llMain, isMultiSelect, key, selTempFilters);
                  }
                  else {
                    //API based cascading ?
                  }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent){

                }
              });
            }
            if(isNonEmpty(filters) && filters.containsKey(key) && isNonEmpty(filters.get(key)) && !filters.get(key).replaceAll("(\\[|\\])", "").trim().matches("(?i)("+AppConstants.ALL+")")){
              headerSpinner.setSelection(new HashSet<String>(Arrays.asList(filters.get(key).replaceAll("\\s*,\\s*", ",").replaceAll("(\\[|\\])", "").trim().split(","))));
              if(isCascadeWithDBData) selTempFilters.put(key, headerSpinner.getSelectedVals());
            }
            if(llRow == null || (counter - 1) % rowLimit == 0){//((counter - 1) % rowLimit == 0 && keys.hasNext())){
              if(llRow != null && llRow.getChildCount() >= rowLimit) llMain.addView(llRow);
              llRow = new LinearLayout(context);
              llRow.setOrientation(LinearLayout.HORIZONTAL);
              llRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            llRow.addView(headerSpinner);
            if(!keys.hasNext()){
              isLastRowSet = true;
              llMain.addView(llRow);
            }
          }
        }
        if(!isLastRowSet && llRow != null && llRow.getChildCount() > 0 && !keys.hasNext())
          llMain.addView(llRow);
        else if(isLastRowSet) isLastRowSet = false;
      }
      catch(Exception e){ e.printStackTrace(); }
    }
    if(llMain != null && llMain.getChildCount() > 0){
      alertDialog.setView(llMain);
      alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.btn_apply), (DialogInterface.OnClickListener) null);
      alertDialog.setOnShowListener(new DialogInterface.OnShowListener(){
        @Override
        public void onShow(DialogInterface dialogInterface){
          if(isCascadeWithDBData && isNonEmpty(selTempFilters)) resetCascadeFilters(llMain, isMultiSelect, "", selTempFilters);
          final Button posBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
          posBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
              filters.clear();
              isAllNullFilters = true;
              updateFilters(llMain);
              AppCommonMethods.showLog("filters", filters.toString());
              if(filters.size() == 0 || isAllNullFilters){//TODO configurable (i.e. if allowed to show error)
                context.showCustomErrDialog(R.string.err_filter_select);
                return;
              }
              try{
                if(isCascadeWithDBData){
                  if(isNonEmpty(selFilterZone)) selFilterZone = getSelectedZone();
                  eans.clear();
                  eans.addAll(productInvFilterDao.getList(selFilterZone,"ean",selTempFilters));
                  inventoryCount = productInvFilterDao.getInvCount(selFilterZone, selTempFilters);
                  onPostExecute(URLConstants.SET_INVENTORY_FILTERS,AppConstants.SESSION_ACTION_START,null,null,true);
                  return;
                }
                //Send Empty Array in JSON Request if value is 'All'
                JSONObject requestParams = new JSONObject();
                requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
                requestParams.put(ParamConstants.TYPE, AppConstants.BRAND_STOCK);
                final Set<String> selZones = sessionObject != null ? new HashSet<String>(Arrays.asList(sessionObject.zone.split(","))) : binding.spinInventoryStartLocation.getSelectedVals();
                final String selZone = sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem().toString(), AppConstants.ALL);
                if(selZone.equalsIgnoreCase(AppConstants.ALL)){
                  requestParams.put(ParamConstants.ZONE_ID, 0);
                  requestParams.put(ParamConstants.ZONE, selZone);
                }
                else{
                  List<Zone> listZones = selZone.contains(",") ? AppDatabase.getZoneDao(context).getZoneByName(selZones) : AppDatabase.getZoneDao(context).getZoneByName(selZone);
                  if(isNonEmpty(listZones) && listZones.size() == 1){
                    final Zone zone = listZones.get(0);
                    requestParams.put(ParamConstants.ZONE_ID, zone != null ? zone.zoneId : 0);
                    requestParams.put(ParamConstants.ZONE, zone != null ? zone.zoneName : sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem().toString(), AppConstants.ALL));
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
                //final String brands = sessionObject != null ? sessionObject.brands : chkNull(binding.spinInventoryStartBrand.getSelectedItem().toString(), AppConstants.ALL);
                /*final String brands = sessionObject != null ? sessionObject.brands : chkNull(binding.spinInventoryStartBrand.getAllItems(), chkNull(binding.spinInventoryStartBrand.getSelectedItem(), AppConstants.ALL));
                requestParams.put(ParamConstants.BRANDS, brands);*/
                //selFilterBrand = brands;
                selFilterZone = selZone;
                if((sessionObject != null && isNonEmpty(sessionObject.extras)) || (isNonEmpty(filters) && !isAllNullFilters)){
                  try{
                    JSONObject jsonFilters = new JSONObject((sessionObject != null && isNonEmpty(sessionObject.extras) ? sessionObject.extras : isNonEmpty(filters) ? filters.toString() : "").replaceAll("\\{", "{\"").replaceAll("\\]\\}", "\"}").replaceAll("=\\[", "\":\"").replaceAll("\\], ", "\", \"").replaceAll("\\[", "\"").replaceAll("\"" + AppConstants.ALL + "\"", "null").trim());
                    if(isNonEmpty(jsonFilters))
                      requestParams.put(ParamConstants.FILTERS, jsonFilters);
                  }
                  catch(Exception e){ e.printStackTrace(); }
                }
                else requestParams.put(ParamConstants.FILTERS, new JSONObject());//JSONObject.NULL);
                showLog(URLConstants.SET_INVENTORY_FILTERS, "" + requestParams);
                callWebService(URLConstants.SET_INVENTORY_FILTERS, requestParams, getString(R.string.progress_msg_apply_filter), false, true);
              }
              catch(Exception e){ e.printStackTrace(); }
            }
          });
          advFilterDialog = alertDialog;
        }
      });
      alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
        @Override
        public void onDismiss(DialogInterface dialogInterface){
          advFilterDialog = null;
        }
      });
      alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
          context.handleTriggerKeyEvent(keyCode, event);
          return false;
        }
      });
      Window window = alertDialog.getWindow();
      WindowManager.LayoutParams wlp = window.getAttributes();
      wlp.gravity = Gravity.TOP;
      if(isLandscape){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int columns = 2;
        final int width = displayMetrics.widthPixels * (80 / 100);
        wlp.width = width;
      }
      alertDialog.show();
    }
    hideProgressDialog();
  }
  
  void resetCascadeFilters(final LinearLayout llMain,final boolean isMultiSelect,final String selTag, final HashMap<String, Set<String>> selTempFilters){
    for(int i = 0; i < llMain.getChildCount(); i++){
      final View view = llMain.getChildAt(i);
      if(view != null && view instanceof HeaderSpinner){
        HeaderSpinner hs = (HeaderSpinner) view;
        final String val = (isNonEmpty(hs.getSelectedVals()) && !hs.getSelectedVals().contains(AppConstants.ALL) ? hs.getSelectedVals().toString() : chkNull(hs.getSelectedItem(), AppConstants.ALL)).trim();
        final String tag = chkNull(hs.getTag() != null && hs.getTag() instanceof String ? hs.getTag().toString() : "", "").trim();
        AppCommonMethods.showLog("val", val);
        if(!selTag.equalsIgnoreCase(tag) && !selTempFilters.containsKey(tag)){
          final List<String> listVals = productInvFilterDao.getList(getSelectedZone(),tag,selTempFilters);
          if(!listVals.contains(AppConstants.ALL)) listVals.add(0, AppConstants.ALL);
          hs.setAdapter(listVals,isMultiSelect);
        }
      }
      else if(view != null && view instanceof LinearLayout){
        final LinearLayout llRow = (LinearLayout) view;
        resetCascadeFilters(llRow,isMultiSelect,selTag,selTempFilters);
      }
    }
  }
  
  void updateFilters(final LinearLayout llMain){
    for(int i = 0; i < llMain.getChildCount(); i++){
      final View view = llMain.getChildAt(i);
      if(view != null && view instanceof HeaderSpinner){
        HeaderSpinner hs = (HeaderSpinner) view;
        final String val = (isNonEmpty(hs.getSelectedVals()) && !hs.getSelectedVals().contains(AppConstants.ALL) ? hs.getSelectedVals().toString() : chkNull(hs.getSelectedItem(), AppConstants.ALL)).trim();
        final String tag = chkNull(hs.getTag() != null && hs.getTag() instanceof String ? hs.getTag().toString() : "", "").trim();
        AppCommonMethods.showLog("val", val);
        //Remove/Don't Put Filter if value is 'All'
        if(!val.equalsIgnoreCase(AppConstants.ALL)) isAllNullFilters = false;
        filters.put(chkNull(tag, toUnderScoreCase(hs.getLabel().trim())), !val.matches("^\\[.*\\]$") ? "[" + val + "]" : val);
      }
      else if(view != null && view instanceof LinearLayout){
        final LinearLayout llRow = (LinearLayout) view;
        updateFilters(llRow);
      }
    }
  }
  
  public void onDBFilterSaved(JSONObject filters){
    if(isNonEmpty(filters)){
      filtersVals = filters;
      showFilterDialog(filtersVals);
    }
  }
  
  /*public void updateInvCount(){
    if(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) setTotalInvCount(getInvCount());
  }*/
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        /*case URLConstants.GET_INVENTORY_DASHBOARD:
          if(isSuccess && jsonResponse != null){
            final String lastInventoryDate = extractString(jsonResponse, ParamConstants.LAST_INVENTORY_TAKEN_TIME, "T").replaceFirst("T", " ").trim();
            SharedPrefManager.setString(SharedPrefManager.SharedPrefKeys.INVENTORY_TAKEN_DATE_TIME, lastInventoryDate);
            if(isNonEmpty(lastInventoryDate) && lastInventoryDate.trim().contains(" ") && lastInventoryDate.length() >= 5){
              context.setTooltipText(binding.imgInventoryStartInfo, HtmlCompat.fromHtml(isNonEmpty(lastInventoryDate) && lastInventoryDate.trim().contains(" ") && lastInventoryDate.trim().split(" ").length > 1 && lastInventoryDate.length() >= 5 ? String.format(getString(R.string.txt_inventory_date_time), lastInventoryDate.split(" ")[0], lastInventoryDate.split(" ")[1]) : "", HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
            JSONArray inventoryCounts = extractJSONArray(jsonResponse, ParamConstants.BRAND_INVENTORY_COUNTS, extractJSONArray(jsonResponse, ParamConstants.CATEGORY_INVENTORY_COUNTS, extractJSONArray(jsonResponse, ParamConstants.INVENTORY_COUNTS)));
            //JSONArray brandwise = extractJSONArray(jsonResponse, ParamConstants.BRAND_INVENTORY_COUNTS);
            if(inventoryCounts != null && context instanceof MainActivity)
              ((MainActivity) context).callInsertBrandwiseDBTask(this, url, jsonRequest, jsonResponse, args, inventoryCounts);
            */
        /*else if(isNonEmpty(inventoryCounts)){
              showProgressDialog(getString(R.string.progress_msg_check_data));
              new Handler().post(new Runnable(){
                @Override
                public void run(){
                  if(isNonEmpty(inventoryCounts)){
                    AppCommonMethods.showLog("brandwise", "" + inventoryCounts.length());
                    for(int i = 0; i < inventoryCounts.length(); i++){
                      try{
                        JSONObject jobj = inventoryCounts.getJSONObject(i);
                        final String brandName = extractString(jobj, ParamConstants.BRAND_NAME, extractString(jobj, ParamConstants.NAME, "")).trim();
                        final String categoryName = extractString(jobj, ParamConstants.CATEGORY_NAME, extractString(jobj, ParamConstants.NAME, "")).trim();
                        final JSONArray categorywise = extractJSONArray(jobj, ParamConstants.CATEGORIES, extractJSONArray(jobj, ParamConstants.CATEGORY_INVENTORY_COUNTS));
                        final JSONArray brandwise = extractJSONArray(jobj, ParamConstants.BRANDS, extractJSONArray(jobj, ParamConstants.BRAND_INVENTORY_COUNTS));
                        final JSONArray zonewise = extractJSONArray(jobj, ParamConstants.ZONES);
                        parseDashboardResponse(categoryName, brandName, chkNull(zonewise, chkNull(categorywise, brandwise)), isNonEmpty(zonewise));
                        
                      }catch(Exception e){e.printStackTrace();}
                    }
                  }
                  hideProgressDialog();
                  updateInvCount();
                }
              });
            }*//*
            else{
              hideProgressDialog();
              updateInvCount();
            }
          }
          break;*/
        case URLConstants.GET_INVENTORY_FILTERS:
          if(isSuccess){
            //showProgressDialog(getString(R.string.progress_msg_check_data));
            final boolean isCascade = extractBoolean(jsonResponse, ParamConstants.IS_CASCADE);
            final JSONArray jsonArrayItems = extractJSONArray(jsonResponse, ParamConstants.ITEMS);
            if(isCascade && isNonEmpty(jsonArrayItems)){
              //TODO save data in the local db & show the dialog //TODO call DBTask for this
              new InsertDBProductInvFilter(context,this,url,jsonRequest,jsonResponse,args,getSessionType().getValue(),getSelectedZone(),getSelectedZoneObject().zoneId).execute(jsonArrayItems);
            }
            else{
              filtersVals = jsonResponse;
              showFilterDialog(jsonResponse);
            }
          }
          else{
            hideProgressDialog();
            filters.clear();
            filtersVals = null;
          }
          break;
        case URLConstants.SET_INVENTORY_FILTERS:
          if(isSuccess){
            //if(advFilterDialog != null && advFilterDialog.isShowing()) advFilterDialog.dismiss();
            try{
              inventoryCount = extractLong(jsonResponse, ParamConstants.INVENTORY_COUNT, Long.parseLong(chkNull(getInvCount(), "0").replace("-", "0")));
              activeUsers = extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, activeUsers);
            }
            catch(Exception e){ e.printStackTrace(); }
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
              hideProgressDialog();
              context.showCustomErrDialog(errMsg);
            }
            else{
              JSONArray jsonBrandEans = extractJSONArray(jsonResponse, ParamConstants.EANS);
              if(isNonEmpty(jsonBrandEans)){
                brandEanDao.deleteAll();
                new InsertDBEANs((MainActivity) context, this, url).execute(jsonBrandEans);
              }
            }
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  protected void handleResponseSetSession(String url, JSONObject jsonRequest, JSONObject jsonResponse, boolean isSuccess, Bundle args){
    final String selZone = getSelectedZone();
    final String action = extractString(jsonRequest, ParamConstants.ACTION, sessionObject == null ? AppConstants.SESSION_ACTION_START : AppConstants.SESSION_ACTION_STOP);
    final String sessionId = isSuccess && jsonResponse != null ? extractString(jsonResponse, ParamConstants.SESSION_ID) : null;
    final String sessionTime = isSuccess && jsonResponse != null ? extractString(jsonResponse, ParamConstants.SESSION_TIME) : null;
    if(isSuccess && jsonResponse != null){
      sessionValidTill = extractInt(jsonResponse, ParamConstants.SESSION_VALID_TILL, 48);
      inventoryCount = extractLong(jsonResponse, ParamConstants.INVENTORY_COUNT, Long.parseLong(chkNull(getInvCount(), "0").replace("-", "0")));
      activeUsers = extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, activeUsers);
      boolean hasEans = !action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) && sessionObject != null ? true : extractBoolean(jsonRequest, ParamConstants.HAS_EANS, false);
      JSONArray jsonEans = hasEans ? null : extractJSONArray(jsonResponse, ParamConstants.EANS);
      JSONArray jsonBrandEans = hasEans || isNonEmpty(jsonEans) ? null : extractJSONArray(jsonResponse, ParamConstants.BRAND_EANS);
      if(!hasEans && isNullOrEmpty(jsonEans) && isNonEmpty(jsonBrandEans)){
        ArrayList<String> listEans = new ArrayList<String>(0);
        for(int i = 0; i < jsonBrandEans.length(); i++){
          try{
            listEans.addAll(Arrays.asList(AppCommonMethods.extractString(jsonBrandEans.getJSONObject(i), ParamConstants.EANS, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").replaceAll("\\s*,\\s*", ",").trim().toUpperCase().split(",")));
          }
          catch(Exception e){
            e.printStackTrace();
          }
        }
        if(isNonEmpty(listEans)) jsonEans = new JSONArray(listEans);
      }
      final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
      if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
        if(!hasEans) hideProgressDialog();
        context.showCustomErrDialog(errMsg);
      }
      else{
        final JSONArray excludeEpcs = extractJSONArray(jsonResponse, ParamConstants.EXCLUDE_INVENTORY_EPCS);
        if(isNonEmpty(excludeEpcs)) extractIgnoreEpcs(excludeEpcs);
        /*if(isNonEmpty(jsonBrandEans)){
          eans.clear();
          for(int i = 0; i < jsonBrandEans.length(); i++){
            try{
              eans.addAll(Arrays.asList(AppCommonMethods.extractString(jsonBrandEans.getJSONObject(i), ParamConstants.EANS, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").replaceAll("\\s*,\\s*", ",").trim().toUpperCase().split(",")));
            }
            catch(Exception e){ e.printStackTrace(); }
          }
        }*/
        if(isNonEmpty(jsonEans))
          new InsertDBEANs((MainActivity) context, this, url).execute(jsonEans);
        else if(action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) && productInvFilterDao.hasData()){
          setSessionAction(action, sessionId, sessionTime, inventoryCount, activeUsers);
          hideProgressDialog();
        }
          //new InsertDBBrandEANs((MainActivity) context, this, url, action, sessionId, sessionTime, inventoryCount).execute(jsonBrandEans);
        else if(!action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) || brandEanDao.hasData()){//isNonEmpty(eans))
          if(action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) && brandEanDao.hasData() && isNonEmpty(selZone) && inventoryCount>0)
            brandEanDao.updateTotalQty(selZone, inventoryCount.intValue());
          setSessionAction(action, sessionId, sessionTime, inventoryCount, activeUsers);
          hideProgressDialog();
        }
      }
    }
  }
  
  @Override
  public void onDestroy(){
    super.onDestroy();
  }
  
  public FragmentInventoryFilterBinding getBinding(){
    return binding;
  }
  
  public void onPostExecute(final String url, final String action, final String sessionId, final String sessionTime, final Boolean result){
    hideProgressDialog();
    final String selZone = getSelectedZone();
    //final Set<String> selZones = getSelectedZones();
    if(result){
      if(url.equalsIgnoreCase(URLConstants.SET_INVENTORY_FILTERS)){
        if(advFilterDialog != null && advFilterDialog.isShowing()) advFilterDialog.dismiss();
        isFilterApplied = true;
        binding.chkFilterApplied.setChecked(true);
        setActiveUsers(activeUsers);
        String totInvCount = getInvCount();//sessionObject != null ? sessionObject.total : chkNull(inventoryCount, -1l) >= 0 || isFilterApplied ? chkZero(inventoryCount, "-") : getInvCount();
        showLog("totInvCount", "" + totInvCount);
        //binding.txtInventoryStartTotal.setText(totInvCount);
        setTotalInvCount(totInvCount);
        //onBtnStartClick();
      }
      else if(url.equalsIgnoreCase(URLConstants.SET_SESSION)){
        setActiveUsers(activeUsers);
        setTotalInvCount(getInvCount());
      }
    }
    if(url.equalsIgnoreCase(URLConstants.SET_SESSION) && (!action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) || brandEanDao.hasData())){//isNonEmpty(eans))
      if(action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) && brandEanDao.hasData() && inventoryCount>0)
        brandEanDao.updateTotalQty(selZone, inventoryCount.intValue());
      setSessionAction(action, sessionId, sessionTime, inventoryCount, activeUsers);
    }
  }
}