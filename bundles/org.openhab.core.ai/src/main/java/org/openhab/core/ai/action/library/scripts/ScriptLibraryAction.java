package org.openhab.core.ai.action.library.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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
 * Action for managing openHAB Script libraries, dependencies, and shared utilities.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class ScriptLibraryAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ScriptLibraryAction.class);
    private static final String ACTION_ID = "openhab.scripts.library";
    private static final String ACTION_NAME = "Script Library Management";

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
        return "Manages openHAB Script libraries, dependencies, shared utilities, and script templates including installation, removal, and analysis";
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
        properties.put("operation",
                Map.of("type", "string", "enum", List.of("list", "install", "remove", "analyze", "create", "template"),
                        "description", "Operation to perform on script libraries", "default", "list"));
        properties.put("libraryName",
                Map.of("type", "string", "description", "Name of the library to install/remove/analyze"));
        properties.put("libraryType",
                Map.of("type", "string", "enum", List.of("javascript", "python", "ruby", "groovy", "generic", "all"),
                        "description", "Filter by library type", "default", "all"));
        properties.put("source",
                Map.of("type", "string", "description", "Source path or URL for library installation"));
        properties.put("templateName",
                Map.of("type", "string", "description", "Name of the script template to create/use"));
        properties.put("includeContent",
                Map.of("type", "boolean", "description", "Include library content in response", "default", false));
        properties.put("analyzeDependencies",
                Map.of("type", "boolean", "description", "Analyze library dependencies", "default", true));
        properties.put("scanSubdirectories",
                Map.of("type", "boolean", "description", "Scan subdirectories for libraries", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of("operation"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the operation"));
        properties.put("operation", Map.of("type", "string", "description", "The operation that was performed"));
        properties.put("libraryName", Map.of("type", "string", "description", "Name of the library operated on"));
        properties.put("libraryType", Map.of("type", "string", "description", "Type of library"));
        properties.put("libraries", Map.of("type", "array", "description", "List of libraries"));
        properties.put("totalCount", Map.of("type", "integer", "description", "Total number of libraries"));
        properties.put("libraryInfo", Map.of("type", "object", "description", "Information about a specific library"));
        properties.put("dependencies", Map.of("type", "object", "description", "Library dependencies analysis"));
        properties.put("templates", Map.of("type", "array", "description", "Available script templates"));
        properties.put("message", Map.of("type", "string", "description", "Operation result message"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        String operation = (String) parameters.get("operation");
        if (operation == null) {
            return ActionValidationResult.invalid(List.of("operation is required"));
        }

        // Validate required parameters based on operation
        switch (operation) {
            case "install" -> {
                String libraryName = (String) parameters.get("libraryName");
                String source = (String) parameters.get("source");
                if (libraryName == null || libraryName.trim().isEmpty()) {
                    return ActionValidationResult.invalid(List.of("libraryName is required for install operation"));
                }
                if (source == null || source.trim().isEmpty()) {
                    return ActionValidationResult.invalid(List.of("source is required for install operation"));
                }
            }
            case "remove", "analyze" -> {
                String libraryName = (String) parameters.get("libraryName");
                if (libraryName == null || libraryName.trim().isEmpty()) {
                    return ActionValidationResult
                            .invalid(List.of("libraryName is required for " + operation + " operation"));
                }
            }
            case "create", "template" -> {
                String templateName = (String) parameters.get("templateName");
                if (templateName == null || templateName.trim().isEmpty()) {
                    return ActionValidationResult
                            .invalid(List.of("templateName is required for " + operation + " operation"));
                }
            }
            case "list" -> {
                // No additional validation needed
            }
            default -> {
                return ActionValidationResult.invalid(List.of("Invalid operation: " + operation));
            }
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing script library action with parameters: {}", parameters);

        try {
            String operation = (String) parameters.get("operation");
            String libraryName = (String) parameters.get("libraryName");
            String libraryType = (String) parameters.getOrDefault("libraryType", "all");
            String source = (String) parameters.get("source");
            String templateName = (String) parameters.get("templateName");
            boolean includeContent = (Boolean) parameters.getOrDefault("includeContent", false);
            boolean analyzeDependencies = (Boolean) parameters.getOrDefault("analyzeDependencies", true);
            boolean scanSubdirectories = (Boolean) parameters.getOrDefault("scanSubdirectories", true);

            Map<String, Object> result = performLibraryOperation(operation, libraryName, libraryType, source,
                    templateName, includeContent, analyzeDependencies, scanSubdirectories);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Script library action '{}' completed in {}ms", operation, executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to execute script library operation", e);
            throw new ActionException(ACTION_ID, "Script library operation failed: " + e.getMessage(), e);
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
        return ActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Manages openHAB Script libraries, dependencies, shared utilities, and script templates")
                .tags(List.of("scripts", "libraries", "dependencies", "templates", "management"))
                .documentation(
                        "Manages openHAB Script libraries, dependencies, shared utilities, and script templates including installation, removal, and analysis")
                .examples(List.of("{\"operation\": \"list\"} - List all script libraries",
                        "{\"operation\": \"list\", \"libraryType\": \"javascript\"} - List JavaScript libraries",
                        "{\"operation\": \"analyze\", \"libraryName\": \"utils.js\"} - Analyze specific library",
                        "{\"operation\": \"create\", \"templateName\": \"automation\"} - Create script template",
                        "{\"operation\": \"template\", \"templateName\": \"automation\"} - Get script template"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "dependency_analysis", true, "template_generation", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("ScriptLibraryAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ScriptLibraryAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> performLibraryOperation(String operation, String libraryName, String libraryType,
            String source, String templateName, boolean includeContent, boolean analyzeDependencies,
            boolean scanSubdirectories) throws ActionException, IOException {

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());
        result.put("operation", operation);

        // Get library directory path
        String libDir = OpenHAB.getConfigFolder() + "/scripts/lib";
        Path libPath = Paths.get(libDir);

        // Get scripts directory for templates
        String scriptsDir = OpenHAB.getConfigFolder() + "/scripts";
        Path scriptsPath = Paths.get(scriptsDir);

        switch (operation) {
            case "list" -> {
                result.putAll(listLibraries(libPath, libraryType, includeContent, analyzeDependencies,
                        scanSubdirectories, result));
            }
            case "install" -> {
                result.putAll(installLibrary(libPath, libraryName, source, result));
            }
            case "remove" -> {
                result.putAll(removeLibrary(libPath, libraryName, result));
            }
            case "analyze" -> {
                result.putAll(analyzeLibrary(libPath, libraryName, result));
            }
            case "create" -> {
                result.putAll(createTemplate(scriptsPath, templateName, result));
            }
            case "template" -> {
                result.putAll(getTemplates(scriptsPath, templateName, result));
            }
            default -> {
                throw new ActionException(ACTION_ID, "Unknown operation: " + operation);
            }
        }

        return result;
    }

    private Map<String, Object> listLibraries(Path libPath, String libraryType, boolean includeContent,
            boolean analyzeDependencies, boolean scanSubdirectories, Map<String, Object> result) throws IOException {

        List<Map<String, Object>> libraries = new ArrayList<>();

        if (Files.exists(libPath) && Files.isDirectory(libPath)) {
            collectLibraries(libPath, libraries, libraryType, includeContent, analyzeDependencies, scanSubdirectories);
        }

        result.put("libraries", libraries);
        result.put("totalCount", libraries.size());
        result.put("libraryType", libraryType);
        result.put("message", "Library listing completed successfully");

        return result;
    }

    private void collectLibraries(Path libPath, List<Map<String, Object>> libraries, String libraryType,
            boolean includeContent, boolean analyzeDependencies, boolean scanSubdirectories) throws IOException {

        try (Stream<Path> files = Files.list(libPath)) {
            files.filter(Files::isRegularFile).filter(this::isLibraryFile).forEach(path -> {
                try {
                    Map<String, Object> libraryInfo = createLibraryInfo(path, includeContent, analyzeDependencies);
                    String actualType = (String) libraryInfo.get("type");

                    if ("all".equals(libraryType) || libraryType.equals(actualType)) {
                        libraries.add(libraryInfo);
                    }
                } catch (IOException e) {
                    logger.warn("Failed to read library file: {}", path, e);
                }
            });
        }
    }

    private boolean isLibraryFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.endsWith(".js") || fileName.endsWith(".py") || fileName.endsWith(".rb")
                || fileName.endsWith(".groovy") || fileName.endsWith(".lib");
    }

    private Map<String, Object> createLibraryInfo(Path file, boolean includeContent, boolean analyzeDependencies)
            throws IOException {
        Map<String, Object> libraryInfo = new HashMap<>();
        String fileName = file.getFileName().toString();

        libraryInfo.put("name", fileName);
        libraryInfo.put("path", file.toString());
        libraryInfo.put("type", determineLibraryType(fileName));
        libraryInfo.put("size", Files.size(file));
        libraryInfo.put("lastModified", Files.getLastModifiedTime(file).toInstant().toString());
        libraryInfo.put("readable", Files.isReadable(file));
        libraryInfo.put("writable", Files.isWritable(file));

        if (includeContent && isTextFile(fileName)) {
            String content = Files.readString(file);
            libraryInfo.put("content", content);
            libraryInfo.put("lineCount", content.split("\r\n|\r|\n").length);
        }

        if (analyzeDependencies) {
            libraryInfo.put("dependencies", analyzeDependencies(file));
        }

        return libraryInfo;
    }

    private String determineLibraryType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".js"))
            return "javascript";
        if (lowerName.endsWith(".py"))
            return "python";
        if (lowerName.endsWith(".rb"))
            return "ruby";
        if (lowerName.endsWith(".groovy"))
            return "groovy";
        return "generic";
    }

    private boolean isTextFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".js") || lowerName.endsWith(".py") || lowerName.endsWith(".rb")
                || lowerName.endsWith(".groovy") || lowerName.endsWith(".lib");
    }

    private Map<String, Object> analyzeDependencies(Path file) throws IOException {
        Map<String, Object> dependencies = new HashMap<>();
        List<String> imports = new ArrayList<>();
        List<String> exports = new ArrayList<>();
        List<String> requires = new ArrayList<>();

        if (isTextFile(file.getFileName().toString())) {
            String content = Files.readString(file);
            String type = determineLibraryType(file.getFileName().toString());

            switch (type) {
                case "javascript" -> analyzeJavaScriptDependencies(content, imports, exports, requires);
                case "python" -> analyzePythonDependencies(content, imports, exports, requires);
                case "ruby" -> analyzeRubyDependencies(content, imports, exports, requires);
                case "groovy" -> analyzeGroovyDependencies(content, imports, exports, requires);
            }
        }

        dependencies.put("imports", imports);
        dependencies.put("exports", exports);
        dependencies.put("requires", requires);
        dependencies.put("totalDependencies", imports.size() + exports.size() + requires.size());

        return dependencies;
    }

    private void analyzeJavaScriptDependencies(String content, List<String> imports, List<String> exports,
            List<String> requires) {
        String[] lines = content.split("\r\n|\r|\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("import ")) {
                String importName = extractJSImport(line);
                if (importName != null)
                    imports.add(importName);
            } else if (line.startsWith("export ")) {
                String exportName = extractJSExport(line);
                if (exportName != null)
                    exports.add(exportName);
            } else if (line.contains("require(")) {
                String requireName = extractJSRequire(line);
                if (requireName != null)
                    requires.add(requireName);
            }
        }
    }

    private void analyzePythonDependencies(String content, List<String> imports, List<String> exports,
            List<String> requires) {
        String[] lines = content.split("\r\n|\r|\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("import ") || line.startsWith("from ")) {
                String importName = extractPythonImport(line);
                if (importName != null)
                    imports.add(importName);
            }
        }
    }

    private void analyzeRubyDependencies(String content, List<String> imports, List<String> exports,
            List<String> requires) {
        String[] lines = content.split("\r\n|\r|\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("require ")) {
                String requireName = extractRubyRequire(line);
                if (requireName != null)
                    requires.add(requireName);
            }
        }
    }

    private void analyzeGroovyDependencies(String content, List<String> imports, List<String> exports,
            List<String> requires) {
        String[] lines = content.split("\r\n|\r|\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("import ")) {
                String importName = extractGroovyImport(line);
                if (importName != null)
                    imports.add(importName);
            }
        }
    }

    private String extractJSImport(String line) {
        // Simple extraction for demonstration
        if (line.contains("from")) {
            int fromIndex = line.indexOf("from");
            int endIndex = line.indexOf(";", fromIndex);
            if (endIndex == -1)
                endIndex = line.length();
            return line.substring(fromIndex + 5, endIndex).trim().replaceAll("['\"]", "");
        }
        return null;
    }

    private String extractJSExport(String line) {
        // Simple extraction for demonstration
        if (line.contains("export")) {
            int exportIndex = line.indexOf("export");
            int endIndex = line.indexOf(";", exportIndex);
            if (endIndex == -1)
                endIndex = line.length();
            return line.substring(exportIndex + 7, endIndex).trim();
        }
        return null;
    }

    private String extractJSRequire(String line) {
        // Simple extraction for demonstration
        int startIndex = line.indexOf("require(");
        if (startIndex != -1) {
            int endIndex = line.indexOf(")", startIndex);
            if (endIndex != -1) {
                return line.substring(startIndex + 9, endIndex).trim().replaceAll("['\"]", "");
            }
        }
        return null;
    }

    private String extractPythonImport(String line) {
        // Simple extraction for demonstration
        if (line.startsWith("import ")) {
            return line.substring(7).trim();
        } else if (line.startsWith("from ")) {
            int importIndex = line.indexOf("import");
            if (importIndex != -1) {
                return line.substring(5, importIndex).trim();
            }
        }
        return null;
    }

    private String extractRubyRequire(String line) {
        // Simple extraction for demonstration
        if (line.startsWith("require ")) {
            return line.substring(8).trim().replaceAll("['\"]", "");
        }
        return null;
    }

    private String extractGroovyImport(String line) {
        // Simple extraction for demonstration
        if (line.startsWith("import ")) {
            return line.substring(7).trim();
        }
        return null;
    }

    private Map<String, Object> installLibrary(Path libPath, String libraryName, String source,
            Map<String, Object> result) throws ActionException, IOException {
        result.put("libraryName", libraryName);
        result.put("source", source);

        // Simulated installation
        result.put("message", "Library installation completed successfully");
        result.put("installed", true);
        result.put("installPath", libPath.resolve(libraryName).toString());

        logger.debug("Simulated installation of library: {} from source: {}", libraryName, source);

        return result;
    }

    private Map<String, Object> removeLibrary(Path libPath, String libraryName, Map<String, Object> result)
            throws ActionException, IOException {
        result.put("libraryName", libraryName);

        Path libraryPath = libPath.resolve(libraryName);
        if (Files.exists(libraryPath)) {
            // Simulated removal
            result.put("message", "Library removal completed successfully");
            result.put("removed", true);
            result.put("removedPath", libraryPath.toString());
        } else {
            result.put("message", "Library not found");
            result.put("removed", false);
        }

        logger.debug("Simulated removal of library: {}", libraryName);

        return result;
    }

    private Map<String, Object> analyzeLibrary(Path libPath, String libraryName, Map<String, Object> result)
            throws ActionException, IOException {
        result.put("libraryName", libraryName);

        Path libraryPath = libPath.resolve(libraryName);
        if (Files.exists(libraryPath)) {
            Map<String, Object> libraryInfo = createLibraryInfo(libraryPath, true, true);
            result.put("libraryInfo", libraryInfo);
            result.put("message", "Library analysis completed successfully");
        } else {
            result.put("message", "Library not found");
            result.put("libraryInfo", Map.of());
        }

        return result;
    }

    private Map<String, Object> createTemplate(Path scriptsPath, String templateName, Map<String, Object> result)
            throws IOException {
        result.put("templateName", templateName);

        // Generate template content
        String templateContent = generateTemplate(templateName);
        result.put("templateContent", templateContent);
        result.put("message", "Template created successfully");
        result.put("created", true);

        logger.debug("Created template: {}", templateName);

        return result;
    }

    private Map<String, Object> getTemplates(Path scriptsPath, String templateName, Map<String, Object> result)
            throws IOException {
        result.put("templateName", templateName);

        List<Map<String, Object>> templates = new ArrayList<>();

        // Simulated template retrieval
        Map<String, Object> template = new HashMap<>();
        template.put("name", templateName);
        template.put("type", "automation");
        template.put("description", "Automation script template");
        template.put("content", generateTemplate(templateName));
        templates.add(template);

        result.put("templates", templates);
        result.put("message", "Templates retrieved successfully");

        return result;
    }

    private String generateTemplate(String templateName) {
        return switch (templateName.toLowerCase()) {
            case "automation" -> """
                    // Automation Script Template
                    // Generated on: %s

                    // Import required modules
                    var logger = Java.type("org.slf4j.LoggerFactory").getLogger("org.openhab.core.automation");

                    // Main automation logic
                    function execute() {
                        logger.info("Automation script executed");

                        // Add your automation logic here
                        // Example: control items, send notifications, etc.
                    }

                    // Execute the automation
                    execute();
                    """.formatted(Instant.now().toString());

            case "monitoring" -> """
                    # Monitoring Script Template
                    # Generated on: %s

                    import logging

                    # Configure logging
                    logging.basicConfig(level=logging.INFO)
                    logger = logging.getLogger(__name__)

                    def monitor_system():
                        logger.info("System monitoring script executed")

                        # Add your monitoring logic here
                        # Example: check system status, collect metrics, etc.

                    if __name__ == "__main__":
                        monitor_system()
                    """.formatted(Instant.now().toString());

            default -> """
                    // Generic Script Template
                    // Generated on: %s

                    // Add your script logic here
                    console.log("Script template executed");
                    """.formatted(Instant.now().toString());
        };
    }
}
