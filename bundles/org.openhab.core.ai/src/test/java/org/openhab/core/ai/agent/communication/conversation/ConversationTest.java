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
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link Conversation}.
 *
 * @author Karel Goderis - Initial Contribution
 */
class ConversationTest {

    @Test
    void testBuilderCreation() {
        Conversation conversation = Conversation.builder().withConversationId("test-conversation")
                .withParticipantIds(List.of("agent1", "agent2")).withTemplateId("template-1")
                .withContext(Map.of("topic", "home automation")).withStartTime(Instant.now().minusSeconds(3600))
                .withState(ConversationState.ACTIVE).withLastActivity(Instant.now()).withMessageCount(5).build();

        assertEquals("test-conversation", conversation.getConversationId());
        assertTrue(conversation.getParticipantIds().contains("agent1"));
        assertTrue(conversation.getParticipantIds().contains("agent2"));
        assertEquals("template-1", conversation.getTemplateId());
        assertEquals("home automation", conversation.getContext().get("topic"));
        assertEquals(ConversationState.ACTIVE, conversation.getState());
        assertEquals(5, conversation.getMessageCount());
    }

    @Test
    void testDefaultValues() {
        Conversation conversation = Conversation.builder().withConversationId("test-conversation")
                .withParticipantIds(List.of("agent1")).build();

        assertEquals("test-conversation", conversation.getConversationId());
        assertTrue(conversation.getParticipantIds().contains("agent1"));
        assertNull(conversation.getTemplateId());
        assertTrue(conversation.getContext().isEmpty());
        assertEquals(ConversationState.ACTIVE, conversation.getState());
        assertEquals(0, conversation.getMessageCount());
    }

    @Test
    void testToBuilder() {
        Conversation original = Conversation.builder().withConversationId("test-conversation")
                .withParticipantIds(List.of("agent1")).withTemplateId("template-1").withMessageCount(3).build();

        Conversation modified = original.toBuilder().withMessageCount(5).withState(ConversationState.ENDED).build();

        assertEquals("test-conversation", modified.getConversationId());
        assertEquals("template-1", modified.getTemplateId());
        assertEquals(5, modified.getMessageCount());
        assertEquals(ConversationState.ENDED, modified.getState());

        // Original should remain unchanged
        assertEquals(3, original.getMessageCount());
        assertEquals(ConversationState.ACTIVE, original.getState());
    }

    @Test
    void testValidation() {
        // Test blank conversationId
        assertThrows(IllegalArgumentException.class, () -> {
            Conversation.builder().withConversationId("").withParticipantIds(List.of("agent1")).build();
        });

        // Test empty participantIds
        assertThrows(IllegalArgumentException.class, () -> {
            Conversation.builder().withConversationId("test").withParticipantIds(List.of()).build();
        });

        // Test negative messageCount
        assertThrows(IllegalArgumentException.class, () -> {
            Conversation.builder().withConversationId("test").withParticipantIds(List.of("agent1")).withMessageCount(-1)
                    .build();
        });
    }

    @Test
    void testImmutability() {
        Map<String, Object> originalContext = Map.of("topic", "home automation");
        List<String> originalParticipants = List.of("agent1", "agent2");

        Conversation conversation = Conversation.builder().withConversationId("test-conversation")
                .withParticipantIds(originalParticipants).withContext(originalContext).build();

        // Verify collections are immutable
        assertThrows(UnsupportedOperationException.class, () -> {
            conversation.getParticipantIds().add("agent3");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            conversation.getContext().put("newKey", "newValue");
        });
    }

    @Test
    void testNullHandling() {
        // Test null conversationId
        assertThrows(NullPointerException.class, () -> {
            Conversation.builder().withConversationId(null).build();
        });

        // Test null participantIds
        assertThrows(NullPointerException.class, () -> {
            Conversation.builder().withConversationId("test").withParticipantIds(null).build();
        });

        // Test null context
        assertThrows(NullPointerException.class, () -> {
            Conversation.builder().withConversationId("test").withParticipantIds(List.of("agent1")).withContext(null)
                    .build();
        });

        // Test null state
        assertThrows(NullPointerException.class, () -> {
            Conversation.builder().withConversationId("test").withParticipantIds(List.of("agent1")).withState(null)
                    .build();
        });

        // Test null startTime
        assertThrows(NullPointerException.class, () -> {
            Conversation.builder().withConversationId("test").withParticipantIds(List.of("agent1")).withStartTime(null)
                    .build();
        });

        // Test null lastActivity
        assertThrows(NullPointerException.class, () -> {
            Conversation.builder().withConversationId("test").withParticipantIds(List.of("agent1"))
                    .withLastActivity(null).build();
        });
    }

    @Test
    void testBuilderReuse() {
        Conversation.Builder builder = Conversation.builder().withConversationId("test-conversation")
                .withParticipantIds(List.of("agent1"));

        Conversation conversation1 = builder.build();
        Conversation conversation2 = builder.build();

        assertEquals(conversation1, conversation2);
    }
}
