package com.itek.retail.ui.replenishondemand;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.itek.retail.R;
import com.itek.retail.adapter.ReplenishBatchArticleListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.InsertDBReplenishBatchDetails;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.ReplenishBatchDetailsDao;
import com.itek.retail.database.ZoneDao;
import com.itek.retail.databinding.FragmentReplenishmentArticleListBinding;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.ReplenishBatch;
import com.itek.retail.model.ReplenishBatchDetails;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.customviews.HeaderSpinner;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.movement.replenishment.ReplenishmentStartFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Replenishment list fragment.
 */
public class ReplenishmentArticleListFragment extends CommonFragment{
  
  public FragmentReplenishmentArticleListBinding binding;
  ZoneDao zoneDao;
  ReplenishBatchDetailsDao replenishBatchDetailsDao;
  private List<Zone> listSrcLocations = new ArrayList<>(0);
  private List<Zone> listDestLocations = new ArrayList<>(0);
  private List<ReplenishBatchDetails> listReplenishProducts = new ArrayList<>(0);
  private ReplenishmentDemandViewModel mViewModel;
  private String selSrcZone = "";
  private String selDestZone = "";
  private ReplenishBatch replenishBatch;
  private Integer visibleListRecordLimit = isDebugApp ? 20 : 20;
  private Integer fromListIndex = 0;
  private long totalListCount = 0l;
  //IS Casecade Effect Flag
  private boolean isCascadeEffect = true;
  private boolean isMultiSelect = true;
  
  private List<String> listMatkl = new ArrayList<>(0);
  private List<String> listCategory = new ArrayList<>(0);
  private ExecutorService executor = Executors.newSingleThreadExecutor(); // You can also use a cached thread pool or a custom executor
  private HashMap<String, Set<String>> selTempFilters = new HashMap<>(0);

  private boolean isEOSSReplenishment = false;
  
