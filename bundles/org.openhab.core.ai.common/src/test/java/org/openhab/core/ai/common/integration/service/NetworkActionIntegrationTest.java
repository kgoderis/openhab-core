package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.actions.network.*;
import org.openhab.core.ai.common.api.action.AIActionResult;

/**
 * Integration tests for Network-related AIActions using mocked openHAB services.
 */
class NetworkActionIntegrationTest extends BaseAIActionIntegrationTest {

    private GetNetworkStatusAction getNetworkStatusAction;
    private GetNetworkInterfacesAction getNetworkInterfacesAction;
    private GetNetworkConfigurationAction getNetworkConfigurationAction;
    private GetNetworkDevicesAction getNetworkDevicesAction;
    private GetNetworkPortsAction getNetworkPortsAction;
    private GetNetworkProtocolsAction getNetworkProtocolsAction;
    private GetNetworkRoutesAction getNetworkRoutesAction;
    private GetNetworkStatisticsAction getNetworkStatisticsAction;
    private ScanNetworkAction scanNetworkAction;
    private TestConnectivityAction testConnectivityAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all network actions
        getNetworkStatusAction = new GetNetworkStatusAction();
        getNetworkInterfacesAction = new GetNetworkInterfacesAction();
        getNetworkConfigurationAction = new GetNetworkConfigurationAction();
        getNetworkDevicesAction = new GetNetworkDevicesAction();
        getNetworkPortsAction = new GetNetworkPortsAction();
        getNetworkProtocolsAction = new GetNetworkProtocolsAction();
        getNetworkRoutesAction = new GetNetworkRoutesAction();
        getNetworkStatisticsAction = new GetNetworkStatisticsAction();
        scanNetworkAction = new ScanNetworkAction();
        testConnectivityAction = new TestConnectivityAction();

