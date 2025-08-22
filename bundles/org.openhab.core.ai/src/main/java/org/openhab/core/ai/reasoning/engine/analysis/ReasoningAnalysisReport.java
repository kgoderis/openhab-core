package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Comprehensive analysis report for reasoning steps.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningAnalysisReport {

    private final String reportId;
    private final String title;
    private final String summary;
    private final List<String> keyFindings;
    private final Map<String, Object> metrics;
    private final List<String> recommendations;
    private final Instant generatedAt;
    private final String generatedBy;

    private ReasoningAnalysisReport(Builder builder) {
        this.reportId = builder.reportId;
        this.title = builder.title;
        this.summary = builder.summary;
        this.keyFindings = List.copyOf(builder.keyFindings);
        this.metrics = Map.copyOf(builder.metrics);
        this.recommendations = List.copyOf(builder.recommendations);
        this.generatedAt = builder.generatedAt;
        this.generatedBy = builder.generatedBy;
    }

    public static Builder builder(String reportId, String title) {
        return new Builder(reportId, title);
    }

    public String getReportId() {
        return reportId;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getKeyFindings() {
        return keyFindings;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public static final class Builder {
        private String reportId;
        private String title;
        private String summary = "";
        private List<String> keyFindings = List.of();
        private Map<String, Object> metrics = Map.of();
        private List<String> recommendations = List.of();
        private Instant generatedAt = Instant.now();
        private String generatedBy = "ReasoningStepAnalysisService";

        public Builder(String reportId, String title) {
            this.reportId = Objects.requireNonNull(reportId, "reportId");
            this.title = Objects.requireNonNull(title, "title");
        }

        public Builder withSummary(String summary) {
            this.summary = summary;
            return this;
        }

        public Builder withKeyFindings(List<String> keyFindings) {
            this.keyFindings = keyFindings;
            return this;
        }

        public Builder withMetrics(Map<String, Object> metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder withRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public Builder withGeneratedAt(Instant generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public Builder withGeneratedBy(String generatedBy) {
            this.generatedBy = generatedBy;
            return this;
        }

        public ReasoningAnalysisReport build() {
            if (reportId.isBlank()) {
                throw new IllegalArgumentException("reportId must not be blank");
            }
            if (title.isBlank()) {
                throw new IllegalArgumentException("title must not be blank");
            }
            return new ReasoningAnalysisReport(this);
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
        ReasoningAnalysisReport other = (ReasoningAnalysisReport) obj;
        return Objects.equals(reportId, other.reportId) && Objects.equals(title, other.title)
                && Objects.equals(summary, other.summary) && Objects.equals(keyFindings, other.keyFindings)
                && Objects.equals(metrics, other.metrics) && Objects.equals(recommendations, other.recommendations)
                && Objects.equals(generatedAt, other.generatedAt) && Objects.equals(generatedBy, other.generatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, title, summary, keyFindings, metrics, recommendations, generatedAt, generatedBy);
    }

    @Override
    public String toString() {
        return String.format("ReasoningAnalysisReport{id='%s', title='%s', findings=%d, recommendations=%d}", reportId,
                title, keyFindings.size(), recommendations.size());
    }
}
