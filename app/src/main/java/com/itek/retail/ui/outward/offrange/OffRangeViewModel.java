package com.itek.retail.ui.outward.offrange;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Outward list view model.
 */
public class OffRangeViewModel extends ViewModel{
  // TODO: Implement the ViewModel
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Outward list view model.
   */
  public OffRangeViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Outward Pick List Details Fragment");
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