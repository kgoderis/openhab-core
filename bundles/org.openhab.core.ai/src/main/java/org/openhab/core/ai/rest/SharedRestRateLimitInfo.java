package org.openhab.core.ai.rest;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rate limit info used by {@link SharedRestInfrastructure} to track request timestamps.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class SharedRestRateLimitInfo {
    final java.util.List<Long> requests = new java.util.concurrent.CopyOnWriteArrayList<>();
}
