package org.openhab.core.ai.action.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ActionMetadata.Builder.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ActionMetadataTest {

    @Test
    void testBuilderCreation() {
        ActionMetadata metadata = ActionMetadata.builder().withVersion("2.0.0").withAuthor("Test Author")
                .withDescription("Test description").build();

        assertEquals("2.0.0", metadata.getVersion());
        assertEquals("Test Author", metadata.getAuthor());
        assertEquals("Test description", metadata.getDescription());
    }

    @Test
    void testDefaultValues() {
        ActionMetadata metadata = ActionMetadata.builder().withVersion("1.0.0").withAuthor("Test Author").build();

        assertEquals("1.0.0", metadata.getVersion());
        assertEquals("Test Author", metadata.getAuthor());
        assertEquals("", metadata.getDescription());
        assertTrue(metadata.getTags().isEmpty());
        assertTrue(metadata.getProperties().isEmpty());
        assertTrue(metadata.getExamples().isEmpty());
        assertTrue(metadata.getRequirements().isEmpty());
        assertEquals("", metadata.getDocumentation());
        assertNotNull(metadata.getCreated());
        assertNotNull(metadata.getLastModified());
    }

    @Test
    void testCustomValues() {
        List<String> tags = List.of("automation", "ai");
        Map<String, Object> properties = Map.of("priority", "high", "category", "core");
        List<String> examples = List.of("example1", "example2");
        Map<String, Object> requirements = Map.of("permission", "admin");
        Instant created = Instant.now().minusSeconds(3600);
        Instant lastModified = Instant.now().minusSeconds(1800);

        ActionMetadata metadata = ActionMetadata.builder().withVersion("1.0.0").withAuthor("Test Author")
                .withDescription("Test description").withTags(tags).withProperties(properties).withExamples(examples)
                .withRequirements(requirements).withCreated(created).withLastModified(lastModified)
                .withDocumentation("Test documentation").build();

        assertEquals(tags, metadata.getTags());
        assertEquals(properties, metadata.getProperties());
        assertEquals(examples, metadata.getExamples());
        assertEquals(requirements, metadata.getRequirements());
        assertEquals(created, metadata.getCreated());
        assertEquals(lastModified, metadata.getLastModified());
        assertEquals("Test documentation", metadata.getDocumentation());
    }

    @Test
    void testToBuilder() {
        ActionMetadata original = ActionMetadata.builder().withVersion("1.0.0").withAuthor("Original Author")
                .withDescription("Original description").withTags(List.of("original")).build();

        ActionMetadata modified = original.toBuilder().withAuthor("Modified Author")
                .withDescription("Modified description").withTags(List.of("modified")).build();

        assertEquals("1.0.0", modified.getVersion());
        assertEquals("Modified Author", modified.getAuthor());
        assertEquals("Modified description", modified.getDescription());
        assertEquals(List.of("modified"), modified.getTags());
    }

    @Test
    void testValidation() {
        // Test blank version
        assertThrows(IllegalArgumentException.class, () -> {
            ActionMetadata.builder().withVersion("").withAuthor("Test Author").build();
        });

        // Test blank author
        assertThrows(IllegalArgumentException.class, () -> {
            ActionMetadata.builder().withVersion("1.0.0").withAuthor("").build();
        });

        // Test null version
        assertThrows(NullPointerException.class, () -> {
            ActionMetadata.builder().withVersion(null).withAuthor("Test Author").build();
        });

        // Test null author
        assertThrows(NullPointerException.class, () -> {
            ActionMetadata.builder().withVersion("1.0.0").withAuthor(null).build();
        });
    }

    @Test
    void testImmutability() {
        List<String> originalTags = List.of("tag1", "tag2");
        Map<String, Object> originalProperties = Map.of("key1", "value1");

        ActionMetadata metadata = ActionMetadata.builder().withVersion("1.0.0").withAuthor("Test Author")
                .withTags(originalTags).withProperties(originalProperties).build();

        // Verify that the returned collections are unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            metadata.getTags().add("newTag");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            metadata.getProperties().put("newKey", "newValue");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            metadata.getExamples().add("newExample");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            metadata.getRequirements().put("newReq", "newValue");
        });
    }

    @Test
    void testNullHandling() {
        ActionMetadata metadata = ActionMetadata.builder().withVersion("1.0.0").withAuthor("Test Author").withTags(null)
                .withProperties(null).withExamples(null).withRequirements(null).build();

        assertTrue(metadata.getTags().isEmpty());
        assertTrue(metadata.getProperties().isEmpty());
        assertTrue(metadata.getExamples().isEmpty());
        assertTrue(metadata.getRequirements().isEmpty());
    }

    @Test
    void testCategoryFromTags() {
        ActionMetadata metadata = ActionMetadata.builder().withVersion("1.0.0").withAuthor("Test Author")
                .withTags(List.of("automation", "ai")).build();

        assertEquals("automation", metadata.getCategory());
    }

    @Test
    void testCategoryFromEmptyTags() {
        ActionMetadata metadata = ActionMetadata.builder().withVersion("1.0.0").withAuthor("Test Author")
                .withTags(List.of()).build();

        assertEquals("general", metadata.getCategory());
    }

    @Test
    void testToString() {
        ActionMetadata metadata = ActionMetadata.builder().withVersion("1.0.0").withAuthor("Test Author")
                .withTags(List.of("automation")).build();

        String toString = metadata.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ActionMetadata"));
        assertTrue(toString.contains("1.0.0"));
        assertTrue(toString.contains("Test Author"));
        assertTrue(toString.contains("automation"));
    }

    @Test
    void testBuilderReuse() {
        // Test that builder methods return the same builder instance for chaining
        ActionMetadata.Builder builder = ActionMetadata.builder();
        assertSame(builder, builder.withVersion("1.0.0"));
        assertSame(builder, builder.withAuthor("Test Author"));
        assertSame(builder, builder.withDescription("Test description"));
        assertSame(builder, builder.withTags(List.of("test")));
        assertSame(builder, builder.withProperties(Map.of("key", "value")));
        assertSame(builder, builder.withExamples(List.of("example")));
        assertSame(builder, builder.withRequirements(Map.of("req", "value")));
        assertSame(builder, builder.withDocumentation("doc"));
    }
}
