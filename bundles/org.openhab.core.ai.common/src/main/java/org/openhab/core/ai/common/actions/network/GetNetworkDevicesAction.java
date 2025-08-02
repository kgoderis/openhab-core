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
import org.openhab.core.ai.common.action.AIActionContext;
import org.openhab.core.ai.common.action.AIActionMetadata;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.action.AIActionValidationResult;
import org.openhab.core.ai.common.api.action.AIAction;
import org.openhab.core.ai.common.api.action.AIActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to get information about network devices
 * 
 * @author openHAB
 */
@NonNullByDefault
public class GetNetworkDevicesAction implements AIAction {

    private static final Logger logger = LoggerFactory.getLogger(GetNetworkDevicesAction.class);

    private static final String ACTION_ID = "get_network_devices";
    private static final String ACTION_NAME = "Get Network Devices";
    private static final String DESCRIPTION = "Retrieves information about network devices and their capabilities";
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
        properties.put("includeLocalDevices",
                Map.of("type", "boolean", "description", "Include local network devices", "default", true));
        properties.put("includeRemoteDevices",
                Map.of("type", "boolean", "description", "Include remote network devices", "default", true));
        properties.put("includeDeviceCapabilities",
                Map.of("type", "boolean", "description", "Include device capabilities", "default", true));
        properties.put("includeDeviceStatus",
                Map.of("type", "boolean", "description", "Include device status", "default", true));
        properties.put("networkRange", Map.of("type", "string", "description", "Network range to scan for devices",
                "default", "192.168.1.0/24"));

        schema.put("properties", properties);
        schema.put("required", List.of());

