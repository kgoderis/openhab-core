package org.openhab.core.ai.reasoning;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Batch of reasoning inputs created for processing and routing.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class InputBatch {
    private final String id;
    private final List<ReasoningInput> inputs;
    private final Instant createdAt;
    private final Instant completedAt;

    public InputBatch(String id, List<ReasoningInput> inputs, Instant createdAt, Instant completedAt) {
        this.id = id;
        this.inputs = new ArrayList<>(inputs);
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public String getId() {
        return id;
    }

    public List<ReasoningInput> getInputs() {
        return inputs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}


