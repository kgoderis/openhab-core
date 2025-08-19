package org.openhab.core.ai.reasoning.decision;

import java.util.ArrayList;
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
public final class DecisionAnalysis {
    private double averageComplexity;
    private List<Integer> redundantSteps;
    private Map<Integer, List<Integer>> dependencyGraph;
    private List<OptimizationOpportunity> optimizationOpportunities;

    public DecisionAnalysis() {
        this.redundantSteps = new ArrayList<>();
        this.dependencyGraph = new ConcurrentHashMap<>();
        this.optimizationOpportunities = new ArrayList<>();
    }

    public double getAverageComplexity() {
        return averageComplexity;
    }

    public void setAverageComplexity(double averageComplexity) {
        this.averageComplexity = averageComplexity;
    }

    public List<Integer> getRedundantSteps() {
        return redundantSteps;
    }

    public void setRedundantSteps(List<Integer> redundantSteps) {
        this.redundantSteps = redundantSteps;
    }

    public Map<Integer, List<Integer>> getDependencyGraph() {
        return dependencyGraph;
    }

    public void setDependencyGraph(Map<Integer, List<Integer>> dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
    }

    public List<OptimizationOpportunity> getOptimizationOpportunities() {
        return optimizationOpportunities;
    }

    public void setOptimizationOpportunities(List<OptimizationOpportunity> optimizationOpportunities) {
        this.optimizationOpportunities = optimizationOpportunities;
    }
}
