package com.itek.retail.ui.search.unencoded;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNonZeroId;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.OmniPickedListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ZoneDao;
import com.itek.retail.databinding.DialogOmniEpcSearchBinding;
import com.itek.retail.databinding.FragmentSearchUnencodedBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.customviews.DashboardDataView;
import com.itek.retail.ui.home.MainActivity;
import com.rscja.team.qcom.deviceapi.G;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * The Unencoded search fragment.
 */
public class SearchUnencodedFragment extends RFIDSessionFragment{
  
  public FragmentSearchUnencodedBinding binding;
  ZoneDao zoneDao;
  InventoryDao inventoryDao;
  private List<String> listZones = new ArrayList<>(0);
  private List<Inventory> listTags = new ArrayList<>(0);
  private SearchUnencodedViewModel mViewModel;
  private DialogOmniEpcSearchBinding dialogOmniEpcSearchBinding;
  private boolean showMarkFoundBtn = false;
  private boolean isRedirected = false;
  private boolean isVerifyEncoding = false;
  
  /**
   * Instantiates a new Unencoded search fragment.
   */
  public SearchUnencodedFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    zoneDao = AppDatabase.getZoneDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
    if(isAllowDirectionalSearch) mainViewModel.getSensorAndStart();
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(SearchUnencodedViewModel.class);
    binding = FragmentSearchUnencodedBinding.inflate(inflater, container, false);
    
    final String zone = extractString(getArguments(), ParamConstants.ZONE, "");
    final String sessionId = extractString(getArguments(), ParamConstants.SESSION_ID, "");
    final Integer sessionType = extractInt(getArguments(), ParamConstants.SESSION_TYPE, 0);
    isRedirected = isNonEmpty(zone) && isNonEmpty(sessionId) && chkZero(sessionType,0)>0;
    
    binding.rbOnline.setSelected(true);
    binding.rbOffline.setSelected(true);
    binding.rbOnline.setTag(AppConstants.UNENCODED_SEARCH_TYPE_ONLINE);
    binding.rbOffline.setTag(AppConstants.UNENCODED_SEARCH_TYPE_OFFLINE);
    
    listZones.clear();
    listZones.add(AppConstants.ALL);
    List<String> zones = zoneDao.getAllNonDisplayZones1();
    if(isNonEmpty(zones)) listZones.addAll(zones);
    
    binding.spinLocation.setAdapter(listZones);
    if(isRedirected /*&& !zone.equalsIgnoreCase(AppConstants.ALL)*/){
      isVerifyEncoding = sessionType.equals(AppCommonMethods.SessionType.ENCODING.getValue());
      
      binding.spinLocation.setSelection(zone);
      binding.spinLocation.setEnabled(false);
      if(isVerifyEncoding) {
        binding.spinLocation.setVisibility(View.GONE);
        binding.ddvUnencodedSearchTotalFound.setLabel(getString(R.string.lbl_verified_total));
        setSessionObject(mainViewModel.getCurrentSession(AppCommonMethods.SessionType.get(sessionType)));
        setDiscardData(false);
      }
    }
    
    binding.listSearchUnencoded.setAdapter(new OmniPickedListAdapter(context, SearchUnencodedFragment.this, listTags));
    binding.listSearchUnencoded.setLayoutManager(new LinearLayoutManager(context));
    
