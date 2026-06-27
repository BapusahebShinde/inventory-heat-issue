package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.lidroid.xutils.db.annotation.NotNull;

import java.io.Serializable;

/**
 * The Outward batch
 */
@Entity(tableName = "owt_batch", indices = {@Index(value = {"batch_id"}, unique = true)})
public class OutwardBatch implements Serializable, Parcelable{
  
  public static final Creator<OutwardBatch> CREATOR = new Creator<OutwardBatch>(){
    @Override
    public OutwardBatch createFromParcel(Parcel in){
      return new OutwardBatch(in);
    }
    
    @Override
    public OutwardBatch[] newArray(int size){
      return new OutwardBatch[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "id")
  public Long id;
  @SerializedName(value = "owtTypeId", alternate = {"OwtTypeId", "typeId", "TypeId", "typeMasterId", "TypeMasterId", "outwardTypeMasterId", "OutwardTypeMasterId", "storeOutwardTypeMasterId", "StoreOutwardTypeMasterId"})
  @ColumnInfo(name = "owt_type_id")
  public Long owtTypeId;
  @SerializedName(value = "destSiteCode", alternate = {"DestSiteCode", "destSite", "DestSite", "siteCode", "SiteCode", "Destination", "destination", "Dest", "dest"})
  @ColumnInfo(name = "dest_site_code")
  public String destSiteCode;
  @SerializedName(value = "batchId", alternate = {"batch_id", "BatchId", "batchNo", "BatchNo", "batchNum", "BatchNum", "batch", "Batch"})
  @ColumnInfo(name = "batch_id")
  public String batchId;
  @SerializedName(value = "listRefBatchId", alternate = {"list_ref_batch_id", "ListRefBatchId", "listBatchId", "list_batch_id", "ListBatchId", "listId", "list_id", "ListId"})
  @ColumnInfo(name = "list_ref_batch_id")
  public String listRefBatchId;
  @SerializedName(value = "expectedQty", alternate = {"ExpectedQty", "expected_qty", "exp_qty", "expQty", "ExpQty"})
  @ColumnInfo(name = "list_exp_qty")
  public Long listExpQty;
  @ColumnInfo(name = "accepted_cartons", defaultValue = "0")
  public Integer acceptedCartons = 0;
  @ColumnInfo(name = "rejected_cartons", defaultValue = "0")
  public Integer rejectedCartons = 0;
  @ColumnInfo(name = "total_cartons", defaultValue = "0")
  public Integer totalCartons = 0;
  @SerializedName(value = "owtType", alternate = {"OwtType", "OutwardType", "outwardType", "type", "Type", "typeName", "TypeName", "outwardType", "OutwardType", "outwardTypeName", "OutwardTypeName"})
  @ColumnInfo(name = "owt_type")
  @NotNull
  String owtType;
  
  /**
   * Instantiates a new OutwardBatch.
   */
  public OutwardBatch(){/*Empty constructor*/}
  
  protected OutwardBatch(Parcel in){
    if(in.readByte() == 0){ id = null; }
    else{ id = in.readLong(); }
    if(in.readByte() == 0){ owtTypeId = null; }
    else{ owtTypeId = in.readLong(); }
    destSiteCode = in.readString();
    batchId = in.readString();
    listRefBatchId = in.readString();
    if(in.readByte() == 0){ listExpQty = null; }
    else{ listExpQty = in.readLong(); }
    if(in.readByte() == 0){ acceptedCartons = null; }
    else{ acceptedCartons = in.readInt(); }
    if(in.readByte() == 0){ rejectedCartons = null; }
    else{ rejectedCartons = in.readInt(); }
    if(in.readByte() == 0){ totalCartons = null; }
    else{ totalCartons = in.readInt(); }
    owtType = in.readString();
  }
  
  public Long getId(){
    return id;
  }
  
  public void setId(Long id){
    this.id = id;
  }
  
  public String getOwtType(){
    return owtType;
  }
  
  public void setOwtType(String owtType){
    this.owtType = owtType;
  }
  
  public Long getOwtTypeId(){
    return owtTypeId;
  }
  
  public void setOwtTypeId(Long owtTypeId){
    this.owtTypeId = owtTypeId;
  }
  
  public String getDestSiteCode(){
    return destSiteCode;
  }
  
  public void setDestSiteCode(String destSiteCode){
    this.destSiteCode = destSiteCode;
  }
  
  public String getBatchId(){
    return batchId;
  }
  
  public void setBatchId(String batchId){
    this.batchId = batchId;
  }
  
  public String getListRefBatchId(){
    return listRefBatchId;
  }
  
  public void setListRefBatchId(String listRefBatchId){
    this.listRefBatchId = listRefBatchId;
  }
  
  public Long getListExpQty(){
    return listExpQty;
  }
  
  public void setListExpQty(Long listExpQty){
    this.listExpQty = listExpQty;
  }
  
  public Integer getAcceptedCartons(){
    return chkNull(acceptedCartons, 0);
  }
  
  public void setAcceptedCartons(Integer acceptedCartons){
    this.acceptedCartons = acceptedCartons;
  }
  
  public Integer getRejectedCartons(){
    return chkNull(rejectedCartons, 0);
  }
  
  public void setRejectedCartons(Integer rejectedCartons){
    this.rejectedCartons = rejectedCartons;
  }
  
  public Integer getTotalCartons(){
    return chkNull(totalCartons, 0);
  }
  
  public void setTotalCartons(Integer totalCartons){
    this.totalCartons = totalCartons;
  }
  
  /**
   *
   */
  @Override
  public int describeContents(){
    return 0;
  }
  
  /**
   * @param dest  The Parcel in which the object should be written.
   * @param flags Additional flags about how the object should be written.
   *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
   */
  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags){
    
    if(id == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeLong(id);
    }
    if(owtTypeId == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeLong(owtTypeId);
    }
    dest.writeString(destSiteCode);
    dest.writeString(batchId);
    dest.writeString(listRefBatchId);
    if(listExpQty == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeLong(listExpQty);
    }
    if(acceptedCartons == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(acceptedCartons);
    }
    if(rejectedCartons == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(rejectedCartons);
    }
    if(totalCartons == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(totalCartons);
    }
    dest.writeString(owtType);
  }
}
