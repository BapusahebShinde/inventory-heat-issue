package com.itek.retail.database;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;
import static com.itek.retail.common.AppCommonMethods.toUnderScoreCase;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.itek.retail.common.AppConstants;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.ReplenishBatchDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * The interface ReplenishBatchDetails dao.
 */
@androidx.room.Dao
public interface ReplenishBatchDetailsDao{
  
  /**
   * Insert.
   *
   * @param replenishBatchDetails the replenishBatchDetails
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(ReplenishBatchDetails... replenishBatchDetails);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<ReplenishBatchDetails> listReplenishBatchDetails);
  
  /**
   * Update.
   *
   * @param replenishBatchDetails the replenishBatchDetails
   */
  @Update
  Integer update(ReplenishBatchDetails... replenishBatchDetails);
  
  /**
   * Delete.
   *
   * @param replenishBatchDetails the replenishBatchDetails
   */
  @Delete
  Integer delete(ReplenishBatchDetails... replenishBatchDetails);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM replenish_batch_details")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM replenish_batch_details")
  List<ReplenishBatchDetails> getAll();
  
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM replenish_batch_details WHERE batch_id=:batchId AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(ean) LIKE '%'||:searchName||'%' OR TRIM(article) LIKE '%'||:searchName||'%')")
  Long getBatchTotalCount(String batchId,String searchName);

  /**
   * Gets batch total count.
   *
   * @param category   the category
   * @param categories the categories
   * @param matkl      the brand
   * @param matkls     the brands
   * @param searchName the search name
   * @return the batch total count
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM replenish_batch_details WHERE batch_id=:batchId AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:matkl IS NULL OR LENGTH(:matkl)<=0 OR :matkl='" + AppConstants.ALL + "' OR TRIM(matkl)=:matkl OR TRIM(matkl) IN (:matkls)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(article) LIKE '%'||:searchName||'%' OR TRIM(ean) LIKE '%'||:searchName||'%')")
  Integer getBatchTotalCount(String batchId,String category, Set<String> categories, String matkl, Set<String> matkls, String searchName);


  @Transaction
  @Query("SELECT * FROM replenish_batch_details WHERE batch_id=:batchId AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(ean) LIKE '%'||:searchName||'%' OR TRIM(article) LIKE '%'||:searchName||'%') ORDER BY COALESCE(pick_qty,0) DESC, article ASC, ean ASC LIMIT :limit OFFSET :offSet")
  List<ReplenishBatchDetails> getBatchDetails(String batchId,String searchName,Integer offSet, Integer limit);

  /**
   * Gets batch details.
   *
   * @param category   the category
   * @param categories the categories
   * @param matkl      the brand
   * @param matkls     the brands
   * @param searchName the search name
   * @return the replenishment products
   */
  @Transaction
  //@Query("SELECT * FROM replenish_batch_details WHERE batch_id=:batchId AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:matkl IS NULL OR LENGTH(:matkl)<=0 OR :matkl='" + AppConstants.ALL + "' OR TRIM(matkl)=:matkl OR TRIM(matkl) IN (:matkls)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(article) LIKE '%'||:searchName||'%' OR TRIM(ean) LIKE '%'||:searchName||'%') ORDER BY CASE WHEN seq_number>0 THEN seq_number END ASC, (CASE WHEN COALESCE(total_qty,0)==COALESCE(pick_qty,0) THEN 1 WHEN COALESCE(pick_qty,0)==0 THEN 2 ELSE 3 END) DESC, article ASC, ean ASC LIMIT :limit OFFSET :offSet")
  //@Query("SELECT * FROM replenish_batch_details WHERE batch_id=:batchId AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:matkl IS NULL OR LENGTH(:matkl)<=0 OR :matkl='" + AppConstants.ALL + "' OR TRIM(matkl)=:matkl OR TRIM(matkl) IN (:matkls)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(article) LIKE '%'||:searchName||'%' OR TRIM(ean) LIKE '%'||:searchName||'%') ORDER BY seq_number ASC, article ASC, ean ASC LIMIT :limit OFFSET :offSet")
  @Query("SELECT * FROM replenish_batch_details WHERE batch_id=:batchId AND (:category IS NULL OR LENGTH(:category)<=0 OR :category='" + AppConstants.ALL + "' OR TRIM(category)=:category OR TRIM(category) IN (:categories)) AND (:matkl IS NULL OR LENGTH(:matkl)<=0 OR :matkl='" + AppConstants.ALL + "' OR TRIM(matkl)=:matkl OR TRIM(matkl) IN (:matkls)) AND (:searchName IS NULL OR LENGTH(:searchName)<=0 OR TRIM(article) LIKE '%'||:searchName||'%' OR TRIM(ean) LIKE '%'||:searchName||'%') ORDER BY (CASE WHEN COALESCE(total_qty,0)==COALESCE(pick_qty,0) THEN 1 WHEN COALESCE(pick_qty,0)==0 THEN 2 ELSE 3 END) DESC, seq_number ASC, article ASC, ean ASC LIMIT :limit OFFSET :offSet")
  List<ReplenishBatchDetails> getBatchDetails(String batchId,String category, Set<String> categories, String matkl, Set<String> matkls, String searchName,Integer offSet, Integer limit);



  /**
   * Gets category list.
   *
   * @return the category list
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(category) FROM  replenish_batch_details WHERE category IS NOT NULL AND LENGTH(TRIM(category))>0 AND TRIM(category) !='" + AppConstants.ALL + "'")
  List<String> getCategoryList();
  
  @Transaction
  @Query("SELECT DISTINCT TRIM(category) FROM  replenish_batch_details WHERE category IS NOT NULL AND LENGTH(TRIM(category))>0 AND TRIM(category) !='" + AppConstants.ALL + "' AND TRIM(matkl) IN (:matklList)")
  List<String> getCategoryList(Set<String> matklList);

  /**
   * Gets matkl list.
   *
   * @return the matkl list
   */
  @Transaction
  @Query("SELECT DISTINCT TRIM(COALESCE(matkl,'" + AppConstants.DEFAULT_NO_VALUE + "')) FROM replenish_batch_details WHERE matkl IS NOT NULL AND LENGTH(TRIM(matkl))>0 AND TRIM(matkl) !='" + AppConstants.ALL + "'")
  List<String> getMatklList();
  
  @Transaction
  @Query("SELECT DISTINCT TRIM(COALESCE(matkl,'" + AppConstants.DEFAULT_NO_VALUE + "')) FROM replenish_batch_details WHERE matkl IS NOT NULL AND LENGTH(TRIM(matkl))>0 AND TRIM(matkl) !='" + AppConstants.ALL + "' AND TRIM(category) IN (:categoryList)")
  List<String> getMatklList(Set<String> categoryList);
  
  @RawQuery
  List<String> getList(SupportSQLiteQuery query);
  
  default List<String> getList(String column, HashMap<String,Set<String>> filters){
    String condition = "";
    String queryColumn = toUnderScoreCase(column);
    if(isNonEmpty(filters)){
      for(String key:filters.keySet()){
        if(isNonEmpty(key) && !key.equalsIgnoreCase(column) && isNonEmpty(filters.get(key))){
          String queryKey = toUnderScoreCase(key);
          condition += (!condition.contains("WHERE") ? "WHERE " : "") + (isNonEmpty(condition) ? " AND " : "") + "TRIM("+queryKey+") IN('" + filters.get(key).toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll("’", "’’").replaceAll("'", "''").replaceAll("\\s*,\\s*", "','") + "') ";
        }
      }
    }
    String statement = "SELECT DISTINCT TRIM(COALESCE("+queryColumn+",'')) FROM replenish_batch_details " + condition + "ORDER BY " + queryColumn + " ASC";
    showLog("getList query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    //List<String> resultList = chkNull(getList(query),new ArrayList<String>(0));
    //if(!column.matches("(?i)(ean|qty)")) resultList.add(0, AppConstants.ALL);
    return getList(query);
  }
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM replenish_batch_details")
  Integer getTableSize();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM replenish_batch_details")
  boolean hasData();
  
}
