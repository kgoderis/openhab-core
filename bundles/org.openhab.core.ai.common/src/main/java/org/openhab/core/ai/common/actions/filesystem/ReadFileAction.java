package org.openhab.core.ai.common.actions.filesystem;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Action for reading files in openHAB.
 * 
 * This action provides functionality to read content
 * from files with various options.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class ReadFileAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(ReadFileAction.class);
    private static final String ACTION_ID = "read_file";
    private static final String ACTION_NAME = "Read File";
    private static final String DESCRIPTION = "Reads file contents within the openHAB root folder with security validation";

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
                "File path to read (must be within openHAB root folder)", "required", true));
        properties.put("encoding", Map.of("type", "string", "description", "File encoding (default: UTF-8)", "default",
                "UTF-8", "enum", List.of("UTF-8", "ISO-8859-1", "US-ASCII", "UTF-16", "UTF-16BE", "UTF-16LE")));
        properties.put("maxSize",
                Map.of("type", "integer", "description", "Maximum file size to read in bytes (default: 10MB)",
                        "default", 10485760, "minimum", 1, "maximum", 104857600));
        properties.put("includeMetadata", Map.of("type", "boolean", "description",
                "Include file metadata (size, timestamps, permissions)", "default", true));
        properties.put("lineNumbers",
                Map.of("type", "boolean", "description", "Include line numbers in the output", "default", false));
        properties.put("startLine", Map.of("type", "integer", "description",
                "Start reading from this line number (1-based)", "default", 1, "minimum", 1));
        properties.put("endLine",
                Map.of("type", "integer", "description", "Stop reading at this line number (inclusive)", "minimum", 1));

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

        // Validate encoding if provided
        if (parameters.containsKey("encoding")) {
            String encoding = (String) parameters.get("encoding");
            if (encoding != null && !List.of("UTF-8", "ISO-8859-1", "US-ASCII", "UTF-16", "UTF-16BE", "UTF-16LE")
                    .contains(encoding)) {
                errors.add("Invalid encoding: " + encoding);
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

        // Validate line numbers if provided
        if (parameters.containsKey("startLine")) {
            Object startLineObj = parameters.get("startLine");
            if (startLineObj instanceof Integer) {
                int startLine = (Integer) startLineObj;
                if (startLine < 1) {
                    errors.add("startLine must be at least 1");
                }
            } else {
                errors.add("startLine must be an integer");
            }
        }

        if (parameters.containsKey("endLine")) {
            Object endLineObj = parameters.get("endLine");
            if (endLineObj instanceof Integer) {
                int endLine = (Integer) endLineObj;
                if (endLine < 1) {
                    errors.add("endLine must be at least 1");
                }

                // Check if startLine is also provided and endLine >= startLine
                if (parameters.containsKey("startLine")) {
                    Object startLineObj = parameters.get("startLine");
                    if (startLineObj instanceof Integer) {
                        int startLine = (Integer) startLineObj;
                        if (endLine < startLine) {
                            errors.add("endLine must be greater than or equal to startLine");
                        }
                    }
                }
            } else {
                errors.add("endLine must be an integer");
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
        properties.put("path", Map.of("type", "string", "description", "File path that was read"));
        properties.put("content", Map.of("type", "string", "description", "File content"));
        properties.put("encoding", Map.of("type", "string", "description", "Encoding used"));
        properties.put("size", Map.of("type", "integer", "description", "File size in bytes"));
        properties.put("lineCount", Map.of("type", "integer", "description", "Number of lines in file"));
        properties.put("metadata", Map.of("type", "object", "description", "File metadata (if requested)"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        logger.debug("Executing ReadFileAction with context: {}", context.getProtocol());

        try {
            Map<String, Object> result = new HashMap<>();

            // Extract parameters with defaults
            Object pathObj = parameters.get("path");
            if (pathObj == null) {
                throw new AIActionException(ACTION_ID, "Path parameter is required");
            }
            String path = (String) pathObj;
            String encoding = (String) parameters.getOrDefault("encoding", "UTF-8");
            Integer maxSize = (Integer) parameters.getOrDefault("maxSize", 10485760);
            Boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", true);
            Boolean lineNumbers = (Boolean) parameters.getOrDefault("lineNumbers", false);
            Integer startLine = (Integer) parameters.getOrDefault("startLine", 1);
            Integer endLine = (Integer) parameters.get("endLine"); // Optional

            // Validate path security
            FileSystemSecurityUtils.validatePath(path, "read file");

            // Resolve the path
            Path filePath = Paths.get(path).toAbsolutePath().normalize();

            if (!Files.exists(filePath)) {
                throw new AIActionException(ACTION_ID, "File does not exist: " + path);
            }

            if (!Files.isRegularFile(filePath)) {
                throw new AIActionException(ACTION_ID, "Path is not a regular file: " + path);
            }

            // Check file size
            long fileSize = Files.size(filePath);
            if (fileSize > maxSize) {
                throw new AIActionException(ACTION_ID, String
                        .format("File size (%d bytes) exceeds maximum allowed size (%d bytes)", fileSize, maxSize));
            }

            // Read file content
            Charset charset = Charset.forName(encoding);
            List<String> lines = Files.readAllLines(filePath, charset);

            // Apply line filtering if specified
            if (endLine != null) {
                lines = lines.subList(Math.max(0, startLine - 1), Math.min(lines.size(), endLine));
            } else if (startLine > 1) {
                lines = lines.subList(Math.max(0, startLine - 1), lines.size());
            }

            // Format content with line numbers if requested
            String content;
            if (lineNumbers) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < lines.size(); i++) {
                    int lineNumber = startLine + i;
                    sb.append(String.format("%6d: %s%n", lineNumber, lines.get(i)));
                }
                content = sb.toString();
            } else {
                content = String.join(System.lineSeparator(), lines);
            }

            // Build result
            result.put("timestamp", Instant.now().toString());
            result.put("path", filePath != null && filePath.toString() != null ? filePath.toString() : "");
            result.put("content", content);
            result.put("encoding", encoding);
            result.put("size", fileSize);
            result.put("lineCount", lines.size());
            result.put("startLine", startLine);
            if (endLine != null) {
                result.put("endLine", endLine);
            }

            // Add metadata if requested
            if (includeMetadata) {
                Map<String, Object> metadata = new HashMap<>();
                try {
                    metadata.put("lastModified", Files.getLastModifiedTime(filePath).toString());
                    metadata.put("created", Files.getAttribute(filePath, "creationTime").toString());
                    metadata.put("lastAccessed", Files.getLastModifiedTime(filePath).toString());
                    metadata.put("isReadable", Files.isReadable(filePath));
                    metadata.put("isWritable", Files.isWritable(filePath));
                    metadata.put("isExecutable", Files.isExecutable(filePath));

                    // Try to get POSIX permissions
                    try {
                        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(filePath);
                        metadata.put("permissions", PosixFilePermissions.toString(permissions));
                    } catch (UnsupportedOperationException e) {
                        metadata.put("permissions", "Not available");
                    }
                } catch (IOException e) {
                    logger.warn("Could not read file metadata for {}: {}", filePath, e.getMessage());
                    metadata.put("error", "Could not read file metadata: " + e.getMessage());
                }
                result.put("metadata", metadata);
            }

            logger.debug("ReadFileAction completed successfully. Read {} bytes from {}", fileSize, filePath.toString());
            return AIActionResult.success(result, System.currentTimeMillis());

        } catch (SecurityException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Security violation";
            logger.error("Security violation in ReadFileAction: {}", errorMessage);
            throw new AIActionException(ACTION_ID, errorMessage);
        } catch (IOException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "IO error";
            logger.error("IO error in ReadFileAction: {}", errorMessage);
            throw new AIActionException(ACTION_ID, "Failed to read file: " + errorMessage);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            logger.error("Unexpected error in ReadFileAction: {}", errorMessage, e);
            throw new AIActionException(ACTION_ID, "Unexpected error: " + errorMessage);
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
                .description("Reads file contents within the openHAB root folder with security validation")
                .tags(List.of("filesystem", "read", "file", "content", "security"))
                .documentation("Provides secure file reading capabilities for openHAB files")
                .examples(List.of("Read entire file: {\"path\": \"conf/services/addons.cfg\"}",
                        "Read with encoding: {\"path\": \"conf/services/addons.cfg\", \"encoding\": \"UTF-8\"}",
                        "Read with line numbers: {\"path\": \"conf/services/addons.cfg\", \"lineNumbers\": true}",
                        "Read specific lines: {\"path\": \"conf/services/addons.cfg\", \"startLine\": 10, \"endLine\": 20}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("encoding_support", true, "line_filtering", true, "metadata", true, "line_numbers", true,
                "size_limits", true, "security", true, "async", true);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("ReadFileAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ReadFileAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
