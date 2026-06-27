package com.itek.retail.common;

import static com.itek.retail.common.AppCommonMethods.allowBtnClick;
import static com.itek.retail.common.AppCommonMethods.chkNotNullFalse;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.chkZero;
import static com.itek.retail.common.AppCommonMethods.extractBoolean;
import static com.itek.retail.common.AppCommonMethods.extractInt;
import static com.itek.retail.common.AppCommonMethods.extractString;
import static com.itek.retail.common.AppCommonMethods.isAllowSessionSaveForDecode;
import static com.itek.retail.common.AppCommonMethods.isAllowSessionSaveForEncode;
import static com.itek.retail.common.AppCommonMethods.isAllowSessionSaveForInventory;
import static com.itek.retail.common.AppCommonMethods.isAllowSessionSaveForThanEncode;
import static com.itek.retail.common.AppCommonMethods.isDebugApp;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isUseDirectionalSearch;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.databinding.HeaderTitleLayoutBinding;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.SearchLog;
import com.itek.retail.reader.RetailDiagnosticLogger;
import com.itek.retail.ui.customviews.InputView;
import com.itek.retail.ui.customviews.PowerView;
import com.itek.retail.ui.customviews.SearchView;
import com.itek.retail.ui.customviews.speedviewlib.PointerSpeedometer;
import com.itek.retail.ui.decoding.DecodingStartFragment;
import com.itek.retail.ui.encoding.EncodingScanScanWriteFragment;
import com.itek.retail.ui.encoding.EncodingStartFragment;
import com.itek.retail.ui.home.MainActivity;
import com.itek.retail.ui.home.MainViewModel;
import com.itek.retail.ui.inventory.InventoryAddFragment;
import com.itek.retail.ui.inventory.InventoryBrandFragment;
import com.itek.retail.ui.inventory.InventoryFilterFragment;
import com.itek.retail.ui.inventory.InventoryStartFragment;
import com.itek.retail.ui.inventory.stockcorrection.StockCorrectionStartFragment;
import com.itek.retail.ui.inward.grn.InwardGrnStartFragment;
import com.itek.retail.ui.inward.tote.InwardToteStartFragment;
import com.itek.retail.ui.inward1.HuProcessStartFragment;
import com.itek.retail.ui.movement.MovementStartFragment;
import com.itek.retail.ui.movement.replenishment.ReplenishmentStartFragment;
import com.itek.retail.ui.outward.OutwardPickStartFragment;
import com.itek.retail.ui.outward.huverification.OutwardHuStartFragment;
import com.itek.retail.ui.outward.offrange.OffRangeListFragment;
import com.itek.retail.ui.outward.offrange.OffRangeStartFragment;
import com.itek.retail.ui.outward.tote.OutwardToteDCStartFragment;
import com.itek.retail.ui.outward.tote.OutwardToteStartFragment;
import com.itek.retail.ui.replenishondemand.ReplenishmentEanStartFragment;
import com.itek.retail.ui.scancount.ScanCountFragment;
import com.itek.retail.ui.search.ageing.AgeingSearchStartFragment;
import com.itek.retail.ui.search.alien.SearchAlienFragment;
import com.itek.retail.ui.search.assortment.SearchAssortStartFragment;
import com.itek.retail.ui.search.fifo.SearchFIFOStartFragment;
import com.itek.retail.ui.search.filesearch.SearchFileBasedFragment;
import com.itek.retail.ui.search.listnewsearch.SearchListExcelFragment;
import com.itek.retail.ui.search.listnewsearch.SearchListExcelStartFragment;
import com.itek.retail.ui.search.listsearch.SearchListStartFragment;
import com.itek.retail.ui.search.omnichannel.OmniChannelStartFragment;
import com.itek.retail.ui.search.productsearch.ProductSearchDetailsFragment;
import com.itek.retail.ui.search.unencoded.SearchUnencodedFragment;
import com.itek.retail.ui.than.ThanEncodingFragment;

import org.json.JSONArray;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The Rfid session fragment.
 */
public class RFIDSessionFragment extends CommonFragment {

