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
public class LabelCounts implements Serializable, Parcelable{
  
  public static final Creator<LabelCounts> CREATOR = new Creator<LabelCounts>(){
    @Override
    public LabelCounts createFromParcel(Parcel in){
      return new LabelCounts(in);
    }
    
    @Override
    public LabelCounts[] newArray(int size){
      return new LabelCounts[size];
    }
  };
  @SerializedName("label")
  private String label;
  @SerializedName("count")
  private Integer counts;
  
  /**
   * Instantiates a new Dashboard stats label counts.
   */
  public LabelCounts(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Dashboard stats label counts.
   *
   * @param label  the label
   * @param counts the counts
   */
  @Ignore
  public LabelCounts(String label, Integer counts){
    this.label = label;
    this.counts = counts;
  }
  
  /**
   * Instantiates a new Dashboard stats label counts.
   *
   * @param in the in
   */
  protected LabelCounts(Parcel in){
    label = in.readString();
    if(in.readByte() == 0){ counts = null; }
    else{ counts = in.readInt(); }
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
   * Get counts str string.
   *
   * @return the string
   */
  public String getCountsStr(){ return String.valueOf(getCounts()); }
  
  /**
   * Get counts integer.
   *
   * @return the integer
   */
  public Integer getCounts(){ return chkNull(counts, 0); }
  
  /**
   * Set counts.
   *
   * @param counts the counts
   */
  public void setCounts(Integer counts){ this.counts = chkNull(counts, 0); }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    parcel.writeString(label);
    if(counts == null){ parcel.writeByte((byte) 0); }
    else{
      parcel.writeByte((byte) 1);
      parcel.writeInt(counts);
    }
  }
}
