package org.openhab.core.ai.tool.logging.audit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
 * Default implementation of {@link AuditLogger}.
 *
 * Author: Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultAuditLogger implements AuditLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuditLogger.class);

    private final String auditLoggerId;
    private final String auditLoggerName;
    private final String auditLoggerDescription;
    private final String[] supportedAuditLevels;
    private final Map<String, Object> configuration;
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
        this.totalEvents = new AtomicLong(0);
        this.totalBytes = new AtomicLong(0);
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.logDirectory = "logs/audit";
        this.currentLogFile = logDirectory + "/audit.log";
        this.encryptionKey = generateEncryptionKey();
        this.maxFileSize = 10 * 1024 * 1024; // 10MB
        this.retentionDays = 30;

        initializeLogDirectory();
        scheduleLogRotation();
        scheduleRetentionCleanup();
    }

    @Override
    public String getAuditLoggerId() { return auditLoggerId; }
    @Override
    public String getAuditLoggerName() { return auditLoggerName; }
    @Override
    public String getAuditLoggerDescription() { return auditLoggerDescription; }
    @Override
    public String[] getSupportedAuditLevels() { return supportedAuditLevels; }
    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void logAuditEvent(AuditEvent event) {
        if (!enabled) { return; }
        try {
            if (!validateAuditEvent(event)) { return; }
            writeAuditEventToFile(event);
            totalEvents.incrementAndGet();
            totalBytes.addAndGet(calculateEventSize(event));
            checkAndRotateLogs();
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
    public Map<String, Object> getConfiguration() { return new HashMap<>(configuration); }

    @Override
    public void updateConfiguration(Map<String, Object> configuration) {
        this.configuration.clear();
        this.configuration.putAll(configuration);
    }

    @Override
    public void rotateLogs() {
        try {
            File currentFile = new File(currentLogFile);
            if (!currentFile.exists() || currentFile.length() == 0) { return; }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFile = logDirectory + "/audit_" + timestamp + ".log";
            encryptAndCompressLog(currentLogFile, backupFile + ".zip");
            new FileWriter(currentLogFile).close();
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
        return statistics;
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    private void initializeLogDirectory() {
        try {
            Path logPath = Paths.get(logDirectory);
            if (!Files.exists(logPath)) { Files.createDirectories(logPath); }
        } catch (IOException e) { LOGGER.error("Failed to create log directory: {}", logDirectory, e); }
    }

    private void scheduleLogRotation() {
        scheduler.scheduleAtFixedRate(() -> { try { rotateLogs(); } catch (Exception e) { LOGGER.error("Scheduled log rotation failed", e); } }, 24, 24, TimeUnit.HOURS);
    }

    private void scheduleRetentionCleanup() {
        scheduler.scheduleAtFixedRate(() -> { try { cleanupOldLogs(); } catch (Exception e) { LOGGER.error("Scheduled retention cleanup failed", e); } }, 1, 1, TimeUnit.DAYS);
    }

    private boolean validateAuditEvent(AuditEvent event) {
        return event != null && event.getId() != null && !event.getId().isEmpty() && event.getLevel() != null
                && !event.getLevel().isEmpty() && event.getAction() != null && !event.getAction().isEmpty()
                && event.getUserId() != null && !event.getUserId().isEmpty() && event.getTimestamp() != null
                && !event.getTimestamp().isEmpty();
    }

    private void writeAuditEventToFile(AuditEvent event) {
        try {
            String logEntry = formatAuditEvent(event);
            byte[] encryptedData = encryptData(logEntry.getBytes());
            try (FileWriter writer = new FileWriter(currentLogFile, true)) { writer.write(new String(encryptedData) + "\n"); }
        } catch (Exception e) { LOGGER.error("Failed to write audit event to file", e); }
    }

    private String formatAuditEvent(AuditEvent event) {
        return "[" + event.getTimestamp() + "] " + "ID=" + event.getId() + " LEVEL=" + event.getLevel() + " ACTION="
                + event.getAction() + " USER=" + event.getUserId() + " DETAILS=" + event.getDetails();
    }

    private void checkAndRotateLogs() {
        try { File currentFile = new File(currentLogFile); if (currentFile.exists() && currentFile.length() > maxFileSize) { rotateLogs(); } } catch (Exception e) { LOGGER.error("Failed to check log rotation", e); }
    }

    private void cleanupOldLogs() {
        try {
            File logDir = new File(logDirectory);
            if (!logDir.exists()) { return; }
            File[] files = logDir.listFiles((dir, name) -> name.endsWith(".zip"));
            if (files == null) { return; }
            long cutoffTime = System.currentTimeMillis() - (retentionDays * 24L * 60 * 60 * 1000);
            for (File file : files) { if (file.lastModified() < cutoffTime) { if (!file.delete()) { LOGGER.warn("Failed to delete old audit log file: {}", file.getName()); } } }
        } catch (Exception e) { LOGGER.error("Failed to cleanup old logs", e); }
    }

    private void encryptAndCompressLog(String sourceFile, String targetFile) {
        try {
            Path sourcePath = Paths.get(sourceFile);
            if (!Files.exists(sourcePath)) { return; }
            byte[] fileContent = Files.readAllBytes(sourcePath);
            byte[] encryptedContent = encryptData(fileContent);
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(targetFile)))) {
                ZipEntry entry = new ZipEntry("audit.log");
                zos.putNextEntry(entry);
                zos.write(encryptedContent);
                zos.closeEntry();
            }
        } catch (Exception e) { LOGGER.error("Failed to encrypt and compress log file", e); }
    }

    private SecretKey generateEncryptionKey() {
        byte[] keyBytes = "OpenHABAuditLoggerKey2024".getBytes();
        return new SecretKeySpec(keyBytes, "AES");
    }

    private byte[] encryptData(byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            return cipher.doFinal(data);
        } catch (Exception e) { LOGGER.error("Failed to encrypt data", e); return data; }
    }

    private String generateEventId() { return "audit_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId(); }
    private long calculateEventSize(AuditEvent event) { return event.getId().length() + event.getLevel().length() + event.getAction().length() + event.getUserId().length() + event.getTimestamp().length() + event.getDetails().toString().length(); }
    private long getCurrentFileSize() { try { File file = new File(currentLogFile); return file.exists() ? file.length() : 0; } catch (Exception e) { return 0; } }
}


