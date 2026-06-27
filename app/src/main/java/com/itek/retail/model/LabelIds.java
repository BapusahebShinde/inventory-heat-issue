package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Ignore;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * The Dashboard stats label counts.
 */
public class LabelIds implements Serializable, Parcelable{
  
  public static final Creator<LabelIds> CREATOR = new Creator<LabelIds>(){
    @Override
    public LabelIds createFromParcel(Parcel in){
      return new LabelIds(in);
    }
    
    @Override
    public LabelIds[] newArray(int size){
      return new LabelIds[size];
    }
  };
  @SerializedName("label")
  private String label;
  @SerializedName(value = "id", alternate = {"attributeId"})
  private Long id;
  
  /**
   * Instantiates a new Dashboard stats label counts.
   */
  public LabelIds(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Dashboard stats label counts.
   *
   * @param label  the label
   * @param id the id
   */
  @Ignore
  public LabelIds(Long id,String label){
    this.id = id;
    this.label = label;
  }
  
  /**
   * Instantiates a new Dashboard stats label counts.
   *
   * @param in the in
   */
  protected LabelIds(Parcel in){
    if(in.readByte() == 0){ id = null; }
    else{ id = in.readLong(); }
    label = in.readString();
  }
  
  @Override
  public int describeContents(){
    return 0;
  }
  
  /**
   * Get label string.
   *
   * @return the string
   */
  public String getLabel(){ return chkNull(label, ""); }
  
  /**
   * Set label.
   *
   * @param label the label
   */
  public void setLabel(String label){ this.label = chkNull(label, ""); }
  
  /**
   * Get id long.
   *
   * @return the long
   */
  public Long getId(){ return chkNull(id, 0l); }
  
  /**
   * Set id.
   *
   * @param id the id
   */
  public void setId(Long id){ this.id = chkNull(id, 0l); }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    if(id == null){ parcel.writeByte((byte) 0); }
    else{
      parcel.writeByte((byte) 1);
      parcel.writeLong(id);
    }
    parcel.writeString(label);
  }
  
  @Override
  public String toString(){
    return getLabel();
  }
}
