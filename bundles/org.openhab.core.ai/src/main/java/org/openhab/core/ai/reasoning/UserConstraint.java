package org.openhab.core.ai.reasoning;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * User-specific constraints enforced on actions.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class UserConstraint {
	private final String userId;
	private final Map<String, ConstraintEntry> constraints = new ConcurrentHashMap<>();

	public UserConstraint(String userId) {
		this.userId = userId;
	}

	public SafetyValidationResult validateAction(String actionType, Map<String, Object> actionParameters) {
		for (ConstraintEntry constraint : constraints.values()) {
			if (constraint.appliesToAction(actionType, actionParameters)) {
				return SafetyValidationResult.invalid("User constraint violation: " + constraint.getDescription());
			}
		}
		return SafetyValidationResult.valid();
	}

	public boolean addConstraint(String constraintType, Map<String, Object> constraintParameters, String description) {
		ConstraintEntry constraint = new ConstraintEntry(constraintType, constraintParameters, description);
		constraints.put(constraintType, constraint);
		return true;
	}

	public boolean removeConstraint(String constraintType) {
		return constraints.remove(constraintType) != null;
	}

	public int getConstraintCount() { return constraints.size(); }
	public String getUserId() { return userId; }
}
