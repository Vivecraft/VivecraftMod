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

/**
 * holds all data from a player
 * @param seated if the player is in seated mode
 * @param hmd device Pose of the headset
 * @param reverseHands if true, {@code controller0} is the left hand, else {@code controller1} is
 * @param controller0 device Pose of the main interaction hand
 * @param reverseHands1legacy same as {@code reverseHands}, just here for legacy compatibility
 * @param controller1 device Pose of the offhand
 */
public record VrPlayerState(boolean seated, Pose hmd, boolean reverseHands, Pose controller0,
                            boolean reverseHands1legacy, Pose controller1) {

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

    /**
     * creates the headset Pose object from the client vr data
     * @param vrPlayer object holding the client data
     * @return headset Pose object of the current state
     */
    private static Pose hmdPose(VRPlayer vrPlayer) {
        FloatBuffer tempBuffer = vrPlayer.vrdata_world_post.hmd.getMatrix().toFloatBuffer();
        ((Buffer) tempBuffer).rewind();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.load(tempBuffer);

        Vec3 position = vrPlayer.vrdata_world_post.getEye(RenderPass.CENTER).getPosition()
            .subtract(Minecraft.getInstance().player.position());
        Quaternion orientation = new Quaternion(matrix4f);
        return new Pose(position, orientation);
    }

    /**
     * creates the controller Pose object for the specified controller, from the client vr data
     * @param vrPlayer object holding the client data
     * @param controller index of the controller to get the Pose for
     * @return headset Pose object of the current state
     */
    private static Pose controllerPose(VRPlayer vrPlayer, int controller) {
        FloatBuffer tempBuffer = vrPlayer.vrdata_world_post.getController(controller).getMatrix().toFloatBuffer();
        ((Buffer) tempBuffer).rewind();
        Matrix4f matrix4f1 = new Matrix4f();
        matrix4f1.load(tempBuffer);

        Vec3 position = vrPlayer.vrdata_world_post.getController(controller).getPosition()
            .subtract(Minecraft.getInstance().player.position());
        Quaternion orientation = new Quaternion(matrix4f1);
        return new Pose(position, orientation);
    }

    /**
     * @param buffer buffer to read from
     * @return a VrPlayerState read from the given {@code buffer}
     */
    public static VrPlayerState deserialize(FriendlyByteBuf buffer) {
        return new VrPlayerState(
            buffer.readBoolean(),
            Pose.deserialize(buffer),
            buffer.readBoolean(),
            Pose.deserialize(buffer),
            buffer.readBoolean(),
            Pose.deserialize(buffer)
        );
    }

    /**
     * writes this VrPlayerState to the given {@code buffer}
     * @param buffer buffer to write to
     */
    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.seated);
        this.hmd.serialize(buffer);
        buffer.writeBoolean(this.reverseHands);
        this.controller0.serialize(buffer);
        buffer.writeBoolean(this.reverseHands);
        this.controller1.serialize(buffer);
    }
}
