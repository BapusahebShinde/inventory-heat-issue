package com.itek.retail.ui.actionmenu;

import static com.itek.retail.common.AppCommonMethods.isSetUserMgmt;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itek.retail.adapter.NotifyAdapter;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.FragmentActionMenuNotifyBinding;
import com.itek.retail.model.Notification;
import com.itek.retail.ui.home.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * The Action menu notify fragment.
 */
public class ActionMenuNotifyFragment extends CommonFragment{
  
  private ActionMenuNotifyViewModel mViewModel;
  private FragmentActionMenuNotifyBinding binding;
  private List<Notification> listNotifications = new ArrayList<>(0);
  
  /**
   * Instantiates a new Action menu notify fragment.
   */
  public ActionMenuNotifyFragment(){
    // Required empty public constructor
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
    mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new ViewModelProvider.NewInstanceFactory()).get(ActionMenuNotifyViewModel.class);
    binding = FragmentActionMenuNotifyBinding.inflate(inflater, container, false);
    
    binding.edtMovNotificationSearch.addTextChangedListener(new TextWatcher(){
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){
        /*Empty Method (Default Overridden)*/
      }
      
      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
        /*Empty Method (Default Overridden)*/
      }
      
      @Override
      public void afterTextChanged(Editable editable){
        updateLists();
        
      }
    });
    
    binding.edtMovNotificationSearch.setOnEditorActionListener(new TextView.OnEditorActionListener(){
      @Override
      public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent){
        if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE){
          hideKeyboard();
          return true;
        }
        return false;
      }
    });
    
    updateLists();
    
    return binding.getRoot();
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
    super.onViewCreated(view, savedInstanceState);
    AppDatabase.getNotificationDao(context).getNotificationTypes(isSetUserMgmt ? SharedPrefManager.getUserID() : "").observe(getViewLifecycleOwner(), new Observer<List<Notification>>(){
      @Override
      public void onChanged(List<Notification> notifications){
        listNotifications.clear();
        if(notifications != null) listNotifications.addAll(notifications);
        updateLists();
      }
    });
  }
  
  @Override
  public void updateLists(){
    super.updateLists();
    if(binding != null && binding.listReplenishmentNotification != null && binding.edtMovNotificationSearch != null){
      binding.listReplenishmentNotification.setAdapter(new NotifyAdapter((MainActivity) context, listNotifications, binding.edtMovNotificationSearch.getText().toString()));
      binding.listReplenishmentNotification.setLayoutManager(isLandscape ? new GridLayoutManager(context, 2) : new LinearLayoutManager(context));
    }
  }
  
  @Override
  public void onDestroyView(){
    AppDatabase.getNotificationDao(context).getNotificationTypes(isSetUserMgmt ? SharedPrefManager.getUserID() : "").removeObservers(getViewLifecycleOwner());
    super.onDestroyView();
  }
}