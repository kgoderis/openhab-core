package org.openhab.core.ai.agent.communication.protocol;

import java.util.List;
import java.util.concurrent.Flow;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionRegistry;
import org.openhab.core.ai.agent.communication.notifications.AgentPushNotificationManager;
import org.openhab.core.ai.agent.communication.streaming.AgentStreamingManager;
import org.openhab.core.ai.agent.delegation.AgentCardBuilder;
import org.openhab.core.ai.agent.execution.AgentSkillRegistry;
import org.openhab.core.ai.agent.execution.AgentTaskManager;
import org.openhab.core.ai.agent.lifecycle.AgentConfigurationManager;
import org.openhab.core.ai.config.ConfigurationService;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyService;
import org.openhab.core.service.ReadyService.ReadyTracker;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.spec.AgentCard;
import io.a2a.spec.DeleteTaskPushNotificationConfigParams;
import io.a2a.spec.EventKind;
import io.a2a.spec.GetTaskPushNotificationConfigParams;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.ListTaskPushNotificationConfigParams;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.StreamingEventKind;
import io.a2a.spec.Task;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskQueryParams;

/**
 * A2A Protocol Communication Handler.
 * 
 * <p>
 * <strong>Primary Responsibility:</strong> A2A Protocol Communication Handler
 * </p>
 * 
 * <p>
 * <strong>Concerns:</strong>
 * <ul>
 * <li><strong>Protocol Communication:</strong> Handles incoming A2A protocol messages</li>
 * <li><strong>Message Routing:</strong> Routes messages to appropriate handlers</li>
 * <li><strong>Protocol Compliance:</strong> Ensures A2A SDK compliance</li>
 * <li><strong>Message Validation:</strong> Validates incoming message format</li>
 * <li><strong>Error Handling:</strong> Handles protocol-level errors</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES:</strong>
 * <ul>
 * <li>Handles A2A protocol messages via {@link #onMessageSend(MessageSendParams)}</li>
 * <li>Routes messages to {@link AgentTaskManager} for task creation</li>
 * <li>Validates message format and protocol compliance</li>
 * <li>Creates protocol-compliant responses</li>
 * <li>Handles protocol-level errors</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>What this class DOES NOT do:</strong>
 * <ul>
 * <li>❌ Execute skills or actions (delegates to AgentTaskManager)</li>
 * <li>❌ Manage skill registration (delegates to AgentSkillRegistry)</li>
 * <li>❌ Handle business logic (delegates to execution layers)</li>
 * <li>❌ Manage authentication (delegates to security manager)</li>
 * <li>❌ Manage task lifecycle (delegates to AgentTaskManager)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Boundary Conditions:</strong>
 * <ul>
 * <li>Only handles A2A protocol messages</li>
 * <li>Delegates all execution to AgentTaskManager</li>
 * <li>Does not maintain state beyond message handling</li>
 * <li>Does not contain business logic</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Dependencies:</strong>
 * <ul>
 * <li>{@link AgentTaskManager}: For task creation and management</li>
 * <li>A2A SDK classes: For protocol compliance</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>Architecture Layer:</strong> A2A Protocol Layer
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@Component(service = AgentProtocolHandler.class, immediate = true)
public class AgentProtocolHandler implements ReadyTracker, RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(AgentProtocolHandler.class);

    // Ready markers for Agent server
    public static final ReadyMarker AGENT_SERVER_READY = new ReadyMarker("agent", "server");
    public static final ReadyMarker AGENT_SERVER_COMPONENTS_READY = new ReadyMarker("agent", "server-components");

    // Core openHAB services we depend on
    private static final ReadyMarker CORE_THINGS_READY = new ReadyMarker("startlevel", "80");
    private static final ReadyMarker CORE_RULES_READY = new ReadyMarker("startlevel", "50");

    @Reference
    private @Nullable ReadyService readyService;

    @Reference
    private @Nullable ActionRegistry actionRegistry;

    @Reference
    private @Nullable ConfigurationService configurationService;

    @Reference
    private @Nullable AgentTaskManager taskManager;

    @Reference
    private @Nullable AgentStreamingManager streamingManager;

    @Reference
    private @Nullable AgentPushNotificationManager pushNotificationManager;

    @Reference
    private @Nullable AgentCardBuilder agentCardBuilder;

    @Reference
    private @Nullable AgentConfigurationManager configurationManager;

    private boolean isRunning = false;

    @Activate
    public void activate() {
        logger.debug("Agent Protocol Handler activated");

        // Register as a tracker for core openHAB services
        if (readyService != null) {
            readyService.registerTracker(this);
        }

        // Check if core services are already ready
        checkCoreServicesReady();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Agent Protocol Handler deactivated");

        // Stop the server if running
        if (isRunning) {
            stop();
        }

        // Unregister tracker
        if (readyService != null) {
            readyService.unregisterTracker(this);
        }

        // Unmark ready markers
        if (readyService != null) {
            readyService.unmarkReady(AGENT_SERVER_COMPONENTS_READY);
            readyService.unmarkReady(AGENT_SERVER_READY);
        }
    }

    // ============================================================================
    // RequestHandler Interface Implementation
    // ============================================================================

    @Override
    public EventKind onMessageSend(@Nullable MessageSendParams params) throws JSONRPCError {
        logger.debug("Processing message send request: {}", params);

        if (params == null) {
            throw new JSONRPCError(-32602, "MessageSendParams cannot be null", null);
        }

        try {
            // Delegate to task manager
            if (taskManager != null) {
                return taskManager.handleMessageSend(params);
            } else {
                throw new JSONRPCError(-32603, "Internal error: TaskManager not available", null);
            }
        } catch (Exception e) {
            logger.error("Error processing message send request", e);
            throw new JSONRPCError(-32603, "Internal error: " + e.getMessage(), null);
        }
    }

    @Override
    public Task onGetTask(@Nullable TaskQueryParams params) throws JSONRPCError {
        logger.debug("Getting task: {}", params != null ? params.id() : "null");

        if (params == null) {
            throw new JSONRPCError(-32602, "TaskQueryParams cannot be null", null);
        }

        if (taskManager != null) {
            return taskManager.getTask(params.id());
        } else {
            throw new JSONRPCError(-32603, "Internal error: TaskManager not available", null);
        }
    }

    @Override
    public Task onCancelTask(@Nullable TaskIdParams params) throws JSONRPCError {
        logger.debug("Cancelling task: {}", params != null ? params.id() : "null");

        if (params == null) {
            throw new JSONRPCError(-32602, "TaskIdParams cannot be null", null);
        }

        if (taskManager != null) {
            return taskManager.cancelTask(params.id());
        } else {
            throw new JSONRPCError(-32603, "Internal error: TaskManager not available", null);
        }
    }

    public List<Task> onListTasks(@Nullable TaskQueryParams params) throws JSONRPCError {
        logger.debug("Listing tasks with params: {}", params);

        if (taskManager != null) {
            return taskManager.listTasks(params);
        } else {
            throw new JSONRPCError(-32603, "Internal error: TaskManager not available", null);
        }
    }

    @Override
    public Flow.Publisher<StreamingEventKind> onMessageSendStream(@Nullable MessageSendParams params)
            throws JSONRPCError {
        logger.debug("Processing streaming message send request: {}", params);

        if (params == null) {
            throw new JSONRPCError(-32602, "MessageSendParams cannot be null", null);
        }

        if (streamingManager != null) {
            return streamingManager.handleStreamingMessageSend(params);
        } else {
            throw new JSONRPCError(-32603, "Internal error: StreamingManager not available", null);
        }
    }

    @Override
    public TaskPushNotificationConfig onSetTaskPushNotificationConfig(@Nullable TaskPushNotificationConfig config)
            throws JSONRPCError {
        logger.debug("Setting task push notification config: {}", config);

        if (config == null) {
            throw new JSONRPCError(-32602, "TaskPushNotificationConfig cannot be null", null);
        }

        if (pushNotificationManager != null) {
            return pushNotificationManager.setTaskPushNotificationConfig(config);
        } else {
            throw new JSONRPCError(-32603, "Internal error: PushNotificationManager not available", null);
        }
    }

    @Override
    public TaskPushNotificationConfig onGetTaskPushNotificationConfig(
            @Nullable GetTaskPushNotificationConfigParams params) throws JSONRPCError {
        logger.debug("Getting task push notification config for task: {}", params != null ? params.id() : "null");

        if (params == null) {
            throw new JSONRPCError(-32602, "GetTaskPushNotificationConfigParams cannot be null", null);
        }

        if (pushNotificationManager != null) {
            return pushNotificationManager.getTaskPushNotificationConfig(params.id());
        } else {
            throw new JSONRPCError(-32603, "Internal error: PushNotificationManager not available", null);
        }
    }

    @Override
    public List<TaskPushNotificationConfig> onListTaskPushNotificationConfig(
            @Nullable ListTaskPushNotificationConfigParams params) throws JSONRPCError {
        logger.debug("Listing task push notification configs for task: {}", params != null ? params.id() : "null");

        if (params == null) {
            throw new JSONRPCError(-32602, "ListTaskPushNotificationConfigParams cannot be null", null);
        }

        if (pushNotificationManager != null) {
            return pushNotificationManager.listTaskPushNotificationConfigs(params.id());
        } else {
            throw new JSONRPCError(-32603, "Internal error: PushNotificationManager not available", null);
        }
    }

    @Override
    public void onDeleteTaskPushNotificationConfig(@Nullable DeleteTaskPushNotificationConfigParams params)
            throws JSONRPCError {
        logger.debug("Deleting task push notification config: {} for task: {}", params != null ? params.id() : "null",
                params != null ? params.id() : "null");

        if (params == null) {
            throw new JSONRPCError(-32602, "DeleteTaskPushNotificationConfigParams cannot be null", null);
        }

        if (pushNotificationManager != null) {
            pushNotificationManager.deleteTaskPushNotificationConfig(params.id());
        } else {
            throw new JSONRPCError(-32603, "Internal error: PushNotificationManager not available", null);
        }
    }

    @Override
    public Flow.Publisher<StreamingEventKind> onResubscribeToTask(@Nullable TaskIdParams params) throws JSONRPCError {
        logger.debug("Resubscribing to task: {}", params != null ? params.id() : "null");

        if (params == null) {
            throw new JSONRPCError(-32602, "TaskIdParams cannot be null", null);
        }

        if (streamingManager != null) {
            return streamingManager.resubscribeToTask(params.id());
        } else {
            throw new JSONRPCError(-32603, "Internal error: StreamingManager not available", null);
        }
    }

    // ============================================================================
    // Server Lifecycle Management
    // ============================================================================

    public void start() {
        if (isRunning) {
            logger.warn("Agent Protocol Handler is already running");
            return;
        }

        try {
            logger.info("Starting Agent Protocol Handler");

            // Initialize components if not already done
            if (configurationManager != null) {
                configurationManager.initializeComponents();
            }

            isRunning = true;
            if (readyService != null) {
                readyService.markReady(AGENT_SERVER_READY);
            }
            logger.info("Agent Protocol Handler started successfully");

        } catch (Exception e) {
            logger.error("Failed to start Agent Protocol Handler", e);
            throw new RuntimeException("Failed to start Agent Protocol Handler", e);
        }
    }

    public void stop() {
        if (!isRunning) {
            logger.warn("Agent Protocol Handler is not running");
            return;
        }

        try {
            logger.info("Stopping Agent Protocol Handler");

            isRunning = false;
            if (readyService != null) {
                readyService.unmarkReady(AGENT_SERVER_READY);
            }
            logger.info("Agent Protocol Handler stopped successfully");

        } catch (Exception e) {
            logger.error("Failed to stop Agent Protocol Handler", e);
            throw new RuntimeException("Failed to stop Agent Protocol Handler", e);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    // ============================================================================
    // Public API Methods
    // ============================================================================

    public AgentCard getAgentCard() {
        if (agentCardBuilder != null) {
            return agentCardBuilder.buildAgentCard();
        } else {
            logger.error("AgentCardBuilder not available");
            throw new RuntimeException("AgentCardBuilder not available");
        }
    }

    // ============================================================================
    // ReadyTracker Implementation
    // ============================================================================

    @Override
    public void onReadyMarkerAdded(ReadyMarker readyMarker) {
        logger.debug("Ready marker added: {}", readyMarker);

        if (isCoreServiceMarker(readyMarker)) {
            checkCoreServicesReady();
        } else if (isSkillRegistryMarker(readyMarker)) {
            checkSkillsReady();
        }
    }

    @Override
    public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
        logger.debug("Ready marker removed: {}", readyMarker);

        if (isCoreServiceMarker(readyMarker)) {
            // Core service became unavailable
            if (readyService != null) {
                readyService.unmarkReady(AGENT_SERVER_COMPONENTS_READY);
                readyService.unmarkReady(AGENT_SERVER_READY);
            }
        }
    }

    // ============================================================================
    // Private Helper Methods
    // ============================================================================

    private boolean isCoreServiceMarker(ReadyMarker marker) {
        return CORE_THINGS_READY.equals(marker) || CORE_RULES_READY.equals(marker);
    }

    private boolean isSkillRegistryMarker(ReadyMarker marker) {
        return AgentSkillRegistry.AGENT_SKILLS_READY.equals(marker);
    }

    private void checkCoreServicesReady() {
        logger.debug("Checking core services readiness");

        if (readyService == null) {
            return;
        }

        boolean thingsReady = readyService.isReady(CORE_THINGS_READY);
        boolean rulesReady = readyService.isReady(CORE_RULES_READY);

        if (thingsReady && rulesReady) {
            logger.debug("Core services are ready, initializing Agent server components");
            if (configurationManager != null) {
                configurationManager.initializeComponents();
            }
            readyService.markReady(AGENT_SERVER_COMPONENTS_READY);
        } else {
            logger.debug("Core services not ready yet - things: {}, rules: {}", thingsReady, rulesReady);
        }
    }

    private void checkSkillsReady() {
        logger.debug("Checking skills readiness");

        if (readyService != null && readyService.isReady(AgentSkillRegistry.AGENT_SKILLS_READY)) {
            logger.debug("Skills are ready, Agent server is fully initialized");
            // Skills are ready, server can start
        } else {
            logger.debug("Skills not ready yet");
        }
    }
}
