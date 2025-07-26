package org.openhab.core.ai.common.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openHAB Item stub service for testing.
 * 
 * 
 */
public class AIItemStubService implements AIStubService {

    private static final Logger logger = LoggerFactory.getLogger(AIItemStubService.class);
    private boolean running = false;
    private AIStubServiceStatistics statistics = new AIStubServiceStatistics();

    @Override
    public boolean start() {
        running = true;
        logger.debug("Item stub service started");
        return true;
    }

    @Override
    public boolean stop() {
        running = false;
        logger.debug("Item stub service stopped");
        return true;
    }

    @Override
    public void reset() {
        statistics.reset();
        logger.debug("Item stub service reset");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public AIStubResponse handleRequest(Object request) {
        statistics.setRequestCount(statistics.getRequestCount() + 1);
        return AIStubResponse.success("Item operation handled");
    }

    @Override
    public void configure(Object configuration) {
        logger.debug("Configuring Item stub service: {}", configuration);
    }

    @Override
    public String getServiceName() {
        return "items";
    }

    @Override
    public AIStubServiceStatistics getStatistics() {
        return statistics;
    }
}
