package org.openhab.core.ai.agent.infrastructure.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public record ConfigurationStatistics(long totalConfigurations, long successfulLoads, long failedLoads, long hotReloads,
        int activeConfigurations, int templates, int presets, int versionHistory) {
}