  /**
   * Instantiates a new Replenishment list fragment.
   */
  public ReplenishmentArticleListFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    zoneDao = AppDatabase.getZoneDao(context);
    replenishBatchDetailsDao = AppDatabase.getReplenishBatchDetailsDao(context);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ReplenishmentDemandViewModel.class);
    binding = FragmentReplenishmentArticleListBinding.inflate(inflater, container, false);

    isEOSSReplenishment = getMenuModel().menuCode.contains("EOSS");

    listSrcLocations = zoneDao.getBOHZones();
    listDestLocations = zoneDao.getFOHZones();
    replenishBatch = (ReplenishBatch) extractSerializable(getArguments(), ReplenishBatch.class);
    if (isEOSSReplenishment && replenishBatch == null){
      replenishBatch = new ReplenishBatch();
      replenishBatch.batchId = "";
    }
    
    if(isNullOrEmpty(listSrcLocations) || isNullOrEmpty(listDestLocations)){
      context.showCustomErrDialog(getString(R.string.err_no_zones_replenishment), true);
      return binding.getRoot();
    }
    if(replenishBatch == null){
      context.showCustomErrDialog(getString(R.string.err_no_batch), true);
      return binding.getRoot();
    }
    
    listMatkl.add(AppConstants.ALL);
    listMatkl.addAll(replenishBatchDetailsDao.getMatklList());
    listCategory.add(AppConstants.ALL);
    listCategory.addAll(replenishBatchDetailsDao.getCategoryList());
    
    binding.spinSourceZone.setAdapter(listSrcLocations, 0);
    binding.spinDestinationZone.setAdapter(listDestLocations, 0);
    
    if(isNonEmpty(listSrcLocations) && listSrcLocations.size() == 1 && isNonEmpty(listDestLocations) && listDestLocations.size() == 1){
      binding.llReplenishmentZones.setVisibility(View.GONE);
    }
    
    //binding.listReplenishmentProducts.setVisibility();
    
    binding.spinMatkl.setAdapter(listMatkl, 0);
    binding.spinCategory.setAdapter(listCategory, 0);
    
    binding.spinCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        if(isNullOrEmpty(binding.spinCategory.getSelectedVals()) || binding.spinCategory.getSelectedVals().contains(AppConstants.ALL))
          selTempFilters.remove("category");
        else
          selTempFilters.put("category", binding.spinCategory.getSelectedVals());
        if(isCascadeEffect)
          resetCascadeFilters(binding.llReplenishmentDetails, isMultiSelect, "category", selTempFilters);
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){
      
      }
    });
    
    binding.spinMatkl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
        if(isNullOrEmpty(binding.spinMatkl.getSelectedVals()) || binding.spinMatkl.getSelectedVals().contains(AppConstants.ALL))
          selTempFilters.remove("matkl");
        else
          selTempFilters.put("matkl", binding.spinMatkl.getSelectedVals());
        if(isCascadeEffect)
          resetCascadeFilters(binding.llReplenishmentDetails, isMultiSelect, "matkl", selTempFilters);
        updateLists();
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> adapterView){
      
      }
    });
    
    binding.listReplenishmentProducts.setAdapter(new ReplenishBatchArticleListAdapter(context, this, listReplenishProducts));
    binding.listReplenishmentProducts.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
    binding.listReplenishmentProducts.addOnScrollListener(new RecyclerView.OnScrollListener(){
      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy){
        super.onScrolled(recyclerView, dx, dy);
        showLog("size", totalListCount + "_" + visibleListRecordLimit);
        if(totalListCount > visibleListRecordLimit){
            /*if (!recyclerView.canScrollVertically(1)){ //1 for down
              //loadMore();
            }*/
          LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
          //if (!isLoading) {
          showLog("size1", linearLayoutManager.findLastCompletelyVisibleItemPosition() + "_" + listReplenishProducts.size());
          if(linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == listReplenishProducts.size() - 1){
            //bottom of list!
            //loadMore();
            showLog("size2", totalListCount + "_" + listReplenishProducts.size());
            if(totalListCount > listReplenishProducts.size()) updateRecyclerView();
            //isLoading = true;
          }
          //}
        }
      }
    });
    
   /* binding.swipeLayout.setColorSchemeColors(context.getColorPrimaryDarkFromTheme());
    binding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
      @Override
      public void onRefresh(){
        binding.swipeLayout.setRefreshing(false);
        callAPI();
      }
    });*/
    
    binding.header.imgConfigSync.setVisibility(View.VISIBLE);
    binding.header.imgConfigSync.setImageResource(R.drawable.ic_sync);
    binding.header.imgConfigSync.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        callAPI();
      }
    });
    
    if(savedInstanceState == null && isNullOrEmpty(listReplenishProducts)) callAPI();
    updateLists();
    return binding.getRoot();
  }
  
  private void updateSpinners(){
    if(isCascadeEffect){
      resetCascadeFilters(binding.llReplenishmentDetails, isMultiSelect, "category", selTempFilters);
      resetCascadeFilters(binding.llReplenishmentDetails, isMultiSelect, "matkl", selTempFilters);
      if(isNonEmpty(selTempFilters) && selTempFilters.containsKey("category"))
        binding.spinCategory.setSelection(selTempFilters.get("category"));
      if(isNonEmpty(selTempFilters) && selTempFilters.containsKey("matkl"))
        binding.spinMatkl.setSelection(selTempFilters.get("matkl"));
      return;
    }
    
    listCategory.clear();
    listCategory.add(AppConstants.ALL);
    listCategory.addAll(replenishBatchDetailsDao.getCategoryList());
    binding.spinCategory.setAdapter(listCategory);
    if(isNonEmpty(selTempFilters) && selTempFilters.containsKey("category"))
      binding.spinCategory.setSelection(selTempFilters.get("category"));
    
    listMatkl.clear();
    listMatkl.add(AppConstants.ALL);
    listMatkl.addAll(replenishBatchDetailsDao.getMatklList());
    binding.spinMatkl.setAdapter(listMatkl);
    if(isNonEmpty(selTempFilters) && selTempFilters.containsKey("matkl"))
      binding.spinMatkl.setSelection(selTempFilters.get("matkl"));
  }
  
  void resetCascadeFilters(final LinearLayout llMain, final boolean isMultiSelect, final String selTag, final HashMap<String, Set<String>> selTempFilters){
    for(int i = 0; i < llMain.getChildCount(); i++){
      final View view = llMain.getChildAt(i);
      if(view != null && view instanceof HeaderSpinner){
        HeaderSpinner hs = (HeaderSpinner) view;
        final String val = (isNonEmpty(hs.getSelectedVals()) && !hs.getSelectedVals().contains(AppConstants.ALL) ? hs.getSelectedVals().toString() : chkNull(hs.getSelectedItem(), AppConstants.ALL)).trim();
        final String tag = chkNull(hs.getTag() != null && hs.getTag() instanceof String ? hs.getTag().toString() : "", "").trim();
        AppCommonMethods.showLog("val", val);
        if(!selTag.equalsIgnoreCase(tag) && !selTempFilters.containsKey(tag)){
          final List<String> listVals = replenishBatchDetailsDao.getList(tag, selTempFilters);
          if(!listVals.contains(AppConstants.ALL)) listVals.add(0, AppConstants.ALL);
          hs.setAdapter(listVals, isMultiSelect);
        }
      }
      else if(view != null && view instanceof LinearLayout){
        final LinearLayout llRow = (LinearLayout) view;
        resetCascadeFilters(llRow, isMultiSelect, selTag, selTempFilters);
      }
    }
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.BATCH_ID, replenishBatch.getBatchId());
      jsonRequest.put(ParamConstants.IS_EOSS_REPLENISHMENT, isEOSSReplenishment);
      callWebService(URLConstants.GET_REPLENISHMENT_BATCH_DETAILS, jsonRequest, getString(R.string.progress_msg_getting_data), false, false);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  /**
   * Get sel dest zone string.
   *
   * @return the string
   */
  public String getSelDestZoneName(){ return chkNull(binding.spinDestinationZone.getSelectedItem(), AppConstants.ALL); }
  
  public Zone getSelDestZone(){
    Object obj = binding.spinDestinationZone.getSelectedObject();
    return obj != null && obj instanceof Zone ? (Zone) obj : null;
  }
  
  /**
   * Get sel src zone string.
   *
   * @return the string
   */
  public String getSelSrcZoneName(){ return chkNull(binding.spinSourceZone.getSelectedItem(), AppConstants.ALL); }
  
  public Zone getSelSrcZone(){
    Object obj = binding.spinSourceZone.getSelectedObject();
    return obj != null && obj instanceof Zone ? (Zone) obj : null;
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
    //productDao.deleteAll();
    //Code to delete all saved epc List ?
    //if(isNonEmpty(listReplenishProducts)){
    //AppDatabase.getBatchEpcDao(context).deleteBatchEpcs(replenishBatch.getBatchId());
    //for(ReplenishBatchDetails rbd :listReplenishProducts)
    //SharedPrefManager.clearArrayList(rbd.getBatchId()+rbd.getEan() + ParamConstants.PICKED_EPCS);
    //}
    AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.REPLENISHMENT.getValue());
    super.onBackPressed();
  }
  
  public void postAPIResult(){
    updateSpinners();
    updateLists();
  }
  
  @Override
  public void updateLists(){ updateLists(true); }
  
  public void updateLists(final boolean isResetIndexes){
    super.updateLists();
    if(binding != null && binding.listReplenishmentProducts != null && binding.textNoData != null){
      //binding.listReplenishmentProducts.setVisibility(isNonEmpty(listReplenishProducts) ? View.VISIBLE : View.GONE);
      //binding.textNoData.setVisibility(isNullOrEmpty(listReplenishProducts) ? View.VISIBLE : View.GONE);
      if(isResetIndexes){
        fromListIndex = 0;
      }
      final String selDestZone = binding.spinDestinationZone.getSelectedItem();
      final Set<String> selDestZones = binding.spinDestinationZone.getSelectedVals();
      final String selSrcZone = binding.spinSourceZone.getSelectedItem();
      final Set<String> selSrcZones = binding.spinSourceZone.getSelectedVals();
      final String selMatkl = binding.spinMatkl.getSelectedItem();
      final Set<String> selMatkls = binding.spinMatkl.getSelectedVals();
      
      final String selCategory = binding.spinCategory.getSelectedItem();
      final Set<String> selCategories = binding.spinCategory.getSelectedVals();
      final String searchName = "";//chkNull(binding.edtReplenishmentNameSearch.getText().toString(), "").trim();
      if(binding.listReplenishmentProducts.getAdapter() != null) executor.execute(new Runnable(){
        @Override
        public void run(){
          showLog("adapterUpdate", "Called");
          totalListCount = (!isResetIndexes) ? totalListCount : replenishBatchDetailsDao.getBatchTotalCount(replenishBatch.getBatchId(), selCategory, selCategories, selMatkl, selMatkls, searchName);//replenishBatchDetailsDao.getBatchTotalCount(replenishBatch.getBatchId(),searchName);
          showLog("listReplenishProds(indexes)", fromListIndex + "_" + visibleListRecordLimit);
          final List<ReplenishBatchDetails> queryList = replenishBatchDetailsDao.getBatchDetails(replenishBatch.getBatchId(), selCategory, selCategories, selMatkl, selMatkls, searchName, fromListIndex, visibleListRecordLimit);//replenishBatchDetailsDao.getBatchDetails(replenishBatch.getBatchId(), searchName, fromListIndex, visibleListRecordLimit);
          showLog("listReplenishProds(total)", "" + totalListCount);
          showLog("listReplenishProds(queryList)", "" + queryList.size());
          context.runOnUiThread(new Runnable(){
            @Override
            public void run(){
              if(isResetIndexes) listReplenishProducts.clear();
              showLog("listReplenishProds0", "" + listReplenishProducts.size());
              listReplenishProducts.addAll(queryList);
              showLog("listReplenishProds1", "" + listReplenishProducts.size());
              if(isResetIndexes) binding.listReplenishmentProducts.scrollToPosition(0);
              ((RecyclerView.Adapter) binding.listReplenishmentProducts.getAdapter()).notifyDataSetChanged();
              showLog("listReplenishProds2", "" + listReplenishProducts.size());
              binding.listReplenishmentProducts.setVisibility(isNonEmpty(listReplenishProducts) ? View.VISIBLE : View.GONE);
              binding.textNoData.setVisibility(isNullOrEmpty(listReplenishProducts) ? View.VISIBLE : View.GONE);
            }
          });
        }
      });
      //((RecyclerView.Adapter) binding.listReplenishmentProducts.getAdapter()).notifyDataSetChanged();
    }
    //final Integer totalEanQty = productDao.getTotalEANCount(selDestZone, selDestZones, selCategory, selCategories, selBrand, selBrands, searchName);
    //binding.txtTotal.setText(String.format(getString(R.string.txt_movement_replenishment_total), chkZero(totalEanQty, "0")));
  }
  
  private void updateRecyclerView(){
    showLog("updateRecyclerView", "called");
    fromListIndex += visibleListRecordLimit;
    updateLists(false);
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_REPLENISHMENT_BATCH_DETAILS:
          if(isSuccess && jsonResponse != null){
            final String batchId = extractString(jsonResponse, ParamConstants.BATCH_ID, extractString(jsonRequest, ParamConstants.BATCH_ID));
            if (isNullOrEmpty(replenishBatch.batchId) && isNonEmpty(batchId)) replenishBatch.batchId = batchId;
            JSONArray replenishmentBatchDetailsArray = extractJSONArray(jsonResponse, ParamConstants.REPLENISHMENT_PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.PRODUCTS, extractJSONArray(jsonResponse, ParamConstants.DATA)));
            if(replenishmentBatchDetailsArray != null){
              new InsertDBReplenishBatchDetails(context, this, jsonResponse, url, batchId, "", args).execute(replenishmentBatchDetailsArray);
            }
            else if(isNonEmpty(replenishmentBatchDetailsArray)){
              List<ReplenishBatchDetails> listReplenishProducts = new ArrayList<>(0);
              for(int i = 0; i < replenishmentBatchDetailsArray.length(); i++){
                Object obj = replenishmentBatchDetailsArray.get(i);
                if(obj != null && obj instanceof JSONObject){
                  JSONObject batchDtls = (JSONObject) obj;
                  ReplenishBatchDetails replenishBatchDetails = getGSON().fromJson(batchDtls.toString(), ReplenishBatchDetails.class);
                  if(replenishBatchDetails != null){
                    if(isNullOrEmpty(replenishBatchDetails.getBatchId()))
                      replenishBatchDetails.setBatchId(batchId);
                    JSONArray jsonArrayEanQty = extractJSONArray(batchDtls, ParamConstants.EANS);
                    if(isNonEmpty(jsonArrayEanQty)){
                      for(int j = 0; j < jsonArrayEanQty.length(); j++){
                        JSONObject eanQty = jsonArrayEanQty.getJSONObject(j);
                        if(eanQty != null){
                          final String ean = extractString(eanQty, ParamConstants.EAN, "");
                          final Integer eanPickQty = extractInt(eanQty, ParamConstants.PICK_QTY, extractInt(eanQty, ParamConstants.PICKED_QTY, extractInt(eanQty, ParamConstants.QTY, 0)));
                          if(isNonEmpty(ean)){
                            ReplenishBatchDetails rbd = new ReplenishBatchDetails(replenishBatchDetails);//AppCommonMethods.deepClone(replenishBatchDetails);
                            rbd.setEan(ean);
                            rbd.setEanPickQty(eanPickQty);
                            listReplenishProducts.add(rbd);
                          }
                        }
                      }
                    }
                    else listReplenishProducts.add(replenishBatchDetails);
                  }
                }
              }
              if(isNonEmpty(listReplenishProducts))
                replenishBatchDetailsDao.insertAll(listReplenishProducts);
            }
            updateLists();
            //Commented for Now
            /*if(isNonEmpty(replenishmentBatchDetailsArray))
              ((MainActivity) context).callInsertProductDBTask(this, url, AppCommonMethods.SessionType.REPLENISHMENT.getValue(), jsonRequest, jsonResponse, args, replenishmentProductsArray);
            else hideProgressDialog();*/
          }
          break;
        case URLConstants.GET_PRODUCT_INFO:
        case URLConstants.GET_PRODUCT_INFO_BY_SKU:
          ProductModel productModel = getProductModelFromResponse(jsonRequest, jsonResponse);
          ReplenishBatchDetails replenishBatchDetails = (ReplenishBatchDetails) extractSerializable(args, ReplenishBatchDetails.class);
          if(productModel != null){
            if(isStaticDebug())
              productModel.setEan(extractString(jsonRequest, ParamConstants.EAN, ""));
            //Update/Set Total & Found Qty for the Product Model from ReplenishBatchDetails
            if(productModel.getEan().equalsIgnoreCase(replenishBatchDetails.getEan())){
              productModel.setQty(replenishBatchDetails.getTotalQty());
              productModel.setFoundQty(replenishBatchDetails.getPickQty());
              productModel.setFound(replenishBatchDetails.getPickQty() > 0);
            }
            if(args == null) args = new Bundle();
            args.putSerializable(AppConstants.SRC_ZONE, getSelSrcZone());
            args.putSerializable(AppConstants.DEST_ZONE, getSelDestZone());
            args.putSerializable(productModel.getClass().getSimpleName(), productModel);
            args.putSerializable(ParamConstants.IS_EOSS_REPLENISHMENT, isEOSSReplenishment);
            context.loadFragment(new ReplenishmentEanStartFragment(), args);
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