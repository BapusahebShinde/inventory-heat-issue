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
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.ListHuscanArticalwiseBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.model.OutwardToteEans;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inward.grn.StoreTripDetailsData;

import java.util.ArrayList;
import java.util.List;

public class OutwardToteTypewiseEanQtyAdapter extends RecyclerView.Adapter<OutwardToteTypewiseEanQtyAdapter.ViewHolder>{
  
  private CommonActivity context;
  private CommonFragment frag;
  private List<EanQty> listOwtToteEans = new ArrayList<>(0);
  
  public OutwardToteTypewiseEanQtyAdapter(@NonNull CommonActivity context, @NonNull CommonFragment frag, List<EanQty> listOwtToteEans){
    this.context = context;
    this.frag = frag;
    this.listOwtToteEans = listOwtToteEans;
  }
  
  public EanQty getItem(int position){ return position >= 0 ? listOwtToteEans.get(position) : null; }
  
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
  public int getItemCount(){ return listOwtToteEans.size() + 1; }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position){
    final EanQty itemModel = getItem(position - 1);
    holder.itemView.setBackgroundResource(itemModel==null?R.color.light_green: itemModel != null && position % 2 != 0 ? R.color.graywhite : R.color.white);
    //holder.textScanEanNo.setText(itemModel != null ? itemModel.getToteType() : context.getString(R.string.lbl_out_tot_type));
    holder.textScanQty.setText(itemModel != null ? "" + itemModel.getEan() : context.getString(R.string.lbl_tote_ean));
    holder.textScanEanQty.setText(itemModel != null ? "" + itemModel.getEanQty() : context.getString(R.string.lbl_scan_qty));
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
      textScanEanNo.setVisibility(View.GONE);
    }
    
    public void bind(final StoreTripDetailsData itemModel){
      binding.setStoreTripDetailsData(itemModel);
      binding.executePendingBindings();
    }
  }
}
