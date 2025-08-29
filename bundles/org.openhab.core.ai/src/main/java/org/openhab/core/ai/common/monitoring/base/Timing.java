/*
 * Copyright (c) 2010-2024 openHAB e.V. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.openhab.core.ai.common.monitoring.base;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Immutable record representing timing metrics.
 * 
 * <p>This record provides a simple way to represent timing-based metrics
 * including total duration and average timing information.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record Timing(long totalDurationNanos) {
    
    /**
     * Get the total duration in milliseconds.
     * 
     * @return total duration in milliseconds
     */
    public long totalDurationMs() {
        return totalDurationNanos / 1_000_000;
    }
    
    /**
     * Get the total duration in seconds.
     * 
     * @return total duration in seconds
     */
    public double totalDurationSeconds() {
        return totalDurationNanos / 1_000_000_000.0;
    }
    
    /**
     * Calculate the average duration per operation.
     * 
     * @param operationCount the number of operations
     * @return average duration in nanoseconds per operation
     */
    public double averageDurationNanos(long operationCount) {
        if (operationCount == 0) {
            return 0.0;
        }
        return (double) totalDurationNanos / operationCount;
    }
    
    /**
     * Calculate the average duration per operation in milliseconds.
     * 
     * @param operationCount the number of operations
     * @return average duration in milliseconds per operation
     */
    public double averageDurationMs(long operationCount) {
        return averageDurationNanos(operationCount) / 1_000_000.0;
    }
    
    /**
     * Check if there is any timing data recorded.
     * 
     * @return true if totalDurationNanos > 0, false otherwise
     */
    public boolean hasTimingData() {
        return totalDurationNanos > 0;
    }
}
