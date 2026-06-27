package com.itek.retail.model;

import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isStaticDebug;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import com.itek.retail.R;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.ui.actionmenu.ActionMenuCompareFragment;
import com.itek.retail.ui.actionmenu.ActionMenuMsgFragment;
import com.itek.retail.ui.actionmenu.ActionMenuNotifyFragment;
import com.itek.retail.ui.actionmenu.ActionMenuSearchFragment;
import com.itek.retail.ui.decoding.DecodingStartFragment;
import com.itek.retail.ui.encoding.EncodingAchieveFragment;
import com.itek.retail.ui.encoding.EncodingConfigFragment;
import com.itek.retail.ui.encoding.EncodingMainFragment;
import com.itek.retail.ui.encoding.EncodingScanScanWriteFragment;
import com.itek.retail.ui.encoding.EncodingStartFragment;
import com.itek.retail.ui.encoding.EncodingVerifyFragment;
import com.itek.retail.ui.inventory.InventoryAddFragment;
import com.itek.retail.ui.inventory.InventoryBrandFragment;
import com.itek.retail.ui.inventory.InventoryFilterFragment;
import com.itek.retail.ui.inventory.InventoryMainFragment;
import com.itek.retail.ui.inventory.InventoryStartFragment;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionMainFragment;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionStartFragment;
import com.itek.retail.ui.inward.InwardMainFragment;
import com.itek.retail.ui.inward.grn.InwardGrnStartFragment;
import com.itek.retail.ui.inward.grn.InwardGrnTripsDataFragment;
import com.itek.retail.ui.inward.huverification.InwardHuVerificationFragment;
import com.itek.retail.ui.inward.tote.InwardToteMainFragment;
import com.itek.retail.ui.inward1.TripListFragment;
import com.itek.retail.ui.movement.MovementMainFragment;
import com.itek.retail.ui.movement.MovementStartFragment;
import com.itek.retail.ui.movement.replenishment.ReplenishmentListFragment;
import com.itek.retail.ui.movement.replenishment.ReplenishmentStartFragment;
import com.itek.retail.ui.navmenu.AppInfoFragment;
import com.itek.retail.ui.outward.OutwardMainFragment;
import com.itek.retail.ui.outward.OutwardPickDataFragment;
import com.itek.retail.ui.outward.OutwardPickStartFragment;
import com.itek.retail.ui.outward.huverification.OutwardHuDataFragment;
import com.itek.retail.ui.outward.offrange.OffRangeMainFragment;
import com.itek.retail.ui.outward.tote.OutwardToteDCFragment;
import com.itek.retail.ui.outward.tote.OutwardToteMainFragment;
import com.itek.retail.ui.replenishondemand.ReplenishmentArticleListFragment;
import com.itek.retail.ui.replenishondemand.ReplenishmentBatchListFragment;
import com.itek.retail.ui.resetpassword.ResetPasswordFragment;
import com.itek.retail.ui.scancount.ScanCountFragment;
import com.itek.retail.ui.search.SearchMainFragment;
import com.itek.retail.ui.search.ageing.AgeingSearchFragment;
import com.itek.retail.ui.search.alien.SearchAlienFragment;
import com.itek.retail.ui.search.assortment.SearchAssortMainFragment;
import com.itek.retail.ui.search.assortment.SearchAssortStartFragment;
import com.itek.retail.ui.search.fifo.SearchFIFOFragment;
import com.itek.retail.ui.search.filesearch.SearchFileBasedFragment;
import com.itek.retail.ui.search.listnewsearch.SearchListExcelFragment;
import com.itek.retail.ui.search.listsearch.SearchListFragment;
import com.itek.retail.ui.search.listsearch.SearchListStartFragment;
import com.itek.retail.ui.search.listsearch.SearchListsFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelListFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelOrderStatsFragment;
import com.itek.retail.ui.search.productsearch.ProductSearchFragment;
import com.itek.retail.ui.search.unencoded.SearchUnencodedFragment;
import com.itek.retail.ui.serial.SaveSerialFragment;
import com.itek.retail.ui.than.ThanCuttingFragment;
import com.itek.retail.ui.than.ThanEncodingFragment;
import com.itek.retail.ui.than.ThanMainFragment;

