package com.itek.retail.ui.search.listnewsearch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Replenishment list view model.
 */
public class SearchListExcelViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Replenishment list view model.
   */
  public SearchListExcelViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Search List New Fragment");
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
