package com.itek.retail.ui.movement;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractInt;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
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
import com.itek.retail.common.CustomTypefaceSpan;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentMovementMainBinding;
import com.itek.retail.model.LabelCounts;
import com.itek.retail.ui.customviews.DashboardDataView;
import com.itek.retail.ui.home.MainActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Movement main fragment.
 */
public class MovementMainFragment extends CommonFragment{
  
  private MovementMainViewModel mViewModel;
  private FragmentMovementMainBinding binding;
  private List<LabelCounts> listHourlyCountPending = new ArrayList<>(0);
  private List<LabelCounts> listHourlyCountCompleted = new ArrayList<>(0);
  
  /**
   * Instantiates a new Movement main fragment.
   */
  public MovementMainFragment(){/*Default/Empty Constructor*/}
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(MovementMainViewModel.class);
    binding = FragmentMovementMainBinding.inflate(inflater, container, false);
    
    binding.rbStatic.setSelected(true);
    binding.rbDynamic.setSelected(true);
    binding.rbStatic.setTag(AppConstants.REPLENISH_TYPE_STATIC);
    binding.rbDynamic.setTag(AppConstants.REPLENISH_TYPE_DYNAMIC);
    
    binding.rgReplenishmentType.setVisibility(chkNull(SharedPrefManager.getReplenishmentType(), AppConstants.REPLENISH_TYPE_BOTH).equalsIgnoreCase(AppConstants.REPLENISH_TYPE_BOTH) ? View.VISIBLE : View.GONE);
    binding.rbStatic.setChecked(!chkNull(SharedPrefManager.getReplenishmentType(), AppConstants.REPLENISH_TYPE_BOTH).equalsIgnoreCase(AppConstants.REPLENISH_TYPE_BOTH) && chkNull(SharedPrefManager.getReplenishmentType(), AppConstants.REPLENISH_TYPE_BOTH).equalsIgnoreCase(binding.rbStatic.getTag().toString().toLowerCase().trim()));
    
    binding.listTimeRecordsPending.setAdapter(new TimeRangeListAdapter((MainActivity) context, this, listHourlyCountPending));
    binding.listTimeRecordsPending.setLayoutManager(new LinearLayoutManager(context));
    
    binding.listTimeRecordsCompleted.setAdapter(new TimeRangeListAdapter((MainActivity) context, this, listHourlyCountCompleted));
    binding.listTimeRecordsCompleted.setLayoutManager(new LinearLayoutManager(context));
    
