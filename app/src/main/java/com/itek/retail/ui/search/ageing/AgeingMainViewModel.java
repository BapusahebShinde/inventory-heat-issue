package com.itek.retail.ui.search.ageing;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AgeingMainViewModel extends ViewModel{
  
  // TODO: Implement the ViewModel
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Product search view model.
   */
  public AgeingMainViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Ageing Main Fragment");
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