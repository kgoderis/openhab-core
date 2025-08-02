package org.openhab.core.ai.common.actions.scripts;

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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openhab.core.OpenHAB;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIAction for validating openHAB Scripts with comprehensive syntax and content analysis.
 * 
 * Validates script files with syntax checking, content analysis, and security validation
 * using real ScriptEngine integration and file system operations.
 */
@Component(service = AIAction.class, immediate = true)
public class ValidateScriptAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(ValidateScriptAction.class);
    private static final String ACTION_ID = "openhab.scripts.validate";
    private static final String ACTION_NAME = "Validate Script";

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
        return "Validates openHAB Scripts with comprehensive syntax and content analysis";
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
                "Path to the script file to validate (relative to scripts directory or absolute)"));
        properties.put("scriptContent", Map.of("type", "string", "description",
                "Script content to validate directly (alternative to scriptPath)"));
        properties.put("scriptType",
                Map.of("type", "string", "enum", List.of("javascript", "python", "ruby", "groovy", "jsr223"),
                        "description", "Script language/type (auto-detected if not specified)"));
        properties.put("validateSyntax",
                Map.of("type", "boolean", "description", "Validate script syntax", "default", true));
        properties.put("validateSecurity",
                Map.of("type", "boolean", "description", "Check for security issues", "default", true));
        properties.put("validateStyle",
                Map.of("type", "boolean", "description", "Check code style and best practices", "default", false));
        properties.put("validateDependencies",
                Map.of("type", "boolean", "description", "Check for missing dependencies", "default", false));
        properties.put("strictMode",
                Map.of("type", "boolean", "description", "Enable strict validation mode", "default", false));

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
        properties.put("valid", Map.of("type", "boolean"));
        properties.put("scriptInfo", Map.of("type", "object"));
        properties.put("validationResults", Map.of("type", "object"));
        properties.put("issues", Map.of("type", "array"));
        properties.put("warnings", Map.of("type", "array"));
        properties.put("recommendations", Map.of("type", "array"));
        properties.put("error", Map.of("type", "string"));
        properties.put("timestamp", Map.of("type", "string"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return AIActionValidationResult.invalid(List.of("Parameters cannot be null or empty"));
        }

        String scriptPath = (String) parameters.get("scriptPath");
        String scriptContent = (String) parameters.get("scriptContent");

        if (scriptPath == null && scriptContent == null) {
            return AIActionValidationResult.invalid(List.of("Either scriptPath or scriptContent must be provided"));
        }

        if (scriptPath != null && scriptContent != null) {
            return AIActionValidationResult.invalid(List.of("Cannot specify both scriptPath and scriptContent"));
        }

        // Validate path format if provided
        if (scriptPath != null) {
            try {
                Paths.get(scriptPath);
            } catch (Exception e) {
                return AIActionValidationResult.invalid(List.of("Invalid scriptPath format: " + scriptPath));
            }
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing validate script action with parameters: {}", parameters);

        try {
            String scriptPath = (String) parameters.get("scriptPath");
            String scriptContent = (String) parameters.get("scriptContent");
            String scriptType = (String) parameters.get("scriptType");
            boolean validateSyntax = (Boolean) parameters.getOrDefault("validateSyntax", true);
            boolean validateSecurity = (Boolean) parameters.getOrDefault("validateSecurity", true);
            boolean validateStyle = (Boolean) parameters.getOrDefault("validateStyle", false);
            boolean validateDependencies = (Boolean) parameters.getOrDefault("validateDependencies", false);
            boolean strictMode = (Boolean) parameters.getOrDefault("strictMode", false);

            Map<String, Object> result = validateScript(scriptPath, scriptContent, scriptType, validateSyntax,
                    validateSecurity, validateStyle, validateDependencies, strictMode);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Validate script action completed in {}ms", executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to validate script", e);
            throw new AIActionException(ACTION_ID, "Failed to validate script: " + e.getMessage(), e);
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
        return AIActionMetadata.builder().version(getVersion())
                .description("Validates scripts with comprehensive analysis").build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "sandboxing", false, "timeout", false);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("ValidateScriptAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ValidateScriptAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> validateScript(String scriptPath, String scriptContent, String scriptType,
            boolean validateSyntax, boolean validateSecurity, boolean validateStyle, boolean validateDependencies,
            boolean strictMode) throws AIActionException, IOException {

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());

        try {
            // Get script content and determine type
            String actualContent;
            String actualType;
            Map<String, Object> scriptInfo = new HashMap<>();

            if (scriptContent != null) {
                actualContent = scriptContent;
                actualType = scriptType != null ? scriptType : "unknown";
                scriptInfo.put("source", "inline");
            } else {
                Path path = resolveScriptPath(scriptPath);
                if (!Files.exists(path)) {
                    result.put("success", false);
                    result.put("error", "Script file not found: " + path);
                    return result;
                }

                actualContent = Files.readString(path);
                actualType = scriptType != null ? scriptType : determineScriptType(path.getFileName().toString());
                scriptInfo.put("source", "file");
                scriptInfo.put("path", path.toString());
                scriptInfo.put("name", path.getFileName().toString());
                scriptInfo.put("size", Files.size(path));
                scriptInfo.put("lastModified", Files.getLastModifiedTime(path).toInstant().toString());
            }

            scriptInfo.put("type", actualType);
            scriptInfo.put("contentLength", actualContent.length());
            scriptInfo.put("lines", actualContent.split("\r\n|\r|\n").length);

            // Perform validation
            Map<String, Object> validationResults = new HashMap<>();
            List<Map<String, Object>> issues = new ArrayList<>();
            List<Map<String, Object>> warnings = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();

            // Syntax validation
            if (validateSyntax) {
                Map<String, Object> syntaxValidation = validateScriptSyntax(actualContent, actualType);
                validationResults.put("syntax", syntaxValidation);

                if (!(Boolean) syntaxValidation.get("valid")) {
                    Object error = syntaxValidation.get("error");
                    String errorStr = error != null ? error.toString() : "Unknown syntax error";
                    issues.add(Map.of("type", "syntax", "message", errorStr, "severity", "error"));
                }
            }

            // Security validation
            if (validateSecurity) {
                Map<String, Object> securityValidation = validateScriptSecurity(actualContent, actualType);
                validationResults.put("security", securityValidation);

                @SuppressWarnings("unchecked")
                List<String> securityIssues = (List<String>) securityValidation.get("issues");
                for (String issue : securityIssues) {
                    issues.add(Map.of("type", "security", "message", issue, "severity", "warning"));
                }
            }

            // Style validation
            if (validateStyle) {
                Map<String, Object> styleValidation = validateScriptStyle(actualContent, actualType);
                validationResults.put("style", styleValidation);

                @SuppressWarnings("unchecked")
                List<String> styleIssues = (List<String>) styleValidation.get("issues");
                for (String issue : styleIssues) {
                    warnings.add(Map.of("type", "style", "message", issue, "severity", "info"));
                }
            }

            // Dependency validation
            if (validateDependencies) {
                Map<String, Object> dependencyValidation = validateScriptDependencies(actualContent, actualType);
                validationResults.put("dependencies", dependencyValidation);

                @SuppressWarnings("unchecked")
                List<String> dependencyIssues = (List<String>) dependencyValidation.get("issues");
                for (String issue : dependencyIssues) {
                    warnings.add(Map.of("type", "dependency", "message", issue, "severity", "warning"));
                }
            }

            // Overall validation result
            boolean isValid = issues.stream().noneMatch(issue -> "error".equals(issue.get("severity")));
            if (strictMode) {
                isValid = isValid && warnings.isEmpty();
            }

            result.put("success", true);
            result.put("valid", isValid);
            result.put("scriptInfo", scriptInfo);
            result.put("validationResults", validationResults);
            result.put("issues", issues);
            result.put("warnings", warnings);
            result.put("recommendations", recommendations);
            result.put("issueCount", issues.size());
            result.put("warningCount", warnings.size());

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to validate script: " + e.getMessage());
            logger.warn("Failed to validate script: {}", e.getMessage());
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

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private Map<String, Object> validateScriptSyntax(String content, String scriptType) {
        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", true);
        validation.put("scriptType", scriptType);
        validation.put("timestamp", Instant.now().toString());

        try {
            // Try to compile/parse the script using ScriptEngine
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName(scriptType);

            if (engine != null) {
                try {
                    // Attempt to compile the script
                    engine.eval(content);
                    validation.put("valid", true);
                    validation.put("message", "Script syntax is valid");
                } catch (ScriptException e) {
                    validation.put("valid", false);
                    validation.put("error", "Script compilation failed: " + e.getMessage());
                    validation.put("lineNumber", e.getLineNumber());
                    validation.put("columnNumber", e.getColumnNumber());
                }
            } else {
                // Fallback to basic syntax validation
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
            }
        } catch (Exception e) {
            validation.put("valid", false);
            validation.put("error", "Syntax validation failed: " + e.getMessage());
        }

        return validation;
    }

    private Map<String, Object> validateScriptSecurity(String content, String scriptType) {
        Map<String, Object> validation = new HashMap<>();
        List<String> issues = new ArrayList<>();

        // Check for potentially dangerous patterns
        String lowerContent = content.toLowerCase();

        // File system access
        if (lowerContent.contains("filesystem") || lowerContent.contains("file.") || lowerContent.contains("path.")) {
            issues.add("Potential file system access detected");
        }

        // Network access
        if (lowerContent.contains("http") || lowerContent.contains("url") || lowerContent.contains("socket")) {
            issues.add("Potential network access detected");
        }

        // System commands
        if (lowerContent.contains("exec") || lowerContent.contains("system") || lowerContent.contains("runtime")) {
            issues.add("Potential system command execution detected");
        }

        // Eval usage
        if (lowerContent.contains("eval(") || lowerContent.contains("settimeout")
                || lowerContent.contains("setinterval")) {
            issues.add("Dynamic code execution detected");
        }

        // Database access
        if (lowerContent.contains("database") || lowerContent.contains("sql") || lowerContent.contains("jdbc")) {
            issues.add("Database access detected");
        }

        validation.put("issues", issues);
        validation.put("issueCount", issues.size());
        validation.put("secure", issues.isEmpty());

        return validation;
    }

    private Map<String, Object> validateScriptStyle(String content, String scriptType) {
        Map<String, Object> validation = new HashMap<>();
        List<String> issues = new ArrayList<>();

        String[] lines = content.split("\r\n|\r|\n");

        // Check line length
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].length() > 120) {
                issues.add("Line " + (i + 1) + " exceeds 120 characters");
            }
        }

        // Check for consistent indentation
        if (content.contains("\t") && content.contains("    ")) {
            issues.add("Mixed indentation (tabs and spaces) detected");
        }

        // Check for trailing whitespace
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].endsWith(" ") || lines[i].endsWith("\t")) {
                issues.add("Line " + (i + 1) + " has trailing whitespace");
            }
        }

        validation.put("issues", issues);
        validation.put("issueCount", issues.size());
        validation.put("styleScore", Math.max(0, 100 - issues.size() * 5));

        return validation;
    }

    private Map<String, Object> validateScriptDependencies(String content, String scriptType) {
        Map<String, Object> validation = new HashMap<>();
        List<String> issues = new ArrayList<>();

        // Check for common dependency patterns
        if (scriptType.equals("javascript")) {
            if (content.contains("require(") && !content.contains("// @ts-ignore")) {
                issues.add("Node.js require() detected - may not work in browser environment");
            }
            if (content.contains("import ") && !content.contains("// @ts-ignore")) {
                issues.add("ES6 import detected - may not work in older environments");
            }
        }

        if (scriptType.equals("python")) {
            if (content.contains("import ") && !content.contains("import logging")) {
                issues.add("External imports detected - ensure dependencies are available");
            }
        }

        validation.put("issues", issues);
        validation.put("issueCount", issues.size());
        validation.put("dependenciesFound", !issues.isEmpty());

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
