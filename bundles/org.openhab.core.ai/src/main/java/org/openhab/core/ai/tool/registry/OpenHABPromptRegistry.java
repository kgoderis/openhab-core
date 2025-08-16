package org.openhab.core.ai.tool.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.prompts.api.PromptRegistry;
import org.openhab.core.ai.tool.prompts.api.dto.Prompt;
import org.openhab.core.ai.tool.prompts.api.dto.PromptArgument;
import org.openhab.core.ai.tool.prompts.library.AutomationPrompt;
import org.openhab.core.ai.tool.prompts.library.ItemControlPrompt;
import org.openhab.core.ai.tool.prompts.library.SystemDiagnosticsPrompt;
import org.openhab.core.items.ItemRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

/**
 * OpenHAB-specific implementation of the MCP Prompt Registry.
 * 
 * This registry provides parameterized prompt templates for openHAB operations
 * including item control, automation management, and system diagnostics.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@Component(service = PromptRegistry.class, immediate = true)
@NonNullByDefault
public class OpenHABPromptRegistry implements PromptRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenHABPromptRegistry.class);

    /** Map of prompts by name. */
    private final Map<String, Prompt> prompts = new ConcurrentHashMap<>();

    /** Security filtering - prompts that should be excluded */
    private final Map<String, Boolean> securityFilters = new ConcurrentHashMap<>();

    /** Performance monitoring */
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTimeMs = new AtomicLong(0);

    /** Prompt implementations */
    private @Nullable ItemControlPrompt itemControlPrompt;
    private @Nullable AutomationPrompt automationPrompt;
    private @Nullable SystemDiagnosticsPrompt systemDiagnosticsPrompt;

    /** Item registry for item-related prompts */
    private @Nullable ItemRegistry itemRegistry;

    @Reference
    public void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        LOGGER.debug("ItemRegistry set for OpenHAB Prompt Registry");
    }

    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
        LOGGER.debug("ItemRegistry unset for OpenHAB Prompt Registry");
    }

    @Activate
    public OpenHABPromptRegistry() {
        LOGGER.debug("Initializing OpenHAB Prompt Registry");
        initializePrompts();
    }

    @Deactivate
    public void deactivate() {
        LOGGER.debug("Deactivating OpenHAB Prompt Registry");
        prompts.clear();
        securityFilters.clear();
    }

    @Modified
    public void modified() {
        LOGGER.debug("Modifying OpenHAB Prompt Registry");
        prompts.clear();
        securityFilters.clear();
        initializePrompts();
    }

    private void initializePrompts() {
        // Initialize prompt implementations
        initializePromptImplementations();

        // Initialize Item Control Prompts
        createItemControlPrompts();

        // Initialize Automation Prompts
        createAutomationPrompts();

        // Initialize System Diagnostics Prompts
        createSystemDiagnosticsPrompts();

        LOGGER.info("OpenHAB Prompt Registry initialized with {} prompts", prompts.size());
    }

    private void initializePromptImplementations() {
        // Create prompt implementations
        if (itemRegistry != null) {
            itemControlPrompt = new ItemControlPrompt(itemRegistry);
            LOGGER.debug("ItemControlPrompt initialized");
        } else {
            LOGGER.warn("ItemRegistry not available, ItemControlPrompt will not be initialized");
        }

        automationPrompt = new AutomationPrompt();
        systemDiagnosticsPrompt = new SystemDiagnosticsPrompt();

        LOGGER.debug("Prompt implementations initialized");
    }

    private void createItemControlPrompts() {
        // Item Control Prompt
        Prompt itemControlPrompt = new Prompt("item_control", "Control openHAB items with parameterized commands",
                Arrays.asList(new PromptArgument("itemName", "Name of the openHAB item to control", true),
                        new PromptArgument("action", "Action to perform (ON, OFF, TOGGLE, INCREASE, DECREASE)", true),
                        new PromptArgument("value", "Optional value for the action", false)));
        prompts.put("item_control", itemControlPrompt);

        // Item Status Prompt
        Prompt itemStatusPrompt = new Prompt("item_status", "Get status and information about openHAB items",
                Arrays.asList(new PromptArgument("itemName", "Name of the openHAB item to check", true),
                        new PromptArgument("includeHistory", "Include item history in response", false)));
        prompts.put("item_status", itemStatusPrompt);

        // Item Configuration Prompt
        Prompt itemConfigPrompt = new Prompt("item_configuration", "Configure and manage openHAB item settings",
                Arrays.asList(new PromptArgument("itemName", "Name of the openHAB item to configure", true),
                        new PromptArgument("operation", "Configuration operation to perform (GET, SET, UPDATE, DELETE)",
                                true),
                        new PromptArgument("config", "Configuration parameters", false)));
        prompts.put("item_configuration", itemConfigPrompt);
    }

    private void createAutomationPrompts() {
        // Automation Control Prompt
        Prompt automationControlPrompt = new Prompt("automation_control",
                "Control openHAB automation rules and workflows",
                Arrays.asList(new PromptArgument("ruleUID", "UID of the automation rule", true),
                        new PromptArgument("action",
                                "Action to perform on the rule (ENABLE, DISABLE, EXECUTE, GET_STATUS)", true),
                        new PromptArgument("parameters", "Optional parameters for the action", false)));
        prompts.put("automation_control", automationControlPrompt);

        // Rule Management Prompt
        Prompt ruleManagementPrompt = new Prompt("rule_management", "Manage and configure openHAB rules", Arrays.asList(
                new PromptArgument("operation", "Rule management operation (LIST, CREATE, UPDATE, DELETE, VALIDATE)",
                        true),
                new PromptArgument("ruleUID", "UID of the rule (for specific operations)", false),
                new PromptArgument("ruleDefinition", "Rule definition for create/update operations", false)));
        prompts.put("rule_management", ruleManagementPrompt);

        // Workflow Prompt
        Prompt workflowPrompt = new Prompt("workflow_control", "Control complex automation workflows",
                Arrays.asList(new PromptArgument("workflowName", "Name of the workflow to control", true),
                        new PromptArgument("action",
                                "Workflow action to perform (START, STOP, PAUSE, RESUME, GET_STATUS)", true),
                        new PromptArgument("parameters", "Workflow parameters", false)));
        prompts.put("workflow_control", workflowPrompt);
    }

    private void createSystemDiagnosticsPrompts() {
        // System Diagnostics Prompt
        Prompt systemDiagnosticsPrompt = new Prompt("system_diagnostics",
                "Perform system diagnostics and health checks",
                Arrays.asList(new PromptArgument("diagnosticType",
                        "Type of diagnostic to perform (SYSTEM_HEALTH, PERFORMANCE, MEMORY, NETWORK, STORAGE, SECURITY)",
                        true), new PromptArgument("scope", "Scope of the diagnostic (FULL, QUICK, TARGETED)", false),
                        new PromptArgument("includeDetails", "Include detailed diagnostic information", false)));
        prompts.put("system_diagnostics", systemDiagnosticsPrompt);

        // Performance Monitoring Prompt
        Prompt performancePrompt = new Prompt("performance_monitoring", "Monitor system performance and resource usage",
                Arrays.asList(new PromptArgument("metricType",
                        "Type of performance metric to monitor (CPU, MEMORY, DISK, NETWORK, JVM, BUNDLE)", true),
                        new PromptArgument("duration", "Monitoring duration in seconds", false),
                        new PromptArgument("interval", "Sampling interval in seconds", false)));
        prompts.put("performance_monitoring", performancePrompt);

        // Security Audit Prompt
        Prompt securityPrompt = new Prompt("security_audit", "Perform security audits and vulnerability checks",
                Arrays.asList(new PromptArgument("auditType",
                        "Type of security audit to perform (AUTHENTICATION, AUTHORIZATION, CONFIGURATION, NETWORK, COMPREHENSIVE)",
                        true), new PromptArgument("includeRemediation", "Include remediation suggestions", false),
                        new PromptArgument("severity", "Minimum severity level to report (LOW, MEDIUM, HIGH, CRITICAL)",
                                false)));
        prompts.put("security_audit", securityPrompt);
    }

    @Override
    public void registerPrompt(final Prompt prompt) {
        String name = prompt.getName();

        // Apply security filtering
        if (isPromptBlocked(name)) {
            LOGGER.warn("Prompt registration blocked by security filter: {}", name);
            return;
        }

        prompts.put(name, prompt);
        LOGGER.debug("Registered prompt: {}", name);
    }

    @Override
    public void unregisterPrompt(final String name) {
        prompts.remove(name);
        LOGGER.debug("Unregistered prompt: {}", name);
    }

    @Override
    public @Nullable Prompt getPrompt(final String name) {
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();

        try {
            // Apply security filtering
            if (isPromptBlocked(name)) {
                LOGGER.warn("Prompt access blocked by security filter: {}", name);
                failedRequests.incrementAndGet();
                return null;
            }

            Prompt prompt = prompts.get(name);
            if (prompt != null) {
                successfulRequests.incrementAndGet();
            } else {
                failedRequests.incrementAndGet();
            }

            return prompt;
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTimeMs.addAndGet(responseTime);
        }
    }

    @Override
    public Map<String, Prompt> getAllPrompts() {
        return prompts.entrySet().stream().filter(entry -> !isPromptBlocked(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public int getPromptCount() {
        return (int) prompts.entrySet().stream().filter(entry -> !isPromptBlocked(entry.getKey())).count();
    }

    @Override
    public boolean isPromptRegistered(final String name) {
        return prompts.containsKey(name) && !isPromptBlocked(name);
    }

    @Override
    public Map<String, Object>[] getPromptDescriptors() {
        try {
            var list = new ArrayList<Map<String, Object>>();
            for (var entry : getAllPrompts().entrySet()) {
                var p = entry.getValue();
                var args = new ArrayList<Map<String, Object>>();
                for (var a : p.getArguments()) {
                    args.add(
                            Map.of("name", a.getName(), "description", a.getDescription(), "required", a.isRequired()));
                }
                list.add(Map.of("name", p.getName(), "description", p.getDescription(), "arguments", args));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object>[] arr = list.toArray(new Map[0]);
            return arr;
        } catch (Exception e) {
            LOGGER.warn("Failed to build prompt descriptors", e);
            return new Map[0];
        }
    }

    @Override
    public McpServerFeatures.SyncPromptSpecification[] getSyncPromptSpecifications() {
        try {
            LOGGER.debug("Creating sync prompt specifications for {} prompts", prompts.size());

            // Create prompt specifications using MCP SDK builders
            var specs = new ArrayList<McpServerFeatures.SyncPromptSpecification>();

            for (var entry : getAllPrompts().entrySet()) {
                var prompt = entry.getValue();

                try {
                    // Convert internal prompt arguments to MCP format
                    var mcpArguments = prompt.getArguments().stream().map(
                            arg -> new McpSchema.PromptArgument(arg.getName(), arg.getDescription(), arg.isRequired()))
                            .collect(Collectors.toList());

                    // Create MCP prompt specification using the correct SDK structure
                    var mcpPrompt = new McpSchema.Prompt(prompt.getName(), prompt.getDescription(), mcpArguments);

                    var syncPromptSpec = new McpServerFeatures.SyncPromptSpecification(mcpPrompt,
                            (exchange, request) -> {
                                LOGGER.debug("Handling sync prompt for: {}", prompt.getName());

                                // Return a simple prompt result with description and empty messages
                                // TODO: Implement proper message handling when internal Prompt class supports messages
                                return new McpSchema.GetPromptResult(prompt.getDescription(), List.of() // Empty
                                                                                                        // messages
                                                                                                        // for
                                                                                                        // now
                                );
                            });

                    specs.add(syncPromptSpec);
                    LOGGER.debug("Created sync prompt specification for: {}", prompt.getName());

                } catch (Exception e) {
                    LOGGER.error("Error creating sync prompt specification for: {}", prompt.getName(), e);
                }
            }

            LOGGER.debug("Created {} sync prompt specifications", specs.size());
            return specs.toArray(new McpServerFeatures.SyncPromptSpecification[0]);

        } catch (Exception e) {
            LOGGER.error("Error creating sync prompt specifications", e);
            return new McpServerFeatures.SyncPromptSpecification[0];
        }
    }

    @Override
    public McpServerFeatures.AsyncPromptSpecification[] getAsyncPromptSpecifications() {
        try {
            LOGGER.debug("Creating async prompt specifications for {} prompts", prompts.size());

            // Create prompt specifications using MCP SDK builders
            var specs = new ArrayList<McpServerFeatures.AsyncPromptSpecification>();

            for (var entry : getAllPrompts().entrySet()) {
                var prompt = entry.getValue();

                try {
                    // Convert internal prompt arguments to MCP format
                    var mcpArguments = prompt.getArguments().stream().map(
                            arg -> new McpSchema.PromptArgument(arg.getName(), arg.getDescription(), arg.isRequired()))
                            .collect(Collectors.toList());

                    // Create MCP prompt specification using the correct SDK structure
                    var mcpPrompt = new McpSchema.Prompt(prompt.getName(), prompt.getDescription(), mcpArguments);

                    var asyncPromptSpec = new McpServerFeatures.AsyncPromptSpecification(mcpPrompt,
                            (exchange, request) -> {
                                LOGGER.debug("Handling async prompt for: {}", prompt.getName());

                                return Mono.fromCallable(() -> {
                                    // Return a simple prompt result with description and empty messages
                                    // TODO: Implement proper message handling when internal Prompt class supports
                                    // messages
                                    return new McpSchema.GetPromptResult(prompt.getDescription(), List.of() // Empty
                                                                                                            // messages
                                                                                                            // for
                                                                                                            // now
                                    );
                                });
                            });

                    specs.add(asyncPromptSpec);
                    LOGGER.debug("Created async prompt specification for: {}", prompt.getName());

                } catch (Exception e) {
                    LOGGER.error("Error creating async prompt specification for: {}", prompt.getName(), e);
                }
            }

            LOGGER.debug("Created {} async prompt specifications", specs.size());
            return specs.toArray(new McpServerFeatures.AsyncPromptSpecification[0]);

        } catch (Exception e) {
            LOGGER.error("Error creating async prompt specifications", e);
            return new McpServerFeatures.AsyncPromptSpecification[0];
        }
    }

    /**
     * Check if a prompt is blocked by security filters.
     *
     * @param name the prompt name
     * @return true if the prompt is blocked
     */
    private boolean isPromptBlocked(String name) {
        return securityFilters.containsKey(name) && securityFilters.get(name);
    }

    /**
     * Add a security filter to block a prompt.
     *
     * @param name the prompt name to block
     */
    public void blockPrompt(String name) {
        securityFilters.put(name, true);
        LOGGER.info("Prompt blocked by security filter: {}", name);
    }

    /**
     * Remove a security filter for a prompt.
     *
     * @param name the prompt name to unblock
     */
    public void unblockPrompt(String name) {
        securityFilters.remove(name);
        LOGGER.info("Prompt unblocked: {}", name);
    }

    /**
     * Execute a specific prompt by name.
     *
     * @param promptName the name of the prompt to execute
     * @param arguments the arguments for the prompt
     * @return the execution result or null if prompt not found
     */
    public @Nullable PromptExecutionResult executePrompt(String promptName, Map<String, Object> arguments) {
        LOGGER.debug("Executing prompt: {} with arguments: {}", promptName, arguments);

        try {
            switch (promptName) {
                case ItemControlPrompt.PROMPT_NAME:
                    if (itemControlPrompt != null) {
                        PromptExecutionResult result = itemControlPrompt.execute(arguments);
                        return new PromptExecutionResult(result.isSuccess(), result.getErrorMessage(),
                                result.getContent());
                    } else {
                        LOGGER.warn("ItemControlPrompt not available");
                        return new PromptExecutionResult(false, "ItemControlPrompt not available", null);
                    }
                case AutomationPrompt.PROMPT_NAME:
                    if (automationPrompt != null) {
                        PromptExecutionResult result = automationPrompt.execute(arguments);
                        return new PromptExecutionResult(result.isSuccess(), result.getErrorMessage(),
                                result.getContent());
                    } else {
                        LOGGER.warn("AutomationPrompt not available");
                        return new PromptExecutionResult(false, "AutomationPrompt not available", null);
                    }
                case SystemDiagnosticsPrompt.PROMPT_NAME:
                    if (systemDiagnosticsPrompt != null) {
                        PromptExecutionResult result = systemDiagnosticsPrompt.execute(arguments);
                        return new PromptExecutionResult(result.isSuccess(), result.getErrorMessage(),
                                result.getContent());
                    } else {
                        LOGGER.warn("SystemDiagnosticsPrompt not available");
                        return new PromptExecutionResult(false, "SystemDiagnosticsPrompt not available", null);
                    }
                default:
                    LOGGER.warn("Unknown prompt: {}", promptName);
                    return new PromptExecutionResult(false, "Unknown prompt: " + promptName, null);
            }
        } catch (Exception e) {
            LOGGER.error("Error executing prompt: {}", promptName, e);
            return new PromptExecutionResult(false, "Error executing prompt: " + e.getMessage(), null);
        }
    }

    /**
     * Get performance metrics for all prompts.
     *
     * @return performance metrics as a map
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("successfulRequests", successfulRequests.get());
        metrics.put("failedRequests", failedRequests.get());
        metrics.put("totalResponseTimeMs", totalResponseTimeMs.get());
        metrics.put("averageResponseTimeMs",
                totalRequests.get() > 0 ? totalResponseTimeMs.get() / totalRequests.get() : 0);
        metrics.put("successRate",
                totalRequests.get() > 0 ? (double) successfulRequests.get() / totalRequests.get() : 0.0);

        // Add individual prompt metrics
        if (itemControlPrompt != null) {
            metrics.put("itemControlPrompt", itemControlPrompt.getPerformanceMetrics());
        }
        if (automationPrompt != null) {
            metrics.put("automationPrompt", automationPrompt.getPerformanceMetrics());
        }
        if (systemDiagnosticsPrompt != null) {
            metrics.put("systemDiagnosticsPrompt", systemDiagnosticsPrompt.getPerformanceMetrics());
        }

        return metrics;
    }

    /**
     * Get the argument schema for a specific prompt.
     *
     * @param promptName the name of the prompt
     * @return the argument schema or null if prompt not found
     */
    public @Nullable Map<String, Object> getPromptArgumentSchema(String promptName) {
        switch (promptName) {
            case ItemControlPrompt.PROMPT_NAME:
                return itemControlPrompt != null ? itemControlPrompt.getArgumentSchema() : null;
            case AutomationPrompt.PROMPT_NAME:
                return automationPrompt != null ? automationPrompt.getArgumentSchema() : null;
            case SystemDiagnosticsPrompt.PROMPT_NAME:
                return systemDiagnosticsPrompt != null ? systemDiagnosticsPrompt.getArgumentSchema() : null;
            default:
                LOGGER.warn("Unknown prompt for schema: {}", promptName);
                return null;
        }
    }

    /**
     * Get usage examples for a specific prompt.
     *
     * @param promptName the name of the prompt
     * @return the usage examples or null if prompt not found
     */
    public @Nullable String[] getPromptUsageExamples(String promptName) {
        switch (promptName) {
            case ItemControlPrompt.PROMPT_NAME:
                return itemControlPrompt != null ? itemControlPrompt.getUsageExamples() : null;
            case AutomationPrompt.PROMPT_NAME:
                return automationPrompt != null ? automationPrompt.getUsageExamples() : null;
            case SystemDiagnosticsPrompt.PROMPT_NAME:
                return systemDiagnosticsPrompt != null ? systemDiagnosticsPrompt.getUsageExamples() : null;
            default:
                LOGGER.warn("Unknown prompt for examples: {}", promptName);
                return null;
        }
    }
}
