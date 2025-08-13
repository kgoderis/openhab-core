package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Access control limits and counters per agent.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AccessControl {
    private final String agentId;
    private final boolean enabled;
    private final int dailyLimit;
    private final int hourlyLimit;
    private int dailyRequests;
    private int hourlyRequests;

    public AccessControl(String agentId, boolean enabled, int dailyLimit, int hourlyLimit) {
        this.agentId = agentId;
        this.enabled = enabled;
        this.dailyLimit = dailyLimit;
        this.hourlyLimit = hourlyLimit;
        this.dailyRequests = 0;
        this.hourlyRequests = 0;
    }

    public String getAgentId() {
        return agentId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    public int getHourlyLimit() {
        return hourlyLimit;
    }

    public int getDailyRequests() {
        return dailyRequests;
    }

    public int getHourlyRequests() {
        return hourlyRequests;
    }

    public void incrementDailyRequests() {
        dailyRequests++;
    }

    public void incrementHourlyRequests() {
        hourlyRequests++;
    }

    public void resetDailyRequests() {
        dailyRequests = 0;
    }

    public void resetHourlyRequests() {
        hourlyRequests = 0;
    }
}


