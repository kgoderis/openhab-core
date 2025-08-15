package org.openhab.core.ai.tool.resources.api;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Resource template with parameters and metadata.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ResourceTemplate {
    private final String id;
    private final String name;
    private final String description;
    private final String resourceType;
    private final Map<String, TemplateParameter> parameters;
    private final Map<String, Object> defaults;

    public ResourceTemplate(String id, String name, String description, String resourceType,
            Map<String, TemplateParameter> parameters, Map<String, Object> defaults) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.resourceType = resourceType;
        this.parameters = parameters;
        this.defaults = defaults;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Map<String, TemplateParameter> getParameters() {
        return parameters;
    }

    public Map<String, Object> getDefaults() {
        return defaults;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("resourceType", resourceType);

        Map<String, Object> paramMap = new HashMap<>();
        for (Map.Entry<String, TemplateParameter> entry : parameters.entrySet()) {
            paramMap.put(entry.getKey(), entry.getValue().toMap());
        }
        map.put("parameters", paramMap);
        map.put("defaults", defaults);

        return map;
    }
}
