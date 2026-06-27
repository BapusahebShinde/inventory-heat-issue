package com.itek.retail.ui.navmenu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The App info view model.
 */
public class AppInfoViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new App info view model.
   */
  public AppInfoViewModel(){
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