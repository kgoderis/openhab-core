package org.openhab.core.ai.common.actions.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Action for getting file information in openHAB.
 * 
 * This action provides functionality to retrieve
 * detailed information about files and directories.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class GetFileInfoAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetFileInfoAction.class);
    private static final String ACTION_ID = "get_file_info";
    private static final String ACTION_NAME = "Get File Info";
    private static final String DESCRIPTION = "Gets detailed file information within the openHAB root folder with security validation";

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
        properties.put("path",
                Map.of("type", "string", "description", "File or directory path (must be within openHAB root folder)"));
        properties.put("includePermissions",
                Map.of("type", "boolean", "description", "Include file permissions information", "default", true));
        properties.put("includeAttributes", Map.of("type", "boolean", "description",
                "Include file attributes (timestamps, etc.)", "default", true));
        properties.put("includeContentInfo", Map.of("type", "boolean", "description",
                "Include content information (MIME type, encoding)", "default", false));
        properties.put("recursive", Map.of("type", "boolean", "description",
                "Include information about subdirectories and files", "default", false));
        properties.put("maxDepth", Map.of("type", "integer", "description", "Maximum depth for recursive information",
                "default", 3, "minimum", 1, "maximum", 10));

        schema.put("properties", properties);
        schema.put("required", List.of("path"));

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
            if (!parameters.containsKey("path")) {
                errors.add("Missing required parameter: path");
            }

            String path = (String) parameters.get("path");

            // Validate path
            if (path == null || path.trim().isEmpty()) {
                errors.add("Path cannot be null or empty");
            }

            // Validate path is within allowed directories
            if (path != null && !path.trim().isEmpty() && !FileSystemSecurityUtils.isPathAllowed(path)) {
                errors.add("Path is not within allowed openHAB directories: " + path);
            }

            // Validate numeric parameters
            if (parameters.containsKey("maxDepth")) {
                Object maxDepth = parameters.get("maxDepth");
                if (!(maxDepth instanceof Number) || ((Number) maxDepth).intValue() < 1
                        || ((Number) maxDepth).intValue() > 10) {
                    errors.add("maxDepth must be between 1 and 10");
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
        properties.put("path", Map.of("type", "string"));
        properties.put("exists", Map.of("type", "boolean"));
        properties.put("fileInfo", Map.of("type", "object"));
        properties.put("children", Map.of("type", "array"));
        properties.put("executionTime", Map.of("type", "number"));

        schema.put("properties", properties);
        schema.put("required", List.of("success", "message", "path", "exists"));

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
            String path = (String) parameters.get("path");
            boolean includePermissions = (Boolean) parameters.getOrDefault("includePermissions", true);
            boolean includeAttributes = (Boolean) parameters.getOrDefault("includeAttributes", true);
            boolean includeContentInfo = (Boolean) parameters.getOrDefault("includeContentInfo", false);
            boolean recursive = (Boolean) parameters.getOrDefault("recursive", false);
            int maxDepth = ((Number) parameters.getOrDefault("maxDepth", 3)).intValue();

            // Validate path
            FileSystemSecurityUtils.validatePath(path, "get file info");

            Path filePath = Paths.get(path);

            // Build response
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("success", true);
            resultData.put("message", "File information retrieved successfully");
            resultData.put("path", path);
            resultData.put("exists", Files.exists(filePath));

            if (Files.exists(filePath)) {
                // Get file information
                Map<String, Object> fileInfo = getFileInfo(filePath, includePermissions, includeAttributes,
                        includeContentInfo);
                resultData.put("fileInfo", fileInfo);

                // Get children information if recursive
                if (recursive && Files.isDirectory(filePath)) {
                    List<Map<String, Object>> children = getChildrenInfo(filePath, maxDepth, includePermissions,
                            includeAttributes, includeContentInfo);
                    resultData.put("children", children);
                }
            }

            resultData.put("executionTime", (System.currentTimeMillis() - startTime) / 1000.0);

            logger.info("File info retrieved for: {}", path);

            return AIActionResult.success(resultData, System.currentTimeMillis() - startTime);

        } catch (SecurityException e) {
            logger.error("Security violation during file info retrieval: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "Security violation: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during file info retrieval: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "IO error during file info retrieval: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during file info retrieval: {}", e.getMessage());
            throw new AIActionException(ACTION_ID, "Unexpected error during file info retrieval: " + e.getMessage());
        }
    }

    private Map<String, Object> getFileInfo(Path filePath, boolean includePermissions, boolean includeAttributes,
            boolean includeContentInfo) throws IOException {
        Map<String, Object> fileInfo = new HashMap<>();

        // Basic information
        fileInfo.put("name", filePath.getFileName().toString());
        fileInfo.put("absolutePath", filePath.toAbsolutePath().toString());
        fileInfo.put("isDirectory", Files.isDirectory(filePath));
        fileInfo.put("isFile", Files.isRegularFile(filePath));
        fileInfo.put("isHidden", isHidden(filePath));
        fileInfo.put("isReadable", Files.isReadable(filePath));
        fileInfo.put("isWritable", Files.isWritable(filePath));
        fileInfo.put("isExecutable", Files.isExecutable(filePath));

        // Size information
        if (Files.isRegularFile(filePath)) {
            fileInfo.put("size", Files.size(filePath));
            fileInfo.put("sizeFormatted", formatFileSize(Files.size(filePath)));
        } else if (Files.isDirectory(filePath)) {
            long totalSize = calculateDirectorySize(filePath);
            fileInfo.put("size", totalSize);
            fileInfo.put("sizeFormatted", formatFileSize(totalSize));
        }

        // Attributes
        if (includeAttributes) {
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            fileInfo.put("creationTime", attrs.creationTime().toInstant().toString());
            fileInfo.put("lastModifiedTime", attrs.lastModifiedTime().toInstant().toString());
            fileInfo.put("lastAccessTime", attrs.lastAccessTime().toInstant().toString());
            fileInfo.put("isSymbolicLink", attrs.isSymbolicLink());
            fileInfo.put("isOther", attrs.isOther());
        }

        // Permissions
        if (includePermissions) {
            try {
                Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(filePath);
                fileInfo.put("permissions", PosixFilePermissions.toString(permissions));
                fileInfo.put("permissionsNumeric", String.format("%04o", getNumericPermissions(permissions)));
            } catch (UnsupportedOperationException e) {
                // POSIX permissions not supported on this platform
                fileInfo.put("permissions", "Not supported on this platform");
                fileInfo.put("permissionsNumeric", "Not supported on this platform");
            }
        }

        // Content information
        if (includeContentInfo && Files.isRegularFile(filePath)) {
            fileInfo.put("mimeType", getMimeType(filePath));
            fileInfo.put("encoding", getFileEncoding(filePath));
        }

        return fileInfo;
    }

    private List<Map<String, Object>> getChildrenInfo(Path directoryPath, int maxDepth, boolean includePermissions,
            boolean includeAttributes, boolean includeContentInfo) throws IOException {
        List<Map<String, Object>> children = new ArrayList<>();

        try (var stream = Files.walk(directoryPath, maxDepth)) {
            stream.skip(1) // Skip the root directory itself
                    .forEach(childPath -> {
                        try {
                            Map<String, Object> childInfo = getFileInfo(childPath, includePermissions,
                                    includeAttributes, includeContentInfo);
                            childInfo.put("relativePath", directoryPath.relativize(childPath).toString());
                            children.add(childInfo);
                        } catch (IOException e) {
                            logger.warn("Failed to get info for child: {}", childPath, e);
                        }
                    });
        }

        return children;
    }

    private boolean isHidden(Path path) {
        try {
            return Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }

    private long calculateDirectorySize(Path directoryPath) throws IOException {
        long totalSize = 0;
        try (var stream = Files.walk(directoryPath)) {
            totalSize = stream.filter(Files::isRegularFile).mapToLong(path -> {
                try {
                    return Files.size(path);
                } catch (IOException e) {
                    return 0L;
                }
            }).sum();
        }
        return totalSize;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private String getMimeType(Path filePath) {
        try {
            String mimeType = Files.probeContentType(filePath);
            return mimeType != null ? mimeType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    private String getFileEncoding(Path filePath) {
        // This is a simplified implementation
        // In a real implementation, you might use libraries like Apache Tika or similar
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".txt") || fileName.endsWith(".log")) {
            return "UTF-8";
        } else if (fileName.endsWith(".json") || fileName.endsWith(".xml")) {
            return "UTF-8";
        } else if (fileName.endsWith(".properties")) {
            return "ISO-8859-1";
        } else {
            return "binary";
        }
    }

    private int getNumericPermissions(Set<PosixFilePermission> permissions) {
        int numeric = 0;
        if (permissions.contains(PosixFilePermission.OWNER_READ))
            numeric |= 0400;
        if (permissions.contains(PosixFilePermission.OWNER_WRITE))
            numeric |= 0200;
        if (permissions.contains(PosixFilePermission.OWNER_EXECUTE))
            numeric |= 0100;
        if (permissions.contains(PosixFilePermission.GROUP_READ))
            numeric |= 0040;
        if (permissions.contains(PosixFilePermission.GROUP_WRITE))
            numeric |= 0020;
        if (permissions.contains(PosixFilePermission.GROUP_EXECUTE))
            numeric |= 0010;
        if (permissions.contains(PosixFilePermission.OTHERS_READ))
            numeric |= 0004;
        if (permissions.contains(PosixFilePermission.OTHERS_WRITE))
            numeric |= 0002;
        if (permissions.contains(PosixFilePermission.OTHERS_EXECUTE))
            numeric |= 0001;
        return numeric;
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
        return AIActionMetadata.builder().description(DESCRIPTION).version(getVersion()).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportsAsync", true);
        capabilities.put("supportsStreaming", false);
        capabilities.put("supportedOperations",
                List.of("get_file_info", "get_directory_info", "get_permissions", "get_attributes"));
        return capabilities;
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("Initializing GetFileInfoAction");
    }

    @Override
    public void cleanup() {
        logger.debug("Cleaning up GetFileInfoAction");
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
