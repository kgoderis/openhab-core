package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.a2a.spec.TaskState;

/**
 * Parameters for server-side filtering and pagination of tasks listing.
 *
 * <p>
 * Author: Karel Goderis - Initial Contribution
 * </p>
 */
@NonNullByDefault
public final class ListTasksParams {

    private final @Nullable TaskState status;
    private final @Nullable String agentId;
    private final @Nullable String skillId;
    private final @Nullable Long createdAfter;
    private final @Nullable Long createdBefore;
    private final int limit;
    private final int offset;

    private ListTasksParams(Builder builder) {
        this.status = builder.status;
        this.agentId = builder.agentId;
        this.skillId = builder.skillId;
        this.createdAfter = builder.createdAfter;
        this.createdBefore = builder.createdBefore;
        this.limit = builder.limit;
        this.offset = builder.offset;
    }

    public static Builder builder() {
        return new Builder();
    }

    public @Nullable TaskState status() {
        return status;
    }

    public @Nullable String agentId() {
        return agentId;
    }

    public @Nullable String skillId() {
        return skillId;
    }

    public @Nullable Long createdAfter() {
        return createdAfter;
    }

    public @Nullable Long createdBefore() {
        return createdBefore;
    }

    public int limit() {
        return limit;
    }

    public int offset() {
        return offset;
    }

    public static final class Builder {
        private @Nullable TaskState status;
        private @Nullable String agentId;
        private @Nullable String skillId;
        private @Nullable Long createdAfter;
        private @Nullable Long createdBefore;
        private int limit = 50;
        private int offset = 0;

        public Builder status(@Nullable TaskState status) {
            this.status = status;
            return this;
        }

        public Builder agentId(@Nullable String agentId) {
            this.agentId = agentId;
            return this;
        }

        public Builder skillId(@Nullable String skillId) {
            this.skillId = skillId;
            return this;
        }

        public Builder createdAfter(@Nullable Long createdAfter) {
            this.createdAfter = createdAfter;
            return this;
        }

        public Builder createdBefore(@Nullable Long createdBefore) {
            this.createdBefore = createdBefore;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = Math.max(0, limit);
            return this;
        }

        public Builder offset(int offset) {
            this.offset = Math.max(0, offset);
            return this;
        }

        public ListTasksParams build() {
            return new ListTasksParams(this);
        }
    }
}
