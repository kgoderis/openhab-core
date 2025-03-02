/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.core.audio;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.PercentType;

/**
 * Definition of an audio output like headphones, a speaker or for writing to
 * a file / clip.
 *
 * @author Harald Kuhn - Initial contribution
 * @author Kelly Davis - Modified to match discussion in #584
 * @author Christoph Weitkamp - Added getSupportedStreams() and UnsupportedAudioStreamException
 */
@NonNullByDefault
public interface AudioSink {

    /**
     * Returns a simple string that uniquely identifies this service
     *
     * @return an id that identifies this service
     */
    String getId();

    /**
     * Returns a localized human readable label that can be used within UIs.
     *
     * @param locale the locale to provide the label for
     * @return a localized string to be used in UIs
     */
    @Nullable
    String getLabel(@Nullable Locale locale);

    /**
     * Processes the passed {@link AudioStream}
     *
     * If the passed {@link AudioStream} is not supported by this instance, an {@link UnsupportedAudioStreamException}
     * is thrown.
     *
     * If the passed {@link AudioStream} has an {@link AudioFormat} not supported by this instance,
     * an {@link UnsupportedAudioFormatException} is thrown.
     *
     * In case the audioStream is null, this should be interpreted as a request to end any currently playing stream.
     *
     * When the stream is not needed anymore, if the stream implements the {@link org.openhab.core.common.Disposable}
     * interface, the sink should hereafter get rid of it by calling the dispose method.
     *
     * @param audioStream the audio stream to play or null to keep quiet
     * @throws UnsupportedAudioFormatException If audioStream format is not supported
     * @throws UnsupportedAudioStreamException If audioStream is not supported
     * @deprecated Use {@link AudioSink#processAndComplete(AudioStream)}
     */
    @Deprecated
    void process(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException;

    /**
     * Processes the passed {@link AudioStream}, and returns a CompletableFuture that should complete when the sound is
     * fully played. It is the sink responsibility to complete this future.
     *
     * If the passed {@link AudioStream} is not supported by this instance, an {@link UnsupportedAudioStreamException}
     * is thrown.
     *
     * If the passed {@link AudioStream} has an {@link AudioFormat} not supported by this instance,
     * an {@link UnsupportedAudioFormatException} is thrown.
     *
     * In case the audioStream is null, this should be interpreted as a request to end any currently playing stream.
     *
     * When the stream is not needed anymore, if the stream implements the {@link org.openhab.core.common.Disposable}
     * interface, the sink should hereafter get rid of it by calling the dispose method.
     *
     * @param audioStream the audio stream to play or null to keep quiet
     * @return A future completed when the sound is fully played. The method can instead complete with
     *         UnsupportedAudioFormatException if the audioStream format is not supported, or
     *         UnsupportedAudioStreamException If audioStream is not supported
     */
    default CompletableFuture<@Nullable Void> processAndComplete(@Nullable AudioStream audioStream) {
        try {
            process(audioStream);
        } catch (UnsupportedAudioFormatException | UnsupportedAudioStreamException e) {
            return CompletableFuture.failedFuture(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Gets a set containing all supported audio formats
     *
     * @return A Set containing all supported audio formats
     */
    Set<AudioFormat> getSupportedFormats();

    /**
     * Gets a set containing all supported audio stream formats
     *
     * @return A Set containing all supported audio stream formats
     */
    Set<Class<? extends AudioStream>> getSupportedStreams();

    /**
     * Gets the volume
     *
     * @return a PercentType value between 0 and 100 representing the actual volume
     * @throws IOException if the volume can not be determined
     */
    PercentType getVolume() throws IOException;

    /**
     * Sets the volume
     *
     * @param volume a PercentType value between 0 and 100 representing the desired volume
     * @throws IOException if the volume can not be set
     */
    void setVolume(PercentType volume) throws IOException;
}
