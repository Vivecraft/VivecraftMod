package org.vivecraft.client.api_impl.convert;

import net.minecraft.world.entity.player.Player;
import org.vivecraft.api_beta.data.VRData;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.common.api_impl.data.VRDataImpl;
import org.vivecraft.common.api_impl.data.VRPoseImpl;

public class VRDataClientVivePlayers {

    public static VRData fromClientVivePlayers(Player player) {
        VRPlayersClient.RotInfo rotInfo = VRPlayersClient.getInstance().getRotationsForPlayer(player.getUUID());
        return new VRDataImpl(
                new VRPoseImpl(rotInfo.Headpos, rotInfo.headRot, rotInfo.headQuat.toEuler().getRoll()),
                new VRPoseImpl(rotInfo.rightArmPos, rotInfo.rightArmRot, rotInfo.rightArmQuat.toEuler().getRoll()),
                new VRPoseImpl(rotInfo.leftArmPos, rotInfo.leftArmRot, rotInfo.leftArmQuat.toEuler().getRoll()),
                rotInfo.seated,
                rotInfo.reverse
        );
    }
}
