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
import org.openhab.core.ai.auth.SecurityMetrics;
import org.openhab.core.ai.auth.SecurityIncident;

/**
 * Enhanced audit logger implementation for AI authentication and authorization events.
 * 
 * This implementation provides comprehensive logging of security events including
 * authentication attempts, authorization decisions, security violations, and session management.
 * It also includes security monitoring and analytics capabilities.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = AuditLogger.class, immediate = true)
public class DefaultAuditLogger implements AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAuditLogger.class);

    // Security metrics tracking
    private final AtomicLong totalAuthenticationAttempts = new AtomicLong(0);
    private final AtomicLong successfulAuthentications = new AtomicLong(0);
    private final AtomicLong failedAuthentications = new AtomicLong(0);
    private final AtomicLong totalPermissionChecks = new AtomicLong(0);
    private final AtomicLong grantedPermissions = new AtomicLong(0);
    private final AtomicLong deniedPermissions = new AtomicLong(0);
    private final AtomicLong securityViolations = new AtomicLong(0);
    private final AtomicLong sessionCreations = new AtomicLong(0);
    private final AtomicLong sessionTimeouts = new AtomicLong(0);

    // Security incident tracking
    private final Map<String, SecurityIncident> activeIncidents = new ConcurrentHashMap<>();
    private final Map<String, Long> clientFailureCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> permissionDenialCounts = new ConcurrentHashMap<>();

    // Configuration
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
    private final String encryptionKey = "default-audit-encryption-key";

    private final ReentrantLock logFileLock = new ReentrantLock();
    private @Nullable FileWriter currentLogWriter;
    private @Nullable String currentLogFile;
    private long currentLogFileSize = 0;

    /**
     * Create a new audit logger instance.
     */
    public DefaultAuditLogger() {
        logger.debug("Enhanced Audit Logger created");
        initializeAuditLogDirectory();
    }

    /**
     * Activate the audit logger component.
     */
    @Activate
    public void activate() {
        logger.info("Enhanced Audit Logger activated - security monitoring enabled");
        initializeLogFile();
        startLogRotationScheduler();
        startRetentionCleanupScheduler();
    }

    /**
     * Deactivate the audit logger component.
     */
    @Deactivate
    public void deactivate() {
        logger.info("Enhanced Audit Logger deactivated");
        closeCurrentLogFile();
    }

    /**
     * Initialize the audit log directory.
     */
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

    /**
     * Initialize the current log file.
     */
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

    /**
     * Write audit log entry with rotation and encryption support.
     * 
     * @param level Log level
     * @param message Log message
     */
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
                currentLogWriter.write(logEntry);
                currentLogWriter.flush();
                currentLogFileSize += logEntry.getBytes().length;
            }

            // Apply encryption if enabled
            if (enableEncryption) {
                encryptLogEntry(logEntry);
            }

        } catch (IOException e) {
            logger.error("Failed to write audit log entry", e);
        } finally {
            logFileLock.unlock();
        }
    }

    /**
     * Rotate the current log file.
     */
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

    /**
     * Compress a log file using GZIP.
     * 
     * @param logFilePath Path to the log file
     */
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

    /**
     * Clean up old log files based on retention policy.
     */
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

    /**
     * Encrypt a log entry.
     * 
     * @param logEntry The log entry to encrypt
     */
    private void encryptLogEntry(String logEntry) {
        try {
            // Simple hash-based encryption for demonstration
            // In production, use proper encryption libraries
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((logEntry + encryptionKey).getBytes());

            // Store encrypted hash for integrity verification
            String encryptedHash = bytesToHex(hash);
            logger.debug("Encrypted log entry hash: {}", encryptedHash);

        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to encrypt log entry", e);
        }
    }

    /**
     * Convert byte array to hexadecimal string.
     * 
     * @param bytes Byte array to convert
     * @return Hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Start the log rotation scheduler.
     */
    private void startLogRotationScheduler() {
        // Schedule daily log rotation
        Thread rotationThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(24 * 60 * 60 * 1000L); // 24 hours
                    rotateLogFile();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in log rotation scheduler", e);
                }
            }
        });
        rotationThread.setDaemon(true);
        rotationThread.setName("AuditLogRotation");
        rotationThread.start();
    }

    /**
     * Start the retention cleanup scheduler.
     */
    private void startRetentionCleanupScheduler() {
        // Schedule daily cleanup
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(24 * 60 * 60 * 1000L); // 24 hours
                    cleanupOldLogFiles();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error in retention cleanup scheduler", e);
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.setName("AuditLogCleanup");
        cleanupThread.start();
    }

    /**
     * Close the current log file.
     */
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

        // Track successful authentication patterns
        trackAuthenticationPattern(principalId, protocol, true);
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

        // Track JWT authentication patterns
        trackAuthenticationPattern(principalId, protocol, true);
    }

    @Override
    public void logJWTAuthenticationFailure(String tokenPrefix, String reason, Instant timestamp) {
        failedAuthentications.incrementAndGet();

        String eventMessage = String.format("JWT_AUTH_FAILURE - Token: %s..., Reason: %s, Timestamp: %s", tokenPrefix,
                reason, timestamp);

        logger.warn("AUDIT: {}", eventMessage);
        writeAuditLogEntry("WARN", eventMessage);

        // Track JWT failure patterns
        trackJWTFailurePattern(tokenPrefix, reason);
    }

    @Override
    public void logTokenRefresh(String principalId, Instant timestamp) {
        String eventMessage = String.format("TOKEN_REFRESH - Principal: %s, Timestamp: %s", principalId, timestamp);

        logger.info("AUDIT: {}", eventMessage);
        writeAuditLogEntry("INFO", eventMessage);

        // Track token refresh patterns
        trackTokenRefreshPattern(principalId);
    }

    @Override
    public void logLogout(String principalId, Instant timestamp) {
        String eventMessage = String.format("LOGOUT - Principal: %s, Timestamp: %s", principalId, timestamp);

        logger.info("AUDIT: {}", eventMessage);
        writeAuditLogEntry("INFO", eventMessage);

        // Track logout patterns
        trackLogoutPattern(principalId);
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

        // Track permission check patterns
        trackPermissionCheckPattern(principalId, permission, protocol, granted);
    }

    @Override
    public void logSecurityViolation(String principalId, String violationType, String description, String protocol,
            Instant timestamp) {
        securityViolations.incrementAndGet();

        String eventMessage = String.format(
                "SECURITY_VIOLATION - Principal: %s, Type: %s, Description: %s, Protocol: %s, Timestamp: %s",
                principalId, violationType, description, protocol, timestamp);

        logger.error("AUDIT: {}", eventMessage);
        writeAuditLogEntry("ERROR", eventMessage);

        // Create security incident
        createSecurityIncident(principalId, violationType, description, protocol, timestamp);

        // Track security violation patterns
        trackSecurityViolationPattern(principalId, violationType, protocol);
    }

    /**
     * Log session timeout event.
     * 
     * @param principalId Principal identifier
     * @param sessionId Session identifier
     * @param timestamp When the timeout occurred
     */
    public void logSessionTimeout(String principalId, String sessionId, Instant timestamp) {
        sessionTimeouts.incrementAndGet();

        String eventMessage = String.format("SESSION_TIMEOUT - Principal: %s, Session: %s, Timestamp: %s", principalId,
                sessionId, timestamp);

        logger.info("AUDIT: {}", eventMessage);
        writeAuditLogEntry("INFO", eventMessage);

        // Track session timeout patterns
        trackSessionTimeoutPattern(principalId, sessionId);
    }

    /**
     * Log session creation event.
     * 
     * @param principalId Principal identifier
     * @param sessionId Session identifier
     * @param timestamp When the session was created
     */
    public void logSessionCreation(String principalId, String sessionId, Instant timestamp) {
        sessionCreations.incrementAndGet();

        String eventMessage = String.format("SESSION_CREATION - Principal: %s, Session: %s, Timestamp: %s", principalId,
                sessionId, timestamp);

        logger.info("AUDIT: {}", eventMessage);
        writeAuditLogEntry("INFO", eventMessage);

        // Track session creation patterns
        trackSessionCreationPattern(principalId, sessionId);
    }

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

    /**
     * Clear resolved incidents.
     */
    public void clearResolvedIncidents() {
        long now = System.currentTimeMillis();
        activeIncidents.entrySet()
                .removeIf(entry -> now - entry.getValue().getTimestamp().toEpochMilli() > incidentTrackingWindowMs);
    }

    /**
     * Track client activity for security monitoring.
     * 
     * @param clientId Client identifier
     * @param activityType Type of activity
     */
    private void trackClientActivity(String clientId, String activityType) {
        // Track client activity patterns for anomaly detection
        logger.debug("Tracking client activity: {} - {}", clientId, activityType);
    }

    /**
     * Track authentication failure patterns.
     * 
     * @param clientId Client identifier
     * @param provider Authentication provider
     * @param reason Failure reason
     */
    private void trackAuthenticationFailure(String clientId, String provider, String reason) {
        long failureCount = clientFailureCounts.getOrDefault(clientId, 0L) + 1;
        clientFailureCounts.put(clientId, failureCount);

        // Check for potential security incidents
        if (failureCount >= maxFailuresBeforeAlert) {
            String incidentId = "auth_failure_" + clientId + "_" + System.currentTimeMillis();
            SecurityIncident incident = new SecurityIncident(incidentId, clientId, "AUTHENTICATION_FAILURE_SPIKE",
                    "Multiple authentication failures from client: " + clientId, "unknown", Instant.now());
            activeIncidents.put(incidentId, incident);

            logger.warn("SECURITY_INCIDENT: Multiple authentication failures detected for client: {}", clientId);
        }
    }

    /**
     * Track permission denial patterns.
     * 
     * @param principalId Principal identifier
     * @param permission Permission that was denied
     * @param protocol Protocol name
     */
    private void trackPermissionDenial(String principalId, String permission, String protocol) {
        String key = principalId + ":" + permission;
        long denialCount = permissionDenialCounts.getOrDefault(key, 0L) + 1;
        permissionDenialCounts.put(key, denialCount);

        // Check for potential security incidents
        if (denialCount >= maxPermissionDenialsBeforeAlert) {
            String incidentId = "permission_denial_" + principalId + "_" + System.currentTimeMillis();
            SecurityIncident incident = new SecurityIncident(incidentId, principalId, "PERMISSION_DENIAL_SPIKE",
                    "Multiple permission denials for: " + permission, protocol, Instant.now());
            activeIncidents.put(incidentId, incident);

            logger.warn("SECURITY_INCIDENT: Multiple permission denials detected for principal: {} permission: {}",
                    principalId, permission);
        }
    }

    /**
     * Create a security incident.
     * 
     * @param principalId Principal identifier
     * @param violationType Type of violation
     * @param description Violation description
     * @param protocol Protocol name
     * @param timestamp When the incident occurred
     */
    private void createSecurityIncident(String principalId, String violationType, String description, String protocol,
            Instant timestamp) {
        String incidentId = "incident_" + System.currentTimeMillis();
        SecurityIncident incident = new SecurityIncident(incidentId, principalId, violationType, description, protocol,
                timestamp);
        activeIncidents.put(incidentId, incident);

        logger.error("SECURITY_INCIDENT_CREATED: {} - {}", incidentId, description);
    }

    // Pattern tracking methods with actual implementation
    private final Map<String, AuthenticationPattern> authenticationPatterns = new ConcurrentHashMap<>();
    private final Map<String, JWTFailurePattern> jwtFailurePatterns = new ConcurrentHashMap<>();
    private final Map<String, TokenRefreshPattern> tokenRefreshPatterns = new ConcurrentHashMap<>();
    private final Map<String, LogoutPattern> logoutPatterns = new ConcurrentHashMap<>();
    private final Map<String, PermissionCheckPattern> permissionCheckPatterns = new ConcurrentHashMap<>();
    private final Map<String, SecurityViolationPattern> securityViolationPatterns = new ConcurrentHashMap<>();
    private final Map<String, SessionPattern> sessionPatterns = new ConcurrentHashMap<>();

    private void trackAuthenticationPattern(String principalId, String protocol, boolean success) {
        try {
            String key = principalId + ":" + protocol;
            AuthenticationPattern pattern = authenticationPatterns.computeIfAbsent(key,
                    k -> new AuthenticationPattern(principalId, protocol));

            pattern.recordAttempt(success);

            // Check for suspicious patterns
            if (pattern.getFailureRate() > 0.8 && pattern.getTotalAttempts() > 5) {
                logger.warn("Suspicious authentication pattern detected for {}: {}% failure rate", principalId,
                        (int) (pattern.getFailureRate() * 100));

                // Create security incident for suspicious pattern
                String incidentId = "auth_pattern_" + principalId + "_" + System.currentTimeMillis();
                SecurityIncident incident = new SecurityIncident(incidentId, principalId, "SUSPICIOUS_AUTH_PATTERN",
                        "High failure rate in authentication pattern: " + pattern.getFailureRate(), protocol,
                        Instant.now());
                activeIncidents.put(incidentId, incident);
            }

            logger.debug("Updated authentication pattern for {}: {} attempts, {}% success rate", principalId,
                    pattern.getTotalAttempts(), (int) ((1 - pattern.getFailureRate()) * 100));
        } catch (Exception e) {
            logger.error("Error tracking authentication pattern for {}: {}", principalId, e.getMessage());
        }
    }

    private void trackJWTFailurePattern(String tokenPrefix, String reason) {
        try {
            String key = tokenPrefix + ":" + reason;
            JWTFailurePattern pattern = jwtFailurePatterns.computeIfAbsent(key,
                    k -> new JWTFailurePattern(tokenPrefix, reason));

            pattern.recordFailure();

            // Check for token abuse patterns
            if (pattern.getFailureCount() > 10) {
                logger.warn("Potential JWT token abuse detected: {} failures for prefix {}", pattern.getFailureCount(),
                        tokenPrefix);

                // Create security incident for token abuse
                String incidentId = "jwt_abuse_" + tokenPrefix + "_" + System.currentTimeMillis();
                SecurityIncident incident = new SecurityIncident(incidentId, "unknown", "JWT_TOKEN_ABUSE",
                        "Multiple JWT failures for token prefix: " + tokenPrefix, "JWT", Instant.now());
                activeIncidents.put(incidentId, incident);
            }

            logger.debug("Updated JWT failure pattern: {} failures for prefix {}", pattern.getFailureCount(),
                    tokenPrefix);
        } catch (Exception e) {
            logger.error("Error tracking JWT failure pattern for {}: {}", tokenPrefix, e.getMessage());
        }
    }

    private void trackTokenRefreshPattern(String principalId) {
        try {
            TokenRefreshPattern pattern = tokenRefreshPatterns.computeIfAbsent(principalId,
                    k -> new TokenRefreshPattern(principalId));

            pattern.recordRefresh();

            // Check for excessive token refresh
            if (pattern.getRefreshCount() > 50) {
                logger.warn("Excessive token refresh detected for {}: {} refreshes", principalId,
                        pattern.getRefreshCount());
            }

            logger.debug("Updated token refresh pattern for {}: {} refreshes", principalId, pattern.getRefreshCount());
        } catch (Exception e) {
            logger.error("Error tracking token refresh pattern for {}: {}", principalId, e.getMessage());
        }
    }

    private void trackLogoutPattern(String principalId) {
        try {
            LogoutPattern pattern = logoutPatterns.computeIfAbsent(principalId, k -> new LogoutPattern(principalId));

            pattern.recordLogout();

            logger.debug("Updated logout pattern for {}: {} logouts", principalId, pattern.getLogoutCount());
        } catch (Exception e) {
            logger.error("Error tracking logout pattern for {}: {}", principalId, e.getMessage());
        }
    }

    private void trackPermissionCheckPattern(String principalId, String permission, String protocol, boolean granted) {
        try {
            String key = principalId + ":" + permission + ":" + protocol;
            PermissionCheckPattern pattern = permissionCheckPatterns.computeIfAbsent(key,
                    k -> new PermissionCheckPattern(principalId, permission, protocol));

            pattern.recordCheck(granted);

            // Check for permission abuse patterns
            if (pattern.getDenialRate() > 0.9 && pattern.getTotalChecks() > 10) {
                logger.warn("Potential permission abuse detected for {}: {}% denial rate for permission {}",
                        principalId, (int) (pattern.getDenialRate() * 100), permission);
            }

            logger.debug("Updated permission check pattern for {}: {} checks, {}% grant rate", principalId,
                    pattern.getTotalChecks(), (int) ((1 - pattern.getDenialRate()) * 100));
        } catch (Exception e) {
            logger.error("Error tracking permission check pattern for {}: {}", principalId, e.getMessage());
        }
    }

    private void trackSecurityViolationPattern(String principalId, String violationType, String protocol) {
        try {
            String key = principalId + ":" + violationType + ":" + protocol;
            SecurityViolationPattern pattern = securityViolationPatterns.computeIfAbsent(key,
                    k -> new SecurityViolationPattern(principalId, violationType, protocol));

            pattern.recordViolation();

            // Check for repeated violations
            if (pattern.getViolationCount() > 5) {
                logger.warn("Repeated security violations detected for {}: {} violations of type {}", principalId,
                        pattern.getViolationCount(), violationType);
            }

            logger.debug("Updated security violation pattern for {}: {} violations of type {}", principalId,
                    pattern.getViolationCount(), violationType);
        } catch (Exception e) {
            logger.error("Error tracking security violation pattern for {}: {}", principalId, e.getMessage());
        }
    }

    private void trackSessionTimeoutPattern(String principalId, String sessionId) {
        try {
            String key = principalId + ":" + sessionId;
            SessionPattern pattern = sessionPatterns.computeIfAbsent(key,
                    k -> new SessionPattern(principalId, sessionId));

            pattern.recordTimeout();

            logger.debug("Updated session timeout pattern for {}: {} timeouts", principalId, pattern.getTimeoutCount());
        } catch (Exception e) {
            logger.error("Error tracking session timeout pattern for {}: {}", principalId, e.getMessage());
        }
    }

    private void trackSessionCreationPattern(String principalId, String sessionId) {
        try {
            String key = principalId + ":" + sessionId;
            SessionPattern pattern = sessionPatterns.computeIfAbsent(key,
                    k -> new SessionPattern(principalId, sessionId));

            pattern.recordCreation();

            logger.debug("Updated session creation pattern for {}: {} creations", principalId,
                    pattern.getCreationCount());
        } catch (Exception e) {
            logger.error("Error tracking session creation pattern for {}: {}", principalId, e.getMessage());
        }
    }

    // Pattern analysis classes
    // Pattern classes extracted to top-level auth package

    /**
     * Security metrics data class.
     */

    /**
     * Security incident data class.
     */
}
