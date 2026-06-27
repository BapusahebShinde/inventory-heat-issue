package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * The Trip inventory.
 */

@Entity(tableName = "trip_inventory")

public class TripInventory implements Serializable, Parcelable{
  
  public static final Creator<TripInventory> CREATOR = new Creator<TripInventory>(){
    @Override
    public TripInventory createFromParcel(Parcel in){
      return new TripInventory(in);
    }
    
    @Override
    public TripInventory[] newArray(int size){
      return new TripInventory[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "trip_id")
  public Integer id;
  public Integer huQty = 0;
  public Integer scanCount = 0;
  @Ignore
  public Integer expCount = 0;
  @SerializedName("tripNum")
  @ColumnInfo(name = "trip_no", defaultValue = "") //column name
  public String tripNo = "";     //column data
  @SerializedName(value = "huNum", alternate = {"HuNum", "HUNum", "HUNumber", "HuNumber", "huNumber"})
  @ColumnInfo(name = "hu_no", defaultValue = "")
  public String huNo = "";
  @SerializedName(value = "skus", alternate = {"EAN","Ean","ean","EANS","EANs","Eans","eans","BarCode", "Barcode", "barCode", "barcode", "bar_code","BarCodes", "Barcodes", "barCodes", "barcodes", "bar_codes", "SkuId", "Skuid", "skuid", "skuId", "skuID", "sku_id","SkuIds", "Skuids", "skuids", "skuIds", "skuIDs", "sku_ids","SKUS","Skus","SKU","Sku","sku","isbn","Isbn","ISBN","isbns","Isbns","ISBNS"})
  @ColumnInfo(name = "ean", defaultValue = "")
  public String ean = "";
  //@SerializedName(value = "rfids", alternate = {"RFIDS","RFIds","RfIds","Rfids","rfIds","rf_ids","RFID","RfId","rfId","rfid","rf_id","EPCS","Epcs","epcs","EPC","Epc","epc"})
  @SerializedName(value = "epcs1")
  @ColumnInfo(name = "rfid", defaultValue = "")
  public String rfid = "";
  @SerializedName(value = "packQty",alternate = {"PackQty","packqty","pack_qty","SkuQty","skuQty","skuqty","sku_qty","EanQty","eanQty","eanqty","ean_qty","Qty","qty"})
  @ColumnInfo(name = "ean_qty", defaultValue = "0")
  public Integer eanQty = 0;
  @ColumnInfo(name = "epc", defaultValue = "")
  public String epc = "";
  @ColumnInfo(name = "tid", defaultValue = "")
  public String tid = "";
  @ColumnInfo(name = "user_action", defaultValue = "RFID")
  public String userAction = "";
  @ColumnInfo(name = "is_original", defaultValue = "true")
  public boolean isOriginal = true;
  @ColumnInfo(name = "status", defaultValue = "P")
  public String status = "P";
  @ColumnInfo(name = "reason", defaultValue = "")
  public String reason = "";
  @ColumnInfo(name = "attempts", defaultValue = "0")
  public Integer attempts = 0;
  @SerializedName("deliveryNum")
  //@SerializedName("delvNum")
  @ColumnInfo(name = "delivery_no", defaultValue = "")
  public String deliveryNo = "";
  @ColumnInfo(name = "is_server_entry", defaultValue = "true")
  public boolean isServerEntry = true;
  @ColumnInfo(name = "is_rescan", defaultValue = "true")
  public boolean isRescan = true;
  @ColumnInfo(name = "is_duplicate", defaultValue = "false")
  public boolean isDuplicate = false;
  @SerializedName(value = "articleNo", alternate = {"articleno", "article_no", "ArticleNo", "articleCode", "article_code", "ArticleCode", "article", "Article"})
  @ColumnInfo(name = "article_code", defaultValue = "")
  public String articleCode = "";
  @SerializedName(value = "serialNo", alternate = {"serialno", "serial_no", "SerialNo"})
  @ColumnInfo(name = "serial_no", defaultValue = "")
  public String serialNo = "";
  @SerializedName("isBarcodeBased")
  @ColumnInfo(name = "apparel_barcode_based", defaultValue = "false")
  public boolean apparelBarcodeBased;
  @SerializedName("fromVendor")
  @ColumnInfo(name = "fromvendor", defaultValue = "false")
  public boolean fromVendor = false;
  @SerializedName("isHuVerified")
  @ColumnInfo(name = "is_hu_verified", defaultValue = "false")
  public boolean isHuVerified = false;
  @ColumnInfo(name = "rssi", defaultValue = "")
  public String rssi = "";
  @ColumnInfo(name = "is_hard_tag", defaultValue = "null")
  public Boolean isHardTag = null;
  
  /**
   * Instantiates a new Trip inventory.
   */
  public TripInventory(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Trip inventory.
   *
   * @param tripNo     the trip no
   * @param deliveryNo the delivery no
   * @param huNo       the hu no
   */
  @Ignore
  public TripInventory(final String tripNo, final String deliveryNo, final String huNo){
    this.tripNo = tripNo;
    this.deliveryNo = deliveryNo;
    this.huNo = huNo;
    this.status = "P";
    this.reason = "";
    this.isServerEntry = false;
    this.isRescan = true;
    this.isDuplicate = false;
  }
  
  /**
   * Instantiates a new Trip inventory.
   *
   * @param in the in
   */
  protected TripInventory(Parcel in){
    if(in.readByte() == 0){ id = null; }
    else{ id = in.readInt(); }
    if(in.readByte() == 0){ huQty = null; }
    else{ huQty = in.readInt(); }
    if(in.readByte() == 0){ scanCount = null; }
    else{ scanCount = in.readInt(); }
    tripNo = in.readString();
    huNo = in.readString();
    ean = in.readString();
    rfid = in.readString();
    if(in.readByte() == 0){ eanQty = null; }
    else{ eanQty = in.readInt(); }
    epc = in.readString();
    isOriginal = in.readByte() != 0;
    status = in.readString();
    reason = in.readString();
    if(in.readByte() == 0){ attempts = null; }
    else{ attempts = in.readInt(); }
    deliveryNo = in.readString();
    isServerEntry = in.readByte() != 0;
    isRescan = in.readByte() != 0;
    isDuplicate = in.readByte() != 0;
    articleCode = in.readString();
    apparelBarcodeBased = in.readByte() != 0;
    fromVendor = in.readByte() != 0;
    isHuVerified = in.readByte() != 0;
    rssi = in.readString();
    isHardTag = in.readByte() != 0;
    serialNo = in.readString();
  }

  @Override
  public int describeContents(){
    return 0;
  }
  
  /**
   * To barcode json json object.
   *
   * @return the json object
   */
  public JSONObject toBarcodeJson(){
    JSONObject dataobject = new JSONObject();
    try{
      dataobject.put(ParamConstants.SKU_ID, epc);
      dataobject.put(ParamConstants.QTY, 1);
    }
    catch(JSONException e){ e.printStackTrace(); }
    return dataobject;
  }
  
  /**
   * To json json object.
   *
   * @return the json object
   */
  public JSONObject toJson(CommonActivity context){ return toJson(context,false);}
  public JSONObject toJson(CommonActivity context,final boolean isInwardWithSerialNumber){
    final boolean isReadOperation = false;
    JSONObject dataobject = new JSONObject();
    try{
      dataobject.put(ParamConstants.EPC, this.epc);
      dataobject.put(ParamConstants.TID, this.tid);
      dataobject.put(ParamConstants.ARTICLE, !articleCode.matches("(?i)(" + AppConstants.EXTRA_EAN + "|" + AppConstants.NON_ENCODED + ")") ? articleCode : "");
      dataobject.put(ParamConstants.K_TRIP_ARTICLE_CODE, !articleCode.matches("(?i)(" + AppConstants.EXTRA_EAN + "|" + AppConstants.NON_ENCODED + ")") ? articleCode : "");
      dataobject.put(ParamConstants.IS_HARD_TAG, this.isHardTag);
      if(isInwardWithSerialNumber) dataobject.put(ParamConstants.SERIAL_NUMBER, this.serialNo.toUpperCase());
      if(isNonEmpty(this.rssi)){
        JSONArray rssiarray = new JSONArray();
        JSONObject rssiObject = new JSONObject();
        rssiObject.put(ParamConstants.ANTENNA_NO, isReadOperation ? "1" : "");
        rssiObject.put(ParamConstants.RSSI, this.rssi);
        rssiObject.put(ParamConstants.PHASE, isReadOperation ? null : "");
        rssiarray.put(rssiObject);
        dataobject.put(ParamConstants.RSSI, rssiarray);
      }
      dataobject.put(ParamConstants.SKU_ID, chkNull(this.ean, context.epcEncoderDecoder.getBarcodeFromEPC(this.epc)));
      dataobject.put(ParamConstants.EAN, chkNull(this.ean, context.epcEncoderDecoder.getBarcodeFromEPC(this.epc)));
    }
    catch(JSONException e){ e.printStackTrace(); }
    return dataobject;
  }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    
    if(id == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(id);
    }
    if(huQty == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(huQty);
    }
    if(scanCount == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(scanCount);
    }
    dest.writeString(tripNo);
    dest.writeString(huNo);
    dest.writeString(ean);
    dest.writeString(rfid);
    if(eanQty == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(eanQty);
    }
    dest.writeString(epc);
    dest.writeByte((byte) (isOriginal ? 1 : 0));
    dest.writeString(status);
    dest.writeString(reason);
    if(attempts == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(attempts);
    }
    dest.writeString(deliveryNo);
    dest.writeByte((byte) (isServerEntry ? 1 : 0));
    dest.writeByte((byte) (isRescan ? 1 : 0));
    dest.writeByte((byte) (isDuplicate ? 1 : 0));
    dest.writeString(articleCode);
    dest.writeByte((byte) (apparelBarcodeBased ? 1 : 0));
    dest.writeByte((byte) (fromVendor ? 1 : 0));
    dest.writeByte((byte) (isHuVerified ? 1 : 0));
    dest.writeString(rssi);
    dest.writeByte((byte) (isHardTag ? 1 : 0));
    dest.writeString(serialNo);
  }
  
}
