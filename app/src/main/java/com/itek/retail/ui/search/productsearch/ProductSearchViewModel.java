package com.itek.retail.ui.search.productsearch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Product search view model.
 */
public class ProductSearchViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Product search view model.
   */
  public ProductSearchViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Product Search Fragment");
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
