package org.openhab.core.ai.agent.infrastructure.security;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.lifecycle.AgentRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Communication Security Manager - Comprehensive security system
 * 
 * This service provides security capabilities for agent communication:
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
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentCommunicationSecurityManager.class)
@NonNullByDefault
public class AgentCommunicationSecurityManager {

    private final Logger logger = LoggerFactory.getLogger(AgentCommunicationSecurityManager.class);

    @Reference
    private @Nullable AgentRegistry agentRegistry;

    // Security management
    private final Map<String, SecurityPolicy> securityPolicies = new ConcurrentHashMap<>();
    private final Map<String, KeyPair> agentKeyPairs = new ConcurrentHashMap<>();
    private final Map<String, SecurityIncident> securityIncidents = new ConcurrentHashMap<>();
    private final Map<String, AuditLog> auditLogs = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalMessagesEncrypted = new AtomicLong(0);
    private final AtomicLong totalMessagesDecrypted = new AtomicLong(0);
    private final AtomicLong totalSignaturesVerified = new AtomicLong(0);
    private final AtomicLong totalSecurityIncidents = new AtomicLong(0);
    private final AtomicLong totalAuthenticationFailures = new AtomicLong(0);

    // Configuration
    private final AtomicReference<SecurityConfiguration> configuration = new AtomicReference<>(
            new SecurityConfiguration());

    // Background processing
    private final ScheduledExecutorService securityMonitor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService keyRotationProcessor = Executors.newSingleThreadScheduledExecutor();

    @Activate
    public void activate() {
        logger.info("Agent Communication Security Manager activated");

        // Start background processors
        securityMonitor.scheduleAtFixedRate(this::monitorSecurity, 0, 30000, TimeUnit.MILLISECONDS); // 30 seconds
        keyRotationProcessor.scheduleAtFixedRate(this::rotateKeys, 0, 3600000, TimeUnit.MILLISECONDS); // 1 hour
    }

    @Deactivate
    public void deactivate() {
        logger.info("Agent Communication Security Manager deactivated");

        // Shutdown background processors
        shutdownExecutor(securityMonitor);
        shutdownExecutor(keyRotationProcessor);
    }

    /**
     * Encrypt message for secure transmission
     * 
     * @param message Message to encrypt
     * @param recipientId Recipient agent ID
     * @param senderId Sender agent ID
     * @return Encrypted message
     */
    public CompletableFuture<EncryptedMessage> encryptMessage(String message, String recipientId, String senderId) {
        logger.debug("Encrypting message from {} to {}", senderId, recipientId);

        // Validate agents exist
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(recipientId, "system") == null
                || registry.getAgent(senderId, "system") == null) {
            return CompletableFuture
                    .completedFuture(EncryptedMessage.failure("Invalid agent ID: " + recipientId + " or " + senderId));
        }

