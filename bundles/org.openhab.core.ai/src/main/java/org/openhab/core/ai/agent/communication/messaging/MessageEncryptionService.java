package org.openhab.core.ai.agent.communication.messaging;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.a2a.spec.Message;

/**
 * Message encryption utility.
 *
 * Provides basic hooks for encrypting/decrypting messages.
 *
 * Author: Karel Goderis - Initial Contribution
 * 
 * @since 1.0.0
 */
@NonNullByDefault
public class MessageEncryptionService {
    public Message encryptMessage(Message message) {
        return message;
    }

    public Message decryptMessage(Message message) {
        return message;
    }
}
