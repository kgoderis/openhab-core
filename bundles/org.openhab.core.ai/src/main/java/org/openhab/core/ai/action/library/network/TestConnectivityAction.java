package org.openhab.core.ai.action.library.network;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.ActionContext;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.ActionValidationResult;
import org.openhab.core.ai.api.action.Action;
import org.openhab.core.ai.api.action.ActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to test network connectivity to various hosts and services
 * 
 * @author openHAB
 */
@NonNullByDefault
public class TestConnectivityAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(TestConnectivityAction.class);

    private static final String ACTION_ID = "test_connectivity";
    private static final String ACTION_NAME = "Test Connectivity";
    private static final String DESCRIPTION = "Tests network connectivity to specified hosts and services";
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
        properties.put("hosts", Map.of("type", "array", "description", "List of hosts to test connectivity to",
                "default", List.of("8.8.8.8", "1.1.1.1", "google.com")));
        properties.put("ports",
                Map.of("type", "array", "description", "List of ports to test", "default", List.of(80, 443, 8080)));
        properties.put("timeout",
                Map.of("type", "integer", "description", "Timeout in milliseconds for each test", "default", 5000));
        properties.put("includeLatency",
                Map.of("type", "boolean", "description", "Include latency measurements", "default", true));
        properties.put("includeDNS",
                Map.of("type", "boolean", "description", "Include DNS resolution tests", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of());

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("overallStatus", Map.of("type", "string", "description", "Overall connectivity status"));
        properties.put("hostTests", Map.of("type", "array", "description", "Results of host connectivity tests"));
        properties.put("portTests", Map.of("type", "array", "description", "Results of port connectivity tests"));
        properties.put("dnsTests", Map.of("type", "array", "description", "Results of DNS resolution tests"));
        properties.put("summary", Map.of("type", "object", "description", "Summary statistics"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the connectivity test"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("host_testing", true);
        capabilities.put("port_testing", true);
        capabilities.put("dns_testing", true);
        capabilities.put("latency_measurement", true);
        return capabilities;
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().description(DESCRIPTION).version(VERSION).author("openHAB").build();
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate timeout
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
        validateBooleanParameter(parameters, "includeLatency", errors);
        validateBooleanParameter(parameters, "includeDNS", errors);

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
    public ActionResult execute(Map<String, Object> parameters, ActionContext context) throws ActionException {
        logger.debug("Executing TestConnectivityAction with context: {}", context.getProtocol());
        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters with defaults
            @SuppressWarnings("unchecked")
            List<String> hosts = (List<String>) parameters.getOrDefault("hosts",
                    List.of("8.8.8.8", "1.1.1.1", "google.com"));
            @SuppressWarnings("unchecked")
            List<Integer> ports = (List<Integer>) parameters.getOrDefault("ports", List.of(80, 443, 8080));
            int timeout = (Integer) parameters.getOrDefault("timeout", 5000);
            boolean includeLatency = (Boolean) parameters.getOrDefault("includeLatency", true);
            boolean includeDNS = (Boolean) parameters.getOrDefault("includeDNS", true);

            Map<String, Object> result = new HashMap<>();

            // Test host connectivity
            List<Map<String, Object>> hostTests = testHostConnectivity(hosts, timeout, includeLatency);
            result.put("hostTests", hostTests);

            // Test port connectivity
            List<Map<String, Object>> portTests = testPortConnectivity(hosts, ports, timeout);
            result.put("portTests", portTests);

            // Test DNS resolution
            if (includeDNS) {
                List<Map<String, Object>> dnsTests = testDNSResolution(hosts);
                result.put("dnsTests", dnsTests);
            }

            // Generate summary
            result.put("summary", generateSummary(hostTests, portTests,
                    includeDNS ? (List<Map<String, Object>>) result.get("dnsTests") : List.of()));
            result.put("overallStatus", determineOverallStatus(hostTests, portTests));
            result.put("timestamp", java.time.Instant.now().toString());

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error testing connectivity: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to test connectivity: " + e.getMessage(), e);
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
    public void initialize(ActionContext context) {
        logger.debug("TestConnectivityAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("TestConnectivityAction cleaned up");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private List<Map<String, Object>> testHostConnectivity(List<String> hosts, int timeout, boolean includeLatency) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (String host : hosts) {
            Map<String, Object> testResult = new HashMap<>();
            testResult.put("host", host);

            try {
                long startTime = System.currentTimeMillis();
                boolean reachable = InetAddress.getByName(host).isReachable(timeout);
                long endTime = System.currentTimeMillis();

                testResult.put("reachable", reachable);
                testResult.put("status", reachable ? "SUCCESS" : "FAILED");

                if (includeLatency && reachable) {
                    testResult.put("latency", endTime - startTime);
                }

                if (reachable) {
                    InetAddress address = InetAddress.getByName(host);
                    testResult.put("resolvedAddress", address.getHostAddress());
                    testResult.put("canonicalHostname", address.getCanonicalHostName());
                }

            } catch (Exception e) {
                testResult.put("reachable", false);
                testResult.put("status", "ERROR");
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
                testResult.put("error", errorMessage);
                logger.debug("Connectivity test failed for {}: {}", host, errorMessage);
            }

            results.add(testResult);
        }

        return results;
    }

    private List<Map<String, Object>> testPortConnectivity(List<String> hosts, List<Integer> ports, int timeout) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (String host : hosts) {
            for (Integer port : ports) {
                Map<String, Object> testResult = new HashMap<>();
                testResult.put("host", host);
                testResult.put("port", port);

                try (Socket socket = new Socket()) {
                    long startTime = System.currentTimeMillis();
                    socket.connect(new java.net.InetSocketAddress(host, port), timeout);
                    long endTime = System.currentTimeMillis();

                    testResult.put("reachable", true);
                    testResult.put("status", "SUCCESS");
                    testResult.put("latency", endTime - startTime);

                } catch (Exception e) {
                    testResult.put("reachable", false);
                    testResult.put("status", "FAILED");
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    testResult.put("error", errorMessage);
                    logger.debug("Port test failed for {}:{} - {}", host, port, errorMessage);
                }

                results.add(testResult);
            }
        }

        return results;
    }

    private List<Map<String, Object>> testDNSResolution(List<String> hosts) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (String host : hosts) {
            Map<String, Object> testResult = new HashMap<>();
            testResult.put("host", host);

            try {
                long startTime = System.currentTimeMillis();
                InetAddress address = InetAddress.getByName(host);
                long endTime = System.currentTimeMillis();

                testResult.put("resolved", true);
                testResult.put("status", "SUCCESS");
                testResult.put("ipAddress", address.getHostAddress());
                testResult.put("canonicalHostname", address.getCanonicalHostName());
                testResult.put("resolutionTime", endTime - startTime);

            } catch (Exception e) {
                testResult.put("resolved", false);
                testResult.put("status", "FAILED");
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
                testResult.put("error", errorMessage);
                logger.debug("DNS resolution failed for {}: {}", host, errorMessage);
            }

            results.add(testResult);
        }

        return results;
    }

    private Map<String, Object> generateSummary(List<Map<String, Object>> hostTests,
            List<Map<String, Object>> portTests, List<Map<String, Object>> dnsTests) {
        Map<String, Object> summary = new HashMap<>();

        // Host connectivity summary
        long successfulHosts = hostTests.stream().mapToLong(test -> (Boolean) test.get("reachable") ? 1 : 0).sum();
        summary.put("hostTests", Map.of("total", hostTests.size(), "successful", successfulHosts, "failed",
                hostTests.size() - successfulHosts, "successRate", (double) successfulHosts / hostTests.size()));

        // Port connectivity summary
        long successfulPorts = portTests.stream().mapToLong(test -> (Boolean) test.get("reachable") ? 1 : 0).sum();
        summary.put("portTests", Map.of("total", portTests.size(), "successful", successfulPorts, "failed",
                portTests.size() - successfulPorts, "successRate", (double) successfulPorts / portTests.size()));

        // DNS resolution summary
        if (dnsTests != null) {
            long successfulDNS = dnsTests.stream().mapToLong(test -> (Boolean) test.get("resolved") ? 1 : 0).sum();
            summary.put("dnsTests", Map.of("total", dnsTests.size(), "successful", successfulDNS, "failed",
                    dnsTests.size() - successfulDNS, "successRate", (double) successfulDNS / dnsTests.size()));
        }

        // Overall summary
        long totalTests = hostTests.size() + portTests.size() + (dnsTests != null ? dnsTests.size() : 0);
        long totalSuccessful = successfulHosts + successfulPorts
                + (dnsTests != null ? dnsTests.stream().mapToLong(test -> (Boolean) test.get("resolved") ? 1 : 0).sum()
                        : 0);

        summary.put("overall", Map.of("totalTests", totalTests, "successfulTests", totalSuccessful, "failedTests",
                totalTests - totalSuccessful, "overallSuccessRate", (double) totalSuccessful / totalTests));

        return summary;
    }

    private String determineOverallStatus(List<Map<String, Object>> hostTests, List<Map<String, Object>> portTests) {
        long successfulHosts = hostTests.stream().mapToLong(test -> (Boolean) test.get("reachable") ? 1 : 0).sum();
        long successfulPorts = portTests.stream().mapToLong(test -> (Boolean) test.get("reachable") ? 1 : 0).sum();

        double hostSuccessRate = (double) successfulHosts / hostTests.size();
        double portSuccessRate = (double) successfulPorts / portTests.size();

        if (hostSuccessRate >= 0.8 && portSuccessRate >= 0.8) {
            return "EXCELLENT";
        } else if (hostSuccessRate >= 0.6 && portSuccessRate >= 0.6) {
            return "GOOD";
        } else if (hostSuccessRate >= 0.4 && portSuccessRate >= 0.4) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }
}
