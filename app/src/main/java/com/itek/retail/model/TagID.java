package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.lidroid.xutils.db.annotation.NotNull;

import java.io.Serializable;

/**
 * The Tag ID.
 */
@Entity(tableName = "tag_id", indices = {@Index(value = {"tid"}, unique = true)})
public class TagID implements Serializable, Parcelable{
  
  public static final Creator<TagID> CREATOR = new Creator<TagID>(){
    @Override
    public TagID createFromParcel(Parcel in){
      return new TagID(in);
    }
    
    @Override
    public TagID[] newArray(int size){
      return new TagID[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "pk_id")
  public Integer pkId;
  @SerializedName("tid")
  @ColumnInfo(name = "tid")
  @NotNull
  public String tid;
  @ColumnInfo(name = "is_found", defaultValue = "false")
  public boolean isFound = false;
  
  /**
   * Instantiates a new Category.
   */
  public TagID(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Tag Id.
   *
   * @param tid   the tag id
   */
  @Ignore
  public TagID(String tid){
    this.tid = tid;
  }
  
  /**
   * Instantiates a new Category.
   *
   * @param in the in
   */
  @Ignore
  protected TagID(Parcel in){
    tid = in.readString();
  }
  
  /**
   * Get tid string.
   *
   * @return the string
   */
  public String getTid(){ return chkNull(tid, ""); }
  
  /**
   * Set tid.
   *
   * @param tid the tid
   */
  public void setTid(String tid){ this.tid = tid; }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    TagID that = (TagID) o;
    return getTid().equalsIgnoreCase(that.getTid());
  }
  
  @Override
  public String toString(){ return getTid(); }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeString(tid);
  }
  
  @Override
  public int describeContents(){ return 0; }
}
