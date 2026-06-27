package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.showLog;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.ui.home.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Images list adapter is a View Holder adapter
 * used in the Product Image Dialog's View Pager
 * for showing Multiple Images with Swipe and Zoom Operations
 */
public class ImagesListAdapter extends RecyclerView.Adapter<ImagesListAdapter.ViewHolder>{
  
  private CommonActivity context;
  private List<String> listItems = new ArrayList<>(0);
  
  /**
   * Instantiates a new Images list adapter.
   *
   * @param context   the context
   * @param listItems the list items
   */
  public ImagesListAdapter(@NonNull CommonActivity context, String[] listItems){
    this.context = context;
    this.listItems = Arrays.asList(listItems);
    showLog("listItems", "" + this.listItems.size());
  }
  
  /**
   * Instantiates a new Images list adapter.
   *
   * @param context   the context
   * @param listItems the list items
   */
  public ImagesListAdapter(@NonNull CommonActivity context, List<String> listItems){
    this.context = context;
    this.listItems = listItems;
  }
  
  /**
   * Get item string.
   *
   * @param position the position
   * @return the string
   */
  public String getItem(int position){
    return listItems.get(position);
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new ViewHolder(context.getLayoutInflater().inflate(R.layout.pager_product_img, parent, false));
  }
  
  @Override
  public long getItemId(int position){ return position; }
  
  @Override
  public int getItemCount(){ return listItems.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, final int position){
    final String itemModel = getItem(position);
    if(itemModel == null) return;
    ((MainActivity) context).loadImage(holder.imgProd, itemModel, 0, false);
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
