package org.openhab.core.ai.actions.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
 * Action to set file permissions for a specified file or directory.
 * Only operates within the openHAB root folder for security.
 */
@NonNullByDefault
public class SetFilePermissionsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(SetFilePermissionsAction.class);

    private static final String ACTION_ID = "filesystem.set_permissions";
    private static final String ACTION_NAME = "Set File Permissions";
    private static final String DESCRIPTION = "Set file permissions for a specified file or directory";
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

        Map<String, Object> pathParam = new HashMap<>();
        pathParam.put("type", "string");
        pathParam.put("description", "Path to the file or directory");
        pathParam.put("required", true);
        schema.put("path", pathParam);

        Map<String, Object> permissionsParam = new HashMap<>();
        permissionsParam.put("type", "string");
        permissionsParam.put("description", "POSIX permissions string (e.g., rw-r--r--)");
        permissionsParam.put("required", true);
        schema.put("permissions", permissionsParam);

        Map<String, Object> recursiveParam = new HashMap<>();
        recursiveParam.put("type", "boolean");
        recursiveParam.put("description", "Apply permissions recursively to subdirectories");
        recursiveParam.put("required", false);
        recursiveParam.put("default", false);
        schema.put("recursive", recursiveParam);

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();

        Map<String, Object> result = new HashMap<>();
        result.put("type", "object");
        result.put("description", "Permission setting result");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> pathProp = new HashMap<>();
        pathProp.put("type", "string");
        pathProp.put("description", "File path");
        properties.put("path", pathProp);

        Map<String, Object> successProp = new HashMap<>();
        successProp.put("type", "boolean");
        successProp.put("description", "Whether the operation was successful");
        properties.put("success", successProp);

        Map<String, Object> oldPermissionsProp = new HashMap<>();
        oldPermissionsProp.put("type", "string");
        oldPermissionsProp.put("description", "Previous permissions");
        properties.put("oldPermissions", oldPermissionsProp);

        Map<String, Object> newPermissionsProp = new HashMap<>();
        newPermissionsProp.put("type", "string");
        newPermissionsProp.put("description", "New permissions");
        properties.put("newPermissions", newPermissionsProp);

        Map<String, Object> messageProp = new HashMap<>();
        messageProp.put("type", "string");
        messageProp.put("description", "Result message");
        properties.put("message", messageProp);

        result.put("properties", properties);
        schema.put("result", result);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("filesystem.write", true);
        capabilities.put("permissions.write", true);
        return capabilities;
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().description(DESCRIPTION).version(getVersion()).build();
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate required parameters
        if (!parameters.containsKey("path")) {
            errors.add("Missing required parameter: path");
        } else {
            String path = (String) parameters.get("path");
            if (path == null || path.trim().isEmpty()) {
                errors.add("Path cannot be null or empty");
            } else {
                // Validate path security
                try {
                    if (!FileSystemSecurityUtils.isPathAllowed(path)) {
                        errors.add("Path is not allowed: " + path);
                    }
                } catch (Exception e) {
                    errors.add("Invalid path format: " + path);
                }
            }
        }

        if (!parameters.containsKey("permissions")) {
            errors.add("Missing required parameter: permissions");
        } else {
            String permissions = (String) parameters.get("permissions");
            if (permissions == null || permissions.trim().isEmpty()) {
                errors.add("Permissions cannot be null or empty");
            } else {
                // Validate POSIX permissions format
                try {
                    PosixFilePermissions.fromString(permissions);
                } catch (IllegalArgumentException e) {
                    errors.add("Invalid POSIX permissions format: " + permissions + ". Expected format: rw-r--r--");
                }
            }
        }

        // Validate optional parameters
        if (parameters.containsKey("recursive")) {
            Object recursive = parameters.get("recursive");
            if (!(recursive instanceof Boolean)) {
                errors.add("Parameter 'recursive' must be a boolean");
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

            String path = (String) parameters.get("path");
            String permissions = (String) parameters.get("permissions");
            boolean recursive = parameters.containsKey("recursive") ? (Boolean) parameters.get("recursive") : false;

            // Security check
            if (!FileSystemSecurityUtils.isPathAllowed(path)) {
                throw new ActionException(ACTION_ID, "Security violation: Path not allowed: " + path);
            }

            Path filePath = Paths.get(path);

            // Check if file exists
            if (!Files.exists(filePath)) {
                throw new ActionException(ACTION_ID, "File does not exist: " + path);
            }

            Map<String, Object> resultData = new HashMap<>();
            Map<String, Object> result = new HashMap<>();

            // Get current permissions
            String oldPermissions = "unknown";
            try {
                Set<PosixFilePermission> currentPerms = Files.getPosixFilePermissions(filePath);
                oldPermissions = PosixFilePermissions.toString(currentPerms);
            } catch (UnsupportedOperationException e) {
                logger.warn("POSIX permissions not supported on this platform");
                throw new ActionException(ACTION_ID, "POSIX permissions not supported on this platform");
            } catch (IOException e) {
                logger.warn("Could not read current permissions: {}", e.getMessage());
            }

            // Parse new permissions
            Set<PosixFilePermission> newPerms = PosixFilePermissions.fromString(permissions);

            // Set permissions
            Files.setPosixFilePermissions(filePath, newPerms);

            // Verify the change
            Set<PosixFilePermission> actualPerms = Files.getPosixFilePermissions(filePath);
            String actualPermissions = PosixFilePermissions.toString(actualPerms);

            result.put("path", path);
            result.put("success", true);
            result.put("oldPermissions", oldPermissions);
            result.put("newPermissions", actualPermissions);
            result.put("message", "Permissions set successfully");

            // Handle recursive setting if requested
            if (recursive && Files.isDirectory(filePath)) {
                int filesProcessed = 0;
                try (var stream = Files.walk(filePath)) {
                    for (Path subPath : (Iterable<Path>) stream::iterator) {
                        if (!subPath.equals(filePath)) { // Skip the root directory
                            try {
                                Files.setPosixFilePermissions(subPath, newPerms);
                                filesProcessed++;
                            } catch (IOException e) {
                                logger.warn("Could not set permissions for {}: {}", subPath, e.getMessage());
                            }
                        }
                    }
                }
                result.put("filesProcessed", filesProcessed);
                result.put("message", "Permissions set successfully for " + (filesProcessed + 1) + " items");
            }

            resultData.put("result", result);

            logger.debug("Set permissions for path: {} to {}", path, actualPermissions);
            return ActionResult.success(resultData, System.currentTimeMillis() - startTime);

        } catch (SecurityException e) {
            logger.error("Security violation during permission setting: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "Security violation: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during permission setting: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "IO error during permission setting: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during permission setting: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "Unexpected error during permission setting: " + e.getMessage());
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
