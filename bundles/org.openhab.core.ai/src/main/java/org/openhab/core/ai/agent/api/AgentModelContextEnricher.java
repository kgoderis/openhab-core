package org.openhab.core.ai.agent.api;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ContextValidationResult;
import org.openhab.core.ai.action.DefaultContextValidationResult;
import org.openhab.core.ai.common.context.AgentModelContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Model Context Enricher for context enrichment and validation.
 * 
 * This class enhances agent contexts with additional information, validates
 * context data, and ensures context completeness for optimal model reasoning.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AgentModelContextEnricher.class)
@NonNullByDefault
public class AgentModelContextEnricher {

    private static final Logger logger = LoggerFactory.getLogger(AgentModelContextEnricher.class);

    /**
     * Enrich an agent model context with additional data.
     * 
     * @param context the context to enrich
     * @return enriched context
     */
    public AgentModelContext enrich(AgentModelContext context) {
        if (context == null) {
            logger.warn("Cannot enrich null context");
            return null;
        }

        try {
            // Get existing context data
            Map<String, Object> existingData = context.getAllValues();
            Map<String, Object> enrichedData = new HashMap<>(existingData);

            // Add enrichment data
            enrichedData.put("enriched", true);
            enrichedData.put("enrichmentTimestamp", Instant.now().toString());
            enrichedData.put("enrichmentSource", "AgentModelContextEnricher");

            // Create enriched context using builder
            return AgentModelContext.builder().withContextId(context.getContextId()).withValue("enriched", true)
                    .withValue("enrichmentTimestamp", Instant.now().toString())
                    .withValue("enrichmentSource", "AgentModelContextEnricher").build();

        } catch (Exception e) {
            logger.error("Error enriching context: {}", e.getMessage(), e);
            return context; // Return original context on error
        }
    }

    /**
     * Enrich context with user preferences.
     * 
     * @param context the context to enrich
     * @param userPreferences user preferences to add
     * @return enriched context
     */
    public AgentModelContext enrichWithUserPreferences(AgentModelContext context, Map<String, Object> userPreferences) {
        if (context == null || userPreferences == null) {
            logger.warn("Cannot enrich context with null user preferences");
            return context;
        }

        try {
            // Check if context already has user preferences
            if (context.hasValue("userPreferences")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> existingPreferences = (Map<String, Object>) context.getValue("userPreferences");
                Map<String, Object> mergedPreferences = new HashMap<>(existingPreferences);
                mergedPreferences.putAll(userPreferences);

                return AgentModelContext.builder().withContextId(context.getContextId())
                        .withUserPreferences(mergedPreferences).build();
            } else {
                return AgentModelContext.builder().withContextId(context.getContextId())
                        .withUserPreferences(userPreferences).build();
            }

        } catch (Exception e) {
            logger.error("Error enriching context with user preferences: {}", e.getMessage(), e);
            return context;
        }
    }

    /**
     * Enrich context with environmental data.
     * 
     * @param context the context to enrich
     * @param environment environmental data to add
     * @return enriched context
     */
    public AgentModelContext enrichWithEnvironment(AgentModelContext context, Map<String, Object> environment) {
        if (context == null || environment == null) {
            logger.warn("Cannot enrich context with null environment data");
            return context;
        }

        try {
            // Check if context already has environment data
            if (context.hasValue("environment")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> existingEnvironment = (Map<String, Object>) context.getValue("environment");
                Map<String, Object> mergedEnvironment = new HashMap<>(existingEnvironment);
                mergedEnvironment.putAll(environment);

                return AgentModelContext.builder().withContextId(context.getContextId())
                        .withEnvironment(mergedEnvironment).build();
            } else {
                return AgentModelContext.builder().withContextId(context.getContextId()).withEnvironment(environment)
                        .build();
            }

        } catch (Exception e) {
            logger.error("Error enriching context with environment data: {}", e.getMessage(), e);
            return context;
        }
    }

