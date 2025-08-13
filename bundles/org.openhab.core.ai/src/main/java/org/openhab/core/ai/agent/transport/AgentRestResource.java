package org.openhab.core.ai.agent.transport;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.agent.communication.notifications.AgentPushNotificationManager;
import org.openhab.core.ai.agent.execution.AgentTaskManager;
import org.openhab.core.ai.agent.execution.ListTasksParams;
import org.openhab.core.ai.agent.lifecycle.AgentRegistry;
import org.openhab.core.ai.rest.SharedRestInfrastructure;
import org.openhab.core.io.rest.RESTConstants;
import org.openhab.core.io.rest.RESTResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.a2a.spec.JSONRPCError;
import io.a2a.spec.PushNotificationAuthenticationInfo;
import io.a2a.spec.PushNotificationConfig;
import io.a2a.spec.Task;
import io.a2a.spec.TaskPushNotificationConfig;
import io.a2a.spec.TaskState;

/**
 * A2A REST endpoints aligned with plan 16.11.2.4 for non-protocol admin/UX.
 * Protocol-compliant operations remain in the servlet transport per policy.
 *
 * <p>
 * Author: Karel Goderis - Initial Contribution
 * </p>
 */
@Component(service = RESTResource.class)
@JaxrsResource
@JaxrsName("a2a/v1")
@JaxrsApplicationSelect("(" + JaxrsWhiteboardConstants.JAX_RS_NAME + "=" + RESTConstants.JAX_RS_NAME + ")")
@JSONRequired
@Path("a2a/v1")
@NonNullByDefault
public class AgentRestResource implements RESTResource {

    @Reference
    private @Nullable AgentTaskManager taskManager;

    @Reference
    private @Nullable AgentRegistry agentRegistry;

    @Reference
    private @Nullable AgentTransportFactory transportFactory;

