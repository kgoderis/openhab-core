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

    private AgentModelContext(Builder builder) {
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String agentId = "";
        private String specialization = "";
        private String domain = "";
        private Map<String, Object> capabilities = new HashMap<>();
        private Map<String, Object> constraints = new HashMap<>();
        private Map<String, String> promptTemplates = new HashMap<>();
        private Map<String, Object> preferences = new HashMap<>();
        private Map<String, Object> optimizationSettings = new HashMap<>();
        private Map<String, Object> securitySettings = new HashMap<>();
        private Map<String, Object> monitoringSettings = new HashMap<>();
        private Instant createdAt = Instant.now();
        private Instant lastUpdated = Instant.now();

        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public Builder specialization(String specialization) {
            this.specialization = specialization;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder capabilities(Map<String, Object> capabilities) {
            this.capabilities = new HashMap<>(capabilities);
            return this;
        }

        public Builder constraints(Map<String, Object> constraints) {
            this.constraints = new HashMap<>(constraints);
            return this;
        }

        public Builder promptTemplates(Map<String, String> promptTemplates) {
            this.promptTemplates = new HashMap<>(promptTemplates);
            return this;
        }

        public Builder preferences(Map<String, Object> preferences) {
            this.preferences = new HashMap<>(preferences);
            return this;
        }

        public Builder optimizationSettings(Map<String, Object> optimizationSettings) {
            this.optimizationSettings = new HashMap<>(optimizationSettings);
            return this;
        }

        public Builder securitySettings(Map<String, Object> securitySettings) {
            this.securitySettings = new HashMap<>(securitySettings);
            return this;
        }

        public Builder monitoringSettings(Map<String, Object> monitoringSettings) {
            this.monitoringSettings = new HashMap<>(monitoringSettings);
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder lastUpdated(Instant lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public AgentModelContext build() {
            return new AgentModelContext(this);
        }
    }
}
