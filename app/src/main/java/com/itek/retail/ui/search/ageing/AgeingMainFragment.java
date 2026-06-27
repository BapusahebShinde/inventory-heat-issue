package com.itek.retail.ui.search.ageing;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.AgeingListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.ZoneDao;
import com.itek.retail.databinding.FragmentAgeingMainBinding;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The Ageing main fragment.
 */
public class AgeingMainFragment extends CommonFragment{
  
  public FragmentAgeingMainBinding binding;
  ZoneDao zoneDao;
  ProductDao productDao;
  private List<String> listLocations = new ArrayList<>(0);
  private List<String> listBrands = new ArrayList<>(0);
  private List<String> listCategories = new ArrayList<>(0);
  private AgeingMainViewModel mViewModel;
  private String ageingBucket = "";
  private String selZone = "";
  private String selBrand = "";
  private String selCategory = "";
  private String sortByValues = "";
  private List<MultiQtyModel> listAgeingProducts = new ArrayList<>(0);
  
  /**
   * Instantiates a new Ageing main fragment.
   */
  public AgeingMainFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    zoneDao = AppDatabase.getZoneDao(context);
    productDao = AppDatabase.getProductDao(context);
    context.copyDataBase();
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(AgeingMainViewModel.class);
    binding = FragmentAgeingMainBinding.inflate(inflater, container, false);
    
    ageingBucket = chkNull(getArguments().getString(AppConstants.AGEING_BUCKET), chkNull(getArguments().getString(ParamConstants.AGEING_BUCKET), ""));
    
    setHeader();
    
    binding.listAgeinglist.setAdapter(new AgeingListAdapter((MainActivity) context, AgeingMainFragment.this, null, listAgeingProducts));
    binding.listAgeinglist.setLayoutManager(new LinearLayoutManager(context));
    
