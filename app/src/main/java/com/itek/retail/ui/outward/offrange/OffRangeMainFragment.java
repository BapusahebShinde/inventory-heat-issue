package com.itek.retail.ui.outward.offrange;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractLong;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUploadSlider;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.InsertDBOutwardToteTypeEANs;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.OutwardBatchDao;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.databinding.FragmentOutwardToteMainBinding;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.OutwardBatch;
import com.itek.retail.model.OutwardTypes;
import com.itek.retail.model.TypeEans;
import com.itek.retail.model.UploadInventory;
import com.itek.retail.ui.customviews.swipeButton.ProSwipeButtonVar;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.outward.tote.OutwardToteScanCartonFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OffRangeMainFragment extends CommonFragment{
  
  private FragmentOutwardToteMainBinding binding;
  private boolean isEmptyToteOutward = false;
  private boolean isOffRange = false;
  private ArrayList<TypeEans> listOutwardToteTypes = new ArrayList<>();
  private ArrayList<String> listEans = new ArrayList<>();
  private ArrayList<OutwardTypes> listOutwardTypes = new ArrayList<>();
  private OffRangeViewModel mViewModel;
  private String destSiteCode = "";
  private OutwardTypes selOutwardType = null;
  private String listRefBatchId="";
  private Long listExpQty=0l;
  private String batchId = "";
  private OutwardBatchDao outwardBatchDao;
  //private OutwardBatch outwardBatch;
  
  /**
   * Instantiates a new Outward Tote Main Fragment.
   */
  public OffRangeMainFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    outwardBatchDao = AppDatabase.getOutwardBatchDao(context);
  }
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OffRangeViewModel.class);
    binding = FragmentOutwardToteMainBinding.inflate(inflater, container, false);
    
    isEmptyToteOutward = getArguments() != null && getArguments().containsKey(ParamConstants.IS_EMPTY_TOTE_OUTWARD) && getArguments().getBoolean(ParamConstants.IS_EMPTY_TOTE_OUTWARD, false);
    isOffRange = !isEmptyToteOutward && getArguments() != null && getArguments().containsKey(ParamConstants.IS_OFF_RANGE) && getArguments().getBoolean(ParamConstants.IS_OFF_RANGE, false);
    batchId = extractString(getArguments(), ParamConstants.BATCH_ID, extractString(getArguments(), ParamConstants.BATCH));
    listRefBatchId = extractString(getArguments(), ParamConstants.LIST_REF_BATCH_ID, extractString(getArguments(), ParamConstants.LIST_BATCH_ID,extractString(getArguments(), ParamConstants.LIST_ID)));
    listExpQty = extractLong(getArguments(), ParamConstants.EXPECTED_QTY, extractLong(getArguments(), ParamConstants.EXP_QTY,0l));
    
    showLog("isEmptyToteOutward", "" + isEmptyToteOutward);
    showLog("isOffRange", "" + isOffRange);
    
    binding.swipeLayout.setColorSchemeColors(context.getColorPrimaryDarkFromTheme());
    binding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
      @Override
      public void onRefresh(){
        binding.swipeLayout.setRefreshing(false);
        callAPI();
      }
    });
    
    binding.btnProceed.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && allowBtnClick && binding.ivDestCode.validate() && binding.spinOutwardToteType.validate() /*&& binding.scanCarton.validate()*/){
          OutwardTypes selOutType = (OutwardTypes) binding.spinOutwardToteType.getSelectedObject();
          //TypeEans selType = isEmptyToteOutward ? (TypeEans) binding.spinOutwardToteType.getSelectedObject() : null;
          destSiteCode = binding.ivDestCode.getText().toString().toUpperCase().trim();
          Bundle args = chkNull(getArguments(), new Bundle());
          args.putString(ParamConstants.DESTINATION_SITE_CODE, destSiteCode);
          if(selOutType != null){
            selOutwardType=selOutType;
            args.putSerializable(selOutType.getClass().getSimpleName(), selOutType);
            if(isEmptyToteOutward && isNonEmpty(listEans)){
              args.putStringArrayList(ParamConstants.OUTWARD_TOTE_EANS, listEans);
              SharedPrefManager.setStringArrayList(ParamConstants.OUTWARD_TOTE_EANS, listEans);
            }
          }
          batchId="";
          if(isNullOrEmpty(batchId) && selOutType != null)
            batchId = outwardBatchDao.getOutwardBatchIdFromTypeIdAndDestSiteCode(selOutType.getTypeId(), selOutType.getName(), destSiteCode);
          if(isNullOrEmpty(batchId)){
            try{
              JSONObject requestParams = new JSONObject();
              requestParams.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
              requestParams.put(ParamConstants.IS_OFF_RANGE, isOffRange);
              requestParams.put(ParamConstants.LIST_REF_BATCH_ID, listRefBatchId);
              requestParams.put(ParamConstants.DESTINATION_CODE, destSiteCode);
              requestParams.put(ParamConstants.DESTINATION_SITE_CODE, destSiteCode);
              requestParams.put(ParamConstants.STORE_OUTWARD_TYPE_MASTER_ID, selOutType.getTypeId());
              callWebService(URLConstants.GET_OUTWARD_BATCH_ID, requestParams, args, true, getString(R.string.progress_msg_getting_data));
            }
            catch(Exception e){ e.printStackTrace(); }
          }
          else{
            args.putString(ParamConstants.BATCH_ID, batchId);
            args.putString(ParamConstants.LIST_REF_BATCH_ID, listRefBatchId);
            context.loadFragment(new OutwardToteScanCartonFragment(), args);
          }
        }
      }
    });
    
    updateSpinnerAdapter("");
    
    binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        callConfirmUploadAPI();
      }
    });

    /*binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
          return;
        callConfirmUploadAPI();
      }
    });
    
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          binding.btnSwipeUpload.reset();
          callConfirmUploadAPI();
        }
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.clMain, binding.btnUpload, binding.btnSwipeUpload);*/
    
    updateView(false);
    
    return binding.getRoot();
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    final OutwardBatch outwardBatch = isNonEmpty(batchId) ? outwardBatchDao.getOutwardBatchFromBatchId(batchId) : getLastPendingBatchId();
    if(outwardBatch != null && outwardBatch.getAcceptedCartons() > 0){
      if(isEmptyToteOutward && isNullOrEmpty(listEans)){
        listEans.addAll(SharedPrefManager.getStringArrayList(ParamConstants.OUTWARD_TOTE_EANS, new ArrayList<String>(0)));
      }
      batchId = outwardBatch.getBatchId();
      listRefBatchId = outwardBatch.getListRefBatchId();
      listExpQty = outwardBatch.getListExpQty();
      destSiteCode = outwardBatch.getDestSiteCode();
      binding.ivDestCode.setText(destSiteCode);
      OutwardTypes selOutType = new OutwardTypes(outwardBatch.getOwtTypeId(), outwardBatch.getOwtType());
      listOutwardTypes.clear();
      listOutwardTypes.add(selOutType);
      selOutwardType = selOutType;
      updateSpinnerAdapter("");
      updateView(true);
    }
    if(isNullOrEmpty(batchId)) callAPI();
  }
  
  OutwardBatch getLastPendingBatchId(){
    OutwardBatch ob = null;
    String[] keys = isEmptyToteOutward ? new String[]{"Empty", "Tote"} : isOffRange ? new String[]{"Range", "OffRange", "OFRNG"} : null;
    if(keys != null && keys.length > 0) for(String key : keys){
      ob = outwardBatchDao.getLastPendingOutwardBatchByKeys(key.trim());
      if(ob != null) break;
    }
    return ob;
  }
  
  @Override
  public void onDestroyView(){
//    if(isNonEmpty(batchId) && outwardBatchDao.isBatchExist(batchId)){
//      deleteBatch();
//    }
//    AppDatabase.getProductDao(context).deleteAll(getSessionTypeValue());
    super.onDestroyView();
  }
  
  private void deleteBatch(){
    AppDatabase.getInventoryDao(context).deleteInventory(isOffRange? AppCommonMethods.SessionType.OFF_RANGE.getValue():isEmptyToteOutward?AppCommonMethods.SessionType.OUTWARD_TOTE.getValue():0);
    AppDatabase.getUploadInventoryDao(context).deleteUploaded();
    outwardBatchDao.deleteBatch(batchId);
    SharedPrefManager.setStringArrayList(batchId + ParamConstants.COMPLETED_CARTONS, new ArrayList<>(0));
    SharedPrefManager.setStringArrayList(ParamConstants.OUTWARD_TOTE_EANS, new ArrayList<>(0));
    batchId = "";
  }
  
  /**
   * Set upload constraints.
   *
   * @param root       the root
   * @param btnCloud   the btn cloud
   * @param btnSwipeUp the btn swipe up
   */
  public void setUploadConstraints(final ConstraintLayout root, final FloatingActionButton btnCloud, final ProSwipeButtonVar btnSwipeUp){
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(root);
    constraintSet.clear(btnCloud.getId(), isUploadSlider ? ConstraintSet.BOTTOM : ConstraintSet.TOP);
    constraintSet.connect(btnCloud.getId(), isUploadSlider ? ConstraintSet.TOP : ConstraintSet.BOTTOM, isUploadSlider ? btnSwipeUp.getId() : ConstraintSet.PARENT_ID, isUploadSlider ? ConstraintSet.TOP : ConstraintSet.BOTTOM);
    constraintSet.applyTo(root);
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      if(isNonEmpty(batchId) && outwardBatchDao.getAcceptedCartonCount(batchId) > 0)
        updateView(true);
    }
  }
  
  private void updateView(boolean isLockView){
    //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isNonEmpty(batchId) && isLockView ? View.VISIBLE : View.GONE);
    binding.btnUpload.setVisibility(AppCommonMethods.isNonEmpty(batchId) && isLockView ? View.VISIBLE : View.GONE);
    binding.spinOutwardToteType.setEnabled(!isLockView && listOutwardTypes.size() != 1);
    binding.swipeLayout.setEnabled(!isLockView);
    binding.ivDestCode.setEnabled(!isLockView);
    binding.swipeLayout.setEnabled(!isLockView);
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    try{
      JSONObject requestParams = new JSONObject();
      requestParams.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
      requestParams.put(ParamConstants.IS_OFF_RANGE, isOffRange);
      callWebService(isEmptyToteOutward ? URLConstants.GET_OUTWARD_TOTE_EANS : URLConstants.GET_OUTWARD_TYPES, requestParams, getString(R.string.progress_msg_getting_data));
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  private ArrayList<String> getEanList(JSONArray jsonArrayEans) throws JSONException{
    ArrayList<String> listEans = new ArrayList<>();
    if(isNonEmpty(jsonArrayEans)){
      for(int i = 0; i < jsonArrayEans.length(); i++){
        Object obj = jsonArrayEans.get(i);
        if(obj instanceof String && isNonEmpty((String) obj)){
          listEans.add(obj.toString());
        }
        else if(obj instanceof JSONObject){
          JSONObject jObj = (JSONObject) obj;
          final String ean = extractString(jObj, ParamConstants.EAN, extractString(jObj, ParamConstants.SKU_ID, extractString(jObj, ParamConstants.OUTWARD_TOTE_EAN)));
          if(isNonEmpty(ean)) listEans.add(ean);
        }
      }
    }
    return listEans;
  }
  
  public void updateSpinnerAdapter(final String label){
    binding.spinOutwardToteType.setAdapter(listOutwardTypes, false, SharedPrefManager.getString(ParamConstants.LABEL_OUTWARD_TOTE_TYPES, label));
    binding.spinOutwardToteType.setEnabled(listOutwardTypes.size() != 1);
  }
  
  private void callAPIForTypes(){
    new Handler().postDelayed(new Runnable(){
      @Override
      public void run(){
        try{
          JSONObject requestParams = new JSONObject();
          requestParams.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
          callWebService(URLConstants.GET_OUTWARD_TYPES, requestParams, getString(R.string.progress_msg_getting_data));
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    }, 50);
    
  }
  
  private int getSessionTypeValue(){
    return getSessionType().getValue();
  }
  
  @Override
  protected AppCommonMethods.SessionType getSessionType(){
    return isOffRange ? AppCommonMethods.SessionType.OFF_RANGE : isEmptyToteOutward ? AppCommonMethods.SessionType.OUTWARD_TOTE : super.getSessionType();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_OUTWARD_TOTE_EANS:
          if(isSuccess && jsonResponse != null){
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
              hideProgressDialog();
              context.showCustomErrDialog(errMsg);
            }
            else{
              JSONObject data = extractJSONObject(jsonResponse, ParamConstants.DATA, jsonResponse);
              JSONArray jsonArrayEans = extractJSONArray(data, ParamConstants.OUTWARD_TOTE_EANS);
              listEans = getEanList(jsonArrayEans);
              if(isNonEmpty(listEans)){
                SharedPrefManager.setStringArrayList(ParamConstants.LIST_TOTE_EANS, listEans);
                JSONArray jsonArrayTypes = extractJSONArray(data, ParamConstants.OUTWARD_TOTE_TYPES, extractJSONArray(data, ParamConstants.TYPES));
                final String label = AppCommonMethods.extractString(data, ParamConstants.LABEL, binding.spinOutwardToteType.getLabel());
                /*if(isNonEmpty(jsonArrayTypes) && false){
                  listOutwardTypes.clear();
                  for(int i = 0; i < jsonArrayTypes.length(); i++){
                    Object obj = jsonArrayTypes.get(i);
                    if(obj instanceof String && isNonEmpty((String) obj)){
                      if(isEmptyToteOutward && !obj.toString().matches("(?i)^.*(Empty|Tote).*$"))
                        continue;
                      listOutwardTypes.add(new OutwardTypes((long) (i + 1), obj.toString()));
                      if(isEmptyToteOutward)
                        listOutwardToteTypes.add(new TypeEans(obj.toString(), listEans));
                    }
                    else if(obj instanceof JSONObject){
                      JSONObject jObj = (JSONObject) obj;
                      final OutwardTypes outwardTypes = getGSON().fromJson(jObj.toString(), OutwardTypes.class);
                      final String type = outwardTypes != null ? outwardTypes.getName() : extractString(jObj, ParamConstants.TYPE, extractString(jObj, ParamConstants.REASON, extractString(jObj, ParamConstants.OUTWARD_TOTE_TYPE)));
                      if(isEmptyToteOutward && !type.matches("(?i)^.*(Empty|Tote).*$")) continue;
                      if(outwardTypes != null) listOutwardTypes.add(outwardTypes);
                    }
                  }
                  updateSpinnerAdapter(label);
                }
                else*/
                callAPIForTypes();
              }
            }
          }
          break;
        case URLConstants.GET_OUTWARD_TYPES:
          if(isSuccess && jsonResponse != null){
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString()))
              context.showCustomErrDialog(errMsg);
            else{
              final MenuModel menuModel = getMenuModel();
              final String title = menuModel != null ? menuModel.getScreenMenuName() : extractString(getArguments(), AppConstants.TITLE, "");
              
              JSONObject data = extractJSONObject(jsonResponse, ParamConstants.DATA, jsonResponse);
              JSONArray jsonArrayTypes = extractJSONArray(data, ParamConstants.OUTWARD_TOTE_TYPES, extractJSONArray(data, ParamConstants.TYPES));
              JSONArray jsonArrayDestinations = extractJSONArray(data, ParamConstants.DESTINATIONS, extractJSONArray(data, ParamConstants.DESTINATION_CODES, extractJSONArray(data, ParamConstants.DESTINATION_SITE_CODES)));
              final String label = AppCommonMethods.extractString(data, ParamConstants.LABEL, binding.spinOutwardToteType.getLabel());
              if(isNonEmpty(jsonArrayTypes)){
                listOutwardTypes.clear();
                for(int i = 0; i < jsonArrayTypes.length(); i++){
                  Object obj = jsonArrayTypes.get(i);
                  if(obj instanceof String && isNonEmpty((String) obj)){
                    final String type = (String) obj;
                    if(isEmptyToteOutward && !chkNull(type, "").matches("(?i)(^.*(Empty|Tote).*$)"))
                      continue;
                    if(isOffRange && !chkNull(type, "").matches("(?i)(^.*(Range|OffRange|OFRNG).*$)"))
                      continue;
                    listOutwardTypes.add(new OutwardTypes((long) (i + 1), isEmptyToteOutward ? chkNull(title, type) : type));
                    if(isEmptyToteOutward) listOutwardToteTypes.add(new TypeEans(type, listEans));
                  }
                  else if(obj instanceof JSONObject){
                    JSONObject jObj = (JSONObject) obj;
                    final OutwardTypes outwardTypes = getGSON().fromJson(jObj.toString(), OutwardTypes.class);
                    final String type = outwardTypes != null && isNonEmpty(outwardTypes.getName()) ? outwardTypes.getName() : extractString(jObj, ParamConstants.TYPE, extractString(jObj, ParamConstants.REASON, extractString(jObj, ParamConstants.OUTWARD_TOTE_TYPE, "")));
                    if(isEmptyToteOutward && !chkNull(type, "").matches("(?i)(^.*(Empty|Tote).*$)"))
                      continue;
                    if(isOffRange && !chkNull(type, "").matches("(?i)(^.*(Range|OffRange|OFRNG).*$)"))
                      continue;
                    if(outwardTypes != null){
                      outwardTypes.setName(isEmptyToteOutward ? chkNull(title, type) : type);
                      listOutwardTypes.add(outwardTypes);
                    }
                  }
                }
                if(isOffRange && listOutwardTypes.size()==1)
                  SharedPrefManager.setArrayList(ParamConstants.OUTWARD_TOTE_TYPES,listOutwardTypes);
              }
              ArrayList<String> destinations = new ArrayList<>();
              if(isNonEmpty(jsonArrayDestinations)){
                for(int i = 0; i < jsonArrayDestinations.length(); i++){
                  Object obj = jsonArrayDestinations.get(i);
                  if(obj instanceof String && isNonEmpty((String) obj)){
                    destinations.add(obj.toString());
                  }
                  else if(obj instanceof JSONObject){
                    JSONObject jObj = (JSONObject) obj;
                    final String type = extractString(jObj, ParamConstants.CODE, extractString(jObj, ParamConstants.NAME, extractString(jObj, ParamConstants.DESTINATION_CODE, extractString(jObj, ParamConstants.DESTINATION_SITE_CODE, extractString(jObj, ParamConstants.DESTINATION_SITE)))));
                    if(isNonEmpty(type)) destinations.add(type);
                  }
                }
              }
              if(isNonEmpty(destinations)) binding.ivDestCode.setAdapter(destinations);
              updateSpinnerAdapter(label);
            }
          }
          break;
        case URLConstants.GET_OUTWARD_TOTE_DATA:
          if(isSuccess && jsonResponse != null){
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
              hideProgressDialog();
              context.showCustomErrDialog(errMsg);
            }
            else{
              JSONArray jsonArrayEans = extractJSONArray(jsonResponse, ParamConstants.OUTWARD_TOTE_EANS);
              JSONArray jsonArrayTypes = extractJSONArray(jsonResponse, ParamConstants.OUTWARD_TOTE_TYPES, extractJSONArray(jsonResponse, ParamConstants.TYPES));
              ArrayList<String> listEans = getEanList(jsonArrayEans);
              final String label = AppCommonMethods.extractString(jsonResponse, ParamConstants.LABEL, binding.spinOutwardToteType.getLabel());
              if(isNonEmpty(jsonArrayTypes)){
                listOutwardToteTypes.clear();
                for(int i = 0; i < jsonArrayTypes.length(); i++){
                  Object obj = jsonArrayTypes.get(i);
                  if(obj instanceof String && isNonEmpty((String) obj))
                    listOutwardToteTypes.add(new TypeEans(obj.toString(), listEans));
                  else if(obj instanceof JSONObject){
                    JSONObject jObj = (JSONObject) obj;
                    final String type = extractString(jObj, ParamConstants.TYPE, extractString(jObj, ParamConstants.REASON, extractString(jObj, ParamConstants.OUTWARD_TOTE_TYPE)));
                    if(isNonEmpty(type)){
                      JSONArray jEans = extractJSONArray(jObj, ParamConstants.OUTWARD_TOTE_EANS);
                      if(isNonEmpty(jEans))
                        listOutwardToteTypes.add(new TypeEans(type, getEanList(jEans)));
                      else listOutwardToteTypes.add(new TypeEans(type, listEans));
                    }
                  }
                }
              }
              updateSpinnerAdapter(label);
              if(isNonEmpty(listOutwardToteTypes)){
                JSONArray jarray = new JSONArray();
                for(TypeEans typeEans : listOutwardToteTypes)
                  if(typeEans != null && isNonEmpty(typeEans.getType()) && isNonEmpty(typeEans.getEans()))
                    jarray.put(typeEans.toJson());
                if(isNonEmpty(jarray))
                  new InsertDBOutwardToteTypeEANs((MainActivity) context, OffRangeMainFragment.this, url, label).execute(jarray);
              }
            }
          }
          break;
        case URLConstants.GET_OUTWARD_BATCH_ID:
          if(isSuccess && jsonResponse != null){
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
              hideProgressDialog();
              context.showCustomErrDialog(errMsg);
            }
            else{
              JSONObject data = extractJSONObject(jsonResponse, ParamConstants.DATA, jsonResponse);
              final String batch = extractString(data, ParamConstants.BATCH_ID,extractString(data, ParamConstants.BATCH));
              final JSONArray jsonArrayBatchDetails = extractJSONArray(data, ParamConstants.BATCH_DETAILS);
              if(isNonEmpty(batch)){
                batchId = batch;
                args.putString(ParamConstants.BATCH_ID, batchId);
                if(!outwardBatchDao.isBatchExist(batchId))
                  insertBatch(batchId, isNonEmpty(jsonArrayBatchDetails) ? jsonArrayBatchDetails.length() : 0, args);
                context.loadFragment(new OutwardToteScanCartonFragment(), args);
                if(isNonEmpty(jsonArrayBatchDetails)){
                  ArrayList<String> listCompletedCartons = SharedPrefManager.getStringArrayList(batchId + ParamConstants.COMPLETED_CARTONS, new ArrayList<>(0));
                  for(int i = 0; i < jsonArrayBatchDetails.length(); i++){
                    Object obj = jsonArrayBatchDetails.get(i);
                    if(obj == null) continue;
                    JSONObject jObj = obj instanceof JSONObject ? jsonArrayBatchDetails.getJSONObject(i) : null;
                    String str = jObj == null ? obj.toString() : null;
                    final String cartonNo = chkNull(str, extractString(jObj, ParamConstants.CARTON_NO, extractString(jsonRequest, ParamConstants.CARTON_NUM, extractString(jsonRequest, ParamConstants.CARTON_NUMBER))));
                    if(isNonEmpty(cartonNo) && !listCompletedCartons.contains(cartonNo)){
                      listCompletedCartons.add(cartonNo);
                      SharedPrefManager.setStringArrayList(batchId + ParamConstants.COMPLETED_CARTONS, listCompletedCartons);
                    }
                  }
                  updateView(true);
                }
              }
            }
          }
          break;
        case URLConstants.UPLOAD_OUTWARD_CARTON_DATA:
          if(isSuccess){
            final InventoryDao inventoryDao = AppDatabase.getInventoryDao(context);
            final UploadInventoryDao uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
            final String sessionId = extractString(jsonRequest, ParamConstants.SESSION_ID, "");
            final String cartonNo = extractString(jsonRequest, ParamConstants.CARTON_NO, extractString(jsonRequest, ParamConstants.CARTON_NUM, extractString(jsonRequest, ParamConstants.CARTON_NUMBER)));
            final String batchId = extractString(jsonRequest, ParamConstants.BATCH_ID, extractString(jsonRequest, ParamConstants.BATCH_ID));
            final ArrayList<String> listCompletedCartons = isNonEmpty(batchId) && isNonEmpty(cartonNo) ? SharedPrefManager.getStringArrayList(batchId + ParamConstants.COMPLETED_CARTONS, new ArrayList<>(0)) : new ArrayList<>(0);
            try{
              uploadInventoryDao.updateUploaded(sessionId);
              inventoryDao.updateUploaded(sessionId);
            }
            catch(Exception e){ e.printStackTrace(); }
            //            if(listCompletedCartons.contains(cartonNo)){
            //              listCompletedCartons.remove(cartonNo);
            //              SharedPrefManager.setStringArrayList(batchId+ParamConstants.COMPLETED_CARTONS,listCompletedCartons);
            //            }
            //uploadInventoryDao.deleteUploaded();
            /*if(isOffRange && AppDatabase.getUploadInventoryDao(context).getNonUploadedCartonCountFromBatchId(batchId) == 0)*/
            callUploadAPI();
          }
          break;
        case URLConstants.COMPLETE_OUTWARD_BATCH_ID:
          if(isSuccess && jsonResponse != null){
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if(isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())){
              hideProgressDialog();
              context.showCustomErrDialog(errMsg);
            }
            else{
              final String successMsg = extractString(jsonResponse, ParamConstants.SUCCESS,"");
              deleteBatch();
              updateView(false);
              binding.ivDestCode.setText("");
              context.showCustomSuccessDialog(chkNull(isNonEmpty(successMsg.replaceAll("(?i)(true|false|t|f)","").trim())?successMsg:"",extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode()))), isNonEmpty(successMsg.replaceAll("(?i)(true|false|t|f)","").trim()));
            }
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  private void insertBatch(final String batchId, final int completedCartonCount, final Bundle args){
    OutwardBatch outwardBatch = new OutwardBatch();
    final OutwardTypes outType = (OutwardTypes) extractSerializable(args, OutwardTypes.class);
    final String destCode = extractString(args, ParamConstants.DESTINATION_SITE_CODE, extractString(args, ParamConstants.DESTINATION_SITE, extractString(args, ParamConstants.DESTINATION_CODE)));
    outwardBatch.setListRefBatchId(listRefBatchId);
    outwardBatch.setListExpQty(listExpQty);
    outwardBatch.setBatchId(batchId);
    outwardBatch.setDestSiteCode(destCode);
    outwardBatch.setOwtType(outType.getName());
    outwardBatch.setOwtTypeId(outType.getTypeId());
    final int nonUploadedCartonCount = isOffRange ? AppDatabase.getUploadInventoryDao(context).getNonUploadedCartonCountFromBatchId(batchId) : 0;
    final int totalCompletedCartonCount = completedCartonCount + nonUploadedCartonCount;
    if(chkZero(totalCompletedCartonCount, 0) > 0){
      outwardBatch.setAcceptedCartons(totalCompletedCartonCount);
      outwardBatch.setTotalCartons(totalCompletedCartonCount);
    }
    outwardBatchDao.insert(outwardBatch);
    //this.outwardBatch=outwardBatch;
  }
  
  public void callConfirmUploadAPI(){
    context.showCustomConfirmDialog((String.format(getString(R.string.msg_completed_cartons, "" + outwardBatchDao.getAcceptedCartonCount(batchId)) + "\n" + getString(R.string.msg_upload_batch_alert)).trim()), R.string.btn_complete, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        callUploadAPI();
      }
    });
  }
  
  public void callUploadAPI(){
    //TODO check if all cartons are uploaded against this batch before completing batch
    if(isOffRange && AppDatabase.getUploadInventoryDao(context).getNonUploadedCartonCountFromBatchId(batchId) > 0){
      final UploadInventoryDao uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
      final List<String> sessionIds = uploadInventoryDao.getSessionIdsFromBatchId(AppCommonMethods.SessionType.OFF_RANGE.getValue(), batchId);
      AppCommonMethods.showLog("sessionIds", "" + sessionIds.size());
      if(isNonEmpty(sessionIds)){//for(String sessionId : sessionIds){
        String sessionId = sessionIds.get(0);
        AppCommonMethods.showLog("sessionId", sessionId);
        final List<UploadInventory> listUploadInvData = uploadInventoryDao.getAllUploadInventoryData(sessionId);
        showLog("uploadSavedCartons_uploadCount", "" + batchId + "->" + (isNonEmpty(listUploadInvData) ? listUploadInvData.size() : 0));
        if(isNonEmpty(listUploadInvData)){
          showLog("listUploadInvData()", "" + listUploadInvData.size());
          try{
            JSONObject requestParams = null;//new JSONObject();
            JSONArray js = new JSONArray();
            for(UploadInventory uploadInventory : listUploadInvData){
              if(uploadInventory != null){
                if(requestParams == null){
                  requestParams = isNonEmpty(uploadInventory.extras) ? new JSONObject(uploadInventory.extras) : new JSONObject();
                  requestParams.put(ParamConstants.SESSION_ID, sessionId);
                  requestParams.put(ParamConstants.SESSION_TYPE, AppCommonMethods.SessionType.OFF_RANGE.name());
                  requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD);
                }
                final JSONObject dataObject = uploadInventory.toJson();
                if(dataObject != null && chkNull(dataObject.toString(), "").length() > 2){
                  js.put(dataObject);
                }
              }
            }
            requestParams.put(ParamConstants.ITEMS, js);
            Bundle args = new Bundle();
            callWebService(URLConstants.UPLOAD_OUTWARD_CARTON_DATA, requestParams, args, getString(R.string.progress_msg_uploading_data));
          }
          catch(Exception e){ e.printStackTrace(); }
        }
      }
      return;
    }
    try{
      showProgressDialog(getString(R.string.progress_msg_check_upload_data));
      //Send Empty Array in JSON Request if value is 'All'
      JSONObject requestParams = new JSONObject();
      requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
      if(selOutwardType != null){
        requestParams.put(ParamConstants.TYPE, selOutwardType.getName());
        requestParams.put(ParamConstants.STORE_OUTWARD_TYPE_MASTER_ID, selOutwardType.getTypeId());
      }
      requestParams.put(ParamConstants.DESTINATION_SITE_CODE, destSiteCode);
      requestParams.put(ParamConstants.DESTINATION_CODE, destSiteCode);
      requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD);
      requestParams.put(ParamConstants.IS_EMPTY_TOTE_OUTWARD, isEmptyToteOutward);
      requestParams.put(ParamConstants.IS_OFF_RANGE, isOffRange);
      requestParams.put(ParamConstants.BATCH_ID, batchId);
      requestParams.put(ParamConstants.LIST_REF_BATCH_ID, listRefBatchId);
      requestParams.put(ParamConstants.STATUS, AppConstants.STATUS_COMPLETED);//"Complete");
      callWebService(URLConstants.COMPLETE_OUTWARD_BATCH_ID, requestParams, getString(R.string.progress_msg_uploading_data), false);
    }
    catch(JSONException e){
      e.printStackTrace();
      hideProgressDialog();
    }
  }
  
  @Override
  public void onBackPressed(){
    if(isNonEmpty(batchId) && outwardBatchDao.getAcceptedCartonCount(batchId) > 0){
      context.showCustomAlertDialog(getString(R.string.title_batch_cancel), getString(R.string.msg_data_discarded), getString(R.string.btn_no), null, getString(R.string.btn_yes), (dialogInterface, i) -> {
        deleteBatch();
        AppDatabase.getProductDao(context).deleteAll(getSessionTypeValue());
        context.popBackStack();
      });
    }
    else super.onBackPressed();
  }
}
