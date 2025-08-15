package org.openhab.core.ai.reasoning.decision;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Analysis result holder for decision optimization.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
final class DecisionAnalysis {
    private double averageComplexity;
    private List<Integer> redundantSteps;
    private Map<Integer, List<Integer>> dependencyGraph;
    private List<OptimizationOpportunity> optimizationOpportunities;

    DecisionAnalysis() {
        this.redundantSteps = new java.util.ArrayList<>();
        this.dependencyGraph = new ConcurrentHashMap<>();
        this.optimizationOpportunities = new java.util.ArrayList<>();
    }

    double getAverageComplexity() {
        return averageComplexity;
    }

    void setAverageComplexity(double averageComplexity) {
        this.averageComplexity = averageComplexity;
    }

    List<Integer> getRedundantSteps() {
        return redundantSteps;
    }

    void setRedundantSteps(List<Integer> redundantSteps) {
        this.redundantSteps = redundantSteps;
    }

    Map<Integer, List<Integer>> getDependencyGraph() {
        return dependencyGraph;
    }

    void setDependencyGraph(Map<Integer, List<Integer>> dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    List<OptimizationOpportunity> getOptimizationOpportunities() {
        return optimizationOpportunities;
    }

    void setOptimizationOpportunities(List<OptimizationOpportunity> optimizationOpportunities) {
        this.optimizationOpportunities = optimizationOpportunities;
    }
}
