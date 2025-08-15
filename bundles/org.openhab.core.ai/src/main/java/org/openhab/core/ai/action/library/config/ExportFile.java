package org.openhab.core.ai.action.library.config;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO for configuration export file metadata and content.
 *
 * Extracted from {@link ConfigurationExportAction}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ExportFile {
    String path = "";
    String name = "";
    String type = "";
    String content = "";
    Long size;
    Instant lastModified;
    Boolean readable;
    Boolean writable;

    Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("path", path);
        map.put("name", name);
        map.put("type", type);
        map.put("content", content);
        map.put("size", size);
        map.put("lastModified", lastModified != null ? lastModified.toString() : null);
        map.put("readable", readable);
        map.put("writable", writable);
        return map;
    }
}
