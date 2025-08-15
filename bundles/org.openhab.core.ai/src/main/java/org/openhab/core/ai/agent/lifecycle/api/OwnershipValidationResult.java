package org.openhab.core.ai.agent.lifecycle.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface OwnershipValidationResult {
    boolean isValid();

    String getMessage();

    List<String> getErrors();

    List<String> getWarnings();
}
