package com.itek.retail.ui.search.omnichannel;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentTabOmnichannelOrderStatsBinding;
import com.itek.retail.ui.customviews.DashboardDataView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The Tab omni channel order stats today fragment.
 */
public class TabOmniChannelOrderStatsFragment extends CommonFragment{
  
  private TabOmniChannelOrderStatsViewModel mViewModel;
  private FragmentTabOmnichannelOrderStatsBinding binding;
  private String tabType = "";
  private String omniType = "";
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(TabOmniChannelOrderStatsViewModel.class);
    binding = FragmentTabOmnichannelOrderStatsBinding.inflate(inflater, container, false);
    tabType = AppCommonMethods.extractString(getArguments(), AppConstants.DASHBOARD_VIEW_TYPE);
    omniType = extractString(getArguments(), AppConstants.OMNICHANNEL_TYPE, chkNull(SharedPrefManager.getOmnichannelType(), AppConstants.OMNI_TYPE_ORDER));
    return binding.getRoot();
  }
  
  /**
   * Clear dashboard values.
   */
  public void clearDashboardVals(){
    final String defNoVal = getString(R.string.default_no_value);
    binding.ddvOrdersPerHour.setText(defNoVal);
    binding.ddvItemsPerHour.setText(defNoVal);
    binding.ddvFastestCompletedOrders.setText(defNoVal);
  }
  
  public void refresh(final String omniType, final JSONObject jsonResponse){
    try{
      if(jsonResponse != null){
        final JSONArray jsonOmniOrderStatsArray = AppCommonMethods.extractJSONArray(jsonResponse, tabType);
        if(jsonOmniOrderStatsArray != null && jsonOmniOrderStatsArray.length() > 0){
          for(int i = 0; i < jsonOmniOrderStatsArray.length(); i++){
            final JSONObject omniDashboardItem = jsonOmniOrderStatsArray.getJSONObject(i);
            final String type = AppCommonMethods.extractString(omniDashboardItem, ParamConstants.DASHBOARD_VIEW_TYPE, "");
            String label = AppCommonMethods.extractString(omniDashboardItem, ParamConstants.LABEL, "");
            if(isStaticDebug() && omniType.equalsIgnoreCase(AppConstants.OMNI_TYPE_EAN))
              label = label.replaceAll("Order", "EAN");
            final String count = AppCommonMethods.extractString(omniDashboardItem, ParamConstants.COUNT, "");
            final String percent = AppCommonMethods.extractString(omniDashboardItem, ParamConstants.PERCENT, "");
            final String percentLabel = AppCommonMethods.extractString(omniDashboardItem, ParamConstants.PERCENT_LABEL, "");
            final Boolean isUpwardArrow = AppCommonMethods.extractBoolean(omniDashboardItem, ParamConstants.IS_UPWARD_ARROW);
            if(AppConstants.DASHBOARD_VIEW_TYPES.contains(type.toLowerCase().trim())){
              final DashboardDataView dataView = type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_ORDER) ? binding.ddvOrdersPerHour : type.equalsIgnoreCase(AppConstants.DASHBOARD_VIEW_TYPE_ITEMS) ? binding.ddvItemsPerHour : binding.ddvFastestCompletedOrders;
              dataView.setLabelTextPercent(type, label, count, percentLabel, percent, isUpwardArrow);
            }
          }
        }
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}