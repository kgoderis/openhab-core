package org.openhab.core.ai.reasoning.engine.analysis;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A recommendation for improving reasoning performance or quality.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ReasoningRecommendation {

    private final String id;
    private final String title;
    private final String description;
    private final ReasoningRecommendationPriority priority;
    private final ReasoningRecommendationCategory category;
    private final double confidence;
    private final List<String> steps;
    private final Map<String, Object> metadata;
    private final @Nullable String rationale;
    private final Instant createdAt;

    private ReasoningRecommendation(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.priority = builder.priority;
        this.category = builder.category;
        this.confidence = builder.confidence;
        this.steps = List.copyOf(builder.steps);
        this.metadata = Map.copyOf(builder.metadata);
        this.rationale = builder.rationale;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder(String id, String title) {
        return new Builder(id, title);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ReasoningRecommendationPriority getPriority() {
        return priority;
    }

    public ReasoningRecommendationCategory getCategory() {
        return category;
    }

    public double getConfidence() {
        return confidence;
    }

    public List<String> getSteps() {
        return steps;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public @Nullable String getRationale() {
        return rationale;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private String id;
        private String title;
        private String description = "";
        private ReasoningRecommendationPriority priority = ReasoningRecommendationPriority.MEDIUM;
        private ReasoningRecommendationCategory category = ReasoningRecommendationCategory.PROCESS_IMPROVEMENT;
        private double confidence = 0.5;
        private List<String> steps = List.of();
        private Map<String, Object> metadata = Map.of();
        private @Nullable String rationale;
        private Instant createdAt = Instant.now();

        public Builder(String id, String title) {
            this.id = Objects.requireNonNull(id, "id");
            this.title = Objects.requireNonNull(title, "title");
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withPriority(ReasoningRecommendationPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder withCategory(ReasoningRecommendationCategory category) {
            this.category = category;
            return this;
        }

        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder withSteps(List<String> steps) {
            this.steps = steps;
            return this;
        }

        public Builder withMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder withRationale(@Nullable String rationale) {
            this.rationale = rationale;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ReasoningRecommendation build() {
            if (id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank");
            }
            if (title.isBlank()) {
                throw new IllegalArgumentException("title must not be blank");
            }
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
            }
            return new ReasoningRecommendation(this);
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
        ReasoningRecommendation other = (ReasoningRecommendation) obj;
        return Objects.equals(id, other.id) && Objects.equals(title, other.title)
                && Objects.equals(description, other.description) && priority == other.priority
                && category == other.category && Double.compare(confidence, other.confidence) == 0
                && Objects.equals(steps, other.steps) && Objects.equals(metadata, other.metadata)
                && Objects.equals(rationale, other.rationale) && Objects.equals(createdAt, other.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, priority, category, confidence, steps, metadata, rationale,
                createdAt);
    }

    @Override
    public String toString() {
        return String.format("ReasoningRecommendation{id='%s', title='%s', priority=%s, confidence=%.2f}", id, title,
                priority, confidence);
    }
}
