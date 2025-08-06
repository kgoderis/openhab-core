package org.openhab.core.ai.tool;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.tool.dto.Prompt;

/**
 * Unit tests for the Prompt class.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class PromptTest {

    @Test
    public void testPromptCreation() {
        // Given
        String name = "test_prompt";
        String description = "A test prompt";
        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("arg1", "First argument", true),
                new Prompt.PromptArgument("arg2", "Second argument", false));

        // When
        Prompt prompt = new Prompt(name, description, arguments);

        // Then
        assertEquals(name, prompt.getName());
        assertEquals(description, prompt.getDescription());
        assertEquals(arguments, prompt.getArguments());
        assertEquals(2, prompt.getArguments().size());
    }

    @Test
    public void testPromptArgumentCreation() {
        // Given
        String name = "test_arg";
        String description = "A test argument";
        boolean required = true;

        // When
        Prompt.PromptArgument argument = new Prompt.PromptArgument(name, description, required);

        // Then
        assertEquals(name, argument.getName());
        assertEquals(description, argument.getDescription());
        assertEquals(required, argument.isRequired());
    }

    @Test
    public void testPromptToString() {
        // Given
        String name = "test_prompt";
        String description = "A test prompt";
        List<Prompt.PromptArgument> arguments = List.of(new Prompt.PromptArgument("arg1", "First argument", true));

        // When
        Prompt prompt = new Prompt(name, description, arguments);
        String result = prompt.toString();

        // Then
        assertTrue(result.contains("name='" + name + "'"));
        assertTrue(result.contains("description='" + description + "'"));
        assertTrue(result.contains("arguments="));
    }

    @Test
    public void testPromptArgumentToString() {
        // Given
        String name = "test_arg";
        String description = "A test argument";
        boolean required = true;

        // When
        Prompt.PromptArgument argument = new Prompt.PromptArgument(name, description, required);
        String result = argument.toString();

        // Then
        assertTrue(result.contains("name='" + name + "'"));
        assertTrue(result.contains("description='" + description + "'"));
        assertTrue(result.contains("required=" + required));
    }
}
