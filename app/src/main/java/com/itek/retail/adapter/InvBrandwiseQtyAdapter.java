package com.itek.retail.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.ListHuscanArticalwiseBinding;
import com.itek.retail.model.BrandEans;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inward.grn.StoreTripDetailsData;

import java.util.ArrayList;
import java.util.List;

public class InvBrandwiseQtyAdapter extends RecyclerView.Adapter<InvBrandwiseQtyAdapter.ViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private List<BrandEans> listBrandEans = new ArrayList<>(0);
  
  public InvBrandwiseQtyAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<BrandEans> listBrandEans){
    this.context = context;
    this.frag = frag;
    this.listBrandEans = listBrandEans;
  }
  
  public BrandEans getItem(int position){ return position >= 0 ? listBrandEans.get(position) : null; }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    ListHuscanArticalwiseBinding binding = ListHuscanArticalwiseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }
  
  @Override
  public long getItemId(int position){ return position; }
  
  @Override
  public int getItemCount(){ return listBrandEans.size() + 1; }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position){
    final BrandEans itemModel = getItem(position - 1);
    holder.itemView.setBackgroundResource(itemModel != null && position % 2 != 0 ? R.color.graywhite : R.color.white);
    holder.textScanEanNo.setText(itemModel != null ? itemModel.getBrandName() : context.getString(R.string.lbl_brand));
    holder.textScanQty.setText(itemModel != null ? "" + itemModel.getTotalQty() : context.getString(R.string.lbl_exp_qty));
    holder.textScanEanQty.setText(itemModel != null ? "" + itemModel.getScanQty() : context.getString(R.string.lbl_scan_qty));
    holder.imgeanInfo.setVisibility(View.GONE);
    holder.imgeanInfo.setImageResource(R.drawable.ic_list_info);
  }
  
  // stores and recycles views as they are scrolled off screen
  public class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView textScanEanNo;
    TextView textScanQty;
    TextView textScanEanQty;
    ImageView imgeanInfo;
    ListHuscanArticalwiseBinding binding;
    
    ViewHolder(ListHuscanArticalwiseBinding binding){
      super(binding.getRoot());
      textScanEanNo = binding.textScanEanNo;
      textScanQty = binding.textScanQty;
      textScanEanQty = binding.textScanEanQty;
      imgeanInfo = binding.imgeanInfo;
    }
    
    public void bind(final StoreTripDetailsData itemModel){
      binding.setStoreTripDetailsData(itemModel);
      binding.executePendingBindings();
    }
  }
}
