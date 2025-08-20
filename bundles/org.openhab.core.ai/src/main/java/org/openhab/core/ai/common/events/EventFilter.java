package org.openhab.core.ai.common.events;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.communication.events.AgentEvent;
import org.openhab.core.events.Event;

/**
 * Unified Event Filter - Comprehensive event filtering system
 * 
 * This interface provides comprehensive event filtering capabilities including:
 * - Basic event delivery filtering for AgentEvents
 * - Advanced event processing filtering for openHAB Events
 * - Priority-based filtering
 * - Pattern-based filtering
 * - Sampling mechanisms
 * - Configurable filters
 * - Filter chains
 * - Performance monitoring
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EventFilter {

    /**
     * Check if an AgentEvent should be delivered based on filtering rules
     * 
     * @param event The AgentEvent to check
     * @return true if the event should be delivered, false if it should be filtered out
     */
    default boolean shouldDeliver(AgentEvent event) {
        return true; // Default implementation allows all events
    }

    /**
     * Check if an openHAB Event should be processed based on filtering rules
     * 
     * @param event The openHAB Event to check
     * @return true if the event should be processed, false if it should be filtered out
     */
    default boolean shouldProcess(Event event) {
        return true; // Default implementation allows all events
    }

    /**
     * Add a filter rule to the filter chain
     * 
     * @param filterRule The filter rule to add
     */
    default void addFilterRule(FilterRule filterRule) {
        // Default implementation does nothing
    }

    /**
     * Remove a filter rule from the filter chain
     * 
     * @param filterRuleId The ID of the filter rule to remove
     */
    default void removeFilterRule(String filterRuleId) {
        // Default implementation does nothing
    }

    /**
     * Get all active filter rules
     * 
     * @return List of active filter rules
     */
    default List<FilterRule> getFilterRules() {
        return List.of(); // Default implementation returns empty list
    }

    /**
     * Enable or disable a specific filter rule
     * 
     * @param filterRuleId The ID of the filter rule
     * @param enabled Whether to enable or disable the rule
     */
    default void setFilterRuleEnabled(String filterRuleId, boolean enabled) {
        // Default implementation does nothing
    }

    /**
     * Get filter performance statistics
     * 
     * @return Filter performance statistics
     */
    default FilterPerformanceStatistics getPerformanceStatistics() {
        return new DefaultFilterPerformanceStatistics(); // Default implementation
    }

    /**
     * Reset filter performance statistics
     */
    default void resetPerformanceStatistics() {
        // Default implementation does nothing
    }

    /**
     * Filter rule definition
     */
    interface FilterRule {
        /**
         * Get the unique identifier for this filter rule
         * 
         * @return The filter rule ID
         */
        String getId();

        /**
         * Get the display name for this filter rule
         * 
         * @return The filter rule name
         */
        String getName();

        /**
         * Get the description for this filter rule
         * 
         * @return The filter rule description
         */
        String getDescription();

        /**
         * Check if this filter rule is enabled
         * 
         * @return true if enabled, false otherwise
         */
        boolean isEnabled();

        /**
         * Get the priority of this filter rule (lower numbers = higher priority)
         * 
         * @return The filter rule priority
         */
        int getPriority();

        /**
         * Get the type of this filter rule
         * 
         * @return The filter rule type
         */
        FilterType getType();

        /**
         * Get the configuration for this filter rule
         * 
         * @return The filter rule configuration
         */
        Map<String, Object> getConfiguration();

        /**
         * Apply this filter rule to an event
         * 
         * @param event The event to filter
         * @return true if the event should pass through, false if it should be filtered out
         */
        default boolean apply(Event event) {
            return true; // Default implementation allows all events
        }
    }

    /**
     * Filter types
     */
    enum FilterType {
        PRIORITY,
        PATTERN,
        SAMPLING,
        TIME_BASED,
        SOURCE_BASED,
        TYPE_BASED,
        CUSTOM
    }

    /**
     * Filter performance statistics
     */
    interface FilterPerformanceStatistics {
        /**
         * Get the total number of events processed
         * 
         * @return Total events processed
         */
        long getTotalEventsProcessed();

        /**
         * Get the total number of events filtered out
         * 
         * @return Total events filtered
         */
        long getTotalEventsFiltered();

        /**
         * Get the filter rate (filtered events / total events)
         * 
         * @return Filter rate as a percentage
         */
        double getFilterRate();

        /**
         * Get the average processing time in milliseconds
         * 
         * @return Average processing time
         */
        double getAverageProcessingTimeMs();

        /**
         * Get the total processing time in milliseconds
         * 
         * @return Total processing time
         */
        long getTotalProcessingTimeMs();

        /**
         * Get statistics for a specific filter rule
         * 
         * @param filterRuleId The filter rule ID
         * @return Filter rule statistics, or null if not found
         */
        @Nullable
        FilterRulePerformanceStatistics getFilterRuleStatistics(String filterRuleId);
    }

    /**
     * Filter rule performance statistics
     */
    interface FilterRulePerformanceStatistics {
        /**
         * Get the filter rule ID
         * 
         * @return The filter rule ID
         */
        String getFilterRuleId();

        /**
         * Get the total number of events processed by this rule
         * 
         * @return Total events processed
         */
        long getTotalEventsProcessed();

        /**
         * Get the total number of events filtered by this rule
         * 
         * @return Total events filtered
         */
        long getTotalEventsFiltered();

        /**
         * Get the filter rate for this rule (filtered events / total events)
         * 
         * @return Filter rate as a percentage
         */
        double getFilterRate();

        /**
         * Get the average processing time for this rule in milliseconds
         * 
         * @return Average processing time
         */
        double getAverageProcessingTimeMs();

        /**
         * Get the total processing time for this rule in milliseconds
         * 
         * @return Total processing time
         */
        long getTotalProcessingTimeMs();
    }

    /**
     * Default implementation of FilterPerformanceStatistics
     */
    class DefaultFilterPerformanceStatistics implements FilterPerformanceStatistics {
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
        public @Nullable FilterRulePerformanceStatistics getFilterRuleStatistics(String filterRuleId) {
            return null;
        }
    }
}
