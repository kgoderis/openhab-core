package org.openhab.core.ai.tool.roots.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for MCP Root data.
 * 
 * This represents a filesystem boundary for server operations.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface Root {

    /**
     * Get the root ID.
     * 
     * @return the root ID
     */
    String getId();

    /**
     * Get the filesystem path.
     * 
     * @return the filesystem path
     */
    String getPath();

    /**
     * Get the root description.
     * 
     * @return the description
     */
    String getDescription();

    /**
     * Check if the root is read-only.
     * 
     * @return true if read-only
     */
    boolean isReadOnly();
}
