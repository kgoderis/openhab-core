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
package org.openhab.core.ai.agent.collaboration.negotiation;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link NegotiationSession}.
 *
 * @author Karel Goderis - Initial Contribution
 */
class NegotiationSessionTest {

    @Test
    void testBuilderCreation() {
        NegotiationSession session = NegotiationSession.builder().withSessionId("session-123")
                .withInitiatorId("agent-1").withParticipantIds(Set.of("agent-1", "agent-2"))
                .withTemplateId("template-1").withStrategyId("strategy-1").withInitialProposal(Map.of("key", "value"))
                .withStatus(NegotiationStatus.ACTIVE).withCreatedAt(Instant.now())
                .withTimeoutAt(Instant.now().plusSeconds(300)).build();

        assertEquals("session-123", session.getSessionId());
        assertEquals("agent-1", session.getInitiatorId());
        assertEquals(Set.of("agent-1", "agent-2"), session.getParticipantIds());
        assertEquals("template-1", session.getTemplateId());
        assertEquals("strategy-1", session.getStrategyId());
        assertEquals(Map.of("key", "value"), session.getInitialProposal());
        assertEquals(NegotiationStatus.ACTIVE, session.getStatus());
    }

    @Test
    void testDefaultValues() {
        NegotiationSession session = NegotiationSession.builder().withSessionId("session-123")
                .withInitiatorId("agent-1").withParticipantIds(Set.of("agent-1")).withTemplateId("template-1")
                .withStrategyId("strategy-1").withInitialProposal(Map.of()).build();

        assertEquals(NegotiationStatus.ACTIVE, session.getStatus());
        assertTrue(session.getCreatedAt().isBefore(Instant.now().plusSeconds(1)));
        assertTrue(session.getTimeoutAt().isAfter(Instant.now().plusSeconds(290)));
    }

    @Test
    void testToBuilder() {
        NegotiationSession original = NegotiationSession.builder().withSessionId("session-123")
                .withInitiatorId("agent-1").withParticipantIds(Set.of("agent-1", "agent-2"))
                .withTemplateId("template-1").withStrategyId("strategy-1").withInitialProposal(Map.of("key", "value"))
                .withStatus(NegotiationStatus.ACTIVE).build();

        NegotiationSession modified = original.toBuilder().withStatus(NegotiationStatus.AGREED).build();

        assertEquals("session-123", modified.getSessionId());
        assertEquals("agent-1", modified.getInitiatorId());
        assertEquals(NegotiationStatus.AGREED, modified.getStatus());

        // Original should remain unchanged
        assertEquals(NegotiationStatus.ACTIVE, original.getStatus());
    }

