package org.vivecraft.common.network.packet.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.PayloadIdentifier;
import org.vivecraft.common.network.packet.VivecraftPayload;

/**
 * Vivecraft packet sent from the Server to the Clients
 */
public interface VivecraftPayloadS2C extends VivecraftPayload {

    StreamCodec<FriendlyByteBuf, VivecraftPayloadS2C> CODEC = CustomPacketPayload.codec(VivecraftPayloadS2C::write,
        VivecraftPayloadS2C::readPacket);

    Type<VivecraftPayloadS2C> TYPE = new Type<>(CommonNetworkHelper.CHANNEL);

    /**
     * @return ResourceLocation identifying this packet
     */
    @Override
    default Type<VivecraftPayloadS2C> type() {
        return TYPE;
    }

    /**
     * creates the correct VivecraftPacket based on the {@link PayloadIdentifier} stored in the first byte
     *
     * @param buffer Buffer to read the VivecraftPacket from
     * @return parsed VivecraftPacket
     */
    static VivecraftPayloadS2C readPacket(FriendlyByteBuf buffer) {
        int index = buffer.readByte();
        if (index < PayloadIdentifier.values().length) {
            PayloadIdentifier id = PayloadIdentifier.values()[index];
            return switch (id) {
                case VERSION -> VersionPayloadS2C.read(buffer);
                case REQUESTDATA -> new RequestDataPayloadS2C();
                case UBERPACKET -> UberPacketPayloadS2C.read(buffer);
                case TELEPORT -> new TeleportPayloadS2C();
                case CLIMBING -> ClimbingPayloadS2C.read(buffer);
                case SETTING_OVERRIDE -> SettingOverridePayloadS2C.read(buffer);
                case CRAWL -> new CrawlPayloadS2C();
                case NETWORK_VERSION -> NetworkVersionPayloadS2C.read(buffer);
                case VR_SWITCHING -> VRSwitchingPayloadS2C.read(buffer);
                case IS_VR_ACTIVE -> VRActivePayloadS2C.read(buffer);
                case DUAL_WIELDING -> DualWieldingPayloadS2C.read(buffer);
                default -> {
                    VRSettings.LOGGER.error("Vivecraft: Got unexpected payload identifier on client: {}", id);
                    yield UnknownPayloadS2C.read(buffer);
                }
            };
        } else {
            VRSettings.LOGGER.error("Vivecraft: Got unknown payload identifier on client: {}", index);
            return UnknownPayloadS2C.read(buffer);
        }
    }
}
