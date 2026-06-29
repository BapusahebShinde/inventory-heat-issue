package com.itek.retail.reader;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class MainThreadBlockMonitor {
  private static final String TAG = "ANR_DIAG";
  private static final long HEARTBEAT_INTERVAL_MS = 1000L;
  private static final long BLOCK_THRESHOLD_MS = 2000L;

  public interface ContextProvider {
    String getAnrDiagnosticContext();
  }

  public static final class MainThreadBlockStats {
    public final long blockCount;
    public final long maxBlockMs;
    public final long lastBlockMs;
    public final long lastBlockTimestampMs;
    public final long totalBlockedMs;
    public final boolean running;
    public final String lastAnrWarning;

    private MainThreadBlockStats(
        final long blockCount,
        final long maxBlockMs,
        final long lastBlockMs,
        final long lastBlockTimestampMs,
        final long totalBlockedMs,
        final boolean running,
        final String lastAnrWarning) {
      this.blockCount = blockCount;
      this.maxBlockMs = maxBlockMs;
      this.lastBlockMs = lastBlockMs;
      this.lastBlockTimestampMs = lastBlockTimestampMs;
      this.totalBlockedMs = totalBlockedMs;
      this.running = running;
      this.lastAnrWarning = lastAnrWarning;
    }
  }

  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private final ContextProvider contextProvider;
  private final Object lifecycleLock = new Object();
  private final AtomicLong blockCount = new AtomicLong(0L);
  private final AtomicLong maxBlockMs = new AtomicLong(0L);
  private final AtomicLong lastBlockMs = new AtomicLong(0L);
  private final AtomicLong lastBlockTimestampMs = new AtomicLong(0L);
  private final AtomicLong totalBlockedMs = new AtomicLong(0L);
  private volatile ScheduledExecutorService scheduler = null;
  private volatile boolean running = false;
  private volatile String lastAnrWarning = "";

  public MainThreadBlockMonitor(final ContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  public void start() {
    synchronized (lifecycleLock) {
      if (running) return;
      running = true;
      scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        final Thread thread = new Thread(runnable, "retail_anr_monitor");
        thread.setDaemon(true);
        return thread;
      });
      scheduler.scheduleAtFixedRate(() -> {
        if (!running) return;
        final long postedAtMs = SystemClock.elapsedRealtime();
        final String context = readContext();
        try {
          mainHandler.post(() -> recordHeartbeat(postedAtMs, context));
        }
        catch (Throwable ignored) {
          // ANR diagnostics must never crash the app.
        }
      }, 0L, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
  }

  public void stop() {
    synchronized (lifecycleLock) {
      running = false;
      if (scheduler != null) {
        scheduler.shutdownNow();
        scheduler = null;
      }
    }
  }

  public boolean isRunning() {
    return running;
  }

  public MainThreadBlockStats snapshot() {
    return new MainThreadBlockStats(
        blockCount.get(),
        maxBlockMs.get(),
        lastBlockMs.get(),
        lastBlockTimestampMs.get(),
        totalBlockedMs.get(),
        running,
        safe(lastAnrWarning));
  }

  private void recordHeartbeat(final long postedAtMs, final String context) {
    try {
      if (!running) return;
      final long delayMs = Math.max(0L, SystemClock.elapsedRealtime() - postedAtMs);
      if (delayMs < BLOCK_THRESHOLD_MS) return;
      blockCount.incrementAndGet();
      lastBlockMs.set(delayMs);
      lastBlockTimestampMs.set(System.currentTimeMillis());
      totalBlockedMs.addAndGet(delayMs);
      updateMax(maxBlockMs, delayMs);
      lastAnrWarning = "Main thread blocked for " + delayMs + " ms" + (context.length() > 0 ? " | " + context : "");
      Log.w(TAG, lastAnrWarning);
    }
    catch (Throwable ignored) {
      // Keep the monitor side-band and non-crashing.
    }
  }

  private String readContext() {
    try {
      return contextProvider == null ? "" : safe(contextProvider.getAnrDiagnosticContext());
    }
    catch (Throwable ignored) {
      return "";
    }
  }

  private static void updateMax(final AtomicLong value, final long candidate) {
    long current = value.get();
    while (candidate > current && !value.compareAndSet(current, candidate)) current = value.get();
  }

  public static String formatTimestamp(final long timeMs) {
    if (timeMs <= 0L) return "";
    try {
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date(timeMs));
    }
    catch (Throwable ignored) {
      return "";
    }
  }

  private static String safe(final String value) {
    return value == null ? "" : value;
  }
}
