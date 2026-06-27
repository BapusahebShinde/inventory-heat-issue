package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.ListReplenishBatchDetailsBinding;
import com.itek.retail.model.ReplenishBatchDetails;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.replenishondemand.ReplenishmentArticleListFragment;

import org.json.JSONObject;

import java.util.List;

/**
 * The Product list adapter.
 */
public class ReplenishBatchArticleListAdapter extends RecyclerView.Adapter<ReplenishBatchArticleListAdapter.MyViewHolder>{
  
  CommonActivity context;
  CommonFragment frag;
  private List<ReplenishBatchDetails> replenishBatchDetailsList;
  private boolean isShowTotalAndPickedQtyForArticle;
  
  /**
   * Instantiates a new Stock correction list adapter.
   *
   * @param context                   the CommonActivity
   * @param frag                      the CommonFragment
   * @param replenishBatchDetailsList the replenishment batch details list
   */
  public ReplenishBatchArticleListAdapter(@NonNull CommonActivity context, @NonNull CommonFragment frag, List<ReplenishBatchDetails> replenishBatchDetailsList){
    this.context = context;
    this.frag = frag;
    this.replenishBatchDetailsList = replenishBatchDetailsList;
    this.isShowTotalAndPickedQtyForArticle=AppCommonMethods.isShowTotalAndPickedQtyForArticle;
  }
  
