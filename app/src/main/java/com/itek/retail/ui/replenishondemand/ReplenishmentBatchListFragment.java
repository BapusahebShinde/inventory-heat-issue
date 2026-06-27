package com.itek.retail.ui.replenishondemand;

import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.itek.retail.R;
import com.itek.retail.adapter.ReplenishBatchListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.FragmentReplenishmentBatchListBinding;
import com.itek.retail.model.ReplenishBatch;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.movement.replenishment.ReplenishmentStartFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The Replenishment list fragment.
 */
public class ReplenishmentBatchListFragment extends CommonFragment{
  
  public FragmentReplenishmentBatchListBinding binding;
  private List<ReplenishBatch> listReplenishBatches = new ArrayList<>(0);
  private ReplenishmentDemandViewModel mViewModel;
  //private ReplenishBatchDao replenishBatchDao;
  
  /**
   * Instantiates a new Replenishment list fragment.
   */
  public ReplenishmentBatchListFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    //replenishBatchDao = AppDatabase.getReplenishBatchDao(context);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ReplenishmentDemandViewModel.class);
    binding = FragmentReplenishmentBatchListBinding.inflate(inflater, container, false);
    
    binding.listReplenishBatches.setAdapter(new ReplenishBatchListAdapter((MainActivity) context, ReplenishmentBatchListFragment.this, listReplenishBatches));
    binding.listReplenishBatches.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
    
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
    
    if(savedInstanceState == null && isNullOrEmpty(listReplenishBatches)/*replenishBatchDao.getTableSize() <= 0*/)
      callAPI();
    updateLists();
    return binding.getRoot();
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    try{
      JSONObject jsonRequest = new JSONObject();
      callWebService(URLConstants.GET_REPLENISHMENT_BATCHES, jsonRequest, getString(R.string.progress_msg_getting_data));
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()){// && isNonEmpty(listReplenishBatches)){//(replenishBatchDao == null || replenishBatchDao.getTableSize() <= 0)){
      callAPI();
    }
  }
  
  @Override
  public void onBackPressed(){
    AppDatabase.getInventoryDao(context).deleteInventory(AppCommonMethods.SessionType.REPLENISHMENT.getValue());
    super.onBackPressed();
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    //listReplenishBatches.clear();
    //listReplenishBatches.addAll(replenishBatchDao.getAll());
    //Old 2.0 Code
    //listReplenishProducts.addAll(productDao.getReplenishmentProducts(selSrcZone, selSrcZones, selCategory, selCategories, selBrand, selBrands, searchName));
    if(binding!=null && binding.listReplenishBatches!=null && binding.textNoData!=null){
      binding.listReplenishBatches.setVisibility(isNonEmpty(listReplenishBatches)?View.VISIBLE:View.GONE);
      binding.textNoData.setVisibility(isNullOrEmpty(listReplenishBatches)?View.VISIBLE:View.GONE);
      if(binding.listReplenishBatches.getAdapter() != null)
        ((RecyclerView.Adapter) binding.listReplenishBatches.getAdapter()).notifyDataSetChanged();
    }
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_REPLENISHMENT_BATCHES:
          if(isSuccess && jsonResponse != null){
            listReplenishBatches.clear();
            //set replenishment batch list
            JSONArray replenishmentBatchesArray = extractJSONArray(jsonResponse, ParamConstants.BATCHES, extractJSONArray(jsonResponse, ParamConstants.DATA));
            ReplenishBatch activeReplenishBatch = null;
            if(isNonEmpty(replenishmentBatchesArray)){
              for(int i = 0; i < replenishmentBatchesArray.length(); i++){
                Object obj = replenishmentBatchesArray.get(i);
                if(obj != null && obj instanceof JSONObject){
                  final ReplenishBatch replenishBatch = getGSON().fromJson(obj.toString(), ReplenishBatch.class);
                  if(replenishBatch != null){
                    if(replenishBatch.isActive()) activeReplenishBatch = replenishBatch;
                    listReplenishBatches.add(replenishBatch);
                  }
                }
              }
              if(activeReplenishBatch!=null)  AppDatabase.getBatchEpcDao(context).deleteBatchEpcsExcept(activeReplenishBatch.getBatchId());
              /*if(isNonEmpty(listReplenishBatches) && activeReplenishBatch!=null && listReplenishBatches.contains(activeReplenishBatch)){
                listReplenishBatches.remove(activeReplenishBatch);
                listReplenishBatches.add(0,activeReplenishBatch);
              }*/
              if(isNonEmpty(listReplenishBatches))
                Collections.sort(listReplenishBatches, new Comparator<ReplenishBatch>(){
                  @Override
                  public int compare(ReplenishBatch o1, ReplenishBatch o2){
                    return o2.getBatchDate().compareTo(o1.getBatchDate());
                  }
                });
            }
            updateLists();
          }
          break;
        case URLConstants.GET_REPLENISHMENT_BATCH_DETAILS:
          if(isSuccess && jsonResponse != null){
            JSONArray replenishmentBatchesDetailsArray = extractJSONArray(jsonResponse, ParamConstants.BATCHES, extractJSONArray(jsonResponse, ParamConstants.DATA));
            //ReplenishBatch activeReplenishBatch = null;
            if(isNonEmpty(replenishmentBatchesDetailsArray)){
              for(int i = 0; i < replenishmentBatchesDetailsArray.length(); i++){
                Object obj = replenishmentBatchesDetailsArray.get(i);
                if(obj != null && obj instanceof JSONObject){
                  final ReplenishBatch replenishBatch = getGSON().fromJson(obj.toString(), ReplenishBatch.class);
                  if(replenishBatch != null){
                    //if(replenishBatch.isActive()) activeReplenishBatch = replenishBatch;
                    listReplenishBatches.add(replenishBatch);
                  }
                }
              }
              if(isNonEmpty(listReplenishBatches))
                Collections.sort(listReplenishBatches, new Comparator<ReplenishBatch>(){
                  @Override
                  public int compare(ReplenishBatch o1, ReplenishBatch o2){
                    return o2.getBatchId().compareTo(o1.getBatchId());
                  }
                });
            }
            updateLists();
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