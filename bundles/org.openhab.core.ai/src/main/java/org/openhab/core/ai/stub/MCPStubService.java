package org.openhab.core.ai.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCP protocol stub service for testing.
 * 
 * 
 */
public class MCPStubService implements StubService {

    private static final Logger logger = LoggerFactory.getLogger(MCPStubService.class);
    private boolean running = false;
    private StubServiceStatistics statistics = new StubServiceStatistics();

    @Override
    public boolean start() {
        running = true;
        logger.debug("MCP stub service started");
        return true;
    }

    @Override
    public boolean stop() {
        running = false;
        logger.debug("MCP stub service stopped");
        return true;
    }

    @Override
    public void reset() {
        statistics.reset();
        logger.debug("MCP stub service reset");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public StubResponse handleRequest(Object request) {
        statistics.setRequestCount(statistics.getRequestCount() + 1);
        return StubResponse.success("MCP request handled");
    }

    @Override
    public void configure(Object configuration) {
        logger.debug("Configuring MCP stub service: {}", configuration);
    }

    @Override
    public String getServiceName() {
        return "mcp";
    }

    @Override
    public StubServiceStatistics getStatistics() {
        return statistics;
    }
}
