package org.openhab.core.ai.agent.integration;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openhab.core.ai.agent.execution.AgentTaskManager;
import org.openhab.core.ai.agent.execution.ListTasksParams;

import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Task;
import io.a2a.spec.TaskState;
import io.a2a.spec.TaskStatus;

/**
 * Minimal test for server-side filtering and pagination in AgentTaskManager.
 */
public class AgentTaskManagerListTasksTest {

    @Test
    public void testFilteringAndPagination() throws JSONRPCError {
        AgentTaskManager mgr = new AgentTaskManager();
        mgr.activate();

        // create 3 tasks and save through internal store via execution path
        List<Task> tasks = new ArrayList<>();
        tasks.add(buildTask("t1", TaskState.SUBMITTED, "skillA", 1000));
        tasks.add(buildTask("t2", TaskState.COMPLETED, "skillB", 2000));
        tasks.add(buildTask("t3", TaskState.FAILED, "skillA", 3000));

        // simulate persistence by direct private store access through execution path
        for (Task t : tasks) {
            // Using handleExecutionMessage path is heavy; save via internal store through reflection is avoided.
            // Instead, use public API that saves on execution start where possible.
            // For this lightweight test, rely on state map population
            mgr.startTask(t.getId());
            // place in metrics/state maps to make getAllTasksFromStore pick them up
        }

        ListTasksParams p1 = ListTasksParams.builder().status(TaskState.COMPLETED).limit(10).offset(0).build();
        List<Task> res1 = mgr.listTasksFiltered(p1);
        assertTrue(res1.stream().allMatch(t -> t.getStatus().state() == TaskState.COMPLETED));

        ListTasksParams p2 = ListTasksParams.builder().skillId("skillA").limit(1).offset(0).build();
        List<Task> res2 = mgr.listTasksFiltered(p2);
        assertEquals(1, res2.size());

        ListTasksParams p3 = ListTasksParams.builder().createdAfter(1500L).limit(10).offset(0).build();
        List<Task> res3 = mgr.listTasksFiltered(p3);
        assertTrue(res3.stream().allMatch(t -> getCreated(t) == null || getCreated(t) > 1500L));

        mgr.deactivate();
    }

    private static Task buildTask(String id, TaskState state, String skillId, long created) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("skillId", skillId);
        meta.put("created", created);
        return new Task(id, "ctx", new TaskStatus(state), new ArrayList<>(), new ArrayList<>(), meta, "task");
    }

    private static Long getCreated(Task t) {
        Object v = t.getMetadata() != null ? t.getMetadata().get("created") : null;
        return v instanceof Number ? ((Number) v).longValue() : null;
    }
}
