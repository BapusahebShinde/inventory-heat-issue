package com.itek.retail.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * The List model.
 */
@Entity(tableName = "list")
public class ListModel implements Serializable, Parcelable{
  
  public static final Creator<ListModel> CREATOR = new Creator<ListModel>(){
    @Override
    public ListModel createFromParcel(Parcel in){
      return new ListModel(in);
    }
    
    @Override
    public ListModel[] newArray(int size){
      return new ListModel[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "list_no")
  public Integer listNo;
  @SerializedName(value = "listId", alternate = {"list_id", "listName", "list_name", "code", "orderNo", "order_no", "name", "ean"})
  @ColumnInfo(name = "list_id")
  public String listId;
  @SerializedName(value = "listType", alternate = {"list_type", "type"})
  @ColumnInfo(name = "list_type")
  public String listType;
  @SerializedName(value = "qty", alternate = {"listQty", "list_qty", "orderQty", "order_qty"})
  @ColumnInfo(name = "qty")
  public Integer qty = 0;
  @SerializedName("priority")
  @ColumnInfo(name = "priority")
  public String priority;
  @SerializedName(value = "listSubType", alternate = {"list_sub_type", "subType", "sub_type"})
  @ColumnInfo(name = "sub_type")
  public String subType;
  @SerializedName(value = "ageingLabel", alternate = {"ageing_label"})
  @ColumnInfo(name = "ageing_label")
  public String ageingLabel;
  @SerializedName(value = "ageingHrs", alternate = {"ageing_hrs", "ageingHours", "ageing_hours"})
  @ColumnInfo(name = "ageing_hrs")
  public Integer ageingHrs;
  
  /**
   * Instantiates a new List model.
   */
  public ListModel(){/*Empty Constructor*/}
  
  protected ListModel(Parcel in){
    if(in.readByte() == 0){
      ;
      listNo = null;
    }
    else{ listNo = in.readInt(); }
    listId = in.readString();
    listType = in.readString();
    qty = in.readInt();
    priority = in.readString();
    subType = in.readString();
    ageingLabel = in.readString();
    if(in.readByte() == 0){ ageingHrs = null; }
    else{ ageingHrs = in.readInt(); }
  }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    if(listNo == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(listNo);
    }
    dest.writeString(listId);
    dest.writeString(listType);
    dest.writeInt(qty);
    dest.writeString(priority);
    dest.writeString(subType);
    dest.writeString(ageingLabel);
    if(ageingHrs == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeInt(ageingHrs);
    }
  }
  
  @Override
  public int describeContents(){
    return 0;
  }
  
}
