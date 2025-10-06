package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3fc;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * contains the last direction the player got damage from
 */
public record DamageDirectionPayloadS2C(Vector3fc damageDir) implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.DAMAGE_DIRECTION;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        CommonNetworkHelper.serializeF(buffer, this.damageDir);
    }

    public static DamageDirectionPayloadS2C read(FriendlyByteBuf buffer) {
        return new DamageDirectionPayloadS2C(CommonNetworkHelper.deserializeFVec3(buffer));
    }
}
