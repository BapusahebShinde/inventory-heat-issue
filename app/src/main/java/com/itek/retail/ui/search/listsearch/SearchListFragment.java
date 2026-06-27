package com.itek.retail.ui.search.listsearch;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.extractStringArrayList;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUse24LengthTIDForUpload;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.ProductListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.ProductDao;
import com.itek.retail.databinding.FragmentSearchListBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Replenishment list fragment.
 */
public class SearchListFragment extends CommonFragment{
  
  private final boolean isAllowListManualListCompletionForDecode = true;
  private final int sessionType = AppCommonMethods.SessionType.SEARCH_LIST.getValue();
  public FragmentSearchListBinding binding;
  ProductDao productDao;
  private List<String> listBrands = new ArrayList<>(0);
  private List<String> listCategories = new ArrayList<>(0);
  private List<ProductModel> listProducts = new ArrayList<>(0);
  private SearchListViewModel mViewModel;
  private String searchListType = "";
  private String searchListName = "";
  private String searchListId = "0";
  private boolean isAllowDecode = false;
  private boolean isAllowDecodeOnPick = false;
  private int found = 0, decoded = 0, total = 0, required = 0;
  
  /**
   * Instantiates a new Replenishment list fragment.
   */
  public SearchListFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(SearchListViewModel.class);
    binding = FragmentSearchListBinding.inflate(inflater, container, false);
    searchListId = extractString(getArguments(), AppConstants.SEARCH_LIST_ID, "0");
    searchListType = extractString(getArguments(), AppConstants.SEARCH_LIST_TYPE, "");
    isAllowDecode = extractBoolean(getArguments(), AppConstants.IS_ALLOW_DECODE, false);
    isAllowDecodeOnPick = isAllowDecode && extractBoolean(getArguments(), AppConstants.IS_ALLOW_DECODE_ON_PICK, false);
    
    listBrands.clear();
    listBrands.add(AppConstants.ALL);
    List<String> brands = productDao.getBrandList();
    if(isNonEmpty(brands)) listBrands.addAll(brands);
    
    listCategories.clear();
    listCategories.add(AppConstants.ALL);
    List<String> categories = productDao.getCategoryList();
    if(isNonEmpty(categories)) listCategories.addAll(categories);
    
    binding.spinBrand.setAdapter(listBrands);
    binding.spinCategory.setAdapter(listCategories);
    
    binding.listSearchProducts.setAdapter(new ProductListAdapter((MainActivity) context, SearchListFragment.this, listProducts));
    binding.listSearchProducts.setLayoutManager(/*isLandscape?new GridLayoutManager(context,2) :*/new LinearLayoutManager(context));

