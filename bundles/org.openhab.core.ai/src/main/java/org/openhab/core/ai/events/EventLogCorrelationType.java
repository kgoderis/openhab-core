package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum EventLogCorrelationType {
    ERROR_CORRELATION,
    WARNING_CORRELATION,
    SECURITY_CORRELATION,
    PERFORMANCE_CORRELATION,
    GENERAL_CORRELATION
}
