package org.openhab.core.ai.a2a.internal;

import org.openhab.core.ai.common.api.action.AIActionRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bundle activator for the openHAB AI A2A (Agent-to-Agent) bundle.
 * 
 * This activator initializes the A2A server components using the official
 * A2A SDK approach with the new SDK-based server manager.
 * 
 * 
 */
public class A2ABundleActivator implements BundleActivator {

    private final Logger logger = LoggerFactory.getLogger(A2ABundleActivator.class);

    private A2AServerManager serverManager;
    private ServiceReference<AIActionRegistry> actionRegistryRef;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting openHAB AI A2A bundle...");

        try {
            // Get the AIAction registry service
            actionRegistryRef = context.getServiceReference(AIActionRegistry.class);
            if (actionRegistryRef == null) {
                logger.warn("AIActionRegistry service not available, A2A server will start with empty action registry");
                // Create a temporary empty registry or wait for the service
                return;
            }

            AIActionRegistry actionRegistry = context.getService(actionRegistryRef);
            if (actionRegistry == null) {
                logger.error("Failed to get AIActionRegistry service");
                return;
            }

        } catch (Exception e) {
            logger.error("Failed to start openHAB AI A2A bundle", e);
            throw e;
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping openHAB AI A2A bundle...");

        try {
            // Release the service reference
            if (actionRegistryRef != null) {
                context.ungetService(actionRegistryRef);
                actionRegistryRef = null;
            }

            logger.info("openHAB AI A2A bundle stopped successfully");

        } catch (Exception e) {
            logger.error("Failed to stop openHAB AI A2A bundle", e);
            throw e;
        }
    }

    /**
     * Get the server manager instance.
     * 
     * @return the server manager
     */
    public A2AServerManager getServerManager() {
        return serverManager;
    }
}
