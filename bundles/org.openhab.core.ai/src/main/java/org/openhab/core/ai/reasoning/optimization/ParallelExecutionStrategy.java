package org.openhab.core.ai.reasoning.optimization;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Strategy that enables parallel execution of independent decision steps.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
final class ParallelExecutionStrategy extends OptimizationStrategy {
    ParallelExecutionStrategy() {
        super("PARALLEL_EXECUTION", "Enables parallel execution of independent decision steps", 0.9);
    }
}
