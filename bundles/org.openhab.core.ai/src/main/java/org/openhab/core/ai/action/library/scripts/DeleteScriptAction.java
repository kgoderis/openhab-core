package org.openhab.core.ai.action.library.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for deleting openHAB Scripts with backup and validation.
 * 
 * Deletes script files with proper validation, backup creation, and cleanup
 * using real file system integration and safety checks.
 */
@Component(service = Action.class, immediate = true)
public class DeleteScriptAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(DeleteScriptAction.class);
    private static final String ACTION_ID = "openhab.scripts.delete";
    private static final String ACTION_NAME = "Delete Script";

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
        return "Deletes openHAB Scripts with backup creation and validation";
    }

    @Override
    public String getCategory() {
        return "scripts";
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
        properties.put("scriptPath", Map.of("type", "string", "description",
                "Path to the script file to delete (relative to scripts directory or absolute)", "required", true));
        properties.put("createBackup",
                Map.of("type", "boolean", "description", "Create a backup before deleting", "default", true));
        properties.put("backupLocation", Map.of("type", "string", "description",
                "Location to store backup (e.g., 'backups', 'trash')", "default", "backups"));
        properties.put("confirmDeletion",
                Map.of("type", "boolean", "description", "Confirm that deletion is intentional", "default", false));
        properties.put("forceDelete", Map.of("type", "boolean", "description",
                "Force deletion without additional safety checks", "default", false));
        properties.put("cleanupBackups", Map.of("type", "boolean", "description",
                "Clean up old backup files after successful deletion", "default", false));
        properties.put("maxBackupAge", Map.of("type", "integer", "minimum", 1, "maximum", 365, "description",
                "Maximum age of backup files to keep (days)", "default", 30));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("success", Map.of("type", "boolean"));
        properties.put("scriptPath", Map.of("type", "string"));
        properties.put("backupPath", Map.of("type", "string"));
        properties.put("deletedInfo", Map.of("type", "object"));
        properties.put("cleanupInfo", Map.of("type", "object"));
        properties.put("error", Map.of("type", "string"));
        properties.put("timestamp", Map.of("type", "string"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null or empty"));
        }

        String scriptPath = (String) parameters.get("scriptPath");
        if (scriptPath == null || scriptPath.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("scriptPath is required and cannot be empty"));
        }

        // Validate path format
        try {
            Paths.get(scriptPath);
        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Invalid scriptPath format: " + scriptPath));
        }

        // Validate backup location
        String backupLocation = (String) parameters.getOrDefault("backupLocation", "backups");
        if (backupLocation != null && (backupLocation.contains("..") || backupLocation.contains("/"))) {
            return ActionValidationResult.invalid(List.of("Invalid backupLocation: " + backupLocation));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing delete script action with parameters: {}", parameters);

        try {
            String scriptPath = (String) parameters.get("scriptPath");
            boolean createBackup = (Boolean) parameters.getOrDefault("createBackup", true);
            String backupLocation = (String) parameters.getOrDefault("backupLocation", "backups");
            boolean confirmDeletion = (Boolean) parameters.getOrDefault("confirmDeletion", false);
            boolean forceDelete = (Boolean) parameters.getOrDefault("forceDelete", false);
            boolean cleanupBackups = (Boolean) parameters.getOrDefault("cleanupBackups", false);
            int maxBackupAge = (Integer) parameters.getOrDefault("maxBackupAge", 30);

            Map<String, Object> result = deleteScript(scriptPath, createBackup, backupLocation, confirmDeletion,
                    forceDelete, cleanupBackups, maxBackupAge);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Delete script action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to delete script", e);
            throw new ActionException(ACTION_ID, "Failed to delete script: " + e.getMessage(), e);
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
        return ActionMetadata.builder().version(getVersion()).description("Deletes scripts with backup and validation")
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "sandboxing", false, "timeout", false);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("DeleteScriptAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("DeleteScriptAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> deleteScript(String scriptPath, boolean createBackup, String backupLocation,
            boolean confirmDeletion, boolean forceDelete, boolean cleanupBackups, int maxBackupAge)
            throws ActionException, IOException {

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());

        try {
            // Resolve script path
            Path path = resolveScriptPath(scriptPath);

            // Check if file exists
            if (!Files.exists(path)) {
                result.put("success", false);
                result.put("error", "Script file not found: " + path);
                return result;
            }

            // Safety check - require confirmation for deletion
            if (!confirmDeletion && !forceDelete) {
                result.put("success", false);
                result.put("error", "Deletion not confirmed. Set confirmDeletion=true or forceDelete=true to proceed.");
                return result;
            }

            // Collect file information before deletion
            Map<String, Object> deletedInfo = new HashMap<>();
            deletedInfo.put("name", path.getFileName().toString());
            deletedInfo.put("path", path.toString());
            deletedInfo.put("type", determineScriptType(path.getFileName().toString()));
            deletedInfo.put("extension", getFileExtension(path.getFileName().toString()));
            deletedInfo.put("size", Files.size(path));
            deletedInfo.put("sizeFormatted", formatBytes(Files.size(path)));
            deletedInfo.put("lastModified", Files.getLastModifiedTime(path).toInstant().toString());
            deletedInfo.put("readable", Files.isReadable(path));
            deletedInfo.put("writable", Files.isWritable(path));

            // Create backup if requested
            String backupPath = null;
            if (createBackup) {
                backupPath = createBackupFile(path, backupLocation);
                result.put("backupPath", backupPath);
            }

            // Delete the file
            Files.delete(path);

            // Cleanup old backups if requested
            Map<String, Object> cleanupInfo = new HashMap<>();
            if (cleanupBackups && backupPath != null) {
                cleanupInfo = cleanupOldBackups(backupLocation, maxBackupAge);
            }

            result.put("success", true);
            result.put("scriptPath", path.toString());
            result.put("deletedInfo", deletedInfo);
            result.put("cleanupInfo", cleanupInfo);
            result.put("message", "Script deleted successfully");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to delete script: " + e.getMessage());
            logger.warn("Failed to delete script: {}", e.getMessage());
        }

        return result;
    }

    private Path resolveScriptPath(String scriptPath) {
        if (Paths.get(scriptPath).isAbsolute()) {
            return Paths.get(scriptPath);
        } else {
            // Assume relative to scripts directory
            String scriptsDir = OpenHAB.getConfigFolder() + "/scripts";
            return Paths.get(scriptsDir, scriptPath);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private String determineScriptType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return switch (extension) {
            case "js" -> "javascript";
            case "py" -> "python";
            case "rb" -> "ruby";
            case "groovy" -> "groovy";
            case "jsr223" -> "jsr223";
            default -> "unknown";
        };
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private String createBackupFile(Path originalPath, String backupLocation) throws IOException {
        // Create backup directory
        String scriptsDir = OpenHAB.getConfigFolder() + "/scripts";
        Path backupDir = Paths.get(scriptsDir, backupLocation);

        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
        }

        // Generate backup filename with timestamp
        String timestamp = Instant.now().toString().replace(":", "-").replace(".", "-");
        String backupFileName = originalPath.getFileName().toString() + "." + timestamp + ".backup";
        Path backupPath = backupDir.resolve(backupFileName);

        // Copy file to backup location
        Files.copy(originalPath, backupPath);

        return backupPath.toString();
    }

    private Map<String, Object> cleanupOldBackups(String backupLocation, int maxBackupAge) {
        Map<String, Object> cleanupInfo = new HashMap<>();
        cleanupInfo.put("backupLocation", backupLocation);
        cleanupInfo.put("maxBackupAge", maxBackupAge);
        cleanupInfo.put("filesProcessed", 0);
        cleanupInfo.put("filesDeleted", 0);
        cleanupInfo.put("errors", 0);

        try {
            String scriptsDir = OpenHAB.getConfigFolder() + "/scripts";
            Path backupDir = Paths.get(scriptsDir, backupLocation);

            if (!Files.exists(backupDir)) {
                cleanupInfo.put("message", "Backup directory does not exist");
                return cleanupInfo;
            }

            Instant cutoffTime = Instant.now().minusSeconds(maxBackupAge * 24 * 60 * 60L);
            final int[] processed = { 0 };
            final int[] deleted = { 0 };
            final int[] errors = { 0 };

            Files.list(backupDir).filter(path -> path.toString().endsWith(".backup")).forEach(backupFile -> {
                try {
                    processed[0]++;
                    if (Files.getLastModifiedTime(backupFile).toInstant().isBefore(cutoffTime)) {
                        Files.delete(backupFile);
                        deleted[0]++;
                    }
                } catch (IOException e) {
                    errors[0]++;
                    logger.warn("Failed to process backup file: {}", backupFile, e);
                }
            });

            cleanupInfo.put("filesProcessed", processed[0]);
            cleanupInfo.put("filesDeleted", deleted[0]);
            cleanupInfo.put("errors", errors[0]);
            cleanupInfo.put("message", "Backup cleanup completed");

        } catch (Exception e) {
            cleanupInfo.put("error", "Failed to cleanup backups: " + e.getMessage());
            logger.warn("Failed to cleanup backups: {}", e.getMessage());
        }

        return cleanupInfo;
    }
}
