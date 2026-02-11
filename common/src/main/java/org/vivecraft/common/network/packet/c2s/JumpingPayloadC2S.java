package org.vivecraft.common.network.packet.c2s;

import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * indicates that the client did a climbey jump
 */
public record JumpingPayloadC2S() implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.JUMPING;
    }
}
