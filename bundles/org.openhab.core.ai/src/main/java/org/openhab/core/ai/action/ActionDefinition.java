package org.openhab.core.ai.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Definition of an action that can be executed by autonomous agents
 * 
 * <p>
 * This class defines the structure and requirements for actions:
 * - Action identification and description
 * - Parameter requirements (required and optional)
 * - Action metadata and capabilities
 * - Validation and documentation
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ActionDefinition {

    private final String name;
    private final String description;
    private final List<String> requiredParameters;
    private final List<String> optionalParameters;

    /**
     * Create a new action definition
     * 
     * @param name the action name
     * @param description the action description
     * @param requiredParameters the list of required parameter names
     * @param optionalParameters the list of optional parameter names
     */
    public ActionDefinition(String name, String description, List<String> requiredParameters,
            List<String> optionalParameters) {
        this.name = name;
        this.description = description;
        this.requiredParameters = new ArrayList<>(requiredParameters);
        this.optionalParameters = new ArrayList<>(optionalParameters);
    }

    /**
     * Get the action name
     * 
     * @return the action name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the action description
     * 
     * @return the action description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the list of required parameters
     * 
     * @return the list of required parameter names
     */
    public List<String> getRequiredParameters() {
        return requiredParameters;
    }

    /**
     * Get the list of optional parameters
     * 
     * @return the list of optional parameter names
     */
    public List<String> getOptionalParameters() {
        return optionalParameters;
    }

    /**
     * Check if a parameter is required
     * 
     * @param parameterName the parameter name to check
     * @return true if the parameter is required
     */
    public boolean isRequiredParameter(String parameterName) {
        return requiredParameters.contains(parameterName);
    }

    /**
     * Check if a parameter is optional
     * 
     * @param parameterName the parameter name to check
     * @return true if the parameter is optional
     */
    public boolean isOptionalParameter(String parameterName) {
        return optionalParameters.contains(parameterName);
    }

    /**
     * Get all parameter names (required and optional)
     * 
     * @return the list of all parameter names
     */
    public List<String> getAllParameters() {
        List<String> allParams = new ArrayList<>(requiredParameters);
        allParams.addAll(optionalParameters);
        return allParams;
    }

    /**
     * Check if the action has any parameters
     * 
     * @return true if the action has parameters
     */
    public boolean hasParameters() {
        return !requiredParameters.isEmpty() || !optionalParameters.isEmpty();
    }

    /**
     * Get the total number of parameters
     * 
     * @return the total number of parameters
     */
    public int getParameterCount() {
        return requiredParameters.size() + optionalParameters.size();
    }

    @Override
    public String toString() {
        return "ActionDefinition{" + "name='" + name + '\'' + ", description='" + description + '\''
                + ", requiredParameters=" + requiredParameters + ", optionalParameters=" + optionalParameters + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ActionDefinition that = (ActionDefinition) obj;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
