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
 * Immutable record representing count metrics.
 * 
 * <p>This record provides a simple way to represent count-based metrics
 * including total operations, successes, and failures.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record Counts(long total, long success, long failure) {
    
    /**
     * Calculate the success rate as a percentage.
     * 
     * @return success rate as a percentage (0.0 to 100.0)
     */
    public double successRate() {
        if (total == 0) {
            return 0.0;
        }
        return (success * 100.0) / total;
    }
    
    /**
     * Calculate the failure rate as a percentage.
     * 
     * @return failure rate as a percentage (0.0 to 100.0)
     */
    public double failureRate() {
        if (total == 0) {
            return 0.0;
        }
        return (failure * 100.0) / total;
    }
    
    /**
     * Check if there are any operations recorded.
     * 
     * @return true if total > 0, false otherwise
     */
    public boolean hasOperations() {
        return total > 0;
    }
}
