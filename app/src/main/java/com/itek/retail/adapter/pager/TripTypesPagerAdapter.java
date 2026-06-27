package com.itek.retail.adapter.pager;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.ui.inward.grn.InwardTripsFragment;
import com.itek.retail.ui.outward.OutwardPickDataFragment;
import com.itek.retail.ui.outward.OutwardPickListsFragment;
import com.itek.retail.ui.outward.huverification.OutwardHuDataFragment;
import com.itek.retail.ui.outward.huverification.OutwardHuListFragment;

import java.util.List;

/**
 * The Inward pager adapter.
 */
public class TripTypesPagerAdapter extends FragmentStateAdapter{
  
  private List<String> listTripTypeTabs;
  private CommonFragment frag;
  private boolean isOutwardPick = false;
  private boolean isOutward = false;
  
  /**
   * Instantiates a new Inward pager adapter.
   *
   * @param fragment         the fragment
   * @param listTripTypeTabs the list trip type tabs
   */
  public TripTypesPagerAdapter(@NonNull CommonFragment fragment, List<String> listTripTypeTabs){
    super(fragment);
    this.frag = fragment;
    this.listTripTypeTabs = listTripTypeTabs;
    this.isOutwardPick = fragment instanceof OutwardPickDataFragment;
    this.isOutward = fragment instanceof OutwardHuDataFragment;
  }
  
  @NonNull
  @Override
  public Fragment createFragment(int position){
    Bundle args = chkNull(frag.getArguments(), new Bundle());
    args.putString(AppConstants.TRIP_TYPE, listTripTypeTabs.get(position).trim());
    if(isOutwardPick){
      OutwardPickListsFragment outwardPickTripsFragment = new OutwardPickListsFragment();
      outwardPickTripsFragment.setArguments(args);
      return outwardPickTripsFragment;
    }
    else if(isOutward){
      OutwardHuListFragment outwardTripsFragment = new OutwardHuListFragment();
      outwardTripsFragment.setArguments(args);
      return outwardTripsFragment;
    }
    else{
      InwardTripsFragment inwardTripsFragment = new InwardTripsFragment();
      inwardTripsFragment.setArguments(args);
      return inwardTripsFragment;
    }
  }
  
  @Override
  public int getItemCount(){
    return listTripTypeTabs.size();
  }
}
