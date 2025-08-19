package org.openhab.core.ai.common.builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified builder for action-related objects in the openHAB AI system.
 *
 * <p>
 * This class provides a common builder pattern for creating action-related objects
 * such as ActionContext, ActionMetadata, ActionAnalytics, etc.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class ActionBuilder<T> extends AbstractBuilder<T> {

    protected String protocol = "";
    protected String clientId = "";
    protected String sessionId = "";
    protected @Nullable Object authContext;
    protected Map<String, Object> protocolContext = Map.of();
    protected long executionStartTime = System.currentTimeMillis();
    protected String correlationId = "";
    protected String priority = "";
    protected String version = "1.0.0";
    protected String author = "openHAB AI Team";
    protected String description = "";
    protected List<String> tags = List.of();
    protected Map<String, Object> properties = Map.of();
    protected @Nullable Instant created;
    protected @Nullable Instant lastModified;
    protected String documentation = "";
    protected List<String> examples = List.of();
    protected Map<String, Object> requirements = Map.of();

    /**
     * Set the protocol for the action.
     *
     * @param protocol the protocol
     * @return this builder
     */
    public ActionBuilder<T> withProtocol(String protocol) {
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        return this;
    }

    /**
     * Set the client ID for the action.
     *
     * @param clientId the client ID
     * @return this builder
     */
    public ActionBuilder<T> withClientId(String clientId) {
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        return this;
    }

    /**
     * Set the session ID for the action.
     *
     * @param sessionId the session ID
     * @return this builder
     */
    public ActionBuilder<T> withSessionId(String sessionId) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        return this;
    }

    /**
     * Set the authentication context for the action.
     *
     * @param authContext the authentication context
     * @return this builder
     */
    public ActionBuilder<T> withAuthContext(@Nullable Object authContext) {
        this.authContext = authContext;
        return this;
    }

    /**
     * Set the protocol context for the action.
     *
     * @param protocolContext the protocol context
     * @return this builder
     */
    public ActionBuilder<T> withProtocolContext(Map<String, Object> protocolContext) {
        this.protocolContext = Objects.requireNonNull(protocolContext, "protocolContext");
        return this;
    }

    /**
     * Set the execution start time for the action.
     *
     * @param executionStartTime the execution start time
     * @return this builder
     */
    public ActionBuilder<T> withExecutionStartTime(long executionStartTime) {
        this.executionStartTime = executionStartTime;
        return this;
    }

    /**
     * Set the correlation ID for the action.
     *
     * @param correlationId the correlation ID
     * @return this builder
     */
    public ActionBuilder<T> withCorrelationId(String correlationId) {
        this.correlationId = Objects.requireNonNull(correlationId, "correlationId");
        return this;
    }

    /**
     * Set the priority for the action.
     *
     * @param priority the priority
     * @return this builder
     */
    public ActionBuilder<T> withPriority(String priority) {
        this.priority = Objects.requireNonNull(priority, "priority");
        return this;
    }

    /**
     * Set the version for the action.
     *
     * @param version the version
     * @return this builder
     */
    public ActionBuilder<T> withVersion(String version) {
        this.version = Objects.requireNonNull(version, "version");
        return this;
    }

    /**
     * Set the author for the action.
     *
     * @param author the author
     * @return this builder
     */
    public ActionBuilder<T> withAuthor(String author) {
        this.author = Objects.requireNonNull(author, "author");
        return this;
    }

    /**
     * Set the description for the action.
     *
     * @param description the description
     * @return this builder
     */
    public ActionBuilder<T> withDescription(String description) {
        this.description = Objects.requireNonNull(description, "description");
        return this;
    }

    /**
     * Set the tags for the action.
     *
     * @param tags the tags
     * @return this builder
     */
    public ActionBuilder<T> withTags(List<String> tags) {
        this.tags = Objects.requireNonNull(tags, "tags");
        return this;
    }

    /**
     * Set the properties for the action.
     *
     * @param properties the properties
     * @return this builder
     */
    public ActionBuilder<T> withProperties(Map<String, Object> properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
        return this;
    }

    /**
     * Set the created timestamp for the action.
     *
     * @param created the created timestamp
     * @return this builder
     */
    public ActionBuilder<T> withCreated(@Nullable Instant created) {
        this.created = created;
        return this;
    }

    /**
     * Set the last modified timestamp for the action.
     *
     * @param lastModified the last modified timestamp
     * @return this builder
     */
    public ActionBuilder<T> withLastModified(@Nullable Instant lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Set the documentation for the action.
     *
     * @param documentation the documentation
     * @return this builder
     */
    public ActionBuilder<T> withDocumentation(String documentation) {
        this.documentation = Objects.requireNonNull(documentation, "documentation");
        return this;
    }

    /**
     * Set the examples for the action.
     *
     * @param examples the examples
     * @return this builder
     */
    public ActionBuilder<T> withExamples(List<String> examples) {
        this.examples = Objects.requireNonNull(examples, "examples");
        return this;
    }

    /**
     * Set the requirements for the action.
     *
     * @param requirements the requirements
     * @return this builder
     */
    public ActionBuilder<T> withRequirements(Map<String, Object> requirements) {
        this.requirements = Objects.requireNonNull(requirements, "requirements");
        return this;
    }

    @Override
    protected void validate() {
        validateRequiredString(protocol, "protocol");
        validateRequiredString(clientId, "clientId");
        validateRequiredString(sessionId, "sessionId");
        validateRequiredString(correlationId, "correlationId");
        validateRequiredString(priority, "priority");
        validateRequiredString(version, "version");
        validateRequiredString(author, "author");
        validateRequiredString(description, "description");
        validateRequiredString(documentation, "documentation");

        if (executionStartTime < 0) {
            addValidationError("executionStartTime must be >= 0");
        }
    }

    @Override
    protected void doReset() {
        protocol = "";
        clientId = "";
        sessionId = "";
        authContext = null;
        protocolContext = Map.of();
        executionStartTime = System.currentTimeMillis();
        correlationId = "";
        priority = "";
        version = "1.0.0";
        author = "openHAB AI Team";
        description = "";
        tags = List.of();
        properties = Map.of();
        created = null;
        lastModified = null;
        documentation = "";
        examples = List.of();
        requirements = Map.of();
    }
}
