package org.openhab.core.ai.events;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;

/**
 * Event Filter - Advanced event filtering system
 * 
 * This interface provides comprehensive event filtering capabilities including:
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
     * Check if an event should be processed based on filtering rules
     * 
     * @param event The event to check
     * @return true if the event should be processed, false if it should be filtered out
     */
    boolean shouldProcess(Event event);

    /**
     * Add a filter rule to the filter chain
     * 
     * @param filterRule The filter rule to add
     */
    void addFilterRule(FilterRule filterRule);

    /**
     * Remove a filter rule from the filter chain
     * 
     * @param filterRuleId The ID of the filter rule to remove
     */
    void removeFilterRule(String filterRuleId);

    /**
     * Get all active filter rules
     * 
     * @return List of active filter rules
     */
    List<FilterRule> getFilterRules();

    /**
     * Enable or disable a specific filter rule
     * 
     * @param filterRuleId The ID of the filter rule
     * @param enabled Whether to enable or disable the rule
     */
    void setFilterRuleEnabled(String filterRuleId, boolean enabled);

    /**
     * Get filter performance statistics
     * 
     * @return Filter performance statistics
     */
    FilterPerformanceStatistics getPerformanceStatistics();

    /**
     * Reset filter performance statistics
     */
    void resetPerformanceStatistics();

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
         * Get the name of this filter rule
         * 
         * @return The filter rule name
         */
        String getName();

        /**
         * Get the description of this filter rule
         * 
         * @return The filter rule description
         */
        String getDescription();

        /**
         * Check if this filter rule is enabled
         * 
         * @return true if enabled, false if disabled
         */
        boolean isEnabled();

        /**
         * Get the priority of this filter rule (lower numbers = higher priority)
         * 
         * @return The filter rule priority
         */
        int getPriority();

        /**
         * Get the filter type
         * 
         * @return The filter type
         */
        FilterType getType();

        /**
         * Get the filter configuration
         * 
         * @return The filter configuration
         */
        Map<String, Object> getConfiguration();

        /**
         * Apply this filter rule to an event
         * 
         * @param event The event to filter
         * @return true if the event passes the filter, false if it should be filtered out
         */
        boolean apply(Event event);
    }

    /**
     * Filter types
     */
    enum FilterType {
        /** Priority-based filtering */
        PRIORITY,
        /** Pattern-based filtering */
        PATTERN,
        /** Sampling-based filtering */
        SAMPLING,
        /** Time-based filtering */
        TIME_BASED,
        /** Source-based filtering */
        SOURCE_BASED,
        /** Type-based filtering */
        TYPE_BASED,
        /** Custom filtering */
        CUSTOM
    }

    /**
     * Filter performance statistics
     */
    interface FilterPerformanceStatistics {
        /**
         * Get total events processed
         * 
         * @return Total events processed
         */
        long getTotalEventsProcessed();

        /**
         * Get total events filtered out
         * 
         * @return Total events filtered out
         */
        long getTotalEventsFiltered();

        /**
         * Get filter rate (percentage of events filtered out)
         * 
         * @return Filter rate as a percentage
         */
        double getFilterRate();

        /**
         * Get average processing time per event in milliseconds
         * 
         * @return Average processing time
         */
        double getAverageProcessingTimeMs();

        /**
         * Get total processing time in milliseconds
         * 
         * @return Total processing time
         */
        long getTotalProcessingTimeMs();

        /**
         * Get performance statistics for a specific filter rule
         * 
         * @param filterRuleId The filter rule ID
         * @return Performance statistics for the filter rule
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
         * Get total events processed by this rule
         * 
         * @return Total events processed
         */
        long getTotalEventsProcessed();

        /**
         * Get total events filtered out by this rule
         * 
         * @return Total events filtered out
         */
        long getTotalEventsFiltered();

        /**
         * Get filter rate for this rule
         * 
         * @return Filter rate as a percentage
         */
        double getFilterRate();

        /**
         * Get average processing time for this rule in milliseconds
         * 
         * @return Average processing time
         */
        double getAverageProcessingTimeMs();

        /**
         * Get total processing time for this rule in milliseconds
         * 
         * @return Total processing time
         */
        long getTotalProcessingTimeMs();
    }
}
