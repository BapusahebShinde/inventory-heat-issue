package com.itek.retail.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Pin implements Serializable, Parcelable{
  
  public static final Creator<Pin> CREATOR = new Creator<Pin>(){
    @Override
    public Pin createFromParcel(Parcel in){
      return new Pin(in);
    }
    
    @Override
    public Pin[] newArray(int size){
      return new Pin[size];
    }
  };
  private String pin;
  
  public Pin(String pin){
    this.pin = pin;
  }
  
  protected Pin(Parcel in){
    pin = in.readString();
  }
  
  public String getPin(){
    return pin;
  }
  
  public void setPin(String pin){
    this.pin = pin;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    Pin pin1 = (Pin) o;
    return pin.equalsIgnoreCase(pin1.pin);
  }
  
  @Override
  public int describeContents(){
    return 0;
  }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeString(pin);
  }
}