    /**
     * Enrich context with reasoning parameters.
     * 
     * @param context the context to enrich
     * @param reasoning reasoning parameters to add
     * @return enriched context
     */
    public AgentModelContext enrichWithReasoning(AgentModelContext context, Map<String, Object> reasoning) {
        if (context == null || reasoning == null) {
            logger.warn("Cannot enrich context with null reasoning data");
            return context;
        }

        try {
            // Check if context already has reasoning data
            if (context.hasValue("reasoning")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> existingReasoning = (Map<String, Object>) context.getValue("reasoning");
                Map<String, Object> mergedReasoning = new HashMap<>(existingReasoning);
                mergedReasoning.putAll(reasoning);

                return AgentModelContext.builder().withContextId(context.getContextId()).withReasoning(mergedReasoning)
                        .build();
            } else {
                return AgentModelContext.builder().withContextId(context.getContextId()).withReasoning(reasoning)
                        .build();
            }

        } catch (Exception e) {
            logger.error("Error enriching context with reasoning data: {}", e.getMessage(), e);
            return context;
        }
    }

    /**
     * Enrich context with current state.
     * 
     * @param context the context to enrich
     * @param currentState current state to add
     * @return enriched context
     */
    public AgentModelContext enrichWithCurrentState(AgentModelContext context, Map<String, Object> currentState) {
        if (context == null || currentState == null) {
            logger.warn("Cannot enrich context with null current state");
            return context;
        }

        try {
            // Check if context already has current state
            if (context.hasValue("currentState")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> existingState = (Map<String, Object>) context.getValue("currentState");
                Map<String, Object> mergedState = new HashMap<>(existingState);
                mergedState.putAll(currentState);

                return AgentModelContext.builder().withContextId(context.getContextId()).withCurrentState(mergedState)
                        .build();
            } else {
                return AgentModelContext.builder().withContextId(context.getContextId()).withCurrentState(currentState)
                        .build();
            }

        } catch (Exception e) {
            logger.error("Error enriching context with current state: {}", e.getMessage(), e);
            return context;
        }
    }

    /**
     * Enrich context with capabilities.
     * 
     * @param context the context to enrich
     * @param capabilities capabilities to add
     * @return enriched context
     */
    public AgentModelContext enrichWithCapabilities(AgentModelContext context, Map<String, String> capabilities) {
        if (context == null || capabilities == null) {
            logger.warn("Cannot enrich context with null capabilities");
            return context;
        }

        try {
            // Check if context already has capabilities
            if (context.hasValue("capabilities")) {
                @SuppressWarnings("unchecked")
                Map<String, String> existingCapabilities = (Map<String, String>) context.getValue("capabilities");
                Map<String, String> mergedCapabilities = new HashMap<>(existingCapabilities);
                mergedCapabilities.putAll(capabilities);

                return AgentModelContext.builder().withContextId(context.getContextId())
                        .withCapabilities(mergedCapabilities).build();
            } else {
                return AgentModelContext.builder().withContextId(context.getContextId()).withCapabilities(capabilities)
                        .build();
            }

        } catch (Exception e) {
            logger.error("Error enriching context with capabilities: {}", e.getMessage(), e);
            return context;
        }
    }

    /**
     * Enrich context with skills.
     * 
     * @param context the context to enrich
     * @param skills skills to add
     * @return enriched context
     */
    public AgentModelContext enrichWithSkills(AgentModelContext context, Map<String, String> skills) {
        if (context == null || skills == null) {
            logger.warn("Cannot enrich context with null skills");
            return context;
        }

        try {
            // Check if context already has skills
            if (context.hasValue("skills")) {
                @SuppressWarnings("unchecked")
                Map<String, String> existingSkills = (Map<String, String>) context.getValue("skills");
                Map<String, String> mergedSkills = new HashMap<>(existingSkills);
                mergedSkills.putAll(skills);

                return AgentModelContext.builder().withContextId(context.getContextId()).withSkills(mergedSkills)
                        .build();
            } else {
                return AgentModelContext.builder().withContextId(context.getContextId()).withSkills(skills).build();
            }

        } catch (Exception e) {
            logger.error("Error enriching context with skills: {}", e.getMessage(), e);
            return context;
        }
    }

