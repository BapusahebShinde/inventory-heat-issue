package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppConstants;
import com.lidroid.xutils.db.annotation.NotNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * The Zone.
 */
@Entity(tableName = "zones", indices = {@Index(value = {"zone_id"}, unique = true)})
public class Zone implements Serializable, Parcelable{
  
  public static final Creator<Zone> CREATOR = new Creator<Zone>(){
    @Override
    public Zone createFromParcel(Parcel in){
      return new Zone(in);
    }
    
    @Override
    public Zone[] newArray(int size){
      return new Zone[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "zone_no")
  public Integer zoneNo;
  @SerializedName("zoneId")
  @ColumnInfo(name = "zone_id")
  @NotNull
  public String zoneId;
  @SerializedName(value = "zoneName",alternate = {"zone"})
  @ColumnInfo(name = "zone_name")
  @NotNull
  public String zoneName;
  @SerializedName("zoneType")
  @ColumnInfo(name = "zone_type")
  public String zoneType;
  @SerializedName("isDefault")
  @ColumnInfo(name = "is_default", defaultValue = "false")
  public Boolean isDefault;
  @SerializedName(value ="isDisplayZone" ,alternate = {"hasMapping","has_mapping","isMapping","is_mapping","isMappingEnabled","is_mapping_enabled","isEnabledMapping","is_enabled_mapping","enabledMapping","enabled_mapping","hasDisplayMapping","has_display_mapping","isDisplayMapping","is_display_mapping","isDisplayMappingEnabled","is_display_mapping_enabled","isEnabledDisplayMapping","is_enabled_display_mapping","enabledDisplayMapping","enabled_display_mapping","displayMapping","display_mapping","isDisplay","is_display","is_display_zone"})
  @ColumnInfo(name = "is_display_mapping", defaultValue = "false")
  public Boolean isDisplayZone = false;
  
  /**
   * Instantiates a new Zone.
   */
  public Zone(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Zone.
   *
   * @param zoneId   the zone id
   * @param zoneName the zone name
   */
  @Ignore
  public Zone(String zoneId, String zoneName){
    this.zoneId = zoneId;
    this.zoneName = zoneName;
    this.zoneType = zoneName;
    this.isDefault = false;
  }
  
  /**
   * Instantiates a new Zone.
   *
   * @param in the in
   */
  @Ignore
  protected Zone(Parcel in){
    zoneId = in.readString();
    zoneName = in.readString();
    zoneType = in.readString();
    byte tmpIsDefault = in.readByte();
    isDefault = tmpIsDefault == 0 ? null : tmpIsDefault == 1;
    byte tmpisDisplayMapping = in.readByte();
    isDisplayZone = tmpisDisplayMapping == 0 ? null : tmpisDisplayMapping == 1;
  }
  
  /**
   * Get zone id string.
   *
   * @return the string
   */
  public String getZoneId(){ return chkNull(zoneId, "0"); }
  
  /**
   * Set zone id.
   *
   * @param zoneId the zone id
   */
  public void setZoneId(String zoneId){ this.zoneId = zoneId; }
  
  /**
   * Get zone name string.
   *
   * @return the string
   */
  public String getZoneName(){ return chkNull(zoneName, ""); }
  
  /**
   * Set zone name.
   *
   * @param zoneName the zone name
   */
  public void setZoneName(String zoneName){ this.zoneName = zoneName; }
  
  /**
   * Get zone type string.
   *
   * @return the string
   */
  public String getZoneType(){ return chkNull(zoneType, ""); }
  
  /**
   * Set zone type.
   *
   * @param zoneType the zone type
   */
  public void setZoneType(String zoneType){ this.zoneType = zoneType; }
  
  /**
   * Get default boolean.
   *
   * @return the boolean
   */
  public Boolean getDefault(){ return chkNull(isDefault, false); }
  
  /**
   * Set default.
   *
   * @param aDefault the a default
   */
  public void setDefault(Boolean aDefault){ isDefault = aDefault; }
  
  public Boolean getIsDisplayZone(){
    return isDisplayZone;
  }
  
  public void setIsDisplayZone(Boolean isDisplayZone){
    this.isDisplayZone = isDisplayZone;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    Zone that = (Zone) o;
    return zoneId.equalsIgnoreCase(that.zoneId);
  }
  
  @Override
  public String toString(){ return getZoneName(); }
  
  /**
   * To json json object.
   *
   * @return the json object
   */
  public JSONObject toJson(){
    JSONObject dataObject = new JSONObject();
    try{
      dataObject.put(ParamConstants.ZONE_ID, getZoneId());
      dataObject.put(ParamConstants.ZONE, chkNull(getZoneName(), AppConstants.ALL));
      dataObject.put(ParamConstants.ZONE_NAME, chkNull(getZoneName(), AppConstants.ALL));
      dataObject.put(ParamConstants.ZONE_TYPE, getZoneType());
      dataObject.put(ParamConstants.IS_DEFAULT_ZONE, getDefault());
      if(isDisplayZone !=null)
        dataObject.put(ParamConstants.IS_DISPLAY_ZONE, getIsDisplayZone());
    }
    catch(JSONException e){ e.printStackTrace(); }
    return dataObject;
  }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeString(zoneId);
    dest.writeString(zoneName);
    dest.writeString(zoneType);
    dest.writeByte((byte) (isDefault == null ? 0 : isDefault ? 1 : 2));
    dest.writeByte((byte) (isDisplayZone == null ? 0 : isDisplayZone ? 1 : 0));
  }
  
  @Override
  public int describeContents(){ return 0; }
}
