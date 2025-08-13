package org.openhab.core.ai.tool.manager;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security statistics DTO for tools.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SecurityStatistics {
    private final int totalRequests;
    private final int allowedRequests;
    private final int deniedRequests;
    private final int securityViolations;

    public SecurityStatistics(int totalRequests, int allowedRequests, int deniedRequests, int securityViolations) {
        this.totalRequests = totalRequests;
        this.allowedRequests = allowedRequests;
        this.deniedRequests = deniedRequests;
        this.securityViolations = securityViolations;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public int getAllowedRequests() {
        return allowedRequests;
    }

    public int getDeniedRequests() {
        return deniedRequests;
    }

    public int getSecurityViolations() {
        return securityViolations;
    }
}


