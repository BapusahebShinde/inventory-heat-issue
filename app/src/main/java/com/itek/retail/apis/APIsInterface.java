package com.itek.retail.apis;

import com.google.gson.JsonObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * The API interface
 * is used for storing Signature of API Call Methods for All APIs
 * and used in Retrofit API calls (AppCommonMethods->callWebService->invokeMethod)
 */
public interface APIsInterface{
  
  @Streaming
  @GET
  Call<ResponseBody> downloadFileByUrl(@Url String fileUrl);
  
  /**
   * Gets access token.
   *
   * @param requestParams the request params
   * @return the access token
   */
  @FormUrlEncoded
  @POST(URLConstants.GET_ACCESS_TOKEN)
  Call<ResponseBody> getAccessToken(@FieldMap HashMap<String, String> requestParams);
  
  /**
   * checks APK version for update.
   *
   * @param jsonRequest the json request
   * @return the new APK version & url
   */
  @POST(URLConstants.CHECK_FOR_UPDATE)
  Call<ResponseBody> chkforapkupdate(@Body JsonObject jsonRequest);
  
  /**
   * checks APK version for update.
   *
   * @param jsonRequest the json request
   * @return the new APK version & url
   */
  @POST(URLConstants.CHECK_FOR_HARDWARE_UPDATE)
  Call<ResponseBody> checkHardwareAppVersion(@Body JsonObject jsonRequest);
  
  /**
   * Gets store details.
   *
   * @param jsonRequest the json request
   * @return the store details
   */
  @POST(URLConstants.GET_STORE_DETAILS)
  Call<ResponseBody> getStoreDetails(@Body JsonObject jsonRequest);
  
  /**
   * Gets zones.
   *
   * @param jsonRequest the json request
   * @return the zones
   */
  @POST(URLConstants.GET_BRANDS)
  Call<ResponseBody> getBrands(@Body JsonObject jsonRequest);
  
  /**
   * Gets zones.
   *
   * @param jsonRequest the json request
   * @return the zones
   */
  @POST(URLConstants.GET_ZONES)
  Call<ResponseBody> getZones(@Body JsonObject jsonRequest);
  
  /**
   * Update password call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.RESET_PASSWORD)
  Call<ResponseBody> resetPassword(@Body JsonObject jsonRequest);
  
  /**
   * Update password call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPDATE_PASSWORD)
  Call<ResponseBody> updatePassword(@Body JsonObject jsonRequest);
  
  /**
   * Logout call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.LOGOUT)
  Call<ResponseBody> logout(@Body JsonObject jsonRequest);
  
  /**
   * Gets active user count.
   *
   * @param jsonRequest the json request
   * @return the active user count
   */
  @POST(URLConstants.GET_ACTIVE_USERS)
  Call<ResponseBody> getActiveUserCount(@Body JsonObject jsonRequest);
  
  /**
   * Gets encoding dashboard.
   *
   * @param jsonRequest the json request
   * @return the encoding dashboard
   */
  @POST(URLConstants.GET_ENCODING_DASHBOARD)
  Call<ResponseBody> getEncodingDashboard(@Body JsonObject jsonRequest);
  
  /**
   * Gets epc for encoding.
   *
   * @param jsonRequest the json request
   * @return the epc for encoding
   */
  @POST(URLConstants.GET_EPC_FOR_ENCODING)
  Call<ResponseBody> getEpcForEncoding(@Body JsonObject jsonRequest);
  
  /**
   * Gets epc for encode.
   *
   * @param jsonRequest the json request
   * @return the epc for encode
   */
  @POST(URLConstants.GET_EPC_FOR_ENCODE)
  Call<ResponseBody> getEpcForEncode(@Body JsonObject jsonRequest);
  
  /**
   * Gets access password
   *
   * @param jsonRequest the json request
   * @return the access password
   */
  @POST(URLConstants.GET_ACCESS_PWD)
  Call<ResponseBody> getaccesspwd(@Body JsonObject jsonRequest);
  
