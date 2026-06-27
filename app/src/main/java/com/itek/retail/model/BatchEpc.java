package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.lidroid.xutils.db.annotation.NotNull;

import java.io.Serializable;

/**
 * The Batch Epcs.
 */
@Entity(tableName = "batch_epc", indices = {@Index(value = {"batch_id","epc"}, unique = true)})
public class BatchEpc implements Serializable, Parcelable{
  
  public static final Creator<BatchEpc> CREATOR = new Creator<BatchEpc>(){
    @Override
    public BatchEpc createFromParcel(Parcel in){
      return new BatchEpc(in);
    }
    
    @Override
    public BatchEpc[] newArray(int size){
      return new BatchEpc[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "batch_no")
  public Integer batchNo;
  @SerializedName(value = "batchId", alternate = {"batch_id", "BatchId", "batchNo", "BatchNo", "batch_no", "batchNum", "BatchNum", "batch_num", "batch", "Batch", "ReplenishBatchId", "replenishBatchId", "replenish_batch_id", "ReplenishmentBatchId", "replenishmentBatchId", "replenishment_batch_id"})
  @ColumnInfo(name = "batch_id",defaultValue = "")
  @NotNull
  public String batchId;
  @SerializedName(value = "articleNo", alternate = {"articleno", "article_no", "ArticleNo", "articleCode", "article_code", "ArticleCode", "article", "Article"})
  @ColumnInfo(name = "article",defaultValue = "")
  public String article;
  @SerializedName(value = "ean", alternate = {"EAN", "BarCode", "Barcode", "barCode", "barcode", "bar_code", "SkuId", "Skuid", "skuid", "skuId", "skuID", "sku_id", "isbn", "ISBN"})
  @ColumnInfo(name = "ean", defaultValue = "")
  public String ean;
  @SerializedName(value = "epc", alternate = {"Epc", "EPC", "rfid", "RFID", "rf_id", "rfId", "rfID"})
  @ColumnInfo(name = "epc", defaultValue = "")
  @NotNull
  public String epc;
  
  /**
   * Instantiates a new Brand.
   */
  public BatchEpc(){/*Empty constructor*/}
  
  protected BatchEpc(Parcel in){
    batchId = in.readString();
    article = in.readString();
    ean = in.readString();
    epc = in.readString();
  }
  
  @Ignore
  public BatchEpc(String batchId, String article, String ean, String epc){
    this.batchId=batchId;
    this.article=article;
    this.ean=ean;
    this.epc=epc;
  }
  
  public String getBatchId(){
    return chkNull(batchId, "");
  }
  
  public void setBatchId(String batchId){
    this.batchId = batchId;
  }
  
  public String getArticle(){
    return chkNull(article, "");
  }
  
  public void setArticle(String article){
    this.article = article;
  }
  
  public String getEan(){
    return chkNull(ean, "");
  }
  
  public void setEan(String ean){
    this.ean = ean;
  }
  
  public String getEpc(){
    return chkNull(epc,"");
  }
  
  public void setEpc(String epc){
    this.epc = epc;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    BatchEpc that = (BatchEpc) o;
    return getBatchId().equalsIgnoreCase(that.getBatchId()) && getEpc().equalsIgnoreCase(that.getEpc());
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
    dest.writeString(article);
    dest.writeString(ean);
    dest.writeString(epc);
  }
}
