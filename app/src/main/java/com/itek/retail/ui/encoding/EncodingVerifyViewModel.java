package com.itek.retail.ui.encoding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Encoding verify view model.
 */
public class EncodingVerifyViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Encoding verify view model.
   */
  public EncodingVerifyViewModel(){
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