  /**
   * Upload encoding call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_ENCODING)
  Call<ResponseBody> uploadEncoding(@Body JsonObject jsonRequest);
  
  /**
   * Upload decoding call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_DECODING)
  Call<ResponseBody> uploadDecoding(@Body JsonObject jsonRequest);
  
  /**
   * Upload audit trail logs call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_AUDITTRAILS_LOG)
  Call<ResponseBody> uploadAuditTrailsLog(@Body JsonObject jsonRequest);
  
  /**
   * Gets encoding achievements.
   *
   * @param jsonRequest the json request
   * @return the encoding achievements
   */
  @POST(URLConstants.GET_ENCODING_ACHIEVEMENTS)
  Call<ResponseBody> getEncodingAchievements(@Body JsonObject jsonRequest);
  
  /**
   * Gets encoding verify.
   *
   * @param jsonRequest the json request
   * @return the encoding verify
   */
  @POST(URLConstants.GET_ENCODING_VERIFY)
  Call<ResponseBody> getEncodingVerify(@Body JsonObject jsonRequest);
  
  /**
   * Gets inventory dashboard.
   *
   * @param jsonRequest the json request
   * @return the inventory dashboard
   */
  @POST(URLConstants.GET_INVENTORY_DASHBOARD)
  Call<ResponseBody> getInventoryDashboard(@Body JsonObject jsonRequest);
  
  /**
   * Sets inventory session.
   *
   * @param jsonRequest the json request
   * @return the inventory session
   */
  @POST(URLConstants.SET_SESSION)
  Call<ResponseBody> setInventorySession(@Body JsonObject jsonRequest);
  
  /**
   * Upload inventory session call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_INVENTORY)
  Call<ResponseBody> uploadInventorySession(@Body JsonObject jsonRequest);
  
  /**
   * Gets inventory filters.
   *
   * @param jsonRequest the json request
   * @return the inventory filters
   */
  @POST(URLConstants.GET_INVENTORY_FILTERS)
  Call<ResponseBody> getInventoryFilters(@Body JsonObject jsonRequest);
  
  /**
   * Sets inventory session filters.
   *
   * @param jsonRequest the json request
   * @return the count and eans
   */
  @POST(URLConstants.SET_INVENTORY_FILTERS)
  Call<ResponseBody> setInventoryFilters(@Body JsonObject jsonRequest);
  
  /**
   * Gets brand inventory session filters.
   *
   * @param jsonRequest the json request
   * @return the brand inventory filters
   */
  @POST(URLConstants.GET_BRAND_INVENTORY_FILTERS)
  Call<ResponseBody> getBrandInventoryFilters(@Body JsonObject jsonRequest);
  
  /**
   * Sets brand inventory session filters.
   *
   * @param jsonRequest the json request
   * @return the count and eans
   */
  @POST(URLConstants.SET_BRAND_INVENTORY_FILTERS)
  Call<ResponseBody> setBrandInventoryFilters(@Body JsonObject jsonRequest);
  
  /**
   * Gets stock correction dashboard.
   *
   * @param jsonRequest the json request
   * @return the stock correction dashboard
   */
  @POST(URLConstants.GET_STOCK_CORRECTION_DASHBOARD)
  Call<ResponseBody> getStockCorrectionDashboard(@Body JsonObject jsonRequest);
  
  /**
   * Upload stock correction call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_STOCK_CORRECTION)
  Call<ResponseBody> uploadStockCorrection(@Body JsonObject jsonRequest);
  
  /**
   * Gets product details.
   *
   * @param jsonRequest the json request
   * @return the product details
   */
  @POST(URLConstants.GET_PRODUCT_INFO)
  Call<ResponseBody> getProductDetails(@Body JsonObject jsonRequest);
  
  /**
   * Gets product details by sku.
   *
   * @param jsonRequest the json request
   * @return the product details
   */
  @POST(URLConstants.GET_PRODUCT_INFO_BY_SKU)
  Call<ResponseBody> productinfobysku(@Body JsonObject jsonRequest);
  
