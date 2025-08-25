package org.openhab.core.ai.agent.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.openhab.core.ai.common.monitoring.service.statistics.AgentBehaviorStatistics;
import org.openhab.core.ai.model.api.ModelProviderType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for managing agent models and their configurations.
 * 
 * <p>
 * This class provides centralized model management including registration,
 * retrieval, validation, and lifecycle management for agent models.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@Component(service = AgentModelRegistry.class)
@NonNullByDefault
public final class AgentModelRegistry {

    private final Logger logger = LoggerFactory.getLogger(AgentModelRegistry.class);

    private final Map<String, AgentModel> models = new ConcurrentHashMap<>();
    private final Map<String, AgentModelConfiguration> configurations = new ConcurrentHashMap<>();
    private final ReadWriteLock registryLock = new ReentrantReadWriteLock();

    // Metrics service for centralized metrics collection
    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private @Nullable MetricsService metricsService;

    /**
     * Register a new agent model.
     * 
     * @param model the model to register
     * @return true if registration was successful, false if model already exists
     */
    public boolean registerModel(AgentModel model) {
        Objects.requireNonNull(model, "model");

        registryLock.writeLock().lock();
        try {
            String modelId = model.getModelId();

            if (models.containsKey(modelId)) {
                logger.warn("Model already registered: {}", modelId);
                return false;
            }

            models.put(modelId, model);
            logger.debug("Registered model: {}", modelId);
            return true;

        } finally {
            registryLock.writeLock().unlock();
        }
    }

    /**
     * Unregister an agent model.
     * 
     * @param modelId the ID of the model to unregister
     * @return true if unregistration was successful, false if model not found
     */
    public boolean unregisterModel(String modelId) {
        Objects.requireNonNull(modelId, "modelId");

        registryLock.writeLock().lock();
        try {
            AgentModel removed = models.remove(modelId);
            if (removed != null) {
                configurations.remove(modelId);
                logger.debug("Unregistered model: {}", modelId);
                return true;
            }

            logger.warn("Model not found for unregistration: {}", modelId);
            return false;

        } finally {
            registryLock.writeLock().unlock();
        }
    }

