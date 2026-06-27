package com.itek.retail.apis;

/**
 * The Param constants file stores the constant/final keys
 * used in Request/Response during API call.
 */
public class ParamConstants{
  
  //Header
  public static final String HEADER_ACCEPT = "Accept";
  public static final String HEADER_CONTENT_TYPE = "Content-Type";
  public static final String HEADER_AUTHORIZATION = "Authorization";
  public static final String HEADER_ACCEPT_VAL = "application/json";
  public static final String HEADER_CONTENT_TYPE_VAL = "application/json";
  public static final String BASIC_ACCESS_TOKEN_VAL = "BASIC RE9UTkVUOjM5QzU1ODU2LUJFOEYtNEJEMi05NDE4LTlDREQ2OTA2QkQxRA==";
  
  //Request (Token)
  public static final String TOKEN_GRANT_TYPE = "grant_type";
  public static final String TOKEN_USER_NAME = "username";
  public static final String TOKEN_PASSWORD = "password";
  public static final String TOKEN_DEVICE_ID = "device_id";
  public static final String TOKEN_GRANT_TYPE_VAL = "password";
  public static final String TOKEN_USER_NAME_VAL = "apiuser";
  public static final String TOKEN_PASSWORD_VAL = "apiuser";
  public static final String DEVICE_TYPE_VAL = "HandyAndroid";
  
  //check_version
  public static final String VERSION = "version";
  public static final String APK_VER = "apkver";
  public static final String FTP_PATH = "ftppath";
  public static final String APK_NAME = "apk_name";
  public static final String BUILD_URL = "buildUrl";
  public static final String BUILD_NAME = "buildName";
  
  
  //Request
  public static final String DEVICE_ID = "deviceId";
  public static final String DEVICE_MAC_ID = "deviceMacId";
  public static final String DEVICE_SERIAL = "deviceSerial";
  public static final String DEVICE_SERIAL_NO = "deviceSerialNo";
  public static final String APP_VERSION = "appVersion";
  public static final String APPLICATION_VERSION = "applicationVersion";
  public static final String MAC_ID = "macId";
  public static final String IP_ADDRESS = "ipAddress";
  public static final String LAT_LNG = "latLng";
  public static final String DEVICE_DATE_TIME = "deviceDateTime";
  public static final String TIME_ZONE_OFFSET_HOURS = "timeZoneOffsetInHours";
  public static final String FIREBASE_TOKEN = "firebaseToken";
  public static final String USER_NAME = "userName";
  public static final String OLD_USER_PASSWORD = "oldPassword";
  public static final String New_USER_PASSWORD = "newPassword";
  public static final String PASSWORD = "password";
  public static final String PIN = "pin";
  public static final String USER_ID = "userId";
  public static final String STORE_ID = "storeId";
  public static final String STORE_CODE = "storeCode";
  public static final String STORE_TYPE = "storeType";
  public static final String SUPPLY_CHAIN_TYPE = "supplyChainType";
  public static final String SUPPLY_CHAIN_TYPE_NAME = "supplyChainTypeName";
  public static final String SUPPLY_CHAIN_TYPE_ID = "supplyChainTypeId";
  public static final String SUPPLY_CHAIN_TYPE_MASTER_NAME = "supplyChainTypeMasterName";
  public static final String SUPPLY_CHAIN_TYPE_MASTER_ID = "supplyChainTypeMasterId";
  public static final String SUPPLY_CHAIN_TYPE_MASTER_LIST = "supplyChainTypeMasterList";
  public static final String SUPPLY_CHAIN_TYPE_LIST = "supplyChainTypeList";
  public static final String STORE_LIST = "storeList";
  public static final String IS_RFID_STORE = "isRfidStore";
  public static final String ACTION = "action";
  public static final String STATUS = "status";
  public static final String TYPE = "type";
  public static final String TYPES = "types";
  public static final String SUB_TYPE = "subType";
  public static final String TYPE_ID = "typeId";
  public static final String SESSION_TYPE = "sessionType";
  public static final String LIST_SITE_TYPE = "listSiteType";
  public static final String SITE_TYPES = "siteTypes";
  
  //Response (Force Logout)
  public static final String IS_FORCE_LOGOUT = "isForceLogout";
  
  //Response (Error/Fail)
  public static final String TOKEN_ERR_MESSAGE = "error_description";//errorDescription
  
  //Response (Error/Fail)
  public static final String MESSAGE = "message";//message
  public static final String MSG = "msg";//message
  public static final String ERR_MSG = "errMsg";//message
  public static final String ERROR = "error";//message
  public static final String SUCCESS = "success";//message
  
  //Response (Token)
  public static final String TOKEN_TYPE = "token_type";//tokenType
  public static final String ACCESS_TOKEN = "access_token";//accessToken
  public static final String EXPIRES_IN = "expires_in";//expiresIn
  
