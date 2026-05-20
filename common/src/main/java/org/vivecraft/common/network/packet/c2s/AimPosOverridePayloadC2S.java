package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3fc;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * holds a position the aim should start from
 *
 * @param position position the player aims from
 */
public record AimPosOverridePayloadC2S(Vector3fc position) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.AIM_POSITION_OVERRIDE;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        CommonNetworkHelper.serializeF(buffer, this.position);
    }

    public static AimPosOverridePayloadC2S read(FriendlyByteBuf buffer) {
        return new AimPosOverridePayloadC2S(CommonNetworkHelper.deserializeFVec3(buffer));
    }
}
