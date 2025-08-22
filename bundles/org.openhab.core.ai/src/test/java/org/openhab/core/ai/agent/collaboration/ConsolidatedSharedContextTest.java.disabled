package org.openhab.core.ai.agent.collaboration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for the consolidated SharedContext system.
 * 
 * Tests that the unified SharedContext, ContextVersion, ContextOptions, and builders
 * work correctly together and provide the expected functionality.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ConsolidatedSharedContextTest {

    @Test
    void testSharedContextBuilder() {
        Instant now = Instant.now();
        List<String> agentIds = List.of("agent1", "agent2");
        Map<String, Object> contextData = Map.of("key1", "value1", "key2", 42);

        SharedContext context = SharedContext.builder().contextId("test-context").agentIds(agentIds)
                .contextData(contextData).accessLevel(ContextAccessLevel.SHARED).createdBy("agent1").createdAt(now)
                .lastModifiedBy("agent2").lastModifiedAt(now.plusSeconds(60)).withVersion(2).build();

        assertEquals("test-context", context.getContextId());
        assertEquals(agentIds, context.getAgentIds());
        assertEquals(contextData, context.getContextData());
        assertEquals(ContextAccessLevel.SHARED, context.getAccessLevel());
        assertEquals("agent1", context.getCreatedBy());
        assertEquals(now, context.getCreatedAt());
        assertEquals("agent2", context.getLastModifiedBy());
        assertEquals(now.plusSeconds(60), context.getLastModifiedAt());
        assertEquals(2, context.getVersion());
    }

    @Test
    void testSharedContextToBuilder() {
        SharedContext original = SharedContext.builder().contextId("original").agentIds(List.of("agent1"))
                .contextData(Map.of("key", "value")).accessLevel(ContextAccessLevel.PRIVATE).createdBy("agent1")
                .withVersion(1).build();

        SharedContext modified = original.toBuilder().contextId("modified").withVersion(2)
                .contextData(Map.of("key", "new-value")).build();

        assertEquals("modified", modified.getContextId());
        assertEquals(2, modified.getVersion());
        assertEquals("new-value", modified.getContextData().get("key"));

        // Original should remain unchanged
        assertEquals("original", original.getContextId());
        assertEquals(1, original.getVersion());
        assertEquals("value", original.getContextData().get("key"));
    }

    @Test
    void testContextVersionBuilder() {
        Instant now = Instant.now();
        Map<String, Object> data = Map.of("version-key", "version-value");
        ContextOptions options = ContextOptions.builder().expectedVersion(1).persistent(true).build();

        ContextVersion version = ContextVersion.builder().versionId("v1").contextId("test-context").agentId("agent1")
                .timestamp(now).data(data).options(options).build();

        assertEquals("v1", version.getVersionId());
        assertEquals("test-context", version.getContextId());
        assertEquals("agent1", version.getAgentId());
        assertEquals(now, version.getTimestamp());
        assertEquals(data, version.getData());
        assertEquals(options, version.getOptions());
    }

    @Test
    void testContextOptionsBuilder() {
        ContextOptions options = ContextOptions.builder().expectedVersion(5).persistent(true)
                .ttl(java.time.Duration.ofHours(2)).metadata(Map.of("meta-key", "meta-value")).build();

        assertEquals(5, options.getExpectedVersion());
        assertTrue(options.isPersistent());
        assertEquals(java.time.Duration.ofHours(2), options.getTtl());
        assertEquals("meta-value", options.getMetadata().get("meta-key"));
    }

    @Test
    void testContextAccessLevel() {
        // Test enum values
        assertEquals("PRIVATE", ContextAccessLevel.PRIVATE.name());
        assertEquals("SHARED", ContextAccessLevel.SHARED.name());
        assertEquals("PUBLIC", ContextAccessLevel.PUBLIC.name());

        // Test ordinal values
        assertEquals(0, ContextAccessLevel.PRIVATE.ordinal());
        assertEquals(1, ContextAccessLevel.SHARED.ordinal());
        assertEquals(2, ContextAccessLevel.PUBLIC.ordinal());
    }

    @Test
    void testSharedContextValidation() {
        // Test that validation works correctly
        assertThrows(IllegalArgumentException.class, () -> {
            SharedContext.builder().contextId("") // Empty context ID should fail
                    .agentIds(List.of("agent1")).build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            SharedContext.builder().contextId("test").agentIds(List.of()) // Empty agent list should fail
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            SharedContext.builder().contextId("test").agentIds(List.of("agent1")).withVersion(-1) // Negative version
                                                                                                  // should
                    // fail
                    .build();
        });
    }

    @Test
    void testContextVersionValidation() {
        // Test that validation works correctly
        assertThrows(IllegalArgumentException.class, () -> {
            ContextVersion.builder().versionId("") // Empty version ID should fail
                    .contextId("test").agentId("agent1").build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ContextVersion.builder().versionId("v1").contextId("") // Empty context ID should fail
                    .agentId("agent1").build();
        });
    }

    @Test
    void testContextOptionsValidation() {
        // Test that validation works correctly
        assertThrows(IllegalArgumentException.class, () -> {
            ContextOptions.builder().expectedVersion(-1) // Negative version should fail
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ContextOptions.builder().ttl(java.time.Duration.ofSeconds(-1)) // Negative TTL should fail
                    .build();
        });
    }

    @Test
    void testSharedContextImmutability() {
        SharedContext context = SharedContext.builder().contextId("test").agentIds(List.of("agent1"))
                .contextData(Map.of("key", "value")).accessLevel(ContextAccessLevel.PRIVATE).createdBy("agent1")
                .withVersion(1).build();

        // Verify that the returned collections are unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            context.getAgentIds().add("agent2");
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            context.getContextData().put("new-key", "new-value");
        });
    }

    @Test
    void testContextVersionImmutability() {
        ContextVersion version = ContextVersion.builder().versionId("v1").contextId("test").agentId("agent1")
                .data(Map.of("key", "value")).build();

        // Verify that the returned data map is unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            version.getData().put("new-key", "new-value");
        });
    }

    @Test
    void testContextOptionsImmutability() {
        ContextOptions options = ContextOptions.builder().expectedVersion(1).persistent(true)
                .metadata(Map.of("key", "value")).build();

        // Verify that the returned metadata map is unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> {
            options.getMetadata().put("new-key", "new-value");
        });
    }

    @Test
    void testSharedContextEquality() {
        SharedContext context1 = SharedContext.builder().contextId("test").agentIds(List.of("agent1"))
                .contextData(Map.of("key", "value")).accessLevel(ContextAccessLevel.PRIVATE).createdBy("agent1")
                .withVersion(1).build();

        SharedContext context2 = SharedContext.builder().contextId("test").agentIds(List.of("agent1"))
                .contextData(Map.of("key", "value")).accessLevel(ContextAccessLevel.PRIVATE).createdBy("agent1")
                .withVersion(1).build();

        assertEquals(context1, context2);
        assertEquals(context1.hashCode(), context2.hashCode());

        // Different context ID should not be equal
        SharedContext context3 = context1.toBuilder().contextId("different").build();

        assertNotEquals(context1, context3);
    }

    @Test
    void testContextVersionEquality() {
        ContextVersion version1 = ContextVersion.builder().versionId("v1").contextId("test").agentId("agent1")
                .data(Map.of("key", "value")).build();

        ContextVersion version2 = ContextVersion.builder().versionId("v1").contextId("test").agentId("agent1")
                .data(Map.of("key", "value")).build();

        assertEquals(version1, version2);
        assertEquals(version1.hashCode(), version2.hashCode());

        // Different version ID should not be equal
        ContextVersion version3 = version1.toBuilder().versionId("v2").build();

        assertNotEquals(version1, version3);
    }

    @Test
    void testContextOptionsEquality() {
        ContextOptions options1 = ContextOptions.builder().expectedVersion(1).persistent(true)
                .metadata(Map.of("key", "value")).build();

        ContextOptions options2 = ContextOptions.builder().expectedVersion(1).persistent(true)
                .metadata(Map.of("key", "value")).build();

        assertEquals(options1, options2);
        assertEquals(options1.hashCode(), options2.hashCode());

        // Different expected version should not be equal
        ContextOptions options3 = options1.toBuilder().expectedVersion(2).build();

        assertNotEquals(options1, options3);
    }
}
