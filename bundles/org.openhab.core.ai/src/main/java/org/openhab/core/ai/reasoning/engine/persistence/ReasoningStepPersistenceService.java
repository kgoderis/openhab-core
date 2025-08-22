package org.openhab.core.ai.reasoning.engine.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStep;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepStatus;
import org.openhab.core.ai.reasoning.engine.api.ReasoningStepType;
import org.openhab.core.ai.reasoning.engine.monitoring.ReasoningStepStorageStatistics;

/**
 * Service for persisting and retrieving reasoning steps.
 * 
 * <p>
 * This service provides comprehensive persistence capabilities for reasoning steps including:
 * - Step storage and retrieval
 * - Step indexing and search
 * - Step cleanup and archival
 * - Step backup and recovery
 * - Step compression for long-term storage
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ReasoningStepPersistenceService {

    /**
     * Store a reasoning step.
     * 
     * @param step the reasoning step to store
     * @return true if the step was successfully stored
     */
    boolean storeStep(ReasoningStep step);

    /**
     * Store multiple reasoning steps in a batch operation.
     * 
     * @param steps the reasoning steps to store
     * @return the number of steps successfully stored
     */
    int storeSteps(List<ReasoningStep> steps);

    /**
     * Retrieve a reasoning step by its session ID and step number.
     * 
     * @param sessionId the session ID
     * @param stepNumber the step number
     * @return the reasoning step, or empty if not found
     */
    Optional<ReasoningStep> getStep(String sessionId, int stepNumber);

    /**
     * Retrieve all steps for a session.
     * 
     * @param sessionId the session ID
     * @return list of reasoning steps for the session
     */
    List<ReasoningStep> getStepsForSession(String sessionId);

    /**
     * Search for reasoning steps based on criteria.
     * 
     * @param criteria the search criteria
     * @return list of matching reasoning steps
     */
    List<ReasoningStep> searchSteps(ReasoningStepSearchCriteria criteria);

    /**
     * Get reasoning steps by status.
     * 
     * @param status the step status
     * @param limit maximum number of steps to return
     * @return list of reasoning steps with the specified status
     */
    List<ReasoningStep> getStepsByStatus(ReasoningStepStatus status, int limit);

    /**
     * Get reasoning steps by type.
     * 
     * @param stepType the step type
     * @param limit maximum number of steps to return
     * @return list of reasoning steps with the specified type
     */
    List<ReasoningStep> getStepsByType(ReasoningStepType stepType, int limit);

    /**
     * Get reasoning steps within a time range.
     * 
     * @param startTime the start time (inclusive)
     * @param endTime the end time (inclusive)
     * @param limit maximum number of steps to return
     * @return list of reasoning steps within the time range
     */
    List<ReasoningStep> getStepsInTimeRange(Instant startTime, Instant endTime, int limit);

    /**
     * Get reasoning steps by model ID.
     * 
     * @param modelId the model ID
     * @param limit maximum number of steps to return
     * @return list of reasoning steps using the specified model
     */
    List<ReasoningStep> getStepsByModel(String modelId, int limit);

    /**
     * Get reasoning steps with high resource usage.
     * 
     * @param minTokens minimum token count
     * @param limit maximum number of steps to return
     * @return list of reasoning steps with high resource usage
     */
    List<ReasoningStep> getStepsWithHighResourceUsage(long minTokens, int limit);

    /**
     * Get reasoning steps with low quality scores.
     * 
     * @param maxQualityScore maximum quality score
     * @param limit maximum number of steps to return
     * @return list of reasoning steps with low quality scores
     */
    List<ReasoningStep> getStepsWithLowQuality(double maxQualityScore, int limit);

    /**
     * Update a reasoning step.
     * 
     * @param step the updated reasoning step
     * @return true if the step was successfully updated
     */
    boolean updateStep(ReasoningStep step);

    /**
     * Delete a reasoning step.
     * 
     * @param sessionId the session ID
     * @param stepNumber the step number
     * @return true if the step was successfully deleted
     */
    boolean deleteStep(String sessionId, int stepNumber);

    /**
     * Delete all steps for a session.
     * 
     * @param sessionId the session ID
     * @return the number of steps deleted
     */
    int deleteStepsForSession(String sessionId);

    /**
     * Clean up old reasoning steps.
     * 
     * @param cutoffTime steps older than this time will be cleaned up
     * @return the number of steps cleaned up
     */
    int cleanupOldSteps(Instant cutoffTime);

    /**
     * Archive reasoning steps to long-term storage.
     * 
     * @param cutoffTime steps older than this time will be archived
     * @return the number of steps archived
     */
    int archiveSteps(Instant cutoffTime);

    /**
     * Restore reasoning steps from archive.
     * 
     * @param sessionId the session ID to restore
     * @return the number of steps restored
     */
    int restoreStepsFromArchive(String sessionId);

    /**
     * Get storage statistics.
     * 
     * @return storage statistics
     */
    ReasoningStepStorageStatistics getStorageStatistics();

    /**
     * Create a backup of all reasoning steps.
     * 
     * @param backupPath the path where the backup should be created
     * @return true if the backup was successfully created
     */
    boolean createBackup(String backupPath);

    /**
     * Restore reasoning steps from a backup.
     * 
     * @param backupPath the path to the backup file
     * @return true if the restore was successful
     */
    boolean restoreFromBackup(String backupPath);

    /**
     * Compress reasoning steps for long-term storage.
     * 
     * @param sessionId the session ID to compress
     * @return true if the compression was successful
     */
    boolean compressSteps(String sessionId);

    /**
     * Decompress reasoning steps.
     * 
     * @param sessionId the session ID to decompress
     * @return true if the decompression was successful
     */
    boolean decompressSteps(String sessionId);

    /**
     * Get the total number of stored reasoning steps.
     * 
     * @return the total number of steps
     */
    long getTotalStepCount();

    /**
     * Get the total storage size in bytes.
     * 
     * @return the total storage size
     */
    long getTotalStorageSize();

    /**
     * Check if the persistence service is healthy.
     * 
     * @return true if the service is healthy
     */
    boolean isHealthy();

    /**
     * Perform maintenance operations.
     * 
     * @return true if maintenance was successful
     */
    boolean performMaintenance();
}
