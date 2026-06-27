package com.itek.retail.ui.movement.replenishment;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.getSampleJSON;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.itek.retail.database.ZoneDao;
import com.itek.retail.databinding.FragmentReplenishmentListBinding;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The Replenishment list fragment.
 */
public class ReplenishmentListFragment extends CommonFragment{
  
  public FragmentReplenishmentListBinding binding;
  ZoneDao zoneDao;
  ProductDao productDao;
  private List<String> listSrcLocations = new ArrayList<>(0);
  private List<String> listDestLocations = new ArrayList<>(0);
  private List<String> listSrcZones = new ArrayList<>(0);
  private List<String> listDestZones = new ArrayList<>(0);
  private List<String> listBrands = new ArrayList<>(0);
  private List<String> listCategories = new ArrayList<>(0);
  private List<ProductModel> listReplenishProducts = new ArrayList<>(0);
  private ReplenishmentListViewModel mViewModel;
  private String selSrcZone = "";
  private String selDestZone = "";
  private String selBrand = "";
  private String selCategory = "";
  
  /**
   * Instantiates a new Replenishment list fragment.
   */
  public ReplenishmentListFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    zoneDao = AppDatabase.getZoneDao(context);
    productDao = AppDatabase.getProductDao(context);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ReplenishmentListViewModel.class);
    binding = FragmentReplenishmentListBinding.inflate(inflater, container, false);
    binding.rbStatic.setSelected(true);
    binding.rbDynamic.setSelected(true);
    binding.rbStatic.setTag(AppConstants.REPLENISH_TYPE_STATIC);
    binding.rbDynamic.setTag(AppConstants.REPLENISH_TYPE_DYNAMIC);
    
    listSrcLocations = zoneDao.getBOHZoneNames();
    listDestLocations = zoneDao.getFOHZoneNames(); //TODO get from API itself
    
    if(isNullOrEmpty(listSrcLocations) || isNullOrEmpty(listDestLocations)){
      context.showCustomErrDialog(getString(R.string.err_no_zones_replenishment), true);
      return binding.getRoot();
    }
    
    listDestZones.clear();
    listDestZones.add(0, AppConstants.ALL);
    
    listSrcZones.clear();
    listSrcZones.add(0, AppConstants.ALL);
    
    listBrands.clear();
    listBrands.add(AppConstants.ALL);
    List<String> brands = productDao.getBrandList();
    if(isNonEmpty(brands)) listBrands.addAll(brands);
    
    listCategories.clear();
    listCategories.add(AppConstants.ALL);
    List<String> categories = productDao.getCategoryList();
    if(isNonEmpty(categories)) listCategories.addAll(categories);
    
    binding.spinSourceZone.setAdapter(listSrcLocations,0);
    binding.spinDestinationZone.setAdapter(listDestLocations,0);
    binding.spinBrand.setAdapter(listBrands);
    binding.spinCategory.setAdapter(listCategories);
    binding.listReplenishmentProducts.setAdapter(new ProductListAdapter((MainActivity) context, ReplenishmentListFragment.this, listReplenishProducts));//listReplenishProducts));
    binding.listReplenishmentProducts.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
    
    
    binding.rgReplenishmentType.setVisibility(chkNull(SharedPrefManager.getReplenishmentType(), AppConstants.REPLENISH_TYPE_BOTH).equalsIgnoreCase(AppConstants.REPLENISH_TYPE_BOTH) ? View.VISIBLE : View.GONE);
    binding.rbStatic.setChecked((extractString(getArguments(), AppConstants.REPLENISHMENT_TYPE, chkNull(SharedPrefManager.getReplenishmentType(), AppConstants.REPLENISH_TYPE_BOTH)).equalsIgnoreCase(binding.rbStatic.getTag().toString())));
    
    binding.rgReplenishmentType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        productDao.deleteAllExcept();
        listReplenishProducts.clear();
        listReplenishProducts.clear();
        ((RecyclerView.Adapter) binding.listReplenishmentProducts.getAdapter()).notifyDataSetChanged();
        callAPI();
      }
    });

    binding.edtReplenishmentNameSearch.setHint(String.format(context.getString(R.string.hint_search_by__),SharedPrefManager.getString(ParamConstants.LABEL_NAME,context.getString(R.string.lbl_name))+"/"+SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean))));
    binding.edtReplenishmentNameSearch.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void afterTextChanged(Editable editable){ updateLists(); }
    });
    
    binding.edtReplenishmentNameSearch.setOnEditorActionListener(new TextView.OnEditorActionListener(){
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent){
        if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
          hideKeyboard();
          return true;
        }
        return false;
      }
    });
    
    binding.spinSourceZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
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
      public void onNothingSelected(AdapterView<?> adapterView){
        /*Empty Method (Default Overridden)*/
      }
    });
    
    binding.spinDestinationZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        showLog("ReplenishList_DestinationZone", "OnItemSelected");
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){
        /*Empty Method (Default Overridden)*/
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
    
    if(savedInstanceState == null && productDao.getAllTotal() <= 0) callAPI();
    updateLists();
    return binding.getRoot();
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    try{
      JSONObject jsonRequest = new JSONObject();
      List<Zone> srcZones = zoneDao.getZoneByName(binding.spinSourceZone.getSelectedItem());
      final Zone selSourceZone = isNonEmpty(srcZones) ? srcZones.get(0) : null;
      List<Zone> srcDestZones = zoneDao.getZoneByName(binding.spinDestinationZone.getSelectedItem());
      final Zone selDestinationZone = isNonEmpty(srcDestZones) ? srcDestZones.get(0) : null;
      if(isNullOrEmpty(ReplenishmentListFragment.this.selSrcZone) && selSourceZone != null)
        ReplenishmentListFragment.this.selSrcZone = selSourceZone.getZoneName();
      if(isNullOrEmpty(ReplenishmentListFragment.this.selDestZone) && selDestinationZone != null)
        ReplenishmentListFragment.this.selDestZone = selDestinationZone.getZoneName();
      if(isNullOrEmpty(ReplenishmentListFragment.this.selCategory) && isNonEmpty(selCategory))
        ReplenishmentListFragment.this.selCategory = selCategory;
      if(isNullOrEmpty(ReplenishmentListFragment.this.selBrand) && isNonEmpty(selBrand))
        ReplenishmentListFragment.this.selBrand = selBrand;
      jsonRequest.put(ParamConstants.REPLENISHMENT_TYPE, getReplenishmentType());
      callWebService(URLConstants.GET_REPLENISHMENT_LIST, jsonRequest, getString(R.string.progress_msg_getting_data), false, true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Get sel dest zone string.
   *
   * @return the string
   */
  public String getSelDestZone(){ return chkNull(binding.spinDestinationZone.getSelectedItem(), AppConstants.ALL); }
  
  /**
   * Get sel src zone string.
   *
   * @return the string
   */
  public String getSelSrcZone(){ return chkNull(binding.spinSourceZone.getSelectedItem(), AppConstants.ALL); }
  
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
   * Get replenishment type string.
   *
   * @return the string
   */
  public String getReplenishmentType(){
    return !chkNull(SharedPrefManager.getReplenishmentType(), AppConstants.REPLENISH_TYPE_BOTH).equalsIgnoreCase(AppConstants.REPLENISH_TYPE_BOTH) ? SharedPrefManager.getReplenishmentType() : binding.rgReplenishmentType.findViewById(binding.rgReplenishmentType.getCheckedRadioButtonId()).getTag().toString().toLowerCase().trim();
  }
  
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
    AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.REPLENISHMENT.getValue());
    super.onBackPressed();
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    final String selDestZone = binding.spinDestinationZone.getSelectedItem();
    final Set<String> selDestZones = binding.spinDestinationZone.getSelectedVals();
    final String selSrcZone = binding.spinSourceZone.getSelectedItem();
    final Set<String> selSrcZones = binding.spinSourceZone.getSelectedVals();
    final String selBrand = binding.spinBrand.getSelectedItem();
    final Set<String> selBrands = binding.spinBrand.getSelectedVals();
    final String selCategory = binding.spinCategory.getSelectedItem();
    final Set<String> selCategories = binding.spinCategory.getSelectedVals();
    final String searchName = chkNull(binding.edtReplenishmentNameSearch.getText().toString(), "").trim();
    
    listReplenishProducts.clear();
    listReplenishProducts.addAll(productDao.getReplenishmentProducts(selSrcZone, selSrcZones, selDestZone, selDestZones, selCategory, selCategories, selBrand, selBrands, searchName));
    //Old 2.0 Code
    //listReplenishProducts.addAll(productDao.getReplenishmentProducts(selSrcZone, selSrcZones, selCategory, selCategories, selBrand, selBrands, searchName));
    ((RecyclerView.Adapter) binding.listReplenishmentProducts.getAdapter()).notifyDataSetChanged();
    
    final Integer totalEanQty = productDao.getTotalEANCount(selDestZone, selDestZones, selCategory, selCategories, selBrand, selBrands, searchName);
    
    binding.txtTotal.setText(String.format(getString(R.string.txt_movement_replenishment_total), chkZero(totalEanQty, "0")));
  }
  
  public void updateSelectedValuesOnSuccess(final JSONObject jsonRequest){
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
    if(listDestZones.size() <= 1 || listSrcZones.size() <= 1){
      listDestZones.clear();
      listSrcZones.clear();
      List<String> listSrc = productDao.getLocationList();
      List<String> listDest = productDao.getDestLocationList();
      if(isNonEmpty(listSrc) && isNonEmpty(listDest)){
        listSrcZones.addAll(listSrc);
        listDestZones.addAll(listDest);
      }
      else{
        for(String zone : productDao.getLocationList()){
          if(listSrcLocations.contains(zone)) listSrcZones.add(zone);
          if(listDestLocations.contains(zone)) listDestZones.add(zone);
        }
      }
      if(isNullOrEmpty(listSrcZones)) listSrcZones.addAll(listSrcLocations);
      if(isNonEmpty(listSrcZones)) listSrcZones.add(0, AppConstants.ALL);
      if(isNullOrEmpty(listDestZones)) listDestZones.addAll(listDestLocations);
      
      binding.spinDestinationZone.setAdapter(listDestZones,0);
      binding.spinSourceZone.setAdapter(listSrcZones,0);
    }
    
    selSrcZone = extractString(jsonRequest, ParamConstants.SOURCE_ZONE);
    selDestZone = extractString(jsonRequest, ParamConstants.DESTINATION_ZONE);
    selCategory = extractString(jsonRequest, ParamConstants.CATEGORY);
    selBrand = extractString(jsonRequest, ParamConstants.BRAND);
    updateLists();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_REPLENISHMENT_LIST:
          if(isSuccess && jsonResponse != null){
            JSONArray replenishmentProductsArray = extractJSONArray(jsonResponse, ParamConstants.REPLENISHMENT_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            if(replenishmentProductsArray != null && context instanceof MainActivity)
              ((MainActivity) context).callInsertProductDBTask(this, url, AppCommonMethods.SessionType.REPLENISHMENT.getValue(), jsonRequest, jsonResponse, args, replenishmentProductsArray);
            /*else if(isNonEmpty(replenishmentProductsArray)){
              productDao.deleteAll();
              AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.REPLENISHMENT.getValue());
              int insertCount = 0;
              for(int i = 0; i < replenishmentProductsArray.length(); i++){
                JSONObject replenishmentProduct = replenishmentProductsArray.getJSONObject(i);
                ProductModel productModel = replenishmentProduct != null ? getGSON().fromJson(replenishmentProduct.toString(), ProductModel.class) : null;
                if(productModel != null){
                  if(isDebugApp && chkNull(productModel.getZone(), AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
                    productModel.setZone(binding.spinSourceZone.getSelectedItem());
                  productModel.setSessionType(AppCommonMethods.SessionType.REPLENISHMENT.getValue());
                  productModel.setItemImgUrl(extractString(replenishmentProduct, ParamConstants.IMG_URL, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim());
                  final JSONArray jsonZones = extractJSONArray(replenishmentProduct, ParamConstants.ZONES);
                  if(jsonZones != null && jsonZones.length() > 0){
                    for(int j = 0; j < jsonZones.length(); j++){
                      JSONObject zone = jsonZones.getJSONObject(j);
                      final String zoneName = extractString(zone, ParamConstants.ZONE_NAME, "");
                      final String zoneId = extractString(zone, ParamConstants.ZONE_ID, "0");
                      final Integer eanQty = extractInt(zone, ParamConstants.EAN_QTY, 0);
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
                selSrcZone = extractString(jsonRequest, ParamConstants.SOURCE_ZONE);
                selDestZone = extractString(jsonRequest, ParamConstants.DESTINATION_ZONE);
                selCategory = extractString(jsonRequest, ParamConstants.CATEGORY);
                selBrand = extractString(jsonRequest, ParamConstants.BRAND);
              }
              hideProgressDialog();
            }*/
            else hideProgressDialog();
          }
          else{
            if(isNonEmpty(selSrcZone) && listSrcLocations.indexOf(selSrcZone.split(",")[0].trim()) >= 0)
              binding.spinSourceZone.setSelection(selSrcZone);
            if(isNonEmpty(selDestZone) && listDestLocations.indexOf(selDestZone.split(",")[0].trim()) >= 0)
              binding.spinDestinationZone.setSelection(selDestZone);
            if(isNonEmpty(selBrand) && listBrands.indexOf(selBrand.split(",")[0].trim()) >= 0)
              binding.spinBrand.setSelection(selBrand);
            if(isNonEmpty(selCategory) && listCategories.indexOf(selCategory.split(",")[0].trim()) >= 0)
              binding.spinCategory.setSelection(selCategory);
          }
          updateLists();
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
              ((MainActivity) context).checkReaderConnection(new ReplenishmentStartFragment(), args);
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}