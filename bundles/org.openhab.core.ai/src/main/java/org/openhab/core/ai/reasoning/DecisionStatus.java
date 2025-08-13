package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Decision status values.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum DecisionStatus {
    APPROVED,
    REQUIRES_REVIEW,
    REQUIRES_APPROVAL,
    REJECTED,
    ERROR
}


