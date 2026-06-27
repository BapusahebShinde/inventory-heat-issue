package com.itek.retail.ui.actionmenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.FragmentActionMenuChatBinding;

/**
 * The Action menu msg fragment.
 */
public class ActionMenuMsgFragment extends CommonFragment{
  
  private ActionMenuMsgViewModel mViewModel;
  private FragmentActionMenuChatBinding binding;
  
  /**
   * Instantiates a new Action menu msg fragment.
   */
  public ActionMenuMsgFragment(){
    // Required empty public constructor
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ActionMenuMsgViewModel.class);
    binding = FragmentActionMenuChatBinding.inflate(inflater, container, false);
    
    return binding.getRoot();
  }
}