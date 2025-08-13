# Agent-Model Performance Optimization Guide

This guide provides comprehensive strategies and techniques for optimizing the performance of the agent-model integration framework in the openHAB AI bundle.

## Performance Metrics and Monitoring

### Key Performance Indicators (KPIs)

```java
public class PerformanceMetrics {
    
    // Response Time Metrics
    private final AtomicLong totalResponseTime;
    private final AtomicLong minResponseTime;
    private final AtomicLong maxResponseTime;
    private final AtomicLong requestCount;
    
    // Throughput Metrics
    private final AtomicLong requestsPerSecond;
    private final AtomicLong concurrentRequests;
    
    // Error Metrics
    private final AtomicLong errorCount;
    private final AtomicLong timeoutCount;
    
    // Resource Metrics
    private final AtomicLong memoryUsage;
    private final AtomicLong cpuUsage;
    
    public PerformanceSnapshot getSnapshot() {
        long total = totalResponseTime.get();
        long count = requestCount.get();
        
        return PerformanceSnapshot.builder()
            .averageResponseTime(count > 0 ? (double) total / count : 0.0)
            .minResponseTime(minResponseTime.get())
            .maxResponseTime(maxResponseTime.get())
            .requestsPerSecond(requestsPerSecond.get())
            .concurrentRequests(concurrentRequests.get())
            .errorRate(count > 0 ? (double) errorCount.get() / count : 0.0)
            .timeoutRate(count > 0 ? (double) timeoutCount.get() / count : 0.0)
            .memoryUsage(memoryUsage.get())
            .cpuUsage(cpuUsage.get())
            .build();
    }
}

public class PerformanceSnapshot {
    private final double averageResponseTime;
    private final long minResponseTime;
    private final long maxResponseTime;
    private final long requestsPerSecond;
    private final long concurrentRequests;
    private final double errorRate;
    private final double timeoutRate;
    private final long memoryUsage;
    private final long cpuUsage;
    
    // Builder and getters
}
```

### Real-time Monitoring

```java
@Component
public class PerformanceMonitor {
    
    private final PerformanceMetrics metrics;
    private final ScheduledExecutorService scheduler;
    private final List<PerformanceListener> listeners;
    
    public PerformanceMonitor() {
        this.metrics = new PerformanceMetrics();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.listeners = new CopyOnWriteArrayList<>();
        
        // Start monitoring
        scheduler.scheduleAtFixedRate(this::collectMetrics, 0, 1, TimeUnit.MINUTES);
    }
    
    private void collectMetrics() {
        PerformanceSnapshot snapshot = metrics.getSnapshot();
        
        // Notify listeners
        for (PerformanceListener listener : listeners) {
            listener.onMetricsUpdate(snapshot);
        }
        
        // Log critical metrics
        if (snapshot.getErrorRate() > 0.05) { // 5% error rate threshold
            logger.warn("High error rate detected: {}%", snapshot.getErrorRate() * 100);
        }
        
        if (snapshot.getAverageResponseTime() > 5000) { // 5 second threshold
            logger.warn("High response time detected: {}ms", snapshot.getAverageResponseTime());
        }
    }
    
    public void addListener(PerformanceListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(PerformanceListener listener) {
        listeners.remove(listener);
    }
}

public interface PerformanceListener {
    void onMetricsUpdate(PerformanceSnapshot snapshot);
}
```

## Caching Strategies

### Response Caching

