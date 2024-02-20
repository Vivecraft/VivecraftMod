package org.vivecraft.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.common.utils.lwjgl.Matrix4f;
import org.vivecraft.common.utils.math.Quaternion;

import java.nio.Buffer;
import java.nio.FloatBuffer;

public record VrPlayerState(boolean seated, Pose hmd, boolean reverseHands, Pose controller0,
                            boolean reverseHands1legacy, Pose controller1) implements BufferSerializable {

    public static VrPlayerState create(VRPlayer vrPlayer) {
        return new VrPlayerState(
            ClientDataHolderVR.getInstance().vrSettings.seated,
            hmdPose(vrPlayer),
            ClientDataHolderVR.getInstance().vrSettings.reverseHands,
            controllerPose(vrPlayer, 0),
            ClientDataHolderVR.getInstance().vrSettings.reverseHands,
            controllerPose(vrPlayer, 1)
        );
    }

    private static Pose hmdPose(VRPlayer vrPlayer) {
        FloatBuffer floatbuffer = vrPlayer.vrdata_world_post.hmd.getMatrix().toFloatBuffer();
        ((Buffer) floatbuffer).rewind();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.load(floatbuffer);
        Vec3 vec3 = vrPlayer.vrdata_world_post.getEye(RenderPass.CENTER).getPosition().subtract(Minecraft.getInstance().player.position());
        Quaternion quaternion = new Quaternion(matrix4f);
        return new Pose(vec3, quaternion);
    }

    private static Pose controllerPose(VRPlayer vrPlayer, int i) {
        Vec3 position = vrPlayer.vrdata_world_post.getController(i).getPosition().subtract(Minecraft.getInstance().player.position());
        FloatBuffer floatbuffer1 = vrPlayer.vrdata_world_post.getController(i).getMatrix().toFloatBuffer();
        ((Buffer) floatbuffer1).rewind();
        Matrix4f matrix4f1 = new Matrix4f();
        matrix4f1.load(floatbuffer1);
        Quaternion orientation = new Quaternion(matrix4f1);
        return new Pose(position, orientation);
    }

    public static VrPlayerState deserialize(FriendlyByteBuf byteBuf) {
        return new VrPlayerState(byteBuf.readBoolean(), Pose.deserialize(byteBuf), byteBuf.readBoolean(), Pose.deserialize(byteBuf), byteBuf.readBoolean(), Pose.deserialize(byteBuf));
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
