package org.openhab.core.ai.reasoning.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class ModelSelection {
    private final String selectionId;
    private final String taskId;
    private final String selectedModelId;
    private final ModelScore selectedModelScore;
    private final Map<String, ModelScore> allScores;
    private final long timestamp;

    public ModelSelection(String selectionId, String taskId, String selectedModelId, ModelScore selectedModelScore,
            Map<String, ModelScore> allScores, long timestamp) {
        this.selectionId = selectionId;
        this.taskId = taskId;
        this.selectedModelId = selectedModelId;
        this.selectedModelScore = selectedModelScore;
        this.allScores = new ConcurrentHashMap<>(allScores);
        this.timestamp = timestamp;
    }

    public String getSelectionId() {
        return selectionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getSelectedModelId() {
        return selectedModelId;
    }

    public ModelScore getSelectedModelScore() {
        return selectedModelScore;
    }

    public Map<String, ModelScore> getAllScores() {
        return new ConcurrentHashMap<>(allScores);
    }

    public long getTimestamp() {
        return timestamp;
    }
}
