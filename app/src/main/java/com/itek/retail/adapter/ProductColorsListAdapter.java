package com.itek.retail.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.ProductDao;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.home.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * The Product colors list adapter.
 * used in 'Check Availability' Product Info Dialog
 * for showing available Product Colors
 */
public class ProductColorsListAdapter extends RecyclerView.Adapter<ProductColorsListAdapter.ViewHolder>{
  
  String selectedColor = "";
  ProductDao productDao;
  private MainActivity context;
  private CommonFragment frag;
  private AlertDialog alert;
  private List<String> listItems = new ArrayList<>(0);
  private List<ProductModel> listSizes = new ArrayList<>(0);
  private RecyclerView lvProdSizes;
  private ProductModel curProdModel;
  
  /**
   * Instantiates a new Product colors list adapter.
   *
   * @param context      the context
   * @param frag         the m frag
   * @param alert        the m alert
   * @param lvProdSizes  the lv prod sizes
   * @param listItems    the list items
   * @param productModel the product model
   */
  public ProductColorsListAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, AlertDialog alert, RecyclerView lvProdSizes, List<String> listItems, ProductModel productModel){
    this.context = context;
    productDao = AppDatabase.getProductDao(context);
    this.frag = frag;
    this.alert = alert;
    this.lvProdSizes = lvProdSizes;
    this.listItems = listItems;
    this.curProdModel = productModel;
    this.selectedColor = listItems.get(0);
    if(this.lvProdSizes != null && this.selectedColor != null){
      listSizes = productDao.getZonewiseSizeCount(selectedColor);
      this.lvProdSizes.setAdapter(new ProductSizesListAdapter((MainActivity) context, frag, alert, listSizes, curProdModel));
      this.lvProdSizes.setLayoutManager(new LinearLayoutManager(context));
    }
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
    return new ViewHolder(context.getLayoutInflater().inflate(R.layout.list_dialog_product_search_color, parent, false));
  }
  
  @Override
  public long getItemId(int position){ return position; }
  
  @Override
  public int getItemCount(){ return listItems.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position){
    final String itemModel = getItem(position);
    if(itemModel == null) return;
    final boolean isColorCode = itemModel.matches(AppConstants.REGEX_HEX_COLOR_CODE);
    context.setTextAppearance(holder.txtColor, isColorCode ? R.style.TextStyleSmallAwesome : R.style.TextStyleSmall);
    holder.txtColor.setText(HtmlCompat.fromHtml(String.format(context.getString(isColorCode ? R.string.txt_color_code : R.string.txt_color_name), itemModel), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.imgColorSelected.setVisibility(isColorCode && selectedColor.equalsIgnoreCase(itemModel) ? View.VISIBLE : View.INVISIBLE);
    holder.itemView.setBackgroundResource(!isColorCode && selectedColor.equalsIgnoreCase(itemModel) ? R.color.bg_quick_action : R.color.transparent);
    
    holder.itemView.setOnClickListener(v -> {
      if(!selectedColor.equalsIgnoreCase(itemModel)){
        selectedColor = itemModel;
        listSizes.clear();
        listSizes.addAll(productDao.getZonewiseSizeCount(selectedColor));
        ((RecyclerView.Adapter) lvProdSizes.getAdapter()).notifyDataSetChanged();
      }
      ProductColorsListAdapter.this.notifyDataSetChanged();
    });
  }
  
  /**
   * Get selected color string.
   *
   * @return the string
   */
  public String getSelectedColor(){ return selectedColor; }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtColor;
    ImageView imgColorSelected;
    
    /**
     * Instantiates a new View holder.
     *
     * @param rootView the root view
     */
    ViewHolder(final View rootView){
      super(rootView);
      txtColor = rootView.findViewById(R.id.txt_product_color);
      imgColorSelected = rootView.findViewById(R.id.img_product_color_selected);
    }
    
  }
}
