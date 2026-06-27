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
public class EpcTid implements Serializable, Parcelable{
  
  public static final Creator<EpcTid> CREATOR = new Creator<EpcTid>(){
    @Override
    public EpcTid createFromParcel(Parcel in){
      return new EpcTid(in);
    }
    
    @Override
    public EpcTid[] newArray(int size){
      return new EpcTid[size];
    }
  };
  @SerializedName("tid")
  public String tid;
  @SerializedName("epc")
  public String epc;
  
  /**
   * Instantiates a new Ean qty.
   */
  public EpcTid(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Ean qty.
   *
   * @param tid the tid
   * @param epc the epc
   */
  @Ignore
  public EpcTid(String tid, String epc){
    this.tid = tid;
    this.epc = epc;
  }
  
  /**
   * Instantiates a new Ean qty.
   *
   * @param in the in
   */
  protected EpcTid(Parcel in){
    tid = in.readString();
    epc = in.readString();
  }
  
  @Override
  public int describeContents(){
    return 0;
  }
  
  /**
   * Get tid string.
   *
   * @return the string
   */
  public String getTid(){ return chkNull(tid, ""); }
  
  /**
   * Set tid.
   *
   * @param tid the tid
   */
  public void setTid(String tid){ this.tid = chkNull(tid, ""); }
  
  /**
   * Get epc string.
   *
   * @return the string
   */
  public String getEpc(){ return chkNull(epc, ""); }
  
  /**
   * Set epc
   *
   * @param epc the epc
   */
  public void setEpc(String epc){ this.epc = chkNull(epc, ""); }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    parcel.writeString(tid);
    parcel.writeString(epc);
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    EpcTid epcTid = (EpcTid) o;
    return Objects.equals(tid, epcTid.tid) && Objects.equals(epc, epcTid.epc);
  }
  
  @Override
  public int hashCode(){
    return Objects.hash(tid, epc);
  }
  
  @Override
  public String toString(){
    return "EpcTid{" + "tid='" + tid + '\'' + ", epc='" + epc + '\'' + '}';
  }
}