  /**
   * Gets size chart.
   *
   * @param jsonRequest the json request
   * @return the size chart
   */
  @POST(URLConstants.GET_SIZE_CHART)
  Call<ResponseBody> getSizeChart(@Body JsonObject jsonRequest);
  
  /**
   * Gets similar styles.
   *
   * @param jsonRequest the json request
   * @return the similar styles
   */
  @POST(URLConstants.GET_SIMILAR_STYLES)
  Call<ResponseBody> getSimilarStyles(@Body JsonObject jsonRequest);
  
  /**
   * Gets omnichannel dashboard.
   *
   * @param jsonRequest the json request
   * @return the omnichannel dashboard
   */
  @POST(URLConstants.GET_OMNICHANNEL_DASHBOARD)
  Call<ResponseBody> getOmnichannelDashboard(@Body JsonObject jsonRequest);
  
  /**
   * Gets omnichannel dashboard.
   *
   * @param jsonRequest the json request
   * @return the omnichannel dashboard
   */
  @POST(URLConstants.GET_OMNICHANNEL_ACHIEVEMENTS)
  Call<ResponseBody> getOmnichannelAchievements(@Body JsonObject jsonRequest);
  
  /**
   * Gets omnichannel list.
   *
   * @param jsonRequest the json request
   * @return the omnichannel list
   */
  @POST(URLConstants.GET_OMNICHANNEL_LIST)
  Call<ResponseBody> getOmnichannelList(@Body JsonObject jsonRequest);
  
  /**
   * Gets omnichannel list details.
   *
   * @param jsonRequest the json request
   * @return the omnichannel list details
   */
  @POST(URLConstants.GET_OMNICHANNEL_LIST_DETAILS)
  Call<ResponseBody> getOmnichannelListDetails(@Body JsonObject jsonRequest);
  
  /**
   * Release omnichannel order/ean.
   *
   * @param jsonRequest the json request
   * @return the release omnichannel order
   */
  @POST(URLConstants.RELEASE_OMNICHANNEL)
  Call<ResponseBody> releaseOmnichannel(@Body JsonObject jsonRequest);
  
  /**
   * verify omnichannel for decode.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.VERIFY_OMNICHANNEL_FOR_DECODE)
  Call<ResponseBody> verifyOmnichannelForDecode(@Body JsonObject jsonRequest);
  
  /**
   * Upload omnichannel.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_OMNICHANNEL)
  Call<ResponseBody> uploadOmnichannel(@Body JsonObject jsonRequest);
  
  /**
   * Gets pick list.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_PICK_LIST)
  Call<ResponseBody> getPickList(@Body JsonObject jsonRequest);
  
  /**
   * Gets pick lists.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_PICK_LISTS)
  Call<ResponseBody> getPickLists(@Body JsonObject jsonRequest);
  
  /**
   * Gets pick list details.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_PICK_LIST_DETAILS)
  Call<ResponseBody> getPickListDetails(@Body JsonObject jsonRequest);
  
  /**
   * Uploads pick list.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_ENCODE)
  Call<ResponseBody> bulkencode(@Body JsonObject jsonRequest);
  
  /**
   * Updates pick list status.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPDATE_PICK_LIST_STATUS)
  Call<ResponseBody> updatePickListStatus(@Body JsonObject jsonRequest);
  
  /**
   * Gets ageing list.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_AGEING_LIST)
  Call<ResponseBody> getAgeingList(@Body JsonObject jsonRequest);
  
  /**
   * Gets assortment list.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_ASSORTMENT_LIST)
  Call<ResponseBody> getassortmentlist(@Body JsonObject jsonRequest);
  
  /**
   * Upload search log call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_SEARCH_LOG)
  Call<ResponseBody> uploadSearchLog(@Body JsonObject jsonRequest);
  
  /**
   * Get unecoded search list call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_UNENCODED_SEARCH_LIST)
  Call<ResponseBody> getUnencodedSearchList(@Body JsonObject jsonRequest);
  
  /**
   * Get alien search list call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_ALIEN_SEARCH_LIST)
  Call<ResponseBody> getAlienSearchList(@Body JsonObject jsonRequest);
  
  /**
   * Get FIFO search list call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_FIFO_SEARCH_LIST)
  Call<ResponseBody> getFIFOList(@Body JsonObject jsonRequest);
  
  /**
   * Gets inward dashboard.
   *
   * @param jsonRequest the json request
   * @return the inward dashboard
   */
  @POST(URLConstants.GET_INWARD_DASHBOARD)
  Call<ResponseBody> getInwardDashboard(@Body JsonObject jsonRequest);
  
