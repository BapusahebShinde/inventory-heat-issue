package com.itek.retail.reader;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class AppAnrDiagnosticLogger {
  private static final boolean ENABLE_ANR_DIAGNOSTICS = BuildConfig.IS_DIAGNOSTIC_BUILD;
  private static final String TAG = "RetailAnrDiag";
  private static final long SAMPLE_INTERVAL_SECONDS = 5L;
  private static final String CSV_HEADER = "row_type,timestamp,elapsed_seconds,current_activity,current_fragment,app_feature,main_thread_block_count,main_thread_max_block_ms,main_thread_last_block_ms,main_thread_last_block_timestamp,main_thread_total_blocked_ms,main_thread_monitor_running,rfid_init_call_count,rfid_release_call_count,inventory_start_call_count,inventory_stop_call_count,rfid_lifecycle_state,last_rfid_lifecycle_event,last_rfid_lifecycle_timestamp,is_inventory_running,reader_connected,process_thread_count,app_memory_used_mb,app_memory_free_mb,gc_count,gc_time_ms,last_known_operation,suspected_blocking_area,last_anr_warning,anr_risk_assessment,recommendation";

  private static final Object LOCK = new Object();
  private static final AtomicInteger START_COUNT = new AtomicInteger(0);
  private static final AtomicLong RFID_INIT_CALL_COUNT = new AtomicLong(0L);
  private static final AtomicLong RFID_RELEASE_CALL_COUNT = new AtomicLong(0L);
  private static final AtomicLong INVENTORY_START_CALL_COUNT = new AtomicLong(0L);
  private static final AtomicLong INVENTORY_STOP_CALL_COUNT = new AtomicLong(0L);
  private static volatile AppAnrDiagnosticLogger instance = null;
  private static volatile String currentActivity = "";
  private static volatile String currentFragment = "";
  private static volatile String appFeature = "";
  private static volatile String rfidLifecycleState = "IDLE";
  private static volatile String lastRfidLifecycleEvent = "";
  private static volatile long lastRfidLifecycleTimestampMs = 0L;
  private static volatile boolean inventoryRunning = false;
  private static volatile boolean readerConnected = false;
  private static volatile String currentOperation = "";
  private static volatile String lastKnownOperation = "";
  private static volatile long lastKnownOperationTimestampMs = 0L;

  private final Context appContext;
  private final MainThreadBlockMonitor mainThreadBlockMonitor;
  private final ScheduledExecutorService executor;
  private final long startTimeMs;
  private final String baseFileName;
  private File csvFile;
  private File summaryFile;
  private BufferedWriter writer;
  private volatile boolean stopped = false;

  private AppAnrDiagnosticLogger(final Context context) {
    this.appContext = context.getApplicationContext();
    this.mainThreadBlockMonitor = new MainThreadBlockMonitor(AppAnrDiagnosticLogger::buildAnrWarningContext);
    this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
      final Thread thread = new Thread(runnable, "retail_app_anr_diag");
      thread.setDaemon(true);
      return thread;
    });
    this.startTimeMs = System.currentTimeMillis();
    this.baseFileName = "RetailAnrDiag_" + sanitize(BuildConfig.VERSION_NAME) + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date(startTimeMs));
  }

  public static void start(final Context context) {
    if (!ENABLE_ANR_DIAGNOSTICS || context == null) return;
    synchronized (LOCK) {
      START_COUNT.incrementAndGet();
      if (instance != null) return;
      try {
        instance = new AppAnrDiagnosticLogger(context);
        instance.startLocked();
      }
      catch (Throwable t) {
        instance = null;
      }
    }
  }

  public static boolean isEnabled() {
    return ENABLE_ANR_DIAGNOSTICS;
  }

  public static void stop() {
    if (!ENABLE_ANR_DIAGNOSTICS) return;
    synchronized (LOCK) {
      if (START_COUNT.decrementAndGet() > 0) return;
      START_COUNT.set(0);
      if (instance == null) return;
      instance.stopLocked();
      instance = null;
    }
  }

  public static void updateCurrentActivity(final Activity activity) {
    if (activity == null) return;
    currentActivity = activity.getClass().getSimpleName();
  }

  public static void updateCurrentFragment(final String fragmentName) {
    currentFragment = safe(fragmentName);
    appFeature = currentFragment.length() > 0 ? currentFragment : currentActivity;
  }

  public static void recordRfidInitCall(final String eventName) {
    RFID_INIT_CALL_COUNT.incrementAndGet();
    recordRfidLifecycleEvent("INITIALIZING", eventName);
  }

  public static void recordRfidReleaseCall(final String eventName) {
    RFID_RELEASE_CALL_COUNT.incrementAndGet();
    recordRfidLifecycleEvent("RELEASING", eventName);
  }

  public static void recordInventoryStartCall(final String eventName) {
    INVENTORY_START_CALL_COUNT.incrementAndGet();
  }

  public static void recordInventoryStopCall(final String eventName) {
    INVENTORY_STOP_CALL_COUNT.incrementAndGet();
  }

  public static void recordRfidLifecycleEvent(final String state, final String eventName) {
    rfidLifecycleState = safe(state);
    lastRfidLifecycleEvent = safe(eventName);
    lastRfidLifecycleTimestampMs = System.currentTimeMillis();
    if ("INVENTORY_RUNNING".equals(rfidLifecycleState)) inventoryRunning = true;
    else if ("IDLE".equals(rfidLifecycleState) || "STOPPING".equals(rfidLifecycleState) || "RELEASING".equals(rfidLifecycleState) || "RELEASED".equals(rfidLifecycleState) || "ERROR".equals(rfidLifecycleState)) inventoryRunning = false;
  }

  public static void updateReaderConnected(final boolean connected) {
    readerConnected = connected;
  }

  public static void markMainThreadOperation(final String operation) {
    final String safeOperation = safe(operation);
    currentOperation = safeOperation;
    lastKnownOperation = safeOperation;
    lastKnownOperationTimestampMs = System.currentTimeMillis();
  }

  public static void clearMainThreadOperation(final String operation) {
    final String safeOperation = safe(operation);
    if (safeOperation.length() <= 0 || safeOperation.equals(currentOperation)) currentOperation = "";
  }

  public static String getSuspectedBlockingArea() {
    if (currentOperation.length() > 0) return currentOperation;
    return System.currentTimeMillis() - lastKnownOperationTimestampMs <= 10000L ? lastKnownOperation : "";
  }

  private void startLocked() throws IOException {
    final File dir = getDiagnosticsDir();
    if (!dir.exists() && !dir.mkdirs()) throw new IOException("Cannot create diagnostics directory");
    csvFile = new File(dir, baseFileName + ".csv");
    summaryFile = new File(dir, baseFileName + "_summary.txt");
    writer = new BufferedWriter(new FileWriter(csvFile, true));
    writer.write(CSV_HEADER);
    writer.newLine();
    writer.flush();
    mainThreadBlockMonitor.start();
    writeSummarySafe();
    executor.scheduleAtFixedRate(() -> {
      if (!stopped) writeSampleSafe("SAMPLE");
    }, SAMPLE_INTERVAL_SECONDS, SAMPLE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    Log.i(TAG, "Started ANR diagnostic logger: " + csvFile.getAbsolutePath());
  }

  private void stopLocked() {
    stopped = true;
    writeSampleSafe("STOP");
    writeSummarySafe();
    mainThreadBlockMonitor.stop();
    closeWriter();
    executor.shutdownNow();
    Log.i(TAG, "Stopped ANR diagnostic logger");
  }

  private void writeSampleSafe(final String rowType) {
    synchronized (this) {
      try {
        if (writer == null) return;
        final long now = System.currentTimeMillis();
        final MainThreadBlockMonitor.MainThreadBlockStats stats = mainThreadBlockMonitor.snapshot();
        final MemorySnapshot memory = readMemorySnapshot();
        final GcSnapshot gc = readGcSnapshot();
        writer.write(joinCsv(
            rowType,
            formatTimestamp(now),
            String.valueOf(Math.max(0L, (now - startTimeMs) / 1000L)),
            currentActivity,
            currentFragment,
            appFeature,
            String.valueOf(stats.blockCount),
            String.valueOf(stats.maxBlockMs),
            String.valueOf(stats.lastBlockMs),
            MainThreadBlockMonitor.formatTimestamp(stats.lastBlockTimestampMs),
            String.valueOf(stats.totalBlockedMs),
            String.valueOf(stats.running),
            String.valueOf(RFID_INIT_CALL_COUNT.get()),
            String.valueOf(RFID_RELEASE_CALL_COUNT.get()),
            String.valueOf(INVENTORY_START_CALL_COUNT.get()),
            String.valueOf(INVENTORY_STOP_CALL_COUNT.get()),
            rfidLifecycleState,
            lastRfidLifecycleEvent,
            formatTimestampOrBlank(lastRfidLifecycleTimestampMs),
            String.valueOf(inventoryRunning),
            String.valueOf(readerConnected),
            String.valueOf(readProcessThreadCount()),
            String.valueOf(memory.usedMb),
            String.valueOf(memory.freeMb),
            gc.count >= 0L ? String.valueOf(gc.count) : "",
            gc.timeMs >= 0L ? String.valueOf(gc.timeMs) : "",
            lastKnownOperation,
            getSuspectedBlockingArea(),
            stats.lastAnrWarning,
            anrRiskAssessment(stats),
            recommendation(stats)));
        writer.newLine();
        writer.flush();
        writeSummarySafe();
      }
      catch (Throwable ignored) {
        // ANR diagnostics must never interrupt app features.
      }
    }
  }

  private void writeSummarySafe() {
    BufferedWriter summaryWriter = null;
    try {
      if (summaryFile == null) return;
      final MainThreadBlockMonitor.MainThreadBlockStats stats = mainThreadBlockMonitor.snapshot();
      summaryWriter = new BufferedWriter(new FileWriter(summaryFile, false));
      summaryWriter.write("ANR Diagnostics Summary");
      summaryWriter.newLine();
      summaryWriter.write("-----------------------");
      summaryWriter.newLine();
      summaryWriter.write("diagnostic_build=" + BuildConfig.VERSION_NAME);
      summaryWriter.newLine();
      summaryWriter.write("session_duration_seconds=" + Math.max(0L, (System.currentTimeMillis() - startTimeMs) / 1000L));
      summaryWriter.newLine();
      summaryWriter.write("main_thread_block_count=" + stats.blockCount);
      summaryWriter.newLine();
      summaryWriter.write("main_thread_max_block_ms=" + stats.maxBlockMs);
      summaryWriter.newLine();
      summaryWriter.write("main_thread_last_block_ms=" + stats.lastBlockMs);
      summaryWriter.newLine();
      summaryWriter.write("main_thread_last_block_timestamp=" + MainThreadBlockMonitor.formatTimestamp(stats.lastBlockTimestampMs));
      summaryWriter.newLine();
      summaryWriter.write("main_thread_total_blocked_ms=" + stats.totalBlockedMs);
      summaryWriter.newLine();
      summaryWriter.write("main_thread_monitor_running=" + stats.running);
      summaryWriter.newLine();
      summaryWriter.write("rfid_init_call_count=" + RFID_INIT_CALL_COUNT.get());
      summaryWriter.newLine();
      summaryWriter.write("rfid_release_call_count=" + RFID_RELEASE_CALL_COUNT.get());
      summaryWriter.newLine();
      summaryWriter.write("inventory_start_call_count=" + INVENTORY_START_CALL_COUNT.get());
      summaryWriter.newLine();
      summaryWriter.write("inventory_stop_call_count=" + INVENTORY_STOP_CALL_COUNT.get());
      summaryWriter.newLine();
      summaryWriter.write("rfid_lifecycle_state=" + rfidLifecycleState);
      summaryWriter.newLine();
      summaryWriter.write("last_lifecycle_event=" + lastRfidLifecycleEvent);
      summaryWriter.newLine();
      summaryWriter.write("last_rfid_lifecycle_timestamp=" + formatTimestampOrBlank(lastRfidLifecycleTimestampMs));
      summaryWriter.newLine();
      summaryWriter.write("last_anr_warning=" + safe(stats.lastAnrWarning));
      summaryWriter.newLine();
      summaryWriter.write("anr_risk_assessment=" + anrRiskAssessment(stats));
      summaryWriter.newLine();
      summaryWriter.write("recommendation=" + recommendation(stats));
      summaryWriter.newLine();
      summaryWriter.flush();
    }
    catch (Throwable ignored) {
      // Summary is optional.
    }
    finally {
      if (summaryWriter != null) {
        try { summaryWriter.close(); }
        catch (IOException ignored) { }
      }
    }
  }

  private static String buildAnrWarningContext() {
    return "activity=" + currentActivity
        + " | fragment=" + currentFragment
        + " | feature=" + appFeature
        + " | state=" + rfidLifecycleState
        + " | inventoryRunning=" + inventoryRunning
        + " | readerConnected=" + readerConnected
        + " | initCalls=" + RFID_INIT_CALL_COUNT.get()
        + " | operation=" + getSuspectedBlockingArea();
  }

  private static String anrRiskAssessment(final MainThreadBlockMonitor.MainThreadBlockStats stats) {
    if (stats == null || stats.blockCount <= 0L) return "LOW";
    if (stats.maxBlockMs > 5000L || stats.blockCount > 2L) return "HIGH";
    return "MEDIUM";
  }

  private static String recommendation(final MainThreadBlockMonitor.MainThreadBlockStats stats) {
    final String risk = anrRiskAssessment(stats);
    if ("LOW".equals(risk)) return "No ANR risk observed.";
    final String suspectedArea = getSuspectedBlockingArea();
    if (suspectedArea.length() > 0) return "Main-thread block detected; review suspected blocking area: " + suspectedArea + ".";
    if (RFID_INIT_CALL_COUNT.get() > 1L && RFID_RELEASE_CALL_COUNT.get() > 0L) return "Repeated RFID init/release detected; review hardware lifecycle.";
    return "Main-thread block detected; review app feature and lifecycle context.";
  }

  private MemorySnapshot readMemorySnapshot() {
    try {
      final Runtime runtime = Runtime.getRuntime();
      final long usedBytes = runtime.totalMemory() - runtime.freeMemory();
      final long freeBytes = runtime.maxMemory() - usedBytes;
      return new MemorySnapshot(bytesToMb(usedBytes), bytesToMb(freeBytes));
    }
    catch (Throwable ignored) {
      return new MemorySnapshot(-1L, -1L);
    }
  }

  private GcSnapshot readGcSnapshot() {
    try {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return new GcSnapshot(-1L, -1L);
      final Map<String, String> stats = Debug.getRuntimeStats();
      return new GcSnapshot(parseLong(stats.get("art.gc.gc-count"), -1L), parseLong(stats.get("art.gc.gc-time"), -1L));
    }
    catch (Throwable ignored) {
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

  private void closeWriter() {
    try {
      if (writer != null) {
        writer.flush();
        writer.close();
      }
    }
    catch (IOException ignored) {
    }
    writer = null;
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
    if (safeValue.indexOf(',') < 0 && safeValue.indexOf('"') < 0 && safeValue.indexOf('\n') < 0 && safeValue.indexOf('\r') < 0) return safeValue;
    return "\"" + safeValue.replace("\"", "\"\"") + "\"";
  }

  private static long parseLong(final String value, final long fallback) {
    try {
      return value == null ? fallback : Long.parseLong(value.trim());
    }
    catch (Throwable ignored) {
      return fallback;
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

  private static String sanitize(final String value) {
    return safe(value).replaceAll("[^A-Za-z0-9_.-]", "_");
  }

  private static String safe(final String value) {
    return value == null ? "" : value;
  }

  private static void closeQuietly(final BufferedReader reader) {
    if (reader == null) return;
    try { reader.close(); }
    catch (IOException ignored) { }
  }

  private static final class MemorySnapshot {
    final long usedMb;
    final long freeMb;

    MemorySnapshot(final long usedMb, final long freeMb) {
      this.usedMb = usedMb;
      this.freeMb = freeMb;
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

  public static final class LifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    @Override public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) { updateCurrentActivity(activity); }
    @Override public void onActivityStarted(final Activity activity) { updateCurrentActivity(activity); }
    @Override public void onActivityResumed(final Activity activity) { updateCurrentActivity(activity); }
    @Override public void onActivityPaused(final Activity activity) { updateCurrentActivity(activity); }
    @Override public void onActivityStopped(final Activity activity) { updateCurrentActivity(activity); }
    @Override public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) { }
    @Override public void onActivityDestroyed(final Activity activity) { updateCurrentActivity(activity); }
  }
}
