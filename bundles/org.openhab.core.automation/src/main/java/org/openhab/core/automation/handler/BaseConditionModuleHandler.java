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
package org.openhab.core.automation.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.Condition;

/**
 * This is a base class that can be used by ConditionModuleHandler implementations
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class BaseConditionModuleHandler extends BaseModuleHandler<Condition> implements ConditionHandler {

    public BaseConditionModuleHandler(Condition module) {
        super(module);
    }
}
