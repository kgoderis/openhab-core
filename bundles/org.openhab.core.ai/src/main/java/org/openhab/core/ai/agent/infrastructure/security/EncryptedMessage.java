package org.openhab.core.ai.agent.infrastructure.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Encrypted message for secure transmission
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class EncryptedMessage {
    private final String encryptedContent;
    private final String signature;
    private final String senderId;
    private final String recipientId;
    private final Instant timestamp;
    private final boolean success;
    private final String errorMessage;

    public EncryptedMessage(String encryptedContent, String signature, String senderId, String recipientId,
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
