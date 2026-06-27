package com.itek.retail.ui.movement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Movement start view model.
 */
public class MovementStartViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Movement start view model.
   */
  public MovementStartViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Movement Start Main Fragment");
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
