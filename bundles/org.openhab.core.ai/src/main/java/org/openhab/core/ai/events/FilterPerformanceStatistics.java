package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public interface FilterPerformanceStatistics {
    long getTotalEventsProcessed();

    long getTotalEventsFiltered();

    double getFilterRate();

    double getAverageProcessingTimeMs();

    long getTotalProcessingTimeMs();

    @Nullable
    FilterRulePerformanceStatistics getFilterRuleStatistics(String filterRuleId);
}
