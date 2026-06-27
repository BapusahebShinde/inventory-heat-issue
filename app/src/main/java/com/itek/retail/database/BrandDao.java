package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.Brand;

import java.util.List;

/**
 * The interface Brand dao.
 */
@androidx.room.Dao
public interface BrandDao{
  
  /**
   * Insert.
   *
   * @param brands the brands
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Brand... brands);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<Brand> listBrands);
  
  /**
   * Update.
   *
   * @param brands the brands
   */
  @Update
  Integer update(Brand... brands);
  
  /**
   * Delete.
   *
   * @param brands the brands
   */
  @Delete
  Integer delete(Brand... brands);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM brands")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM brands")
  List<Brand> getAll();
  
  /**
   * Gets brand by name.
   *
   * @param brandName the brand name
   * @return the brand by name
   */
  @Query("SELECT * FROM brands WHERE TRIM(brand_name)=:brandName")
  Brand getBrandByName(String brandName);
  
  /**
   * Gets all brands.
   *
   * @return the all brands
   */
  @Transaction
  @Query("SELECT TRIM(brand_name) FROM brands")
  List<String> getAllBrands();
  
  /**
   * Gets all brands except.
   *
   * @param brand the brand
   * @return the all brands except
   */
  @Transaction
  @Query("SELECT TRIM(brand_name) FROM brands WHERE TRIM(brand_name) NOT IN (:brand)")
  List<String> getAllBrandsExcept(String brand);
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM brands")
  Integer getTableSize();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM brands")
  boolean hasData();
  
}
