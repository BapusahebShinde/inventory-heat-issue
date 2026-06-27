package com.itek.retail.database;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.TagID;

import java.util.List;

/**
 * The interface TagID dao.
 */
@androidx.room.Dao
public interface TagIDDao{
  
  /**
   * Insert.
   *
   * @param tids the tids
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(TagID... tids);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<TagID> listtids);
  
  /**
   * Update.
   *
   * @param tids the tids
   */
  @Update
  Integer update(TagID... tids);
  
  /**
   * Delete.
   *
   * @param tids the tids
   */
  @Delete
  Integer delete(TagID... tids);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM tag_id")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM tag_id order by is_found DESC")
  List<TagID> getAll();
  
  /**
   * Gets all tids.
   *
   * @return the all tids
   */
  @Transaction
  @Query("SELECT TRIM(tid) FROM tag_id")
  List<String> getAllTids();
  
  /**
   * Gets all tids except.
   *
   * @param tid the tis
   * @return the all tids except
   */
  @Transaction
  @Query("SELECT TRIM(tid) FROM tag_id WHERE TRIM(tid) NOT IN (:tid)")
  List<String> getAllTidsExcept(String tid);
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM tag_id")
  Integer getTableSize();
  
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM tag_id where is_found=1")
  LiveData<Integer> getFoundCountData();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM tag_id")
  boolean hasData();
  
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM tag_id WHERE TRIM(tid) COLLATE NOCASE = :tid")
  boolean isTIDPresent(String tid);
  
  @Query("UPDATE tag_id SET is_found=1 WHERE TRIM(tid) COLLATE NOCASE = :tid AND is_found<=0")
  public Integer updateFound(String tid);
}
