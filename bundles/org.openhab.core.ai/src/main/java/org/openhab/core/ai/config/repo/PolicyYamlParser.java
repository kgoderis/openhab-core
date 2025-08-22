package org.openhab.core.ai.config.repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YAML parser for policy definitions with rule validation.
 * 
 * <p>
 * This parser handles loading and parsing of policy definition YAML files,
 * including rule validation and constraint checking.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class PolicyYamlParser {

    private final Logger logger = LoggerFactory.getLogger(PolicyYamlParser.class);

    /**
     * Parses a policy definition YAML file.
     * 
     * @param filePath the path to the YAML file
     * @return the parsed policy definition
     * @throws PolicyRepositoryException if parsing fails
     */
    public PolicyRepository.PolicyDefinition parsePolicyDefinition(Path filePath) throws PolicyRepositoryException {
        try {
            String content = Files.readString(filePath);
            return parsePolicyDefinition(content, filePath.getFileName().toString());
        } catch (IOException e) {
            throw new PolicyRepositoryException("Failed to read policy definition file: " + filePath, e);
        }
    }

    /**
     * Parses policy definition content.
     * 
     * @param content the YAML content
     * @param policyName the name of the policy
     * @return the parsed policy definition
     * @throws PolicyRepositoryException if parsing fails
     */
    public PolicyRepository.PolicyDefinition parsePolicyDefinition(String content, String policyName)
            throws PolicyRepositoryException {
        try {
            // Parse YAML content into map
            Map<String, Object> yamlData = parseYamlContent(content);

            // Validate the structure
            List<String> validationErrors = validatePolicyDefinition(yamlData);
            if (!validationErrors.isEmpty()) {
                throw new PolicyRepositoryException("Policy definition validation failed: " + validationErrors);
            }

            // Create policy definition
            return createPolicyDefinition(yamlData, policyName);

        } catch (Exception e) {
            throw new PolicyRepositoryException("Failed to parse policy definition: " + e.getMessage(), e);
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
     * Validates policy definition structure.
     * 
     * @param data the parsed YAML data
     * @return list of validation errors, empty if valid
     */
    private List<String> validatePolicyDefinition(Map<String, Object> data) {
        List<String> errors = new ArrayList<>();

        // Validate that we have a policies section
        if (!data.containsKey("policies")) {
            errors.add("Missing required 'policies' section");
            return errors;
        }

        // Validate policies section is a map
        YamlValidationUtils.validateMapField(data, "policies", errors);

        return errors;
    }

    /**
     * Creates a policy definition from parsed data.
     * 
     * @param data the parsed YAML data
     * @param policyName the policy name
     * @return the policy definition
     */
    @SuppressWarnings("unchecked")
    private PolicyRepository.PolicyDefinition createPolicyDefinition(Map<String, Object> data, String policyName) {
        Map<String, Object> policiesData = (Map<String, Object>) data.get("policies");

        // Extract the first policy (assuming single policy per file for now)
        String firstPolicyKey = policiesData.keySet().iterator().next();
        Map<String, Object> policyData = (Map<String, Object>) policiesData.get(firstPolicyKey);

        return new PolicyRepository.PolicyDefinition() {
            @Override
            public String getName() {
                return (String) policyData.getOrDefault("name", firstPolicyKey);
            }

            @Override
            public @Nullable String getDescription() {
                return (String) policyData.get("description");
            }

            @Override
            public String getVersion() {
                return (String) policyData.getOrDefault("version", "1.0.0");
            }

            @Override
            public List<PolicyRepository.PolicyRule> getRules() {
                List<PolicyRepository.PolicyRule> rules = new ArrayList<>();

                if (policyData.containsKey("rules")) {
                    List<Map<String, Object>> rulesData = (List<Map<String, Object>>) policyData.get("rules");
                    for (Map<String, Object> ruleData : rulesData) {
                        rules.add(createPolicyRule(ruleData));
                    }
                }

                return rules;
            }

            @Override
            public Map<String, Object> getMetadata() {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("constraints", policyData.get("constraints"));
                metadata.put("original_policy_key", firstPolicyKey);
                return metadata;
            }

            @Override
            public @Nullable String getAgent() {
                return null; // Not specified in the example structure
            }

            @Override
            public PolicyRepository.PolicyPriority getPriority() {
                return PolicyRepository.PolicyPriority.MEDIUM; // Default priority
            }
        };
    }

    /**
     * Creates a policy rule from parsed data.
     * 
     * @param ruleData the parsed rule data
     * @return the policy rule
     */
    private PolicyRepository.PolicyRule createPolicyRule(Map<String, Object> ruleData) {
        return new PolicyRepository.PolicyRule() {
            @Override
            public String getName() {
                return (String) ruleData.get("name");
            }

            @Override
            public String getCondition() {
                return (String) ruleData.get("condition");
            }

            @Override
            public String getAction() {
                return (String) ruleData.get("action");
            }

            @Override
            public PolicyRepository.PolicyPriority getPriority() {
                String priorityStr = (String) ruleData.get("priority");
                if (priorityStr != null) {
                    try {
                        return PolicyRepository.PolicyPriority.valueOf(priorityStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid priority value: {}, using MEDIUM", priorityStr);
                    }
                }
                return PolicyRepository.PolicyPriority.MEDIUM;
            }

            @Override
            public Map<String, Object> getMetadata() {
                return new HashMap<>(); // No additional metadata in the example
            }
        };
    }

    /**
     * Serializes a policy definition to YAML.
     * 
     * @param policy the policy definition to serialize
     * @return the YAML content
     * @throws PolicyRepositoryException if serialization fails
     */
    public String serializePolicyDefinition(PolicyRepository.PolicyDefinition policy) throws PolicyRepositoryException {
        try {
            // TODO: Implement actual YAML serialization using a library like SnakeYAML
            // For now, return a simple YAML structure
            StringBuilder yaml = new StringBuilder();
            yaml.append("policies:\n");
            yaml.append("  ").append(policy.getName()).append(":\n");
            yaml.append("    version: ").append(policy.getVersion()).append("\n");

            if (policy.getDescription() != null) {
                yaml.append("    description: ").append(policy.getDescription()).append("\n");
            }

            if (!policy.getRules().isEmpty()) {
                yaml.append("    rules:\n");
                for (PolicyRepository.PolicyRule rule : policy.getRules()) {
                    yaml.append("      - name: ").append(rule.getName()).append("\n");
                    yaml.append("        condition: ").append(rule.getCondition()).append("\n");
                    yaml.append("        action: ").append(rule.getAction()).append("\n");
                    yaml.append("        priority: ").append(rule.getPriority().name().toLowerCase()).append("\n");
                }
            }

            return yaml.toString();

        } catch (Exception e) {
            throw new PolicyRepositoryException("Failed to serialize policy definition: " + e.getMessage(), e);
        }
    }

    /**
     * Validates policy rules for consistency.
     * 
     * @param rules the list of policy rules to validate
     * @return list of validation errors, empty if valid
     */
    public List<String> validatePolicyRules(List<PolicyRepository.PolicyRule> rules) {
        List<String> errors = new ArrayList<>();

        for (PolicyRepository.PolicyRule rule : rules) {
            // Validate rule name
            if (rule.getName() == null || rule.getName().trim().isEmpty()) {
                errors.add("Policy rule must have a name");
            }

            // Validate condition
            if (rule.getCondition() == null || rule.getCondition().trim().isEmpty()) {
                errors.add("Policy rule '" + rule.getName() + "' must have a condition");
            }

            // Validate action
            if (rule.getAction() == null || rule.getAction().trim().isEmpty()) {
                errors.add("Policy rule '" + rule.getName() + "' must have an action");
            }
        }

        return errors;
    }
}
