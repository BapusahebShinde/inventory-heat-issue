package com.itek.retail.ui.search.omnichannel;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractInt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.itek.retail.R;
import com.itek.retail.adapter.TimeRangeListAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentOmnichannelBinding;
import com.itek.retail.model.LabelCounts;
import com.itek.retail.ui.customviews.DashboardDataView;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Omni channel fragment.
 */
public class OmniChannelFragment extends CommonFragment{
  
  private OmniChannelViewModel mViewModel;
  private FragmentOmnichannelBinding binding;
  private List<LabelCounts> listHourlyCountPending = new ArrayList<>(0);
  private List<LabelCounts> listHourlyCountCompleted = new ArrayList<>(0);
  
  /**
   * Instantiates a new Omni channel fragment.
   */
  public OmniChannelFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OmniChannelViewModel.class);
    binding = FragmentOmnichannelBinding.inflate(inflater, container, false);
    
    final String omniType = SharedPrefManager.getOmnichannelType();
    binding.rbOrder.setSelected(true);
    binding.rbEan.setSelected(true);
    binding.rbOrder.setTag(AppConstants.OMNI_TYPE_ORDER);
    binding.rbEan.setTag(AppConstants.OMNI_TYPE_EAN);
    binding.rbOrder.setText(SharedPrefManager.getString(ParamConstants.LABEL_ORDER,context.getString(R.string.lbl_order)));
    binding.rbEan.setText(SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean)));
    
    binding.rgOmniChannelType.setVisibility(omniType.equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH) ? View.VISIBLE : View.GONE);
    binding.rbEan.setChecked(!omniType.equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH) && omniType.equalsIgnoreCase(binding.rbEan.getTag().toString().toLowerCase().trim()));
    
    binding.ddvToday.setLabel(String.format(getString(R.string.lbl_todays_qty), omniType));
    binding.ddvCompleted.setLabel(String.format(getString(R.string.lbl_completed_qty), omniType));
    binding.ddvPending.setLabel(String.format(getString(R.string.lbl_pending_qty), omniType));
    
    binding.ddvPending.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        final View btnOmniStart = binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_SER_OMNI_START);
        if(btnOmniStart != null) btnOmniStart.performClick();
      }
    });
    
    binding.listTimeRecordsPending.setAdapter(new TimeRangeListAdapter((MainActivity) context, this, listHourlyCountPending));
    binding.listTimeRecordsPending.setLayoutManager(new LinearLayoutManager(context));
    
    binding.listTimeRecordsCompleted.setAdapter(new TimeRangeListAdapter((MainActivity) context, this, listHourlyCountCompleted));
    binding.listTimeRecordsCompleted.setLayoutManager(new LinearLayoutManager(context));
    
    binding.rgOmniChannelType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        clearDashboardVals();
        callAPI();
      }
    });
    
    if(savedInstanceState == null){
      clearDashboardVals();
      callAPI();
    }
    
    clearDashboardVals();
    
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
    
    return binding.getRoot();
  }
  
  /**
   * Clear dashboard vals.
   */
  private void clearDashboardVals(){
    if(SharedPrefManager.getOmnichannelType().equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH)){
      final String omniType = getOmnichannelType();
      binding.ddvToday.setLabel(String.format(getString(R.string.lbl_todays_qty), omniType));
      binding.ddvCompleted.setLabel(String.format(getString(R.string.lbl_completed_qty), omniType));
      binding.ddvPending.setLabel(String.format(getString(R.string.lbl_pending_qty), omniType));
    }
    
    listHourlyCountPending.clear();
    ((RecyclerView.Adapter) binding.listTimeRecordsPending.getAdapter()).notifyDataSetChanged();
    listHourlyCountCompleted.clear();
    ((RecyclerView.Adapter) binding.listTimeRecordsCompleted.getAdapter()).notifyDataSetChanged();
    final String defNoVal = getString(R.string.default_no_value);
    binding.ddvToday.setText(defNoVal);
    binding.ddvCompleted.setText(defNoVal);
    binding.ddvPending.setText(defNoVal);
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(binding != null && binding.rgOmniChannelType != null && isTopInStack()) callAPI();
  }
  
  /**
   * Get omnichannel type string.
   *
   * @return the string
   */
  public String getOmnichannelType(){
    return !chkNull(SharedPrefManager.getOmnichannelType(), AppConstants.OMNI_TYPE_BOTH).equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH) ? SharedPrefManager.getOmnichannelType() : binding.rgOmniChannelType.findViewById(binding.rgOmniChannelType.getCheckedRadioButtonId()).getTag().toString().toLowerCase().trim();
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.OMNICHANNEL_TYPE, getOmnichannelType());
      callWebService(URLConstants.GET_OMNICHANNEL_DASHBOARD, jsonRequest, getString(R.string.progress_msg_getting_data));
    }
    catch(JSONException e){ e.printStackTrace(); }
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
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
    try{
      switch(url){
        case URLConstants.GET_OMNICHANNEL_DASHBOARD:
          if(isSuccess){
            JSONArray jsonOmniDashboardItemsArray = AppCommonMethods.extractJSONArray(jsonResponse, ParamConstants.ITEMS);
            if(jsonOmniDashboardItemsArray != null && jsonOmniDashboardItemsArray.length() > 0){
              clearDashboardVals();
              for(int i = 0; i < jsonOmniDashboardItemsArray.length(); i++){
                final JSONObject omniDashboardItem = jsonOmniDashboardItemsArray.getJSONObject(i);
                final String type = AppCommonMethods.extractString(omniDashboardItem, ParamConstants.DASHBOARD_VIEW_TYPE, "");
                String label = AppCommonMethods.extractString(omniDashboardItem, ParamConstants.LABEL, "");
                final String count = AppCommonMethods.extractString(omniDashboardItem, ParamConstants.COUNT, "");
                final String percent = AppCommonMethods.extractString(omniDashboardItem, ParamConstants.PERCENT, "");
                final String percentLabel = AppCommonMethods.extractString(omniDashboardItem, ParamConstants.PERCENT_LABEL, "");
                final Boolean isUpwardArrow = AppCommonMethods.extractBoolean(omniDashboardItem, ParamConstants.IS_UPWARD_ARROW);
                final JSONArray jsonArrayStats = AppCommonMethods.extractJSONArray(omniDashboardItem, ParamConstants.STATS);
                if(AppConstants.DASHBOARD_VIEW_TYPES.contains(type.toLowerCase().trim())){//type.matches("(?i)(total|completed|pending)")){
                  final DashboardDataView dataView = type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_PENDING) ? binding.ddvPending : type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_COMPLETED) ? binding.ddvCompleted : binding.ddvToday;
                  dataView.setLabelTextPercent(type, label, count, percentLabel, percent, isUpwardArrow);
                  if(type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_PENDING) && jsonArrayStats != null){
                    listHourlyCountPending.clear();
                    for(int j = 0; j < jsonArrayStats.length(); j++){
                      final LabelCounts labelCounts = getGSON().fromJson(jsonArrayStats.getJSONObject(j).toString(), LabelCounts.class);
                      if(labelCounts != null) listHourlyCountPending.add(labelCounts);
                    }
                    ((RecyclerView.Adapter) binding.listTimeRecordsPending.getAdapter()).notifyDataSetChanged();
                  }
                  if(type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_COMPLETED) && jsonArrayStats != null){
                    listHourlyCountCompleted.clear();
                    for(int j = 0; j < jsonArrayStats.length(); j++){
                      final LabelCounts labelCounts = getGSON().fromJson(jsonArrayStats.getJSONObject(j).toString(), LabelCounts.class);
                      if(labelCounts != null) listHourlyCountCompleted.add(labelCounts);
                    }
                    ((RecyclerView.Adapter) binding.listTimeRecordsCompleted.getAdapter()).notifyDataSetChanged();
                  }
                }
              }
            }
          }
          setActiveUsers(extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, -2));
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
}