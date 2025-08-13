package org.openhab.core.ai.tool.resources;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when a resource limit is exceeded.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ResourceLimitExceededException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResourceLimitExceededException(String message) {
        super(message);
    }
}
