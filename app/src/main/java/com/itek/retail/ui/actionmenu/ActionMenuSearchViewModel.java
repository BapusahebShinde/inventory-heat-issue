package com.itek.retail.ui.actionmenu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Action menu search view model.
 */
public class ActionMenuSearchViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Action menu search view model.
   */
  public ActionMenuSearchViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is App Info Fragment");
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