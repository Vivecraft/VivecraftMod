package org.vivecraft.server.api_impl;

import net.minecraft.server.level.ServerPlayer;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.server.VRServerAPI;
import org.vivecraft.common.api_impl.VRAPIImpl;
import org.vivecraft.server.ServerNetworking;

public class VRServerAPIImpl implements VRServerAPI {

    public static final VRServerAPIImpl INSTANCE = new VRServerAPIImpl();

    @Override
    public void sendHapticPulse(
        ServerPlayer player, VRBodyPart bodyPart, float duration, float frequency, float amplitude, float delay)
    {
        if (VRAPIImpl.INSTANCE.isVRPlayer(player)) {
            ServerNetworking.sendHapticToClient(player, bodyPart, duration, frequency, amplitude, delay);
        }
    }
}
