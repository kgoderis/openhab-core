package org.openhab.core.ai.reasoning.strategies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.optimization.OptimizationStrategy;

/**
 * Strategy that terminates decision process early when sufficient confidence is reached.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
final class EarlyTerminationStrategy extends OptimizationStrategy {
    EarlyTerminationStrategy() {
        super("EARLY_TERMINATION", "Terminates decision process early when sufficient confidence is reached", 0.6);
    }
}
