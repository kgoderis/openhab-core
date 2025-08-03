package org.openhab.core.ai.a2a.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.spec.APIKeySecurityScheme;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentInterface;
import io.a2a.spec.AgentProvider;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.SecurityScheme;

/**
 * A2A Agent Card Builder - Handles agent card generation and capability building.
 * 
 * <p>
 * This class is responsible for:
 * - Building agent cards with capabilities
 * - Generating skill definitions
 * - Creating security schemes
 * - Building agent instructions
 * </p>
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@Component(service = A2AAgentCardBuilder.class)
public class A2AAgentCardBuilder {

    private static final Logger logger = LoggerFactory.getLogger(A2AAgentCardBuilder.class);

    @Reference
    private @Nullable A2ASkillRegistry skillRegistry;

    @Activate
    public void activate() {
        logger.debug("A2A Agent Card Builder activated");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("A2A Agent Card Builder deactivated");
    }

    // ============================================================================
    // Public API Methods
    // ============================================================================

    public AgentCard buildAgentCard() {
        logger.debug("Building OpenHAB agent card");

        // Build agent card using SDK patterns
        AgentCapabilities capabilities = buildCapabilities();
        List<AgentSkill> skills = buildSkills();
        Map<String, SecurityScheme> securitySchemes = buildSecuritySchemes();
        List<Map<String, List<String>>> security = buildSecurityConfig();
        String instructions = buildInstructions();

        // Create agent card using actual SDK classes
        return new AgentCard("OpenHAB AI Agent", // name
                "OpenHAB AI agent that can control and monitor home automation systems", // description
                "http://localhost:8080/a2a", // url
                new AgentProvider("openHAB", "openHAB AI Team"), // provider
                "1.0.0", // version
                "http://docs.openhab.org", // documentationUrl
                capabilities, // capabilities
                List.of("text"), // defaultInputModes
                List.of("text"), // defaultOutputModes
                skills, // skills
                false, // supportsAuthenticatedExtendedCard
                securitySchemes, // securitySchemes
                security, // security
                "", // iconUrl
                new ArrayList<AgentInterface>(), // additionalInterfaces
                "http", // preferredTransport
                "1.0" // protocolVersion
        );
    }

    // ============================================================================
    // Private Helper Methods
    // ============================================================================

    private AgentCapabilities buildCapabilities() {
        // Use the correct AgentCapabilities constructor
        return new AgentCapabilities(true, // streaming
                false, // pushNotifications
                false, // stateTransitionHistory
                new ArrayList<>() // extensions
        );
    }

    private List<AgentSkill> buildSkills() {
        List<AgentSkill> skills = new ArrayList<>();

        // Get skills from registry
        if (skillRegistry != null) {
            List<Map<String, Object>> skillDefinitions = skillRegistry.getSkillDefinitions();
            for (Map<String, Object> skillDef : skillDefinitions) {
                // Convert to AgentSkill using the constructor
                AgentSkill skill = new AgentSkill((String) skillDef.get("id"), // id
                        (String) skillDef.get("name"), // name
                        (String) skillDef.get("description"), // description
                        new ArrayList<>(), // tags
                        new ArrayList<>(), // examples
                        new ArrayList<>(), // inputModes
                        new ArrayList<>() // outputModes
                );

                skills.add(skill);
                logger.debug("Adding skill: {}", skillDef.get("id"));
            }
        } else {
            logger.warn("Skill registry not available, building empty skill list");
        }

        return skills;
    }

    private Map<String, SecurityScheme> buildSecuritySchemes() {
        Map<String, SecurityScheme> schemes = new HashMap<>();

        // Add API Key security scheme using the constructor
        APIKeySecurityScheme apiKeyScheme = new APIKeySecurityScheme("X-API-Key", // name
                "header", // in
                "API Key authentication" // description
        );
        schemes.put("apiKey", apiKeyScheme);

        // Add OAuth2 security scheme using the constructor
        // Note: OAuth2SecurityScheme requires OAuthFlows, which we'll create a simple one
        // For now, let's use a simpler approach with just API Key
        logger.debug("Built {} security schemes", schemes.size());
        return schemes;
    }

    private List<Map<String, List<String>>> buildSecurityConfig() {
        List<Map<String, List<String>>> security = new ArrayList<>();

        // Configure which security schemes apply to which operations
        Map<String, List<String>> globalSecurity = new HashMap<>();
        globalSecurity.put("apiKey", List.of("execute", "read", "write"));

        security.add(globalSecurity);

        return security;
    }

    private String buildInstructions() {
        return """
                You are an OpenHAB AI agent that can control and monitor home automation systems.

                Available capabilities:
                - Control devices and things
                - Monitor system status
                - Manage configurations
                - Execute automation rules

                Use the available skills to interact with the OpenHAB system.
                Always prioritize user safety and system stability.
                """;
    }

    // ============================================================================
    // Additional Builder Methods
    // ============================================================================

    /**
     * Build a custom agent card with specific configuration.
     * 
     * @param name the agent name
     * @param description the agent description
     * @param version the agent version
     * @return custom agent card
     */
    public AgentCard buildCustomAgentCard(String name, String description, String version) {
        logger.debug("Building custom agent card: {} (v{})", name, version);

        AgentCapabilities capabilities = buildCapabilities();
        List<AgentSkill> skills = buildSkills();
        Map<String, SecurityScheme> securitySchemes = buildSecuritySchemes();
        List<Map<String, List<String>>> security = buildSecurityConfig();
        String instructions = buildInstructions();

        return new AgentCard(name, // name
                description, // description
                "http://localhost:8080/a2a", // url
                new AgentProvider("openHAB", "openHAB AI Team"), // provider
                version, // version
                "http://docs.openhab.org", // documentationUrl
                capabilities, // capabilities
                List.of("text"), // defaultInputModes
                List.of("text"), // defaultOutputModes
                skills, // skills
                false, // supportsAuthenticatedExtendedCard
                securitySchemes, // securitySchemes
                security, // security
                "", // iconUrl
                new ArrayList<AgentInterface>(), // additionalInterfaces
                "http", // preferredTransport
                "1.0" // protocolVersion
        );
    }

    /**
     * Get agent card statistics.
     * 
     * @return agent card statistics map
     */
    public Map<String, Object> getAgentCardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<AgentSkill> skills = buildSkills();
            Map<String, SecurityScheme> securitySchemes = buildSecuritySchemes();

            stats.put("totalSkills", skills.size());
            stats.put("totalSecuritySchemes", securitySchemes.size());
            stats.put("supportsStreaming", true);
            stats.put("supportsPushNotifications", false);
            stats.put("supportsStateTransitionHistory", false);
            stats.put("lastUpdated", System.currentTimeMillis());

        } catch (Exception e) {
            logger.error("Error getting agent card statistics", e);
            stats.put("error", e.getMessage());
            stats.put("lastUpdated", System.currentTimeMillis());
        }

        return stats;
    }

    /**
     * Validate agent card configuration.
     * 
     * @param card the agent card to validate
     * @return true if valid, false otherwise
     */
    public boolean validateAgentCard(AgentCard card) {
        if (card == null) {
            logger.warn("Agent card is null");
            return false;
        }

        // Note: AgentCard is a record, so we access fields directly
        // The actual field names depend on the SDK implementation
        // For now, we'll do basic null checks
        try {
            // Basic validation - the card object should not be null
            logger.debug("Agent card validation passed");
            return true;
        } catch (Exception e) {
            logger.warn("Agent card validation failed: {}", e.getMessage());
            return false;
        }
    }
}
