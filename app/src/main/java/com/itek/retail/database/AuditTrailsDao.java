package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.AuditTrailsLog;

import java.util.List;

@androidx.room.Dao
public interface AuditTrailsDao{
  
  /**
   * Insert.
   *
   * @param auditTrailsLog the search logs
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(AuditTrailsLog... auditTrailsLog);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<AuditTrailsLog> listAuditTrailsLogs);
  
  /**
   * Update.
   *
   * @param auditTrailsLog the Audit Trails Log
   */
  @Update
  Integer update(AuditTrailsLog... auditTrailsLog);
  
  /**
   * Delete.
   *
   * @param auditTrailsLog the Audit Trails Log
   */
  @Delete
  Integer delete(AuditTrailsLog... auditTrailsLog);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM audit_trail_log")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM audit_trail_log")
  List<AuditTrailsLog> getAll();
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM zones")
  Integer getTableSize();
  
  /**
   * Delete all logs.
   *
   * @return the integer
   */
  @Query("DELETE FROM audit_trail_log")
  void deleteAllLogs();
  
  /**
   * Delete logs integer.
   *
   * @param logIds the log ids
   * @return the integer
   */
  @Query("DELETE FROM audit_trail_log WHERE log_no IN(:logIds)")
  Integer deleteAuditTrailsLogs(List<Integer> logIds);
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM audit_trail_log")
  boolean hasData();
  
  /**
   * Has data string.
   *
   * @return the string
   */
  @Query("SELECT action_state FROM audit_trail_log ORDER BY log_no DESC LIMIT 1")
  String getLastAction();
  
}