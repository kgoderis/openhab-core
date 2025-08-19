package org.openhab.core.ai.common.builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Unified builder for communication-related objects in the openHAB AI system.
 *
 * <p>
 * This class provides a common builder pattern for creating communication-related objects
 * such as ConversationMessage, MessageOptions, EventSubscription, etc.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public abstract class CommunicationBuilder<T> extends AbstractBuilder<T> {

    protected String messageId = "";
    protected String conversationId = "";
    protected String fromAgentId = "";
    protected String toAgentId = "";
    protected String content = "";
    protected @Nullable Object messageType;
    protected @Nullable Instant timestamp;
    protected String eventType = "";
    protected String eventSource = "";
    protected Map<String, Object> eventData = Map.of();
    protected String subscriptionId = "";
    protected String subscriberId = "";
    protected List<String> eventTypes = List.of();
    protected Map<String, Object> subscriptionOptions = Map.of();
    protected String acknowledgmentId = "";
    protected String acknowledgmentStatus = "";
    protected @Nullable Instant acknowledgmentTime;
    protected String exportFormat = "";
    protected Map<String, Object> exportOptions = Map.of();
    protected String conversationTitle = "";
    protected List<String> participants = List.of();
    protected @Nullable Instant conversationStartTime;
    protected @Nullable Instant conversationEndTime;
    protected Map<String, Object> conversationMetadata = Map.of();

    /**
     * Set the message ID.
     *
     * @param messageId the message ID
     * @return this builder
     */
    public CommunicationBuilder<T> withMessageId(String messageId) {
        this.messageId = Objects.requireNonNull(messageId, "messageId");
        return this;
    }

    /**
     * Set the conversation ID.
     *
     * @param conversationId the conversation ID
     * @return this builder
     */
    public CommunicationBuilder<T> withConversationId(String conversationId) {
        this.conversationId = Objects.requireNonNull(conversationId, "conversationId");
        return this;
    }

    /**
     * Set the from agent ID.
     *
     * @param fromAgentId the from agent ID
     * @return this builder
     */
    public CommunicationBuilder<T> withFromAgentId(String fromAgentId) {
        this.fromAgentId = Objects.requireNonNull(fromAgentId, "fromAgentId");
        return this;
    }

    /**
     * Set the to agent ID.
     *
     * @param toAgentId the to agent ID
     * @return this builder
     */
    public CommunicationBuilder<T> withToAgentId(String toAgentId) {
        this.toAgentId = Objects.requireNonNull(toAgentId, "toAgentId");
        return this;
    }

    /**
     * Set the content.
     *
     * @param content the content
     * @return this builder
     */
    public CommunicationBuilder<T> withContent(String content) {
        this.content = Objects.requireNonNull(content, "content");
        return this;
    }

    /**
     * Set the message type.
     *
     * @param messageType the message type
     * @return this builder
     */
    public CommunicationBuilder<T> withMessageType(@Nullable Object messageType) {
        this.messageType = messageType;
        return this;
    }

    /**
     * Set the timestamp.
     *
     * @param timestamp the timestamp
     * @return this builder
     */
    public CommunicationBuilder<T> withTimestamp(@Nullable Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Set the event type.
     *
     * @param eventType the event type
     * @return this builder
     */
    public CommunicationBuilder<T> withEventType(String eventType) {
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        return this;
    }

    /**
     * Set the event source.
     *
     * @param eventSource the event source
     * @return this builder
     */
    public CommunicationBuilder<T> withEventSource(String eventSource) {
        this.eventSource = Objects.requireNonNull(eventSource, "eventSource");
        return this;
    }

    /**
     * Set the event data.
     *
     * @param eventData the event data
     * @return this builder
     */
    public CommunicationBuilder<T> withEventData(Map<String, Object> eventData) {
        this.eventData = Objects.requireNonNull(eventData, "eventData");
        return this;
    }

    /**
     * Set the subscription ID.
     *
     * @param subscriptionId the subscription ID
     * @return this builder
     */
    public CommunicationBuilder<T> withSubscriptionId(String subscriptionId) {
        this.subscriptionId = Objects.requireNonNull(subscriptionId, "subscriptionId");
        return this;
    }

    /**
     * Set the subscriber ID.
     *
     * @param subscriberId the subscriber ID
     * @return this builder
     */
    public CommunicationBuilder<T> withSubscriberId(String subscriberId) {
        this.subscriberId = Objects.requireNonNull(subscriberId, "subscriberId");
        return this;
    }

    /**
     * Set the event types.
     *
     * @param eventTypes the event types
     * @return this builder
     */
    public CommunicationBuilder<T> withEventTypes(List<String> eventTypes) {
        this.eventTypes = Objects.requireNonNull(eventTypes, "eventTypes");
        return this;
    }

    /**
     * Set the subscription options.
     *
     * @param subscriptionOptions the subscription options
     * @return this builder
     */
    public CommunicationBuilder<T> withSubscriptionOptions(Map<String, Object> subscriptionOptions) {
        this.subscriptionOptions = Objects.requireNonNull(subscriptionOptions, "subscriptionOptions");
        return this;
    }

    /**
     * Set the acknowledgment ID.
     *
     * @param acknowledgmentId the acknowledgment ID
     * @return this builder
     */
    public CommunicationBuilder<T> withAcknowledgmentId(String acknowledgmentId) {
        this.acknowledgmentId = Objects.requireNonNull(acknowledgmentId, "acknowledgmentId");
        return this;
    }

    /**
     * Set the acknowledgment status.
     *
     * @param acknowledgmentStatus the acknowledgment status
     * @return this builder
     */
    public CommunicationBuilder<T> withAcknowledgmentStatus(String acknowledgmentStatus) {
        this.acknowledgmentStatus = Objects.requireNonNull(acknowledgmentStatus, "acknowledgmentStatus");
        return this;
    }

    /**
     * Set the acknowledgment time.
     *
     * @param acknowledgmentTime the acknowledgment time
     * @return this builder
     */
    public CommunicationBuilder<T> withAcknowledgmentTime(@Nullable Instant acknowledgmentTime) {
        this.acknowledgmentTime = acknowledgmentTime;
        return this;
    }

    /**
     * Set the export format.
     *
     * @param exportFormat the export format
     * @return this builder
     */
    public CommunicationBuilder<T> withExportFormat(String exportFormat) {
        this.exportFormat = Objects.requireNonNull(exportFormat, "exportFormat");
        return this;
    }

    /**
     * Set the export options.
     *
     * @param exportOptions the export options
     * @return this builder
     */
    public CommunicationBuilder<T> withExportOptions(Map<String, Object> exportOptions) {
        this.exportOptions = Objects.requireNonNull(exportOptions, "exportOptions");
        return this;
    }

    /**
     * Set the conversation title.
     *
     * @param conversationTitle the conversation title
     * @return this builder
     */
    public CommunicationBuilder<T> withConversationTitle(String conversationTitle) {
        this.conversationTitle = Objects.requireNonNull(conversationTitle, "conversationTitle");
        return this;
    }

    /**
     * Set the participants.
     *
     * @param participants the participants
     * @return this builder
     */
    public CommunicationBuilder<T> withParticipants(List<String> participants) {
        this.participants = Objects.requireNonNull(participants, "participants");
        return this;
    }

    /**
     * Set the conversation start time.
     *
     * @param conversationStartTime the conversation start time
     * @return this builder
     */
    public CommunicationBuilder<T> withConversationStartTime(@Nullable Instant conversationStartTime) {
        this.conversationStartTime = conversationStartTime;
        return this;
    }

    /**
     * Set the conversation end time.
     *
     * @param conversationEndTime the conversation end time
     * @return this builder
     */
    public CommunicationBuilder<T> withConversationEndTime(@Nullable Instant conversationEndTime) {
        this.conversationEndTime = conversationEndTime;
        return this;
    }

    /**
     * Set the conversation metadata.
     *
     * @param conversationMetadata the conversation metadata
     * @return this builder
     */
    public CommunicationBuilder<T> withConversationMetadata(Map<String, Object> conversationMetadata) {
        this.conversationMetadata = Objects.requireNonNull(conversationMetadata, "conversationMetadata");
        return this;
    }

    @Override
    protected void validate() {
        validateRequiredString(messageId, "messageId");
        validateRequiredString(conversationId, "conversationId");
        validateRequiredString(fromAgentId, "fromAgentId");
        validateRequiredString(content, "content");
        validateRequiredString(eventType, "eventType");
        validateRequiredString(eventSource, "eventSource");
        validateRequiredString(subscriptionId, "subscriptionId");
        validateRequiredString(subscriberId, "subscriberId");
        validateRequiredString(acknowledgmentId, "acknowledgmentId");
        validateRequiredString(acknowledgmentStatus, "acknowledgmentStatus");
        validateRequiredString(exportFormat, "exportFormat");
        validateRequiredString(conversationTitle, "conversationTitle");
    }

    @Override
    protected void doReset() {
        messageId = "";
        conversationId = "";
        fromAgentId = "";
        toAgentId = "";
        content = "";
        messageType = null;
        timestamp = null;
        eventType = "";
        eventSource = "";
        eventData = Map.of();
        subscriptionId = "";
        subscriberId = "";
        eventTypes = List.of();
        subscriptionOptions = Map.of();
        acknowledgmentId = "";
        acknowledgmentStatus = "";
        acknowledgmentTime = null;
        exportFormat = "";
        exportOptions = Map.of();
        conversationTitle = "";
        participants = List.of();
        conversationStartTime = null;
        conversationEndTime = null;
        conversationMetadata = Map.of();
    }
}
