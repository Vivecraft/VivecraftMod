package org.vivecraft.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.render.RenderPass;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.convertToVec3;

public record VRPlayerState(boolean seated, Pose hmd, boolean reverseHands, Pose controller0,
                            boolean reverseHands1legacy, Pose controller1) {

    public static VRPlayerState create(VRPlayer vrPlayer) {
        return new VRPlayerState(
            dh.vrSettings.seated,
            hmdPose(vrPlayer),
            dh.vrSettings.reverseHands,
            controllerPose(vrPlayer, 0),
            dh.vrSettings.reverseHands,
            controllerPose(vrPlayer, 1)
        );
    }

    private static Pose hmdPose(VRPlayer vrPlayer) {
        Vec3 vec3 = convertToVec3(vrPlayer.vrdata_world_post.getEye(RenderPass.CENTER).getPosition(new Vector3f())).subtract(mc.player.position());
        Quaternionf quaternion = new Quaternionf().setFromNormalized(vrPlayer.vrdata_world_post.hmd.getMatrix());
        return new Pose(vec3, quaternion);
    }

    private static Pose controllerPose(VRPlayer vrPlayer, int i) {
        Vec3 position = convertToVec3(vrPlayer.vrdata_world_post.getController(i).getPosition(new Vector3f())).subtract(mc.player.position());
        Quaternionf orientation = new Quaternionf().setFromNormalized(vrPlayer.vrdata_world_post.getController(i).getMatrix());
        return new Pose(position, orientation);
    }

    public static VRPlayerState deserialize(FriendlyByteBuf byteBuf) {
        return new VRPlayerState(byteBuf.readBoolean(), Pose.deserialize(byteBuf), byteBuf.readBoolean(), Pose.deserialize(byteBuf), byteBuf.readBoolean(), Pose.deserialize(byteBuf));
    }

    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.seated);
        this.hmd.serialize(buffer);
        buffer.writeBoolean(this.reverseHands);
        this.controller0.serialize(buffer);
        buffer.writeBoolean(this.reverseHands);
        this.controller1.serialize(buffer);
    }
}
