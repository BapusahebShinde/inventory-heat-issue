package com.itek.retail.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.databinding.HuDataAdapterBinding;
import com.itek.retail.model.HUStatus;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.inward1.TripHUListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The Trip list adapter.
 */
public class HUListAdapter extends RecyclerView.Adapter<HUListAdapter.ViewHolder>{
  
  private CommonActivity context;
  private TripHUListFragment frag;
  private List<HUStatus> listMenus = new ArrayList<>(0);
  private boolean isShowInfoIcon = false;
  private boolean isShowActionIcon = false;
  private boolean isAlert = false;
  
  /**
   * Instantiates a new Store inward data adapter.
   *
   * @param context   the context
   * @param listMenus the list menus
   */
  public HUListAdapter(@NonNull CommonActivity context, @NonNull TripHUListFragment frag, List<HUStatus> listMenus, boolean isShowInfoIcon, boolean isShowActionIcon, boolean isAlert){
    this.context = context;
    this.frag = frag;
    this.isShowInfoIcon = isShowInfoIcon;
    this.isShowActionIcon = isShowActionIcon;
    this.isAlert = isAlert;
    this.listMenus = listMenus;
  }
  
  /**
   * Get item trip status.
   *
   * @param position the position
   * @return the trip status
   */
  public HUStatus getItem(int position){
    return listMenus.get(position);
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    HuDataAdapterBinding binding = HuDataAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
    final HUStatus itemModel = getItem(position);
    if(itemModel == null) return;
    holder.txtHuNumber.setShowOrderIcon(itemModel == null);
    holder.txtHuExpQty.setShowOrderIcon(itemModel == null);
    holder.txtHuScanQty.setShowOrderIcon(itemModel == null);
    holder.textStatus.setShowOrderIcon(itemModel == null);
    
    final boolean hasReason = itemModel.reason != null && itemModel.reason.trim().length() > 0;
    holder.txtHuNumber.setText(itemModel.getHuNumber());
    holder.txtHuExpQty.setText("" + itemModel.getExpQty());
    holder.txtHuScanQty.setText("" + itemModel.getScanQty());// + "/" + itemModel.getExpQty());
    holder.txtHuScanQty.setTextColor(ContextCompat.getColor(context, getColorIdByQty(itemModel.getExpQty(), itemModel.getScanQty())));
    holder.textStatus.setText(getFullStringByStatus(itemModel.getStatus()));
    holder.textStatus.setTextColor(ContextCompat.getColor(context, getColorIdByStatus(itemModel.getStatus())));
    
    holder.imgInfo.setVisibility(isShowInfoIcon ? View.VISIBLE : View.GONE);
    holder.imgAction.setVisibility(isShowActionIcon ? View.VISIBLE : View.GONE);
    
    holder.itemView.setOnClickListener(v -> {
      if(v != null && v.getVisibility() == ViewGroup.VISIBLE && frag != null && !isAlert)
        frag.processHuStatus(itemModel);
    });
    
    holder.imgInfo.setOnClickListener(v -> {
      if(v != null && v.getVisibility() == ViewGroup.VISIBLE && frag != null)
        frag.processHuStatus(itemModel, true);
    });
    
    holder.imgAction.setOnClickListener(v -> {
      if(v != null && v.getVisibility() == ViewGroup.VISIBLE && frag != null && !isAlert)
        frag.processHuStatus(itemModel);
    });
  }
  
  private String getFullStringByStatus(final String status){
    switch(status){
      case AppConstants.HU_STATUS_ACCEPT:
      case AppConstants.K_HU_STATUS_ACCEPT:
      case AppConstants.HU_STATUS_COMPLETE:
      case AppConstants.STATUS_COMPLETE:
      case AppConstants.STATUS_COMPLETED:
        return AppConstants.STATUS_COMPLETE;
      case AppConstants.HU_STATUS_IN_PROGRESS:
      case AppConstants.TRIP_STATUS_IN_PROGRESS:
        return AppConstants.TRIP_STATUS_IN_PROGRESS;
      case AppConstants.HU_STATUS_PENDING:
      case AppConstants.TRIP_STATUS_PENDING:
        return AppConstants.TRIP_STATUS_PENDING;
      case AppConstants.HU_STATUS_REJECT:
      case AppConstants.K_HU_STATUS_REJECT:
        return AppConstants.K_HU_STATUS_REJECT;
      default:
        return status;
    }
  }
  
  private int getColorIdByStatus(final String status){
    switch(status){
      case AppConstants.HU_STATUS_ACCEPT:
      case AppConstants.K_HU_STATUS_ACCEPT:
      case AppConstants.HU_STATUS_COMPLETE:
      case AppConstants.STATUS_COMPLETE:
      case AppConstants.STATUS_COMPLETED:
        return R.color.green;
      case AppConstants.HU_STATUS_IN_PROGRESS:
      case AppConstants.TRIP_STATUS_IN_PROGRESS:
        return R.color.gold;
      case AppConstants.HU_STATUS_PENDING:
      case AppConstants.TRIP_STATUS_PENDING:
        return R.color.txt_regular;
      default:
        return R.color.red;
    }
  }
  
  private int getColorIdByQty(final int expQty, final int scanQty){
    if(scanQty == 0) return R.color.txt_regular;
    if(scanQty > 0 && scanQty == expQty) return R.color.green;
    if(scanQty >= 0 && (scanQty < expQty || scanQty > expQty)) return R.color.red;
    return R.color.txt_regular;
  }
  
  /**
   * The View holder.
   */
  
  public class ViewHolder extends RecyclerView.ViewHolder{
    
    SortHeaderView txtHuNumber;
    SortHeaderView txtHuExpQty;
    SortHeaderView txtHuScanQty;
    SortHeaderView textStatus;
    ImageButton imgInfo;
    ImageButton imgAction;
    HuDataAdapterBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(HuDataAdapterBinding binding){
      super(binding.getRoot());
      txtHuNumber = binding.txtHuNumber;
      txtHuExpQty = binding.txtHuExpQty;
      txtHuScanQty = binding.txtHuScanQty;
      textStatus = binding.txtHuStatus;
      imgInfo = binding.imgInfo;
      imgAction = binding.imgAction;
    }
    
    /**
     * Bind.
     *
     * @param itemModel the item model
     */
    public void bind(final HUStatus itemModel){
      binding.setHuStatus(itemModel);
      binding.executePendingBindings();
    }
  }
}