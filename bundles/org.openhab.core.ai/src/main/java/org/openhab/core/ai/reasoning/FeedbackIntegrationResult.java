package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class FeedbackIntegrationResult {
    private final boolean success;
    private final String message;
    private final int feedbackCount;

    private FeedbackIntegrationResult(boolean success, String message, int feedbackCount) {
        this.success = success;
        this.message = message;
        this.feedbackCount = feedbackCount;
    }

    public static FeedbackIntegrationResult success(int feedbackCount) {
        return new FeedbackIntegrationResult(true, "Feedback integration successful", feedbackCount);
    }

    public static FeedbackIntegrationResult disabled(String reason) {
        return new FeedbackIntegrationResult(false, "Feedback integration disabled: " + reason, 0);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getFeedbackCount() { return feedbackCount; }
}


