package org.openhab.core.ai.reasoning.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Intent categories recognized by the NLP processor.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum IntentType {
    CONTROL_DEVICE,
    QUERY_STATUS,
    SET_PREFERENCE,
    REQUEST_HELP,
    ACKNOWLEDGE,
    UNKNOWN,
    ERROR
}
