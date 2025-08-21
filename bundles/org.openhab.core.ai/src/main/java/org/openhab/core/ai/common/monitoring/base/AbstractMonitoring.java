package org.openhab.core.ai.common.monitoring.base;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Monitoring;
import org.openhab.core.ai.common.monitoring.api.MonitoringType;
import org.openhab.core.ai.common.monitoring.api.ValidationUtils;

/**
 * Abstract base class for all monitoring data implementations.
 * 
 * <p>
 * This class provides common functionality and implementation patterns for
 * all monitoring data types, reducing code duplication and ensuring
 * consistent behavior across different monitoring domains.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class AbstractMonitoring implements Monitoring {

    private final String id;
    private final Instant timestamp;
    private final String domain;
    private final @Nullable String source;
    private final @Nullable String description;
    private final Map<String, Object> data;

    /**
     * Create a new AbstractMonitoring instance.
     * 
     * @param id the unique identifier
     * @param timestamp the timestamp when data was created
     * @param domain the domain this data belongs to
     * @param source the source component, or null if not specified
     * @param description the description, or null if not available
     * @param data the raw data map, or null if no raw data
     */
    protected AbstractMonitoring(String id, Instant timestamp, String domain, @Nullable String source,
            @Nullable String description, @Nullable Map<String, Object> data) {
        this.id = Objects.requireNonNull(id, "id");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.domain = Objects.requireNonNull(domain, "domain");
        this.source = source;
        this.description = description;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public @Nullable String getSource() {
        return source;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public @Nullable Map<String, Object> getData() {
        return data.isEmpty() ? null : Map.copyOf(data);
    }

    @Override
    public MonitoringType getType() {
        // This should be implemented by subclasses
        throw new UnsupportedOperationException("getType() must be implemented by subclasses");
    }

    /**
     * Get the raw data map for internal use (mutable).
     * 
     * @return the raw data map
     */
    protected Map<String, Object> getRawData() {
        return data;
    }

    /**
     * Add a data entry to the raw data map.
     * 
     * @param key the data key
     * @param value the data value
     */
    protected void addData(String key, Object value) {
        data.put(Objects.requireNonNull(key, "key"), value);
    }

    /**
     * Remove a data entry from the raw data map.
     * 
     * @param key the data key to remove
     */
    protected void removeData(String key) {
        data.remove(key);
    }

    /**
     * Clear all data entries from the raw data map.
     */
    protected void clearData() {
        data.clear();
    }

    /**
     * Validate the monitoring data for consistency.
     * 
     * <p>
     * This method provides common validation for all monitoring data classes.
     * Subclasses should call this method in their constructors and may add
     * additional validation specific to their data type.
     * </p>
     * 
     * @throws IllegalArgumentException if the data is invalid
     */
    protected void validateMonitoringData() {
        ValidationUtils.validateNotBlank(id, "ID");
        ValidationUtils.validateNotNull(timestamp, "Timestamp");
        ValidationUtils.validateNotBlank(domain, "Domain");
        ValidationUtils.validateNotInFuture(timestamp, "Timestamp");

        // Validate data map entries
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            ValidationUtils.validateNotBlank(entry.getKey(), "Data map key");
            // Note: We allow null values in the data map as they might be meaningful
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractMonitoring other = (AbstractMonitoring) obj;
        return Objects.equals(id, other.id) && Objects.equals(timestamp, other.timestamp)
                && Objects.equals(domain, other.domain) && Objects.equals(source, other.source)
                && Objects.equals(description, other.description) && Objects.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, domain, source, description, data);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "id='" + id + '\'' + ", timestamp=" + timestamp + ", domain='"
                + domain + '\'' + ", source='" + source + '\'' + ", dataSize=" + data.size() + '}';
    }
}
