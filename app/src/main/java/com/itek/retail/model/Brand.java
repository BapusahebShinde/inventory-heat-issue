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

import java.io.Serializable;

/**
 * The Brand.
 */
@Entity(tableName = "brands", indices = {@Index(value = {"brand_name"}, unique = true)})
public class Brand implements Serializable, Parcelable{
  
  public static final Creator<Brand> CREATOR = new Creator<Brand>(){
    @Override
    public Brand createFromParcel(Parcel in){
      return new Brand(in);
    }
    
    @Override
    public Brand[] newArray(int size){
      return new Brand[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "brand_no")
  public Integer brandNo;
  @SerializedName("brandId")
  @ColumnInfo(name = "brand_id")
  @NotNull
  public String brandId;
  @SerializedName("brandName")
  @ColumnInfo(name = "brand_name")
  @NotNull
  public String brandName;
  @SerializedName("brandType")
  @ColumnInfo(name = "brand_type")
  public String brandType;
  
  /**
   * Instantiates a new Brand.
   */
  public Brand(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Brand.
   *
   * @param brandId   the brand id
   * @param brandName the brand name
   */
  @Ignore
  public Brand(String brandId, String brandName){
    this.brandId = brandId;
    this.brandName = brandName;
    this.brandType = brandName;
  }
  
  /**
   * Instantiates a new Brand.
   *
   * @param in the in
   */
  @Ignore
  protected Brand(Parcel in){
    brandId = in.readString();
    brandName = in.readString();
    brandType = in.readString();
  }
  
  /**
   * Get brand id string.
   *
   * @return the string
   */
  public String getBrandId(){ return chkNull(brandId, "0"); }
  
  /**
   * Set brand id.
   *
   * @param brandId the brand id
   */
  public void setBrandId(String brandId){ this.brandId = brandId; }
  
  /**
   * Get brand name string.
   *
   * @return the string
   */
  public String getBrandName(){ return chkNull(brandName, "").trim(); }
  
  /**
   * Set brand name.
   *
   * @param brandName the brand name
   */
  public void setBrandName(String brandName){ this.brandName = brandName.trim(); }
  
  /**
   * Get brand type string.
   *
   * @return the string
   */
  public String getBrandType(){ return chkNull(brandType, ""); }
  
  /**
   * Set brand type.
   *
   * @param brandType the brand type
   */
  public void setBrandType(String brandType){ this.brandType = brandType; }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    Brand that = (Brand) o;
    return getBrandName().equalsIgnoreCase(that.getBrandName());
  }
  
  @Override
  public String toString(){ return getBrandName(); }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeString(brandId);
    dest.writeString(brandName);
    dest.writeString(brandType);
  }
  
  @Override
  public int describeContents(){ return 0; }
}
