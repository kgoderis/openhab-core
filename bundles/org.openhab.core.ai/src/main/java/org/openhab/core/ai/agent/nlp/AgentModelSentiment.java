package org.openhab.core.ai.agent.nlp;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents sentiment analysis results from NLP processing.
 * 
 * This class encapsulates the overall sentiment and individual scores
 * for positive, negative, and neutral sentiment components.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentModelSentiment {

    private final String sentiment;
    private final double positiveScore;
    private final double negativeScore;
    private final double neutralScore;

    private AgentModelSentiment(Builder builder) {
        this.sentiment = Objects.requireNonNull(builder.sentiment, "sentiment");
        this.positiveScore = Math.max(0.0, Math.min(1.0, builder.positiveScore));
        this.negativeScore = Math.max(0.0, Math.min(1.0, builder.negativeScore));
        this.neutralScore = Math.max(0.0, Math.min(1.0, builder.neutralScore));
    }

    /**
     * Create a new builder.
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Get the overall sentiment.
     * 
     * @return the sentiment (positive, negative, neutral)
     */
    public String getSentiment() {
        return sentiment;
    }

    /**
     * Get the positive sentiment score.
     * 
     * @return the positive score
     */
    public double getPositiveScore() {
        return positiveScore;
    }

    /**
     * Get the negative sentiment score.
     * 
     * @return the negative score
     */
    public double getNegativeScore() {
        return negativeScore;
    }

    /**
     * Get the neutral sentiment score.
     * 
     * @return the neutral score
     */
    public double getNeutralScore() {
        return neutralScore;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AgentModelSentiment other = (AgentModelSentiment) obj;
        return Double.compare(other.positiveScore, positiveScore) == 0
                && Double.compare(other.negativeScore, negativeScore) == 0
                && Double.compare(other.neutralScore, neutralScore) == 0 && Objects.equals(sentiment, other.sentiment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sentiment, positiveScore, negativeScore, neutralScore);
    }

    @Override
    public String toString() {
        return String.format("AgentModelSentiment{sentiment='%s', positive=%.2f, negative=%.2f, neutral=%.2f}",
                sentiment, positiveScore, negativeScore, neutralScore);
    }

    /**
     * Builder for AgentModelSentiment.
     */
    public static final class Builder {
        private String sentiment = "neutral";
        private double positiveScore = 0.0;
        private double negativeScore = 0.0;
        private double neutralScore = 1.0;

        public Builder withSentiment(String sentiment) {
            this.sentiment = Objects.requireNonNull(sentiment, "sentiment");
            return this;
        }

        public Builder withPositiveScore(double positiveScore) {
            this.positiveScore = positiveScore;
            return this;
        }

        public Builder withNegativeScore(double negativeScore) {
            this.negativeScore = negativeScore;
            return this;
        }

        public Builder withNeutralScore(double neutralScore) {
            this.neutralScore = neutralScore;
            return this;
        }

        public AgentModelSentiment build() {
            return new AgentModelSentiment(this);
        }
    }
}
