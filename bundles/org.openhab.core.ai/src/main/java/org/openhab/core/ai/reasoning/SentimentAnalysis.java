package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Sentiment and emotion analysis result.
 *
 * Extracted from {@link AgentModelNLPProcessor} for reuse across modules.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SentimentAnalysis {

    private final SentimentType sentiment;
    private final EmotionType emotion;
    private final double confidence;

    public SentimentAnalysis(SentimentType sentiment, EmotionType emotion, double confidence) {
        this.sentiment = sentiment;
        this.emotion = emotion;
        this.confidence = confidence;
    }

    public SentimentType getSentiment() {
        return sentiment;
    }

    public EmotionType getEmotion() {
        return emotion;
    }

    public double getConfidence() {
        return confidence;
    }
}


