package com.itek.retail.database;

import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.itek.retail.common.AppConstants;
import com.itek.retail.model.HUDetails;
import com.itek.retail.model.MultiQtyModel;

import java.util.List;

/**
 * The interface HU details dao.
 */
@androidx.room.Dao
public interface HUDetailsDao{
  
  /**
   * Insert hu status data.
   *
   * @param huDetails the hu status
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertHUDetailsData(HUDetails... huDetails);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<HUDetails> huDetails);
  
  /**
   * Update hu status data.
   *
   * @param huDetails the hu status
   */
  @Update
  Integer updateHUDetailsData(HUDetails... huDetails);
  
  /**
   * Delete hu status data.
   *
   * @param huDetails the hu status
   */
  @Delete
  Integer deleteHUDetailsData(HUDetails... huDetails);
  
  /**
   * Delete all hu status.
   */
  @Query("DELETE FROM hu_details WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public void deleteAllTripHus(String type, String tripNo);
  
  /**
   * Delete all hu status.
   */
  @Query("DELETE FROM hu_details WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public void deleteAllNonCompletedTripHus(String type, String tripNo);
  
  /**
   * Delete all hu status.
   */
  @Query("DELETE FROM hu_details WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)=:huNo")
  public void deleteAllTripHus(String type, String tripNo, String huNo);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM hu_details")
  public void deleteAll();
  
  /**
   * Gets hu data.
   *
   * @param huNo the hu no
   * @return the hu data
   */
  @Query("SELECT * FROM hu_details WHERE TRIM(hu_no) =:huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public HUDetails getHUDetails(String type, String tripNo, String huNo);
  
  /**
   * Gets hu data.
   *
   * @param huNo the hu no
   * @return the hu data
   */
  @Query("SELECT SUM(COALESCE(exp_qty,0)) FROM hu_details WHERE TRIM(hu_no) =:huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer getTotalExpQty(String type, String tripNo, String huNo);
  
  /**
   * Has hu data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM hu_details WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)=:huNo")
  public boolean hasHUDetails(String type, String tripNo, String huNo);
  
  /**
   * is hu present boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM hu_details WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)= :huNo")
  public boolean isHuPresent(String type, String tripNo, String huNo);
  
  /**
   * Has article data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(article_code),0)>0 FROM hu_details WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)=:huNo AND article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"'")
  public boolean hasArticleData(String type, String tripNo, String huNo);
  
  /**
   * Has epc data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(rfid),0)>0 FROM hu_details WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)=:huNo AND rfid IS NOT NULL AND LENGTH(TRIM(rfid))>0")
  public boolean hasEpcData(String type, String tripNo, String huNo);
  
  
  @Query("SELECT TRIM(COALESCE(rfid,'')) FROM hu_details WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)=:huNo")
  public List<String> getEpcData(String type, String tripNo, String huNo);
  
  
  @Transaction
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Query("SELECT (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) AS 'title',COALESCE(SUM(exp_qty),0) AS 'total',COALESCE(SUM(scan_qty),0) AS 'found' FROM hu_details WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)= :huNo GROUP BY (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) ORDER BY (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) DESC")
  List<MultiQtyModel> getHUDisplayDetails(String type, String tripNo, String huNo);
  
  @Transaction
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Query("SELECT (CASE WHEN :isArticleBasedTrip AND article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) AS 'title',COALESCE(SUM(exp_qty),0) AS 'total',COALESCE(SUM(scan_qty),0) AS 'found' FROM hu_details WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)= :huNo GROUP BY (CASE WHEN :isArticleBasedTrip AND article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) ORDER BY (CASE WHEN :isArticleBasedTrip AND article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) DESC")
  List<MultiQtyModel> getHUDisplayDetails(String type, String tripNo, String huNo,boolean isArticleBasedTrip);
  
  
  /**
   * get hu list.
   *
   * @param query the query
   * @return the list
   */
  @RawQuery
  List<MultiQtyModel> getHUDisplayDetails(SupportSQLiteQuery query);
  
  default List<MultiQtyModel> getHUDisplayDetails(String type,String tripNo, String huNo, String sortColumn){return getHUDisplayDetails(type,tripNo,huNo,true,sortColumn);}
  default List<MultiQtyModel> getHUDisplayDetails(String type,String tripNo, String huNo,boolean isArticleBasedTrip, String sortColumn){
    String condition = "";
    if(isNonEmpty(type))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "type = '"+type+"'";
    if(isNonEmpty(tripNo))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(trip_no) COLLATE NOCASE = '"+tripNo+"'";
    if(isNonEmpty(huNo))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(hu_no) = '"+huNo+"'";
    //String statement = "SELECT (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) AS 'title',COALESCE(SUM(exp_qty),0) AS 'total',COALESCE(SUM(scan_qty),0) AS 'found' FROM hu_details "+ condition + " GROUP BY (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END)" + (isNonEmpty(sortColumn) ? " ORDER BY " + sortColumn : "");
    String statement = "SELECT (CASE WHEN "+isArticleBasedTrip+" AND article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) AS 'title',COALESCE(SUM(exp_qty),0) AS 'total',COALESCE(SUM(scan_qty),0) AS 'found' FROM hu_details "+ condition + " GROUP BY (CASE WHEN "+isArticleBasedTrip+" AND article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END)" + (isNonEmpty(sortColumn) ? " ORDER BY " + sortColumn : "");
    showLog("getHUDisplayDetails query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    List<MultiQtyModel> resultList = getHUDisplayDetails(query);
    return resultList;
  }
  
  
  @Query("SELECT COALESCE(COUNT(1),0) FROM hu_details")
  public int getTableSize();
  
}
