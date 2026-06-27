package com.itek.retail.ui.inventory.stockcorrection;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.adapter.ShortageListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.databinding.FragmentStockCorrectionMainBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Stock correction main fragment.
 */
public class StockCorrectionMainFragment extends CommonFragment{
  
  private StockCorrectionViewModel mViewModel;
  private FragmentStockCorrectionMainBinding binding;
  private List<MultiQtyModel> listShortageBrands = new ArrayList<>(0);
  private ProductDao productDao;
  private InventoryDao inventoryDao;
  private List<String> listCategories = new ArrayList<>(0);
  private List<String> listBrands = new ArrayList<>(0);
  private List<String> listZones = new ArrayList<>(0);
  private MultiQtyModel totalCounts = null;
  private String sortByValues = "";
  
  /**
   * Instantiates a new Stock correction main fragment.
   */
  public StockCorrectionMainFragment(){
    // Required empty public constructor
  }
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    productDao = AppDatabase.getProductDao(context);
    inventoryDao = AppDatabase.getInventoryDao(context);
    context.copyDataBase();
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(StockCorrectionViewModel.class);
    binding = FragmentStockCorrectionMainBinding.inflate(inflater, container, false);
    setActiveUsers(AppCommonMethods.extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2));
    
    setSpinners();
    
    binding.spinInventoryCorrectionCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> parent){
        /*Empty Method (Default Overridden)*/
      }
    });
    
    binding.spinInventoryCorrectionLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> parent){
        /*Empty Method (Default Overridden)*/
      }
    });
    
    binding.spinInventoryCorrectionBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> parent){
        /*Empty Method (Default Overridden)*/
      }
    });
    
    setHeader();
    
    binding.listInventoryCorrectionShortageAnalysis.setAdapter(new ShortageListAdapter((MainActivity) context, StockCorrectionMainFragment.this, null, listShortageBrands));
    binding.listInventoryCorrectionShortageAnalysis.setLayoutManager(new LinearLayoutManager(context));
    
    binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        final MultiQtyModel multiQtyModel = productDao.getTotalCounts();
        final int found = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
        final int total = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
        if(found > 0 && total > 0){
          context.showCustomConfirmDialog(/*String.format(getString(*/R.string.title_stock_correction_upload/*),found+"/"+total)*/, R.string.btn_upload, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              apiCall(AppConstants.SESSION_ACTION_UPLOAD);
            }
          });
        }
      }
    });
    
    /*binding.btnUpload.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        if(view.getVisibility() != View.VISIBLE) return;
        if(binding.btnSwipeUpload != null && binding.btnSwipeUpload.getVisibility() == View.VISIBLE)
          return;
        final MultiQtyModel multiQtyModel = productDao.getTotalCounts();
        final int found = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
        final int total = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
        if(found > 0 && total > 0){
          
          context.showCustomConfirmDialog(*//*String.format(getString(*//*R.string.title_stock_correction_upload*//*),found+"/"+total)*//*, R.string.btn_upload, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
              apiCall(AppConstants.SESSION_ACTION_UPLOAD);
            }
          });
        }
      }
    });
    
    binding.btnSwipeUpload.isSuccessfulSwipe.observe(getViewLifecycleOwner(), new Observer<Boolean>(){
      @Override
      public void onChanged(Boolean isSuccessfulSwipe){
        if(!isTopInStack()) return;
        boolean isSwiped = chkNotNullTrue(isSuccessfulSwipe);
        if(isSwiped){
          binding.btnSwipeUpload.reset();
          final MultiQtyModel multiQtyModel = productDao.getTotalCounts();
          final int found = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
          final int total = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
          if(found > 0 && total > 0){
            context.showCustomConfirmDialog(*//*String.format(getString(*//*R.string.title_stock_correction_upload*//*),found+"/"+total)*//*, R.string.btn_upload, new DialogInterface.OnClickListener(){
              @Override
              public void onClick(DialogInterface dialogInterface, int i){
                apiCall(AppConstants.SESSION_ACTION_UPLOAD);
              }
            });
            
          }
        }
      }
    });
    
    setUploadConstraints((ConstraintLayout) binding.getRoot(), binding.btnUpload, binding.btnSwipeUpload);*/
    
    updateLists();
    
    return binding.getRoot();
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    if(productDao != null){
      productDao.onProductFound().observe(getViewLifecycleOwner(), new Observer<Integer>(){
        @Override
        public void onChanged(Integer foundProducts){
          if(foundProducts != null && foundProducts > 0){
            updateLists();
          }
        }
      });
    }
  }
  
  @Override
  public void onDetach(){
    ((MainActivity) context).lockDrawer(false);
    super.onDetach();
  }
  
  /**
   * Set spinners.
   */
  void setSpinners(){
    listCategories.clear();
    List<String> listCategory = productDao.getCategoryList();
    if(isNonEmpty(listCategory)) listCategories.addAll(listCategory);
    if(listCategories != null) listCategories.add(0, AppConstants.ALL);
    binding.spinInventoryCorrectionCategory.setAdapter(listCategories);
    
    listBrands.clear();
    List<String> listBrand = productDao.getBrandList();
    if(isNonEmpty(listBrand)) listBrands.addAll(listBrand);
    if(listBrands != null) listBrands.add(0, AppConstants.ALL);
    binding.spinInventoryCorrectionBrand.setAdapter(listBrands);
    
    listZones.clear();
    List<String> listLocations = productDao.getLocationList();
    if(isNonEmpty(listLocations)) listZones.addAll(listLocations);
    if(listZones != null && !listZones.contains(AppConstants.ALL))
      listZones.add(0, AppConstants.ALL);
    binding.spinInventoryCorrectionLocation.setAdapter(listZones);
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    if(binding != null && binding.spinInventoryCorrectionCategory != null && binding.spinInventoryCorrectionLocation != null && binding.listInventoryCorrectionShortageAnalysis != null){
      final String selZone = binding.spinInventoryCorrectionLocation.getSelectedItem();
      final String selCategory = binding.spinInventoryCorrectionCategory.getSelectedItem();
      final String selBrand = binding.spinInventoryCorrectionBrand.getSelectedItem();
      listShortageBrands.clear();
      listShortageBrands.addAll(productDao.searchShortageProducts(selZone, selCategory, selBrand, sortByValues));
      if(binding != null && binding.listInventoryCorrectionShortageAnalysis != null && binding.listInventoryCorrectionShortageAnalysis.getAdapter() != null)
        ((RecyclerView.Adapter) binding.listInventoryCorrectionShortageAnalysis.getAdapter()).notifyDataSetChanged();
      binding.llListHeader.llInventoryShortage.setVisibility(isNonEmpty(listShortageBrands) ? View.VISIBLE : View.INVISIBLE);
      binding.btnUpload.setVisibility(isNonEmpty(listShortageBrands) && productDao.getTotalFoundCount() > 0 ? View.VISIBLE : View.GONE);
      //binding.btnSwipeUpload.setVisibility(AppCommonMethods.isUploadSlider && isNonEmpty(listShortageBrands) && productDao.getTotalFoundCount() > 0 ? View.VISIBLE : View.GONE);
    }
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(!isTopInStack()) return;
    if(productDao != null){
      MultiQtyModel multiQtyModel = productDao.getTotalCounts();
      if(multiQtyModel != null){
        final int found = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
        final int total = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
        if(totalCounts != null && isTopInStack()){
          if(total < chkNull(totalCounts.total, 0)) setSpinners();
          totalCounts = null;
        }
        showLog("Resume", found + "/" + total);
        //find better solution if possible
        new Handler().postDelayed(new Runnable(){
          @Override
          public void run(){
            ((MainActivity) context).lockDrawer(found > 0 && total > 0);
          }
        }, 500);
      }
    }
    updateLists();
  }
  
  /**
   * Get selected zone string.
   *
   * @return the string
   */
  public String getSelectedZone(){
    return binding.spinInventoryCorrectionLocation.getSelectedItem();
  }
  
  /**
   * Get selected zone id string.
   *
   * @return the string
   */
  public String getSelectedZoneId(){
    Object selZoneObj = binding.spinInventoryCorrectionLocation.getSelectedObject();
    return selZoneObj != null && selZoneObj instanceof Zone ? ((Zone) selZoneObj).getZoneId() : "0";
  }
  
  /**
   * Get selected category string.
   *
   * @return the string
   */
  public String getSelectedCategory(){
    return binding.spinInventoryCorrectionCategory.getSelectedItem();
  }
  
  /**
   * Api call.
   *
   * @param action the action
   */
  public void apiCall(String action){
    final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
    if(isInternetConnected(context, false, isUpload)){
      try{
        if(isUpload) showProgressDialog(getString(R.string.progress_msg_check_upload_data));
        allowBtnClick = false;
        JSONObject requestParams = new JSONObject();
        requestParams.put(ParamConstants.SESSION_TYPE, AppCommonMethods.SessionType.STOCK_CORRECTION.name());
        requestParams.put(ParamConstants.TYPE, AppConstants.STOCK_CORRECTION);
        requestParams.put(ParamConstants.ZONE_ID, 0);
        requestParams.put(ParamConstants.ZONE, AppConstants.ALL);
        requestParams.put(ParamConstants.ZONE_TYPE, null);
        requestParams.put(ParamConstants.IS_DEFAULT_ZONE, false);
        requestParams.put(ParamConstants.CATEGORY, AppConstants.ALL);
        requestParams.put(ParamConstants.BRAND, AppConstants.ALL);
        requestParams.put(ParamConstants.ACTION, action);
        requestParams.put(ParamConstants.STATUS, action.replaceFirst(AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst(AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
        
        if(isUpload){
          new Handler().post(new Runnable(){
            @Override
            public void run(){
              try{
                JSONArray js = new JSONArray();
                List<ProductModel> listFoundProducts = productDao.getAllFoundProducts();
                if(isNonEmpty(listFoundProducts))
                  for(ProductModel productModel : listFoundProducts){
                    if(productModel != null && productModel.getEpc() != null){
                      Inventory inventory = inventoryDao.getInventoryByEPC(productModel.epc);
                      if(inventory != null){
                        JSONObject dataObject = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_SHORT_JSON_REQUEST_FOR_STOCK_CORRECTION_UPLOAD) ? inventory.toOnlyEpcJson() : inventory.toJson(context);
                        
                        if(dataObject != null && chkNull(dataObject.toString(), "").length() > 2)
                          js.put(dataObject);
                      }
                    }
                  }
                requestParams.put(ParamConstants.ITEMS, js);
                allowBtnClick = true;
                callWebService(URLConstants.UPLOAD_STOCK_CORRECTION, requestParams, isUpload, getString(R.string.progress_msg_uploading_data), !isUpload);
              }
              catch(Exception e){
                e.printStackTrace();
                hideProgressDialog();
                allowBtnClick = true;
              }
            }
          });
        }
        else{
          allowBtnClick = true;
          callWebService(isUpload ? URLConstants.UPLOAD_STOCK_CORRECTION : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : "", !isUpload);
        }
      }
      catch(JSONException e){
        e.printStackTrace();
        hideProgressDialog();
        allowBtnClick = true;
      }
    }
  }
  
  /**
   * Store total counts.
   */
  public void storeTotalCounts(){
    totalCounts = productDao.getTotalCounts();
  }
  
  /**
   * Set header.
   */
  public void setHeader(){
    binding.llListHeader.txtBrand.setText(SharedPrefManager.getString(ParamConstants.LABEL_BRANDS,context.getString(R.string.lbl_brand)));
    final LinearLayout llHeader = binding.llListHeader.llInventoryShortage;
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
    final LinearLayout llHeader = binding.llListHeader.llInventoryShortage;
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
  
  /**
   * Set active users.
   *
   * @param activeUsers the active users
   */
  private void setActiveUsers(final int activeUsers){
    binding.header.flActiveDevices.setVisibility(activeUsers >= -1 ? View.VISIBLE : View.GONE);
    binding.header.btnActiveDevices.setSelected(true);
    binding.header.btnActiveDevices.setText(activeUsers >= 0 ? "" + activeUsers : "");
  }
  
  @Override
  public void onDestroyView(){
    totalCounts = null;
    productDao.onProductFound().removeObservers(getViewLifecycleOwner());
    super.onDestroyView();
  }
  
  @Override
  public void onBackPressed(){
    final MultiQtyModel multiQtyModel = productDao.getTotalCounts();
    final int found = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
    final int total = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
    if(found > 0 && total > 0){
      context.showCustomAlertDialog("", String.format(getString(R.string.title_stock_correction_back), found + "/" + total), R.string.btn_no, null, R.string.btn_yes, new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialogInterface, int i){
          productDao.deleteAllExcept();
          inventoryDao.deleteInventory(AppCommonMethods.SessionType.STOCK_CORRECTION.getValue());
          AppDatabase.getDbInstance(context).RFIDSessionDao().deleteAll(AppCommonMethods.SessionType.STOCK_CORRECTION.getValue());
          popBackStack();
        }
      });
    }
    else popBackStack();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.UPLOAD_STOCK_CORRECTION:
          if(isSuccess && jsonResponse != null){
            final MultiQtyModel multiQtyModel = productDao.getTotalCounts();
            final int found = multiQtyModel != null ? chkNull(multiQtyModel.found, 0) : 0;
            final int total = multiQtyModel != null ? chkNull(multiQtyModel.total, 0) : 0;
            
            context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), found == total);
            //delete Uploaded Records
            List<ProductModel> listFoundProducts = productDao.getAllFoundProducts();
            if(isNonEmpty(listFoundProducts)) for(ProductModel productModel : listFoundProducts){
              if(productModel != null && productModel.getEpc() != null){
                Inventory inventory = inventoryDao.getInventoryByEPC(productModel.epc);
                if(inventory != null){
                  inventoryDao.deleteInventoryData(inventory);
                  productDao.delete(productModel);
                }
              }
            }
          }
          sortByValues = "";
          resetHeader(0);
          setSpinners();
          updateLists();
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}