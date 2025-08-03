package org.openhab.core.ai.a2a.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bundle activator for A2A bundle.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
public class A2ABundleActivator implements BundleActivator {

    private final Logger logger = LoggerFactory.getLogger(A2ABundleActivator.class);

    private @Nullable A2AProtocolHandler protocolHandler;
    private @Nullable ServiceReference<AIActionRegistry> actionRegistryRef;

    @Override
    public void start(org.osgi.framework.BundleContext context) throws Exception {
        try {
            logger.info("Starting A2A bundle...");

            // Initialize protocol handler
            protocolHandler = new A2AProtocolHandler();
            if (protocolHandler != null) {
                protocolHandler.start();
            }

            // Note: A2AProtocolHandler is already registered as an OSGi service via @Component annotation
            // No need to manually register it here

            logger.info("A2A bundle started successfully");

        } catch (Exception e) {
            logger.error("Error starting A2A bundle", e);
            throw e;
        }
    }

    @Override
    public void stop(org.osgi.framework.BundleContext context) throws Exception {
        try {
            logger.info("Stopping A2A bundle...");

            // Clean up protocol handler
            A2AProtocolHandler handler = protocolHandler;
            if (handler != null) {
                handler.stop();
                protocolHandler = null;
            }

            // Unregister services
            ServiceReference<AIActionRegistry> ref = actionRegistryRef;
            if (ref != null) {
                context.ungetService(ref);
                actionRegistryRef = null;
            }

            logger.info("A2A bundle stopped successfully");

        } catch (Exception e) {
            logger.error("Error stopping A2A bundle", e);
            throw e;
        }
    }

    /**
     * Get the protocol handler instance.
     * 
     * @return the protocol handler instance
     */
    public @Nullable A2AProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }
}
