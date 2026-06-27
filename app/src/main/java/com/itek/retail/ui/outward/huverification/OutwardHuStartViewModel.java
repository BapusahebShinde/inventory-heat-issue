package com.itek.retail.ui.outward.huverification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Outward grn start view model.
 */
public class OutwardHuStartViewModel extends ViewModel{
  
  // TODO: Implement the ViewModel
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Outward grn start view model.
   */
  public OutwardHuStartViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Outward Grn Start Main Fragment");
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