package org.openhab.core.ai.config.repo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Base class for YAML parsers with common functionality.
 * 
 * <p>
 * This class provides common functionality for YAML parsers including:
 * - YAML content parsing using SnakeYAML
 * - File reading and error handling
 * - Common validation patterns
 * - Standardized exception handling
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public abstract class BaseYamlParser {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Yaml yaml;

    protected BaseYamlParser() {
        this.yaml = new Yaml();
    }

    /**
     * Reads and parses a YAML file.
     * 
     * @param filePath the path to the YAML file
     * @param fileName the name of the file (for error messages)
     * @return the parsed domain object
     * @throws Exception if parsing fails
     */
    protected <T> T parseFile(Path filePath, String fileName) throws Exception {
        try {
            String content = Files.readString(filePath);
            return parseContent(content, fileName);
        } catch (IOException e) {
            String errorMsg = "Failed to read " + fileName + " file: " + filePath;
            logger.error(errorMsg, e);
            throw createParseException(errorMsg, e);
        }
    }

    /**
     * Parses YAML content into a domain object.
     * 
     * @param content the YAML content
     * @param name the name for the parsed object
     * @return the parsed domain object
     * @throws Exception if parsing fails
     */
    protected <T> T parseContent(String content, String name) throws Exception {
        try {
            // Parse YAML content into map
            Map<String, Object> yamlData = parseYamlContent(content);

            // Validate the structure
            List<String> validationErrors = validateStructure(yamlData);
            if (!validationErrors.isEmpty()) {
                String errorMsg = name + " validation failed: " + validationErrors;
                logger.error(errorMsg);
                throw createParseException(errorMsg, null);
            }

            // Create domain object
            return createDomainObject(yamlData, name);

        } catch (Exception e) {
            String errorMsg = "Failed to parse " + name + ": " + e.getMessage();
            logger.error(errorMsg, e);
            throw createParseException(errorMsg, e);
        }
    }

    /**
     * Parses YAML content into a map using SnakeYAML.
     * 
     * @param content the YAML content
     * @return the parsed map
     * @throws Exception if parsing fails
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> parseYamlContent(String content) throws Exception {
        Map<String, Object> yamlData = yaml.load(content);
        if (yamlData == null || yamlData.isEmpty()) {
            throw new IllegalArgumentException("Empty or invalid YAML content");
        }
        return yamlData;
    }

    /**
     * Validates the structure of parsed YAML data.
     * 
     * @param yamlData the parsed YAML data
     * @return list of validation errors, empty if valid
     */
    protected abstract List<String> validateStructure(Map<String, Object> yamlData);

    /**
     * Creates a domain object from validated YAML data.
     * 
     * @param yamlData the validated YAML data
     * @param name the name for the object
     * @return the created domain object
     * @throws Exception if object creation fails
     */
    protected abstract <T> T createDomainObject(Map<String, Object> yamlData, String name) throws Exception;

    /**
     * Creates an appropriate exception for parsing errors.
     * 
     * @param message the error message
     * @param cause the cause exception
     * @return the created exception
     */
    protected abstract Exception createParseException(String message, Throwable cause);
}
