package org.openhab.core.ai.reasoning.context;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of optimizing an agent model context.
 *
 * <p>
 * Contains recommendations and optimizations suggested by the validator.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextOptimizationResult {
    private final List<String> recommendations = new ArrayList<>();
    private final List<String> optimizations = new ArrayList<>();

    public void addRecommendation(String recommendation) {
        recommendations.add(recommendation);
    }

    public void addOptimization(String optimization) {
        optimizations.add(optimization);
    }

    public List<String> getRecommendations() {
        return new ArrayList<>(recommendations);
    }

    public List<String> getOptimizations() {
        return new ArrayList<>(optimizations);
    }

    public boolean hasRecommendations() {
        return !recommendations.isEmpty();
    }

    public boolean hasOptimizations() {
        return !optimizations.isEmpty();
    }

    public int getRecommendationCount() {
        return recommendations.size();
    }

    public int getOptimizationCount() {
        return optimizations.size();
    }
}
