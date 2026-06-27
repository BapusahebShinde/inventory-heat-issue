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
 * The Category.
 */
@Entity(tableName = "categories", indices = {@Index(value = {"category_name"}, unique = true)})
public class Category implements Serializable, Parcelable{
  
  public static final Creator<Category> CREATOR = new Creator<Category>(){
    @Override
    public Category createFromParcel(Parcel in){
      return new Category(in);
    }
    
    @Override
    public Category[] newArray(int size){
      return new Category[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "category_no")
  public Integer categoryNo;
  @SerializedName("categoryId")
  @ColumnInfo(name = "category_id")
  @NotNull
  public String categoryId;
  @SerializedName("categoryName")
  @ColumnInfo(name = "category_name")
  @NotNull
  public String categoryName;
  @SerializedName("categoryType")
  @ColumnInfo(name = "category_type")
  public String categoryType;
  
  /**
   * Instantiates a new Category.
   */
  public Category(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Category.
   *
   * @param categoryId   the category id
   * @param categoryName the category name
   */
  @Ignore
  public Category(String categoryId, String categoryName){
    this.categoryId = categoryId;
    this.categoryName = categoryName;
    this.categoryType = categoryName;
  }
  
  /**
   * Instantiates a new Category.
   *
   * @param in the in
   */
  @Ignore
  protected Category(Parcel in){
    categoryId = in.readString();
    categoryName = in.readString();
    categoryType = in.readString();
  }
  
  /**
   * Get category id string.
   *
   * @return the string
   */
  public String getCategoryId(){ return chkNull(categoryId, "0"); }
  
  /**
   * Set category id.
   *
   * @param categoryId the category id
   */
  public void setCategoryId(String categoryId){ this.categoryId = categoryId; }
  
  /**
   * Get category name string.
   *
   * @return the string
   */
  public String getCategoryName(){ return chkNull(categoryName, "").trim(); }
  
  /**
   * Set category name.
   *
   * @param categoryName the category name
   */
  public void setCategoryName(String categoryName){ this.categoryName = categoryName.trim(); }
  
  /**
   * Get category type string.
   *
   * @return the string
   */
  public String getCategoryType(){ return chkNull(categoryType, ""); }
  
  /**
   * Set category type.
   *
   * @param categoryType the category type
   */
  public void setCategoryType(String categoryType){ this.categoryType = categoryType; }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    Category that = (Category) o;
    return getCategoryName().equalsIgnoreCase(that.getCategoryName());
  }
  
  @Override
  public String toString(){ return getCategoryName(); }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeString(categoryId);
    dest.writeString(categoryName);
    dest.writeString(categoryType);
  }
  
  @Override
  public int describeContents(){ return 0; }
}