  //Login Response (Internal Flags)
  public static final String IS_ALLOW_SHARE_LOG = "isAllowShareLog";
  public static final String IS_SHOW_USER_PROFILE_ALERT_AFTER_LOGIN = "isShowUserProfileAlertAfterLogin";
  public static final String IS_SHOW_CHECK_AVAILABILITY_BTN = "isShowCheckAvailabilityBtnForProductDetails";
  public static final String IS_DASHBOARD_AUTO_REFRESH = "isAutoRefreshDashboards";
  
  //Login Response (Internal Flags -> Inventory)
  public static final String IS_ALLOW_SHORT_JSON_REQUEST_FOR_INVENTORY_UPLOAD = "isAllowShortJsonRequestForInventoryUpload";
  public static final String IS_ALLOW_SHORT_JSON_REQUEST_FOR_STOCK_CORRECTION_UPLOAD = "isAllowShortJsonRequestForStockCorrectionUpload";
  public static final String IS_ALLOW_ADVANCE_FILTERS_FOR_BRAND_INVENTORY = "isAllowAdvanceFiltersForBrandInventory";
  public static final String IS_ALLOW_ADVANCE_FILTERS_FOR_MULTI_BRANDS = "isAllowAdvanceFiltersForMultiBrands";
  public static final String IS_ALLOW_ADVANCE_FILTERS_FOR_CATEGORY_INVENTORY = "isAllowAdvanceFiltersForCategoryInventory";
  public static final String IS_ALLOW_ADVANCE_FILTERS_FOR_MULTI_CATEGORIES = "isAllowAdvanceFiltersForMultiCategories";
  public static final String IS_EPC_SEARCH_IN_STOCK_CORRECTION = "isEPCSearchInStockCorrection";
  public static final String IS_ALLOW_ALL_ZONE_INVENTORY_FOR_TAKE_STOCK = "isAllowAllZoneInventoryForTakeStock";
  
  //Login Response (Internal Flags -> Encoding Config)
  public static final String IS_OFFLINE_ENCODE="isUseOfflineEncoding";
  public static final String IS_OFFLINE_REENCODE="isAllowOfflineReEncoding";
  public static final String IS_CHECK_DEFAULT_PASSWORD_FIRST="isCheckDefaultPasswordFirst";
  
  //Login Response (Internal Flags -> New Encoding Verify)
  public static final String IS_ENC_VERIFY="isVerifyEncoding";
  public static final String IS_ENC_UPLOAD_AFTER_VERIFY="isUploadEncodingAfterVerifying";
  public static final String IS_ENC_UPLOAD_BY_SCHEDULER="isUseSchedulerForWritenTagUpload";
  public static final String IS_ENC_UPLOAD_BY_WHILE_PROCESSING="isAllowBackgroundWritenTagUploadWhileProcessing";
  public static final String IS_ENC_UPLOAD_BY_BOTH_IMMEDIATE_SCHEDULER="isAllowBothImmediateUploadAndUploadSchedulerForWrittenTags";
  public static final String SESSION_FORCE_END_PASSWORD="sessionForceEndPassword";
  
  //Flags for Handling append Zero to Barcode Config
  public static final String IS_ALLOW_LEADING_ZERO_FOR_NON_STD_BARCODE = "isIsAllowLeadingZeroForNonStdBarcode";
  public static final String IS_ALLOW_LEADING_ZERO_FOR_STD_BARCODE = "isIsAllowLeadingZeroForStdBarcode";
  
  
  //Login Response (Internal Flags -> Notification Handling)
  public static final String IS_HANDLE_NOTIFICATION_DATA_VIA_INTENT = "isHandleNotifcationDataFromIntent";
  
