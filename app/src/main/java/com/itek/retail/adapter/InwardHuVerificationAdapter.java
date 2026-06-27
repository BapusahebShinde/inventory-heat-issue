package com.itek.retail.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.databinding.ListInwardHuVarificationBinding;
import com.itek.retail.model.InwardHuVerificationModel;

import java.util.ArrayList;
import java.util.List;

/**
 * The Inward hu verification adapter.
 */
public class InwardHuVerificationAdapter extends RecyclerView.Adapter<InwardHuVerificationAdapter.ViewHolder>{
  
  Context context;
  private List<InwardHuVerificationModel> listMenus = new ArrayList<>(0);
  
  /**
   * Instantiates a new Inward hu verification adapter.
   *
   * @param listMenus the list menus
   * @param context   the context
   */
  public InwardHuVerificationAdapter(List<InwardHuVerificationModel> listMenus, Context context){
    this.listMenus = listMenus;
    this.context = context;
  }
  
  @NonNull
  @Override
  public InwardHuVerificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    ListInwardHuVarificationBinding binding = ListInwardHuVarificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    
    return new InwardHuVerificationAdapter.ViewHolder(binding);
  }
  
  @Override
  public void onBindViewHolder(@NonNull InwardHuVerificationAdapter.ViewHolder holder, int position){
    final InwardHuVerificationModel inwardHuVerificationViewModel = listMenus.get(position);
    if(inwardHuVerificationViewModel != null){
      holder.huNumber.setText("" + inwardHuVerificationViewModel.getHuNumbers());
      holder.status.setImageResource(inwardHuVerificationViewModel.getStatus().trim().equalsIgnoreCase("Verified") ? R.drawable.ic_ok : R.color.transparent);
    }
  }
  
  @Override
  public int getItemCount(){
    return listMenus.size();
  }
  
  /**
   * The View holder.
   */
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    public TextView huNumber;
    public ImageView status;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ListInwardHuVarificationBinding binding){
      super(binding.getRoot());
      huNumber = binding.txtInwardHuVerificationHuNumbers;
      status = binding.txtInwardHuVerificationItemStatus;
    }
  }
}