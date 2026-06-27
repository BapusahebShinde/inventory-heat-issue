package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.lidroid.xutils.db.annotation.NotNull;
import com.lidroid.xutils.db.annotation.Unique;

import java.io.Serializable;

/**
 * The Site Type.
 */
@Entity(tableName = "site_codes", indices = {@Index(value = {"site_type_id","site_code"}, unique = true)})
public class SiteCode implements Serializable, Parcelable{
  
  public static final Creator<SiteCode> CREATOR = new Creator<SiteCode>(){
    @Override
    public SiteCode createFromParcel(Parcel in){
      return new SiteCode(in);
    }
    
    @Override
    public SiteCode[] newArray(int size){
      return new SiteCode[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "site_code_no")
  public Integer siteCodeNo;
  @SerializedName(value = "siteTypeId",alternate = {"SiteID","SiteId","Siteid","siteID","siteId","siteid","site_id","SiteTypeID","SiteTypeId","SiteTypeid","siteTypeID","sitetypeid","site_type_id","LocID","LocId","Locid","locID","locId","locid","loc_id","LocTypeID","LocTypeId","LocTypeid","locTypeID","locTypeId","loctypeid","loc_type_id","SupplyChainID","SupplyChainId","SupplyChainid","supplyChainID","supplyChainId","supplychainid","supply_chain_id","SupplyChainTypeid","supplyChainTypeID","supplyChainTypeId","supplychaintypeid","supply_chain_type_id"})
  @ColumnInfo(name = "site_type_id", defaultValue = "0")
  @NotNull
  public Long siteTypeId;
  @SerializedName(value = "siteCode",alternate = {"SiteCode","sitecode","site_code","LocCode","locCode","loccode","loc_code","LocationCode","locationCode","locationcode","location_code"})
  @ColumnInfo(name = "site_code")
  @NotNull
  public String siteCode;
  
  /**
   * Instantiates a new Site Code.
   */
  public SiteCode(){/*Empty constructor*/}
  
  /**
   * Instantiates a new site type.
   *
   * @param siteTypeId   the site type id
   * @param siteCode the site code
   */
  @Ignore
  public SiteCode(Long siteTypeId, String siteCode){
    this.siteTypeId = siteTypeId;
    this.siteCode = siteCode;
  }
  
  /**
   * Instantiates a new Site Type.
   *
   * @param in the in
   */
  @Ignore
  protected SiteCode(Parcel in){
    siteTypeId = in.readLong();
    siteCode = in.readString();
  }
  
  /**
   * Get site type id string.
   *
   * @return the string
   */
  public Long getSiteTypeId(){ return chkNull(siteTypeId, 0l); }
  
  /**
   * Set site type id.
   *
   * @param siteTypeId the site type id
   */
  public void setSiteTypeId(Long siteTypeId){ this.siteTypeId = siteTypeId; }
  
  /**
   * Get site code string.
   *
   * @return the string
   */
  public String getSiteCode(){ return chkNull(siteCode, "").trim().toUpperCase(); }
  
  /**
   * Set site code
   *
   * @param siteCode the site code
   */
  public void setSiteCode(String siteCode){ this.siteCode = chkNull(siteCode,"").trim(); }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    SiteCode that = (SiteCode) o;
    return getSiteTypeId()>0 && isNonEmpty(getSiteCode()) && getSiteTypeId()==that.getSiteTypeId() && getSiteCode().equalsIgnoreCase(that.getSiteCode());
  }
  
  @Override
  public String toString(){ return getSiteCode(); }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeLong(siteTypeId);
    dest.writeString(siteCode);
  }
  
  @Override
  public int describeContents(){ return 0; }
}