    binding.rgReplenishmentType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
      @Override
      public void onCheckedChanged(RadioGroup radioGroup, int i){
        clearDashboardVals();
        callAPI();
      }
    });
    
    //commented by Bhupen Morgaonkar on 26-09-2023 for 'On Demand Dashboard'
    /*if(savedInstanceState == null){
      clearDashboardVals();
      callAPI();
    }*/
    
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
    
    binding.movePendingEanQty.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        final View btnReplenish = binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_REPLENISH);
        if(btnReplenish != null) btnReplenish.performClick();
      }
    });
    
    //code for setting label
    final String msgInvRefresh = getString(R.string.msg_refresh_dashboard);
    int index = msgInvRefresh.indexOf("'");
    int indexEnd = msgInvRefresh.lastIndexOf("'") - 1;
    SpannableString ss = new SpannableString(msgInvRefresh.replaceAll("\'", ""));
    ss.setSpan(new CustomTypefaceSpan("", ResourcesCompat.getFont(context, R.font.font_awesome)), index, indexEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    ss.setSpan(new StyleSpan(Typeface.BOLD), indexEnd, indexEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    ss.setSpan(new RelativeSizeSpan(2f), indexEnd, indexEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    //ss.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.red)),indexEnd,indexEnd,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    binding.lblRefresh.setText(ss);
    
    //binding.lblRefresh.setText(HtmlCompat.fromHtml(getString(R.string.msg_refresh_inv_dashboard), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    return binding.getRoot();
  }
  
  @Override
  public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    final View btnMovStart = binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_MOV_START);
    final View btnReplenishment = chkNull((View) binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_MOV_REPLENISH),(View) binding.llSubHeader.llSubHeader.findViewWithTag(AppConstants.MENU_CODE_REPLENISH));
    
    final boolean hasReplenishmentMenu = btnReplenishment != null && btnReplenishment.getVisibility() == View.VISIBLE;
    AppCommonMethods.showLog("hasReplenishmentMenu", "" + hasReplenishmentMenu);
    
    if(!hasReplenishmentMenu){
      binding.swipeLayout.setEnabled(false);
      binding.txtReplenishment.setVisibility(View.GONE);
      binding.rgReplenishmentType.setVisibility(View.GONE);
      binding.llDashboardMov.setVisibility(View.GONE);
      binding.lblRefresh.setVisibility(View.GONE);
    }
    
  }
  
  /**
   * Clear dashboard vals.
   */
  private void clearDashboardVals(){
    listHourlyCountPending.clear();
    ((RecyclerView.Adapter) binding.listTimeRecordsPending.getAdapter()).notifyDataSetChanged();
    listHourlyCountCompleted.clear();
    ((RecyclerView.Adapter) binding.listTimeRecordsCompleted.getAdapter()).notifyDataSetChanged();
    final String defNoVal = getString(R.string.default_no_value);
    binding.moveTotalItem.setText(defNoVal);
    binding.moveCompletedEanQty.setText(defNoVal);
    binding.movePendingEanQty.setText(defNoVal);
  }
  
  @Override
  public void onResume(){
    super.onResume();
    //commented by Bhupen Morgaonkar on 26-09-2023 for 'On Demand Dashboard'
    if(binding != null && binding.rgReplenishmentType != null && isTopInStack() && SharedPrefManager.getBoolean(ParamConstants.IS_DASHBOARD_AUTO_REFRESH, false))
      callAPI();
  }
  
  /**
   * Get replenishment type string.
   *
   * @return the string
   */
  public String getReplenishmentType(){
    return !chkNull(SharedPrefManager.getReplenishmentType(), AppConstants.REPLENISH_TYPE_BOTH).equalsIgnoreCase(AppConstants.REPLENISH_TYPE_BOTH) ? SharedPrefManager.getReplenishmentType() : binding.rgReplenishmentType.findViewById(binding.rgReplenishmentType.getCheckedRadioButtonId()).getTag().toString().toLowerCase().trim();
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.REPLENISHMENT_TYPE, getReplenishmentType());
      callWebService(URLConstants.GET_REPLENISHMENT_DASHBOARD, jsonRequest, getString(R.string.progress_msg_getting_data));
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
        case URLConstants.GET_REPLENISHMENT_DASHBOARD:
          if(isSuccess){
            JSONArray jsonReplenishDashboardItemsArray = AppCommonMethods.extractJSONArray(jsonResponse, ParamConstants.ITEMS);
            if(jsonReplenishDashboardItemsArray != null && jsonReplenishDashboardItemsArray.length() > 0){
              clearDashboardVals();
              for(int i = 0; i < jsonReplenishDashboardItemsArray.length(); i++){
                final JSONObject replenishmentDashboardItem = jsonReplenishDashboardItemsArray.getJSONObject(i);
                final String type = AppCommonMethods.extractString(replenishmentDashboardItem, ParamConstants.DASHBOARD_VIEW_TYPE, "");
                final String label = AppCommonMethods.extractString(replenishmentDashboardItem, ParamConstants.LABEL, "");
                final String count = AppCommonMethods.extractString(replenishmentDashboardItem, ParamConstants.COUNT, "");
                final String percent = AppCommonMethods.extractString(replenishmentDashboardItem, ParamConstants.PERCENT, "");
                final String percentLabel = AppCommonMethods.extractString(replenishmentDashboardItem, ParamConstants.PERCENT_LABEL, "");
                final Boolean isUpwardArrow = AppCommonMethods.extractBoolean(replenishmentDashboardItem, ParamConstants.IS_UPWARD_ARROW);
                final JSONArray jsonArrayStats = AppCommonMethods.extractJSONArray(replenishmentDashboardItem, ParamConstants.STATS);
                if(AppConstants.DASHBOARD_VIEW_TYPES.contains(type.toLowerCase().trim())){
                  final DashboardDataView dataView = type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_PENDING) ? binding.movePendingEanQty : type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_COMPLETED) ? binding.moveCompletedEanQty : binding.moveTotalItem;
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