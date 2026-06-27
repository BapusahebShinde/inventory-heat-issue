package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.SearchLog;

import java.util.List;

/**
 * The interface Search log dao.
 */
@androidx.room.Dao
public interface SearchLogDao{
  
  /**
   * Insert.
   *
   * @param searchLogs the search logs
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(SearchLog... searchLogs);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<SearchLog> listSearchLogs);
  
  /**
   * Update.
   *
   * @param searchLogs the search logs
   */
  @Update
  Integer update(SearchLog... searchLogs);
  
  /**
   * Delete.
   *
   * @param searchLogs the search logs
   */
  @Delete
  Integer delete(SearchLog... searchLogs);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM search_log")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM search_log")
  List<SearchLog> getAll();
  
  @Transaction
  @Query("UPDATE search_log SET session_id=:sessionId,durationTime=:duration WHERE TRIM(ean) COLLATE NOCASE =:ean AND type=:type AND transaction_id=:transactionId")
  Integer updateDuration(String sessionId, String ean, String type, String transactionId, String duration);
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM zones")
  Integer getTableSize();
  
  /**
   * Delete logs integer.
   *
   * @param logIds the log ids
   * @return the integer
   */
  @Query("DELETE FROM search_log WHERE logNo IN(:logIds)")
  Integer deleteLogs(List<Integer> logIds);
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM search_log")
  boolean hasData();
  
}
