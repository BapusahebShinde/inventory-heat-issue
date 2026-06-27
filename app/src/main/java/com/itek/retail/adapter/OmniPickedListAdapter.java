package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
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
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.RFIDSessionFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.databinding.OmniPickListBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.ui.encoding.EncodingStartFragment;
import com.itek.retail.ui.search.alien.SearchAlienFragment;
import com.itek.retail.ui.search.fifo.SearchFIFOStartFragment;
import com.itek.retail.ui.search.filesearch.SearchFileBasedFragment;
import com.itek.retail.ui.search.listsearch.SearchListStartFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelStartFragment;
import com.itek.retail.ui.search.unencoded.SearchUnencodedFragment;
import com.itek.retail.ui.than.ThanEncodingFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Omni picked list adapter.
 * used in Omnichannel Screen (OmnichannelStartFragment)
 * for showing picked items (eans) with their decode status
 */
public class OmniPickedListAdapter extends RecyclerView.Adapter<OmniPickedListAdapter.ViewHolder>{
  
  final Timer blinkTimer = new Timer();
  private CommonActivity context;
  private RFIDSessionFragment frag;
  private boolean isAllowDecode = false;
  private boolean isShowPartialEpc = false;
  private boolean isVerifyEncoding = false;
  private boolean isShowEpcTid = false;
  private List<Inventory> listEans = new ArrayList<>(0);
  private List<TextView> listTxtErr = new ArrayList<>(0);
  
  /**
   * Instantiates a new Omni picked list adapter.
   *
   * @param context  the context
   * @param frag     the frag
   * @param listEans the list eans
   */
  public OmniPickedListAdapter(@NonNull CommonActivity context, @NonNull RFIDSessionFragment frag, List<Inventory> listEans){
    this.context = context;
    this.frag = frag;
    this.listEans = listEans;
    this.isVerifyEncoding = frag instanceof EncodingStartFragment || frag instanceof ThanEncodingFragment;// && ((VerifyEncodingFragment)frag).isVerifyEncoding();
    this.isShowEpcTid= frag instanceof SearchFileBasedFragment;
    this.isShowPartialEpc = !isVerifyEncoding && (frag instanceof SearchUnencodedFragment || frag instanceof SearchAlienFragment);
    this.isAllowDecode = frag != null && extractBoolean(frag.getArguments(), AppConstants.IS_ALLOW_DECODE, extractBoolean(frag.getArguments(), ParamConstants.IS_ALLOW_DECODE, false));
    blinkTimer.schedule(new TimerTask(){
      @Override
      public void run(){
        blink();
      }
    }, 100l, 500);
  }
  
