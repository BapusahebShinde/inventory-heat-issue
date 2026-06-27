package com.itek.retail.ui.inventory;

import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractJSONArray;
import static com.itek.retail.common.AppCommonMethods.extractJSONObject;
import static com.itek.retail.common.AppCommonMethods.extractLong;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isInternetConnected;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUseAPICallForSessionInventory;
import static com.itek.retail.common.AppCommonMethods.toUnderScoreCase;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.itek.retail.R;
import com.itek.retail.adapter.InvBrandwiseQtyAdapter;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.apis.URLConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.InsertDBBrandEANs;
import com.itek.retail.common.InventoryScanFragment;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.BrandDao;
import com.itek.retail.database.BrandEanDao;
import com.itek.retail.databinding.FragmentInventoryBrandBinding;
import com.itek.retail.model.BrandEans;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.Zone;
import com.itek.retail.ui.customviews.HeaderSpinner;
import com.itek.retail.ui.customviews.MaxHeightRecyclerView;
import com.itek.retail.ui.home.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The Inv brand fragment.
 */
public class InventoryBrandFragment extends InventoryScanFragment {

    //private final Set<String> eans = new HashSet<>(0);
    private final HashMap<String, String> filters = new HashMap<>(0);
    BrandDao brandDao;
    BrandEanDao brandEanDao;
    private FragmentInventoryBrandBinding binding;
    private InventoryBrandViewModel mViewModel;
    private Long inventoryCount = -1L;
    private AlertDialog advFilterDialog = null;
    private String selFilterBrand = "";
    private String selFilterZone = "";
    private JSONObject filtersVals = null;
    private boolean isAllNullFilters = true;
    private boolean isFilterApplied = false;
    private boolean isAllowAdvanceFilters = false;
    private boolean isAllowAdvanceFiltersForMultiBrands = false;

