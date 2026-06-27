package com.itek.retail.ui.inward.grn;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Inward grn hu scan view model.
 */
public class InwardGrnHuScanViewModel extends ViewModel{
  
  // TODO: Implement the ViewModel
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Inward grn hu scan view model.
   */
  public InwardGrnHuScanViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Inward Grn Start Main Fragment");
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