    private static final DecimalFormat df = new DecimalFormat("00.00");
    private static final long INVENTORY_UI_UPDATE_INTERVAL_MS = 500L;
    protected MainViewModel mainViewModel;
    protected RFIDSession sessionObject;
    protected Long searchStartTime = 0L;
    //changes for common search
    protected Timer countChangeTimer;
    protected int oldPercentage = 0;
    protected String oldRssi = "";
    protected boolean removePercentage = false;
    protected SearchLog searchLog;
    protected boolean isShowArrow = false;
    protected boolean isAllowDirectionalSearch = false;
    protected ArrayList<String> listIgnoreEpcs = new ArrayList<>(0);
    protected int activeUsers = -2;
    protected int sessionValidTill = 48;
    boolean isDiscardData = false;
    int size = 0;
    int sizeNonUploadedCount = 0;
    int chainwaySearchRemovalPercentCount = 0;
    private boolean isTripInventory = false;
    private final AppCommonMethods.SessionType sessionType = getType();
    private final boolean isInvSession = sessionType != null && (sessionType == AppCommonMethods.SessionType.INVENTORY || sessionType == AppCommonMethods.SessionType.ADD_INVENTORY || sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY || sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY);
    private final int currentPercentageThreshold = 0;
    //temp code
    private View searchView;
    private ImageView imgSearchDir;
    private String[] referenceArray = {"0", "0", "0", "0", "0"};
    private String[] currentArray = {"0", "0", "0", "0", "0"};
    private int resetCoordinates = 0;
    private final int threshold_angle_to_go_straight = 9;
    private ScheduledThreadPoolExecutor threadPool;
    //private Timer threadPool = null;
    private Timer inventoryTimer = null;
    private int resetDirectionArrayCounterLimit = AppCommonMethods.searchResetCounterOnZero;
    private int resetDirectionArrayCounter = 0;
    private HeaderTitleLayoutBinding header;
    private final Handler inventoryUiHandler = new Handler(Looper.getMainLooper());
    private Integer pendingInventoryUiSize = null;
    private long lastInventoryUiUpdateMs = 0L;
    private boolean isInventoryUiUpdateScheduled = false;
    private final Runnable inventoryUiUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            dispatchInventorySizeChanged();
        }
    };


    /**
     * Instantiates a new Rfid session fragment.
     */
    public RFIDSessionFragment() {
        /*
         * This method will be called in respective child fragments*/
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainViewModel = ((MainActivity) context).getRfidViewModel();
        isAllowDirectionalSearch = SharedPrefManager.getBoolean(ParamConstants.IS_USE_DIRECTIONAL_SEARCH, isUseDirectionalSearch) && SharedPrefManager.getIsSensorAvailable();

        if (sessionType.getValue() > 0) {
            final boolean isAllowDecode = extractBoolean(getArguments(), AppConstants.IS_ALLOW_DECODE, extractBoolean(getArguments(), ParamConstants.IS_ALLOW_DECODE, false));
            isDiscardData = sessionType.getValue() >= 4 && (sessionType.getValue() <= 9 || sessionType.getValue() >= 14);
            if (sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.OUTWARD_PICK || isAllowDecode)
                isDiscardData = false;

            resetDirectionArrayCounterLimit = SharedPrefManager.getInt(ParamConstants.SEARCH_RESET_COUNTER, AppCommonMethods.searchResetCounterOnZero);
            showLog(sessionType.name() + " isDiscard", "" + isDiscardData);
            setSessionObject(mainViewModel.updateActiveSessionFlag(sessionType, true));
            mainViewModel.getReaderUHFInstance(sessionType);
            showLog(sessionType.name() + " isDiscard", sessionType.name());
            AppCommonMethods.logInFile(context, "-----------------------------------\n" + sessionType.name());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActiveUsersAndSessionValidTill();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected void getActiveUsersAndSessionValidTill() {
        activeUsers = extractInt(getArguments(), AppConstants.ACTIVE_USERS, -2);
        sessionValidTill = extractInt(getArguments(), AppConstants.SESSION_VALID_TILL, 48);
    }


    /**
     * Get type app common methods . session type.
     *
     * @return the app common methods . session type
     */
    private AppCommonMethods.SessionType getType() {
        if (this instanceof EncodingStartFragment || this instanceof EncodingScanScanWriteFragment)
            return AppCommonMethods.SessionType.ENCODING;
        else if (this instanceof ThanEncodingFragment)
            return AppCommonMethods.SessionType.ENCODING_THAN;
        else if (this instanceof DecodingStartFragment)
            return AppCommonMethods.SessionType.DECODING;
        else if (this instanceof InventoryStartFragment) return AppCommonMethods.SessionType.INVENTORY;
    else if(this instanceof InventoryAddFragment) return AppCommonMethods.SessionType.ADD_INVENTORY;
        else if (this instanceof InventoryBrandFragment) return AppCommonMethods.SessionType.BRAND_INVENTORY;
    else if(this instanceof InventoryFilterFragment) return AppCommonMethods.SessionType.FILTER_INVENTORY;
        else if (this instanceof StockCorrectionStartFragment)
            return AppCommonMethods.SessionType.STOCK_CORRECTION;
        else if (this instanceof ReplenishmentStartFragment)
            return AppCommonMethods.SessionType.REPLENISHMENT;
        else if (this instanceof MovementStartFragment)
            return AppCommonMethods.SessionType.MOVEMENT;
        else if (this instanceof ProductSearchDetailsFragment)
            return AppCommonMethods.SessionType.SEARCH;
        else if (this instanceof SearchListStartFragment)
            return AppCommonMethods.SessionType.SEARCH_LIST;
        else if (this instanceof SearchAssortStartFragment)
            return AppCommonMethods.SessionType.SEARCH_ASSORTMENT;
        else if (this instanceof SearchUnencodedFragment)
            return AppCommonMethods.SessionType.SEARCH_UNENCODED;
        else if (this instanceof SearchAlienFragment)
            return AppCommonMethods.SessionType.SEARCH_ALIEN;
        else if (this instanceof SearchFileBasedFragment)
            return AppCommonMethods.SessionType.SEARCH_FILE;
        else if (this instanceof AgeingSearchStartFragment)
            return AppCommonMethods.SessionType.SEARCH_AGEING;
        else if (this instanceof SearchFIFOStartFragment)
            return AppCommonMethods.SessionType.SEARCH_FIFO;
        else if (this instanceof OmniChannelStartFragment)
            return AppCommonMethods.SessionType.OMNICHANNEL;
        else if (this instanceof InwardGrnStartFragment) {
            isTripInventory = true;
            return AppCommonMethods.SessionType.INWARD;
        } else if (this instanceof OutwardPickStartFragment)
            return AppCommonMethods.SessionType.OUTWARD_PICK;
        else if (this instanceof OutwardHuStartFragment) {
            isTripInventory = true;
            return AppCommonMethods.SessionType.OUTWARD;
        } else if (this instanceof OutwardToteStartFragment)
            return AppCommonMethods.SessionType.OUTWARD_TOTE;
        else if (this instanceof OutwardToteDCStartFragment) {
            isTripInventory = true;
            return AppCommonMethods.SessionType.OUTWARD_TOTE;
        }
        else if (this instanceof InwardToteStartFragment)
            return AppCommonMethods.SessionType.INWARD_TOTE;
        else if (this instanceof OffRangeListFragment || this instanceof OffRangeStartFragment)
            return AppCommonMethods.SessionType.OFF_RANGE;
        else if (this instanceof ScanCountFragment) return AppCommonMethods.SessionType.SCAN;
        else if (this instanceof HuProcessStartFragment) {
            isTripInventory = true;
            return !extractString(getArguments(), ParamConstants.TYPE, extractString(getArguments(), ParamConstants.OPERATION_TYPE, AppConstants.INWARD)).equalsIgnoreCase(AppConstants.INWARD) ? AppCommonMethods.SessionType.OUTWARD : AppCommonMethods.SessionType.INWARD;
            //return AppCommonMethods.SessionType.INWARD;//tmp code
        } else if (this instanceof ReplenishmentEanStartFragment)
            return AppCommonMethods.SessionType.REPLENISHMENT;
        else if (this instanceof SearchListExcelFragment || this instanceof SearchListExcelStartFragment)
            return AppCommonMethods.SessionType.SER_EXCEL;
        else return AppCommonMethods.SessionType.OTHER;
    }

    @Override
    public String getTypeCharCode() {
        return super.getTypeCharCode(sessionType);
    }

    /**
     * Set session object.
     *
     * @param sessionObject the session object
     */
    public void setSessionObject(RFIDSession sessionObject) {
        if (sessionObject != null) this.sessionObject = sessionObject;
    }

    /**
     * Check reader connected boolean.
     *
     * @return the boolean
     */
    protected boolean checkReaderConnected() {
        if (mainViewModel.isReaderConnected()) return true;
        else {
            context.showCustomAlertDialog("_", String.format(getString(R.string.err_reader_connection), getTypeCharCode()), getString(R.string.btn_ok), (dialogInterface, i) -> {
                if (((MainActivity) context).isReaderConnected())
                    onTriggerPressed();//mainViewModel.startInventory();
                else mainViewModel.checkAndConnectReader();
            });
            return false;
        }
    }

    @Override
    public AppCommonMethods.SessionType getSessionType() {
        return sessionType;
    }

    @Override
    public void onPause() {
        super.onPause();
        mainViewModel.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isTopInStack()) {
            updateLists();
            new Handler(Looper.getMainLooper()).postDelayed(() -> mainViewModel.onResume(sessionType), 300);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mainViewModel.onDestroy();
    }

    /**
     * On reader configured.
     */
    protected void onReaderConfigured() {/** to be overridden by child fragment **/}

    /**
     * Is session on changed.
     *
     * @param isSessionOn the is session on
     */
    protected void isSessionOnChanged(Boolean isSessionOn) {
        if (isSessionOn == null) return;
        AppCommonMethods.showLog("isSessionOn", "" + (isSessionOn != null ? isSessionOn : "null"));
    }

    /**
     * Is inventory on changed.
     *
     * @param isInventoryOn the is inventory on
     */
    protected void isInventoryOnChanged(Boolean isInventoryOn) {
        if (isInventoryOn == null) return;
    }

    /**
     * Is search on changed.
     *
     * @param isSearchOn the is search on
     */
    protected void isSearchOnChanged(Boolean isSearchOn) {
        if (isSearchOn == null) return;
        if (searchView != null && searchView instanceof SearchView) {
            ((SearchView) searchView).setEnableCheck(!isSearchOn);
        }
        if (isAllowDirectionalSearch && mainViewModel != null && mainViewModel.getSensorData() != null) {
            if (imgSearchDir != null) {
                imgSearchDir.setRotation(0.0f);
                imgSearchDir.setRotationX(0.0f);
                imgSearchDir.setRotationY(0.0f);
                if (!isSearchOn && isShowArrow) isShowArrow = false;
                imgSearchDir.setVisibility(isSearchOn && isShowArrow ? View.VISIBLE : View.GONE);
            }
            if (isSearchOn) {
                mainViewModel.getSensorData().observe(getViewLifecycleOwner(), data -> {
                    if (data != null && !data.equalsIgnoreCase("0$0$0$0")) {
                        //showLog("SENSORDataObserve", data);
                        currentArray[0] = data.split("\\$")[0];
                        currentArray[1] = data.split("\\$")[1];
                        currentArray[2] = data.split("\\$")[2];
                        currentArray[3] = data.split("\\$")[3];
                    }
                });
            } else {
                mainViewModel.getSensorData().removeObservers(getViewLifecycleOwner());
            }
        }
    }

    /**
     * Is pick on changed.
     *
     * @param isPickOn the is pick on
     */
    protected void isPickOnChanged(Boolean isPickOn) {
    if(isPickOn == null) return;
    }

    /**
     * On pick data changed.
     *
     * @param pickData the pick data
     */
    protected void onPickDataChanged(Inventory pickData) {/** to be overridden by child fragment **/}

    /**
     * On pick data changed.
     *
     * @param listPickedData the pick data
     */
    protected void onPickedListDataChanged(List<Inventory> listPickedData) {/** to be overridden by child fragment **/}

    /**
     * Is encode on changed.
     *
     * @param isEncodeOn the is encode on
     */
    protected void isEncodeOnChanged(Boolean isEncodeOn) {
    if(isEncodeOn == null) return;
    }

    /**
     * Is encode on changed.
     *
     * @param isDecodeOn the is encode on
     */
    protected void isDecodeOnChanged(Boolean isDecodeOn) {
    if(isDecodeOn == null) return;
    }

    /**
     * Toggle inventory.
     */
    protected void toggleInventory() {
        AppCommonMethods.showLog("toggleInventory", "" + sessionType);
        if (sessionObject != null && chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue()))
            mainViewModel.stopInventory();
        else if (checkReaderConnected() && (sessionObject != null || chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) && !chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue())) {
            mainViewModel.startInventory(listIgnoreEpcs);
        }
    }

    protected void toggleSearch(final String barcode, final SearchLog searchLog) {
        toggleSearch(barcode, searchLog, false);
    }

    /**
     * Toggle search.
     *
     * @param barcode         the barcode
     * @param searchLog       the SearchLog
     * @param isLockSearchEPC the isLockSearchEPC
     */
    protected void toggleSearch(final String barcode, final SearchLog searchLog, final boolean isLockSearchEPC) {
        if (sessionObject != null && chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()))
            mainViewModel.stopInventory();
        else if (checkReaderConnected() && (sessionObject != null || chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) && !chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())) {
            if (sessionObject != null && mainViewModel.getIsSessionOn().getValue() == null)
                mainViewModel.startSession(sessionObject, true);
            else mainViewModel.performBarcodeBasedSearch(barcode, isLockSearchEPC);
            if (searchLog != null && isNullOrEmpty(searchLog.getSearchDurationTime()))
                searchStartTime = System.currentTimeMillis();
        }
    }

    /**
     * On trigger pressed.
     */
    protected void onTriggerPressed() {
        toggleInventory();
    }

    /**
     * On reader power changed.
     *
     * @param readerPower the reader power
     */
    protected void onReaderPowerChanged(Integer readerPower) {
        /*
         * This method will be called in respective child fragments*/
    }

    /**
     * On data size changed.
     *
     * @param size the size
     */
    protected void onDataSizeChanged(Integer size) {
        /*
         * This method will be called in respective child fragments*/
    }

    /**
     * On data changed.
     *
     * @param listData the list data
     */
    protected void onDataChanged(List<Inventory> listData) {
        /*
         * This method will be called in respective child fragments*/
    }

    /**
     * On search percentage changed.
     *
     * @param percentage the percentage
     */
    protected void onSearchPercentageChanged(Integer percentage) {
        onSearchPercentageChanged(percentage, "");
    }

    /**
     * On search percentage changed.
     *
     * @param percentage the percentage
     */
    protected void onSearchPercentageChanged(Integer percentage, String rssi) {
        showLog("searchPercent", "" + percentage);
    showLog("searchRssi", "" + rssi);
        if (SharedPrefManager.getIsSensorAvailable()) {
            currentArray[4] = "" + percentage;
            if (Integer.parseInt(currentArray[4]) > Integer.parseInt(referenceArray[4])) {
                referenceArray[0] = currentArray[0];
                referenceArray[1] = currentArray[1];
                referenceArray[2] = currentArray[2];
                referenceArray[3] = currentArray[3];
                referenceArray[4] = currentArray[4];
            }
        }
        if (percentage == 0 && isAllowDirectionalSearch) {
            resetDirectionArrayCounter++;
            if (resetDirectionArrayCounter >= resetDirectionArrayCounterLimit) {
                resetDirectionArrays();
                resetDirectionArrayCounter = 0;
            }
        }
        if (chkNotNullTrue(mainViewModel.getIsSearchOn().getValue()) && percentage >= oldPercentage) {
            oldPercentage = percentage;
            oldRssi = rssi;
            if (searchLog != null && isNullOrEmpty(searchLog.getSearchDurationTime()) && searchStartTime > 0 && oldPercentage >= 70)
                updateSearchDuration(searchLog, oldPercentage);
        }
        /*
         * This method will be called in respective child fragments*/
    }

    protected void setDefaultSearchViews() {
        removePercentage = false;
        oldPercentage = 0;
        oldRssi = "";
        if (searchView != null && searchView instanceof SearchView)
            ((SearchView) searchView).resetToDefault();
        else if (searchView != null && searchView instanceof PointerSpeedometer) {
            ((PointerSpeedometer) searchView).resetToDefault();
            if (isAllowDirectionalSearch && imgSearchDir != null)
                updateSearchDir(0, 0.0, 0.0, 0.0, "", 0);
        }
    }

    protected void startTimer(final View searchView) {
        startTimer(searchView, null);
    }

    protected void startTimer(final View searchView, final ImageView imgSearchDir) {
        this.searchView = searchView;
        this.imgSearchDir = isAllowDirectionalSearch ? imgSearchDir : null;

        if (searchView != null && searchView instanceof SearchView)
            ((SearchView) searchView).setEnableCheck(false);
        countChangeTimer = new Timer();
        countChangeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //Called each time when 1000 milliseconds (1 second) (the period parameter)
                if (context != null && chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())) {
                    context.runOnUiThread(() -> {
                        int result = oldPercentage;
                        String result1 = oldRssi;
                        if (result == 0) {
                            resetCoordinates++;
                        } else {
                            resetCoordinates = 0;
                        }

                        //showLog("result Percent", "" + result + SharedPrefManager.getIsSensorAvailable());
                        if (isAllowDirectionalSearch) {
                            //calculate direction
                            //showLog("DIR,REF", Arrays.asList(referenceArray).toString());
                            //showLog("DIR,CUR", Arrays.asList(currentArray).toString() + "\n..");
                            Double refAngle = Double.parseDouble(referenceArray[0]);
                            // refAngle = refAngle>360.00?360.00-refAngle:refAngle;
                            Double refAngleX = Double.parseDouble(referenceArray[1]);
                            Double refAngleY = Double.parseDouble(referenceArray[2]);
                            Double refDirection = Double.parseDouble(referenceArray[3]);
                            Double refPercentage = Double.parseDouble(referenceArray[4]);

                            Double curAngle = Double.parseDouble(currentArray[0]);
                            //curAngle = curAngle>360.00?360-curAngle:curAngle;
                            Double curAngleX = Double.parseDouble(currentArray[1]);
                            Double curAngleY = Double.parseDouble(currentArray[2]);
                            Double curDirection = Double.parseDouble(currentArray[3]);
                            Double curPercentage = Double.parseDouble(currentArray[4]);
                            //showLog("DIRECTIONDATA", "Move " + curAngle + "  " + refAngle);

                            if (curPercentage > 0 && refPercentage > 0) {

                                double diff = Math.abs(refAngle) > Math.abs(curAngle) ? Math.abs(refAngle) - Math.abs(curAngle) : Math.abs(curAngle) - Math.abs(refAngle);
                                double diffX = Math.abs(refAngleX) > Math.abs(curAngleX) ? Math.abs(refAngleX) - Math.abs(curAngleX) : Math.abs(curAngleX) - Math.abs(refAngleX);
                                double diffY = Math.abs(refAngleY) > Math.abs(curAngleY) ? Math.abs(refAngleY) - Math.abs(curAngleY) : Math.abs(curAngleY) - Math.abs(refAngleY);

                                if (refAngle > curAngle) {
                                    //   41.31192398071289 >16.34443473815918
                                    //showLog("DIRECTIONDATA", "Move refAngle > curAngle " + refAngle + ">cur :" + curAngle + " diff:" + diff);
                                    //  if(diff>5){
                                    updateSearchDir(curDirection < 0 ? 1 : 2, diff, diffX, diffY, "Move refAngle > curAngle  " + refAngle, result);

                                } else {
                                    // if(diff>5){
                                    //showLog("DIRECTIONDATA", "Move curAngle > refAngle " + refAngle + ">cur :" + curAngle + " diff:" + (360 - diff));
                                    updateSearchDir(curDirection < 0 ? 2 : 1, 360 - diff, diffX, diffY, "Move curAngle > refAngle" + refAngle, result);//diff);

                                }

                                //                Double totAngle = refPercentage >= curPercentage ? refAngle : curAngle;

                            } else {
                                //
                                if (curPercentage == 0) {
                                    //updateSearchDir(2,0.0,"Go Straight");//add);
                                }
                            }

                            if (resetCoordinates == 2) {
                                showLog("resetCoordinates == 2", "" + oldPercentage);
                            }
                        }
                        if (searchView != null) {
                            if (searchView != null && searchView instanceof SearchView) {
                                if (isDebugApp)
                                    ((SearchView) searchView).setPercentageValue(result, result1);
                                else ((SearchView) searchView).setPercentageValue(result);
                            } else if (searchView != null && searchView instanceof PointerSpeedometer) {
                                ((PointerSpeedometer) searchView).setPercentageValue(result);
                                AppCommonMethods.searchBeep(context, result);
                            }
                            updateSearchUI(result);
                            AppCommonMethods.searchBeep(context, result);
                            if (result < 90) removePercentage = true;
                        }

                        if (removePercentage) {
                            //showLog("set old Percent 0", "" + oldPercentage);
              if(false && SharedPrefManager.getDeviceType() == AppCommonMethods.DeviceType.CHAINWAY){
                                //showLog("came if loop", "" + oldPercentage);
                                if (result < 90) {
                                    //showLog("came if loop if", "" + oldPercentage);
                                    removePercentage = false;
                                    oldPercentage = 0;
                                    oldRssi = "";
                                    chainwaySearchRemovalPercentCount = 0;
                                } else {
                                    //showLog("came if loop else", "" + oldPercentage);
                                    chainwaySearchRemovalPercentCount++;
                                }
                                if (chainwaySearchRemovalPercentCount == 1) {
                                    //showLog("came if loop if lloop if", "" + oldPercentage);
                                    removePercentage = false;
                                    oldPercentage = 0;
                                    oldRssi = "";
                                    chainwaySearchRemovalPercentCount = 0;
                                } else {
                                    //showLog("came if loop if lloop if else", "" + oldPercentage);
                                    chainwaySearchRemovalPercentCount = 0;
                                }

                            } else {
                                //showLog("came if  else", "" + oldPercentage);
                                removePercentage = false;
                                oldPercentage = 0;
                                oldRssi = "";
                                chainwaySearchRemovalPercentCount = 0;
                            }
                        } else {
                            //showLog("came if  else else else", "" + oldPercentage);
                            chainwaySearchRemovalPercentCount = 0;
                            removePercentage = true;
                        }
                        //showLog("came default", "" + oldPercentage);
                        oldPercentage = 0;
                        oldRssi = "";
                        //showLog("set old Percent 0", "" + oldPercentage);
                    });
                }
            }
        }, 0, 500);//500);
    }

    protected void updateSearchUI(final int result) {
    }

    protected void updateSearchDir(int direction, Double angle, String message, int percent) {
        updateSearchDir(direction, angle, null, null, message, percent);
    }

    protected void updateSearchDir(int direction, Double angle, Double angleX, Double angleY, String message, int percent) {
        //Rotate ImageView based on direction and angle
        if (isAllowDirectionalSearch && imgSearchDir != null && chkNotNullTrue(mainViewModel.getIsSearchOn().getValue())) {
            //showLog("DIRECTION.,angle", +direction + "," + angle + "\n" + message);
            float rotation = imgSearchDir.getRotation();
            if (percent >= AppConstants.SEARCH_PERCENT_VALUE_33 && !isShowArrow && !isEmptyArray(currentArray))
                isShowArrow = true;
            if (percent <= 0 && isShowArrow) isShowArrow = false;
            imgSearchDir.setVisibility(isShowArrow ? View.VISIBLE : View.GONE);

            float rotAngle = angle.floatValue();
            if (rotAngle > 180) {
                rotAngle = (360 - angle.floatValue()) * -1;
            }
            //showLog("DIRECTION..", "PreAngle:" + rotation + "_CurAngle:" + rotAngle);

            imgSearchDir.animate().rotation(rotAngle).start();

            imgSearchDir.setImageResource(percent >= 30 ? R.drawable.top_green1 : R.drawable.top_red1);

            // Create our Preview view and set it as the content of our Activity

            //      imgSearchDir.animate().rotation((direction == 1 ? -1.0f : 1.5f) * angle.floatValue()).start();

            //   textSearchDir.setText(message+" "+rotation+"  "+(direction == 1 ? -1.0f : 1.5f) * angle.floatValue());

            //imgSearchDir.setColorFilter(colorId);
            // imgSearchDir.setColorFilter(imgSearchDir.getContext().getResources().getColor(colorId), PorterDuff.Mode.SRC_ATOP);
        }
    }

    private boolean isEmptyArray(String[] arr) {
        boolean isEmpty = true;
        for (int i = 0; i < (arr.length > 4 ? arr.length - 1 : arr.length); i++) {
            if (Double.parseDouble(chkZero(arr[i], "0.0")) != 0) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }

    private void resetDirectionArrays() {
        referenceArray = "0,0,0,0,0".split(",");
        currentArray = "0,0,0,0,0".split(",");
    }

    /**
     * Stop timer.
     */
    public void stopTimer() {

        if (SharedPrefManager.getIsSensorAvailable()) {
            updateSearchDir(0, 0.0, 0.0, 0.0, "", 0);//add);
            referenceArray = "0,0,0,0,0".split(",");
            currentArray = "0,0,0,0,0".split(",");
        }

        if (searchView != null && searchView instanceof SearchView)
            ((SearchView) searchView).setEnableCheck(true);
        if (countChangeTimer != null) countChangeTimer.cancel();

        if (searchView != null && searchView instanceof PointerSpeedometer) setDefaultSearchViews();
        if (searchView != null && searchView instanceof SearchView)
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    setDefaultSearchViews();
                }
            }, 50);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setObservers();

        mainViewModel.getIsDeviceConfigured().observe(getViewLifecycleOwner(), isDeviceConfigured -> {
            if (!isTopInStack()) return;
            showLog("RFIDFrag_isDeviceConfigured", AppCommonMethods.chkVal(isDeviceConfigured));
            if (chkNotNullTrue(isDeviceConfigured)) {
                onReaderConfigured();
            }
        });

        mainViewModel.getIsSessionOn().observe(getViewLifecycleOwner(), isSessionOn -> {
            if (!isTopInStack()) return;
            showLog("RFIDFrag_isSessionOn", AppCommonMethods.chkVal((isSessionOn)));
            if (isSessionOn == null || sessionObject == null) return;
            else if (sessionObject != null && isSessionOn != null) {
                ((MainActivity) context).lockDrawer(chkNotNullTrue(isSessionOn));
                isSessionOnChanged(isSessionOn);
                final long invUploadTimeInterval = SharedPrefManager.getLong(ParamConstants.OPTIMIZED_INVENTORY_UPLOAD_TIME_INTERVAL);
                if (invUploadTimeInterval > 0) {
                    if (threadPool != null) threadPool.shutdownNow();
                    showLog("OnSessionChanged_Type", "" + sessionObject.sessionType);
                    if (isSessionOn && isInvSession) {
                        threadPool = new ScheduledThreadPoolExecutor(1);
                        threadPool.schedule(new Runnable() {
                            @Override
                            public void run() {
                                showLog("threadPool", invUploadTimeInterval + " scheduleAtFixedRate (" + AppDatabase.getInventoryDao(context).getNonUploadedCount(sessionObject.sessionId) + ")");
                                context.uploadSavedInventoryTags();
                            }
                        }, invUploadTimeInterval, TimeUnit.SECONDS);
                    }
                }
                setInventoryDataObserver();
            }
            if (sessionObject != null && isInvSession) {
                if (chkNotNullTrue(isSessionOn)) startInventoryTimer();
                else if (chkNotNullFalse(isSessionOn)) discardInventoryTimer();
            }
        });

        mainViewModel.getReaderPower().observe(getViewLifecycleOwner(), readerPower -> {
            if (!isTopInStack()) return;
            showLog("RFIDFrag_ReaderPower", AppCommonMethods.chkVal(readerPower));
            if (readerPower == null) return;
            onReaderPowerChanged(readerPower);
        });

        if (sessionObject != null /***&& chkNotNullTrue(mainViewModel.getIsDeviceConfigured().getValue())*/)
            mainViewModel.startSession(sessionObject, false);
    }

    /**
     * Set observers.
     */
    protected void setObservers() {
        mainViewModel.getIsInventoryOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsInventoryOn().observe(getViewLifecycleOwner(), isInventoryOn -> {
            if (!isTopInStack()) return;
            showLog("RFIDFrag_isInventoryOn", AppCommonMethods.chkVal(isInventoryOn));
      if(isInventoryOn == null) return;
            else {
                insertAuditTrailsLog("Inventory_" + (isInventoryOn ? "ON" : "OFF"));
                isInventoryOnChanged(isInventoryOn);
            }
        });

        mainViewModel.getIsSearchOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsSearchOn().observe(getViewLifecycleOwner(), isSearchOn -> {
            if (!isTopInStack()) return;
            showLog("RFIDFrag_isSearchOn", AppCommonMethods.chkVal(isSearchOn));
      if(isSearchOn == null) return;
            else {
                insertAuditTrailsLog("Search_" + (isSearchOn ? "ON" : "OFF"));
                isSearchOnChanged(isSearchOn);
            }
        });

        mainViewModel.getSearchPercentage().removeObservers(getViewLifecycleOwner());
        mainViewModel.getSearchPercentage().observe(getViewLifecycleOwner(), searchPercentage -> {
            if (!isTopInStack()) return;
            showLog("RFIDFrag_SearchPercentage", AppCommonMethods.chkVal(searchPercentage));
      if(searchPercentage == null) return;
            else {
                //insertAuditTrailsLog("SearchPercentage_"+searchPercentage);
                if (isDebugApp)
                    onSearchPercentageChanged(searchPercentage, mainViewModel.getSearchRssi().getValue());
                else onSearchPercentageChanged(searchPercentage);
            }
        });

        mainViewModel.getIsPickOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsPickOn().observe(getViewLifecycleOwner(), isPickOn -> {
            if (!isTopInStack()) return;
            showLog("RFIDFrag_isPickOn", AppCommonMethods.chkVal(isPickOn));
      if(isPickOn == null) return;
            else {
                insertAuditTrailsLog("Pick_" + (isPickOn ? "ON" : "OFF"));
                isPickOnChanged(isPickOn);
            }
        });

        mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getPickData().observe(getViewLifecycleOwner(), pickData -> {
            if (!isTopInStack()) return;
            showLog("RFIDFrag_pickData", "" + (pickData != null));
            if(pickData == null) return;
            else {
                //insertAuditTrailsLog("PICK_DATA_"+pickData);
                onPickDataChanged(pickData);
                if (pickData != null && chkNotNullTrue(mainViewModel.getIsPickOn().getValue()))
                    mainViewModel.getIsPickOn().setValue(false);
                mainViewModel.getPickData().postValue(null);
            }
        });

        mainViewModel.getPickedListData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getPickedListData().observe(getViewLifecycleOwner(), listPickData -> {
            if (!isTopInStack()) return;
            showLog("RFIDFrag_pickData", "" + (listPickData != null));
            if(listPickData == null) return;
            else {
                //insertAuditTrailsLog("PICK_DATA_"+pickData);
                onPickedListDataChanged(listPickData);
                if (isNonEmpty(listPickData) && chkNotNullTrue(mainViewModel.getIsPickOn().getValue()))
                    mainViewModel.getIsPickOn().setValue(false);
                mainViewModel.getPickedListData().postValue(null);
            }
        });

        mainViewModel.getIsEncodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsEncodeOn().observe(getViewLifecycleOwner(), isEncodeOn -> {
            if (!isTopInStack()) return;
            showLog("RFIDFrag_isEncodeOn", AppCommonMethods.chkVal(isEncodeOn));
      if(isEncodeOn == null) return;
            else {
                insertAuditTrailsLog("Encode_" + (isEncodeOn ? "ON" : "OFF"));
                isEncodeOnChanged(isEncodeOn);
            }
        });

        mainViewModel.getIsDecodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsDecodeOn().observe(getViewLifecycleOwner(), isDecodeOn -> {
            if (!isTopInStack()) return;
            showLog("RFIDFrag_isDecodeOn", AppCommonMethods.chkVal(isDecodeOn));
      if(isDecodeOn == null) return;
            else {
                isDecodeOnChanged(isDecodeOn);
            }
        });

        setTriggerDataObserver();
    }

    /**
     * Is process on boolean.
     *
     * @return the boolean
     */
    public boolean isProcessOn() {
        return mainViewModel != null && sessionObject != null && chkNotNullTrue(mainViewModel.getIsSessionOn().getValue()) && chkNotNullTrue(mainViewModel.getIsProcessOn().getValue());
    }

    protected void updateSearchDuration(final SearchLog searchLog, final int searchPercentage) {
        if (searchLog != null && isNullOrEmpty(searchLog.getSearchDurationTime()) && searchStartTime > 0 && searchPercentage >= 120) {
            final Long durationInSeconds = (System.currentTimeMillis() - searchStartTime) / 1000L;
            showLog("SearchDurationInSeconds", "" + durationInSeconds);
            searchLog.setSearchDurationTime(String.valueOf(durationInSeconds));
            AppDatabase.getSearchLogDao(context).updateDuration(sessionObject != null ? sessionObject.sessionId : "", searchLog.ean, searchLog.type, searchLog.transactionId, searchLog.searchDurationTime);
            searchStartTime = 0L;
        }
    }

    protected void removeObservers() {
        mainViewModel.getAllInventoryData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getAllInventoryDataSize(sessionObject != null ? sessionObject.sessionId : "", isTripInventory).removeObservers(getViewLifecycleOwner());
        mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsInventoryOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsSearchOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getSearchPercentage().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsPickOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsEncodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsDecodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getReaderPower().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsDeviceConfigured().removeObservers(getViewLifecycleOwner());
    }

    @Override
    public void onDestroyView() {
        if (threadPool != null) threadPool.shutdownNow();
        inventoryUiHandler.removeCallbacks(inventoryUiUpdateRunnable);
        pendingInventoryUiSize = null;
        isInventoryUiUpdateScheduled = false;
        final int sizeNonUploadedCount = chkZero(this.sizeNonUploadedCount, AppDatabase.getInventoryDao(context).getNonUploadedCount(sessionObject != null ? sessionObject.sessionId : ""));
        final int size1 = chkZero(size, sizeNonUploadedCount);
        if (SharedPrefManager.getBoolean(ParamConstants.IS_OPTIMIZED_INVENTORY) && isInvSession)
            isDiscardData = sizeNonUploadedCount <= 0;
        discardInventoryTimer();
        mainViewModel.stopSession(sessionObject, sessionType.getValue(), isDiscardData || (size1 == 0 && (sessionObject == null || sessionObject.sessionType == sessionType.getValue())));
        mainViewModel.updateActiveSessionFlag(sessionType, false);
        mainViewModel.getAllInventoryData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getAllInventoryDataSize(sessionObject != null ? sessionObject.sessionId : "", isTripInventory).removeObservers(getViewLifecycleOwner());
        sessionObject = null;
        mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsInventoryOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsSearchOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getSearchPercentage().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsPickOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getPickData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsEncodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsDecodeOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsSessionOn().removeObservers(getViewLifecycleOwner());
        mainViewModel.getReaderPower().removeObservers(getViewLifecycleOwner());
        mainViewModel.getIsDeviceConfigured().removeObservers(getViewLifecycleOwner());
        ((MainActivity) context).lockDrawer(false);
        super.onDestroyView();
    }

    /**
     * Set trigger data observer.
     */
    private void setTriggerDataObserver() {
        mainViewModel.isTriggerPressed().removeObservers(getViewLifecycleOwner());
        mainViewModel.isTriggerPressed().observe(getViewLifecycleOwner(), triggerPressed -> {
            if (!isTopInStack()) return;
            //showLog("RFIDFrag_TRIGGER", AppCommonMethods.chkVal(triggerPressed));
            //showLog("RFIDFrag_TRIGGER_allowBtnClick", "" + allowBtnClick);
            if (triggerPressed != null && getViewLifecycleOwner().getLifecycle().getCurrentState() == Lifecycle.State.RESUMED && allowBtnClick && isTopInStack()) {
                onTriggerPressed();
            }
        });
    }

    /**
     * Set inventory data observer.
     */
    private void setInventoryDataObserver() {
        mainViewModel.getAllInventoryDataSize(sessionObject != null ? sessionObject.sessionId : "", isTripInventory).removeObservers(getViewLifecycleOwner());
        mainViewModel.getAllInventoryDataSize(sessionObject != null ? sessionObject.sessionId : "", isTripInventory).observe(getViewLifecycleOwner(), size -> {
            if (!isTopInStack()) return;
            this.size = chkNull(size, 0);
            showLog("RFIDFrag_InventoryDataSize", "" + (size == null ? "null" : size));
            if (chkNull(size, 0) < AppCommonMethods.invLiveDataLimit) postInventorySizeChanged(chkNull(size, 0));
            final int nonUploadedCount = sessionObject != null ? AppDatabase.getInventoryDao(context).getNonUploadedCount(sessionObject.sessionId) : 0;
            final long uploadTagCountEventLimit = SharedPrefManager.getLong(ParamConstants.OPTIMIZED_INVENTORY_UPLOAD_TAG_COUNT);
            if (uploadTagCountEventLimit > 0 && nonUploadedCount >= uploadTagCountEventLimit) {
                showLog("uploadOfflineInventory", "tagCountLimit (" + nonUploadedCount + ")");
                context.uploadSavedInventoryTags();
            }
        });
        mainViewModel.getAllInventoryData().removeObservers(getViewLifecycleOwner());
        mainViewModel.getAllInventoryData().observe(getViewLifecycleOwner(), listTagData -> {
            if (!isTopInStack()) return;
            onDataChanged(listTagData);
        });
    }

    public int getInventoryTotalSize() {
        return sessionObject != null ? chkNull(mainViewModel.getInventoryTotalCount(sessionObject.sessionId), 0) : 0;
    }

    public int getInventoryScoreCount() {
        return sessionObject != null ? chkNull(mainViewModel.getInventoryScoreCount(sessionObject.sessionId), 0) : 0;
    }

    public int getUnencodedTagCount() {
        return sessionObject != null && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV) && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_COUNT_IN_INV) ? chkNull(mainViewModel.getUnencodedTagCount(sessionObject.sessionId), 0) : 0;
    }

    public int getVerifiedTagCount() {
        return sessionObject != null && SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY) ? chkNull(mainViewModel.getVerifiedTagCount(sessionObject.sessionId), 0) : 0;
    }

    private void postInventorySizeChanged(final int newSize) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            inventoryUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    postInventorySizeChanged(newSize);
                }
            });
            return;
        }
        pendingInventoryUiSize = newSize;
        final long now = System.currentTimeMillis();
        final long elapsedMs = now - lastInventoryUiUpdateMs;
        if (elapsedMs >= INVENTORY_UI_UPDATE_INTERVAL_MS) {
            inventoryUiHandler.removeCallbacks(inventoryUiUpdateRunnable);
            dispatchInventorySizeChanged();
        } else if (!isInventoryUiUpdateScheduled) {
            isInventoryUiUpdateScheduled = true;
            inventoryUiHandler.postDelayed(inventoryUiUpdateRunnable, INVENTORY_UI_UPDATE_INTERVAL_MS - elapsedMs);
        }
    }

    private void dispatchInventorySizeChanged() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            inventoryUiHandler.post(inventoryUiUpdateRunnable);
            return;
        }
        isInventoryUiUpdateScheduled = false;
        if (pendingInventoryUiSize == null) return;
        final int sizeToDispatch = pendingInventoryUiSize;
        pendingInventoryUiSize = null;
        lastInventoryUiUpdateMs = System.currentTimeMillis();
        onDataSizeChanged(sizeToDispatch);
        RetailDiagnosticLogger.recordInventoryUiUpdate();
    }

    public int getAlignTagCount() {
        return sessionObject != null && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_UNENCODED_ALIEN_COUNT_IN_INV) && SharedPrefManager.getBoolean(ParamConstants.IS_SHOW_ALIEN_COUNT_IN_INV) ? chkNull(mainViewModel.getAlignTagCount(sessionObject.sessionId), 0) : 0;
    }

    public String getInvnetoryScoreCountStr() {
        return sessionObject != null ? String.valueOf(getInventoryScoreCount()) : "0";
    }

    public String getUnencodedTagCountStr() {
        return sessionObject != null ? String.valueOf(getUnencodedTagCount()) : "0";
    }

    public String getAlignTagCountStr() {
        return sessionObject != null ? String.valueOf(getAlignTagCount()) : "0";
    }

    /**
     * Get size int.
     *
     * @return the int
     */
    public int getSize() {
        return chkNull(size, 0);
    }

    /**
     * Is discard data boolean.
     *
     * @return the boolean
     */
    public boolean isDiscardData() {
        return isDiscardData;
    }

    /**
     * Set discard data.
     *
     * @param discardData the discard data
     */
    public void setDiscardData(boolean discardData) {
        isDiscardData = discardData;
    }

    /**
     * Api call.
     *
     * @param action the action
     */
    public void apiCall(String action) {
        /*
         * This method will be called in respective child fragments*/
    }

    /**
     * Api call.
     *
     * @param action the action
     * @param args   the args
     */
    public void apiCall(String action, Bundle args) {
        /*
         * This method will be called in respective child fragments*/
    }

    private void startInventoryTimer() {
        if (isInvSession && inventoryTimer == null) {
            inventoryTimer = new Timer();
            inventoryTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isInvSession && size >= AppCommonMethods.invLiveDataLimit) {
                        postInventorySizeChanged(getInventoryTotalSize());
                    }
                }
            }, 0, INVENTORY_UI_UPDATE_INTERVAL_MS);
        }
    }

    private void discardInventoryTimer() {
        if (isInvSession && inventoryTimer != null) {
            inventoryTimer.cancel();
            inventoryTimer = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (sessionObject != null && chkNotNullTrue(mainViewModel.getIsSessionOn().getValue())) {
            if (isProcessOn()) {
                context.showCustomAlertDialog("", String.format(getString(R.string.err_op_back_press), getTypeCharCode(), sessionType.name().replaceAll("_", " ")), false, true, getString(R.string.btn_ok), null);
            } else {
                showLog(sessionType.name() + " size", "" + size);
                sizeNonUploadedCount = AppDatabase.getInventoryDao(context).getNonUploadedCount(sessionObject != null ? sessionObject.sessionId : "");
                switch (sessionType) {
                    case DECODING:
                        if (chkZero(size, sizeNonUploadedCount) > 0) {
                            if (!isAllowSessionSaveForDecode) {
                                apiCall(AppConstants.SESSION_ACTION_DISCARD);
                                return;
                            }
                            context.showCustomAlertDialog("_", getString(R.string.title_back_session_save), getString(R.string.btn_yes), (dialogInterface, i) -> {
                                apiCall(AppConstants.SESSION_ACTION_SAVE);
                            }, getString(R.string.btn_no), (dialogInterface, i) -> apiCall(AppConstants.SESSION_ACTION_DISCARD));
                        } else {
                            apiCall(AppConstants.SESSION_ACTION_DISCARD);
                        }
                        break;
                    case ENCODING:
                    case ENCODING_THAN:
                        //if(size > 0){
                        if (chkZero(size, sizeNonUploadedCount) > 0) {
                            if ((sessionType == AppCommonMethods.SessionType.ENCODING && !isAllowSessionSaveForEncode) || (sessionType == AppCommonMethods.SessionType.ENCODING_THAN && !isAllowSessionSaveForThanEncode)) {
                                apiCall(AppConstants.SESSION_ACTION_DISCARD);
                                return;
                            }
              /*if(SharedPrefManager.getBoolean(ParamConstants.IS_ENC_VERIFY) && getVerifiedTagCount() < getSize() && isNonEmpty(SharedPrefManager.getString(ParamConstants.SESSION_FORCE_END_PASSWORD)))
                showForcePasswordDialog();
              else*/
                            context.showCustomAlertDialog("_", getString(R.string.title_back_session_save), getString(R.string.btn_yes), (dialogInterface, i) -> {
                                apiCall(AppConstants.SESSION_ACTION_SAVE);
                            }, getString(R.string.btn_no), (dialogInterface, i) -> apiCall(AppConstants.SESSION_ACTION_DISCARD));
                        } else {
                            apiCall(AppConstants.SESSION_ACTION_DISCARD);
                        }
                        break;
                    case INVENTORY:
                    case ADD_INVENTORY:
                    case BRAND_INVENTORY:
                    case FILTER_INVENTORY:
                        if (size > 0) {
                            if (SharedPrefManager.getBoolean(ParamConstants.IS_OPTIMIZED_INVENTORY)) {
                                //apiCall(AppConstants.SESSION_ACTION_SAVE);
                                //Save Offline
                                if (threadPool != null) threadPool.shutdownNow();
                                mainViewModel.stopSession(sessionObject, sizeNonUploadedCount <= 0);
                                context.clearBackStack();
                                return;
                            }
                            if (!isAllowSessionSaveForInventory) {
                                apiCall(AppConstants.SESSION_ACTION_DISCARD);
                                return;
                            }
                            context.showCustomAlertDialog("_", getString(R.string.title_back_session_save), getString(R.string.btn_yes), (dialogInterface, i) -> {
                                apiCall(AppConstants.SESSION_ACTION_SAVE);
                            }, getString(R.string.btn_no), (dialogInterface, i) -> apiCall(AppConstants.SESSION_ACTION_DISCARD));
                        } else apiCall(AppConstants.SESSION_ACTION_DISCARD);
                        break;
                    case STOCK_CORRECTION:
                        context.popBackStack();
                        break;
                    case SEARCH:
                        AppDatabase.getProductDao(context).deleteAll(AppCommonMethods.SessionType.SEARCH.getValue());
                        mainViewModel.stopSession(sessionObject, true);
                        context.popBackStack();
                        break;
                    case OMNICHANNEL:
                        mainViewModel.stopSession(sessionObject, false);
                        context.popBackStack();
                        break;
                    case SEARCH_AGEING:
                        mainViewModel.stopSession(sessionObject, false);
                        context.popBackStack();
                        break;
                    case SEARCH_LIST:
                        super.onBackPressed();
                        break;
                    case SEARCH_UNENCODED:
                        if (size > 0) {
                            context.showCustomAlertDialog("", getString(R.string.msg_unencoded_search_back), R.string.btn_no, null, R.string.btn_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mainViewModel.stopSession(sessionObject, true);
                                    context.popBackStack();
                                }
                            });
                        } else {
                            mainViewModel.stopSession(sessionObject, sessionObject.sessionType == sessionType.getValue());
                            context.popBackStack();
                        }
                        break;
                    case SEARCH_ALIEN:
                        if (size > 0) {
                            context.showCustomAlertDialog("", getString(R.string.msg_alien_search_back), R.string.btn_no, null, R.string.btn_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mainViewModel.stopSession(sessionObject, true);
                                    context.popBackStack();
                                }
                            });
                        } else {
                            mainViewModel.stopSession(sessionObject, true);
                            context.popBackStack();
                        }
                        break;
                    case SEARCH_FILE:
                        if (size > 0) {
                            context.showCustomAlertDialog("", getString(R.string.title_cancel_trip_session_alert), R.string.btn_no, null, R.string.btn_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mainViewModel.stopSession(sessionObject, true);
                                    context.popBackStack();
                                }
                            });
                        } else {
                            mainViewModel.stopSession(sessionObject, true);
                            context.popBackStack();
                        }
                        break;
                    case SEARCH_FIFO:
                        if (size > 0) {
                            context.showCustomAlertDialog("", this instanceof SearchFIFOStartFragment ? ((SearchFIFOStartFragment) this).getBackPressMsg() : getString(R.string.msg_fifo_search_back), R.string.btn_no, null, R.string.btn_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mainViewModel.stopSession(sessionObject, true);
                                    context.popBackStack();
                                }
                            });
                        } else {
                            mainViewModel.stopSession(sessionObject, true);
                            context.popBackStack();
                        }
                        break;
                    case INWARD:
                        if (size > 0) {
                            context.showCustomAlertDialog(getString(R.string.title_inventory_stop_alert), getString(AppCommonMethods.isSetInwOnline ? R.string.msg_stock_correction_stop_alert : R.string.msg_inward_stop_alert), getString(AppCommonMethods.isSetInwOnline ? R.string.btn_upload : R.string.btn_save), (dialogInterface, i) -> {
                                if (!AppCommonMethods.isSetInwOnline && this instanceof InwardGrnStartFragment)
                                    ((InwardGrnStartFragment) this).callAlert();
                                else apiCall(AppConstants.SESSION_ACTION_UPLOAD);
                            }, getString(R.string.btn_discard), (dialogInterface, i) -> {
                                mainViewModel.stopSession(sessionObject, true);
                                if (this instanceof HuProcessStartFragment)
                                    ((HuProcessStartFragment) this).discardData();
                                context.popBackStack();
                            });
                        } else {
                            mainViewModel.stopSession(sessionObject, true);
                            context.popBackStack();
                        }
                        break;
                    case REPLENISHMENT:
                        if (size > 0) {
                            context.showCustomAlertDialog(getString(R.string.title_inventory_stop_alert), getString(R.string.msg_stock_correction_stop_alert), getString(R.string.btn_upload), (dialogInterface, i) -> {
                                apiCall(AppConstants.SESSION_ACTION_UPLOAD);
                            }, getString(R.string.btn_discard), (dialogInterface, i) -> {
                                mainViewModel.stopSession(sessionObject, true);
                                context.popBackStack();
                            });
                        } else {
                            mainViewModel.stopSession(sessionObject, true);
                            context.popBackStack();
                        }
                        break;
                    case MOVEMENT:
                        if (size > 0) {
                            context.showCustomAlertDialog(getString(R.string.title_inventory_stop_alert), getString(R.string.msg_stock_correction_stop_alert), getString(R.string.btn_upload), (dialogInterface, i) -> {
                                apiCall(AppConstants.SESSION_ACTION_UPLOAD);
                            }, getString(R.string.btn_discard), (dialogInterface, i) -> {
                                mainViewModel.stopSession(sessionObject, true);
                                context.popBackStack();
                            });
                        } else {
                            mainViewModel.stopSession(sessionObject, true);
                            context.popBackStack();
                        }
                        break;
                    case OUTWARD:
                        if (size > 0) {
                            context.showCustomAlertDialog(getString(R.string.title_inventory_stop_alert), getString(R.string.msg_inward_stop_alert), getString(R.string.btn_save), (dialogInterface, i) -> {
                                if (this instanceof OutwardHuStartFragment)
                                    ((OutwardHuStartFragment) this).callAlert();
                                else apiCall(AppConstants.SESSION_ACTION_UPLOAD);
                            }, getString(R.string.btn_discard), (dialogInterface, i) -> {
                                mainViewModel.stopSession(sessionObject, true);
                                context.popBackStack();
                            });
                        } else {
                            mainViewModel.stopSession(sessionObject, true);
                            context.popBackStack();
                        }
                        break;
                    case OUTWARD_TOTE:
                        if (size > 0) {
                            context.showCustomAlertDialog("", getString(R.string.msg_outward_pick_back), getString(R.string.btn_no), null, getString(R.string.btn_yes), (dialogInterface, i) -> {
                                mainViewModel.stopSession(sessionObject, true);
                                context.popBackStack();
                            });
                        } else {
                            mainViewModel.stopSession(sessionObject, true);
                            context.popBackStack();
                        }
                        break;
                    case INWARD_TOTE:
                        if (size > 0) {
                            context.showCustomAlertDialog("", getString(R.string.msg_outward_pick_back), getString(R.string.btn_no), null, getString(R.string.btn_yes), (dialogInterface, i) -> {
                                final String challanNo = SharedPrefManager.getString(ParamConstants.CHALLAN_NO);
                                if (isNonEmpty(challanNo) && isNonEmpty(SharedPrefManager.getStringArrayList(challanNo))) {
                                    SharedPrefManager.clearArrayList(challanNo);
                                    SharedPrefManager.setString(ParamConstants.CHALLAN_NO, "");
                                }
                                mainViewModel.stopSession(sessionObject, true);
                                context.popBackStack();
                            });
                        } else {
                            final String challanNo = SharedPrefManager.getString(ParamConstants.CHALLAN_NO);
                            if (isNonEmpty(challanNo) && isNonEmpty(SharedPrefManager.getStringArrayList(challanNo))) {
                                SharedPrefManager.clearArrayList(challanNo);
                                SharedPrefManager.setString(ParamConstants.CHALLAN_NO, "");
                            }
                            mainViewModel.stopSession(sessionObject, true);
                            context.popBackStack();
                        }
                        break;
                    case OFF_RANGE:
                        apiCall(AppConstants.SESSION_ACTION_STOP);
                        break;
                    case SCAN:
                        if (size > 0 && this instanceof ScanCountFragment) {
                            context.showCustomAlertDialog("", getString(R.string.msg_outward_pick_back), getString(R.string.btn_no), null, getString(R.string.btn_yes), (dialogInterface, i) -> {
                                apiCall(AppConstants.SESSION_ACTION_DISCARD);
                            });
                        } else {
                            apiCall(AppConstants.SESSION_ACTION_DISCARD);
                        }
                        break;
                    default:
                        super.onBackPressed();
                }
            }
        } else super.onBackPressed();
    }

    public void showForcePasswordDialog() {
        InputView inputView = new InputView(context);
        inputView.setHint(R.string.hint_password);
        inputView.setLabel(R.string.hint_password);
        inputView.setMinLen(1);
        inputView.setMaxLen(20);
        //inputView.setValidationRegex(AppConstants.REGEX_CHAR_BARCODE);
        inputView.setValidationRegex(SharedPrefManager.getString(ParamConstants.SESSION_FORCE_END_PASSWORD));
        inputView.setButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputView.validate()) apiCall(AppConstants.SESSION_ACTION_DISCARD);
            }
        });
        context.showCustomAlertDialog(getString(R.string.title_session_close_pwd), "", inputView, getString(R.string.btn_validate), getString(R.string.btn_cancel));
    }

    public void startEPCSearch(Inventory inventory) {
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    protected void extractIgnoreEpcs(JSONArray excludeEpcs) {
        if (isNullOrEmpty(excludeEpcs) || isNonEmpty(listIgnoreEpcs)) return;
        listIgnoreEpcs.clear();
        for (int i = 0; i < excludeEpcs.length(); i++) {
            try {
                final String epc = excludeEpcs.getString(i);
                if (isNonEmpty(epc) && !listIgnoreEpcs.contains(epc)) listIgnoreEpcs.add(epc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void setViews(final HeaderTitleLayoutBinding header) {
        setViews(header, null);
    }

    protected void setViews(final HeaderTitleLayoutBinding header, final PowerView llSeekbarPower) {
        this.header = header;
        getActiveUsersAndSessionValidTill();
        setActiveUsers(activeUsers);
        if (llSeekbarPower != null) setupPowerView(llSeekbarPower);
    }

    protected void setupPowerView(final PowerView llSeekbarPower) {
        if (header == null || llSeekbarPower == null) return;
        header.imgConfigSync.setVisibility(View.VISIBLE);
        header.imgConfigSync.setImageResource(R.drawable.ic_config);
        header.imgConfigSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chkNotNullTrue(mainViewModel.getIsInventoryOn().getValue())) return;
                llSeekbarPower.setVisibility(llSeekbarPower.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });
        llSeekbarPower.setupProgress(mainViewModel);
    }

    public void setActiveUsers(final int activeUsers) {
        if (header == null) return;
        int oldActiveUsers = -2;
        try {
            oldActiveUsers = AppCommonMethods.parseInt(header.btnActiveDevices.getText().toString(), "-2");
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        final int activeCount = chkNull(activeUsers, -1) >= 0 ? activeUsers : oldActiveUsers;
        header.flActiveDevices.setVisibility(activeUsers >= -1 ? View.VISIBLE : View.GONE);
        header.btnActiveDevices.setSelected(true);
        header.btnActiveDevices.setText(activeCount >= 0 ? "" + activeCount : "");
    }
}
