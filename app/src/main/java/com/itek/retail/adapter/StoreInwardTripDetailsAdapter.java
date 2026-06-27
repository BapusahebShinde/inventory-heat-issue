package com.itek.retail.adapter;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.databinding.ListInwardTripDetailsBinding;
import com.itek.retail.model.TripInventory;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inward.grn.InwardGrnStartFragment;
import com.itek.retail.ui.inward.grn.StoreTripDetailsData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Store inward trip details adapter.
 */
public class StoreInwardTripDetailsAdapter extends RecyclerView.Adapter<StoreInwardTripDetailsAdapter.ViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private List<TripInventory> listMenus = new ArrayList<>(0);
  private TripInventoryDao tripInventoryDao;
  
  /**
   * Instantiates a new Store inward trip details adapter.
   *
   * @param context   the context
   * @param listMenus the list menus
   */
  public StoreInwardTripDetailsAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<TripInventory> listMenus){
    this.context = context;
    this.frag = frag;
    this.listMenus = listMenus;
    tripInventoryDao = AppDatabase.getDbInstance(context).TripInventoryDao();
  }
  
  /**
   * Get item trip inventory.
   *
   * @param position the position
   * @return the trip inventory
   */
  public TripInventory getItem(int position){
    return listMenus.get(position);
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    ListInwardTripDetailsBinding binding = ListInwardTripDetailsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }
  
  @Override
  public long getItemId(int position){
    return position;
  }
  
  @Override
  public int getItemCount(){
    return listMenus.size();
  }
  
  // binds the data to the TextView in each cell
  @SuppressLint("ResourceAsColor")
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position){
    final TripInventory itemModel = getItem(position);
    if(itemModel == null) return;
    holder.textHuNumber.setText(itemModel.huNo);
    holder.textHuNumber.setTextColor(ContextCompat.getColor(context, R.color.txt_number));
    holder.textHuQty.setText("" + itemModel.huQty);
    holder.textHuQty.setTextColor(ContextCompat.getColor(context, R.color.txt_number));
    holder.imgInfo.setImageResource(R.drawable.dummy_white_background);
    holder.imgInfo.setVisibility(View.VISIBLE);
    holder.textStatus.setVisibility(View.VISIBLE);
    String status = itemModel.status;
    switch(status){
      case "P":
        holder.textStatus.setText("");
        holder.textHuQty.setTextColor(ContextCompat.getColor(context, R.color.txt_number));
        holder.imgStatus.setVisibility(View.GONE);
        holder.imgInfo.setVisibility(View.VISIBLE);
        break;
      case "A":
        if(itemModel.reason.equalsIgnoreCase("")){
          holder.imgStatus.setVisibility(View.VISIBLE);
          holder.textStatus.setVisibility(View.VISIBLE);
          holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
          holder.textStatus.setText("" + itemModel.scanCount);
          holder.imgInfo.setVisibility(View.VISIBLE);
          holder.textHuQty.setTextColor(ContextCompat.getColor(context, R.color.green));
        }
        else{
          
          holder.imgStatus.setVisibility(View.VISIBLE);
          holder.textStatus.setVisibility(View.VISIBLE);
          holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
          holder.textStatus.setText("" + itemModel.scanCount);
          holder.imgStatus.setVisibility(View.GONE);
          holder.imgInfo.setVisibility(View.VISIBLE);
          holder.textHuQty.setTextColor(ContextCompat.getColor(context, R.color.red));
          holder.imgInfo.setImageResource(R.drawable.ic_list_info);
        }
        holder.textHuQty.setText("" + itemModel.huQty);
        break;
      case "R":
        holder.textHuQty.setTextColor(ContextCompat.getColor(context, R.color.red));
        holder.imgStatus.setVisibility(View.VISIBLE);
        holder.textStatus.setVisibility(View.VISIBLE);
        holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
        holder.textStatus.setText("" + itemModel.scanCount);
        holder.imgStatus.setVisibility(View.GONE);
        holder.textHuQty.setText("" + itemModel.huQty);
        holder.imgInfo.setVisibility(View.VISIBLE);
        holder.imgInfo.setImageResource(R.drawable.ic_list_info);
        break;
      default:
        holder.textStatus.setText("" + itemModel.scanCount);
        holder.textHuQty.setTextColor(ContextCompat.getColor(context, R.color.txt_number));
        break;
    }
    if(itemModel.isDuplicate){
      holder.itemView.setBackgroundResource(R.color.txtRed);
      holder.textStatus.setText("" + itemModel.scanCount);
    }
    else{
      holder.itemView.setBackgroundResource(R.color.bg_quick_action);
    }
    
    holder.imgInfo.setOnClickListener(v -> {
      showLog("STATUS", "" + itemModel.reason);
      if(!itemModel.reason.equalsIgnoreCase("")){
        String massage = itemModel.reason;
        String hustatus = itemModel.status;
        String statusmessage = "";
        switch(hustatus){
          case "P":
            statusmessage = "Pending";
            break;
          case "A":
            statusmessage = "HU Status – Accepted";
            break;
          case "R":
            statusmessage = "HU Status – Rejected";
            break;
          default:
            statusmessage = "";
            break;
        }
        context.showCustomAlertDialog(null, statusmessage + "\n\n" + massage, R.string.btn_ok);
      }
      
    });
    holder.itemView.setOnClickListener(v -> {
      final int totalQty = tripInventoryDao.getCurrentHuQty(itemModel.huNo, itemModel.tripNo);
      final int totalEanQty = tripInventoryDao.getCurrentHuEanQty(itemModel.huNo, itemModel.tripNo);
      final int totalArticleQty = tripInventoryDao.getCurrentHuArticleQty(itemModel.huNo, itemModel.tripNo);
      
      if(totalArticleQty <= 0){
        context.showCustomErrDialog(String.format(context.getString(R.string.err_msg_error_no_article), itemModel.huNo));// no article
        return;
      }
      else if(totalEanQty <= 0){
        context.showCustomErrDialog(String.format(context.getString(R.string.err_msg_error_no_ean), itemModel.huNo));// no ean
        return;
      }
      else if(totalQty <= 0){
        context.showCustomErrDialog(String.format(context.getString(R.string.err_msg_error_no_qty), itemModel.huNo));// no ean
        return;
      }
      //Go to scanning if it is rejected
      else if(itemModel.reason.equalsIgnoreCase("") && !itemModel.isRescan && itemModel.status.equalsIgnoreCase("A")){
        //happy flow
        context.showCustomAlertDialog(context.getString(R.string.err_title_inward_data), context.getString(R.string.err_msg_inward_data), null, false, true, context.getString(R.string.btn_ok), null, "", null);
        
      }
      else if(!itemModel.status.equalsIgnoreCase("P") && !itemModel.isDuplicate){
        //accept and reject
        context.showCustomAlertDialog(null, context.getString(R.string.err_msg_inward_delete_data), context.getString(R.string.btn_yes), (dialogInterface, i) -> {
          Bundle arg = chkNull(frag.getArguments(), new Bundle());
          arg.putSerializable(itemModel.getClass().getSimpleName(), (Serializable) itemModel);
          arg.putString(AppConstants.TRIP_NUMBER, itemModel.tripNo);
          arg.putString(AppConstants.HU_NUMBER, itemModel.huNo);
          
          tripInventoryDao.deleteHU(itemModel.huNo, itemModel.tripNo);
          tripInventoryDao.updateHUattemptcount(itemModel.huNo, itemModel.tripNo);
          context.loadFragment(new InwardGrnStartFragment(), arg);
        }, context.getString(R.string.btn_no), null);
      }
      else if(itemModel.isDuplicate){
        //duplicate or pending
        //increment attempt query
        //Go to rfid scanning page
        Bundle arg = chkNull(frag.getArguments(), new Bundle());
        arg.putSerializable(itemModel.getClass().getSimpleName(), (Serializable) itemModel);
        arg.putString(AppConstants.TRIP_NUMBER, itemModel.tripNo);
        arg.putString(AppConstants.HU_NUMBER, itemModel.huNo);
        context.loadFragment(new InwardGrnStartFragment(), arg);
        
      }
    });
    
  }
  
  /**
   * The View holder.
   */
  
  public class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView textHuNumber;
    TextView textHuQty;
    TextView textStatus;
    ImageView imgStatus;
    ImageView imgInfo;
    ListInwardTripDetailsBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ListInwardTripDetailsBinding binding){
      super(binding.getRoot());
      textHuNumber = binding.textHuNumber;
      textHuQty = binding.textHuQty;
      textStatus = binding.textStatus;
      imgStatus = binding.imgStatus;
      imgInfo = binding.imgInfo;
      textHuNumber.setTextSize(COMPLEX_UNIT_SP, 14);
      textHuQty.setTextSize(COMPLEX_UNIT_SP, 14);
      textStatus.setTextSize(COMPLEX_UNIT_SP, 14);
    }
    
    /**
     * Bind.
     *
     * @param itemModel the item model
     */
    public void bind(final StoreTripDetailsData itemModel){
      binding.setStoreTripDetailsData(itemModel);
      binding.executePendingBindings();
    }
  }
}