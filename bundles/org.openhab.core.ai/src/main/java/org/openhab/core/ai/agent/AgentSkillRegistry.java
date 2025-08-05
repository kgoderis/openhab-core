package org.openhab.core.ai.agent.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.agent.AgentSkillException;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.AgentSkill;
import io.a2a.spec.Message;

/**
 * Skill registry for Agent operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentSkillRegistry implements ReadyTracker {

    private static final Logger logger = LoggerFactory.getLogger(AgentSkillRegistry.class);

    // Ready marker for Agent skills
    public static final ReadyMarker AGENT_SKILLS_READY = new ReadyMarker("agent", "skills");

    @Reference
    private @Nullable ReadyService readyService;

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    // Skill registry using SDK patterns
    private final ConcurrentHashMap<String, AgentSkillAdapter> skillAdapters = new ConcurrentHashMap<>();

    // Enhanced skill tracking using SDK patterns
    private final ConcurrentHashMap<String, Map<String, Object>> skillMetadata = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> skillExecutionCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> skillLastExecutionTimes = new ConcurrentHashMap<>();

    // Skill execution statistics
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong successfulExecutions = new AtomicLong(0);
    private final AtomicLong failedExecutions = new AtomicLong(0);

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
        skillExecutionCounts.clear();
        skillLastExecutionTimes.clear();
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

                // Initialize execution tracking
                skillExecutionCounts.put(skillId, new AtomicLong(0));
                skillLastExecutionTimes.put(skillId, 0L);

                logger.debug("Registered AI action as Agent skill: {} -> {}", action.getActionId(), skillId);

            } catch (Exception e) {
                logger.error("Failed to register AI action as Agent skill: {}", action.getActionId(), e);
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

    public Object executeSkill(String skillId, Message message) throws AgentSkillException {
        logger.debug("Executing Agent skill: {} using SDK patterns", skillId);

        // Update execution tracking
        skillLastExecutionTimes.put(skillId, System.currentTimeMillis());
        AtomicLong executionCount = skillExecutionCounts.get(skillId);
        if (executionCount != null) {
            executionCount.incrementAndGet();
        }
        totalExecutions.incrementAndGet();

        try {
            // Get skill adapter
            AgentSkillAdapter adapter = skillAdapters.get(skillId);
            if (adapter == null) {
                logger.warn("Skill not found: {}", skillId);
                failedExecutions.incrementAndGet();
                throw new AgentSkillException("Skill not found: " + skillId);
            }

            // Execute the skill using SDK patterns
            Object result = adapter.execute(message);

            // Track successful execution
            successfulExecutions.incrementAndGet();

            logger.debug("Skill execution completed successfully: {}", skillId);
            return result;

        } catch (Exception e) {
            // Track failed execution
            failedExecutions.incrementAndGet();

            logger.error("Skill execution failed: {}", skillId, e);
            throw new AgentSkillException("Skill execution failed: " + e.getMessage(), e);
        }
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

            // Add execution statistics
            AtomicLong executionCount = skillExecutionCounts.get(skillId);
            if (executionCount != null) {
                definition.put("executionCount", executionCount.get());
            }

            Long lastExecutionTime = skillLastExecutionTimes.get(skillId);
            if (lastExecutionTime != null) {
                definition.put("lastExecutionTime", lastExecutionTime);
            }

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
        stats.put("totalExecutions", totalExecutions.get());
        stats.put("successfulExecutions", successfulExecutions.get());
        stats.put("failedExecutions", failedExecutions.get());

        // Calculate success rate
        long total = totalExecutions.get();
        if (total > 0) {
            double successRate = (double) successfulExecutions.get() / total;
            stats.put("successRate", successRate);
        } else {
            stats.put("successRate", 0.0);
        }

        // Per-skill statistics
        Map<String, Object> skillStats = new HashMap<>();
        for (String skillId : skillAdapters.keySet()) {
            Map<String, Object> skillStat = new HashMap<>();

            AtomicLong executionCount = skillExecutionCounts.get(skillId);
            if (executionCount != null) {
                skillStat.put("executionCount", executionCount.get());
            }

            Long lastExecutionTime = skillLastExecutionTimes.get(skillId);
            if (lastExecutionTime != null) {
                skillStat.put("lastExecutionTime", lastExecutionTime);
            }

            skillStats.put(skillId, skillStat);
        }
        stats.put("skillStatistics", skillStats);

        return stats;
    }

    public void refreshSkills() {
        logger.debug("Refreshing Agent skills using SDK patterns");

        // Clear existing registrations
        skillAdapters.clear();
        skillMetadata.clear();
        skillExecutionCounts.clear();
        skillLastExecutionTimes.clear();

        // Reset statistics
        totalExecutions.set(0);
        successfulExecutions.set(0);
        failedExecutions.set(0);

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

    public long getSkillExecutionCount(String skillId) {
        AtomicLong count = skillExecutionCounts.get(skillId);
        return count != null ? count.get() : 0;
    }

    public long getSkillLastExecutionTime(String skillId) {
        Long time = skillLastExecutionTimes.get(skillId);
        return time != null ? time : 0;
    }
}
