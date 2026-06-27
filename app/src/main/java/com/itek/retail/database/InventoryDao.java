package com.itek.retail.database;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.common.AppConstants;
import com.itek.retail.model.EanQty;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.MultiQtyModel;

import java.util.List;
import java.util.Set;

/**
 * The interface Inventory dao.
 */
@androidx.room.Dao
public interface InventoryDao{
  
  /**
   * Insert inventory data.
   *
   * @param inventories the inventories
   */
  @Insert
  void insertInventoryData(Inventory... inventories);
  
  @Insert
  void insertAll(List<Inventory> listInventories);
  
  /**
   * Update inventory data.
   *
   * @param inventories the inventories
   */
  @Update
  Integer updateInventoryData(Inventory... inventories);
  
  /**
   * Delete inventory data.
   *
   * @param inventories the inventories
   */
  @Delete
  Integer deleteInventoryData(Inventory... inventories);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM inventory")
  public void deleteAll();
  
  /**
   * Delete inventory.
   *
   * @param sessionId the session id
   */
  @Query("DELETE FROM inventory WHERE TRIM(session_id) = :sessionId")
  public Integer deleteInventory(String sessionId);
  
  @Query("DELETE FROM inventory WHERE TRIM(session_id) = :sessionId AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) IN (:tids)")
  public Integer deleteInventory(String sessionId, List<String> tids);
  
  @Query("DELETE FROM inventory WHERE TRIM(session_id) = :sessionId AND (:ean IS NULL OR LENGTH(TRIM(ean))<=0 OR TRIM(ean) COLLATE NOCASE =:ean) AND (:zone IS NULL OR LENGTH(TRIM(zone))<=0 OR TRIM(zone)=:zone)")
  public Integer deleteInventory(String sessionId, String ean, String zone);
  
  @Query("DELETE FROM inventory WHERE TRIM(session_id) = :sessionId AND (:ean IS NULL OR LENGTH(TRIM(ean))<=0 OR TRIM(ean) COLLATE NOCASE =:ean) AND (:zone IS NULL OR LENGTH(TRIM(zone))<=0 OR TRIM(zone)=:zone) AND is_found<=0")
  public Integer deleteNotFoundInventory(String sessionId, String ean, String zone);
  
  @Query("UPDATE inventory SET is_found=1 WHERE TRIM(session_id) = :sessionId AND (:ean IS NULL OR LENGTH(TRIM(ean))<=0 OR TRIM(ean) COLLATE NOCASE =:ean) AND (:zone IS NULL OR LENGTH(TRIM(zone))<=0 OR TRIM(zone)=:zone) AND is_found<=0")
  public Integer updateFound(String sessionId, String ean, String zone);
  
  @Query("SELECT SUM(COALESCE(is_found,0)) FROM inventory WHERE TRIM(session_id) = :sessionId AND (:ean IS NULL OR LENGTH(TRIM(ean))<=0 OR TRIM(ean) COLLATE NOCASE =:ean) AND (:zone IS NULL OR LENGTH(TRIM(zone))<=0 OR TRIM(zone)=:zone) AND is_found>0")
  public Integer getFoundInventoryCount(String sessionId, String ean, String zone);
  
  @Query("SELECT DISTINCT TRIM(COALESCE(epc,'')) FROM inventory WHERE TRIM(session_id) = :sessionId AND (:ean IS NULL OR LENGTH(TRIM(ean))<=0 OR TRIM(ean) COLLATE NOCASE =:ean) AND (:zone IS NULL OR LENGTH(TRIM(zone))<=0 OR TRIM(zone)=:zone) AND is_found>0")
  public List<String> getFoundEPCs(String sessionId, String ean, String zone);
  
  /**
   * Delete inventory.
   *
   * @param sessionType the session type
   */
  @Query("DELETE FROM inventory WHERE session_type = :sessionType")
  public Integer deleteInventory(int sessionType);
  
  /**
   * Delete all except.
   */
  @Query("DELETE FROM inventory WHERE session_type NOT IN (1,2,12,13,18,28)")
  public Integer deleteAllExcept();
  
  /**
   * Gets last inserted.
   *
   * @param sessionId the session id
   * @return the last inserted
   */
  @Query("SELECT * FROM inventory WHERE TRIM(session_id) = :sessionId ORDER BY ino desc LIMIT 1")
  public Inventory getLastInserted(String sessionId);
  
