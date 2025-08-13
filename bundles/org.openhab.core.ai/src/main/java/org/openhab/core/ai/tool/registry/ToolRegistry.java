package org.openhab.core.ai.tool.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.adapter.ToolAdapter;
import org.openhab.core.ai.tool.api.CompletionRegistry;
import org.openhab.core.ai.tool.api.PromptRegistry;
import org.openhab.core.ai.tool.api.ResourceRegistry;
import org.openhab.core.ai.tool.api.Tool;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Registry for MCP Tools.
 *
 * This class manages the registration and discovery of MCP tools,
 * providing access to tool specifications for the MCP server.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
@Component(immediate = true)
public class ToolRegistry {

    static final Logger LOGGER = LoggerFactory.getLogger(ToolRegistry.class);

    /** Map of tool adapters by tool ID. */
    private final Map<String, ToolAdapter> toolAdapters = new ConcurrentHashMap<>();

    /** Map of tools by tool ID. */
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    /** Map of registered tools by service reference. */
    private final Map<ServiceReference<Tool>, Tool> registeredTools = new ConcurrentHashMap<>();

    /** Resource registry. */
    private final ResourceRegistry resourceRegistry;

    /** Prompt registry. */
    private final PromptRegistry promptRegistry;

    /** Completion registry. */
    private final CompletionRegistry completionRegistry;

    /** Bundle context for OSGi service tracking. */
    private @Nullable BundleContext bundleContext;

    /** Service tracker for Tool services. */
    private @Nullable ServiceTracker<Tool, Tool> toolTracker;

    /**
     * Create a new ToolRegistry with all specification registries.
     */
    public ToolRegistry() {
        this.resourceRegistry = new DefaultResourceRegistry();
        this.promptRegistry = new DefaultPromptRegistry();
        this.completionRegistry = new DefaultCompletionRegistry();
    }

