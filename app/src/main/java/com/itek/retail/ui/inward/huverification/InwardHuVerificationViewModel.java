package com.itek.retail.ui.inward.huverification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Inward hu verification view model.
 */
public class InwardHuVerificationViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Inward hu verification view model.
   */
  public InwardHuVerificationViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Inward HU Verification  Fragment");
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