    @Reference
    private @Nullable AgentPushNotificationManager pushManager;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @POST
    @Path("message:send")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMessage() {
        // Protocol-standard messages are handled by servlet for compliance
        return Response.status(501)
                .entity(Map.of("error", "Protocol-standard messages must use A2A servlet", "code",
                        "PROTOCOL_COMPLIANCE", "servlet_endpoint", "/a2a/message/send"))
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @POST
    @Path("message:stream")
    @Produces(MediaType.APPLICATION_JSON)
    public Response streamMessage() {
        // Protocol-standard streaming is handled by servlet for compliance
        return Response.status(501)
                .entity(Map.of("error", "Protocol-standard streaming must use A2A servlet", "code",
                        "PROTOCOL_COMPLIANCE", "servlet_endpoint", "/a2a/message/stream"))
                .type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Path("tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listTasks(@QueryParam("status") @Nullable String status,
            @QueryParam("agentId") @Nullable String agentId, @QueryParam("skillId") @Nullable String skillId,
            @QueryParam("createdAfter") @Nullable Long createdAfter,
            @QueryParam("createdBefore") @Nullable Long createdBefore, @QueryParam("limit") @Nullable Integer limit,
            @QueryParam("offset") @Nullable Integer offset) {
        try {
            ListTasksParams.Builder b = ListTasksParams.builder();
            if (status != null) {
                try {
                    b.status(TaskState.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException ex) {
                    return SharedRestInfrastructure.error(400, "Invalid status: " + status);
                }
            }
            if (agentId != null) {
                b.agentId(agentId);
            }
            if (skillId != null) {
                b.skillId(skillId);
            }
            if (createdAfter != null) {
                b.createdAfter(createdAfter);
            }
            if (createdBefore != null) {
                b.createdBefore(createdBefore);
            }
            if (limit != null) {
                b.limit(limit.intValue());
            }
            if (offset != null) {
                b.offset(offset.intValue());
            }

            AgentTaskManager mgr = taskManager;
            if (mgr == null) {
                return SharedRestInfrastructure.error(503, "Task manager not available");
            }
            List<Task> tasks = mgr.listTasksFiltered(b.build());
            return SharedRestInfrastructure.okJson(tasks);
        } catch (JSONRPCError e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Internal error listing tasks";
            return SharedRestInfrastructure.error(500, msg);
        }
    }

    @GET
    @Path("tasks/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTask(@PathParam("id") String id) {
        try {
            AgentTaskManager mgr = taskManager;
            if (mgr == null) {
                return SharedRestInfrastructure.error(Status.SERVICE_UNAVAILABLE, "Task manager not available");
            }
            Task task = mgr.getTask(id);
            return SharedRestInfrastructure.okJson(task);
        } catch (JSONRPCError e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Task not found";
            return SharedRestInfrastructure.error(Status.NOT_FOUND, msg);
        }
    }

    @POST
    @Path("tasks/{id}:cancel")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelTask(@PathParam("id") String id) {
        try {
            AgentTaskManager mgr = taskManager;
            if (mgr == null) {
                return SharedRestInfrastructure.error(Status.SERVICE_UNAVAILABLE, "Task manager not available");
            }
            Task task = mgr.cancelTask(id);
            return SharedRestInfrastructure.okJson(task);
        } catch (JSONRPCError e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Task not found";
            return SharedRestInfrastructure.error(Status.NOT_FOUND, msg);
        }
    }

    @GET
    @Path("card")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAgentCard() {
        AgentRegistry registry = agentRegistry;
        if (registry == null) {
            return SharedRestInfrastructure.error(Status.SERVICE_UNAVAILABLE, "Agent registry not available");
        }
        var agentIds = registry.getRegisteredAgentIds("system");

        AgentTransportFactory tf = transportFactory;
        TransportSummary transportSummary;
        if (tf != null) {
            var active = tf.getActiveTransports();
            int total = active.size();
            int restCount = 0;
            for (AgentTransport t : active.values()) {
                if (t.getCapabilities().getTransportType() == AgentTransport.TransportType.REST) {
                    restCount++;
                }
            }
            transportSummary = new TransportSummary(total, restCount);
        } else {
            transportSummary = new TransportSummary(0, 0);
        }

        return SharedRestInfrastructure.okJson(new AgentCardSummary(agentIds, transportSummary));
    }

    // ---------------------------------------------------------------------
    // Push Notification Configuration (A2A admin/UX)
    // ---------------------------------------------------------------------

    @POST
    @Path("tasks/{id}/pushNotificationConfig")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setTaskPushNotificationConfig(@PathParam("id") String taskId, String body) {
        var mgr = pushManager;
        if (mgr == null) {
            return SharedRestInfrastructure.error(Status.SERVICE_UNAVAILABLE, "Push manager not available");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = MAPPER.readValue(body, Map.class);
            String url = stringOrNull(payload.get("url"));
            String token = stringOrNull(payload.get("token"));
            String notificationId = stringOrNull(payload.get("notificationId"));
            @SuppressWarnings("unchecked")
            List<String> authSchemes = (List<String>) payload.getOrDefault("authSchemes", List.of("basic"));
            String credentials = stringOrNull(payload.get("credentials"));

            if (url == null || token == null || credentials == null || notificationId == null) {
                return SharedRestInfrastructure.error(Status.BAD_REQUEST,
                        "Missing required fields: url, token, credentials, notificationId");
            }

            PushNotificationAuthenticationInfo authInfo = new PushNotificationAuthenticationInfo(authSchemes,
                    credentials);
            PushNotificationConfig cfg = new PushNotificationConfig(url, token, authInfo, notificationId);
            TaskPushNotificationConfig tcfg = new TaskPushNotificationConfig(taskId, cfg);

            if (!mgr.validatePushNotificationConfig(tcfg)) {
                return SharedRestInfrastructure.error(Status.BAD_REQUEST, "Invalid push notification config");
            }

            TaskPushNotificationConfig saved = mgr.setTaskPushNotificationConfig(tcfg);
            return SharedRestInfrastructure.okJson(saved);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GET
    @Path("tasks/{id}/pushNotificationConfig")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskPushNotificationConfig(@PathParam("id") String taskId) {
        var mgr = pushManager;
        if (mgr == null) {
            return SharedRestInfrastructure.error(Status.SERVICE_UNAVAILABLE, "Push manager not available");
        }
        try {
            TaskPushNotificationConfig cfg = mgr.getTaskPushNotificationConfig(taskId);
            return SharedRestInfrastructure.okJson(cfg);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(Status.NOT_FOUND, e.getMessage());
        }
    }

    @GET
    @Path("tasks/{id}/pushNotificationConfigs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listTaskPushNotificationConfigs(@PathParam("id") String taskId) {
        var mgr = pushManager;
        if (mgr == null) {
            return SharedRestInfrastructure.error(Status.SERVICE_UNAVAILABLE, "Push manager not available");
        }
        try {
            List<TaskPushNotificationConfig> list = mgr.listTaskPushNotificationConfigs(taskId);
            return SharedRestInfrastructure.okJson(list);
        } catch (Exception e) {
            return SharedRestInfrastructure.error(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DELETE
    @Path("tasks/{id}/pushNotificationConfig")
    public Response deleteTaskPushNotificationConfig(@PathParam("id") String taskId) {
        var mgr = pushManager;
        if (mgr == null) {
            return SharedRestInfrastructure.error(Status.SERVICE_UNAVAILABLE, "Push manager not available");
        }
        try {
            mgr.deleteTaskPushNotificationConfig(taskId);
            return SharedRestInfrastructure.applyStandardHeaders(Response.status(Status.NO_CONTENT)).build();
        } catch (Exception e) {
            return SharedRestInfrastructure.error(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private static @Nullable String stringOrNull(@Nullable Object o) {
        return o instanceof String ? (String) o : null;
    }
}
