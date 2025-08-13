package org.openhab.core.ai.reasoning;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Strategy that reduces decision complexity through step consolidation.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
final class ComplexityReductionStrategy extends OptimizationStrategy {
    ComplexityReductionStrategy() {
        super("COMPLEXITY_REDUCTION", "Reduces decision complexity through step consolidation", 0.8);
    }
}


