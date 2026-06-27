package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.ListDialogProductSearchSizeChartBinding;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.ProductSizeModel;
import com.itek.retail.ui.customviews.MaxHeightRecyclerView;
import com.itek.retail.ui.home.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * The Product sizes list adapter.
 * used in 'Check Availability' Product Info Dialog
 * for showing Zone/Location and Size wise available Product Quantity
 */
public class ProductSizesListAdapter extends RecyclerView.Adapter<ProductSizesListAdapter.ViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private AlertDialog alert;
  private List<ProductModel> listMenus = new ArrayList<>(0);
  private ProductModel curProdModel;
  
  /**
   * Instantiates a new Product sizes list adapter.
   *
   * @param context      the context
   * @param frag         the m frag
   * @param alert        the m alert
   * @param listMenus    the list menus
   * @param productModel the product model
   */
  public ProductSizesListAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, AlertDialog alert, List<ProductModel> listMenus, ProductModel productModel){
    this.context = context;
    this.frag = frag;
    this.alert = alert;
    this.listMenus = listMenus;
    this.curProdModel = productModel;
  }
  
  /**
   * Get item product model.
   *
   * @param position the position
   * @return the product model
   */
  public ProductModel getItem(int position){
    return listMenus.get(position);
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    ListDialogProductSearchSizeChartBinding binding = ListDialogProductSearchSizeChartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }
  
  @Override
  public long getItemId(int position){ return position; }
  
  @Override
  public int getItemCount(){ return listMenus.size() + 1; }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, final int position){
    final ProductModel itemModel = position == 0 ? null : getItem(position - 1);
    final String defNoVal = context.getString(R.string.default_no_value);
    final boolean isRedDash = itemModel != null && itemModel.getZone().replace(AppConstants.ALL, defNoVal).equalsIgnoreCase(defNoVal) && chkZero(itemModel.getEanQty(), defNoVal).equalsIgnoreCase(defNoVal);
    final boolean isHighlight = itemModel != null && curProdModel != null && isNonEmpty(curProdModel.getEan()) && curProdModel.getEan().equalsIgnoreCase(itemModel.getEan()) && (curProdModel.getZone().replace(AppConstants.ALL, defNoVal).equalsIgnoreCase(defNoVal) || curProdModel.getZone().equalsIgnoreCase(itemModel.getZone()));
    holder.itemView.setBackgroundResource(itemModel != null ? isHighlight ? R.color.highlight : position % 2 == 1 ? R.color.bgListAlternet : R.color.white : R.color.white);
    //holder.itemView.setBackgroundResource(itemModel != null ? curProdModel != null && isNonEmpty(curProdModel.getColor()) && curProdModel.getColor().equalsIgnoreCase(itemModel.getColor()) && isNonEmpty(curProdModel.getSize()) && curProdModel.getSize().equalsIgnoreCase(itemModel.getSize()) && (curProdModel.getZone().replace(AppConstants.ALL,defNoVal).equalsIgnoreCase(defNoVal) || curProdModel.getZone().equalsIgnoreCase(itemModel.getZone())) ? R.color.highlight : position % 2 == 1 ? R.color.bgListAlternet : R.color.white : R.color.white);
    holder.txtSize.setText(HtmlCompat.fromHtml(itemModel != null ? itemModel.getSize() : SharedPrefManager.getString(ParamConstants.LABEL_SIZES,context.getString(R.string.lbl_size)), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtSize.setTextColor(ContextCompat.getColor(context, isRedDash ? R.color.err_red : R.color.txt_regular));
    holder.txtQty.setText(HtmlCompat.fromHtml(itemModel != null ? chkZero(itemModel.getEanQty(), defNoVal) : context.getString(R.string.lbl_qty), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtQty.setTextColor(ContextCompat.getColor(context, isRedDash ? R.color.err_red : R.color.txt_regular));
    holder.txtLocation.setText(HtmlCompat.fromHtml(itemModel != null ? itemModel.getZone().replace(AppConstants.ALL, defNoVal) : SharedPrefManager.getString(ParamConstants.LABEL_ZONES,context.getString(R.string.lbl_location)), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtLocation.setTextColor(ContextCompat.getColor(context, isRedDash ? R.color.err_red : R.color.txt_regular));
    
    holder.imgNext.setVisibility(itemModel != null ? View.VISIBLE : View.INVISIBLE);
    if(itemModel != null){
      holder.imgNext.setOnClickListener(v -> {
        if(itemModel != null){
          //movement & search
          Bundle args = new Bundle();
          args.putParcelable(itemModel.getClass().getSimpleName(), itemModel);
          final AlertDialog redirectionChoice = new AlertDialog.Builder(context, R.style.AlertDialog).create();
          final List<AlertDialog> listAlerts = new ArrayList<>(0);
          listAlerts.add(alert);
          listAlerts.add(redirectionChoice);
          context.setAlertDialogCustomTitle(redirectionChoice, R.string.lbl_select_action);
          //Quick Action menu here (if possible)
          List<MenuModel> listMenus = new ArrayList<>(0);
          if(!isRedDash)
            listMenus = AppDatabase.getMenuDao(context).getMenusByCodes(new String[]{AppConstants.MENU_CODE_SER_PROD, AppConstants.MENU_CODE_MOV});
          else listMenus.add(AppConstants.MENU_SHOP_CLUSTER);
          if(isNonEmpty(listMenus)){
            DisplayMetrics displayMetrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            final int columns = 2;
            final int width = (context.isLandscape ? displayMetrics.heightPixels : displayMetrics.widthPixels) / columns;
            final MaxHeightRecyclerView rvList = new MaxHeightRecyclerView(context);
            rvList.setBackgroundResource(R.color.white);
            rvList.setAdapter(new ProductSearchSizesRedirectionDialogAdapter((MainActivity) context, frag, listAlerts, args, listMenus));
            rvList.setLayoutManager(new GridLayoutManager(context, listMenus.size() >= columns ? columns : listMenus.size()));
            redirectionChoice.setView(rvList, 0, 0, 0, 0);
            redirectionChoice.show();
            if(listMenus.size() < columns)
              redirectionChoice.getWindow().setLayout(width * listMenus.size(), ViewGroup.LayoutParams.WRAP_CONTENT);
          }
        }
      });
    }
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    ImageView imgNext;
    TextView txtSize;
    TextView txtQty;
    TextView txtLocation;
    ListDialogProductSearchSizeChartBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ListDialogProductSearchSizeChartBinding binding){
      super(binding.getRoot());
      txtSize = binding.txtSizeChartSize;
      txtQty = binding.txtSizeChartQty;
      txtLocation = binding.txtSizeChartLocation;
      imgNext = binding.imgSizeChartNext;
      txtSize.setSelected(true);
      txtQty.setSelected(true);
      txtLocation.setSelected(true);
    }
    
    /**
     * Bind.
     *
     * @param itemModel the item model
     */
    public void bind(final ProductSizeModel itemModel){
      binding.setProductSizeViewModel(itemModel);
      binding.executePendingBindings();
    }
  }
}
