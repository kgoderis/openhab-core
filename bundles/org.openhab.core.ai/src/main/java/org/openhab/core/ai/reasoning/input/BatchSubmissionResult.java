package org.openhab.core.ai.reasoning.input;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result for batch input submission processing.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class BatchSubmissionResult {
    private final boolean success;
    private final List<String> processedIds;
    private final List<String> failedIds;
    private final int totalCount;
    private final @Nullable String error;

    private BatchSubmissionResult(boolean success, List<String> processedIds, List<String> failedIds, int totalCount,
            @Nullable String error) {
        this.success = success;
        this.processedIds = processedIds;
        this.failedIds = failedIds;
        this.totalCount = totalCount;
        this.error = error;
    }

    public static BatchSubmissionResult success(List<String> processedIds, List<String> failedIds, int totalCount) {
        return new BatchSubmissionResult(true, processedIds, failedIds, totalCount, null);
    }

    public static BatchSubmissionResult error(String reason) {
        return new BatchSubmissionResult(false, new ArrayList<>(), new ArrayList<>(), 0, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<String> getProcessedIds() {
        return processedIds;
    }

    public List<String> getFailedIds() {
        return failedIds;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public @Nullable String getError() {
        return error;
    }
}