  //temp
  @Query("SELECT * FROM inventory WHERE ino = :pkId ORDER BY ino desc LIMIT 1")
  public Inventory get(Integer pkId);
  
  /**
   * Gets list epc.
   *
   * @param sessionId the session id
   * @return the list epc
   */
  @Transaction
  @Query("SELECT TRIM(epc) FROM inventory WHERE TRIM(session_id) = :sessionId")
  public List<String> getListEPC(String sessionId);
  
  /**
   * Gets list epc.
   *
   * @param sessionId the session id
   * @param ean       the ean
   * @return the list epc
   */
  @Transaction
  @Query("SELECT TRIM(epc) FROM inventory WHERE TRIM(session_id) = :sessionId AND ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean")
  public List<String> getListEPC(String sessionId, String ean);
  
  /**
   * Gets list epc.
   *
   * @param sessionId the session id
   * @param ean       the ean
   * @param zone      the zone
   * @return the list epc
   */
  @Transaction
  @Query("SELECT TRIM(epc) FROM inventory WHERE TRIM(session_id) = :sessionId AND ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean AND zone IS NOT NULL AND LENGTH(zone)>0 AND TRIM(zone)= :zone")
  public List<String> getListEPC(String sessionId, String ean, String zone);
  
  /**
   * Gets zone list.
   *
   * @param sessionId the session id
   * @param zone      the zone
   * @return the list epc
   */
  @Transaction
  @Query("SELECT * FROM inventory WHERE TRIM(session_id) = :sessionId AND (:zone IS NULL OR LENGTH(TRIM(:zone))<=0 OR TRIM(:zone) ='" + AppConstants.ALL + "' OR TRIM(zone)= :zone) ORDER BY is_found")
  public List<Inventory> getZoneList(String sessionId, String zone);
  
  /**
   * Gets enc verify list.
   *
   * @param sessionId the session id
   * @return the list inventory
   */
  @Transaction
  @Query("SELECT * FROM inventory WHERE TRIM(session_id) = :sessionId AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0) ORDER BY enc_verify_status")
  public List<Inventory> getEncVerifyList(String sessionId);
  
  @Transaction
  @Query("SELECT * FROM inventory WHERE TRIM(session_id) = :sessionId ORDER BY enc_verify_status desc")
  public List<Inventory> getEncVerifyListReverse(String sessionId);
  
  /**
   * Gets list.
   *
   * @param sessionId the session id
   * @param zone      the zone
   * @return the list epc
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND (:zone IS NULL OR LENGTH(TRIM(:zone))<=0 OR TRIM(:zone) ='" + AppConstants.ALL + "' OR TRIM(zone)= :zone) AND is_found>0 AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  public Integer getZonewiseFound(String sessionId, String zone);
  
  /**
   * Gets ean qty.
   *
   * @param sessionId the session id
   * @param ean       the ean
   * @return the ean qty
   */
  @Query("SELECT COALESCE(COUNT(ean),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean")
  public int getEANQty(String sessionId, String ean);
  
  /**
   * Gets ean qty.
   *
   * @param sessionId the session id
   * @param ean       the ean
   * @return the ean qty
   */
  @Query("SELECT COALESCE(COUNT(ean),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean AND zone IS NOT NULL AND LENGTH(zone)>0 AND TRIM(zone)= :zone")
  public int getEANQty(String sessionId, String ean, String zone);
  
  /**
   * Gets ean qty.
   *
   * @param sessionId the session id
   * @param eans      the eans
   * @return the ean qty
   */
  @Query("SELECT COALESCE(COUNT(ean),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND TRIM(ean) COLLATE NOCASE IN (:eans)")
  public int getEANQty(String sessionId, Set<String> eans);
  
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean AND new_epc IS NOT NULL AND LENGTH(new_epc)>0 AND TRIM(new_epc) LIKE '0%'")
  public int getEANDecodeQty(String sessionId, String ean);
  
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean AND zone IS NOT NULL AND LENGTH(zone)>0 AND TRIM(zone)= :zone AND new_epc IS NOT NULL AND LENGTH(new_epc)>0 AND TRIM(new_epc) LIKE '0%'")
  public int getEANDecodeQty(String sessionId, String ean, String zone);
  
