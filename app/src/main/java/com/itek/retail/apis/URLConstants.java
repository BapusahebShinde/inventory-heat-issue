package com.itek.retail.apis;

import static com.itek.retail.common.AppCommonMethods.isDebugApp;

/**
 * The Url constants file stores constants/final values for API Name/URL.
 * used in while calling API.
 */
public class URLConstants{
  
  //timeout
  public static final int TIME_OUT = isDebugApp?30:90;
  //Token
  public static final String GET_ACCESS_TOKEN = "token";
  //old APIs
  public static final String CHECK_FOR_UPDATE = "chkforapkupdate";
  public static final String CHECK_FOR_HARDWARE_UPDATE = "checkHardwareAppVersion";
  //Login/Change Password/Logout
  public static final String GET_STORE_DETAILS = "getStoreDetails";
  public static final String GET_ZONES = "getZones";
  public static final String GET_BRANDS = "getBrands";
  public static final String GET_CATEGORIES = "getCategories";
  public static final String RESET_PASSWORD = "resetPassword";
  public static final String UPDATE_PASSWORD = "updatePassword";
  public static final String LOGOUT = "logout";
  //ActiveUsers
  public static final String GET_ACTIVE_USERS = "getActiveUserCount";
  //SetSession
  public static final String SET_SESSION = "setInventorySession";
  //Encoding
  public static final String GET_ENCODING_DASHBOARD = "getEncodingDashboard";
  public static final String GET_ENCODING_ACHIEVEMENTS = "getEncodingAchievements";
  public static final String GET_ENCODING_VERIFY = "getEncodingVerify";
  public static final String GET_ACCESS_PWD = "getaccesspwd";
  public static final String GET_EPC_FOR_ENCODE = "getEpcForEncode";
  public static final String GET_EPC_FOR_ENCODING = "getEpcForEncoding";
  public static final String UPLOAD_ENCODING = "uploadEncoding";
  public static final String UPLOAD_ENCODE = "bulkencode";
  //Scan Count
  public static final String SCAN_COUNT = "scancount";
  //Inventory
  public static final String GET_INVENTORY_DASHBOARD = "getInventoryDashboard";
  public static final String UPLOAD_INVENTORY = "uploadInventorySession";
  //Brand Inventory
  public static final String GET_BRAND_INVENTORY_FILTERS = "getBrandInventoryFilters";
  public static final String SET_BRAND_INVENTORY_FILTERS = "setBrandInventoryFilters";
  //Filtered Inventory
  public static final String GET_INVENTORY_FILTERS = "getInventoryFilters";
  public static final String SET_INVENTORY_FILTERS = "getEansByInventoryFilters";//""setInventoryFilters";
  //Stock Correction
  public static final String GET_STOCK_CORRECTION_DASHBOARD = "getStockCorrectionDashboard";
  public static final String UPLOAD_STOCK_CORRECTION = "uploadInventorySession";
  //Search/Info
  public static final String GET_PRODUCT_INFO = "getProductDetails";
  public static final String GET_PRODUCT_INFO_BY_SKU = "productinfobysku";
  public static final String GET_SIZE_CHART = "getSizeChart";
  public static final String GET_SIMILAR_STYLES = "getSimilarStyles";
  //List Based Search
  public static final String GET_PICK_LISTS = "getPickLists";
  public static final String GET_PICK_LIST_DETAILS = "getPickListDetails";
  public static final String GET_PICK_LIST = "getPickList";
  public static final String UPDATE_PICK_LIST_STATUS = "updatePickListStatus";
  //public static final String UPLOAD_PICK_LIST = "uploadPickListData";
  //public static final String UPLOAD_PICK_LIST = "bulkencode";
  //Ageing Search
  public static final String GET_AGEING_LIST = "getAgeingList";
  //Assortment Search
  public static final String GET_ASSORTMENT_LIST = "getassortmentlist";
  //Unencoded Search
  public static final String GET_UNENCODED_SEARCH_LIST = "getUnencodedSearchList";
  //Alien Search
  public static final String GET_ALIEN_SEARCH_LIST = "getAlienSearchList";
  //Omnichannel
  public static final String GET_OMNICHANNEL_DASHBOARD = "getOmnichannelDashboard";
  public static final String GET_OMNICHANNEL_ACHIEVEMENTS = "getOmnichannelAchievements";
  public static final String GET_OMNICHANNEL_LIST = "getOmnichannelList";
  public static final String GET_OMNICHANNEL_LIST_DETAILS = "getOmnichannelListDetails";
  public static final String UPLOAD_OMNICHANNEL = "uploadOmnichannel";
  public static final String VERIFY_OMNICHANNEL_FOR_DECODE = "verifyOmnichannelForDecode";
  public static final String RELEASE_OMNICHANNEL = "releaseOmnichannel";
  //FIFO Search
  public static final String GET_FIFO_SEARCH_LIST = "getFIFOList";
  //Search Logs
  public static final String UPLOAD_SEARCH_LOG = "uploadSearchLog";
  //AuditTrails Logs
  public static final String UPLOAD_AUDITTRAILS_LOG = "uploadAuditTrailsLog";
  //Inward
  public static final String GET_INWARD_DASHBOARD = "getInwardDashboard";
  public static final String VERIFY_HU_DATA = "verifyHuData";
  public static final String GET_INWARD_TRIP_DATA = "getInwardTripData";
  public static final String GET_INWARD_HU_DATA = "getInwardHuData";
  public static final String UPLOAD_INWARD = "uploadInward";
  //Serial
  public static final String GET_SERIAL_NUMBER="getSerialNumber";
  public static final String VERIFY_PRODUCT_SERIAL = "verifyProductSerial";
  public static final String SAVE_SERIAL_NUMBER="saveSerialNumber";
  //Movement
  public static final String GET_MOVEMENT_DASHBOARD = "getMovementDashboard";
  public static final String UPLOAD_MOVEMENT = "movementUpload";
  public static final String GET_MAPPED_EAN = "getMappedEan";
  public static final String GET_UNMAPPED_EAN = "getUnmappedEan";
  //Replenishment
  public static final String GET_REPLENISHMENT_DASHBOARD = "getReplenishDashboard";
  public static final String GET_REPLENISHMENT_LIST = "getReplenishmentList";
  //Outward
  public static final String GET_OUTWARD_DASHBOARD = "getOutwardDashboard";
  public static final String GET_OUTWARD_TRIP_DATA = "getOutwardTripData";
  public static final String GET_OUTWARD_PICK_LIST = "getOutwardPickList";
  public static final String GET_OUTWARD_PICK_LIST_DETAILS = "getOutwardPickListDetails";
  public static final String UPLOAD_OUTWARD_PICK = "uploadOutwardPickData";
  public static final String UPLOAD_OUTWARD = "uploadOutward";
  
