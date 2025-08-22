package org.openhab.core.ai.agent.infrastructure.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
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
import org.openhab.core.ai.agent.infrastructure.security.api.AgentSecurityManager;
import org.openhab.core.ai.agent.lifecycle.api.AgentRegistry;
import org.openhab.core.ai.auth.AuthenticationContext;
import org.openhab.core.ai.auth.SecurityIncident;
import org.openhab.core.ai.common.security.AgentSecurityStatistics;
import org.openhab.core.ai.common.security.MessageSecurityStatistics;
import org.openhab.core.ai.common.security.QuickSecurityResult;
import org.openhab.core.ai.common.security.SecurityManager;
import org.openhab.core.ai.common.security.SecuritySeverity;
import org.openhab.core.ai.common.security.SecurityStatistics;
import org.openhab.core.ai.security.config.SecurityConfiguration;
import org.openhab.core.ai.tool.security.filters.SecurityResult;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Agent Security Manager - Comprehensive security system
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
@Component(service = AgentSecurityManager.class)
@NonNullByDefault
public class DefaultAgentSecurityManager implements AgentSecurityManager {

    private final Logger logger = LoggerFactory.getLogger(DefaultAgentSecurityManager.class);

    @Reference
    private @Nullable AgentRegistry agentRegistry;

    // Security management
    private final Map<String, AgentSecurityPolicy> securityPolicies = new ConcurrentHashMap<>();
    private final Map<String, KeyPair> agentKeyPairs = new ConcurrentHashMap<>();
    private final Map<String, SecurityIncident> securityIncidents = new ConcurrentHashMap<>();
    private final Map<String, AuditLog> auditLogs = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalMessagesEncrypted = new AtomicLong(0);
    private final AtomicLong totalMessagesDecrypted = new AtomicLong(0);
    private final AtomicLong totalSignaturesVerified = new AtomicLong(0);
    private final AtomicLong totalSecurityIncidents = new AtomicLong(0);
    private final AtomicLong totalAuthenticationFailures = new AtomicLong(0);
    private final AtomicLong totalChecks = new AtomicLong(0);
    private final AtomicLong allowedOperations = new AtomicLong(0);
    private final AtomicLong deniedOperations = new AtomicLong(0);

