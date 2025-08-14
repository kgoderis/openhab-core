package org.openhab.core.ai.action.library.automation;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Automation template.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
class AutomationTemplate {
    final String templateId;
    final String name;
    final String description;
    final List<?> steps;
    final Map<?, ?> defaultParameters;

    AutomationTemplate(String templateId, String name, String description, List<?> steps,
            Map<?, ?> defaultParameters) {
        this.templateId = templateId;
        this.name = name;
        this.description = description;
        this.steps = steps;
        this.defaultParameters = defaultParameters;
    }
}


