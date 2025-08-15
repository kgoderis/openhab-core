package org.openhab.core.ai.reasoning.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Time ranges for error analytics.
 *
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public enum TimeRange {
    LAST_HOUR,
    LAST_DAY,
    LAST_WEEK,
    LAST_MONTH
}
