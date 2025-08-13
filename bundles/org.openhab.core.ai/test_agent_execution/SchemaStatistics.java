package org.openhab.core.ai.agent.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.action.ActionMetadata;
import org.openhab.core.ai.action.ActionRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.a2a.spec.Task;

    public static class SchemaStatistics {
        private final long totalSchemasGenerated;
        private final long totalSchemaValidations;
        private final long totalSchemaCacheHits;
        private final long totalSchemaCacheMisses;
        private final int cacheSize;
        private final int versionCount;

        public SchemaStatistics(long totalSchemasGenerated, long totalSchemaValidations, long totalSchemaCacheHits,
                long totalSchemaCacheMisses, int cacheSize, int versionCount) {
            this.totalSchemasGenerated = totalSchemasGenerated;
            this.totalSchemaValidations = totalSchemaValidations;
            this.totalSchemaCacheHits = totalSchemaCacheHits;
            this.totalSchemaCacheMisses = totalSchemaCacheMisses;
            this.cacheSize = cacheSize;
            this.versionCount = versionCount;
        }

        // Getters
        public long getTotalSchemasGenerated() {
            return totalSchemasGenerated;
        }

        public long getTotalSchemaValidations() {
            return totalSchemaValidations;
        }

        public long getTotalSchemaCacheHits() {
            return totalSchemaCacheHits;
        }

        public long getTotalSchemaCacheMisses() {
            return totalSchemaCacheMisses;
        }

        public int getCacheSize() {
            return cacheSize;
        }

        public int getVersionCount() {
            return versionCount;
        }

        public double getCacheHitRate() {
            long totalRequests = totalSchemaCacheHits + totalSchemaCacheMisses;
            return totalRequests > 0 ? (double) totalSchemaCacheHits / totalRequests : 0.0;
        }
    }