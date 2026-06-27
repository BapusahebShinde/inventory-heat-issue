package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isUse24LengthTIDForUpload;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.database.AppDatabase;

import org.json.JSONObject;

import java.io.Serializable;

@Entity(tableName = "fifo")
public class FIFOModel implements Serializable, Parcelable{
  
  public static final Creator<FIFOModel> CREATOR = new Creator<FIFOModel>(){
    @Override
    public FIFOModel createFromParcel(Parcel in){
      return new FIFOModel(in);
    }
    
    @Override
    public FIFOModel[] newArray(int size){
      return new FIFOModel[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "fifo_no")
  public Integer fifoNo;
  @ColumnInfo(name = "ean")
  @SerializedName(value = "ean", alternate = {"EAN", "Ean", "barcode", "barCode", "bar_code", "skuid", "skuId", "skuID", "sku_id", "isbn", "ISBN"})
  public String ean;
  @ColumnInfo(name = "epc")
  @SerializedName(value = "epc", alternate = {"Epc", "EPC", "rfid", "RFID", "rf_id", "rfId", "rfID"})
  public String epc;
  @ColumnInfo(name = "tid")
  @SerializedName(value = "tid", alternate = {"TID", "Tid"})
  public String tid;
  @ColumnInfo(name = "zone")
  @SerializedName(value = "zone", alternate = {"ZONE", "Zone", "ZONENAME", "ZoneName", "zoneName", "zonename", "ZONE_NAME", "Zone_Name", "zone_name", "location", "locationName", "location_name"})
  public String zone;
  @ColumnInfo(name = "zone_id", defaultValue = "0")
  @SerializedName(value = "zoneId", alternate = {"ZONEID", "ZoneId", "ZONE_ID", "zone_id", "loc_id", "locId", "location_id", "locationId"})
  public String zoneId;
  @ColumnInfo(name = "age")
  @SerializedName("age")
  public Integer age = -1;
  @ColumnInfo(name = "fifo_date")
  @SerializedName(value = "fifoDate", alternate = {"date", "fifo_date", "FIFO_DATE", "FIFODATE", "FifoDate"})
  public String fifoDate;
  @ColumnInfo(name = "from")
  @SerializedName(value = "from", alternate = {"FROM", "From"})
  public String from;
  @ColumnInfo(name = "is_found", defaultValue = "0")
  @SerializedName(value = "isFound", alternate = {"is_found"})
  public Boolean isFound = false;
  @ColumnInfo(name = "is_decoded", defaultValue = "0")
  @SerializedName(value = "isDecoded", alternate = {"is_decoded"})
  public Boolean isDecoded = false;
  public Integer totalQty = 0;
  public Integer foundQty = 0;
  public Integer decodedQty = 0;
  
  /**
   * Instantiates a new Ean qty.
   */
  public FIFOModel(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Ean qty.
   *
   * @param in the in
   */
  protected FIFOModel(Parcel in){
    ean = in.readString();
    epc = in.readString();
    tid = in.readString();
    fifoDate = in.readString();
    from = in.readString();
    zone = in.readString();
    zoneId = chkNull(in.readString(), "0");
    age = chkNull(in.readInt(),0);
    isFound = in.readByte() > 0;
    isDecoded = in.readByte() > 0;
    totalQty = chkNull(in.readInt(), 0);
    foundQty = chkNull(in.readInt(), 0);
    decodedQty = chkNull(in.readInt(), 0);
  }
  
  @Override
  public int describeContents(){
    return 0;
  }
  
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
   * Get epc string.
   *
   * @return the string
   */
  public String getEpc(){ return chkNull(epc, "").toUpperCase().trim(); }
  
  /**
   * Set epc.
   *
   * @param epc the epc
   */
  public void setEpc(String epc){ this.epc = epc; }
  
  /**
   * Get fifoDate string.
   *
   * @return the string
   */
  public String getFifoDate(){
    return fifoDate.replaceFirst("T", " ");//.split(" ")[0];
  }
  
  /**
   * Set fifo date.
   *
   * @param fifoDate the fifo date
   */
  public void setFifoDate(String fifoDate){
    this.fifoDate = fifoDate.replaceFirst("T", " ");//.split(" ")[0];
  }
  
  public String getFrom(){
    return from;
  }
  
  public void setFrom(String from){
    this.from = from;
  }
  
  /**
   * Get zone string.
   *
   * @return the string
   */
  public String getZone(){ return chkNull(zone, AppConstants.DEFAULT_NO_VALUE).trim(); }
  
  /**
   * Set zone.
   *
   * @param zone the zone
   */
  public void setZone(String zone){ this.zone = zone; }
  
  /**
   * Get zone id string.
   *
   * @return the string
   */
  public String getZoneId(){ return getZoneId(null); }
  
  /**
   * Set zone id.
   *
   * @param zoneId the zone id.
   */
  public void setZoneId(String zoneId){ this.zoneId = zoneId; }
  
  public String getZoneId(final Context context){ return chkNull(zoneId, context != null && isNonEmpty(zone) && !zone.equalsIgnoreCase(AppConstants.ALL) ? chkNull(AppDatabase.getZoneDao(context).getZoneIdByName(zone), "0") : "0"); }


  /**
   * Get age integer.
   *
   * @return the integer
   */
  public Integer getStockAge(){ return age; }

  /**
   * Set item age.
   *
   * @param age
   */
  public void setStockAge(Integer age){ this.age = age; }
  
  /**
   * Get found boolean.
   *
   * @return the boolean
   */
  public Boolean getFound(){ return chkNull(isFound, false); }
  
  /**
   * Set found.
   *
   * @param found the found
   */
  public void setFound(Boolean found){ isFound = found; }
  
  /**
   * Get decoded boolean.
   *
   * @return the boolean
   */
  public Boolean getDecoded(){ return chkNull(isDecoded, false); }
  
  /**
   * Set decoded.
   *
   * @param decoded the decoded
   */
  public void setDecoded(Boolean decoded){ isDecoded = decoded; }
  
  public Integer getTotalQty(){
    return totalQty;
  }
  
  public void setTotalQty(Integer totalQty){
    this.totalQty = totalQty;
  }
  
  public Integer getFoundQty(){
    return foundQty;
  }
  
  public void setFoundQty(Integer foundQty){
    this.foundQty = foundQty;
  }
  
  public Integer getDecodedQty(){
    return decodedQty;
  }
  
  public void setDecodedQty(Integer decodedQty){
    this.decodedQty = decodedQty;
  }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    parcel.writeString(ean);
    parcel.writeString(epc);
    parcel.writeString(tid);
    parcel.writeString(fifoDate);
    parcel.writeString(from);
    parcel.writeString(zone);
    parcel.writeString(chkNull(zoneId, "0"));
    parcel.writeInt(chkNull(age,0));
    parcel.writeByte((byte) (isFound == null && isFound ? 1 : 0));
    parcel.writeByte((byte) (isDecoded == null && isDecoded ? 1 : 0));
    parcel.writeInt(chkNull(totalQty, 0));
    parcel.writeInt(chkNull(foundQty, 0));
    parcel.writeInt(chkNull(decodedQty, 0));
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    return this.ean.equals(((FIFOModel) o).ean) && this.epc.equals(((FIFOModel) o).epc) && this.fifoDate.equals(((FIFOModel) o).fifoDate) && this.zoneId.equals(((FIFOModel) o).zoneId);
  }
  
  public JSONObject toJson(){
    JSONObject jsonObject = null;
    try{
      jsonObject = new JSONObject();
      jsonObject.put(ParamConstants.EAN, ean);
      jsonObject.put(ParamConstants.EPC, epc);
      if(isUse24LengthTIDForUpload && this.tid.length() > 24){
        jsonObject.put(ParamConstants.TID, this.tid.substring(0, 24));
      }
      else{
        jsonObject.put(ParamConstants.TID, this.tid);
      }
      jsonObject.put(ParamConstants.FIFO_DATE, fifoDate);
      jsonObject.put(ParamConstants.FROM, from);
      jsonObject.put(ParamConstants.ZONE, zone);
      jsonObject.put(ParamConstants.ZONE_ID, zoneId);
      jsonObject.put(ParamConstants.AGE, age);
    }
    catch(Exception e){ e.printStackTrace(); }
    return jsonObject;
  }
  
}