```java
@Component
public class ResponseCache {
    
    private final Cache<String, CachedResponse> cache;
    private final CacheStats stats;
    
    public ResponseCache() {
        this.cache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .recordStats()
            .build();
        
        this.stats = cache.stats();
    }
    
    public Optional<ModelResponse> getCachedResponse(String cacheKey) {
        CachedResponse cached = cache.getIfPresent(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return Optional.of(cached.getResponse());
        }
        return Optional.empty();
    }
    
    public void cacheResponse(String cacheKey, ModelResponse response, Duration ttl) {
        CachedResponse cached = new CachedResponse(response, Instant.now().plus(ttl));
        cache.put(cacheKey, cached);
    }
    
    public String generateCacheKey(String agentId, String prompt, Map<String, Object> context) {
        // Create a deterministic cache key
        String contextHash = context.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
        
        return agentId + ":" + prompt.hashCode() + ":" + contextHash.hashCode();
    }
    
    public CacheStats getStats() {
        return stats;
    }
}

public class CachedResponse {
    private final ModelResponse response;
    private final Instant expirationTime;
    
    public CachedResponse(ModelResponse response, Instant expirationTime) {
        this.response = response;
        this.expirationTime = expirationTime;
    }
    
    public ModelResponse getResponse() {
        return response;
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(expirationTime);
    }
}
```

### Context Caching

```java
@Component
public class ContextCache {
    
    private final Map<String, WeakReference<AgentModelContext>> contextCache;
    private final ScheduledExecutorService cleanupExecutor;
    
    public ContextCache() {
        this.contextCache = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newScheduledThreadPool(1);
        
        // Schedule periodic cleanup
        cleanupExecutor.scheduleAtFixedRate(this::cleanupCache, 1, 1, TimeUnit.HOURS);
    }
    
    public void cacheContext(String agentId, AgentModelContext context) {
        contextCache.put(agentId, new WeakReference<>(context));
    }
    
    public Optional<AgentModelContext> getContext(String agentId) {
        WeakReference<AgentModelContext> ref = contextCache.get(agentId);
        if (ref != null) {
            AgentModelContext context = ref.get();
            if (context != null) {
                return Optional.of(context);
            }
        }
        return Optional.empty();
    }
    
    private void cleanupCache() {
        contextCache.entrySet().removeIf(entry -> entry.getValue().get() == null);
        System.gc(); // Suggest garbage collection
    }
}
```

## Connection Pooling and Resource Management

### HTTP Connection Pooling

```java
@Component
public class ModelProviderConnectionPool {
    
    private final PoolingHttpClientConnectionManager connectionManager;
    private final CloseableHttpClient httpClient;
    private final RequestConfig requestConfig;
    
    public ModelProviderConnectionPool() {
        this.connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(50);
        connectionManager.setValidateAfterInactivity(30000); // 30 seconds
        
        this.requestConfig = RequestConfig.custom()
            .setConnectTimeout(10000) // 10 seconds
            .setSocketTimeout(30000)  // 30 seconds
            .setConnectionRequestTimeout(5000) // 5 seconds
            .build();
        
        this.httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
            .build();
    }
    
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }
    
    public PoolStats getPoolStats() {
        return connectionManager.getTotalStats();
    }
    
    @PreDestroy
    public void shutdown() {
        try {
            httpClient.close();
            connectionManager.close();
        } catch (IOException e) {
            logger.error("Error closing HTTP client", e);
        }
    }
}
```

### Thread Pool Management

