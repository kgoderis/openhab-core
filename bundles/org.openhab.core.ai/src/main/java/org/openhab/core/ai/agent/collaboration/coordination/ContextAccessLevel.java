package org.openhab.core.ai.agent.collaboration.coordination;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Access level for a shared coordination context.
 *
 * author Karel Goderis - Initial Contribution
 * since 1.0.0
 */
@NonNullByDefault
public enum ContextAccessLevel {
	PRIVATE,
	SHARED,
	PUBLIC
}
