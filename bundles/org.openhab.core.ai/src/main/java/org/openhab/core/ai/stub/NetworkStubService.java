package org.openhab.core.ai.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network stub service for testing.
 * 
 * 
 */
public class NetworkStubService implements StubService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkStubService.class);
    private boolean running = false;
    private StubServiceStatistics statistics = new StubServiceStatistics();

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
    public StubResponse handleRequest(Object request) {
        statistics.setRequestCount(statistics.getRequestCount() + 1);
        return StubResponse.success("Network operation handled");
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
    public StubServiceStatistics getStatistics() {
        return statistics;
    }
}
