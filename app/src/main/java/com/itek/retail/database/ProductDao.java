package com.itek.retail.database;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.itek.retail.common.AppConstants;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.model.ProductModel;

import java.util.List;
import java.util.Set;

/**
 * The interface Stock correction dao.
 */
@androidx.room.Dao
public interface ProductDao{
  
  /**
   * Insert.
   *
   * @param productModels the product models
   */
  @Insert
  void insert(ProductModel... productModels);
  
  @Insert
  void insertAll(List<ProductModel> productModels);
  
  /**
   * Update.
   *
   * @param productModels the product models
   */
  @Update
  Integer update(ProductModel... productModels);
  
  /**
   * Delete.
   *
   * @param productModels the product models
   */
  @Delete
  Integer delete(ProductModel... productModels);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM product")
  void deleteAll();
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM product WHERE session_type NOT IN (27)")
  void deleteAllExcept();
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM product WHERE session_type IS NULL OR session_type<=0 OR session_type=:sessionType")
  void deleteAll(int sessionType);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM product WHERE TRIM(ean) COLLATE NOCASE =:ean")
  Integer deleteEan(String ean);
  
  /**
   * Delete extra zone.
   */
  @Query("DELETE FROM product WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE =:ean AND is_found<=0 AND found_qty<=0")
  Integer deleteExtraZones(String ean);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM product WHERE TRIM(order_no)=:orderNo")
  Integer deleteOrder(String orderNo);
  
  /**
   * Update found epc.
   *
   * @param epc  the epc
   * @param ean  the ean
   * @param zone the zone
   */
  @Query("UPDATE product SET is_found=1,found_qty=found_qty+1,epc=CASE WHEN epc IS NOT NULL AND LENGTH(epc)>0 THEN epc||','||:epc ELSE :epc END WHERE ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean AND (zone IS NULL OR LENGTH(zone)<=0 OR TRIM(zone)= :zone)")
  Integer updateFoundEPC(String epc, String ean, String zone);
  
  /**
   * Update found epc.
   *
   * @param epc  the epc
   * @param ean  the ean
   * @param zone the zone
   */
  @Query("UPDATE product SET priority=0,is_found=1,found_qty=found_qty+1,epc=CASE WHEN epc IS NOT NULL AND LENGTH(epc)>0 THEN epc||','||:epc ELSE :epc END WHERE ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean AND (zone IS NULL OR LENGTH(zone)<=0 OR TRIM(zone)= :zone)")
  Integer updateFoundEPCOffRange(String epc, String ean, String zone);
  
  /**
   * Update decoded epc.
   *
   * @param epc  the epc
   * @param ean  the ean
   * @param zone the zone
   */
  @Query("UPDATE product SET is_decoded=1,decode_qty=decode_qty+1,epc=CASE WHEN epc IS NOT NULL AND LENGTH(epc)>0 THEN epc||','||:epc ELSE :epc END WHERE ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean AND (zone IS NULL OR LENGTH(zone)<=0 OR TRIM(zone)= :zone)")
  Integer updateDecodedEPC(String epc, String ean, String zone);
  
  @Query("UPDATE product SET is_found=found_qty>1,found_qty=found_qty-1,epc=REPLACE(REPLACE(epc,:epc,''),',,',',') WHERE (epc IS NULL OR LENGTH(TRIM(epc))<=0 OR TRIM(epc) COLLATE NOCASE LIKE '%'||:epc||'%') AND ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean AND (zone IS NULL OR LENGTH(zone)<=0 OR TRIM(zone)= :zone)")
  Integer updateDeletedEPC(String epc, String ean, String zone);
  
  /**
   * Update found.
   *
   * @param epc     the epc
   * @param selZone the sel zone
   */
  @Query("UPDATE product SET is_found=1,found_in_zone= :selZone WHERE TRIM(epc) COLLATE NOCASE = :epc AND is_found=0")
  Integer updateFound(String epc, String selZone);
  
