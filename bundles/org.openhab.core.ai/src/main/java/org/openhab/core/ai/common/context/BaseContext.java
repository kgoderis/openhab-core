package org.openhab.core.ai.common.context;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class implementing the Context interface.
 * 
 * <p>
 * This class provides common functionality for all context implementations:
 * - Immutable context data with defensive copying
 * - Common validation and utility methods
 * - Standard equals/hashCode/toString implementations
 * - Type-safe value retrieval
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class BaseContext implements Context {

    private final String contextId;
    private final String contextType;
    private final Instant createdAt;
    private final Instant lastModifiedAt;
    private final String version;
    private final Map<String, Object> values;
    private final Map<String, Object> metadata;

    /**
     * Create a new base context.
     * 
     * @param contextId the unique context identifier
     * @param contextType the context type/category
     * @param version the context version
     * @param values the context values (will be copied)
     * @param metadata the context metadata (will be copied)
     */
    protected BaseContext(String contextId, String contextType, String version, @Nullable Map<String, Object> values,
            @Nullable Map<String, Object> metadata) {
        this.contextId = Objects.requireNonNull(contextId, "Context ID cannot be null");
        this.contextType = Objects.requireNonNull(contextType, "Context type cannot be null");
        this.version = Objects.requireNonNull(version, "Context version cannot be null");
        this.createdAt = Instant.now();
        this.lastModifiedAt = Instant.now();
        this.values = values != null ? Map.copyOf(values) : Map.of();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    /**
     * Create a new base context with custom timestamps.
     * 
     * @param contextId the unique context identifier
     * @param contextType the context type/category
     * @param version the context version
     * @param values the context values (will be copied)
     * @param metadata the context metadata (will be copied)
     * @param createdAt the creation timestamp
     * @param lastModifiedAt the last modification timestamp
     */
    protected BaseContext(String contextId, String contextType, String version, @Nullable Map<String, Object> values,
            @Nullable Map<String, Object> metadata, Instant createdAt, Instant lastModifiedAt) {
        this.contextId = Objects.requireNonNull(contextId, "Context ID cannot be null");
        this.contextType = Objects.requireNonNull(contextType, "Context type cannot be null");
        this.version = Objects.requireNonNull(version, "Context version cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Creation timestamp cannot be null");
        this.lastModifiedAt = Objects.requireNonNull(lastModifiedAt, "Last modified timestamp cannot be null");
        this.values = values != null ? Map.copyOf(values) : Map.of();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    @Override
    public String getContextId() {
        return contextId;
    }

    @Override
    public String getContextType() {
        return contextType;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public @Nullable Object getValue(String key) {
        return values.get(key);
    }

    @Override
    public <T> @Nullable T getValue(String key, Class<T> type) {
        Object value = values.get(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    @Override
    public boolean hasValue(String key) {
        return values.containsKey(key);
    }

    @Override
    public Map<String, Object> getAllValues() {
        return values;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public @Nullable Object getMetadata(String key) {
        return metadata.get(key);
    }

    @Override
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Set a value in the context. Since contexts are immutable, this returns a new context instance.
     * 
     * @param key the key to set
     * @param value the value to set
     * @return a new context instance with the updated value
     */
    public BaseContext setValue(String key, @Nullable Object value) {
        Map<String, Object> newValues = new HashMap<>(values);
        if (value == null) {
            newValues.remove(key);
        } else {
            newValues.put(key, value);
        }
        return createCopy(newValues, metadata);
    }

    /**
     * Create a copy of this context with new values and metadata.
     * Subclasses should override this to return the correct type.
     * 
     * @param newValues the new values map
     * @param newMetadata the new metadata map
     * @return a new context instance
     */
    protected abstract BaseContext createCopy(Map<String, Object> newValues, Map<String, Object> newMetadata);

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseContext other = (BaseContext) obj;
        return Objects.equals(contextId, other.contextId) && Objects.equals(contextType, other.contextType)
                && Objects.equals(version, other.version) && Objects.equals(values, other.values)
                && Objects.equals(metadata, other.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextId, contextType, version, values, metadata);
    }

    @Override
    public String toString() {
        return String.format("BaseContext{id='%s', type='%s', version='%s', size=%d, metadataSize=%d}", contextId,
                contextType, version, values.size(), metadata.size());
    }
}
