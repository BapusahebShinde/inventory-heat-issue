package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Ignore;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.apis.ParamConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Type eans.
 */
public class TypeEans implements Serializable, Parcelable{
  
  public static final Creator<TypeEans> CREATOR = new Creator<TypeEans>(){
    @Override
    public TypeEans createFromParcel(Parcel in){
      return new TypeEans(in);
    }
    
    @Override
    public TypeEans[] newArray(int size){
      return new TypeEans[size];
    }
  };
  @SerializedName("label")
  public String label;
  @SerializedName("type")
  public String type;
  @SerializedName("eans")
  public List<String> eans;
  
  /**
   * Instantiates a new Ean qty.
   */
  public TypeEans(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Ean qty.
   *
   * @param type the type
   * @param eans the eans
   */
  @Ignore
  public TypeEans(String type, List<String> eans){
    this.type = type;
    this.eans = eans;
  }
  
  /**
   * Instantiates a new Ean qty.
   *
   * @param in the in
   */
  protected TypeEans(Parcel in){
    label = in.readString();
    type = in.readString();
    in.readStringList(eans);
  }
  
  @Override
  public int describeContents(){
    return 0;
  }
  
  public String getLabel(){
    return label;
  }
  
  public void setLabel(String label){
    this.label = label;
  }
  
  public String getType(){
    return chkNull(type,"");
  }
  
  public void setType(String type){
    this.type = type;
  }
  
  public List<String> getEans(){
    return (List<String>) chkNull(eans,new ArrayList<String>(0));
  }
  
  public void setEans(List<String> eans){
    this.eans = eans;
  }
  
  @Override
  public void writeToParcel(Parcel parcel, int i){
    parcel.writeString(label);
    parcel.writeString(type);
    parcel.writeStringList(eans);
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    TypeEans typeEans = (TypeEans) o;
    return Objects.equals(type, typeEans.type);
  }
  
  @Override
  public int hashCode(){
    return Objects.hash(type);
  }
  
  @Override
  public String toString(){
    return type;
  }
  
  public JSONObject toJson(){
    JSONObject dataObject = new JSONObject();
    try{
      dataObject.put(ParamConstants.OUTWARD_TOTE_TYPE, this.type);
      dataObject.put(ParamConstants.OUTWARD_TOTE_EANS, new JSONArray(this.eans));
    }
    catch(JSONException e){ e.printStackTrace(); }
    return dataObject;
  }
}
