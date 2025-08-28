/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.common.monitoring.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.SamplingMetrics;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Immutable snapshot of sampling model metrics data.
 * 
 * This class provides a thread-safe, immutable view of sampling model-related metrics
 * including counts, latency, and sampling-specific statistics.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record SamplingModelSnapshot(Counts counts, Timing timing, long totalSamplesGenerated, long cacheHits,
        long cacheMisses, int cacheSize, String modelType, String modelVersion,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, SamplingMetrics {

    @Override
    public long total() {
        return counts.total();
    }

    @Override
    public long success() {
        return counts.success();
    }

    @Override
    public long failure() {
        return counts.failure();
    }

    @Override
    public double successRate() {
        return CountsMetrics.super.successRate();
    }

    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    /**
     * Get the average response time in milliseconds.
     *
     * @return average response time in milliseconds
     */
    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }
}
