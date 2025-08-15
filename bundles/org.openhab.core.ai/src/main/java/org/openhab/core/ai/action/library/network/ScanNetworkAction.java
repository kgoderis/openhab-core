package org.openhab.core.ai.action.library.network;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionContext;
import org.openhab.core.ai.action.api.ActionException;
import org.openhab.core.ai.action.api.ActionMetadata;
import org.openhab.core.ai.action.api.ActionResult;
import org.openhab.core.ai.action.api.ActionValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to scan the local network for devices and services
 * 
 * @author openHAB
 */
@NonNullByDefault
public class ScanNetworkAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ScanNetworkAction.class);

    private static final String ACTION_ID = "scan_network";
    private static final String ACTION_NAME = "Scan Network";
    private static final String DESCRIPTION = "Scans the local network for devices and services";
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
        properties.put("networkRange", Map.of("type", "string", "description",
                "Network range to scan (e.g., 192.168.1.0/24)", "default", "192.168.1.0/24"));
        properties.put("ports", Map.of("type", "array", "description", "Ports to scan for services", "default",
                List.of(22, 23, 80, 443, 8080, 8081)));
        properties.put("timeout", Map.of("type", "integer", "description",
                "Timeout in milliseconds for each connection", "default", 1000));
        properties.put("maxHosts",
                Map.of("type", "integer", "description", "Maximum number of hosts to scan", "default", 254));
        properties.put("includeHostname",
                Map.of("type", "boolean", "description", "Try to resolve hostnames", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of());

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("networkRange", Map.of("type", "string", "description", "Network range that was scanned"));
        properties.put("discoveredHosts", Map.of("type", "array", "description", "List of discovered hosts"));
        properties.put("openPorts", Map.of("type", "array", "description", "List of open ports found"));
        properties.put("summary", Map.of("type", "object", "description", "Scan summary statistics"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the scan"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("network_scanning", true);
        capabilities.put("port_scanning", true);
        capabilities.put("host_discovery", true);
        capabilities.put("service_detection", true);
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
                if (timeout < 100 || timeout > 10000) {
                    errors.add("Timeout must be between 100 and 10000 milliseconds");
                }
            }
        }

        // Validate maxHosts
        Object maxHostsObj = parameters.get("maxHosts");
        if (maxHostsObj != null) {
            if (!(maxHostsObj instanceof Integer)) {
                errors.add("maxHosts must be an integer");
            } else {
                int maxHosts = (Integer) maxHostsObj;
                if (maxHosts < 1 || maxHosts > 1000) {
                    errors.add("maxHosts must be between 1 and 1000");
                }
            }
        }

        // Validate boolean parameters
        validateBooleanParameter(parameters, "includeHostname", errors);

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
        logger.debug("Executing ScanNetworkAction with context: {}", context.getProtocol());
        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters with defaults
            String networkRange = (String) parameters.getOrDefault("networkRange", "192.168.1.0/24");
            @SuppressWarnings("unchecked")
            List<Integer> ports = (List<Integer>) parameters.getOrDefault("ports",
                    List.of(22, 23, 80, 443, 8080, 8081));
            int timeout = (Integer) parameters.getOrDefault("timeout", 1000);
            int maxHosts = (Integer) parameters.getOrDefault("maxHosts", 254);
            boolean includeHostname = (Boolean) parameters.getOrDefault("includeHostname", true);

            Map<String, Object> result = new HashMap<>();
            result.put("networkRange", networkRange);

            // Generate list of hosts to scan
            List<String> hosts = generateHostList(networkRange, maxHosts);

            // Scan for hosts
            List<Map<String, Object>> discoveredHosts = scanForHosts(hosts, timeout, includeHostname);
            result.put("discoveredHosts", discoveredHosts);

            // Scan for open ports on discovered hosts
            List<Map<String, Object>> openPorts = scanForOpenPorts(discoveredHosts, ports, timeout);
            result.put("openPorts", openPorts);

            // Generate summary
            result.put("summary", generateScanSummary(discoveredHosts, openPorts, hosts.size()));
            result.put("timestamp", java.time.Instant.now().toString());

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error scanning network: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to scan network: " + e.getMessage(), e);
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
        logger.debug("ScanNetworkAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("ScanNetworkAction cleaned up");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private List<String> generateHostList(String networkRange, int maxHosts) {
        List<String> hosts = new ArrayList<>();

        try {
            // Parse network range (simplified - assumes /24 for now)
            String baseIP = networkRange.split("/")[0];
            String[] parts = baseIP.split("\\.");

            if (parts.length == 4) {
                String base = parts[0] + "." + parts[1] + "." + parts[2] + ".";
                int start = 1;
                int end = Math.min(254, start + maxHosts - 1);

                for (int i = start; i <= end; i++) {
                    hosts.add(base + i);
                }
            }
        } catch (Exception e) {
            logger.warn("Error parsing network range {}: {}", networkRange, e.getMessage());
            // Fallback to default range
            for (int i = 1; i <= Math.min(maxHosts, 254); i++) {
                hosts.add("192.168.1." + i);
            }
        }

        return hosts;
    }

    private List<Map<String, Object>> scanForHosts(List<String> hosts, int timeout, boolean includeHostname) {
        List<Map<String, Object>> discoveredHosts = new ArrayList<>();

        for (String host : hosts) {
            try {
                if (InetAddress.getByName(host).isReachable(timeout)) {
                    Map<String, Object> hostInfo = new HashMap<>();
                    hostInfo.put("ip", host);
                    hostInfo.put("status", "ONLINE");

                    if (includeHostname) {
                        try {
                            InetAddress address = InetAddress.getByName(host);
                            String hostname = address.getCanonicalHostName();
                            if (!hostname.equals(host)) {
                                hostInfo.put("hostname", hostname);
                            }
                        } catch (Exception e) {
                            logger.debug("Could not resolve hostname for {}: {}", host, e.getMessage());
                        }
                    }

                    discoveredHosts.add(hostInfo);
                }
            } catch (Exception e) {
                logger.debug("Host {} is not reachable: {}", host, e.getMessage());
            }
        }

        return discoveredHosts;
    }

    private List<Map<String, Object>> scanForOpenPorts(List<Map<String, Object>> hosts, List<Integer> ports,
            int timeout) {
        List<Map<String, Object>> openPorts = new ArrayList<>();

        for (Map<String, Object> host : hosts) {
            String hostIP = (String) host.get("ip");

            for (Integer port : ports) {
                try (Socket socket = new Socket()) {
                    socket.connect(new java.net.InetSocketAddress(hostIP, port), timeout);

                    Map<String, Object> portInfo = new HashMap<>();
                    portInfo.put("host", hostIP);
                    portInfo.put("port", port);
                    portInfo.put("status", "OPEN");
                    portInfo.put("service", getServiceName(port));

                    openPorts.add(portInfo);

                } catch (Exception e) {
                    // Port is closed or filtered
                    logger.debug("Port {} on {} is closed: {}", port, hostIP, e.getMessage());
                }
            }
        }

        return openPorts;
    }

    private String getServiceName(int port) {
        switch (port) {
            case 22:
                return "SSH";
            case 23:
                return "Telnet";
            case 25:
                return "SMTP";
            case 53:
                return "DNS";
            case 80:
                return "HTTP";
            case 110:
                return "POP3";
            case 143:
                return "IMAP";
            case 443:
                return "HTTPS";
            case 993:
                return "IMAPS";
            case 995:
                return "POP3S";
            case 8080:
                return "HTTP-Alt";
            case 8081:
                return "HTTP-Alt";
            case 8443:
                return "HTTPS-Alt";
            default:
                return "Unknown";
        }
    }

    private Map<String, Object> generateScanSummary(List<Map<String, Object>> discoveredHosts,
            List<Map<String, Object>> openPorts, int totalHosts) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalHostsScanned", totalHosts);
        summary.put("discoveredHosts", discoveredHosts.size());
        summary.put("openPortsFound", openPorts.size());
        summary.put("discoveryRate", (double) discoveredHosts.size() / totalHosts);

        // Count services by type
        Map<String, Integer> serviceCounts = new HashMap<>();
        for (Map<String, Object> port : openPorts) {
            String service = (String) port.get("service");
            serviceCounts.put(service, serviceCounts.getOrDefault(service, 0) + 1);
        }
        summary.put("serviceBreakdown", serviceCounts);

        // Most common open ports
        Map<Integer, Integer> portCounts = new HashMap<>();
        for (Map<String, Object> port : openPorts) {
            Integer portNum = (Integer) port.get("port");
            portCounts.put(portNum, portCounts.getOrDefault(portNum, 0) + 1);
        }
        summary.put("portBreakdown", portCounts);

        return summary;
    }
}