    binding.spinLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){/*Don't handle (Default Overridden Empty Method)*/}
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
      public void onNothingSelected(AdapterView<?> adapterView){/*Empty Method (Default Overridden)*/}
    });
    
    binding.header.imgConfigSync.setVisibility(View.VISIBLE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_sync);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        callAPI();
      }
    });
    
    if(savedInstanceState == null && productDao.getAllTotal() <= 0) callAPI();
    
    updateLists();
    return binding.getRoot();
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    try{
      JSONObject requestParams = new JSONObject();
      requestParams.put(ParamConstants.AGEING_BUCKET, ageingBucket);
      callWebService(URLConstants.GET_AGEING_LIST, requestParams, getString(R.string.progress_msg_getting_data), false, true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Get sel dest zone string.
   *
   * @return the string
   */
  public String getSelZone(){ return chkNull(binding.spinLocation.getSelectedItem(), AppConstants.ALL); }
  
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
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack() && (productDao == null || productDao.getAllTotal() <= 0)){
      callAPI();
    }
  }
  
  @Override
  public void onBackPressed(){
    productDao.deleteAllExcept();
    AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.SEARCH_AGEING.getValue());
    super.onBackPressed();
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    final String selZone = binding.spinLocation.getSelectedItem();
    final Set<String> selZones = binding.spinLocation.getSelectedVals();
    final String selBrand = binding.spinBrand.getSelectedItem();
    final Set<String> selBrands = binding.spinBrand.getSelectedVals();
    final String selCategory = binding.spinCategory.getSelectedItem();
    final Set<String> selCategories = binding.spinCategory.getSelectedVals();
    listAgeingProducts.clear();
    listAgeingProducts.addAll(productDao.searchShortageProducts(selZone, selCategory, selBrand, sortByValues));
    ((RecyclerView.Adapter) binding.listAgeinglist.getAdapter()).notifyDataSetChanged();
    
  }
  
  /**
   * Set header.
   */
  public void setHeader(){
    binding.llListHeader.txtBrand.setText(SharedPrefManager.getString(ParamConstants.LABEL_BRANDS,context.getString(R.string.lbl_brand)));
    final LinearLayout llHeader = binding.llListHeader.llAgeing;
    final int childCount = llHeader.getChildCount();
    if(childCount > 0) for(int i = 0; i < childCount; i++){
      final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
      if(sortView != null){
        sortView.setOnClickListener(new View.OnClickListener(){
          @Override
          public void onClick(View view){
            if(llHeader.getVisibility() != View.VISIBLE) return;
            final SortHeaderView sortView = view != null && view instanceof SortHeaderView ? (SortHeaderView) view : null;
            if(sortView != null){
              resetHeader(sortView.getId());
              setSortBy(sortView.getSortColumn(), sortView.getSortOrder());
            }
          }
        });
      }
    }
  }
  
  /**
   * Reset header.
   *
   * @param viewId the view id
   */
  public void resetHeader(@IdRes final int viewId){
    final LinearLayout llHeader = binding.llListHeader.llAgeing;
    final int childCount = llHeader.getChildCount();
    if(childCount > 0) for(int i = 0; i < childCount; i++){
      final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
      if(sortView != null){
        if(viewId != 0 && sortView.getId() == viewId) sortView.updateDescOrder();
        else sortView.reset();
      }
    }
  }
  
  /**
   * Set sort by.
   *
   * @param column  the column
   * @param orderBy the order by
   */
  private void setSortBy(String column, String orderBy){
    sortByValues = isNonEmpty(column) && isNonEmpty(orderBy)?column + " " + orderBy:"";
    updateLists();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_AGEING_LIST:
          if(isSuccess && jsonResponse != null){
            JSONArray ageingProductsArray = extractJSONArray(jsonResponse, ParamConstants.AGEING_PRODUCTS);
            
            if(isNonEmpty(ageingProductsArray)){
              productDao.deleteAllExcept();
              AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.SEARCH_AGEING.getValue());
              int insertCount = 0;
              for(int i = 0; i < ageingProductsArray.length(); i++){
                JSONObject replenishmentProduct = ageingProductsArray.getJSONObject(i);
                ProductModel productModel = replenishmentProduct != null ? getGSON().fromJson(replenishmentProduct.toString(), ProductModel.class) : null;
                if(productModel != null){
                  if(isDebugApp && chkNull(productModel.getZone(), AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
                    productModel.setZone(binding.spinLocation.getSelectedItem());
                  productModel.setSessionType(AppCommonMethods.SessionType.SEARCH_AGEING.getValue());
                  productModel.setItemImgUrl(extractString(replenishmentProduct, ParamConstants.IMG_URL, "").replaceAll(AppConstants.IMAGE_URL_REPLACE_REGEX, "").trim());
                  final JSONArray jsonZones = extractJSONArray(replenishmentProduct, ParamConstants.ZONES);
                  if(jsonZones != null && jsonZones.length() > 0){
                    for(int j = 0; j < jsonZones.length(); j++){
                      JSONObject zone = jsonZones.getJSONObject(j);
                      final String zoneId = extractString(zone, ParamConstants.ZONE_ID, "0");
                      final Integer eanQty = extractInt(zone, ParamConstants.EAN_QTY, extractInt(zone, ParamConstants.QTY, 0));
                      final String zoneName = extractString(zone, ParamConstants.ZONE_NAME, extractString(zone, ParamConstants.ZONE, Integer.parseInt(chkZero(zoneId, "0")) > 0 && chkNull(eanQty, 0) > 0 ? AppDatabase.getZoneDao(context).getZoneNameById(zoneId) : ""));
                      if(isNonEmpty(zoneName) && chkNull(eanQty, 0) > 0){
                        productModel.setZone(zoneName);
                        productModel.setZoneId(zoneId);
                        productModel.setEanQty(eanQty);
                        productDao.insert(productModel);
                        insertCount++;
                      }
                    }
                  }
                  else{
                    productDao.insert(productModel);
                    insertCount++;
                  }
                }
              }
              if(insertCount > 0){
                if(listBrands.size() <= 1){
                  listBrands.clear();
                  listBrands.add(0, AppConstants.ALL);
                  final List<String> brands = productDao.getBrandList();
                  if(isNonEmpty(brands)) listBrands.addAll(brands);
                  binding.spinBrand.setAdapter(listBrands);
                }
                if(listCategories.size() <= 1){
                  listCategories.clear();
                  listCategories.add(0, AppConstants.ALL);
                  final List<String> categories = productDao.getCategoryList();
                  if(isNonEmpty(categories)) listCategories.addAll(categories);
                  binding.spinCategory.setAdapter(listCategories);
                }
                if(listLocations.size() <= 1){
                  listLocations.clear();
                  listLocations.add(0, AppConstants.ALL);
                  final List<String> locations = productDao.getLocationList();
                  if(isNonEmpty(locations)) listLocations.addAll(locations);
                  binding.spinLocation.setAdapter(listLocations);
                }
                selCategory = extractString(jsonRequest, ParamConstants.CATEGORY);
                selBrand = extractString(jsonRequest, ParamConstants.BRAND);
                selZone = extractString(jsonRequest, ParamConstants.SOURCE_ZONE);
              }
              hideProgressDialog();
            }
            else hideProgressDialog();
          }
          else{
            if(isNonEmpty(selZone) && listLocations.indexOf(selZone.split(",")[0].trim()) >= 0)
              binding.spinLocation.setSelection(selZone);
            if(isNonEmpty(selBrand) && listBrands.indexOf(selBrand.split(",")[0].trim()) >= 0)
              binding.spinBrand.setSelection(selBrand);
            if(isNonEmpty(selCategory) && listCategories.indexOf(selCategory.split(",")[0].trim()) >= 0)
              binding.spinCategory.setSelection(selCategory);
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