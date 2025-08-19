package org.openhab.core.ai.common.context;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.auth.AuthenticationContext;

/**
 * Unit tests for the ExecutionContext class and ExecutionContextBuilder.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ExecutionContextTest {

    @Test
    void testExecutionContextBuilder() {
        ExecutionContext context = ExecutionContext.builder().withContextId("test-execution").withProtocol("mcp")
                .withClientId("test-client").withSessionId("test-session").withCorrelationId("test-correlation")
                .withPriority("high").withValue("tool", "test-tool").withMetadata("domain", "automation").build();

        assertEquals("test-execution", context.getContextId());
        assertEquals("execution", context.getContextType());
        assertEquals("1.0.0", context.getVersion());
        assertEquals("mcp", context.getProtocol());
        assertEquals("test-client", context.getClientId());
        assertEquals("test-session", context.getSessionId());
        assertEquals("test-correlation", context.getCorrelationId());
        assertEquals(Optional.of("high"), context.getPriority());
        assertEquals("test-tool", context.getValue("tool"));
        assertEquals("automation", context.getMetadata("domain"));
        assertTrue(context.hasValue("tool"));
        assertTrue(context.hasMetadata("domain"));
    }

    @Test
    void testExecutionContextWithAuthContext() {
        AuthenticationContext authContext = new AuthenticationContext("test-user", "oauth2",
                Map.of("token", "test-token"), java.util.Set.of("read", "write"), Instant.now(), null, "test-session");

        ExecutionContext context = ExecutionContext.builder().withContextId("test-execution").withProtocol("a2a")
                .withClientId("test-client").withSessionId("test-session").withAuthContext(authContext).build();

        assertEquals("a2a", context.getProtocol());
        assertEquals(authContext, context.getAuthContext());
        assertEquals("test-user", context.getAuthContext().getPrincipalId());
        assertTrue(context.getAuthContext().hasPermission("read"));
        assertTrue(context.getAuthContext().hasPermission("write"));
        assertFalse(context.getAuthContext().hasPermission("admin"));
    }

    @Test
    void testExecutionContextTiming() {
        long startTime = System.currentTimeMillis();
        ExecutionContext context = ExecutionContext.builder().withContextId("test-execution").withProtocol("mcp")
                .withClientId("test-client").withSessionId("test-session").withExecutionStartTime(startTime).build();

        assertEquals(startTime, context.getExecutionStartTime());

        // Test execution duration (should be small since we just created it)
        long duration = context.getExecutionDuration();
        assertTrue(duration >= 0);
        assertTrue(duration < 1000); // Should be less than 1 second
    }

    @Test
    void testExecutionContextEquality() {
        ExecutionContext context1 = ExecutionContext.builder().withContextId("test-execution").withProtocol("mcp")
                .withClientId("test-client").withSessionId("test-session").withCorrelationId("test-correlation")
                .withValue("tool", "test-tool").build();

        ExecutionContext context2 = ExecutionContext.builder().withContextId("test-execution").withProtocol("mcp")
                .withClientId("test-client").withSessionId("test-session").withCorrelationId("test-correlation")
                .withValue("tool", "test-tool").build();

        ExecutionContext context3 = ExecutionContext.builder().withContextId("different-execution").withProtocol("mcp")
                .withClientId("test-client").withSessionId("test-session").withCorrelationId("test-correlation")
                .withValue("tool", "test-tool").build();

        assertEquals(context1, context2);
        assertEquals(context1.hashCode(), context2.hashCode());
        assertNotEquals(context1, context3);
        assertNotEquals(context1.hashCode(), context3.hashCode());
    }

    @Test
    void testExecutionContextToString() {
        ExecutionContext context = ExecutionContext.builder().withContextId("test-execution").withProtocol("mcp")
                .withClientId("test-client").withSessionId("test-session").withCorrelationId("test-correlation")
                .build();

        String toString = context.toString();
        assertTrue(toString.contains("test-execution"));
        assertTrue(toString.contains("mcp"));
        assertTrue(toString.contains("test-client"));
        assertTrue(toString.contains("test-session"));
        assertTrue(toString.contains("test-correlation"));
    }

    @Test
    void testExecutionContextBuilderValidation() {
        ExecutionContextBuilder builder = ExecutionContext.builder();

        // Test null validation
        assertThrows(NullPointerException.class, () -> builder.withContextId(null));
        assertThrows(NullPointerException.class, () -> builder.withProtocol(null));
        assertThrows(NullPointerException.class, () -> builder.withClientId(null));
        assertThrows(NullPointerException.class, () -> builder.withSessionId(null));
        assertThrows(NullPointerException.class, () -> builder.withCorrelationId(null));
        assertThrows(NullPointerException.class, () -> builder.withVersion(null));

        // Test that build succeeds with valid data
        ExecutionContext context = builder.withContextId("test-execution").withProtocol("mcp")
                .withClientId("test-client").withSessionId("test-session").withCorrelationId("test-correlation")
                .build();

        assertNotNull(context);
        assertEquals("test-execution", context.getContextId());
    }

    @Test
    void testExecutionContextDefaultValues() {
        ExecutionContext context = ExecutionContext.builder().build();

        // Test default values
        assertNotNull(context.getContextId()); // Should be a UUID
        assertEquals("execution", context.getContextType());
        assertEquals("1.0.0", context.getVersion());
        assertEquals("unknown", context.getProtocol());
        assertEquals("unknown", context.getClientId());
        assertNotNull(context.getSessionId()); // Should be a UUID
        assertNotNull(context.getCorrelationId()); // Should be a UUID
        assertEquals(Optional.empty(), context.getPriority());
        assertNull(context.getAuthContext());
        assertTrue(context.getExecutionStartTime() > 0);
    }

    @Test
    void testExecutionContextWithCustomTimestamps() {
        Instant createdAt = Instant.now().minusSeconds(3600); // 1 hour ago
        Instant lastModifiedAt = Instant.now().minusSeconds(1800); // 30 minutes ago

        ExecutionContext context = ExecutionContext.builder().withContextId("test-execution").withProtocol("mcp")
                .withClientId("test-client").withSessionId("test-session").withCreatedAt(createdAt)
                .withLastModifiedAt(lastModifiedAt).build();

        assertEquals(createdAt, context.getCreatedAt());
        assertEquals(lastModifiedAt, context.getLastModifiedAt());
    }
}
