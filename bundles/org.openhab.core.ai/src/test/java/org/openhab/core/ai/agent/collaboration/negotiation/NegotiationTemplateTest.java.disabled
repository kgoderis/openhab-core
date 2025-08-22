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

import java.time.Duration;

import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link NegotiationTemplate}.
 *
 * @author Karel Goderis - Initial Contribution
 */
class NegotiationTemplateTest {

    @Test
    void testBuilderCreation() {
        NegotiationTemplate template = NegotiationTemplate.builder().withTemplateId("template-123")
                .withName("Test Template").withDescription("A test negotiation template")
                .withTimeout(Duration.ofMinutes(10)).withMaxRounds(5).build();

        assertEquals("template-123", template.getTemplateId());
        assertEquals("Test Template", template.getName());
        assertEquals("A test negotiation template", template.getDescription());
        assertEquals(Duration.ofMinutes(10), template.getTimeout());
        assertEquals(5, template.getMaxRounds());
    }

    @Test
    void testDefaultValues() {
        NegotiationTemplate template = NegotiationTemplate.builder().withTemplateId("template-123")
                .withName("Test Template").withDescription("A test negotiation template").build();

        assertEquals(Duration.ofMinutes(5), template.getTimeout());
        assertEquals(3, template.getMaxRounds());
    }

    @Test
    void testToBuilder() {
        NegotiationTemplate original = NegotiationTemplate.builder().withTemplateId("template-123")
                .withName("Test Template").withDescription("A test negotiation template")
                .withTimeout(Duration.ofMinutes(10)).withMaxRounds(5).build();

        NegotiationTemplate modified = original.toBuilder().withMaxRounds(10).withTimeout(Duration.ofMinutes(15))
                .build();

        assertEquals("template-123", modified.getTemplateId());
        assertEquals("Test Template", modified.getName());
        assertEquals("A test negotiation template", modified.getDescription());
        assertEquals(Duration.ofMinutes(15), modified.getTimeout());
        assertEquals(10, modified.getMaxRounds());

        // Original should remain unchanged
        assertEquals(5, original.getMaxRounds());
        assertEquals(Duration.ofMinutes(10), original.getTimeout());
    }

    @Test
    void testValidation() {
        // Test blank templateId
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationTemplate.builder().withTemplateId("").withName("Test Template")
                    .withDescription("A test negotiation template").build();
        });

        // Test blank name
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationTemplate.builder().withTemplateId("template-123").withName("")
                    .withDescription("A test negotiation template").build();
        });

        // Test maxRounds <= 0
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationTemplate.builder().withTemplateId("template-123").withName("Test Template")
                    .withDescription("A test negotiation template").withMaxRounds(0).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationTemplate.builder().withTemplateId("template-123").withName("Test Template")
                    .withDescription("A test negotiation template").withMaxRounds(-1).build();
        });

        // Test negative timeout
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationTemplate.builder().withTemplateId("template-123").withName("Test Template")
                    .withDescription("A test negotiation template").withTimeout(Duration.ofMinutes(-1)).build();
        });

        // Test zero timeout
        assertThrows(IllegalArgumentException.class, () -> {
            NegotiationTemplate.builder().withTemplateId("template-123").withName("Test Template")
                    .withDescription("A test negotiation template").withTimeout(Duration.ZERO).build();
        });
    }

    @Test
    void testNullHandling() {
        // Test null templateId
        assertThrows(NullPointerException.class, () -> {
            NegotiationTemplate.builder().withTemplateId(null).withName("Test Template")
                    .withDescription("A test negotiation template").build();
        });

        // Test null name
        assertThrows(NullPointerException.class, () -> {
            NegotiationTemplate.builder().withTemplateId("template-123").withName(null)
                    .withDescription("A test negotiation template").build();
        });

        // Test null description
        assertThrows(NullPointerException.class, () -> {
            NegotiationTemplate.builder().withTemplateId("template-123").withName("Test Template").withDescription(null)
                    .build();
        });

        // Test null timeout
        assertThrows(NullPointerException.class, () -> {
            NegotiationTemplate.builder().withTemplateId("template-123").withName("Test Template")
                    .withDescription("A test negotiation template").withTimeout(null).build();
        });
    }

    @Test
    void testEqualityAndHashCode() {
        NegotiationTemplate template1 = NegotiationTemplate.builder().withTemplateId("template-123")
                .withName("Test Template").withDescription("A test negotiation template")
                .withTimeout(Duration.ofMinutes(10)).withMaxRounds(5).build();

        NegotiationTemplate template2 = NegotiationTemplate.builder().withTemplateId("template-123")
                .withName("Test Template").withDescription("A test negotiation template")
                .withTimeout(Duration.ofMinutes(10)).withMaxRounds(5).build();

        NegotiationTemplate template3 = NegotiationTemplate.builder().withTemplateId("different-template")
                .withName("Test Template").withDescription("A test negotiation template")
                .withTimeout(Duration.ofMinutes(10)).withMaxRounds(5).build();

        assertEquals(template1, template2);
        assertEquals(template1.hashCode(), template2.hashCode());
        assertNotEquals(template1, template3);
        assertNotEquals(template1.hashCode(), template3.hashCode());
    }

    @Test
    void testBuilderReuse() {
        NegotiationTemplate.Builder builder = NegotiationTemplate.builder().withTemplateId("template-123")
                .withName("Test Template").withDescription("A test negotiation template");

        NegotiationTemplate template1 = builder.build();
        assertNotNull(template1);

        // Reset and build another object
        NegotiationTemplate template2 = builder.withTemplateId("template-new").withName("New Template")
                .withMaxRounds(10).build();

        assertNotNull(template2);
        assertNotEquals(template1.getTemplateId(), template2.getTemplateId());
        assertNotEquals(template1.getName(), template2.getName());
        assertNotEquals(template1.getMaxRounds(), template2.getMaxRounds());
    }
}
