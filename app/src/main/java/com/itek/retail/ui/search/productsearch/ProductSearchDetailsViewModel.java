package com.itek.retail.ui.search.productsearch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Product search details view model.
 */
public class ProductSearchDetailsViewModel extends ViewModel{
  // TODO: Implement the ViewModel
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Product search details view model.
   */
  public ProductSearchDetailsViewModel(){
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