package com.itek.retail.ui.inward.grn;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Inward grn trip details view model.
 */
public class InwardGrnTripDetailsViewModel extends ViewModel{
  
  // TODO: Implement the ViewModel
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Inward grn trip details view model.
   */
  public InwardGrnTripDetailsViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Inward Grn Trip Details Fragment");
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