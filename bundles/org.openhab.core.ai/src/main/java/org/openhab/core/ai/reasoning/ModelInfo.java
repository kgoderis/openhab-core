package org.openhab.core.ai.reasoning;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ModelInfo {
    private final String modelId;
    private final String name;
    private final String provider;
    private final ModelStatus status;
    private final Set<TaskType> supportedTasks;
    private final Set<String> optimizedFor;
    private final double costPerToken;
    private final Map<String, Object> capabilities;

    public ModelInfo(String modelId, String name, String provider, ModelStatus status, Set<TaskType> supportedTasks,
            Set<String> optimizedFor, double costPerToken, Map<String, Object> capabilities) {
        this.modelId = modelId;
        this.name = name;
        this.provider = provider;
        this.status = status;
        this.supportedTasks = new java.util.HashSet<>(supportedTasks);
        this.optimizedFor = new java.util.HashSet<>(optimizedFor);
        this.costPerToken = costPerToken;
        this.capabilities = new ConcurrentHashMap<>(capabilities);
    }

    public String getModelId() { return modelId; }
    public String getName() { return name; }
    public String getProvider() { return provider; }
    public ModelStatus getStatus() { return status; }
    public Set<TaskType> getSupportedTasks() { return new java.util.HashSet<>(supportedTasks); }
    public Set<String> getOptimizedFor() { return new java.util.HashSet<>(optimizedFor); }
    public double getCostPerToken() { return costPerToken; }
    public Map<String, Object> getCapabilities() { return new ConcurrentHashMap<>(capabilities); }
}


