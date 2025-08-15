package org.openhab.core.ai.tool.validation.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ValidationListener {
    void onValidationEvent(ValidationEvent event);
}
