package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Specification performance metrics record matching monitor expectations.
 */
@NonNullByDefault
public record SpecificationPerformanceMetrics(
        String specificationId,
        long totalRequests,
        long successfulRequests,
        long failedRequests,
        long totalResponseTime,
        double averageResponseTime,
        double successRate,
        int currentThroughput,
        Instant lastUpdated) {}
