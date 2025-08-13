package org.openhab.core.ai.reasoning;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Single constraint entry defined by a user.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ConstraintEntry {
	private final String constraintType;
	private final Map<String, Object> parameters;
	private final String description;

	public ConstraintEntry(String constraintType, Map<String, Object> parameters, String description) {
		this.constraintType = constraintType;
		this.parameters = parameters;
		this.description = description;
	}

	public boolean appliesToAction(String actionType, Map<String, Object> actionParameters) {
		return actionType.equals(parameters.get("actionType"));
	}

	public String getConstraintType() { return constraintType; }
	public Map<String, Object> getParameters() { return parameters; }
	public String getDescription() { return description; }
}
