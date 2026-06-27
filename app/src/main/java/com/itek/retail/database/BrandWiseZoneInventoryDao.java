package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.common.AppConstants;
import com.itek.retail.model.BrandWiseZoneInventory;

import java.util.List;
import java.util.Set;

/**
 * The interface Brand wise zone inventory dao.
 */
@androidx.room.Dao
public interface BrandWiseZoneInventoryDao{
  
  /**
   * Insert.
   *
   * @param brandWiseZoneInventories the brand wise zone inventories
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(BrandWiseZoneInventory... brandWiseZoneInventories);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<BrandWiseZoneInventory> productModels);
  
  /**
   * Update.
   *
   * @param brandWiseZoneInventories the brand wise zone inventories
   */
  @Update
  Integer update(BrandWiseZoneInventory... brandWiseZoneInventories);
  
  /**
   * Delete.
   *
   * @param brandWiseZoneInventories the brand wise zone inventories
   */
  @Delete
  Integer delete(BrandWiseZoneInventory... brandWiseZoneInventories);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM inventory_dashboard_count")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM inventory_dashboard_count")
  List<BrandWiseZoneInventory> getAll();
  
  /**
   * Gets zones.
   *
   * @return the zones
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(zone_name) FROM inventory_dashboard_count WHERE zone_name IS NOT NULL AND LENGTH(TRIM(zone_name))>0 AND TRIM(zone_name) !='" + AppConstants.ALL + "'")
  List<String> getZones();
  
  /**
   * Gets brands.
   *
   * @return the brands
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(brand_name) FROM inventory_dashboard_count WHERE brand_name IS NOT NULL AND LENGTH(TRIM(brand_name))>0 AND TRIM(brand_name) !='" + AppConstants.ALL + "'")
  List<String> getBrands();
  
  /**
   * Gets categories.
   *
   * @return the categories
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(category_name) FROM inventory_dashboard_count WHERE category_name IS NOT NULL AND LENGTH(TRIM(category_name))>0 AND TRIM(category_name) !='" + AppConstants.ALL + "'")
  List<String> getCategories();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM inventory_dashboard_count")
  boolean hasData();
  
  /**
   * Gets inv count.
   *
   * @param zone the location
   * @return the inv count
   */
  @Query("SELECT COALESCE(SUM(inventory_count),0) FROM inventory_dashboard_count WHERE (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone='" + AppConstants.ALL + "' OR TRIM(zone_name)=:zone OR TRIM(zone_name)IN(:zones))")
  Long getInvCount(String zone, Set<String> zones);
  
  /**
   * Gets inv count.
   *
   * @param zone  the zone
   * @param zones the zones
   * @param brand the brand
   * @return the inv count
   */
  @Query("SELECT COALESCE(SUM(inventory_count),0) FROM inventory_dashboard_count WHERE (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone='" + AppConstants.ALL + "' OR TRIM(zone_name)=:zone OR TRIM(zone_name)IN(:zones)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand_name)=:brand)")
  Long getInvCount(String zone, Set<String> zones, String brand);
  
  /**
   * Gets inv count.
   *
   * @param zone   the zone
   * @param zones  the zones
   * @param brand  the brand
   * @param brands the brands
   * @return the inv count
   */
  @Query("SELECT COALESCE(SUM(inventory_count),0) FROM inventory_dashboard_count WHERE (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone='" + AppConstants.ALL + "' OR TRIM(zone_name)=:zone OR TRIM(zone_name) IN(:zones)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand='" + AppConstants.ALL + "' OR TRIM(brand_name)=:brand OR TRIM(brand_name) IN(:brands))")
  Long getInvCount(String zone, Set<String> zones, String brand, Set<String> brands);
  
  /**
   * Gets counts.
   *
   * @param zone   the zone
   * @param zones  the zones
   * @param brand  the brand
   * @param brands the brands
   * @return the counts
   */
  @Query("SELECT TRIM(COALESCE(SUM(inventory_count),0)||','||COALESCE(SUM(shortage),0)) FROM inventory_dashboard_count WHERE (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone ='" + AppConstants.ALL + "' OR TRIM(zone_name) = :zone OR TRIM(zone_name) IN(:zones)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand ='" + AppConstants.ALL + "' OR TRIM(brand_name) = :brand OR TRIM(brand_name) IN (:brands))")
  String getCounts(String zone, Set<String> zones, String brand, Set<String> brands);
  
  /**
   * Gets counts.
   *
   * @param zone       the zone
   * @param zones      the zones
   * @param brand      the brand
   * @param brands     the brands
   * @param category   the category
   * @param categories the categories
   * @return the counts
   */
  @Query("SELECT TRIM(COALESCE(SUM(inventory_count),0)||','||COALESCE(SUM(shortage),0)) FROM inventory_dashboard_count WHERE (:zone IS NULL OR LENGTH(:zone)<=0 OR :zone ='" + AppConstants.ALL + "' OR TRIM(zone_name) = :zone OR TRIM(zone_name) IN(:zones)) AND (:brand IS NULL OR LENGTH(:brand)<=0 OR :brand ='" + AppConstants.ALL + "' OR TRIM(brand_name) = :brand OR TRIM(brand_name) IN (:brands)) AND (:category IS NULL OR LENGTH(:category)<=0 OR :category ='" + AppConstants.ALL + "' OR TRIM(category_name) = :category OR TRIM(category_name) IN (:categories))")
  String getCounts(String zone, Set<String> zones, String brand, Set<String> brands, String category, Set<String> categories);
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM inventory_dashboard_count")
  Integer getTableSize();
  
}
