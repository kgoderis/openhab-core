package org.openhab.core.ai.auth;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

    public static class SecurityIncident {
        private final String incidentId;
        private final String principalId;
        private final String violationType;
        private final String description;
        private final String protocol;
        private final Instant timestamp;

        public SecurityIncident(String incidentId, String principalId, String violationType, String description,
                String protocol, Instant timestamp) {
            this.incidentId = incidentId;
            this.principalId = principalId;
            this.violationType = violationType;
            this.description = description;
            this.protocol = protocol;
            this.timestamp = timestamp;
        }

        public String getIncidentId() {
            return incidentId;
        }

        public String getPrincipalId() {
            return principalId;
        }

        public String getViolationType() {
            return violationType;
        }

        public String getDescription() {
            return description;
        }

        public String getProtocol() {
            return protocol;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format(
                    "SecurityIncident{incidentId='%s', principalId='%s', violationType='%s', description='%s', protocol='%s', timestamp=%s}",
                    incidentId, principalId, violationType, description, protocol, timestamp);
        }
    }