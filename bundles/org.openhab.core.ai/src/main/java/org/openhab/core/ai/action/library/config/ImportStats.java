package org.openhab.core.ai.action.library.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Aggregate statistics for a configuration import operation.
 *
 * Extracted from {@link ConfigurationImportAction}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ImportStats {
    int processed = 0;
    int created = 0;
    int updated = 0;
    int skipped = 0;
    int errors = 0;
    List<String> validationErrors = new ArrayList<>();
    List<Map<String, Object>> details = new ArrayList<>();
}