    /**
     * Activate the registry with OSGi service tracking.
     * 
     * @param bundleContext the bundle context
     */
    @Activate
    public void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        startToolTracking();
        LOGGER.info("Tool registry activated with OSGi service tracking");
    }

    /**
     * Deactivate the registry.
     */
    @Deactivate
    public void deactivate() {
        stopToolTracking();
        LOGGER.info("Tool registry deactivated");
    }

    /**
     * Start tracking Tool services.
     */
    private void startToolTracking() {
        BundleContext context = bundleContext;
        if (context == null) {
            LOGGER.warn("Bundle context not available for tool tracking");
            return;
        }

        toolTracker = new ServiceTracker<>(context, Tool.class, new ToolServiceCustomizer(this));
        toolTracker.open();
        LOGGER.debug("Tool service tracking started");
    }

    /**
     * Stop tracking Tool services.
     */
    private void stopToolTracking() {
        ServiceTracker<Tool, Tool> tracker = toolTracker;
        if (tracker != null) {
            tracker.close();
            toolTracker = null;
            LOGGER.debug("Tool service tracking stopped");
        }
    }

    /**
     * Get the resource registry.
     *
     * @return the resource registry
     */
    public ResourceRegistry getResourceRegistry() {
        return resourceRegistry;
    }

    /**
     * Get the prompt registry.
     *
     * @return the prompt registry
     */
    public PromptRegistry getPromptRegistry() {
        return promptRegistry;
    }

    /**
     * Get the completion registry.
     *
     * @return the completion registry
     */
    public CompletionRegistry getCompletionRegistry() {
        return completionRegistry;
    }

    /**
     * Register a tool.
     *
     * @param tool the tool to register
     */
    public void registerTool(final Tool tool) {
        String toolId = tool.getId();
        tools.put(toolId, tool);

        // Create and register tool adapter
        ToolAdapter adapter = new ToolAdapter(tool);
        toolAdapters.put(toolId, adapter);

        LOGGER.debug("Registered tool: {}", toolId);
    }

    /**
     * Register a tool with OSGi service reference tracking.
     *
     * @param tool the tool to register
     * @param reference the OSGi service reference
     */
    void registerToolWithReference(final Tool tool, final ServiceReference<Tool> reference) {
        try {
            registerTool(tool);
            registeredTools.put(reference, tool);
            LOGGER.info("Registered tool with OSGi tracking: {} (service: {})", tool.getId(), reference);
        } catch (Exception e) {
            LOGGER.error("Failed to register tool: {}", tool.getId(), e);
        }
    }

    /**
     * Unregister a tool.
     *
     * @param toolId the tool ID to unregister
     */
    public void unregisterTool(final String toolId) {
        tools.remove(toolId);
        toolAdapters.remove(toolId);
        LOGGER.debug("Unregistered tool: {}", toolId);
    }

    /**
     * Unregister a tool with OSGi service reference tracking.
     *
     * @param tool the tool to unregister
     * @param reference the OSGi service reference
     */
    void unregisterToolWithReference(final Tool tool, final ServiceReference<Tool> reference) {
        try {
            unregisterTool(tool.getId());
            registeredTools.remove(reference);
            LOGGER.info("Unregistered tool with OSGi tracking: {} (service: {})", tool.getId(), reference);
        } catch (Exception e) {
            LOGGER.error("Failed to unregister tool: {}", tool.getId(), e);
        }
    }

    /**
     * Get a tool by ID.
     *
     * @param toolId the tool ID
     * @return the tool or null if not found
     */
    public @Nullable Tool getTool(final String toolId) {
        return tools.get(toolId);
    }

    /**
     * Get a tool adapter by ID.
     *
     * @param toolId the tool ID
     * @return the tool adapter or null if not found
     */
    public @Nullable ToolAdapter getToolAdapter(final String toolId) {
        return toolAdapters.get(toolId);
    }

    /**
     * Get all tools.
     *
     * @return all registered tools
     */
    public Map<String, Tool> getAllTools() {
        return new ConcurrentHashMap<>(tools);
    }

    /**
     * Get all tool adapters.
     *
     * @return all registered tool adapters
     */
    public Map<String, ToolAdapter> getAllToolAdapters() {
        return new ConcurrentHashMap<>(toolAdapters);
    }

    /**
     * Get the number of registered tools.
     *
     * @return the number of tools
     */
    public int getToolCount() {
        return tools.size();
    }

    /**
     * Get the number of registered tools with OSGi tracking.
     *
     * @return the number of registered tools
     */
    public int getRegisteredToolCount() {
        return registeredTools.size();
    }

    /**
     * Get all registered tools with OSGi tracking.
     *
     * @return map of registered tools by service reference
     */
    public Map<ServiceReference<Tool>, Tool> getRegisteredTools() {
        return new ConcurrentHashMap<>(registeredTools);
    }

    /**
     * Check if a tool is registered.
     *
     * @param toolId the tool ID
     * @return true if the tool is registered
     */
    public boolean isToolRegistered(final String toolId) {
        return tools.containsKey(toolId);
    }

    /**
     * Get sync tool specifications.
     *
     * @return sync tool specifications
     */
    public McpServerFeatures.SyncToolSpecification[] getSyncToolSpecifications() {
        return toolAdapters.values().stream().map(adapter -> adapter.createSyncToolSpecification())
                .filter(spec -> spec != null).toArray(McpServerFeatures.SyncToolSpecification[]::new);
    }

    /**
     * Get async tool specifications.
     *
     * @return async tool specifications
     */
    public McpServerFeatures.AsyncToolSpecification[] getAsyncToolSpecifications() {
        return toolAdapters.values().stream().map(adapter -> adapter.createAsyncToolSpecification())
                .filter(spec -> spec != null).toArray(McpServerFeatures.AsyncToolSpecification[]::new);
    }

    /**
     * Get sync resource specifications.
     *
     * @return sync resource specifications
     */
    public McpServerFeatures.SyncResourceSpecification[] getSyncResourceSpecifications() {
        return resourceRegistry.getMcpSyncResourceSpecifications();
    }

    /**
     * Get async resource specifications.
     *
     * @return async resource specifications
     */
    public McpServerFeatures.AsyncResourceSpecification[] getAsyncResourceSpecifications() {
        return resourceRegistry.getMcpAsyncResourceSpecifications();
    }

    /**
     * Get sync prompt specifications.
     *
     * @return sync prompt specifications
     */
    public McpServerFeatures.SyncPromptSpecification[] getSyncPromptSpecifications() {
        return promptRegistry.getSyncPromptSpecifications();
    }

    /**
     * Get async prompt specifications.
     *
     * @return async prompt specifications
     */
    public McpServerFeatures.AsyncPromptSpecification[] getAsyncPromptSpecifications() {
        return promptRegistry.getAsyncPromptSpecifications();
    }

    /**
     * Get sync completion specifications.
     *
     * @return sync completion specifications
     */
    public McpServerFeatures.SyncCompletionSpecification[] getSyncCompletionSpecifications() {
        return completionRegistry.getSyncCompletionSpecifications();
    }

    /**
     * Get async completion specifications.
     *
     * @return async completion specifications
     */
    public McpServerFeatures.AsyncCompletionSpecification[] getAsyncCompletionSpecifications() {
        return completionRegistry.getAsyncCompletionSpecifications();
    }

    /**
     * Service tracker customizer for Tool services.
     */
    BundleContext getBundleContext() {
        return bundleContext;
    }
}
