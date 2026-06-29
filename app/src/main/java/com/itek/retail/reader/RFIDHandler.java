package com.itek.retail.reader;

import static com.itek.retail.common.AppCommonMethods.beep;
import static com.itek.retail.common.AppCommonMethods.chkNotNullTrue;
import static com.itek.retail.common.AppCommonMethods.chkNull;
import static com.itek.retail.common.AppCommonMethods.errorBeep;
import static com.itek.retail.common.AppCommonMethods.isNonEmpty;
import static com.itek.retail.common.AppCommonMethods.isNullOrEmpty;
import static com.itek.retail.common.AppCommonMethods.isPlayBeepForSameToteTagIfNotLastInserted;
import static com.itek.retail.common.AppCommonMethods.isPlayErrorBeepForOtherThanToteEanTag;
import static com.itek.retail.common.AppCommonMethods.isShowErrorForOtherThanToteEanTag;
import static com.itek.retail.common.AppCommonMethods.isShowErrorForSameToteTag;
import static com.itek.retail.common.AppCommonMethods.playSound;

import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import com.itek.retail.R;
import com.itek.retail.apis.ParamConstants;
import com.itek.retail.common.AppCommonMethods;
import com.itek.retail.common.AppConstants;
import com.itek.retail.common.CommonActivity;
import com.itek.retail.common.SharedPrefManager;
import com.itek.retail.common.UpdateWrittenFoundTagsTask;
import com.itek.retail.database.AppDatabase;
import com.itek.retail.database.BrandEanDao;
import com.itek.retail.database.FIFODao;
import com.itek.retail.database.InventoryDao;
import com.itek.retail.database.ProductDao;
import com.itek.retail.database.TagIDDao;
import com.itek.retail.database.TripInventoryDao;
import com.itek.retail.database.UploadInventoryDao;
import com.itek.retail.model.Inventory;
import com.itek.retail.model.RFIDSession;
import com.itek.retail.model.TripInventory;
import com.itek.retail.model.UploadInventory;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The Rfid handler.
 */
public abstract class RFIDHandler {

  protected final String PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS = "savedBluetoothReaderAddress";
  public static final String TAG = "RFID_HANDLER_" + SharedPrefManager.getDeviceType().name();
  public static final int BEEP_DELAY_TIME_MIN = 0;
  public static final int BEEP_DELAY_TIME_MAX = 300;
  public static final int pickCountDownTime = 2000;//1000;
    public static final List<String> NON_PASSWORD_TIDS = Arrays.asList("E2806995", "E2801160", "E2801190");
    public static final List<String> NON_128_BIT_TIDS = Arrays.asList("E2806995", "E2801171", "E2801160", "E2801190");
    protected final String defaultTagZeroPassword = "00000000";
    protected final double inwDefPowerMultiplier = 1.6;
    protected final double owtToteMinPowerMultiplier = 1.6;
    protected final double inwToteMinPowerMultiplier = 3.0;//1.0;//1.4;
    protected final double decodePickMinPowerMultiplier = 1.4;//1.0;
    private final int SOUND_THRESHOLD = 8;
    private static final int INVENTORY_BATCH_SIZE = 64;
    private static final long INVENTORY_BATCH_MAX_DELAY_MS = 750L;
    private static final long INVENTORY_METRICS_LOG_INTERVAL_MS = 5000L;
    public MutableLiveData<Boolean> isReaderSet = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isDeviceConfigured = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isSessionOn = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isInventoryOn = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isSearchOn = new MutableLiveData<>(false);
    //temp code (for testing)
    public MutableLiveData<String> searchRssi = new MutableLiveData<>("");
    public MutableLiveData<Integer> searchPercent = new MutableLiveData<>(0);
    public MutableLiveData<Boolean> isPickOn = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isEncodeOn = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isDecodeOn = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isEncodeDone = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> isDecodeDone = new MutableLiveData<>(null);
    public MutableLiveData<Inventory> pickData = new MutableLiveData<>(null);
    public MutableLiveData<List<Inventory>> pickedListData = new MutableLiveData<>(null);
    public MutableLiveData<Boolean> isTriggerPressed = new MutableLiveData<>(false);
    public MutableLiveData<Integer> readerPower = new MutableLiveData<>(30);
    public MutableLiveData<String> readTagPassword = new MutableLiveData<>("");
    public Timer locatebeep;
    protected RFIDInitInterface rfidInterface;
    protected CommonActivity context;
    protected MainReaderRepository mainReaderRepository;
    protected AppCommonMethods.SessionType sessionType = AppCommonMethods.SessionType.OTHER;
    protected AppCommonMethods.SessionAction sessionAction = AppCommonMethods.SessionAction.OTHER;
    protected RFIDSession rfidSession;
    protected String sessionId = null;
    protected String extras = null;
    protected Set<String> eans = null;
    protected String ean = null;
    protected String zone = null;
    protected String zoneId = null;
    protected String fifoDate = null;
    protected int multiWriteListSize = 0;
    protected int multiWriteCount = 0;
    protected boolean isMultiWriteDone = false;
    protected int multiWriteSuccessCount = 0;
    protected InventoryDao inventoryDao;
    protected UploadInventoryDao uploadInventoryDao;
    protected TagIDDao tagIDDao;
    protected boolean readTid = false;
    protected boolean readRssi = false;
    protected boolean readPC = false;
    protected boolean readUser = false;
    protected boolean readType = false;
    protected boolean readEAN = false;
    protected boolean isCommandForSearch = false;
    protected boolean isCommandForEPCSearch = false;
    protected boolean isCommandForTIDSearch = false;
    protected boolean isLockSearchEPC = false;
    protected String SEARCH_LOCKED_EPC = "";
    protected String SEARCH_BARCODE = "";
    protected String SEARCH_EPC = "";
    protected String SEARCH_TID = "";
    protected List<String> SCANNED_TIDS = new ArrayList<String>(0);
    protected int counter_for_threshold_percentage_to_sound_beep = 0;
    protected boolean beepONLocate = false;
    protected boolean isActionPick = false;
    protected boolean isActionTidPick = false;
    protected Timer beepTimer;
    protected int count = 0;
    protected int percent = 0;
    protected boolean isSinglePick = AppCommonMethods.isSinglePick;
    protected Set<String> pickTags = new HashSet<>(0);
    protected Set<Inventory> pickTagData = new HashSet<>(0);
    protected CountDownTimer pickCountDownTimer = null;
    protected Integer pickPower = null;
    protected Set<String> pickedEpcs = new HashSet<>(0);
    protected boolean isHideUnencodedTagsInInventory = false;
    protected Set<String> listIgnoreEpcs = null;
    protected Timer pickTimer = null;
    private final Object inventoryPerformanceLock = new Object();
    private final Set<String> sessionInventoryReadKeys = new HashSet<>(0);
    private final Map<String, Inventory> pendingInventoryWrites = new LinkedHashMap<>(0);
    private final Map<String, TripInventory> pendingTripInventoryWrites = new LinkedHashMap<>(0);
    private Timer inventoryPerformanceTimer = null;
    private long inventoryStartTimeMs = 0L;
    private long inventoryLastBatchFlushMs = 0L;
    private long inventoryLastMetricsLogMs = 0L;
    private long inventoryTotalReads = 0L;
    private long inventoryUniqueReads = 0L;
    private long inventoryDuplicateReads = 0L;
    private final AtomicLong inventoryRawCallbacks = new AtomicLong(0L);
    private final AtomicLong inventoryRssiTotal = new AtomicLong(0L);
    private final AtomicLong inventoryRssiCount = new AtomicLong(0L);
    private final AtomicLong inventoryRssiMin = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong inventoryRssiMax = new AtomicLong(Long.MIN_VALUE);
    private long inventoryLastDbFlushDurationMs = 0L;
    private final AtomicLong inventoryCallbackTotalDurationNs = new AtomicLong(0L);
    private final AtomicLong inventoryCallbackMaxDurationNs = new AtomicLong(0L);
    private final AtomicLong inventorySdkErrorCount = new AtomicLong(0L);
    private final AtomicLong inventorySdkWarningCount = new AtomicLong(0L);
    private volatile String inventoryLastSdkError = "";
    private volatile String inventoryLastSdkWarning = "";
    private final AtomicLong rfidInitCallCount = new AtomicLong(0L);
    private final AtomicLong rfidReleaseCallCount = new AtomicLong(0L);
    private final AtomicLong inventoryStartCallCount = new AtomicLong(0L);
    private final AtomicLong inventoryStopCallCount = new AtomicLong(0L);
    private volatile String rfidLifecycleState = "IDLE";
    private volatile String lastRfidLifecycleEvent = "";
    private volatile long lastRfidLifecycleTimestampMs = 0L;
    private volatile String currentMainThreadOperation = "";
    private volatile String lastMainThreadOperation = "";
    private volatile long lastMainThreadOperationTimestampMs = 0L;
    private RetailDiagnosticLogger inventoryDiagnosticLogger = null;

    protected boolean restrictTriggerPress = false;


    /**
     * On create.
     *
     * @param activity             the activity
     * @param mainReaderRepository the main reader repository
     * @param rfidInterface        the rfid interface
     * @param sessionType          the session type
     */
    public void onCreate(CommonActivity activity, MainReaderRepository mainReaderRepository, RFIDInitInterface rfidInterface, AppCommonMethods.SessionType sessionType) {
        // application context
        try {
            context = activity;
            this.mainReaderRepository = mainReaderRepository;
            this.rfidInterface = rfidInterface;
            this.sessionType = sessionType;
            AppDatabase db = AppDatabase.getDbInstance(context);
            inventoryDao = db.InventoryDao();
            uploadInventoryDao = db.UploadInventoryDao();
            tagIDDao = db.TagIDDao();
            InitSDK();
        } catch (Exception e) {
            e.printStackTrace();
            setProgressMessage(false);
            rfidInterface.RFIDInitializationStatus(false, "RFID initialization Failed", null);
        }
    }

    public void setSession(AppCommonMethods.SessionType sessionType) {
        this.sessionType = sessionType;
        if (!this.isReaderConnected()) checkAndConnectReader();
        else if (sessionType.getValue() > 0 /*&& !chkNotNullTrue(isDeviceConfigured.getValue())*/)
            configureReader(sessionType);
    }

    /**
     * Check and connect reader.
     */
    public abstract void checkAndConnectReader();

    /**
     * Check and connect reader.
     */
    public abstract void checkAndSetReader();

    /**
     * Init sdk.
     */
    public void InitSDK() {
        recordRfidInitCallForDiagnostics("InitSDK");
        markMainThreadOperationForDiagnostics("RFID_INIT");
        resetCommandFlags();
        clearMainThreadOperationForDiagnostics("RFID_INIT");
    }

    /**
     * Set progress message.
     *
     * @param isShowDialog the is show dialog
     */
    public void setProgressMessage(boolean isShowDialog) {
        setProgressMessage("", isShowDialog);
    }

    /**
     * Set progress message.
     *
     * @param message      the message
     * @param isShowDialog the is show dialog
     */
    protected void setProgressMessage(String message, boolean isShowDialog) {
        if (mainReaderRepository != null)
            mainReaderRepository.setProgressMessage(message, isShowDialog);
    }

