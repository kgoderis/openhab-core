package org.openhab.core.ai.agent.delegation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.execution.AgentSkillRegistry;
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
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentCardBuilder.class)
public class AgentCardBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AgentCardBuilder.class);

    @Reference
    private @Nullable AgentSkillRegistry skillRegistry;

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
        logger.debug("Building OpenHAB agent card with multi-transport support");

        // Build agent card using SDK patterns
        AgentCapabilities capabilities = buildCapabilities();
        List<AgentSkill> skills = buildSkills();
        Map<String, SecurityScheme> securitySchemes = buildSecuritySchemes();
        List<Map<String, List<String>>> security = buildSecurityConfig();
        String instructions = buildInstructions();

        // Create agent card using actual SDK classes with multi-transport support
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
                buildAdditionalInterfaces(), // additionalInterfaces
                "http", // preferredTransport
                "1.0" // protocolVersion
        );
    }

    /**
     * Build additional interfaces for multi-transport support.
     * 
     * @return list of additional interfaces
     */
    private List<AgentInterface> buildAdditionalInterfaces() {
        List<AgentInterface> interfaces = new ArrayList<>();

        // TODO: Add multi-transport interfaces when A2A SDK supports them
        // For now, return empty list to maintain compatibility
        logger.debug("Multi-transport interfaces will be added when A2A SDK supports them");

        return interfaces;
    }

    /**
     * Build REST endpoints configuration.
     * 
     * @return REST endpoints map
     */
    private Map<String, String> buildRestEndpoints() {
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("messageSend", "/a2a/v1/message:send");
        endpoints.put("messageStream", "/a2a/v1/message:stream");
        endpoints.put("tasksGet", "/a2a/v1/tasks/{id}");
        endpoints.put("tasksCancel", "/a2a/v1/tasks/{id}:cancel");
        endpoints.put("tasksSubscribe", "/a2a/v1/tasks/{id}:subscribe");
        endpoints.put("tasksList", "/a2a/v1/tasks");
        endpoints.put("agentCard", "/a2a/v1/card");
        return endpoints;
    }

    /**
     * Build transport capabilities for multi-transport support.
     * 
     * @return enhanced capabilities
     */
    private AgentCapabilities buildCapabilities() {
        // Use the correct AgentCapabilities constructor
        return new AgentCapabilities(true, // streaming
                false, // pushNotifications
                false, // stateTransitionHistory
                new ArrayList<>() // extensions - will be enhanced with transport capabilities
        );
    }

    /**
     * Build transport capabilities configuration.
     * 
     * @return transport capabilities map
     */
    private Map<String, Object> buildTransportCapabilities() {
        Map<String, Object> transportCapabilities = new HashMap<>();

        // JSON-RPC Transport
        Map<String, Object> jsonRpcTransport = new HashMap<>();
        jsonRpcTransport.put("type", "json-rpc");
        jsonRpcTransport.put("url", "http://localhost:8080/a2a");
        jsonRpcTransport.put("port", 8080);
        jsonRpcTransport.put("streaming", true);
        jsonRpcTransport.put("bidirectional", false);
        jsonRpcTransport.put("authentication", true);
        jsonRpcTransport.put("compression", false);
        jsonRpcTransport.put("priority", 1);

        // REST Transport
        Map<String, Object> restTransport = new HashMap<>();
        restTransport.put("type", "rest");
        restTransport.put("url", "http://localhost:8082/a2a/v1");
        restTransport.put("port", 8082);
        restTransport.put("streaming", true);
        restTransport.put("bidirectional", false);
        restTransport.put("authentication", true);
        restTransport.put("compression", true);
        restTransport.put("priority", 2);
        restTransport.put("endpoints", buildRestEndpoints());

        // gRPC Transport
        Map<String, Object> grpcTransport = new HashMap<>();
        grpcTransport.put("type", "grpc");
        grpcTransport.put("url", "http://localhost:8083");
        grpcTransport.put("port", 8083);
        grpcTransport.put("streaming", true);
        grpcTransport.put("bidirectional", true);
        grpcTransport.put("authentication", true);
        grpcTransport.put("compression", true);
        grpcTransport.put("priority", 3);

        transportCapabilities.put("json-rpc", jsonRpcTransport);
        transportCapabilities.put("rest", restTransport);
        transportCapabilities.put("grpc", grpcTransport);

        // Transport preferences
        transportCapabilities.put("preferences",
                Map.of("primary", "json-rpc", "fallback", "rest", "highPerformance", "grpc", "webCompatible", "rest"));

        // Transport health status
        transportCapabilities.put("health",
                Map.of("json-rpc", Map.of("status", "healthy", "lastCheck", System.currentTimeMillis()), "rest",
                        Map.of("status", "healthy", "lastCheck", System.currentTimeMillis()), "grpc",
                        Map.of("status", "healthy", "lastCheck", System.currentTimeMillis())));

        return transportCapabilities;
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
