package org.openhab.core.ai.common.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication stub service for testing.
 * 
 * 
 */
public class AIAuthenticationStubService implements AIStubService {

    private static final Logger logger = LoggerFactory.getLogger(AIAuthenticationStubService.class);
    private boolean running = false;
    private AIStubServiceStatistics statistics = new AIStubServiceStatistics();

    @Override
    public boolean start() {
        running = true;
        logger.debug("Authentication stub service started");
        return true;
    }

    @Override
    public boolean stop() {
        running = false;
        logger.debug("Authentication stub service stopped");
        return true;
    }

    @Override
    public void reset() {
        statistics.reset();
        logger.debug("Authentication stub service reset");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public AIStubResponse handleRequest(Object request) {
        statistics.setRequestCount(statistics.getRequestCount() + 1);
        return AIStubResponse.success("Authentication successful");
    }

    @Override
    public void configure(Object configuration) {
        logger.debug("Configuring authentication stub service: {}", configuration);
    }

    @Override
    public String getServiceName() {
        return "auth";
    }

    @Override
    public AIStubServiceStatistics getStatistics() {
        return statistics;
    }
}