```java
@Component
public class ReasoningThreadPool {
    
    private final ThreadPoolExecutor executor;
    private final BlockingQueue<Runnable> workQueue;
    
    public ReasoningThreadPool() {
        this.workQueue = new LinkedBlockingQueue<>(1000);
        
        this.executor = new ThreadPoolExecutor(
            10, // Core pool size
            50, // Maximum pool size
            60L, // Keep alive time
            TimeUnit.SECONDS,
            workQueue,
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(1);
                
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "reasoning-thread-" + counter.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );
        
        // Enable monitoring
        executor.setThreadFactory(new MonitoredThreadFactory(executor.getThreadFactory()));
    }
    
    public <T> CompletableFuture<T> submit(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }
    
    public ThreadPoolStats getStats() {
        return ThreadPoolStats.builder()
            .activeThreads(executor.getActiveCount())
            .poolSize(executor.getPoolSize())
            .corePoolSize(executor.getCorePoolSize())
            .maximumPoolSize(executor.getMaximumPoolSize())
            .queueSize(workQueue.size())
            .completedTasks(executor.getCompletedTaskCount())
            .totalTasks(executor.getTaskCount())
            .build();
    }
}

public class MonitoredThreadFactory implements ThreadFactory {
    
    private final ThreadFactory delegate;
    private final AtomicInteger createdThreads;
    private final AtomicInteger activeThreads;
    
    public MonitoredThreadFactory(ThreadFactory delegate) {
        this.delegate = delegate;
        this.createdThreads = new AtomicInteger(0);
        this.activeThreads = new AtomicInteger(0);
    }
    
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = delegate.newThread(() -> {
            activeThreads.incrementAndGet();
            try {
                r.run();
            } finally {
                activeThreads.decrementAndGet();
            }
        });
        
        createdThreads.incrementAndGet();
        return thread;
    }
    
    public int getCreatedThreads() {
        return createdThreads.get();
    }
    
    public int getActiveThreads() {
        return activeThreads.get();
    }
}
```

## Asynchronous Processing and Batching

### Request Batching

```java
@Component
public class RequestBatcher {
    
    private final BlockingQueue<BatchRequest> requestQueue;
    private final ScheduledExecutorService scheduler;
    private final int batchSize;
    private final Duration batchTimeout;
    
    public RequestBatcher(int batchSize, Duration batchTimeout) {
        this.requestQueue = new LinkedBlockingQueue<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.batchSize = batchSize;
        this.batchTimeout = batchTimeout;
        
        // Start batch processing
        scheduler.scheduleAtFixedRate(this::processBatch, 0, batchTimeout.toMillis(), TimeUnit.MILLISECONDS);
    }
    
    public CompletableFuture<ModelResponse> submitRequest(String agentId, String prompt, 
                                                         Map<String, Object> context, 
                                                         ModelParameters params) {
        CompletableFuture<ModelResponse> future = new CompletableFuture<>();
        BatchRequest request = new BatchRequest(agentId, prompt, context, params, future);
        
        try {
            requestQueue.put(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    private void processBatch() {
        List<BatchRequest> batch = new ArrayList<>();
        
        // Collect requests for batching
        requestQueue.drainTo(batch, batchSize);
        
        if (!batch.isEmpty()) {
            processBatchRequests(batch);
        }
    }
    
    private void processBatchRequests(List<BatchRequest> batch) {
        // Group requests by model provider for efficient batching
        Map<String, List<BatchRequest>> groupedRequests = batch.stream()
            .collect(Collectors.groupingBy(request -> request.getParams().getModel()));
        
        for (Map.Entry<String, List<BatchRequest>> entry : groupedRequests.entrySet()) {
            processModelBatch(entry.getKey(), entry.getValue());
        }
    }
    
    private void processModelBatch(String model, List<BatchRequest> requests) {
        // Implement batch processing logic for specific model
        // This could involve combining multiple prompts or using model-specific batching
        for (BatchRequest request : requests) {
            // Process individual request for now
            // In a real implementation, this would batch multiple requests together
            processSingleRequest(request);
        }
    }
    
    private void processSingleRequest(BatchRequest request) {
        // Process the request asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                ModelResponse response = modelProvider.reasonAsync(
                    request.getAgentId(), 
                    request.getPrompt(), 
                    request.getContext(), 
                    request.getParams()
                ).get(30, TimeUnit.SECONDS);
                
                request.getFuture().complete(response);
            } catch (Exception e) {
                request.getFuture().completeExceptionally(e);
            }
        });
    }
}

public class BatchRequest {
    private final String agentId;
    private final String prompt;
    private final Map<String, Object> context;
    private final ModelParameters params;
    private final CompletableFuture<ModelResponse> future;
    
    // Constructor and getters
}
```

### Stream Processing