  //Login Response (Internal Flags ->  List Base Search - > set/don't set API calling for Mark as Found)
  public static final String IS_API_BASED_MARK_FOUND_LBS = "isAPIBasedMarkFoundForListBaseSearch";
  
  
  //Response
  public static final String DATA = "data";
  public static final String DASHBOARD_DATA = "dashboardData";
  public static final String ALLOW_DB_BACKUP = "allowDBBackup";
  public static final String DEVICE_TYPE = "deviceType";
  public static final String DEVICE_MODEL = "deviceModel";
  public static final String DEVICE_KEY_CODES = "deviceKeyCodes";
  public static final String IS_DEVICE_BLUETOOTH_DEPENDENT = "isDeviceBluetoothDependent";
  public static final String CLIENT_ID = "clientId";
  public static final String CLIENT_NAME = "clientName";
  public static final String STORE_NAME = "storeName";
  public static final String USER_PROFILE_IMG = "userProfileImg";
  public static final String MENUS = "menus";
  public static final String ACTION_MENUS = "actionMenus";
  public static final String NAV_MENUS = "navMenus";
  public static final String IS_SHOW_FAVOURITE_MENU_SCREEN = "isShowFavouriteMenuScreen";
  public static final String DASHBOARD_URL = "dashboardUrl";
  public static final String MAX_SELECTION = "maxSelection";
  public static final String MAX_SELECTION_ZONES = "maxSelectionZones";
  public static final String ZONES = "zones";
  public static final String ZONE_ID = "zoneId";
  public static final String SOURCE_ZONE_ID = "sourceZoneId";
  public static final String DESTINATION_ZONE_ID = "destinationZoneId";
  public static final String ZONE = "zone";
  public static final String ZONE_NAME = "zoneName";
  public static final String SOURCE_ZONE = "sourceZone";
  public static final String DESTINATION_ZONE = "destinationZone";
  public static final String ZONE_TYPE = "zoneType";
  public static final String IS_DEFAULT_ZONE = "isDefault";
  public static final String MAX_SELECTION_BRANDS = "maxSelectionBrands";
  public static final String BRANDS = "brands";
  public static final String BRAND_EANS = "brandEans";
  public static final String BRAND_ID = "brandId";
  public static final String BRAND = "brand";
  public static final String BRAND_NAME = "brandName";
  public static final String BRAND_TYPE = "brandType";
  public static final String MAX_SELECTION_CATEGORIES = "maxSelectionCategories";
  public static final String CATEGORIES = "categories";
  public static final String CATEGORY_ID = "categoryId";
  public static final String CATEGORY = "category";
  public static final String CATEGORY_NAME = "categoryName";
  public static final String CATEGORY_TYPE = "categoryType";
  public static final String REPLENISHMENT_TYPE = "replenishmentType";
  public static final String OMNICHANNEL_TYPE = "omnichannelType";
  public static final String UNENCODED_SEARCH_TYPE = "unencodedSearchType";
  public static final String ALIEN_SEARCH_TYPE = "alienSearchType";
  public static final String ORDER_NO = "orderNo";
  public static final String CURRENT_ACCESS_PWD = "currentaccesspwd";
  public static final String OLD_ACCESS_PWDS = "oldaccesspwds";
  public static final String CURRENT_ACCESS_PASSWORD = "currentAccessPassword";
  public static final String OLD_ACCESS_PASSWORDS = "oldAccessPasswords";
  public static final String OLD_PASSWORD = "accesspwd";
  public static final String IS_CHECK_PASSWORD_FIRST = "isCheckPasswordFirst";
  public static final String IS_CHECK_CURRENT_PASSWORD_FIRST = "isCheckCurrentPasswordFirst";
  public static final String IS_CHECK_PASSWORD_BEFORE_API = "isCheckPasswordBeforeAPI";
  public static final String IS_CHECK_PASSWORD_BEFORE_BASED_ON_EPC = "isCheckPasswordBasedOnEPC";
  public static final String EAN = "ean";
  public static final String AGE = "age";
  public static final String EANS = "eans";
  
  public static final String IS_MULTI_ATTRIBUTES_SELECTION="isMultiAttributesSelection";
  public static final String ATTRIBUTE_ID="attributeId";
  public static final String ATTRIBUTE_NAME="attributeName";
  
  //Changes for EAN mapping
  public static final String IS_EAN_MAPPING = "isEANMappingEnabled";
  public static final String IS_11_DIGIT_STD_EAN = "is11DigitStandard";
  public static final String MAPPED_EAN = "mappedEan";
  public static final String MAPPED_EANS = "mappedEans";
  public static final String IS_USE_REFERENCE_EAN = "useReferenceEan";
  public static final String IS_USE_REFERENCE_BARCODE = "useReferenceBarcode";
  public static final String IS_USE_REFERENCE_ID = "useReferenceId";
  public static final String REFERENCE_EAN = "referenceEan";
  public static final String REFERENCE_BARCODE = "referenceBarcode";
  public static final String REFERENCE_ID = "referenceId";

  //Changes for dynamic Product labels

  public static final String PRODUCT_LABELS = "productLabels";
  public static final String LABEL_CATEGORIES = "labelCategories";
  public static final String LABEL_BRANDS = "labelBrands";

  public static final String LABEL_ZONES = "labelZones";
  public static final String LABEL_EANS = "labelEANs";
  public static final String LABEL_PRODUCT_ID = "labelProductId";
  public static final String LABEL_ARTICLES = "labelArticles";
  public static final String LABEL_SIZES = "labelSizes";
  public static final String LABEL_COLORS = "labelColors";

  public static final String LABEL_NAME = "labelName";
  public static final String LABEL_DESCRIPTION = "labelDescription";
  public static final String LABEL_MATKL = "labelMatkl";

  public static final String LABEL_SUB_CATEGORY = "labelSubCategory";
  public static final String LABEL_TYPE = "labelType";
  public static final String LABEL_SUB_TYPE = "labelSubType";
  public static final String LABEL_SRC_ZONE = "labelSrcZone";
  public static final String LABEL_DEST_ZONE = "labelDestZone";
  public static final String LABEL_PRIORITY = "labelPriority";
  public static final String LABEL_STATUS = "labelStatus";
  public static final String LABEL_ORDER = "labelOrder";
  public static final String LABEL_AGEING = "labelAgeing";
  public static final String LABEL_EPC = "labelEPC";
  public static final String LABEL_TID = "labelTID";


  //changes for configurable optimized inventory
  public static final String OPTIMIZED_INVENTORY_UPLOAD_TIME_INTERVAL= "InventoryUploadTimeInterval";
  public static final String OPTIMIZED_INVENTORY_UPLOAD_TAG_COUNT = "InventoryUploadTagCount";
  public static final String IS_OPTIMIZED_INVENTORY = "isOptimizedInventory";
  
