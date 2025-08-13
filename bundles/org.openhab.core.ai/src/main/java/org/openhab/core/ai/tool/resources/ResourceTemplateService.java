package org.openhab.core.ai.tool.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.api.ResourceContext;
import org.openhab.core.ai.tool.api.ResourceRegistry;
import org.openhab.core.ai.tool.api.ResourceResult;
import org.openhab.core.ai.tool.api.validation.ResourceValidationResult;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Template Service for MCP Resources
 * 
 * This service implements the resources/templates/list method for resource template discovery
 * with parameter validation, completion, and performance monitoring.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class ResourceTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceTemplateService.class);

    private final Map<String, ResourceTemplate> templates = new ConcurrentHashMap<>();
    private final AtomicLong totalTemplateRequests = new AtomicLong(0);
    private final AtomicLong totalTemplateCompletions = new AtomicLong(0);
    private final AtomicLong totalTemplateTime = new AtomicLong(0);

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable ResourceRegistry resourceRegistry;

    @Activate
    protected void activate() {
        logger.debug("Activating ResourceTemplateService");
        initializeTemplates();
    }

    @Deactivate
    protected void deactivate() {
        logger.debug("Deactivating ResourceTemplateService");
        templates.clear();
    }

    /**
     * Initialize default resource templates
     */
    private void initializeTemplates() {
        try {
            // Item templates
            createItemTemplates();

            // Thing templates
            createThingTemplates();

            // Rule templates
            createRuleTemplates();

            // Configuration templates
            createConfigurationTemplates();

            logger.info("Initialized {} resource templates", templates.size());
        } catch (Exception e) {
            logger.error("Failed to initialize resource templates", e);
        }
    }

    /**
     * Create item-related templates
     */
    private void createItemTemplates() {
        // Template for getting item state
        ResourceTemplate getItemTemplate = new ResourceTemplate("get-item-state", "Get Item State",
                "Template for retrieving the current state of an openHAB item", "items",
                Map.of("itemName", new TemplateParameter("itemName", "string", "The name of the item", true, null)),
                Map.of("action", "get"));
        templates.put("get-item-state", getItemTemplate);

        // Template for setting item state
        ResourceTemplate setItemTemplate = new ResourceTemplate("set-item-state", "Set Item State",
                "Template for setting the state of an openHAB item", "items",
                Map.of("itemName", new TemplateParameter("itemName", "string", "The name of the item", true, null),
                        "value", new TemplateParameter("value", "string", "The value to set", true, null)),
                Map.of("action", "set"));
        templates.put("set-item-state", setItemTemplate);
    }

    /**
     * Create thing-related templates
     */
    private void createThingTemplates() {
        // Template for getting thing status
        ResourceTemplate getThingTemplate = new ResourceTemplate("get-thing-status", "Get Thing Status",
                "Template for retrieving the status of an openHAB thing", "things",
                Map.of("thingUID", new TemplateParameter("thingUID", "string", "The UID of the thing", true, null)),
                Map.of("action", "get"));
        templates.put("get-thing-status", getThingTemplate);

        // Template for configuring thing
        ResourceTemplate configureThingTemplate = new ResourceTemplate("configure-thing", "Configure Thing",
                "Template for configuring an openHAB thing", "things",
                Map.of("thingUID", new TemplateParameter("thingUID", "string", "The UID of the thing", true, null),
                        "configuration",
                        new TemplateParameter("configuration", "object", "Configuration parameters", true, null)),
                Map.of("action", "configure"));
        templates.put("configure-thing", configureThingTemplate);
    }

    /**
     * Create rule-related templates
     */
    private void createRuleTemplates() {
        // Template for getting rule information
        ResourceTemplate getRuleTemplate = new ResourceTemplate("get-rule-info", "Get Rule Information",
                "Template for retrieving information about an openHAB rule", "rules",
                Map.of("ruleUID", new TemplateParameter("ruleUID", "string", "The UID of the rule", true, null)),
                Map.of("action", "get"));
        templates.put("get-rule-info", getRuleTemplate);

        // Template for enabling/disabling rule
        ResourceTemplate toggleRuleTemplate = new ResourceTemplate(
                "toggle-rule", "Toggle Rule", "Template for enabling or disabling an openHAB rule", "rules", Map
                        .of("ruleUID", new TemplateParameter("ruleUID", "string", "The UID of the rule", true, null),
                                "enabled", new TemplateParameter("enabled", "boolean",
                                        "Whether to enable or disable the rule", true, null)),
                Map.of("action", "enable"));
        templates.put("toggle-rule", toggleRuleTemplate);
    }

    /**
     * Create configuration-related templates
     */
    private void createConfigurationTemplates() {
        // Template for getting configuration
        ResourceTemplate getConfigTemplate = new ResourceTemplate("get-configuration", "Get Configuration",
                "Template for retrieving openHAB configuration", "configuration",
                Map.of("configPath",
                        new TemplateParameter("configPath", "string", "The configuration path", true, null)),
                Map.of("action", "get"));
        templates.put("get-configuration", getConfigTemplate);

        // Template for setting configuration
        ResourceTemplate setConfigTemplate = new ResourceTemplate("set-configuration", "Set Configuration",
                "Template for setting openHAB configuration", "configuration",
                Map.of("configPath",
                        new TemplateParameter("configPath", "string", "The configuration path", true, null), "value",
                        new TemplateParameter("value", "object", "The configuration value", true, null)),
                Map.of("action", "set"));
        templates.put("set-configuration", setConfigTemplate);
    }

    /**
     * List all available resource templates
     * 
     * @param context The execution context
     * @return The template list result
     */
    public ResourceResult listTemplates(ResourceContext context) {
        long startTime = System.currentTimeMillis();
        totalTemplateRequests.incrementAndGet();

        try {
            logger.debug("Listing resource templates");

            List<Map<String, Object>> templateList = new ArrayList<>();
            for (ResourceTemplate template : templates.values()) {
                templateList.add(template.toMap());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("templates", templateList);
            result.put("count", templateList.size());

            long executionTime = System.currentTimeMillis() - startTime;
            totalTemplateTime.addAndGet(executionTime);

            logger.debug("Template listing completed in {}ms", executionTime);
            return ResourceResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error listing templates", e);
            return ResourceResult.failure("Template listing error: " + e.getMessage(),
                    System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Get parameter completion suggestions for a template
     * 
     * @param templateId The template ID
     * @param partialParams The partial parameters
     * @param context The execution context
     * @return The completion suggestions result
     */
    public ResourceResult getParameterCompletions(String templateId, Map<String, Object> partialParams,
            ResourceContext context) {
        long startTime = System.currentTimeMillis();
        totalTemplateCompletions.incrementAndGet();

        try {
            logger.debug("Getting parameter completions for template: {} with params: {}", templateId, partialParams);

            ResourceTemplate template = templates.get(templateId);
            if (template == null) {
                return ResourceResult.failure("Template not found: " + templateId,
                        System.currentTimeMillis() - startTime);
            }

            Map<String, Object> completions = new HashMap<>();
            for (Map.Entry<String, TemplateParameter> entry : template.getParameters().entrySet()) {
                String paramName = entry.getKey();
                TemplateParameter param = entry.getValue();

                if (!partialParams.containsKey(paramName)) {
                    completions.put(paramName, Map.of("type", param.getType(), "description", param.getDescription(),
                            "required", param.isRequired(), "defaultValue", param.getDefaultValue()));
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("templateId", templateId);
            result.put("completions", completions);

            long executionTime = System.currentTimeMillis() - startTime;
            totalTemplateTime.addAndGet(executionTime);

            logger.debug("Parameter completions completed in {}ms", executionTime);
            return ResourceResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error getting parameter completions", e);
            return ResourceResult.failure("Completion error: " + e.getMessage(),
                    System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Validate template parameters
     * 
     * @param templateId The template ID
     * @param parameters The parameters to validate
     * @return The validation result
     */
    public ResourceValidationResult validateTemplateParameters(String templateId, Map<String, Object> parameters) {
        try {
            ResourceTemplate template = templates.get(templateId);
            if (template == null) {
                return ResourceValidationResult.failure("Template not found: " + templateId);
            }

            Map<String, TemplateParameter> templateParams = template.getParameters();

            // Check required parameters
            for (Map.Entry<String, TemplateParameter> entry : templateParams.entrySet()) {
                String paramName = entry.getKey();
                TemplateParameter param = entry.getValue();

                if (param.isRequired() && !parameters.containsKey(paramName)) {
                    return ResourceValidationResult.failure("Required parameter missing: " + paramName);
                }
            }

            // Check parameter types
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String paramName = entry.getKey();
                Object paramValue = entry.getValue();

                TemplateParameter templateParam = templateParams.get(paramName);
                if (templateParam != null) {
                    if (!isValidType(paramValue, templateParam.getType())) {
                        return ResourceValidationResult.failure(
                                "Invalid type for parameter " + paramName + ": expected " + templateParam.getType());
                    }
                }
            }

            return ResourceValidationResult.success();

        } catch (Exception e) {
            logger.error("Error validating template parameters", e);
            return ResourceValidationResult.failure("Validation error: " + e.getMessage());
        }
    }

    /**
     * Get performance metrics
     * 
     * @return Performance metrics map
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalTemplateRequests", totalTemplateRequests.get());
        metrics.put("totalTemplateCompletions", totalTemplateCompletions.get());
        metrics.put("averageTemplateTime",
                totalTemplateRequests.get() > 0 ? (double) totalTemplateTime.get() / totalTemplateRequests.get() : 0.0);
        metrics.put("templateCount", templates.size());
        return metrics;
    }

    /**
     * Check if a value is valid for the given type
     * 
     * @param value The value to check
     * @param type The expected type
     * @return true if valid, false otherwise
     */
    private boolean isValidType(Object value, String type) {
        if (value == null) {
            return true; // null is valid for all types
        }

        switch (type) {
            case "string":
                return value instanceof String;
            case "boolean":
                return value instanceof Boolean;
            case "integer":
                return value instanceof Integer || value instanceof Long;
            case "number":
                return value instanceof Number;
            case "object":
                return value instanceof Map;
            case "array":
                return value instanceof List;
            default:
                return true; // unknown types are considered valid
        }
    }

    // ResourceTemplate extracted to org.openhab.core.ai.tool.resources.ResourceTemplate

    // TemplateParameter extracted to org.openhab.core.ai.tool.resources.TemplateParameter
}
