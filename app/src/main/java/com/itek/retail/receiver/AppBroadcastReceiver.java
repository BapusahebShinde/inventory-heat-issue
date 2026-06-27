package com.itek.retail.receiver;

import static com.itek.retail.apis.URLConstants.UPLOAD_OUTWARD_CARTON_DATA;
import static com.itek.retail.common.AppCommonMethods.SERVER_DATE_TIME_FORMAT;
import static com.itek.retail.common.AppCommonMethods.batteryPercent;
import static com.itek.retail.common.AppCommonMethods.callWebService;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isDCApp;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isSaveCompletedCartonAfterUpload;
import static com.itek.retail.common.AppCommonMethods.isUpdateUploadStatusBasedOnTID;
import static com.itek.retail.common.AppCommonMethods.isUse24LengthTIDForUpload;
import static com.itek.retail.common.AppCommonMethods.isValidUrl;
import static com.itek.retail.common.AppCommonMethods.mainActivity;
import static com.itek.retail.common.AppCommonMethods.saveLimitForCompletedCartons;
import static com.itek.retail.common.AppCommonMethods.showLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.itek.retail.BuildConfig;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.model.AuditTrailsLog;
import com.itek.retail.model.EpcTid;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.SearchLog;
import com.itek.retail.model.UploadInventory;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.decoding.DecodingStartFragment;
import com.itek.retail.ui.encoding.EncodingStartFragment;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.inventory.InventoryAddFragment;
import com.itek.retail.ui.inventory.InventoryBrandFragment;
import com.itek.retail.ui.inventory.InventoryFilterFragment;
import com.itek.retail.ui.inventory.InventoryStartFragment;
import com.itek.retail.ui.outward.offrange.OffRangeMainFragment;
import com.itek.retail.ui.search.fifo.SearchFIFOFragment;
import com.itek.retail.ui.search.fifo.SearchFIFOStartFragment;
import com.itek.retail.ui.search.listsearch.SearchListFragment;
import com.itek.retail.ui.search.listsearch.SearchListStartFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelListDetailsFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelStartFragment;
import com.itek.retail.ui.than.ThanEncodingFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The App broadcast receiver
 * used in Landing & Main Activity Screens
 * as Registered Intent Broadcast Receiver
 * for Multiple Intent Actions such as TIME_TICK, CONNECTIVITY_CHANGE, BATTERY_CHANGED
 */
public class AppBroadcastReceiver extends BroadcastReceiver {

