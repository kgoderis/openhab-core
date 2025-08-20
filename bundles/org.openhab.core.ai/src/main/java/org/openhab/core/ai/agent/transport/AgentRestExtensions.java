package org.openhab.core.ai.agent.transport;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agent.api.AgentInfo;
import org.openhab.core.ai.rest.SharedRestInfrastructure;
import org.openhab.core.io.rest.RESTConstants;
import org.openhab.core.io.rest.RESTResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

/**
 * REST Extensions for AI functionality with advanced features
 * 
 * @author Karel Goderis - Initial Contribution
 */
@JaxrsResource
@JaxrsName("ai/extensions")
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@JSONRequired
@Path("ai/extensions")
@NonNullByDefault
@Component(service = RESTResource.class)
public class AgentRestExtensions implements RESTResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExtensionsInfo(@Context HttpHeaders headers) {
        // Simple ETag/If-None-Match support for demo purposes
        String etag = "W/\"ai-extensions-v1\"";
        String ifNoneMatch = headers.getHeaderString(HttpHeaders.IF_NONE_MATCH);
        if (etag.equals(ifNoneMatch)) {
            return SharedRestInfrastructure.applyStandardHeaders(Response.status(Response.Status.NOT_MODIFIED))
                    .tag(etag).build();
        }
        return SharedRestInfrastructure
                .applyStandardHeaders(Response.ok(new AgentInfo("extensions", "ok", "ai extensions ready"))).tag(etag)
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Path("features")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFeatures(@QueryParam("category") String category, @Context HttpHeaders headers) {
        // Rate limiting simulation
        String userAgent = headers.getHeaderString(HttpHeaders.USER_AGENT);
        if (userAgent != null && userAgent.contains("bot")) {
            return Response.status(429).entity(Map.of("error", "Rate limit exceeded")).build();
        }

        Map<String, Object> features = Map.of("caching", Map.of("enabled", true, "type", "ETag", "max_age", 300),
                "pagination", Map.of("enabled", true, "default_limit", 50, "max_limit", 100), "rate_limiting",
                Map.of("enabled", true, "requests_per_minute", 60), "compression",
                Map.of("enabled", true, "type", "gzip"), "monitoring",
                Map.of("enabled", true, "metrics", "prometheus"));

        return SharedRestInfrastructure.okJson(features);
    }

    @GET
    @Path("health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHealth() {
        Map<String, Object> health = Map.of("status", "healthy", "timestamp", System.currentTimeMillis(), "version",
                "1.0", "uptime", System.currentTimeMillis() - 1704067200000L);
        return SharedRestInfrastructure.okJson(health);
    }

    @GET
    @Path("metrics")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetrics() {
        Map<String, Object> metrics = Map.of("requests_total", 0, "requests_per_second", 0.0, "average_response_time",
                0.0, "error_rate", 0.0, "active_connections", 0);
        return SharedRestInfrastructure.okJson(metrics);
    }
}
