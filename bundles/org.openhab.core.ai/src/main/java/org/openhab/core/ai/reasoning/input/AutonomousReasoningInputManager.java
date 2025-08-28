package org.openhab.core.ai.reasoning.input;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.service.snapshot.GenericMetricsSnapshot;
import org.openhab.core.ai.common.validation.InputValidationResult;
import org.openhab.core.ai.events.EventSystemIntegration;
import org.openhab.core.ai.events.LogEntry;
import org.openhab.core.ai.events.LogIngestionPipeline;
import org.openhab.core.ai.events.LogLevel;
import org.openhab.core.ai.reasoning.events.AutonomousEventProcessor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Autonomous Reasoning Input Manager
 * 
 * <p>
 * This component provides unified input management for autonomous reasoning agents:
 * - Unified input aggregation from events and logs
 * - Input prioritization and scheduling for autonomous reasoning
 * - Input validation and quality assessment
 * - Input context enrichment and correlation
 * - Input buffering and batching for reasoning efficiency
 * - Input routing to appropriate autonomous agents
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = AutonomousReasoningInputManager.class)
@NonNullByDefault
public class AutonomousReasoningInputManager {

    private static final Logger logger = LoggerFactory.getLogger(AutonomousReasoningInputManager.class);

    // Configuration
    private static final int DEFAULT_BUFFER_SIZE = 1000;
    private static final Duration DEFAULT_BATCH_TIMEOUT = Duration.ofSeconds(5);
    private static final int DEFAULT_MAX_BATCH_SIZE = 50;
    private static final double DEFAULT_QUALITY_THRESHOLD = 0.7;

