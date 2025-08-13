package org.openhab.core.ai.events;

import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.events.Event;

    public interface FilterPerformanceStatistics {
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