    /**
     * Reset command flags.
     */
    private void resetCommandFlags() {
        isCommandForTIDSearch = false;
        isCommandForEPCSearch = false;
        isCommandForSearch = false;
        isLockSearchEPC = false;
        readTid = false;
        readRssi = true;
        readPC = false;
        readUser = false;
        readType = true;
        readEAN = true;
        this.SEARCH_EPC = "";
        this.SEARCH_BARCODE = "";
        this.SEARCH_LOCKED_EPC = "";
        this.SCANNED_TIDS.clear();
    }

    /**
     * Perform inventory.
     */
    public boolean performInventory() {
        return performInventory(false);
    }

    public boolean performInventory(final boolean isHideUnencodedTags) {
        return performInventory(isHideUnencodedTags, null);
    }

    public boolean performInventory(final List<String> listIgnoreEpcs) {
        return performInventory(false, listIgnoreEpcs);
    }

    public boolean performInventory(final boolean isHideUnencodedTags, final List<String> listIgnoreEpcs) {
        recordInventoryStartCallForDiagnostics("performInventory");
        markMainThreadOperationForDiagnostics("INVENTORY_START");
        resetCommandFlags();
        //check session on
        if (!chkNotNullTrue(isSessionOn.getValue())) {
            clearMainThreadOperationForDiagnostics("INVENTORY_START");
            return false;
        }
            //check process On
        else if (isProcessOn()) {
            clearMainThreadOperationForDiagnostics("INVENTORY_START");
            return false;
        }
            //check reader connection
        else if (!isReaderConnected()) {
            checkAndConnectReader();
            showLog("performInventory Reader Connection", "Disconnected");
            recordRfidLifecycleEventForDiagnostics("ERROR", "INVENTORY_START_READER_DISCONNECTED");
            clearMainThreadOperationForDiagnostics("INVENTORY_START");
            return false;
        } else if (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.SEARCH_FILE) {
            readTid = true;
            readPC = true;
        } else if (sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD || sessionType == AppCommonMethods.SessionType.MOVEMENT || sessionType == AppCommonMethods.SessionType.SEARCH_UNENCODED || sessionType == AppCommonMethods.SessionType.SEARCH_ALIEN || sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE || sessionType == AppCommonMethods.SessionType.INWARD_TOTE) {
            readTid = true;
        } else if (sessionType == AppCommonMethods.SessionType.OFF_RANGE || sessionType == AppCommonMethods.SessionType.SER_EXCEL)
            readRssi = true;
        isHideUnencodedTagsInInventory = isHideUnencodedTags;
        this.listIgnoreEpcs = isNonEmpty(listIgnoreEpcs) ? new HashSet<String>(listIgnoreEpcs) : null;
        resetInventoryPerformanceState();
        startInventoryDiagnosticLogger();
        recordRfidLifecycleEventForDiagnostics("INVENTORY_RUNNING", "INVENTORY_START");
        clearMainThreadOperationForDiagnostics("INVENTORY_START");
        return true;
    }

    /**
     * Stop inventory.
     */
    public void stopInventory() {
        recordInventoryStopCallForDiagnostics("stopInventory");
        markMainThreadOperationForDiagnostics("INVENTORY_STOP");
        showLog("stopInventory", "" + (isActionPick));
        stopInventoryPerformanceTimer();
        flushInventoryWriteBatches(true);
        logInventoryMetrics(true);
        stopInventoryDiagnosticLogger();
        resetCommandFlags();
        pickPower = null;
        if (this.sessionAction == AppCommonMethods.SessionAction.INVENTORY || chkNotNullTrue(isInventoryOn.getValue()))
            isInventoryOn.postValue(false);
        if (isCommandForSearch || this.sessionAction == AppCommonMethods.SessionAction.SEARCH || chkNotNullTrue(isSearchOn.getValue()))
            isSearchOn.postValue(false);
        if (isActionPick || this.sessionAction == AppCommonMethods.SessionAction.PICK || chkNotNullTrue(isPickOn.getValue()))
            isPickOn.postValue(false);
        if (this.sessionAction == AppCommonMethods.SessionAction.ENCODE || chkNotNullTrue(isEncodeOn.getValue()))
            isEncodeOn.postValue(false);
        if (this.sessionAction == AppCommonMethods.SessionAction.DECODE || chkNotNullTrue(isDecodeOn.getValue()))
            isDecodeOn.postValue(false);
        isActionPick = false;
        isActionTidPick = false;
        isCommandForSearch = false;
        isCommandForEPCSearch = false;
        isCommandForTIDSearch = false;
        showLog("stopInvisActionPick", "" + (isActionPick));
        searchPercent.postValue(0);
        searchRssi.postValue("");
        percent = 0;
        if (pickCountDownTimer != null) pickCountDownTimer.cancel();
        if (pickTimer != null) {
            pickTimer.cancel();
            pickTimer = null;
        }
        if (beepTimer != null) beepTimer.cancel();
        isHideUnencodedTagsInInventory = false;
        if (isNonEmpty(listIgnoreEpcs)) listIgnoreEpcs = null;
        setProgressMessage(false);
        recordRfidLifecycleEventForDiagnostics("IDLE", "INVENTORY_STOP");
        clearMainThreadOperationForDiagnostics("INVENTORY_STOP");
    }

    /**
     * Perform barcode based search.
     *
     * @param SEARCH_BARCODE the search barcode
     */
    public void performBarcodeBasedSearch(String SEARCH_BARCODE) {
        performBarcodeBasedSearch(SEARCH_BARCODE, false);
    }

    public void performBarcodeBasedSearch(String SEARCH_BARCODE, final boolean isLockSearchEPC) {
        if (!chkNotNullTrue(isSessionOn.getValue())) return;
        else if (isProcessOn()) return;
        else if (!isReaderConnected()) {
            checkAndConnectReader();
            showLog("performBarcodeBasedSearch Reader Connection", "Disconnected");
            return;
        } else {
            resetCommandFlags();
            isCommandForSearch = true;
            isCommandForEPCSearch = false;
            isCommandForTIDSearch = false;
            this.isLockSearchEPC = isLockSearchEPC;
            this.SEARCH_LOCKED_EPC = "";
            this.SEARCH_EPC = getSgtin(SEARCH_BARCODE);
            this.SEARCH_BARCODE = SEARCH_BARCODE;
            showLog("Ser_EAN_EPC", SEARCH_BARCODE + "_" + SEARCH_EPC);
        }
        performSearch();
    }

    /**
     * Get sgtin string.
     *
     * @param barcode the barcode
     * @return the string
     */
    private String getSgtin(String barcode) {
        if (isNullOrEmpty(barcode)) return "";
        barcode = AppCommonMethods.getLeftZeroReplacedString(context, barcode);
        return context.epcEncoderDecoder.getEpcFromBarcode(barcode, false);
    }

    /**
     * Perform epc based search.
     *
     * @param sgtin the sgtin
     */
    public void performEPCBasedSearch(String sgtin) {
        if (!chkNotNullTrue(isSessionOn.getValue())) return;
        else if (isProcessOn()) return;
        else if (!isReaderConnected()) {
            checkAndConnectReader();
            showLog("performEPCBasedSearch Reader Connection", "Disconnected");
            return;
        } else {
            resetCommandFlags();
            isCommandForSearch = true;
            isCommandForEPCSearch = true;
            isCommandForTIDSearch = false;
            isLockSearchEPC = false;
            this.SEARCH_LOCKED_EPC = "";
            this.SEARCH_EPC = sgtin;
            this.SEARCH_BARCODE = "";
        }
        performSearch();
    }

    /**
     * Perform tid based search.
     *
     * @param tid the tid
     */
    public void performTIDBasedSearch(String tid) {
        if (!chkNotNullTrue(isSessionOn.getValue())) return;
        else if (isProcessOn()) return;
        else if (!isReaderConnected()) {
            checkAndConnectReader();
            showLog("performTIDBasedSearch Reader Connection", "Disconnected");
            return;
        } else {
            resetCommandFlags();
            readTid = true;
            isCommandForSearch = true;
            isCommandForEPCSearch = false;
            isCommandForTIDSearch = true;
            isLockSearchEPC = false;
            this.SEARCH_LOCKED_EPC = "";
            this.SEARCH_EPC = "";
            this.SEARCH_TID = tid;
            this.SEARCH_BARCODE = "";
        }
        performSearch();
    }

    protected abstract void performSearch();

    protected abstract void addEpcBasedFilters(final String epc, final boolean isNonStdEnc);

    protected abstract void addFilters(final String epc, final boolean isNonStdEnc);

    protected abstract void addTidBasedFilters(final String tid);

    public boolean performTidPick(final String findBarcode, final List<String> tids) {
        if (isProcessOn()) return false;
        else if (!isReaderConnected()) {
            checkAndConnectReader();
            showLog("performPick Reader Connection", "Disconnected");
            return false;
        } else resetCommandFlags();
        return true;
    }

    /**
     * Perform pick.
     *
     * @param barcode the barcode
     */
    private boolean performPick(String barcode) {
        return performPick(barcode, false);
    }

    public boolean performPick(String barcode, int pickPower) {
        return performPick(barcode, false, pickPower);
    }

    public boolean performPick(String barcode, boolean isDecodeOnPick) {
        return performPick(barcode, isDecodeOnPick, 0);
    }

    public boolean performPick(String barcode, List<String> pickedEpcs) {
        return performPick(barcode, false, 0, pickedEpcs);
    }

    public boolean performPick(String barcode, boolean isDecodeOnPick, Integer pickPower) {
        return performPick(barcode, isDecodeOnPick, pickPower, null);
    }

    public boolean performPick(String barcode, Integer pickPower, boolean isPostPicked) {
        return performPick(barcode, pickPower, isPostPicked, null);
    }

    public boolean performPick(String barcode, int pickPower, List<String> pickedEpcs) {
        return performPick(barcode, false, pickPower, pickedEpcs);
    }

    public boolean performPick(String barcode, boolean isPostPicked, List<String> pickedEpcs) {
        return performPick(barcode, false, 0, isPostPicked, pickedEpcs);
    }

    public boolean performPick(String barcode, boolean isDecodeOnPick, Integer pickPower, List<String> pickedEpcs) {
        return performPick(barcode, isDecodeOnPick, pickPower, false, pickedEpcs);
    }

    public boolean performPick(String barcode, Integer pickPower, final boolean isPostPicked, List<String> pickedEpcs) {
        return performPick(barcode, false, pickPower, isPostPicked, pickedEpcs);
    }

    public boolean performPick(String barcode, boolean isDecodeOnPick, Integer pickPower, boolean isPostPicked, List<String> pickedEpcs) {
        this.pickPower = pickPower;
        this.pickedEpcs.clear();
        if (isNonEmpty(pickedEpcs)) this.pickedEpcs.addAll(pickedEpcs);
        if (isProcessOn()) return false;
        else if (!isReaderConnected()) {
            checkAndConnectReader();
            showLog("performPick Reader Connection", "Disconnected");
            return false;
        } else resetCommandFlags();
        readTid = true;
        return true;
    }

    protected abstract void startPick(String barcode, final boolean isDecodeOnPick, final boolean isPostPicked);

