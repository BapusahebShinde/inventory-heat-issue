package com.itek.retail.database;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;
import static com.itek.retail.common.AppCommonMethods.toUnderScoreCase;

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
import com.itek.retail.model.ProductInvFilterModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * The interface Product Inventory Filter dao.
 */
@androidx.room.Dao
public interface ProductInvFilterDao{
  
  /**
   * Insert.
   *
   * @param productInvFilterModels the product_inv_filter models
   */
  @Insert
  void insert(ProductInvFilterModel... productInvFilterModels);
  
  @Insert
  void insertAll(List<ProductInvFilterModel> productInvFilterModels);
  
  /**
   * Update.
   *
   * @param productInvFilterModels the product_inv_filter models
   */
  @Update
  Integer update(ProductInvFilterModel... productInvFilterModels);
  
  /**
   * Delete.
   *
   * @param productInvFilterModels the product_inv_filter models
   */
  @Delete
  Integer delete(ProductInvFilterModel... productInvFilterModels);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM product_inv_filter")
  void deleteAll();
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM product_inv_filter WHERE session_type IS NULL OR session_type<=0 OR session_type=:sessionType")
  void deleteAll(int sessionType);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM product_inv_filter WHERE TRIM(ean) COLLATE NOCASE =:ean")
  Integer deleteEan(String ean);
  
  @RawQuery
  Long getInvCount(SupportSQLiteQuery query);
  
  default Long getInvCount(String zone, HashMap<String,Set<String>> filters){
    String condition = "";
    if(!chkNull(zone, AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(zone) IN('" + zone.replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
    if(isNonEmpty(filters)){
      for(String key:filters.keySet()){
        if(isNonEmpty(key) && isNonEmpty(filters.get(key))){
          String queryKey = toUnderScoreCase(key);
          condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM("+queryKey+") IN('" + filters.get(key).toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
        }
      }
    }
    String statement = "SELECT COALESCE(SUM(qty),0) FROM product_inv_filter " + condition;
    showLog("getInvCount query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    return getInvCount(query);
  }
  
  @RawQuery
  List<String> getList(SupportSQLiteQuery query);
  
  default List<String> getList(String zone, String column, HashMap<String,Set<String>> filters){
    String condition = "";
    String queryColumn = toUnderScoreCase(column);
    if(!chkNull(zone, AppConstants.ALL).equalsIgnoreCase(AppConstants.ALL))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(zone) IN('" + zone.replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
    if(isNonEmpty(filters)){
      for(String key:filters.keySet()){
        if(isNonEmpty(key) && !key.equalsIgnoreCase(column) && isNonEmpty(filters.get(key))){
          String queryKey = toUnderScoreCase(key);
          condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM("+queryKey+") IN('" + filters.get(key).toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
        }
      }
    }
    String statement = "SELECT DISTINCT TRIM(COALESCE("+queryColumn+",'')) FROM product_inv_filter " + condition + "ORDER BY " + queryColumn + " ASC";
    showLog("getList query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    //List<String> resultList = chkNull(getList(query),new ArrayList<String>(0));
    //if(!column.matches("(?i)(ean|qty)")) resultList.add(0, AppConstants.ALL);
    return getList(query);
  }
  
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
    String statement = "SELECT brand AS 'title',COALESCE(COUNT(*),0) AS 'total',COALESCE(SUM(is_found),0) AS 'found' FROM product_inv_filter " + condition + " GROUP BY TRIM(brand) " + (isNonEmpty(sortColumn) ? "ORDER BY " + sortColumn : "");
    showLog("searchShortageProducts query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    List<MultiQtyModel> resultList = searchShortageProducts(query);
    if(isNonEmpty(resultList))
      resultList.add(0, searchShortageHeader(new SimpleSQLiteQuery("SELECT '" + AppConstants.ALL + "' AS 'title',COALESCE(COUNT(*),0) AS 'total',COALESCE(SUM(is_found),0) AS 'found' FROM product_inv_filter " + condition, new Object[]{})));
    return resultList;
  }
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product_inv_filter")
  boolean hasData();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM product_inv_filter WHERE session_type=:sessionType")
  boolean hasData(int sessionType);
}
