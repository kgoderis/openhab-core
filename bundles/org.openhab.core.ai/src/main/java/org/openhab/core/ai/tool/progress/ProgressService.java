package org.openhab.core.ai.tool.progress;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.progress.tracking.ProgressOperation;
import org.openhab.core.ai.tool.progress.tracking.ProgressStatus;

/**
 * Interface for MCP Progress Tracking service.
 * 
 * This service provides long-running operation progress tracking for MCP operations,
 * including tool execution progress, resource loading, and system operations.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ProgressService {

    /**
     * Begin a progress operation.
     * 
     * @param operationId the operation ID
     * @param description the operation description
     * @param totalSteps the total number of steps
     * @return true if operation was begun successfully
     */
    boolean beginOperation(String operationId, String description, int totalSteps);

    /**
     * Report progress for an operation.
     * 
     * @param operationId the operation ID
     * @param currentStep the current step number
     * @param message the progress message
     * @return true if progress was reported successfully
     */
    boolean reportProgress(String operationId, int currentStep, String message);

    /**
     * End a progress operation.
     * 
     * @param operationId the operation ID
     * @param status the final status
     * @param finalMessage the final message
     * @return true if operation was ended successfully
     */
    boolean endOperation(String operationId, ProgressStatus status, String finalMessage);

    /**
     * Get a progress operation by ID.
     * 
     * @param operationId the operation ID
     * @return the progress operation or null if not found
     */
    @Nullable
    ProgressOperation getOperation(String operationId);

    /**
     * Get all active progress operations.
     * 
     * @return map of all active operations
     */
    Map<String, ProgressOperation> getAllActiveOperations();

    /**
     * Get the count of active operations.
     * 
     * @return active operation count
     */
    int getActiveOperationCount();

    /**
     * Get the total steps for an operation.
     * 
     * @param operationId the operation ID
     * @return total steps or 0 if operation not found
     */
    int getTotalSteps(String operationId);

    /**
     * Get performance metrics.
     * 
     * @return performance metrics as a map
     */
    Map<String, Object> getPerformanceMetrics();
}
