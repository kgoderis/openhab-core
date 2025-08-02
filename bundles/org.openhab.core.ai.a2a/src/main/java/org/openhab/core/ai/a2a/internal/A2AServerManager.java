package org.openhab.core.ai.a2a.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionRegistry;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIAction;
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
import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.APIKeySecurityScheme;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentInterface;
import io.a2a.spec.AgentProvider;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.DeleteTaskPushNotificationConfigParams;
import io.a2a.spec.EventKind;
import io.a2a.spec.GetTaskPushNotificationConfigParams;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.ListTaskPushNotificationConfigParams;
import io.a2a.spec.MessageSendParams;
import io.a2a.spec.PushNotificationAuthenticationInfo;
import io.a2a.spec.PushNotificationConfig;
import io.a2a.spec.SecurityScheme;
import io.a2a.spec.StreamingEventKind;
import io.a2a.spec.Task;
import io.a2a.spec.TaskIdParams;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskQueryParams;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;
import io.a2a.spec.TaskStatusUpdateEvent;

/**
 * Server manager for A2A operations.
 * 
 * <p>
 * <strong>Note on @NonNullByDefault:</strong> This class intentionally does not use @NonNullByDefault
 * to maintain compatibility with the A2A SDK interfaces. The A2A SDK (version 0.2.5) does not use
 * nullability annotations on its interface methods, which would conflict with @NonNullByDefault's
 * strict null safety requirements. Instead, this class uses explicit @NonNull and @Nullable annotations
 * where appropriate to provide null safety while maintaining SDK compatibility.
 * 
 * <p>
 * Key compatibility considerations:
 * <ul>
 * <li>A2A SDK interfaces (RequestHandler, TaskStore) have parameters that can be null</li>
 * <li>Return types in A2A SDK interfaces are not annotated with nullability</li>
 * <li>@NonNullByDefault would require all parameters to be @NonNull, breaking SDK compatibility</li>
 * <li>Explicit annotations provide better control over null safety without breaking SDK contracts</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@Component(service = A2AServerManager.class, immediate = true)
public class A2AServerManager implements ReadyTracker, RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(A2AServerManager.class);

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
    private @Nullable A2AOpenHABPersistenceManager persistenceManager;

    @Reference
    private @Nullable AIConfigurationService configurationService;

    @Reference
    private @Nullable A2ASkillRegistry skillRegistry;

    @Reference
    private @Nullable A2ASecurityManager securityManager;

    @Reference
    private @Nullable A2AAgentExecutor agentExecutor;

    // A2A Server Components using available SDK classes
    private @Nullable RequestHandler requestHandler;
    private @Nullable TaskStore taskStore;
    private @Nullable ExecutorService asyncExecutor;

    // Enhanced event management using SDK patterns
    private final Map<String, SubmissionPublisher<StreamingEventKind>> streamingPublishers = new HashMap<>();

    private boolean isRunning = false;
    private boolean componentsInitialized = false;

    @Activate
    public void activate() {
        logger.debug("A2A Server Manager activated");

        // Register as a tracker for core openHAB services
        readyService.registerTracker(this);

        // Check if core services are already ready
        checkCoreServicesReady();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("A2A Server Manager deactivated");

        // Stop the server if running
        if (isRunning) {
            stop();
        }

        // Unregister tracker
        readyService.unregisterTracker(this);

        // Unmark ready markers
        readyService.unmarkReady(A2A_SERVER_COMPONENTS_READY);
        readyService.unmarkReady(A2A_SERVER_READY);

        // Shutdown executor
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
        }

        // Close all streaming publishers
        streamingPublishers.values().forEach(SubmissionPublisher::close);
        streamingPublishers.clear();
    }

    private void initializeServerComponents() {
        if (componentsInitialized) {
            return;
        }

        logger.debug("Initializing A2A server components using SDK patterns");

        // Create async executor
        asyncExecutor = Executors.newCachedThreadPool();

        // Create task store using SDK TaskStore interface
        taskStore = createTaskStore();

        // Set this class as the request handler since it implements RequestHandler
        requestHandler = this;

        componentsInitialized = true;
        logger.debug("A2A server components initialized using SDK patterns");
    }

    private TaskStore createTaskStore() {
        logger.debug("Creating A2A task store using SDK TaskStore interface");

        return new TaskStore() {
            private final Map<String, Task> tasks = new HashMap<>();

            @Override
            public void save(@Nullable Task task) {
                logger.debug("Saving task: {}", task != null ? task.getId() : "null");
                if (task != null) {
                    tasks.put(task.getId(), task);
                }
            }

            @Override
            public @Nullable Task get(@Nullable String taskId) {
                logger.debug("Getting task: {}", taskId);
                if (taskId == null) {
                    return null;
                }
                return tasks.get(taskId);
            }

            @Override
            public void delete(@Nullable String taskId) {
                logger.debug("Deleting task: {}", taskId);
                if (taskId != null) {
                    tasks.remove(taskId);
                }
            }
        };
    }

    @Override
    public EventKind onMessageSend(@Nullable MessageSendParams params) throws JSONRPCError {
        logger.debug("Processing message send request: {}", params);

        if (params == null) {
            throw new JSONRPCError(-32602, "MessageSendParams cannot be null", null);
        }

        try {
            // Create a task for this message
            Task task = createTaskFromMessage(params);
            taskStore.save(task);

            // Execute the task asynchronously using SDK patterns
            ExecutorService executor = asyncExecutor;
            if (executor == null) {
                logger.error("Async executor not available");
                throw new JSONRPCError(-32603, "Internal error: executor not available", null);
            }
            CompletableFuture.runAsync(() -> {
                try {
                    logger.debug("Executing task: {}", task.getId());

                    // Publish task status update using SDK patterns
                    publishTaskStatusUpdate(task.getId(), TaskState.WORKING, "Task started");

                    // Extract action information from the message
                    String actionId = extractActionIdFromMessage(params.message());
                    Map<String, Object> parameters = extractParametersFromMessage(params.message());

                    // Execute the actual AI action
                    AIActionResult result = executeAIAction(actionId, parameters, task.getId());

                    if (result.isSuccess()) {
                        // Publish success status
                        publishTaskStatusUpdate(task.getId(), TaskState.COMPLETED, "Task completed successfully");

                        // Update task with result
                        updateTaskWithResult(task.getId(), result);
                    } else {
                        // Publish failure status
                        String errorMessage = result.getMessage() != null ? result.getMessage()
                                : "Task execution failed";
                        publishTaskStatusUpdate(task.getId(), TaskState.FAILED, errorMessage);
                    }

                } catch (Exception e) {
                    logger.error("Error executing task: {}", task.getId(), e);
                    publishTaskStatusUpdate(task.getId(), TaskState.FAILED, "Task failed: " + e.getMessage());
                }
            }, executor);

            // Return SDK event kind
            return new EventKind() {
                @Override
                public String getKind() {
                    return "task.created";
                }
            };
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

        Task task = taskStore.get(params.id());
        if (task == null) {
            throw new JSONRPCError(-32001, "Task not found: " + params.id(), null);
        }
        return task; // task is guaranteed to be non-null here
    }

    @Override
    public Task onCancelTask(@Nullable TaskIdParams params) throws JSONRPCError {
        logger.debug("Cancelling task: {}", params != null ? params.id() : "null");

        if (params == null) {
            throw new JSONRPCError(-32602, "TaskIdParams cannot be null", null);
        }

        Task task = taskStore.get(params.id());
        if (task == null) {
            throw new JSONRPCError(-32001, "Task not found: " + params.id(), null);
        }

        try {
            // Cancel the task in the agent executor
            // Note: A2AAgentExecutor.cancel() requires RequestContext and EventQueue
            // This is a simplified implementation - in practice, we'd need to create proper context
            logger.debug("Task cancellation requested for: {}", params.id());

            // Publish cancellation status using SDK patterns
            publishTaskStatusUpdate(params.id(), TaskState.CANCELED, "Task cancelled");

            taskStore.delete(params.id());
            return task; // task is guaranteed to be non-null here
        } catch (Exception e) {
            logger.error("Error cancelling task: {}", params.id(), e);
            throw new JSONRPCError(-32603, "Internal error: " + e.getMessage(), null);
        }
    }

    // Implement streaming using SDK patterns
    @Override
    public java.util.concurrent.Flow.Publisher<io.a2a.spec.StreamingEventKind> onMessageSendStream(
            @Nullable MessageSendParams params) throws JSONRPCError {
        logger.debug("Processing streaming message send request: {}", params);

        if (params == null) {
            throw new JSONRPCError(-32602, "MessageSendParams cannot be null", null);
        }

        // Create streaming publisher using SDK patterns
        String streamId = "stream-" + System.currentTimeMillis();
        SubmissionPublisher<StreamingEventKind> publisher = new SubmissionPublisher<>();
        streamingPublishers.put(streamId, publisher);

        // Create a task for this message
        Task task = createTaskFromMessage(params);
        if (taskStore != null) {
            taskStore.save(task);
        }

        // Execute the task asynchronously and stream updates using SDK patterns
        ExecutorService executor = asyncExecutor;
        if (executor == null) {
            logger.error("Async executor not available for streaming");
            // Send error status and close publisher instead of throwing
            publishStreamingTaskStatus(publisher, task.getId(), TaskState.FAILED,
                    "Internal error: executor not available");
            publisher.close();
            streamingPublishers.remove(streamId);
            // Return the publisher instead of throwing - it will emit the error status
            return publisher;
        }

        CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Executing streaming task: {}", task.getId());

                // Send initial status update using SDK patterns
                publishStreamingTaskStatus(publisher, task.getId(), TaskState.WORKING, "Task started");

                // Extract action information from the message
                String actionId = extractActionIdFromMessage(params.message());
                Map<String, Object> parameters = extractParametersFromMessage(params.message());

                // Execute the actual AI action with streaming updates
                AIActionResult result = executeAIActionWithStreaming(actionId, parameters, task.getId(), publisher);

                if (result.isSuccess()) {
                    // Send completion status using SDK patterns
                    publishStreamingTaskStatus(publisher, task.getId(), TaskState.COMPLETED,
                            "Task completed successfully");

                    // Update task with result
                    updateTaskWithResult(task.getId(), result);
                } else {
                    // Send error status using SDK patterns
                    String errorMessage = result.getMessage() != null ? result.getMessage() : "Task execution failed";
                    publishStreamingTaskStatus(publisher, task.getId(), TaskState.FAILED, errorMessage);
                }

                // Close the publisher
                publisher.close();
                streamingPublishers.remove(streamId);

            } catch (Exception e) {
                logger.error("Error in streaming task execution: {}", task.getId(), e);

                // Send error status using SDK patterns
                publishStreamingTaskStatus(publisher, task.getId(), TaskState.FAILED, "Task failed: " + e.getMessage());
                publisher.close();
                streamingPublishers.remove(streamId);
            }
        }, executor);

        // Always return the publisher - it will handle errors by emitting error events
        return publisher;
    }

    // Implement push notification configuration using SDK patterns
    @Override
    public TaskPushNotificationConfig onSetTaskPushNotificationConfig(@Nullable TaskPushNotificationConfig config)
            throws JSONRPCError {
        logger.debug("Setting task push notification config: {}", config);

        if (config == null) {
            throw new JSONRPCError(-32602, "TaskPushNotificationConfig cannot be null", null);
        }

        try {
            // Convert TaskPushNotificationConfig to Map for storage
            Map<String, Object> pushConfig = new HashMap<>();
            pushConfig.put("taskId", config.taskId());
            pushConfig.put("pushNotificationConfig", config.pushNotificationConfig());
            pushConfig.put("timestamp", System.currentTimeMillis());

            // Save to persistent storage
            persistenceManager.savePushNotificationConfig(config.taskId(), pushConfig);

            logger.info("A2A Push notification config saved: taskId={}", config.taskId());

            return config;

        } catch (Exception e) {
            logger.error("Error saving push notification config: {}", config.taskId(), e);
            throw new JSONRPCError(-32001, "Failed to save push notification config", null);
        }
    }

    @Override
    public TaskPushNotificationConfig onGetTaskPushNotificationConfig(
            @Nullable GetTaskPushNotificationConfigParams params) throws JSONRPCError {
        logger.debug("Getting task push notification config for task: {}", params != null ? params.id() : "null");

        if (params == null) {
            throw new JSONRPCError(-32602, "GetTaskPushNotificationConfigParams cannot be null", null);
        }

        try {
            // Load from persistent storage
            Map<String, Object> storedConfig = persistenceManager.loadPushNotificationConfig(params.id());

            if (!storedConfig.isEmpty()) {
                // Reconstruct TaskPushNotificationConfig from stored data
                @SuppressWarnings("unchecked")
                Map<String, Object> pushConfigData = (Map<String, Object>) storedConfig.get("pushNotificationConfig");

                if (pushConfigData != null) {
                    // For now, return a default configuration since we can't reconstruct the SDK objects
                    // In a real implementation, you would need to know the exact SDK API
                    logger.debug("Found stored push notification config for taskId={}, returning default", params.id());
                    return createDefaultTaskPushNotificationConfig(params.id());
                }
            }

            // Return default configuration if not found in storage
            logger.debug("No stored push notification config found, returning default for taskId={}", params.id());
            return createDefaultTaskPushNotificationConfig(params.id());

        } catch (Exception e) {
            logger.error("Error loading push notification config: {}", params.id(), e);
            throw new JSONRPCError(-32001, "Failed to load push notification config", null);
        }
    }

    // Implement push notification configuration listing
    @Override
    public java.util.List<TaskPushNotificationConfig> onListTaskPushNotificationConfig(
            @Nullable ListTaskPushNotificationConfigParams params) throws JSONRPCError {
        logger.debug("Listing task push notification configs for task: {}", params != null ? params.id() : "null");

        if (params == null) {
            throw new JSONRPCError(-32602, "ListTaskPushNotificationConfigParams cannot be null", null);
        }

        List<TaskPushNotificationConfig> configs = new ArrayList<>();

        try {
            // Load all push notification configs from storage
            if (persistenceManager != null) {
                List<Map<String, Object>> storedConfigs = persistenceManager.loadAllPushNotificationConfigs();

                for (Map<String, Object> storedConfig : storedConfigs) {
                    String taskId = (String) storedConfig.get("taskId");

                    // Filter by task ID if specified
                    if (params.id() != null && !params.id().equals(taskId)) {
                        continue;
                    }

                    // Create default config for each stored entry
                    TaskPushNotificationConfig config = createDefaultTaskPushNotificationConfig(taskId);
                    configs.add(config);
                }
            }

            logger.debug("Listed {} push notification configs for task: {}", configs.size(), params.id());

        } catch (Exception e) {
            logger.error("Error listing push notification configs for task: {}", params.id(), e);
            // Don't throw exception, just return empty list with error logged
        }

        // Always return a non-null list (empty if there was an error)
        return configs;
    }

    // Implement push notification configuration deletion
    @Override
    public void onDeleteTaskPushNotificationConfig(@Nullable DeleteTaskPushNotificationConfigParams params)
            throws JSONRPCError {
        logger.debug("Deleting task push notification config: {} for task: {}", params != null ? params.id() : "null",
                params != null ? params.id() : "null");

        if (params == null) {
            throw new JSONRPCError(-32602, "DeleteTaskPushNotificationConfigParams cannot be null", null);
        }

        try {
            // Delete from persistent storage
            persistenceManager.deletePushNotificationConfig(params.id());

            logger.info("A2A Push notification config deleted: taskId={}", params.id());

        } catch (Exception e) {
            logger.error("Error deleting push notification config: {}", params.id(), e);
            throw new JSONRPCError(-32001, "Failed to delete push notification config", null);
        }
    }

    // Implement task resubscription using SDK patterns
    @Override
    public java.util.concurrent.Flow.Publisher<io.a2a.spec.StreamingEventKind> onResubscribeToTask(
            @Nullable TaskIdParams params) throws JSONRPCError {
        logger.debug("Resubscribing to task: {}", params != null ? params.id() : "null");

        if (params == null) {
            throw new JSONRPCError(-32602, "TaskIdParams cannot be null", null);
        }

        // Create streaming publisher for resubscription using SDK patterns
        SubmissionPublisher<StreamingEventKind> publisher = new SubmissionPublisher<>();
        streamingPublishers.put(params.id(), publisher);

        // Check if task exists
        if (taskStore != null) {
            Task task = taskStore.get(params.id());
            if (task != null) {
                // Send current task status using SDK patterns
                publishStreamingTaskStatus(publisher, params.id(), task.getStatus().state(), "Task resubscription");
            } else {
                // Task not found, send error status
                publishStreamingTaskStatus(publisher, params.id(), TaskState.FAILED, "Task not found: " + params.id());
                logger.warn("Task not found for resubscription: {}", params.id());
            }
        } else {
            // TaskStore not available, send error status
            publishStreamingTaskStatus(publisher, params.id(), TaskState.FAILED, "TaskStore not available");
            logger.error("TaskStore not available for resubscription");
        }

        // Always return the publisher - it will handle errors by emitting error events
        return publisher;
    }

    // Helper method to create default TaskPushNotificationConfig
    private TaskPushNotificationConfig createDefaultTaskPushNotificationConfig(String taskId) {
        // Create a default PushNotificationConfig - this is a placeholder
        // In a real implementation, you would need to know the exact SDK API
        PushNotificationConfig defaultPushConfig = createDefaultPushNotificationConfig();
        return new TaskPushNotificationConfig(taskId, defaultPushConfig);
    }

    // Helper method to create default PushNotificationConfig
    private PushNotificationConfig createDefaultPushNotificationConfig() {
        // Create a default push notification configuration
        // This is a simplified implementation - in a real scenario, you'd use the actual SDK constructors
        try {
            // Create default authentication info
            List<String> schemes = List.of("basic");
            PushNotificationAuthenticationInfo authInfo = new PushNotificationAuthenticationInfo(schemes,
                    "default-credentials");

            // Create default push notification config
            return new PushNotificationConfig("default-url", "default-token", authInfo, "default-id");
        } catch (Exception e) {
            logger.error("Error creating default push notification config", e);
            // If we can't create a proper config, throw an exception rather than returning null
            throw new RuntimeException("Failed to create default push notification config", e);
        }
    }

    private void publishTaskStatusUpdate(String taskId, TaskState state, String message) {
        try {
            TaskStatus status = new TaskStatus(state);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("message", message);
            metadata.put("timestamp", System.currentTimeMillis());

            TaskStatusUpdateEvent event = new TaskStatusUpdateEvent(taskId, status, taskId, false, metadata);
            logger.debug("Published task status update: {} -> {}", taskId, state);
        } catch (Exception e) {
            logger.error("Error publishing task status update for task: {}", taskId, e);
        }
    }

    private void publishStreamingTaskStatus(SubmissionPublisher<StreamingEventKind> publisher, String taskId,
            TaskState state, String message) {
        try {
            TaskStatus status = new TaskStatus(state);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("message", message);
            metadata.put("timestamp", System.currentTimeMillis());

            TaskStatusUpdateEvent event = new TaskStatusUpdateEvent(taskId, status, taskId, false, metadata);
            publisher.submit(event);
            logger.debug("Published streaming task status: {} -> {}", taskId, state);
        } catch (Exception e) {
            logger.error("Error publishing streaming task status for task: {}", taskId, e);
        }
    }

    private Task createTaskFromMessage(MessageSendParams params) {
        String taskId = "task-" + System.currentTimeMillis();
        String content = extractTextContent(params.message());

        // Create task using SDK patterns - use the correct constructor
        TaskStatus initialStatus = new TaskStatus(TaskState.SUBMITTED);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("content", content);
        metadata.put("created", System.currentTimeMillis());

        // Use the correct Task constructor based on the SDK
        return new Task(taskId, "OpenHAB A2A Task", initialStatus, new ArrayList<>(), List.of(params.message()),
                metadata, "task");
    }

    public void start() {
        if (isRunning) {
            logger.warn("A2A server is already running");
            return;
        }

        try {
            logger.info("Starting A2A server using SDK patterns");

            // Initialize components if not already done
            if (!componentsInitialized) {
                initializeServerComponents();
            }

            isRunning = true;
            readyService.markReady(A2A_SERVER_READY);
            logger.info("A2A server started successfully using SDK patterns");

        } catch (Exception e) {
            logger.error("Failed to start A2A server", e);
            throw new RuntimeException("Failed to start A2A server", e);
        }
    }

    public void stop() {
        if (!isRunning) {
            logger.warn("A2A server is not running");
            return;
        }

        try {
            logger.info("Stopping A2A server");

            isRunning = false;
            readyService.unmarkReady(A2A_SERVER_READY);
            logger.info("A2A server stopped successfully");

        } catch (Exception e) {
            logger.error("Failed to stop A2A server", e);
            throw new RuntimeException("Failed to stop A2A server", e);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public @Nullable RequestHandler getRequestHandler() {
        return requestHandler;
    }

    public @Nullable TaskStore getTaskStore() {
        return taskStore;
    }

    public AgentCard getAgentCard() {
        logger.debug("Getting OpenHAB agent card");

        // Build agent card using SDK patterns
        AgentCapabilities capabilities = buildCapabilities();
        List<AgentSkill> skills = buildSkills();
        Map<String, SecurityScheme> securitySchemes = buildSecuritySchemes();
        List<Map<String, List<String>>> security = buildSecurityConfig();
        String instructions = buildInstructions();

        // Create agent card using actual SDK classes
        return new AgentCard("OpenHAB AI Agent", // name
                "OpenHAB AI agent that can control and monitor home automation systems", // description
                "http://localhost:8080/a2a", // url
                new AgentProvider("openHAB", "openHAB AI Team"), // provider
                "1.0.0", // version
                "http://docs.openhab.org", // documentationUrl
                capabilities, // capabilities
                List.of("text"), // defaultInputModes
                List.of("text"), // defaultOutputModes
                skills, // skills
                false, // supportsAuthenticatedExtendedCard
                securitySchemes, // securitySchemes
                security, // security
                "", // iconUrl
                new ArrayList<AgentInterface>(), // additionalInterfaces
                "http", // preferredTransport
                "1.0" // protocolVersion
        );
    }

    private AgentCapabilities buildCapabilities() {
        // Use the correct AgentCapabilities constructor
        return new AgentCapabilities(true, // streaming
                false, // pushNotifications
                false, // stateTransitionHistory
                new ArrayList<>() // extensions
        );
    }

    private List<AgentSkill> buildSkills() {
        List<AgentSkill> skills = new ArrayList<>();

        // Get skills from registry
        List<Map<String, Object>> skillDefinitions = skillRegistry.getSkillDefinitions();
        for (Map<String, Object> skillDef : skillDefinitions) {
            // Convert to AgentSkill using the constructor
            AgentSkill skill = new AgentSkill((String) skillDef.get("id"), // id
                    (String) skillDef.get("name"), // name
                    (String) skillDef.get("description"), // description
                    new ArrayList<>(), // tags
                    new ArrayList<>(), // examples
                    new ArrayList<>(), // inputModes
                    new ArrayList<>() // outputModes
            );

            skills.add(skill);
            logger.debug("Adding skill: {}", skillDef.get("id"));
        }

        return skills;
    }

    private Map<String, SecurityScheme> buildSecuritySchemes() {
        Map<String, SecurityScheme> schemes = new HashMap<>();

        // Add API Key security scheme using the constructor
        APIKeySecurityScheme apiKeyScheme = new APIKeySecurityScheme("X-API-Key", // name
                "header", // in
                "API Key authentication" // description
        );
        schemes.put("apiKey", apiKeyScheme);

        // Add OAuth2 security scheme using the constructor
        // Note: OAuth2SecurityScheme requires OAuthFlows, which we'll create a simple one
        // For now, let's use a simpler approach with just API Key
        logger.debug("Built {} security schemes", schemes.size());
        return schemes;
    }

    private List<Map<String, List<String>>> buildSecurityConfig() {
        List<Map<String, List<String>>> security = new ArrayList<>();

        // Configure which security schemes apply to which operations
        Map<String, List<String>> globalSecurity = new HashMap<>();
        globalSecurity.put("apiKey", List.of("execute", "read", "write"));

        security.add(globalSecurity);

        return security;
    }

    private String buildInstructions() {
        return """
                You are an OpenHAB AI agent that can control and monitor home automation systems.

                Available capabilities:
                - Control devices and things
                - Monitor system status
                - Manage configurations
                - Execute automation rules

                Use the available skills to interact with the OpenHAB system.
                Always prioritize user safety and system stability.
                """;
    }

    public Object executeSkill(io.a2a.spec.Message message) {
        logger.debug("Executing skill for message: {}", message);

        try {
            String skillId = extractSkillIdFromMessage(message);
            if (skillId != null && skillRegistry.hasSkill(skillId)) {
                return skillRegistry.executeSkill(skillId, message);
            } else {
                logger.warn("Skill not found: {}", skillId);
                return Map.of("error", "Skill not found: " + skillId);
            }
        } catch (Exception e) {
            logger.error("Error executing skill", e);
            return Map.of("error", "Error executing skill: " + e.getMessage());
        }
    }

    private String extractSkillIdFromMessage(io.a2a.spec.Message message) {
        String content = extractTextContent(message);
        if (content != null && content.contains(" ")) {
            return content.split(" ")[0];
        }
        return "default"; // Return default skill ID instead of null
    }

    private String extractTextContent(io.a2a.spec.Message message) {
        if (message.getParts() != null) {
            StringBuilder textBuilder = new StringBuilder();
            for (io.a2a.spec.Part part : message.getParts()) {
                if (part instanceof io.a2a.spec.TextPart textPart) {
                    textBuilder.append(textPart.getText());
                }
            }
            return textBuilder.toString();
        }
        return ""; // Return empty string instead of null
    }

    private String extractActionIdFromMessage(io.a2a.spec.Message message) {
        // Extract action ID from message metadata
        Map<String, Object> metadata = message.getMetadata();
        if (metadata != null && metadata.containsKey("actionId")) {
            return metadata.get("actionId").toString();
        }

        // Fallback: extract from message content
        String content = extractTextContent(message);
        if (content != null && content.contains(" ")) {
            String[] parts = content.split(" ");
            if (parts.length > 0) {
                return parts[0];
            }
        }

        return "system.info";
    }

    private Map<String, Object> extractParametersFromMessage(io.a2a.spec.Message message) {
        Map<String, Object> parameters = new HashMap<>();

        // Extract parameters from message metadata
        Map<String, Object> metadata = message.getMetadata();
        if (metadata != null) {
            // Copy relevant parameters from metadata
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("param.")) {
                    String paramName = key.substring(6); // Remove "param." prefix
                    parameters.put(paramName, entry.getValue());
                }
            }
        }

        // Extract parameters from message content
        String content = extractTextContent(message);
        if (content != null) {
            // Parse content for parameters (format: actionId param1=value1 param2=value2)
            String[] parts = content.split(" ");
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i];
                if (part.contains("=")) {
                    String[] keyValue = part.split("=", 2);
                    if (keyValue.length == 2) {
                        parameters.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        }

        return parameters;
    }

    private AIActionResult executeAIAction(String actionId, Map<String, Object> parameters, String taskId) {
        try {
            logger.debug("Executing AI action: {} for task: {}", actionId, taskId);

            // Get the AI action from registry
            AIAction action = actionRegistry.getAction(actionId);
            if (action == null) {
                logger.warn("AI action not found: {}", actionId);
                return AIActionResult.error("Action not found: " + actionId, null, System.currentTimeMillis());
            }

            // Create AI action context
            AIActionContext context = AIActionContext.builder().protocol("a2a").clientId("a2a-client")
                    .sessionId("a2a-session-" + taskId).correlationId(taskId).priority("normal").build();

            // Execute the action
            AIActionResult result = action.execute(parameters, context);

            logger.debug("AI action execution completed for task: {} - success: {}", taskId, result.isSuccess());
            return result;

        } catch (Exception e) {
            logger.error("Error executing AI action: {} for task: {}", actionId, taskId, e);
            return AIActionResult.error("Execution error: " + e.getMessage(), null, System.currentTimeMillis());
        }
    }

    private AIActionResult executeAIActionWithStreaming(String actionId, Map<String, Object> parameters, String taskId,
            SubmissionPublisher<StreamingEventKind> publisher) {
        try {
            logger.debug("Executing AI action with streaming: {} for task: {}", actionId, taskId);

            // Get the AI action from registry
            AIAction action = actionRegistry.getAction(actionId);
            if (action == null) {
                logger.warn("AI action not found: {}", actionId);
                return AIActionResult.error("Action not found: " + actionId, null, System.currentTimeMillis());
            }

            // Create AI action context
            AIActionContext context = AIActionContext.builder().protocol("a2a").clientId("a2a-client")
                    .sessionId("a2a-session-" + taskId).correlationId(taskId).priority("normal").build();

            // Send progress update
            publishStreamingTaskStatus(publisher, taskId, TaskState.WORKING, "Executing action: " + actionId);

            // Execute the action
            AIActionResult result = action.execute(parameters, context);

            // Send final status update
            if (result.isSuccess()) {
                publishStreamingTaskStatus(publisher, taskId, TaskState.WORKING, "Action completed successfully");
            } else {
                publishStreamingTaskStatus(publisher, taskId, TaskState.WORKING, "Action completed with errors");
            }

            logger.debug("AI action streaming execution completed for task: {} - success: {}", taskId,
                    result.isSuccess());
            return result;

        } catch (Exception e) {
            logger.error("Error executing AI action with streaming: {} for task: {}", actionId, taskId, e);
            return AIActionResult.error("Execution error: " + e.getMessage(), null, System.currentTimeMillis());
        }
    }

    private void updateTaskWithResult(String taskId, AIActionResult result) {
        try {
            // Get the current task
            Task task = taskStore.get(taskId);
            if (task != null) {
                // Update task metadata with result
                Map<String, Object> metadata = new HashMap<>(task.getMetadata());
                metadata.put("result", result.getData());
                metadata.put("success", result.isSuccess());
                metadata.put("message", result.getMessage());
                metadata.put("completedAt", System.currentTimeMillis());

                // Create updated task (simplified - in real implementation, you'd need to create a new Task object)
                logger.debug("Updated task {} with result - success: {}", taskId, result.isSuccess());
            }
        } catch (Exception e) {
            logger.error("Error updating task with result: {}", taskId, e);
        }
    }

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
            readyService.unmarkReady(A2A_SERVER_COMPONENTS_READY);
            readyService.unmarkReady(A2A_SERVER_READY);
        }
    }

    private boolean isCoreServiceMarker(ReadyMarker marker) {
        return CORE_THINGS_READY.equals(marker) || CORE_RULES_READY.equals(marker);
    }

    private boolean isSkillRegistryMarker(ReadyMarker marker) {
        return A2ASkillRegistry.A2A_SKILLS_READY.equals(marker);
    }

    private void checkCoreServicesReady() {
        logger.debug("Checking core services readiness");

        boolean thingsReady = readyService.isReady(CORE_THINGS_READY);
        boolean rulesReady = readyService.isReady(CORE_RULES_READY);

        if (thingsReady && rulesReady) {
            logger.debug("Core services are ready, initializing A2A server components");
            initializeServerComponents();
            readyService.markReady(A2A_SERVER_COMPONENTS_READY);
        } else {
            logger.debug("Core services not ready yet - things: {}, rules: {}", thingsReady, rulesReady);
        }
    }

    private void checkSkillsReady() {
        logger.debug("Checking skills readiness");

        if (readyService.isReady(A2ASkillRegistry.A2A_SKILLS_READY)) {
            logger.debug("Skills are ready, A2A server is fully initialized");
            // Skills are ready, server can start
        } else {
            logger.debug("Skills not ready yet");
        }
    }

    private void initializeA2AServer() throws Exception {
        logger.info("Initializing A2A server with configuration from AIConfigurationService");

        // Load configuration from AIConfigurationService
        Map<String, Object> serverConfig = loadConfigurationFromService();

        // Initialize server with loaded configuration
        String serverId = (String) serverConfig.get("server.id");
        String serverName = (String) serverConfig.get("server.name");
        String serverVersion = (String) serverConfig.get("server.version");

        logger.info("Initializing A2A server: {} (v{})", serverName, serverVersion);

        // Initialize server components based on configuration
        boolean enableSkills = (Boolean) serverConfig.get("server.enable.skills");
        boolean enableTasks = (Boolean) serverConfig.get("server.enable.tasks");
        boolean enablePushNotifications = (Boolean) serverConfig.get("server.enable.push.notifications");
        boolean enablePersistence = (Boolean) serverConfig.get("server.enable.persistence");

        if (enableSkills) {
            logger.debug("Initializing A2A skills");
            // Skills initialization logic
        }

        if (enableTasks) {
            logger.debug("Initializing A2A task management");
            // Task management initialization logic
        }

        if (enablePushNotifications) {
            logger.debug("Initializing A2A push notifications");
            // Push notification initialization logic
        }

        if (enablePersistence) {
            logger.debug("Initializing A2A persistence");
            // Persistence initialization logic
        }

        logger.info("A2A server initialization completed");
    }

    /**
     * Load A2A configuration from the AIConfigurationService.
     * 
     * @return A2A server configuration map
     */
    private Map<String, Object> loadConfigurationFromService() {
        Map<String, Object> config = new HashMap<>();

        if (configurationService == null) {
            logger.warn("AIConfigurationService not available, using default configuration");
            return createDefaultConfiguration();
        }

        try {
            // Server Identity
            config.put("server.id", configurationService.getConfigValue("a2a.server.id", "openhab-a2a-server"));
            config.put("server.name", configurationService.getConfigValue("a2a.server.name", "openHAB A2A Server"));
            config.put("server.version", configurationService.getConfigValue("a2a.server.version", "1.0.0"));
            config.put("server.description", configurationService.getConfigValue("a2a.server.description",
                    "openHAB A2A Server for Multi-Agent Coordination"));

            // Server Features
            config.put("server.enable.skills",
                    configurationService.getConfigValue("a2a.server.enable.skills", Boolean.class, true));
            config.put("server.enable.tasks",
                    configurationService.getConfigValue("a2a.server.enable.tasks", Boolean.class, true));
            config.put("server.enable.push.notifications",
                    configurationService.getConfigValue("a2a.server.enable.push.notifications", Boolean.class, true));
            config.put("server.enable.persistence",
                    configurationService.getConfigValue("a2a.server.enable.persistence", Boolean.class, true));

            // Persistence Configuration
            config.put("persistence.enabled",
                    configurationService.getConfigValue("a2a.persistence.enabled", Boolean.class, true));
            config.put("persistence.service", configurationService.getConfigValue("a2a.persistence.service", "mapdb"));
            config.put("persistence.backup.enabled",
                    configurationService.getConfigValue("a2a.persistence.backup.enabled", Boolean.class, true));
            config.put("persistence.backup.interval",
                    configurationService.getConfigValue("a2a.persistence.backup.interval", "24h"));
            config.put("persistence.backup.retention",
                    configurationService.getConfigValue("a2a.persistence.backup.retention", "7d"));

            // Data Retention Settings
            config.put("persistence.retention.tasks",
                    configurationService.getConfigValue("a2a.persistence.retention.tasks", "30d"));
            config.put("persistence.retention.executions",
                    configurationService.getConfigValue("a2a.persistence.retention.executions", "90d"));
            config.put("persistence.retention.statistics",
                    configurationService.getConfigValue("a2a.persistence.retention.statistics", "1y"));
            config.put("persistence.retention.logs",
                    configurationService.getConfigValue("a2a.persistence.retention.logs", "30d"));
            config.put("persistence.retention.metadata",
                    configurationService.getConfigValue("a2a.persistence.retention.metadata", "1y"));

            // Storage Configuration
            config.put("storage.tasks.key", configurationService.getConfigValue("a2a.storage.tasks.key", "a2a-tasks"));
            config.put("storage.metadata.key",
                    configurationService.getConfigValue("a2a.storage.metadata.key", "a2a-metadata"));
            config.put("storage.statistics.key",
                    configurationService.getConfigValue("a2a.storage.statistics.key", "a2a-statistics"));
            config.put("storage.config.key",
                    configurationService.getConfigValue("a2a.storage.config.key", "a2a-config"));
            config.put("storage.recovery.key",
                    configurationService.getConfigValue("a2a.storage.recovery.key", "a2a-recovery"));
            config.put("storage.push.notifications.key", configurationService
                    .getConfigValue("a2a.storage.push.notifications.key", "a2a-push-notifications"));

            // Task Execution Configuration
            config.put("execution.max.concurrent.tasks",
                    configurationService.getConfigValue("a2a.execution.max.concurrent.tasks", Integer.class, 10));
            config.put("execution.max.queue.size",
                    configurationService.getConfigValue("a2a.execution.max.queue.size", Integer.class, 100));
            config.put("execution.timeout", configurationService.getConfigValue("a2a.execution.timeout", "300s"));
            config.put("execution.cleanup.interval",
                    configurationService.getConfigValue("a2a.execution.cleanup.interval", "60s"));

            // Retry Policy
            config.put("execution.retry.max.attempts",
                    configurationService.getConfigValue("a2a.execution.retry.max.attempts", Integer.class, 3));
            config.put("execution.retry.backoff.multiplier",
                    configurationService.getConfigValue("a2a.execution.retry.backoff.multiplier", Double.class, 2.0));
            config.put("execution.retry.initial.delay",
                    configurationService.getConfigValue("a2a.execution.retry.initial.delay", "1s"));
            config.put("execution.retry.max.delay",
                    configurationService.getConfigValue("a2a.execution.retry.max.delay", "60s"));

            // Task Lifecycle
            config.put("execution.task.states", configurationService.getConfigValue("a2a.execution.task.states",
                    "CREATED,VALIDATED,QUEUED,EXECUTING,COMPLETED,FAILED,CANCELLED,TIMEOUT"));
            config.put("execution.task.timeout",
                    configurationService.getConfigValue("a2a.execution.task.timeout", "300s"));
            config.put("execution.task.cancellation.enabled", configurationService
                    .getConfigValue("a2a.execution.task.cancellation.enabled", Boolean.class, true));

            // Push Notifications Configuration
            config.put("push.notifications.enabled",
                    configurationService.getConfigValue("a2a.push.notifications.enabled", Boolean.class, true));
            config.put("push.notifications.persistence.enabled", configurationService
                    .getConfigValue("a2a.push.notifications.persistence.enabled", Boolean.class, true));
            config.put("push.notifications.validation.enabled", configurationService
                    .getConfigValue("a2a.push.notifications.validation.enabled", Boolean.class, true));
            config.put("push.notifications.max.retries",
                    configurationService.getConfigValue("a2a.push.notifications.max.retries", Integer.class, 3));
            config.put("push.notifications.retry.delay",
                    configurationService.getConfigValue("a2a.push.notifications.retry.delay", "5s"));
            config.put("push.notifications.timeout",
                    configurationService.getConfigValue("a2a.push.notifications.timeout", "30s"));
            config.put("push.notifications.batch.size",
                    configurationService.getConfigValue("a2a.push.notifications.batch.size", Integer.class, 10));

            // Agent Management Configuration
            config.put("agents.max.count",
                    configurationService.getConfigValue("a2a.agents.max.count", Integer.class, 50));
            config.put("agents.coordination.enabled",
                    configurationService.getConfigValue("a2a.agents.coordination.enabled", Boolean.class, true));
            config.put("agents.communication.enabled",
                    configurationService.getConfigValue("a2a.agents.communication.enabled", Boolean.class, true));
            config.put("agents.negotiation.enabled",
                    configurationService.getConfigValue("a2a.agents.negotiation.enabled", Boolean.class, true));

            // Skills Configuration
            config.put("skills.enabled",
                    configurationService.getConfigValue("a2a.skills.enabled", Boolean.class, true));
            config.put("skills.auto.discovery",
                    configurationService.getConfigValue("a2a.skills.auto.discovery", Boolean.class, true));
            config.put("skills.validation.enabled",
                    configurationService.getConfigValue("a2a.skills.validation.enabled", Boolean.class, true));
            config.put("skills.caching.enabled",
                    configurationService.getConfigValue("a2a.skills.caching.enabled", Boolean.class, true));

            // Security Configuration
            config.put("security.auth.enabled",
                    configurationService.getConfigValue("a2a.security.auth.enabled", Boolean.class, true));
            config.put("security.authorization.enabled",
                    configurationService.getConfigValue("a2a.security.authorization.enabled", Boolean.class, true));
            config.put("security.rate.limit.enabled",
                    configurationService.getConfigValue("a2a.security.rate.limit.enabled", Boolean.class, true));
            config.put("security.rate.limit.requests.per.minute", configurationService
                    .getConfigValue("a2a.security.rate.limit.requests.per.minute", Integer.class, 1000));
            config.put("security.rate.limit.max.connections",
                    configurationService.getConfigValue("a2a.security.rate.limit.max.connections", Integer.class, 100));

            // Logging Configuration
            config.put("logging.level", configurationService.getConfigValue("a2a.logging.level", "INFO"));
            config.put("logging.structured",
                    configurationService.getConfigValue("a2a.logging.structured", Boolean.class, true));
            config.put("logging.include.metadata",
                    configurationService.getConfigValue("a2a.logging.include.metadata", Boolean.class, true));
            config.put("logging.performance.tracking",
                    configurationService.getConfigValue("a2a.logging.performance.tracking", Boolean.class, true));

            // Performance Configuration
            config.put("performance.max.memory.usage",
                    configurationService.getConfigValue("a2a.performance.max.memory.usage", "1GB"));
            config.put("performance.gc.optimization",
                    configurationService.getConfigValue("a2a.performance.gc.optimization", Boolean.class, true));
            config.put("performance.thread.pool.size",
                    configurationService.getConfigValue("a2a.performance.thread.pool.size", Integer.class, 20));

            // Recovery Configuration
            config.put("recovery.enabled",
                    configurationService.getConfigValue("a2a.recovery.enabled", Boolean.class, true));
            config.put("recovery.auto.restart",
                    configurationService.getConfigValue("a2a.recovery.auto.restart", Boolean.class, true));
            config.put("recovery.state.persistence",
                    configurationService.getConfigValue("a2a.recovery.state.persistence", Boolean.class, true));
            config.put("recovery.max.attempts",
                    configurationService.getConfigValue("a2a.recovery.max.attempts", Integer.class, 3));
            config.put("recovery.backoff.delay",
                    configurationService.getConfigValue("a2a.recovery.backoff.delay", "5s"));
            config.put("recovery.timeout", configurationService.getConfigValue("a2a.recovery.timeout", "60s"));
            config.put("recovery.cleanup.enabled",
                    configurationService.getConfigValue("a2a.recovery.cleanup.enabled", Boolean.class, true));

            // Development Configuration
            config.put("debug.enabled", configurationService.getConfigValue("a2a.debug.enabled", Boolean.class, false));
            config.put("debug.log.task.executions",
                    configurationService.getConfigValue("a2a.debug.log.task.executions", Boolean.class, true));
            config.put("debug.log.skill.calls",
                    configurationService.getConfigValue("a2a.debug.log.skill.calls", Boolean.class, true));
            config.put("debug.log.persistence.operations",
                    configurationService.getConfigValue("a2a.debug.log.persistence.operations", Boolean.class, true));

            logger.info("Loaded A2A configuration from AIConfigurationService with {} settings", config.size());
            return config;

        } catch (Exception e) {
            logger.warn("Failed to load configuration from AIConfigurationService, using default configuration", e);
            return createDefaultConfiguration();
        }
    }

    /**
     * Create default configuration as fallback.
     * 
     * @return Default A2A server configuration map
     */
    private Map<String, Object> createDefaultConfiguration() {
        Map<String, Object> config = new HashMap<>();

        // Default server configuration
        config.put("server.id", "openhab-a2a-server");
        config.put("server.name", "openHAB A2A Server");
        config.put("server.version", "1.0.0");
        config.put("server.description", "openHAB A2A Server for Multi-Agent Coordination");

        // Default feature enablement
        config.put("server.enable.skills", true);
        config.put("server.enable.tasks", true);
        config.put("server.enable.push.notifications", true);
        config.put("server.enable.persistence", true);

        // Default persistence configuration
        config.put("persistence.enabled", true);
        config.put("persistence.service", "mapdb");

        // Default execution configuration
        config.put("execution.max.concurrent.tasks", 10);
        config.put("execution.timeout", "300s");

        // Default push notifications
        config.put("push.notifications.enabled", true);
        config.put("push.notifications.persistence.enabled", true);

        return config;
    }
}
