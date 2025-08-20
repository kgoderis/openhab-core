package org.openhab.core.ai.tool.sampling.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.sampling.SamplingStatus;
import org.openhab.core.ai.tool.sampling.SamplingRequest;

/**
 * Concrete implementation of SamplingRequest interface.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultSamplingRequest implements SamplingRequest {

    private final String id;
    private final String modelName;
    private final String message;
    private final boolean includeContext;
    private SamplingStatus status;
    private String rejectionReason;
    private final long createdAt;

    public DefaultSamplingRequest(String id, String modelName, String message, boolean includeContext,
            SamplingStatus status) {
        this.id = id;
        this.modelName = modelName;
        this.message = message;
        this.includeContext = includeContext;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isIncludeContext() {
        return includeContext;
    }

    @Override
    public SamplingStatus getStatus() {
        return status;
    }

    public void setStatus(SamplingStatus status) {
        this.status = status;
    }

    @Override
    public String getRejectionReason() {
        return rejectionReason;
    }

    @Override
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "SamplingRequest{id='" + id + "', modelName='" + modelName + "', status=" + status + "}";
    }
}