  /**
   * Update found.
   *
   * @param epc the epc
   */
  @Query("UPDATE product SET is_found=1 WHERE TRIM(epc) COLLATE NOCASE = :epc AND is_found=0")
  Integer updateFound(String epc);
  
  /**
   * Update found qty.
   *
   * @param ean      the ean
   * @param foundQty the found qty
   */
  @Query("UPDATE product SET is_found=:foundQty>0 , found_qty=:foundQty WHERE TRIM(ean) COLLATE NOCASE = :ean")
  Integer updateFoundQty(String ean, int foundQty);
  
  /**
   * Update found qty.
   *
   * @param ean      the ean
   * @param foundQty the found qty
   */
  @Query("UPDATE product SET is_found=:foundQty>0, epc=:epc, found_qty=:foundQty WHERE TRIM(ean) COLLATE NOCASE = :ean")
  Integer updateFoundQty(String ean, String epc, int foundQty);
  
  /**
   * Update total qty.
   *
   * @param ean      the ean
   * @param totalQty the found qty
   */
  @Query("UPDATE product SET total_qty=:totalQty WHERE TRIM(ean) COLLATE NOCASE = :ean AND (session_type IS NULL OR session_type<=0 OR session_type=:sessionType)")
  Integer updateTotalQty(String ean, int totalQty, int sessionType);
  
  @Query("UPDATE product SET total_qty=(SELECT COALESCE(SUM(ean_qty),0) FROM product WHERE TRIM(ean) COLLATE NOCASE = :ean) WHERE TRIM(ean) COLLATE NOCASE = :ean AND (session_type IS NULL OR session_type<=0 OR session_type=:sessionType)")
  Integer updateTotalQty(String ean, int sessionType);
  
  @Query("Select DISTINCT ean FROM product WHERE (session_type IS NULL OR session_type<=0 OR session_type=:sessionType)")
  List<String> getDistinctEans(int sessionType);
  
  /**
   * Update verify status
   */
  @Query("UPDATE product SET product_status='" + AppConstants.STATUS_VERIFIED + "' WHERE found_qty>0 AND product_status!='" + AppConstants.STATUS_VERIFIED + "'")
  Integer updateVerifyStatus();
  
  /**
   * get total qty.
   *
   * @param ean the ean
   * @return the total
   */
  @Query("SELECT COALESCE(SUM(ean_qty),0) FROM product WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean")
  Integer getTotalQty(String ean);
  
  /**
   * Update decode qty.
   *
   * @param ean       the ean
   * @param decodeQty the decode qty
   */
  @Query("UPDATE product SET is_decoded=:decodeQty>0,decode_qty=:decodeQty WHERE TRIM(ean) COLLATE NOCASE = :ean")
  Integer updateDecodeQty(String ean, int decodeQty);
  
  /**
   * Reset order.
   *
   * @param orderNo the order no
   */
  @Query("UPDATE product SET is_found=0,found_qty=0,is_decoded=0,decode_qty=0 WHERE TRIM(order_no)= :orderNo")
  void resetOrder(String orderNo);
  
  /**
   * Reset ean.
   *
   * @param ean the order no
   */
  @Query("UPDATE product SET is_found=0,found_qty=0,is_decoded=0,decode_qty=0 WHERE TRIM(ean) COLLATE NOCASE = :ean")
  void resetEan(String ean);
  
