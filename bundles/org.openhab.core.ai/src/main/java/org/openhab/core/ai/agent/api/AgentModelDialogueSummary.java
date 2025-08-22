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
 * Dialogue summary for autonomous agents
 * 
 * <p>
 * This class provides:
 * - Dialogue summary representation and statistics
 * - Summary metadata and context
 * - Summary analysis and insights
 * - Summary validation and quality metrics
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class AgentModelDialogueSummary {

    // Summary identification
    private final String sessionId;
    private final String agentId;
    private final String userId;
    private final Instant createdAt;
    private final Instant endedAt;
    private final Duration sessionDuration;

    // Summary statistics
    private final int totalMessages;
    private final int userMessages;
    private final int agentMessages;
    private final int systemMessages;
    private final double averageResponseTime;
    private final Duration totalResponseTime;

    // Summary content
    private final String summary;
    private final List<String> keyTopics;
    private final List<String> actions;
    private final Map<String, Object> insights;
    private final Map<String, Object> metadata;

    // Summary quality
    private final double qualityScore;
    private final double relevanceScore;
    private final double completenessScore;
    private final boolean isValid;
    private final @Nullable String validationMessage;

    // Summary analysis
    private final @Nullable String sentiment;
    private final @Nullable Double sentimentScore;
    private final @Nullable String intent;
    private final @Nullable Double confidence;
    private final @Nullable List<String> recommendations;

    private AgentModelDialogueSummary(Builder builder) {
        this.sessionId = builder.sessionId;
        this.agentId = builder.agentId;
        this.userId = builder.userId;
        this.createdAt = builder.createdAt;
        this.endedAt = builder.endedAt;
        this.sessionDuration = builder.sessionDuration;
        this.totalMessages = builder.totalMessages;
        this.userMessages = builder.userMessages;
        this.agentMessages = builder.agentMessages;
        this.systemMessages = builder.systemMessages;
        this.averageResponseTime = builder.averageResponseTime;
        this.totalResponseTime = builder.totalResponseTime;
        this.summary = builder.summary;
        this.keyTopics = List.copyOf(builder.keyTopics);
        this.actions = List.copyOf(builder.actions);
        this.insights = Map.copyOf(builder.insights);
        this.metadata = Map.copyOf(builder.metadata);
        this.qualityScore = builder.qualityScore;
        this.relevanceScore = builder.relevanceScore;
        this.completenessScore = builder.completenessScore;
        this.isValid = builder.isValid;
        this.validationMessage = builder.validationMessage;
        this.sentiment = builder.sentiment;
        this.sentimentScore = builder.sentimentScore;
        this.intent = builder.intent;
        this.confidence = builder.confidence;
        this.recommendations = builder.recommendations != null ? List.copyOf(builder.recommendations) : null;
    }

    /**
     * Create a new dialogue summary builder
     * 
     * @param sessionId the session ID
     * @param agentId the agent ID
     * @param userId the user ID
     * @param summary the summary content
     * @return the builder
     */
    public static Builder builder(String sessionId, String agentId, String userId, String summary) {
        return new Builder(sessionId, agentId, userId, summary);
    }

    // Getters
    public String getSessionId() {
        return sessionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public Duration getSessionDuration() {
        return sessionDuration;
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public int getUserMessages() {
        return userMessages;
    }

    public int getAgentMessages() {
        return agentMessages;
    }

    public int getSystemMessages() {
        return systemMessages;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public Duration getTotalResponseTime() {
        return totalResponseTime;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getKeyTopics() {
        return keyTopics;
    }

    public List<String> getActions() {
        return actions;
    }

    public Map<String, Object> getInsights() {
        return insights;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public double getQualityScore() {
        return qualityScore;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public double getCompletenessScore() {
        return completenessScore;
    }

    public boolean isValid() {
        return isValid;
    }

    public @Nullable String getValidationMessage() {
        return validationMessage;
    }

    public @Nullable String getSentiment() {
        return sentiment;
    }

    public @Nullable Double getSentimentScore() {
        return sentimentScore;
    }

    public @Nullable String getIntent() {
        return intent;
    }

    public @Nullable Double getConfidence() {
        return confidence;
    }

    public @Nullable List<String> getRecommendations() {
        return recommendations;
    }

    /**
     * Check if the dialogue was successful
     * 
     * @return true if successful
     */
    public boolean isSuccessful() {
        return qualityScore >= 0.7 && relevanceScore >= 0.7 && completenessScore >= 0.7;
    }

    /**
     * Check if the dialogue was productive
     * 
     * @return true if productive
     */
    public boolean isProductive() {
        return totalMessages > 2 && !actions.isEmpty() && qualityScore >= 0.6;
    }

    /**
     * Check if the dialogue was engaging
     * 
     * @return true if engaging
     */
    public boolean isEngaging() {
        return totalMessages > 5 && averageResponseTime < 30.0 && sentimentScore != null && sentimentScore > 0.0;
    }

    /**
     * Get the overall dialogue score
     * 
     * @return the overall score
     */
    public double getOverallScore() {
        return (qualityScore + relevanceScore + completenessScore) / 3.0;
    }

    /**
     * Get the dialogue efficiency
     * 
     * @return the efficiency score
     */
    public double getEfficiency() {
        if (totalMessages == 0) {
            return 0.0;
        }
        double timeEfficiency = 1.0 - Math.min(sessionDuration.toSeconds() / 300.0, 1.0);
        double messageEfficiency = Math.min(totalMessages / 10.0, 1.0);
        double responseEfficiency = 1.0 - Math.min(averageResponseTime / 60.0, 1.0);

        return (timeEfficiency + messageEfficiency + responseEfficiency) / 3.0;
    }

    /**
     * Get the dialogue summary report
     * 
     * @return the summary report
     */
    public String getSummaryReport() {
        StringBuilder report = new StringBuilder();
        report.append("Dialogue Summary Report\n");
        report.append("Session ID: ").append(sessionId).append("\n");
        report.append("Agent ID: ").append(agentId).append("\n");
        report.append("User ID: ").append(userId).append("\n");
        report.append("Duration: ").append(sessionDuration).append("\n");
        report.append("Total Messages: ").append(totalMessages).append("\n");
        report.append("User Messages: ").append(userMessages).append("\n");
        report.append("Agent Messages: ").append(agentMessages).append("\n");
        report.append("Average Response Time: ").append(String.format("%.2f", averageResponseTime)).append("s\n");
        report.append("Quality Score: ").append(String.format("%.2f", qualityScore)).append("\n");
        report.append("Relevance Score: ").append(String.format("%.2f", relevanceScore)).append("\n");
        report.append("Completeness Score: ").append(String.format("%.2f", completenessScore)).append("\n");
        report.append("Overall Score: ").append(String.format("%.2f", getOverallScore())).append("\n");
        report.append("Efficiency: ").append(String.format("%.2f", getEfficiency())).append("\n");

        if (!keyTopics.isEmpty()) {
            report.append("Key Topics: ").append(String.join(", ", keyTopics)).append("\n");
        }
        if (!actions.isEmpty()) {
            report.append("Actions: ").append(String.join(", ", actions)).append("\n");
        }
        if (sentiment != null) {
            report.append("Sentiment: ").append(sentiment).append("\n");
        }
        if (intent != null) {
            report.append("Intent: ").append(intent).append("\n");
        }

        report.append("\nSummary:\n").append(summary);

        return report.toString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        AgentModelDialogueSummary that = (AgentModelDialogueSummary) obj;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return String.format("AgentModelDialogueSummary{sessionId='%s', agentId='%s', messages=%d, quality=%.2f}",
                sessionId, agentId, totalMessages, qualityScore);
    }

    /**
     * Builder for AgentModelDialogueSummary
     */
    public static final class Builder {
        private String sessionId;
        private String agentId;
        private String userId;
        private Instant createdAt;
        private Instant endedAt;
        private Duration sessionDuration;
        private int totalMessages;
        private int userMessages;
        private int agentMessages;
        private int systemMessages;
        private double averageResponseTime;
        private Duration totalResponseTime;
        private String summary;
        private List<String> keyTopics;
        private List<String> actions;
        private Map<String, Object> insights;
        private Map<String, Object> metadata;
        private double qualityScore;
        private double relevanceScore;
        private double completenessScore;
        private boolean isValid;
        private @Nullable String validationMessage;
        private @Nullable String sentiment;
        private @Nullable Double sentimentScore;
        private @Nullable String intent;
        private @Nullable Double confidence;
        private @Nullable List<String> recommendations;

        public Builder(String sessionId, String agentId, String userId, String summary) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            this.userId = Objects.requireNonNull(userId, "userId");
            this.summary = Objects.requireNonNull(summary, "summary");
            this.createdAt = Instant.now();
            this.endedAt = Instant.now();
            this.sessionDuration = Duration.ZERO;
            this.totalMessages = 0;
            this.userMessages = 0;
            this.agentMessages = 0;
            this.systemMessages = 0;
            this.averageResponseTime = 0.0;
            this.totalResponseTime = Duration.ZERO;
            this.keyTopics = new ArrayList<>();
            this.actions = new ArrayList<>();
            this.insights = new HashMap<>();
            this.metadata = new HashMap<>();
            this.qualityScore = 0.0;
            this.relevanceScore = 0.0;
            this.completenessScore = 0.0;
            this.isValid = true;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
            return this;
        }

        public Builder agentId(String agentId) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            return this;
        }

        public Builder userId(String userId) {
            this.userId = Objects.requireNonNull(userId, "userId");
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
            return this;
        }

        public Builder endedAt(Instant endedAt) {
            this.endedAt = Objects.requireNonNull(endedAt, "endedAt");
            return this;
        }

        public Builder sessionDuration(Duration sessionDuration) {
            this.sessionDuration = Objects.requireNonNull(sessionDuration, "sessionDuration");
            return this;
        }

        public Builder totalMessages(int totalMessages) {
            this.totalMessages = totalMessages;
            return this;
        }

        public Builder userMessages(int userMessages) {
            this.userMessages = userMessages;
            return this;
        }

        public Builder agentMessages(int agentMessages) {
            this.agentMessages = agentMessages;
            return this;
        }

        public Builder systemMessages(int systemMessages) {
            this.systemMessages = systemMessages;
            return this;
        }

        public Builder averageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
            return this;
        }

        public Builder totalResponseTime(Duration totalResponseTime) {
            this.totalResponseTime = Objects.requireNonNull(totalResponseTime, "totalResponseTime");
            return this;
        }

        public Builder summary(String summary) {
            this.summary = Objects.requireNonNull(summary, "summary");
            return this;
        }

        public Builder keyTopics(List<String> keyTopics) {
            this.keyTopics = new ArrayList<>(Objects.requireNonNull(keyTopics, "keyTopics"));
            return this;
        }

        public Builder addKeyTopic(String topic) {
            if (this.keyTopics == null) {
                this.keyTopics = new ArrayList<>();
            }
            this.keyTopics.add(Objects.requireNonNull(topic, "topic"));
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

        public Builder insights(Map<String, Object> insights) {
            this.insights = new HashMap<>(Objects.requireNonNull(insights, "insights"));
            return this;
        }

        public Builder addInsight(String key, Object value) {
            if (this.insights == null) {
                this.insights = new HashMap<>();
            }
            this.insights.put(Objects.requireNonNull(key, "key"), value);
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

        public Builder qualityScore(double qualityScore) {
            this.qualityScore = qualityScore;
            return this;
        }

        public Builder relevanceScore(double relevanceScore) {
            this.relevanceScore = relevanceScore;
            return this;
        }

        public Builder completenessScore(double completenessScore) {
            this.completenessScore = completenessScore;
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

        public Builder sentiment(@Nullable String sentiment) {
            this.sentiment = sentiment;
            return this;
        }

        public Builder sentimentScore(@Nullable Double sentimentScore) {
            this.sentimentScore = sentimentScore;
            return this;
        }

        public Builder intent(@Nullable String intent) {
            this.intent = intent;
            return this;
        }

        public Builder confidence(@Nullable Double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder recommendations(@Nullable List<String> recommendations) {
            this.recommendations = recommendations;
            return this;
        }

        public Builder addRecommendation(String recommendation) {
            if (this.recommendations == null) {
                this.recommendations = new ArrayList<>();
            }
            this.recommendations.add(Objects.requireNonNull(recommendation, "recommendation"));
            return this;
        }

        public AgentModelDialogueSummary build() {
            return new AgentModelDialogueSummary(this);
        }
    }
}
