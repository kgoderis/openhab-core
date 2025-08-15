package org.openhab.core.ai.agent.infrastructure.performance;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public record PerformanceOptimizationSuggestion(String type, String suggestion, PerformanceImpact impact) {
}
