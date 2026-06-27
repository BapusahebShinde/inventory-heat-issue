package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.formatDoubleStr2Decimals;
import static com.itek.retail.common.AppCommonMethods.isDCApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.lidroid.xutils.db.annotation.NotNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The Product model.
 */
@Entity(tableName = "product")//,indices = {@Index(value = {"epc"}, unique = true)})
public class ProductModel implements Serializable, Parcelable{
  
  public static final Creator<ProductModel> CREATOR = new Creator<ProductModel>(){
    @Override
    public ProductModel createFromParcel(Parcel in){
      return new ProductModel(in);
    }
    
    @Override
    public ProductModel[] newArray(int size){
      return new ProductModel[size];
    }
  };

  public static ProductModel productModelLabels=null;

  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "product_no")
  public Long productNo;
  @SerializedName(value = "productId", alternate = {"ProductId", "Productid", "productid", "productID", "product_id"})
  @ColumnInfo(name = "product_id")
  public String productId;
  @ColumnInfo(name = "image_urls")
  @SerializedName("imageUrl1")
  public String itemImgUrl;
  @ColumnInfo(name = "video_url")
  @SerializedName(value = "videoUrl", alternate = {"video_url", "video", "videos"})
  public String itemVideoUrl;
  @ColumnInfo(name = "product_name")
  @SerializedName(value = "title", alternate = {"Title", "Name", "name", "ProdName", "prodName", "prod_name", "ProductName", "productName", "product_name"})
  public String name;
  @ColumnInfo(name = "product_status")
  @SerializedName(value = "status", alternate = {"Status", "prodStatus", "ProdStatus", "prodstatus", "prod_status", "productStatus", "ProductStatus", "productstatus", "product_status"})
  public String status;
  @ColumnInfo(name = "ean")
  @SerializedName(value = "ean", alternate = {"EAN", "BarCode", "Barcode", "barCode", "barcode", "bar_code", "SkuId", "Skuid", "skuid", "skuId", "skuID", "sku_id", "isbn", "ISBN"})
  public String ean;
  @ColumnInfo(name = "ref_ean")
  @SerializedName(value = "refEan", alternate = {"RefEan", "refean", "ref_ean", "ReferenceEan", "referenceEan", "referenceean", "reference_ean", "RefId", "RefID", "refId", "refID", "refid", "ref_id", "ReferenceId", "ReferenceID", "referenceId", "referenceID", "referenceid", "reference_id", "RefBarCode", "refBarCode", "RefBarcode", "refBarcode", "refbarcode", "ref_bar_code", "ReferenceBarCode", "referenceBarCode", "ReferenceBarcode", "referenceBarcode", "referencebarcode", "reference_bar_code", "RefSkuId", "refSkuId", "RefSkuid", "refSkuid", "Refskuid", "refskuid", "RefskuId", "refskuId", "RefskuID", "refskuID", "ref_sku_id", "ReferenceSkuId", "referenceSkuId", "ReferenceSkuid", "referenceSkuid", "Referenceskuid", "referenceskuid", "ReferenceskuId", "referenceskuId", "ReferenceskuID", "referenceskuID", "reference_sku_id", "RefISBN", "refISBN", "RefIsbn", "refIsbn", "refisbn", "ref_isbn", "ReferenceISBN", "referenceISBN", "ReferenceIsbn", "referenceIsbn", "referenceisbn", "reference_isbn"})
  public String refEan;
  @ColumnInfo(name = "ean_qty", defaultValue = "0")
  @SerializedName("eanQty")
  public Integer eanQty = 0;
  @ColumnInfo(name = "epc")
  @SerializedName(value = "epc", alternate = {"Epc", "EPC", "rfid", "RFID", "rf_id", "rfId", "rfID"})
  public String epc;
  @ColumnInfo(name = "order_no")
  @SerializedName(value = "orderNo", alternate = {"code"})
  public String orderNo;
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
  @ColumnInfo(name = "age")
  @SerializedName("age")
  public Integer age = -1;
  @ColumnInfo(name = "sub_category")
  @SerializedName(value = "subCategory", alternate = "SubCategory")
  public String subCategory;
  @ColumnInfo(name = "matkl")
  @SerializedName(value = "matkl", alternate = {"Matkl", "family", "Family", "familyName", "FamilyName", "familyname", "family_name"})
  public String matkl;
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
  @ColumnInfo(name = "dest_zone")
  @SerializedName(value = "destZone", alternate = {"destZoneName", "dest_zone_name", "dest_zone", "destLocation", "destLocationName", "dest_location", "dest_location_name"})
  public String destZone;
  @ColumnInfo(name = "dest_zone_id", defaultValue = "0")
  @SerializedName(value = "destZoneId", alternate = {"dest_zone_id", "dest_loc_id", "destLocId", "dest_location_id", "destLocationId"})
  public String destZoneId;
  @ColumnInfo(name = "is_decoded", defaultValue = "0")
  @SerializedName(value = "isDecoded", alternate = {"isdecoded","is_decoded"})
  public Boolean isDecoded = false;
  @ColumnInfo(name = "is_found", defaultValue = "0")
  @SerializedName(value = "isFound", alternate = {"isfound","is_found"})
  public Boolean isFound = false;
  @ColumnInfo(name = "is_sold", defaultValue = "0")
  @SerializedName(value = "isSold", alternate = {"issold","is_sold"})
  public Boolean isSold = false;
  @Ignore
  @SerializedName(value = "isCompleted", alternate = {"is_completed"})
  public Boolean isCompleted = false;
  @Ignore
  @SerializedName(value = "isClosed", alternate = {"is_closed"})
  public Boolean isClosed = false;
  @Ignore
  @SerializedName(value = "isMismatched", alternate = {"is_mismatched"})
  public Boolean isMismatched = false;
  @ColumnInfo(name = "found_in_zone")
  @SerializedName("foundInZone")
  public String foundInZone;
  @SerializedName("imageId")
  public Integer itemImgId = 0;
  @SerializedName("qty") //temp change
  //@SerializedName(value = "remainingQuantity", alternate = {"qty"})
  public Integer qty;
  @ColumnInfo(name = "total_qty", defaultValue = "0")
  @SerializedName(value = "totalQty", alternate = {"totalqty", "total_qty", "total"})
  public Integer totalQty = 0;
  @ColumnInfo(name = "found_qty", defaultValue = "0")
  @SerializedName(value = "foundQty", alternate = {"foundqty", "found_qty", "found"})
  public Integer foundQty = 0;
  @ColumnInfo(name = "decode_qty", defaultValue = "0")
  @SerializedName(value = "decodedQty", alternate = {"decodedqty", "decoded_qty", "decoded"})
  public Integer decodedQty = 0;
  @ColumnInfo(name = "err_msg")
  @SerializedName(value = "errStockMsg", alternate = {"err_stock_msg", "err_msg", "errMsg", "err", "error"})
  public String errStockMsg;
  @ColumnInfo(name = "ageing_label")
  @SerializedName(value = "ageingLabel", alternate = {"ageing_label"})
  public String ageingLabel;
  @ColumnInfo(name = "ageing_hrs")
  @SerializedName(value = "ageingHrs", alternate = "ageing_hrs")
  public Integer ageingHrs;
  @ColumnInfo(name = "priority")
  @SerializedName("priority")
  public Integer priority;
  @ColumnInfo(name = "fifo_date")
  @SerializedName(value = "fifoDate", alternate = {"date", "fifo_date"})
  public String fifoDate;
  @ColumnInfo(name = "picked_epcs")
  @SerializedName(value = "pickedEPCs", alternate = {"picked_epcs", "picked_epc", "pickedEPC"})
  public String pickedEPCs;
  @ColumnInfo(name = "decoded_epcs")
  @SerializedName(value = "decodedEPCs", alternate = {"decoded_epcs", "decoded_epc", "decodedEPC"})
  public String decodedEPCs;
  @Ignore
  @SerializedName(value = "displayData", alternate = {"display_data"})
  public String displayData;
  @Ignore
  @SerializedName(value = "detailsDisplayData", alternate = {"DetailsDisplayData","detailsdisplayData","detailsdisplaydata","details_display_data"})
  public String displayDataDetails;
  //THAN
  @Ignore
  @SerializedName(value = "originalLength", alternate = {"OriginalLength", "original_length", "originallength"})
  public String lengthOriginal;
  @Ignore
  @SerializedName(value = "balanceLength", alternate = {"balance_length", "balancelength", "oldLength", "old_length", "oldlength"})
  public String lengthBalance;
  @Ignore
  @SerializedName(value = "cuttingLength", alternate = {"cutting_length", "cuttinglength"})
  public String lengthCutting;
  @Ignore
  @SerializedName(value = "closureLength", alternate = {"closure_length", "closurelength"})
  public String lengthClosure;
  @Ignore
  public boolean isUploaded = false;
  
  /**
   * Instantiates a new Product model.
   */
  public ProductModel(){/*Empty constructor*/}
  
  /**
   * Instantiates a new Product model.
   *
   * @param ean     the ean
   * @param orderNo the order no
   * @param status  the status
   */
  @Ignore
  public ProductModel(String ean, String orderNo, String status){
    this.ean = ean;
    this.orderNo = orderNo;
    this.status = status;
  }
  
  /**
   * Instantiates a new Product model.
   *
   * @param ean      the ean
   * @param orderNo  the order no
   * @param category the category
   * @param brand    the brand
   * @param status   the status
   */
  @Ignore
  public ProductModel(String ean, String orderNo, String category, String brand, String status){
    this.ean = ean;
    this.orderNo = orderNo;
    this.category = category;
    this.brand = brand;
    this.status = status;
  }
  
  /**
   * Instantiates a new Product model.
   *
   * @param epc       the epc
   * @param ean       the ean
   * @param name      the name
   * @param color     the color
   * @param itemImgId the item img id
   * @param qty       the qty
   */
  @Ignore
  public ProductModel(String epc, String ean, String name, String color, int itemImgId, Integer qty){
    this.epc = epc;
    this.ean = ean;
    this.name = name;
    this.color = color;
    this.itemImgId = itemImgId;
    this.qty = qty;
  }
  
  /**
   * Instantiates a new Product model.
   *
   * @param ean       the ean
   * @param name      the name
   * @param color     the color
   * @param itemImgId the item img id
   * @param qty       the qty
   */
  @Ignore
  public ProductModel(String ean, String name, String color, int itemImgId, Integer qty){
    this.ean = ean;
    this.name = name;
    this.color = color;
    this.itemImgId = itemImgId;
    this.qty = qty;
  }
  
  @Ignore
  public ProductModel(final ProductModel productModel){
    this.productNo = productModel.productNo;
    this.productId = productModel.productId;
    this.itemImgId = productModel.itemImgId;
    this.itemImgUrl = productModel.itemImgUrl;
    this.itemVideoUrl = productModel.itemVideoUrl;
    this.name = productModel.name;
    this.status = productModel.status;
    this.ean = productModel.ean;
    this.refEan = productModel.refEan;
    this.eanQty = productModel.eanQty;
    this.epc = productModel.epc;
    this.orderNo = productModel.orderNo;
    this.sessionType = productModel.sessionType;
    this.type = productModel.type;
    this.category = productModel.category;
    this.subCategory = productModel.subCategory;
    this.matkl = productModel.matkl;
    this.brand = productModel.brand;
    this.articleNo = productModel.articleNo;
    this.batchId = productModel.batchId;
    this.description = productModel.description;
    this.color = productModel.color;
    this.size = productModel.size;
    this.zone = productModel.zone;
    this.age = productModel.age;
    this.zoneId = chkNull(productModel.zoneId, "0");
    this.destZone = productModel.destZone;
    this.destZoneId = chkNull(productModel.destZoneId, "0");
    this.foundInZone = productModel.foundInZone;
    this.qty = productModel.qty;
    this.totalQty = productModel.totalQty;
    this.foundQty = productModel.foundQty;
    this.decodedQty = productModel.decodedQty;
    this.isDecoded = productModel.isDecoded;
    this.isFound = productModel.isFound;
    this.isSold = productModel.isSold;
    this.errStockMsg = productModel.errStockMsg;
    this.ageingLabel = productModel.ageingLabel;
    this.ageingHrs = productModel.ageingHrs;
    this.priority = productModel.priority;
    this.pickedEPCs = productModel.pickedEPCs;
    this.decodedEPCs = productModel.decodedEPCs;
    this.displayData = productModel.displayData;
    this.displayDataDetails = productModel.displayDataDetails;
    this.fifoDate = productModel.fifoDate;
    this.lengthOriginal=productModel.lengthOriginal;
    this.lengthBalance=productModel.lengthBalance;
    this.lengthCutting=productModel.lengthCutting;
    this.lengthClosure=productModel.lengthClosure;
  }
  
  /**
   * Instantiates a new Product model.
   *
   * @param in the in
   */
  @Ignore
  protected ProductModel(Parcel in){
    if(in.readByte() == 0){ productNo = null; }
    else{ productNo = in.readLong(); }
    productId = in.readString();
    itemImgId = in.readInt();
    itemImgUrl = in.readString();
    itemVideoUrl = in.readString();
    name = in.readString();
    status = in.readString();
    ean = in.readString();
    refEan = in.readString();
    eanQty = in.readInt();
    epc = in.readString();
    age = in.readInt();
    orderNo = in.readString();
    sessionType = in.readInt();
    type = in.readString();
    category = in.readString();
    subCategory = in.readString();
    matkl = in.readString();
    brand = in.readString();
    articleNo = in.readString();
    batchId = in.readString();
    description = in.readString();
    color = in.readString();
    size = in.readString();
    zone = in.readString();
    zoneId = chkNull(in.readString(), "0");
    destZone = in.readString();
    destZoneId = chkNull(in.readString(), "0");
    foundInZone = in.readString();
    qty = in.readInt();
    totalQty = in.readInt();
    foundQty = in.readInt();
    decodedQty = in.readInt();
    isDecoded = in.readByte() > 0;
    isFound = in.readByte() > 0;
    isSold = in.readByte() > 0;
    errStockMsg = in.readString();
    ageingLabel = in.readString();
    ageingHrs = in.readInt();
    priority = in.readInt();
    pickedEPCs = in.readString();
    decodedEPCs = in.readString();
    displayData = in.readString();
    displayDataDetails = in.readString();
    fifoDate = in.readString();
  }

  /**
   * Get age integer.
   *
   * @return the integer
   */
  public Integer getStockAge(){ return age; }

  /**
   * Set item age.
   *
   * @param age
   */
  public void setStockAge(Integer age){ this.age = age; }

  /**
   * Get item img id integer.
   *
   * @return the integer
   */
  public Integer getItemImgId(){ return chkNull(itemImgId, R.drawable.ic_no_img); }

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
  public String getItemImgUrl(){ return chkNull(itemImgUrl, ""); }
  
  /**
   * Set item img url.
   *
   * @param itemImgUrl the item img url
   */
  public void setItemImgUrl(String itemImgUrl){ this.itemImgUrl = itemImgUrl; }
  
  /**
   * Get item video url string.
   *
   * @return the string
   */
  public String getItemVideoUrl(){ return chkNull(itemVideoUrl, ""); }
  
  /**
   * Set item img url.
   *
   * @param itemVideoUrl the item img url
   */
  public void setItemVideoUrl(String itemVideoUrl){ this.itemVideoUrl = itemVideoUrl; }
  
  /**
   * Get name string.
   *
   * @return the string
   */
  public String getName(){ return chkNull(name, AppConstants.DEFAULT_NO_VALUE).trim(); }

  public String getNameLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.name)?productModelLabels.name:SharedPrefManager.getString(ParamConstants.LABEL_NAME,context.getString(R.string.lbl_name)).trim(); }


  public String getNameTxt(Context context){ return String.format(context.getString(R.string.txt__),getNameLbl(context),getName()); }
  
  /**
   * Set name.
   *
   * @param name the name
   */
  public void setName(String name){ this.name = name; }
  
  /**
   * Get status string.
   *
   * @return the string
   */
  public String getStatus(){ return chkNull(status, "").trim(); }
  
  /**
   * Set status.
   *
   * @param status the status
   */
  public void setStatus(String status){ this.status = status; }
  
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
   * Get ean qty string.
   *
   * @return the string
   */
  public String getEanQtyStr(){ return chkZero(eanQty, "0").trim(); }
  
  /**
   * Get ean qty int.
   *
   * @return the integer
   */
  public int getEanQty(){ return AppCommonMethods.parseInt(chkZero(eanQty, "0").trim()); }
  
  /**
   * Set ean qty.
   *
   * @param eanQty the ean qty
   */
  public void setEanQty(Integer eanQty){ this.eanQty = eanQty; }
  
  /**
   * Get epc string.
   *
   * @return the string
   */
  public String getEpc(){ return chkNull(epc, "").toUpperCase().trim(); }

  public String getEpcLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.epc)?productModelLabels.epc:SharedPrefManager.getString(ParamConstants.LABEL_EPC,context.getString(R.string.lbl_epc)).trim(); }


  public String getEpcTxt(Context context){ return String.format(context.getString(R.string.txt__),getEpcLbl(context),getEpc()); }


  /**
   * Set epc.
   *
   * @param epc the epc
   */
  public void setEpc(String epc){ this.epc = epc; }
  
  /**
   * Get order no string.
   *
   * @return the string
   */
  public String getOrderNo(){ return chkNull(orderNo, "").trim(); }
  
  public String getOrderNoLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.orderNo)?productModelLabels.orderNo:SharedPrefManager.getString(ParamConstants.LABEL_ORDER,context.getString(R.string.lbl_order)).trim(); }
  
  
  public String getOrderNoTxt(Context context){ return String.format(context.getString(R.string.txt__),getOrderNoLbl(context),getOrderNo()); }
  
  /**
   * Set order no.
   *
   * @param orderNo the order no
   */
  public void setOrderNo(String orderNo){ this.orderNo = orderNo; }
  
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
  
  public String getMatkl(){
    return chkNull(matkl, "");
  }

  public String getMatklLbl(Context context){ return productModelLabels!=null && isNonEmpty(productModelLabels.matkl)?productModelLabels.matkl:SharedPrefManager.getString(ParamConstants.LABEL_MATKL,context.getString(R.string.lbl_matkl)).trim(); }


  public String getMatklTxt(Context context){ return String.format(context.getString(R.string.txt__),getMatklLbl(context),getMatkl()); }


  public void setMatkl(String matkl){
    this.matkl = matkl;
  }
  
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
   * Get dest zone string.
   *
   * @return the string
   */
  public String getDestZone(){ return chkNull(destZone, AppConstants.DEFAULT_NO_VALUE).trim(); }
  
  /**
   * Set dest zone.
   *
   * @param destZone the dest zone
   */
  public void setDestZone(String destZone){ this.destZone = destZone; }
  
  /**
   * Get dest zone id string.
   *
   * @return the string
   */
  public String getDestZoneId(){ return getDestZoneId(null); }
  
  /**
   * Set dest zone id.
   *
   * @param destZoneId the dest zone id.
   */
  public void setDestZoneId(String destZoneId){ this.destZoneId = destZoneId; }
  
  public String getDestZoneId(final Context context){ return chkNull(destZoneId, context != null && isNonEmpty(destZone) && !destZone.equalsIgnoreCase(AppConstants.ALL) ? chkNull(AppDatabase.getZoneDao(context).getZoneIdByName(destZone), "0") : "0"); }
  
  /**
   * Get found in zone string.
   *
   * @return the string
   */
  public String getFoundInZone(){ return chkNull(foundInZone, getZone().trim()); }
  
  /**
   * Get total qty string.
   *
   * @return the string
   */
  public String getTotalQtyStr(){ return chkZero(totalQty, "0").trim(); }
  
  /**
   * Get total qty int.
   *
   * @return the int
   */
  public int getTotalQty(){ return AppCommonMethods.parseInt(chkZero(totalQty, "0").trim()); }
  
  /**
   * Set total qty.
   *
   * @param totalQty the total qty
   */
  public void setTotalQty(Integer totalQty){ this.totalQty = totalQty; }
  
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
   * Get decoded qty int.
   *
   * @return the int
   */
  public int getDecodedQty(){ return AppCommonMethods.parseInt(chkZero(decodedQty, "0").trim()); }
  
  /**
   * Set decoded qty.
   *
   * @param decodedQty the decoded qty
   */
  public void setDecodedQty(Integer decodedQty){ this.decodedQty = decodedQty; }
  
  /**
   * Get decoded qty string.
   *
   * @return the string
   */
  public String getDecodedQtyStr(){ return chkZero(decodedQty, "0").trim(); }
  
  /**
   * Get decoded boolean.
   *
   * @return the boolean
   */
  public Boolean getDecoded(){ return chkNull(isDecoded, false); }
  
  /**
   * Set decoded.
   *
   * @param decoded the decoded
   */
  public void setDecoded(Boolean decoded){ isDecoded = decoded; }
  
  /**
   * Get found boolean.
   *
   * @return the boolean
   */
  public Boolean getFound(){ return chkNull(isFound, false); }
  
  /**
   * Set found.
   *
   * @param found the found
   */
  public void setFound(Boolean found){ isFound = found; }
  
  /**
   * Get is verified boolean.
   *
   * @return the boolean
   */
  public Boolean getIsVerified(){ return getStatus().trim().equalsIgnoreCase(AppConstants.STATUS_VERIFIED); }
  
  /**
   * Get is completed boolean.
   *
   * @return the boolean
   */
  public Boolean getIsCompleted(){ return chkNull(isCompleted, getStatus().trim().equalsIgnoreCase(AppConstants.STATUS_COMPLETED)); }
  
  /**
   * Set is completed.
   *
   * @param completed the completed
   */
  public void setIsCompleted(Boolean completed){ isCompleted = completed; }
  
  /**
   * Get is mismatched boolean.
   *
   * @return the boolean
   */
  public Boolean getIsMismatched(){ return chkNull(isMismatched, getStatus().trim().equalsIgnoreCase("mismatched")); }
  
  /**
   * Set is mismatched.
   *
   * @param mismatched the mismatched
   */
  public void setIsMismatched(Boolean mismatched){ isMismatched = mismatched; }
  
  /**
   * Get sold boolean.
   *
   * @return the boolean
   */
  public Boolean getSold(){ return chkNull(isSold, getStatus().trim().equalsIgnoreCase("sold") || isNonEmpty(epc) && epc.length() >= 24 && epc.startsWith("0")); }
  
  /**
   * Set sold.
   *
   * @param sold the sold
   */
  public void setSold(Boolean sold){ isSold = sold; }
  
  /**
   * Get err stock msg string.
   *
   * @return the string
   */
  public String getErrStockMsg(){ return chkNull(errStockMsg, "").trim(); }
  
  /**
   * Set err stock msg.
   *
   * @param errStockMsg the err stock msg
   */
  public void setErrStockMsg(String errStockMsg){ this.errStockMsg = errStockMsg; }
  
  /**
   * Get ageing label string.
   *
   * @return the string
   */
  public String getAgeingLabel(){ return chkNull(ageingLabel, "").trim(); }
  
  /**
   * Set ageing label.
   *
   * @param ageingLabel the ageing label
   */
  public void setAgeingLabel(String ageingLabel){ this.ageingLabel = ageingLabel; }
  
  /**
   * Get ageing hrs integer.
   *
   * @return the integer
   */
  public Integer getAgeingHrs(){ return chkNull(ageingHrs, 0); }
  
  /**
   * Set ageing hrs.
   *
   * @param ageingHrs the ageing hrs
   */
  public void setAgeingHrs(Integer ageingHrs){ this.ageingHrs = ageingHrs; }
  
  /**
   * Get priority integer.
   *
   * @return the integer
   */
  public Integer getPriority(){ return chkNull(priority, 0); }
  
  /**
   * Set priority.
   *
   * @param priority the ageing hrs
   */
  public void setPriority(Integer priority){ this.priority = chkNull(priority, 0); }
  
  /**
   * Get decoded eans string.
   *
   * @return the string
   */
  public String getPickedEPCs(){ return chkNull(pickedEPCs, ""); }
  
  /**
   * Set decoed eans.
   *
   * @param pickedEPCs the decoded eans
   */
  public void setPickedEPCs(String pickedEPCs){ this.pickedEPCs = chkNull(pickedEPCs, ""); }
  
  /**
   * Get decoded eans string.
   *
   * @return the string
   */
  public String getDecodedEPCs(){ return chkNull(decodedEPCs, ""); }
  
  /**
   * Set decoed eans.
   *
   * @param decodedEPCs the decoded eans
   */
  public void setDecodedEPCs(String decodedEPCs){ this.decodedEPCs = chkNull(decodedEPCs, ""); }
  
  /**
   * Get display data json array.
   *
   * @return the json array
   */
  public JSONArray getDisplayData(){ return getDisplayData(null); }
  
  /**
   * Set display data.
   *
   * @param displayData the display data
   */
  public void setDisplayData(String displayData){ this.displayData = displayData; }
  
  /**
   * Get display data json array.
   *
   * @param context the context
   * @return the json array
   */
  public JSONArray getDisplayData(Context context){ return getDisplayData(context, null); }
  
  /**
   * Get display data json array.
   *
   * @param context             the context
   * @param compareProductModel the compare product model
   * @return the json array
   */
  public JSONArray getDisplayData(Context context, final ProductModel compareProductModel){ return getDisplayData(context, compareProductModel, ""); }
  
  /**
   * Get display data json array.
   *
   * @param context             the context
   * @param compareProductModel the compare product model
   * @param replenishmentType   the replenishment type
   * @return the json array
   */
  public JSONArray getDisplayData(Context context, final ProductModel compareProductModel, String replenishmentType){ return getDisplayData(context, compareProductModel, replenishmentType, true); }
  
  /**
   * Get display data json array.
   *
   * @param context             the context
   * @param compareProductModel the compare product model
   * @param replenishmentType   the replenishment type
   * @param isShowZone          the is show zone
   * @return the json array
   */
  public JSONArray getDisplayData(Context context, final ProductModel compareProductModel, String replenishmentType, boolean isShowZone){ return generateDisplayData(context, compareProductModel, replenishmentType, isShowZone); }
  
  /**
   * Generate display data json array.
   *
   * @param context             the context
   * @param compareProductModel the compare product model
   * @param replenishmentType   the replenishment type
   * @return the json array
   */
  private JSONArray generateDisplayData(Context context, final ProductModel compareProductModel, final String replenishmentType, final boolean isShowZone){
    try{
      if(isNonEmpty(displayData) && compareProductModel != null){
        //handle Comparison
      }
      return new JSONArray(chkNull(displayData, setDefaultDisplay(context, compareProductModel, replenishmentType, isShowZone)));
    }
    catch(Exception e){ e.printStackTrace(); }
    return null;
  }
  
  /**
   * Set default display string.
   *
   * @param context             the context
   * @param compareProductModel the compare product model
   * @param replenishmentType   the replenishment type
   * @return the string
   */
  private String setDefaultDisplay(final Context context, final ProductModel compareProductModel, final String replenishmentType, final boolean isShowZone){
    if(context == null) return null;
    try{
      JSONArray jsonDataArray = new JSONArray();
      if(isNonEmpty(getEan())){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, getEanLbl(context));
        jobj.put(ParamConstants.VALUE, getEan());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(getSubcategory().replaceFirst(AppConstants.ALL, AppConstants.DEFAULT_NO_VALUE).trim())){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, getSubCategoryLbl(context));
        jobj.put(ParamConstants.VALUE, getSubcategory());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(getName())){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, getNameLbl(context));//SharedPrefManager.getString(ParamConstants.LABEL_NAME,context.getString(R.string.lbl_name)));
        jobj.put(ParamConstants.VALUE, getName());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(getProductId()) && isNullOrEmpty(articleNo)){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL,getProductIdLbl(context));//SharedPrefManager.getString(ParamConstants.LABEL_PRODUCT_ID,context.getString(R.string.lbl_product_id)));
        jobj.put(ParamConstants.VALUE, getProductId());
        jsonDataArray.put(jobj);
      }
      if(isNullOrEmpty(productId) && isNonEmpty(getArticleNo())){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, getArticleNoLbl(context));//SharedPrefManager.getString(ParamConstants.LABEL_ARTICLES,context.getString(R.string.lbl_article_no)));
        jobj.put(ParamConstants.VALUE, getArticleNo());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(getLengthOriginal())){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_than_length_original));
        jobj.put(ParamConstants.VALUE, getLengthOriginal());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(getLengthBalance())){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_than_length_balance));
        jobj.put(ParamConstants.VALUE, getLengthBalance());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(chkZero(getLengthTotalCut(), ""))){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_than_length_total_cut));
        jobj.put(ParamConstants.VALUE, getLengthTotalCut());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(chkZero(getLengthClosure(), ""))){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_than_length_closure));
        jobj.put(ParamConstants.VALUE, getLengthClosure());
        jsonDataArray.put(jobj);
      }
      if(getClosed()){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_grn_status));
        jobj.put(ParamConstants.VALUE, AppConstants.THAN_STATUS_CLOSED);
        jsonDataArray.put(jobj);
      }
      if(getSold()){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_grn_status));
        jobj.put(ParamConstants.VALUE, AppConstants.STATUS_SOLD);
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(chkZero(getLengthCutting(), ""))){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_than_length_cutting));
        jobj.put(ParamConstants.VALUE, getLengthCutting());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(getFifoDate())){
        String fifoDate = getFifoDate();
        try{
          fifoDate = new SimpleDateFormat(AppCommonMethods.DATE_FORMAT).format(new SimpleDateFormat(AppCommonMethods.SERVER_DATE_FORMAT).parse(fifoDate));
        }
        catch(Exception e){ }
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_fifo_date));
        jobj.put(ParamConstants.VALUE, fifoDate);
        jsonDataArray.put(jobj);
      }
      if (getStockAge() != null && getStockAge() >= 0){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, context.getString(R.string.lbl_age));
        jobj.put(ParamConstants.VALUE, age+" Day(s)");
        jsonDataArray.put(jobj);
      }
      if((isNonEmpty(ean) && isNonEmpty(fifoDate)) || (isShowZone && isNonEmpty(getZone().replaceFirst(AppConstants.ALL, AppConstants.DEFAULT_NO_VALUE).trim()))){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, getZoneLbl(context));//SharedPrefManager.getString(ParamConstants.LABEL_ZONES,context.getString(R.string.lbl_loc)));
        jobj.put(ParamConstants.VALUE, isNonEmpty(fifoDate) ? chkNull(AppDatabase.getFIFODao(context).getDatewiseZoneStr(ean, fifoDate) ,getZone()): getZone());
        jsonDataArray.put(jobj);
      }
      if((!isShowZone && getTotalQty() > 0) || (isShowZone && chkZero(getEanQty(), getQty()) > 0)){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, context.getString(chkNull(replenishmentType, "").equalsIgnoreCase(AppConstants.REPLENISH_TYPE_STATIC) ? R.string.search_product_stock_available : R.string.lbl_qty));
        jobj.put(ParamConstants.VALUE, !isShowZone ? getTotalQty() : chkNull(replenishmentType, "").equalsIgnoreCase(AppConstants.REPLENISH_TYPE_DYNAMIC) ? chkZero(getQty(), getEanQty()) : chkZero(getEanQty(), getQty()));
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(getSize())){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, getSizeLbl(context));//SharedPrefManager.getString(ParamConstants.LABEL_SIZES,context.getString(R.string.lbl_size)));
        jobj.put(ParamConstants.VALUE, getSize());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(getColor())){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, getColorLbl(context));//SharedPrefManager.getString(ParamConstants.LABEL_COLORS,context.getString(R.string.lbl_color)));
        jobj.put(ParamConstants.VALUE, getColor());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(chkNull(getMatkl(), getOrderNo()))){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, isNonEmpty(getMatkl())?getMatklLbl(context):getOrderNoLbl(context));
        jobj.put(ParamConstants.VALUE, chkNull(getMatkl(), getOrderNo()));
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(getBrand().replaceFirst(AppConstants.ALL, AppConstants.DEFAULT_NO_VALUE).trim())){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, getBrandLbl(context));//SharedPrefManager.getString(ParamConstants.LABEL_BRANDS,context.getString(R.string.lbl_brand)));
        jobj.put(ParamConstants.VALUE, getBrand());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(getCategory().replaceFirst(AppConstants.ALL, AppConstants.DEFAULT_NO_VALUE).trim())){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, getCategoryLbl(context));//SharedPrefManager.getString(ParamConstants.LABEL_CATEGORIES,context.getString(R.string.lbl_category)));
        jobj.put(ParamConstants.VALUE, getCategory());
        jsonDataArray.put(jobj);
      }
      if(isNonEmpty(getDescription())){
        JSONObject jobj = new JSONObject();
        jobj.put(ParamConstants.LABEL, getDescriptionLbl(context));//SharedPrefManager.getString(ParamConstants.LABEL_DESCRIPTION,context.getString(R.string.lbl_description)));
        jobj.put(ParamConstants.VALUE, getDescription());
        jsonDataArray.put(jobj);
      }
      return jsonDataArray.toString();
    }
    catch(Exception e){ e.printStackTrace(); }
    return null;
  }

  public String getDisplayDataDetails() {
    return displayDataDetails;
  }

  public void setDisplayDataDetails(String displayDataDetails) {
    this.displayDataDetails = displayDataDetails;
  }

  /**
   * Get fifoDate string.
   *
   * @return the string
   */
  public String getFifoDate(){
    return chkNull(fifoDate, "");
  }
  
  /**
   * Set fifo date.
   *
   * @param fifoDate the fifo date
   */
  public void setFifoDate(String fifoDate){
    this.fifoDate = chkNull(fifoDate,"");
  }
  
  public Boolean getClosed(){
    return chkNull(isClosed, false);
  }
  
  public void setClosed(Boolean closed){
    isClosed = closed;
  }
  
  public String getLengthOriginal(){
    return formatDoubleStr2Decimals(chkNull(lengthOriginal, ""));
  }
  
  public void setLengthOriginal(String lengthOriginal){
    this.lengthOriginal = chkNull(lengthOriginal, "");
  }
  
  public String getLengthBalance(){
    return formatDoubleStr2Decimals(chkNull(lengthBalance, ""));
  }
  
  public void setLengthBalance(String lengthBalance){
    this.lengthBalance = chkNull(lengthBalance, "");
  }
  
  public String getLengthTotalCut(){
    try{
      return formatDoubleStr2Decimals(String.valueOf(Double.parseDouble(chkNull(lengthOriginal, "0")) - Double.parseDouble(chkNull(lengthBalance, "0"))));
    }
    catch(Exception e){ }
    return "0";
  }
  
  public String getLengthCutting(){
    return formatDoubleStr2Decimals(chkNull(lengthCutting, ""));
  }
  
  public void setLengthCutting(String lengthCutting){
    this.lengthCutting = chkNull(lengthCutting, "");
    this.lengthBalance = String.valueOf(Double.parseDouble(chkNull(lengthBalance, "0")) - Double.parseDouble(chkNull(this.lengthCutting, "0")));
  }
  
  public String getLengthClosure(){
    return formatDoubleStr2Decimals(chkNull(lengthClosure, ""));
  }
  
  public void setLengthClosure(String lengthClosure){
    this.lengthClosure = chkNull(lengthClosure, "");
    this.lengthBalance = String.valueOf(Double.parseDouble(chkNull(lengthBalance, "0")) - Double.parseDouble(chkNull(this.lengthClosure, "0")));
  }
  
  public boolean isUploaded(){
    return isUploaded;
  }
  
  public void setUploaded(boolean uploaded){
    isUploaded = uploaded;
  }
  
  /**
   * To get list of label value fields for than printing
   *
   * @return the list of label value fields
   */
  public List<LabelValues> getLabelValueListForThanPrinting(final CommonActivity context, final boolean isClosure, final String length, final String unit){
    List<LabelValues> listPrintFields = new ArrayList<>(0);
    if(isNonEmpty(getEan()))
      listPrintFields.add(new LabelValues(SharedPrefManager.getString(ParamConstants.LABEL_EANS,context.getString(R.string.lbl_ean)), getEan()));
    if(isNonEmpty(getArticleNo()))
      listPrintFields.add(new LabelValues(SharedPrefManager.getString(ParamConstants.LABEL_ARTICLES,context.getString(R.string.lbl_article_no)), getArticleNo()));
    if(isNonEmpty(getName()))
      listPrintFields.add(new LabelValues(SharedPrefManager.getString(ParamConstants.LABEL_NAME,context.getString(R.string.lbl_name)), getName()));
    if(isNonEmpty(length)){
      final String lblLen = context.getString(isClosure ? R.string.lbl_than_length_closure : R.string.lbl_than_length_cutting);
      final String lbl = isNonEmpty(unit) ? lblLen + "\t(" + unit + ")" : lblLen;
      listPrintFields.add(new LabelValues(lbl, length));
    }
    return listPrintFields;
  }
  
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
    ProductModel that = (ProductModel) o;
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
    dest.writeLong(chkNull(itemImgId, 0));
    dest.writeString(itemImgUrl);
    dest.writeString(itemVideoUrl);
    dest.writeString(name);
    dest.writeString(status);
    dest.writeString(ean);
    dest.writeString(refEan);
    dest.writeInt(chkNull(eanQty, 0));
    dest.writeString(epc);
    dest.writeString(orderNo);
    dest.writeInt(chkNull(age, 0));
    dest.writeInt(chkNull(sessionType, 0));
    dest.writeString(type);
    dest.writeString(category);
    dest.writeString(subCategory);
    dest.writeString(matkl);
    dest.writeString(brand);
    dest.writeString(articleNo);
    dest.writeString(batchId);
    dest.writeString(description);
    dest.writeString(color);
    dest.writeString(size);
    dest.writeString(zone);
    dest.writeString(chkNull(zoneId, "0"));
    dest.writeString(destZone);
    dest.writeString(chkNull(destZoneId, "0"));
    dest.writeString(foundInZone);
    dest.writeInt(chkNull(qty, 0));
    dest.writeInt(chkNull(totalQty, 0));
    dest.writeInt(chkNull(foundQty, 0));
    dest.writeInt(chkNull(decodedQty, 0));
    dest.writeByte((byte) (isDecoded == null && isDecoded ? 1 : 0));
    dest.writeByte((byte) (isFound == null && isFound ? 1 : 0));
    dest.writeByte((byte) (isSold == null && isSold ? 1 : 0));
    dest.writeString(errStockMsg);
    dest.writeString(ageingLabel);
    dest.writeInt(chkNull(ageingHrs, 0));
    dest.writeInt(chkNull(priority, 0));
    dest.writeString(pickedEPCs);
    dest.writeString(decodedEPCs);
    dest.writeString(displayData);
    dest.writeString(displayDataDetails);
    dest.writeString(fifoDate);
  }
}
