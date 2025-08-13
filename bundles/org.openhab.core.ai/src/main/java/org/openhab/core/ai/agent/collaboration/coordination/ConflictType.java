package org.openhab.core.ai.agent.collaboration.coordination;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Types of conflicts that can occur during coordination.
 *
 * author Karel Goderis - Initial Contribution
 * since 1.0.0
 */
@NonNullByDefault
public enum ConflictType {
	RESOURCE_CONFLICT,
	PRIORITY_CONFLICT,
	POLICY_CONFLICT,
	COMMUNICATION_CONFLICT,
	GENERAL_CONFLICT
}
