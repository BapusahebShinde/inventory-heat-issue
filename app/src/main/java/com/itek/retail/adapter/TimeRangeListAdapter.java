package com.itek.retail.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.ListTimeViewBinding;
import com.itek.retail.model.LabelCounts;
import com.itek.retail.ui.home.MainActivity;

import java.util.List;

/**
 * The Time range list adapter.
 * used in Dashboard screens for showing List of Time-Slot-wise values
 * against Total/Completed/Pending Statuses
 */
public class TimeRangeListAdapter extends RecyclerView.Adapter<TimeRangeListAdapter.MyViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private List<LabelCounts> listHourlyCount;
  
  /**
   * Instantiates a new Time range list adapter.
   *
   * @param context         the context
   * @param listHourlyCount the list hourly count
   */
  public TimeRangeListAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<LabelCounts> listHourlyCount){
    this.context = context;
    this.frag = frag;
    this.listHourlyCount = listHourlyCount;
  }
  
  @NonNull
  @Override
  public TimeRangeListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    return new TimeRangeListAdapter.MyViewHolder(ListTimeViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public void onBindViewHolder(TimeRangeListAdapter.MyViewHolder holder, final int position){
    final LabelCounts itemModel = listHourlyCount.get(position);
    if(itemModel == null) return;
    holder.txtHrs.setText(HtmlCompat.fromHtml(itemModel.getLabel(), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtHrsCount.setText(HtmlCompat.fromHtml(itemModel.getCountsStr(), HtmlCompat.FROM_HTML_MODE_LEGACY));
  }
  
  @Override
  public int getItemCount(){
    return listHourlyCount.size();
  }
  
  /**
   * The My view holder.
   */
  static class MyViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtHrs, txtHrsCount;
    
    /**
     * Instantiates a new My view holder.
     *
     * @param binding the binding
     */
    MyViewHolder(@NonNull ListTimeViewBinding binding){
      super(binding.getRoot());
      txtHrs = binding.txtHrs;
      txtHrsCount = binding.txtHrsCount;
      txtHrs.setSelected(true);
      txtHrsCount.setSelected(true);
    }
  }
}