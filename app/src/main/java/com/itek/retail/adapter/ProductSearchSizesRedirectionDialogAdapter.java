package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.ProdSearchSizeRedirectionAdapterBinding;
import com.itek.retail.model.MenuModel;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.movement.MovementStartFragment;
import com.itek.retail.ui.search.fifo.SearchFIFOStartFragment;
import com.itek.retail.ui.search.productsearch.ProductSearchDetailsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The Product search sizes redirection dialog adapter.
 * user can also use '>' symbol for Redirection Options (Product Search, Movement & Other Stores)
 */
public class ProductSearchSizesRedirectionDialogAdapter extends RecyclerView.Adapter<ProductSearchSizesRedirectionDialogAdapter.ViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private List<AlertDialog> listAlerts;
  private Bundle args;
  private List<MenuModel> listMenus = new ArrayList<>(0);
  
  /**
   * Instantiates a new Product search sizes redirection dialog adapter.
   *
   * @param context       the context
   * @param frag          the m frag
   * @param listAlerts    the list alerts
   * @param args          the args
   * @param listGridMenus the list grid menus
   */
  public ProductSearchSizesRedirectionDialogAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<AlertDialog> listAlerts, Bundle args, List<MenuModel> listGridMenus){
    this.context = context;
    this.frag = frag;
    this.listAlerts = listAlerts;
    this.args = args;
    this.listMenus = listGridMenus;
  }
  
  /**
   * Gets item.
   *
   * @param position the position
   * @return the item
   */
  public MenuModel getItem(int position){
    return listMenus.get(position);
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    ProdSearchSizeRedirectionAdapterBinding binding = ProdSearchSizeRedirectionAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }
  
  @Override
  public long getItemId(int position){
    return getItem(position).getIconId();
  }
  
  @Override
  public int getItemCount(){ return listMenus.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position){
    final MenuModel menuModel = getItem(position);
    if(menuModel == null) return;
    holder.itemView.setBackgroundResource(R.drawable.border_right);
    holder.txtview.setText(HtmlCompat.fromHtml(menuModel.getMenuName(), HtmlCompat.FROM_HTML_MODE_LEGACY));
    try{
      context.loadImage(holder.imgview, chkNull(menuModel.getImageUrl(), ""), chkZero(menuModel.getIconId(), context.getResources().getIdentifier(menuModel.getMenuIconName(), AppConstants.RES_DRAWABLE, context.getPackageName())));
    }
    catch(Exception e){ e.printStackTrace(); }
    
    holder.itemView.setOnClickListener(v -> {
      if(!menuModel.getIsEnabled()){
        context.showShortToast(menuModel.getErrEnabledMsg(context));
        return;
      }
      //Load/Redirect to specific fragment
      Bundle args = chkNull(ProductSearchSizesRedirectionDialogAdapter.this.args, new Bundle());
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
      new Handler().postDelayed(new Runnable(){
        @Override
        public void run(){
          dismissAllAlerts();
        }
      },50);
      
    });
  }
  
  /**
   * Dismiss all alerts.
   */
  private void dismissAllAlerts(){
    if(listAlerts != null && !listAlerts.isEmpty()){
      for(AlertDialog alert : listAlerts)
        if(alert != null) alert.dismiss();
    }
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtview;
    ImageView imgview;
    ProdSearchSizeRedirectionAdapterBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ProdSearchSizeRedirectionAdapterBinding binding){
      super(binding.getRoot());
      txtview = binding.lblMenuTitle;
      imgview = binding.imgMenuLogo;
    }
    
    /**
     * Bind.
     *
     * @param menuModel the menu model
     */
    public void bind(final MenuModel menuModel){
      binding.setGridMenuViewModel(menuModel);
      binding.executePendingBindings();
    }
  }
}
