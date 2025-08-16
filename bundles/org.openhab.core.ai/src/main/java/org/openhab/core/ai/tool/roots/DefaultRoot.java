package org.openhab.core.ai.tool.roots;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.roots.discovery.Root;

/**
 * Default `Root` implementation extracted from `RootDiscoveryManager`.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class DefaultRoot implements Root {
    private final String id;
    private final String path;
    private final String description;
    private final boolean readOnly;

    public DefaultRoot(String id, String path, String description, boolean readOnly) {
        this.id = id;
        this.path = path;
        this.description = description;
        this.readOnly = readOnly;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public String toString() {
        return "Root{id='" + id + "', path='" + path + "', readOnly=" + readOnly + "}";
    }
}
