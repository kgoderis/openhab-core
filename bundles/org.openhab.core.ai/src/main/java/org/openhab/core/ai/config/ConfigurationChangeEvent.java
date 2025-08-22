/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.config;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Event representing a configuration change.
 * 
 * <p>
 * This event is fired when configuration values change, either through OSGi Config Admin
 * updates or YAML file modifications.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class ConfigurationChangeEvent {

    /**
     * Type of configuration change.
     */
    public enum ChangeType {
        /** Configuration was added */
        ADDED,
        /** Configuration was modified */
        MODIFIED,
        /** Configuration was removed */
        REMOVED,
        /** Configuration was reloaded */
        RELOADED
    }

    /**
     * Source of the configuration change.
     */
    public enum ChangeSource {
        /** Change from environment variable */
        ENVIRONMENT,
        /** Change from OSGi Config Admin */
        OSGI_CONFIG,
        /** Change from YAML file */
        YAML_FILE,
        /** Change from default value */
        DEFAULT
    }

    private final String configurationKey;
    private final @Nullable String oldValue;
    private final @Nullable String newValue;
    private final ChangeType changeType;
    private final ChangeSource changeSource;
    private final String domain;
    private final Instant timestamp;
    private final @Nullable Map<String, Object> additionalData;

    /**
     * Creates a new configuration change event.
     * 
     * @param configurationKey the configuration key that changed
     * @param oldValue the old value (can be null for ADDED events)
     * @param newValue the new value (can be null for REMOVED events)
     * @param changeType the type of change
     * @param changeSource the source of the change
     * @param domain the configuration domain
     * @param additionalData additional data about the change
     */
    public ConfigurationChangeEvent(String configurationKey, @Nullable String oldValue, @Nullable String newValue,
            ChangeType changeType, ChangeSource changeSource, String domain,
            @Nullable Map<String, Object> additionalData) {
        this.configurationKey = Objects.requireNonNull(configurationKey, "Configuration key cannot be null");
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeType = Objects.requireNonNull(changeType, "Change type cannot be null");
        this.changeSource = Objects.requireNonNull(changeSource, "Change source cannot be null");
        this.domain = Objects.requireNonNull(domain, "Domain cannot be null");
        this.timestamp = Instant.now();
        this.additionalData = additionalData;
    }

    /**
     * Creates a new configuration change event with minimal parameters.
     * 
     * @param configurationKey the configuration key that changed
     * @param oldValue the old value
     * @param newValue the new value
     * @param changeType the type of change
     * @param changeSource the source of the change
     * @param domain the configuration domain
     */
    public ConfigurationChangeEvent(String configurationKey, @Nullable String oldValue, @Nullable String newValue,
            ChangeType changeType, ChangeSource changeSource, String domain) {
        this(configurationKey, oldValue, newValue, changeType, changeSource, domain, null);
    }

    /**
     * Gets the configuration key that changed.
     * 
     * @return the configuration key
     */
    public String getConfigurationKey() {
        return configurationKey;
    }

    /**
     * Gets the old value.
     * 
     * @return the old value, or null if not applicable
     */
    public @Nullable String getOldValue() {
        return oldValue;
    }

    /**
     * Gets the new value.
     * 
     * @return the new value, or null if not applicable
     */
    public @Nullable String getNewValue() {
        return newValue;
    }

    /**
     * Gets the type of change.
     * 
     * @return the change type
     */
    public ChangeType getChangeType() {
        return changeType;
    }

    /**
     * Gets the source of the change.
     * 
     * @return the change source
     */
    public ChangeSource getChangeSource() {
        return changeSource;
    }

    /**
     * Gets the configuration domain.
     * 
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Gets the timestamp when the change occurred.
     * 
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Gets additional data about the change.
     * 
     * @return additional data, or null if none
     */
    public @Nullable Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    /**
     * Checks if this is a value change (MODIFIED event with different values).
     * 
     * @return true if the value actually changed
     */
    public boolean isValueChange() {
        return changeType == ChangeType.MODIFIED && !Objects.equals(oldValue, newValue);
    }

    /**
     * Checks if this change affects a specific domain.
     * 
     * @param targetDomain the domain to check
     * @return true if the change affects the target domain
     */
    public boolean affectsDomain(String targetDomain) {
        return domain.equals(targetDomain) || configurationKey.startsWith(targetDomain + ".");
    }

    @Override
    public String toString() {
        return String.format(
                "ConfigurationChangeEvent{key='%s', oldValue='%s', newValue='%s', type=%s, source=%s, domain='%s', timestamp=%s}",
                configurationKey, oldValue, newValue, changeType, changeSource, domain, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConfigurationChangeEvent other = (ConfigurationChangeEvent) obj;
        return Objects.equals(configurationKey, other.configurationKey) && Objects.equals(oldValue, other.oldValue)
                && Objects.equals(newValue, other.newValue) && changeType == other.changeType
                && changeSource == other.changeSource && Objects.equals(domain, other.domain)
                && Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configurationKey, oldValue, newValue, changeType, changeSource, domain, timestamp);
    }
}
