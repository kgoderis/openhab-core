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
package org.openhab.core.ai.reasoning.error.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.api.TimeRange;

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

    // Extracted: ErrorContext, ErrorHandlingResult, ErrorRecoveryResult, ErrorAnalytics, TimeRange
}