  //Changes for Configurable Minimum Battery Percentage to Allow Reader Operations
  public static final String minBatteryPercentForReaderOperations = "minBatteryPercentForReaderOperations";
  public static final String maxBatteryPercentForReaderDisconnect = "maxBatteryPercentForReaderDisconnect";
  
  public static final String EXPECTED_QTY = "expectedQty";
  public static final String EXP_QTY = "expQty";
  public static final String EAN_QTY = "eanQty";
  public static final String PC_DATA = "pcData";
  public static final String EPC = "epc";
  public static final String PRE_EPC = "previousEpc";
  public static final String TID = "tid";
  public static final String RSSI = "rssi";
  public static final String SKU_ID = "skuId";
  public static final String SKU_IDS = "skuIds";
  public static final String SKUS = "skus";
  public static final String TAG_TYPE = "tagType";
  public static final String IS_HARD_TAG = "isHardTag";
  public static final String LOG_ID = "logNo";
  public static final String TRANSACTION_ID = "txId";
  public static final String TRANSACTION_DATE = "txDate";
  public static final String DATE = "date";
  public static final String FIFO_DATE = "fifoDate";
  public static final String FROM = "from";
  public static final String ANTENNA_NO = "antno";
  public static final String IS_UPLOADED = "antno";
  public static final String UPLOAD_RETRY_COUNT = "antno";
  public static final String PHASE = "phase";
  public static final String SHEET1 = "sheet1";
  public static final String ITEMS = "items";
  public static final String DASHBOARD_ITEMS = "dashboardItems";
  public static final String SIZE = "size";
  public static final String COLOR = "color";
  public static final String NAME = "name";
  public static final String CODE = "code";
  public static final String PRODUCT_ID = "productId";
  public static final String QTY = "qty";
  public static final String TOTAL_QTY = "totalQty";
  public static final String TASK_QTY = "taskQty";
  public static final String PICK_QTY = "pickQty";
  public static final String PICKED_QTY = "pickedQty";
  public static final String SESSION_ID = "sessionId";
  public static final String SESSION_USER_ID = "sessionUserId";
  public static final String SESSION_TIME = "sessionTime";
  public static final String SESSION_VALID_TILL = "sessionValidTill";
  public static final String INVENTORY_COUNT = "inventoryCount";
  public static final String ACTIVE_USERS = "activeUsers";
  public static final String SEARCH_DURATION = "searchDuration";
  public static final String SEARCH_LISTS = "lists";
  public static final String SEARCH_LIST_ID = "listId";
  public static final String SEARCH_LIST_TYPE = "listType";//"type";
  public static final String SEARCH_LIST_NAME = "name";
  public static final String ASSORTMENT_CODE = "code";
  public static final String ASSORTMENT_PRIORITY = "priority";
  
  //Changes for isUseDirectionalSearch
  public static final String IS_USE_DIRECTIONAL_SEARCH = "isUseDirectionalSearch";
  
  //Changes for markFoundPercent
  public static final String MARK_FOUND_PERCENT_SER_LIST_BASED = "markFoundPercentLBS";
  public static final String MARK_FOUND_PERCENT_SER_LIST_BASED_NEW = "markFoundPercentLBS";
  public static final String MARK_FOUND_PERCENT_SER_UNENCODED = "markFoundPercentUnencodedSearch";
  public static final String MARK_FOUND_PERCENT_SER_ASSORTMENT = "markFoundPercentAssortmentSearch";
  
