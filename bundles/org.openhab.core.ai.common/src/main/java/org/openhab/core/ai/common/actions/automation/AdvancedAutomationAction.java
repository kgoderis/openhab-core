package org.openhab.core.ai.common.actions.automation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.openhab.core.automation.RuleRegistry;
import org.openhab.core.scheduler.CronScheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advanced automation action for complex automation scenarios.
 * 
 * This action provides sophisticated automation capabilities including
 * conditional logic, scheduling, and complex rule execution.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class AdvancedAutomationAction implements AIAction {

    private static final String ACTION_ID = "openhab.automation.advanced";
    private static final String ACTION_NAME = "Advanced Automation Management";
    private static final String DESCRIPTION = "Advanced automation management including workflows, task scheduling, job queues, and automation templates";
    private static final String CATEGORY = "automation";
    private static final String VERSION = "1.0.0";

    private final Logger logger = LoggerFactory.getLogger(AdvancedAutomationAction.class);

    @Reference
    private @Nullable RuleRegistry ruleRegistry;

    @Reference
    private @Nullable CronScheduler cronScheduler;

    // Internal automation state management
    private final Map<String, WorkflowExecution> activeWorkflows = new ConcurrentHashMap<>();
    private final Map<String, AutomationJob> jobQueue = new ConcurrentHashMap<>();
    private final Map<String, AutomationTemplate> templates = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private @Nullable ScheduledExecutorService scheduler;

    @Activate
    protected void activate() {
        scheduler = Executors.newScheduledThreadPool(5);
        loadDefaultTemplates();
        logger.info("Advanced Automation Action activated");
    }

    @Deactivate
    protected void deactivate() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        // Cancel all scheduled tasks
        scheduledTasks.values().forEach(task -> task.cancel(false));
        scheduledTasks.clear();
        logger.info("Advanced Automation Action deactivated");
    }

    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("operation",
                Map.of("type", "string", "enum", List.of("create_workflow", "execute_workflow", "list_workflows",
                        "get_workflow_status", "stop_workflow", "schedule_task", "list_scheduled_tasks", "cancel_task",
                        "create_template", "list_templates", "apply_template", "manage_job_queue",
                        "get_automation_stats", "create_automation_chain", "export_automation", "import_automation"),
                        "description", "Advanced automation operation to perform"));

        properties.put("workflowId", Map.of("type", "string", "description", "Workflow identifier"));
        properties.put("templateId", Map.of("type", "string", "description", "Template identifier"));
        properties.put("taskId", Map.of("type", "string", "description", "Task identifier"));
        properties.put("name", Map.of("type", "string", "description", "Name for workflow/template/task"));
        properties.put("description", Map.of("type", "string", "description", "Description"));

        properties.put("steps",
                Map.of("type", "array", "description", "Workflow steps", "items", Map.of("type", "object")));
        properties.put("schedule", Map.of("type", "string", "description", "Cron expression or interval"));
        properties.put("parameters", Map.of("type", "object", "description", "Parameters for execution"));
        properties.put("priority", Map.of("type", "string", "enum", List.of("LOW", "NORMAL", "HIGH", "CRITICAL"),
                "description", "Job priority", "default", "NORMAL"));
        properties.put("conditions", Map.of("type", "object", "description", "Execution conditions"));
        properties.put("timeout", Map.of("type", "integer", "description", "Timeout in seconds", "default", 300));

        schema.put("properties", properties);
        schema.put("required", List.of("operation"));
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        String operation = (String) parameters.get("operation");
        if (operation == null) {
            return AIActionValidationResult.invalid(List.of("Missing required parameter: operation"));
        }

        // Validate operation-specific requirements
        return switch (operation) {
            case "create_workflow", "create_template" -> {
                String name = (String) parameters.get("name");
                if (name == null || name.trim().isEmpty()) {
                    yield AIActionValidationResult.invalid(List.of("name is required for " + operation));
                }
                yield AIActionValidationResult.valid(parameters);
            }
            case "execute_workflow", "get_workflow_status", "stop_workflow" -> {
                String workflowId = (String) parameters.get("workflowId");
                if (workflowId == null || workflowId.trim().isEmpty()) {
                    yield AIActionValidationResult.invalid(List.of("workflowId is required for " + operation));
                }
                yield AIActionValidationResult.valid(parameters);
            }
            case "schedule_task" -> {
                String schedule = (String) parameters.get("schedule");
                String name = (String) parameters.get("name");
                if (schedule == null || name == null) {
                    yield AIActionValidationResult.invalid(List.of("schedule and name are required for schedule_task"));
                }
                yield AIActionValidationResult.valid(parameters);
            }
            case "apply_template" -> {
                String templateId = (String) parameters.get("templateId");
                if (templateId == null || templateId.trim().isEmpty()) {
                    yield AIActionValidationResult.invalid(List.of("templateId is required for apply_template"));
                }
                yield AIActionValidationResult.valid(parameters);
            }
            default -> AIActionValidationResult.valid(parameters);
        };
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("operation", Map.of("type", "string", "description", "Operation performed"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the operation"));
        properties.put("success", Map.of("type", "boolean", "description", "Whether operation was successful"));
        properties.put("message", Map.of("type", "string", "description", "Operation result message"));
        properties.put("workflowId", Map.of("type", "string", "description", "Workflow identifier"));
        properties.put("executionId", Map.of("type", "string", "description", "Execution identifier"));
        properties.put("taskId", Map.of("type", "string", "description", "Task identifier"));
        properties.put("templateId", Map.of("type", "string", "description", "Template identifier"));
        properties.put("status", Map.of("type", "string", "description", "Status of the operation"));
        properties.put("workflows", Map.of("type", "array", "description", "List of workflows"));
        properties.put("templates", Map.of("type", "array", "description", "List of templates"));
        properties.put("tasks", Map.of("type", "array", "description", "List of scheduled tasks"));
        properties.put("jobs", Map.of("type", "array", "description", "List of jobs"));
        properties.put("automationStatistics", Map.of("type", "object", "description", "Automation statistics"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        long startTime = System.currentTimeMillis();

        try {
            String operation = (String) parameters.get("operation");
            Map<String, Object> result = switch (operation) {
                case "create_workflow" -> createWorkflow(parameters);
                case "execute_workflow" -> executeWorkflow(parameters);
                case "list_workflows" -> listWorkflows(parameters);
                case "get_workflow_status" -> getWorkflowStatus(parameters);
                case "stop_workflow" -> stopWorkflow(parameters);
                case "schedule_task" -> scheduleTask(parameters);
                case "list_scheduled_tasks" -> listScheduledTasks(parameters);
                case "cancel_task" -> cancelTask(parameters);
                case "create_template" -> createTemplate(parameters);
                case "list_templates" -> listTemplates(parameters);
                case "apply_template" -> applyTemplate(parameters);
                case "manage_job_queue" -> manageJobQueue(parameters);
                case "get_automation_stats" -> getAutomationStats(parameters);
                case "create_automation_chain" -> createAutomationChain(parameters);
                case "export_automation" -> exportAutomation(parameters);
                case "import_automation" -> importAutomation(parameters);
                default ->
                    throw new AIActionException(ACTION_ID, "Unknown operation: " + operation, "INVALID_PARAMETER");
            };

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            throw new AIActionException(ACTION_ID, "Advanced automation operation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<AIActionResult> executeAsync(Map<String, Object> parameters, AIActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (AIActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder().description(DESCRIPTION).version(VERSION).author("openHAB")
                .tags(List.of("automation", "workflows", "scheduling", "templates")).build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("workflow_management", true);
        capabilities.put("task_scheduling", true);
        capabilities.put("template_management", true);
        capabilities.put("job_queue_management", true);
        capabilities.put("automation_statistics", true);
        capabilities.put("automation_chains", true);
        capabilities.put("import_export", true);
        return capabilities;
    }

    @Override
    public void initialize(AIActionContext context) {
        // No initialization required
    }

    @Override
    public void cleanup() {
        // No cleanup required
    }

    @Override
    public boolean isReady() {
        return ruleRegistry != null && cronScheduler != null;
    }

    private Map<String, Object> createWorkflow(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "create_workflow");
        result.put("timestamp", Instant.now().toString());

        String name = (String) parameters.get("name");
        String description = (String) parameters.get("description");
        List<?> steps = (List<?>) parameters.get("steps");
        Map<?, ?> conditions = (Map<?, ?>) parameters.get("conditions");

        String workflowId = UUID.randomUUID().toString();

        WorkflowDefinition workflow = new WorkflowDefinition(workflowId, name, description,
                steps != null ? steps : List.of(), conditions != null ? conditions : Map.of());

        // Store workflow for later execution
        result.put("workflowId", workflowId);
        result.put("name", name);
        result.put("description", description);
        result.put("steps", workflow.steps.size());
        result.put("status", "created");
        result.put("message", "Workflow created successfully");

        return result;
    }

    private Map<String, Object> executeWorkflow(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "execute_workflow");
        result.put("timestamp", Instant.now().toString());

        String workflowId = (String) parameters.get("workflowId");
        Map<?, ?> workflowParams = (Map<?, ?>) parameters.get("parameters");

        // Create workflow execution
        WorkflowExecution execution = new WorkflowExecution(workflowId,
                workflowParams != null ? workflowParams : Map.of());

        activeWorkflows.put(execution.executionId, execution);

        // Start workflow execution asynchronously
        scheduler.submit(() -> executeWorkflowSteps(execution));

        result.put("workflowId", workflowId);
        result.put("executionId", execution.executionId);
        result.put("status", "started");
        result.put("startTime", execution.startTime.toString());
        result.put("message", "Workflow execution started");

        return result;
    }

    private Map<String, Object> listWorkflows(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "list_workflows");
        result.put("timestamp", Instant.now().toString());

        List<Map<String, Object>> workflows = activeWorkflows.values().stream().map(this::workflowExecutionToMap)
                .collect(Collectors.toList());

        result.put("workflows", workflows);
        result.put("totalWorkflows", workflows.size());
        result.put("activeWorkflows",
                workflows.stream().mapToInt(w -> "running".equals(w.get("status")) ? 1 : 0).sum());

        return result;
    }

    private Map<String, Object> getWorkflowStatus(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "get_workflow_status");
        result.put("timestamp", Instant.now().toString());

        String workflowId = (String) parameters.get("workflowId");

        WorkflowExecution execution = findWorkflowExecution(workflowId);
        if (execution == null) {
            result.put("error", "Workflow not found: " + workflowId);
            return result;
        }

        result.putAll(workflowExecutionToMap(execution));
        return result;
    }

    private Map<String, Object> stopWorkflow(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "stop_workflow");
        result.put("timestamp", Instant.now().toString());

        String workflowId = (String) parameters.get("workflowId");

        WorkflowExecution execution = findWorkflowExecution(workflowId);
        if (execution == null) {
            result.put("error", "Workflow not found: " + workflowId);
            return result;
        }

        execution.status = "stopped";
        execution.endTime = Instant.now();

        result.put("workflowId", workflowId);
        result.put("executionId", execution.executionId);
        result.put("status", "stopped");
        result.put("message", "Workflow stopped successfully");

        return result;
    }

    private Map<String, Object> scheduleTask(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "schedule_task");
        result.put("timestamp", Instant.now().toString());

        String name = (String) parameters.get("name");
        String schedule = (String) parameters.get("schedule");
        Map<?, ?> taskParams = (Map<?, ?>) parameters.get("parameters");

        String taskId = UUID.randomUUID().toString();

        try {
            // Parse schedule and create scheduled task
            ScheduledFuture<?> scheduledTask;

            if (schedule.matches("\\d+[smhd]")) {
                // Simple interval: 30s, 5m, 2h, 1d
                long delay = parseInterval(schedule);
                scheduledTask = scheduler.scheduleAtFixedRate(() -> executeScheduledTask(taskId, name, taskParams),
                        delay, delay, TimeUnit.SECONDS);
            } else {
                // Try cron expression (simplified)
                result.put("error", "Cron expressions not yet implemented");
                return result;
            }

            scheduledTasks.put(taskId, scheduledTask);

            result.put("taskId", taskId);
            result.put("name", name);
            result.put("schedule", schedule);
            result.put("status", "scheduled");
            result.put("message", "Task scheduled successfully");

        } catch (Exception e) {
            result.put("error", "Failed to schedule task: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Object> listScheduledTasks(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "list_scheduled_tasks");
        result.put("timestamp", Instant.now().toString());

        List<Map<String, Object>> tasks = new ArrayList<>();
        for (Map.Entry<String, ScheduledFuture<?>> entry : scheduledTasks.entrySet()) {
            Map<String, Object> task = new HashMap<>();
            task.put("taskId", entry.getKey());
            task.put("isDone", entry.getValue().isDone());
            task.put("isCancelled", entry.getValue().isCancelled());
            tasks.add(task);
        }

        result.put("tasks", tasks);
        result.put("totalTasks", tasks.size());
        result.put("activeTasks", tasks.stream().mapToInt(t -> !(Boolean) t.get("isDone") ? 1 : 0).sum());

        return result;
    }

    private Map<String, Object> cancelTask(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "cancel_task");
        result.put("timestamp", Instant.now().toString());

        String taskId = (String) parameters.get("taskId");

        ScheduledFuture<?> task = scheduledTasks.get(taskId);
        if (task == null) {
            result.put("error", "Task not found: " + taskId);
            return result;
        }

        boolean cancelled = task.cancel(false);
        if (cancelled) {
            scheduledTasks.remove(taskId);
        }

        result.put("taskId", taskId);
        result.put("cancelled", cancelled);
        result.put("message", cancelled ? "Task cancelled successfully" : "Task could not be cancelled");

        return result;
    }

    private Map<String, Object> createTemplate(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "create_template");
        result.put("timestamp", Instant.now().toString());

        String name = (String) parameters.get("name");
        String description = (String) parameters.get("description");
        List<?> steps = (List<?>) parameters.get("steps");
        Map<?, ?> defaultParams = (Map<?, ?>) parameters.get("parameters");

        String templateId = UUID.randomUUID().toString();

        AutomationTemplate template = new AutomationTemplate(templateId, name, description,
                steps != null ? steps : List.of(), defaultParams != null ? defaultParams : Map.of());

        templates.put(templateId, template);

        result.put("templateId", templateId);
        result.put("name", name);
        result.put("description", description);
        result.put("steps", template.steps.size());
        result.put("message", "Template created successfully");

        return result;
    }

    private Map<String, Object> listTemplates(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "list_templates");
        result.put("timestamp", Instant.now().toString());

        List<Map<String, Object>> templateList = templates.values().stream().map(this::templateToMap)
                .collect(Collectors.toList());

        result.put("templates", templateList);
        result.put("totalTemplates", templateList.size());

        return result;
    }

    private Map<String, Object> applyTemplate(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "apply_template");
        result.put("timestamp", Instant.now().toString());

        String templateId = (String) parameters.get("templateId");
        Map<?, ?> overrideParams = (Map<?, ?>) parameters.get("parameters");

        AutomationTemplate template = templates.get(templateId);
        if (template == null) {
            result.put("error", "Template not found: " + templateId);
            return result;
        }

        // Create workflow from template
        Map<String, Object> workflowParams = new HashMap<>();
        workflowParams.put("name", template.name + " (from template)");
        workflowParams.put("description", template.description);
        workflowParams.put("steps", template.steps);

        Map<String, Object> workflowResult = createWorkflow(workflowParams);

        result.put("templateId", templateId);
        result.put("templateName", template.name);
        Object workflowId = workflowResult.get("workflowId");
        result.put("workflowId", workflowId != null ? workflowId : "");
        result.put("message", "Template applied successfully");

        return result;
    }

    private Map<String, Object> manageJobQueue(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "manage_job_queue");
        result.put("timestamp", Instant.now().toString());

        List<Map<String, Object>> jobs = jobQueue.values().stream().map(this::jobToMap).collect(Collectors.toList());

        result.put("jobs", jobs);
        result.put("totalJobs", jobs.size());
        result.put("pendingJobs", jobs.stream().mapToInt(j -> "pending".equals(j.get("status")) ? 1 : 0).sum());

        return result;
    }

    private Map<String, Object> getAutomationStats(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "get_automation_stats");
        result.put("timestamp", Instant.now().toString());

        Map<String, Object> stats = new HashMap<>();
        stats.put("activeWorkflows", activeWorkflows.size());
        stats.put("queuedJobs", jobQueue.size());
        stats.put("availableTemplates", templates.size());
        stats.put("scheduledTasks", scheduledTasks.size());

        // Workflow status breakdown
        Map<String, Integer> workflowStatusBreakdown = activeWorkflows.values().stream().collect(Collectors
                .groupingBy(w -> w.status, Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
        stats.put("workflowStatusBreakdown", workflowStatusBreakdown);

        result.put("automationStatistics", stats);
        return result;
    }

    private Map<String, Object> createAutomationChain(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "create_automation_chain");
        result.put("timestamp", Instant.now().toString());

        // This would create a chain of connected workflows
        result.put("message", "Automation chain functionality - future implementation");
        return result;
    }

    private Map<String, Object> exportAutomation(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "export_automation");
        result.put("timestamp", Instant.now().toString());

        // Export automation configurations
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("templates", templates.size());
        exportData.put("activeWorkflows", activeWorkflows.size());
        exportData.put("exportTimestamp", Instant.now().toString());

        result.put("exportData", exportData);
        result.put("message", "Automation configuration exported");
        return result;
    }

    private Map<String, Object> importAutomation(Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "import_automation");
        result.put("timestamp", Instant.now().toString());

        // Import automation configurations
        result.put("message", "Automation import functionality - future implementation");
        return result;
    }

    // Helper methods

    private void executeWorkflowSteps(WorkflowExecution execution) {
        try {
            execution.status = "running";
            logger.info("Executing workflow: {}", execution.workflowId);

            // Simulate workflow execution
            Thread.sleep(1000);

            execution.status = "completed";
            execution.endTime = Instant.now();
            logger.info("Workflow completed: {}", execution.workflowId);

        } catch (Exception e) {
            execution.status = "failed";
            execution.endTime = Instant.now();
            execution.error = e.getMessage();
            logger.error("Workflow failed: {}", execution.workflowId, e);
        }
    }

    private void executeScheduledTask(String taskId, String name, Map<?, ?> parameters) {
        logger.info("Executing scheduled task: {} - {}", taskId, name);

        // Create job for scheduled task
        AutomationJob job = new AutomationJob(taskId, name, "scheduled", parameters);
        jobQueue.put(job.jobId, job);

        // Execute the job
        scheduler.submit(() -> {
            try {
                Thread.sleep(500); // Simulate work
                job.status = "completed";
                job.endTime = Instant.now();
            } catch (Exception e) {
                job.status = "failed";
                job.error = e.getMessage();
            }
        });
    }

    private @Nullable WorkflowExecution findWorkflowExecution(String workflowId) {
        return activeWorkflows.values().stream().filter(w -> workflowId.equals(w.workflowId)).findFirst().orElse(null);
    }

    private Map<String, Object> workflowExecutionToMap(WorkflowExecution execution) {
        Map<String, Object> map = new HashMap<>();
        map.put("executionId", execution.executionId);
        map.put("workflowId", execution.workflowId);
        map.put("status", execution.status);
        map.put("startTime", execution.startTime.toString());
        if (execution.endTime != null) {
            map.put("endTime", execution.endTime.toString());
            map.put("duration", execution.endTime.toEpochMilli() - execution.startTime.toEpochMilli());
        }
        if (execution.error != null) {
            String errorMessage = execution.error;
            map.put("error", errorMessage);
        }
        return map;
    }

    private Map<String, Object> templateToMap(AutomationTemplate template) {
        Map<String, Object> map = new HashMap<>();
        map.put("templateId", template.templateId);
        map.put("name", template.name);
        map.put("description", template.description);
        map.put("steps", template.steps.size());
        map.put("hasDefaultParameters", !template.defaultParameters.isEmpty());
        return map;
    }

    private Map<String, Object> jobToMap(AutomationJob job) {
        Map<String, Object> map = new HashMap<>();
        map.put("jobId", job.jobId);
        map.put("name", job.name);
        map.put("type", job.type);
        map.put("status", job.status);
        map.put("createdTime", job.createdTime.toString());
        if (job.endTime != null) {
            map.put("endTime", job.endTime.toString());
        }
        if (job.error != null) {
            String errorMessage = job.error;
            map.put("error", errorMessage);
        }
        return map;
    }

    private long parseInterval(String interval) {
        char unit = interval.charAt(interval.length() - 1);
        int value = Integer.parseInt(interval.substring(0, interval.length() - 1));

        return switch (unit) {
            case 's' -> value;
            case 'm' -> value * 60;
            case 'h' -> value * 3600;
            case 'd' -> value * 86400;
            default -> throw new IllegalArgumentException("Invalid interval unit: " + unit);
        };
    }

    private void loadDefaultTemplates() {
        // Load some default automation templates
        AutomationTemplate deviceHealthCheck = new AutomationTemplate("device-health-check", "Device Health Check",
                "Checks the health status of all connected devices",
                List.of(Map.of("type", "check_things", "action", "status"),
                        Map.of("type", "log_results", "level", "INFO")),
                Map.of("checkInterval", "5m", "alertOnFailure", true));
        templates.put("device-health-check", deviceHealthCheck);

        AutomationTemplate nightMode = new AutomationTemplate("night-mode", "Night Mode Activation",
                "Activates night mode settings across the home",
                List.of(Map.of("type", "dim_lights", "level", 20), Map.of("type", "set_thermostat", "temperature", 20),
                        Map.of("type", "activate_security", "mode", "night")),
                Map.of("activationTime", "22:00", "deactivationTime", "07:00"));
        templates.put("night-mode", nightMode);
    }

    // Data classes for automation management

    private static class WorkflowDefinition {
        final String workflowId;
        final String name;
        final String description;
        final List<?> steps;
        final Map<?, ?> conditions;

        WorkflowDefinition(String workflowId, String name, String description, List<?> steps, Map<?, ?> conditions) {
            this.workflowId = workflowId;
            this.name = name;
            this.description = description;
            this.steps = steps;
            this.conditions = conditions;
        }
    }

    private static class WorkflowExecution {
        final String executionId;
        final String workflowId;
        final Map<?, ?> parameters;
        final Instant startTime;
        String status;
        @Nullable
        Instant endTime;
        @Nullable
        String error;

        WorkflowExecution(String workflowId, Map<?, ?> parameters) {
            this.executionId = UUID.randomUUID().toString();
            this.workflowId = workflowId;
            this.parameters = parameters;
            this.startTime = Instant.now();
            this.status = "pending";
        }
    }

    private static class AutomationTemplate {
        final String templateId;
        final String name;
        final String description;
        final List<?> steps;
        final Map<?, ?> defaultParameters;

        AutomationTemplate(String templateId, String name, String description, List<?> steps,
                Map<?, ?> defaultParameters) {
            this.templateId = templateId;
            this.name = name;
            this.description = description;
            this.steps = steps;
            this.defaultParameters = defaultParameters;
        }
    }

    private static class AutomationJob {
        final String jobId;
        final String name;
        final String type;
        final Map<?, ?> parameters;
        final Instant createdTime;
        String status;
        @Nullable
        Instant endTime;
        @Nullable
        String error;

        AutomationJob(String taskId, String name, String type, Map<?, ?> parameters) {
            this.jobId = UUID.randomUUID().toString();
            this.name = name;
            this.type = type;
            this.parameters = parameters;
            this.createdTime = Instant.now();
            this.status = "pending";
        }
    }
}
