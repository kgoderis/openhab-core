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

import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link NegotiationProposal}.
 *
 * @author Karel Goderis - Initial Contribution
 */
class NegotiationProposalTest {

    @Test
    void testBuilderCreation() {
        NegotiationProposal proposal = NegotiationProposal.builder().withSessionId("session-123").withAgentId("agent-1")
                .withProposal(Map.of("key", "value")).withSubmittedAt(Instant.now()).build();

        assertEquals("session-123", proposal.getSessionId());
        assertEquals("agent-1", proposal.getAgentId());
        assertEquals(Map.of("key", "value"), proposal.getProposal());
    }

    @Test
    void testDefaultValues() {
        NegotiationProposal proposal = NegotiationProposal.builder().withSessionId("session-123").withAgentId("agent-1")
                .withProposal(Map.of()).build();

        assertTrue(proposal.getSubmittedAt().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void testToBuilder() {
        NegotiationProposal original = NegotiationProposal.builder().withSessionId("session-123").withAgentId("agent-1")
                .withProposal(Map.of("key", "value")).build();

        NegotiationProposal modified = original.toBuilder().withProposal(Map.of("newKey", "newValue")).build();

        assertEquals("session-123", modified.getSessionId());
        assertEquals("agent-1", modified.getAgentId());
        assertEquals(Map.of("newKey", "newValue"), modified.getProposal());

        // Original should remain unchanged
        assertEquals(Map.of("key", "value"), original.getProposal());
    }

    @Test
    void testValidation() {
        // Test blank sessionId
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationProposal.builder().withSessionId("").withAgentId("agent-1").withProposal(Map.of()).build();
        });

        // Test blank agentId
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationProposal.builder().withSessionId("session-123").withAgentId("").withProposal(Map.of()).build();
        });
    }

    @Test
    void testImmutability() {
        Map<String, Object> proposalData = Map.of("key", "value");

        NegotiationProposal proposal = NegotiationProposal.builder().withSessionId("session-123").withAgentId("agent-1")
                .withProposal(proposalData).build();

        // Verify collections are immutable
        assertThrows(UnsupportedOperationException.class, () -> {
            proposal.getProposal().put("newKey", "newValue");
        });
    }

    @Test
    void testNullHandling() {
        // Test null sessionId
        assertThrows(NullPointerException.class, () -> {
            NegotiationProposal.builder().withSessionId(null).withAgentId("agent-1").withProposal(Map.of()).build();
        });

        // Test null agentId
        assertThrows(NullPointerException.class, () -> {
            NegotiationProposal.builder().withSessionId("session-123").withAgentId(null).withProposal(Map.of()).build();
        });

        // Test null proposal
        assertThrows(NullPointerException.class, () -> {
            NegotiationProposal.builder().withSessionId("session-123").withAgentId("agent-1").withProposal(null)
                    .build();
        });

        // Test null submittedAt
        assertThrows(NullPointerException.class, () -> {
            NegotiationProposal.builder().withSessionId("session-123").withAgentId("agent-1").withProposal(Map.of())
                    .withSubmittedAt(null).build();
        });
    }

    @Test
    void testEqualityAndHashCode() {
        NegotiationProposal proposal1 = NegotiationProposal.builder().withSessionId("session-123")
                .withAgentId("agent-1").withProposal(Map.of("key", "value")).build();

        NegotiationProposal proposal2 = NegotiationProposal.builder().withSessionId("session-123")
                .withAgentId("agent-1").withProposal(Map.of("key", "value")).build();

        NegotiationProposal proposal3 = NegotiationProposal.builder().withSessionId("different-session")
                .withAgentId("agent-1").withProposal(Map.of("key", "value")).build();

        assertEquals(proposal1, proposal2);
        assertEquals(proposal1.hashCode(), proposal2.hashCode());
        assertNotEquals(proposal1, proposal3);
        assertNotEquals(proposal1.hashCode(), proposal3.hashCode());
    }

    @Test
    void testBuilderReuse() {
        NegotiationProposal.Builder builder = NegotiationProposal.builder().withSessionId("session-123")
                .withAgentId("agent-1").withProposal(Map.of());

        NegotiationProposal proposal1 = builder.build();
        assertNotNull(proposal1);

        // Reset and build another object
        NegotiationProposal proposal2 = builder.withSessionId("session-new").withAgentId("agent-new")
                .withProposal(Map.of("newKey", "newValue")).build();

        assertNotNull(proposal2);
        assertNotEquals(proposal1.getSessionId(), proposal2.getSessionId());
    }
}
