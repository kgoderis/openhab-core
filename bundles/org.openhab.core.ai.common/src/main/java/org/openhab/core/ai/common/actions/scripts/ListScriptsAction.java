package org.openhab.core.ai.common.actions.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIAction for listing openHAB Scripts with comprehensive filtering and metadata.
 * 
 * 
 */
@Component(service = AIAction.class, immediate = true)
public class ListScriptsAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(ListScriptsAction.class);
    private static final String ACTION_ID = "openhab.scripts.list";
    private static final String ACTION_NAME = "List Scripts";

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return "Lists openHAB Scripts with comprehensive filtering, sorting, and metadata options including script type, size, and modification details";
    }

    @Override
    public String getCategory() {
        return "scripts";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("scriptType",
                Map.of("type", "string", "enum", List.of("js", "py", "rb", "groovy", "jsr223", "all"), "description",
                        "Filter by script type/language", "default", "all"));
        properties.put("nameContains",
                Map.of("type", "string", "description", "Filter by script name containing this text"));
        properties.put("includeContent",
                Map.of("type", "boolean", "description", "Include script content in response", "default", false));
        properties.put("includeMetadata", Map.of("type", "boolean", "description",
                "Include file metadata (size, dates, permissions)", "default", true));
        properties.put("maxContentSize", Map.of("type", "integer", "minimum", 1024, "maximum", 1048576, "description",
                "Maximum script content size to include (bytes)", "default", 102400));
        properties.put("sortBy",
                Map.of("type", "string", "enum", List.of("name", "size", "modified", "type", "extension"),
                        "description", "Sort scripts by specified field", "default", "name"));
        properties.put("sortOrder", Map.of("type", "string", "enum", List.of("asc", "desc"), "description",
                "Sort order", "default", "asc"));
        properties.put("limit", Map.of("type", "integer", "minimum", 1, "maximum", 500, "description",
                "Maximum number of scripts to return", "default", 100));
        properties.put("offset", Map.of("type", "integer", "minimum", 0, "description",
                "Number of scripts to skip (pagination)", "default", 0));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the operation"));
        properties.put("scripts", Map.of("type", "array", "description", "List of script information"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of scripts found"));
        properties.put("returnedCount", Map.of("type", "integer", "description", "Number of scripts returned"));
        properties.put("offset", Map.of("type", "integer", "description", "Pagination offset"));
        properties.put("limit", Map.of("type", "integer", "description", "Pagination limit"));
        properties.put("hasMore", Map.of("type", "boolean", "description", "Whether more scripts are available"));
        properties.put("scriptsDirectory", Map.of("type", "string", "description", "Path to scripts directory"));
        properties.put("summary", Map.of("type", "object", "description", "Summary statistics"));

        schema.put("properties", properties);
        schema.put("required", List.of("timestamp", "scripts", "totalCount", "returnedCount", "offset", "limit",
                "hasMore", "scriptsDirectory", "summary"));
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return AIActionValidationResult.valid(parameters);
        }

        Object limit = parameters.get("limit");
        if (limit != null) {
            if (!(limit instanceof Integer) || (Integer) limit < 1 || (Integer) limit > 500) {
                return AIActionValidationResult.invalid(List.of("limit must be an integer between 1 and 500"));
            }
        }

        Object offset = parameters.get("offset");
        if (offset != null) {
            if (!(offset instanceof Integer) || (Integer) offset < 0) {
                return AIActionValidationResult.invalid(List.of("offset must be a non-negative integer"));
            }
        }

        Object maxContentSize = parameters.get("maxContentSize");
        if (maxContentSize != null) {
            if (!(maxContentSize instanceof Integer) || (Integer) maxContentSize < 1024
                    || (Integer) maxContentSize > 1048576) {
                return AIActionValidationResult
                        .invalid(List.of("maxContentSize must be an integer between 1024 and 1048576 bytes"));
            }
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing list scripts action with parameters: {}", parameters);

        try {
            String scriptType = (String) parameters.getOrDefault("scriptType", "all");
            String nameContains = (String) parameters.get("nameContains");
            boolean includeContent = (Boolean) parameters.getOrDefault("includeContent", false);
            boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", true);
            int maxContentSize = (Integer) parameters.getOrDefault("maxContentSize", 102400);
            String sortBy = (String) parameters.getOrDefault("sortBy", "name");
            String sortOrder = (String) parameters.getOrDefault("sortOrder", "asc");
            int limit = (Integer) parameters.getOrDefault("limit", 100);
            int offset = (Integer) parameters.getOrDefault("offset", 0);

            Map<String, Object> result = listScripts(scriptType, nameContains, includeContent, includeMetadata,
                    maxContentSize, sortBy, sortOrder, limit, offset);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("List scripts action completed in {}ms, returned {} scripts", executionTime,
                    ((List<?>) result.get("scripts")).size());

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to list scripts", e);
            throw new AIActionException(ACTION_ID, "Failed to list Scripts: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<AIActionResult> executeAsync(Map<String, Object> parameters, AIActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (AIActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Comprehensive script listing with filtering, content inclusion, and metadata options")
                .tags(List.of("scripts", "list", "filter", "metadata", "sorting"))
                .documentation(
                        "Lists openHAB Scripts with comprehensive filtering, sorting, and metadata options including script type, size, and modification details")
                .examples(List.of("{} - List all scripts", "{\"scriptType\": \"js\"} - List only JavaScript scripts",
                        "{\"nameContains\": \"light\"} - List scripts with 'light' in the name",
                        "{\"includeContent\": true, \"limit\": 10} - List first 10 scripts with content"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", true, "pagination", true, "metadata", true, "async", true);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("ListScriptsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ListScriptsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> listScripts(String scriptType, String nameContains, boolean includeContent,
            boolean includeMetadata, int maxContentSize, String sortBy, String sortOrder, int limit, int offset)
            throws AIActionException, IOException {

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());

        // Use the proper openHAB configuration directory
        String confDir = OpenHAB.getConfigFolder();
        Path scriptsPath = Paths.get(confDir, "scripts");

        if (!Files.exists(scriptsPath)) {
            result.put("scripts", List.of());
            result.put("totalCount", 0);
            result.put("returnedCount", 0);
            result.put("offset", offset);
            result.put("limit", limit);
            result.put("hasMore", false);
            result.put("scriptsDirectory", scriptsPath.toString());
            result.put("summary",
                    Map.of("totalScripts", 0, "typeBreakdown", Map.of(), "totalSize", 0, "totalSizeFormatted", "0 B"));
            result.put("message", "Scripts directory not found: " + scriptsPath);
            return result;
        }

        // Collect all script files
        List<Map<String, Object>> scripts = new ArrayList<>();
        collectScripts(scriptsPath, scripts, includeContent, includeMetadata, maxContentSize);

        // Apply filters
        List<Map<String, Object>> filteredScripts = applyFilters(scripts, scriptType, nameContains);

        // Sort results
        sortScripts(filteredScripts, sortBy, sortOrder);

        // Apply pagination
        int totalCount = filteredScripts.size();
        List<Map<String, Object>> paginatedScripts = applyPagination(filteredScripts, limit, offset);

        result.put("scripts", paginatedScripts);
        result.put("totalCount", totalCount);
        result.put("returnedCount", paginatedScripts.size());
        result.put("offset", offset);
        result.put("limit", limit);
        result.put("hasMore", offset + paginatedScripts.size() < totalCount);
        result.put("scriptsDirectory", scriptsPath.toString());

        // Add summary statistics
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalScripts", totalCount);
        summary.put("typeBreakdown", getTypeBreakdown(filteredScripts));
        long totalSize = filteredScripts.stream().mapToLong(s -> (Long) s.getOrDefault("size", 0L)).sum();
        summary.put("totalSize", totalSize);
        summary.put("totalSizeFormatted", formatBytes(totalSize));
        result.put("summary", summary);

        return result;
    }

    private void collectScripts(Path scriptsPath, List<Map<String, Object>> scripts, boolean includeContent,
            boolean includeMetadata, int maxContentSize) throws IOException {

        try (Stream<Path> files = Files.walk(scriptsPath)) {
            files.filter(Files::isRegularFile).filter(this::isScriptFile).forEach(file -> {
                try {
                    Map<String, Object> scriptInfo = createScriptInfo(file, includeContent, includeMetadata,
                            maxContentSize);
                    scripts.add(scriptInfo);
                } catch (IOException e) {
                    logger.warn("Failed to read script file: {}", file, e);
                }
            });
        }
    }

    private boolean isScriptFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.endsWith(".js") || fileName.endsWith(".py") || fileName.endsWith(".rb")
                || fileName.endsWith(".groovy") || fileName.endsWith(".jsr223");
    }

    private Map<String, Object> createScriptInfo(Path file, boolean includeContent, boolean includeMetadata,
            int maxContentSize) throws IOException {
        Map<String, Object> scriptInfo = new HashMap<>();

        scriptInfo.put("name", file.getFileName().toString());
        scriptInfo.put("path", file.toString());
        scriptInfo.put("type", determineScriptType(file.getFileName().toString()));
        scriptInfo.put("extension", getFileExtension(file.getFileName().toString()));

        if (includeMetadata) {
            scriptInfo.put("size", Files.size(file));
            scriptInfo.put("sizeFormatted", formatBytes(Files.size(file)));
            scriptInfo.put("lastModified", Files.getLastModifiedTime(file).toInstant().toString());
            scriptInfo.put("readable", Files.isReadable(file));
            scriptInfo.put("writable", Files.isWritable(file));
        }

        if (includeContent && Files.size(file) <= maxContentSize) {
            String content = Files.readString(file);
            scriptInfo.put("content", content);
            scriptInfo.put("contentLength", content.length());
            analyzeScript(content, scriptInfo);
        }

        return scriptInfo;
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private String determineScriptType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return switch (extension) {
            case "js" -> "js";
            case "py" -> "py";
            case "rb" -> "rb";
            case "groovy" -> "groovy";
            case "jsr223" -> "jsr223";
            default -> "unknown";
        };
    }

    private void analyzeScript(String content, Map<String, Object> scriptInfo) {
        Map<String, Object> analysis = new HashMap<>();

        // Basic analysis
        analysis.put("lines", content.split("\n").length);
        analysis.put("characters", content.length());
        analysis.put("nonWhitespaceCharacters", content.replaceAll("\\s", "").length());

        // Count common patterns
        analysis.put("functionCount", countOccurrences(content, "function"));
        analysis.put("ifCount", countOccurrences(content, "if"));
        analysis.put("forCount", countOccurrences(content, "for"));
        analysis.put("whileCount", countOccurrences(content, "while"));

        scriptInfo.put("analysis", analysis);
    }

    private int countOccurrences(String content, String pattern) {
        return content.split(pattern, -1).length - 1;
    }

    private List<Map<String, Object>> applyFilters(List<Map<String, Object>> scripts, String scriptType,
            String nameContains) {
        return scripts.stream().filter(script -> scriptType.equals("all") || scriptType.equals(script.get("type")))
                .filter(script -> nameContains == null
                        || ((String) script.get("name")).toLowerCase().contains(nameContains.toLowerCase()))
                .toList();
    }

    private void sortScripts(List<Map<String, Object>> scripts, String sortBy, String sortOrder) {
        scripts.sort((a, b) -> {
            Object aValue = a.get(sortBy);
            Object bValue = b.get(sortBy);

            if (aValue == null && bValue == null)
                return 0;
            if (aValue == null)
                return 1;
            if (bValue == null)
                return -1;

            int comparison = 0;
            if (aValue instanceof Comparable && bValue instanceof Comparable) {
                @SuppressWarnings("unchecked")
                Comparable<Object> aComp = (Comparable<Object>) aValue;
                comparison = aComp.compareTo(bValue);
            }

            return "desc".equals(sortOrder) ? -comparison : comparison;
        });
    }

    private List<Map<String, Object>> applyPagination(List<Map<String, Object>> scripts, int limit, int offset) {
        int endIndex = Math.min(offset + limit, scripts.size());
        return scripts.subList(Math.min(offset, scripts.size()), endIndex);
    }

    private Map<String, Integer> getTypeBreakdown(List<Map<String, Object>> scripts) {
        Map<String, Integer> breakdown = new HashMap<>();
        for (Map<String, Object> script : scripts) {
            String type = (String) script.get("type");
            breakdown.put(type, breakdown.getOrDefault(type, 0) + 1);
        }
        return breakdown;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