    /**
     * Enrich context with history.
     * 
     * @param context the context to enrich
     * @param history history data to add
     * @return enriched context
     */
    public AgentModelContext enrichWithHistory(AgentModelContext context, Object history) {
        if (context == null) {
            logger.warn("Cannot enrich null context with history");
            return null;
        }

        try {
            return AgentModelContext.builder().withContextId(context.getContextId()).withHistory(history).build();

        } catch (Exception e) {
            logger.error("Error enriching context with history: {}", e.getMessage(), e);
            return context;
        }
    }

    /**
     * Enrich context with specialization.
     * 
     * @param context the context to enrich
     * @param specialization specialization to add
     * @return enriched context
     */
    public AgentModelContext enrichWithSpecialization(AgentModelContext context, String specialization) {
        if (context == null || specialization == null) {
            logger.warn("Cannot enrich context with null specialization");
            return context;
        }

        try {
            return AgentModelContext.builder().withContextId(context.getContextId()).withSpecialization(specialization)
                    .build();

        } catch (Exception e) {
            logger.error("Error enriching context with specialization: {}", e.getMessage(), e);
            return context;
        }
    }

    /**
     * Enrich context with priority.
     * 
     * @param context the context to enrich
     * @param priority priority to add
     * @return enriched context
     */
    public AgentModelContext enrichWithPriority(AgentModelContext context, String priority) {
        if (context == null || priority == null) {
            logger.warn("Cannot enrich context with null priority");
            return context;
        }

        try {
            return AgentModelContext.builder().withContextId(context.getContextId()).withPriority(priority).build();

        } catch (Exception e) {
            logger.error("Error enriching context with priority: {}", e.getMessage(), e);
            return context;
        }
    }

    /**
     * Enrich context with source information.
     * 
     * @param context the context to enrich
     * @param source source information to add
     * @return enriched context
     */
    public AgentModelContext enrichWithSource(AgentModelContext context, String source) {
        if (context == null || source == null) {
            logger.warn("Cannot enrich context with null source");
            return context;
        }

        try {
            return AgentModelContext.builder().withContextId(context.getContextId()).withSource(source).build();

        } catch (Exception e) {
            logger.error("Error enriching context with source: {}", e.getMessage(), e);
            return context;
        }
    }

    /**
     * Validate the agent model context.
     * 
     * @param context The context to validate
     * @return Validation result with issues and recommendations
     */
    public ContextValidationResult validate(AgentModelContext context) {
        DefaultContextValidationResult result = new DefaultContextValidationResult();

        // Check required fields
        validateRequiredFields(context, result);

        // Check data quality
        validateDataQuality(context, result);

        // Check consistency
        validateConsistency(context, result);

        // Check completeness
        validateCompleteness(context, result);

        return result;
    }

    /**
     * Validate required fields in the context.
     * 
     * @param context The context to validate
     * @param result The validation result to update
     */
    private void validateRequiredFields(AgentModelContext context, DefaultContextValidationResult result) {
        if (!context.hasValue("agentId")) {
            result.addIssue("Missing required field: agentId");
        }

        if (!context.hasValue("agentType")) {
            result.addIssue("Missing required field: agentType");
        }

        if (!context.hasValue("domain")) {
            result.addIssue("Missing required field: domain");
        }
    }

    /**
     * Validate data quality in the context.
     * 
     * @param context The context to validate
     * @param result The validation result to update
     */
    private void validateDataQuality(AgentModelContext context, DefaultContextValidationResult result) {
        // Check for null or empty values
        for (Map.Entry<String, Object> entry : context.getAllValues().entrySet()) {
            if (entry.getValue() == null) {
                result.addIssue("Null value found for field: " + entry.getKey());
            }
        }
    }

