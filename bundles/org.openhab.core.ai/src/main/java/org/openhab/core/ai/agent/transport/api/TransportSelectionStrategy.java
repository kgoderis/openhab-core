package org.openhab.core.ai.agent.transport.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Transport selection strategies for the A2A transport factory.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum TransportSelectionStrategy {
    FIRST_AVAILABLE,
    BEST_PERFORMANCE,
    LOWEST_LATENCY,
    HIGHEST_RELIABILITY,
    CLIENT_PREFERENCE
}
