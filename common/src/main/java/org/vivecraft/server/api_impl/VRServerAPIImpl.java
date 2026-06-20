package org.vivecraft.server.api_impl;

import net.minecraft.server.level.ServerPlayer;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.ViveVersion;
import org.vivecraft.api.server.VRServerAPI;
import org.vivecraft.common.api_impl.VRAPIImpl;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

import javax.annotation.Nullable;

public class VRServerAPIImpl implements VRServerAPI {

    public static final VRServerAPIImpl INSTANCE = new VRServerAPIImpl();

    @Override
    public boolean hasVivecraft(ServerPlayer player) {
        return ServerVRPlayers.getVivePlayer(player) != null;
    }

    @Nullable
    @Override
    public ViveVersion getVivecraftVersion(ServerPlayer player) {
        ServerVivePlayer serverVivePlayer = ServerVRPlayers.getVivePlayer(player);
        if (serverVivePlayer == null) {
            return null;
        } else {
            return serverVivePlayer.version;
        }
    }

    @Override
    public void sendHapticPulse(
        ServerPlayer player, VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay)
    {
        if (VRAPIImpl.INSTANCE.isVRPlayer(player)) {
            ServerNetworking.sendHapticToClient(player, bodyPart, duration, frequency, amplitude, delay);
        }
    }
}
