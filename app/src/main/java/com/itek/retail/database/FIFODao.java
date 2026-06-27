package com.itek.retail.database;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.FIFOModel;

import java.util.List;

/**
 * The interface Stock correction dao.
 */
@androidx.room.Dao
public interface FIFODao{
  
  /**
   * Insert.
   *
   * @param fifoModels the fifo models
   */
  @Insert
  void insert(FIFOModel... fifoModels);
  
  @Insert
  void insertAll(List<FIFOModel> fifoModels);
  
  /**
   * Update.
   *
   * @param fifoModels the fifo models
   */
  @Update
  Integer update(FIFOModel... fifoModels);
  
  /**
   * Delete.
   *
   * @param productModels the fifo models
   */
  @Delete
  Integer delete(FIFOModel... productModels);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM fifo")
  void deleteAll();
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM fifo WHERE TRIM(fifo_date) COLLATE NOCASE =:fifoDate")
  Integer deleteEan(String fifoDate);
  
  /**
   * Update found.
   *
   * @param epc the epc
   */
  @Query("UPDATE fifo SET is_found=1 WHERE TRIM(epc) COLLATE NOCASE = :epc AND TRIM(ean) COLLATE NOCASE = :ean AND TRIM(fifo_date) COLLATE NOCASE = :fifoDate AND is_found=0")
  Integer updateFound(String ean, String epc, String fifoDate);
  
  /**
   * Update found.
   *
   * @param epc the epc
   */
  @Query("UPDATE fifo SET is_found=1,is_decoded=1 WHERE TRIM(epc) COLLATE NOCASE = :epc AND TRIM(ean) COLLATE NOCASE = :ean AND TRIM(fifo_date) COLLATE NOCASE = :fifoDate AND is_decoded=0")
  Integer updateDecoded(String ean, String epc, String fifoDate);
  
