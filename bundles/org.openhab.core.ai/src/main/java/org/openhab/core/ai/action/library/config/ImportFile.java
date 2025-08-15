package org.openhab.core.ai.action.library.config;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO for configuration import file entry extracted from import archives or JSON.
 *
 * Extracted from {@link ConfigurationImportAction}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ImportFile {
    String path = "";
    String name = "";
    String type = "";
    String content = "";
    Instant lastModified;
}
