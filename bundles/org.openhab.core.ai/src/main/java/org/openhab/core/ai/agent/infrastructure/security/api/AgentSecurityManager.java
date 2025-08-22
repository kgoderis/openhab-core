package org.openhab.core.ai.agent.infrastructure.security.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.infrastructure.security.AgentSecurityPolicy;
import org.openhab.core.ai.agent.infrastructure.security.AuditLog;
import org.openhab.core.ai.agent.infrastructure.security.AuthenticationResult;
import org.openhab.core.ai.agent.infrastructure.security.AuthorizationResult;
import org.openhab.core.ai.agent.infrastructure.security.DecryptedMessage;
import org.openhab.core.ai.agent.infrastructure.security.EncryptedMessage;
import org.openhab.core.ai.agent.infrastructure.security.KeyGenerationResult;
import org.openhab.core.ai.auth.SecurityIncident;
import org.openhab.core.ai.common.security.QuickSecurityResult;
import org.openhab.core.ai.common.security.SecurityManager;
import org.openhab.core.ai.common.security.SecuritySeverity;
import org.openhab.core.ai.security.config.SecurityConfiguration;

/**
 * Agent Security Manager Interface
 * 
 * This interface implements the common SecurityManager interface and defines the contract for agent security management
 * including message encryption/decryption, authentication, authorization, and audit logging.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AgentSecurityManager extends SecurityManager {

    // Message Security Operations

    /**
     * Encrypt message for secure transmission.
     * 
     * @param message Message to encrypt
     * @param recipientId Recipient agent ID
     * @param senderId Sender agent ID
     * @return Encrypted message
     */
    CompletableFuture<EncryptedMessage> encrypt(String message, String recipientId, String senderId);

    /**
     * Decrypt message from secure transmission.
     * 
     * @param encryptedMessage Encrypted message to decrypt
     * @param recipientId Recipient agent ID
     * @return Decrypted message
     */
    CompletableFuture<DecryptedMessage> decrypt(EncryptedMessage encryptedMessage, String recipientId);

    // Authentication and Authorization

    /**
     * Authenticate agent.
     * 
     * @param agentId Agent ID
     * @param credentials Authentication credentials
     * @return Authentication result
     */
    CompletableFuture<AuthenticationResult> authenticate(String agentId, String credentials);

    /**
     * Authorize action for agent.
     * 
     * @param agentId Agent ID
     * @param action Action to authorize
     * @param resource Resource to access
     * @return Authorization result
     */
    CompletableFuture<AuthorizationResult> authorize(String agentId, String action, String resource);

    /**
     * Quick security check for agent action.
     * 
     * @param agentId Agent ID
     * @param action Action to check
     * @return Quick security result
     */
    CompletableFuture<QuickSecurityResult> quickCheck(String agentId, String action);

    // Key Management

    /**
     * Generate key pair for agent.
     * 
     * @param agentId Agent ID
     * @return Key generation result
     */
    CompletableFuture<KeyGenerationResult> generateKeys(String agentId);

    // Security Policy Management

    /**
     * Get security policy for agent.
     * 
     * @param agentId Agent ID
     * @return Security policy
     */
    AgentSecurityPolicy getPolicy(String agentId);

    /**
     * Set security policy for agent.
     * 
     * @param agentId Agent ID
     * @param policy Security policy
     */
    void setPolicy(String agentId, AgentSecurityPolicy policy);

    // Audit Logging

    /**
     * Get all audit logs.
     * 
     * @return List of audit logs
     */
    List<AuditLog> getAuditLogs();

    /**
     * Get audit logs for specific agent.
     * 
     * @param agentId Agent ID
     * @return List of audit logs
     */
    List<AuditLog> getAuditLogsByAgent(String agentId);

    /**
     * Get audit logs by time range.
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return List of audit logs
     */
    List<AuditLog> getAuditLogsByTimeRange(Instant startTime, Instant endTime);

    /**
     * Clear all audit logs.
     */
    void clearAuditLogs();

    // Security Incidents

    /**
     * Get security incidents.
     * 
     * @return List of security incidents
     */
    List<SecurityIncident> getIncidents();

    /**
     * Get security incidents by severity.
     * 
     * @param severity Security severity
     * @return List of security incidents
     */
    List<SecurityIncident> getIncidentsBySeverity(SecuritySeverity severity);

    /**
     * Clear all security incidents.
     */
    void clearIncidents();

    // Agent Management

    /**
     * Check if agent is locked out.
     * 
     * @param agentId Agent ID
     * @return true if agent is locked out
     */
    boolean isLockedOut(String agentId);

    /**
     * Add permission to agent.
     * 
     * @param agentId Agent ID
     * @param permission Permission to add
     */
    void addPermission(String agentId, String permission);

    /**
     * Remove permission from agent.
     * 
     * @param agentId Agent ID
     * @param permission Permission to remove
     */
    void removePermission(String agentId, String permission);

    /**
     * Get agent permissions.
     * 
     * @param agentId Agent ID
     * @return Set of permissions
     */
    Set<String> getPermissions(String agentId);

    // Configuration Management

    /**
     * Get security configuration.
     * 
     * @return Security configuration
     */
    @Override
    SecurityConfiguration getConfig();

    /**
     * Set security configuration.
     * 
     * @param config Security configuration
     */
    @Override
    void updateConfig(SecurityConfiguration config);

    // Reporting and Backup

    /**
     * Export security report.
     * 
     * @param format Report format
     * @return Security report
     */
    String exportReport(String format);

    /**
     * Backup security data.
     * 
     * @return Security data backup
     */
    Map<String, Object> backup();

    /**
     * Restore security data.
     * 
     * @param data Security data to restore
     */
    void restore(Map<String, Object> data);

    /**
     * Import security configuration.
     * 
     * @param configData Configuration data
     */
    void importConfig(String configData);
}
