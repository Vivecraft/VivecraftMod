package org.vivecraft.common.network.packet.c2s;

import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * indicates that the client requested damage direction data
 */
public record DamageDirectionPayloadC2S() implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.DAMAGE_DIRECTION;
    }
}
