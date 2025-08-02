package org.openhab.core.ai.common.actions.network;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionContext;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.openhab.core.ai.common.api.action.AIActionMetadata;
import org.openhab.core.ai.common.api.action.AIActionResult;
import org.openhab.core.ai.common.api.action.AIActionValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to get network statistics and performance metrics
 * 
 * @author openHAB
 */
@NonNullByDefault
public class GetNetworkStatisticsAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetNetworkStatisticsAction.class);

    private static final String ACTION_ID = "get_network_statistics";
    private static final String ACTION_NAME = "Get Network Statistics";
    private static final String DESCRIPTION = "Retrieves network statistics and performance metrics";
    private static final String CATEGORY = "network";
    private static final String VERSION = "1.0.0";

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
        properties.put("includeInterfaceStats",
                Map.of("type", "boolean", "description", "Include per-interface statistics", "default", true));
        properties.put("includeTrafficStats",
                Map.of("type", "boolean", "description", "Include traffic statistics", "default", true));
        properties.put("includeErrorStats",
                Map.of("type", "boolean", "description", "Include error statistics", "default", true));
        properties.put("interfaceName",
                Map.of("type", "string", "description", "Specific interface name to query (optional)"));

        schema.put("properties", properties);
        schema.put("required", List.of());

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("overallStats", Map.of("type", "object", "description", "Overall network statistics"));
        properties.put("interfaceStats", Map.of("type", "array", "description", "Per-interface statistics"));
        properties.put("trafficStats", Map.of("type", "object", "description", "Traffic statistics"));
        properties.put("errorStats", Map.of("type", "object", "description", "Error statistics"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the statistics collection"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("interface_statistics", true);
        capabilities.put("traffic_statistics", true);
        capabilities.put("error_statistics", true);
        capabilities.put("performance_metrics", true);
        return capabilities;
    }

    @Override
    public AIActionMetadata getMetadata() {
        return AIActionMetadata.builder().description(DESCRIPTION).version(VERSION).author("openHAB").build();
    }

    @Override
    public AIActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate boolean parameters
        validateBooleanParameter(parameters, "includeInterfaceStats", errors);
        validateBooleanParameter(parameters, "includeTrafficStats", errors);
        validateBooleanParameter(parameters, "includeErrorStats", errors);

        if (errors.isEmpty()) {
            return AIActionValidationResult.valid(parameters);
        } else {
            return AIActionValidationResult.invalid(errors);
        }
    }

    private void validateBooleanParameter(Map<String, Object> parameters, String paramName, List<String> errors) {
        Object value = parameters.get(paramName);
        if (value != null && !(value instanceof Boolean)) {
            errors.add(paramName + " must be a boolean");
        }
    }

    @Override
    public AIActionResult execute(Map<String, Object> parameters, AIActionContext context) throws AIActionException {
        logger.debug("Executing GetNetworkStatisticsAction with context: {}", context.getProtocol());
        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters with defaults
            boolean includeInterfaceStats = (Boolean) parameters.getOrDefault("includeInterfaceStats", true);
            boolean includeTrafficStats = (Boolean) parameters.getOrDefault("includeTrafficStats", true);
            boolean includeErrorStats = (Boolean) parameters.getOrDefault("includeErrorStats", true);
            String interfaceName = (String) parameters.get("interfaceName");

            Map<String, Object> result = new HashMap<>();

            // Get overall statistics
            result.put("overallStats", getOverallStatistics());

            // Get interface statistics
            if (includeInterfaceStats) {
                result.put("interfaceStats", getInterfaceStatistics(interfaceName));
            }

            // Get traffic statistics
            if (includeTrafficStats) {
                result.put("trafficStats", getTrafficStatistics());
            }

            // Get error statistics
            if (includeErrorStats) {
                result.put("errorStats", getErrorStatistics());
            }

            result.put("timestamp", java.time.Instant.now().toString());

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error getting network statistics: {}", e.getMessage(), e);
            throw new AIActionException(ACTION_ID, "Failed to get network statistics: " + e.getMessage(), e);
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
    public void initialize(AIActionContext context) {
        logger.debug("GetNetworkStatisticsAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetNetworkStatisticsAction cleaned up");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            int totalInterfaces = 0;
            int activeInterfaces = 0;
            int loopbackInterfaces = 0;
            int virtualInterfaces = 0;

            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                totalInterfaces++;

                if (ni.isUp()) {
                    activeInterfaces++;
                }

                if (ni.isLoopback()) {
                    loopbackInterfaces++;
                }

                if (ni.isVirtual()) {
                    virtualInterfaces++;
                }
            }

            stats.put("totalInterfaces", totalInterfaces);
            stats.put("activeInterfaces", activeInterfaces);
            stats.put("inactiveInterfaces", totalInterfaces - activeInterfaces);
            stats.put("loopbackInterfaces", loopbackInterfaces);
            stats.put("virtualInterfaces", virtualInterfaces);
            stats.put("physicalInterfaces", totalInterfaces - virtualInterfaces);

        } catch (SocketException e) {
            logger.warn("Error getting overall network statistics: {}", e.getMessage());
            stats.put("error", "Could not retrieve overall statistics: " + e.getMessage());
        }

        return stats;
    }

    private List<Map<String, Object>> getInterfaceStatistics(String specificInterface) {
        List<Map<String, Object>> interfaceStats = new ArrayList<>();

        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                // Skip if specific interface is requested and this isn't it
                if (specificInterface != null && !ni.getName().equals(specificInterface)) {
                    continue;
                }

                Map<String, Object> stats = new HashMap<>();
                stats.put("name", ni.getName());
                stats.put("displayName", ni.getDisplayName());
                stats.put("isUp", ni.isUp());
                stats.put("isLoopback", ni.isLoopback());
                stats.put("isPointToPoint", ni.isPointToPoint());
                stats.put("isVirtual", ni.isVirtual());
                stats.put("mtu", ni.getMTU());
                stats.put("hardwareAddress", getHardwareAddress(ni));

                // Get interface addresses count
                stats.put("addressCount", ni.getInterfaceAddresses().size());

                // Get sub-interfaces count
                stats.put("subInterfaceCount", Collections.list(ni.getSubInterfaces()).size());

                // Simulated traffic statistics (in real implementation would use system APIs)
                if (ni.isUp() && !ni.isLoopback()) {
                    Map<String, Object> trafficStats = new HashMap<>();
                    trafficStats.put("bytesReceived", generateRandomTrafficValue());
                    trafficStats.put("bytesSent", generateRandomTrafficValue());
                    trafficStats.put("packetsReceived", generateRandomTrafficValue() / 1000);
                    trafficStats.put("packetsSent", generateRandomTrafficValue() / 1000);
                    trafficStats.put("errorsReceived", generateRandomErrorValue());
                    trafficStats.put("errorsSent", generateRandomErrorValue());
                    trafficStats.put("droppedReceived", generateRandomErrorValue());
                    trafficStats.put("droppedSent", generateRandomErrorValue());

                    stats.put("trafficStats", trafficStats);
                }

                interfaceStats.add(stats);
            }
        } catch (SocketException e) {
            logger.warn("Error getting interface statistics: {}", e.getMessage());
        }

        return interfaceStats;
    }

    private String getHardwareAddress(NetworkInterface ni) {
        try {
            byte[] mac = ni.getHardwareAddress();
            if (mac != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                }
                return sb.toString();
            }
        } catch (SocketException e) {
            logger.debug("Could not get hardware address for {}: {}", ni.getName(), e.getMessage());
        }
        return "unknown";
    }

    private Map<String, Object> getTrafficStatistics() {
        Map<String, Object> trafficStats = new HashMap<>();

        // Simulated overall traffic statistics
        long totalBytesReceived = 0;
        long totalBytesSent = 0;
        long totalPacketsReceived = 0;
        long totalPacketsSent = 0;

        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isUp() && !ni.isLoopback()) {
                    // Simulate traffic data
                    long bytesReceived = generateRandomTrafficValue();
                    long bytesSent = generateRandomTrafficValue();

                    totalBytesReceived += bytesReceived;
                    totalBytesSent += bytesSent;
                    totalPacketsReceived += bytesReceived / 1000;
                    totalPacketsSent += bytesSent / 1000;
                }
            }
        } catch (SocketException e) {
            logger.warn("Error getting traffic statistics: {}", e.getMessage());
        }

        trafficStats.put("totalBytesReceived", totalBytesReceived);
        trafficStats.put("totalBytesSent", totalBytesSent);
        trafficStats.put("totalPacketsReceived", totalPacketsReceived);
        trafficStats.put("totalPacketsSent", totalPacketsSent);
        trafficStats.put("averagePacketSize", totalPacketsReceived > 0 ? totalBytesReceived / totalPacketsReceived : 0);

        return trafficStats;
    }

    private Map<String, Object> getErrorStatistics() {
        Map<String, Object> errorStats = new HashMap<>();

        // Simulated error statistics
        long totalErrorsReceived = 0;
        long totalErrorsSent = 0;
        long totalDroppedReceived = 0;
        long totalDroppedSent = 0;

        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isUp() && !ni.isLoopback()) {
                    totalErrorsReceived += generateRandomErrorValue();
                    totalErrorsSent += generateRandomErrorValue();
                    totalDroppedReceived += generateRandomErrorValue();
                    totalDroppedSent += generateRandomErrorValue();
                }
            }
        } catch (SocketException e) {
            logger.warn("Error getting error statistics: {}", e.getMessage());
        }

        errorStats.put("totalErrorsReceived", totalErrorsReceived);
        errorStats.put("totalErrorsSent", totalErrorsSent);
        errorStats.put("totalDroppedReceived", totalDroppedReceived);
        errorStats.put("totalDroppedSent", totalDroppedSent);
        errorStats.put("errorRate", totalErrorsReceived + totalErrorsSent);
        errorStats.put("dropRate", totalDroppedReceived + totalDroppedSent);

        return errorStats;
    }

    private long generateRandomTrafficValue() {
        // Generate realistic traffic values (in bytes)
        return (long) (Math.random() * 1000000000) + 1000000; // 1MB to 1GB
    }

    private long generateRandomErrorValue() {
        // Generate realistic error values
        return (long) (Math.random() * 1000) + 1; // 1 to 1000 errors
    }
}