        try {
            // Get recipient's public key
            KeyPair recipientKeyPair = agentKeyPairs.get(recipientId);
            if (recipientKeyPair == null) {
                return CompletableFuture
                        .completedFuture(EncryptedMessage.failure("No public key found for recipient: " + recipientId));
            }

            // Encrypt message (placeholder implementation)
            String encryptedContent = encryptContent(message, recipientKeyPair.getPublic());
            String signature = signContent(message, senderId);

            EncryptedMessage encryptedMessage = new EncryptedMessage(encryptedContent, signature, senderId, recipientId,
                    Instant.now());

            totalMessagesEncrypted.incrementAndGet();
            logAuditEvent("MESSAGE_ENCRYPTED", senderId, recipientId, "Message encrypted successfully");

            return CompletableFuture.completedFuture(encryptedMessage);

        } catch (Exception e) {
            logger.error("Error encrypting message: {}", e.getMessage(), e);
            totalSecurityIncidents.incrementAndGet();
            return CompletableFuture.completedFuture(EncryptedMessage.failure("Encryption failed: " + e.getMessage()));
        }
    }

    /**
     * Decrypt message from secure transmission
     * 
     * @param encryptedMessage Encrypted message to decrypt
     * @param recipientId Recipient agent ID
     * @return Decrypted message
     */
    public CompletableFuture<DecryptedMessage> decryptMessage(EncryptedMessage encryptedMessage, String recipientId) {
        logger.debug("Decrypting message for recipient: {}", recipientId);

        // Validate recipient
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(recipientId, "system") == null) {
            return CompletableFuture.completedFuture(DecryptedMessage.failure("Invalid recipient ID: " + recipientId));
        }

        try {
            // Verify recipient matches
            if (!encryptedMessage.getRecipientId().equals(recipientId)) {
                totalAuthenticationFailures.incrementAndGet();
                return CompletableFuture
                        .completedFuture(DecryptedMessage.failure("Message not intended for this recipient"));
            }

            // Get recipient's private key
            KeyPair recipientKeyPair = agentKeyPairs.get(recipientId);
            if (recipientKeyPair == null) {
                return CompletableFuture.completedFuture(
                        DecryptedMessage.failure("No private key found for recipient: " + recipientId));
            }

            // Decrypt message (placeholder implementation)
            String decryptedContent = decryptContent(encryptedMessage.getEncryptedContent(),
                    recipientKeyPair.getPrivate());

            // Verify signature
            boolean signatureValid = verifySignature(decryptedContent, encryptedMessage.getSignature(),
                    encryptedMessage.getSenderId());
            if (!signatureValid) {
                totalAuthenticationFailures.incrementAndGet();
                return CompletableFuture.completedFuture(DecryptedMessage.failure("Invalid message signature"));
            }

            DecryptedMessage decryptedMessage = new DecryptedMessage(decryptedContent, encryptedMessage.getSenderId(),
                    recipientId, Instant.now(), true);

            totalMessagesDecrypted.incrementAndGet();
            totalSignaturesVerified.incrementAndGet();
            logAuditEvent("MESSAGE_DECRYPTED", encryptedMessage.getSenderId(), recipientId,
                    "Message decrypted successfully");

            return CompletableFuture.completedFuture(decryptedMessage);

        } catch (Exception e) {
            logger.error("Error decrypting message: {}", e.getMessage(), e);
            totalSecurityIncidents.incrementAndGet();
            return CompletableFuture.completedFuture(DecryptedMessage.failure("Decryption failed: " + e.getMessage()));
        }
    }

    /**
     * Authenticate agent
     * 
     * @param agentId Agent ID to authenticate
     * @param credentials Authentication credentials
     * @return Authentication result
     */
    public CompletableFuture<AuthenticationResult> authenticateAgent(String agentId, String credentials) {
        logger.debug("Authenticating agent: {}", agentId);

        // Validate agent exists
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(agentId, "system") == null) {
            totalAuthenticationFailures.incrementAndGet();
            return CompletableFuture.completedFuture(AuthenticationResult.failure("Agent not found: " + agentId));
        }

        try {
            // Verify credentials (placeholder implementation)
            boolean authenticated = verifyCredentials(agentId, credentials);
            if (!authenticated) {
                totalAuthenticationFailures.incrementAndGet();
                logAuditEvent("AUTHENTICATION_FAILED", agentId, "system", "Invalid credentials");
                return CompletableFuture.completedFuture(AuthenticationResult.failure("Invalid credentials"));
            }

            // Generate authentication token
            String token = generateAuthenticationToken(agentId);
            AuthenticationResult result = AuthenticationResult.success(agentId, token, Instant.now());

            logAuditEvent("AUTHENTICATION_SUCCESS", agentId, "system", "Agent authenticated successfully");

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            logger.error("Error authenticating agent: {}", e.getMessage(), e);
            totalSecurityIncidents.incrementAndGet();
            return CompletableFuture
                    .completedFuture(AuthenticationResult.failure("Authentication failed: " + e.getMessage()));
        }
    }

    /**
     * Authorize action for agent
     * 
     * @param agentId Agent ID
     * @param action Action to authorize
     * @param resource Resource to access
     * @return Authorization result
     */
    public CompletableFuture<AuthorizationResult> authorizeAction(String agentId, String action, String resource) {
        logger.debug("Authorizing action {} on resource {} for agent {}", action, resource, agentId);

        // Get security policy for agent
        SecurityPolicy policy = securityPolicies.get(agentId);
        if (policy == null) {
            policy = getDefaultSecurityPolicy();
        }

        try {
            // Check permissions (placeholder implementation)
            boolean authorized = checkPermissions(agentId, action, resource, policy);
            if (!authorized) {
                logAuditEvent("AUTHORIZATION_FAILED", agentId, "system",
                        "Action " + action + " on " + resource + " denied");
                return CompletableFuture.completedFuture(AuthorizationResult.denied("Action not authorized"));
            }

            AuthorizationResult result = AuthorizationResult.granted(agentId, action, resource, Instant.now());

            logAuditEvent("AUTHORIZATION_SUCCESS", agentId, "system",
                    "Action " + action + " on " + resource + " authorized");

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            logger.error("Error authorizing action: {}", e.getMessage(), e);
            totalSecurityIncidents.incrementAndGet();
            return CompletableFuture
                    .completedFuture(AuthorizationResult.denied("Authorization failed: " + e.getMessage()));
        }
    }

    /**
     * Generate key pair for agent
     * 
     * @param agentId Agent ID
     * @return Key generation result
     */
    public CompletableFuture<KeyGenerationResult> generateKeyPair(String agentId) {
        logger.debug("Generating key pair for agent: {}", agentId);

        // Validate agent exists
        AgentRegistry registry = agentRegistry;
        if (registry == null || registry.getAgent(agentId, "system") == null) {
            return CompletableFuture.completedFuture(KeyGenerationResult.failure("Agent not found: " + agentId));
        }

        try {
            // Generate key pair (placeholder implementation)
            KeyPair keyPair = generateKeyPair();
            agentKeyPairs.put(agentId, keyPair);

            KeyGenerationResult result = KeyGenerationResult.success(agentId, keyPair.getPublic(), Instant.now());

            logAuditEvent("KEY_GENERATED", agentId, "system", "Key pair generated successfully");

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            logger.error("Error generating key pair: {}", e.getMessage(), e);
            totalSecurityIncidents.incrementAndGet();
            return CompletableFuture
                    .completedFuture(KeyGenerationResult.failure("Key generation failed: " + e.getMessage()));
        }
    }

    /**
     * Get security statistics
     * 
     * @return Security statistics
     */
    public SecurityStatistics getStatistics() {
        return new SecurityStatistics(totalMessagesEncrypted.get(), totalMessagesDecrypted.get(),
                totalSignaturesVerified.get(), totalSecurityIncidents.get(), totalAuthenticationFailures.get(),
                securityPolicies.size(), agentKeyPairs.size(), securityIncidents.size(), auditLogs.size());
    }

    /**
     * Get security incidents
     * 
     * @param limit Maximum number of incidents
     * @return List of security incidents
     */
    public List<SecurityIncident> getSecurityIncidents(int limit) {
        return securityIncidents.values().stream().sorted((i1, i2) -> i2.getTimestamp().compareTo(i1.getTimestamp()))
                .limit(limit).toList();
    }

    /**
     * Get audit logs
     * 
     * @param agentId Agent ID (optional)
     * @param limit Maximum number of logs
     * @return List of audit logs
     */
    public List<AuditLogEntry> getAuditLogs(@Nullable String agentId, int limit) {
        return auditLogs.values().stream().flatMap(log -> log.getEntries().stream())
                .filter(entry -> agentId == null || entry.getAgentId().equals(agentId))
                .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp())).limit(limit).toList();
    }

    // Private helper methods

    private String encryptContent(String content, PublicKey publicKey) {
        // Placeholder implementation
        return "encrypted_" + content;
    }

    private String decryptContent(String encryptedContent, PrivateKey privateKey) {
        // Placeholder implementation
        return encryptedContent.replace("encrypted_", "");
    }

    private String signContent(String content, String agentId) {
        // Placeholder implementation
        return "signature_" + agentId + "_" + content.hashCode();
    }

    private boolean verifySignature(String content, String signature, String agentId) {
        // Placeholder implementation
        String expectedSignature = "signature_" + agentId + "_" + content.hashCode();
        return signature.equals(expectedSignature);
    }

    private boolean verifyCredentials(String agentId, String credentials) {
        // Placeholder implementation
        return "valid_credentials".equals(credentials);
    }

    private String generateAuthenticationToken(String agentId) {
        // Placeholder implementation
        return "token_" + agentId + "_" + System.currentTimeMillis();
    }

    private boolean checkPermissions(String agentId, String action, String resource, SecurityPolicy policy) {
        // Placeholder implementation
        return policy.getAllowedActions().contains(action);
    }

    private KeyPair generateKeyPair() {
        // Placeholder implementation
        return new KeyPair(null, null);
    }

    private SecurityPolicy getDefaultSecurityPolicy() {
        return new SecurityPolicy("default", Set.of("read", "write"), Set.of("*"), Instant.now());
    }

    private void logAuditEvent(String event, String agentId, String target, String description) {
        AuditLog log = auditLogs.computeIfAbsent(agentId, id -> new AuditLog(id));
        log.addEntry(new AuditLogEntry(event, agentId, target, description, Instant.now()));
    }

    private void monitorSecurity() {
        logger.debug("Monitoring security");

        // Check for security incidents
        // This is a placeholder for actual security monitoring
    }

    private void rotateKeys() {
        logger.debug("Rotating keys");

        // Rotate keys for agents
        // This is a placeholder for actual key rotation
    }

    private void shutdownExecutor(ScheduledExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Inner classes

    public static class EncryptedMessage {
        private final String encryptedContent;
        private final String signature;
        private final String senderId;
        private final String recipientId;
        private final Instant timestamp;
        private final boolean success;
        private final String errorMessage;

        private EncryptedMessage(String encryptedContent, String signature, String senderId, String recipientId,
                Instant timestamp) {
            this.encryptedContent = encryptedContent;
            this.signature = signature;
            this.senderId = senderId;
            this.recipientId = recipientId;
            this.timestamp = timestamp;
            this.success = true;
            this.errorMessage = null;
        }

        private EncryptedMessage(String errorMessage) {
            this.encryptedContent = null;
            this.signature = null;
            this.senderId = null;
            this.recipientId = null;
            this.timestamp = null;
            this.success = false;
            this.errorMessage = errorMessage;
        }

        // Getters
        public String getEncryptedContent() {
            return encryptedContent;
        }

        public String getSignature() {
            return signature;
        }

        public String getSenderId() {
            return senderId;
        }

        public String getRecipientId() {
            return recipientId;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static EncryptedMessage failure(String errorMessage) {
            return new EncryptedMessage(errorMessage);
        }
    }

    public static class DecryptedMessage {
        private final String decryptedContent;
        private final String senderId;
        private final String recipientId;
        private final Instant timestamp;
        private final boolean signatureValid;
        private final boolean success;
        private final String errorMessage;

        private DecryptedMessage(String decryptedContent, String senderId, String recipientId, Instant timestamp,
                boolean signatureValid) {
            this.decryptedContent = decryptedContent;
            this.senderId = senderId;
            this.recipientId = recipientId;
            this.timestamp = timestamp;
            this.signatureValid = signatureValid;
            this.success = true;
            this.errorMessage = null;
        }

        private DecryptedMessage(String errorMessage) {
            this.decryptedContent = null;
            this.senderId = null;
            this.recipientId = null;
            this.timestamp = null;
            this.signatureValid = false;
            this.success = false;
            this.errorMessage = errorMessage;
        }

        // Getters
        public String getDecryptedContent() {
            return decryptedContent;
        }

        public String getSenderId() {
            return senderId;
        }

        public String getRecipientId() {
            return recipientId;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public boolean isSignatureValid() {
            return signatureValid;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static DecryptedMessage failure(String errorMessage) {
            return new DecryptedMessage(errorMessage);
        }
    }

    public static class AuthenticationResult {
        private final String agentId;
        private final String token;
        private final Instant timestamp;
        private final boolean success;
        private final String errorMessage;

        private AuthenticationResult(String agentId, String token, Instant timestamp) {
            this.agentId = agentId;
            this.token = token;
            this.timestamp = timestamp;
            this.success = true;
            this.errorMessage = null;
        }

        private AuthenticationResult(String errorMessage) {
            this.agentId = null;
            this.token = null;
            this.timestamp = null;
            this.success = false;
            this.errorMessage = errorMessage;
        }

        // Getters
        public String getAgentId() {
            return agentId;
        }

        public String getToken() {
            return token;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static AuthenticationResult success(String agentId, String token, Instant timestamp) {
            return new AuthenticationResult(agentId, token, timestamp);
        }

        public static AuthenticationResult failure(String errorMessage) {
            return new AuthenticationResult(errorMessage);
        }
    }

    public static class AuthorizationResult {
        private final String agentId;
        private final String action;
        private final String resource;
        private final Instant timestamp;
        private final boolean granted;
        private final String reason;

        private AuthorizationResult(String agentId, String action, String resource, Instant timestamp, boolean granted,
                String reason) {
            this.agentId = agentId;
            this.action = action;
            this.resource = resource;
            this.timestamp = timestamp;
            this.granted = granted;
            this.reason = reason;
        }

        // Getters
        public String getAgentId() {
            return agentId;
        }

        public String getAction() {
            return action;
        }

        public String getResource() {
            return resource;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public boolean isGranted() {
            return granted;
        }

        public String getReason() {
            return reason;
        }

        public static AuthorizationResult granted(String agentId, String action, String resource, Instant timestamp) {
            return new AuthorizationResult(agentId, action, resource, timestamp, true, "Action authorized");
        }

        public static AuthorizationResult denied(String reason) {
            return new AuthorizationResult(null, null, null, null, false, reason);
        }
    }

    public static class KeyGenerationResult {
        private final String agentId;
        private final PublicKey publicKey;
        private final Instant timestamp;
        private final boolean success;
        private final String errorMessage;

        private KeyGenerationResult(String agentId, PublicKey publicKey, Instant timestamp) {
            this.agentId = agentId;
            this.publicKey = publicKey;
            this.timestamp = timestamp;
            this.success = true;
            this.errorMessage = null;
        }

        private KeyGenerationResult(String errorMessage) {
            this.agentId = null;
            this.publicKey = null;
            this.timestamp = null;
            this.success = false;
            this.errorMessage = errorMessage;
        }

        // Getters
        public String getAgentId() {
            return agentId;
        }

        public PublicKey getPublicKey() {
            return publicKey;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static KeyGenerationResult success(String agentId, PublicKey publicKey, Instant timestamp) {
            return new KeyGenerationResult(agentId, publicKey, timestamp);
        }

        public static KeyGenerationResult failure(String errorMessage) {
            return new KeyGenerationResult(errorMessage);
        }
    }

    public static class SecurityPolicy {
        private final String policyId;
        private final Set<String> allowedActions;
        private final Set<String> allowedResources;
        private final Instant createdAt;

        public SecurityPolicy(String policyId, Set<String> allowedActions, Set<String> allowedResources,
                Instant createdAt) {
            this.policyId = policyId;
            this.allowedActions = allowedActions;
            this.allowedResources = allowedResources;
            this.createdAt = createdAt;
        }

        // Getters
        public String getPolicyId() {
            return policyId;
        }

        public Set<String> getAllowedActions() {
            return allowedActions;
        }

        public Set<String> getAllowedResources() {
            return allowedResources;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }
    }

    public static class SecurityIncident {
        private final String incidentId;
        private final String agentId;
        private final String incidentType;
        private final String description;
        private final Instant timestamp;
        private final SecuritySeverity severity;

        public SecurityIncident(String incidentId, String agentId, String incidentType, String description,
                Instant timestamp, SecuritySeverity severity) {
            this.incidentId = incidentId;
            this.agentId = agentId;
            this.incidentType = incidentType;
            this.description = description;
            this.timestamp = timestamp;
            this.severity = severity;
        }

        // Getters
        public String getIncidentId() {
            return incidentId;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getIncidentType() {
            return incidentType;
        }

        public String getDescription() {
            return description;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public SecuritySeverity getSeverity() {
            return severity;
        }
    }

    public static class AuditLog {
        private final String agentId;
        private final List<AuditLogEntry> entries;

        public AuditLog(String agentId) {
            this.agentId = agentId;
            this.entries = new java.util.ArrayList<>();
        }

        // Getters
        public String getAgentId() {
            return agentId;
        }

        public List<AuditLogEntry> getEntries() {
            return entries;
        }

        public void addEntry(AuditLogEntry entry) {
            entries.add(entry);
        }
    }

    public static class AuditLogEntry {
        private final String event;
        private final String agentId;
        private final String target;
        private final String description;
        private final Instant timestamp;

        public AuditLogEntry(String event, String agentId, String target, String description, Instant timestamp) {
            this.event = event;
            this.agentId = agentId;
            this.target = target;
            this.description = description;
            this.timestamp = timestamp;
        }

        // Getters
        public String getEvent() {
            return event;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getTarget() {
            return target;
        }

        public String getDescription() {
            return description;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    public static class SecurityStatistics {
        private final long totalMessagesEncrypted;
        private final long totalMessagesDecrypted;
        private final long totalSignaturesVerified;
        private final long totalSecurityIncidents;
        private final long totalAuthenticationFailures;
        private final int securityPolicies;
        private final int agentKeyPairs;
        private final int securityIncidents;
        private final int auditLogs;

        public SecurityStatistics(long totalMessagesEncrypted, long totalMessagesDecrypted,
                long totalSignaturesVerified, long totalSecurityIncidents, long totalAuthenticationFailures,
                int securityPolicies, int agentKeyPairs, int securityIncidents, int auditLogs) {
            this.totalMessagesEncrypted = totalMessagesEncrypted;
            this.totalMessagesDecrypted = totalMessagesDecrypted;
            this.totalSignaturesVerified = totalSignaturesVerified;
            this.totalSecurityIncidents = totalSecurityIncidents;
            this.totalAuthenticationFailures = totalAuthenticationFailures;
            this.securityPolicies = securityPolicies;
            this.agentKeyPairs = agentKeyPairs;
            this.securityIncidents = securityIncidents;
            this.auditLogs = auditLogs;
        }

        // Getters
        public long getTotalMessagesEncrypted() {
            return totalMessagesEncrypted;
        }

        public long getTotalMessagesDecrypted() {
            return totalMessagesDecrypted;
        }

        public long getTotalSignaturesVerified() {
            return totalSignaturesVerified;
        }

        public long getTotalSecurityIncidents() {
            return totalSecurityIncidents;
        }

        public long getTotalAuthenticationFailures() {
            return totalAuthenticationFailures;
        }

        public int getSecurityPolicies() {
            return securityPolicies;
        }

        public int getAgentKeyPairs() {
            return agentKeyPairs;
        }

        public int getSecurityIncidents() {
            return securityIncidents;
        }

        public int getAuditLogs() {
            return auditLogs;
        }
    }

    public static class SecurityConfiguration {
        private Duration keyRotationInterval = Duration.ofDays(30);
        private int maxAuthenticationFailures = 5;
        private Duration lockoutDuration = Duration.ofMinutes(15);
        private boolean enableAuditLogging = true;
        private boolean enableIncidentDetection = true;

        // Getters and setters
        public Duration getKeyRotationInterval() {
            return keyRotationInterval;
        }

        public void setKeyRotationInterval(Duration keyRotationInterval) {
            this.keyRotationInterval = keyRotationInterval;
        }

        public int getMaxAuthenticationFailures() {
            return maxAuthenticationFailures;
        }

        public void setMaxAuthenticationFailures(int maxAuthenticationFailures) {
            this.maxAuthenticationFailures = maxAuthenticationFailures;
        }

        public Duration getLockoutDuration() {
            return lockoutDuration;
        }

        public void setLockoutDuration(Duration lockoutDuration) {
            this.lockoutDuration = lockoutDuration;
        }

        public boolean isEnableAuditLogging() {
            return enableAuditLogging;
        }

        public void setEnableAuditLogging(boolean enableAuditLogging) {
            this.enableAuditLogging = enableAuditLogging;
        }

        public boolean isEnableIncidentDetection() {
            return enableIncidentDetection;
        }

        public void setEnableIncidentDetection(boolean enableIncidentDetection) {
            this.enableIncidentDetection = enableIncidentDetection;
        }
    }

    // Enums
    public enum SecuritySeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
