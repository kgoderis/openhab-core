package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Analysis of reasoning sessions.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningSessionAnalysis {

    private final String sessionId;
    private final int totalSteps;
    private final double averageStepDuration;
    private final double successRate;
    private final Map<String, Integer> stepsByType;
    private final List<String> sessionInsights;
    private final Instant analysisTime;

    private ReasoningSessionAnalysis(Builder builder) {
        this.sessionId = builder.sessionId;
        this.totalSteps = builder.totalSteps;
        this.averageStepDuration = builder.averageStepDuration;
        this.successRate = builder.successRate;
        this.stepsByType = Map.copyOf(builder.stepsByType);
        this.sessionInsights = List.copyOf(builder.sessionInsights);
        this.analysisTime = builder.analysisTime;
    }

    public static Builder builder(String sessionId) {
        return new Builder(sessionId);
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public double getAverageStepDuration() {
        return averageStepDuration;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public Map<String, Integer> getStepsByType() {
        return stepsByType;
    }

    public List<String> getSessionInsights() {
        return sessionInsights;
    }

    public Instant getAnalysisTime() {
        return analysisTime;
    }

    public static final class Builder {
        private String sessionId;
        private int totalSteps = 0;
        private double averageStepDuration = 0.0;
        private double successRate = 0.0;
        private Map<String, Integer> stepsByType = Map.of();
        private List<String> sessionInsights = List.of();
        private Instant analysisTime = Instant.now();

        public Builder(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        }

        public Builder withTotalSteps(int totalSteps) {
            this.totalSteps = totalSteps;
            return this;
        }

        public Builder withAverageStepDuration(double averageStepDuration) {
            this.averageStepDuration = averageStepDuration;
            return this;
        }

        public Builder withSuccessRate(double successRate) {
            this.successRate = successRate;
            return this;
        }

        public Builder withStepsByType(Map<String, Integer> stepsByType) {
            this.stepsByType = stepsByType;
            return this;
        }

        public Builder withSessionInsights(List<String> sessionInsights) {
            this.sessionInsights = sessionInsights;
            return this;
        }

        public Builder withAnalysisTime(Instant analysisTime) {
            this.analysisTime = analysisTime;
            return this;
        }

        public ReasoningSessionAnalysis build() {
            if (sessionId.isBlank()) {
                throw new IllegalArgumentException("sessionId must not be blank");
            }
            if (totalSteps < 0) {
                throw new IllegalArgumentException("totalSteps must be non-negative");
            }
            if (averageStepDuration < 0.0) {
                throw new IllegalArgumentException("averageStepDuration must be non-negative");
            }
            if (successRate < 0.0 || successRate > 1.0) {
                throw new IllegalArgumentException("successRate must be between 0.0 and 1.0");
            }
            return new ReasoningSessionAnalysis(this);
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
        ReasoningSessionAnalysis other = (ReasoningSessionAnalysis) obj;
        return Objects.equals(sessionId, other.sessionId) && totalSteps == other.totalSteps
                && Double.compare(averageStepDuration, other.averageStepDuration) == 0
                && Double.compare(successRate, other.successRate) == 0 && Objects.equals(stepsByType, other.stepsByType)
                && Objects.equals(sessionInsights, other.sessionInsights)
                && Objects.equals(analysisTime, other.analysisTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, totalSteps, averageStepDuration, successRate, stepsByType, sessionInsights,
                analysisTime);
    }

    @Override
    public String toString() {
        return String.format("ReasoningSessionAnalysis{sessionId='%s', steps=%d, success=%.2f}", sessionId, totalSteps,
                successRate);
    }
}
