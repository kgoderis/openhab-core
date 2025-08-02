package org.openhab.core.ai.common.actions.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
 * AI Action for creating directories within the openHAB root folder.
 * 
 * This action provides secure directory creation capabilities, ensuring operations
 * only work within the openHAB configuration and user data directories.
 * 
 * 
 */
@NonNullByDefault
public class CreateDirectoryAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(CreateDirectoryAction.class);
    private static final String ACTION_ID = "create_directory";
    private static final String ACTION_NAME = "Create Directory";
    private static final String DESCRIPTION = "Creates directories within the openHAB root folder with security validation";

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
                "Directory path to create (must be within openHAB root folder)", "required", true));
        properties.put("createParents", Map.of("type", "boolean", "description",
                "Create parent directories if they don't exist", "default", true));
        properties.put("mode",
                Map.of("type", "string", "description", "Creation mode: create, create_if_missing, or fail_if_exists",
                        "default", "create_if_missing", "enum",
                        List.of("create", "create_if_missing", "fail_if_exists")));
        properties.put("permissions", Map.of("type", "string", "description",
                "Directory permissions (POSIX format, e.g., '755')", "default", "755"));

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

        // Validate mode if provided
        if (parameters.containsKey("mode")) {
            String mode = (String) parameters.get("mode");
            if (mode != null && !List.of("create", "create_if_missing", "fail_if_exists").contains(mode)) {
                errors.add("Invalid mode: " + mode + ". Must be one of: create, create_if_missing, fail_if_exists");
            }
        }

        // Validate permissions if provided
        if (parameters.containsKey("permissions")) {
            String permissions = (String) parameters.get("permissions");
            if (permissions != null && !permissions.matches("^[0-7]{3}$")) {
                errors.add("Invalid permissions format: " + permissions + ". Must be 3 digits (0-7)");
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
        properties.put("path", Map.of("type", "string", "description", "Directory path that was created"));
        properties.put("created", Map.of("type", "boolean", "description", "Whether directory was created"));
        properties.put("existed", Map.of("type", "boolean", "description", "Whether directory already existed"));
        properties.put("mode", Map.of("type", "string", "description", "Creation mode used"));
        properties.put("permissions", Map.of("type", "string", "description", "Directory permissions"));
        properties.put("parentDirectoriesCreated",
                Map.of("type", "integer", "description", "Number of parent directories created"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        logger.debug("Executing CreateDirectoryAction with context: {}", context.getProtocol());

        try {
            Map<String, Object> result = new HashMap<>();

            // Extract parameters with defaults
            Object pathObj = parameters.get("path");
            if (pathObj == null) {
                throw new AIActionException(ACTION_ID, "Path parameter is required");
            }
            String path = (String) pathObj;
            Boolean createParents = (Boolean) parameters.getOrDefault("createParents", true);
            String mode = (String) parameters.getOrDefault("mode", "create_if_missing");
            String permissions = (String) parameters.getOrDefault("permissions", "755");

            // Validate path security
            FileSystemSecurityUtils.validatePath(path, "create directory");

            // Resolve the path
            Path dirPath = Paths.get(path).toAbsolutePath().normalize();

            // Check if directory already exists
            boolean existed = Files.exists(dirPath);

            if (existed && mode.equals("fail_if_exists")) {
                throw new AIActionException(ACTION_ID,
                        "Directory already exists and mode is 'fail_if_exists': " + path);
            }

            if (existed && mode.equals("create")) {
                throw new AIActionException(ACTION_ID, "Directory already exists and mode is 'create': " + path);
            }

            boolean created = false;
            int parentDirectoriesCreated = 0;

            if (!existed) {
                // Create parent directories if requested
                if (createParents) {
                    Path parent = dirPath.getParent();
                    if (parent != null && !Files.exists(parent)) {
                        parentDirectoriesCreated = createParentDirectories(parent);
                    }
                }

                // Create the directory
                Files.createDirectory(dirPath);
                created = true;
                logger.debug("Created directory: {}", dirPath);
            } else {
                logger.debug("Directory already exists: {}", dirPath);
            }

            // Set permissions if specified and directory was created
            if (created && permissions != null) {
                try {
                    Set<PosixFilePermission> perms = parsePermissions(permissions);
                    Files.setPosixFilePermissions(dirPath, perms);
                    logger.debug("Set permissions {} on directory: {}", permissions, dirPath);
                } catch (UnsupportedOperationException e) {
                    logger.warn("POSIX permissions not supported on this platform");
                } catch (Exception e) {
                    logger.warn("Failed to set permissions {} on directory {}: {}", permissions, dirPath,
                            e.getMessage());
                }
            }

            // Build result
            result.put("timestamp", Instant.now().toString());
            result.put("path", dirPath != null && dirPath.toString() != null ? dirPath.toString() : "");
            result.put("created", created);
            result.put("existed", existed);
            result.put("mode", mode);
            result.put("permissions", permissions);
            result.put("parentDirectoriesCreated", parentDirectoriesCreated);

            logger.debug("CreateDirectoryAction completed successfully. Directory: {}, Created: {}", dirPath.toString(),
                    created);
            return AIActionResult.success(result, System.currentTimeMillis());

        } catch (SecurityException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Security violation";
            logger.error("Security violation in CreateDirectoryAction: {}", errorMessage);
            throw new AIActionException(ACTION_ID, errorMessage);
        } catch (IOException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "IO error";
            logger.error("IO error in CreateDirectoryAction: {}", errorMessage);
            throw new AIActionException(ACTION_ID, "Failed to create directory: " + errorMessage);
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            logger.error("Unexpected error in CreateDirectoryAction: {}", errorMessage, e);
            throw new AIActionException(ACTION_ID, "Unexpected error: " + errorMessage);
        }
    }

    private int createParentDirectories(Path parentPath) throws IOException {
        int created = 0;
        Path current = parentPath;

        // Find the first existing parent
        while (current != null && !Files.exists(current)) {
            current = current.getParent();
        }

        // Create directories from the first missing one up to the target parent
        if (current != null) {
            Path missingParent = current.resolve(parentPath.getFileName());
            while (missingParent != null && !missingParent.equals(parentPath)) {
                if (!Files.exists(missingParent)) {
                    Files.createDirectory(missingParent);
                    created++;
                    logger.debug("Created parent directory: {}", missingParent);
                }
                missingParent = missingParent.resolve(parentPath.getFileName());
            }
        }

        return created;
    }

    private Set<PosixFilePermission> parsePermissions(String permissions) {
        Set<PosixFilePermission> perms = new HashSet<>();

        if (permissions.length() != 3) {
            throw new IllegalArgumentException("Permissions must be exactly 3 digits");
        }

        // Parse owner permissions
        int owner = Character.getNumericValue(permissions.charAt(0));
        if ((owner & 4) != 0)
            perms.add(PosixFilePermission.OWNER_READ);
        if ((owner & 2) != 0)
            perms.add(PosixFilePermission.OWNER_WRITE);
        if ((owner & 1) != 0)
            perms.add(PosixFilePermission.OWNER_EXECUTE);

        // Parse group permissions
        int group = Character.getNumericValue(permissions.charAt(1));
        if ((group & 4) != 0)
            perms.add(PosixFilePermission.GROUP_READ);
        if ((group & 2) != 0)
            perms.add(PosixFilePermission.GROUP_WRITE);
        if ((group & 1) != 0)
            perms.add(PosixFilePermission.GROUP_EXECUTE);

        // Parse others permissions
        int others = Character.getNumericValue(permissions.charAt(2));
        if ((others & 4) != 0)
            perms.add(PosixFilePermission.OTHERS_READ);
        if ((others & 2) != 0)
            perms.add(PosixFilePermission.OTHERS_WRITE);
        if ((others & 1) != 0)
            perms.add(PosixFilePermission.OTHERS_EXECUTE);

        return perms;
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
                .description("Creates directories within the openHAB root folder with security validation")
                .tags(List.of("filesystem", "create", "directory", "security"))
                .documentation("Provides secure directory creation capabilities for openHAB")
                .examples(List.of("Create directory: {\"path\": \"conf/newdir\"}",
                        "Create with parents: {\"path\": \"conf/nested/deep/dir\", \"createParents\": true}",
                        "Create with permissions: {\"path\": \"conf/secure\", \"permissions\": \"700\"}",
                        "Fail if exists: {\"path\": \"conf/temp\", \"mode\": \"fail_if_exists\"}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("directory_creation", true, "parent_creation", true, "permission_setting", true, "mode_control",
                true, "security", true, "async", true);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("CreateDirectoryAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("CreateDirectoryAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
