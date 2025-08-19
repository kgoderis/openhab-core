package org.openhab.core.ai.agent.delegation;

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
 * Information and execution helper for a registered agent.
 *
 * author Karel Goderis - Initial Contribution
 * since 1.0.0
 */
@NonNullByDefault
public class AgentInfo {
    private final String agentId;
    private final List<String> capabilities;
    private final AtomicLong currentLoad;
    private final Map<String, Double> capabilityScores;

    public AgentInfo(String agentId, List<String> capabilities) {
        this.agentId = agentId;
        this.capabilities = capabilities;
        this.currentLoad = new AtomicLong(0);
        this.capabilityScores = new ConcurrentHashMap<>();
    }

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

    public CompletableFuture<ActionResult> executeAction(ExecutionContext actionContext) {
        // This would integrate with the actual agent execution system
        return CompletableFuture.completedFuture(ActionResult.success("Mock result from agent " + agentId, 100));
    }

    public void incrementLoad() {
        currentLoad.incrementAndGet();
    }

    public void decrementLoad() {
        currentLoad.decrementAndGet();
    }

    public int getCurrentLoad() {
        return (int) currentLoad.get();
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public String getAgentId() {
        return agentId;
    }
}
