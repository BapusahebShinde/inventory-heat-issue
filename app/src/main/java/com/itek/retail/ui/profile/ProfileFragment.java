/*
package com.itek.retail.ui.profile;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.R;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.FragmentProfileBinding;
import com.itek.retail.ui.home.MainActivity;

public class ProfileFragment extends CommonFragment{
  
  private ProfileViewModel profileViewModel;
  private FragmentProfileBinding binding;
  
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    ((MainActivity) context).lockDrawer(true);
    profileViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ProfileViewModel.class);
    binding = FragmentProfileBinding.inflate(inflater, container, false);
    binding.helloUser.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_hello_user), chkNull(SharedPrefManager.getUserName(), ""))));
    binding.txtStoreId.setText(HtmlCompat.fromHtml(String.format(getString(R.string.txt_store_id), chkNull(SharedPrefManager.getStoreID(), "0"))));
    //binding.imgUser
    context.loadImage(binding.imgProfileUser, SharedPrefManager.getUserProfileUrl(), R.drawable.ic_temp_person);
    binding.button.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        context.getSupportFragmentManager().popBackStack(ProfileFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
      }
    });
    return binding.getRoot();
  }
  
  @Override
  public void onDetach(){
    ((MainActivity) context).lockDrawer(false);
    super.onDetach();
  }
  
  @Override
  public void onDestroyView(){
    super.onDestroyView();
    binding = null;
  }
  
  @Override
  public void onBackPressed(){}
}*/
