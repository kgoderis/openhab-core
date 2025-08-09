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
package org.openhab.core.ai.action;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Performance metrics for an action.
 * 
 * This class tracks various performance metrics including execution counts,
 * success/failure rates, response times, and other performance indicators.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ActionPerformanceMetrics {

    private final String actionId;
    private final long totalExecutions;
    private final long successfulExecutions;
    private final long failedExecutions;
    private final long totalExecutionTimeMs;
    private final long minExecutionTimeMs;
    private final long maxExecutionTimeMs;
    private final long averageExecutionTimeMs;
    private final Instant firstExecution;
    private final Instant lastExecution;
    private final double successRate;
    private final double failureRate;

    private ActionPerformanceMetrics(Builder builder) {
        this.actionId = builder.actionId;
        this.totalExecutions = builder.totalExecutions;
        this.successfulExecutions = builder.successfulExecutions;
        this.failedExecutions = builder.failedExecutions;
        this.totalExecutionTimeMs = builder.totalExecutionTimeMs;
        this.minExecutionTimeMs = builder.minExecutionTimeMs;
        this.maxExecutionTimeMs = builder.maxExecutionTimeMs;
        this.averageExecutionTimeMs = builder.totalExecutions > 0
                ? builder.totalExecutionTimeMs / builder.totalExecutions
                : 0;
        this.firstExecution = builder.firstExecution;
        this.lastExecution = builder.lastExecution;
        this.successRate = builder.totalExecutions > 0 ? (double) builder.successfulExecutions / builder.totalExecutions
                : 0.0;
        this.failureRate = builder.totalExecutions > 0 ? (double) builder.failedExecutions / builder.totalExecutions
                : 0.0;
    }

    // Getters
    public String getActionId() {
        return actionId;
    }

    public long getTotalExecutions() {
        return totalExecutions;
    }

    public long getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public long getFailedExecutions() {
        return failedExecutions;
    }

    public long getTotalExecutionTimeMs() {
        return totalExecutionTimeMs;
    }

    public long getMinExecutionTimeMs() {
        return minExecutionTimeMs;
    }

    public long getMaxExecutionTimeMs() {
        return maxExecutionTimeMs;
    }

    public long getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public Instant getFirstExecution() {
        return firstExecution;
    }

    public Instant getLastExecution() {
        return lastExecution;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public Duration getAverageExecutionTime() {
        return Duration.ofMillis(averageExecutionTimeMs);
    }

    public Duration getMinExecutionTime() {
        return Duration.ofMillis(minExecutionTimeMs);
    }

    public Duration getMaxExecutionTime() {
        return Duration.ofMillis(maxExecutionTimeMs);
    }

    /**
     * Builder for ActionPerformanceMetrics.
     */
    public static class Builder {
        private String actionId = "";
        private long totalExecutions = 0;
        private long successfulExecutions = 0;
        private long failedExecutions = 0;
        private long totalExecutionTimeMs = 0;
        private long minExecutionTimeMs = Long.MAX_VALUE;
        private long maxExecutionTimeMs = 0;
        private Instant firstExecution = Instant.now();
        private Instant lastExecution = Instant.now();

        public Builder actionId(String actionId) {
            this.actionId = actionId;
            return this;
        }

        public Builder totalExecutions(long totalExecutions) {
            this.totalExecutions = totalExecutions;
            return this;
        }

        public Builder successfulExecutions(long successfulExecutions) {
            this.successfulExecutions = successfulExecutions;
            return this;
        }

        public Builder failedExecutions(long failedExecutions) {
            this.failedExecutions = failedExecutions;
            return this;
        }

        public Builder totalExecutionTimeMs(long totalExecutionTimeMs) {
            this.totalExecutionTimeMs = totalExecutionTimeMs;
            return this;
        }

        public Builder minExecutionTimeMs(long minExecutionTimeMs) {
            this.minExecutionTimeMs = minExecutionTimeMs;
            return this;
        }

        public Builder maxExecutionTimeMs(long maxExecutionTimeMs) {
            this.maxExecutionTimeMs = maxExecutionTimeMs;
            return this;
        }

        public Builder firstExecution(Instant firstExecution) {
            this.firstExecution = firstExecution;
            return this;
        }

        public Builder lastExecution(Instant lastExecution) {
            this.lastExecution = lastExecution;
            return this;
        }

        public ActionPerformanceMetrics build() {
            return new ActionPerformanceMetrics(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return String.format("ActionPerformanceMetrics{actionId='%s', totalExecutions=%d, successRate=%.2f%%}",
                actionId, totalExecutions, successRate * 100);
    }
}
