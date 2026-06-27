package com.itek.retail.ui.search.omnichannel;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.toTitleCase;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.itek.retail.R;
import com.itek.retail.adapter.pager.OmniChannelOrderStatsViewPagerAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentOmnichannelOrderStatsBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Omni channel order stats fragment.
 */
public class OmniChannelOrderStatsFragment extends CommonFragment{
  
  List<String> tabs = new ArrayList<>();
  String omniType = AppConstants.OMNI_TYPE_ORDER;
  private OmniChannelOrderStatsViewModel mViewModel;
  private FragmentOmnichannelOrderStatsBinding binding;
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(OmniChannelOrderStatsViewModel.class);
    binding = FragmentOmnichannelOrderStatsBinding.inflate(inflater, container, false);
    omniType = extractString(getArguments(), AppConstants.OMNICHANNEL_TYPE, AppConstants.OMNI_TYPE_ORDER);
    
    binding.rbOrder.setSelected(true);
    binding.rbEan.setSelected(true);
    binding.rbOrder.setTag(AppConstants.OMNI_TYPE_ORDER);
    binding.rbEan.setTag(AppConstants.OMNI_TYPE_EAN);
    binding.rbOrder.setText(SharedPrefManager.getString(ParamConstants.LABEL_ORDER,context.getString(R.string.lbl_order)));
    binding.rbEan.setText(SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean)));
    
    binding.rgOmniChannelType.setVisibility(SharedPrefManager.getOmnichannelType().equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH) ? View.VISIBLE : View.GONE);
    binding.rbEan.setChecked(!omniType.equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH) && omniType.equalsIgnoreCase(binding.rbEan.getTag().toString().toLowerCase().trim()));
    
    binding.viewPager.setAdapter(new OmniChannelOrderStatsViewPagerAdapter(this, tabs));
    new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> tab.setText(toTitleCase(tabs.get(position)))).attach();
    binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback(){
      @Override
      public void onPageSelected(int position){
        super.onPageSelected(position);
      }
    });
    
    binding.swipeLayout.setColorSchemeColors(context.getColorPrimaryDarkFromTheme());
    binding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
      @Override
      public void onRefresh(){
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
        OmniChannelOrderStatsFragment.this.omniType = omniType;
        clearDashboardVals();
        callAPI();
      }
    });
    
    clearDashboardVals();
    if(savedInstanceState == null) callAPI();
    
    return binding.getRoot();
  }
  
  public void clearDashboardVals(){
    ((OmniChannelOrderStatsViewPagerAdapter) binding.viewPager.getAdapter()).clear();
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(binding != null && binding.rgOmniChannelType != null && isTopInStack()) callAPI();
  }
  
  /**
   * Call api.
   */
  private void callAPI(){
    try{
      JSONObject jsonRequest = new JSONObject();
      jsonRequest.put(ParamConstants.OMNICHANNEL_TYPE, getOmnichannelType());
      callWebService(URLConstants.GET_OMNICHANNEL_ACHIEVEMENTS, jsonRequest, getString(R.string.progress_msg_getting_data));
    }
    catch(JSONException e){ e.printStackTrace(); }
  }
  
  /**
   * Get omnichannel type string.
   *
   * @return the string
   */
  public String getOmnichannelType(){
    return !chkNull(SharedPrefManager.getOmnichannelType(), AppConstants.OMNI_TYPE_BOTH).equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH) ? SharedPrefManager.getOmnichannelType() : omniType.toLowerCase().trim();
  }
  
  @Override
  public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args){
    try{
      switch(url){
        case URLConstants.GET_OMNICHANNEL_ACHIEVEMENTS:
          if(isSuccess){
            if(isNullOrEmpty(tabs) && jsonResponse != null){
              final Iterator<String> keys = jsonResponse.keys();
              while(keys.hasNext()){
                final String key = keys.next();
                if(isNonEmpty(extractJSONArray(jsonResponse, key))) tabs.add(key);
              }
              ((FragmentStateAdapter) binding.viewPager.getAdapter()).notifyDataSetChanged();
              new CountDownTimer(20, 20){
                @Override
                public void onTick(long l){//Empty method
                }
                
                @Override
                public void onFinish(){
                  ((OmniChannelOrderStatsViewPagerAdapter) binding.viewPager.getAdapter()).refresh(omniType, jsonResponse);
                }
              }.start();
            }
            else
              ((OmniChannelOrderStatsViewPagerAdapter) binding.viewPager.getAdapter()).refresh(omniType, jsonResponse);
          }
          break;
        default:
          break;
      }
    }
    catch(Exception e){ e.printStackTrace(); }
  }
}