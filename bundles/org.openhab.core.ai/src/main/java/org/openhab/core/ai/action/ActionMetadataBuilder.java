package org.openhab.core.ai.action;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link ActionMetadata}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ActionMetadataBuilder {
    String version = "1.0.0";
    String author = "openHAB AI Team";
    String description = "";
    List<String> tags = List.of();
    Map<String, Object> properties = Map.of();
    Instant created = Instant.now();
    Instant lastModified = Instant.now();
    String documentation = "";
    List<String> examples = List.of();
    Map<String, Object> requirements = Map.of();

    public ActionMetadataBuilder version(String version) { this.version = version; return this; }
    public ActionMetadataBuilder author(String author) { this.author = author; return this; }
    public ActionMetadataBuilder description(String description) { this.description = description; return this; }
    public ActionMetadataBuilder tags(List<String> tags) { this.tags = tags; return this; }
    public ActionMetadataBuilder properties(Map<String, Object> properties) { this.properties = properties; return this; }
    public ActionMetadataBuilder created(Instant created) { this.created = created; return this; }
    public ActionMetadataBuilder lastModified(Instant lastModified) { this.lastModified = lastModified; return this; }
    public ActionMetadataBuilder documentation(String documentation) { this.documentation = documentation; return this; }
    public ActionMetadataBuilder examples(List<String> examples) { this.examples = examples; return this; }
    public ActionMetadataBuilder requirements(Map<String, Object> requirements) { this.requirements = requirements; return this; }

    public ActionMetadata build() { return new ActionMetadata(this); }
}


