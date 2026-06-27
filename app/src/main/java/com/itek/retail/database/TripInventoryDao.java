package com.itek.retail.database;

import androidx.lifecycle.LiveData;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.common.AppConstants;
import com.itek.retail.model.InwardHuVerificationModel;
import com.itek.retail.model.MultiQtyModel;
import com.itek.retail.model.TripInventory;

import java.util.List;
import java.util.Set;

/**
 * The interface Trip inventory dao.
 */
@androidx.room.Dao
public interface TripInventoryDao{
  
  /**
   * Insert trip inventory data.
   *
   * @param tripInventories the trip inventories
   */
  @Insert
  void insertTripInventoryData(TripInventory... tripInventories);
  
  @Insert
  void insertAll(List<TripInventory> tripInventories);
  
  /**
   * Update trip inventory data.
   *
   * @param tripInventories the trip inventories
   */
  @Update
  Integer updateTripInventoryData(TripInventory... tripInventories);
  
  /**
   * Delete all trip inventory.
   */
  @Query("DELETE FROM trip_inventory")
  public void deleteAllTripInventory();
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM trip_status")
  public void deleteAll();
  
  /**
   * Delete trip inventory.
   *
   * @param tripno the tripno
   */
  @Query("DELETE FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripno")
  public Integer deleteTripInventory(String tripno);
  
  /**
   * Delete inventory.
   *
   * @param tripno the tripno
   * @param huno the huno
   */
  @Query("DELETE FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripno AND TRIM(hu_no) = :huno")
  public Integer deleteInventory(String tripno,String huno);
  
  /**
   * Delete all trip inventory data.
   *
   * @param tripno the tripno
   */
  @Query("DELETE FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripno")
  public void deleteAllTripInventoryData(String tripno);
  
  /**
   * Delete all uploaded trip inventory data.
   *
   * @param listTripNo the list trip no
   */
  @Query("DELETE FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  IN(:listTripNo)")
  public Integer deleteAllUploadedTripInventoryData(Set<String> listTripNo);
  
  /**
   * Delete trip inventory data.
   *
   * @param tripno the tripno
   */
  @Query("DELETE FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripno AND TRIM(epc)!=''")
  public Integer deleteTripInventoryData(String tripno);
  
  /**
   * Gets trip inventory data size.
   *
   * @param tripNo the trip no
   * @return the trip inventory data size
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE = :tripNo")
  public Integer getTripInventoryDataSize(String tripNo);
  
  /**
   * Gets trip inventory data size.
   *
   * @param tripNo the trip no
   * @param huNo   the hu no
   * @return the trip inventory data size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE = :tripNo AND TRIM(hu_no)=:huNo AND TRIM(epc)!=''")
  public LiveData<Integer> getTripInventoryDataSize(String tripNo, String huNo);
  
  /**
   * Is epc present boolean.
   *
   * @param tripNo the trip no
   * @param huNo   the hu no
   * @param epc    the epc
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE = :tripNo AND TRIM(hu_no)=:huNo AND TRIM(epc) COLLATE NOCASE =:epc")
  public boolean isEpcPresent(String tripNo, String huNo, String epc);

  /**
   * Is serial present boolean.
   *
   * @param tripNo   the trip no
   * @param huNo     the hu no
   * @param serialNo the serial no
   * @return the boolean
   */

  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE = :tripNo AND TRIM(hu_no)=:huNo AND TRIM(serial_no) COLLATE NOCASE =:serialNo")
  public boolean isSerialPresent(String tripNo, String huNo, String serialNo);
  
  /**
   * Gets all trip count details.
   *
   * @param tripNo the trip no
   * @return the all trip count details
   */
  @Transaction
  @Query("SELECT (CASE WHEN status='A' THEN 1 ELSE 0 END) || ',' ||(CASE WHEN status='R' THEN 1 ELSE 0 END) || ',' || (CASE WHEN status='P' THEN 1 ELSE 0 END)  FROM trip_inventory WHERE epc='' AND trip_no = :tripNo GROUP BY hu_no ")
  public List<String> getAllTripCountDetails(String tripNo);
  
