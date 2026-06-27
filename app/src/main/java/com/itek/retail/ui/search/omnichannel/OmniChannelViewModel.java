package com.itek.retail.ui.search.omnichannel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Omni channel view model.
 */
public class OmniChannelViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Omni channel view model.
   */
  public OmniChannelViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Omni Search Fragment");
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