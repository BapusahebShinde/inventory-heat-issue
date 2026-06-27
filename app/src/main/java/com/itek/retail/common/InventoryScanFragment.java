package com.itek.retail.common;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractLong;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUseAPICallForSessionInventory;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.BrandWiseZoneInventoryDao;
import com.itek.retail.database.ZoneDao;
import com.itek.retail.databinding.HeaderTitleLayoutBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.MenuModel;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.customviews.BtnStartStopView;
import com.itek.retail.ui.customviews.BtnSwipeUploadView;
import com.itek.retail.ui.customviews.CountTotalView;
import com.itek.retail.ui.customviews.HeaderSpinner;
import com.itek.retail.ui.customviews.PowerView;
import com.itek.retail.ui.customviews.swipeButton.ProSwipeButtonVar;
import com.itek.retail.ui.search.alien.SearchAlienFragment;
import com.itek.retail.ui.search.unencoded.SearchUnencodedFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryScanFragment extends CommonInventoryFragment {

    protected ZoneDao zoneDao;
    protected BrandWiseZoneInventoryDao brandWiseZoneInventoryDao;
    protected CountTotalView ctwAlienTags, ctwUnencodedTags;
    //temp flags
    boolean isAPICallForSessionResume = false;
    private ImageView imgInventoryStartInfo;
    private HeaderSpinner spinInventoryStartLocation;
    protected Long inventoryCount = -1L;

    /**
     * Instantiates a new Inventory scan fragment.
     */
    public InventoryScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zoneDao = AppDatabase.getZoneDao(context);
        brandWiseZoneInventoryDao = AppDatabase.getBrandWiseZoneInventoryDao(context);
    }

    protected void setViews(final HeaderTitleLayoutBinding header, final HeaderSpinner spinInventoryStartLocation, final PowerView llSeekbarPower, final BtnStartStopView llBtnStart, final CountTotalView ctwInventoryStart, final CountTotalView ctwAlienTags, final CountTotalView ctwUnencodedTags, final LinearLayout llInventoryStartSessionLbls, final BtnSwipeUploadView swipeUpload) {
        super.setViews(header, llSeekbarPower, llBtnStart, ctwInventoryStart,swipeUpload);
        if (spinInventoryStartLocation != null) {
            this.spinInventoryStartLocation = spinInventoryStartLocation;
            setSpinnerZone(spinInventoryStartLocation);
        }
        if (ctwAlienTags != null) {
            this.ctwAlienTags = ctwAlienTags;
            ctwAlienTags.setVisibility(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV) || SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_ALIEN_COUNT_IN_INV) ? View.VISIBLE : View.GONE);
            //Redirection to Alien Search (with Title changed)
            ctwAlienTags.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v != null && v.getVisibility() == View.VISIBLE && sessionObject != null && sessionObject.sessionId != null && getSize() > 0 && !chkNotNullTrue(mainViewModel.getIsProcessOn().getValue()) && chkZero(ctwAlienTags.getScore(), 0L) > 0) {
                        Bundle args = new Bundle();
                        final MenuModel menuAlienSearch = AppDatabase.getMenuDao(context).getMenuByCode(AppConstants.MENU_CODE_SER_ALIEN);
                        args.putString(AppConstants.TITLE, menuAlienSearch != null ? menuAlienSearch.getScreenMenuName() : "Alien Search");
                        args.putString(AppConstants.TITLE_LOGO_URL, menuAlienSearch != null ? menuAlienSearch.getScreenImageUrl() : "");
                        args.putInt(AppConstants.TITLE_LOGO_RES_ID, menuAlienSearch != null ? menuAlienSearch.getScreenIconId(context) : R.drawable.ic_ser_unencoded);
                        args.putString(AppConstants.ALIEN_SEARCH_TYPE, AppConstants.ALIEN_SEARCH_TYPE_ONLINE);
                        args.putString(ParamConstants.ZONE, sessionObject.zone);
                        args.putString(ParamConstants.SESSION_ID, sessionObject.sessionId);
                        args.putInt(ParamConstants.SESSION_TYPE, sessionObject.sessionType);
                        context.loadFragment(new SearchAlienFragment(), args);
                    }
                }
            });
        }
        if (ctwUnencodedTags != null) {
            this.ctwUnencodedTags = ctwUnencodedTags;
            ctwUnencodedTags.setVisibility(SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV) || SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_COUNT_IN_INV) ? View.VISIBLE : View.GONE);
            //Redirection to Unencoded Search
            ctwUnencodedTags.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v != null && v.getVisibility() == View.VISIBLE && sessionObject != null && sessionObject.sessionId != null && getSize() > 0 && !chkNotNullTrue(mainViewModel.getIsProcessOn().getValue()) && chkZero(ctwUnencodedTags.getScore(), 0L) > 0) {
                        Bundle args = new Bundle();
                        final MenuModel menuUnencodedSearch = AppDatabase.getMenuDao(context).getMenuByCode(AppConstants.MENU_CODE_SER_UNENCODED);
                        args.putString(AppConstants.TITLE, menuUnencodedSearch != null ? menuUnencodedSearch.getScreenMenuName() : "Unencoded Search");
                        args.putString(AppConstants.TITLE_LOGO_URL, menuUnencodedSearch != null ? menuUnencodedSearch.getScreenImageUrl() : "");
                        args.putInt(AppConstants.TITLE_LOGO_RES_ID, menuUnencodedSearch != null ? menuUnencodedSearch.getScreenIconId(context) : R.drawable.ic_ser_unencoded);
                        args.putString(AppConstants.UNENCODED_SEARCH_TYPE, AppConstants.UNENCODED_SEARCH_TYPE_ONLINE);
                        args.putString(ParamConstants.ZONE, sessionObject.zone);
                        args.putString(ParamConstants.SESSION_ID, sessionObject.sessionId);
                        args.putInt(ParamConstants.SESSION_TYPE, sessionObject.sessionType);
                        context.loadFragment(new SearchUnencodedFragment(), args);
                    }
                }
            });
        }
        if (llInventoryStartSessionLbls != null) {
            imgInventoryStartInfo = llInventoryStartSessionLbls.findViewById(R.id.img_inventory_start_info);
            if (imgInventoryStartInfo != null) setLastInventoryDate(imgInventoryStartInfo);
        }
        //API Call for Getting Active Users (When Restarting Inventory)
        if (isAPICallForSessionResume && sessionObject != null)
            apiCall(AppConstants.SESSION_ACTION_RESUME);
    }

    protected void onBtnStartClick() {
        context.dismissCustomAlertDialog();
        if (!validateBeforeInvSessionStart()) return;
        final Boolean isInventorySessionOn = chkNotNullTrue(mainViewModel.getIsSessionOn().getValue());
        if (!isInventorySessionOn) {
            if (sessionObject != null) mainViewModel.startSession(sessionObject, true);
            else if (sessionObject == null) apiCall(AppConstants.SESSION_ACTION_START);
        } else {
            if (getSize() >= AppCommonMethods.invLimit) {
                if (chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
                    mainViewModel.stopInventory();
                context.showCustomErrDialog(R.string.err_inventory_max_limit);
            } else toggleInventory();
        }
    }


    @Override
    protected void onBtnUploadSwiped() {
        context.showCustomConfirmDialog(getString(R.string.msg_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                apiCall(AppConstants.SESSION_ACTION_UPLOAD);
            }
        });
    }

    protected void setLastInventoryDate(final ImageView imgInventoryStartInfo) {
        String lastInventoryDate = SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.INVENTORY_TAKEN_DATE_TIME, "T").replaceFirst("T", " ").trim();
        if (imgInventoryStartInfo != null && isNonEmpty(lastInventoryDate) && lastInventoryDate.trim().contains(" ") && lastInventoryDate.length() >= 5) {
            context.setTooltipText(imgInventoryStartInfo, HtmlCompat.fromHtml(isNonEmpty(lastInventoryDate) && lastInventoryDate.trim().contains(" ") && lastInventoryDate.trim().split(" ").length > 1 && lastInventoryDate.length() >= 5 ? String.format(getString(R.string.txt_inventory_date_time), lastInventoryDate.split(" ")[0], lastInventoryDate.split(" ")[1]) : "", HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    @Override
    protected void onReaderConfigured() {
        super.onReaderConfigured();
    }

    protected void setSpinnerZone(final HeaderSpinner spinInventoryLocation) {
        if (spinInventoryLocation == null) return;
        List<Zone> listLocations = new ArrayList<Zone>(zoneDao.getAllNonDisplayZones());
        if (SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_ALL_ZONE_INVENTORY_FOR_TAKE_STOCK, AppCommonMethods.isAllowAllZoneInventoryForTakeStock))
            listLocations.add(new Zone("" + 0, AppConstants.ALL));
        else if (isNullOrEmpty(listLocations))//Show error for 'No zones available'
            context.showCustomErrDialog(String.format(getString(R.string.err_no__available), spinInventoryLocation.getLabel()), true);

        spinInventoryLocation.setAdapter(listLocations, 1);

        spinInventoryLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) {
                    onSpinInventoryLocationItemSelected();
                   // if (ctwInventoryStart != null) ctwInventoryStart.setTotal(getInvCount());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                /*Empty Method (Default Overridden)*/
            }
        });

        final String loc = sessionObject != null ? sessionObject.zone : extractString(getArguments(), AppConstants.LOCATION, "");
        showLog("loc", loc);
        if (isNonEmpty(loc)) spinInventoryLocation.setSelection(loc);
        spinInventoryLocation.setEnabled(sessionObject == null);
    }
    
    protected void onSpinInventoryLocationItemSelected(){
       if (ctwInventoryStart != null) ctwInventoryStart.setTotal(getInvCount());
    }

    @Override
    protected boolean validateBeforeInvSessionStart() {
        return spinInventoryStartLocation != null && spinInventoryStartLocation.validate();
    }

    @Override
    protected String getSelectedZone() {
        return spinInventoryStartLocation != null ? chkNull(spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL) : AppConstants.ALL;
    }

    @Override
    protected Zone getSelectedZoneObject() {
        Object selZoneObj = spinInventoryStartLocation.getSelectedObject();
        return selZoneObj != null && selZoneObj instanceof Zone ? (Zone) selZoneObj : null;
    }

    @Override
    protected Set<String> getSelectedZones() {
        return spinInventoryStartLocation != null ? spinInventoryStartLocation.getSelectedVals() : new HashSet<String>(0);
    }

    @Override
    protected void isSessionOnChanged(final Boolean isInventorySessionOn) {
        super.isSessionOnChanged(isInventorySessionOn);
        if (isInventorySessionOn == null) return;
        boolean isInvSessionOn = chkNotNullTrue(isInventorySessionOn);
        if (spinInventoryStartLocation != null)
            spinInventoryStartLocation.setEnabled(!isInvSessionOn);
        final boolean isShowLastInventoryDate = chkNull(SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.INVENTORY_TAKEN_DATE_TIME, "T").replaceFirst("T", " ").trim(), "").length() >= 5;
        if (imgInventoryStartInfo != null)
            imgInventoryStartInfo.setVisibility(isShowLastInventoryDate && !isInvSessionOn ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void isInventoryOnChanged(Boolean isInventoryOn) {
        super.isInventoryOnChanged(isInventoryOn);
        final boolean isShowLastInventoryDate = chkNull(SharedPrefManager.getString(SharedPrefManager.SharedPrefKeys.INVENTORY_TAKEN_DATE_TIME, "T").replaceFirst("T", " ").trim(), "").length() >= 5;
        if (imgInventoryStartInfo != null)
            imgInventoryStartInfo.setVisibility(isShowLastInventoryDate && !chkNotNullTrue(isInventoryOn) ? View.VISIBLE : View.GONE);
    }


    @Override
    protected void onTriggerPressed() {
        super.onTriggerPressed();
    }

    @Override
    protected void onDataSizeChanged(Integer size) {
        super.onDataSizeChanged(size);
        if (ctwAlienTags != null) ctwAlienTags.setScore(chkNull(getAlignTagCount(), 0));
        if (ctwUnencodedTags != null) ctwUnencodedTags.setScore(chkNull(getUnencodedTagCount(), 0));
    }

    @Override
    protected void onReaderPowerChanged(Integer power) {
        super.onReaderPowerChanged(power);
    }

    @Override
    public void apiCall(String action) {
        final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
        if (isUseAPICallForSessionInventory && isInternetConnected(context, false, isUpload)) {
            try {
                if (isUpload)
                    showProgressDialog(getString(R.string.progress_msg_check_upload_data));
                allowBtnClick = false;
                JSONObject requestParams = new JSONObject();
                requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
                requestParams.put(ParamConstants.TYPE, getType());
                final String selZone = sessionObject != null ? sessionObject.zone : getSelectedZone();//chkNull(spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL);
                if (selZone.equalsIgnoreCase(AppConstants.ALL)) {
                    requestParams.put(ParamConstants.ZONE_ID, 0);
                    requestParams.put(ParamConstants.ZONE, selZone);
                } else {
                    List<Zone> listZones = AppDatabase.getZoneDao(context).getZoneByName(sessionObject != null ? sessionObject.zone : selZone);//chkNull(spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL));
                    if (isNonEmpty(listZones) && listZones.size() == 1) {
                        Zone zone = listZones.get(0);
                        requestParams.put(ParamConstants.ZONE_ID, zone != null ? zone.zoneId : 0);
                        requestParams.put(ParamConstants.ZONE, zone != null ? zone.zoneName : sessionObject != null ? sessionObject.zone : selZone);//chkNull(spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL));
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
                requestParams.put(ParamConstants.ACTION, action);
                requestParams.put(ParamConstants.STATUS, action.replaceFirst("(?i)" + AppConstants.SESSION_ACTION_UPLOAD, AppConstants.SESSION_ACTION_STOP).replaceFirst("(?i)" + AppConstants.SESSION_ACTION_DISCARD, AppConstants.SESSION_ACTION_STOP).replaceFirst("(?i)" + AppConstants.SESSION_ACTION_SAVE, AppConstants.SESSION_ACTION_PAUSE));
                if (sessionObject != null && sessionObject.sessionId != null) {
                    requestParams.put(ParamConstants.SESSION_ID, sessionObject.sessionId);
                    if (isUpload) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONArray js = new JSONArray();
                                    List<Inventory> dataList = AppDatabase.getInventoryDao(context).getAllInventoryData(sessionObject.sessionId);
                                    if (isNonEmpty(dataList)) for (Inventory inventory : dataList) {
                                        JSONObject dataobject = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_SHORT_JSON_REQUEST_FOR_INVENTORY_UPLOAD) ? inventory.toOnlyEpcJson() : inventory.toJson(context);
                                        if (dataobject != null && chkNull(dataobject.toString(), "").length() > 2)
                                            js.put(dataobject);
                                    }
                                    requestParams.put(ParamConstants.ITEMS, js);
                                    allowBtnClick = true;
                                    callWebService(URLConstants.UPLOAD_INVENTORY, requestParams, getString(R.string.progress_msg_uploading_data), !isUpload);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    hideProgressDialog();
                                    allowBtnClick = true;
                                }
                            }
                        });
                        //add Other Parameters for bulk uploading
                    }
                }
                if (!isUpload) {
                    allowBtnClick = true;
                    callWebService(isUpload ? URLConstants.UPLOAD_INVENTORY : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : action.matches("(?i)(" + AppConstants.SESSION_ACTION_START + "|" + AppConstants.SESSION_ACTION_RESUME + ")") ? action + "ing Session...\nPlease wait..." : action + "ing Data...\nPlease wait...", !isUpload);
                }
            } catch (JSONException e) {
                allowBtnClick = true;
                e.printStackTrace();
                if (!isUpload)
                    setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
                else hideProgressDialog();
            }
        }
        else if (!isUpload)
            setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
    }

    protected String getType() {
        switch (getSessionType()) {
            case INVENTORY:
                return AppConstants.TAKE_STOCK;
            case ADD_INVENTORY:
                return AppConstants.ADD_STOCK;
            case BRAND_INVENTORY:
            case FILTER_INVENTORY:
                return AppConstants.BRAND_STOCK;
            default:
                break;
        }
        return "";
    }

    /**
     * Get inv count string.
     *
     * @return the string
     */
    @Override
    public String getInvCount() {
        if (sessionObject != null) return sessionObject.total;
        final String loc = getSelectedZone();//spinInventoryStartLocation != null ? chkNull(spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL) : AppConstants.ALL;
        final Set<String> locations = getSelectedZones();//spinInventoryStartLocation != null ? spinInventoryStartLocation.getSelectedVals() : new HashSet<String>(0);
        if (brandWiseZoneInventoryDao == null || !brandWiseZoneInventoryDao.hasData()) return "0";
        else return chkZero(brandWiseZoneInventoryDao.getInvCount(loc, locations), "0");
    }

    /**
     * Set session action.
     *
     * @param action    the action
     * @param sessionId the session id
     */
    protected void setSessionAction(String action, String sessionId) {
        setSessionAction(action, sessionId, null, null, null);
    }

    @Override
    protected void resetViewsOnUpload() {
        super.resetViewsOnUpload();
        if (ctwAlienTags != null) ctwAlienTags.setScore(0);
        if (ctwUnencodedTags != null) ctwUnencodedTags.setScore(0);
    }

    @Override
    public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args) {
        super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
        try {
            switch (url) {
                case URLConstants.SET_SESSION:
                    handleResponseSetSession(url, jsonRequest, jsonResponse, isSuccess, args);
                    break;
                case URLConstants.UPLOAD_INVENTORY:
                    if (isSuccess && jsonResponse != null && sessionObject != null) {
                        removeObservers();
                        mainViewModel.stopSession(sessionObject, true);
                        resetViewsOnUpload();
                        context.showCustomSuccessDialog(extractString(jsonResponse, ParamConstants.MESSAGE, String.format(getString(R.string.done_upload), getTypeCharCode())), true);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void handleResponseSetSession(String url, JSONObject jsonRequest, JSONObject jsonResponse, boolean isSuccess, Bundle args) {
        final String action = extractString(jsonRequest, ParamConstants.ACTION, sessionObject == null ? AppConstants.SESSION_ACTION_START : AppConstants.SESSION_ACTION_STOP);
        String sessionId = null, sessionTime = null;
        //Long inventoryCount = null;
        if (isSuccess && jsonResponse != null) {
            boolean hasEans = !action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) && sessionObject != null || extractBoolean(jsonRequest, ParamConstants.HAS_EANS, false);
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if (isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())) {
                if (!hasEans) hideProgressDialog();
                context.showCustomErrDialog(errMsg);
                return;
            }
            sessionId = extractString(jsonResponse, ParamConstants.SESSION_ID);
            sessionTime = extractString(jsonResponse, ParamConstants.SESSION_TIME);
            sessionValidTill = extractInt(jsonResponse, ParamConstants.SESSION_VALID_TILL, 48);
            inventoryCount = extractLong(jsonResponse, ParamConstants.INVENTORY_COUNT, Long.parseLong(chkNull(getInvCount(), "-").replaceAll("-", "0")));//AppDatabase.getBrandWiseZoneInventoryDao(context).getInvCount(loc, locations));
            activeUsers = extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, activeUsers);
            final JSONArray excludeEpcs = extractJSONArray(jsonResponse, ParamConstants.EXCLUDE_INVENTORY_EPCS);
            if (isNonEmpty(excludeEpcs) && sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START))
                extractIgnoreEpcs(excludeEpcs);
        }
        setSessionAction(action, sessionId, sessionTime, inventoryCount, activeUsers);
    }

}
