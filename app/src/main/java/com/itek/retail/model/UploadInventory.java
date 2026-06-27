package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isUse24LengthTIDForUpload;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.sgtin.EPCEncoderDecoder;
import com.lidroid.xutils.db.annotation.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * The Inventory.
 */

@Entity(tableName = "upload_inventory", indices = {@Index(value = {"epc", "new_epc", "session_id"}, unique = true)})

public class UploadInventory implements Serializable, Parcelable{
  
  public static final Creator<UploadInventory> CREATOR = new Creator<UploadInventory>(){
    @Override
    public UploadInventory createFromParcel(Parcel in){
      return new UploadInventory(in);
    }
    
    @Override
    public UploadInventory[] newArray(int size){
      return new UploadInventory[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  public Integer uino;
  @ColumnInfo(name = "session_id")
  @NotNull
  public String sessionId;
  @ColumnInfo(name = "session_type")
  @NotNull
  public int sessionType;
  @ColumnInfo(name = "session_action")
  @NotNull
  public int sessionAction;
  @ColumnInfo(name = "session_user_id")
  @NotNull
  public String sessionUserId;
  @ColumnInfo(name = "epc", defaultValue = "")
  @NotNull
  public String epc;
  @ColumnInfo(name = "ean", defaultValue = "")
  @NotNull
  public String ean;
  @ColumnInfo(name = "zone", defaultValue = "")
  @NotNull
  public String zone;
  @ColumnInfo(name = "zone_id", defaultValue = "0")
  @NotNull
  public String zoneId;
  @ColumnInfo(name = "tag_type", defaultValue = "0")//1-Standard 2-Non Standard 3-unencoded
  public int tagtype;
  @ColumnInfo(name = "tid")
  public String tid;
  @ColumnInfo(name = "rssi")
  public String rssi;
  @ColumnInfo(name = "is_hard_tag", defaultValue = "true")
  //public boolean isHardTag;
  public Boolean isHardTag;
  @ColumnInfo(name = "pc_data")
  public String pcdata;
  @ColumnInfo(name = "new_epc", defaultValue = "")
  @NotNull
  public String newEpc = "";
  @ColumnInfo(name = "is_uploaded", defaultValue = "false")
  public boolean isUploaded = false;
  @ColumnInfo(name = "upload_retry_count", defaultValue = "0")
  public int retryUploadCount;
  @ColumnInfo(name = "insert_time", defaultValue = "")
  @NotNull
  public String insertTime;
  @ColumnInfo(name = "is_found", defaultValue = "false")
  public boolean isFound = false;
  @ColumnInfo(name = "fifo_date")
  @SerializedName(value = "fifoDate", alternate = {"date", "fifo_date"})
  public String fifoDate;
  @ColumnInfo(name = "extras", defaultValue = "")
  public String extras;
  @ColumnInfo(name = "enc_verify_status", defaultValue = "false")
  public Integer encVerifyStatus;
  @ColumnInfo(name = "remark")
  public String remark;
  
  /**
   * Instantiates a new Upload Inventory.
   */
  public UploadInventory(){/*Empty Constructor*/}
  
  /**
   * Instantiates a new Upload Inventory.
   *
   * @param inventory the inventory
   * @param extras    the extras
   */
  @Ignore
  public UploadInventory(final Inventory inventory, final String extras){
    this.sessionId = inventory.sessionId;
    this.sessionType = inventory.sessionType;
    this.sessionAction = inventory.sessionAction;
    this.sessionUserId = SharedPrefManager.getUserID();
    this.epc = inventory.epc;
    this.tagtype = inventory.tagtype;
    this.isHardTag = inventory.isHardTag;
    this.ean = inventory.ean;
    this.tid = inventory.tid;
    this.rssi = inventory.rssi;
    this.pcdata = inventory.pcdata;
    this.newEpc = inventory.newEpc;
    this.zone = inventory.zone;
    this.zoneId = inventory.zoneId;
    this.isUploaded = inventory.isUploaded;
    this.retryUploadCount = inventory.retryUploadCount;
    this.insertTime = inventory.insertTime;
    this.isFound=inventory.isFound;
    this.fifoDate = inventory.fifoDate;
    this.encVerifyStatus = inventory.encVerifyStatus;
    this.remark = inventory.remark;
    this.extras = extras;
  }
  
  /**
   * Instantiates a new Inventory.
   *
   * @param in the in
   */
  protected UploadInventory(Parcel in){
    if(in.readByte() == 0){ uino = null; }
    else{ uino = in.readInt(); }
    sessionId = in.readString();
    sessionType = in.readInt();
    sessionAction = in.readInt();
    sessionUserId = in.readString();
    epc = in.readString();
    tagtype = in.readInt();
    isHardTag = in.readByte() != 0;
    tid = in.readString();
    rssi = in.readString();
    pcdata = in.readString();
    newEpc = in.readString();
    isUploaded = in.readByte() != 0;
    retryUploadCount = in.readInt();
    insertTime = in.readString();
    isFound = in.readByte()!=0;
    fifoDate = in.readString();
    encVerifyStatus = in.readInt();
    remark = in.readString();
    extras = in.readString();
  }
  
  public boolean isDecoded(){
    return chkNull(this.newEpc, this.epc).startsWith("0");
  }
  
  @Override
  public int describeContents(){
    return 0;
  }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    if(uino == null){ parcel.writeByte((byte) 0); }
    else{
      parcel.writeByte((byte) 1);
      parcel.writeInt(uino);
    }
    parcel.writeString(sessionId);
    parcel.writeInt(chkNull(sessionType, 0));
    parcel.writeInt(chkNull(sessionAction, 0));
    parcel.writeString(sessionUserId);
    parcel.writeString(epc);
    parcel.writeInt(chkNull(tagtype, 0));
    parcel.writeByte((byte) (isHardTag ? 1 : 0));
    parcel.writeString(tid);
    parcel.writeString(rssi);
    parcel.writeString(pcdata);
    parcel.writeString(newEpc);
    parcel.writeByte((byte) (isUploaded ? 1 : 0));
    parcel.writeInt(retryUploadCount);
    parcel.writeString(insertTime);
    parcel.writeByte((byte) (isFound?1:0));
    parcel.writeString(fifoDate);
    parcel.writeInt(encVerifyStatus);
    parcel.writeString(remark);
    parcel.writeString(extras);
  }
  
  /**
   * To json json object.
   *
   * @return the json object
   */
  public JSONObject toJson(){
    final boolean isReadOperation = sessionType > 0 && sessionType != 3;
    JSONObject dataObject = null;
    try{
      JSONObject data = new JSONObject(extras.toString());
      if(sessionType == AppCommonMethods.SessionType.ENCODING_THAN.getValue() && data!=null && data.has(ParamConstants.ARTICLE_NO) && data.has(ParamConstants.LENGTH_ORIGINAL)){
        if(dataObject == null) dataObject=new JSONObject();
        dataObject.put(ParamConstants.ARTICLE_NO,data.get(ParamConstants.ARTICLE_NO));
        dataObject.put(ParamConstants.LENGTH_ORIGINAL,data.get(ParamConstants.LENGTH_ORIGINAL));
      }
    }catch(Exception e){
      e.printStackTrace();
    }
    
    if(dataObject == null) dataObject = new JSONObject();
    try{
      dataObject.put(ParamConstants.SESSION_USER_ID, sessionUserId);
      if(sessionType == AppCommonMethods.SessionType.OMNICHANNEL.getValue()){
        dataObject.put(ParamConstants.IS_DECODED, isDecoded());
        dataObject.put(ParamConstants.PRE_EPC, this.epc);
        dataObject.put(ParamConstants.EPC, chkNull(this.newEpc, this.epc));
      }
      else if(isNonEmpty(this.newEpc)){
        dataObject.put(ParamConstants.PRE_EPC, this.epc);
        dataObject.put(ParamConstants.EPC, this.newEpc);
        if(SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY))
         dataObject.put(ParamConstants.IS_STATUS_VERIFIED,this.isFound);
         if(encVerifyStatus!=null && encVerifyStatus>0 && AppCommonMethods.EncodeVerifyStatus.get(encVerifyStatus)!=null)
          dataObject.put(ParamConstants.STATUS,AppCommonMethods.EncodeVerifyStatus.get(this.encVerifyStatus).name());
      }
      else dataObject.put(ParamConstants.EPC, this.epc);
      
      if(isUse24LengthTIDForUpload && this.tid.length() > 24)
        dataObject.put(ParamConstants.TID, this.tid.substring(0, 24));
      else dataObject.put(ParamConstants.TID, this.tid);
      dataObject.put(ParamConstants.TAG_TYPE, AppCommonMethods.TagType.get(this.tagtype).name());
      dataObject.put(ParamConstants.IS_HARD_TAG, this.isHardTag);
      if((sessionType == AppCommonMethods.SessionType.ENCODING.getValue() || sessionType == AppCommonMethods.SessionType.ENCODING_THAN.getValue() || sessionType == AppCommonMethods.SessionType.DECODING.getValue() || sessionType == AppCommonMethods.SessionType.OMNICHANNEL.getValue()) && isNonEmpty(this.insertTime))
        dataObject.put(ParamConstants.TRANSACTION_DATE, this.insertTime);
      if(isNonEmpty(this.rssi)){
        JSONArray rssiarray = new JSONArray();
        JSONObject rssiObject = new JSONObject();
        rssiObject.put(ParamConstants.ANTENNA_NO, isReadOperation ? "1" : "");
        rssiObject.put(ParamConstants.RSSI, this.rssi);
        rssiObject.put(ParamConstants.PHASE, isReadOperation ? null : "");
        rssiarray.put(rssiObject);
        dataObject.put(ParamConstants.RSSI, rssiarray);
      }
      dataObject.put(ParamConstants.SKU_ID, chkNull(this.ean, EPCEncoderDecoder.getInstance().getBarcodeFromEPC(chkNull(this.newEpc, this.epc))));
      dataObject.put(ParamConstants.ZONE, chkNull(this.zone, ""));
      if(isNonEmpty(this.zone) && !chkNull(this.zoneId, "0").equalsIgnoreCase("0"))
        dataObject.put(ParamConstants.ZONE_ID, chkNull(this.zoneId, "0"));
      if(isNonEmpty(this.newEpc) && sessionType == AppCommonMethods.SessionType.ENCODING.getValue() ||
        sessionType == AppCommonMethods.SessionType.ENCODING_THAN.getValue()||
        sessionType == AppCommonMethods.SessionType.DECODING.getValue() || sessionType == AppCommonMethods.SessionType.OMNICHANNEL.getValue()){
        dataObject.put(ParamConstants.IS_UPLOADED, chkNull(retryUploadCount, 0));
        dataObject.put(ParamConstants.UPLOAD_RETRY_COUNT, chkNull(retryUploadCount, 0));
      }
      if(isNonEmpty(this.newEpc) && (sessionType == AppCommonMethods.SessionType.ENCODING.getValue() || sessionType == AppCommonMethods.SessionType.ENCODING_THAN.getValue()) && isNonEmpty(fifoDate) && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_FIFO_DATE_FOR_ENCODING))
        dataObject.put(ParamConstants.FIFO_DATE, fifoDate);
      if(isNonEmpty(this.remark))
        dataObject.put(ParamConstants.REASON, remark);
    }
    catch(JSONException e){ e.printStackTrace(); }
    return dataObject;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    UploadInventory inventory = (UploadInventory) o;
    return sessionType == inventory.sessionType && sessionAction == inventory.sessionAction && sessionId.equals(inventory.sessionId) && epc.equals(inventory.epc);
  }
}
