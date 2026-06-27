package com.itek.retail.ui.search.ageing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.FragmentAgeingSearchBinding;
import com.itek.retail.ui.home.MainViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AgeingSearchFragment extends CommonFragment{
  
  private final AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.SEARCH_AGEING;
  private FragmentAgeingSearchBinding binding;
  private MainViewModel mainViewModel;
  private AgeingSearchViewModel mViewModel;
  private List<String> listAgeing = new ArrayList<>(0);
  
  public static AgeingSearchFragment newInstance(){
    return new AgeingSearchFragment();
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(AgeingSearchViewModel.class);
    binding = FragmentAgeingSearchBinding.inflate(inflater, container, false);
    setSpinners();
    
    binding.btnSearchProduct.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(v != null && v.getVisibility() == View.VISIBLE && binding.selectSearchAgeing.validate()){
          Bundle args = getArguments();
          args.putString(AppConstants.AGEING_BUCKET, binding.selectSearchAgeing.getSelectedItem());
          context.loadFragment(new AgeingMainFragment(), args);
        }
      }
    });
    
    return binding.getRoot();
  }
  
  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState){
    super.onActivityCreated(savedInstanceState);
    mViewModel = new ViewModelProvider(this).get(AgeingSearchViewModel.class);
    // TODO: Use the ViewModel
  }
  
  /**
   * Set spinners.
   */
  void setSpinners(){
    listAgeing.clear();
    List<String> listAgeing = new ArrayList<>(0);
    listAgeing.addAll(Arrays.asList(getResources().getStringArray(R.array.Ageing)));
    /*if(AppCommonMethods.isNonEmpty(listAgeing))
      listAgeing.add(0, String.format(getString(R.string.header_select), binding.selectSearchAgeing.getLabel()));*/
    
    // if(isNonEmpty(listAgeing)) listAgeing.addAll(listAgeing);
    //if(listAgeing != null) listAgeing.add(0, AppConstants.ALL);
    binding.selectSearchAgeing.setAdapter(listAgeing,1);
  }
  
  @Override
  public void onBackPressed(){
    super.onBackPressed();
  }
  
}