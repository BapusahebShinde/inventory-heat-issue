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
  public static final String CSV_HEADER = "row_type,session_id,timestamp,elapsed_seconds,battery_temp_c,battery_level,charging_status,total_raw_callbacks,total_duplicate_callbacks,total_unique_epcs,raw_callbacks_per_sec,unique_epcs_per_sec,db_pending_queue_size,last_db_batch_flush_ms,ui_update_count,last_ui_update_timestamp,app_memory_used_mb,app_memory_free_mb,reader_inventory_running,warnings,thermal_status,thermal_throttling_level,thermal_warning,cpu_usage_percent,cpu_frequency_khz,gc_count,gc_time_ms,process_thread_count,callback_avg_ms,callback_max_ms";

  private final Context appContext;
  private final String sessionId;
  private final SnapshotProvider snapshotProvider;
  private final ScheduledExecutorService executor;
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
        final double callbackMaxMs) {
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
    }
  }

  private static final class BatterySnapshot {
    final double tempC;
    final int levelPercent;
    final String chargingStatus;

    BatterySnapshot(final double tempC, final int levelPercent, final String chargingStatus) {
      this.tempC = tempC;
      this.levelPercent = levelPercent;
      this.chargingStatus = chargingStatus;
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
          formatDouble(snapshot.callbackMaxMs)));
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
    return new InventorySnapshot(sessionId, Math.max(0L, (now - startTimeMs) / 1000L), 0L, 0L, 0L, 0, 0L, false, Double.NaN, Double.NaN);
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

  private BatterySnapshot readBatterySnapshot() {
    try {
      final Intent intent = appContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
      if (intent == null) return new BatterySnapshot(Double.NaN, -1, "");
      final int tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Integer.MIN_VALUE);
      final double tempC = tempTenths == Integer.MIN_VALUE ? Double.NaN : tempTenths / 10.0d;
      final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
      final int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
      final int levelPercent = level >= 0 && scale > 0 ? Math.round((level * 100.0f) / scale) : -1;
      final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
      return new BatterySnapshot(tempC, levelPercent, chargingStatus(status));
    }
    catch (Throwable t) {
      return new BatterySnapshot(Double.NaN, -1, "");
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
