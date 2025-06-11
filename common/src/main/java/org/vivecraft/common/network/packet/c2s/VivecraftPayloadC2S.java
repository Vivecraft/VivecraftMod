package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.PayloadIdentifier;
import org.vivecraft.common.network.packet.VivecraftPayload;
import org.vivecraft.server.ServerNetworking;

/**
 * Vivecraft packet sent from Clients to the Server
 */
public interface VivecraftPayloadC2S extends VivecraftPayload {

    StreamCodec<FriendlyByteBuf, VivecraftPayloadC2S> CODEC = CustomPacketPayload.codec(VivecraftPayloadC2S::write,
        VivecraftPayloadC2S::readPacket);

    Type<VivecraftPayloadC2S> TYPE = new Type<>(CommonNetworkHelper.CHANNEL);

    /**
     * @return ResourceLocation identifying this packet
     */
    @Override
    default Type<VivecraftPayloadC2S> type() {
        return TYPE;
    }

    /**
     * creates the correct VivecraftPacket based on the {@link PayloadIdentifier} stored in the first byte
     *
     * @param buffer Buffer to read the VivecraftPacket from
     * @return parsed VivecraftPacket
     */
    static VivecraftPayloadC2S readPacket(FriendlyByteBuf buffer) {
        int index = buffer.readByte();
        if (index < PayloadIdentifier.values().length) {
            PayloadIdentifier id = PayloadIdentifier.values()[index];
            return switch (id) {
                case VERSION -> VersionPayloadC2S.read(buffer);
                case HEADDATA -> LegacyHeadDataPayloadC2S.read(buffer);
                case CONTROLLER0DATA -> LegacyController0DataPayloadC2S.read(buffer);
                case CONTROLLER1DATA -> LegacyController1DataPayloadC2S.read(buffer);
                case WORLDSCALE -> WorldScalePayloadC2S.read(buffer);
                case DRAW -> DrawPayloadC2S.read(buffer);
                case TELEPORT -> TeleportPayloadC2S.read(buffer);
                case CLIMBING -> new ClimbingPayloadC2S();
                case HEIGHT -> HeightPayloadC2S.read(buffer);
                case ACTIVEHAND -> ActiveBodyPartPayloadC2S.read(buffer);
                case CRAWL -> CrawlPayloadC2S.read(buffer);
                case IS_VR_ACTIVE -> VRActivePayloadC2S.read(buffer);
                case VR_PLAYER_STATE -> VRPlayerStatePayloadC2S.read(buffer);
                default -> {
                    ServerNetworking.LOGGER.error("Vivecraft: Got unexpected payload identifier on server: {}", id);
                    yield UnknownPayloadC2S.read(buffer);
                }
            };
        } else {
            ServerNetworking.LOGGER.error("Vivecraft: Got unknown payload identifier on server: {}", index);
            return UnknownPayloadC2S.read(buffer);
        }
    }
}
