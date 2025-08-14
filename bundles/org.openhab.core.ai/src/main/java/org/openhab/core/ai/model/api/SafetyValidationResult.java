package org.openhab.core.ai.model.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface SafetyValidationResult {
    boolean isValid();
    List<ConstraintViolation> getViolations();
    List<String> getRecommendations();
}


