package org.openhab.core.ai.agent.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.api.ActionKeys;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.common.context.ExecutionContext;

/**
 * Unified agent information class combining response data and execution capabilities.
 * 
 * This class provides comprehensive agent information including:
 * - Basic agent identification and status
 * - Capability management and scoring
 * - Load tracking and execution helpers
 * - REST response information
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AgentInfo {
    // Basic agent information
    private final String agentId;
    private final String status;
    private final String message;

    // Capability management
    private final List<String> capabilities;
    private final Map<String, Double> capabilityScores;

    // Load tracking
    private final AtomicLong currentLoad;

    /**
     * Create a new agent info with basic information.
     * 
     * @param agentId the agent identifier
     * @param status the agent status
     * @param message the status message
     */
    public AgentInfo(String agentId, String status, String message) {
        this.agentId = agentId;
        this.status = status;
        this.message = message;
        this.capabilities = List.of();
        this.capabilityScores = new ConcurrentHashMap<>();
        this.currentLoad = new AtomicLong(0);
    }

    /**
     * Create a new agent info with capabilities.
     * 
     * @param agentId the agent identifier
     * @param capabilities the list of agent capabilities
     */
    public AgentInfo(String agentId, List<String> capabilities) {
        this.agentId = agentId;
        this.status = "ACTIVE";
        this.message = "Agent is active and ready";
        this.capabilities = capabilities;
        this.capabilityScores = new ConcurrentHashMap<>();
        this.currentLoad = new AtomicLong(0);
    }

    /**
     * Create a new agent info with full configuration.
     * 
     * @param agentId the agent identifier
     * @param status the agent status
     * @param message the status message
     * @param capabilities the list of agent capabilities
     */
    public AgentInfo(String agentId, String status, String message, List<String> capabilities) {
        this.agentId = agentId;
        this.status = status;
        this.message = message;
        this.capabilities = capabilities;
        this.capabilityScores = new ConcurrentHashMap<>();
        this.currentLoad = new AtomicLong(0);
    }

    // Basic getters
    public String getAgentId() {
        return agentId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    // Capability management
    public boolean canHandleAction(String actionName) {
        return capabilities.contains(actionName);
    }

    public double getCapabilityScore(ExecutionContext actionContext) {
        @Nullable
        String actionName = actionContext.getValue(ActionKeys.ACTION_NAME.getKey(), String.class);
        if (actionName == null) {
            return 0.0;
        }
        return capabilityScores.getOrDefault(actionName, 1.0);
    }

    public void setCapabilityScore(String actionName, double score) {
        capabilityScores.put(actionName, score);
    }

    // Load management
    public void incrementLoad() {
        currentLoad.incrementAndGet();
    }

    public void decrementLoad() {
        currentLoad.decrementAndGet();
    }

    public int getCurrentLoad() {
        return (int) currentLoad.get();
    }

    // Execution helper
    public CompletableFuture<ActionResult> executeAction(ExecutionContext actionContext) {
        // This would integrate with the actual agent execution system
        return CompletableFuture.completedFuture(ActionResult.success("Mock result from agent " + agentId, 100));
    }

    // Builder pattern for complex construction
    public static Builder builder(String agentId) {
        return new Builder(agentId);
    }

    public static final class Builder {
        private final String agentId;
        private String status = "ACTIVE";
        private String message = "Agent is active and ready";
        private List<String> capabilities = List.of();

        public Builder(String agentId) {
            this.agentId = agentId;
        }

        public Builder withStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withCapabilities(List<String> capabilities) {
            this.capabilities = capabilities;
            return this;
        }

        public AgentInfo build() {
            return new AgentInfo(agentId, status, message, capabilities);
        }
    }
}
