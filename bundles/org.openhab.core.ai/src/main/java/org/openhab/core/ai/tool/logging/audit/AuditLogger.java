package org.openhab.core.ai.tool.logging.audit;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

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

    // TODO: Implement audit logging logic
    // TODO: Add support for audit log rotation
    // TODO: Implement audit log encryption
    // TODO: Add support for audit log retention policies
}
