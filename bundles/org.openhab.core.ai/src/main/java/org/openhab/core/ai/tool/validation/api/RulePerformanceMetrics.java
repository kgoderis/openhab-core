package org.openhab.core.ai.tool.validation.api;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics for a validation rule.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
class RulePerformanceMetrics {
    private final java.util.concurrent.atomic.AtomicLong executionCount = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong totalExecutionTimeMs = new java.util.concurrent.atomic.AtomicLong(
            0);
    private final java.util.concurrent.atomic.AtomicLong successCount = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong failureCount = new java.util.concurrent.atomic.AtomicLong(0);

    void recordExecution(long executionTimeMs, boolean success) {
        executionCount.incrementAndGet();
        totalExecutionTimeMs.addAndGet(executionTimeMs);
        if (success) {
            successCount.incrementAndGet();
        } else {
            failureCount.incrementAndGet();
        }
    }

    Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        long totalExecutions = executionCount.get();

        metrics.put("executionCount", totalExecutions);
        metrics.put("totalExecutionTimeMs", totalExecutionTimeMs.get());
        metrics.put("successCount", successCount.get());
        metrics.put("failureCount", failureCount.get());

        if (totalExecutions > 0) {
            metrics.put("averageExecutionTimeMs", totalExecutionTimeMs.get() / totalExecutions);
            metrics.put("successRate", (double) successCount.get() / totalExecutions);
        } else {
            metrics.put("averageExecutionTimeMs", 0L);
            metrics.put("successRate", 0.0);
        }

        return metrics;
    }
}
