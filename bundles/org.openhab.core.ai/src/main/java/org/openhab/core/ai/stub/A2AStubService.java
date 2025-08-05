package org.openhab.core.ai.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A2A protocol stub service for testing.
 * 
 * 
 */
public class A2AStubService implements StubService {

    private static final Logger logger = LoggerFactory.getLogger(A2AStubService.class);
    private boolean running = false;
    private StubServiceStatistics statistics = new StubServiceStatistics();

    @Override
    public boolean start() {
        running = true;
        logger.debug("A2A stub service started");
        return true;
    }

    @Override
    public boolean stop() {
        running = false;
        logger.debug("A2A stub service stopped");
        return true;
    }

    @Override
    public void reset() {
        statistics.reset();
        logger.debug("A2A stub service reset");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public StubResponse handleRequest(Object request) {
        statistics.setRequestCount(statistics.getRequestCount() + 1);
        return StubResponse.success("A2A message handled");
    }

    @Override
    public void configure(Object configuration) {
        logger.debug("Configuring A2A stub service: {}", configuration);
    }

    @Override
    public String getServiceName() {
        return "a2a";
    }

    @Override
    public StubServiceStatistics getStatistics() {
        return statistics;
    }
}