  @Transaction
  @Query("SELECT * FROM inventory WHERE TRIM(session_id) = :sessionId AND ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean ORDER BY ino desc")
  public List<Inventory> getEANInventory(String sessionId, String ean);
  
  @Transaction
  @Query("SELECT * FROM inventory WHERE TRIM(session_id) = :sessionId AND ean IS NOT NULL AND LENGTH(ean)>0 AND TRIM(ean) COLLATE NOCASE = :ean AND zone IS NOT NULL AND LENGTH(zone)>0 AND TRIM(zone)= :zone ORDER BY ino desc")
  public List<Inventory> getEANInventory(String sessionId, String ean, String zone);
  
  /**
   * Gets encoded eans.
   *
   * @param sessionId the session id
   * @return the encoded eans
   */
  @Transaction
  @Query("SELECT TRIM(ean) AS ean, COALESCE(COUNT(ean),0) AS eanQty FROM inventory WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(session_id) = :sessionId AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0) GROUP BY TRIM(ean) ORDER BY ino DESC")
  public List<EanQty> getEncodedEans(String sessionId);
  
  @Transaction
  @Query("SELECT TRIM(ean) AS ean, COALESCE(COUNT(ean),0) AS eanQty FROM inventory WHERE ean IS NOT NULL AND LENGTH(TRIM(ean))>0 AND TRIM(session_id) = :sessionId GROUP BY TRIM(ean) ORDER BY ean DESC")
  public List<EanQty> getEans(String sessionId);
  
  /**
   * Gets all inventory data.
   *
   * @return the all inventory data
   */
  @Transaction
  @Query("SELECT * FROM inventory WHERE session_type=1")
  LiveData<List<Inventory>> getAllInventoryData();
  
  /**
   * Gets all inventory data.
   *
   * @param sessionId the session id
   * @return the all inventory data
   */
  @Transaction
  @Query("SELECT * FROM inventory WHERE TRIM(session_id) = :sessionId AND is_uploaded<=0 ")
  List<Inventory> getAllInventoryData(String sessionId);
  
  /**
   * Gets all inventory data.
   *
   * @param sessionId the session id
   * @return the all inventory data
   */
  @Transaction
  @Query("SELECT * FROM inventory WHERE TRIM(session_id) = :sessionId AND is_found>0 AND is_uploaded<=0")
  List<Inventory> getAllFoundInventoryData(String sessionId);
  
  /**
   * Gets all inventory data.
   *
   * @param sessionType the session type
   * @return the all inventory data
   */
  @Transaction
  @Query("SELECT * FROM inventory WHERE session_type=:sessionType")
  List<Inventory> getAllInventoryData(int sessionType);
  
  /**
   * Gets all inventory data.
   *
   * @param sessionType the session type
   * @return the all inventory data
   */
  @Transaction
  @Query("SELECT * FROM inventory WHERE session_type=:sessionType AND is_found>0")
  List<Inventory> getAllFoundInventoryData(int sessionType);
  
  /**
   * Gets all non decoded inventory data.
   *
   * @param sessionType the session type
   * @return the all inventory data
   */
  @Transaction
  @Query("SELECT * FROM inventory WHERE session_type=:sessionType AND COALESCE(NULLIF(new_epc,''),epc) IS NOT NULL AND LENGTH(COALESCE(NULLIF(new_epc,''),epc))>=24 AND COALESCE(NULLIF(new_epc,''),epc) NOT LIKE '0%'")
  List<Inventory> getAllNonDecodedInventoryData(int sessionType);
  