    /**
     * Validate consistency in the context.
     * 
     * @param context The context to validate
     * @param result The validation result to update
     */
    private void validateConsistency(AgentModelContext context, DefaultContextValidationResult result) {
        String agentType = (String) context.getValue("agentType");
        String domain = (String) context.getValue("domain");

        if (agentType != null && domain != null) {
            // Check if agent type and domain are consistent
            if (!isConsistent(agentType, domain)) {
                result.addIssue("Inconsistent agent type and domain: " + agentType + " vs " + domain);
            }
        }
    }

    /**
     * Validate completeness of the context.
     * 
     * @param context The context to validate
     * @param result The validation result to update
     */
    private void validateCompleteness(AgentModelContext context, DefaultContextValidationResult result) {
        // Check if context has minimum required information
        if (!context.hasValue("capabilities") && !context.hasValue("skills")) {
            result.addRecommendation("Consider adding capabilities or skills information");
        }

        if (!context.hasValue("currentState")) {
            result.addRecommendation("Consider adding current state information");
        }
    }

    /**
     * Check if agent type and domain are consistent.
     * 
     * @param agentType The agent type
     * @param domain The domain
     * @return True if consistent, false otherwise
     */
    private boolean isConsistent(String agentType, String domain) {
        // Implement proper agent type and domain consistency validation
        if (agentType == null || domain == null) {
            return false;
        }

        // Define valid agent type and domain combinations
        Map<String, Set<String>> validCombinations = new HashMap<>();

        // Energy domain agents
        validCombinations.put("energy",
                Set.of("energy_optimizer", "power_monitor", "solar_controller", "battery_manager"));

        // Security domain agents
        validCombinations.put("security",
                Set.of("security_monitor", "access_controller", "alarm_manager", "surveillance_agent"));

        // Comfort domain agents
        validCombinations.put("comfort",
                Set.of("climate_controller", "lighting_manager", "entertainment_controller", "comfort_optimizer"));

        // Automation domain agents
        validCombinations.put("automation",
                Set.of("scheduler", "rule_engine", "workflow_manager", "automation_coordinator"));

        // Health domain agents
        validCombinations.put("health",
                Set.of("health_monitor", "medication_reminder", "wellness_tracker", "emergency_responder"));

        // General purpose agents
        validCombinations.put("general", Set.of("assistant", "coordinator", "orchestrator", "supervisor"));

        // Check if the combination is valid
        Set<String> validAgentTypes = validCombinations.get(domain.toLowerCase());
        if (validAgentTypes != null) {
            return validAgentTypes.contains(agentType.toLowerCase());
        }

        // If domain not found, check if it's a custom domain (allow with warning)
        logger.debug("Unknown domain '{}' for agent type '{}', allowing as custom domain", domain, agentType);
        return true;
    }

    /**
     * Check if current time is weekend.
     * 
     * @param timestamp The timestamp to check
     * @return True if weekend, false otherwise
     */
    private boolean isWeekend(long timestamp) {
        DayOfWeek dayOfWeek = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Check if current time is business hours.
     * 
     * @param timestamp The timestamp to check
     * @return True if business hours, false otherwise
     */
    private boolean isBusinessHours(long timestamp) {
        int hour = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).getHour();
        return hour >= 9 && hour <= 17 && !isWeekend(timestamp);
    }

    /**
     * Check if current time is night time.
     * 
     * @param timestamp The timestamp to check
     * @return True if night time, false otherwise
     */
    private boolean isNightTime(long timestamp) {
        int hour = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).getHour();
        return hour >= 22 || hour <= 6;
    }

    /**
     * Context validation result.
     */
    // class extracted to top-level: org.openhab.core.ai.reasoning.ContextValidationResult
}
