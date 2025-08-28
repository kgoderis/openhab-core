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
package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for sampling-specific metrics.
 * 
 * <p>
 * This interface provides functionality for measuring and reporting sampling-specific
 * metrics including sample generation rates, cache efficiency, and sampling distribution statistics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface SamplingMetrics {

    /**
     * Get the total number of samples generated.
     * 
     * @return total samples generated count
     */
    long totalSamplesGenerated();

    /**
     * Get the number of cache hits.
     * 
     * @return cache hits count
     */
    long cacheHits();

    /**
     * Get the number of cache misses.
     * 
     * @return cache misses count
     */
    long cacheMisses();

    /**
     * Get the current cache size.
     * 
     * @return current cache size
     */
    int cacheSize();

    /**
     * Get the sampling model type.
     * 
     * @return sampling model type
     */
    String modelType();

    /**
     * Get the sampling model version.
     * 
     * @return sampling model version
     */
    String modelVersion();

    /**
     * Calculate the cache hit rate as a percentage.
     * 
     * @return cache hit rate between 0.0 and 1.0, or 0.0 if no cache operations
     */
    default double cacheHitRate() {
        long totalCacheOperations = cacheHits() + cacheMisses();
        return totalCacheOperations > 0 ? (double) cacheHits() / totalCacheOperations : 0.0;
    }

    /**
     * Calculate the cache efficiency (hit rate as percentage).
     * 
     * @return cache efficiency as percentage (0.0 to 100.0)
     */
    default double cacheEfficiency() {
        return cacheHitRate() * 100.0;
    }

    /**
     * Calculate samples generated per cache access.
     * 
     * @return samples per cache access, or 0.0 if no cache operations
     */
    default double samplesPerCacheAccess() {
        long totalCacheOperations = cacheHits() + cacheMisses();
        return totalCacheOperations > 0 ? (double) totalSamplesGenerated() / totalCacheOperations : 0.0;
    }

    /**
     * Calculate the cache utilization rate.
     * 
     * @return cache utilization rate between 0.0 and 1.0
     */
    default double cacheUtilizationRate() {
        // This would typically require knowing max cache size, but we'll use current size as approximation
        long totalOperations = total();
        return totalOperations > 0 ? (double) cacheSize() / totalOperations : 0.0;
    }

    /**
     * Get the total count of operations (from CountsMetrics).
     * 
     * @return total count
     */
    long total();
}
