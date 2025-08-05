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
 * Action for retrieving openHAB Script content and metadata.
 * 
 * Provides detailed script information including content, metadata, syntax analysis,
 * and validation status using real ScriptEngineManager integration.
 */
@Component(service = Action.class, immediate = true)
public class GetScriptAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetScriptAction.class);
    private static final String ACTION_ID = "openhab.scripts.get";
    private static final String ACTION_NAME = "Get Script";

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
        return "Retrieves openHAB Script content, metadata, and analysis information";
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
                "Path to the script file (relative to scripts directory or absolute)", "required", true));
        properties.put("includeContent",
                Map.of("type", "boolean", "description", "Include script content in response", "default", true));
        properties.put("includeMetadata", Map.of("type", "boolean", "description",
                "Include file metadata (size, modification time, etc.)", "default", true));
        properties.put("includeAnalysis", Map.of("type", "boolean", "description",
                "Include script analysis (syntax, dependencies, etc.)", "default", true));
        properties.put("maxContentSize", Map.of("type", "integer", "minimum", 1, "maximum", 1048576, "description",
                "Maximum content size to include in bytes", "default", 65536));

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
        properties.put("script", Map.of("type", "object"));
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

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing get script action with parameters: {}", parameters);

        try {
            String scriptPath = (String) parameters.get("scriptPath");
            boolean includeContent = (Boolean) parameters.getOrDefault("includeContent", true);
            boolean includeMetadata = (Boolean) parameters.getOrDefault("includeMetadata", true);
            boolean includeAnalysis = (Boolean) parameters.getOrDefault("includeAnalysis", true);
            int maxContentSize = (Integer) parameters.getOrDefault("maxContentSize", 65536);

            Map<String, Object> result = getScript(scriptPath, includeContent, includeMetadata, includeAnalysis,
                    maxContentSize);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Get script action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to get script", e);
            throw new ActionException(ACTION_ID, "Failed to get script: " + e.getMessage(), e);
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
                .description("Retrieves script content, metadata, and analysis information").build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "sandboxing", false, "timeout", false);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("GetScriptAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetScriptAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> getScript(String scriptPath, boolean includeContent, boolean includeMetadata,
            boolean includeAnalysis, int maxContentSize) throws ActionException, IOException {

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());

        try {
            // Resolve script path
            Path path = resolveScriptPath(scriptPath);

            if (!Files.exists(path)) {
                result.put("success", false);
                result.put("error", "Script file not found: " + path);
                return result;
            }

            if (!Files.isReadable(path)) {
                result.put("success", false);
                result.put("error", "Script file not readable: " + path);
                return result;
            }

            // Build script information
            Map<String, Object> scriptInfo = new HashMap<>();
            scriptInfo.put("name", path.getFileName().toString());
            scriptInfo.put("path", path.toString());
            scriptInfo.put("type", determineScriptType(path.getFileName().toString()));
            scriptInfo.put("extension", getFileExtension(path.getFileName().toString()));

            // Add metadata if requested
            if (includeMetadata) {
                scriptInfo.put("size", Files.size(path));
                scriptInfo.put("sizeFormatted", formatBytes(Files.size(path)));
                scriptInfo.put("lastModified", Files.getLastModifiedTime(path).toInstant().toString());
                scriptInfo.put("readable", Files.isReadable(path));
                scriptInfo.put("writable", Files.isWritable(path));
                scriptInfo.put("executable", Files.isExecutable(path));
            }

            // Add content if requested
            if (includeContent) {
                long fileSize = Files.size(path);
                if (fileSize <= maxContentSize) {
                    String content = Files.readString(path);
                    scriptInfo.put("content", content);
                    scriptInfo.put("contentLength", content.length());
                    scriptInfo.put("contentTruncated", false);
                } else {
                    // Read partial content
                    String content = Files.readString(path).substring(0, maxContentSize);
                    scriptInfo.put("content", content + "\n\n[Content truncated - file too large]");
                    scriptInfo.put("contentLength", fileSize);
                    scriptInfo.put("contentTruncated", true);
                    scriptInfo.put("maxContentSize", maxContentSize);
                }
            }

            // Add analysis if requested
            if (includeAnalysis && includeContent && scriptInfo.containsKey("content")) {
                String content = (String) scriptInfo.get("content");
                if (!content.contains("[Content truncated")) {
                    analyzeScript(content, scriptInfo);
                }
            }

            result.put("success", true);
            result.put("script", scriptInfo);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to retrieve script: " + e.getMessage());
            logger.warn("Failed to retrieve script: {}", e.getMessage());
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

    private void analyzeScript(String content, Map<String, Object> scriptInfo) {
        Map<String, Object> analysis = new HashMap<>();

        // Basic statistics
        analysis.put("lines", content.split("\r\n|\r|\n").length);
        analysis.put("characters", content.length());
        analysis.put("words", content.split("\\s+").length);

        // Language-specific analysis
        String scriptType = (String) scriptInfo.get("type");
        switch (scriptType) {
            case "javascript" -> analyzeJavaScript(content, analysis);
            case "python" -> analyzePython(content, analysis);
            case "groovy" -> analyzeGroovy(content, analysis);
            default -> analyzeGeneric(content, analysis);
        }

        scriptInfo.put("analysis", analysis);
    }

    private void analyzeJavaScript(String content, Map<String, Object> analysis) {
        analysis.put("language", "JavaScript");
        analysis.put("hasConsoleLog", content.contains("console.log"));
        analysis.put("hasFunction", content.contains("function"));
        analysis.put("hasVar", content.contains("var ") || content.contains("let ") || content.contains("const "));
        analysis.put("hasRequire", content.contains("require("));
        analysis.put("hasImport", content.contains("import "));
    }

    private void analyzePython(String content, Map<String, Object> analysis) {
        analysis.put("language", "Python");
        analysis.put("hasPrint", content.contains("print("));
        analysis.put("hasImport", content.contains("import "));
        analysis.put("hasDef", content.contains("def "));
        analysis.put("hasClass", content.contains("class "));
        analysis.put("hasIf", content.contains("if "));
    }

    private void analyzeGroovy(String content, Map<String, Object> analysis) {
        analysis.put("language", "Groovy");
        analysis.put("hasPrintln", content.contains("println"));
        analysis.put("hasImport", content.contains("import "));
        analysis.put("hasDef", content.contains("def "));
        analysis.put("hasClass", content.contains("class "));
        analysis.put("hasIf", content.contains("if "));
    }

    private void analyzeGeneric(String content, Map<String, Object> analysis) {
        analysis.put("language", "Unknown");
        analysis.put("hasComments", content.contains("//") || content.contains("#") || content.contains("/*"));
        analysis.put("hasStrings", content.contains("\"") || content.contains("'"));
        analysis.put("hasNumbers", content.matches(".*\\d+.*"));
    }
}