  /**
   * Gets non uploaded count.
   *
   * @param sessionId the session id
   * @return the non uploaded count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND is_uploaded<=0")// AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  Integer getNonUploadedCount(String sessionId);
  
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND is_found<=0 AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  Integer getNonVerifiedCount(String sessionId);
  
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND is_found>0 AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  Integer getVerifiedCount(String sessionId);
  
  @Transaction
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Query("SELECT COALESCE(COUNT(*),0) AS 'total',COALESCE(SUM(is_found),0) AS 'found' FROM inventory WHERE TRIM(session_id) = :sessionId AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  LiveData<MultiQtyModel> getEncVerifiedCount(String sessionId);
  
  @Transaction
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Query("SELECT COALESCE(COUNT(*),0) AS 'total',COALESCE(SUM(is_uploaded),0) AS 'found' FROM inventory WHERE TRIM(session_id) = :sessionId AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  LiveData<MultiQtyModel> getUploadedCount(String sessionId);
  
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId")
  Integer getTotalCount(String sessionId);
  
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE session_type = :sessionType AND is_uploaded>0")
  Integer getUploadedCount(int sessionType);
  
  /**
   * Gets non uploaded count.
   *
   * @param sessionType the session type
   * @return the non uploaded count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE session_type = :sessionType AND is_uploaded<=0")
  Integer getNonUploadedCount(int sessionType);
  
  /**
   * Gets non verified pick count.
   *
   * @param sessionType the session type
   * @return the non verified pick count
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE session_type = :sessionType AND COALESCE(NULLIF(new_epc,''),epc) IS NOT NULL AND LENGTH(COALESCE(NULLIF(new_epc,''),epc))>=24 AND COALESCE(NULLIF(new_epc,''),epc) NOT LIKE '0%' AND is_uploaded<=0")
  LiveData<Integer> getNonVerifiedPickCount(int sessionType);
  
  /**
   * Gets non decoded verified count.
   *
   * @param sessionType the session type
   * @return the non decoded verified count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE session_type = :sessionType AND COALESCE(NULLIF(new_epc,''),epc) IS NOT NULL AND LENGTH(COALESCE(NULLIF(new_epc,''),epc))>=24 AND COALESCE(NULLIF(new_epc,''),epc) NOT LIKE '0%' AND is_uploaded>0")
  Integer getNonDecodedVerifiedCount(int sessionType);
  
  /**
   * Gets non uploaded decode count.
   *
   * @param sessionType the session type
   * @return the non uploaded decode count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE session_type = :sessionType AND COALESCE(NULLIF(new_epc,''),epc) IS NOT NULL AND LENGTH(COALESCE(NULLIF(new_epc,''),epc))>=24 AND COALESCE(NULLIF(new_epc,''),epc) LIKE '0%' AND is_uploaded<=0")
  Integer getNonUploadedDecodeCount(int sessionType);
  
  /**
   * Gets non decoded count.
   *
   * @param sessionType the session type
   * @return the non uploaded count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE session_type = :sessionType AND COALESCE(NULLIF(new_epc,''),epc) IS NOT NULL AND LENGTH(COALESCE(NULLIF(new_epc,''),epc))>=24 AND COALESCE(NULLIF(new_epc,''),epc) NOT LIKE '0%'")
  Integer getNonDecodedCount(int sessionType);
  
  /**
   * Gets non decoded count.
   *
   * @param sessionType the session type
   * @return the non uploaded count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE session_type = :sessionType AND COALESCE(NULLIF(new_epc,''),epc) IS NOT NULL AND LENGTH(COALESCE(NULLIF(new_epc,''),epc))>=24 AND COALESCE(NULLIF(new_epc,''),epc) NOT LIKE '0%' AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) IN (:tids)")
  Integer getNonDecodedCount(int sessionType, Set<String> tids);
  
  /**
   * Gets decoded count.
   *
   * @param sessionType the session type
   * @return the decoded count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_type) = :sessionType AND COALESCE(NULLIF(new_epc,''),epc) IS NOT NULL AND LENGTH(COALESCE(NULLIF(new_epc,''),epc))>=24 AND COALESCE(NULLIF(new_epc,''),epc) LIKE '0%' AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) IN (:tids)")
  Integer getDecodedCount(int sessionType, Set<String> tids);
  
  /**
   * Gets inventory by epc.
   *
   * @param epc the epc
   * @return the inventory by epc
   */
  @Query("SELECT * FROM inventory WHERE TRIM(epc) COLLATE NOCASE = :epc")
  public Inventory getInventoryByEPC(String epc);
  
  @Query("SELECT * FROM inventory WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(tid) COLLATE NOCASE = :tid AND session_type = :sessionType")
  public Inventory getInventoryByEpcAndTid(int sessionType, String epc, String tid);
  
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM inventory WHERE TRIM(tid) COLLATE NOCASE = :tid AND TRIM(new_epc) COLLATE NOCASE = :newEpc AND TRIM(epc) COLLATE NOCASE = :epc AND TRIM(session_id) = :sessionId")
  public boolean isTagPresent(String sessionId, String epc, String newEpc, String tid);
  
