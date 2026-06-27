package com.itek.retail.ui.than;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Encoding config view model.
 */
public class ThanViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Than view model.
   */
  public ThanViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Than");
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