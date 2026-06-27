package com.itek.retail.ui.search.omnichannel;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.itek.retail.R;
import com.itek.retail.adapter.OmniChannelOrderListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.databinding.FragmentOmnichannelListBinding;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Omni channel list fragment.
 */
public class OmniChannelListFragment extends CommonFragment{
  
  ProductDao productDao;
  InventoryDao inventoryDao;
  List<ProductModel> listOmniProducts = new ArrayList<>(0);
  private List<String> listBrands = new ArrayList<>(0);
  private List<String> listCategories = new ArrayList<>(0);
  private OmniChannelListViewModel mViewModel;
  private FragmentOmnichannelListBinding binding;
  private String selBrand = "";
  private String selCategory = "";
  private boolean isAllowDecode = false;
  private boolean isAllowDecodeOnPick = false;
  private boolean isAllowDecodeWithoutVerify = false;
  private boolean isEANSearch = false;
  private String omniType = AppConstants.OMNI_TYPE_ORDER;
  private String omniUploadType = AppConstants.OMNI_UPLOAD_TYPE_COMPLETE;
  private ProductModel selProd = null;
  private String sortByValues = "";
  
  /**
   * Instantiates a new Omni channel list fragment.
   */
  public OmniChannelListFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OmniChannelListViewModel.class);
    binding = FragmentOmnichannelListBinding.inflate(inflater, container, false);
    omniType = extractString(getArguments(), AppConstants.OMNICHANNEL_TYPE, chkNull(SharedPrefManager.getOmnichannelType(), AppConstants.OMNI_TYPE_ORDER));
    isEANSearch = omniType.equalsIgnoreCase(AppConstants.OMNI_TYPE_EAN);
    //binding.edtOmnichannelSearch.setHint(getString(isEANSearch ? R.string.hint_search_by_ean : R.string.hint_search_by_order_no));
    binding.edtOmnichannelSearch.setHint(String.format(getString(R.string.hint_search_by__),SharedPrefManager.getString(isEANSearch?ParamConstants.LABEL_EANS:ParamConstants.LABEL_ORDER,getString(isEANSearch ? R.string.lbl_ean : R.string.lbl_order))));

    binding.rbOrder.setSelected(true);
    binding.rbEan.setSelected(true);
    binding.rbOrder.setTag(AppConstants.OMNI_TYPE_ORDER);
    binding.rbEan.setTag(AppConstants.OMNI_TYPE_EAN);
    binding.rbOrder.setText(SharedPrefManager.getString(ParamConstants.LABEL_ORDER,context.getString(R.string.lbl_order)));
    binding.rbEan.setText(SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean)));
    
    binding.rgOmniChannelType.setVisibility(SharedPrefManager.getOmnichannelType().equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH) ? View.VISIBLE : View.GONE);
    binding.rbEan.setChecked(isEANSearch);
    
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
    
    binding.edtOmnichannelSearch.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count){/*Empty Method (Default Overridden)*/}
      
      @Override
      public void afterTextChanged(Editable s){ updateLists(); }
    });
    
    setHeader();
    
    binding.llListHeader.txtOrderNo.setText(SharedPrefManager.getString(isEANSearch?ParamConstants.LABEL_EANS:ParamConstants.LABEL_ORDER,getString(isEANSearch ? R.string.lbl_ean : R.string.lbl_order)));
    binding.llOmniBrandCategory.setVisibility(isEANSearch ? View.VISIBLE : View.GONE);
    binding.listOmnichannelSearch.setAdapter(new OmniChannelOrderListAdapter((MainActivity) context, OmniChannelListFragment.this, listOmniProducts));
    binding.listOmnichannelSearch.setLayoutManager(new LinearLayoutManager(context));
    
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
    
    binding.rgOmniChannelType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        final String omniType = binding.rgOmniChannelType.findViewById(binding.rgOmniChannelType.getCheckedRadioButtonId()).getTag().toString().toLowerCase().trim();
        OmniChannelListFragment.this.omniType = omniType;
        isEANSearch = omniType.equalsIgnoreCase(AppConstants.OMNI_TYPE_EAN);
        //binding.edtOmnichannelSearch.setHint(getString(isEANSearch ? R.string.hint_search_by_ean : R.string.hint_search_by_order_no));
        binding.edtOmnichannelSearch.setHint(String.format(getString(R.string.hint_search_by__),SharedPrefManager.getString(isEANSearch?ParamConstants.LABEL_EANS:ParamConstants.LABEL_ORDER,getString(isEANSearch ? R.string.lbl_ean : R.string.lbl_order))));
        binding.llListHeader.txtOrderNo.setText(SharedPrefManager.getString(isEANSearch?ParamConstants.LABEL_EANS:ParamConstants.LABEL_ORDER,context.getString(isEANSearch ? R.string.lbl_ean : R.string.lbl_order)));
        binding.llOmniBrandCategory.setVisibility(isEANSearch ? View.VISIBLE : View.GONE);
        listOmniProducts.clear();
        productDao.deleteAllExcept();
        if(binding != null && binding.listOmnichannelSearch != null && binding.listOmnichannelSearch.getAdapter() != null && binding.listOmnichannelSearch.getAdapter() instanceof RecyclerView.Adapter)
          ((RecyclerView.Adapter) binding.listOmnichannelSearch.getAdapter()).notifyDataSetChanged();
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
      jsonRequest.put(ParamConstants.OMNICHANNEL_TYPE, chkNull(omniType, chkNull(SharedPrefManager.getOmnichannelType(), AppConstants.OMNI_TYPE_EAN)));
      callWebService(URLConstants.GET_OMNICHANNEL_LIST, jsonRequest, getString(R.string.progress_msg_getting_data), false, true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  public void callDetailsAPI(final ProductModel productModel){
    selProd = productModel;
    try{
      Bundle args = getArguments();
      args.putSerializable(selProd.getClass().getSimpleName(), selProd);
      args.putBoolean(AppConstants.IS_EAN_SEARCH, isEANSearch());
      args.putBoolean(AppConstants.IS_ALLOW_DECODE, isAllowDecode());
      args.putBoolean(AppConstants.IS_ALLOW_DECODE_ON_PICK, isAllowDecodeOnPick());
      args.putBoolean(AppConstants.IS_ALLOW_DECODE_WITHOUT_VERIFY, isAllowDecodeWithoutVerify());
      args.putString(AppConstants.HEADER_ORDER_NO_EAN, String.format(context.getString(R.string.txt__no),SharedPrefManager.getString(isEANSearch()?ParamConstants.LABEL_EANS:ParamConstants.LABEL_ORDER,context.getString(isEANSearch() ? R.string.lbl_ean : R.string.lbl_order)).replaceFirst("/", ""), isEANSearch() ? productModel.getEan() : productModel.getOrderNo()));
      args.putSerializable(AppConstants.OMNICHANNEL_TYPE, getOmnichannelType());
      args.putSerializable(AppConstants.OMNICHANNEL_UPLOAD_TYPE, getOmnichannelUploadType());
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.ORDER_NO, productModel.getOrderNo());
      if(isEANSearch) jsonRequest.put(ParamConstants.EAN, isEANSearch ? productModel.getEan() : "");
      jsonRequest.put(ParamConstants.OMNICHANNEL_TYPE, getOmnichannelType());
      callWebService(URLConstants.GET_OMNICHANNEL_LIST_DETAILS, jsonRequest, args, getString(R.string.progress_msg_getting_data), false, true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Is allow decode boolean.
   *
   * @return the boolean
   */
  public boolean isAllowDecode(){ return isAllowDecode; }
  
  public boolean isAllowDecodeOnPick(){ return isAllowDecodeOnPick; }
  
  public boolean isAllowDecodeWithoutVerify(){ return isAllowDecodeWithoutVerify; }
  
  /**
   * Is ean search boolean.
   *
   * @return the boolean
   */
  public boolean isEANSearch(){ return isEANSearch; }
  
  /**
   * Get omnichannel type string.
   *
   * @return the string
   */
  public String getOmnichannelType(){
    return !chkNull(SharedPrefManager.getOmnichannelType(), AppConstants.OMNI_TYPE_BOTH).equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH) ? SharedPrefManager.getOmnichannelType() : omniType.toLowerCase().trim();
  }
  
  /**
   * Get omnichannel upload type string.
   *
   * @return the string
   */
  public String getOmnichannelUploadType(){ return omniUploadType; }
  
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
   * Set header.
   */
  public void setHeader(){
    final LinearLayout llHeader = binding.llListHeader.llOmniOrder;
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
    final LinearLayout llHeader = binding.llListHeader.llOmniOrder;
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
  public void onResume(){
    super.onResume();
    if(isTopInStack()){
      callAPI();
    }
  }
  
  @Override
  public void onBackPressed(){
    productDao.deleteAllExcept();
    AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.OMNICHANNEL.getValue());
    super.onBackPressed();
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    if(isEANSearch){
      final String selBrand = binding.spinBrand.getSelectedItem();
      final String selCategory = binding.spinCategory.getSelectedItem();
      final String searchName = chkNull(binding.edtOmnichannelSearch.getText().toString(), "").trim();
      listOmniProducts.clear();
      listOmniProducts.addAll(productDao.searchOmniHeader(selCategory, selBrand, searchName, sortByValues));
    }
    else if(!isEANSearch){
      //handle header
      final String searchName = chkNull(binding.edtOmnichannelSearch.getText().toString(), "").trim();
      listOmniProducts.clear();
      listOmniProducts.addAll(productDao.searchOmniHeader(searchName, sortByValues));
    }
    if(binding != null && binding.listOmnichannelSearch != null && binding.listOmnichannelSearch.getAdapter() != null && binding.listOmnichannelSearch.getAdapter() instanceof RecyclerView.Adapter)
      ((RecyclerView.Adapter) binding.listOmnichannelSearch.getAdapter()).notifyDataSetChanged();
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
    selCategory = extractString(jsonRequest, ParamConstants.CATEGORY);
    selBrand = extractString(jsonRequest, ParamConstants.BRAND);
    updateLists();
  }
  
  public void handleOmniDetailsRedirection(final Bundle args, final boolean hasPickData){
    args.putBoolean(AppConstants.HAS_PICK_DATA, hasPickData);
    context.loadFragment(new OmniChannelListDetailsFragment(), args);
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_OMNICHANNEL_LIST:
          if(isSuccess){
            isAllowDecode = extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE, false);
            isAllowDecodeOnPick = isAllowDecode && extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE_ON_PICK, false);
            isAllowDecodeWithoutVerify = isAllowDecode && extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE_WITHOUT_VERIFY, false);
            omniUploadType = extractString(jsonResponse, ParamConstants.OMNI_UPLOAD_TYPE, AppConstants.OMNI_UPLOAD_TYPE_COMPLETE);
            if(isAllowDecode) context.saveTagWritePasswords(jsonResponse);
            
            args = (args == null) ? new Bundle() : args;
            args.putBoolean(AppConstants.IS_ALLOW_DECODE, isAllowDecode);
            args.putBoolean(AppConstants.IS_ALLOW_DECODE_ON_PICK, isAllowDecodeOnPick);
            args.putBoolean(AppConstants.IS_ALLOW_DECODE_WITHOUT_VERIFY, isAllowDecodeWithoutVerify);
            
            productDao.deleteAllExcept();
            JSONArray omniProductsArray = extractJSONArray(jsonResponse, ParamConstants.OMNICHANNEL_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            if(omniProductsArray != null && context instanceof MainActivity)
              ((MainActivity) context).callInsertProductDBTask(this, url, AppCommonMethods.SessionType.OMNICHANNEL.getValue(), jsonRequest, jsonResponse, args, omniProductsArray);
            /*else if(isNonEmpty(omniProductsArray)){
              AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.OMNICHANNEL.getValue());
              int insertCount = 0;
              for(int i = 0; i < omniProductsArray.length(); i++){
                JSONObject omniProduct = omniProductsArray.getJSONObject(i);
                ProductModel productModel = omniProduct != null ? getGSON().fromJson(omniProduct.toString(), ProductModel.class) : null;
                if(productModel != null){
                  productModel.setSessionType(AppCommonMethods.SessionType.OMNICHANNEL.getValue());
                  productModel.setItemImgUrl(extractString(omniProduct, ParamConstants.IMG_URL, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim());
                  final JSONArray jsonZones = extractJSONArray(omniProduct, ParamConstants.ZONES);
                  if(isNonEmpty(jsonZones)){
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
                        final JSONArray jsonTags = extractJSONArray(zone, ParamConstants.PICKED_EPCS);
                        if(jsonTags != null && jsonTags.length() > 0){
                          String epc = "";
                          int foundQty = 0;
                          int decodedQty = 0;
                          for(int k = 0; k < jsonTags.length(); k++){
                            final JSONObject jsonTag = jsonTags.getJSONObject(k);
                            if(jsonTag != null){
                              final boolean isDecoded = extractBoolean(jsonTag, ParamConstants.IS_DECODED, false);
                              decodedQty += isDecoded ? 1 : 0;
                              foundQty += 1;
                              epc += (isNonEmpty(epc) ? "," : "") + extractString(jsonTag, ParamConstants.EPC);
                            }
                          }
                          productModel.setFoundQty(foundQty);
                          productModel.setFound(foundQty > 0);
                          productModel.setDecodedQty(decodedQty);
                          productModel.setDecoded(decodedQty > 0);
                          productModel.setEpc(epc);
                        }
                        productDao.insert(productModel);
                        totalQty += eanQty;
                        insertCount++;
                      }
                    }
                    if(insertCount > 0 && totalQty > 0)
                      productDao.updateTotalQty(productModel.ean, totalQty, productModel.getSessionType());
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
                selCategory = extractString(jsonRequest, ParamConstants.CATEGORY);
                selBrand = extractString(jsonRequest, ParamConstants.BRAND);
              }
              hideProgressDialog();
            }*/
            else hideProgressDialog();
          }
          else{
            if(isNonEmpty(selBrand) && listBrands.indexOf(selBrand.split(",")[0].trim()) >= 0)
              binding.spinBrand.setSelection(selBrand);
            if(isNonEmpty(selCategory) && listCategories.indexOf(selCategory.split(",")[0].trim()) >= 0)
              binding.spinCategory.setSelection(selCategory);
          }
          updateLists();
          break;
        case URLConstants.GET_OMNICHANNEL_LIST_DETAILS:
          if(isSuccess){
            isAllowDecode = extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE, isAllowDecode);
            isAllowDecodeOnPick = isAllowDecode && extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE_ON_PICK, AppCommonMethods.isAllowDecodeOnPick);
            isAllowDecodeWithoutVerify = isAllowDecode && extractBoolean(jsonResponse, ParamConstants.IS_ALLOW_DECODE_WITHOUT_VERIFY, false);
            omniUploadType = extractString(jsonResponse, ParamConstants.OMNI_UPLOAD_TYPE, omniUploadType);
            args.putBoolean(AppConstants.IS_ALLOW_DECODE, isAllowDecode);
            args.putBoolean(AppConstants.IS_ALLOW_DECODE_ON_PICK, isAllowDecodeOnPick);
            args.putBoolean(AppConstants.IS_ALLOW_DECODE_WITHOUT_VERIFY, isAllowDecodeWithoutVerify);
            args.putSerializable(AppConstants.OMNICHANNEL_UPLOAD_TYPE, omniUploadType);
            if(isAllowDecode) context.saveTagWritePasswords(jsonResponse);
            
            final JSONArray omniProductsArray = extractJSONArray(jsonResponse, ParamConstants.OMNICHANNEL_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS));
            if(omniProductsArray != null){
              if(selProd != null){
                if(isEANSearch) productDao.deleteEan(selProd.getEan());
                else productDao.deleteOrder(selProd.getOrderNo());
              }
              AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.OMNICHANNEL.getValue());
              ((MainActivity) context).callInsertProductDBTask(this, url, AppCommonMethods.SessionType.OMNICHANNEL.getValue(), jsonRequest, jsonResponse, args, omniProductsArray);
            }
            /*else if(isNonEmpty(omniProductsArray)){
              if(selProd != null){
                if(isEANSearch) productDao.deleteEan(selProd.getEan());
                else productDao.deleteOrder(selProd.getOrderNo());
              }
              AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.OMNICHANNEL.getValue());
              int insertCount = 0;
              boolean hasPickData = false;
              for(int i = 0; i < omniProductsArray.length(); i++){
                final JSONObject omniProduct = omniProductsArray.getJSONObject(i);
                final ProductModel productModel = omniProduct != null ? getGSON().fromJson(omniProduct.toString(), ProductModel.class) : null;
                if(productModel != null && (selProd == null || (!isEANSearch && selProd.getOrderNo().equalsIgnoreCase(productModel.getOrderNo())) || (isEANSearch && selProd.getEan().equalsIgnoreCase(productModel.getEan())))){
                  productModel.setSessionType(AppCommonMethods.SessionType.OMNICHANNEL.getValue());
                  productModel.setItemImgUrl(extractString(omniProduct, ParamConstants.IMG_URL, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").trim());
                  final JSONArray jsonZones = extractJSONArray(omniProduct, ParamConstants.ZONES);
                  if(selProd != null){
                    if(isNullOrEmpty(productModel.getOrderNo()))
                      productModel.setOrderNo(selProd.getOrderNo());
                    if(isNullOrEmpty(productModel.getAgeingLabel()))
                      productModel.setAgeingLabel(selProd.getAgeingLabel());
                    if(productModel.getAgeingHrs() <= 0)
                      productModel.setAgeingHrs(selProd.getAgeingHrs());
                    if(productModel.getPriority() <= 0)
                      productModel.setPriority(selProd.getPriority());
                    if(isNullOrEmpty(productModel.getStatus()))
                      productModel.setStatus(selProd.getStatus());
                    if(isEANSearch){
                      if(productModel.getQty() <= 0) productModel.setQty(selProd.getQty());
                      if(isNullOrEmpty(productModel.getEan()))
                        productModel.setEan(selProd.getEan());
                      if(isNullOrEmpty(productModel.getBrand()))
                        productModel.setBrand(selProd.getBrand());
                      if(isNullOrEmpty(productModel.getCategory()))
                        productModel.setCategory(selProd.getCategory());
                    }
                  }
                  if(jsonZones != null && jsonZones.length() > 0){
                    int totalQty = 0;
                    for(int j = 0; j < jsonZones.length(); j++){
                      JSONObject zone = jsonZones.getJSONObject(j);
                      final String zoneId = extractString(zone, ParamConstants.ZONE_ID, "0");
                      final Integer eanQty = extractInt(zone, ParamConstants.EAN_QTY, extractInt(zone, ParamConstants.QTY, 0));
                      final String zoneName = extractString(zone, ParamConstants.ZONE_NAME, extractString(zone, ParamConstants.ZONE, Integer.parseInt(chkZero(zoneId,"0"))>0 && chkNull(eanQty, 0) > 0?AppDatabase.getZoneDao(context).getZoneNameById(zoneId):""));
                      if(isNonEmpty(zoneName) && chkNull(eanQty, 0) > 0){
                        productModel.setZone(zoneName);
                        productModel.setZoneId(zoneId);
                        productModel.setEanQty(eanQty);
                        productModel.setPickedEPCs(extractString(zone, ParamConstants.PICKED_EPCS));
                        final JSONArray jsonTags = extractJSONArray(zone, ParamConstants.PICKED_EPCS);
                        if(jsonTags != null && jsonTags.length() > 0){
                          String foundEpc = "";
                          String decodedEpc = "";
                          int foundQty = 0;
                          int decodedQty = 0;
                          for(int k = 0; k < jsonTags.length(); k++){
                            final JSONObject jsonTag = jsonTags.getJSONObject(k);
                            if(jsonTag != null){
                              final boolean isDecoded = extractBoolean(jsonTag, ParamConstants.IS_DECODED, false);
                              final String epc = extractString(jsonTag, ParamConstants.EPC, "");
                              decodedQty += isDecoded ? 1 : 0;
                              decodedEpc += isDecoded ? (isNonEmpty(decodedEpc) ? "," : "") + epc : "";
                              foundQty += 1;
                              foundEpc += (isNonEmpty(foundEpc) ? "," : "") + epc;
                            }
                          }
                          productModel.setFoundQty(foundQty);
                          productModel.setFound(foundQty > 0);
                          productModel.setDecodedQty(decodedQty);
                          productModel.setDecoded(decodedQty > 0);
                          if(decodedQty > 0) productModel.setStatus(AppConstants.STATUS_VERIFIED);
                          productModel.setEpc(foundEpc);
                          if(!hasPickData) hasPickData = true;
                        }
                        else{
                          productModel.setFoundQty(0);
                          productModel.setFound(false);
                          productModel.setDecodedQty(0);
                          productModel.setDecoded(false);
                          productModel.setEpc(null);
                        }
                        //temp solution
                        if(j > 0) productModel.setQty(0);
                        productDao.insert(productModel);
                        totalQty += eanQty;
                        insertCount++;
                      }
                    }
                    if(insertCount > 0 && totalQty > 0)
                      productDao.updateTotalQty(productModel.ean, totalQty, productModel.getSessionType());
                  }
                  else{
                    productModel.setTotalQty(productModel.getEanQty());
                    productDao.insert(productModel);
                    insertCount++;
                  }
                }
              }
              if(insertCount > 0){
                args.putBoolean(AppConstants.HAS_PICK_DATA, hasPickData);
                context.loadFragment(new OmniChannelListDetailsFragment(), args);
              }
              hideProgressDialog();
            }*/
            else hideProgressDialog();
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