  /**
   * Is epc present boolean.
   *
   * @param sessionId the session id
   * @param epc       the epc
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM inventory WHERE TRIM(epc) COLLATE NOCASE = :epc AND TRIM(session_id) = :sessionId")
  public boolean isEPCPresent(String sessionId, String epc);
  
  @Query("SELECT COALESCE(TRIM(epc),'') FROM inventory WHERE ino=(SELECT MAX(ino) FROM inventory WHERE TRIM(session_id) = :sessionId AND (epc IS NOT NULL AND LENGTH(TRIM(epc))>0))")
  public String getLastInsertedEpc(String sessionId);
  
  @Query("SELECT COALESCE(COUNT(epc),0)>0 FROM inventory WHERE ino=(SELECT MAX(ino) FROM inventory WHERE TRIM(session_id) = :sessionId AND (epc IS NOT NULL AND LENGTH(TRIM(epc))>0)) AND TRIM(epc) COLLATE NOCASE = :epc")
  public boolean isLastInsertedEpc(String sessionId, String epc);
  
  /**
   * Is tid present boolean.
   *
   * @param sessionId the session id
   * @param tid       the tid
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM inventory WHERE tid IS NOT NULL AND LENGTH(TRIM(tid))>0 AND TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId")
  public boolean isTidPresent(String sessionId, String tid);
  
  @Query("SELECT COALESCE(COUNT(*),0)>=:size FROM inventory WHERE tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) IN (:tids) AND TRIM(session_id) = :sessionId")
  public boolean isTidsPresent(String sessionId, List<String> tids, int size);
  
  /**
   * Is tid present boolean.
   *
   * @param sessionId the session id
   * @param tid       the tid
   * @return the boolean
   */
  @Query("SELECT ino FROM inventory WHERE tid IS NOT NULL AND LENGTH(TRIM(tid))>0 AND TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId")
  public Integer getRowIdFromTid(String sessionId, String tid);
  
  /**
   * Is epc present boolean.
   *
   * @param ean       the ean
   * @param sessionId the session id
   * @param epc       the epc
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM inventory WHERE (:ean IS NULL OR LENGTH(:ean)<=0 OR TRIM(ean) COLLATE NOCASE = :ean) AND TRIM(epc) COLLATE NOCASE = :epc AND TRIM(session_id) = :sessionId")
  public boolean isEPCPresent(String ean, String sessionId, String epc);
  
  /**
   * Is epc present boolean.
   *
   * @param ean       the ean
   * @param sessionId the session id
   * @param epc       the epc
   * @param tid       the epc
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM inventory WHERE (:ean IS NULL OR LENGTH(:ean)<=0 OR TRIM(ean) COLLATE NOCASE = :ean) AND TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId")
  public boolean isEpcAndTidPresent(String ean, String sessionId, String epc, String tid);
  
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM inventory WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(tid) COLLATE NOCASE = :tid AND session_type = :sessionType")
  public boolean isEpcAndTidPresent(int sessionType, String epc, String tid);
  
  @Query("SELECT COALESCE(is_found,0)>0 FROM inventory WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  public boolean isVerified(String sessionId, String epc, String tid);
  
  @Query("SELECT COALESCE(is_found,0)>0 FROM inventory WHERE TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId")
  public boolean isVerifiedByTid(String sessionId, String tid);
  
  @Query("SELECT COALESCE(is_found,0)>0 FROM inventory WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(session_id) = :sessionId")
  public boolean isVerifiedByEpc(String sessionId, String epc);
  
  @Query("UPDATE inventory SET is_found=1 WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId AND is_found<=0 AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  public Integer updateEncVerified(String sessionId, String epc, String tid);
  
  @Query("UPDATE inventory SET is_found=1 WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(session_id) = :sessionId AND is_found<=0 AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  public Integer updateEncVerifiedByEpc(String sessionId, String epc);
  
  @Query("UPDATE inventory SET is_found=1,enc_verify_status=:status WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId AND is_found<=0 AND (enc_verify_status IS NULL OR enc_verify_status<=2) AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  public Integer updateEncVerified(String sessionId, String epc, String tid, Integer status);
  
  @Query("UPDATE inventory SET is_found=1,enc_verify_status=:status WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(session_id) = :sessionId AND is_found<=0 AND (enc_verify_status IS NULL OR enc_verify_status<=2) AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  public Integer updateEncVerifiedByEpc(String sessionId, String epc, Integer status);
  
  @Query("UPDATE inventory SET enc_verify_status=:status WHERE TRIM(new_epc) COLLATE NOCASE = :epc AND TRIM(session_id) = :sessionId AND is_found<=0 AND (enc_verify_status IS NULL OR enc_verify_status<=2) AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  public Integer updateStatusByEpc(String sessionId, String epc, Integer status);
  
  @Query("UPDATE inventory SET enc_verify_status=:status WHERE TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId AND is_found<=0 AND (enc_verify_status IS NULL OR enc_verify_status<=2) AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  public Integer updateEncVerifyStatusByTid(String sessionId, String tid, Integer status);
  
  @Query("UPDATE inventory SET enc_verify_status=:status WHERE TRIM(epc) COLLATE NOCASE = :epc AND TRIM(session_id) = :sessionId AND is_found<=0 AND (enc_verify_status IS NULL OR enc_verify_status<=2) AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  public Integer updateEncVerifyStatusByEpc(String sessionId, String epc, Integer status);
  
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE tid IS NOT NULL AND LENGTH(TRIM(tid))>0 AND TRIM(tid) COLLATE NOCASE = :tid AND TRIM(session_id) = :sessionId AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  public Long getTidCount(String sessionId, String tid);
  
  /**
   * Gets all inventory data size.
   *
   * @param sessionId the session id
   * @return the all inventory data size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  LiveData<Integer> getAllInventoryDataSize(String sessionId);
  
  /**
   * Gets ean counts.
   *
   * @param sessionId the session id
   * @return the ean counts
   */
  @Transaction
  @Query("SELECT ean,COALESCE(COUNT(*),0) AS eanQty FROM inventory WHERE TRIM(session_id) = :sessionId GROUP BY ean")
  List<EanQty> getEanCounts(String sessionId);
  
