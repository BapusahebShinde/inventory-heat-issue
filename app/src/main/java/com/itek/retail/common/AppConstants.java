package com.itek.retail.common;

import com.itek.retail.model.MenuModel;

import java.util.Arrays;
import java.util.List;

/**
 * The App constants
 * stores constant string values
 * such as intent/argument/bundle parameter key constants,
 * menu constants, status constants, type constants
 * session constants and many more...
 */
public class AppConstants{
  
  //default tag password
  public static final String defaultTagZeroPassword = "00000000";
  
  //res types
  public static final String RES_ID = "id";
  public static final String RES_DRAWABLE = "drawable";
  public static final String RES_LAYOUT = "layout";
  
  //Search Percent Values
  public static final int SEARCH_PERCENT_VALUE_0 = 0;
  public static final int SEARCH_PERCENT_VALUE_33 = 33;
  public static final int SEARCH_PERCENT_VALUE_66 = 66;
  public static final int SEARCH_PERCENT_VALUE_90 = 90;
  
  public static final int SEARCH_PERCENT_VALUE_50 = 50;
  public static final int SEARCH_PERCENT_VALUE_75 = 75;
  
  public static final int SEARCH_PERCENT_VALUE_80 = 80;
  public static final int SEARCH_PERCENT_VALUE_60 = 60;
  public static final int SEARCH_PERCENT_VALUE_40 = 40;
  
  //Regex
  public static final String REGEX_USERNAME = "^[A-Za-z0-9\\.\\_\\@]{6,}$";
  public static final String REGEX_PASSWORD = "^[A-Za-z0-9\\~\\!\\@\\#\\$\\%\\^\\&\\*\\-\\_\\+\\=\\,\\.\\:\\;\\?]{6,20}$";
  public static final String REGEX_PIN = "^[0-9]{4}$";
  public static final String REGEX_TAG_PASS = "^[0-9]{8}$";
  public static final String REGEX_URL = "^(http://|https://).*$";
  public static final String REGEX_HEX_COLOR_CODE = "^\\#[0-9A-Fa-f]{3,8}$";
  public static final String REGEX_NUM_BARCODE = "^[0-9]{1,20}$";
  public static final String REGEX_HEX_BARCODE = "^[0-9A-Fa-f]{1,20}$";
  public static final String REGEX_CHAR_BARCODE = "^[0-9A-Za-z]{1,20}$";
  public static final String REGEX_ANY_BARCODE = "^.{1,20}$";
  public static final String REGEX_ANY_BARCODE_BIG = "^.{1,100}$";
  //public static final String REGEX_HEX_HU = "^[0-9-A-Fa-f]{10,25}$";
  public static final String REGEX_TID = "^[0-9-A-Fa-f]{20,}$";
  public static final String REGEX_SERIAL = "^[0-9-A-Za-z!@#$%^&*()_-]{1,50}$";
  //Image URL Replace Regex
  public static final String IMAGE_URL_REPLACE_REGEX = "(\\\"\\\",|\"|\\[|\\]|\\\\|,null|null,)";
  
  //Than Length
  public static final String REGEX_THAN_LENGTH = "((0\\.[1-9][0-9]|0\\.0[1-9])|([1-9][0-9]{0,4}|[1-9][0-9]{0,4}\\.[0-9]{1,2}))";
  // in case 0 length is allowed
  public static final String REGEX_THAN_LENGTH_CLOSURE = "((0|0\\.[0-9]{1,2})|([1-9][0-9]{0,4}|[1-9][0-9]{0,4}\\.[0-9]{1,2}))";
  public static final String REGEX_THAN_LENGTH_INPUT = "((0{0}|0|0\\.[0-9]{0,2})|([1-9]{0}|[1-9][0-9]{0,4}|[1-9][0-9]{0,4}\\.[0-9]{0,2}))";
  
  //String Constants ---
  public static final String UNKNOWN = "Unknown";
  public static final String ALL = "All";
  public static final String NA = "NA";
  public static final String DEFAULT_NO_VALUE = "-";
  
  //tabs
  public static final String TAB_HOME = "HOME";
  public static final String TAB_FAVOURITES = "FAVOURITES";
  public static final String TAB_DASHBOARD = "DASHBOARD";
  public static final String TAB_TODAY = "Today";
  public static final String TAB_ACHIEVEMENTS = "Achievements";
  
