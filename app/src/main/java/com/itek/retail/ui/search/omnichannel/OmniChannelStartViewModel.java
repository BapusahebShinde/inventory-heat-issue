package com.itek.retail.ui.search.omnichannel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Omni channel start view model.
 */
public class OmniChannelStartViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Omni channel start view model.
   */
  public OmniChannelStartViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is OmniChannel start Fragment");
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
