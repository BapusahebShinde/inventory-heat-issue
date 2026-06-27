package com.itek.retail.ui.outward.huverification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Outward grn trip details view model.
 */
public class OutwardHuDetailsViewModel extends ViewModel{
  
  // TODO: Implement the ViewModel
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Outward grn trip details view model.
   */
  public OutwardHuDetailsViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Outward Grn Trip Details Fragment");
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