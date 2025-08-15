package org.openhab.core.ai.action.library.scripts;

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
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for searching openHAB Scripts with comprehensive search capabilities.
 * 
 * Provides script search functionality with content analysis, pattern matching,
 * and metadata filtering using real file system integration.
 */
@Component(service = Action.class, immediate = true)
public class SearchScriptsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SearchScriptsAction.class);
    private static final String ACTION_ID = "openhab.scripts.search";
    private static final String ACTION_NAME = "Search Scripts";

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
        return "Searches openHAB Scripts with comprehensive search capabilities and content analysis";
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
        properties.put("query",
                Map.of("type", "string", "description", "Search query (text to search for)", "required", true));
        properties.put("searchType", Map.of("type", "string", "enum", List.of("content", "filename", "metadata", "all"),
                "description", "Type of search to perform", "default", "all"));
        properties.put("scriptType",
                Map.of("type", "string", "enum", List.of("javascript", "python", "ruby", "groovy", "jsr223", "all"),
                        "description", "Filter by script type", "default", "all"));
        properties.put("caseSensitive",
                Map.of("type", "boolean", "description", "Case-sensitive search", "default", false));
        properties.put("useRegex",
                Map.of("type", "boolean", "description", "Use regular expression for search", "default", false));
        properties.put("includeContent",
                Map.of("type", "boolean", "description", "Include script content in results", "default", false));
        properties.put("maxResults", Map.of("type", "integer", "minimum", 1, "maximum", 100, "description",
                "Maximum number of results to return", "default", 20));
        properties.put("searchSubdirectories",
                Map.of("type", "boolean", "description", "Search in subdirectories", "default", true));
        properties.put("minFileSize",
                Map.of("type", "integer", "minimum", 0, "description", "Minimum file size in bytes", "default", 0));
        properties.put("maxFileSize",
                Map.of("type", "integer", "minimum", 1, "description", "Maximum file size in bytes"));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean"));
        properties.put("results", Map.of("type", "array"));
        properties.put("totalResults", Map.of("type", "integer"));
        properties.put("searchInfo", Map.of("type", "object"));
        properties.put("statistics", Map.of("type", "object"));
        properties.put("error", Map.of("type", "string"));
        properties.put("timestamp", Map.of("type", "string"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null or empty"));
        }

        String query = (String) parameters.get("query");
        if (query == null || query.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("query is required and cannot be empty"));
        }

        // Validate regex if specified
        Boolean useRegex = (Boolean) parameters.getOrDefault("useRegex", false);
        if (useRegex) {
            try {
                Pattern.compile(query);
            } catch (Exception e) {
                return ActionValidationResult.invalid(List.of("Invalid regular expression: " + e.getMessage()));
            }
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing search scripts action with parameters: {}", parameters);

        try {
            String query = (String) parameters.get("query");
            String searchType = (String) parameters.getOrDefault("searchType", "all");
            String scriptType = (String) parameters.getOrDefault("scriptType", "all");
            boolean caseSensitive = (Boolean) parameters.getOrDefault("caseSensitive", false);
            boolean useRegex = (Boolean) parameters.getOrDefault("useRegex", false);
            boolean includeContent = (Boolean) parameters.getOrDefault("includeContent", false);
            int maxResults = (Integer) parameters.getOrDefault("maxResults", 20);
            boolean searchSubdirectories = (Boolean) parameters.getOrDefault("searchSubdirectories", true);
            int minFileSize = (Integer) parameters.getOrDefault("minFileSize", 0);
            Integer maxFileSize = (Integer) parameters.get("maxFileSize");

            Map<String, Object> result = searchScripts(query, searchType, scriptType, caseSensitive, useRegex,
                    includeContent, maxResults, searchSubdirectories, minFileSize, maxFileSize);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Search scripts action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to search scripts", e);
            throw new ActionException(ACTION_ID, "Failed to search scripts: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (ActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().version(getVersion())
                .description("Searches scripts with comprehensive capabilities").build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", true, "pagination", true, "metadata", true, "async", true,
                "sandboxing", false, "timeout", false);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("SearchScriptsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("SearchScriptsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> searchScripts(String query, String searchType, String scriptType, boolean caseSensitive,
            boolean useRegex, boolean includeContent, int maxResults, boolean searchSubdirectories, int minFileSize,
            Integer maxFileSize) throws ActionException, IOException {

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());

        try {
            String scriptsDir = OpenHAB.getConfigFolder() + "/scripts";
            Path scriptsPath = Paths.get(scriptsDir);

            if (!Files.exists(scriptsPath)) {
                result.put("success", false);
                result.put("error", "Scripts directory not found: " + scriptsPath);
                return result;
            }

            // Prepare search pattern
            Pattern searchPattern = prepareSearchPattern(query, caseSensitive, useRegex);

            // Collect script files
            List<Path> scriptFiles = collectScriptFiles(scriptsPath, scriptType, searchSubdirectories, minFileSize,
                    maxFileSize);

            // Perform search
            List<Map<String, Object>> searchResults = new ArrayList<>();
            Map<String, Object> statistics = new HashMap<>();
            int totalFiles = 0;
            int matchingFiles = 0;
            int contentMatches = 0;
            int filenameMatches = 0;
            int metadataMatches = 0;

            for (Path scriptFile : scriptFiles) {
                totalFiles++;
                Map<String, Object> fileResult = searchScriptFile(scriptFile, searchPattern, searchType,
                        includeContent);

                if ((Boolean) fileResult.get("matched")) {
                    matchingFiles++;
                    searchResults.add(fileResult);

                    if ((Boolean) fileResult.get("contentMatch"))
                        contentMatches++;
                    if ((Boolean) fileResult.get("filenameMatch"))
                        filenameMatches++;
                    if ((Boolean) fileResult.get("metadataMatch"))
                        metadataMatches++;

                    if (searchResults.size() >= maxResults) {
                        break;
                    }
                }
            }

            // Build search information
            Map<String, Object> searchInfo = new HashMap<>();
            searchInfo.put("query", query);
            searchInfo.put("searchType", searchType);
            searchInfo.put("scriptType", scriptType);
            searchInfo.put("caseSensitive", caseSensitive);
            searchInfo.put("useRegex", useRegex);
            searchInfo.put("searchSubdirectories", searchSubdirectories);
            searchInfo.put("maxResults", maxResults);
            searchInfo.put("executionTime", System.currentTimeMillis());

            // Build statistics
            statistics.put("totalFiles", totalFiles);
            statistics.put("matchingFiles", matchingFiles);
            statistics.put("contentMatches", contentMatches);
            statistics.put("filenameMatches", filenameMatches);
            statistics.put("metadataMatches", metadataMatches);
            statistics.put("resultsReturned", searchResults.size());

            result.put("success", true);
            result.put("results", searchResults);
            result.put("totalResults", matchingFiles);
            result.put("searchInfo", searchInfo);
            result.put("statistics", statistics);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to search scripts: " + e.getMessage());
            logger.warn("Failed to search scripts: {}", e.getMessage());
        }

        return result;
    }

    private Pattern prepareSearchPattern(String query, boolean caseSensitive, boolean useRegex) {
        if (useRegex) {
            return caseSensitive ? Pattern.compile(query) : Pattern.compile(query, Pattern.CASE_INSENSITIVE);
        } else {
            String pattern = Pattern.quote(query);
            return caseSensitive ? Pattern.compile(pattern) : Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        }
    }

    private List<Path> collectScriptFiles(Path scriptsPath, String scriptType, boolean searchSubdirectories,
            int minFileSize, Integer maxFileSize) throws IOException {
        List<Path> scriptFiles = new ArrayList<>();

        try (Stream<Path> paths = searchSubdirectories ? Files.walk(scriptsPath) : Files.list(scriptsPath)) {

            paths.filter(Files::isRegularFile).filter(path -> isScriptFile(path, scriptType)).filter(path -> {
                try {
                    long size = Files.size(path);
                    return size >= minFileSize && (maxFileSize == null || size <= maxFileSize);
                } catch (IOException e) {
                    return false;
                }
            }).forEach(scriptFiles::add);
        }

        return scriptFiles;
    }

    private boolean isScriptFile(Path path, String scriptType) {
        String fileName = path.getFileName().toString().toLowerCase();

        if ("all".equals(scriptType)) {
            return fileName.endsWith(".js") || fileName.endsWith(".py") || fileName.endsWith(".rb")
                    || fileName.endsWith(".groovy") || fileName.endsWith(".jsr223");
        }

        return switch (scriptType) {
            case "javascript" -> fileName.endsWith(".js");
            case "python" -> fileName.endsWith(".py");
            case "ruby" -> fileName.endsWith(".rb");
            case "groovy" -> fileName.endsWith(".groovy");
            case "jsr223" -> fileName.endsWith(".jsr223");
            default -> false;
        };
    }

    private Map<String, Object> searchScriptFile(Path scriptFile, Pattern searchPattern, String searchType,
            boolean includeContent) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("path", scriptFile.toString());
        result.put("name", scriptFile.getFileName().toString());
        result.put("matched", false);
        result.put("contentMatch", false);
        result.put("filenameMatch", false);
        result.put("metadataMatch", false);

        // Check filename
        boolean filenameMatch = searchPattern.matcher(scriptFile.getFileName().toString()).find();
        result.put("filenameMatch", filenameMatch);

        // Check content if needed
        boolean contentMatch = false;
        List<Map<String, Object>> contentMatches = new ArrayList<>();

        if ("content".equals(searchType) || "all".equals(searchType)) {
            String content = Files.readString(scriptFile);
            contentMatch = searchPattern.matcher(content).find();

            if (contentMatch) {
                contentMatches = findContentMatches(content, searchPattern);
            }
        }

        result.put("contentMatch", contentMatch);
        result.put("contentMatches", contentMatches);

        // Check metadata
        boolean metadataMatch = false;
        if ("metadata".equals(searchType) || "all".equals(searchType)) {
            metadataMatch = checkMetadataMatch(scriptFile, searchPattern);
        }
        result.put("metadataMatch", metadataMatch);

        // Determine overall match
        boolean matched = filenameMatch || contentMatch || metadataMatch;
        result.put("matched", matched);

        // Add file information
        if (matched) {
            result.put("size", Files.size(scriptFile));
            result.put("lastModified", Files.getLastModifiedTime(scriptFile).toInstant().toString());
            result.put("type", determineScriptType(scriptFile.getFileName().toString()));

            if (includeContent) {
                result.put("content", Files.readString(scriptFile));
            }
        }

        return result;
    }

    private List<Map<String, Object>> findContentMatches(String content, Pattern searchPattern) {
        List<Map<String, Object>> matches = new ArrayList<>();
        String[] lines = content.split("\r\n|\r|\n");

        for (int i = 0; i < lines.length; i++) {
            if (searchPattern.matcher(lines[i]).find()) {
                Map<String, Object> match = new HashMap<>();
                match.put("line", i + 1);
                match.put("content", lines[i]);
                match.put("startIndex", lines[i].indexOf(searchPattern.pattern()));
                matches.add(match);
            }
        }

        return matches;
    }

    private boolean checkMetadataMatch(Path scriptFile, Pattern searchPattern) throws IOException {
        // Check file metadata like creation time, modification time, etc.
        String metadata = Files.getLastModifiedTime(scriptFile).toString();
        return searchPattern.matcher(metadata).find();
    }

    private String determineScriptType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return switch (extension) {
            case "js" -> "javascript";
            case "py" -> "python";
            case "rb" -> "ruby";
            case "groovy" -> "groovy";
            case "jsr223" -> "jsr223";
            default -> "unknown";
        };
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}
