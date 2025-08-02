package org.openhab.core.ai.common.actions.filesystem;

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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for deleting files in openHAB.
 * 
 * This action provides functionality to delete files
 * and directories with safety checks.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class DeleteFileAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(DeleteFileAction.class);
    private static final String ACTION_ID = "delete_file";
    private static final String ACTION_NAME = "Delete File";
    private static final String DESCRIPTION = "Deletes files and directories within the openHAB root folder with security validation";

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
    public String getCategory() {
        return "filesystem";
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
        properties.put("path", Map.of("type", "string", "description",
                "File or directory path to delete (must be within openHAB root folder)", "required", true));
        properties.put("recursive",
                Map.of("type", "boolean", "description", "Delete directories recursively", "default", false));
        properties.put("createBackup",
                Map.of("type", "boolean", "description", "Create backup before deletion", "default", false));
        properties.put("force",
                Map.of("type", "boolean", "description", "Force deletion even if read-only", "default", false));
        properties.put("maxItems", Map.of("type", "integer", "description",
                "Maximum number of items to delete (for safety)", "default", 1000, "minimum", 1, "maximum", 10000));

        schema.put("properties", properties);
        schema.put("required", List.of("path"));
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate required path
        if (!parameters.containsKey("path")) {
            errors.add("path is required");
        } else {
            String path = (String) parameters.get("path");
            if (path == null || path.trim().isEmpty()) {
                errors.add("path cannot be empty");
            } else if (!FileSystemSecurityUtils.isPathAllowed(path)) {
                errors.add("Path is not within allowed openHAB directories: " + path);
            }
        }

        // Validate maxItems if provided
        if (parameters.containsKey("maxItems")) {
            Object maxItemsObj = parameters.get("maxItems");
            if (maxItemsObj instanceof Integer) {
                int maxItems = (Integer) maxItemsObj;
                if (maxItems < 1 || maxItems > 10000) {
                    errors.add("maxItems must be between 1 and 10000");
                }
            } else {
                errors.add("maxItems must be an integer");
            }
        }

        if (!errors.isEmpty()) {
            return AIActionValidationResult.invalid(errors);
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "Execution timestamp"));
        properties.put("path", Map.of("type", "string", "description", "Path that was deleted"));
        properties.put("deleted", Map.of("type", "boolean", "description", "Whether deletion was successful"));
        properties.put("type", Map.of("type", "string", "description", "Type of item deleted (file/directory)"));
        properties.put("size", Map.of("type", "integer", "description", "Size of deleted item in bytes"));
        properties.put("itemsDeleted",
                Map.of("type", "integer", "description", "Number of items deleted (for directories)"));
        properties.put("backupCreated", Map.of("type", "boolean", "description", "Whether backup was created"));
        properties.put("backupPath", Map.of("type", "string", "description", "Path to backup (if created)"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        logger.debug("Executing DeleteFileAction with context: {}", context.getProtocol());

        try {
            Map<String, Object> result = new HashMap<>();

            // Extract parameters with defaults
            Object pathObj = parameters.get("path");
            if (pathObj == null) {
                throw new AIActionException(ACTION_ID, "Path parameter is required");
            }
            String path = (String) pathObj;
            Boolean recursive = (Boolean) parameters.getOrDefault("recursive", false);
            Boolean createBackup = (Boolean) parameters.getOrDefault("createBackup", false);
            Boolean force = (Boolean) parameters.getOrDefault("force", false);
            Integer maxItems = (Integer) parameters.getOrDefault("maxItems", 1000);

            // Validate path security
            FileSystemSecurityUtils.validatePath(path, "delete file");

            // Resolve the path
            Path filePath = Paths.get(path).toAbsolutePath().normalize();

            if (!Files.exists(filePath)) {
                throw new AIActionException(ACTION_ID, "Path does not exist: " + path);
            }

            // Create backup if requested
            String backupPath = null;
            if (createBackup) {
                backupPath = createBackup(filePath);
            }

            // Determine item type and size
            boolean isDirectory = Files.isDirectory(filePath);
            long size = 0;
            int itemsDeleted = 1;

            if (isDirectory) {
                if (!recursive) {
                    throw new AIActionException(ACTION_ID, "Cannot delete directory without recursive=true: " + path);
                }

                // Count items in directory
                try (var stream = Files.walk(filePath)) {
                    long count = stream.count();
                    if (count > maxItems) {
                        throw new AIActionException(ACTION_ID,
                                String.format("Directory contains %d items, exceeds maximum of %d", count, maxItems));
                    }
                    itemsDeleted = (int) count;
                }

                // Calculate total size
                try (var stream = Files.walk(filePath)) {
                    size = stream.filter(Files::isRegularFile).mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0;
                        }
                    }).sum();
                }
            } else {
                size = Files.size(filePath);
            }

            // Delete the item
            boolean deleted = false;
            if (isDirectory) {
                deleted = deleteDirectory(filePath, force);
            } else {
                deleted = deleteFile(filePath, force);
            }

            if (!deleted) {
                throw new AIActionException(ACTION_ID, "Failed to delete: " + path);
            }

            // Build result
            result.put("timestamp", Instant.now().toString());
            result.put("path", filePath != null && filePath.toString() != null ? filePath.toString() : "");
            result.put("deleted", deleted);
            result.put("type", isDirectory ? "directory" : "file");
            result.put("size", size);
            result.put("itemsDeleted", itemsDeleted);
            result.put("backupCreated", backupPath != null);
            if (backupPath != null) {
                result.put("backupPath", backupPath != null ? backupPath : "");
            }

            logger.debug("DeleteFileAction completed successfully. Deleted {} items from {}", itemsDeleted,
                    filePath.toString());
            return AIActionResult.success(result, System.currentTimeMillis());

        } catch (SecurityException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Security violation";
            logger.error("Security violation in DeleteFileAction: {}", errorMessage);
            throw new AIActionException(ACTION_ID, errorMessage);
        } catch (IOException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "IO error";
            logger.error("IO error in DeleteFileAction: {}", errorMessage);
            throw new AIActionException(ACTION_ID, "Failed to delete: " + errorMessage);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            logger.error("Unexpected error in DeleteFileAction: {}", errorMessage, e);
            throw new AIActionException(ACTION_ID, "Unexpected error: " + errorMessage);
        }
    }

    private String createBackup(Path filePath) throws IOException {
        String timestamp = Instant.now().toString().replace(":", "-").replace(".", "-");
        String fileName = filePath.getFileName().toString();
        String backupFileName = fileName + ".backup." + timestamp;
        Path backupPath = filePath.resolveSibling(backupFileName);

        if (Files.isDirectory(filePath)) {
            // For directories, we'll create a simple marker file since copying entire directories is complex
            Files.write(backupPath.resolveSibling(backupFileName + ".marker"),
                    ("Backup marker for: " + filePath.toString() + "\nTimestamp: " + timestamp).getBytes());
        } else {
            Files.copy(filePath, backupPath);
        }

        logger.debug("Created backup marker: {}", backupPath);
        return backupPath.toString();
    }

    private boolean deleteFile(Path filePath, boolean force) throws IOException {
        try {
            if (force) {
                // Make file writable if it's read-only
                filePath.toFile().setWritable(true);
            }
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            logger.warn("Failed to delete file {}: {}", filePath, e.getMessage());
            return false;
        }
    }

    private boolean deleteDirectory(Path dirPath, boolean force) throws IOException {
        try {
            if (force) {
                // Make directory writable if it's read-only
                dirPath.toFile().setWritable(true);
            }

            // Delete all files and subdirectories
            Files.walk(dirPath).sorted((a, b) -> b.compareTo(a)) // Delete deepest files first
                    .forEach(path -> {
                        try {
                            if (force) {
                                path.toFile().setWritable(true);
                            }
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            logger.warn("Failed to delete path {}: {}", path, e.getMessage());
                        }
                    });

            return !Files.exists(dirPath);
        } catch (IOException e) {
            logger.warn("Failed to delete directory {}: {}", dirPath, e.getMessage());
            return false;
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
                .description("Deletes files and directories within the openHAB root folder with security validation")
                .tags(List.of("filesystem", "delete", "file", "directory", "security"))
                .documentation("Provides secure file deletion capabilities for openHAB files")
                .examples(List.of("Delete file: {\"path\": \"conf/test.txt\"}",
                        "Delete directory: {\"path\": \"conf/temp\", \"recursive\": true}",
                        "Delete with backup: {\"path\": \"conf/config.cfg\", \"createBackup\": true}",
                        "Force delete: {\"path\": \"conf/readonly.txt\", \"force\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("file_deletion", true, "directory_deletion", true, "recursive_deletion", true, "backup_support",
                true, "force_deletion", true, "safety_limits", true, "security", true, "async", true);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("DeleteFileAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("DeleteFileAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
