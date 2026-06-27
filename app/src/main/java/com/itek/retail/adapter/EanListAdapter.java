package com.itek.retail.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.EncodingHistoryListBinding;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.than.ThanCuttingFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The Encoding History adapter
 * used in Encoding Dashboard and Start Encoding Screens (EncodingMainFragment & EncodingStartFragment)
 * for showing History of Encoded Tags as Ean-wise Quantity.
 * user can be redirected to 'Verify Encoding' Screen by clicking the 'RIFD' icon
 */
public class EanListAdapter extends RecyclerView.Adapter<EanListAdapter.ViewHolder>{
  
  boolean isThanClosure = false;
  boolean isThanCutting = false;
  private CommonActivity context;
  private CommonFragment frag;
  private List<ProductModel> listProducts = new ArrayList<>(0);
  
  /**
   * Instantiates a new Encoding start history adapter.
   *
   * @param context      the context
   * @param frag         the m frag
   * @param listProducts the list products
   */
  public EanListAdapter(@NonNull CommonActivity context, @NonNull CommonFragment frag, List<ProductModel> listProducts){
    this.context = context;
    this.listProducts = listProducts;
    this.frag = frag;
    if(frag instanceof ThanCuttingFragment){
      ThanCuttingFragment fragment = (ThanCuttingFragment) frag;
      this.isThanClosure = fragment.isThanClosure();
      this.isThanCutting = fragment.isThanCutting();
    }
  }
  
  /**
   * Get item ean qty.
   *
   * @param position the position
   * @return the ean qty
   */
  public ProductModel getItem(int position){
    return listProducts.get(position);
  }
  
  @Override
  @NonNull
  public EanListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new EanListAdapter.ViewHolder(EncodingHistoryListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public long getItemId(int position){
    return position;
  }
  
  @Override
  public int getItemCount(){ return listProducts.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull EanListAdapter.ViewHolder holder, int position){
    final ProductModel itemModel = getItem(position);
    if(itemModel == null) return;
    holder.itemView.setBackgroundResource((context.isLandscape ? (position % 4 == 0 || position % 4 == 3) : position % 2 == 0) ? R.color.bgListAlternet : R.color.white);
    if(isThanClosure || isThanCutting)
      holder.txtStatus.setText(itemModel.ean + " (" + (isThanClosure ? itemModel.getLengthClosure() : itemModel.getLengthCutting()) + " cm)");
    else holder.txtStatus.setText(itemModel.ean);
    holder.imgStatusCompleted.setVisibility(View.INVISIBLE);
    holder.imgStatusCompleted1.setVisibility(View.VISIBLE);
    
    holder.imgStatusCompleted1.setOnClickListener(view -> {
      if(frag instanceof ThanCuttingFragment && !((ThanCuttingFragment) frag).isProcessOn()){
        ((ThanCuttingFragment) frag).setupView(itemModel, true);
      }
    });
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    ImageView imgStatusCompleted;
    ImageView imgStatusCompleted1;
    TextView txtStatus;
    EncodingHistoryListBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(final EncodingHistoryListBinding binding){
      super(binding.getRoot());
      txtStatus = binding.txtHistoryItemTitle;
      imgStatusCompleted = binding.imgHistoryRfid;
      imgStatusCompleted1 = binding.imgHistoryInfo;
    }
  }
}
