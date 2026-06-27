package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OutwardTypes implements Serializable, Parcelable{
  
  @SerializedName(value = "typeId",alternate = {"TypeId","typeMasterId","TypeMasterId","outwardTypeMasterId","OutwardTypeMasterId","storeOutwardTypeMasterId","StoreOutwardTypeMasterId"})
  public Long typeId;
  @SerializedName(value = "name",alternate = {"Name","type","Type","typeName","TypeName","outwardType","OutwardType","outwardTypeName","OutwardTypeName"})
  public String name;
  @SerializedName(value = "remark",alternate = {"reason","Remark"})
  public String remark;
  
  public OutwardTypes(Long typeId, String name){
    this.typeId = typeId;
    this.name = name;
  }
  
  protected OutwardTypes(Parcel in){
    typeId = in.readLong();
    name = in.readString();
    remark = in.readString();
  }
  
  public static final Creator<OutwardTypes> CREATOR = new Creator<OutwardTypes>(){
    @Override
    public OutwardTypes createFromParcel(Parcel in){
      return new OutwardTypes(in);
    }
    
    @Override
    public OutwardTypes[] newArray(int size){
      return new OutwardTypes[size];
    }
  };
  
  public Long getTypeId(){
    return chkNull(typeId,0l);
  }
  
  public void setTypeId(Long typeId){
    this.typeId = typeId;
  }
  
  public String getName(){
    return chkNull(name,"").trim();
  }
  
  public void setName(String name){
    this.name = name;
  }
  
  public String getRemark(){
    return chkNull(remark,"");
  }
  
  public void setRemark(String remark){
    this.remark = remark;
  }
  
  /**
   * @return
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
    dest.writeLong(typeId);
    dest.writeString(name);
    dest.writeString(remark);
  }
  
  @Override
  public String toString(){
    return name;
  }
}
