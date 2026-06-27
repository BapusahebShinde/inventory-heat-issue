package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.SiteCode;

import java.util.List;

/**
 * The interface SiteCode dao.
 */
@androidx.room.Dao
public interface SiteCodeDao{
  
  /**
   * Insert.
   *
   * @param siteCodes the site codes
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(SiteCode... siteCodes);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<SiteCode> listSiteCodes);
  
  /**
   * Update.
   *
   * @param siteCodes the site codes
   */
  @Update
  Integer update(SiteCode... siteCodes);
  
  /**
   * Delete.
   *
   * @param siteCodes the site codes
   */
  @Delete
  Integer delete(SiteCode... siteCodes);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM site_codes")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM site_codes")
  List<SiteCode> getAll();
  
  /**
   * Gets siteCode by id.
   *
   * @param siteTypeId the site type id
   * @return the site code by id
   */
  @Query("SELECT * FROM site_codes WHERE site_type_id=:siteTypeId")
  List<SiteCode> getSiteCodeById(Long siteTypeId);
  
  /**
   * Gets site code name by id.
   *
   * @param siteTypeId the site type id
   * @return the site code name by id
   */
  @Query("SELECT TRIM(site_code) FROM site_codes WHERE site_type_id=:siteTypeId")
  List<String> getSiteCodeNameById(Long siteTypeId);
  
  /**
   * Gets siteType by name.
   *
   * @param siteCodeName the site code
   * @return the site code by name
   */
  @Query("SELECT * FROM site_codes WHERE TRIM(site_code)=:siteCodeName")
  SiteCode getSiteCodeByName(String siteCodeName);
  
  /**
   * Gets all site codes.
   *
   * @return the all site codes
   */
  @Transaction
  @Query("SELECT TRIM(site_code) FROM site_codes")
  List<String> getAllSiteCodes();
  
  /**
   * Gets all site codes except.
   *
   * @param siteCode the site code
   * @return the all site codes except
   */
  @Transaction
  @Query("SELECT TRIM(site_code) FROM site_codes WHERE TRIM(site_code) NOT IN (:siteCode)")
  List<String> getAllSiteCodesExcept(String siteCode);
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM site_codes")
  Integer getTableSize();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM site_codes")
  boolean hasData();
  
}
