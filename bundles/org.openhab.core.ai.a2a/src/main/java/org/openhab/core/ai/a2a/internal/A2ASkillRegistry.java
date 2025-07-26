package org.openhab.core.ai.a2a.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.openhab.core.ai.a2a.api.A2ASkillException;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionRegistry;
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
import io.a2a.spec.Message;

/**
 * A2A Skill Registry implementation for OpenHAB using SDK patterns.
 * 
 * This class manages the registration and execution of A2A skills,
 * bridging AI actions from the ai.common bundle to the A2A protocol.
 * Enhanced with SDK utilities and patterns for better integration.
 * 
 * 
 */
@Component(service = A2ASkillRegistry.class, immediate = true)
public class A2ASkillRegistry implements ReadyTracker {

    private static final Logger logger = LoggerFactory.getLogger(A2ASkillRegistry.class);

    // Ready marker for A2A skills
    public static final ReadyMarker A2A_SKILLS_READY = new ReadyMarker("a2a", "skills");

    @Reference
    private ReadyService readyService;

    @Reference
    private AIActionRegistry actionRegistry;

    // Skill registry using SDK patterns
    private final ConcurrentHashMap<String, A2ASkillAdapter> skillAdapters = new ConcurrentHashMap<>();

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
        logger.debug("A2A Skill Registry activated with SDK patterns");

        // Register as a tracker
        readyService.registerTracker(this);

        // Initialize skill registry
        initializeSkillRegistry();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("A2A Skill Registry deactivated");

        // Unregister tracker
        readyService.unregisterTracker(this);

        // Unmark ready marker
        readyService.unmarkReady(A2A_SKILLS_READY);

        // Clean up registrations
        skillAdapters.clear();
        skillMetadata.clear();
        skillExecutionCounts.clear();
        skillLastExecutionTimes.clear();
    }

    private void initializeSkillRegistry() {
        logger.debug("Initializing A2A skill registry using SDK patterns");

        try {
            // Register all AI actions as A2A skills
            registerAIActionsAsSkills();

            // Mark skills as ready
            readyService.markReady(A2A_SKILLS_READY);
            logger.info("A2A skill registry initialized with {} skills", skillAdapters.size());

        } catch (Exception e) {
            logger.error("Failed to initialize A2A skill registry", e);
        }
    }

    private void registerAIActionsAsSkills() {
        logger.debug("Registering AI actions as A2A skills using SDK patterns");

        // Get all available AI actions
        Map<String, AIAction> actions = actionRegistry.getAllActions();

        for (AIAction action : actions.values()) {
            try {
                // Create skill adapter for this action
                A2ASkillAdapter adapter = new A2ASkillAdapter("openhab." + action.getActionId(), action);

                // Register the skill
                String skillId = "openhab." + action.getActionId();
                skillAdapters.put(skillId, adapter);

                // Create skill metadata using SDK patterns
                Map<String, Object> metadata = createSkillMetadata(action, skillId);
                skillMetadata.put(skillId, metadata);

                // Initialize execution tracking
                skillExecutionCounts.put(skillId, new AtomicLong(0));
                skillLastExecutionTimes.put(skillId, 0L);

                logger.debug("Registered AI action as A2A skill: {} -> {}", action.getActionId(), skillId);

            } catch (Exception e) {
                logger.error("Failed to register AI action as A2A skill: {}", action.getActionId(), e);
            }
        }
    }

    private Map<String, Object> createSkillMetadata(AIAction action, String skillId) {
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
            // Convert AIActionMetadata to Map<String, Object>
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

    public Object executeSkill(String skillId, Message message) throws A2ASkillException {
        logger.debug("Executing A2A skill: {} using SDK patterns", skillId);

        // Update execution tracking
        skillLastExecutionTimes.put(skillId, System.currentTimeMillis());
        AtomicLong executionCount = skillExecutionCounts.get(skillId);
        if (executionCount != null) {
            executionCount.incrementAndGet();
        }
        totalExecutions.incrementAndGet();

        try {
            // Get skill adapter
            A2ASkillAdapter adapter = skillAdapters.get(skillId);
            if (adapter == null) {
                logger.warn("Skill not found: {}", skillId);
                failedExecutions.incrementAndGet();
                throw new A2ASkillException("Skill not found: " + skillId);
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
            throw new A2ASkillException("Skill execution failed: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getSkillDefinitions() {
        logger.debug("Getting A2A skill definitions using SDK patterns");

        List<Map<String, Object>> definitions = new ArrayList<>();

        for (Map.Entry<String, A2ASkillAdapter> entry : skillAdapters.entrySet()) {
            String skillId = entry.getKey();
            A2ASkillAdapter adapter = entry.getValue();
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
        logger.debug("Getting A2A agent skills using SDK patterns");

        List<AgentSkill> skills = new ArrayList<>();

        for (Map.Entry<String, A2ASkillAdapter> entry : skillAdapters.entrySet()) {
            String skillId = entry.getKey();
            A2ASkillAdapter adapter = entry.getValue();

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
        logger.debug("Refreshing A2A skills using SDK patterns");

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
        registerAIActionsAsSkills();

        logger.info("A2A skills refreshed - {} skills registered", skillAdapters.size());
    }

    public boolean isSkillReady(String skillId) {
        return skillAdapters.containsKey(skillId) && readyService.isReady(A2A_SKILLS_READY);
    }

    @Override
    public void onReadyMarkerAdded(ReadyMarker readyMarker) {
        logger.debug("Ready marker added: {}", readyMarker);

        // Check if this is a marker we're waiting for
        if (isAIActionRegistryMarker(readyMarker)) {
            checkSkillsReady();
        }
    }

    @Override
    public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
        logger.debug("Ready marker removed: {}", readyMarker);

        // If AI action registry becomes unavailable, unmark skills as ready
        if (isAIActionRegistryMarker(readyMarker)) {
            readyService.unmarkReady(A2A_SKILLS_READY);
        }
    }

    private boolean isAIActionRegistryMarker(ReadyMarker marker) {
        // Check if this is the AI action registry marker
        return "ai".equals(marker.getType()) && "actions".equals(marker.getIdentifier());
    }

    private void checkSkillsReady() {
        logger.debug("Checking A2A skills readiness");

        // Check if AI action registry is ready
        ReadyMarker aiActionsReady = new ReadyMarker("ai", "actions");
        boolean actionsReady = readyService.isReady(aiActionsReady);

        if (actionsReady) {
            logger.debug("AI actions are ready, initializing A2A skills");
            initializeSkillRegistry();
        } else {
            logger.debug("AI actions not ready yet");
        }
    }

    // Enhanced utility methods for SDK integration
    public List<String> getSkillIds() {
        return new ArrayList<>(skillAdapters.keySet());
    }

    public A2ASkillAdapter getSkillAdapter(String skillId) {
        return skillAdapters.get(skillId);
    }

    public Map<String, Object> getSkillMetadata(String skillId) {
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
