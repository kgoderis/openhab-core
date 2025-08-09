package org.openhab.core.ai.tool.roots;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.roots.discovery.Root;

/**
 * Interface for MCP Roots service.
 * 
 * This service provides filesystem boundary management for server operations,
 * allowing clients to specify which directories servers should focus on.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface RootsService {

    /**
     * List all active roots.
     * 
     * @return list of active roots
     */
    List<Root> listRoots();

    /**
     * Get a root by ID.
     * 
     * @param rootId the root ID
     * @return the root or null if not found
     */
    @Nullable
    Root getRoot(String rootId);

    /**
     * Add a new root.
     * 
     * @param rootId the root ID
     * @param path the filesystem path
     * @param description the root description
     * @param readOnly whether the root is read-only
     * @return true if added successfully
     */
    boolean addRoot(String rootId, String path, String description, boolean readOnly);

    /**
     * Remove a root.
     * 
     * @param rootId the root ID
     * @return true if removed successfully
     */
    boolean removeRoot(String rootId);

    /**
     * Get the count of active roots.
     * 
     * @return root count
     */
    int getRootCount();

    /**
     * Get performance metrics.
     * 
     * @return performance metrics as a map
     */
    Map<String, Object> getPerformanceMetrics();
}