    protected abstract void startTidPick(String barcode, List<String> tids);

    /**
     * Configure reader.
     */
    public void configureReader() {
        configureReader(sessionType);
    }

    /**
     * Configure reader.
     */
    public abstract void configureReader(AppCommonMethods.SessionType sessionType);

    /**
     * Start session.
     *
     * @param rfidSession      the rfid session
     * @param isStartInventory the is start inventory
     */
    public synchronized void startSession(RFIDSession rfidSession, boolean isStartInventory) {
        startSession(rfidSession, null, isStartInventory);
    }

    public synchronized void startSession(RFIDSession rfidSession, Set<String> eans, boolean isStartInventory) {
        if (!chkNotNullTrue(isSessionOn.getValue())) {
            showLog("RFID_Handler_rfidSession", "" + rfidSession);
            showLog("RFID_Handler_rfidSession", "" + eans);
            if (rfidSession != null) {
                this.rfidSession = rfidSession;
                this.sessionId = rfidSession.sessionId;
                this.sessionType = AppCommonMethods.SessionType.get(rfidSession.sessionType);
                this.sessionAction = AppCommonMethods.SessionAction.get(rfidSession.sessionAction);
                final BrandEanDao brandEansDao = AppDatabase.getBrandEansDao(context);
                if (isNonEmpty(eans)) this.eans = eans;
                else if((sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY || sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY) && brandEansDao.hasData()){
                    this.eans = new HashSet<String>(0);
                    this.eans.addAll(Arrays.asList(brandEansDao.getAllEans().toString().replaceAll("(\"|\\[,|,\\]|\\[|\\]|,null|null,)", "").replaceAll("\\s*,\\s*", ",").trim().replaceAll(",,,",",").trim().toUpperCase().split(",")));
                } else this.eans = null;
                //this.eans = isNonEmpty(eans)?this.eans.addAll(eans): isNonEmpty(rfidSession.eans) ? Arrays.asList(rfidSession.eans.replaceAll("\\s*,\\s*", ",").trim().split(",")) : null;
                this.extras = rfidSession.extras;
                this.ean = chkNull(rfidSession.eans, "").split(",")[0].replace(AppConstants.ALL, "").trim();
                this.zone = rfidSession.zone;
                this.zoneId = rfidSession.zoneId;
                this.fifoDate = sessionType == AppCommonMethods.SessionType.SEARCH_FIFO ? rfidSession.category : "";
                isSessionOn.postValue(true);
                isSessionOn.setValue(true);
                showLog("RFID_Handler_isInventorySessionOn", "" + (isSessionOn.getValue() != null ? isSessionOn.getValue() : "null"));
                isInventoryOn.postValue(this.sessionAction.getValue() == AppCommonMethods.SessionAction.INVENTORY.getValue() ? false : null);
                isSearchOn.postValue(this.sessionAction.getValue() == AppCommonMethods.SessionAction.SEARCH.getValue() ? false : null);
                isPickOn.postValue(this.sessionAction.getValue() == AppCommonMethods.SessionAction.PICK.getValue() ? false : null);
                isActionPick = false;
                if (isStartInventory) {
                    if (sessionAction == AppCommonMethods.SessionAction.PICK) {
                        performPick(rfidSession.eans);
                    } else performInventory();
                }
            }
        }
    }

    /**
     * Stop session.
     */
    public synchronized void stopSession() {
        if (chkNotNullTrue(isSessionOn.getValue())) {
            if (chkNotNullTrue(isInventoryOn.getValue()) || chkNotNullTrue(isSearchOn.getValue()))
                stopInventory();
            isDeviceConfigured.postValue(false);
            isPickOn.postValue(null);
            isActionPick = false;
            isSearchOn.postValue(null);
            isInventoryOn.postValue(null);
            isSessionOn.postValue(null);
            this.sessionId = null;
            this.sessionType = AppCommonMethods.SessionType.OTHER;
            this.sessionAction = AppCommonMethods.SessionAction.OTHER;
            this.eans = null;
            this.zone = null;
            this.zoneId = null;
            this.extras = null;
            this.pickedEpcs.clear();
        }
    }

    private void resetReaderPower() {
        if (sessionType != null && sessionType.getValue() > 0) {
            SharedPrefManager.setInt(sessionType.name() + SharedPrefManager.SharedPrefKeys.READER_POWER.name(), 0);
        }
    }

    /**
     * Is process on boolean.
     *
     * @return the boolean
     */
    public boolean isProcessOn() {
        showLog("isProcessOn", "isInvOn:" + chkNotNullTrue(isInventoryOn.getValue()) + " isSerOn:" + chkNotNullTrue(isSearchOn.getValue()) + " isPickOn:" + chkNotNullTrue(isPickOn.getValue()) + " isEncOn:" + chkNotNullTrue(isEncodeOn.getValue()) + " isDecOn:" + chkNotNullTrue(isDecodeOn.getValue()));
        return chkNotNullTrue(isInventoryOn.getValue()) || chkNotNullTrue(isSearchOn.getValue()) || chkNotNullTrue(isPickOn.getValue()) || chkNotNullTrue(isEncodeOn.getValue()) || chkNotNullTrue(isDecodeOn.getValue());
    }

    /**
     * Is reader present boolean.
     *
     * @return the boolean
     */
    public boolean isReaderPresent(boolean isReaderInstanceSet) {
        return isReaderInstanceSet;
    }

    /**
     * Is reader connected boolean.
     *
     * @return the boolean
     */
    public boolean isReaderConnected() {
        return false;
    }

    /**
     * Set reader power.
     *
     * @param power the power
     */
    public abstract void setReaderPower(final int power);
  
  /*protected void beeperSettings(){beeperSettings(BEEPER_VOLUME.HIGH_BEEP);}
  
  protected void beeperSettings(BEEPER_VOLUME beeperVolume){
    int percantageVolume = 100;
    if(beeperVolume == BEEPER_VOLUME.HIGH_BEEP) percantageVolume = 100;
    else if(beeperVolume == BEEPER_VOLUME.MEDIUM_BEEP) percantageVolume = 75;
    else if(beeperVolume == BEEPER_VOLUME.LOW_BEEP) percantageVolume = 50;
    else if(beeperVolume == BEEPER_VOLUME.QUIET_BEEP) percantageVolume = 3;
    toneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, percantageVolume);
  }*/

