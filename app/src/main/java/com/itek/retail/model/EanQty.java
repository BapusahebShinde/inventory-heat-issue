package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Ignore;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Ean qty.
 */
public class EanQty implements Serializable, Parcelable{
  
  public static final Creator<EanQty> CREATOR = new Creator<EanQty>(){
    @Override
    public EanQty createFromParcel(Parcel in){
      return new EanQty(in);
    }
    
    @Override
    public EanQty[] newArray(int size){
      return new EanQty[size];
    }
  };
  @SerializedName("ean")
  public String ean;
  @SerializedName("eanQty")
  public Integer eanQty = 0;
  
  /**
   * Instantiates a new Ean qty.
   */
  public EanQty(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Ean qty.
   *
   * @param ean    the ean
   * @param eanQty the ean qty
   */
  @Ignore
  public EanQty(String ean, Integer eanQty){
    this.ean = ean;
    this.eanQty = eanQty;
  }
  
  /**
   * Instantiates a new Ean qty.
   *
   * @param in the in
   */
  protected EanQty(Parcel in){
    ean = in.readString();
    if(in.readByte() == 0){ eanQty = null; }
    else{ eanQty = in.readInt(); }
  }
  
  @Override
  public int describeContents(){
    return 0;
  }
  
  /**
   * Get ean string.
   *
   * @return the string
   */
  public String getEan(){ return AppCommonMethods.getLeftZeroReplacedString(chkNull(ean, "").trim()); }
  
  /**
   * Set ean.
   *
   * @param ean the ean
   */
  public void setEan(String ean){ this.ean = chkNull(ean, ""); }
  
  /**
   * Get ean qty integer.
   *
   * @return the integer
   */
  public Integer getEanQty(){ return chkNull(eanQty, 0); }
  
  /**
   * Set ean qty.
   *
   * @param eanQty the ean qty
   */
  public void setEanQty(Integer eanQty){ this.eanQty = chkNull(eanQty, 0); }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    parcel.writeString(ean);
    if(eanQty == null){ parcel.writeByte((byte) 0); }
    else{
      parcel.writeByte((byte) 1);
      parcel.writeInt(eanQty);
    }
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    return this.ean.equals(((EanQty) o).ean);
  }
  
  public JSONObject toJson(){
    JSONObject jsonObject = null;
    try{
      jsonObject = new JSONObject();
      jsonObject.put(ParamConstants.EAN, ean);
      jsonObject.put(ParamConstants.EAN_QTY, eanQty);
    }
    catch(Exception e){ e.printStackTrace(); }
    return jsonObject;
  }
  
  @Override
  public int hashCode(){
    return Objects.hash(ean);
  }
  
  @Override
  public String toString(){
    return getEan() + " (" + getEanQty() + ")";
  }
}
