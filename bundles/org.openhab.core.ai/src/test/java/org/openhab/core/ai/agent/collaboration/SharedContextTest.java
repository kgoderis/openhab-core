package org.openhab.core.ai.agent.collaboration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the unified SharedContext class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class SharedContextTest {

    @Test
    void testSharedContextBuilder() {
        Instant now = Instant.now();
        List<String> agentIds = List.of("agent1", "agent2");
        Map<String, Object> contextData = Map.of("key1", "value1", "key2", 42);

        SharedContext context = SharedContext.builder().contextId("test-context").agentIds(agentIds)
                .contextData(contextData).accessLevel(ContextAccessLevel.SHARED).createdBy("agent1").createdAt(now)
                .lastModifiedBy("agent2").lastModifiedAt(now).withVersion(1).build();

        assertEquals("test-context", context.getContextId());
        assertEquals(agentIds, context.getAgentIds());
        assertEquals(contextData, context.getContextData());
        assertEquals(ContextAccessLevel.SHARED, context.getAccessLevel());
        assertEquals("agent1", context.getCreatedBy());
        assertEquals(now, context.getCreatedAt());
        assertEquals("agent2", context.getLastModifiedBy());
        assertEquals(now, context.getLastModifiedAt());
        assertEquals(1, context.getVersion());
    }

    @Test
    void testSharedContextAccessControl() {
        List<String> agentIds = List.of("agent1", "agent2");

        SharedContext context = SharedContext.builder().contextId("test-context").agentIds(agentIds)
                .contextData(Map.of()).accessLevel(ContextAccessLevel.PRIVATE).createdBy("agent1")
                .createdAt(Instant.now()).lastModifiedBy("agent1").lastModifiedAt(Instant.now()).withVersion(1).build();

        // Test access control
        assertTrue(context.hasAccess("agent1"));
        assertTrue(context.hasAccess("agent2"));
        assertFalse(context.hasAccess("agent3"));

        // Test write access
        assertTrue(context.hasWriteAccess("agent1"));
        assertTrue(context.hasWriteAccess("agent2"));
        assertFalse(context.hasWriteAccess("agent3"));
    }

    @Test
    void testSharedContextPublicAccess() {
        SharedContext context = SharedContext.builder().contextId("test-context").agentIds(List.of("agent1"))
                .contextData(Map.of()).accessLevel(ContextAccessLevel.PUBLIC).createdBy("agent1")
                .createdAt(Instant.now()).lastModifiedBy("agent1").lastModifiedAt(Instant.now()).withVersion(1).build();

        // Test public access
        assertTrue(context.hasAccess("agent1"));
        assertTrue(context.hasAccess("agent2"));
        assertTrue(context.hasAccess("agent3"));

        // Test write access (still restricted to listed agents)
        assertTrue(context.hasWriteAccess("agent1"));
        assertFalse(context.hasWriteAccess("agent2"));
        assertFalse(context.hasWriteAccess("agent3"));
    }

    @Test
    void testSharedContextToBuilder() {
        SharedContext original = SharedContext.builder().contextId("test-context").agentIds(List.of("agent1"))
                .contextData(Map.of("key", "value")).accessLevel(ContextAccessLevel.PRIVATE).createdBy("agent1")
                .createdAt(Instant.now()).lastModifiedBy("agent1").lastModifiedAt(Instant.now()).withVersion(1).build();

        SharedContext copy = original.toBuilder().build();

        assertEquals(original.getContextId(), copy.getContextId());
        assertEquals(original.getAgentIds(), copy.getAgentIds());
        assertEquals(original.getContextData(), copy.getContextData());
        assertEquals(original.getAccessLevel(), copy.getAccessLevel());
        assertEquals(original.getCreatedBy(), copy.getCreatedBy());
        assertEquals(original.getCreatedAt(), copy.getCreatedAt());
        assertEquals(original.getLastModifiedBy(), copy.getLastModifiedBy());
        assertEquals(original.getLastModifiedAt(), copy.getLastModifiedAt());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    void testSharedContextEquality() {
        Instant now = Instant.now();
        SharedContext context1 = SharedContext.builder().contextId("test-context").agentIds(List.of("agent1"))
                .contextData(Map.of("key", "value")).accessLevel(ContextAccessLevel.PRIVATE).createdBy("agent1")
                .createdAt(now).lastModifiedBy("agent1").lastModifiedAt(now).withVersion(1).build();

        SharedContext context2 = SharedContext.builder().contextId("test-context").agentIds(List.of("agent1"))
                .contextData(Map.of("key", "value")).accessLevel(ContextAccessLevel.PRIVATE).createdBy("agent1")
                .createdAt(now).lastModifiedBy("agent1").lastModifiedAt(now).withVersion(1).build();

        // Should be equal since all fields are the same
        assertEquals(context1, context2);
        assertEquals(context1.hashCode(), context2.hashCode());
    }

    @Test
    void testSharedContextInequality() {
        Instant now = Instant.now();
        SharedContext context1 = SharedContext.builder().contextId("test-context-1").agentIds(List.of("agent1"))
                .contextData(Map.of("key", "value")).accessLevel(ContextAccessLevel.PRIVATE).createdBy("agent1")
                .createdAt(now).lastModifiedBy("agent1").lastModifiedAt(now).withVersion(1).build();

        SharedContext context2 = SharedContext.builder().contextId("test-context-2").agentIds(List.of("agent1"))
                .contextData(Map.of("key", "value")).accessLevel(ContextAccessLevel.PRIVATE).createdBy("agent1")
                .createdAt(now).lastModifiedBy("agent1").lastModifiedAt(now).withVersion(1).build();

        // Should not be equal since context IDs are different
        assertNotEquals(context1, context2);
        assertNotEquals(context1.hashCode(), context2.hashCode());
    }

    @Test
    void testSharedContextToString() {
        SharedContext context = SharedContext.builder().contextId("test-context").agentIds(List.of("agent1"))
                .contextData(Map.of("key", "value")).accessLevel(ContextAccessLevel.PRIVATE).createdBy("agent1")
                .createdAt(Instant.now()).lastModifiedBy("agent1").lastModifiedAt(Instant.now()).withVersion(1).build();

        String toString = context.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("SharedContext"));
        assertTrue(toString.contains("test-context"));
        assertTrue(toString.contains("agent1"));
        assertTrue(toString.contains("PRIVATE"));
    }

    @Test
    void testContextAccessLevelValues() {
        assertEquals(3, ContextAccessLevel.values().length);
        assertTrue(List.of(ContextAccessLevel.values()).contains(ContextAccessLevel.PRIVATE));
        assertTrue(List.of(ContextAccessLevel.values()).contains(ContextAccessLevel.SHARED));
        assertTrue(List.of(ContextAccessLevel.values()).contains(ContextAccessLevel.PUBLIC));
    }

    @Test
    void testContextVersionBuilder() {
        Instant now = Instant.now();
        Map<String, Object> data = Map.of("key", "value");

        ContextVersion version = ContextVersion.builder().versionId("v1").contextId("test-context").agentId("agent1")
                .timestamp(now).data(data).build();

        assertEquals("v1", version.getVersionId());
        assertEquals("test-context", version.getContextId());
        assertEquals("agent1", version.getAgentId());
        assertEquals(now, version.getTimestamp());
        assertEquals(data, version.getData());
        assertNull(version.getOptions());
    }

    @Test
    void testContextOptionsBuilder() {
        ContextOptions options = ContextOptions.builder().expectedVersion(5).persistent(true)
                .ttl(java.time.Duration.ofHours(2)).metadata(Map.of("key", "value")).build();

        assertEquals(5, options.getExpectedVersion());
        assertTrue(options.isPersistent());
        assertEquals(java.time.Duration.ofHours(2), options.getTtl());
        assertEquals(Map.of("key", "value"), options.getMetadata());
    }
}
