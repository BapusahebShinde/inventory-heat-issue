package com.itek.retail.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.common.SharedPrefManager;
import com.lidroid.xutils.db.annotation.NotNull;

import java.io.Serializable;

/**
 * The Rfid session.
 */

@Entity(tableName = "rfid_session", indices = {@Index(value = {"session_id"}, unique = true)})

public class RFIDSession implements Serializable, Parcelable{
  
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "session_no")//auto increment ino
  public Integer sessionNo;
  @ColumnInfo(name = "session_id")
  @NotNull
  public String sessionId;
  @SerializedName("userId")
  @NotNull
  @ColumnInfo(name = "user_id")
  public String userId;
  @ColumnInfo(name = "session_type", defaultValue = "0")
  @NotNull
  public int sessionType;
  @ColumnInfo(name = "session_action", defaultValue = "0")
  @NotNull
  public int sessionAction;
  @ColumnInfo(name = "session_start_time", defaultValue = "")
  @NotNull
  public String sessionStartTime;
  @ColumnInfo(name = "session_stop_time")
  public String sessionStopTime;
  @ColumnInfo(name = "session_valid_till")
  public String sessionValidTill;
  @ColumnInfo(name = "zone")
  public String zone;
  @ColumnInfo(name = "zone_id", defaultValue = "0")
  public String zoneId;
  @ColumnInfo(name = "dest_zone")
  public String destZone;
  @ColumnInfo(name = "dest_zone_id")
  public String destZoneId;
  @ColumnInfo(name = "brands")
  public String brands;
  @ColumnInfo(name = "brand_ean")
  public String brandEan;
  @ColumnInfo(name = "category")
  public String category;
  @ColumnInfo(name = "eans", defaultValue = "")
  @NotNull
  public String eans;
  @ColumnInfo(name = "total", defaultValue = "0")
  public String total;
  @ColumnInfo(name = "is_active", defaultValue = "false")
  public boolean isActive;
  @ColumnInfo(name = "is_running", defaultValue = "false")
  public boolean isRunning;
  @ColumnInfo(name = "is_uploading", defaultValue = "false")
  public boolean isUploading;
  @ColumnInfo(name = "extras", defaultValue = "")
  public String extras;
  
  /**
   * Instantiates a new Rfid session.
   */
  public RFIDSession(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Rfid session.
   *
   * @param sessionId   the session id
   * @param sessionType the session type
   */
  @Ignore
  public RFIDSession(String sessionId, int sessionType){
    this.sessionId = sessionId;
    this.sessionType = sessionType;
    this.userId = SharedPrefManager.getUserID();
  }
  
  /**
   * Instantiates a new Rfid session.
   *
   * @param sessionId     the session id
   * @param sessionType   the session type
   * @param sessionAction the session action
   */
  @Ignore
  public RFIDSession(String sessionId, int sessionType, int sessionAction){
    this.sessionId = sessionId;
    this.sessionType = sessionType;
    this.sessionAction = sessionAction;
    this.userId = SharedPrefManager.getUserID();
  }
  
  protected RFIDSession(Parcel in){
    if(in.readByte() == 0){ sessionNo = null; }
    else{ sessionNo = in.readInt(); }
    sessionId = in.readString();
    userId = in.readString();
    sessionType = in.readInt();
    sessionAction = in.readInt();
    sessionStartTime = in.readString();
    sessionStopTime = in.readString();
    sessionValidTill = in.readString();
    zone = in.readString();
    zoneId = in.readString();
    destZone = in.readString();
    destZoneId = in.readString();
    brands = in.readString();
    brandEan = in.readString();
    category = in.readString();
    eans = in.readString();
    total = in.readString();
    isActive = in.readByte() != 0;
    isRunning = in.readByte() != 0;
    isUploading = in.readByte() != 0;
    extras = in.readString();
  }
  
  public static final Creator<RFIDSession> CREATOR = new Creator<RFIDSession>(){
    @Override
    public RFIDSession createFromParcel(Parcel in){
      return new RFIDSession(in);
    }
    
    @Override
    public RFIDSession[] newArray(int size){
      return new RFIDSession[size];
    }
  };
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    RFIDSession that = (RFIDSession) o;
    return sessionId.equals(that.sessionId);
  }
  
  @Override
  public int describeContents(){
    return 0;
  }
  
  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags){
  
    if(sessionNo == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(sessionNo);
    }
    dest.writeString(sessionId);
    dest.writeString(userId);
    dest.writeInt(sessionType);
    dest.writeInt(sessionAction);
    dest.writeString(sessionStartTime);
    dest.writeString(sessionStopTime);
    dest.writeString(sessionValidTill);
    dest.writeString(zone);
    dest.writeString(zoneId);
    dest.writeString(destZone);
    dest.writeString(destZoneId);
    dest.writeString(brands);
    dest.writeString(brandEan);
    dest.writeString(category);
    dest.writeString(eans);
    dest.writeString(total);
    dest.writeByte((byte) (isActive ? 1 : 0));
    dest.writeByte((byte) (isRunning ? 1 : 0));
    dest.writeByte((byte) (isUploading ? 1 : 0));
    dest.writeString(extras);
  }
}