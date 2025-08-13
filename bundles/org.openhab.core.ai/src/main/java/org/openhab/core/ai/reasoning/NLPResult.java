package org.openhab.core.ai.reasoning;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Natural language processing result DTO.
 *
 * Extracted from {@link AgentModelNLPProcessor} to a top-level reusable type.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class NLPResult {

    private final String nlpId;
    private final String input;
    private final Intent intent;
    private final Map<String, Object> entities;
    private final SentimentAnalysis sentiment;
    private final String response;
    private final long timestamp;

    public NLPResult(String nlpId, String input, Intent intent, Map<String, Object> entities,
            SentimentAnalysis sentiment, String response, long timestamp) {
        this.nlpId = nlpId;
        this.input = input;
        this.intent = intent;
        this.entities = new ConcurrentHashMap<>(entities);
        this.sentiment = sentiment;
        this.response = response;
        this.timestamp = timestamp;
    }

    public String getNlpId() {
        return nlpId;
    }

    public String getInput() {
        return input;
    }

    public Intent getIntent() {
        return intent;
    }

    public Map<String, Object> getEntities() {
        return new ConcurrentHashMap<>(entities);
    }

    public SentimentAnalysis getSentiment() {
        return sentiment;
    }

    public String getResponse() {
        return response;
    }

    public long getTimestamp() {
        return timestamp;
    }
}


