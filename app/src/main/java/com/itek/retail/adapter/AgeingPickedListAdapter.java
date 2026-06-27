package com.itek.retail.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.databinding.AgeingPickListBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.search.ageing.AgeingSearchStartFragment;
import com.itek.retail.ui.search.alien.SearchAlienFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelStartFragment;
import com.itek.retail.ui.search.unencoded.SearchUnencodedFragment;

import java.util.ArrayList;
import java.util.List;

public class AgeingPickedListAdapter extends RecyclerView.Adapter<AgeingPickedListAdapter.ViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private List<Inventory> listEans = new ArrayList<>(0);
  
  /**
   * Instantiates a new Omni picked list adapter.
   *
   * @param context  the context
   * @param frag     the frag
   * @param listEans the list eans
   */
  public AgeingPickedListAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<Inventory> listEans){
    this.context = context;
    this.frag = frag;
    this.listEans = listEans;
  }
  
  /**
   * Get item inventory.
   *
   * @param position the position
   * @return the inventory
   */
  public Inventory getItem(int position){
    return listEans.get(position);
  }
  
  @Override
  @NonNull
  public AgeingPickedListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new AgeingPickedListAdapter.ViewHolder(AgeingPickListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public long getItemId(int position){
    return position;
  }
  
  @Override
  public int getItemCount(){ return listEans.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull AgeingPickedListAdapter.ViewHolder holder, int position){
    final Inventory itemModel = getItem(position);
    if(itemModel == null) return;
    holder.itemView.setBackgroundResource(position % 2 == 0 ? R.color.bgListAlternet : R.color.white);
    AppCommonMethods.showLog("unencoded_epc", AppCommonMethods.isNonEmpty(itemModel.epc) ? itemModel.epc : "empty");
    holder.txtEan.setText(frag instanceof SearchUnencodedFragment || frag instanceof SearchAlienFragment ? "****" + (itemModel.epc.length() - 4 > 4 ? itemModel.epc.substring(4, itemModel.epc.length() - 4) : "") + "****" : itemModel.ean);
    holder.txtEan.setTextColor(ContextCompat.getColor(context, (frag instanceof AgeingSearchStartFragment && itemModel.isDecoded()) || (frag instanceof SearchUnencodedFragment && itemModel.isFound) ? R.color.green : R.color.txt_regular));
    holder.imgDecode.setImageResource(frag instanceof AgeingSearchStartFragment && itemModel.isDecoded() ? R.drawable.ic_completed : R.drawable.ic_decode);
    holder.imgDecode.setOnClickListener(v -> {
      if(v.getVisibility() == View.VISIBLE && !itemModel.isDecoded() && frag != null && frag instanceof OmniChannelStartFragment){
        ((AgeingSearchStartFragment) frag).startDecode(itemModel);
      }
    });
    
    holder.imgSearch.setOnClickListener(v -> {
      if(v.getVisibility() == View.VISIBLE && frag != null && (frag instanceof AgeingSearchStartFragment || frag instanceof SearchUnencodedFragment || frag instanceof SearchAlienFragment)){
        try{
          if(frag instanceof AgeingSearchStartFragment)
            ((AgeingSearchStartFragment) frag).startEPCSearch(itemModel);
          else if(frag instanceof SearchUnencodedFragment)
            ((SearchUnencodedFragment) frag).startEPCSearch(itemModel);
          else if(frag instanceof SearchAlienFragment)
            ((SearchAlienFragment) frag).startEPCSearch(itemModel);
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    });
    
    holder.imgSearch.setVisibility(!((frag instanceof AgeingSearchStartFragment && ((AgeingSearchStartFragment) frag).isProcessOn()) || (frag instanceof SearchUnencodedFragment && ((SearchUnencodedFragment) frag).isProcessOn()) || (frag instanceof SearchAlienFragment && ((SearchAlienFragment) frag).isProcessOn())) ? View.VISIBLE : View.GONE);
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    ImageView imgDecode;
    ImageView imgSearch;
    TextView txtEan;
    AgeingPickListBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(final AgeingPickListBinding binding){
      super(binding.getRoot());
      txtEan = binding.txtEan;
      imgDecode = binding.imgDecode;
      imgSearch = binding.imgSearch;
      imgDecode.setVisibility(View.INVISIBLE);
    }
  }
}