package org.openhab.core.ai.reasoning.strategies.adaptation;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AdaptiveStrategy {
    private final String agentId;
    private final Map<String, AdaptiveStrategyEntry> strategies = new ConcurrentHashMap<>();
    private int adaptationCount = 0;

    public AdaptiveStrategy(String agentId) {
        this.agentId = agentId;
    }

    public boolean adaptStrategy(String strategyType, Map<String, Object> strategyParameters, double learningRate) {
        AdaptiveStrategyEntry strategy = strategies.computeIfAbsent(strategyType,
                k -> new AdaptiveStrategyEntry(strategyType));
        boolean adapted = strategy.adapt(strategyParameters, learningRate);
        if (adapted) {
            adaptationCount++;
        }
        return adapted;
    }

    public int getAdaptationCount() {
        return adaptationCount;
    }

    public String getAgentId() {
        return agentId;
    }

    public Map<String, AdaptiveStrategyEntry> getStrategies() {
        return Collections.unmodifiableMap(strategies);
    }
}
