package com.itek.retail.adapter;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.databinding.ListHuscanArticalwiseBinding;
import com.itek.retail.model.TripInventory;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inward.grn.StoreTripDetailsData;

import java.util.ArrayList;
import java.util.List;

/**
 * The Store inward scan adapter.
 */
public class StoreInwardScanAdapter extends RecyclerView.Adapter<StoreInwardScanAdapter.ViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private TripInventoryDao tripInventoryDao;
  private List<TripInventory> listMenus = new ArrayList<>(0);
  private List<TripInventory> dataList = new ArrayList<>(0);
  
  /**
   * Instantiates a new Store inward scan adapter.
   *
   * @param context   the context
   * @param listMenus the list menus
   */
  public StoreInwardScanAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<TripInventory> listMenus){
    this.context = context;
    this.frag = frag;
    this.listMenus = listMenus;
    
    AppDatabase db = AppDatabase.getDbInstance(context);
    tripInventoryDao = db.TripInventoryDao();
    
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
    ListHuscanArticalwiseBinding binding = ListHuscanArticalwiseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new ViewHolder(binding);
  }
  
  @Override
  public long getItemId(int position){ return position; }
  
  @Override
  public int getItemCount(){ return listMenus.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position){
    final TripInventory itemModel = getItem(position);
    if(itemModel == null) return;
    holder.itemView.setBackgroundResource(position % 2 == 0 ? R.color.graywhite : R.color.white);
    holder.textScanEanNo.setText(itemModel.articleCode);
    holder.textScanQty.setText("" + itemModel.scanCount);
    holder.textScanEanQty.setText("" + itemModel.eanQty);
    holder.imgeanInfo.setVisibility(View.VISIBLE);
    holder.imgeanInfo.setImageResource(R.drawable.ic_list_info);
    
    if(itemModel.articleCode == null){
      holder.textScanEanNo.setText(AppConstants.UNKNOWN);
    }
    
    if(itemModel.isServerEntry){
      holder.itemView.setBackgroundResource(R.color.greenlight);
    }
    
    holder.imgeanInfo.setOnClickListener(v -> {
      showLog("STATUS", "" + itemModel.reason);
      //no action
      
      if(dataList != null){
        dataList.clear();
      }
      
      dataList = tripInventoryDao.getCurrentHuArticalDetails(itemModel.huNo, itemModel.articleCode);
      String message = SharedPrefManager.getString(ParamConstants.LABEL_ARTICLES,context.getString(R.string.lbl_article_no)) + ": " + itemModel.articleCode;
      
      int scanCount = 0;
      String eans = "";
      for(TripInventory tripInventory : dataList){
        scanCount += tripInventory.scanCount;
        eans += "," + tripInventory.ean;
      }
      eans = eans.startsWith(",") ? eans.substring(1) : eans;
      message += "\n" + context.getString(R.string.lbl_ean) + ": " + eans;
      message += "\n" + context.getString(R.string.lbl_scan_qty) + ": " + scanCount;
      
      context.showCustomAlertDialog(null, message, R.string.btn_ok);
      
    });
  }
  
  /**
   * The View holder.
   */
  
  public class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView textScanEanNo;
    TextView textScanQty;
    TextView textScanEanQty;
    ImageView imgeanInfo;
    ListHuscanArticalwiseBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ListHuscanArticalwiseBinding binding){
      super(binding.getRoot());
      textScanEanNo = binding.textScanEanNo;
      textScanQty = binding.textScanQty;
      textScanEanQty = binding.textScanEanQty;
      imgeanInfo = binding.imgeanInfo;
      textScanEanNo.setTextSize(COMPLEX_UNIT_SP, 14);
      textScanQty.setTextSize(COMPLEX_UNIT_SP, 14);
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
