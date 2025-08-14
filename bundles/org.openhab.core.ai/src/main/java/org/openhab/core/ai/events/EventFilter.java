package org.openhab.core.ai.events;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
    // Extracted: org.openhab.core.ai.events.FilterRule

    /**
     * Filter types
     */
    // Extracted: org.openhab.core.ai.events.FilterType

    /**
     * Filter performance statistics
     */
    // Extracted: org.openhab.core.ai.events.FilterPerformanceStatistics

    /**
     * Filter rule performance statistics
     */
    // Extracted: org.openhab.core.ai.events.FilterRulePerformanceStatistics
}
