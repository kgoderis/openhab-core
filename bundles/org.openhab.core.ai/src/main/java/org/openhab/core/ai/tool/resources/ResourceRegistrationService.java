package org.openhab.core.ai.tool.resources;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.api.ResourceRegistry;
import org.openhab.core.ai.tool.resources.specification.ResourceSpecification;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Registration Service for MCP Resources
 * 
 * This service automatically discovers and registers ResourceSpecification implementations
 * and registers them with the ResourceRegistry.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class ResourceRegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceRegistrationService.class);

    private @Nullable BundleContext bundleContext;
    private @Nullable ServiceTracker<ResourceSpecification, ResourceSpecification> resourceTracker;
    private @Nullable ResourceRegistry resourceRegistry;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        logger.debug("Activating ResourceRegistrationService");

        if (bundleContext != null) {
            resourceTracker = new ServiceTracker<>(bundleContext, ResourceSpecification.class,
                    new ResourceServiceCustomizer());
            resourceTracker.open();
        }
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Deactivating ResourceRegistrationService");

        ServiceTracker<ResourceSpecification, ResourceSpecification> tracker = resourceTracker;
        if (tracker != null) {
            tracker.close();
            resourceTracker = null;
        }

        bundleContext = null;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setResourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
        logger.debug("ResourceRegistry service bound");
    }

    protected void unsetResourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = null;
        logger.debug("ResourceRegistry service unbound");
    }

    /**
     * Service tracker customizer for ResourceSpecification services
     */
    private class ResourceServiceCustomizer
            implements ServiceTrackerCustomizer<ResourceSpecification, ResourceSpecification> {

        @Override
        public ResourceSpecification addingService(ServiceReference<ResourceSpecification> reference) {
            BundleContext context = bundleContext;
            if (context == null) {
                return null;
            }

            ResourceSpecification resource = context.getService(reference);
            if (resource != null) {
                ResourceRegistry registry = resourceRegistry;
                if (registry != null) {
                    registry.registerResource(resource);
                    logger.debug("Registered resource: {}", resource.getId());
                } else {
                    logger.warn("ResourceRegistry not available, cannot register resource: {}", resource.getId());
                }
            }
            return resource;
        }

        @Override
        public void modifiedService(ServiceReference<ResourceSpecification> reference, ResourceSpecification resource) {
            logger.debug("Resource service modified: {}", resource.getId());
            // Re-register the resource
            addingService(reference);
        }

        @Override
        public void removedService(ServiceReference<ResourceSpecification> reference, ResourceSpecification resource) {
            ResourceRegistry registry = resourceRegistry;
            if (registry != null) {
                registry.unregisterResource(resource.getId());
                logger.debug("Unregistered resource: {}", resource.getId());
            }

            BundleContext context = bundleContext;
            if (context != null) {
                context.ungetService(reference);
            }
        }
    }
}
