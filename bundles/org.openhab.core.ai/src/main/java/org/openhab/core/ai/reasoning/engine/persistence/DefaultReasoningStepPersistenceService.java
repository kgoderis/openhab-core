package org.openhab.core.ai.reasoning.engine.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.statistics.ReasoningPerformanceStatistics;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStep;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepStatus;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * File-based implementation of ReasoningStepPersistenceService.
 * 
 * <p>
 * This implementation provides persistent storage of reasoning steps using JSON files
 * with optional compression for long-term storage.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ReasoningStepPersistenceService.class)
@NonNullByDefault
public class DefaultReasoningStepPersistenceService implements ReasoningStepPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultReasoningStepPersistenceService.class);

    private final ObjectMapper objectMapper;
    private final Path storageBasePath;
    private final Path activeStoragePath;
    private final Path archivedStoragePath;
    private final Path compressedStoragePath;
    private final Path backupStoragePath;
    private final Path indexFilePath;

    // In-memory cache for performance
    private final ConcurrentHashMap<String, ReasoningStep> stepCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<ReasoningStep>> sessionCache = new ConcurrentHashMap<>();

    // Metrics service for centralized metrics collection
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private @Nullable MetricsService metricsService;

    // Index for efficient searching
    private final Map<String, StepIndexEntry> stepIndex = new ConcurrentHashMap<>();

    @Activate
    public DefaultReasoningStepPersistenceService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        // Initialize storage paths
        this.storageBasePath = Paths.get(System.getProperty("user.home"), ".openhab", "ai", "reasoning-steps");
        this.activeStoragePath = storageBasePath.resolve("active");
        this.archivedStoragePath = storageBasePath.resolve("archived");
        this.compressedStoragePath = storageBasePath.resolve("compressed");
        this.backupStoragePath = storageBasePath.resolve("backup");
        this.indexFilePath = storageBasePath.resolve("index.json");

        try {
            initializeStorage();
            loadIndex();
            logger.info("ReasoningStepPersistenceService initialized with storage at: {}", storageBasePath);
        } catch (IOException e) {
            logger.error("Failed to initialize ReasoningStepPersistenceService", e);
        }
    }

    @Modified
    protected void modified() {
        logger.debug("ReasoningStepPersistenceService configuration modified");
    }

    @Deactivate
    public void deactivate() {
        saveIndex();
        logger.info("ReasoningStepPersistenceService deactivated");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.debug("MetricsService set for ReasoningStepPersistenceService");
    }

    protected void unsetMetricsService(MetricsService metricsService) {
        this.metricsService = null;
        logger.debug("MetricsService unset for ReasoningStepPersistenceService");
    }

    /**
     * Record metrics for reasoning step storage operations
     */
    private void recordMetrics(String operation, boolean success, long durationNanos) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("reasoning-storage", operation, success, Duration.ofNanos(durationNanos));
            } catch (Exception e) {
                logger.debug("Failed to record metrics for {}.{}: {}", "reasoning-storage", operation, e.getMessage());
            }
        } else {
            logger.debug("MetricsService not available, cannot record metrics for operation: {}", operation);
        }
    }

    @Override
    public boolean storeStep(ReasoningStep step) {
        try {
            String stepKey = generateStepKey(step.getSessionId(), step.getStepNumber());
            Path stepFilePath = getStepFilePath(step.getSessionId(), step.getStepNumber());

            // Create directory if it doesn't exist
            Files.createDirectories(stepFilePath.getParent());

            // Serialize and write step
            String stepJson = objectMapper.writeValueAsString(step);
            Files.write(stepFilePath, stepJson.getBytes());

            // Update cache
            stepCache.put(stepKey, step);
            sessionCache.computeIfAbsent(step.getSessionId(), k -> new ArrayList<>()).add(step);

            // Update index
            updateStepIndex(step);

            // Update statistics
            updateStatistics(step, true);

            logger.debug("Stored reasoning step: {} -> {}", stepKey, stepFilePath);
            return true;

        } catch (Exception e) {
            logger.error("Failed to store reasoning step: {}", step, e);
            return false;
        }
    }

    @Override
    public int storeSteps(List<ReasoningStep> steps) {
        int successCount = 0;
        for (ReasoningStep step : steps) {
            if (storeStep(step)) {
                successCount++;
            }
        }
        logger.debug("Stored {}/{} reasoning steps", successCount, steps.size());
        return successCount;
    }

    @Override
    public Optional<ReasoningStep> getStep(String sessionId, int stepNumber) {
        try {
            String stepKey = generateStepKey(sessionId, stepNumber);

            // Check cache first
            ReasoningStep cachedStep = stepCache.get(stepKey);
            if (cachedStep != null) {
                return Optional.of(cachedStep);
            }

            // Load from file
            Path stepFilePath = getStepFilePath(sessionId, stepNumber);
            if (!Files.exists(stepFilePath)) {
                return Optional.empty();
            }

            String stepJson = Files.readString(stepFilePath);
            ReasoningStep step = objectMapper.readValue(stepJson, ReasoningStep.class);

            // Update cache
            stepCache.put(stepKey, step);

            return Optional.of(step);

        } catch (Exception e) {
            logger.error("Failed to retrieve reasoning step: {}:{}", sessionId, stepNumber, e);
            return Optional.empty();
        }
    }

    @Override
    public List<ReasoningStep> getStepsForSession(String sessionId) {
        try {
            // Check cache first
            List<ReasoningStep> cachedSteps = sessionCache.get(sessionId);
            if (cachedSteps != null) {
                return new ArrayList<>(cachedSteps);
            }

            // Load from files
            Path sessionDir = activeStoragePath.resolve(sessionId);
            if (!Files.exists(sessionDir)) {
                return List.of();
            }

            List<ReasoningStep> steps = new ArrayList<>();
            Files.list(sessionDir).filter(path -> path.toString().endsWith(".json")).sorted().forEach(stepFile -> {
                try {
                    String stepJson = Files.readString(stepFile);
                    ReasoningStep step = objectMapper.readValue(stepJson, ReasoningStep.class);
                    steps.add(step);
                } catch (Exception e) {
                    logger.error("Failed to load step from file: {}", stepFile, e);
                }
            });

            // Update cache
            sessionCache.put(sessionId, new ArrayList<>(steps));

            return steps;

        } catch (Exception e) {
            logger.error("Failed to retrieve steps for session: {}", sessionId, e);
            return List.of();
        }
    }

    @Override
    public List<ReasoningStep> searchSteps(ReasoningStepSearchCriteria criteria) {
        try {
            List<ReasoningStep> results = new ArrayList<>();
            int count = 0;

            // Use index for efficient searching
            for (StepIndexEntry indexEntry : stepIndex.values()) {
                if (count >= criteria.getLimit()) {
                    break;
                }

                if (matchesCriteria(indexEntry, criteria)) {
                    Optional<ReasoningStep> step = getStep(indexEntry.getSessionId(), indexEntry.getStepNumber());
                    if (step.isPresent()) {
                        results.add(step.get());
                        count++;
                    }
                }
            }

            return results;

        } catch (Exception e) {
            logger.error("Failed to search reasoning steps with criteria: {}", criteria, e);
            return List.of();
        }
    }

    @Override
    public List<ReasoningStep> getStepsByStatus(ReasoningStepStatus status, int limit) {
        ReasoningStepSearchCriteria criteria = ReasoningStepSearchCriteria.builder().withStatus(status).withLimit(limit)
                .build();
        return searchSteps(criteria);
    }

    @Override
    public List<ReasoningStep> getStepsByType(ReasoningStepType stepType, int limit) {
        ReasoningStepSearchCriteria criteria = ReasoningStepSearchCriteria.builder().withStepType(stepType)
                .withLimit(limit).build();
        return searchSteps(criteria);
    }

    @Override
    public List<ReasoningStep> getStepsInTimeRange(Instant startTime, Instant endTime, int limit) {
        ReasoningStepSearchCriteria criteria = ReasoningStepSearchCriteria.builder().withStartTime(startTime)
                .withEndTime(endTime).withLimit(limit).build();
        return searchSteps(criteria);
    }

    @Override
    public List<ReasoningStep> getStepsByModel(String modelId, int limit) {
        ReasoningStepSearchCriteria criteria = ReasoningStepSearchCriteria.builder().withModelId(modelId)
                .withLimit(limit).build();
        return searchSteps(criteria);
    }

    @Override
    public List<ReasoningStep> getStepsWithHighResourceUsage(long minTokens, int limit) {
        // This would require more sophisticated indexing or scanning
        // For now, implement a simple scan approach
        List<ReasoningStep> results = new ArrayList<>();
        int count = 0;

        for (StepIndexEntry indexEntry : stepIndex.values()) {
            if (count >= limit) {
                break;
            }

            if (indexEntry.getTotalTokens() >= minTokens) {
                Optional<ReasoningStep> step = getStep(indexEntry.getSessionId(), indexEntry.getStepNumber());
                if (step.isPresent()) {
                    results.add(step.get());
                    count++;
                }
            }
        }

        return results;
    }

    @Override
    public List<ReasoningStep> getStepsWithLowQuality(double maxQualityScore, int limit) {
        ReasoningStepSearchCriteria criteria = ReasoningStepSearchCriteria.builder()
                .withMaxQualityScore(maxQualityScore).withLimit(limit).build();
        return searchSteps(criteria);
    }

    @Override
    public boolean updateStep(ReasoningStep step) {
        return storeStep(step); // Overwrite existing step
    }

    @Override
    public boolean deleteStep(String sessionId, int stepNumber) {
        try {
            String stepKey = generateStepKey(sessionId, stepNumber);
            Path stepFilePath = getStepFilePath(sessionId, stepNumber);

            if (Files.exists(stepFilePath)) {
                Files.delete(stepFilePath);
            }

            // Remove from cache
            stepCache.remove(stepKey);
            sessionCache.computeIfPresent(sessionId, (k, v) -> {
                v.removeIf(s -> s.getStepNumber() == stepNumber);
                return v.isEmpty() ? null : v;
            });

            // Remove from index
            stepIndex.remove(stepKey);

            // Update statistics
            updateStatistics(null, false);

            logger.debug("Deleted reasoning step: {}", stepKey);
            return true;

        } catch (Exception e) {
            logger.error("Failed to delete reasoning step: {}:{}", sessionId, stepNumber, e);
            return false;
        }
    }

    @Override
    public int deleteStepsForSession(String sessionId) {
        try {
            List<ReasoningStep> steps = getStepsForSession(sessionId);
            int deletedCount = 0;

            for (ReasoningStep step : steps) {
                if (deleteStep(sessionId, step.getStepNumber())) {
                    deletedCount++;
                }
            }

            // Remove session from cache
            sessionCache.remove(sessionId);

            logger.debug("Deleted {} steps for session: {}", deletedCount, sessionId);
            return deletedCount;

        } catch (Exception e) {
            logger.error("Failed to delete steps for session: {}", sessionId, e);
            return 0;
        }
    }

    @Override
    public int cleanupOldSteps(Instant cutoffTime) {
        try {
            int cleanedCount = 0;

            for (StepIndexEntry indexEntry : stepIndex.values()) {
                if (indexEntry.getStartTime().isBefore(cutoffTime)) {
                    if (deleteStep(indexEntry.getSessionId(), indexEntry.getStepNumber())) {
                        cleanedCount++;
                    }
                }
            }

            logger.info("Cleaned up {} old reasoning steps before {}", cleanedCount, cutoffTime);
            return cleanedCount;

        } catch (Exception e) {
            logger.error("Failed to cleanup old steps before: {}", cutoffTime, e);
            return 0;
        }
    }

    @Override
    public int archiveSteps(Instant cutoffTime) {
        try {
            int archivedCount = 0;

            for (StepIndexEntry indexEntry : stepIndex.values()) {
                if (indexEntry.getStartTime().isBefore(cutoffTime)) {
                    Optional<ReasoningStep> step = getStep(indexEntry.getSessionId(), indexEntry.getStepNumber());
                    if (step.isPresent() && archiveStep(step.get())) {
                        archivedCount++;
                    }
                }
            }

            logger.info("Archived {} reasoning steps before {}", archivedCount, cutoffTime);
            return archivedCount;

        } catch (Exception e) {
            logger.error("Failed to archive steps before: {}", cutoffTime, e);
            return 0;
        }
    }

    @Override
    public int restoreStepsFromArchive(String sessionId) {
        try {
            Path archivedSessionDir = archivedStoragePath.resolve(sessionId);
            if (!Files.exists(archivedSessionDir)) {
                return 0;
            }

            final int[] restoredCount = { 0 };
            Files.list(archivedSessionDir).filter(path -> path.toString().endsWith(".json")).forEach(archivedFile -> {
                try {
                    String stepJson = Files.readString(archivedFile);
                    ReasoningStep step = objectMapper.readValue(stepJson, ReasoningStep.class);

                    // Move back to active storage
                    Path activeFile = getStepFilePath(step.getSessionId(), step.getStepNumber());
                    Files.createDirectories(activeFile.getParent());
                    Files.move(archivedFile, activeFile);

                    // Update cache and index
                    String stepKey = generateStepKey(step.getSessionId(), step.getStepNumber());
                    stepCache.put(stepKey, step);
                    sessionCache.computeIfAbsent(step.getSessionId(), k -> new ArrayList<>()).add(step);
                    updateStepIndex(step);

                    restoredCount[0]++;
                } catch (Exception e) {
                    logger.error("Failed to restore step from archive: {}", archivedFile, e);
                }
            });

            logger.info("Restored {} reasoning steps from archive for session: {}", restoredCount[0], sessionId);
            return restoredCount[0];

        } catch (Exception e) {
            logger.error("Failed to restore steps from archive for session: {}", sessionId, e);
            return 0;
        }
    }

    @Override
    public ReasoningPerformanceStatistics getStorageStatistics() {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                return metrics.getReasoningPerformanceStatistics("default", Duration.ofHours(1));
            } catch (Exception e) {
                logger.warn("Error retrieving reasoning performance statistics: {}", e.getMessage());
                // Fallback to empty statistics
                return ReasoningPerformanceStatistics.fromReasoningData(0L, 0L, 0L, 0L, 0L, 0L, 0.0, 0L, 0.0, 0.0, 0.0,
                        0.0, Map.of(), Map.of(), Map.of(), Map.of(), Duration.ofHours(1));
            }
        } else {
            // Fallback to empty statistics when MetricsService is not available
            return ReasoningPerformanceStatistics.fromReasoningData(0L, 0L, 0L, 0L, 0L, 0L, 0.0, 0L, 0.0, 0.0, 0.0, 0.0,
                    Map.of(), Map.of(), Map.of(), Map.of(), Duration.ofHours(1));
        }
    }

    @Override
    public boolean createBackup(String backupPath) {
        try {
            Path backupDir = Paths.get(backupPath);
            Files.createDirectories(backupDir);

            // Copy all active steps
            copyDirectory(activeStoragePath, backupDir.resolve("active"));
            copyDirectory(archivedStoragePath, backupDir.resolve("archived"));
            copyDirectory(compressedStoragePath, backupDir.resolve("compressed"));

            // Save index
            Path backupIndexFile = backupDir.resolve("index.json");
            objectMapper.writeValue(backupIndexFile.toFile(), stepIndex);

            logger.info("Created backup at: {}", backupPath);
            return true;

        } catch (Exception e) {
            logger.error("Failed to create backup at: {}", backupPath, e);
            return false;
        }
    }

    @Override
    public boolean restoreFromBackup(String backupPath) {
        try {
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                logger.error("Backup directory does not exist: {}", backupPath);
                return false;
            }

            // Clear existing data
            clearAllData();

            // Restore from backup
            copyDirectory(backupDir.resolve("active"), activeStoragePath);
            copyDirectory(backupDir.resolve("archived"), archivedStoragePath);
            copyDirectory(backupDir.resolve("compressed"), compressedStoragePath);

            // Load index
            Path backupIndexFile = backupDir.resolve("index.json");
            if (Files.exists(backupIndexFile)) {
                String indexJson = Files.readString(backupIndexFile);
                Map<String, StepIndexEntry> backupIndex = objectMapper.readValue(indexJson,
                        new TypeReference<Map<String, StepIndexEntry>>() {
                        });
                stepIndex.putAll(backupIndex);
            }

            logger.info("Restored from backup: {}", backupPath);
            return true;

        } catch (Exception e) {
            logger.error("Failed to restore from backup: {}", backupPath, e);
            return false;
        }
    }

    @Override
    public boolean compressSteps(String sessionId) {
        try {
            List<ReasoningStep> steps = getStepsForSession(sessionId);
            if (steps.isEmpty()) {
                return false;
            }

            Path compressedFile = compressedStoragePath.resolve(sessionId + ".json.gz");
            Files.createDirectories(compressedFile.getParent());

            try (GZIPOutputStream gzipOut = new GZIPOutputStream(Files.newOutputStream(compressedFile))) {
                String stepsJson = objectMapper.writeValueAsString(steps);
                gzipOut.write(stepsJson.getBytes());
            }

            // Remove original files
            deleteStepsForSession(sessionId);

            // Record metrics via MetricsService instead of using AtomicLong counters
            recordMetrics("steps_compressed", true, 0);
            logger.info("Compressed {} steps for session: {}", steps.size(), sessionId);
            return true;

        } catch (Exception e) {
            logger.error("Failed to compress steps for session: {}", sessionId, e);
            return false;
        }
    }

    @Override
    public boolean decompressSteps(String sessionId) {
        try {
            Path compressedFile = compressedStoragePath.resolve(sessionId + ".json.gz");
            if (!Files.exists(compressedFile)) {
                return false;
            }

            try (GZIPInputStream gzipIn = new GZIPInputStream(Files.newInputStream(compressedFile))) {
                String stepsJson = new String(gzipIn.readAllBytes());
                List<ReasoningStep> steps = objectMapper.readValue(stepsJson, new TypeReference<List<ReasoningStep>>() {
                });

                // Store steps back to active storage
                storeSteps(steps);

                // Remove compressed file
                Files.delete(compressedFile);

                // Record metrics via MetricsService instead of using AtomicLong counters
                recordMetrics("steps_decompressed", true, 0);
                logger.info("Decompressed {} steps for session: {}", steps.size(), sessionId);
                return true;
            }

        } catch (Exception e) {
            logger.error("Failed to decompress steps for session: {}", sessionId, e);
            return false;
        }
    }

    @Override
    public long getTotalStepCount() {
        return 0; // No longer tracked
    }

    @Override
    public long getTotalStorageSize() {
        return 0; // No longer tracked
    }

    @Override
    public boolean isHealthy() {
        try {
            // Check if storage directories are accessible
            return Files.exists(storageBasePath) && Files.isDirectory(storageBasePath)
                    && Files.exists(activeStoragePath) && Files.isDirectory(activeStoragePath);
        } catch (Exception e) {
            logger.error("Health check failed", e);
            return false;
        }
    }

    @Override
    public boolean performMaintenance() {
        try {
            // Rebuild index from files
            rebuildIndex();

            // Clean up cache
            stepCache.clear();
            sessionCache.clear();

            // Save updated index
            saveIndex();

            logger.info("Maintenance completed successfully");
            return true;

        } catch (Exception e) {
            logger.error("Maintenance failed", e);
            return false;
        }
    }

    // Helper methods

    private void initializeStorage() throws IOException {
        Files.createDirectories(activeStoragePath);
        Files.createDirectories(archivedStoragePath);
        Files.createDirectories(compressedStoragePath);
        Files.createDirectories(backupStoragePath);
    }

    private String generateStepKey(String sessionId, int stepNumber) {
        return sessionId + ":" + stepNumber;
    }

    private Path getStepFilePath(String sessionId, int stepNumber) {
        return activeStoragePath.resolve(sessionId).resolve(stepNumber + ".json");
    }

    private void updateStepIndex(ReasoningStep step) {
        String stepKey = generateStepKey(step.getSessionId(), step.getStepNumber());
        StepIndexEntry indexEntry = new StepIndexEntry(step.getSessionId(), step.getStepNumber(), step.getStepType(),
                step.getStatus(), step.getModelId(), step.getStartTime(), step.getEndTime(), step.getConfidence(),
                step.getQualityScore(), step.hasResourceUsage() ? step.getResourceUsage().getTotalTokens() : 0,
                step.hasResourceUsage() ? step.getResourceUsage().costUsd() : 0.0, step.getDurationMs());

        stepIndex.put(stepKey, indexEntry);
    }

    private boolean matchesCriteria(StepIndexEntry indexEntry, ReasoningStepSearchCriteria criteria) {
        if (criteria.getSessionId() != null && !criteria.getSessionId().equals(indexEntry.getSessionId())) {
            return false;
        }
        if (criteria.getStepType() != null && criteria.getStepType() != indexEntry.getStepType()) {
            return false;
        }
        if (criteria.getStatus() != null && criteria.getStatus() != indexEntry.getStatus()) {
            return false;
        }
        if (criteria.getModelId() != null && !criteria.getModelId().equals(indexEntry.getModelId())) {
            return false;
        }
        if (criteria.getStartTime() != null && indexEntry.getStartTime().isBefore(criteria.getStartTime())) {
            return false;
        }
        if (criteria.getEndTime() != null && indexEntry.getStartTime().isAfter(criteria.getEndTime())) {
            return false;
        }
        if (criteria.getMinConfidence() != null && indexEntry.getConfidence() < criteria.getMinConfidence()) {
            return false;
        }
        if (criteria.getMaxConfidence() != null && indexEntry.getConfidence() > criteria.getMaxConfidence()) {
            return false;
        }
        if (criteria.getMinQualityScore() != null && indexEntry.getQualityScore() < criteria.getMinQualityScore()) {
            return false;
        }
        if (criteria.getMaxQualityScore() != null && indexEntry.getQualityScore() > criteria.getMaxQualityScore()) {
            return false;
        }
        if (criteria.getMinTokens() != null && indexEntry.getTotalTokens() < criteria.getMinTokens()) {
            return false;
        }
        if (criteria.getMaxTokens() != null && indexEntry.getTotalTokens() > criteria.getMaxTokens()) {
            return false;
        }
        if (criteria.getMinCost() != null && indexEntry.getCostUsd() < criteria.getMinCost()) {
            return false;
        }
        if (criteria.getMaxCost() != null && indexEntry.getCostUsd() > criteria.getMaxCost()) {
            return false;
        }

        return true;
    }

    private void updateStatistics(@Nullable ReasoningStep step, boolean isAdd) {
        if (isAdd && step != null) {
            // Record metrics via MetricsService instead of using AtomicLong counters
            recordMetrics("step_added", true, 0);
            if (step.hasResourceUsage()) {
                recordMetrics("storage_size_updated", true, 0);
            }
        } else if (!isAdd) {
            // Record metrics via MetricsService instead of using AtomicLong counters
            recordMetrics("step_removed", true, 0);
        }
    }

    private void loadIndex() {
        try {
            if (Files.exists(indexFilePath)) {
                String indexJson = Files.readString(indexFilePath);
                Map<String, StepIndexEntry> loadedIndex = objectMapper.readValue(indexJson,
                        new TypeReference<Map<String, StepIndexEntry>>() {
                        });
                stepIndex.putAll(loadedIndex);
                logger.debug("Loaded {} index entries", loadedIndex.size());
            }
        } catch (Exception e) {
            logger.warn("Failed to load index, will rebuild", e);
            rebuildIndex();
        }
    }

    private void saveIndex() {
        try {
            String indexJson = objectMapper.writeValueAsString(stepIndex);
            Files.write(indexFilePath, indexJson.getBytes());
            logger.debug("Saved {} index entries", stepIndex.size());
        } catch (Exception e) {
            logger.error("Failed to save index", e);
        }
    }

    private void rebuildIndex() {
        stepIndex.clear();
        try {
            Files.walk(activeStoragePath).filter(path -> path.toString().endsWith(".json")).forEach(stepFile -> {
                try {
                    String stepJson = Files.readString(stepFile);
                    ReasoningStep step = objectMapper.readValue(stepJson, ReasoningStep.class);
                    updateStepIndex(step);
                } catch (Exception e) {
                    logger.error("Failed to rebuild index for file: {}", stepFile, e);
                }
            });
            logger.info("Rebuilt index with {} entries", stepIndex.size());
        } catch (Exception e) {
            logger.error("Failed to rebuild index", e);
        }
    }

    private boolean archiveStep(ReasoningStep step) {
        try {
            Path activeFile = getStepFilePath(step.getSessionId(), step.getStepNumber());
            Path archivedFile = archivedStoragePath.resolve(step.getSessionId())
                    .resolve(step.getStepNumber() + ".json");

            if (Files.exists(activeFile)) {
                Files.createDirectories(archivedFile.getParent());
                Files.move(activeFile, archivedFile);

                // Record metrics via MetricsService instead of using AtomicLong counters
                recordMetrics("step_archived", true, 0);

                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to archive step: {}:{}", step.getSessionId(), step.getStepNumber(), e);
            return false;
        }
    }

    private void clearAllData() {
        stepCache.clear();
        sessionCache.clear();
        stepIndex.clear();
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        if (!Files.exists(source)) {
            return;
        }

        Files.createDirectories(target);
        Files.walk(source).filter(path -> !Files.isDirectory(path)).forEach(sourceFile -> {
            try {
                Path targetFile = target.resolve(source.relativize(sourceFile));
                Files.createDirectories(targetFile.getParent());
                Files.copy(sourceFile, targetFile);
            } catch (IOException e) {
                logger.error("Failed to copy file: {} -> {}", sourceFile, target, e);
            }
        });
    }

    /**
     * Index entry for efficient searching.
     */
    private static final class StepIndexEntry {
        private final String sessionId;
        private final int stepNumber;
        private final ReasoningStepType stepType;
        private final ReasoningStepStatus status;
        private final @Nullable String modelId;
        private final Instant startTime;
        private final Instant endTime;
        private final double confidence;
        private final double qualityScore;
        private final long totalTokens;
        private final double costUsd;
        private final long processingTimeMs;

        public StepIndexEntry(String sessionId, int stepNumber, ReasoningStepType stepType, ReasoningStepStatus status,
                @Nullable String modelId, Instant startTime, Instant endTime, double confidence, double qualityScore,
                long totalTokens, double costUsd, long processingTimeMs) {
            this.sessionId = sessionId;
            this.stepNumber = stepNumber;
            this.stepType = stepType;
            this.status = status;
            this.modelId = modelId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.confidence = confidence;
            this.qualityScore = qualityScore;
            this.totalTokens = totalTokens;
            this.costUsd = costUsd;
            this.processingTimeMs = processingTimeMs;
        }

        // Getters
        public String getSessionId() {
            return sessionId;
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public ReasoningStepType getStepType() {
            return stepType;
        }

        public ReasoningStepStatus getStatus() {
            return status;
        }

        public @Nullable String getModelId() {
            return modelId;
        }

        public Instant getStartTime() {
            return startTime;
        }

        public Instant getEndTime() {
            return endTime;
        }

        public double getConfidence() {
            return confidence;
        }

        public double getQualityScore() {
            return qualityScore;
        }

        public long getTotalTokens() {
            return totalTokens;
        }

        public double getCostUsd() {
            return costUsd;
        }

        public long getProcessingTimeMs() {
            return processingTimeMs;
        }
    }
}
