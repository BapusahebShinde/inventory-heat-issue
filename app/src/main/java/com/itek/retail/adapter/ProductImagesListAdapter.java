package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.itek.retail.R;
import com.itek.retail.ui.home.MainActivity;

/**
 * The Product images list adapter.
 * used in Product Details View
 * for showing Multiple Image with Swipe Operation
 */
public class ProductImagesListAdapter extends RecyclerView.Adapter<ProductImagesListAdapter.ViewHolder>{
  
  private MainActivity context;
  private String[] listItems;
  private ViewPager2 pager;
  
  /**
   * Instantiates a new Product images list adapter.
   *
   * @param context   the context
   * @param pager     the pager
   * @param listItems the list items
   */
  public ProductImagesListAdapter(@NonNull MainActivity context, ViewPager2 pager, String[] listItems){
    this.context = context;
    this.pager = pager;
    this.listItems = listItems;
    showLog("listItems", "" + this.listItems.length);
  }
  
  /**
   * Get item string.
   *
   * @param position the position
   * @return the string
   */
  public String getItem(int position){
    return listItems[position];
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new ViewHolder(context.getLayoutInflater().inflate(R.layout.list_pager_product_img, parent, false));
  }
  
  @Override
  public long getItemId(int position){ return position; }
  
  @Override
  public int getItemCount(){ return listItems.length; }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, final int position){
    final String itemModel = getItem(position);
    context.loadImage(holder.imgProd, itemModel, 0, false);
    holder.imgProd.setOnClickListener(view -> {
      if(pager.isEnabled() && (listItems.length > 1 || isNonEmpty(listItems[0])))
        context.showAlert(listItems);
    });
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    ImageView imgProd;
    
    /**
     * Instantiates a new View holder.
     *
     * @param rootView the root view
     */
    ViewHolder(final View rootView){
      super(rootView);
      imgProd = rootView.findViewById(R.id.imgProduct);
    }
  }
}