  /**
   * Gets all trip inventory.
   *
   * @param tripNo the trip no
   * @return the all trip inventory
   */
  @Transaction
  @Query("SELECT * FROM trip_inventory WHERE TRIM(status)!= 'P' AND TRIM(trip_no) COLLATE NOCASE  = :tripNo ORDER BY hu_no")
  List<TripInventory> getAllTripInventory(String tripNo);
  
  /**
   * Gets all trip inventory.
   *
   * @param tripNo the trip no
   * @param huNo   the hu no
   * @return the all trip inventory
   */
  @Query("SELECT * FROM trip_inventory WHERE TRIM(status)!= 'P' AND TRIM(trip_no) COLLATE NOCASE  = :tripNo AND TRIM(hu_no)=:huNo")
  List<TripInventory> getAllTripInventory(String tripNo, String huNo);
  
  @Query("SELECT * FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNo AND TRIM(hu_no)=:huNo")
  List<TripInventory> getAll(String tripNo, String huNo);
  
  @Query("SELECT * FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNo AND TRIM(hu_no)=:huNo AND epc!=''")
  List<TripInventory> getAllScannedData(String tripNo, String huNo);

  @Query("SELECT * FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNo AND TRIM(hu_no)=:huNo AND epc!='' ORDER BY ean ASC")
  List<TripInventory> getAllScannedData1(String tripNo, String huNo);
  
  @Query("SELECT COALESCE(COUNT(hu_no),0)>0 FROM trip_inventory WHERE is_server_entry > 0 AND TRIM(trip_no) COLLATE NOCASE  = :tripNo AND TRIM(hu_no)=:huNo")
  public boolean hasData(String tripNo, String huNo);
  
  /**
   * Gets original hu details.
   *
   * @param tripNo the trip no
   * @return the original hu details
   */
  @Transaction
  @Query("SELECT *, COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0) AS huQty,COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END),0) AS scanCount FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNo GROUP BY hu_no  order BY is_duplicate desc")
  public List<TripInventory> getOriginalHuDetails(String tripNo);
  
  
  @Transaction
  @Query("SELECT *, COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0) AS huQty,COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END),0) AS scanCount FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNo AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(hu_no) LIKE '%'||:searchName||'%') GROUP BY hu_no  order BY is_duplicate desc")
  public List<TripInventory> getOriginalHuDetails(String tripNo,String searchName);
  
  /**
   * Gets original hu details.
   *
   * @param tripNo the trip no
   * @return the original hu details
   */
  @Query("SELECT COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0)==COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END),0) FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNo AND status != '" + AppConstants.TRIP_STATUS_COMPLETED + "'")
  public boolean getHuComplete(String tripNo);
  
  /**
   * Gets original hu article details.
   *
   * @param tripNo the trip no
   * @return the original hu article details
   */
  @Query("SELECT * ,COALESCE(SUM(CASE WHEN epc='' THEN article_code ELSE 0 END),0)  AS huQty,COALESCE(SUM(CASE WHEN TRIM(epc)!='' THEN 1 ELSE 0 END),0) AS scanCount FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNo GROUP BY hu_no  order BY is_duplicate desc ")
  public List<TripInventory> getOriginalHuArticleDetails(String tripNo);
  
  /**
   * Gets current hu details.
   *
   * @param huNumber the hu number
   * @param tripNo   the trip no
   * @return the current hu details
   */
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Transaction
  @Query("SELECT * ,COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END),0)  AS scanCount,COALESCE(SUM(ean_qty),0)  AS hu_qty FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE  = :tripNo GROUP BY (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) order BY is_server_entry desc")
  public List<TripInventory> getCurrentHuDetails1(String huNumber, String tripNo);
  
  @Transaction
  @Query("SELECT * ,COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END),0)  AS scanCount FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE  = :tripNo GROUP BY is_original,(CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) order BY is_server_entry desc")
  public List<TripInventory> getCurrentHuDetails(String huNumber, String tripNo);
  
  @Transaction
  @Query("SELECT * ,COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END),0)  AS scanCount FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE  = :tripNo GROUP BY is_original,(CASE WHEN :isArticleBasedTrip AND article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) order BY is_server_entry desc")
  public List<TripInventory> getCurrentHuDetails(String huNumber, String tripNo,final boolean isArticleBasedTrip);
  
