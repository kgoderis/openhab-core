package org.openhab.core.ai.agent.collaboration.conflict;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Prevention rule interface.
 *
 * Defines a rule that can automatically prevent a conflict.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface PreventionRule {
    String getRuleId();
    boolean shouldPrevent(Conflict conflict);
}
