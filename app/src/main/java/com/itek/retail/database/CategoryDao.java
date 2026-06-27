package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.Category;

import java.util.List;

/**
 * The interface Category dao.
 */
@androidx.room.Dao
public interface CategoryDao{
  
  /**
   * Insert.
   *
   * @param categories the categories
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Category... categories);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<Category> listCategories);
  
  /**
   * Update.
   *
   * @param categories the categories
   */
  @Update
  Integer update(Category... categories);
  
  /**
   * Delete.
   *
   * @param categories the categories
   */
  @Delete
  Integer delete(Category... categories);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM categories")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM categories")
  List<Category> getAll();
  
  /**
   * Gets category by name.
   *
   * @param categoryName the category name
   * @return the category by name
   */
  @Query("SELECT * FROM categories WHERE TRIM(category_name)=:categoryName")
  Category getCategoryByName(String categoryName);
  
  /**
   * Gets all categories.
   *
   * @return the all categories
   */
  @Transaction
  @Query("SELECT TRIM(category_name) FROM categories")
  List<String> getAllCategories();
  
  /**
   * Gets all categories except.
   *
   * @param category the category
   * @return the all categories except
   */
  @Transaction
  @Query("SELECT TRIM(category_name) FROM categories WHERE TRIM(category_name) NOT IN (:category)")
  List<String> getAllCategoriesExcept(String category);
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM categories")
  Integer getTableSize();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM categories")
  boolean hasData();
  
}
