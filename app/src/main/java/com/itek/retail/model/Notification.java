package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.R;

import java.io.Serializable;

/**
 * The Notification.
 */
@Entity(tableName = "notifications")
public class Notification implements Serializable, Parcelable{
  
  public static final Creator<Notification> CREATOR = new Creator<Notification>(){
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public Notification createFromParcel(Parcel in){
      return new Notification(in);
    }
    
    @Override
    public Notification[] newArray(int size){
      return new Notification[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "notification_no")
  public Integer notificationNo;
  @SerializedName("imageId")
  public Integer itemImgId;
  @SerializedName("imageUrl")
  @ColumnInfo(name = "img_url")
  public String itemImgURL;
  @SerializedName("userId")
  @ColumnInfo(name = "user_id")
  public String userId;
  @SerializedName("title")
  @ColumnInfo(name = "title")
  public String title;
  @SerializedName("message")
  @ColumnInfo(name = "message")
  public String message;
  @SerializedName("date")
  @ColumnInfo(name = "date")
  public String date;
  @SerializedName("validTill1")
  @ColumnInfo(name = "valid_till")
  public String validTill;
  @SerializedName("receivedOn")
  @ColumnInfo(name = "received_on")
  public String receivedOn;
  @SerializedName("type")
  @ColumnInfo(name = "type")
  public String type;
  @SerializedName("typeId")
  @ColumnInfo(name = "typeId")
  public String typeId;
  @SerializedName("isRead")
  @ColumnInfo(name = "is_read", defaultValue = "false")
  public boolean isRead = false;
  @SerializedName("brand")
  public String brand;
  @SerializedName("qty")
  public Integer qty;
  
  /**
   * Instantiates a new Notification.
   */
  public Notification(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Notification.
   *
   * @param in the in
   */
  @RequiresApi(api = Build.VERSION_CODES.Q)
  protected Notification(Parcel in){
    itemImgId = in.readInt();
    itemImgURL = in.readString();
    userId = in.readString();
    title = in.readString();
    message = in.readString();
    date = in.readString();
    validTill = in.readString();
    receivedOn = in.readString();
    type = in.readString();
    typeId = in.readString();
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) isRead = in.readBoolean();
    else isRead = in.readInt() > 0;
    brand = in.readString();
    qty = in.readInt();
  }
  
  /**
   * Get item img id integer.
   *
   * @return the integer
   */
  public Integer getItemImgId(){ return chkNull(itemImgId, R.drawable.ic_app_logo); }
  
  /**
   * Set item img id.
   *
   * @param itemImgId the item img id
   */
  public void setItemImgId(Integer itemImgId){ this.itemImgId = itemImgId; }
  
  /**
   * Get item img url string.
   *
   * @return the string
   */
  public String getItemImgURL(){ return chkNull(itemImgURL, ""); }
  
  /**
   * Set item img url.
   *
   * @param itemImgURL the item img url
   */
  public void setItemImgURL(String itemImgURL){ this.itemImgURL = itemImgURL; }
  
  /**
   * Get menu icon name string.
   *
   * @return the string
   */
  public String getMenuIconName(){
    if(isNonEmpty(typeId)){
      return "ic_" + typeId.toLowerCase().replaceAll(" ", "_").replaceAll("_start", "");
    }
    switch(getType().toUpperCase()){
      case "ENCODING":
        return "ic_enc";
      case "INVENTORY":
        return "ic_inv";
      case "STOCK_CORRECTION":
        return "ic_stock_correct";
      case "SEARCH":
        return "ic_ser";
      case "OMNISEARCH":
        return "ic_ser_omni";
      case "MOVEMENT":
        return "ic_mov";
      case "REPLENISHMENT":
        return "ic_replenish";
      case "INWARD":
        return "ic_inw";
      case "OUTWARD":
        return "ic_otw";
      default:
        return "";
    }
  }
  
  /**
   * Get user id string.
   *
   * @return the string
   */
  public String getUserId(){ return chkNull(userId, ""); }
  
  /**
   * Set user id.
   *
   * @param userId the user id
   */
  public void setUserId(String userId){ this.userId = userId; }
  
  /**
   * Get title string.
   *
   * @return the string
   */
  public String getTitle(){ return chkNull(title, ""); }
  
  /**
   * Set title.
   *
   * @param title the title
   */
  public void setTitle(String title){ this.title = title; }
  
  /**
   * Get message string.
   *
   * @return the string
   */
  public String getMessage(){ return message; }
  
  /**
   * Set message.
   *
   * @param message the message
   */
  public void setMessage(String message){ this.message = message; }
  
  /**
   * Get date string.
   *
   * @return the string
   */
  public String getDate(){ return date; }
  
  /**
   * Set date.
   *
   * @param date the date
   */
  public void setDate(String date){ this.date = date; }
  
  /**
   * Get valid till string.
   *
   * @return the string
   */
  public String getValidTill(){ return validTill; }
  
  /**
   * Set valid till.
   *
   * @param validTill the valid till
   */
  public void setValidTill(String validTill){ this.validTill = validTill; }
  
  /**
   * Get received on string.
   *
   * @return the string
   */
  public String getReceivedOn(){ return receivedOn; }
  
  /**
   * Set received on.
   *
   * @param receivedOn the received on
   */
  public void setReceivedOn(String receivedOn){ this.receivedOn = receivedOn; }
  
  /**
   * Get type string.
   *
   * @return the string
   */
  public String getType(){ return type; }
  
  /**
   * Set type.
   *
   * @param type the type
   */
  public void setType(String type){ this.type = type; }
  
  /**
   * Get type id string.
   *
   * @return the string
   */
  public String getTypeId(){ return typeId; }
  
  /**
   * Set type id.
   *
   * @param typeId the type id
   */
  public void setTypeId(String typeId){ this.typeId = typeId; }
  
  /**
   * Is read boolean.
   *
   * @return the boolean
   */
  public boolean isRead(){ return isRead; }
  
  /**
   * Set read.
   *
   * @param read the read
   */
  public void setRead(boolean read){ isRead = read; }
  
  /**
   * Get brand string.
   *
   * @return the string
   */
  public String getBrand(){ return brand; }
  
  /**
   * Set brand.
   *
   * @param brand the brand
   */
  public void setBrand(String brand){ this.brand = brand; }
  
  /**
   * Get qty integer.
   *
   * @return the integer
   */
  public Integer getQty(){ return qty; }
  
  /**
   * Set qty.
   *
   * @param qty the qty
   */
  public void setQty(Integer qty){ this.qty = qty; }
  
  @Override
  public int describeContents(){ return 0; }
  
  @RequiresApi(api = Build.VERSION_CODES.Q)
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeInt(chkNull(itemImgId, 0));
    dest.writeString(itemImgURL);
    dest.writeString(userId);
    dest.writeString(title);
    dest.writeString(message);
    dest.writeString(date);
    dest.writeString(validTill);
    dest.writeString(receivedOn);
    dest.writeString(type);
    dest.writeString(typeId);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) dest.writeBoolean(isRead);
    else dest.writeInt(isRead ? 1 : 0);
    dest.writeString(brand);
    dest.writeInt(chkNull(qty, 0));
  }
}
