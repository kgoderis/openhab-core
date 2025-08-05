package org.openhab.core.ai.actions.scripts;

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
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for updating existing openHAB Scripts with validation and backup.
 * 
 * Updates script files with proper validation, syntax checking, backup creation,
 * and metadata preservation using real file system integration.
 */
@Component(service = Action.class, immediate = true)
public class UpdateScriptAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(UpdateScriptAction.class);
    private static final String ACTION_ID = "openhab.scripts.update";
    private static final String ACTION_NAME = "Update Script";

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
        return "Updates existing openHAB Scripts with validation, backup, and metadata preservation";
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
                "Path to the script file to update (relative to scripts directory or absolute)", "required", true));
        properties.put("scriptContent",
                Map.of("type", "string", "description", "New script content to write to the file", "required", true));
        properties.put("createBackup", Map.of("type", "boolean", "description",
                "Create a backup of the original file before updating", "default", true));
        properties.put("backupSuffix", Map.of("type", "string", "description",
                "Suffix for backup files (e.g., '.bak', '.backup')", "default", ".bak"));
        properties.put("validateSyntax",
                Map.of("type", "boolean", "description", "Validate script syntax before updating", "default", true));
        properties.put("preserveMetadata",
                Map.of("type", "boolean", "description", "Preserve existing metadata comments", "default", true));
        properties.put("updateMetadata",
                Map.of("type", "boolean", "description", "Update modification timestamp in metadata", "default", true));
        properties.put("forceUpdate",
                Map.of("type", "boolean", "description", "Force update even if validation fails", "default", false));

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
        properties.put("scriptInfo", Map.of("type", "object"));
        properties.put("validation", Map.of("type", "object"));
        properties.put("changes", Map.of("type", "object"));
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

        String scriptContent = (String) parameters.get("scriptContent");
        if (scriptContent == null || scriptContent.trim().isEmpty()) {
            return ActionValidationResult.invalid(List.of("scriptContent is required and cannot be empty"));
        }

        // Validate path format
        try {
            Paths.get(scriptPath);
        } catch (Exception e) {
            return ActionValidationResult.invalid(List.of("Invalid scriptPath format: " + scriptPath));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing update script action with parameters: {}", parameters);

        try {
            String scriptPath = (String) parameters.get("scriptPath");
            String scriptContent = (String) parameters.get("scriptContent");
            boolean createBackup = (Boolean) parameters.getOrDefault("createBackup", true);
            String backupSuffix = (String) parameters.getOrDefault("backupSuffix", ".bak");
            boolean validateSyntax = (Boolean) parameters.getOrDefault("validateSyntax", true);
            boolean preserveMetadata = (Boolean) parameters.getOrDefault("preserveMetadata", true);
            boolean updateMetadata = (Boolean) parameters.getOrDefault("updateMetadata", true);
            boolean forceUpdate = (Boolean) parameters.getOrDefault("forceUpdate", false);

            Map<String, Object> result = updateScript(scriptPath, scriptContent, createBackup, backupSuffix,
                    validateSyntax, preserveMetadata, updateMetadata, forceUpdate);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Update script action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to update script", e);
            throw new ActionException(ACTION_ID, "Failed to update script: " + e.getMessage(), e);
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
        return ActionMetadata.builder().version(getVersion())
                .description("Updates existing scripts with validation and backup").build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "sandboxing", false, "timeout", false);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("UpdateScriptAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("UpdateScriptAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> updateScript(String scriptPath, String scriptContent, boolean createBackup,
            String backupSuffix, boolean validateSyntax, boolean preserveMetadata, boolean updateMetadata,
            boolean forceUpdate) throws ActionException, IOException {

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

            if (!Files.isWritable(path)) {
                result.put("success", false);
                result.put("error", "Script file not writable: " + path);
                return result;
            }

            // Read original content
            String originalContent = Files.readString(path);
            String originalMetadata = extractMetadata(originalContent);

            // Prepare new content
            String finalContent = scriptContent;
            if (preserveMetadata && originalMetadata != null) {
                if (updateMetadata) {
                    finalContent = updateMetadataTimestamp(originalMetadata) + "\n\n" + scriptContent;
                } else {
                    finalContent = originalMetadata + "\n\n" + scriptContent;
                }
            }

            // Validate syntax if requested
            Map<String, Object> validation = new HashMap<>();
            if (validateSyntax) {
                String scriptType = determineScriptType(path.getFileName().toString());
                validation = validateScriptSyntax(finalContent, scriptType);
                result.put("validation", validation);

                if (!(Boolean) validation.get("valid") && !forceUpdate) {
                    result.put("success", false);
                    result.put("error", "Script syntax validation failed: " + validation.get("error"));
                    return result;
                }
            }

            // Create backup if requested
            String backupPath = null;
            if (createBackup) {
                backupPath = createBackupFile(path, backupSuffix);
                result.put("backupPath", backupPath);
            }

            // Write updated script file
            Files.writeString(path, finalContent);

            // Build script information
            Map<String, Object> scriptInfo = new HashMap<>();
            scriptInfo.put("name", path.getFileName().toString());
            scriptInfo.put("path", path.toString());
            scriptInfo.put("type", determineScriptType(path.getFileName().toString()));
            scriptInfo.put("extension", getFileExtension(path.getFileName().toString()));
            scriptInfo.put("size", Files.size(path));
            scriptInfo.put("sizeFormatted", formatBytes(Files.size(path)));
            scriptInfo.put("lastModified", Files.getLastModifiedTime(path).toInstant().toString());
            scriptInfo.put("readable", Files.isReadable(path));
            scriptInfo.put("writable", Files.isWritable(path));
            scriptInfo.put("contentLength", finalContent.length());
            scriptInfo.put("lines", finalContent.split("\r\n|\r|\n").length);

            // Build changes information
            Map<String, Object> changes = new HashMap<>();
            changes.put("originalSize", originalContent.length());
            changes.put("newSize", finalContent.length());
            changes.put("sizeDifference", finalContent.length() - originalContent.length());
            changes.put("originalLines", originalContent.split("\r\n|\r|\n").length);
            changes.put("newLines", finalContent.split("\r\n|\r|\n").length);
            changes.put("linesDifference",
                    finalContent.split("\r\n|\r|\n").length - originalContent.split("\r\n|\r|\n").length);
            changes.put("backupCreated", backupPath != null);
            changes.put("metadataPreserved", preserveMetadata && originalMetadata != null);
            changes.put("metadataUpdated", updateMetadata && preserveMetadata && originalMetadata != null);

            result.put("success", true);
            result.put("scriptPath", path.toString());
            result.put("scriptInfo", scriptInfo);
            result.put("changes", changes);
            result.put("message", "Script updated successfully");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to update script: " + e.getMessage());
            logger.warn("Failed to update script: {}", e.getMessage());
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

    private String extractMetadata(String content) {
        String[] lines = content.split("\r\n|\r|\n");
        StringBuilder metadata = new StringBuilder();

        for (String line : lines) {
            if (line.trim().startsWith("//") || line.trim().startsWith("#")) {
                metadata.append(line).append("\n");
            } else if (line.trim().isEmpty() && metadata.length() > 0) {
                metadata.append(line).append("\n");
            } else if (line.trim().isEmpty()) {
                continue;
            } else {
                break; // Stop at first non-comment, non-empty line
            }
        }

        return metadata.length() > 0 ? metadata.toString().trim() : null;
    }

    private String updateMetadataTimestamp(String metadata) {
        String timestamp = Instant.now().toString();
        String[] lines = metadata.split("\r\n|\r|\n");
        StringBuilder updatedMetadata = new StringBuilder();

        for (String line : lines) {
            if (line.contains("Created:") || line.contains("Modified:")) {
                // Update existing timestamp
                if (line.contains("Created:")) {
                    updatedMetadata.append(line).append("\n");
                } else {
                    updatedMetadata.append(line.replaceFirst("Modified:.*", "Modified: " + timestamp)).append("\n");
                }
            } else {
                updatedMetadata.append(line).append("\n");
            }
        }

        // Add modification timestamp if not present
        if (!metadata.contains("Modified:")) {
            updatedMetadata.append("// Modified: " + timestamp).append("\n");
        }

        return updatedMetadata.toString().trim();
    }

    private String createBackupFile(Path originalPath, String backupSuffix) throws IOException {
        String backupFileName = originalPath.getFileName().toString() + backupSuffix;
        Path backupPath = originalPath.resolveSibling(backupFileName);

        // Ensure backup suffix doesn't create invalid filename
        if (backupPath.equals(originalPath)) {
            backupFileName = originalPath.getFileName().toString() + ".backup";
            backupPath = originalPath.resolveSibling(backupFileName);
        }

        Files.copy(originalPath, backupPath);
        return backupPath.toString();
    }

    private Map<String, Object> validateScriptSyntax(String content, String scriptType) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", true);
        validation.put("scriptType", scriptType);
        validation.put("timestamp", Instant.now().toString());

        try {
            // Basic syntax validation based on script type
            switch (scriptType) {
                case "javascript" -> validateJavaScriptSyntax(content, validation);
                case "python" -> validatePythonSyntax(content, validation);
                case "groovy" -> validateGroovySyntax(content, validation);
                case "ruby" -> validateRubySyntax(content, validation);
                default -> {
                    validation.put("valid", true);
                    validation.put("message", "Syntax validation not implemented for type: " + scriptType);
                }
            }
        } catch (Exception e) {
            validation.put("valid", false);
            validation.put("error", "Syntax validation failed: " + e.getMessage());
        }

        return validation;
    }

    private void validateJavaScriptSyntax(String content, Map<String, Object> validation) {
        // Basic JavaScript syntax checks
        if (content.contains("function") && !content.contains("(")) {
            validation.put("valid", false);
            validation.put("error", "Function declaration missing parentheses");
            return;
        }

        // Check for balanced braces
        long openBraces = content.chars().filter(ch -> ch == '{').count();
        long closeBraces = content.chars().filter(ch -> ch == '}').count();
        if (openBraces != closeBraces) {
            validation.put("valid", false);
            validation.put("error", "Unbalanced braces: " + openBraces + " open, " + closeBraces + " close");
            return;
        }

        validation.put("valid", true);
        validation.put("message", "JavaScript syntax appears valid");
    }

    private void validatePythonSyntax(String content, Map<String, Object> validation) {
        // Basic Python syntax checks
        if (content.contains("def ") && !content.contains(":")) {
            validation.put("valid", false);
            validation.put("error", "Function definition missing colon");
            return;
        }

        // Check for balanced parentheses
        long openParens = content.chars().filter(ch -> ch == '(').count();
        long closeParens = content.chars().filter(ch -> ch == ')').count();
        if (openParens != closeParens) {
            validation.put("valid", false);
            validation.put("error", "Unbalanced parentheses: " + openParens + " open, " + closeParens + " close");
            return;
        }

        validation.put("valid", true);
        validation.put("message", "Python syntax appears valid");
    }

    private void validateGroovySyntax(String content, Map<String, Object> validation) {
        // Basic Groovy syntax checks
        if (content.contains("def ") && !content.contains("(")) {
            validation.put("valid", false);
            validation.put("error", "Method definition missing parentheses");
            return;
        }

        // Check for balanced braces
        long openBraces = content.chars().filter(ch -> ch == '{').count();
        long closeBraces = content.chars().filter(ch -> ch == '}').count();
        if (openBraces != closeBraces) {
            validation.put("valid", false);
            validation.put("error", "Unbalanced braces: " + openBraces + " open, " + closeBraces + " close");
            return;
        }

        validation.put("valid", true);
        validation.put("message", "Groovy syntax appears valid");
    }

    private void validateRubySyntax(String content, Map<String, Object> validation) {
        // Basic Ruby syntax checks
        if (content.contains("def ") && !content.contains("end")) {
            validation.put("valid", false);
            validation.put("error", "Method definition missing 'end'");
            return;
        }

        // Check for balanced parentheses
        long openParens = content.chars().filter(ch -> ch == '(').count();
        long closeParens = content.chars().filter(ch -> ch == ')').count();
        if (openParens != closeParens) {
            validation.put("valid", false);
            validation.put("error", "Unbalanced parentheses: " + openParens + " open, " + closeParens + " close");
            return;
        }

        validation.put("valid", true);
        validation.put("message", "Ruby syntax appears valid");
    }
}
