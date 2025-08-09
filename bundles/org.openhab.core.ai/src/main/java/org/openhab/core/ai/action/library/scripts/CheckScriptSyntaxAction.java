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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
 * Action for dedicated syntax checking of openHAB Scripts.
 * 
 * Provides focused syntax validation with detailed error reporting,
 * line-by-line analysis, and syntax suggestions using real ScriptEngine integration.
 */
@Component(service = Action.class, immediate = true)
public class CheckScriptSyntaxAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(CheckScriptSyntaxAction.class);
    private static final String ACTION_ID = "openhab.scripts.syntax";
    private static final String ACTION_NAME = "Check Script Syntax";

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
        return "Performs dedicated syntax checking of openHAB Scripts with detailed error reporting";
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
                "Path to the script file to check (relative to scripts directory or absolute)"));
        properties.put("scriptContent", Map.of("type", "string", "description",
                "Script content to check directly (alternative to scriptPath)"));
        properties.put("scriptType",
                Map.of("type", "string", "enum", List.of("javascript", "python", "ruby", "groovy", "jsr223"),
                        "description", "Script language/type (auto-detected if not specified)"));
        properties.put("includeWarnings",
                Map.of("type", "boolean", "description", "Include syntax warnings in results", "default", true));
        properties.put("includeSuggestions",
                Map.of("type", "boolean", "description", "Include syntax improvement suggestions", "default", true));
        properties.put("strictMode",
                Map.of("type", "boolean", "description", "Enable strict syntax checking", "default", false));
        properties.put("checkStyle",
                Map.of("type", "boolean", "description", "Include style-related syntax checks", "default", false));

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
        properties.put("syntaxErrors", Map.of("type", "array"));
        properties.put("syntaxWarnings", Map.of("type", "array"));
        properties.put("suggestions", Map.of("type", "array"));
        properties.put("lineAnalysis", Map.of("type", "array"));
        properties.put("summary", Map.of("type", "object"));
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
        String scriptContent = (String) parameters.get("scriptContent");

        if (scriptPath == null && scriptContent == null) {
            return ActionValidationResult.invalid(List.of("Either scriptPath or scriptContent must be provided"));
        }

        if (scriptPath != null && scriptContent != null) {
            return ActionValidationResult.invalid(List.of("Cannot specify both scriptPath and scriptContent"));
        }

        // Validate path format if provided
        if (scriptPath != null) {
            try {
                Paths.get(scriptPath);
            } catch (Exception e) {
                return ActionValidationResult.invalid(List.of("Invalid scriptPath format: " + scriptPath));
            }
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing check script syntax action with parameters: {}", parameters);

        try {
            String scriptPath = (String) parameters.get("scriptPath");
            String scriptContent = (String) parameters.get("scriptContent");
            String scriptType = (String) parameters.get("scriptType");
            boolean includeWarnings = (Boolean) parameters.getOrDefault("includeWarnings", true);
            boolean includeSuggestions = (Boolean) parameters.getOrDefault("includeSuggestions", true);
            boolean strictMode = (Boolean) parameters.getOrDefault("strictMode", false);
            boolean checkStyle = (Boolean) parameters.getOrDefault("checkStyle", false);

            Map<String, Object> result = checkScriptSyntax(scriptPath, scriptContent, scriptType, includeWarnings,
                    includeSuggestions, strictMode, checkStyle);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Check script syntax action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to check script syntax", e);
            throw new ActionException(ACTION_ID, "Failed to check script syntax: " + e.getMessage(), e);
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
                .description("Performs dedicated syntax checking with detailed error reporting").build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", false, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "sandboxing", false, "timeout", false);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("CheckScriptSyntaxAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("CheckScriptSyntaxAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> checkScriptSyntax(String scriptPath, String scriptContent, String scriptType,
            boolean includeWarnings, boolean includeSuggestions, boolean strictMode, boolean checkStyle)
            throws ActionException, IOException {

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

            // Perform syntax analysis
            List<Map<String, Object>> syntaxErrors = new ArrayList<>();
            List<Map<String, Object>> syntaxWarnings = new ArrayList<>();
            List<String> suggestions = new ArrayList<>();
            List<Map<String, Object>> lineAnalysis = new ArrayList<>();

            // Engine-based syntax checking
            Map<String, Object> engineCheck = performEngineSyntaxCheck(actualContent, actualType);
            if (engineCheck.containsKey("error")) {
                Object error = engineCheck.get("error");
                Object line = engineCheck.get("line");
                Object column = engineCheck.get("column");
                String errorStr = error != null ? error.toString() : "Unknown error";
                String lineStr = line != null ? line.toString() : "0";
                String columnStr = column != null ? column.toString() : "0";
                syntaxErrors.add(Map.of("type", "engine", "message", errorStr, "line", lineStr, "column", columnStr,
                        "severity", "error"));
            }

            // Pattern-based syntax checking
            Map<String, Object> patternCheck = performPatternSyntaxCheck(actualContent, actualType, strictMode);
            syntaxErrors.addAll((List<Map<String, Object>>) patternCheck.get("errors"));
            if (includeWarnings) {
                syntaxWarnings.addAll((List<Map<String, Object>>) patternCheck.get("warnings"));
            }

            // Line-by-line analysis
            lineAnalysis = performLineAnalysis(actualContent, actualType, checkStyle);

            // Generate suggestions
            if (includeSuggestions) {
                suggestions = generateSyntaxSuggestions(actualContent, actualType, syntaxErrors, syntaxWarnings);
            }

            // Build summary
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalErrors", syntaxErrors.size());
            summary.put("totalWarnings", syntaxWarnings.size());
            summary.put("totalSuggestions", suggestions.size());
            summary.put("valid", syntaxErrors.isEmpty());
            summary.put("linesAnalyzed", lineAnalysis.size());
            summary.put("syntaxScore", calculateSyntaxScore(syntaxErrors, syntaxWarnings, actualContent));

            result.put("success", true);
            result.put("valid", syntaxErrors.isEmpty());
            result.put("scriptInfo", scriptInfo);
            result.put("syntaxErrors", syntaxErrors);
            result.put("syntaxWarnings", syntaxWarnings);
            result.put("suggestions", suggestions);
            result.put("lineAnalysis", lineAnalysis);
            result.put("summary", summary);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to check script syntax: " + e.getMessage());
            logger.warn("Failed to check script syntax: {}", e.getMessage());
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

    private Map<String, Object> performEngineSyntaxCheck(String content, String scriptType) {
        Map<String, Object> result = new HashMap<>();

        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName(scriptType);

            if (engine != null) {
                try {
                    engine.eval(content);
                    result.put("valid", true);
                } catch (ScriptException e) {
                    result.put("valid", false);
                    result.put("error", "Script compilation failed: " + e.getMessage());
                    result.put("line", e.getLineNumber());
                    result.put("column", e.getColumnNumber());
                }
            } else {
                result.put("valid", true);
                result.put("message", "No engine available for type: " + scriptType);
            }
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", "Engine check failed: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> performPatternSyntaxCheck(String content, String scriptType, boolean strictMode) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        List<Map<String, Object>> warnings = new ArrayList<>();

        String[] lines = content.split("\r\n|\r|\n");

        switch (scriptType) {
            case "javascript" -> checkJavaScriptSyntax(lines, errors, warnings, strictMode);
            case "python" -> checkPythonSyntax(lines, errors, warnings, strictMode);
            case "groovy" -> checkGroovySyntax(lines, errors, warnings, strictMode);
            case "ruby" -> checkRubySyntax(lines, errors, warnings, strictMode);
            default -> {
                // Generic checks for unknown types
                checkGenericSyntax(lines, errors, warnings, strictMode);
            }
        }

        result.put("errors", errors);
        result.put("warnings", warnings);
        return result;
    }

    private void checkJavaScriptSyntax(String[] lines, List<Map<String, Object>> errors,
            List<Map<String, Object>> warnings, boolean strictMode) {
        int braceCount = 0;
        int parenCount = 0;
        int bracketCount = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;

            // Check for balanced braces
            for (char c : line.toCharArray()) {
                switch (c) {
                    case '{' -> braceCount++;
                    case '}' -> braceCount--;
                    case '(' -> parenCount++;
                    case ')' -> parenCount--;
                    case '[' -> bracketCount++;
                    case ']' -> bracketCount--;
                }
            }

            // Check for common JavaScript syntax issues
            if (line.contains("function") && !line.contains("(")) {
                errors.add(Map.of("line", lineNum, "message", "Function declaration missing parentheses", "type",
                        "syntax"));
            }

            if (line.contains("var ") && line.contains("=") && !line.contains(";") && !line.endsWith(";")) {
                warnings.add(Map.of("line", lineNum, "message", "Missing semicolon", "type", "style"));
            }

            if (line.contains("console.log") && strictMode) {
                warnings.add(Map.of("line", lineNum, "message", "Console.log statement found", "type", "debug"));
            }
        }

        // Check final brace counts
        if (braceCount != 0) {
            errors.add(Map.of("line", lines.length, "message", "Unbalanced braces: " + braceCount + " unclosed", "type",
                    "syntax"));
        }
        if (parenCount != 0) {
            errors.add(Map.of("line", lines.length, "message", "Unbalanced parentheses: " + parenCount + " unclosed",
                    "type", "syntax"));
        }
        if (bracketCount != 0) {
            errors.add(Map.of("line", lines.length, "message", "Unbalanced brackets: " + bracketCount + " unclosed",
                    "type", "syntax"));
        }
    }

    private void checkPythonSyntax(String[] lines, List<Map<String, Object>> errors, List<Map<String, Object>> warnings,
            boolean strictMode) {
        int indentLevel = 0;
        boolean inFunction = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;

            // Check indentation
            int currentIndent = getIndentLevel(line);
            if (currentIndent > indentLevel + 1) {
                errors.add(Map.of("line", lineNum, "message", "Unexpected indentation level", "type", "syntax"));
            }
            indentLevel = currentIndent;

            // Check for function definitions
            if (line.trim().startsWith("def ") && !line.contains(":")) {
                errors.add(Map.of("line", lineNum, "message", "Function definition missing colon", "type", "syntax"));
            }

            // Check for proper spacing
            if (line.contains("=") && !line.contains(" = ") && !line.contains("==")) {
                warnings.add(Map.of("line", lineNum, "message", "Missing spaces around assignment operator", "type",
                        "style"));
            }

            // Check for print statements in strict mode
            if (line.contains("print ") && strictMode) {
                warnings.add(Map.of("line", lineNum, "message", "Print statement found", "type", "debug"));
            }
        }
    }

    private void checkGroovySyntax(String[] lines, List<Map<String, Object>> errors, List<Map<String, Object>> warnings,
            boolean strictMode) {
        int braceCount = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;

            // Check for balanced braces
            for (char c : line.toCharArray()) {
                if (c == '{')
                    braceCount++;
                if (c == '}')
                    braceCount--;
            }

            // Check for method definitions
            if (line.contains("def ") && !line.contains("(")) {
                errors.add(
                        Map.of("line", lineNum, "message", "Method definition missing parentheses", "type", "syntax"));
            }

            // Check for println statements in strict mode
            if (line.contains("println") && strictMode) {
                warnings.add(Map.of("line", lineNum, "message", "Println statement found", "type", "debug"));
            }
        }

        if (braceCount != 0) {
            errors.add(Map.of("line", lines.length, "message", "Unbalanced braces: " + braceCount + " unclosed", "type",
                    "syntax"));
        }
    }

    private void checkRubySyntax(String[] lines, List<Map<String, Object>> errors, List<Map<String, Object>> warnings,
            boolean strictMode) {
        int defCount = 0;
        int endCount = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;

            if (line.trim().startsWith("def "))
                defCount++;
            if (line.trim() == "end")
                endCount++;

            // Check for method definitions
            if (line.contains("def ") && !line.contains("end")) {
                warnings.add(Map.of("line", lineNum, "message", "Method definition should have corresponding 'end'",
                        "type", "style"));
            }

            // Check for puts statements in strict mode
            if (line.contains("puts ") && strictMode) {
                warnings.add(Map.of("line", lineNum, "message", "Puts statement found", "type", "debug"));
            }
        }

        if (defCount != endCount) {
            errors.add(Map.of("line", lines.length, "message",
                    "Unbalanced def/end: " + defCount + " def, " + endCount + " end", "type", "syntax"));
        }
    }

    private void checkGenericSyntax(String[] lines, List<Map<String, Object>> errors,
            List<Map<String, Object>> warnings, boolean strictMode) {
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;

            // Check for balanced parentheses
            int openParens = 0;
            int closeParens = 0;
            for (char c : line.toCharArray()) {
                if (c == '(')
                    openParens++;
                if (c == ')')
                    closeParens++;
            }

            if (openParens != closeParens) {
                warnings.add(Map.of("line", lineNum, "message", "Unbalanced parentheses in line", "type", "style"));
            }

            // Check for long lines
            if (line.length() > 120) {
                warnings.add(Map.of("line", lineNum, "message", "Line exceeds 120 characters", "type", "style"));
            }
        }
    }

    private int getIndentLevel(String line) {
        int level = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ')
                level++;
            else if (c == '\t')
                level += 4;
            else
                break;
        }
        return level / 4; // Assuming 4 spaces per indent level
    }

    private List<Map<String, Object>> performLineAnalysis(String content, String scriptType, boolean checkStyle) {
        List<Map<String, Object>> analysis = new ArrayList<>();
        String[] lines = content.split("\r\n|\r|\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNum = i + 1;

            Map<String, Object> lineInfo = new HashMap<>();
            lineInfo.put("lineNumber", lineNum);
            lineInfo.put("content", line);
            lineInfo.put("length", line.length());
            lineInfo.put("isEmpty", line.trim().isEmpty());
            lineInfo.put("isComment", line.trim().startsWith("//") || line.trim().startsWith("#"));

            if (checkStyle) {
                Map<String, Object> styleInfo = new HashMap<>();
                styleInfo.put("hasTrailingWhitespace", line.endsWith(" ") || line.endsWith("\t"));
                styleInfo.put("indentLevel", getIndentLevel(line));
                styleInfo.put("isTooLong", line.length() > 120);
                lineInfo.put("style", styleInfo);
            }

            analysis.add(lineInfo);
        }

        return analysis;
    }

    private List<String> generateSyntaxSuggestions(String content, String scriptType, List<Map<String, Object>> errors,
            List<Map<String, Object>> warnings) {
        List<String> suggestions = new ArrayList<>();

        // Generate suggestions based on errors and warnings
        for (Map<String, Object> error : errors) {
            String message = (String) error.get("message");
            if (message.contains("missing parentheses")) {
                suggestions.add("Add parentheses to function/method declarations");
            } else if (message.contains("Unbalanced")) {
                suggestions.add("Check for matching opening and closing brackets/braces/parentheses");
            }
        }

        for (Map<String, Object> warning : warnings) {
            String message = (String) warning.get("message");
            if (message.contains("Missing semicolon")) {
                suggestions.add("Add semicolons at the end of statements");
            } else if (message.contains("spaces around")) {
                suggestions.add("Add spaces around operators for better readability");
            }
        }

        // Language-specific suggestions
        switch (scriptType) {
            case "javascript" -> {
                if (content.contains("var ")) {
                    suggestions.add("Consider using 'let' or 'const' instead of 'var' for better scoping");
                }
                if (content.contains("function(")) {
                    suggestions.add("Consider using arrow functions for shorter syntax");
                }
            }
            case "python" -> {
                if (content.contains("print ")) {
                    suggestions.add("Consider using logging instead of print statements");
                }
                if (content.contains("import *")) {
                    suggestions.add("Avoid wildcard imports - import specific modules");
                }
            }
            case "groovy" -> {
                if (content.contains("println")) {
                    suggestions.add("Consider using logger instead of println");
                }
            }
        }

        return suggestions;
    }

    private int calculateSyntaxScore(List<Map<String, Object>> errors, List<Map<String, Object>> warnings,
            String content) {
        int baseScore = 100;
        int errorPenalty = errors.size() * 10;
        int warningPenalty = warnings.size() * 2;
        int lengthPenalty = content.length() > 1000 ? 5 : 0;

        return Math.max(0, baseScore - errorPenalty - warningPenalty - lengthPenalty);
    }
}
