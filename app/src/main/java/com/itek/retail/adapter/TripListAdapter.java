package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.TripDataAdapterBinding;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.inward1.TripListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The Trip list adapter.
 */
public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.ViewHolder>{
  
  private CommonActivity context;
  private CommonFragment frag;
  private List<TripStatus> listMenus = new ArrayList<>(0);
  
  /**
   * Instantiates a new Store inward data adapter.
   *
   * @param context   the context
   * @param listMenus the list menus
   */
  public TripListAdapter(@NonNull CommonActivity context, @NonNull CommonFragment frag, List<TripStatus> listMenus){
    this.context = context;
    this.frag = frag;
    this.listMenus = listMenus;
  }
  
  /**
   * Get item trip status.
   *
   * @param position the position
   * @return the trip status
   */
  public TripStatus getItem(int position){
    return listMenus.get(position);
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    TripDataAdapterBinding binding = TripDataAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }
  
  @Override
  public long getItemId(int position){
    return position;
  }
  
  @Override
  public int getItemCount(){
    return listMenus.size();
  }
  
  //binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position){
    final TripStatus itemModel = getItem(position);
    if(itemModel == null) return;
    holder.textTripNumber.setShowOrderIcon(itemModel == null);
    holder.textHuCount.setShowOrderIcon(itemModel == null);
    holder.txtCompletedHuCount.setShowOrderIcon(itemModel == null);
    holder.txtVerifiedHuCount.setShowOrderIcon(itemModel == null);
    holder.textStatus.setShowOrderIcon(itemModel == null);
    
    final boolean hasReason = itemModel.reason != null && itemModel.reason.trim().length() > 0;
    //holder.textTripNumber.setText(chkNull(itemModel.getTripNumber(), itemModel.getRefTripNumber()));
    holder.textTripNumber.setText(chkNull(itemModel.getRefTripNumber(), itemModel.getTripNumber()));
    holder.textTripNumber.setTextColor(ContextCompat.getColor(context, !itemModel.isManualTrip() && isNullOrEmpty(itemModel.getRefTripNumber()) ? R.color.txtRed : R.color.txt_regular));
    holder.textHuCount.setText("" + itemModel.getNumberOfHu());
    holder.textStatus.setText("" + itemModel.getStatus());//+(AppCommonMethods.isDebugApp?"_"+itemModel.getTripDateTime():""));
    holder.txtCompletedHuCount.setText("" + itemModel.getCompletedHu() + "/" + itemModel.getNumberOfHu());
    //holder.txtCompletedHuCount.setTextColor(ContextCompat.getColor(context, R.color.black));
    holder.textHuCount.setVisibility(AppCommonMethods.isShowTotalAndCompletedCount ? View.VISIBLE : View.GONE);
    holder.textStatus.setTextColor(ContextCompat.getColor(context, getColorIdByStatus(itemModel.getStatus())));
    
    int noofhu = itemModel.getNumberOfHu();
    int verifiedhu = itemModel.getVerifiedHu();
    holder.txtVerifiedHuCount.setText("" + itemModel.getVerifiedHu() + "/" + itemModel.getNumberOfHu());
    holder.txtVerifiedHuCount.setTextColor(ContextCompat.getColor(context, noofhu == verifiedhu ? R.color.green : R.color.red));
    
    holder.itemView.setOnClickListener(v -> {
      //Process Selected Trip
      if(frag != null && frag instanceof TripListFragment)
        ((TripListFragment) frag).processSelectedTrip(itemModel);
      /*if(noofhu <= 0 && !itemModel.isManualTrip())
        context.showCustomAlertDialog(null, "" + context.getResources().getString(R.string.err_msg_error_null), R.string.btn_ok);
      else if(noofhu != verifiedhu && itemModel.isHuProcessCompulsion()){
        if(noofhu < verifiedhu)
          context.showCustomAlertDialog(null, String.format(context.getResources().getString(R.string.err_msg_hu_more), "" + (verifiedhu - noofhu)), R.string.btn_ok);
        else
          context.showCustomAlertDialog("", String.format(context.getResources().getString(R.string.err_msg_hu_less_cannot_proceed), "" + (noofhu - verifiedhu)), R.string.btn_verify_hu, (dialog, which) -> {
            InwardHuVerificationFragment huVerificationFragment = new InwardHuVerificationFragment();
            Bundle args = new Bundle();
            args.putString(AppConstants.TRIP_NUMBER, itemModel.getTripNumber());
            huVerificationFragment.setArguments(args);
            context.loadFragment(new InwardHuVerificationFragment(), args);
          }, R.string.btn_cancel, null);
      }
      else{
        if(itemModel.getStatus().equalsIgnoreCase(AppConstants.TRIP_STATUS_COMPLETED))
          context.showCustomErrDialog(context.getResources().getString(R.string.err_msg_trip_complete));
        //else if(AppCommonMethods.isNonEmpty(itemModel.getRefTripNumber()) && itemModel.getRefTripNumber().equalsIgnoreCase(chkNull(itemModel.getTripNumber(), itemModel.getRefTripNumber())))
        else if(!itemModel.isManualTrip() && AppCommonMethods.isNullOrEmpty(itemModel.getRefTripNumber()))// && itemModel.getRefTripNumber().equalsIgnoreCase(chkNull(itemModel.getTripNumber(), itemModel.getRefTripNumber())))
          frag.showTripNumInputAlert(itemModel);
        else
          frag.callTripHuDetailsAPI(itemModel);
      }*/
    });
    
    holder.imgAction.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        holder.itemView.performClick();
      }
    });
  }
  
  private int getColorIdByStatus(final String status){
    switch(status){
      case AppConstants.TRIP_STATUS_COMPLETED:
        return R.color.green;
      case AppConstants.TRIP_STATUS_IN_PROGRESS:
        return R.color.txtBlue;
      case AppConstants.TRIP_STATUS_PENDING:
        return R.color.txt_regular;
      default:
        return R.color.red;
    }
  }
  
  /**
   * The View holder.
   */
  
  public class ViewHolder extends RecyclerView.ViewHolder{
    
    SortHeaderView textTripNumber;
    SortHeaderView textHuCount;
    SortHeaderView txtCompletedHuCount;
    SortHeaderView txtVerifiedHuCount;
    SortHeaderView textStatus;
    ImageButton imgAction;
    TripDataAdapterBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(TripDataAdapterBinding binding){
      super(binding.getRoot());
      textTripNumber = binding.textTripNumber;
      textHuCount = binding.textHuCount;
      txtCompletedHuCount = binding.txtCompletedHuCount;
      txtVerifiedHuCount = binding.txtVerifiedHuCount;
      textStatus = binding.txtTripStatus;
      imgAction = binding.imgAction;
    }
    
    /**
     * Bind.
     *
     * @param itemModel the item model
     */
    public void bind(final TripStatus itemModel){
      binding.setTripStatus(itemModel);
      binding.executePendingBindings();
    }
  }
}