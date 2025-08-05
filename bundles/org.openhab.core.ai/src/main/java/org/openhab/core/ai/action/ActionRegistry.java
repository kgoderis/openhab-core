package org.openhab.core.ai.action;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.api.action.Action;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized registry for Actions that can be used by both MCP and A2A protocols.
 * This registry provides a unified way to discover and manage Actions across the system.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = ActionRegistry.class, immediate = true)
@NonNullByDefault
public class ActionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ActionRegistry.class);

    private @Nullable BundleContext bundleContext;
    private final Map<String, Action> actions = new ConcurrentHashMap<>();
    private final Map<String, ActionMetadata> metadata = new ConcurrentHashMap<>();

    /**
     * Activate the component.
     * 
     * @param bundleContext the bundle context
     */
    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        logger.info("Action Registry activated");
        discoverActions();
        logger.info("Action Registry started with {} actions", actions.size());
    }

    /**
     * Deactivate the component.
     */
    @Deactivate
    public void deactivate() {
        logger.info("Action Registry deactivated");
        actions.clear();
        metadata.clear();
        this.bundleContext = null;
    }

    /**
     * Bind an Action service.
     * 
     * @param action the Action service to bind
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void bindAction(Action action) {
        registerAction(action);
    }

    /**
     * Unbind an Action service.
     * 
     * @param action the Action service to unbind
     */
    public void unbindAction(Action action) {
        unregisterAction(action.getActionId());
    }

    /**
     * Discover and register available Actions via OSGi services.
     * This method is called during activation to discover existing services.
     */
    private void discoverActions() {
        try {
            // Find all Action services that are already registered
            Collection<ServiceReference<Action>> refs = bundleContext.getServiceReferences(Action.class, null);
            if (refs != null) {
                for (ServiceReference<Action> ref : refs) {
                    Action action = bundleContext.getService(ref);
                    if (action != null) {
                        registerAction(action);
                        // Release the service reference
                        bundleContext.ungetService(ref);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error discovering existing Actions", e);
        }
    }

    /**
     * Register an Action with the registry.
     * 
     * @param action the Action to register
     */
    public void registerAction(Action action) {
        String actionId = action.getActionId();
        actions.put(actionId, action);

        // Create and store metadata
        ActionMetadata actionMetadata = new ActionMetadata(action);
        metadata.put(actionId, actionMetadata);

        logger.debug("Registered action: {} with metadata: {}", actionId, actionMetadata);
    }

    /**
     * Unregister an Action from the registry.
     * 
     * @param actionId the ID of the Action to unregister
     */
    public void unregisterAction(String actionId) {
        actions.remove(actionId);
        metadata.remove(actionId);
        logger.debug("Unregistered action: {}", actionId);
    }

    /**
     * Get an Action by its ID.
     * 
     * @param actionId the ID of the Action to retrieve
     * @return the Action, or null if not found
     */
    public @Nullable Action getAction(String actionId) {
        return actions.get(actionId);
    }

    /**
     * Get metadata for an Action by its ID.
     * 
     * @param actionId the ID of the Action
     * @return the ActionMetadata, or null if not found
     */
    public @Nullable ActionMetadata getActionMetadata(String actionId) {
        return metadata.get(actionId);
    }

    /**
     * Get all registered Actions.
     * 
     * @return a map of all Actions by their IDs
     */
    public Map<String, Action> getAllActions() {
        return new ConcurrentHashMap<>(actions);
    }

    /**
     * Get all Action metadata.
     * 
     * @return a map of all ActionMetadata by Action IDs
     */
    public Map<String, ActionMetadata> getAllMetadata() {
        return new ConcurrentHashMap<>(metadata);
    }

    /**
     * Get the total number of registered Actions.
     * 
     * @return the number of registered Actions
     */
    public int getActionCount() {
        return actions.size();
    }

    /**
     * Check if an Action is registered.
     * 
     * @param actionId the ID of the Action to check
     * @return true if the Action is registered, false otherwise
     */
    public boolean isActionRegistered(String actionId) {
        return actions.containsKey(actionId);
    }

    /**
     * Get all Actions by category.
     * 
     * @param category the category to filter by
     * @return a map of Actions in the specified category
     */
    public Map<String, Action> getActionsByCategory(String category) {
        Map<String, Action> categoryActions = new ConcurrentHashMap<>();
        for (Map.Entry<String, Action> entry : actions.entrySet()) {
            ActionMetadata actionMetadata = metadata.get(entry.getKey());
            if (actionMetadata != null && category.equals(actionMetadata.getCategory())) {
                categoryActions.put(entry.getKey(), entry.getValue());
            }
        }
        return categoryActions;
    }

    /**
     * Get all Action IDs by category.
     * 
     * @param category the category to filter by
     * @return a collection of Action IDs in the specified category
     */
    public Collection<String> getActionIdsByCategory(String category) {
        return getActionsByCategory(category).keySet();
    }
}
