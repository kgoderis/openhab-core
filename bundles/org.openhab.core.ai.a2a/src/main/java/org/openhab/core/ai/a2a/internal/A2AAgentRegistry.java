package org.openhab.core.ai.a2a.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.a2a.api.A2AAgent;
import org.osgi.service.component.annotations.Component;

/**
 * Agent registry for A2A agents, supporting registration, discovery, capability management,
 * lifecycle, and performance monitoring.
 *
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = A2AAgentRegistry.class)
@NonNullByDefault
public class A2AAgentRegistry {

    // Agent storage
    private final Map<String, A2AAgent> agents = new ConcurrentHashMap<>();
    // Capability mapping: agentId -> set of capabilities
    private final Map<String, Set<String>> agentCapabilities = new ConcurrentHashMap<>();
    // Capability reverse mapping: capability -> set of agentIds
    private final Map<String, Set<String>> capabilityAgents = new ConcurrentHashMap<>();
    // Agent status
    private final Map<String, A2AAgent.AgentStatus> agentStatus = new ConcurrentHashMap<>();
    // Agent metrics
    private final Map<String, A2AAgent.AgentMetrics> agentMetrics = new ConcurrentHashMap<>();

    /**
     * Register an agent with the registry.
     */
    public void registerAgent(String agentId, A2AAgent agent) {
        agents.put(agentId, agent);
        agentCapabilities.putIfAbsent(agentId, new CopyOnWriteArraySet<>());
        agentStatus.put(agentId, A2AAgent.AgentStatus.STOPPED);
        agentMetrics.putIfAbsent(agentId, new AgentMetricsImpl(agentId));
    }

    /**
     * Unregister an agent from the registry.
     */
    public void unregisterAgent(String agentId) {
        agents.remove(agentId);
        agentCapabilities.remove(agentId);
        agentStatus.remove(agentId);
        agentMetrics.remove(agentId);
        // Remove from capability reverse mapping
        for (Set<String> agentSet : capabilityAgents.values()) {
            agentSet.remove(agentId);
        }
    }

    /**
     * Get an agent by ID.
     */
    public @Nullable A2AAgent getAgent(String agentId) {
        return agents.get(agentId);
    }

    /**
     * Get all registered agent IDs.
     */
    public List<String> getRegisteredAgentIds() {
        return List.copyOf(agents.keySet());
    }

    /**
     * Register a capability for an agent.
     */
    public void registerCapability(String agentId, String capability) {
        agentCapabilities.computeIfAbsent(agentId, k -> new CopyOnWriteArraySet<>()).add(capability);
        capabilityAgents.computeIfAbsent(capability, k -> new CopyOnWriteArraySet<>()).add(agentId);
    }

    /**
     * Get all capabilities for an agent.
     */
    public List<String> getAgentCapabilities(String agentId) {
        Set<String> caps = agentCapabilities.get(agentId);
        return caps != null ? List.copyOf(caps) : List.of();
    }

    /**
     * Find all agents with a given capability.
     */
    public List<String> findAgentsWithCapability(String capability) {
        Set<String> agentIds = capabilityAgents.get(capability);
        return agentIds != null ? List.copyOf(agentIds) : List.of();
    }

    /**
     * Start an agent (stub).
     */
    public void startAgent(String agentId) {
        agentStatus.put(agentId, A2AAgent.AgentStatus.RUNNING);
        // TODO: Actually start agent if needed
    }

    /**
     * Stop an agent (stub).
     */
    public void stopAgent(String agentId) {
        agentStatus.put(agentId, A2AAgent.AgentStatus.STOPPED);
        // TODO: Actually stop agent if needed
    }

    /**
     * Get the status of an agent.
     */
    public A2AAgent.AgentStatus getAgentStatus(String agentId) {
        return agentStatus.getOrDefault(agentId, A2AAgent.AgentStatus.UNKNOWN);
    }

    /**
     * Get metrics for an agent.
     */
    public A2AAgent.@Nullable AgentMetrics getAgentMetrics(String agentId) {
        return agentMetrics.computeIfAbsent(agentId, k -> new AgentMetricsImpl(k));
    }

    /**
     * Record an agent execution for metrics.
     */
    public void recordAgentExecution(String agentId, long executionTime, boolean success) {
        AgentMetricsImpl metrics = (AgentMetricsImpl) agentMetrics.computeIfAbsent(agentId,
                k -> new AgentMetricsImpl(k));
        metrics.recordExecution(executionTime, success);
    }

    /**
     * Implementation of AgentMetrics interface.
     */
    private static class AgentMetricsImpl implements A2AAgent.AgentMetrics {
        private final String agentId;
        private final List<Long> executionTimes = new CopyOnWriteArrayList<>();
        private int successCount = 0;
        private int failureCount = 0;

        public AgentMetricsImpl(String agentId) {
            this.agentId = agentId;
        }

        public void recordExecution(long time, boolean success) {
            executionTimes.add(time);
            if (success) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        @Override
        public long[] getExecutionTimes() {
            return executionTimes.stream().mapToLong(Long::longValue).toArray();
        }

        @Override
        public long getSuccessCount() {
            return successCount;
        }

        @Override
        public long getFailureCount() {
            return failureCount;
        }

        @Override
        public double getAverageExecutionTime() {
            return executionTimes.isEmpty() ? 0.0
                    : executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }

        @Override
        public long getTotalExecutions() {
            return executionTimes.size();
        }
    }

    // TODO: Security, validation, communication, ownership/access controls
}
