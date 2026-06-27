package com.itek.retail.ui.resetpassword;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The Reset password view model.
 */
public class ResetPasswordViewModel extends ViewModel{
  
  private MutableLiveData<String> mText;
  
  /**
   * Instantiates a new Reset password view model.
   */
  public ResetPasswordViewModel(){
    mText = new MutableLiveData<>();
    mText.setValue("This is Reset Password Main Fragment");
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
