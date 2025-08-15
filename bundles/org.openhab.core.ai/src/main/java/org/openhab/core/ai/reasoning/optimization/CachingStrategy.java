package org.openhab.core.ai.reasoning.optimization;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Strategy that caches decision results for reuse.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
final class CachingStrategy extends OptimizationStrategy {
    CachingStrategy() {
        super("CACHING", "Caches decision results for reuse", 0.7);
    }
}
