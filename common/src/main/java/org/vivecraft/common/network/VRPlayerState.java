package org.vivecraft.common.network;

import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.render.RenderPass;

import org.joml.Quaternionf;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public record VRPlayerState(boolean seated, Pose hmd, boolean reverseHands, Pose controller0, boolean reverseHands1legacy, Pose controller1) {

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
        Vec3 vec3 = vrPlayer.vrdata_world_post.getEye(RenderPass.CENTER).getPosition().subtract(mc.player.position());
        Quaternionf quaternion = new Quaternionf().setFromNormalized(vrPlayer.vrdata_world_post.hmd.getMatrix());
        return new Pose(vec3, quaternion);
    }

    private static Pose controllerPose(VRPlayer vrPlayer, int i) {
        Vec3 position = vrPlayer.vrdata_world_post.getController(i).getPosition().subtract(mc.player.position());
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
