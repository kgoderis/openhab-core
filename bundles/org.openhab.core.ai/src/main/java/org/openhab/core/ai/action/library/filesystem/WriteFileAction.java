package org.openhab.core.ai.action.library.filesystem;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Action for writing file contents within the openHAB root folder.
 * 
 * This action provides secure file writing capabilities, ensuring operations
 * only work within the openHAB configuration and user data directories.
 * 
 * 
 */
@NonNullByDefault
public class WriteFileAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(WriteFileAction.class);
    private static final String ACTION_ID = "write_file";
    private static final String ACTION_NAME = "Write File";
    private static final String DESCRIPTION = "Writes file contents within the openHAB root folder with security validation";

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
                "File path to write (must be within openHAB root folder)", "required", true));
        properties.put("content",
                Map.of("type", "string", "description", "Content to write to the file", "required", true));
        properties.put("encoding", Map.of("type", "string", "description", "File encoding (default: UTF-8)", "default",
                "UTF-8", "enum", List.of("UTF-8", "ISO-8859-1", "US-ASCII", "UTF-16", "UTF-16BE", "UTF-16LE")));
        properties.put("mode", Map.of("type", "string", "description", "Write mode: overwrite, append, or create",
                "default", "overwrite", "enum", List.of("overwrite", "append", "create")));
        properties.put("createBackup", Map.of("type", "boolean", "description",
                "Create backup of existing file before writing", "default", false));
        properties.put("createDirectories", Map.of("type", "boolean", "description",
                "Create parent directories if they don't exist", "default", true));
        properties.put("maxSize",
                Map.of("type", "integer", "description", "Maximum content size in bytes (default: 10MB)", "default",
                        10485760, "minimum", 1, "maximum", 104857600));

        schema.put("properties", properties);
        schema.put("required", List.of("path", "content"));
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
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

        // Validate required content
        if (!parameters.containsKey("content")) {
            errors.add("content is required");
        } else {
            String content = (String) parameters.get("content");
            if (content == null) {
                errors.add("content cannot be null");
            }
        }

        // Validate encoding if provided
        if (parameters.containsKey("encoding")) {
            String encoding = (String) parameters.get("encoding");
            if (encoding != null && !List.of("UTF-8", "ISO-8859-1", "US-ASCII", "UTF-16", "UTF-16BE", "UTF-16LE")
                    .contains(encoding)) {
                errors.add("Invalid encoding: " + encoding);
            }
        }

        // Validate mode if provided
        if (parameters.containsKey("mode")) {
            String mode = (String) parameters.get("mode");
            if (mode != null && !List.of("overwrite", "append", "create").contains(mode)) {
                errors.add("Invalid mode: " + mode + ". Must be one of: overwrite, append, create");
            }
        }

        // Validate maxSize if provided
        if (parameters.containsKey("maxSize")) {
            Object maxSizeObj = parameters.get("maxSize");
            if (maxSizeObj instanceof Integer) {
                int maxSize = (Integer) maxSizeObj;
                if (maxSize < 1 || maxSize > 104857600) {
                    errors.add("maxSize must be between 1 and 104857600 bytes");
                }
            } else {
                errors.add("maxSize must be an integer");
            }
        }

        // Validate content size
        if (parameters.containsKey("content")) {
            String content = (String) parameters.get("content");
            if (content != null) {
                int maxSize = (Integer) parameters.getOrDefault("maxSize", 10485760);
                if (content.getBytes().length > maxSize) {
                    errors.add("Content size exceeds maximum allowed size of " + maxSize + " bytes");
                }
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
        properties.put("path", Map.of("type", "string", "description", "File path that was written"));
        properties.put("size", Map.of("type", "integer", "description", "Number of bytes written"));
        properties.put("encoding", Map.of("type", "string", "description", "Encoding used"));
        properties.put("mode", Map.of("type", "string", "description", "Write mode used"));
        properties.put("backupCreated", Map.of("type", "boolean", "description", "Whether backup was created"));
        properties.put("backupPath", Map.of("type", "string", "description", "Path to backup file (if created)"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        logger.debug("Executing WriteFileAction with context: {}", context.getProtocol());

        try {
            Map<String, Object> result = new HashMap<>();

            // Extract parameters with defaults
            Object pathObj = parameters.get("path");
            if (pathObj == null) {
                throw new ActionException(ACTION_ID, "Path parameter is required");
            }
            String path = (String) pathObj;
            String content = (String) parameters.get("content");
            String encoding = (String) parameters.getOrDefault("encoding", "UTF-8");
            String mode = (String) parameters.getOrDefault("mode", "overwrite");
            Boolean createBackup = (Boolean) parameters.getOrDefault("createBackup", false);
            Boolean createDirectories = (Boolean) parameters.getOrDefault("createDirectories", true);
            Integer maxSize = (Integer) parameters.getOrDefault("maxSize", 10485760);

            // Validate path security
            FileSystemSecurityUtils.validatePath(path, "write file");

            // Resolve the path
            Path filePath = Paths.get(path).toAbsolutePath().normalize();

            // Check if file exists and handle mode
            boolean fileExists = Files.exists(filePath);

            if (mode.equals("create") && fileExists) {
                throw new ActionException(ACTION_ID, "File already exists and mode is 'create': " + path);
            }

            if (mode.equals("append") && !fileExists) {
                throw new ActionException(ACTION_ID, "File does not exist and mode is 'append': " + path);
            }

            // Create backup if requested and file exists
            String backupPath = null;
            if (createBackup && fileExists) {
                backupPath = createBackup(filePath);
            }

            // Create parent directories if requested
            if (createDirectories) {
                Path parentDir = filePath.getParent();
                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                    logger.debug("Created parent directories for: {}", filePath);
                }
            }

            // Write content to file
            Charset charset = Charset.forName(encoding);
            byte[] contentBytes = content.getBytes(charset);

            if (contentBytes.length > maxSize) {
                throw new ActionException(ACTION_ID,
                        String.format("Content size (%d bytes) exceeds maximum allowed size (%d bytes)",
                                contentBytes.length, maxSize));
            }

            int bytesWritten;
            switch (mode) {
                case "overwrite":
                    Files.write(filePath, contentBytes);
                    bytesWritten = contentBytes.length;
                    break;
                case "append":
                    Files.write(filePath, contentBytes, StandardOpenOption.APPEND);
                    bytesWritten = contentBytes.length;
                    break;
                case "create":
                    Files.write(filePath, contentBytes);
                    bytesWritten = contentBytes.length;
                    break;
                default:
                    throw new ActionException(ACTION_ID, "Invalid write mode: " + mode);
            }

            // Build result
            result.put("timestamp", Instant.now().toString());
            result.put("path", filePath != null && filePath.toString() != null ? filePath.toString() : "");
            result.put("size", bytesWritten);
            result.put("encoding", encoding);
            result.put("mode", mode);
            result.put("backupCreated", backupPath != null);
            if (backupPath != null) {
                result.put("backupPath", backupPath != null ? backupPath : "");
            }

            logger.debug("WriteFileAction completed successfully. Wrote {} bytes to {}", bytesWritten,
                    filePath.toString());
            return ActionResult.success(result, System.currentTimeMillis());

        } catch (SecurityException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Security violation";
            logger.error("Security violation in WriteFileAction: {}", errorMessage);
            throw new ActionException(ACTION_ID, errorMessage);
        } catch (IOException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "IO error";
            logger.error("IO error in WriteFileAction: {}", errorMessage);
            throw new ActionException(ACTION_ID, "Failed to write file: " + errorMessage);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            logger.error("Unexpected error in WriteFileAction: {}", errorMessage, e);
            throw new ActionException(ACTION_ID, "Unexpected error: " + errorMessage);
        }
    }

    private String createBackup(Path filePath) throws IOException {
        String timestamp = Instant.now().toString().replace(":", "-").replace(".", "-");
        String fileName = filePath.getFileName().toString();
        String backupFileName = fileName + ".backup." + timestamp;
        Path backupPath = filePath.resolveSibling(backupFileName);

        Files.copy(filePath, backupPath);
        logger.debug("Created backup: {}", backupPath);

        return backupPath.toString();
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
                .description("Writes file contents within the openHAB root folder with security validation")
                .tags(List.of("filesystem", "write", "file", "content", "security"))
                .documentation("Provides secure file writing capabilities for openHAB files")
                .examples(List.of("Write new file: {\"path\": \"conf/test.txt\", \"content\": \"Hello World\"}",
                        "Append to file: {\"path\": \"conf/log.txt\", \"content\": \"New log entry\", \"mode\": \"append\"}",
                        "Create with backup: {\"path\": \"conf/config.cfg\", \"content\": \"new config\", \"createBackup\": true}",
                        "Create directories: {\"path\": \"conf/newdir/test.txt\", \"content\": \"test\", \"createDirectories\": true}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("encoding_support", true, "write_modes", true, "backup_support", true, "directory_creation", true,
                "size_limits", true, "security", true, "async", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("WriteFileAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("WriteFileAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
