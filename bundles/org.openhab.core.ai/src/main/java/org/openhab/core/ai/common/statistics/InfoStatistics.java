package org.openhab.core.ai.common.statistics;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.builder.AbstractBuilder;

/**
 * Unified statistics for information objects in the openHAB AI system.
 *
 * <p>
 * This class provides comprehensive statistics for information objects including
 * session info, servlet info, status info, and other informational data.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class InfoStatistics extends BaseStatistics {

    private final String infoId;
    private final String infoType;
    private final String infoName;
    private final String infoStatus;
    private final long creationTime;
    private final long lastUpdateTime;
    private final boolean isActive;
    private final Map<String, String> attributes;

    /**
     * Create a new InfoStatistics instance.
     *
     * @param id the statistics identifier
     * @param timestamp the collection timestamp
     * @param infoId the information identifier
     * @param infoType the information type
     * @param infoName the information name
     * @param infoStatus the information status
     * @param creationTime the creation time
     * @param lastUpdateTime the last update time
     * @param isActive whether the info is active
     * @param attributes additional attributes
     * @param metrics additional metrics
     */
    public InfoStatistics(String id, @Nullable Instant timestamp, String infoId, String infoType, String infoName,
            String infoStatus, long creationTime, long lastUpdateTime, boolean isActive, Map<String, String> attributes,
            Map<String, Object> metrics) {
        super(id, timestamp, StatisticsType.MONITORING, metrics);
        this.infoId = Objects.requireNonNull(infoId, "infoId");
        this.infoType = Objects.requireNonNull(infoType, "infoType");
        this.infoName = Objects.requireNonNull(infoName, "infoName");
        this.infoStatus = Objects.requireNonNull(infoStatus, "infoStatus");
        this.creationTime = creationTime;
        this.lastUpdateTime = lastUpdateTime;
        this.isActive = isActive;
        this.attributes = Objects.requireNonNull(attributes, "attributes");
    }

    /**
     * Get the information identifier.
     *
     * @return the information identifier
     */
    public String getInfoId() {
        return infoId;
    }

    /**
     * Get the information type.
     *
     * @return the information type
     */
    public String getInfoType() {
        return infoType;
    }

    /**
     * Get the information name.
     *
     * @return the information name
     */
    public String getInfoName() {
        return infoName;
    }

    /**
     * Get the information status.
     *
     * @return the information status
     */
    public String getInfoStatus() {
        return infoStatus;
    }

    /**
     * Get the creation time.
     *
     * @return the creation time
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Get the last update time.
     *
     * @return the last update time
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Check if the information is active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Get the additional attributes.
     *
     * @return the attributes map
     */
    public Map<String, String> getAttributes() {
        return Map.copyOf(attributes);
    }

    /**
     * Get a specific attribute value.
     *
     * @param key the attribute key
     * @return the attribute value, or null if not found
     */
    public @Nullable String getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Check if a specific attribute exists.
     *
     * @param key the attribute key
     * @return true if the attribute exists
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * Create a new builder for InfoStatistics.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for InfoStatistics.
     */
    public static final class Builder extends AbstractBuilder<InfoStatistics> {

        private @Nullable String id;
        private @Nullable Instant timestamp;
        private String infoId = "";
        private String infoType = "unknown";
        private String infoName = "";
        private String infoStatus = "unknown";
        private long creationTime = System.currentTimeMillis();
        private long lastUpdateTime = System.currentTimeMillis();
        private boolean isActive = true;
        private Map<String, String> attributes = Map.of();
        private Map<String, Object> metrics = Map.of();

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withTimestamp(@Nullable Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withInfoId(String infoId) {
            this.infoId = Objects.requireNonNull(infoId, "infoId");
            return this;
        }

        public Builder withInfoType(String infoType) {
            this.infoType = Objects.requireNonNull(infoType, "infoType");
            return this;
        }

        public Builder withInfoName(String infoName) {
            this.infoName = Objects.requireNonNull(infoName, "infoName");
            return this;
        }

        public Builder withInfoStatus(String infoStatus) {
            this.infoStatus = Objects.requireNonNull(infoStatus, "infoStatus");
            return this;
        }

        public Builder withCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder withLastUpdateTime(long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }

        public Builder withActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder withAttributes(Map<String, String> attributes) {
            this.attributes = Objects.requireNonNull(attributes, "attributes");
            return this;
        }

        public Builder withMetrics(Map<String, Object> metrics) {
            this.metrics = Objects.requireNonNull(metrics, "metrics");
            return this;
        }

        @Override
        protected void validate() {
            validateRequiredString(infoId, "infoId");
            validateRequiredString(infoType, "infoType");
            validateRequiredString(infoName, "infoName");
            validateRequiredString(infoStatus, "infoStatus");
            if (creationTime < 0) {
                addValidationError("creationTime must be >= 0");
            }
            if (lastUpdateTime < 0) {
                addValidationError("lastUpdateTime must be >= 0");
            }
            if (lastUpdateTime < creationTime) {
                addValidationError("lastUpdateTime cannot be before creationTime");
            }
        }

        @Override
        protected void doReset() {
            id = null;
            timestamp = null;
            infoId = "";
            infoType = "unknown";
            infoName = "";
            infoStatus = "unknown";
            creationTime = System.currentTimeMillis();
            lastUpdateTime = System.currentTimeMillis();
            isActive = true;
            attributes = Map.of();
            metrics = Map.of();
        }

        @Override
        public InfoStatistics build() {
            if (!isValid()) {
                throw new IllegalArgumentException("Invalid InfoStatisticsBuilder state: " + getValidationErrors());
            }
            String resolvedId = id != null ? id
                    : ("info-stats-" + System.currentTimeMillis() + "-" + System.nanoTime());
            return new InfoStatistics(resolvedId, timestamp, infoId, infoType, infoName, infoStatus, creationTime,
                    lastUpdateTime, isActive, attributes, metrics);
        }
    }
}
