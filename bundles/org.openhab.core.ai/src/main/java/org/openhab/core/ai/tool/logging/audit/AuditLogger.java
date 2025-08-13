package org.openhab.core.ai.tool.logging.audit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Audit logger for tool system operations.
 * 
 * This interface defines the contract for audit logging that can track
 * and record tool system operations for security and compliance purposes.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AuditLogger {

    /**
     * Get the audit logger ID.
     * 
     * @return the audit logger ID
     */
    String getAuditLoggerId();

    /**
     * Get the audit logger name.
     * 
     * @return the audit logger name
     */
    String getAuditLoggerName();

    /**
     * Get the audit logger description.
     * 
     * @return the audit logger description
     */
    String getAuditLoggerDescription();

    /**
     * Get the supported audit levels.
     * 
     * @return list of supported audit levels
     */
    String[] getSupportedAuditLevels();

    /**
     * Check if the audit logger is enabled.
     * 
     * @return true if the audit logger is enabled
     */
    boolean isEnabled();

    /**
     * Log an audit event.
     * 
     * @param event the audit event to log
     */
    void logAuditEvent(AuditEvent event);

    /**
     * Log an audit event with the given parameters.
     * 
     * @param level the audit level
     * @param action the action being audited
     * @param userId the user ID
     * @param details additional audit details
     */
    void logAuditEvent(String level, String action, String userId, Map<String, Object> details);

    /**
     * Get the audit logger configuration.
     * 
     * @return the audit logger configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the audit logger configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    /**
     * Rotate audit logs.
     */
    void rotateLogs();

    /**
     * Get audit log statistics.
     * 
     * @return audit log statistics
     */
    Map<String, Object> getAuditStatistics();

    /**
     * Default implementation of AuditLogger.
     * 
     * @author Karel Goderis - Initial Contribution
     * @since 1.0.0
     */
    class DefaultAuditLogger implements AuditLogger {

        private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuditLogger.class);

        private final String auditLoggerId;
        private final String auditLoggerName;
        private final String auditLoggerDescription;
        private final String[] supportedAuditLevels;
        private final Map<String, Object> configuration;
        private final List<AuditEvent> auditEvents;
        private final AtomicLong totalEvents;
        private final AtomicLong totalBytes;
        private final ScheduledExecutorService scheduler;
        private final String logDirectory;
        private final String currentLogFile;
        private final SecretKey encryptionKey;
        private final int maxFileSize;
        private final int retentionDays;
        private boolean enabled = true;

        public DefaultAuditLogger(String auditLoggerId, String auditLoggerName, String auditLoggerDescription,
                String[] supportedAuditLevels) {
            this.auditLoggerId = auditLoggerId;
            this.auditLoggerName = auditLoggerName;
            this.auditLoggerDescription = auditLoggerDescription;
            this.supportedAuditLevels = supportedAuditLevels;
            this.configuration = new HashMap<>();
            this.auditEvents = new ArrayList<>();
            this.totalEvents = new AtomicLong(0);
            this.totalBytes = new AtomicLong(0);
            this.scheduler = Executors.newScheduledThreadPool(1);
            this.logDirectory = "logs/audit";
            this.currentLogFile = logDirectory + "/audit.log";
            this.encryptionKey = generateEncryptionKey();
            this.maxFileSize = 10 * 1024 * 1024; // 10MB
            this.retentionDays = 30;

            // Initialize log directory
            initializeLogDirectory();

            // Schedule log rotation
            scheduleLogRotation();

            // Schedule retention policy cleanup
            scheduleRetentionCleanup();
        }

        @Override
        public String getAuditLoggerId() {
            return auditLoggerId;
        }

        @Override
        public String getAuditLoggerName() {
            return auditLoggerName;
        }

        @Override
        public String getAuditLoggerDescription() {
            return auditLoggerDescription;
        }

        @Override
        public String[] getSupportedAuditLevels() {
            return supportedAuditLevels;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void logAuditEvent(AuditEvent event) {
            if (!enabled) {
                return;
            }

            try {
                // Validate audit event
                if (!validateAuditEvent(event)) {
                    LOGGER.warn("Invalid audit event: {}", event.getId());
                    return;
                }

                // Add to in-memory list
                synchronized (auditEvents) {
                    auditEvents.add(event);
                }

                // Write to log file
                writeAuditEventToFile(event);

                // Update statistics
                totalEvents.incrementAndGet();
                totalBytes.addAndGet(calculateEventSize(event));

                // Check if rotation is needed
                checkAndRotateLogs();

                LOGGER.debug("Audit event logged: {}", event.getId());

            } catch (Exception e) {
                LOGGER.error("Failed to log audit event: {}", event.getId(), e);
            }
        }

        @Override
        public void logAuditEvent(String level, String action, String userId, Map<String, Object> details) {
            String eventId = generateEventId();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            AuditEvent event = new AuditEvent(eventId, level, action, userId, timestamp, details);
            logAuditEvent(event);
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return new HashMap<>(configuration);
        }

        @Override
        public void updateConfiguration(Map<String, Object> configuration) {
            this.configuration.clear();
            this.configuration.putAll(configuration);
        }

        @Override
        public void rotateLogs() {
            try {
                File currentFile = new File(currentLogFile);
                if (!currentFile.exists() || currentFile.length() == 0) {
                    return;
                }

                // Create backup filename with timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String backupFile = logDirectory + "/audit_" + timestamp + ".log";

                // Encrypt and compress the current log file
                encryptAndCompressLog(currentLogFile, backupFile + ".zip");

                // Clear the current log file
                new FileWriter(currentLogFile).close();

                LOGGER.info("Audit logs rotated: {}", backupFile + ".zip");

            } catch (Exception e) {
                LOGGER.error("Failed to rotate audit logs", e);
            }
        }

        @Override
        public Map<String, Object> getAuditStatistics() {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalEvents", totalEvents.get());
            statistics.put("totalBytes", totalBytes.get());
            statistics.put("currentFileSize", getCurrentFileSize());
            statistics.put("enabled", enabled);
            statistics.put("logDirectory", logDirectory);
            statistics.put("maxFileSize", maxFileSize);
            statistics.put("retentionDays", retentionDays);

            synchronized (auditEvents) {
                statistics.put("inMemoryEvents", auditEvents.size());
            }

            return statistics;
        }

        /**
         * Set the enabled state of this audit logger.
         * 
         * @param enabled whether the audit logger is enabled
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Initialize the log directory.
         */
        private void initializeLogDirectory() {
            try {
                Path logPath = Paths.get(logDirectory);
                if (!Files.exists(logPath)) {
                    Files.createDirectories(logPath);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to create log directory: {}", logDirectory, e);
            }
        }

        /**
         * Schedule log rotation.
         */
        private void scheduleLogRotation() {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    rotateLogs();
                } catch (Exception e) {
                    LOGGER.error("Scheduled log rotation failed", e);
                }
            }, 24, 24, TimeUnit.HOURS);
        }

        /**
         * Schedule retention policy cleanup.
         */
        private void scheduleRetentionCleanup() {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    cleanupOldLogs();
                } catch (Exception e) {
                    LOGGER.error("Scheduled retention cleanup failed", e);
                }
            }, 1, 1, TimeUnit.DAYS);
        }

        /**
         * Validate an audit event.
         * 
         * @param event the audit event to validate
         * @return true if valid
         */
        private boolean validateAuditEvent(AuditEvent event) {
            if (event == null) {
                return false;
            }

            if (event.getId() == null || event.getId().isEmpty()) {
                return false;
            }

            if (event.getLevel() == null || event.getLevel().isEmpty()) {
                return false;
            }

            if (event.getAction() == null || event.getAction().isEmpty()) {
                return false;
            }

            if (event.getUserId() == null || event.getUserId().isEmpty()) {
                return false;
            }

            if (event.getTimestamp() == null || event.getTimestamp().isEmpty()) {
                return false;
            }

            return true;
        }

        /**
         * Write an audit event to the log file.
         * 
         * @param event the audit event to write
         */
        private void writeAuditEventToFile(AuditEvent event) {
            try {
                String logEntry = formatAuditEvent(event);

                // Encrypt the log entry
                byte[] encryptedData = encryptData(logEntry.getBytes());

                // Write to file
                try (FileWriter writer = new FileWriter(currentLogFile, true)) {
                    writer.write(new String(encryptedData) + "\n");
                }

            } catch (Exception e) {
                LOGGER.error("Failed to write audit event to file", e);
            }
        }

        /**
         * Format an audit event for logging.
         * 
         * @param event the audit event to format
         * @return formatted log entry
         */
        private String formatAuditEvent(AuditEvent event) {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(event.getTimestamp()).append("] ");
            sb.append("ID=").append(event.getId()).append(" ");
            sb.append("LEVEL=").append(event.getLevel()).append(" ");
            sb.append("ACTION=").append(event.getAction()).append(" ");
            sb.append("USER=").append(event.getUserId()).append(" ");
            sb.append("DETAILS=").append(event.getDetails());
            return sb.toString();
        }

        /**
         * Check if log rotation is needed and perform it.
         */
        private void checkAndRotateLogs() {
            try {
                File currentFile = new File(currentLogFile);
                if (currentFile.exists() && currentFile.length() > maxFileSize) {
                    rotateLogs();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to check log rotation", e);
            }
        }

        /**
         * Clean up old log files based on retention policy.
         */
        private void cleanupOldLogs() {
            try {
                File logDir = new File(logDirectory);
                if (!logDir.exists()) {
                    return;
                }

                File[] files = logDir.listFiles((dir, name) -> name.endsWith(".zip"));
                if (files == null) {
                    return;
                }

                long cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L);

                for (File file : files) {
                    if (file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            LOGGER.info("Deleted old audit log file: {}", file.getName());
                        } else {
                            LOGGER.warn("Failed to delete old audit log file: {}", file.getName());
                        }
                    }
                }

            } catch (Exception e) {
                LOGGER.error("Failed to cleanup old logs", e);
            }
        }

        /**
         * Encrypt and compress a log file.
         * 
         * @param sourceFile the source log file
         * @param targetFile the target compressed file
         */
        private void encryptAndCompressLog(String sourceFile, String targetFile) {
            try {
                Path sourcePath = Paths.get(sourceFile);
                if (!Files.exists(sourcePath)) {
                    return;
                }

                byte[] fileContent = Files.readAllBytes(sourcePath);
                byte[] encryptedContent = encryptData(fileContent);

                try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(targetFile)))) {
                    ZipEntry entry = new ZipEntry("audit.log");
                    zos.putNextEntry(entry);
                    zos.write(encryptedContent);
                    zos.closeEntry();
                }

            } catch (Exception e) {
                LOGGER.error("Failed to encrypt and compress log file", e);
            }
        }

        /**
         * Generate an encryption key.
         * 
         * @return the encryption key
         */
        private SecretKey generateEncryptionKey() {
            // In a real implementation, you would use a proper key management system
            byte[] keyBytes = "OpenHABAuditLoggerKey2024".getBytes();
            return new SecretKeySpec(keyBytes, "AES");
        }

        /**
         * Encrypt data.
         * 
         * @param data the data to encrypt
         * @return encrypted data
         */
        private byte[] encryptData(byte[] data) {
            try {
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
                return cipher.doFinal(data);
            } catch (Exception e) {
                LOGGER.error("Failed to encrypt data", e);
                return data; // Return original data if encryption fails
            }
        }

        /**
         * Generate a unique event ID.
         * 
         * @return the event ID
         */
        private String generateEventId() {
            return "audit_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
        }

        /**
         * Calculate the size of an audit event.
         * 
         * @param event the audit event
         * @return the size in bytes
         */
        private long calculateEventSize(AuditEvent event) {
            return event.getId().length() + event.getLevel().length() + event.getAction().length()
                    + event.getUserId().length() + event.getTimestamp().length()
                    + event.getDetails().toString().length();
        }

        /**
         * Get the current file size.
         * 
         * @return the file size in bytes
         */
        private long getCurrentFileSize() {
            try {
                File file = new File(currentLogFile);
                return file.exists() ? file.length() : 0;
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
