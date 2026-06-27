package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.common.AppConstants;
import com.lidroid.xutils.db.annotation.NotNull;

import java.io.Serializable;

/**
 * The Replenish Batch.
 */
public class ReplenishBatch implements Serializable, Parcelable{
  
  public static final Creator<ReplenishBatch> CREATOR = new Creator<ReplenishBatch>(){
    @Override
    public ReplenishBatch createFromParcel(Parcel in){
      return new ReplenishBatch(in);
    }
    
    @Override
    public ReplenishBatch[] newArray(int size){
      return new ReplenishBatch[size];
    }
  };
  @SerializedName(value = "batchId", alternate = {"batch_id", "BatchId", "batchNo", "BatchNo","batch_no", "batchNum", "BatchNum","batch_num", "batch", "Batch","ReplenishBatchId","replenishBatchId","replenish_batch_id","ReplenishmentBatchId","replenishmentBatchId","replenishment_batch_id"})
  public String batchId;
  @SerializedName(value = "batchDate",alternate = {"BatchDate","batch_date","BatchDateTime","batchDateTime","batch_date_time","ReplenishBatchDate","replenishBatchDate","replenish_batch_date","ReplenishmentBatchDate","replenishmentBatchDate","replenishment_batch_date","CreatedOn","createdOn","created_on","DateTime","dateTime","date_time","Date","date"})
  public String batchDate;
  @SerializedName(value = "status",alternate = {"Status","batchStatus","BatchStatus","batch_status","replenishBatchStatus","ReplenishBatchStatus","replenish_batch_status","replenishmentBatchStatus","ReplenishmentBatchStatus","replenishment_batch_status"})
  public String status;
  @SerializedName(value = "totalQty", alternate = {"totalqty", "total_qty", "total","totalBatchQty", "total_batch_qty","qty","Qty","batchQty","BatchQty","batch_qty","batchTotalQty","BatchTotalQty","batch_total_qty","TaskQty","taskQty","task_qty"})
  public Integer totalQty = 0;
  @SerializedName(value = "pickQty", alternate = {"pickqty", "pick_qty", "pick","pickedQty", "picked_qty","picked", "pickedqty","pickedBatchQty", "picked_batch_qty", "pickedbatchqty","batchPickQty","BatchPickQty","batch_pick_qty","batchPickedQty","BatchPickedQty","batch_picked_qty","replenishBatchPickedQty","ReplenishBatchPickedQty","replenish_batch_picked_qty","replenishmentBatchPickedQty","ReplenishmentBatchPickedQty","replenishment_batch_picked_qty"})
  public Integer pickQty = 0;

  public ReplenishBatch(){}
  
  protected ReplenishBatch(Parcel in){
    batchId = in.readString();
    batchDate = in.readString();
    status = in.readString();
    if(in.readByte() == 0){ totalQty = null; }
    else{ totalQty = in.readInt(); }
    if(in.readByte() == 0){ pickQty = null; }
    else{ pickQty = in.readInt(); }
  }
  
  public String getBatchId(){
    return chkNull(batchId,"");
  }
  
  public void setBatchId(String batchId){
    this.batchId = batchId;
  }
  
  public String getBatchDate(){
    return chkNull(batchDate,"");
  }
  
  public void setBatchDate(String batchDate){
    this.batchDate = batchDate;
  }
  
  public String getStatus(){
    return chkNull(status, AppConstants.BATCH_STATUS_INACTIVE);
  }
  
  public void setStatus(String status){
    this.status = chkNull(status,"");
  }
  
  public boolean isActive(){
    return getStatus().equalsIgnoreCase(AppConstants.BATCH_STATUS_ACTIVE);
  }
  
  public Integer getTotalQty(){
    return chkNull(totalQty,0);
  }
  
  public void setTotalQty(Integer totalQty){
    this.totalQty = totalQty;
  }
  
  public Integer getPickQty(){
    return chkNull(pickQty,0);
  }
  
  public void setPickQty(Integer pickQty){
    this.pickQty = pickQty;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    ReplenishBatch that = (ReplenishBatch) o;
    return getBatchId().equalsIgnoreCase(that.getBatchId());
  }
  
  @Override
  public String toString(){ return getBatchId(); }
  
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
    dest.writeString(batchId);
    dest.writeString(batchDate);
    dest.writeString(status);
    if(totalQty == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(totalQty);
    }
    if(pickQty == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(pickQty);
    }
  }
}
