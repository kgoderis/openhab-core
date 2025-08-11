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
package org.openhab.core.ai.reasoning.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for error handling in the AI reasoning system.
 * 
 * This interface provides a common abstraction for error handling operations,
 * ensuring consistent behavior across all reasoning components.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ErrorHandler {

    /**
     * Handle an error that occurred during reasoning.
     * 
     * @param error The error to handle
     * @param context The error context
     * @return A CompletableFuture containing the error handling result
     */
    CompletableFuture<ErrorHandlingResult> handleError(Throwable error, ErrorContext context);

    /**
     * Log an error for monitoring and analytics.
     * 
     * @param error The error to log
     * @param context The error context
     * @return A CompletableFuture containing the logging result
     */
    CompletableFuture<Boolean> logError(Throwable error, ErrorContext context);

    /**
     * Get error analytics for monitoring.
     * 
     * @param timeRange The time range for analytics
     * @return A CompletableFuture containing the error analytics
     */
    CompletableFuture<ErrorAnalytics> getErrorAnalytics(TimeRange timeRange);

    /**
     * Recover from an error.
     * 
     * @param error The error to recover from
     * @param context The error context
     * @return A CompletableFuture containing the recovery result
     */
    CompletableFuture<ErrorRecoveryResult> recoverFromError(Throwable error, ErrorContext context);

    /**
     * Error context.
     */
    class ErrorContext {
        private final String componentId;
        private final String operation;
        private final String agentId;
        private final long timestamp;
        private final String[] additionalInfo;

        public ErrorContext(String componentId, String operation, String agentId, long timestamp,
                String[] additionalInfo) {
            this.componentId = componentId;
            this.operation = operation;
            this.agentId = agentId;
            this.timestamp = timestamp;
            this.additionalInfo = additionalInfo;
        }

        public String getComponentId() {
            return componentId;
        }

        public String getOperation() {
            return operation;
        }

        public String getAgentId() {
            return agentId;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String[] getAdditionalInfo() {
            return additionalInfo;
        }
    }

    /**
     * Error handling result.
     */
    class ErrorHandlingResult {
        private final boolean handled;
        private final String action;
        private final String message;
        private final boolean shouldRetry;

        public ErrorHandlingResult(boolean handled, String action, String message, boolean shouldRetry) {
            this.handled = handled;
            this.action = action;
            this.message = message;
            this.shouldRetry = shouldRetry;
        }

        public boolean isHandled() {
            return handled;
        }

        public String getAction() {
            return action;
        }

        public String getMessage() {
            return message;
        }

        public boolean shouldRetry() {
            return shouldRetry;
        }
    }

    /**
     * Error recovery result.
     */
    class ErrorRecoveryResult {
        private final boolean recovered;
        private final String recoveryAction;
        private final String message;
        private final long recoveryTime;

        public ErrorRecoveryResult(boolean recovered, String recoveryAction, String message, long recoveryTime) {
            this.recovered = recovered;
            this.recoveryAction = recoveryAction;
            this.message = message;
            this.recoveryTime = recoveryTime;
        }

        public boolean isRecovered() {
            return recovered;
        }

        public String getRecoveryAction() {
            return recoveryAction;
        }

        public String getMessage() {
            return message;
        }

        public long getRecoveryTime() {
            return recoveryTime;
        }
    }

    /**
     * Error analytics.
     */
    class ErrorAnalytics {
        private final long totalErrors;
        private final long criticalErrors;
        private final long recoverableErrors;
        private final double averageRecoveryTime;
        private final String[] topErrorTypes;

        public ErrorAnalytics(long totalErrors, long criticalErrors, long recoverableErrors, double averageRecoveryTime,
                String[] topErrorTypes) {
            this.totalErrors = totalErrors;
            this.criticalErrors = criticalErrors;
            this.recoverableErrors = recoverableErrors;
            this.averageRecoveryTime = averageRecoveryTime;
            this.topErrorTypes = topErrorTypes;
        }

        public long getTotalErrors() {
            return totalErrors;
        }

        public long getCriticalErrors() {
            return criticalErrors;
        }

        public long getRecoverableErrors() {
            return recoverableErrors;
        }

        public double getAverageRecoveryTime() {
            return averageRecoveryTime;
        }

        public String[] getTopErrorTypes() {
            return topErrorTypes;
        }
    }

    /**
     * Time range for analytics.
     */
    enum TimeRange {
        LAST_HOUR,
        LAST_DAY,
        LAST_WEEK,
        LAST_MONTH
    }
}
