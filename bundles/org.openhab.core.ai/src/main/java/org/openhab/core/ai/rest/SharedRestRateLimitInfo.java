package org.openhab.core.ai.rest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Rate limit info used by {@link SharedRestInfrastructure} to track request timestamps.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class SharedRestRateLimitInfo {
    final List<Long> requests = new CopyOnWriteArrayList<>();
}
