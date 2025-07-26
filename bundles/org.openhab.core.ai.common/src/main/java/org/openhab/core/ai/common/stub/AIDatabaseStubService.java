package org.openhab.core.ai.common.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database stub service for testing.
 * 
 * 
 */
public class AIDatabaseStubService implements AIStubService {

    private static final Logger logger = LoggerFactory.getLogger(AIDatabaseStubService.class);
    private boolean running = false;
    private AIStubServiceStatistics statistics = new AIStubServiceStatistics();

    @Override
    public boolean start() {
        running = true;
        logger.debug("Database stub service started");
        return true;
    }

    @Override
    public boolean stop() {
        running = false;
        logger.debug("Database stub service stopped");
        return true;
    }

    @Override
    public void reset() {
        statistics.reset();
        logger.debug("Database stub service reset");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public AIStubResponse handleRequest(Object request) {
        statistics.setRequestCount(statistics.getRequestCount() + 1);
        return AIStubResponse.success("Database operation handled");
    }

    @Override
    public void configure(Object configuration) {
        logger.debug("Configuring Database stub service: {}", configuration);
    }

    @Override
    public String getServiceName() {
        return "database";
    }

    @Override
    public AIStubServiceStatistics getStatistics() {
        return statistics;
    }
}
