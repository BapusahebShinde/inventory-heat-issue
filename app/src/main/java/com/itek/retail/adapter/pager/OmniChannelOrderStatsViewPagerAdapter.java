package com.itek.retail.adapter.pager;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.ui.search.omnichannel.TabOmniChannelOrderStatsFragment;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Omni channel order stats view pager adapter.
 */
public class OmniChannelOrderStatsViewPagerAdapter extends FragmentStateAdapter{
  
  private List<String> tabs;
  private Set<Fragment> listFragments = new HashSet<>(0);
  private CommonFragment frag;
  private String omniType;
  private JSONObject jsonObject;
  
  /**
   * Instantiates a new Omni channel order stats view pager adapter.
   *
   * @param fragment the fragment
   * @param tabs     the tabs
   */
  public OmniChannelOrderStatsViewPagerAdapter(@NonNull CommonFragment fragment, List<String> tabs){
    super(fragment);
    this.frag = fragment;
    this.tabs = tabs;
  }
  
  /**
   * Instantiates a new Omni channel order stats view pager adapter.
   *
   * @param fragment the fragment
   * @param tabs     the tabs
   */
  public OmniChannelOrderStatsViewPagerAdapter(@NonNull CommonFragment fragment, final List<String> tabs, final JSONObject jsonObject, final String omniType){
    super(fragment);
    this.frag = fragment;
    this.tabs = tabs;
    this.jsonObject = jsonObject;
    this.omniType = omniType;
  }
  
  @Override
  public Fragment createFragment(int position){
    showLog("createFragment", "" + position);
    Bundle args = chkNull(frag.getArguments(), new Bundle());
    args.putString(AppConstants.DASHBOARD_VIEW_TYPE, tabs.get(position).trim());
    Fragment fragment = new TabOmniChannelOrderStatsFragment();
    fragment.setArguments(args);
    listFragments.add(fragment);
    if(fragment != null && fragment instanceof TabOmniChannelOrderStatsFragment && isNonEmpty(omniType) && jsonObject != null){
      showLog("" + omniType + "_jsonResponse", "" + (jsonObject != null));
      new Handler().postDelayed(new Runnable(){
        @Override
        public void run(){
          ((TabOmniChannelOrderStatsFragment) fragment).refresh(omniType, jsonObject);
        }
      }, 20);
    }
    return fragment;
  }
  
  public void setJsonAndOmniType(final JSONObject jsonObject, final String omniType){
    if(isNonEmpty(jsonObject) && isNonEmpty(omniType)){
      showLog("setJsonAndOmniType", omniType + "_" + (jsonObject != null));
      this.omniType = omniType;
      this.jsonObject = jsonObject;
    }
  }
  
  @Override
  public int getItemCount(){ return tabs.size(); }
  
  public void refresh(final String omniType, final JSONObject jsonObject){
    setJsonAndOmniType(jsonObject, omniType);
    if(jsonObject != null && isNonEmpty(listFragments)) for(Fragment fragment : listFragments)
      if(fragment != null && fragment instanceof TabOmniChannelOrderStatsFragment)
        ((TabOmniChannelOrderStatsFragment) fragment).refresh(omniType, jsonObject);
    
  }
  
  public void clear(){
    if(isNonEmpty(listFragments)) for(Fragment fragment : listFragments)
      if(fragment != null && fragment instanceof TabOmniChannelOrderStatsFragment)
        ((TabOmniChannelOrderStatsFragment) fragment).clearDashboardVals();
    
  }
}

