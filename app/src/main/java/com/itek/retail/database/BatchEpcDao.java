package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.BatchEpc;

import java.util.List;

/**
 * The interface BatchEpc dao.
 */
@androidx.room.Dao
public interface BatchEpcDao{
  
  /**
   * Insert.
   *
   * @param batchEpcs the batchEpcs
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(BatchEpc... batchEpcs);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<BatchEpc> listBatchEpcs);
  
  /**
   * Update.
   *
   * @param batchEpcs the batch epcs
   */
  @Update
  Integer update(BatchEpc... batchEpcs);
  
  /**
   * Delete.
   *
   * @param batchEpcs the batch epcs
   */
  @Delete
  Integer delete(BatchEpc... batchEpcs);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM batch_epc")
  void deleteAll();
  
  /**
   * Delete batch epcs.
   *
   * @return the integer delete row count
   */
  @Transaction
  @Query("DELETE FROM batch_epc WHERE TRIM(batch_id)!=:batchId")
  public Integer deleteBatchEpcsExcept(String batchId);
  
  /**
   * Delete batch epcs.
   *
   * @return the integer delete row count
   */
  @Transaction
  @Query("DELETE FROM batch_epc WHERE TRIM(batch_id)=:batchId")
  public Integer deleteBatchEpcs(String batchId);
  
  /**
   * Delete batch epcs.
   *
   * @return the batch epcs
   */
  @Transaction
  @Query("DELETE FROM batch_epc WHERE TRIM(batch_id)=:batchId AND TRIM(ean) COLLATE NOCASE =:ean")
  public Integer deleteBatchEpcs(String batchId,String ean);
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM batch_epc")
  List<BatchEpc> getAll();
  
  /**
   * Gets brand by name.
   *
   * @param batchId the batch id
   * @return the brand by name
   */
  @Query("SELECT * FROM batch_epc WHERE TRIM(batch_id)=:batchId")
  List<BatchEpc> getBatchEpcByBatchId(String batchId);
  
  /**
   * Gets all epcs.
   *
   * @return the all epcs
   */
  @Transaction
  @Query("SELECT TRIM(epc) FROM batch_epc WHERE TRIM(batch_id)=:batchId")
  List<String> getAllBatchEpcs(String batchId);
  
  /**
   * Gets all epcs.
   *
   * @return the all epcs
   */
  @Transaction
  @Query("SELECT TRIM(epc) FROM batch_epc WHERE TRIM(batch_id)=:batchId AND TRIM(ean) COLLATE NOCASE =:ean")
  List<String> getAllBatchEpcs(String batchId,String ean);
  
  /**
   * Is epc present boolean.
   *
   * @param batchId   the batch id
   * @param ean       the ean
   * @param epc       the epc
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM batch_epc WHERE TRIM(batch_id)=:batchId AND (:ean IS NULL OR LENGTH(:ean)<=0 OR TRIM(ean) COLLATE NOCASE = :ean) AND TRIM(epc) COLLATE NOCASE = :epc")
  public boolean isEPCPresent(String batchId, String ean, String epc);
  
  /**
   * Is epc present boolean.
   *
   * @param batchId   the batch id
   * @param article   the article
   * @param ean       the ean
   * @param epc       the epc
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM batch_epc WHERE TRIM(batch_id) = :batchId AND (:article IS NULL OR LENGTH(:article)<=0 OR TRIM(article) = :article) AND (:ean IS NULL OR LENGTH(:ean)<=0 OR TRIM(ean) COLLATE NOCASE = :ean) AND TRIM(epc) COLLATE NOCASE = :epc")
  public boolean isEPCPresent(String batchId, String article, String ean, String epc);
  
  /**
   * Is epc present boolean.
   *
   * @param batchId   the batch id
   * @param epc       the epc
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM batch_epc WHERE TRIM(batch_id) = :batchId AND TRIM(epc) COLLATE NOCASE = :epc")
  public boolean isEPCPresent(String batchId, String epc);
  
  
  /**
   * Gets all epcs.
   *
   * @return the all epcs
   */
  @Transaction
  @Query("SELECT TRIM(epc) FROM batch_epc WHERE TRIM(batch_id)=:batchId AND TRIM(article)=:article AND TRIM(ean) COLLATE NOCASE =:ean")
  List<String> getAllBatchEpcs(String batchId,String article,String ean);
  
  
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM batch_epc")
  Integer getTableSize();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM batch_epc")
  boolean hasData();
  
}
