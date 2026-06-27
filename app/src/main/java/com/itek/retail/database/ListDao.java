package com.itek.retail.database;

import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.showLog;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.itek.retail.model.ListModel;

import java.util.List;

/**
 * The interface list dao.
 */
@androidx.room.Dao
public interface ListDao{
  
  /**
   * Insert.
   *
   * @param listModels the list models
   */
  @Insert
  void insert(ListModel... listModels);
  
  @Insert
  void insertAll(List<ListModel> listModels);
  
  /**
   * Update.
   *
   * @param listModels the list models
   */
  @Update
  Integer update(ListModel... listModels);
  
  /**
   * Delete.
   *
   * @param listModels the list models
   */
  @Delete
  Integer delete(ListModel... listModels);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM list")
  void deleteAll();
  
  @Transaction
  @Query("SELECT * FROM list WHERE list_id IS NOT NULL AND LENGTH(list_id)>0 AND (:searchName IS NULL OR LENGTH(TRIM(:searchName))<=0 OR list_id LIKE '%'||:searchName||'%') ORDER BY list_type,list_id ASC")
  List<ListModel> getTypeWiseSearchList(String searchName);
  
  /**
   * Search header list model.
   *
   * @param query the query
   * @return the serList
   */
  @RawQuery
  List<ListModel> searchHeader(SupportSQLiteQuery query);
  
  /**
   * Search header.
   *
   * @param searchName the search name
   * @param sortColumn the sort column
   * @return the serList
   */
  default List<ListModel> searchHeader(String searchName, String sortColumn){
    String statement = "SELECT * FROM list " + (isNonEmpty(searchName) ? " WHERE list_id LIKE '%" + searchName + "%' " : "") + " GROUP BY list_id ORDER BY " + (isNonEmpty(sortColumn) ? sortColumn + ", " : "") + " priority";
    showLog("searchOmniLists query", statement);
    SupportSQLiteQuery query = new SimpleSQLiteQuery(statement, new Object[]{/*Empty array*/});
    return searchHeader(query);
  }
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM list")
  boolean hasData();
}
