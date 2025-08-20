package org.openhab.core.ai.agent.collaboration;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified interface for conflict resolution strategies.
 *
 * This interface defines the contract for conflict resolution strategies that can
 * resolve conflicts between agents using both synchronous and asynchronous approaches.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConflictResolutionStrategy {

    /**
     * Resolve a conflict using this strategy asynchronously.
     *
     * @param session the conflict resolution session
     * @return a CompletableFuture containing the conflict resolution result
     */
    default CompletableFuture<ConflictResolutionResult> resolveAsync(Object session) {
        return CompletableFuture.completedFuture(resolve(session));
    }

    /**
     * Resolve a conflict using this strategy synchronously.
     *
     * @param session the conflict resolution session or conflict object
     * @return the conflict resolution result
     */
    ConflictResolutionResult resolve(Object session);

    /**
     * Resolve a conflict with a mediator (synchronous).
     *
     * @param conflict the conflict to resolve
     * @param mediator the mediator to use for resolution
     * @return the conflict resolution result
     */
    default ConflictResolutionResult resolve(Object conflict, @Nullable Object mediator) {
        return resolve(conflict);
    }

    /**
     * Get the strategy name.
     *
     * @return the strategy name
     */
    default String getStrategyName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Get the strategy description.
     *
     * @return the strategy description
     */
    default String getStrategyDescription() {
        return "Conflict resolution strategy";
    }

    /**
     * Check if this strategy can handle the given conflict type.
     *
     * @param conflictType the conflict type
     * @return true if this strategy can handle the conflict type
     */
    default boolean canHandle(String conflictType) {
        return true; // Default implementation handles all conflict types
    }

    /**
     * Get the priority of this strategy (lower values = higher priority).
     *
     * @return the strategy priority
     */
    default int getPriority() {
        return 100; // Default priority
    }

    /**
     * Check if this strategy is enabled.
     *
     * @return true if this strategy is enabled
     */
    default boolean isEnabled() {
        return true; // Default implementation is always enabled
    }
}
