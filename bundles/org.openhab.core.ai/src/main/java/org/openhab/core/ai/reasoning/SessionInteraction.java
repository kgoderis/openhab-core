package org.openhab.core.ai.reasoning;

import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SessionInteraction {
    private final String input;
    private final String response;
    private final Instant timestamp;

    public SessionInteraction(String input, String response, Instant timestamp) {
        this.input = Objects.requireNonNull(input, "Input cannot be null");
        this.response = Objects.requireNonNull(response, "Response cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
    }

    public String getInput() { return input; }
    public String getResponse() { return response; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SessionInteraction other = (SessionInteraction) obj;
        return Objects.equals(input, other.input) && Objects.equals(response, other.response)
                && Objects.equals(timestamp, other.timestamp);
    }

    @Override
    public int hashCode() { return Objects.hash(input, response, timestamp); }

    @Override
    public String toString() { return "SessionInteraction [input=" + input + ", response=" + response + ", timestamp=" + timestamp + "]"; }
}
