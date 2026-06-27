package com.itek.retail.database;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.itek.retail.model.Notification;

import java.util.List;

/**
 * The interface Notification dao.
 */
@androidx.room.Dao
public interface NotificationDao{
  
  /**
   * Insert.
   *
   * @param notifications the notifications
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insert(Notification... notifications);
  
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertAll(List<Notification> listNotifications);
  
  /**
   * Update.
   *
   * @param notifications the notifications
   */
  @Update
  Integer update(Notification... notifications);
  
  /**
   * Delete.
   *
   * @param notifications the notifications
   */
  @Delete
  Integer delete(Notification... notifications);
  
  /**
   * Delete all.
   */
  @Query("DELETE FROM notifications")
  void deleteAll();
  
  /**
   * Gets all.
   *
   * @return the all
   */
  @Query("SELECT * FROM notifications")
  List<Notification> getAll();
  
  /**
   * Gets notify model by name.
   *
   * @param userId           the user id
   * @param notificationName the notification name
   * @return the notify model by name
   */
  @Query("SELECT * FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND TRIM(title)=:notificationName")
  Notification getNotifyModelByName(String userId, String notificationName);
  
  /**
   * Gets all notifications.
   *
   * @param userId the user id
   * @return the all notifications
   */
  @Transaction
  @Query("SELECT TRIM(title) FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId)")
  List<String> getAllNotifications(String userId);
  
  /**
   * Gets notifications by type.
   *
   * @param userId the user id
   * @param type   the type
   * @return the notifications by type
   */
  @Transaction
  //@Query("SELECT * FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND TRIM(type)=:type ORDER BY date desc")
  @Query("SELECT * FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND TRIM(typeId)=:type ORDER BY date desc")
  LiveData<List<Notification>> getNotificationsByType(String userId, String type);
  
  /**
   * Update read notifications.
   *
   * @param userId the user id
   * @param type   the type
   */
  @Transaction
  @Query("UPDATE notifications SET is_read=1 WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND TRIM(typeId)=:type")
  //@Query("UPDATE notifications SET is_read=1 WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND TRIM(type)=:type")
  Integer updateReadNotifications(String userId, String type);
  
  //@Query("SELECT * FROM notifications GROUP BY type ORDER BY type")
  
  /**
   * Gets notification types.
   *
   * @param userId the user id
   * @return the notification types
   */
  @Transaction
  @Query("SELECT t1.*,t2.qty AS 'qty' FROM notifications AS t1 JOIN (SELECT type, COALESCE(MAX(notification_no),0)  AS notification_no, COALESCE(COUNT(*),0) AS qty FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) GROUP BY type) AS t2 ON t1.notification_no = t2.notification_no AND t1.type = t2.type")
  LiveData<List<Notification>> getNotificationTypes(String userId);
    
    /*@Query("SELECT type,title,message,img_url,itemImgId, is_read, 'kk' AS 'brand' FROM notifications")
    List<Notification> getNotificationTypes();*/
  
  /**
   * Gets table size.
   *
   * @return the table size
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM notifications")
  Integer getTableSize();
  
  /**
   * Is read pending boolean.
   *
   * @param userId the user id
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND is_read<=0")
  boolean isReadPending(String userId);
  
  /**
   * Is read pending boolean.
   *
   * @param userId the user id
   * @param type   the type
   * @return the boolean
   */
  @Transaction
  //@Query("SELECT COALESCE(COUNT(*),0)>0 FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND (:type IS NULL OR LENGTH(:type)<=0 OR TRIM(type) LIKE '%'||:type||'%') AND is_read<=0")
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND (:type IS NULL OR LENGTH(:type)<=0 OR TRIM(typeId) = :type) AND is_read<=0")
  boolean isReadPending(String userId, String type);
    
    /*@Query("SELECT COALESCE(COUNT(*),0)>0 FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND (:type IS NULL OR LENGTH(:type)<=0 OR TRIM(type) REGEXP '['||:type||']+') AND is_read<=0")
    boolean isReadPending(String userId,String type);*/
  
  /**
   * Is read pending boolean.
   *
   * @param userId the user id
   * @param types  the types
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND TRIM(type) IN (:types) AND is_read<=0")
  boolean isReadPending(String userId, String[] types);
  
  /**
   * Is read pending boolean.
   *
   * @param userId the user id
   * @param types  the types
   * @return the boolean
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND TRIM(type) LIKE '%'||:types||'%' AND is_read<=0")
  boolean isReadPending(String userId, List<String> types);
  
  /**
   * Gets unread notification count.
   *
   * @param userId the user id
   * @return the unread notification count
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND is_read<=0")
  LiveData<Integer> getUnreadNotificationCount(String userId);
  
  /**
   * Gets unread notification count.
   *
   * @param userId the user id
   * @param type   the type
   * @return the unread notification count
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND TRIM(typeId)=:type AND is_read<=0")
  //@Query("SELECT COALESCE(COUNT(*),0) FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId) AND TRIM(type)=:type AND is_read<=0")
  Integer getUnreadNotificationCount(String userId, String type);
  
  /**
   * Gets total notification count.
   *
   * @param userId the user id
   * @return the total notification count
   */
  @Transaction
  @Query("SELECT COALESCE(COUNT(*),0) FROM notifications WHERE (:userId IS NULL OR LENGTH(:userId)<=0 OR TRIM(user_id)=:userId)")
  LiveData<Integer> getTotalNotificationCount(String userId);
  
  /**
   * Delete expired integer.
   *
   * @param ccDate the cc date
   * @return the integer
   */
  @Query("DELETE FROM notifications WHERE TRIM(valid_till)<=:ccDate")
  Integer deleteExpired(String ccDate);
  
  /**
   * Has data boolean.
   *
   * @return the boolean
   */
  @Query("SELECT COALESCE(COUNT(*),0)>0 FROM notifications")
  boolean hasData();
}
