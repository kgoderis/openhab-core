package org.openhab.core.ai.tool.server.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.service.statistics.ErrorRecoveryStatistics;
import org.openhab.core.ai.common.security.ToolSecurityStatistics;
import org.openhab.core.ai.tool.config.ToolServerConfiguration;
import org.openhab.core.ai.tool.error.DefaultErrorRecoveryService;
import org.openhab.core.ai.tool.error.ErrorInfo;
import org.openhab.core.ai.tool.security.DefaultToolSecurityService;
import org.openhab.core.ai.tool.server.TransportHealthInfo;
import org.openhab.core.ai.tool.server.TransportStatistics;

/**
 * Tool Server Interface (consolidated MCP server API)
 *
 * Combines lifecycle, transport, security, and error recovery operations for an MCP Tool server.
 */
@NonNullByDefault
public interface ToolServer {

    void start() throws Exception;

    void stop() throws Exception;

    String getServerId();

    ToolServerConfiguration getConfiguration();

    ToolServerState getState();

    boolean isRunning();

    @Nullable
    ToolSecurityStatistics getSecurityStatistics();

    @Nullable
    ErrorRecoveryStatistics getErrorRecoveryStatistics();

    @Nullable
    Map<String, ErrorInfo> getErrorDetails();

    boolean isSecurityEnabled();

    boolean isErrorRecoveryEnabled();

    boolean isHealthy();

    TransportHealthInfo getTransportHealth();

    TransportStatistics getTransportStatistics();

    void setSecurityManager(DefaultToolSecurityService securityManager);

    void setErrorRecoveryManager(DefaultErrorRecoveryService errorRecoveryManager);
}