    // Input processing
    private final PriorityBlockingQueue<ReasoningInput> inputQueue = new PriorityBlockingQueue<>(DEFAULT_BUFFER_SIZE);
    private final Map<String, ReasoningInput> activeInputs = new ConcurrentHashMap<>();
    private final Map<String, InputBatch> inputBatches = new ConcurrentHashMap<>();
    private final Map<String, AgentInputRouter> agentRouters = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalInputsProcessed = new AtomicLong(0);
    private final AtomicLong totalBatchesCreated = new AtomicLong(0);
    private final AtomicLong totalInputsRouted = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);

    // Threading
    private final ExecutorService processingExecutor = Executors.newFixedThreadPool(4);
    private final ExecutorService routingExecutor = Executors.newFixedThreadPool(2);
    private volatile boolean isRunning = false;

    // Dependencies
    @Reference
    private @Nullable EventSystemIntegration eventSystemIntegration;

    @Reference
    private @Nullable LogIngestionPipeline logIngestionPipeline;

    @Reference
    private @Nullable AutonomousEventProcessor autonomousEventProcessor;

    // Configuration
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private Duration batchTimeout = DEFAULT_BATCH_TIMEOUT;
    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;
    private double qualityThreshold = DEFAULT_QUALITY_THRESHOLD;
    private boolean enableInputValidation = true;
    private boolean enableContextEnrichment = true;
    private boolean enablePerformanceMonitoring = true;

    @Activate
    public void activate() {
        logger.debug("Autonomous Reasoning Input Manager activated");
        startManager();
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Autonomous Reasoning Input Manager deactivated");
        stopManager();
    }

    /**
     * Start the input manager
     */
    public void startManager() {
        if (isRunning) {
            logger.warn("Autonomous Reasoning Input Manager is already running");
            return;
        }

        isRunning = true;
        logger.info("Starting Autonomous Reasoning Input Manager");

        try {
            // Initialize agent routers
            initializeAgentRouters();

            // Start input processing
            startInputProcessing();

            logger.info("Autonomous Reasoning Input Manager started successfully");
        } catch (Exception e) {
            logger.error("Failed to start Autonomous Reasoning Input Manager", e);
            isRunning = false;
        }
    }

    /**
     * Stop the input manager
     */
    public void stopManager() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
        logger.info("Stopping Autonomous Reasoning Input Manager");

        // Shutdown executors
        processingExecutor.shutdown();
        routingExecutor.shutdown();

        // Clear queues and maps
        inputQueue.clear();
        activeInputs.clear();
        inputBatches.clear();
        agentRouters.clear();

        logger.info("Autonomous Reasoning Input Manager stopped");
    }

    /**
     * Submit input for autonomous reasoning
     */
    public CompletableFuture<InputSubmissionResult> submitInput(ReasoningInput input) {
        return CompletableFuture.supplyAsync(() -> {
            Instant startTime = Instant.now();

            try {
                // Validate input
                if (enableInputValidation) {
                    InputValidationResult validation = validateInput(input);
                    if (!validation.isValid()) {
                        return InputSubmissionResult.validationFailed(validation.getReason());
                    }
                }

                // Enrich input context
                if (enableContextEnrichment) {
                    enrichInputContext(input);
                }

                // Assess input quality
                double quality = assessInputQuality(input);
                input.setQuality(quality);

                // Add to processing queue
                if (inputQueue.offer(input)) {
                    activeInputs.put(input.getId(), input);
                    totalInputsProcessed.incrementAndGet();

                    logger.debug("Submitted input for autonomous reasoning: {}", input.getId());

                    return InputSubmissionResult.success(input.getId(), quality);
                } else {
                    return InputSubmissionResult.queueFull("Input queue is full");
                }

            } catch (Exception e) {
                logger.error("Error submitting input for autonomous reasoning", e);
                return InputSubmissionResult.error("Error processing input: " + e.getMessage());
            } finally {
                totalProcessingTime.addAndGet(Duration.between(startTime, Instant.now()).toMillis());
            }
        }, processingExecutor);
    }

    /**
     * Submit batch of inputs for autonomous reasoning
     */
    public CompletableFuture<BatchSubmissionResult> submitInputBatch(List<ReasoningInput> inputs) {
        return CompletableFuture.supplyAsync(() -> {
            Instant startTime = Instant.now();
            List<String> processedIds = new ArrayList<>();
            List<String> failedIds = new ArrayList<>();

            try {
                for (ReasoningInput input : inputs) {
                    InputSubmissionResult result = submitInput(input).get();
                    if (result.isSuccess()) {
                        processedIds.add(input.getId());
                    } else {
                        failedIds.add(input.getId());
                    }
                }

                totalBatchesCreated.incrementAndGet();

                return BatchSubmissionResult.success(processedIds, failedIds, inputs.size());

            } catch (Exception e) {
                logger.error("Error submitting input batch", e);
                return BatchSubmissionResult.error("Error processing batch: " + e.getMessage());
            } finally {
                totalProcessingTime.addAndGet(Duration.between(startTime, Instant.now()).toMillis());
            }
        }, processingExecutor);
    }

    /**
     * Get next input for processing
     */
    public @Nullable ReasoningInput getNextInput() {
        try {
            return inputQueue.poll();
        } catch (Exception e) {
            logger.error("Error getting next input from queue", e);
            return null;
        }
    }

    /**
     * Get input batch for processing
     */
    public CompletableFuture<InputBatch> getInputBatch() {
        return CompletableFuture.supplyAsync(() -> {
            List<ReasoningInput> batchInputs = new ArrayList<>();
            Instant batchStartTime = Instant.now();

            try {
                // Collect inputs until batch is full or timeout reached
                while (batchInputs.size() < maxBatchSize
                        && Duration.between(batchStartTime, Instant.now()).compareTo(batchTimeout) < 0) {

                    ReasoningInput input = inputQueue.poll();
                    if (input != null) {
                        batchInputs.add(input);
                    } else {
                        // No more inputs available, break
                        break;
                    }
                }

                if (!batchInputs.isEmpty()) {
                    InputBatch batch = new InputBatch(generateBatchId(), batchInputs, batchStartTime, Instant.now());

                    inputBatches.put(batch.getId(), batch);
                    return batch;
                }

                return null;

            } catch (Exception e) {
                logger.error("Error creating input batch", e);
                return null;
            }
        }, processingExecutor);
    }

    /**
     * Route input to appropriate autonomous agent
     */
    public CompletableFuture<InputRoutingResult> routeInput(ReasoningInput input) {
        return CompletableFuture.supplyAsync(() -> {
            Instant startTime = Instant.now();

            try {
                // Determine target agent
                String targetAgent = determineTargetAgent(input);
                if (targetAgent == null) {
                    return InputRoutingResult.noTarget("No suitable agent found for input");
                }

                // Get agent router
                AgentInputRouter router = agentRouters.get(targetAgent);
                if (router == null) {
                    return InputRoutingResult.routerNotFound("Router not found for agent: " + targetAgent);
                }

                // Route input
                boolean routed = router.routeInput(input);
                if (routed) {
                    totalInputsRouted.incrementAndGet();
                    return InputRoutingResult.success(targetAgent, input.getId());
                } else {
                    return InputRoutingResult.routingFailed("Failed to route input to agent: " + targetAgent);
                }

            } catch (Exception e) {
                logger.error("Error routing input", e);
                return InputRoutingResult.error("Error routing input: " + e.getMessage());
            } finally {
                totalProcessingTime.addAndGet(Duration.between(startTime, Instant.now()).toMillis());
            }
        }, routingExecutor);
    }

    /**
     * Validate input quality and structure
     */
    private InputValidationResult validateInput(ReasoningInput input) {
        List<String> errors = new ArrayList<>();

        // Check required fields
        if (input.getId() == null || input.getId().trim().isEmpty()) {
            errors.add("Input ID is required");
        }

        if (input.getType() == null) {
            errors.add("Input type is required");
        }

        if (input.getContent() == null || input.getContent().trim().isEmpty()) {
            errors.add("Input content is required");
        }

        if (input.getTimestamp() == null) {
            errors.add("Input timestamp is required");
        }

        // Check input size limits
        if (input.getContent().length() > 10000) {
            errors.add("Input content exceeds maximum size limit");
        }

        // Check input type validity
        if (!isValidInputType(input.getType())) {
            errors.add("Invalid input type: " + input.getType());
        }

        if (errors.isEmpty()) {
            return InputValidationResult.valid();
        } else {
            return InputValidationResult.invalid(String.join("; ", errors));
        }
    }

    /**
     * Enrich input with additional context
     */
    private void enrichInputContext(ReasoningInput input) {
        Map<String, Object> enrichedContext = new HashMap<>(input.getContext());

        // Add system context
        enrichedContext.put("systemTimestamp", Instant.now());
        enrichedContext.put("inputManagerVersion", "1.0.0");

        // Add correlation context if available
        if (logIngestionPipeline != null) {
            List<LogEntry> recentLogs = logIngestionPipeline.getRecentLogs(10);
            if (!recentLogs.isEmpty()) {
                enrichedContext.put("recentLogs", recentLogs.size());
                enrichedContext.put("hasRecentErrors",
                        recentLogs.stream().anyMatch(log -> log.getLevel() == LogLevel.ERROR));
            }
        }

        // Add event context if available
        if (eventSystemIntegration != null) {
            enrichedContext.put("eventSystemAvailable", true);
        }

        input.setContext(enrichedContext);
    }

    /**
     * Assess input quality based on various factors
     */
    private double assessInputQuality(ReasoningInput input) {
        double quality = 1.0;

        // Content quality assessment
        if (input.getContent().length() < 10) {
            quality -= 0.3; // Too short
        }

        if (input.getContent().length() > 1000) {
            quality -= 0.1; // Very long
        }

        // Context quality assessment
        if (input.getContext().isEmpty()) {
            quality -= 0.2; // No context
        }

        // Timestamp quality assessment
        if (input.getTimestamp().isBefore(Instant.now().minus(Duration.ofHours(1)))) {
            quality -= 0.4; // Stale data
        }

        // Type-specific quality assessment
        switch (input.getType()) {
            case EVENT:
                quality += 0.1; // Events are typically high quality
                break;
            case LOG:
                quality += 0.05; // Logs are structured
                break;
            case USER_INPUT:
                quality -= 0.1; // User input may be noisy
                break;
            case SYSTEM_METRIC:
                quality += 0.2; // System metrics are reliable
                break;
        }

        return Math.max(0.0, Math.min(1.0, quality));
    }

    /**
     * Determine target agent for input
     */
    private @Nullable String determineTargetAgent(ReasoningInput input) {
        // Simple agent selection logic based on input type and content
        switch (input.getType()) {
            case EVENT:
                return determineEventAgent(input);
            case LOG:
                return determineLogAgent(input);
            case USER_INPUT:
                return determineUserInputAgent(input);
            case SYSTEM_METRIC:
                return determineSystemMetricAgent(input);
            default:
                return "general_agent";
        }
    }

    /**
     * Determine agent for event inputs
     */
    private String determineEventAgent(ReasoningInput input) {
        String content = input.getContent().toLowerCase();

        if (content.contains("security") || content.contains("threat") || content.contains("alert")) {
            return "security_agent";
        } else if (content.contains("energy") || content.contains("power") || content.contains("consumption")) {
            return "energy_agent";
        } else if (content.contains("comfort") || content.contains("temperature") || content.contains("humidity")) {
            return "comfort_agent";
        } else {
            return "general_agent";
        }
    }

    /**
     * Determine agent for log inputs
     */
    private String determineLogAgent(ReasoningInput input) {
        String content = input.getContent().toLowerCase();

        if (content.contains("error") || content.contains("exception") || content.contains("failed")) {
            return "system_health_agent";
        } else if (content.contains("performance") || content.contains("slow") || content.contains("timeout")) {
            return "performance_agent";
        } else {
            return "general_agent";
        }
    }

    /**
     * Determine agent for user input
     */
    private String determineUserInputAgent(ReasoningInput input) {
        String content = input.getContent().toLowerCase();

        if (content.contains("schedule") || content.contains("automation") || content.contains("rule")) {
            return "automation_agent";
        } else if (content.contains("device") || content.contains("thing") || content.contains("binding")) {
            return "device_management_agent";
        } else {
            return "general_agent";
        }
    }

    /**
     * Determine agent for system metrics
     */
    private String determineSystemMetricAgent(ReasoningInput input) {
        String content = input.getContent().toLowerCase();

        if (content.contains("memory") || content.contains("cpu") || content.contains("disk")) {
            return "system_health_agent";
        } else if (content.contains("network") || content.contains("connection")) {
            return "network_agent";
        } else {
            return "general_agent";
        }
    }

    /**
     * Check if input type is valid
     */
    private boolean isValidInputType(ReasoningInputType type) {
        return type != null;
    }

    /**
     * Initialize agent routers
     */
    private void initializeAgentRouters() {
        // Initialize routers for different agent types
        agentRouters.put("general_agent", new AgentInputRouter("general_agent"));
        agentRouters.put("security_agent", new AgentInputRouter("security_agent"));
        agentRouters.put("energy_agent", new AgentInputRouter("energy_agent"));
        agentRouters.put("comfort_agent", new AgentInputRouter("comfort_agent"));
        agentRouters.put("system_health_agent", new AgentInputRouter("system_health_agent"));
        agentRouters.put("performance_agent", new AgentInputRouter("performance_agent"));
        agentRouters.put("automation_agent", new AgentInputRouter("automation_agent"));
        agentRouters.put("device_management_agent", new AgentInputRouter("device_management_agent"));
        agentRouters.put("network_agent", new AgentInputRouter("network_agent"));

        logger.debug("Initialized {} agent routers", agentRouters.size());
    }

    /**
     * Start input processing
     */
    private void startInputProcessing() {
        // Start background processing tasks
        processingExecutor.submit(this::processInputQueue);
        routingExecutor.submit(this::routeInputs);
    }

    /**
     * Process input queue
     */
    private void processInputQueue() {
        while (isRunning) {
            try {
                ReasoningInput input = inputQueue.take();
                if (input != null) {
                    // Process the input
                    processInput(input);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error processing input from queue", e);
            }
        }
    }

    /**
     * Route inputs to agents
     */
    private void routeInputs() {
        while (isRunning) {
            try {
                // Get batch of inputs to route
                InputBatch batch = getInputBatch().get();
                if (batch != null) {
                    for (ReasoningInput input : batch.getInputs()) {
                        routeInput(input);
                    }
                }
            } catch (Exception e) {
                logger.error("Error routing inputs", e);
            }
        }
    }

    /**
     * Process individual input
     */
    private void processInput(ReasoningInput input) {
        try {
            // Update input status
            input.setStatus(ReasoningInputStatus.PROCESSING);

            // Route to appropriate agent
            routeInput(input).thenAccept(result -> {
                if (result.isSuccess()) {
                    input.setStatus(ReasoningInputStatus.ROUTED);
                    logger.debug("Input {} routed to agent {}", input.getId(), result.getTargetAgent());
                } else {
                    input.setStatus(ReasoningInputStatus.FAILED);
                    logger.warn("Failed to route input {}: {}", input.getId(), result.getError());
                }
            });

        } catch (Exception e) {
            input.setStatus(ReasoningInputStatus.FAILED);
            logger.error("Error processing input {}", input.getId(), e);
        }
    }

    /**
     * Get performance metrics
     */
    public GenericMetricsSnapshot getPerformanceMetrics() {
        return GenericMetricsSnapshot.builder("reasoning-input", "performance")
                .withCounts(totalInputsProcessed.get(), totalInputsProcessed.get() - totalInputsRouted.get())
                .withLatency(totalProcessingTime.get() * 1_000_000L) // Convert to nanoseconds
                .withMetric("totalBatchesCreated", totalBatchesCreated.get())
                .withMetric("totalInputsRouted", totalInputsRouted.get()).withMetric("queueSize", inputQueue.size())
                .withMetric("activeInputsCount", activeInputs.size()).withMetric("batchCount", inputBatches.size())
                .withMetric("routerCount", agentRouters.size()).build();
    }

    /**
     * Generate unique batch ID
     */
    private String generateBatchId() {
        return "batch_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    // Configuration methods
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setBatchTimeout(Duration batchTimeout) {
        this.batchTimeout = batchTimeout;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public void setQualityThreshold(double qualityThreshold) {
        this.qualityThreshold = qualityThreshold;
    }

    public void setEnableInputValidation(boolean enableInputValidation) {
        this.enableInputValidation = enableInputValidation;
    }

    public void setEnableContextEnrichment(boolean enableContextEnrichment) {
        this.enableContextEnrichment = enableContextEnrichment;
    }

    public void setEnablePerformanceMonitoring(boolean enablePerformanceMonitoring) {
        this.enablePerformanceMonitoring = enablePerformanceMonitoring;
    }

    // (Inner data classes extracted to top-level in org.openhab.core.ai.reasoning:)
    // ReasoningInput, InputBatch, InputSubmissionResult, BatchSubmissionResult,
    // InputRoutingResult, InputValidationResult, InputPerformanceMetrics,
    // AgentInputRouter
}
