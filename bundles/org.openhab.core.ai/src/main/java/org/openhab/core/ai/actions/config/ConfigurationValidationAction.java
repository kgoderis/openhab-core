package org.openhab.core.ai.actions.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * AI Action for comprehensive validation of openHAB configurations.
 * 
 * 
 */
@Component(service = Action.class, immediate = true)
public class ConfigurationValidationAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationValidationAction.class);

    private static final String ACTION_ID = "openhab.config.validate";
    private static final String ACTION_NAME = "Configuration Validation";
    private static final String DESCRIPTION = "Performs comprehensive validation of openHAB configuration files including syntax checking, cross-references, and best practices analysis";
    private static final String CATEGORY = "config";
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
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties",
                Map.of("validationType",
                        Map.of("type", "string", "enum", List.of("syntax", "references", "bestpractices", "all"),
                                "description", "Type of validation to perform", "default", "all"),
                        "configType",
                        Map.of("type", "string", "enum",
                                List.of("items", "things", "rules", "scripts", "sitemaps", "persistence", "transforms",
                                        "services", "all"),
                                "description", "Type of configuration to validate", "default", "all"),
                        "configName",
                        Map.of("type", "string", "description", "Specific configuration file name to validate"),
                        "content",
                        Map.of("type", "string", "description",
                                "Configuration content to validate (instead of reading from file)"),
                        "strictMode", Map.of("type", "boolean", "description",
                                "Enable strict validation with additional checks", "default", false)));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties",
                Map.of("validationResults", Map.of("type", "array", "items", Map.of("type", "object")), "summary",
                        Map.of("type", "object"), "totalFiles", Map.of("type", "integer"), "totalIssues",
                        Map.of("type", "integer"), "executionTime", Map.of("type", "integer")));
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        String validationType = (String) parameters.getOrDefault("validationType", "all");
        String configType = (String) parameters.getOrDefault("configType", "all");

        List<String> validValidationTypes = List.of("syntax", "references", "bestpractices", "all");
        if (!validValidationTypes.contains(validationType)) {
            return ActionValidationResult.invalid(List.of("Invalid validationType: " + validationType
                    + ". Must be one of: " + String.join(", ", validValidationTypes)));
        }

        List<String> validConfigTypes = List.of("items", "things", "rules", "scripts", "sitemaps", "persistence",
                "transforms", "services", "all");
        if (!validConfigTypes.contains(configType)) {
            return ActionValidationResult.invalid(List.of(
                    "Invalid configType: " + configType + ". Must be one of: " + String.join(", ", validConfigTypes)));
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();

        try {
            logger.debug("Executing configuration validation action with parameters: {}", parameters);

            String validationType = (String) parameters.getOrDefault("validationType", "all");
            String configType = (String) parameters.getOrDefault("configType", "all");
            String configName = (String) parameters.get("configName");
            String content = (String) parameters.get("content");
            boolean strictMode = (Boolean) parameters.getOrDefault("strictMode", false);

            Map<String, Object> validationResults = performValidation(validationType, configType, configName, content,
                    strictMode);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Configuration validation action completed in {}ms", executionTime);

            return ActionResult.success(validationResults, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Configuration validation action failed", e);
            throw new ActionException(ACTION_ID, "Failed to validate configuration: " + e.getMessage(), e);
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
                .description("Provides comprehensive configuration validation for openHAB")
                .tags(List.of("validation", "configuration", "syntax", "best-practices"))
                .documentation(
                        "Validates openHAB configuration files for syntax errors, cross-references, and best practices")
                .examples(List.of(
                        "Validate all configuration files: {\"validationType\": \"all\", \"configType\": \"all\"}",
                        "Validate specific file: {\"configName\": \"items.conf\", \"validationType\": \"syntax\"}",
                        "Validate content: {\"content\": \"Group Test\", \"configType\": \"items\", \"validationType\": \"all\"}"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("validation", true, "syntax_checking", true, "reference_validation", true, "best_practices", true,
                "strict_mode", true, "async", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("ConfigurationValidationAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ConfigurationValidationAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private Map<String, Object> performValidation(String validationType, String configType, String configName,
            String content, boolean strictMode) throws IOException {
        Map<String, Object> result = new HashMap<>();

        if (content != null) {
            // Validate provided content
            Map<String, Object> validationResult = validateContent(content, configType,
                    configName != null ? configName : "unknown", validationType, strictMode);
            result.put("validationResults", List.of(validationResult));
        } else if (configName != null) {
            // Validate specific file
            Map<String, Object> validationResult = validateConfigurationFile(configName, configType, validationType,
                    strictMode);
            result.put("validationResults", List.of(validationResult));
        } else {
            // Validate all files of specified type
            List<Map<String, Object>> validationResults = validateConfigurationFiles(configType, validationType,
                    strictMode);
            result.put("validationResults", validationResults);
        }

        // Calculate summary
        Map<String, Object> summary = calculateValidationSummary(
                (List<Map<String, Object>>) result.get("validationResults"));
        result.put("summary", summary);
        result.put("totalFiles", ((List<Map<String, Object>>) result.get("validationResults")).size());
        result.put("totalIssues", summary.get("totalIssues"));

        return result;
    }

    private List<Map<String, Object>> validateConfigurationFiles(String configType, String validationType,
            boolean strictMode) throws IOException {
        List<Map<String, Object>> results = new ArrayList<>();
        Path confPath = Paths.get(OpenHAB.getConfigFolder());

        if ("all".equals(configType)) {
            // Validate all configuration types
            List<String> allTypes = List.of("items", "things", "rules", "scripts", "sitemaps", "persistence",
                    "transforms", "services");
            for (String type : allTypes) {
                results.addAll(validateFilesOfType(confPath, type, validationType, strictMode));
            }
        } else {
            results.addAll(validateFilesOfType(confPath, configType, validationType, strictMode));
        }

        return results;
    }

    private List<Map<String, Object>> validateFilesOfType(Path confPath, String configType, String validationType,
            boolean strictMode) throws IOException {
        List<Map<String, Object>> results = new ArrayList<>();
        Path typePath = confPath.resolve(configType);

        if (!Files.exists(typePath)) {
            return results;
        }

        try (Stream<Path> paths = Files.walk(typePath, 1)) {
            paths.filter(Files::isRegularFile).filter(path -> !path.getFileName().toString().startsWith("."))
                    .forEach(path -> {
                        try {
                            Map<String, Object> result = validateConfigurationFile(path.getFileName().toString(),
                                    configType, validationType, strictMode);
                            results.add(result);
                        } catch (IOException e) {
                            logger.warn("Failed to validate file: {}", path, e);
                            Map<String, Object> errorResult = new HashMap<>();
                            errorResult.put("fileName", path.getFileName().toString());
                            errorResult.put("configType", configType);
                            errorResult.put("valid", false);
                            errorResult.put("issues", List.of(Map.of("severity", "error", "line", 0, "message",
                                    "Failed to read file: " + e.getMessage())));
                            results.add(errorResult);
                        }
                    });
        }

        return results;
    }

    private Map<String, Object> validateConfigurationFile(String configName, String configType, String validationType,
            boolean strictMode) throws IOException {
        Path confPath = Paths.get(OpenHAB.getConfigFolder());
        Path filePath = confPath.resolve(configType).resolve(configName);

        if (!Files.exists(filePath)) {
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", configName);
            result.put("configType", configType);
            result.put("valid", false);
            result.put("issues", List.of(Map.of("severity", "error", "line", 0, "message", "File not found")));
            return result;
        }

        String content = Files.readString(filePath);
        return validateContent(content, configType, configName, validationType, strictMode);
    }

    private Map<String, Object> validateContent(String content, String configType, String fileName,
            String validationType, boolean strictMode) {
        Map<String, Object> result = new HashMap<>();
        result.put("fileName", fileName);
        result.put("configType", configType);
        result.put("contentLength", content.length());

        List<Map<String, Object>> issues = new ArrayList<>();

        if ("all".equals(validationType) || "syntax".equals(validationType)) {
            issues.addAll(performSyntaxValidation(content, configType, strictMode));
        }

        if ("all".equals(validationType) || "references".equals(validationType)) {
            issues.addAll(performReferenceValidation(content, configType, strictMode));
        }

        if ("all".equals(validationType) || "bestpractices".equals(validationType)) {
            issues.addAll(performBestPracticesValidation(content, configType, strictMode));
        }

        result.put("issues", issues);
        result.put("valid", issues.stream().noneMatch(issue -> "error".equals(issue.get("severity"))));
        result.put("issueCount", issues.size());

        return result;
    }

    private List<Map<String, Object>> performSyntaxValidation(String content, String configType, boolean strictMode) {
        List<Map<String, Object>> issues = new ArrayList<>();

        switch (configType) {
            case "items":
                validateItemsSyntax(content, issues, strictMode);
                break;
            case "things":
                validateThingsSyntax(content, issues, strictMode);
                break;
            case "rules":
                validateRulesSyntax(content, issues, strictMode);
                break;
            case "sitemaps":
                validateSitemapSyntax(content, issues, strictMode);
                break;
            case "persistence":
                validatePersistenceSyntax(content, issues, strictMode);
                break;
            default:
                validateGenericSyntax(content, issues, strictMode);
        }

        return issues;
    }

    private List<Map<String, Object>> performReferenceValidation(String content, String configType,
            boolean strictMode) {
        List<Map<String, Object>> issues = new ArrayList<>();
        // Basic reference validation - check for common patterns
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//"))
                continue;

            // Check for potential broken references
            if (line.contains("Thing") && !line.contains("UID")) {
                createIssue(issues, "warning", i + 1, "Potential missing Thing UID", line);
            }
        }

        return issues;
    }

    private List<Map<String, Object>> performBestPracticesValidation(String content, String configType,
            boolean strictMode) {
        List<Map<String, Object>> issues = new ArrayList<>();

        checkCommentingPractices(content, issues, strictMode);
        checkNamingConventions(content, configType, issues, strictMode);
        checkFileStructure(content, configType, issues, strictMode);

        return issues;
    }

    private void validateItemsSyntax(String content, List<Map<String, Object>> issues, boolean strictMode) {
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//"))
                continue;

            // Basic item syntax validation
            if (!line.contains(" ") && !line.startsWith("Group")) {
                createIssue(issues, "error", i + 1, "Invalid item syntax: missing type", line);
            }

            if (line.contains("Group") && !line.contains("(")) {
                createIssue(issues, "warning", i + 1, "Group should specify member types", line);
            }
        }
    }

    private void validateThingsSyntax(String content, List<Map<String, Object>> issues, boolean strictMode) {
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//"))
                continue;

            // Basic thing syntax validation
            if (!line.contains("Thing") && !line.contains("Bridge")) {
                createIssue(issues, "error", i + 1, "Invalid thing syntax: missing Thing/Bridge keyword", line);
            }

            if (line.contains("Thing") && !line.contains("UID")) {
                createIssue(issues, "error", i + 1, "Thing missing UID", line);
            }
        }
    }

    private void validateRulesSyntax(String content, List<Map<String, Object>> issues, boolean strictMode) {
        String[] lines = content.split("\n");
        boolean inRule = false;
        int ruleStart = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//"))
                continue;

            if (line.startsWith("rule")) {
                if (inRule) {
                    createIssue(issues, "warning", i + 1, "Nested rule definition", line);
                }
                inRule = true;
                ruleStart = i + 1;

                if (!line.contains("\"") || !line.contains("when")) {
                    createIssue(issues, "error", i + 1, "Invalid rule syntax: missing name or when clause", line);
                }
            } else if (line.equals("end") && inRule) {
                inRule = false;
            }
        }

        if (inRule) {
            createIssue(issues, "error", ruleStart, "Unclosed rule definition", "");
        }
    }

    private void validateSitemapSyntax(String content, List<Map<String, Object>> issues, boolean strictMode) {
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//"))
                continue;

            if (line.startsWith("sitemap") && !line.contains("label=")) {
                createIssue(issues, "warning", i + 1, "Sitemap should have a label", line);
            }
        }
    }

    private void validatePersistenceSyntax(String content, List<Map<String, Object>> issues, boolean strictMode) {
        // Basic persistence syntax validation
        if (!content.contains("Strategies") && !content.contains("Items")) {
            createIssue(issues, "warning", 1, "Persistence configuration should include Strategies and Items sections",
                    "");
        }
    }

    private void validateGenericSyntax(String content, List<Map<String, Object>> issues, boolean strictMode) {
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//"))
                continue;

            // Check for common syntax issues
            if (line.contains("=") && !line.contains(" ") && !line.contains("\"")) {
                createIssue(issues, "warning", i + 1, "Potential syntax issue: missing spaces around equals", line);
            }
        }
    }

    private void checkCommentingPractices(String content, List<Map<String, Object>> issues, boolean strictMode) {
        String[] lines = content.split("\n");
        int totalLines = lines.length;
        int commentLines = 0;

        for (String line : lines) {
            if (line.trim().startsWith("//")) {
                commentLines++;
            }
        }

        double commentRatio = (double) commentLines / totalLines;
        if (commentRatio < 0.1 && strictMode) {
            createIssue(issues, "info", 1, "Low comment ratio: consider adding more documentation",
                    String.format("Comment ratio: %.1f%%", commentRatio * 100));
        }
    }

    private void checkNamingConventions(String content, String configType, List<Map<String, Object>> issues,
            boolean strictMode) {
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("//"))
                continue;

            // Check for naming conventions
            if (line.contains("Thing") && line.contains("UID")) {
                String uid = extractUID(line);
                if (uid != null && !uid.matches("^[a-zA-Z0-9_-]+:[a-zA-Z0-9_-]+$")) {
                    createIssue(issues, "warning", i + 1, "Thing UID should follow binding:thing format", line);
                }
            }
        }
    }

    private void checkFileStructure(String content, String configType, List<Map<String, Object>> issues,
            boolean strictMode) {
        if (content.length() > 10000 && !content.contains("//")) {
            createIssue(issues, "info", 1, "Large file without comments: consider adding documentation",
                    "File size: " + content.length() + " characters");
        }
    }

    private String extractUID(String line) {
        // Simple UID extraction - look for pattern after "Thing"
        int thingIndex = line.indexOf("Thing");
        if (thingIndex != -1) {
            String afterThing = line.substring(thingIndex + 5).trim();
            String[] parts = afterThing.split("\\s+");
            if (parts.length > 0) {
                return parts[0];
            }
        }
        return null;
    }

    private void createIssue(List<Map<String, Object>> issues, String severity, int line, String message,
            String context) {
        Map<String, Object> issue = new HashMap<>();
        issue.put("severity", severity);
        issue.put("line", line);
        issue.put("message", message);
        if (context != null && !context.isEmpty()) {
            issue.put("context", context);
        }
        issues.add(issue);
    }

    private Map<String, Object> calculateValidationSummary(List<Map<String, Object>> validationResults) {
        Map<String, Object> summary = new HashMap<>();
        int totalFiles = validationResults.size();
        int validFiles = 0;
        int totalIssues = 0;
        int errorCount = 0;
        int warningCount = 0;
        int infoCount = 0;

        for (Map<String, Object> result : validationResults) {
            if ((Boolean) result.get("valid")) {
                validFiles++;
            }

            List<Map<String, Object>> issues = (List<Map<String, Object>>) result.get("issues");
            totalIssues += issues.size();

            for (Map<String, Object> issue : issues) {
                String severity = (String) issue.get("severity");
                switch (severity) {
                    case "error":
                        errorCount++;
                        break;
                    case "warning":
                        warningCount++;
                        break;
                    case "info":
                        infoCount++;
                        break;
                }
            }
        }

        summary.put("totalFiles", totalFiles);
        summary.put("validFiles", validFiles);
        summary.put("invalidFiles", totalFiles - validFiles);
        summary.put("totalIssues", totalIssues);
        summary.put("errorCount", errorCount);
        summary.put("warningCount", warningCount);
        summary.put("infoCount", infoCount);
        summary.put("validationRate", totalFiles > 0 ? (double) validFiles / totalFiles : 1.0);

        return summary;
    }
}
