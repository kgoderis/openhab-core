/*
 * Copyright (c) 2010-2024 openHAB e.V. and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.openhab.core.ai.common.monitoring.patterns;

import java.time.Duration;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;

/**
 * Static utility class for recording configuration manager metrics.
 * 
 * <p>This class provides static methods for recording various configuration manager operations
 * including configuration operations, changes, validation, and reload.
 * All methods follow the static utility pattern with MetricsService as the first parameter.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Record configuration operation
 * ConfigurationManagerMetrics.recordConfigurationOperation(metricsService, "update", "agent-config", 
 *     true, Duration.ofMillis(100), 5);
 * 
 * // Record configuration change
 * ConfigurationManagerMetrics.recordConfigurationChange(metricsService, "agent-123", "timeout", 
 *     "30s", "60s", Duration.ofMillis(50));
 * }</pre>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ConfigurationManagerMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private ConfigurationManagerMetrics() {
        // Utility class
    }

    /**
     * Record configuration operation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param operation the type of configuration operation (create, read, update, delete, etc.)
     * @param configurationType the type of configuration (agent-config, model-config, system-config, etc.)
     * @param success whether the configuration operation was successful
     * @param duration the duration of the configuration operation
     * @param configurationCount the number of configuration items involved
     */
    public static void recordConfigurationOperation(MetricsService metricsService, String operation, String configurationType,
            boolean success, Duration duration, int configurationCount) {
        try {
            metricsService.recordOperation("configuration-manager", "operation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("operation", operation)
                    .withData("configurationType", configurationType)
                    .withData("configurationCount", configurationCount)
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting configuration operations
        }
    }

    /**
     * Record configuration change metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param configurationId the unique identifier of the configuration
     * @param propertyName the name of the configuration property that changed
     * @param oldValue the previous value of the configuration property
     * @param newValue the new value of the configuration property
     * @param duration the duration of the configuration change
     */
    public static void recordConfigurationChange(MetricsService metricsService, String configurationId, String propertyName,
            @Nullable String oldValue, @Nullable String newValue, Duration duration) {
        try {
            var operation = metricsService.recordOperation("configuration-manager", "change")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("configurationId", configurationId)
                    .withData("propertyName", propertyName);

            if (oldValue != null) {
                operation.withData("oldValue", oldValue);
            }

            if (newValue != null) {
                operation.withData("newValue", newValue);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting configuration operations
        }
    }

    /**
     * Record configuration validation metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param validationType the type of configuration validation (schema-validation, constraint-validation, etc.)
     * @param success whether the configuration validation was successful
     * @param duration the duration of the validation process
     * @param validationErrors the number of validation errors found
     * @param configurationSize the size of the configuration being validated
     */
    public static void recordConfigurationValidation(MetricsService metricsService, String validationType, boolean success,
            Duration duration, int validationErrors, long configurationSize) {
        try {
            metricsService.recordOperation("configuration-manager", "validation")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("validationType", validationType)
                    .withData("validationErrors", validationErrors)
                    .withData("configurationSize", configurationSize)
                    .withData("validationRate", configurationSize / (duration.toSeconds() + 1.0))
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting configuration operations
        }
    }

    /**
     * Record configuration reload metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param reloadType the type of configuration reload (full-reload, partial-reload, hot-reload, etc.)
     * @param success whether the configuration reload was successful
     * @param duration the duration of the reload process
     * @param configurationsReloaded the number of configurations reloaded
     * @param reloadSource the source of the reload (file, database, api, etc.)
     */
    public static void recordConfigurationReload(MetricsService metricsService, String reloadType, boolean success,
            Duration duration, int configurationsReloaded, String reloadSource) {
        try {
            metricsService.recordOperation("configuration-manager", "reload")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("reloadType", reloadType)
                    .withData("configurationsReloaded", configurationsReloaded)
                    .withData("reloadSource", reloadSource)
                    .withData("reloadRate", configurationsReloaded / (duration.toSeconds() + 1.0))
                    .record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting configuration operations
        }
    }

    /**
     * Record configuration backup metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param backupType the type of configuration backup (full-backup, incremental-backup, etc.)
     * @param success whether the configuration backup was successful
     * @param duration the duration of the backup process
     * @param backupSize the size of the backup in bytes
     * @param backupLocation the location where the backup was stored
     */
    public static void recordConfigurationBackup(MetricsService metricsService, String backupType, boolean success,
            Duration duration, long backupSize, @Nullable String backupLocation) {
        try {
            var operation = metricsService.recordOperation("configuration-manager", "backup")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("backupType", backupType)
                    .withData("backupSize", backupSize);

            if (backupLocation != null) {
                operation.withData("backupLocation", backupLocation);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting configuration operations
        }
    }

    /**
     * Record configuration restore metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param restoreType the type of configuration restore (full-restore, partial-restore, etc.)
     * @param success whether the configuration restore was successful
     * @param duration the duration of the restore process
     * @param configurationsRestored the number of configurations restored
     * @param restoreSource the source of the restore (backup-file, database, etc.)
     */
    public static void recordConfigurationRestore(MetricsService metricsService, String restoreType, boolean success,
            Duration duration, int configurationsRestored, @Nullable String restoreSource) {
        try {
            var operation = metricsService.recordOperation("configuration-manager", "restore")
                    .withSuccess(success)
                    .withDuration(duration.toNanos())
                    .withData("restoreType", restoreType)
                    .withData("configurationsRestored", configurationsRestored);

            if (restoreSource != null) {
                operation.withData("restoreSource", restoreSource);
            }

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting configuration operations
        }
    }

    /**
     * Record configuration performance metrics.
     * 
     * @param metricsService the metrics service to record with
     * @param performanceMetric the type of configuration performance metric being measured
     * @param value the performance value
     * @param duration the measurement duration
     * @param context additional context data for the performance metric
     */
    public static void recordConfigurationPerformance(MetricsService metricsService, String performanceMetric, double value,
            Duration duration, Map<String, Object> context) {
        try {
            var operation = metricsService.recordOperation("configuration-manager", "performance")
                    .withSuccess(true)
                    .withDuration(duration.toNanos())
                    .withData("performanceMetric", performanceMetric)
                    .withData("value", value)
                    .withData("durationMs", duration.toMillis());

            // Add context data
            context.forEach(operation::withData);

            operation.record();
        } catch (Exception e) {
            // Log error but don't throw to avoid disrupting configuration operations
        }
    }
}
