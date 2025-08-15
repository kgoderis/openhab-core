package org.openhab.core.ai.events;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Correlation pattern with a weight for influence on confidence calculation.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class CorrelationPattern {

    private final String id;
    private final Pattern pattern;
    private final double weight;

    public CorrelationPattern(String id, Pattern pattern, double weight) {
        this.id = id;
        this.pattern = pattern;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public double getWeight() {
        return weight;
    }
}
