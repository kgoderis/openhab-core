package org.openhab.core.ai.tool.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Access log entry for tracking individual access attempts.
 * 
 * <p>
 * This class provides detailed information about individual access attempts including
 * user identification, action performed, resource accessed, and access decision.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AccessLogEntry {
    private final String userId;
    private final String action;
    private final String specificationId;
    private final String resource;
    private final boolean allowed;
    private final String reason;
    private final Instant timestamp;
    private final String ipAddress;

    /**
     * Constructor for AccessLogEntry.
     * 
     * @param userId user identifier
     * @param action action performed
     * @param specificationId specification identifier
     * @param resource resource accessed
     * @param allowed whether access was allowed
     * @param reason reason for access decision
     * @param timestamp timestamp of access attempt
     * @param ipAddress IP address of the request
     */
    public AccessLogEntry(String userId, String action, String specificationId, String resource, boolean allowed,
            String reason, Instant timestamp, String ipAddress) {
        this.userId = userId;
        this.action = action;
        this.specificationId = specificationId;
        this.resource = resource;
        this.allowed = allowed;
        this.reason = reason;
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
    }

    /**
     * Get user identifier.
     * 
     * @return user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get action performed.
     * 
     * @return action
     */
    public String getAction() {
        return action;
    }

    /**
     * Get specification identifier.
     * 
     * @return specification ID
     */
    public String getSpecificationId() {
        return specificationId;
    }

    /**
     * Get resource accessed.
     * 
     * @return resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * Check if access was allowed.
     * 
     * @return true if access was allowed
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * Get reason for access decision.
     * 
     * @return reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Get timestamp of access attempt.
     * 
     * @return timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get IP address of the request.
     * 
     * @return IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }
}
