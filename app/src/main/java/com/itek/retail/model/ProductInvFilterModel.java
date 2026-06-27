package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.isDCApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.lidroid.xutils.db.annotation.NotNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * The Product Inventory Filter model.
 */
@Entity(tableName = "product_inv_filter",indices = {@Index(value = {"ean","ref_ean"}, unique = true)})
public class ProductInvFilterModel implements Serializable, Parcelable{
  
  public static final Creator<ProductInvFilterModel> CREATOR = new Creator<ProductInvFilterModel>(){
    @Override
    public ProductInvFilterModel createFromParcel(Parcel in){
      return new ProductInvFilterModel(in);
    }
    
    @Override
    public ProductInvFilterModel[] newArray(int size){
      return new ProductInvFilterModel[size];
    }
  };

  public static ProductInvFilterModel productModelLabels=null;

  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "product_no")
  public Long productNo;
  @SerializedName(value = "productId", alternate = {"ProductId", "Productid", "productid", "productID", "product_id"})
  @ColumnInfo(name = "product_id")
  public String productId;
  @ColumnInfo(name = "ean")
  @SerializedName(value = "ean", alternate = {"EAN", "BarCode", "Barcode", "barCode", "barcode", "bar_code", "SkuId", "Skuid", "skuid", "skuId", "skuID", "sku_id", "isbn", "ISBN"})
  public String ean;
  @ColumnInfo(name = "ref_ean")
  @SerializedName(value = "refEan", alternate = {"RefEan", "refean", "ref_ean", "ReferenceEan", "referenceEan", "referenceean", "reference_ean", "RefId", "RefID", "refId", "refID", "refid", "ref_id", "ReferenceId", "ReferenceID", "referenceId", "referenceID", "referenceid", "reference_id", "RefBarCode", "refBarCode", "RefBarcode", "refBarcode", "refbarcode", "ref_bar_code", "ReferenceBarCode", "referenceBarCode", "ReferenceBarcode", "referenceBarcode", "referencebarcode", "reference_bar_code", "RefSkuId", "refSkuId", "RefSkuid", "refSkuid", "Refskuid", "refskuid", "RefskuId", "refskuId", "RefskuID", "refskuID", "ref_sku_id", "ReferenceSkuId", "referenceSkuId", "ReferenceSkuid", "referenceSkuid", "Referenceskuid", "referenceskuid", "ReferenceskuId", "referenceskuId", "ReferenceskuID", "referenceskuID", "reference_sku_id", "RefISBN", "refISBN", "RefIsbn", "refIsbn", "refisbn", "ref_isbn", "ReferenceISBN", "referenceISBN", "ReferenceIsbn", "referenceIsbn", "referenceisbn", "reference_isbn"})
  public String refEan;
  @NotNull
  @ColumnInfo(name = "session_type", defaultValue = "0")
  @SerializedName("session_type")
  public Integer sessionType;
  @ColumnInfo(name = "type")
  @SerializedName("type")
  public String type;
  @ColumnInfo(name = "category")
  @SerializedName(value = "category", alternate = "Category")
  public String category;
  @ColumnInfo(name = "sub_category")
  @SerializedName(value = "sub_category", alternate = {"subCategory","SubCategory","matkl","Matkl", "family", "Family", "familyName", "FamilyName", "familyname", "family_name"})
  public String subCategory;
  @ColumnInfo(name = "brand")
  @SerializedName(value = "brand", alternate = {"branddesc", "Brand", "brandDesc", "BrandDesc", "brand_desc", "brandName","brandname", "brand_name"})
  public String brand;
  @ColumnInfo(name = "article_no")
  @SerializedName(value = "articleNo", alternate = {"articleno", "article_no", "ArticleNo", "articleCode", "article_code", "ArticleCode", "article", "Article"})
  public String articleNo;
  @ColumnInfo(name = "batch_id")
  @SerializedName(value = "batchId", alternate = {"batch_id"})
  public String batchId;
  @ColumnInfo(name = "description")
  @SerializedName(value = "description", alternate = {"desc"})
  public String description;
  @ColumnInfo(name = "color")
  @SerializedName(value = "color", alternate = {"ColorName", "colorName","colorname", "color_name", "purity", "ColorCode", "colorCode", "color_code"})
  public String color;
  @ColumnInfo(name = "size")
  @SerializedName(value = "size", alternate = {"Size", "weight", "pages"})
  public String size;
  @ColumnInfo(name = "zone")
  @SerializedName(value = "zone", alternate = {"zoneName", "zone_name", "location", "locationName", "location_name", "srcZone", "src_zone", "srcZoneName", "src_zone_name", "srcLocation", "srcLocationName", "src_location", "src_location_name"})
  public String zone;
  @ColumnInfo(name = "zone_id", defaultValue = "0")
  @SerializedName(value = "zoneId", alternate = {"zone_id", "loc_id", "locId", "location_id", "locationId", "srcZoneId", "src_zone_id", "src_loc_id", "srcLocId", "src_location_id", "srcLocationId"})
  public String zoneId;
  @SerializedName(value = "qty",alternate = {"Qty","EanQty","eanQty","ean_qty"}) //temp change
  public Integer qty;
  @ColumnInfo(name = "found_qty", defaultValue = "0")
  @SerializedName(value = "foundQty", alternate = {"foundqty", "found_qty", "found"})
  public Integer foundQty = 0;

  
  /**
   * Instantiates a new Product model.
   */
  public ProductInvFilterModel(){/*Empty constructor*/}
  
  
  /**
   * Instantiates a new Product model.
   *
   * @param in the in
   */
  @Ignore
  protected ProductInvFilterModel(Parcel in){
    if(in.readByte() == 0){ productNo = null; }
    else{ productNo = in.readLong(); }
    productId = in.readString();
    ean = in.readString();
    refEan = in.readString();
    sessionType = in.readInt();
    type = in.readString();
    category = in.readString();
    subCategory = in.readString();
    brand = in.readString();
    articleNo = in.readString();
    batchId = in.readString();
    description = in.readString();
    color = in.readString();
    size = in.readString();
    zone = in.readString();
    zoneId = chkNull(in.readString(), "0");
    qty = in.readInt();
    foundQty = in.readInt();
  }
  
  /**
   * Get ean string.
   *
   * @return the string
   */
  public String getEan(){ return AppCommonMethods.getLeftZeroReplacedString(chkNull(ean, "").trim()); }
  
  public String getEanLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.ean)?productModelLabels.ean:SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean)).trim(); }


  public String getEanTxt(Context context){ return String.format(context.getString(R.string.txt__),getEanLbl(context),getEan()); }


  /**
   * Set ean.
   *
   * @param ean the ean
   */
  public void setEan(String ean){ this.ean = ean; }
  
  public String getRefEan(){
    return chkNull(refEan, "");
  }
  
  public void setRefEan(String refEan){
    this.refEan = refEan;
  }
  
  public String getSearchEan(){
    return SharedPrefManager.getBoolean(ParamConstants.IS_USE_REFERENCE_BARCODE, AppCommonMethods.isUseReferenceBarcode) ? chkNull(refEan, ean) : ean;
  }
  
  /**
   * Get session type int.
   *
   * @return the string
   */
  public Integer getSessionType(){ return chkNull(sessionType, AppCommonMethods.SessionType.OTHER.getValue()); }
  
  /**
   * Set session type.
   *
   * @param sessionType the session type
   */
  public void setSessionType(Integer sessionType){ this.sessionType = sessionType; }
  
  /**
   * Get type string.
   *
   * @return the string
   */
  public String getType(){ return chkNull(type, ""); }
  
  /**
   * Set type.
   *
   * @param type the type
   */
  public void setType(String type){ this.type = type; }
  
  /**
   * Get category string.
   *
   * @return the string
   */
  public String getCategory(){ return chkNull(category, AppConstants.DEFAULT_NO_VALUE).trim(); }

  public String getCategoryLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.category)?productModelLabels.category:SharedPrefManager.getString(ParamConstants.LABEL_CATEGORIES,context.getString(R.string.lbl_category)).trim(); }
  
  public String getCategoryTxt(Context context){ return String.format(context.getString(R.string.txt__),getCategoryLbl(context),getCategory()); }


  /**
   * Set category.
   *
   * @param category the category
   */
  public void setCategory(String category){ this.category = category; }
  
  /**
   * Get subcategory string.
   *
   * @return the string
   */
  public String getSubcategory(){ return chkNull(subCategory, "").trim(); }

  public String getSubCategoryLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.subCategory)?productModelLabels.subCategory:SharedPrefManager.getString(ParamConstants.LABEL_SUB_CATEGORY,context.getString(R.string.lbl_sub_category)).trim(); }
  
  public String getSubCategoryTxt(Context context){ return String.format(context.getString(R.string.txt__),getSubCategoryLbl(context),getSubcategory()); }


  /**
   * Set subcategory.
   *
   * @param subCategory the sub category
   */
  public void setSubcategory(String subCategory){ this.subCategory = subCategory; }
  
  /**
   * Get brand string.
   *
   * @return the string
   */
  public String getBrand(){ return chkNull(brand, AppConstants.DEFAULT_NO_VALUE).trim(); }
  
  public String getBrandLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.brand)?productModelLabels.brand:SharedPrefManager.getString(ParamConstants.LABEL_BRANDS,context.getString(R.string.lbl_brand)).trim(); }
  
  public String getBrandTxt(Context context){ return String.format(context.getString(R.string.txt__),getBrandLbl(context),getBrand()); }

  /**
   * Set brand.
   *
   * @param brand the brand
   */
  public void setBrand(String brand){ this.brand = brand; }
  
  public String getProductId(){
    return chkNull(productId,isDCApp?AppConstants.DEFAULT_NO_VALUE:"").trim();
  }
  
  public String getProductIdLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.productId)?productModelLabels.productId:SharedPrefManager.getString(ParamConstants.LABEL_PRODUCT_ID,context.getString(R.string.lbl_product_id)).trim(); }
  
  public String getProductIdTxt(Context context){ return String.format(context.getString(R.string.txt__),getProductIdLbl(context),getProductId()); }
  
  public void setProductId(String productId){
    this.productId = productId;
  }
  
  /**
   * Get article no string.
   *
   * @return the string
   */
  public String getArticleNo(){ return chkNull(articleNo, AppConstants.DEFAULT_NO_VALUE).trim(); }
  
  public String getArticleNoLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.articleNo)?productModelLabels.articleNo:SharedPrefManager.getString(ParamConstants.LABEL_ARTICLES,context.getString(R.string.lbl_article)).trim(); }
  
  public String getArticleNoTxt(Context context){ return String.format(context.getString(R.string.txt__),getArticleNoLbl(context),getArticleNo()); }

  /**
   * Set article no.
   *
   * @param articleNo the article no
   */
  public void setArticleNo(String articleNo){ this.articleNo = articleNo; }
  
  /**
   * Get batch id string.
   *
   * @return the string
   */
  public String getBatchId(){ return chkNull(batchId, "").trim(); }
  
  /**
   * Set batch id.
   *
   * @param batchId the batch id
   */
  public void setBatchId(String batchId){ this.batchId = batchId; }
  
  /**
   * Get description string.
   *
   * @return the string
   */
  public String getDescription(){ return chkNull(description, "").trim(); }

  public String getDescriptionLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.description)?productModelLabels.description:SharedPrefManager.getString(ParamConstants.LABEL_DESCRIPTION,context.getString(R.string.lbl_description)).trim(); }

  public String getDescriptionTxt(Context context){ return String.format(context.getString(R.string.txt__),getDescriptionLbl(context),getDescription()); }


  /**
   * Set description.
   *
   * @param description the description
   */
  public void setDescription(String description){ this.description = description; }
  
  /**
   * Get color string.
   *
   * @return the string
   */
  public String getColor(){ return chkNull(color, AppConstants.DEFAULT_NO_VALUE).trim(); }

  public String getColorLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.color)?productModelLabels.color:SharedPrefManager.getString(ParamConstants.LABEL_COLORS,context.getString(R.string.lbl_colors)).trim(); }


  public String getColorTxt(Context context){
    final boolean isHexColorCode = getColor().matches(AppConstants.REGEX_HEX_COLOR_CODE);
    return isHexColorCode?String.format(context.getString(R.string.txt_color_code), getColor()):String.format(context.getString(R.string.txt__),getColorLbl(context),getColor());
  }


  /**
   * Set color.
   *
   * @param color the color
   */
  public void setColor(String color){ this.color = color; }
  
  /**
   * Get size string.
   *
   * @return the string
   */
  public String getSize(){ return chkNull(size, AppConstants.DEFAULT_NO_VALUE).trim(); }

  public String getSizeLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.size)?productModelLabels.size:SharedPrefManager.getString(ParamConstants.LABEL_SIZES,context.getString(R.string.lbl_size)).trim(); }


  public String getSizeTxt(Context context){ return String.format(context.getString(R.string.txt__),getSizeLbl(context),getSize()); }


  /**
   * Set size.
   *
   * @param size the size
   */
  public void setSize(String size){ this.size = size; }
  
  /**
   * Get zone string.
   *
   * @return the string
   */
  public String getZone(){ return chkNull(zone, AppConstants.DEFAULT_NO_VALUE).trim(); }

  public String getZoneLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.zone)?productModelLabels.zone:SharedPrefManager.getString(ParamConstants.LABEL_ZONES,context.getString(R.string.lbl_location)).trim(); }


  public String getZoneTxt(Context context){ return String.format(context.getString(R.string.txt__),getZoneLbl(context),getZone()); }


  /**
   * Set zone.
   *
   * @param zone the zone
   */
  public void setZone(String zone){ this.zone = zone; }
  
  /**
   * Get zone id string.
   *
   * @return the string
   */
  public String getZoneId(){ return getZoneId(null); }
  
  /**
   * Set zone id.
   *
   * @param zoneId the zone id.
   */
  public void setZoneId(String zoneId){ this.zoneId = zoneId; }
  
  public String getZoneId(final Context context){ return chkNull(zoneId, context != null && isNonEmpty(zone) && !zone.equalsIgnoreCase(AppConstants.ALL) ? chkNull(AppDatabase.getZoneDao(context).getZoneIdByName(zone), "0") : "0"); }
  
  /**
   * Get qty string.
   *
   * @return the string
   */
  public String getQtyStr(){ return chkZero(qty, "0").trim(); }
  
  /**
   * Get qty int.
   *
   * @return the int
   */
  public int getQty(){ return AppCommonMethods.parseInt(chkZero(qty, "0").trim()); }
  
  /**
   * Set qty.
   *
   * @param qty the qty
   */
  public void setQty(Integer qty){ this.qty = qty; }
  
  /**
   * Get found qty int.
   *
   * @return the int
   */
  public int getFoundQty(){ return AppCommonMethods.parseInt(chkZero(foundQty, "0").trim()); }
  
  /**
   * Set found qty.
   *
   * @param foundQty the found qty
   */
  public void setFoundQty(Integer foundQty){ this.foundQty = foundQty; }
  
  /**
   * Get found qty string.
   *
   * @return the string
   */
  public String getFoundQtyStr(){ return chkZero(foundQty, "0").trim(); }
  
  /**
   * To barcode json json object.
   *
   * @return the json object
   */
  public JSONObject toBarcodeJson(){
    if(isNonEmpty(ean) && getFoundQty() > 0){
      JSONObject dataobject = new JSONObject();
      try{
        dataobject.put(ParamConstants.SKU_ID, getEan());
        dataobject.put(ParamConstants.QTY, getFoundQty());
      }
      catch(JSONException e){
        e.printStackTrace();
      }
      return dataobject;
    }
    return null;
  }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    ProductInvFilterModel that = (ProductInvFilterModel) o;
    if(productNo == null || that.productNo == null) return false;
    return productNo.equals(that.productNo);
  }
  
  @Override
  public int describeContents(){ return 0; }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    if(productNo == null){ dest.writeByte((byte) 0); }
    else{
      dest.writeByte((byte) 1);
      dest.writeLong(productNo);
    }
    dest.writeString(productId);
    dest.writeString(ean);
    dest.writeString(refEan);
    dest.writeInt(chkNull(sessionType, 0));
    dest.writeString(type);
    dest.writeString(category);
    dest.writeString(subCategory);
    dest.writeString(subCategory);
    dest.writeString(brand);
    dest.writeString(articleNo);
    dest.writeString(batchId);
    dest.writeString(description);
    dest.writeString(color);
    dest.writeString(size);
    dest.writeString(zone);
    dest.writeString(chkNull(zoneId, "0"));
    dest.writeInt(chkNull(qty, 0));
    dest.writeInt(chkNull(foundQty, 0));
  }
}
