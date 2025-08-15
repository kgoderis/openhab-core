package org.openhab.core.ai.agent.infrastructure.synchronization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Agent task representation used by synchronization manager.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public final class AgentTask {
    private final String taskId;
    private final String agentId;
    private final List<String> dependencies;
    private final Map<String, Object> parameters;

    public AgentTask(String taskId, String agentId, List<String> dependencies, Map<String, Object> parameters) {
        this.taskId = taskId;
        this.agentId = agentId;
        this.dependencies = new ArrayList<>(dependencies);
        this.parameters = Map.copyOf(parameters);
    }

    public String getTaskId() {
        return taskId;
    }

    public String getAgentId() {
        return agentId;
    }

    public List<String> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
}
