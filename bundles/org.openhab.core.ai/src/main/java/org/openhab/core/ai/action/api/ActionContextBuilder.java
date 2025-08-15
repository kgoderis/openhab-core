package org.openhab.core.ai.action.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.auth.AuthenticationContext;

/**
 * Builder for {@link ActionContext}.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public final class ActionContextBuilder {
    String protocol = "";
    String clientId = "";
    String sessionId = "";
    @Nullable
    AuthenticationContext authContext;
    Map<String, Object> protocolContext = Map.of();
    long executionStartTime = System.currentTimeMillis();
    String correlationId = "";
    String priority = "";

    public ActionContextBuilder protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public ActionContextBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public ActionContextBuilder sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public ActionContextBuilder authContext(AuthenticationContext authContext) {
        this.authContext = authContext;
        return this;
    }

    public ActionContextBuilder protocolContext(Map<String, Object> protocolContext) {
        this.protocolContext = protocolContext != null ? protocolContext : Map.of();
        return this;
    }

    public ActionContextBuilder executionStartTime(long executionStartTime) {
        this.executionStartTime = executionStartTime;
        return this;
    }

    public ActionContextBuilder correlationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public ActionContextBuilder priority(String priority) {
        this.priority = priority;
        return this;
    }

    public ActionContext build() {
        return new ActionContext(this);
    }
}
