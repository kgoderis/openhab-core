package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum FilterType {
    PRIORITY,
    PATTERN,
    SAMPLING,
    TIME_BASED,
    SOURCE_BASED,
    TYPE_BASED,
    CUSTOM
}
