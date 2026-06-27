package com.itek.retail.ui.search.filesearch;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.itek.retail.databinding.DialogOmniEpcSearchBinding;
import com.itek.retail.databinding.FragmentSearchFileBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.ui.customviews.DashboardDataView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * The Unencoded search fragment.
 */
public class SearchFileBasedFragment extends RFIDSessionFragment{
  
  public FragmentSearchFileBinding binding;
  public ActivityResultLauncher<Intent> readFileActivityResultLauncher;
  InventoryDao inventoryDao;
  private List<Inventory> listTags = new ArrayList<>(0);
  private SearchFileBasedViewModel mViewModel;
  private DialogOmniEpcSearchBinding dialogOmniEpcSearchBinding;
  private boolean showMarkFoundBtn = false;
  
  /**
   * Instantiates a new Unencoded search fragment.
   */
  public SearchFileBasedFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    inventoryDao = AppDatabase.getInventoryDao(context);
    readFileActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
      if(result.getResultCode() == Activity.RESULT_OK){
        // There are no request codes
        Intent data = result.getData();
        final Uri documentUri = data.getData();
        if(documentUri != null){
          doFileValidationsAndTakeAction(context, documentUri);
        }
        else{
          /**
           * Invalid File
           */
          showLog("HOME", "INVALID");
          context.showCustomErrDialog(getString(R.string.err_invalid_file));
        }
      }
    });
    if(isAllowDirectionalSearch) mainViewModel.getSensorAndStart();
  }
  
  private void openIntent(){
    try{
      Intent intent = new Intent();
      intent.setType("*/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      readFileActivityResultLauncher.launch(intent);
      //readFileActivityResultLauncher.launch(Intent.createChooser(intent, "Select a file"));
    }catch(Exception e){e.printStackTrace();}
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(SearchFileBasedViewModel.class);
    binding = FragmentSearchFileBinding.inflate(inflater, container, false);
    
    binding.listSearchUnencoded.setAdapter(new OmniPickedListAdapter(context, SearchFileBasedFragment.this, listTags));
    binding.listSearchUnencoded.setLayoutManager(new LinearLayoutManager(context));
    
    //binding.scanSelectFile.setVisibility(isNullOrEmpty(listTags)?View.VISIBLE:View.GONE);
    binding.btnUploadFile.setVisibility(isNullOrEmpty(listTags) ? View.VISIBLE : View.GONE);
    
    binding.llUnencodedSearchFoundTotal.setVisibility(isNonEmpty(listTags) ? View.VISIBLE : View.GONE);
    binding.llUnencodedSearchFoundTotal.setVisibility(isNonEmpty(listTags) ? View.VISIBLE : View.GONE);
    
    //    binding.scanSelectFile.setImgScanOnClickListener(new View.OnClickListener(){
    //      @Override
    //      public void onClick(View v){
    //        //TODO get file
    //          openIntent();
    //      }
    //    });
    
    binding.btnUploadFile.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        openIntent();
        //handleResponse(URLConstants.GET_UNENCODED_SEARCH_LIST,null,getSampleJSON(context,"getFileSearchList"),200,true,null);
      }
    });
    
    binding.header.imgConfigSync.setVisibility(isOfflineInventory() && !isProcessOn() ? View.VISIBLE : View.GONE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_config);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(view != null && view.getVisibility() == View.VISIBLE){
          binding.llSeekbarPower.setVisibility(!isProcessOn() && binding.llSeekbarPower.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
        }
      }
    });
    
    binding.llSeekbarPower.setupProgress(mainViewModel);
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && isOfflineInventory()){
          context.dismissCustomAlertDialog();
          final Boolean isSessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
          if(!isSessionOn){
            if(sessionObject != null) mainViewModel.startSession(sessionObject, true);
            else if(sessionObject == null){
              setSessionAction(AppConstants.SESSION_ACTION_START);
            }
          }
          else if(isSessionOn){
            if(getSize() >= AppCommonMethods.invLimit){
              if(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
                mainViewModel.stopInventory();
              context.showCustomErrDialog(R.string.err_inventory_max_limit);
            }
            else toggleInventory();
          }
        }
      }
    });
    
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
        jsonExtras1.put(ParamConstants.EAN, chkNull(inventory.newEpc,inventory.epc));
        jsonExtras1.put(ParamConstants.EAN_QTY, 1);
        jsonExtras1.put(ParamConstants.IS_EAN_SEARCH, false);
        jsonExtras1.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        jsonExtras1.put(ParamConstants.TYPE, getSessionType().name());
      }
      catch(Exception e){ e.printStackTrace(); }
      final JSONObject jsonExtras = jsonExtras1;
      DialogOmniEpcSearchBinding binding = DialogOmniEpcSearchBinding.inflate(LayoutInflater.from(context), null, false);
      LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(wid, wid);
      binding.rgSearchType.setVisibility(isNonEmpty(inventory.epc) && isNonEmpty(inventory.getTid()) ? View.VISIBLE : View.GONE);
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
              inventory.encVerifyStatus = AppCommonMethods.EncodeVerifyStatus.VERIFIED_SUCCESS.ordinal();
              inventoryDao.updateInventoryData(inventory);
              binding.btnDecode.setTag(inventory);
              showMarkFoundBtn = false;
              binding.btnDecode.setVisibility(showMarkFoundBtn && !isProcessOn() && !inventory.isFound ? View.VISIBLE : View.GONE);
              updateLists();
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
            if(isNonEmpty(inventory.tid) && (isNullOrEmpty(inventory.epc) || (binding.rgSearchType.getVisibility() == View.VISIBLE && binding.rgSearchType.findViewById(binding.rgSearchType.getCheckedRadioButtonId()).getTag().equals(getString(R.string.search_type_tid))))) {
              if(jsonExtras!=null) {
                try {
                  jsonExtras.put(ParamConstants.EAN, inventory.tid);
                } catch (JSONException e) {
                  throw new RuntimeException(e);
                }
              }
              searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", inventory.tid, 1, getSessionType().name(), "", jsonExtras);
              mainViewModel.performTIDBasedSearch(inventory.tid);
            }else{
              if(jsonExtras!=null) {
                try {
                  jsonExtras.put(ParamConstants.EAN, chkNull(inventory.newEpc, inventory.epc));
                } catch (JSONException e) {
                  throw new RuntimeException(e);
                }
              }
              searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(inventory.newEpc,inventory.epc), 1, getSessionType().name(), "", jsonExtras);
              mainViewModel.performEPCBasedSearch(chkNull(inventory.newEpc, inventory.epc));
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
          /*if(searchLog == null){
            JSONObject jsonExtras = null;
            try{
              jsonExtras = new JSONObject();
              jsonExtras.put(ParamConstants.EAN, chkNull(inventory.newEpc,inventory.epc));
              jsonExtras.put(ParamConstants.EAN_QTY, 1);
              jsonExtras.put(ParamConstants.IS_EAN_SEARCH, false);
              jsonExtras.put(ParamConstants.SESSION_TYPE, getSessionType().name());
              jsonExtras.put(ParamConstants.TYPE, getSessionType().name());
            }
            catch(Exception e){ e.printStackTrace(); }
            searchLog = insertSearchLog(sessionObject != null ? sessionObject.sessionId : "", chkNull(inventory.newEpc,inventory.epc), 1, getSessionType().name(), "", jsonExtras);
          }*/
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
  
  private boolean isOfflineInventory(){
    return AppCommonMethods.isListInventoryAllowed && isNonEmpty(listTags);
  }
  
  @Override
  protected void onTriggerPressed(){
    if(dialogOmniEpcSearchBinding != null && dialogOmniEpcSearchBinding.llBtnStart != null)
      dialogOmniEpcSearchBinding.llBtnStart.performClick();
    else binding.llBtnStart.performClick();
  }
  
  /**
   * Set session action.
   *
   * @param action the action
   */
  public void setSessionAction(String action){
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.sessionType = AppCommonMethods.SessionType.SEARCH_FILE.getValue();
      sessionObject.sessionAction = /*isOfflineInventory() ?*/ AppCommonMethods.SessionAction.SEARCH.getValue() /*: AppCommonMethods.SessionAction.SEARCH.getValue()*/;
      sessionObject.userId = SharedPrefManager.getUserID();
      Calendar cc = Calendar.getInstance();
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, 24);
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc);
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, false);
    }
    else if(sessionObject != null && !action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      //TODO
    }
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
    if(!showMarkFoundBtn && result >= AppCommonMethods.markFoundPercentUnencodedSearch && inventory != null && !inventory.isFound)
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
      dialogOmniEpcSearchBinding.rgSearchType.setEnabled(!isProcessOn);
      dialogOmniEpcSearchBinding.rbEpc.setEnabled(!isProcessOn);
      dialogOmniEpcSearchBinding.rbTid.setEnabled(!isProcessOn);
    }
    binding.llBtnStart.setEnabled(isOfflineInventory() && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()));
    binding.header.imgConfigSync.setVisibility(isOfflineInventory() && !isProcessOn() ? View.VISIBLE : View.GONE);
    //binding.scanSelectFile.setVisibility(isNullOrEmpty(listTags)?View.VISIBLE:View.GONE);
    binding.btnUploadFile.setVisibility(isNullOrEmpty(listTags) ? View.VISIBLE : View.GONE);
    binding.llUnencodedSearchFoundTotal.setVisibility(isOfflineInventory() ? View.VISIBLE : View.GONE);
    binding.listSearchUnencoded.setVisibility(isOfflineInventory() ? View.VISIBLE : View.GONE);
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    listTags.clear();
    if(sessionObject != null){
      AppCommonMethods.showLog("sessionId", sessionObject.sessionId);
      AppCommonMethods.showLog("sessionType", AppCommonMethods.SessionType.get(sessionObject.sessionType).name());
      listTags.addAll(inventoryDao.getEncVerifyListReverse(sessionObject.sessionId));
      setFoundTotalText(binding.ddvUnencodedSearchTotalFound, inventoryDao.getZonewiseFound(sessionObject.sessionId, null), listTags.size());
    }
    ((RecyclerView.Adapter) binding.listSearchUnencoded.getAdapter()).notifyDataSetChanged();
    binding.llBtnStart.setVisibility(isOfflineInventory() && isNonEmpty(listTags) && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()) ? View.VISIBLE : View.GONE);
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
  
  public String getFileNameFromURI(Context context, Uri uri){
    try{
      Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
      assert returnCursor != null;
      int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
      returnCursor.moveToFirst();
      String name = returnCursor.getString(nameIndex);
      returnCursor.close();
      return name;
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return null;
  }
  
  public void doFileValidationsAndTakeAction(CommonActivity context, Uri documentUri){
    
    String fileName = getFileNameFromURI(context, documentUri);
    showLog("fileName", fileName);
    String fileContent = "";
    
    if(fileName != null && fileName.endsWith(".json") || fileName.endsWith(".JSON")){
      context.showProgressDialog(context.getString(R.string.progress_msg_check_data));
      try{
        InputStream fileInputStream = context.getContentResolver().openInputStream(documentUri);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line = reader.readLine();
        while(line != null){
          lines.add(line);
          line = reader.readLine();
        }
        fileContent = TextUtils.join("\n", lines);
        
      }
      catch(Exception e){
        e.printStackTrace();
        context.hideProgressDialog();
        context.showCustomErrDialog(context.getString(R.string.err_invalid_file));
      }
      if(isNonEmpty(fileContent)){
        showLog("fileContent", fileContent);
        try{
          JSONObject jsonObject = new JSONObject(fileContent);
          //TODO check json format
          //context.fillProductMaster(jsonObject);
          handleResponse(URLConstants.GET_UNENCODED_SEARCH_LIST, null, jsonObject, 200, true, null);
        }
        catch(Exception e){
          e.printStackTrace();
          try{
            JSONArray jsonArray = new JSONArray(fileContent);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ParamConstants.ITEMS, jsonArray);
            //TODO check json format
            //context.fillProductMaster(jsonObject);
            handleResponse(URLConstants.GET_UNENCODED_SEARCH_LIST, null, jsonObject, 200, true, null);
          }
          catch(Exception ex){
            ex.printStackTrace();
          }
        }
      }
      else{
        context.hideProgressDialog();
        context.showCustomErrDialog(context.getString(R.string.err_invalid_file));
      }
    }
    else{
      context.showCustomErrDialog(context.getString(R.string.err_invalid_file));
    }
    
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
              inventoryDao.deleteInventory(AppCommonMethods.SessionType.SEARCH_FILE.getValue());
              setSessionAction(AppConstants.SESSION_ACTION_START);
            }
            final String sessionId = "";//extractString(args, ParamConstants.SESSION_ID, "");
            if(false && isNonEmpty(sessionId)){
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
                  serInv.encVerifyStatus = inv.getEncVerifyStatus();
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
              JSONArray jsonInvArray = extractJSONArray(jsonResponse,ParamConstants.ITEMS,extractJSONArray(jsonResponse,ParamConstants.SHEET1));
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
                /*if(insertCount > 0)*/ updateLists();
              }
              else hideProgressDialog();
            }
          }
          else updateLists();
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}