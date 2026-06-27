package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.ListProcessNotificationBinding;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.Notification;
import com.itek.retail.ui.home.MainActivity;

import java.util.List;

public class NotifyTypeListAdapter extends RecyclerView.Adapter<NotifyTypeListAdapter.MyViewHolder>{
  
  private MainActivity context;
  private CommonFragment frag;
  private List<Notification> moveList;
  
  public NotifyTypeListAdapter(@NonNull MainActivity context, @NonNull CommonFragment frag, List<Notification> moviesList){
    this.context = context;
    this.frag = frag;
    this.moveList = moviesList;
  }
  
  @NonNull
  @Override
  public NotifyTypeListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    return new NotifyTypeListAdapter.MyViewHolder(ListProcessNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public void onBindViewHolder(NotifyTypeListAdapter.MyViewHolder holder, int position){
    final Notification itemModel = moveList.get(position);
    if(itemModel == null) return;
    holder.txtMessage.setText(itemModel.getMessage());
    holder.txtDate.setText(itemModel.getDate());
    MenuModel menuModel = AppDatabase.getMenuDao(context).getMenuByCode(itemModel.getType().toUpperCase());
    menuModel = menuModel == null ? AppDatabase.getMenuDao(context).getMenuByCode(itemModel.getTypeId()) : null;
    final String menuCode = menuModel != null ? menuModel.getMenuCode() : "";
    final boolean isAllowRedirect = menuModel != null && menuModel.isEnabled && isNonEmpty(menuCode) && holder.getAdapterPosition() == 0;
    final MenuModel menu = menuModel;
    holder.txtGoToList.setTextColor(context.getColorAccentFromTheme());
    holder.txtGoToList.setVisibility(isAllowRedirect ? View.VISIBLE : View.GONE);
    holder.txtGoToList.setOnClickListener(view -> {
      if(isAllowRedirect){
        Bundle args = new Bundle();
        if(menuCode.toLowerCase().contains(AppConstants.MENU_CODE_REPLENISH.toLowerCase()))
          args.putString(AppConstants.REPLENISHMENT_TYPE, !chkNull(SharedPrefManager.getReplenishmentType(), AppConstants.REPLENISH_TYPE_BOTH).equalsIgnoreCase(AppConstants.REPLENISH_TYPE_BOTH) ? SharedPrefManager.getReplenishmentType() : itemModel.getType().toLowerCase().contains(AppConstants.REPLENISH_TYPE_STATIC) ? AppConstants.REPLENISH_TYPE_STATIC : AppConstants.REPLENISH_TYPE_DYNAMIC);
        if(menuCode.toLowerCase().contains(AppConstants.MENU_CODE_SER_OMNI.toLowerCase()))
          args.putString(AppConstants.OMNICHANNEL_TYPE, !chkNull(SharedPrefManager.getOmnichannelType(), AppConstants.OMNI_TYPE_BOTH).equalsIgnoreCase(AppConstants.OMNI_TYPE_BOTH) ? SharedPrefManager.getReplenishmentType() : itemModel.getType().toLowerCase().contains(AppConstants.OMNI_TYPE_EAN) ? AppConstants.OMNI_TYPE_EAN : AppConstants.OMNI_TYPE_ORDER);
        context.doublePopBackStack();
        frag.handleFragmentRedirection(menu, args);
      }
    });
  }
  
  @Override
  public int getItemCount(){
    return moveList.size();
  }
  
  static class MyViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtGoToList;
    TextView txtMessage;
    TextView txtDate;
    
    MyViewHolder(ListProcessNotificationBinding binding){
      super(binding.getRoot());
      
      txtGoToList = binding.txtGoToList;
      txtMessage = binding.txtMessage;
      txtDate = binding.txtNotificationDatetime;
      
      txtMessage.setSelected(true);
      txtGoToList.setSelected(true);
      txtDate.setSelected(true);
      
    }
  }
}