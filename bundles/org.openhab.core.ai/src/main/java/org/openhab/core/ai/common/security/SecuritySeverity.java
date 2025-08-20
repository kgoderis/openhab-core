package org.openhab.core.ai.common.security;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Security incident severity levels.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum SecuritySeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