```java
@Component
public class StreamProcessor {
    
    private final ExecutorService executor;
    private final int bufferSize;
    
    public StreamProcessor(int bufferSize) {
        this.executor = Executors.newWorkStealingPool();
        this.bufferSize = bufferSize;
    }
    
    public <T, R> CompletableFuture<List<R>> processStream(
            Stream<T> input, 
            Function<T, CompletableFuture<R>> processor,
            int parallelism) {
        
        return CompletableFuture.supplyAsync(() -> {
            return input
                .parallel()
                .map(item -> processor.apply(item))
                .collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        }, executor);
    }
    
    public <T> CompletableFuture<Void> processStreamAsync(
            Stream<T> input,
            Consumer<T> processor,
            int parallelism) {
        
        List<CompletableFuture<Void>> futures = input
            .parallel()
            .map(item -> CompletableFuture.runAsync(() -> processor.accept(item), executor))
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
```

## Memory Optimization

### Object Pooling

```java
@Component
public class ModelResponsePool {
    
    private final ObjectPool<ModelResponse> responsePool;
    
    public ModelResponsePool() {
        this.responsePool = new GenericObjectPool<>(new ModelResponseFactory());
    }
    
    public ModelResponse borrowResponse() throws Exception {
        return responsePool.borrowObject();
    }
    
    public void returnResponse(ModelResponse response) {
        try {
            responsePool.returnObject(response);
        } catch (Exception e) {
            logger.warn("Error returning response to pool", e);
        }
    }
    
    public PoolStats getStats() {
        return responsePool.getStats();
    }
}

public class ModelResponseFactory extends BasePooledObjectFactory<ModelResponse> {
    
    @Override
    public ModelResponse create() {
        return ModelResponse.builder().build();
    }
    
    @Override
    public PooledObject<ModelResponse> wrap(ModelResponse response) {
        return new DefaultPooledObject<>(response);
    }
    
    @Override
    public void passivateObject(PooledObject<ModelResponse> pooledObject) {
        ModelResponse response = pooledObject.getObject();
        // Reset response state
        response.setContent(null);
        response.setError(null);
        response.setTimestamp(null);
    }
}
```

### Memory-Efficient Data Structures

```java
public class MemoryEfficientContext {
    
    private final String agentId;
    private final String specialization;
    private final String domain;
    private final Map<String, Boolean> capabilities;
    private final Map<String, Object> constraints;
    private final Map<String, String> promptTemplates;
    private final Map<String, Object> preferences;
    
    // Use primitive arrays for large datasets
    private final int[] constraintValues;
    private final String[] capabilityNames;
    
    public MemoryEfficientContext(String agentId, String specialization, String domain) {
        this.agentId = agentId;
        this.specialization = specialization;
        this.domain = domain;
        this.capabilities = new ConcurrentHashMap<>();
        this.constraints = new ConcurrentHashMap<>();
        this.promptTemplates = new ConcurrentHashMap<>();
        this.preferences = new ConcurrentHashMap<>();
        
        // Pre-allocate arrays for better memory management
        this.constraintValues = new int[10];
        this.capabilityNames = new String[20];
    }
    
    // Implement memory-efficient operations
    public void addCapability(String capability, boolean enabled) {
        capabilities.put(capability, enabled);
    }
    
    public boolean hasCapability(String capability) {
        return capabilities.getOrDefault(capability, false);
    }
    
    // Use weak references for large objects that can be recreated
    private final Map<String, WeakReference<Object>> largeObjects = new ConcurrentHashMap<>();
    
    public void cacheLargeObject(String key, Object value) {
        largeObjects.put(key, new WeakReference<>(value));
    }
    
    public Optional<Object> getLargeObject(String key) {
        WeakReference<Object> ref = largeObjects.get(key);
        if (ref != null) {
            Object value = ref.get();
            if (value != null) {
                return Optional.of(value);
            } else {
                largeObjects.remove(key);
            }
        }
        return Optional.empty();
    }
}
```

