package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.UploadInventory;

import java.util.List;
import java.util.Set;

/**
 * The interface UploadInventory dao.
 */
@androidx.room.Dao
public interface UploadInventoryDao{
  
  /**
   * Insert upload inventory data.
   *
   * @param uploadInventories the upload inventories
   */
  @Insert
  void insertUploadInventoryData(UploadInventory... uploadInventories);
  
  @Insert
  void insertAll(List<UploadInventory> listUploadInventory);
  
  /**
   * Update upload inventory data.
   *
   * @param uploadInventories the upload inventories
   */
  @Update
  Integer updateUploadInventoryData(UploadInventory... uploadInventories);
  
  /**
   * Delete upload inventory data.
   *
   * @param uploadInventories the upload inventories
   */
  @Delete
  Integer deleteUploadInventoryData(UploadInventory... uploadInventories);
  
  /**
   * Gets all upload inventory data.
   *
   * @return the all upload inventory data
   */
  @Transaction
  @Query("SELECT * FROM upload_inventory WHERE session_type = :sessionType AND is_uploaded<=0")
  List<UploadInventory> getAllUploadInventoryData(int sessionType);
  
  @Transaction
  @Query("SELECT * FROM upload_inventory WHERE TRIM(session_id) = :sessionId AND is_uploaded<=0")
  List<UploadInventory> getAllUploadInventoryData(String sessionId);
  
  @Transaction
  @Query("SELECT * FROM upload_inventory WHERE TRIM(session_id) = :sessionId AND is_found>0 AND is_uploaded<=0")
  List<UploadInventory> getAllVerifiedUploadInventoryData(String sessionId);
  
  @Transaction
  @Query("SELECT * FROM upload_inventory WHERE is_uploaded<=0 ORDER BY session_type")
  List<UploadInventory> getAllUploadInventoryData();
  
  /**
   * Gets non uploaded count.
   *
   * @param sessionId the session id
   * @return the non uploaded count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM upload_inventory WHERE TRIM(session_id) = :sessionId AND is_uploaded<=0")
  Integer getNonUploadedCount(String sessionId);
  
  /**
   * Gets non uploaded carton count.
   *
   * @param batchId the batch id
   * @return the non uploaded carton count
   */
  @Query("SELECT COALESCE(COUNT(DISTINCT remark),0) FROM upload_inventory WHERE TRIM(fifo_date) = :batchId AND is_uploaded<=0")
  Integer getNonUploadedCartonCountFromBatchId(String batchId);
  
  /**
   * Gets carton count.
   *
   * @param batchId the batch id
   * @return the carton count
   */
  @Query("SELECT COALESCE(COUNT(DISTINCT remark),0) FROM upload_inventory WHERE TRIM(fifo_date) = :batchId")
  Integer getCartonCountFromBatchId(String batchId);
  
  /**
   * Gets carton count.
   *
   * @param batchId the batch id
   * @return the carton count
   */
  @Query("SELECT COALESCE(COUNT(DISTINCT remark),0) FROM upload_inventory WHERE TRIM(fifo_date) = :batchId AND is_uploaded>0")
  Integer getUploadedCartonCountFromBatchId(String batchId);
  
  /**
   * Gets uploaded cartons.
   *
   * @param batchId the batch id
   * @return the carton count
   */
  @Query("SELECT DISTINCT remark FROM upload_inventory WHERE is_uploaded>0 AND remark IS NOT NULL AND LENGTH(TRIM(remark))>0 AND TRIM(fifo_date) = :batchId ORDER BY uino ASC")
  List<String> getUploadedCartonsFromBatchId(String batchId);
  
  
  /**
   * Delete uploaded cartons.
   */
  @Query("DELETE FROM upload_inventory WHERE is_uploaded>0 AND TRIM(fifo_date) = :batchId AND remark IN(:listCartons)")
  Integer deleteUploaded(String batchId,List<String> listCartons);
  
  /**
   * Gets non uploaded count.
   *
   * @return the non uploaded count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM upload_inventory WHERE session_type = :sessionType AND is_uploaded<=0")
  Integer getNonUploadedCount(int sessionType);
  
  /**
   * Gets non uploaded count.
   *
   * @return the non uploaded count
   */
  @Transaction
  @Query("SELECT DISTINCT session_id FROM upload_inventory WHERE session_type = :sessionType AND is_uploaded<=0")
  List<String> getSessionIds(int sessionType);
  
  /**
   * Gets non uploaded count.
   *
   * @return the non uploaded count
   */
  @Transaction
  @Query("SELECT DISTINCT session_id FROM upload_inventory WHERE session_type = :sessionType AND TRIM(fifo_date)=:batchId  AND is_uploaded<=0")
  List<String> getSessionIdsFromBatchId(int sessionType,String batchId);
  
  /**
   * Gets non uploaded count.
   *
   * @return the non uploaded count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM upload_inventory WHERE is_uploaded<=0")
  Integer getNonUploadedCount();
  
  /**
   * Update uploaded.
   *
   * @param sessionId the session id
   */
  @Query("UPDATE upload_inventory SET is_uploaded=1 WHERE TRIM(session_id) = :sessionId AND is_uploaded<=0")
  Integer updateUploaded(String sessionId);
  