  public static final String PRODUCTS = "products";
  public static final String SEARCH_PRODUCTS = "searchProducts";
  public static final String SHORTAGE_PRODUCTS = "shortageProducts";
  public static final String REPLENISHMENT_PRODUCTS = "replenishmentProducts";
  public static final String IS_EOSS_REPLENISHMENT = "isEOSSReplenishment";
  public static final String AGEING_BUCKET = "ageingBucket";
  public static final String AGEING_PRODUCTS = "ageingProducts";
  public static final String IS_ALLOW_DECODE = "isAllowDecoding";
  public static final String IS_ALLOW_DECODE_ON_PICK = "isAllowDecodingOnPick";
  public static final String IS_ALLOW_DECODE_WITHOUT_VERIFY = "isAllowDecodingWithoutVerify";
  public static final String OMNI_UPLOAD_TYPE = "uploadType";
  public static final String IS_DECODED = "isDecoded";
  public static final String IS_STATUS_VERIFIED = "isVerified";
  public static final String IS_VERIFY_DECODE = "isVerifyingForDecode";
  public static final String OMNICHANNEL_PRODUCTS = "omnichannelProducts";
  public static final String EPCS = "EPCs";
  public static final String RFIDS = "rfids";
  public static final String RFIDS1 = "rfids1";
  public static final String TIDS = "tids";
  public static final String PICKED_EPCS = "pickedEPCs";
  public static final String CURRENT_HU_NUMBER = "currentHuNum";
  public static final String OUTWARD_PRODUCTS = "outwardProducts";
  public static final String OFF_RANGE_PRODUCTS = "offRangeProducts";
  public static final String IMAGE = "image";
  public static final String IMG_URL = "imageUrl";
  public static final String DISPLAY_DATA = "displayData";
  public static final String DISPLAY_DATA_DETAILS = "detailsDisplayData";
  public static final String DISPLAY_DATA_DETAILS1 = "detailsdisplayData";
  public static final String LAST_INVENTORY_TAKEN_TIME = "lastInventoryTakenTime";
  public static final String BRAND_INVENTORY_COUNTS = "brandInventoryCounts";
  public static final String CATEGORY_INVENTORY_COUNTS = "categoryInventoryCounts";
  public static final String INVENTORY_COUNTS = "inventoryCounts";
  public static final String FILTERS = "filters";
  public static final String HAS_EANS = "hasEans";
  public static final String IS_MULTI_SELECT = "isMultiSelect";
  public static final String IS_CASCADE = "isCascade";
  public static final String VALID_TILL = "validTill";
  public static final String SIZE_CHART_PRODUCTS = "sizeChartProducts";
  public static final String ENCODE_HISTORY_TAGS = "history";
  public static final String ENCODE_TODAY = "today";
  public static final String AVG_TAGS_ENCODE_PER_DAY = "avgTagsEncodedPerDay";
  public static final String MAX_TAGS_ENCODE_PER_DAY = "maxTagsEncodedPerDay";
  public static final String AVG_ENCODE_TIME_PER_TAG = "avgEncodingTimePerTag";
  public static final String HOURS_SPENT = "hoursSpent";
  public static final String EAN_TYPE = "eanType";
  public static final String IS_ONE_TO_MANY_RELATION = "isOneToManyRelation";
  public static final String IS_BARCODE_BARCODE_RFID = "isBarcodeBarcodeRFID";
  public static final String TARGET = "target";
  public static final String SGTINS = "sgtins";
  public static final String ACCESS_PASSWORD = "accessPassword";
  public static final String ACCESS_PASS = "ap";
  public static final String DI = "displayData";
  public static final String IS_SOLD = "isSold";
  public static final String IS_EAN_SEARCH = "isEANSearch";
  public static final String IS_EPC_SEARCH = "isEPCSearch";
  //Dashboard
  public static final String HEADER = "header";
  public static final String LABEL = "label";
  public static final String VALUE = "value";
  public static final String DASHBOARD_VIEW_TYPE = "type";
  public static final String DASHBOARD_VIEW_TYPE_TOTAL = "Total";
  public static final String DASHBOARD_VIEW_TYPE_TODAY = "Today";
  public static final String COUNT = "count";
  public static final String PERCENT = "percent";
  public static final String PERCENT_LABEL = "percentLabel";
  public static final String IS_UPWARD_ARROW = "isUpwardArrow";
  public static final String STATS = "stats";
  
  //Product fields
  public static final String ARTICLE = "article";
  public static final String ARTICLE_NO = "articleNo";
  public static final String TITLE = "title";
  public static final String SUB_CATEGORY = "subCategory";
  public static final String DESCRIPTION = "description";
  public static final String DIVISION = "division";
  public static final String AGEING_LABEL = "ageingLabel";
  
  //brandwise-zone qty fields
  public static final String SHORTAGE = "shortage";
  
  //inv counts
  public static final String IS_SHOW_UNENCODED_COUNT_IN_INV = "showUnencodedCountInInv";
  public static final String IS_SHOW_ALIEN_COUNT_IN_INV = "showAlienCountInInv";
  public static final String IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV = "showUnencodedAndAlienCountsInInventory";
  public static final String IS_SHOW_UNENCODED_ALIEN_COUNT_IN_BRAND_INV = "showUnencodedAndAlienCountsInBrandInventory";
  
  //inward Trip json
  public static final String K_TRIPS = "trips";
  public static final String K_TRIPS_DATA = "tripsData";
  public static final String K_ARTICLE_DATA = "articleData";
  public static final String K_HUS = "hus";
  public static final String K_HU_DATA = "huData";
  //HU Verify
  public static final String SENDER_NAME = "senderName";
  public static final String SENDER_CODE = "senderCode";
  public static final String RECEIVER_NAME = "receiverName";
  public static final String RECEIVER_CODE = "receiverCode";
  public static final String K_TRIP_STATUS = "tripStatus";
  public static final String K_TRIP_NUMBER = "tripNum";
  public static final String K_TRIP_TYPE = "tripType";
  public static final String K_TRIP_DELIVERY_NUMBER = "deliveryNum";
  public static final String K_LOWER_TOLERANCE = "lowerTolerance";
  public static final String K_UPPER_TOLERANCE = "upperTolerance";
  public static final String K_MIX_TAG_COMPULSION = "mixTagCompulsion";
  public static final String K_MANUAL_BARCODE_COMPULSION = "manualBarCodeCompulsion";
  //upload Trip json Inward
  public static final String SUCCEED_TRIPS = "succeedTrips";
  public static final String FAILED_TRIPS = "failedTrips";
  public static final String REASON = "reason";
  public static final String REMARK = "remark";
  public static final String K_TRIP_ITEM_BARCODE = "itemBarcode";
  public static final String K_TRIP_HU_NUMBER = "huNum";
  public static final String K_TRIP_RECEIVED_QTY = "receivedQty";
  public static final String K_TRIP_HU_STATUS = "huStatus";
  public static final String K_TRIP_HU_REASON = "huReason";
  public static final String K_TRIP_ARTICLE_CODE = "articleCode";
  public static final String K_TRIP_HU_DETAILS = "huDetails";
  