  //firebase topics
  public static final String FIREBASE_TOPICS = "FireBaseSubscribedTopics";
  
  //menu_codes
  //static menus
  public static final String MENU_CODE_ADD = "ADD_FAV_MENU";
  public static final String MENU_CODE_SHOP_CLUSTER = "SHOP_CLUSTER";
  public static final String MENU_CODE_PROD_DTLS = "Product Details";
  //actual menus
  public static final String MENU_CODE_ACT_SER = "ACT_SER";
  public static final String MENU_CODE_ACT_MSG = "ACT_MSG";
  public static final String MENU_CODE_ACT_COMPARE = "ACT_COMPARE";
  public static final String MENU_CODE_ACT_NOTIFY = "ACT_NOTIFY";
  public static final String MENU_CODE_ACT_APP_INFO = "ACT_APP_INFO";
  public static final String MENU_CODE_NAV_APP_INFO = "NAV_APP_INFO";
  public static final String MENU_CODE_APP_INFO = "APP_INFO";
  public static final String MENU_CODE_NAV_CHANGE_PASS = "NAV_CHANGE_PASS";
  public static final String MENU_CODE_ACT_CHANGE_PASS = "ACT_CHANGE_PASS";
  public static final String MENU_CODE_APP_CHANGE_PASS = "CHANGE_PASS";
  public static final String MENU_CODE_DEC = "DEC";
  public static final String MENU_CODE_ENC = "ENC";
  public static final String MENU_CODE_ENC_CONFIG = "ENC_CONFIG";
  public static final String MENU_CODE_ENC_ACHIEVE = "ENC_ACHIEVE";
  public static final String MENU_CODE_ENC_VERIFY = "ENC_VERIFY";
  public static final String MENU_CODE_ENC_START = "ENC_START";
  public static final String MENU_CODE_ENC_SSW = "ENC_SSW";
  public static final String MENU_CODE_SCN_CNT = "SCN_CNT";
  public static final String MENU_CODE_INV = "INV";
  public static final String MENU_CODE_INV_BRAND = "INV_BRAND";
  public static final String MENU_CODE_INV_BRAND_START = "INV_BRAND_START";
  public static final String MENU_CODE_INV_FILTER = "INV_FILTER";
  public static final String MENU_CODE_INV_FILTER_START = "INV_FILTER_START";
  public static final String MENU_CODE_INV_START = "INV_START";
  public static final String MENU_CODE_INV_ADD = "INV_ADD";
  public static final String MENU_CODE_INV_ADD_START = "INV_ADD_START";
  public static final String MENU_CODE_INV_STOCK_CORRECT = "INV_STOCK_CORRECT";
  public static final String MENU_CODE_STOCK_CORRECT = "STOCK_CORRECT";
  public static final String MENU_CODE_INV_STOCK_CORRECT_START = "INV_STOCK_CORRECT_START";
  public static final String MENU_CODE_STOCK_CORRECT_START = "STOCK_CORRECT_START";
  public static final String MENU_CODE_SER = "SER";
  public static final String MENU_CODE_SER_PROD = "SER_PROD";
  public static final String MENU_CODE_SER_AGEING = "SER_AGE";
  public static final String MENU_CODE_SER_OMNI = "SER_OMNI";
  public static final String MENU_CODE_SER_OMNI_ACHIEVE = "SER_OMNI_ACHIEVE";
  public static final String MENU_CODE_SER_OMNI_ORD_STATS = "SER_OMNI_ORD_STATS";
  public static final String MENU_CODE_SER_OMNI_START = "SER_OMNI_START";
  public static final String MENU_CODE_SER_LIST = "SER_LIST";
  public static final String MENU_CODE_SER_LIST_START = "SER_LIST_START";
  public static final String MENU_CODE_SER_ASSORT = "SER_ASSORT";
  public static final String MENU_CODE_SER_ASSORT_START = "SER_ASSORT_START";
  public static final String MENU_CODE_SER_UNENCODED = "SER_UNENCODED";
  public static final String MENU_CODE_SER_ALIEN = "SER_ALIEN";
  public static final String MENU_CODE_SER_AGE = "SER_AGE";
  public static final String MENU_CODE_SER_FILE = "SER_FILE";
  public static final String MENU_CODE_SER_FIFO = "SER_FIFO";
  public static final String MENU_CODE_INW = "INW";
  public static final String MENU_CODE_INW_HU = "INW_HU";
  public static final String MENU_CODE_INW_GRN = "INW_GRN";
  public static final String MENU_CODE_INW_GRN_START = "INW_GRN_START";
  public static final String MENU_CODE_MOV = "MOV";
  public static final String MENU_CODE_MOV_START = "MOV_START";
  public static final String MENU_CODE_MOV_REPLENISH = "MOV_REPLENISH";
  public static final String MENU_CODE_REPLENISH = "REPLENISH";
  public static final String MENU_CODE_MOV_REPLENISH_START = "MOV_REPLENISH_START";
  public static final String MENU_CODE_REPLENISH_START = "REPLENISH_START";
  public static final String MENU_CODE_OTW = "OTW";
  public static final String MENU_CODE_OTW_PICK = "OTW_PICK";
  public static final String MENU_CODE_OTW_PICK_START = "OTW_PICK_START";
  public static final String MENU_CODE_OTW_HU = "OTW_HU";
  public static final String MENU_CODE_OTW_START = "OTW_START";
  public static final String MENU_CODE_OTW_TOTE = "OTW_TOTE";
  public static final String MENU_CODE_OTW_OFF_RANGE = "OTW_OFF_RANGE";
  public static final String MENU_CODE_OFF_RANGE = "OFF_RANGE";
  public static final String MENU_CODE_THAN = "THAN";
  public static final String MENU_CODE_THAN_ENC = "THAN_ENC";
  public static final String MENU_CODE_THAN_CUTTING = "THAN_CUTTING";
  public static final String MENU_CODE_THAN_CLOSURE = "THAN_CLOSURE";
  public static final String MENU_CODE_INW_TOTE = "INW_TOTE";
  public static final String MENU_CODE_OTW_TOTE_MT = "OTW_TOTE_MT";
  public static final String MENU_CODE_OTW_TOTE_DC = "OTW_TOTE_DC";

