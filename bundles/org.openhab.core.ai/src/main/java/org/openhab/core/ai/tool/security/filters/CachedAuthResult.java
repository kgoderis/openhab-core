package org.openhab.core.ai.tool.security.filters;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Cached authentication result container.
 *
 * <p>
 * Extracted from {@code SecurityFilter.AbstractSecurityFilter} to a top-level
 * class for reuse and clarity.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class CachedAuthResult {

    final SecurityResult result;
    final long expirationTime;

    CachedAuthResult(SecurityResult result, long expirationTime) {
        this.result = result;
        this.expirationTime = expirationTime;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
}
