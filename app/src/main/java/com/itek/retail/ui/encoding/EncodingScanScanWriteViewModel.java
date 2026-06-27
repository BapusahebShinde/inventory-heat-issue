package com.itek.retail.ui.encoding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Encoding start view model.
 */
public class EncodingScanScanWriteViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Encoding start view model.
   */
  public EncodingScanScanWriteViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Encoding Scan Scan Write Fragment");
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