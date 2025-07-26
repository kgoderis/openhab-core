package org.openhab.core.ai.common.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network stub service for testing.
 * 
 * 
 */
public class AINetworkStubService implements AIStubService {

    private static final Logger logger = LoggerFactory.getLogger(AINetworkStubService.class);
    private boolean running = false;
    private AIStubServiceStatistics statistics = new AIStubServiceStatistics();

    @Override
    public boolean start() {
        running = true;
        logger.debug("Network stub service started");
        return true;
    }

    @Override
    public boolean stop() {
        running = false;
        logger.debug("Network stub service stopped");
        return true;
    }

    @Override
    public void reset() {
        statistics.reset();
        logger.debug("Network stub service reset");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public AIStubResponse handleRequest(Object request) {
        statistics.setRequestCount(statistics.getRequestCount() + 1);
        return AIStubResponse.success("Network operation handled");
    }

    @Override
    public void configure(Object configuration) {
        logger.debug("Configuring Network stub service: {}", configuration);
    }

    @Override
    public String getServiceName() {
        return "network";
    }

    @Override
    public AIStubServiceStatistics getStatistics() {
        return statistics;
    }
}
