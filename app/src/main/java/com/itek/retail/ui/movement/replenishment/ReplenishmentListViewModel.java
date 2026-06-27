package com.itek.retail.ui.movement.replenishment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Replenishment list view model.
 */
public class ReplenishmentListViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Replenishment list view model.
   */
  public ReplenishmentListViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Replenishment Main Fragment");
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
