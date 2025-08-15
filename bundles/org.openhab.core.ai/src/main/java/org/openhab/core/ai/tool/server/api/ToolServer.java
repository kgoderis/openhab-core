package org.openhab.core.ai.tool.server.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.error.DefaultErrorRecoveryService;
import org.openhab.core.ai.tool.error.ErrorInfo;
import org.openhab.core.ai.tool.error.ErrorRecoveryStatistics;
import org.openhab.core.ai.tool.security.DefaultToolSecurityService;
import org.openhab.core.ai.tool.security.api.SecurityStatistics;

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

    org.openhab.core.ai.tool.server.ServerConfiguration getConfiguration();

    ToolServerState getState();

    boolean isRunning();

    @Nullable
    SecurityStatistics getSecurityStatistics();

    @Nullable
    ErrorRecoveryStatistics getErrorRecoveryStatistics();

    @Nullable
    Map<String, ErrorInfo> getErrorDetails();

    boolean isSecurityEnabled();

    boolean isErrorRecoveryEnabled();

    boolean isHealthy();

    org.openhab.core.ai.tool.server.TransportHealthInfo getTransportHealth();

    org.openhab.core.ai.tool.server.TransportStatistics getTransportStatistics();

    void setSecurityManager(DefaultToolSecurityService securityManager);

    void setErrorRecoveryManager(DefaultErrorRecoveryService errorRecoveryManager);
}
