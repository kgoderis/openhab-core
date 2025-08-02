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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Action for copying files and directories within the openHAB root folder.
 * 
 * This action provides secure file copying capabilities, ensuring operations
 * only work within the openHAB configuration and user data directories.
 * 
 * @author Karel Goderis
 */
@NonNullByDefault
public class CopyFileAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(CopyFileAction.class);
    private static final String ACTION_ID = "copy_file";
    private static final String ACTION_NAME = "Copy File";
    private static final String DESCRIPTION = "Copies files and directories within the openHAB root folder with security validation";

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
        properties.put("recursive",
                Map.of("type", "boolean", "description", "Copy directories recursively", "default", true));
        properties.put("overwrite",
                Map.of("type", "boolean", "description", "Overwrite existing files", "default", false));
        properties.put("preserveAttributes", Map.of("type", "boolean", "description",
                "Preserve file attributes (timestamps, permissions)", "default", true));
        properties.put("createBackup", Map.of("type", "boolean", "description",
                "Create backup of existing files before overwriting", "default", false));
        properties.put("maxFileSize", Map.of("type", "integer", "description", "Maximum file size to copy in bytes",
                "default", DEFAULT_MAX_FILE_SIZE, "minimum", 1, "maximum", 1024 * 1024 * 1024 // 1GB
        ));
        properties.put("maxItems", Map.of("type", "integer", "description", "Maximum number of items to copy",
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
        properties.put("itemsCopied", Map.of("type", "integer"));
        properties.put("bytesCopied", Map.of("type", "integer"));
        properties.put("backupCreated", Map.of("type", "boolean"));
        properties.put("backupPath", Map.of("type", "string"));
        properties.put("executionTime", Map.of("type", "number"));

        schema.put("properties", properties);
        schema.put("required", List.of("success", "message", "source", "destination", "itemsCopied", "bytesCopied"));

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
            boolean recursive = (Boolean) parameters.getOrDefault("recursive", true);
            boolean overwrite = (Boolean) parameters.getOrDefault("overwrite", false);
            boolean preserveAttributes = (Boolean) parameters.getOrDefault("preserveAttributes", true);
            boolean createBackup = (Boolean) parameters.getOrDefault("createBackup", false);
            long maxFileSize = ((Number) parameters.getOrDefault("maxFileSize", DEFAULT_MAX_FILE_SIZE)).longValue();
            int maxItems = ((Number) parameters.getOrDefault("maxItems", DEFAULT_MAX_ITEMS)).intValue();

            // Validate paths
            FileSystemSecurityUtils.validatePath(source, "copy source");
            FileSystemSecurityUtils.validatePath(destination, "copy destination");

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

            // Perform the copy operation
            CopyResult result = copyPath(sourcePath, destPath, recursive, overwrite, preserveAttributes, maxFileSize,
                    maxItems);

            // Build response
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("success", true);
            resultData.put("message", "File copy completed successfully");
            resultData.put("source", source);
            resultData.put("destination", destination);
            resultData.put("itemsCopied", result.itemsCopied);
            resultData.put("bytesCopied", result.bytesCopied);
            resultData.put("backupCreated", backupPath != null);
            resultData.put("backupPath", backupPath);
            resultData.put("executionTime", (System.currentTimeMillis() - startTime) / 1000.0);

            logger.info("File copy completed: {} -> {} ({} items, {} bytes)", source, destination, result.itemsCopied,
                    result.bytesCopied);

            return AIActionResult.success(resultData, System.currentTimeMillis() - startTime);

        } catch (SecurityException e) {
            logger.error("Security violation during file copy: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "Security violation: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during file copy: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "IO error during file copy: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during file copy: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "Unexpected error during file copy: " + e.getMessage());
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

    private CopyResult copyPath(Path source, Path destination, boolean recursive, boolean overwrite,
            boolean preserveAttributes, long maxFileSize, int maxItems) throws IOException {

        CopyResult result = new CopyResult();

        if (Files.isDirectory(source)) {
            if (!recursive) {
                throw new IOException("Source is a directory but recursive copy is disabled");
            }

            // Create destination directory
            Files.createDirectories(destination);

            // Copy directory contents
            Files.walk(source).forEach(sourcePath -> {
                try {
                    if (result.itemsCopied >= maxItems) {
                        throw new RuntimeException("Maximum items limit reached: " + maxItems);
                    }

                    Path relativePath = source.relativize(sourcePath);
                    Path targetPath = destination.resolve(relativePath);

                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath);
                    } else {
                        // Check file size
                        long fileSize = Files.size(sourcePath);
                        if (fileSize > maxFileSize) {
                            throw new IOException(
                                    "File size exceeds limit: " + sourcePath + " (" + fileSize + " bytes)");
                        }

                        // Create parent directories
                        Files.createDirectories(targetPath.getParent());

                        // Copy file
                        if (overwrite || !Files.exists(targetPath)) {
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            result.itemsCopied++;
                            result.bytesCopied += fileSize;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error copying file: " + sourcePath, e);
                }
            });

        } else {
            // Copy single file
            Path finalDestination = destination;
            if (Files.exists(destination) && Files.isDirectory(destination)) {
                finalDestination = destination.resolve(source.getFileName());
            }

            // Check file size
            long fileSize = Files.size(source);
            if (fileSize > maxFileSize) {
                throw new IOException("File size exceeds limit: " + source + " (" + fileSize + " bytes)");
            }

            // Create parent directories
            Files.createDirectories(finalDestination.getParent());

            // Copy file
            if (overwrite || !Files.exists(finalDestination)) {
                Files.copy(source, finalDestination, StandardCopyOption.REPLACE_EXISTING);
                result.itemsCopied++;
                result.bytesCopied += fileSize;
            }
        }

        return result;
    }

    private static class CopyResult {
        int itemsCopied = 0;
        long bytesCopied = 0;
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
        capabilities.put("supportedOperations", List.of("copy_file", "copy_directory", "recursive_copy"));
        return capabilities;
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("Initializing CopyFileAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up CopyFileAction");
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