  private void blink(){
    context.runOnUiThread(new Runnable(){
      @Override
      public void run(){
        if(isNonEmpty(listTxtErr)){
          for(TextView txtErr : listTxtErr){
            if(txtErr != null && txtErr.getVisibility() != View.GONE){
              txtErr.setVisibility(txtErr.getVisibility() == View.INVISIBLE && txtErr.getTag()!=null && txtErr.getTag() instanceof Integer &&  ((Integer)txtErr.getTag())==1 ? View.VISIBLE : View.INVISIBLE);
            }
          }
        }
      }
    });
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
  public OmniPickedListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
    return new OmniPickedListAdapter.ViewHolder(OmniPickListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public long getItemId(int position){
    return position;
  }
  
  @Override
  public int getItemCount(){ return listEans.size(); }
  
  // binds the data to the TextView in each cell
  @Override
  public void onBindViewHolder(@NonNull OmniPickedListAdapter.ViewHolder holder, int position){
    final Inventory itemModel = getItem(position);
    if(itemModel == null) return;
    holder.itemView.setBackgroundResource(position % 2 == 0 ? R.color.bgListAlternet : R.color.white);
    //    AppCommonMethods.showLog("unencoded_epc", AppCommonMethods.isNonEmpty(itemModel.epc) ? itemModel.epc : "empty");
    if(isVerifyEncoding){
      holder.txtEan.setText(itemModel.ean + " " + getStatus(itemModel.getEncVerifyStatus()) + "\n" + SharedPrefManager.getString(ParamConstants.LABEL_EPC,context.getString(R.string.lbl_epc)) + ": " + itemModel.newEpc + "\n" + SharedPrefManager.getString(ParamConstants.LABEL_TID,context.getString(R.string.lbl_tid)) + ": " + itemModel.tid);
      //context.setTextAppearance(holder.txtErr, R.style.TextStyleSubSubHeaderAwesome);
      //holder.txtErr.setText(HtmlCompat.fromHtml(String.format(context.getString(R.string.txt_color_code),context.getString(R.color.err_red)), HtmlCompat.FROM_HTML_MODE_LEGACY));
      int colorId = getColorCode(itemModel.getEncVerifyStatus());
      if(colorId > 0) holder.txtEan.setTextColor(ContextCompat.getColor(context, colorId));
      holder.txtErr.setVisibility(itemModel.getEncVerifyStatus() == 1 ? View.VISIBLE : View.INVISIBLE);
      holder.txtErr.setTag(itemModel.getEncVerifyStatus());
      if(itemModel.getEncVerifyStatus() == 1) listTxtErr.add(holder.txtErr);
      else if(listTxtErr.contains(holder.txtErr)) listTxtErr.remove(holder.txtErr);
    }
    else if(isShowEpcTid){
      holder.txtEan.setText(SharedPrefManager.getString(ParamConstants.LABEL_EPC,context.getString(R.string.lbl_epc)) + ": " + itemModel.epc + "\n" + SharedPrefManager.getString(ParamConstants.LABEL_TID,context.getString(R.string.lbl_tid)) + ": " + itemModel.tid);
      showLog("inv_file_status",""+itemModel.getEncVerifyStatus());
      int colorId = getColorCode(itemModel.getEncVerifyStatus());
      if(colorId > 0) holder.txtEan.setTextColor(ContextCompat.getColor(context, colorId));
      //holder.txtEan.setTextColor(ContextCompat.getColor(context, (isAllowDecode && itemModel.isDecoded()) || (!isAllowDecode && itemModel.isFound) ? R.color.green : R.color.txt_regular));
    }
    else{
      holder.txtEan.setText(isShowPartialEpc ? "****" + (itemModel.epc.length() - 4 > 4 ? itemModel.epc.substring(4, itemModel.epc.length() - 4) : "") + "****" : itemModel.ean);
      holder.txtEan.setTextColor(ContextCompat.getColor(context, (isAllowDecode && itemModel.isDecoded()) || (!isAllowDecode && itemModel.isFound) ? R.color.green : R.color.txt_regular));
    }
    holder.imgDecode.setImageResource(isAllowDecode ? itemModel.isDecoded() ? R.drawable.ic_completed : itemModel.retryWriteCount > 0 ? R.drawable.ic_remove : R.drawable.ic_decode : R.drawable.ic_decode);
    holder.imgDecode.setVisibility(isAllowDecode && itemModel != null && !itemModel.isDecoded() && !itemModel.isUploaded && itemModel.retryWriteCount > 0 && frag != null && isAllowDecode ? View.VISIBLE : !isVerifyEncoding ? View.INVISIBLE : View.GONE);
    holder.imgDecode.setOnClickListener(v -> {
      if(v != null && v.getVisibility() == View.VISIBLE && isAllowDecode && !itemModel.isDecoded() && frag != null && !frag.isProcessOn()){
        if(!itemModel.isUploaded && itemModel.retryWriteCount > 0){
          if(frag instanceof OmniChannelStartFragment)
            ((OmniChannelStartFragment) frag).removeNonDecodedTag(itemModel);
          if(frag instanceof SearchFIFOStartFragment)
            ((SearchFIFOStartFragment) frag).removeNonDecodedTag(itemModel);
          if(frag instanceof SearchListStartFragment)
            ((SearchListStartFragment) frag).removeNonDecodedTag(itemModel);
        }
        else{
          if(frag instanceof OmniChannelStartFragment)
            ((OmniChannelStartFragment) frag).startDecode(itemModel);
          if(frag instanceof SearchFIFOStartFragment)
            ((SearchFIFOStartFragment) frag).startDecode(itemModel);
          if(frag instanceof SearchListStartFragment)
            ((SearchListStartFragment) frag).startDecode(itemModel);
        }
      }
    });
    
    holder.imgSearch.setOnClickListener(v -> {
      if(v != null && v.getVisibility() == View.VISIBLE && frag != null && !frag.isProcessOn()){// && (frag instanceof OmniChannelStartFragment || frag instanceof SearchUnencodedFragment || frag instanceof SearchAlienFragment || frag instanceof SearchFIFOStartFragment || frag instanceof SearchListStartFragment || frag instanceof StockCorrectionStartFragment)){
        try{
          frag.startEPCSearch(itemModel);
          /*if(frag instanceof OmniChannelStartFragment)
            ((OmniChannelStartFragment) frag).startEPCSearch(itemModel);
          else if(frag instanceof SearchUnencodedFragment)
            ((SearchUnencodedFragment) frag).startEPCSearch(itemModel);
          else if(frag instanceof SearchAlienFragment)
            ((SearchAlienFragment) frag).startEPCSearch(itemModel);
          else if(frag instanceof SearchFIFOStartFragment)
            ((SearchFIFOStartFragment) frag).startEPCSearch(itemModel);
          else if(frag instanceof SearchListStartFragment)
            ((SearchListStartFragment) frag).startEPCSearch(itemModel);
          else if(frag instanceof StockCorrectionStartFragment)
            ((StockCorrectionStartFragment) frag).startEPCSearch(itemModel);*/
        }
        catch(Exception e){ e.printStackTrace(); }
      }
    });
    
    holder.imgSearch.setVisibility(!frag.isProcessOn() ? View.VISIBLE : View.GONE);
  }
  
  private String getStatus(Integer encVerifyStatus){
    if(encVerifyStatus == null || encVerifyStatus <= 0) return "";
    AppCommonMethods.EncodeVerifyStatus encStatus = AppCommonMethods.EncodeVerifyStatus.get(encVerifyStatus);
    if(encStatus == null) return "";
    switch(encStatus){
      case EPC_WRONG:
        return "(Needs Verification!)";
      case PENDING:
        return "";
      case RE_ENCODED:
        return "(Re-Encoded)";
      case VERIFIED_DECODED:
        return "(Sold)";
      case VERIFIED_SUCCESS:
        return "(Verified)";
      default:
        return "";
    }
  }
  
  private int getColorCode(Integer encVerifyStatus){
    if(encVerifyStatus == null || encVerifyStatus <= 0) return R.color.txt_light;
    AppCommonMethods.EncodeVerifyStatus encStatus = AppCommonMethods.EncodeVerifyStatus.get(encVerifyStatus);
    if(encStatus == null) return 0;
    switch(encStatus){
      case EPC_WRONG:
        return R.color.red;
      case PENDING:
        return R.color.txt_light;
      case RE_ENCODED:
        return R.color.orange;
      case VERIFIED_DECODED:
        return R.color.light_green;
      case VERIFIED_SUCCESS:
        return R.color.green;
      default:
        return R.color.txt_light;
    }
  }
  
  /**
   * The View holder.
   */
  
  public static class ViewHolder extends RecyclerView.ViewHolder{
    
    ImageView imgDecode;
    ImageView imgSearch;
    TextView txtEan;
    TextView txtErr;
    OmniPickListBinding binding;
    
    /**
     * Instantiates a new View holder.
     *
     * @param binding the binding
     */
    ViewHolder(final OmniPickListBinding binding){
      super(binding.getRoot());
      txtEan = binding.txtEan;
      txtErr = binding.txtErr;
      imgDecode = binding.imgDecode;
      imgSearch = binding.imgSearch;
      imgDecode.setVisibility(View.INVISIBLE);
    }
  }
}