  /**
   * Gets inventory size.
   *
   * @param sessionId the session id
   * @return the inventory size
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  Integer getInventorySize(String sessionId);
  
  /**
   * Gets inventory size.
   *
   * @param sessionType the session type
   * @return the inventory size
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE session_type= :sessionType AND (write_fail_reason IS NULL OR LENGTH(TRIM(write_fail_reason))<=0)")
  Integer getInventorySize(int sessionType);
  
  /**
   * Gets inventory score count.
   *
   * @param sessionId the session id
   * @return the inventory score count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND tag_status IS NOT NULL AND TRIM(tag_status) = '" + AppConstants.VALID + "'")
  Integer getInventoryScoreCount(String sessionId);
  
  /**
   * Gets unencoded tag count.
   *
   * @param sessionId the session id
   * @return the unencoded tag count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND tag_status IS NOT NULL AND TRIM(tag_status) = '" + AppConstants.NON_ENCODED + "'")
  Integer getUnencodedTagCount(String sessionId);
  
  /**
   * Gets align tag count.
   *
   * @param sessionId the session id
   * @return the align tag count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE TRIM(session_id) = :sessionId AND tag_status IS NOT NULL AND TRIM(tag_status) ='" + AppConstants.ALIEN + "'")
  Integer getAlignTagCount(String sessionId);
  
  /**
   * Gets unencoded tags.
   *
   * @param sessionId the session id
   * @return the unencoded tag count
   */
  @Query("SELECT * FROM inventory WHERE TRIM(session_id) = :sessionId AND tag_status IS NOT NULL AND TRIM(tag_status) = '" + AppConstants.NON_ENCODED + "'")
  List<Inventory> getUnencodedTags(String sessionId);
  
  /**
   * Gets align tags
   *
   * @param sessionId the session id
   * @return the align tag count
   */
  @Query("SELECT * FROM inventory WHERE TRIM(session_id) = :sessionId AND tag_status IS NOT NULL AND TRIM(tag_status) ='" + AppConstants.ALIEN + "'")
  List<Inventory> getAlignTags(String sessionId);
  
  /**
   * Gets all inventory data size.
   *
   * @param sessionType the session type
   * @return the all inventory data size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory WHERE session_type=:sessionType")
  LiveData<Integer> getAllInventoryDataSize(int sessionType);
  
  /**
   * Update uploaded.
   *
   * @param sessionId the session id
   */
  @Query("UPDATE inventory SET is_uploaded=1 WHERE TRIM(session_id) = :sessionId AND is_uploaded<=0")
  Integer updateUploaded(String sessionId);
  
