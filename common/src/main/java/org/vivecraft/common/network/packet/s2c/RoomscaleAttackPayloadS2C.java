package org.vivecraft.common.network.packet.s2c;

import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * indicates that the server supports the roomscale attack packet
 *
 */
public record RoomscaleAttackPayloadS2C() implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.ROOMSCALE_ATTACK;
    }
}
