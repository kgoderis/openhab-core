package org.openhab.core.ai.stub;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base interface for stub services in the AI stub framework.
 * 
 * This interface defines the common contract for all stub services
 * that can mock real services, protocols, and external dependencies.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface StubService {

    /**
     * Start the stub service.
     * 
     * @return true if started successfully
     */
    boolean start();

    /**
     * Stop the stub service.
     * 
     * @return true if stopped successfully
     */
    boolean stop();

    /**
     * Reset the stub service to initial state.
     */
    void reset();

    /**
     * Check if the stub service is running.
     * 
     * @return true if running
     */
    boolean isRunning();

    /**
     * Handle a request to the stub service.
     * 
     * @param request Request data
     * @return Stub response
     */
    StubResponse handleRequest(Object request);

    /**
     * Configure the stub service behavior.
     * 
     * @param configuration Configuration data
     */
    void configure(Object configuration);

    /**
     * Get the stub service name.
     * 
     * @return Service name
     */
    String getServiceName();

    /**
     * Get stub service statistics.
     * 
     * @return Service statistics
     */
    StubServiceStatistics getStatistics();
}