  /**
   * Delete uploaded.
   */
  @Query("DELETE FROM upload_inventory WHERE is_uploaded>0")
  Integer deleteUploaded();
  
  /**
   * Update uploaded.
   *
   * @param sessionId the session id
   * @param tids      the tids
   */
  @Query("UPDATE upload_inventory SET is_uploaded=1 WHERE TRIM(session_id) = :sessionId AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) IN (:tids) AND is_uploaded<=0")
  Integer updateUploaded(String sessionId, Set<String> tids);
  
  @Query("UPDATE upload_inventory SET is_uploaded=1 WHERE session_type = :sessionType AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) IN (:tids) AND is_uploaded<=0")
  Integer updateUploaded(int sessionType, Set<String> tids);
  
  /**
   * Update uploaded.
   *
   * @param sessionId the session id
   * @param tid     the tid
   * @param newEpc     the new epc
   */
  @Query("UPDATE upload_inventory SET is_uploaded=1 WHERE TRIM(session_id) = :sessionId AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) = :tid AND new_epc IS NOT NULL AND LENGTH(new_epc)>0 AND TRIM(new_epc) = :newEpc AND is_uploaded<=0")
  Integer updateUploaded(String sessionId, String tid, String newEpc);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM upload_inventory")
  public void deleteAll();
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM upload_inventory WHERE session_type=:sessionType")
  public void deleteAll(int sessionType);
  
  /**
   * Update upload retry count.
   *
   * @param sessionId the session id
   * @param tids      the tids
   */
  @Query("UPDATE upload_inventory SET upload_retry_count=upload_retry_count+1 WHERE TRIM(session_id) = :sessionId AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) IN (:tids) AND is_uploaded<=0")
  Integer updateUploadRetryCount(String sessionId, Set<String> tids);
  
  /**
   * Update upload retry count.
   *
   * @param sessionId the session id
   * @param tid      the tid
   * @param newEpc  the newEpc
   */
  @Query("UPDATE upload_inventory SET upload_retry_count=upload_retry_count+1 WHERE TRIM(session_id) = :sessionId AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) = (:tid) AND new_epc IS NOT NULL AND LENGTH(new_epc)>0 AND TRIM(new_epc) = (:newEpc) AND is_uploaded<=0")
  Integer updateUploadRetryCount(String sessionId, String tid, String newEpc);
  
  /**
   * Update upload retry count.
   *
   * @param sessionId the session id
   */
  @Query("UPDATE upload_inventory SET upload_retry_count=upload_retry_count+1 WHERE TRIM(session_id) = :sessionId AND is_uploaded<=0")
  Integer updateUploadRetryCount(String sessionId);
  
  /**
   * Is epc present boolean.
   *
   * @param sessionType the session type
   * @param epc       the epc
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM upload_inventory WHERE TRIM(epc) COLLATE NOCASE = :epc AND session_type = :sessionType")
  public boolean isEPCPresent(int sessionType, String epc);
  
  /**
   * Is upload inventory data
   *
   * @param sessionType the session type
   * @param epc       the epc
   * @return the boolean
   */
  @Query("SELECT * FROM upload_inventory WHERE TRIM(epc) COLLATE NOCASE = :epc AND session_type = :sessionType")
  public UploadInventory getBysessionTypeAndEpc(int sessionType, String epc);
  
  @Query("SELECT COALESCE(is_found,0)>0 FROM upload_inventory WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId")
  public boolean isVerifiedByTid(String sessionId, String epc, String tid);
  
  @Query("SELECT COALESCE(is_found,0)>0 FROM upload_inventory WHERE TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId")
  public boolean isVerifiedByTid(String sessionId, String tid);
  
  @Query("SELECT COALESCE(is_found,0)>0 FROM upload_inventory WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(session_id) = :sessionId")
  public boolean isVerifiedByEpc(String sessionId, String epc);
  
  @Query("UPDATE upload_inventory SET is_found=1 WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId AND is_found<=0")
  public Integer updateEncVerified(String sessionId, String epc, String tid);
  
  @Query("UPDATE upload_inventory SET is_found=1 WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(session_id) = :sessionId AND is_found<=0")
  public Integer updateEncVerifiedByEpc(String sessionId, String epc);
  
  @Query("UPDATE upload_inventory SET is_found=1 WHERE TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId AND is_found<=0")
  public Integer updateEncVerifiedByTid(String sessionId, String tid);
  
  @Query("UPDATE upload_inventory SET is_found=1,enc_verify_status=:status WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId AND is_found<=0")
  public Integer updateEncVerified(String sessionId, String epc, String tid, Integer status);
  
  @Query("UPDATE upload_inventory SET is_found=1,enc_verify_status=:status WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(session_id) = :sessionId AND is_found<=0")
  public Integer updateEncVerifiedByEpc(String sessionId, String epc, Integer status);
  
  @Query("UPDATE upload_inventory SET enc_verify_status=:status WHERE TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId AND is_found<=0 AND (enc_verify_status==0 OR enc_verify_status==2)")
  public Integer updateEncVerifyStatusByTid(String sessionId, String tid, Integer status);
}
