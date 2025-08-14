package org.openhab.core.ai.reasoning;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AdaptiveStrategyEntry {
    private final String strategyType;
    private final Map<String, Object> parameters = new ConcurrentHashMap<>();
    private double effectiveness = 0.5;

    public AdaptiveStrategyEntry(String strategyType) { this.strategyType = strategyType; }

    public boolean adapt(Map<String, Object> newParameters, double learningRate) {
        boolean adapted = false;
        for (Map.Entry<String, Object> entry : newParameters.entrySet()) {
            Object currentValue = parameters.get(entry.getKey());
            Object newValue = entry.getValue();
            if (newValue != null && !newValue.equals(currentValue)) {
                parameters.put(entry.getKey(), newValue);
                adapted = true;
            }
        }
        return adapted;
    }

    public String getStrategyType() { return strategyType; }
    public Map<String, Object> getParameters() { return Collections.unmodifiableMap(parameters); }
    public double getEffectiveness() { return effectiveness; }
}
