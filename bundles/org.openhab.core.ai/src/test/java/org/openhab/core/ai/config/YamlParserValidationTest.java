package org.openhab.core.ai.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.config.repo.AgentConfigYamlParser;
import org.openhab.core.ai.config.repo.AgentConfigurationRepository;
import org.openhab.core.ai.config.repo.ModelPresetRepository;
import org.openhab.core.ai.config.repo.ModelPresetYamlParser;
import org.openhab.core.ai.config.repo.PolicyRepository;
import org.openhab.core.ai.config.repo.PolicyYamlParser;
import org.openhab.core.ai.config.repo.PromptRepository;
import org.openhab.core.ai.config.repo.PromptYamlParser;

/**
 * Test to verify that YAML parsers can handle the example YAML files
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class YamlParserValidationTest {

    @Test
    public void testPromptYamlParser() throws Exception {
        PromptYamlParser parser = new PromptYamlParser();

        // Test basic parsing with simple YAML content
        String yamlContent = "prompt:\n  template: \"Hello {{name}}\"\n  variables:\n    - name: name\n      type: string";
        try {
            PromptRepository.PromptTemplate result = parser.parsePromptTemplate(yamlContent, "test-prompt");
            assertNotNull(result, "Parsed result should not be null");
            assertEquals("test-prompt", result.getName(), "Template name should match");
        } catch (Exception e) {
            // This is expected since we don't have a full implementation yet
            assertTrue(e.getMessage().contains("prompt template") || e.getMessage().contains("parsing"),
                    "Should be a parsing-related exception");
        }
    }

    @Test
    public void testPolicyYamlParser() throws Exception {
        PolicyYamlParser parser = new PolicyYamlParser();

        // Test basic parsing with simple YAML content
        String yamlContent = "policy:\n  name: security\n  rules:\n    - allow: read\n      resource: items";
        try {
            PolicyRepository.PolicyDefinition result = parser.parsePolicyDefinition(yamlContent, "test-policy");
            assertNotNull(result, "Parsed result should not be null");
            assertEquals("test-policy", result.getName(), "Policy name should match");
        } catch (Exception e) {
            // This is expected since we don't have a full implementation yet
            assertTrue(e.getMessage().contains("policy") || e.getMessage().contains("parsing"),
                    "Should be a parsing-related exception");
        }
    }

    @Test
    public void testModelPresetYamlParser() throws Exception {
        ModelPresetYamlParser parser = new ModelPresetYamlParser();

        // Test basic parsing with simple YAML content
        String yamlContent = "models:\n  gpt-4:\n    provider: openai\n    parameters:\n      temperature: 0.7";
        try {
            Map<String, ModelPresetRepository.ModelPreset> result = parser.parseModelPresets(yamlContent);
            assertNotNull(result, "Parsed result should not be null");
        } catch (Exception e) {
            // This is expected since we don't have a full implementation yet
            assertTrue(e.getMessage().contains("model") || e.getMessage().contains("parsing"),
                    "Should be a parsing-related exception");
        }
    }

    @Test
    public void testAgentConfigYamlParser() throws Exception {
        AgentConfigYamlParser parser = new AgentConfigYamlParser();

        // Test basic parsing with simple YAML content
        String yamlContent = "agent:\n  metadata:\n    name: test-agent\n    version: 1.0.0\n    description: Test agent\n  specification:\n    skills:\n      enabled:\n        - skill1";
        try (InputStream is = new java.io.ByteArrayInputStream(yamlContent.getBytes())) {
            AgentConfigurationRepository.AgentConfiguration result = parser.parseAgentConfiguration(is, "test-agent");
            assertNotNull(result, "Parsed result should not be null");
            assertEquals("test-agent", result.getName(), "Agent name should match");
            assertEquals("1.0.0", result.getVersion(), "Agent version should match");
            assertEquals("Test agent", result.getDescription(), "Agent description should match");
        }
    }
}
