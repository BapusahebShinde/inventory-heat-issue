package com.itek.retail.database;

import static com.itek.retail.common.AppCommonMethods.chkNull;
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
import com.itek.retail.model.TripStatus;

import java.util.List;
import java.util.Set;

/**
 * The interface Trip status dao.
 */
@androidx.room.Dao
public interface TripStatusDao{
  
  /**
   * Insert trip status data.
   *
   * @param tripStatus the trip status
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertTripStatusData(TripStatus... tripStatus);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<TripStatus> tripStatus);
  
  /**
   * Update trip status data.
   *
   * @param tripStatus the trip status
   */
  @Update
  Integer updateTripStatusData(TripStatus... tripStatus);
  
  /**
   * Delete trip status data.
   *
   * @param tripStatus the trip status
   */
  @Delete
  Integer deleteTripStatusData(TripStatus... tripStatus);
  
  /**
   * Delete all trip status.
   */
  @Query("DELETE FROM trip_status WHERE type=:type")
  public void deleteAllTripStatus(String type);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM trip_status")
  public void deleteAll();
  
  /**
   * Delete upload all trip status.
   *
   * @param listTripNo the list trip no
   */
  @Query("DELETE FROM trip_status WHERE trip_no IN( :listTripNo) AND type=:type")
  public Integer deleteUploadAllTripStatus(Set<String> listTripNo, String type);
  
  /**
   * Update pending trip status.
   *
   * @param tripNo the trip no
   */
  @Query("UPDATE trip_status SET status='" + AppConstants.TRIP_STATUS_PENDING + "' WHERE TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE = :tripNo AND type=:type")
  public Integer updatePendingTripStatus(String tripNo, String type);
  
  /**
   * Update complete trip status.
   *
   * @param tripNo the trip no
   */
  @Query("UPDATE trip_status SET status='" + AppConstants.TRIP_STATUS_COMPLETED + "' WHERE TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE = :tripNo AND type=:type")
  public Integer updateCompleteTripStatus(String tripNo, String type);
  
  /**
   * Update in progress trip status.
   *
   * @param tripNo the trip no
   */
  @Query("UPDATE trip_status SET status='" + AppConstants.TRIP_STATUS_IN_PROGRESS + "' WHERE TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE = :tripNo AND type=:type")
  public Integer updateInProgressTripStatus(String tripNo, String type);
  
  /**
   * Update processing trip status.
   *
   * @param tripNo the trip no
   */
  @Query("UPDATE trip_status SET status='" + AppConstants.TRIP_STATUS_PROCESSING + "' WHERE TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE = :tripNo AND type=:type")
  public Integer updateProcessingTripStatus(String tripNo, String type);
  
  /**
   * Gets trip type list.
   *
   * @param tripType the trip type
   * @return the trip type list
   */
  @Transaction
  @Query("SELECT * FROM trip_status WHERE TRIM(trip_type) =:tripType AND type=:type")
  public LiveData<List<TripStatus>> getTripTypeList(String tripType, String type);
  
  /**
   * Gets trip list.
   *
   * @return the trip type list
   */
  @Transaction
  @Query("SELECT * FROM trip_status WHERE type=:type AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE  LIKE '%'||:searchName||'%')")
  public List<TripStatus> getTripList(String type,String searchName);
  
 /* @Transaction
  @Query("SELECT * FROM trip_status WHERE type=:type AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE  LIKE '%'||:searchName||'%') ORDER BY :sortBy")
  public List<TripStatus> getTripList(String type,String searchName,String sortBy);*/
  
  /**
   * get trip list.
   *
   * @param query the query
   * @return the list
   */
  @RawQuery
  List<TripStatus> getTripList(SupportSQLiteQuery query);
  
