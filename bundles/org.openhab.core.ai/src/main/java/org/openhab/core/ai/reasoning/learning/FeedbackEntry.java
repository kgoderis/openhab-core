package org.openhab.core.ai.reasoning.learning;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class FeedbackEntry {
    private final String agentId;
    private final String feedbackType;
    private final double feedbackScore;
    private final Map<String, Object> feedbackData;
    private final Instant timestamp;

    public FeedbackEntry(String agentId, String feedbackType, double feedbackScore, Map<String, Object> feedbackData,
            Instant timestamp) {
        this.agentId = agentId;
        this.feedbackType = feedbackType;
        this.feedbackScore = feedbackScore;
        this.feedbackData = feedbackData;
        this.timestamp = timestamp;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public double getFeedbackScore() {
        return feedbackScore;
    }

    public Map<String, Object> getFeedbackData() {
        return feedbackData;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
