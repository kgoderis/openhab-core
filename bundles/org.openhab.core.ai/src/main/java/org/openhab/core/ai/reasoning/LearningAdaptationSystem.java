package org.openhab.core.ai.reasoning;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Learning and Adaptation System for AI agents
 * 
 * Implements user preference learning, behavior pattern recognition,
 * feedback integration, and adaptive reasoning strategies.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
@Component(service = LearningAdaptationSystem.class)
public class LearningAdaptationSystem {

    private final Logger logger = LoggerFactory.getLogger(LearningAdaptationSystem.class);

    // Data storage
    private final Map<String, UserPreferenceModel> userPreferences = new ConcurrentHashMap<>();
    private final Map<String, BehaviorPattern> behaviorPatterns = new ConcurrentHashMap<>();
    private final Map<String, FeedbackHistory> feedbackHistory = new ConcurrentHashMap<>();
    private final Map<String, AdaptiveStrategy> adaptiveStrategies = new ConcurrentHashMap<>();

    // Performance monitoring
    private final AtomicLong totalLearningEvents = new AtomicLong(0);
    private final AtomicLong totalPatternRecognitions = new AtomicLong(0);
    private final AtomicLong totalFeedbackIntegrations = new AtomicLong(0);
    private final AtomicLong totalStrategyAdaptations = new AtomicLong(0);

    // Thread safety
    private final ReadWriteLock preferenceLock = new ReentrantReadWriteLock();
    private final ReadWriteLock patternLock = new ReentrantReadWriteLock();
    private final ReadWriteLock feedbackLock = new ReentrantReadWriteLock();
    private final ReadWriteLock strategyLock = new ReentrantReadWriteLock();

    // Configuration
    private boolean enableLearning = true;
    private boolean enablePatternRecognition = true;
    private boolean enableFeedbackIntegration = true;
    private boolean enableStrategyAdaptation = true;
    private double learningRate = 0.1;
    private int maxPatternHistory = 1000;
    private int maxFeedbackHistory = 500;
    private Duration patternDetectionWindow = Duration.ofHours(24);

    @Activate
    public void activate() {
        logger.info("Learning and Adaptation System activated");
    }

    @Deactivate
    public void deactivate() {
        logger.info("Learning and Adaptation System deactivated");
    }

    /**
     * Learn from user interaction
     */
    public LearningResult learnFromInteraction(String agentId, String userId, String interactionType,
            Map<String, Object> interactionData, double feedbackScore) {
        if (!enableLearning) {
            return LearningResult.disabled("Learning is disabled");
        }

        try {
            preferenceLock.writeLock().lock();
            feedbackLock.writeLock().lock();

            // Update user preferences
            UserPreferenceModel preferences = userPreferences.computeIfAbsent(userId,
                    k -> new UserPreferenceModel(userId));
            preferences.learnFromInteraction(interactionType, interactionData, feedbackScore, learningRate);

            // Store feedback
            FeedbackHistory history = feedbackHistory.computeIfAbsent(userId, k -> new FeedbackHistory(userId));
            history.addFeedback(agentId, interactionType, feedbackScore, interactionData);

            // Trigger pattern recognition
            if (enablePatternRecognition) {
                recognizeBehaviorPatterns(userId, interactionType, interactionData);
            }

            totalLearningEvents.incrementAndGet();
            logger.debug("Learned from interaction: {} for user: {}", interactionType, userId);

            return LearningResult.success(preferences.getConfidence());
        } finally {
            preferenceLock.writeLock().unlock();
            feedbackLock.writeLock().unlock();
        }
    }

    /**
     * Recognize behavior patterns
     */
    public PatternRecognitionResult recognizeBehaviorPatterns(String userId, String interactionType,
            Map<String, Object> interactionData) {
        if (!enablePatternRecognition) {
            return PatternRecognitionResult.disabled("Pattern recognition is disabled");
        }

        try {
            patternLock.writeLock().lock();

            BehaviorPattern pattern = behaviorPatterns.computeIfAbsent(userId, k -> new BehaviorPattern(userId));
            boolean patternDetected = pattern.analyzeInteraction(interactionType, interactionData);

            if (patternDetected) {
                totalPatternRecognitions.incrementAndGet();
                logger.debug("Behavior pattern detected for user: {}", userId);
            }

            return PatternRecognitionResult.success(patternDetected, pattern.getConfidence());
        } finally {
            patternLock.writeLock().unlock();
        }
    }

