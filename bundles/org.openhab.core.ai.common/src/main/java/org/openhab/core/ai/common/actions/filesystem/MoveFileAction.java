package org.openhab.core.ai.common.actions.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Action for moving files and directories within the openHAB root folder.
 * 
 * This action provides secure file moving capabilities, ensuring operations
 * only work within the openHAB configuration and user data directories.
 * 
 * @author Karel Goderis
 */
public class MoveFileAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(MoveFileAction.class);
    private static final String ACTION_ID = "move_file";
    private static final String ACTION_NAME = "Move File";
    private static final String DESCRIPTION = "Moves files and directories within the openHAB root folder with security validation";

    // Default limits
    private static final long DEFAULT_MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final int DEFAULT_MAX_ITEMS = 1000;

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
        properties.put("source", Map.of("type", "string", "description",
                "Source file or directory path (must be within openHAB root folder)"));
        properties.put("destination",
                Map.of("type", "string", "description", "Destination path (must be within openHAB root folder)"));
        properties.put("atomic", Map.of("type", "boolean", "description",
                "Use atomic move operation (fails if destination exists)", "default", false));
        properties.put("overwrite",
                Map.of("type", "boolean", "description", "Overwrite existing files", "default", false));
        properties.put("createBackup", Map.of("type", "boolean", "description",
                "Create backup of existing files before overwriting", "default", false));
        properties.put("maxFileSize", Map.of("type", "integer", "description", "Maximum file size to move in bytes",
                "default", DEFAULT_MAX_FILE_SIZE, "minimum", 1, "maximum", 1024 * 1024 * 1024 // 1GB
        ));
        properties.put("maxItems", Map.of("type", "integer", "description", "Maximum number of items to move",
                "default", DEFAULT_MAX_ITEMS, "minimum", 1, "maximum", 10000));

        schema.put("properties", properties);
        schema.put("required", List.of("source", "destination"));

        return schema;
    }

    @Override
    public String getCategory() {
        return "File System";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        try {
            // Validate required parameters
            if (!parameters.containsKey("source")) {
                errors.add("Missing required parameter: source");
            }
            if (!parameters.containsKey("destination")) {
                errors.add("Missing required parameter: destination");
            }

            String source = (String) parameters.get("source");
            String destination = (String) parameters.get("destination");

            // Validate source path
            if (source == null || source.trim().isEmpty()) {
                errors.add("Source path cannot be null or empty");
            }

            // Validate destination path
            if (destination == null || destination.trim().isEmpty()) {
                errors.add("Destination path cannot be null or empty");
            }

            // Validate paths are within allowed directories
            if (source != null && !source.trim().isEmpty() && !FileSystemSecurityUtils.isPathAllowed(source)) {
                errors.add("Source path is not within allowed openHAB directories: " + source);
            }
            if (destination != null && !destination.trim().isEmpty()
                    && !FileSystemSecurityUtils.isPathAllowed(destination)) {
                errors.add("Destination path is not within allowed openHAB directories: " + destination);
            }

            // Validate numeric parameters
            if (parameters.containsKey("maxFileSize")) {
                Object maxFileSize = parameters.get("maxFileSize");
                if (!(maxFileSize instanceof Number) || ((Number) maxFileSize).longValue() <= 0) {
                    errors.add("maxFileSize must be a positive number");
                }
            }

            if (parameters.containsKey("maxItems")) {
                Object maxItems = parameters.get("maxItems");
                if (!(maxItems instanceof Number) || ((Number) maxItems).intValue() <= 0) {
                    errors.add("maxItems must be a positive number");
                }
            }

        } catch (Exception e) {
            logger.error("Error validating parameters: {}", e.getMessage());
            errors.add("Parameter validation failed: " + e.getMessage());
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
        properties.put("success", Map.of("type", "boolean"));
        properties.put("message", Map.of("type", "string"));
        properties.put("source", Map.of("type", "string"));
        properties.put("destination", Map.of("type", "string"));
        properties.put("itemsMoved", Map.of("type", "integer"));
        properties.put("bytesMoved", Map.of("type", "integer"));
        properties.put("backupCreated", Map.of("type", "boolean"));
        properties.put("backupPath", Map.of("type", "string"));
        properties.put("executionTime", Map.of("type", "number"));

        schema.put("properties", properties);
        schema.put("required", List.of("success", "message", "source", "destination", "itemsMoved", "bytesMoved"));

        return schema;
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();

        try {
            // Validate parameters
            AIActionValidationResult validation = validateParameters(parameters);
            if (!validation.isValid()) {
                throw new AIActionException(ACTION_ID,
                        "Parameter validation failed: " + String.join(", ", validation.getErrors()));
            }

            // Extract parameters
            String source = (String) parameters.get("source");
            String destination = (String) parameters.get("destination");
            boolean atomic = (Boolean) parameters.getOrDefault("atomic", false);
            boolean overwrite = (Boolean) parameters.getOrDefault("overwrite", false);
            boolean createBackup = (Boolean) parameters.getOrDefault("createBackup", false);
            long maxFileSize = ((Number) parameters.getOrDefault("maxFileSize", DEFAULT_MAX_FILE_SIZE)).longValue();
            int maxItems = ((Number) parameters.getOrDefault("maxItems", DEFAULT_MAX_ITEMS)).intValue();

            // Validate paths
            FileSystemSecurityUtils.validatePath(source, "move source");
            FileSystemSecurityUtils.validatePath(destination, "move destination");

            Path sourcePath = Paths.get(source);
            Path destPath = Paths.get(destination);

            // Check if source exists
            if (!Files.exists(sourcePath)) {
                throw new AIActionException(ACTION_ID, "Source path does not exist: " + source);
            }

            // Check if destination already exists and handle accordingly
            String backupPath = null;
            if (Files.exists(destPath) && createBackup) {
                backupPath = createBackup(destPath);
            }

            // Perform the move operation
            MoveResult result = movePath(sourcePath, destPath, atomic, overwrite, maxFileSize, maxItems);

            // Build response
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("success", true);
            resultData.put("message", "File move completed successfully");
            resultData.put("source", source);
            resultData.put("destination", destination);
            resultData.put("itemsMoved", result.itemsMoved);
            resultData.put("bytesMoved", result.bytesMoved);
            resultData.put("backupCreated", backupPath != null);
            resultData.put("backupPath", backupPath);
            resultData.put("executionTime", (System.currentTimeMillis() - startTime) / 1000.0);

            logger.info("File move completed: {} -> {} ({} items, {} bytes)", source, destination, result.itemsMoved,
                    result.bytesMoved);

            return AIActionResult.success(resultData, System.currentTimeMillis() - startTime);

        } catch (SecurityException e) {
            logger.error("Security violation during file move: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "Security violation: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during file move: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "IO error during file move: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during file move: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "Unexpected error during file move: " + e.getMessage());
        }
    }

    private String createBackup(Path originalPath) throws IOException {
        String timestamp = Instant.now().toString().replace(":", "-");
        String backupName = originalPath.getFileName().toString() + ".backup." + timestamp;
        Path backupPath = originalPath.resolveSibling(backupName);

        if (Files.isDirectory(originalPath)) {
            Files.walk(originalPath).forEach(source -> {
                try {
                    Path target = backupPath.resolve(originalPath.relativize(source));
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    logger.warn("Failed to create backup of: {}", source, e);
                }
            });
        } else {
            Files.createDirectories(backupPath.getParent());
            Files.copy(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return backupPath.toString();
    }

    private MoveResult movePath(Path source, Path destination, boolean atomic, boolean overwrite, long maxFileSize,
            int maxItems) throws IOException {

        MoveResult result = new MoveResult();

        // Check if destination exists and handle accordingly
        if (Files.exists(destination)) {
            if (atomic) {
                throw new IOException("Atomic move failed: destination already exists: " + destination);
            }
            if (!overwrite) {
                throw new IOException("Destination exists and overwrite is disabled: " + destination);
            }
        }

        // Calculate total size and item count for validation
        if (Files.isDirectory(source)) {
            long totalSize = 0;
            int itemCount = 0;

            for (Path file : Files.walk(source).toList()) {
                if (!Files.isDirectory(file)) {
                    long fileSize = Files.size(file);
                    if (fileSize > maxFileSize) {
                        throw new IOException("File size exceeds limit: " + file + " (" + fileSize + " bytes)");
                    }
                    totalSize += fileSize;
                    itemCount++;

                    if (itemCount > maxItems) {
                        throw new IOException("Maximum items limit reached: " + maxItems);
                    }
                }
            }

            result.bytesMoved = totalSize;
            result.itemsMoved = itemCount;
        } else {
            // Single file move
            long fileSize = Files.size(source);
            if (fileSize > maxFileSize) {
                throw new IOException("File size exceeds limit: " + source + " (" + fileSize + " bytes)");
            }

            result.bytesMoved = fileSize;
            result.itemsMoved = 1;
        }

        // Create parent directories for destination
        Files.createDirectories(destination.getParent());

        // Perform the move operation
        if (atomic) {
            Files.move(source, destination);
        } else {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        return result;
    }

    private static class MoveResult {
        int itemsMoved = 0;
        long bytesMoved = 0;
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
        return AIActionMetadata.builder().description(DESCRIPTION).version(getVersion()).author("openHAB").build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsStreaming", false);
        capabilities.put("maxFileSize", DEFAULT_MAX_FILE_SIZE);
        capabilities.put("maxItems", DEFAULT_MAX_ITEMS);
        capabilities.put("supportedOperations", List.of("move_file", "move_directory", "atomic_move"));
        return capabilities;
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("Initializing MoveFileAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up MoveFileAction");
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
