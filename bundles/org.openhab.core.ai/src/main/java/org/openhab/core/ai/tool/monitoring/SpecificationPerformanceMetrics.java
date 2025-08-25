package org.openhab.core.ai.tool.monitoring;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics for tool specifications.
 * 
 * <p>
 * This record provides performance metrics for individual tool specifications,
 * including request counts, success rates, response times, and throughput.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record SpecificationPerformanceMetrics(String specificationId, long totalRequests, long successfulRequests,
        long failedRequests, long totalResponseTime, double averageResponseTime, double successRate,
        int currentThroughput, Instant lastUpdated) {
}
