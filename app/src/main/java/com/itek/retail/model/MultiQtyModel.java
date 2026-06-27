package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * The Shortage model.
 */
public class MultiQtyModel implements Serializable, Parcelable{
  
  public static final Creator<MultiQtyModel> CREATOR = new Creator<MultiQtyModel>(){
    @Override
    public MultiQtyModel createFromParcel(Parcel in){
      return new MultiQtyModel(in);
    }
    
    @Override
    public MultiQtyModel[] newArray(int size){
      return new MultiQtyModel[size];
    }
  };
  @SerializedName("title")
  public String title;
  /*@SerializedName("inventory")
  public String inventory;*/
  @SerializedName("total")
  public Integer total;
  @SerializedName("found")
  public Integer found;
  @SerializedName("decoded")
  public Integer decoded;
  @SerializedName("required")
  public Integer required;
  
  /**
   * Instantiates a new Shortage model.
   */
  public MultiQtyModel(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Shortage model.
   *
   * @param in the in
   */
  protected MultiQtyModel(Parcel in){
    title = in.readString();
    
    total = in.readInt();
    found = in.readInt();
    decoded = in.readInt();
    required = in.readInt();
  }
  
  /**
   * Get brand string.
   *
   * @return the string
   */
  public String getTitle(){ return chkNull(title, ""); }
  
  /**
   * Set size.
   *
   * @param brand the brand
   */
  public void setSize(String brand){ this.title = brand; }
  
  /**
   * Get shortage integer.
   *
   * @return the integer
   */
  public Integer getTotal(){ return chkNull(total, 0); }
  
  /**
   * Set shortage.
   *
   * @param total the shortage
   */
  public void setTotal(Integer total){ this.total = total; }
  
  /**
   * Get found integer.
   *
   * @return the integer
   */
  public Integer getFound(){ return chkNull(found, 0); }
  
  /**
   * Set found.
   *
   * @param found the found
   */
  public void setFound(Integer found){ this.found = found; }
  
  /**
   * Get decoded integer.
   *
   * @return the integer
   */
  public Integer getDecoded(){ return chkNull(decoded, 0); }
  
  /**
   * Set decoded.
   *
   * @param decoded the decoded
   */
  public void setDecoded(Integer decoded){ this.decoded = decoded; }
  
  /**
   * Get decoded integer.
   *
   * @return the integer
   */
  public Integer getRequired(){ return chkNull(required, 0); }
  
  /**
   * Set decoded.
   *
   * @param required the order qty
   */
  public void setRequired(Integer required){ this.required = required; }
  
  @Override
  public int describeContents(){ return 0; }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeString(title);
    dest.writeInt(chkNull(total, 0));
    dest.writeInt(chkNull(found, 0));
    dest.writeInt(chkNull(decoded, 0));
    dest.writeInt(chkNull(required, 0));
  }
}
