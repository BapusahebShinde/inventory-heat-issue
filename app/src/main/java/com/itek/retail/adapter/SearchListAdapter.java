package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

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
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.ListSearchListBinding;
import com.itek.retail.model.ListModel;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.listsearch.SearchListsFragment;

import java.util.List;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.MyViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private List<ListModel> searchLists;
  
  public SearchListAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<ListModel> list){
    this.context = context;
    this.frag = frag;
    this.searchLists = list;
  }
  
  @NonNull
  @Override
  public SearchListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    return new SearchListAdapter.MyViewHolder(ListSearchListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public void onBindViewHolder(SearchListAdapter.MyViewHolder holder, int position){
    final int pos = holder.getAdapterPosition();
    final ListModel itemModel = searchLists.get(position);
    if(itemModel == null) return;
    final boolean samePreType = pos > 0 && itemModel.listType.equalsIgnoreCase(searchLists.get(pos - 1).listType);
    final boolean sameNextType = pos < getItemCount() - 1 && itemModel.listType.equalsIgnoreCase(searchLists.get(pos + 1).listType);
    holder.txtHeader.setText(itemModel.listType);
    holder.txtHeader.setVisibility(!samePreType ? View.VISIBLE : View.GONE);
    holder.txtCode.setText(HtmlCompat.fromHtml(chkNull(itemModel.listId, "").trim(), HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtQty.setText(HtmlCompat.fromHtml(itemModel.qty > 0 ? String.format(context.getString(R.string.txt_qty), "" + itemModel.qty) : "", HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtQty.setVisibility(holder.txtQty.getText().toString().trim().length() > 0 ? View.VISIBLE : View.GONE);
    holder.txtPriority.setText(HtmlCompat.fromHtml(isNonEmpty(itemModel.priority) ? String.format(context.getString(R.string.txt__), SharedPrefManager.getString(ParamConstants.LABEL_PRIORITY,context.getString(R.string.lbl_priority)), itemModel.priority) : "", HtmlCompat.FROM_HTML_MODE_LEGACY));
    holder.txtPriority.setVisibility(holder.txtPriority.getText().toString().trim().length() > 0 ? View.VISIBLE : View.GONE);
    
    ((LinearLayout) holder.txtCode.getParent()).setBackgroundResource(sameNextType ? R.drawable.border_except_top_no_corner : R.drawable.border_except_top_corner_bottom);
    holder.imgNext.setOnClickListener(view -> {
      if(frag != null && frag instanceof SearchListsFragment){
        ((SearchListsFragment) frag).callDetailsAPI(itemModel);
      }
      
    });
  }
  
  @Override
  public int getItemCount(){
    return searchLists.size();
  }
  
  static class MyViewHolder extends RecyclerView.ViewHolder{
    
    ImageView imgNext;
    TextView txtHeader;
    TextView txtCode;
    TextView txtQty;
    TextView txtPriority;
    
    MyViewHolder(ListSearchListBinding binding){
      super(binding.getRoot());
      
      imgNext = binding.imgNext;
      txtHeader = binding.txtSearchListHeader;
      txtCode = binding.txtSearchListCode;
      txtQty = binding.txtSearchListQty;
      txtPriority = binding.txtSearchListPriority;
      
      txtHeader.setSelected(true);
      txtCode.setSelected(true);
      txtQty.setSelected(true);
      txtPriority.setSelected(true);
      
    }
  }
}