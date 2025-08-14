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
package org.openhab.core.ai.agent.collaboration.negotiation;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Extracted details holder for negotiation proposals.
 *
 * @author Karel Goderis - Initial Contribution
 * @since 5.0.0
 */
@NonNullByDefault
public final class NegotiationProposalDetails {
    private final String proposalId;
    private final Map<String, Object> terms;

    public NegotiationProposalDetails(String proposalId, Map<String, Object> terms) {
        this.proposalId = proposalId;
        this.terms = Map.copyOf(terms);
    }

    public String getProposalId() { return proposalId; }
    public Map<String, Object> getTerms() { return terms; }
}


