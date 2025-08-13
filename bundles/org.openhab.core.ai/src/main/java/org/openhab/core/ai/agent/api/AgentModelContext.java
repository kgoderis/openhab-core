package org.openhab.core.ai.agent.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent-specific context for model integration
 * 
 * <p>
 * This class contains:
 * - Agent specialization and domain information
 * - Agent capabilities and constraints
 * - Agent-specific prompt templates and preferences
 * - Agent-specific model optimization settings
 * - Agent-specific security and access controls
 * - Agent-specific performance monitoring settings
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class AgentModelContext {

    private final String agentId;
    private final String specialization;
    private final String domain;
    private final Map<String, Object> capabilities;
    private final Map<String, Object> constraints;
    private final Map<String, String> promptTemplates;
    private final Map<String, Object> preferences;
    private final Map<String, Object> optimizationSettings;
    private final Map<String, Object> securitySettings;
    private final Map<String, Object> monitoringSettings;
    private final Instant createdAt;
    private final Instant lastUpdated;

    AgentModelContext(AgentModelContextBuilder builder) {
        this.agentId = builder.agentId;
        this.specialization = builder.specialization;
        this.domain = builder.domain;
        this.capabilities = new HashMap<>(builder.capabilities);
        this.constraints = new HashMap<>(builder.constraints);
        this.promptTemplates = new HashMap<>(builder.promptTemplates);
        this.preferences = new HashMap<>(builder.preferences);
        this.optimizationSettings = new HashMap<>(builder.optimizationSettings);
        this.securitySettings = new HashMap<>(builder.securitySettings);
        this.monitoringSettings = new HashMap<>(builder.monitoringSettings);
        this.createdAt = builder.createdAt;
        this.lastUpdated = builder.lastUpdated;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getDomain() {
        return domain;
    }

    public Map<String, Object> getCapabilities() {
        return new HashMap<>(capabilities);
    }

    public Map<String, Object> getConstraints() {
        return new HashMap<>(constraints);
    }

    public Map<String, String> getPromptTemplates() {
        return new HashMap<>(promptTemplates);
    }

    public Map<String, Object> getPreferences() {
        return new HashMap<>(preferences);
    }

    public Map<String, Object> getOptimizationSettings() {
        return new HashMap<>(optimizationSettings);
    }

    public Map<String, Object> getSecuritySettings() {
        return new HashMap<>(securitySettings);
    }

    public Map<String, Object> getMonitoringSettings() {
        return new HashMap<>(monitoringSettings);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public static AgentModelContextBuilder builder() {
        return new AgentModelContextBuilder();
    }

    /* Extracted: org.openhab.core.ai.agent.api.AgentModelContextBuilder */
}
