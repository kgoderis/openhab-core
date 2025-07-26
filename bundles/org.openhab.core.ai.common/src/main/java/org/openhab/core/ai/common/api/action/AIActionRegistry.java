package org.openhab.core.ai.common.api.action;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 * Centralized registry for AIActions that can be used by both MCP and A2A protocols.
 * This registry provides a unified way to discover and manage AIActions across the system.
 * 
 * 
 */
@Component(service = AIActionRegistry.class, immediate = true)
public class AIActionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(AIActionRegistry.class);

    private BundleContext bundleContext;
    private final Map<String, AIAction> actions = new ConcurrentHashMap<>();
    private final Map<String, AIActionMetadata> metadata = new ConcurrentHashMap<>();

    /**
     * Activate the component.
     * 
     * @param bundleContext the bundle context
     */
    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        logger.info("AIAction Registry activated");
        discoverActions();
        logger.info("AIAction Registry started with {} actions", actions.size());
    }

    /**
     * Deactivate the component.
     */
    @Deactivate
    public void deactivate() {
        logger.info("AIAction Registry deactivated");
        actions.clear();
        metadata.clear();
        this.bundleContext = null;
    }

    /**
     * Bind an AIAction service.
     * 
     * @param action the AIAction service to bind
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void bindAIAction(AIAction action) {
        registerAction(action);
    }

    /**
     * Unbind an AIAction service.
     * 
     * @param action the AIAction service to unbind
     */
    public void unbindAIAction(AIAction action) {
        unregisterAction(action.getActionId());
    }

    /**
     * Discover and register available AIActions via OSGi services.
     * This method is called during activation to discover existing services.
     */
    private void discoverActions() {
        try {
            // Find all AIAction services that are already registered
            Collection<ServiceReference<AIAction>> refs = bundleContext.getServiceReferences(AIAction.class, null);
            if (refs != null) {
                for (ServiceReference<AIAction> ref : refs) {
                    AIAction action = bundleContext.getService(ref);
                    if (action != null) {
                        registerAction(action);
                        // Release the service reference
                        bundleContext.ungetService(ref);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error discovering existing AIActions", e);
        }
    }

    /**
     * Register an AIAction with the registry.
     * 
     * @param action the action to register
     */
    public void registerAction(AIAction action) {
        String actionId = action.getActionId();
        actions.put(actionId, action);

        try {
            AIActionMetadata actionMetadata = action.getMetadata();
            metadata.put(actionId, actionMetadata);
        } catch (Exception e) {
            logger.warn("Could not get metadata for action {}: {}", actionId, e.getMessage());
        }

        logger.info("Registered AIAction: {} ({})", actionId, action.getActionName());
    }

    /**
     * Unregister an AIAction from the registry.
     * 
     * @param actionId the action ID to unregister
     */
    public void unregisterAction(String actionId) {
        actions.remove(actionId);
        metadata.remove(actionId);
        logger.info("Unregistered AIAction: {}", actionId);
    }

    /**
     * Get an AIAction by ID.
     * 
     * @param actionId action identifier
     * @return action or null if not found
     */
    public AIAction getAction(String actionId) {
        return actions.get(actionId);
    }

    /**
     * Get action metadata by ID.
     * 
     * @param actionId action identifier
     * @return metadata or null if not found
     */
    public AIActionMetadata getActionMetadata(String actionId) {
        return metadata.get(actionId);
    }

    /**
     * Get all registered actions.
     * 
     * @return map of all actions
     */
    public Map<String, AIAction> getAllActions() {
        return new ConcurrentHashMap<>(actions);
    }

    /**
     * Get all action metadata.
     * 
     * @return map of all action metadata
     */
    public Map<String, AIActionMetadata> getAllMetadata() {
        return new ConcurrentHashMap<>(metadata);
    }

    /**
     * Get the number of registered actions.
     * 
     * @return number of actions
     */
    public int getActionCount() {
        return actions.size();
    }

    /**
     * Check if an action is registered.
     * 
     * @param actionId action identifier
     * @return true if registered
     */
    public boolean isActionRegistered(String actionId) {
        return actions.containsKey(actionId);
    }

    /**
     * Get actions by category.
     * 
     * @param category the category to filter by
     * @return map of actions in the category
     */
    public Map<String, AIAction> getActionsByCategory(String category) {
        Map<String, AIAction> categoryActions = new ConcurrentHashMap<>();
        actions.forEach((id, action) -> {
            if (category.equals(action.getCategory())) {
                categoryActions.put(id, action);
            }
        });
        return categoryActions;
    }

    /**
     * Get action IDs by category.
     * 
     * @param category the category to filter by
     * @return collection of action IDs in the category
     */
    public Collection<String> getActionIdsByCategory(String category) {
        return actions.entrySet().stream().filter(entry -> category.equals(entry.getValue().getCategory()))
                .map(Map.Entry::getKey).toList();
    }
}
