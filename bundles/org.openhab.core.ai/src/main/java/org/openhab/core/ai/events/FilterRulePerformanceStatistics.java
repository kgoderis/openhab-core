package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface FilterRulePerformanceStatistics {
    String getFilterRuleId();

    long getTotalEventsProcessed();

    long getTotalEventsFiltered();

    double getFilterRate();

    double getAverageProcessingTimeMs();

    long getTotalProcessingTimeMs();
}
