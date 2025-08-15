package org.openhab.core.ai.tool.validation.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum RuleLifecycleState {
    ACTIVE,
    INACTIVE,
    DEPRECATED,
    TESTING,
    ERROR
}
