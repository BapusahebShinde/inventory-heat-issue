package com.itek.retail.ui.inventory.stockcorrection;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Stock correction view model.
 */
public class StockCorrectionViewModel extends ViewModel{
  
  // TODO: Implement the ViewModel
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Stock correction view model.
   */
  public StockCorrectionViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Inventory Correction Fragment");
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