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
import com.lidroid.xutils.db.annotation.NotNull;
import com.lidroid.xutils.db.annotation.Unique;

import java.io.Serializable;

/**
 * The Site Type.
 */
@Entity(tableName = "site_types", indices = {@Index(value = {"site_type_name"}, unique = true)})
public class SiteType implements Serializable, Parcelable{
  
  public static final Creator<SiteType> CREATOR = new Creator<SiteType>(){
    @Override
    public SiteType createFromParcel(Parcel in){
      return new SiteType(in);
    }
    
    @Override
    public SiteType[] newArray(int size){
      return new SiteType[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "site_type_no")
  public Integer siteTypeNo;
  @SerializedName(value = "siteTypeId",alternate = {"SiteID","SiteId","Siteid","siteID","siteId","siteid","site_id","SiteTypeID","SiteTypeId","SiteTypeid","siteTypeID","sitetypeid","site_type_id","LocID","LocId","Locid","locID","locId","locid","loc_id","LocTypeID","LocTypeId","LocTypeid","locTypeID","locTypeId","loctypeid","loc_type_id","SupplyChainID","SupplyChainId","SupplyChainid","supplyChainID","supplyChainId","supplychainid","supply_chain_id","SupplyChainTypeid","supplyChainTypeID","supplyChainTypeId","supplychaintypeid","supply_chain_type_id"})
  @ColumnInfo(name = "site_type_id", defaultValue = "0")
  @Unique
  @NotNull
  public Long siteTypeId;
  @SerializedName("siteTypeName")
  @ColumnInfo(name = "site_type_name")
  @Unique
  @NotNull
  public String siteTypeName;
  
  /**
   * Instantiates a new Site Type.
   */
  public SiteType(){/*Empty constructor*/}
  
  /**
   * Instantiates a new site type.
   *
   * @param siteTypeId   the site type id
   * @param siteTypeName the site type name
   */
  @Ignore
  public SiteType(Long siteTypeId, String siteTypeName){
    this.siteTypeId = siteTypeId;
    this.siteTypeName = siteTypeName;
  }
  
  /**
   * Instantiates a new Site Type.
   *
   * @param in the in
   */
  @Ignore
  protected SiteType(Parcel in){
    siteTypeId = in.readLong();
    siteTypeName = in.readString();
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
   * Get site type name string.
   *
   * @return the string
   */
  public String getSiteTypeName(){ return chkNull(siteTypeName, "").trim(); }
  
  /**
   * Set site type name.
   *
   * @param siteTypeName the site type name
   */
  public void setSiteTypeName(String siteTypeName){ this.siteTypeName = chkNull(siteTypeName,"").trim(); }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    SiteType that = (SiteType) o;
    return getSiteTypeId()>0 && that.getSiteTypeId()>0 && getSiteTypeId()==that.getSiteTypeId();
  }
  
  @Override
  public String toString(){ return getSiteTypeName(); }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeLong(siteTypeId);
    dest.writeString(siteTypeName);
  }
  
  @Override
  public int describeContents(){ return 0; }
}
