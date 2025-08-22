#!/bin/bash

# Fix API mismatches in DefaultReasoningStepAnalysisService.java

echo "Fixing API mismatches in DefaultReasoningStepAnalysisService.java..."

# Fix ResourceUsage method calls
sed -i '' 's/\.getProcessingTimeMs()/\.processingTimeMs()/g' src/main/java/org/openhab/core/ai/reasoning/engine/analysis/DefaultReasoningStepAnalysisService.java
sed -i '' 's/\.getTokensUsed()/\.getTotalTokens()/g' src/main/java/org/openhab/core/ai/reasoning/engine/analysis/DefaultReasoningStepAnalysisService.java
sed -i '' 's/\.getCostCents()/\.costUsd() * 100.0/g' src/main/java/org/openhab/core/ai/reasoning/engine/analysis/DefaultReasoningStepAnalysisService.java

# Fix ValidationInfo method calls
sed -i '' 's/\.getScore()/\.validationScore()/g' src/main/java/org/openhab/core/ai/reasoning/engine/analysis/DefaultReasoningStepAnalysisService.java

echo "API mismatches fixed!"
