package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Ignore;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Ean qty.
 */
public class DecodeType implements Serializable, Parcelable{
  
  public static final Creator<DecodeType> CREATOR = new Creator<DecodeType>(){
    @Override
    public DecodeType createFromParcel(Parcel in){
      return new DecodeType(in);
    }
    
    @Override
    public DecodeType[] newArray(int size){
      return new DecodeType[size];
    }
  };
  @SerializedName(value = "label",alternate = {"Label","lbl","Lbl","name","Name","title","Title","DispName","dispName","disp_name","DisplayName","Displayname","displayName","displayname","display_name"})
  public String label;
  @SerializedName(value = "type", alternate = {"Type","val","Val","value","Value","decType","DecType","Dectype","dectype","dec_type","decodeType","DecodeType","Decodetype","decodeype","decode_type","decodingType","DecodingType","Decodingtype","decodingtype","decoding_type"})
  public String type;
  
  /**
   * Instantiates a new Ean qty.
   */
  public DecodeType(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Ean qty.
   *
   * @param label the label
   * @param type  the type
   */
  @Ignore
  public DecodeType(String label, String type){
    this.label = label;
    this.type = type;
  }
  
  /**
   * Instantiates a new decode type.
   *
   * @param in the in
   */
  protected DecodeType(Parcel in){
    label = in.readString();
    type = in.readString();
  }
  
  @Override
  public int describeContents(){
    return 0;
  }
  
  public String getLabel(){
    return chkNull(label,"");
  }
  
  public void setLabel(String label){
    this.label = chkNull(label,"");
  }
  
  public String getType(){
    return chkNull(type,"");
  }
  
  public void setType(String type){
    this.type = chkNull(type,"");
  }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    parcel.writeString(label);
    parcel.writeString(type);
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    return this.type.equals(((DecodeType) o).type);
  }
  
  public JSONObject toJson(){
    JSONObject jsonObject = null;
    try{
      jsonObject = new JSONObject();
      jsonObject.put(ParamConstants.LABEL,label);
      jsonObject.put(ParamConstants.TYPE, type);
    }
    catch(Exception e){ e.printStackTrace(); }
    return jsonObject;
  }
  
  @Override
  public int hashCode(){
    return Objects.hash(type);
  }
  
  @Override
  public String toString(){
    return getLabel();
  }
}
