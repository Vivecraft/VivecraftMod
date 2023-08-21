package org.vivecraft.server.api_impl.convert;

import net.minecraft.server.level.ServerPlayer;
import org.vivecraft.api_beta.data.VRData;
import org.vivecraft.common.api_impl.data.VRDataImpl;
import org.vivecraft.common.api_impl.data.VRPoseImpl;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

public class VRDataServerVivePlayer {

    public static VRData fromServerVivePlayer(ServerPlayer player) {
        ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(player);
        return new VRDataImpl(
                new VRPoseImpl(vivePlayer.getHMDPos(player), vivePlayer.getHMDDir(), vivePlayer.getHMDRollDeg()),
                new VRPoseImpl(vivePlayer.getControllerPos(0, player), vivePlayer.getControllerDir(0), vivePlayer.getControllerRollDeg(0)),
                new VRPoseImpl(vivePlayer.getControllerPos(1, player), vivePlayer.getControllerDir(1), vivePlayer.getControllerRollDeg(1)),
                vivePlayer.isSeated(),
                vivePlayer.usingReversedHands()
        );
    }
}
