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
package org.openhab.core.ai.action;

import java.time.Instant;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Version information for an action.
 * 
 * This class tracks version information including version numbers,
 * compatibility information, and deprecation status.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ActionVersionInfo {

    private final String actionId;
    private final String version;
    private final String minCompatibleVersion;
    private final String maxCompatibleVersion;
    private final Set<String> compatibleVersions;
    private final boolean deprecated;
    private final String deprecationMessage;
    private final Instant deprecationDate;
    private final Instant removalDate;
    private final String migrationGuide;
    private final Set<String> breakingChanges;

    private ActionVersionInfo(Builder builder) {
        this.actionId = builder.actionId;
        this.version = builder.version;
        this.minCompatibleVersion = builder.minCompatibleVersion;
        this.maxCompatibleVersion = builder.maxCompatibleVersion;
        this.compatibleVersions = builder.compatibleVersions;
        this.deprecated = builder.deprecated;
        this.deprecationMessage = builder.deprecationMessage;
        this.deprecationDate = builder.deprecationDate;
        this.removalDate = builder.removalDate;
        this.migrationGuide = builder.migrationGuide;
        this.breakingChanges = builder.breakingChanges;
    }

    // Getters
    public String getActionId() {
        return actionId;
    }

    public String getVersion() {
        return version;
    }

    public String getMinCompatibleVersion() {
        return minCompatibleVersion;
    }

    public String getMaxCompatibleVersion() {
        return maxCompatibleVersion;
    }

    public Set<String> getCompatibleVersions() {
        return compatibleVersions;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public String getDeprecationMessage() {
        return deprecationMessage;
    }

    public Instant getDeprecationDate() {
        return deprecationDate;
    }

    public Instant getRemovalDate() {
        return removalDate;
    }

    public String getMigrationGuide() {
        return migrationGuide;
    }

    public Set<String> getBreakingChanges() {
        return breakingChanges;
    }

    /**
     * Check if this version is compatible with another version.
     * 
     * @param otherVersion the version to check compatibility with
     * @return true if compatible, false otherwise
     */
    public boolean isCompatibleWith(String otherVersion) {
        return compatibleVersions.contains(otherVersion);
    }

    /**
     * Check if this action is scheduled for removal.
     * 
     * @return true if scheduled for removal, false otherwise
     */
    public boolean isScheduledForRemoval() {
        return removalDate != null && Instant.now().isAfter(removalDate);
    }

    /**
     * Builder for ActionVersionInfo.
     */
    public static class Builder {
        private String actionId = "";
        private String version = "1.0.0";
        private String minCompatibleVersion = "1.0.0";
        private String maxCompatibleVersion = "2.0.0";
        private Set<String> compatibleVersions = Set.of();
        private boolean deprecated = false;
        private String deprecationMessage = "";
        private Instant deprecationDate = null;
        private Instant removalDate = null;
        private String migrationGuide = "";
        private Set<String> breakingChanges = Set.of();

        public Builder actionId(String actionId) {
            this.actionId = actionId;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder minCompatibleVersion(String minCompatibleVersion) {
            this.minCompatibleVersion = minCompatibleVersion;
            return this;
        }

        public Builder maxCompatibleVersion(String maxCompatibleVersion) {
            this.maxCompatibleVersion = maxCompatibleVersion;
            return this;
        }

        public Builder compatibleVersions(Set<String> compatibleVersions) {
            this.compatibleVersions = compatibleVersions;
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder deprecationMessage(String deprecationMessage) {
            this.deprecationMessage = deprecationMessage;
            return this;
        }

        public Builder deprecationDate(Instant deprecationDate) {
            this.deprecationDate = deprecationDate;
            return this;
        }

        public Builder removalDate(Instant removalDate) {
            this.removalDate = removalDate;
            return this;
        }

        public Builder migrationGuide(String migrationGuide) {
            this.migrationGuide = migrationGuide;
            return this;
        }

        public Builder breakingChanges(Set<String> breakingChanges) {
            this.breakingChanges = breakingChanges;
            return this;
        }

        public ActionVersionInfo build() {
            return new ActionVersionInfo(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return String.format("ActionVersionInfo{actionId='%s', version='%s', deprecated=%s}", actionId, version,
                deprecated);
    }
}
