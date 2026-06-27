package com.itek.retail.ui.outward.huverification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.adapter.StoreOutwardDataAdapter;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.FragmentOutwardHuListBinding;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.home.MainActivity;

import java.util.ArrayList;

/**
 * The Outward trips fragment.
 */
public class OutwardHuListFragment extends CommonFragment{
  
  private FragmentOutwardHuListBinding binding;
  private OutwardHuListViewModel storeOutwardViewModel;
  private TripStatusDao tripStatusDao;
  private String tripType = "";
  private ArrayList<TripStatus> dataList = new ArrayList<>(0);
  
  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    tripStatusDao = AppDatabase.getDbInstance(this.context).TripStatusDao();
  }
  
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    binding = FragmentOutwardHuListBinding.inflate(inflater, container, false);
    
    binding.list.setAdapter(new StoreOutwardDataAdapter((MainActivity) context, this, dataList));
    binding.list.setLayoutManager(new LinearLayoutManager(context));
    
    tripType = AppCommonMethods.extractString(getArguments(), AppConstants.TRIP_TYPE, AppCommonMethods.isStaticDebug() ? "dc_to_store" : "");
    if(AppCommonMethods.isNullOrEmpty(tripType)) popBackStack();
    
    try{
      tripStatusDao.getTripTypeList(tripType, AppConstants.OUTWARD).observe(getViewLifecycleOwner(), storeOutwardDataList -> {
        if(storeOutwardDataList != null && storeOutwardDataList.size() > 0){
          if(dataList != null){
            dataList.clear();
            dataList.addAll(storeOutwardDataList);
            if(dataList.size() > 0){
              binding.llHeader.setVisibility(View.VISIBLE);
              binding.list.setVisibility(View.VISIBLE);
              binding.textNoData.setVisibility(View.GONE);
              
            }
            else{
              binding.llHeader.setVisibility(View.GONE);
              binding.list.setVisibility(View.GONE);
              binding.textNoData.setVisibility(View.VISIBLE);
            }
          }
          else{
            binding.llHeader.setVisibility(View.GONE);
            binding.list.setVisibility(View.GONE);
            binding.textNoData.setVisibility(View.VISIBLE);
          }
        }
        else{
          binding.llHeader.setVisibility(View.GONE);
          binding.list.setVisibility(View.GONE);
          binding.textNoData.setVisibility(View.VISIBLE);
        }
        if(binding != null && binding.list != null && binding.list.getAdapter() != null)
          ((RecyclerView.Adapter) binding.list.getAdapter()).notifyDataSetChanged();
        
      });
    }
    catch(Exception e){ e.printStackTrace(); }
    
    return binding.getRoot();
  }
  
  @Override
  public void onDestroyView(){
    super.onDestroyView();
    tripStatusDao.getTripTypeList(tripType, AppConstants.OUTWARD).removeObservers(getViewLifecycleOwner());
  }
}