  /**
   * Gets inward trip data.
   *
   * @param jsonRequest the json request
   * @return the inward trip data
   */
  @POST(URLConstants.GET_INWARD_TRIP_DATA)
  Call<ResponseBody> getInwardTripData(@Body JsonObject jsonRequest);
  
  /**
   * Verify hu data call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.VERIFY_HU_DATA)
  Call<ResponseBody> verifyHuData(@Body JsonObject jsonRequest);

  /**
   * Get serial data call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_SERIAL_NUMBER)
  Call<ResponseBody> getSerialNumber(@Body JsonObject jsonRequest);
  
  
  /**
   * Verify serial data call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.VERIFY_PRODUCT_SERIAL)
  Call<ResponseBody> verifyProductSerial(@Body JsonObject jsonRequest);
  
  /**
   * Save serial data call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.SAVE_SERIAL_NUMBER)
  Call<ResponseBody> saveSerialNumber(@Body JsonObject jsonRequest);
  
  /**
   * Gets inward hu data.
   *
   * @param jsonRequest the json request
   * @return the inward hu data
   */
  @POST(URLConstants.GET_INWARD_HU_DATA)
  Call<ResponseBody> getInwardHuData(@Body JsonObject jsonRequest);
  
  /**
   * Upload inward call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_INWARD)
  Call<ResponseBody> uploadInward(@Body JsonObject jsonRequest);
  
  /**
   * Gets replenish dashboard.
   *
   * @param jsonRequest the json request
   * @return the replenish dashboard
   */
  @POST(URLConstants.GET_REPLENISHMENT_DASHBOARD)
  Call<ResponseBody> getReplenishDashboard(@Body JsonObject jsonRequest);
  
  /**
   * Gets replenishment list.
   *
   * @param jsonRequest the json request
   * @return the replenishment list
   */
  @POST(URLConstants.GET_REPLENISHMENT_LIST)
  Call<ResponseBody> getReplenishmentList(@Body JsonObject jsonRequest);
  
  /**
   * Gets movement dashboard.
   *
   * @param jsonRequest the json request
   * @return the movement dashboard
   */
  @POST(URLConstants.GET_MOVEMENT_DASHBOARD)
  Call<ResponseBody> getMovementDashboard(@Body JsonObject jsonRequest);
  
  /**
   * Movement upload call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_MOVEMENT)
  Call<ResponseBody> movementUpload(@Body JsonObject jsonRequest);
  
  /**
   * Gets outward dashboard.
   *
   * @param jsonRequest the json request
   * @return the outward dashboard
   */
  @POST(URLConstants.GET_OUTWARD_DASHBOARD)
  Call<ResponseBody> getOutwardDashboard(@Body JsonObject jsonRequest);
  
  /**
   * Gets outward trip data.
   *
   * @param jsonRequest the json request
   * @return the outward trip data
   */
  @POST(URLConstants.GET_OUTWARD_TRIP_DATA)
  Call<ResponseBody> getOutwardTripData(@Body JsonObject jsonRequest);
  
  /**
   * Gets outward pick list.
   *
   * @param jsonRequest the json request
   * @return the outward pick list
   */
  @POST(URLConstants.GET_OUTWARD_PICK_LIST)
  Call<ResponseBody> getOutwardPickList(@Body JsonObject jsonRequest);
  
  /**
   * Gets outward pick list details.
   *
   * @param jsonRequest the json request
   * @return the outward pick list details
   */
  @POST(URLConstants.GET_OUTWARD_PICK_LIST_DETAILS)
  Call<ResponseBody> getOutwardPickListDetails(@Body JsonObject jsonRequest);
  
