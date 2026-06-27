package com.itek.retail.ui.actionmenu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Action menu notify type list view model.
 */
public class ActionMenuNotifyTypeListViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Action menu notify type list view model.
   */
  public ActionMenuNotifyTypeListViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Notification list Fragment");
  }
  
  /**
   * Get text live data.
   *
   * @return the live data
   */
  public LiveData<String> getText(){
    return mText;
  }
}