package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Analysis of dependencies between reasoning steps.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningDependencyAnalysis {

    private final Map<String, List<String>> dependencies;
    private final Map<String, Integer> dependencyCounts;
    private final List<String> circularDependencies;
    private final List<String> missingDependencies;
    private final double averageDependenciesPerStep;
    private final Instant analysisTime;

    private ReasoningDependencyAnalysis(Builder builder) {
        this.dependencies = Map.copyOf(builder.dependencies);
        this.dependencyCounts = Map.copyOf(builder.dependencyCounts);
        this.circularDependencies = List.copyOf(builder.circularDependencies);
        this.missingDependencies = List.copyOf(builder.missingDependencies);
        this.averageDependenciesPerStep = builder.averageDependenciesPerStep;
        this.analysisTime = builder.analysisTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, List<String>> getDependencies() {
        return dependencies;
    }

    public Map<String, Integer> getDependencyCounts() {
        return dependencyCounts;
    }

    public List<String> getCircularDependencies() {
        return circularDependencies;
    }

    public List<String> getMissingDependencies() {
        return missingDependencies;
    }

    public double getAverageDependenciesPerStep() {
        return averageDependenciesPerStep;
    }

    public Instant getAnalysisTime() {
        return analysisTime;
    }

    public static final class Builder {
        private Map<String, List<String>> dependencies = Map.of();
        private Map<String, Integer> dependencyCounts = Map.of();
        private List<String> circularDependencies = List.of();
        private List<String> missingDependencies = List.of();
        private double averageDependenciesPerStep = 0.0;
        private Instant analysisTime = Instant.now();

        public Builder withDependencies(Map<String, List<String>> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Builder withDependencyCounts(Map<String, Integer> dependencyCounts) {
            this.dependencyCounts = dependencyCounts;
            return this;
        }

        public Builder withCircularDependencies(List<String> circularDependencies) {
            this.circularDependencies = circularDependencies;
            return this;
        }

        public Builder withMissingDependencies(List<String> missingDependencies) {
            this.missingDependencies = missingDependencies;
            return this;
        }

        public Builder withAverageDependenciesPerStep(double averageDependenciesPerStep) {
            this.averageDependenciesPerStep = averageDependenciesPerStep;
            return this;
        }

        public Builder withAnalysisTime(Instant analysisTime) {
            this.analysisTime = analysisTime;
            return this;
        }

        public ReasoningDependencyAnalysis build() {
            if (averageDependenciesPerStep < 0.0) {
                throw new IllegalArgumentException("averageDependenciesPerStep must be non-negative");
            }
            return new ReasoningDependencyAnalysis(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ReasoningDependencyAnalysis other = (ReasoningDependencyAnalysis) obj;
        return Objects.equals(dependencies, other.dependencies)
                && Objects.equals(dependencyCounts, other.dependencyCounts)
                && Objects.equals(circularDependencies, other.circularDependencies)
                && Objects.equals(missingDependencies, other.missingDependencies)
                && Double.compare(averageDependenciesPerStep, other.averageDependenciesPerStep) == 0
                && Objects.equals(analysisTime, other.analysisTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencies, dependencyCounts, circularDependencies, missingDependencies,
                averageDependenciesPerStep, analysisTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningDependencyAnalysis{avg=%.2f, circular=%d, missing=%d}",
                averageDependenciesPerStep, circularDependencies.size(), missingDependencies.size());
    }
}
