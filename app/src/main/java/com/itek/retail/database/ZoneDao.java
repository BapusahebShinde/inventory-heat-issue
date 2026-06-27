package com.itek.retail.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.Zone;

import java.util.List;
import java.util.Set;

/**
 * The interface Zone dao.
 */
@androidx.room.Dao
public interface ZoneDao{
  
  /**
   * Insert.
   *
   * @param zones the zones
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Zone... zones);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<Zone> listZones);
  
  /**
   * Update.
   *
   * @param zones the zones
   */
  @Update
  Integer update(Zone... zones);
  
  /**
   * Delete.
   *
   * @param zones the zones
   */
  @Delete
  Integer delete(Zone... zones);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM zones")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Transaction
  @Query("SELECT * FROM zones")
  List<Zone> getAll();
  
  /**
   * Gets zone by name.
   *
   * @param zoneName the zone name
   * @return the zone by name
   */
  @Transaction
  @Query("SELECT * FROM zones WHERE TRIM(zone_name)=:zoneName")
  List<Zone> getZoneByName(String zoneName);
  
  /**
   * Gets zone by name.
   *
   * @param zoneName the zone name
   * @return the zone by name
   */
  @Transaction
  @Query("SELECT * FROM zones WHERE TRIM(zone_name)=:zoneName LIMIT 1")
  Zone getZoneObjByName(String zoneName);
  
  /**
   * Gets zone id by name.
   *
   * @param zoneName the zone name
   * @return the zone by name
   */
  @Transaction
  @Query("SELECT COALESCE(zone_id,'0') FROM zones WHERE TRIM(zone_name)=:zoneName")
  String getZoneIdByName(String zoneName);
  
  /**
   * Gets zone name by id.
   *
   * @param zoneId the zone id
   * @return the zoneName by id
   */
  @Transaction
  @Query("SELECT COALESCE(zone_name,'') FROM zones WHERE TRIM(zone_id)=:zoneId")
  String getZoneNameById(String zoneId);
  
  /**
   * Gets zone by name.
   *
   * @param zones the zones
   *
   * @return the zone by name
   */
  @Transaction
  @Query("SELECT * FROM zones WHERE TRIM(zone_name) IN (:zones)")
  List<Zone> getZoneByName(Set<String> zones);
  
  /**
   * Gets all zones.
   *
   * @return the all zones
   */
  @Transaction
  @Query("SELECT TRIM(zone_name) FROM zones")
  List<String> getAllZones();


  /**
   * Gets all non display zones.
   *
   * @return the all non display zones
   */
  @Transaction
  @Query("SELECT TRIM(zone_name) FROM zones WHERE is_display_mapping IS NULL OR is_display_mapping<=0")
  List<String> getAllNonDisplayZones1();

  /**
   * Gets all non display zones.
   *
   * @return the all non display zones
   */
  @Transaction
  @Query("SELECT * FROM zones WHERE is_display_mapping IS NULL OR is_display_mapping<=0")
  List<Zone> getAllNonDisplayZones();
  
  @Transaction
  @Query("SELECT TRIM(zone_name) FROM zones WHERE is_display_mapping IS NULL OR is_display_mapping<=0")
  List<String> getAllNonDisplayZoneNames();
  
  /**
   * Gets foh zone names.
   *
   * @return the foh zone names
   */
  @Transaction
  @Query("SELECT TRIM(zone_name) FROM zones WHERE TRIM(zone_type)='FOH'")
  List<String> getFOHZoneNames();
  
  /**
   * Gets boh zone names.
   *
   * @return the boh zone names
   */
  @Transaction
  @Query("SELECT TRIM(zone_name) FROM zones WHERE TRIM(zone_type)='BOH'")
  List<String> getBOHZoneNames();
  
  /**
   * Gets foh zones.
   *
   * @return the foh zones
   */
  @Transaction
  @Query("SELECT * FROM zones WHERE TRIM(zone_type)='FOH'")
  List<Zone> getFOHZones();
  
  /**
   * Gets boh zones.
   *
   * @return the boh zones
   */
  @Transaction
  @Query("SELECT * FROM zones WHERE TRIM(zone_type)='BOH'")
  List<Zone> getBOHZones();
  
  /**
   * Gets all zones except.
   *
   * @param zone the zone
   * @return the all zones except
   */
  @Transaction
  @Query("SELECT TRIM(zone_name) FROM zones WHERE TRIM(zone_name) NOT IN (:zone)")
  List<String> getAllZonesExcept(String zone);
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM zones")
  Integer getTableSize();
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM zones")
  boolean hasData();
  
}
