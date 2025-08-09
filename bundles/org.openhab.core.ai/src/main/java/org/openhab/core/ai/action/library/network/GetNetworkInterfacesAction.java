package org.openhab.core.ai.action.library.network;

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
import org.openhab.core.ai.action.api.Action;
import org.openhab.core.ai.action.api.ActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to get detailed information about network interfaces
 * 
 * @author openHAB
 */
@NonNullByDefault
public class GetNetworkInterfacesAction implements Action {

    private static final Logger logger = LoggerFactory.getLogger(GetNetworkInterfacesAction.class);

    private static final String ACTION_ID = "get_network_interfaces";
    private static final String ACTION_NAME = "Get Network Interfaces";
    private static final String DESCRIPTION = "Retrieves detailed information about network interfaces";
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
        properties.put("includeAddresses",
                Map.of("type", "boolean", "description", "Include interface addresses", "default", true));
        properties.put("includeSubInterfaces",
                Map.of("type", "boolean", "description", "Include sub-interfaces", "default", true));
        properties.put("includeHardwareInfo",
                Map.of("type", "boolean", "description", "Include hardware information", "default", true));
        properties.put("filterActive",
                Map.of("type", "boolean", "description", "Filter to show only active interfaces", "default", false));
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
        properties.put("interfaces", Map.of("type", "array", "description", "List of network interfaces"));
        properties.put("summary", Map.of("type", "object", "description", "Summary of interface types"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the interface information"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("interface_details", true);
        capabilities.put("address_information", true);
        capabilities.put("hardware_information", true);
        capabilities.put("sub_interface_support", true);
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
        validateBooleanParameter(parameters, "includeAddresses", errors);
        validateBooleanParameter(parameters, "includeSubInterfaces", errors);
        validateBooleanParameter(parameters, "includeHardwareInfo", errors);
        validateBooleanParameter(parameters, "filterActive", errors);

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
        logger.debug("Executing GetNetworkInterfacesAction with context: {}", context.getProtocol());
        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters with defaults
            boolean includeAddresses = (Boolean) parameters.getOrDefault("includeAddresses", true);
            boolean includeSubInterfaces = (Boolean) parameters.getOrDefault("includeSubInterfaces", true);
            boolean includeHardwareInfo = (Boolean) parameters.getOrDefault("includeHardwareInfo", true);
            boolean filterActive = (Boolean) parameters.getOrDefault("filterActive", false);
            String interfaceName = (String) parameters.get("interfaceName");

            Map<String, Object> result = new HashMap<>();

            // Get interface information
            List<Map<String, Object>> interfaces = getInterfaces(includeAddresses, includeSubInterfaces,
                    includeHardwareInfo, filterActive, interfaceName);
            result.put("interfaces", interfaces);

            // Generate summary
            result.put("summary", generateInterfaceSummary(interfaces));
            result.put("timestamp", java.time.Instant.now().toString());

            long executionTime = System.currentTimeMillis() - startTime;
            return ActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error getting network interfaces: {}", e.getMessage(), e);
            throw new ActionException(ACTION_ID, "Failed to get network interfaces: " + e.getMessage(), e);
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
        logger.debug("GetNetworkInterfacesAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetNetworkInterfacesAction cleaned up");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private List<Map<String, Object>> getInterfaces(boolean includeAddresses, boolean includeSubInterfaces,
            boolean includeHardwareInfo, boolean filterActive, String specificInterface) {
        List<Map<String, Object>> interfaces = new ArrayList<>();

        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                // Skip if specific interface is requested and this isn't it
                if (specificInterface != null && !ni.getName().equals(specificInterface)) {
                    continue;
                }

                // Skip if filtering for active interfaces and this one is not active
                if (filterActive && !ni.isUp()) {
                    continue;
                }

                Map<String, Object> interfaceInfo = new HashMap<>();
                interfaceInfo.put("name", ni.getName());
                interfaceInfo.put("displayName", ni.getDisplayName());
                interfaceInfo.put("index", ni.getIndex());
                interfaceInfo.put("isUp", ni.isUp());
                interfaceInfo.put("isLoopback", ni.isLoopback());
                interfaceInfo.put("isPointToPoint", ni.isPointToPoint());
                interfaceInfo.put("isVirtual", ni.isVirtual());
                interfaceInfo.put("supportsMulticast", ni.supportsMulticast());
                interfaceInfo.put("mtu", ni.getMTU());

                // Get hardware information
                if (includeHardwareInfo) {
                    interfaceInfo.put("hardwareAddress", getHardwareAddress(ni));
                    interfaceInfo.put("parentInterface", ni.getParent() != null ? ni.getParent().getName() : "");
                }

                // Get interface addresses
                if (includeAddresses) {
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
                    interfaceInfo.put("addresses", addresses);
                }

                // Get sub-interfaces
                if (includeSubInterfaces) {
                    List<Map<String, Object>> subInterfaces = new ArrayList<>();
                    Collections.list(ni.getSubInterfaces()).forEach(subNi -> {
                        try {
                            Map<String, Object> subInterfaceInfo = new HashMap<>();
                            subInterfaceInfo.put("name", subNi.getName());
                            subInterfaceInfo.put("displayName", subNi.getDisplayName());
                            subInterfaceInfo.put("isUp", subNi.isUp());
                            subInterfaceInfo.put("isLoopback", subNi.isLoopback());
                            subInterfaceInfo.put("isPointToPoint", subNi.isPointToPoint());
                            subInterfaceInfo.put("isVirtual", subNi.isVirtual());
                            subInterfaceInfo.put("mtu", subNi.getMTU());
                            subInterfaces.add(subInterfaceInfo);
                        } catch (SocketException e) {
                            logger.debug("Error getting sub-interface info: {}", e.getMessage());
                        }
                    });
                    interfaceInfo.put("subInterfaces", subInterfaces);
                }

                interfaces.add(interfaceInfo);
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

    private String getNetworkAddress(java.net.InetAddress address, int prefixLength) {
        try {
            byte[] addr = address.getAddress();
            int mask = 0xffffffff << (32 - prefixLength);

            byte[] networkAddr = new byte[4];
            for (int i = 0; i < 4; i++) {
                networkAddr[i] = (byte) (addr[i] & (mask >> (24 - i * 8)));
            }

            return java.net.InetAddress.getByAddress(networkAddr).getHostAddress();
        } catch (Exception e) {
            logger.debug("Could not calculate network address: {}", e.getMessage());
            return "unknown";
        }
    }

    private Map<String, Object> generateInterfaceSummary(List<Map<String, Object>> interfaces) {
        Map<String, Object> summary = new HashMap<>();

        int totalInterfaces = interfaces.size();
        int activeInterfaces = 0;
        int loopbackInterfaces = 0;
        int virtualInterfaces = 0;
        int pointToPointInterfaces = 0;
        int multicastInterfaces = 0;

        for (Map<String, Object> iface : interfaces) {
            if ((Boolean) iface.get("isUp")) {
                activeInterfaces++;
            }
            if ((Boolean) iface.get("isLoopback")) {
                loopbackInterfaces++;
            }
            if ((Boolean) iface.get("isVirtual")) {
                virtualInterfaces++;
            }
            if ((Boolean) iface.get("isPointToPoint")) {
                pointToPointInterfaces++;
            }
            if ((Boolean) iface.get("supportsMulticast")) {
                multicastInterfaces++;
            }
        }

        summary.put("totalInterfaces", totalInterfaces);
        summary.put("activeInterfaces", activeInterfaces);
        summary.put("inactiveInterfaces", totalInterfaces - activeInterfaces);
        summary.put("loopbackInterfaces", loopbackInterfaces);
        summary.put("virtualInterfaces", virtualInterfaces);
        summary.put("physicalInterfaces", totalInterfaces - virtualInterfaces);
        summary.put("pointToPointInterfaces", pointToPointInterfaces);
        summary.put("multicastInterfaces", multicastInterfaces);

        return summary;
    }
}
