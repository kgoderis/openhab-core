package org.openhab.core.ai.model.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ConstraintViolation {
    String getConstraintId();
    String getDescription();
    Severity getSeverity();
    List<String> getSuggestedActions();
}


