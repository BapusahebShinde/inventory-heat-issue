package com.itek.retail.reader;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Debug;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.itek.retail.BuildConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class RetailDiagnosticLogger {
  private static final boolean ENABLE_DIAGNOSTIC_LOGGER = true;
  private static final String TAG = "RetailDiag";
  private static final long SAMPLE_INTERVAL_SECONDS = 5L;
  private static final double READ_RATE_DROP_FACTOR = 0.50d;
  private static final long FIVE_MINUTES_SECONDS = 300L;
  private static final long DB_FLUSH_SLOW_MS = 500L;
  private static final long MEMORY_LOW_MB = 64L;
  private static final AtomicLong UI_UPDATE_COUNT = new AtomicLong(0L);
  private static final AtomicLong LAST_UI_UPDATE_MS = new AtomicLong(0L);
  public static final String CSV_HEADER = "row_type,session_id,timestamp,elapsed_seconds,battery_temp_c,battery_level,charging_status,total_raw_callbacks,total_duplicate_callbacks,total_unique_epcs,raw_callbacks_per_sec,unique_epcs_per_sec,duplicate_percent,db_pending_queue_size,last_db_batch_flush_ms,ui_update_count,last_ui_update_timestamp,app_memory_used_mb,app_memory_free_mb,reader_inventory_running,warnings,thermal_status,thermal_throttling_level,thermal_warning,cpu_usage_percent,cpu_frequency_khz,gc_count,gc_time_ms,process_thread_count,callback_avg_ms,callback_max_ms,uhf_module_temp_c,rf_power_dbm,inventory_session,inventory_target,q_value,dynamic_q_enabled,antenna_state,reader_connected,sdk_error_count,sdk_last_error,sdk_warning_count,sdk_last_warning,callback_queue_depth,avg_rssi,min_rssi,max_rssi,battery_voltage_mv,battery_current_ma,screen_brightness,known_unread_expected,known_unread_found,main_thread_block_count,main_thread_max_block_ms,main_thread_last_block_ms,main_thread_last_block_timestamp,main_thread_total_blocked_ms,main_thread_monitor_running,rfid_init_call_count,rfid_release_call_count,inventory_start_call_count,inventory_stop_call_count,rfid_lifecycle_state,last_rfid_lifecycle_event,last_rfid_lifecycle_timestamp,last_anr_warning,main_thread_last_known_operation,main_thread_suspected_blocking_area";

  private final Context appContext;
  private final String sessionId;
  private final SnapshotProvider snapshotProvider;
  private final ScheduledExecutorService executor;
  private final MainThreadBlockMonitor mainThreadBlockMonitor;
  private final long startTimeMs;
  private final long startUiUpdateCount;
  private final String baseFileName;
  private File csvFile;
  private BufferedWriter writer;
  private volatile boolean stopped = false;
  private long lastSampleMs;
  private long lastRawCallbacks = 0L;
  private long lastUniqueEpcs = 0L;
  private long finalRawCallbacks = 0L;
  private long finalDuplicateCallbacks = 0L;
  private long finalUniqueEpcs = 0L;
  private double maxBatteryTempC = Double.NaN;
  private String maxBatteryTempTimestamp = "";
  private String firstTempAbove40Timestamp = "";
  private String firstTempAbove45Timestamp = "";
  private double peakRawReadsPerSecond = 0.0d;
  private double peakUniqueReadsPerSecond = 0.0d;
  private double lowestRawReadsPerSecondAfterFiveMinutes = Double.NaN;
  private double lowestUniqueReadsPerSecondAfterFiveMinutes = Double.NaN;
  private long lastProcessCpuJiffies = -1L;
  private long lastTotalCpuJiffies = -1L;
  private long lastElapsedCpuTimeMs = -1L;
  private long lastElapsedCpuWallMs = -1L;

  public interface SnapshotProvider {
    InventorySnapshot getInventorySnapshot();
  }

  public static final class InventorySnapshot {
    public final String sessionId;
    public final long elapsedSeconds;
    public final long totalRawCallbacks;
    public final long totalDuplicateCallbacks;
    public final long totalUniqueEpcs;
    public final int dbPendingQueueSize;
    public final long lastDbBatchFlushDurationMs;
    public final boolean readerInventoryRunning;
    public final double callbackAverageMs;
    public final double callbackMaxMs;
    public final double uhfModuleTempC;
    public final int rfPowerDbm;
    public final String inventorySession;
    public final String inventoryTarget;
    public final String qValue;
    public final String dynamicQEnabled;
    public final String antennaState;
    public final boolean readerConnected;
    public final long sdkErrorCount;
    public final String sdkLastError;
    public final long sdkWarningCount;
    public final String sdkLastWarning;
    public final int callbackQueueDepth;
    public final double avgRssi;
    public final double minRssi;
    public final double maxRssi;
    public final int knownUnreadExpected;
    public final int knownUnreadFound;
    public final long rfidInitCallCount;
    public final long rfidReleaseCallCount;
    public final long inventoryStartCallCount;
    public final long inventoryStopCallCount;
    public final String rfidLifecycleState;
    public final String lastRfidLifecycleEvent;
    public final long lastRfidLifecycleTimestampMs;
    public final String mainThreadLastKnownOperation;
    public final String mainThreadSuspectedBlockingArea;

    public InventorySnapshot(
        final String sessionId,
        final long elapsedSeconds,
        final long totalRawCallbacks,
        final long totalDuplicateCallbacks,
        final long totalUniqueEpcs,
        final int dbPendingQueueSize,
        final long lastDbBatchFlushDurationMs,
        final boolean readerInventoryRunning,
        final double callbackAverageMs,
        final double callbackMaxMs,
        final double uhfModuleTempC,
        final int rfPowerDbm,
        final String inventorySession,
        final String inventoryTarget,
        final String qValue,
        final String dynamicQEnabled,
        final String antennaState,
        final boolean readerConnected,
        final long sdkErrorCount,
        final String sdkLastError,
        final long sdkWarningCount,
        final String sdkLastWarning,
        final int callbackQueueDepth,
        final double avgRssi,
        final double minRssi,
        final double maxRssi,
        final int knownUnreadExpected,
        final int knownUnreadFound,
        final long rfidInitCallCount,
        final long rfidReleaseCallCount,
        final long inventoryStartCallCount,
        final long inventoryStopCallCount,
        final String rfidLifecycleState,
        final String lastRfidLifecycleEvent,
        final long lastRfidLifecycleTimestampMs,
        final String mainThreadLastKnownOperation,
        final String mainThreadSuspectedBlockingArea) {
      this.sessionId = sessionId;
      this.elapsedSeconds = elapsedSeconds;
      this.totalRawCallbacks = totalRawCallbacks;
      this.totalDuplicateCallbacks = totalDuplicateCallbacks;
      this.totalUniqueEpcs = totalUniqueEpcs;
      this.dbPendingQueueSize = dbPendingQueueSize;
      this.lastDbBatchFlushDurationMs = lastDbBatchFlushDurationMs;
      this.readerInventoryRunning = readerInventoryRunning;
      this.callbackAverageMs = callbackAverageMs;
      this.callbackMaxMs = callbackMaxMs;
      this.uhfModuleTempC = uhfModuleTempC;
      this.rfPowerDbm = rfPowerDbm;
      this.inventorySession = inventorySession;
      this.inventoryTarget = inventoryTarget;
      this.qValue = qValue;
      this.dynamicQEnabled = dynamicQEnabled;
      this.antennaState = antennaState;
      this.readerConnected = readerConnected;
      this.sdkErrorCount = sdkErrorCount;
      this.sdkLastError = sdkLastError;
      this.sdkWarningCount = sdkWarningCount;
      this.sdkLastWarning = sdkLastWarning;
      this.callbackQueueDepth = callbackQueueDepth;
      this.avgRssi = avgRssi;
      this.minRssi = minRssi;
      this.maxRssi = maxRssi;
      this.knownUnreadExpected = knownUnreadExpected;
      this.knownUnreadFound = knownUnreadFound;
      this.rfidInitCallCount = rfidInitCallCount;
      this.rfidReleaseCallCount = rfidReleaseCallCount;
      this.inventoryStartCallCount = inventoryStartCallCount;
      this.inventoryStopCallCount = inventoryStopCallCount;
      this.rfidLifecycleState = rfidLifecycleState;
      this.lastRfidLifecycleEvent = lastRfidLifecycleEvent;
      this.lastRfidLifecycleTimestampMs = lastRfidLifecycleTimestampMs;
      this.mainThreadLastKnownOperation = mainThreadLastKnownOperation;
      this.mainThreadSuspectedBlockingArea = mainThreadSuspectedBlockingArea;
    }
  }

  private static final class BatterySnapshot {
    final double tempC;
    final int levelPercent;
    final String chargingStatus;
    final int voltageMv;
    final double currentMa;

    BatterySnapshot(final double tempC, final int levelPercent, final String chargingStatus, final int voltageMv, final double currentMa) {
      this.tempC = tempC;
      this.levelPercent = levelPercent;
      this.chargingStatus = chargingStatus;
      this.voltageMv = voltageMv;
      this.currentMa = currentMa;
    }
  }

  private static final class ThermalSnapshot {
    final String status;
    final int throttlingLevel;
    final String warning;

    ThermalSnapshot(final String status, final int throttlingLevel, final String warning) {
      this.status = status;
      this.throttlingLevel = throttlingLevel;
      this.warning = warning;
    }
  }

  private static final class CpuSnapshot {
    final double usagePercent;
    final String frequencyKhz;

    CpuSnapshot(final double usagePercent, final String frequencyKhz) {
      this.usagePercent = usagePercent;
      this.frequencyKhz = frequencyKhz;
    }
  }

  private static final class GcSnapshot {
    final long count;
    final long timeMs;

    GcSnapshot(final long count, final long timeMs) {
      this.count = count;
      this.timeMs = timeMs;
    }
  }

  public static RetailDiagnosticLogger start(final Context context, final String sessionId, final SnapshotProvider snapshotProvider) {
    if (!ENABLE_DIAGNOSTIC_LOGGER || context == null || snapshotProvider == null) return null;
    try {
      final RetailDiagnosticLogger logger = new RetailDiagnosticLogger(context, sessionId, snapshotProvider);
      logger.start();
      return logger;
    }
    catch (Throwable t) {
      return null;
    }
  }

  public static void recordInventoryUiUpdate() {
    if (!ENABLE_DIAGNOSTIC_LOGGER) return;
    UI_UPDATE_COUNT.incrementAndGet();
    LAST_UI_UPDATE_MS.set(System.currentTimeMillis());
  }

  private RetailDiagnosticLogger(final Context context, final String sessionId, final SnapshotProvider snapshotProvider) {
    this.appContext = context.getApplicationContext();
    this.sessionId = safe(sessionId);
    this.snapshotProvider = snapshotProvider;
    this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
      final Thread thread = new Thread(runnable, "retail_diag_logger");
      thread.setDaemon(true);
      return thread;
    });
    this.mainThreadBlockMonitor = new MainThreadBlockMonitor(() -> buildAnrWarningContext());
    this.startTimeMs = System.currentTimeMillis();
    this.lastSampleMs = startTimeMs;
    this.startUiUpdateCount = UI_UPDATE_COUNT.get();
    this.baseFileName = "RetailDiag_" + sanitize(BuildConfig.VERSION_NAME) + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date(startTimeMs));
    initializeCpuSample();
  }

  private void start() throws IOException {
    final File dir = getDiagnosticsDir();
    if (!dir.exists() && !dir.mkdirs()) throw new IOException("Cannot create diagnostics directory");
    csvFile = new File(dir, baseFileName + ".csv");
    writer = new BufferedWriter(new FileWriter(csvFile, true));
    writer.write(CSV_HEADER);
    writer.newLine();
    writer.flush();
    mainThreadBlockMonitor.start();
    executor.scheduleAtFixedRate(() -> {
      if (!stopped) writeSampleSafe("SAMPLE");
    }, SAMPLE_INTERVAL_SECONDS, SAMPLE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    Log.i(TAG, "Started diagnostic logger: " + csvFile.getAbsolutePath());
  }

  public void stop() {
    synchronized (this) {
      if (stopped) return;
      stopped = true;
      writeSampleLocked("STOP");
      writeSummaryLocked();
      mainThreadBlockMonitor.stop();
      closeWriterLocked();
    }
    executor.shutdownNow();
    Log.i(TAG, "Stopped diagnostic logger");
  }

  private void writeSampleSafe(final String rowType) {
    synchronized (this) {
      if (stopped && !"STOP".equals(rowType)) return;
      writeSampleLocked(rowType);
    }
  }

  private void writeSampleLocked(final String rowType) {
    try {
      if (writer == null) return;
      final long now = System.currentTimeMillis();
      final InventorySnapshot snapshot = getSnapshot(now);
      final BatterySnapshot battery = readBatterySnapshot();
      final ThermalSnapshot thermal = readThermalSnapshot();
      final MemorySnapshot memory = readMemorySnapshot();
      final CpuSnapshot cpu = readCpuSnapshot();
      final GcSnapshot gc = readGcSnapshot();
      final int threadCount = readProcessThreadCount();
      final MainThreadBlockMonitor.MainThreadBlockStats mainThreadStats = mainThreadBlockMonitor.snapshot();
      final double deltaSeconds = Math.max(1.0d, (now - lastSampleMs) / 1000.0d);
      final long rawDelta = Math.max(0L, snapshot.totalRawCallbacks - lastRawCallbacks);
      final long uniqueDelta = Math.max(0L, snapshot.totalUniqueEpcs - lastUniqueEpcs);
      final double rawRate = rawDelta / deltaSeconds;
      final double uniqueRate = uniqueDelta / deltaSeconds;
      final String timestamp = formatTimestamp(now);
      updateSummaryStats(snapshot, battery, rawRate, uniqueRate, timestamp);
      writer.write(joinCsv(
          rowType,
          snapshot.sessionId,
          timestamp,
          String.valueOf(snapshot.elapsedSeconds),
          formatDouble(battery.tempC),
          String.valueOf(battery.levelPercent),
          battery.chargingStatus,
          String.valueOf(snapshot.totalRawCallbacks),
          String.valueOf(snapshot.totalDuplicateCallbacks),
          String.valueOf(snapshot.totalUniqueEpcs),
          formatDouble(rawRate),
          formatDouble(uniqueRate),
          formatDouble(snapshot.totalRawCallbacks > 0L ? (snapshot.totalDuplicateCallbacks * 100.0d) / snapshot.totalRawCallbacks : Double.NaN),
          String.valueOf(snapshot.dbPendingQueueSize),
          String.valueOf(snapshot.lastDbBatchFlushDurationMs),
          String.valueOf(Math.max(0L, UI_UPDATE_COUNT.get() - startUiUpdateCount)),
          formatTimestampOrBlank(LAST_UI_UPDATE_MS.get()),
          String.valueOf(memory.usedMb),
          String.valueOf(memory.freeMb),
          String.valueOf(snapshot.readerInventoryRunning),
          buildWarnings(battery, snapshot, memory, rawRate),
          thermal.status,
          thermal.throttlingLevel >= 0 ? String.valueOf(thermal.throttlingLevel) : "",
          thermal.warning,
          formatDouble(cpu.usagePercent),
          cpu.frequencyKhz,
          gc.count >= 0L ? String.valueOf(gc.count) : "",
          gc.timeMs >= 0L ? String.valueOf(gc.timeMs) : "",
          threadCount >= 0 ? String.valueOf(threadCount) : "",
          formatDouble(snapshot.callbackAverageMs),
          formatDouble(snapshot.callbackMaxMs),
          formatDouble(snapshot.uhfModuleTempC),
          snapshot.rfPowerDbm >= 0 ? String.valueOf(snapshot.rfPowerDbm) : "",
          snapshot.inventorySession,
          snapshot.inventoryTarget,
          snapshot.qValue,
          snapshot.dynamicQEnabled,
          snapshot.antennaState,
          String.valueOf(snapshot.readerConnected),
          String.valueOf(snapshot.sdkErrorCount),
          snapshot.sdkLastError,
          String.valueOf(snapshot.sdkWarningCount),
          snapshot.sdkLastWarning,
          snapshot.callbackQueueDepth >= 0 ? String.valueOf(snapshot.callbackQueueDepth) : "",
          formatDouble(snapshot.avgRssi),
          formatDouble(snapshot.minRssi),
          formatDouble(snapshot.maxRssi),
          battery.voltageMv >= 0 ? String.valueOf(battery.voltageMv) : "",
          formatDouble(battery.currentMa),
          readScreenBrightness(),
          snapshot.knownUnreadExpected >= 0 ? String.valueOf(snapshot.knownUnreadExpected) : "",
          snapshot.knownUnreadFound >= 0 ? String.valueOf(snapshot.knownUnreadFound) : "",
          String.valueOf(mainThreadStats.blockCount),
          String.valueOf(mainThreadStats.maxBlockMs),
          String.valueOf(mainThreadStats.lastBlockMs),
          MainThreadBlockMonitor.formatTimestamp(mainThreadStats.lastBlockTimestampMs),
          String.valueOf(mainThreadStats.totalBlockedMs),
          String.valueOf(mainThreadStats.running),
          String.valueOf(snapshot.rfidInitCallCount),
          String.valueOf(snapshot.rfidReleaseCallCount),
          String.valueOf(snapshot.inventoryStartCallCount),
          String.valueOf(snapshot.inventoryStopCallCount),
          snapshot.rfidLifecycleState,
          snapshot.lastRfidLifecycleEvent,
          formatTimestampOrBlank(snapshot.lastRfidLifecycleTimestampMs),
          mainThreadStats.lastAnrWarning,
          snapshot.mainThreadLastKnownOperation,
          snapshot.mainThreadSuspectedBlockingArea));
      writer.newLine();
      writer.flush();
      lastSampleMs = now;
      lastRawCallbacks = snapshot.totalRawCallbacks;
      lastUniqueEpcs = snapshot.totalUniqueEpcs;
      finalRawCallbacks = snapshot.totalRawCallbacks;
      finalDuplicateCallbacks = snapshot.totalDuplicateCallbacks;
      finalUniqueEpcs = snapshot.totalUniqueEpcs;
    }
    catch (Throwable t) {
      // Diagnostics must never interrupt scanning.
    }
  }

  private InventorySnapshot getSnapshot(final long now) {
    try {
      final InventorySnapshot snapshot = snapshotProvider.getInventorySnapshot();
      if (snapshot != null) return snapshot;
    }
    catch (Throwable ignored) {
      // Keep diagnostics side-band; never interrupt inventory.
    }
    return new InventorySnapshot(sessionId, Math.max(0L, (now - startTimeMs) / 1000L), 0L, 0L, 0L, 0, 0L, false, Double.NaN, Double.NaN,
        Double.NaN, -1, "", "", "", "", "", false, 0L, "", 0L, "", -1, Double.NaN, Double.NaN, Double.NaN, -1, -1,
        0L, 0L, 0L, 0L, "", "", 0L, "", "");
  }

  private String buildAnrWarningContext() {
    try {
      final InventorySnapshot snapshot = getSnapshot(System.currentTimeMillis());
      final double tagsPerSecond = snapshot.elapsedSeconds > 0L ? snapshot.totalUniqueEpcs / (double) snapshot.elapsedSeconds : Double.NaN;
      return "state=" + safe(snapshot.rfidLifecycleState)
          + " | inventoryRunning=" + snapshot.readerInventoryRunning
          + " | rawCallbacks=" + snapshot.totalRawCallbacks
          + " | uniqueTags=" + snapshot.totalUniqueEpcs
          + " | duplicateTags=" + snapshot.totalDuplicateCallbacks
          + " | durationSec=" + snapshot.elapsedSeconds
          + " | tagsPerSec=" + formatDouble(tagsPerSecond)
          + " | initCalls=" + snapshot.rfidInitCallCount
          + " | operation=" + safe(snapshot.mainThreadSuspectedBlockingArea);
    }
    catch (Throwable ignored) {
      return "";
    }
  }

  private void updateSummaryStats(final InventorySnapshot snapshot, final BatterySnapshot battery, final double rawRate, final double uniqueRate, final String timestamp) {
    if (!Double.isNaN(battery.tempC)) {
      if (Double.isNaN(maxBatteryTempC) || battery.tempC > maxBatteryTempC) {
        maxBatteryTempC = battery.tempC;
        maxBatteryTempTimestamp = timestamp;
      }
      if (battery.tempC >= 40.0d && firstTempAbove40Timestamp.length() <= 0) firstTempAbove40Timestamp = timestamp;
      if (battery.tempC >= 45.0d && firstTempAbove45Timestamp.length() <= 0) firstTempAbove45Timestamp = timestamp;
    }
    if (rawRate > peakRawReadsPerSecond) peakRawReadsPerSecond = rawRate;
    if (uniqueRate > peakUniqueReadsPerSecond) peakUniqueReadsPerSecond = uniqueRate;
    if (snapshot.elapsedSeconds >= FIVE_MINUTES_SECONDS) {
      if (Double.isNaN(lowestRawReadsPerSecondAfterFiveMinutes) || rawRate < lowestRawReadsPerSecondAfterFiveMinutes)
        lowestRawReadsPerSecondAfterFiveMinutes = rawRate;
      if (Double.isNaN(lowestUniqueReadsPerSecondAfterFiveMinutes) || uniqueRate < lowestUniqueReadsPerSecondAfterFiveMinutes)
        lowestUniqueReadsPerSecondAfterFiveMinutes = uniqueRate;
    }
  }

  private String buildWarnings(final BatterySnapshot battery, final InventorySnapshot snapshot, final MemorySnapshot memory, final double rawRate) {
    final StringBuilder warnings = new StringBuilder();
    if (!Double.isNaN(battery.tempC) && battery.tempC >= 40.0d) appendWarning(warnings, "TEMP_ABOVE_40");
    if (!Double.isNaN(battery.tempC) && battery.tempC >= 45.0d) appendWarning(warnings, "TEMP_ABOVE_45");
    if (snapshot.elapsedSeconds >= FIVE_MINUTES_SECONDS && peakRawReadsPerSecond > 0.0d && rawRate < peakRawReadsPerSecond * READ_RATE_DROP_FACTOR)
      appendWarning(warnings, "READ_RATE_DROP");
    if (snapshot.lastDbBatchFlushDurationMs >= DB_FLUSH_SLOW_MS) appendWarning(warnings, "DB_FLUSH_SLOW");
    if (memory.freeMb >= 0L && memory.freeMb < MEMORY_LOW_MB) appendWarning(warnings, "MEMORY_LOW");
    return warnings.toString();
  }

  private void writeSummaryLocked() {
    BufferedWriter summaryWriter = null;
    try {
      if (csvFile == null) return;
      final long durationSeconds = Math.max(0L, (System.currentTimeMillis() - startTimeMs) / 1000L);
      final File summaryFile = new File(csvFile.getParentFile(), baseFileName + "_summary.txt");
      summaryWriter = new BufferedWriter(new FileWriter(summaryFile, false));
      summaryWriter.write("diagnostic_build=" + BuildConfig.VERSION_NAME);
      summaryWriter.newLine();
      summaryWriter.write("session_id=" + sessionId);
      summaryWriter.newLine();
      summaryWriter.write("session_duration_seconds=" + durationSeconds);
      summaryWriter.newLine();
      summaryWriter.write("max_battery_temp_c=" + formatDouble(maxBatteryTempC));
      summaryWriter.newLine();
      summaryWriter.write("max_battery_temp_timestamp=" + safe(maxBatteryTempTimestamp));
      summaryWriter.newLine();
      summaryWriter.write("first_temp_above_40_timestamp=" + safe(firstTempAbove40Timestamp));
      summaryWriter.newLine();
      summaryWriter.write("first_temp_above_45_timestamp=" + safe(firstTempAbove45Timestamp));
      summaryWriter.newLine();
      summaryWriter.write("peak_raw_reads_per_sec=" + formatDouble(peakRawReadsPerSecond));
      summaryWriter.newLine();
      summaryWriter.write("lowest_raw_reads_per_sec_after_5_min=" + formatDouble(lowestRawReadsPerSecondAfterFiveMinutes));
      summaryWriter.newLine();
      summaryWriter.write("peak_unique_reads_per_sec=" + formatDouble(peakUniqueReadsPerSecond));
      summaryWriter.newLine();
      summaryWriter.write("lowest_unique_reads_per_sec_after_5_min=" + formatDouble(lowestUniqueReadsPerSecondAfterFiveMinutes));
      summaryWriter.newLine();
      summaryWriter.write("total_unique_epcs=" + finalUniqueEpcs);
      summaryWriter.newLine();
      summaryWriter.write("total_duplicates=" + finalDuplicateCallbacks);
      summaryWriter.newLine();
      summaryWriter.write("total_raw_callbacks=" + finalRawCallbacks);
      summaryWriter.newLine();
      summaryWriter.write(generateAnrSummaryReport());
      summaryWriter.newLine();
      summaryWriter.flush();
    }
    catch (Throwable t) {
      // Diagnostics must never interrupt scanning.
    }
    finally {
      if (summaryWriter != null) {
        try {
          summaryWriter.close();
        }
        catch (IOException ignored) {
          // Ignore close errors.
        }
      }
    }
  }

  public String generateAnrSummaryReport() {
    try {
      final InventorySnapshot snapshot = getSnapshot(System.currentTimeMillis());
      final MainThreadBlockMonitor.MainThreadBlockStats mainThreadStats = mainThreadBlockMonitor.snapshot();
      final double tagsPerSecond = snapshot.elapsedSeconds > 0L ? snapshot.totalUniqueEpcs / (double) snapshot.elapsedSeconds : Double.NaN;
      final String risk = anrRiskAssessment(mainThreadStats);
      final String recommendation = anrRecommendation(snapshot, mainThreadStats, risk);
      return "\nANR / Main Thread Diagnostics\n"
          + "-----------------------------\n"
          + "scan_duration_seconds=" + snapshot.elapsedSeconds + "\n"
          + "total_raw_callbacks=" + snapshot.totalRawCallbacks + "\n"
          + "total_unique_tags=" + snapshot.totalUniqueEpcs + "\n"
          + "total_duplicate_tags=" + snapshot.totalDuplicateCallbacks + "\n"
          + "tags_per_second=" + formatDouble(tagsPerSecond) + "\n"
          + "main_thread_block_count=" + mainThreadStats.blockCount + "\n"
          + "main_thread_max_block_ms=" + mainThreadStats.maxBlockMs + "\n"
          + "main_thread_last_block_ms=" + mainThreadStats.lastBlockMs + "\n"
          + "main_thread_last_block_timestamp=" + MainThreadBlockMonitor.formatTimestamp(mainThreadStats.lastBlockTimestampMs) + "\n"
          + "main_thread_total_blocked_ms=" + mainThreadStats.totalBlockedMs + "\n"
          + "main_thread_monitor_running=" + mainThreadStats.running + "\n"
          + "rfid_init_call_count=" + snapshot.rfidInitCallCount + "\n"
          + "rfid_release_call_count=" + snapshot.rfidReleaseCallCount + "\n"
          + "inventory_start_call_count=" + snapshot.inventoryStartCallCount + "\n"
          + "inventory_stop_call_count=" + snapshot.inventoryStopCallCount + "\n"
          + "rfid_lifecycle_state=" + safe(snapshot.rfidLifecycleState) + "\n"
          + "last_rfid_lifecycle_event=" + safe(snapshot.lastRfidLifecycleEvent) + "\n"
          + "last_rfid_lifecycle_timestamp=" + formatTimestampOrBlank(snapshot.lastRfidLifecycleTimestampMs) + "\n"
          + "last_anr_warning=" + safe(mainThreadStats.lastAnrWarning) + "\n"
          + "main_thread_last_known_operation=" + safe(snapshot.mainThreadLastKnownOperation) + "\n"
          + "main_thread_suspected_blocking_area=" + safe(snapshot.mainThreadSuspectedBlockingArea) + "\n"
          + "ANR Risk Assessment=" + risk + "\n"
          + "recommendation=" + recommendation;
    }
    catch (Throwable ignored) {
      return "\nANR / Main Thread Diagnostics\n-----------------------------\nANR Risk Assessment=UNKNOWN\nrecommendation=ANR diagnostics unavailable";
    }
  }

  private static String anrRiskAssessment(final MainThreadBlockMonitor.MainThreadBlockStats stats) {
    if (stats == null || stats.blockCount <= 0L) return "LOW";
    if (stats.maxBlockMs > 5000L || stats.blockCount > 2L) return "HIGH";
    return "MEDIUM";
  }

  private static String anrRecommendation(final InventorySnapshot snapshot, final MainThreadBlockMonitor.MainThreadBlockStats stats, final String risk) {
    if ("LOW".equals(risk)) return "No ANR risk observed.";
    final String suspectedArea = snapshot == null ? "" : safe(snapshot.mainThreadSuspectedBlockingArea);
    if (suspectedArea.length() > 0) return "Main-thread block detected; review suspected blocking area: " + suspectedArea + ".";
    if (snapshot != null && snapshot.rfidInitCallCount > 1L && snapshot.rfidReleaseCallCount > 0L)
      return "Repeated RFID init/release detected; review hardware lifecycle.";
    return stats != null && stats.blockCount > 2L ? "Repeated main-thread blocks detected; review UI-thread hardware and database operations." : "Main-thread block detected; review suspected blocking area.";
  }

  private BatterySnapshot readBatterySnapshot() {
    try {
      final Intent intent = appContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
      if (intent == null) return new BatterySnapshot(Double.NaN, -1, "", -1, Double.NaN);
      final int tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Integer.MIN_VALUE);
      final double tempC = tempTenths == Integer.MIN_VALUE ? Double.NaN : tempTenths / 10.0d;
      final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
      final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
      final int levelPercent = level >= 0 && scale > 0 ? Math.round((level * 100.0f) / scale) : -1;
      final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
      final int voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
      return new BatterySnapshot(tempC, levelPercent, chargingStatus(status), voltageMv, readBatteryCurrentMa());
    }
    catch (Throwable t) {
      return new BatterySnapshot(Double.NaN, -1, "", -1, Double.NaN);
    }
  }

  private double readBatteryCurrentMa() {
    try {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return Double.NaN;
      final BatteryManager batteryManager = (BatteryManager) appContext.getSystemService(Context.BATTERY_SERVICE);
      if (batteryManager == null) return Double.NaN;
      final int currentMicroAmps = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
      if (currentMicroAmps == Integer.MIN_VALUE) return Double.NaN;
      return currentMicroAmps / 1000.0d;
    }
    catch (Throwable ignored) {
      return Double.NaN;
    }
  }

  private MemorySnapshot readMemorySnapshot() {
    try {
      final Runtime runtime = Runtime.getRuntime();
      final long usedBytes = runtime.totalMemory() - runtime.freeMemory();
      final long freeBytes = runtime.maxMemory() - usedBytes;
      return new MemorySnapshot(bytesToMb(usedBytes), bytesToMb(freeBytes));
    }
    catch (Throwable t) {
      return new MemorySnapshot(-1L, -1L);
    }
  }

  private ThermalSnapshot readThermalSnapshot() {
    try {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return new ThermalSnapshot("", -1, "");
      final PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
      if (powerManager == null) return new ThermalSnapshot("", -1, "");
      final int status = powerManager.getCurrentThermalStatus();
      return new ThermalSnapshot(thermalStatusName(status), status, thermalWarning(status));
    }
    catch (Throwable t) {
      return new ThermalSnapshot("", -1, "");
    }
  }

  private void initializeCpuSample() {
    try {
      final long process = readProcessCpuJiffies();
      final long total = readTotalCpuJiffies();
      if (process >= 0L && total > 0L) {
        lastProcessCpuJiffies = process;
        lastTotalCpuJiffies = total;
      }
      lastElapsedCpuTimeMs = Process.getElapsedCpuTime();
      lastElapsedCpuWallMs = SystemClock.elapsedRealtime();
    }
    catch (Throwable ignored) {
      // CPU diagnostics are optional.
    }
  }

  private CpuSnapshot readCpuSnapshot() {
    double usagePercent = Double.NaN;
    try {
      final long process = readProcessCpuJiffies();
      final long total = readTotalCpuJiffies();
      if (process >= 0L && total > 0L && lastProcessCpuJiffies >= 0L && lastTotalCpuJiffies > 0L) {
        final long processDelta = Math.max(0L, process - lastProcessCpuJiffies);
        final long totalDelta = Math.max(0L, total - lastTotalCpuJiffies);
        if (totalDelta > 0L) usagePercent = (processDelta * Runtime.getRuntime().availableProcessors() * 100.0d) / totalDelta;
      }
      if (process >= 0L) lastProcessCpuJiffies = process;
      if (total > 0L) lastTotalCpuJiffies = total;
    }
    catch (Throwable ignored) {
      // CPU diagnostics are optional.
    }
    if (Double.isNaN(usagePercent)) usagePercent = readElapsedProcessCpuPercent();
    return new CpuSnapshot(usagePercent, readCpuFrequencyKhz());
  }

  private double readElapsedProcessCpuPercent() {
    try {
      final long processCpuMs = Process.getElapsedCpuTime();
      final long wallMs = SystemClock.elapsedRealtime();
      double usagePercent = Double.NaN;
      if (lastElapsedCpuTimeMs >= 0L && lastElapsedCpuWallMs > 0L) {
        final long processDeltaMs = Math.max(0L, processCpuMs - lastElapsedCpuTimeMs);
        final long wallDeltaMs = Math.max(0L, wallMs - lastElapsedCpuWallMs);
        if (wallDeltaMs > 0L) usagePercent = (processDeltaMs * 100.0d) / wallDeltaMs;
      }
      lastElapsedCpuTimeMs = processCpuMs;
      lastElapsedCpuWallMs = wallMs;
      return usagePercent;
    }
    catch (Throwable ignored) {
      return Double.NaN;
    }
  }

  private long readProcessCpuJiffies() {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader("/proc/self/stat"));
      final String line = reader.readLine();
      if (line == null) return -1L;
      final int end = line.lastIndexOf(')');
      if (end < 0 || end + 2 >= line.length()) return -1L;
      final String[] parts = line.substring(end + 2).trim().split("\\s+");
      if (parts.length <= 12) return -1L;
      return parseLong(parts[11], 0L) + parseLong(parts[12], 0L);
    }
    catch (Throwable t) {
      return -1L;
    }
    finally {
      closeQuietly(reader);
    }
  }

  private long readTotalCpuJiffies() {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader("/proc/stat"));
      final String line = reader.readLine();
      if (line == null || !line.startsWith("cpu ")) return -1L;
      long total = 0L;
      final String[] parts = line.trim().split("\\s+");
      for (int i = 1; i < parts.length; i++) total += parseLong(parts[i], 0L);
      return total;
    }
    catch (Throwable t) {
      return -1L;
    }
    finally {
      closeQuietly(reader);
    }
  }

  private String readCpuFrequencyKhz() {
    try {
      final int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
      final StringBuilder builder = new StringBuilder();
      for (int i = 0; i < cores; i++) {
        final String freq = readFirstNonEmpty(
            "/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq",
            "/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_cur_freq");
        if (freq.length() > 0) {
          if (builder.length() > 0) builder.append('|');
          builder.append("cpu").append(i).append(':').append(freq);
        }
      }
      return builder.toString();
    }
    catch (Throwable t) {
      return "";
    }
  }

  private GcSnapshot readGcSnapshot() {
    try {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return new GcSnapshot(-1L, -1L);
      final Map<String, String> stats = Debug.getRuntimeStats();
      final long count = parseLong(stats.get("art.gc.gc-count"), -1L);
      final long timeMs = parseLong(stats.get("art.gc.gc-time"), -1L);
      return new GcSnapshot(count, timeMs);
    }
    catch (Throwable t) {
      return new GcSnapshot(-1L, -1L);
    }
  }

  private int readProcessThreadCount() {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader("/proc/self/status"));
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("Threads:")) return (int) parseLong(line.substring("Threads:".length()).trim(), -1L);
      }
    }
    catch (Throwable ignored) {
      // Thread count is optional.
    }
    finally {
      closeQuietly(reader);
    }
    return -1;
  }

  private String readScreenBrightness() {
    try {
      return String.valueOf(Settings.System.getInt(appContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
    }
    catch (Throwable ignored) {
      return "";
    }
  }

  private File getDiagnosticsDir() {
    File dir = appContext.getExternalFilesDir("diagnostics");
    if (dir == null) dir = new File(appContext.getFilesDir(), "diagnostics");
    return dir;
  }

  private void closeWriterLocked() {
    if (writer == null) return;
    try {
      writer.flush();
      writer.close();
    }
    catch (IOException ignored) {
      // Ignore close errors.
    }
    writer = null;
  }

  private static void appendWarning(final StringBuilder warnings, final String warning) {
    if (warnings.length() > 0) warnings.append('|');
    warnings.append(warning);
  }

  private static String chargingStatus(final int status) {
    if (status == BatteryManager.BATTERY_STATUS_CHARGING) return "CHARGING";
    if (status == BatteryManager.BATTERY_STATUS_FULL) return "FULL";
    if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) return "DISCHARGING";
    if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) return "NOT_CHARGING";
    return "";
  }

  private static String thermalStatusName(final int status) {
    if (status == PowerManager.THERMAL_STATUS_NONE) return "NONE";
    if (status == PowerManager.THERMAL_STATUS_LIGHT) return "LIGHT";
    if (status == PowerManager.THERMAL_STATUS_MODERATE) return "MODERATE";
    if (status == PowerManager.THERMAL_STATUS_SEVERE) return "SEVERE";
    if (status == PowerManager.THERMAL_STATUS_CRITICAL) return "CRITICAL";
    if (status == PowerManager.THERMAL_STATUS_EMERGENCY) return "EMERGENCY";
    if (status == PowerManager.THERMAL_STATUS_SHUTDOWN) return "SHUTDOWN";
    return "UNKNOWN_" + status;
  }

  private static String thermalWarning(final int status) {
    if (status == PowerManager.THERMAL_STATUS_NONE) return "";
    if (status == PowerManager.THERMAL_STATUS_LIGHT) return "THERMAL_WARNING_LIGHT";
    if (status == PowerManager.THERMAL_STATUS_MODERATE) return "THERMAL_WARNING_MODERATE";
    if (status == PowerManager.THERMAL_STATUS_SEVERE) return "THERMAL_WARNING_SEVERE";
    if (status == PowerManager.THERMAL_STATUS_CRITICAL) return "THERMAL_WARNING_CRITICAL";
    if (status == PowerManager.THERMAL_STATUS_EMERGENCY) return "THERMAL_WARNING_EMERGENCY";
    if (status == PowerManager.THERMAL_STATUS_SHUTDOWN) return "THERMAL_WARNING_SHUTDOWN";
    return "THERMAL_WARNING_UNKNOWN";
  }

  private static String readFirstNonEmpty(final String... paths) {
    if (paths == null) return "";
    for (String path : paths) {
      final String value = readFirstLine(path);
      if (value.length() > 0) return value;
    }
    return "";
  }

  private static String readFirstLine(final String path) {
    BufferedReader reader = null;
    try {
      if (path == null || path.length() <= 0) return "";
      reader = new BufferedReader(new FileReader(path));
      final String line = reader.readLine();
      return line == null ? "" : line.trim();
    }
    catch (Throwable ignored) {
      return "";
    }
    finally {
      closeQuietly(reader);
    }
  }

  private static long parseLong(final String value, final long fallback) {
    try {
      if (value == null) return fallback;
      return Long.parseLong(value.trim());
    }
    catch (Throwable ignored) {
      return fallback;
    }
  }

  private static void closeQuietly(final BufferedReader reader) {
    if (reader == null) return;
    try {
      reader.close();
    }
    catch (IOException ignored) {
      // Ignore close errors.
    }
  }

  private static long bytesToMb(final long bytes) {
    return bytes / (1024L * 1024L);
  }

  private static String formatTimestamp(final long timeMs) {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date(timeMs));
  }

  private static String formatTimestampOrBlank(final long timeMs) {
    return timeMs > 0L ? formatTimestamp(timeMs) : "";
  }

  private static String formatDouble(final double value) {
    return Double.isNaN(value) ? "" : String.format(Locale.US, "%.2f", value);
  }

  private static String joinCsv(final String... values) {
    final StringBuilder builder = new StringBuilder();
    for (int i = 0; i < values.length; i++) {
      if (i > 0) builder.append(',');
      builder.append(csv(values[i]));
    }
    return builder.toString();
  }

  private static String csv(final String value) {
    final String safeValue = safe(value);
    if (safeValue.indexOf(',') < 0 && safeValue.indexOf('"') < 0 && safeValue.indexOf('\n') < 0 && safeValue.indexOf('\r') < 0)
      return safeValue;
    return "\"" + safeValue.replace("\"", "\"\"") + "\"";
  }

  private static String sanitize(final String value) {
    return safe(value).replaceAll("[^A-Za-z0-9_.-]", "_");
  }

  private static String safe(final String value) {
    return value == null ? "" : value;
  }

  private static final class MemorySnapshot {
    final long usedMb;
    final long freeMb;

    MemorySnapshot(final long usedMb, final long freeMb) {
      this.usedMb = usedMb;
      this.freeMb = freeMb;
    }
  }
}