    binding.spinLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){/*Don't handle (Default Overridden Empty Method)*/}
    });
    
    binding.rgUnencodedSearchType.setVisibility(!isRedirected && chkNull(SharedPrefManager.getUnencodedSearchType(), AppConstants.UNENCODED_SEARCH_TYPE_BOTH).equalsIgnoreCase(AppConstants.UNENCODED_SEARCH_TYPE_BOTH) ? View.VISIBLE : View.GONE);
    binding.rbOffline.setChecked((extractString(getArguments(), AppConstants.UNENCODED_SEARCH_TYPE, chkNull(SharedPrefManager.getUnencodedSearchType(), AppConstants.UNENCODED_SEARCH_TYPE_BOTH)).equalsIgnoreCase(binding.rbOffline.getTag().toString())));
    
    binding.rgUnencodedSearchType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        listTags.clear();
        ((RecyclerView.Adapter) binding.listSearchUnencoded.getAdapter()).notifyDataSetChanged();
        callAPI();
        binding.header.imgConfigSync.setVisibility(isOfflineInventory() && !isProcessOn() ? View.VISIBLE : View.GONE);
        binding.llSeekbarPower.setVisibility(isOfflineInventory() && !isProcessOn() && binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
      }
    });
    
    binding.header.imgConfigSync.setVisibility(isOfflineInventory() && !isProcessOn() ? View.VISIBLE : View.GONE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_config);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(view != null && view.getVisibility() == View.VISIBLE){
          binding.llSeekbarPower.setVisibility(isOfflineInventory() && !isProcessOn() && binding.llSeekbarPower.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
        }
      }
    });
    
    binding.llSeekbarPower.setupProgress(mainViewModel);
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE){
          context.dismissCustomAlertDialog();
          final Boolean isSessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
          if(!isSessionOn){
            if(sessionObject != null) mainViewModel.startSession(sessionObject, true);
            else if(sessionObject == null){
              setSessionAction(AppConstants.SESSION_ACTION_START);
            }
          }
          else if(isSessionOn){
            if(isOfflineInventory()){
              if(getSize() >= AppCommonMethods.invLimit){
                if(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
                  mainViewModel.stopInventory();
                context.showCustomErrDialog(R.string.err_inventory_max_limit);
              }
              else toggleInventory();
            }
          }
        }
      }
    });
    
    if(isRedirected && sessionObject == null) setSessionAction(AppConstants.SESSION_ACTION_START);
    
    updateLists();
    return binding.getRoot();
  }
  
  @Override
  public void startEPCSearch(Inventory inventory){
    if(!isProcessOn() && inventory != null){
      //TODO show EPC & TID options for the Search
      final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
      setAlertDialogCustomTitle(alertDialog, R.string.search);
      DisplayMetrics displayMetrics = new DisplayMetrics();
      context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
      final int wid = (context.isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) / 2;

      JSONObject jsonExtras1 = null;
      try{
        jsonExtras1 = new JSONObject();
        jsonExtras1.put(ParamConstants.ZONE, isOfflineInventory() ? getSelLocation() : inventory.zone);
        jsonExtras1.put(ParamConstants.ZONE_ID, isOfflineInventory() ? getSelLocationId() : chkNull(inventory.zoneId, context != null && isNonEmpty(inventory.zone) && !inventory.zone.equalsIgnoreCase(AppConstants.ALL) ? chkNull(AppDatabase.getZoneDao(context).getZoneIdByName(inventory.zone), "0") : "0"));
        jsonExtras1.put(ParamConstants.EAN, chkNull(inventory.newEpc,inventory.epc));
        jsonExtras1.put(ParamConstants.EAN_QTY, 1);
        jsonExtras1.put(ParamConstants.IS_EAN_SEARCH, false);
        jsonExtras1.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        jsonExtras1.put(ParamConstants.TYPE, AppConstants.UNENCODED_SEARCH_TYPE);
        jsonExtras1.put(ParamConstants.UNENCODED_SEARCH_TYPE, getUnencodedSearchType());
      }
      catch(Exception e){ e.printStackTrace(); }

      final JSONObject jsonExtras = jsonExtras1;
      
      DialogOmniEpcSearchBinding binding = DialogOmniEpcSearchBinding.inflate(LayoutInflater.from(context), null, false);
      LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(wid, wid);
      binding.rgSearchType.setVisibility(isVerifyEncoding?View.VISIBLE:View.GONE);
      binding.clOmniEPCSearch.setLayoutParams(llParams);
      binding.btnDecode.setText(R.string.btn_mark_found);
      binding.btnDecode.setTag(inventory);
      showMarkFoundBtn = false;
      binding.btnDecode.setVisibility(!isProcessOn() && showMarkFoundBtn && !inventory.isFound ? View.VISIBLE : View.GONE);
      binding.btnDecode.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          //Mark as found
          if(view != null && view.getVisibility() == View.VISIBLE && !inventory.isFound){
            try{
              inventory.isFound = true;
              inventoryDao.updateInventoryData(inventory);
              binding.btnDecode.setTag(inventory);
              showMarkFoundBtn = false;
              binding.btnDecode.setVisibility(showMarkFoundBtn && !isProcessOn() && !inventory.isFound ? View.VISIBLE : View.GONE);
              updateLists();
              if(AppCommonMethods.isDismissSearchDialogWhenMarkedFound) alertDialog.dismiss();
            }
            catch(Exception e){ e.printStackTrace(); }
          }
        }
      });
      binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          context.dismissCustomAlertDialog();
          if(!isProcessOn() && checkReaderConnected()){
            if(binding.rgSearchType.getVisibility()== View.VISIBLE && binding.rgSearchType.findViewById(binding.rgSearchType.getCheckedRadioButtonId()).getTag().equals(getString(R.string.search_type_tid)) && isNonEmpty(inventory.tid)) {
              if(jsonExtras!=null) {
                try {
                  jsonExtras.put(ParamConstants.EAN, inventory.tid);
                } catch (JSONException e) {
                  throw new RuntimeException(e);
                }
              }
              mainViewModel.performTIDBasedSearch(inventory.tid);
              searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", inventory.tid, 1, getSessionType().name(), getUnencodedSearchType(), jsonExtras);
            }
            else{
              if(jsonExtras!=null) {
                try {
                  jsonExtras.put(ParamConstants.EAN, chkNull(inventory.newEpc, inventory.epc));
                } catch (JSONException e) {
                  throw new RuntimeException(e);
                }
              }
              mainViewModel.performEPCBasedSearch(chkNull(inventory.newEpc, inventory.epc));
              searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(inventory.newEpc, inventory.epc), 1, getSessionType().name(), getUnencodedSearchType(), jsonExtras);
            }
            searchStartTime = System.currentTimeMillis();
          }
          else if(chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()))
            mainViewModel.stopInventory();
        }
      });
      alertDialog.setView(binding.getRoot());
      alertDialog.setOnShowListener(new DialogInterface.OnShowListener(){
        @Override
        public void onShow(DialogInterface dialogInterface){
          dialogOmniEpcSearchBinding = binding;
          if(searchLog == null && !isVerifyEncoding){
            /*JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject();
              jsonExtras.put(ParamConstants.ZONE, isOfflineInventory() ? getSelLocation() : inventory.zone);
              jsonExtras.put(ParamConstants.ZONE_ID, isOfflineInventory() ? getSelLocationId() : chkNull(inventory.zoneId, context != null && isNonEmpty(inventory.zone) && !inventory.zone.equalsIgnoreCase(AppConstants.ALL) ? chkNull(AppDatabase.getZoneDao(context).getZoneIdByName(inventory.zone), "0") : "0"));
              jsonExtras.put(ParamConstants.EAN, inventory.epc);
              jsonExtras.put(ParamConstants.EAN_QTY, 1);
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, false);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, AppConstants.UNENCODED_SEARCH_TYPE);
              jsonExtras.put(ParamConstants.UNENCODED_SEARCH_TYPE, getUnencodedSearchType());
            }
            catch(Exception e){ e.printStackTrace(); }*/
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(inventory.newEpc, inventory.epc), 1, getSessionType().name(), getUnencodedSearchType(), jsonExtras);
          }
        }
      });
      alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
        @Override
        public void onDismiss(DialogInterface dialogInterface){
          if(chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()))
            mainViewModel.stopInventory();
          if(searchLog != null) searchLog = null;
          dialogOmniEpcSearchBinding = null;
        }
      });
      alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener(){
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event){
          context.handleTriggerKeyEvent(keyCode, event);
          return false;
        }
      });
      alertDialog.show();
    }
    else if(isProcessOn() && inventory != null) showShortToast(R.string.not_allowed);
    //showShortToast(String.format(getString(R.string.err_op_not_allowed),getTypeCharCode(), AppCommonMethods.SessionType.INVENTORY.name()));
  }
  
  public boolean isVerifyEncoding(){
    return isVerifyEncoding;
  }
  
  @Override
  protected void onTriggerPressed(){
    if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null)
      dialogOmniEpcSearchBinding.llBtnStart.performClick();
    else binding.llBtnStart.performClick();
  }
  
  /**
   * Get sel location string.
   *
   * @return the string
   */
  public String getSelLocation(){ return sessionObject != null ? chkNull(sessionObject.zone, "") : chkNull(binding.spinLocation.getSelectedItem(), AppConstants.ALL); }
  
  /**
   * Get sel location id string.
   *
   * @return the string
   */
  public String getSelLocationId(){
    if(sessionObject != null) return chkNull(sessionObject.zoneId, "0");
    final Object selLocationObj = binding.spinLocation.getSelectedObject();
    return selLocationObj != null && selLocationObj instanceof Zone ? chkNull(((Zone) selLocationObj).zoneId, "0") : "0";
  }
  
  /**
   * Get unencoded search type string.
   *
   * @return the string
   */
  public String getUnencodedSearchType(){
    return !isRedirected && !chkNull(SharedPrefManager.getUnencodedSearchType(), AppConstants.UNENCODED_SEARCH_TYPE_BOTH).equalsIgnoreCase(AppConstants.UNENCODED_SEARCH_TYPE_BOTH) ? SharedPrefManager.getUnencodedSearchType() : binding.rgUnencodedSearchType.findViewById(binding.rgUnencodedSearchType.getCheckedRadioButtonId()).getTag().toString().toLowerCase().trim();
  }
  
  private boolean isOfflineInventory(){
    return getUnencodedSearchType().equalsIgnoreCase(AppConstants.UNENCODED_SEARCH_TYPE_OFFLINE);
  }
  
  /**
   * Set session action.
   *
   * @param action the action
   */
  public void setSessionAction(String action){
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.zone = getSelLocation();
      sessionObject.zoneId = getSelLocationId();
      sessionObject.sessionType = AppCommonMethods.SessionType.SEARCH_UNENCODED.getValue();
      sessionObject.sessionAction = /*isOfflineInventory() ?*/ AppCommonMethods.SessionAction.INVENTORY.getValue() /*: AppCommonMethods.SessionAction.SEARCH.getValue()*/;
      sessionObject.userId = SharedPrefManager.getUserID();
      Calendar cc = Calendar.getInstance();
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, 24);
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc);
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, isOfflineInventory());
      if(!isOfflineInventory()) callAPI();
    }
    else if(sessionObject != null && !action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      //TODO
    }
  }
  
  private void callAPI(){
    if(getUnencodedSearchType().equalsIgnoreCase(AppConstants.UNENCODED_SEARCH_TYPE_ONLINE) && sessionObject != null){
      try{
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.UNENCODED_SEARCH_TYPE, getUnencodedSearchType());
        if(isRedirected)
          handleResponse(URLConstants.GET_UNENCODED_SEARCH_LIST, requestParams, null, 200, true, getArguments());
        else
          callWebService(URLConstants.GET_UNENCODED_SEARCH_LIST, requestParams, getString(R.string.progress_msg_connect_server), false, true);
      }
      catch(Exception e){ e.printStackTrace(); }
    }
    updateLists();
  }
  
  @Override
  public void onBackPressed(){
    super.onBackPressed();
  }
  
  /**
   * Set default search views.
   */
  @Override
  protected void setDefaultSearchViews(){
    super.setDefaultSearchViews();
    if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null)
      dialogOmniEpcSearchBinding.clOmniChannelEpcSearch.resetToDefault();
  }
  
  @Override
  protected void updateSearchUI(int result){
    super.updateSearchUI(result);
    final Object tag = dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.btnDecode != null ? dialogOmniEpcSearchBinding.btnDecode.getTag() : null;
    final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
    if(!isVerifyEncoding && !showMarkFoundBtn && result >= AppCommonMethods.markFoundPercentUnencodedSearch && inventory != null && !inventory.isFound)
      showMarkFoundBtn = true;
  }
  
  @Override
  public void setSessionObject(RFIDSession sessionObject){
    super.setSessionObject(sessionObject);
    if(sessionObject != null && sessionObject instanceof RFIDSession)
      this.sessionObject = (RFIDSession) sessionObject;
    showLog("searchSession", "" + (this.sessionObject != null));
  }
  
  @Override
  protected void onReaderConfigured(){
    super.onReaderConfigured();
  }
  
  @Override
  protected void isSessionOnChanged(Boolean isSessionOn){
    super.isSessionOnChanged(isSessionOn);
    boolean isSessionRunning = chkNotNullTrue(isSessionOn);
    binding.spinLocation.setEnabled(!isSessionRunning || !isOfflineInventory());
    binding.rbOffline.setEnabled(!isSessionRunning);
    binding.rbOnline.setEnabled(!isSessionRunning);
    binding.rgUnencodedSearchType.setEnabled(!isSessionRunning);
    binding.rgUnencodedSearchType.setVisibility(!isSessionRunning ? View.VISIBLE : View.GONE);
    updateLists();
  }
  
  @Override
  protected void isSearchOnChanged(Boolean isSearchOn){
    super.isSearchOnChanged(isSearchOn);
    if(isSearchOn == null) return;
    else{
      updateViews();
      updateLists();
      if(!isSearchOn) stopTimer();
      else
        startTimer(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null ? dialogOmniEpcSearchBinding.clOmniChannelEpcSearch : null, dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.imgSearchDir != null ? dialogOmniEpcSearchBinding.imgSearchDir : null);
      if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.clOmniChannelEpcSearch != null)
        dialogOmniEpcSearchBinding.clOmniChannelEpcSearch.setEnableCheck(false);
      if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null && dialogOmniEpcSearchBinding.btnDecode != null){
        dialogOmniEpcSearchBinding.llBtnStart.toggle(isSearchOn);
        final Object tag = dialogOmniEpcSearchBinding.btnDecode.getTag();
        final Inventory inventory = tag != null && tag instanceof Inventory ? (Inventory) tag : null;
        dialogOmniEpcSearchBinding.btnDecode.setVisibility(showMarkFoundBtn && inventory != null && !inventory.isFound && !isSearchOn ? View.VISIBLE : View.GONE);
      }
    }
  }
  
  @Override
  protected void onSearchPercentageChanged(Integer searchPercent, String searchRssi){
    super.onSearchPercentageChanged(searchPercent, searchRssi);
    updateViews();
    updateLists();
  }
  
  @Override
  protected void isInventoryOnChanged(Boolean isInventoryOn){
    super.isInventoryOnChanged(isInventoryOn);
    if(isInventoryOn == null) return;
    else{
      binding.llBtnStart.toggle(isInventoryOn);
      updateViews();
      updateLists();
    }
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    super.onDataSizeChanged(size);
    updateViews();
    updateLists();
  }
  
  private void updateViews(){
    final boolean isProcessOn = chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()) || chkNotNullTrue(mainViewModel.getIsSearchOn().getValue());
    if(dialogOmniEpcSearchBinding != null){
      dialogOmniEpcSearchBinding.llBtnStart.setEnabled(!chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()));
    }
    binding.llBtnStart.setEnabled(!chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()));
    binding.header.imgConfigSync.setVisibility(!isProcessOn && isOfflineInventory() ? View.VISIBLE : View.GONE);
    binding.llSeekbarPower.setEnabled(!isProcessOn);
    binding.llSeekbarPower.setVisibility(!isProcessOn && isOfflineInventory() && binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
  }
  
  @Override
  protected void onReaderPowerChanged(Integer power){
    binding.llSeekbarPower.updateReaderPower(mainViewModel, power);
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    
    final String selZone = binding.spinLocation.getSelectedItem();
    final Set<String> selZones = binding.spinLocation.getSelectedVals();
    listTags.clear();
    if(sessionObject != null){
        AppCommonMethods.showLog("sessionId",sessionObject.sessionId);
        AppCommonMethods.showLog("sessionType", AppCommonMethods.SessionType.get(sessionObject.sessionType).name());
        listTags.addAll(inventoryDao.getZoneList(sessionObject.sessionId, selZone));
        setFoundTotalText(binding.ddvUnencodedSearchTotalFound, inventoryDao.getZonewiseFound(sessionObject.sessionId, selZone), listTags.size());
    }
    ((RecyclerView.Adapter) binding.listSearchUnencoded.getAdapter()).notifyDataSetChanged();
    binding.llBtnStart.setVisibility(!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) || (isOfflineInventory() && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())) ? View.VISIBLE : View.GONE);
  }
  
  private void setFoundTotalText(DashboardDataView ddv, int found, int total){
    final int len = String.valueOf(AppCommonMethods.greater(found, total)).length();
    if(len > 3){
      final int loopLimit = len / 2;
      final String format = "<small>%s</small>";
      String appendFormat = format;
      for(int i = 0; i < loopLimit; i++)
        appendFormat = appendFormat.replaceFirst(">%s</", ">" + format + "</");
      ddv.setText(String.format(appendFormat, found) + "/" + String.format(appendFormat, total));
    }
    else ddv.setText(found + "/" + total);
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_UNENCODED_SEARCH_LIST:
          if(isSuccess){
            if(sessionObject != null) inventoryDao.deleteInventory(sessionObject.sessionId);
            else{
              inventoryDao.deleteInventory(AppCommonMethods.SessionType.SEARCH_UNENCODED.getValue());
              setSessionAction(AppConstants.SESSION_ACTION_START);
            }
            final String sessionId = extractString(args, ParamConstants.SESSION_ID, "");
            if(isRedirected && sessionId != null){
              final List<Inventory> listInventory = inventoryDao.getUnencodedTags(sessionId);
              if(isNonEmpty(listInventory) && sessionObject != null){
                int insertCount = 0;
                for(Inventory inv : listInventory){
                  Inventory serInv = new Inventory();
                  serInv.sessionId = sessionObject.sessionId;
                  serInv.sessionType = sessionObject.sessionType;
                  serInv.sessionAction = sessionObject.sessionAction;
                  serInv.tid = inv.tid;
                  serInv.epc = inv.epc;
                  serInv.pcdata = inv.pcdata;
                  serInv.newEpc = inv.newEpc;
                  serInv.ean = inv.ean;
                  serInv.tagtype = inv.tagtype;
                  serInv.isHardTag = inv.isHardTag;
                  serInv.tagStatus = inv.tagStatus;
                  serInv.isFound = inv.isFound;
                  serInv.isUploaded = inv.isUploaded;
                  serInv.rssi = inv.rssi;
                  serInv.retryUploadCount = inv.retryUploadCount;
                  serInv.zone = inv.zone;
                  serInv.zoneId = inv.zoneId;
                  serInv.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                  try{
                    inventoryDao.insertInventoryData(serInv);
                    insertCount++;
                  }
                  catch(Exception e){ e.printStackTrace(); }
                }
                showLog("insertCount", "" + insertCount);
                hideProgressDialog();
                if(insertCount > 0) updateLists();
              }
              else{
                hideProgressDialog();
                context.popBackStack();
              }
            }
            else{
              JSONArray jsonInvArray = extractJSONArray(jsonResponse, ParamConstants.ITEMS);
              showLog("sessionObject", "" + (sessionObject != null));
              if(jsonInvArray != null && jsonInvArray.length() > 0 && sessionObject != null){
                int insertCount = 0;
                for(int i = 0; i < jsonInvArray.length(); i++){
                  final JSONObject jsonObjInv = jsonInvArray.getJSONObject(i);
                  final Inventory inv = getGSON().fromJson(jsonObjInv.toString(), Inventory.class);
                  inv.sessionId = sessionObject.sessionId;
                  inv.sessionType = sessionObject.sessionType;
                  inv.sessionAction = sessionObject.sessionAction;
                  inv.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                  try{
                    inventoryDao.insertInventoryData(inv);
                    insertCount++;
                  }
                  catch(Exception e){ e.printStackTrace(); }
                }
                showLog("insertCount", "" + insertCount);
                hideProgressDialog();
                if(insertCount > 0) updateLists();
              }
              else hideProgressDialog();
            }
          }
          updateLists();
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}