  //outward-tote
  public static final String LABEL_OUTWARD_TOTE_TYPES = "labelOutToteTypes";
  public static final String LABEL_OUTWARD_TYPES = "labelOutTypes";
  public static final String LABEL_TYPES = "labelTypes";
  public static final String DESTINATIONS = "destinations";
  public static final String DESTINATION_SITE = "destSite";
  public static final String DESTINATION_SITE_CODE = "destSiteCode";
  public static final String DESTINATION_SITE_CODES = "destSiteCodes";
  public static final String DESTINATION_CODE = "destinationCode";
  public static final String DESTINATION_CODES = "destinationCodes";
  public static final String SOURCES = "sources";
  public static final String SOURCE_SITE = "srcSite";
  public static final String SOURCE_SITE_CODE = "srcSiteCode";
  public static final String SOURCE_SITE_CODES = "srcSiteCodes";
  public static final String SOURCE_CODE = "sourceCode";
  public static final String SOURCE_CODES = "sourceCodes";
  public static final String OUTWARD_TOTE_EANS = "eans";
  public static final String OUTWARD_TOTE_EAN = "ean";
  public static final String OUTWARD_TOTE_TYPES = "outwardTypes";
  public static final String OUTWARD_TOTE_TYPE = "type";
  public static final String COMPLETED_CARTONS = "CompletedCartons";
  public static final String CARTONS = "cartons";
  public static final String CARTON_NO = "cartonNo";
  public static final String CARTON_NUM = "cartonNum";
  public static final String CARTON_NUMBER = "cartonNumber";
  public static final String BATCH = "batch";
  public static final String BATCHES = "batches";
  public static final String BATCH_ID = "batchId";
  public static final String BATCH_DETAILS = "batchDetails";
  public static final String LIST_ID = "listId";
  public static final String LIST_BATCH_ID = "listBatchId";
  public static final String LIST_REF_BATCH_ID = "listRefBatchId";
  public static final String IS_EMPTY_TOTE_OUTWARD = "IsEmptyToteOutward";
  public static final String STORE_OUTWARD_TYPE_MASTER_ID = "StoreOutwardTypeMasterId";
  public static final String OUTWARD_TYPE_MASTER_ID = "outwardTypeMasterId";
  public static final String TYPE_MASTER_ID = "typeMasterId";
  
  //inward-tote
  public static final String IS_EMPTY_TOTE_INWARD = "IsEmptyToteInward";
  public static final String TOTE_TYPE = "toteType";
  public static final String DELIVERY_CHALLAN = "deliveryChallan";
  public static final String CHALLAN_NO = "ChallanNo";
  public static final String CHALLAN_DETAILS = "ChallanDetails";
  public static final String INWARD_TOTE_EANS = "ToteEans";
  
  //off-range
  public static final String IS_OFF_RANGE = "IsOFFRange";
  
  //Decode Types
  public static final String IS_ALLOW_NON_ENCODED_TAG_PICK_FOR_DECODE = "isAllowNonEncodedTagPickForDecode";
  public static final String IS_SHOW_DECODE_TYPE_SELECTION_DIALOG = "isShowDecodeTypeSelectionDialog";
  public static final String IS_CHECK_PRODUCT_DETAILS_BEFORE_DECODING = "isCheckProductDetailsBeforeEncoding";
  public static final String IS_SHOW_STATIC_DECODE_TYPES_FOR_SELECTION_IF_API_FAILS = "isShowStaticDecodeTypesForSelectionIfApiFails";
  public static final String IS_SAVED_FROM_LOGIN = "isSavedFromLogin";
  public static final String DECODE_TYPES = "decodeTypes";
  public static final String DECODE_TYPE = "decodeType";
  public static final String FIFO_TYPE = "fifoType";
  public static final String IS_FIFO_VALIDATION = "isAgeingValidation";
  public static final String IS_FIFO_VALIDATION_DECODE = "isAgeingValidationInDecode";
  public static final String IS_FIFO_VALIDATION_OUTWARD = "isAgeingValidationInOutward";
  public static final String FIFO_VALIDATION_MODE = "ageingValidationMode";
  public static final String FIFO_VALIDATION_CRITERIA = "ageingValidationCriteria";
  public static final String FIFO_OVERRIDE_REASONS = "fifoOverrideReasons";
  
  //extras
  public static final String NON_PASSWORD_TIDS = "nonPasswordTids";
  public static final String NON_128_BIT_TIDS = "non128BitTids";
  
  //ean config
  public static final String IS_ALLOW_NON_STD_EAN = "isAllowNonStdEan";
  public static final String IS_ALLOW_ALPHANUMERIC_NON_STD_EAN = "isAllowAlphanumericNonStdEan";
  
