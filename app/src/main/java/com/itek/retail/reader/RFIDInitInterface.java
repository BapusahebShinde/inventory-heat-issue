package com.itek.retail.reader;

/**
 * The interface Rfid init interface.
 */
public interface RFIDInitInterface{
  
  /**
   * Rfid initialization status object.
   *
   * @param status  the status
   * @param message the message
   * @param reader  the reader
   * @return the object
   */
  public Object RFIDInitializationStatus(boolean status, String message, Object reader);
}
