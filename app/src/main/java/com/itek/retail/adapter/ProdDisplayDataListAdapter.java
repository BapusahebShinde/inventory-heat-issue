package com.itek.retail.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.AppConstants;
import com.itek.retail.databinding.ViewLabelValuePairBinding;
import com.itek.retail.model.LabelValues;
import com.itek.retail.ui.home.MainActivity;

import java.util.List;

/**
 * The Prod display data list adapter.
 */
public class ProdDisplayDataListAdapter extends RecyclerView.Adapter<ProdDisplayDataListAdapter.MyViewHolder>{
  
  private MainActivity context;
  private List<LabelValues> listLabelValues;
  private boolean isVerticalView = false;
  private boolean isMismatch = false;
  private boolean isSameRatioForLabelValue = false;
  private boolean isInwardDialog = false;
  
  /**
   * Instantiates a new Prod display data list adapter.
   *
   * @param context         the context
   * @param listLabelValues the list label values
   */
  public ProdDisplayDataListAdapter(@NonNull MainActivity context, List<LabelValues> listLabelValues){
    this.context = context;
    this.listLabelValues = listLabelValues;
  }
  
  /**
   * Instantiates a new Prod display data list adapter.
   *
   * @param context         the context
   * @param listLabelValues the list label values
   * @param isInwardDialog  the is inward dialog
   */
  public ProdDisplayDataListAdapter(@NonNull MainActivity context, List<LabelValues> listLabelValues, boolean isInwardDialog){
    this.context = context;
    this.listLabelValues = listLabelValues;
    this.isInwardDialog = isInwardDialog;
  }
  
  /**
   * Instantiates a new Prod display data list adapter.
   *
   * @param context         the context
   * @param listLabelValues the list label values
   * @param isVerticalView  the is vertical view
   * @param isMismatch      the is mismatch
   */
  public ProdDisplayDataListAdapter(@NonNull MainActivity context, List<LabelValues> listLabelValues, boolean isVerticalView, boolean isMismatch, boolean isSameRatioForLabelValue){
    this.context = context;
    this.listLabelValues = listLabelValues;
    this.isVerticalView = isVerticalView;
    this.isMismatch = isMismatch;
    this.isSameRatioForLabelValue = isSameRatioForLabelValue;
  }
  
  @NonNull
  @Override
  public ProdDisplayDataListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    return new ProdDisplayDataListAdapter.MyViewHolder(ViewLabelValuePairBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public void onBindViewHolder(ProdDisplayDataListAdapter.MyViewHolder holder, final int position){
    final LabelValues itemModel = listLabelValues.get(position);
    if(itemModel == null) return;
    final int margin = context.getResources().getDimensionPixelSize(R.dimen.dp_5);
    holder.itemView.setPadding(isInwardDialog ? margin : 0, margin, isInwardDialog ? margin : 0, margin);
    holder.itemView.setBackgroundResource(position % 2 == 0 ? R.color.white : R.color.bgListAlternet);
    if(isInwardDialog){
      ((LinearLayout.LayoutParams) holder.txtLbl.getLayoutParams()).weight = 1.0f;
      ((LinearLayout.LayoutParams) holder.txtVals.getLayoutParams()).weight = 2.0f;
    }
    if(isSameRatioForLabelValue){
      ((LinearLayout.LayoutParams) holder.txtLbl.getLayoutParams()).weight = 1.0f;
      ((LinearLayout.LayoutParams) holder.txtVals.getLayoutParams()).weight = 1.0f;
    }
    holder.txtLbl.setText(HtmlCompat.fromHtml(itemModel.getLabel(), HtmlCompat.FROM_HTML_MODE_LEGACY));
    final boolean isHexColorCode = itemModel.getValue().matches(AppConstants.REGEX_HEX_COLOR_CODE);
    context.setTextAppearance(holder.txtVals, isHexColorCode ? R.style.TextStyleSubSubHeaderAwesome : R.style.TextStyleSubSubHeader);
    holder.txtVals.setText(HtmlCompat.fromHtml(isHexColorCode ? String.format(context.getString(R.string.txt_color_code), itemModel.getValue()) : itemModel.getValue(), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtVals.setTextColor(ContextCompat.getColor(context, isMismatch && itemModel.getLabel().equalsIgnoreCase(context.getString(R.string.lbl_ean)) ? R.color.err_red : R.color.txt_sub_sub_header));
  }
  
  @Override
  public int getItemCount(){
    return listLabelValues.size();
  }
  
  /**
   * The My view holder.
   */
  static class MyViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtLbl, txtVals, txtDiv;
    
    /**
     * Instantiates a new My view holder.
     *
     * @param binding the binding
     */
    MyViewHolder(@NonNull ViewLabelValuePairBinding binding){
      super(binding.getRoot());
      txtLbl = binding.txtRowLabel;
      txtVals = binding.txtRowValue;
      txtDiv = binding.txtRowDiv;
      txtLbl.setSelected(true);
      txtVals.setSelected(true);
    }
  }
}