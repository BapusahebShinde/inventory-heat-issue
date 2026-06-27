package com.itek.retail.ui.inward;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Inward main view model.
 */
public class InwardMainViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Inward main view model.
   */
  public InwardMainViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Inward Main Fragment");
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
