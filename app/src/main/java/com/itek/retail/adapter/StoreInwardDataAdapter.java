package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.StoreInwardDataAdapterBinding;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inward.grn.InwardGrnTripDetailsFragment;
import com.itek.retail.ui.inward.huverification.InwardHuVerificationFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Store inward data adapter.
 */
public class StoreInwardDataAdapter extends RecyclerView.Adapter<StoreInwardDataAdapter.ViewHolder>{
  
  TripStatusDao tripStatusDao;
  TripInventoryDao tripInventoryDao;
  private MainActivity context;
  private CommonFragment frag;
  private List<TripStatus> listMenus = new ArrayList<>(0);
  
  /**
   * Instantiates a new Store inward data adapter.
   *
   * @param context   the context
   * @param listMenus the list menus
   */
  public StoreInwardDataAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<TripStatus> listMenus){
    this.context = context;
    this.frag = frag;
    this.listMenus = listMenus;
    tripStatusDao = AppDatabase.getDbInstance(context).TripStatusDao();
    tripInventoryDao = AppDatabase.getDbInstance(context).TripInventoryDao();
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
    StoreInwardDataAdapterBinding binding = StoreInwardDataAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position){
    final TripStatus itemModel = getItem(position);
    if(itemModel == null) return;
    final boolean hasReason = itemModel.reason != null && itemModel.reason.trim().length() > 0;
    holder.textTripNumber.setText(itemModel.getTripNumber());
    holder.textTripNumber.setTextColor(ContextCompat.getColor(context, R.color.black));
    holder.textVerifiedHu.setText("" + itemModel.getVerifiedHu() + "/" + itemModel.getNumberOfHu());
    holder.textVerifiedHu.setTextColor(ContextCompat.getColor(context, R.color.black));
    holder.textStatus.setText(itemModel.getStatus());
    if(itemModel.getStatus().equalsIgnoreCase(AppConstants.TRIP_STATUS_COMPLETED)){
      holder.imgAction.setImageResource(hasReason ? R.drawable.ic_delete : R.color.transparent);
      holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
    }
    else if(itemModel.getStatus().equalsIgnoreCase(AppConstants.TRIP_STATUS_IN_PROGRESS)){
      holder.imgAction.setImageResource(R.drawable.ic_delete);
      holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.gold));
      
    }
    else{
      holder.imgAction.setImageResource(R.color.transparent);
      holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
    }
    
    int noofhu = itemModel.getNumberOfHu();
    int verifiedhu = itemModel.getVerifiedHu();
    
    if(noofhu != verifiedhu){
      holder.textVerifiedHu.setTextColor(ContextCompat.getColor(context, R.color.red));
    }
    
    holder.itemView.setOnClickListener(v -> {
      //Load/Redirect to specific fragment
      if((noofhu <= 0 || noofhu != verifiedhu) && itemModel.isHuProcessCompulsion()){
        if(noofhu <= 0)
          context.showCustomAlertDialog(null, "" + context.getResources().getString(R.string.err_msg_error_null), R.string.btn_ok);
        else if(noofhu < verifiedhu){
          context.showCustomAlertDialog(null, String.format(context.getResources().getString(R.string.err_msg_hu_more), "" + (verifiedhu - noofhu)), R.string.btn_ok);
        }
        else{
          context.showCustomAlertDialog("", String.format(context.getResources().getString(R.string.err_msg_hu_less_cannot_proceed), "" + (noofhu - verifiedhu)), R.string.btn_verify_hu, (dialog, which) -> {
            InwardHuVerificationFragment huVerificationFragment = new InwardHuVerificationFragment();
            Bundle args = new Bundle();
            args.putString(AppConstants.TRIP_NUMBER, itemModel.tripNumber);
            huVerificationFragment.setArguments(args);
            context.loadFragment(new InwardHuVerificationFragment(), args);
          }, R.string.btn_cancel, null);
          
        }
      }
      else{
        if(itemModel.getStatus().equalsIgnoreCase(AppConstants.TRIP_STATUS_COMPLETED)){
          context.showCustomErrDialog(context.getResources().getString(R.string.err_msg_trip_complete));
        }
        else if(tripInventoryDao.getTripInventoryDataSize(itemModel.tripNumber) <= 0){
          context.showCustomErrDialog(context.getResources().getString(R.string.err_msg_error_null));
        }
        else{
          Bundle arg = chkNull(frag.getArguments(), new Bundle());
          arg.putSerializable(itemModel.getClass().getSimpleName(), (Serializable) itemModel);
          context.loadFragment(new InwardGrnTripDetailsFragment(), arg);
          
        }
      }
    });
    
    holder.imgAction.setOnClickListener(v -> {
      if(itemModel.getStatus().equalsIgnoreCase(AppConstants.TRIP_STATUS_PENDING)){
        holder.itemView.performClick();
      }
      else{
        context.showCustomAlertDialog("", String.format(context.getString(R.string.msg_inward_delete_trip), itemModel.tripNumber), context.getString(R.string.btn_cancel), null, context.getString(R.string.btn_delete_data), new DialogInterface.OnClickListener(){
          @Override
          public void onClick(DialogInterface dialogInterface, int i){
            tripInventoryDao.deleteTripInventoryData(itemModel.tripNumber);
            tripInventoryDao.resetTripStatus(itemModel.tripNumber);
            tripStatusDao.updatePendingTripStatus(itemModel.tripNumber, AppConstants.INWARD);
            
          }
        });
      }
    });
    
    holder.textStatus.setOnClickListener(v -> {
      if(!itemModel.getStatus().equalsIgnoreCase(AppConstants.TRIP_STATUS_COMPLETED)){
        holder.itemView.performClick();
      }
    });
  }
  
  /**
   * The View holder.
   */
  
  public class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView textTripNumber;
    TextView textVerifiedHu;
    TextView textStatus;
    ImageButton imgAction;
    StoreInwardDataAdapterBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(StoreInwardDataAdapterBinding binding){
      super(binding.getRoot());
      textTripNumber = binding.textTripNumber;
      textVerifiedHu = binding.textNoOfHu;
      imgAction = binding.imgAction;
      textStatus = binding.textVerifiedHu;
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