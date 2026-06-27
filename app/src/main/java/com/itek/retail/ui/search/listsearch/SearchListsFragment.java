package com.itek.retail.ui.search.listsearch;

import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.getSampleJSON;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.itek.retail.R;
import com.itek.retail.adapter.SearchListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.ListDao;
import com.itek.retail.databinding.FragmentSearchListsBinding;
import com.itek.retail.model.ListModel;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Replenishment list fragment.
 */
public class SearchListsFragment extends CommonFragment{
  
  public FragmentSearchListsBinding binding;
  ListDao listDao;
  private List<ListModel> listSearchLists = new ArrayList<>(0);
  private SearchListsViewModel mViewModel;
  //private String sortByValues = "";
  
  /**
   * Instantiates a new Replenishment list fragment.
   */
  public SearchListsFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    listDao = AppDatabase.getListDao(context);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(SearchListsViewModel.class);
    binding = FragmentSearchListsBinding.inflate(inflater, container, false);
    
    binding.listListsCodes.setAdapter(new SearchListAdapter((MainActivity) context, SearchListsFragment.this, listSearchLists));
    binding.listListsCodes.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
    
    //binding.edtListsCode.setHint(String.format(context.getString(R.string.hint_search_by__),SharedPrefManager.getString(ParamConstants.NAME,context.getString(R.string.lbl_name))));
    binding.edtListsCode.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void afterTextChanged(Editable editable){ updateLists(); }
    });
    
    //setHeader();
    
    binding.edtListsCode.setOnEditorActionListener(new TextView.OnEditorActionListener(){
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent){
        if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
          hideKeyboard();
          return true;
        }
        return false;
      }
    });
    
    binding.swipeLayout.setColorSchemeColors(context.getColorPrimaryDarkFromTheme());
    binding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
      @Override
      public void onRefresh(){
        binding.swipeLayout.setRefreshing(false);
        callAPI();
      }
    });
    
    binding.header.imgConfigSync.setVisibility(View.VISIBLE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_sync);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        callAPI();
      }
    });
    
    if(savedInstanceState == null && !listDao.hasData()) callAPI();
    
    updateLists();
    
    return binding.getRoot();
  }
  
  public void callDetailsAPI(final ListModel listModel){
    //selProd = productModel;
    try{
      JSONObject request = new JSONObject();
      request.put(ParamConstants.SEARCH_LIST_ID, listModel.listId);
      request.put(ParamConstants.SEARCH_LIST_TYPE, listModel.listType);
      //callWebService(URLConstants.GET_PICK_LIST_DETAILS,request,context.getString(R.string.progress_msg_getting_data),false,true);
      callWebService(URLConstants.GET_PICK_LIST, request, context.getString(R.string.progress_msg_getting_data), false, true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack() && (listDao == null || !listDao.hasData())){
      callAPI();
    }
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    try{
      callWebService(URLConstants.GET_PICK_LISTS, new JSONObject(), getString(R.string.progress_msg_getting_data), false, true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void onBackPressed(){
    super.onBackPressed();
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    final String searchName = AppCommonMethods.chkNull(binding.edtListsCode.getText().toString(), "").trim();
    listSearchLists.clear();
    listSearchLists.addAll(listDao.getTypeWiseSearchList(searchName));
    if(binding != null && binding.listListsCodes != null && binding.listListsCodes.getAdapter() != null && binding.listListsCodes.getAdapter() instanceof RecyclerView.Adapter)
      ((RecyclerView.Adapter) binding.listListsCodes.getAdapter()).notifyDataSetChanged();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_PICK_LISTS:
          if(isSuccess && jsonResponse != null){
            final JSONArray jsonCodesArray = extractJSONArray(jsonResponse, ParamConstants.DATA, extractJSONArray(jsonResponse, ParamConstants.SEARCH_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.SEARCH_LISTS))));
            if(isNonEmpty(jsonCodesArray)){
              listDao.deleteAll();
              int insertCount = 0;
              showProgressDialog(getString(R.string.progress_msg_check_data));
              for(int a = 0; a < jsonCodesArray.length(); a++){
                final JSONObject listCode = jsonCodesArray.getJSONObject(a);
                final ListModel listModel = getGSON().fromJson(listCode.toString(), ListModel.class);
                if(listModel != null) listDao.insert(listModel);
              }
              if(insertCount > 0){
                SharedPrefManager.setInt(ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED, extractInt(jsonResponse, ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED, AppCommonMethods.markFoundPercentLBS));
              }
              hideProgressDialog();
              updateLists();
            }
            else{
              final String errorMsg = extractString(jsonResponse, ParamConstants.ERROR, "");
              if(isNonEmpty(errorMsg)) context.showCustomErrDialog(errorMsg);
              hideProgressDialog();
              updateLists();
            }
          }
          break;
        case URLConstants.GET_PICK_LIST_DETAILS:
        case URLConstants.GET_PICK_LIST:
          if(isSuccess && jsonResponse != null){
            if(isStaticDebug()){
              final String listType = extractString(jsonRequest, ParamConstants.SEARCH_LIST_TYPE);
              if(isNonEmpty(listType)){
                try{
                  if(listType.matches("(?i)^(Replenish).*$")){
                    jsonResponse = getSampleJSON(context, URLConstants.GET_REPLENISHMENT_LIST);
                    jsonResponse.put(ParamConstants.PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.REPLENISHMENT_PRODUCTS));
                  }
                  else if(listType.matches("(?i)^(Omni).*$")){
                    jsonResponse = getSampleJSON(context, URLConstants.GET_OMNICHANNEL_LIST_DETAILS);
                    jsonResponse.put(ParamConstants.PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.OMNICHANNEL_PRODUCTS));
                  }
                  else if(listType.matches("(?i)^(GRDC|IST|ASSORT).*$")){
                    jsonResponse = getSampleJSON(context, URLConstants.GET_ASSORTMENT_LIST);
                    jsonResponse = extractJSONArray(jsonResponse, ParamConstants.DATA, new JSONArray()).getJSONObject(0);
                    jsonResponse.put(ParamConstants.PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.ITEMS));
                  }
                }
                catch(Exception e){ e.printStackTrace(); }
              }
            }
            
            final boolean isAllowDecode = extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE, false);
            final boolean isAllowDecodeOnPick = isAllowDecode && extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE_ON_PICK, AppCommonMethods.isAllowDecodeOnPick);
            if(isAllowDecode) context.saveTagWritePasswords(jsonResponse);
            
            args = args == null ? new Bundle() : args;
            args.putBoolean(AppConstants.IS_ALLOW_DECODE, isAllowDecode);
            args.putBoolean(AppConstants.IS_ALLOW_DECODE_ON_PICK, isAllowDecodeOnPick);
            
            JSONArray searchProductsArray = extractJSONArray(jsonResponse, ParamConstants.SEARCH_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            if(searchProductsArray != null && context != null && context instanceof MainActivity)
              ((MainActivity) context).callInsertProductDBTask(url, AppCommonMethods.SessionType.SEARCH_LIST.getValue(), jsonResponse, args, searchProductsArray);
            /*else if(isNonEmpty(searchProductsArray)){
              final ProductDao productDao = AppDatabase.getProductDao(context);
              productDao.deleteAll();
              AppDatabase.getInventoryDao(context).deleteAllExcept();
              int insertCount = 0;
              showProgressDialog(getString(R.string.progress_msg_check_data));
              for(int i = 0; i < searchProductsArray.length(); i++){
                final JSONObject product = searchProductsArray.getJSONObject(i);
                final ProductModel productModel = product != null ? getGSON().fromJson(product.toString(), ProductModel.class) : null;
                if(productModel != null){
                  productModel.setSessionType(AppCommonMethods.SessionType.SEARCH_LIST.getValue());
                  productModel.setItemImgUrl(extractString(product, ParamConstants.IMG_URL, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim());
                  final JSONArray jsonZones = extractJSONArray(product, ParamConstants.ZONES);
                  if(jsonZones != null && jsonZones.length() > 0){
                    int totalQty = 0;
                    for(int j = 0; j < jsonZones.length(); j++){
                      JSONObject zone = jsonZones.getJSONObject(j);
                      final String zoneName = extractString(zone, ParamConstants.ZONE_NAME, "");
                      final String zoneId = extractString(zone, ParamConstants.ZONE_ID, "0");
                      final Integer eanQty = extractInt(zone, ParamConstants.EAN_QTY, 0);
                      if(isNonEmpty(zoneName) && chkNull(eanQty, 0) > 0){
                        productModel.setZone(zoneName);
                        productModel.setZoneId(zoneId);
                        productModel.setEanQty(eanQty);
                        totalQty += chkNull(eanQty, 0);
                        productDao.insert(productModel);
                        insertCount++;
                      }
                    }
                    if(insertCount > 0 && totalQty > 0){
                      if(!isUseNewUIForLBS && totalQty < productModel.getQty()){
                        final String zoneName = AppConstants.DEFAULT_NO_VALUE;
                        final String zoneId = "0";
                        final Integer eanQty = productModel.getQty() - totalQty;
                        productModel.setZone(zoneName);
                        productModel.setZoneId(zoneId);
                        productModel.setEanQty(eanQty);
                        totalQty += chkNull(eanQty, 0);
                        productDao.insert(productModel);
                        insertCount++;
                      }
                      productDao.updateTotalQty(productModel.ean, totalQty, productModel.getSessionType());
                    }
                  }
                  else if(!isUseNewUIForLBS){
                    productModel.eanQty = productModel.qty;
                    productModel.totalQty = productModel.qty;
                    productDao.insert(productModel);
                    insertCount++;
                  }
                }
              }
              if(insertCount > 0){
                SharedPrefManager.setInt(ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED, extractInt(jsonResponse, ParamConstants.MARK_FOUND_PERCENT_SER_LIST_BASED, AppCommonMethods.markFoundPercentLBS));
                Bundle bundle = args != null ? args : new Bundle();
                bundle.putString(AppConstants.SEARCH_LIST_ID, extractString(jsonRequest, ParamConstants.SEARCH_LIST_ID, extractString(jsonResponse, ParamConstants.SEARCH_LIST_ID, extractString(jsonResponse, ParamConstants.SEARCH_LIST_NAME, extractString(jsonResponse, ParamConstants.NAME, extractString(jsonResponse, ParamConstants.CODE, extractString(jsonResponse, ParamConstants.ORDER_NO, "")))))));
                bundle.putString(AppConstants.SEARCH_LIST_TYPE, extractString(jsonRequest, ParamConstants.SEARCH_LIST_TYPE, extractString(jsonResponse, ParamConstants.SEARCH_LIST_TYPE, "")));
                context.loadFragment(new SearchListFragment(), bundle);
              }
              hideProgressDialog();
            }*/
            else hideProgressDialog();
          }
          break;
        default:
          break;
        
      }
    }
    catch(Exception e){ }
  }
}