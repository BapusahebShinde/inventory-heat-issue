package com.itek.retail.database;

import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.itek.retail.common.AppConstants;
import com.itek.retail.model.HUStatus;
import com.itek.retail.model.TripStatus;

import java.util.List;
import java.util.Set;

/**
 * The interface HU status dao.
 */
@androidx.room.Dao
public interface HUStatusDao{
  
  /**
   * Insert hu status data.
   *
   * @param huStatus the hu status
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertHUStatusData(HUStatus... huStatus);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<HUStatus> huStatus);
  
  /**
   * Update hu status data.
   *
   * @param huStatus the hu status
   */
  @Update
  Integer updateHUStatusData(HUStatus... huStatus);
  
  /**
   * Delete hu status data.
   *
   * @param huStatus the hu status
   */
  @Delete
  Integer deleteHUStatusData(HUStatus... huStatus);
  
  /**
   * Delete all hu status.
   */
  @Query("DELETE FROM hu_status WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public void deleteAllTripHus(String type,String tripNo);
  
  /**
   * Delete all hu status.
   */
  @Query("DELETE FROM hu_status WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND status!='"+AppConstants.HU_STATUS_COMPLETE+"'")
  public void deleteAllNonCompletedTripHus(String type,String tripNo);
  
  /**
   * Delete all hu status.
   */
  @Query("DELETE FROM hu_status WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)=:huNo")
  public void deleteAllTripHuData(String type,String tripNo,String huNo);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM hu_status")
  public void deleteAll();
  
  /**
   * Update pending hu status.
   *
   * @param huNo the hu no
   */
  @Query("UPDATE hu_status SET status='" + AppConstants.HU_STATUS_PENDING + "' WHERE TRIM(hu_no)= :huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer updatePendingHUStatus(String tripNo, String huNo, String type);
  
  /**
   * Update reject hu status.
   *
   * @param huNo the hu no
   */
  @Query("UPDATE hu_status SET status='" + AppConstants.HU_STATUS_REJECT + "' WHERE TRIM(hu_no)= :huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer updateRejectHUStatus(String tripNo, String huNo, String type);
  
  /**
   * Update reject hu status.
   *
   * @param huNo the hu no
   */
  @Query("UPDATE hu_status SET scan_qty=:scanCount,status='" + AppConstants.HU_STATUS_REJECT + "' WHERE TRIM(hu_no)= :huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer updateRejectHUStatus(String tripNo, String huNo, String type,int scanCount);
  
  /**
   * Update complete hu status.
   *
   * @param huNo the hu no
   */
  @Query("UPDATE hu_status SET status='" + AppConstants.HU_STATUS_COMPLETE + "' WHERE TRIM(hu_no)= :huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer updateCompleteHUStatus(String tripNo, String huNo, String type);
  
  /**
   * Update complete hu status.
   *
   * @param huNo the hu no
   */
  @Query("UPDATE hu_status SET scan_qty=:scanCount,status='" + AppConstants.HU_STATUS_COMPLETE + "' WHERE TRIM(hu_no)= :huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer updateCompleteHUStatus(String tripNo, String huNo, String type, int scanCount);
  
  /**
   * Update in progress hu status.
   *
   * @param huNo the hu no
   */
  @Query("UPDATE hu_status SET status='" + AppConstants.HU_STATUS_IN_PROGRESS + "' WHERE TRIM(hu_no)= :huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer updateInProgressHUStatus(String type, String tripNo, String huNo);
  
  @Query("UPDATE hu_status SET exp_qty=exp_qty+:expQty,scan_qty=scan_qty+:scanQty WHERE TRIM(hu_no)= :huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer updateHUStatusQuantities(String tripNo, String huNo, String type,int expQty,int scanQty);
  
  
  @Query("UPDATE hu_status SET exp_qty=:expQty WHERE TRIM(hu_no)= :huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer updateHUStatusExpQty(String tripNo, String huNo, String type,int expQty);
  
  @Query("UPDATE hu_status SET scan_qty=:scanQty WHERE TRIM(hu_no)= :huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer updateHUStatusScanQty(String tripNo, String huNo, String type,int scanQty);
  
  @Query("UPDATE hu_status SET exp_qty=:expQty,scan_qty=:scanQty WHERE TRIM(hu_no)= :huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer updateHUStatusExpScanQty(String tripNo, String huNo, String type,int expQty,int scanQty);
  
  /**
   * Gets hu list.
   *
   * @return the hu type list
   */
  @Transaction
  @Query("SELECT * FROM hu_status WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(hu_no) LIKE '%'||:searchName||'%')")
  public List<HUStatus> getHuList(String type,String tripNo,String searchName);
  
  /**
   * get hu list.
   *
   * @param query the query
   * @return the list
   */
  @RawQuery
  List<HUStatus> getHuList(SupportSQLiteQuery query);
  
  default List<HUStatus> getHuList(String type,String tripNo, String searchName, String sortColumn){
    String condition = "";
    if(isNonEmpty(type))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "type = '"+type+"'";
    if(isNonEmpty(tripNo))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(trip_no) COLLATE NOCASE  = '"+tripNo+"'";
    if(isNonEmpty(searchName))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(hu_no) LIKE '%'||'"+searchName+"'||'%' ";
    String statement = "SELECT * FROM hu_status "+ condition + (isNonEmpty(sortColumn) ? " ORDER BY " + sortColumn : "");
    showLog("getHuList query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    List<HUStatus> resultList = getHuList(query);
    return resultList;
  }
  
  /**
   * Gets hu data.
   *
   * @param huNo the hu no
   * @return the hu data
   */
  @Query("SELECT * FROM hu_status WHERE TRIM(hu_no) =:huNo AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public HUStatus getHUData(String type, String tripNo, String huNo);
  
  /**
   * Has hu data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM hu_status WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public boolean hasHUData(String type, String tripNo);
  
  /**
   * is hu present boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM hu_status WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)= :huNo")
  public boolean isHuPresent(String type, String tripNo, String huNo);
  
  /**
   * Has in-progress hu boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM hu_status WHERE TRIM(status)='" + AppConstants.HU_STATUS_IN_PROGRESS + "' AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public boolean hasInProgressHU(String type, String tripNo);
  
  /**
   * get in-progress hu Data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT * FROM hu_status WHERE TRIM(status)='" + AppConstants.HU_STATUS_IN_PROGRESS + "' AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo ORDER BY pk_id DESC LIMIT 1")
  public HUStatus getInProgressHUData(String type, String tripNo);
  
  
  /**
   * Is all hus pending boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)<=0 FROM hu_status WHERE TRIM(status)!='" + AppConstants.HU_STATUS_PENDING + "' AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public boolean isAllHUsPending(String type,String tripNo);
  
  /**
   * Gets completed hus count.
   *
   * @return the completed hus count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM hu_status WHERE TRIM(status)='" + AppConstants.HU_STATUS_COMPLETE + "' AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer getCompletedHUsCount(String type,String tripNo);
  
  /**
   * Gets completed hus.
   *
   * @return the completed hus
   */
  @Transaction
  @Query("SELECT hu_no FROM hu_status WHERE TRIM(status)='" + AppConstants.HU_STATUS_COMPLETE + "' AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public List<String> getCompletedHUs(String type,String tripNo);
  
  
  /**
   * Gets rejected hus count.
   *
   * @return the rejected hus count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM hu_status WHERE TRIM(status)='" + AppConstants.HU_STATUS_REJECT + "' AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer getRejectedHUsCount(String type,String tripNo);
  
  /**
   * Gets rejected hus.
   *
   * @return the rejected hus
   */
  @Transaction
  @Query("SELECT hu_no FROM hu_status WHERE TRIM(status)='" + AppConstants.HU_STATUS_REJECT + "' AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public List<String> getRejectedHUs(String type,String tripNo);
  
  /**
   * Gets pending hus count.
   *
   * @return the pending hus count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM hu_status WHERE TRIM(status)='" + AppConstants.HU_STATUS_PENDING + "' AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer getPendingHUsCount(String type,String tripNo);
  
  /**
   * Gets pending hus.
   *
   * @return the pending hus
   */
  @Transaction
  @Query("SELECT hu_no FROM hu_status WHERE TRIM(status)='" + AppConstants.HU_STATUS_PENDING + "' AND type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public List<String> getPendingHUs(String type,String tripNo);
  
  /**
   * Gets hus status counts.
   *
   * @return the hus status counts
   */
  @Query("SELECT COALESCE(SUM(CASE WHEN status='" + AppConstants.HU_STATUS_COMPLETE + "' THEN 1 ELSE 0 END ),0)||','||COALESCE(SUM(CASE WHEN TRIM(status)='" + AppConstants.HU_STATUS_IN_PROGRESS + "' THEN 1 ELSE 0 END ),0)||','||COALESCE(SUM(CASE WHEN TRIM(status)='" + AppConstants.HU_STATUS_PENDING + "' THEN 1 ELSE 0 END ),0) FROM hu_status WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public String getTripsStatusCounts(String type,String tripNo);
  
  /**
   * Gets hus count.
   *
   * @return the hus count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM hu_status WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo")
  public Integer getHUCount(String type,String tripNo);
  
  /**
   * Update hu upload reason.
   *
   * @param huNo the hu no
   * @param reason the reason
   */
  @Query("UPDATE hu_status SET reason=:reason WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)= :huNo")
  public Integer updateHuUploadReason(String type, String tripNo, String huNo, String reason);
  
  
  /**
   * Update hu upload status reason.
   *
   * @param huNo the hu no
   * @param reason the reason
   */
  @Query("UPDATE hu_status SET status=:status,reason=:reason WHERE type=:type AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)= :huNo")
  public Integer updateHuUploadStatusReason(String type, String tripNo, String huNo, String reason,String status);
  
  
}
