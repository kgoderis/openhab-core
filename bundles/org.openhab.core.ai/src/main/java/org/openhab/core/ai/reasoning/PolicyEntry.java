package org.openhab.core.ai.reasoning;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Single policy entry describing an allow/deny rule.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PolicyEntry {
	private final String policyType;
	private final Map<String, Object> parameters;
	private final String description;
	private final boolean allowed;

	public PolicyEntry(String policyType, Map<String, Object> parameters, String description) {
		this.policyType = policyType;
		this.parameters = parameters;
		this.description = description;
		this.allowed = !"deny".equals(policyType);
	}

	public boolean appliesToAction(String actionType, Map<String, Object> actionParameters) {
		return actionType.equals(parameters.get("actionType"));
	}

	public boolean isAllowed() { return allowed; }
	public String getPolicyType() { return policyType; }
	public Map<String, Object> getParameters() { return parameters; }
	public String getDescription() { return description; }
}
