package org.openhab.core.ai.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filesystem stub service for testing.
 * 
 * 
 */
public class FilesystemStubService implements StubService {

    private static final Logger logger = LoggerFactory.getLogger(FilesystemStubService.class);
    private boolean running = false;
    private StubServiceStatistics statistics = new StubServiceStatistics();

    @Override
    public boolean start() {
        running = true;
        logger.debug("Filesystem stub service started");
        return true;
    }

    @Override
    public boolean stop() {
        running = false;
        logger.debug("Filesystem stub service stopped");
        return true;
    }

    @Override
    public void reset() {
        statistics.reset();
        logger.debug("Filesystem stub service reset");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public StubResponse handleRequest(Object request) {
        statistics.setRequestCount(statistics.getRequestCount() + 1);
        return StubResponse.success("Filesystem operation handled");
    }

    @Override
    public void configure(Object configuration) {
        logger.debug("Configuring Filesystem stub service: {}", configuration);
    }

    @Override
    public String getServiceName() {
        return "filesystem";
    }

    @Override
    public StubServiceStatistics getStatistics() {
        return statistics;
    }
}