## Load Balancing and Scaling

### Load Balancer

```java
@Component
public class ModelProviderLoadBalancer {
    
    private final List<ModelProviderEndpoint> endpoints;
    private final AtomicInteger currentIndex;
    private final LoadBalancingStrategy strategy;
    
    public ModelProviderLoadBalancer(List<ModelProviderEndpoint> endpoints, 
                                   LoadBalancingStrategy strategy) {
        this.endpoints = new CopyOnWriteArrayList<>(endpoints);
        this.currentIndex = new AtomicInteger(0);
        this.strategy = strategy;
    }
    
    public ModelProviderEndpoint getNextEndpoint() {
        switch (strategy) {
            case ROUND_ROBIN:
                return getRoundRobinEndpoint();
            case LEAST_CONNECTIONS:
                return getLeastConnectionsEndpoint();
            case WEIGHTED:
                return getWeightedEndpoint();
            default:
                return getRoundRobinEndpoint();
        }
    }
    
    private ModelProviderEndpoint getRoundRobinEndpoint() {
        int index = currentIndex.getAndIncrement() % endpoints.size();
        return endpoints.get(index);
    }
    
    private ModelProviderEndpoint getLeastConnectionsEndpoint() {
        return endpoints.stream()
            .min(Comparator.comparing(ModelProviderEndpoint::getActiveConnections))
            .orElse(endpoints.get(0));
    }
    
    private ModelProviderEndpoint getWeightedEndpoint() {
        // Implement weighted round-robin based on endpoint weights
        return endpoints.stream()
            .max(Comparator.comparing(ModelProviderEndpoint::getWeight))
            .orElse(endpoints.get(0));
    }
    
    public void addEndpoint(ModelProviderEndpoint endpoint) {
        endpoints.add(endpoint);
    }
    
    public void removeEndpoint(ModelProviderEndpoint endpoint) {
        endpoints.remove(endpoint);
    }
    
    public void updateEndpointHealth(String endpointId, boolean healthy) {
        endpoints.stream()
            .filter(endpoint -> endpoint.getId().equals(endpointId))
            .findFirst()
            .ifPresent(endpoint -> endpoint.setHealthy(healthy));
    }
}

public class ModelProviderEndpoint {
    private final String id;
    private final String url;
    private final int weight;
    private final AtomicInteger activeConnections;
    private volatile boolean healthy;
    
    // Constructor and methods
}

public enum LoadBalancingStrategy {
    ROUND_ROBIN,
    LEAST_CONNECTIONS,
    WEIGHTED
}
```

### Auto-scaling

