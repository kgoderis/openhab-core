package org.openhab.core.ai.agent.collaboration.conflict;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Default in-memory implementation of a conflict learning model.
 *
 * A simple placeholder implementation; replace with a real model when available.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultConflictLearningModel implements ConflictLearningModel {
    private final String modelId;
    private final AtomicBoolean trained = new AtomicBoolean(false);

    public DefaultConflictLearningModel(String modelId) {
        this.modelId = Objects.requireNonNull(modelId);
    }

    @Override
    public String getModelId() { return modelId; }

    @Override
    public void train(java.util.List<Conflict> trainingData) { trained.set(true); }

    @Override
    public ConflictResolution predict(Conflict conflict) {
        if (!trained.get()) {
            throw new IllegalStateException("Model must be trained before making predictions");
        }
        return new ConflictResolution("prediction_" + System.currentTimeMillis(), "default", "default",
                ResolutionOutcome.SUCCESS, "Predicted resolution", Map.of(), Instant.now());
    }
}
