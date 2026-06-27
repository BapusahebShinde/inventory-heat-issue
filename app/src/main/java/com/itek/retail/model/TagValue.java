package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Ignore;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Label values.
 */
public class TagValue implements Serializable, Parcelable{
  
  public static final Creator<TagValue> CREATOR = new Creator<TagValue>(){
    @Override
    public TagValue createFromParcel(Parcel in){
      return new TagValue(in);
    }
    
    @Override
    public TagValue[] newArray(int size){
      return new TagValue[size];
    }
  };
  @SerializedName("tag")
  private String tag;
  @SerializedName("value")
  private String value;
  
  /**
   * Instantiates a new Label values.
   */
  public TagValue(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Label values.
   *
   * @param tag   the label
   * @param value the value
   */
  @Ignore
  public TagValue(String tag, String value){
    this.tag = tag;
    this.value = value;
  }
  
  /**
   * Instantiates a new Label values.
   *
   * @param in the in
   */
  protected TagValue(Parcel in){
    tag = in.readString();
    value = in.readString();
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
  public String getTag(){ return chkNull(tag, ""); }
  
  /**
   * Set label.
   *
   * @param tag the label
   */
  public void setTag(String tag){ this.tag = chkNull(tag, ""); }
  
  /**
   * Get value string.
   *
   * @return the string
   */
  public String getValue(){ return chkNull(value, ""); }
  
  /**
   * Set value.
   *
   * @param value the value
   */
  public void setValue(String value){ this.value = chkNull(value, ""); }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    parcel.writeString(tag);
    parcel.writeString(value);
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    TagValue that = (TagValue) o;
    return Objects.equals(tag, that.tag);
  }
}
