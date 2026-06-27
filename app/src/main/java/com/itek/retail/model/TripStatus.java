package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppConstants;
import com.lidroid.xutils.db.annotation.NotNull;

import java.io.Serializable;

/**
 * The Trip status.
 */
@Entity(tableName = "trip_status", indices = {@Index(value = {"trip_no", "type"}, unique = true)})
public class TripStatus implements Serializable, Parcelable{
  
  public static final Creator<TripStatus> CREATOR = new Creator<TripStatus>(){
    @Override
    public TripStatus createFromParcel(Parcel in){
      return new TripStatus(in);
    }
    
    @Override
    public TripStatus[] newArray(int size){
      return new TripStatus[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "pk_id")
  public Integer pkId;
  @NotNull
  @ColumnInfo(name = "trip_no", defaultValue = "")
  @SerializedName(value = "tripNum", alternate = {"TripNum", "tripnum", "trip_num", "TripNo", "tripNo", "tripno", "trip_no", "TripNumber", "tripNumber", "tripnumber", "trip_number"})
  public String tripNumber;
  @NotNull
  @ColumnInfo(name = "ref_trip_no", defaultValue = "")
  @SerializedName(value = "refTripNum", alternate = {"refTripNumber", "refTripNo", "refTrip", "refNo", "refNum", "refNumber", "referenceTripNumber", "referenceTripNum", "referenceTripNo", "referenceTrip", "referenceNo", "referenceNum", "referenceNumber"})
  public String refTripNumber;
  @SerializedName("whNum")
  @ColumnInfo(name = "wh_no", defaultValue = "")
  public String whNo;
  @SerializedName(value = "type", alternate = {"OperationType", "operationType", "operationtype", "operation_type", "OpType", "opType", "optype", "op_type", "TYPE", "Type"})
  @ColumnInfo(name = "type", defaultValue = "IN")
  public String type;
  @SerializedName(value = "huNums", alternate = {"huNumbers", "huCount", "totalHuCount", "totalHus", "totalHuNums", "totalHuNumbers", "totalCount", "total", "hus"})
  @ColumnInfo(name = "total_hu", defaultValue = "0")
  public int numberOfHu;
  @SerializedName("verifiedHus")
  @ColumnInfo(name = "verified_hu", defaultValue = "0")
  public int verifiedHu;
  @SerializedName(value = "completedHus", alternate = {"completedHuCount", "completedHuNums", "completedHuNumbers", "completeHuCount", "completeHuNums", "completeHuNumbers"})
  @ColumnInfo(name = "completed_hu", defaultValue = "0")
  public int completedHu;
  @ColumnInfo(name = "status", defaultValue = "Pending")
  public String status = "Pending";
  @SerializedName(value = "tripDateTime",alternate = {"TripDate","tripDate","trip_date","TripDateTime","trip_date_time","CreatedOn","createdOn","created_on","DateTime","dateTime","date_time","Date","date"})
  @ColumnInfo(name = "trip_date_time", defaultValue = "")
  public String tripDateTime;
  @SerializedName("tripType")
  @ColumnInfo(name = "trip_type", defaultValue = "dc_to_store")
  public String tripType;
  @SerializedName("lowerTolerance")
  @ColumnInfo(name = "lower_tolerance", defaultValue = "10")
  public int lowerTolerance;
  @SerializedName("upperTolerance")
  @ColumnInfo(name = "upper_tolerance", defaultValue = "5")
  public int upperTolerance;
  @SerializedName("restrictUnverifiedHu")
  @ColumnInfo(name = "process_compulsion", defaultValue = "false")
  public boolean huProcessCompulsion;
  @SerializedName("allowBarcodeScanning")
  @ColumnInfo(name = "manual_barcode_compulsion", defaultValue = "false")
  public boolean manualBarCodeCompulsion;
  @SerializedName("allowMixTagType")
  @ColumnInfo(name = "mix_tag_compulsion", defaultValue = "true")
  public boolean mixTagTypeCompulsion;
  @SerializedName("reason")
  @ColumnInfo(name = "reason", defaultValue = "")
  public String reason;
  @SerializedName(value = "excelTripType", alternate = {"ExcelTripType","excel_trip_type"})
  @ColumnInfo(name = "excel_trip_type", defaultValue = "Article")
  public String excelTripType="";
  @SerializedName(value = "isManualTrip", alternate = {"IsManualTrip","ismanualtrip","is_manual_trip","ManualTrip","manualTrip","manual_trip"})
  @ColumnInfo(name = "manual_trip", defaultValue = "false")
  public boolean isManualTrip = false;
  @SerializedName(value = "isUploaded", alternate = {"uploaded"})
  @ColumnInfo(name = "is_uploaded", defaultValue = "false")
  public boolean isUploaded = false;
  @SerializedName(value = "srcCode", alternate = {"SrcCode", "srccode", "src_code", "srcLocCode", "SrcLocCode", "srcloccode", "src_loc_code", "srcLocationCode", "SrcLocationCode", "srclocationcode", "src_location_code", "sourceLocCode", "SourceLocCode", "sourceloccode", "source_loc_code", "sourceLocationCode", "SourceLocationCode", "sourcelocationcode", "source_location_code", "Src", "src", "Source", "source"})
  @ColumnInfo(name = "src_loc_code", defaultValue = "")
  public String srcLocCode;
  @SerializedName(value = "srcType", alternate = {"SrcType", "srctype", "src_type", "srcLocType", "SrcLocType", "srcloctype", "src_loc_type", "srcLocationType", "SrcLocationType", "srclocationtype", "src_location_type", "sourceLocType", "SourceLocType", "sourceloctype", "source_loc_type", "sourceLocationType", "SourceLocationType", "sourcelocationtype", "source_location_type"})
  @ColumnInfo(name = "src_loc_type", defaultValue = "")
  public String srcLocType;
  @SerializedName(value = "destCode", alternate = {"DestCode", "destcode", "dest_code", "destLocCode", "DestLocCode", "destloccode", "dest_loc_code", "destLocationCode", "DestLocationCode", "destlocationcode", "dest_location_code", "destinationLocCode", "DestinationLocCode", "destinationloccode", "destination_loc_code", "destinationLocationCode", "DestinationLocationCode", "destinationlocationcode", "destination_location_code", "Dest", "dest", "Destination", "destination"})
  @ColumnInfo(name = "dest_loc_code", defaultValue = "")
  public String destLocCode;
  @SerializedName(value = "destType", alternate = {"DestType", "desttype", "dest_type", "destLocType", "DestLocType", "destloctype", "dest_loc_type", "destLocationType", "DestLocationType", "destlocationtype", "dest_location_type", "destinationLocType", "DestinationLocType", "destinationloctype", "destination_loc_type", "destinationLocationType", "DestinationLocationType", "destinationlocationtype", "destination_location_type"})
  @ColumnInfo(name = "dest_loc_type", defaultValue = "")
  public String destLocType;
  
  /**
   * Instantiates a new Trip status.
   */
  public TripStatus(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Trip status.
   *
   * @param in the in
   */
  protected TripStatus(Parcel in){
    tripNumber = in.readString();
    refTripNumber = in.readString();
    whNo = in.readString();
    type = in.readString();
    numberOfHu = in.readInt();
    verifiedHu = in.readInt();
    completedHu = in.readInt();
    status = in.readString();
    tripDateTime = in.readString();
    tripType = in.readString();
    lowerTolerance = in.readInt();
    upperTolerance = in.readInt();
    huProcessCompulsion = in.readByte() != 0;
    manualBarCodeCompulsion = in.readByte() != 0;
    mixTagTypeCompulsion = in.readByte() != 0;
    reason = in.readString();
    excelTripType = in.readString();
    isManualTrip = in.readByte() != 0;
    isUploaded = in.readByte() != 0;
    srcLocCode = in.readString();
    srcLocType = in.readString();
    destLocCode = in.readString();
    destLocType = in.readString();
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
  
  public String getRefTripNumber(){
    return chkNull(refTripNumber, "");
  }
  
  public void setRefTripNumber(String refTripNumber){
    this.refTripNumber = refTripNumber;
  }
  
  /**
   * Get number of hu int.
   *
   * @return the int
   */
  public int getNumberOfHu(){
    return chkNull(numberOfHu, 0);
  }
  
  /**
   * Set number of hu.
   *
   * @param numberOfHu the number of hu
   */
  public void setNumberOfHu(int numberOfHu){
    this.numberOfHu = numberOfHu;
  }
  
  /**
   * Get verified hu int.
   *
   * @return the int
   */
  public int getVerifiedHu(){
    return chkNull(verifiedHu, 0);
  }
  
  /**
   * Set verified hu.
   *
   * @param verifiedHu the verified hu
   */
  public void setVerifiedHu(int verifiedHu){
    this.verifiedHu = verifiedHu;
  }
  
  public int getCompletedHu(){
    return chkNull(completedHu, 0);
  }
  
  public void setCompletedHu(int completedHu){
    this.completedHu = completedHu;
  }
  
  /**
   * Get status string.
   *
   * @return the string
   */
  public String getStatus(){
    return chkNull(status, AppConstants.TRIP_STATUS_PENDING);
  }
  
  /**
   * Set processtype.
   *
   * @param status the status
   */
  public void setStatus(String status){
    this.status = status;
  }
  
  public String getTripDateTime(){
    return tripDateTime;
  }
  
  public void setTripDateTime(String tripDateTime){
    this.tripDateTime = tripDateTime;
  }
  
  /**
   * Get trip type string.
   *
   * @return the string
   */
  public String getTripType(){
    return chkNull(tripType, "");
  }
  
  /**
   * Set trip type.
   *
   * @param tripType the trip type
   */
  public void setTripType(String tripType){
    this.tripType = tripType;
  }
  
  /**
   * Get lower tolerance int.
   *
   * @return the int
   */
  public int getLowerTolerance(){
    return chkNull(lowerTolerance, 0);
  }
  
  /**
   * Set lower tolerance.
   *
   * @param lowerTolerance the lower tolerance
   */
  public void setLowerTolerance(int lowerTolerance){
    this.lowerTolerance = lowerTolerance;
  }
  
  /**
   * Get upper tolerance int.
   *
   * @return the int
   */
  public int getUpperTolerance(){
    return chkNull(upperTolerance, 0);
  }
  
  /**
   * Set upper tolerance.
   *
   * @param upperTolerance the upper tolerance
   */
  public void setUpperTolerance(int upperTolerance){
    this.upperTolerance = upperTolerance;
  }
  
  /**
   * Get wh no string.
   *
   * @return the string
   */
  public String getWhNo(){
    return chkNull(whNo, "");
  }
  
  /**
   * Set wh no.
   *
   * @param whNo the wh no
   */
  public void setWhNo(String whNo){
    this.whNo = whNo;
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
  
  /**
   * Get hu process compulsion boolean.
   *
   * @return the boolean
   */
  public boolean isHuProcessCompulsion(){
    return huProcessCompulsion;
  }
  
  /**
   * Set hu process compulsion.
   *
   * @param huProcessCompulsion the hu process compulsion
   */
  public void setHuProcessCompulsion(boolean huProcessCompulsion){ this.huProcessCompulsion = huProcessCompulsion; }
  
  /**
   * Is manual bar code compulsion boolean.
   *
   * @return the boolean
   */
  public boolean isManualBarCodeCompulsion(){ return manualBarCodeCompulsion; }
  
  /**
   * Set manual bar code compulsion.
   *
   * @param manualBarCodeCompulsion the manual bar code compulsion
   */
  public void setManualBarCodeCompulsion(boolean manualBarCodeCompulsion){ this.manualBarCodeCompulsion = manualBarCodeCompulsion; }
  
  /**
   * Is mix tag type compulsion boolean.
   *
   * @return the boolean
   */
  public boolean isMixTagTypeCompulsion(){ return mixTagTypeCompulsion; }
  
  /**
   * Set mix tag type compulsion.
   *
   * @param mixTagTypeCompulsion the mix tag type compulsion
   */
  public void setMixTagTypeCompulsion(boolean mixTagTypeCompulsion){ this.mixTagTypeCompulsion = mixTagTypeCompulsion; }
  
  public String getReason(){
    return chkNull(reason, "");
  }
  
  public void setReason(String reason){
    this.reason = reason;
  }
  
  public boolean isEanBasedTrip(){
    return getExcelTripType().equalsIgnoreCase(ParamConstants.EAN);
  }
  
  public boolean isArticleBasedTrip(){
    return !isManualTrip() && !isEanBasedTrip() && getExcelTripType().equalsIgnoreCase(ParamConstants.ARTICLE);
  }
  
  public String getExcelTripType(){
    return chkNull(excelTripType, "");
  }
  
  public void setExcelTripType(String excelTripType){
    this.excelTripType = excelTripType;
  }
  
  public boolean isManualTrip(){
    return isManualTrip;
  }
  
  public void setManualTrip(boolean manualTrip){
    isManualTrip = manualTrip;
  }
  
  public boolean isUploaded(){
    return isUploaded;
  }
  
  public void setUploaded(boolean uploaded){
    isUploaded = uploaded;
  }
  
  public String getSrcLocCode(){
    return chkNull(srcLocCode, "");
  }
  
  public void setSrcLocCode(String srcLocCode){
    this.srcLocCode = srcLocCode;
  }
  
  public String getSrcLocType(){
    return chkNull(srcLocType, "");
  }
  
  public void setSrcLocType(String srcLocType){
    this.srcLocType = srcLocType;
  }
  
  public String getDestLocCode(){
    return chkNull(destLocCode, "");
  }
  
  public void setDestLocCode(String destLocCode){
    this.destLocCode = destLocCode;
  }
  
  public String getDestLocType(){
    return chkNull(destLocType, "");
  }
  
  public void setDestLocType(String destLocType){
    this.destLocType = destLocType;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    TripStatus that = (TripStatus) o;
    return chkNull(tripNumber, refTripNumber).equals(chkNull(that.tripNumber, that.refTripNumber));
  }
  
  @Override
  public int describeContents(){ return 0; }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeString(tripNumber);
    dest.writeString(refTripNumber);
    dest.writeString(whNo);
    dest.writeString(type);
    dest.writeInt(chkNull(numberOfHu, 0));
    dest.writeInt(chkNull(verifiedHu, 0));
    dest.writeInt(chkNull(completedHu, 0));
    dest.writeString(status);
    dest.writeString(tripDateTime);
    dest.writeString(tripType);
    dest.writeInt(chkNull(lowerTolerance, 0));
    dest.writeInt(chkNull(upperTolerance, 0));
    dest.writeByte((byte) (huProcessCompulsion ? 1 : 0));
    dest.writeByte((byte) (manualBarCodeCompulsion ? 1 : 0));
    dest.writeByte((byte) (mixTagTypeCompulsion ? 1 : 0));
    dest.writeString(reason);
    dest.writeString(excelTripType);
    dest.writeByte((byte) (isManualTrip ? 1 : 0));
    dest.writeByte((byte) (isUploaded ? 1 : 0));
    dest.writeString(srcLocCode);
    dest.writeString(srcLocType);
    dest.writeString(destLocCode);
    dest.writeString(destLocType);
  }
}
