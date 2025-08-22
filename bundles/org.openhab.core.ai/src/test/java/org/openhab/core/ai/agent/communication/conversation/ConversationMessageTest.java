/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.agent.communication.conversation;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link ConversationMessage}.
 *
 * @author Karel Goderis - Initial Contribution
 */
class ConversationMessageTest {

    @Test
    void testBuilderCreation() {
        Instant now = Instant.now();
        ConversationMessage message = ConversationMessage.builder().withMessageId("msg-123")
                .withConversationId("conv-456").withFromAgentId("agent-1").withContent("Hello, world!")
                .withMessageType(ConversationMessageType.TEXT).withTimestamp(now).build();

        assertEquals("msg-123", message.getMessageId());
        assertEquals("conv-456", message.getConversationId());
        assertEquals("agent-1", message.getFromAgentId());
        assertEquals("Hello, world!", message.getContent());
        assertEquals(ConversationMessageType.TEXT, message.getMessageType());
        assertEquals(now, message.getTimestamp());
    }

    @Test
    void testDefaultValues() {
        ConversationMessage message = ConversationMessage.builder().withMessageId("msg-123")
                .withConversationId("conv-456").withFromAgentId("agent-1").withContent("Hello").build();

        assertEquals("msg-123", message.getMessageId());
        assertEquals("conv-456", message.getConversationId());
        assertEquals("agent-1", message.getFromAgentId());
        assertEquals("Hello", message.getContent());
        assertEquals(ConversationMessageType.TEXT, message.getMessageType());
        assertNotNull(message.getTimestamp());
    }

    @Test
    void testToBuilder() {
        ConversationMessage original = ConversationMessage.builder().withMessageId("msg-123")
                .withConversationId("conv-456").withFromAgentId("agent-1").withContent("Hello")
                .withMessageType(ConversationMessageType.QUERY).build();

        ConversationMessage modified = original.toBuilder().withContent("Updated content")
                .withMessageType(ConversationMessageType.RESPONSE).build();

        assertEquals("msg-123", modified.getMessageId());
        assertEquals("conv-456", modified.getConversationId());
        assertEquals("agent-1", modified.getFromAgentId());
        assertEquals("Updated content", modified.getContent());
        assertEquals(ConversationMessageType.RESPONSE, modified.getMessageType());

        // Original should remain unchanged
        assertEquals("Hello", original.getContent());
        assertEquals(ConversationMessageType.QUERY, original.getMessageType());
    }

    @Test
    void testValidation() {
        // Test blank messageId
        assertThrows(IllegalArgumentException.class, () -> {
            ConversationMessage.builder().withMessageId("").withConversationId("conv-456").withFromAgentId("agent-1")
                    .withContent("Hello").build();
        });

        // Test blank conversationId
        assertThrows(IllegalArgumentException.class, () -> {
            ConversationMessage.builder().withMessageId("msg-123").withConversationId("").withFromAgentId("agent-1")
                    .withContent("Hello").build();
        });

        // Test blank fromAgentId
        assertThrows(IllegalArgumentException.class, () -> {
            ConversationMessage.builder().withMessageId("msg-123").withConversationId("conv-456").withFromAgentId("")
                    .withContent("Hello").build();
        });

        // Test blank content
        assertThrows(IllegalArgumentException.class, () -> {
            ConversationMessage.builder().withMessageId("msg-123").withConversationId("conv-456")
                    .withFromAgentId("agent-1").withContent("").build();
        });
    }

    @Test
    void testNullHandling() {
        // Test null messageId
        assertThrows(NullPointerException.class, () -> {
            ConversationMessage.builder().withMessageId(null).withConversationId("conv-456").withFromAgentId("agent-1")
                    .withContent("Hello").build();
        });

        // Test null conversationId
        assertThrows(NullPointerException.class, () -> {
            ConversationMessage.builder().withMessageId("msg-123").withConversationId(null).withFromAgentId("agent-1")
                    .withContent("Hello").build();
        });

        // Test null fromAgentId
        assertThrows(NullPointerException.class, () -> {
            ConversationMessage.builder().withMessageId("msg-123").withConversationId("conv-456").withFromAgentId(null)
                    .withContent("Hello").build();
        });

        // Test null content
        assertThrows(NullPointerException.class, () -> {
            ConversationMessage.builder().withMessageId("msg-123").withConversationId("conv-456")
                    .withFromAgentId("agent-1").withContent(null).build();
        });

        // Test null messageType
        assertThrows(NullPointerException.class, () -> {
            ConversationMessage.builder().withMessageId("msg-123").withConversationId("conv-456")
                    .withFromAgentId("agent-1").withContent("Hello").withMessageType(null).build();
        });

        // Test null timestamp
        assertThrows(NullPointerException.class, () -> {
            ConversationMessage.builder().withMessageId("msg-123").withConversationId("conv-456")
                    .withFromAgentId("agent-1").withContent("Hello").withTimestamp(null).build();
        });
    }

    @Test
    void testBuilderReuse() {
        ConversationMessage.Builder builder = ConversationMessage.builder().withMessageId("msg-123")
                .withConversationId("conv-456").withFromAgentId("agent-1").withContent("Hello");

        ConversationMessage message1 = builder.build();
        ConversationMessage message2 = builder.build();

        assertEquals(message1, message2);
    }

    @Test
    void testAllMessageTypes() {
        for (ConversationMessageType type : ConversationMessageType.values()) {
            ConversationMessage message = ConversationMessage.builder().withMessageId("msg-123")
                    .withConversationId("conv-456").withFromAgentId("agent-1").withContent("Hello")
                    .withMessageType(type).build();

            assertEquals(type, message.getMessageType());
        }
    }
}
