package com.itek.retail.ui.outward;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Outward start view model.
 */
public class OutwardPickStartViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Outward start view model.
   */
  public OutwardPickStartViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Outward Pick Start Fragment");
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
