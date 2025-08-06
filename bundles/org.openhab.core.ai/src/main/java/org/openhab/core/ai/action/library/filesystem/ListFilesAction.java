package org.openhab.core.ai.action.library.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
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
 * Action for listing files in openHAB.
 * 
 * This action provides functionality to list files
 * and directories with various options.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ListFilesAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ListFilesAction.class);
    private static final String ACTION_ID = "list_files";
    private static final String ACTION_NAME = "List Files";
    private static final String DESCRIPTION = "Lists files and directories within the openHAB root folder with security validation";

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
        return DESCRIPTION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("path", Map.of("type", "string", "description",
                "Directory path to list (must be within openHAB root folder)", "default", "."));
        properties.put("recursive",
                Map.of("type", "boolean", "description", "List files recursively", "default", false));
        properties.put("includeHidden",
                Map.of("type", "boolean", "description", "Include hidden files and directories", "default", false));
        properties.put("maxDepth", Map.of("type", "integer", "description", "Maximum depth for recursive listing",
                "default", 10, "minimum", 1, "maximum", 50));
        properties.put("includeDetails", Map.of("type", "boolean", "description",
                "Include detailed file information (size, permissions, timestamps)", "default", true));
        properties.put("fileType", Map.of("type", "string", "enum", List.of("all", "files", "directories"),
                "description", "Type of items to list", "default", "all"));

        schema.put("properties", properties);
        schema.put("required", List.of());
        return schema;
    }

    @Override
    public String getCategory() {
        return "filesystem";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate path if provided
        if (parameters.containsKey("path")) {
            String path = (String) parameters.get("path");
            if (path == null || path.trim().isEmpty()) {
                errors.add("path cannot be empty");
            } else if (!FileSystemSecurityUtils.isPathAllowed(path)) {
                errors.add("Path is not within allowed openHAB directories: " + path);
            }
        }

        // Validate maxDepth if provided
        if (parameters.containsKey("maxDepth")) {
            Object maxDepthObj = parameters.get("maxDepth");
            if (maxDepthObj instanceof Integer) {
                int maxDepth = (Integer) maxDepthObj;
                if (maxDepth < 1 || maxDepth > 50) {
                    errors.add("maxDepth must be between 1 and 50");
                }
            } else {
                errors.add("maxDepth must be an integer");
            }
        }

        // Validate fileType if provided
        if (parameters.containsKey("fileType")) {
            String fileType = (String) parameters.get("fileType");
            if (fileType != null && !List.of("all", "files", "directories").contains(fileType)) {
                errors.add("fileType must be one of: all, files, directories");
            }
        }

        if (!errors.isEmpty()) {
            return ActionValidationResult.invalid(errors);
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "Execution timestamp"));
        properties.put("path", Map.of("type", "string", "description", "Directory path that was listed"));
        properties.put("files", Map.of("type", "array", "description", "List of files and directories"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of items found"));
        properties.put("summary", Map.of("type", "object", "description", "Summary statistics"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        logger.debug("Executing ListFilesAction with context: {}", context.getProtocol());

        try {
            Map<String, Object> result = new HashMap<>();

            // Extract parameters with defaults
            Object pathObj = parameters.get("path");
            String path = pathObj != null ? (String) pathObj : ".";
            Boolean recursive = (Boolean) parameters.getOrDefault("recursive", false);
            Boolean includeHidden = (Boolean) parameters.getOrDefault("includeHidden", false);
            Integer maxDepth = (Integer) parameters.getOrDefault("maxDepth", 10);
            Boolean includeDetails = (Boolean) parameters.getOrDefault("includeDetails", true);
            String fileType = (String) parameters.getOrDefault("fileType", "all");

            // Validate path security
            FileSystemSecurityUtils.validatePath(path, "list files");

            // Resolve the path
            Path directoryPath = Paths.get(path).toAbsolutePath().normalize();

            if (!Files.exists(directoryPath)) {
                throw new ActionException(ACTION_ID, "Directory does not exist: " + path);
            }

            if (!Files.isDirectory(directoryPath)) {
                throw new ActionException(ACTION_ID, "Path is not a directory: " + path);
            }

            // List files
            List<Map<String, Object>> files = listFiles(directoryPath, recursive, includeHidden, maxDepth,
                    includeDetails, fileType);

            // Build result
            result.put("timestamp", Instant.now().toString());
            result.put("path",
                    directoryPath != null && directoryPath.toString() != null ? directoryPath.toString() : "");
            result.put("recursive", recursive);
            result.put("includeHidden", includeHidden);
            result.put("maxDepth", maxDepth);
            result.put("includeDetails", includeDetails);
            result.put("fileType", fileType);
            result.put("files", files);
            result.put("totalCount", files.size());

            // Add summary statistics
            Map<String, Object> summary = new HashMap<>();
            long totalSize = files.stream().filter(file -> (Boolean) file.get("isFile"))
                    .mapToLong(file -> (Long) file.getOrDefault("size", 0L)).sum();

            long fileCount = files.stream().filter(file -> (Boolean) file.get("isFile")).count();

            long directoryCount = files.stream().filter(file -> (Boolean) file.get("isDirectory")).count();

            summary.put("totalSize", totalSize);
            summary.put("fileCount", fileCount);
            summary.put("directoryCount", directoryCount);
            summary.put("totalItems", files.size());
            result.put("summary", summary);

            logger.debug("ListFilesAction completed successfully. Found {} items in {}", files.size(),
                    directoryPath.toString());
            return ActionResult.success(result, System.currentTimeMillis());

        } catch (SecurityException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Security violation";
            logger.error("Security violation in ListFilesAction: {}", errorMessage);
            throw new ActionException(ACTION_ID, errorMessage);
        } catch (IOException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "IO error";
            logger.error("IO error in ListFilesAction: {}", errorMessage);
            throw new ActionException(ACTION_ID, "Failed to list files: " + errorMessage);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            logger.error("Unexpected error in ListFilesAction: {}", errorMessage, e);
            throw new ActionException(ACTION_ID, "Unexpected error: " + errorMessage);
        }
    }

    private List<Map<String, Object>> listFiles(Path directoryPath, boolean recursive, boolean includeHidden,
            int maxDepth, boolean includeDetails, String fileType) throws IOException {

        List<Map<String, Object>> files = new ArrayList<>();

        try (Stream<Path> stream = recursive ? Files.walk(directoryPath, maxDepth) : Files.list(directoryPath)) {

            files = stream.filter(path -> {
                // Skip hidden files if not requested
                if (!includeHidden && isHidden(path)) {
                    return false;
                }

                // Filter by file type
                switch (fileType) {
                    case "files":
                        return Files.isRegularFile(path);
                    case "directories":
                        return Files.isDirectory(path);
                    case "all":
                    default:
                        return true;
                }
            }).map(path -> createFileInfo(path, includeDetails)).collect(Collectors.toList());
        }

        return files;
    }

    private Map<String, Object> createFileInfo(Path filePath, boolean includeDetails) {
        Map<String, Object> fileInfo = new HashMap<>();

        try {
            fileInfo.put("name", filePath.getFileName().toString());
            fileInfo.put("path", filePath.toString());
            fileInfo.put("relativePath", filePath.getFileName().toString());
            fileInfo.put("isDirectory", Files.isDirectory(filePath));
            fileInfo.put("isFile", Files.isRegularFile(filePath));
            fileInfo.put("isHidden", isHidden(filePath));

            if (includeDetails) {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);

                    fileInfo.put("size", attrs.size());
                    fileInfo.put("lastModified", attrs.lastModifiedTime().toString());
                    fileInfo.put("created", attrs.creationTime().toString());
                    fileInfo.put("lastAccessed", attrs.lastAccessTime().toString());
                    fileInfo.put("isReadable", Files.isReadable(filePath));
                    fileInfo.put("isWritable", Files.isWritable(filePath));
                    fileInfo.put("isExecutable", Files.isExecutable(filePath));

                    // Try to get POSIX permissions
                    try {
                        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(filePath);
                        fileInfo.put("permissions", PosixFilePermissions.toString(permissions));
                    } catch (UnsupportedOperationException e) {
                        fileInfo.put("permissions", "Not available");
                    }
                } catch (IOException e) {
                    logger.warn("Could not read file attributes for {}: {}", filePath, e.getMessage());
                    fileInfo.put("error", "Could not read file attributes: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warn("Error creating file info for {}: {}", filePath, e.getMessage());
            fileInfo.put("error", "Error reading file: " + e.getMessage());
        }

        return fileInfo;
    }

    private boolean isHidden(Path path) {
        try {
            return Files.isHidden(path) || path.getFileName().toString().startsWith(".");
        } catch (IOException e) {
            return path.getFileName().toString().startsWith(".");
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
        return ActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Lists files and directories within the openHAB root folder with security validation")
                .tags(List.of("filesystem", "list", "files", "directories", "security"))
                .documentation("Provides secure file system listing capabilities for openHAB directories")
                .examples(List.of("List current directory: {\"path\": \".\"}",
                        "List recursively: {\"path\": \".\", \"recursive\": true}",
                        "List only files: {\"path\": \".\", \"fileType\": \"files\"}",
                        "List with details: {\"path\": \".\", \"includeDetails\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("recursive", true, "filtering", true, "security", true, "details", true, "async", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("ListFilesAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ListFilesAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