import java.io.Serializable;

/**
 * The Menu model.
 */
@Entity(tableName = "menus", indices = {@Index(value = {"menu_id"}, unique = true)})
public class MenuModel implements Serializable, Parcelable{
  
  public static final Creator<MenuModel> CREATOR = new Creator<MenuModel>(){
    @Override
    public MenuModel createFromParcel(Parcel in){
      return new MenuModel(in);
    }
    
    @Override
    public MenuModel[] newArray(int size){
      return new MenuModel[size];
    }
  };
  @PrimaryKey(autoGenerate = true)//auto increment ino
  @ColumnInfo(name = "menu_no")
  public Integer menuNo;
  @ColumnInfo(name = "menu_id", defaultValue = "0")
  @SerializedName("menuId")
  @NonNull
  public Integer menuId = 0;
  @ColumnInfo(name = "menu_code", defaultValue = "")
  @SerializedName(value = "menuCode")
  @NonNull
  public String menuCode = "";
  @ColumnInfo(name = "parent_id", defaultValue = "0")
  @SerializedName("parentId")
  public Integer parentId = 0;
  @SerializedName("iconId")
  public int iconId;
  @ColumnInfo(name = "image_url", defaultValue = "")
  @SerializedName("imageUrl")
  public String imageUrl;
  @ColumnInfo(name = "name", defaultValue = "")
  @SerializedName("menuName")
  @NonNull
  public String menuName;
  @ColumnInfo(name = "is_enabled", defaultValue = "false")
  @SerializedName("isEnabled")
  public Boolean isEnabled;
  @ColumnInfo(name = "err_msg")
  @SerializedName("errEnabledMsg")
  public String errEnabledMsg;
  @ColumnInfo(name = "is_home_menu", defaultValue = "false")
  @SerializedName("isEnabledOnHomeScreen")
  public Boolean isHomeMenu;
  @ColumnInfo(name = "is_fav_menu", defaultValue = "false")
  @SerializedName("isEnabledOnFavScreen")
  public Boolean isFavMenu;
  @ColumnInfo(name = "is_action_menu", defaultValue = "false")
  @SerializedName("isEnabledOnActionScreen")
  public Boolean isActionMenu;
  @ColumnInfo(name = "is_nav_menu", defaultValue = "false")
  @SerializedName("isEnabledOnNavScreen")
  public Boolean isNavMenu;
  @ColumnInfo(name = "sequence", defaultValue = "0")
  @SerializedName("sequence")
  public Integer sequence;
  @Ignore
  public Class redirectFragment;
  @ColumnInfo(name = "fav_image_url", defaultValue = "")
  @SerializedName("favImageUrl")
  public String favImageUrl;
  @ColumnInfo(name = "fav_menu_code", defaultValue = "")
  @SerializedName("favMenuCode")
  public String favMenuCode;
  @SerializedName("favIconId")
  public int favIconId;
  @ColumnInfo(name = "fav_menu_name", defaultValue = "")
  @SerializedName("favMenuName")
  public String favMenuName;
  @ColumnInfo(name = "screen_image_url", defaultValue = "")
  @SerializedName("screenImageUrl")
  public String screenImageUrl;
  @ColumnInfo(name = "screen_menu_code", defaultValue = "")
  @SerializedName("screenMenuCode")
  public String screenMenuCode;
  @SerializedName("screenIconId")
  public int screenIconId;
  @ColumnInfo(name = "screen_menu_name", defaultValue = "")
  @SerializedName("screenMenuName")
  public String screenMenuName;
  @ColumnInfo(name = "is_subscribe_for_notification", defaultValue = "false")
  @SerializedName("isSubscribeForNotification")
  public boolean isSubscribeForNotification;
  
  /**
   * Instantiates a new Menu model.
   */
  public MenuModel(){/*Empty Constructor*/}
  
  /**
   * Instantiates a new Menu model.
   *
   * @param menuId   the menu id
   * @param menuCode the menu code
   * @param menuName the menu name
   */
  @Ignore
  public MenuModel(Integer menuId, String menuCode, String menuName){
    this.menuId = menuId;
    this.menuCode = chkNull(menuCode, "");
    this.parentId = 0;
    this.menuName = menuName;
    this.isEnabled = menuId != 5003; //temp condition
    this.isHomeMenu = false;
    this.isFavMenu = false;
    this.isActionMenu = false;
    this.isActionMenu = false;
    this.isNavMenu = false;
    this.isSubscribeForNotification = false;
    
  }
  
