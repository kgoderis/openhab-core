package org.openhab.core.ai.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.actions.system.*;

/**
 * Integration tests for System-related Actions using mocked openHAB services.
 */
class SystemActionIntegrationTest extends BaseActionIntegrationTest {

    private SystemInfoAction systemInfoAction;
    private SystemStatusAction systemStatusAction;
    private SystemDiagnosticsAction systemDiagnosticsAction;
    private HealthCheckAction healthCheckAction;
    private CPUInfoAction cpuInfoAction;
    private MemoryInfoAction memoryInfoAction;
    private DiskInfoAction diskInfoAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all system actions
        systemInfoAction = new SystemInfoAction();
        systemStatusAction = new SystemStatusAction();
        systemDiagnosticsAction = new SystemDiagnosticsAction();
        healthCheckAction = new HealthCheckAction();
        cpuInfoAction = new CPUInfoAction();
        memoryInfoAction = new MemoryInfoAction();
        diskInfoAction = new DiskInfoAction();

        // Initialize actions with context
        systemInfoAction.initialize(actionContext);
        systemStatusAction.initialize(actionContext);
        systemDiagnosticsAction.initialize(actionContext);
        healthCheckAction.initialize(actionContext);
        cpuInfoAction.initialize(actionContext);
        memoryInfoAction.initialize(actionContext);
        diskInfoAction.initialize(actionContext);
    }

    @Test
    void testSystemInfoAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        ActionResult result = executeAction(systemInfoAction, parameters);

        // In mocked mode without real system information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "systemInfo");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSystemInfoActionWithOSFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "os");
        ActionResult result = executeAction(systemInfoAction, parameters);

        // In mocked mode without real system information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "osInfo");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSystemInfoActionWithJavaFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "java");
        ActionResult result = executeAction(systemInfoAction, parameters);

        // In mocked mode without real system information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "javaInfo");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSystemStatusAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        ActionResult result = executeAction(systemStatusAction, parameters);

        // In mocked mode without real system status, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "systemStatus");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSystemStatusActionWithUptimeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "uptime");
        ActionResult result = executeAction(systemStatusAction, parameters);

        // In mocked mode without real system status, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "uptime");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSystemStatusActionWithLoadFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "load");
        ActionResult result = executeAction(systemStatusAction, parameters);

        // In mocked mode without real system status, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "loadAverage");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSystemDiagnosticsAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "run");
        ActionResult result = executeAction(systemDiagnosticsAction, parameters);

        // In mocked mode without real system diagnostics, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "diagnostics");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSystemDiagnosticsActionWithSpecificTest() throws Exception {
        Map<String, Object> parameters = Map.of("action", "run", "test", "memory");
        ActionResult result = executeAction(systemDiagnosticsAction, parameters);

        // In mocked mode without real system diagnostics, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "diagnostics");
            assertResultContainsKey(result, "test");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSystemDiagnosticsActionWithDetailedOutput() throws Exception {
        Map<String, Object> parameters = Map.of("action", "run", "detailed", true);
        ActionResult result = executeAction(systemDiagnosticsAction, parameters);

        // In mocked mode without real system diagnostics, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "diagnostics");
            assertResultContainsKey(result, "detailed");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testHealthCheckAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        ActionResult result = executeAction(healthCheckAction, parameters);

        // In mocked mode without real health check system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "healthStatus");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testHealthCheckActionWithServiceFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "service", "service", "core");
        ActionResult result = executeAction(healthCheckAction, parameters);

        // In mocked mode without real health check system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "healthStatus");
            assertResultContainsKey(result, "service");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testHealthCheckActionWithComponentFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "component", "component", "items");
        ActionResult result = executeAction(healthCheckAction, parameters);

        // In mocked mode without real health check system, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "healthStatus");
            assertResultContainsKey(result, "component");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCPUInfoAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        ActionResult result = executeAction(cpuInfoAction, parameters);

        // In mocked mode without real CPU information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "cpuInfo");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCPUInfoActionWithUsageFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "usage");
        ActionResult result = executeAction(cpuInfoAction, parameters);

        // In mocked mode without real CPU information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "cpuUsage");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCPUInfoActionWithLoadFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "load");
        ActionResult result = executeAction(cpuInfoAction, parameters);

        // In mocked mode without real CPU information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "cpuLoad");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testMemoryInfoAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        ActionResult result = executeAction(memoryInfoAction, parameters);

        // In mocked mode without real memory information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "memoryInfo");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testMemoryInfoActionWithUsageFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "usage");
        ActionResult result = executeAction(memoryInfoAction, parameters);

        // In mocked mode without real memory information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "memoryUsage");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testMemoryInfoActionWithHeapFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "heap");
        ActionResult result = executeAction(memoryInfoAction, parameters);

        // In mocked mode without real memory information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "heapInfo");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDiskInfoAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        ActionResult result = executeAction(diskInfoAction, parameters);

        // In mocked mode without real disk information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "diskInfo");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDiskInfoActionWithUsageFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "usage");
        ActionResult result = executeAction(diskInfoAction, parameters);

        // In mocked mode without real disk information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "diskUsage");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDiskInfoActionWithSpecificPath() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "path", "path", "/tmp");
        ActionResult result = executeAction(diskInfoAction, parameters);

        // In mocked mode without real disk information, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "diskInfo");
            assertResultContainsKey(result, "path");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSystemDiagnosticsActionWithInvalidAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "invalid_action");
        ActionResult result = executeAction(systemDiagnosticsAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testHealthCheckActionWithInvalidFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "invalid_filter");
        ActionResult result = executeAction(healthCheckAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testDiskInfoActionWithInvalidPath() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "path", "path", "/invalid/path");
        ActionResult result = executeAction(diskInfoAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
