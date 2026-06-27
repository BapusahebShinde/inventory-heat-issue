package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.BrandEans;

import java.util.List;

/**
 * The interface Brand dao.
 */
@androidx.room.Dao
public interface BrandEanDao{
  
  /**
   * Insert.
   *
   * @param brandEansQties the BrandEans
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(BrandEans... brandEansQties);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<BrandEans> brandEans);
  
  /**
   * Update.
   *
   * @param brandEansQties the BrandEans
   */
  @Update
  Integer update(BrandEans... brandEansQties);
  
  /**
   * Delete.
   *
   * @param brandEansQties the BrandEans
   */
  @Delete
  Integer delete(BrandEans... brandEansQties);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM brand_eans")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM brand_eans")
  List<BrandEans> getAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
  @Transaction
  @Query("SELECT brand_name,'' AS ean, SUM(scan_qty) AS scan_qty, SUM(total_qty) AS total_qty FROM brand_eans GROUP BY brand_name")
  List<BrandEans> getBrandwiseCount();
  
  /**
   * Update scan_count.
   *
   * @param ean the ean
   */
  @Query("UPDATE brand_eans SET scan_qty=scan_qty+1 WHERE TRIM(ean) LIKE '%'||:ean||'%'")
  Integer updateScanQty(String ean);
  
  /**
   * Update scan_count.
   *
   * @param brand the brand
   */
  @Query("UPDATE brand_eans SET total_qty=:totalQty WHERE TRIM(brand_name)=:brand")
  Integer updateTotalQty(String brand, int totalQty);
  
  /**
   * Is epc present boolean.
   *
   * @param ean the ean
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM brand_eans WHERE (:ean IS NOT NULL AND LENGTH(TRIM(:ean))>0 AND TRIM(ean) LIKE '%'||:ean||'%')")
  public boolean isEANPresent(String ean);
  
  /**
   * Gets all brand_eans.
   *
   * @return the all brand_eans
   */
  @Transaction
  @Query("SELECT TRIM(brand_name) FROM brand_eans")
  List<String> getAllBrands();
  
  @Transaction
  @Query("SELECT TRIM(brand_name) FROM brand_eans WHERE brand_name IS NOT NULL AND LENGTH(TRIM(brand_name))>0 AND scan_qty>0")
  List<String> getScanBrands();
  
  /**
   * Gets all brand_eans.
   *
   * @return the all brand_eans
   */
  @Transaction
  @Query("SELECT TRIM(ean) FROM brand_eans")
  List<String> getAllEans();
  
  /**
   * Gets all brand_eans except.
   *
   * @param brand the brand
   * @return the all brand_eans except
   */
  @Transaction
  @Query("SELECT TRIM(brand_name) FROM brand_eans WHERE TRIM(brand_name) NOT IN (:brand)")
  List<String> getAllBrandsExcept(String brand);
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM brand_eans")
  Integer getTableSize();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM brand_eans")
  boolean hasData();
  
}