  //encode config
  public static final String IS_CONFIG_ENC_START = "isAllowConfigForEncoding";
  public static final String IS_CONFIG_ENC_SSW = "isAllowConfigForEncSSW";
  public static final String IS_BULK_ENCODE = "isAllowBulkEncoding";
  public static final String IS_GID = "isUseGID";
  public static final String IS_IDENTIFY_ITEK_TAG_BY_EPC = "identifyITEKTags";
  public static final String IS_IDENTIFY_ITEK_TAG_BY_TID = "isFirstTimeEncode";
  
  //encoding-algorithm-config
  public static final String ENCODE_ALGORITHM_STD = "encodeAlgorithmStd";
  public static final String ENCODE_ALGORITHM_NON_STD = "encodeAlgorithmNonStd";
  
  //EPC validations
  public static final String EPC_HEADER_VALIDATION_REGEX = "epcHeaderValidationRegex";
  public static final String EPC_SERIAL_LENGTH_VALIDATION_REGEX = "epcSerialLengthValidationRegex";
  public static final String EPC_HEADER_STD = "epcHeaderStd";
  public static final String EPC_HEADER_VALIDATION_REGEX_STD = "epcHeaderValidationRegexStd";
  public static final String EPC_SERIAL_LENGTH_VALIDATION_REGEX_STD = "epcSerialLengthValidationRegexStd";
  public static final String EPC_HEADER_NON_STD = "epcHeaderNonStd";
  public static final String EPC_HEADER_VALIDATION_REGEX_NON_STD = "epcHeaderValidationRegexNonStd";
  public static final String EPC_SERIAL_LENGTH_VALIDATION_REGEX_NON_STD = "epcSerialLengthValidationRegexNonStd";
  
  //fifo config flags
  public static final String IS_TID_BASED_COUNT_FOR_ENCODE = "isTidBasedCountForEncode";
  public static final String IS_SHOW_FIFO_CHART = "showFifoChart";
  public static final String IS_SHOW_FIFO_DATE_FOR_ENCODING = "showFIFODateForEncode";
  public static final String IS_SHOW_FIFO_DATE_FOR_INWARD = "showFIFODateForInward";
  public static final String IS_SHOW_FIFO_DATE_FOR_USER_SELECTION = "showFIFODateForUserSelection";
  
  //Than config flags
  public static final String IS_SET_EXTRA_PICK_TIME_FOR_THAN_ENC = "isSetExtraPickTimeForThanEncoding";
  public static final String IS_GET_ORIGINAL_LENGTH_IN_THAN_ENC_FROM_FIELD = "isGetOriginalLengthInEncFromField";
  public static final String IS_GET_ORIGINAL_LENGTH_IN_THAN_ENC_BEFORE_API = "isGetOriginalLengthInThanEncBeforeAPI";
  public static final String IS_CLEAR_ORIGINAL_LENGTH_IN_THAN_ENC_FOR_EACH_TAG = "isClearOriginalLengthInEncForEachTag";
  public static final String IS_ALLOW_ZERO_LENGTH_FOR_THAN_CLOSURE = "isAllowZeroLengthForThanClosure";
  public static final String IS_THAN_CUTTING = "isThanCutting";
  public static final String IS_THAN_CLOSURE = "isThanClosure";
  
  //Than Params
  public static final String LENGTH_ORIGINAL = "originalLength";
  public static final String LENGTH_BALANCE = "balanceLength";
  public static final String LENGTH_OLD = "oldLength";
  public static final String LENGTH_CUTTING = "cuttingLength";
  public static final String LENGTH_CLOSURE = "closureLength";
  public static final String UNIT = "unit";
  public static final String LENGTH_UNIT = "lengthUnit";
  
  //Bluetooth & Printing
  public static final String HAS_BLUETOOTH_CONNECTION_REQUIREMENT = "isUseBlueToothConnection";
  public static final String HAS_BLUETOOTH_PRINTING = "isUseBlueToothPrinting";
  
  //logs
  public static final String SHOW_CRASH_LOGS = "showCrashLogs";
  public static final String SHOW_FILE_LOGS = "showFileLogs";
  public static final String MAX_LOG_FILES = "maxLogFiles";
  public static final String MAX_LOG_FILE_SIZE = "maxLogFileSize";
  
  //Search_Reset_Counter
  public static final String  SEARCH_RESET_COUNTER ="searchResetCounter";
  
