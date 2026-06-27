package com.itek.retail.adapter;

import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.databinding.ListMovementEansBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.movement.MovementStartFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The Movement scan adapter.
 * used in Movement Start Fragment
 * for showing the Picked Tags for Moving as Ean-wise Quantity
 */
public class MovementScanAdapter extends RecyclerView.Adapter<MovementScanAdapter.ViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private List<EanQty> listMenus = new ArrayList<>(0);
  
  /**
   * Instantiates a new Movement scan adapter.
   *
   * @param context   the context
   * @param listMenus the list menus
   */
  public MovementScanAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<EanQty> listMenus){
    this.context = context;
    this.frag = frag;
    this.listMenus = listMenus;
  }
  
  /**
   * Get item ean qty.
   *
   * @param position the position
   * @return the ean qty
   */
  public EanQty getItem(int position){
    return listMenus.get(position);
  }
  
  @Override
  @NonNull
  public MovementScanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new MovementScanAdapter.ViewHolder(ListMovementEansBinding.inflate(context.getLayoutInflater(), parent, false));
  }
  
  @Override
  public long getItemId(int position){
    return position;
  }
  
  @Override
  public int getItemCount(){ return listMenus.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull MovementScanAdapter.ViewHolder holder, int position){
    final EanQty itemModel = getItem(position);
    if(itemModel == null) return;
    holder.itemView.setBackgroundResource((context.isLandscape ? (position % 4 == 0 || position % 4 == 3) : position % 2 == 0) ? R.color.bgListAlternet : R.color.white);
    holder.txtEan.setText(itemModel.getEan());
    holder.txtEanQty.setText("" + itemModel.getEanQty());
    holder.imgDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
          if (view != null && view.getVisibility() == View.VISIBLE && (!(frag instanceof RFIDSessionFragment) || !((RFIDSessionFragment)frag).isProcessOn())){
             context.showCustomConfirmDialog(String.format(context.getString(R.string.msg_delete_ean),itemModel.getEan()), context.getString(R.string.btn_delete_all), new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialogInterface, int i) {
                  if (frag instanceof MovementStartFragment)
                    ((MovementStartFragment)frag).deleteSelectedEan(itemModel);
               }
             });
          }
      }
    });
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtEan;
    TextView txtEanQty;

    ImageView imgDelete;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ListMovementEansBinding binding){
      super(binding.getRoot());
      txtEan = binding.txtEan;
      txtEanQty = binding.txtEanQty;
      imgDelete = binding.imgDelete;
    }
  }
}