    /**
     * Start timer.
     */
    protected void startTimer() {
        if (inventoryDao == null) inventoryDao = AppDatabase.getInventoryDao(context);
        count = inventoryDao.getInventorySize(sessionId);

        beepTimer = new Timer();
        beepTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //Called each time when 1000 milliseconds (1 second) (the period parameter)
        /*if(chkNotNullTrue(isSearchOn.getValue()) || isCommandForSearch){
          if(percent>0 && chkNull(searchPercent.getValue(), 0) > 0){
            AppCommonMethods.beep();
          }
          else{percent=0;searchPercent.postValue(0);}
        }
        else */
                if (chkNotNullTrue(isInventoryOn.getValue())) {
                    final int oldcount = count;
                    showLog("oldcount", "" + oldcount);
                    count = inventoryDao != null ? inventoryDao.getInventorySize(sessionId) : 0;
                    showLog("count", "" + count);
                }
            }

        }, 0, /*isCommandForSearch?500:*/1000);
    }

    protected boolean acceptInventoryRead(final String epc, final String tid) {
        final String key = getInventoryReadKey(epc, tid);
        if (isNullOrEmpty(key)) return false;
        synchronized (inventoryPerformanceLock) {
            inventoryTotalReads++;
            if (sessionInventoryReadKeys.contains(key)) {
                inventoryDuplicateReads++;
                return false;
            }
            sessionInventoryReadKeys.add(key);
            inventoryUniqueReads++;
        }
        beep();
        return true;
    }

    protected boolean rejectKnownInventoryDuplicate(final String epc, final String tid) {
        final String key = getInventoryReadKey(epc, tid);
        if (isNullOrEmpty(key)) return false;
        synchronized (inventoryPerformanceLock) {
            if (!sessionInventoryReadKeys.contains(key)) return false;
            inventoryTotalReads++;
            inventoryDuplicateReads++;
            return true;
        }
    }

    protected void enqueueInventoryWrite(final Inventory inventory) {
        if (inventory == null) return;
        final String key = getInventoryReadKey(inventory.epc, inventory.tid);
        if (isNullOrEmpty(key)) return;
        final boolean shouldFlush;
        synchronized (inventoryPerformanceLock) {
            pendingInventoryWrites.put(key, inventory);
            shouldFlush = pendingInventoryWrites.size() + pendingTripInventoryWrites.size() >= INVENTORY_BATCH_SIZE;
        }
        if (shouldFlush) flushInventoryWriteBatches(true);
    }

    protected void enqueueTripInventoryWrite(final TripInventory tripInventory) {
        if (tripInventory == null) return;
        final String key = getInventoryReadKey(tripInventory.epc, tripInventory.tid);
        if (isNullOrEmpty(key)) return;
        final boolean shouldFlush;
        synchronized (inventoryPerformanceLock) {
            pendingTripInventoryWrites.put(key, tripInventory);
            shouldFlush = pendingInventoryWrites.size() + pendingTripInventoryWrites.size() >= INVENTORY_BATCH_SIZE;
        }
        if (shouldFlush) flushInventoryWriteBatches(true);
    }

    protected void flushInventoryWriteBatches(final boolean force) {
        final List<Inventory> inventoryBatch;
        final List<TripInventory> tripInventoryBatch;
        synchronized (inventoryPerformanceLock) {
            final int pendingSize = pendingInventoryWrites.size() + pendingTripInventoryWrites.size();
            final long now = System.currentTimeMillis();
            if (pendingSize <= 0) return;
            if (!force && pendingSize < INVENTORY_BATCH_SIZE && now - inventoryLastBatchFlushMs < INVENTORY_BATCH_MAX_DELAY_MS)
                return;
            inventoryBatch = new ArrayList<>(pendingInventoryWrites.values());
            tripInventoryBatch = new ArrayList<>(pendingTripInventoryWrites.values());
            pendingInventoryWrites.clear();
            pendingTripInventoryWrites.clear();
            inventoryLastBatchFlushMs = now;
        }
        final long flushStartMs = System.currentTimeMillis();
        insertInventoryBatch(inventoryBatch);
        insertTripInventoryBatch(tripInventoryBatch);
        synchronized (inventoryPerformanceLock) {
            inventoryLastDbFlushDurationMs = System.currentTimeMillis() - flushStartMs;
        }
    }

    protected void logInventoryMetrics(final boolean force) {
        final long totalReads;
        final long uniqueReads;
        final long duplicateReads;
        final long elapsedMs;
        final long now = System.currentTimeMillis();
        synchronized (inventoryPerformanceLock) {
            if (inventoryStartTimeMs <= 0L || inventoryTotalReads <= 0L) return;
            if (!force && now - inventoryLastMetricsLogMs < INVENTORY_METRICS_LOG_INTERVAL_MS) return;
            inventoryLastMetricsLogMs = now;
            totalReads = inventoryTotalReads;
            uniqueReads = inventoryUniqueReads;
            duplicateReads = inventoryDuplicateReads;
            elapsedMs = now - inventoryStartTimeMs;
        }
        final double readsPerSecond = elapsedMs > 0L ? (totalReads * 1000.0d) / elapsedMs : 0.0d;
        showLog("RFID_INVENTORY_METRICS",
                "session=" + chkNull(sessionId, "") +
                        ", total=" + totalReads +
                        ", unique=" + uniqueReads +
                        ", duplicates=" + duplicateReads +
                        ", reads_per_sec=" + String.format(Locale.US, "%.2f", readsPerSecond) +
                        ", elapsed_ms=" + elapsedMs,
                true);
    }

    private void insertInventoryBatch(final List<Inventory> inventoryBatch) {
        if (isNullOrEmpty(inventoryBatch)) return;
        try {
            if (inventoryDao == null) inventoryDao = AppDatabase.getInventoryDao(context);
            inventoryDao.insertAll(inventoryBatch);
        } catch (SQLiteConstraintException e) {
            for (Inventory inventory : inventoryBatch) {
                try {
                    inventoryDao.insertInventoryData(inventory);
                } catch (SQLiteConstraintException ignored) {
                    // Duplicate already persisted by a previous flush.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertTripInventoryBatch(final List<TripInventory> tripInventoryBatch) {
        if (isNullOrEmpty(tripInventoryBatch)) return;
        final TripInventoryDao tripInventoryDao = AppDatabase.getTripInventoryDao(context);
        try {
            tripInventoryDao.insertAll(tripInventoryBatch);
        } catch (SQLiteConstraintException e) {
            for (TripInventory tripInventory : tripInventoryBatch) {
                try {
                    tripInventoryDao.insertTripInventoryData(tripInventory);
                } catch (SQLiteConstraintException ignored) {
                    // Duplicate already persisted by a previous flush.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetInventoryPerformanceState() {
        stopInventoryPerformanceTimer();
        synchronized (inventoryPerformanceLock) {
            sessionInventoryReadKeys.clear();
            pendingInventoryWrites.clear();
            pendingTripInventoryWrites.clear();
            inventoryTotalReads = 0L;
            inventoryUniqueReads = 0L;
            inventoryDuplicateReads = 0L;
            inventoryRawCallbacks.set(0L);
            inventoryRssiTotal.set(0L);
            inventoryRssiCount.set(0L);
            inventoryRssiMin.set(Long.MAX_VALUE);
            inventoryRssiMax.set(Long.MIN_VALUE);
            inventoryLastDbFlushDurationMs = 0L;
            inventoryCallbackTotalDurationNs.set(0L);
            inventoryCallbackMaxDurationNs.set(0L);
            inventorySdkErrorCount.set(0L);
            inventorySdkWarningCount.set(0L);
            inventoryLastSdkError = "";
            inventoryLastSdkWarning = "";
            inventoryStartTimeMs = System.currentTimeMillis();
            inventoryLastBatchFlushMs = inventoryStartTimeMs;
            inventoryLastMetricsLogMs = inventoryStartTimeMs;
        }
        startInventoryPerformanceTimer();
    }

    private void startInventoryPerformanceTimer() {
        inventoryPerformanceTimer = new Timer("rfid_inventory_batch", true);
        inventoryPerformanceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                flushInventoryWriteBatches(false);
                logInventoryMetrics(false);
            }
        }, INVENTORY_BATCH_MAX_DELAY_MS, INVENTORY_BATCH_MAX_DELAY_MS);
    }

    private void stopInventoryPerformanceTimer() {
        if (inventoryPerformanceTimer != null) {
            inventoryPerformanceTimer.cancel();
            inventoryPerformanceTimer = null;
        }
    }

    private String getInventoryReadKey(final String epc, final String tid) {
        final String epcKey = chkNull(epc, "").trim().toUpperCase(Locale.US);
        if (isNonEmpty(epcKey)) return chkNull(sessionId, "") + "|EPC|" + epcKey;
        final String tidKey = chkNull(tid, "").trim().toUpperCase(Locale.US);
        return isNonEmpty(tidKey) ? chkNull(sessionId, "") + "|TID|" + tidKey : "";
    }

    protected void recordInventoryRawCallbackForDiagnostics() {
        inventoryRawCallbacks.incrementAndGet();
    }

    protected void recordInventoryCallbackDuration(final long durationNs) {
        if (durationNs < 0L) return;
        inventoryCallbackTotalDurationNs.addAndGet(durationNs);
        long currentMaxNs = inventoryCallbackMaxDurationNs.get();
        while (durationNs > currentMaxNs && !inventoryCallbackMaxDurationNs.compareAndSet(currentMaxNs, durationNs)) {
            currentMaxNs = inventoryCallbackMaxDurationNs.get();
        }
    }

    protected void recordInventoryRssiForDiagnostics(final String rssiValue) {
        try {
            if (isNullOrEmpty(rssiValue)) return;
            final long rssi = Math.round(Double.parseDouble(rssiValue));
            inventoryRssiTotal.addAndGet(rssi);
            inventoryRssiCount.incrementAndGet();
            updateAtomicMin(inventoryRssiMin, rssi);
            updateAtomicMax(inventoryRssiMax, rssi);
        }
        catch (Throwable ignored) {
            // RSSI diagnostics are optional and must not interrupt inventory.
        }
    }

    protected void recordInventorySdkErrorForDiagnostics(final String message) {
        inventorySdkErrorCount.incrementAndGet();
        inventoryLastSdkError = chkNull(message, "");
    }

    protected void recordInventorySdkWarningForDiagnostics(final String message) {
        inventorySdkWarningCount.incrementAndGet();
        inventoryLastSdkWarning = chkNull(message, "");
    }

    protected void recordRfidInitCallForDiagnostics(final String eventName) {
        rfidInitCallCount.incrementAndGet();
        recordRfidLifecycleEventForDiagnostics("INITIALIZING", eventName);
    }

    protected void recordRfidReleaseCallForDiagnostics(final String eventName) {
        rfidReleaseCallCount.incrementAndGet();
        recordRfidLifecycleEventForDiagnostics("RELEASING", eventName);
    }

    protected void recordInventoryStartCallForDiagnostics(final String eventName) {
        inventoryStartCallCount.incrementAndGet();
        recordRfidLifecycleEventForDiagnostics("READY", eventName);
    }

    protected void recordInventoryStopCallForDiagnostics(final String eventName) {
        inventoryStopCallCount.incrementAndGet();
        recordRfidLifecycleEventForDiagnostics("STOPPING", eventName);
    }

    protected void recordRfidLifecycleEventForDiagnostics(final String state, final String eventName) {
        rfidLifecycleState = chkNull(state, "");
        lastRfidLifecycleEvent = chkNull(eventName, "");
        lastRfidLifecycleTimestampMs = System.currentTimeMillis();
    }

    protected void markMainThreadOperationForDiagnostics(final String operation) {
        final String safeOperation = chkNull(operation, "");
        currentMainThreadOperation = safeOperation;
        lastMainThreadOperation = safeOperation;
        lastMainThreadOperationTimestampMs = System.currentTimeMillis();
    }

    protected void clearMainThreadOperationForDiagnostics(final String operation) {
        final String safeOperation = chkNull(operation, "");
        if (safeOperation.length() <= 0 || safeOperation.equals(currentMainThreadOperation)) currentMainThreadOperation = "";
    }

    private String getSuspectedBlockingAreaForDiagnostics() {
        if (currentMainThreadOperation.length() > 0) return currentMainThreadOperation;
        return System.currentTimeMillis() - lastMainThreadOperationTimestampMs <= 10000L ? lastMainThreadOperation : "";
    }

    private void updateAtomicMin(final AtomicLong value, final long candidate) {
        long current = value.get();
        while (candidate < current && !value.compareAndSet(current, candidate)) current = value.get();
    }

    private void updateAtomicMax(final AtomicLong value, final long candidate) {
        long current = value.get();
        while (candidate > current && !value.compareAndSet(current, candidate)) current = value.get();
    }

    protected double readUhfModuleTemperatureCForDiagnostics() {
        return Double.NaN;
    }

    protected int readRfPowerDbmForDiagnostics() {
        final Integer power = readerPower != null ? readerPower.getValue() : null;
        return power == null ? -1 : power;
    }

    protected String readInventorySessionForDiagnostics() {
        return "";
    }

    protected String readInventoryTargetForDiagnostics() {
        return "";
    }

    protected String readQValueForDiagnostics() {
        return "";
    }

    protected String readDynamicQEnabledForDiagnostics() {
        return "";
    }

    protected String readAntennaStateForDiagnostics() {
        return chkNotNullTrue(isInventoryOn.getValue()) ? "ENABLED" : "DISABLED";
    }

    protected int readKnownUnreadExpectedForDiagnostics() {
        return -1;
    }

    protected int readKnownUnreadFoundForDiagnostics() {
        return -1;
    }

    private void startInventoryDiagnosticLogger() {
        stopInventoryDiagnosticLogger();
        inventoryDiagnosticLogger = RetailDiagnosticLogger.start(context, sessionId, new RetailDiagnosticLogger.SnapshotProvider() {
            @Override
            public RetailDiagnosticLogger.InventorySnapshot getInventorySnapshot() {
                return getInventoryDiagnosticSnapshot();
            }
        });
    }

    private void stopInventoryDiagnosticLogger() {
        if (inventoryDiagnosticLogger == null) return;
        inventoryDiagnosticLogger.stop();
        inventoryDiagnosticLogger = null;
    }

    private RetailDiagnosticLogger.InventorySnapshot getInventoryDiagnosticSnapshot() {
        final long elapsedSeconds;
        final long duplicateReads;
        final long uniqueReads;
        final int callbackQueueDepth;
        final long lastDbFlushDurationMs;
        synchronized (inventoryPerformanceLock) {
            elapsedSeconds = inventoryStartTimeMs > 0L ? Math.max(0L, (System.currentTimeMillis() - inventoryStartTimeMs) / 1000L) : 0L;
            duplicateReads = inventoryDuplicateReads;
            uniqueReads = inventoryUniqueReads;
            callbackQueueDepth = pendingInventoryWrites.size() + pendingTripInventoryWrites.size();
            lastDbFlushDurationMs = inventoryLastDbFlushDurationMs;
        }
        final long rawCallbacks = inventoryRawCallbacks.get();
        final long rssiCount = inventoryRssiCount.get();
        final long minRssi = inventoryRssiMin.get();
        final long maxRssi = inventoryRssiMax.get();
        return new RetailDiagnosticLogger.InventorySnapshot(
                    chkNull(sessionId, ""),
                    elapsedSeconds,
                    rawCallbacks,
                    duplicateReads,
                    uniqueReads,
                    callbackQueueDepth,
                    lastDbFlushDurationMs,
                    chkNotNullTrue(isInventoryOn.getValue()),
                    rawCallbacks > 0L ? inventoryCallbackTotalDurationNs.get() / 1000000.0d / rawCallbacks : Double.NaN,
                    inventoryCallbackMaxDurationNs.get() / 1000000.0d,
                    readUhfModuleTemperatureCForDiagnostics(),
                    readRfPowerDbmForDiagnostics(),
                    readInventorySessionForDiagnostics(),
                    readInventoryTargetForDiagnostics(),
                    readQValueForDiagnostics(),
                    readDynamicQEnabledForDiagnostics(),
                    readAntennaStateForDiagnostics(),
                    isReaderConnected(),
                    inventorySdkErrorCount.get(),
                    inventoryLastSdkError,
                    inventorySdkWarningCount.get(),
                    inventoryLastSdkWarning,
                    callbackQueueDepth,
                    rssiCount > 0L ? inventoryRssiTotal.get() / (double) rssiCount : Double.NaN,
                    minRssi == Long.MAX_VALUE ? Double.NaN : minRssi,
                    maxRssi == Long.MIN_VALUE ? Double.NaN : maxRssi,
                    readKnownUnreadExpectedForDiagnostics(),
                    readKnownUnreadFoundForDiagnostics(),
                    rfidInitCallCount.get(),
                    rfidReleaseCallCount.get(),
                    inventoryStartCallCount.get(),
                    inventoryStopCallCount.get(),
                    rfidLifecycleState,
                    lastRfidLifecycleEvent,
                    lastRfidLifecycleTimestampMs,
                    lastMainThreadOperation,
                    getSuspectedBlockingAreaForDiagnostics());
    }

    /**
     * Startlocatebeeping timer.
     *
     * @param proximity the proximity
     */
    public void startlocatebeepingTimer(int proximity) {
        showLog("proximity", "" + proximity);
        int POLLING_INTERVAL1 = BEEP_DELAY_TIME_MIN + (((BEEP_DELAY_TIME_MAX - BEEP_DELAY_TIME_MIN) * (100 - proximity)) / 100);
        if (!beepONLocate) {
            beepONLocate = true;
            if (proximity > 0) beep();
            if (locatebeep == null) {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        stoplocatebeepingTimer();
                        beepONLocate = false;
                    }
                };
                locatebeep = new Timer();
                locatebeep.schedule(task, POLLING_INTERVAL1, 10);
            }
        }
    }

    /**
     * Stoplocatebeeping timer.
     */
    public void stoplocatebeepingTimer() {
        if (locatebeep != null) {
            AppCommonMethods.stopBeep();
            locatebeep.cancel();
            locatebeep.purge();
        }
        locatebeep = null;
    }

    /**
     * Get type char code string.
     *
     * @return the string
     */
    public String getTypeCharCode() {
        if (sessionType != null && sessionType.getValue() > 0)
            return sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION ? "C" : sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD ? sessionType.name().substring(1, 2).toUpperCase() : sessionType.name().substring(0, 1);
        else return "";
    }

    public void onResume(AppCommonMethods.SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public void onPause() {
        stopInventory();
    }

    public void onDestroy() {
        recordRfidReleaseCallForDiagnostics("onDestroy");
        markMainThreadOperationForDiagnostics("RFID_RELEASE");
        stopSession();
        SharedPrefManager.setString(PREF_KEY_SAVED_BLUETOOTH_READER_ADDRESS, "");
        recordRfidLifecycleEventForDiagnostics("RELEASED", "RFID_RELEASE");
        clearMainThreadOperationForDiagnostics("RFID_RELEASE");
    }

    public void showLog(final String tag, final String msg) {
        showLog(tag, msg, true);
    }

    public void showLog(final String tag, final String msg, final boolean isViewInRelease) {
        AppCommonMethods.showLog(tag, msg, isViewInRelease);
    }

    public void setFifoDate(final String fifoDate) {
        this.fifoDate = fifoDate;
    }

    protected int getPercentage(int value) {
        value = Math.abs(value);
        int a = 0;
        switch (value) {
            case 15:
                a = 100;
            case 16:
                a = 100;
            case 17:
                a = 100;
            case 18:
                a = 100;
            case 19:
                a = 100;
            case 20:
                a = 100;
            case 21:
                a = 100;
            case 22:
                a = 100;
            case 23:
                a = 100;
            case 24:
                a = 100;
            case 25:
                a = 100;
            case 26:
                a = 100;
            case 27:
                a = 100;
            case 28:
                a = 100;
            case 29:
                a = 100;
            case 30:
                a = 100;
            case 31:
                a = 100;
            case 32:
                a = 100;
            case 33:
                a = 100;
            case 34:
                a = 100;
                break;
            case 35:
                a = 99;
            case 36:
                a = 99;
            case 37:
                a = 99;
            case 38:
                a = 99;
            case 39:
                a = 99;
                break;
            case 40:
                a = 98;
                break;
            case 41:
                a = 97;
                break;
            case 42:
                a = 96;
                break;
            case 43:
                a = 94;
                break;
            case 44:
                a = 92;
                break;
            case 45:
                a = 90;
                break;
            case 46:
                a = 89;
                break;
            case 47:
                a = 87;
                break;
            case 48:
                a = 85;
                break;
            case 49:
                a = 84;
                break;
            case 50:
                a = 82;
                break;
            case 51:
                a = 79;
                break;
            case 52:
                a = 75;
                break;
            case 53:
                a = 72;
                break;
            case 54:
                a = 70;
                break;
            case 55:
                a = 67;
                break;
            case 56:
                a = 65;
                break;
            case 57:
                a = 62;
                break;
            case 58:
                a = 60;
                break;
            case 59:
                a = 57;
                break;
            case 60:
                a = 54;
                break;
            case 61:
                a = 51;
                break;
            case 62:
                a = 48;
                break;
            case 63:
                a = 43;
                break;
            case 64:
                a = 40;
                break;
            case 65:
                a = 36;
                break;
            case 66:
                a = 33;
                break;
            case 67:
                a = 31;
                break;
            case 68:
                a = 29;
                break;
            case 69:
                a = 27;
                break;
            case 70:
                a = 25;
                break;
            case 71:
                a = 23;
                break;
            case 72:
                a = 21;
                break;
            case 73:
                a = 19;
                break;
            case 74:
                a = 17;
                break;
            case 75:
                a = 15;
                break;
            case 76:
                a = 13;
                break;
            case 77:
                a = 11;
                break;
            case 78:
                a = 10;
                break;
            case 79:
                a = 8;
                break;
            case 80:
                a = 7;
                break;
            case 81:
                a = 6;
                break;
            case 82:
                a = 5;
                break;
            case 83:
                a = 4;
                break;
            case 84:
                a = 3;
                break;
            case 85:
                a = 2;
                break;
            case 86:
                a = 1;
                break;
            default:
                a = 0;
                break;
        }
        return a;
    }

    protected int getPercentageForGID(int value) {
        value = Math.abs(value);
        int a = 0;
        switch (value) {
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
                //pantaloons
                a = 100;
                break;
            case 25:
                a = 100;
                break;
            case 26:
                a = 100;
                break;
            case 27:
                a = 100;
                break;
            case 28:
                a = 100;
                break;
            case 29:
                a = 100;
                break;
            case 30:
                a = 99;
                break;
            case 31:
                a = 98;
                break;
            case 32:
                a = 97;
                break;
            case 33:
                a = 96;
                break;
            case 34:
                a = 95;
                break;
            case 35:
                a = 93;
                break;
            case 36:
                a = 91;
                break;
            case 37:
                a = 89;
                break;
            case 38:
                a = 87;
                break;
            case 39:
                a = 85;
                break;
            case 40:
                a = 84;
                break;
            case 41:
                a = 83;
                break;
            case 42:
                a = 82;
                break;
            case 43:
                a = 81;
                break;
            case 44:
                a = 80;
                break;
            case 45:
                a = 79;
                break;
            case 46:
                a = 78;
                break;
            case 47:
                a = 77;
                break;
            case 48:
                a = 76;
                break;
            case 49:
                a = 75;
                break;
            case 50:
                a = 74;
                break;
            case 51:
                a = 73;
                break;
            case 52:
                a = 70;
                break;
            case 53:
                a = 68;
                break;
            case 54:
                a = 63;
                break;
            case 55:
                a = 60;
                break;
            case 56:
                a = 55;
                break;
            case 57:
                a = 52;
                break;
            case 58:
                a = 50;
                break;
            case 59:
                a = 48;
                break;
            case 60:
                a = 47;
                break;
            case 61:
                a = 44;
                break;
            case 62:
                a = 42;
                break;
            case 63:
                a = 41;
                break;
            case 64:
                a = 40;
                break;
            case 65:
                a = 36;
                break;
            case 66:
                a = 33;
                break;
            case 67:
                a = 30;
                break;
            case 68:
                a = 28;
                break;
            case 69:
                a = 25;
                break;
            case 70:
                a = 22;
                break;
            case 71:
                a = 20;
                break;
            case 72:
                a = 18;
                break;
            case 73:
                a = 15;
                break;
            case 74:
                a = 13;
                break;
            case 75:
                a = 12;
                break;
            case 76:
                a = 11;
                break;
            case 77:
                a = 10;
                break;
            case 78:
                a = 9;
                break;
            case 79:
                a = 8;
                break;
            case 80:
                a = 7;
                break;
            case 81:
                a = 6;
                break;
            case 82:
                a = 5;
                break;
            case 83:
                a = 4;
                break;
            case 84:
                a = 3;
                break;
            case 85:
                a = 2;
                break;
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
                a = 1;
                break;
            default:
                a = 0;
                break;
        }
        return a;
    }

    protected String bytesToHex(byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public abstract Object getReaderInstance(CommonActivity context, MainReaderRepository mainReaderRepository, RFIDInitInterface rfidInitInterface);
  /*public void configureSessionAction(){configureSessionAction(sessionAction);}
  public abstract void configureSessionAction(AppCommonMethods.SessionAction sessionAction);*/

    public void performEncoding(final Inventory pickedTag) {
        performEncoding(pickedTag, "", null);
    }

    public abstract void performEncoding(final Inventory pickedTag, final String currentTagPassword);//{ performEncoding(pickedTag, currentTagPassword,null); }

    public void performEncoding(final Inventory pickedTag, final Bundle extras) {
        performEncoding(pickedTag, "", extras);
    }

    public void performEncoding(final Inventory pickedTag, final String currentTagPassword, final Bundle extras) {
        if (isNonEmpty(extras)) {
            try {
                JSONObject jobj = isNonEmpty(this.extras) ? new JSONObject(this.extras) : new JSONObject();
                for (String key : extras.keySet())
                    jobj.put(key, extras.get(key));
                this.extras = jobj.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        performEncoding(pickedTag, currentTagPassword);
    }

    public void performEncoding(List<Inventory> listPickedTags) {
        performEncoding(listPickedTags, sessionType);
    }

    public abstract void performEncoding(List<Inventory> listPickedTags, final AppCommonMethods.SessionType sessionType);

    public abstract void readTagCurrentPassword(final Inventory pickedTag);

  public void performDecoding(final Inventory pickedTag, Bundle extras){
    if(isNonEmpty(extras)){
      try{
        JSONObject jobj = isNonEmpty(this.extras) ? new JSONObject(this.extras) : new JSONObject();
        for(String key : extras.keySet())
          jobj.put(key, extras.get(key));
        this.extras = jobj.toString();
      }
      catch(Exception e){ e.printStackTrace(); }
    }
    performDecoding(pickedTag);
  }
  
    public abstract void performDecoding(final Inventory pickedTag);

    public void performDecoding(final List<Inventory> listPickedTags) {
        performDecoding(listPickedTags, sessionType);
    }

    public void performDecoding(final AppCommonMethods.SessionType sessionType) {
        performDecoding(inventoryDao.getAllNonDecodedInventoryData(sessionType.getValue()), sessionType);
    }

    public abstract void performDecoding(List<Inventory> listPickedTags, final AppCommonMethods.SessionType sessionType);

    protected final void updateTagWriteCount(final String errMsg) {
        updateTagWriteCount(false, errMsg);
    }

    protected final void updateTagWriteCount(final boolean isTagWriteSuccess) {
        updateTagWriteCount(isTagWriteSuccess, "");
    }

    protected final void updateTagWriteCount(final boolean isTagWriteSuccess, final String errMsg) {
        final boolean isMultiWrite = multiWriteListSize > 0;
        if (isMultiWrite && !isMultiWriteDone) {
            if (isTagWriteSuccess) {
                multiWriteSuccessCount++;
                showLog("Done:", multiWriteSuccessCount + " of " + multiWriteListSize);
            }
            multiWriteCount--;
        }
        if (multiWriteCount == 0 && !isMultiWriteDone) {
            isMultiWriteDone = true;
            final boolean isDone = isTagWriteSuccess || (isMultiWrite && multiWriteSuccessCount > 0);
            final boolean isActionEncode = sessionAction == AppCommonMethods.SessionAction.ENCODE;
            if ((sessionAction == AppCommonMethods.SessionAction.ENCODE || sessionAction == AppCommonMethods.SessionAction.DECODE) && multiWriteCount == 0) {
                (isActionEncode ? isEncodeDone : isDecodeDone).setValue(isDone);
                //        new Handler().postDelayed(new Runnable(){
                //          @Override
                //          public void run(){
                setProgressMessage(false);
                if (isDone)
                    context.showCustomSuccessDialog(context.getString(isActionEncode ? R.string.success_encoding : R.string.success_decoding) + (isMultiWrite ? "\n" + "( " + multiWriteSuccessCount + " / " + multiWriteListSize + " )" : ""));
                else
                    context.showCustomErrDialog(chkNull(isMultiWrite ? "" : errMsg, context.getString(isActionEncode ? R.string.err_encoding_fail : R.string.err_decoding_fail)));
                (isActionEncode ? isEncodeOn : isDecodeOn).postValue(false);
                //}
                //}, isMultiWrite ? 50 : 0);
            }
        }
    }

    protected final boolean validateTagInfoForInventory(final String epc) {
        return validateTagInfoForInventory(epc, "");
    }

    protected final boolean isErrorOrBeepForTote() {
        return isShowErrorForSameToteTag || isShowErrorForOtherThanToteEanTag || isPlayBeepForSameToteTagIfNotLastInserted || isPlayErrorBeepForOtherThanToteEanTag;
    }

    protected final boolean validateTagInfoForInventory(final String epc, final String tid) {
        if (isCommandForSearch || isActionPick || sessionAction != AppCommonMethods.SessionAction.INVENTORY) return false;
        final String epcdt = chkNull(epc, "");
        final String ean = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt).toUpperCase().trim();
        if ((sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN) && sessionAction != AppCommonMethods.SessionAction.ENCODE) return false;
        if ((sessionType == AppCommonMethods.SessionType.OUTWARD_TOTE || sessionType == AppCommonMethods.SessionType.INWARD_TOTE) && isErrorOrBeepForTote() && isNonEmpty(eans)) {
            if ((AppCommonMethods.isShowErrorForOtherThanToteEanTag || AppCommonMethods.isPlayErrorBeepForOtherThanToteEanTag) && isNonEmpty(eans) && !eans.contains(ean)) {
                if (AppCommonMethods.isShowErrorForOtherThanToteEanTag) {
                    stopInventory();
                    context.showCustomErrDialog(String.format(context.getString(R.string.err_msg_pick_wrong_tote), ean));
                    return false;
                } else if (AppCommonMethods.isPlayErrorBeepForOtherThanToteEanTag) errorBeep();
            } else if (AppCommonMethods.isShowErrorForSameToteTag && inventoryDao.isEPCPresent(sessionId, epcdt)) {
                stopInventory();
                context.showCustomErrDialog(R.string.err_msg_already_added);
                return false;
            } else if (AppCommonMethods.isPlayBeepForSameToteTagIfNotLastInserted && !chkNull(inventoryDao.getLastInsertedEpc(sessionId), "").equalsIgnoreCase(epc))
                beep();
        }
        //Checks only Selected Ean Tags
        if (isNonEmpty(eans) && !eans.contains(ean)) return false;
        //Check if Duplicate EPC (i.e. already present)
        if (inventoryDao.isEPCPresent(sessionId, epcdt)) return false;
        //Check if Unencoded Tags should be scanned
        if (this.isHideUnencodedTagsInInventory && ean.equalsIgnoreCase(AppConstants.NON_ENCODED)) return false;
        //Check if Epcs Should be ignored
        if (isNonEmpty(listIgnoreEpcs) && listIgnoreEpcs.contains(epcdt)) return false;

        //Checks if EPC present in productDao
        if (sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION && !AppDatabase.getProductDao(context).isEPCPresent(epcdt))
            return false;
        //Checks only Non-Alien Decoded Tags
        if (sessionType == AppCommonMethods.SessionType.SEARCH_UNENCODED && !context.epcEncoderDecoder.setDataFromEPC(new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue(), epcdt)).tagStatus.equalsIgnoreCase(AppConstants.NON_ENCODED))
            return false;
        //Checks only Alien Tags
        if (sessionType == AppCommonMethods.SessionType.SEARCH_ALIEN && !context.epcEncoderDecoder.setDataFromEPC(new Inventory(sessionId, sessionType.getValue(), sessionAction.getValue(), epcdt)).tagStatus.equalsIgnoreCase(AppConstants.ALIEN))
            return false;
        //Checks if EPC present in fifoDao
        return sessionType != AppCommonMethods.SessionType.SEARCH_FIFO || sessionAction != AppCommonMethods.SessionAction.SEARCH || isCommandForEPCSearch || AppDatabase.getFIFODao(context).isEPCPresent(epcdt, ean, fifoDate/*,zone,zoneId*/);
    }

    protected final boolean validateTagInfoForSearch(final String epc, final String rssi) {
        return validateTagInfoForSearch(epc, rssi, "");
    }

    protected final boolean validateTagInfoForSearch(final String epc, final String rssi, final String tid) {
        if (!isCommandForSearch || sessionAction != AppCommonMethods.SessionAction.SEARCH)
            return false;
        final String epcdt = chkNull(epc, "");
        return sessionType != AppCommonMethods.SessionType.SEARCH_FIFO || sessionAction != AppCommonMethods.SessionAction.SEARCH || isCommandForEPCSearch || AppDatabase.getFIFODao(context).isEPCPresent(epcdt, ean, fifoDate/*,zone,zoneId*/);
    }

    protected final void handleTagInfoForSearch(final String epc, final String rssiVal, final String tid) {
        if (validateTagInfoForSearch(epc, rssiVal)) {
            Integer rssi = null;
            try {
                rssi = Integer.parseInt(rssiVal);
            } catch (Exception e) {
                return;
            }
            showLog(sessionType.name() + sessionAction.name() + " SEARCH_EPC == epc", SEARCH_EPC + "==" + epc);
            showLog(sessionType.name() + sessionAction.name() + " rssi", "" + rssi);
            int actualPercentage = context.epcEncoderDecoder.isGID() && SEARCH_EPC.startsWith("35") ? getPercentageForGID(rssi) : getPercentage(rssi);
            percent = actualPercentage;
            showLog(sessionType.name() + sessionAction.name() + " actualPer", "" + actualPercentage);
            if (!isCommandForEPCSearch && !isCommandForTIDSearch) {
                showLog("SEARCH_LOCKED_EPC", isLockSearchEPC + "_" + SEARCH_LOCKED_EPC);
                if (isLockSearchEPC && isNullOrEmpty(SEARCH_LOCKED_EPC)) SEARCH_LOCKED_EPC = epc;
                if (!isLockSearchEPC || isNullOrEmpty(SEARCH_LOCKED_EPC) || epc.equalsIgnoreCase(SEARCH_LOCKED_EPC)) {
                    if (epc.length() >= 24) {
                        //check by using getBarcode method instead of switch case
                        final String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epc);
                        final String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context, SEARCH_BARCODE);
                        showLog("search_barcode", barcode);
                        showLog("search_compare_barcode", compbarcode);
                        if (!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase()))) {
                            showLog("RFIDHANDLER_barcode", barcode);
                            searchPercent.postValue(actualPercentage);
                            searchRssi.postValue(String.valueOf(rssi));
                            if (actualPercentage > 90) {
                                counter_for_threshold_percentage_to_sound_beep++;
                                if (counter_for_threshold_percentage_to_sound_beep >= SOUND_THRESHOLD) {
                                    counter_for_threshold_percentage_to_sound_beep = 0;
                                    playSound(context, R.raw.successbeep);
                                }
                            }
                        }
                    }
                }
            }
            else if (isCommandForEPCSearch) {
                if (epc.equalsIgnoreCase(SEARCH_EPC)) {
                /*if(sessionType == AppCommonMethods.SessionType.ENCODING || rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue()){
                  updateEncVerifyByEpc(epcdt);
                }*/
                    searchPercent.postValue(actualPercentage);
                    searchRssi.postValue(String.valueOf(rssi));
                    if (actualPercentage > 90) {
                        counter_for_threshold_percentage_to_sound_beep++;
                        if (counter_for_threshold_percentage_to_sound_beep >= SOUND_THRESHOLD) {
                            counter_for_threshold_percentage_to_sound_beep = 0;
                            playSound(context, R.raw.successbeep);
                        }
                    }
                }
            }
            else if (isCommandForTIDSearch) {
                showLog("tid_search", tid + "_" + SEARCH_TID);
                if (tid.equalsIgnoreCase(SEARCH_TID)) {
                    if ((sessionType == AppCommonMethods.SessionType.ENCODING || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue())) || (sessionType == AppCommonMethods.SessionType.ENCODING_THAN || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING_THAN.getValue()))) {
                        updateFoundWrittenTag(epc, tid);
                        updateEncVerifyByEpcTid(epc, tid);
                    }
                    searchPercent.postValue(actualPercentage);
                    searchRssi.postValue(String.valueOf(rssi));
                    if (actualPercentage > 90) {
                        counter_for_threshold_percentage_to_sound_beep++;
                        if (counter_for_threshold_percentage_to_sound_beep >= SOUND_THRESHOLD) {
                            counter_for_threshold_percentage_to_sound_beep = 0;
                            playSound(context, R.raw.successbeep);
                        }
                    }
                }
            }
        }
    }

    protected final void handleTagInfoForPick(final Object tagData, final String epc, final String tidVal) {
        final String tid = tidVal.length() > 24 ? tidVal.substring(0, 24) : tidVal;
        if (isActionTidPick) showLog("tid_pick", tid);
        if (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || epc.length() >= 24) {
            if (isSinglePick && ((!isActionTidPick && !pickTags.contains(epc)) || (isActionTidPick && !pickTags.contains(tid) && SCANNED_TIDS.contains(tid) /*uhftagInfo.getTid().matches("(?i)(^" + SCANNED_TID + ".*$)")*/))) {
                pickTags.add(isActionTidPick ? tid : epc);
                pickTagData.add(getDataFromTagInfo(tagData)); // handle this
                if (isActionTidPick && pickTags.size() == SCANNED_TIDS.size() && pickCountDownTimer != null) {
                    pickCountDownTimer.cancel();
                    pickCountDownTimer.onFinish();
                } else if (pickTags.size() > (isActionTidPick ? SCANNED_TIDS.size() : 1)) {
                    stopInventory();
                    setProgressMessage(false);
                    context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
                }
            }
        } else {
            stopInventory();
            context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
            setProgressMessage(false);
      return;
        }
    }

    protected final void handleTagInfoForInventory(final Object tagData, final String epc, final String tid, final String rssiVal) {
        final long callbackStartNs = System.nanoTime();
        recordInventoryRawCallbackForDiagnostics();
        recordInventoryRssiForDiagnostics(rssiVal);
        try {
        if ((sessionType == AppCommonMethods.SessionType.ENCODING || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING.getValue())) || (sessionType == AppCommonMethods.SessionType.ENCODING_THAN || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.ENCODING_THAN.getValue()))) {
            showLog("Inv_ENC", sessionId + "_" + sessionType + "_" + sessionAction);
            updateFoundWrittenTag(epc, tid);
            if (inventoryDao.getNonVerifiedCount(sessionId) <= 0) {
                showLog("Inv_ENC", "all verified");
                stopInventory();
                context.showCustomSuccessDialog("Verified!");
            } else {
                showLog("Inv_ENC_epc_tid", epc + "_" + tid);
                updateEncVerifyByEpcTid(epc, tid);
            }
        } else if (sessionType == AppCommonMethods.SessionType.SEARCH_FILE || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.SEARCH_FILE.getValue())) {
            showLog("Inv_SER_FILE_epc_tid", epc + "_" + tid);
            final boolean isEPCPresent = inventoryDao.isEPCPresent(sessionId, epc);
            final boolean isTidPresent = inventoryDao.isTidPresent(sessionId, tid);
            showLog("Inv_SER_FILE_epc_tid", epc + "_" + tid + "_" + isEPCPresent + "_" + isTidPresent);
            if (isEPCPresent || isTidPresent) {
                final int status = AppCommonMethods.EncodeVerifyStatus.RE_ENCODED.ordinal();
                if (isTidPresent) {
                    inventoryDao.updateEncVerifyStatusByTid(sessionId, tid, status);
                } else if (isEPCPresent) {
                    inventoryDao.updateStatusByEpc(sessionId, epc, status);
                }
            }
        } else if ((sessionType == AppCommonMethods.SessionType.OFF_RANGE || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.OFF_RANGE.getValue())) ||
                (sessionType == AppCommonMethods.SessionType.SER_EXCEL || (rfidSession != null && rfidSession.sessionType == AppCommonMethods.SessionType.SER_EXCEL.getValue()))) {
            final String ean = context.epcEncoderDecoder.getBarcodeFromEPC(epc);
            final int rssi = readRssi ? (int) Math.round(Double.parseDouble(rssiVal)) : 0;
            int actualPercentage = getPercentage(rssi);
            if (isNonEmpty(ean) && isNonEmpty(eans) && eans.contains(ean)) {
                showLog("off_matched", "true");
                final ProductDao productDao = AppDatabase.getProductDao(context);
                productDao.updateRssiPercentage(sessionType.getValue(), ean, actualPercentage);
            }
        } else if (!rejectKnownInventoryDuplicate(epc, tid) && validateTagInfoForInventory(epc))
            storeInventoryData(getDataFromTagInfo(tagData));
        } finally {
            recordInventoryCallbackDuration(System.nanoTime() - callbackStartNs);
        }
    }

    protected abstract Inventory getDataFromTagInfo(Object object);

    private void storeInventoryData(Inventory inventory) {
        try {
            if (isNonEmpty(sessionId) && sessionType.getValue() > 0) {
                if (inventory == null || !acceptInventoryRead(inventory.epc, inventory.tid)) return;
                inventory.zone = zone;
                inventory.zoneId = zoneId;
                try {
                    inventory = context.epcEncoderDecoder.setDataFromEPC(inventory);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                inventory.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
                if (/*!AppCommonMethods.isSetInwOnline &&*/ (sessionType == AppCommonMethods.SessionType.INWARD || sessionType == AppCommonMethods.SessionType.OUTWARD)) {
                    //Set 3rd Table for this
                    final String tripNo = SharedPrefManager.getTripNo();
                    final String huNo = SharedPrefManager.getHuNo();
                    final String ean = chkNull(inventory.ean, "").replaceFirst(AppConstants.UNKNOWN, AppConstants.NON_ENCODED);
                    final TripInventoryDao tripInventoryDao = AppDatabase.getTripInventoryDao(context);
                    final String articleCode = !chkNull(ean, AppConstants.NON_ENCODED).equalsIgnoreCase(AppConstants.NON_ENCODED) ? chkNull(tripInventoryDao.getArticleCode(ean, huNo, tripNo), AppConstants.EXTRA_EAN) : AppConstants.NON_ENCODED;
                    final Integer originalEanQty = !chkNull(articleCode, AppConstants.NON_ENCODED).matches("(?i)(" + AppConstants.NON_ENCODED + "|" + AppConstants.EXTRA_EAN + ")") ? tripInventoryDao.getOriginalArticleQty(tripNo, huNo, articleCode) : 0;

                    TripInventory tripInventory = new TripInventory(tripNo, SharedPrefManager.getDeliveryNo(), huNo);
                    tripInventory.userAction = "RFID";
                    tripInventory.ean = ean;
                    tripInventory.eanQty = originalEanQty;
                    tripInventory.tid = inventory.tid;
                    tripInventory.epc = inventory.epc;
                    tripInventory.rssi = inventory.rssi;
                    tripInventory.isOriginal = originalEanQty > 0;
                    tripInventory.articleCode = articleCode;
                    tripInventory.isHardTag = inventory.isHardTag;
                    if (!tripInventoryDao.isEpcPresent(tripNo, huNo, inventory.epc))
                        enqueueTripInventoryWrite(tripInventory);
                } else {
                    if (isNullOrEmpty(sessionId) || (isNullOrEmpty(inventory.tid) && (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO || sessionType == AppCommonMethods.SessionType.SEARCH_LIST)))
                        throw new NullPointerException();
                    enqueueInventoryWrite(inventory);
                    final ProductDao productDao = AppDatabase.getProductDao(context);
                    final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                    if (productDao != null && sessionType == AppCommonMethods.SessionType.STOCK_CORRECTION) {
                        if (zone.equalsIgnoreCase(AppConstants.ALL))
                            productDao.updateFound(inventory.epc);
                        else productDao.updateFound(inventory.epc, zone);
                    }
                    if (productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
                        productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
                    if (productDao != null && sessionType == AppCommonMethods.SessionType.OUTWARD_PICK)
                        productDao.updateFoundEPC(inventory.epc, inventory.ean, zone);
                    if (productDao != null && sessionType == AppCommonMethods.SessionType.OFF_RANGE)
                        productDao.updateFoundEPCOffRange(inventory.epc, inventory.ean, zone);
                    if (fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO && fifoDate != null && (isActionPick || sessionAction == AppCommonMethods.SessionAction.PICK))
                        fifoDao.updateFound(inventory.ean, inventory.epc, fifoDate);
          if(sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY)// || sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY)
                        AppDatabase.getBrandEansDao(context).updateScanQty("," + inventory.ean + ",");
                }
                //}
            }
        } catch (SQLiteConstraintException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onPickTimerFinish(final String findBarcode, final boolean isDecodeOnPick) {
        showLog("onFinish", "onFinish");
        stopInventory();
        if (isNullOrEmpty(pickTags)) {
            context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
            setProgressMessage(false);
        } else if (pickTags.size() > 1) {
            setProgressMessage(false);
            context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_multi_tag), getTypeCharCode()));
        } else if (pickTags.size() == 1) {
            final Inventory tagData = new ArrayList<Inventory>(pickTagData).get(0);
            final String epcdt = chkNull(tagData.epc, "");
            final String tid = chkNull(tagData.tid, "");
            setProgressMessage(false);
            updateFoundWrittenTag(epcdt, tid);
            if (sessionType == AppCommonMethods.SessionType.OFF_RANGE && uploadInventoryDao.isEPCPresent(sessionType.getValue(), epcdt)) {
                AppCommonMethods.logInFile(context, sessionType.name(), "_PICK_STOP (Tag Already Picked)");
                final UploadInventory ui = uploadInventoryDao.getBysessionTypeAndEpc(sessionType.getValue(), epcdt);
                if (ui != null && isNonEmpty(ui.remark) && isNonEmpty(ui.fifoDate))
                    context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag_in_carton), getTypeCharCode(), ui.remark));
                else
                    context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
                setProgressMessage(false);
            } else if (sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING_THAN && sessionType != AppCommonMethods.SessionType.DECODING && inventoryDao.isEPCPresent(findBarcode, sessionId, epcdt)) {
                context.showCustomErrDialog(String.format(context.getString(R.string.err_pick_already_picked_tag), getTypeCharCode()));
            } else {
                boolean isMatchingBarcode = false;
                //TODO
                //final String epc = ((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && chkNull(epcdt, "").length() > 2) ? chkNull(epcdt, "").startsWith("0C") ? epcdt.replaceFirst("0C", "BC") : chkNull(epcdt, "").startsWith("05") ? epcdt.replaceFirst("05", "35") : chkNull(epcdt, "").startsWith("00") ? (epcdt.length() >= 32) ? epcdt.replaceFirst("00", "BC") : (epcdt.length() >= 24) ? epcdt.replaceFirst("00", "35") : epcdt : epcdt : epcdt;
                //final String epc = ((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && chkNull(epcdt, "").length() > 2) ? chkNull(epcdt, "").startsWith("0C") ? epcdt.replaceFirst("0C", "BC") : chkNull(epcdt, "").startsWith("00") ? (epcdt.length() >= 32) ? epcdt.replaceFirst("00", "BC") : (epcdt.length() >= 24) ? epcdt.replaceFirst("00", "30") : epcdt : epcdt : epcdt;
                //final String header = epc.length() > 2 ? epc.substring(0, 2) : "";
                String matchingBarcode = "";
                //showLog("pick_finish_epc_header", header);

                //check by using getBarcode method instead of switch case
                String barcode = context.epcEncoderDecoder.getBarcodeFromEPC(epcdt, sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING);
                String compbarcode = AppCommonMethods.getLeftZeroReplacedString(context, findBarcode);
                showLog("pick_barcode", barcode);
                showLog("pick_compare_barcode", compbarcode);
                final FIFODao fifoDao = AppDatabase.getFIFODao(context);
                if (!barcode.equalsIgnoreCase(AppConstants.UNKNOWN) && !barcode.equalsIgnoreCase(AppConstants.NON_ENCODED) && (barcode.equalsIgnoreCase(compbarcode) || barcode.toUpperCase().contains(compbarcode.toUpperCase())) && (sessionType != AppCommonMethods.SessionType.SEARCH_FIFO || fifoDao.isEPCPresent(epcdt, ean, fifoDate/*,zone,zoneId*/))) {
                    isMatchingBarcode = true;
                    matchingBarcode = AppCommonMethods.getLeftZeroReplacedString(context, barcode);
                } else if (sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING_THAN && sessionType != AppCommonMethods.SessionType.DECODING) {
                    if (isNullOrEmpty(barcode) || barcode.equalsIgnoreCase(AppConstants.UNKNOWN) || barcode.equalsIgnoreCase(AppConstants.NON_ENCODED))
                        context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                    else if (isNonEmpty(barcode))
                        context.showCustomAlertDialog(String.format(context.getString(R.string.err_pick_wrong_tag_header), getTypeCharCode()), String.format(context.getString(R.string.err_pick_wrong_tag)/*,getTypeCharCode()*/, barcode), context.getString(R.string.btn_ok), null);
                }
                showLog("isMatchingBarcode", "" + isMatchingBarcode);

                if ((isMatchingBarcode /*&& isNonEmpty(matchingBarcode)*/) || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING) {
                    readTid = true;
                    readRssi = true;
                    readEAN = true;
                    readPC = true;//sessionType == AppCommonMethods.SessionType.ENCODING;// || sessionType == AppCommonMethods.SessionType.DECODING;
                    if (sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING) {
                        final Inventory pickedTag = getDataFromTagInfo(tagData);
                        if ((sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING) && isMatchingBarcode && isNonEmpty(matchingBarcode))
                            pickedTag.ean = AppCommonMethods.getLeftZeroReplacedString(context, matchingBarcode);
                        if (isNullOrEmpty(matchingBarcode) && sessionType != AppCommonMethods.SessionType.ENCODING && sessionType != AppCommonMethods.SessionType.ENCODING_THAN && (sessionType != AppCommonMethods.SessionType.DECODING || !SharedPrefManager.getBoolean(ParamConstants.IS_ALLOW_NON_ENCODED_TAG_PICK_FOR_DECODE, AppCommonMethods.isAllowNonEncodedTagPickForDecode)))
                            context.showCustomErrDialog(String.format(context.getString(sessionType == AppCommonMethods.SessionType.SCAN || sessionType == AppCommonMethods.SessionType.VERIFY_ENCODING || sessionType == AppCommonMethods.SessionType.DECODING ? R.string.err_non_encoded_tag : R.string.err_pick_non_encoded_tag), getTypeCharCode()));
                        else if ((sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING) && (pickedTag == null || isNullOrEmpty(pickedTag.epc) || isNullOrEmpty(pickedTag.tid)))
                            context.showCustomAlertDialog("", String.format(context.getString(R.string.err_pick_no_tag), getTypeCharCode()), false, false, context.getString(R.string.btn_ok), null);
                        else pickData.postValue(pickedTag);
                    } else {
                        storeInventoryData(tagData);
                        if (isDecodeOnPick) {
                            final Inventory pickedTag = getDataFromTagInfo(tagData);
                            pickData.postValue(pickedTag);
                        }
                    }
                }
            }
        }
    }

    protected void updateFoundWrittenTag(final String epc, final String tid) {
        if (isNullOrEmpty(epc) || isNullOrEmpty(tid)) return;
        if (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN || sessionType == AppCommonMethods.SessionType.DECODING || sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST || sessionType == AppCommonMethods.SessionType.SEARCH_FIFO) {
            new UpdateWrittenFoundTagsTask(context, sessionType, sessionId, epc, tid, fifoDate, extras).execute();
      /*if(inventoryDao.isEpcAndTidPresent(sessionType.getValue(),epc,tid)){
        //Update in DB for decode if the scanned Tag is already encoded/decoded with expected epc & tid
        Inventory tagInfo = inventoryDao.getInventoryByEpcAndTid(sessionType.getValue(), epc, tid);
        if(!tagInfo.isUploaded){
          tagInfo.insertTime = new SimpleDateFormat(AppCommonMethods.SERVER_DATE_TIME_FORMAT).format(new Date(System.currentTimeMillis()));
          tagInfo.writeFailReason = null;
          tagInfo.isUploaded = false;
          inventoryDao.updateInventoryData(tagInfo);
          try{
            uploadInventoryDao.insertUploadInventoryData(new UploadInventory(tagInfo, extras));
          }
          catch(Exception e){ e.printStackTrace(); }
          try{
            final ProductDao productDao = AppDatabase.getProductDao(context);
            final FIFODao fifoDao = AppDatabase.getFIFODao(context);
            if(productDao != null && (sessionType == AppCommonMethods.SessionType.OMNICHANNEL || sessionType == AppCommonMethods.SessionType.SEARCH_LIST))
              productDao.updateDecodedEPC(tagInfo.epc, tagInfo.ean, tagInfo.zone);
            else if(fifoDao != null && sessionType == AppCommonMethods.SessionType.SEARCH_FIFO)
              fifoDao.updateDecoded(tagInfo.ean, tagInfo.epc, fifoDate);
          }
          catch(Exception e){ e.printStackTrace(); }
        }
      }*/
        }
    }
  
  /*protected final void updateMultiDecodeCount(final boolean isMultiDecode, final boolean isDecoded){ updateMultiDecodeCount(isMultiDecode, isDecoded, ""); }
  
  protected final void updateMultiDecodeCount(final boolean isMultiDecode, final boolean isDecoded, final String errMsg){
    isMultiWriteDone = true;
    if(sessionAction == AppCommonMethods.SessionAction.DECODE && multiWriteCount == 0){
      isDecodeDone.postValue(isDecoded);
      setProgressMessage(false);
      new Handler().postDelayed(new Runnable(){
        @Override
        public void run(){
          setProgressMessage(false);
          if(isDecoded && sessionType != AppCommonMethods.SessionType.DECODING)
            ((MainActivity) context).showCustomSuccessDialog(context.getString(R.string.success_decoding) + (isMultiDecode ? "\n" + "( " + multiWriteSuccessCount + " / " + multiWriteListSize + " )" : ""));
          else if(!isDecoded)
            ((MainActivity) context).showCustomErrDialog(chkNull(isMultiDecode ? "" : errMsg, context.getString(R.string.err_decoding_fail)));
          isDecodeOn.postValue(false);
        }
      }, isMultiDecode ? 50 : 0);
    }
  }*/

    //For Verifying after Encode + Updating Color Status
    //protected void updateEncVerifyByEpc(final String epcdt){ updateEncVerifyByEpcTid(epcdt,"");};
    protected void updateEncVerifyByEpcTid(final String epcdt, final String tid) {
        final boolean isDecodedEPC = epcdt.trim().startsWith("0");
        final String epc = context.epcEncoderDecoder.getEpcFromDecodedEpc(epcdt.trim());
        showLog("Inv_ENC_epc_tid", epc + "_" + tid);
        if (isNonEmpty(epc) && isNonEmpty(tid)) {
            if (inventoryDao.isVerified(sessionId, epc, tid))
                showLog("Inv_ENC_Verified_already", epc + "_" + tid);
            else {
                int status = (isDecodedEPC ? AppCommonMethods.EncodeVerifyStatus.VERIFIED_DECODED : AppCommonMethods.EncodeVerifyStatus.VERIFIED_SUCCESS).ordinal();
                Integer result = inventoryDao.updateEncVerified(sessionId, epc, tid, status);
                showLog("Inv_ENC_updated", result + "_" + status);
                if (result > 0) {
                    uploadInventoryDao.updateEncVerified(sessionId, epc, tid, status);
                    //update status for Re-Encoded Tags to Orange
                    int tidStatus = AppCommonMethods.EncodeVerifyStatus.RE_ENCODED.ordinal();
                    inventoryDao.updateEncVerifyStatusByTid(sessionId, tid, tidStatus);
                    uploadInventoryDao.updateEncVerifyStatusByTid(sessionId, tid, tidStatus);
                    showLog("Inv_TID_updated", result + "_" + tidStatus);
                } else if (result <= 0) {
                    if (inventoryDao.getTidCount(sessionId, tid) == 1) {
                        int tidErrStatus = AppCommonMethods.EncodeVerifyStatus.EPC_WRONG.ordinal();
                        inventoryDao.updateEncVerifyStatusByTid(sessionId, tid, tidErrStatus);
                        uploadInventoryDao.updateEncVerifyStatusByTid(sessionId, tid, tidErrStatus);
                    }
                }
            }
        }
    /*else if(isNonEmpty(epc)){
      if(inventoryDao.isVerifiedByEpc(sessionId,epc))
        showLog("Inv_ENC_Verified_already",""+epc);
      else{
        int status = (isDecodedEPC?AppCommonMethods.EncodeVerifyStatus.VERIFIED_DECODED:AppCommonMethods.EncodeVerifyStatus.VERIFIED_SUCCESS).ordinal();
        Integer result= inventoryDao.updateEncVerifiedByEpc(sessionId, epc, status);
        showLog("updateEncVerifiedByEpc",""+result+"_"+status);
        if(result>0) {
          uploadInventoryDao.updateEncVerifiedByEpc(sessionId, epc, status);
        }
      }
    }*/
    }
  
    protected boolean isValidItekTag(final String epc, final String tid){
        if(!SharedPrefManager.getBoolean(ParamConstants.IS_IDENTIFY_ITEK_TAG_BY_EPC)) return true;
        if(isNonEmpty(epc) && context.epcEncoderDecoder.isValidItekTag(epc)){
          return true;
        }
        if(SharedPrefManager.getBoolean(ParamConstants.IS_IDENTIFY_ITEK_TAG_BY_TID) && isNonEmpty(tid) && tagIDDao.isTIDPresent(tid)) {
          return true;
        }
        return false;
    }
    
    protected boolean isInvSession(){
      return sessionType != null && (sessionType == AppCommonMethods.SessionType.INVENTORY || sessionType == AppCommonMethods.SessionType.ADD_INVENTORY || sessionType == AppCommonMethods.SessionType.BRAND_INVENTORY || sessionType == AppCommonMethods.SessionType.FILTER_INVENTORY);
    }
    
    protected boolean isEncSession(){
      return sessionType != null && (sessionType == AppCommonMethods.SessionType.ENCODING || sessionType == AppCommonMethods.SessionType.ENCODING_THAN);
    }
    
    protected boolean isDecSession(){
      return sessionType != null && (sessionType == AppCommonMethods.SessionType.DECODING);
    }
  
  protected abstract void saveSerialNo();

    public void setTriggerPressed(){
        if (!restrictTriggerPress) {
            showLog("restrictTriggerPress", "" + restrictTriggerPress);
            isTriggerPressed.postValue(true);
            checkTimer();
        }
    }

    protected void checkTimer(){
        restrictTriggerPress = true;
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            //Do something after 100ms
            restrictTriggerPress = false;
        }, 500);
    }
}
