package org.openhab.core.ai.action.library.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to get comprehensive network status information
 * 
 * @author openHAB
 */
@NonNullByDefault
public class GetNetworkStatusAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetNetworkStatusAction.class);

    private static final String ACTION_ID = "get_network_status";
    private static final String ACTION_NAME = "Get Network Status";
    private static final String DESCRIPTION = "Retrieves comprehensive network status information including connectivity, interfaces, and network health";
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
        properties.put("includeInterfaceDetails",
                Map.of("type", "boolean", "description", "Include detailed interface information", "default", true));
        properties.put("includeConnectivityTest",
                Map.of("type", "boolean", "description", "Test connectivity to common hosts", "default", true));
        properties.put("includeNetworkHealth",
                Map.of("type", "boolean", "description", "Include network health assessment", "default", true));
        properties.put("timeout",
                Map.of("type", "integer", "description", "Connectivity test timeout in milliseconds", "default", 5000));

        schema.put("properties", properties);
        schema.put("required", List.of());

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("overallStatus", Map.of("type", "string", "description", "Overall network status"));
        properties.put("interfaces", Map.of("type", "array", "description", "List of network interfaces"));
        properties.put("connectivity", Map.of("type", "object", "description", "Connectivity test results"));
        properties.put("networkHealth", Map.of("type", "object", "description", "Network health assessment"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the status check"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("network_status", true);
        capabilities.put("connectivity_test", true);
        capabilities.put("interface_info", true);
        return capabilities;
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().withDescription(DESCRIPTION).withVersion(VERSION).withAuthor("openHAB").build();
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate timeout parameter
        Object timeoutObj = parameters.get("timeout");
        if (timeoutObj != null) {
            if (!(timeoutObj instanceof Integer)) {
                errors.add("Timeout must be an integer");
            } else {
                int timeout = (Integer) timeoutObj;
                if (timeout < 100 || timeout > 30000) {
                    errors.add("Timeout must be between 100 and 30000 milliseconds");
                }
            }
        }

        // Validate boolean parameters
        validateBooleanParameter(parameters, "includeInterfaceDetails", errors);
        validateBooleanParameter(parameters, "includeConnectivityTest", errors);
        validateBooleanParameter(parameters, "includeNetworkHealth", errors);

        if (errors.isEmpty()) {
            return ActionValidationResult.valid(parameters);
        } else {
            return ActionValidationResult.invalid(errors);
        }
    }

    private void validateBooleanParameter(Map<String, Object> parameters, String paramName, List<String> errors) {
        Object value = parameters.get(paramName);
        if (value != null && !(value instanceof Boolean)) {
            errors.add(paramName + " must be a boolean");
        }
    }

    @Override
    public ActionResult execute(Map<String, Object> parameters, ExecutionContext context) throws ActionException {
        logger.debug("Executing GetNetworkStatusAction with context: {}", context.getProtocol());
        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters with defaults
            boolean includeInterfaceDetails = (Boolean) parameters.getOrDefault("includeInterfaceDetails", true);
            boolean includeConnectivityTest = (Boolean) parameters.getOrDefault("includeConnectivityTest", true);
            boolean includeNetworkHealth = (Boolean) parameters.getOrDefault("includeNetworkHealth", true);
            int timeout = (Integer) parameters.getOrDefault("timeout", 5000);

            Map<String, Object> result = new HashMap<>();

            // Get overall network status
            String overallStatus = determineOverallStatus();
            result.put("overallStatus", overallStatus);

            // Get interface information
            if (includeInterfaceDetails) {
                result.put("interfaces", getNetworkInterfaces());
            }

            // Test connectivity
            if (includeConnectivityTest) {
                result.put("connectivity", testConnectivity(timeout));
            }

            // Assess network health
            if (includeNetworkHealth) {
                result.put("networkHealth", assessNetworkHealth());
            }

            result.put("timestamp", Instant.now().toString());

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error getting network status: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to get network status: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<ActionResult> executeAsync(Map<String, Object> parameters, ExecutionContext context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(parameters, context);
            } catch (ActionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void initialize(ExecutionContext context) {
        logger.debug("GetNetworkStatusAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetNetworkStatusAction cleaned up");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private String determineOverallStatus() {
        try {
            // Check if we have any active network interfaces
            boolean hasActiveInterface = false;
            boolean hasInternetConnectivity = false;

            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isUp() && !ni.isLoopback()) {
                    hasActiveInterface = true;
                    break;
                }
            }

            // Test basic internet connectivity
            try {
                InetAddress.getByName("8.8.8.8").isReachable(3000);
                hasInternetConnectivity = true;
            } catch (Exception e) {
                // Internet connectivity test failed
            }

            if (!hasActiveInterface) {
                return "DISCONNECTED";
            } else if (!hasInternetConnectivity) {
                return "LOCAL_ONLY";
            } else {
                return "CONNECTED";
            }

        } catch (Exception e) {
            logger.warn("Error determining network status: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    private List<Map<String, Object>> getNetworkInterfaces() {
        List<Map<String, Object>> interfaces = new ArrayList<>();

        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                Map<String, Object> interfaceInfo = new HashMap<>();
                interfaceInfo.put("name", ni.getName());
                interfaceInfo.put("displayName", ni.getDisplayName());
                interfaceInfo.put("isUp", ni.isUp());
                interfaceInfo.put("isLoopback", ni.isLoopback());
                interfaceInfo.put("isPointToPoint", ni.isPointToPoint());
                interfaceInfo.put("isVirtual", ni.isVirtual());
                interfaceInfo.put("mtu", ni.getMTU());

                // Get interface addresses
                List<Map<String, Object>> addresses = new ArrayList<>();
                ni.getInterfaceAddresses().forEach(addr -> {
                    Map<String, Object> addressInfo = new HashMap<>();
                    addressInfo.put("address", addr.getAddress().getHostAddress());
                    addressInfo.put("broadcast",
                            addr.getBroadcast() != null ? addr.getBroadcast().getHostAddress() : "");
                    addressInfo.put("prefixLength", addr.getNetworkPrefixLength());
                    addresses.add(addressInfo);
                });
                interfaceInfo.put("addresses", addresses);

                interfaces.add(interfaceInfo);
            }
        } catch (SocketException e) {
            logger.warn("Error getting network interfaces: {}", e.getMessage());
        }

        return interfaces;
    }

    private Map<String, Object> testConnectivity(int timeout) {
        Map<String, Object> connectivity = new HashMap<>();
        List<String> testHosts = List.of("8.8.8.8", "1.1.1.1", "208.67.222.222");

        Map<String, Boolean> results = new HashMap<>();
        int successfulTests = 0;

        for (String host : testHosts) {
            try {
                boolean reachable = InetAddress.getByName(host).isReachable(timeout);
                results.put(host, reachable);
                if (reachable) {
                    successfulTests++;
                }
            } catch (Exception e) {
                results.put(host, false);
                logger.debug("Connectivity test failed for {}: {}", host, e.getMessage());
            }
        }

        connectivity.put("testResults", results);
        connectivity.put("successfulTests", successfulTests);
        connectivity.put("totalTests", testHosts.size());
        connectivity.put("successRate", (double) successfulTests / testHosts.size());

        return connectivity;
    }

    private Map<String, Object> assessNetworkHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Count active interfaces
            int activeInterfaces = 0;
            int totalInterfaces = 0;

            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                totalInterfaces++;
                if (ni.isUp() && !ni.isLoopback()) {
                    activeInterfaces++;
                }
            }

            health.put("activeInterfaces", activeInterfaces);
            health.put("totalInterfaces", totalInterfaces);
            health.put("interfaceHealth", activeInterfaces > 0 ? "GOOD" : "POOR");

            // Basic health assessment
            if (activeInterfaces == 0) {
                health.put("overallHealth", "CRITICAL");
                health.put("issues", List.of("No active network interfaces"));
            } else if (activeInterfaces < 2) {
                health.put("overallHealth", "WARNING");
                health.put("issues", List.of("Limited network interface availability"));
            } else {
                health.put("overallHealth", "GOOD");
                health.put("issues", List.of());
            }

        } catch (Exception e) {
            health.put("overallHealth", "UNKNOWN");
            health.put("issues", List.of("Unable to assess network health: " + e.getMessage()));
        }

        return health;
    }
}
