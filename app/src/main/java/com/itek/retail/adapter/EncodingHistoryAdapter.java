package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.isInternetConnected;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.EncodingHistoryListBinding;
import com.itek.retail.model.EanQty;
import com.itek.retail.model.MenuModel;
import com.itek.retail.ui.decoding.DecodingStartFragment;
import com.itek.retail.ui.encoding.EncodingMainFragment;
import com.itek.retail.ui.encoding.EncodingStartFragment;
import com.itek.retail.ui.encoding.EncodingVerifyFragment;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Encoding History adapter
 * used in Encoding Dashboard and Start Encoding Screens (EncodingMainFragment & EncodingStartFragment)
 * for showing History of Encoded Tags as Ean-wise Quantity.
 * user can be redirected to 'Verify Encoding' Screen by clicking the 'RIFD' icon
 */
public class EncodingHistoryAdapter extends RecyclerView.Adapter<EncodingHistoryAdapter.ViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private List<EanQty> listEans = new ArrayList<>(0);
  private MenuModel encodeVerifyMenuModel;
  
  /**
   * Instantiates a new Encoding start history adapter.
   *
   * @param context  the context
   * @param frag     the m frag
   * @param listEans the list eans
   */
  public EncodingHistoryAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<EanQty> listEans){
    this.context = context;
    this.listEans = listEans;
    this.frag = frag;
    this.encodeVerifyMenuModel = AppDatabase.getMenuDao(context).getMenuByCode(AppConstants.MENU_CODE_ENC_VERIFY);
  }
  
  /**
   * Get item ean qty.
   *
   * @param position the position
   * @return the ean qty
   */
  public EanQty getItem(int position){
    return listEans.get(position);
  }
  
  @Override
  @NonNull
  public EncodingHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new EncodingHistoryAdapter.ViewHolder(EncodingHistoryListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public long getItemId(int position){
    return position;
  }
  
  @Override
  public int getItemCount(){ return listEans.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull EncodingHistoryAdapter.ViewHolder holder, int position){
    final EanQty itemModel = getItem(position);
    if(itemModel == null) return;
    holder.itemView.setBackgroundResource((context.isLandscape ? (position % 4 == 0 || position % 4 == 3) : position % 2 == 0) ? R.color.bgListAlternet : R.color.white);
    holder.txtStatus.setText(itemModel.toString());
    holder.imgStatusCompleted.setVisibility(encodeVerifyMenuModel != null && !(frag instanceof DecodingStartFragment) ? View.VISIBLE : View.GONE);
    holder.imgStatusCompleted1.setVisibility(/*frag instanceof EncodingMainFragment ? View.VISIBLE :*/ View.GONE);
    boolean is2Icons = holder.imgStatusCompleted1.getVisibility()==View.VISIBLE;
    ((LinearLayout.LayoutParams)holder.imgStatusCompleted.getLayoutParams()).weight=is2Icons?3:6;
    
    holder.imgStatusCompleted.setOnClickListener(v -> {
      if(encodeVerifyMenuModel != null && (frag instanceof EncodingMainFragment || !((RFIDSessionFragment)frag).isProcessOn())){
        Bundle args = new Bundle();
        args.putString(AppConstants.EAN, itemModel.getEan());
        args.putSerializable(encodeVerifyMenuModel.getClass().getSimpleName(), encodeVerifyMenuModel);
        if(frag instanceof EncodingStartFragment && ((EncodingStartFragment) frag).isSessionOn())
          ((EncodingStartFragment) frag).apiCall(AppConstants.SESSION_ACTION_PAUSE, args);
        frag.handleFragmentRedirection(new EncodingVerifyFragment(), encodeVerifyMenuModel, args);
      }
    });
    
    holder.imgStatusCompleted1.setOnClickListener(view -> {
      if(frag instanceof EncodingMainFragment){
        try{
          JSONObject jsonRequest = new JSONObject();
          jsonRequest.put(ParamConstants.EAN, itemModel.getEan());
          jsonRequest.put(ParamConstants.EPC, "");
          jsonRequest.put(ParamConstants.TID, "");
          final MenuModel menuSearchDetails = AppDatabase.getMenuDao(context).getMenuByCode(AppConstants.MENU_CODE_SER_PROD);
          Bundle args = new Bundle();
          args.putString(AppConstants.EAN, itemModel.getEan());
          args.putString(AppConstants.TITLE, "Product Details");
          args.putString(AppConstants.TITLE_LOGO_URL, menuSearchDetails != null ? menuSearchDetails.getScreenImageUrl() : "");
          args.putInt(AppConstants.TITLE_LOGO_RES_ID, menuSearchDetails != null ? menuSearchDetails.getScreenIconId(context) : R.drawable.ic_ser_prod);
          
          if(frag instanceof EncodingStartFragment && ((EncodingStartFragment) frag).isSessionOn())
            ((EncodingStartFragment) frag).apiCall(AppConstants.SESSION_ACTION_PAUSE, args);
          else if(isInternetConnected(context, false, false))
            frag.callWebService(URLConstants.GET_PRODUCT_INFO, jsonRequest, args, context.getString(R.string.progress_msg_getting_data), true);
          else
            frag.handleResponse(URLConstants.GET_PRODUCT_INFO, jsonRequest, null, -1, false, args);
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    });
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    ImageView imgStatusCompleted;
    ImageView imgStatusCompleted1;
    TextView txtStatus;
    EncodingHistoryListBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(final EncodingHistoryListBinding binding){
      super(binding.getRoot());
      txtStatus = binding.txtHistoryItemTitle;
      imgStatusCompleted = binding.imgHistoryRfid;
      imgStatusCompleted1 = binding.imgHistoryInfo;
    }
  }
}
