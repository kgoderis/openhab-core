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
package org.openhab.core.ai.config;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ConfigurationChangeEvent.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class ConfigurationChangeEventTest {

    @Test
    public void testConfigurationChangeEventCreation() {
        // Test basic event creation
        ConfigurationChangeEvent event = new ConfigurationChangeEvent("ai.test.key", "oldValue", "newValue",
                ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                "ai.test");

        assertEquals("ai.test.key", event.getConfigurationKey());
        assertEquals("oldValue", event.getOldValue());
        assertEquals("newValue", event.getNewValue());
        assertEquals(ConfigurationChangeEvent.ChangeType.MODIFIED, event.getChangeType());
        assertEquals(ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG, event.getChangeSource());
        assertEquals("ai.test", event.getDomain());
        assertNotNull(event.getTimestamp());
        assertNull(event.getAdditionalData());
    }

    @Test
    public void testConfigurationChangeEventWithAdditionalData() {
        // Test event creation with additional data
        Map<String, Object> additionalData = Map.of("source", "test", "priority", 1);

        ConfigurationChangeEvent event = new ConfigurationChangeEvent("ai.test.key", "oldValue", "newValue",
                ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.YAML_FILE,
                "ai.test", additionalData);

        assertEquals("ai.test.key", event.getConfigurationKey());
        assertEquals("oldValue", event.getOldValue());
        assertEquals("newValue", event.getNewValue());
        assertEquals(ConfigurationChangeEvent.ChangeType.MODIFIED, event.getChangeType());
        assertEquals(ConfigurationChangeEvent.ChangeSource.YAML_FILE, event.getChangeSource());
        assertEquals("ai.test", event.getDomain());
        assertNotNull(event.getTimestamp());
        assertEquals(additionalData, event.getAdditionalData());
    }

    @Test
    public void testConfigurationChangeEventWithNullValues() {
        // Test event creation with null values
        ConfigurationChangeEvent event = new ConfigurationChangeEvent("ai.test.key", null, null,
                ConfigurationChangeEvent.ChangeType.ADDED, ConfigurationChangeEvent.ChangeSource.ENVIRONMENT,
                "ai.test");

        assertEquals("ai.test.key", event.getConfigurationKey());
        assertNull(event.getOldValue());
        assertNull(event.getNewValue());
        assertEquals(ConfigurationChangeEvent.ChangeType.ADDED, event.getChangeType());
        assertEquals(ConfigurationChangeEvent.ChangeSource.ENVIRONMENT, event.getChangeSource());
        assertEquals("ai.test", event.getDomain());
        assertNotNull(event.getTimestamp());
    }

    @Test
    public void testIsValueChange() {
        // Test value change detection
        ConfigurationChangeEvent modifiedEvent = new ConfigurationChangeEvent("ai.test.key", "oldValue", "newValue",
                ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                "ai.test");
        assertTrue(modifiedEvent.isValueChange());

        // Test when values are the same
        ConfigurationChangeEvent sameValueEvent = new ConfigurationChangeEvent("ai.test.key", "sameValue", "sameValue",
                ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                "ai.test");
        assertFalse(sameValueEvent.isValueChange());

        // Test when not a MODIFIED event
        ConfigurationChangeEvent addedEvent = new ConfigurationChangeEvent("ai.test.key", null, "newValue",
                ConfigurationChangeEvent.ChangeType.ADDED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                "ai.test");
        assertFalse(addedEvent.isValueChange());
    }

    @Test
    public void testAffectsDomain() {
        // Test domain matching
        ConfigurationChangeEvent event = new ConfigurationChangeEvent("ai.test.key", "oldValue", "newValue",
                ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                "ai.test");

        assertTrue(event.affectsDomain("ai.test"));
        assertTrue(event.affectsDomain("ai"));
        assertFalse(event.affectsDomain("ai.other"));
        assertFalse(event.affectsDomain("other"));

        // Test with exact domain match
        ConfigurationChangeEvent exactEvent = new ConfigurationChangeEvent("ai.test", "oldValue", "newValue",
                ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                "ai.test");
        assertTrue(exactEvent.affectsDomain("ai.test"));
    }

    @Test
    public void testToString() {
        // Test toString method
        ConfigurationChangeEvent event = new ConfigurationChangeEvent("ai.test.key", "oldValue", "newValue",
                ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                "ai.test");

        String toString = event.toString();
        assertTrue(toString.contains("ai.test.key"));
        assertTrue(toString.contains("oldValue"));
        assertTrue(toString.contains("newValue"));
        assertTrue(toString.contains("MODIFIED"));
        assertTrue(toString.contains("OSGI_CONFIG"));
        assertTrue(toString.contains("ai.test"));
    }

    @Test
    public void testEqualsAndHashCode() {
        // Test equals and hashCode
        ConfigurationChangeEvent event1 = new ConfigurationChangeEvent("ai.test.key", "oldValue", "newValue",
                ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                "ai.test");

        ConfigurationChangeEvent event2 = new ConfigurationChangeEvent("ai.test.key", "oldValue", "newValue",
                ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                "ai.test");

        ConfigurationChangeEvent event3 = new ConfigurationChangeEvent("ai.test.key", "differentOldValue", "newValue",
                ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                "ai.test");

        // Test equals
        assertEquals(event1, event1); // Same object
        // Note: events with different timestamps are not equal, so event1 and event2 are not equal
        assertNotEquals(event1, event2); // Different timestamps
        assertNotEquals(event1, event3); // Different objects

        // Test hashCode
        assertNotEquals(event1.hashCode(), event2.hashCode()); // Different timestamps
        assertNotEquals(event1.hashCode(), event3.hashCode());

        // Test with null
        assertNotEquals(event1, null);

        // Test with different type
        assertNotEquals(event1, "string");
    }

    @Test
    public void testNullKeyValidation() {
        // Test that null key throws exception
        assertThrows(NullPointerException.class, () -> {
            new ConfigurationChangeEvent(null, "oldValue", "newValue", ConfigurationChangeEvent.ChangeType.MODIFIED,
                    ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG, "ai.test");
        });
    }

    @Test
    public void testNullChangeTypeValidation() {
        // Test that null change type throws exception
        assertThrows(NullPointerException.class, () -> {
            new ConfigurationChangeEvent("ai.test.key", "oldValue", "newValue", null,
                    ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG, "ai.test");
        });
    }

    @Test
    public void testNullChangeSourceValidation() {
        // Test that null change source throws exception
        assertThrows(NullPointerException.class, () -> {
            new ConfigurationChangeEvent("ai.test.key", "oldValue", "newValue",
                    ConfigurationChangeEvent.ChangeType.MODIFIED, null, "ai.test");
        });
    }

    @Test
    public void testNullDomainValidation() {
        // Test that null domain throws exception
        assertThrows(NullPointerException.class, () -> {
            new ConfigurationChangeEvent("ai.test.key", "oldValue", "newValue",
                    ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                    null);
        });
    }

    @Test
    public void testTimestampGeneration() {
        // Test that timestamp is generated correctly
        Instant before = Instant.now();

        ConfigurationChangeEvent event = new ConfigurationChangeEvent("ai.test.key", "oldValue", "newValue",
                ConfigurationChangeEvent.ChangeType.MODIFIED, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG,
                "ai.test");

        Instant after = Instant.now();

        assertTrue(event.getTimestamp().isAfter(before) || event.getTimestamp().equals(before));
        assertTrue(event.getTimestamp().isBefore(after) || event.getTimestamp().equals(after));
    }

    @Test
    public void testAllChangeTypes() {
        // Test all change types
        ConfigurationChangeEvent.ChangeType[] changeTypes = ConfigurationChangeEvent.ChangeType.values();
        assertEquals(4, changeTypes.length);

        assertTrue(contains(changeTypes, ConfigurationChangeEvent.ChangeType.ADDED));
        assertTrue(contains(changeTypes, ConfigurationChangeEvent.ChangeType.MODIFIED));
        assertTrue(contains(changeTypes, ConfigurationChangeEvent.ChangeType.REMOVED));
        assertTrue(contains(changeTypes, ConfigurationChangeEvent.ChangeType.RELOADED));
    }

    @Test
    public void testAllChangeSources() {
        // Test all change sources
        ConfigurationChangeEvent.ChangeSource[] changeSources = ConfigurationChangeEvent.ChangeSource.values();
        assertEquals(4, changeSources.length);

        assertTrue(contains(changeSources, ConfigurationChangeEvent.ChangeSource.ENVIRONMENT));
        assertTrue(contains(changeSources, ConfigurationChangeEvent.ChangeSource.OSGI_CONFIG));
        assertTrue(contains(changeSources, ConfigurationChangeEvent.ChangeSource.YAML_FILE));
        assertTrue(contains(changeSources, ConfigurationChangeEvent.ChangeSource.DEFAULT));
    }

    private <T> boolean contains(T[] array, T value) {
        for (T item : array) {
            if (item.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
