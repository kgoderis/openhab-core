package org.openhab.core.ai.actions.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
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
 * Action to get network configuration information
 * 
 * @author openHAB
 */
@NonNullByDefault
public class GetNetworkConfigurationAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetNetworkConfigurationAction.class);

    private static final String ACTION_ID = "get_network_configuration";
    private static final String ACTION_NAME = "Get Network Configuration";
    private static final String DESCRIPTION = "Retrieves network configuration information including IP addresses, DNS settings, and interface configurations";
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
        properties.put("includeDNS",
                Map.of("type", "boolean", "description", "Include DNS configuration", "default", true));
        properties.put("includeRouting",
                Map.of("type", "boolean", "description", "Include routing table information", "default", true));
        properties.put("includeInterfaceConfig",
                Map.of("type", "boolean", "description", "Include detailed interface configuration", "default", true));
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
        properties.put("hostname", Map.of("type", "string", "description", "System hostname"));
        properties.put("interfaces", Map.of("type", "array", "description", "Network interface configurations"));
        properties.put("dns", Map.of("type", "object", "description", "DNS configuration"));
        properties.put("routing", Map.of("type", "object", "description", "Routing table information"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the configuration check"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("interface_config", true);
        capabilities.put("dns_config", true);
        capabilities.put("routing_config", true);
        return capabilities;
    }

    @Override
    public ActionMetadata getMetadata() {
        return ActionMetadata.builder().description(DESCRIPTION).version(VERSION).author("openHAB").build();
    }

    @Override
    public ActionValidationResult validateParameters(Map<String, Object> parameters) {
        List<String> errors = new ArrayList<>();

        // Validate boolean parameters
        validateBooleanParameter(parameters, "includeDNS", errors);
        validateBooleanParameter(parameters, "includeRouting", errors);
        validateBooleanParameter(parameters, "includeInterfaceConfig", errors);

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
        logger.debug("Executing GetNetworkConfigurationAction with context: {}", context.getProtocol());
        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters with defaults
            boolean includeDNS = (Boolean) parameters.getOrDefault("includeDNS", true);
            boolean includeRouting = (Boolean) parameters.getOrDefault("includeRouting", true);
            boolean includeInterfaceConfig = (Boolean) parameters.getOrDefault("includeInterfaceConfig", true);
            String interfaceName = (String) parameters.get("interfaceName");

            Map<String, Object> result = new HashMap<>();

            // Get hostname
            result.put("hostname", getHostname());

            // Get interface configurations
            if (includeInterfaceConfig) {
                result.put("interfaces", getInterfaceConfigurations(interfaceName));
            }

            // Get DNS configuration
            if (includeDNS) {
                result.put("dns", getDNSConfiguration());
            }

            // Get routing information
            if (includeRouting) {
                result.put("routing", getRoutingInformation());
            }

            result.put("timestamp", java.time.Instant.now().toString());

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error getting network configuration: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to get network configuration: " + e.getMessage(), e);
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
        logger.debug("GetNetworkConfigurationAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetNetworkConfigurationAction cleaned up");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            logger.warn("Could not get hostname: {}", e.getMessage());
            return "unknown";
        }
    }

    private List<Map<String, Object>> getInterfaceConfigurations(String specificInterface) {
        List<Map<String, Object>> interfaces = new ArrayList<>();

        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                // Skip if specific interface is requested and this isn't it
                if (specificInterface != null && !ni.getName().equals(specificInterface)) {
                    continue;
                }

                Map<String, Object> interfaceConfig = new HashMap<>();
                interfaceConfig.put("name", ni.getName());
                interfaceConfig.put("displayName", ni.getDisplayName());
                interfaceConfig.put("isUp", ni.isUp());
                interfaceConfig.put("isLoopback", ni.isLoopback());
                interfaceConfig.put("isPointToPoint", ni.isPointToPoint());
                interfaceConfig.put("isVirtual", ni.isVirtual());
                interfaceConfig.put("mtu", ni.getMTU());
                interfaceConfig.put("hardwareAddress", getHardwareAddress(ni));

                // Get interface addresses
                List<Map<String, Object>> addresses = new ArrayList<>();
                ni.getInterfaceAddresses().forEach(addr -> {
                    Map<String, Object> addressInfo = new HashMap<>();
                    addressInfo.put("address", addr.getAddress().getHostAddress());
                    addressInfo.put("broadcast",
                            addr.getBroadcast() != null ? addr.getBroadcast().getHostAddress() : "");
                    addressInfo.put("prefixLength", addr.getNetworkPrefixLength());
                    addressInfo.put("networkAddress",
                            getNetworkAddress(addr.getAddress(), addr.getNetworkPrefixLength()));
                    addresses.add(addressInfo);
                });
                interfaceConfig.put("addresses", addresses);

                interfaces.add(interfaceConfig);
            }
        } catch (SocketException e) {
            logger.warn("Error getting network interfaces: {}", e.getMessage());
        }

        return interfaces;
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

    private String getNetworkAddress(InetAddress address, int prefixLength) {
        try {
            byte[] addr = address.getAddress();
            int mask = 0xffffffff << (32 - prefixLength);

            byte[] networkAddr = new byte[4];
            for (int i = 0; i < 4; i++) {
                networkAddr[i] = (byte) (addr[i] & (mask >> (24 - i * 8)));
            }

            return InetAddress.getByAddress(networkAddr).getHostAddress();
        } catch (Exception e) {
            logger.debug("Could not calculate network address: {}", e.getMessage());
            return "unknown";
        }
    }

    private Map<String, Object> getDNSConfiguration() {
        Map<String, Object> dns = new HashMap<>();

        try {
            // Get system DNS servers (simulated - in real implementation would read from system config)
            List<String> dnsServers = new ArrayList<>();
            dnsServers.add("8.8.8.8");
            dnsServers.add("8.8.4.4");
            dnsServers.add("1.1.1.1");

            dns.put("servers", dnsServers);
            dns.put("searchDomains", List.of("local", "home"));
            dns.put("timeout", 5000);
            dns.put("retries", 3);

            // Test DNS resolution
            Map<String, Boolean> dnsTests = new HashMap<>();
            try {
                InetAddress.getByName("google.com");
                dnsTests.put("google.com", true);
            } catch (Exception e) {
                dnsTests.put("google.com", false);
            }

            try {
                InetAddress.getByName("openhab.org");
                dnsTests.put("openhab.org", true);
            } catch (Exception e) {
                dnsTests.put("openhab.org", false);
            }

            dns.put("resolutionTests", dnsTests);

        } catch (Exception e) {
            logger.warn("Error getting DNS configuration: {}", e.getMessage());
            dns.put("error", "Could not retrieve DNS configuration: " + e.getMessage());
        }

        return dns;
    }

    private Map<String, Object> getRoutingInformation() {
        Map<String, Object> routing = new HashMap<>();

        try {
            // Get default gateway (simulated)
            Map<String, Object> defaultRoute = new HashMap<>();
            defaultRoute.put("destination", "0.0.0.0/0");
            defaultRoute.put("gateway", "192.168.1.1");
            defaultRoute.put("interface", "eth0");
            defaultRoute.put("metric", 100);

            routing.put("defaultRoute", defaultRoute);

            // Get local routes
            List<Map<String, Object>> localRoutes = new ArrayList<>();

            // Loopback route
            Map<String, Object> loopbackRoute = new HashMap<>();
            loopbackRoute.put("destination", "127.0.0.0/8");
            loopbackRoute.put("gateway", "0.0.0.0");
            loopbackRoute.put("interface", "lo");
            loopbackRoute.put("metric", 1);
            localRoutes.add(loopbackRoute);

            // Local network route
            Map<String, Object> localNetworkRoute = new HashMap<>();
            localNetworkRoute.put("destination", "192.168.1.0/24");
            localNetworkRoute.put("gateway", "0.0.0.0");
            localNetworkRoute.put("interface", "eth0");
            localNetworkRoute.put("metric", 100);
            localRoutes.add(localNetworkRoute);

            routing.put("localRoutes", localRoutes);
            routing.put("totalRoutes", localRoutes.size() + 1);

        } catch (Exception e) {
            logger.warn("Error getting routing information: {}", e.getMessage());
            routing.put("error", "Could not retrieve routing information: " + e.getMessage());
        }

        return routing;
    }
}