  //new INW/OTW (Phase1)
  public static final String MENU_CODE_INW1 = "INW1";
  public static final String MENU_CODE_INW_SERIAL = "INW_SERIAL";
  public static final String MENU_CODE_OTW1 = "OTW1";
  
  //Save Serial
  public static final String MENU_CODE_SERIAL_SAVE = "SAVE_SERIAL";

  //On Demand Replenishment
  public static final String MENU_CODE_REPLENISH_DEMAND = "REPLENISH_DEMAND";

  //Replenishment End of Season Sale
  public static final String MENU_CODE_REPLENISH_EOSS = "REPLENISH_EOSS";

  //Excel Based Search
  public static final String MENU_CODE_SER_EXCEL = "SER_EXCEL";
  public static final String MENU_CODE_SER_EXCEL_START = "SER_EXCEL_START";
  
  //menus
  public static final String MENU_TITLE_ADD_MORE = "Add More";
  public static final MenuModel MENU_ADD_MORE = new MenuModel(5001, AppConstants.MENU_CODE_ADD, AppConstants.MENU_TITLE_ADD_MORE);
  public static final String MENU_TITLE_CHECK_SHOPS = "Other Stores";
  public static final MenuModel MENU_SHOP_CLUSTER = new MenuModel(5003, AppConstants.MENU_CODE_SHOP_CLUSTER, AppConstants.MENU_TITLE_CHECK_SHOPS);
  public static final String MENU_TITLE_PRODUCT_DETAILS = "Product Details";
  public static final MenuModel MENU_PRODUCT_DETAILS = new MenuModel(5005, AppConstants.MENU_CODE_SHOP_CLUSTER, AppConstants.MENU_TITLE_PRODUCT_DETAILS);
  
