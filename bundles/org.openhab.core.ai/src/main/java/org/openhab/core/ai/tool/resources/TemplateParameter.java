package org.openhab.core.ai.tool.resources;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Template parameter with validation rules.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class TemplateParameter {
    private final String name;
    private final String type;
    private final String description;
    private final boolean required;
    private final Object defaultValue;

    public TemplateParameter(String name, String type, String description, boolean required, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("type", type);
        map.put("description", description);
        map.put("required", required);
        map.put("defaultValue", defaultValue);
        return map;
    }
}


