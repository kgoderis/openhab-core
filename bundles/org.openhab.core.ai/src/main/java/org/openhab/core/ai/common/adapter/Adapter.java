package org.openhab.core.ai.common.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified adapter interface for all AI component adapters.
 * 
 * This interface provides a common contract for adapters that transform
 * data between different formats or systems within the AI bundle.
 * 
 * @param <T> The source type to be adapted
 * @param <C> The context type for the adaptation
 * @param <R> The result type after adaptation
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Adapter<T, C, R> {

    /**
     * Adapt the source object using the provided context.
     * 
     * @param source the source object to adapt
     * @param context the context for adaptation
     * @return the adapted result, or null if adaptation fails
     */
    @Nullable
    R adapt(T source, C context);

    /**
     * Check if this adapter can handle the given source object.
     * 
     * @param source the source object to check
     * @return true if this adapter can handle the source
     */
    boolean canAdapt(T source);

    /**
     * Get the source type that this adapter can handle.
     * 
     * @return the source type class
     */
    Class<T> getSourceType();

    /**
     * Get the result type that this adapter produces.
     * 
     * @return the result type class
     */
    Class<R> getResultType();

    /**
     * Get the adapter type identifier.
     * 
     * @return the adapter type
     */
    String getAdapterType();

    /**
     * Get the URI pattern for this adapter.
     * 
     * @return the URI pattern
     */
    String getUriPattern();

    /**
     * Check if the adapter is valid.
     * 
     * @return true if the adapter is valid
     */
    boolean isValid();

    /**
     * Refresh the adapter's internal state.
     * 
     * @param identifier the identifier to refresh
     * @param context the context for refresh
     */
    void refresh(String identifier, C context);

    /**
     * Clean up resources used by this adapter.
     */
    void cleanup();

    /**
     * Close the adapter and release all resources.
     */
    void close();
}
