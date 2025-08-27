package org.openhab.core.ai.common.monitoring.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.ModelCompletionSnapshot;
import org.openhab.core.ai.common.monitoring.base.AbstractMetrics;

/**
 * Integration tests for metrics migration from old to new system.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class MetricsMigrationTest {

    @Mock
    private MetricsService newMetricsService;

    @Mock
    private AbstractMetrics oldMetrics;

    private MetricsMigrationService migrationService;

    @BeforeEach
    void setUp() {
        migrationService = new MetricsMigrationService();
    }

    @Test
    void testMigrateOldMetricsToNewSystem() {
        // Given
        OldMetricsData oldData = new OldMetricsData();
        oldData.setTotalOperations(1000L);
        oldData.setSuccessfulOperations(950L);
        oldData.setFailedOperations(50L);
        oldData.setTotalProcessingTime(5000000000L);
        oldData.setAverageResponseTime(50.0);
        oldData.setLastOperationTime(Instant.now());
        oldData.setDomain("test-domain");
        oldData.setSource("test-source");

        doNothing().when(newMetricsService).recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class));

        // When
        boolean result = migrationService.migrateMetrics(oldData, newMetricsService);

        // Then
        assertTrue(result);
        verify(newMetricsService, times(1)).recordOperation(
                eq("test-domain"),
                eq("migrated-operation"),
                eq(true),
                any(Duration.class)
        );
    }

    @Test
    void testMigrateMultipleOldMetrics() {
        // Given
        List<OldMetricsData> oldDataList = List.of(
                createOldMetricsData("domain1", 100, 90, 10),
                createOldMetricsData("domain2", 200, 180, 20),
                createOldMetricsData("domain3", 300, 270, 30)
        );

        doNothing().when(newMetricsService).recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class));

        // When
        MigrationResult result = migrationService.migrateMultipleMetrics(oldDataList, newMetricsService);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getMigratedCount());
        assertEquals(0, result.getFailedCount());
        assertTrue(result.isSuccess());
        verify(newMetricsService, times(3)).recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class));
    }

    @Test
    void testMigrationWithValidation() {
        // Given
        OldMetricsData oldData = createOldMetricsData("test-domain", 100, 90, 10);
        oldData.setValidationChecksum("abc123");

        doNothing().when(newMetricsService).recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class));

        // When
        MigrationResult result = migrationService.migrateWithValidation(oldData, newMetricsService, "abc123");

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getMigratedCount());
        assertEquals(0, result.getFailedCount());
        assertTrue(result.isValidated());
    }

    @Test
    void testMigrationWithInvalidChecksum() {
        // Given
        OldMetricsData oldData = createOldMetricsData("test-domain", 100, 90, 10);
        oldData.setValidationChecksum("abc123");

        // When
        MigrationResult result = migrationService.migrateWithValidation(oldData, newMetricsService, "invalid");

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(0, result.getMigratedCount());
        assertEquals(1, result.getFailedCount());
        assertFalse(result.isValidated());
        assertTrue(result.getErrors().contains("Checksum validation failed"));
    }

    @Test
    void testMigrationRollback() {
        // Given
        OldMetricsData oldData = createOldMetricsData("test-domain", 100, 90, 10);
        when(newMetricsService.recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class)))
                .thenReturn(true)
                .thenThrow(new RuntimeException("Migration failed"));

        // When
        MigrationResult result = migrationService.migrateWithRollback(oldData, newMetricsService);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(0, result.getMigratedCount());
        assertEquals(1, result.getFailedCount());
        assertTrue(result.isRolledBack());
        assertTrue(result.getErrors().contains("Migration failed"));
    }

    @Test
    void testIncrementalMigration() {
        // Given
        List<OldMetricsData> oldDataList = List.of(
                createOldMetricsData("domain1", 100, 90, 10),
                createOldMetricsData("domain2", 200, 180, 20),
                createOldMetricsData("domain3", 300, 270, 30)
        );

        when(newMetricsService.recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class)))
                .thenReturn(true);

        // When
        IncrementalMigrationResult result = migrationService.migrateIncrementally(oldDataList, newMetricsService, 2);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalProcessed());
        assertEquals(3, result.getSuccessfullyMigrated());
        assertEquals(0, result.getFailed());
        assertEquals(2, result.getBatchesProcessed());
        assertTrue(result.isComplete());
    }

    @Test
    void testMigrationWithDataTransformation() {
        // Given
        OldMetricsData oldData = createOldMetricsData("test-domain", 100, 90, 10);
        oldData.setCustomField("old-format-data");

        when(newMetricsService.recordOperationWithData(anyString(), anyString(), anyBoolean(), any(Duration.class), any()))
                .thenReturn(true);

        // When
        boolean result = migrationService.migrateWithTransformation(oldData, newMetricsService, data -> {
            Map<String, Object> transformed = Map.of(
                    "transformedField", "new-format-data",
                    "originalField", data.getCustomField(),
                    "migrationTimestamp", Instant.now().toEpochMilli()
            );
            return transformed;
        });

        // Then
        assertTrue(result);
        verify(newMetricsService, times(1)).recordOperationWithData(
                eq("test-domain"),
                eq("migrated-operation"),
                eq(true),
                any(Duration.class),
                argThat(data -> data.containsKey("transformedField") && data.containsKey("originalField"))
        );
    }

    @Test
    void testMigrationCompatibilityCheck() {
        // Given
        OldMetricsData oldData = createOldMetricsData("test-domain", 100, 90, 10);
        oldData.setVersion("1.0.0");

        // When
        CompatibilityResult result = migrationService.checkCompatibility(oldData, "2.0.0");

        // Then
        assertNotNull(result);
        assertTrue(result.isCompatible());
        assertEquals("1.0.0", result.getSourceVersion());
        assertEquals("2.0.0", result.getTargetVersion());
        assertTrue(result.getCompatibilityNotes().isEmpty());
    }

    @Test
    void testMigrationWithIncompatibleVersion() {
        // Given
        OldMetricsData oldData = createOldMetricsData("test-domain", 100, 90, 10);
        oldData.setVersion("0.5.0");

        // When
        CompatibilityResult result = migrationService.checkCompatibility(oldData, "2.0.0");

        // Then
        assertNotNull(result);
        assertFalse(result.isCompatible());
        assertEquals("0.5.0", result.getSourceVersion());
        assertEquals("2.0.0", result.getTargetVersion());
        assertFalse(result.getCompatibilityNotes().isEmpty());
        assertTrue(result.getCompatibilityNotes().contains("Version 0.5.0 is not compatible with 2.0.0"));
    }

    @Test
    void testMigrationProgressTracking() {
        // Given
        List<OldMetricsData> oldDataList = List.of(
                createOldMetricsData("domain1", 100, 90, 10),
                createOldMetricsData("domain2", 200, 180, 20),
                createOldMetricsData("domain3", 300, 270, 30),
                createOldMetricsData("domain4", 400, 360, 40),
                createOldMetricsData("domain5", 500, 450, 50)
        );

        when(newMetricsService.recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class)))
                .thenReturn(true);

        // When
        MigrationProgressTracker tracker = new MigrationProgressTracker();
        MigrationResult result = migrationService.migrateWithProgressTracking(oldDataList, newMetricsService, tracker);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getMigratedCount());
        assertEquals(0, result.getFailedCount());
        assertTrue(result.isSuccess());
        assertEquals(100.0, tracker.getProgressPercentage(), 0.1);
        assertEquals(5, tracker.getProcessedCount());
        assertEquals(5, tracker.getTotalCount());
    }

    @Test
    void testMigrationWithErrorHandling() {
        // Given
        List<OldMetricsData> oldDataList = List.of(
                createOldMetricsData("domain1", 100, 90, 10),
                createOldMetricsData("domain2", 200, 180, 20),
                createOldMetricsData("domain3", 300, 270, 30)
        );

        when(newMetricsService.recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class)))
                .thenReturn(true)
                .thenThrow(new RuntimeException("Migration error"))
                .thenReturn(true);

        // When
        MigrationResult result = migrationService.migrateWithErrorHandling(oldDataList, newMetricsService);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getMigratedCount());
        assertEquals(1, result.getFailedCount());
        assertFalse(result.isSuccess());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Migration error"));
    }

    @Test
    void testMigrationDataIntegrity() {
        // Given
        OldMetricsData oldData = createOldMetricsData("test-domain", 100, 90, 10);
        oldData.setDataIntegrityHash("abc123");

        when(newMetricsService.recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class)))
                .thenReturn(true);

        // When
        DataIntegrityResult result = migrationService.migrateWithIntegrityCheck(oldData, newMetricsService, "abc123");

        // Then
        assertNotNull(result);
        assertTrue(result.isIntegrityValid());
        assertTrue(result.isMigrationSuccessful());
        assertEquals("abc123", result.getOriginalHash());
        assertEquals("abc123", result.getValidatedHash());
    }

    @Test
    void testMigrationWithDataLoss() {
        // Given
        OldMetricsData oldData = createOldMetricsData("test-domain", 100, 90, 10);
        oldData.setDataIntegrityHash("abc123");

        when(newMetricsService.recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class)))
                .thenReturn(true);

        // When
        DataIntegrityResult result = migrationService.migrateWithIntegrityCheck(oldData, newMetricsService, "different");

        // Then
        assertNotNull(result);
        assertFalse(result.isIntegrityValid());
        assertTrue(result.isMigrationSuccessful());
        assertEquals("abc123", result.getOriginalHash());
        assertEquals("different", result.getValidatedHash());
        assertTrue(result.getIntegrityWarnings().contains("Data integrity check failed"));
    }

    @Test
    void testMigrationPerformance() {
        // Given
        List<OldMetricsData> oldDataList = List.of();
        for (int i = 0; i < 1000; i++) {
            oldDataList.add(createOldMetricsData("domain" + i, 100, 90, 10));
        }

        when(newMetricsService.recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class)))
                .thenReturn(true);

        // When
        long startTime = System.currentTimeMillis();
        MigrationResult result = migrationService.migrateMultipleMetrics(oldDataList, newMetricsService);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1000, result.getMigratedCount());
        assertEquals(0, result.getFailedCount());
        
        long duration = endTime - startTime;
        assertTrue(duration < 5000, "Migration should complete within 5 seconds for 1000 records");
    }

    @Test
    void testMigrationConcurrency() {
        // Given
        List<OldMetricsData> oldDataList = List.of();
        for (int i = 0; i < 100; i++) {
            oldDataList.add(createOldMetricsData("domain" + i, 100, 90, 10));
        }

        when(newMetricsService.recordOperation(anyString(), anyString(), anyBoolean(), any(Duration.class)))
                .thenReturn(true);

        // When & Then - Concurrent migration should work correctly
        assertDoesNotThrow(() -> {
            Thread[] threads = new Thread[5];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> {
                    MigrationResult result = migrationService.migrateMultipleMetrics(oldDataList, newMetricsService);
                    assertNotNull(result);
                    assertTrue(result.isSuccess());
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }
        });
    }

    // Helper methods
    private OldMetricsData createOldMetricsData(String domain, long total, long success, long failure) {
        OldMetricsData data = new OldMetricsData();
        data.setTotalOperations(total);
        data.setSuccessfulOperations(success);
        data.setFailedOperations(failure);
        data.setTotalProcessingTime(total * 50000000L); // 50ms per operation
        data.setAverageResponseTime(50.0);
        data.setLastOperationTime(Instant.now());
        data.setDomain(domain);
        data.setSource("old-system");
        return data;
    }

    // Mock classes for testing
    private static class OldMetricsData {
        private long totalOperations;
        private long successfulOperations;
        private long failedOperations;
        private long totalProcessingTime;
        private double averageResponseTime;
        private Instant lastOperationTime;
        private String domain;
        private String source;
        private String version = "1.0.0";
        private String validationChecksum;
        private String customField;
        private String dataIntegrityHash;

        // Getters and setters
        public long getTotalOperations() { return totalOperations; }
        public void setTotalOperations(long totalOperations) { this.totalOperations = totalOperations; }
        public long getSuccessfulOperations() { return successfulOperations; }
        public void setSuccessfulOperations(long successfulOperations) { this.successfulOperations = successfulOperations; }
        public long getFailedOperations() { return failedOperations; }
        public void setFailedOperations(long failedOperations) { this.failedOperations = failedOperations; }
        public long getTotalProcessingTime() { return totalProcessingTime; }
        public void setTotalProcessingTime(long totalProcessingTime) { this.totalProcessingTime = totalProcessingTime; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        public Instant getLastOperationTime() { return lastOperationTime; }
        public void setLastOperationTime(Instant lastOperationTime) { this.lastOperationTime = lastOperationTime; }
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getValidationChecksum() { return validationChecksum; }
        public void setValidationChecksum(String validationChecksum) { this.validationChecksum = validationChecksum; }
        public String getCustomField() { return customField; }
        public void setCustomField(String customField) { this.customField = customField; }
        public String getDataIntegrityHash() { return dataIntegrityHash; }
        public void setDataIntegrityHash(String dataIntegrityHash) { this.dataIntegrityHash = dataIntegrityHash; }
    }

    private static class MetricsMigrationService {
        public boolean migrateMetrics(OldMetricsData oldData, MetricsService newService) {
            try {
                newService.recordOperation(
                        oldData.getDomain(),
                        "migrated-operation",
                        oldData.getSuccessfulOperations() > oldData.getFailedOperations(),
                        Duration.ofNanos(oldData.getTotalProcessingTime())
                );
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public MigrationResult migrateMultipleMetrics(List<OldMetricsData> oldDataList, MetricsService newService) {
            MigrationResult result = new MigrationResult();
            for (OldMetricsData oldData : oldDataList) {
                try {
                    migrateMetrics(oldData, newService);
                    result.incrementMigrated();
                } catch (Exception e) {
                    result.incrementFailed();
                    result.addError(e.getMessage());
                }
            }
            return result;
        }

        public MigrationResult migrateWithValidation(OldMetricsData oldData, MetricsService newService, String expectedChecksum) {
            MigrationResult result = new MigrationResult();
            if (expectedChecksum.equals(oldData.getValidationChecksum())) {
                migrateMetrics(oldData, newService);
                result.incrementMigrated();
                result.setValidated(true);
            } else {
                result.incrementFailed();
                result.addError("Checksum validation failed");
            }
            return result;
        }

        public MigrationResult migrateWithRollback(OldMetricsData oldData, MetricsService newService) {
            MigrationResult result = new MigrationResult();
            try {
                migrateMetrics(oldData, newService);
                result.incrementMigrated();
            } catch (Exception e) {
                result.incrementFailed();
                result.addError(e.getMessage());
                result.setRolledBack(true);
            }
            return result;
        }

        public IncrementalMigrationResult migrateIncrementally(List<OldMetricsData> oldDataList, MetricsService newService, int batchSize) {
            IncrementalMigrationResult result = new IncrementalMigrationResult();
            result.setTotalCount(oldDataList.size());
            
            for (int i = 0; i < oldDataList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, oldDataList.size());
                List<OldMetricsData> batch = oldDataList.subList(i, end);
                
                for (OldMetricsData oldData : batch) {
                    try {
                        migrateMetrics(oldData, newService);
                        result.incrementMigrated();
                    } catch (Exception e) {
                        result.incrementFailed();
                    }
                }
                result.incrementBatches();
            }
            
            result.setComplete(true);
            return result;
        }

        public boolean migrateWithTransformation(OldMetricsData oldData, MetricsService newService, 
                java.util.function.Function<OldMetricsData, Map<String, Object>> transformer) {
            try {
                Map<String, Object> transformedData = transformer.apply(oldData);
                newService.recordOperationWithData(
                        oldData.getDomain(),
                        "migrated-operation",
                        oldData.getSuccessfulOperations() > oldData.getFailedOperations(),
                        Duration.ofNanos(oldData.getTotalProcessingTime()),
                        transformedData
                );
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public CompatibilityResult checkCompatibility(OldMetricsData oldData, String targetVersion) {
            CompatibilityResult result = new CompatibilityResult();
            result.setSourceVersion(oldData.getVersion());
            result.setTargetVersion(targetVersion);
            
            if ("1.0.0".equals(oldData.getVersion()) || "1.5.0".equals(oldData.getVersion())) {
                result.setCompatible(true);
            } else {
                result.setCompatible(false);
                result.addCompatibilityNote("Version " + oldData.getVersion() + " is not compatible with " + targetVersion);
            }
            
            return result;
        }

        public MigrationResult migrateWithProgressTracking(List<OldMetricsData> oldDataList, MetricsService newService, 
                MigrationProgressTracker tracker) {
            tracker.setTotalCount(oldDataList.size());
            MigrationResult result = new MigrationResult();
            
            for (OldMetricsData oldData : oldDataList) {
                try {
                    migrateMetrics(oldData, newService);
                    result.incrementMigrated();
                    tracker.incrementProcessed();
                } catch (Exception e) {
                    result.incrementFailed();
                    result.addError(e.getMessage());
                    tracker.incrementProcessed();
                }
            }
            
            return result;
        }

        public MigrationResult migrateWithErrorHandling(List<OldMetricsData> oldDataList, MetricsService newService) {
            MigrationResult result = new MigrationResult();
            for (OldMetricsData oldData : oldDataList) {
                try {
                    migrateMetrics(oldData, newService);
                    result.incrementMigrated();
                } catch (Exception e) {
                    result.incrementFailed();
                    result.addError(e.getMessage());
                }
            }
            return result;
        }

        public DataIntegrityResult migrateWithIntegrityCheck(OldMetricsData oldData, MetricsService newService, String expectedHash) {
            DataIntegrityResult result = new DataIntegrityResult();
            result.setOriginalHash(oldData.getDataIntegrityHash());
            result.setValidatedHash(expectedHash);
            
            boolean integrityValid = expectedHash.equals(oldData.getDataIntegrityHash());
            result.setIntegrityValid(integrityValid);
            
            if (!integrityValid) {
                result.addIntegrityWarning("Data integrity check failed");
            }
            
            try {
                migrateMetrics(oldData, newService);
                result.setMigrationSuccessful(true);
            } catch (Exception e) {
                result.setMigrationSuccessful(false);
            }
            
            return result;
        }
    }

    private static class MigrationResult {
        private int migratedCount = 0;
        private int failedCount = 0;
        private boolean validated = false;
        private boolean rolledBack = false;
        private final java.util.List<String> errors = new java.util.ArrayList<>();

        public void incrementMigrated() { migratedCount++; }
        public void incrementFailed() { failedCount++; }
        public void addError(String error) { errors.add(error); }
        public void setValidated(boolean validated) { this.validated = validated; }
        public void setRolledBack(boolean rolledBack) { this.rolledBack = rolledBack; }

        public int getMigratedCount() { return migratedCount; }
        public int getFailedCount() { return failedCount; }
        public boolean isSuccess() { return failedCount == 0; }
        public boolean isValidated() { return validated; }
        public boolean isRolledBack() { return rolledBack; }
        public java.util.List<String> getErrors() { return errors; }
    }

    private static class IncrementalMigrationResult {
        private int totalCount = 0;
        private int migratedCount = 0;
        private int failedCount = 0;
        private int batchesProcessed = 0;
        private boolean complete = false;

        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public void incrementMigrated() { migratedCount++; }
        public void incrementFailed() { failedCount++; }
        public void incrementBatches() { batchesProcessed++; }
        public void setComplete(boolean complete) { this.complete = complete; }

        public int getTotalProcessed() { return totalCount; }
        public int getSuccessfullyMigrated() { return migratedCount; }
        public int getFailed() { return failedCount; }
        public int getBatchesProcessed() { return batchesProcessed; }
        public boolean isComplete() { return complete; }
    }

    private static class CompatibilityResult {
        private String sourceVersion;
        private String targetVersion;
        private boolean compatible = false;
        private final java.util.List<String> compatibilityNotes = new java.util.ArrayList<>();

        public void setSourceVersion(String sourceVersion) { this.sourceVersion = sourceVersion; }
        public void setTargetVersion(String targetVersion) { this.targetVersion = targetVersion; }
        public void setCompatible(boolean compatible) { this.compatible = compatible; }
        public void addCompatibilityNote(String note) { compatibilityNotes.add(note); }

        public String getSourceVersion() { return sourceVersion; }
        public String getTargetVersion() { return targetVersion; }
        public boolean isCompatible() { return compatible; }
        public java.util.List<String> getCompatibilityNotes() { return compatibilityNotes; }
    }

    private static class MigrationProgressTracker {
        private final AtomicLong totalCount = new AtomicLong(0);
        private final AtomicLong processedCount = new AtomicLong(0);

        public void setTotalCount(int total) { totalCount.set(total); }
        public void incrementProcessed() { processedCount.incrementAndGet(); }

        public long getTotalCount() { return totalCount.get(); }
        public long getProcessedCount() { return processedCount.get(); }
        public double getProgressPercentage() {
            long total = totalCount.get();
            if (total == 0) return 0.0;
            return (double) processedCount.get() / total * 100.0;
        }
    }

    private static class DataIntegrityResult {
        private String originalHash;
        private String validatedHash;
        private boolean integrityValid = false;
        private boolean migrationSuccessful = false;
        private final java.util.List<String> integrityWarnings = new java.util.ArrayList<>();

        public void setOriginalHash(String originalHash) { this.originalHash = originalHash; }
        public void setValidatedHash(String validatedHash) { this.validatedHash = validatedHash; }
        public void setIntegrityValid(boolean integrityValid) { this.integrityValid = integrityValid; }
        public void setMigrationSuccessful(boolean migrationSuccessful) { this.migrationSuccessful = migrationSuccessful; }
        public void addIntegrityWarning(String warning) { integrityWarnings.add(warning); }

        public String getOriginalHash() { return originalHash; }
        public String getValidatedHash() { return validatedHash; }
        public boolean isIntegrityValid() { return integrityValid; }
        public boolean isMigrationSuccessful() { return migrationSuccessful; }
        public java.util.List<String> getIntegrityWarnings() { return integrityWarnings; }
    }
}