    @Test
    void testValidation() {
        // Test blank sessionId
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationSession.builder().withSessionId("").withInitiatorId("agent-1")
                    .withParticipantIds(Set.of("agent-1")).withTemplateId("template-1").withStrategyId("strategy-1")
                    .withInitialProposal(Map.of()).build();
        });

        // Test blank initiatorId
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationSession.builder().withSessionId("session-123").withInitiatorId("")
                    .withParticipantIds(Set.of("agent-1")).withTemplateId("template-1").withStrategyId("strategy-1")
                    .withInitialProposal(Map.of()).build();
        });

        // Test empty participantIds
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationSession.builder().withSessionId("session-123").withInitiatorId("agent-1")
                    .withParticipantIds(Set.of()).withTemplateId("template-1").withStrategyId("strategy-1")
                    .withInitialProposal(Map.of()).build();
        });

        // Test blank templateId
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationSession.builder().withSessionId("session-123").withInitiatorId("agent-1")
                    .withParticipantIds(Set.of("agent-1")).withTemplateId("").withStrategyId("strategy-1")
                    .withInitialProposal(Map.of()).build();
        });

        // Test blank strategyId
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationSession.builder().withSessionId("session-123").withInitiatorId("agent-1")
                    .withParticipantIds(Set.of("agent-1")).withTemplateId("template-1").withStrategyId("")
                    .withInitialProposal(Map.of()).build();
        });

        // Test timeoutAt before createdAt
        Instant now = Instant.now();
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationSession.builder().withSessionId("session-123").withInitiatorId("agent-1")
                    .withParticipantIds(Set.of("agent-1")).withTemplateId("template-1").withStrategyId("strategy-1")
                    .withInitialProposal(Map.of()).withCreatedAt(now).withTimeoutAt(now.minusSeconds(1)).build();
        });
    }

    @Test
    void testImmutability() {
        Set<String> participantIds = Set.of("agent-1", "agent-2");
        Map<String, Object> initialProposal = Map.of("key", "value");

        NegotiationSession session = NegotiationSession.builder().withSessionId("session-123")
                .withInitiatorId("agent-1").withParticipantIds(participantIds).withTemplateId("template-1")
                .withStrategyId("strategy-1").withInitialProposal(initialProposal).build();

        // Verify collections are immutable
        assertThrows(UnsupportedOperationException.class, () -> {
            session.getParticipantIds().add("agent-3");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            session.getInitialProposal().put("newKey", "newValue");
        });
    }

    @Test
    void testNullHandling() {
        // Test null sessionId
        assertThrows(NullPointerException.class, () -> {
            NegotiationSession.builder().withSessionId(null).withInitiatorId("agent-1")
                    .withParticipantIds(Set.of("agent-1")).withTemplateId("template-1").withStrategyId("strategy-1")
                    .withInitialProposal(Map.of()).build();
        });

        // Test null initiatorId
        assertThrows(NullPointerException.class, () -> {
            NegotiationSession.builder().withSessionId("session-123").withInitiatorId(null)
                    .withParticipantIds(Set.of("agent-1")).withTemplateId("template-1").withStrategyId("strategy-1")
                    .withInitialProposal(Map.of()).build();
        });

        // Test null participantIds
        assertThrows(NullPointerException.class, () -> {
            NegotiationSession.builder().withSessionId("session-123").withInitiatorId("agent-1")
                    .withParticipantIds(null).withTemplateId("template-1").withStrategyId("strategy-1")
                    .withInitialProposal(Map.of()).build();
        });

        // Test null status
        assertThrows(NullPointerException.class, () -> {
            NegotiationSession.builder().withSessionId("session-123").withInitiatorId("agent-1")
                    .withParticipantIds(Set.of("agent-1")).withTemplateId("template-1").withStrategyId("strategy-1")
                    .withInitialProposal(Map.of()).withStatus(null).build();
        });
    }

    @Test
    void testEqualityAndHashCode() {
        NegotiationSession session1 = NegotiationSession.builder().withSessionId("session-123")
                .withInitiatorId("agent-1").withParticipantIds(Set.of("agent-1", "agent-2"))
                .withTemplateId("template-1").withStrategyId("strategy-1").withInitialProposal(Map.of("key", "value"))
                .build();

        NegotiationSession session2 = NegotiationSession.builder().withSessionId("session-123")
                .withInitiatorId("agent-1").withParticipantIds(Set.of("agent-1", "agent-2"))
                .withTemplateId("template-1").withStrategyId("strategy-1").withInitialProposal(Map.of("key", "value"))
                .build();

        assertEquals(session1, session2);
        assertEquals(session1.hashCode(), session2.hashCode());
    }

    @Test
    void testBuilderReuse() {
        NegotiationSession.Builder builder = NegotiationSession.builder().withSessionId("session-123")
                .withInitiatorId("agent-1").withParticipantIds(Set.of("agent-1")).withTemplateId("template-1")
                .withStrategyId("strategy-1").withInitialProposal(Map.of());

        NegotiationSession session1 = builder.build();
        NegotiationSession session2 = builder.build();

        assertEquals(session1, session2);
    }
}
