package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripStatusDao;
import com.itek.retail.databinding.StoreOutwardDataAdapterBinding;
import com.itek.retail.model.TripStatus;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.outward.OutwardPickListsFragment;
import com.itek.retail.ui.outward.huverification.OutwardHuDetailsFragment;
import com.itek.retail.ui.outward.huverification.OutwardHuListFragment;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Store outward data adapter.
 * used in Outward Trips Screen (OutwardTripsFragment) for showing Outward Trips
 * user will be redirected to next Trip Details Screen upon clicking '>' icon/ list item
 */
public class StoreOutwardDataAdapter extends RecyclerView.Adapter<StoreOutwardDataAdapter.ViewHolder>{
  
  TripStatusDao tripStatusDao;
  private MainActivity context;
  private CommonFragment frag;
  private List<TripStatus> listMenus = new ArrayList<>(0);
  
  /**
   * Instantiates a new Store outward data adapter.
   *
   * @param context   the context
   * @param listMenus the list menus
   */
  public StoreOutwardDataAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<TripStatus> listMenus){
    this.context = context;
    this.frag = frag;
    this.listMenus = listMenus;
    tripStatusDao = AppDatabase.getDbInstance(this.context).TripStatusDao();
  }
  
  /**
   * Get item trip status.
   *
   * @param position the position
   * @return the trip status
   */
  public TripStatus getItem(int position){
    return listMenus.get(position);
  }
  
  // inflates the cell layout from xml when needed
  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new ViewHolder(StoreOutwardDataAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position){
    final TripStatus itemModel = getItem(position);
    
    holder.textListNumber.setText(itemModel.getTripNumber());
    holder.textExpQty.setText("" + itemModel.getNumberOfHu());
    holder.textPickQty.setText("" + itemModel.getVerifiedHu());
    holder.textStatus.setText(itemModel.getStatus());
    
    if(itemModel.getStatus().equalsIgnoreCase(AppConstants.TRIP_STATUS_COMPLETED)){
      holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
    }
    
    holder.itemView.setBackgroundResource(position % 2 == 0 ? R.color.bgListAlternet : R.color.white);
    
    holder.itemView.setOnClickListener(v -> {
      Bundle arg = chkNull(frag.getArguments(), new Bundle());
      arg.putSerializable(itemModel.getClass().getSimpleName(), (Serializable) itemModel);
      arg.putString(AppConstants.TRIP_NUMBER, itemModel.getTripNumber());
      arg.putString(AppConstants.TRIP_TYPE, itemModel.getTripType());
      arg.putString(AppConstants.ACTION_TYPE, itemModel.getType());
      if(frag instanceof OutwardPickListsFragment){
        final OutwardPickListsFragment outwardPickListsFragment = (OutwardPickListsFragment) frag;
        try{
          JSONObject jsonRequest = new JSONObject();
          jsonRequest.put(ParamConstants.K_TRIP_NUMBER, itemModel.getTripNumber());
          jsonRequest.put(ParamConstants.K_TRIP_TYPE, itemModel.getTripType());
          jsonRequest.put(ParamConstants.TYPE, chkNull(itemModel.getType(), AppConstants.OUTWARD_PICK));
          outwardPickListsFragment.callWebService(URLConstants.GET_OUTWARD_PICK_LIST_DETAILS, jsonRequest, arg, context.getString(R.string.progress_msg_getting_data), false, true);
        }
        catch(Exception e){
          e.printStackTrace();
        }
      }
      else if(frag instanceof OutwardHuListFragment){
        if(itemModel.getStatus().equalsIgnoreCase(AppConstants.TRIP_STATUS_COMPLETED))
          context.showCustomErrDialog(context.getResources().getString(R.string.err_msg_list_complete));
        else context.loadFragment(new OutwardHuDetailsFragment(), arg);
      }
    });
  }
  
  /**
   * The View holder.
   */
  
  public class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView textListNumber;
    TextView textExpQty;
    TextView textPickQty;
    TextView textStatus;
    StoreOutwardDataAdapterBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(StoreOutwardDataAdapterBinding binding){
      super(binding.getRoot());
      textListNumber = binding.textListNumber;
      textExpQty = binding.textExpQty;
      textPickQty = binding.textPickQty;
      textStatus = binding.textStatus;
    }
    
    /**
     * Bind.
     *
     * @param itemModel the item model
     */
    public void bind(final TripStatus itemModel){
      binding.setTripStatus(itemModel);
      binding.executePendingBindings();
    }
  }
}