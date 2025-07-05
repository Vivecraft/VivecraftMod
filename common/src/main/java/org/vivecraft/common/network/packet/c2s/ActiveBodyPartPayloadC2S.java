package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * holds the clients current active BodyPart, that will cause the next action
 *
 * @param bodyPart  the active BodyPart
 * @param useForAim when set, will use this BodyPart for aim when using items, when unset, the server can stil decide to use it for aiming in certain situations
 */
public record ActiveBodyPartPayloadC2S(VRBodyPart bodyPart, boolean useForAim) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.ACTIVEHAND;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeByte(this.bodyPart.ordinal());
        if (ClientNetworking.USED_NETWORK_VERSION >= CommonNetworkHelper.NETWORK_VERSION_HEAD_AIM) {
            buffer.writeBoolean(this.useForAim);
        }
    }

    public static ActiveBodyPartPayloadC2S read(FriendlyByteBuf buffer) {
        VRBodyPart bodyPart = VRBodyPart.values()[buffer.readByte()];
        boolean useForAim = false;
        if (buffer.readableBytes() > 0) {
            useForAim = buffer.readBoolean();
        }
        return new ActiveBodyPartPayloadC2S(bodyPart, useForAim);
    }
}
