package org.openhab.core.ai.agents;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Skill Execution Request
 * 
 * <p>
 * Represents a request to execute a skill with specific parameters.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class SkillExecutionRequest {
    private final String skillName;
    private final Map<String, Object> parameters;

    public SkillExecutionRequest(String skillName, Map<String, Object> parameters) {
        this.skillName = skillName;
        this.parameters = parameters;
    }

    public String getSkillName() {
        return skillName;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
