package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * The Product size model.
 */
public class ProductSizeModel implements Serializable, Parcelable{
  
  public static final Creator<ProductSizeModel> CREATOR = new Creator<ProductSizeModel>(){
    @Override
    public ProductSizeModel createFromParcel(Parcel in){
      return new ProductSizeModel(in);
    }
    
    @Override
    public ProductSizeModel[] newArray(int size){
      return new ProductSizeModel[size];
    }
  };
  @SerializedName("size")
  String size;
  @SerializedName("qty")
  String qty;
  @SerializedName("loc")
  String loc;
  
  /**
   * Instantiates a new Product size model.
   */
  public ProductSizeModel(){/*Empty Constructor*/}
  
  /**
   * Instantiates a new Product size model.
   *
   * @param size the size
   * @param qty  the qty
   * @param loc  the loc
   */
  public ProductSizeModel(String size, String qty, String loc){
    this.size = size;
    this.qty = qty;
    this.loc = loc;
  }
  
  /**
   * Instantiates a new Product size model.
   *
   * @param in the in
   */
  protected ProductSizeModel(Parcel in){
    size = in.readString();
    qty = in.readString();
    loc = in.readString();
  }
  
  /**
   * Get size string.
   *
   * @return the string
   */
  public String getSize(){ return chkNull(size, "M"); }
  
  /**
   * Set size.
   *
   * @param size the size
   */
  public void setSize(String size){ this.size = size; }
  
  /**
   * Get qty string.
   *
   * @return the string
   */
  public String getQty(){ return chkNull(qty, "0"); }
  
  /**
   * Set qty.
   *
   * @param qty the qty
   */
  public void setQty(String qty){ this.qty = qty; }
  
  /**
   * Get loc string.
   *
   * @return the string
   */
  public String getLoc(){ return chkNull(loc, "FOH"); }
  
  /**
   * Set loc.
   *
   * @param loc the loc
   */
  public void setLoc(String loc){ this.loc = loc; }
  
  @Override
  public int describeContents(){ return 0; }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeString(size);
    dest.writeString(qty);
    dest.writeString(loc);
  }
}
