package org.openhab.core.ai.agent.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import java.util.Map;
import java.util.Set;
import org.openhab.core.ai.common.monitoring.api.MetricKey;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.AgentSkill;

/**
 * Skill Registration and Lookup.
 * 
 * <p>
 * <strong>Primary Responsibility:</strong> Skill Registration and Lookup
 * </p>
 * 
 * <p>
 * <strong>Concerns:</strong>
 * <ul>
 * <li><strong>Skill Registration:</strong> Registers skill adapters</li>
 * <li><strong>Skill Lookup:</strong> Provides skill adapter lookup functionality</li>
 * <li><strong>Skill Metadata:</strong> Manages skill metadata and definitions</li>
 * <li><strong>Skill Statistics:</strong> Tracks skill registration statistics via MetricsService</li>
 * <li><strong>Skill Discovery:</strong> Provides skill discovery capabilities</li>
 * <li><strong>Ready State Management:</strong> Manages skill registry ready state</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES:</strong>
 * <ul>
 * <li>Registers skills via {@link #registerSkill(String, AgentSkillAdapter)}</li>
 * <li>Unregisters skills via {@link #unregisterSkill(String)}</li>
 * <li>Provides skill lookup via {@link #getSkillAdapter(String)}</li>
 * <li>Checks skill existence via {@link #hasSkill(String)}</li>
 * <li>Returns skill definitions via {@link #getSkillDefinitions()}</li>
 * <li>Returns A2A SDK AgentSkill objects via {@link #getAgentSkills()}</li>
 * <li>Returns all registered skill IDs via {@link #getSkillIds()}</li>
 * <li>Manages skill metadata and statistics via MetricsService</li>
 * <li>Provides skill discovery capabilities</li>
 * <li>Manages skill registry ready state</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES NOT do:</strong>
 * <ul>
 * <li>❌ Execute skills (delegates to AgentSkillExecutor)</li>
 * <li>❌ Handle protocol communication (delegates to AgentProtocolHandler)</li>
 * <li>❌ Manage task lifecycle (delegates to AgentTaskManager)</li>
 * <li>❌ Handle authentication (delegates to security manager)</li>
 * <li>❌ Convert parameters (delegates to AgentSkillManagerImpl)</li>
 * <li>❌ Track execution metrics directly (delegates to MetricsService)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Boundary Conditions:</strong>
 * <ul>
 * <li>Only manages skill registration and lookup</li>
 * <li>Does not execute skills</li>
 * <li>Does not handle protocol communication</li>
 * <li>Does not manage task lifecycle</li>
 * <li>Does not contain business logic</li>
 * <li>Does not maintain direct counters (uses MetricsService)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Dependencies:</strong>
 * <ul>
 * <li>{@link ActionRegistry}: For action-to-skill mapping</li>
 * <li>{@link ReadyService}: For ready state management</li>
 * <li>{@link MetricsService}: For statistics tracking</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Architecture Layer:</strong> Skill Registry Layer
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(service = AgentSkillRegistry.class)
public class AgentSkillRegistry implements ReadyTracker {

    private static final Logger logger = LoggerFactory.getLogger(AgentSkillRegistry.class);

    // Ready marker for Agent skills
    public static final ReadyMarker AGENT_SKILLS_READY = new ReadyMarker("agent", "skills");

    @Reference
    private @Nullable ReadyService readyService;

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    @Reference
    private @Nullable MetricsService metricsService;

    // Skill registry using SDK patterns
    private final ConcurrentHashMap<String, AgentSkillAdapter> skillAdapters = new ConcurrentHashMap<>();

    // Enhanced skill tracking using SDK patterns
    private final ConcurrentHashMap<String, Map<String, Object>> skillMetadata = new ConcurrentHashMap<>();

    @Activate
    public void activate() {
        logger.debug("Agent Skill Registry activated with SDK patterns");

        // Register as a tracker
        if (readyService != null) {
            readyService.registerTracker(this);
        }

        // Initialize skill registry
        initializeSkillRegistry();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Agent Skill Registry deactivated");

        // Unregister tracker
        if (readyService != null) {
            readyService.unregisterTracker(this);
            readyService.unmarkReady(AGENT_SKILLS_READY);
        }

        // Clean up registrations
        skillAdapters.clear();
        skillMetadata.clear();
    }

    private void initializeSkillRegistry() {
        logger.debug("Initializing Agent skill registry using SDK patterns");

        try {
            // Register all AI actions as Agent skills
            registerActionsAsSkills();

            // Mark skills as ready
            if (readyService != null) {
                readyService.markReady(AGENT_SKILLS_READY);
            }
            logger.info("Agent skill registry initialized with {} skills", skillAdapters.size());

        } catch (Exception e) {
            logger.error("Failed to initialize Agent skill registry", e);
        }
    }

    private void registerActionsAsSkills() {
        logger.debug("Registering AI actions as Agent skills using SDK patterns");

        // Get all available AI actions
        Map<String, Action> actions = actionRegistry != null ? actionRegistry.getAllActions() : new HashMap<>();

        for (Action action : actions.values()) {
            try {
                // Create skill adapter for this action
                AgentSkillAdapter adapter = new AgentSkillAdapter("openhab." + action.getActionId(), action);

                // Register the skill
                String skillId = "openhab." + action.getActionId();
                skillAdapters.put(skillId, adapter);

                // Create skill metadata using SDK patterns
                Map<String, Object> metadata = createSkillMetadata(action, skillId);
                skillMetadata.put(skillId, metadata);

                // Record skill registration metrics
                recordMetrics("agent-skill", "skill-registration", true, 0L);

                logger.debug("Registered AI action as Agent skill: {} -> {}", action.getActionId(), skillId);

            } catch (Exception e) {
                logger.error("Failed to register AI action as Agent skill: {}", action.getActionId(), e);
                recordMetrics("agent-skill", "skill-registration", false, 0L);
            }
        }
    }

    private Map<String, Object> createSkillMetadata(Action action, String skillId) {
        // Create comprehensive skill metadata using SDK patterns
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("actionId", action.getActionId());
        metadata.put("category", action.getCategory());
        metadata.put("description", action.getDescription());
        metadata.put("version", "1.0.0");
        metadata.put("openhab", true);
        metadata.put("registrationTime", System.currentTimeMillis());

        // Add action-specific metadata
        if (action.getMetadata() != null) {
            // Convert ActionMetadata to Map<String, Object>
            metadata.put("author", action.getMetadata().getAuthor());
            metadata.put("version", action.getMetadata().getVersion());
            metadata.put("description", action.getMetadata().getDescription());
            metadata.put("tags", action.getMetadata().getTags());
        }

        return metadata;
    }

    public boolean hasSkill(String skillId) {
        return skillAdapters.containsKey(skillId);
    }

    public List<Map<String, Object>> getSkillDefinitions() {
        logger.debug("Getting Agent skill definitions using SDK patterns");

        List<Map<String, Object>> definitions = new ArrayList<>();

        for (Map.Entry<String, AgentSkillAdapter> entry : skillAdapters.entrySet()) {
            String skillId = entry.getKey();
            AgentSkillAdapter adapter = entry.getValue();
            Map<String, Object> metadata = skillMetadata.get(skillId);

            Map<String, Object> definition = new HashMap<>();
            definition.put("id", skillId);
            definition.put("name", adapter.getSkillName());
            definition.put("description", adapter.getSkillDescription());
            definition.put("category", adapter.getSkillCategory());
            definition.put("version", "1.0.0");
            definition.put("openhab", true);

            // Add metadata if available
            if (metadata != null) {
                definition.put("metadata", metadata);
            }

            definitions.add(definition);
        }

        return definitions;
    }

    public List<AgentSkill> getAgentSkills() {
        logger.debug("Getting Agent agent skills using SDK patterns");

        List<AgentSkill> skills = new ArrayList<>();

        for (Map.Entry<String, AgentSkillAdapter> entry : skillAdapters.entrySet()) {
            String skillId = entry.getKey();
            AgentSkillAdapter adapter = entry.getValue();

            // Create AgentSkill using SDK patterns
            AgentSkill skill = new AgentSkill(skillId, // id
                    adapter.getSkillName(), // name
                    adapter.getSkillDescription(), // description
                    List.of(adapter.getSkillCategory()), // tags
                    List.of("Example: " + adapter.getSkillDescription()), // examples
                    List.of("text"), // inputModes
                    List.of("text") // outputModes
            );

            skills.add(skill);
            logger.debug("Added agent skill: {}", skillId);
        }

        return skills;
    }

    public Map<String, Object> getSkillStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Basic statistics
        stats.put("totalSkills", skillAdapters.size());

        // Get metrics from MetricsService
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                MetricKey agentSkillKey = MetricKeys.custom("agent-skill", Map.of(), Set.of("counts", "latency"));
                var snapshot = metrics.getSnapshot(agentSkillKey, org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                
                if (snapshot != null) {
                    long totalOperations = snapshot.getLong("total");
                    long failedOperations = snapshot.getLong("failure");
                    long successfulOperations = totalOperations - failedOperations;
                    
                    stats.put("totalExecutions", totalOperations);
                    stats.put("successfulExecutions", successfulOperations);
                    stats.put("failedExecutions", failedOperations);

                    // Calculate success rate
                    if (totalOperations > 0) {
                        double successRate = (double) successfulOperations / totalOperations;
                        stats.put("successRate", successRate);
                    } else {
                        stats.put("successRate", 0.0);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error retrieving metrics for agent-skill: {}", e.getMessage());
                stats.put("totalExecutions", 0L);
                stats.put("successfulExecutions", 0L);
                stats.put("failedExecutions", 0L);
                stats.put("successRate", 0.0);
            }
        } else {
            logger.warn("MetricsService not available, returning empty statistics");
            stats.put("totalExecutions", 0L);
            stats.put("successfulExecutions", 0L);
            stats.put("failedExecutions", 0L);
            stats.put("successRate", 0.0);
        }

        return stats;
    }

    public void refreshSkills() {
        logger.debug("Refreshing Agent skills using SDK patterns");

        // Clear existing registrations
        skillAdapters.clear();
        skillMetadata.clear();

        // Reset statistics - handled by MetricsService
        logger.info("Statistics reset requested - handled by MetricsService");

        // Re-register skills
        registerActionsAsSkills();

        logger.info("Agent skills refreshed - {} skills registered", skillAdapters.size());
    }

    public boolean isSkillReady(String skillId) {
        return skillAdapters.containsKey(skillId) && readyService != null && readyService.isReady(AGENT_SKILLS_READY);
    }

    @Override
    public void onReadyMarkerAdded(ReadyMarker readyMarker) {
        logger.debug("Ready marker added: {}", readyMarker);

        // Check if this is a marker we're waiting for
        if (isActionRegistryMarker(readyMarker)) {
            checkSkillsReady();
        }
    }

    @Override
    public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
        logger.debug("Ready marker removed: {}", readyMarker);

        // If AI action registry becomes unavailable, unmark skills as ready
        if (isActionRegistryMarker(readyMarker)) {
            if (readyService != null) {
                readyService.unmarkReady(AGENT_SKILLS_READY);
            }
        }
    }

    /**
     * Record metrics for an operation.
     * 
     * @param domain the operation domain
     * @param operation the operation name
     * @param success whether the operation was successful
     * @param durationNanos the operation duration in nanoseconds
     */
    private void recordMetrics(String domain, String operation, boolean success, long durationNanos) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            metrics.recordOperation(domain, operation, success, java.time.Duration.ofNanos(durationNanos));
        } else {
            logger.warn("MetricsService not available, cannot record metrics for operation: {} - {}", domain,
                    operation);
        }
    }

    private boolean isActionRegistryMarker(ReadyMarker marker) {
        // Check if this is the AI action registry marker
        return "ai".equals(marker.getType()) && "actions".equals(marker.getIdentifier());
    }

    private void checkSkillsReady() {
        logger.debug("Checking Agent skills readiness");

        // Check if AI action registry is ready
        ReadyMarker ActionsReady = new ReadyMarker("ai", "actions");
        boolean actionsReady = readyService != null && readyService.isReady(ActionsReady);

        if (actionsReady) {
            logger.debug("AI actions are ready, initializing Agent skills");
            initializeSkillRegistry();
        } else {
            logger.debug("AI actions not ready yet");
        }
    }

    // Enhanced utility methods for SDK integration
    public List<String> getSkillIds() {
        return new ArrayList<>(skillAdapters.keySet());
    }

    public @Nullable AgentSkillAdapter getSkillAdapter(String skillId) {
        return skillAdapters.get(skillId);
    }

    public @Nullable Map<String, Object> getSkillMetadata(String skillId) {
        return skillMetadata.get(skillId);
    }

    /**
     * Get skill execution count from MetricsService.
     * 
     * @param skillId the skill ID
     * @return the execution count, or 0 if MetricsService is not available
     */
    public long getSkillExecutionCount(String skillId) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                MetricKey agentSkillKey = MetricKeys.custom("agent-skill", Map.of(), Set.of("counts", "latency"));
                var snapshot = metrics.getSnapshot(agentSkillKey, org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot.class);
                return snapshot != null ? snapshot.getLong("total") : 0L;
            } catch (Exception e) {
                logger.warn("Error retrieving execution count for skill {}: {}", skillId, e.getMessage());
            }
        }
        return 0L;
    }

    /**
     * Get skill last execution time from MetricsService.
     * 
     * @param skillId the skill ID
     * @return the last execution time, or 0 if MetricsService is not available
     */
    public long getSkillLastExecutionTime(String skillId) {
        // Note: This would require domain-specific data in MetricsService
        // For now, return 0 as this information is not available in the current MetricsService implementation
        logger.debug("Last execution time not available in current MetricsService implementation for skill: {}",
                skillId);
        return 0L;
    }
}