  /**
   * Upload outward pick data call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_OUTWARD_PICK)
  Call<ResponseBody> uploadOutwardPickData(@Body JsonObject jsonRequest);
  
  /**
   * Upload outward call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_OUTWARD)
  Call<ResponseBody> uploadOutward(@Body JsonObject jsonRequest);
  
  /**
   * Get outward tote data call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_OUTWARD_TOTE_DATA)
  Call<ResponseBody> getOutwardToteData(@Body JsonObject jsonRequest);
  
  /**
   * Upload outward tote data call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_OUTWARD_TOTE_DATA)
  Call<ResponseBody> uploadOutwardToteData(@Body JsonObject jsonRequest);


  /**
   * Get lpn status for outward call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_LPN_STATUS_FOR_OUTWARD)
  Call<ResponseBody> getLPNStatusForOutward(@Body JsonObject jsonRequest);

  /**
   * Upload lpn for outward call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_LPN_FOR_OUTWARD)
  Call<ResponseBody> uploadLPNForOutward(@Body JsonObject jsonRequest);

  
  /**
   * Get outward tote eans call.
   *
   * @return the call
   */
  @GET(URLConstants.GET_OUTWARD_TOTE_EANS)
  Call<ResponseBody> getToteMaster();
  
  /**
   * Get outward types call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_OUTWARD_TYPES)
  Call<ResponseBody> getDestinationsAndOutwardTypeList(@Body JsonObject jsonRequest);
  
  /**
   * Get outward batch id call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_OUTWARD_BATCH_ID)
  Call<ResponseBody> getBatchIdForStoreOutward(@Body JsonObject jsonRequest);
  
  /**
   * Upload outward carton data call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_OUTWARD_CARTON_DATA)
  Call<ResponseBody> uploadOutwardCartonData(@Body JsonObject jsonRequest);
  
  /**
   * Complete Batch for Outward call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.COMPLETE_OUTWARD_BATCH_ID)
  Call<ResponseBody> completeBatchIdForStoreOutward(@Body JsonObject jsonRequest);
  
  
  /**
   * Get Off Range Products call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_OFF_RANGE_PRODUCTS)
  Call<ResponseBody> getOffRangeProducts(@Body JsonObject jsonRequest);
  
  
  /**
   * validate product age for decode call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.VALIDATE_PRODUCT_AGE_FOR_DECODE)
  Call<ResponseBody> validateProductAgeForDecode(@Body JsonObject jsonRequest);
  
  /**
   * Get Decode Types call.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_DECODE_TYPES)
  Call<ResponseBody> getDecodeTypes(@Body JsonObject jsonRequest);
  
  /**
   * get mapped ean.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_MAPPED_EAN)
  Call<ResponseBody> getMappedEan(@Body JsonObject jsonRequest);
  
  /**
   * get unmapped ean.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_UNMAPPED_EAN)
  Call<ResponseBody> getUnmappedEan(@Body JsonObject jsonRequest);
  
  /**
   * get scancount.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.SCAN_COUNT)
  Call<ResponseBody> scancount(@Body JsonObject jsonRequest);
  
  
  /**
   * get epc for encoding than.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_EPC_FOR_ENCODING_THAN)
  Call<ResponseBody> getEpcForThanEncoding(@Body JsonObject jsonRequest);
  
  /**
   * upload encoding than.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_ENCODING_THAN)
  Call<ResponseBody> uploadThanEncoding(@Body JsonObject jsonRequest);
  
  /**
   * get product info than.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_PRODUCT_INFO_THAN)
  Call<ResponseBody> getThanDetails(@Body JsonObject jsonRequest);
  
  /**
   * upload cutting than.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_CUTTING_THAN)
  Call<ResponseBody> uploadThanCutting(@Body JsonObject jsonRequest);
  
  /**
   * upload closure than.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_CLOSURE_THAN)
  Call<ResponseBody> uploadThanClosure(@Body JsonObject jsonRequest);
  
  /**
   * Get challan detail.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_CHALLAN_DETAILS)
  Call<ResponseBody> getChallanDetail(@Body JsonObject jsonRequest);
  
  /**
   * Upload challan detail.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_CHALLAN_DETAILS)
  Call<ResponseBody> uploadChallanDetail(@Body JsonObject jsonRequest);
  
  
  /**
   * get IO configuration.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_IO_CONFIGURATION)
  Call<ResponseBody> getIOConfiguration(@Body JsonObject jsonRequest);
  
  /**
   * get supply chain types.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_SUPPLY_CHAIN_TYPES)
  Call<ResponseBody> getSupplyChainTypeMasterList(@Body JsonObject jsonRequest);
  
  /**
   * get pending trips list.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_TRIPS_DATA)
  Call<ResponseBody> getPendingTripsList(@Body JsonObject jsonRequest);
  
  /**
   * get trip details summary
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_HU_DATA)
  Call<ResponseBody> getTripDetailsSummary(@Body JsonObject jsonRequest);
  
  
  /**
   * get hu details.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_HU_DETAILS)
  Call<ResponseBody> getTripHuDetails(@Body JsonObject jsonRequest);
  
  /**
   * get trip hu count
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_TRIP_HU_COUNT)
  Call<ResponseBody> getTripHuCount(@Body JsonObject jsonRequest);
  
  /**
   * verify manual trip num.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.VERIFY_MANUAL_TRIP_NUM)
  Call<ResponseBody> verifyManualTripNum(@Body JsonObject jsonRequest);
  
  /**
   * check trip num.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.CHECK_TRIP_NUM)
  Call<ResponseBody> checkTripNumberExists(@Body JsonObject jsonRequest);
  
  /**
   * update trip ref num.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPDATE_REFERENCE_TRIP_NUM)
  Call<ResponseBody> updateTripReferenceNum(@Body JsonObject jsonRequest);
  
  /**
   * release trip num.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.RELEASE_TRIP)
  Call<ResponseBody> releaseTripNum(@Body JsonObject jsonRequest);
  
  /**
   * release HU.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.RELEASE_HU)
  Call<ResponseBody> releaseHU(@Body JsonObject jsonRequest);
  
  /**
   * accept HU.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.ACCEPT_HU)
  Call<ResponseBody> acceptHU(@Body JsonObject jsonRequest);
  
  /**
   * reject HU.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.REJECT_HU)
  Call<ResponseBody> rejectHU(@Body JsonObject jsonRequest);
  
  /**
   * complete Trip.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.COMPLETE_TRIP)
  Call<ResponseBody> completeTripStatus(@Body JsonObject jsonRequest);
  
  
  /**
   * get replenishment batch list.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_REPLENISHMENT_BATCHES)
  Call<ResponseBody> getReplenishmentBatchList(@Body JsonObject jsonRequest);
  
  /**
   * get replenishment batch details.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_REPLENISHMENT_BATCH_DETAILS)
  Call<ResponseBody> getReplenishmentBatchDetails(@Body JsonObject jsonRequest);
  
  /**
   * upload replenishment.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_REPLENISHMENT)
  Call<ResponseBody> uploadReplenishmentData(@Body JsonObject jsonRequest);
  
  /**
   * Gets excel based search list.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.GET_EXCEL_SEARCH_LIST)
  Call<ResponseBody> getExcelBasedSearchList(@Body JsonObject jsonRequest);
  
  
  /**
   * Updates excel based search item as found.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPDATE_EXCEL_SEARCH_ITEM_FOUND)
  Call<ResponseBody> updateExcelBasedSearchItemFound(@Body JsonObject jsonRequest);
  
  /**
   * Uploads movement to display zone.
   *
   * @param jsonRequest the json request
   * @return the call
   */
  @POST(URLConstants.UPLOAD_MOVEMENT_DISPLAY)
  Call<ResponseBody> uploadMoveToDisplay(@Body JsonObject jsonRequest);
}