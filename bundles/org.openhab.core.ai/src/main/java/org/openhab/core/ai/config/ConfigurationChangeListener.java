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
package org.openhab.core.ai.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener for configuration change events.
 * 
 * <p>
 * Components can implement this interface to be notified when configuration
 * values change. The listener will be called for all configuration changes
 * unless filtered by domain or key patterns.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface ConfigurationChangeListener {

    /**
     * Called when a configuration change occurs.
     * 
     * @param event the configuration change event
     */
    void onConfigurationChanged(ConfigurationChangeEvent event);

    /**
     * Gets the domains this listener is interested in.
     * 
     * <p>
     * If this returns null or an empty array, the listener will receive
     * events for all domains. If specific domains are returned, the listener
     * will only receive events for those domains.
     * </p>
     * 
     * @return array of domain names, or null/empty for all domains
     */
    default String[] getInterestedDomains() {
        return null; // Listen to all domains by default
    }

    /**
     * Gets the configuration key patterns this listener is interested in.
     * 
     * <p>
     * If this returns null or an empty array, the listener will receive
     * events for all keys. If specific patterns are returned, the listener
     * will only receive events for keys matching those patterns.
     * </p>
     * 
     * @return array of key patterns (supports wildcards), or null/empty for all keys
     */
    default String[] getInterestedKeys() {
        return null; // Listen to all keys by default
    }

    /**
     * Gets the priority of this listener.
     * 
     * <p>
     * Listeners with higher priority values are called before listeners
     * with lower priority values. The default priority is 0.
     * </p>
     * 
     * @return the priority (higher values = higher priority)
     */
    default int getPriority() {
        return 0;
    }
}
