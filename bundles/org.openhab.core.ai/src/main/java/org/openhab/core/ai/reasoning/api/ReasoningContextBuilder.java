package org.openhab.core.ai.reasoning.api;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class ReasoningContextBuilder {
    String initialContext = "";
    String currentContext = "";
    @Nullable String domain;
    @Nullable String userId;
    @Nullable String sessionId;
    Instant timestamp = Instant.now();
    Map<String, Object> metadata = Map.of();

    public ReasoningContextBuilder initialContext(String initialContext) { this.initialContext = initialContext; return this; }
    public ReasoningContextBuilder currentContext(String currentContext) { this.currentContext = currentContext; return this; }
    public ReasoningContextBuilder domain(@Nullable String domain) { this.domain = domain; return this; }
    public ReasoningContextBuilder userId(@Nullable String userId) { this.userId = userId; return this; }
    public ReasoningContextBuilder sessionId(@Nullable String sessionId) { this.sessionId = sessionId; return this; }
    public ReasoningContextBuilder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
    public ReasoningContextBuilder metadata(Map<String, Object> metadata) { this.metadata = metadata; return this; }
    public ReasoningContext build() { return new ReasoningContext(this); }
}