```java
@Component
public class AutoScalingManager {
    
    private final ReasoningThreadPool threadPool;
    private final PerformanceMonitor performanceMonitor;
    private final ScheduledExecutorService scheduler;
    private final AutoScalingConfig config;
    
    public AutoScalingManager(ReasoningThreadPool threadPool, 
                            PerformanceMonitor performanceMonitor,
                            AutoScalingConfig config) {
        this.threadPool = threadPool;
        this.performanceMonitor = performanceMonitor;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.config = config;
        
        // Start auto-scaling
        scheduler.scheduleAtFixedRate(this::adjustScaling, 0, 30, TimeUnit.SECONDS);
    }
    
    private void adjustScaling() {
        PerformanceSnapshot snapshot = performanceMonitor.getLatestSnapshot();
        ThreadPoolStats poolStats = threadPool.getStats();
        
        // Scale up if needed
        if (shouldScaleUp(snapshot, poolStats)) {
            scaleUp();
        }
        
        // Scale down if needed
        if (shouldScaleDown(snapshot, poolStats)) {
            scaleDown();
        }
    }
    
    private boolean shouldScaleUp(PerformanceSnapshot snapshot, ThreadPoolStats poolStats) {
        return snapshot.getAverageResponseTime() > config.getMaxResponseTime() ||
               poolStats.getQueueSize() > config.getMaxQueueSize() ||
               snapshot.getErrorRate() > config.getMaxErrorRate();
    }
    
    private boolean shouldScaleDown(PerformanceSnapshot snapshot, ThreadPoolStats poolStats) {
        return snapshot.getAverageResponseTime() < config.getMinResponseTime() &&
               poolStats.getQueueSize() < config.getMinQueueSize() &&
               snapshot.getErrorRate() < config.getMinErrorRate();
    }
    
    private void scaleUp() {
        int currentMax = threadPool.getMaximumPoolSize();
        int newMax = Math.min(currentMax + config.getScaleUpIncrement(), config.getMaxPoolSize());
        threadPool.setMaximumPoolSize(newMax);
        
        logger.info("Scaling up thread pool from {} to {}", currentMax, newMax);
    }
    
    private void scaleDown() {
        int currentMax = threadPool.getMaximumPoolSize();
        int newMax = Math.max(currentMax - config.getScaleDownIncrement(), config.getMinPoolSize());
        threadPool.setMaximumPoolSize(newMax);
        
        logger.info("Scaling down thread pool from {} to {}", currentMax, newMax);
    }
}

public class AutoScalingConfig {
    private final int maxPoolSize;
    private final int minPoolSize;
    private final int scaleUpIncrement;
    private final int scaleDownIncrement;
    private final long maxResponseTime;
    private final long minResponseTime;
    private final int maxQueueSize;
    private final int minQueueSize;
    private final double maxErrorRate;
    private final double minErrorRate;
    
    // Constructor and getters
}
```

## Performance Testing and Benchmarking

### Benchmark Suite

