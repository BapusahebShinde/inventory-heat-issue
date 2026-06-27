package com.itek.retail.database;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.RFIDSession;

import java.util.List;

/**
 * The interface Rfid session dao.
 */
@androidx.room.Dao
public interface RFIDSessionDao{
  
  /**
   * Insert.
   *
   * @param rfidSessions the rfid sessions
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(RFIDSession... rfidSessions);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<RFIDSession> listRFIDSessions);
  
  /**
   * Update.
   *
   * @param rfidSession the rfid session
   */
  @Update
  Integer update(RFIDSession... rfidSession);
  
  /**
   * Delete.
   *
   * @param rfidSession the rfid session
   */
  @Delete
  Integer delete(RFIDSession rfidSession);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM rfid_session")
  public void deleteAll();
  
  /**
   * Delete all except.
   */
  @Query("DELETE FROM rfid_session WHERE session_type NOT IN (1,2,12,13,18,28)")
  public Integer deleteAllExcept();
  
  /**
   * Delete all.
   *
   * @param sessionId the session id
   */
  @Query("DELETE FROM rfid_session WHERE TRIM(session_id) = :sessionId")
  public void deleteAll(String sessionId);
  
  /**
   * Delete all.
   *
   * @param sessionType the session type
   */
  @Query("DELETE FROM rfid_session WHERE session_type = :sessionType")
  public void deleteAll(int sessionType);
  
  /**
   * Gets all session data.
   *
   * @return the all session data
   */
  @Transaction
  @Query("SELECT * FROM rfid_session")
  List<RFIDSession> getAllSessionData();
  
  /**
   * Gets all session count.
   *
   * @return the all session count
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM rfid_session")
  LiveData<Integer> getAllSessionCount();
  
  /**
   * Gets all session types.
   *
   * @return the all session types
   */
  @Transaction
  @Query("SELECT session_type FROM rfid_session")
  LiveData<List<Integer>> getAllSessionTypes();
  
  /**
   * Has existing active session boolean.
   *
   * @param userId the user id
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM rfid_session WHERE session_type>0 AND (session_type<3 OR session_type==12 OR session_type==13) AND user_id=:userId")
  Boolean hasExistingActiveSession(String userId);
  
  /**
   * Gets session.
   *
   * @param sessionId the session id
   * @return the session
   */
  @Transaction
  @Query("SELECT * FROM rfid_session WHERE TRIM(session_id) = :sessionId")
  RFIDSession getSession(String sessionId);
  
  /**
   * Gets session.
   *
   * @param sessionId   the session id
   * @param sessionType the session type
   * @return the session
   */
  @Transaction
  @Query("SELECT * FROM rfid_session WHERE TRIM(session_id) = :sessionId AND session_type = :sessionType")
  RFIDSession getSession(String sessionId, int sessionType);
  
  /**
   * Gets current session.
   *
   * @param sessionType the session type
   * @return the current session
   */
  @Transaction
  @Query("SELECT * FROM rfid_session WHERE session_type = :sessionType")
  RFIDSession getCurrentSession(int sessionType);
  
  /**
   * has current session.
   *
   * @param sessionType the session type
   * @return the boolean
   */
  @Query("SELECT COUNT(*)>0 FROM rfid_session WHERE session_type = :sessionType")
  boolean hasCurrentSession(int sessionType);
  
  /**
   * Gets current session id.
   *
   * @param sessionType the session type
   * @return the current session id
   */
  @Query("SELECT session_id FROM rfid_session WHERE session_type = :sessionType")
  String getCurrentSessionID(int sessionType);
  
  /**
   * Update active session.
   *
   * @param isActive    the is active
   * @param sessionType the session type
   */
  @Query("Update rfid_session set is_active = :isActive WHERE session_type=:sessionType")
  Integer updateActiveSession(boolean isActive, int sessionType);
}
