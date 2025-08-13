package org.openhab.core.ai.agent.collaboration.context;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link SharedContext}.
 *
 * Provides a fluent API to construct immutable {@link SharedContext} instances.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SharedContextBuilder {

    private String contextId;
    private ContextVersion currentVersion;
    private String createdBy;
    private Instant createdAt;
    private String lastModifiedBy;
    private Instant lastModifiedAt;
    private ContextOptions options;

    public SharedContextBuilder contextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    public SharedContextBuilder currentVersion(ContextVersion currentVersion) {
        this.currentVersion = currentVersion;
        return this;
    }

    public SharedContextBuilder createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public SharedContextBuilder createdAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public SharedContextBuilder lastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    public SharedContextBuilder lastModifiedAt(Instant lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
        return this;
    }

    public SharedContextBuilder options(ContextOptions options) {
        this.options = options;
        return this;
    }

    public SharedContext build() {
        return new SharedContext(this);
    }

    String getContextId() {
        return contextId;
    }

    ContextVersion getCurrentVersion() {
        return currentVersion;
    }

    String getCreatedBy() {
        return createdBy;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    String getLastModifiedBy() {
        return lastModifiedBy;
    }

    Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    ContextOptions getOptions() {
        return options;
    }
}


