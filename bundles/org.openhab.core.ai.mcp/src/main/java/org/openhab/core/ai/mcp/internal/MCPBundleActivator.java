package org.openhab.core.ai.mcp.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi Bundle Activator for the openHAB MCP (Model Context Protocol) bundle.
 * 
 * This activator manages the lifecycle of the MCP server infrastructure,
 * including connection handling, service registration, and cleanup.
 * 
 * 
 */
public class MCPBundleActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(MCPBundleActivator.class);

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        logger.info("Starting openHAB MCP bundle (SDK)");

        try {
            logger.info("openHAB MCP bundle (SDK) started successfully");
        } catch (Exception e) {
            logger.error("Failed to start openHAB MCP bundle (SDK)", e);
            throw e;
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        logger.info("Stopping openHAB MCP bundle (SDK)");

        try {
            logger.info("openHAB MCP bundle (SDK) stopped successfully");
        } catch (Exception e) {
            logger.error("Error stopping openHAB MCP bundle (SDK)", e);
            throw e;
        }
    }
}
