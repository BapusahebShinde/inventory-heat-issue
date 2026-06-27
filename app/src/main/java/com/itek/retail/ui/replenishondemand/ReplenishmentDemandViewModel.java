package com.itek.retail.ui.replenishondemand;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Replenishment start view model.
 */
public class ReplenishmentDemandViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;

  public String selSrcZone = null;
  public String selDestZone = null;
  public String selMatkl = null;
  public String selCategory = null;
  
  /**
   * Instantiates a new Replenishment start view model.
   */
  public ReplenishmentDemandViewModel(){
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
