package com.itek.retail.ui.landing;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.MutableLiveData;

import com.itek.retail.BR;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.CommonFragment;
import com.itek.retail.model.Pin;

import org.json.JSONObject;

import java.util.regex.Pattern;

/**
 * The Pin view model.
 */
public class PinViewModel extends BaseObservable{
  
  private Pin pin;
  String [] arrayPin={"0770","2489"};
  private MutableLiveData<String> pinValidationValue = new MutableLiveData<>();
  private LandingRepository landingRepository;
  private Context context;
  
  /**
   * Instantiates a new Pin view model.
   *
   * @param context the context
   */
  
  public PinViewModel(Context context){
    // instantiating object of model class
    pin = new Pin("");
    this.context = context;
    landingRepository = new LandingRepository(this.context);
  }
  
  /**
   * Get action mutable live data.
   *
   * @return the mutable live data
   */
  public MutableLiveData<String> getAction(){
    return pinValidationValue;
  }
  
  /**
   * Get pin string.
   *
   * @return the string
   */
  
  @Bindable
  public String getPin(){
    return pin.getPin();
  }
  
  /**
   * Set pin.
   *
   * @param strPin the str pin
   */
  public void setPin(String strPin){
    pin.setPin(strPin);
    notifyPropertyChanged(BR.pin);
  }
  
  /**
   * Is valid boolean.
   *
   * @return the boolean
   */
  
  public boolean isValid(){
    return !TextUtils.isEmpty(getPin()) && getPin().length() >= 4 && Pattern.compile("[0-9]{4,6}").matcher(getPin()).matches();
  }
  
  public boolean isValid(final String pin){
    return !TextUtils.isEmpty(pin) && pin.length() >= 4 && Pattern.compile("[0-9]{4,6}").matcher(pin).matches();
  }
  
  public boolean isValidPin(){
    return isValid() && validateEnteredPin(getPin());
  }
  public boolean isValidPin(final String pin){
    return isValid(pin) && validateEnteredPin(pin);
  }
  
  private boolean validateEnteredPin(String pin){
    boolean contains = false;
    for (String s : arrayPin) {
      if (s.equals(pin)) {
        contains = true;
        break;
      }
    }
    return contains;
  }
  
  /**
   * Call web service.
   *
   * @param commonActivity   the common activity
   * @param commonFragment   the common fragment
   * @param url              the url
   * @param jsonRequest      the json request
   * @param args             the args
   * @param isRetry          the is retry
   * @param progressMsg      the progress msg
   * @param isOfflineProcess the is offline process
   */
  public void callWebService(final CommonActivity commonActivity, final CommonFragment commonFragment, final String url, final JSONObject jsonRequest, final Bundle args, final boolean isRetry, final String progressMsg, final boolean isOfflineProcess, final boolean isDBProcess){
    AppCommonMethods.callWebService(commonActivity, commonFragment, url, jsonRequest, args, isRetry, progressMsg, isOfflineProcess, isDBProcess);
  }
}
