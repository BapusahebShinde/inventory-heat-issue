package com.itek.retail.ui.than;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.itek.retail.adapter.DashboardAdapter;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.FragmentSearchMainBinding;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.SearchMainViewModel;

/**
 * The Search main fragment.
 */
public class ThanMainFragment extends CommonFragment{
  
  private ThanViewModel mViewModel;
  private FragmentSearchMainBinding binding;
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ThanViewModel.class);
    binding = FragmentSearchMainBinding.inflate(inflater, container, false);
    binding.gridMenusSearch.setAdapter(new DashboardAdapter((MainActivity) context, this, AppDatabase.getMenuDao(context).getSubMenus(getMenuModel().getMenuId())));
    binding.gridMenusSearch.setLayoutManager(new GridLayoutManager(context, isLandscape ? 5 : 3));
    return binding.getRoot();
  }
  
}