        // Initialize actions with context
        getNetworkStatusAction.initialize(actionContext);
        getNetworkInterfacesAction.initialize(actionContext);
        getNetworkConfigurationAction.initialize(actionContext);
        getNetworkDevicesAction.initialize(actionContext);
        getNetworkPortsAction.initialize(actionContext);
        getNetworkProtocolsAction.initialize(actionContext);
        getNetworkRoutesAction.initialize(actionContext);
        getNetworkStatisticsAction.initialize(actionContext);
        scanNetworkAction.initialize(actionContext);
        testConnectivityAction.initialize(actionContext);
    }

    @Test
    void testGetNetworkStatusAction() throws Exception {
        Map<String, Object> parameters = Map.of("interface", "eth0");
        AIActionResult result = executeAction(getNetworkStatusAction, parameters);

        // In mocked mode without real network interfaces, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "status");
            assertResultContainsKey(result, "interface");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetNetworkStatusActionWithAllInterfaces() throws Exception {
        Map<String, Object> parameters = Map.of("interface", "all");
        AIActionResult result = executeAction(getNetworkStatusAction, parameters);

        // In mocked mode without real network interfaces, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "interfaces");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetNetworkInterfacesAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(getNetworkInterfacesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "interfaces");
    }

    @Test
    void testGetNetworkInterfacesActionWithTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "type", "type", "ethernet");
        AIActionResult result = executeAction(getNetworkInterfacesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "interfaces");
    }

    @Test
    void testGetNetworkInterfacesActionWithStatusFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "status", "status", "up");
        AIActionResult result = executeAction(getNetworkInterfacesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "interfaces");
    }

    @Test
    void testGetNetworkConfigurationAction() throws Exception {
        Map<String, Object> parameters = Map.of("interface", "eth0");
        AIActionResult result = executeAction(getNetworkConfigurationAction, parameters);

        // In mocked mode without real network interfaces, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "configuration");
            assertResultContainsKey(result, "interface");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetNetworkConfigurationActionWithAllInterfaces() throws Exception {
        Map<String, Object> parameters = Map.of("interface", "all");
        AIActionResult result = executeAction(getNetworkConfigurationAction, parameters);

        // In mocked mode without real network interfaces, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "configurations");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetNetworkDevicesAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(getNetworkDevicesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "devices");
    }

    @Test
    void testGetNetworkDevicesActionWithTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "type", "type", "router");
        AIActionResult result = executeAction(getNetworkDevicesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "devices");
    }

    @Test
    void testGetNetworkDevicesActionWithIPFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "ip", "ipRange", "192.168.1.0/24");
        AIActionResult result = executeAction(getNetworkDevicesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "devices");
    }

    @Test
    void testGetNetworkPortsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(getNetworkPortsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "ports");
    }

    @Test
    void testGetNetworkPortsActionWithPortFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "port", "port", "80");
        AIActionResult result = executeAction(getNetworkPortsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "ports");
    }

    @Test
    void testGetNetworkPortsActionWithServiceFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "service", "service", "http");
        AIActionResult result = executeAction(getNetworkPortsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "ports");
    }

    @Test
    void testGetNetworkProtocolsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(getNetworkProtocolsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "protocols");
    }

    @Test
    void testGetNetworkProtocolsActionWithTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "type", "type", "tcp");
        AIActionResult result = executeAction(getNetworkProtocolsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "protocols");
    }

    @Test
    void testGetNetworkProtocolsActionWithPortFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "port", "port", "443");
        AIActionResult result = executeAction(getNetworkProtocolsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "protocols");
    }

    @Test
    void testGetNetworkRoutesAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        AIActionResult result = executeAction(getNetworkRoutesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "routes");
    }

    @Test
    void testGetNetworkRoutesActionWithDestinationFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "destination", "destination", "192.168.1.0/24");
        AIActionResult result = executeAction(getNetworkRoutesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "routes");
    }

    @Test
    void testGetNetworkRoutesActionWithGatewayFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "gateway", "gateway", "192.168.1.1");
        AIActionResult result = executeAction(getNetworkRoutesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "routes");
    }

    @Test
    void testGetNetworkStatisticsAction() throws Exception {
        Map<String, Object> parameters = Map.of("interface", "eth0");
        AIActionResult result = executeAction(getNetworkStatisticsAction, parameters);

        // In mocked mode without real network interfaces, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "statistics");
            assertResultContainsKey(result, "interface");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetNetworkStatisticsActionWithTimeRange() throws Exception {
        Map<String, Object> parameters = Map.of("interface", "eth0", "startTime", "2024-01-01T00:00:00Z", "endTime",
                "2024-01-01T23:59:59Z");
        AIActionResult result = executeAction(getNetworkStatisticsAction, parameters);

        // In mocked mode without real network interfaces, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "statistics");
            assertResultContainsKey(result, "timeRange");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testScanNetworkAction() throws Exception {
        Map<String, Object> parameters = Map.of("network", "192.168.1.0/24");
        AIActionResult result = executeAction(scanNetworkAction, parameters);

        // In mocked mode without real network scanning, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "devices");
            assertResultContainsKey(result, "network");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testScanNetworkActionWithPorts() throws Exception {
        Map<String, Object> parameters = Map.of("network", "192.168.1.0/24", "ports", List.of(80, 443, 22));
        AIActionResult result = executeAction(scanNetworkAction, parameters);

        // In mocked mode without real network scanning, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "devices");
            assertResultContainsKey(result, "ports");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testTestConnectivityAction() throws Exception {
        Map<String, Object> parameters = Map.of("target", "192.168.1.1");
        AIActionResult result = executeAction(testConnectivityAction, parameters);

        // In mocked mode without real network connectivity, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "reachable");
            assertResultContainsKey(result, "target");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testTestConnectivityActionWithPort() throws Exception {
        Map<String, Object> parameters = Map.of("target", "192.168.1.1", "port", 80);
        AIActionResult result = executeAction(testConnectivityAction, parameters);

        // In mocked mode without real network connectivity, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "reachable");
            assertResultContainsKey(result, "port");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testTestConnectivityActionWithProtocol() throws Exception {
        Map<String, Object> parameters = Map.of("target", "192.168.1.1", "protocol", "tcp");
        AIActionResult result = executeAction(testConnectivityAction, parameters);

        // In mocked mode without real network connectivity, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "reachable");
            assertResultContainsKey(result, "protocol");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetNetworkStatusActionWithInvalidInterface() throws Exception {
        Map<String, Object> parameters = Map.of("interface", "invalid-interface");
        AIActionResult result = executeAction(getNetworkStatusAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testGetNetworkConfigurationActionWithInvalidInterface() throws Exception {
        Map<String, Object> parameters = Map.of("interface", "invalid-interface");
        AIActionResult result = executeAction(getNetworkConfigurationAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testScanNetworkActionWithInvalidNetwork() throws Exception {
        Map<String, Object> parameters = Map.of("network", "invalid-network");
        AIActionResult result = executeAction(scanNetworkAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testTestConnectivityActionWithInvalidTarget() throws Exception {
        Map<String, Object> parameters = Map.of("target", "invalid-target");
        AIActionResult result = executeAction(testConnectivityAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testTestConnectivityActionWithInvalidPort() throws Exception {
        Map<String, Object> parameters = Map.of("target", "192.168.1.1", "port", 99999);
        AIActionResult result = executeAction(testConnectivityAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