    /**
     * Instantiates a new Inv brand fragment.
     */
    public InventoryBrandFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        brandDao = AppDatabase.getBrandDao(context);
        brandEanDao = AppDatabase.getBrandEansDao(context);
        isAllowAdvanceFilters = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_ADVANCE_FILTERS_FOR_BRAND_INVENTORY, AppCommonMethods.isAllowAdvanceFilterForBrand);
        isAllowAdvanceFiltersForMultiBrands = SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_ADVANCE_FILTERS_FOR_MULTI_BRANDS, AppCommonMethods.isAllowAdvanceFilterForMultiBrands);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(InventoryBrandViewModel.class);

        binding = FragmentInventoryBrandBinding.inflate(inflater, container, false);

        setViews(binding.header, binding.spinInventoryStartLocation, binding.llSeekbarPower, binding.llBtnStart, binding.ctwInventoryStart, binding.ctwAlien, binding.ctwUnencoded, binding.llInventoryStartSessionLbls, binding.btnUpload);

        /*binding.spinInventoryStartLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) {
                    if (isAllowAdvanceFilters && isNonEmpty(selFilterBrand) && isNonEmpty(selFilterZone) && !binding.spinInventoryStartLocation.getSelectedItem().equalsIgnoreCase(selFilterZone)) {
                        selFilterBrand = "";
                        selFilterZone = "";
                        filtersVals = null;
                        inventoryCount = -1L;
                        filters.clear();
                        brandEanDao.deleteAll();
                        binding.chkFilterApplied.setChecked(false);
                        isFilterApplied = false;
                    }
                    if (ctwInventoryStart != null) ctwInventoryStart.setTotal(getInvCount());

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                *//*Empty Method (Default Overridden)*//*
            }
        });*/

        /*final String loc = sessionObject != null ? sessionObject.zone : extractString(getArguments(), AppConstants.LOCATION, "");
        showLog("loc", loc);
        if (isNonEmpty(loc)) binding.spinInventoryStartLocation.setSelection(loc);
        binding.spinInventoryStartLocation.setEnabled(sessionObject == null);*/

        final int maxSelectionBrands = SharedPrefManager.getInt(ParamConstants.MAX_SELECTION_BRANDS, 0);
        List<String> listBrands = brandDao.getAllBrands();
        if (listBrands != null && listBrands.size() <= maxSelectionBrands)
            listBrands.add(0, AppConstants.ALL);
        binding.spinInventoryStartBrand.setAdapter(listBrands, maxSelectionBrands, extractString(getArguments(), ParamConstants.LABEL, SharedPrefManager.getString(ParamConstants.LABEL_BRANDS)));

        binding.spinInventoryStartBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) {
                    if (isAllowAdvanceFilters && isNonEmpty(selFilterBrand) && !binding.spinInventoryStartBrand.getSelectedItem().equalsIgnoreCase(selFilterBrand)) {
                        selFilterBrand = "";
                        selFilterZone = "";
                        filtersVals = null;
                        inventoryCount = -1L;
                        filters.clear();
                        brandEanDao.deleteAll();
                        binding.chkFilterApplied.setChecked(false);
                        isFilterApplied = false;
                    }
                    if (ctwInventoryStart != null) ctwInventoryStart.setTotal(getInvCount());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                /*Empty Method (Default Overridden)*/
            }
        });

        final String brands = sessionObject != null ? sessionObject.brands : extractString(getArguments(), AppConstants.BRANDS, "");
        showLog("brands", brands);
        if (isNonEmpty(brands)) binding.spinInventoryStartBrand.setSelection(brands);
        binding.spinInventoryStartBrand.setEnabled(sessionObject == null);

        isFilterApplied = sessionObject != null && isNonEmpty(sessionObject.extras);
        binding.chkFilterApplied.setChecked(isFilterApplied);
        binding.chkApplyFilter.setVisibility(isAllowAdvanceFilters ? View.VISIBLE : View.GONE);
        binding.chkApplyFilter.setEnabled(sessionObject == null);
        binding.chkFilterApplied.setEnabled(sessionObject == null);

        binding.chkApplyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.dismissCustomAlertDialog();
                if (sessionObject == null && binding.spinInventoryStartLocation.validate() && binding.spinInventoryStartBrand.validate()) {
                    if (!isAllowAdvanceFiltersForMultiBrands && (binding.spinInventoryStartBrand.getSelectedVals().size() > 1 || binding.spinInventoryStartBrand.getSelectedItem().contains(",") || (binding.spinInventoryStartBrand.getSelectedItem().matches("((?i)" + AppConstants.ALL + ")")) && binding.spinInventoryStartBrand.getListObjectSize() > 1))
                        context.showCustomErrDialog(R.string.err_filter);
                    else if (isNonEmpty(selFilterBrand) && binding.spinInventoryStartBrand.getSelectedItem().matches(selFilterBrand) && filtersVals != null) {
                        showFilterDialog(filtersVals);
                    } else {
                        try {
                            selFilterBrand = "";
                            selFilterZone = "";
                            filtersVals = null;
                            inventoryCount = -1L;
                            filters.clear();
                            JSONObject requestParams = new JSONObject();
                            requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
                            requestParams.put(ParamConstants.TYPE, AppConstants.BRAND_STOCK);
                            final Set<String> selZones = sessionObject != null ? new HashSet<String>(Arrays.asList(sessionObject.zone.split(","))) : binding.spinInventoryStartLocation.getSelectedVals();
                            final String selZone = sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL);
                            if (selZone.equalsIgnoreCase(AppConstants.ALL)) {
                                requestParams.put(ParamConstants.ZONE_ID, 0);
                                requestParams.put(ParamConstants.ZONE, selZone);
                            } else {
                                List<Zone> listZones = selZone.contains(",") ? AppDatabase.getZoneDao(context).getZoneByName(selZones) : AppDatabase.getZoneDao(context).getZoneByName(selZone);
                                if (isNonEmpty(listZones) && listZones.size() == 1) {
                                    final Zone zone = listZones.get(0);
                                    requestParams.put(ParamConstants.ZONE_ID, zone != null ? zone.zoneId : 0);
                                    requestParams.put(ParamConstants.ZONE, zone != null ? zone.zoneName : sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL));
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
                            final String brands = sessionObject != null ? sessionObject.brands : chkNull(binding.spinInventoryStartBrand.getAllItems(), chkNull(binding.spinInventoryStartBrand.getSelectedItem(), AppConstants.ALL));
                            requestParams.put(ParamConstants.BRANDS, brands);
                            callWebService(URLConstants.GET_BRAND_INVENTORY_FILTERS, requestParams, getString(R.string.progress_msg_getting_data), false, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        return binding.getRoot();
    }
    
   @Override
   protected void onSpinInventoryLocationItemSelected(){
        if (isAllowAdvanceFilters && isNonEmpty(selFilterBrand) && isNonEmpty(selFilterZone) && !binding.spinInventoryStartLocation.getSelectedItem().equalsIgnoreCase(selFilterZone)) {
            selFilterBrand = "";
            selFilterZone = "";
            filtersVals = null;
            inventoryCount = -1L;
            filters.clear();
            brandEanDao.deleteAll();
            binding.chkFilterApplied.setChecked(false);
            isFilterApplied = false;
        }
        super.onSpinInventoryLocationItemSelected();
    }

    @Override
    protected void onBtnStartClick() {
        if (advFilterDialog != null && advFilterDialog.isShowing()) return;
        super.onBtnStartClick();
    }

    @Override
    protected void onBtnUploadSwiped() {
        showBrandInventoryUploadSummary();
    }
    

    /**
     * Show brand inventory upload summary.
     */
    private void showBrandInventoryUploadSummary() {
        List<BrandEans> listBrandEans = brandEanDao.getBrandwiseCount();
        MaxHeightRecyclerView listDisplayData = isNonEmpty(listBrandEans) ? new MaxHeightRecyclerView(context) : null;
        if (listDisplayData != null) {
            int margin = getResources().getDimensionPixelSize(R.dimen.dp_15);
            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            llParams.setMargins(margin, 0, margin, 0);
            listDisplayData.setLayoutParams(llParams);
            listDisplayData.setPadding(margin, 0, margin, 0);
            listDisplayData.setAdapter(new InvBrandwiseQtyAdapter((MainActivity) context, this, listBrandEans));
            listDisplayData.setLayoutManager(new LinearLayoutManager(context));
        }
        context.showCustomAlertDialog(getString(R.string.title_summary), "", listDisplayData, R.string.btn_upload, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                context.showCustomConfirmDialog(getString(R.string.title_inventory_upload), R.string.btn_upload, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        apiCall(AppConstants.SESSION_ACTION_UPLOAD);
                    }
                });
            }
        }, R.string.btn_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                context.showCustomConfirmDialog(getString(R.string.title_back_session_save), R.string.btn_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        apiCall(AppConstants.SESSION_ACTION_SAVE);
                    }
                });
            }
        });
    }

    @Override
    protected void isSessionOnChanged(final Boolean isInventorySessionOn) {
        super.isSessionOnChanged(isInventorySessionOn);
        boolean isInvSessionOn = chkNotNullTrue(isInventorySessionOn);

        binding.spinInventoryStartBrand.setEnabled(!isInvSessionOn);
        binding.chkApplyFilter.setEnabled(!isInvSessionOn);
        binding.chkFilterApplied.setEnabled(!isInvSessionOn);
    }

    @Override
    protected void onDataSizeChanged(Integer size) {
        super.onDataSizeChanged(size);
    }

    @Override
    protected boolean validateBeforeInvSessionStart() {
        return super.validateBeforeInvSessionStart() && binding.spinInventoryStartBrand.validate();
    }

    @Override
    public void apiCall(String action) {
        final boolean isUpload = action.equalsIgnoreCase(AppConstants.SESSION_ACTION_UPLOAD);
        if (isUseAPICallForSessionInventory && isInternetConnected(context, false, isUpload || (sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)))) {
            try {
                if (isUpload)
                    showProgressDialog(getString(R.string.progress_msg_check_upload_data));
                //Send Empty Array in JSON Request if value is 'All'
                JSONObject requestParams = new JSONObject();
                requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
                requestParams.put(ParamConstants.TYPE, AppConstants.BRAND_STOCK);
                final Set<String> selZones = sessionObject != null ? new HashSet<String>(Arrays.asList(sessionObject.zone.split(","))) : binding.spinInventoryStartLocation.getSelectedVals();
                final String selZone = sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL);
                if (selZone.equalsIgnoreCase(AppConstants.ALL)) {
                    requestParams.put(ParamConstants.ZONE_ID, 0);
                    requestParams.put(ParamConstants.ZONE, selZone);
                }
                else {
                    List<Zone> listZones = selZone.contains(",") ? AppDatabase.getZoneDao(context).getZoneByName(selZones) : AppDatabase.getZoneDao(context).getZoneByName(selZone);
                    if (isNonEmpty(listZones) && listZones.size() == 1) {
                        final Zone zone = listZones.get(0);
                        requestParams.put(ParamConstants.ZONE_ID, zone != null ? zone.zoneId : 0);
                        requestParams.put(ParamConstants.ZONE, zone != null ? zone.zoneName : sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL));
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
                String brands = "";
                if (isUpload) {
                    for (String brand : brandEanDao.getScanBrands()) {
                        brands += "," + brand.trim();
                    }
                    brands = (brands.startsWith(",") ? brands.substring(1) : brands).trim();
                }
                else
                    brands = sessionObject != null ? sessionObject.brands : chkNull(binding.spinInventoryStartBrand.getAllItems(), chkNull(binding.spinInventoryStartBrand.getSelectedItem(), AppConstants.ALL));
                requestParams.put(ParamConstants.BRANDS, brands);
                if (isAllowAdvanceFilters && (sessionObject != null && isNonEmpty(sessionObject.extras)) || (isNonEmpty(filters) && !isAllNullFilters)) {
                    try {
                        JSONObject jsonFilters = new JSONObject((sessionObject != null && isNonEmpty(sessionObject.extras) ? sessionObject.extras : isNonEmpty(filters) ? filters.toString() : "").replaceAll("\\{", "{\"").replaceAll("\\]\\}", "\"}").replaceAll("=\\[", "\":\"").replaceAll("\\], ", "\", \"").replaceAll("\\[", "\"").replaceAll("\"" + AppConstants.ALL + "\"", "null").trim());
                        if (isNonEmpty(jsonFilters))
                            requestParams.put(ParamConstants.FILTERS, jsonFilters);
                    } catch (Exception e) {
                        //Don't handle
                    }
                }
                requestParams.put(ParamConstants.HAS_EANS, brandEanDao.hasData());
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
                                    callWebService(URLConstants.UPLOAD_INVENTORY, requestParams, getString(R.string.progress_msg_uploading_data), !isUpload && !(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    hideProgressDialog();
                                }
                            }
                        });
                    }
                }
                if (!isUpload)
                    callWebService(isUpload ? URLConstants.UPLOAD_INVENTORY : URLConstants.SET_SESSION, requestParams, isUpload, isUpload ? getString(R.string.progress_msg_uploading_data) : action.matches("(?i)(" + AppConstants.SESSION_ACTION_START + "|" + AppConstants.SESSION_ACTION_RESUME + ")") ? action + "ing Session...\nPlease wait..." : action + "ing Data...\nPlease wait...", !isUpload && !(sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START)), !isUpload && sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START));
            } catch (JSONException e) {
                e.printStackTrace();
                if (!isUpload)
                    setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
                else hideProgressDialog();
            }
        }
        else if (!(isUpload || (sessionObject == null && action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START))))
            setSessionAction(action, sessionObject != null ? sessionObject.sessionId : null);
    }

    /**
     * Get inv count string.
     *
     * @return the string
     */
    public String getInvCount() {
        try {
            if (sessionObject != null) return sessionObject.total;
            if (chkNull(inventoryCount, -1L) >= 0 || isFilterApplied)
                return inventoryCount == 0 ? "-" : String.valueOf(inventoryCount);
            final String zone = getSelectedZone();
            final Set<String> zones = getSelectedZones();
            final String brand = binding != null && binding.spinInventoryStartBrand != null ? chkNull(binding.spinInventoryStartBrand.getSelectedItem(), AppConstants.ALL) : AppConstants.ALL;
            final Set<String> brands = binding != null && binding.spinInventoryStartBrand != null ? binding.spinInventoryStartBrand.getSelectedVals() : null;
            if (brandWiseZoneInventoryDao == null || !brandWiseZoneInventoryDao.hasData()) return "-";
            //might be temp condition
            if (isNullOrEmpty(brands) && (binding == null || binding.spinInventoryStartBrand == null || isNullOrEmpty(binding.spinInventoryStartBrand.getSelectedItem()))) return "-";
            else return chkZero(brandWiseZoneInventoryDao.getInvCount(zone, zones, brand, brands), "-");
        } catch (Exception e) {
            e.printStackTrace();
            AppCommonMethods.showLog("Inv_DB",e.getMessage());
            return "-";
        }
    }

    @Override
    protected String getBrandsForSession() {
        return binding.spinInventoryStartBrand.getSelectedItem();
    }

    @Override
    protected String getExtrasForSession() {
        return isNonEmpty(filters) ? filters.toString() : "";
    }

    
    /*@Override
    public void setSessionAction(String action, String sessionId, String sessionTime, Long inventoryCount, Integer activeUserCount) {
        super.setSessionAction(action, sessionId, sessionTime, inventoryCount, activeUserCount);
    }*/

    void showFilterDialog(final JSONObject jsonResponse) {
        final boolean isMultiSelect = extractBoolean(jsonResponse, ParamConstants.IS_MULTI_SELECT);
        final JSONObject jsonFilters = extractJSONObject(jsonResponse, ParamConstants.FILTERS, jsonResponse);
        AppCommonMethods.showLog("keys_jsonFilters", "" + jsonFilters.length());
        final Iterator<String> keys = jsonFilters.keys();
        if (advFilterDialog != null && advFilterDialog.isShowing()) advFilterDialog.dismiss();
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialog).create();
        alertDialog.setCancelable(false);
        setAlertDialogCustomTitle(alertDialog, R.string.title_set_filter);
        final int margin = getResources().getDimensionPixelSize(R.dimen.dp_15);
        final LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        llParams.setMargins(margin, 0, margin, 0);
        final LinearLayout llMain = new LinearLayout(context);
        llMain.setOrientation(LinearLayout.VERTICAL);
        llMain.setLayoutParams(llParams);
        llMain.setPadding(margin, 0, margin, 0);
        int counter = 0;
        final int rowLimit = ((jsonFilters.length() - 1) > 9 ? 3 : 2) + (isLandscape ? 1 : 0);
        showLog("keys_rowLimit", "" + rowLimit);
        LinearLayout llRow = null;
        while (keys.hasNext()) {
            final String key = keys.next();
            try {
                final JSONObject jsonObject = extractJSONObject(jsonFilters, key);
                final JSONArray jsonArray = extractJSONArray(jsonObject, ParamConstants.ITEMS, extractJSONArray(jsonFilters, key));
                final String displayKey = extractString(jsonObject, ParamConstants.LABEL, key);
                final List<String> listVals = new ArrayList<String>(0);
                boolean isLastRowSet = false;
                if (isNonEmpty(jsonArray)) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        final Object obj = jsonArray.get(i);
                        final String val = obj != null ? obj.toString().trim() : "";
                        if (isNonEmpty(val)) listVals.add(val);
                    }
                    if (isNonEmpty(listVals)) {
                        if (!listVals.contains(AppConstants.ALL)) listVals.add(0, AppConstants.ALL);
                        final HeaderSpinner headerSpinner = new HeaderSpinner(context);
                        final LinearLayout.LayoutParams headSpinParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                        headSpinParams.gravity = Gravity.CENTER;
                        headerSpinner.setId(111 + (++counter));
                        headerSpinner.setLayoutParams(headSpinParams);
                        headerSpinner.setBackgroundResource(R.drawable.border_light);
                        headerSpinner.setLabel(displayKey);//toTitleCase(key));
                        headerSpinner.setTag(key);//toTitleCase(key));
                        headerSpinner.setAdapter(listVals, isMultiSelect);
                        if (isNonEmpty(filters) && filters.containsKey(key) && isNonEmpty(filters.get(key)) && !filters.get(key).replaceAll("(\\[|\\])", "").trim().matches("(?i)("+AppConstants.ALL+")")) {
                            headerSpinner.setSelection(new HashSet<String>(Arrays.asList(filters.get(key).replaceAll("\\s*,\\s*", ",").replaceAll("(\\[|\\])", "").trim().split(","))));
                        }
                        if (llRow == null || (counter - 1) % rowLimit == 0) {//((counter - 1) % rowLimit == 0 && keys.hasNext())){
                            if (llRow != null && llRow.getChildCount() >= rowLimit)
                                llMain.addView(llRow);
                            llRow = new LinearLayout(context);
                            llRow.setOrientation(LinearLayout.HORIZONTAL);
                            llRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        }
                        llRow.addView(headerSpinner);
                        if (!keys.hasNext()) {
                            isLastRowSet = true;
                            llMain.addView(llRow);
                        }
                    }
                }
                if (!isLastRowSet && llRow != null && llRow.getChildCount() > 0 && !keys.hasNext())
                    llMain.addView(llRow);
                else if (isLastRowSet) isLastRowSet = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (llMain != null && llMain.getChildCount() > 0) {
            alertDialog.setView(llMain);
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.btn_apply), (DialogInterface.OnClickListener) null);
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    final Button posBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    posBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            filters.clear();
                            isAllNullFilters = true;
                            updateFilters(llMain);
                            AppCommonMethods.showLog("filters", filters.toString());
                            try {
                                //Send Empty Array in JSON Request if value is 'All'
                                JSONObject requestParams = new JSONObject();
                                requestParams.put(ParamConstants.SESSION_TYPE, getSessionType().name());
                                requestParams.put(ParamConstants.TYPE, AppConstants.BRAND_STOCK);
                                final Set<String> selZones = sessionObject != null ? new HashSet<String>(Arrays.asList(sessionObject.zone.split(","))) : binding.spinInventoryStartLocation.getSelectedVals();
                                final String selZone = sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL);
                                if (selZone.equalsIgnoreCase(AppConstants.ALL)) {
                                    requestParams.put(ParamConstants.ZONE_ID, 0);
                                    requestParams.put(ParamConstants.ZONE, selZone);
                                } else {
                                    List<Zone> listZones = selZone.contains(",") ? AppDatabase.getZoneDao(context).getZoneByName(selZones) : AppDatabase.getZoneDao(context).getZoneByName(selZone);
                                    if (isNonEmpty(listZones) && listZones.size() == 1) {
                                        final Zone zone = listZones.get(0);
                                        requestParams.put(ParamConstants.ZONE_ID, zone != null ? zone.zoneId : 0);
                                        requestParams.put(ParamConstants.ZONE, zone != null ? zone.zoneName : sessionObject != null ? sessionObject.zone : chkNull(binding.spinInventoryStartLocation.getSelectedItem(), AppConstants.ALL));
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
                                final String brands = sessionObject != null ? sessionObject.brands : chkNull(binding.spinInventoryStartBrand.getAllItems(), chkNull(binding.spinInventoryStartBrand.getSelectedItem(), AppConstants.ALL));
                                requestParams.put(ParamConstants.BRANDS, brands);
                                selFilterBrand = brands;
                                selFilterZone = selZone;
                                if ((sessionObject != null && isNonEmpty(sessionObject.extras)) || (isNonEmpty(filters) && !isAllNullFilters)) {
                                    try {
                                        JSONObject jsonFilters = new JSONObject((sessionObject != null && isNonEmpty(sessionObject.extras) ? sessionObject.extras : isNonEmpty(filters) ? filters.toString() : "").replaceAll("\\{", "{\"").replaceAll("\\]\\}", "\"}").replaceAll("=\\[", "\":\"").replaceAll("\\], ", "\", \"").replaceAll("\\[", "\"").replaceAll("\"" + AppConstants.ALL + "\"", "null").trim());
                                        if (isNonEmpty(jsonFilters))
                                            requestParams.put(ParamConstants.FILTERS, jsonFilters);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else
                                    requestParams.put(ParamConstants.FILTERS, new JSONObject());//JSONObject.NULL);
                                callWebService(URLConstants.SET_BRAND_INVENTORY_FILTERS, requestParams, getString(R.string.progress_msg_apply_filter), false, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    advFilterDialog = alertDialog;
                }
            });
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    advFilterDialog = null;
                }
            });
            alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    context.handleTriggerKeyEvent(keyCode, event);
                    return false;
                }
            });
            Window window = alertDialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.TOP;
            if (isLandscape) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                final int columns = 2;
                final int width = 0;
                wlp.width = width;
            }
            alertDialog.show();
        }
        hideProgressDialog();
    }

    void updateFilters(final LinearLayout llMain) {
        for (int i = 0; i < llMain.getChildCount(); i++) {
            final View view = llMain.getChildAt(i);
            if (view != null && view instanceof HeaderSpinner hs) {
                final String val = (isNonEmpty(hs.getSelectedVals()) && !hs.getSelectedVals().contains(AppConstants.ALL) ? hs.getSelectedVals().toString() : chkNull(hs.getSelectedItem(), AppConstants.ALL)).trim();
                final String tag = chkNull(hs.getTag() != null && hs.getTag() instanceof String ? hs.getTag().toString() : "", "").trim();
                AppCommonMethods.showLog("val", val);
                //Remove/Don't Put Filter if value is 'All'
                if (!val.equalsIgnoreCase(AppConstants.ALL)) isAllNullFilters = false;
                filters.put(chkNull(tag, toUnderScoreCase(hs.getLabel().trim())), !val.matches("^\\[.*\\]$") ? "[" + val + "]" : val);
            } else if (view != null && view instanceof LinearLayout llRow) {
                updateFilters(llRow);
            }
        }
    }

    public void updateInvCount() {
        if (!chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()))
            if (ctwInventoryStart != null) ctwInventoryStart.setTotal(getInvCount());
    }

    @Override
    public void handleResponse(String url, JSONObject jsonRequest, JSONObject jsonResponse, Integer responseCode, boolean isSuccess, Bundle args) {
        super.handleResponse(url, jsonRequest, jsonResponse, responseCode, isSuccess, args);
        try {
            switch (url) {
                case URLConstants.GET_INVENTORY_DASHBOARD:
                    if (isSuccess && jsonResponse != null) {
                        final String lastInventoryDate = extractString(jsonResponse, ParamConstants.LAST_INVENTORY_TAKEN_TIME, "T").replaceFirst("T", " ").trim();
                        SharedPrefManager.setString(SharedPrefManager.SharedPrefKeys.INVENTORY_TAKEN_DATE_TIME, lastInventoryDate);
                        if (isNonEmpty(lastInventoryDate) && lastInventoryDate.trim().contains(" ") && lastInventoryDate.length() >= 5) {
                            context.setTooltipText(binding.imgInventoryStartInfo, HtmlCompat.fromHtml(isNonEmpty(lastInventoryDate) && lastInventoryDate.trim().contains(" ") && lastInventoryDate.trim().split(" ").length > 1 && lastInventoryDate.length() >= 5 ? String.format(getString(R.string.txt_inventory_date_time), lastInventoryDate.split(" ")[0], lastInventoryDate.split(" ")[1]) : "", HtmlCompat.FROM_HTML_MODE_LEGACY));
                        }
                        JSONArray inventoryCounts = extractJSONArray(jsonResponse, ParamConstants.BRAND_INVENTORY_COUNTS, extractJSONArray(jsonResponse, ParamConstants.CATEGORY_INVENTORY_COUNTS, extractJSONArray(jsonResponse, ParamConstants.INVENTORY_COUNTS)));
                        if (inventoryCounts != null && context instanceof MainActivity)
                            ((MainActivity) context).callInsertBrandwiseDBTask(this, url, jsonRequest, jsonResponse, args, inventoryCounts);
                        else {
                            hideProgressDialog();
                            updateInvCount();
                        }
                    }
                    break;
                case URLConstants.GET_BRAND_INVENTORY_FILTERS:
                    if (isSuccess) {
                        showProgressDialog(getString(R.string.progress_msg_check_data));
                        filtersVals = jsonResponse;
                        showFilterDialog(jsonResponse);
                    } else {
                        hideProgressDialog();
                        filters.clear();
                        filtersVals = null;
                    }
                    break;
                case URLConstants.SET_BRAND_INVENTORY_FILTERS:
                    if (isSuccess) {
                        try {
                            inventoryCount = extractLong(jsonResponse, ParamConstants.INVENTORY_COUNT, Long.parseLong(chkNull(this.getInvCount(), "0").replace("-", "0")));
                            activeUsers = extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, activeUsers);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
                        if (isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())) {
                            hideProgressDialog();
                            context.showCustomErrDialog(errMsg);
                        } else {
                            JSONArray jsonBrandEans = extractJSONArray(jsonResponse, ParamConstants.BRAND_EANS);
                            if (isNonEmpty(jsonBrandEans)) {
                                brandEanDao.deleteAll();
                                new InsertDBBrandEANs(context, this, url).execute(jsonBrandEans);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleResponseSetSession(String url, JSONObject jsonRequest, JSONObject jsonResponse, boolean isSuccess, Bundle args) {
        final String selBrand = binding.spinInventoryStartBrand.getSelectedItem();
        final boolean isSingleBrand = binding.spinInventoryStartBrand.getSelectedVals().size() == 1;
        final String action = extractString(jsonRequest, ParamConstants.ACTION, sessionObject == null ? AppConstants.SESSION_ACTION_START : AppConstants.SESSION_ACTION_STOP);
        final String sessionId = isSuccess && jsonResponse != null ? extractString(jsonResponse, ParamConstants.SESSION_ID) : null;
        final String sessionTime = isSuccess && jsonResponse != null ? extractString(jsonResponse, ParamConstants.SESSION_TIME) : null;
        if (isSuccess && jsonResponse != null) {
            sessionValidTill = extractInt(jsonResponse, ParamConstants.SESSION_VALID_TILL, 48);
            inventoryCount = extractLong(jsonResponse, ParamConstants.INVENTORY_COUNT, Long.parseLong(chkNull(this.getInvCount(), "0").replace("-", "0")));
            activeUsers = extractInt(jsonResponse, ParamConstants.ACTIVE_USERS, activeUsers);
            boolean hasEans = !action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) && sessionObject != null || extractBoolean(jsonRequest, ParamConstants.HAS_EANS, false);
            JSONArray jsonBrandEans = hasEans ? null : extractJSONArray(jsonResponse, ParamConstants.BRAND_EANS);
            final String errMsg = extractString(jsonResponse, ParamConstants.ERR_MSG, extractString(jsonResponse, ParamConstants.ERROR, ""));
            if (isNonEmpty(errMsg) && !errMsg.equalsIgnoreCase(Boolean.FALSE.toString())) {
                if (!hasEans) hideProgressDialog();
                context.showCustomErrDialog(errMsg);
            } else {
                final JSONArray excludeEpcs = extractJSONArray(jsonResponse, ParamConstants.EXCLUDE_INVENTORY_EPCS);
                if (isNonEmpty(excludeEpcs)) extractIgnoreEpcs(excludeEpcs);
                /*if (isNonEmpty(jsonBrandEans)) {
                    eans.clear();
                    for (int i = 0; i < jsonBrandEans.length(); i++) {
                        try {
                            eans.addAll(Arrays.asList(AppCommonMethods.extractString(jsonBrandEans.getJSONObject(i), ParamConstants.EANS, "").replaceAll("(\"|\\[|\\]|,null|null,)", "").replaceAll("\\s*,\\s*", ",").trim().toUpperCase().split(",")));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }*/
                if (isNonEmpty(jsonBrandEans))
                    new InsertDBBrandEANs(context, this, url, action, sessionId, sessionTime, inventoryCount).execute(jsonBrandEans);
                else if (!action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) || brandEanDao.hasData()) {//isNonEmpty(eans))
                    if (action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) && brandEanDao.hasData() && isSingleBrand && inventoryCount>0)
                        brandEanDao.updateTotalQty(selBrand, inventoryCount.intValue());
                    setSessionAction(action, sessionId, sessionTime, inventoryCount, activeUsers);
                    hideProgressDialog();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public FragmentInventoryBrandBinding getBinding() {
        return binding;
    }

    public void onPostExecute(final String url, final String action, final String sessionId, final String sessionTime, final Boolean result) {
        hideProgressDialog();
        final String selZone = binding.spinInventoryStartLocation.getSelectedItem();
        final String selBrand = binding.spinInventoryStartBrand.getSelectedItem();
        final Set<String> selZones = binding.spinInventoryStartLocation.getSelectedVals();
        final Set<String> selBrands = binding.spinInventoryStartBrand.getSelectedVals();
        final boolean isSingleBrand = selBrands.size() == 1;
        if (result) {
            if (url.equalsIgnoreCase(URLConstants.SET_BRAND_INVENTORY_FILTERS)) {
                if (advFilterDialog != null && advFilterDialog.isShowing())
                    advFilterDialog.dismiss();
                isFilterApplied = true;
                binding.chkFilterApplied.setChecked(true);
                setActiveUsers(activeUsers);
                String totInvCount = sessionObject != null ? sessionObject.total : chkNull(inventoryCount, -1L) >= 0 || isFilterApplied ? chkZero(inventoryCount, "-") : this.getInvCount();
                showLog("totInvCount", totInvCount);
                if (ctwInventoryStart != null) ctwInventoryStart.setTotal(getInvCount());
            }
        }
        if (url.equalsIgnoreCase(URLConstants.SET_SESSION) && (!action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) || brandEanDao.hasData())) {//isNonEmpty(eans))
            if (action.equalsIgnoreCase(AppConstants.SESSION_ACTION_START) && brandEanDao.hasData() && isSingleBrand && inventoryCount>0)
                brandEanDao.updateTotalQty(selBrand, inventoryCount.intValue());
            setSessionAction(action, sessionId, sessionTime, inventoryCount, activeUsers);
        }
    }
}