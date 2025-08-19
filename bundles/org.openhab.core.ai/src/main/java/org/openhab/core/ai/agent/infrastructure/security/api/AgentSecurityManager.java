package org.openhab.core.ai.agent.infrastructure.security.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.infrastructure.security.AuditLog;
import org.openhab.core.ai.agent.infrastructure.security.AuthenticationResult;
import org.openhab.core.ai.agent.infrastructure.security.AuthorizationResult;
import org.openhab.core.ai.agent.infrastructure.security.DecryptedMessage;
import org.openhab.core.ai.agent.infrastructure.security.EncryptedMessage;
import org.openhab.core.ai.agent.infrastructure.security.KeyGenerationResult;
import org.openhab.core.ai.agent.infrastructure.security.SecurityIncident;
import org.openhab.core.ai.agent.infrastructure.security.SecurityPolicy;
import org.openhab.core.ai.agent.infrastructure.security.SecuritySeverity;
import org.openhab.core.ai.common.configuration.SecurityConfiguration;
import org.openhab.core.ai.common.security.MessageSecurityStatistics;

/**
 * Agent Security Manager Interface
 * 
 * <p>
 * This interface defines the contract for agent security management implementations that provide:
 * - Message encryption and decryption
 * - Digital signature verification
 * - Authentication and authorization
 * - Access control and permissions
 * - Audit logging and monitoring
 * - Security policy enforcement
 * - Security incident detection
 * - Security performance monitoring
 * - Security key management
 * - Security compliance and reporting
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AgentSecurityManager {

    /**
     * Encrypt message for secure transmission
     * 
     * @param message Message to encrypt
     * @param recipientId Recipient agent ID
     * @param senderId Sender agent ID
     * @return Encrypted message
     */
    CompletableFuture<EncryptedMessage> encryptMessage(String message, String recipientId, String senderId);

    /**
     * Decrypt message from secure transmission
     * 
     * @param encryptedMessage Encrypted message to decrypt
     * @param recipientId Recipient agent ID
     * @return Decrypted message
     */
    CompletableFuture<DecryptedMessage> decryptMessage(EncryptedMessage encryptedMessage, String recipientId);

    /**
     * Authenticate agent
     * 
     * @param agentId Agent ID to authenticate
     * @param credentials Authentication credentials
     * @return Authentication result
     */
    CompletableFuture<AuthenticationResult> authenticateAgent(String agentId, String credentials);

    /**
     * Authorize action for agent
     * 
     * @param agentId Agent ID
     * @param action Action to authorize
     * @param resource Resource to access
     * @return Authorization result
     */
    CompletableFuture<AuthorizationResult> authorizeAction(String agentId, String action, String resource);

    /**
     * Generate key pair for agent
     * 
     * @param agentId Agent ID
     * @return Key generation result
     */
    CompletableFuture<KeyGenerationResult> generateKeyPair(String agentId);

    /**
     * Set security policy for agent
     * 
     * @param agentId Agent ID
     * @param policy Security policy
     */
    void setSecurityPolicy(String agentId, SecurityPolicy policy);

    /**
     * Get security policy for agent
     * 
     * @param agentId Agent ID
     * @return Security policy or null if not found
     */
    @Nullable
    SecurityPolicy getSecurityPolicy(String agentId);

    /**
     * Get security statistics
     * 
     * @return Security statistics
     */
    MessageSecurityStatistics getSecurityStatistics();

    /**
     * Get security configuration
     * 
     * @return Security configuration
     */
    SecurityConfiguration getConfiguration();

    /**
     * Set security configuration
     * 
     * @param configuration Security configuration
     */
    void setConfiguration(SecurityConfiguration configuration);

    /**
     * Get security incidents
     * 
     * @return List of security incidents
     */
    List<SecurityIncident> getSecurityIncidents();

    /**
     * Get audit logs
     * 
     * @return List of audit logs
     */
    List<AuditLog> getAuditLogs();

    /**
     * Check if security is enabled
     * 
     * @return true if security is enabled
     */
    boolean isSecurityEnabled();

    /**
     * Check if agent is locked out
     * 
     * @param agentId Agent ID
     * @return true if agent is locked out
     */
    boolean isAgentLockedOut(String agentId);

    /**
     * Get agent permissions
     * 
     * @param agentId Agent ID
     * @return Set of permissions
     */
    Set<String> getAgentPermissions(String agentId);

    /**
     * Add agent permission
     * 
     * @param agentId Agent ID
     * @param permission Permission to add
     */
    void addAgentPermission(String agentId, String permission);

    /**
     * Remove agent permission
     * 
     * @param agentId Agent ID
     * @param permission Permission to remove
     */
    void removeAgentPermission(String agentId, String permission);

    /**
     * Get security incidents by severity
     * 
     * @param severity Security severity
     * @return List of security incidents
     */
    List<SecurityIncident> getSecurityIncidentsBySeverity(SecuritySeverity severity);

    /**
     * Get audit logs by agent
     * 
     * @param agentId Agent ID
     * @return List of audit logs
     */
    List<AuditLog> getAuditLogsByAgent(String agentId);

    /**
     * Get audit logs by time range
     * 
     * @param startTime Start time
     * @param endTime End time
     * @return List of audit logs
     */
    List<AuditLog> getAuditLogsByTimeRange(Instant startTime, Instant endTime);

    /**
     * Clear security incidents
     */
    void clearSecurityIncidents();

    /**
     * Clear audit logs
     */
    void clearAuditLogs();

    /**
     * Export security report
     * 
     * @param format Report format
     * @return Security report
     */
    String exportSecurityReport(String format);

    /**
     * Import security configuration
     * 
     * @param configuration Configuration to import
     */
    void importSecurityConfiguration(String configuration);

    /**
     * Backup security data
     * 
     * @return Backup data
     */
    Map<String, Object> backupSecurityData();

    /**
     * Restore security data
     * 
     * @param backupData Backup data to restore
     */
    void restoreSecurityData(Map<String, Object> backupData);
}
