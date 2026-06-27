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
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.ListShortageBrandsBinding;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionMainFragment;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionStartFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The Shortage list adapter.
 */
public class ShortageListAdapter extends RecyclerView.Adapter<ShortageListAdapter.ViewHolder>{
  
  private MainActivity context;
  private StockCorrectionMainFragment frag;
  private AlertDialog alert;
  private List<MultiQtyModel> listMenus = new ArrayList<>(0);
  private MultiQtyModel multiQtyModel;
  
  /**
   * Instantiates a new Shortage list adapter.
   *
   * @param context   the context
   * @param frag      the frag
   * @param alert     the alert
   * @param listMenus the list menus
   */
  public ShortageListAdapter(@NonNull MainActivity context, @NonNull StockCorrectionMainFragment frag, AlertDialog alert, List<MultiQtyModel> listMenus){
    this.context = context;
    this.frag = frag;
    this.alert = alert;
    this.listMenus = listMenus;
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
  public ShortageListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    ListShortageBrandsBinding binding = ListShortageBrandsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new ShortageListAdapter.ViewHolder(binding);
  }
  
  public long getItemId(int position){ return position; }
  
  @Override
  public int getItemCount(){ return listMenus.size() /*+ 1*/; }
  
  // binds the data to the TextView in each cell
  @SuppressLint("ResourceAsColor")
  @Override
  public void onBindViewHolder(@NonNull ShortageListAdapter.ViewHolder holder, final int position){
    final MultiQtyModel itemModel = getItem(position);
    holder.itemView.setBackgroundResource(itemModel == null ? R.drawable.border_top_bottom : R.color.white);
    holder.txtBrand.setShowOrderIcon(itemModel == null);
    holder.txtBrand.setText(HtmlCompat.fromHtml(itemModel != null ? itemModel.getTitle() : SharedPrefManager.getString(ParamConstants.LABEL_BRANDS,context.getString(R.string.lbl_brand)), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtFound.setShowOrderIcon(itemModel == null);
    holder.txtFound.setText(HtmlCompat.fromHtml(itemModel != null ? "" + itemModel.getFound() : context.getString(R.string.lbl_found), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtFound.setTextColor(ContextCompat.getColor(context, itemModel != null ? itemModel.getFound() >= itemModel.getTotal() ? R.color.txtGreen : R.color.txtRed : R.color.txt_regular));
    holder.txtShortage.setShowOrderIcon(itemModel == null);
    holder.txtShortage.setText(HtmlCompat.fromHtml(itemModel != null ? "" + itemModel.getTotal() : context.getString(R.string.lbl_shortage), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.imgNext.setVisibility(itemModel != null ? View.VISIBLE : View.INVISIBLE);
    
    holder.itemView.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(itemModel != null && frag != null){
          Bundle args = AppCommonMethods.chkNull(frag.getArguments(), new Bundle());
          args.putString(AppConstants.BRANDS, itemModel.getTitle());
          args.putInt(AppConstants.FOUND, itemModel.getFound());
          args.putInt(AppConstants.SHORTAGE, itemModel.getTotal());
          args.putSerializable(itemModel.getClass().getSimpleName(), itemModel);
          args.putString(AppConstants.CATEGORY, frag.getSelectedCategory());
          args.putString(AppConstants.ZONE, frag.getSelectedZone());
          args.putString(AppConstants.ZONE_ID, frag.getSelectedZoneId());
          frag.storeTotalCounts();
          context.checkReaderConnection(new StockCorrectionStartFragment(), args);
        }
      }
    });
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    ImageView imgNext;
    SortHeaderView txtBrand, txtFound, txtShortage;
    ListShortageBrandsBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ListShortageBrandsBinding binding){
      super(binding.getRoot());
      txtBrand = binding.txtBrand;
      txtFound = binding.txtFound;
      txtShortage = binding.txtShortage;
      imgNext = binding.imgInventoryShortageNext;
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
