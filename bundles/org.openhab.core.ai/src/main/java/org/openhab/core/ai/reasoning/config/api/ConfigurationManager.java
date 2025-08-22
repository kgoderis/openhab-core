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
package org.openhab.core.ai.reasoning.config.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.validation.ConfigurationValidationResult;

/**
 * Interface for configuration management in the AI reasoning system.
 * 
 * This interface provides a common abstraction for configuration operations,
 * ensuring consistent behavior across all reasoning components.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ConfigurationManager {

    /**
     * Get configuration value for a key.
     * 
     * @param key The configuration key
     * @return A CompletableFuture containing the configuration value
     */
    CompletableFuture<@Nullable Object> getConfiguration(String key);

    /**
     * Set configuration value for a key.
     * 
     * @param key The configuration key
     * @param value The configuration value
     * @return A CompletableFuture containing the operation result
     */
    CompletableFuture<Boolean> setConfiguration(String key, Object value);

    /**
     * Get all configuration values.
     * 
     * @return A CompletableFuture containing all configuration values
     */
    CompletableFuture<Map<String, Object>> getAllConfiguration();

    /**
     * Validate configuration.
     * 
     * @param configuration The configuration to validate
     * @return A CompletableFuture containing the validation result
     */
    CompletableFuture<ConfigurationValidationResult> validateConfiguration(Map<String, Object> configuration);

    /**
     * Backup configuration.
     * 
     * @return A CompletableFuture containing the backup result
     */
    CompletableFuture<ConfigurationBackupResult> backupConfiguration();

    /**
     * Restore configuration from backup.
     * 
     * @param backupId The backup identifier
     * @return A CompletableFuture containing the restore result
     */
    CompletableFuture<Boolean> restoreConfiguration(String backupId);

    /**
     * Get configuration version.
     * 
     * @return A CompletableFuture containing the configuration version
     */
    CompletableFuture<String> getConfigurationVersion();

    // Extracted: ConfigurationValidationResult, ConfigurationBackupResult
}
