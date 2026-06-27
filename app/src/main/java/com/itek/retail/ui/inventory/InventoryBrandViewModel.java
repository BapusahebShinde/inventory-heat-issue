package com.itek.retail.ui.inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Inv brand view model.
 */
public class InventoryBrandViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Inv brand view model.
   */
  public InventoryBrandViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Inventory Start Fragment");
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