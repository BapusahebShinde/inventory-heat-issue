package com.itek.retail.ui.inward.tote;

import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.DATE_TIME_FORMAT_PATTERN;
import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractInt;
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
import androidx.core.text.HtmlCompat;
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
import com.itek.retail.databinding.FragmentInwardToteStartBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.RFIDSession;
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
import java.util.stream.Collectors;

public class InwardToteStartFragment extends CommonInventoryFragment {
  private FragmentInwardToteStartBinding binding;

  private InwardToteViewModel mViewModel;
  private InventoryDao inventoryDao;
  private boolean isEmptyToteInward = false;
  private boolean isEmptyToteOutward = false;
  private String challanNo;
  private String totetype;
  private ArrayList<EanQty> listInvData = new ArrayList<>(0);
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    inventoryDao = AppDatabase.getInventoryDao(context);
  }
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(InwardToteViewModel.class);
    
    binding = FragmentInwardToteStartBinding.inflate(inflater, container, false);

    getActiveUsersAndSessionValidTill();
    if(isNonEmpty(getArguments())){
      isEmptyToteOutward = getArguments().getBoolean(ParamConstants.IS_EMPTY_TOTE_OUTWARD, false);
      isEmptyToteInward = getArguments().getBoolean(ParamConstants.IS_EMPTY_TOTE_INWARD, true);
      challanNo = extractString(getArguments(), ParamConstants.CHALLAN_NO, extractString(getArguments(), ParamConstants.DELIVERY_CHALLAN, SharedPrefManager.getString(ParamConstants.CHALLAN_NO)));
      eans.addAll(isNonEmpty(challanNo) ? SharedPrefManager.getStringArrayList(challanNo) : chkNull(getArguments().getStringArrayList(challanNo), new ArrayList<String>(0)));
      totetype = isEmptyToteOutward? "EMPTYTOTEOUTWARD" : "EMPTYTOTEINWARD";
      showLog("toteType", totetype);
      try{
        extras = new JSONObject();
        extras.put(ParamConstants.CHALLAN_NO, challanNo);
        extras.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
        extras.put(ParamConstants.IS_EMPTY_TOTE_INWARD, isEmptyToteInward);
        extras.put(ParamConstants.TOTE_TYPE, totetype);
      }
      catch(JSONException e){
        throw new RuntimeException(e);
      }
    }
    if(isNullOrEmpty(eans) || isNullOrEmpty(challanNo)){
      showShortToast(R.string.err_no_data);
      popBackStack();
    }

    setViews(binding.header,binding.llSeekbarPower,binding.llBtnStart,null);
    
   /* activeUsers = extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2);
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
    
    binding.llSeekbarPower.setupProgress(mainViewModel);*/
    
    binding.txtChallanNo.setText(HtmlCompat.fromHtml(String.format(getString(isEmptyToteOutward?R.string.txt_otw_ref_number:R.string.txt_delivery_challan), challanNo), HtmlCompat.FROM_HTML_MODE_COMPACT));
    
    binding.listEanQty.setAdapter(new OutwardToteTypewiseEanQtyAdapter(context, this, listInvData));
    binding.listEanQty.setLayoutManager(new LinearLayoutManager(context){
      @Override
      public boolean canScrollVertically(){
        return !isProcessOn();
      }
    });
    
   /* binding.llBtnStart.setOnClickListener(new View.OnClickListener(){
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
    });*/
    
    binding.btnComplete.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && allowBtnClick && !isProcessOn()){
          context.showCustomConfirmDialog(getString(R.string.msg_confirm_complete_challan), R.string.btn_complete, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              callUploadAPI(true);
            }
          });
        }
      }
    });
    
    binding.btnRescan.setTextColor(context.getColorPrimaryDarkFromTheme());
    binding.btnReject.setTextColor(context.getColorPrimaryDarkFromTheme());
    
    binding.btnRescan.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && allowBtnClick && !isProcessOn()){
          context.showCustomConfirmDialog(getString(R.string.msg_confirm_rescan_challan), R.string.btn_inward_alert_rescan, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              inventoryDao.deleteInventory(sessionObject.sessionId);
              updateView();
            }
          });
        }
      }
    });
    
    binding.btnReject.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && allowBtnClick && !isProcessOn()){
          context.showCustomConfirmDialog(getString(isEmptyToteOutward ? R.string.msg_confirm_reject_outward_ref:R.string.msg_confirm_reject_challan), R.string.btn_inward_alert_reject, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              mainViewModel.stopSession(sessionObject, true);
              SharedPrefManager.clearArrayList(challanNo);
              SharedPrefManager.getString(ParamConstants.CHALLAN_NO, "");
              context.popBackStack();
            }
          });
        }
      }
    });
    
    updateLists();
    
    //if(sessionObject == null) apiCall(AppConstants.SESSION_ACTION_START);
   /* new Handler().postDelayed(new Runnable(){
      @Override
      public void run() {
        binding.llBtnStart.performClick();
      }
    },50);*/
    
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
          //requestParams.put(ParamConstants.TYPE, type);
          requestParams.put(ParamConstants.ACTION, action);
          requestParams.put(ParamConstants.IS_EMPTY_TOTE_INWARD, isEmptyToteInward);
          requestParams.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
          requestParams.put(ParamConstants.TOTE_TYPE, totetype);
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
                      JSONObject dataobject = inventory.toShortJson(context);
                      if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                        js.put(dataobject);
                    }
                    requestParams.put(ParamConstants.CHALLAN_DETAILS, js);
                    callWebService(URLConstants.UPLOAD_CHALLAN_DETAILS, requestParams, getString(R.string.progress_msg_uploading_data), false);
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
        requestParams.put(ParamConstants.IS_EMPTY_TOTE_INWARD, isEmptyToteInward);
        requestParams.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
        requestParams.put(ParamConstants.TOTE_TYPE, totetype);
        //requestParams.put(ParamConstants.TYPE, type);
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, isAccepted ? AppConstants.STATUS_COMPLETE : AppConstants.K_HU_STATUS_REJECT);
        //requestParams.put(ParamConstants.STATUS, action.replaceFirst("(?i)" + AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst("(?i)" + AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst("(?i)" + AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        if(sessionObject != null && sessionObject.sessionId != null){
          requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
          JSONArray js = new JSONArray();
          List<Inventory> dataList = AppDatabase.getInventoryDao(context).getAllInventoryData(sessionObject.sessionId);
          if(isNonEmpty(dataList)) for(Inventory inventory : dataList){
            JSONObject dataobject = inventory.toShortJson(context);
            if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
              js.put(dataobject);
          }
          requestParams.put(ParamConstants.CHALLAN_DETAILS, js);
          if(isAccepted){
            callWebService(URLConstants.UPLOAD_CHALLAN_DETAILS, requestParams, getString(R.string.progress_msg_uploading_data));
          }
          //else if(!isAccepted) handleResponse(URLConstants.UPLOAD_OUTWARD_CARTON_DATA, requestParams,new JSONObject(),200,true,null);
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

  protected void onReaderConfigured(){
    super.onReaderConfigured();
  }
  
  @Override
  protected void isSessionOnChanged(final Boolean isInventorySessionOn){
    super.isSessionOnChanged(isInventorySessionOn);
//    if(isInventorySessionOn == null) return;
//    boolean isInvSessionOn = chkNotNullTrue(isInventorySessionOn);
//    binding.llBtnStart.toggle(isInvSessionOn);
//    final boolean isInvCount = getSize() > 0;//AppCommonMethods.parseInt(chkNull(binding.txtInventoryStartScoreCount.getText().toString(), "0")) > 0;
  }
  
  @Override
  protected void isInventoryOnChanged(Boolean isSessionOn){
    super.isInventoryOnChanged(isSessionOn);
    /*final Boolean isInventoryOn = mainViewModel.getIsInventoryOn().getValue();
    if(isInventoryOn == null) return;
    else{
      binding.llSeekbarPower.setEnabled(!isInventoryOn);
      binding.llSeekbarPower.setVisibility(!isInventoryOn && binding.llSeekbarPower.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
      binding.llBtnStart.toggle(isInventoryOn);*/
      updateView();
    //}
  }
  
  private void updateView(){
    final boolean isInvCount = getSize() > 0;
    binding.llButtons.setVisibility(!isProcessOn() && isInvCount ? View.VISIBLE : View.INVISIBLE);
  }
  
  @Override
  protected void onTriggerPressed(){
    super.onTriggerPressed();
    //binding.llBtnStart.performClick();
  }
  
  @Override
  protected void onDataSizeChanged(Integer size){
    AppCommonMethods.showLog("size", "" + size);
    super.onDataSizeChanged(size);
    /*if(chkNull(size, 0) > 0 && sessionObject != null && !chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))// && chkNotNullTrue(mainViewModel.getIsDeviceConfigured().getValue()) && chkNotNullFalse(mainViewModel.getIsInventorySessionOn().getValue()))
      mainViewModel.startSession(sessionObject, eans, false);
    
    if(chkNull(size, 0) >= AppCommonMethods.invLimit){
      if(sessionObject != null && chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))
        mainViewModel.stopInventory();
      if(chkNull(size, 0) == AppCommonMethods.invLimit)
        context.showCustomErrDialog(R.string.err_inventory_max_limit);
    }
    else */updateLists();
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    final int size = chkNull(getSize(), 0);
    if(binding != null && binding.txtTotal != null)
      binding.txtTotal.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_total), "" + size), HtmlCompat.FROM_HTML_MODE_COMPACT));
    listInvData.clear();
    listInvData.addAll(getUpdatedEanQtyList(inventoryDao.getEanCounts(sessionObject != null ? sessionObject.sessionId : "0")));
    if(binding != null && binding.listEanQty != null && binding.listEanQty.getAdapter() != null)
      binding.listEanQty.getAdapter().notifyDataSetChanged();
    updateView();
  }
  
  private ArrayList<EanQty> getUpdatedEanQtyList(List<EanQty> listEanQty){
    ArrayList<EanQty> updatedArrayList = new ArrayList<>(0);
    if(isNonEmpty(listEanQty)) updatedArrayList.addAll(listEanQty);
    ArrayList<String> scanEans = getEansFromEanQtyList(listEanQty);
    ArrayList<String> listDefaultEans = new ArrayList<>(eans);
    if(isNonEmpty(listDefaultEans) && isNonEmpty(scanEans)) listDefaultEans.removeAll(scanEans);
    if(isNonEmpty(listDefaultEans)){
      for(String defEan : listDefaultEans)
        updatedArrayList.add(new EanQty(defEan, 0));
    }
    return updatedArrayList;
  }
  
  private ArrayList<String> getEansFromEanQtyList(List<EanQty> listEanQty){
    ArrayList<String> scanEans = new ArrayList<>(0);
    if(isNullOrEmpty(listEanQty)) return scanEans;
    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
      scanEans = (ArrayList<String>) listEanQty.stream().map(e -> e.getEan()).distinct().collect(Collectors.toList());
      return scanEans;
    }
    else{
      for(EanQty eanQty : listEanQty)
        if(eanQty != null && isNonEmpty(eanQty.getEan())) scanEans.add(eanQty.getEan());
      return scanEans;
    }
  }
  
/*  @Override
  protected void onReaderPowerChanged(Integer power){
    binding.llSeekbarPower.updateReaderPower(mainViewModel, power);
  }*/
  
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
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.UPLOAD_CHALLAN_DETAILS:
          hideProgressDialog();
          if(isSuccess && jsonResponse != null && sessionObject != null){
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
              hideProgressDialog();
              context.showCustomErrDialog(errMsg);
            }
            else{
              context.showCustomMsgDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(isEmptyToteOutward?R.string.msg_otw_ref_number_upload_success:R.string.msg_delivery_challan_upload_success), challanNo)), null, true, true, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                  mainViewModel.stopSession(sessionObject, true);
                  SharedPrefManager.clearArrayList(chkNull(challanNo, ""));
                  SharedPrefManager.getString(ParamConstants.CHALLAN_NO, "");
                  popBackStack();
                }
              });
            }
          }
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}
