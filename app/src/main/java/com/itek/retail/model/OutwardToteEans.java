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
@Entity(tableName = "owt_tote_eans", indices = {@Index(value = {"tote_type", "ean"}, unique = true)})
public class OutwardToteEans implements Serializable, Parcelable{
  
  public static final Creator<OutwardToteEans> CREATOR = new Creator<OutwardToteEans>(){
    @Override
    public OutwardToteEans createFromParcel(Parcel in){
      return new OutwardToteEans(in);
    }
    
    @Override
    public OutwardToteEans[] newArray(int size){
      return new OutwardToteEans[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "owt_tote_ean_no")
  public Integer toteEanNo;
  @SerializedName("tote_type")
  @ColumnInfo(name = "tote_type")
  @NotNull
  public String toteType;
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
  public OutwardToteEans(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Brand ean qty.
   *
   * @param toteType the toteType
   * @param ean       the ean
   */
  @Ignore
  public OutwardToteEans(String toteType, String ean){
    this.toteType = toteType;
    this.ean = ean;
    this.totalQty = totalQty;
  }
  
  @Ignore
  public OutwardToteEans(String brandName, String ean, int totalQty){
    this.toteType = brandName;
    this.ean = ean;
    this.totalQty = totalQty;
  }
  
  @Ignore
  public OutwardToteEans(String brandName, String ean, int scanQty, int totalQty){
    this.toteType = brandName;
    this.ean = ean;
    this.scanQty = scanQty;
    this.totalQty = totalQty;
  }
  
  /**
   * Instantiates a new outward tote ean qty.
   *
   * @param in the in
   */
  protected OutwardToteEans(Parcel in){
    toteType = in.readString();
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
   * Get tote type string.
   *
   * @return the string
   */
  public String getToteType(){ return chkNull(toteType, ""); }
  
  /**
   * Set tote type
   *
   * @param toteType the tote type
   */
  public void setToteType(String toteType){ this.toteType = toteType; }
  
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
    parcel.writeString(toteType);
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
    OutwardToteEans that = (OutwardToteEans) o;
    return toteType.equalsIgnoreCase(that.toteType) && ean.equalsIgnoreCase(that.ean);
  }
  
  @Override
  public int hashCode(){
    return Objects.hash(toteType,ean);
  }
  
  @Override
  public String toString(){
    return  getEan() + " (" + getTotalQty() + ")";
  }
}