  //Outward Tote
  public static final String GET_OUTWARD_TOTE_DATA = "getOutwardToteData";
  public static final String UPLOAD_OUTWARD_TOTE_DATA = "uploadOutwardToteData";

  //Empty Outward Tote DC
  public static final String GET_LPN_STATUS_FOR_OUTWARD = "getLPNStatusForOutward";
  public static final String UPLOAD_LPN_FOR_OUTWARD = "uploadLPNForOutward";

  //Empty Tote Outward & Off-Range Outward
  public static final String GET_OUTWARD_TOTE_EANS = "getToteMaster";
  public static final String GET_OUTWARD_TYPES = "getDestinationsAndOutwardTypeList";
  public static final String GET_OUTWARD_BATCH_ID = "getBatchIdForStoreOutward";
  public static final String UPLOAD_OUTWARD_CARTON_DATA = "uploadOutwardCartonData";
  public static final String COMPLETE_OUTWARD_BATCH_ID = "completeBatchIdForStoreOutward";
  
  //Off-Range Outward
  public static final String GET_OFF_RANGE_PRODUCTS = "getOffRangeProducts";
  
  //Decode
  public static final String GET_DECODE_TYPES = "getDecodeTypes";
  public static final String VALIDATE_PRODUCT_AGE_FOR_DECODE = "validateProductAgeForDecode";
  public static final String UPLOAD_DECODING = "uploadDecoding";
  
  //THAN (LULU)
  public static final String GET_EPC_FOR_ENCODING_THAN= "getEpcForThanEncoding";
  public static final String UPLOAD_ENCODING_THAN= "uploadThanEncoding";
  public static final String GET_PRODUCT_INFO_THAN= "getThanDetails";
  public static final String UPLOAD_CUTTING_THAN= "uploadThanCutting";
  public static final String UPLOAD_CLOSURE_THAN= "uploadThanClosure";
  
  //Empty Tote Inward
  public static final String GET_CHALLAN_DETAILS="getChallanDetail";
  public static final String UPLOAD_CHALLAN_DETAILS="uploadChallanDetail";
  
  //New Inward/Outward (Phase1)
  public static final String GET_IO_CONFIGURATION = "getIOConfiguration";
  public static final String GET_SUPPLY_CHAIN_TYPES = "getSupplyChainTypeMasterList";
  public static final String GET_TRIPS_DATA = "getPendingTripsList";
  public static final String GET_HU_DATA = "getTripDetailsSummary";
  public static final String GET_HU_DETAILS = "getTripHuDetails";
  public static final String GET_TRIP_HU_COUNT = "getTripHuCount";
  public static final String VERIFY_MANUAL_TRIP_NUM = "verifyManualTripNum";
  public static final String CHECK_TRIP_NUM = "checkTripNumberExists";
  public static final String UPDATE_REFERENCE_TRIP_NUM = "updateTripReferenceNum";
  public static final String RELEASE_TRIP = "releaseTripNum";
  public static final String RELEASE_HU = "releaseHU";
  public static final String ACCEPT_HU = "acceptHU";
  public static final String REJECT_HU = "rejectHU";
  public static final String COMPLETE_TRIP = "completeTripStatus";
  
  //On-Demand Replenishment
  public static final String GET_REPLENISHMENT_BATCHES = "getReplenishmentBatchList";
  public static final String GET_REPLENISHMENT_BATCH_DETAILS = "getReplenishmentBatchDetails";
  public static final String UPLOAD_REPLENISHMENT = "uploadReplenishmentData";
  
  //Excel Based Search
  public static final String GET_EXCEL_SEARCH_LIST = "getExcelBasedSearchList";
  public static final String UPDATE_EXCEL_SEARCH_ITEM_FOUND = "updateExcelBasedSearchItemFound";
  
  //
  public static final String UPLOAD_MOVEMENT_DISPLAY = "uploadMoveToDisplay";
}
