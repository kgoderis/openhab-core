package org.openhab.core.ai.agent.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for AgentModelContext.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class AgentModelContextBuilder {
    String agentId = "";
    String specialization = "";
    String domain = "";
    Map<String, Object> capabilities = new HashMap<>();
    Map<String, Object> constraints = new HashMap<>();
    Map<String, String> promptTemplates = new HashMap<>();
    Map<String, Object> preferences = new HashMap<>();
    Map<String, Object> optimizationSettings = new HashMap<>();
    Map<String, Object> securitySettings = new HashMap<>();
    Map<String, Object> monitoringSettings = new HashMap<>();
    Instant createdAt = Instant.now();
    Instant lastUpdated = Instant.now();

    public AgentModelContextBuilder agentId(String agentId) { this.agentId = agentId; return this; }
    public AgentModelContextBuilder specialization(String specialization) { this.specialization = specialization; return this; }
    public AgentModelContextBuilder domain(String domain) { this.domain = domain; return this; }
    public AgentModelContextBuilder capabilities(Map<String, Object> capabilities) { this.capabilities = new HashMap<>(capabilities); return this; }
    public AgentModelContextBuilder constraints(Map<String, Object> constraints) { this.constraints = new HashMap<>(constraints); return this; }
    public AgentModelContextBuilder promptTemplates(Map<String, String> promptTemplates) { this.promptTemplates = new HashMap<>(promptTemplates); return this; }
    public AgentModelContextBuilder preferences(Map<String, Object> preferences) { this.preferences = new HashMap<>(preferences); return this; }
    public AgentModelContextBuilder optimizationSettings(Map<String, Object> optimizationSettings) { this.optimizationSettings = new HashMap<>(optimizationSettings); return this; }
    public AgentModelContextBuilder securitySettings(Map<String, Object> securitySettings) { this.securitySettings = new HashMap<>(securitySettings); return this; }
    public AgentModelContextBuilder monitoringSettings(Map<String, Object> monitoringSettings) { this.monitoringSettings = new HashMap<>(monitoringSettings); return this; }
    public AgentModelContextBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
    public AgentModelContextBuilder lastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; return this; }
    public AgentModelContext build() { return new AgentModelContext(this); }
}


