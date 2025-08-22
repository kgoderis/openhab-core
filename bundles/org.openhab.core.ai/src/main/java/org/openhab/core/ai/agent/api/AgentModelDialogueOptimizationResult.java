package org.openhab.core.ai.agent.api;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Dialogue optimization result for autonomous agents
 * 
 * <p>
 * This class provides:
 * - Dialogue optimization result representation
 * - Optimization metrics and improvements
 * - Optimization recommendations and actions
 * - Optimization validation and quality metrics
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelDialogueOptimizationResult {

    // Optimization identification
    private final String sessionId;
    private final String agentId;
    private final Instant optimizationTimestamp;
    private final Duration optimizationDuration;
    private final String optimizationStrategy;

    // Optimization status
    private final boolean success;
    private final OptimizationStatus status;
    private final double improvementScore;
    private final double confidenceScore;

    // Optimization metrics
    private final double responseTimeImprovement;
    private final double qualityImprovement;
    private final double engagementImprovement;
    private final double efficiencyImprovement;
    private final Map<String, Double> metricImprovements;

    // Optimization recommendations
    private final List<String> recommendations;
    private final List<String> actions;
    private final List<String> warnings;
    private final Map<String, Object> optimizationData;

    // Optimization validation
    private final boolean isValid;
    private final @Nullable String validationMessage;
    private final boolean isSafe;
    private final @Nullable List<String> safetyIssues;

    // Optimization impact
    private final @Nullable String impactAssessment;
    private final @Nullable Duration estimatedImplementationTime;
    private final @Nullable Double estimatedCost;
    private final @Nullable Double estimatedBenefit;

    // Metadata
    private final Map<String, Object> metadata;
    private final @Nullable String notes;

    private AgentModelDialogueOptimizationResult(Builder builder) {
        this.sessionId = builder.sessionId;
        this.agentId = builder.agentId;
        this.optimizationTimestamp = builder.optimizationTimestamp;
        this.optimizationDuration = builder.optimizationDuration;
        this.optimizationStrategy = builder.optimizationStrategy;
        this.success = builder.success;
        this.status = builder.status;
        this.improvementScore = builder.improvementScore;
        this.confidenceScore = builder.confidenceScore;
        this.responseTimeImprovement = builder.responseTimeImprovement;
        this.qualityImprovement = builder.qualityImprovement;
        this.engagementImprovement = builder.engagementImprovement;
        this.efficiencyImprovement = builder.efficiencyImprovement;
        this.metricImprovements = Map.copyOf(builder.metricImprovements);
        this.recommendations = List.copyOf(builder.recommendations);
        this.actions = List.copyOf(builder.actions);
        this.warnings = List.copyOf(builder.warnings);
        this.optimizationData = Map.copyOf(builder.optimizationData);
        this.isValid = builder.isValid;
        this.validationMessage = builder.validationMessage;
        this.isSafe = builder.isSafe;
        this.safetyIssues = builder.safetyIssues != null ? List.copyOf(builder.safetyIssues) : null;
        this.impactAssessment = builder.impactAssessment;
        this.estimatedImplementationTime = builder.estimatedImplementationTime;
        this.estimatedCost = builder.estimatedCost;
        this.estimatedBenefit = builder.estimatedBenefit;
        this.metadata = Map.copyOf(builder.metadata);
        this.notes = builder.notes;
    }

    /**
     * Create a new dialogue optimization result builder
     * 
     * @param sessionId the session ID
     * @param agentId the agent ID
     * @param optimizationStrategy the optimization strategy
     * @return the builder
     */
    public static Builder builder(String sessionId, String agentId, String optimizationStrategy) {
        return new Builder(sessionId, agentId, optimizationStrategy);
    }

    // Getters
    public String getSessionId() {
        return sessionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public Instant getOptimizationTimestamp() {
        return optimizationTimestamp;
    }

    public Duration getOptimizationDuration() {
        return optimizationDuration;
    }

    public String getOptimizationStrategy() {
        return optimizationStrategy;
    }

    public boolean isSuccess() {
        return success;
    }

    public OptimizationStatus getStatus() {
        return status;
    }

    public double getImprovementScore() {
        return improvementScore;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public double getResponseTimeImprovement() {
        return responseTimeImprovement;
    }

    public double getQualityImprovement() {
        return qualityImprovement;
    }

    public double getEngagementImprovement() {
        return engagementImprovement;
    }

    public double getEfficiencyImprovement() {
        return efficiencyImprovement;
    }

    public Map<String, Double> getMetricImprovements() {
        return metricImprovements;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public List<String> getActions() {
        return actions;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public Map<String, Object> getOptimizationData() {
        return optimizationData;
    }

    public boolean isValid() {
        return isValid;
    }

    public @Nullable String getValidationMessage() {
        return validationMessage;
    }

    public boolean isSafe() {
        return isSafe;
    }

    public @Nullable List<String> getSafetyIssues() {
        return safetyIssues;
    }

    public @Nullable String getImpactAssessment() {
        return impactAssessment;
    }

    public @Nullable Duration getEstimatedImplementationTime() {
        return estimatedImplementationTime;
    }

    public @Nullable Double getEstimatedCost() {
        return estimatedCost;
    }

    public @Nullable Double getEstimatedBenefit() {
        return estimatedBenefit;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public @Nullable String getNotes() {
        return notes;
    }

    /**
     * Check if the optimization was significant
     * 
     * @return true if significant
     */
    public boolean isSignificant() {
        return improvementScore >= 0.1 && confidenceScore >= 0.7;
    }

    /**
     * Check if the optimization is worth implementing
     * 
     * @return true if worth implementing
     */
    public boolean isWorthImplementing() {
        return success && isValid && isSafe && improvementScore >= 0.05
                && (estimatedBenefit == null || estimatedCost == null || estimatedBenefit > estimatedCost);
    }

    /**
     * Check if the optimization has high impact
     * 
     * @return true if high impact
     */
    public boolean isHighImpact() {
        return improvementScore >= 0.2 && qualityImprovement >= 0.1 && efficiencyImprovement >= 0.1;
    }

    /**
     * Get the overall optimization score
     * 
     * @return the overall score
     */
    public double getOverallScore() {
        return (improvementScore + confidenceScore) / 2.0;
    }

    /**
     * Get the total improvement across all metrics
     * 
     * @return the total improvement
     */
    public double getTotalImprovement() {
        return responseTimeImprovement + qualityImprovement + engagementImprovement + efficiencyImprovement;
    }

    /**
     * Check if the optimization has safety issues
     * 
     * @return true if has safety issues
     */
    public boolean hasSafetyIssues() {
        return !isSafe || (safetyIssues != null && !safetyIssues.isEmpty());
    }

    /**
     * Get the optimization summary report
     * 
     * @return the summary report
     */
    public String getOptimizationReport() {
        StringBuilder report = new StringBuilder();
        report.append("Dialogue Optimization Report\n");
        report.append("Session ID: ").append(sessionId).append("\n");
        report.append("Agent ID: ").append(agentId).append("\n");
        report.append("Strategy: ").append(optimizationStrategy).append("\n");
        report.append("Status: ").append(status).append("\n");
        report.append("Success: ").append(success).append("\n");
        report.append("Timestamp: ").append(optimizationTimestamp).append("\n");
        report.append("Duration: ").append(optimizationDuration).append("\n\n");

        report.append("Optimization Metrics:\n");
        report.append("  Improvement Score: ").append(String.format("%.2f", improvementScore)).append("\n");
        report.append("  Confidence Score: ").append(String.format("%.2f", confidenceScore)).append("\n");
        report.append("  Overall Score: ").append(String.format("%.2f", getOverallScore())).append("\n");
        report.append("  Total Improvement: ").append(String.format("%.2f", getTotalImprovement())).append("\n\n");

        report.append("Metric Improvements:\n");
        report.append("  Response Time: ").append(String.format("%.2f", responseTimeImprovement)).append("\n");
        report.append("  Quality: ").append(String.format("%.2f", qualityImprovement)).append("\n");
        report.append("  Engagement: ").append(String.format("%.2f", engagementImprovement)).append("\n");
        report.append("  Efficiency: ").append(String.format("%.2f", efficiencyImprovement)).append("\n\n");

        if (!recommendations.isEmpty()) {
            report.append("Recommendations:\n");
            for (String recommendation : recommendations) {
                report.append("  - ").append(recommendation).append("\n");
            }
            report.append("\n");
        }

        if (!actions.isEmpty()) {
            report.append("Actions:\n");
            for (String action : actions) {
                report.append("  - ").append(action).append("\n");
            }
            report.append("\n");
        }

        if (!warnings.isEmpty()) {
            report.append("Warnings:\n");
            for (String warning : warnings) {
                report.append("  - ").append(warning).append("\n");
            }
            report.append("\n");
        }

        if (impactAssessment != null) {
            report.append("Impact Assessment:\n").append(impactAssessment).append("\n\n");
        }

        if (estimatedImplementationTime != null) {
            report.append("Estimated Implementation Time: ").append(estimatedImplementationTime).append("\n");
        }
        if (estimatedCost != null) {
            report.append("Estimated Cost: ").append(String.format("%.2f", estimatedCost)).append("\n");
        }
        if (estimatedBenefit != null) {
            report.append("Estimated Benefit: ").append(String.format("%.2f", estimatedBenefit)).append("\n");
        }

        return report.toString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AgentModelDialogueOptimizationResult that = (AgentModelDialogueOptimizationResult) obj;
        return Objects.equals(sessionId, that.sessionId)
                && Objects.equals(optimizationTimestamp, that.optimizationTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, optimizationTimestamp);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelDialogueOptimizationResult{sessionId='%s', agentId='%s', strategy='%s', success=%s, improvement=%.2f}",
                sessionId, agentId, optimizationStrategy, success, improvementScore);
    }

    /**
     * Optimization status enum
     */
    public enum OptimizationStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED,
        VALIDATED,
        IMPLEMENTED
    }

    /**
     * Builder for AgentModelDialogueOptimizationResult
     */
    public static final class Builder {
        private String sessionId;
        private String agentId;
        private Instant optimizationTimestamp;
        private Duration optimizationDuration;
        private String optimizationStrategy;
        private boolean success;
        private OptimizationStatus status;
        private double improvementScore;
        private double confidenceScore;
        private double responseTimeImprovement;
        private double qualityImprovement;
        private double engagementImprovement;
        private double efficiencyImprovement;
        private Map<String, Double> metricImprovements;
        private List<String> recommendations;
        private List<String> actions;
        private List<String> warnings;
        private Map<String, Object> optimizationData;
        private boolean isValid;
        private @Nullable String validationMessage;
        private boolean isSafe;
        private @Nullable List<String> safetyIssues;
        private @Nullable String impactAssessment;
        private @Nullable Duration estimatedImplementationTime;
        private @Nullable Double estimatedCost;
        private @Nullable Double estimatedBenefit;
        private Map<String, Object> metadata;
        private @Nullable String notes;

        public Builder(String sessionId, String agentId, String optimizationStrategy) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            this.optimizationStrategy = Objects.requireNonNull(optimizationStrategy, "optimizationStrategy");
            this.optimizationTimestamp = Instant.now();
            this.optimizationDuration = Duration.ZERO;
            this.success = false;
            this.status = OptimizationStatus.PENDING;
            this.improvementScore = 0.0;
            this.confidenceScore = 0.0;
            this.responseTimeImprovement = 0.0;
            this.qualityImprovement = 0.0;
            this.engagementImprovement = 0.0;
            this.efficiencyImprovement = 0.0;
            this.metricImprovements = new HashMap<>();
            this.recommendations = new ArrayList<>();
            this.actions = new ArrayList<>();
            this.warnings = new ArrayList<>();
            this.optimizationData = new HashMap<>();
            this.isValid = true;
            this.isSafe = true;
            this.metadata = new HashMap<>();
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            return this;
        }

        public Builder agentId(String agentId) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            return this;
        }

        public Builder optimizationTimestamp(Instant optimizationTimestamp) {
            this.optimizationTimestamp = Objects.requireNonNull(optimizationTimestamp, "optimizationTimestamp");
            return this;
        }

        public Builder optimizationDuration(Duration optimizationDuration) {
            this.optimizationDuration = Objects.requireNonNull(optimizationDuration, "optimizationDuration");
            return this;
        }

        public Builder optimizationStrategy(String optimizationStrategy) {
            this.optimizationStrategy = Objects.requireNonNull(optimizationStrategy, "optimizationStrategy");
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder status(OptimizationStatus status) {
            this.status = Objects.requireNonNull(status, "status");
            return this;
        }

        public Builder improvementScore(double improvementScore) {
            this.improvementScore = improvementScore;
            return this;
        }

        public Builder confidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
            return this;
        }

        public Builder responseTimeImprovement(double responseTimeImprovement) {
            this.responseTimeImprovement = responseTimeImprovement;
            return this;
        }

        public Builder qualityImprovement(double qualityImprovement) {
            this.qualityImprovement = qualityImprovement;
            return this;
        }

        public Builder engagementImprovement(double engagementImprovement) {
            this.engagementImprovement = engagementImprovement;
            return this;
        }

        public Builder efficiencyImprovement(double efficiencyImprovement) {
            this.efficiencyImprovement = efficiencyImprovement;
            return this;
        }

        public Builder metricImprovements(Map<String, Double> metricImprovements) {
            this.metricImprovements = new HashMap<>(Objects.requireNonNull(metricImprovements, "metricImprovements"));
            return this;
        }

        public Builder addMetricImprovement(String metric, double improvement) {
            if (this.metricImprovements == null) {
                this.metricImprovements = new HashMap<>();
            }
            this.metricImprovements.put(Objects.requireNonNull(metric, "metric"), improvement);
            return this;
        }

        public Builder recommendations(List<String> recommendations) {
            this.recommendations = new ArrayList<>(Objects.requireNonNull(recommendations, "recommendations"));
            return this;
        }

        public Builder addRecommendation(String recommendation) {
            if (this.recommendations == null) {
                this.recommendations = new ArrayList<>();
            }
            this.recommendations.add(Objects.requireNonNull(recommendation, "recommendation"));
            return this;
        }

        public Builder actions(List<String> actions) {
            this.actions = new ArrayList<>(Objects.requireNonNull(actions, "actions"));
            return this;
        }

        public Builder addAction(String action) {
            if (this.actions == null) {
                this.actions = new ArrayList<>();
            }
            this.actions.add(Objects.requireNonNull(action, "action"));
            return this;
        }

        public Builder warnings(List<String> warnings) {
            this.warnings = new ArrayList<>(Objects.requireNonNull(warnings, "warnings"));
            return this;
        }

        public Builder addWarning(String warning) {
            if (this.warnings == null) {
                this.warnings = new ArrayList<>();
            }
            this.warnings.add(Objects.requireNonNull(warning, "warning"));
            return this;
        }

        public Builder optimizationData(Map<String, Object> optimizationData) {
            this.optimizationData = new HashMap<>(Objects.requireNonNull(optimizationData, "optimizationData"));
            return this;
        }

        public Builder addOptimizationData(String key, Object value) {
            if (this.optimizationData == null) {
                this.optimizationData = new HashMap<>();
            }
            this.optimizationData.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder isValid(boolean isValid) {
            this.isValid = isValid;
            return this;
        }

        public Builder validationMessage(@Nullable String validationMessage) {
            this.validationMessage = validationMessage;
            return this;
        }

        public Builder isSafe(boolean isSafe) {
            this.isSafe = isSafe;
            return this;
        }

        public Builder safetyIssues(@Nullable List<String> safetyIssues) {
            this.safetyIssues = safetyIssues;
            return this;
        }

        public Builder addSafetyIssue(String issue) {
            if (this.safetyIssues == null) {
                this.safetyIssues = new ArrayList<>();
            }
            this.safetyIssues.add(Objects.requireNonNull(issue, "issue"));
            return this;
        }

        public Builder impactAssessment(@Nullable String impactAssessment) {
            this.impactAssessment = impactAssessment;
            return this;
        }

        public Builder estimatedImplementationTime(@Nullable Duration estimatedImplementationTime) {
            this.estimatedImplementationTime = estimatedImplementationTime;
            return this;
        }

        public Builder estimatedCost(@Nullable Double estimatedCost) {
            this.estimatedCost = estimatedCost;
            return this;
        }

        public Builder estimatedBenefit(@Nullable Double estimatedBenefit) {
            this.estimatedBenefit = estimatedBenefit;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = new HashMap<>(Objects.requireNonNull(metadata, "metadata"));
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            if (this.metadata == null) {
                this.metadata = new HashMap<>();
            }
            this.metadata.put(Objects.requireNonNull(key, "key"), value);
            return this;
        }

        public Builder notes(@Nullable String notes) {
            this.notes = notes;
            return this;
        }

        public AgentModelDialogueOptimizationResult build() {
            return new AgentModelDialogueOptimizationResult(this);
        }
    }
}