  @Transaction
  @Query("SELECT * , COALESCE(CASE WHEN epc!='' THEN 1 ELSE 0 END,0) AS scanCount FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE  = :tripNo order BY is_server_entry, is_original desc")
  public List<TripInventory> getCurrentHuScanList(String huNumber, String tripNo);
  
  @Transaction
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Query("SELECT (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) AS 'title',COALESCE(SUM(ean_qty),0) AS 'total',COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END),0) AS 'found' FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)= :huNo GROUP BY (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) ORDER BY (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END) DESC")
  List<MultiQtyModel> getHUDisplayDetails(String tripNo, String huNo);
  
  
  @Query("SELECT COALESCE(COUNT(article_code),0)>0 FROM trip_inventory WHERE is_server_entry > 0 AND TRIM(trip_no) COLLATE NOCASE =:tripNo AND TRIM(hu_no)=:huNo AND article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"'")
  public boolean hasArticleData(String tripNo, String huNo);
  
  /**
   * Gets current hu details.
   *
   * @param huNumber the hu number
   * @param tripNo   the trip no
   * @return the current hu details
   */
  //@Transaction
  //@Query("SELECT * ,COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END),0)  AS scanCount FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE  = :tripNo GROUP BY (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+AppConstants.UNKNOWN+"' THEN article_code ELSE ean END)  ORDER BY ean DESC")
  //public List<TripInventory> getCurrentHuDetails(String huNumber, String tripNo);
  
  /**
   * Gets current hu artical details.
   *
   * @param huNumber    the hu number
   * @param articalcode the articalcode
   * @param tripNo      the trip no
   * @return the current hu artical details
   */
  @Transaction
  @Query("SELECT * ,COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END),0) AS scanCount FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(article_code) = :articalcode AND TRIM(trip_no) COLLATE NOCASE  = :tripNo GROUP BY ean order BY is_server_entry desc")
  public List<TripInventory> getCurrentHuArticalDetails(String huNumber, String articalcode, String tripNo);
  
  /**
   * Gets all hus duplicate rfid.
   *
   * @param tripNo the trip no
   * @return the all hus duplicate rfid
   */
  @Transaction
  @Query("SELECT DISTINCT hu_no FROM trip_inventory WHERE trip_no = :tripNo AND TRIM(epc) COLLATE NOCASE IN (SELECT epc FROM trip_inventory  WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNo AND epc!='' AND user_action!='MANUAL' GROUP BY epc HAVING COUNT(epc) > 1)")
  public List<String> getAllHusDuplicateRfid(String tripNo);
  
  /**
   * Gets processed hu count.
   *
   * @param tripNo the trip no
   * @return the processed hu count
   */
  @Query("SELECT COALESCE(COUNT(Distinct hu_no),0) FROM trip_inventory WHERE TRIM(status) != 'P' AND TRIM(trip_no) COLLATE NOCASE  = :tripNo")
  public int getProcessedHuCount(String tripNo);
  
  /**
   * Gets un processed hu count.
   *
   * @param tripNo the trip no
   * @return the un processed hu count
   */
  @Query("SELECT COALESCE(COUNT(Distinct hu_no),0) FROM trip_inventory WHERE status = 'P' AND TRIM(trip_no) COLLATE NOCASE  = :tripNo")
  public int getUnProcessedHuCount(String tripNo);
  
  /**
   * Gets current hu qty.
   *
   * @param huNumber the hu number
   * @param tripNo   the trip no
   * @return the current hu qty
   */
  @Query("SELECT COALESCE(SUM(ean_qty),0) FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNo AND hu_no = :huNumber AND epc='' AND article_code IS NOT NULL AND LENGTH(article_code)>0 AND ean IS NOT NULL AND LENGTH(ean)>0")
  public Integer getCurrentHuQty(String huNumber, String tripNo);
  
  /**
   * Gets current hu article qty.
   *
   * @param huNumber the hu number
   * @param tripNo   the trip no
   * @return the current hu article qty
   */
  @Query("SELECT COALESCE(COUNT(article_code),0) FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNo AND hu_no = :huNumber AND epc='' AND  article_code IS NOT NULL AND LENGTH(article_code)>0")
  public Integer getCurrentHuArticleQty(String huNumber, String tripNo);
  
  /**
   * Gets current hu ean qty.
   *
   * @param huNumber the hu number
   * @param tripNo   the trip no
   * @return the current hu ean qty
   */
  @Query("SELECT COALESCE(COUNT(ean),0) FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNo AND hu_no = :huNumber AND epc='' AND article_code IS NOT NULL AND LENGTH(article_code)>0 AND ean IS NOT NULL AND LENGTH(ean)>0")
  public Integer getCurrentHuEanQty(String huNumber, String tripNo);
  
  /**
   * Gets original article qty.
   *
   * @param tripNumber  the trip number
   * @param huNumber    the hu number
   * @param articalcode the articalcode
   * @return the original article qty
   */
  @Query("SELECT COALESCE(ean_qty,0) FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE = :tripNumber AND hu_no= :huNumber AND article_code = :articalcode AND epc ='' AND is_server_entry>0")
  public Integer getOriginalArticleQty(String tripNumber, String huNumber, String articalcode);
  
  /**
   * Gets original ean qty.
   *
   * @param tripNumber  the trip number
   * @param huNumber    the hu number
   * @param ean the ean
   * @return the original ean qty
   */
  @Query("SELECT COALESCE(ean_qty,0) FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE = :tripNumber AND TRIM(hu_no)= :huNumber AND TRIM(ean) = :ean AND epc ='' AND is_server_entry>0")
  public Integer getOriginalEanQty(String tripNumber, String huNumber, String ean);
  
  /**
   * Gets article code.
   *
   * @param eanNumber  the ean number
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the article code
   */
  @Query("SELECT article_code FROM trip_inventory WHERE (','||ean||',') LIKE '%' || ','||:eanNumber||',' || '%' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber AND TRIM(hu_no)= :huNumber AND is_server_entry>0")
  public String getArticleCode(String eanNumber, String huNumber, String tripNumber);
  
  /**
   * Gets ean code.
   *
   * @param eanNumber  the ean number
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the ean code
   */
  @Query("SELECT ean FROM trip_inventory WHERE ean IS NOT NULL AND TRIM(ean) COLLATE NOCASE = :eanNumber AND TRIM(trip_no) COLLATE NOCASE = :tripNumber AND TRIM(hu_no)= :huNumber AND is_server_entry>0")
  public String getEanCode(String eanNumber, String huNumber, String tripNumber);
  
  /**
   * Update hu status.
   *
   * @param huNumber     the hu number
   * @param status       the status
   * @param isRescan     the is rescan
   * @param stringreason the stringreason
   * @param tripNumber   the trip number
   */
  @Query("UPDATE trip_inventory SET status = :status, is_rescan = :isRescan, reason = :stringreason  WHERE TRIM(hu_no)= :huNumber AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public Integer updateHUStatus(String huNumber, String status, Boolean isRescan, String stringreason, String tripNumber);
  
  /**
   * Gets is ean qty matched.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the is ean qty matched
   */
  @Query("SELECT COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END ),0)=COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0) FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public Boolean getIsEanQtyMatched(String huNumber, String tripNumber);
  
  /**
   * Gets is ean qty lower percent matched.
   *
   * @param huNumber       the hu number
   * @param lowerTolerance the lower tolerance
   * @param tripNumber     the trip number
   * @return the is ean qty lower percent matched
   */
  @Query("SELECT COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END ),0)<COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0) AND COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0)>0 AND (COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END ),0)*100/COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0)>=(100 - :lowerTolerance)) FROM trip_inventory WHERE hu_no = :huNumber AND trip_no= :tripNumber ")
  public Boolean getIsEanQtyLowerPercentMatched(String huNumber, int lowerTolerance, String tripNumber);
  
  /**
   * Gets iseanqty lower percent.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the iseanqty lower percent
   */
  @Query("SELECT COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END ),0)<COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0)  FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public Boolean getIseanqtyLowerPercent(String huNumber, String tripNumber);
  
  /**
   * Gets iseanqty upper percent matched.
   *
   * @param huNumber       the hu number
   * @param upperTolerance the upper tolerance
   * @param tripNumber     the trip number
   * @return the iseanqty upper percent matched
   */
  @Query("SELECT COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END ),0)>COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0) AND COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END)>0,0) AND  COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END ),0)*100/COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0)<=(100 + :upperTolerance)FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE = :tripNumber ")
  public Boolean getIseanqtyUpperPercentMatched(String huNumber, int upperTolerance, String tripNumber);
  
  /**
   * Gets iseanqty upper percent exceed.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the iseanqty upper percent exceed
   */
  @Query("SELECT COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END ),0)>COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0) FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public Boolean getIseanqtyUpperPercentExceed(String huNumber, String tripNumber);
  
  /**
   * Gets tag type count.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the tag type count
   */
  @Query("SELECT COALESCE(SUM(CASE WHEN is_hard_tag=0 THEN 1 ELSE 0 END ),0) ||','|| COALESCE(SUM(CASE WHEN is_hard_tag=1 THEN 1 ELSE 0 END ),0) FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE = :tripNumber AND TRIM(epc)!=''")
  public String getTagTypeCount(String huNumber, String tripNumber);
  
  /**
   * Is mixed tags present boolean.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(DISTINCT is_hard_tag),0)>1  FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE = :tripNumber AND TRIM(epc)!=''")
  public Boolean isMixedTagsPresent(String huNumber, String tripNumber);
  
  /**
   * Gets is happy flow.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the is happy flow
   */
  @Query("SELECT COALESCE(SUM(CASE WHEN epc!='' AND is_original>0 THEN 1 ELSE 0 END ),0)=COALESCE(SUM(CASE WHEN epc='' THEN ean_qty ELSE 0 END),0) FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public Boolean getIsHappyFlow(String huNumber, String tripNumber);
  
  /**
   * Gets hu status.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the hu status
   */
  @Query("SELECT status FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc) COLLATE NOCASE ='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public String getHuStatus(String huNumber, String tripNumber);
  
  /**
   * Gets is duplicate.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the is duplicate
   */
  @Query("SELECT is_duplicate FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc) COLLATE NOCASE ='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public Boolean getIsDuplicate(String huNumber, String tripNumber);
  
  /**
   * Is happy status boolean.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc) COLLATE NOCASE ='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber AND is_rescan<=0 AND TRIM(status) ='A'")
  public Boolean isHappyStatus(String huNumber, String tripNumber);
  
  /**
   * Gets is happy flow eancount.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the is happy flow eancount
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(ean),0)=ean_qty FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc)!='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber GROUP BY (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END)")
  public List<Boolean> getIsHappyFlowEanCount(String huNumber, String tripNumber);
  
  /**
   * Gets is happy flow eancount.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the is happy flow eancount
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(ean),0)=ean_qty FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc)!='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber GROUP BY (CASE WHEN :isArticleBasedTrip AND article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END)")
  public List<Boolean> getIsHappyFlowEanCount(String huNumber, String tripNumber, final boolean isArticleBasedTrip);
  
  /**
   * Gets mismatching eancount.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the mismatching eancount
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(ean),0)>ean_qty FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc)!='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber GROUP BY (CASE WHEN article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END)")
  public List<Boolean> getMismatchingEancount(String huNumber, String tripNumber);
  
  /**
   * Gets mismatching eancount.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the mismatching eancount
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(ean),0)>ean_qty FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc)!='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber GROUP BY (CASE WHEN :isArticleBasedTrip AND article_code IS NOT NULL AND LENGTH(TRIM(article_code))>0 AND TRIM(article_code)!='"+ AppConstants.UNKNOWN+"' THEN article_code ELSE ean END)")
  public List<Boolean> getMismatchingEancount(String huNumber, String tripNumber,final boolean isArticleBasedTrip);
  
  /**
   * Delete duplicate hu.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   */
  @Query("Delete FROM trip_inventory WHERE TRIM(hu_no) IN( :huNumber) AND TRIM(epc)!='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public Integer deleteDuplicateHU(List<String> huNumber, String tripNumber);
  
  /**
   * Delete hu.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   */
  @Query("Delete FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc)!='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public Integer deleteHU(String huNumber, String tripNumber);
  
  /**
   * Update h uattemptcount.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   */
  @Query("UPDATE trip_inventory SET attempts = (attempts + 1), status = 'P',reason = '',is_rescan=1 WHERE TRIM(hu_no)= :huNumber  AND TRIM(epc) COLLATE NOCASE ='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public Integer updateHUattemptcount(String huNumber, String tripNumber);
  
  /**
   * Reset trip status.
   *
   * @param tripNumber the trip number
   */
  @Query("UPDATE trip_inventory SET attempts = 0, status = 'P',reason = '',is_rescan=1 WHERE TRIM(epc) COLLATE NOCASE ='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public void resetTripStatus(String tripNumber);
  
  /**
   * Update duplicate h uattemptcount.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   */
  @Query("UPDATE trip_inventory SET attempts = (attempts + 1), status = 'P',reason = '',is_rescan=1,is_duplicate =1 WHERE hu_no  IN( :huNumber)  AND TRIM(epc) COLLATE NOCASE ='' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public Integer updateDuplicateHUattemptcount(List<String> huNumber, String tripNumber);
  
  /**
   * Gets is non encoded.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the is non encoded
   */
  @Query("SELECT COALESCE(COUNT(ean),0)>0 FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc)!='' AND is_original<=0 AND TRIM(ean) COLLATE NOCASE ='" + AppConstants.NON_ENCODED + "' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber ")
  public Boolean getIsNonEncoded(String huNumber, String tripNumber);
  
  /**
   * Gets is non encoded.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the is non encoded
   */
  @Query("SELECT COALESCE(COUNT(ean),0)>0 FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc)!='' AND is_original<=0 AND TRIM(ean) COLLATE NOCASE ='" + AppConstants.UNKNOWN + "' AND TRIM(trip_no) COLLATE NOCASE = :tripNumber ")
  public Boolean getIsUnknown(String huNumber, String tripNumber);
  
  /**
   * Gets is ean extra.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the is ean extra
   */
  @Query("SELECT COALESCE(COUNT(ean),0)>0 FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc)!='' AND is_original<=0 AND TRIM(trip_no) COLLATE NOCASE = :tripNumber")
  public Boolean getIsEanExtra(String huNumber, String tripNumber);
  
  /**
   * Ge trip no of hu string.
   *
   * @param huNumber the hu number
   * @return the string
   */
  @Query("SELECT DISTINCT trip_no FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(epc) COLLATE NOCASE =''")
  public String geTripNoOfHu(String huNumber);
  
  /**
   * Is hu verified boolean.
   *
   * @param tripNumber the trip number
   * @param huNumber   the hu number
   * @return the boolean
   */
  @Query("SELECT is_hu_verified FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNumber AND TRIM(hu_no) = :huNumber AND TRIM(epc) COLLATE NOCASE ='' GROUP BY hu_no")
  public Boolean isHuVerified(String tripNumber, String huNumber);
  
  /**
   * Is hu present in trip boolean.
   *
   * @param tripNumber the trip number
   * @param huNumber   the hu number
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM trip_inventory WHERE TRIM(trip_no) COLLATE NOCASE  = :tripNumber AND TRIM(hu_no) = :huNumber AND TRIM(epc) COLLATE NOCASE =''")
  public Boolean isHuPresentInTrip(String tripNumber, String huNumber);
  
  /**
   * Update hu verified.
   *
   * @param tripNumber the trip number
   * @param huNumber   the hu number
   */
  @Query("UPDATE trip_inventory set  is_hu_verified=1 WHERE  TRIM(trip_no) COLLATE NOCASE  = :tripNumber AND TRIM(hu_no) = :huNumber AND TRIM(epc) COLLATE NOCASE =''")
  public Integer updateHuVerified(String tripNumber, String huNumber);
  
  /**
   * Gets hu verified.
   *
   * @param tripNum the trip num
   * @return the hu verified
   */
  @Transaction
  @Query("SELECT DISTINCT hu_no AS huNumbers, (CASE WHEN is_hu_verified>0 THEN 'Verified' ELSE '' END) AS status FROM trip_inventory WHERE (:tripNum IS NULL OR LENGTH(:tripNum)<=0 OR TRIM(trip_no) COLLATE NOCASE  = :tripNum)")
  public List<InwardHuVerificationModel> getHuVerified(String tripNum);
  
  /**
   * Gets extra ean count.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the extra ean count
   */
  @Query("SELECT COALESCE(COUNT(ean_qty),0) FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE  = :tripNumber AND TRIM(epc)!='' AND is_original<=0 AND TRIM(ean)!='" + AppConstants.NON_ENCODED + "'")
  public Integer getExtraEanCount(String huNumber, String tripNumber);
  
  /**
   * Gets non encoded count.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the non encoded count
   */
  @Query("SELECT COALESCE(COUNT(ean_qty),0) FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE  = :tripNumber AND TRIM(epc)!='' AND is_original<=0 AND TRIM(ean) COLLATE NOCASE ='" + AppConstants.NON_ENCODED + "' ")
  public int getNonEncodedCount(String huNumber, String tripNumber);
  
  /**
   * Gets non encoded count.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the non encoded count
   */
  @Query("SELECT COALESCE(COUNT(ean_qty),0) FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE  = :tripNumber AND TRIM(epc)!='' AND is_original<=0 AND TRIM(ean) COLLATE NOCASE ='" + AppConstants.UNKNOWN + "' ")
  public int getUnknownCount(String huNumber, String tripNumber);
  
  /**
   * Gets articles original ean count.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the articles original ean count
   */
  @Query("SELECT COALESCE(COUNT(ean),0) FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE  = :tripNumber AND TRIM(epc)!='' AND is_original=1")
  public int getArticlesOriginalEanCount(String huNumber, String tripNumber);
  
  /**
   * Is hu exists boolean.
   *
   * @param huNumber   the hu number
   * @param tripNumber the trip number
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(hu_no),0)>0 FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(trip_no) COLLATE NOCASE  = :tripNumber")
  public Boolean isHuExists(String huNumber, String tripNumber);
  
  /**
   * Gets current hu artical details.
   *
   * @param huNumber    the hu number
   * @param articalcode the articalcode
   * @return the current hu artical details
   */
  @Transaction
  @Query("SELECT * ,COALESCE(SUM(CASE WHEN epc!='' THEN 1 ELSE 0 END),0) AS scanCount FROM trip_inventory WHERE TRIM(hu_no) = :huNumber AND TRIM(article_code) = :articalcode GROUP BY ean ORDER BY is_server_entry desc")
  public List<TripInventory> getCurrentHuArticalDetails(String huNumber, String articalcode);
  
  @Transaction
  @Query("SELECT serial_no FROM trip_inventory WHERE LENGTH(TRIM(serial_no))>0 AND TRIM(hu_no) = :huNumber AND (TRIM(article_code) = :articalcode OR TRIM(ean) = :articalcode) AND is_original=:isOriginal ORDER BY is_server_entry desc")
  public List<String> getCurrentHuSerialDetails(String huNumber, String articalcode, boolean isOriginal);
  
  /**
   * Gets hu status counts.
   *
   * @return the hu status counts
   */
  @Query("SELECT COALESCE(SUM(CASE WHEN status='A' THEN 1 ELSE 0 END ),0)||','||COALESCE(SUM(CASE WHEN TRIM(status)='R' THEN 1 ELSE 0 END ),0)||','||COALESCE(SUM(CASE WHEN TRIM(status)='P' THEN 1 ELSE 0 END ),0) FROM trip_inventory WHERE epc=''")
  public String getHUStatusCounts();
  
  /**
   * Gets hu status complete count.
   *
   * @return the hu status complete count
   */
  @Query("SELECT COALESCE(COUNT(DISTINCT hu_no),0) FROM trip_inventory WHERE epc='' AND status!='P'")
  public Integer getHUStatusCompleteCount();
  
  /**
   * Gets hu status all count.
   *
   * @return the hu status all count
   */
  @Query("SELECT COALESCE(COUNT(DISTINCT hu_no),0) FROM trip_inventory WHERE epc=''")
  public Integer getHUStatusAllCount();
}
