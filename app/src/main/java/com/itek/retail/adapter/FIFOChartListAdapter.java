package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractSerializable;
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
import com.itek.retail.database.FIFODao;
import com.itek.retail.databinding.ListDialogProductSearchSizeChartBinding;
import com.itek.retail.model.FIFOModel;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.ProductSizeModel;
import com.itek.retail.ui.customviews.MaxHeightRecyclerView;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.movement.MovementStartFragment;
import com.itek.retail.ui.search.fifo.SearchFIFOStartFragment;
import com.itek.retail.ui.search.productsearch.ProductSearchDetailsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The Product sizes list adapter.
 * used in 'Check Availability' Product Info Dialog
 * for showing Zone/Location and Size wise available Product Quantity
 */
public class FIFOChartListAdapter extends RecyclerView.Adapter<FIFOChartListAdapter.ViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private AlertDialog alert;
  private List<FIFOModel> listFifoModels = new ArrayList<>(0);
  private ProductModel curProdModel;
  private FIFODao fifoDao;
  
  /**
   * Instantiates a new Product sizes list adapter.
   *
   * @param context        the context
   * @param frag           the m frag
   * @param alert          the m alert
   * @param listFifoModels the list fifo models
   * @param productModel   the product model
   */
  public FIFOChartListAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, AlertDialog alert, List<FIFOModel> listFifoModels, ProductModel productModel){
    this.context = context;
    this.frag = frag;
    this.alert = alert;
    this.listFifoModels = listFifoModels;
    this.curProdModel = productModel;
    this.fifoDao = AppDatabase.getFIFODao(context);
  }
  
  /**
   * Get item product model.
   *
   * @param position the position
   * @return the product model
   */
  public FIFOModel getItem(int position){
    return listFifoModels.get(position);
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
  public int getItemCount(){ return listFifoModels.size() + 1; }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, final int position){
    final FIFOModel itemModel = position == 0 ? null : getItem(position - 1);
    final String defNoVal = context.getString(R.string.default_no_value);
    final boolean isRedDash = itemModel != null && itemModel.getZone().replace(AppConstants.ALL, defNoVal).equalsIgnoreCase(defNoVal) && chkZero(itemModel.getTotalQty(), defNoVal).equalsIgnoreCase(defNoVal);
    final boolean isHighlight = itemModel != null && curProdModel != null && isNonEmpty(curProdModel.getEan()) && curProdModel.getEan().equalsIgnoreCase(itemModel.getEan()) && (curProdModel.getZone().replace(AppConstants.ALL, defNoVal).equalsIgnoreCase(defNoVal) || curProdModel.getZone().equalsIgnoreCase(itemModel.getZone())) && curProdModel.getFifoDate().equalsIgnoreCase(itemModel.getFifoDate());
    holder.itemView.setBackgroundResource(itemModel != null ? isHighlight ? R.color.highlight : position % 2 == 1 ? R.color.bgListAlternet : R.color.white : R.color.white);
    //holder.itemView.setBackgroundResource(itemModel != null ? curProdModel != null && isNonEmpty(curProdModel.getColor()) && curProdModel.getColor().equalsIgnoreCase(itemModel.getColor()) && isNonEmpty(curProdModel.getSize()) && curProdModel.getSize().equalsIgnoreCase(itemModel.getSize()) && (curProdModel.getZone().replace(AppConstants.ALL,defNoVal).equalsIgnoreCase(defNoVal) || curProdModel.getZone().equalsIgnoreCase(itemModel.getZone())) ? R.color.highlight : position % 2 == 1 ? R.color.bgListAlternet : R.color.white : R.color.white);
    holder.txtSize.setText(HtmlCompat.fromHtml(itemModel != null ? itemModel.getFifoDate() : context.getString(R.string.lbl_date), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtSize.setTextColor(ContextCompat.getColor(context, isRedDash ? R.color.err_red : R.color.txt_regular));
    holder.txtQty.setText(HtmlCompat.fromHtml(itemModel != null ? chkZero(itemModel.getTotalQty(), defNoVal) : context.getString(R.string.lbl_qty), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtQty.setTextColor(ContextCompat.getColor(context, isRedDash ? R.color.err_red : R.color.txt_regular));
    //holder.txtQty.setVisibility(View.GONE);
    holder.txtLocation.setText(HtmlCompat.fromHtml(itemModel != null ? isNonEmpty(itemModel.getFifoDate()) ? chkNull(fifoDao.getDatewiseZoneStr(itemModel.getEan(), itemModel.getFifoDate()), "").replaceAll(",", ", ") : itemModel.getZone().replace(AppConstants.ALL, defNoVal) : SharedPrefManager.getString(ParamConstants.LABEL_ZONES,context.getString(R.string.lbl_locations)), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtLocation.setTextColor(ContextCompat.getColor(context, isRedDash ? R.color.err_red : R.color.txt_regular));
    
    holder.imgNext.setVisibility(itemModel != null && !isRedDash ? View.VISIBLE : View.INVISIBLE);
    if(itemModel != null && !isRedDash){
      holder.imgNext.setOnClickListener(v -> {
        if(itemModel != null && !isRedDash){
          //movement & search
          Bundle args = new Bundle();
          args.putParcelable(itemModel.getClass().getSimpleName(), itemModel);
          ProductModel pm = (ProductModel) chkNull(extractSerializable(frag.getArguments(), ProductModel.class),new ProductModel());
          pm.setFifoDate(itemModel.fifoDate);
          pm.setZone(itemModel.zone);
          pm.setZoneId(itemModel.zoneId);
          pm.setStockAge(itemModel.age);
          int totalQty = AppDatabase.getFIFODao(context).getDateTotalCount(itemModel.ean, itemModel.fifoDate);
          pm.setTotalQty(totalQty);
          pm.setQty(totalQty);
          //pm.setEanQty(totalQty);
          args.putSerializable(ProductModel.class.getSimpleName(), pm);
          args.putString(ParamConstants.FIFO_DATE, itemModel.fifoDate);
          args.putString(ParamConstants.ZONE_NAME, itemModel.zone);
          args.putString(ParamConstants.ZONE_ID, itemModel.zoneId);
          
          //Quick Action menu here (if possible)
          List<MenuModel> listMenus = new ArrayList<>(0);
          if(!isRedDash)
            listMenus = AppDatabase.getMenuDao(context).getMenusByCodes(new String[]{AppConstants.MENU_CODE_SER_FIFO});
          //else listMenus.add(AppConstants.MENU_SHOP_CLUSTER);
          if(isNonEmpty(listMenus)){
            if(listMenus.size() == 1){
              menuRedirection(args, listMenus.get(0));
              if(alert != null && alert.isShowing()) alert.dismiss();
            }
            else{
              final AlertDialog redirectionChoice = new AlertDialog.Builder(context, R.style.AlertDialog).create();
              final List<AlertDialog> listAlerts = new ArrayList<>(0);
              listAlerts.add(alert);
              listAlerts.add(redirectionChoice);
              context.setAlertDialogCustomTitle(redirectionChoice, R.string.lbl_select_action);
              
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
        }
      });
    }
  }
  
  private void menuRedirection(Bundle args, final MenuModel menuModel){
    args.putString(AppConstants.TITLE, menuModel.getScreenMenuName());
    args.putInt(AppConstants.TITLE_LOGO_RES_ID, menuModel.getScreenIconId(context));
    args.putString(AppConstants.TITLE_LOGO_URL, menuModel.getImageUrl());
    args.putSerializable(MenuModel.class.getSimpleName(), menuModel);
    
    switch(menuModel.getMenuCode().toUpperCase().replaceAll(" ", "_")){
      case AppConstants.MENU_CODE_SER_PROD:
        if(frag instanceof SearchFIFOStartFragment)
          ((SearchFIFOStartFragment) frag).stopSession();
        if(frag instanceof ProductSearchDetailsFragment)
          ((ProductSearchDetailsFragment) frag).setProduct(args);
        else frag.handleFragmentRedirection(new ProductSearchDetailsFragment(), args);
        break;
      case AppConstants.MENU_CODE_MOV:
        if(frag instanceof ProductSearchDetailsFragment)
          ((ProductSearchDetailsFragment) frag).stopSession();
        if(frag instanceof SearchFIFOStartFragment)
          ((SearchFIFOStartFragment) frag).stopSession();
        frag.handleFragmentRedirection(new MovementStartFragment(), args);
        break;
      case AppConstants.MENU_CODE_SER_FIFO:
        if(frag instanceof ProductSearchDetailsFragment)
          ((ProductSearchDetailsFragment) frag).stopSession();
        if(frag instanceof SearchFIFOStartFragment)
          ((SearchFIFOStartFragment) frag).setProduct(args);
        else frag.handleFragmentRedirection(new SearchFIFOStartFragment(), args);
        break;
      case AppConstants.MENU_CODE_SHOP_CLUSTER:
        //TODO Add & Show New Screen Here.
        context.showShortToast(menuModel.getErrEnabledMsg(context));
        break;
      default:
        break;
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
