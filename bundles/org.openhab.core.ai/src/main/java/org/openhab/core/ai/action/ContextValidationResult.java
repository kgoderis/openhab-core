package org.openhab.core.ai.action;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ContextValidationResult {
    boolean isValid();

    String getMessage();

    List<String> getErrors();

    List<String> getWarnings();
}