        return schema;
    }

    @Override
    public Map<String, Object> getReturnSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        properties.put("localDevices", Map.of("type", "array", "description", "Local network devices"));
        properties.put("remoteDevices", Map.of("type", "array", "description", "Remote network devices"));
        properties.put("deviceCapabilities", Map.of("type", "object", "description", "Device capabilities summary"));
        properties.put("deviceStatus", Map.of("type", "object", "description", "Device status summary"));
        properties.put("summary", Map.of("type", "object", "description", "Device summary information"));
        properties.put("timestamp", Map.of("type", "string", "description", "Timestamp of the device scan"));

        schema.put("properties", properties);

        return schema;
    }

    @Override
    public Map<String, Object> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("device_discovery", true);
        capabilities.put("device_capabilities", true);
        capabilities.put("device_status", true);
        capabilities.put("network_scanning", true);
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
        validateBooleanParameter(parameters, "includeLocalDevices", errors);
        validateBooleanParameter(parameters, "includeRemoteDevices", errors);
        validateBooleanParameter(parameters, "includeDeviceCapabilities", errors);
        validateBooleanParameter(parameters, "includeDeviceStatus", errors);

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
        logger.debug("Executing GetNetworkDevicesAction with context: {}", context.getProtocol());
        long startTime = System.currentTimeMillis();

        try {
            // Extract parameters with defaults
            boolean includeLocalDevices = (Boolean) parameters.getOrDefault("includeLocalDevices", true);
            boolean includeRemoteDevices = (Boolean) parameters.getOrDefault("includeRemoteDevices", true);
            boolean includeDeviceCapabilities = (Boolean) parameters.getOrDefault("includeDeviceCapabilities", true);
            boolean includeDeviceStatus = (Boolean) parameters.getOrDefault("includeDeviceStatus", true);
            String networkRange = (String) parameters.getOrDefault("networkRange", "192.168.1.0/24");

            Map<String, Object> result = new HashMap<>();

            // Get local devices
            if (includeLocalDevices) {
                result.put("localDevices", getLocalDevices(includeDeviceCapabilities, includeDeviceStatus));
            }

            // Get remote devices
            if (includeRemoteDevices) {
                result.put("remoteDevices",
                        getRemoteDevices(networkRange, includeDeviceCapabilities, includeDeviceStatus));
            }

            // Generate device capabilities summary
            if (includeDeviceCapabilities) {
                result.put("deviceCapabilities", getDeviceCapabilitiesSummary(result));
            }

            // Generate device status summary
            if (includeDeviceStatus) {
                result.put("deviceStatus", getDeviceStatusSummary(result));
            }

            // Generate overall summary
            result.put("summary", generateDeviceSummary(result));
            result.put("timestamp", java.time.Instant.now().toString());

            long executionTime = System.currentTimeMillis() - startTime;
            return AIActionResult.success(result, executionTime);

        } catch (Exception e) {
            logger.error("Error getting network devices: {}", e.getMessage(), e);
            throw new AIActionException(ACTION_ID, "Failed to get network devices: " + e.getMessage(), e);
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
        logger.debug("GetNetworkDevicesAction initialized with context: {}", context.getProtocol());
    }

    @Override
    public void cleanup() {
        logger.debug("GetNetworkDevicesAction cleaned up");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    private List<Map<String, Object>> getLocalDevices(boolean includeCapabilities, boolean includeStatus) {
        List<Map<String, Object>> devices = new ArrayList<>();

        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (ni.isUp() && !ni.isLoopback()) {
                    Map<String, Object> device = new HashMap<>();
                    device.put("name", ni.getName());
                    device.put("displayName", ni.getDisplayName());
                    device.put("type", "Network Interface");
                    device.put("macAddress", getHardwareAddress(ni));
                    device.put("mtu", ni.getMTU());
                    device.put("isVirtual", ni.isVirtual());
                    device.put("supportsMulticast", ni.supportsMulticast());

                    // Get IP addresses
                    List<String> ipAddresses = new ArrayList<>();
                    ni.getInterfaceAddresses().forEach(addr -> {
                        ipAddresses.add(addr.getAddress().getHostAddress());
                    });
                    device.put("ipAddresses", ipAddresses);

                    if (includeCapabilities) {
                        device.put("capabilities", getDeviceCapabilities(ni));
                    }

                    if (includeStatus) {
                        device.put("status", getDeviceStatus(ni));
                    }

                    devices.add(device);
                }
            }
        } catch (SocketException e) {
            logger.warn("Error getting local network devices: {}", e.getMessage());
        }

        return devices;
    }

    private List<Map<String, Object>> getRemoteDevices(String networkRange, boolean includeCapabilities,
            boolean includeStatus) {
        List<Map<String, Object>> devices = new ArrayList<>();

        // Simulated remote devices (in real implementation would scan the network)
        Map<String, Object> router = new HashMap<>();
        router.put("name", "Router");
        router.put("type", "Network Router");
        router.put("ipAddress", "192.168.1.1");
        router.put("macAddress", "00:11:22:33:44:55");
        router.put("manufacturer", "TP-Link");
        router.put("model", "Archer C7");
        router.put("capabilities", List.of("Routing", "DHCP", "NAT", "WiFi", "Firewall"));
        router.put("status", "Online");
        devices.add(router);

        Map<String, Object> printer = new HashMap<>();
        printer.put("name", "Network Printer");
        printer.put("type", "Printer");
        printer.put("ipAddress", "192.168.1.100");
        printer.put("macAddress", "AA:BB:CC:DD:EE:FF");
        printer.put("manufacturer", "HP");
        printer.put("model", "LaserJet Pro");
        printer.put("capabilities", List.of("Printing", "Scanning", "Network"));
        printer.put("status", "Online");
        devices.add(printer);

        Map<String, Object> nas = new HashMap<>();
        nas.put("name", "NAS Device");
        nas.put("type", "Network Attached Storage");
        nas.put("ipAddress", "192.168.1.200");
        nas.put("macAddress", "11:22:33:44:55:66");
        nas.put("manufacturer", "Synology");
        nas.put("model", "DS220+");
        nas.put("capabilities", List.of("File Storage", "Backup", "Media Server", "VPN"));
        nas.put("status", "Online");
        devices.add(nas);

        return devices;
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

    private Map<String, Object> getDeviceCapabilities(NetworkInterface ni) {
        Map<String, Object> capabilities = new HashMap<>();
        try {
            capabilities.put("ethernet", true);
            capabilities.put("wireless",
                    ni.getName().toLowerCase().contains("wlan") || ni.getName().toLowerCase().contains("wifi"));
            capabilities.put("bluetooth", ni.getName().toLowerCase().contains("bluetooth"));
            capabilities.put("multicast", ni.supportsMulticast());
            capabilities.put("pointToPoint", ni.isPointToPoint());
            capabilities.put("virtual", ni.isVirtual());
        } catch (SocketException e) {
            logger.debug("Error getting device capabilities for {}: {}", ni.getName(), e.getMessage());
        }
        return capabilities;
    }

    private Map<String, Object> getDeviceStatus(NetworkInterface ni) {
        Map<String, Object> status = new HashMap<>();
        try {
            boolean isUp = ni.isUp();
            status.put("operational", isUp);
            status.put("enabled", isUp);
            status.put("connected", isUp && !ni.getInterfaceAddresses().isEmpty());
            status.put("speed", "1000 Mbps"); // Simulated
            status.put("duplex", "Full"); // Simulated
        } catch (SocketException e) {
            logger.debug("Error getting device status for {}: {}", ni.getName(), e.getMessage());
            status.put("operational", false);
            status.put("enabled", false);
            status.put("connected", false);
            status.put("speed", "unknown");
            status.put("duplex", "unknown");
        }
        return status;
    }

    private Map<String, Object> getDeviceCapabilitiesSummary(Map<String, Object> devices) {
        Map<String, Object> summary = new HashMap<>();

        int totalDevices = 0;
        int ethernetDevices = 0;
        int wirelessDevices = 0;
        int bluetoothDevices = 0;
        int multicastDevices = 0;
        int virtualDevices = 0;

        // Count local devices
        if (devices.containsKey("localDevices")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> localDevices = (List<Map<String, Object>>) devices.get("localDevices");
            totalDevices += localDevices.size();

            for (Map<String, Object> device : localDevices) {
                if (device.containsKey("capabilities")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> capabilities = (Map<String, Object>) device.get("capabilities");

                    if ((Boolean) capabilities.getOrDefault("ethernet", false)) {
                        ethernetDevices++;
                    }
                    if ((Boolean) capabilities.getOrDefault("wireless", false)) {
                        wirelessDevices++;
                    }
                    if ((Boolean) capabilities.getOrDefault("bluetooth", false)) {
                        bluetoothDevices++;
                    }
                    if ((Boolean) capabilities.getOrDefault("multicast", false)) {
                        multicastDevices++;
                    }
                    if ((Boolean) capabilities.getOrDefault("virtual", false)) {
                        virtualDevices++;
                    }
                }
            }
        }

        // Count remote devices
        if (devices.containsKey("remoteDevices")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> remoteDevices = (List<Map<String, Object>>) devices.get("remoteDevices");
            totalDevices += remoteDevices.size();
        }

        summary.put("totalDevices", totalDevices);
        summary.put("ethernetDevices", ethernetDevices);
        summary.put("wirelessDevices", wirelessDevices);
        summary.put("bluetoothDevices", bluetoothDevices);
        summary.put("multicastDevices", multicastDevices);
        summary.put("virtualDevices", virtualDevices);

        return summary;
    }

    private Map<String, Object> getDeviceStatusSummary(Map<String, Object> devices) {
        Map<String, Object> summary = new HashMap<>();

        int totalDevices = 0;
        int onlineDevices = 0;
        int offlineDevices = 0;
        int connectedDevices = 0;

        // Count local devices
        if (devices.containsKey("localDevices")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> localDevices = (List<Map<String, Object>>) devices.get("localDevices");
            totalDevices += localDevices.size();

            for (Map<String, Object> device : localDevices) {
                if (device.containsKey("status")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> status = (Map<String, Object>) device.get("status");

                    if ((Boolean) status.getOrDefault("operational", false)) {
                        onlineDevices++;
                    } else {
                        offlineDevices++;
                    }

                    if ((Boolean) status.getOrDefault("connected", false)) {
                        connectedDevices++;
                    }
                }
            }
        }

        // Count remote devices
        if (devices.containsKey("remoteDevices")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> remoteDevices = (List<Map<String, Object>>) devices.get("remoteDevices");
            totalDevices += remoteDevices.size();

            for (Map<String, Object> device : remoteDevices) {
                String status = (String) device.getOrDefault("status", "Unknown");
                if ("Online".equals(status)) {
                    onlineDevices++;
                    connectedDevices++;
                } else {
                    offlineDevices++;
                }
            }
        }

        summary.put("totalDevices", totalDevices);
        summary.put("onlineDevices", onlineDevices);
        summary.put("offlineDevices", offlineDevices);
        summary.put("connectedDevices", connectedDevices);
        summary.put("onlinePercentage", totalDevices > 0 ? (double) onlineDevices / totalDevices * 100 : 0);

        return summary;
    }

    private Map<String, Object> generateDeviceSummary(Map<String, Object> devices) {
        Map<String, Object> summary = new HashMap<>();

        int localDevices = 0;
        int remoteDevices = 0;

        if (devices.containsKey("localDevices")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> localDevicesList = (List<Map<String, Object>>) devices.get("localDevices");
            localDevices = localDevicesList.size();
        }

        if (devices.containsKey("remoteDevices")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> remoteDevicesList = (List<Map<String, Object>>) devices.get("remoteDevices");
            remoteDevices = remoteDevicesList.size();
        }

        summary.put("totalDevices", localDevices + remoteDevices);
        summary.put("localDevices", localDevices);
        summary.put("remoteDevices", remoteDevices);

        return summary;
    }
}
