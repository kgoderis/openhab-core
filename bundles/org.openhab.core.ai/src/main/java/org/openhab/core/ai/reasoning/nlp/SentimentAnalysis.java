package org.openhab.core.ai.reasoning.nlp;

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

    private final org.openhab.core.ai.reasoning.enums.SentimentType sentiment;
    private final org.openhab.core.ai.reasoning.enums.EmotionType emotion;
    private final double confidence;

    public SentimentAnalysis(org.openhab.core.ai.reasoning.enums.SentimentType sentiment,
            org.openhab.core.ai.reasoning.enums.EmotionType emotion, double confidence) {
        this.sentiment = sentiment;
        this.emotion = emotion;
        this.confidence = confidence;
    }

    public org.openhab.core.ai.reasoning.enums.SentimentType getSentiment() {
        return sentiment;
    }

    public org.openhab.core.ai.reasoning.enums.EmotionType getEmotion() {
        return emotion;
    }

    public double getConfidence() {
        return confidence;
    }
}
