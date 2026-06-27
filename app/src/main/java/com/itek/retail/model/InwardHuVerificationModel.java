package com.itek.retail.model;

/**
 * The Inward hu verification model.
 */
public class InwardHuVerificationModel{
  
  String huNumbers, status;
  
  /**
   * Instantiates a new Inward hu verification model.
   *
   * @param huNumbers the hu numbers
   * @param status    the status
   */
  public InwardHuVerificationModel(String huNumbers, String status){
    this.huNumbers = huNumbers;
    this.status = status;
  }
  
  /**
   * Get hu numbers string.
   *
   * @return the string
   */
  public String getHuNumbers(){
    return huNumbers;
  }
  
  /**
   * Set hu numbers.
   *
   * @param huNumbers the hu numbers
   */
  public void setHuNumbers(String huNumbers){
    this.huNumbers = huNumbers;
  }
  
  /**
   * Get status string.
   *
   * @return the string
   */
  public String getStatus(){
    return status;
  }
  
  /**
   * Set status.
   *
   * @param status the status
   */
  public void setStatus(String status){
    this.status = status;
  }
}
