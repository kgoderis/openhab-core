package org.openhab.core.ai.agent.collaboration.context;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Shared context data holder.
 *
 * Encapsulates the current state of a shared context, including versioning and audit metadata.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SharedContext {

    private final String contextId;
    private final ContextVersion currentVersion;
    private final String createdBy;
    private final Instant createdAt;
    private final String lastModifiedBy;
    private final Instant lastModifiedAt;
    private final ContextOptions options;

    SharedContext(SharedContextBuilder builder) {
        this.contextId = builder.getContextId();
        this.currentVersion = builder.getCurrentVersion();
        this.createdBy = builder.getCreatedBy();
        this.createdAt = builder.getCreatedAt();
        this.lastModifiedBy = builder.getLastModifiedBy();
        this.lastModifiedAt = builder.getLastModifiedAt();
        this.options = builder.getOptions();
    }

    public String getContextId() {
        return contextId;
    }

    public ContextVersion getCurrentVersion() {
        return currentVersion;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public ContextOptions getOptions() {
        return options;
    }

    public long getVersion() {
        return currentVersion.getVersionId().hashCode();
    }

    public static SharedContextBuilder builder() {
        return new SharedContextBuilder();
    }
}
