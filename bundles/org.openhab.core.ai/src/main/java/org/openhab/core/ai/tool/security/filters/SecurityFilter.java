package org.openhab.core.ai.tool.security.filters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Authentication filter for MCP tool security.
 * 
 * This interface defines the contract for authentication filters that
 * validate and authenticate requests to the MCP tool server.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface SecurityFilter {

    /**
     * Get the filter ID.
     * 
     * @return the filter ID
     */
    String getFilterId();

    /**
     * Get the filter name.
     * 
     * @return the filter name
     */
    String getFilterName();

    /**
     * Get the filter priority.
     * 
     * @return the filter priority (higher values = higher priority)
     */
    int getPriority();

    /**
     * Check if the filter is enabled.
     * 
     * @return true if the filter is enabled
     */
    boolean isEnabled();

    /**
     * Authenticate a request.
     * 
     * @param request the request to authenticate
     * @return authentication result
     */
    SecurityResult authenticate(Map<String, Object> request);

    /**
     * Get the filter configuration.
     * 
     * @return the filter configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the filter configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    // AbstractSecurityFilter extracted to top-level class org.openhab.core.ai.tool.security.filters.AbstractSecurityFilter
}
