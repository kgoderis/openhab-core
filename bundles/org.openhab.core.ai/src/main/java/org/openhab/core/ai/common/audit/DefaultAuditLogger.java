package org.openhab.core.ai.common.audit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.SecurityIncident;
import org.openhab.core.ai.auth.SecurityMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified audit logger implementation combining authentication events and generic audit logging.
 * 
 * This implementation provides comprehensive logging capabilities including:
 * - Authentication and authorization events with security monitoring
 * - Generic audit event logging with encryption and compression
 * - Log rotation and retention management
 * - Security incident tracking and analytics
 * - Performance metrics and statistics
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = { AuditLogger.class }, immediate = true)
public class DefaultAuditLogger implements AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAuditLogger.class);

    // Security metrics tracking (from auth version)
    private final AtomicLong totalAuthenticationAttempts = new AtomicLong(0);
    private final AtomicLong successfulAuthentications = new AtomicLong(0);
    private final AtomicLong failedAuthentications = new AtomicLong(0);
    private final AtomicLong totalPermissionChecks = new AtomicLong(0);
    private final AtomicLong grantedPermissions = new AtomicLong(0);
    private final AtomicLong deniedPermissions = new AtomicLong(0);
    private final AtomicLong securityViolations = new AtomicLong(0);
    private final AtomicLong sessionCreations = new AtomicLong(0);
    private final AtomicLong sessionTimeouts = new AtomicLong(0);

    // Generic audit metrics (from tool version)
    private final AtomicLong totalEvents = new AtomicLong(0);

    // Metrics service for centralized metrics collection
    @Reference
    private @Nullable MetricsService metricsService;

    // Security incident tracking
    private final Map<String, SecurityIncident> activeIncidents = new ConcurrentHashMap<>();
    private final Map<String, Long> clientFailureCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> permissionDenialCounts = new ConcurrentHashMap<>();

    // Configuration
    private final Map<String, Object> configuration = new HashMap<>();
    private final int maxFailuresBeforeAlert = 5;
    private final int maxPermissionDenialsBeforeAlert = 10;
    private final long incidentTrackingWindowMs = 300000; // 5 minutes

    // Audit log file management
    private final String auditLogDir = "logs/audit";
    private final String auditLogPrefix = "audit";
    private final String auditLogExtension = ".log";
    private final String compressedExtension = ".gz";
    private final int maxLogFileSizeMB = 100;
    private final int maxLogFiles = 10;
    private final int retentionDays = 90;
    private final boolean enableEncryption = true;
    private final String encryptionKeyString = "OpenHABAuditLoggerKey2024";

    private final ReentrantLock logFileLock = new ReentrantLock();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private @Nullable FileWriter currentLogWriter;
    private @Nullable String currentLogFile;
    private long currentLogFileSize = 0;
    private boolean enabled = true;

    // Encryption support
    private final SecretKey encryptionKey;

    // Identity fields for tool interface compatibility
    private final String auditLoggerId = "unified-audit-logger";
    private final String auditLoggerName = "Unified OpenHAB AI Audit Logger";
    private final String auditLoggerDescription = "Combined authentication and generic audit logging";
    private final String[] supportedAuditLevels = { "INFO", "WARN", "ERROR", "DEBUG", "TRACE" };

    /**
     * Create a new unified audit logger instance.
     */
    public DefaultAuditLogger() {
        this.encryptionKey = generateEncryptionKey();
        logger.debug("Unified Audit Logger created");
        initializeAuditLogDirectory();
    }

    /**
     * Activate the audit logger component.
     */
    @Activate
    public void activate() {
        logger.info("Unified Audit Logger activated - comprehensive audit logging enabled");
        initializeLogFile();
        startLogRotationScheduler();
        startRetentionCleanupScheduler();
    }

    /**
     * Deactivate the audit logger component.
     */
    @Deactivate
    public void deactivate() {
        logger.info("Unified Audit Logger deactivated");
        closeCurrentLogFile();
        scheduler.shutdown();
    }

    // Authentication/Authorization Audit Methods (from auth interface)

    @Override
    public void logAuthenticationAttempt(String clientId, String protocol, Instant timestamp) {
        totalAuthenticationAttempts.incrementAndGet();

        String eventMessage = String.format("AUTH_ATTEMPT - Client: %s, Protocol: %s, Timestamp: %s", clientId,
                protocol, timestamp);

        logger.info("AUDIT: {}", eventMessage);
        writeAuditLogEntry("INFO", eventMessage);

        // Track client failure patterns
        trackClientActivity(clientId, "authentication_attempt");
    }

    @Override
    public void logAuthenticationSuccess(String clientId, String principalId, String protocol, Instant timestamp) {
        successfulAuthentications.incrementAndGet();
        sessionCreations.incrementAndGet();

        String eventMessage = String.format("AUTH_SUCCESS - Client: %s, Principal: %s, Protocol: %s, Timestamp: %s",
                clientId, principalId, protocol, timestamp);

        logger.info("AUDIT: {}", eventMessage);
        writeAuditLogEntry("INFO", eventMessage);

        // Reset failure count for successful authentication
        clientFailureCounts.remove(clientId);
    }

    @Override
    public void logAuthenticationFailure(String clientId, String provider, String reason, Instant timestamp) {
        failedAuthentications.incrementAndGet();

        String eventMessage = String.format("AUTH_FAILURE - Client: %s, Provider: %s, Reason: %s, Timestamp: %s",
                clientId, provider, reason, timestamp);

        logger.warn("AUDIT: {}", eventMessage);
        writeAuditLogEntry("WARN", eventMessage);

        // Track failure patterns and check for security incidents
        trackAuthenticationFailure(clientId, provider, reason);
    }

    @Override
    public void logJWTAuthenticationSuccess(String principalId, String protocol, Instant timestamp) {
        successfulAuthentications.incrementAndGet();

        String eventMessage = String.format("JWT_AUTH_SUCCESS - Principal: %s, Protocol: %s, Timestamp: %s",
                principalId, protocol, timestamp);

        logger.info("AUDIT: {}", eventMessage);
        writeAuditLogEntry("INFO", eventMessage);
    }

    @Override
    public void logJWTAuthenticationFailure(String tokenPrefix, String reason, Instant timestamp) {
        failedAuthentications.incrementAndGet();

        String eventMessage = String.format("JWT_AUTH_FAILURE - Token: %s..., Reason: %s, Timestamp: %s", tokenPrefix,
                reason, timestamp);

        logger.warn("AUDIT: {}", eventMessage);
        writeAuditLogEntry("WARN", eventMessage);
    }

    @Override
    public void logTokenRefresh(String principalId, Instant timestamp) {
        String eventMessage = String.format("TOKEN_REFRESH - Principal: %s, Timestamp: %s", principalId, timestamp);

        logger.info("AUDIT: {}", eventMessage);
        writeAuditLogEntry("INFO", eventMessage);
    }

    @Override
    public void logLogout(String principalId, Instant timestamp) {
        String eventMessage = String.format("LOGOUT - Principal: %s, Timestamp: %s", principalId, timestamp);

        logger.info("AUDIT: {}", eventMessage);
        writeAuditLogEntry("INFO", eventMessage);
    }

    @Override
    public void logPermissionCheck(String principalId, String permission, String protocol, boolean granted,
            Instant timestamp) {
        totalPermissionChecks.incrementAndGet();

        if (granted) {
            grantedPermissions.incrementAndGet();
        } else {
            deniedPermissions.incrementAndGet();
            trackPermissionDenial(principalId, permission, protocol);
        }

        String eventMessage = String.format(
                "PERMISSION_CHECK - Principal: %s, Permission: %s, Protocol: %s, Granted: %s, Timestamp: %s",
                principalId, permission, protocol, granted, timestamp);

        logger.info("AUDIT: {}", eventMessage);
        writeAuditLogEntry("INFO", eventMessage);
    }

    @Override
    public void logSecurityViolation(String principalId, String violationType, String description, String protocol,
            Instant timestamp) {
        long startTime = System.nanoTime();
        securityViolations.incrementAndGet();

        String eventMessage = String.format(
                "SECURITY_VIOLATION - Principal: %s, Type: %s, Description: %s, Protocol: %s, Timestamp: %s",
                principalId, violationType, description, protocol, timestamp);

        logger.error("AUDIT: {}", eventMessage);

        // Record metrics for security violation logging
        recordAuditMetrics("security-violation", true, System.nanoTime() - startTime);
        writeAuditLogEntry("ERROR", eventMessage);

        // Create security incident
        createSecurityIncident(principalId, violationType, description, protocol, timestamp);
    }

    // Generic Audit Methods (from tool interface)

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
        return supportedAuditLevels.clone();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void logAuditEvent(AuditEvent event) {
        if (!enabled || event == null) {
            return;
        }
        long startTime = System.nanoTime();
        boolean success = false;
        try {
            String formattedEvent = String.format(
                    "AUDIT_EVENT - ID: %s, Type: %s, Level: %s, Action: %s, User: %s, Timestamp: %s",
                    event.getEventId(), event.getEventType(), event.getLevel(), event.getAction(), event.getUserId(),
                    event.getTimestamp());
            writeAuditLogEntry(event.getLevel(), formattedEvent);
            totalEvents.incrementAndGet();
            success = true;
        } catch (Exception e) {
            logger.error("Failed to log audit event: {}", event.getEventId(), e);
        } finally {
            // Record metrics for audit event logging
            recordAuditMetrics("audit-event", success, System.nanoTime() - startTime);
        }
    }

    @Override
    public void logAuditEvent(String level, String action, String userId, Map<String, Object> details) {
        String eventId = generateEventId();
        Instant timestamp = Instant.now();
        AuditEvent event = new AuditEvent(eventId, "GENERIC", level, action, userId, null, null, timestamp, details,
                null);
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
        rotateLogFile();
    }

    // Additional methods for security monitoring

    /**
     * Get security metrics.
     * 
     * @return security metrics
     */
    public SecurityMetrics getSecurityMetrics() {
        return new SecurityMetrics(totalAuthenticationAttempts.get(), successfulAuthentications.get(),
                failedAuthentications.get(), totalPermissionChecks.get(), grantedPermissions.get(),
                deniedPermissions.get(), securityViolations.get(), sessionCreations.get(), sessionTimeouts.get(),
                activeIncidents.size(), System.currentTimeMillis());
    }

    /**
     * Get active security incidents.
     * 
     * @return map of active incidents
     */
    public Map<String, SecurityIncident> getActiveIncidents() {
        return new ConcurrentHashMap<>(activeIncidents);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Record audit metrics using MetricsService
     */
    private void recordAuditMetrics(String operation, boolean success, long durationNanos) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("audit", operation, success, java.time.Duration.ofNanos(durationNanos));
            } catch (Exception e) {
                logger.debug("Failed to record audit metrics: {}", e.getMessage());
            }
        }
    }

    // Private helper methods

    private void initializeAuditLogDirectory() {
        try {
            Path logDir = Paths.get(auditLogDir);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                logger.info("Created audit log directory: {}", auditLogDir);
            }
        } catch (IOException e) {
            logger.error("Failed to create audit log directory: {}", auditLogDir, e);
        }
    }

    private void initializeLogFile() {
        try {
            String dateSuffix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            currentLogFile = String.format("%s/%s-%s%s", auditLogDir, auditLogPrefix, dateSuffix, auditLogExtension);

            File logFile = new File(currentLogFile);
            if (!logFile.exists()) {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }

            currentLogWriter = new FileWriter(currentLogFile, true);
            currentLogFileSize = logFile.length();

            logger.info("Initialized audit log file: {}", currentLogFile);
        } catch (IOException e) {
            logger.error("Failed to initialize audit log file", e);
        }
    }

    private void writeAuditLogEntry(String level, String message) {
        logFileLock.lock();
        try {
            if (currentLogWriter == null) {
                initializeLogFile();
            }

            String timestamp = Instant.now().toString();
            String logEntry = String.format("[%s] %s: %s%n", timestamp, level, message);

            // Check if log rotation is needed
            if (currentLogFileSize > maxLogFileSizeMB * 1024 * 1024) {
                rotateLogFile();
            }

            // Write to log file
            if (currentLogWriter != null) {
                if (enableEncryption) {
                    byte[] encryptedData = encryptData(logEntry.getBytes());
                    String encryptedEntry = new String(encryptedData) + "\n";
                    currentLogWriter.write(encryptedEntry);
                    currentLogFileSize += encryptedEntry.getBytes().length;
                } else {
                    currentLogWriter.write(logEntry);
                    currentLogFileSize += logEntry.getBytes().length;
                }
                currentLogWriter.flush();
            }

        } catch (IOException e) {
            logger.error("Failed to write audit log entry", e);
        } finally {
            logFileLock.unlock();
        }
    }

    private void rotateLogFile() {
        try {
            if (currentLogWriter != null) {
                currentLogWriter.close();
            }

            // Compress the current log file
            if (currentLogFile != null) {
                compressLogFile(currentLogFile);
            }

            // Initialize new log file
            initializeLogFile();

            // Clean up old log files
            cleanupOldLogFiles();

            logger.info("Audit log file rotated: {}", currentLogFile);
        } catch (IOException e) {
            logger.error("Failed to rotate audit log file", e);
        }
    }

    private void compressLogFile(String logFilePath) {
        try {
            File logFile = new File(logFilePath);
            if (!logFile.exists()) {
                return;
            }

            String compressedPath = logFilePath + compressedExtension;
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(Files.newOutputStream(Paths.get(compressedPath)))) {
                Files.copy(logFile.toPath(), gzipOut);
            }

            // Delete the original uncompressed file
            logFile.delete();

            logger.debug("Compressed audit log file: {} -> {}", logFilePath, compressedPath);
        } catch (IOException e) {
            logger.error("Failed to compress audit log file: {}", logFilePath, e);
        }
    }

    private void cleanupOldLogFiles() {
        try {
            File logDir = new File(auditLogDir);
            if (!logDir.exists()) {
                return;
            }

            File[] logFiles = logDir.listFiles((dir, name) -> name.startsWith(auditLogPrefix)
                    && (name.endsWith(auditLogExtension) || name.endsWith(compressedExtension)));

            if (logFiles == null) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L);

            for (File logFile : logFiles) {
                if (logFile.lastModified() < cutoffTime) {
                    if (logFile.delete()) {
                        logger.info("Deleted old audit log file: {}", logFile.getName());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup old log files", e);
        }
    }

    private void startLogRotationScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                rotateLogFile();
            } catch (Exception e) {
                logger.error("Scheduled log rotation failed", e);
            }
        }, 24, 24, TimeUnit.HOURS);
    }

    private void startRetentionCleanupScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupOldLogFiles();
                clearResolvedIncidents();
            } catch (Exception e) {
                logger.error("Scheduled retention cleanup failed", e);
            }
        }, 1, 1, TimeUnit.DAYS);
    }

    private void closeCurrentLogFile() {
        logFileLock.lock();
        try {
            if (currentLogWriter != null) {
                currentLogWriter.close();
                currentLogWriter = null;
            }
        } catch (IOException e) {
            logger.error("Failed to close audit log file", e);
        } finally {
            logFileLock.unlock();
        }
    }

    private SecretKey generateEncryptionKey() {
        byte[] keyBytes = encryptionKeyString.getBytes();
        // Pad or truncate to 16 bytes for AES
        byte[] aesKeyBytes = new byte[16];
        System.arraycopy(keyBytes, 0, aesKeyBytes, 0, Math.min(keyBytes.length, 16));
        return new SecretKeySpec(aesKeyBytes, "AES");
    }

    private byte[] encryptData(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            logger.error("Failed to encrypt data", e);
            return data; // Return unencrypted data if encryption fails
        }
    }

    private boolean validateAuditEvent(AuditEvent event) {
        return event != null && event.getEventId() != null && !event.getEventId().isEmpty() && event.getLevel() != null
                && !event.getLevel().isEmpty() && event.getAction() != null && !event.getAction().isEmpty()
                && event.getUserId() != null && !event.getUserId().isEmpty() && event.getTimestamp() != null;
    }

    private String formatAuditEvent(AuditEvent event) {
        return "ID=" + event.getEventId() + " LEVEL=" + event.getLevel() + " ACTION=" + event.getAction() + " USER="
                + event.getUserId() + " DETAILS=" + event.getDetails();
    }

    private String generateEventId() {
        return "audit_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    private long calculateEventSize(AuditEvent event) {
        return event.getEventId().length() + event.getLevel().length() + event.getAction().length()
                + event.getUserId().length() + event.getTimestamp().toString().length()
                + event.getDetails().toString().length();
    }

    private long getCurrentFileSize() {
        try {
            File file = new File(currentLogFile != null ? currentLogFile : "");
            return file.exists() ? file.length() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void clearResolvedIncidents() {
        long now = System.currentTimeMillis();
        activeIncidents.entrySet()
                .removeIf(entry -> now - entry.getValue().getTimestamp().toEpochMilli() > incidentTrackingWindowMs);
    }

    private void trackClientActivity(String clientId, String activityType) {
        logger.debug("Tracking client activity: {} - {}", clientId, activityType);
    }

    private void trackAuthenticationFailure(String clientId, String provider, String reason) {
        long failureCount = clientFailureCounts.getOrDefault(clientId, 0L) + 1;
        clientFailureCounts.put(clientId, failureCount);

        if (failureCount >= maxFailuresBeforeAlert) {
            String incidentId = "auth_failure_" + clientId + "_" + System.currentTimeMillis();
            SecurityIncident incident = new SecurityIncident(incidentId, clientId, "AUTHENTICATION_FAILURE_SPIKE",
                    "Multiple authentication failures from client: " + clientId, "unknown", Instant.now());
            activeIncidents.put(incidentId, incident);

            logger.warn("SECURITY_INCIDENT: Multiple authentication failures detected for client: {}", clientId);
        }
    }

    private void trackPermissionDenial(String principalId, String permission, String protocol) {
        String key = principalId + ":" + permission;
        long denialCount = permissionDenialCounts.getOrDefault(key, 0L) + 1;
        permissionDenialCounts.put(key, denialCount);

        if (denialCount >= maxPermissionDenialsBeforeAlert) {
            String incidentId = "permission_denial_" + principalId + "_" + System.currentTimeMillis();
            SecurityIncident incident = new SecurityIncident(incidentId, principalId, "PERMISSION_DENIAL_SPIKE",
                    "Multiple permission denials for: " + permission, protocol, Instant.now());
            activeIncidents.put(incidentId, incident);

            logger.warn("SECURITY_INCIDENT: Multiple permission denials detected for principal: {} permission: {}",
                    principalId, permission);
        }
    }

    private void createSecurityIncident(String principalId, String violationType, String description, String protocol,
            Instant timestamp) {
        String incidentId = "incident_" + System.currentTimeMillis();
        SecurityIncident incident = new SecurityIncident(incidentId, principalId, violationType, description, protocol,
                timestamp);
        activeIncidents.put(incidentId, incident);

        logger.error("SECURITY_INCIDENT_CREATED: {} - {}", incidentId, description);
    }

    // ===== Tool Operation Methods =====

    @Override
    public void logToolExecution(String toolId, String userId, String operation, Map<String, Object> parameters,
            boolean success, Instant timestamp) {
        totalEvents.incrementAndGet();

        String eventMessage = String.format(
                "TOOL_EXECUTION - Tool: %s, User: %s, Operation: %s, Success: %s, Timestamp: %s", toolId, userId,
                operation, success, timestamp);

        if (enabled) {
            writeAuditLogEntry("INFO", eventMessage);
        }

        logger.debug("Tool execution logged: {} by {} - {}", toolId, userId, operation);
    }

    @Override
    public void logToolAccess(String toolId, String userId, boolean granted, Instant timestamp) {
        totalEvents.incrementAndGet();

        String eventMessage = String.format("TOOL_ACCESS - Tool: %s, User: %s, Granted: %s, Timestamp: %s", toolId,
                userId, granted, timestamp);

        if (enabled) {
            writeAuditLogEntry("INFO", eventMessage);
        }

        logger.debug("Tool access logged: {} by {} - {}", toolId, userId, granted ? "GRANTED" : "DENIED");
    }
}
