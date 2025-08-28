package org.openhab.core.ai.common.monitoring.patterns;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.utils.SystemMetricsCollector;

/**
 * Utility class for recording configuration operation metrics using the enhanced MetricsService.
 * 
 * <p>
 * This class provides methods to record comprehensive metrics for configuration operations
 * including cache hits/misses, reloads, file operations, and configuration changes.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConfigurationOperationMetrics {

    private final MetricsService metricsService;

    public ConfigurationOperationMetrics(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Record cache operation metrics.
     * 
     * @param cacheName the name of the cache
     * @param operationType the type of cache operation (hit, miss, put, evict)
     * @param key the cache key
     * @param valueSize the size of the cached value
     * @param operationTime the time taken for the cache operation
     */
    public void recordCacheOperation(String cacheName, String operationType, String key, long valueSize,
            Duration operationTime) {
        boolean success = operationTime.toNanos() < 1_000_000; // Consider successful if under 1ms

        metricsService.recordOperation("configuration", "cache-" + operationType).withSuccess(success)
                .withDuration(operationTime.toNanos()).withData("cacheName", cacheName)
                .withData("operationType", operationType).withData("key", key).withData("valueSize", valueSize)
                // Performance Metrics Enhancements
                .withTimingContext(2, key.length(), valueSize) // Simple cache operation
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0)
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics(success ? null : "cache-operation-slow", success ? null : 1, 0, false)
                .withUserExperienceMetrics(operationTime.toNanos() / 1_000_000, 0, success ? 5 : 3) // Cache should be
                                                                                                    // fast
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record configuration reload metrics.
     * 
     * @param configType the type of configuration being reloaded
     * @param configPath the path to the configuration file
     * @param fileSize the size of the configuration file
     * @param reloadTime the time taken to reload the configuration
     * @param validationErrors the number of validation errors found
     */
    public void recordConfigurationReload(String configType, String configPath, long fileSize, Duration reloadTime,
            int validationErrors) {
        boolean success = validationErrors == 0; // Success if no validation errors

        metricsService.recordOperation("configuration", "reload").withSuccess(success)
                .withDuration(reloadTime.toNanos()).withData("configType", configType)
                .withData("configPath", configPath).withData("fileSize", fileSize)
                .withData("validationErrors", validationErrors)
                // Performance Metrics Enhancements
                .withTimingContext(6, fileSize, fileSize / 2) // Moderate complexity for reload
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "validation-errors", success ? null : 3, 0, false)
                .withUserExperienceMetrics(reloadTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record file operation metrics.
     * 
     * @param operationType the type of file operation (read, write, delete, move)
     * @param filePath the path to the file
     * @param fileSize the size of the file
     * @param operationTime the time taken for the file operation
     * @param success whether the operation was successful
     */
    public void recordFileOperation(String operationType, String filePath, long fileSize, Duration operationTime,
            boolean success) {
        metricsService.recordOperation("configuration", "file-" + operationType).withSuccess(success)
                .withDuration(operationTime.toNanos()).withData("operationType", operationType)
                .withData("filePath", filePath).withData("fileSize", fileSize)
                // Performance Metrics Enhancements
                .withTimingContext(4, fileSize, fileSize) // Moderate complexity for file operations
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "file-operation-failure", success ? null : 2, 0, false)
                .withUserExperienceMetrics(operationTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record configuration change metrics.
     * 
     * @param configKey the configuration key that changed
     * @param oldValue the old value
     * @param newValue the new value
     * @param changeTime the time taken to apply the change
     * @param impactLevel the impact level of the change (1-5)
     * @param rollbackRequired whether rollback is required
     */
    public void recordConfigurationChange(String configKey, String oldValue, String newValue, Duration changeTime,
            int impactLevel, boolean rollbackRequired) {
        boolean success = changeTime.toMillis() < 1000; // Configuration changes should be reasonably fast

        long valueSize = Math.max(oldValue.length(), newValue.length());

        metricsService.recordOperation("configuration", "change").withSuccess(success)
                .withDuration(changeTime.toNanos()).withData("configKey", configKey).withData("oldValue", oldValue)
                .withData("newValue", newValue).withData("impactLevel", impactLevel)
                .withData("rollbackRequired", rollbackRequired)
                // Performance Metrics Enhancements
                .withTimingContext(5, valueSize, valueSize) // Moderate complexity for configuration changes
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), 0)
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(), 0)
                .withQualityMetrics(success ? null : "config-change-slow", success ? null : impactLevel, 0,
                        rollbackRequired)
                .withUserExperienceMetrics(changeTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record configuration validation metrics.
     * 
     * @param configType the type of configuration being validated
     * @param validationRules the number of validation rules applied
     * @param validationTime the time taken for validation
     * @param errorCount the number of validation errors
     * @param warningCount the number of validation warnings
     */
    public void recordConfigurationValidation(String configType, int validationRules, Duration validationTime,
            int errorCount, int warningCount) {
        boolean success = errorCount == 0; // Success if no errors

        metricsService.recordOperation("configuration", "validation").withSuccess(success)
                .withDuration(validationTime.toNanos()).withData("configType", configType)
                .withData("validationRules", validationRules).withData("errorCount", errorCount)
                .withData("warningCount", warningCount)
                // Performance Metrics Enhancements
                .withTimingContext(7, validationRules * 100L, 0) // High complexity for validation
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "validation-errors", success ? null : 3, 0, false)
                .withUserExperienceMetrics(validationTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record configuration backup metrics.
     * 
     * @param backupType the type of backup operation
     * @param backupSize the size of the backup
     * @param backupTime the time taken for the backup
     * @param compressionRatio the compression ratio achieved
     * @param success whether the backup was successful
     */
    public void recordConfigurationBackup(String backupType, long backupSize, Duration backupTime,
            double compressionRatio, boolean success) {
        metricsService.recordOperation("configuration", "backup").withSuccess(success)
                .withDuration(backupTime.toNanos()).withData("backupType", backupType)
                .withData("backupSize", backupSize).withData("compressionRatio", compressionRatio)
                // Performance Metrics Enhancements
                .withTimingContext(6, backupSize, backupSize / 2) // High complexity for backup operations
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "backup-failure", success ? null : 3, 0, false)
                .withUserExperienceMetrics(backupTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }

    /**
     * Record configuration migration metrics.
     * 
     * @param migrationType the type of migration
     * @param sourceVersion the source version
     * @param targetVersion the target version
     * @param migrationTime the time taken for migration
     * @param migratedItems the number of items migrated
     * @param failedItems the number of items that failed to migrate
     */
    public void recordConfigurationMigration(String migrationType, String sourceVersion, String targetVersion,
            Duration migrationTime, int migratedItems, int failedItems) {
        boolean success = failedItems == 0; // Success if no items failed to migrate

        metricsService.recordOperation("configuration", "migration").withSuccess(success)
                .withDuration(migrationTime.toNanos()).withData("migrationType", migrationType)
                .withData("sourceVersion", sourceVersion).withData("targetVersion", targetVersion)
                .withData("migratedItems", migratedItems).withData("failedItems", failedItems)
                // Performance Metrics Enhancements
                .withTimingContext(8, migratedItems * 200L, migratedItems * 100L) // Very high complexity for migration
                .withResourceUtilization(SystemMetricsCollector.getCurrentMemoryUsage(),
                        SystemMetricsCollector.getCurrentCpuUsage(), SystemMetricsCollector.estimateDiskIOTime())
                .withConcurrencyMetrics(SystemMetricsCollector.getActiveOperationsCount(),
                        SystemMetricsCollector.getCurrentQueueSize(),
                        (int) SystemMetricsCollector.getTotalContentionCount())
                .withQualityMetrics(success ? null : "migration-failures", success ? null : 4, 0, false)
                .withUserExperienceMetrics(migrationTime.toMillis(), 0, success ? 4 : 2)
                .withSystemHealthCorrelation(SystemMetricsCollector.calculateSystemHealthScore(),
                        SystemMetricsCollector.getActiveAlertsCount(),
                        SystemMetricsCollector.calculateResourceAvailability(),
                        SystemMetricsCollector.getSystemLoadAverage())
                .record();
    }
}
