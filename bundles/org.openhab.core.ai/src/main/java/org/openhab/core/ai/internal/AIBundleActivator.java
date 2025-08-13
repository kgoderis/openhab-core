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

        try {
            // Initialize authentication services
            initializeAuthenticationServices();

            // Initialize configuration services
            initializeConfigurationServices();

            // Initialize utility services
            initializeUtilityServices();

            // Initialize stub framework services
            initializeStubFrameworkServices();

            // Initialize A2A protocol handler
            initializeA2AProtocolHandler();

        } catch (Exception e) {
            logger.error("Error initializing shared AI services", e);
            throw new RuntimeException("Failed to initialize AI services", e);
        }

        logger.debug("Shared AI services initialized");
    }

    /**
     * Cleanup shared services when the bundle is stopped.
     */
    private void cleanupSharedServices() {
        logger.debug("Cleaning up shared AI services");

        try {
            // Cleanup authentication services
            cleanupAuthenticationServices();

            // Cleanup configuration services
            cleanupConfigurationServices();

            // Cleanup utility services
            cleanupUtilityServices();

            // Cleanup stub framework services
            cleanupStubFrameworkServices();

            // Cleanup A2A protocol handler
            cleanupA2AProtocolHandler();

        } catch (Exception e) {
            logger.error("Error cleaning up shared AI services", e);
        }

        logger.debug("Shared AI services cleaned up");
    }

    /**
     * Initialize authentication services.
     */
    private void initializeAuthenticationServices() {
        logger.debug("Initializing authentication services");
        // Authentication services are initialized via OSGi components
        // No additional initialization needed here
    }

    /**
     * Initialize configuration services.
     */
    private void initializeConfigurationServices() {
        logger.debug("Initializing configuration services");
        // Configuration services are initialized via OSGi components
        // No additional initialization needed here
    }

    /**
     * Initialize utility services.
     */
    private void initializeUtilityServices() {
        logger.debug("Initializing utility services");
        // Utility services are initialized via OSGi components
        // No additional initialization needed here
    }

    /**
     * Initialize stub framework services.
     */
    private void initializeStubFrameworkServices() {
        logger.debug("Initializing stub framework services");
        // Stub framework services are initialized via OSGi components
        // No additional initialization needed here
    }

    /**
     * Initialize A2A protocol handler.
     */
    private void initializeA2AProtocolHandler() {
        logger.debug("Initializing A2A protocol handler");
        // A2A protocol handler is initialized via OSGi components
        // No additional initialization needed here
    }

    /**
     * Cleanup authentication services.
     */
    private void cleanupAuthenticationServices() {
        logger.debug("Cleaning up authentication services");
        // Authentication services are cleaned up via OSGi components
        // No additional cleanup needed here
    }

    /**
     * Cleanup configuration services.
     */
    private void cleanupConfigurationServices() {
        logger.debug("Cleaning up configuration services");
        // Configuration services are cleaned up via OSGi components
        // No additional cleanup needed here
    }

    /**
     * Cleanup utility services.
     */
    private void cleanupUtilityServices() {
        logger.debug("Cleaning up utility services");
        // Utility services are cleaned up via OSGi components
        // No additional cleanup needed here
    }

    /**
     * Cleanup stub framework services.
     */
    private void cleanupStubFrameworkServices() {
        logger.debug("Cleaning up stub framework services");
        // Stub framework services are cleaned up via OSGi components
        // No additional cleanup needed here
    }

    /**
     * Cleanup A2A protocol handler.
     */
    private void cleanupA2AProtocolHandler() {
        logger.debug("Cleaning up A2A protocol handler");
        // A2A protocol handler is cleaned up via OSGi components
        // No additional cleanup needed here
    }
}
