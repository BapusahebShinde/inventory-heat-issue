/*
package com.itek.retail.ui.drawermenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.FragmentDrawerMenuConfigBinding;

public class DrawerMenuConfigFragment extends CommonFragment{
  
  private DrawerMenuConfigViewModel mViewModel;
  private FragmentDrawerMenuConfigBinding binding;
  
  public DrawerMenuConfigFragment(){
    // Required empty public constructor
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(DrawerMenuConfigViewModel.class);
    binding = FragmentDrawerMenuConfigBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }
  
  @Override
  public void onDetach(){
    clearBackStack();
    super.onDetach();
  }
}*/
