package com.itek.retail.ui.outward;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Outward main view model.
 */
public class OutwardPickDataViewModel extends ViewModel{
  
  // TODO: Implement the ViewModel
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Outward main view model.
   */
  public OutwardPickDataViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Outward Pick Data Fragment");
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