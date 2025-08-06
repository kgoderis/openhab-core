package org.openhab.core.ai.action.library.filesystem;

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
 * Action for getting file permissions in openHAB.
 * 
 * This action provides functionality to retrieve
 * permissions for files and directories.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class GetFilePermissionsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetFilePermissionsAction.class);

    private static final String ACTION_ID = "filesystem.get_permissions";
    private static final String ACTION_NAME = "Get File Permissions";
    private static final String DESCRIPTION = "Get detailed file permissions for a specified file or directory";
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
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();

        Map<String, Object> pathParam = new HashMap<>();
        pathParam.put("type", "string");
        pathParam.put("description", "Path to the file or directory");
        pathParam.put("required", true);
        schema.put("path", pathParam);

        Map<String, Object> detailedParam = new HashMap<>();
        detailedParam.put("type", "boolean");
        detailedParam.put("description", "Include detailed permission information");
        detailedParam.put("required", false);
        detailedParam.put("default", false);
        schema.put("detailed", detailedParam);

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();

        Map<String, Object> permissions = new HashMap<>();
        permissions.put("type", "object");
        permissions.put("description", "File permissions information");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> pathProp = new HashMap<>();
        pathProp.put("type", "string");
        pathProp.put("description", "File path");
        properties.put("path", pathProp);

        Map<String, Object> existsProp = new HashMap<>();
        existsProp.put("type", "boolean");
        existsProp.put("description", "Whether the file exists");
        properties.put("exists", existsProp);

        Map<String, Object> readableProp = new HashMap<>();
        readableProp.put("type", "boolean");
        readableProp.put("description", "Whether the file is readable");
        properties.put("readable", readableProp);

        Map<String, Object> writableProp = new HashMap<>();
        writableProp.put("type", "boolean");
        writableProp.put("description", "Whether the file is writable");
        properties.put("writable", writableProp);

        Map<String, Object> executableProp = new HashMap<>();
        executableProp.put("type", "boolean");
        executableProp.put("description", "Whether the file is executable");
        properties.put("executable", executableProp);

        Map<String, Object> posixPermissionsProp = new HashMap<>();
        posixPermissionsProp.put("type", "string");
        posixPermissionsProp.put("description", "POSIX permissions string (e.g., rw-r--r--)");
        properties.put("posixPermissions", posixPermissionsProp);

        Map<String, Object> detailedPermissionsProp = new HashMap<>();
        detailedPermissionsProp.put("type", "object");
        detailedPermissionsProp.put("description", "Detailed permission breakdown");
        properties.put("detailedPermissions", detailedPermissionsProp);

        permissions.put("properties", properties);
        schema.put("permissions", permissions);

        return schema;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("filesystem.read", true);
        capabilities.put("permissions.read", true);
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

        // Validate optional parameters
        if (parameters.containsKey("detailed")) {
            Object detailed = parameters.get("detailed");
            if (!(detailed instanceof Boolean)) {
                errors.add("Parameter 'detailed' must be a boolean");
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
            boolean detailed = parameters.containsKey("detailed") ? (Boolean) parameters.get("detailed") : false;

            // Security check
            if (!FileSystemSecurityUtils.isPathAllowed(path)) {
                throw new ActionException(ACTION_ID, "Security violation: Path not allowed: " + path);
            }

            Path filePath = Paths.get(path);

            Map<String, Object> resultData = new HashMap<>();
            Map<String, Object> permissions = new HashMap<>();

            // Check if file exists
            boolean exists = Files.exists(filePath);
            permissions.put("path", path);
            permissions.put("exists", exists);

            if (exists) {
                // Basic permissions
                permissions.put("readable", Files.isReadable(filePath));
                permissions.put("writable", Files.isWritable(filePath));
                permissions.put("executable", Files.isExecutable(filePath));

                // POSIX permissions (if supported)
                try {
                    Set<PosixFilePermission> posixPermissions = Files.getPosixFilePermissions(filePath);
                    String posixString = PosixFilePermissions.toString(posixPermissions);
                    permissions.put("posixPermissions", posixString);

                    if (detailed) {
                        Map<String, Object> detailedPerms = new HashMap<>();

                        // Owner permissions
                        Map<String, Boolean> ownerPerms = new HashMap<>();
                        ownerPerms.put("read", posixPermissions.contains(PosixFilePermission.OWNER_READ));
                        ownerPerms.put("write", posixPermissions.contains(PosixFilePermission.OWNER_WRITE));
                        ownerPerms.put("execute", posixPermissions.contains(PosixFilePermission.OWNER_EXECUTE));
                        detailedPerms.put("owner", ownerPerms);

                        // Group permissions
                        Map<String, Boolean> groupPerms = new HashMap<>();
                        groupPerms.put("read", posixPermissions.contains(PosixFilePermission.GROUP_READ));
                        groupPerms.put("write", posixPermissions.contains(PosixFilePermission.GROUP_WRITE));
                        groupPerms.put("execute", posixPermissions.contains(PosixFilePermission.GROUP_EXECUTE));
                        detailedPerms.put("group", groupPerms);

                        // Others permissions
                        Map<String, Boolean> othersPerms = new HashMap<>();
                        othersPerms.put("read", posixPermissions.contains(PosixFilePermission.OTHERS_READ));
                        othersPerms.put("write", posixPermissions.contains(PosixFilePermission.OTHERS_WRITE));
                        othersPerms.put("execute", posixPermissions.contains(PosixFilePermission.OTHERS_EXECUTE));
                        detailedPerms.put("others", othersPerms);

                        // Special permissions
                        Map<String, Boolean> specialPerms = new HashMap<>();
                        specialPerms.put("setuid",
                                posixPermissions.contains(PosixFilePermission.OWNER_EXECUTE)
                                        && posixPermissions.contains(PosixFilePermission.GROUP_EXECUTE)
                                        && posixPermissions.contains(PosixFilePermission.OTHERS_EXECUTE));
                        specialPerms.put("setgid", posixPermissions.contains(PosixFilePermission.GROUP_EXECUTE));
                        specialPerms.put("sticky", posixPermissions.contains(PosixFilePermission.OTHERS_EXECUTE));
                        detailedPerms.put("special", specialPerms);

                        permissions.put("detailedPermissions", detailedPerms);
                    }
                } catch (UnsupportedOperationException e) {
                    // POSIX not supported on this platform
                    permissions.put("posixPermissions", "not_supported");
                    if (detailed) {
                        permissions.put("detailedPermissions", "not_supported");
                    }
                }
            } else {
                permissions.put("readable", false);
                permissions.put("writable", false);
                permissions.put("executable", false);
                permissions.put("posixPermissions", "file_not_found");
                if (detailed) {
                    permissions.put("detailedPermissions", "file_not_found");
                }
            }

            resultData.put("permissions", permissions);

            logger.debug("Retrieved permissions for path: {}", path);
            return ActionResult.success(resultData, System.currentTimeMillis() - startTime);

        } catch (SecurityException e) {
            logger.error("Security violation during permission check: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "Security violation: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during permission check: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "IO error during permission check: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during permission check: {}", e.getMessage());
            throw new ActionException(ACTION_ID, "Unexpected error during permission check: " + e.getMessage());
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
