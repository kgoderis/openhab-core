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
package org.openhab.core.ai.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;

/**
 * Unit tests for AIBundleActivator
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AIBundleActivatorTest {

    @Mock
    private BundleContext bundleContext;

    private AIBundleActivator activator;

    @BeforeEach
    void setUp() {
        activator = new AIBundleActivator();
    }

    @Test
    void testBundleActivatorCreation() {
        assertNotNull(activator);
    }

    @Test
    void testStartBundle() throws Exception {
        // When
        activator.start(bundleContext);

        // Then
        // Should not throw any exceptions
        assertDoesNotThrow(() -> activator.start(bundleContext));
    }

    @Test
    void testStopBundle() throws Exception {
        // Given
        activator.start(bundleContext);

        // When
        activator.stop(bundleContext);

        // Then
        // Should not throw any exceptions
        assertDoesNotThrow(() -> activator.stop(bundleContext));
    }

    @Test
    void testGetBundleContextBeforeStart() {
        // When
        BundleContext context = AIBundleActivator.getBundleContext();

        // Then
        assertNull(context);
    }

    @Test
    void testGetBundleContextAfterStart() throws Exception {
        // Given
        activator.start(bundleContext);

        // When
        BundleContext context = AIBundleActivator.getBundleContext();

        // Then
        assertEquals(bundleContext, context);
    }

    @Test
    void testGetBundleContextAfterStop() throws Exception {
        // Given
        activator.start(bundleContext);
        activator.stop(bundleContext);

        // When
        BundleContext context = AIBundleActivator.getBundleContext();

        // Then
        assertNull(context);
    }

    @Test
    void testMultipleStartStopCycles() throws Exception {
        // When/Then
        assertDoesNotThrow(() -> {
            activator.start(bundleContext);
            activator.stop(bundleContext);
            activator.start(bundleContext);
            activator.stop(bundleContext);
        });
    }

    @Test
    void testStartWithNullContext() throws Exception {
        // When/Then
        assertDoesNotThrow(() -> activator.start(null));
    }

    @Test
    void testStopWithNullContext() throws Exception {
        // When/Then
        assertDoesNotThrow(() -> activator.stop(null));
    }
}