  //dashboard view types
  public static final String DASHBOARD_VIEW_TYPE = "type";
  public static final String DASHBOARD_VIEW_TYPE_COMPLETED = "Completed";
  public static final String DASHBOARD_VIEW_TYPE_PENDING = "Pending";
  public static final String DASHBOARD_VIEW_TYPE_TOTAL = "Total";
  public static final String DASHBOARD_VIEW_TYPE_TODAY = "Today";
  public static final String DASHBOARD_VIEW_TYPE_SESSION = "Session";
  public static final String DASHBOARD_VIEW_TYPE_ORDER = "Order";
  public static final String DASHBOARD_VIEW_TYPE_ITEMS = "Item";
  public static final String DASHBOARD_VIEW_TYPE_FASTEST_TIME = "Time";
  public static final List<String> DASHBOARD_VIEW_TYPES = Arrays.asList(new String[]{DASHBOARD_VIEW_TYPE_COMPLETED.toLowerCase().trim(), DASHBOARD_VIEW_TYPE_PENDING.toLowerCase().trim(), DASHBOARD_VIEW_TYPE_TOTAL.toLowerCase().trim(), DASHBOARD_VIEW_TYPE_TODAY.toLowerCase().trim(), DASHBOARD_VIEW_TYPE_SESSION.toLowerCase().trim(), DASHBOARD_VIEW_TYPE_ORDER.toLowerCase().trim(), DASHBOARD_VIEW_TYPE_ITEMS.toLowerCase().trim(), DASHBOARD_VIEW_TYPE_FASTEST_TIME.toLowerCase().trim()});
  
  //than status
  public static final String THAN_STATUS_ENCODING = "Encoding";
  public static final String THAN_STATUS_CUTTING = "Cutting";
  public static final String THAN_STATUS_CLOSURE = "Closure";
  public static final String THAN_STATUS_CLOSED = "Closed";
  
  //replenishment types
  public static final String REPLENISH_TYPE_BOTH = "both";
  public static final String REPLENISH_TYPE_STATIC = "static";
  public static final String REPLENISH_TYPE_DYNAMIC = "dynamic";
  
  //unencoded search types
  public static final String UNENCODED_SEARCH_TYPE_BOTH = "both";
  public static final String UNENCODED_SEARCH_TYPE_OFFLINE = "offline";
  public static final String UNENCODED_SEARCH_TYPE_ONLINE = "online";
  
  //alien search types
  public static final String ALIEN_SEARCH_TYPE_BOTH = "both";
  public static final String ALIEN_SEARCH_TYPE_OFFLINE = "offline";
  public static final String ALIEN_SEARCH_TYPE_ONLINE = "online";
  
  //omnichannel types
  public static final String OMNI_TYPE_BOTH = "both";
  public static final String OMNI_TYPE_ORDER = "order";
  public static final String OMNI_TYPE_EAN = "ean";
  
  //ageing types
  public static final String AGEING_TYPE_BOTH = "both";
  public static final String AGEING_TYPE_ORDER = "order";
  public static final String AGEING_TYPE_EAN = "ean";
  public static final String AGEING_BUCKET = "ean";
  
  //omnichannel upload types
  public static final String OMNI_UPLOAD_TYPE_COMPLETE = "complete";
  public static final String OMNI_UPLOAD_TYPE_PARTIAL = "partial";
  public static final String OMNI_UPLOAD_TYPE_NIL = "nil";
  
  //Product Status
  public static final String STATUS_EMPTY = "";
  public static final String STATUS_PENDING = "Pending";
  public static final String STATUS_IN_PROGRESS = "In Progress";
  public static final String STATUS_COMPLETE = "Complete";
  public static final String STATUS_COMPLETED = "Completed";
  public static final String STATUS_MISMATCHED = "Mismatched";
  public static final String STATUS_MATCHED = "Matched";
  public static final String STATUS_SOLD = "Sold";
  public static final String STATUS_ERROR = "Error";
  public static final String STATUS_VERIFIED = "Verified";
  public static final String STATUS_SAVE = "Save";
  public static final String STATUS_SAVED = "Saved";
  
  //EAN type
  public static final String EAN_TYPE_STD = "std";
  public static final String EAN_TYPE_NONSTD = "nonstd";
  public static final String EAN_TYPE_BOTH = "both";
  
  //Encode type
  public static final String ENCODE_TYPE_ONE = "One-One";
  public static final String ENCODE_TYPE_MANY = "One-Many";
  public static final String ENCODE_TYPE_BARCODE_RFID = "Barcode-RFID";
  public static final String ENCODE_TYPE_BARCODE_BARCODE_RFID = "Barcode-Barcode-RFID";
  
  //Encode Qty
  public static final String ENCODE_CONFIG_QTY_ONE_MANY = "Enc-One-Many-Qty";
  