  //New Inward/Outward Phase 1
  public static final String CONFIG="config";
  public static final String CONFIG_LABEL="labelConfig";
  public static final String CONFIG_LABELS="labelsConfig";
  public static final String CONFIG_SETTING="settingConfig";
  public static final String CONFIG_SETTINGS="settingsConfig";
  public static final String OPERATION_TYPE="operationType";
  public static final String LABEL_TRIP="labelTrip";
  public static final String LABEL_HU="labelHu";
  public static final String LABEL_ARTICLE="labelArticle";
  public static final String LABEL_SKUID="labelSkuId";
  public static final String IS_USE_TRIP_AS_HU="useTripAsHU";
  public static final String IS_MANUAL_TRIP_NO_ENTRY="isManualTrip";
  public static final String IS_ALLOW_MANUAL_TRIP_NO_ENTRY="isAllowManualTripEntry";
  public static final String IS_ALLOW_MANUAL_HU_ENTRY="isAllowManualHuEntry";
  public static final String IS_TRIP_DEVICE_LOCK="isTripDeviceLock";
  public static final String IS_HU_DEVICE_LOCK="isHuDeviceLock";
  public static final String IS_API_CALL_FOR_REJECT_HU="isAPIBasedRejectHU";
  public static final String IS_ENCODE_TAGS="isEncodeTags";
  public static final String RESTRICT_UNVERIFIED_HU="restrictUnverifiedHu";
  public static final String ALLOW_BARCODE_SCANNING="allowBarcodeScanning";
  public static final String ALLOW_MIX_TAG_TYPE="allowMixTagType";
  public static final String LOWER_TOLERANCE="lowerTolerance";
  public static final String UPPER_TOLERANCE ="upperTolerance";
  public static final String IS_EPC_BASED_CHECK ="rfidReconciliation";//"rfidReconsilation";
  public static final String TRIP_NUMBER = "tripNum";
  public static final String REF_TRIP_NUMBER = "refTripNum";//""refTripNum";//"referenceTripNum";
  public static final String REFERENCE_TRIP_NUMBER = "referenceTripNum";
  public static final String TRIPS_DATA = "tripsData";
  public static final String ARTICLE_DATA = "articleData";
  public static final String HUS = "hus";
  public static final String HU_DATA = "huData";
  public static final String LIST_DATA = "listData";
  public static final String IS_ON_DEMAND_TRIP_HU_LIST = "isOnDemandTripHuList";
  public static final String IS_SHOW_HU_INFO_DIALOG = "isShowHUDetailsDialog";
  public static final String IS_SHOW_TRIP_HU_LIST_DIALOG = "isShowTripHUListDialog";
  public static final String IS_VERIFED_TRIP_NUM = "isVerifiedTripNum";
  public static final String IS_VERIFY_TRIP_NUM = "isVerifyTripNum";
  public static final String EXCEL_TRIP_TYPE = "excelTripType";
  public static final String IS_ALLOW_LOWER_CASE_TRIP_NUMBER = "isAllowLowerCaseTripNumber";
  public static final String SEQUENCE_NUMBER = "seqNumber";

  //Move to Display
  public static final String IS_MOVE_TO_DISPLAY = "isMoveToDisplay";
  public static final String HAS_DISPLAY_MAPPING = "hasDisplayMapping";
  public static final String IS_DISPLAY_ZONE = "isDisplayZone";
  public static final String IS_DISPLAY_MAPPING = "isDisplayMapping";
  public static final String IS_DISPLAY_ZONE_MAPPING = "isDisplayZoneMapping";
  public static final String IS_ACTUAL_TAG_DISPLAY_MAPPING = "isDisplayMappingForActualTags";
  public static final String IS_ACTUAL_TAG_MAPPING_DISPLAY = "isActualTagMappingForDisplayZone";
  public static final String EXCLUDE_INVENTORY_EPCS = "excludeEpcs";
  public static final String IS_CONFIREMED_BY_USER = "isConfirmedByUser";
  
  //theme
  public static final String THEME = "theme";
  public static final String THEME_COLOR = "themeColor";
  public static final String COLOR_THEME = "colorTheme";
  public static final String COLOR_PRIMARY_DARK = "colorPrimaryDark";
  public static final String COLOR_ACCENT = "colorAccent";
  public static final String COLOR_PRIMARY = "colorPrimary";
  public static final String THEME_COLOR_PRIMARY_DARK = "themeColorPrimaryDark";
  public static final String THEME_COLOR_ACCENT = "themeColorAccent";
  public static final String THEME_COLOR_PRIMARY = "themeColorPrimary";

  //inward with serial
  public static final String IS_INW_WITH_SERIAL_NUMBER = "isInwardWithSerialNumber";
  public static final String IS_UPDATE_SERIAL_NUMBER = "isUpdateSerialNumber";
  public static final String IS_ALLOW_UPDATE_SERIAL_NUMBER = "allowUpdate";
  public static final String IS_SERIAL_NUMBER_MANDATORY = "isSerialNumberMandatory";
  public static final String SERIAL = "serial";
  public static final String SERIAL_NO = "serialNo";
  public static final String SERIAL_NUMBER = "serialNumber";

  //Empty Tote DC Outward
  public static final String DC_CODE = "DCCode";
  public static final String LPN_NO = "lpnNo";
  public static final String EPC_EXPECTED_QTY = "epcExpectedQty";
  public static final String EPC_SCAN_QTY = "epcScanQty";
  public static final String IS_COMPLETED = "isCompleted";
  public static final String LIST_TOTE_EANS = "listToteEans";
  public static final String RFID = "rfid";
  public static final String EAN_INFO = "eanInfo";
  public static final String QUANTITY = "Quantity";



}
