package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.common.AppConstants;
import com.lidroid.xutils.db.annotation.NotNull;

import java.io.Serializable;

/**
 * The HU status.
 */
@Entity(tableName = "hu_status", indices = {@Index(value = {"trip_no", "hu_no", "type"}, unique = true)})
public class HUStatus implements Serializable, Parcelable{
  
  public static final Creator<HUStatus> CREATOR = new Creator<HUStatus>(){
    @Override
    public HUStatus createFromParcel(Parcel in){
      return new HUStatus(in);
    }
    
    @Override
    public HUStatus[] newArray(int size){
      return new HUStatus[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "pk_id")
  public Integer pkId;
  @NotNull
  @ColumnInfo(name = "trip_no", defaultValue = "")
  @SerializedName("tripNum")
  public String tripNumber;
  @NotNull
  @ColumnInfo(name = "hu_no", defaultValue = "")
  @SerializedName(value = "huNum", alternate = {"HuNum", "HUNum", "HUNumber", "HuNumber", "huNumber"})
  public String huNumber;
  @NotNull
  @SerializedName(value = "type",alternate = {"OperationType","operationType","operationtype","operation_type","OpType","opType","optype","op_type","TYPE","Type"})
  @ColumnInfo(name = "type", defaultValue = "IN")
  public String type="";
  @SerializedName(value = "status",alternate = {"Status","HUStatus","HuStatus","huStatus","hustatus","hu_status"})
  @ColumnInfo(name = "status", defaultValue = "P")
  public String status = "P";
  @SerializedName("reason")
  @ColumnInfo(name = "reason", defaultValue = "")
  public String reason;
  @SerializedName(value = "expQty",alternate = {"packQty","eanQty","totalQty"})
  @ColumnInfo(name = "exp_qty", defaultValue = "0")
  public int expQty=0;
  @SerializedName("scanQty")
  @ColumnInfo(name = "scan_qty", defaultValue = "0")
  public int scanQty=0;
  @SerializedName(value = "isManualHU", alternate = {"manualHU", "manual_hu"})
  @ColumnInfo(name = "manual_hu", defaultValue = "false")
  public boolean isManualHU = false;
  @SerializedName(value = "isUploaded", alternate = {"uploaded"})
  @ColumnInfo(name = "is_uploaded", defaultValue = "false")
  public boolean isUploaded = false;
  
  /**
   * Instantiates a new HU status.
   */
  public HUStatus(){/*Empty constructor*/}
  
  /**
   * Instantiates a new HU status.
   *
   * @param in the in
   */
  protected HUStatus(Parcel in){
    tripNumber = in.readString();
    huNumber = in.readString();
    type = in.readString();
    status = in.readString();
    reason = in.readString();
    expQty = in.readInt();
    scanQty = in.readInt();
    isManualHU = in.readByte() != 0;
    isUploaded = in.readByte() != 0;
  }
  
  /**
   * Get trip number string.
   *
   * @return the string
   */
  public String getTripNumber(){
    return chkNull(tripNumber, "");
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
   * Get trip number string.
   *
   * @return the string
   */
  public String getHuNumber(){
    return chkNull(huNumber, "");
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
   * Get status string.
   *
   * @return the string
   */
  public String getStatus(){
    return chkNull(status, AppConstants.TRIP_STATUS_PENDING);
  }
  
  /**
   * Set processtype.
   *
   * @param status the status
   */
  public void setStatus(String status){
    this.status = status;
  }
  
  /**
   * Get type string.
   *
   * @return the string
   */
  public String getType(){
    return chkNull(type, "");
  }
  
  /**
   * Set type.
   *
   * @param type the type
   */
  public void setType(String type){
    this.type = type;
  }
  
  public String getReason(){
    return chkNull(reason, "");
  }
  
  public void setReason(String reason){
    this.reason = reason;
  }
  
  public int getExpQty(){
    return chkNull(expQty, 0);
  }
  
  public void setExpQty(int expQty){
    this.expQty = expQty;
  }
  
  public int getScanQty(){
    return chkNull(scanQty, 0);
  }
  
  public void setScanQty(int scanQty){
    this.scanQty = scanQty;
  }
  
  public boolean isManualHU(){
    return isManualHU;
  }
  
  public void setManualHU(boolean manualHU){
    isManualHU = manualHU;
  }
  
  public boolean isUploaded(){
    return isUploaded;
  }
  
  public void setUploaded(boolean uploaded){
    isUploaded = uploaded;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    HUStatus that = (HUStatus) o;
    return isNonEmpty(tripNumber) && tripNumber.equals(that.tripNumber) && isNonEmpty(huNumber) && huNumber.equals(that.huNumber);
  }
  
  @Override
  public int describeContents(){ return 0; }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeString(tripNumber);
    dest.writeString(huNumber);
    dest.writeString(type);
    dest.writeString(status);
    dest.writeString(reason);
    dest.writeInt(chkNull(expQty, 0));
    dest.writeInt(chkNull(scanQty, 0));
    dest.writeByte((byte) (isManualHU ? 1 : 0));
    dest.writeByte((byte) (isUploaded ? 1 : 0));
  }
}
