package com.itek.retail.reader;

/**
 * The Progress dialog model.
 */
public class ProgressDialogModel{
  
  String message;
  boolean showDialog = false;
  
  /**
   * Get message string.
   *
   * @return the string
   */
  public String getMessage(){
    return message;
  }
  
  /**
   * Set message.
   *
   * @param message the message
   */
  public void setMessage(String message){
    this.message = message;
  }
  
  /**
   * Is show dialog boolean.
   *
   * @return the boolean
   */
  public boolean isShowDialog(){
    return showDialog;
  }
  
  /**
   * Set show dialog.
   *
   * @param showDialog the show dialog
   */
  public void setShowDialog(boolean showDialog){
    this.showDialog = showDialog;
  }
}
