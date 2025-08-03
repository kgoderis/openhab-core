package org.openhab.core.ai.a2a.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionRegistry;
import org.openhab.core.ai.common.config.AIConfigurationService;
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
 * A2A Protocol Handler - Main entry point for A2A protocol operations.
 * 
 * <p>
 * This class implements the A2A RequestHandler interface and coordinates with
 * other A2A components to handle protocol requests and responses.
 * </p>
 * 
 * <p>
 * <strong>Note on @NonNullByDefault:</strong> This class intentionally does not use @NonNullByDefault
 * to maintain compatibility with the A2A SDK interfaces. The A2A SDK (version 0.2.5) does not use
 * nullability annotations on its interface methods, which would conflict with @NonNullByDefault's
 * strict null safety requirements. Instead, this class uses explicit @NonNull and @Nullable annotations
 * where appropriate to provide null safety while maintaining SDK compatibility.
 * </p>
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@Component(service = A2AProtocolHandler.class, immediate = true)
public class A2AProtocolHandler implements ReadyTracker, RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(A2AProtocolHandler.class);

    // Ready markers for A2A server
    public static final ReadyMarker A2A_SERVER_READY = new ReadyMarker("a2a", "server");
    public static final ReadyMarker A2A_SERVER_COMPONENTS_READY = new ReadyMarker("a2a", "server-components");

    // Core openHAB services we depend on
    private static final ReadyMarker CORE_THINGS_READY = new ReadyMarker("startlevel", "80");
    private static final ReadyMarker CORE_RULES_READY = new ReadyMarker("startlevel", "50");

    @Reference
    private @Nullable ReadyService readyService;

    @Reference
    private @Nullable AIActionRegistry actionRegistry;

    @Reference
    private @Nullable AIConfigurationService configurationService;

    @Reference
    private @Nullable A2ATaskManager taskManager;

    @Reference
    private @Nullable A2AStreamingManager streamingManager;

    @Reference
    private @Nullable A2APushNotificationManager pushNotificationManager;

    @Reference
    private @Nullable A2AAgentCardBuilder agentCardBuilder;

    @Reference
    private @Nullable A2AConfigurationManager configurationManager;

    private boolean isRunning = false;

    @Activate
    public void activate() {
        logger.debug("A2A Protocol Handler activated");

        // Register as a tracker for core openHAB services
        if (readyService != null) {
            readyService.registerTracker(this);
        }

        // Check if core services are already ready
        checkCoreServicesReady();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("A2A Protocol Handler deactivated");

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
            readyService.unmarkReady(A2A_SERVER_COMPONENTS_READY);
            readyService.unmarkReady(A2A_SERVER_READY);
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

    @Override
    public java.util.concurrent.Flow.Publisher<StreamingEventKind> onMessageSendStream(
            @Nullable MessageSendParams params) throws JSONRPCError {
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
    public java.util.List<TaskPushNotificationConfig> onListTaskPushNotificationConfig(
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
    public java.util.concurrent.Flow.Publisher<StreamingEventKind> onResubscribeToTask(@Nullable TaskIdParams params)
            throws JSONRPCError {
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
            logger.warn("A2A Protocol Handler is already running");
            return;
        }

        try {
            logger.info("Starting A2A Protocol Handler");

            // Initialize components if not already done
            if (configurationManager != null) {
                configurationManager.initializeComponents();
            }

            isRunning = true;
            if (readyService != null) {
                readyService.markReady(A2A_SERVER_READY);
            }
            logger.info("A2A Protocol Handler started successfully");

        } catch (Exception e) {
            logger.error("Failed to start A2A Protocol Handler", e);
            throw new RuntimeException("Failed to start A2A Protocol Handler", e);
        }
    }

    public void stop() {
        if (!isRunning) {
            logger.warn("A2A Protocol Handler is not running");
            return;
        }

        try {
            logger.info("Stopping A2A Protocol Handler");

            isRunning = false;
            if (readyService != null) {
                readyService.unmarkReady(A2A_SERVER_READY);
            }
            logger.info("A2A Protocol Handler stopped successfully");

        } catch (Exception e) {
            logger.error("Failed to stop A2A Protocol Handler", e);
            throw new RuntimeException("Failed to stop A2A Protocol Handler", e);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    // ============================================================================
    // Public API Methods
    // ============================================================================

    public io.a2a.spec.AgentCard getAgentCard() {
        if (agentCardBuilder != null) {
            return agentCardBuilder.buildAgentCard();
        } else {
            logger.error("AgentCardBuilder not available");
            throw new RuntimeException("AgentCardBuilder not available");
        }
    }

    public Object executeSkill(io.a2a.spec.Message message) {
        if (taskManager != null) {
            return taskManager.executeSkill(message);
        } else {
            logger.error("TaskManager not available for skill execution");
            return Map.of("error", "TaskManager not available");
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
                readyService.unmarkReady(A2A_SERVER_COMPONENTS_READY);
                readyService.unmarkReady(A2A_SERVER_READY);
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
        return A2ASkillRegistry.A2A_SKILLS_READY.equals(marker);
    }

    private void checkCoreServicesReady() {
        logger.debug("Checking core services readiness");

        if (readyService == null) {
            return;
        }

        boolean thingsReady = readyService.isReady(CORE_THINGS_READY);
        boolean rulesReady = readyService.isReady(CORE_RULES_READY);

        if (thingsReady && rulesReady) {
            logger.debug("Core services are ready, initializing A2A server components");
            if (configurationManager != null) {
                configurationManager.initializeComponents();
            }
            readyService.markReady(A2A_SERVER_COMPONENTS_READY);
        } else {
            logger.debug("Core services not ready yet - things: {}, rules: {}", thingsReady, rulesReady);
        }
    }

    private void checkSkillsReady() {
        logger.debug("Checking skills readiness");

        if (readyService != null && readyService.isReady(A2ASkillRegistry.A2A_SKILLS_READY)) {
            logger.debug("Skills are ready, A2A server is fully initialized");
            // Skills are ready, server can start
        } else {
            logger.debug("Skills not ready yet");
        }
    }
}
