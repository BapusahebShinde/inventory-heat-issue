package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.OutwardBatch;

import java.util.List;

/**
 * The interface Outward Tote Eans dao.
 */
@androidx.room.Dao
public interface OutwardBatchDao{
  
  /**
   * Insert.
   *
   * @param outwardBatch the OutwardBatch
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(OutwardBatch... outwardBatch);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<OutwardBatch> outwardBatch);
  
  /**
   * Update.
   *
   * @param outwardBatch the OutwardBatch
   */
  @Update
  Integer update(OutwardBatch... outwardBatch);
  
  /**
   * Delete.
   *
   * @param outwardBatch the OutwardBatch
   */
  @Delete
  Integer delete(OutwardBatch... outwardBatch);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM owt_batch")
  void deleteAll();
  
  
  @Query("SELECT * FROM owt_batch WHERE owt_type_id=:owtTypeId AND owt_type=:owtType AND dest_site_code=:destSiteCode  LIMIT 1")
  OutwardBatch getOutwardBatchFromTypeIdAndDestSiteCode(Long owtTypeId,String owtType, String destSiteCode);
  
  @Query("SELECT * FROM owt_batch WHERE  batch_id=:batchId LIMIT 1")
  OutwardBatch getOutwardBatchFromBatchId(String batchId);
  
  @Query("SELECT * FROM owt_batch WHERE batch_id IS NOT NULL AND LENGTH(TRIM(batch_id))>0 AND total_cartons>0 ORDER BY id LIMIT 1")
  OutwardBatch getLastPendingOutwardBatch();
  
  @Query("SELECT * FROM owt_batch WHERE batch_id IS NOT NULL AND LENGTH(TRIM(batch_id))>0 AND total_cartons>0 AND owt_type IS NOT NULL AND LENGTH(TRIM(owt_type))>0 AND TRIM(owt_type) COLLATE NOCASE LIKE '%'||:owtType||'%' ORDER BY id LIMIT 1")
  OutwardBatch getLastPendingOutwardBatchByKeys(String owtType);
  
  @Query("SELECT * FROM owt_batch WHERE batch_id IS NOT NULL AND LENGTH(TRIM(batch_id))>0 AND total_cartons>0 AND owt_type IS NOT NULL AND LENGTH(TRIM(owt_type))>0 AND TRIM(owt_type) =:owtType ORDER BY id LIMIT 1")
  OutwardBatch getLastPendingOutwardBatch(String owtType);
  
  @Query("SELECT COALESCE(batch_id,'') FROM owt_batch WHERE owt_type_id=:owtTypeId AND owt_type=:owtType AND dest_site_code=:destSiteCode  LIMIT 1")
  String getOutwardBatchIdFromTypeIdAndDestSiteCode(Long owtTypeId,String owtType, String destSiteCode);
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM owt_batch")
  List<OutwardBatch> getAll();
  
  
  /**
   * Update accepted carton count.
   *
   * @param batchId the batch id
   */
  @Query("UPDATE owt_batch SET accepted_cartons=accepted_cartons+1,total_cartons=total_cartons+1 WHERE TRIM(batch_id) = :batchId")
  Integer updateAcceptedQty(String batchId);
  
  /**
   * Update rejected carton count.
   *
   * @param batchId the batch id
   */
  @Query("UPDATE owt_batch SET rejected_cartons=rejected_cartons+1,total_cartons=total_cartons+1 WHERE TRIM(batch_id) = :batchId")
  Integer updateRejectedQty(String batchId);
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(batch_id),0)>0 FROM owt_batch WHERE batch_id=:batchId")
  boolean isBatchExist(String batchId);
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(SUM(total_cartons),0) FROM owt_batch WHERE batch_id=:batchId")
  Long getTotalCartonCount(String batchId);
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(SUM(accepted_cartons),0) FROM owt_batch WHERE batch_id=:batchId")
  Long getAcceptedCartonCount(String batchId);
  
  
  /**
   * Delete inventory.
   *
   * @param batchId the batch id
   */
  @Query("DELETE FROM owt_batch WHERE batch_id=:batchId")
  public Integer deleteBatch(String batchId);
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM owt_batch")
  boolean hasData();
  
}
