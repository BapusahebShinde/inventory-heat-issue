package com.itek.retail.ui.inward1;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Inward main view model.
 */
public class InOutWardPhase1ViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Inward main view model.
   */
  public InOutWardPhase1ViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is New Inward/Outward Phase 1");
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
