package org.openhab.core.ai.action.library.config;

import java.nio.file.Path;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Backup file descriptor used by configuration backup.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
class BackupFile {
    String path = "";
    Path absolutePath;
    String name = "";
    String type = "";
    long size = 0;
    Instant lastModified;
}
