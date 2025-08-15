package org.openhab.core.ai.action;

import java.time.Instant;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builder for {@link ActionVersionInfo}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ActionVersionInfoBuilder {
    String actionId = "";
    String version = "1.0.0";
    String minCompatibleVersion = "1.0.0";
    String maxCompatibleVersion = "2.0.0";
    Set<String> compatibleVersions = Set.of();
    boolean deprecated = false;
    String deprecationMessage = "";
    Instant deprecationDate = null;
    Instant removalDate = null;
    String migrationGuide = "";
    Set<String> breakingChanges = Set.of();

    public ActionVersionInfoBuilder actionId(String actionId) {
        this.actionId = actionId;
        return this;
    }

    public ActionVersionInfoBuilder version(String version) {
        this.version = version;
        return this;
    }

    public ActionVersionInfoBuilder minCompatibleVersion(String v) {
        this.minCompatibleVersion = v;
        return this;
    }

    public ActionVersionInfoBuilder maxCompatibleVersion(String v) {
        this.maxCompatibleVersion = v;
        return this;
    }

    public ActionVersionInfoBuilder compatibleVersions(Set<String> v) {
        this.compatibleVersions = v;
        return this;
    }

    public ActionVersionInfoBuilder deprecated(boolean v) {
        this.deprecated = v;
        return this;
    }

    public ActionVersionInfoBuilder deprecationMessage(String v) {
        this.deprecationMessage = v;
        return this;
    }

    public ActionVersionInfoBuilder deprecationDate(Instant v) {
        this.deprecationDate = v;
        return this;
    }

    public ActionVersionInfoBuilder removalDate(Instant v) {
        this.removalDate = v;
        return this;
    }

    public ActionVersionInfoBuilder migrationGuide(String v) {
        this.migrationGuide = v;
        return this;
    }

    public ActionVersionInfoBuilder breakingChanges(Set<String> v) {
        this.breakingChanges = v;
        return this;
    }

    public ActionVersionInfo build() {
        return new ActionVersionInfo(this);
    }
}
