package org.openhab.core.ai.common.action;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Dynamic Context Builder - Defines the contract for building dynamic context
 * 
 * <p>
 * This service provides:
 * - Dynamic context generation
 * - Context relevance assessment
 * - Context optimization and caching
 * - Context security and privacy
 * - Context performance monitoring
 * - Context debugging and logging
 * - Context testing and validation
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface DynamicContextBuilder {

    /**
     * Build dynamic context for an agent
     * 
     * @param agentId the agent ID
     * @param trigger the trigger event
     * @return the built context
     */
    AgentContext buildContext(String agentId, Object trigger);

    /**
     * Build context with specific focus areas
     * 
     * @param agentId the agent ID
     * @param trigger the trigger event
     * @param focusAreas the focus areas to include
     * @return the built context
     */
    AgentContext buildContextWithFocus(String agentId, Object trigger, List<String> focusAreas);

    /**
     * Assess context relevance
     * 
     * @param context the context to assess
     * @param trigger the trigger event
     * @return relevance score
     */
    double assessContextRelevance(AgentContext context, Object trigger);

    /**
     * Optimize context for performance
     * 
     * @param context the context to optimize
     * @return optimized context
     */
    AgentContext optimizeContext(AgentContext context);

    /**
     * Cache context for reuse
     * 
     * @param contextKey the context key
     * @param context the context to cache
     */
    void cacheContext(String contextKey, AgentContext context);

    /**
     * Get cached context
     * 
     * @param contextKey the context key
     * @return cached context or null if not found
     */
    AgentContext getCachedContext(String contextKey);

    /**
     * Clear context cache
     */
    void clearContextCache();

    /**
     * Apply security filters to context
     * 
     * @param context the context to filter
     * @param securityLevel the security level
     * @return filtered context
     */
    AgentContext applySecurityFilters(AgentContext context, SecurityLevel securityLevel);

    /**
     * Get context performance metrics
     * 
     * @return performance metrics
     */
    ContextPerformanceMetrics getPerformanceMetrics();

    /**
     * Enable context debugging
     * 
     * @param enabled true to enable debugging
     */
    void setDebuggingEnabled(boolean enabled);

    /**
     * Get context debug logs
     * 
     * @return debug logs
     */
    List<ContextDebugLog> getDebugLogs();

    /**
     * Validate context
     * 
     * @param context the context to validate
     * @return validation result
     */
    ContextValidationResult validateContext(AgentContext context);

    /**
     * Get service status
     * 
     * @return true if the service is available and ready
     */
    boolean isAvailable();

    /**
     * Agent context
     */
    interface AgentContext {
        String getAgentId();

        Map<String, Object> getContextData();

        List<String> getAvailableActions();

        Map<String, Object> getEnvironmentState();

        long getTimestamp();

        String getContextId();
    }

    /**
     * Security level
     */
    enum SecurityLevel {
        LOW,
        MEDIUM,
        HIGH,
        MAXIMUM
    }

    /**
     * Context performance metrics
     */
    interface ContextPerformanceMetrics {
        long getTotalContextsBuilt();

        long getCacheHits();

        long getCacheMisses();

        double getAverageBuildTime();

        double getCacheHitRate();
    }

    /**
     * Context debug log
     */
    interface ContextDebugLog {
        String getContextId();

        String getOperation();

        long getTimestamp();

        String getMessage();

        Map<String, Object> getData();
    }

    /**
     * Context validation result
     */
    interface ContextValidationResult {
        boolean isValid();

        String getMessage();

        List<String> getErrors();

        List<String> getWarnings();
    }
}