    public static boolean isUploadWrittenTagsRunning = false;
    Context context;

    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;
        final String intentAction = intent != null ? chkNull(intent.getAction(), "") : "";
        if (intent != null && isNonEmpty(intentAction)) {
            switch (intentAction) {
                case Intent.ACTION_TIME_TICK:
                    Calendar cc = Calendar.getInstance();
                    //uploadWrittenInventoryTags();//Call Every Minute
                    if (SharedPrefManager.getMin() < 0 || ((SharedPrefManager.getMin() > cc.get(Calendar.MINUTE) ? SharedPrefManager.getMin() + 60 : SharedPrefManager.getMin()) - cc.get(Calendar.MINUTE) >= 5)) { //call Every 5 min
                        uploadSavedCartons();
                        SharedPrefManager.setMin(cc.get(Calendar.MINUTE));
                    }
                    if (SharedPrefManager.getHour() < 0 || SharedPrefManager.getHour() != cc.get(Calendar.HOUR) || (SharedPrefManager.getHour() == cc.get(Calendar.HOUR) && cc.get(Calendar.MINUTE) == 30 /*&& cc.get(Calendar.MINUTE)!=SharedPrefManager.getMin()*/)) {
                        //showLog(intentAction, "time:" + new SimpleDateFormat(AppCommonMethods.DATE_TIME_FORMAT).format(cc.getTime()));
                        SharedPrefManager.setHour(cc.get(Calendar.HOUR));
                        //SharedPrefManager.setMin(cc.get(Calendar.MINUTE));
                        Integer delCount = AppDatabase.getNotificationDao(context).deleteExpired(new SimpleDateFormat(SERVER_DATE_TIME_FORMAT).format(cc.getTime()));
                        showLog("deletedNotifications", "" + (chkNull(delCount, 0)));
                        uploadSearchLogs();
                        //uploadAuditTrailsLogs();
                        uploadWrittenInventoryTags();
                        checkVersionUpdates();
                    }
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    int batteryPercentage = intent.getIntExtra("level", 0);
                    batteryPercent = intent.getIntExtra("level", 0) + "%";
                    SharedPrefManager.setBatteryChargedPercent(batteryPercentage);
                    break;
                case Intent.ACTION_BATTERY_LOW:
                    showLog("batteryPercent_low", batteryPercent, true);
                    if (context != null && context instanceof CommonActivity)
                        ((CommonActivity) context).showCustomErrDialog(String.format(context.getString(R.string.err_device_battery_low), batteryPercent));
                    else if (mainActivity != null && !mainActivity.isFinishing())
                        mainActivity.showCustomErrDialog(String.format(context.getString(R.string.err_device_battery_low), batteryPercent));
                    showLog(intentAction, batteryPercent);
                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    if (AppCommonMethods.isInternetConnected(context, false, false)) {
                        //network connected perform any pending tasks related to N/W (like autoSync)
                        uploadSearchLogs();
                        //uploadAuditTrailsLogs();
                        uploadWrittenInventoryTags();
                        uploadSavedCartons();
                    } else {
                        //network not connected
                    }
                    break;
                case LocationManager.MODE_CHANGED_ACTION:
                    AppCommonMethods.checkLocationOn(context);
                    break;
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Check Version Updates.
     */
    public void checkVersionUpdates(Context context) {
        if (context != null) {
            this.context = context;
            checkVersionUpdates();
        }
    }

    private void checkVersionUpdates() {
        if (!AppCommonMethods.isCheckVersionUpdates) return;
        Log.e("checkVersionUpdates", "checkVersionUpdates");
        try {
            JSONObject request = new JSONObject();
            request.put(ParamConstants.VERSION, BuildConfig.VERSION_NAME);
            callWebService(context, this, URLConstants.CHECK_FOR_HARDWARE_UPDATE/*URLConstants.CHECK_FOR_UPDATE*/, request, null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Upload search logs.
     */
    public void uploadSearchLogs(Context context) {
        if (context != null) {
            this.context = context;
            uploadSearchLogs();
        }
    }

    /**
     * Upload AuditTrails logs.
     */
    public void uploadAuditTrailsLogs(Context context) {
        if (context != null) {
            this.context = context;
            uploadAuditTrailsLogs();
        }
    }

    /**
     * Upload search logs.
     */
    private void uploadSearchLogs() {
        final List<SearchLog> listSearchLogs = AppDatabase.getSearchLogDao(context).getAll();
        if (isNonEmpty(listSearchLogs)) {
            new Handler().post(() -> {
                try {
                    final ArrayList<Integer> listSearchLogIds = new ArrayList<>(0);
                    final JSONObject requestParams = new JSONObject();
                    requestParams.put(ParamConstants.NAME, "SearchLog");
                    requestParams.put(ParamConstants.CODE, 402);
                    JSONArray js = new JSONArray();
                    for (SearchLog log : listSearchLogs) {
                        if (log != null) {
                            listSearchLogIds.add(log.logNo);
                            final JSONObject dataObject = log.toJson();
                            if (dataObject != null && chkNull(dataObject.toString(), "").length() > 2)
                                js.put(dataObject);
                        }
                    }
                    requestParams.put(ParamConstants.ITEMS, js);
                    Bundle args = new Bundle();
                    args.putIntegerArrayList(AppConstants.LOG_IDS, listSearchLogIds);
                    callWebService(context, AppBroadcastReceiver.this, URLConstants.UPLOAD_SEARCH_LOG, requestParams, args, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Upload AuditTrails logs.
     */
    private void uploadAuditTrailsLogs() {
        final List<AuditTrailsLog> listAuditTrailsLogs = AppDatabase.getAuditTrailsDao(context).getAll();
        if (isNonEmpty(listAuditTrailsLogs)) {
            new Handler().post(() -> {
                try {
                    final ArrayList<Integer> listAuditTrailsLogsIds = new ArrayList<>(0);
                    final JSONObject requestParams = new JSONObject();
                    requestParams.put(ParamConstants.NAME, "AuditTrailsLog");
                    requestParams.put(ParamConstants.CODE, 402);
                    JSONArray js = new JSONArray();
                    for (AuditTrailsLog log : listAuditTrailsLogs) {
                        if (log != null) {
                            listAuditTrailsLogsIds.add(log.logNo);
                            final JSONObject dataObject = log.toJson();
                            if (dataObject != null && chkNull(dataObject.toString(), "").length() > 2)
                                js.put(dataObject);
                        }
                    }
                    requestParams.put(ParamConstants.ITEMS, js);
                    Bundle args = new Bundle();
                    args.putIntegerArrayList(AppConstants.LOG_IDS, listAuditTrailsLogsIds);
                    showLog("args: ", "" + args);
                    callWebService(context, AppBroadcastReceiver.this, URLConstants.UPLOAD_AUDITTRAILS_LOG, requestParams, args, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Upload written (i.e. Encoded/Decoded) inventory tags.
     */
    public void uploadWrittenInventoryTags(Context context) {
        uploadWrittenInventoryTags(context, false);
    }

    public void uploadWrittenInventoryTags(Context context, final boolean isForceUpload) {
        if (context != null) {
            this.context = context;
            uploadWrittenInventoryTags(isForceUpload);
        }
    }

    /**
     * Upload written (i.e. Encoded/Decoded) inventory tags.
     */
    private void uploadWrittenInventoryTags() {
        uploadWrittenInventoryTags(false);
    }

    private void uploadWrittenInventoryTags(final boolean isForceUpload) {
        final String currentFragmentClassName = context != null && context instanceof MainActivity ? ((MainActivity) context).currentFragmentClassName : "";
        final UploadInventoryDao uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
        showLog("isUploadWrittenTagsRunning1", "" + isUploadWrittenTagsRunning);
        //TODO write common method instead of this if ladder (Params -> SessionType, API Name/URL , Fragments to be compared

        //Encoding
        commonUploadInventoryWrittenTags(
                uploadInventoryDao,
                AppCommonMethods.SessionType.ENCODING,
                isDCApp ? URLConstants.UPLOAD_ENCODE : URLConstants.UPLOAD_ENCODING,
                isForceUpload,
                currentFragmentClassName,
                new String[]{EncodingStartFragment.class.getSimpleName()},
                AppConstants.ENCODE
        );

        //Than Encoding
        commonUploadInventoryWrittenTags(
                uploadInventoryDao,
                AppCommonMethods.SessionType.ENCODING_THAN,
                URLConstants.UPLOAD_ENCODING_THAN,
                isForceUpload,
                currentFragmentClassName,
                new String[]{ThanEncodingFragment.class.getSimpleName()},
                AppConstants.ENCODE
        );

        //Decoding
        commonUploadInventoryWrittenTags(
                uploadInventoryDao,
                AppCommonMethods.SessionType.DECODING,
                isDCApp ? URLConstants.UPLOAD_ENCODE : URLConstants.UPLOAD_DECODING,
                isForceUpload,
                currentFragmentClassName,
                new String[]{DecodingStartFragment.class.getSimpleName()},
                AppConstants.DECODE
        );

        //OmniChannel
        commonUploadInventoryWrittenTags(
                uploadInventoryDao,
                AppCommonMethods.SessionType.OMNICHANNEL,
                URLConstants.UPLOAD_OMNICHANNEL,
                isForceUpload,
                currentFragmentClassName,
                new String[]{OmniChannelStartFragment.class.getSimpleName(), OmniChannelListDetailsFragment.class.getSimpleName()},
                AppConstants.OMNICHANNEL
        );

        //Search Fifo
        commonUploadInventoryWrittenTags(
                uploadInventoryDao,
                AppCommonMethods.SessionType.SEARCH_FIFO,
                URLConstants.UPLOAD_DECODING,
                isForceUpload,
                currentFragmentClassName,
                new String[]{SearchFIFOStartFragment.class.getSimpleName(), SearchFIFOFragment.class.getSimpleName()},
                null
        );

        //Search List
        commonUploadInventoryWrittenTags(
                uploadInventoryDao,
                AppCommonMethods.SessionType.SEARCH_LIST,
                URLConstants.UPLOAD_ENCODE,
                isForceUpload,
                currentFragmentClassName,
                new String[]{SearchListFragment.class.getSimpleName(), SearchListStartFragment.class.getSimpleName()},
                AppConstants.DECODE
        );
    }

    /**
     * Handles uploading of inventory written tags (either ENCODING/DECODING/other sessions)
     * to the server in both normal and DCApp modes.
     * <p>
     * This method prepares the request payload with session details, inventory data,
     * and EPC/TID mappings before calling the appropriate web service endpoint.
     * <p>
     * Special cases:
     * - If force upload is enabled, it bypasses fragment skip checks.
     * - For DCApp encoding/decoding sessions, data is uploaded differently (single vs multi-item).
     * - EAN type is only added when required (encoding/encoding-than).
     *
     * @param uploadInventoryDao       DAO for accessing inventory records
     * @param sessionType              Current session type (Encoding/Decoding/etc.)
     * @param urlNormal                API endpoint for normal uploads
     * @param isForceUpload            Whether to force upload regardless of skip conditions
     * @param currentFragmentClassName Name of the currently active fragment (for skip check)
     * @param skipFragments            List of fragment names where uploads should be skipped
     * @param typeValue                Optional type value for request payload
     * @param statusValue              Status value for request payload
     */

    private void commonUploadInventoryWrittenTags(
            final UploadInventoryDao uploadInventoryDao,
            final AppCommonMethods.SessionType sessionType,
            final String urlNormal,
            final boolean isForceUpload,
            final String currentFragmentClassName,
            final String[] skipFragments,
            final String typeValue,
            final String statusValue
    ) {

        // Determine if EAN type is required (encoding sessions only)
        final boolean requiresEanType = sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN;

        // Check if this is DC App mode (special handling for encoding/decoding)
        final boolean isDCAppCheck = isDCApp && (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.DECODING);

        // Decide whether upload should be skipped based on current fragment
        final boolean shouldSkip = isNonEmpty(currentFragmentClassName) && skipFragments != null && skipFragments.length > 0 && (skipFragments[0].equalsIgnoreCase(currentFragmentClassName) || Arrays.asList(skipFragments).contains(currentFragmentClassName));

        // Proceed only if:
        // - Force upload is requested (and upload is not already running), OR
        // - Not in a skipped fragment
        // AND there are pending records to upload
        if (((isForceUpload && !isUploadWrittenTagsRunning) || !shouldSkip) && uploadInventoryDao.getNonUploadedCount(sessionType.getValue()) > 0) {
            isUploadWrittenTagsRunning = true;

            // Fetch all session IDs with pending uploads
            final List<String> sessionIds = uploadInventoryDao.getSessionIds(sessionType.getValue());
            if (isNonEmpty(sessionIds)) {
                for (String sessionId : sessionIds) {
                    // Fetch inventory data based on verification and upload settings
                    final List<UploadInventory> listUploadInvData =
                            (requiresEanType && SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY) && SharedPrefManager.getBoolean(ParamConstants.IS_ENC_UPLOAD_AFTER_VERIFY))
                                    ? uploadInventoryDao.getAllVerifiedUploadInventoryData(sessionId)
                                    : uploadInventoryDao.getAllUploadInventoryData(sessionId);

                    if (isNonEmpty(listUploadInvData)) {
                        try {
                            JSONObject requestParams = null;
                            JSONArray js = new JSONArray();
                            Set<EpcTid> epcTids = new HashSet<>();
                            Set<String> tids = new HashSet<>();

                            // Iterate through inventory records
                            for (UploadInventory uploadInventory : listUploadInvData) {
                                if (uploadInventory != null) {

                                    // Initialize request parameters with session metadata
                                    if (requestParams == null) {
                                        requestParams = isNonEmpty(uploadInventory.extras)
                                                ? new JSONObject(uploadInventory.extras)
                                                : new JSONObject();

                                        if(isNullOrEmpty(extractString(requestParams,ParamConstants.SESSION_ID,""))) requestParams.put(ParamConstants.SESSION_ID, sessionId);
                                        if(isNullOrEmpty(extractString(requestParams,ParamConstants.SESSION_TYPE,""))) requestParams.put(ParamConstants.SESSION_TYPE, sessionType.name());
                                        if(isNullOrEmpty(extractString(requestParams,ParamConstants.TYPE,""))) requestParams.put(ParamConstants.TYPE, isNonEmpty(typeValue) ? typeValue : sessionType.name());
                                        requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD_OFFLINE);
                                        if(isNullOrEmpty(extractString(requestParams,ParamConstants.STATUS,""))) requestParams.put(ParamConstants.STATUS, statusValue);

                                        // Add EAN_TYPE only if required
                                        if (requiresEanType) {
                                            requestParams.put(
                                                    ParamConstants.EAN_TYPE,
                                                    SharedPrefManager.getString(
                                                            SharedPrefManager.SharedPrefKeys.EAN_TYPE,
                                                            context.getString(R.string.btn_encode_config_ean_std)
                                                    )
                                            );
                                        }

                                        // DC App special case: single item
                                        if (isDCAppCheck && listUploadInvData.size() == 1) {
                                            requestParams.put(ParamConstants.SKU_ID, uploadInventory.ean);
                                            requestParams.put(ParamConstants.EAN, uploadInventory.ean);
                                        }
                                    }

                                    final JSONObject dataObject = uploadInventory.toJson();
                                    if (dataObject != null && chkNull(dataObject.toString(), "").length() > 2) {
                                        js.put(dataObject);

                                        final String tid = uploadInventory.tid;
                                        final String newEpc = uploadInventory.newEpc;

                                        if (isNonEmpty(tid)) {
                                            tids.add(isUse24LengthTIDForUpload && tid.length() > 24
                                                    ? tid.substring(0, 24) : tid);
                                        }
                                        if (isNonEmpty(tid) && isNonEmpty(newEpc)) {
                                            epcTids.add(new EpcTid(
                                                    isUse24LengthTIDForUpload && tid.length() > 24
                                                            ? tid.substring(0, 24) : tid,
                                                    newEpc));
                                        }
                                    }
                                    // DC App special case: multi-items (i.e. 1 by 1 uploads)
                                    if (isDCAppCheck && listUploadInvData.size() > 1) {
                                        requestParams.put(ParamConstants.ITEMS, js);
                                        Bundle args = new Bundle();
                                        if (isNonEmpty(epcTids))
                                            args.putParcelableArrayList(AppConstants.LOG_IDS + "1", new ArrayList<EpcTid>(epcTids));
                                        if (isNonEmpty(tids))
                                            args.putStringArrayList(AppConstants.LOG_IDS, new ArrayList<String>(tids));
                                        callWebService(context, AppBroadcastReceiver.this, isDCApp ? URLConstants.UPLOAD_ENCODE : URLConstants.UPLOAD_DECODING, requestParams, args, false);
                                    }
                                }
                            }

                            // Normal case
                            if (isDCAppCheck && listUploadInvData.size() > 1) return;

                            requestParams.put(ParamConstants.ITEMS, js);
                            Bundle args = new Bundle();
                            if (isNonEmpty(epcTids))
                                args.putParcelableArrayList(AppConstants.LOG_IDS + "1", new ArrayList<>(epcTids));
                            if (isNonEmpty(tids))
                                args.putStringArrayList(AppConstants.LOG_IDS, new ArrayList<>(tids));

                            callWebService(
                                    context,
                                    AppBroadcastReceiver.this,
                                    urlNormal,
                                    requestParams,
                                    args,
                                    false
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

  private void commonUploadInventoryWrittenTags(final UploadInventoryDao uploadInventoryDao,
                                                final AppCommonMethods.SessionType sessionType,
                                                final String urlNormal,
                                                final boolean isForceUpload,
                                                final String currentFragmentClassName,
                                                final String[] skipFragments,
                                                final String typeValue) {
    commonUploadInventoryWrittenTags(uploadInventoryDao, sessionType, urlNormal, isForceUpload, currentFragmentClassName, skipFragments, typeValue, sessionType == AppCommonMethods.SessionType.ENCODING_THAN ? AppConstants.THAN_STATUS_ENCODING : AppConstants.SESSION_ACTION_UPLOAD_OFFLINE);
  }


    /**
     * Upload saved inventory tags.
     */
    public void uploadSavedInventoryTags(Context context) {
        if (context != null) {
            this.context = context;
            uploadSavedInventoryTags();
        }
    }

    /**
     * Upload saved inventory tags.
     */
    private void uploadSavedInventoryTags() {
        final String currentFragmentClassName = context != null && context instanceof MainActivity ? ((MainActivity) context).currentFragmentClassName : "";
        if (currentFragmentClassName.equalsIgnoreCase(InventoryStartFragment.class.getSimpleName()) || currentFragmentClassName.equalsIgnoreCase(InventoryAddFragment.class.getSimpleName()) || currentFragmentClassName.equalsIgnoreCase(InventoryBrandFragment.class.getSimpleName()) || currentFragmentClassName.equalsIgnoreCase(InventoryFilterFragment.class.getSimpleName())) {
            final InventoryDao inventoryDao = AppDatabase.getInventoryDao(context);
            final List<String> sessionIds = inventoryDao.getSessionIds();
            if (isNonEmpty(sessionIds)) for (String sessionId : sessionIds) {
                final RFIDSession sessionObject = AppDatabase.getRIFDSessionDao(context).getSession(sessionId);
                final List<Inventory> listUploadInvData = inventoryDao.getAllInventoryData(sessionId);
                if (isNonEmpty(listUploadInvData)) {
                    //new Handler().post(() -> {
                    try {
                        JSONObject requestParams = new JSONObject();
                        requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
                        requestParams.put(ParamConstants.SESSION_TYPE, sessionObject.sessionType);
                        requestParams.put(ParamConstants.TYPE, getInvType(sessionObject.sessionType));
                        final String selZone = chkNull(sessionObject.zone, AppConstants.ALL);
                        if (selZone.equalsIgnoreCase(AppConstants.ALL)) {
                            requestParams.put(ParamConstants.ZONE_ID, 0);
                            requestParams.put(ParamConstants.ZONE, selZone);
                        } else {
                            List<Zone> listZones = AppDatabase.getZoneDao(context).getZoneByName(chkNull(sessionObject.zone, AppConstants.ALL));
                            if (isNonEmpty(listZones) && listZones.size() == 1) {
                                Zone zone = listZones.get(0);
                                requestParams.put(ParamConstants.ZONE_ID, zone != null ? zone.zoneId : 0);
                                requestParams.put(ParamConstants.ZONE, zone != null ? zone.zoneName : chkNull(sessionObject.zone, AppConstants.ALL));
                                requestParams.put(ParamConstants.ZONE_TYPE, zone != null ? zone.zoneType : null);
                                requestParams.put(ParamConstants.IS_DEFAULT_ZONE, zone != null ? zone.isDefault : false);
                            } else {
                                JSONArray zones = new JSONArray();
                                for (Zone zone : listZones) {
                                    if (zone != null) {
                                        JSONObject jsonZone = zone.toJson();
                                        if (jsonZone != null) {
                                            zones.put(jsonZone);
                                        }
                                    }
                                }
                                if (zones != null && zones.length() > 0)
                                    requestParams.put(ParamConstants.ZONES, zones);
                            }
                        }
                        requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD_BACKGROUND);
                        requestParams.put(ParamConstants.STATUS, AppConstants.SESSION_ACTION_UPLOAD_BACKGROUND);
                        JSONArray js = new JSONArray();
                        List<String> epcs = new ArrayList<String>(0);
                        for (Inventory inventory : listUploadInvData) {
                            if (inventory != null) {
                                epcs.add(inventory.epc);
                                JSONObject dataobject = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_SHORT_JSON_REQUEST_FOR_INVENTORY_UPLOAD) ? inventory.toOnlyEpcJson() : inventory.toJson((CommonActivity) context);
                                if (dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                                    js.put(dataobject);
                            }
                        }
                        requestParams.put(ParamConstants.ITEMS, js);
                        Bundle args = new Bundle();
                        args.putStringArrayList(AppConstants.LOG_IDS, new ArrayList<String>(epcs));
                        callWebService(context, AppBroadcastReceiver.this, URLConstants.UPLOAD_INVENTORY, requestParams, args, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //});
                }
            }
        }
    }

    /**
     * Upload saved cartons.
     */
    public void uploadSavedCartons(Context context) {
        if (context != null) {
            this.context = context;
            uploadSavedCartons(true);
        }
    }

    /**
     * Upload saved cartons.
     */
    private void uploadSavedCartons() {
        uploadSavedCartons(false);
    }

    private void uploadSavedCartons(final boolean isForcefullyCall) {
        showLog("uploadSavedCartons", "method called");
        final String currentFragmentClassName = context != null && context instanceof MainActivity ? ((MainActivity) context).currentFragmentClassName : "";
        if (!isForcefullyCall && currentFragmentClassName.equalsIgnoreCase(OffRangeMainFragment.class.getSimpleName()))
            return;
        final UploadInventoryDao uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
        showLog("uploadSavedCartons_Count", "" + uploadInventoryDao.getNonUploadedCount(AppCommonMethods.SessionType.OFF_RANGE.getValue()));
        if (uploadInventoryDao.getNonUploadedCount(AppCommonMethods.SessionType.OFF_RANGE.getValue()) > 0) {
            final List<String> sessionIds = uploadInventoryDao.getSessionIds(AppCommonMethods.SessionType.OFF_RANGE.getValue());
            showLog("uploadSavedCartons_sessionIds", "" + sessionIds);
            if (isNonEmpty(sessionIds)) for (String sessionId : sessionIds) {
                final List<UploadInventory> listUploadInvData = uploadInventoryDao.getAllUploadInventoryData(sessionId);
                showLog("uploadSavedCartons_uploadCount", sessionId + "->" + (isNonEmpty(listUploadInvData) ? listUploadInvData.size() : 0));
                if (isNonEmpty(listUploadInvData)) {
                    showLog("listUploadInvData()", "" + listUploadInvData.size());
                    try {
                        JSONObject requestParams = null;//new JSONObject();
                        JSONArray js = new JSONArray();
                        for (UploadInventory uploadInventory : listUploadInvData) {
                            if (uploadInventory != null) {
                                if (requestParams == null) {
                                    requestParams = isNonEmpty(uploadInventory.extras) ? new JSONObject(uploadInventory.extras) : new JSONObject();
                                    requestParams.put(ParamConstants.SESSION_ID, sessionId);
                                    requestParams.put(ParamConstants.SESSION_TYPE, AppCommonMethods.SessionType.OFF_RANGE.name());
                                    requestParams.put(ParamConstants.ACTION, AppConstants.SESSION_ACTION_UPLOAD_OFFLINE);
                                }
                                final JSONObject dataObject = uploadInventory.toJson();
                                if (dataObject != null && chkNull(dataObject.toString(), "").length() > 2) {
                                    js.put(dataObject);
                                }
                            }
                        }
                        requestParams.put(ParamConstants.ITEMS, js);
                        Bundle args = new Bundle();
                        callWebService(context, AppBroadcastReceiver.this, UPLOAD_OUTWARD_CARTON_DATA, requestParams, args, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String getInvType(final int sessionType) {
        if (sessionType == AppCommonMethods.SessionType.INVENTORY.getValue())
            return AppConstants.TAKE_STOCK;
        if (sessionType == AppCommonMethods.SessionType.ADD_INVENTORY.getValue())
            return AppConstants.ADD_STOCK;
    if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY.getValue() || sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY.getValue())
            return AppConstants.BRAND_STOCK;
        return "";
    }

    /**
     * Handle response.
     *
     * @param url          the url
     * @param jsonRequest  the json request
     * @param jsonResponse the json response
     * @param responseCode the response code
     * @param isSuccess    the is success
     * @param args         the args
     */
    public void handleResponse(final String url, final JSONObject jsonRequest, final JSONObject jsonResponse, final Integer responseCode, final boolean isSuccess, final Bundle args) {
        try {
            switch (url) {
                case URLConstants.UPLOAD_SEARCH_LOG:
                    if (isSuccess) {
                        ArrayList<Integer> listSearchLogIds = args != null && args.containsKey(AppConstants.LOG_IDS) ? args.getIntegerArrayList(AppConstants.LOG_IDS) : null;
                        if (isNullOrEmpty(listSearchLogIds)) {
                            final JSONArray searchLogsJsonArray = extractJSONArray(jsonRequest, ParamConstants.ITEMS);
                            if (searchLogsJsonArray != null && searchLogsJsonArray.length() > 0) {
                                listSearchLogIds = new ArrayList<>(0);
                                //get & Add using requestParams Data in the list before deleting
                                for (int i = 0; i < searchLogsJsonArray.length(); i++) {
                                    try {
                                        final Integer logNo = extractInt(searchLogsJsonArray.getJSONObject(i), ParamConstants.LOG_ID, 0);
                                        if (logNo != null && logNo > 0) listSearchLogIds.add(logNo);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        if (isNonEmpty(listSearchLogIds))
                            AppDatabase.getSearchLogDao(context).deleteLogs(listSearchLogIds);
                    }
                    break;
                case URLConstants.UPLOAD_AUDITTRAILS_LOG:
                    if (isSuccess) {
                        ArrayList<Integer> listAuditTrailsLogIds = args != null && args.containsKey(AppConstants.LOG_IDS) ? args.getIntegerArrayList(AppConstants.LOG_IDS) : null;
                        if (isNullOrEmpty(listAuditTrailsLogIds)) {
                            final JSONArray auditTrailsLogsJsonArray = extractJSONArray(jsonRequest, ParamConstants.ITEMS);
                            if (auditTrailsLogsJsonArray != null && auditTrailsLogsJsonArray.length() > 0) {
                                listAuditTrailsLogIds = new ArrayList<>(0);
                                //get & Add using requestParams Data in the list before deleting
                                for (int i = 0; i < auditTrailsLogsJsonArray.length(); i++) {
                                    try {
                                        final Integer logNo = extractInt(auditTrailsLogsJsonArray.getJSONObject(i), ParamConstants.LOG_ID, 0);
                                        if (logNo != null && logNo > 0)
                                            listAuditTrailsLogIds.add(logNo);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        if (isNonEmpty(listAuditTrailsLogIds)) {
                            //showLog("listAuditTrailsLogIds",listAuditTrailsLogIds.toString());
                            //AppDatabase.getAuditTrailsDao(context).deleteAuditTrailsLogs(listAuditTrailsLogIds);
                            AppDatabase.getAuditTrailsDao(context).deleteAllLogs();
                        }
                    }
                    break;
                case URLConstants.UPLOAD_ENCODING:
                case URLConstants.UPLOAD_ENCODING_THAN:
                case URLConstants.UPLOAD_ENCODE:
                case URLConstants.UPLOAD_DECODING:
                    if (true) {
                        isUploadWrittenTagsRunning = false;
                        //showLog("isUploadWrittenTagsRunning3", "" + isUploadWrittenTagsRunning);
                        final InventoryDao inventoryDao = AppDatabase.getInventoryDao(context);
                        final UploadInventoryDao uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
                        ArrayList<EpcTid> listEpcTids = args != null && args.containsKey(AppConstants.LOG_IDS + "1") ? args.getParcelableArrayList(AppConstants.LOG_IDS + "1") : null;
                        ArrayList<String> listTids = args != null && args.containsKey(AppConstants.LOG_IDS) ? args.getStringArrayList(AppConstants.LOG_IDS) : null;
                        String sessionId = extractString(jsonRequest, ParamConstants.SESSION_ID, "");
                        if (isUse24LengthTIDForUpload && isNullOrEmpty(listTids)) {
                            final JSONArray searchLogsJsonArray = extractJSONArray(jsonRequest, ParamConstants.ITEMS);
                            if (searchLogsJsonArray != null && searchLogsJsonArray.length() > 0) {
                                //listEpcTids = new ArrayList<>(0);
                                listTids = new ArrayList<>(0);
                                //get & Add using requestParams Data in the list before deleting
                                for (int i = 0; i < searchLogsJsonArray.length(); i++) {
                                    try {
                                        if (isNullOrEmpty(sessionId))
                                            sessionId = extractString(searchLogsJsonArray.getJSONObject(i), ParamConstants.SESSION_ID, "").trim();
                                        final String tid = extractString(searchLogsJsonArray.getJSONObject(i), ParamConstants.TID, "").trim();
                                        final String newEpc = extractString(searchLogsJsonArray.getJSONObject(i), ParamConstants.EPC, "").trim();
                                        if (isNonEmpty(tid) && isNonEmpty(newEpc) && !listEpcTids.contains(new EpcTid(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid, newEpc))) {
                                            listEpcTids.add(new EpcTid(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid, newEpc));
                                        }
                                        if (isNonEmpty(tid) && !listTids.contains(tid))
                                            listTids.add(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        if (isSuccess) {
                            if (isNonEmpty(listEpcTids) && isNonEmpty(sessionId)) {
                                try {
                                    for (EpcTid epcTid : listEpcTids) {
                                        uploadInventoryDao.updateUploaded(sessionId, epcTid.getTid(), epcTid.getEpc());
                                        inventoryDao.updateUploaded(sessionId, epcTid.getTid(), epcTid.getEpc());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (isNonEmpty(listTids) && isNonEmpty(sessionId)) {
                                final Set<String> tids = new HashSet<String>(listTids);
                                try {
                                    if (isUpdateUploadStatusBasedOnTID && isNonEmpty(tids)) {
                                        uploadInventoryDao.updateUploaded(sessionId, tids);
                                        inventoryDao.updateUploaded(sessionId, tids);
                                    } else {
                                        uploadInventoryDao.updateUploaded(sessionId);
                                        inventoryDao.updateUploaded(sessionId);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            uploadInventoryDao.deleteUploaded();
                        } else {
                            if (isNonEmpty(sessionId)) {
                                try {
                                    if (isNonEmpty(listEpcTids)) {
                                        for (EpcTid epcTid : listEpcTids) {
                                            uploadInventoryDao.updateUploadRetryCount(sessionId, epcTid.getTid(), epcTid.getEpc());
                                            inventoryDao.updateUploadRetryCount(sessionId, epcTid.getTid(), epcTid.getEpc());
                                        }
                                    }
                                    if (isUpdateUploadStatusBasedOnTID && isNonEmpty(listTids)) {
                                        final Set<String> tids = new HashSet<String>(listTids);
                                        uploadInventoryDao.updateUploadRetryCount(sessionId, tids);
                                        inventoryDao.updateUploadRetryCount(sessionId, tids);
                                    } else {
                                        uploadInventoryDao.updateUploadRetryCount(sessionId);
                                        inventoryDao.updateUploadRetryCount(sessionId);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    break;
                case URLConstants.UPLOAD_INVENTORY:
                    if (true) {
                        final InventoryDao inventoryDao = AppDatabase.getInventoryDao(context);
                        ArrayList<String> listEpcs = args != null && args.containsKey(AppConstants.LOG_IDS) ? args.getStringArrayList(AppConstants.LOG_IDS) : null;
                        String sessionId = extractString(jsonRequest, ParamConstants.SESSION_ID, "");
                        if (isSuccess) {
                            if (isNonEmpty(listEpcs) && isNonEmpty(sessionId)) {
                                final Set<String> epcs = new HashSet<String>(listEpcs);
                                try {
                                    if (isNonEmpty(epcs))
                                        inventoryDao.updateUploaded(epcs, sessionId);
                  /*else{
                    inventoryDao.updateUploaded(sessionId);
                  }*/
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if (isNonEmpty(listEpcs) && isNonEmpty(sessionId)) {
                                final Set<String> epcs = new HashSet<String>(listEpcs);
                                try {
                                    if (isNonEmpty(epcs)) {
                                        inventoryDao.updateUploadRetryCount(epcs, sessionId);
                                    } else {
                                        inventoryDao.updateUploadRetryCount(sessionId);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    break;
                case URLConstants.UPLOAD_OMNICHANNEL:
                    if (true) {
                        final InventoryDao inventoryDao = AppDatabase.getInventoryDao(context);
                        final UploadInventoryDao uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
                        ArrayList<String> listTids = args != null && args.containsKey(AppConstants.LOG_IDS) ? args.getStringArrayList(AppConstants.LOG_IDS) : null;
                        String sessionId = extractString(jsonRequest, ParamConstants.SESSION_ID, "");
                        if (AppCommonMethods.isUpdateUploadStatusBasedOnTID && isNullOrEmpty(listTids)) {
                            final JSONArray searchLogsJsonArray = extractJSONArray(jsonRequest, ParamConstants.ITEMS);
                            if (searchLogsJsonArray != null && searchLogsJsonArray.length() > 0) {
                                listTids = new ArrayList<>(0);
                                //get & Add using requestParams Data in the list before deleting
                                for (int i = 0; i < searchLogsJsonArray.length(); i++) {
                                    try {
                                        if (isNullOrEmpty(sessionId))
                                            sessionId = extractString(searchLogsJsonArray.getJSONObject(i), ParamConstants.SESSION_ID, "").trim();
                                        final String tid = extractString(searchLogsJsonArray.getJSONObject(i), ParamConstants.TID, "").trim();
                                        listTids.add(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        if (isSuccess) {
                            if (isNonEmpty(listTids) && isNonEmpty(sessionId)) {
                                final Set<String> tids = new HashSet<String>(listTids);
                                try {
                                    if (isUpdateUploadStatusBasedOnTID) {
                                        inventoryDao.updateUploaded(sessionId, tids);
                                        uploadInventoryDao.updateUploaded(sessionId, tids);
                                    } else {
                                        inventoryDao.updateUploaded(sessionId);
                                        uploadInventoryDao.updateUploaded(sessionId);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            uploadInventoryDao.deleteUploaded();
                        } else {
                            if (isNonEmpty(listTids) && isNonEmpty(sessionId)) {
                                final Set<String> tids = new HashSet<String>(listTids);
                                try {
                                    if (isUpdateUploadStatusBasedOnTID) {
                                        inventoryDao.updateUploadRetryCount(sessionId, tids);
                                        uploadInventoryDao.updateUploadRetryCount(sessionId, tids);
                                    } else {
                                        inventoryDao.updateUploadRetryCount(sessionId);
                                        uploadInventoryDao.updateUploadRetryCount(sessionId);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    break;
        /*case URLConstants.UPLOAD_DECODING:
          if(true){
            final InventoryDao inventoryDao = AppDatabase.getInventoryDao(context);
            final UploadInventoryDao uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
            ArrayList<String> listTids = args != null && args.containsKey(AppConstants.LOG_IDS) ? args.getStringArrayList(AppConstants.LOG_IDS) : null;
            String sessionId = extractString(jsonRequest, ParamConstants.SESSION_ID, "");
            if(AppCommonMethods.isUpdateUploadStatusBasedOnTID && isNullOrEmpty(listTids)){
              final JSONArray searchLogsJsonArray = extractJSONArray(jsonRequest, ParamConstants.ITEMS);
              if(searchLogsJsonArray != null && searchLogsJsonArray.length() > 0){
                listTids = new ArrayList<>(0);
                //get & Add using requestParams Data in the list before deleting
                for(int i = 0; i < searchLogsJsonArray.length(); i++){
                  try{
                    if(isNullOrEmpty(sessionId))
                      sessionId = extractString(searchLogsJsonArray.getJSONObject(i), ParamConstants.SESSION_ID, "").trim();
                    final String tid = extractString(searchLogsJsonArray.getJSONObject(i), ParamConstants.TID, "").trim();
                    listTids.add(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid);
                  }
                  catch(Exception e){ e.printStackTrace(); }
                }
              }
            }
            if(isSuccess){
              if(isNonEmpty(listTids) && isNonEmpty(sessionId)){
                final Set<String> tids = new HashSet<String>(listTids);
                try{
                  if(isUpdateUploadStatusBasedOnTID){
                    inventoryDao.updateUploaded(sessionId, tids);
                    uploadInventoryDao.updateUploaded(sessionId, tids);
                  }
                  else{
                    inventoryDao.updateUploaded(sessionId);
                    uploadInventoryDao.updateUploaded(sessionId);
                  }
                }
                catch(Exception e){ e.printStackTrace(); }
              }
              uploadInventoryDao.deleteUploaded();
            }
            else{
              if(isNonEmpty(listTids) && isNonEmpty(sessionId)){
                final Set<String> tids = new HashSet<String>(listTids);
                try{
                  if(isUpdateUploadStatusBasedOnTID){
                    inventoryDao.updateUploadRetryCount(sessionId, tids);
                    uploadInventoryDao.updateUploadRetryCount(sessionId, tids);
                  }
                  else{
                    inventoryDao.updateUploadRetryCount(sessionId);
                    uploadInventoryDao.updateUploadRetryCount(sessionId);
                  }
                }
                catch(Exception e){ e.printStackTrace(); }
              }
            }
          }*/
        /*case URLConstants.UPLOAD_ENCODE:
          if(true){
            final InventoryDao inventoryDao = AppDatabase.getInventoryDao(context);
            final UploadInventoryDao uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
            ArrayList<EpcTid> listEpcTids = args != null && args.containsKey(AppConstants.LOG_IDS + "1") ? args.getParcelableArrayList(AppConstants.LOG_IDS + "1") : null;
            ArrayList<String> listTids = args != null && args.containsKey(AppConstants.LOG_IDS) ? args.getStringArrayList(AppConstants.LOG_IDS) : null;
            String sessionId = extractString(jsonRequest, ParamConstants.SESSION_ID, "");
            if(isUse24LengthTIDForUpload && isNullOrEmpty(listTids)){
              final JSONArray searchLogsJsonArray = extractJSONArray(jsonRequest, ParamConstants.ITEMS);
              if(searchLogsJsonArray != null && searchLogsJsonArray.length() > 0){
                //listEpcTids = new ArrayList<>(0);
                listTids = new ArrayList<>(0);
                //get & Add using requestParams Data in the list before deleting
                for(int i = 0; i < searchLogsJsonArray.length(); i++){
                  try{
                    if(isNullOrEmpty(sessionId))
                      sessionId = extractString(searchLogsJsonArray.getJSONObject(i), ParamConstants.SESSION_ID, "").trim();
                    final String tid = extractString(searchLogsJsonArray.getJSONObject(i), ParamConstants.TID, "").trim();
                    final String newEpc = extractString(searchLogsJsonArray.getJSONObject(i), ParamConstants.EPC, "").trim();
                    if(isNonEmpty(tid) && isNonEmpty(newEpc) && !listEpcTids.contains(new EpcTid(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid, newEpc))){
                      listEpcTids.add(new EpcTid(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid, newEpc));
                    }
                    if(isNonEmpty(tid) && !listTids.contains(tid))
                      listTids.add(isUse24LengthTIDForUpload && tid.length() > 24 ? tid.substring(0, 24) : tid);
                  }
                  catch(Exception e){ e.printStackTrace(); }
                }
              }
            }
            if(isSuccess){
              if(isNonEmpty(listEpcTids) && isNonEmpty(sessionId)){
                try{
                  for(EpcTid epcTid : listEpcTids){
                    uploadInventoryDao.updateUploaded(sessionId, epcTid.getTid(), epcTid.getEpc());
                    inventoryDao.updateUploaded(sessionId, epcTid.getTid(), epcTid.getEpc());
                  }
                }
                catch(Exception e){ e.printStackTrace(); }
              }
              if(isNonEmpty(listTids) && isNonEmpty(sessionId)){
                final Set<String> tids = new HashSet<String>(listTids);
                try{
                  if(isUpdateUploadStatusBasedOnTID && isNonEmpty(tids)){
                    uploadInventoryDao.updateUploaded(sessionId, tids);
                    inventoryDao.updateUploaded(sessionId, tids);
                  }
                  else{
                    uploadInventoryDao.updateUploaded(sessionId);
                    inventoryDao.updateUploaded(sessionId);
                  }
                }
                catch(Exception e){ e.printStackTrace(); }
              }
              uploadInventoryDao.deleteUploaded();
            }
            else{
              if(isNonEmpty(sessionId)){
                try{
                  if(isNonEmpty(listEpcTids)){
                    for(EpcTid epcTid : listEpcTids){
                      uploadInventoryDao.updateUploadRetryCount(sessionId, epcTid.getTid(), epcTid.getEpc());
                      inventoryDao.updateUploadRetryCount(sessionId, epcTid.getTid(), epcTid.getEpc());
                    }
                  }
                  if(isUpdateUploadStatusBasedOnTID && isNonEmpty(listTids)){
                    final Set<String> tids = new HashSet<String>(listTids);
                    uploadInventoryDao.updateUploadRetryCount(sessionId, tids);
                    inventoryDao.updateUploadRetryCount(sessionId, tids);
                  }
                  else{
                    uploadInventoryDao.updateUploadRetryCount(sessionId);
                    inventoryDao.updateUploadRetryCount(sessionId);
                  }
                }
                catch(Exception e){ e.printStackTrace(); }
              }
            }
          }
          break;*/
                case URLConstants.CHECK_FOR_UPDATE:
                case URLConstants.CHECK_FOR_HARDWARE_UPDATE:
                    if (isSuccess) {
                        final String errorMsg = extractString(jsonResponse, ParamConstants.ERROR, "");
                        if (isNonEmpty(errorMsg)) return;
                        JSONObject jsonResponseData = extractJSONObject(jsonResponse, ParamConstants.DATA, jsonResponse);
                        final String apkVersion = extractString(jsonResponseData, ParamConstants.APK_VER, "");
                        final String downloadUrl1 = extractString(jsonResponseData, ParamConstants.FTP_PATH, extractString(jsonResponseData, ParamConstants.BUILD_URL, isDebugApp ? "http://43.204.72.42/" : "")).trim();
                        final String apkName = downloadUrl1.matches("(?i)(^(http|https)://.*\\.apk$)") ? downloadUrl1.substring(downloadUrl1.lastIndexOf("/") + 1) : extractString(jsonResponseData, ParamConstants.APK_NAME, extractString(jsonResponseData, ParamConstants.BUILD_NAME, isDebugApp ? "Retail_240202_30.apk" : "")).trim();
                        final String downloadUrl = downloadUrl1.contains(apkName) ? downloadUrl1.replace(apkName, "") : downloadUrl1;
                        showLog("apk_fullFileUrl", downloadUrl + apkName);
                        showLog("apkVer_appVer_compare", apkVersion + "==" + BuildConfig.VERSION_NAME + "? =>" + (apkVersion.replaceAll("[A-Za-z]", "").trim().compareTo(BuildConfig.VERSION_NAME)));
                        final boolean isVersionMisMatch = isNullOrEmpty(apkVersion) || apkVersion.replaceAll("[A-Za-z]", "").trim().compareTo(BuildConfig.VERSION_NAME) > 0;
                        if (isVersionMisMatch && isValidUrl(downloadUrl) && apkName.toLowerCase().endsWith(".apk"))
                            AppCommonMethods.callDownloadFile(context, AppBroadcastReceiver.this, downloadUrl, apkName, extractString(jsonResponseData, ParamConstants.MESSAGE));
                    }
                    break;
                case UPLOAD_OUTWARD_CARTON_DATA:
                    if (isSuccess) {
                        final InventoryDao inventoryDao = AppDatabase.getInventoryDao(context);
                        final UploadInventoryDao uploadInventoryDao = AppDatabase.getUploadInventoryDao(context);
                        final String sessionId = extractString(jsonRequest, ParamConstants.SESSION_ID, "");
                        final String cartonNo = extractString(jsonRequest, ParamConstants.CARTON_NO, extractString(jsonRequest, ParamConstants.CARTON_NUM, extractString(jsonRequest, ParamConstants.CARTON_NUMBER)));
                        final String batchId = extractString(jsonRequest, ParamConstants.BATCH_ID, extractString(jsonRequest, ParamConstants.BATCH_ID));
                        final ArrayList<String> listCompletedCartons = isNonEmpty(batchId) && isNonEmpty(cartonNo) ? SharedPrefManager.getStringArrayList(batchId + ParamConstants.COMPLETED_CARTONS, new ArrayList<>(0)) : new ArrayList<>(0);
                        try {
                            uploadInventoryDao.updateUploaded(sessionId);
                            inventoryDao.updateUploaded(sessionId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        final int batchCartons = uploadInventoryDao.getCartonCountFromBatchId(batchId);
                        if (!isSaveCompletedCartonAfterUpload) uploadInventoryDao.deleteUploaded();
                        else if (isSaveCompletedCartonAfterUpload && batchCartons > saveLimitForCompletedCartons) {
                            List<String> listUploadedCartons = uploadInventoryDao.getUploadedCartonsFromBatchId(batchId);
                            if (listUploadedCartons.size() > 0) {
                                if (listUploadedCartons.size() <= (batchCartons - saveLimitForCompletedCartons))
                                    uploadInventoryDao.deleteUploaded();
                                else
                                    uploadInventoryDao.deleteUploaded(batchId, listUploadedCartons.subList(0, (batchCartons - saveLimitForCompletedCartons) + 1));
                            }
                            //uploadInventoryDao.deleteUploaded();
                        }
                    }
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postFileWrite(final String filePath) {

    }
}