  @Query("UPDATE inventory SET is_uploaded=1 WHERE TRIM(session_id) = :sessionId AND new_epc IS NOT NULL AND is_uploaded<=0")
  Integer updateUploadedAfterDecode(String sessionId);
  
  /**
   * Update uploaded.
   *
   * @param sessionId the session id
   * @param tids      the tids
   */
  @Query("UPDATE inventory SET is_uploaded=1 WHERE TRIM(session_id) = :sessionId AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) IN (:tids) AND is_uploaded<=0")
  Integer updateUploaded(String sessionId, Set<String> tids);
  
  
  /**
   * Update uploaded.
   *
   * @param sessionId the session id
   * @param tid     the tid
   * @param newEpc     the new epc
   */
  @Query("UPDATE inventory SET is_uploaded=1 WHERE TRIM(session_id) = :sessionId AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) = :tid AND new_epc IS NOT NULL AND LENGTH(new_epc)>0 AND TRIM(new_epc) = :newEpc AND is_uploaded<=0")
  Integer updateUploaded(String sessionId, String tid, String newEpc);
  
  /**
   * Update uploaded.
   *
   * @param sessionId the session id
   * @param epcs      the epcs
   */
  @Query("UPDATE inventory SET is_uploaded=1 WHERE TRIM(session_id) = :sessionId AND epc IS NOT NULL AND LENGTH(epc)>0 AND TRIM(epc) COLLATE NOCASE IN (:epcs) AND is_uploaded<=0")
  Integer updateUploaded(Set<String> epcs, String sessionId);
  
  /**
   * Update uploaded.
   *
   * @param sessionType the session type
   */
  @Query("UPDATE inventory SET is_uploaded=1 WHERE session_type = :sessionType AND is_uploaded<=0")
  Integer updateUploaded(int sessionType);
  
  /**
   * Update uploaded.
   *
   * @param sessionType the session type
   * @param tids        the tids
   */
  @Query("UPDATE inventory SET is_uploaded=1 WHERE session_type = :sessionType AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) IN (:tids) AND is_uploaded<=0")
  Integer updateUploaded(int sessionType, Set<String> tids);
  
  /**
   * Update upload retry count.
   *
   * @param sessionId the session id
   * @param tids      the tids
   */
  @Query("UPDATE inventory SET upload_retry_count=upload_retry_count+1 WHERE TRIM(session_id) = :sessionId AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) IN (:tids) AND is_uploaded<=0")
  Integer updateUploadRetryCount(String sessionId, Set<String> tids);
  
  /**
   * Update upload retry count.
   *
   * @param sessionId the session id
   * @param tid      the tid
   * @param newEpc  the newEpc
   */
  @Query("UPDATE inventory SET upload_retry_count=upload_retry_count+1 WHERE TRIM(session_id) = :sessionId AND tid IS NOT NULL AND LENGTH(tid)>0 AND SUBSTR(TRIM(tid),0,25) = (:tid) AND new_epc IS NOT NULL AND LENGTH(new_epc)>0 AND TRIM(new_epc) = (:newEpc) AND is_uploaded<=0")
  Integer updateUploadRetryCount(String sessionId, String tid, String newEpc);
  
  /**
   * Update upload retry count.
   *
   * @param sessionId the session id
   * @param epcs      the epcs
   */
  @Query("UPDATE inventory SET upload_retry_count=upload_retry_count+1 WHERE TRIM(session_id) = :sessionId AND tid IS NOT NULL AND LENGTH(epc)>0 AND TRIM(epc) COLLATE NOCASE IN (:epcs) AND is_uploaded<=0")
  Integer updateUploadRetryCount(Set<String> epcs, String sessionId);
  
  /**
   * Update upload retry count.
   *
   * @param sessionId the session id
   */
  @Query("UPDATE inventory SET upload_retry_count=upload_retry_count+1 WHERE TRIM(session_id) = :sessionId AND is_uploaded<=0")
  Integer updateUploadRetryCount(String sessionId);
  
  @Transaction
  @Query("SELECT DISTINCT session_id FROM inventory WHERE is_uploaded<=0 AND session_type IN (2,12,13,28)")
  List<String> getSessionIds();
}
