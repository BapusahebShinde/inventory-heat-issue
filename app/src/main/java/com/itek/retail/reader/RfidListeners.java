package com.itek.retail.reader;

/**
 * The interface Rfid listeners.
 */
public interface RfidListeners{
  
  /**
   * On success.
   *
   * @param object the object
   */
  void onSuccess(Object object);
  
  /**
   * On failure.
   *
   * @param exception the exception
   */
  void onFailure(Exception exception);
  
  /**
   * On failure.
   *
   * @param message the message
   */
  void onFailure(String message);
  
}
