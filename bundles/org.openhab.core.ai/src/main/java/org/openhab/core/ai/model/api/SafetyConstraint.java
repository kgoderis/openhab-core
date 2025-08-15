package org.openhab.core.ai.model.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface SafetyConstraint {
    String getId();

    String getType();

    Map<String, Object> getParameters();

    Severity getSeverity();
}
