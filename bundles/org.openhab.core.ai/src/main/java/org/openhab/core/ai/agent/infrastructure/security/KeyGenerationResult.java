package org.openhab.core.ai.agent.infrastructure.security;

import java.security.KeyPair;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Key generation result for agent key pair generation
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class KeyGenerationResult {
    private final boolean success;
    private final String agentId;
    private final KeyPair keyPair;
    private final String algorithm;
    private final Instant expiryTime;
    private final String errorMessage;

    public KeyGenerationResult(boolean success, String agentId, KeyPair keyPair, String algorithm, Instant expiryTime,
            String errorMessage) {
        this.success = success;
        this.agentId = agentId;
        this.keyPair = keyPair;
        this.algorithm = algorithm;
        this.expiryTime = expiryTime;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getAgentId() {
        return agentId;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Instant getExpiryTime() {
        return expiryTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
