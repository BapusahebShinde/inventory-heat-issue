package com.itek.retail.ui.search.listnewsearch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Replenishment list view model.
 */
public class SearchListExcelStartViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Search list  start view model.
   */
  public SearchListExcelStartViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Search List New Start Fragment");
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
