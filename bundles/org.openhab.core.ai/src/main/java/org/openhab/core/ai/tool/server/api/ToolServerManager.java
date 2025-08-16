package org.openhab.core.ai.tool.server.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.server.ServerConfiguration;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.framework.BundleContext;

/**
 * Tool Server Orchestrator Interface
 * 
 * <p>
 * This interface defines the contract for tool server orchestration implementations that provide:
 * - MCP server instance management and coordination
 * - Server creation, configuration, and lifecycle management
 * - Transport management and service integration
 * - Ready marker tracking and core service coordination
 * - Server instance registry and lookup capabilities
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ToolServerManager extends ReadyTracker {

    /**
     * Get a server instance by ID
     * 
     * @param serverId Server identifier
     * @return Server instance or null if not found
     */
    @Nullable
    ToolServer getServerInstance(String serverId);

    /**
     * Get all server instances
     * 
     * @return Map of server instances
     */
    Map<String, ToolServer> getAllServerInstances();

    /**
     * Create a new server instance
     * 
     * @param serverId Server identifier
     * @param config Server configuration
     * @return Created server instance
     * @throws Exception if creation fails
     */
    ToolServer createServerInstance(String serverId, ServerConfiguration config) throws Exception;

    /**
     * Remove a server instance
     * 
     * @param serverId Server identifier
     * @return true if server was removed successfully
     */
    boolean removeServerInstance(String serverId);

    /**
     * Start the orchestrator
     * 
     * @throws Exception if startup fails
     */
    void start() throws Exception;

    /**
     * Stop the orchestrator
     * 
     * @throws Exception if shutdown fails
     */
    void stop() throws Exception;

    /**
     * Check if the orchestrator is started
     * 
     * @return true if started
     */
    boolean isStarted();

    /**
     * Activate the orchestrator
     * 
     * @param bundleContext OSGi bundle context
     * @throws Exception if activation fails
     */
    void activate(BundleContext bundleContext) throws Exception;

    /**
     * Deactivate the orchestrator
     */
    void deactivate();

    /**
     * Handle ready marker added event
     * 
     * @param readyMarker The ready marker that was added
     */
    @Override
    void onReadyMarkerAdded(ReadyMarker readyMarker);

    /**
     * Handle ready marker removed event
     * 
     * @param readyMarker The ready marker that was removed
     */
    @Override
    void onReadyMarkerRemoved(ReadyMarker readyMarker);

    /**
     * Get bundle context
     * 
     * @return Bundle context or null if not available
     */
    @Nullable
    BundleContext getBundleContext();

    /**
     * Check if components are initialized
     * 
     * @return true if components are initialized
     */
    boolean isComponentsInitialized();

    /**
     * Get MCP server ready marker
     * 
     * @return MCP server ready marker
     */
    ReadyMarker getMcpServerReadyMarker();

    /**
     * Get MCP tool registry ready marker
     * 
     * @return MCP tool registry ready marker
     */
    ReadyMarker getMcpToolRegistryReadyMarker();
}
