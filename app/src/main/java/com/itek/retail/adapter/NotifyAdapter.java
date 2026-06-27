package com.itek.retail.adapter;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.common.AppConstants;
import com.itek.retail.databinding.ListNotificationBinding;
import com.itek.retail.model.Notification;
import com.itek.retail.ui.actionmenu.ActionMenuNotifyTypeListFragment;
import com.itek.retail.ui.home.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class NotifyAdapter extends RecyclerView.Adapter<NotifyAdapter.MyViewHolder>{
  
  private MainActivity context;
  private List<Notification> notifications = new ArrayList<>(0);
  
  public NotifyAdapter(@NonNull MainActivity context, List<Notification> listNotifications, String strSearch){
    this.context = context;
    if(isNonEmpty(listNotifications)){
      if(isNonEmpty(strSearch)){
        for(Notification notice : listNotifications)
          if(notice.getTitle().matches("(?i)^.*" + strSearch + ".*$") || notice.getMessage().matches("(?i)^.*" + strSearch + ".*$"))
            notifications.add(notice);
      }
      else this.notifications.addAll(listNotifications);
    }
  }
  
  @NonNull
  @Override
  public NotifyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    return new NotifyAdapter.MyViewHolder(ListNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }
  
  @Override
  public void onBindViewHolder(NotifyAdapter.MyViewHolder holder, final int position){
    final Notification itemModel = notifications.get(position);
    if(itemModel == null) return;
    holder.txtTitle.setText(itemModel.getTitle());
    holder.txtmessage.setText(itemModel.getMessage());
    holder.txtDate.setText(itemModel.getDate());
    holder.txtNoticount.setText("" + itemModel.getQty());
    try{
      final int menuIconId = context.getResources().getIdentifier(itemModel.getMenuIconName(), AppConstants.RES_DRAWABLE, context.getPackageName());
      if(menuIconId > 0){ itemModel.setItemImgId(menuIconId); }
      context.loadImage(holder.img, chkNull(itemModel.getItemImgURL(), ""), chkZero(itemModel.getItemImgId(), menuIconId));
    }
    catch(Exception e){ e.printStackTrace(); }
    
    holder.itemView.setOnClickListener(view -> {
      Bundle args = new Bundle();
      args.putString(AppConstants.NOTIFICATION_TYPE, itemModel.getType());
      args.putString(AppConstants.NOTIFICATION_TYPE_ID, itemModel.getTypeId());
      args.putString(AppConstants.TITLE, itemModel.getTitle());
      args.putString(AppConstants.TITLE_LOGO_URL, itemModel.getItemImgURL());
      args.putInt(AppConstants.TITLE_LOGO_RES_ID, itemModel.getItemImgId());
      context.loadFragment(new ActionMenuNotifyTypeListFragment(), args);
    });
  }
  
  @Override
  public int getItemCount(){
    return notifications.size();
  }
  
  static class MyViewHolder extends RecyclerView.ViewHolder{
    
    TextView txtTitle;
    TextView txtmessage;
    TextView txtNoticount;
    TextView txtDate;
    ImageView img;
    
    MyViewHolder(ListNotificationBinding binding){
      super(binding.getRoot());
      img = binding.imgNotificationLogo;
      txtTitle = binding.txtNotificationTitle;
      txtmessage = binding.txtNotificationStatus;
      txtNoticount = binding.txtNotificationCount;
      txtDate = binding.txtNotificationDatetime;
      img.setSelected(true);
      txtNoticount.setSelected(true);
      txtmessage.setSelected(true);
      txtTitle.setSelected(true);
      txtDate.setSelected(true);
    }
  }
}