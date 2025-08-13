package org.openhab.core.ai.tool.logging.audit;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Audit event for tool system operations.
 * 
 * This class represents an audit event that can be logged for security
 * and compliance purposes.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AuditEvent implements Serializable, Comparable<AuditEvent> {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String level;
    private final String action;
    private final String userId;
    private final String timestamp;
    private final Map<String, Object> details;
    private final String encryptedHash;

    /**
     * Create a new audit event.
     * 
     * @param id the event ID
     * @param level the audit level
     * @param action the action being audited
     * @param userId the user ID
     * @param timestamp the event timestamp
     * @param details additional audit details
     */
    public AuditEvent(String id, String level, String action, String userId, String timestamp,
            Map<String, Object> details) {
        this.id = validateId(id);
        this.level = validateLevel(level);
        this.action = validateAction(action);
        this.userId = validateUserId(userId);
        this.timestamp = validateTimestamp(timestamp);
        this.details = validateDetails(details);
        this.encryptedHash = generateEncryptedHash();
    }

    /**
     * Validate event ID.
     * 
     * @param id Event ID to validate
     * @return Validated event ID
     * @throws IllegalArgumentException if ID is invalid
     */
    private String validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }
        if (id.length() > 255) {
            throw new IllegalArgumentException("Event ID cannot exceed 255 characters");
        }
        return id.trim();
    }

    /**
     * Validate audit level.
     * 
     * @param level Audit level to validate
     * @return Validated audit level
     * @throws IllegalArgumentException if level is invalid
     */
    private String validateLevel(String level) {
        if (level == null || level.trim().isEmpty()) {
            throw new IllegalArgumentException("Audit level cannot be null or empty");
        }
        String normalizedLevel = level.trim().toUpperCase();
        if (!isValidLevel(normalizedLevel)) {
            throw new IllegalArgumentException("Invalid audit level: " + level);
        }
        return normalizedLevel;
    }

    /**
     * Check if audit level is valid.
     * 
     * @param level Audit level to check
     * @return true if valid, false otherwise
     */
    private boolean isValidLevel(String level) {
        return "INFO".equals(level) || "WARN".equals(level) || "ERROR".equals(level) || "DEBUG".equals(level);
    }

    /**
     * Validate action.
     * 
     * @param action Action to validate
     * @return Validated action
     * @throws IllegalArgumentException if action is invalid
     */
    private String validateAction(String action) {
        if (action == null || action.trim().isEmpty()) {
            throw new IllegalArgumentException("Action cannot be null or empty");
        }
        if (action.length() > 1000) {
            throw new IllegalArgumentException("Action cannot exceed 1000 characters");
        }
        return action.trim();
    }

    /**
     * Validate user ID.
     * 
     * @param userId User ID to validate
     * @return Validated user ID
     * @throws IllegalArgumentException if user ID is invalid
     */
    private String validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (userId.length() > 255) {
            throw new IllegalArgumentException("User ID cannot exceed 255 characters");
        }
        return userId.trim();
    }

    /**
     * Validate timestamp.
     * 
     * @param timestamp Timestamp to validate
     * @return Validated timestamp
     * @throws IllegalArgumentException if timestamp is invalid
     */
    private String validateTimestamp(String timestamp) {
        if (timestamp == null || timestamp.trim().isEmpty()) {
            throw new IllegalArgumentException("Timestamp cannot be null or empty");
        }
        try {
            Instant.parse(timestamp);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timestamp format: " + timestamp, e);
        }
        return timestamp.trim();
    }

    /**
     * Validate details map.
     * 
     * @param details Details map to validate
     * @return Validated details map
     * @throws IllegalArgumentException if details are invalid
     */
    private Map<String, Object> validateDetails(Map<String, Object> details) {
        if (details == null) {
            throw new IllegalArgumentException("Details cannot be null");
        }
        // Validate that all keys and values are not null
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException("Details map cannot contain null keys or values");
            }
        }
        return details;
    }

    /**
     * Generate encrypted hash for integrity verification.
     * 
     * @return Encrypted hash string
     */
    private String generateEncryptedHash() {
        try {
            String content = String.format("%s:%s:%s:%s:%s:%s", id, level, action, userId, timestamp,
                    details.toString());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate encrypted hash", e);
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
     * Verify the integrity of this audit event.
     * 
     * @return true if integrity is valid, false otherwise
     */
    public boolean verifyIntegrity() {
        try {
            String content = String.format("%s:%s:%s:%s:%s:%s", id, level, action, userId, timestamp,
                    details.toString());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            String currentHash = bytesToHex(hash);
            return encryptedHash.equals(currentHash);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    /**
     * Get the event ID.
     * 
     * @return the event ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the audit level.
     * 
     * @return the audit level
     */
    public String getLevel() {
        return level;
    }

    /**
     * Get the action being audited.
     * 
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Get the user ID.
     * 
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get the event timestamp.
     * 
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Get additional audit details.
     * 
     * @return audit details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Get the encrypted hash for integrity verification.
     * 
     * @return encrypted hash
     */
    public String getEncryptedHash() {
        return encryptedHash;
    }

    /**
     * Serialize the audit event to JSON format.
     * 
     * @return JSON string representation
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":\"").append(escapeJson(id)).append("\",");
        json.append("\"level\":\"").append(escapeJson(level)).append("\",");
        json.append("\"action\":\"").append(escapeJson(action)).append("\",");
        json.append("\"userId\":\"").append(escapeJson(userId)).append("\",");
        json.append("\"timestamp\":\"").append(escapeJson(timestamp)).append("\",");
        json.append("\"encryptedHash\":\"").append(escapeJson(encryptedHash)).append("\",");
        json.append("\"details\":{");

        boolean first = true;
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(escapeJson(entry.getKey())).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(escapeJson((String) entry.getValue())).append("\"");
            } else {
                json.append(entry.getValue().toString());
            }
            first = false;
        }

        json.append("}");
        json.append("}");
        return json.toString();
    }

    /**
     * Escape JSON string.
     * 
     * @param input String to escape
     * @return Escaped string
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t",
                "\\t");
    }

    /**
     * Serialize the audit event to XML format.
     * 
     * @return XML string representation
     */
    public String toXml() {
        StringBuilder xml = new StringBuilder();
        xml.append("<auditEvent>");
        xml.append("<id>").append(escapeXml(id)).append("</id>");
        xml.append("<level>").append(escapeXml(level)).append("</level>");
        xml.append("<action>").append(escapeXml(action)).append("</action>");
        xml.append("<userId>").append(escapeXml(userId)).append("</userId>");
        xml.append("<timestamp>").append(escapeXml(timestamp)).append("</timestamp>");
        xml.append("<encryptedHash>").append(escapeXml(encryptedHash)).append("</encryptedHash>");
        xml.append("<details>");

        for (Map.Entry<String, Object> entry : details.entrySet()) {
            xml.append("<detail key=\"").append(escapeXml(entry.getKey())).append("\">");
            xml.append(escapeXml(entry.getValue().toString()));
            xml.append("</detail>");
        }

        xml.append("</details>");
        xml.append("</auditEvent>");
        return xml.toString();
    }

    /**
     * Escape XML string.
     * 
     * @param input String to escape
     * @return Escaped string
     */
    private String escapeXml(String input) {
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    @Override
    public int compareTo(AuditEvent other) {
        if (other == null) {
            return 1;
        }

        // Compare by timestamp first
        int timestampCompare = this.timestamp.compareTo(other.timestamp);
        if (timestampCompare != 0) {
            return timestampCompare;
        }

        // Then by ID
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AuditEvent other = (AuditEvent) obj;
        return Objects.equals(id, other.id) && Objects.equals(level, other.level)
                && Objects.equals(action, other.action) && Objects.equals(userId, other.userId)
                && Objects.equals(timestamp, other.timestamp) && Objects.equals(details, other.details)
                && Objects.equals(encryptedHash, other.encryptedHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, level, action, userId, timestamp, details, encryptedHash);
    }

    @Override
    public String toString() {
        return String.format(
                "AuditEvent{id='%s', level='%s', action='%s', userId='%s', timestamp='%s', details=%s, encryptedHash='%s'}",
                id, level, action, userId, timestamp, details, encryptedHash);
    }
}
