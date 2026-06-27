package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.MenuModel;

import java.util.List;

/**
 * The interface Menu dao.
 */
@androidx.room.Dao
public interface MenuDao{
  
  /**
   * Insert.
   *
   * @param menuModels the menu models
   */
  @Insert
  void insert(MenuModel... menuModels);
  
  @Insert
  void insertAll(List<MenuModel> menuModels);
  
  /**
   * Update.
   *
   * @param menuModels the menu models
   */
  @Update
  Integer update(MenuModel... menuModels);
  
  /**
   * Delete.
   *
   * @param menuModels the menu models
   */
  @Delete
  Integer delete(MenuModel... menuModels);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM menus")
  public void deleteAll();
  
  @Query("DELETE FROM menus WHERE menu_id=:menuId")
  public Integer delete(Integer menuId);
  
  /**
   * Gets all home menus.
   *
   * @return the all home menus
   */
  @Transaction
  @Query("SELECT * FROM menus WHERE is_home_menu>0 ORDER BY sequence")
  List<MenuModel> getAllHomeMenus();
  
  /**
   * Gets action menus.
   *
   * @return the action menus
   */
  @Transaction
  @Query("SELECT * FROM menus WHERE is_action_menu>0 ORDER BY sequence")
  List<MenuModel> getActionMenus();
  
  @Transaction
  @Query("SELECT * FROM menus WHERE is_nav_menu>0 ORDER BY sequence")
  List<MenuModel> getNavMenus();
  
  /**
   * Gets sub menus.
   *
   * @param parentId the parent id
   * @return the sub menus
   */
  @Transaction
  @Query("SELECT * FROM menus WHERE parent_id=:parentId ORDER BY sequence")
  List<MenuModel> getSubMenus(Integer parentId);
  
  /**
   * Gets sub menus codes.
   *
   * @param parentId the parent id
   * @return the sub menus codes
   */
  @Transaction
  @Query("SELECT menu_code FROM menus WHERE menu_id=:parentId OR parent_id=:parentId ORDER BY sequence")
  List<String> getSubMenusCodes(Integer parentId);
  
  /**
   * Gets parent.
   *
   * @param parentId the parent id
   * @return the parent
   */
  @Query("SELECT * FROM menus WHERE menu_id=:parentId LIMIT 1")
  MenuModel getParent(Integer parentId);
  
  /**
   * Gets menu by code.
   *
   * @param menuCode the menu code
   * @return the menu by code
   */
  @Query("SELECT * FROM menus WHERE menu_code=:menuCode AND is_enabled>0 LIMIT 1")
  MenuModel getMenuByCode(String menuCode);
  
  /**
   * Gets menu by code.
   *
   * @param menuCode the menu code
   * @return the menu by code
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM menus WHERE menu_code=:menuCode AND is_enabled>0 LIMIT 1")
  Boolean hasMenu(String menuCode);
  
  /**
   * Gets menus by codes.
   *
   * @param menuCodes the menu codes
   * @return the menus by codes
   */
  @Transaction
  @Query("SELECT * FROM menus WHERE menu_code IN(:menuCodes) AND is_enabled>0 ORDER BY sequence")
  List<MenuModel> getMenusByCodes(String[] menuCodes);
  
  /**
   * Gets menu by id.
   *
   * @param menuId the menu id
   * @return the menu by id
   */
  @Query("SELECT * FROM menus WHERE menu_id=:menuId AND is_enabled>0 LIMIT 1")
  MenuModel getMenuById(Integer menuId);
  
  /**
   * Gets all fav menus.
   *
   * @return the all fav menus
   */
  @Transaction
  @Query("SELECT * FROM menus WHERE  is_fav_menu>0 AND is_enabled>0 ORDER BY sequence")
  List<MenuModel> getAllFavMenus();
  
  /**
   * Gets current fav menus.
   *
   * @param listFavMenuCodes the list fav menu codes
   * @param orderBy          the order by
   * @return the current fav menus
   */
  @Transaction
  @Query("SELECT * FROM menus WHERE  TRIM(menu_code) IN (:listFavMenuCodes) AND is_fav_menu>0 AND is_enabled>0 ORDER BY :orderBy")
  List<MenuModel> getCurrentFavMenus(List<String> listFavMenuCodes, String orderBy);
  
  /**
   * Gets remaining fav menus.
   *
   * @param listFavMenuCodes the list fav menu codes
   * @return the remaining fav menus
   */
  @Transaction
  @Query("SELECT * FROM menus WHERE is_fav_menu>0 AND TRIM(menu_code) NOT IN(:listFavMenuCodes) AND is_enabled>0 ORDER BY sequence")
  List<MenuModel> getRemainingFavMenus(List<String> listFavMenuCodes);
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM menus")
  List<MenuModel> getAll();
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM menus")
  Integer getTableSize();
  
  /**
   * Gets total fav menu size.
   *
   * @return the total fav menu size
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM menus WHERE is_fav_menu>0 AND is_enabled>0")
  Integer getTotalFavMenuSize();
  
  /**
   * Gets total home menu size.
   *
   * @return the total home menu size
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM menus WHERE is_home_menu>0 AND is_enabled>0")
  Integer getTotalHomeMenuSize();
  
  /**
   * Gets total nav menu size.
   *
   * @return the total nav menu size
   */
  @Query("SELECT COALESCE(COUNT(*),0) FROM menus WHERE is_home_menu>0 AND is_enabled>0")
  Integer getTotalNavMenuSize();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM menus")
  boolean hasData();
}
