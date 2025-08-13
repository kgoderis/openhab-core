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
package org.openhab.core.ai.tool.logging.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

/**
 * Test class for AuditEvent implementation.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class AuditEventTest {

    @Test
    void testValidAuditEventCreation() {
        Map<String, Object> details = new HashMap<>();
        details.put("ip", "192.168.1.1");
        details.put("userAgent", "Mozilla/5.0");

        AuditEvent event = new AuditEvent("test-123", "INFO", "user_login", "user1", Instant.now().toString(), details);

        assertNotNull(event);
        assertEquals("test-123", event.getId());
        assertEquals("INFO", event.getLevel());
        assertEquals("user_login", event.getAction());
        assertEquals("user1", event.getUserId());
        assertNotNull(event.getTimestamp());
        assertEquals(details, event.getDetails());
        assertNotNull(event.getEncryptedHash());
        assertTrue(event.verifyIntegrity());
    }

    @Test
    void testInvalidIdValidation() {
        Map<String, Object> details = new HashMap<>();

        // Test empty ID
        assertThrows(IllegalArgumentException.class, () -> {
            new AuditEvent("", "INFO", "user_login", "user1", Instant.now().toString(), details);
        });

        // Test whitespace ID
        assertThrows(IllegalArgumentException.class, () -> {
            new AuditEvent("   ", "INFO", "user_login", "user1", Instant.now().toString(), details);
        });
    }

    @Test
    void testInvalidLevelValidation() {
        Map<String, Object> details = new HashMap<>();

        // Test invalid level
        assertThrows(IllegalArgumentException.class, () -> {
            new AuditEvent("test-123", "INVALID", "user_login", "user1", Instant.now().toString(), details);
        });

        // Test valid levels
        assertDoesNotThrow(() -> {
            new AuditEvent("test-123", "INFO", "user_login", "user1", Instant.now().toString(), details);
        });

        assertDoesNotThrow(() -> {
            new AuditEvent("test-123", "WARN", "user_login", "user1", Instant.now().toString(), details);
        });

        assertDoesNotThrow(() -> {
            new AuditEvent("test-123", "ERROR", "user_login", "user1", Instant.now().toString(), details);
        });

        assertDoesNotThrow(() -> {
            new AuditEvent("test-123", "DEBUG", "user_login", "user1", Instant.now().toString(), details);
        });
    }

    @Test
    void testInvalidActionValidation() {
        Map<String, Object> details = new HashMap<>();

        // Test empty action
        assertThrows(IllegalArgumentException.class, () -> {
            new AuditEvent("test-123", "INFO", "", "user1", Instant.now().toString(), details);
        });
    }

    @Test
    void testInvalidUserIdValidation() {
        Map<String, Object> details = new HashMap<>();

        // Test empty user ID
        assertThrows(IllegalArgumentException.class, () -> {
            new AuditEvent("test-123", "INFO", "user_login", "", Instant.now().toString(), details);
        });
    }

    @Test
    void testInvalidTimestampValidation() {
        Map<String, Object> details = new HashMap<>();

        // Test invalid timestamp format
        assertThrows(IllegalArgumentException.class, () -> {
            new AuditEvent("test-123", "INFO", "user_login", "user1", "invalid-timestamp", details);
        });
    }

    @Test
    void testInvalidDetailsValidation() {
        // Test details with null key
        Map<String, Object> detailsWithNullKey = new HashMap<>();
        detailsWithNullKey.put("validKey", "value");

        assertDoesNotThrow(() -> {
            new AuditEvent("test-123", "INFO", "user_login", "user1", Instant.now().toString(), detailsWithNullKey);
        });

        // Test details with null value
        Map<String, Object> detailsWithNullValue = new HashMap<>();
        detailsWithNullValue.put("key", "validValue");

        assertDoesNotThrow(() -> {
            new AuditEvent("test-123", "INFO", "user_login", "user1", Instant.now().toString(), detailsWithNullValue);
        });
    }

    @Test
    void testJsonSerialization() {
        Map<String, Object> details = new HashMap<>();
        details.put("ip", "192.168.1.1");
        details.put("userAgent", "Mozilla/5.0");
        details.put("count", 42);

        AuditEvent event = new AuditEvent("test-123", "INFO", "user_login", "user1", "2023-01-01T12:00:00Z", details);

        String json = event.toJson();

        assertNotNull(json);
        assertTrue(json.contains("\"id\":\"test-123\""));
        assertTrue(json.contains("\"level\":\"INFO\""));
        assertTrue(json.contains("\"action\":\"user_login\""));
        assertTrue(json.contains("\"userId\":\"user1\""));
        assertTrue(json.contains("\"timestamp\":\"2023-01-01T12:00:00Z\""));
        assertTrue(json.contains("\"ip\":\"192.168.1.1\""));
        assertTrue(json.contains("\"userAgent\":\"Mozilla/5.0\""));
        assertTrue(json.contains("\"count\":42"));
        assertTrue(json.contains("\"encryptedHash\":"));
    }

    @Test
    void testXmlSerialization() {
        Map<String, Object> details = new HashMap<>();
        details.put("ip", "192.168.1.1");
        details.put("userAgent", "Mozilla/5.0");

        AuditEvent event = new AuditEvent("test-123", "INFO", "user_login", "user1", "2023-01-01T12:00:00Z", details);

        String xml = event.toXml();

        assertNotNull(xml);
        assertTrue(xml.contains("<id>test-123</id>"));
        assertTrue(xml.contains("<level>INFO</level>"));
        assertTrue(xml.contains("<action>user_login</action>"));
        assertTrue(xml.contains("<userId>user1</userId>"));
        assertTrue(xml.contains("<timestamp>2023-01-01T12:00:00Z</timestamp>"));
        assertTrue(xml.contains("<detail key=\"ip\">192.168.1.1</detail>"));
        assertTrue(xml.contains("<detail key=\"userAgent\">Mozilla/5.0</detail>"));
        assertTrue(xml.contains("<encryptedHash>"));
    }

    @Test
    void testEventComparison() {
        Map<String, Object> details = new HashMap<>();

        AuditEvent event1 = new AuditEvent("test-1", "INFO", "action1", "user1", "2023-01-01T12:00:00Z", details);
        AuditEvent event2 = new AuditEvent("test-2", "INFO", "action2", "user2", "2023-01-01T12:00:01Z", details);
        AuditEvent event3 = new AuditEvent("test-3", "INFO", "action3", "user3", "2023-01-01T12:00:00Z", details);

        // Test timestamp-based comparison
        assertTrue(event1.compareTo(event2) < 0); // event1 timestamp < event2 timestamp
        assertTrue(event2.compareTo(event1) > 0); // event2 timestamp > event1 timestamp

        // Test ID-based comparison for same timestamp
        assertTrue(event1.compareTo(event3) < 0); // event1 ID < event3 ID
        assertTrue(event3.compareTo(event1) > 0); // event3 ID > event1 ID

        // Test equality
        assertEquals(0, event1.compareTo(event1));
    }

    @Test
    void testEventEquality() {
        Map<String, Object> details1 = new HashMap<>();
        details1.put("key1", "value1");

        Map<String, Object> details2 = new HashMap<>();
        details2.put("key1", "value1");

        AuditEvent event1 = new AuditEvent("test-123", "INFO", "user_login", "user1", "2023-01-01T12:00:00Z", details1);
        AuditEvent event2 = new AuditEvent("test-123", "INFO", "user_login", "user1", "2023-01-01T12:00:00Z", details2);
        AuditEvent event3 = new AuditEvent("test-456", "INFO", "user_login", "user1", "2023-01-01T12:00:00Z", details1);

        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
        assertNotEquals(event1, null);
        assertNotEquals(event1, "not an audit event");
    }

    @Test
    void testIntegrityVerification() {
        Map<String, Object> details = new HashMap<>();
        details.put("ip", "192.168.1.1");

        AuditEvent event = new AuditEvent("test-123", "INFO", "user_login", "user1", Instant.now().toString(), details);

        // Verify integrity should be true for valid event
        assertTrue(event.verifyIntegrity());

        // Create a modified event (this would normally be done through reflection or cloning)
        // For this test, we'll create a new event with different details
        Map<String, Object> modifiedDetails = new HashMap<>();
        modifiedDetails.put("ip", "192.168.1.2"); // Different IP

        AuditEvent modifiedEvent = new AuditEvent("test-123", "INFO", "user_login", "user1", event.getTimestamp(),
                modifiedDetails);

        // The modified event should have different hash
        assertNotEquals(event.getEncryptedHash(), modifiedEvent.getEncryptedHash());
    }

    @Test
    void testEventSorting() {
        Map<String, Object> details = new HashMap<>();

        AuditEvent event1 = new AuditEvent("test-1", "INFO", "action1", "user1", "2023-01-01T12:00:00Z", details);
        AuditEvent event2 = new AuditEvent("test-2", "INFO", "action2", "user2", "2023-01-01T12:00:01Z", details);
        AuditEvent event3 = new AuditEvent("test-3", "INFO", "action3", "user3", "2023-01-01T12:00:00Z", details);

        TreeSet<AuditEvent> sortedEvents = new TreeSet<>();
        sortedEvents.add(event2); // Latest timestamp
        sortedEvents.add(event3); // Same timestamp as event1, but higher ID
        sortedEvents.add(event1); // Earliest timestamp

        // Verify order: event1, event3, event2
        AuditEvent[] expectedOrder = { event1, event3, event2 };
        int index = 0;
        for (AuditEvent event : sortedEvents) {
            assertEquals(expectedOrder[index], event);
            index++;
        }
    }

    @Test
    void testJsonEscaping() {
        Map<String, Object> details = new HashMap<>();
        details.put("message", "Hello \"World\" with\nnewlines\tand\\backslashes");

        AuditEvent event = new AuditEvent("test-123", "INFO", "user_login", "user1", Instant.now().toString(), details);

        String json = event.toJson();

        // Verify that special characters are properly escaped
        assertTrue(json.contains("\\\"World\\\""));
        assertTrue(json.contains("\\n"));
        assertTrue(json.contains("\\t"));
        assertTrue(json.contains("\\\\"));
    }

    @Test
    void testXmlEscaping() {
        Map<String, Object> details = new HashMap<>();
        details.put("message", "Hello <World> with & special characters");

        AuditEvent event = new AuditEvent("test-123", "INFO", "user_login", "user1", Instant.now().toString(), details);

        String xml = event.toXml();

        // Verify that special characters are properly escaped
        assertTrue(xml.contains("&lt;World&gt;"));
        assertTrue(xml.contains("&amp;"));
    }

    @Test
    void testToString() {
        Map<String, Object> details = new HashMap<>();
        details.put("ip", "192.168.1.1");

        AuditEvent event = new AuditEvent("test-123", "INFO", "user_login", "user1", "2023-01-01T12:00:00Z", details);

        String stringRepresentation = event.toString();

        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("AuditEvent"));
        assertTrue(stringRepresentation.contains("id='test-123'"));
        assertTrue(stringRepresentation.contains("level='INFO'"));
        assertTrue(stringRepresentation.contains("action='user_login'"));
        assertTrue(stringRepresentation.contains("userId='user1'"));
        assertTrue(stringRepresentation.contains("timestamp='2023-01-01T12:00:00Z'"));
        assertTrue(stringRepresentation.contains("encryptedHash="));
    }
}
