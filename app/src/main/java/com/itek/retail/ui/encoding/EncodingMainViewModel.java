package com.itek.retail.ui.encoding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Encoding main view model.
 */
public class EncodingMainViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Encoding main view model.
   */
  public EncodingMainViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Encoding Main Fragment");
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