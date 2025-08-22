package org.openhab.core.ai.config.repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YAML parser for prompt templates with template variable support.
 * 
 * <p>
 * This parser handles loading and parsing of prompt template YAML files,
 * including template variable substitution and validation.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class PromptYamlParser {

    private final Logger logger = LoggerFactory.getLogger(PromptYamlParser.class);

    // Pattern for template variables: {{variable_name}}
    private static final Pattern TEMPLATE_VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Parses a prompt template YAML file.
     * 
     * @param filePath the path to the YAML file
     * @return the parsed prompt template
     * @throws PromptRepositoryException if parsing fails
     */
    public PromptRepository.PromptTemplate parsePromptTemplate(Path filePath) throws PromptRepositoryException {
        try {
            String content = Files.readString(filePath);
            return parsePromptTemplate(content, filePath.getFileName().toString());
        } catch (IOException e) {
            throw new PromptRepositoryException("Failed to read prompt template file: " + filePath, e);
        }
    }

    /**
     * Parses prompt template content.
     * 
     * @param content the YAML content
     * @param templateName the name of the template
     * @return the parsed prompt template
     * @throws PromptRepositoryException if parsing fails
     */
    public PromptRepository.PromptTemplate parsePromptTemplate(String content, String templateName)
            throws PromptRepositoryException {
        try {
            // Parse YAML content into map
            Map<String, Object> yamlData = parseYamlContent(content);

            // Validate the structure
            List<String> validationErrors = validatePromptTemplate(yamlData);
            if (!validationErrors.isEmpty()) {
                throw new PromptRepositoryException("Prompt template validation failed: " + validationErrors);
            }

            // Extract template variables from content
            Map<String, String> variables = extractTemplateVariables(yamlData);

            // Create prompt template
            return createPromptTemplate(yamlData, templateName, variables);

        } catch (Exception e) {
            throw new PromptRepositoryException("Failed to parse prompt template: " + e.getMessage(), e);
        }
    }

    /**
     * Parses YAML content into a map.
     * 
     * @param content the YAML content
     * @return the parsed map
     * @throws Exception if parsing fails
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseYamlContent(String content) throws Exception {
        // TODO: Implement actual YAML parsing using a library like SnakeYAML
        // For now, return a simple map structure
        Map<String, Object> result = new HashMap<>();

        // This is a placeholder implementation
        // In a real implementation, you would use:
        // Yaml yaml = new Yaml();
        // return yaml.load(content);

        logger.debug("Parsing YAML content (placeholder implementation)");
        return result;
    }

    /**
     * Validates prompt template structure.
     * 
     * @param data the parsed YAML data
     * @return list of validation errors, empty if valid
     */
    private List<String> validatePromptTemplate(Map<String, Object> data) {
        List<String> errors = new ArrayList<>();

        // Validate required fields
        YamlValidationUtils.validateRequiredField(data, "name", errors);
        YamlValidationUtils.validateRequiredField(data, "version", errors);
        YamlValidationUtils.validateRequiredField(data, "content", errors);

        // Validate string fields
        YamlValidationUtils.validateStringField(data, "name", errors);
        YamlValidationUtils.validateStringField(data, "version", errors);
        YamlValidationUtils.validateStringField(data, "content", errors);
        YamlValidationUtils.validateStringField(data, "description", errors);

        // Validate optional fields
        if (data.containsKey("agent")) {
            YamlValidationUtils.validateStringField(data, "agent", errors);
        }
        if (data.containsKey("intent")) {
            YamlValidationUtils.validateStringField(data, "intent", errors);
        }

        // Validate variables if present
        if (data.containsKey("variables")) {
            YamlValidationUtils.validateMapField(data, "variables", errors);
        }

        // Validate metadata if present
        if (data.containsKey("metadata")) {
            YamlValidationUtils.validateMapField(data, "metadata", errors);
        }

        return errors;
    }

    /**
     * Extracts template variables from the content.
     * 
     * @param data the parsed YAML data
     * @return map of variable names to descriptions
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> extractTemplateVariables(Map<String, Object> data) {
        Map<String, String> variables = new HashMap<>();

        // Extract variables from content
        String content = (String) data.get("content");
        if (content != null) {
            Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(content);
            while (matcher.find()) {
                String variableName = matcher.group(1).trim();
                variables.put(variableName, "Template variable: " + variableName);
            }
        }

        // Override with explicit variables if defined
        if (data.containsKey("variables")) {
            Map<String, Object> explicitVariables = (Map<String, Object>) data.get("variables");
            for (Map.Entry<String, Object> entry : explicitVariables.entrySet()) {
                String description = entry.getValue() instanceof String ? (String) entry.getValue()
                        : "Template variable";
                variables.put(entry.getKey(), description);
            }
        }

        return variables;
    }

    /**
     * Creates a prompt template from parsed data.
     * 
     * @param data the parsed YAML data
     * @param templateName the template name
     * @param variables the template variables
     * @return the prompt template
     */
    @SuppressWarnings("unchecked")
    private PromptRepository.PromptTemplate createPromptTemplate(Map<String, Object> data, String templateName,
            Map<String, String> variables) {
        return new PromptRepository.PromptTemplate() {
            @Override
            public String getName() {
                return (String) data.getOrDefault("name", templateName);
            }

            @Override
            public @Nullable String getDescription() {
                return (String) data.get("description");
            }

            @Override
            public String getVersion() {
                return (String) data.get("version");
            }

            @Override
            public String getContent() {
                return (String) data.get("content");
            }

            @Override
            public Map<String, String> getVariables() {
                return variables;
            }

            @Override
            public Map<String, Object> getMetadata() {
                return data.containsKey("metadata") ? (Map<String, Object>) data.get("metadata") : new HashMap<>();
            }

            @Override
            public @Nullable String getAgent() {
                return (String) data.get("agent");
            }

            @Override
            public @Nullable String getIntent() {
                return (String) data.get("intent");
            }
        };
    }

    /**
     * Processes template content by substituting variables.
     * 
     * @param template the prompt template
     * @param variables the variable values to substitute
     * @return the processed content
     * @throws PromptRepositoryException if substitution fails
     */
    public String processTemplate(PromptRepository.PromptTemplate template, Map<String, String> variables)
            throws PromptRepositoryException {
        String content = template.getContent();
        Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            String replacement = variables.get(variableName);

            if (replacement == null) {
                throw new PromptRepositoryException("Missing required template variable: " + variableName);
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Validates that all required variables are provided.
     * 
     * @param template the prompt template
     * @param variables the provided variables
     * @return list of missing variables
     */
    public List<String> validateTemplateVariables(PromptRepository.PromptTemplate template,
            Map<String, String> variables) {
        List<String> missingVariables = new ArrayList<>();
        Map<String, String> requiredVariables = template.getVariables();

        for (String requiredVar : requiredVariables.keySet()) {
            if (!variables.containsKey(requiredVar)) {
                missingVariables.add(requiredVar);
            }
        }

        return missingVariables;
    }

    /**
     * Serializes a prompt template to YAML.
     * 
     * @param template the prompt template to serialize
     * @return the YAML content
     * @throws PromptRepositoryException if serialization fails
     */
    public String serializePromptTemplate(PromptRepository.PromptTemplate template) throws PromptRepositoryException {
        try {
            // TODO: Implement actual YAML serialization using a library like SnakeYAML
            // For now, return a simple YAML structure
            StringBuilder yaml = new StringBuilder();
            yaml.append("name: ").append(template.getName()).append("\n");
            yaml.append("version: ").append(template.getVersion()).append("\n");

            if (template.getDescription() != null) {
                yaml.append("description: ").append(template.getDescription()).append("\n");
            }

            yaml.append("content: |\n");
            String[] lines = template.getContent().split("\n");
            for (String line : lines) {
                yaml.append("  ").append(line).append("\n");
            }

            if (template.getAgent() != null) {
                yaml.append("agent: ").append(template.getAgent()).append("\n");
            }

            if (template.getIntent() != null) {
                yaml.append("intent: ").append(template.getIntent()).append("\n");
            }

            if (!template.getVariables().isEmpty()) {
                yaml.append("variables:\n");
                for (Map.Entry<String, String> entry : template.getVariables().entrySet()) {
                    yaml.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }

            if (!template.getMetadata().isEmpty()) {
                yaml.append("metadata:\n");
                for (Map.Entry<String, Object> entry : template.getMetadata().entrySet()) {
                    yaml.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }

            return yaml.toString();

        } catch (Exception e) {
            throw new PromptRepositoryException("Failed to serialize prompt template: " + e.getMessage(), e);
        }
    }
}
