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
package org.openhab.core.ai.reasoning.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provider abstraction for pluggable memory backends (e.g., DB, vector store).
 *
 * <p>Implementations supply storage and search capabilities for the unified
 * memory system.</p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public interface MemoryProvider {
}


