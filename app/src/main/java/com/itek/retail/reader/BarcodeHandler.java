package com.itek.retail.reader;

import static com.itek.retail.common.AppCommonMethods.showLog;

import android.os.CountDownTimer;

import androidx.lifecycle.MutableLiveData;

import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.CommonActivity;

public abstract class BarcodeHandler{
  
  protected final CountDownTimer scanTimer = new CountDownTimer(3000, 3000){
    @Override
    public void onTick(long l){ }
    
    @Override
    public void onFinish(){
      onTimerFinish();
    }
  };
  public MutableLiveData<Boolean> isBarcodeOn = new MutableLiveData<>(false);
  public MutableLiveData<String> barcodeData = new MutableLiveData<>("");
  protected AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.OTHER;
  protected String TAG = BarcodeHandler.class.getSimpleName();
  protected CommonActivity context;
  protected MainReaderRepository mainReaderRepository;
  protected String scanType = "";
  protected Boolean isInitiated = false;
  
  /**
   * Instantiates a new Chainway barcode handler.
   *
   * @param context              the context
   * @param mainReaderRepository the main reader repository
   * @param sessionType          the session type
   * @param isInit               the is init
   */
  public BarcodeHandler(CommonActivity context, MainReaderRepository mainReaderRepository, AppCommonMethods.SessionType sessionType, boolean isInit){
    this.context = context;
    this.mainReaderRepository = mainReaderRepository;
    this.sessionType = sessionType;
    this.TAG = this.getClass().getSimpleName();
    try{
      if(isInit) init();
    }
    catch(Exception e){
      e.printStackTrace();
      showLog(e.getClass().getSimpleName(), "" + e.getMessage());
    }
  }
  
  protected abstract void onTimerFinish();
  
  public abstract void init();
  
  public abstract void startScan(String scanType);
  
  public abstract void stopScan();
  
  protected String getTypeCharCode(){
    if(sessionType != null && sessionType.getValue() > 0)
      return sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION ? "C" : sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD ? sessionType.name().substring(1, 2).toUpperCase() : sessionType.name().substring(0, 1);
    else return "";
  }
  
  /**
   * Set progress message.
   *
   * @param isShowDialog the is show dialog
   */
  public void setProgressMessage(boolean isShowDialog){
    setProgressMessage("", isShowDialog);
  }
  
  /**
   * Set progress message.
   *
   * @param message      the message
   * @param isShowDialog the is show dialog
   */
  protected void setProgressMessage(String message, boolean isShowDialog){
    if(mainReaderRepository != null) mainReaderRepository.setProgressMessage(message, isShowDialog);
  }
  
  /**
   * On resume.
   */
  public abstract void onResume();
  
  /**
   * On pause.
   */
  public abstract void onPause();
  
  /**
   * On destroy.
   */
  public abstract void onDestroy();
}
