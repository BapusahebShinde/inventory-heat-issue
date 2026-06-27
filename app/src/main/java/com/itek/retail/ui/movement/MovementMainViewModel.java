package com.itek.retail.ui.movement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Movement main view model.
 */
public class MovementMainViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Movement main view model.
   */
  public MovementMainViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Movement Main Fragment");
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
