package org.openhab.core.ai.reasoning.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Model usage tracking for an agent-model combination.
 *
 * Tracks hourly and daily usage counts and reset windows.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
class ModelUsageInfo {
    final String agentId;
    final String modelId;
    final int maxDailyUsage;
    final int maxHourlyUsage;
    int dailyUsage;
    int hourlyUsage;
    long lastDailyReset;
    long lastHourlyReset;

    ModelUsageInfo(String agentId, String modelId, int maxDailyUsage, int maxHourlyUsage) {
        this.agentId = agentId;
        this.modelId = modelId;
        this.maxDailyUsage = maxDailyUsage;
        this.maxHourlyUsage = maxHourlyUsage;
        this.dailyUsage = 0;
        this.hourlyUsage = 0;
        this.lastDailyReset = System.currentTimeMillis();
        this.lastHourlyReset = System.currentTimeMillis();
    }

    void incrementUsage() {
        dailyUsage++;
        hourlyUsage++;
    }

    void resetDailyUsage() {
        dailyUsage = 0;
        lastDailyReset = System.currentTimeMillis();
    }

    void resetHourlyUsage() {
        hourlyUsage = 0;
        lastHourlyReset = System.currentTimeMillis();
    }
}
