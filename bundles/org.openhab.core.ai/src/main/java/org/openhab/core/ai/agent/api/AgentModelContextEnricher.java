package org.openhab.core.ai.agent.api;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ContextValidationResult;
import org.openhab.core.ai.action.DefaultContextValidationResult;
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
     * Enrich an agent model context with additional information.
     * 
     * @param context The original context to enrich
     * @return The enriched context
     */
    public AgentModelContext enrich(AgentModelContext context) {
        logger.debug("Enriching context: {}", context.getContextId());

        Map<String, Object> enrichedData = new ConcurrentHashMap<>(context.getContextData());
        Map<String, Object> enrichedMetadata = new ConcurrentHashMap<>(context.getMetadata());

        // Enrich with domain-specific information
        enrichDomainContext(enrichedData, context);

        // Enrich with temporal context
        enrichTemporalContext(enrichedData, context);

        // Enrich with spatial context
        enrichSpatialContext(enrichedData, context);

        // Enrich with user context
        enrichUserContext(enrichedData, context);

        // Enrich with system context
        enrichSystemContext(enrichedData, context);

        // Add enrichment metadata
        enrichedMetadata.put("enriched", true);
        enrichedMetadata.put("enrichmentTimestamp", System.currentTimeMillis());

        return new AgentModelContext(context.getContextId(), enrichedData, enrichedMetadata);
    }

    /**
     * Enrich context with domain-specific information.
     * 
     * @param enrichedData The enriched data map
     * @param originalContext The original context
     */
    private void enrichDomainContext(Map<String, Object> enrichedData, AgentModelContext originalContext) {
        String agentType = (String) originalContext.getContextData("agentType");
        if (agentType != null) {
            switch (agentType.toLowerCase()) {
                case "energy":
                    enrichEnergyContext(enrichedData, originalContext);
                    break;
                case "security":
                    enrichSecurityContext(enrichedData, originalContext);
                    break;
                case "comfort":
                    enrichComfortContext(enrichedData, originalContext);
                    break;
                default:
                    logger.debug("Unknown agent type for enrichment: {}", agentType);
            }
        }
    }

    /**
     * Enrich energy agent context.
     * 
     * @param enrichedData The enriched data map
     * @param originalContext The original context
     */
    private void enrichEnergyContext(Map<String, Object> enrichedData, AgentModelContext originalContext) {
        Map<String, Object> energyContext = new ConcurrentHashMap<>();

        // Add energy-specific capabilities
        energyContext.put("energyMonitoring", true);
        energyContext.put("loadOptimization", true);
        energyContext.put("demandResponse", true);
        energyContext.put("renewableIntegration", true);

        // Add energy domain knowledge
        energyContext.put("peakHours", "14:00-18:00");
        energyContext.put("offPeakHours", "22:00-06:00");
        energyContext.put("energyUnits", "kWh");
        energyContext.put("costOptimization", true);

        enrichedData.put("domainContext", energyContext);
    }

    /**
     * Enrich security agent context.
     * 
     * @param enrichedData The enriched data map
     * @param originalContext The original context
     */
    private void enrichSecurityContext(Map<String, Object> enrichedData, AgentModelContext originalContext) {
        Map<String, Object> securityContext = new ConcurrentHashMap<>();

        // Add security-specific capabilities
        securityContext.put("intrusionDetection", true);
        securityContext.put("accessControl", true);
        securityContext.put("surveillance", true);
        securityContext.put("alarmManagement", true);

        // Add security domain knowledge
        securityContext.put("securityLevels", "LOW,MEDIUM,HIGH,CRITICAL");
        securityContext.put("responseTime", "immediate");
        securityContext.put("notificationChannels", "email,sms,push");
        securityContext.put("emergencyContacts", true);

        enrichedData.put("domainContext", securityContext);
    }

    /**
     * Enrich comfort agent context.
     * 
     * @param enrichedData The enriched data map
     * @param originalContext The original context
     */
    private void enrichComfortContext(Map<String, Object> enrichedData, AgentModelContext originalContext) {
        Map<String, Object> comfortContext = new ConcurrentHashMap<>();

        // Add comfort-specific capabilities
        comfortContext.put("temperatureControl", true);
        comfortContext.put("humidityControl", true);
        comfortContext.put("lightingControl", true);
        comfortContext.put("airQualityControl", true);

        // Add comfort domain knowledge
        comfortContext.put("comfortZones", "living,bedroom,kitchen,bathroom");
        comfortContext.put("temperatureRange", "18-24°C");
        comfortContext.put("humidityRange", "40-60%");
        comfortContext.put("lightingLevels", "dim,normal,bright");

        enrichedData.put("domainContext", comfortContext);
    }

    /**
     * Enrich context with temporal information.
     * 
     * @param enrichedData The enriched data map
     * @param originalContext The original context
     */
    private void enrichTemporalContext(Map<String, Object> enrichedData, AgentModelContext originalContext) {
        Map<String, Object> temporalContext = new ConcurrentHashMap<>();

        long currentTime = System.currentTimeMillis();
        temporalContext.put("timestamp", currentTime);
        temporalContext.put("hourOfDay", Instant.ofEpochMilli(currentTime).atZone(ZoneId.systemDefault()).getHour());
        temporalContext.put("dayOfWeek",
                Instant.ofEpochMilli(currentTime).atZone(ZoneId.systemDefault()).getDayOfWeek());
        temporalContext.put("isWeekend", isWeekend(currentTime));
        temporalContext.put("isBusinessHours", isBusinessHours(currentTime));
        temporalContext.put("isNightTime", isNightTime(currentTime));

        enrichedData.put("temporalContext", temporalContext);
    }

    /**
     * Enrich context with spatial information.
     * 
     * @param enrichedData The enriched data map
     * @param originalContext The original context
     */
    private void enrichSpatialContext(Map<String, Object> enrichedData, AgentModelContext originalContext) {
        Map<String, Object> spatialContext = new ConcurrentHashMap<>();

        // Add spatial context information
        spatialContext.put("location", "home");
        spatialContext.put("rooms", "living,bedroom,kitchen,bathroom,office");
        spatialContext.put("zones", "indoor,outdoor");
        spatialContext.put("floorPlan", true);
        spatialContext.put("roomMapping", true);

        enrichedData.put("spatialContext", spatialContext);
    }

    /**
     * Enrich context with user information.
     * 
     * @param enrichedData The enriched data map
     * @param originalContext The original context
     */
    private void enrichUserContext(Map<String, Object> enrichedData, AgentModelContext originalContext) {
        Map<String, Object> userContext = new ConcurrentHashMap<>();

        // Add user context information
        userContext.put("userPresence", "unknown");
        userContext.put("userActivity", "unknown");
        userContext.put("userPreferences", originalContext.getContextData("userPreferences"));
        userContext.put("userSchedule", true);
        userContext.put("userHistory", originalContext.getContextData("history"));

        enrichedData.put("userContext", userContext);
    }

    /**
     * Enrich context with system information.
     * 
     * @param enrichedData The enriched data map
     * @param originalContext The original context
     */
    private void enrichSystemContext(Map<String, Object> enrichedData, AgentModelContext originalContext) {
        Map<String, Object> systemContext = new ConcurrentHashMap<>();

        // Add system context information
        systemContext.put("systemStatus", "operational");
        systemContext.put("availableResources", true);
        systemContext.put("networkConnectivity", true);
        systemContext.put("dataAccess", true);
        systemContext.put("currentState", originalContext.getContextData("currentState"));

        enrichedData.put("systemContext", systemContext);
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
        if (!context.hasContextData("agentId")) {
            result.addIssue("Missing required field: agentId");
        }

        if (!context.hasContextData("agentType")) {
            result.addIssue("Missing required field: agentType");
        }

        if (!context.hasContextData("domain")) {
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
        for (Map.Entry<String, Object> entry : context.getContextData().entrySet()) {
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
        String agentType = (String) context.getContextData("agentType");
        String domain = (String) context.getContextData("domain");

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
        if (!context.hasContextData("capabilities") && !context.hasContextData("skills")) {
            result.addRecommendation("Consider adding capabilities or skills information");
        }

        if (!context.hasContextData("currentState")) {
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
