package org.openhab.core.ai.agent.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.validation.BaseValidationResult;

/**
 * Validation result for dialogue interactions
 * 
 * <p>
 * This class provides:
 * - Dialogue validation status and results extending BaseValidationResult
 * - Dialogue-specific validation errors and warnings
 * - Context comprehension and appropriateness checks
 * - Content safety and moderation validation
 * - Dialogue quality and effectiveness assessment
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelDialogueValidationResult extends BaseValidationResult {

    private final String sessionId;
    private final int messageCount;
    private final boolean contextAppropriate;
    private final boolean contentSafe;
    private final double comprehensionScore;
    private final List<String> qualityIssues;

    /**
     * Private constructor for internal use.
     */
    private AgentModelDialogueValidationResult(boolean valid, List<String> errors, List<String> warnings,
            Map<String, Object> details, @Nullable Instant validationTime, String sessionId, int messageCount,
            boolean contextAppropriate, boolean contentSafe, double comprehensionScore, List<String> qualityIssues) {
        super(valid, errors, warnings, details, validationTime);
        this.sessionId = sessionId;
        this.messageCount = messageCount;
        this.contextAppropriate = contextAppropriate;
        this.contentSafe = contentSafe;
        this.comprehensionScore = comprehensionScore;
        this.qualityIssues = List.copyOf(qualityIssues);
    }

    /**
     * Get the dialogue session identifier.
     * 
     * @return the session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Get the number of messages in the dialogue.
     * 
     * @return the message count
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * Check if the dialogue content is contextually appropriate.
     * 
     * @return true if contextually appropriate
     */
    public boolean isContextAppropriate() {
        return contextAppropriate;
    }

    /**
     * Check if the dialogue content is safe.
     * 
     * @return true if content is safe
     */
    public boolean isContentSafe() {
        return contentSafe;
    }

    /**
     * Get the comprehension score (0.0-1.0).
     * 
     * @return the comprehension score
     */
    public double getComprehensionScore() {
        return comprehensionScore;
    }

    /**
     * Get quality issues identified during validation.
     * 
     * @return list of quality issues
     */
    public List<String> getQualityIssues() {
        return qualityIssues;
    }

    /**
     * Check if the dialogue has quality concerns.
     * 
     * @return true if there are quality issues
     */
    public boolean hasQualityIssues() {
        return !qualityIssues.isEmpty();
    }

    /**
     * Get the overall dialogue quality score (0.0-1.0).
     * 
     * @return quality score combining various factors
     */
    public double getQualityScore() {
        double contextScore = contextAppropriate ? 1.0 : 0.0;
        double safetyScore = contentSafe ? 1.0 : 0.0;
        double qualityPenalty = Math.min(1.0, qualityIssues.size() * 0.1);
        return Math.max(0.0, (contextScore + safetyScore + comprehensionScore - qualityPenalty) / 3.0);
    }

    /**
     * Builder for AgentModelDialogueValidationResult.
     */
    public static final class Builder {
        private boolean valid = true;
        private List<String> errors = List.of();
        private List<String> warnings = List.of();
        private Map<String, Object> details = Map.of();
        private @Nullable Instant validationTime = null;
        private String sessionId = "";
        private int messageCount = 0;
        private boolean contextAppropriate = true;
        private boolean contentSafe = true;
        private double comprehensionScore = 1.0;
        private List<String> qualityIssues = List.of();

        public Builder() {
            // Default constructor
        }

        public Builder(AgentModelDialogueValidationResult source) {
            this.valid = source.isValid();
            this.errors = source.getErrors();
            this.warnings = source.getWarnings();
            this.details = source.getDetails();
            this.validationTime = source.getValidationTime();
            this.sessionId = source.sessionId;
            this.messageCount = source.messageCount;
            this.contextAppropriate = source.contextAppropriate;
            this.contentSafe = source.contentSafe;
            this.comprehensionScore = source.comprehensionScore;
            this.qualityIssues = source.qualityIssues;
        }

        public Builder withValid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder withErrors(List<String> errors) {
            this.errors = List.copyOf(Objects.requireNonNull(errors, "errors"));
            return this;
        }

        public Builder withWarnings(List<String> warnings) {
            this.warnings = List.copyOf(Objects.requireNonNull(warnings, "warnings"));
            return this;
        }

        public Builder withDetails(Map<String, Object> details) {
            this.details = Map.copyOf(Objects.requireNonNull(details, "details"));
            return this;
        }

        public Builder withValidationTime(@Nullable Instant validationTime) {
            this.validationTime = validationTime;
            return this;
        }

        public Builder withSessionId(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            return this;
        }

        public Builder withMessageCount(int messageCount) {
            this.messageCount = messageCount;
            return this;
        }

        public Builder withContextAppropriate(boolean contextAppropriate) {
            this.contextAppropriate = contextAppropriate;
            return this;
        }

        public Builder withContentSafe(boolean contentSafe) {
            this.contentSafe = contentSafe;
            return this;
        }

        public Builder withComprehensionScore(double comprehensionScore) {
            this.comprehensionScore = comprehensionScore;
            return this;
        }

        public Builder withQualityIssues(List<String> qualityIssues) {
            this.qualityIssues = List.copyOf(Objects.requireNonNull(qualityIssues, "qualityIssues"));
            return this;
        }

        public AgentModelDialogueValidationResult build() {
            validate();
            return new AgentModelDialogueValidationResult(valid, errors, warnings, details, validationTime, sessionId,
                    messageCount, contextAppropriate, contentSafe, comprehensionScore, qualityIssues);
        }

        private void validate() {
            if (sessionId.trim().isEmpty()) {
                throw new IllegalArgumentException("sessionId must not be empty");
            }
            if (messageCount < 0) {
                throw new IllegalArgumentException("messageCount must be non-negative");
            }
            if (comprehensionScore < 0.0 || comprehensionScore > 1.0) {
                throw new IllegalArgumentException("comprehensionScore must be between 0.0 and 1.0");
            }
        }
    }

    /**
     * Create a successful validation result.
     * 
     * @param sessionId the session identifier
     * @param messageCount the number of messages
     * @return a successful validation result
     */
    public static AgentModelDialogueValidationResult success(String sessionId, int messageCount) {
        return builder().withValid(true).withSessionId(sessionId).withMessageCount(messageCount)
                .withValidationTime(Instant.now()).build();
    }

    /**
     * Create a failed validation result.
     * 
     * @param sessionId the session identifier
     * @param messageCount the number of messages
     * @param errors the validation errors
     * @return a failed validation result
     */
    public static AgentModelDialogueValidationResult failure(String sessionId, int messageCount, List<String> errors) {
        return builder().withValid(false).withSessionId(sessionId).withMessageCount(messageCount).withErrors(errors)
                .withValidationTime(Instant.now()).build();
    }

    /**
     * Create a new builder for AgentModelDialogueValidationResult.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for AgentModelDialogueValidationResult from an existing instance.
     *
     * @return a new builder instance initialized with this instance's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelDialogueValidationResult other = (AgentModelDialogueValidationResult) obj;
        return Objects.equals(sessionId, other.sessionId) && messageCount == other.messageCount
                && contextAppropriate == other.contextAppropriate && contentSafe == other.contentSafe
                && Double.compare(comprehensionScore, other.comprehensionScore) == 0
                && Objects.equals(qualityIssues, other.qualityIssues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sessionId, messageCount, contextAppropriate, contentSafe,
                comprehensionScore, qualityIssues);
    }

    @Override
    public String toString() {
        return String.format(
                "AgentModelDialogueValidationResult{sessionId='%s', messageCount=%d, valid=%s, "
                        + "contextAppropriate=%s, contentSafe=%s, comprehensionScore=%.3f, qualityIssues=%d}",
                sessionId, messageCount, isValid(), contextAppropriate, contentSafe, comprehensionScore,
                qualityIssues.size());
    }
}
