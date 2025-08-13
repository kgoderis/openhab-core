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
package org.openhab.core.ai.events;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for EventSystemIntegration
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class EventSystemIntegrationTest {

    private EventSystemIntegration eventSystemIntegration;

    @BeforeEach
    void setUp() {
        eventSystemIntegration = new EventSystemIntegration();
    }

    @Test
    void testGetStatistics() {
        EventSystemIntegration.EventProcessingStatistics stats = eventSystemIntegration.getStatistics();

        assertNotNull(stats);
        assertEquals(0, stats.getTotalEventsProcessed());
        assertEquals(0, stats.getFilteredEvents());
        assertEquals(0, stats.getEnrichedEvents());
        assertEquals(0, stats.getRoutedEvents());
        assertEquals(0, stats.getPersistedEvents());
        assertEquals(0, stats.getReasoningTriggers());
        assertTrue(stats.isEventProcessingEnabled());
        assertTrue(stats.isEventPersistenceEnabled());
        assertTrue(stats.isReasoningIntegrationEnabled());
        assertEquals(Duration.ofDays(30), stats.getEventRetentionPeriod());
        assertFalse(stats.isReplayInProgress());
    }

    @Test
    void testConfigurationMethods() {
        // Test event processing configuration
        eventSystemIntegration.setEnableEventProcessing(false);
        EventSystemIntegration.EventProcessingStatistics stats = eventSystemIntegration.getStatistics();
        assertFalse(stats.isEventProcessingEnabled());

        // Test event persistence configuration
        eventSystemIntegration.setEnableEventPersistence(false);
        stats = eventSystemIntegration.getStatistics();
        assertFalse(stats.isEventPersistenceEnabled());

        // Test reasoning integration configuration
        eventSystemIntegration.setEnableReasoningIntegration(false);
        stats = eventSystemIntegration.getStatistics();
        assertFalse(stats.isReasoningIntegrationEnabled());

        // Test retention period configuration
        Duration newRetentionPeriod = Duration.ofDays(7);
        eventSystemIntegration.setEventRetentionPeriod(newRetentionPeriod);
        stats = eventSystemIntegration.getStatistics();
        assertEquals(newRetentionPeriod, stats.getEventRetentionPeriod());
    }

    @Test
    void testReplayMethods() {
        // Test replay state
        assertFalse(eventSystemIntegration.isReplayInProgress());

        // Test stop replay (should not throw exception)
        eventSystemIntegration.stopReplay();
        assertFalse(eventSystemIntegration.isReplayInProgress());
    }

    @Test
    void testResetStatistics() {
        // Reset statistics
        eventSystemIntegration.resetStatistics();

        EventSystemIntegration.EventProcessingStatistics stats = eventSystemIntegration.getStatistics();
        assertEquals(0, stats.getTotalEventsProcessed());
        assertEquals(0, stats.getFilteredEvents());
        assertEquals(0, stats.getEnrichedEvents());
        assertEquals(0, stats.getRoutedEvents());
        assertEquals(0, stats.getPersistedEvents());
        assertEquals(0, stats.getReasoningTriggers());
    }

    @Test
    void testGetSubscribedEventTypes() {
        // Should subscribe to all event types
        assertEquals(1, eventSystemIntegration.getSubscribedEventTypes().size());
        assertTrue(eventSystemIntegration.getSubscribedEventTypes().contains("*"));
    }
}
