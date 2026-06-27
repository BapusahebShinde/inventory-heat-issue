package com.itek.retail.ui.home;

import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isSafariApp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;
import com.itek.retail.R;
import com.itek.retail.adapter.pager.MainViewPagerAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.MenuDao;
import com.itek.retail.databinding.FragmentHomeBinding;
import com.itek.retail.model.TagValue;

import java.util.ArrayList;
import java.util.List;

/**
 * The Home fragment.
 */
public class HomeFragment extends CommonFragment{
  
  private HomeViewModel homeViewModel;
  private FragmentHomeBinding binding;
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    homeViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(HomeViewModel.class);
    binding = FragmentHomeBinding.inflate(inflater, container, false);
    List<TagValue> tabs = new ArrayList<>(0);
    final MenuDao menuDao = AppDatabase.getMenuDao(context);
    int menuSize = menuDao.getTableSize();
    if(menuSize > 0 && menuDao.getTotalHomeMenuSize() > 0)
      tabs.add(new TagValue(AppConstants.TAB_HOME, getString(R.string.tab_home)));
    if(menuSize > 0 && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FAVOURITE_MENU_SCREEN,AppCommonMethods.isShowFavouriteMenuScreen) && menuDao.getTotalFavMenuSize() > 0)
      tabs.add(new TagValue(AppConstants.TAB_FAVOURITES, getString(R.string.tab_favourites)));
    if(AppCommonMethods.isNonEmpty(SharedPrefManager.getDashboardUrl()))
      tabs.add(new TagValue(AppConstants.TAB_DASHBOARD, getString(R.string.tab_dashboard)));
    
    if(isNonEmpty(tabs)){
      binding.viewPager.setAdapter(new MainViewPagerAdapter(this, tabs));
      new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> tab.setText(tabs.get(position).getValue())).attach();
    }
    return binding.getRoot();
  }
  
  @Override
  public void onResume(){
    super.onResume();
    if(isTopInStack()) updateLists();
  }
  
  @Override
  public void updateLists(){
    if(binding != null && binding.viewPager != null && binding.viewPager.getAdapter() != null)
      binding.viewPager.getAdapter().notifyDataSetChanged();
  }
  
  @Override
  public void onDestroyView(){
    super.onDestroyView();
    binding = null;
  }
  
  @Override
  public void onBackPressed(){
    context.finish();
  }
}