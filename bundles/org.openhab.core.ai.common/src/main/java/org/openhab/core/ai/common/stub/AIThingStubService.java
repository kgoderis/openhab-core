package org.openhab.core.ai.common.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openHAB Thing stub service for testing.
 * 
 * 
 */
public class AIThingStubService implements AIStubService {

    private static final Logger logger = LoggerFactory.getLogger(AIThingStubService.class);
    private boolean running = false;
    private AIStubServiceStatistics statistics = new AIStubServiceStatistics();

    @Override
    public boolean start() {
        running = true;
        logger.debug("Thing stub service started");
        return true;
    }

    @Override
    public boolean stop() {
        running = false;
        logger.debug("Thing stub service stopped");
        return true;
    }

    @Override
    public void reset() {
        statistics.reset();
        logger.debug("Thing stub service reset");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public AIStubResponse handleRequest(Object request) {
        statistics.setRequestCount(statistics.getRequestCount() + 1);
        return AIStubResponse.success("Thing operation handled");
    }

    @Override
    public void configure(Object configuration) {
        logger.debug("Configuring Thing stub service: {}", configuration);
    }

    @Override
    public String getServiceName() {
        return "things";
    }

    @Override
    public AIStubServiceStatistics getStatistics() {
        return statistics;
    }
}