  //Encoding
  public static final String AVG_TAGS_ENCODE_PER_DAY = "avgTagsEncodedPerDay";
  public static final String MAX_TAGS_ENCODE_PER_DAY = "maxTagsEncodedPerDay";
  public static final String AVG_ENCODE_TIME_PER_TAG = "avgEncodingTimePerTag";
  public static final String HOURS_SPENT = "hoursSpent";
  
  //Search (Unencoded Search)
  public static final String UNENCODED_SEARCH_TYPE = "unencodedSearchType";
  public static final String ALIEN_SEARCH_TYPE = "alienSearchType";
  
  //Search (Omnichannel)
  public static final String OMNICHANNEL_TYPE = "omnichannelType";
  public static final String OMNICHANNEL_UPLOAD_TYPE = "omnichannelUploadType";
  public static final String IS_EAN_SEARCH = "isEANSearch";
  public static final String IS_ALLOW_DECODE = "isAllowDecode";
  public static final String IS_ALLOW_DECODE_ON_PICK = "isAllowDecodeOnPick";
  public static final String IS_ALLOW_DECODE_WITHOUT_VERIFY = "isAllowDecodeOnPick";
  public static final String IS_STATUS_VERIFIED = "isStatusVerified";
  public static final String HEADER_ORDER_NO_EAN = "headerOrderNoEan";
  public static final String HAS_PICK_DATA = "hasPickData";
  public static final String PICKED_EPCS = "pickedEPCs";
  public static final String DECODED_EPCS = "decoedEPCs";
  
  //Search Log (Backend Upload)
  public static final String LOG_IDS = "logNo";
  
  //Search (List based)
  public static final String SEARCH_LIST_ID = "listId";
  public static final String SEARCH_LIST_TYPE = "listType";//"type";
  
  //Assortment Search (List based)
  public static final String SEARCH_ASSORTMENT_CODE = "code";
  public static final String SEARCH_ASSORTMENT_PRIORITY = "priority";
  
  //File View
  public static final String FILE_PATH = "filePath";
  public static final String FILE_NAME = "fileName";
  public static final String FILE_EXT = "fileExt";
  
  //INWARD
  public static final String TRIP_NUMBERS = "tripNumbers";
  public static final String TRIP_NUMBER = "tripNum";
  public static final String TRIP_TYPE = "tripType";
  public static final String HU_NUMBER = "huNum";
  public static final String HU_NUMBERS = "huNumbers";
  public static final String DELIVERY_NUMBER = "deliveryNum";
  
  //tripStatus
  public static final String TRIP_STATUS_PENDING = "Pending";
  public static final String TRIP_STATUS_IN_PROGRESS = "In Progress";
  public static final String TRIP_STATUS_PROCESSING = "Processing";
  public static final String TRIP_STATUS_COMPLETED = "Completed";
  
  //huStatus
  public static final String K_HU_STATUS_ACCEPT = "Accept";
  public static final String K_HU_STATUS_REJECT = "Reject";
  public static final String K_HU_STATUS_RESCAN = "Rescan";
  
  public static final String HU_STATUS_COMPLETE = "C";
  public static final String HU_STATUS_ACCEPT = "A";
  public static final String HU_STATUS_REJECT = "R";
  public static final String HU_STATUS_PENDING = "P";
  public static final String HU_STATUS_IN_PROGRESS = "I";
  
  //pickListStatus
  public static final String PICK_LIST_STATUS_COMPLETE = "C";
  public static final String PICK_LIST_STATUS_RELEASE = "P";
  
  //On Demand Replenishment Status
  public static final String BATCH_STATUS_ACTIVE = "Active";
  public static final String BATCH_STATUS_INACTIVE = "Inactive";
  public static final String BATCH_STATUS_COMPLETE = "Complete";
  public static final String BATCH_STATUS_IN_PROGRESS = "In Progress";
  public static final String BATCH_STATUS_PENDING = "Pending";
  
  //userAction types
  public static final String USER_ACTION_RFID = "RFID";
  public static final String USER_ACTION_MANUAL = "MANUAL";
  
  //epc extra values
  public static final String NON_ENCODED = "Unencoded";
  public static final String ALIEN = "Alien";
  public static final String VALID = "Valid";
  public static final String EXTRA_EAN = "Unknown";
  
