package org.openhab.core.ai.reasoning;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class FeedbackHistory {
    private final String userId;
    private final List<FeedbackEntry> feedbackEntries = new ArrayList<>();

    public FeedbackHistory(String userId) { this.userId = userId; }

    public void addFeedback(String agentId, String feedbackType, double feedbackScore, Map<String, Object> feedbackData) {
        feedbackEntries.add(new FeedbackEntry(agentId, feedbackType, feedbackScore, feedbackData, Instant.now()));
        if (feedbackEntries.size() > 500) { feedbackEntries.remove(0); }
    }

    public List<FeedbackEntry> getRecentFeedback(int limit) {
        List<FeedbackEntry> recent = new ArrayList<>(feedbackEntries);
        recent.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        if (recent.size() > limit) { recent = recent.subList(0, limit); }
        return recent;
    }

    public int getFeedbackCount() { return feedbackEntries.size(); }
    public String getUserId() { return userId; }
}


