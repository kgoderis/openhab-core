package org.openhab.core.ai.action.library.automation;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Workflow definition data holder.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class WorkflowDefinition {
    final String workflowId;
    final String name;
    final String description;
    final List<?> steps;
    final Map<?, ?> conditions;

    WorkflowDefinition(String workflowId, String name, String description, List<?> steps, Map<?, ?> conditions) {
        this.workflowId = workflowId;
        this.name = name;
        this.description = description;
        this.steps = steps;
        this.conditions = conditions;
    }
}