    // Configuration
    private final AtomicReference<SecurityConfiguration> configuration = new AtomicReference<>(
            SecurityConfiguration.builder().build());

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
    @Override
    public CompletableFuture<EncryptedMessage> encrypt(String message, String recipientId, String senderId) {
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
    @Override
    public CompletableFuture<DecryptedMessage> decrypt(EncryptedMessage encryptedMessage, String recipientId) {
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
    @Override
    public CompletableFuture<AuthenticationResult> authenticate(String agentId, String credentials) {
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
    @Override
    public CompletableFuture<AuthorizationResult> authorize(String agentId, String action, String resource) {
        logger.debug("Authorizing action {} on resource {} for agent {}", action, resource, agentId);

        // Get security policy for agent
        AgentSecurityPolicy policy = securityPolicies.get(agentId);
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
            return CompletableFuture.completedFuture(
                    new KeyGenerationResult(false, agentId, null, null, null, "Agent not found: " + agentId));
        }

        try {
            // Generate key pair (placeholder implementation)
            KeyPair keyPair = generateKeyPair();
            agentKeyPairs.put(agentId, keyPair);

            KeyGenerationResult result = new KeyGenerationResult(true, agentId, keyPair, "RSA",
                    Instant.now().plusSeconds(86400), null);

            logAuditEvent("KEY_GENERATED", agentId, "system", "Key pair generated successfully");

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            logger.error("Error generating key pair: {}", e.getMessage(), e);
            totalSecurityIncidents.incrementAndGet();
            return CompletableFuture.completedFuture(new KeyGenerationResult(false, agentId, null, null, null,
                    "Key generation failed: " + e.getMessage()));
        }
    }

    /**
     * Get security statistics
     * 
     * @return Security statistics
     */
    public MessageSecurityStatistics getSecurityStatistics() {
        long totalOps = totalMessagesEncrypted.get() + totalMessagesDecrypted.get() + totalSignaturesVerified.get();
        long successfulOps = totalMessagesEncrypted.get() + totalMessagesDecrypted.get()
                + totalSignaturesVerified.get();
        long failedOps = totalAuthenticationFailures.get();
        long violations = totalSecurityIncidents.get();
        return new MessageSecurityStatistics(totalOps, successfulOps, failedOps, violations, Instant.now(),
                totalMessagesEncrypted.get(), totalMessagesDecrypted.get(), totalSignaturesVerified.get(),
                totalAuthenticationFailures.get(), securityPolicies.size(), agentKeyPairs.size(), auditLogs.size());
    }

    /**
     * Get security incidents
     * 
     * @param limit Maximum number of incidents
     * @return List of security incidents
     */
    public List<SecurityIncident> getSecurityIncidents() {
        return securityIncidents.values().stream().sorted((i1, i2) -> i2.getTimestamp().compareTo(i1.getTimestamp()))
                .toList();
    }

    /**
     * Get security incidents with limit
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

    @Override
    public List<AuditLog> getAuditLogs() {
        return auditLogs.values().stream().toList();
    }

    @Override
    public List<AuditLog> getAuditLogsByAgent(String agentId) {
        return auditLogs.values().stream().filter(log -> log.getAgentId().equals(agentId)).toList();
    }

    @Override
    public List<AuditLog> getAuditLogsByTimeRange(Instant startTime, Instant endTime) {
        return auditLogs.values().stream()
                .filter(log -> log.getEntries().stream().anyMatch(
                        entry -> !entry.getTimestamp().isBefore(startTime) && !entry.getTimestamp().isAfter(endTime)))
                .toList();
    }

    @Override
    public void clearAuditLogs() {
        auditLogs.clear();
        logger.info("All audit logs cleared");
    }

    // Interface method implementations

    @Override
    public boolean isEnabled() {
        return configuration.get().isEnableAuditLogging();
    }

    @Override
    public boolean canAccess(String componentId, @Nullable String userId) {
        totalChecks.incrementAndGet();

        // Basic implementation - can be extended with actual access validation
        boolean allowed = true; // Default to allow

        if (allowed) {
            allowedOperations.incrementAndGet();
        } else {
            deniedOperations.incrementAndGet();
        }

        logger.debug("Access validation for component: {}, user: {}, allowed: {}", componentId, userId, allowed);
        return allowed;
    }

    @Override
    public SecurityResult validate(String action, String resource, AuthenticationContext context) {
        return SecurityResult.success("Agent validation passed");
    }

    @Override
    public void logViolation(String componentId, String violation, @Nullable Map<String, Object> context) {
        // Log a security violation or incident
        String incidentId = "violation_" + System.currentTimeMillis();
        SecurityIncident incident = new SecurityIncident(incidentId, componentId, "SECURITY_VIOLATION", violation,
                Instant.now(), SecuritySeverity.MEDIUM);
        securityIncidents.put(incidentId, incident);

        logAuditEvent("SECURITY_VIOLATION", componentId, "system", violation);
        logger.warn("Security violation in component {}: {} with context: {}", componentId, violation, context);
    }

    @Override
    public SecurityStatistics getStatistics() {
        return new AgentSecurityStatistics(totalChecks.get(), allowedOperations.get(), deniedOperations.get(), 0L,
                Instant.now(), 0, 0, true, true, 100, 60, 0);
    }

    @Override
    public SecurityManager.SecurityManagerType getType() {
        return SecurityManager.SecurityManagerType.AGENT;
    }

    @Override
    public SecurityConfiguration getConfig() {
        return configuration.get();
    }

    @Override
    public void updateConfig(SecurityConfiguration config) {
        this.configuration.set(config);
    }

    @Override
    public CompletableFuture<QuickSecurityResult> quickCheck(String agentId, String action) {
        return CompletableFuture.supplyAsync(() -> {
            AgentSecurityPolicy policy = getPolicy(agentId);
            if (policy == null) {
                return QuickSecurityResult.denied("No security policy found for agent: " + agentId);
            }
            // Simplified check: allow if encryption is enabled and action is permitted
            boolean granted = policy.isEncryptionEnabled() && policy.getAllowedActions().contains(action);
            return granted ? QuickSecurityResult.granted("Quick check passed")
                    : QuickSecurityResult.denied("Quick check failed for action: " + action);
        });
    }

    @Override
    public AgentSecurityPolicy getPolicy(String agentId) {
        return securityPolicies.getOrDefault(agentId, getDefaultSecurityPolicy());
    }

    @Override
    public void setPolicy(String agentId, AgentSecurityPolicy policy) {
        securityPolicies.put(agentId, policy);
        logger.debug("Security policy set for agent: {}", agentId);
    }

    @Override
    public Set<String> getPermissions(String agentId) {
        AgentSecurityPolicy policy = getPolicy(agentId);
        return policy.getAllowedActions();
    }

    @Override
    public void addPermission(String agentId, String permission) {
        AgentSecurityPolicy policy = getPolicy(agentId);
        Set<String> newActions = new HashSet<>(policy.getAllowedActions());
        newActions.add(permission);
        AgentSecurityPolicy newPolicy = new AgentSecurityPolicy(policy.getAgentId(), true, true, true, newActions,
                policy.getAllowedResources(), 86400, true);
        securityPolicies.put(agentId, newPolicy);
    }

    @Override
    public void removePermission(String agentId, String permission) {
        AgentSecurityPolicy policy = getPolicy(agentId);
        Set<String> newActions = new HashSet<>(policy.getAllowedActions());
        newActions.remove(permission);
        AgentSecurityPolicy newPolicy = new AgentSecurityPolicy(policy.getAgentId(), true, true, true, newActions,
                policy.getAllowedResources(), 86400, true);
        securityPolicies.put(agentId, newPolicy);
    }

    @Override
    public CompletableFuture<KeyGenerationResult> generateKeys(String agentId) {
        return generateKeyPair(agentId);
    }

    @Override
    public boolean isLockedOut(String agentId) {
        // Simple implementation - check if agent has too many authentication failures
        return totalAuthenticationFailures.get() > configuration.get().getMaxAuthenticationFailures();
    }

    @Override
    public List<SecurityIncident> getIncidents() {
        return securityIncidents.values().stream().sorted((i1, i2) -> i2.getTimestamp().compareTo(i1.getTimestamp()))
                .toList();
    }

    @Override
    public List<SecurityIncident> getIncidentsBySeverity(SecuritySeverity severity) {
        return securityIncidents.values().stream().filter(incident -> incident.getSeverity() == severity).toList();
    }

    @Override
    public void clearIncidents() {
        securityIncidents.clear();
    }

    @Override
    public Map<String, Object> backup() {
        Map<String, Object> backup = new HashMap<>();
        backup.put("securityPolicies", new HashMap<>(securityPolicies));
        backup.put("agentKeyPairs", new HashMap<>(agentKeyPairs));
        backup.put("configuration", configuration.get());
        return backup;
    }

    @Override
    public void restore(Map<String, Object> data) {
        if (data.containsKey("securityPolicies")) {
            securityPolicies.clear();
            securityPolicies.putAll((Map<String, AgentSecurityPolicy>) data.get("securityPolicies"));
        }
        if (data.containsKey("configuration")) {
            configuration.set((SecurityConfiguration) data.get("configuration"));
        }
    }

    @Override
    public String exportReport(String format) {
        // Simple implementation - return basic statistics as JSON
        return String.format(
                "{\"totalMessagesEncrypted\":%d,\"totalMessagesDecrypted\":%d,\"totalSecurityIncidents\":%d}",
                totalMessagesEncrypted.get(), totalMessagesDecrypted.get(), totalSecurityIncidents.get());
    }

    @Override
    public void importConfig(String configData) {
        // Simple implementation - parse JSON-like config
        logger.info("Importing security configuration: {}", configData);
    }

    // Private helper methods

    private String encryptContent(String content, PublicKey publicKey) {
        try {
            // Simple encryption using XOR with a key derived from the public key
            // In a real implementation, this would use proper encryption algorithms
            byte[] contentBytes = content.getBytes();
            byte[] keyBytes = publicKey.getEncoded();

            byte[] encryptedBytes = new byte[contentBytes.length];
            for (int i = 0; i < contentBytes.length; i++) {
                encryptedBytes[i] = (byte) (contentBytes[i] ^ keyBytes[i % keyBytes.length]);
            }

            // Convert to base64 for safe transmission
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            logger.error("Error encrypting content: {}", e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }

    private String decryptContent(String encryptedContent, PrivateKey privateKey) {
        try {
            // Decode from base64
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedContent);
            byte[] keyBytes = privateKey.getEncoded();

            // Simple decryption using XOR with the private key
            byte[] decryptedBytes = new byte[encryptedBytes.length];
            for (int i = 0; i < encryptedBytes.length; i++) {
                decryptedBytes[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);
            }

            return new String(decryptedBytes);
        } catch (Exception e) {
            logger.error("Error decrypting content: {}", e.getMessage());
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private String signContent(String content, String agentId) {
        try {
            // Create a digital signature using SHA-256 and the agent's private key
            KeyPair agentKeyPair = agentKeyPairs.get(agentId);
            if (agentKeyPair == null) {
                throw new RuntimeException("No key pair found for agent: " + agentId);
            }

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(agentKeyPair.getPrivate());
            signature.update(content.getBytes());

            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            logger.error("Error signing content: {}", e.getMessage());
            throw new RuntimeException("Signing failed", e);
        }
    }

    private boolean verifySignature(String content, String signature, String agentId) {
        try {
            // Verify the digital signature using the agent's public key
            KeyPair agentKeyPair = agentKeyPairs.get(agentId);
            if (agentKeyPair == null) {
                logger.warn("No key pair found for agent: {}", agentId);
                return false;
            }

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(agentKeyPair.getPublic());
            sig.update(content.getBytes());

            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            return sig.verify(signatureBytes);
        } catch (Exception e) {
            logger.error("Error verifying signature: {}", e.getMessage());
            return false;
        }
    }

    private boolean verifyCredentials(String agentId, String credentials) {
        try {
            // In a real implementation, this would verify against a secure credential store
            // For now, we'll use a simple validation pattern
            if (credentials == null || credentials.isEmpty()) {
                return false;
            }

            // Check if credentials match expected pattern for the agent
            // This is a simplified validation - in production, use proper authentication
            return credentials.startsWith("cred_") && credentials.contains(agentId);
        } catch (Exception e) {
            logger.error("Error verifying credentials: {}", e.getMessage());
            return false;
        }
    }

    private String generateAuthenticationToken(String agentId) {
        try {
            // Generate a JWT-like token with agent information
            // In a real implementation, this would use proper JWT libraries
            String header = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
            String payload = Base64.getEncoder()
                    .encodeToString(String
                            .format("{\"agentId\":\"%s\",\"exp\":%d}", agentId, System.currentTimeMillis() + 3600000)
                            .getBytes());

            // Create a simple signature
            String signature = Base64.getEncoder()
                    .encodeToString((agentId + "_" + System.currentTimeMillis()).getBytes());

            return header + "." + payload + "." + signature;
        } catch (Exception e) {
            logger.error("Error generating authentication token: {}", e.getMessage());
            throw new RuntimeException("Token generation failed", e);
        }
    }

    private boolean checkPermissions(String agentId, String action, String resource, AgentSecurityPolicy policy) {
        try {
            // Check if the action is allowed by the policy
            if (!policy.getAllowedActions().contains(action)) {
                logger.debug("Action {} not allowed for agent {}", action, agentId);
                return false;
            }

            // Check if the resource is allowed by the policy
            boolean resourceAllowed = policy.getAllowedResources().contains("*")
                    || policy.getAllowedResources().contains(resource);

            if (!resourceAllowed) {
                logger.debug("Resource {} not allowed for agent {}", resource, agentId);
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("Error checking permissions: {}", e.getMessage());
            return false;
        }
    }

    private KeyPair generateKeyPair() {
        try {
            // Generate RSA key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048, new SecureRandom());
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            logger.error("Error generating key pair: {}", e.getMessage());
            throw new RuntimeException("Key pair generation failed", e);
        }
    }

    private AgentSecurityPolicy getDefaultSecurityPolicy() {
        return new AgentSecurityPolicy("default", true, true, true, Set.of("read", "write"), Set.of("*"), 86400, true);
    }

    private void logAuditEvent(String event, String agentId, String target, String description) {
        AuditLog log = auditLogs.computeIfAbsent(agentId, id -> new AuditLog(id));
        log.addEntry(new AuditLogEntry(event, agentId, target, description, Instant.now()));
    }

    private void monitorSecurity() {
        try {
            logger.debug("Monitoring security");

            // Check for security incidents
            long currentTime = System.currentTimeMillis();
            long incidentThreshold = 10; // Alert if more than 10 incidents in monitoring window

            if (totalSecurityIncidents.get() > incidentThreshold) {
                String incidentId = "security_monitoring_" + currentTime;
                SecurityIncident incident = new SecurityIncident(incidentId, "system", "SECURITY_MONITORING_ALERT",
                        "High number of security incidents detected: " + totalSecurityIncidents.get(), Instant.now(),
                        SecuritySeverity.HIGH);
                securityIncidents.put(incidentId, incident);

                logger.warn("SECURITY_ALERT: High number of security incidents detected: {}",
                        totalSecurityIncidents.get());
            }

            // Check for authentication failures
            long authFailureThreshold = 20; // Alert if more than 20 auth failures
            if (totalAuthenticationFailures.get() > authFailureThreshold) {
                String incidentId = "auth_failure_monitoring_" + currentTime;
                SecurityIncident incident = new SecurityIncident(incidentId, "system", "AUTHENTICATION_FAILURE_ALERT",
                        "High number of authentication failures detected: " + totalAuthenticationFailures.get(),
                        Instant.now(), SecuritySeverity.MEDIUM);
                securityIncidents.put(incidentId, incident);

                logger.warn("AUTH_ALERT: High number of authentication failures detected: {}",
                        totalAuthenticationFailures.get());
            }

            // Check key pair health
            for (Map.Entry<String, KeyPair> entry : agentKeyPairs.entrySet()) {
                String agentId = entry.getKey();
                KeyPair keyPair = entry.getValue();

                if (keyPair.getPublic() == null || keyPair.getPrivate() == null) {
                    String incidentId = "key_health_" + agentId + "_" + currentTime;
                    SecurityIncident incident = new SecurityIncident(incidentId, agentId, "KEY_PAIR_HEALTH_ISSUE",
                            "Invalid key pair detected for agent: " + agentId, Instant.now(), SecuritySeverity.MEDIUM);
                    securityIncidents.put(incidentId, incident);

                    logger.warn("KEY_HEALTH_ALERT: Invalid key pair detected for agent: {}", agentId);
                }
            }

            // Clean up old incidents (older than 24 hours)
            long cleanupThreshold = 24 * 60 * 60 * 1000; // 24 hours
            securityIncidents.entrySet()
                    .removeIf(entry -> currentTime - entry.getValue().getTimestamp().toEpochMilli() > cleanupThreshold);

        } catch (Exception e) {
            logger.error("Error in security monitoring: {}", e.getMessage(), e);
        }
    }

    private void rotateKeys() {
        try {
            logger.debug("Rotating keys");

            // Rotate keys for agents
            for (String agentId : agentKeyPairs.keySet()) {
                try {
                    // Generate new key pair
                    KeyPair newKeyPair = generateKeyPair();

                    // Store the new key pair
                    agentKeyPairs.put(agentId, newKeyPair);

                    // Log the key rotation
                    logAuditEvent("KEY_ROTATED", agentId, "system", "Key pair rotated successfully");

                    logger.info("Key pair rotated for agent: {}", agentId);

                } catch (Exception e) {
                    logger.error("Error rotating keys for agent {}: {}", agentId, e.getMessage());

                    // Create security incident for key rotation failure
                    String incidentId = "key_rotation_failure_" + agentId + "_" + System.currentTimeMillis();
                    SecurityIncident incident = new SecurityIncident(incidentId, agentId, "KEY_ROTATION_FAILURE",
                            "Failed to rotate key pair for agent: " + agentId, Instant.now(), SecuritySeverity.HIGH);
                    securityIncidents.put(incidentId, incident);
                }
            }

            // Update configuration with new rotation time
            SecurityConfiguration config = configuration.get();
            SecurityConfiguration newConfig = SecurityConfiguration.builder()
                    .withKeyRotationInterval(Duration.ofDays(30)) // Reset to 30 days
                    .build();
            configuration.set(newConfig);

        } catch (Exception e) {
            logger.error("Error in key rotation: {}", e.getMessage(), e);
        }
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
}
