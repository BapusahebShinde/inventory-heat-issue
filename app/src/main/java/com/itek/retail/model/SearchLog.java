package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * The Search log.
 */
@Entity(tableName = "search_log")
public class SearchLog implements Serializable, Parcelable{
  
  public static final Creator<SearchLog> CREATOR = new Creator<SearchLog>(){
    @Override
    public SearchLog createFromParcel(Parcel in){
      return new SearchLog(in);
    }
    
    @Override
    public SearchLog[] newArray(int size){
      return new SearchLog[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  public Integer logNo;
  @SerializedName("transactionId")
  @ColumnInfo(name = "transaction_id")
  public String transactionId;
  @SerializedName("dateTime")
  @ColumnInfo(name = "dateTime")
  public String transactionDateTime;
  @ColumnInfo(name = "session_id")
  public String sessionId;
  @ColumnInfo(name = "session_user_id")
  public String sessionUserId;
  @SerializedName("ean")
  @ColumnInfo(name = "ean")
  public String ean;
  @ColumnInfo(name = "ean_qty", defaultValue = "0")
  @SerializedName("eanQty")
  public Integer eanQty;
  @SerializedName("extras")
  @ColumnInfo(name = "extras")
  public String extras;
  @SerializedName("type")
  @ColumnInfo(name = "type")
  public String type;
  @SerializedName("subType")
  @ColumnInfo(name = "subType")
  public String subType;
  @SerializedName("typeId")
  @ColumnInfo(name = "typeId")
  public String typeId;
  @ColumnInfo(name = "durationTime")
  public String searchDurationTime;
  
  /**
   * Instantiates a new Search log.
   */
  public SearchLog(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Search log.
   *
   * @param sessionId the session id
   * @param ean       the ean
   * @param eanQty    the ean qty
   * @param type      the type
   */
  @Ignore
  public SearchLog(String sessionId, String ean, Integer eanQty, String type){
    this.sessionId = sessionId;
    this.sessionUserId = SharedPrefManager.getUserID();
    this.ean = ean;
    this.eanQty = chkNull(eanQty, 0);
    this.type = type;
  }
  
  /**
   * Instantiates a new Search log.
   *
   * @param sessionId the session id
   * @param ean       the ean
   * @param eanQty    the ean qty
   * @param type      the type
   * @param subType   the sub type
   */
  @Ignore
  public SearchLog(String sessionId, String ean, Integer eanQty, String type, String subType, String extras){
    this.sessionId = sessionId;
    this.sessionUserId = SharedPrefManager.getUserID();
    this.ean = ean;
    this.eanQty = chkNull(eanQty, 0);
    this.type = type;
    this.subType = subType;
    this.extras = extras;
  }
  
  /**
   * Instantiates a new Search log.
   *
   * @param sessionId the session id
   * @param ean       the ean
   * @param eanQty    the ean qty
   * @param type      the type
   * @param subType   the sub type
   * @param typeId    the type id
   */
  @Ignore
  public SearchLog(String sessionId, String ean, Integer eanQty, String type, String subType, String typeId, String extras){
    this.sessionId = sessionId;
    this.sessionUserId = SharedPrefManager.getUserID();
    this.ean = ean;
    this.eanQty = chkNull(eanQty, 0);
    this.type = type;
    this.subType = subType;
    this.typeId = typeId;
    this.extras = extras;
  }
  
  /**
   * Instantiates a new Search log.
   *
   * @param in the in
   */
  protected SearchLog(Parcel in){
    transactionId = in.readString();
    transactionDateTime = in.readString();
    sessionId = in.readString();
    sessionUserId = in.readString();
    ean = in.readString();
    eanQty = in.readInt();
    extras = in.readString();
    type = in.readString();
    subType = in.readString();
    typeId = in.readString();
    searchDurationTime = in.readString();
  }
  
  /**
   * Get transaction id string.
   *
   * @return the string
   */
  public String getTransactionId(){
    return chkNull(transactionId, "");
  }
  
  /**
   * Set transaction id.
   *
   * @param transactionId the transaction id
   */
  public void setTransactionId(String transactionId){
    this.transactionId = transactionId;
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
   * Get ean string.
   *
   * @return the string
   */
  public String getEan(){
    return AppCommonMethods.getLeftZeroReplacedString(chkNull(ean, "").trim());
  }
  
  /**
   * Set ean.
   *
   * @param ean the ean
   */
  public void setEan(String ean){
    this.ean = ean;
  }
  
  /**
   * Get ean qty integer.
   *
   * @return the integer
   */
  public Integer getEanQty(){
    return chkNull(eanQty, 0);
  }
  
  /**
   * Set ean qty.
   *
   * @param eanQty the ean qty
   */
  public void setEanQty(Integer eanQty){
    this.eanQty = eanQty;
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
  
  /**
   * Get sub type string.
   *
   * @return the string
   */
  public String getSubType(){ return chkNull(subType, ""); }
  
  /**
   * Set sub type.
   *
   * @param subType the sub type
   */
  public void setSubType(String subType){ this.subType = subType; }
  
  /**
   * Get type id string.
   *
   * @return the string
   */
  public String getTypeId(){ return chkNull(typeId, ""); }
  
  /**
   * Set type id.
   *
   * @param typeId the type id
   */
  public void setTypeId(String typeId){ this.typeId = typeId; }
  
  /**
   * Get search duration time string.
   *
   * @return the string
   */
  public String getSearchDurationTime(){ return chkNull(searchDurationTime, ""); }
  
  /**
   * Set search duration time.
   *
   * @param searchDurationTime the type id
   */
  public void setSearchDurationTime(String searchDurationTime){ this.searchDurationTime = searchDurationTime; }
  
  @Override
  public int describeContents(){ return 0; }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    parcel.writeString(transactionId);
    parcel.writeString(transactionDateTime);
    parcel.writeString(sessionId);
    parcel.writeString(sessionUserId);
    parcel.writeString(ean);
    parcel.writeInt(chkNull(eanQty, 0));
    parcel.writeString(extras);
    parcel.writeString(type);
    parcel.writeString(subType);
    parcel.writeString(typeId);
    parcel.writeString(searchDurationTime);
  }
  
  /**
   * To json json object.
   *
   * @return the json object
   */
  public JSONObject toJson(){
    JSONObject dataObject = null;
    if(isNonEmpty(extras)){
      try{
        dataObject = new JSONObject(extras);
      }
      catch(Exception e){
        dataObject = new JSONObject();
      }
    }
    if(dataObject == null) dataObject = new JSONObject();
    try{
      dataObject.put(ParamConstants.LOG_ID, this.logNo);
      dataObject.put(ParamConstants.TRANSACTION_ID, this.transactionId);
      dataObject.put(ParamConstants.TRANSACTION_DATE, this.transactionDateTime);
      dataObject.put(ParamConstants.SESSION_ID, this.sessionId);
      dataObject.put(ParamConstants.SESSION_USER_ID, this.sessionUserId);
      dataObject.put(ParamConstants.EAN, this.ean);
      dataObject.put(ParamConstants.EAN_QTY, this.eanQty);
      if(!dataObject.has(ParamConstants.SESSION_TYPE))
        dataObject.put(ParamConstants.SESSION_TYPE, this.type);
      if(!dataObject.has(ParamConstants.TYPE)) dataObject.put(ParamConstants.TYPE, this.type);
      if(!dataObject.has(ParamConstants.SUB_TYPE))
        dataObject.put(ParamConstants.SUB_TYPE, this.subType);
      if(!dataObject.has(ParamConstants.TYPE_ID))
        dataObject.put(ParamConstants.TYPE_ID, this.typeId);
      dataObject.put(ParamConstants.SEARCH_DURATION, this.searchDurationTime);
    }
    catch(JSONException e){ e.printStackTrace(); }
    return dataObject;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    SearchLog searchLog = (SearchLog) o;
    return isNonEmpty(ean) && ean.equals(searchLog.ean) && isNonEmpty(type) && type.equals(searchLog.type) && isNonEmpty(transactionId) && transactionId.equals(searchLog.transactionId);
  }
}
