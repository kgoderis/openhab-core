package org.openhab.core.ai.mcp.unit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.mcp.internal.MCPTransportType;

/**
 * Unit tests for MCPTransportType enum.
 *
 * Tests enum values, methods, and transport type functionality.
 *
 * 
 */
class MCPTransportTypeTest {

    @Test
    void testEnumValues() {
        // Test that the enum has the expected values
        MCPTransportType[] values = MCPTransportType.values();
        assertEquals(2, values.length);

        // Check for STDIO
        assertTrue(containsValue(values, MCPTransportType.STDIO));

        // Check for SSE
        assertTrue(containsValue(values, MCPTransportType.SSE));
    }

    @Test
    void testValueOf() {
        // Test valueOf method with valid values
        assertEquals(MCPTransportType.STDIO, MCPTransportType.valueOf("STDIO"));
        assertEquals(MCPTransportType.SSE, MCPTransportType.valueOf("SSE"));
    }

    @Test
    void testValueOfInvalid() {
        // Test valueOf method with invalid values
        assertThrows(IllegalArgumentException.class, () -> {
            MCPTransportType.valueOf("INVALID");
        });

        assertThrows(NullPointerException.class, () -> {
            MCPTransportType.valueOf(null);
        });
    }

    @Test
    void testEquality() {
        // Test equality
        assertEquals(MCPTransportType.STDIO, MCPTransportType.STDIO);
        assertEquals(MCPTransportType.SSE, MCPTransportType.SSE);
        assertNotEquals(MCPTransportType.STDIO, MCPTransportType.SSE);
    }

    @Test
    void testHashCode() {
        // Test hashCode consistency
        assertEquals(MCPTransportType.STDIO.hashCode(), MCPTransportType.STDIO.hashCode());
        assertEquals(MCPTransportType.SSE.hashCode(), MCPTransportType.SSE.hashCode());
    }

    @Test
    void testToString() {
        // Test toString method
        assertEquals("STDIO", MCPTransportType.STDIO.toString());
        assertEquals("SSE", MCPTransportType.SSE.toString());
    }

    @Test
    void testOrdinal() {
        // Test ordinal values
        assertEquals(0, MCPTransportType.STDIO.ordinal());
        assertEquals(1, MCPTransportType.SSE.ordinal());
    }

    @Test
    void testName() {
        // Test name method
        assertEquals("STDIO", MCPTransportType.STDIO.name());
        assertEquals("SSE", MCPTransportType.SSE.name());
    }

    @Test
    void testCompareTo() {
        // Test compareTo method
        assertTrue(MCPTransportType.STDIO.compareTo(MCPTransportType.SSE) < 0);
        assertTrue(MCPTransportType.SSE.compareTo(MCPTransportType.STDIO) > 0);
        assertEquals(0, MCPTransportType.STDIO.compareTo(MCPTransportType.STDIO));
    }

    @Test
    void testGetDeclaringClass() {
        // Test getDeclaringClass method
        assertEquals(MCPTransportType.class, MCPTransportType.STDIO.getDeclaringClass());
        assertEquals(MCPTransportType.class, MCPTransportType.SSE.getDeclaringClass());
    }

    @Test
    void testIsEnum() {
        // Test isEnum method
        assertTrue(MCPTransportType.STDIO instanceof Enum);
        assertTrue(MCPTransportType.SSE instanceof Enum);
    }

    @Test
    void testSwitchStatement() {
        // Test enum in switch statement
        MCPTransportType transport = MCPTransportType.STDIO;
        String result = switch (transport) {
            case STDIO -> "stdio";
            case SSE -> "sse";
        };
        assertEquals("stdio", result);
    }

    @Test
    void testInCollections() {
        // Test enum in collections
        java.util.Set<MCPTransportType> transports = java.util.Set.of(MCPTransportType.STDIO, MCPTransportType.SSE);
        assertEquals(2, transports.size());
        assertTrue(transports.contains(MCPTransportType.STDIO));
        assertTrue(transports.contains(MCPTransportType.SSE));
    }

    @Test
    void testSerialization() {
        // Test that enum values can be serialized to string and back
        String stdioString = MCPTransportType.STDIO.toString();
        String sseString = MCPTransportType.SSE.toString();

        assertEquals(MCPTransportType.STDIO, MCPTransportType.valueOf(stdioString));
        assertEquals(MCPTransportType.SSE, MCPTransportType.valueOf(sseString));
    }

    @Test
    void testThreadSafety() {
        // Test that enum values are thread-safe (they should be immutable)
        MCPTransportType stdio = MCPTransportType.STDIO;
        MCPTransportType sse = MCPTransportType.SSE;

        // These should always be the same instances
        assertSame(stdio, MCPTransportType.STDIO);
        assertSame(sse, MCPTransportType.SSE);
    }

    @Test
    void testEqualsWithNull() {
        // Test equals with null
        assertNotEquals(null, MCPTransportType.STDIO);
        assertNotEquals(null, MCPTransportType.SSE);
    }

    @Test
    void testEqualsWithDifferentType() {
        // Test equals with different type
        assertNotEquals("STDIO", MCPTransportType.STDIO);
        assertNotEquals("SSE", MCPTransportType.SSE);
    }

    @Test
    void testInheritedObjectMethods() {
        // Test inherited Object methods
        assertNotNull(MCPTransportType.STDIO.getClass());
        assertNotNull(MCPTransportType.SSE.getClass());

        // Test that getClass() returns the correct class
        assertEquals(MCPTransportType.class, MCPTransportType.STDIO.getClass());
        assertEquals(MCPTransportType.class, MCPTransportType.SSE.getClass());
    }

    private boolean containsValue(MCPTransportType[] values, MCPTransportType target) {
        for (MCPTransportType value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}
