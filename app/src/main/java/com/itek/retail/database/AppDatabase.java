package com.itek.retail.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.itek.retail.model.AuditTrailsLog;
import com.itek.retail.model.BatchEpc;
import com.itek.retail.model.Brand;
import com.itek.retail.model.BrandEans;
import com.itek.retail.model.BrandWiseZoneInventory;
import com.itek.retail.model.Category;
import com.itek.retail.model.FIFOModel;
import com.itek.retail.model.HUDetails;
import com.itek.retail.model.HUStatus;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.ListModel;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.Notification;
import com.itek.retail.model.OutwardBatch;
import com.itek.retail.model.OutwardToteEans;
import com.itek.retail.model.ProductInvFilterModel;
import com.itek.retail.model.ProductModel;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.ReplenishBatchDetails;
import com.itek.retail.model.SearchLog;
import com.itek.retail.model.SerialDetails;
import com.itek.retail.model.SiteCode;
import com.itek.retail.model.SiteType;
import com.itek.retail.model.TagID;
import com.itek.retail.model.TripInventory;
import com.itek.retail.model.TripStatus;
import com.itek.retail.model.UploadInventory;
import com.itek.retail.model.Zone;

/**
 * The App database.
 */
@Database(entities = {Zone.class, Category.class, Brand.class, BrandEans.class, BrandWiseZoneInventory.class, ProductModel.class, MenuModel.class, RFIDSession.class, Inventory.class, UploadInventory.class, Notification.class, TripInventory.class, TripStatus.class, SearchLog.class, ListModel.class, AuditTrailsLog.class, FIFOModel.class, OutwardToteEans.class, OutwardBatch.class, HUStatus.class, HUDetails.class, SiteType.class, SiteCode.class, BatchEpc.class, ReplenishBatchDetails.class, SerialDetails.class, ProductInvFilterModel.class, TagID.class},
  version = 30, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase{
  
  public static final String DB_NAME = "ITEK_APP_DB";
  static final Migration MIGRATION_3_4 = new Migration(3, 4){
    @Override
    public void migrate(SupportSQLiteDatabase database){
      try{
        database.execSQL("ALTER TABLE inventory ADD COLUMN tag_status TEXT DEFAULT 'Unencoded' ");
        //TODO Write Code to get ALL inventory and update every object 1 by 1 using the 'setInventoryData' method;
      }
      catch(Exception e){ }
    }
  };
  static final Migration MIGRATION_4_5 = new Migration(4, 5){
    @Override
    public void migrate(SupportSQLiteDatabase database){
      try{
        database.execSQL("ALTER TABLE inventory ADD COLUMN fifo_date TEXT DEFAULT null");
        database.execSQL("ALTER TABLE inventory_dashboard_count ADD COLUMN category_name TEXT DEFAULT null");
        database.execSQL("ALTER TABLE upload_inventory ADD COLUMN fifo_date TEXT DEFAULT null");
        database.execSQL("ALTER TABLE product ADD COLUMN fifo_date TEXT DEFAULT null");
      }
      catch(Exception e){ }
    }
  };
  static final Migration MIGRATION_5_6 = new Migration(5, 6){
    @Override
    public void migrate(SupportSQLiteDatabase database){
      try{
        database.execSQL("ALTER TABLE inventory_dashboard_count ADD COLUMN category_name TEXT DEFAULT null");
        database.execSQL("ALTER TABLE inventory ADD COLUMN fifo_date TEXT DEFAULT null");
        database.execSQL("ALTER TABLE upload_inventory ADD COLUMN fifo_date TEXT DEFAULT null");
        database.execSQL("ALTER TABLE product ADD COLUMN fifo_date TEXT DEFAULT null");
      }
      catch(Exception e){ }
    }
  };
  private static AppDatabase INSTANCE;
  
  /**
   * Get db instance app database.
   *
   * @param context the context
   * @return the app database
   */
  public static AppDatabase getDbInstance(Context context){
    if(INSTANCE == null)//build DB
    {
      //INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME).allowMainThreadQueries().addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build();
      INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME).allowMainThreadQueries().fallbackToDestructiveMigration().build();
    }
    return INSTANCE;
    
  }
  
  /**
   * Get rifd session dao rfid session dao.
   *
   * @param context the context
   * @return the rfid session dao
   */
  public static RFIDSessionDao getRIFDSessionDao(Context context){
    if(INSTANCE != null) return INSTANCE.RFIDSessionDao();
    else return getDbInstance(context).RFIDSessionDao();
  }
  
  //interface
  
  /**
   * Get product dao.
   *
   * @param context the context
   * @return the product dao
   */
  public static ProductDao getProductDao(Context context){
    if(INSTANCE != null) return INSTANCE.ProductDao();
    else return getDbInstance(context).ProductDao();
  }
  
  /**
   * Get product dao.
   *
   * @param context the context
   * @return the product dao
   */
  public static ProductInvFilterDao getProductInvFilterDao(Context context){
    if(INSTANCE != null) return INSTANCE.ProductInvFilterDao();
    else return getDbInstance(context).ProductInvFilterDao();
  }
  
  /**
   * Get product dao.
   *
   * @param context the context
   * @return the product dao
   */
  public static FIFODao getFIFODao(Context context){
    if(INSTANCE != null) return INSTANCE.FIFODao();
    else return getDbInstance(context).FIFODao();
  }
  
  /**
   * Get list dao.
   *
   * @param context the context
   * @return the list dao
   */
  public static ListDao getListDao(Context context){
    if(INSTANCE != null) return INSTANCE.ListDao();
    else return getDbInstance(context).ListDao();
  }
  
  /**
   * Get notification dao notification dao.
   *
   * @param context the context
   * @return the notification dao
   */
  public static NotificationDao getNotificationDao(Context context){
    if(INSTANCE != null) return INSTANCE.NotifyDao();
    else return getDbInstance(context).NotifyDao();
  }
  
  /**
   * Get zone dao zone dao.
   *
   * @param context the context
   * @return the zone dao
   */
  public static ZoneDao getZoneDao(Context context){
    if(INSTANCE != null) return INSTANCE.ZoneDao();
    else return getDbInstance(context).ZoneDao();
  }
  
  /**
   * Get category dao category dao.
   *
   * @param context the context
   * @return the category dao
   */
  public static CategoryDao getCategoryDao(Context context){
    if(INSTANCE != null) return INSTANCE.CategoryDao();
    else return getDbInstance(context).CategoryDao();
  }
  
  /**
   * Get tag id dao.
   *
   * @param context the context
   * @return the tag id dao
   */
  public static TagIDDao getTagIDDao(Context context){
    if(INSTANCE != null) return INSTANCE.TagIDDao();
    else return getDbInstance(context).TagIDDao();
  }
  
  /**
   * Get brand dao brand dao.
   *
   * @param context the context
   * @return the brand dao
   */
  public static BrandDao getBrandDao(Context context){
    if(INSTANCE != null) return INSTANCE.BrandDao();
    else return getDbInstance(context).BrandDao();
  }
  
  /**
   * Get brand dao brand dao.
   *
   * @param context the context
   * @return the brand dao
   */
  public static BrandEanDao getBrandEansDao(Context context){
    if(INSTANCE != null) return INSTANCE.BrandEansDao();
    else return getDbInstance(context).BrandEansDao();
  }
  
  public static OutwardToteEansDao getOutwardToteEansDao(Context context){
    if(INSTANCE != null) return INSTANCE.OutwardToteEansDao();
    else return getDbInstance(context).OutwardToteEansDao();
  }
  public static OutwardBatchDao getOutwardBatchDao(Context context){
    if(INSTANCE != null) return INSTANCE.OutwardBatchDao();
    else return getDbInstance(context).OutwardBatchDao();
  }
  
  /**
   * Get brand wise zone inventory dao brand wise zone inventory dao.
   *
   * @param context the context
   * @return the brand wise zone inventory dao
   */
  public static BrandWiseZoneInventoryDao getBrandWiseZoneInventoryDao(Context context){
    if(INSTANCE != null) return INSTANCE.BrandWiseZoneInventoryDao();
    else return getDbInstance(context).BrandWiseZoneInventoryDao();
  }
  
  /**
   * Get menu dao menu dao.
   *
   * @param context the context
   * @return the menu dao
   */
  public static MenuDao getMenuDao(Context context){
    if(INSTANCE != null) return INSTANCE.MenuDao();
    else return getDbInstance(context).MenuDao();
  }
  
  /**
   * Get inventory dao inventory dao.
   *
   * @param context the context
   * @return the inventory dao
   */
  public static InventoryDao getInventoryDao(Context context){
    if(INSTANCE != null) return INSTANCE.InventoryDao();
    else return getDbInstance(context).InventoryDao();
  }
  
  /**
   * Get upload inventory dao upload inventory dao.
   *
   * @param context the context
   * @return the upload inventory dao
   */
  public static UploadInventoryDao getUploadInventoryDao(Context context){
    if(INSTANCE != null) return INSTANCE.UploadInventoryDao();
    else return getDbInstance(context).UploadInventoryDao();
  }
  
  /**
   * Gets trip inventory dao.
   *
   * @param context the context
   * @return the trip inventory dao
   */
  public static TripInventoryDao getTripInventoryDao(Context context){
    if(INSTANCE != null) return INSTANCE.TripInventoryDao();
    else return getDbInstance(context).TripInventoryDao();
  }

  public static SerialDetailsDao getSerialDetailsDao(Context context){
    if(INSTANCE != null) return INSTANCE.SerialDetailsDao();
    else return getDbInstance(context).SerialDetailsDao();
  }
  
  /**
   * Gets trip status dao.
   *
   * @param context the context
   * @return the trip status dao
   */
  public static TripStatusDao getTripStatusDao(Context context){
    if(INSTANCE != null) return INSTANCE.TripStatusDao();
    else return getDbInstance(context).TripStatusDao();
  }
  
  /**
   * Gets hu status dao.
   *
   * @param context the context
   * @return the hu status dao
   */
  public static HUStatusDao getHUStatusDao(Context context){
    if(INSTANCE != null) return INSTANCE.HUStatusDao();
    else return getDbInstance(context).HUStatusDao();
  }
  
  /**
   * Gets hu details dao.
   *
   * @param context the context
   * @return the hu details dao
   */
  public static HUDetailsDao getHUDetailsDao(Context context){
    if(INSTANCE != null) return INSTANCE.HUDetailsDao();
    else return getDbInstance(context).HUDetailsDao();
  }
  
  /**
   * Gets site type dao.
   *
   * @param context the context
   * @return the site type dao
   */
  public static SiteTypeDao getSiteTypeDao(Context context){
    if(INSTANCE != null) return INSTANCE.SiteTypeDao();
    else return getDbInstance(context).SiteTypeDao();
  }
  
  /**
   * Gets site code dao.
   *
   * @param context the context
   * @return the site type dao
   */
  public static SiteCodeDao getSiteCodeDao(Context context){
    if(INSTANCE != null) return INSTANCE.SiteCodeDao();
    else return getDbInstance(context).SiteCodeDao();
  }
  
  /**
   * Gets batch epc dao.
   *
   * @param context the context
   * @return the batch epc dao
   */
  public static BatchEpcDao getBatchEpcDao(Context context){
    if(INSTANCE != null) return INSTANCE.BatchEpcDao();
    else return getDbInstance(context).BatchEpcDao();
  }
  
  /**
   * Gets replenish batch details dao.
   *
   * @param context the context
   * @return the replenish batch details dao
   */
  public static ReplenishBatchDetailsDao getReplenishBatchDetailsDao(Context context){
    if(INSTANCE != null) return INSTANCE.ReplenishBatchDetailsDao();
    else return getDbInstance(context).ReplenishBatchDetailsDao();
  }
  
  /**
   * Gets search log dao.
   *
   * @param context the context
   * @return the search log dao
   */
  public static SearchLogDao getSearchLogDao(Context context){
    if(INSTANCE != null) return INSTANCE.SearchLogDao();
    else return getDbInstance(context).SearchLogDao();
  }
  
  /**
   * Gets auditTrails log dao.
   *
   * @param context the context
   * @return the auditTrails log dao
   */
  public static AuditTrailsDao getAuditTrailsDao(Context context){
    if(INSTANCE != null) return INSTANCE.AuditTrailsDao();
    else return getDbInstance(context).AuditTrailsDao();
  }
  
  @NonNull
  //@org.jetbrains.annotations.NotNull
  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config){
    return null;
  }
  
  @NonNull
  //@org.jetbrains.annotations.NotNull
  @Override
  protected InvalidationTracker createInvalidationTracker(){
    return null;
  }
  
  @Override
  public void clearAllTables(){
    //Don't handle
    //default overridden method
  }
  
  /**
   * Notify dao notification dao.
   *
   * @return the notification dao
   */
  public abstract NotificationDao NotifyDao();//interface
  
  /**
   * Zone dao zone dao.
   *
   * @return the zone dao
   */
  public abstract ZoneDao ZoneDao();//interface
  
  /**
   * Category dao category dao.
   *
   * @return the category dao
   */
  public abstract CategoryDao CategoryDao();//interface
  
  /**
   * tag id dao.
   *
   * @return the tag id dao
   */
  public abstract TagIDDao TagIDDao();//interface
  
  /**
   * Brand dao brand dao.
   *
   * @return the brand dao
   */
  public abstract BrandDao BrandDao();//interface
  
  /**
   * Brand dao brand dao.
   *
   * @return the brand dao
   */
  public abstract BrandEanDao BrandEansDao();//interface
  
  /**
   * Outward Tote Eans dao.
   *
   * @return the Outward Tote Eans dao
   */
  public abstract OutwardToteEansDao OutwardToteEansDao();//interface
  
  /**
   * Outward Tote Eans dao.
   *
   * @return the Outward Tote Eans dao
   */
  public abstract OutwardBatchDao OutwardBatchDao();//interface
  
  /**
   * Brand wise zone inventory dao brand wise zone inventory dao.
   *
   * @return the brand wise zone inventory dao
   */
  public abstract BrandWiseZoneInventoryDao BrandWiseZoneInventoryDao();//interface
  
  /**
   * Menu dao menu dao.
   *
   * @return the menu dao
   */
  public abstract MenuDao MenuDao();//interface
  
  /**
   * Inventory dao inventory dao.
   *
   * @return the inventory dao
   */
  public abstract InventoryDao InventoryDao();//interface
  
  /**
   * Upload inventory dao upload inventory dao.
   *
   * @return the upload inventory dao
   */
  public abstract UploadInventoryDao UploadInventoryDao();//interface
  
  /**
   * Rfid session dao rfid session dao.
   *
   * @return the rfid session dao
   */
  public abstract RFIDSessionDao RFIDSessionDao();//interface
  
  /**
   * product dao.
   *
   * @return the product dao
   */
  public abstract ProductDao ProductDao();//interface
  
  /**
   * product dao.
   *
   * @return the product dao
   */
  public abstract ProductInvFilterDao ProductInvFilterDao();//interface
  
  /**
   * fifo dao.
   *
   * @return the fifo dao
   */
  public abstract FIFODao FIFODao();//interface
  
  /**
   * list dao.
   *
   * @return the list dao
   */
  public abstract ListDao ListDao();//interface
  
  /**
   * Trip inventory dao trip inventory dao.
   *
   * @return the trip inventory dao
   */
  public abstract TripInventoryDao TripInventoryDao();//interface

  /**
   * Serial Details dao serial details dao.
   *
   * @return the serial details dao
   */

  public abstract SerialDetailsDao SerialDetailsDao();//interface
  
  /**
   * Trip status dao trip status dao.
   *
   * @return the trip status dao
   */
  public abstract TripStatusDao TripStatusDao();//interface
  
  
  /**
   * HU status dao hu status dao.
   *
   * @return the hu status dao
   */
  public abstract HUStatusDao HUStatusDao();//interface
  
  /**
   * HU details dao hu details dao.
   *
   * @return the hu details dao
   */
  public abstract HUDetailsDao HUDetailsDao();//interface
  
  /**
   * Site type dao site type dao.
   *
   * @return the site type dao
   */
  public abstract SiteTypeDao SiteTypeDao();//interface
  
  
  /**
   * Site code dao site code dao.
   *
   * @return the site code dao
   */
  public abstract SiteCodeDao SiteCodeDao();//interface
  
  
  /**
   * Batch epc dao batch epc dao.
   *
   * @return the batch epc dao
   */
  public abstract BatchEpcDao BatchEpcDao();//interface
  
  /**
   * Replenish batch details dao.
   *
   * @return the replenish batch details dao
   */
  public abstract ReplenishBatchDetailsDao ReplenishBatchDetailsDao();//interface
  
  /**
   * Search log dao search log dao.
   *
   * @return the search log dao
   */
  public abstract SearchLogDao SearchLogDao();//interface
  
  /**
   * log dao audit trails log dao.audit trails
   *
   * @return the audit trails log dao
   */
  public abstract AuditTrailsDao AuditTrailsDao();//interface
  
  public void closeDatabase(){
    if(INSTANCE != null && INSTANCE.isOpen()){
      INSTANCE.close();
    }
  }
  
  public void destroyInstance(){
    if(INSTANCE != null && INSTANCE.isOpen()){ INSTANCE.close(); }
    INSTANCE = null;
  }
}
