package com.itek.retail.ui.outward;

import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.itek.retail.R;
import com.itek.retail.adapter.pager.TripTypesPagerAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.FragmentOutwardPickDataBinding;
import com.itek.retail.model.TripStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Outward main fragment.
 */
public class OutwardPickDataFragment extends CommonFragment{
  
  private OutwardPickDataViewModel mViewModel;
  private TripStatusDao tripStatusDao;
  private FragmentOutwardPickDataBinding binding;
  private List<String> listTripTypeTabs = new ArrayList<>(0);
  
  /**
   * Instantiates a new Outward main fragment.
   */
  public OutwardPickDataFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    final AppDatabase db = AppDatabase.getDbInstance(context);
    tripStatusDao = db.TripStatusDao();
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    
    binding = FragmentOutwardPickDataBinding.inflate(inflater, container, false);
    
    listTripTypeTabs.clear();
    List<String> tripTypes = tripStatusDao != null ? tripStatusDao.getTripTypes(AppConstants.OUTWARD_PICK) : new ArrayList<>(0);
    if(isNonEmpty(tripTypes)) listTripTypeTabs.addAll(tripTypes);
    
    binding.viewPager.setAdapter(new TripTypesPagerAdapter(this, listTripTypeTabs));
    new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> tab.setText(AppCommonMethods.toTitleCase(listTripTypeTabs.get(position)).replaceAll("_", " "))).attach();
    
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
    
    if(savedInstanceState == null) callAPI();
    
    return binding.getRoot();
  }
  
  /**
   * Call api.
   */
  void callAPI(){
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.TYPE, AppConstants.OUTWARD);
      callWebService(URLConstants.GET_OUTWARD_PICK_LIST, jsonRequest, getString(R.string.progress_msg_getting_data), false, true);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()) callAPI();
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    if(binding != null && binding.viewPager != null && binding.viewPager.getAdapter() != null && binding.viewPager.getAdapter() instanceof FragmentStateAdapter)
      ((FragmentStateAdapter) binding.viewPager.getAdapter()).notifyDataSetChanged();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_OUTWARD_PICK_LIST:
          if(isSuccess && jsonResponse != null){
            JSONArray tripsData = extractJSONArray(jsonResponse, ParamConstants.K_TRIPS_DATA);
            if(isNonEmpty(tripsData)){
              tripStatusDao.deleteAllTripStatus(AppConstants.OUTWARD_PICK);
              for(int i = 0; i < tripsData.length(); i++){
                final TripStatus tripStatus = getGSON().fromJson(tripsData.getJSONObject(i).toString(), TripStatus.class);
                if(tripStatus != null){
                  tripStatus.setType(AppConstants.OUTWARD_PICK);
                  tripStatusDao.insertTripStatusData(tripStatus);
                }
              }
              listTripTypeTabs.clear();
              List<String> tripTypes = tripStatusDao != null ? tripStatusDao.getTripTypes(AppConstants.OUTWARD_PICK) : new ArrayList<>(0);
              if(isNonEmpty(tripTypes)) listTripTypeTabs.addAll(tripTypes);
              showLog("listTripTypeTabs", "" + listTripTypeTabs.size());
              hideProgressDialog();
            }
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