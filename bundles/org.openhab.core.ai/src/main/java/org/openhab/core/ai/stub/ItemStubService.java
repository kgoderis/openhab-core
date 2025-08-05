package org.openhab.core.ai.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openHAB Item stub service for testing.
 * 
 * 
 */
public class ItemStubService implements StubService {

    private static final Logger logger = LoggerFactory.getLogger(ItemStubService.class);
    private boolean running = false;
    private StubServiceStatistics statistics = new StubServiceStatistics();

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
    public StubResponse handleRequest(Object request) {
        statistics.setRequestCount(statistics.getRequestCount() + 1);
        return StubResponse.success("Item operation handled");
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
    public StubServiceStatistics getStatistics() {
        return statistics;
    }
}