    /**
     * Integrate user feedback
     */
    public FeedbackIntegrationResult integrateFeedback(String userId, String agentId, String feedbackType,
            String feedbackContent, double feedbackScore) {
        if (!enableFeedbackIntegration) {
            return FeedbackIntegrationResult.disabled("Feedback integration is disabled");
        }

        try {
            feedbackLock.writeLock().lock();

            FeedbackHistory history = feedbackHistory.computeIfAbsent(userId, k -> new FeedbackHistory(userId));
            history.addFeedback(agentId, feedbackType, feedbackScore, Map.of("content", feedbackContent));

            // Update user preferences based on feedback
            UserPreferenceModel preferences = userPreferences.get(userId);
            if (preferences != null) {
                preferences.updateFromFeedback(feedbackType, feedbackScore, learningRate);
            }

            totalFeedbackIntegrations.incrementAndGet();
            logger.debug("Integrated feedback for user: {} from agent: {}", userId, agentId);

            return FeedbackIntegrationResult.success(history.getFeedbackCount());
        } finally {
            feedbackLock.writeLock().unlock();
        }
    }

    /**
     * Adapt reasoning strategy
     */
    public StrategyAdaptationResult adaptStrategy(String agentId, String userId, String strategyType,
            Map<String, Object> strategyParameters) {
        if (!enableStrategyAdaptation) {
            return StrategyAdaptationResult.disabled("Strategy adaptation is disabled");
        }

        try {
            strategyLock.writeLock().lock();

            AdaptiveStrategy strategy = adaptiveStrategies.computeIfAbsent(agentId, k -> new AdaptiveStrategy(agentId));
            boolean adapted = strategy.adaptStrategy(strategyType, strategyParameters, learningRate);

            if (adapted) {
                totalStrategyAdaptations.incrementAndGet();
                logger.debug("Adapted strategy for agent: {} with user: {}", agentId, userId);
            }

            return StrategyAdaptationResult.success(adapted, strategy.getAdaptationCount());
        } finally {
            strategyLock.writeLock().unlock();
        }
    }

    /**
     * Get user preferences
     */
    public UserPreferenceModel getUserPreferences(String userId) {
        try {
            preferenceLock.readLock().lock();
            return userPreferences.get(userId);
        } finally {
            preferenceLock.readLock().unlock();
        }
    }

    /**
     * Get behavior patterns
     */
    public BehaviorPattern getBehaviorPatterns(String userId) {
        try {
            patternLock.readLock().lock();
            return behaviorPatterns.get(userId);
        } finally {
            patternLock.readLock().unlock();
        }
    }

    /**
     * Get feedback history
     */
    public List<FeedbackHistory.FeedbackEntry> getFeedbackHistory(String userId, int limit) {
        try {
            feedbackLock.readLock().lock();
            FeedbackHistory history = feedbackHistory.get(userId);
            if (history == null) {
                return Collections.emptyList();
            }
            return history.getRecentFeedback(limit);
        } finally {
            feedbackLock.readLock().unlock();
        }
    }

    /**
     * Get adaptive strategy
     */
    public AdaptiveStrategy getAdaptiveStrategy(String agentId) {
        try {
            strategyLock.readLock().lock();
            return adaptiveStrategies.get(agentId);
        } finally {
            strategyLock.readLock().unlock();
        }
    }

    /**
     * Get learning performance metrics
     */
    public LearningPerformanceMetrics getPerformanceMetrics() {
        return LearningPerformanceMetrics.builder().totalLearningEvents(totalLearningEvents.get())
                .totalPatternRecognitions(totalPatternRecognitions.get())
                .totalFeedbackIntegrations(totalFeedbackIntegrations.get())
                .totalStrategyAdaptations(totalStrategyAdaptations.get()).userPreferenceCount(userPreferences.size())
                .behaviorPatternCount(behaviorPatterns.size()).feedbackHistoryCount(feedbackHistory.size())
                .adaptiveStrategyCount(adaptiveStrategies.size()).build();
    }

    // Configuration methods
    public void setLearningRate(double learningRate) {
        this.learningRate = Math.max(0.0, Math.min(1.0, learningRate));
    }

    public void setEnableLearning(boolean enableLearning) {
        this.enableLearning = enableLearning;
    }

    public void setEnablePatternRecognition(boolean enablePatternRecognition) {
        this.enablePatternRecognition = enablePatternRecognition;
    }

    public void setEnableFeedbackIntegration(boolean enableFeedbackIntegration) {
        this.enableFeedbackIntegration = enableFeedbackIntegration;
    }

    public void setEnableStrategyAdaptation(boolean enableStrategyAdaptation) {
        this.enableStrategyAdaptation = enableStrategyAdaptation;
    }

    // Result classes
    public static class LearningResult {
        private final boolean success;
        private final String message;
        private final double confidence;

