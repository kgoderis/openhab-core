package org.openhab.core.ai.agent.collaboration.context;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Schema definition for a shared context.
 *
 * Contains required/optional fields and a version identifier.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextSchema {

    private final String contextId;
    private final Map<String, String> requiredFields;
    private final Map<String, String> optionalFields;
    private final String version;

    public ContextSchema(String contextId, Map<String, String> requiredFields, Map<String, String> optionalFields,
            String version) {
        this.contextId = contextId;
        this.requiredFields = requiredFields;
        this.optionalFields = optionalFields;
        this.version = version;
    }

    public String getContextId() {
        return contextId;
    }

    public Map<String, String> getRequiredFields() {
        return requiredFields;
    }

    public Map<String, String> getOptionalFields() {
        return optionalFields;
    }

    public String getVersion() {
        return version;
    }

    public boolean validate(Map<String, Object> data) {
        return true;
    }
}


