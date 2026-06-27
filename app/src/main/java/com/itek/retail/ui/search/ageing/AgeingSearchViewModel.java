package com.itek.retail.ui.search.ageing;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AgeingSearchViewModel extends ViewModel{
  
  // TODO: Implement the ViewModel
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Product search view model.
   */
  public AgeingSearchViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Ageing Search Fragment");
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