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
package org.openhab.core.ai.agent.delegation;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Extracted performance data holder for delegation metrics.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class PerformanceData {
    private final long totalDelegations;
    private final long successfulDelegations;
    private final long failedDelegations;
    private final long averageExecutionMs;

    public PerformanceData(long totalDelegations, long successfulDelegations, long failedDelegations,
            long averageExecutionMs) {
        this.totalDelegations = totalDelegations;
        this.successfulDelegations = successfulDelegations;
        this.failedDelegations = failedDelegations;
        this.averageExecutionMs = averageExecutionMs;
    }

    public long getTotalDelegations() { return totalDelegations; }
    public long getSuccessfulDelegations() { return successfulDelegations; }
    public long getFailedDelegations() { return failedDelegations; }
    public long getAverageExecutionMs() { return averageExecutionMs; }
}


