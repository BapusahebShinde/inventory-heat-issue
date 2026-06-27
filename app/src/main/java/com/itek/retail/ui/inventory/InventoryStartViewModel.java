package com.itek.retail.ui.inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Inventory start view model.
 */
public class InventoryStartViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Inventory start view model.
   */
  public InventoryStartViewModel(){
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