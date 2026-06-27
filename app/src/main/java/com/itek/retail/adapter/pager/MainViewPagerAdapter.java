package com.itek.retail.adapter.pager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.model.TagValue;
import com.itek.retail.ui.home.TabDashboardFragment;
import com.itek.retail.ui.home.TabFavouritesFragment;
import com.itek.retail.ui.home.TabHomeFragment;

import java.util.List;

/**
 * The Main view pager adapter.
 */
public class MainViewPagerAdapter extends FragmentStateAdapter{
  
  private List<TagValue> tabs;
  private CommonFragment frag;
  
  /**
   * Instantiates a new Main view pager adapter.
   *
   * @param fragment the fragment
   * @param tabs     the tabs
   */
  public MainViewPagerAdapter(@NonNull CommonFragment fragment, List<TagValue> tabs){
    super(fragment);
    this.frag = fragment;
    this.tabs = tabs;
  }
  
  @Override
  public Fragment createFragment(int position){
    Fragment fragment = null;
    switch(tabs.get(position).getTag().toUpperCase().trim()){
      case AppConstants.TAB_HOME:
        fragment = new TabHomeFragment();
        break;
      case AppConstants.TAB_FAVOURITES:
        fragment = new TabFavouritesFragment();
        break;
      case AppConstants.TAB_DASHBOARD:
        fragment = new TabDashboardFragment();
        break;
      default:
        break;
    }
    return fragment;
  }
  
  @Override
  public int getItemCount(){ return tabs.size(); }
}

