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
import com.itek.retail.common.AppCommonMethods;
import com.lidroid.xutils.db.annotation.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Brand eans
 */
@Entity(tableName = "brand_eans", indices = {@Index(value = {"brand_name", "ean"}, unique = true)})
public class BrandEans implements Serializable, Parcelable{
  
  public static final Creator<BrandEans> CREATOR = new Creator<BrandEans>(){
    @Override
    public BrandEans createFromParcel(Parcel in){
      return new BrandEans(in);
    }
    
    @Override
    public BrandEans[] newArray(int size){
      return new BrandEans[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "brand_ean_no")
  public Integer brandEanNo;
  @SerializedName("brandName")
  @ColumnInfo(name = "brand_name")
  @NotNull
  public String brandName;
  @SerializedName("eans")
  @ColumnInfo(name = "ean")
  @NotNull
  public String ean;
  @SerializedName("scanQty")
  @ColumnInfo(name = "scan_qty")
  @NotNull
  public Integer scanQty = 0;
  @SerializedName("totalQty")
  @ColumnInfo(name = "total_qty")
  @NotNull
  public Integer totalQty = 0;
  
  /**
   * Instantiates a new Brand ean qty.
   */
  public BrandEans(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Brand ean qty.
   *
   * @param brandName the brand name
   * @param ean       the ean
   * @param totalQty  the total qty
   */
  @Ignore
  public BrandEans(String brandName, String ean, int totalQty){
    this.brandName = brandName;
    this.ean = ean;
    this.totalQty = totalQty;
  }
  
  @Ignore
  public BrandEans(String brandName, String ean, int scanQty, int totalQty){
    this.brandName = brandName;
    this.ean = ean;
    this.scanQty = scanQty;
    this.totalQty = totalQty;
  }
  
  /**
   * Instantiates a new Brand ean qty.
   *
   * @param in the in
   */
  protected BrandEans(Parcel in){
    brandName = in.readString();
    ean = in.readString();
    if(in.readByte() == 0){ scanQty = null; }
    else{ scanQty = in.readInt(); }
    if(in.readByte() == 0){ totalQty = null; }
    else{ totalQty = in.readInt(); }
  }
  
  @Override
  public int describeContents(){
    return 0;
  }
  
  /**
   * Get brand name string.
   *
   * @return the string
   */
  public String getBrandName(){ return chkNull(brandName, ""); }
  
  /**
   * Set brand name.
   *
   * @param brandName the brand name
   */
  public void setBrandName(String brandName){ this.brandName = brandName; }
  
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
   * Get scan qty integer.
   *
   * @return the integer
   */
  public Integer getScanQty(){ return chkNull(scanQty, 0); }
  
  /**
   * Set scan qty.
   *
   * @param scanQty the ean qty
   */
  public void setScanQty(Integer scanQty){ this.scanQty = chkNull(scanQty, 0); }
  
  /**
   * Get total qty integer.
   *
   * @return the integer
   */
  public Integer getTotalQty(){ return chkNull(totalQty, 0); }
  
  /**
   * Set total qty.
   *
   * @param totalQty the ean qty
   */
  public void setTotalQty(Integer totalQty){ this.totalQty = chkNull(totalQty, 0); }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    parcel.writeString(brandName);
    parcel.writeString(ean);
    if(scanQty == null){ parcel.writeByte((byte) 0); }
    else{
      parcel.writeByte((byte) 1);
      parcel.writeInt(scanQty);
    }
    if(totalQty == null){ parcel.writeByte((byte) 0); }
    else{
      parcel.writeByte((byte) 1);
      parcel.writeInt(totalQty);
    }
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    BrandEans that = (BrandEans) o;
    return brandName.equalsIgnoreCase(that.brandName) && ean.equalsIgnoreCase(that.ean);
  }
  
  @Override
  public int hashCode(){
    return Objects.hash(ean);
  }
  
  @Override
  public String toString(){
    return getEan() + " (" + getTotalQty() + ")";
  }
}
