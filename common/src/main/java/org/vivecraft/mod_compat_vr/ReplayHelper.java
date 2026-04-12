package org.vivecraft.mod_compat_vr;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import org.vivecraft.Xplat;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.common.network.VrPlayerState;
import org.vivecraft.common.network.packet.s2c.UberPacketPayloadS2C;
import org.vivecraft.common.network.packet.s2c.VRActivePayloadS2C;
import org.vivecraft.mod_compat_vr.flashback.FlashBackHelper;
import org.vivecraft.mod_compat_vr.replaymod.ReplayModHelper;

public class ReplayHelper {
    public static boolean isLoaded() {
        return ReplayModHelper.isLoaded() || FlashBackHelper.isLoaded();
    }

    public static void storePlayerData(VRPlayer vrPlayer) {
        if (!ClientVRPlayers.GOT_LOCAL_PLAYER_INFO && Minecraft.getInstance().player != null) {
            UberPacketPayloadS2C payload = new UberPacketPayloadS2C(
                Minecraft.getInstance().player.getUUID(),
                VrPlayerState.create(vrPlayer),
                ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_post.worldScale,
                AutoCalibration.getPlayerHeight() / AutoCalibration.DEFAULT_HEIGHT);
            storePacket(Xplat.INSTANCE.getS2CPacket(payload));
        }
    }

    public static void storeVRActive(boolean active) {
        if (!ClientVRPlayers.GOT_LOCAL_PLAYER_INFO && Minecraft.getInstance().player != null) {
            VRActivePayloadS2C payload = new VRActivePayloadS2C(active, Minecraft.getInstance().player.getUUID());
            storePacket(Xplat.INSTANCE.getS2CPacket(payload));
        }
    }

    private static void storePacket(Packet<?> packet) {
        if (FlashBackHelper.isLoaded()) {
            FlashBackHelper.storePacket(packet);
        }
        if (ReplayModHelper.isLoaded()) {
            ReplayModHelper.storePacket(packet);
        }
    }
}
