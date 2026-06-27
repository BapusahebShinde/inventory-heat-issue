package com.itek.retail.ui.inward.grn;

import java.io.Serializable;

/**
 * The Store trip details data.
 */
public class StoreTripDetailsData implements Serializable{
  
  String tripNumber, deliveryNumber, huNumber, scanRfids, huStatus, message;
  int huTotalQty, eanNo, eanQty, scanQty;
  
  /**
   * Get delivery number string.
   *
   * @return the string
   */
  public String getDeliveryNumber(){
    return deliveryNumber;
  }
  
  /**
   * Set delivery number.
   *
   * @param deliveryNumber the delivery number
   */
  public void setDeliveryNumber(String deliveryNumber){
    this.deliveryNumber = deliveryNumber;
  }
  
  /**
   * Get trip number string.
   *
   * @return the string
   */
  public String getTripNumber(){
    return tripNumber;
  }
  
  /**
   * Set trip number.
   *
   * @param tripNumber the trip number
   */
  public void setTripNumber(String tripNumber){
    this.tripNumber = tripNumber;
  }
  
  /**
   * Get scan rfids string.
   *
   * @return the string
   */
  public String getScanRfids(){
    return scanRfids;
  }
  
  /**
   * Set scan rfids.
   *
   * @param scanRfids the scan rfids
   */
  public void setScanRfids(String scanRfids){
    this.scanRfids = scanRfids;
  }
  
  /**
   * Get hu status string.
   *
   * @return the string
   */
  public String getHuStatus(){
    return huStatus;
  }
  
  /**
   * Set hu status.
   *
   * @param huStatus the hu status
   */
  public void setHuStatus(String huStatus){
    this.huStatus = huStatus;
  }
  
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
   * Get hu total qty int.
   *
   * @return the int
   */
  public int getHuTotalQty(){
    return huTotalQty;
  }
  
  /**
   * Set hu total qty.
   *
   * @param huTotalQty the hu total qty
   */
  public void setHuTotalQty(int huTotalQty){
    this.huTotalQty = huTotalQty;
  }
  
  /**
   * Get ean no int.
   *
   * @return the int
   */
  public int getEanNo(){
    return eanNo;
  }
  
  /**
   * Set ean no.
   *
   * @param eanNo the ean no
   */
  public void setEanNo(int eanNo){
    this.eanNo = eanNo;
  }
  
  /**
   * Get scan qty int.
   *
   * @return the int
   */
  public int getScanQty(){
    return scanQty;
  }
  
  /**
   * Set scan qty.
   *
   * @param scanQty the scan qty
   */
  public void setScanQty(int scanQty){
    this.scanQty = scanQty;
  }
  
  /**
   * Get hu number string.
   *
   * @return the string
   */
  public String getHuNumber(){
    return huNumber;
  }
  
  /**
   * Set hu number.
   *
   * @param huNumber the hu number
   */
  public void setHuNumber(String huNumber){
    this.huNumber = huNumber;
  }
  
  /**
   * Get ean qty int.
   *
   * @return the int
   */
  public int getEanQty(){
    return eanQty;
  }
  
  /**
   * Set ean qty.
   *
   * @param eanQty the ean qty
   */
  public void setEanQty(int eanQty){
    this.eanQty = eanQty;
  }
  
}
