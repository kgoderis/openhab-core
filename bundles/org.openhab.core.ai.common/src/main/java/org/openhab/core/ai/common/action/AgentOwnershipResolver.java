package org.openhab.core.ai.common.action;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent Ownership Resolver - Defines the contract for resolving agent ownership
 * 
 * <p>
 * This service provides:
 * - Ownership determination algorithms
 * - Ownership validation and testing
 * - Ownership caching and optimization
 * - Ownership security and access controls
 * - Ownership performance monitoring
 * - Ownership debugging and logging
 * - Ownership testing and validation
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AgentOwnershipResolver {

    /**
     * Determine ownership for an agent
     * 
     * @param agentId the agent ID
     * @return ownership information
     */
    AgentOwnership determineOwnership(String agentId);

    /**
     * Get all owners for an agent
     * 
     * @param agentId the agent ID
     * @return set of owner IDs
     */
    Set<String> getAgentOwners(String agentId);

    /**
     * Check if user is owner of agent
     * 
     * @param userId the user ID
     * @param agentId the agent ID
     * @return true if user is owner
     */
    boolean isOwner(String userId, String agentId);

    /**
     * Add owner to agent
     * 
     * @param agentId the agent ID
     * @param ownerId the owner ID
     * @return true if addition was successful
     */
    boolean addOwner(String agentId, String ownerId);

    /**
     * Remove owner from agent
     * 
     * @param agentId the agent ID
     * @param ownerId the owner ID
     * @return true if removal was successful
     */
    boolean removeOwner(String agentId, String ownerId);

    /**
     * Get all agents owned by user
     * 
     * @param userId the user ID
     * @return list of agent IDs
     */
    List<String> getAgentsOwnedByUser(String userId);

    /**
     * Validate ownership
     * 
     * @param agentId the agent ID
     * @param userId the user ID
     * @return validation result
     */
    OwnershipValidationResult validateOwnership(String agentId, String userId);

    /**
     * Test ownership resolution
     * 
     * @param agentId the agent ID
     * @return test result
     */
    OwnershipTestResult testOwnershipResolution(String agentId);

    /**
     * Cache ownership information
     * 
     * @param agentId the agent ID
     * @param ownership the ownership information
     */
    void cacheOwnership(String agentId, AgentOwnership ownership);

    /**
     * Get cached ownership
     * 
     * @param agentId the agent ID
     * @return cached ownership or null if not found
     */
    AgentOwnership getCachedOwnership(String agentId);

    /**
     * Clear ownership cache
     */
    void clearOwnershipCache();

    /**
     * Apply ownership security rules
     * 
     * @param agentId the agent ID
     * @param userId the user ID
     * @param action the action to perform
     * @return security result
     */
    OwnershipSecurityResult applySecurityRules(String agentId, String userId, String action);

    /**
     * Get ownership performance metrics
     * 
     * @return performance metrics
     */
    OwnershipPerformanceMetrics getPerformanceMetrics();

    /**
     * Enable ownership debugging
     * 
     * @param enabled true to enable debugging
     */
    void setDebuggingEnabled(boolean enabled);

    /**
     * Get ownership debug logs
     * 
     * @return debug logs
     */
    List<OwnershipDebugLog> getDebugLogs();

    /**
     * Get service status
     * 
     * @return true if the service is available and ready
     */
    boolean isAvailable();

    /**
     * Get the number of ownership records
     * 
     * @return the number of ownership records
     */
    int getOwnershipCount();

    /**
     * Agent ownership information
     */
    interface AgentOwnership {
        String getAgentId();

        Set<String> getOwners();

        String getPrimaryOwner();

        long getCreatedAt();

        long getLastModified();

        Map<String, Object> getMetadata();
    }

    /**
     * Ownership validation result
     */
    interface OwnershipValidationResult {
        boolean isValid();

        String getMessage();

        List<String> getErrors();

        List<String> getWarnings();
    }

    /**
     * Ownership test result
     */
    interface OwnershipTestResult {
        boolean isSuccessful();

        String getMessage();

        long getResolutionTime();

        Map<String, Object> getTestData();
    }

    /**
     * Ownership security result
     */
    interface OwnershipSecurityResult {
        boolean isAllowed();

        String getMessage();

        List<String> getRequiredPermissions();

        Map<String, Object> getSecurityContext();
    }

    /**
     * Ownership performance metrics
     */
    interface OwnershipPerformanceMetrics {
        long getTotalResolutions();

        long getCacheHits();

        long getCacheMisses();

        double getAverageResolutionTime();

        double getCacheHitRate();
    }

    /**
     * Ownership debug log
     */
    interface OwnershipDebugLog {
        String getAgentId();

        String getOperation();

        long getTimestamp();

        String getMessage();

        Map<String, Object> getData();
    }
}
