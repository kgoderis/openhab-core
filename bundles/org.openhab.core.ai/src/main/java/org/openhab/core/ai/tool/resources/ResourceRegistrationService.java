package org.openhab.core.ai.tool.resources;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.resources.api.ResourceRegistry;
import org.openhab.core.ai.tool.resources.api.specification.ResourceSpecification;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.tracker.ServiceTracker;
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
                    new ResourceServiceCustomizer(this));
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
    BundleContext getBundleContext() {
        return bundleContext;
    }

    ResourceRegistry getResourceRegistry() {
        return resourceRegistry;
    }
}
