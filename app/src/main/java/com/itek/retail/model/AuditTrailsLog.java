package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.apis.ParamConstants;
import com.lidroid.xutils.db.annotation.NotNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * The AuditTrails log.
 */
@Entity(tableName = "audit_trail_log")
public class AuditTrailsLog implements Serializable, Parcelable{
  
  public static final Creator<AuditTrailsLog> CREATOR = new Creator<AuditTrailsLog>(){
    @Override
    public AuditTrailsLog createFromParcel(Parcel in){
      return new AuditTrailsLog(in);
    }
    
    @Override
    public AuditTrailsLog[] newArray(int size){
      return new AuditTrailsLog[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "log_no")
  public Integer logNo;
  @SerializedName("dateTime")
  @NotNull
  @ColumnInfo(name = "date_time")
  public String transactionDateTime;
  @SerializedName("type")
  @NotNull
  @ColumnInfo(name = "type_class")
  public String type;
  @NotNull
  @ColumnInfo(name = "action_state")
  public String action;
  @ColumnInfo(name = "info_result")
  public String result;
  
  /**
   * Instantiates a new AuditTrails log.
   */
  public AuditTrailsLog(){/*Empty constructor*/}
  
  /**
   * Instantiates a new AuditTrails log.
   *
   * @param type   the type
   * @param action the type
   */
  @Ignore
  public AuditTrailsLog(String action, String type){
    this.action = action;
    this.type = type;
  }
  
  /**
   * Instantiates a new AuditTrails log.
   *
   * @param actionStatus the action status
   * @param typeClass    the type class
   * @param infoResult   the info result
   */
  @Ignore
  public AuditTrailsLog(String actionStatus, String typeClass, String infoResult){
    this.action = actionStatus;
    this.type = typeClass;
    this.result = infoResult;
  }
  
  /**
   * Instantiates a new Search log.
   *
   * @param in the in
   */
  protected AuditTrailsLog(Parcel in){
    transactionDateTime = in.readString();
    type = in.readString();
    action = in.readString();
    result = in.readString();
  }
  
  /**
   * Get transaction date time string.
   *
   * @return the string
   */
  public String getTransactionDateTime(){ return chkNull(transactionDateTime, ""); }
  
  /**
   * Set transaction date time.
   *
   * @param transactionDateTime the transaction date time
   */
  public void setTransactionDateTime(String transactionDateTime){
    this.transactionDateTime = transactionDateTime;
  }
  
  /**
   * Get type id string.
   *
   * @return the string
   */
  public String getType(){ return chkNull(type, ""); }
  
  /**
   * Set type id.
   *
   * @param type the type id
   */
  public void setType(String type){ this.type = type; }
  
  @Override
  public int describeContents(){ return 0; }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    parcel.writeString(transactionDateTime);
    parcel.writeString(type);
    parcel.writeString(action);
    parcel.writeString(result);
  }
  
  /**
   * To json json object.
   *
   * @return the json object
   */
  public JSONObject toJson(){
    JSONObject dataObject = new JSONObject();
    try{
      dataObject.put(ParamConstants.LOG_ID, this.logNo);
      dataObject.put(ParamConstants.TRANSACTION_DATE, this.transactionDateTime);
      dataObject.put(ParamConstants.TYPE_ID, this.type);
      dataObject.put(ParamConstants.ACTION, this.action);
      dataObject.put(ParamConstants.DATA, this.result);
    }
    catch(JSONException e){ e.printStackTrace(); }
    return dataObject;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    AuditTrailsLog that = (AuditTrailsLog) o;
    return type.equals(that.type) && action.equals(that.action);
  }
}
