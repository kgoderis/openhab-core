package org.openhab.core.ai.reasoning.session;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.model.ModelParameters;
import org.openhab.core.ai.model.api.IntelligentToolClient;
import org.openhab.core.ai.reasoning.api.ReasoningContext;
import org.openhab.core.ai.reasoning.engine.api.MultiStepReasoningResult;

/**
 * Represents a single multi-step reasoning session.
 *
 * Manages execution state and delegates reasoning to the provided client using
 * the supplied context and parameters.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class ReasoningSession {

    private final String sessionId;
    private final IntelligentToolClient client;
    private final ReasoningContext context;
    private final ModelParameters params;
    private final ExecutorService executorService;
    private final Instant startTime;
    private volatile boolean cancelled = false;

    public ReasoningSession(String sessionId, IntelligentToolClient client, ReasoningContext context,
            ModelParameters params, ExecutorService executorService) {
        this.sessionId = sessionId;
        this.client = client;
        this.context = context;
        this.params = params;
        this.executorService = executorService;
        this.startTime = Instant.now();
    }

    public CompletableFuture<MultiStepReasoningResult> execute() {
        if (cancelled) {
            return CompletableFuture.failedFuture(new IllegalStateException("Session cancelled"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return client.reasonWithContext(context, params).get();
            } catch (Exception e) {
                throw new RuntimeException("Reasoning execution failed", e);
            }
        }, executorService);
    }

    public void cancel() {
        cancelled = true;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Instant getStartTime() {
        return startTime;
    }
}
