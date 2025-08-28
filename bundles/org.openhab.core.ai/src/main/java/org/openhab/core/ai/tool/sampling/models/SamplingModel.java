package org.openhab.core.ai.tool.sampling.models;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.MetricsService;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model for MCP sampling operations.
 * 
 * This class represents a sampling model that can be used for generating
 * samples from various data sources and distributions.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SamplingModel {

    private static final Logger logger = LoggerFactory.getLogger(SamplingModel.class);

    private final String id;
    private final String name;
    private final String description;
    private final String type;
    private final Map<String, Object> parameters;
    private final Map<String, Object> configuration;

    // Caching and versioning support
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final AtomicReference<String> version = new AtomicReference<>("1.0.0");
    private final Random random = new Random();

    // Metrics service for performance monitoring
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile @Nullable MetricsService metricsService;

    /**
     * Create a new sampling model.
     * 
     * @param id the model ID
     * @param name the model name
     * @param description the model description
     * @param type the model type
     * @param parameters the model parameters
     * @param configuration the model configuration
     */
    public SamplingModel(String id, String name, String description, String type, Map<String, Object> parameters,
            Map<String, Object> configuration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.parameters = parameters;
        this.configuration = configuration;

        // Record model configuration tracking metrics
        recordModelConfiguration();
    }

    /**
     * Get the model ID.
     * 
     * @return the model ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the model name.
     * 
     * @return the model name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the model description.
     * 
     * @return the model description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the model type.
     * 
     * @return the model type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the model parameters.
     * 
     * @return the model parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Get the model configuration.
     * 
     * @return the model configuration
     */
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    /**
     * Generate a sample using this model.
     * 
     * @param input the input data
     * @return the generated sample
     */
    public Object generateSample(Map<String, Object> input) {
        long startTime = System.currentTimeMillis();
        logger.debug("Generating sample for model: {} with input: {}", id, input);

        // Check cache first
        String cacheKey = generateCacheKey(input);
        Object cachedResult = cache.get(cacheKey);
        if (cachedResult != null) {
            // Record cache hit metrics
            if (metricsService != null) {
                try {
                    metricsService.recordOperation("sampling_model", "cache_hit").withSuccess(true).withDuration(0L)
                            .withData("modelId", id).withData("cacheKey", cacheKey).record();
                } catch (Exception e) {
                    logger.warn("Failed to record sampling model cache hit metrics for model {}: {}", id,
                            e.getMessage());
                    // Graceful degradation: continue with sampling even if metrics recording fails
                }
            }
            logger.debug("Cache hit for key: {}", cacheKey);
            return cachedResult;
        }

        // Record cache miss metrics
        if (metricsService != null) {
            try {
                metricsService.recordOperation("sampling_model", "cache_miss").withSuccess(true).withDuration(0L)
                        .withData("modelId", id).withData("cacheKey", cacheKey).record();
            } catch (Exception e) {
                logger.warn("Failed to record sampling model cache miss metrics for model {}: {}", id, e.getMessage());
                // Graceful degradation: continue with sampling even if metrics recording fails
            }
        }

        // Execute sampling model
        Object result = executeSamplingModel(input);

        // Cache the result
        cacheResult(cacheKey, result);

        // Record sample generation metrics
        long duration = System.currentTimeMillis() - startTime;
        if (metricsService != null) {
            try {
                metricsService.recordOperation("sampling_model", "sample_generation").withSuccess(true)
                        .withDuration(Duration.ofMillis(duration).toNanos()).withData("modelId", id)
                        .withData("modelType", type).withData("durationMs", duration).record();
            } catch (Exception e) {
                logger.warn("Failed to record sampling model generation metrics for model {}: {}", id, e.getMessage());
                // Graceful degradation: continue with sampling even if metrics recording fails
            }
        }

        logger.debug("Generated sample for model: {} in {}ms", id, System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * Validate the model.
     * 
     * @return true if the model is valid
     */
    public boolean validate() {
        logger.debug("Validating model: {}", id);

        // Basic validation
        if (id == null || id.trim().isEmpty()) {
            logger.error("Model ID is null or empty");
            return false;
        }

        if (name == null || name.trim().isEmpty()) {
            logger.error("Model name is null or empty");
            return false;
        }

        if (type == null || type.trim().isEmpty()) {
            logger.error("Model type is null or empty");
            return false;
        }

        // Type-specific validation
        switch (type.toLowerCase()) {
            case "uniform":
                return validateUniformDistribution();
            case "normal":
                return validateNormalDistribution();
            case "exponential":
                return validateExponentialDistribution();
            case "poisson":
                return validatePoissonDistribution();
            case "custom":
                return validateCustomDistribution();
            default:
                logger.error("Unknown distribution type: {}", type);
                return false;
        }
    }

    /**
     * Execute the sampling model with the given input.
     * 
     * @param input the input data
     * @return the sampling result
     */
    public Object executeSamplingModel(Map<String, Object> input) {
        logger.debug("Executing sampling model: {} with type: {}", id, type);

        switch (type.toLowerCase()) {
            case "uniform":
                return generateUniformSample(input);
            case "normal":
                return generateNormalSample(input);
            case "exponential":
                return generateExponentialSample(input);
            case "poisson":
                return generatePoissonSample(input);
            case "custom":
                return generateCustomSample(input);
            default:
                logger.error("Unknown sampling type: {}", type);
                return null;
        }
    }

    /**
     * Generate a sample from uniform distribution.
     * 
     * @param input the input parameters
     * @return the generated sample
     */
    private Object generateUniformSample(Map<String, Object> input) {
        double min = getDoubleParameter(input, "min", 0.0);
        double max = getDoubleParameter(input, "max", 1.0);
        int count = getIntParameter(input, "count", 1);

        if (count == 1) {
            return min + (max - min) * random.nextDouble();
        } else {
            List<Double> samples = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                samples.add(min + (max - min) * random.nextDouble());
            }
            return samples;
        }
    }

    /**
     * Generate a sample from normal distribution.
     * 
     * @param input the input parameters
     * @return the generated sample
     */
    private Object generateNormalSample(Map<String, Object> input) {
        double mean = getDoubleParameter(input, "mean", 0.0);
        double stdDev = getDoubleParameter(input, "stdDev", 1.0);
        int count = getIntParameter(input, "count", 1);

        if (count == 1) {
            return mean + stdDev * random.nextGaussian();
        } else {
            List<Double> samples = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                samples.add(mean + stdDev * random.nextGaussian());
            }
            return samples;
        }
    }

    /**
     * Generate a sample from exponential distribution.
     * 
     * @param input the input parameters
     * @return the generated sample
     */
    private Object generateExponentialSample(Map<String, Object> input) {
        double lambda = getDoubleParameter(input, "lambda", 1.0);
        int count = getIntParameter(input, "count", 1);

        if (count == 1) {
            return -Math.log(1 - random.nextDouble()) / lambda;
        } else {
            List<Double> samples = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                samples.add(-Math.log(1 - random.nextDouble()) / lambda);
            }
            return samples;
        }
    }

    /**
     * Generate a sample from Poisson distribution.
     * 
     * @param input the input parameters
     * @return the generated sample
     */
    private Object generatePoissonSample(Map<String, Object> input) {
        double lambda = getDoubleParameter(input, "lambda", 1.0);
        int count = getIntParameter(input, "count", 1);

        if (count == 1) {
            return generatePoissonRandom(lambda);
        } else {
            List<Integer> samples = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                samples.add(generatePoissonRandom(lambda));
            }
            return samples;
        }
    }

    /**
     * Generate a custom sample based on the model configuration.
     * 
     * @param input the input parameters
     * @return the generated sample
     */
    private Object generateCustomSample(Map<String, Object> input) {
        // Custom sampling logic based on configuration
        String customType = (String) configuration.get("customType");
        if ("categorical".equals(customType)) {
            return generateCategoricalSample(input);
        } else if ("weighted".equals(customType)) {
            return generateWeightedSample(input);
        } else {
            logger.warn("Unknown custom sampling type: {}", customType);
            return null;
        }
    }

    /**
     * Generate a categorical sample.
     * 
     * @param input the input parameters
     * @return the generated sample
     */
    @SuppressWarnings("unchecked")
    private Object generateCategoricalSample(Map<String, Object> input) {
        List<String> categories = (List<String>) input.get("categories");
        if (categories == null || categories.isEmpty()) {
            return null;
        }

        int index = random.nextInt(categories.size());
        return categories.get(index);
    }

    /**
     * Generate a weighted sample.
     * 
     * @param input the input parameters
     * @return the generated sample
     */
    @SuppressWarnings("unchecked")
    private Object generateWeightedSample(Map<String, Object> input) {
        List<Object> items = (List<Object>) input.get("items");
        List<Double> weights = (List<Double>) input.get("weights");

        if (items == null || weights == null || items.size() != weights.size()) {
            return null;
        }

        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = random.nextDouble() * totalWeight;

        double cumulativeWeight = 0.0;
        for (int i = 0; i < items.size(); i++) {
            cumulativeWeight += weights.get(i);
            if (randomValue <= cumulativeWeight) {
                return items.get(i);
            }
        }

        return items.get(items.size() - 1);
    }

    /**
     * Generate a Poisson random variable.
     * 
     * @param lambda the Poisson parameter
     * @return the generated value
     */
    private int generatePoissonRandom(double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= random.nextDouble();
        } while (p > L);

        return k - 1;
    }

    /**
     * Cache a result with the given key.
     * 
     * @param key the cache key
     * @param result the result to cache
     */
    private void cacheResult(String key, Object result) {
        int maxCacheSize = getIntParameter(configuration, "maxCacheSize", 1000);
        if (cache.size() >= maxCacheSize) {
            // Simple LRU: remove oldest entry
            String oldestKey = cache.keySet().iterator().next();
            cache.remove(oldestKey);
        }
        cache.put(key, result);
    }

    /**
     * Generate a cache key from input parameters.
     * 
     * @param input the input parameters
     * @return the cache key
     */
    private String generateCacheKey(Map<String, Object> input) {
        return input.toString(); // Simple implementation
    }

    /**
     * Get model statistics.
     * 
     * @return model statistics
     */
    // Eliminated getStatistics() method after enhancing metric capture
    // Consumers should use MetricsService directly to access sampling model statistics:
    // - Sample generation: metricsService.getSnapshot(MetricKeys.custom("sampling-model", Map.of("operation",
    // "sample_generation", "modelId", id)))
    // - Cache hits: metricsService.getSnapshot(MetricKeys.custom("sampling-model", Map.of("operation", "cache_hit",
    // "modelId", id)))
    // - Cache misses: metricsService.getSnapshot(MetricKeys.custom("sampling-model", Map.of("operation", "cache_miss",
    // "modelId", id)))
    // - Configuration tracking: metricsService.getSnapshot(MetricKeys.custom("sampling-model", Map.of("operation",
    // "configuration-tracking", "modelId", id)))
    // - Version changes: metricsService.getSnapshot(MetricKeys.custom("sampling-model", Map.of("operation",
    // "version-change", "modelId", id)))
    // - Sample quality: metricsService.getSnapshot(MetricKeys.custom("sampling-model", Map.of("operation",
    // "sample-quality", "modelId", id)))
    // - Performance optimization: metricsService.getSnapshot(MetricKeys.custom("sampling-model", Map.of("operation",
    // "performance-optimization", "modelId", id)))

    /**
     * Update the model version.
     * 
     * @param newVersion the new version
     */
    public void updateVersion(String newVersion) {
        logger.info("Updating model version from {} to {}", version.get(), newVersion);
        version.set(newVersion);
    }

    // Validation methods for different distributions
    private boolean validateUniformDistribution() {
        double min = getDoubleParameter(parameters, "min", 0.0);
        double max = getDoubleParameter(parameters, "max", 1.0);
        return min < max;
    }

    private boolean validateNormalDistribution() {
        double stdDev = getDoubleParameter(parameters, "stdDev", 1.0);
        return stdDev > 0;
    }

    private boolean validateExponentialDistribution() {
        double lambda = getDoubleParameter(parameters, "lambda", 1.0);
        return lambda > 0;
    }

    private boolean validatePoissonDistribution() {
        double lambda = getDoubleParameter(parameters, "lambda", 1.0);
        return lambda > 0;
    }

    private boolean validateCustomDistribution() {
        String customType = (String) configuration.get("customType");
        return customType != null && !customType.trim().isEmpty();
    }

    // Helper methods
    private double getDoubleParameter(Map<String, Object> params, String key, double defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    private int getIntParameter(Map<String, Object> params, String key, int defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private double calculateCacheHitRate(long hits, long misses) {
        long total = hits + misses;
        if (total == 0) {
            return 0.0;
        }
        return (double) hits / total * 100.0;
    }

    /**
     * Record model configuration tracking metrics
     */
    public void recordModelConfiguration() {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("sampling-model", "configuration-tracking").withSuccess(true)
                        .withData("modelId", id).withData("modelType", type).withData("version", version.get())
                        .withData("parameterCount", parameters.size())
                        .withData("configurationCount", configuration.size()).record();
            } catch (Exception e) {
                logger.warn("Failed to record model configuration metrics for {}: {}", id, e.getMessage());
            }
        }
    }

    /**
     * Record model version change metrics
     */
    public void recordVersionChange(String oldVersion, String newVersion) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("sampling-model", "version-change").withSuccess(true)
                        .withData("modelId", id).withData("oldVersion", oldVersion).withData("newVersion", newVersion)
                        .withData("timestamp", System.currentTimeMillis()).record();
            } catch (Exception e) {
                logger.warn("Failed to record version change metrics for {}: {}", id, e.getMessage());
            }
        }
    }

    /**
     * Record sample quality metrics
     */
    public void recordSampleQuality(Object sample, double qualityScore, Map<String, Object> qualityMetrics) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("sampling-model", "sample-quality").withSuccess(qualityScore > 0.7) // Good
                                                                                                                   // quality
                                                                                                                   // threshold
                        .withData("modelId", id).withData("qualityScore", qualityScore)
                        .withData("sampleSize", sample != null ? sample.toString().length() : 0)
                        .withData("qualityMetrics", qualityMetrics.toString()).record();
            } catch (Exception e) {
                logger.warn("Failed to record sample quality metrics for {}: {}", id, e.getMessage());
            }
        }
    }

    /**
     * Record model performance optimization metrics
     */
    public void recordPerformanceOptimization(String optimizationType, double improvementPercentage,
            long executionTimeBefore, long executionTimeAfter) {
        if (metricsService != null) {
            try {
                metricsService.recordOperation("sampling-model", "performance-optimization")
                        .withSuccess(improvementPercentage > 0).withData("modelId", id)
                        .withData("optimizationType", optimizationType)
                        .withData("improvementPercentage", improvementPercentage)
                        .withData("executionTimeBefore", executionTimeBefore)
                        .withData("executionTimeAfter", executionTimeAfter).record();
            } catch (Exception e) {
                logger.warn("Failed to record performance optimization metrics for {}: {}", id, e.getMessage());
            }
        }
    }
}