    /**
     * Get a registered model by ID.
     * 
     * @param modelId the model ID
     * @return the model if found, empty otherwise
     */
    public Optional<AgentModel> getModel(String modelId) {
        Objects.requireNonNull(modelId, "modelId");

        registryLock.readLock().lock();
        try {
            return Optional.ofNullable(models.get(modelId));
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * Get all registered models.
     * 
     * @return list of all registered models
     */
    public List<AgentModel> getAllModels() {
        registryLock.readLock().lock();
        try {
            return new ArrayList<>(models.values());
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * Get models by provider type.
     * 
     * @param providerType the provider type to filter by
     * @return list of models for the specified provider
     */
    public List<AgentModel> getModelsByProvider(ModelProviderType providerType) {
        Objects.requireNonNull(providerType, "providerType");

        registryLock.readLock().lock();
        try {
            return models.values().stream().filter(model -> providerType.equals(model.getProviderType())).toList();
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * Get models by capability.
     * 
     * @param capability the capability to filter by
     * @return list of models with the specified capability
     */
    public List<AgentModel> getModelsByCapability(String capability) {
        Objects.requireNonNull(capability, "capability");

        registryLock.readLock().lock();
        try {
            return models.values().stream().filter(model -> model.getCapabilities().contains(capability)).toList();
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * Register a model configuration.
     * 
     * @param configuration the configuration to register
     * @return true if registration was successful, false if already registered
     */
    public boolean registerConfiguration(AgentModelConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");

        registryLock.writeLock().lock();
        try {
            String modelId = configuration.getId();

            if (configurations.containsKey(modelId)) {
                logger.warn("Configuration already registered for model: {}", modelId);
                return false;
            }

            configurations.put(modelId, configuration);
            logger.debug("Registered configuration for model: {}", modelId);
            return true;

        } finally {
            registryLock.writeLock().unlock();
        }
    }

    /**
     * Get a model configuration by model ID.
     * 
     * @param modelId the model ID
     * @return the configuration if found, empty otherwise
     */
    public Optional<AgentModelConfiguration> getConfiguration(String modelId) {
        Objects.requireNonNull(modelId, "modelId");

        registryLock.readLock().lock();
        try {
            return Optional.ofNullable(configurations.get(modelId));
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * Update a model configuration.
     * 
     * @param configuration the updated configuration
     * @return true if update was successful, false if configuration not found
     */
    public boolean updateConfiguration(AgentModelConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");

        registryLock.writeLock().lock();
        try {
            String modelId = configuration.getId();

            if (!configurations.containsKey(modelId)) {
                logger.warn("Configuration not found for update: {}", modelId);
                return false;
            }

            configurations.put(modelId, configuration);
            logger.debug("Updated configuration for model: {}", modelId);
            return true;

        } finally {
            registryLock.writeLock().unlock();
        }
    }

    /**
     * Remove a model configuration.
     * 
     * @param modelId the model ID
     * @return true if removal was successful, false if configuration not found
     */
    public boolean removeConfiguration(String modelId) {
        Objects.requireNonNull(modelId, "modelId");

        registryLock.writeLock().lock();
        try {
            AgentModelConfiguration removed = configurations.remove(modelId);
            if (removed != null) {
                logger.debug("Removed configuration for model: {}", modelId);
                return true;
            }

            logger.warn("Configuration not found for removal: {}", modelId);
            return false;

        } finally {
            registryLock.writeLock().unlock();
        }
    }

    /**
     * Get all model configurations.
     * 
     * @return list of all configurations
     */
    public List<AgentModelConfiguration> getAllConfigurations() {
        registryLock.readLock().lock();
        try {
            return new ArrayList<>(configurations.values());
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * Check if a model is registered.
     * 
     * @param modelId the model ID
     * @return true if model is registered
     */
    public boolean isModelRegistered(String modelId) {
        Objects.requireNonNull(modelId, "modelId");

        registryLock.readLock().lock();
        try {
            return models.containsKey(modelId);
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * Check if a configuration exists for a model.
     * 
     * @param modelId the model ID
     * @return true if configuration exists
     */
    public boolean hasConfiguration(String modelId) {
        Objects.requireNonNull(modelId, "modelId");

        registryLock.readLock().lock();
        try {
            return configurations.containsKey(modelId);
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * Get the number of registered models.
     * 
     * @return the number of models
     */
    public int getModelCount() {
        registryLock.readLock().lock();
        try {
            return models.size();
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * Get the number of configurations.
     * 
     * @return the number of configurations
     */
    public int getConfigurationCount() {
        registryLock.readLock().lock();
        try {
            return configurations.size();
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * Clear all models and configurations.
     */
    public void clear() {
        registryLock.writeLock().lock();
        try {
            models.clear();
            configurations.clear();
            logger.debug("Cleared all models and configurations");
        } finally {
            registryLock.writeLock().unlock();
        }
    }

    /**
     * Get registry statistics.
     * 
     * @return registry statistics
     */
    public AgentBehaviorStatistics getStatistics() {
        // Create AgentBehaviorStatistics from registry data
        return AgentBehaviorStatistics.fromSnapshots(List.of(), // No snapshots available yet - will be integrated with
                                                                // MetricsService
                Duration.ofDays(1));
    }

    protected void setMetricsService(MetricsService metricsService) {
        this.metricsService = metricsService;
        logger.debug("MetricsService set for AgentModelRegistry");
    }

    protected void unsetMetricsService(MetricsService metricsService) {
        this.metricsService = null;
        logger.debug("MetricsService unset for AgentModelRegistry");
    }

    /**
     * Record metrics for agent model registry operations
     */
    private void recordMetrics(String operation, boolean success, long durationNanos) {
        MetricsService metrics = metricsService;
        if (metrics != null) {
            try {
                metrics.recordOperation("agent-model-registry", operation, success, Duration.ofNanos(durationNanos));
            } catch (Exception e) {
                logger.debug("Failed to record metrics for {}.{}: {}", "agent-model-registry", operation,
                        e.getMessage());
            }
        } else {
            logger.debug("MetricsService not available, cannot record metrics for operation: {}", operation);
        }
    }
}
