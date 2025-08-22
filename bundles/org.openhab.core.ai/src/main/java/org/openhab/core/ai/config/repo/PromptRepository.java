package org.openhab.core.ai.config.repo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Repository for managing YAML prompt templates.
 * 
 * <p>
 * This interface provides methods for loading, storing, and managing prompt templates
 * from YAML files in the /conf/ai/prompts/ directory. It supports template variables
 * and provides validation for prompt structure.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface PromptRepository {

    /**
     * Loads all prompt templates from the configured directory.
     * 
     * @return map of prompt template names to their content
     * @throws PromptRepositoryException if loading fails
     */
    Map<String, PromptTemplate> loadAllTemplates() throws PromptRepositoryException;

    /**
     * Loads a specific prompt template by name.
     * 
     * @param templateName the name of the template to load
     * @return the prompt template if found
     * @throws PromptRepositoryException if loading fails
     */
    Optional<PromptTemplate> loadTemplate(String templateName) throws PromptRepositoryException;

    /**
     * Loads prompt templates for a specific agent.
     * 
     * @param agentName the name of the agent
     * @return map of template names to their content for the agent
     * @throws PromptRepositoryException if loading fails
     */
    Map<String, PromptTemplate> loadAgentTemplates(String agentName) throws PromptRepositoryException;

    /**
     * Saves a prompt template to the repository.
     * 
     * @param templateName the name of the template
     * @param template the template content
     * @throws PromptRepositoryException if saving fails
     */
    void saveTemplate(String templateName, PromptTemplate template) throws PromptRepositoryException;

    /**
     * Deletes a prompt template from the repository.
     * 
     * @param templateName the name of the template to delete
     * @throws PromptRepositoryException if deletion fails
     */
    void deleteTemplate(String templateName) throws PromptRepositoryException;

    /**
     * Validates a prompt template structure.
     * 
     * @param template the template to validate
     * @return list of validation errors, empty if valid
     */
    List<String> validateTemplate(PromptTemplate template);

    /**
     * Gets the list of available template names.
     * 
     * @return list of template names
     * @throws PromptRepositoryException if listing fails
     */
    List<String> getTemplateNames() throws PromptRepositoryException;

    /**
     * Checks if a template exists.
     * 
     * @param templateName the name of the template
     * @return true if the template exists
     */
    boolean templateExists(String templateName);

    /**
     * Reloads all templates from disk.
     * 
     * @throws PromptRepositoryException if reloading fails
     */
    void reload() throws PromptRepositoryException;

    /**
     * Represents a prompt template with metadata and content.
     */
    interface PromptTemplate {
        /**
         * Gets the template name.
         * 
         * @return the template name
         */
        String getName();

        /**
         * Gets the template description.
         * 
         * @return the template description
         */
        @Nullable
        String getDescription();

        /**
         * Gets the template version.
         * 
         * @return the template version
         */
        String getVersion();

        /**
         * Gets the template content.
         * 
         * @return the template content
         */
        String getContent();

        /**
         * Gets the template variables.
         * 
         * @return map of variable names to their descriptions
         */
        Map<String, String> getVariables();

        /**
         * Gets the template metadata.
         * 
         * @return map of metadata key-value pairs
         */
        Map<String, Object> getMetadata();

        /**
         * Gets the agent this template is associated with.
         * 
         * @return the agent name, or null if not associated
         */
        @Nullable
        String getAgent();

        /**
         * Gets the intent this template is for.
         * 
         * @return the intent name, or null if not specified
         */
        @Nullable
        String getIntent();
    }
}
