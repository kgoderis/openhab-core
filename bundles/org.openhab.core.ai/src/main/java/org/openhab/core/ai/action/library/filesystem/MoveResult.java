package org.openhab.core.ai.action.library.filesystem;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result metrics for file move operations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class MoveResult {
    int itemsMoved = 0;
    long bytesMoved = 0;
}
