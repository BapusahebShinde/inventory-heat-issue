package com.itek.retail.ui.encoding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Encoding start view model.
 */
public class EncodingStartViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Encoding start view model.
   */
  public EncodingStartViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Encoding Config Fragment");
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