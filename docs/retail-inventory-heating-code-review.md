# Retail Inventory Heating Investigation - Code Review Diagnostic Report

## Reviewed Build Context

This review focuses on the Build 187 changes described in the handover: immediate beep after accepted unique EPC detection plus passive diagnostic logging. The available repository history in this workspace contains only the current branch snapshot, so this report reviews the current hot path implementation directly rather than performing a commit-to-commit diff against Build 186.

## Executive Summary

The current implementation does not show evidence of database backlog, UI throttling, or per-tag CSV writes in the diagnostic logger. The logger samples on a single background executor every five seconds, and the audio feedback path uses the shared `ToneGenerator` beep helper rather than creating a new `MediaPlayer` for each accepted inventory EPC.

The most important code-review observation is that the inventory callback hot path still performs multiple operations for every raw RFID callback, including string normalization/key construction, an atomic raw-callback counter increment, and synchronized duplicate-tracking checks. With the supplied session volume of 370,837 raw callbacks and approximately 91% duplicates, duplicate-path overhead is the most likely application-side area to profile next.

## Findings

### 1. Immediate beep implementation is lightweight

Accepted inventory reads call `beep()` only after the unique read has been accepted. The duplicate path returns before the beep. The shared beep helper uses `ToneGenerator.startTone(...)`, which avoids per-EPC `MediaPlayer.create(...)` allocation.

Risk level: Low.

### 2. Diagnostic logger is side-band, not per-tag CSV logging

The diagnostic logger writes CSV samples every five seconds using a single scheduled background executor. It also swallows diagnostics failures so logging does not interrupt inventory scanning.

Risk level: Low to Medium. The scheduled logger itself is low cost, but Build 187 still adds hot-path counters that run per callback.

### 3. Raw callback counting happens for every inventory callback

Every inventory callback records a raw diagnostic count before duplicate filtering. In the common handler this is `recordInventoryRawCallbackForDiagnostics()`, and Chainway inventory callbacks do the same when `sessionAction == INVENTORY`.

Risk level: Medium. A single atomic increment is small, but it runs hundreds of thousands of times in long sessions.

### 4. Duplicate filtering remains the key hot path

For each accepted/rejected inventory read, the code builds a normalized inventory key, enters `inventoryPerformanceLock`, checks a `HashSet`, and updates counters. With the supplied diagnostic data showing 337,484 duplicate callbacks, this path dominates the application-side callback cost.

Risk level: Medium to High. Even if each operation is individually small, the duplicate volume makes this the primary code path to benchmark.

### 5. Database batching appears appropriate

Inventory writes are queued in memory and flushed in batches. The code copies pending maps into lists, clears them, and performs database insertion outside the performance lock. This is consistent with the diagnostic evidence that database queue and flush timings remained healthy.

Risk level: Low.

## Recommended Next Step

Run one controlled A/B performance build to isolate whether Build 187 heating is caused by application callback overhead or by RFID hardware/firmware behavior:

1. Keep the same APK code, same reader power, same site location, same operator pattern, and same 45-60 minute scan duration.
2. Add callback micro-timing in memory only, not per-tag logging:
   - total callback processing nanoseconds,
   - max callback processing nanoseconds,
   - accepted unique callback count,
   - duplicate callback count,
   - optional 5-second average callback processing time.
3. Add a feature flag to disable only immediate unique-EPC beep while keeping diagnostics on.
4. Add a feature flag to disable only diagnostic raw callback counting while keeping the beep on.
5. Compare three runs:
   - Build 187 behavior as-is,
   - beep disabled,
   - raw diagnostic hot-path counting disabled.

If callback average/max time rises before the observed read-rate collapse, focus on application hot-path optimization. If callback time remains flat while raw callback rate falls from approximately 646/sec to 26/sec, prioritize RFID SDK, reader firmware, RF power behavior, antenna duty cycle, and hardware thermal investigation.

## Code Optimization Candidates

These should be considered after micro-timing confirms application CPU overhead:

1. Avoid duplicate inventory-key construction by passing the already normalized key through duplicate rejection and accept/store paths.
2. Consider replacing per-callback `AtomicLong` raw counting with lock-local primitive counters or per-thread counters that are aggregated every five seconds.
3. Avoid diagnostic logger startup in production builds unless explicitly enabled by a diagnostic flag.
4. Add callback timing fields to the CSV summary so field logs can prove whether callback processing slows before read-rate degradation.
5. Keep the immediate beep on the accepted-unique path only; do not switch it to `playSound(...)` because that path allocates `MediaPlayer` objects.

## Current Evidence-Based Conclusion

Based on the current code review and diagnostic summary, the diagnostic CSV writer and immediate beep implementation are unlikely to be the sole heating cause. The highest-value next investigation is callback-path timing, especially duplicate callback processing, because the reported session had approximately 91% duplicates and more than 370,000 raw callbacks.

## Implemented Diagnostic Code Changes

The diagnostic CSV should now include `callback_avg_ms` and `callback_max_ms` so long-running field sessions can show whether application callback processing time increases before the RFID read-rate drop. CPU reporting also now has a fallback path using Android process elapsed CPU time when `/proc/stat` or `/proc/self/stat` is unavailable or restricted on the handheld.


## Signed APK Stability Follow-up

The Chainway/C5 callback wrapper was rolled back to match the previously working signed APK path. CPU diagnostics remain available in the CSV, while Chainway callback timing should be reintroduced only after a signed APK smoke test confirms stability.

## Callback Hot-Path Overhead Follow-up

Callback duration tracking now avoids the shared inventory performance lock by using atomic counters for total and max duration. CPU, memory, thermal, GC, and thread sampling remain in the five-second diagnostic logger thread rather than the RFID callback path.

## UHF Module Visibility Follow-up

The diagnostic CSV now separates Android OS temperature from RFID module diagnostics. `battery_temp_c` still comes from Android battery telemetry, while `uhf_module_temp_c` is sampled from the reader SDK when supported. Chainway/C5 diagnostics use the SDK `getTemperature()` API on the active UART/BLE reader instance and sample it only from the five-second diagnostic snapshot path, not from the per-tag callback.

Additional five-second fields were added for RF power, reader connection/antenna state, SDK error/warning counters, callback queue depth, RSSI min/avg/max, duplicate percentage, battery voltage/current, screen brightness, and placeholders for known-unread tag test counts. The per-tag callback remains limited to counters/timing/RSSI aggregation and does not perform per-tag file I/O.
