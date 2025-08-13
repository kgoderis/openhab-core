package org.openhab.core.ai.agent.collaboration.conflict;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Conflict learning model interface.
 *
 * Machine learning-style model for predicting resolutions.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConflictLearningModel {
    String getModelId();
    void train(List<Conflict> trainingData);
    ConflictResolution predict(Conflict conflict);
}
