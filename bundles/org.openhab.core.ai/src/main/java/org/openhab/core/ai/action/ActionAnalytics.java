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
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

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

    /* package */ ActionAnalytics(ActionAnalyticsBuilder builder) {
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
        this.executionByAgent = builder.executionByAgent;
        this.executionByTimeOfDay = builder.executionByTimeOfDay;
        this.executionByDayOfWeek = builder.executionByDayOfWeek;
        this.commonErrorMessages = builder.commonErrorMessages;
        this.parameterUsage = builder.parameterUsage;
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
    public String getMostActiveAgent() {
        return executionByAgent.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get the peak usage time of day.
     * 
     * @return the time of day with the most executions, or null if no executions
     */
    public String getPeakUsageTime() {
        return executionByTimeOfDay.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Get the most common error message.
     * 
     * @return the most common error message, or null if no errors
     */
    public String getMostCommonError() {
        return commonErrorMessages.isEmpty() ? null : commonErrorMessages.get(0);
    }

    /**
     * Get the most used parameter.
     * 
     * @return the parameter name with the most usage, or null if no parameters
     */
    public String getMostUsedParameter() {
        return parameterUsage.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    public static ActionAnalyticsBuilder builder() {
        return new ActionAnalyticsBuilder();
    }

    @Override
    public String toString() {
        return String.format("ActionAnalytics{actionId='%s', totalExecutions=%d, successRate=%.2f%%}", actionId,
                totalExecutions, successRate * 100);
    }
}