        private LearningResult(boolean success, String message, double confidence) {
            this.success = success;
            this.message = message;
            this.confidence = confidence;
        }

        public static LearningResult success(double confidence) {
            return new LearningResult(true, "Learning successful", confidence);
        }

        public static LearningResult disabled(String reason) {
            return new LearningResult(false, "Learning disabled: " + reason, 0.0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public double getConfidence() {
            return confidence;
        }
    }

    public static class PatternRecognitionResult {
        private final boolean success;
        private final String message;
        private final boolean patternDetected;
        private final double confidence;

        private PatternRecognitionResult(boolean success, String message, boolean patternDetected, double confidence) {
            this.success = success;
            this.message = message;
            this.patternDetected = patternDetected;
            this.confidence = confidence;
        }

        public static PatternRecognitionResult success(boolean patternDetected, double confidence) {
            return new PatternRecognitionResult(true, "Pattern recognition successful", patternDetected, confidence);
        }

        public static PatternRecognitionResult disabled(String reason) {
            return new PatternRecognitionResult(false, "Pattern recognition disabled: " + reason, false, 0.0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean isPatternDetected() {
            return patternDetected;
        }

        public double getConfidence() {
            return confidence;
        }
    }

    public static class FeedbackIntegrationResult {
        private final boolean success;
        private final String message;
        private final int feedbackCount;

        private FeedbackIntegrationResult(boolean success, String message, int feedbackCount) {
            this.success = success;
            this.message = message;
            this.feedbackCount = feedbackCount;
        }

        public static FeedbackIntegrationResult success(int feedbackCount) {
            return new FeedbackIntegrationResult(true, "Feedback integration successful", feedbackCount);
        }

        public static FeedbackIntegrationResult disabled(String reason) {
            return new FeedbackIntegrationResult(false, "Feedback integration disabled: " + reason, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getFeedbackCount() {
            return feedbackCount;
        }
    }

    public static class StrategyAdaptationResult {
        private final boolean success;
        private final String message;
        private final boolean adapted;
        private final int adaptationCount;

        private StrategyAdaptationResult(boolean success, String message, boolean adapted, int adaptationCount) {
            this.success = success;
            this.message = message;
            this.adapted = adapted;
            this.adaptationCount = adaptationCount;
        }

        public static StrategyAdaptationResult success(boolean adapted, int adaptationCount) {
            return new StrategyAdaptationResult(true, "Strategy adaptation successful", adapted, adaptationCount);
        }

        public static StrategyAdaptationResult disabled(String reason) {
            return new StrategyAdaptationResult(false, "Strategy adaptation disabled: " + reason, false, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public boolean isAdapted() {
            return adapted;
        }

        public int getAdaptationCount() {
            return adaptationCount;
        }
    }

    public static class LearningPerformanceMetrics {
        private final long totalLearningEvents;
        private final long totalPatternRecognitions;
        private final long totalFeedbackIntegrations;
        private final long totalStrategyAdaptations;
        private final int userPreferenceCount;
        private final int behaviorPatternCount;
        private final int feedbackHistoryCount;
        private final int adaptiveStrategyCount;

        private LearningPerformanceMetrics(Builder builder) {
            this.totalLearningEvents = builder.totalLearningEvents;
            this.totalPatternRecognitions = builder.totalPatternRecognitions;
            this.totalFeedbackIntegrations = builder.totalFeedbackIntegrations;
            this.totalStrategyAdaptations = builder.totalStrategyAdaptations;
            this.userPreferenceCount = builder.userPreferenceCount;
            this.behaviorPatternCount = builder.behaviorPatternCount;
            this.feedbackHistoryCount = builder.feedbackHistoryCount;
            this.adaptiveStrategyCount = builder.adaptiveStrategyCount;
        }

        public static Builder builder() {
            return new Builder();
        }

        public long getTotalLearningEvents() {
            return totalLearningEvents;
        }

        public long getTotalPatternRecognitions() {
            return totalPatternRecognitions;
        }

        public long getTotalFeedbackIntegrations() {
            return totalFeedbackIntegrations;
        }

        public long getTotalStrategyAdaptations() {
            return totalStrategyAdaptations;
        }

        public int getUserPreferenceCount() {
            return userPreferenceCount;
        }

        public int getBehaviorPatternCount() {
            return behaviorPatternCount;
        }

        public int getFeedbackHistoryCount() {
            return feedbackHistoryCount;
        }

        public int getAdaptiveStrategyCount() {
            return adaptiveStrategyCount;
        }

        public static class Builder {
            private long totalLearningEvents;
            private long totalPatternRecognitions;
            private long totalFeedbackIntegrations;
            private long totalStrategyAdaptations;
            private int userPreferenceCount;
            private int behaviorPatternCount;
            private int feedbackHistoryCount;
            private int adaptiveStrategyCount;

            public Builder totalLearningEvents(long totalLearningEvents) {
                this.totalLearningEvents = totalLearningEvents;
                return this;
            }

            public Builder totalPatternRecognitions(long totalPatternRecognitions) {
                this.totalPatternRecognitions = totalPatternRecognitions;
                return this;
            }

            public Builder totalFeedbackIntegrations(long totalFeedbackIntegrations) {
                this.totalFeedbackIntegrations = totalFeedbackIntegrations;
                return this;
            }

            public Builder totalStrategyAdaptations(long totalStrategyAdaptations) {
                this.totalStrategyAdaptations = totalStrategyAdaptations;
                return this;
            }

            public Builder userPreferenceCount(int userPreferenceCount) {
                this.userPreferenceCount = userPreferenceCount;
                return this;
            }

            public Builder behaviorPatternCount(int behaviorPatternCount) {
                this.behaviorPatternCount = behaviorPatternCount;
                return this;
            }

            public Builder feedbackHistoryCount(int feedbackHistoryCount) {
                this.feedbackHistoryCount = feedbackHistoryCount;
                return this;
            }

            public Builder adaptiveStrategyCount(int adaptiveStrategyCount) {
                this.adaptiveStrategyCount = adaptiveStrategyCount;
                return this;
            }

            public LearningPerformanceMetrics build() {
                return new LearningPerformanceMetrics(this);
            }
        }
    }

    // Internal data classes
    public static class UserPreferenceModel {
        private final String userId;
        private final Map<String, Double> preferences = new ConcurrentHashMap<>();
        private final Map<String, Integer> interactionCounts = new ConcurrentHashMap<>();
        private double confidence = 0.0;

        public UserPreferenceModel(String userId) {
            this.userId = userId;
        }

        public void learnFromInteraction(String interactionType, Map<String, Object> interactionData,
                double feedbackScore, double learningRate) {
            // Update preference for this interaction type
            double currentPreference = preferences.getOrDefault(interactionType, 0.5);
            double newPreference = currentPreference + learningRate * (feedbackScore - currentPreference);
            preferences.put(interactionType, Math.max(0.0, Math.min(1.0, newPreference)));

            // Update interaction count
            interactionCounts.put(interactionType, interactionCounts.getOrDefault(interactionType, 0) + 1);

            // Update confidence based on number of interactions
            updateConfidence();
        }

        public void updateFromFeedback(String feedbackType, double feedbackScore, double learningRate) {
            // Update preference based on feedback
            double currentPreference = preferences.getOrDefault(feedbackType, 0.5);
            double newPreference = currentPreference + learningRate * (feedbackScore - currentPreference);
            preferences.put(feedbackType, Math.max(0.0, Math.min(1.0, newPreference)));

            updateConfidence();
        }

        private void updateConfidence() {
            int totalInteractions = interactionCounts.values().stream().mapToInt(Integer::intValue).sum();
            confidence = Math.min(1.0, totalInteractions / 100.0); // Confidence increases with more interactions
        }

        public String getUserId() {
            return userId;
        }

        public Map<String, Double> getPreferences() {
            return Collections.unmodifiableMap(preferences);
        }

        public double getConfidence() {
            return confidence;
        }
    }

    public static class BehaviorPattern {
        private final String userId;
        private final List<PatternEntry> patterns = new ArrayList<>();
        private double confidence = 0.0;

        public BehaviorPattern(String userId) {
            this.userId = userId;
        }

        public boolean analyzeInteraction(String interactionType, Map<String, Object> interactionData) {
            // Simple pattern detection - can be enhanced with ML
            PatternEntry pattern = patterns.stream().filter(p -> interactionType.equals(p.getInteractionType()))
                    .findFirst().orElse(null);

            if (pattern == null) {
                pattern = new PatternEntry(interactionType);
                patterns.add(pattern);
            }

            boolean patternDetected = pattern.addInteraction(interactionData);
            updateConfidence();
            return patternDetected;
        }

        private void updateConfidence() {
            confidence = patterns.stream().mapToDouble(PatternEntry::getConfidence).average().orElse(0.0);
        }

        public String getUserId() {
            return userId;
        }

        public List<PatternEntry> getPatterns() {
            return Collections.unmodifiableList(patterns);
        }

        public double getConfidence() {
            return confidence;
        }

        public static class PatternEntry {
            private final String interactionType;
            private final List<Map<String, Object>> recentInteractions = new ArrayList<>();
            private double confidence = 0.0;

            public PatternEntry(String interactionType) {
                this.interactionType = interactionType;
            }

            public boolean addInteraction(Map<String, Object> interactionData) {
                recentInteractions.add(interactionData);

                // Keep only recent interactions
                if (recentInteractions.size() > 100) {
                    recentInteractions.remove(0);
                }

                // Simple pattern detection - check for repeated patterns
                boolean patternDetected = detectPattern();
                updateConfidence();
                return patternDetected;
            }

            private boolean detectPattern() {
                if (recentInteractions.size() < 3) {
                    return false;
                }

                // Check for repeated interaction patterns
                // This is a simplified implementation - can be enhanced with ML
                return recentInteractions.size() >= 5;
            }

            private void updateConfidence() {
                confidence = Math.min(1.0, recentInteractions.size() / 10.0);
            }

            public String getInteractionType() {
                return interactionType;
            }

            public double getConfidence() {
                return confidence;
            }
        }
    }

    public static class FeedbackHistory {
        private final String userId;
        private final List<FeedbackEntry> feedbackEntries = new ArrayList<>();

        public FeedbackHistory(String userId) {
            this.userId = userId;
        }

        public void addFeedback(String agentId, String feedbackType, double feedbackScore,
                Map<String, Object> feedbackData) {
            FeedbackEntry entry = new FeedbackEntry(agentId, feedbackType, feedbackScore, feedbackData, Instant.now());
            feedbackEntries.add(entry);

            // Keep only recent feedback
            if (feedbackEntries.size() > 500) {
                feedbackEntries.remove(0);
            }
        }

        public List<FeedbackEntry> getRecentFeedback(int limit) {
            List<FeedbackEntry> recent = new ArrayList<>(feedbackEntries);
            recent.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

            if (recent.size() > limit) {
                recent = recent.subList(0, limit);
            }

            return recent;
        }

        public int getFeedbackCount() {
            return feedbackEntries.size();
        }

        public String getUserId() {
            return userId;
        }

        public static class FeedbackEntry {
            private final String agentId;
            private final String feedbackType;
            private final double feedbackScore;
            private final Map<String, Object> feedbackData;
            private final Instant timestamp;

            public FeedbackEntry(String agentId, String feedbackType, double feedbackScore,
                    Map<String, Object> feedbackData, Instant timestamp) {
                this.agentId = agentId;
                this.feedbackType = feedbackType;
                this.feedbackScore = feedbackScore;
                this.feedbackData = feedbackData;
                this.timestamp = timestamp;
            }

            public String getAgentId() {
                return agentId;
            }

            public String getFeedbackType() {
                return feedbackType;
            }

            public double getFeedbackScore() {
                return feedbackScore;
            }

            public Map<String, Object> getFeedbackData() {
                return feedbackData;
            }

            public Instant getTimestamp() {
                return timestamp;
            }
        }
    }

    public static class AdaptiveStrategy {
        private final String agentId;
        private final Map<String, StrategyEntry> strategies = new ConcurrentHashMap<>();
        private int adaptationCount = 0;

        public AdaptiveStrategy(String agentId) {
            this.agentId = agentId;
        }

        public boolean adaptStrategy(String strategyType, Map<String, Object> strategyParameters, double learningRate) {
            StrategyEntry strategy = strategies.computeIfAbsent(strategyType, k -> new StrategyEntry(strategyType));
            boolean adapted = strategy.adapt(strategyParameters, learningRate);

            if (adapted) {
                adaptationCount++;
            }

            return adapted;
        }

        public int getAdaptationCount() {
            return adaptationCount;
        }

        public String getAgentId() {
            return agentId;
        }

        public static class StrategyEntry {
            private final String strategyType;
            private final Map<String, Object> parameters = new ConcurrentHashMap<>();
            private double effectiveness = 0.5;

            public StrategyEntry(String strategyType) {
                this.strategyType = strategyType;
            }

            public boolean adapt(Map<String, Object> newParameters, double learningRate) {
                boolean adapted = false;

                for (Map.Entry<String, Object> entry : newParameters.entrySet()) {
                    Object currentValue = parameters.get(entry.getKey());
                    Object newValue = entry.getValue();

                    if (!newValue.equals(currentValue)) {
                        parameters.put(entry.getKey(), newValue);
                        adapted = true;
                    }
                }

                return adapted;
            }

            public String getStrategyType() {
                return strategyType;
            }

            public Map<String, Object> getParameters() {
                return Collections.unmodifiableMap(parameters);
            }

            public double getEffectiveness() {
                return effectiveness;
            }
        }
    }
}
