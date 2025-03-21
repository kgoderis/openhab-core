/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.core.semantics;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.semantics.model.property.Humidity;
import org.openhab.core.semantics.model.property.Temperature;

/**
 * These are tests for {@link SemanticsPredicates}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class SemanticsPredicatesTest {

    private @NonNullByDefault({}) GroupItem locationItem;
    private @NonNullByDefault({}) GroupItem equipmentItem;
    private @NonNullByDefault({}) GenericItem pointItem;

    @BeforeEach
    public void setup() {
        CoreItemFactory itemFactory = new CoreItemFactory(mock(UnitProvider.class));

        locationItem = new GroupItem("TestBathRoom");
        locationItem.addTag("Bathroom");

        equipmentItem = new GroupItem("Test08");
        equipmentItem.addTag("CleaningRobot");

        pointItem = itemFactory.createItem(CoreItemFactory.NUMBER, "TestTemperature");
        pointItem.addTag("Measurement");
        pointItem.addTag("Temperature");
    }

    @Test
    public void testIsLocation() {
        assertTrue(SemanticsPredicates.isLocation().test(locationItem));
        assertFalse(SemanticsPredicates.isLocation().test(equipmentItem));
        assertFalse(SemanticsPredicates.isLocation().test(pointItem));
    }

    @Test
    public void testIsEquipment() {
        assertFalse(SemanticsPredicates.isEquipment().test(locationItem));
        assertTrue(SemanticsPredicates.isEquipment().test(equipmentItem));
        assertFalse(SemanticsPredicates.isEquipment().test(pointItem));
    }

    @Test
    public void testIsPoint() {
        assertFalse(SemanticsPredicates.isPoint().test(locationItem));
        assertFalse(SemanticsPredicates.isPoint().test(equipmentItem));
        assertTrue(SemanticsPredicates.isPoint().test(pointItem));
    }

    @Test
    public void testRelatesTo() {
        assertFalse(SemanticsPredicates.relatesTo(Temperature.class).test(locationItem));
        assertFalse(SemanticsPredicates.relatesTo(Temperature.class).test(equipmentItem));
        assertTrue(SemanticsPredicates.relatesTo(Temperature.class).test(pointItem));
        assertFalse(SemanticsPredicates.relatesTo(Humidity.class).test(equipmentItem));
    }
}
