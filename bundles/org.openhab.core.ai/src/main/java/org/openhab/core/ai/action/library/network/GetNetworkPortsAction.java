package org.openhab.core.ai.action.library.network;

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
 * Action to get information about network ports and services
 * 
 * @author openHAB
 */
@NonNullByDefault
public class GetNetworkPortsAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetNetworkPortsAction.class);

    private static final String ACTION_ID = "get_network_ports";
    private static final String ACTION_NAME = "Get Network Ports";
    private static final String DESCRIPTION = "Retrieves information about network ports and services";
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
        properties.put("host", Map.of("type", "string", "description", "Host to check ports on (default: localhost)",
                "default", "localhost"));
        properties.put("ports", Map.of("type", "array", "description", "List of ports to check", "default",
                List.of(22, 23, 25, 53, 80, 110, 143, 443, 993, 995, 8080, 8081, 8443)));
        properties.put("timeout", Map.of("type", "integer", "description",
                "Timeout in milliseconds for each port check", "default", 1000));
        properties.put("includeServiceInfo",
                Map.of("type", "boolean", "description", "Include service information for ports", "default", true));
        properties.put("includeWellKnownPorts",
                Map.of("type", "boolean", "description", "Include well-known port information", "default", true));

        schema.put("properties", properties);
        schema.put("required", List.of());

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("host", Map.of("type", "string", "description", "Host that was checked"));
        properties.put("openPorts", Map.of("type", "array", "description", "List of open ports"));
        properties.put("closedPorts", Map.of("type", "array", "description", "List of closed ports"));
        properties.put("serviceInfo",
                Map.of("type", "object", "description", "Service information for well-known ports"));
        properties.put("summary", Map.of("type", "object", "description", "Port scan summary"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the port check"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("port_scanning", true);
        capabilities.put("service_detection", true);
        capabilities.put("well_known_ports", true);
        capabilities.put("port_status", true);
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

        // Validate boolean parameters
        validateBooleanParameter(parameters, "includeServiceInfo", errors);
        validateBooleanParameter(parameters, "includeWellKnownPorts", errors);

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
        logger.debug("Executing GetNetworkPortsAction with context: {}", context.getProtocol());
        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters with defaults
            String host = (String) parameters.getOrDefault("host", "localhost");
            @SuppressWarnings("unchecked")
            List<Integer> ports = (List<Integer>) parameters.getOrDefault("ports",
                    List.of(22, 23, 25, 53, 80, 110, 143, 443, 993, 995, 8080, 8081, 8443));
            int timeout = (Integer) parameters.getOrDefault("timeout", 1000);
            boolean includeServiceInfo = (Boolean) parameters.getOrDefault("includeServiceInfo", true);
            boolean includeWellKnownPorts = (Boolean) parameters.getOrDefault("includeWellKnownPorts", true);

            Map<String, Object> result = new HashMap<>();
            result.put("host", host);

            // Check ports
            List<Map<String, Object>> openPorts = new ArrayList<>();
            List<Map<String, Object>> closedPorts = new ArrayList<>();

            for (Integer port : ports) {
                Map<String, Object> portInfo = checkPort(host, port, timeout, includeServiceInfo);

                if ((Boolean) portInfo.get("open")) {
                    openPorts.add(portInfo);
                } else {
                    closedPorts.add(portInfo);
                }
            }

            result.put("openPorts", openPorts);
            result.put("closedPorts", closedPorts);

            // Add service information
            if (includeServiceInfo) {
                result.put("serviceInfo", getServiceInfo(ports));
            }

            // Add well-known ports information
            if (includeWellKnownPorts) {
                result.put("wellKnownPorts", getWellKnownPorts());
            }

            // Generate summary
            result.put("summary", generatePortSummary(openPorts, closedPorts));
            result.put("timestamp", java.time.Instant.now().toString());

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error getting network ports: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to get network ports: " + e.getMessage(), e);
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
        logger.debug("GetNetworkPortsAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetNetworkPortsAction cleaned up");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private Map<String, Object> checkPort(String host, int port, int timeout, boolean includeServiceInfo) {
        Map<String, Object> portInfo = new HashMap<>();
        portInfo.put("port", port);
        portInfo.put("host", host);

        try (Socket socket = new Socket()) {
            long startTime = System.currentTimeMillis();
            socket.connect(new java.net.InetSocketAddress(host, port), timeout);
            long endTime = System.currentTimeMillis();

            portInfo.put("open", true);
            portInfo.put("status", "OPEN");
            portInfo.put("responseTime", endTime - startTime);

            if (includeServiceInfo) {
                portInfo.put("service", getServiceName(port));
                portInfo.put("protocol", getProtocol(port));
            }

        } catch (Exception e) {
            portInfo.put("open", false);
            portInfo.put("status", "CLOSED");
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
            portInfo.put("error", errorMessage);

            if (includeServiceInfo) {
                portInfo.put("service", getServiceName(port));
                portInfo.put("protocol", getProtocol(port));
            }
        }

        return portInfo;
    }

    private String getServiceName(int port) {
        switch (port) {
            case 20:
                return "FTP-DATA";
            case 21:
                return "FTP";
            case 22:
                return "SSH";
            case 23:
                return "Telnet";
            case 25:
                return "SMTP";
            case 53:
                return "DNS";
            case 67:
                return "DHCP-Server";
            case 68:
                return "DHCP-Client";
            case 69:
                return "TFTP";
            case 80:
                return "HTTP";
            case 110:
                return "POP3";
            case 123:
                return "NTP";
            case 143:
                return "IMAP";
            case 161:
                return "SNMP";
            case 162:
                return "SNMP-TRAP";
            case 389:
                return "LDAP";
            case 443:
                return "HTTPS";
            case 465:
                return "SMTPS";
            case 514:
                return "Syslog";
            case 515:
                return "LPR";
            case 587:
                return "SMTP-Submission";
            case 631:
                return "IPP";
            case 636:
                return "LDAPS";
            case 993:
                return "IMAPS";
            case 995:
                return "POP3S";
            case 1433:
                return "MSSQL";
            case 1521:
                return "Oracle";
            case 3306:
                return "MySQL";
            case 3389:
                return "RDP";
            case 5432:
                return "PostgreSQL";
            case 5900:
                return "VNC";
            case 6379:
                return "Redis";
            case 8080:
                return "HTTP-Alt";
            case 8081:
                return "HTTP-Alt";
            case 8443:
                return "HTTPS-Alt";
            case 9000:
                return "Jenkins";
            case 27017:
                return "MongoDB";
            default:
                return "Unknown";
        }
    }

    private String getProtocol(int port) {
        switch (port) {
            case 20:
            case 21:
            case 22:
            case 23:
            case 25:
            case 110:
            case 143:
            case 465:
            case 587:
            case 993:
            case 995:
                return "TCP";
            case 53:
            case 67:
            case 68:
            case 69:
            case 123:
            case 161:
            case 162:
            case 514:
                return "UDP";
            case 80:
            case 443:
            case 389:
            case 636:
            case 1433:
            case 1521:
            case 3306:
            case 3389:
            case 5432:
            case 5900:
            case 6379:
            case 8080:
            case 8081:
            case 8443:
            case 9000:
            case 27017:
                return "TCP";
            default:
                return "TCP/UDP";
        }
    }

    private Map<String, Object> getServiceInfo(List<Integer> ports) {
        Map<String, Object> serviceInfo = new HashMap<>();

        for (Integer port : ports) {
            Map<String, Object> info = new HashMap<>();
            info.put("service", getServiceName(port));
            info.put("protocol", getProtocol(port));
            info.put("description", getServiceDescription(port));
            serviceInfo.put(port.toString(), info);
        }

        return serviceInfo;
    }

    private String getServiceDescription(int port) {
        switch (port) {
            case 22:
                return "Secure Shell - encrypted remote access";
            case 23:
                return "Telnet - unencrypted remote access";
            case 25:
                return "Simple Mail Transfer Protocol";
            case 53:
                return "Domain Name System";
            case 80:
                return "Hypertext Transfer Protocol";
            case 110:
                return "Post Office Protocol version 3";
            case 143:
                return "Internet Message Access Protocol";
            case 443:
                return "HTTP Secure - encrypted web traffic";
            case 993:
                return "IMAP over SSL";
            case 995:
                return "POP3 over SSL";
            case 8080:
                return "Alternative HTTP port";
            case 8081:
                return "Alternative HTTP port";
            case 8443:
                return "Alternative HTTPS port";
            default:
                return "Network service";
        }
    }

    private Map<String, Object> getWellKnownPorts() {
        Map<String, Object> wellKnownPorts = new HashMap<>();

        // Common well-known ports
        wellKnownPorts.put("system", Map.of("22", "SSH", "23", "Telnet", "25", "SMTP", "53", "DNS", "80", "HTTP", "110",
                "POP3", "143", "IMAP", "443", "HTTPS"));

        wellKnownPorts.put("databases", Map.of("1433", "MSSQL", "1521", "Oracle", "3306", "MySQL", "5432", "PostgreSQL",
                "27017", "MongoDB", "6379", "Redis"));

        wellKnownPorts.put("development",
                Map.of("8080", "HTTP-Alt", "8081", "HTTP-Alt", "8443", "HTTPS-Alt", "9000", "Jenkins", "5900", "VNC"));

        return wellKnownPorts;
    }

    private Map<String, Object> generatePortSummary(List<Map<String, Object>> openPorts,
            List<Map<String, Object>> closedPorts) {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalPorts", openPorts.size() + closedPorts.size());
        summary.put("openPorts", openPorts.size());
        summary.put("closedPorts", closedPorts.size());
        summary.put("openPortPercentage", (double) openPorts.size() / (openPorts.size() + closedPorts.size()) * 100);

        // Count services by type
        Map<String, Integer> serviceCounts = new HashMap<>();
        for (Map<String, Object> port : openPorts) {
            String service = (String) port.get("service");
            serviceCounts.put(service, serviceCounts.getOrDefault(service, 0) + 1);
        }
        summary.put("serviceBreakdown", serviceCounts);

        return summary;
    }
}
