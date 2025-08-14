package org.openhab.core.ai.events;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class PredictionAlert {
    private final String type;
    private final String message;
    private final double value;

    public PredictionAlert(String type, String message, double value) {
        this.type = type;
        this.message = message;
        this.value = value;
    }

    public String getType() { return type; }
    public String getMessage() { return message; }
    public double getValue() { return value; }
}
