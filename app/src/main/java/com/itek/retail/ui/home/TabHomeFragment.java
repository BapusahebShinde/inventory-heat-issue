package com.itek.retail.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;

import com.itek.retail.adapter.DashboardAdapter;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.FragmentTabHomeBinding;

/**
 * The Tab home fragment.
 */
public class TabHomeFragment extends CommonFragment{
  
  private FragmentTabHomeBinding binding;
  
  /**
   * Instantiates a new Tab home fragment.
   */
  public TabHomeFragment(){/*Default/Empty Constructor*/}
  
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    binding = FragmentTabHomeBinding.inflate(inflater, container, false);
    binding.gridMenusHome.setAdapter(new DashboardAdapter((MainActivity) context, this, AppDatabase.getMenuDao(context).getAllHomeMenus()));
    binding.gridMenusHome.setLayoutManager(new GridLayoutManager(context, isLandscape ? 5 : 3));
    return binding.getRoot();
  }
}