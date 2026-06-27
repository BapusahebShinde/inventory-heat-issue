package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUseNewUIForLBS;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.ListProductDetailsBinding;
import com.itek.retail.model.ProductModel;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionStartFragment;
import com.itek.retail.ui.movement.replenishment.ReplenishmentListFragment;
import com.itek.retail.ui.movement.replenishment.ReplenishmentStartFragment;
import com.itek.retail.ui.outward.OutwardPickListDetailsFragment;
import com.itek.retail.ui.outward.OutwardPickStartFragment;
import com.itek.retail.ui.outward.offrange.OffRangeListFragment;
import com.itek.retail.ui.search.assortment.SearchAssortListFragment;
import com.itek.retail.ui.search.assortment.SearchAssortStartFragment;
import com.itek.retail.ui.search.listnewsearch.SearchListExcelFragment;
import com.itek.retail.ui.search.listnewsearch.SearchListExcelStartFragment;
import com.itek.retail.ui.search.listsearch.SearchListFragment;
import com.itek.retail.ui.search.listsearch.SearchListStartFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelListDetailsFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelStartFragment;

import org.json.JSONObject;

import java.util.List;

/**
 * The Product list adapter.
 */
public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.MyViewHolder>{
  
  //temp flag
  private final boolean isGlobalQtyCheck = false;
  MainActivity context;
  CommonFragment frag;
  AlertDialog alert;
  private List<ProductModel> productList;
  
  /**
   * Instantiates a new Stock correction list adapter.
   *
   * @param context     the CommonActivity
   * @param frag        the CommonFragment
   * @param productList the product list
   */
  public ProductListAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<ProductModel> productList){
    this.context = context;
    this.frag = frag;
    this.productList = productList;
  }
  
  /**
   * Instantiates a new Stock correction list adapter.
   *
   * @param context     the CommonActivity
   * @param frag        the CommonFragment
   * @param alertDialog the AlertDialog
   * @param productList the product list
   */
  public ProductListAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, AlertDialog alertDialog, List<ProductModel> productList){
    this.context = context;
    this.frag = frag;
    this.alert = alertDialog;
    this.productList = productList;
  }
  
  @Override
  public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new MyViewHolder(ListProductDetailsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public void onBindViewHolder(MyViewHolder holder, int position){
    final ProductModel itemModel = productList.get(position);
    if(itemModel == null) return;
    if(frag instanceof StockCorrectionStartFragment){
      holder.txtEPC.setVisibility(View.VISIBLE);
      ((LinearLayout) holder.txtEAN.getParent()).setVisibility(View.GONE);
      
      //holder.txtEPC.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean)), itemModel.getEan()) + "\t" + String.format(context.getString(R.string.txt_append_qty), context.getColorCode(itemModel.getFoundQty() > 0 && itemModel.getEanQty() <= itemModel.getFoundQty() ? R.color.green : itemModel.getFoundQty() > 0 ? R.color.orange : R.color.err_red), itemModel.getFoundQtyStr(), "" + itemModel.getEanQty()), HtmlCompat.FROM_HTML_MODE_LEGACY));
      holder.txtEPC.setText(HtmlCompat.fromHtml(itemModel.getEanTxt(context) + "\t" + String.format(context.getString(R.string.txt_append_qty), context.getColorCode(itemModel.getFoundQty() > 0 && itemModel.getEanQty() <= itemModel.getFoundQty() ? R.color.green : itemModel.getFoundQty() > 0 ? R.color.orange : R.color.err_red), itemModel.getFoundQtyStr(), "" + itemModel.getEanQty()), HtmlCompat.FROM_HTML_MODE_LEGACY));
      //holder.txtExtra.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_ZONES,context.getString(R.string.lbl_loc)), itemModel.getZone()), HtmlCompat.FROM_HTML_MODE_LEGACY));
      holder.txtExtra.setText(HtmlCompat.fromHtml(itemModel.getZoneTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
      holder.txtQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_shortage_qty), context.getColorCode(itemModel.getFoundQty() > 0 && itemModel.getEanQty() <= itemModel.getFoundQty() ? R.color.green : itemModel.getFoundQty() > 0 ? R.color.orange : R.color.err_red), itemModel.getFoundQtyStr(), "" + itemModel.getEanQty()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    }
    if(frag instanceof ReplenishmentListFragment){
      final String replenishmentType = ((ReplenishmentListFragment) frag).getReplenishmentType();
      final boolean isStaticReplenishment = replenishmentType.equalsIgnoreCase(AppConstants.REPLENISH_TYPE_STATIC);
      holder.txtQty.setText(HtmlCompat.fromHtml(String.format(context.getString(isStaticReplenishment ? R.string.txt_stock : R.string.txt_qty), "" + (isStaticReplenishment ? itemModel.getEanQty() : chkZero(itemModel.getQty(), itemModel.getEanQty()))), HtmlCompat.FROM_HTML_MODE_LEGACY));
    }
    if(frag != null && (frag instanceof OffRangeListFragment || frag instanceof SearchListExcelFragment)){
      //final boolean hasZone = !(itemModel.getDestZone().equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE) || itemModel.getZone().equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE));
      final boolean hasQty = chkZero(itemModel.getQty(), itemModel.getEanQty()) > 0;
      //holder.txtLoc.setVisibility(hasZone?View.VISIBLE:View.GONE);
      holder.txtQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_available_stock), !hasQty ? AppConstants.DEFAULT_NO_VALUE : "" + chkZero(itemModel.getQty(), itemModel.getEanQty())), HtmlCompat.FROM_HTML_MODE_LEGACY));
      holder.txtQty.setVisibility(false && hasQty?View.VISIBLE:View.GONE);
      if(frag instanceof OffRangeListFragment)
        holder.txtLoc.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_picked_qty), itemModel.getFoundQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
      if(frag instanceof SearchListExcelFragment){
        if(chkZero(itemModel.getEanQty(), itemModel.getQty())<=0) holder.txtLoc.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_found), itemModel.getFound() ? "\u2705" : "\u2013"), HtmlCompat.FROM_HTML_MODE_LEGACY));
        else holder.txtLoc.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_found), itemModel.getFoundQtyStr() + " / " + chkZero(itemModel.getEanQtyStr(), itemModel.getQtyStr())), HtmlCompat.FROM_HTML_MODE_LEGACY));
      }
      
      //holder.txtName.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_MATKL,context.getString(R.string.lbl_matkl)), chkNull(itemModel.getMatkl(),itemModel.getOrderNo())), HtmlCompat.FROM_HTML_MODE_LEGACY));
      holder.txtName.setText(HtmlCompat.fromHtml(itemModel.getMatklTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
      holder.llExtra.setVisibility(View.VISIBLE);
      holder.txtExtra1.setVisibility(View.VISIBLE);
      //holder.txtExtra1.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_NAME,context.getString(R.string.lbl_name)), itemModel.getName()), HtmlCompat.FROM_HTML_MODE_LEGACY));
      holder.txtExtra1.setText(HtmlCompat.fromHtml(itemModel.getNameTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));

      //final int colorId = itemModel.getPriority() >= AppConstants.SEARCH_PERCENT_VALUE_90 ? R.color.green : itemModel.getPriority() >= AppConstants.SEARCH_PERCENT_VALUE_66 ? R.color.light_green : itemModel.getPriority() >= AppConstants.SEARCH_PERCENT_VALUE_33 ? R.color.orange : R.color.transparent;
      //final int colorId = itemModel.getPriority() >= AppConstants.SEARCH_PERCENT_VALUE_75 ? R.color.light_green : itemModel.getPriority() >= AppConstants.SEARCH_PERCENT_VALUE_50 ? R.color.orange : R.color.transparent;
      //old code (percentage wise color)
      //final int colorId = itemModel.getPriority() >= AppConstants.SEARCH_PERCENT_VALUE_80 ? R.color.light_green : itemModel.getPriority() >= AppConstants.SEARCH_PERCENT_VALUE_60 ? R.color.orange : R.color.transparent;
      //((View) holder.txtEAN.getParent()).setBackgroundResource(colorId);
      
      final int index = productList.indexOf(itemModel);
      //final int pos = holder.getAdapterPosition();
      final int colorId= itemModel.getPriority()> 0 && index<5?R.color.light_green:R.color.transparent;
      ((View) holder.txtEAN.getParent()).setBackgroundResource(colorId);
    }
    
    if(frag instanceof SearchListFragment || frag instanceof SearchAssortListFragment || frag instanceof OutwardPickListDetailsFragment || frag instanceof OmniChannelListDetailsFragment){
      final int dp5 = context.getResources().getDimensionPixelOffset(R.dimen.dp_5);
      final int pos = holder.getAdapterPosition();
      showLog("pos", "" + pos);
      final boolean samePreEan = pos > 0 && itemModel.getEan().equalsIgnoreCase(productList.get(pos - 1).getEan());
      final boolean sameNextEan = pos < getItemCount() - 1 && itemModel.getEan().equalsIgnoreCase(productList.get(pos + 1).getEan());
      showLog("samePreEan", "" + pos + "_" + itemModel.getEan() + (pos > 0 ? "==" + productList.get(pos - 1).getEan() : ""));
      showLog("sameNextEan", "" + pos + "_" + itemModel.getEan() + (pos < getItemCount() - 1 ? "==" + productList.get(pos + 1).getEan() : ""));
      ((RecyclerView.LayoutParams) holder.itemView.getLayoutParams()).setMargins(dp5, !samePreEan ? dp5 : dp5 * -1, dp5, !samePreEan ? dp5 : 0);
      ((LinearLayout) holder.img.getParent()).setPadding(dp5, 0, dp5, 0);
      if(frag instanceof OutwardPickListDetailsFragment){
        ((LinearLayout) holder.txtEAN.getParent()).setVisibility(View.GONE);
        ((LinearLayout) holder.img.getParent()).setBackgroundResource(R.drawable.border_except_top_corner_bottom);
      }
      else if(isUseNewUIForLBS){
        ((LinearLayout) holder.txtEAN.getParent()).setVisibility(View.GONE);
        ((LinearLayout) holder.img.getParent()).setBackgroundResource(R.drawable.border_side);
        ((LinearLayout) holder.img.getParent()).setVisibility(!samePreEan ? View.VISIBLE : View.GONE);
        ((LinearLayout) holder.txtTotalQty.getParent()).setVisibility(View.VISIBLE);
        ((LinearLayout) holder.txtTotalQty.getParent()).setBackgroundResource(sameNextEan ? R.drawable.border_except_top_no_corner : R.drawable.border_except_top_corner_bottom);
      }
      final int eanFoundQty = AppDatabase.getProductDao(context).getEANFoundCount(itemModel.getEan());
      holder.txtHeader.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_color__),context.getColorCode(frag instanceof SearchListFragment || frag instanceof OutwardPickListDetailsFragment ? itemModel.getZone().equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE) && itemModel.getFoundQty() <= 0 ? R.color.red : eanFoundQty >= itemModel.getTotalQty() ? R.color.green : R.color.txt_header: R.color.txt_header),SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean)), itemModel.getEan())
              + (frag instanceof SearchListFragment || frag instanceof OutwardPickListDetailsFragment ? String.format(context.getString(R.string.txt_append_qty), context.getColorCode(eanFoundQty > 0 && eanFoundQty >= itemModel.getTotalQty() ? R.color.green : eanFoundQty > 0 ? R.color.orange : R.color.err_red), "" + eanFoundQty, "" + itemModel.getTotalQtyStr()) : ""), HtmlCompat.FROM_HTML_MODE_LEGACY));
      /*holder.txtHeader.setText(HtmlCompat.fromHtml(String.format(context.getString(frag instanceof SearchListFragment || frag instanceof OutwardPickListDetailsFragment ? itemModel.getZone().equalsIgnoreCase(AppConstants.DEFAULT_NO_VALUE) && itemModel.getFoundQty() <= 0 ? R.string.txt_ean_red : eanFoundQty >= itemModel.getTotalQty() ? R.string.txt_ean_green : R.string.txt_ean : R.string.txt_ean), itemModel.getEan())
              + (frag instanceof SearchListFragment || frag instanceof OutwardPickListDetailsFragment ? String.format(context.getString(R.string.txt_append_qty), context.getColorCode(eanFoundQty > 0 && eanFoundQty >= itemModel.getTotalQty() ? R.color.green : eanFoundQty > 0 ? R.color.orange : R.color.err_red), "" + eanFoundQty, "" + itemModel.getTotalQtyStr()) : ""), HtmlCompat.FROM_HTML_MODE_LEGACY));*/
      holder.txtHeader.setVisibility(!samePreEan ? View.VISIBLE : View.GONE);
      
      if(frag instanceof OmniChannelListDetailsFragment){
        final OmniChannelListDetailsFragment omniListDtlsFragment = (OmniChannelListDetailsFragment) frag;
        final boolean isEANSearch = omniListDtlsFragment.isEANSearch();
        final boolean isAllowDecode = omniListDtlsFragment.isAllowDecode();
        final boolean isStatusVerified = omniListDtlsFragment.isStatusVerified();
        
        holder.txtHeader.setVisibility(View.GONE);
        if(true){//!isUseNewUIForLBS){
          ((LinearLayout) holder.txtEAN.getParent()).setVisibility(View.GONE);
          ((LinearLayout) holder.img.getParent()).setVisibility(!samePreEan ? View.VISIBLE : View.GONE);
          ((LinearLayout) holder.img.getParent()).setBackgroundResource(R.drawable.border_side);
          ((LinearLayout) holder.txtTotalQty.getParent()).setVisibility(View.VISIBLE);
          ((LinearLayout) holder.txtTotalQty.getParent()).setBackgroundResource(sameNextEan ? R.drawable.border_except_top_no_corner : R.drawable.border_except_top_corner_bottom);
        }
        ((LinearLayout) holder.txtHeaderEan.getParent()).setVisibility(!samePreEan ? View.VISIBLE : View.GONE);
        ((LinearLayout) holder.lblFooterLoc.getParent()).setVisibility(!samePreEan ? View.VISIBLE : View.GONE);
        
        holder.lblDecodedQty.setVisibility(isAllowDecode ? View.VISIBLE : View.GONE);
        holder.txtDecodedQty.setVisibility(isAllowDecode ? View.VISIBLE : View.GONE);
        //holder.txtHeaderEan.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean)), itemModel.getEan()), HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.txtHeaderEan.setText(HtmlCompat.fromHtml(itemModel.getEanTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.txtOrderQty.setText(HtmlCompat.fromHtml(String.format(context.getString(frag instanceof SearchListFragment ? isAllowDecode ? R.string.txt_remaining_qty : R.string.txt_search_qty : R.string.txt_order_qty), isAllowDecode && frag instanceof SearchListFragment ? itemModel.getQty() - chkNull(AppDatabase.getProductDao(context).getTotalCounts(AppCommonMethods.SessionType.SEARCH_LIST.getValue(), itemModel.getEan()).decoded, 0) : itemModel.getQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.txtOrderQty.setVisibility(itemModel.getQty() > 0 ? View.VISIBLE : View.GONE);
        holder.txtAvailableQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_available_qty), itemModel.getTotalQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
        
        holder.lblFooterLoc.setText(HtmlCompat.fromHtml(SharedPrefManager.getString(ParamConstants.LABEL_ZONES,context.getString(R.string.lbl_loc)), HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.txtFooterLoc.setText(HtmlCompat.fromHtml(itemModel.getZone(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.txtTotalQty.setText(HtmlCompat.fromHtml(itemModel.getEanQtyStr(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.txtFoundQty.setText(HtmlCompat.fromHtml(itemModel.getFoundQtyStr(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.txtDecodedQty.setText(HtmlCompat.fromHtml(itemModel.getDecodedQtyStr(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        
        holder.txtFoundQty.setTextColor(ContextCompat.getColor(context, itemModel.getFoundQty() > 0 && itemModel.getEanQty() > 0 && itemModel.getEanQty() <= itemModel.getFoundQty() ? R.color.green : itemModel.getFoundQty() > 0 ? R.color.orange : R.color.err_red));
        holder.txtDecodedQty.setTextColor(ContextCompat.getColor(context, itemModel.getDecodedQty() > 0 && itemModel.getEanQty() > 0 && itemModel.getEanQty() <= itemModel.getDecodedQty() ? R.color.green : itemModel.getDecodedQty() > 0 ? R.color.orange : R.color.err_red));
        holder.txtAvailableQty.setTextColor(ContextCompat.getColor(context, itemModel.getQty() >= 0 && itemModel.getTotalQty() >= 0 && itemModel.getTotalQty() >= itemModel.getQty() ? R.color.txt_regular : R.color.err_red));
      }
      else if(frag instanceof SearchListFragment || frag instanceof SearchAssortListFragment){
        if(isUseNewUIForLBS){
          final boolean isAllowDecode = frag instanceof SearchListFragment ? ((SearchListFragment) frag).isAllowDecode() :/*frag instanceof SearchAssortListFragment?((SearchAssortListFragment)frag).isAllowDecode():*/false;
          holder.txtHeader.setVisibility(View.GONE);
          ((LinearLayout) holder.txtEAN.getParent()).setVisibility(View.GONE);
          holder.txtFoundQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_found), itemModel.getFoundQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
          holder.txtFoundQty.setTextColor(ContextCompat.getColor(context, itemModel.getFoundQty() > 0 && itemModel.getEanQty() > 0 && itemModel.getEanQty() <= itemModel.getFoundQty() ? R.color.green : itemModel.getFoundQty() > 0 ? R.color.orange : R.color.err_red));
          holder.txtFoundQty.setVisibility(View.VISIBLE);
          holder.txtDecodedQty.setVisibility(isAllowDecode ? View.VISIBLE : View.GONE);
          //holder.txtFooterLoc.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_ZONES,context.getString(R.string.lbl_loc)), "" + itemModel.getZone()), HtmlCompat.FROM_HTML_MODE_LEGACY));
          holder.txtFooterLoc.setText(HtmlCompat.fromHtml(itemModel.getZoneTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
          holder.txtTotalQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_qty), "" + itemModel.getEanQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
          holder.txtDecodedQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_decoded), itemModel.getDecodedQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
          holder.txtDecodedQty.setTextColor(ContextCompat.getColor(context, itemModel.getDecodedQty() > 0 && itemModel.getEanQty() > 0 && itemModel.getEanQty() <= itemModel.getDecodedQty() ? R.color.green : itemModel.getDecodedQty() > 0 ? R.color.orange : R.color.err_red));
          
          ((LinearLayout) holder.txtHeaderEan.getParent()).setVisibility(!samePreEan ? View.VISIBLE : View.GONE);
          
          //holder.txtHeaderEan.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean)), itemModel.getEan()), HtmlCompat.FROM_HTML_MODE_LEGACY));
          holder.txtHeaderEan.setText(HtmlCompat.fromHtml(itemModel.getEanTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
          holder.txtOrderQty.setText(HtmlCompat.fromHtml(String.format(context.getString(frag instanceof SearchListFragment ? isAllowDecode ? R.string.txt_remaining_qty : R.string.txt_search_qty : R.string.txt_order_qty), isAllowDecode && frag instanceof SearchListFragment ? itemModel.getQty() - chkNull(AppDatabase.getProductDao(context).getTotalCounts(AppCommonMethods.SessionType.SEARCH_LIST.getValue(), itemModel.getEan()).decoded, 0) : itemModel.getQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
          holder.txtOrderQty.setVisibility(itemModel.getQty() > 0 ? View.VISIBLE : View.GONE);
          holder.txtAvailableQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_available_qty), itemModel.getTotalQtyStr()), HtmlCompat.FROM_HTML_MODE_LEGACY));
          
          holder.txtAvailableQty.setTextColor(ContextCompat.getColor(context, itemModel.getQty() >= 0 && itemModel.getTotalQty() >= 0 && itemModel.getTotalQty() >= itemModel.getQty() ? R.color.txt_regular : R.color.err_red));
        }
        else{
          //holder.txtEAN.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_NAME,context.getString(R.string.lbl_name)), itemModel.getName()), HtmlCompat.FROM_HTML_MODE_LEGACY));
          holder.txtEAN.setText(HtmlCompat.fromHtml(itemModel.getNameTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
          final boolean isHexColorCode = itemModel.getColor().matches(AppConstants.REGEX_HEX_COLOR_CODE);
          context.setTextAppearance(holder.txtName, isHexColorCode ? R.style.TextStyleSmallAwesome : R.style.TextStyleSmall);
          //holder.txtName.setText(HtmlCompat.fromHtml(String.format(context.getString(isHexColorCode ? R.string.txt_color_code : R.string.txt_lbl_color_name), itemModel.getColor()), HtmlCompat.FROM_HTML_MODE_LEGACY));
          holder.txtName.setText(HtmlCompat.fromHtml(itemModel.getColorTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
          holder.txtColor.setVisibility(View.GONE);
          ((LinearLayout) holder.img.getParent()).setBackgroundResource(sameNextEan ? R.drawable.border_except_top_no_corner : R.drawable.border_except_top);
        }
        holder.txtQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_qty), String.format(context.getString(R.string.txt_append_qty), context.getColorCode(itemModel.getFoundQty() > 0 && itemModel.getFoundQty() >= itemModel.getEanQty() ? R.color.green : itemModel.getFoundQty() > 0 ? R.color.orange : R.color.err_red), itemModel.getFoundQtyStr(), "" + itemModel.getEanQty())), HtmlCompat.FROM_HTML_MODE_LEGACY));
      }
      
      ((LinearLayout) holder.txtTotalQty.getParent()).setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
          Bundle args = frag != null ? chkNull(frag.getArguments(), new Bundle()) : new Bundle();
          args.putSerializable(itemModel.getClass().getSimpleName(), itemModel);
          if(AppCommonMethods.isNonEmpty(itemModel.getErrStockMsg()))
            context.showCustomErrDialog(itemModel.getErrStockMsg());
          else if(isGlobalQtyCheck && !(frag instanceof SearchListFragment) && itemModel.getEanQty() <= 0)
            context.showCustomErrDialog(String.format(context.getString(R.string.err_no_qty), frag.getTypeCharCode()));
          else if(isUseNewUIForLBS && frag != null && frag instanceof SearchListFragment){
            final SearchListFragment searchListFragment = ((SearchListFragment) frag);
            /*if(!isGlobalQtyCheck && itemModel.getEanQty() <= 0)
            context.showCustomErrDialog(R.string.err_no_qty_search);
            else{*/
            args.putString(AppConstants.SEARCH_LIST_ID, searchListFragment.getSearchListId());
            args.putString(AppConstants.SEARCH_LIST_TYPE, searchListFragment.getSearchListType());
            args.putString(AppConstants.BRAND, searchListFragment.getSelBrand());
            args.putString(AppConstants.CATEGORY, searchListFragment.getSelCategory());
            if(SharedPrefManager.getIsEANMapped()){
              try{
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put(ParamConstants.EAN, itemModel.getEan());
                jsonRequest.put(ParamConstants.EPC, "");
                searchListFragment.callWebService(URLConstants.GET_MAPPED_EAN, jsonRequest, args, context.getString(R.string.progress_msg_check_map_data));
              }
              catch(Exception e){ e.printStackTrace(); }
            }
            else{
              context.checkReaderConnection(new SearchListStartFragment(), args);
            }
          }
          else if(isUseNewUIForLBS && frag != null && frag instanceof SearchAssortListFragment){
            final SearchAssortListFragment searchAssortListFragment = ((SearchAssortListFragment) frag);
            /*if(!isGlobalQtyCheck && itemModel.getEanQty() <= 0)
            context.showCustomErrDialog(R.string.err_no_qty_search);
            else{*/
            args.putString(AppConstants.SEARCH_LIST_ID, searchAssortListFragment.getSearchAssortmentListId());
            args.putString(AppConstants.SEARCH_LIST_TYPE, searchAssortListFragment.getSearchAssortmentListType());
            args.putString(AppConstants.SEARCH_ASSORTMENT_CODE, searchAssortListFragment.getSearchAssortmentCode());
            args.putString(AppConstants.SEARCH_ASSORTMENT_PRIORITY, searchAssortListFragment.getSearchAssortmentPriority());
            args.putString(AppConstants.BRAND, searchAssortListFragment.getSelBrand());
            args.putString(AppConstants.CATEGORY, searchAssortListFragment.getSelCategory());
            if(SharedPrefManager.getIsEANMapped()){
              try{
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put(ParamConstants.EAN, itemModel.getEan());
                jsonRequest.put(ParamConstants.EPC, "");
                searchAssortListFragment.callWebService(URLConstants.GET_MAPPED_EAN, jsonRequest, args, context.getString(R.string.progress_msg_check_map_data));
              }
              catch(Exception e){ e.printStackTrace(); }
            }
            else{
              context.checkReaderConnection(new SearchAssortStartFragment(), args);
            }
          }
          else if(frag != null && frag instanceof OmniChannelListDetailsFragment){
            if(!isGlobalQtyCheck && itemModel.getEanQty() <= 0)
              context.showCustomErrDialog(R.string.err_no_qty_search);
            else{
              final OmniChannelListDetailsFragment omniListDtlsFragment = (OmniChannelListDetailsFragment) frag;
              final boolean isEanSearch = omniListDtlsFragment.isEANSearch();
              args.putBoolean(AppConstants.IS_EAN_SEARCH, isEanSearch);
              args.putBoolean(AppConstants.IS_ALLOW_DECODE, omniListDtlsFragment.isAllowDecode());
              args.putBoolean(AppConstants.IS_ALLOW_DECODE_ON_PICK, omniListDtlsFragment.isAllowDecodeOnPick());
              args.putBoolean(AppConstants.IS_ALLOW_DECODE_WITHOUT_VERIFY, omniListDtlsFragment.isAllowDecodeWithoutVerify());
              args.putBoolean(AppConstants.IS_STATUS_VERIFIED, omniListDtlsFragment.isStatusVerified());
              args.putString(AppConstants.HEADER_ORDER_NO_EAN, String.format(context.getString(R.string.txt__no),SharedPrefManager.getString(isEanSearch?ParamConstants.LABEL_EANS:ParamConstants.ORDER_NO,context.getString(isEanSearch ? R.string.lbl_ean_no : R.string.lbl_order_no).replaceFirst("/", "")), isEanSearch ? itemModel.getEan() : itemModel.getOrderNo()));
              args.putSerializable(AppConstants.OMNICHANNEL_TYPE, omniListDtlsFragment.getOmnichannelType());
              args.putSerializable(AppConstants.OMNICHANNEL_UPLOAD_TYPE, omniListDtlsFragment.getOmnichannelUploadType());
              if(SharedPrefManager.getIsEANMapped()){
                try{
                  JSONObject jsonRequest = new JSONObject();
                  jsonRequest.put(ParamConstants.EAN, itemModel.getEan());
                  jsonRequest.put(ParamConstants.EPC, "");
                  omniListDtlsFragment.callWebService(URLConstants.GET_MAPPED_EAN, jsonRequest, args, context.getString(R.string.progress_msg_check_map_data));
                }
                catch(Exception e){ e.printStackTrace(); }
              }
              else{
                context.loadFragment(new OmniChannelStartFragment(), args);
              }
            }
          }
        }
      });
    }
    
    ((LinearLayout.LayoutParams) holder.txtCategory.getLayoutParams()).weight = holder.txtExtra.getVisibility() == View.VISIBLE ? 2.0f : 1.375f;
    ((LinearLayout.LayoutParams) holder.txtLoc.getLayoutParams()).weight = holder.txtQty.getVisibility() == View.VISIBLE ? 2.0f : 1.375f;
    ((LinearLayout.LayoutParams) holder.txtSize.getLayoutParams()).weight = holder.txtColor.getVisibility() == View.VISIBLE ? 2.0f : 1.375f;
    
    
    
    if(!(!isUseNewUIForLBS && (frag instanceof SearchListFragment || frag instanceof SearchAssortListFragment))){
      //holder.txtEAN.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean)), itemModel.getEan()), HtmlCompat.FROM_HTML_MODE_LEGACY));
      holder.txtEAN.setText(HtmlCompat.fromHtml(itemModel.getEanTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
      if(!(frag instanceof OffRangeListFragment || frag instanceof SearchListExcelFragment))
       //holder.txtName.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_NAME,context.getString(R.string.lbl_name)), itemModel.getName()), HtmlCompat.FROM_HTML_MODE_LEGACY));
       holder.txtName.setText(HtmlCompat.fromHtml(itemModel.getNameTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    final boolean isHexColorCode = itemModel.getColor().matches(AppConstants.REGEX_HEX_COLOR_CODE);
    context.setTextAppearance(holder.txtColor, isHexColorCode ? R.style.TextStyleSmallAwesome : R.style.TextStyleSmall);
    //holder.txtColor.setText(HtmlCompat.fromHtml(String.format(context.getString(isHexColorCode ? R.string.txt_color_code : R.string.txt_lbl_color_name), itemModel.getColor()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    //holder.txtColor.setText(HtmlCompat.fromHtml(isHexColorCode?String.format(context.getString(R.string.txt_color_code), itemModel.getColor()):String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_COLORS,context.getString(R.string.lbl_color)), itemModel.getColor()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtColor.setText(HtmlCompat.fromHtml(itemModel.getColorTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
    if(!(frag instanceof OffRangeListFragment || frag instanceof SearchListExcelFragment))
      //holder.txtLoc.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_ZONES,context.getString(R.string.lbl_loc)), itemModel.getZone()), HtmlCompat.FROM_HTML_MODE_LEGACY));
      holder.txtLoc.setText(HtmlCompat.fromHtml(itemModel.getZoneTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
    //holder.txtSize.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_SIZES,context.getString(R.string.lbl_size)), itemModel.getSize()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtSize.setText(HtmlCompat.fromHtml(itemModel.getSizeTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
    if(!(frag instanceof StockCorrectionStartFragment || frag instanceof ReplenishmentListFragment || frag instanceof SearchListFragment || frag instanceof SearchAssortListFragment || frag instanceof OffRangeListFragment || frag instanceof SearchListExcelFragment))
      holder.txtQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_qty), "" + itemModel.getEanQty()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    context.loadImage(holder.img, itemModel.getItemImgUrl());
    
    //holder.txtBrand.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_BRANDS,context.getString(R.string.lbl_brand)), "" + itemModel.getBrand()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtBrand.setText(HtmlCompat.fromHtml(itemModel.getBrandTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
   // holder.txtCategory.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__),SharedPrefManager.getString(ParamConstants.LABEL_CATEGORIES,context.getString(R.string.lbl_category)), "" + itemModel.getCategory()), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtCategory.setText(HtmlCompat.fromHtml(itemModel.getCategoryTxt(context), HtmlCompat.FROM_HTML_MODE_LEGACY));
    
    holder.itemView.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view){
        Bundle args = frag != null ? chkNull(frag.getArguments(), new Bundle()) : new Bundle();
        args.putSerializable(itemModel.getClass().getSimpleName(), itemModel);
        if(frag!=null && frag instanceof RFIDSessionFragment && ((RFIDSessionFragment)frag).isProcessOn()) return;
        if(AppCommonMethods.isNonEmpty(itemModel.getErrStockMsg()))
          context.showCustomErrDialog(itemModel.getErrStockMsg());
        else if(isGlobalQtyCheck && !(frag instanceof SearchListFragment || frag instanceof SearchListExcelFragment) && itemModel.getEanQty() <= 0)
          context.showCustomErrDialog(String.format(context.getString(R.string.err_no_qty), frag.getTypeCharCode()));
        else{
          if(frag != null && frag instanceof StockCorrectionStartFragment){
            final StockCorrectionStartFragment stockCorrectionStartFragment = (StockCorrectionStartFragment) frag;
            stockCorrectionStartFragment.showEpcList(itemModel.ean);
          }
          else if(frag != null && frag instanceof ReplenishmentListFragment){
            final ReplenishmentListFragment replenishFragment = (ReplenishmentListFragment) frag;
            if(!isGlobalQtyCheck && itemModel.getEanQty() <= 0)
              context.showCustomErrDialog(R.string.err_no_qty_replenishment);
            else{
              args.putString(AppConstants.SRC_ZONE, replenishFragment.getSelSrcZone());
              args.putString(AppConstants.DEST_ZONE, replenishFragment.getSelDestZone());
              args.putString(AppConstants.BRAND, replenishFragment.getSelBrand());
              args.putString(AppConstants.CATEGORY, replenishFragment.getSelCategory());
              args.putString(AppConstants.REPLENISHMENT_TYPE, replenishFragment.getReplenishmentType());
              if(SharedPrefManager.getIsEANMapped()){
                try{
                  JSONObject jsonRequest = new JSONObject();
                  jsonRequest.put(ParamConstants.EAN, itemModel.getEan());
                  jsonRequest.put(ParamConstants.EPC, "");
                  replenishFragment.callWebService(URLConstants.GET_MAPPED_EAN, jsonRequest, args, context.getString(R.string.progress_msg_check_map_data));
                }
                catch(Exception e){ e.printStackTrace(); }
              }
              else{
                context.checkReaderConnection(new ReplenishmentStartFragment(), args);
              }
            }
          }
          else if(frag != null && frag instanceof OffRangeListFragment){
            ((OffRangeListFragment) frag).setupProductView(itemModel);
            //context.checkReaderConnection(new OffRangeStartFragment(), args);
          }
          else if(frag != null && frag instanceof OutwardPickListDetailsFragment){
            final OutwardPickListDetailsFragment outwardPickListDetailsFragment = ((OutwardPickListDetailsFragment) frag);
          /*if(AppCommonMethods.isNonEmpty(itemModel.getErrStockMsg()))
            context.showCustomErrDialog(itemModel.getErrStockMsg());
          else */
            if(itemModel.getEanQty() <= 0) context.showCustomErrDialog(R.string.err_no_qty_outward);
            else{
              if(isNullOrEmpty(outwardPickListDetailsFragment.getHuNum())){
                outwardPickListDetailsFragment.scanHU(itemModel);
              }
              else{
                context.showCustomAlertDialog("_", String.format(context.getString(R.string.hu_process), outwardPickListDetailsFragment.getHuNum()), R.string.btn_yes, new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialog, int which){
                    args.putString(AppConstants.TRIP_NUMBER, outwardPickListDetailsFragment.getTripNum());
                    args.putString(AppConstants.TRIP_TYPE, outwardPickListDetailsFragment.getTripType());
                    args.putString(AppConstants.ACTION_TYPE, outwardPickListDetailsFragment.getActionType());
                    args.putString(AppConstants.HU_NUMBER, outwardPickListDetailsFragment.getHuNum());
                    args.putString(AppConstants.BRAND, outwardPickListDetailsFragment.getSelBrand());
                    args.putString(AppConstants.CATEGORY, outwardPickListDetailsFragment.getSelCategory());
                    if(SharedPrefManager.getIsEANMapped()){
                      try{
                        JSONObject jsonRequest = new JSONObject();
                        jsonRequest.put(ParamConstants.EAN, itemModel.getEan());
                        jsonRequest.put(ParamConstants.EPC, "");
                        outwardPickListDetailsFragment.callWebService(URLConstants.GET_MAPPED_EAN, jsonRequest, args, context.getString(R.string.progress_msg_check_map_data));
                      }
                      catch(Exception e){ e.printStackTrace(); }
                    }
                    else{
                      context.checkReaderConnection(new OutwardPickStartFragment(), args);
                    }
                  }
                }, R.string.btn_create_hu, new DialogInterface.OnClickListener(){
                  @Override
                  public void onClick(DialogInterface dialog, int which){
                    //TODO show HU scan dialog
                    outwardPickListDetailsFragment.uploadTrip(args);
                    outwardPickListDetailsFragment.scanHU(itemModel);
                  }
                });
              }
            }
          }
          else if(!isUseNewUIForLBS && frag != null && frag instanceof SearchListFragment){
            final SearchListFragment searchListFragment = ((SearchListFragment) frag);
          /*if(!isGlobalQtyCheck && itemModel.getEanQty() <= 0)
            context.showCustomErrDialog(R.string.err_no_qty_search);
          else{*/
            args.putString(AppConstants.SEARCH_LIST_ID, searchListFragment.getSearchListId());
            args.putString(AppConstants.SEARCH_LIST_TYPE, searchListFragment.getSearchListType());
            args.putString(AppConstants.BRAND, searchListFragment.getSelBrand());
            args.putString(AppConstants.CATEGORY, searchListFragment.getSelCategory());
            if(SharedPrefManager.getIsEANMapped()){
              try{
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put(ParamConstants.EAN, itemModel.getEan());
                jsonRequest.put(ParamConstants.EPC, "");
                searchListFragment.callWebService(URLConstants.GET_MAPPED_EAN, jsonRequest, args, context.getString(R.string.progress_msg_check_map_data));
              }
              catch(Exception e){ e.printStackTrace(); }
            }
            else{
              context.checkReaderConnection(new SearchListStartFragment(), args);
            }
            //}
          }
          else if(!isUseNewUIForLBS && frag != null && frag instanceof SearchAssortListFragment){
            final SearchAssortListFragment searchAssortListFragment = ((SearchAssortListFragment) frag);
          /*if(!isGlobalQtyCheck && itemModel.getEanQty() <= 0)
            context.showCustomErrDialog(R.string.err_no_qty_search);
          else{*/
            args.putString(AppConstants.SEARCH_LIST_ID, searchAssortListFragment.getSearchAssortmentListId());
            args.putString(AppConstants.SEARCH_LIST_TYPE, searchAssortListFragment.getSearchAssortmentListType());
            args.putString(AppConstants.SEARCH_ASSORTMENT_CODE, searchAssortListFragment.getSearchAssortmentCode());
            args.putString(AppConstants.SEARCH_ASSORTMENT_PRIORITY, searchAssortListFragment.getSearchAssortmentPriority());
            args.putString(AppConstants.BRAND, searchAssortListFragment.getSelBrand());
            args.putString(AppConstants.CATEGORY, searchAssortListFragment.getSelCategory());
            if(SharedPrefManager.getIsEANMapped()){
              try{
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put(ParamConstants.EAN, itemModel.getEan());
                jsonRequest.put(ParamConstants.EPC, "");
                searchAssortListFragment.callWebService(URLConstants.GET_MAPPED_EAN, jsonRequest, args, context.getString(R.string.progress_msg_check_map_data));
              }
              catch(Exception e){ e.printStackTrace(); }
            }
            else{
              context.checkReaderConnection(new SearchAssortStartFragment(), args);
            }
            //}
          }
          else if(frag != null && frag instanceof SearchListExcelFragment){
            if(AppCommonMethods.isRestrictUserForAllFound && itemModel.getFoundQty()>=chkZero(itemModel.getEanQty(),itemModel.getQty())){
              context.showCustomErrDialog(R.string.msg_all_found);//R.string.err_qty_matched);
              return;
            }
            final SearchListExcelFragment searchListExcelFragment = ((SearchListExcelFragment) frag);
            if(!searchListExcelFragment.isProcessOn()){
              args.putString(AppConstants.SEARCH_LIST_ID, searchListExcelFragment.getSearchListId());
              args.putString(AppConstants.SEARCH_LIST_TYPE, searchListExcelFragment.getSearchListType());
              args.putString(AppConstants.BRAND, searchListExcelFragment.getSelBrand());
              args.putString(AppConstants.CATEGORY, searchListExcelFragment.getSelCategory());
              context.checkReaderConnection(new SearchListExcelStartFragment(), args);
            }
          }
        }
      }
    });
  }
  
  @Override
  public int getItemCount(){
    return productList.size();
  }
  
  /**
   * The My view holder.
   */
  static class MyViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtHeader;
    TextView txtHeaderEan;
    TextView txtOrderQty;
    TextView txtAvailableQty;
    TextView txtEPC;
    TextView txtEAN;
    TextView txtName;
    TextView txtColor;
    TextView txtLoc;
    TextView txtSize;
    TextView txtQty;
    TextView txtBrand;
    TextView txtCategory;
    TextView txtExtra;
    TextView lblFooterLoc;
    TextView lblTotalQty;
    TextView lblFoundQty;
    TextView lblDecodedQty;
    TextView txtFooterLoc;
    TextView txtTotalQty;
    TextView txtFoundQty;
    TextView txtDecodedQty;
    ImageView img;
    ImageView imgNext;
    ImageView imgNextInvisible;
    LinearLayout llExtra;
    TextView txtExtra1;
    TextView txtExtra2;
    TextView txtExtra3;
    /**
     * Instantiates a new My view holder.
     *
     * @param binding the binding
     */
    MyViewHolder(ListProductDetailsBinding binding){
      super(binding.getRoot());
      img = binding.imgProduct;
      txtHeader = binding.txtProductHeader;
      txtHeaderEan = binding.txtHeaderEan;
      txtOrderQty = binding.txtOrderQty;
      txtAvailableQty = binding.txtAvailableQty;
      txtEPC = binding.txtProductEpc;
      txtEAN = binding.txtProductEan;
      txtName = binding.txtProductName;
      txtColor = binding.txtProductColor;
      txtLoc = binding.txtProductLoc;
      txtSize = binding.txtProductSize;
      txtQty = binding.txtProductQty;
      txtBrand = binding.txtProductBrand;
      txtCategory = binding.txtProductCategory;
      txtExtra = binding.txtProductExtra;
      
      lblFooterLoc = binding.lblFooterLoc;
      lblTotalQty = binding.lblTotalQty;
      lblFoundQty = binding.lblFoundQty;
      lblDecodedQty = binding.lblDecodedQty;
      
      txtFooterLoc = binding.txtFooterLoc;
      txtTotalQty = binding.txtTotalQty;
      txtFoundQty = binding.txtFoundQty;
      txtDecodedQty = binding.txtDecodedQty;
      imgNext = binding.imgNext;
      imgNextInvisible = binding.imgNextInvisible;
      
      llExtra=binding.llExtraRow;
      txtExtra1=binding.txtProductExtra1;
      txtExtra2=binding.txtProductExtra2;
      txtExtra3=binding.txtProductExtra3;
      
      txtHeader.setSelected(true);
      txtHeaderEan.setSelected(true);
      txtOrderQty.setSelected(true);
      
      txtEPC.setSelected(true);
      
      txtEAN.setSelected(true);
      txtLoc.setSelected(true);
      txtQty.setSelected(true);
      txtName.setSelected(true);
      txtSize.setSelected(true);
      txtColor.setSelected(true);
      txtBrand.setSelected(true);
      txtCategory.setSelected(true);
      txtExtra.setSelected(true);
      
      txtExtra1.setSelected(true);
      txtExtra2.setSelected(true);
      txtExtra3.setSelected(true);
      
      lblFooterLoc.setSelected(true);
      lblTotalQty.setSelected(true);
      lblFoundQty.setSelected(true);
      lblDecodedQty.setSelected(true);
      
      txtFooterLoc.setSelected(true);
      txtTotalQty.setSelected(true);
      txtFoundQty.setSelected(true);
      txtDecodedQty.setSelected(true);
    }
  }
}