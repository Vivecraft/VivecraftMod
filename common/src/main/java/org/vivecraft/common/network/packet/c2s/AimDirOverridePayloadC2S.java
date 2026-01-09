package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3fc;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * holds a direction the aim should be overridden to
 *
 * @param direction direction the player aimed in
 */
public record AimDirOverridePayloadC2S(Vector3fc direction) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.AIM_DIRECTION_OVERRIDE;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        CommonNetworkHelper.serializeF(buffer, this.direction);
    }

    public static AimDirOverridePayloadC2S read(FriendlyByteBuf buffer) {
        return new AimDirOverridePayloadC2S(CommonNetworkHelper.deserializeFVec3(buffer));
    }
}