  /**
   * Instantiates a new Menu model.
   *
   * @param in the in
   */
  @Ignore
  protected MenuModel(Parcel in){
    menuId = in.readInt();
    menuCode = in.readString();
    menuName = in.readString();
    parentId = in.readInt();
    iconId = in.readInt();
    imageUrl = in.readString();
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) isEnabled = in.readBoolean();
    else isEnabled = in.readInt() > 0;
    errEnabledMsg = in.readString();
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) isHomeMenu = in.readBoolean();
    else isHomeMenu = in.readInt() > 0;
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) isFavMenu = in.readBoolean();
    else isFavMenu = in.readInt() > 0;
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) isActionMenu = in.readBoolean();
    else isActionMenu = in.readInt() > 0;
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) isNavMenu = in.readBoolean();
    else isNavMenu = in.readInt() > 0;
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) isSubscribeForNotification = in.readBoolean();
    else isSubscribeForNotification = in.readInt() > 0;
    sequence = in.readInt();
    favMenuCode = in.readString();
    favMenuName = in.readString();
    favIconId = in.readInt();
    favImageUrl = in.readString();
    screenMenuCode = in.readString();
    screenMenuName = in.readString();
    screenIconId = in.readInt();
    screenImageUrl = in.readString();
    
  }
  
  /**
   * Get session type int.
   *
   * @param menuCode the menu code
   * @return the int
   */
  public static int getSessionType(final String menuCode){
    switch(chkNull(menuCode, "").toUpperCase().replaceAll("NAV_SER", "NAVSER").replaceAll("ENC_VERIFY", "ENCVERIFY").replaceAll("INV_ADD", "INVADD").replaceAll("INV_BRAND", "INVBRAND").replaceAll("(INV_STOCK_CORRECT|STOCK_CORRECT)", "STOCKCORRECT").replaceAll("SER_OMNI", "OMNI").replaceAll("MOV_REPLENISH", "REPLENISH").split("_")[0]){
      case "DEC":
        return AppCommonMethods.SessionType.DECODING.getValue();
      case "ENC":
        return AppCommonMethods.SessionType.ENCODING.getValue();
      case "ENC_THAN":
        return AppCommonMethods.SessionType.ENCODING_THAN.getValue();
      case "ENCVERIFY":
        return AppCommonMethods.SessionType.VERIFY_ENCODING.getValue();
      case "INV":
        return AppCommonMethods.SessionType.INVENTORY.getValue();
      case "INVADD":
        return AppCommonMethods.SessionType.ADD_INVENTORY.getValue();
      case "INVBRAND":
        return AppCommonMethods.SessionType.BRAND_INVENTORY.getValue();
      case "INVFILTER":
        return AppCommonMethods.SessionType.FILTER_INVENTORY.getValue();
      case "STOCKCORRECT":
        return AppCommonMethods.SessionType.STOCK_CORRECTION.getValue();
      case "NAVSER":
        return AppCommonMethods.SessionType.SCAN.getValue();
      case "SER":
        return AppCommonMethods.SessionType.SEARCH.getValue();
      case "MOV":
        return AppCommonMethods.SessionType.MOVEMENT.getValue();
      case "REPLENISH":
        return AppCommonMethods.SessionType.REPLENISHMENT.getValue();
      case "OMNI":
        return AppCommonMethods.SessionType.OMNICHANNEL.getValue();
      case "INW":
        return AppCommonMethods.SessionType.INWARD.getValue();
      case "OTW":
        return AppCommonMethods.SessionType.OUTWARD.getValue();
      default:
        return 0;
    }
  }
  
  /**
   * Get menu icon name string.
   *
   * @return the string
   */
  public String getMenuIconName(){
    final String menuIcon = menuCode.toLowerCase().replaceAll(" ", "_");
    return "ic_" + (menuIcon.endsWith("_start") ? "start" : menuIcon.endsWith("_config") ? "config" : menuIcon.endsWith("_ord_stats") || menuIcon.endsWith("_order_stats") || menuIcon.endsWith("_achieve") || menuIcon.endsWith("_achievement") || menuIcon.endsWith("_achievements") || menuIcon.endsWith("_award") ? "achieve" : menuIcon);
  }
  
  /**
   * Get session type int.
   *
   * @return the int
   */
  public int getSessionType(){
    return getSessionType(getMenuCode());
  }
  
  /**
   * Get menu id integer.
   *
   * @return the integer
   */
  public Integer getMenuId(){ return chkNull(menuId, 0); }
  
  /**
   * Set menu id.
   *
   * @param menuId the menu id
   */
  public void setMenuId(Integer menuId){ this.menuId = menuId; }
  
  /**
   * Get action menu id int.
   *
   * @param context the context
   * @return the int
   */
  public int getActionMenuId(final Context context){ return context != null ? context.getResources().getIdentifier(getMenuCode().replaceFirst("(?i)TOP_", "ACT_").toLowerCase(), AppConstants.RES_ID, context.getPackageName()) : 0; }
  
  public int getNavMenuId(final Context context){ return context != null ? context.getResources().getIdentifier(getMenuCode(), AppConstants.RES_ID, context.getPackageName()) : 0; }
  
  /**
   * Get menu code string.
   *
   * @return the string
   */
  public String getMenuCode(){ return chkNull(menuCode, ""); }
  
  /**
   * Set menu code.
   *
   * @param menuCode the menu code
   */
  public void setMenuCode(String menuCode){ this.menuCode = menuCode; }
  
  /**
   * Get menu name string.
   *
   * @return the string
   */
  public String getMenuName(){ return chkNull(menuName, ""); }
  
  /**
   * Set menu name.
   *
   * @param menuName the menu name
   */
  public void setMenuName(String menuName){ this.menuName = menuName; }
  
  /**
   * Get parent id integer.
   *
   * @return the integer
   */
  public Integer getParentId(){ return parentId; }
  
  /**
   * Set parent id.
   *
   * @param parentId the parent id
   */
  public void setParentId(Integer parentId){ this.parentId = parentId; }
  
  /**
   * Get icon id int.
   *
   * @param context the context
   * @return the int
   */
  public int getIconId(Context context){ return chkZero(iconId, context.getResources().getIdentifier(getMenuIconName(), AppConstants.RES_DRAWABLE, context.getPackageName())); }
  
  /**
   * Get icon id int.
   *
   * @return the int
   */
  public int getIconId(){ return chkNull(iconId, 0); }
  
  /**
   * Set icon id.
   *
   * @param iconId the icon id
   */
  public void setIconId(int iconId){ this.iconId = iconId; }
  
  /**
   * Get image url string.
   *
   * @return the string
   */
  public String getImageUrl(){ return chkNull(imageUrl, ""); }
  
  /**
   * Set image url.
   *
   * @param imageUrl the image url
   */
  public void setImageUrl(String imageUrl){ this.imageUrl = imageUrl; }
  
  /**
   * Get def redirect fragment class.
   *
   * @return the class
   */
  public Class getDefRedirectFragment(){
    //Class redirectFrag=Class.forName()
    switch(menuCode.toUpperCase().trim()){
      case AppConstants.MENU_CODE_NAV_APP_INFO:
      case AppConstants.MENU_CODE_ACT_APP_INFO:
      case AppConstants.MENU_CODE_APP_INFO:
        return AppInfoFragment.class;
      case AppConstants.MENU_CODE_NAV_CHANGE_PASS:
      case AppConstants.MENU_CODE_ACT_CHANGE_PASS:
      case AppConstants.MENU_CODE_APP_CHANGE_PASS:
        return ResetPasswordFragment.class;
      case AppConstants.MENU_CODE_ACT_SER:
        return ActionMenuSearchFragment.class;
      case AppConstants.MENU_CODE_ACT_MSG:
        return ActionMenuMsgFragment.class;
      case AppConstants.MENU_CODE_ACT_COMPARE:
        return ActionMenuCompareFragment.class;
      case AppConstants.MENU_CODE_ACT_NOTIFY:
        return ActionMenuNotifyFragment.class;
      case AppConstants.MENU_CODE_ENC:
        return EncodingMainFragment.class;
      case AppConstants.MENU_CODE_ENC_CONFIG:
        return EncodingConfigFragment.class;
      case AppConstants.MENU_CODE_ENC_ACHIEVE:
        return EncodingAchieveFragment.class;
      case AppConstants.MENU_CODE_ENC_VERIFY:
        return EncodingVerifyFragment.class;
      case AppConstants.MENU_CODE_ENC_START:
        return EncodingStartFragment.class;
      case AppConstants.MENU_CODE_ENC_SSW:
        return EncodingScanScanWriteFragment.class;
      case AppConstants.MENU_CODE_DEC:
        return DecodingStartFragment.class;
      case AppConstants.MENU_CODE_SCN_CNT:
        return ScanCountFragment.class;
      case AppConstants.MENU_CODE_INV:
        return InventoryMainFragment.class;
      case AppConstants.MENU_CODE_INV_START:
        return InventoryStartFragment.class;
      case AppConstants.MENU_CODE_INV_ADD:
      case AppConstants.MENU_CODE_INV_ADD_START:
        return InventoryAddFragment.class;
      case AppConstants.MENU_CODE_INV_BRAND:
      case AppConstants.MENU_CODE_INV_BRAND_START:
        return InventoryBrandFragment.class;
      case AppConstants.MENU_CODE_INV_FILTER:
      case AppConstants.MENU_CODE_INV_FILTER_START:
        return InventoryFilterFragment.class;
      case AppConstants.MENU_CODE_INV_STOCK_CORRECT:
      case AppConstants.MENU_CODE_STOCK_CORRECT:
        return StockCorrectionMainFragment.class;
      case AppConstants.MENU_CODE_INV_STOCK_CORRECT_START:
      case AppConstants.MENU_CODE_STOCK_CORRECT_START:
        return StockCorrectionStartFragment.class;
      case AppConstants.MENU_CODE_SER:
        return SearchMainFragment.class;
      case AppConstants.MENU_CODE_SER_PROD:
        return ProductSearchFragment.class;
      case AppConstants.MENU_CODE_SER_OMNI:
        return OmniChannelFragment.class;
      case AppConstants.MENU_CODE_SER_OMNI_ACHIEVE:
      case AppConstants.MENU_CODE_SER_OMNI_ORD_STATS:
        return OmniChannelOrderStatsFragment.class;
      case AppConstants.MENU_CODE_SER_OMNI_START:
        return OmniChannelListFragment.class;
      case AppConstants.MENU_CODE_SER_LIST:
        return isStaticDebug() && false ? SearchListsFragment.class : SearchListFragment.class;
      case AppConstants.MENU_CODE_SER_LIST_START:
        return SearchListStartFragment.class;
      case AppConstants.MENU_CODE_SER_ASSORT:
        return SearchAssortMainFragment.class;
      case AppConstants.MENU_CODE_SER_ASSORT_START:
        return SearchAssortStartFragment.class;
      case AppConstants.MENU_CODE_SER_UNENCODED:
        return SearchUnencodedFragment.class;
      case AppConstants.MENU_CODE_SER_ALIEN:
        return SearchAlienFragment.class;
      case AppConstants.MENU_CODE_SER_FILE:
        return SearchFileBasedFragment.class;
      case AppConstants.MENU_CODE_SER_AGE:
        return AgeingSearchFragment.class;
      case AppConstants.MENU_CODE_SER_FIFO:
        return SearchFIFOFragment.class;
      case AppConstants.MENU_CODE_INW:
        return InwardMainFragment.class;
      case AppConstants.MENU_CODE_INW_HU:
        return InwardHuVerificationFragment.class;
      case AppConstants.MENU_CODE_INW_GRN:
        return InwardGrnTripsDataFragment.class;//AppCommonMethods.isSetInwOnline? InwardGrnStartFragment.class :
      case AppConstants.MENU_CODE_INW_GRN_START:
        return InwardGrnStartFragment.class;
      case AppConstants.MENU_CODE_MOV:
        return MovementMainFragment.class;
      case AppConstants.MENU_CODE_MOV_REPLENISH:
      case AppConstants.MENU_CODE_REPLENISH:
        return ReplenishmentListFragment.class;
      case AppConstants.MENU_CODE_MOV_REPLENISH_START:
      case AppConstants.MENU_CODE_REPLENISH_START:
        return ReplenishmentStartFragment.class;
      case AppConstants.MENU_CODE_MOV_START:
        return MovementStartFragment.class;
      case AppConstants.MENU_CODE_OTW:
        return OutwardMainFragment.class;
      case AppConstants.MENU_CODE_OTW_PICK:
        return OutwardPickDataFragment.class;
      case AppConstants.MENU_CODE_OTW_HU:
        return OutwardHuDataFragment.class;
      case AppConstants.MENU_CODE_OTW_START:
        return OutwardPickStartFragment.class;
      case AppConstants.MENU_CODE_OTW_TOTE:
        return OutwardToteMainFragment.class;
      case AppConstants.MENU_CODE_OTW_OFF_RANGE:
      case AppConstants.MENU_CODE_OFF_RANGE:
        return OffRangeMainFragment.class;
      case AppConstants.MENU_CODE_THAN:
        return ThanMainFragment.class;
      case AppConstants.MENU_CODE_THAN_ENC:
        return ThanEncodingFragment.class;
      case AppConstants.MENU_CODE_THAN_CLOSURE:
      case AppConstants.MENU_CODE_THAN_CUTTING:
        return ThanCuttingFragment.class;
      case AppConstants.MENU_CODE_INW_TOTE:
        return InwardToteMainFragment.class;
      case AppConstants.MENU_CODE_OTW_TOTE_MT:
        return InwardToteMainFragment.class;
      case AppConstants.MENU_CODE_OTW_TOTE_DC:
        return OutwardToteDCFragment.class;
      case AppConstants.MENU_CODE_INW1:
      case AppConstants.MENU_CODE_INW_SERIAL:
      case AppConstants.MENU_CODE_OTW1:
        return TripListFragment.class;
      case AppConstants.MENU_CODE_SERIAL_SAVE:
        return SaveSerialFragment.class;
      case AppConstants.MENU_CODE_REPLENISH_DEMAND:
        return ReplenishmentBatchListFragment.class;
      case AppConstants.MENU_CODE_REPLENISH_EOSS:
        return ReplenishmentArticleListFragment.class;
      case AppConstants.MENU_CODE_SER_EXCEL:
        return SearchListExcelFragment.class;
      default:
        return null;
    }
  }
  
  /**
   * Get redirect fragment class.
   *
   * @return the class
   */
  public Class getRedirectFragment(){ return chkNull(redirectFragment, getDefRedirectFragment()); }
  
  /**
   * Set redirect fragment.
   *
   * @param redirectFragment the redirect fragment
   */
  public void setRedirectFragment(Class redirectFragment){ this.redirectFragment = redirectFragment; }
  
  /**
   * Get is enabled boolean.
   *
   * @return the boolean
   */
  public Boolean getIsEnabled(){ return chkNull(isEnabled, false); }
  
  /**
   * Set is enabled.
   *
   * @param isEnabled the is enabled
   */
  public void setIsEnabled(Boolean isEnabled){ this.isEnabled = isEnabled; }
  
  /**
   * Get err enabled msg string.
   *
   * @param context the context
   * @return the string
   */
  public String getErrEnabledMsg(Context context){ return chkNull(errEnabledMsg, context.getString(R.string.feature_not_available)); }
  
  /**
   * Get err enabled msg string.
   *
   * @return the string
   */
  public String getErrEnabledMsg(){ return chkNull(errEnabledMsg, ""); }
  
  /**
   * Set err enabled msg.
   *
   * @param errEnabledMsg the err enabled msg
   */
  public void setErrEnabledMsg(String errEnabledMsg){ this.errEnabledMsg = errEnabledMsg; }
  
  /**
   * Get is home menu boolean.
   *
   * @return the boolean
   */
  public Boolean getIsHomeMenu(){ return chkNull(isHomeMenu, false); }
  
  /**
   * Set is home menu.
   *
   * @param isHomeMenu the is home menu
   */
  public void setIsHomeMenu(Boolean isHomeMenu){ this.isHomeMenu = isHomeMenu; }
  
  /**
   * Get is fav menu boolean.
   *
   * @return the boolean
   */
  public Boolean getIsFavMenu(){ return chkNull(isFavMenu, false); }
  
  /**
   * Set is fav menu.
   *
   * @param isFavMenu the is fav menu
   */
  public void setIsFavMenu(Boolean isFavMenu){ this.isFavMenu = isFavMenu; }
  
  /**
   * Get is action menu boolean.
   *
   * @return the boolean
   */
  public Boolean getIsActionMenu(){ return chkNull(isActionMenu, false); }
  
  /**
   * Set is action menu.
   *
   * @param isActionMenu the is action menu
   */
  public void setIsActionMenu(Boolean isActionMenu){ this.isActionMenu = isActionMenu; }
  
  /**
   * Get is nav menu boolean.
   *
   * @return the boolean
   */
  public Boolean getIsNavMenu(){ return chkNull(isNavMenu, false); }
  
  /**
   * Set is nav menu.
   *
   * @param isNavMenu the is nav menu
   */
  public void setIsNavMenu(Boolean isNavMenu){ this.isNavMenu = isNavMenu; }
  
  /**
   * Get is subscribe for notification boolean.
   *
   * @return the boolean
   */
  public Boolean getIsSubscribeForNotification(){ return chkNull(isSubscribeForNotification, false); }
  
  /**
   * Set is subscribe for notification boolean.
   *
   * @param isSubscribeForNotification the is subscribe for notification
   */
  public void setIsSubscribeForNotification(Boolean isSubscribeForNotification){ this.isSubscribeForNotification = isSubscribeForNotification; }
  
  /**
   * Get sequence integer.
   *
   * @return the integer
   */
  public Integer getSequence(){ return chkNull(sequence, 0); }
  
  /**
   * Set sequence.
   *
   * @param sequence the sequence
   */
  public void setSequence(Integer sequence){ this.sequence = sequence; }
  
  /**
   * Get fav menu code string.
   *
   * @return the string
   */
  public String getFavMenuCode(){ return chkNull(favMenuCode, getMenuCode()); }
  
  /**
   * Set fav menu code.
   *
   * @param favMenuCode the fav menu code
   */
  public void setFavMenuCode(String favMenuCode){ this.favMenuCode = favMenuCode; }
  
  /**
   * Get fav menu name string.
   *
   * @return the string
   */
  public String getFavMenuName(){ return chkNull(favMenuName, getMenuName()); }
  
  /**
   * Set fav menu name.
   *
   * @param favMenuName the fav menu name
   */
  public void setFavMenuName(String favMenuName){ this.favMenuName = favMenuName; }
  
  /**
   * Get fav menu icon name string.
   *
   * @return the string
   */
  public String getFavMenuIconName(){
    if(isNullOrEmpty(favMenuCode)) return getMenuIconName();
    final String favMenuIcon = favMenuCode.toLowerCase().replaceAll(" ", "_");
    return "ic_" + (favMenuIcon.endsWith("_start") ? "start" : favMenuIcon.endsWith("_config") ? "config" : favMenuIcon.endsWith("_ord_stats") || favMenuIcon.endsWith("_order_stats") || favMenuIcon.endsWith("_achieve") || favMenuIcon.endsWith("_achievement") || favMenuIcon.endsWith("_achievements") || favMenuIcon.endsWith("_award") ? "achieve" : favMenuIcon);
  }
  
  /**
   * Get fav icon id int.
   *
   * @param context the context
   * @return the int
   */
  public int getFavIconId(Context context){ return chkZero(getFavIconId(), context.getResources().getIdentifier(getFavMenuIconName(), AppConstants.RES_DRAWABLE, context.getPackageName())); }
  
  /**
   * Get fav icon id int.
   *
   * @return the int
   */
  public int getFavIconId(){ return chkZero(favIconId, getIconId()); }
  
  /**
   * Set fav icon id.
   *
   * @param favIconId the fav icon id
   */
  public void setFavIconId(int favIconId){ this.favIconId = favIconId; }
  
  /**
   * Get fav image url string.
   *
   * @return the string
   */
  public String getFavImageUrl(){ return chkNull(favImageUrl, getImageUrl()); }
  
  /**
   * Set fav image url.
   *
   * @param favImageUrl the fav image url
   */
  public void setFavImageUrl(String favImageUrl){ this.favImageUrl = favImageUrl; }
  
  /**
   * Get screen menu code string.
   *
   * @return the string
   */
  public String getScreenMenuCode(){ return chkNull(screenMenuCode, getMenuCode()); }
  
  /**
   * Set screen menu code.
   *
   * @param screenMenuCode the screen menu code
   */
  public void setScreenMenuCode(String screenMenuCode){ this.screenMenuCode = screenMenuCode; }
  
  /**
   * Get screen menu name string.
   *
   * @return the string
   */
  public String getScreenMenuName(){ return chkNull(screenMenuName, getMenuName()); }
  
  /**
   * Set screen menu name.
   *
   * @param screenMenuName the screen menu name
   */
  public void setScreenMenuName(String screenMenuName){ this.screenMenuName = screenMenuName; }
  
  /**
   * Get screen menu icon name string.
   *
   * @return the string
   */
  public String getScreenMenuIconName(){
    if(isNullOrEmpty(screenMenuCode)) return getMenuIconName();
    final String screenIcon = screenMenuCode.toLowerCase().replaceAll(" ", "_");
    return "ic_" + (screenIcon.endsWith("_start") ? "start" : screenIcon.endsWith("_config") ? "config" : screenIcon.endsWith("_ord_stats") || screenIcon.endsWith("_order_stats") || screenIcon.endsWith("_achieve") || screenIcon.endsWith("_achievement") || screenIcon.endsWith("_achievements") || screenIcon.endsWith("_award") ? "_achieve" : screenIcon);
  }
  
  /**
   * Get screen icon id int.
   *
   * @param context the context
   * @return the int
   */
  public int getScreenIconId(Context context){ return chkZero(getScreenIconId(), context.getResources().getIdentifier(getScreenMenuIconName(), AppConstants.RES_DRAWABLE, context.getPackageName())); }
  
  /**
   * Get screen icon id int.
   *
   * @return the int
   */
  public int getScreenIconId(){ return chkZero(screenIconId, getIconId()); }
  
  /**
   * Set screen icon id.
   *
   * @param screenIconId the screen icon id
   */
  public void setScreenIconId(int screenIconId){ this.screenIconId = screenIconId; }
  
  /**
   * Get screen image url string.
   *
   * @return the string
   */
  public String getScreenImageUrl(){ return chkNull(screenImageUrl, getImageUrl()); }
  
  /**
   * Set screen image url.
   *
   * @param screenImageUrl the screen image url
   */
  public void setScreenImageUrl(String screenImageUrl){ this.screenImageUrl = screenImageUrl; }
  
  @Override
  public int describeContents(){ return 0; }
  
  @Override
  public boolean equals(Object o){
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    MenuModel that = (MenuModel) o;
    return menuId == that.menuId;
  }
  
  @Override
  public String toString(){ return getMenuName(); }
  
  @Override
  public void writeToParcel(Parcel dest, int flags){
    dest.writeInt(chkNull(menuId, 0));
    dest.writeString(menuCode);
    dest.writeString(menuName);
    dest.writeInt(chkNull(parentId, 0));
    dest.writeInt(chkNull(iconId, 0));
    dest.writeString(imageUrl);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) dest.writeBoolean(isEnabled);
    else dest.writeInt(isEnabled ? 1 : 0);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) dest.writeBoolean(isHomeMenu);
    else dest.writeInt(isHomeMenu ? 1 : 0);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) dest.writeBoolean(isFavMenu);
    else dest.writeInt(isFavMenu ? 1 : 0);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) dest.writeBoolean(isActionMenu);
    else dest.writeInt(isActionMenu ? 1 : 0);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) dest.writeBoolean(isNavMenu);
    else dest.writeInt(isNavMenu ? 1 : 0);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) dest.writeBoolean(isSubscribeForNotification);
    else dest.writeInt(isSubscribeForNotification ? 1 : 0);
    dest.writeInt(chkNull(sequence, 0));
    dest.writeString(favMenuCode);
    dest.writeString(favMenuName);
    dest.writeInt(chkNull(favIconId, 0));
    dest.writeString(favImageUrl);
    dest.writeString(screenMenuCode);
    dest.writeString(screenMenuName);
    dest.writeInt(chkNull(screenIconId, 0));
    dest.writeString(screenImageUrl);
  }
}
