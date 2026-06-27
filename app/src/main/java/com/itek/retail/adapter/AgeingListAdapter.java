package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.ListAgeingBrandsBinding;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.ageing.AgeingMainFragment;
import com.itek.retail.ui.search.ageing.AgeingSearchStartFragment;

import java.util.ArrayList;
import java.util.List;

public class AgeingListAdapter extends RecyclerView.Adapter<AgeingListAdapter.ViewHolder>{
  
  private MainActivity context;
  private AgeingMainFragment frag;
  private AlertDialog alert;
  private List<MultiQtyModel> listMenus = new ArrayList<>(0);
  private MultiQtyModel multiQtyModel;
  
  /**
   * Instantiates a new Ageing list adapter.
   *
   * @param context   the context
   * @param frag      the frag
   * @param alert     the alert
   * @param listMenus the list menus
   */
  public AgeingListAdapter(@NonNull MainActivity context, @NonNull AgeingMainFragment frag, AlertDialog alert, List<MultiQtyModel> listMenus){
    this.context = context;
    this.frag = frag;
    this.alert = alert;
    this.listMenus = listMenus;
  }
  
  /**
   * Get item ageing model.
   *
   * @param position the position
   * @return the ageing model
   */
  public MultiQtyModel getItem(int position){
    return listMenus.get(position);
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public AgeingListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    ListAgeingBrandsBinding binding = ListAgeingBrandsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new AgeingListAdapter.ViewHolder(binding);
  }
  
  public long getItemId(int position){ return position; }
  
  @Override
  public int getItemCount(){ return listMenus.size() /*+ 1*/; }
  
  // binds the data to the TextView in each cell
  @SuppressLint("ResourceAsColor")
  @Override
  public void onBindViewHolder(@NonNull AgeingListAdapter.ViewHolder holder, final int position){
    final MultiQtyModel itemModel = getItem(position);
    holder.itemView.setBackgroundResource(itemModel == null ? R.drawable.border_top_bottom : R.color.white);
    holder.txtBrand.setShowOrderIcon(itemModel == null);
    holder.txtBrand.setText(HtmlCompat.fromHtml(itemModel != null ? itemModel.getTitle() : context.getString(R.string.lbl_brand), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtQuantity.setShowOrderIcon(itemModel == null);
    holder.txtQuantity.setText(HtmlCompat.fromHtml(itemModel != null ? "" + itemModel.getTotal() : context.getString(R.string.lbl_shortage), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.imgNext.setVisibility(itemModel != null ? View.VISIBLE : View.INVISIBLE);
    
    holder.itemView.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(itemModel != null && frag != null){
          List<ProductModel> listProducts = AppDatabase.getProductDao(context).getShortageProducts(frag.getSelZone(), frag.getSelCategory(), frag.getSelBrand().equalsIgnoreCase(AppConstants.ALL) ? itemModel.getTitle() : frag.getSelBrand());
          if(isNonEmpty(listProducts)){
            Bundle args = AppCommonMethods.chkNull(frag.getArguments(), new Bundle());
            if(listProducts.size() == 1){
              final ProductModel model = listProducts.get(0);
              args.putSerializable(model.getClass().getSimpleName(), model);
              context.checkReaderConnection(new AgeingSearchStartFragment(), args);
            }
            else if(listProducts.size() > 1){
              //Extra List Fragment for Selection
            }
          }
          else{
            //show error
          }
        }
      }
    });
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    ImageView imgNext;
    SortHeaderView txtBrand, txtQuantity;
    ListAgeingBrandsBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ListAgeingBrandsBinding binding){
      super(binding.getRoot());
      txtBrand = binding.txtBrand;
      txtQuantity = binding.txtQuantity;
      imgNext = binding.imgAgeingNext;
      
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
      binding.setAgeingSearchViewModel(itemModel);
      binding.executePendingBindings();
    }
  }
}
