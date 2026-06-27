package com.itek.retail.adapter;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.databinding.ListHuscanArticalwiseBinding;
import com.itek.retail.databinding.ListHuscanDtlsBinding;
import com.itek.retail.model.TripInventory;
import com.itek.retail.ui.inward.grn.StoreTripDetailsData;
import com.itek.retail.ui.inward1.HuProcessStartFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The Store inward scan adapter.
 */
public class HUProcessScanAdapter extends RecyclerView.Adapter<HUProcessScanAdapter.ViewHolder>{
  
  private CommonActivity context;
  private CommonFragment frag;
  private TripInventoryDao tripInventoryDao;
  private List<TripInventory> listMenus = new ArrayList<>(0);
  //private List<TripInventory> dataList = new ArrayList<>(0);
  private boolean hasArticleData = false;
  private boolean isManualTrip = false;
  private boolean isInwardWithSerial = false;
  
  /**
   * Instantiates a new Store inward scan adapter.
   *
   * @param context   the context
   * @param listMenus the list menus
   */
  public HUProcessScanAdapter(@NonNull CommonActivity context, @NonNull CommonFragment frag, List<TripInventory> listMenus){
    this.context = context;
    this.frag = frag;
    this.listMenus = listMenus;
    if(frag instanceof HuProcessStartFragment) {
      HuProcessStartFragment frag1 = (HuProcessStartFragment) frag;
      this.isInwardWithSerial=frag1.isInwardWithSerial();
      this.hasArticleData = frag1.hasArticleData();
      this.isManualTrip = frag1.isManualTrip();
      AppDatabase db = AppDatabase.getDbInstance(context);
      tripInventoryDao = db.TripInventoryDao();
    }
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
    ListHuscanDtlsBinding binding = ListHuscanDtlsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
    holder.itemView.setBackgroundResource(/*position % 2 == 0 ? R.color.graywhite : */R.color.white);
    holder.textScanEanNo.setText(hasArticleData ? itemModel.articleCode : itemModel.ean);
    //holder.textScanSerialNo.setText(itemModel.serialNo);
    //holder.textScanSerialNo.setVisibility(isInwardWithSerial?View.VISIBLE:View.GONE);
    holder.textScanQty.setText("" + itemModel.scanCount);
    holder.textScanEanQty.setText("" + itemModel.eanQty);
    holder.textScanEanQty.setVisibility(!isManualTrip || !AppCommonMethods.isHideEanExpQtyColumnForManualHU ? View.VISIBLE : View.GONE);
    
    holder.imgeanInfo.setVisibility(hasArticleData || isInwardWithSerial ? View.VISIBLE : View.GONE);
    holder.imgeanInfo.setImageResource(R.drawable.ic_list_info);
    
    /*if(itemModel.articleCode == null){
      holder.textScanEanNo.setText(AppConstants.UNKNOWN);
    }*/
    
    //if(itemModel.isServerEntry)
    if(itemModel.isOriginal && itemModel.eanQty > 0 && itemModel.scanCount == itemModel.eanQty)
      holder.itemView.setBackgroundResource(R.color.greenlight);
    else if(AppCommonMethods.isShowRedBgWhileScanningHU && !isManualTrip && (itemModel.ean.equalsIgnoreCase(AppConstants.NON_ENCODED) || !itemModel.isOriginal || itemModel.scanCount > itemModel.eanQty))
      holder.itemView.setBackgroundResource(R.color.red_bg);
    else if(AppCommonMethods.isShowRedBgWhileScanningHU && isManualTrip && itemModel.ean.equalsIgnoreCase(AppConstants.NON_ENCODED))
      holder.itemView.setBackgroundResource(R.color.red_bg);
    
    holder.imgeanInfo.setOnClickListener(v -> {
      if(v != null && v.getVisibility() == View.VISIBLE){
        showLog("STATUS", "" + itemModel.reason);
        final boolean isArticleUnknownOrUnencoded = itemModel.articleCode.matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.UNKNOWN + ")");
        
        String serialMsg="";
        String message = "";
        if(isInwardWithSerial){
          final List<String> serialList = tripInventoryDao.getCurrentHuSerialDetails(itemModel.huNo, hasArticleData && !isArticleUnknownOrUnencoded ? itemModel.articleCode : itemModel.ean,itemModel.isOriginal);
          String serialNos = "";
          if(isNonEmpty(serialList))
            for(String serialNo : serialList){
              //scanCount += tripInventory.scanCount;
              if(!serialNos.contains(serialNo)) serialNos += "," + serialNo;
            }
          serialNos = serialNos.startsWith(",") ? serialNos.substring(1) : serialNos;
          serialMsg += isNonEmpty(serialNos) ? "\n" + SharedPrefManager.getString(ParamConstants.SERIAL,context.getString(R.string.lbl_serial)) + ": " + serialNos +"\n\n" : "";
        }
        
        if(hasArticleData){
          final List<TripInventory> dataList = !isArticleUnknownOrUnencoded ? tripInventoryDao.getCurrentHuArticalDetails(itemModel.huNo, itemModel.articleCode) : null;
          message = SharedPrefManager.getString(ParamConstants.LABEL_ARTICLES, context.getString(R.string.lbl_article_no)) + ": " + itemModel.articleCode;
          String eans = "";
          if(isNonEmpty(dataList)){
            for(TripInventory tripInventory : dataList){
              //scanCount += tripInventory.scanCount;
              if(!eans.contains(tripInventory.ean)) eans += "," + tripInventory.ean;
            }
          }
          
          eans = eans.startsWith(",") ? eans.substring(1) : eans;
          if(isNullOrEmpty(eans) && isArticleUnknownOrUnencoded && isNonEmpty(itemModel.articleCode)) eans = itemModel.ean;
          message += isNonEmpty(eans) ? "\n" + SharedPrefManager.getString(ParamConstants.LABEL_EANS, context.getString(R.string.lbl_ean)) + ": " + eans : "";
          message += "\n" + context.getString(R.string.lbl_scan_qty) + ": " + itemModel.scanCount;//scanCount;
        }
        else{
          message = SharedPrefManager.getString(ParamConstants.LABEL_EANS, context.getString(R.string.lbl_ean)) + ": " + itemModel.ean;
          message += "\n" + context.getString(R.string.lbl_scan_qty) + ": " + itemModel.scanCount;//scanCount;
        }
        
        context.showCustomAlertDialog(null,(serialMsg+message).trim(), R.string.btn_ok);
      }
    });
  }
  
  /**
   * The View holder.
   */
  
  public class ViewHolder extends RecyclerView.ViewHolder{
    
    TextView textScanEanNo;
    //TextView textScanSerialNo;
    TextView textScanQty;
    TextView textScanEanQty;
    ImageView imgeanInfo;
    ListHuscanArticalwiseBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(ListHuscanDtlsBinding binding){
      super(binding.getRoot());
      textScanEanNo = binding.textScanEanNo;
      //textScanSerialNo = binding.textScanSerialNo;
      textScanQty = binding.textScanQty;
      textScanEanQty = binding.textScanEanQty;
      imgeanInfo = binding.imgInfo;
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
