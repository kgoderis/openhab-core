/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.tool;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.tool.prompts.api.dto.Prompt;
import org.openhab.core.ai.tool.prompts.api.dto.PromptArgument;

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
        List<PromptArgument> arguments = List.of(new PromptArgument("arg1", "First argument", true),
                new PromptArgument("arg2", "Second argument", false));

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
        PromptArgument argument = new PromptArgument(name, description, required);

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
        List<PromptArgument> arguments = List.of(new PromptArgument("arg1", "First argument", true));

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
        PromptArgument argument = new PromptArgument(name, description, required);
        String result = argument.toString();

        // Then
        assertTrue(result.contains("name='" + name + "'"));
        assertTrue(result.contains("description='" + description + "'"));
        assertTrue(result.contains("required=" + required));
    }
}
