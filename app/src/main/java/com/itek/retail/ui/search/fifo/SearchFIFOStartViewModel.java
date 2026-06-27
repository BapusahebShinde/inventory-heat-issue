package com.itek.retail.ui.search.fifo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Product search details view model.
 */
public class SearchFIFOStartViewModel extends ViewModel{
  // TODO: Implement the ViewModel
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Product search details view model.
   */
  public SearchFIFOStartViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is FIFO Search Fragment");
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