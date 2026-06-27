package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.common.AppConstants;
import com.lidroid.xutils.db.annotation.NotNull;

import java.io.Serializable;

/**
 * The HU details.
 */
@Entity(tableName = "hu_details", indices = {@Index(value = {"trip_no", "hu_no", "type","article_code","ean"}, unique = true)})
public class HUDetails implements Serializable, Parcelable{
  
  public static final Creator<HUDetails> CREATOR = new Creator<HUDetails>(){
    @Override
    public HUDetails createFromParcel(Parcel in){
      return new HUDetails(in);
    }
    
    @Override
    public HUDetails[] newArray(int size){
      return new HUDetails[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "pk_id")
  public Integer pkId;
  @NotNull
  @ColumnInfo(name = "trip_no", defaultValue = "")
  @SerializedName("tripNum")
  public String tripNumber;
  @NotNull
  @ColumnInfo(name = "hu_no", defaultValue = "")
  @SerializedName(value = "huNum", alternate = {"HuNum", "HUNum", "HUNumber", "HuNumber", "huNumber"})
  public String huNumber;
  @NotNull
  @SerializedName(value = "type",alternate = {"OperationType","operationType","operationtype","operation_type","OpType","opType","optype","op_type","TYPE","Type"})
  @ColumnInfo(name = "type", defaultValue = "IN")
  public String type="";
  @SerializedName(value = "articleNo", alternate = {"articleno", "article_no", "ArticleNo", "articleCode", "article_code", "ArticleCode", "article", "Article"})
  @ColumnInfo(name = "article_code", defaultValue = "")
  public String articleCode = "";
  //@SerializedName(value = "skus", alternate = {"EAN","Ean","ean","EANS","EANs","Eans","eans","BarCode", "Barcode", "barCode", "barcode", "bar_code","BarCodes", "Barcodes", "barCodes", "barcodes", "bar_codes", "SkuId", "Skuid", "skuid", "skuId", "skuID", "sku_id","SkuIds", "Skuids", "skuids", "skuIds", "skuIDs", "sku_ids","SKUS","Skus","SKU","Sku","sku","isbn","Isbn","ISBN","isbns","Isbns","ISBNS"})
  @SerializedName("skus1")
  @ColumnInfo(name = "ean", defaultValue = "")
  public String ean = "";
  //@SerializedName(value = "rfids", alternate = {"RFIDS","RFIds","RfIds","Rfids","rfIds","rf_ids","RFID","RfId","rfId","rfid","rf_id","EPCS","Epcs","epcs","EPC","Epc","epc"})
  @SerializedName(value = "epcs1")
  @ColumnInfo(name = "rfid", defaultValue = "")
  public String rfid = "";
  @SerializedName(value = "expQty",alternate = {"packQty","eanQty","totalQty","qty","Qty"})
  @ColumnInfo(name = "exp_qty", defaultValue = "0")
  public int expQty=0;
  @SerializedName(value = "scanQty",alternate = {"ScanQty","scan_qty"})
  @ColumnInfo(name = "scan_qty", defaultValue = "0")
  public int scanQty=0;
  @SerializedName(value = "isUploaded", alternate = {"uploaded"})
  @ColumnInfo(name = "is_uploaded", defaultValue = "false")
  public boolean isUploaded = false;
  
  /**
   * Instantiates a new HU details.
   */
  public HUDetails(){/*Empty constructor*/}
  
  /**
   * Instantiates a new HU details.
   *
   * @param in the in
   */
  protected HUDetails(Parcel in){
    tripNumber = in.readString();
    huNumber = in.readString();
    type = in.readString();
    articleCode = in.readString();
    ean = in.readString();
    rfid = in.readString();
    expQty = in.readInt();
    scanQty = in.readInt();
    isUploaded = in.readByte() != 0;
  }
  
  /**
   * Get trip number string.
   *
   * @return the string
   */
  public String getTripNumber(){
    return chkNull(tripNumber, "");
  }
  
  /**
   * Set trip number.
   *
   * @param tripNumber the trip number
   */
  public void setTripNumber(String tripNumber){
    this.tripNumber = tripNumber;
  }
  
  /**
   * Get trip number string.
   *
   * @return the string
   */
  public String getHuNumber(){
    return chkNull(huNumber, "");
  }
  
  /**
   * Set hu number.
   *
   * @param huNumber the hu number
   */
  public void setHuNumber(String huNumber){
    this.huNumber = huNumber;
  }
  
  
  /**
   * Get type string.
   *
   * @return the string
   */
  public String getType(){
    return chkNull(type, "");
  }
  
  /**
   * Set type.
   *
   * @param type the type
   */
  public void setType(String type){
    this.type = type;
  }
  
  public String getArticleCode(){
    return articleCode;
  }
  
  public void setArticleCode(String articleCode){
    this.articleCode = articleCode;
  }
  
  public String getEan(){
    return ean;
  }
  
  public void setEan(String ean){
    this.ean = ean;
  }
  
  public String getRfid(){
    return rfid;
  }
  
  public void setRfid(String rfid){
    this.rfid = rfid;
  }
  
  public int getExpQty(){
    return chkNull(expQty, 0);
  }
  
  public void setExpQty(int expQty){
    this.expQty = expQty;
  }
  
  public int getScanQty(){
    return chkNull(scanQty, 0);
  }
  
  public void setScanQty(int scanQty){
    this.scanQty = scanQty;
  }
  
  public boolean isUploaded(){
    return isUploaded;
  }
  
  public void setUploaded(boolean uploaded){
    isUploaded = uploaded;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    HUDetails that = (HUDetails) o;
    return isNonEmpty(tripNumber) && tripNumber.equals(that.tripNumber) && isNonEmpty(huNumber) && huNumber.equals(that.huNumber);
  }
  
  @Override
  public int describeContents(){ return 0; }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeString(tripNumber);
    dest.writeString(huNumber);
    dest.writeString(type);
    dest.writeString(articleCode);
    dest.writeString(ean);
    dest.writeString(rfid);
    dest.writeInt(chkNull(expQty, 0));
    dest.writeInt(chkNull(scanQty, 0));
    dest.writeByte((byte) (isUploaded ? 1 : 0));
  }
}
