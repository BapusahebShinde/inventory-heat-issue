package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.ListReplenishBatchBinding;
import com.itek.retail.model.ReplenishBatch;
import com.itek.retail.ui.replenishondemand.ReplenishmentArticleListFragment;

import java.util.List;

public class ReplenishBatchListAdapter extends RecyclerView.Adapter<ReplenishBatchListAdapter.MyViewHolder>{
  
  CommonActivity context;
  CommonFragment frag;
  private List<ReplenishBatch> replenishBatchList;
  
  /**
   * Instantiates a new Replenish batch list adapter.
   *
   * @param context            the CommonActivity
   * @param frag               the CommonFragment
   * @param replenishBatchList the replenish batch list
   */
  public ReplenishBatchListAdapter(@NonNull CommonActivity context, @NonNull CommonFragment frag, List<ReplenishBatch> replenishBatchList){
    this.context = context;
    this.frag = frag;
    this.replenishBatchList = replenishBatchList;
  }
  
  @Override
  public ReplenishBatchListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new ReplenishBatchListAdapter.MyViewHolder(ListReplenishBatchBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  /**
   * @param holder   The ViewHolder which should be updated to represent the contents of the
   *                 item at the given position in the data set.
   * @param position The position of the item within the adapter's data set.
   */
  @Override
  public void onBindViewHolder(@NonNull MyViewHolder holder, int position){
    final ReplenishBatch replenishBatch = replenishBatchList.get(position);
    if(replenishBatch == null) return;
    holder.txtBatchDate.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_date_time), replenishBatch.getBatchDate()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.txtBatchStatus.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_status), replenishBatch.getStatus()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.txtBatchTotalQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_total_qty), "" + replenishBatch.getTotalQty()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.txtBatchPickQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_picked_qty), "" + replenishBatch.getPickQty()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.itemView.setOnClickListener(!replenishBatch.isActive() ? null : new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(replenishBatch.isActive()){
          Bundle args = chkNull(frag.getArguments(), new Bundle());
          args.putSerializable(replenishBatch.getClass().getSimpleName(), replenishBatch);
          context.loadFragment(new ReplenishmentArticleListFragment(), args);
          //frag.callWebService(URLConstants.GET_REPLENISHMENT_BATCH_DETAILS,,args);
        }
      }
    });
    holder.itemView.setBackgroundResource(replenishBatch.isActive() ? R.drawable.border : R.drawable.border_disabled);
  }
  
  @Override
  public int getItemCount(){
    return replenishBatchList.size();
  }
  
  /**
   * The My view holder.
   */
  static class MyViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtBatchDate;
    TextView txtBatchStatus;
    TextView txtBatchTotalQty;
    TextView txtBatchPickQty;
    
    /**
     * Instantiates a new My view holder.
     *
     * @param binding the binding
     */
    MyViewHolder(ListReplenishBatchBinding binding){
      super(binding.getRoot());
      txtBatchDate = binding.txtReplenishBatchDate;
      txtBatchStatus = binding.txtReplenishBatchStatus;
      txtBatchTotalQty = binding.txtReplenishBatchTotalQty;
      txtBatchPickQty = binding.txtReplenishBatchPickedQty;
      
      txtBatchDate.setSelected(true);
      txtBatchStatus.setSelected(true);
      txtBatchTotalQty.setSelected(true);
      txtBatchPickQty.setSelected(true);
    }
  }
}
