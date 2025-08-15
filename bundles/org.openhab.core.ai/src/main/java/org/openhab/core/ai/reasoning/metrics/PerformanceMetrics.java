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
package org.openhab.core.ai.reasoning.metrics;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics data class for multi-step reasoning engine.
 *
 * This top-level class was extracted from an inner class to improve cohesion
 * and testability. It uses a builder for convenient construction.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PerformanceMetrics {
    private final long totalSessions;
    private final long successfulSessions;
    private final long failedSessions;
    private final long totalSteps;
    private final long totalActions;
    private final double averageSessionDuration;

    PerformanceMetrics(PerformanceMetricsBuilder builder) {
        this.totalSessions = builder.totalSessions;
        this.successfulSessions = builder.successfulSessions;
        this.failedSessions = builder.failedSessions;
        this.totalSteps = builder.totalSteps;
        this.totalActions = builder.totalActions;
        this.averageSessionDuration = builder.averageSessionDuration;
    }

    public long getTotalSessions() {
        return totalSessions;
    }

    public long getSuccessfulSessions() {
        return successfulSessions;
    }

    public long getFailedSessions() {
        return failedSessions;
    }

    public long getTotalSteps() {
        return totalSteps;
    }

    public long getTotalActions() {
        return totalActions;
    }

    public double getAverageSessionDuration() {
        return averageSessionDuration;
    }

    public static PerformanceMetricsBuilder builder() {
        return new PerformanceMetricsBuilder();
    }

    /* Extracted: org.openhab.core.ai.reasoning.PerformanceMetricsBuilder */
}