  /**
   * Is epc present boolean.
   *
   * @param epc the epc
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product WHERE epc IS NOT NULL AND LENGTH(TRIM(epc))>0 AND TRIM(epc) COLLATE NOCASE = :epc AND is_found=0")
  boolean isEPCPresent(String epc);
  
  /**
   * Gets category list.
   *
   * @return the category list
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(category) FROM  product WHERE category IS NOT NULL AND LENGTH(TRIM(category))>0 AND TRIM(category) !='" + AppConstants.ALL + "'")
  List<String> getCategoryList();
  
  /**
   * Gets location list.
   *
   * @return the location list
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(zone) FROM  product WHERE zone IS NOT NULL AND LENGTH(TRIM(zone))>0 AND TRIM(zone) !='" + AppConstants.ALL + "'")
  List<String> getLocationList();
  
  /**
   * Gets dest location list.
   *
   * @return the dest location list
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(dest_zone) FROM  product WHERE dest_zone IS NOT NULL AND LENGTH(TRIM(dest_zone))>0 AND TRIM(dest_zone) !='" + AppConstants.ALL + "'")
  List<String> getDestLocationList();
  
  /**
   * Gets brand list.
   *
   * @return the brand list
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(brand) FROM  product WHERE brand IS NOT NULL AND LENGTH(TRIM(brand))>0 AND TRIM(brand) !='" + AppConstants.ALL + "'")
  List<String> getBrandList();
  
  /**
   * Gets color list.
   *
   * @return the color list
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(COALESCE(color,'" + AppConstants.DEFAULT_NO_VALUE + "')) FROM product WHERE (session_type IS NULL OR session_type<=0 OR session_type=4)")
  List<String> getColorList();
  
  /**
   * On product found live data.
   *
   * @return the live data
   */
  @Transaction
  @Query("SELECT COALESCE(SUM(is_found),0) FROM  product")
  LiveData<Integer> onProductFound();
  
  /**
   * Search shortage products list.
   *
   * @param query the query
   * @return the list
   */
  @RawQuery
  List<MultiQtyModel> searchShortageProducts(SupportSQLiteQuery query);
  
  /**
   * Search shortage header shortage model.
   *
   * @param query the query
   * @return the shortage model
   */
  @RawQuery
  MultiQtyModel searchShortageHeader(SupportSQLiteQuery query);
  
