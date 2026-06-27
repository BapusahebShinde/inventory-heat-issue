package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.lidroid.xutils.db.annotation.NotNull;

import java.io.Serializable;

/**
 * The Brand wise zone inventory.
 */
@Entity(tableName = "inventory_dashboard_count")
public class BrandWiseZoneInventory implements Serializable, Parcelable{
  
  public static final Creator<BrandWiseZoneInventory> CREATOR = new Creator<BrandWiseZoneInventory>(){
    @Override
    public BrandWiseZoneInventory createFromParcel(Parcel in){
      return new BrandWiseZoneInventory(in);
    }
    
    @Override
    public BrandWiseZoneInventory[] newArray(int size){
      return new BrandWiseZoneInventory[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "pk_no")
  public Integer pkNo;
  @SerializedName(value = "brandName", alternate = {"BrandDesc", "brandDesc", "brand_desc", "branddesc", "Brand", "brand", "BrandName", "brand_name", "brandname"})
  @ColumnInfo(name = "brand_name")
  public String brandName;
  @SerializedName(value = "categoryName", alternate = {"Category", "category", "CategoryName", "category_name", "categoryname"})
  @ColumnInfo(name = "category_name")
  public String categoryName;
  @SerializedName(value = "zoneId", alternate = {"ZoneId", "zone_id", "zoneid"})
  @ColumnInfo(name = "zone_id")
  @NotNull
  public String zoneId;
  @SerializedName(value = "zoneName", alternate = {"ZoneName", "zonename", "Zone_Name", "zone_name", "zone", "Zone"})
  @ColumnInfo(name = "zone_name")
  @NotNull
  public String zoneName;
  @SerializedName("zoneType")
  @ColumnInfo(name = "zone_type")
  public String zoneType;
  @SerializedName("isDefault")
  @ColumnInfo(name = "is_default", defaultValue = "false")
  public Boolean isDefault;
  @SerializedName("inventoryCount")
  @ColumnInfo(name = "inventory_count", defaultValue = "0")
  public int inventoryCount;
  @SerializedName("shortage")
  @ColumnInfo(name = "shortage", defaultValue = "0")
  public int shortage;
  
  /**
   * Instantiates a new Brand wise zone inventory.
   */
  public BrandWiseZoneInventory(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Brand wise zone inventory.
   *
   * @param in the in
   */
  protected BrandWiseZoneInventory(Parcel in){
    brandName = in.readString();
    categoryName = in.readString();
    zoneId = in.readString();
    zoneName = in.readString();
    zoneType = in.readString();
    isDefault = in.readByte() > 0;
    inventoryCount = in.readInt();
    shortage = in.readInt();
  }
  
  @Override
  public String toString(){ return chkNull(brandName, ""); }
  
  @Override
  public int describeContents(){ return 0; }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    parcel.writeString(brandName);
    parcel.writeString(categoryName);
    parcel.writeString(zoneId);
    parcel.writeString(zoneName);
    parcel.writeString(zoneType);
    parcel.writeByte((byte) (isDefault == null && isDefault ? 1 : 0));
    parcel.writeInt(chkNull(inventoryCount, 0));
    parcel.writeInt(chkNull(shortage, 0));
  }
}
