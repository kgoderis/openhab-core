package org.openhab.core.ai.reasoning.learning;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.reasoning.metrics.LearningPerformanceMetrics;
import org.openhab.core.ai.reasoning.patterns.BehaviorPattern;
import org.openhab.core.ai.reasoning.policies.UserPreferenceModel;
import org.openhab.core.ai.reasoning.strategies.adaptation.AdaptiveStrategy;
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
    public List<FeedbackEntry> getFeedbackHistory(String userId, int limit) {
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
        return new LearningPerformanceMetricsBuilder().totalLearningEvents(totalLearningEvents.get())
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

    /* Extracted: org.openhab.core.ai.reasoning.LearningPerformanceMetrics */

    // Extracted: org.openhab.core.ai.reasoning.UserPreferenceModel

    // Extracted: org.openhab.core.ai.reasoning.BehaviorPattern (with nested PatternEntry)

    // Extracted: org.openhab.core.ai.reasoning.FeedbackHistory (with nested FeedbackEntry)

    // Extracted: org.openhab.core.ai.reasoning.AdaptiveStrategy (with nested StrategyEntry)
}
