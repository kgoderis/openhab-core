package org.openhab.core.ai.reasoning.strategies;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Strategy that terminates decision process early when sufficient confidence is reached.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
final class EarlyTerminationStrategy extends org.openhab.core.ai.reasoning.optimization.OptimizationStrategy {
    EarlyTerminationStrategy() {
        super("EARLY_TERMINATION", "Terminates decision process early when sufficient confidence is reached", 0.6);
    }
}