  /**
   * Search shortage products list.
   *
   * @param zone       the zone
   * @param category   the category
   * @param brand      the brand
   * @param sortColumn the sort column
   * @return the list
   */
  default List<MultiQtyModel> searchShortageProducts(String zone, String category, String brand, String sortColumn){
    String condition = "";
    if(!chkNull(zone, AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(zone) IN('" + zone.replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
    if(!chkNull(category, AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(category) IN('" + category.replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
    if(!chkNull(brand, AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(brand) IN('" + brand.replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
    String statement = "SELECT brand AS 'title',COALESCE(COUNT(*),0) AS 'total',COALESCE(SUM(is_found),0) AS 'found' FROM product " + condition + " GROUP BY TRIM(brand) " + (isNonEmpty(sortColumn) ? "ORDER BY " + sortColumn : "");
    showLog("searchShortageProducts query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    List<MultiQtyModel> resultList = searchShortageProducts(query);
    if(isNonEmpty(resultList))
      resultList.add(0, searchShortageHeader(new SimpleSQLiteQuery("SELECT '" + AppConstants.ALL + "' AS 'title',COALESCE(COUNT(*),0) AS 'total',COALESCE(SUM(is_found),0) AS 'found' FROM product " + condition, new Object[]{})));
    return resultList;
  }
  
  /**
   * Gets total counts.
   *
   * @return the total counts
   */
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Query("SELECT '" + AppConstants.ALL + "' AS 'title',COALESCE(COUNT(*),0) AS 'total',COALESCE(SUM(is_found),0) AS 'found' FROM product")
  MultiQtyModel getTotalCounts();
  
  /**
   * Gets total omni counts.
   *
   * @param orderNo the order no
   * @return the total omni order counts
   */
  @Query("SELECT order_no AS 'title',COALESCE(SUM(ean_qty),0) AS 'total',COALESCE(SUM(found_qty),0) AS 'found',COALESCE(SUM(decode_qty),0) AS 'decoded',COALESCE(SUM(qty),0) AS 'required' FROM product WHERE order_no IS NOT NULL AND LENGTH(TRIM(order_no))>0 AND TRIM(order_no)=:orderNo")
  MultiQtyModel getTotalOmniOrderCounts(String orderNo);
  
  /**
   * Gets total omni ean counts.
   *
   * @param ean the ean
   * @return the total omni ean counts
   */
  @Query("SELECT ean AS 'title',COALESCE(SUM(ean_qty),0) AS 'total',COALESCE(SUM(found_qty),0) AS 'found',COALESCE(SUM(decode_qty),0) AS 'decoded',COALESCE(SUM(qty),0) AS 'required' FROM product WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE =:ean")
  MultiQtyModel getTotalOmniEanCounts(String ean);
  
  @Query("SELECT ean AS 'title',COALESCE(SUM(ean_qty),0) AS 'total',COALESCE(SUM(found_qty),0) AS 'found',COALESCE(SUM(decode_qty),0) AS 'decoded',COALESCE(SUM(qty),0) AS 'required' FROM product WHERE session_type=:sessionType AND ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE =:ean")
  MultiQtyModel getTotalCounts(int sessionType, String ean);
  
  @Query("SELECT ean AS 'title',COALESCE(SUM(ean_qty),0) AS 'total',COALESCE(SUM(found_qty),0) AS 'found',COALESCE(SUM(decode_qty),0) AS 'decoded',COALESCE(SUM(qty),0) AS 'required' FROM product WHERE session_type=:sessionType")
  MultiQtyModel getTotalCounts(int sessionType);
  
  
  @Query("SELECT COALESCE(SUM(found_qty),0)>0 AND COALESCE(SUM(found_qty),0)>=COALESCE(SUM(qty),0) FROM product WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE =:ean")
  boolean isQtyFound(String ean);
  
  @Query("SELECT COALESCE(SUM(decode_qty),0)>0 AND COALESCE(SUM(decode_qty),0)>=COALESCE(SUM(qty),0) FROM product WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE =:ean")
  boolean isQtyDecoded(String ean);
  
  @Query("UPDATE product SET qty=:orderQty WHERE rowid IN (SELECT rowid FROM product WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE =:ean ORDER BY rowid LIMIT 1)")
  Integer updateOrderQty(Integer orderQty, String ean);
  
  /**
   * get Order qty.
   */
  @Query("SELECT COALESCE(SUM(qty),0) FROM product WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE =:ean")
  Integer getOrderQty(String ean);
  
  /**
   * Gets total found count.
   *
   * @return the total found count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM product WHERE is_found>0")
  Integer getTotalFoundCount();
  
  /**
   * Gets total found count.
   *
   * @return the total found count
   */
  @Query("SELECT COALESCE(SUM(found_qty),0) FROM product WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE =:ean")
  Integer getEANFoundCount(String ean);
  
  /**
   * Gets all total.
   *
   * @return the all total
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM product")
  Integer getAllTotal();
  
  /**
   * Gets all total.
   *
   * @return the all total
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM product WHERE session_type=:sessionType")
  Long getAllTotal(int sessionType);
  
  /**
   * Gets found count.
   *
   * @param zone     the zone
   * @param category the category
   * @param brand    the brand
   * @return the found count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM product WHERE is_found>0 AND (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone='" + AppConstants.ALL + "' OR TRIM(zone) = :zone) AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand)")
  Integer getFoundCount(String zone, String category, String brand);
  
  /**
   * Gets total ean count.
   *
   * @param zone       the zone
   * @param zones      the zones
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @param searchName the search name
   * @return the total ean count
   */
  @Query("SELECT COALESCE(SUM(ean_qty),0) FROM product WHERE (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone='" + AppConstants.ALL + "' OR TRIM(zone)=:zone OR TRIM(zone) IN(:zones)) AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(product_name) LIKE '%'||:searchName||'%' OR TRIM(ean) LIKE '%'||:searchName||'%')")
  Integer getTotalEANCount(String zone, Set<String> zones, String category, Set<String> categories, String brand, Set<String> brands, String searchName);
  
  /**
   * Gets total ean count.
   *
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @return the total ean count
   */
  @Query("SELECT COALESCE(SUM(ean_qty),0) FROM product WHERE (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands))")
  Integer getTotalEANCount(String category, Set<String> categories, String brand, Set<String> brands);
  
  /**
   * Gets total ean count.
   *
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @param searchName the search name
   * @return the total ean count
   */
  @Query("SELECT COALESCE(SUM(ean_qty),0) FROM product WHERE (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(product_name) LIKE '%'||:searchName||'%' OR TRIM(ean) LIKE '%'||:searchName||'%')")
  Integer getTotalEANCount(String category, Set<String> categories, String brand, Set<String> brands, String searchName);
  
  /**
   * Gets total ean count.
   *
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @return the total ean count
   */
  @Query("SELECT COALESCE(SUM(ean_qty),0) FROM product WHERE is_found>0 AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands))")
  Integer getTotalFoundEANCount(String category, Set<String> categories, String brand, Set<String> brands);
  
  /**
   * Gets total ean count.
   *
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @param searchName the search name
   * @return the total ean count
   */
  @Query("SELECT COALESCE(SUM(ean_qty),0) FROM product WHERE is_found>0 AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(product_name) LIKE '%'||:searchName||'%' OR TRIM(ean) LIKE '%'||:searchName||'%')")
  Integer getTotalFoundEANCount(String category, Set<String> categories, String brand, Set<String> brands, String searchName);
  
  /**
   * Gets total count.
   *
   * @param zone     the zone
   * @param category the category
   * @param brand    the brand
   * @return the total count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM product WHERE  (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone='" + AppConstants.ALL + "' OR TRIM(zone) = :zone) AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand)")
  Integer getTotalCount(String zone, String category, String brand);
  
  /**
   * Gets shortage products.
   *
   * @param zone     the zone
   * @param category the category
   * @param brand    the brand
   * @return the shortage products
   */
  @Transaction
  @Query("SELECT *,'' AS 'epc', COALESCE(SUM(CASE WHEN is_found THEN 1 ELSE 0 END),0) AS 'found_qty',COALESCE(COUNT(ean),0) AS 'ean_qty' FROM product WHERE (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone='" + AppConstants.ALL + "' OR TRIM(zone) = :zone) AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand) GROUP BY ean HAVING ean IS NOT NULL AND LENGTH(ean)>0 ORDER BY is_found asc,brand asc")
  List<ProductModel> getShortageProducts(String zone, String category, String brand);
  
  /**
   * Gets shortage found products.
   *
   * @param zone     the zone
   * @param category the category
   * @param brand    the brand
   * @return the shortage found products
   */
  @Query("SELECT *,'' AS 'epc', COALESCE(SUM(CASE WHEN is_found THEN 1 ELSE 0 END),0) AS 'found_qty', COALESCE(COUNT(ean),0) AS 'ean_qty' FROM product WHERE (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone='" + AppConstants.ALL + "' OR TRIM(zone) = :zone OR TRIM(found_in_zone)=:zone) AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand) GROUP BY ean HAVING ean IS NOT NULL AND LENGTH(ean)>0 AND COALESCE(SUM(CASE WHEN is_found THEN 1 ELSE 0 END),0)>0 ORDER BY is_found desc")
  List<ProductModel> getShortageFoundProducts(String zone, String category, String brand);
  
  /**
   * Gets all found products.
   *
   * @return the all found products
   */
  @Transaction
  @Query("SELECT * FROM product WHERE is_found=1")
  List<ProductModel> getAllFoundProducts();
  
  /**
   * Gets replenishment products.
   *
   * @param zone       the zone
   * @param zones      the zones
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @param searchName the search name
   * @return the replenishment products
   */
  @Transaction
  @Query("SELECT * FROM product WHERE (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone='" + AppConstants.ALL + "' OR TRIM(zone)=:zone OR TRIM(zone) IN(:zones)) AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(product_name) LIKE '%'||:searchName||'%' OR TRIM(ean) LIKE '%'||:searchName||'%')")
  List<ProductModel> getReplenishmentProducts(String zone, Set<String> zones, String category, Set<String> categories, String brand, Set<String> brands, String searchName);
  
  /**
   * Gets replenishment products.
   *
   * @param zone       the zone
   * @param zones      the zones
   * @param destZone   the dest zone
   * @param destZones  the dest zones
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @param searchName the search name
   * @return the replenishment products
   */
  @Transaction
  @Query("SELECT * FROM product WHERE (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone='" + AppConstants.ALL + "' OR TRIM(zone)=:zone OR TRIM(zone) IN(:zones)) AND (dest_zone is NULL OR LENGTH(dest_zone)<=0 OR :destZone IS NULL OR LENGTH(:destZone)<=0 OR :destZone='" + AppConstants.ALL + "' OR TRIM(dest_zone)=:destZone OR TRIM(dest_zone) IN(:destZones)) AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(product_name) LIKE '%'||:searchName||'%' OR TRIM(ean) LIKE '%'||:searchName||'%')")
  List<ProductModel> getReplenishmentProducts(String zone, Set<String> zones, String destZone, Set<String> destZones, String category, Set<String> categories, String brand, Set<String> brands, String searchName);
  
  /**
   * Gets replenishment products.
   *
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @return the replenishment products
   */
  @Transaction
  @Query("SELECT * FROM product WHERE (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands))")
  List<ProductModel> getReplenishmentProducts(String category, Set<String> categories, String brand, Set<String> brands);
  
  /**
   * Gets off range products.
   *
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @return the replenishment products
   */
  @Transaction
  @Query("SELECT * FROM product WHERE (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(ean) LIKE '%'||:searchName||'%' OR TRIM(product_name) LIKE '%'||:searchName||'%') ORDER BY COALESCE(is_found,0) ASC,COALESCE(priority,0) DESC")
  List<ProductModel> getOffRangeProducts(String category, Set<String> categories, String brand, Set<String> brands, String searchName);
  
  /**
   * Gets off range products.
   *
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @return the replenishment products
   */
  @Transaction
  @Query("SELECT * FROM product WHERE (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(ean) LIKE '%'||:searchName||'%' OR TRIM(product_name) LIKE '%'||:searchName||'%') ORDER BY  COALESCE(found_qty,0) ASC, COALESCE(priority,0) DESC, ean ASC, COALESCE(product_no,0) ASC LIMIT :limit OFFSET :offSet")
  List<ProductModel> getOffRangeProducts(String category, Set<String> categories, String brand, Set<String> brands, String searchName, Integer offSet, Integer limit);
  
  /**
   * Gets off range products.
   *
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @return the replenishment products
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM product WHERE (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(ean) LIKE '%'||:searchName||'%' OR TRIM(product_name) LIKE '%'||:searchName||'%')")
  Long getOffRangeProductsTotalCount(String category, Set<String> categories, String brand, Set<String> brands, String searchName);
  
  /**
   * Gets total found count.
   *
   * @return the total found count
   */
  @Query("SELECT COALESCE(SUM(found_qty),0) FROM product WHERE (session_type IS NULL OR session_type<=0 OR session_type=:sessionType)")
  Integer getTotalFoundQty(Integer sessionType);
  
  @Transaction
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Query("SELECT SUM(COALESCE(priority,0)) FROM product WHERE (session_type IS NULL OR session_type<=0 OR session_type=:sessionType)")
  LiveData<Integer> getPriorityChanged(Integer sessionType);
  
  @Transaction
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Query("SELECT SUM(COALESCE(priority,0)) FROM product WHERE (session_type IS NULL OR session_type<=0 OR session_type=:sessionType)")
  Integer getPercentChanged(Integer sessionType);
  
  
  /**
   * Gets zonewise size count.
   *
   * @param selColor the sel color
   * @return the zonewise size count
   */
  @Transaction
  @Query("SELECT * FROM product WHERE TRIM(COALESCE(color,'" + AppConstants.DEFAULT_NO_VALUE + "'))=:selColor GROUP BY size,zone,ean")
  List<ProductModel> getZonewiseSizeCount(String selColor);
  
  /**
   * Gets omni products.
   *
   * @param categories the categories
   * @param brand      the brand
   * @param searchName the search name
   * @return the omni products
   */
  @Query("SELECT * FROM product WHERE (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN(:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN(:brands)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(ean) LIKE '%'||:searchName||'%' OR TRIM(product_name) LIKE '%'||:searchName||'%') ORDER BY ean")
  List<ProductModel> getOmniProducts(String category, Set<String> categories, String brand, Set<String> brands, String searchName);
  
  /**
   * Gets omni order products.
   *
   * @param orderNo the order no
   * @return the omni order products
   */
  @Transaction
  @Query("SELECT * FROM product WHERE order_no IS NOT NULL AND LENGTH(order_no)>0 AND TRIM(order_no)=:orderNo ORDER BY ean")
  List<ProductModel> getOmniOrderProducts(String orderNo);
  
  /**
   * Gets omni ean products.
   *
   * @param ean the ean
   * @return the omni order products
   */
  @Transaction
  @Query("SELECT * FROM product WHERE ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE =:ean ORDER BY ean")
  List<ProductModel> getOmniEANProducts(String ean);
  
  @Transaction
  @Query("SELECT * FROM product WHERE ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE =:ean ORDER BY ean")
  ProductModel getProduct(String ean);
  
  @Transaction
  @Query("SELECT * FROM product WHERE ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE IN(:eans) ORDER BY ean")
  List<ProductModel> getProducts(List<String> eans);
  
  /**
   * Search omni header product model.
   *
   * @param query the query
   * @return the list
   */
  @RawQuery
  List<ProductModel> searchOmniHeader(SupportSQLiteQuery query);
  
  /**
   * Search shortage products list.
   *
   * @param searchName the search name
   * @param sortColumn the sort column
   * @return the list
   */
  default List<ProductModel> searchOmniHeader(String searchName, String sortColumn){
    String statement = "SELECT DISTINCT order_no, COALESCE(SUM(qty),0) AS 'qty', ageing_label FROM product WHERE order_no IS NOT NULL AND LENGTH(order_no)>0 " + (isNonEmpty(searchName) ? " AND order_no LIKE '%" + searchName + "%' " : "") + " GROUP BY order_no ORDER BY " + (isNonEmpty(sortColumn) ? sortColumn + ", " : "") + " priority";
    showLog("searchOmniProducts query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    return searchOmniHeader(query);
  }
  
  /**
   * Search shortage products list.
   *
   * @param searchName the search name
   * @param sortColumn the sort column
   * @return the list
   */
  default List<ProductModel> searchOmniHeader(String category, String brand, String searchName, String sortColumn){
    String condition = "";
    if(!chkNull(category, AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(category) IN('" + category.replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
    if(!chkNull(brand, AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(brand) IN('" + brand.replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
    if(isNonEmpty(searchName))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(ean) LIKE '%" + searchName.trim() + "%' ";
    String statement = "SELECT DISTINCT ean, order_no, COALESCE(qty,0) AS 'qty', ageing_label FROM product " + condition + " GROUP BY ean ORDER BY " + (isNonEmpty(sortColumn) ? sortColumn + ", " : "") + " priority";
    showLog("searchOmniProducts query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    return searchOmniHeader(query);
  }
  
  /**
   * Search shortage products list.
   *
   * @param searchName the search name
   * @param sortColumn the sort column
   * @return the list
   */
  default List<ProductModel> searchAssortHeader(String searchName, String sortColumn){
    String statement = "SELECT DISTINCT order_no, COALESCE(SUM(ean_qty),0) AS 'qty', priority FROM product WHERE order_no IS NOT NULL AND LENGTH(order_no)>0 " + (isNonEmpty(searchName) ? " AND order_no LIKE '%" + searchName + "%' " : "") + " GROUP BY order_no ORDER BY " + (isNonEmpty(sortColumn) ? sortColumn + ", " : "") + " priority";
    showLog("searchOmniProducts query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    return searchOmniHeader(query);
  }
  
  /**
   * Search shortage products list.
   *
   * @param searchName the search name
   * @param sortColumn the sort column
   * @return the list
   */
  default List<ProductModel> searchAssortHeader(String category, String brand, String searchName, String sortColumn){
    String condition = "";
    if(!chkNull(category, AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(category) IN('" + category.replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
    if(!chkNull(brand, AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(brand) IN('" + brand.replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
    if(isNonEmpty(searchName))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(ean) LIKE '%" + searchName.trim() + "%' ";
    String statement = "SELECT DISTINCT ean, order_no, COALESCE(ean_qty,0) AS 'qty', priority FROM product " + condition + " GROUP BY ean ORDER BY " + (isNonEmpty(sortColumn) ? sortColumn + ", " : "") + " priority";
    showLog("searchAssortProducts query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    return searchOmniHeader(query);
  }
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product")
  boolean hasData();
  
  /**
   * Gets assort brand list.
   *
   * @return the assort brand list
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(brand) FROM  product WHERE brand IS NOT NULL AND LENGTH(TRIM(brand))>0 AND TRIM(brand) !='" + AppConstants.ALL + "' AND order_no IS NOT NULL AND LENGTH(TRIM(order_no))>0 AND TRIM(order_no)=:assortCode")
  List<String> getAssortBrandList(String assortCode);
  
  /**
   * Gets assort category list.
   *
   * @return the assort category list
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(brand) FROM  product WHERE category IS NOT NULL AND LENGTH(TRIM(category))>0 AND TRIM(category) !='" + AppConstants.ALL + "' AND order_no IS NOT NULL AND LENGTH(TRIM(order_no))>0 AND TRIM(order_no)=:assortCode")
  List<String> getAssortCategoryList(String assortCode);
  
  /**
   * Gets assort products.
   *
   * @param assortCode the assort code
   * @param categories the categories
   * @param brand      the brand
   * @param searchName the search name
   * @return the omni products
   */
  @Query("SELECT * FROM product WHERE (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN(:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN(:brands)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(ean) LIKE '%'||:searchName||'%' OR TRIM(product_name) LIKE '%'||:searchName||'%') AND order_no IS NOT NULL AND LENGTH(TRIM(order_no))>0 AND TRIM(order_no)=:assortCode ORDER BY ean")
  List<ProductModel> getAssortProducts(String assortCode, String category, Set<String> categories, String brand, Set<String> brands, String searchName);
  
  /**
   * Gets total ean count.
   *
   * @param category   the category
   * @param categories the categories
   * @param brand      the brand
   * @param brands     the brands
   * @param searchName the search name
   * @return the total ean count
   */
  @Query("SELECT COALESCE(SUM(ean_qty),0) FROM product WHERE (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand)=:brand OR TRIM(brand) IN (:brands)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(product_name) LIKE '%'||:searchName||'%' OR TRIM(ean) LIKE '%'||:searchName||'%') AND order_no IS NOT NULL AND LENGTH(TRIM(order_no))>0 AND TRIM(order_no)=:assortCode")
  Integer getAssortTotalEANCount(String assortCode, String category, Set<String> categories, String brand, Set<String> brands, String searchName);
  
  @Query("UPDATE product SET priority=:percentage WHERE (session_type IS NULL OR session_type<=0 OR session_type=:sessionType) AND (ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE =:ean)")
  Integer updateRssiPercentage(int sessionType, String ean, int percentage);
  
  @Query("UPDATE product SET priority=0 WHERE (session_type IS NULL OR session_type<=0 OR session_type=:sessionType)")
  Integer resetRssiPercentage(int sessionType);
  
  @Query("UPDATE product SET priority=0,epc='',is_found=0,found_qty=0,is_decoded=0,decode_qty=0 WHERE (session_type IS NULL OR session_type<=0 OR session_type=:sessionType)")
  Integer resetProducts(int sessionType);
}
