package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.OutwardToteEans;

import java.util.List;

/**
 * The interface Outward Tote Eans dao.
 */
@androidx.room.Dao
public interface OutwardToteEansDao{
  
  /**
   * Insert.
   *
   * @param outwardToteEans the OutwardToteEans
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(OutwardToteEans... outwardToteEans);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<OutwardToteEans> outwardToteEans);
  
  /**
   * Update.
   *
   * @param outwardToteEans the OutwardToteEans
   */
  @Update
  Integer update(OutwardToteEans... outwardToteEans);
  
  /**
   * Delete.
   *
   * @param outwardToteEans the OutwardToteEans
   */
  @Delete
  Integer delete(OutwardToteEans... outwardToteEans);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM owt_tote_eans")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM owt_tote_eans")
  List<OutwardToteEans> getAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Transaction
  @Query("SELECT tote_type,'' AS ean, SUM(scan_qty) AS scan_qty, SUM(total_qty) AS total_qty FROM owt_tote_eans GROUP BY tote_type")
  List<OutwardToteEans> getToteTypewiseCount();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Transaction
  @Query("SELECT tote_type,ean AS ean, SUM(scan_qty) AS scan_qty, SUM(total_qty) AS total_qty FROM owt_tote_eans WHERE scan_qty>0 GROUP BY tote_type,ean")
  List<OutwardToteEans> getToteTypeEanwiseCount();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Transaction
  @Query("SELECT tote_type,ean,scan_qty,total_qty FROM owt_tote_eans ORDER BY tote_type,ean")
  List<OutwardToteEans> getToteTypeEanCount();
  
  /**
   * Update scan_count.
   *
   * @param ean the ean
   */
  @Query("UPDATE owt_tote_eans SET scan_qty=scan_qty+1 WHERE TRIM(ean) = :ean")
  Integer updateScanQty(String ean);
  
  /**
   * Update scan_count.
   *
   * @param toteType the tote type
   */
  @Query("UPDATE owt_tote_eans SET total_qty=:totalQty WHERE TRIM(tote_type)=:toteType")
  Integer updateTotalQty(String toteType, int totalQty);
  
  /**
   * Is ean present boolean.
   *
   * @param ean the ean
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM owt_tote_eans WHERE (:ean IS NOT NULL AND LENGTH(TRIM(:ean))>0 AND TRIM(ean) =:ean)")
  public boolean isEANPresent(String ean);
  
  /**
   * Gets all owt_tote_eans.
   *
   * @return the all owt_tote_eans
   */
  @Transaction
  @Query("SELECT TRIM(tote_type) FROM owt_tote_eans")
  List<String> getAllToteTypes();
  
  @Transaction
  @Query("SELECT TRIM(tote_type) FROM owt_tote_eans WHERE tote_type IS NOT NULL AND LENGTH(TRIM(tote_type))>0 AND scan_qty>0")
  List<String> getScannedToteTypes();
  
  /**
   * Gets all owt_tote_eans.
   *
   * @return the all owt_tote_eans
   */
  @Transaction
  @Query("SELECT TRIM(ean) FROM owt_tote_eans")
  List<String> getAllEans();
  
  /**
   * Gets all owt_tote_eans except.
   *
   * @param toteType the tote_type
   * @return the all owt_tote_eans except
   */
  @Transaction
  @Query("SELECT TRIM(tote_type) FROM owt_tote_eans WHERE TRIM(tote_type) NOT IN (:toteType)")
  List<String> getAllToteTypesExcept(String toteType);
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM owt_tote_eans")
  Integer getTableSize();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM owt_tote_eans")
  boolean hasData();
  
}
