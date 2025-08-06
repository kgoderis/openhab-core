package org.openhab.core.ai.action.library.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to search for files and directories within the openHAB root folder.
 * Only operates within the openHAB root folder for security.
 */
@NonNullByDefault
public class SearchFilesAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SearchFilesAction.class);

    private static final String ACTION_ID = "filesystem.search_files";
    private static final String ACTION_NAME = "Search Files";
    private static final String DESCRIPTION = "Search for files and directories using various criteria";
    private static final String VERSION = "1.0.0";

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return "filesystem";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();

        Map<String, Object> rootPathParam = new HashMap<>();
        rootPathParam.put("type", "string");
        rootPathParam.put("description", "Root directory to search in");
        rootPathParam.put("required", false);
        rootPathParam.put("default", ".");
        schema.put("rootPath", rootPathParam);

        Map<String, Object> namePatternParam = new HashMap<>();
        namePatternParam.put("type", "string");
        namePatternParam.put("description", "File name pattern (supports glob patterns like *.txt)");
        namePatternParam.put("required", false);
        schema.put("namePattern", namePatternParam);

        Map<String, Object> regexPatternParam = new HashMap<>();
        regexPatternParam.put("type", "string");
        regexPatternParam.put("description", "Regular expression pattern for file names");
        regexPatternParam.put("required", false);
        schema.put("regexPattern", regexPatternParam);

        Map<String, Object> fileTypeParam = new HashMap<>();
        fileTypeParam.put("type", "string");
        fileTypeParam.put("enum", List.of("all", "files", "directories"));
        fileTypeParam.put("description", "Type of items to search for");
        fileTypeParam.put("required", false);
        fileTypeParam.put("default", "all");
        schema.put("fileType", fileTypeParam);

        Map<String, Object> maxDepthParam = new HashMap<>();
        maxDepthParam.put("type", "integer");
        maxDepthParam.put("description", "Maximum search depth");
        maxDepthParam.put("required", false);
        maxDepthParam.put("default", 10);
        maxDepthParam.put("minimum", 1);
        maxDepthParam.put("maximum", 50);
        schema.put("maxDepth", maxDepthParam);

        Map<String, Object> includeHiddenParam = new HashMap<>();
        includeHiddenParam.put("type", "boolean");
        includeHiddenParam.put("description", "Include hidden files and directories");
        includeHiddenParam.put("required", false);
        includeHiddenParam.put("default", false);
        schema.put("includeHidden", includeHiddenParam);

        Map<String, Object> minSizeParam = new HashMap<>();
        minSizeParam.put("type", "integer");
        minSizeParam.put("description", "Minimum file size in bytes");
        minSizeParam.put("required", false);
        minSizeParam.put("minimum", 0);
        schema.put("minSize", minSizeParam);

        Map<String, Object> maxSizeParam = new HashMap<>();
        maxSizeParam.put("type", "integer");
        maxSizeParam.put("description", "Maximum file size in bytes");
        maxSizeParam.put("required", false);
        maxSizeParam.put("minimum", 0);
        schema.put("maxSize", maxSizeParam);

        Map<String, Object> maxResultsParam = new HashMap<>();
        maxResultsParam.put("type", "integer");
        maxResultsParam.put("description", "Maximum number of results to return");
        maxResultsParam.put("required", false);
        maxResultsParam.put("default", 100);
        maxResultsParam.put("minimum", 1);
        maxResultsParam.put("maximum", 1000);
        schema.put("maxResults", maxResultsParam);

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();

        Map<String, Object> searchResults = new HashMap<>();
        searchResults.put("type", "object");
        searchResults.put("description", "Search results");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> resultsProp = new HashMap<>();
        resultsProp.put("type", "array");
        resultsProp.put("description", "List of matching files and directories");
        properties.put("results", resultsProp);

        Map<String, Object> totalCountProp = new HashMap<>();
        totalCountProp.put("type", "integer");
        totalCountProp.put("description", "Total number of matches found");
        properties.put("totalCount", totalCountProp);

        Map<String, Object> searchCriteriaProp = new HashMap<>();
        searchCriteriaProp.put("type", "object");
        searchCriteriaProp.put("description", "Search criteria used");
        properties.put("searchCriteria", searchCriteriaProp);

        Map<String, Object> summaryProp = new HashMap<>();
        summaryProp.put("type", "object");
        summaryProp.put("description", "Summary statistics");
        properties.put("summary", summaryProp);

        searchResults.put("properties", properties);
        schema.put("searchResults", searchResults);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("filesystem.read", true);
        capabilities.put("search", true);
        capabilities.put("pattern_matching", true);
        return capabilities;
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().description(DESCRIPTION).version(getVersion()).build();
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate rootPath
        if (parameters.containsKey("rootPath")) {
            String rootPath = (String) parameters.get("rootPath");
            if (rootPath != null && !rootPath.trim().isEmpty()) {
                try {
                    if (!FileSystemSecurityUtils.isPathAllowed(rootPath)) {
                        errors.add("Root path is not allowed: " + rootPath);
                    }
                } catch (Exception e) {
                    errors.add("Invalid root path format: " + rootPath);
                }
            }
        }

        // Validate regex pattern if provided
        if (parameters.containsKey("regexPattern")) {
            String regexPattern = (String) parameters.get("regexPattern");
            if (regexPattern != null && !regexPattern.trim().isEmpty()) {
                try {
                    Pattern.compile(regexPattern);
                } catch (Exception e) {
                    errors.add("Invalid regular expression pattern: " + regexPattern);
                }
            }
        }

        // Validate numeric parameters
        if (parameters.containsKey("maxDepth")) {
            Object maxDepth = parameters.get("maxDepth");
            if (!(maxDepth instanceof Integer) || (Integer) maxDepth < 1 || (Integer) maxDepth > 50) {
                errors.add("maxDepth must be an integer between 1 and 50");
            }
        }

        if (parameters.containsKey("minSize")) {
            Object minSize = parameters.get("minSize");
            if (!(minSize instanceof Integer) || (Integer) minSize < 0) {
                errors.add("minSize must be a non-negative integer");
            }
        }

        if (parameters.containsKey("maxSize")) {
            Object maxSize = parameters.get("maxSize");
            if (!(maxSize instanceof Integer) || (Integer) maxSize < 0) {
                errors.add("maxSize must be a non-negative integer");
            }
        }

        if (parameters.containsKey("maxResults")) {
            Object maxResults = parameters.get("maxResults");
            if (!(maxResults instanceof Integer) || (Integer) maxResults < 1 || (Integer) maxResults > 1000) {
                errors.add("maxResults must be an integer between 1 and 1000");
            }
        }

        // Validate fileType
        if (parameters.containsKey("fileType")) {
            String fileType = (String) parameters.get("fileType");
            if (fileType != null && !List.of("all", "files", "directories").contains(fileType)) {
                errors.add("fileType must be one of: all, files, directories");
            }
        }

        if (errors.isEmpty()) {
            return ActionValidationResult.valid(parameters);
        } else {
            return ActionValidationResult.invalid(errors);
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            // Validate parameters
            ActionValidationResult validation = validateParameters(parameters);
            if (!validation.isValid()) {
                throw new ActionException(ACTION_ID,
                        "Parameter validation failed: " + String.join(", ", validation.getErrors()));
            }

            // Extract parameters with defaults
            String rootPath = (String) parameters.getOrDefault("rootPath", ".");
            String namePattern = (String) parameters.get("namePattern");
            String regexPattern = (String) parameters.get("regexPattern");
            String fileType = (String) parameters.getOrDefault("fileType", "all");
            Integer maxDepth = (Integer) parameters.getOrDefault("maxDepth", 10);
            Boolean includeHidden = (Boolean) parameters.getOrDefault("includeHidden", false);
            Integer minSize = (Integer) parameters.get("minSize");
            Integer maxSize = (Integer) parameters.get("maxSize");
            Integer maxResults = (Integer) parameters.getOrDefault("maxResults", 100);

            // Security check
            if (!FileSystemSecurityUtils.isPathAllowed(rootPath)) {
                throw new ActionException(ACTION_ID, "Security violation: Root path not allowed: " + rootPath);
            }

            Path rootDir = Paths.get(rootPath).toAbsolutePath().normalize();

            if (!Files.exists(rootDir)) {
                throw new ActionException(ACTION_ID, "Root directory does not exist: " + rootPath);
            }

            if (!Files.isDirectory(rootDir)) {
                throw new ActionException(ACTION_ID, "Root path is not a directory: " + rootPath);
            }

            // Compile regex pattern if provided
            Pattern compiledRegex = null;
            if (regexPattern != null && !regexPattern.trim().isEmpty()) {
                compiledRegex = Pattern.compile(regexPattern);
            }

            // Perform search
            List<Map<String, Object>> results = new ArrayList<>();
            int totalFound = 0;

            try (Stream<Path> stream = Files.walk(rootDir, maxDepth)) {
                for (Path path : (Iterable<Path>) stream::iterator) {
                    if (totalFound >= maxResults) {
                        break;
                    }

                    // Skip hidden files if not requested
                    if (!includeHidden && isHidden(path)) {
                        continue;
                    }

                    // Filter by file type
                    if (!matchesFileType(path, fileType)) {
                        continue;
                    }

                    // Filter by name pattern
                    if (namePattern != null && !matchesNamePattern(path, namePattern)) {
                        continue;
                    }

                    // Filter by regex pattern
                    if (compiledRegex != null && !compiledRegex.matcher(path.getFileName().toString()).matches()) {
                        continue;
                    }

                    // Filter by size
                    if (!matchesSizeCriteria(path, minSize, maxSize)) {
                        continue;
                    }

                    // Add to results
                    Map<String, Object> fileInfo = createFileInfo(path);
                    results.add(fileInfo);
                    totalFound++;
                }
            }

            // Build result
            Map<String, Object> resultData = new HashMap<>();
            Map<String, Object> searchResults = new HashMap<>();

            searchResults.put("results", results);
            searchResults.put("totalCount", totalFound);

            // Search criteria
            Map<String, Object> searchCriteria = new HashMap<>();
            searchCriteria.put("rootPath", rootPath);
            searchCriteria.put("namePattern", namePattern);
            searchCriteria.put("regexPattern", regexPattern);
            searchCriteria.put("fileType", fileType);
            searchCriteria.put("maxDepth", maxDepth);
            searchCriteria.put("includeHidden", includeHidden);
            searchCriteria.put("minSize", minSize);
            searchCriteria.put("maxSize", maxSize);
            searchCriteria.put("maxResults", maxResults);
            searchResults.put("searchCriteria", searchCriteria);

            // Summary statistics
            Map<String, Object> summary = new HashMap<>();
            long totalSize = results.stream().filter(file -> (Boolean) file.get("isFile"))
                    .mapToLong(file -> (Long) file.getOrDefault("size", 0L)).sum();

            long fileCount = results.stream().filter(file -> (Boolean) file.get("isFile")).count();

            long directoryCount = results.stream().filter(file -> (Boolean) file.get("isDirectory")).count();

            summary.put("totalSize", totalSize);
            summary.put("fileCount", fileCount);
            summary.put("directoryCount", directoryCount);
            summary.put("totalItems", results.size());
            searchResults.put("summary", summary);

            resultData.put("searchResults", searchResults);

            logger.debug("Search completed. Found {} items in {}", totalFound, rootPath);
            return ActionResult.success(resultData, System.currentTimeMillis() - startTime);

        } catch (SecurityException e) {
            logger.error("Security violation during file search: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "Security violation: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during file search: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "IO error during file search: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during file search: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "Unexpected error during file search: " + e.getMessage());
        }
    }

    private boolean isHidden(Path path) {
        try {
            return Files.isHidden(path) || path.getFileName().toString().startsWith(".");
        } catch (IOException e) {
            return path.getFileName().toString().startsWith(".");
        }
    }

    private boolean matchesFileType(Path path, String fileType) {
        switch (fileType) {
            case "files":
                return Files.isRegularFile(path);
            case "directories":
                return Files.isDirectory(path);
            case "all":
            default:
                return true;
        }
    }

    private boolean matchesNamePattern(Path path, String namePattern) {
        String fileName = path.getFileName().toString();
        return fileName.matches(namePattern.replace("*", ".*").replace("?", "."));
    }

    private boolean matchesSizeCriteria(Path path, Integer minSize, Integer maxSize) {
        if (!Files.isRegularFile(path)) {
            return true; // Size criteria only apply to files
        }

        try {
            long size = Files.size(path);

            if (minSize != null && size < minSize) {
                return false;
            }

            if (maxSize != null && size > maxSize) {
                return false;
            }

            return true;
        } catch (IOException e) {
            logger.warn("Could not get size for {}: {}", path, e.getMessage());
            return false;
        }
    }

    private Map<String, Object> createFileInfo(Path path) {
        Map<String, Object> fileInfo = new HashMap<>();

        try {
            fileInfo.put("name", path.getFileName().toString());
            fileInfo.put("path", path.toString());
            fileInfo.put("relativePath", path.getFileName().toString());
            fileInfo.put("isDirectory", Files.isDirectory(path));
            fileInfo.put("isFile", Files.isRegularFile(path));
            fileInfo.put("isHidden", isHidden(path));

            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            fileInfo.put("size", attrs.size());
            fileInfo.put("lastModified", attrs.lastModifiedTime().toString());
            fileInfo.put("created", attrs.creationTime().toString());
            fileInfo.put("lastAccessed", attrs.lastAccessTime().toString());
            fileInfo.put("isReadable", Files.isReadable(path));
            fileInfo.put("isWritable", Files.isWritable(path));
            fileInfo.put("isExecutable", Files.isExecutable(path));

        } catch (IOException e) {
            logger.warn("Could not read file attributes for {}: {}", path, e.getMessage());
            fileInfo.put("error", "Could not read file attributes: " + e.getMessage());
        }

        return fileInfo;
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
    public void initialize(ActionContext context) {
        // No initialization required
    }

    @Override
    public void cleanup() {
        // No cleanup required
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
