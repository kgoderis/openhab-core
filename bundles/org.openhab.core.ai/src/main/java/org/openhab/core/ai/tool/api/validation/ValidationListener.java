package org.openhab.core.ai.tool.api.validation;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ValidationListener {
    void onValidationEvent(ValidationEvent event);
}