```java
@Component
public class PerformanceBenchmark {
    
    private final SharedModelReasoningEngine reasoningEngine;
    private final PerformanceMonitor performanceMonitor;
    
    public PerformanceBenchmark(SharedModelReasoningEngine reasoningEngine,
                               PerformanceMonitor performanceMonitor) {
        this.reasoningEngine = reasoningEngine;
        this.performanceMonitor = performanceMonitor;
    }
    
    public BenchmarkResult runBenchmark(BenchmarkConfig config) {
        List<BenchmarkTest> tests = createTests(config);
        List<TestResult> results = new ArrayList<>();
        
        for (BenchmarkTest test : tests) {
            TestResult result = runTest(test);
            results.add(result);
        }
        
        return BenchmarkResult.builder()
            .tests(results)
            .summary(calculateSummary(results))
            .build();
    }
    
    private List<BenchmarkTest> createTests(BenchmarkConfig config) {
        List<BenchmarkTest> tests = new ArrayList<>();
        
        // Single request test
        tests.add(new SingleRequestTest(config.getSingleRequestCount()));
        
        // Concurrent requests test
        tests.add(new ConcurrentRequestTest(config.getConcurrentRequestCount()));
        
        // Load test
        tests.add(new LoadTest(config.getLoadTestDuration(), config.getLoadTestRate()));
        
        // Stress test
        tests.add(new StressTest(config.getStressTestDuration(), config.getStressTestRate()));
        
        return tests;
    }
    
    private TestResult runTest(BenchmarkTest test) {
        long startTime = System.currentTimeMillis();
        List<Long> responseTimes = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        test.execute(reasoningEngine, responseTimes, errors);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        return TestResult.builder()
            .testName(test.getName())
            .duration(duration)
            .requestCount(responseTimes.size())
            .errorCount(errors.size())
            .averageResponseTime(calculateAverage(responseTimes))
            .minResponseTime(calculateMin(responseTimes))
            .maxResponseTime(calculateMax(responseTimes))
            .percentile95(calculatePercentile(responseTimes, 95))
            .percentile99(calculatePercentile(responseTimes, 99))
            .throughput(calculateThroughput(responseTimes.size(), duration))
            .errors(errors)
            .build();
    }
    
    private BenchmarkSummary calculateSummary(List<TestResult> results) {
        return BenchmarkSummary.builder()
            .totalTests(results.size())
            .totalRequests(results.stream().mapToInt(TestResult::getRequestCount).sum())
            .totalErrors(results.stream().mapToInt(TestResult::getErrorCount).sum())
            .averageResponseTime(results.stream().mapToDouble(TestResult::getAverageResponseTime).average().orElse(0.0))
            .maxThroughput(results.stream().mapToDouble(TestResult::getThroughput).max().orElse(0.0))
            .build();
    }
}

public interface BenchmarkTest {
    String getName();
    void execute(SharedModelReasoningEngine engine, List<Long> responseTimes, List<String> errors);
}

public class SingleRequestTest implements BenchmarkTest {
    private final int requestCount;
    
    public SingleRequestTest(int requestCount) {
        this.requestCount = requestCount;
    }
    
    @Override
    public String getName() {
        return "Single Request Test (" + requestCount + " requests)";
    }
    
    @Override
    public void execute(SharedModelReasoningEngine engine, List<Long> responseTimes, List<String> errors) {
        for (int i = 0; i < requestCount; i++) {
            long startTime = System.currentTimeMillis();
            try {
                ModelResponse response = engine.reasonAsync("test-agent", "Test prompt " + i, 
                    new HashMap<>(), ModelParameters.builder().build()).get(30, TimeUnit.SECONDS);
                long endTime = System.currentTimeMillis();
                responseTimes.add(endTime - startTime);
            } catch (Exception e) {
                errors.add("Request " + i + " failed: " + e.getMessage());
            }
        }
    }
}

public class ConcurrentRequestTest implements BenchmarkTest {
    private final int concurrentCount;
    
    public ConcurrentRequestTest(int concurrentCount) {
        this.concurrentCount = concurrentCount;
    }
    
    @Override
    public String getName() {
        return "Concurrent Request Test (" + concurrentCount + " concurrent)";
    }
    
    @Override
    public void execute(SharedModelReasoningEngine engine, List<Long> responseTimes, List<String> errors) {
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        for (int i = 0; i < concurrentCount; i++) {
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    ModelResponse response = engine.reasonAsync("test-agent", "Concurrent test prompt", 
                        new HashMap<>(), ModelParameters.builder().build()).get(30, TimeUnit.SECONDS);
                    long endTime = System.currentTimeMillis();
                    return endTime - startTime;
                } catch (Exception e) {
                    errors.add("Concurrent request failed: " + e.getMessage());
                    return -1L;
                }
            });
            futures.add(future);
        }
        
        // Wait for all requests to complete
        for (CompletableFuture<Long> future : futures) {
            try {
                Long responseTime = future.get(60, TimeUnit.SECONDS);
                if (responseTime >= 0) {
                    responseTimes.add(responseTime);
                }
            } catch (Exception e) {
                errors.add("Future completion failed: " + e.getMessage());
            }
        }
    }
}
```

## Configuration Optimization

### Performance Configuration

