package org.openhab.core.ai.mcp.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bundle activator for MCP bundle.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class MCPBundleActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(MCPBundleActivator.class);

    @Override
    public void start(@Nullable BundleContext bundleContext) throws Exception {
        logger.info("Starting openHAB MCP bundle (SDK)");

        try {
            logger.info("openHAB MCP bundle (SDK) started successfully");
        } catch (Exception e) {
            logger.error("Failed to start openHAB MCP bundle (SDK)", e);
            throw e;
        }
    }

    @Override
    public void stop(@Nullable BundleContext bundleContext) throws Exception {
        logger.info("Stopping openHAB MCP bundle (SDK)");

        try {
            logger.info("openHAB MCP bundle (SDK) stopped successfully");
        } catch (Exception e) {
            logger.error("Error stopping openHAB MCP bundle (SDK)", e);
            throw e;
        }
    }
}