  /**
   * Is epc present boolean.
   *
   * @param epc the epc
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM fifo WHERE epc IS NOT NULL AND LENGTH(TRIM(epc))>0 AND TRIM(epc) COLLATE NOCASE = :epc")
  boolean isEPCPresent(String epc);
  
  /**
   * Is epc present boolean.
   *
   * @param epc the epc
   * @param ean the ean
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM fifo WHERE epc IS NOT NULL AND LENGTH(TRIM(epc))>0 AND TRIM(epc) COLLATE NOCASE = :epc AND ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean")
  boolean isEPCPresent(String epc, String ean);
  
  /**
   * Is epc present boolean.
   *
   * @param epc      the epc
   * @param ean      the ean
   * @param fifoDate the fifo date
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM fifo WHERE epc IS NOT NULL AND LENGTH(TRIM(epc))>0 AND TRIM(epc) COLLATE NOCASE = :epc AND ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND fifo_date IS NOT NULL AND LENGTH(TRIM(fifo_date))>0 AND TRIM(fifo_date) COLLATE NOCASE = :fifoDate")
  boolean isEPCPresent(String epc, String ean, String fifoDate);
  
  /**
   * Is decode epc present boolean.
   *
   * @param epc      the epc
   * @param ean      the ean
   * @param fifoDate the fifo date
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM fifo WHERE epc IS NOT NULL AND LENGTH(TRIM(epc))>0 AND TRIM(epc) COLLATE NOCASE = :epc AND ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND fifo_date IS NOT NULL AND LENGTH(TRIM(fifo_date))>0 AND TRIM(fifo_date) COLLATE NOCASE = :fifoDate AND is_found>0 AND is_decoded>0")
  boolean isDecodeEPCPresent(String epc, String ean, String fifoDate);
  
  /**
   * Is epc present boolean.
   *
   * @param epc      the epc
   * @param ean      the ean
   * @param fifoDate the fifo date
   * @param zone     the zone
   * @param zoneId   the zoneId
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM fifo WHERE epc IS NOT NULL AND LENGTH(TRIM(epc))>0 AND TRIM(epc) COLLATE NOCASE = :epc AND ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND fifo_date IS NOT NULL AND LENGTH(TRIM(fifo_date))>0 AND TRIM(fifo_date) COLLATE NOCASE = :fifoDate AND zone IS NOT NULL AND LENGTH(TRIM(zone))>0 AND TRIM(zone) COLLATE NOCASE = :zone AND zone_id IS NOT NULL AND LENGTH(TRIM(zone_id))>0 AND TRIM(zone_id) COLLATE NOCASE = :zoneId")
  boolean isEPCPresent(String epc, String ean, String fifoDate, String zone, String zoneId);
  
  @Query("SELECT epc FROM fifo WHERE epc IS NOT NULL AND LENGTH(TRIM(epc))>0 AND ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND fifo_date IS NOT NULL AND LENGTH(TRIM(fifo_date))>0 AND TRIM(fifo_date) COLLATE NOCASE = :fifoDate AND  is_found=0")
  List<String> getEPCList(String ean, String fifoDate);
  
  @Query("SELECT * FROM fifo WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND is_decoded<=0 ORDER BY fifo_date ASC")
  List<FIFOModel> getList(String ean);
  
  @Query("SELECT * FROM fifo WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND fifo_date IS NOT NULL AND LENGTH(TRIM(fifo_date))>0 AND TRIM(fifo_date) COLLATE NOCASE = :fifoDate AND is_decoded<=0 ORDER BY fifo_date ASC")
  List<FIFOModel> getList(String ean, String fifoDate);
  
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Transaction
  @Query("SELECT ean,fifo_date,zone,zone_id,COALESCE(COUNT(epc),0) AS 'totalQty' FROM fifo WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND is_decoded<=0 GROUP BY fifo_date ORDER BY fifo_date ASC")
  List<FIFOModel> getDatewiseList(String ean);
  
  @Query("SELECT fifo_date FROM fifo WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND is_decoded<=0 ORDER BY fifo_date ASC LIMIT 1")
  String getOldestDate(String ean);
  
  @Query("SELECT *,COALESCE(COUNT(epc),0) AS 'totalQty' FROM fifo WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND is_decoded<=0 GROUP BY fifo_date ORDER BY fifo_date ASC LIMIT 1")
  FIFOModel getOldestDateObj(String ean);
  
  @Query("SELECT REPLACE(GROUP_CONCAT(z),',',', ') FROM (SELECT (zone||' ('||COALESCE(COUNT(*),0)||')') AS z FROM fifo WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND fifo_date IS NOT NULL AND LENGTH(TRIM(fifo_date))>0 AND TRIM(fifo_date) COLLATE NOCASE = :fifoDate AND zone IS NOT NULL AND LENGTH(TRIM(zone))>0 AND zone_id IS NOT NULL AND LENGTH(TRIM(zone_id))>0 AND TRIM(zone_id) COLLATE NOCASE != '0' AND is_decoded<=0 GROUP BY zone_id ORDER BY zone_id ASC)")
  String getDatewiseZoneStr(String ean, String fifoDate);
  
  /**
   * On fifo found live data.
   *
   * @return the live data
   */
  @Transaction
  @Query("SELECT COALESCE(SUM(is_found),0) FROM  fifo")
  LiveData<Integer> onFIFOFound();
  
  /**
   * Gets total count.
   *
   * @return the total count
   */
  @Query("SELECT COALESCE(SUM(is_found),0) FROM fifo WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND fifo_date IS NOT NULL AND LENGTH(TRIM(fifo_date))>0 AND TRIM(fifo_date) COLLATE NOCASE =:fifoDate AND is_found>=0")
  Integer getDateFoundCount(String ean, String fifoDate);
  
  /**
   * Gets total found count.
   *
   * @return the total found count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM fifo WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND fifo_date IS NOT NULL AND LENGTH(TRIM(fifo_date))>0 AND TRIM(fifo_date) COLLATE NOCASE =:fifoDate AND is_decoded<=0")
  Integer getDateTotalCount(String ean, String fifoDate);
  
  /**
   * Gets total found count.
   *
   * @return the total found count
   */
  @Query("SELECT COALESCE(SUM(is_decoded),0) FROM fifo WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND fifo_date IS NOT NULL AND LENGTH(TRIM(fifo_date))>0 AND TRIM(fifo_date) COLLATE NOCASE =:fifoDate AND is_found>=0 AND is_decoded>=0")
  Integer getDateDecodedCount(String ean, String fifoDate);
  
  @Query("SELECT COALESCE(SUM(is_found),0)>0 AND COALESCE(SUM(is_found),0)>=COUNT(*) FROM fifo WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(ean) COLLATE NOCASE = :ean AND fifo_date IS NOT NULL AND LENGTH(TRIM(fifo_date))>0 AND TRIM(fifo_date) COLLATE NOCASE =:fifoDate")
  boolean isQtyFound(String ean, String fifoDate);
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM fifo")
  boolean hasData();
}
