package org.openhab.core.ai.agent.infrastructure.security;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Decrypted message from secure transmission
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DecryptedMessage {
    private final String decryptedContent;
    private final String senderId;
    private final String recipientId;
    private final Instant timestamp;
    private final boolean signatureValid;
    private final boolean success;
    private final String errorMessage;

    public DecryptedMessage(String decryptedContent, String senderId, String recipientId, Instant timestamp,
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
