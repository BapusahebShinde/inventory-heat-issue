package com.itek.retail.ui.actionmenu;

import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isSetUserMgmt;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itek.retail.adapter.NotifyTypeListAdapter;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.NotificationDao;
import com.itek.retail.databinding.FragmentActionMenuNotifyTypeListBinding;
import com.itek.retail.model.Notification;
import com.itek.retail.ui.home.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * The Action menu notify type list fragment.
 */
public class ActionMenuNotifyTypeListFragment extends CommonFragment{
  
  private ActionMenuNotifyTypeListViewModel mViewModel;
  private FragmentActionMenuNotifyTypeListBinding binding;
  private String type;
  private String typeId;
  private NotificationDao notificationDao;
  private List<Notification> listNotifications = new ArrayList<>(0);
  
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    notificationDao = AppDatabase.getNotificationDao(context);
    type = AppCommonMethods.extractString(getArguments(), AppConstants.NOTIFICATION_TYPE, "");
    typeId = AppCommonMethods.extractString(getArguments(), AppConstants.NOTIFICATION_TYPE_ID, "");
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ActionMenuNotifyTypeListViewModel.class);
    binding = FragmentActionMenuNotifyTypeListBinding.inflate(inflater, container, false);
    
    binding.listNotifications.setAdapter(new NotifyTypeListAdapter((MainActivity) context, ActionMenuNotifyTypeListFragment.this, listNotifications));
    binding.listNotifications.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
    
    return binding.getRoot();
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    notificationDao.getNotificationsByType(isSetUserMgmt ? SharedPrefManager.getUserID() : "", typeId).observe(getViewLifecycleOwner(), new Observer<List<Notification>>(){
      @Override
      public void onChanged(List<Notification> notifications){
        if(isTopInStack() && AppCommonMethods.chkNull(notificationDao.getUnreadNotificationCount(isSetUserMgmt ? SharedPrefManager.getUserID() : "", typeId), 0) > 0)
          notificationDao.updateReadNotifications(isSetUserMgmt ? SharedPrefManager.getUserID() : "", typeId);
        if(isNonEmpty(notifications)){
          listNotifications.clear();
          if(notifications != null) listNotifications.addAll(notifications);
          if(binding != null && binding.listNotifications != null && binding.listNotifications.getAdapter() != null && binding.listNotifications.getAdapter() instanceof RecyclerView.Adapter)
            ((RecyclerView.Adapter) binding.listNotifications.getAdapter()).notifyDataSetChanged();
        }
        //if(isTopInStack())
        
      }
    });
  }
  
  @Override
  public void onDestroyView(){
    notificationDao.getNotificationsByType(isSetUserMgmt ? SharedPrefManager.getUserID() : "", typeId).removeObservers(getViewLifecycleOwner());
    super.onDestroyView();
  }
}