  @Override
  public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new MyViewHolder(ListReplenishBatchDetailsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public void onBindViewHolder(MyViewHolder holder, int position){
    final ReplenishBatchDetails itemModel = replenishBatchDetailsList.get(position);
    if(itemModel == null) return;
    
    holder.txtHeaderArticle.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__), SharedPrefManager.getString(ParamConstants.LABEL_ARTICLES,context.getString(R.string.lbl_article)), itemModel.getArticle()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.txtHeaderCategory.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__), SharedPrefManager.getString(ParamConstants.LABEL_CATEGORIES,context.getString(R.string.lbl_category)), itemModel.getCategory()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.txtHeaderMatkl.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__), SharedPrefManager.getString(ParamConstants.LABEL_MATKL,context.getString(R.string.lbl_matkl)), itemModel.getMatkl()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.txtHeaderColor.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__), SharedPrefManager.getString(ParamConstants.LABEL_COLORS,context.getString(R.string.lbl_color)), itemModel.getColor()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.txtHeaderSize.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__), SharedPrefManager.getString(ParamConstants.LABEL_SIZES,context.getString(R.string.lbl_size)), itemModel.getSize()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.txtHeaderDesc.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__), SharedPrefManager.getString(ParamConstants.LABEL_DESCRIPTION,context.getString(R.string.lbl_description)), itemModel.getDescription()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.txtOrderQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_total), "" + itemModel.getTotalQty()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    //holder.txtAvailableQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_picked_qty_total), "" + itemModel.getPickQty(), "" + itemModel.getTotalQty()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.txtAvailableQty.setText(HtmlCompat.fromHtml(String.format(context.getString(isShowTotalAndPickedQtyForArticle?R.string.txt_picked_qty:R.string.txt_picked_qty_total), "" + itemModel.getPickQty(), "" + itemModel.getTotalQty()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    
    holder.txtOrderQty.setVisibility(isShowTotalAndPickedQtyForArticle?View.VISIBLE:View.GONE);
    ((LinearLayout.LayoutParams)holder.txtAvailableQty.getLayoutParams()).weight=isShowTotalAndPickedQtyForArticle?1.5f:1.75f;
    
    
    holder.txtFooterEan.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt__), SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.txt_ean)), itemModel.getEan()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    holder.txtFoundQty.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_picked_qty), "" + itemModel.getEanPickQty()), HtmlCompat.FROM_HTML_MODE_COMPACT));
    
    final int dp5 = context.getResources().getDimensionPixelOffset(R.dimen.dp_5);
    final int pos = holder.getAdapterPosition();
    showLog("pos", "" + pos);
    final boolean samePreEan = pos > 0 && itemModel.getArticle().equalsIgnoreCase(replenishBatchDetailsList.get(pos - 1).getArticle());
    final boolean sameNextEan = pos < getItemCount() - 1 && itemModel.getArticle().equalsIgnoreCase(replenishBatchDetailsList.get(pos + 1).getArticle());
    showLog("samePreEan", "" + pos + "_" + itemModel.getArticle() + (pos > 0 ? "==" + replenishBatchDetailsList.get(pos - 1).getArticle() : ""));
    showLog("sameNextEan", "" + pos + "_" + itemModel.getArticle() + (pos < getItemCount() - 1 ? "==" + replenishBatchDetailsList.get(pos + 1).getArticle() : ""));
    ((RecyclerView.LayoutParams) holder.itemView.getLayoutParams()).setMargins(dp5, !samePreEan ? dp5 : dp5 * -1, dp5, !samePreEan ? dp5 : 0);
    
    ((LinearLayout) holder.txtHeaderArticle.getParent()).setVisibility(!samePreEan ? View.VISIBLE : View.GONE);
    ((LinearLayout) holder.txtHeaderCategory.getParent()).setVisibility(!samePreEan && isNonEmpty(itemModel.getCategory()) && isNonEmpty(itemModel.getMatkl()) ? View.VISIBLE : View.GONE);
    ((LinearLayout) holder.txtHeaderColor.getParent()).setVisibility(!samePreEan && isNonEmpty(itemModel.getColor()) && isNonEmpty(itemModel.getSize()) ? View.VISIBLE : View.GONE);
    //((LinearLayout) holder.txtHeaderMatkl.getParent()).setVisibility(!samePreEan ? View.VISIBLE : View.GONE);
    ((LinearLayout) holder.txtHeaderDesc.getParent()).setVisibility(!samePreEan && isNonEmpty(itemModel.getDescription())  ? View.VISIBLE : View.GONE);
    ((LinearLayout) holder.txtTotalQty.getParent()).setBackgroundResource(sameNextEan ? R.drawable.border_except_top_no_corner : R.drawable.border_except_top_corner_bottom);
    
    holder.itemView.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v){
        if(itemModel.getTotalQty() == itemModel.getPickQty()){
          context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_qty_matched),frag.getTypeCharCode(AppCommonMethods.SessionType.REPLENISHMENT)));
          return;
        }
        
        Bundle args = chkNull(frag.getArguments(), new Bundle());
        args.putSerializable(itemModel.getClass().getSimpleName(), itemModel);
        try{
          JSONObject jsonRequest = new JSONObject();
          jsonRequest.put(ParamConstants.EAN, itemModel.getEan());
          jsonRequest.put(ParamConstants.EPC, "");
          jsonRequest.put(ParamConstants.TID, "");
          frag.callWebService(URLConstants.GET_PRODUCT_INFO, jsonRequest, args, context.getString(R.string.progress_msg_getting_data)/*,true*/);
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    });
  }
  
  @Override
  public int getItemCount(){
    return replenishBatchDetailsList.size();
  }
  
  /**
   * The My view holder.
   */
  static class MyViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtHeaderArticle;
    TextView txtHeaderCategory;
    TextView txtHeaderMatkl;
    TextView txtHeaderColor;
    TextView txtHeaderSize;
    TextView txtHeaderDesc;
    TextView txtOrderQty;
    TextView txtAvailableQty;
    TextView txtFooterEan;
    TextView txtTotalQty;
    TextView txtFoundQty;
    ImageView imgNext;
    ImageView imgNextInvisible;
    
    /**
     * Instantiates a new My view holder.
     *
     * @param binding the binding
     */
    MyViewHolder(ListReplenishBatchDetailsBinding binding){
      super(binding.getRoot());
      txtHeaderArticle = binding.txtHeaderArticle;
      txtHeaderCategory = binding.txtHeaderCategory;
      txtHeaderMatkl = binding.txtHeaderMatkl;
      txtHeaderColor = binding.txtHeaderColor;
      txtHeaderSize = binding.txtHeaderSize;
      txtHeaderDesc = binding.txtHeaderDesc;
      txtOrderQty = binding.txtOrderQty;
      txtAvailableQty = binding.txtAvailableQty;
      
      txtFooterEan = binding.txtFooterEan;
      txtTotalQty = binding.txtTotalQty;
      txtFoundQty = binding.txtFoundQty;
      
      imgNext = binding.imgNext;
      imgNextInvisible = binding.imgNextInvisible;
      
      txtHeaderArticle.setSelected(true);
      txtHeaderCategory.setSelected(true);
      txtHeaderMatkl.setSelected(true);
      txtHeaderColor.setSelected(true);
      txtHeaderSize.setSelected(true);
      txtHeaderDesc.setSelected(true);
      txtOrderQty.setSelected(true);
      txtAvailableQty.setSelected(true);
      
      txtFooterEan.setSelected(true);
      txtTotalQty.setSelected(true);
      txtFoundQty.setSelected(true);
    }
  }
}