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

import java.io.Serializable;

/**
 * The Replenish Batch.
 */
@Entity(tableName = "replenish_batch_details", indices = {@Index(value = {"batch_id","article","ean"}, unique = true)})
public class ReplenishBatchDetails implements Serializable, Parcelable{
  
  public static final Creator<ReplenishBatchDetails> CREATOR = new Creator<ReplenishBatchDetails>(){
    @Override
    public ReplenishBatchDetails createFromParcel(Parcel in){
      return new ReplenishBatchDetails(in);
    }
    
    @Override
    public ReplenishBatchDetails[] newArray(int size){
      return new ReplenishBatchDetails[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "pk_id")
  public Integer pkId;
  @SerializedName(value = "batchId", alternate = {"batch_id", "BatchId", "batchNo", "BatchNo", "batch_no", "batchNum", "BatchNum", "batch_num", "batch", "Batch", "ReplenishBatchId", "replenishBatchId", "replenish_batch_id", "ReplenishmentBatchId", "replenishmentBatchId", "replenishment_batch_id"})
  @ColumnInfo(name = "batch_id")
  public String batchId;
  @SerializedName(value = "articleNo", alternate = {"articleno", "article_no", "ArticleNo", "articleCode", "article_code", "ArticleCode", "article", "Article"})
  @ColumnInfo(name = "article")
  public String article;
  @ColumnInfo(name = "category")
  @SerializedName(value = "category", alternate = "Category")
  public String category;
  @ColumnInfo(name = "matkl")
  @SerializedName(value = "matkl", alternate = {"Matkl", "family", "Family", "familyName", "FamilyName", "familyname", "family_name"})
  public String matkl;
  @ColumnInfo(name = "color")
  @SerializedName(value = "color", alternate = {"ColorName", "colorName","colorname", "color_name", "purity", "ColorCode", "colorCode", "color_code"})
  public String color;
  @ColumnInfo(name = "size")
  @SerializedName(value = "size", alternate = {"Size", "weight", "pages"})
  public String size;
  @ColumnInfo(name = "description")
  @SerializedName(value = "description", alternate = {"desc"})
  public String description;
  @SerializedName(value = "ean", alternate = {"EAN", "BarCode", "Barcode", "barCode", "barcode", "bar_code", "SkuId", "Skuid", "skuid", "skuId", "skuID", "sku_id", "isbn", "ISBN"})
  @ColumnInfo(name = "ean")
  public String ean;
  @SerializedName(value = "eanPickQty", alternate = {"EanPickQty","eanQty","EanQty","ean_qty", "ean_pick_qty"})
  @ColumnInfo(name = "ean_pick_qty")
  public Integer eanPickQty = 0;
  @SerializedName(value = "totalQty", alternate = {"totalqty", "total_qty", "total", "totalBatchQty", "total_batch_qty", "qty", "Qty", "batchQty", "BatchQty", "batch_qty", "batchTotalQty", "BatchTotalQty", "batch_total_qty", "TaskQty", "taskQty", "task_qty"})
  @ColumnInfo(name = "total_qty")
  public Integer totalQty = 0;
  @SerializedName(value = "pickQty", alternate = {"pickqty", "pick_qty", "pick", "pickedQty", "picked_qty", "picked", "pickedqty", "pickedBatchQty", "picked_batch_qty", "pickedbatchqty", "batchPickQty", "BatchPickQty", "batch_pick_qty", "batchPickedQty", "BatchPickedQty", "batch_picked_qty", "replenishBatchPickedQty", "ReplenishBatchPickedQty", "replenish_batch_picked_qty", "replenishmentBatchPickedQty", "ReplenishmentBatchPickedQty", "replenishment_batch_picked_qty"})
  @ColumnInfo(name = "pick_qty")
  public Integer pickQty = 0;
  @SerializedName(value = "seqNumber", alternate = {"seq_number"})
  @ColumnInfo(name = "seq_number" , defaultValue = "0")
  public Integer seqNumber = 0;
  
  /**
   * Instantiates a new Replenish batch details.
   */
  public ReplenishBatchDetails(){/*Empty constructor*/}
  
  @Ignore
  public ReplenishBatchDetails(final ReplenishBatchDetails replenishBatchDetails){
    this.batchId = replenishBatchDetails.batchId;
    this.article = replenishBatchDetails.article;
    this.category = replenishBatchDetails.category;
    this.matkl = replenishBatchDetails.matkl;
    this.color = replenishBatchDetails.color;
    this.size = replenishBatchDetails.size;
    this.description = replenishBatchDetails.description;
    this.ean = replenishBatchDetails.ean;
    this.eanPickQty = replenishBatchDetails.eanPickQty;
    this.totalQty = replenishBatchDetails.totalQty;
    this.pickQty = replenishBatchDetails.pickQty;
    this.seqNumber = replenishBatchDetails.seqNumber;
  }
  
  protected ReplenishBatchDetails(Parcel in){
    batchId = in.readString();
    article = in.readString();
    category = in.readString();
    matkl = in.readString();
    color = in.readString();
    size = in.readString();
    description = in.readString();
    ean = in.readString();
    if(in.readByte() == 0){ eanPickQty = null; }
    else{ eanPickQty = in.readInt(); }
    if(in.readByte() == 0){ totalQty = null; }
    else{ totalQty = in.readInt(); }
    if(in.readByte() == 0){ pickQty = null; }
    else{ pickQty = in.readInt(); }
    if(in.readByte() == 0){ seqNumber = null; }
    else{ seqNumber = in.readInt(); }
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

  public String getCategory(){
    return chkNull(category, "");
  }

  public void setCategory(String category){
    this.category = category;
  }

  public String getMatkl(){
    return chkNull(matkl, "");
  }

  public void setMatkl(String matkl){
    this.matkl = matkl;
  }

  public String getColor() {
    return chkNull(color,"");
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getSize() {
    return chkNull(size,"");
  }

  public void setSize(String size) {
    this.size = size;
  }

  public String getDescription() {
    return chkNull(description,"");
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getEan(){
    return chkNull(ean, "");
  }
  
  public void setEan(String ean){
    this.ean = ean;
  }
  
  public Integer getEanPickQty(){
    return chkNull(eanPickQty, 0);
  }
  
  public void setEanPickQty(Integer eanPickQty){
    this.eanPickQty = eanPickQty;
  }
  
  public Integer getTotalQty(){
    return chkNull(totalQty, 0);
  }
  
  public void setTotalQty(Integer totalQty){
    this.totalQty = totalQty;
  }
  
  public Integer getPickQty(){
    return chkNull(pickQty, 0);
  }
  
  public void setPickQty(Integer pickQty){
    this.pickQty = pickQty;
  }

  public Integer getSeqNumber(){
    return chkNull(seqNumber, 0);
  }

  public void setSeqNumber(Integer seqNumber){
    this.seqNumber = seqNumber;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    ReplenishBatchDetails that = (ReplenishBatchDetails) o;
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
    dest.writeString(article);
    dest.writeString(category);
    dest.writeString(matkl);
    dest.writeString(color);
    dest.writeString(size);
    dest.writeString(description);
    dest.writeString(ean);
    if(eanPickQty == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(eanPickQty);
    }
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
    if(seqNumber == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(seqNumber);
    }
  }
}