  //dialog (shift to strings.xml)
  public static final String K_TRIP_INFO = "Trip Information: ";
  public static final String K_TRIP_COMPLETED = "Total Completed Trips : ";
  public static final String K_UPLOAD_TRIP = "Upload Trip";
  public static final String K_TRIP_NOS = "Trip No(s):";
  public static final String K_TRIP_COMPLETE_COUNT = " Completed :";
  public static final String K_TRIP_ACCEPT_COUNT = " Accepted :";
  public static final String K_TRIP_REJECT_COUNT = " Rejected :";
  public static final String K_TRIP_PENDING_COUNT = " Pending :";
  
  //Intent/Bundle ParameterKeys
  public static final String TITLE = "title";
  public static final String NOTIFICATION_TYPE = "type";
  public static final String NOTIFICATION_TYPE_ID = "typeId";
  public static final String TITLE_LOGO_RES_ID = "titleLogoResId";
  public static final String TITLE_LOGO_URL = "titleLogoUrl";
  public static final String MENU_ID = "menuId";
  public static final String MENU_CODE = "menuCode";
  public static final String MENU_ICON_ID = "menuIconId";
  public static final String ACTIVE_USERS = "activeUsers";
  public static final String SESSION_VALID_TILL = "sessionValidTill";
  public static final String TARGET = "target";
  public static final String EAN = "ean";
  public static final String SAVE_INSTANCE = "save_instance";
  public static final String PROGRESS = "progress";
  public static final String ITEM_MODEL = "itemModel";
  public static final String BRAND = "brand";
  public static final String BRANDS = "brands";
  public static final String FOUND = "found";
  public static final String SHORTAGE = "shortage";
  public static final String CATEGORY = "category";
  public static final String LOCATION = "location";
  public static final String ZONE = "zone";
  public static final String ZONE_ID = "zoneId";
  public static final String SRC_ZONE = "srcZone";
  public static final String DEST_ZONE = "destZone";
  public static final String REPLENISHMENT_TYPE = "replenishmentType";
  public static final String ACTION_TYPE = "type";
  
  //SessionAction
  public static final String SESSION_ACTION_START = "Start";
  public static final String SESSION_ACTION_STOP = "Stop";
  public static final String SESSION_ACTION_PAUSE = "Pause";
  public static final String SESSION_ACTION_RESUME = "Resume";
  public static final String SESSION_ACTION_SAVE = "Save";
  public static final String SESSION_ACTION_UPLOAD = "Upload";
  public static final String SESSION_ACTION_UPLOAD_OFFLINE = "Upload_Offline";
  public static final String SESSION_ACTION_UPLOAD_BACKGROUND = "Upload_Background";
  public static final String SESSION_ACTION_DISCARD = "Discard";
  public static final String SESSION_ACTION_VERIFY_STATUS = "Verify";
  public static final String SESSION_ACTION_RELEASE = "Release";
  
  //Type
  public static final String OMNICHANNEL = "OMNI";
  public static final String INWARD = "IN";
  public static final String OUTWARD = "OUT";
  public static final String OUTWARD_PICK = "OUT_PICK";
  public static final String DECODE = "D";
  public static final String ENCODE = "E";
  public static final String TAKE_STOCK = "SC";
  public static final String TURBO_STOCK = "TC";
  public static final String BRAND_STOCK = "BI";//"BR"
  public static final String STOCK_CORRECTION = "AC";
  public static final String LISTED_STOCK = "LS";
  public static final String TEAM_STOCK = "CC";
  public static final String ADD_STOCK = "IC";
  public static final String REMOVE_STOCK = "DC";
  public static final String CYCLE_COUNT_CORRECTION_STOCK = "CL";
  public static final String STR_CYCLE_COUNT_CORRECTION_STOCK = "CCC";
  public static final String MOVE_STOCK = "MV";
  public static final String STR_MOVE_STOCK = "Move";
  public static final String MOVE_LISTED_STOCK = "ML";
  public static final String RECEIVE_STOCK = "GI";
  public static final String REPLENISHMENT_STOCK = "GI";
  public static final String PIC_STOCK = "PK";
  public static final String RETURN_STOCK = "RT";
  public static final String TRANSFER_STOCK = "TR";
  public static final String TRANSFER_LISTED_STOCK = "TL";
}
