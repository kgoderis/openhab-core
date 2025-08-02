package org.openhab.core.ai.common.actions.scripts;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

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
 * AIAction for retrieving available Script Engines and their capabilities.
 * 
 * Provides detailed information about available script engines, their features,
 * supported languages, and capabilities using real ScriptEngineManager integration.
 */
@Component(service = AIAction.class, immediate = true)
public class GetScriptEnginesAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetScriptEnginesAction.class);
    private static final String ACTION_ID = "openhab.scripts.engines";
    private static final String ACTION_NAME = "Get Script Engines";

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
        return "Retrieves available Script Engines and their capabilities";
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
        properties.put("includeCapabilities",
                Map.of("type", "boolean", "description", "Include detailed engine capabilities", "default", true));
        properties.put("includeExtensions",
                Map.of("type", "boolean", "description", "Include supported file extensions", "default", true));
        properties.put("includeMimeTypes",
                Map.of("type", "boolean", "description", "Include supported MIME types", "default", true));
        properties.put("filterByLanguage", Map.of("type", "string", "description",
                "Filter engines by specific language (e.g., 'javascript', 'python')"));
        properties.put("includeVersion",
                Map.of("type", "boolean", "description", "Include engine version information", "default", true));

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
        properties.put("engines", Map.of("type", "array"));
        properties.put("totalEngines", Map.of("type", "integer"));
        properties.put("supportedLanguages", Map.of("type", "array"));
        properties.put("error", Map.of("type", "string"));
        properties.put("timestamp", Map.of("type", "string"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return AIActionValidationResult.valid(new HashMap<>());
        }

        // Validate filterByLanguage if provided
        String filterLanguage = (String) parameters.get("filterByLanguage");
        if (filterLanguage != null && filterLanguage.trim().isEmpty()) {
            return AIActionValidationResult.invalid(List.of("filterByLanguage cannot be empty if provided"));
        }

        return AIActionValidationResult.valid(parameters);
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing get script engines action with parameters: {}", parameters);

        try {
            boolean includeCapabilities = (Boolean) parameters.getOrDefault("includeCapabilities", true);
            boolean includeExtensions = (Boolean) parameters.getOrDefault("includeExtensions", true);
            boolean includeMimeTypes = (Boolean) parameters.getOrDefault("includeMimeTypes", true);
            boolean includeVersion = (Boolean) parameters.getOrDefault("includeVersion", true);
            String filterLanguage = (String) parameters.get("filterByLanguage");

            Map<String, Object> result = getScriptEngines(includeCapabilities, includeExtensions, includeMimeTypes,
                    includeVersion, filterLanguage);

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Get script engines action completed in {}ms", executionTime);

            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to get script engines", e);
            throw new AIActionException(ACTION_ID, "Failed to get script engines: " + e.getMessage(), e);
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
                .description("Retrieves available script engines and their capabilities").build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", false, "pagination", false, "metadata", true, "async", true,
                "sandboxing", false, "timeout", false);
    }

    @Override
    public void initialize(AIActionContext context) {
        logger.debug("GetScriptEnginesAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetScriptEnginesAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true; // No external dependencies required
    }

    private Map<String, Object> getScriptEngines(boolean includeCapabilities, boolean includeExtensions,
            boolean includeMimeTypes, boolean includeVersion, String filterLanguage) {

        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", Instant.now().toString());

        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            List<ScriptEngineFactory> factories = manager.getEngineFactories();

            List<Map<String, Object>> engines = new ArrayList<>();
            List<String> supportedLanguages = new ArrayList<>();

            for (ScriptEngineFactory factory : factories) {
                String languageName = factory.getLanguageName();
                String languageVersion = factory.getLanguageVersion();

                // Apply language filter if specified
                if (filterLanguage != null && !languageName.toLowerCase().contains(filterLanguage.toLowerCase())) {
                    continue;
                }

                Map<String, Object> engineInfo = new HashMap<>();
                engineInfo.put("languageName", languageName);
                engineInfo.put("languageVersion", languageVersion);
                engineInfo.put("engineName", factory.getEngineName());
                engineInfo.put("engineVersion", factory.getEngineVersion());

                if (includeVersion) {
                    engineInfo.put("version", languageVersion);
                }

                if (includeExtensions) {
                    List<String> extensions = factory.getExtensions();
                    engineInfo.put("extensions", extensions);
                    engineInfo.put("extensionCount", extensions.size());
                }

                if (includeMimeTypes) {
                    List<String> mimeTypes = factory.getMimeTypes();
                    engineInfo.put("mimeTypes", mimeTypes);
                    engineInfo.put("mimeTypeCount", mimeTypes.size());
                }

                if (includeCapabilities) {
                    Map<String, Object> capabilities = new HashMap<>();
                    capabilities.put("threading", factory.getParameter("THREADING"));
                    capabilities.put("compiled", factory.getParameter("COMPILED"));
                    capabilities.put("names", factory.getNames());
                    capabilities.put("parameterCount", factory.getParameter("THREADING") != null ? 1 : 0);
                    engineInfo.put("capabilities", capabilities);
                }

                // Add common script type mapping
                String scriptType = mapLanguageToScriptType(languageName);
                engineInfo.put("scriptType", scriptType);
                engineInfo.put("isOpenHABSupported", isOpenHABSupported(languageName));

                engines.add(engineInfo);
                supportedLanguages.add(languageName);
            }

            result.put("success", true);
            result.put("engines", engines);
            result.put("totalEngines", engines.size());
            result.put("supportedLanguages", supportedLanguages);
            result.put("filterApplied", filterLanguage != null);
            result.put("filterLanguage", filterLanguage);

            // Add summary information
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalEngines", engines.size());
            summary.put("openHABSupported",
                    engines.stream().mapToInt(e -> (Boolean) e.get("isOpenHABSupported") ? 1 : 0).sum());
            summary.put("javascriptEngines",
                    engines.stream().mapToInt(e -> "javascript".equals(e.get("scriptType")) ? 1 : 0).sum());
            summary.put("pythonEngines",
                    engines.stream().mapToInt(e -> "python".equals(e.get("scriptType")) ? 1 : 0).sum());
            summary.put("groovyEngines",
                    engines.stream().mapToInt(e -> "groovy".equals(e.get("scriptType")) ? 1 : 0).sum());
            summary.put("rubyEngines",
                    engines.stream().mapToInt(e -> "ruby".equals(e.get("scriptType")) ? 1 : 0).sum());
            result.put("summary", summary);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to retrieve script engines: " + e.getMessage());
            logger.warn("Failed to retrieve script engines: {}", e.getMessage());
        }

        return result;
    }

    private String mapLanguageToScriptType(String languageName) {
        String lowerName = languageName.toLowerCase();
        if (lowerName.contains("javascript") || lowerName.contains("js") || lowerName.contains("ecmascript")) {
            return "javascript";
        } else if (lowerName.contains("python") || lowerName.contains("jython")) {
            return "python";
        } else if (lowerName.contains("groovy")) {
            return "groovy";
        } else if (lowerName.contains("ruby") || lowerName.contains("jruby")) {
            return "ruby";
        } else if (lowerName.contains("java")) {
            return "java";
        } else if (lowerName.contains("php")) {
            return "php";
        } else if (lowerName.contains("perl")) {
            return "perl";
        } else if (lowerName.contains("tcl")) {
            return "tcl";
        } else {
            return "unknown";
        }
    }

    private boolean isOpenHABSupported(String languageName) {
        String lowerName = languageName.toLowerCase();
        return lowerName.contains("javascript") || lowerName.contains("js") || lowerName.contains("ecmascript")
                || lowerName.contains("python") || lowerName.contains("jython") || lowerName.contains("groovy")
                || lowerName.contains("ruby") || lowerName.contains("jruby");
    }
}
