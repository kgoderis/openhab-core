package org.openhab.core.ai.events;

import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;

    public interface FilterRule {
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