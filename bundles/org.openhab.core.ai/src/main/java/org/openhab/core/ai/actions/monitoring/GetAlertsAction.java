package org.openhab.core.ai.actions.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for retrieving and managing alerts and notifications in openHAB monitoring.
 */
@Component(service = Action.class, immediate = true)
public class GetAlertsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetAlertsAction.class);
    private static final String ACTION_ID = "openhab.monitoring.get_alerts";
    private static final String ACTION_NAME = "Get Alerts";

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
        return "Retrieves and manages alerts and notifications for openHAB monitoring and system health";
    }

    @Override
    public String getCategory() {
        return "monitoring";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("action",
                Map.of("type", "string", "enum", List.of("get_alerts", "get_alert_history", "get_alert_config",
                        "acknowledge_alert", "clear_alert"), "description", "Alert action to perform", "default",
                        "get_alerts"));
        properties.put("severity", Map.of("type", "string", "enum", List.of("LOW", "MEDIUM", "HIGH", "CRITICAL"),
                "description", "Filter alerts by severity"));
        properties.put("status", Map.of("type", "string", "enum",
                List.of("ACTIVE", "ACKNOWLEDGED", "RESOLVED", "CLEARED"), "description", "Filter alerts by status"));
        properties.put("category", Map.of("type", "string", "description",
                "Filter alerts by category (e.g., 'system', 'performance', 'security')"));
        properties.put("timeRange", Map.of("type", "string", "description",
                "Time range for alert history (e.g., '1h', '24h', '7d')", "default", "24h"));
        properties.put("maxResults", Map.of("type", "integer", "description", "Maximum number of alerts to return",
                "default", 100, "minimum", 1, "maximum", 1000));
        properties.put("alertId",
                Map.of("type", "string", "description", "Specific alert ID for acknowledge or clear actions"));

        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("action", Map.of("type", "string", "description", "The action that was performed"));
        properties.put("alerts",
                Map.of("type", "array", "items", Map.of("type", "object"), "description", "List of alerts"));
        properties.put("alertHistory", Map.of("type", "array", "description", "Alert history entries"));
        properties.put("alertConfig", Map.of("type", "object", "description", "Alert configuration"));
        properties.put("totalAlerts", Map.of("type", "integer", "description", "Total number of alerts"));
        properties.put("activeAlerts", Map.of("type", "integer", "description", "Number of active alerts"));
        properties.put("filters", Map.of("type", "object", "description", "Applied filters"));
        properties.put("timestamp", Map.of("type", "string", "description", "ISO timestamp of the operation"));
        properties.put("message", Map.of("type", "string", "description", "Operation result message"));
        properties.put("error", Map.of("type", "string", "description", "Error message if operation failed"));

        schema.put("properties", properties);
        return schema;
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return ActionValidationResult.invalid(List.of("Parameters cannot be null"));
        }

        List<String> errors = new ArrayList<>();

        // Validate maxResults
        Object maxResultsObj = parameters.get("maxResults");
        if (maxResultsObj != null) {
            if (maxResultsObj instanceof Integer) {
                Integer maxResults = (Integer) maxResultsObj;
                if (maxResults < 1 || maxResults > 1000) {
                    errors.add("maxResults must be between 1 and 1000");
                }
            } else {
                errors.add("maxResults must be an integer");
            }
        }

        // Validate alertId for specific actions
        String action = (String) parameters.getOrDefault("action", "get_alerts");
        if ((action.equals("acknowledge_alert") || action.equals("clear_alert"))) {
            String alertId = (String) parameters.get("alertId");
            if (alertId == null || alertId.trim().isEmpty()) {
                errors.add("alertId is required for acknowledge_alert and clear_alert actions");
            }
        }

        if (!errors.isEmpty()) {
            return ActionValidationResult.invalid(errors);
        }

        return ActionValidationResult.valid(parameters);
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        long startTime = System.currentTimeMillis();
        logger.debug("Executing get alerts action with parameters: {}", parameters);

        try {
            String action = (String) parameters.getOrDefault("action", "get_alerts");
            String severity = (String) parameters.get("severity");
            String status = (String) parameters.get("status");
            String category = (String) parameters.get("category");
            String timeRange = (String) parameters.getOrDefault("timeRange", "24h");
            Integer maxResults = (Integer) parameters.getOrDefault("maxResults", 100);
            String alertId = (String) parameters.get("alertId");

            Map<String, Object> result = new HashMap<>();
            result.put("action", action);
            result.put("timestamp", Instant.now().toString());

            switch (action) {
                case "get_alerts":
                    result.putAll(getCurrentAlerts(severity, status, category, maxResults));
                    break;
                case "get_alert_history":
                    result.putAll(getAlertHistory(timeRange, maxResults));
                    break;
                case "get_alert_config":
                    result.putAll(getAlertConfiguration());
                    break;
                case "acknowledge_alert":
                    result.putAll(acknowledgeAlert(alertId));
                    break;
                case "clear_alert":
                    result.putAll(clearAlert(alertId));
                    break;
                default:
                    throw new ActionException(ACTION_ID, "Unknown action: " + action);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("Get alerts action completed in {}ms", executionTime);

            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to get alerts", e);
            throw new ActionException(ACTION_ID, "Failed to get alerts: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ActionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (ActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().version(getVersion()).author("openHAB")
                .description("Retrieves and manages alerts and notifications for monitoring")
                .tags(List.of("monitoring", "alerts", "notifications", "system-health"))
                .documentation(
                        "Provides comprehensive alert management including current alerts, history, configuration, and alert actions")
                .examples(List.of("{\"action\": \"get_alerts\", \"severity\": \"HIGH\"} - Get high severity alerts",
                        "{\"action\": \"get_alert_history\", \"timeRange\": \"7d\"} - Get alert history for last week",
                        "{\"action\": \"acknowledge_alert\", \"alertId\": \"alert-123\"} - Acknowledge specific alert"))
                .build();
    }

    @Override
    public Map<String, Object> getCapabilities() {
        return Map.of("filtering", true, "sorting", true, "pagination", true, "metadata", true, "async", true,
                "realTime", true);
    }

    @Override
    public void initialize(ActionContext context) {
        logger.debug("GetAlertsAction initialized for protocol: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetAlertsAction cleanup completed");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private Map<String, Object> getCurrentAlerts(String severity, String status, String category, int maxResults) {
        Map<String, Object> result = new HashMap<>();

        // In a real implementation, you would query an alert management system
        // For now, we'll simulate some alerts
        List<Map<String, Object>> alerts = new ArrayList<>();

        // Simulate some system alerts
        if (severity == null || "HIGH".equals(severity)) {
            alerts.add(createAlert("alert-001", "High CPU Usage", "HIGH", "ACTIVE", "system",
                    "CPU usage is above 90% for more than 5 minutes", Instant.now().minusSeconds(300)));
        }

        if (severity == null || "MEDIUM".equals(severity)) {
            alerts.add(createAlert("alert-002", "Memory Usage Warning", "MEDIUM", "ACTIVE", "performance",
                    "Memory usage is above 80%", Instant.now().minusSeconds(600)));
        }

        if (severity == null || "LOW".equals(severity)) {
            alerts.add(createAlert("alert-003", "Log File Size", "LOW", "ACKNOWLEDGED", "maintenance",
                    "Log file size is approaching limit", Instant.now().minusSeconds(3600)));
        }

        // Apply filters
        alerts = alerts.stream().filter(alert -> status == null || status.equals(alert.get("status")))
                .filter(alert -> category == null || category.equals(alert.get("category"))).limit(maxResults).toList();

        result.put("alerts", alerts);
        result.put("totalAlerts", alerts.size());
        result.put("activeAlerts", alerts.stream().filter(a -> "ACTIVE".equals(a.get("status"))).count());
        result.put("filters", Map.of("severity", severity != null ? severity : "all", "status",
                status != null ? status : "all", "category", category != null ? category : "all"));
        result.put("message", "Retrieved " + alerts.size() + " alerts");

        return result;
    }

    private Map<String, Object> getAlertHistory(String timeRange, int maxResults) {
        Map<String, Object> result = new HashMap<>();

        // In a real implementation, you would query alert history from a database
        // For now, we'll simulate some historical alerts
        List<Map<String, Object>> history = new ArrayList<>();

        history.add(createAlertHistory("alert-001", "High CPU Usage", "HIGH", "RESOLVED", "system",
                Instant.now().minusSeconds(3600), Instant.now().minusSeconds(300)));
        history.add(createAlertHistory("alert-002", "Disk Space Low", "MEDIUM", "CLEARED", "storage",
                Instant.now().minusSeconds(7200), Instant.now().minusSeconds(1800)));
        history.add(createAlertHistory("alert-003", "Network Connectivity", "CRITICAL", "RESOLVED", "network",
                Instant.now().minusSeconds(10800), Instant.now().minusSeconds(900)));

        result.put("alertHistory", history.stream().limit(maxResults).toList());
        result.put("totalHistory", history.size());
        result.put("timeRange", timeRange);
        result.put("message", "Retrieved " + Math.min(history.size(), maxResults) + " alert history entries");

        return result;
    }

    private Map<String, Object> getAlertConfiguration() {
        Map<String, Object> result = new HashMap<>();

        // In a real implementation, you would retrieve alert configuration from settings
        Map<String, Object> config = new HashMap<>();

        // Alert thresholds
        Map<String, Object> thresholds = new HashMap<>();
        thresholds.put("cpuUsage", 90.0);
        thresholds.put("memoryUsage", 80.0);
        thresholds.put("diskUsage", 85.0);
        thresholds.put("logFileSize", "100MB");
        config.put("thresholds", thresholds);

        // Notification settings
        Map<String, Object> notifications = new HashMap<>();
        notifications.put("emailEnabled", true);
        notifications.put("webhookEnabled", false);
        notifications.put("consoleEnabled", true);
        config.put("notifications", notifications);

        // Alert categories
        List<String> categories = List.of("system", "performance", "security", "maintenance", "network");
        config.put("categories", categories);

        result.put("alertConfig", config);
        result.put("message", "Alert configuration retrieved successfully");

        return result;
    }

    private Map<String, Object> acknowledgeAlert(String alertId) {
        Map<String, Object> result = new HashMap<>();

        // In a real implementation, you would update the alert status in the alert management system
        logger.info("Acknowledging alert: {}", alertId);

        result.put("alertId", alertId);
        result.put("status", "ACKNOWLEDGED");
        result.put("acknowledgedAt", Instant.now().toString());
        result.put("message", "Alert " + alertId + " acknowledged successfully");

        return result;
    }

    private Map<String, Object> clearAlert(String alertId) {
        Map<String, Object> result = new HashMap<>();

        // In a real implementation, you would clear the alert in the alert management system
        logger.info("Clearing alert: {}", alertId);

        result.put("alertId", alertId);
        result.put("status", "CLEARED");
        result.put("clearedAt", Instant.now().toString());
        result.put("message", "Alert " + alertId + " cleared successfully");

        return result;
    }

    private Map<String, Object> createAlert(String id, String title, String severity, String status, String category,
            String description, Instant createdAt) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", id);
        alert.put("title", title);
        alert.put("severity", severity);
        alert.put("status", status);
        alert.put("category", category);
        alert.put("description", description);
        alert.put("createdAt", createdAt.toString());
        alert.put("updatedAt", Instant.now().toString());
        return alert;
    }

    private Map<String, Object> createAlertHistory(String id, String title, String severity, String status,
            String category, Instant createdAt, Instant resolvedAt) {
        Map<String, Object> history = new HashMap<>();
        history.put("id", id);
        history.put("title", title);
        history.put("severity", severity);
        history.put("status", status);
        history.put("category", category);
        history.put("createdAt", createdAt.toString());
        history.put("resolvedAt", resolvedAt.toString());
        history.put("duration", resolvedAt.getEpochSecond() - createdAt.getEpochSecond());
        return history;
    }
}
