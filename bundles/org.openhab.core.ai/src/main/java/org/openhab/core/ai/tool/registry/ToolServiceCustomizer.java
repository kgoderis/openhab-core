package org.openhab.core.ai.tool.registry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.Tool;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Service tracker customizer for Tool services.
 *
 * Delegates add/modify/remove events back to the owning {@link ToolRegistry}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ToolServiceCustomizer implements ServiceTrackerCustomizer<Tool, Tool> {

    private final ToolRegistry owner;

    public ToolServiceCustomizer(ToolRegistry owner) {
        this.owner = owner;
    }

    @Override
    public Tool addingService(ServiceReference<Tool> reference) {
        BundleContext context = owner.getBundleContext();
        if (context == null) {
            return null;
        }
        Tool tool = context.getService(reference);
        if (tool != null) {
            owner.registerToolWithReference(tool, reference);
        }
        return tool;
    }

    @Override
    public void modifiedService(ServiceReference<Tool> reference, Tool tool) {
        owner.unregisterToolWithReference(tool, reference);
        owner.registerToolWithReference(tool, reference);
        ToolRegistry.LOGGER.debug("Modified tool service: {}", tool.getId());
    }

    @Override
    public void removedService(ServiceReference<Tool> reference, Tool tool) {
        owner.unregisterToolWithReference(tool, reference);
        BundleContext context = owner.getBundleContext();
        if (context != null) {
            context.ungetService(reference);
        }
    }
}


