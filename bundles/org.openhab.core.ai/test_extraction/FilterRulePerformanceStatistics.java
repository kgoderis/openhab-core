package org.openhab.core.ai.events;

import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;

    public interface FilterRulePerformanceStatistics {
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