  default List<TripStatus> getTripList(String type, String searchName, String sortColumn){
    String condition = "WHERE status != '"+AppConstants.TRIP_STATUS_COMPLETED+"'";
    if(isNonEmpty(type))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "type = '"+type+"'";
    if(isNonEmpty(searchName))
      condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM(COALESCE(ref_trip_no,trip_no)) LIKE '%'||'"+searchName+"'||'%'";
    //String statement = "SELECT * FROM trip_status "+ condition + (isNonEmpty(sortColumn) ? " ORDER BY " + sortColumn : "");
    //String statement = "SELECT * FROM trip_status "+ condition + (isNonEmpty(sortColumn) ? " ORDER BY " + sortColumn : " ORDER BY trip_date_time DESC");
    String statement = "SELECT * FROM trip_status "+ condition + " ORDER BY " + (isNonEmpty(sortColumn) ? sortColumn +", " :"") + " COALESCE(trip_date_time,'') DESC";
    //String statement = "SELECT * FROM trip_status "+ condition + " ORDER BY COALESCE(trip_date_time,'') DESC " + (isNonEmpty(sortColumn) ? ", "+sortColumn : ""); //fail
    showLog("getTripList query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    List<TripStatus> resultList = getTripList(query);
    return resultList;
  }
  
  /**
   * Gets trip data.
   *
   * @param tripNo the trip no
   * @return the trip data
   */
  @Query("SELECT * FROM trip_status WHERE TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE =:tripNo AND type=:type")
  public TripStatus getTripData(String tripNo, String type);
  
  /**
   * Update hu verify count.
   *
   * @param tripNo the trip no
   */
  @Query("UPDATE trip_status SET verified_hu = (CASE WHEN verified_hu < total_hu THEN verified_hu+1 ELSE verified_hu END) WHERE TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE  = :tripNo AND type=:type")
  public Integer updateHUVerifyCount(String tripNo, String type);
  
  /**
   * Has trip data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM trip_status WHERE type=:type")
  public boolean hasTripData(String type);
  
  /**
   * is trip present boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM trip_status WHERE type=:type AND TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE =:tripNo")
  public boolean isTripPresent(String type, String tripNo);
  
  /**
   * Has in-progress trip boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM trip_status WHERE TRIM(status)='" + AppConstants.TRIP_STATUS_IN_PROGRESS + "' AND type=:type")
  public boolean hasInProgressTrip(String type);
  
  /**
   * get in-progress trip Data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT * FROM trip_status WHERE TRIM(status)='" + AppConstants.TRIP_STATUS_IN_PROGRESS + "' AND type=:type ORDER BY pk_id DESC LIMIT 1")
  public TripStatus getInProgressTripData(String type);
  
  /**
   * Has processing trip boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM trip_status WHERE TRIM(status)='" + AppConstants.TRIP_STATUS_PROCESSING + "' AND type=:type")
  public boolean hasProcessingTrip(String type);
  
  /**
   * get processing trip Data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT * FROM trip_status WHERE TRIM(status)='" + AppConstants.TRIP_STATUS_PROCESSING + "' AND type=:type ORDER BY pk_id DESC LIMIT 1")
  public TripStatus getProcessingTripData(String type);
  
  
  /**
   * Is all trips pending boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)<=0 FROM trip_status WHERE TRIM(status)!='" + AppConstants.TRIP_STATUS_PENDING + "' AND type=:type")
  public boolean isAllTripsPending(String type);
  
  /**
   * Gets completed trips count.
   *
   * @return the completed trips count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM trip_status WHERE TRIM(status)='" + AppConstants.TRIP_STATUS_COMPLETED + "' AND type=:type")
  public Integer getCompletedTripsCount(String type);
  
  /**
   * Gets completed trips.
   *
   * @return the completed trips
   */
  @Transaction
  @Query("SELECT trip_no FROM trip_status WHERE TRIM(status)='" + AppConstants.TRIP_STATUS_COMPLETED + "' AND type=:type")
  public List<String> getCompletedTrips(String type);
  
  /**
   * Gets trips status counts.
   *
   * @return the trips status counts
   */
  @Query("SELECT COALESCE(SUM(CASE WHEN status='" + AppConstants.TRIP_STATUS_COMPLETED + "' THEN 1 ELSE 0 END ),0)||','||COALESCE(SUM(CASE WHEN TRIM(status)='" + AppConstants.TRIP_STATUS_IN_PROGRESS + "' THEN 1 ELSE 0 END ),0)||','||COALESCE(SUM(CASE WHEN TRIM(status)='" + AppConstants.TRIP_STATUS_PENDING + "' THEN 1 ELSE 0 END ),0) FROM trip_status WHERE type=:type")
  public String getTripsStatusCounts(String type);
  
  /**
   * Gets trip types.
   *
   * @return the trip types
   */
  @Transaction
  @Query("SELECT DISTINCT trip_type FROM trip_status WHERE type=:type")
  public List<String> getTripTypes(String type);
  
  /**
   * Gets trip type.
   *
   * @param tripNo the trip no
   * @return the trip type
   */
  @Query("SELECT DISTINCT trip_type FROM trip_status WHERE TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE =:tripNo AND type=:type")
  public String getTripType(String tripNo, String type);
  
  /**
   * Gets trips count.
   *
   * @return the trips count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM trip_status WHERE type=:type")
  public Integer getTripsCount(String type);
  
  /**
   * Update trip upload reason.
   *
   * @param tripNo the trip no
   * @param reason the reason
   */
  @Query("UPDATE trip_status SET reason=:reason WHERE TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE = :tripNo AND type=:type")
  public Integer updateTripUploadReason(String tripNo, String reason, String type);
  
  @Query("UPDATE trip_status SET total_hu=COALESCE(total_hu,0)+1 WHERE TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE = :tripNo AND type=:type")
  public Integer updateTripHUCount(String tripNo, String type);
  
  @Query("UPDATE trip_status SET completed_hu=COALESCE(completed_hu,0)+1 WHERE TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE = :tripNo AND type=:type")
  public Integer updateTripHUCompletedCount(String tripNo, String type);
  
  @Query("UPDATE trip_status SET total_hu=:huCount,completed_hu=:huCompleteCount WHERE TRIM(COALESCE(ref_trip_no,trip_no)) COLLATE NOCASE = :tripNo AND type=:type")
  public Integer updateTripHUCounts(String tripNo, String type, int huCount, int huCompleteCount);
  
  /**
   * Gets trips completed count.
   *
   * @return the trips completed count
   */
  @Query("SELECT COALESCE(COUNT(DISTINCT trip_no),0) FROM trip_status WHERE type=:type AND status='" + AppConstants.TRIP_STATUS_COMPLETED + "'")
  public Integer getTripsCompletedCount(String type);
  
  /**
   * Gets trips all count.
   *
   * @return the trips all count
   */
  @Query("SELECT COALESCE(COUNT(DISTINCT trip_no),0) FROM trip_status WHERE type=:type")
  public Integer getTripsAllCount(String type);
}
