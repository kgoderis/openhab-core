package org.openhab.core.ai.action.library.filesystem;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result metrics for file copy operations.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
final class CopyResult {
    int itemsCopied = 0;
    long bytesCopied = 0;
}