    binding.edtSearchName.setHint(String.format(context.getString(R.string.hint_search_by__), SharedPrefManager.getString(ParamConstants.LABEL_NAME,context.getString(R.string.lbl_name))+"/"+SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean))));
    binding.edtSearchName.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void afterTextChanged(Editable editable){ updateLists(); }
    });
    
    binding.edtSearchName.setOnEditorActionListener(new TextView.OnEditorActionListener(){
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent){
        if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
          hideKeyboard();
          return true;
        }
        return false;
      }
    });
    
    binding.spinBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){/*Don't handle (Default Overridden Empty Method)*/}
    });
    
    binding.spinCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        showLog("ReplenishList_Category", "OnItemSelected");
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){/*Don't handle (Default Overridden Empty Method)*/}
    });
    
    updateLists();
    
    binding.btnUpload.setOnClickListener(view -> {
      if(isAllowDecode && !isAllowListManualListCompletionForDecode) callUploadAPI();
      else callAPI(true);
    });
    
    /*binding.btnUpload.setOnClickListener(view -> {
      if(view.getVisibility() != View.VISIBLE) return;
      if(binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
        return;
      if(isAllowDecode && !isAllowListManualListCompletionForDecode) callUploadAPI();
      else callAPI(true);
    });
    
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          binding.btnSwipeUpload.reset();
          if(isAllowDecode && !isAllowListManualListCompletionForDecode) callUploadAPI();
          else callAPI(true);
        }
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);*/
    
    return binding.getRoot();
  }
  
  public boolean isAllowDecode(){ return isAllowDecode; }
  
  public boolean isAllowDecodeOnPick(){ return isAllowDecodeOnPick; }
  
  /**
   * Get sel brand string.
   *
   * @return the string
   */
  public String getSelBrand(){ return chkNull(binding.spinBrand.getSelectedItem(), AppConstants.ALL); }
  
  /**
   * Get sel category string.
   *
   * @return the string
   */
  public String getSelCategory(){ return chkNull(binding.spinCategory.getSelectedItem(), AppConstants.ALL); }
  
  /**
   * Get search list id string.
   *
   * @return the string
   */
  public String getSearchListId(){
    return searchListId;
  }
  
  /**
   * Get search list type string.
   *
   * @return the string
   */
  public String getSearchListType(){
    return searchListType;
  }
  
  /**
   * Get search list name string.
   *
   * @return the string
   */
  public String getSearchListName(){
    return searchListName;
  }
  
  private void callUploadAPI(){
    final String action = AppConstants.SESSION_ACTION_UPLOAD;
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    if(isInternetConnected(context, false, isUpload)){
      try{
        if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.SEARCH_LIST_ID, searchListId);
        requestParams.put(ParamConstants.SEARCH_LIST_TYPE, searchListType);
        requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
        requestParams.put(ParamConstants.TYPE, AppConstants.DECODE);//""Shrinkage");
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        if(isUpload){
          showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          new Handler().post(new Runnable(){
            @Override
            public void run(){
              try{
                JSONArray js = new JSONArray();
                ArrayList<String> tids = new ArrayList<>(0);
                for(Inventory inventory : AppDatabase.getInventoryDao(context).getAllInventoryData(sessionType)){
                  if(inventory != null && isUpload && !inventory.isUploaded && inventory.isDecoded()){
                    JSONObject dataobject = inventory.toJson(context);
                    if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2){
                      js.put(dataobject);
                      tids.add(inventory.tid);
                    }
                  }
                }
                requestParams.put(ParamConstants.ITEMS, js);
                if(js != null && js.length() > 0){
                  Bundle args = new Bundle();
                  if(isNonEmpty(tids)) args.putStringArrayList(ParamConstants.TIDS, tids);
                  callWebService(URLConstants.UPLOAD_ENCODE, requestParams, args, getString(R.string.progress_msg_uploading_data), !isUpload);
                }
              }
              catch(Exception e){
                e.printStackTrace();
                hideProgressDialog();
              }
            }
          });
        }
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
      }
    }
  }
  
  private void callAPI(final boolean isCompleteList){
    context.showCustomAlertDialog("", R.string.msg_list_mark_complete, R.string.btn_yes, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        callListStatusUpdateAPI(isCompleteList);
      }
    }, R.string.btn_no, null);
  }
  
  private void callListStatusUpdateAPI(final boolean isCompleteList){
    try{
      JSONObject requestParams = new JSONObject();
      requestParams.put(ParamConstants.SEARCH_LIST_ID, searchListId);
      requestParams.put(ParamConstants.TYPE, searchListType);
      requestParams.put(ParamConstants.SEARCH_LIST_TYPE, searchListType);
      requestParams.put(ParamConstants.STATUS, isCompleteList ? AppConstants.PICK_LIST_STATUS_COMPLETE : AppConstants.PICK_LIST_STATUS_RELEASE);
      //Add/Set Picked/Searched Data as JSON Array (Now Commented)
      //showProgressDialog(getString(R.string.progress_msg_check_upload_data));
          /*new Handler().post(new Runnable(){
            @Override
            public void run(){
              try{
                JSONArray js = new JSONArray();
                for(Inventory inventory : AppDatabase.getInventoryDao(context).getAllInventoryData(sessionType)){
                  if(inventory != null && !inventory.isUploaded){
                    JSONObject dataobject = inventory.toJson();
                    if(dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                      js.put(dataobject);
                  }
                }
                requestParams.put(ParamConstants.ITEMS, js);
                callWebService(URLConstants.UPDATE_PICK_LIST_STATUS, requestParams, getString(R.string.progress_msg_uploading_data));
              }catch(Exception e){
                e.printStackTrace();
                hideProgressDialog();
              }
            }
          });*/
      callWebService(URLConstants.UPDATE_PICK_LIST_STATUS, requestParams, getString(R.string.progress_msg_connect_server));
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      updateLists();
    }
  }
  
  @Override
  public void onBackPressed(){
    context.showCustomAlertDialog("", /*isAllowDecode && decoded>0?String.format(getString(R.string.msg_list_search_back_decoded),""+decoded):*/getString(R.string.msg_list_search_back), R.string.btn_no, null, R.string.btn_yes, new DialogInterface.OnClickListener(){
      @Override
      public void onClick(DialogInterface dialogInterface, int i){
        if(isAllowDecode) callListStatusUpdateAPI(false);
        else{
          productDao.deleteAllExcept();
          AppDatabase.getInventoryDao(context).deleteAllExcept();
          popBackStack();
        }
      }
    });
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    final String selBrand = binding.spinBrand.getSelectedItem();
    final Set<String> selBrands = binding.spinBrand.getSelectedVals();
    final String selCategory = binding.spinCategory.getSelectedItem();
    final Set<String> selCategories = binding.spinCategory.getSelectedVals();
    final String searchName = chkNull(binding.edtSearchName.getText().toString(), "").trim();
    listProducts.clear();
    listProducts.addAll(productDao.getOmniProducts(selCategory, selCategories, selBrand, selBrands, searchName));
    ((RecyclerView.Adapter) binding.listSearchProducts.getAdapter()).notifyDataSetChanged();
    final Integer totalEanQty = productDao.getTotalEANCount(selCategory, selCategories, selBrand, selBrands, searchName);
    binding.txtTotal.setText(String.format(getString(R.string.txt_movement_replenishment_total), chkZero(totalEanQty, "0")));
    if(isAllowDecode){
      //final MultiQtyModel multiQtyModel = productDao.getTotalCounts(sessionType);
      decoded = AppDatabase.getInventoryDao(context).getNonUploadedDecodeCount(sessionType);
      /*found = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
      decoded = multiQtyModel != null ? chkNull(multiQtyModel.decoded, 0) : 0;
      total = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
      required = multiQtyModel != null ? chkNull(multiQtyModel.required, 0) : 0;*/
      
      final boolean isShowUpload = !isAllowDecode || isAllowListManualListCompletionForDecode;// || decoded > 0;
      ((MainActivity) context).lockDrawer(decoded > 0 /*&& total > 0*/ /*&& required > 0*/);
      binding.btnUpload.setVisibility(isShowUpload ? View.VISIBLE : View.GONE);
      binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && isShowUpload ? View.VISIBLE : View.GONE);
    }
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.UPDATE_PICK_LIST_STATUS:
          if(isSuccess){
            final String status = extractString(jsonRequest, ParamConstants.STATUS, AppConstants.PICK_LIST_STATUS_COMPLETE);
            productDao.deleteAllExcept();
            AppDatabase.getInventoryDao(context).deleteAllExcept();
            if(isAllowDecode && !status.equalsIgnoreCase(AppConstants.PICK_LIST_STATUS_COMPLETE))
              popBackStack();
            else
              context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_update), getTypeCharCode())), true, true);
          }
          break;
        case URLConstants.GET_MAPPED_EAN:
          if(isSuccess){
            final String ean = extractString(jsonResponse, ParamConstants.EAN, extractString(jsonRequest, ParamConstants.EAN));
            final String mappedEan = extractString(jsonResponse, ParamConstants.MAPPED_EAN, "");
            if(args == null) args = getArguments();
            if(args == null) args = new Bundle();
            //use this Mapped EAN for Search/Encode
            args.putString(ParamConstants.MAPPED_EAN, mappedEan);
            if(context != null && !context.isFinishing() && context instanceof MainActivity)
              ((MainActivity) context).checkReaderConnection(new SearchListStartFragment(), args);
          }
          break;
        case URLConstants.UPLOAD_ENCODE:
          if(isSuccess){
            //final int sessionType = sessionObject != null ? sessionObject.sessionType : 0;
            //inventoryDao.updateUploaded(sessionObject.sessionId);
            //if(sessionType > 0){
              /*final boolean isAllDecoded = isAllDecoded();
              final int decodeQty = inventoryDao.getEANDecodeQty(sessionObject.sessionId, model.getEan(), model.getZone());
              final boolean isEanZoneDecoded = isAutoBackOnEanZoneDecoded && decodeQty >= model.getEanQty();
              showLog("isAllDecoded", "" + isAllDecoded);*/
              /*if(searchEPCDialog != null && searchEPCDialog.isShowing())
                searchEPCDialog.dismiss();*/
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_decode), getTypeCharCode())).replaceFirst("(?i)Upload", "Decode"), false/*isAllDecoded || isEanZoneDecoded*/ ? new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialogInterface, int i){
                //if(isAllDecoded) context.doublePopBackStack();
                //else if(isEanZoneDecoded) context.popBackStack();
              }
            } : null);
            //update Uploaded Records
            if(args == null) args = getArguments();
            if(args == null) args = new Bundle();
            final Bundle bundle = args;
            new Handler().post(new Runnable(){
              @Override
              public void run(){
                //if(isAllDecoded) inventoryDao.deleteInventory(sessionType);
                JSONArray js = extractJSONArray(jsonRequest, ParamConstants.ITEMS);
                ArrayList<String> tids = extractStringArrayList(bundle, ParamConstants.TIDS, new ArrayList<String>(0));
                if(isNullOrEmpty(tids) && isNonEmpty(js)){
                  for(int i = 0; i < js.length(); i++){
                    try{
                      final String tid = extractString(js.getJSONObject(i), ParamConstants.TID, "").trim();
                      if(isNonEmpty(tid))
                        tids.add(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid);
                    }
                    catch(Exception e){ e.printStackTrace(); }
                  }
                }
                if(isNonEmpty(tids)){
                  /*if(!isAllDecoded)*/
                  Set<String> distinctTids = new HashSet<String>();
                  distinctTids.addAll(tids);
                  AppDatabase.getInventoryDao(context).updateUploaded(sessionType, distinctTids);
                  AppDatabase.getUploadInventoryDao(context).updateUploaded(sessionType, distinctTids);
                }
                AppDatabase.getUploadInventoryDao(context).deleteUploaded();
                updateLists();
              }
            });
            //}
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}