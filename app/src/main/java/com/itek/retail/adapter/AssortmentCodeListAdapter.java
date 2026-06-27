package com.itek.retail.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.ListAssortCodesBinding;
import com.itek.retail.databinding.ListOmniOrdersBinding;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.customviews.SortHeaderView;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.assortment.SearchAssortMainFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The product list adapter.
 */
public class AssortmentCodeListAdapter extends RecyclerView.Adapter<AssortmentCodeListAdapter.ViewHolder>{
  
  private MainActivity context;
  private SearchAssortMainFragment frag;
  private List<ProductModel> listAssortCodes = new ArrayList<>(0);
  private boolean isSimpleHeader = false;
  
  /**
   * Instantiates a new Shortage list adapter.
   *
   * @param context         the context
   * @param frag            the frag
   * @param listAssortCodes the list menus
   */
  public AssortmentCodeListAdapter(@NonNull MainActivity context, @NonNull SearchAssortMainFragment frag, List<ProductModel> listAssortCodes){
    this.context = context;
    this.frag = frag;
    this.listAssortCodes = listAssortCodes;
  }
  
  /**
   * Get item shortage model.
   *
   * @param position the position
   * @return the shortage model
   */
  public ProductModel getItem(int position){
    return listAssortCodes.get(position);
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public AssortmentCodeListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    ListAssortCodesBinding binding = ListAssortCodesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new AssortmentCodeListAdapter.ViewHolder(binding);
  }
  
  @Override
  public long getItemId(int position){ return position; }
  
  @Override
  public int getItemCount(){ return listAssortCodes.size() + (isSimpleHeader ? 1 : 0); }
  
  // binds the data to the TextView in each cell
  @SuppressLint("ResourceAsColor")
  @Override
  public void onBindViewHolder(@NonNull AssortmentCodeListAdapter.ViewHolder holder, final int position){
    final ProductModel itemModel = getItemCount() <= listAssortCodes.size() ? getItem(position) : position > 0 ? getItem(position - 1) : null;
    final boolean isShowOrderIcon = !isSimpleHeader && itemModel == null && frag instanceof SearchAssortMainFragment;
    holder.itemView.setBackgroundResource(itemModel == null ? R.drawable.border_top_bottom : R.color.white);
    holder.txtCodeNo.setShowOrderIcon(isShowOrderIcon);
    holder.txtCodeNo.setText(HtmlCompat.fromHtml(itemModel != null ? itemModel.getOrderNo() : context.getString(R.string.lbl_code_no), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtQty.setShowOrderIcon(isShowOrderIcon);
    holder.txtQty.setText(HtmlCompat.fromHtml(itemModel != null ? "" + itemModel.getQtyStr() : context.getString(R.string.lbl_qty), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtPriority.setShowOrderIcon(isShowOrderIcon);
    holder.txtPriority.setText(HtmlCompat.fromHtml(itemModel != null ? "" + itemModel.getPriority() : SharedPrefManager.getString(ParamConstants.LABEL_PRIORITY,context.getString(R.string.lbl_priority)), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.imgNext.setVisibility(itemModel != null ? View.VISIBLE : View.INVISIBLE);
    
    holder.itemView.setOnClickListener(v -> {
      if(itemModel != null && frag != null){
        frag.callDetailsAPI(itemModel);
      }
    });
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    ImageView imgNext;
    SortHeaderView txtCodeNo;
    SortHeaderView txtQty;
    SortHeaderView txtPriority;
    ListOmniOrdersBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ListAssortCodesBinding binding){
      super(binding.getRoot());
      txtCodeNo = binding.txtAssortCode;
      txtQty = binding.txtAssortQty;
      txtPriority = binding.txtAssortPriority;
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
    public void bind(final ProductModel itemModel){
      binding.setOmniOrderModel(itemModel);
      binding.executePendingBindings();
    }
  }
}
