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

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.events.EventFilter;
import org.openhab.core.events.Event;

/**
 * Unit tests for EventFilter interface
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class EventFilterTest {

    @Test
    void testFilterRuleInterface() {
        // Test filter rule interface methods
        EventFilter.FilterRule rule = new TestFilterRule("test-rule", "Test Rule", "Test description", true, 1,
                EventFilter.FilterType.PRIORITY);

        assertEquals("test-rule", rule.getId());
        assertEquals("Test Rule", rule.getName());
        assertEquals("Test description", rule.getDescription());
        assertTrue(rule.isEnabled());
        assertEquals(1, rule.getPriority());
        assertEquals(EventFilter.FilterType.PRIORITY, rule.getType());
        assertNotNull(rule.getConfiguration());
    }

    @Test
    void testFilterTypeEnum() {
        // Test all filter types
        EventFilter.FilterType[] types = EventFilter.FilterType.values();
        assertEquals(7, types.length);

        assertTrue(contains(types, EventFilter.FilterType.PRIORITY));
        assertTrue(contains(types, EventFilter.FilterType.PATTERN));
        assertTrue(contains(types, EventFilter.FilterType.SAMPLING));
        assertTrue(contains(types, EventFilter.FilterType.TIME_BASED));
        assertTrue(contains(types, EventFilter.FilterType.SOURCE_BASED));
        assertTrue(contains(types, EventFilter.FilterType.TYPE_BASED));
        assertTrue(contains(types, EventFilter.FilterType.CUSTOM));
    }

    @Test
    void testFilterPerformanceStatisticsInterface() {
        // Test filter performance statistics interface
        EventFilter.FilterPerformanceStatistics stats = new TestFilterPerformanceStatistics();

        assertEquals(0, stats.getTotalEventsProcessed());
        assertEquals(0, stats.getTotalEventsFiltered());
        assertEquals(0.0, stats.getFilterRate(), 0.001);
        assertEquals(0.0, stats.getAverageProcessingTimeMs(), 0.001);
        assertEquals(0, stats.getTotalProcessingTimeMs());
        assertNull(stats.getFilterRuleStatistics("test-rule"));
    }

    @Test
    void testFilterRulePerformanceStatisticsInterface() {
        // Test filter rule performance statistics interface
        EventFilter.FilterRulePerformanceStatistics ruleStats = new TestFilterRulePerformanceStatistics("test-rule");

        assertEquals("test-rule", ruleStats.getFilterRuleId());
        assertEquals(0, ruleStats.getTotalEventsProcessed());
        assertEquals(0, ruleStats.getTotalEventsFiltered());
        assertEquals(0.0, ruleStats.getFilterRate(), 0.001);
        assertEquals(0.0, ruleStats.getAverageProcessingTimeMs(), 0.001);
        assertEquals(0, ruleStats.getTotalProcessingTimeMs());
    }

    private boolean contains(EventFilter.FilterType[] types, EventFilter.FilterType type) {
        for (EventFilter.FilterType t : types) {
            if (t == type) {
                return true;
            }
        }
        return false;
    }

    // Test implementations for interface testing
    private static class TestFilterRule implements EventFilter.FilterRule {
        private final String id;
        private final String name;
        private final String description;
        private final boolean enabled;
        private final int priority;
        private final EventFilter.FilterType type;

        public TestFilterRule(String id, String name, String description, boolean enabled, int priority,
                EventFilter.FilterType type) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.enabled = enabled;
            this.priority = priority;
            this.type = type;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public EventFilter.FilterType getType() {
            return type;
        }

        @Override
        public Map<String, Object> getConfiguration() {
            return Map.of("test", "value");
        }

        @Override
        public boolean apply(Event event) {
            return true; // Always pass for testing
        }
    }

    private static class TestFilterPerformanceStatistics implements EventFilter.FilterPerformanceStatistics {
        @Override
        public long getTotalEventsProcessed() {
            return 0;
        }

        @Override
        public long getTotalEventsFiltered() {
            return 0;
        }

        @Override
        public double getFilterRate() {
            return 0.0;
        }

        @Override
        public double getAverageProcessingTimeMs() {
            return 0.0;
        }

        @Override
        public long getTotalProcessingTimeMs() {
            return 0;
        }

        @Override
        public EventFilter.FilterRulePerformanceStatistics getFilterRuleStatistics(String filterRuleId) {
            return null;
        }
    }

    private static class TestFilterRulePerformanceStatistics implements EventFilter.FilterRulePerformanceStatistics {
        private final String filterRuleId;

        public TestFilterRulePerformanceStatistics(String filterRuleId) {
            this.filterRuleId = filterRuleId;
        }

        @Override
        public String getFilterRuleId() {
            return filterRuleId;
        }

        @Override
        public long getTotalEventsProcessed() {
            return 0;
        }

        @Override
        public long getTotalEventsFiltered() {
            return 0;
        }

        @Override
        public double getFilterRate() {
            return 0.0;
        }

        @Override
        public double getAverageProcessingTimeMs() {
            return 0.0;
        }

        @Override
        public long getTotalProcessingTimeMs() {
            return 0;
        }
    }
}
