package com.itek.retail.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.ViewEanQtyBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.ui.home.MainActivity;

import java.util.List;

public class ScanCountAdapter extends RecyclerView.Adapter<ScanCountAdapter.ViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private List<EanQty> eanQtyList;
  
  public ScanCountAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<EanQty> list){
    this.context = context;
    this.frag = frag;
    this.eanQtyList = list;
  }
  
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    return new ViewHolder(ViewEanQtyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public void onBindViewHolder(ViewHolder holder, int position){
    final int pos = holder.getAdapterPosition();
    final EanQty itemModel = eanQtyList.get(position);
    if(itemModel == null) return;
    
    holder.txtEan.setText(itemModel.ean);
    holder.txtQty.setText("" + itemModel.eanQty);
    holder.itemView.setBackgroundResource(R.drawable.border_bottom);
    
  }
  
  @Override
  public int getItemCount(){
    return eanQtyList.size();
  }
  
  static class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtEan;
    
    TextView txtQty;
    
    ViewHolder(ViewEanQtyBinding binding){
      super(binding.getRoot());
      
      txtEan = binding.txtEan;
      
      txtQty = binding.txtQty;
      
      txtEan.setSelected(true);
      
      txtQty.setSelected(true);
      
    }
  }
}