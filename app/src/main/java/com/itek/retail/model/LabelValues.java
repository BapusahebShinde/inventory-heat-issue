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
public class LabelValues implements Serializable, Parcelable{
  
  public static final Creator<LabelValues> CREATOR = new Creator<LabelValues>(){
    @Override
    public LabelValues createFromParcel(Parcel in){
      return new LabelValues(in);
    }
    
    @Override
    public LabelValues[] newArray(int size){
      return new LabelValues[size];
    }
  };
  @SerializedName("label")
  private String label;
  @SerializedName("value")
  private String value;
  
  /**
   * Instantiates a new Label values.
   */
  public LabelValues(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Label values.
   *
   * @param label the label
   * @param value the value
   */
  @Ignore
  public LabelValues(String label, String value){
    this.label = label;
    this.value = value;
  }
  
  /**
   * Instantiates a new Label values.
   *
   * @param in the in
   */
  protected LabelValues(Parcel in){
    label = in.readString();
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
  public String getLabel(){ return chkNull(label, ""); }
  
  /**
   * Set label.
   *
   * @param label the label
   */
  public void setLabel(String label){ this.label = chkNull(label, ""); }
  
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
    parcel.writeString(label);
    parcel.writeString(value);
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    LabelValues that = (LabelValues) o;
    return Objects.equals(label, that.label);
  }
}
