package org.openhab.core.ai.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consolidated bundle activator for the openHAB AI Common bundle.
 * 
 * This activator handles the lifecycle of the consolidated AI bundle,
 * which provides common functionality for both MCP and A2A protocol implementations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class AIBundleActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(AIBundleActivator.class);

    private static @Nullable BundleContext bundleContext;

    @Override
    public void start(@Nullable BundleContext context) throws Exception {
        logger.info("Starting openHAB AI Common bundle");
        bundleContext = context;

        try {
            // Initialize shared services
            initializeSharedServices();

            logger.info("openHAB AI Common bundle started successfully");
        } catch (Exception e) {
            logger.error("Failed to start openHAB AI Common bundle", e);
            throw e;
        }
    }

    @Override
    public void stop(@Nullable BundleContext context) throws Exception {
        logger.info("Stopping openHAB AI Common bundle");

        try {
            // Cleanup shared services
            cleanupSharedServices();

            bundleContext = null;
            logger.info("openHAB AI Common bundle stopped successfully");
        } catch (Exception e) {
            logger.error("Error stopping openHAB AI Common bundle", e);
            throw e;
        }
    }

    /**
     * Get the bundle context for this bundle.
     * 
     * @return the bundle context, or null if the bundle is not active
     */
    public static @Nullable BundleContext getBundleContext() {
        return bundleContext;
    }

    /**
     * Initialize shared services for AI protocols.
     * This includes authentication, configuration, and utility services.
     */
    private void initializeSharedServices() {
        logger.debug("Initializing shared AI services");

        // TODO: Initialize authentication services when implemented
        // TODO: Initialize configuration services when implemented
        // TODO: Initialize utility services when implemented
        // TODO: Initialize stub framework services when implemented
        // TODO: Initialize A2A protocol handler when Agent classes are available

        logger.debug("Shared AI services initialized");
    }

    /**
     * Cleanup shared services when the bundle is stopped.
     */
    private void cleanupSharedServices() {
        logger.debug("Cleaning up shared AI services");

        // TODO: Cleanup authentication services when implemented
        // TODO: Cleanup configuration services when implemented
        // TODO: Cleanup utility services when implemented
        // TODO: Cleanup stub framework services when implemented
        // TODO: Cleanup A2A protocol handler when Agent classes are available

        logger.debug("Shared AI services cleaned up");
    }
}
