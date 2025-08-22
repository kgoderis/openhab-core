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
package org.openhab.core.ai.tool.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link ToolMetadata}.
 *
 * @author Karel Goderis - Initial Contribution
 */
class ToolMetadataTest {

    @Test
    void testBuilderCreation() {
        ToolMetadata metadata = ToolMetadata.builder().withVersion("2.0.0").withAuthor("Test Author")
                .withDescription("Test description").build();

        assertEquals("2.0.0", metadata.getVersion());
        assertEquals("Test Author", metadata.getAuthor());
        assertEquals("Test description", metadata.getDescription());
    }

    @Test
    void testDefaultValues() {
        ToolMetadata metadata = ToolMetadata.builder().build();

        assertEquals("1.0.0", metadata.getVersion());
        assertEquals("Unknown", metadata.getAuthor());
        assertEquals("No description provided", metadata.getDescription());
    }

    @Test
    void testToBuilder() {
        ToolMetadata original = ToolMetadata.builder().withVersion("1.0.0").withAuthor("Original Author")
                .withDescription("Original description").build();

        ToolMetadata modified = original.toBuilder().withVersion("2.0.0").withAuthor("Modified Author").build();

        assertEquals("2.0.0", modified.getVersion());
        assertEquals("Modified Author", modified.getAuthor());
        assertEquals("Original description", modified.getDescription());

        // Original should remain unchanged
        assertEquals("1.0.0", original.getVersion());
        assertEquals("Original Author", original.getAuthor());
        assertEquals("Original description", original.getDescription());
    }

    @Test
    void testValidation() {
        // Test blank version
        assertThrows(IllegalArgumentException.class, () -> {
            ToolMetadata.builder().withVersion("").withAuthor("Test Author").withDescription("Test description")
                    .build();
        });

        // Test blank author
        assertThrows(IllegalArgumentException.class, () -> {
            ToolMetadata.builder().withVersion("1.0.0").withAuthor("").withDescription("Test description").build();
        });

        // Test blank description
        assertThrows(IllegalArgumentException.class, () -> {
            ToolMetadata.builder().withVersion("1.0.0").withAuthor("Test Author").withDescription("").build();
        });
    }

    @Test
    void testNullHandling() {
        // Test null version
        assertThrows(NullPointerException.class, () -> {
            ToolMetadata.builder().withVersion(null).build();
        });

        // Test null author
        assertThrows(NullPointerException.class, () -> {
            ToolMetadata.builder().withAuthor(null).build();
        });

        // Test null description
        assertThrows(NullPointerException.class, () -> {
            ToolMetadata.builder().withDescription(null).build();
        });
    }

    @Test
    void testEqualityAndHashCode() {
        ToolMetadata metadata1 = ToolMetadata.builder().withVersion("1.0.0").withAuthor("Test Author")
                .withDescription("Test description").build();

        ToolMetadata metadata2 = ToolMetadata.builder().withVersion("1.0.0").withAuthor("Test Author")
                .withDescription("Test description").build();

        assertEquals(metadata1, metadata2);
        assertEquals(metadata1.hashCode(), metadata2.hashCode());
    }

    @Test
    void testBuilderReuse() {
        ToolMetadata.Builder builder = ToolMetadata.builder().withVersion("1.0.0").withAuthor("Test Author")
                .withDescription("Test description");

        ToolMetadata metadata1 = builder.build();
        ToolMetadata metadata2 = builder.build();

        assertEquals(metadata1, metadata2);
    }
}