```java
@Configuration
public class PerformanceConfiguration {
    
    @Bean
    public PerformanceConfig performanceConfig() {
        return PerformanceConfig.builder()
            .cacheEnabled(true)
            .cacheSize(10000)
            .cacheTtl(Duration.ofHours(1))
            .connectionPoolSize(200)
            .maxConcurrentRequests(100)
            .requestTimeout(Duration.ofSeconds(30))
            .retryAttempts(3)
            .retryDelay(Duration.ofSeconds(1))
            .loadBalancingStrategy(LoadBalancingStrategy.ROUND_ROBIN)
            .autoScalingEnabled(true)
            .monitoringEnabled(true)
            .build();
    }
    
    @Bean
    public ResponseCache responseCache(PerformanceConfig config) {
        if (config.isCacheEnabled()) {
            return new ResponseCache(config.getCacheSize(), config.getCacheTtl());
        }
        return new NoOpResponseCache();
    }
    
    @Bean
    public ModelProviderConnectionPool connectionPool(PerformanceConfig config) {
        return new ModelProviderConnectionPool(config.getConnectionPoolSize());
    }
    
    @Bean
    public ReasoningThreadPool threadPool(PerformanceConfig config) {
        return new ReasoningThreadPool(config.getMaxConcurrentRequests());
    }
    
    @Bean
    public AutoScalingManager autoScalingManager(ReasoningThreadPool threadPool,
                                                PerformanceMonitor performanceMonitor,
                                                PerformanceConfig config) {
        if (config.isAutoScalingEnabled()) {
            return new AutoScalingManager(threadPool, performanceMonitor, createAutoScalingConfig());
        }
        return null;
    }
    
    private AutoScalingConfig createAutoScalingConfig() {
        return AutoScalingConfig.builder()
            .maxPoolSize(100)
            .minPoolSize(10)
            .scaleUpIncrement(5)
            .scaleDownIncrement(2)
            .maxResponseTime(5000)
            .minResponseTime(100)
            .maxQueueSize(1000)
            .minQueueSize(10)
            .maxErrorRate(0.05)
            .minErrorRate(0.01)
            .build();
    }
}

public class PerformanceConfig {
    private final boolean cacheEnabled;
    private final int cacheSize;
    private final Duration cacheTtl;
    private final int connectionPoolSize;
    private final int maxConcurrentRequests;
    private final Duration requestTimeout;
    private final int retryAttempts;
    private final Duration retryDelay;
    private final LoadBalancingStrategy loadBalancingStrategy;
    private final boolean autoScalingEnabled;
    private final boolean monitoringEnabled;
    
    // Builder and getters
}
```

## Best Practices Summary

### 1. Caching Strategy
- **Response Caching**: Cache frequently requested responses
- **Context Caching**: Cache agent contexts with weak references
- **Connection Pooling**: Reuse HTTP connections
- **Object Pooling**: Reuse expensive objects

### 2. Asynchronous Processing
- **Non-blocking Operations**: Use CompletableFuture for all I/O
- **Request Batching**: Group similar requests
- **Stream Processing**: Process data streams efficiently
- **Backpressure Handling**: Implement proper flow control

### 3. Resource Management
- **Memory Management**: Use weak references and object pools
- **Thread Pool Management**: Configure appropriate pool sizes
- **Connection Management**: Implement connection pooling
- **Garbage Collection**: Optimize GC settings

### 4. Load Balancing and Scaling
- **Load Balancing**: Distribute requests across endpoints
- **Auto-scaling**: Automatically adjust resources
- **Health Checks**: Monitor endpoint health
- **Circuit Breakers**: Handle failing endpoints gracefully

### 5. Monitoring and Optimization
- **Performance Metrics**: Track key performance indicators
- **Real-time Monitoring**: Monitor system health
- **Benchmarking**: Regular performance testing
- **Profiling**: Identify bottlenecks

### 6. Configuration Optimization
- **Tunable Parameters**: Make performance parameters configurable
- **Environment-specific**: Optimize for different environments
- **Dynamic Configuration**: Support runtime configuration changes
- **Validation**: Validate configuration parameters

## Conclusion

This performance optimization guide provides comprehensive strategies for optimizing the agent-model integration framework. Key takeaways:

1. **Measure First**: Always measure performance before optimizing
2. **Cache Strategically**: Use caching for expensive operations
3. **Process Asynchronously**: Avoid blocking operations
4. **Manage Resources**: Implement proper resource management
5. **Scale Intelligently**: Use auto-scaling and load balancing
6. **Monitor Continuously**: Track performance metrics
7. **Test Regularly**: Run performance benchmarks
8. **Configure Optimally**: Tune configuration for your use case

Remember that performance optimization is an iterative process. Start with the most impactful optimizations and measure the results before proceeding with more complex changes.
