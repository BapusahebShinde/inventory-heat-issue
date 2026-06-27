package com.itek.retail.ui.outward.tote;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT_PATTERN;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itek.retail.R;
import com.itek.retail.adapter.OutwardToteTypewiseEanQtyAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonInventoryFragment;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.OutwardBatchDao;
import com.itek.retail.databinding.FragmentOutwardToteStartBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.OutwardTypes;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.TypeEans;
import com.itek.retail.ui.customviews.MaxHeightRecyclerView;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OutwardToteStartFragment extends CommonInventoryFragment {
  
  int activeUsers = 0, sessionValidTill = 48;
  private FragmentOutwardToteStartBinding binding;
  private String type;
  private String destCode;
  private String cartonNo;
  private TypeEans typeEans;
  private OutwardTypes outType;
  private OutwardToteViewModel mViewModel;
  private InventoryDao inventoryDao;
  private boolean isEmptyToteOutward = false;
  private boolean isOffRange = false;
  private OutwardBatchDao outwardBatchDao;
  private String batchId;
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    inventoryDao = AppDatabase.getInventoryDao(context);
    outwardBatchDao = AppDatabase.getOutwardBatchDao(context);
  }
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OutwardToteViewModel.class);
    
    binding = FragmentOutwardToteStartBinding.inflate(inflater, container, false);
    
    if(isNonEmpty(getArguments())){
      isEmptyToteOutward = getArguments().getBoolean(ParamConstants.IS_EMPTY_TOTE_OUTWARD, true);
      isOffRange = getArguments().containsKey(ParamConstants.IS_OFF_RANGE) && getArguments().getBoolean(ParamConstants.IS_OFF_RANGE, false);
      typeEans = (TypeEans) extractSerializable(getArguments(), TypeEans.class);
      outType = (OutwardTypes) extractSerializable(getArguments(), OutwardTypes.class);
      cartonNo = extractString(getArguments(), ParamConstants.CARTON_NO, extractString(getArguments(), ParamConstants.CARTON_NUM,extractString(getArguments(), ParamConstants.CARTON_NUMBER)));
      destCode = extractString(getArguments(), ParamConstants.DESTINATION_SITE_CODE, extractString(getArguments(), ParamConstants.DESTINATION_SITE, extractString(getArguments(), ParamConstants.DESTINATION_CODE)));
      batchId = extractString(getArguments(), ParamConstants.BATCH_ID, extractString(getArguments(), ParamConstants.BATCH_ID));
      type = typeEans != null && isNonEmpty(typeEans.getType()) ? typeEans.getType() : outType != null ? outType.getName() : extractString(getArguments(), ParamConstants.OUTWARD_TOTE_TYPE, extractString(getArguments(), ParamConstants.TYPE));
      eans.addAll(typeEans != null && isNonEmpty(typeEans.getEans()) ? typeEans.getEans() : chkNull(getArguments().getStringArrayList(ParamConstants.OUTWARD_TOTE_EANS),new ArrayList<String>(0)));
      try{
        extras = new JSONObject();
        extras.put(ParamConstants.DESTINATION_SITE_CODE, destCode);
        extras.put(ParamConstants.DESTINATION_CODE, destCode);
        extras.put(ParamConstants.CARTON_NUMBER, cartonNo);
        extras.put(ParamConstants.OUTWARD_TOTE_TYPE, type);
        extras.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
        extras.put(ParamConstants.IS_OFF_RANGE, isOffRange);
        extras.put(ParamConstants.BATCH_ID, batchId);
        if(outType != null)
          extras.put(ParamConstants.STORE_OUTWARD_TYPE_MASTER_ID, outType.getTypeId());
        //extras.put(ParamConstants.OUTWARD_TOTE_EANS,type);
      }
      catch(JSONException e){
        throw new RuntimeException(e);
      }
    }
    if(isNullOrEmpty(eans) || isNullOrEmpty(type)){
      showShortToast(R.string.err_no_data);
      popBackStack();
    }
    
    activeUsers = extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2);
    sessionValidTill = extractInt(getArguments(), AppConstants.SESSION_VALID_TILL, 48);
    
    //    binding.header.imgConfigSync.setVisibility(isDebugApp?View.VISIBLE:View.GONE);
    //    binding.header.imgConfigSync.setImageResource(R.drawable.ic_config);
    //    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
    //      @Override
    //      public void onClick(View view){
    //        if(chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue())) return;
    //        binding.llSeekbarPower.setVisibility(binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    //      }
    //    });
    
    setActiveUsers(activeUsers);
    
    //binding.llSeekbarPower.setupProgress(mainViewModel);
    
   // setTotalInvCount(getInvCount());

    setViews(binding.header,binding.llSeekbarPower,binding.llBtnStart,binding.ctwInventoryStart,binding.btnUpload);
    
    binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        context.dismissCustomAlertDialog();
        final Boolean isInventorySessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
        if(!isInventorySessionOn){
          if(sessionObject != null) mainViewModel.startSession(sessionObject, eans, true);
          else if(sessionObject == null){
            //Start Inventory Session
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
    
   /* binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
          return;
        showOutwardToteUploadSummary();
      }
    });
    
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          binding.btnSwipeUpload.reset();
          showOutwardToteUploadSummary();
        }
      }
    });*/
    
    //setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);
    
    return binding.getRoot();
  }
  
  @Override
  public void apiCall(String action){
    final Boolean isInventorySessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
    if(isInventorySessionOn != null){
      final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
      if(isUpload && isInternetConnected(context, false, isUpload)){
        try{
          if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          //Send Empty Array in JSON Request if value is 'All'
          JSONObject requestParams = new JSONObject();
          requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
          requestParams.put(ParamConstants.TYPE, type);
          requestParams.put(ParamConstants.ACTION, action);
          requestParams.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
          requestParams.put(ParamConstants.IS_OFF_RANGE, isOffRange);
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
                    callWebService(URLConstants.UPLOAD_OUTWARD_CARTON_DATA, requestParams, getString(R.string.progress_msg_uploading_data), false);
                  }
                  catch(Exception e){
                    e.printStackTrace();
                    hideProgressDialog();
                  }
                }
              });
            }
          }
        }
        catch(JSONException e){
          e.printStackTrace();
          if(!isUpload)
            setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
          else hideProgressDialog();
        }
      }
      else //if(!(isUpload || (sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START))))
        setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
    }
  }
  
  public void callUploadAPI(final boolean isAccepted){
    final String action = AppConstants.SESSION_ACTION_UPLOAD;
    if(!isAccepted || isInternetConnected(context, false, isAccepted)){
      try{
        showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        //Send Empty Array in JSON Request if value is 'All'
        JSONObject requestParams = extras != null ? extras : new JSONObject();
        requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        requestParams.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
        requestParams.put(ParamConstants.IS_OFF_RANGE, isOffRange);
        requestParams.put(ParamConstants.TYPE, type);
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, isAccepted ? AppConstants.K_HU_STATUS_ACCEPT : AppConstants.K_HU_STATUS_REJECT);
        //requestParams.put(ParamConstants.STATUS, action.replaceFirst("(?i)" + AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst("(?i)" + AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst("(?i)" + AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        if(sessionObject != null && sessionObject.sessionId != null){
          requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
          JSONArray js = new JSONArray();
          List<Inventory> dataList = AppDatabase.getInventoryDao(context).getAllInventoryData(sessionObject.sessionId);
          if(isNonEmpty(dataList)) for(Inventory inventory : dataList){
            JSONObject dataobject = inventory.toJson(context);
            if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
              js.put(dataobject);
          }
          requestParams.put(ParamConstants.ITEMS, js);
          if(isAccepted) callWebService(URLConstants.UPLOAD_OUTWARD_CARTON_DATA, requestParams, getString(R.string.progress_msg_uploading_data));
          else if(!isAccepted) handleResponse(URLConstants.UPLOAD_OUTWARD_CARTON_DATA, requestParams,new JSONObject(),200,true,null);
        }
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
      }
    }
  }
  
  /**
   * Set active users.
   */
  public void setActiveUsers(final int activeUsers){
    int oldActiveUsers = -2;
    try{
      oldActiveUsers = AppCommonMethods.parseInt(binding.header.btnActiveDevices.getText().toString(), "-2");
    }
    catch(NumberFormatException e){ e.printStackTrace(); }
    final int activeCount = chkNull(activeUsers, -1) >= 0 ? activeUsers : oldActiveUsers;
    binding.header.flActiveDevices.setVisibility(activeUsers >= -1 ? View.VISIBLE : View.GONE);
    binding.header.btnActiveDevices.setSelected(true);
    binding.header.btnActiveDevices.setText(activeCount >= 0 ? "" + activeCount : "");
  }
  
  /**
   * Show outward tote upload summary.
   */
  private void showOutwardToteUploadSummary(){
    List<EanQty> listToteEans = inventoryDao.getEanCounts(sessionObject.sessionId);
    MaxHeightRecyclerView listDisplayData = isNonEmpty(listToteEans) ? new MaxHeightRecyclerView(context) : null;
    if(listDisplayData != null){
      int margin = getResources().getDimensionPixelSize(R.dimen.dp_15);
      LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      llParams.setMargins(margin, 0, margin, 0);
      listDisplayData.setLayoutParams(llParams);
      listDisplayData.setPadding(margin, 0, margin, 0);
      listDisplayData.setAdapter(new OutwardToteTypewiseEanQtyAdapter((MainActivity) context, this, listToteEans));
      listDisplayData.setLayoutManager(new LinearLayoutManager(context));
    }
    context.showCustomAlertDialog(getString(R.string.title_summary), "", listDisplayData, R.string.btn_inward_alert_accept, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        context.showCustomConfirmDialog(getString(R.string.msg_confirm_accept), R.string.btn_inward_alert_accept, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            
            callUploadAPI(true);
          }
        });
      }
    }, R.string.btn_inward_alert_reject, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        context.showCustomConfirmDialog(getString(R.string.msg_confirm_reject), R.string.btn_inward_alert_reject, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            
            callUploadAPI(false);
          }
        });
      }
    }, R.string.btn_inward_alert_rescan, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialog, int which){
        context.showCustomConfirmDialog(getString(R.string.msg_confirm_rescan), R.string.btn_inward_alert_rescan, new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            inventoryDao.deleteInventory(sessionObject.sessionId);
          }
        });
      }
    });
  }

  @Override
  protected void onBtnUploadSwiped() {
    showOutwardToteUploadSummary();
  }

  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.UPLOAD_OUTWARD_CARTON_DATA:
          hideProgressDialog();
          if(isSuccess && jsonResponse != null && sessionObject != null){
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
              hideProgressDialog();
              context.showCustomErrDialog(errMsg);
            }
            else{
              final String status = extractString(jsonRequest, ParamConstants.STATUS, "");
              final boolean isAccepted = status.equalsIgnoreCase(AppConstants.K_HU_STATUS_ACCEPT);
              if(isAccepted) {
                outwardBatchDao.updateAcceptedQty(batchId);
                ArrayList<String> listCompletedCartons = SharedPrefManager.getStringArrayList(batchId+ParamConstants.COMPLETED_CARTONS, new ArrayList<>(0));
                if(!listCompletedCartons.contains(cartonNo)){
                  listCompletedCartons.add(cartonNo);
                  SharedPrefManager.setStringArrayList(batchId + ParamConstants.COMPLETED_CARTONS, listCompletedCartons);
                }
              }
              else outwardBatchDao.updateRejectedQty(batchId);
              //remove observers
              removeObservers();
              mainViewModel.stopSession(sessionObject, true);
             // binding.txtInventoryStartScoreCount.setText("" + 0);
              if (binding.ctwInventoryStart != null) binding.ctwInventoryStart.setScore(0);
              binding.btnUpload.setVisibility(View.GONE);
              //binding.btnSwipeUpload.setVisibility(View.GONE);
              final String statusMsg=extractString(jsonResponse, ParamConstants.MESSAGE,(!status.endsWith("ed")?status+"ed":status)+"!");
              if(isNonEmpty(statusMsg)){
                if(!isAccepted) context.showCustomErrDialog(statusMsg, true);
                else if(isAccepted) context.showCustomSuccessDialog(statusMsg, true);
              }
              else context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), true);
            }
          }
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}
