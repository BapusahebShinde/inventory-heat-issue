package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.SiteType;
import com.itek.retail.model.SiteType;

import java.util.List;

/**
 * The interface SiteType dao.
 */
@androidx.room.Dao
public interface SiteTypeDao{
  
  /**
   * Insert.
   *
   * @param siteTypes the site types
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(SiteType... siteTypes);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<SiteType> listSiteTypes);
  
  /**
   * Update.
   *
   * @param siteTypes the site types
   */
  @Update
  Integer update(SiteType... siteTypes);
  
  /**
   * Delete.
   *
   * @param site types the site types
   */
  @Delete
  Integer delete(SiteType... siteTypes);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM site_types")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM site_types")
  List<SiteType> getAll();
  
  /**
   * Gets siteType by id.
   *
   * @param siteTypeId the site type id
   * @return the site type by id
   */
  @Query("SELECT * FROM site_types WHERE site_type_id=:siteTypeId")
  SiteType getSiteTypeById(Long siteTypeId);
  
  /**
   * Gets siteType by name.
   *
   * @param siteTypeName the site type name
   * @return the site type by name
   */
  @Query("SELECT * FROM site_types WHERE TRIM(site_type_name)=:siteTypeName")
  SiteType getSiteTypeByName(String siteTypeName);
  
  /**
   * Gets all site types.
   *
   * @return the all site types
   */
  @Transaction
  @Query("SELECT TRIM(site_type_name) FROM site_types")
  List<String> getAllSiteTypes();
  
  /**
   * Gets all site types except.
   *
   * @param siteType the site type
   * @return the all site types except
   */
  @Transaction
  @Query("SELECT TRIM(site_type_name) FROM site_types WHERE TRIM(site_type_name) NOT IN (:siteType)")
  List<String> getAllSiteTypesExcept(String siteType);
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM site_types")
  Integer getTableSize();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM site_types")
  boolean hasData();
  
}
