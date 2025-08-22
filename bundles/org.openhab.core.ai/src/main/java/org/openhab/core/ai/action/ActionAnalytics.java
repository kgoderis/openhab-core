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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Analytics data for an action.
 * 
 * This class provides comprehensive analytics including usage patterns,
 * performance trends, and operational insights for actions.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ActionAnalytics {

    private final String actionId;
    private final long totalExecutions;
    private final long successfulExecutions;
    private final long failedExecutions;
    private final double successRate;
    private final double failureRate;
    private final Duration averageExecutionTime;
    private final Duration minExecutionTime;
    private final Duration maxExecutionTime;
    private final Map<String, Long> executionByAgent;
    private final Map<String, Long> executionByTimeOfDay;
    private final Map<String, Long> executionByDayOfWeek;
    private final List<String> commonErrorMessages;
    private final Map<String, Long> parameterUsage;
    private final Instant firstExecution;
    private final Instant lastExecution;
    private final Duration totalExecutionTime;

    /**
     * Private constructor for builder pattern.
     */
    private ActionAnalytics(Builder builder) {
        this.actionId = builder.actionId;
        this.totalExecutions = builder.totalExecutions;
        this.successfulExecutions = builder.successfulExecutions;
        this.failedExecutions = builder.failedExecutions;
        this.successRate = builder.totalExecutions > 0 ? (double) builder.successfulExecutions / builder.totalExecutions
                : 0.0;
        this.failureRate = builder.totalExecutions > 0 ? (double) builder.failedExecutions / builder.totalExecutions
                : 0.0;
        this.averageExecutionTime = builder.averageExecutionTime;
        this.minExecutionTime = builder.minExecutionTime;
        this.maxExecutionTime = builder.maxExecutionTime;
        this.executionByAgent = Map.copyOf(builder.executionByAgent);
        this.executionByTimeOfDay = Map.copyOf(builder.executionByTimeOfDay);
        this.executionByDayOfWeek = Map.copyOf(builder.executionByDayOfWeek);
        this.commonErrorMessages = List.copyOf(builder.commonErrorMessages);
        this.parameterUsage = Map.copyOf(builder.parameterUsage);
        this.firstExecution = builder.firstExecution;
        this.lastExecution = builder.lastExecution;
        this.totalExecutionTime = builder.totalExecutionTime;
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

    public double getSuccessRate() {
        return successRate;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public Duration getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public Duration getMinExecutionTime() {
        return minExecutionTime;
    }

    public Duration getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public Map<String, Long> getExecutionByAgent() {
        return executionByAgent;
    }

    public Map<String, Long> getExecutionByTimeOfDay() {
        return executionByTimeOfDay;
    }

    public Map<String, Long> getExecutionByDayOfWeek() {
        return executionByDayOfWeek;
    }

    public List<String> getCommonErrorMessages() {
        return commonErrorMessages;
    }

    public Map<String, Long> getParameterUsage() {
        return parameterUsage;
    }

    public Instant getFirstExecution() {
        return firstExecution;
    }

    public Instant getLastExecution() {
        return lastExecution;
    }

    public Duration getTotalExecutionTime() {
        return totalExecutionTime;
    }

    /**
     * Get the most active agent for this action.
     * 
     * @return the agent ID with the most executions, or null if no executions
     */
    public @Nullable String getMostActiveAgent() {
        return executionByAgent.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get the peak usage time of day.
     * 
     * @return the time of day with the most executions, or null if no executions
     */
    public @Nullable String getPeakUsageTime() {
        return executionByTimeOfDay.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get the most common error message.
     * 
     * @return the most common error message, or null if no errors
     */
    public @Nullable String getMostCommonError() {
        return commonErrorMessages.isEmpty() ? null : commonErrorMessages.get(0);
    }

    /**
     * Get the most used parameter.
     * 
     * @return the parameter name with the most usage, or null if no parameters
     */
    public @Nullable String getMostUsedParameter() {
        return parameterUsage.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    /**
     * Create a new builder for ActionAnalytics.
     *
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder from this ActionAnalytics for modification.
     *
     * @return a new Builder with current values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public String toString() {
        return String.format("ActionAnalytics{actionId='%s', totalExecutions=%d, successRate=%.2f%%}", actionId,
                totalExecutions, successRate * 100);
    }

    /**
     * Builder for creating ActionAnalytics instances.
     *
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    public static final class Builder {
        private String actionId = "";
        private long totalExecutions = 0;
        private long successfulExecutions = 0;
        private long failedExecutions = 0;
        private Duration averageExecutionTime = Duration.ZERO;
        private Duration minExecutionTime = Duration.ZERO;
        private Duration maxExecutionTime = Duration.ZERO;
        private Map<String, Long> executionByAgent = Map.of();
        private Map<String, Long> executionByTimeOfDay = Map.of();
        private Map<String, Long> executionByDayOfWeek = Map.of();
        private List<String> commonErrorMessages = List.of();
        private Map<String, Long> parameterUsage = Map.of();
        private Instant firstExecution = Instant.now();
        private Instant lastExecution = Instant.now();
        private Duration totalExecutionTime = Duration.ZERO;

        /**
         * Default constructor.
         */
        public Builder() {
        }

        /**
         * Copy constructor.
         *
         * @param source the source ActionAnalytics
         */
        public Builder(ActionAnalytics source) {
            this.actionId = source.actionId;
            this.totalExecutions = source.totalExecutions;
            this.successfulExecutions = source.successfulExecutions;
            this.failedExecutions = source.failedExecutions;
            this.averageExecutionTime = source.averageExecutionTime;
            this.minExecutionTime = source.minExecutionTime;
            this.maxExecutionTime = source.maxExecutionTime;
            this.executionByAgent = new HashMap<>(source.executionByAgent);
            this.executionByTimeOfDay = new HashMap<>(source.executionByTimeOfDay);
            this.executionByDayOfWeek = new HashMap<>(source.executionByDayOfWeek);
            this.commonErrorMessages = new ArrayList<>(source.commonErrorMessages);
            this.parameterUsage = new HashMap<>(source.parameterUsage);
            this.firstExecution = source.firstExecution;
            this.lastExecution = source.lastExecution;
            this.totalExecutionTime = source.totalExecutionTime;
        }

        public Builder withActionId(String actionId) {
            this.actionId = Objects.requireNonNull(actionId, "actionId");
            return this;
        }

        public Builder withTotalExecutions(long totalExecutions) {
            this.totalExecutions = totalExecutions;
            return this;
        }

        public Builder withSuccessfulExecutions(long successfulExecutions) {
            this.successfulExecutions = successfulExecutions;
            return this;
        }

        public Builder withFailedExecutions(long failedExecutions) {
            this.failedExecutions = failedExecutions;
            return this;
        }

        public Builder withAverageExecutionTime(Duration averageExecutionTime) {
            this.averageExecutionTime = Objects.requireNonNull(averageExecutionTime, "averageExecutionTime");
            return this;
        }

        public Builder withMinExecutionTime(Duration minExecutionTime) {
            this.minExecutionTime = Objects.requireNonNull(minExecutionTime, "minExecutionTime");
            return this;
        }

        public Builder withMaxExecutionTime(Duration maxExecutionTime) {
            this.maxExecutionTime = Objects.requireNonNull(maxExecutionTime, "maxExecutionTime");
            return this;
        }

        public Builder withExecutionByAgent(Map<String, Long> executionByAgent) {
            this.executionByAgent = Objects.requireNonNull(executionByAgent, "executionByAgent");
            return this;
        }

        public Builder withExecutionByTimeOfDay(Map<String, Long> executionByTimeOfDay) {
            this.executionByTimeOfDay = Objects.requireNonNull(executionByTimeOfDay, "executionByTimeOfDay");
            return this;
        }

        public Builder withExecutionByDayOfWeek(Map<String, Long> executionByDayOfWeek) {
            this.executionByDayOfWeek = Objects.requireNonNull(executionByDayOfWeek, "executionByDayOfWeek");
            return this;
        }

        public Builder withCommonErrorMessages(List<String> commonErrorMessages) {
            this.commonErrorMessages = Objects.requireNonNull(commonErrorMessages, "commonErrorMessages");
            return this;
        }

        public Builder withParameterUsage(Map<String, Long> parameterUsage) {
            this.parameterUsage = Objects.requireNonNull(parameterUsage, "parameterUsage");
            return this;
        }

        public Builder withFirstExecution(Instant firstExecution) {
            this.firstExecution = Objects.requireNonNull(firstExecution, "firstExecution");
            return this;
        }

        public Builder withLastExecution(Instant lastExecution) {
            this.lastExecution = Objects.requireNonNull(lastExecution, "lastExecution");
            return this;
        }

        public Builder withTotalExecutionTime(Duration totalExecutionTime) {
            this.totalExecutionTime = Objects.requireNonNull(totalExecutionTime, "totalExecutionTime");
            return this;
        }

        /**
         * Build the ActionAnalytics.
         *
         * @return the new ActionAnalytics
         */
        public ActionAnalytics build() {
            if (actionId.isBlank()) {
                throw new IllegalArgumentException("actionId must not be blank");
            }
            if (totalExecutions < 0) {
                throw new IllegalArgumentException("totalExecutions must be non-negative");
            }
            if (successfulExecutions < 0) {
                throw new IllegalArgumentException("successfulExecutions must be non-negative");
            }
            if (failedExecutions < 0) {
                throw new IllegalArgumentException("failedExecutions must be non-negative");
            }
            if (totalExecutions != successfulExecutions + failedExecutions) {
                throw new IllegalArgumentException(
                        "totalExecutions must equal successfulExecutions + failedExecutions");
            }
            if (minExecutionTime.toNanos() > maxExecutionTime.toNanos() && maxExecutionTime.toNanos() > 0) {
                throw new IllegalArgumentException("minExecutionTime cannot be greater than maxExecutionTime");
            }
            if (firstExecution.isAfter(lastExecution)) {
                throw new IllegalArgumentException("firstExecution cannot be after lastExecution");
            }
            return new ActionAnalytics(this);
        }
    }
}
