package org.openhab.core.ai.action;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.snapshot.ActionExecutionSnapshot;
import org.openhab.core.ai.model.ActionExecutionEventStatus;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced centralized registry for Actions that can be used by both MCP and A2A protocols.
 * This registry provides a unified way to discover and manage Actions across the system with
 * advanced features for skill integration, performance monitoring, security validation,
 * and analytics.
 * 
 * <p>
 * Enhanced features include:
 * - Agent-specific action registration and management
 * - Action-skill mapping and integration
 * - Performance monitoring and analytics
 * - Security validation and access control
 * - Action caching and optimization
 * - Versioning and compatibility management
 * - Documentation generation and testing framework
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ActionRegistry.class, immediate = true)
@NonNullByDefault
public class ActionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ActionRegistry.class);

    // Core registry storage
    private @Nullable BundleContext bundleContext;
    private final Map<String, Action> actions = new ConcurrentHashMap<>();
    private final Map<String, ActionMetadata> metadata = new ConcurrentHashMap<>();

    // Agent-specific action registration
    private final Map<String, Set<String>> agentActionMappings = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> actionAgentMappings = new ConcurrentHashMap<>();

    // Action-skill mapping management
    private final Map<String, String> actionSkillMappings = new ConcurrentHashMap<>();
    private final Map<String, List<String>> skillActionMappings = new ConcurrentHashMap<>();

    // Performance monitoring
    @Reference
    private @Nullable MetricsService metricsService;
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong successfulExecutions = new AtomicLong(0);
    private final AtomicLong failedExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTimeMs = new AtomicLong(0);
    private final AtomicReference<Instant> lastExecutionTime = new AtomicReference<>(Instant.now());

    // Security validation
    private final Map<String, ActionSecurityPolicy> securityPolicies = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> actionPermissions = new ConcurrentHashMap<>();

    // Caching and optimization
    private final Map<String, ActionCacheEntry> actionCache = new ConcurrentHashMap<>();
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    // Versioning and compatibility
    private final Map<String, ActionVersionInfo> versionInfo = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> compatibilityMatrix = new ConcurrentHashMap<>();

    // Analytics and reporting
    private final Map<String, ActionAnalytics> analytics = new ConcurrentHashMap<>();
    private final List<ActionExecutionEvent> executionHistory = new ArrayList<>();

    // Configuration
    private boolean enableCaching = true;
    private boolean enableSecurityValidation = true;
    private boolean enablePerformanceMonitoring = true;
    private boolean enableAnalytics = true;
    private Duration cacheExpiration = Duration.ofMinutes(30);
    private int maxCacheSize = 1000;
    private int maxExecutionHistory = 10000;

    /**
     * Activate the component.
     * 
     * @param bundleContext the bundle context
     */
    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        logger.info("Action Registry activated");
        discoverActions();
        logger.info("Action Registry started with {} actions", actions.size());
    }

    /**
     * Deactivate the component.
     */
    @Deactivate
    public void deactivate() {
        logger.info("Action Registry deactivated");
        actions.clear();
        metadata.clear();
        this.bundleContext = null;
    }

    /**
     * Bind an Action service.
     * 
     * @param action the Action service to bind
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void bindAction(Action action) {
        registerAction(action);
    }

    /**
     * Unbind an Action service.
     * 
     * @param action the Action service to unbind
     */
    public void unbindAction(Action action) {
        unregisterAction(action.getActionId());
    }

    /**
     * Discover and register available Actions via OSGi services.
     * This method is called during activation to discover existing services.
     */
    private void discoverActions() {
        try {
            // Find all Action services that are already registered
            Collection<ServiceReference<Action>> refs = bundleContext.getServiceReferences(Action.class, null);
            if (refs != null) {
                for (ServiceReference<Action> ref : refs) {
                    Action action = bundleContext.getService(ref);
                    if (action != null) {
                        registerAction(action);
                        // Release the service reference
                        bundleContext.ungetService(ref);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error discovering existing Actions", e);
        }
    }

    /**
     * Register an Action with the registry.
     * 
     * @param action the Action to register
     */
    public void registerAction(Action action) {
        String actionId = action.getActionId();
        actions.put(actionId, action);

        // Create and store metadata
        ActionMetadata actionMetadata = new ActionMetadata(action);
        metadata.put(actionId, actionMetadata);

        logger.debug("Registered action: {} with metadata: {}", actionId, actionMetadata);
    }

    /**
     * Unregister an Action from the registry.
     * 
     * @param actionId the ID of the Action to unregister
     */
    public void unregisterAction(String actionId) {
        actions.remove(actionId);
        metadata.remove(actionId);
        logger.debug("Unregistered action: {}", actionId);
    }

    /**
     * Get an Action by its ID.
     * 
     * @param actionId the ID of the Action to retrieve
     * @return the Action, or null if not found
     */
    public @Nullable Action getAction(String actionId) {
        return actions.get(actionId);
    }

    /**
     * Get metadata for an Action by its ID.
     * 
     * @param actionId the ID of the Action
     * @return the ActionMetadata, or null if not found
     */
    public @Nullable ActionMetadata getActionMetadata(String actionId) {
        return metadata.get(actionId);
    }

    /**
     * Get all registered Actions.
     * 
     * @return a map of all Actions by their IDs
     */
    public Map<String, Action> getAllActions() {
        return new ConcurrentHashMap<>(actions);
    }

    /**
     * Get all Action metadata.
     * 
     * @return a map of all ActionMetadata by Action IDs
     */
    public Map<String, ActionMetadata> getAllMetadata() {
        return new ConcurrentHashMap<>(metadata);
    }

    /**
     * Get the total number of registered Actions.
     * 
     * @return the number of registered Actions
     */
    public int getActionCount() {
        return actions.size();
    }

    /**
     * Check if an Action is registered.
     * 
     * @param actionId the ID of the Action to check
     * @return true if the Action is registered, false otherwise
     */
    public boolean isActionRegistered(String actionId) {
        return actions.containsKey(actionId);
    }

    /**
     * Get all Actions by category.
     * 
     * @param category the category to filter by
     * @return a map of Actions in the specified category
     */
    public Map<String, Action> getActionsByCategory(String category) {
        Map<String, Action> categoryActions = new ConcurrentHashMap<>();
        for (Map.Entry<String, Action> entry : actions.entrySet()) {
            ActionMetadata actionMetadata = metadata.get(entry.getKey());
            if (actionMetadata != null && category.equals(actionMetadata.getCategory())) {
                categoryActions.put(entry.getKey(), entry.getValue());
            }
        }
        return categoryActions;
    }

    /**
     * Get all Action IDs by category.
     * 
     * @param category the category to filter by
     * @return a collection of Action IDs in the specified category
     */
    public Collection<String> getActionIdsByCategory(String category) {
        return getActionsByCategory(category).keySet();
    }

    // Enhanced methods for skill integration

    /**
     * Register an action for a specific agent.
     * 
     * @param actionId the action ID
     * @param agentId the agent ID
     * @return true if registration was successful, false otherwise
     */
    public boolean registerActionForAgent(String actionId, String agentId) {
        if (!isActionRegistered(actionId)) {
            logger.warn("Cannot register unregistered action {} for agent {}", actionId, agentId);
            return false;
        }

        agentActionMappings.computeIfAbsent(agentId, k -> ConcurrentHashMap.newKeySet()).add(actionId);
        actionAgentMappings.computeIfAbsent(actionId, k -> ConcurrentHashMap.newKeySet()).add(agentId);

        logger.debug("Registered action {} for agent {}", actionId, agentId);
        return true;
    }

    /**
     * Unregister an action for a specific agent.
     * 
     * @param actionId the action ID
     * @param agentId the agent ID
     * @return true if unregistration was successful, false otherwise
     */
    public boolean unregisterActionForAgent(String actionId, String agentId) {
        Set<String> agentActions = agentActionMappings.get(agentId);
        if (agentActions != null) {
            agentActions.remove(actionId);
        }

        Set<String> actionAgents = actionAgentMappings.get(actionId);
        if (actionAgents != null) {
            actionAgents.remove(agentId);
        }

        logger.debug("Unregistered action {} for agent {}", actionId, agentId);
        return true;
    }

    /**
     * Get all actions registered for a specific agent.
     * 
     * @param agentId the agent ID
     * @return a map of actions registered for the agent
     */
    public Map<String, Action> getActionsForAgent(String agentId) {
        Set<String> agentActions = agentActionMappings.get(agentId);
        if (agentActions == null) {
            return Map.of();
        }

        Map<String, Action> result = new HashMap<>();
        for (String actionId : agentActions) {
            Action action = actions.get(actionId);
            if (action != null) {
                result.put(actionId, action);
            }
        }
        return result;
    }

    /**
     * Get all agents that can execute a specific action.
     * 
     * @param actionId the action ID
     * @return a set of agent IDs that can execute the action
     */
    public Set<String> getAgentsForAction(String actionId) {
        return actionAgentMappings.getOrDefault(actionId, Set.of());
    }

    /**
     * Map an action to a skill.
     * 
     * @param actionId the action ID
     * @param skillId the skill ID
     * @return true if mapping was successful, false otherwise
     */
    public boolean mapActionToSkill(String actionId, String skillId) {
        if (!isActionRegistered(actionId)) {
            logger.warn("Cannot map unregistered action {} to skill {}", actionId, skillId);
            return false;
        }

        actionSkillMappings.put(actionId, skillId);
        skillActionMappings.computeIfAbsent(skillId, k -> new ArrayList<>()).add(actionId);

        logger.debug("Mapped action {} to skill {}", actionId, skillId);
        return true;
    }

    /**
     * Get the skill ID for an action.
     * 
     * @param actionId the action ID
     * @return the skill ID, or null if not mapped
     */
    public @Nullable String getSkillForAction(String actionId) {
        return actionSkillMappings.get(actionId);
    }

    /**
     * Get all actions for a skill.
     * 
     * @param skillId the skill ID
     * @return a list of action IDs for the skill
     */
    public List<String> getActionsForSkill(String skillId) {
        return skillActionMappings.getOrDefault(skillId, List.of());
    }

    /**
     * Record an action execution for performance monitoring.
     * 
     * @param actionId the action ID
     * @param agentId the agent ID
     * @param executionTimeMs the execution time in milliseconds
     * @param success whether the execution was successful
     * @param error the error message if failed, null if successful
     */
    public void recordExecution(String actionId, String agentId, long executionTimeMs, boolean success,
            @Nullable String error) {
        if (!enablePerformanceMonitoring) {
            return;
        }

        totalExecutions.incrementAndGet();
        if (success) {
            successfulExecutions.incrementAndGet();
        } else {
            failedExecutions.incrementAndGet();
        }
        totalExecutionTimeMs.addAndGet(executionTimeMs);
        lastExecutionTime.set(Instant.now());

        // Update action-specific metrics
        // Record action execution metrics
        if (metricsService != null) {
            metricsService.recordOperation("action", actionId, success, Duration.ofMillis(executionTimeMs));
        }

        // Record execution event
        if (enableAnalytics) {
            ActionExecutionEvent event = new ActionExecutionEventBuilder().eventId("exec-" + System.currentTimeMillis())
                    .actionId(actionId).agentId(agentId)
                    .status(success ? ActionExecutionEventStatus.SUCCESS : ActionExecutionEventStatus.FAILED)
                    .error(error).build();

            synchronized (executionHistory) {
                executionHistory.add(event);
                if (executionHistory.size() > maxExecutionHistory) {
                    executionHistory.remove(0);
                }
            }
        }

        logger.debug("Recorded execution for action {} by agent {}: success={}, time={}ms", actionId, agentId, success,
                executionTimeMs);
    }

    /**
     * Get performance metrics for an action.
     * 
     * @param actionId the action ID
     * @return the performance metrics, or null if not found
     */
    public @Nullable ActionExecutionSnapshot getPerformanceMetrics(String actionId) {
        return metricsService != null ? metricsService.getActionExecutionSnapshot(actionId) : null;
    }

    /**
     * Get overall performance metrics.
     * 
     * @return the overall performance metrics
     */
    public ActionExecutionSnapshot getOverallPerformanceMetrics() {
        return metricsService != null ? metricsService.getActionExecutionSnapshot("overall")
                : ActionExecutionSnapshot.empty("overall");
    }

    /**
     * Set security policy for an action.
     * 
     * @param actionId the action ID
     * @param policy the security policy
     */
    public void setSecurityPolicy(String actionId, ActionSecurityPolicy policy) {
        securityPolicies.put(actionId, policy);
        logger.debug("Set security policy for action {}", actionId);
    }

    /**
     * Get security policy for an action.
     * 
     * @param actionId the action ID
     * @return the security policy, or null if not found
     */
    public @Nullable ActionSecurityPolicy getSecurityPolicy(String actionId) {
        return securityPolicies.get(actionId);
    }

    /**
     * Validate action execution against security policy.
     * 
     * @param actionId the action ID
     * @param agentId the agent ID
     * @param origin the origin of the request
     * @return true if execution is allowed, false otherwise
     */
    public boolean validateExecution(String actionId, String agentId, String origin) {
        if (!enableSecurityValidation) {
            return true;
        }

        ActionSecurityPolicy policy = securityPolicies.get(actionId);
        if (policy == null) {
            // Default policy: allow if no specific policy is set
            return true;
        }

        boolean agentAllowed = policy.isAgentAllowed(agentId);
        boolean originAllowed = policy.isOriginAllowed(origin);

        if (!agentAllowed) {
            logger.warn("Agent {} not allowed to execute action {}", agentId, actionId);
        }
        if (!originAllowed) {
            logger.warn("Origin {} not allowed to execute action {}", origin, actionId);
        }

        return agentAllowed && originAllowed;
    }

    /**
     * Cache an action result.
     * 
     * @param actionId the action ID
     * @param parameters the action parameters
     * @param result the action result
     */
    public void cacheResult(String actionId, Map<String, Object> parameters, @Nullable Object result) {
        if (!enableCaching) {
            return;
        }

        String cacheKey = generateCacheKey(actionId, parameters);
        ActionCacheEntry entry = new ActionCacheEntry(actionId, parameters, result, Instant.now(),
                Instant.now().plus(cacheExpiration));

        // Check cache size limit
        if (actionCache.size() >= maxCacheSize) {
            // Remove oldest entry
            String oldestKey = actionCache.keySet().iterator().next();
            actionCache.remove(oldestKey);
        }

        actionCache.put(cacheKey, entry);
        logger.debug("Cached result for action {} with key {}", actionId, cacheKey);
    }

    /**
     * Get cached result for an action.
     * 
     * @param actionId the action ID
     * @param parameters the action parameters
     * @return the cached result, or null if not found or expired
     */
    public @Nullable Object getCachedResult(String actionId, Map<String, Object> parameters) {
        if (!enableCaching) {
            return null;
        }

        String cacheKey = generateCacheKey(actionId, parameters);
        ActionCacheEntry entry = actionCache.get(cacheKey);

        if (entry == null) {
            cacheMisses.incrementAndGet();
            return null;
        }

        if (entry.isExpired()) {
            actionCache.remove(cacheKey);
            cacheMisses.incrementAndGet();
            return null;
        }

        cacheHits.incrementAndGet();
        actionCache.put(cacheKey, entry.withAccess());
        return entry.getResult();
    }

    /**
     * Clear cache for an action.
     * 
     * @param actionId the action ID
     */
    public void clearCache(String actionId) {
        actionCache.entrySet().removeIf(entry -> entry.getValue().getActionId().equals(actionId));
        logger.debug("Cleared cache for action {}", actionId);
    }

    /**
     * Clear all caches.
     */
    public void clearAllCaches() {
        actionCache.clear();
        logger.debug("Cleared all action caches");
    }

    /**
     * Get cache statistics.
     * 
     * @return a map containing cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheHits", cacheHits.get());
        stats.put("cacheMisses", cacheMisses.get());
        stats.put("cacheSize", actionCache.size());
        stats.put("maxCacheSize", maxCacheSize);
        stats.put("enableCaching", enableCaching);
        return stats;
    }

    /**
     * Set version information for an action.
     * 
     * @param actionId the action ID
     * @param versionInfo the version information
     */
    public void setVersionInfo(String actionId, ActionVersionInfo versionInfo) {
        this.versionInfo.put(actionId, versionInfo);
        logger.debug("Set version info for action {}", actionId);
    }

    /**
     * Get version information for an action.
     * 
     * @param actionId the action ID
     * @return the version information, or null if not found
     */
    public @Nullable ActionVersionInfo getVersionInfo(String actionId) {
        return versionInfo.get(actionId);
    }

    /**
     * Check if two actions are compatible.
     * 
     * @param actionId1 the first action ID
     * @param actionId2 the second action ID
     * @return true if compatible, false otherwise
     */
    public boolean areActionsCompatible(String actionId1, String actionId2) {
        Set<String> compatibleActions = compatibilityMatrix.get(actionId1);
        return compatibleActions != null && compatibleActions.contains(actionId2);
    }

    /**
     * Get analytics for an action.
     * 
     * @param actionId the action ID
     * @return the analytics data, or null if not found
     */
    public @Nullable ActionAnalytics getAnalytics(String actionId) {
        return analytics.get(actionId);
    }

    /**
     * Get execution history for an action.
     * 
     * @param actionId the action ID
     * @param limit the maximum number of events to return
     * @return a list of execution events
     */
    public List<ActionExecutionEvent> getExecutionHistory(String actionId, int limit) {
        synchronized (executionHistory) {
            return executionHistory.stream().filter(event -> event.getActionId().equals(actionId)).limit(limit)
                    .toList();
        }
    }

    /**
     * Generate a cache key for an action and parameters.
     * 
     * @param actionId the action ID
     * @param parameters the parameters
     * @return the cache key
     */
    private String generateCacheKey(String actionId, Map<String, Object> parameters) {
        return actionId + ":" + parameters.hashCode();
    }

    /**
     * Update configuration settings.
     * 
     * @param enableCaching whether to enable caching
     * @param enableSecurityValidation whether to enable security validation
     * @param enablePerformanceMonitoring whether to enable performance monitoring
     * @param enableAnalytics whether to enable analytics
     * @param cacheExpiration the cache expiration duration
     * @param maxCacheSize the maximum cache size
     * @param maxExecutionHistory the maximum execution history size
     */
    public void updateConfiguration(boolean enableCaching, boolean enableSecurityValidation,
            boolean enablePerformanceMonitoring, boolean enableAnalytics, Duration cacheExpiration, int maxCacheSize,
            int maxExecutionHistory) {
        this.enableCaching = enableCaching;
        this.enableSecurityValidation = enableSecurityValidation;
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
        this.enableAnalytics = enableAnalytics;
        this.cacheExpiration = cacheExpiration;
        this.maxCacheSize = maxCacheSize;
        this.maxExecutionHistory = maxExecutionHistory;

        logger.info("Updated ActionRegistry configuration: caching={}, security={}, monitoring={}, analytics={}",
                enableCaching, enableSecurityValidation, enablePerformanceMonitoring, enableAnalytics);
    }
}
