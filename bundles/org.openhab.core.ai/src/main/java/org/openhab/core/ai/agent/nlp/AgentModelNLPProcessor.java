package org.openhab.core.ai.agent.nlp;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.context.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Natural Language Processing processor for intelligent agents.
 * 
 * This class provides NLP capabilities for goal interpretation, intent recognition,
 * and semantic understanding to support action plan generation from natural language.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class AgentModelNLPProcessor {

    private final Logger logger = LoggerFactory.getLogger(AgentModelNLPProcessor.class);

    /**
     * Process natural language input to extract goals and intents.
     * 
     * @param input the natural language input
     * @param context the execution context
     * @return the processed NLP result
     */
    public AgentModelNLPResult processInput(String input, ExecutionContext context) {
        logger.debug("Processing NLP input: {}", input);

        // Extract goals from the input
        List<String> goals = extractGoals(input);

        // Recognize intents
        List<AgentModelIntent> intents = recognizeIntents(input, context);

        // Perform semantic analysis
        AgentModelSemanticAnalysis semanticAnalysis = performSemanticAnalysis(input, context);

        // Generate structured representation
        Map<String, Object> structuredData = generateStructuredData(input, goals, intents, semanticAnalysis);

        return AgentModelNLPResult.builder().withOriginalInput(input).withGoals(goals).withIntents(intents)
                .withSemanticAnalysis(semanticAnalysis).withStructuredData(structuredData)
                .withConfidence(calculateConfidence(goals, intents, semanticAnalysis)).build();
    }

    /**
     * Extract goals from natural language input.
     * 
     * @param input the natural language input
     * @return list of extracted goals
     */
    private List<String> extractGoals(String input) {
        // Simple goal extraction based on keywords and patterns
        List<String> goals = new java.util.ArrayList<>();

        String lowerInput = input.toLowerCase();

        // Look for goal indicators
        if (lowerInput.contains("turn on") || lowerInput.contains("switch on") || lowerInput.contains("activate")) {
            goals.add("activate_device");
        }

        if (lowerInput.contains("turn off") || lowerInput.contains("switch off") || lowerInput.contains("deactivate")) {
            goals.add("deactivate_device");
        }

        if (lowerInput.contains("set") && lowerInput.contains("temperature")) {
            goals.add("set_temperature");
        }

        if (lowerInput.contains("dim") || lowerInput.contains("brighten")) {
            goals.add("adjust_lighting");
        }

        if (lowerInput.contains("lock") || lowerInput.contains("unlock")) {
            goals.add("security_control");
        }

        if (lowerInput.contains("schedule") || lowerInput.contains("timer")) {
            goals.add("schedule_action");
        }

        // If no specific goals found, add a generic goal
        if (goals.isEmpty()) {
            goals.add("general_automation");
        }

        return goals;
    }

    /**
     * Recognize intents from natural language input.
     * 
     * @param input the natural language input
     * @param context the execution context
     * @return list of recognized intents
     */
    private List<AgentModelIntent> recognizeIntents(String input, ExecutionContext context) {
        List<AgentModelIntent> intents = new java.util.ArrayList<>();

        String lowerInput = input.toLowerCase();

        // Recognize device control intents
        if (lowerInput.contains("light") || lowerInput.contains("lamp") || lowerInput.contains("bulb")) {
            intents.add(AgentModelIntent.builder().withIntentType("device_control").withDeviceType("lighting")
                    .withAction("control").withConfidence(0.8).build());
        }

        if (lowerInput.contains("thermostat") || lowerInput.contains("heating") || lowerInput.contains("cooling")) {
            intents.add(AgentModelIntent.builder().withIntentType("device_control").withDeviceType("climate")
                    .withAction("control").withConfidence(0.8).build());
        }

        if (lowerInput.contains("door") || lowerInput.contains("lock")) {
            intents.add(AgentModelIntent.builder().withIntentType("security").withDeviceType("access_control")
                    .withAction("control").withConfidence(0.9).build());
        }

        // Recognize time-based intents
        if (lowerInput.contains("when") || lowerInput.contains("schedule") || lowerInput.contains("at")) {
            intents.add(AgentModelIntent.builder().withIntentType("temporal").withAction("schedule").withConfidence(0.7)
                    .build());
        }

        // Recognize conditional intents
        if (lowerInput.contains("if") || lowerInput.contains("when") || lowerInput.contains("then")) {
            intents.add(AgentModelIntent.builder().withIntentType("conditional").withAction("create_rule")
                    .withConfidence(0.8).build());
        }

        return intents;
    }

    /**
     * Perform semantic analysis of the input.
     * 
     * @param input the natural language input
     * @param context the execution context
     * @return semantic analysis result
     */
    private AgentModelSemanticAnalysis performSemanticAnalysis(String input, ExecutionContext context) {
        // Extract entities
        List<AgentModelEntity> entities = extractEntities(input);

        // Analyze sentiment
        AgentModelSentiment sentiment = analyzeSentiment(input);

        // Extract context information
        Map<String, Object> contextInfo = extractContextInfo(input, context);

        return AgentModelSemanticAnalysis.builder().withEntities(entities).withSentiment(sentiment)
                .withContextInfo(contextInfo).withLanguage("en") // Default to English for now
                .withConfidence(0.7).build();
    }

    /**
     * Extract entities from the input.
     * 
     * @param input the natural language input
     * @return list of extracted entities
     */
    private List<AgentModelEntity> extractEntities(String input) {
        List<AgentModelEntity> entities = new java.util.ArrayList<>();

        String lowerInput = input.toLowerCase();

        // Extract device names
        if (lowerInput.contains("living room")) {
            entities.add(AgentModelEntity.builder().withEntityType("location").withValue("living_room")
                    .withConfidence(0.9).build());
        }

        if (lowerInput.contains("bedroom")) {
            entities.add(AgentModelEntity.builder().withEntityType("location").withValue("bedroom").withConfidence(0.9)
                    .build());
        }

        if (lowerInput.contains("kitchen")) {
            entities.add(AgentModelEntity.builder().withEntityType("location").withValue("kitchen").withConfidence(0.9)
                    .build());
        }

        // Extract time entities
        if (lowerInput.contains("morning") || lowerInput.contains("evening") || lowerInput.contains("night")) {
            entities.add(AgentModelEntity.builder().withEntityType("time_period")
                    .withValue(extractTimePeriod(lowerInput)).withConfidence(0.8).build());
        }

        // Extract numeric values
        java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher matcher = numberPattern.matcher(input);
        while (matcher.find()) {
            entities.add(AgentModelEntity.builder().withEntityType("number").withValue(matcher.group())
                    .withConfidence(1.0).build());
        }

        return entities;
    }

    /**
     * Extract time period from input.
     * 
     * @param input the lowercase input
     * @return the time period
     */
    private String extractTimePeriod(String input) {
        if (input.contains("morning")) {
            return "morning";
        } else if (input.contains("evening")) {
            return "evening";
        } else if (input.contains("night")) {
            return "night";
        } else if (input.contains("afternoon")) {
            return "afternoon";
        }
        return "unknown";
    }

    /**
     * Analyze sentiment of the input.
     * 
     * @param input the natural language input
     * @return sentiment analysis result
     */
    private AgentModelSentiment analyzeSentiment(String input) {
        String lowerInput = input.toLowerCase();

        // Simple sentiment analysis based on keywords
        double positiveScore = 0.0;
        double negativeScore = 0.0;

        // Positive indicators
        if (lowerInput.contains("please") || lowerInput.contains("thank")) {
            positiveScore += 0.3;
        }

        if (lowerInput.contains("comfortable") || lowerInput.contains("nice") || lowerInput.contains("good")) {
            positiveScore += 0.4;
        }

        // Negative indicators
        if (lowerInput.contains("cold") || lowerInput.contains("hot") || lowerInput.contains("uncomfortable")) {
            negativeScore += 0.4;
        }

        if (lowerInput.contains("urgent") || lowerInput.contains("emergency")) {
            negativeScore += 0.5;
        }

        // Determine overall sentiment
        String sentiment = "neutral";
        if (positiveScore > negativeScore && positiveScore > 0.2) {
            sentiment = "positive";
        } else if (negativeScore > positiveScore && negativeScore > 0.2) {
            sentiment = "negative";
        }

        return AgentModelSentiment.builder().withSentiment(sentiment).withPositiveScore(positiveScore)
                .withNegativeScore(negativeScore).withNeutralScore(1.0 - positiveScore - negativeScore).build();
    }

    /**
     * Extract context information from input and execution context.
     * 
     * @param input the natural language input
     * @param context the execution context
     * @return context information map
     */
    private Map<String, Object> extractContextInfo(String input, ExecutionContext context) {
        Map<String, Object> contextInfo = new java.util.HashMap<>();

        // Add user context
        contextInfo.put("user_id", context.getClientId());
        contextInfo.put("session_id", context.getSessionId());
        contextInfo.put("protocol", context.getProtocol());

        // Add time context
        contextInfo.put("timestamp", java.time.Instant.now());

        // Add input length
        contextInfo.put("input_length", input.length());

        return contextInfo;
    }

    /**
     * Generate structured data from NLP processing results.
     * 
     * @param input the original input
     * @param goals the extracted goals
     * @param intents the recognized intents
     * @param semanticAnalysis the semantic analysis
     * @return structured data map
     */
    private Map<String, Object> generateStructuredData(String input, List<String> goals, List<AgentModelIntent> intents,
            AgentModelSemanticAnalysis semanticAnalysis) {
        Map<String, Object> structuredData = new java.util.HashMap<>();

        structuredData.put("original_input", input);
        structuredData.put("goals", goals);
        structuredData.put("intents", intents.stream().map(AgentModelIntent::getIntentType).toList());
        structuredData.put("entities", semanticAnalysis.getEntities().stream()
                .map(entity -> Map.of("type", entity.getEntityType(), "value", entity.getValue())).toList());
        structuredData.put("sentiment", semanticAnalysis.getSentiment().getSentiment());
        structuredData.put("language", semanticAnalysis.getLanguage());

        return structuredData;
    }

    /**
     * Calculate confidence score for the NLP processing.
     * 
     * @param goals the extracted goals
     * @param intents the recognized intents
     * @param semanticAnalysis the semantic analysis
     * @return confidence score
     */
    private double calculateConfidence(List<String> goals, List<AgentModelIntent> intents,
            AgentModelSemanticAnalysis semanticAnalysis) {
        double confidence = 0.5; // Base confidence

        // Increase confidence based on goal extraction
        if (!goals.isEmpty()) {
            confidence += 0.2;
        }

        // Increase confidence based on intent recognition
        if (!intents.isEmpty()) {
            confidence += 0.2;
        }

        // Increase confidence based on entity extraction
        if (!semanticAnalysis.getEntities().isEmpty()) {
            confidence += 0.1;
        }

        return Math.min(confidence, 1.0);
    }
}
