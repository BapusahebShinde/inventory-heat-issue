package com.itek.retail.ui.movement;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isSinglePick;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.MovementScanAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ZoneDao;
import com.itek.retail.databinding.FragmentMovementStartBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The Movement start fragment.
 */
public class MovementStartFragment extends RFIDSessionFragment{
  
  public FragmentMovementStartBinding binding;
  ZoneDao zoneDao;
  InventoryDao inventoryDao;
  private List<String> listSrcLocations = new ArrayList<>(0);
  private List<String> listDestLocations = new ArrayList<>(0);
  private List<EanQty> listPickedEans = new ArrayList<>(0);
  private MovementStartViewModel mViewModel;
  private ProductModel model;
  private boolean isConfirmedByUser=false;
  
  /**
   * Instantiates a new Movement start fragment.
   */
  public MovementStartFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    zoneDao = AppDatabase.getZoneDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(MovementStartViewModel.class);
    binding = FragmentMovementStartBinding.inflate(inflater, container, false);
    
    binding.header.imgConfigSync.setVisibility(View.VISIBLE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_config);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(chkNotNullTrue(mainViewModel.getIsPickOn().getValue())) return;
        binding.llSeekbarPower.setVisibility(binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
      }
    });
    
    final Object obj = extractSerializable(getArguments(), ProductModel.class);
    model = obj != null && obj instanceof ProductModel ? (ProductModel) obj : null;
    listSrcLocations = zoneDao.getAllZones();
    listDestLocations = zoneDao.getAllZones();
    
    binding.spinSourceZone.setAdapter(listSrcLocations,1);
    binding.spinDestinationZone.setAdapter(listDestLocations,1);
    
    if(model != null && isNonEmpty(model.getEan())) binding.txtMovementEan.setText(model.getEan());
    binding.txtMovementEan.setIsViewControlEnabled(false);
    binding.txtMovementEan.setVisibility(model != null && isNonEmpty(model.getEan()) ? View.VISIBLE : View.GONE);
    
    listPickedEans.clear();
    if(sessionObject != null)
      listPickedEans.addAll(inventoryDao.getEanCounts(sessionObject.sessionId));
    
    binding.listMovementEan.setAdapter(new MovementScanAdapter((MainActivity) context, this, listPickedEans));
    binding.listMovementEan.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
    
    binding.spinSourceZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        final String source = binding.spinSourceZone.getSelectedItem().toString();
        listDestLocations.clear();
        listDestLocations.addAll(zoneDao.getAllZonesExcept(source));
        
        /*if(isNonEmpty(listDestLocations))
          listDestLocations.add(0, String.format(getString(R.string.header_select), binding.spinDestinationZone.getLabel()));*/
        binding.spinDestinationZone.setAdapter(listDestLocations,1);
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){/*Don't handle (Default Overridden Empty Method)*/}
    });
    
    if(model != null && isNonEmpty(model.zone)) binding.spinSourceZone.setSelection(model.zone);
    binding.spinSourceZone.setEnabled(model == null || isNullOrEmpty(model.zone) || !listSrcLocations.contains(model.zone));
    
    binding.llSeekbarPower.setupProgress(mainViewModel);
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @SuppressLint("StringFormatInvalid")
      @Override
      public void onClick(View view){
        context.dismissCustomAlertDialog();
        final Boolean isSessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
        if(!isSessionOn){
          if(sessionObject != null) mainViewModel.startSession(sessionObject, true);
          else if(sessionObject == null && binding.spinSourceZone.validate() && binding.spinDestinationZone.validate()){
            apiCall(AppConstants.SESSION_ACTION_START);
          }
        }
        else if(sessionObject != null && isSessionOn){
          if(chkNotNullTrue(mainViewModel.getIsPickOn().getValue())) mainViewModel.stopInventory();
          else{
            context.dismissCustomAlertDialog();
            int size = getSize();
            if(checkReaderConnected()){
              if(size > 0 && !isSinglePick){
                context.showCustomAlertDialog(getString(R.string.title_confirm_action), String.format(getString(R.string.msg_clear_and_start), "" + size), getString(R.string.btn_yes), new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i){
                    inventoryDao.deleteInventory(sessionObject.sessionId);
                    mainViewModel.performPick(model != null ? model.getEan() : "");
                  }
                }, getString(R.string.btn_no), new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i){
                    mainViewModel.performPick(model != null ? model.getEan() : "");
                  }
                });
              }
              else mainViewModel.performPick(model != null ? model.getEan() : "");
            }
          }
        }
      }
    });
    
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          if(SharedPrefManager.getBoolean(ParamConstants.IS_ACTUAL_TAG_DISPLAY_MAPPING)){
            //TODO allow mapping via Dialog List -> 1 by 1 scan
          }
          else{
            apiCall(AppConstants.SESSION_ACTION_UPLOAD);
            binding.btnSwipeUpload.performSuccessfulSwipe();
          }
        }
      }
    });
    
    return binding.getRoot();
  }
  
  @Override
  protected void onReaderPowerChanged(Integer power){
    binding.llSeekbarPower.updateReaderPower(mainViewModel, power);
  }
  
  @Override
  protected void onReaderConfigured(){
    super.onReaderConfigured();
  }
  
  @Override
  public void apiCall(String action){
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    final Zone srcZone = zoneDao.getZoneByName(sessionObject != null ? sessionObject.zone : binding.spinSourceZone.getSelectedItem().toString()).get(0);
    final Zone destZone = zoneDao.getZoneByName(sessionObject != null ? sessionObject.destZone : binding.spinDestinationZone.getSelectedItem().toString()).get(0);
    if(isInternetConnected(context, false, isUpload)){
      try{
        allowBtnClick = false;
        if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.REPLENISHMENT_TYPE, "");
        requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        requestParams.put(ParamConstants.TYPE, AppConstants.MOVE_STOCK);
        requestParams.put(ParamConstants.SOURCE_ZONE, srcZone.toJson());
        requestParams.put(ParamConstants.DESTINATION_ZONE, destZone.toJson());
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        if(sessionObject != null)
          requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
        if(isUpload){
          new Handler().post(new Runnable(){
            @Override
            public void run(){
              try{
                JSONArray js = new JSONArray();
                for(Inventory inventory : inventoryDao.getAllInventoryData(sessionObject.sessionId)){
                  if(inventory != null){
                    JSONObject dataobject = inventory.toJson(context);
                    if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                      js.put(dataobject);
                  }
                }
                requestParams.put(ParamConstants.ITEMS, js);
                allowBtnClick = true;
                callWebService(URLConstants.UPLOAD_MOVEMENT, requestParams, isUpload, getString(R.string.progress_msg_uploading_data), !isUpload);
              }
              catch(Exception e){
                e.printStackTrace();
                hideProgressDialog();
                allowBtnClick = true;
              }
            }
          });
          JSONArray js = new JSONArray();
          for(Inventory inventory : inventoryDao.getAllInventoryData(sessionObject.sessionId)){
            if(inventory != null){
              JSONObject dataobject = inventory.toJson(context);
              
              if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                js.put(dataobject);
            }
          }
          requestParams.put(ParamConstants.ITEMS, js);
          //add Other Parameters for bulk uploading
        }
        else{
          allowBtnClick = true;
          callWebService(isUpload ? URLConstants.UPLOAD_MOVEMENT : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload);
        }
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
        allowBtnClick = true;
      }
    }
  }
  
  @Override
  public void setSessionObject(RFIDSession sessionObject){
    super.setSessionObject(sessionObject);
    if(sessionObject != null && sessionObject instanceof RFIDSession)
      this.sessionObject = (RFIDSession) sessionObject;
    showLog("movementSession", "" + (this.sessionObject != null));
  }
  
  @Override
  protected void isSessionOnChanged(final Boolean isSessionOn){
    super.isSessionOnChanged(isSessionOn);
    if(isSessionOn == null) return;
    boolean isInvSessionOn = chkNotNullTrue(isSessionOn);
    binding.spinSourceZone.setEnabled((model == null || isNullOrEmpty(model.zone) || !listSrcLocations.contains(model.zone)) && !isInvSessionOn);
    binding.spinDestinationZone.setEnabled(!isInvSessionOn);
  }
  
  @Override
  protected void isPickOnChanged(Boolean isPickOn){
    super.isPickOnChanged(isPickOn);
    
    if(isPickOn == null) return;
    else{
      binding.btnSwipeUpload.setVisibility(getSize() > 0 && !isPickOn ? View.VISIBLE : View.GONE);
      binding.llBtnStart.toggle(isPickOn);
      binding.llSeekbarPower.setEnabled(!isPickOn);
      binding.llSeekbarPower.setVisibility(!isPickOn && binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
    }
  }
  
  @Override
  protected void onTriggerPressed(){
    showLog(this.getClass().getSimpleName(), "onTriggerPressed");
    binding.llBtnStart.performClick();
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    listPickedEans.clear();
    if(sessionObject != null)
      listPickedEans.addAll(inventoryDao.getEanCounts(sessionObject.sessionId));
    if(binding.listMovementEan != null && binding.listMovementEan.getAdapter() != null)
      ((RecyclerView.Adapter) binding.listMovementEan.getAdapter()).notifyDataSetChanged();
    binding.txtMovementTotal.setText(String.format(getString(R.string.txt_movement_replenishment_total), "" + chkNull(size, 0)));
    binding.btnSwipeUpload.setVisibility(size > 0 && chkNotNullFalse(mainViewModel.getIsPickOn().getValue()) ? View.VISIBLE : View.GONE);
  }
  
  @Override
  public void onPause(){
    super.onPause();
  }
  
  @Override
  public void onResume(){
    super.onResume();
  }

  @Override
  public void updateLists(){
    listPickedEans.clear();
    if(sessionObject != null)
      listPickedEans.addAll(inventoryDao.getEanCounts(sessionObject.sessionId));

    binding.listMovementEan.getAdapter().notifyDataSetChanged();
  }

  public void deleteSelectedEan(final EanQty eanQty){
     if (eanQty == null || isNullOrEmpty(eanQty.getEan())) return;
     inventoryDao.deleteInventory(sessionObject.sessionId, eanQty.getEan(),null);
     updateLists();
  }
  
  @Override
  public void onDestroyView(){
    sessionObject = null;
    super.onDestroyView();
  }
  
  /**
   * Set session action.
   *
   * @param action the action
   */
  public void setSessionAction(String action){
    if(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      RFIDSession sessionObject = new RFIDSession();
      sessionObject.eans = model != null ? model.getEan() : "";
      sessionObject.zone = binding.spinSourceZone.getSelectedItem();
      final Object selZoneObj = binding.spinSourceZone.getSelectedObject();
      sessionObject.zoneId = selZoneObj != null && selZoneObj instanceof Zone ? chkNull(((Zone) selZoneObj).zoneId, "0") : "0";
      sessionObject.destZone = binding.spinDestinationZone.getSelectedItem();
      final Object selDestZoneObj = binding.spinSourceZone.getSelectedObject();
      sessionObject.destZoneId = selDestZoneObj != null && selDestZoneObj instanceof Zone ? chkNull(((Zone) selDestZoneObj).zoneId, "0") : "0";
      sessionObject.sessionType = AppCommonMethods.SessionType.MOVEMENT.getValue();
      sessionObject.sessionAction = AppCommonMethods.SessionAction.PICK.getValue();
      sessionObject.userId = SharedPrefManager.getUserID();
      Calendar cc = Calendar.getInstance();
      sessionObject.sessionStartTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      cc.add(Calendar.HOUR_OF_DAY, 24);
      sessionObject.sessionValidTill = new SimpleDateFormat(DATE_TIME_FORMAT).format(cc.getTime());
      sessionObject.sessionId = mainViewModel.generateOfflineSessionId(AppCommonMethods.SessionType.get(sessionObject.sessionType), cc);
      setSessionObject(sessionObject);
      mainViewModel.startSession(sessionObject, true);
    }
    else if(sessionObject != null && !action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)){
      //TODO
    }
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.SET_SESSION:
          setSessionAction(AppConstants.SESSION_ACTION_START);
          break;
        case URLConstants.UPLOAD_MOVEMENT:
          binding.btnSwipeUpload.showResultIcon(isSuccess);
          if(isSuccess){
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), true);
            //delete Uploaded Records
            mainViewModel.stopSession(sessionObject, true);
            inventoryDao.deleteInventory(sessionObject.sessionId);
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}