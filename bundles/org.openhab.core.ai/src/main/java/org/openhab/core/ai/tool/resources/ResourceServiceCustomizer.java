package org.openhab.core.ai.tool.resources;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.resources.api.ResourceRegistry;
import org.openhab.core.ai.tool.resources.api.specification.ResourceSpecification;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service tracker customizer for ResourceSpecification services.
 *
 * Delegates to the owning {@link ResourceRegistrationService} for registration
 * and lifecycle handling.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ResourceServiceCustomizer
        implements ServiceTrackerCustomizer<ResourceSpecification, ResourceSpecification> {

    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceCustomizer.class);

    private final ResourceRegistrationService owner;

    public ResourceServiceCustomizer(ResourceRegistrationService owner) {
        this.owner = owner;
    }

    @Override
    public ResourceSpecification addingService(ServiceReference<ResourceSpecification> reference) {
        BundleContext context = owner.getBundleContext();
        if (context == null) {
            return null;
        }

        ResourceSpecification resource = context.getService(reference);
        if (resource != null) {
            ResourceRegistry registry = owner.getResourceRegistry();
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
        ResourceRegistry registry = owner.getResourceRegistry();
        if (registry != null) {
            registry.unregisterResource(resource.getId());
            logger.debug("Unregistered resource: {}", resource.getId());
        }

        BundleContext context = owner.getBundleContext();
        if (context != null) {
            context.ungetService(reference);
        }
    }
}
