package com.itek.retail.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.databinding.ListHuDetailsBinding;
import com.itek.retail.databinding.ListShortageBrandsBinding;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionMainFragment;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionStartFragment;
import com.itek.retail.ui.inward1.TripHUListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The Shortage list adapter.
 */
public class HUDetailsAdapter extends RecyclerView.Adapter<HUDetailsAdapter.ViewHolder>{
  
  private MainActivity context;
  private TripHUListFragment frag;
  private AlertDialog alert;
  private List<MultiQtyModel> listMenus = new ArrayList<>(0);
  private String lblTitle="";
  
  /**
   * Instantiates a new Shortage list adapter.
   *
   * @param context   the context
   * @param frag      the frag
   * @param alert     the alert
   * @param listMenus the list menus
   */
  public HUDetailsAdapter(@NonNull MainActivity context, @NonNull TripHUListFragment frag, AlertDialog alert, List<MultiQtyModel> listMenus, String lblTitle){
    this.context = context;
    this.frag = frag;
    this.alert = alert;
    this.listMenus = listMenus;
    this.lblTitle=lblTitle;
  }
  
  /**
   * Get item shortage model.
   *
   * @param position the position
   * @return the shortage model
   */
  public MultiQtyModel getItem(int position){
    return listMenus.get(position);
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public HUDetailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    ListHuDetailsBinding binding = ListHuDetailsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new HUDetailsAdapter.ViewHolder(binding);
  }
  
  public long getItemId(int position){ return position; }
  
  @Override
  public int getItemCount(){ return listMenus.size() /*+ 1*/; }
  
  // binds the data to the TextView in each cell
  @SuppressLint("ResourceAsColor")
  @Override
  public void onBindViewHolder(@NonNull HUDetailsAdapter.ViewHolder holder, final int position){
    final MultiQtyModel itemModel = getItem(position);
    holder.itemView.setBackgroundResource(itemModel == null ? R.drawable.border_top_bottom : R.color.white);
    holder.txtTitle.setShowOrderIcon(itemModel == null);
    holder.txtTitle.setText(HtmlCompat.fromHtml(itemModel != null ? itemModel.getTitle() : lblTitle, HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtScanQty.setShowOrderIcon(itemModel == null);
    holder.txtScanQty.setText(HtmlCompat.fromHtml(itemModel != null ? "" + itemModel.getFound() : context.getString(R.string.lbl_scan_qty), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtScanQty.setTextColor(ContextCompat.getColor(context, itemModel != null && itemModel.getFound()>0 ? itemModel.getFound() == itemModel.getTotal() ? R.color.txtGreen : R.color.txtRed : R.color.txt_regular));
    holder.txtExpQty.setShowOrderIcon(itemModel == null);
    holder.txtExpQty.setText(HtmlCompat.fromHtml(itemModel != null ? "" + itemModel.getTotal() : context.getString(R.string.lbl_exp_qty), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.imgNext.setVisibility(View.GONE);
    //holder.imgNext.setVisibility(itemModel != null ? View.VISIBLE : View.INVISIBLE);
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    ImageView imgNext;
    SortHeaderView txtTitle, txtScanQty, txtExpQty;
    ListShortageBrandsBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ListHuDetailsBinding binding){
      super(binding.getRoot());
      txtTitle = binding.txtTitle;
      txtScanQty = binding.txtScanQty;
      txtExpQty = binding.txtExpQty;
      imgNext = binding.imgNext;
    }
    
    /**
     * Reset.
     *
     * @param viewId the view id
     */
    public void reset(final int viewId){
      if(itemView != null && itemView instanceof LinearLayout){
        final LinearLayout llHeader = ((LinearLayout) itemView);
        final int childCount = llHeader.getChildCount();
        if(childCount > 0) for(int i = 0; i < childCount; i++){
          final SortHeaderView sortView = llHeader.getChildAt(i) != null && llHeader.getChildAt(i) instanceof SortHeaderView ? (SortHeaderView) llHeader.getChildAt(i) : null;
          if(sortView != null){
            if(sortView.getId() == viewId) sortView.updateDescOrder();
            else sortView.reset();
          }
        }
      }
    }
    
    /**
     * Bind.
     *
     * @param itemModel the item model
     */
    public void bind(final MultiQtyModel itemModel){
      binding.setInventoryShortageViewModel(itemModel);
      binding.executePendingBindings();
    }
  }
}
