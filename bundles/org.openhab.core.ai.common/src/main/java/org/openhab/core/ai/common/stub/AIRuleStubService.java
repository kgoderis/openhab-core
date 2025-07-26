package org.openhab.core.ai.common.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openHAB Rule stub service for testing.
 * 
 * 
 */
public class AIRuleStubService implements AIStubService {

    private static final Logger logger = LoggerFactory.getLogger(AIRuleStubService.class);
    private boolean running = false;
    private AIStubServiceStatistics statistics = new AIStubServiceStatistics();

    @Override
    public boolean start() {
        running = true;
        logger.debug("Rule stub service started");
        return true;
    }

    @Override
    public boolean stop() {
        running = false;
        logger.debug("Rule stub service stopped");
        return true;
    }

    @Override
    public void reset() {
        statistics.reset();
        logger.debug("Rule stub service reset");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public AIStubResponse handleRequest(Object request) {
        statistics.setRequestCount(statistics.getRequestCount() + 1);
        return AIStubResponse.success("Rule operation handled");
    }

    @Override
    public void configure(Object configuration) {
        logger.debug("Configuring Rule stub service: {}", configuration);
    }

    @Override
    public String getServiceName() {
        return "rules";
    }

    @Override
    public AIStubServiceStatistics getStatistics() {
        return statistics;
    }
}
